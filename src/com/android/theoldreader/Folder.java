package com.android.theoldreader;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class Folder {
	private static final String TAG = "Folder";
	public static final String FOLDER_ID_PREFIX = "user/-/label/";
	
	public String sId = null;
	public String sName = null;
	public int iUnread = 0;
	public List<Feed> lFeeds = new ArrayList<Feed>(20);
	
	public Folder() {}

	public void loadFromJSON(JSONObject jFolder) {
    	try {
			sId = jFolder.getString("id");
			iUnread = jFolder.getInt("count");
			sName = sId.substring(FOLDER_ID_PREFIX.length());
    	} catch (JSONException e) {
    		sId = null;
    		iUnread = 0;
    		Log.e(TAG,"Invalid JSON message: " + e);
		}
	}
}
