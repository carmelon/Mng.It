package il.ac.shenkar.mngit;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

/**
 * Created by Ori on 1/10/14.
 */
public class DatabaseHandler extends SQLiteOpenHelper {
    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "TasksManager";

    // Contacts table name
    private static final String TABLE_TASKS = "Tasks";

    // Contacts Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_DESC = "description";
    private static final String KEY_LOC = "location";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_TASKS + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_DESC + " TEXT,"
                + KEY_LOC + " TEXT" + ")";
        db.execSQL(CREATE_CONTACTS_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TASKS);

        // Create tables again
        onCreate(db);
    }

    // Adding new task
    long addTask(TaskDetails task) {
        long resultId;
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_DESC, task.getDescription()); // Task Description
        values.put(KEY_LOC, task.getLocation()); // Task Location

        // Inserting Row
        resultId = db.insert(TABLE_TASKS, null, values);
        db.close(); // Closing database connection

        return resultId;
    }

    // Getting All Tasks
    public ArrayList<TaskDetails> getAllTasks() {
        ArrayList<TaskDetails> tasksList = new ArrayList<TaskDetails>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_TASKS;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                TaskDetails task = new TaskDetails(cursor.getString(1), cursor.getString(2));
                task.setId(Integer.parseInt(cursor.getString(0)));

                // Adding task to list
                tasksList.add(task);
            } while (cursor.moveToNext());
        }

        // return task list
        return tasksList;
    }

    // Deleting single task
    public void deleteTask(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_TASKS, KEY_ID + " = ?", new String[] { String.valueOf(id) });
        db.close();
    }
}
