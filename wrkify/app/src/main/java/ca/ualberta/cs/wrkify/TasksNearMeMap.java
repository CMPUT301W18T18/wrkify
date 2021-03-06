/*
 * Copyright 2018 CMPUT301W18T18
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package ca.ualberta.cs.wrkify;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class TasksNearMeMap extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleMap.OnInfoWindowClickListener, GoogleMap.InfoWindowAdapter{

    private GoogleMap mMap;
    private CameraPosition cameraPosition;
    private LatLng defaultLocation = new LatLng(53.509,-113.5);
    private Location lastKnownLocation;
    private FusedLocationProviderClient mFusedLocationClient;
    private static final int DEFAULT_ZOOM = 14;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mLocationPermissionGranted;
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";
    private List<Task> tasksNearMe = new ArrayList<>();
    private HashMap<Marker,Task> markerTaskHashMap;
    private CardView taskCardView;

    /**
     * Override android standard onCreate, gets the last known location
     * of the device, and sets the google map map fragment
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            lastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            cameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }
        setContentView(R.layout.activity_maps);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     *Overridden android predefined
     * @param outState
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mMap != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, mMap.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, lastKnownLocation);
            super.onSaveInstanceState(outState);
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnInfoWindowClickListener(this);
        mMap.setInfoWindowAdapter(this);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation,DEFAULT_ZOOM));
        getLocationPermission();
        updateLocationUI();

        getCurrentLocation();

    }

//    public void viewMarker(Marker marker){
//        this.new MarkerInfo().execute(marker);
//    }
    /**
     * Prompts the user for permission to use the device location.
     */
    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    /**
     * Handles the result of the request for location permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
//        updateLocationUI();
    }

    /**
     * Gets the device's current location
     * if permission is not granted to get location
     * the location is assumed default.
     */
    public void getCurrentLocation(){
        try {
            if (mLocationPermissionGranted) {
                com.google.android.gms.tasks.Task<Location> locationResult = mFusedLocationClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull com.google.android.gms.tasks.Task<Location> task) {
                        lastKnownLocation = task.getResult();
                        if (task.isSuccessful()&&lastKnownLocation!=null) {

                            // Set the map's camera position to the current location of the devices
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(lastKnownLocation.getLatitude(),
                                            lastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                            searchTasksNearMe();

                        } else {
                            mMap.moveCamera(CameraUpdateFactory
                                    .newLatLngZoom(defaultLocation, DEFAULT_ZOOM));
                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
                            searchTasksNearMe();
                        }
                    }
                });
            }
            else{
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation,DEFAULT_ZOOM));
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                searchTasksNearMe();
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    /**
     * Updates the map's UI settings based on whether the user has granted location permission.
     */
    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                getLocationPermission();
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    /**
     * Calls execution of Async task SearchTask
     */
    private void searchTasksNearMe(){
       //Search for tasks by location
        try{
            TaskLocation taskLocation = new TaskLocation(lastKnownLocation.getLatitude(),lastKnownLocation.getLongitude());
            this.new SearchTask().execute(taskLocation);
        }
        catch (NullPointerException e){
            TaskLocation taskLocation = new TaskLocation(defaultLocation.latitude,defaultLocation.longitude);
            this.new SearchTask().execute(taskLocation);

        }
    }

    /**
     * Adds task markers for the list of tasks
     * within 25 km, given by search results.
     */
    private void addTaskMarkers(){
        markerTaskHashMap = new HashMap<>();
        for(Task task : tasksNearMe){
            TaskLocation location = task.getLocation();
            try {
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.setOnMarkerClickListener(this);
                markerTaskHashMap.put(mMap.addMarker(new MarkerOptions().position(latLng)),task);
            }
            catch (NullPointerException e){
                Log.d("Task location null--->","yes");
                continue;
            }

        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    /**
     * Starts the view task activity when the marker's task is clicked.
     * @param marker
     */
    @Override
    public void onInfoWindowClick(Marker marker) {
        Task task = markerTaskHashMap.get(marker);
        Intent intent = new Intent(this,ViewTaskActivity.class);
        intent.putExtra(ViewTaskActivity.EXTRA_TARGET_TASK,task);
        startActivity(intent);
    }

    /**
     * Implements Async task
     */
    private class SearchTask extends AsyncTask<TaskLocation, Void,List<Task>> {
        @Override
        protected List<Task> doInBackground(TaskLocation... locations) {
            if (locations.length != 1) {
                throw new IllegalArgumentException("exactly one query must be provided");
            }

            TaskLocation location = locations[0];

            List<Task> tasks;
            try {
                tasks = WrkifyClient.getInstance().getSearcher().findTasksNear(location);
            } catch (IOException e) {
                tasks = new ArrayList<>();
            }

            if (tasks == null) {
                tasks = new ArrayList<>();
            }

            return tasks;
        }

        /**
         * after the tasks have been fetched,
         * update the data set
         *
         * @param tasks the tasks of our search
         */
        @Override
        protected void onPostExecute(List<Task> tasks) {
            tasksNearMe.addAll(tasks);
            addTaskMarkers();
            Log.d("TASK LIST --->",Integer.valueOf(tasksNearMe.size()).toString());
        }
    }

    private class MarkerInfo extends AsyncTask<Marker,Void,CardView>{

        @Override
        protected CardView doInBackground(Marker... markers) {
            if (markers.length != 1) {
                throw new IllegalArgumentException("exactly one query must be provided");
            }

            Marker marker = markers[0];

            int layout = R.layout.taskcardview;
            View rootView = LayoutInflater.from(getApplicationContext()).inflate(layout,null,false);
            taskCardView = (CardView) rootView.findViewById(R.id.taskCardView);
            Task task = markerTaskHashMap.get(marker);
            TextView taskTitle = taskCardView.findViewById(R.id.taskTitle);
            taskTitle.setText(task.getTitle());
            TextView taskDescription = taskCardView.findViewById(R.id.taskDescription);
            taskDescription.setText(task.getDescription());
            TextView taskUser = taskCardView.findViewById(R.id.taskUser);
            taskUser.setText("User");
            try {
                taskUser.setText(task.getRemoteRequester(WrkifyClient.getInstance()).getUsername());
            } catch (IOException e){

            }
            StatusView taskStatus = taskCardView.findViewById(R.id.taskStatus);
            ArrayList<Bid> bids = task.getBidList();
            if(bids.size()>0) {
                taskStatus.setStatus(task.getStatus(), task.getBidList().get(0).getValue());
            }
            else{
                taskStatus.setStatus(task.getStatus(), new Price(0.0));
            }
            return taskCardView;
        }

        @Override
        protected void onPostExecute(CardView cv) {
            taskCardView = cv;
        }

    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        AsyncTask mi = this.new MarkerInfo().execute(marker);
        try {
            taskCardView = (CardView) mi.get();
        }
        catch(InterruptedException a){
            return null;
        }
        catch(ExecutionException e){
            return null;
        }
        return taskCardView;

    }
}
