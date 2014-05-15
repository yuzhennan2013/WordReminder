package zhennan.yu.wordreminder;


public interface ScreenStateListener {
	
	public static int ORIENTATION_LANDSCAPE = 1;
	public static int ORIENTATION_PORTRAIT = 0;
	
	/**
	 * @param orientation 0 is portrait, 1 is landscape
	 */
	public void onOrientationChanged(int orientation);
}
