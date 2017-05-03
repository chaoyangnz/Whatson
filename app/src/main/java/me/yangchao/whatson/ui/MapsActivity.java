package me.yangchao.whatson.ui;

import android.Manifest;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterManager;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

import me.yangchao.whatson.R;
import me.yangchao.whatson.model.Event;
import me.yangchao.whatson.net.EventApi;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;
import pl.charmas.android.reactivelocation.ReactiveLocationProvider;

@RuntimePermissions
public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    GoogleMap mMap;
    UiSettings uiSettings;
//    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;

    ReactiveLocationProvider locationProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        locationProvider = new ReactiveLocationProvider(this);


        // Create an instance of GoogleAPIClient.
//        if (mGoogleApiClient == null) {
//            mGoogleApiClient = new GoogleApiClient.Builder(this)
//                    .addConnectionCallbacks(this)
//                    .addOnConnectionFailedListener(this)
//                    .addApi(LocationServices.API)
//                    .build();
//        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        uiSettings = mMap.getUiSettings();
        uiSettings.setZoomControlsEnabled(true);
        mMap.setInfoWindowAdapter(new EventInfoWindow());

        // Initialize the manager with the context and the map.
        // (Activity extends context, so we can pass 'this' in the constructor.)
        mClusterManager = new ClusterManager<EventClusterItem>(this, mMap);

        // Point the map's listeners at the listeners implemented by the cluster
        // manager.
//        mMap.setOnCameraIdleListener(mClusterManager);
//        mMap.setOnMarkerClickListener(mClusterManager);

        MapsActivityPermissionsDispatcher.updateLocationWithCheck(this);
    }

    // Declare a variable for the cluster manager.
    private ClusterManager<EventClusterItem> mClusterManager;

    @NeedsPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    public void updateLocation() {
        mMap.setMyLocationEnabled(true);
//        uiSettings.setMyLocationButtonEnabled(true);
        locationProvider.getLastKnownLocation()
                .subscribe(location -> {
                    LatLng current = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(current, 15));

                    EventApi.getNearbyEvents(current, 500, "venue", events -> {
                        events.stream().collect(Collectors.groupingBy(event -> event.getLatLng())).entrySet().stream()
                        .forEach(e -> {
                            LatLng there = e.getKey();
                            List<Event> eventsThere = e.getValue();
                            for(int i = 0; i < eventsThere.size(); ++i) {
                                Event event = eventsThere.get(i);
                                mMap.addMarker(new MarkerOptions()
                                    .position(event.getLatLng(i))
                                    .title(event.getName()))
                                .setTag(event);

//                                EventClusterItem offsetItem = new EventClusterItem(event, event.getLatLng(i));
//                                mClusterManager.addItem(offsetItem);
                            }
                        });

                    });

                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // NOTE: delegate the permission handling to generated method
        MapsActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    class EventInfoWindow implements GoogleMap.InfoWindowAdapter {
        View view;

        public EventInfoWindow() {
            view = getLayoutInflater().inflate(R.layout.event_info_window, null);
        }
        @Override
        public View getInfoWindow(Marker marker) {
            return null;
        }

        @Override
        public View getInfoContents(Marker marker) {
            Event event = (Event) marker.getTag();
            TextView name = (TextView) view.findViewById(R.id.name);
            name.setText(event.getName());
            TextView description = (TextView) view.findViewById(R.id.description);
            description.setText(event.getDescription());
            ImageView coverPicture = (ImageView) view.findViewById(R.id.coverPicture);
            coverPicture.setImageURI(Uri.parse(event.getCoverPicture()));
            return view;
        }
    }
}
