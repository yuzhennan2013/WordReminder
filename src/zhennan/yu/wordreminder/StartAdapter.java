package zhennan.yu.wordreminder;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

class StartAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    ArrayList<IndexItem> datasource;
    
    static int COLOR_REMEMBER;
    static int COLOR_FORGOTTEN;
    static int COLOR_UNTESTED;
    
    static{
        COLOR_REMEMBER = Color.rgb(114, 208, 13);
        COLOR_FORGOTTEN = Color.rgb(255, 23, 23);
        COLOR_UNTESTED = Color.rgb(255, 255, 255);
    }
    
    public StartAdapter(Context context, ArrayList<IndexItem> datasource) {
        // Cache the LayoutInflate to avoid asking for a new one each time.
        mInflater = LayoutInflater.from(context);
        this.datasource = datasource;
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
     * Since the data comes from an array, just returning the index is
     * sufficent to get at the data. If we were using a more complex data
     * structure, we would return whatever object represents one row in the
     * list.
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
    public View getView(int position, View convertView, ViewGroup parent) {
        // A ViewHolder keeps references to children views to avoid unneccessary calls
        // to findViewById() on each row.
        ViewHolder holder;

        // When convertView is not null, we can reuse it directly, there is no need
        // to reinflate it. We only inflate a new View when the convertView supplied
        // by ListView is null.
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.start_listview_item, null);

            // Creates a ViewHolder and store references to the two children views
            // we want to bind data to.
            holder = new ViewHolder();
            holder.text = (TextView) convertView.findViewById(R.id.text);

            convertView.setTag(holder);
        } else {
            // Get the ViewHolder back to get fast access to the TextView
            // and the ImageView.
            holder = (ViewHolder) convertView.getTag();
        }

        // Bind the data efficiently with the holder.
        holder.text.setText(datasource.get(position).toString());
        if (datasource.get(position).category.equals(Config.CATEGORY_FORGOTTEN)) {
        	holder.text.setTextColor(COLOR_FORGOTTEN);
		}
        else if(datasource.get(position).category.equals(Config.CATEGORY_REMEMBERED)){
        	holder.text.setTextColor(COLOR_REMEMBER);
		}
        else {
        	holder.text.setTextColor(COLOR_UNTESTED);
		}
        return convertView;
    }

    static class ViewHolder {
        TextView text;
    }
}