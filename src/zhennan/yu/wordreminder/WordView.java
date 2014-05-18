package zhennan.yu.wordreminder;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnTouchListener;
import android.widget.LinearLayout;
import android.widget.TextView;

public class WordView extends LinearLayout implements OnTouchListener{

	private int word_size;
	private int word_meaning_size;
	private int word_color;
	private int word_meaning_color;
	private TextView word_textView;
	private TextView word_meaning_textView;
	
	public WordView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		String word = null;
		String word_meaning = null;
		
		if (attrs != null) {
			TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.WordView, 0, 0);
			word_size = Math.max(1, a.getDimensionPixelSize(R.styleable.WordView_word_size, 1));
			word_meaning_size = Math.max(1, a.getDimensionPixelSize(R.styleable.WordView_word_meaning_size, 1));
			word_color = a.getColor(R.styleable.WordView_word_color, Color.WHITE);
			word_meaning_color = a.getColor(R.styleable.WordView_word_meaning_color, Color.WHITE);
			word = a.getString(R.styleable.WordView_word);
			word_meaning = a.getString(R.styleable.WordView_word_meaning);
			a.recycle();
		}
		
		word_textView = new TextView(context);
		word_textView.setText(word);
		word_textView.setTextSize(word_size);
		word_textView.setTextColor(word_color);
		word_textView.setGravity(Gravity.CENTER_HORIZONTAL);
		ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		word_textView.setLayoutParams(params);
		
		word_meaning_textView = new TextView(context);
		word_meaning_textView.setText(word_meaning);
		word_meaning_textView.setTextSize(word_meaning_size);
		word_meaning_textView.setTextColor(word_meaning_color);
		word_meaning_textView.setGravity(Gravity.CENTER_HORIZONTAL);
		word_meaning_textView.setPadding(0, 20, 0, 0);
		word_meaning_textView.setLayoutParams(params);
		
		setOrientation(LinearLayout.VERTICAL);
		
		addView(word_textView);
		addView(word_meaning_textView);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		return true;
	}

	public String getWord() {
		return word_textView.getText().toString();
	}

	public void setWord(String word) {
		word_textView.setText(word);
	}

	public String getWord_meaning() {
		return word_meaning_textView.getText().toString();
	}

	public void setWord_meaning(String word_meaning) {
		word_meaning_textView.setText(word_meaning);
	}
	
	
}
