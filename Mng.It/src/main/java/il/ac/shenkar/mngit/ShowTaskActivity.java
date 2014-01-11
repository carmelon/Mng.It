package il.ac.shenkar.mngit;

import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Activity to show the task parameters and edit them.
 */
public class ShowTaskActivity extends ActionBarActivity {

    /**
     * Initialize the fragments.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.show_edit_container, new EditTaskFragment())
                    .add(R.id.show_location_container, new EditTaskLocFragment())
                    .commit();
        }
    }

    /**
     * Add the delete action to the Action Bar.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.task, menu);
        return true;
    }

    /**
     * Handle the delete action.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId())
        {
            /* Delete Task */
            case R.id.action_remove_task:
                /* Get Position from Activity Intent */
                int position = getIntent().getIntExtra("POSITION", -1);
                if(position != -1)
                {
                    /* Remove Task from the databse */
                    TaskListDB.getInstance(this).removeTask(position);
                }

                /* Terminate the Activity */
                finish();
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
     * A fragment containing the task title.
     */
    public static class EditTaskFragment extends Fragment {

        private EditText taskDesc;
        private Button editTaskButton;
        private int position;

        /**
         * Initialize UI objects for the Title fragment.
         */
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            /* Inflate the fragment's layout */
            View rootView = inflater.inflate(R.layout.fragment_edit, container, false);
            if(rootView == null)
            {
                return null;
            }

            /* Get the position of the task from the intent */
            position = getActivity().getIntent().getIntExtra("POSITION", -1);

            /* Initialize the UI Objecsts */
            taskDesc = (EditText) rootView.findViewById(R.id.show_edit_message);
            if(position != -1 && taskDesc != null)
            {
                TaskDetails currentTask = TaskListDB.getInstance(getActivity()).getTask(position);
                taskDesc.setText(currentTask.getDescription());
            }
            editTaskButton = (Button) rootView.findViewById(R.id.show_editButton);
            editTaskButton.setOnClickListener(new View.OnClickListener() {
                /**
                 * Update a task.
                 */
                @Override
                public void onClick(View v) {
                    Editable tempText;
                    String description, location;

                    /* Get the Text information */
                    EditText taskLoc = (EditText) getActivity().findViewById(R.id.show_edit_location);
                    if(taskLoc == null) {
                        location = "";
                    }
                    else {
                        tempText = taskLoc.getText();
                        if(tempText == null) {
                            location = "";
                        }
                        else {
                            location = tempText.toString();
                        }
                    }
                    if(taskDesc == null) {
                        description = "";
                    }
                    else {
                        tempText = taskDesc.getText();
                        if(tempText == null) {
                            description = "";
                        }
                        else {
                            description = tempText.toString();
                        }
                    }

                    /* Update the Task */
                    if(position != -1)
                    {
                        TaskDetails currentTask = TaskListDB.getInstance(getActivity()).getTask(position);
                        currentTask.setDescription(description);
                        currentTask.setLocation(location);
                        TaskListDB.getInstance(getActivity()).updateTask(position, currentTask);
                    }

                    /* Terminate the Activity */
                    getActivity().finish();
                }
            });

            return rootView;
        }
    }

    /**
     * A fragment to handle the location activities and information.
     */
    public static class EditTaskLocFragment extends Fragment {

        private GoogleMap googleMap;
        private Marker marker;
        private EditText taskLoc;

        /**
         * Initialize the location text box and map.
         */
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            /* Inflate the fragment's layout */
            View rootView = inflater.inflate(R.layout.fragment_show_loc, container, false);
            if(rootView == null) {
                return null;
            }

            /* Initialize the UI Objects */
            taskLoc = (EditText) rootView.findViewById(R.id.show_edit_location);
            taskLoc.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                /**
                 * Verify location and update the map after its entered in the text box.
                 */
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    Editable tempText;

                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        tempText = taskLoc.getText();
                        if(tempText != null) {
                            lookUp(tempText.toString());
                        }
                    }
                    return false;
                }
            });

             /* Initialize Google Map */
            SupportMapFragment supportMapFragment =
                    (SupportMapFragment)getActivity().getSupportFragmentManager().findFragmentByTag("mapFragment");
            googleMap = supportMapFragment.getMap();
            if (googleMap != null) {
                googleMap.setMyLocationEnabled(true); //map available
            }

            /* Initialize Text box and map according to the parameters of the current task */
            int position = getActivity().getIntent().getIntExtra("POSITION", -1); //position sent by intent
            if(position != -1)
            {
                TaskDetails currentTask = TaskListDB.getInstance(getActivity()).getTask(position);
                if(currentTask.getLocation() != null)
                {
                    taskLoc.setText(currentTask.getLocation());
                    lookUp(currentTask.getLocation());
                }
            }

            return rootView;
        }

        /**
         * Lookup the address with the Geocoder and update map if possible
         */
        private void lookUp(String addressString) {
            new LookUpTask().execute(addressString);
        }

        /**
         * Display a marker on the map and reposition the camera according to location.
         */
        private void updateMap(LatLng latLng){
            if (googleMap == null){
                return; //no play services
            }

            if (marker != null){
                marker.remove(); // remove old marker
            }

            /* Set Marker */
            marker = googleMap.addMarker(new MarkerOptions().position(latLng));

            /* Set Camera */
            CameraPosition newPosition = new CameraPosition.Builder()
                    .target(latLng)
                    .zoom(15)
                    .build();
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(newPosition));
        }

        /**
         * AsyncTask to run the Geocoder in the background to verify the address string.
         */
        private class LookUpTask extends AsyncTask<String, Void, LatLng>
        {
            @Override
            protected LatLng doInBackground(String... params) {
                Geocoder geoCoder = new Geocoder(getActivity(), new Locale("iw_IL"));
                try {
                    List<Address> addresses = geoCoder.getFromLocationName(params[0], 1);
                    if (addresses.size() >= 1) {
                        Address address = addresses.get(0);
                        return new LatLng(address.getLatitude(), address.getLongitude());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return null;
            }

            /**
             * If Geocoder was successful, update the map accordingly.
             */
            @Override
            protected void onPostExecute(LatLng latLng) {
                if(latLng != null)
                {
                    updateMap(latLng);
                }
                else
                {
                    Toast.makeText(getActivity(), "Location Failed!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
