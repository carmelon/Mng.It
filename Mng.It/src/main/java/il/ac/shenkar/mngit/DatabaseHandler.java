package il.ac.shenkar.mngit;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

/**
 * Wrapper class for the SQLite database functionality.
 */
public class DatabaseHandler extends SQLiteOpenHelper {
    // DB parameters:
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "TasksManager";
    private static final String TABLE_TASKS = "Tasks";
    // Tasks contents:
    private static final String KEY_ID = "id";
    private static final String KEY_DESC = "description";
    private static final String KEY_LOC = "location";
    private static final String KEY_DONE = "done";

    /**
     * Initialize the database.
     */
    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Create Tasks Table.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_TASKS + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_DESC + " TEXT,"
                + KEY_LOC + " TEXT," + KEY_DONE + " TEXT" + ")";
        try {
            db.execSQL(CREATE_CONTACTS_TABLE);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Refresh DB on version modification.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        try {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_TASKS);
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        // Create tables again
        onCreate(db);
    }

    /**
     * Create new Task.
     * Returns its id in the database, or -1 in case of failure.
     */
    long addTask(TaskDetails task) {
        long resultId;

        /* Check Parameter Validity */
        if(task == null) {
            return -1;
        }

        /* Get the database */
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
        } catch (SQLiteException e) {
            e.printStackTrace();
        }
        if(db == null) {
            return -1;
        }

        /* Format the task's parameters */
        ContentValues values = new ContentValues();
        values.put(KEY_DESC, task.getDescription());
        values.put(KEY_LOC, task.getLocation());
        values.put(KEY_DONE, String.valueOf(task.getDone()));

        /* Insert the data */
        resultId = db.insert(TABLE_TASKS, null, values);

        /* Close the connection */
        db.close();

        return resultId;
    }

    /**
     * Get the entire database table as an ArrayList.
     * Return the full list or null if failed.
     */
    public ArrayList<TaskDetails> getAllTasks() {
        ArrayList<TaskDetails> tasksList = new ArrayList<TaskDetails>();
        String selectQuery = "SELECT  * FROM " + TABLE_TASKS; //query for the entire table

        /* Get the database */
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
        } catch (SQLiteException e) {
            e.printStackTrace();
        }
        if(db == null) {
            return null;
        }

        /* Create a Cursor to cycle through the table */
        Cursor cursor = db.rawQuery(selectQuery, null);
        if(cursor == null) {
            return null;
        }

        if (cursor.moveToFirst()) {
            do {
                /* Get task information from each row and add to the list */
                TaskDetails task = new TaskDetails(cursor.getString(1), cursor.getString(2));
                task.setId(Integer.parseInt(cursor.getString(0)));
                task.setDone(Boolean.valueOf(cursor.getString(3)));

                tasksList.add(task);
            } while (cursor.moveToNext());
        }

        return tasksList;
    }

    /**
     * Update an existing task.
     */
    public void updateTask(TaskDetails task) {
        /* Check Parameter Validity */
        if(task == null) {
            return;
        }

        /* Get the database */
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
        } catch (SQLiteException e) {
            e.printStackTrace();
        }
        if(db == null) {
            return;
        }

        /* Format the task's parameters */
        ContentValues values = new ContentValues();
        values.put(KEY_DESC, task.getDescription());
        values.put(KEY_LOC, task.getLocation());
        values.put(KEY_DONE, String.valueOf(task.getDone()));

        /* Update the database */
        db.update(TABLE_TASKS, values, KEY_ID + " = ?", new String[]{String.valueOf(task.getId())});

        /* Close the connection */
        db.close();
    }

    /**
     * Delete an existing task from the table.
     */
    public void deleteTask(long id) {
        /* Get the database */
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
        } catch (SQLiteException e) {
            e.printStackTrace();
        }
        if(db == null) {
            return;
        }

        /* Delete the task at the given id */
        db.delete(TABLE_TASKS, KEY_ID + " = ?", new String[] { String.valueOf(id) });

        /* Close the connection */
        db.close();
    }
}
