package com.android.theoldreader;

import org.json.JSONException;
import org.json.JSONObject;

import android.net.Uri;
import android.util.Log;

public class Feed {
	private static final String TAG = "Folder";
	public static final String FEED_ID_PREFIX = "feed/";
	
	public String sId = null;
	public Uri uId = null;
	public String sName = null;
	
	public String sFeedUrl = null;
	public String sHtmlUrl = null;
	public String sIconUrl = null;

	public String sFolderId = null;
	public Folder fFolder = null;
	
	public int iUnread = 0;
	
	public Feed() {}

	private void setId(String id) {
		sId = id;
		uId = Uri.fromParts("reader", id, null);
	}
	
	public void loadUnreadFromJSON(JSONObject jFeed) {
    	try {
			setId(jFeed.getString("id"));
			iUnread = jFeed.getInt("count");
    	} catch (JSONException e) {
    		sId = null;
    		iUnread = 0;
    		Log.e(TAG,"Invalid JSON message: " + e);
		}
	}

	public void loadInfoFromJSON(JSONObject jFeed) {
    	try {
			setId(jFeed.getString("id"));
			sName = jFeed.getString("title");
			sFeedUrl = jFeed.getString("url");
			sHtmlUrl = jFeed.getString("htmlUrl");
			sIconUrl = jFeed.getString("iconUrl");
			
			// For now we only support a single folder
			JSONObject jCat = jFeed.getJSONArray("categories").getJSONObject(0);
			sFolderId = jCat.getString("id");
    	} catch (JSONException e) {
    		Log.e(TAG,"Invalid JSON message: " + e);
		}
	}
	
}
