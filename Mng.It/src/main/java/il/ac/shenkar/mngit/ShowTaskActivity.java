package il.ac.shenkar.mngit;

import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
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
 * Created by Ori on 1/11/14.
 */
public class ShowTaskActivity extends ActionBarActivity {

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.task, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch(item.getItemId())
        {
            case R.id.action_remove_task:
                int position = getIntent().getIntExtra("POSITION", -1);
                if(position != -1)
                {
                    TaskListDB.getInstance(this).removeTask(position);
                }
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        EasyTracker.getInstance(this).activityStart(this);
    }

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

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            View rootView = inflater.inflate(R.layout.fragment_edit, container, false);

            position = getActivity().getIntent().getIntExtra("POSITION", -1);
            try {
                taskDesc = (EditText) rootView.findViewById(R.id.show_edit_message);
                editTaskButton = (Button) rootView.findViewById(R.id.show_editButton);
            } catch (Exception e) {
                e.printStackTrace();
                return rootView;
            }
            if(position != -1)
            {
                TaskDetails currentTask = TaskListDB.getInstance(getActivity()).getTask(position);
                taskDesc.setText(currentTask.getDescription());
            }
            editTaskButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EditText taskLoc = (EditText) getActivity().findViewById(R.id.show_edit_location);

                    String description;
                    try {
                        description = taskDesc.getText().toString();
                    } catch (Exception e) {
                        e.printStackTrace();
                        description = "";
                    }

                    String location;
                    try {
                        location = taskLoc.getText().toString();
                    } catch (Exception e) {
                        e.printStackTrace();
                        location = "";
                    }

                    if(position != -1)
                    {
                        TaskDetails currentTask = TaskListDB.getInstance(getActivity()).getTask(position);
                        currentTask.setDescription(description);
                        currentTask.setLocation(location);
                        TaskListDB.getInstance(getActivity()).updateTask(position, currentTask);
                    }
                    getActivity().finish();
                }
            });

            return rootView;
        }
    }

    /**
     * A fragment containing the task location.
     */
    public static class EditTaskLocFragment extends Fragment {

        private GoogleMap googleMap;
        private Marker marker;
        private EditText taskLoc;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_show_loc, container, false);

            try {
                taskLoc = (EditText) rootView.findViewById(R.id.show_edit_location);
            } catch (Exception e) {
                e.printStackTrace();
                return rootView;
            }
            taskLoc.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        try {
                            lookUp(taskLoc.getText().toString());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    return false;
                }
            });

            SupportMapFragment supportMapFragment =
                    (SupportMapFragment)getActivity().getSupportFragmentManager().findFragmentByTag("mapFragment");

            googleMap = supportMapFragment.getMap();
            if (googleMap != null) {
                //map available
                googleMap.setMyLocationEnabled(true);
            }

            int position = getActivity().getIntent().getIntExtra("POSITION", -1);
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
         * Lookup the address in input and update map if possible
         */
        private void lookUp(String addressString) {
            new LookUpTask().execute(addressString);
        }

        /**
         * Display a marker on the map and reposition the camera according to location
         */
        private void updateMap(LatLng latLng){
            if (googleMap == null){
                return; //no play services
            }

            if (marker != null){
                marker.remove();
            }

            marker = googleMap.addMarker(new MarkerOptions().position(latLng));

            //reposition camera
            CameraPosition newPosition = new CameraPosition.Builder()
                    .target(latLng)
                    .zoom(15)
                    .build();
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(newPosition));
        }

        private class LookUpTask extends AsyncTask<String, Void, LatLng>
        {
            @Override
            protected LatLng doInBackground(String... params) {

                Geocoder geoCoder;
                try {
                    geoCoder = new Geocoder(getActivity(), new Locale("iw_IL"));
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }

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
