package zhennan.yu.wordreminder;

import android.app.DialogFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;

public class MyDialogFragment extends DialogFragment{
	
        static String path;
        static MyDialogFragment newInstance(String path1) {
        	path = path1;
            MyDialogFragment f = new MyDialogFragment();
            return f;
        }
        
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
        	getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            View v = inflater.inflate(R.layout.fragment_layout, container, true);
            final EditText et = (EditText)v.findViewById(R.id.path);
            SharedPreferences sharedPreferences = getActivity().getSharedPreferences("path", Context.MODE_PRIVATE);
            if (path.equals("1")) {
            	et.setText(sharedPreferences.getString("BACKUPDBPATH", Environment.getExternalStorageDirectory() + "/" + DBHelper.DATABASE_NAME));
			}
            else {
            	et.setText(sharedPreferences.getString("SDWORDXMLPATH", Environment.getExternalStorageDirectory().getAbsolutePath() + "/word.xml"));
			}
            et.setSelectAllOnFocus(true);
            
            // Watch for button clicks.
            Button button = (Button)v.findViewById(R.id.confirm);
            button.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    if (TextUtils.isEmpty(et.getText())) {
                        Animation shake = AnimationUtils.loadAnimation(getActivity(), R.anim.shake);
                        et.startAnimation(shake);
                    	return;
					}
                    if (path.equals("1")) {
						Config.BACKUPDBPATH = et.getText().toString().trim();
						SharedPreferences sharedPreferences = getActivity().getSharedPreferences("path", Context.MODE_PRIVATE);
						Editor editor = sharedPreferences.edit();
						editor.putString("BACKUPDBPATH", Config.BACKUPDBPATH);
						editor.commit();//�ύ�޸�
					}
                    else {
                    	Config.SDWORDXMLPATH = et.getText().toString().trim();
                    	SharedPreferences sharedPreferences = getActivity().getSharedPreferences("path", Context.MODE_PRIVATE);
                		Editor editor = sharedPreferences.edit();//��ȡ�༭��
                		editor.putString("SDWORDXMLPATH", Config.SDWORDXMLPATH);
                		editor.commit();//�ύ�޸�
					}
                    dismiss();
                }
            });

            return v;
        }
}
