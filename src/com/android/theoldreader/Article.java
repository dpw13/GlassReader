package com.android.theoldreader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.net.Uri;
import android.util.Log;

public class Article {
	private static final String TAG = "Article";

	public String sTitle;
	public int iPublished;
	public int iUpdated;
	public String sSummary;
	public String sAuthor;
	public Uri uLink;
	
	public Article() {}

	public void loadFromJSON(JSONObject jObj) {
		try {
			JSONObject jSub;
			JSONArray jArr;
			
			sTitle = jObj.getString("title");
    		iPublished = jObj.getInt("published");
    		iUpdated = jObj.getInt("updated");
    		sAuthor = jObj.getString("author");
    		jSub = jObj.getJSONObject("summary");
    		sSummary = jSub.getString("content");
    		jArr = jObj.getJSONArray("canonical");
    		jSub = jArr.getJSONObject(0);
    		uLink = Uri.parse(jSub.getString("href"));
    	} catch (JSONException e) {
    		Log.e(TAG,"Invalid JSON message: " + e);
		}
	}	
}
