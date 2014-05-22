package zhennan.yu.wordreminder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

import cn.sina.youxi.util.CV_Log;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.ViewFlipper;

public class TestingActivity extends Activity {
	static String TAG = "TestingActivity";
	private ViewFlipper mFlipper;
	WordView current_word1, current_word2;
	RelativeLayout l_btn_group, r_btn_group, word_page;
	int count, btn_width;
	Button l_stop_btn, r_stop_btn, l_know_btn, r_know_btn;
	// lrTranslateAnimation is animation that moves bingo and stop button group
	// from left to right
	// rlTranslateAnimation otherwise
	TranslateAnimation lrTranslateAnimation, rlTranslateAnimation;
	volatile boolean isAnimating = false;
	boolean isBottomMenuShown = false;

	// initial word1 size
	int initWord1Size;

	class WordModel {
		String word;
		String meaning;
	}

	// words1 keeps origin-order set of word
	// words keeps re-ordered set of word
	volatile ArrayList<WordModel> words, words1;
	String currentWord;
	boolean currentWordRemember;
	String lastBtnState;
	LinearLayout test_or_memorize;

	RefreshContentFilter refreshContentFilter;

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			CV_Log.i_f("back button pressed !");
			CV_Log.i_f("alwaysInMemoriseMode is " + alwaysInMemoriseMode);
			CV_Log.i_f("startPressed is " + startPressed);
			CV_Log.i_f("bingoPressed is " + bingoPressed);
			if (alwaysInMemoriseMode) {
				startpageNeedRefresh = false;
			} else {
				if ((mIndexItem.category.equals(Config.CATEGORY_REMEMBERED) || mIndexItem.category.equals(Config.CATEGORY_UNTESTED) || mIndexItem.category.equals(Config.CATEGORY_RANDOM)) && startPressed) {
					startpageNeedRefresh = true;
					dbNeedExported = true;
				} else if ((mIndexItem.category.equals(Config.CATEGORY_FORGOTTEN) && startPressed)) {
					if (bingoPressed) {
						// optimization: when the words you are testing are all forgotten, and bingo button is never pressed
						// main page need on refresh
						startpageNeedRefresh = true;
					}
					dbNeedExported = true;
				}
			}
			CV_Log.i_f("startpageNeedRefresh is " + startpageNeedRefresh);
			Intent resultIntent = new Intent();
			resultIntent.putExtra(Config.NEED_REFRESH, startpageNeedRefresh);
			resultIntent.putExtra(Config.NEED_EXPORTDB, dbNeedExported);
			resultIntent.putExtra(Config.REFRESH_CONTENT, refreshContentFilter);
			setResult(RESULT_OK, resultIntent);
			finish();
			CV_Log.i_f(TAG + " finished !");
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		// TODO Auto-generated method stub
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus) {
			btn_width = l_stop_btn.getWidth();

		}
	}

	// notify if startpage need to refresh itself
	boolean startpageNeedRefresh = false;
	boolean dbNeedExported = false;
	boolean alwaysInMemoriseMode = true;

	final int STARTFLIPPING = 1;
	final int SHOWBOTTOMMENU = 2;
	final int SHOWBINGOBUTTON = 3;

	Handler handler = new Handler(new Handler.Callback() {
		@Override
		public boolean handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
			case STARTFLIPPING:
				l_stop_btn.setText("stop");
				r_stop_btn.setText("stop");
				if (test_or_memorize.getTag().equals("want_to_memorize")) {
					if (!TextUtils.isEmpty(currentWord)) {
						DBManager.getInstance(TestingActivity.this).setTime(currentWord, "last_rem_time");
					}
				} else {
					if (!TextUtils.isEmpty(currentWord)) {
						// update test time
						DBManager.getInstance(TestingActivity.this).setTime(currentWord, "last_test_time");
						if (!bingoWords.contains(currentWord)) {
							// this means that currentWord is not remembered and
							// should
							// increase its difficulty
							DBManager.getInstance(TestingActivity.this).increaseDifficulty(currentWord);
						}
					}
					currentWordRemember = false;
				}
				mFlipper.startFlipping();
				break;
			case SHOWBOTTOMMENU:
				setVisibility(View.INVISIBLE);
				word_page.setFocusable(false);
				test_or_memorize.startAnimation(animation3);
				break;
			case SHOWBINGOBUTTON:
				// this will be executed only once
				reOrder();
				if (test_or_memorize.getTag().equals("want_to_memorize")) {
					showAppropriateBtnGroup(1);
				} else if (test_or_memorize.getTag().equals("want_to_test")) {
					showAppropriateBtnGroup(0);
				}
				word_page.setFocusable(true);
				isBottomMenuShown = false;
				break;
			default:
				break;
			}

			return false;
		}
	});

	/**
	 * after you press start, animation1 for the next word(right in) is
	 * executed, not animation2(left out) for current word
	 */
	View.OnClickListener stopStartClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			if (l_know_btn.getVisibility() == View.VISIBLE) {
				startPressed = true;
			}
			Button vButton = (Button) v;
			if (vButton.getText().equals(lastBtnState)) {
				return;
			}
			lastBtnState = vButton.getText().toString();
			if (vButton.getText().equals("start")) {
				if (mFlipper.isFlipping()) {
					return;
				}
				handler.sendEmptyMessageDelayed(STARTFLIPPING, 1000);
			} else {
				mFlipper.stopFlipping();
				l_stop_btn.setText("start");
				r_stop_btn.setText("start");
			}
		}
	};

	// a hashset that keep all the words that has been bingoed
	// is appended everytime you click bingo button
	HashSet<String> bingoWords;

	View.OnClickListener bingoClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			bingoPressed = true;
			String word;
			if (current_word1.getVisibility() == View.VISIBLE) {
				word = (String) current_word1.getWord();
			} else {
				word = (String) current_word2.getWord();
			}
			currentWordRemember = true;
			DBManager.getInstance(TestingActivity.this).decreaseDifficulty(word);
			bingoWords.add(word);
		}
	};

	void setEnabled(boolean clickable) {
		l_stop_btn.setEnabled(clickable);
		r_stop_btn.setEnabled(clickable);
		l_know_btn.setEnabled(clickable);
		r_know_btn.setEnabled(clickable);
	}

	void setVisibility(int visibility) {
		l_btn_group.setVisibility(visibility);
		r_btn_group.setVisibility(visibility);
	}

	void setBtnText(String text) {
		l_stop_btn.setText(text);
		r_stop_btn.setText(text);
	}

	IndexItem mIndexItem;
	boolean bingoPressed, startPressed;

	// right in left out animation for flipper when you start testing
	Animation animation1, animation2;
	// bottom menu animation, [memorize][test]
	// direction: up and down
	Animation animation3, animation4;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		cmb = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
		isBottomMenuShown = true;

		bingoWords = new HashSet<String>();
		setContentView(R.layout.testing_layout);
		count = 0;
		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		final int width = size.x;

		mIndexItem = (IndexItem) getIntent().getSerializableExtra("item");

		refreshContentFilter = new RefreshContentFilter(false);
		refreshContentFilter.addContent(new Content(mIndexItem.initialchar, mIndexItem.groupid));

		Cursor cursor = null;
		if (mIndexItem.category.equals(Config.CATEGORY_REMEMBERED)) {
			cursor = DBManager.getInstance(this).queryRememberByInitialCharacter(mIndexItem.initialchar, null, mIndexItem.groupid);
			prepareDatasource(cursor, Config.MAXCOUNTEACHTEST, Config.MAXCOUNTEACHTEST);
		} else if (mIndexItem.category.equals(Config.CATEGORY_FORGOTTEN)) {
			cursor = DBManager.getInstance(this).queryForgetByInitialCharacter(mIndexItem.initialchar, null, mIndexItem.groupid);
			prepareDatasource(cursor, Config.MAXCOUNTEACHTEST, Config.MAXCOUNTEACHTEST);
		} else if (mIndexItem.category.equals(Config.CATEGORY_UNTESTED)) {
			cursor = DBManager.getInstance(this).queryUntestedByInitialCharacter(mIndexItem.initialchar, null, mIndexItem.groupid);
			prepareDatasource(cursor, Config.MAXCOUNTEACHTEST, Config.MAXCOUNTEACHTEST);
		} else {
			cursor = DBManager.getInstance(this).queryRandomly();
			// because you should get all words with difficulty value > 0 out
			// to a array , so , 20000 is for that use
			prepareDatasourceForRandom(cursor, 20000, Config.MAXCOUNTEACHTEST);
		}

		initBottomTestOrMemorizeButton();

		initBingoStartButton(width);

		initFlipper();
	}

	CountDownLatch countDownLatch;

	/**
	 * reorder word sequence to for further test
	 */
	private void reOrder() {
		if (initWord1Size != words1.size()) {
			// when in here, means re-append operation is not over
			try {
				if (countDownLatch != null) {
					countDownLatch.await();
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		words.clear();
		Random r = new Random();
		int seed;
		int c = 0;
		while (words1.size() > 0) {
			c++;
			seed = r.nextInt(words1.size());
			words.add(words1.get(seed));
			words1.remove(seed);
			if (mIndexItem.category.equals(Config.CATEGORY_RANDOM) && c > Config.RANDOMTESTSIZE) {
				break;
			}
		}
		new Thread() {
			// re-append operation
			@Override
			public void run() {
				countDownLatch = new CountDownLatch(1);
				// re-append word to words1 for later use
				for (int i = 0; i < words.size(); i++) {
					words1.add(words.get(i));
				}
				countDownLatch.countDown();
			}
		}.start();
	}

	/**
	 * @param cursor
	 * @param words1Size
	 * @param wordsSize
	 */
	private void prepareDatasource(Cursor cursor, int words1Size, int wordsSize) {
		words1 = new ArrayList<WordModel>(words1Size);
		words = new ArrayList<WordModel>(wordsSize);
		try {
			while (cursor.moveToNext()) {
				WordModel wordModel = new WordModel();
				wordModel.word = cursor.getString(cursor.getColumnIndex("word"));
				wordModel.meaning = cursor.getString(cursor.getColumnIndex("meaning"));
				words1.add(wordModel);
			}
		} finally {
			// TODO: handle exception
			if (cursor != null) {
				cursor.close();
			}
			initWord1Size = words1.size();
		}
	}

	/**
	 * @param cursor
	 * @param words1Size
	 * @param wordsSize
	 */
	private void prepareDatasourceForRandom(Cursor cursor, int words1Size, int wordsSize) {
		words1 = new ArrayList<WordModel>(words1Size);
		words = new ArrayList<WordModel>(wordsSize);
		try {
			if (cursor.moveToFirst()) {
				do {
					WordModel wordModel = new WordModel();
					wordModel.word = cursor.getString(cursor.getColumnIndex("word"));
					wordModel.meaning = cursor.getString(cursor.getColumnIndex("meaning"));
					words1.add(wordModel);
				} while (cursor.moveToNext());
			}
		} finally {
			// TODO: handle exception
			if (cursor != null) {
				cursor.close();
			}
			initWord1Size = words1.size();
		}
	}

	private void initBottomTestOrMemorizeButton() {
		test_or_memorize = (LinearLayout) findViewById(R.id.test_or_memorize);
		android.view.View.OnClickListener onClickListener = new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				switch (v.getId()) {
				case R.id.want_to_memorize:
					test_or_memorize.setTag("want_to_memorize");
					break;
				case R.id.want_to_test:
					test_or_memorize.setTag("want_to_test");
					alwaysInMemoriseMode = false;
					break;
				default:
					break;
				}
				test_or_memorize.startAnimation(animation4);
			}
		};
		test_or_memorize.findViewById(R.id.want_to_test).setOnClickListener(onClickListener);
		test_or_memorize.findViewById(R.id.want_to_memorize).setOnClickListener(onClickListener);
		// up in animation ,to show up bottom menu
		animation3 = AnimationUtils.loadAnimation(this, R.anim.up_in);
		// down out animation, to hide bottom menu
		animation4 = AnimationUtils.loadAnimation(this, R.anim.down_out);
		animation3.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
				isBottomMenuShown = true;
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				test_or_memorize.setVisibility(View.VISIBLE);
				isBottomMenuShown = true;
			}
		});

		animation4.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				test_or_memorize.setVisibility(View.INVISIBLE);
				handler.sendEmptyMessageDelayed(SHOWBINGOBUTTON, 200);
			}
		});
		handler.sendEmptyMessageDelayed(SHOWBOTTOMMENU, 300);
	}

	SharedPreferences sharedPreferences;
	Editor editor;

	/**
	 * show up the memorise group button, include only start btn type 0 for test
	 * 1 for memorization
	 */
	private void showAppropriateBtnGroup(int type) {
		sharedPreferences = getSharedPreferences("position", Context.MODE_PRIVATE);
		editor = sharedPreferences.edit();
		// testbtn_gravity 0 for left , 1 for right
		if (sharedPreferences.getInt("testbtn_gravity", 0) == 0) {
			l_btn_group.setVisibility(View.VISIBLE);
			editor.putInt("testbtn_gravity", 0).commit();
		} else {
			r_btn_group.setVisibility(View.VISIBLE);
			editor.putInt("testbtn_gravity", 1).commit();
		}
		if (type == 1) {
			l_know_btn.setVisibility(View.GONE);
			r_know_btn.setVisibility(View.GONE);
		} else {
			l_know_btn.setVisibility(View.VISIBLE);
			r_know_btn.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * @param width
	 *            the width of the screen
	 */
	private void initBingoStartButton(final int width) {
		l_btn_group = ((RelativeLayout) this.findViewById(R.id.l_btn_group));
		l_stop_btn = (Button) l_btn_group.findViewById(R.id.l_stop_btn);
		l_stop_btn.setOnClickListener(stopStartClickListener);
		r_btn_group = ((RelativeLayout) this.findViewById(R.id.r_btn_group));
		r_stop_btn = (Button) r_btn_group.findViewById(R.id.r_stop_btn);
		r_stop_btn.setOnClickListener(stopStartClickListener);
		l_know_btn = (Button) l_btn_group.findViewById(R.id.l_know_btn);
		r_know_btn = (Button) r_btn_group.findViewById(R.id.r_know_btn);
		l_know_btn.setOnClickListener(bingoClickListener);
		r_know_btn.setOnClickListener(bingoClickListener);
		word_page = (RelativeLayout) this.findViewById(R.id.word_page);
		word_page.setFocusable(false);
		word_page.setOnTouchListener(new View.OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (isAnimating || isBottomMenuShown) {
					return true;
				}
				// TODO Auto-generated method stub
				if (event.getX() < (width / 2) && l_btn_group.getVisibility() != View.VISIBLE) {
					if (rlTranslateAnimation == null) {
						rlTranslateAnimation = new TranslateAnimation(0, -(width - btn_width), 0, 0);
						rlTranslateAnimation.setInterpolator(AnimationUtils.loadInterpolator(TestingActivity.this, android.R.anim.accelerate_decelerate_interpolator));
						rlTranslateAnimation.setDuration(400);
						rlTranslateAnimation.setAnimationListener(new AnimationListener() {

							@Override
							public void onAnimationStart(Animation animation) {
								// TODO Auto-generated method stub
								setEnabled(false);
								isAnimating = true;
							}

							@Override
							public void onAnimationRepeat(Animation animation) {
								// TODO Auto-generated method stub

							}

							@Override
							public void onAnimationEnd(Animation animation) {
								// TODO Auto-generated method stub
								setEnabled(true);
								isAnimating = false;
								l_btn_group.setVisibility(View.VISIBLE);
								r_btn_group.setVisibility(View.GONE);
								// remember where is the test btn
								// next time wont bother you to move manually
								editor.putInt("testbtn_gravity", 0).commit();
							}
						});
					}
					r_btn_group.startAnimation(rlTranslateAnimation);
				} else if (event.getX() >= (width / 2) && r_btn_group.getVisibility() != View.VISIBLE) {
					if (lrTranslateAnimation == null) {
						lrTranslateAnimation = new TranslateAnimation(0, width - btn_width, 0, 0);

						lrTranslateAnimation.setDuration(400);
						lrTranslateAnimation.setInterpolator(AnimationUtils.loadInterpolator(TestingActivity.this, android.R.anim.accelerate_decelerate_interpolator));
						lrTranslateAnimation.setAnimationListener(new AnimationListener() {

							@Override
							public void onAnimationStart(Animation animation) {
								// TODO Auto-generated method stub
								setEnabled(false);
								isAnimating = true;
							}

							@Override
							public void onAnimationRepeat(Animation animation) {
								// TODO Auto-generated method stub

							}

							@Override
							public void onAnimationEnd(Animation animation) {
								// TODO Auto-generated method stub
								setEnabled(true);
								isAnimating = false;
								l_btn_group.setVisibility(View.GONE);
								r_btn_group.setVisibility(View.VISIBLE);
								// remember where is the test btn
								// next time wont bother you to move manually
								editor.putInt("testbtn_gravity", 1).commit();
							}
						});
					}
					l_btn_group.startAnimation(lrTranslateAnimation);
				}
				return false;
			}
		});
	}

	ClipboardManager cmb;

	private void initFlipper() {
		mFlipper = ((ViewFlipper) this.findViewById(R.id.flipper));
		current_word1 = (WordView) mFlipper.findViewById(R.id.current_word1);
		current_word2 = (WordView) mFlipper.findViewById(R.id.current_word2);
		animation1 = AnimationUtils.loadAnimation(this, R.anim.push_left_in);
		animation2 = AnimationUtils.loadAnimation(this, R.anim.push_left_out);

		// listener when you click the word area
		OnClickListener onClickListener = new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mFlipper.isFlipping()) {
					return;
				}
				CharSequence clip = null;
				switch (v.getId()) {
				case R.id.current_word1:
					clip = current_word1.getWord();
					break;
				case R.id.current_word2:
					clip = current_word2.getWord();
					break;
				}
				int i = 0;
				for (; i < clip.length(); i++) {
					if (clip.charAt(i) == '\n') {
						clip = clip.subSequence(0, i);
						break;
					}
				}
				cmb.setPrimaryClip(ClipData.newPlainText("word", clip));
			}
		};

		current_word1.setOnClickListener(onClickListener);
		current_word2.setOnClickListener(onClickListener);

		animation1.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
				setEnabled(false);
				String word = null;
				String word_meaning = null;
				boolean over = false;
				if (count >= words.size()) {
					word = "";
					word_meaning = "";
					count = 0;
					Toast.makeText(TestingActivity.this, "all words in this category have been played", 1000).show();
					// test over, all words has been tested
					handler.sendEmptyMessageDelayed(SHOWBOTTOMMENU, 1000);
					bingoWords.clear();
					lastBtnState = null;
					setEnabled(true);
					mFlipper.stopFlipping();
					setBtnText("start");
					over = true;
					currentWordRemember = false;
				} else {
					// set word for memorization or test
					if (test_or_memorize.getTag().equals("want_to_memorize")) {
						word = words.get(count).word;
						word_meaning = words.get(count).meaning;
					} else if (test_or_memorize.getTag().equals("want_to_test")) {
						word = words.get(count).word;
						word_meaning = "";
					}
				}
				if (current_word1.getVisibility() == View.VISIBLE) {
					current_word1.setWord(word);
					current_word1.setWord_meaning(word_meaning);
				} else {
					current_word2.setWord(word);
					current_word2.setWord_meaning(word_meaning);
				}
				if (over) {
					setTitle("over !");
				} else {
					setTitle("counting down " + (words.size() - count));
					count++;
				}
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationEnd(Animation animation) {

				if (current_word1.getVisibility() == View.VISIBLE) {
					currentWord = (String) current_word1.getWord();
				} else {
					currentWord = (String) current_word2.getWord();
				}

				setEnabled(true);
			}
		});

		animation2.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
				setEnabled(false);
				if (!TextUtils.isEmpty(currentWord) && !currentWordRemember) {
					if (test_or_memorize.getTag().equals("want_to_test")) {
						DBManager.getInstance(TestingActivity.this).increaseDifficulty(currentWord);
					}
				}
				currentWordRemember = false;
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				setEnabled(true);
			}
		});

		mFlipper.setInAnimation(animation1);
		mFlipper.setOutAnimation(animation2);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();

		if (words1 != null) {
			words1.clear();
			words1 = null;
		}

		if (words != null) {
			words.clear();
			words = null;
		}

	}
}