package com.android.theoldreader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class Retriever {
	private static final String TAG = "Retriever";

    private static final String READER_URL = "http://theoldreader.com/reader/api/0/";
    private static final String CHARSET = "UTF-8";

    private static DefaultHttpClient httpclient = new DefaultHttpClient(new BasicHttpParams());
    private String sAuth = null;
    private String sError = null;

    private int iTotalUnread;
    private HashMap<String,Folder> hmFolders = new HashMap<String,Folder>();
    private HashMap<String,Feed> hmFeeds = new HashMap<String,Feed>();
    
    public Retriever() {}

    public void setAuth(String auth) {
    	sAuth = auth;
    }
    
    private JSONObject _execute(String method, List<NameValuePair> params) {
    	if(params == null) {
    		params = new ArrayList<NameValuePair>(1);
    	}
    	//params.add(new BasicNameValuePair("output", "json"));

    	String uri = READER_URL+method+"?output=json";
    	
    	try {
    		for(NameValuePair nvp : params) {
    			uri += "&" +
    					URLEncoder.encode(nvp.getName(), CHARSET) + 
    					"=" + 
    					URLEncoder.encode(nvp.getValue(), CHARSET);
    		}
    	} catch (UnsupportedEncodingException e) {
    		// TODO Auto-generated catch block
    		e.printStackTrace();
    	}
    	
    	System.out.println("Retrieving "+uri);
    	/*
    	//the URL we will send the request to
    	URL reqURL = null;
		try {
			reqURL = new URL(uri);
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			sError = "Malformed URL: "+uri;
			return null;
		}

		HttpURLConnection request;
		try {
			request = (HttpURLConnection) (reqURL.openConnection());
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			sError = "IOException when opening connection";
			return null;
		}
        try {
			request.setRequestMethod("GET");
		} catch (ProtocolException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			sError = "Bad HTTP protocol";
			return null;
		}
        try {
			request.connect();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			sError = "Error connecting to "+reqURL;
		}
        */
        HttpGet transport = new HttpGet(uri);
    	// Depends on your web service
    	
    	InputStream inputStream = null;
    	String result = null;
    	try {
    		//transport.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
    		if(sAuth != null) {
    			transport.setHeader("Authorization", "GoogleLogin auth="+sAuth);
    		}
    		HttpResponse response = httpclient.execute(transport);
    		HttpEntity entity = response.getEntity();

    		inputStream = entity.getContent();
    		// json is UTF-8 by default
    		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);
    		StringBuilder sb = new StringBuilder();

    		String line = null;
    		while ((line = reader.readLine()) != null)
    		{
    			sb.append(line + "\n");
    		}
    		result = sb.toString();
    	} catch (Exception e) { 
    		if(e instanceof UnknownHostException) {
    			sError = "Could not resolve host name";
    		} else if(e instanceof IOException) {
    			sError = "IOException";
    		} else {
    			sError = "Unknown exception when retrieving JSON";
    		}
    		// Oops
    		e.printStackTrace();
    	}
    	finally {
    		try{if(inputStream != null)inputStream.close();}catch(Exception squish){}
    	}

    	//System.out.println("Got: "+result);
    	
    	JSONObject ret = null;
    	if(result != null) {
    		try {
    			ret = new JSONObject(result);
    		} catch (JSONException e) {
    			e.printStackTrace();
    		}
    	}

    	if(ret != null) {
        	try {
    			sError = ret.getString("error");
        	} catch (JSONException e) {
        		// ignore if no error
        		sError = null;
    		}
    	}
    	
    	return ret;
    }

    public String getError() {
    	return sError;
    }
    
    public boolean isUp() {
    	boolean up = false;
    	JSONObject j = _execute("status", null);    	
    	
    	if(j != null && sError == null) {
    		try {
    			String sApiStatus = j.getString("status");
    			up = sApiStatus.equals("up");
    		} catch (JSONException e) {
        		// ignore if no error
    		}

    	}

    	return up;
    }
    
    public void refreshUnread() {
    	JSONObject j = _execute("unread-count", null);

    	try {
			JSONArray jUnread = j.getJSONArray("unreadcounts");
			for(int i=0;i<jUnread.length();i++) {
				JSONObject jObj = jUnread.getJSONObject(i);
				String sId = jObj.getString("id");
				if(sId.equals("user/-/state/com.google/reading-list")) {
					iTotalUnread = jObj.getInt("count");
				} else if(sId.startsWith(Folder.FOLDER_ID_PREFIX)) {
					Folder f = hmFolders.get(sId);
					if(f == null) {
						f = new Folder();
					}
					f.loadFromJSON(jObj);
					hmFolders.put(sId, f);
				} else if(sId.startsWith(Feed.FEED_ID_PREFIX)) {
					Feed f = hmFeeds.get(sId);
					if(f == null) {
						f = new Feed();
					}
					f.loadUnreadFromJSON(jObj);
					hmFeeds.put(sId, f);
				} else {
					Log.w(TAG,"Did not understand id "+sId);
				}
			}
    	} catch (JSONException e) {
    		Log.e(TAG,"Invalid JSON message: " + e);
		}
    }

    public void refreshFeeds() {
    	JSONObject j = _execute("subscription/list", null);

    	try {
			JSONArray jFeeds = j.getJSONArray("subscriptions");
			for(int i=0;i<jFeeds.length();i++) {
				JSONObject jObj = jFeeds.getJSONObject(i);
				String sId = jObj.getString("id");
				Feed f = hmFeeds.get(sId);
				if(f == null) {
					f = new Feed();
				}
				f.loadInfoFromJSON(jObj);
				hmFeeds.put(sId, f);				
			}
    	} catch (JSONException e) {
    		Log.e(TAG,"Invalid JSON message: " + e);
		}
    	
    }

    public void linkFeedFolders() {
    	for(Folder oFol : hmFolders.values()) {
    		oFol.lFeeds.clear();
    	}

    	for(Feed oFeed : hmFeeds.values()) {
    		Folder oFol = hmFolders.get(oFeed.sFolderId);
    		if(oFol != null) {
    			// Feed points to the folder...
    			oFeed.fFolder = oFol;
    			// ... and each folder has a list of feeds.
    			oFol.lFeeds.add(oFeed);
    		} else {
    			Log.w(TAG,"Feed "+oFeed.sId+" has an unknown folder");
    		}
    	}
    }
    
    public int getTotalUnreadCount() {
    	return iTotalUnread;
    }

    public HashMap<String,Folder> getFolders() {
    	return hmFolders;
    }

    public HashMap<String,Feed> getFeeds() {
    	return hmFeeds;
    }

    public List<Article> getFeed(String feedId) {
    	List<Article> lArticles = new ArrayList<Article>();
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("s", feedId));
		// For now exclude all read posts
		params.add(new BasicNameValuePair("xt", "user/-/state/com.google/read"));
    	JSONObject j = _execute("stream/contents", params);

    	try {
			JSONArray jArticles = j.getJSONArray("items");
			for(int i=0;i<jArticles.length();i++) {
				JSONObject jObj = jArticles.getJSONObject(i);

				Article a = new Article();
				a.loadFromJSON(jObj);
				Log.d(TAG,"Found article: "+a.sTitle);
				lArticles.add(a);
			}
    	} catch (JSONException e) {
    		Log.e(TAG,"Invalid JSON message: " + e);
		}
    	
    	return lArticles;
    }
};
