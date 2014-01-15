package il.ac.shenkar.mngit;

import android.content.Intent;
import android.content.IntentSender;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

/**
 * Activity for the Task creation.
 */
public class CreateTaskActivity extends ActionBarActivity {

    /**
     * Initialize the fragments.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.create_container, new CreateTaskFragment())
                    .add(R.id.location_container, new LocationFragment())
                    .commit();
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
     * A fragment containing the task creation functionality.
     */
    public static class CreateTaskFragment extends Fragment {

        private EditText taskDesc;
        private Button createTaskButton;

        /**
         * Initialize the EditText and the Button functionalities.
         */
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            /* Verify Parameters */
            if(container == null || inflater == null) {
                return null;
            }

            /* Inflate the fragment's layout */
            View rootView = inflater.inflate(R.layout.fragment_create, container, false);
            if(rootView == null) {
                return null;
            }

            /* Initialize the UI Objects */
            taskDesc = (EditText) rootView.findViewById(R.id.edit_message);
            createTaskButton = (Button) rootView.findViewById(R.id.createButton);
            if(createTaskButton != null) {
                createTaskButton.setOnClickListener(new View.OnClickListener() {
                    /**
                     * Add a Task.
                     */
                    @Override
                    public void onClick(View v) {
                        Editable tempText;
                        String description, location;

                        /* Get the Text information */
                        EditText taskLoc = (EditText) getActivity().findViewById(R.id.edit_location);
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

                        /* Add the task to the database */
                        TaskListDB.getInstance(getActivity()).addTask(new TaskDetails(description, location));

                        /* Terminate the activity */
                        getActivity().finish();
                    }
                });
            }

            return rootView;
        }
    }

    /**
     * A fragment to handle the location activities and information.
     */
    public static class LocationFragment extends Fragment implements
            GooglePlayServicesClient.ConnectionCallbacks,
            GooglePlayServicesClient.OnConnectionFailedListener{

        private LocationClient locationClient;
        private GoogleMap googleMap;
        private Marker marker;
        private EditText taskLoc;
        private final static int
                CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000; //arbitrary error code

        /**
         * Initialize the location text box, map and the location process.
         */
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            /* Verify Parameters */
            if(container == null || inflater == null) {
                return null;
            }

            /* Inflate the fragment's layout */
            View rootView = inflater.inflate(R.layout.fragment_location, container, false);
            if(rootView == null) {
                return null;
            }

            /* Initialize the location client */
            locationClient = new LocationClient(getActivity(), this, this);

            /* Initialize the UI objects */
            taskLoc = (EditText) rootView.findViewById(R.id.edit_location);
            if(taskLoc != null) {
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
            }

            /* Initialize Google Map */
            SupportMapFragment supportMapFragment =
                    (SupportMapFragment)getActivity().getSupportFragmentManager().findFragmentByTag("mapFragment");
            googleMap = supportMapFragment.getMap();
            if (googleMap != null) {
                googleMap.setMyLocationEnabled(true); //map available
            }

            return rootView;
        }

        /**
         * Connect the LocationClient.
         */
        @Override
        public void onStart() {
            super.onStart();
            locationClient.connect();
        }

        /**
         * Terminate the LocationClient.
         */
        @Override
        public void onStop() {
            locationClient.disconnect();
            super.onStop();
        }

        /**
         * Perform the connection logic in an AsyncTask for performance.
         */
        @Override
        public void onConnected(Bundle bundle) {
            try {
                new GeocoderLocationTask().execute();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onDisconnected() {
        }

        /**
         * Start an Activity that tries to resolve the error.
         */
        @Override
        public void onConnectionFailed(ConnectionResult connectionResult) {
            if(connectionResult != null) {
                try {
                    connectionResult.startResolutionForResult(getActivity(), CONNECTION_FAILURE_RESOLUTION_REQUEST);
                } catch (IntentSender.SendIntentException e) {
                    e.printStackTrace();
                }
            }
        }

        /**
         * Error resolving callback.
         */
        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            switch (requestCode){
                case CONNECTION_FAILURE_RESOLUTION_REQUEST:
                    switch (resultCode){
                        case RESULT_OK:
                            locationClient.connect();
                            break;
                        default:
                    }
                    break;
            }
        }

        /**
         * Lookup the address with the Geocoder and update map if possible
         */
        private void lookUp(String addressString) {
            /* Verify Parameters */
            if(addressString == null) {
                return;
            }

            try {
                new LookUpTask().execute(addressString);
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }

        /**
         * Display a marker on the map and reposition the camera according to location.
         */
        private void updateMap(LatLng latLng){
            /* Verify Parameters */
            if (googleMap == null || latLng == null){
                return;
            }

            /* Remove Old Marker */
            if (marker != null){
                marker.remove();
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
                /* Verify Parameters */
                if(params == null || params[0] == null) {
                    return null;
                }

                /* Get GeoCode Address by the given string */
                Geocoder geoCoder = new Geocoder(getActivity());
                try {
                    List<Address> addresses = geoCoder.getFromLocationName(params[0], 1);
                    if (addresses != null && addresses.size() >= 1) {
                        Address address = addresses.get(0);
                        return new LatLng(address.getLatitude(), address.getLongitude());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                } catch (IndexOutOfBoundsException e) {
                    e.printStackTrace();
                }

                return null;
            }

            /**
             * If Geocoder was successful, update the map accordingly.
             */
            @Override
            protected void onPostExecute(LatLng latLng) {
                /* Verify Parameters */
                if(latLng != null) {
                    updateMap(latLng);
                }
                else {
                    Toast.makeText(getActivity(), "Location Failed!", Toast.LENGTH_SHORT).show();
                }
            }
        }

        /**
         * AsyncTask to handle the LocationClient connection flow.
         */
        private class GeocoderLocationTask extends AsyncTask<Void, Void, Address>
        {
            /**
             * Poll for location and decode it to address.
             */
            @Override
            protected Address doInBackground(Void... params) {
                Location location = null;
                int retries = 20;

                /* Poll for a good location return */
                while(retries > 0 && location == null) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    location = locationClient.getLastLocation();
                    retries--;
                }
                if (location == null){
                    return null;
                }

                /* Decode location to an address */
                Geocoder geoCoder = new Geocoder(getActivity());
                try {
                    List<Address> addresses =
                            geoCoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                    if (addresses != null && addresses.size() > 0){
                        return addresses.get(0);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (IndexOutOfBoundsException e) {
                    e.printStackTrace();
                }
                return null;
            }

            /**
             * If location decoding was successful, set the text box and update the map.
             */
            @Override
            protected void onPostExecute(Address address) {
                /* Verify Parameters */
                if(address != null) {
                    try {
                        if(taskLoc != null) {
                            taskLoc.setText(address.getAddressLine(0) + " " + address.getAddressLine(1));
                            lookUp(address.getAddressLine(0) + " " + address.getAddressLine(1));
                        }
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                        }
                }
                else {
                    Toast.makeText(getActivity(), "Location Failed!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
