package zhennan.yu.wordreminder;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

/**
 * @author Administrator
 * CRUD related to temp.db, that's db file that imported from youdao
 */
public class TempDBManager {
	static String TAG = "TempDBManager";   
	static TempDBManager tempDBManager;
	// external DB file that you want to manipulate
	static SQLiteDatabase mTempDB = null;
	
	synchronized public static TempDBManager getInstance(File ... tempDBFile) {
		if (tempDBManager == null) {
			tempDBManager = new TempDBManager();
			if (tempDBFile.length > 0) {
				mTempDB = SQLiteDatabase.openDatabase(tempDBFile[0].getAbsolutePath(), null, SQLiteDatabase.OPEN_READONLY);
			}
		}
		return tempDBManager;
	}

	/**
	 * query new added words from provided timeline
	 * @return
	 */
	public Cursor queryWordAfter(long timeline){
		Cursor c = mTempDB.query("notes", new String[]{"word", "created", "detail"}, "created >= ?", new String[] {String.valueOf(timeline)}, null, null, null);
		return c;
	}
	
	/**
	 * close database
	 */
	public void close() {
		if (mTempDB != null) {
			mTempDB.close();
			mTempDB = null;
		}
		if (tempDBManager != null) {
			tempDBManager = null;
		}
	}
}
