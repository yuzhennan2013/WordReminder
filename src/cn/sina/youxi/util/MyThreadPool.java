package cn.sina.youxi.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyThreadPool {
	private static int cpuNums = Runtime.getRuntime().availableProcessors();
	private static ExecutorService mExecutorServie = Executors
			.newFixedThreadPool(2 * cpuNums);

	public static void addThread(Runnable task) {
		mExecutorServie.submit(task);
	}
}
