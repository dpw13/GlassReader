package com.tryinglobster.glassreader;

import java.util.HashMap;

import com.android.theoldreader.Feed;
import com.android.theoldreader.Folder;

import android.os.Bundle;
import android.os.IBinder;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;

public class MainActivity extends Activity {
    private static final String TAG = "ReaderMain";

    public static final String FEED_ID = "com.tryinglobster.glassreader.FEED_ID";
    public static final String FEED_URI = "com.tryinglobster.glassreader.FEED_URI";
    
    private ReaderService.ReaderBinder mService;
	private boolean mResumed;
	
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (service instanceof ReaderService.ReaderBinder) {
                mService = (ReaderService.ReaderBinder) service;
                openOptionsMenu();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // Do nothing.
        }
    };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		bindService(new Intent(this, ReaderService.class), mConnection, 0);
	}

    @Override
    public void onResume() {
        super.onResume();
        mResumed = true;
        openOptionsMenu();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mResumed = false;
    }

    @Override
    public void openOptionsMenu() {
        if (mResumed && mService != null) {
            super.openOptionsMenu();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
    	HashMap<String,Folder> hmFolders = mService.getFolders();

		for(Folder f: hmFolders.values()) {
			if(f.iUnread > 0) {
				Log.i(TAG,"Folder "+f.sName+": "+f.iUnread);
				SubMenu sm = menu.addSubMenu(f.sName+" ("+f.iUnread+")");

				for(Feed oFeed: f.lFeeds) {
					if(oFeed.iUnread > 0) {
						Log.i(TAG,"Feed "+oFeed.sName+": "+oFeed.iUnread);		
						MenuItem m = sm.add(oFeed.sName+" ("+oFeed.iUnread+")");
						Intent i = new Intent(Intent.ACTION_VIEW, oFeed.uId);
						i.putExtra(FEED_URI, oFeed.sHtmlUrl);
						m.setIntent(i);
					}
				}
			}
		}

    	return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection.
        switch (item.getItemId()) {
            case R.id.stop:
                stopService(new Intent(this, ReaderService.class));
                return true;
            case R.id.refresh:
                mService.refresh();
                return true;
            case R.id.action_settings:
            	// TODO
            	return true;
            default:
            	Intent i = item.getIntent();
            	Log.d(TAG, "MenuItem intent is "+i);
            	if(i != null) {
            		String feedId = i.getData().getSchemeSpecificPart();
            		String feedUri = i.getStringExtra(FEED_URI);
            		Log.d(TAG, "Feed ID is "+feedId+" at "+feedUri);

            		Intent intent = new Intent(this, FeedActivity.class);
            		intent.putExtra(FEED_ID, feedId);
            		intent.putExtra(FEED_URI, feedUri);
            		startActivity(intent);
            	}
            	return true;
        }
    }

    @Override
    public void onOptionsMenuClosed(Menu menu) {
        super.onOptionsMenuClosed(menu);
        unbindService(mConnection);
        // We must call finish() from this method to ensure that the activity ends either when an
        // item is selected from the menu or when the menu is dismissed by swiping down.
        finish();
    }

}
