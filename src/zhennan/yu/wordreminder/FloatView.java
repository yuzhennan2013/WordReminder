package zhennan.yu.wordreminder;

import java.lang.reflect.Field;

import android.animation.Animator;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import cn.sina.youxi.util.CV_Log;
import cn.sina.youxi.util.ScreenUtils;

/**
 * @author Administrator
 * this class provides only base class of FloatView
 * only fundamental functions are available
 */
public class FloatView extends RelativeLayout implements ScreenStateListener{

	private static final String TAG = "FloatView";

	private float mTouchStartX;
	private float mTouchStartY;
	private float x;
	private float y;

	private float tempX = 0;
	private float tempY = 0;
	private long tempT = 0;

	private int mStatusBarHeight = 0;

	private Context mContext;

	public static WindowManager.LayoutParams mWindowMgrParams;
	public static WindowManager mWindowManager = null;
	Animator animation = null;

	private final static int HIDE = 0;
	private final static int SHOW = 1;

	boolean isNotFullScreen = true;
	boolean longPressed = false;
	// the width of the round button that you press
	int roundBtnWidth;

	// task present the docking animation
	AnimatingTask dockingTask;
	
	//keep the screen width and height info
	// screendimension.x is width, y is height
	// that's portrait value, not landscape
	Point screendimension;
	// present the current width and height of the screen
	Point currentdimension;
	// indicates the max horizontal position the round button can be
	int xMovementLimit;
	// indicates the max vertical position the round button can be	
	int yMovementLimit;
	// if the round button is docking, prevent user from interrupt it
	boolean moving = false;
	// when your finger press the button this value is false
	// when your finger is up this value is true
	private volatile boolean fingerUP = false;

	// the view that floating as your finger moves
	View mFLoatView;
	
	OnLongPressedListener mOnLongPressedListener;
	OnActionDownListener mOnActionDownListener;
	OnActionUpListener mOnActionUpListener;
	OnClickListener mOnClickListener;
	
	/**
	 * @param context: context must be the type of Activity
	 * @return
	 */
	protected FloatView(Context context, View view) {
		this(context, null, view);
	}

	protected FloatView(Context context, RelativeLayout.LayoutParams params, View view) {
		super(context);
		mContext = context;
		mFLoatView = view;
		screendimension = ScreenUtils.getScreenDimen((Activity) mContext);
		currentdimension = screendimension;
		initFloatView(params);
		initReceiver();
	}

	/**
	 * @author Administrator detect if screen is poweroff ,also screen locked is
	 *         detected
	 */
	class ScreenLockReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {

			if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
				// do whatever you need to do here
				// mInfiniteLoopThread.timetostop = true;
				showOrHide(HIDE);
			} else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
				// and do whatever you need to do here
				showOrHide(SHOW);
			} else if (intent.getAction().equals(Intent.ACTION_USER_PRESENT)) {
				showOrHide(SHOW);
			} else if (intent.getAction().equals(Intent.ACTION_PACKAGE_ADDED) || intent.getAction().equals(Intent.ACTION_PACKAGE_REMOVED)) {
				onAppsChanged();
			}
		}
	}

	private ScreenLockReceiver receiver1;

	private void initReceiver() {

		// only when you enable screenlock function on your cellphone
		// can this receiver be triggered
		IntentFilter screenOnOffFilter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
		screenOnOffFilter.addAction(Intent.ACTION_SCREEN_ON);
		screenOnOffFilter.addAction(Intent.ACTION_USER_PRESENT);
		screenOnOffFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
		screenOnOffFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
		screenOnOffFilter.addDataScheme("package");
		receiver1 = new ScreenLockReceiver();
		mContext.registerReceiver(receiver1, screenOnOffFilter);
	}

	/**
	 * 
	 */
	private void showOrHide(int showorhide) {
		synchronized (FloatView.class) {
			if (showorhide == SHOW) {
				if (getVisibility() == View.GONE) {
					if (getParent() != null) {
						setVisibility(View.VISIBLE);
					} else {
						mWindowManager.addView(FloatView.this, mWindowMgrParams);
					}
				}
			} else if (showorhide == HIDE) {
				setVisibility(View.GONE);
			}
		}
	}

	protected void hide() {
		setVisibility(View.GONE);
	}

	protected void destroy() {
		try {
			if (receiver1 != null) {
				mContext.unregisterReceiver(receiver1);
			}
			mWindowManager.removeView(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	final protected boolean isFingerUp() {
		return fingerUP;
	}
	
	public void show() {
		if (getParent() != null) {
			setVisibility(View.VISIBLE);
		} else {
			mWindowManager.addView(this, mWindowMgrParams);
			setVisibility(View.VISIBLE);
		}
	}

	private boolean isFullScreen(Activity activity) {
		WindowManager.LayoutParams attrs = activity.getWindow().getAttributes();
		if ((attrs.flags & WindowManager.LayoutParams.FLAG_FULLSCREEN) == WindowManager.LayoutParams.FLAG_FULLSCREEN) {
			return true;
		} else {
			return false;
		}
	}

	private void setPosition() {
		SharedPreferences sharedPreferences = mContext.getSharedPreferences("position", Context.MODE_PRIVATE);
		int curX = sharedPreferences.getInt("x", 0);
		int curY = sharedPreferences.getInt("y", 0);
		if (curX >= 0 && curY >= 0) {
			mWindowMgrParams.x = curX;
			mWindowMgrParams.y = curY;
		} else {
			mWindowMgrParams.x = xMovementLimit;
			mWindowMgrParams.y = currentdimension.y / 2;
		}
	}

	/**
	 * when orientation change, place the round button 
	 * to the nearest position
	 */
	private void positionToNearest(int orientation) {
		xMovementLimit = currentdimension.x - roundBtnWidth / 2;
		yMovementLimit = currentdimension.y - roundBtnWidth / 2;
		int [] location = new int[2];
		getLocationOnScreen(location);
		if (location[0] < currentdimension.x / 2) {
			mWindowMgrParams.x = 0;	
		}
		else {
			mWindowMgrParams.x = xMovementLimit;	
		}
		float ratio = 0.0f;
		if (orientation == ScreenStateListener.ORIENTATION_LANDSCAPE) {
			// for example , if round button is at the lower half part when portrait
			// it will keep its relative position to screen height when orientation is landscape
			ratio = (float)location[1] / screendimension.y;
			mWindowMgrParams.y = (int) (ratio * screendimension.x);
		}
		else {
			ratio = (float)location[1] / screendimension.x;
			mWindowMgrParams.y = (int) (ratio * screendimension.y);
		}
		mWindowManager.updateViewLayout(this, mWindowMgrParams);
	}
	
	private void initFloatView(RelativeLayout.LayoutParams params) {
		
		mWindowManager = (WindowManager) mContext.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
		mWindowMgrParams = new WindowManager.LayoutParams();
		
		if (params == null) {
			params = new RelativeLayout.LayoutParams(100, 100);
		}

		roundBtnWidth = params.width;
		
		
		xMovementLimit = currentdimension.x -  roundBtnWidth / 2;
		yMovementLimit = currentdimension.y - roundBtnWidth / 2;
		
		setClickable(true);
		setStatusBarHeight(isFullScreen(((Activity) mContext)));
		
		addView(mFLoatView, params);

		mWindowMgrParams.type = android.view.WindowManager.LayoutParams.TYPE_PHONE;
																			
		mWindowMgrParams.format = PixelFormat.RGBA_8888;

		mWindowMgrParams.flags = android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
		/*
		 * wmParams.flags=LayoutParams.FLAG_NOT_TOUCH_MODAL |
		 * LayoutParams.FLAG_NOT_FOCUSABLE | LayoutParams.FLAG_NOT_TOUCHABLE;
		 */
		mWindowMgrParams.gravity = Gravity.LEFT | Gravity.TOP;

		setPosition();

		mWindowMgrParams.width = RelativeLayout.LayoutParams.WRAP_CONTENT;
		mWindowMgrParams.height = RelativeLayout.LayoutParams.WRAP_CONTENT;

		getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			
			@Override
			public void onGlobalLayout() {
				
				//detect fullscreen / normalscreen change
				int [] location = new int[2];
				getLocationOnScreen(location);
				if (mWindowMgrParams.y == location[1]) {
					// is full screen
					isNotFullScreen = false;
				}
				else {
					isNotFullScreen = true;
				}

				//detect screen orientation change
				Point tempDimension = ScreenUtils.getScreenDimen((Activity) mContext);
				if (screendimension.x == tempDimension.y) {
					//now is in landscape
					if (currentdimension.x == screendimension.x) {
						// this is the first time that detect screen orientation
						// is changed to landscape
						currentdimension = tempDimension;
						onOrientationChanged(ScreenStateListener.ORIENTATION_LANDSCAPE);
					}
				}
				else {
					//now is in portrait
					if (currentdimension.x != screendimension.x) {
						// this is the first time that screen orientation
						// is recovered from landscape for the first time
						currentdimension = tempDimension;
						onOrientationChanged(ScreenStateListener.ORIENTATION_PORTRAIT);
					}
				}
			}
		});
	}
	
	/**
	 * when you hold you finger on the float view 
	 * for a certain time, this will be called and only once before
	 * your finger is up
	 */
	interface OnLongPressedListener{
		void onLongPressed();
	}
	interface OnActionDownListener{
		void onActionDown();
	}
	interface OnActionUpListener{
		void onActionUp();
	}
	interface OnClickListener{
		void onClick();
	}

	protected void setOnLongPressedListener(OnLongPressedListener onLongPressedListener) {
		mOnLongPressedListener = onLongPressedListener;
	}
	
	protected void setOnActionDownListener(OnActionDownListener onActionDownListener) {
		mOnActionDownListener = onActionDownListener;
	}
	
	protected void setOnActionUpListener(OnActionUpListener onActionUpListener) {
		mOnActionUpListener = onActionUpListener;
	}
	
	protected void setOnClickListener(OnClickListener onClickListener) {
		mOnClickListener = onClickListener;
	}
	
	/**
	 * if there is any app that has been removed or added
	 * this will be called
	 */
	protected void onAppsChanged() {
		
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		
		// setStatusBarHeight(isFullScreen());
		// the docking animation is going on
		if (moving) {
			return true;
		}

		x = event.getRawX();
		y = event.getRawY();
		
		if (isNotFullScreen) {
			y -= mStatusBarHeight;
		}

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			fingerUP = false;
			longPressed = false;
			if (mOnActionDownListener != null) {
				mOnActionDownListener.onActionDown();
			}
			// getRawX() and getRawY() that is guaranteed to return absolute
			// coordinates, relative to the device screen.
			// While getX() and getY(), should return you coordinates, relative
			// to the View, that dispatched them.
			mTouchStartX = event.getX();
			mTouchStartY = event.getY();

			tempX = x;
			tempY = y;
			tempT = SystemClock.uptimeMillis();

			break;

		case MotionEvent.ACTION_MOVE:
			mWindowMgrParams.x = (int) (x - mTouchStartX);
			mWindowMgrParams.y = (int) (y - mTouchStartY);
			if (withinClickRegion()) {
				if (!longPressed && SystemClock.uptimeMillis() - tempT > 800) {
					longPressed = true;
					if (mOnLongPressedListener != null) {
						mOnLongPressedListener.onLongPressed();
					}
				}
			} else {
				mWindowManager.updateViewLayout(this, mWindowMgrParams);
			}
			break;

		case MotionEvent.ACTION_UP:
			fingerUP = true;
			longPressed = false;
			if (mOnActionUpListener != null) {
				mOnActionUpListener.onActionUp();
			}

			if (withinClickRegion()) {
				if (SystemClock.uptimeMillis() - tempT < 100) {
					// this is click operation
					if (mOnClickListener != null) {
						mOnClickListener.onClick();
					}
				}
			} else {
				// below is fling testing
				moving = true;
				boolean isFling = false;
				float k = 0, b = 0, y1;
				float kmax, kmin;
				// y should at least be defaultFlipY
				int defaultFlipYmin = 30;
				int defaultFlipYmax = yMovementLimit - 30;
				if (SystemClock.uptimeMillis() - tempT < 130 && Math.abs(x - tempX) > 10) {
					// a fling is considered valid only when 
					// the user is finging towards the center of screen, not the oppsite
					boolean flingIsValid = false;
					float deltaY = y - tempY;
					float deltaX = x - tempX;
					k = deltaY / deltaX;
					if (x < (currentdimension.x / 2)) {
						kmax = (y - currentdimension.y) / (x - currentdimension.x);
						kmin = y / (x - currentdimension.x);
						if (deltaX > 0) {
							// when button is on left side of screen
							// you must fling to right
							flingIsValid = true;
						}
					} else {
						kmax = y / x;
						kmin = (y - currentdimension.y) / x;
						if (deltaX < 0) {
							// when button is on right side of screen
							// you must fling to left
							flingIsValid = true;
						}
					}
					if (k < kmax && k > kmin && flingIsValid) {
						isFling = true;
						b = y - k * x;
					}
				}

				//

				x = x - mTouchStartX;
				y = y - mTouchStartY;

				Point finalPoint = new Point();
				if (x < (currentdimension.x / 2)) {
					// you finger now is on the left side
					if (isFling) {
						// if your finger flips
						y1 = k * xMovementLimit + b;
						if (y1 < defaultFlipYmin) {
							y1 = defaultFlipYmin;
						} else if (y1 > defaultFlipYmax) {
							y1 = defaultFlipYmax;
						}
						finalPoint.x = xMovementLimit;
						finalPoint.y = (int) y1;
					} else {
						// the button will move only in horizontal direction
						finalPoint.x = 0;
						finalPoint.y = (int) y;
					}
				} else {
					// you finger now is on the right side
					if (isFling) {
						// if your finger flips
						y1 = b;
						if (y1 < defaultFlipYmin) {
							y1 = defaultFlipYmin;
						} else if (y1 > defaultFlipYmax) {
							y1 = defaultFlipYmax;
						}
						finalPoint.x = 0;
						finalPoint.y = (int) y1;
					} else {
						// the button will move only in horizontal direction
						finalPoint.x = xMovementLimit;
						finalPoint.y = (int) y;
					}
				}

				Point startPoint = new Point((int) x, (int) y);
				
				mWindowMgrParams.x = startPoint.x;
				mWindowMgrParams.y = startPoint.y;

				mWindowManager.updateViewLayout(this, mWindowMgrParams);
				
				dockingTask = new AnimatingTask();
				dockingTask.execute(startPoint, finalPoint);
			}

			mTouchStartX = mTouchStartY = 0;
			break;
		}
		return true;
	}

	private boolean withinClickRegion() {
		return Math.abs(x - tempX) <= 10 && Math.abs(y - tempY) <= 10;
	}

	private void savePosition() {
		SharedPreferences sharedPreferences = mContext.getSharedPreferences("position", Context.MODE_PRIVATE);
		Editor editor = sharedPreferences.edit();
		editor.putInt("x", mWindowMgrParams.x);
		editor.putInt("y", mWindowMgrParams.y);
		editor.commit();
	}

	/**
	 * @author Administrator animation task that carry out the animation
	 */
	class AnimatingTask extends AsyncTask<Point, WindowManager.LayoutParams, WindowManager.LayoutParams> {
		
		@Override
		protected void onPostExecute(android.view.WindowManager.LayoutParams result) {
			moving = false;
			mWindowManager.updateViewLayout(FloatView.this, mWindowMgrParams);
			savePosition();
		}

		@Override
		protected void onCancelled() {
			// TODO Auto-generated method stub
			super.onCancelled();
			moving = false;
		}

		@Override
		protected void onProgressUpdate(android.view.WindowManager.LayoutParams... values) {
			if (isCancelled()) {
				return;
			}
			mWindowManager.updateViewLayout(FloatView.this, values[0]);
		}

		@Override
		protected android.view.WindowManager.LayoutParams doInBackground(Point... paramVarArgs) {

			float dx = paramVarArgs[1].x - paramVarArgs[0].x;
			float dy = paramVarArgs[1].y - paramVarArgs[0].y;

			float steps = Math.abs(dx);
			float absdy = Math.abs(dy);
			float xIncrement = 0, yIncrement = 0;
			if (steps < absdy) {
				steps = absdy;
			}

			//

			xIncrement = dx / steps;
			yIncrement = dy / steps;

			//
			//
			// because xIncrement or yIncrement may not be integer,
			// but pixels are integer, so you must choose to get floor or ceil
			// of the increment value
			// and then you must save the error in errorX or errorY
			float errorX = 0.0f;
			float errorY = 0.0f;
			float actualX = mWindowMgrParams.x, actualY = mWindowMgrParams.y;
			int tempX, tempY;
			float tempX1, tempY1;

			// caculate how many pixels each step cost
			int i = 0;
			for (; i < steps && !isCancelled(); i++) {
				actualX += xIncrement;
				actualY += yIncrement;

				if (i % 4 == 0) {
					tempX = (int) actualX;
					tempX1 = actualX - tempX;

					if (xIncrement > 0) {
						// if actualX is , for example, 20.9,then i will choose
						// 21 for
						// its next movement x value rather than 20
						// but if it is 20.1, then choose 20 rather than 21
						if (tempX1 > 0.5) {
							tempX++;
						}
						errorX -= tempX1;

						if (errorX > 1.0f) {
							tempX--;
							errorX -= 1.0f;
						} else if (errorX < -1.0f) {
							tempX++;
							errorX += 1.0f;
						}
					} else {

						if (tempX1 > 0.5) {
							tempX++;
						}
						errorX += tempX1;

						if (errorX > 1.0f) {
							tempX++;
							errorX -= 1.0f;
						} else if (errorX < -1.0f) {
							tempX--;
							errorX += 1.0f;
						}

					}

					tempY = (int) actualY;
					tempY1 = actualY - tempY;
					if (yIncrement > 0) {
						if (tempY1 > 0.5) {
							tempY++;
						}
						errorY -= tempY1;

						if (errorY > 1.0f) {
							tempY--;
							errorY -= 1.0f;
						} else if (errorY < -1.0f) {
							tempY++;
							errorY += 1.0f;
						}
					} else {

						if (tempY1 > 0.5) {
							tempY++;
						}
						errorY += (tempY1);

						if (errorY > 1.0f) {
							tempY++;
							errorY -= 1.0f;
						} else if (errorY < -1.0f) {
							tempY--;
							errorY += 1.0f;
						}

					}

					mWindowMgrParams.x = tempX;
					mWindowMgrParams.y = tempY;

					publishProgress(mWindowMgrParams);
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			if ((i - 1) % 4 != 0) {
				tempX = (int) actualX;
				if (xIncrement < 0) {
					tempX++;
				}
				tempY = (int) actualY;
				if (yIncrement < 0) {
					tempY++;
				}

				mWindowMgrParams.x = tempX;
				mWindowMgrParams.y = tempY;
				//
				//

				publishProgress(mWindowMgrParams);
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			mWindowMgrParams.x = paramVarArgs[1].x;
			mWindowMgrParams.y = paramVarArgs[1].y;
			return mWindowMgrParams;
		}

	}

	private void setStatusBarHeight(boolean isFullScreen) {
		if (isFullScreen) {
			mStatusBarHeight = 0;
		} else {
			mStatusBarHeight = getStatusBarHeight(mContext);
		}
	}

	private int getStatusBarHeight(Context context) {
		Class<?> c = null;
		Object obj = null;
		Field field = null;
		int x = 0, statusBarHeight = 0;
		try {
			c = Class.forName("com.android.internal.R$dimen");
			obj = c.newInstance();
			field = c.getField("status_bar_height");
			x = Integer.parseInt(field.get(obj).toString());
			statusBarHeight = context.getResources().getDimensionPixelSize(x);
		} catch (Exception e) {
			e.printStackTrace();
			return 25;
		}

		return statusBarHeight;
	}

	@Override
	public void onOrientationChanged(int orientation) {
		fingerUP = true;
		if (dockingTask != null) {
			// while to avoid potential synchronization problem
			while (!dockingTask.isCancelled()) {
				dockingTask.cancel(false);
			}
		}
		positionToNearest(orientation);
	}
}