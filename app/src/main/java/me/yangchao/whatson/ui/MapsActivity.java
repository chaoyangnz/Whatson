package me.yangchao.whatson.ui;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.w3c.dom.Document;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.yangchao.whatson.R;
import me.yangchao.whatson.model.Event;
import me.yangchao.whatson.net.EventApi;
import me.yangchao.whatson.net.GMapV2Direction;
import me.yangchao.whatson.net.GMapV2DirectionAsyncTask;
import me.yangchao.whatson.util.MessageFormaterUtil;
import me.yangchao.whatson.util.MetricsUtil;
import me.yangchao.whatson.util.StringUtil;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;
import pl.charmas.android.reactivelocation.ReactiveLocationProvider;
import rx.Subscription;
import us.feras.mdv.MarkdownView;

import static android.support.design.widget.BottomSheetBehavior.STATE_EXPANDED;
import static android.support.design.widget.BottomSheetBehavior.STATE_HIDDEN;
import static android.view.View.VISIBLE;

@RuntimePermissions
public class MapsActivity extends BaseActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    GoogleMap googleMap;
    UiSettings uiSettings;
    // location provider
    ReactiveLocationProvider locationProvider;
    Subscription locationUpdated;

    @BindView(R.id.bottom_sheet)
    View vBottomSheet;
    BottomSheetBehavior<View> bottomSheetBehavior;

    @BindView(R.id.event_name)
    TextView vEventName;
    @BindView(R.id.event_description)
    TextView vEventDescription;
    @BindView(R.id.event_venue)
    TextView vEventVenue;
    @BindView(R.id.event_cover_picture)
    ImageView vEventCoverPicture;
    @BindView(R.id.event_time)
    TextView vEventTime;
    @BindView(R.id.event_distance)
    TextView vEventDistance;
    @BindView(R.id.event_walk)
    TextView vEventWalk;
    @BindView(R.id.event_stats)
    TextView vEventStats;
    @BindView(R.id.route_fab)
    FloatingActionButton routeFAB;

    Event currentEvent;
    LatLng currentLocation;
    Polyline currentRoute;

    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        ButterKnife.bind(this);

        sharedPreferences = getPreferences(Context.MODE_PRIVATE);

        addToolbar(true);

        // Google Map fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // location provider
        locationProvider = new ReactiveLocationProvider(this);

        // init the bottom sheet behavior
        bottomSheetBehavior = BottomSheetBehavior.from(vBottomSheet);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if(newState != STATE_EXPANDED) {
                    vEventCoverPicture.setVisibility(View.GONE);
                }
                if(newState == STATE_HIDDEN) {
                    routeFAB.hide();
                } else {
                    routeFAB.show();
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                if(slideOffset > .75f) {
                    vEventCoverPicture.setVisibility(VISIBLE);
                } else {
                    vEventCoverPicture.setVisibility(View.GONE);
                }
            }
        });

        // FAB
        routeFAB.hide();
        routeFAB.setOnClickListener(v -> {
            if(currentEvent != null && currentLocation != null) {
                drawRoute(currentLocation, currentEvent.getLatLng());
            }
        });
    }

    private void drawRoute(LatLng sourcePosition, LatLng destPosition) {
        if(currentRoute != null) currentRoute.remove();
        final Handler handler = new Handler() {
            public void handleMessage(Message msg) {
                try {
                    Document doc = (Document) msg.obj;
                    GMapV2Direction md = new GMapV2Direction();
                    List<LatLng> directionPoints = md.getDirection(doc);
                    PolylineOptions rectLine = new PolylineOptions().
                            pattern(Arrays.asList(new Dot())).
                            width(20).
                            color(Color.DKGRAY);

                    for (LatLng directionPoint : directionPoints) {
                        rectLine.add(directionPoint);
                    }
                    currentRoute = googleMap.addPolyline(rectLine);
                    md.getDurationText(doc);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        new GMapV2DirectionAsyncTask(handler, sourcePosition, destPosition, GMapV2Direction.MODE_WALKING).execute();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        if(locationUpdated != null) {
            locationUpdated.unsubscribe();
        }
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
        this.googleMap = googleMap;
        googleMap.setPadding(0, MetricsUtil.dpToPx(this, 80), 0, MetricsUtil.dpToPx(this, 100));
        uiSettings = this.googleMap.getUiSettings();
        uiSettings.setZoomControlsEnabled(true);
        this.googleMap.setOnMarkerClickListener(this);

        MapsActivityPermissionsDispatcher.updateLocationWithCheck(this);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {

        Event event = (Event) marker.getTag();
        currentEvent = event;

        // update UI
        vEventName.setText(StringUtil.trim(event.getName(), 30));
        vEventDescription.setText(event.getDescription());
        vEventVenue.setText(event.getVenue().getName());
        Glide.with(this)
                .load(Uri.parse(event.getCoverPicture()))
//                .signature(new StringSignature(String.valueOf(System.currentTimeMillis())))
                .into(vEventCoverPicture);

        vEventTime.setText(MessageFormaterUtil.timeRange(event.getStartTime(), event.getEndTime()));
        vEventDistance.setText(MessageFormaterUtil.distance(event.getDistance()));
        vEventWalk.setText(MessageFormaterUtil.walkTime(event.getDistance()));
        vEventStats.setText(MessageFormaterUtil.stats(event.getStats()));

        // show bottom sheet
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        return true;
    }

    @NeedsPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    @SuppressWarnings({"MissingPermission"})
    public void updateLocation() {
        googleMap.setMyLocationEnabled(true);

        LocationRequest request = LocationRequest.create() //standard GMS LocationRequest
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
//                .setNumUpdates(5)
                .setInterval(30_000);

        locationProvider = new ReactiveLocationProvider(this);
        if(locationUpdated != null) {
            locationUpdated.unsubscribe();
        }
        locationUpdated = locationProvider.getUpdatedLocation(request).subscribe(location -> {
            Log.d("Location update", location.toString());
            currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
            refreshEvents();
        });
    }

    private void refreshEvents() {

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));

        EventApi.getNearbyEvents(currentLocation, 100*sharedPreferences.getInt(getString(R.string.preference_distance), 5), "venue", events -> {
            events.stream().collect(Collectors.groupingBy(event -> event.getLatLng())).entrySet().stream()
                    .forEach(e -> {
                        LatLng there = e.getKey();
                        List<Event> eventsThere = e.getValue();
                        for(int i = 0; i < eventsThere.size(); ++i) {
                            Event event = eventsThere.get(i);
                            googleMap.addMarker(new MarkerOptions()
                                    .position(event.getLatLng(i))
                                    .title(event.getName()))
                                    .setTag(event);
                        }
                    });

        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // NOTE: delegate the permission handling to generated method
        MapsActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // don't show option menu when searching
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        View sheetView = null;
        BottomSheetDialog mBottomSheetDialog = null;
        switch (item.getItemId()) {
            // settings bottom sheet
            case R.id.action_settings:
                sheetView = getLayoutInflater().inflate(R.layout.settings_bottom_sheet, null);
                TextView vDistance = (TextView) sheetView.findViewById(R.id.distance);
                SeekBar vDistanceChooser = (SeekBar) sheetView.findViewById(R.id.distance_chooser);

                int distance = sharedPreferences.getInt(getString(R.string.preference_distance), 5);
                vDistance.setText(distance * 100 + " metres");

                vDistanceChooser.setMax(25);
                vDistanceChooser.incrementProgressBy(1);
                vDistanceChooser.setProgress(distance);
                vDistanceChooser.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                        sharedPreferences.edit()
                                .putInt(getString(R.string.preference_distance), progress)
                                .commit();

                        vDistance.setText(progress * 100 + " metres");
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });
                Switch vClusterMarker = (Switch) sheetView.findViewById(R.id.cluster_marker);
                vClusterMarker.setChecked(false);

                mBottomSheetDialog = new BottomSheetDialog(this);
                mBottomSheetDialog.setContentView(sheetView);
                mBottomSheetDialog.show();
                return true;
            case R.id.action_refresh:
                if(currentLocation != null) {
                    refreshEvents();
                } else {
                    MapsActivityPermissionsDispatcher.updateLocationWithCheck(this);
                }
                return true;
            case R.id.action_help:
                sheetView = getLayoutInflater().inflate(R.layout.help_bottom_sheet, null);
                MarkdownView markdownView = (MarkdownView) sheetView.findViewById(R.id.help_markdownView);
                markdownView.loadMarkdownFile("file:///android_asset/README.md", "file:///android_asset/markdown.css");
                mBottomSheetDialog = new BottomSheetDialog(this);
                mBottomSheetDialog.setContentView(sheetView);
                mBottomSheetDialog.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
