package com.tryinglobster.glassreader;

import java.util.HashMap;
import java.util.List;

import com.android.theoldreader.Article;
import com.android.theoldreader.Feed;
import com.android.theoldreader.Folder;
import com.android.theoldreader.Retriever;
import com.google.android.glass.timeline.LiveCard;
import com.google.android.glass.timeline.TimelineManager;
import com.google.android.glass.timeline.LiveCard.PublishMode;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.TextView;

/**
 * Service owning the LiveCard living in the timeline.
 */
public class ReaderService extends Service {

    private static final String TAG = "ReaderService";
    private static final String LIVE_CARD_ID = "reader";

    private TimelineManager mTimelineManager;
    private LiveCard mLiveCard;
    private RemoteViews mLiveViews;

	private Retriever mRetriever = new Retriever();
    
    public class ReaderBinder extends Binder {
    	public void refresh() {
    		updateFeeds();
    	}
    	
    	public HashMap<String,Folder> getFolders() {
    		return mRetriever.getFolders();
    	}

    	public List<Article> getFeed(String feedId) {
    		return mRetriever.getFeed(feedId);
    	}
    };

    private final ReaderBinder mBinder = new ReaderBinder();
    
    @Override
    public void onCreate() {
        super.onCreate();
        mTimelineManager = TimelineManager.from(this);
    	mRetriever.setAuth("guJhUzZG6eJ5yqjxqSjx");
    }

    @Override
    public IBinder onBind(Intent intent) {
    	/* TODO: use Binder to supply access to feeds to menu activity.
    	 * See Compass example. */
    	return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mLiveCard == null) {
            Log.d(TAG, "Publishing LiveCard");
            mLiveCard = mTimelineManager.createLiveCard(LIVE_CARD_ID);
            mLiveViews = new RemoteViews(this.getPackageName(),
                    R.layout.live_card);
        	mLiveViews.setTextViewText(R.id.unread, "Loading...");
            mLiveCard.setViews(mLiveViews);

            updateFeeds();
            
            Intent menuIntent = new Intent(this, MainActivity.class);
            menuIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            mLiveCard.setAction(PendingIntent.getActivity(this, 0, menuIntent, 0));

            mLiveCard.publish(PublishMode.REVEAL);
            Log.d(TAG, "Done publishing LiveCard");
        } else {
            // TODO(alainv): Jump to the LiveCard when API is available.
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (mLiveCard != null && mLiveCard.isPublished()) {
            Log.d(TAG, "Unpublishing LiveCard");
            mLiveCard.unpublish();
            mLiveCard = null;
        }
        super.onDestroy();
    }

    public void updateFeeds() {
        new Thread(new Runnable() {
            public void run() {
                _updateFeeds();
            }
        }).start();
    }
    
    public void _updateFeeds() {
    	if(mRetriever.isUp()) {
        	mLiveViews.setTextViewText(R.id.unread, "Updating...");
            mLiveCard.setViews(mLiveViews);

    		mRetriever.refreshUnread();
    		mRetriever.refreshFeeds();
    		mRetriever.linkFeedFolders();

    		Log.i(TAG,"Found "+mRetriever.getTotalUnreadCount()+" total unread");

        	mLiveViews.setTextViewText(R.id.unread, 
        			Integer.toString(mRetriever.getTotalUnreadCount()) + " Unread");
            mLiveCard.setViews(mLiveViews);
    	} else {
    		Log.e(TAG,"Service not up: " + mRetriever.getError());

    		mLiveViews.setTextViewText(R.id.unread, "Error updating");
            mLiveCard.setViews(mLiveViews);
    	}
    }
}
