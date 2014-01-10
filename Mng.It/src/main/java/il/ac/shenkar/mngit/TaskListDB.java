package il.ac.shenkar.mngit;

import android.content.Context;

import java.util.ArrayList;

/**
 * Created by Ori on 1/10/14.
 */
public class TaskListDB {
    private static TaskListDB instance = null;
    private ArrayList<TaskDetails> taskArray;
    private DatabaseHandler db;

    private TaskListDB(Context context) {
        db = new DatabaseHandler(context);
        taskArray = db.getAllTasks();
    }

    public static synchronized TaskListDB getInstance(Context context) {
        if(instance == null) {
            instance = new TaskListDB(context);
        }

        return instance;
    }

    public void removeTask(int position) {
        db.deleteTask(taskArray.get(position).getId());
        taskArray.remove(position);
    }

    public long addTask(TaskDetails task) {
        task.setId(db.addTask(task));
        taskArray.add(task);

        return task.getId();
    }

    public TaskDetails getTask(int position) {
        return taskArray.get(position);
    }

    public int getSize() {
        return taskArray.size();
    }
}
