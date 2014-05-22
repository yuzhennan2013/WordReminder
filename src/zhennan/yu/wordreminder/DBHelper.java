package zhennan.yu.wordreminder;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper{
	
    public static final String DATABASE_NAME = "wordreminder.db"; 
    public static final String DATABASE_NAME_SHM = "wordreminder.db-shm"; 
    public static final String DATABASE_NAME_WAL = "wordreminder.db-wal"; 
    public static final String [] DB_ARR = {DATABASE_NAME, DATABASE_NAME_SHM, DATABASE_NAME_WAL};
    public static final String TABLE_WORD = "words";
    public static final String TABLE_AVAILABLEGROUP = "available";
    private static final int DATABASE_VERSION = 1;
      
    public DBHelper(Context context) {  
        super(context, DATABASE_NAME, null, DATABASE_VERSION);  
    }
    
	@Override
	public void onCreate(SQLiteDatabase db) {
		// removed
		// 1 : this word has been removed and should not show 
		// 0 : this word will be kept
		
		// last_test_result
		// 1 : this word has been remembered
		// 0 : this word is not remembered
		
		// difficulty
		// -1 : this word never tested
		// 0: this word is remembered
		// otherwise:not remembered, number indicates the weight of this word
		
		// group you should seperate so many words into different groups
		// that's, if words start with a have 300, you should divide these
		// words into 3 groups, A1->100(group = 1), A2->100(group = 2), A3->100(group = 3) 
		db.beginTransaction(); // ��ʼ����
		try {
	        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_WORD +  
	                "(word CHAR(70) PRIMARY KEY, meaning TEXT, difficulty INTEGER," +
	                " removed INTEGER, " +
	                "created_time INTEGER NOT NULL DEFAULT 0, " +
	                "last_rem_time INTEGER NOT NULL DEFAULT 0 ," +
	                "last_test_time INTEGER NOT NULL DEFAULT 0, " +
	                "last_test_result INTEGER NOT NULL DEFAULT 0, " +
	                " wordgroup INTEGER NOT NULL DEFAULT 0)");
	        
	        // this table contains available groups for each character in the alphabet
	        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_AVAILABLEGROUP +  
	        		"(initialchar CHAR(1) PRIMARY KEY, availablegroups TEXT)");
	        // initially, available group will all be 1
	        for (char character : RefreshContentIterator.CHARS) {
	        	db.execSQL("INSERT INTO " + TABLE_AVAILABLEGROUP + " VALUES(?, ?)", new Object[] {character, 1});
			}
	        db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		for (int version = oldVersion + 1; version <= newVersion; version++) {
			upgradeTo(db, version);
		}
	}
	
	/**
	 * Upgrade database from (version - 1) to version.
	 */
	private void upgradeTo(SQLiteDatabase db, int version) {
		switch (version) {
//		case 3:
//			addColumn(db, TABLE_NAME, "id",
//					"INTEGER NOT NULL DEFAULT 0");
//			
//			long start = SystemClock.uptimeMillis();
//			Cursor c = db.query(DBHelper.TABLE_NAME, new String[] { "word"},
//					null,
//					null,
//					null, null, null, null);
//			long end = SystemClock.uptimeMillis();
//			long querytime = end - start;
//			
//			start = SystemClock.uptimeMillis();
//			String word;
//			int i = 0;
//			
//			db.beginTransaction();
//			try {
//				while (c.moveToNext()) {
//					word = c.getString(c.getColumnIndex("word"));
//					CV_Log.i_debug(++i + " word is " + word);
//					db.execSQL("UPDATE " + TABLE_NAME + " SET id = ((SELECT MAX(id) FROM " + TABLE_NAME +  " ) + 1) WHERE word = \"" + word + "\"");
//				}
//				db.setTransactionSuccessful();
//			} finally{
//				db.endTransaction();
//			}
//
//			end = SystemClock.uptimeMillis();
//			
//			long updatetime = end - start;
//			
//			CV_Log.i_debug("query time is " + querytime);
//			CV_Log.i_debug("updatetime time is " + updatetime);
//			break;
//		case 2:
//			addColumn(db, TABLE_NAME, "created_time",
//					"INTEGER NOT NULL DEFAULT 0");
//			addColumn(db, TABLE_NAME, "last_rem_time",
//					"INTEGER NOT NULL DEFAULT 0");
//			addColumn(db, TABLE_NAME, "last_test_time",
//					"INTEGER NOT NULL DEFAULT 0");
//			// 1 : this word has been remembered
//			// 0 : this word has been forgotten
//			addColumn(db, TABLE_NAME, "last_test_result",
//					"INTEGER NOT NULL DEFAULT 0");
//			break;
//		default:
//			throw new IllegalStateException("Don't know how to upgrade to "
//					+ version);
		}
	}
	
	/**
	 * Add a column to a table using ALTER TABLE.
	 * 
	 * @param dbTable
	 *            name of the table
	 * @param columnName
	 *            name of the column to add
	 * @param columnDefinition
	 *            SQL for the column definition
	 */
	private void addColumn(SQLiteDatabase db, String dbTable,
			String columnName, String columnDefinition) {
		db.execSQL("ALTER TABLE " + dbTable + " ADD COLUMN " + columnName
				+ " " + columnDefinition);
	}
}
