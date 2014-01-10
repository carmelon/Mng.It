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
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

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
import java.util.Locale;

/**
 * Created by Ori on 1/10/14.
 */
public class CreateTaskActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new CreateTaskFragment())
                    .commit();
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
     * A fragment containing the task creation.
     */
    public static class CreateTaskFragment extends Fragment implements
            GooglePlayServicesClient.ConnectionCallbacks,
            GooglePlayServicesClient.OnConnectionFailedListener{

        private final static int
                CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

        private EditText taskDesc;
        private Button createTaskButton;
        private EditText taskLoc;
        private LocationClient locationClient;
        private GoogleMap googleMap;
        private Marker marker;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_create, container, false);

            locationClient = new LocationClient(getActivity(), this, this);
            taskDesc = (EditText) rootView.findViewById(R.id.edit_message);
            taskLoc = (EditText) rootView.findViewById(R.id.edit_location);
            taskLoc.setText("");
            taskLoc.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        lookUp(taskLoc.getText().toString());
                    }
                    return false;
                }
            });
            createTaskButton = (Button) rootView.findViewById(R.id.createButton);
            createTaskButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String description = taskDesc.getText().toString();
                    String location = taskLoc.getText().toString();

                    setTask(new TaskDetails(description, location));
                    getActivity().finish();
                }
            });

            SupportMapFragment supportMapFragment =
                    (SupportMapFragment)getActivity().getSupportFragmentManager().findFragmentByTag("mapFragment");

            googleMap = supportMapFragment.getMap();
            if (googleMap != null) {
                //map available
                googleMap.setMyLocationEnabled(true);
            }

            return rootView;
        }

        @Override
        public void onStart() {
            super.onStart();
            // Connect the client.
            locationClient.connect();
        }

        @Override
        public void onStop() {
            // Disconnecting the client invalidates it.
            locationClient.disconnect();
            super.onStop();
        }

        @Override
        public void onConnected(Bundle bundle) {
            // Display the connection status
            Location location = locationClient.getLastLocation();
            if (location == null){
                return;
            }

            new GeocoderLocationTask().execute(location);
        }

        @Override
        public void onDisconnected() {
        }

        @Override
        public void onConnectionFailed(ConnectionResult connectionResult) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(getActivity(), CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        }

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

        public void setTask(TaskDetails task)
        {
            TaskListDB.getInstance(getActivity()).addTask(task);
        }

        /**
         * Lookup the address in input, format an output string and update map if possible
         */
        private void lookUp(String addressString) {
            new LookUpTask().execute(addressString);
        }

        /**
         * Display a marker on the map and reposition the camera according to location
         * @param latLng
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

                geoCoder = new Geocoder(getActivity(), new Locale("iw_IL"));
                try {
                    List<Address> addresses = geoCoder.getFromLocationName(params[0], 1);
                    if (addresses.size() >= 1) {
                        Address address = addresses.get(0);
                        LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());

                        return latLng;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return null;
            }

            @Override
            protected void onPostExecute(LatLng latLng) {
                super.onPostExecute(latLng);

                updateMap(latLng);
            }
        }

        private class GeocoderLocationTask extends AsyncTask<Location, Void, Address>
        {
            @Override
            protected Address doInBackground(Location... params) {
                Geocoder geoCoder;
                Location location;

                geoCoder = new Geocoder(getActivity(), new Locale("iw_IL"));
                location = params[0];
                try {
                    List<Address> addresses =
                            geoCoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                    if (addresses.size()>0){
                        return addresses.get(0);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Address address) {
                super.onPostExecute(address);

                taskLoc.setText(address.getAddressLine(0) + " " + address.getAddressLine(1));
                lookUp(address.getAddressLine(0) + " " + address.getAddressLine(1));
            }
        }
    }
}
