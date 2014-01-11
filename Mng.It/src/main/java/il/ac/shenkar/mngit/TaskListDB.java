package il.ac.shenkar.mngit;

import android.content.Context;

import java.util.ArrayList;

/**
 * Database abstraction layer in the Model.
 * Using an ArrayList as a faster access medium to the data.
 */
public class TaskListDB {
    private static TaskListDB instance = null;
    private ArrayList<TaskDetails> taskArray;
    private DatabaseHandler db;

    /**
     * Singleton initialization - only one interface to the database.
     */
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

    public void updateTask(int position, TaskDetails task) {
        taskArray.set(position, task);
        db.updateTask(task);
    }

    public int getSize() {
        return taskArray.size();
    }
}
