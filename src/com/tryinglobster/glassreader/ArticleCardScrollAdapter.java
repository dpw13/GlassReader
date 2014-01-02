package com.tryinglobster.glassreader;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.net.Uri;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;

import com.android.theoldreader.Article;
import com.google.android.glass.app.Card;
import com.google.android.glass.widget.CardScrollAdapter;

public class ArticleCardScrollAdapter extends CardScrollAdapter {
	private List<Article> mCards = new ArrayList<Article>();
	private Context mContext;
	
	public ArticleCardScrollAdapter(Context c) {
		mContext = c;
	}
	
	public void clear() {
		mCards.clear();
	}
	
	public void add(Article a) {
		mCards.add(a);
	}
	
	@Override
	public int findIdPosition(Object arg0) {
		return -1;
	}

	@Override
	public int findItemPosition(Object arg0) {
		return mCards.indexOf(arg0);
	}

	@Override
	public int getCount() {
		return Math.max(1, mCards.size());
	}

	@Override
	public Object getItem(int arg0) {
		return mCards.get(arg0);
	}

	@Override
	public View getView(int arg0, View arg1, ViewGroup arg2) {
		Card c = new Card(mContext);
		View v;
		if(mCards.isEmpty()) {
			Card c1 = new Card(mContext);
			c1.setText("Loading feeds...");			
			v = c.toView();
		} else {
			Article a = mCards.get(arg0);
			c.setText(
					Html.fromHtml(a.sTitle).toString() + 
					"\n" +
					Html.fromHtml(a.sSummary).toString());
			c.setFootnote(a.uLink.toString());
			v = c.toView();
			v.setTag(a);
		}
		return v;
	}

}
