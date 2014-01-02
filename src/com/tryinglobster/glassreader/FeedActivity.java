package com.tryinglobster.glassreader;

import java.util.List;

import com.android.theoldreader.Article;
import com.google.android.glass.app.Card;
import com.google.android.glass.widget.CardScrollView;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.text.Html;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

public class FeedActivity extends Activity {
    private static final String TAG = "FeedActivity";

    private ReaderService.ReaderBinder mService;
	private ArticleCardScrollAdapter mCardScrollAdapter; 
    private CardScrollView mCardScrollView;
	private String mFeedId;
	private Article mMenuArticle;
	private Uri mFeedUri;
	
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (service instanceof ReaderService.ReaderBinder) {
                mService = (ReaderService.ReaderBinder) service;
                new Thread(new Runnable() {
                    public void run() {
                        _updateFeed();
                    }
                }).start();
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

	    Intent intent = getIntent();
		// Feed ID must be set before bind
		mFeedId = intent.getStringExtra(MainActivity.FEED_ID);
		mFeedUri = Uri.parse(intent.getStringExtra(MainActivity.FEED_URI));
		
		bindService(new Intent(this, ReaderService.class), mConnection, 0);

		mCardScrollAdapter = new ArticleCardScrollAdapter(this);		
		mCardScrollAdapter.clear();

		mCardScrollView = new CardScrollView(this);
		mCardScrollView.setAdapter(mCardScrollAdapter);
		mCardScrollView.activate();
		mCardScrollView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				// TODO Auto-generated method stub
				Log.d(TAG, "Displaying menu for "+view.getTag());
				mMenuArticle = (Article) view.getTag();
				openOptionsMenu();
			}
		});
		
		setContentView(mCardScrollView);
	}

	@Override
	protected void onDestroy() {
		unbindService(mConnection);
		super.onDestroy();
	}
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.feed_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection.
        switch (item.getItemId()) {
            case R.id.action_open_article:
            	startActivity(new Intent(Intent.ACTION_VIEW, mMenuArticle.uLink));
                return true;
            case R.id.action_open_feed:
            	startActivity(new Intent(Intent.ACTION_VIEW, mFeedUri));
                return true;
            case R.id.action_mark_read:
                return true;
            default:
            	Log.i(TAG, "Unknown menu ID "+item.getItemId());
            	return false;
        }
    }
    
	private void _updateFeed() {
		List<Article> articles = mService.getFeed(mFeedId);

		mCardScrollAdapter.clear();
		for(Article a: articles) {
			mCardScrollAdapter.add(a);
		}
	
		mCardScrollView.post(new Runnable() {
	        public void run() {
	    		mCardScrollView.updateViews(true);
	        }
	    });
	}
}
