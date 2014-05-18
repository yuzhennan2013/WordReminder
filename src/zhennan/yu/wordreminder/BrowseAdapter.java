package zhennan.yu.wordreminder;
import java.util.ArrayList;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.Keyframe;
import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

@SuppressLint("NewApi")
public class BrowseAdapter extends BaseAdapter {
	private LayoutInflater mInflater;
	ArrayList<Word> datasource;
	private LayoutTransition mTransitioner;
	private IndexItem mIndexItem;
	Context mContext;
	boolean startpageNeedRefresh;
	
	@SuppressLint("NewApi")
	public BrowseAdapter(Context context, ArrayList<Word> datasource,
			IndexItem indexItem) {
		// Cache the LayoutInflate to avoid asking for a new one each time.
		this.mContext = context;
		mInflater = LayoutInflater.from(context);
		this.datasource = datasource;
		mIndexItem = indexItem;
		mTransitioner = new LayoutTransition();
		mTransitioner.setStagger(LayoutTransition.CHANGE_APPEARING, 30);
		mTransitioner.setStagger(LayoutTransition.CHANGE_DISAPPEARING, 30);
		setupCustomAnimations();
		mTransitioner.setDuration(500);
	}

	@SuppressLint("NewApi")
	private void setupCustomAnimations() {
		// Changing while Adding
		PropertyValuesHolder pvhLeft = PropertyValuesHolder.ofInt("left", 0, 1);
		PropertyValuesHolder pvhTop = PropertyValuesHolder.ofInt("top", 0, 1);
		PropertyValuesHolder pvhRight = PropertyValuesHolder.ofInt("right", 0,
				1);
		PropertyValuesHolder pvhBottom = PropertyValuesHolder.ofInt("bottom",
				0, 1);
		PropertyValuesHolder pvhScaleX = PropertyValuesHolder.ofFloat("scaleX",
				1f, 0f, 1f);
		PropertyValuesHolder pvhScaleY = PropertyValuesHolder.ofFloat("scaleY",
				1f, 0f, 1f);
		final ObjectAnimator changeIn = ObjectAnimator.ofPropertyValuesHolder(
				this, pvhLeft, pvhTop, pvhRight, pvhBottom, pvhScaleX,
				pvhScaleY).setDuration(
				mTransitioner.getDuration(LayoutTransition.CHANGE_APPEARING));
		mTransitioner.setAnimator(LayoutTransition.CHANGE_APPEARING, changeIn);
		changeIn.addListener(new AnimatorListenerAdapter() {
			public void onAnimationEnd(Animator anim) {
				View view = (View) ((ObjectAnimator) anim).getTarget();
				view.setScaleX(1f);
				view.setScaleY(1f);
			}
		});

		// Changing while Removing
		Keyframe kf0 = Keyframe.ofFloat(0f, 0f);
		Keyframe kf1 = Keyframe.ofFloat(.9999f, 360f);
		Keyframe kf2 = Keyframe.ofFloat(1f, 0f);
		PropertyValuesHolder pvhRotation = PropertyValuesHolder.ofKeyframe(
				"rotation", kf0, kf1, kf2);
		final ObjectAnimator changeOut = ObjectAnimator
				.ofPropertyValuesHolder(this, pvhLeft, pvhTop, pvhRight,
						pvhBottom, pvhRotation)
				.setDuration(
						mTransitioner
								.getDuration(LayoutTransition.CHANGE_DISAPPEARING));
		mTransitioner.setAnimator(LayoutTransition.CHANGE_DISAPPEARING,
				changeOut);
		changeOut.addListener(new AnimatorListenerAdapter() {
			public void onAnimationEnd(Animator anim) {
				View view = (View) ((ObjectAnimator) anim).getTarget();
				view.setRotation(0f);
			}
		});

		// Adding
		ObjectAnimator animIn = ObjectAnimator.ofFloat(null, "rotationY", 90f,
				0f).setDuration(
				mTransitioner.getDuration(LayoutTransition.APPEARING));
		mTransitioner.setAnimator(LayoutTransition.APPEARING, animIn);
		animIn.addListener(new AnimatorListenerAdapter() {
			public void onAnimationEnd(Animator anim) {
				View view = (View) ((ObjectAnimator) anim).getTarget();
				view.setRotationY(0f);
			}
		});

		// Removing
		ObjectAnimator animOut = ObjectAnimator.ofFloat(null, "rotationX", 0f,
				90f).setDuration(
				mTransitioner.getDuration(LayoutTransition.DISAPPEARING));
		mTransitioner.setAnimator(LayoutTransition.DISAPPEARING, animOut);
		animOut.addListener(new AnimatorListenerAdapter() {
			public void onAnimationEnd(Animator anim) {
				View view = (View) ((ObjectAnimator) anim).getTarget();
				view.setRotationX(0f);
			}
		});

	}

	/**
	 * The number of items in the list is determined by the number of speeches
	 * in our array.
	 * 
	 * @see android.widget.ListAdapter#getCount()
	 */
	public int getCount() {
		return datasource.size();
	}

	/**
	 * Since the data comes from an array, just returning the index is sufficent
	 * to get at the data. If we were using a more complex data structure, we
	 * would return whatever object represents one row in the list.
	 * 
	 * @see android.widget.ListAdapter#getItem(int)
	 */
	public Object getItem(int position) {
		return position;
	}

	/**
	 * Use the array index as a unique id.
	 * 
	 * @see android.widget.ListAdapter#getItemId(int)
	 */
	public long getItemId(int position) {
		return position;
	}

	/**
	 * Make a view to hold each row.
	 * 
	 * @see android.widget.ListAdapter#getView(int, android.view.View,
	 *      android.view.ViewGroup)
	 */
	public View getView(final int position, View convertView, ViewGroup parent) {
		// A ViewHolder keeps references to children views to avoid unneccessary
		// calls
		// to findViewById() on each row.
		final ViewHolder holder;

		// When convertView is not null, we can reuse it directly, there is no
		// need
		// to reinflate it. We only inflate a new View when the convertView
		// supplied
		// by ListView is null.
		if (convertView == null) {
			convertView = mInflater
					.inflate(R.layout.browse_listview_item, null);
			// Creates a ViewHolder and store references to the two children
			// views
			// we want to bind data to.
			holder = new ViewHolder();
			holder.item_root = (RelativeLayout) convertView.findViewById(R.id.item_root);
			holder.word = (TextView) convertView.findViewById(R.id.word);
			holder.meaning = (TextView) convertView.findViewById(R.id.meaning);
			holder.difficulty = (TextView) convertView
					.findViewById(R.id.difficulty);
			holder.container = (LinearLayout) convertView
					.findViewById(R.id.auxiliary_button_group);
			holder.container.setLayoutTransition(mTransitioner);
			holder.button1 = (Button) holder.container
					.findViewById(R.id.auxiliary_button1);
			holder.button1.setFocusable(false);
			convertView.setTag(holder);
		} else {
			// Get the ViewHolder back to get fast access to the TextView
			// and the ImageView.
			holder = (ViewHolder) convertView.getTag();
		}
		
		// Bind the data efficiently with the holder.
		holder.word.setText(datasource.get(position).word);
		holder.meaning.setText(datasource.get(position).meaning);
		if (datasource.get(position).difficulty == -1
				|| datasource.get(position).difficulty == 0) {
			holder.difficulty.setText("");
		} else {
			holder.difficulty
					.setText(String.valueOf(datasource.get(position).difficulty));
		}

		if (datasource.get(position).expanded) {
			if (mIndexItem.category.equals(Config.CATEGORY_REMEMBERED)) {
				holder.button1.setVisibility(View.VISIBLE);
				holder.button1.setText("Forget !");
				holder.button1.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						startpageNeedRefresh = true;
						DBManager.getInstance(mContext).increaseDifficulty(datasource.get(position).word);
						datasource.remove(position);
						BrowseAdapter.this.notifyDataSetChanged();
					}
				});
			} else if (mIndexItem.category.equals(Config.CATEGORY_FORGOTTEN)) {
				holder.button1.setVisibility(View.VISIBLE);
				holder.button1.setText("Remember !");
				holder.button1.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						startpageNeedRefresh = true;
						DBManager.getInstance(mContext).setDifficulty(datasource.get(position).word, 0);
						datasource.remove(position);
						BrowseAdapter.this.notifyDataSetChanged();
					}
				});
			}
		} else {
			holder.button1.setVisibility(View.GONE);
		}
		
		return convertView;
	}
	
	static class ViewHolder {
		TextView word;
		TextView meaning;
		TextView difficulty;
		Button button1;
		RelativeLayout item_root;
		LinearLayout container;
	}
}