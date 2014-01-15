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

    /**
     * Remove task from the database and the local list.
     */
    public void removeTask(int position) {
        if(taskArray != null) {
            if(position >= 0 && position <= taskArray.size()) {
                try {
                    db.deleteTask(taskArray.get(position).getId());
                } catch (IndexOutOfBoundsException e) {
                    e.printStackTrace();
                }
                taskArray.remove(position);
            }
        }
    }

    /**
     * Add task to the database and the local list.
     * Return -1 on failure.
     */
    public long addTask(TaskDetails task) {
        /* Verify Parameters */
        if(task != null && taskArray != null) {

            /* Add to database */
            long id = db.addTask(task);

            /* If database didn't fail, add to local list */
            if(id != -1) {
                task.setId(id);
                taskArray.add(task);
            }

            return id;
        }
        else {
            return -1;
        }
    }

    /**
     * Get the task by position from the local task array.
     * Returns null on failure.
     */
    public TaskDetails getTask(int position) {
        if(taskArray != null) {
            if(position >= 0 && position <= taskArray.size()) {
                try {
                    return taskArray.get(position);
                } catch (IndexOutOfBoundsException e) {
                    e.printStackTrace();
                }
            }
        }

        return null;
    }

    /**
     * Modify a task in the local list and the database.
     */
    public void updateTask(int position, TaskDetails task) {
        if(taskArray != null && task != null) {
            if(position >= 0 && position <= taskArray.size()) {
                try {
                    taskArray.set(position, task);
                } catch (IndexOutOfBoundsException e) {
                    e.printStackTrace();
                    return;
                }
                db.updateTask(task);
            }
        }
    }

    /**
     * Get the number of tasks stored.
     */
    public int getSize() {
        if(taskArray != null) {
            return taskArray.size();
        }
        else {
            return 0;
        }
    }
}
