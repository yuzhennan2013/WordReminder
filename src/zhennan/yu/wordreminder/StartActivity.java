package zhennan.yu.wordreminder;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import zhennan.yu.wordreminder.DBManager.OnAddWordListener;
import zhennan.yu.wordreminder.ExternalDBObserver.ExternalDBStateListener;
import android.R.integer;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.TextView;
import cn.sina.youxi.util.CV_Log;
import cn.sina.youxi.util.CharHelper;

public class StartActivity extends Activity implements OnScrollListener, OnItemLongClickListener, OnItemClickListener {

	String TAG = "StartActivity";
	// when list view stops scroll, it keeps the current position of listview
	// for recovery;
	private int listviewPosition = 0;
	// thread pool that query data for the main page
	private ExecutorService mThreadPool = null;

	/**
	 * @author Administrator runnable that query data from DB
	 */
	class QueryRunnable implements Runnable {

		char initialchar;
		String category;
		ArrayList<Short> groups;
		WordGroupContainer wordGroupContainer;

		public QueryRunnable(char initial_char, ArrayList<Short> groups, WordGroupContainer wordGroupContainer) {
			// convert to uppercase
			this.initialchar = CharHelper.toUppercase(initial_char);
			this.groups = groups;
			this.wordGroupContainer = wordGroupContainer;
		}

		@Override
		public void run() {
			if (groups == null || groups.size() == 0) {
				wordGroupContainer.deleteBranch(initialchar);
			} else {
				for (int i = 0; i < groups.size(); i++) {
					WordGroup wordGroup = new WordGroup(groups.get(i), initialchar);
					short cnt = dbManager.queryUntestedCntByInitialCharacter(initialchar, null, groups.get(i));
					CV_Log.i(String.valueOf((char) initialchar) + groups.get(i) + ":untested:" + cnt);
					if (cnt != 0) {
						wordGroup.addUntested(buildIndexItem(Config.CATEGORY_UNTESTED, i, cnt));
					}

					cnt = dbManager.queryForgetCntByInitialCharacter(initialchar, null, groups.get(i));
					CV_Log.i(String.valueOf((char) initialchar) + groups.get(i) + ":forgotten:" + cnt);
					if (cnt != 0) {
						wordGroup.addForgotten(buildIndexItem(Config.CATEGORY_FORGOTTEN, i, cnt));
					}

					cnt = dbManager.queryRememberCntByInitialCharacter(initialchar, null, groups.get(i));
					CV_Log.i(String.valueOf((char) initialchar) + groups.get(i) + ":remember:" + cnt);
					if (cnt != 0) {
						wordGroup.addRemembered(buildIndexItem(Config.CATEGORY_REMEMBERED, i, cnt));
					}

					try {
						if (wordGroup.isEmpty()) {
							// this group should be deleted
							wordGroupContainer.deleteGroup(wordGroup);
						} else {
							short index = wordGroupContainer.indexOf(wordGroup);
							if (index != -1) {
								wordGroupContainer.updateGroup(wordGroup, index);
							} else {
								wordGroupContainer.addGroup(wordGroup);
							}
						}
					} catch (Exception e) {
						// TODO: handle exception
						e.printStackTrace();
					}
				}
			}
			mCountDownLatch.countDown();
		}

		private IndexItem buildIndexItem(String category, int i, short cnt) {
			IndexItem indexItem = new IndexItem();
			indexItem.initialchar = this.initialchar;
			indexItem.category = category;
			indexItem.count = cnt;
			indexItem.groupid = groups.get(i);
			return indexItem;
		}

	}

	// because mainpage uses 26 seperate thread
	// to load data, mCountDownLatch is used as a barrier
	// when these threads is not finished
	// single thread costs 3850ms to refresh mainpage
	// while 26 threads cost 1387ms
	CountDownLatch mCountDownLatch;

	// void refreshAsync(){
	// new Thread(){
	//
	// @Override
	// public void run() {
	// refresh();
	// }
	//
	// }.start();
	// }
	WordGroupContainer wordGroupContainer;

	@SuppressLint("DefaultLocale")
	synchronized void refresh(RefreshContentFilter refreshContentFilter) {
		try {
			long refreshStart = SystemClock.uptimeMillis();

			if (wordGroupContainer == null) {
				wordGroupContainer = new WordGroupContainer();
			}

			mCountDownLatch = new CountDownLatch(refreshContentFilter.size());

			Iterator<Content> iterator = refreshContentFilter.iterator();

			while (iterator.hasNext()) {
				ArrayList<Short> groups;
				Content content = iterator.next();
				if (content.hasGroup()) {
					// this may leads to a little more threads running at the
					// same time
					groups = new ArrayList<Short>(1);
					groups.add(content.getGroupid());
				} else {
					// first query group set for 'a'
					groups = dbManager.queryGroupsByInitialCharacter((char) content.initialChar);
					if (groups == null) {
						mCountDownLatch.countDown();
						continue;
					}
				}
				mThreadPool.submit(new QueryRunnable((char) content.initialChar, groups, wordGroupContainer));
			}

			// must wait until all QueryRunnable are finished
			mCountDownLatch.await();
			CV_Log.i("count down latch released !");
			
			if (arrayList == null) {
				arrayList = new ArrayList<IndexItem>(20 * 3 * RefreshContentIterator.CHARS.length);
			} else {
				arrayList.clear();
			}

			wordGroupContainer.traverse(arrayList);

			if (arrayList.size() > 0) {
				IndexItemRandom indexItemRandom = new IndexItemRandom();
				indexItemRandom.category = Config.CATEGORY_RANDOM;
				indexItemRandom.count = 100;
				arrayList.add(indexItemRandom);
			}

			handler.sendEmptyMessage(REFRESHSTARTPAGE);
			CV_Log.i("refresh mainpage finished, cost " + (SystemClock.uptimeMillis() - refreshStart) + "ms !");

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// synchronizing from xml to DB finished
	// static final int SYNCFINISHED = 1;
	// call this to refresh the start page
	static final int REFRESHSTARTPAGE = 2;
	// show on the title how many words have been synced
	static final int SHOWSYNCCOUNT = 3;

	Handler handler = new Handler(new Handler.Callback() {
		@Override
		public boolean handleMessage(Message msg) {
			// TODO Auto-generated method stub

			switch (msg.what) {
			// case SYNCFINISHED:
			// // synchronizing from xml to DB finished
			// dbManager.deleteRemoved();
			// refresh();
			// setTitle("synchronized " + synchronizedCnt);
			// break;
			case REFRESHSTARTPAGE:
				if (arrayList.size() == 0) {
					textView.setVisibility(View.VISIBLE);
				} else {
					if (startAdapter == null) {
						startAdapter = new StartAdapter(StartActivity.this, arrayList);
						mListView.setAdapter(startAdapter);
					}
					startAdapter.notifyDataSetChanged();
					textView.setVisibility(View.GONE);
				}
				mListView.setSelection(listviewPosition);
				// dismissDialog(1);
				break;
			case SHOWSYNCCOUNT:
				StartActivity.this.setTitle("synchronizing " + msg.arg1 + "th");
				break;
			}

			return false;
		}
	});

	void showDialog(String path) {
		// DialogFragment.show() will take care of adding the fragment
		// in a transaction. We also want to remove any currently showing
		// dialog, so make our own transaction and take care of that here.
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		Fragment prev = getFragmentManager().findFragmentByTag("dialog");
		if (prev != null) {
			ft.remove(prev);
		}
		ft.addToBackStack(null);

		// Create and show the dialog.
		DialogFragment newFragment = MyDialogFragment.newInstance(path);
		newFragment.show(ft, "dialog");
	}

	/**
	 * @author Administrator callback that handle each word
	 */
	public interface XMLParsorCallBack {
		void doit(Word word);

		void onComplete();
	}

	// void synchronizeDataIntoDB() {
	// synchronizedCnt = 0;
	// try {
	// File file = new File(Config.SDWORDXMLPATH);
	// if (!file.exists()) {
	// Toast.makeText(this, "can't find word.xml on path " +
	// file.getAbsolutePath(), Toast.LENGTH_LONG).show();
	// return;
	// }
	// dbManager.setAllRemoved();
	// InputStream is = new FileInputStream(file.getPath());
	// StackOverflowXmlParser parser = new StackOverflowXmlParser();
	// parser.parse(is, new XMLParsorCallBack() {
	//
	// @Override
	// public void doit(Word word) {
	// // TODO Auto-generated method stub
	// if (dbManager.wordExists(word.word)) {
	// dbManager.setRemoved(word.word, "0");
	// } else {
	// // there is no create time node in external xml
	// // so set it with system time
	// word.created_time = System.currentTimeMillis();
	// dbManager.add(word);
	// }
	// synchronizedCnt++;
	// if (synchronizedCnt % 10 == 0) {
	// Message msg = Message.obtain();
	// msg.what = SHOWSYNCCOUNT;
	// msg.arg1 = synchronizedCnt;
	// handler.sendMessage(msg);
	// }
	// }
	//
	// @Override
	// public void onComplete() {
	// // TODO Auto-generated method stub
	// handler.sendEmptyMessage(SYNCFINISHED);
	// }
	// });
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// }

	boolean destroyed;

	@Override
	protected void onDestroy() {
		destroyed = true;
		wordGroupContainer.destroy();
		ExternalDBObserver.unregisterExternalDBObserver();
		WordReminderFloatView.getFloatView(this).destroy();
		dbManager.close();
		mThreadPool.shutdownNow();
		super.onDestroy();
	}

	DBManager dbManager;
	ListView mListView;
	StartAdapter startAdapter;
	ArrayList<IndexItem> arrayList;
	TextView textView;
	// how many words have been synced
	volatile int synchronizedCnt = 0;

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		if (data != null && data.getExtras().getBoolean(Config.NEED_EXPORTDB)) {
			DBStorageManager.exportDB(StartActivity.this);
		}
		
		if (data != null && data.getExtras().getBoolean(Config.NEED_REFRESH)) {
			RefreshContentFilter refreshContentFilter = (RefreshContentFilter) data.getSerializableExtra(Config.REFRESH_CONTENT);
			refresh(refreshContentFilter);
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		CV_Log.init(this);
		setContentView(R.layout.activity_main);

		mThreadPool = Executors.newCachedThreadPool();

		WordReminderFloatView.getFloatView(this).show();

//		SharedPreferences sharedPreferences = getSharedPreferences("path", Context.MODE_PRIVATE);
//		Config.SDWORDXMLPATH = sharedPreferences.getString("SDWORDXMLPATH", Environment.getExternalStorageDirectory().getAbsolutePath() + "/word.xml");
//		Config.BACKUPDBPATH = sharedPreferences.getString("BACKUPDBPATH", Environment.getExternalStorageDirectory() + "/" + DBHelper.DATABASE_NAME);

		dbManager = DBManager.getInstance(this);
		if (dbManager.getTotalCnt() == 0) {
			// database is empty, use saved wordreminder database if there is
			// any
			dbManager.close();
			dbManager = null;
			DBStorageManager.importDB(this);
			dbManager = DBManager.getInstance(this);
		}
		textView = (TextView) findViewById(R.id.no_word_textview);
		mListView = (ListView) findViewById(R.id.dead_word_list);
		mListView.setOnItemLongClickListener(this);
		mListView.setOnItemClickListener(this);
		mListView.setOnScrollListener(this);

		refresh(new RefreshContentFilter(true));

//		 dbManager.deleteWord("a", DBManager.DELETE_MODE_PHYSICAL);
		ExternalDBObserver.registerExternalDBObserver(this, new ExternalDBStateListener() {

			@Override
			public void onChange() {
				DBStorageManager.markSyncBegan(StartActivity.this);
				RefreshContentFilter refreshContentFilter = sync();
				if (refreshContentFilter != null) {
					refresh(refreshContentFilter);
				}
				DBStorageManager.exportDB(StartActivity.this);
				DBStorageManager.markSyncFinished(StartActivity.this);
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// menu.add(Menu.NONE, Menu.FIRST + 1, 1, "SYNCHRONIZE YOUR WORD");
		// menu.add(Menu.NONE, Menu.FIRST + 2, 2, "SAVE YOUR DB");
		// menu.add(Menu.NONE, Menu.FIRST + 3, 3, "RECOVER YOUR DB");
		// menu.add(Menu.NONE, Menu.FIRST + 4, 4, "RECOVER YOUR DB FROM");
		// menu.add(Menu.NONE, Menu.FIRST + 5, 5, "SYNCHRONIZE YOUR WORD FROM");
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		// case Menu.FIRST + 1:
		// // todo
		// synchronizeDataIntoDB();
		// break;
		// case Menu.FIRST + 2:
		// DBStorageManager.exportDBIntoSD(this);
		// break;
		// case Menu.FIRST + 3:
		// DBStorageManager.importDBIntoReminder(this);
		// reloadPage();
		// break;
		// case Menu.FIRST + 4:
		// // RECOVER YOUR DB FROM
		// showDialog("1");
		// break;
		// case Menu.FIRST + 5:
		// // SYNCHRONIZE YOUR WORD FROM
		// showDialog("2");
		// break;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * sync from third party db
	 */
	private RefreshContentFilter sync() {

		File externalData = Environment.getExternalStorageDirectory();
		File tempDB = new File(externalData, Config.TEMPDBNAME);
		Cursor cursor = null;
		final RefreshContentFilter refreshContentFilter = new RefreshContentFilter(false);

		try {
			long start = SystemClock.uptimeMillis();
			cursor = TempDBManager.getInstance(tempDB).queryWordAfter(dbManager.getMaxCreatedTime());
			if (cursor.getCount() == 0) {
				throw new Exception("no word to sync, queryWordAfter(MaxCreatedTime) = 0");
			}
			long end = SystemClock.uptimeMillis();
			long querytime = end - start;

			start = SystemClock.uptimeMillis();

			int cnt = dbManager.add(cursor, new OnAddWordListener() {

				@Override
				public void onAddWord(Word word) {
					refreshContentFilter.addContent(new Content(word.word.charAt(0), word.groupid));
				}
			});

			if (cnt == 0) {
				throw new Exception("exception occurs while adding words");
			}

			end = SystemClock.uptimeMillis();
			long updatetime = end - start;

			CV_Log.i_debug("query time is " + querytime);
			CV_Log.i_debug("updatetime time is " + updatetime);
		} catch (Exception e) {
			e.printStackTrace();
			CV_Log.i("sync exception occurs!");
			return null;
		} finally {
			if (cursor != null) {
				cursor.close();
			}
			// synchronize from temp DB is over, close it
			TempDBManager.getInstance().close();
			CV_Log.i("sync finished!");
		}
		return refreshContentFilter;
	}

	/**
	 * just retrieve data from DB again
	 */
	// private void reloadPage() {
	// new Thread() {
	// @Override
	// public void run() {
	// refresh();
	// }
	// }.start();
	// }

	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		if (arrayList.get(arg2).category.contains(Config.CATEGORY_RANDOM)) {
			return false;
		}
		Intent intent = new Intent();
		Bundle bundle = new Bundle();
		bundle.putSerializable("item", arrayList.get(arg2));
		intent.putExtras(bundle);
		intent.setClass(StartActivity.this, BrowseActivity.class);
		StartActivity.this.startActivityForResult(intent, 1);
		return false;
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		Intent intent = new Intent();
		Bundle bundle = new Bundle();
		bundle.putSerializable("item", arrayList.get(arg2));
		intent.putExtras(bundle);
		intent.setClass(StartActivity.this, TestingActivity.class);
		StartActivity.this.startActivityForResult(intent, 1);
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
			listviewPosition = mListView.getFirstVisiblePosition();
		}
	}
}
