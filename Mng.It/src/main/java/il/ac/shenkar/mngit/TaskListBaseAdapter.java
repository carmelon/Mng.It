package il.ac.shenkar.mngit;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageButton;
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
            try {
                holder.description = (TextView) convertView.findViewById(R.id.description);
                holder.doneButton = (CheckBox) convertView.findViewById(R.id.doneButton);
                holder.doneButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int position = (Integer)v.getTag();
                        TaskDetails tempTask = db.getTask(position);
                        tempTask.setDone(((CheckBox)v).isChecked());
                        db.updateTask(position, tempTask);
                        notifyDataSetChanged();
                    }
                });
                holder.editButton = (ImageButton) convertView.findViewById(R.id.editButton);
                holder.editButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context, ShowTaskActivity.class);
                        intent.putExtra("POSITION", (Integer)v.getTag());
                        context.startActivity(intent);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                return convertView;
            }

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        if(holder != null)
        {
            TaskDetails tempTask = db.getTask(position);
            holder.description.setText(tempTask.getDescription());
            holder.editButton.setTag(position);
            holder.doneButton.setChecked(tempTask.getDone());
            holder.doneButton.setTag(position);
        }

        return convertView;
    }

    private static class ViewHolder {
        TextView description;
        CheckBox doneButton;
        ImageButton editButton;
    }
}
