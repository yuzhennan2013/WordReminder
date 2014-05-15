package zhennan.yu.wordreminder;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import android.app.Notification;
import android.app.Notification.Builder;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import cn.sina.youxi.util.CV_Log;


public class ExternalDBObserver extends Service{
	
	private static ScheduledExecutorService mScheduler = null;
	private static ExternalDBStateListener mExternalDBStateListener;
	private static Context mContext;
	
	public static void registerExternalDBObserver(final Context context, final ExternalDBStateListener externalDBStateListener) {
		mContext = context;
		mExternalDBStateListener = externalDBStateListener;
		mContext.startService(new Intent(mContext, ExternalDBObserver.class));
	}

	public static void unregisterExternalDBObserver(){
		mContext.stopService(new Intent(mContext, ExternalDBObserver.class));
	}
	
	public interface ExternalDBStateListener{
		void onChange();
	}
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mScheduler != null) {
			mScheduler.shutdown();
			mScheduler = null;
		}
		stopForeground(true);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

        Builder builder = new Notification.Builder(mContext);
        builder.setSmallIcon(R.drawable.ic_launcher)//设置状态栏里面的图标（小图标）
               .setContentTitle("WordReminder").setContentText("synchronization service is running !");//设置上下文内容  
        Notification notification = builder.build();
		startForeground(1, notification);
        
		if (mScheduler == null) {
		    mScheduler = Executors.newSingleThreadScheduledExecutor();
		}
		mScheduler.scheduleWithFixedDelay(new Runnable() {
			
			@Override
			public void run() {
				CV_Log.i("begin to check external DB update!");
				if (DBStorageManager.shouldDoSync(mContext)) {
					CV_Log.i("external db changed !");
					mExternalDBStateListener.onChange();
				}
				else {
					CV_Log.i("don't need to update from external DB!");
				}
			}
		}, 0, 1, TimeUnit.MINUTES);
		super.onStartCommand(intent, flags, startId);
		return START_NOT_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
}
