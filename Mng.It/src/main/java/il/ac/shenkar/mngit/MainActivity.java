package il.ac.shenkar.mngit;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.google.analytics.tracking.android.EasyTracker;

/**
 * - Entry point to the application.
 * - Contains a list of tracked tasks.
 * - Can enter the CreateTaskActivity from the Action Bar.
 * - Can enter the ShowTaskActivity from the edit button of a specific task in the list.
 */
public class MainActivity extends ActionBarActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new TaskListFragment())
                    .commit();
        }
    }

    /**
     * Initialize the Action Bar action.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    /**
     * Callback when selecting an action in the Action Bar.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId())
        {
            /* Call CreateTaskActivity */
            case R.id.action_add_task:
                Intent intent = new Intent(this, CreateTaskActivity.class);
                try {
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    e.printStackTrace();
                    return false;
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Initialize Google Analytics.
     */
    @Override
    protected void onStart() {
        super.onStart();
        EasyTracker.getInstance(this).activityStart(this);
    }

    /**
     * Terminate Google Analytics.
     */
    @Override
    protected void onStop() {
        super.onStop();
        EasyTracker.getInstance(this).activityStop(this);
    }

    /**
     * A UI fragment containing the task list.
     */
    public static class TaskListFragment extends Fragment {
        private ActionBarActivity activity;
        private ListView listView;
        private TaskListBaseAdapter adapter;

        /**
         * Update current activity parameter.
         */
        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            this.activity = (ActionBarActivity) activity;
        }

        /**
         * Initializes the ListView object and binds it to an adapter.
         */
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            /* Verify Parameters */
            if(container == null || inflater == null || activity == null) {
                return null;
            }

            /* Inflate the fragment's layout */
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            if(rootView == null) {
                return null;
            }

            /* Initialize Components */
            adapter = new TaskListBaseAdapter(activity);
            listView = (ListView) rootView.findViewById(R.id.listV_main);
            if(listView != null) {
                /* Bind the Adapter to the List */
                listView.setAdapter(adapter);
            }

            return rootView;
        }

        /**
         * Ask Android to always refresh the list when returning. (countering any possible modifications)
         */
        @Override
        public void onResume() {
            super.onResume();
            adapter.notifyDataSetChanged();
        }
    }
}
