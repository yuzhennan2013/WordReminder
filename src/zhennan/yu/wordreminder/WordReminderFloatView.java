package zhennan.yu.wordreminder;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import zhennan.yu.wordreminder.FloatView.OnActionDownListener;
import zhennan.yu.wordreminder.FloatView.OnActionUpListener;
import zhennan.yu.wordreminder.FloatView.OnClickListener;
import zhennan.yu.wordreminder.FloatView.OnLongPressedListener;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class WordReminderFloatView extends FloatView implements OnActionDownListener, OnActionUpListener, OnClickListener, OnLongPressedListener{

	// global float window singleton instance
	private static WordReminderFloatView mWordReminderFloatView;
	private static ImageView imageView;
	//thread that carry out the switch job
	BringTaskToFrontThread mBringTaskToFrontThread;
	/**
	 * when you switch task, some tasks, like the launcher,
	 * is useless task that you never want to switch to
	 * so this keeps the packagenames of those useless packagenames
	 */
	HashSet<String> exclusion;
	
	static Context mContext;
	
	private WordReminderFloatView(Context context, RelativeLayout.LayoutParams params, ImageView imageView) {
		super(context, params, imageView);
		exclusion = new HashSet<String>();
		updateExclusion();
	}
	
	private void updateExclusion() {
		PackageManager pm = mContext.getPackageManager();
		List<ApplicationInfo> apps = pm.getInstalledApplications(0);
		for (ApplicationInfo app : apps) {
			if ((app.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 1) {
				exclusion.add(app.packageName);
			} else if ((app.flags & ApplicationInfo.FLAG_SYSTEM) == 1) {
				exclusion.add(app.packageName);
			} else {
			}
		}
		exclusion.remove(Config.YOUDAODICPACNAME);
	}
	
	/**
	 * @param context: context must be the type of Activity
	 * @return
	 */
	public synchronized static WordReminderFloatView getFloatView(Context context) {
		if (mWordReminderFloatView == null) {
			mContext = context;
			imageView = new ImageView(context);
			imageView.setImageResource(R.drawable.floatview_defaut_transparent);
			
			Resources resources = mContext.getApplicationContext().getResources();
			int height = resources.getDimensionPixelSize(R.dimen.gamehall_float_height);
			int width = resources.getDimensionPixelSize(R.dimen.gamehall_float_width);

			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width, height);
			
			mWordReminderFloatView = new WordReminderFloatView(context, params, imageView);
			mWordReminderFloatView.setOnActionDownListener(mWordReminderFloatView);
			mWordReminderFloatView.setOnActionUpListener(mWordReminderFloatView);
			mWordReminderFloatView.setOnClickListener(mWordReminderFloatView);
			mWordReminderFloatView.setOnLongPressedListener(mWordReminderFloatView);
		}
		return mWordReminderFloatView;
	}

	/**
	 * @author yuzhennan BootReceiver must be declared in AndroidManifest.xml
	 *         otherwise you can't get to onReceive
	 *         this receiver is used to 
	 */
	public static class BootReceiver extends BroadcastReceiver {

		public BootReceiver() {
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			Intent LaunchIntent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
			context.startActivity(LaunchIntent);
		}
	}
	
	@Override
	protected void onAppsChanged() {
		updateExclusion();
	}

	@Override
	public void onActionDown() {
		imageView.setImageResource(R.drawable.floatview_default);
	}

	@Override
	public void onActionUp() {
		imageView.setImageResource(R.drawable.floatview_defaut_transparent);
	}
	
	/**
	 * if mode == 0, that's oneshot, means only one task is brought to front
	 * after you click ,if mode == 1, that's dartle, when you press the button,
	 * the background task will be brought to front continuously util you lift
	 * up your finger
	 * 
	 * @param mode
	 */
	private void bringTaskToFront(short mode) {

		if (mBringTaskToFrontThread == null) {
			mBringTaskToFrontThread = new BringTaskToFrontThread();
		} else if (mBringTaskToFrontThread.getState() == Thread.State.TERMINATED) {
			mBringTaskToFrontThread = null;
			mBringTaskToFrontThread = new BringTaskToFrontThread();
		}

		if (mBringTaskToFrontThread.getState() == Thread.State.NEW) {
			// setMode must be called before Thread.start()
			mBringTaskToFrontThread.setMode(mode);
			mBringTaskToFrontThread.start();
		}
	}

	@Override
	public void onClick() {
		bringTaskToFront((short) 0);
	}

	@Override
	public void onLongPressed() {
		bringTaskToFront((short) 1);
	}
	
	/**
	 * @author Administrator thread that bring background task to foreground
	 */
	class BringTaskToFrontThread extends Thread {
		// mode = 0, oneshot mode
		// mode = 1, dartle mode
		private int mode;

		public int getMode() {
			return mode;
		}

		public void setMode(int mode) {
			this.mode = mode;
		}

		@Override
		public void run() {
			while (!isFingerUp() || mode == 0) {
				// if you finger is not up, bringTaskToFront will be executed
				// continuously
				// get current OS launcher package name
				Intent intent = new Intent(Intent.ACTION_MAIN);
				intent.addCategory(Intent.CATEGORY_HOME);
				ResolveInfo resolveInfo = mContext.getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
				String currentLauncherPackage = resolveInfo.activityInfo.packageName;
				// exclude the launcher program
				exclusion.add(currentLauncherPackage);

				ActivityManager manager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
				// useless task must not be brought to the foreground
				int querytaskCnt = exclusion.size();
				int taskCntToSkip = 0;
				if (mode == 0) {
					// in oneshot mode, you switch between the most foreground
					// and
					// the second foreground, so use 2
					querytaskCnt += 2;
					taskCntToSkip = 1;
				} else {
					// in dartle mode, you switch between 3th and at most 10th
					// useful
					// task
					// here 10 may be other value, but i think 10 is really
					// enough
					querytaskCnt += 5;
					taskCntToSkip = 2;
				}

				List<RunningTaskInfo> runningTasks = manager.getRunningTasks(querytaskCnt);
				Iterator<RunningTaskInfo> runningTasksList = runningTasks.iterator();

				while (runningTasksList.hasNext() && (!isFingerUp() || mode == 0)) {
					RunningTaskInfo info = runningTasksList.next();
					if (uselessTask(info.baseActivity.getPackageName())) {

						continue;
					} else if (taskCntToSkip-- > 0) {

						continue;
					}

					// Message message = Message.obtain();
					// message.arg1 = info.id;
					// message.what = BRINGTASKFRONT;
					final ActivityManager activityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
					if (mode == 0) {
						// handler.sendMessage(message);
						activityManager.moveTaskToFront(info.id, ActivityManager.MOVE_TASK_WITH_HOME);
						return;
					} else {
						if (isFingerUp()) {
							// when thread wake up from sleep
							// and find that finger already up
							return;
						}

						activityManager.moveTaskToFront(info.id, ActivityManager.MOVE_TASK_WITH_HOME);

						try {
							Thread.sleep(900);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
		}

	}
	
	@Override
	protected void destroy() {
		// TODO Auto-generated method stub
		super.destroy();
		mWordReminderFloatView = null;
		exclusion.clear();
	}

	private boolean uselessTask(String packageNm) {
		if (exclusion.contains(packageNm)) {
			return true;
		}
		return false;
	}
}
