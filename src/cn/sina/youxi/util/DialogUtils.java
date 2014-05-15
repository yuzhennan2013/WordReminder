package cn.sina.youxi.util;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;

/** 
 * 类说明：  对话框弹出帮助类
 * @author  Cundong
 * @date    Jan 1, 2012 9:27:27 AM 
 * @version 1.0
 */
public class DialogUtils {

	/**
	 * 弹出询问窗口
	 * @param
	 * @param
	 */
	public static void dialogBuilder(Context context, String title,
			String message, String positiveText, String negativeText,
			final DialogCallBack callBack) {
		AlertDialog.Builder builder = new Builder(context);
		builder.setMessage(message);
		builder.setTitle(title);

		// 确定
		builder.setPositiveButton(positiveText,
				new android.content.DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						callBack.onCompate();
					}
				});

		// 取消
		builder.setNegativeButton(negativeText,
				new android.content.DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						callBack.onCancel();
					}
				});

		builder.create().show();
	}

	/**
	 * 弹出询问窗口
	 * @param
	 * @param
	 */
	public static void dialogBuilder(Context context, String title,
			String message, final DialogCallBack callBack) {
		dialogBuilder(context, title, message, "确认", "取消", callBack);
	}

	public interface DialogCallBack {
		public void onCompate();

		public void onCancel();
	}
}