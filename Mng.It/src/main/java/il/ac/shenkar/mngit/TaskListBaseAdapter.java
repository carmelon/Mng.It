package il.ac.shenkar.mngit;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by Ori on 1/10/14.
 */
public class TaskListBaseAdapter extends BaseAdapter {

    private TaskListDB db;
    private LayoutInflater l_Inflater;
    private Context context;

    public TaskListBaseAdapter(Context context) {
        this.context = context;
        db = TaskListDB.getInstance(context);
        l_Inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return db.getSize();
    }

    @Override
    public Object getItem(int position) {
        return db.getTask(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;

        if (convertView == null) {
            convertView = l_Inflater.inflate(R.layout.task_details_view, null);
            holder = new ViewHolder();
            holder.description = (TextView) convertView.findViewById(R.id.description);
            holder.description.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //TODO: use v.getTag() to call appropriate task page activity
                }
            });
            holder.doneButton = (Button) convertView.findViewById(R.id.doneButton);
            holder.doneButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TaskListDB.getInstance(context).removeTask((Integer) v.getTag());
                    notifyDataSetChanged();
                }
            });
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.description.setText(db.getTask(position).getDescription());
        holder.description.setTag(position);
        holder.doneButton.setTag(position);

        return convertView;
    }

    private static class ViewHolder {
        TextView description;
        Button doneButton;
    }
}
