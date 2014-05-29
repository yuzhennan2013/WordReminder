package zhennan.yu.wordreminder;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;

public class BrowseActivity extends Activity implements 
		OnTouchListener, OnScrollListener,OnItemLongClickListener,OnItemClickListener {

	ArrayList<Word> arrayList;
	ListView listView;
	FrameLayout frameLayout;
	Button mMoveButton;
	int contentViewTop;
	int buttonHeight;
	int maxMoveBtnTopMargin;
	float fractionHeight;
	FrameLayout.LayoutParams layoutParams;
	BrowseAdapter browseAdapter;
	ClipboardManager cmb;
	ArrayList<Integer> expandedPositions;
	IndexItem mIndexItem;
	RefreshContentFilter refreshContentFilter;
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Intent resultIntent = new Intent();
			resultIntent.putExtra(Config.NEED_REFRESH, browseAdapter.startpageNeedRefresh);
			resultIntent.putExtra(Config.REFRESH_CONTENT, refreshContentFilter);
			resultIntent.putExtra(Config.NEED_EXPORTDB, false);
			setResult(RESULT_OK, resultIntent);
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}
	
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.browseactivity);
		expandedPositions = new ArrayList<Integer>();
		mIndexItem = (IndexItem) getIntent().getSerializableExtra("item");
		
		refreshContentFilter = new RefreshContentFilter(false);
		refreshContentFilter.addContent(new Content(mIndexItem.initialchar, mIndexItem.groupid));
		
		cmb = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
		setTitle((char)mIndexItem.initialchar + "" + mIndexItem.groupid + " " + mIndexItem.category);
		frameLayout = (FrameLayout) findViewById(R.id.browse_root_view);
		frameLayout.post(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				Rect rect = new Rect();
				frameLayout.getWindowVisibleDisplayFrame(rect);
				Window window = getWindow();
				contentViewTop = window.findViewById(Window.ID_ANDROID_CONTENT)
						.getTop() + rect.top;
				Display display = getWindowManager().getDefaultDisplay();
				Point size = new Point();
				display.getSize(size);
				buttonHeight = mMoveButton.getHeight();
				maxMoveBtnTopMargin = size.y - contentViewTop - buttonHeight
						+ rect.top;
				fractionHeight = ((float) maxMoveBtnTopMargin)
						/ arrayList.size();
			}
		});

		listView = (ListView) findViewById(R.id.word_list);
		mMoveButton = (Button) findViewById(R.id.move_btn);
		mMoveButton.setOnTouchListener(this);
		layoutParams = (FrameLayout.LayoutParams) mMoveButton.getLayoutParams();
		getBrowseDatasource("difficulty DESC");
		browseAdapter = new BrowseAdapter(this, arrayList, mIndexItem);
		listView.setOnScrollListener(this);
		listView.setOnItemClickListener(this);
		listView.setOnItemLongClickListener(this);
		listView.setAdapter(browseAdapter);
		listView.setBackgroundColor(Color.rgb(0, 0, 0));
		ColorDrawable sage = new ColorDrawable(this.getResources().getColor(
				R.drawable.grey));
		listView.setDivider(sage);
		listView.setDividerHeight(2);
	}

	private void getBrowseDatasource(String orderByStr) {
		if (arrayList == null) {
			arrayList = new ArrayList<Word>(Config.MAXCOUNTEACHTEST);
		} else {
			arrayList.clear();
		}
		Cursor cursor = null;
		if (mIndexItem.category.equals(Config.CATEGORY_REMEMBERED)) {
			mMoveButton.setText(alphabeticOrder);
			cursor = DBManager.getInstance(this)
					.queryRememberByInitialCharacter(mIndexItem.initialchar, "word ASC", mIndexItem.groupid);
		} else if (mIndexItem.category.equals(Config.CATEGORY_UNTESTED)) {
			mMoveButton.setText(alphabeticOrder);
			cursor = DBManager.getInstance(this)
					.queryUntestedByInitialCharacter(mIndexItem.initialchar, "word ASC", mIndexItem.groupid);
		} else {
			cursor = DBManager.getInstance(this).queryForgetByInitialCharacter(
					mIndexItem.initialchar, orderByStr, mIndexItem.groupid);
		}
		prepareDatasource(cursor);
	}

	private void prepareDatasource(Cursor cursor) {
		
		while(cursor.moveToNext()){
			arrayList.add(new Word(cursor.getString(cursor
					.getColumnIndex("word")), cursor.getString(cursor
					.getColumnIndex("meaning")), cursor.getShort(cursor
					.getColumnIndex("difficulty"))));
		}
		if (cursor != null) {
			cursor.close();
		}
	}

	int lastPosition = -1, nowPosition = 0;
	boolean handsOff = true;
	long pressTime;

	static final String alphabeticOrder = "A ↑";
	static final String difficultyOrder = "D ↓";
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		final int Y = (int) event.getRawY();

		switch (event.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN:
			pressTime = System.currentTimeMillis();
			break;
		case MotionEvent.ACTION_UP:
			handsOff = true;
			if (mIndexItem.category.equals(Config.CATEGORY_REMEMBERED) || mIndexItem.category.equals(Config.CATEGORY_UNTESTED)) {
				return false;
			}
			if (System.currentTimeMillis() - pressTime < 150) {
				if (mMoveButton.getText().equals(difficultyOrder)) {
					getBrowseDatasource("word ASC");
					browseAdapter.notifyDataSetChanged();
					mMoveButton.setText(alphabeticOrder);
				} else {
					getBrowseDatasource("difficulty DESC");
					browseAdapter.notifyDataSetChanged();
					mMoveButton.setText(difficultyOrder);
				}
			}
			break;
		case MotionEvent.ACTION_POINTER_DOWN:
			break;
		case MotionEvent.ACTION_POINTER_UP:
			break;
		case MotionEvent.ACTION_MOVE:
			handsOff = false;
			layoutParams.topMargin = Y - contentViewTop;
			if (layoutParams.topMargin < 0) {
				layoutParams.topMargin = 0;
			} else if (layoutParams.topMargin > maxMoveBtnTopMargin) {
				layoutParams.topMargin = maxMoveBtnTopMargin;
			}
			nowPosition = (int) (layoutParams.topMargin / fractionHeight);
			if (nowPosition > arrayList.size()) {
				nowPosition = arrayList.size();
			}
			if (nowPosition != lastPosition) {
				lastPosition = nowPosition;
				listView.setSelection(nowPosition);
			}
			v.setLayoutParams(layoutParams);
			break;
		}
		mMoveButton.invalidate();
		return false;
	}

	Handler handler = new Handler(){
		public void handleMessage(Message msg) {
			for (int i = 0; i < expandedPositions.size(); i++) {
					browseAdapter.datasource.get(expandedPositions.get(i)).expanded = false;	
			}
			expandedPositions.clear();
			browseAdapter.notifyDataSetChanged();
		}
	};
	
	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		if (handsOff) {
			if (firstVisibleItem + visibleItemCount == totalItemCount) {
				layoutParams.topMargin = maxMoveBtnTopMargin;
			} else {
				float a = firstVisibleItem;
				layoutParams.topMargin = (int) (a * fractionHeight);
			}
			mMoveButton.setLayoutParams(layoutParams);
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		// TODO Auto-generated method stub
		if (scrollState == SCROLL_STATE_TOUCH_SCROLL) {
			if (expandedPositions.size() > 0) {
				handler.sendEmptyMessage(1);
			}
		}
	}

	@SuppressLint("NewApi")
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		cmb.setPrimaryClip(ClipData.newPlainText("word", browseAdapter.datasource.get(arg2).word));
	}
	
	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		if (browseAdapter.datasource.get(arg2).expanded) {
			browseAdapter.datasource.get(arg2).expanded = false;
			browseAdapter.notifyDataSetChanged();
		}
		else {
			browseAdapter.datasource.get(arg2).expanded = true;
			for (int i = 0; i < expandedPositions.size(); i++) {
				if (arg2 != expandedPositions.get(i)) {
					browseAdapter.datasource.get(expandedPositions.get(i)).expanded = false;					
				}
			}
			expandedPositions.clear();
			expandedPositions.add(arg2);
			browseAdapter.notifyDataSetChanged();
		}
		return true;
	}
}
