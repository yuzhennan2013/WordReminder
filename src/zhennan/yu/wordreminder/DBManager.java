package zhennan.yu.wordreminder;

import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;
import cn.sina.youxi.util.CV_Log;

/**
 * @author Administrator all CRUD operation in this class
 */
public class DBManager {
	static String TAG = "DBManager";
	private DBHelper helper;
	private SQLiteDatabase db;
	static DBManager dbManager;
	// thread pool that carry out DB operation
	private static ExecutorService mThreadPool = null;
	Context context;

	synchronized public static DBManager getInstance(Context context) {
		if (dbManager == null) {
			dbManager = new DBManager(context);
			mThreadPool = Executors.newCachedThreadPool();
			dbManager.context = context;
		}
		return dbManager;
	}

	private DBManager(Context context) {
		helper = new DBHelper(context);
		db = helper.getWritableDatabase();
		db.enableWriteAheadLogging();
	}

	private void close(Cursor c) {
		if (c != null) {
			c.close();
		}
	}

	public int getTotalCnt() {
		Cursor c = db.query(DBHelper.TABLE_WORD, new String[] { "COUNT(1)" }, null, null, null, null, null, null);
		int totalcnt = 0;
		if (c.moveToFirst()) {
			totalcnt = c.getInt(c.getColumnIndex("COUNT(1)"));
		}
		close(c);
		return totalcnt;
	}

	public long getMaxCreatedTime() {
		Cursor c = db.query(DBHelper.TABLE_WORD, new String[] { "MAX(created_time)" }, null, null, null, null, null, null);
		long maxCreatedTime = 0;
		if (c.moveToFirst()) {
			maxCreatedTime = c.getLong(c.getColumnIndex("MAX(created_time)"));
		}
		close(c);
		return maxCreatedTime;
	}

	public boolean wordExists(String word) {
		Cursor c = null;
		try {
			// TODO: handle exception
			c = db.query(DBHelper.TABLE_WORD, new String[] { "word" }, "word = ?", new String[] { word }, null, null, null);
			return c.getCount() > 0;
		} finally {
			if (c != null) {
				c.close();
			}
		}
	}

	/**
	 * 1 : this word will be removed from DB later 0 : this word will be keeped
	 */
	public void setAllRemoved() {
		db.beginTransactionNonExclusive(); // ��ʼ����
		try {
			ContentValues cv = new ContentValues();
			cv.put("removed", "1");
			db.update(DBHelper.TABLE_WORD, cv, null, null);
			db.setTransactionSuccessful(); // ��������ɹ����
		} finally {
			db.endTransaction(); // ��������
		}
	}

	/**
	 * 1 : this word will be removed from DB later, 0 : this word will be keeped
	 */
	public static final String WORD_KEPT = "0";
	public static final String WORD_REMOVED = "1";

	public void setRemoved(String word, String removed) {
		ContentValues cv = new ContentValues();
		cv.put("removed", removed);
		db.update(DBHelper.TABLE_WORD, cv, "word = ?", new String[] { word });
	}

	public void updateCreatedTime(String word, long created_time) {
		ContentValues cv = new ContentValues();
		cv.put("created_time", created_time);
		db.update(DBHelper.TABLE_WORD, cv, "word = ?", new String[] { word });
	}

	/**
	 * 1 : this word will be removed from DB later 0 : this word will be keeped
	 */
	public void deleteRemoved() {
		db.delete(DBHelper.TABLE_WORD, "removed = ?", new String[] { String.valueOf("1") });
	}

	public interface OnAddWordListener{
		public void onAddWord(Word word);
	}
	
	/**
	 * when you add huge amount data to DB like 6078, without transaction,
	 * 217300 ms use transaction, 48742 ms
	 * return how many words has been added
	 * if exception occurs whild add(word), return 0;
	 * and db roll back
	 * @param cursor
	 */
	public int add(Cursor cursor, OnAddWordListener onAddWordListener) {
		if (cursor == null) {
			return 0;
		}
		db.beginTransaction();
		int cnt = 0;
		try {
			Word word = new Word();
			while (cursor.moveToNext()) {
				word.word = cursor.getString(cursor.getColumnIndex("word"));
				word.meaning = cursor.getString(cursor.getColumnIndex("detail"));
				word.created_time = cursor.getLong(cursor.getColumnIndex("created"));
				if (!wordExists(word.word)) {
					add(word);
					onAddWordListener.onAddWord(word);
					CV_Log.i(cnt++ + " : " + word.word + " create time: ?  from temp db is added to local db !", word.created_time);
				}
			}
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
		if (cursor.getCount() == cnt) {
			return cnt;
		}
		return 0;
	}

	/**
	 * this method must be executed synchonizingly otherwise wrong wordgroup id
	 * allocation may happen
	 * 
	 * @param persons
	 */
	public void add(Word word) {
		String word1 = word.word.trim();
		char initialchar = word1.toUpperCase(Locale.ENGLISH).charAt(0);
		Cursor c = db.query(DBHelper.TABLE_AVAILABLEGROUP, new String[] { "availablegroups" }, "initialchar = ? ", new String[] { String.valueOf(initialchar) }, null, null, null);

		String availablegroups = null;
		String availablegroup = null;
		try {
			if (c.moveToFirst()) {
				availablegroups = c.getString(c.getColumnIndex("availablegroups"));
				availablegroup = availablegroups.substring(0, 1);
			} else {
				CV_Log.i("fail to add " + word1 + " because can't allot group to this word !");
				return;
			}
		} finally {
			close(c);
		}
		
		word.groupid = Short.valueOf(availablegroup);
		db.execSQL("INSERT INTO words VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)", new Object[] { word1, word.meaning, -1, 0, word.created_time, 0, 0, 0, availablegroup });

		c = db.query(DBHelper.TABLE_WORD, new String[] { "COUNT(1)" }, "word like ? AND wordgroup = ? AND removed = ?", new String[] { initialchar + "%", availablegroup, "0" }, null, null, null);

		short cnt = 0;
		if (c.moveToFirst()) {
			cnt = c.getShort(c.getColumnIndex("COUNT(1)"));
		}
		close(c);

		if (cnt == Config.MAXCOUNTEACHTEST) {
			// this wordgroup is full, choose another wordgroup and update
			// TABLE_AVAILABLEGROUP
			if (availablegroups.length() == 1) {
				c = db.query(DBHelper.TABLE_WORD, new String[] { "MAX(wordgroup)" }, "word like ? AND removed = ?", new String[] { initialchar + "%", "0" }, null, null, null);
				short maxGroupId = 0;
				if (c.moveToFirst()) {
					maxGroupId = c.getShort(c.getColumnIndex("MAX(wordgroup)"));
				}
				availablegroups = String.valueOf(maxGroupId + 1);
				close(c);
			} else {
				availablegroups = availablegroups.replace(availablegroup, "");
			}
			ContentValues cv = new ContentValues();
			cv.put("availablegroups", availablegroups);
			db.update(DBHelper.TABLE_AVAILABLEGROUP, cv, "initialchar = ?", new String[] { String.valueOf(initialchar) });
		}
	}

	/**
	 * delete old person mode if mode is 0, physical delete, 1, logical
	 * delete(removed is set to 1)
	 * 
	 * @param person
	 */
	public static final int DELETE_MODE_PHYSICAL = 0;
	public static final int DELETE_MODE_LOGICAL = 1;

	public void deleteWord(String word_str, int mode) {
		Word word = new Word();
		word.word = word_str;
		deleteWord(word, mode);
	}
	
	public void deleteWord(Word word, int mode) {

		db.beginTransaction();
		try {
			String word1 = word.word.trim();
			char initialchar = word1.toUpperCase(Locale.ENGLISH).charAt(0);

			// get deleted word's wordgroup id, this id may be next available
			// wordgroup id
			Cursor c = db.query(DBHelper.TABLE_WORD, new String[] { "wordgroup" }, "word = ? ", new String[] { word1 }, null, null, null);
			char deletedwordgroup = '0';
			try {
				if (c.moveToFirst()) {
					deletedwordgroup = c.getString(c.getColumnIndex("wordgroup")).charAt(0);	
				}
				else {
					CV_Log.i("fail to get the group of deleted word " + word1);
					return;
				}
			} finally {
				close(c);
			}

			if (mode == DELETE_MODE_PHYSICAL) {
				db.delete(DBHelper.TABLE_WORD, "word = ?", new String[] { word1 });
			} else {
				setRemoved(word1, WORD_REMOVED);
			}

			c = db.query(DBHelper.TABLE_AVAILABLEGROUP, new String[] { "availablegroups" }, "initialchar = ? ", new String[] { String.valueOf(initialchar) }, null, null, null);
			String availablegroups = null;
			try {
				if (c.moveToFirst()) {
					availablegroups = c.getString(c.getColumnIndex("availablegroups"));	
				}
				else {
					CV_Log.i("fail to get the available groups of initialchar " + initialchar);
					return;
				}
			} finally {
				close(c);
			}

			char[] availablegroupsCharArr = availablegroups.toCharArray();
			StringBuilder sBuilder = new StringBuilder();
			boolean hit = false;
			for (int i = 0; i < availablegroupsCharArr.length; i++) {
				if (hit) {
					sBuilder.append(availablegroupsCharArr[i]);
					continue;
				}
				if (availablegroupsCharArr[i] == deletedwordgroup) {
					hit = true;
				} else if (availablegroupsCharArr[i] > deletedwordgroup) {
					sBuilder.append(deletedwordgroup);
					sBuilder.append(availablegroupsCharArr[i]);
					hit = true;
				} else {
					sBuilder.append(availablegroupsCharArr[i]);
				}
			}
			if (!hit) {
				sBuilder.append(deletedwordgroup);
			}

			availablegroups = sBuilder.toString();

			ContentValues cv = new ContentValues();
			cv.put("availablegroups", availablegroups);
			db.update(DBHelper.TABLE_AVAILABLEGROUP, cv, "initialchar = ?", new String[] { String.valueOf(initialchar) });

			db.setTransactionSuccessful();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.endTransaction();
		}
	}

	public Cursor queryRandomly() {
		Cursor c = db.query(DBHelper.TABLE_WORD, new String[] { "word", "meaning", "difficulty" }, "difficulty > ? and removed = ?", new String[] { "0", "0" }, null, null, null);
		return c;
	}

	/**
	 * return wordgroup set of this initialCharacter returned ArrayList maybe
	 * null
	 * 
	 * @param initialCharacter
	 * @return
	 */
	public ArrayList<Short> queryGroupsByInitialCharacter(char initialCharacter) {

		ArrayList<Short> groups = null;
		Cursor c = db.query(DBHelper.TABLE_WORD, new String[] { "wordgroup" }, "word like ? AND removed = ?", new String[] { initialCharacter + "%", "0" }, "wordgroup", null, null);
		while (c.moveToNext()) {
			if (groups == null) {
				groups = new ArrayList<Short>(c.getCount());
			}
			groups.add(c.getShort(c.getColumnIndex("wordgroup")));
		}
		close(c);
		return groups;
	}

	private Cursor queryByInitialCharacter(char initialCharacter, String orderByStr, short group, String difficulty_where, String difficulty_value, String[] selection) {
		Cursor c = db.query(DBHelper.TABLE_WORD, selection, "word like ? and " + difficulty_where + " and removed = ? and wordgroup = ?", new String[] { String.valueOf(initialCharacter) + "%", difficulty_value, "0", String.valueOf(group) }, null, null, orderByStr);
		return c;
	}

	public Cursor queryUntestedByInitialCharacter(char initialCharacter, String orderByStr, short group) {
		return queryByInitialCharacter(initialCharacter, orderByStr, group, "difficulty = ?", "-1", new String[] { "word", "meaning", "difficulty" });
	}

	public short queryUntestedCntByInitialCharacter(char initialCharacter, String orderByStr, short group) {
		Cursor c = null;
		try {
			c = queryByInitialCharacter(initialCharacter, orderByStr, group, "difficulty = ?", "-1", new String[] { "COUNT(1)" });
			if (c.moveToFirst()) {
				return c.getShort(c.getColumnIndex("COUNT(1)"));	
			}
			return 0;
		} finally {
			close(c);
		}
	}

	public Cursor queryForgetByInitialCharacter(char initialCharacter, String orderByStr, short group) {
		return queryByInitialCharacter(initialCharacter, orderByStr, group, "difficulty > ?", "0", new String[] { "word", "meaning", "difficulty" });
	}

	public short queryForgetCntByInitialCharacter(char initialCharacter, String orderByStr, short group) {
		Cursor c = null;
		try {
			c = queryByInitialCharacter(initialCharacter, orderByStr, group, "difficulty > ?", "0", new String[] { "COUNT(1)" });
			if (c.moveToFirst()) {
				return c.getShort(c.getColumnIndex("COUNT(1)"));	
			}
			return 0;
		} finally {
			close(c);
		}
	}

	public Cursor queryRememberByInitialCharacter(char initialCharacter, String orderByStr, short group) {
		return queryByInitialCharacter(initialCharacter, orderByStr, group, "difficulty = ?", "0", new String[] { "word", "meaning", "difficulty" });
	}

	public short queryRememberCntByInitialCharacter(char initialCharacter, String orderByStr, short group) {
		Cursor c = null;
		try {
			c = queryByInitialCharacter(initialCharacter, orderByStr, group, "difficulty = ?", "0", new String[] { "COUNT(1)" });
			if (c.moveToFirst()) {
				return c.getShort(c.getColumnIndex("COUNT(1)"));	
			}
			return 0;
		} finally {
			close(c);
		}
	}

	public void setTime(final String word, final String column_nm) {
		if (TextUtils.isEmpty(word)) {
			return;
		}
		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				db.beginTransactionNonExclusive();
				try {
					ContentValues cv = new ContentValues();
					cv.put(column_nm, System.currentTimeMillis());
					db.update(DBHelper.TABLE_WORD, cv, "word = ?", new String[] { word.trim() });
					db.setTransactionSuccessful();
				} finally {
					db.endTransaction();
				}
			}
		};
		mThreadPool.submit(runnable);
	}

	public void increaseDifficulty(final String word) {
		if (TextUtils.isEmpty(word)) {
			return;
		}
		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				Cursor c = null;
				try {
					c = db.query(DBHelper.TABLE_WORD, new String[] { "difficulty", "last_test_result" }, "word = ?", new String[] { word.trim() }, null, null, null);
					if (c.moveToNext()) {

						short difficulty = c.getShort(c.getColumnIndex("difficulty"));

						// difficulty max is 20
						if (difficulty == Config.MAXDIFFICULTY) {
							return;
						}

						if (difficulty == -1) {
							difficulty += (1 + Config.INCREASE_DIFFICULTY);
						} else {
							difficulty += Config.INCREASE_DIFFICULTY;
						}

						if (difficulty > Config.MAXDIFFICULTY) {
							difficulty = Config.MAXDIFFICULTY;
						}

						try {
							db.beginTransactionNonExclusive();
							ContentValues cv = new ContentValues();
							cv.put("difficulty", difficulty);
							cv.put("last_test_result", 0);
							db.update(DBHelper.TABLE_WORD, cv, "word = ?", new String[] { word.trim() });
							db.setTransactionSuccessful();
							Log.i(TAG, "increaseDifficulty word is " + word);
						} finally {
							db.endTransaction();
						}
					}
				} finally {
					if (c != null) {
						c.close();
					}
				}
			}
		};
		mThreadPool.submit(runnable);
	}

	public void setDifficulty(final String word, final int difficulty) {
		if (TextUtils.isEmpty(word)) {
			return;
		}
		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				db.beginTransactionNonExclusive(); // ��ʼ����
				try {
					ContentValues cv = new ContentValues();
					cv.put("difficulty", difficulty);
					db.update(DBHelper.TABLE_WORD, cv, "word = ?", new String[] { word.trim() });
					db.setTransactionSuccessful(); // ��������ɹ����
				} finally {
					db.endTransaction(); // ��������
				}
			}
		};
		mThreadPool.submit(runnable);
	}

	public void decreaseDifficulty(final String word) {
		if (TextUtils.isEmpty(word)) {
			return;
		}
		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				Cursor c = null;
				try {
					c = db.query(DBHelper.TABLE_WORD, new String[] { "difficulty" }, "word = ?", new String[] { word.trim() }, null, null, null);
					if (c.moveToNext()) {
						int difficulty = c.getInt(c.getColumnIndex("difficulty"));

						if (difficulty == 0) {
							return;
						} else if (difficulty < 0) {
							difficulty = 0;
						} else {
							difficulty -= Config.DECREASE_DIFFICULTY;
						}

						try {
							db.beginTransactionNonExclusive(); // ��ʼ����
							ContentValues cv = new ContentValues();
							cv.put("difficulty", difficulty);
							cv.put("last_test_result", 1);
							db.update(DBHelper.TABLE_WORD, cv, "word = ?", new String[] { word.trim() });
							db.setTransactionSuccessful(); // ��������ɹ����
							Log.i(TAG, "decreaseDifficulty word is " + word);
						} finally {
							db.endTransaction(); // ��������
						}
					}
				} finally {
					if (c != null) {
						c.close();
					}
				}
			}
		};
		mThreadPool.submit(runnable);
	}

	/**
	 * close database
	 */
	public void close() {
		if (mThreadPool != null) {
			mThreadPool.shutdown();
			mThreadPool = null;
		}
		if (db != null) {
			db.close();	
			db = null;
		}
		dbManager = null;
	}
}
