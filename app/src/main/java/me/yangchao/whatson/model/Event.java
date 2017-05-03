
package me.yangchao.whatson.model;


import com.google.android.gms.maps.model.LatLng;

import java.util.Random;

public class Event {

    private String id;
    private String name;
    private String type;
    private String coverPicture;
    private String profilePicture;
    private String description;
    private String distance;
    private String startTime;
    private String endTime;
    private Integer timeFromNow;
    private String category;
    private me.yangchao.whatson.model.Stats stats;
    private me.yangchao.whatson.model.Venue venue;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCoverPicture() {
        return coverPicture;
    }

    public void setCoverPicture(String coverPicture) {
        this.coverPicture = coverPicture;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public Integer getTimeFromNow() {
        return timeFromNow;
    }

    public void setTimeFromNow(Integer timeFromNow) {
        this.timeFromNow = timeFromNow;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Stats getStats() {
        return stats;
    }

    public void setStats(Stats stats) {
        this.stats = stats;
    }

    public Venue getVenue() {
        return venue;
    }

    public void setVenue(Venue venue) {
        this.venue = venue;
    }

    public LatLng getLatLng() {
        Location location = venue.getLocation();
        return new LatLng(location.getLatitude(), location.getLongitude());
    }

    private final static double SHIFT_UNIT = 0.0001;
    public LatLng getLatLng(int shift) {
        if(shift == 0) return getLatLng();
        Location location = venue.getLocation();
        double random = new Random(shift).nextInt(10) * 0.00001;
        return new LatLng(location.getLatitude() + random, location.getLongitude() + random);
    }

}
