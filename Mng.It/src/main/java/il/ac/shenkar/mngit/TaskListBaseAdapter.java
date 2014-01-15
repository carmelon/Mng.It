package il.ac.shenkar.mngit;

import android.content.ActivityNotFoundException;
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
 * Adapter for the ListView in the Main activity.
 * Acts as the glue between the Model layer(Database) and the Controller layer(Activities).
 */
public class TaskListBaseAdapter extends BaseAdapter {
    private TaskListDB db;
    private LayoutInflater l_Inflater;
    private Context context;

    /**
     * Saves these variables since they are used repeatedly.
     */
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

    /**
     * Creates the sections of the list.
     * Initializes the UI objects with data from the Model and binds Controller code to them.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        /* Holder Pattern for performance boost when scrolling */
        ViewHolder holder;

        if (convertView == null) {
            convertView = l_Inflater.inflate(R.layout.task_details_view, null);
            if (convertView == null) {
                return null;
            }

            holder = new ViewHolder();
            holder.description = (TextView) convertView.findViewById(R.id.description);
            holder.doneButton = (CheckBox) convertView.findViewById(R.id.doneButton);
            if(holder.doneButton != null) {
                holder.doneButton.setOnClickListener(new View.OnClickListener() {
                    /**
                     * Update DB and the List when a checkbox is pressed.
                     */
                    @Override
                    public void onClick(View v) {
                        int position = (Integer)v.getTag(); //task position saved while initializing
                        TaskDetails tempTask = db.getTask(position);
                        if(tempTask != null) {
                            tempTask.setDone(((CheckBox)v).isChecked());
                            db.updateTask(position, tempTask);
                            notifyDataSetChanged(); //inform the list it was changed
                        }
                    }
                });
            }
            holder.editButton = (ImageButton) convertView.findViewById(R.id.editButton);
            if(holder.editButton != null) {
                holder.editButton.setOnClickListener(new View.OnClickListener() {
                    /**
                     * Start an edit task activity when the edit button is pressed.
                     */
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context, ShowTaskActivity.class);
                        intent.putExtra("POSITION", (Integer)v.getTag()); //pass the current position through the intent
                        try {
                            context.startActivity(intent);
                        } catch (ActivityNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }

            convertView.setTag(holder); //save the holder instead of wasting the object
        } else {
            holder = (ViewHolder) convertView.getTag(); //recycle the holder
        }
        if(holder != null) {
            /* Set the current state of object, save the position for later use in the listeners */
            TaskDetails tempTask = db.getTask(position);
            if(tempTask != null) {
                if(holder.description != null) {
                    holder.description.setText(tempTask.getDescription());
                }
                if(holder.editButton != null) {
                    holder.editButton.setTag(position);
                }
                if(holder.doneButton != null) {
                    holder.doneButton.setChecked(tempTask.getDone());
                    holder.doneButton.setTag(position);
                }
            }
        }

        return convertView;
    }

    /**
     * Holder for the ListView Adapter.
     */
    private static class ViewHolder {
        TextView description;
        CheckBox doneButton;
        ImageButton editButton;
    }
}
