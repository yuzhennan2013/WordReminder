package cn.sina.youxi.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;

public class FileHelper {

	private final Context context;
	private static FileHelper fileHelper;
	public static String SDPATH; // SD卡路径
	public static String INTERNALPATH; // 手机内存中的文件路径

	private FileHelper(Context context) {
		this.context = context;
		SDPATH = Environment.getExternalStorageDirectory().getPath() + "//";
		INTERNALPATH = this.context.getFilesDir().getPath() + "//";
	}

	public static FileHelper getInstance(Context context) {
		if (fileHelper == null) {
			synchronized (FileHelper.class) {
				if (fileHelper == null) {
					fileHelper = new FileHelper(context);
				}
			}
		}
		return fileHelper;
	}

	/** 判断SDCard是否存在？是否可以进行读写 */
	public boolean SDCardReady() {
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {// 表示SDCard存在并且可以读写
			return true;
		} else {
			return false;
		}
	}

	/** 获取SDCard文件路径 */
	public String SDCardPath() {
		if (SDCardReady()) {// 如果SDCard存在并且可以读写
			SDPATH = Environment.getExternalStorageDirectory().getPath();
			return SDPATH;
		} else {
			return null;
		}
	}

	/** 获取SDCard 总容量大小(MB) */
	public long SDCardTotal() {
		if (null != SDCardPath() && SDCardPath().equals("")) {
			StatFs statfs = new StatFs(SDCardPath());
			// 获取SDCard的Block总数
			long totalBlocks = statfs.getBlockCount();
			// 获取每个block的大小
			long blockSize = statfs.getBlockSize();
			// 计算SDCard 总容量大小MB
			long SDtotalSize = totalBlocks * blockSize / 1024 / 1024;
			return SDtotalSize;
		} else {
			return 0;
		}
	}

	/** 获取SDCard 可用容量大小(MB) */
	public long SDCardFree() {
		if (null != SDCardPath() && SDCardPath().equals("")) {
			StatFs statfs = new StatFs(SDCardPath());
			// 获取SDCard的Block可用数
			long availaBlocks = statfs.getAvailableBlocks();
			// 获取每个block的大小
			long blockSize = statfs.getBlockSize();
			// 计算SDCard 可用容量大小MB
			long SDFreeSize = availaBlocks * blockSize / 1024 / 1024;
			return SDFreeSize;
		} else {
			return 0;
		}
	}

	/**
	 * path=SDPATH(path=SDPATH 只是根路径，还需要指定根目录下的子目录) 删除SD卡上的目录 path=INTERNALPATH
	 * 删除手机内存上的目录
	 * 
	 * @param dirName
	 * @return 删除操作是否成功，没有删除任何东西返回false
	 */
	public boolean delDir(String dirName, String path) {
		File dir = new File(path + dirName);
		return delDir(dir);
	}

	/**
	 * path=SDPATH(path=SDPATH 只是根路径，还需要指定根目录下的子目录) 判断SD卡上这个文件是否已经存在
	 * path=INTERNALPATH 判断手机内存上这个文件是否已经存在
	 * 
	 * @param fileName
	 *            要检查的文件名
	 * @return boolean, true表示存在，false表示不存在
	 */
	public boolean isFileExist(String fileName, String path) {
		File file = new File(path + fileName);
		return file.exists();
	}

	/**
	 * path=SDPATH(path=SDPATH 只是根路径，还需要指定根目录下的子目录) 删除SD卡上的文件 path=INTERNALPATH
	 * 删除手机内存上的文件
	 * 
	 * @param fileName
	 */
	public boolean delFile(String fileName, String path) {
		File file = new File(path + fileName);
		if (file == null || !file.exists() || file.isDirectory())
			return false;
		return file.delete();
	}

	/**
	 * path=SDPATH(path=SDPATH 只是根路径，还需要指定根目录下的子目录) 修改SD卡上的文件或目录名
	 * path=INTERNALPATH 修改手机内存上的文件或目录名
	 * 
	 * @param fileName
	 */
	public boolean renameFile(String oldfileName, String newFileName,
			String path) {
		File oleFile = new File(path + oldfileName);
		File newFile = new File(path + newFileName);
		return oleFile.renameTo(newFile);
	}

	public void writeStringIntoSDFile(String content, String filename) {

		try {
			File file = new File(SDPATH + filename);
			BufferedWriter out = new BufferedWriter(new FileWriter(file, true));
			out.write(content);
			out.close();
			out = null;
			file = null;
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	/**
	 * 将文件写入应用私有的files目录。如:writeFile("test.txt");
	 */
	public Output wirteFile(String fileName, String path) throws IOException {
		OutputStream os = context.openFileOutput(fileName,
				Context.MODE_WORLD_WRITEABLE);
		return new Output(os);
	}

	/**
	 * 在原有文件上继续写文件。如:appendFile("test.txt");
	 */
	public Output appendPrivateFile(String fileName, String path)
			throws IOException {
		OutputStream os = context.openFileOutput(fileName, Context.MODE_APPEND);
		return new Output(os);
	}

	/**
	 * 从应用的私有目录files读取文件。如:readFile("test.txt");
	 */
	public Input readPrivateFile(String fileName, String path)
			throws IOException {
		InputStream is = context.openFileInput(fileName);
		return new Input(is);
	}

	/**
	 * 删除一个文件
	 * 
	 * @param file
	 * @return
	 */
	public boolean delFile(File file) {
		if (file.isDirectory())
			return false;
		return file.delete();
	}

	/**
	 * 删除一个目录（可以是非空目录）
	 * 
	 * @param dir
	 */
	public boolean delDir(File dir) {
		if (dir == null || !dir.exists() || dir.isFile()) {
			return false;
		}
		for (File file : dir.listFiles()) {
			if (file.isFile()) {
				file.delete();
			} else if (file.isDirectory()) {
				delDir(file);// 递归
			}
		}
		dir.delete();
		return true;
	}

	/**
	 * 拷贝一个文件,srcFile源文件，destFile目标文件
	 * 
	 * @param path
	 * @throws IOException
	 */
	public boolean copyFileTo(File srcFile, File destFile) throws IOException {
		FileInputStream fis = null;
		FileOutputStream fos = null;
		try {
			if (srcFile.isDirectory() || destFile.isDirectory())
				return false;// 判断是否是文件
			fis = new FileInputStream(srcFile);
			fos = new FileOutputStream(destFile);
			int readLen = 0;
			byte[] buf = new byte[1024];
			while ((readLen = fis.read(buf)) != -1) {
				fos.write(buf, 0, readLen);
			}
			return true;
		} catch (Exception e) {
			throw new IOException();
		} finally {
			fos.flush();
			fos.close();
			fis.close();
		}
	}

	/**
	 * 拷贝目录下的所有文件到指定目录
	 * 
	 * @param srcDir
	 * @param destDir
	 * @return
	 * @throws IOException
	 */
	public boolean copyFilesTo(File srcDir, File destDir) throws IOException {
		if (!srcDir.isDirectory() || !destDir.isDirectory())
			return false;// 判断是否是目录
		if (!destDir.exists())
			return false;// 判断目标目录是否存在
		File[] srcFiles = srcDir.listFiles();
		for (int i = 0; i < srcFiles.length; i++) {
			if (srcFiles[i].isFile()) {
				// 获得目标文件
				File destFile = new File(destDir.getPath() + "//"
						+ srcFiles[i].getName());
				copyFileTo(srcFiles[i], destFile);
			} else if (srcFiles[i].isDirectory()) {
				File theDestDir = new File(destDir.getPath() + "//"
						+ srcFiles[i].getName());
				copyFilesTo(srcFiles[i], theDestDir);
			}
		}
		return true;
	}

	/**
	 * 移动一个文件
	 * 
	 * @param srcFile
	 * @param destFile
	 * @return
	 * @throws IOException
	 */
	public boolean moveFileTo(File srcFile, File destFile) throws IOException {
		boolean iscopy = copyFileTo(srcFile, destFile);
		if (!iscopy)
			return false;
		delFile(srcFile);
		return true;
	}

	/**
	 * 移动目录下的所有文件到指定目录
	 * 
	 * @param srcDir
	 * @param destDir
	 * @return
	 * @throws IOException
	 */
	public boolean moveFilesTo(File srcDir, File destDir) throws IOException {
		if (!srcDir.isDirectory() || !destDir.isDirectory()) {
			return false;
		}
		File[] srcDirFiles = srcDir.listFiles();
		for (int i = 0; i < srcDirFiles.length; i++) {
			if (srcDirFiles[i].isFile()) {
				File oneDestFile = new File(destDir.getPath() + "//"
						+ srcDirFiles[i].getName());
				moveFileTo(srcDirFiles[i], oneDestFile);
				delFile(srcDirFiles[i]);
			} else if (srcDirFiles[i].isDirectory()) {
				File oneDestFile = new File(destDir.getPath() + "//"
						+ srcDirFiles[i].getName());
				moveFilesTo(srcDirFiles[i], oneDestFile);
				delDir(srcDirFiles[i]);
			}

		}
		return true;
	}

	/**
	 * 获取文件MIME 类型
	 * 
	 * @param paramFile
	 * @return
	 */
	public static String getMIMEType(File paramFile) {
		String str1 = paramFile.getName();
		String str2 = str1.substring(1 + str1.lastIndexOf("."), str1.length())
				.toLowerCase();
		String str3 = null;

		if ((str2.equals("m4a")) || (str2.equals("mp3"))
				|| (str2.equals("mid")) || (str2.equals("xmf"))
				|| (str2.equals("ogg")) || (str2.equals("wav"))) {
			str3 = "audio";
		}
		while (true) {
			if (!str2.equals("apk")) {
				str3 = str3 + "/*";
				return str3;
			}

			if ((str2.equals("3gp")) || (str2.equals("mp4"))) {
				str3 = "video";
				continue;
			}
			if ((str2.equals("jpg")) || (str2.equals("gif"))
					|| (str2.equals("png")) || (str2.equals("jpeg"))
					|| (str2.equals("bmp"))) {
				str3 = "image";
				continue;
			}
			if (str2.equals("apk")) {
				str3 = "application/vnd.android.package-archive";
				continue;
			}
			str3 = "*";
		}
	}
}