package me.yangchao.whatson.net;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;

import java.lang.reflect.Type;
import java.util.List;
import java.util.function.Consumer;

import me.yangchao.whatson.App;
import me.yangchao.whatson.model.Event;

/**
 * Created by richard on 5/3/17.
 */

public class EventApi {

    public static final String EVENTS_API = "http://139.59.104.254:3000/events";

    public static void getNearbyEvents(LatLng latLng, int distance, String sort, Consumer<List<Event>> consumer) {
        String url = EVENTS_API + "?lat=" + latLng.latitude + "&lng="
                + latLng.longitude + "&distance=" + distance + "&sort=" + sort;
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
        response -> {
            try {
                Type type = new TypeToken<List<Event>>() {}.getType();
                Gson gson = new Gson();

                List<Event> events = gson.fromJson(response.getJSONArray("events").toString(), type);
                consumer.accept(events);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> {
            System.err.println(error);
        });
        int socketTimeout = 30_000; // 30 seconds. You can change it
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

        request.setRetryPolicy(policy);
        App.addRequest(request, "Events API");
    }
}
