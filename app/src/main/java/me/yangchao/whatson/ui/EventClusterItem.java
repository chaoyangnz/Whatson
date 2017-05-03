package me.yangchao.whatson.ui;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

import me.yangchao.whatson.model.Event;

/**
 * Created by richard on 5/3/17.
 */

class EventClusterItem implements ClusterItem {
    private final LatLng mPosition;
    private Event event;

    public EventClusterItem(double lat, double lng) {
        mPosition = new LatLng(lat, lng);
    }

    public EventClusterItem(Event event, LatLng latLng) {
        event = event;
        mPosition = latLng;
    }

    @Override
    public LatLng getPosition() {
        return mPosition;
    }
}
