package zhennan.yu.wordreminder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Environment;
import cn.sina.youxi.util.CV_Log;
import cn.sina.youxi.util.Regexp;
import eu.chainfire.libsuperuser.Debug;
import eu.chainfire.libsuperuser.Shell;

/**
 * @author Administrator if you want to back your DB, or import DB from
 *         somewhere, this class provide you methods
 */
public class DBStorageManager {

	public static void importDB(Context context, File DBFile) {
		if (DBFile == null) {
			return;
		}
		
		if (!DBFile.canRead()) {
			CV_Log.i("can't read " + DBFile.getAbsolutePath());
			return;
		}

		File data_dir = Environment.getDataDirectory();
		File internalDB = new File(data_dir, "//data//" + context.getPackageName() + "//databases//" + DBFile.getName());

		try {
			FileInputStream srcInputStream = new FileInputStream(DBFile);
			FileChannel src = srcInputStream.getChannel();
			FileOutputStream dstOutputStream = new FileOutputStream(internalDB);
			FileChannel dst = dstOutputStream.getChannel();
			dst.transferFrom(src, 0, src.size());
			src.close();
			dst.close();
			srcInputStream.close();
			dstOutputStream.close();
			CV_Log.i("successfully import " + DBFile.getAbsolutePath());
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void importDB(Context context) {

		File external_storage = Environment.getExternalStorageDirectory();
		File external_db_file = null;
		for (int i = 0; i < DBHelper.DB_ARR.length; i++) {
			external_db_file = new File(external_storage, DBHelper.DB_ARR[i]);
			importDB(context, external_db_file);
		}
	}

	public static void exportDB(Context context, File DBFile) {

		if (DBFile == null) {
			return;
		}

		if (!DBFile.canRead()) {
			CV_Log.i("can't read " + DBFile.getAbsolutePath());
			return;
		}

		File external_storage = Environment.getExternalStorageDirectory();
		if (!external_storage.canWrite()) {
			CV_Log.i("can't write to " + external_storage.getAbsolutePath());
			return;
		}

		File externalDB = new File(external_storage, DBFile.getName());
		
		try {
			FileInputStream srcInputStream = new FileInputStream(DBFile);
			FileChannel src = srcInputStream.getChannel();
			FileOutputStream dstOutputStream = new FileOutputStream(externalDB);
			FileChannel dst = dstOutputStream.getChannel();
			dst.transferFrom(src, 0, src.size());
			src.close();
			dst.close();
			srcInputStream.close();
			dstOutputStream.close();
			CV_Log.i("successfully export " + DBFile.getAbsolutePath());
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void exportDB(Context context) {
		File internal_data_storage = Environment.getDataDirectory();
		File internal_db_file = null;
		for (int i = 0; i < DBHelper.DB_ARR.length; i++) {
			internal_db_file = new File(internal_data_storage, "//data//" + context.getPackageName() + "//databases//" + DBHelper.DB_ARR[i]);
			exportDB(context, internal_db_file);
		}
	}

	/**
	 * check if a file has been changed since last modified use file last modify
	 * time
	 * 
	 * @param context
	 * @param file
	 * @return
	 */
	private static boolean fileChanged(Context context, File file) {
		SharedPreferences sharedPreferences = context.getSharedPreferences("sync_info", Context.MODE_PRIVATE);
		long lastmodtime = sharedPreferences.getLong(file.getAbsolutePath(), 0);
		if (lastmodtime != file.lastModified()) {
			CV_Log.i(file.getName() + " last modify time has been changed!");
			Editor editor = sharedPreferences.edit();
			editor.putLong(file.getAbsolutePath(), file.lastModified());
			editor.commit();
			return true;
		}
		return false;
	}

	/**
	 * when check if a sync operation is over you just check (key = youdaoDB
	 * last_modify, value = true / false)
	 * 
	 * @param context
	 * @return
	 */
	public static boolean syncFinished(Context context) {
		SharedPreferences sharedPreferences = context.getSharedPreferences("sync_info", Context.MODE_PRIVATE);
		boolean finished = sharedPreferences.getBoolean(String.valueOf(youdaoDB.lastModified()), false);
		if (finished) {
			CV_Log.i("sync already finished, external db last modify time is ?", youdaoDB.lastModified());
		}
		return finished;
	}

	public static void markSyncBegan(Context context) {
		SharedPreferences sharedPreferences = context.getSharedPreferences("sync_info", Context.MODE_PRIVATE);
		Editor editor = sharedPreferences.edit();
		editor.putBoolean(String.valueOf(youdaoDB.lastModified()), false);
		editor.commit();
		CV_Log.i("markSyncBegan task id is " + youdaoDB.lastModified());
	}

	public static void markSyncFinished(Context context) {
		SharedPreferences sharedPreferences = context.getSharedPreferences("sync_info", Context.MODE_PRIVATE);
		Editor editor = sharedPreferences.edit();
		String last_modify_str = String.valueOf(youdaoDB.lastModified());
		// mark this sync operation corresponds to this last_modify_time is over
		editor.putBoolean(last_modify_str, true);
		Map<String, ?> map = sharedPreferences.getAll();
		Pattern pattern = Pattern.compile(Regexp.non_negative_integers_regexp);
		Matcher matcher;
		String key;
		// except the lastest sync operation result,
		// there is no need to preserve previous sync operation results
		// so delete them
		for (Entry<String, ?> entry : map.entrySet()) {
			key = entry.getKey().toString();
			matcher = pattern.matcher(key);
			if (matcher.matches() && !last_modify_str.equals(key)) {
				editor.remove(key);
			}
		}
		editor.commit();
		CV_Log.i("markSyncFinished task id is " + youdaoDB.lastModified());
	}

	static File data;
	static File externalData;
	static String youdaoInternalDBPath;
	static File youdaoDB;
	static File tempDB;

	/**
	 * indicates whether synchronization from third party db to local db is
	 * needed if needed, third party db will be copied to external storage with
	 * the name temp.db
	 * 
	 * @return
	 */
	public static boolean shouldDoSync(Context context) {

		data = Environment.getDataDirectory();
		externalData = Environment.getExternalStorageDirectory();
		youdaoInternalDBPath = "//data//" + Config.YOUDAODICPACNAME + "//databases//notes.db";
		youdaoDB = new File(data, youdaoInternalDBPath);
		tempDB = new File(externalData, Config.TEMPDBNAME);

		if (!youdaoDB.exists()) {
			CV_Log.i("external db not exists");
			return false;
		}
		if (fileChanged(context, youdaoDB) || !syncFinished(context)) {
			Debug.setDebug(false);
			Shell.SU.run(new String[] { "cp -f " + youdaoDB.getAbsolutePath() + " " + tempDB.getAbsolutePath() });
			CV_Log.i("cp -f " + youdaoDB.getAbsolutePath() + " " + tempDB.getAbsolutePath());
			if (tempDB.exists()) {
				Shell.SU.run(new String[] { "chmod 777" + tempDB.getAbsolutePath() });
			}
			return true;
		}
		return false;
	}
}
