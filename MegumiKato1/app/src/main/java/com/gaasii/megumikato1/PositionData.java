package com.gaasii.megumikato1;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by negi on 2017/08/03.
 */

public class PositionData {

    private LatLng lat = new LatLng(0, 0);
    private String samples = new String("aaa");
    private MarkerOptions recentMarkerOptions = new MarkerOptions().position(lat).title(samples);

    private Marker recentMarker;

    private GoogleMap mMap;

    PositionData(GoogleMap mMap){
        this.mMap = mMap;
    }


    public Marker getRecentMarker(){
        return this.recentMarker;
    }

    public void setRecentMarker(Marker recentMarker){
        this.recentMarker = recentMarker;
    }

    public MarkerOptions getRecentMarkerOptions(){
        return this.recentMarkerOptions;
    }

    public void setRecentMarkerOptions(MarkerOptions recentMarkerOptions){
        this.recentMarkerOptions = recentMarkerOptions;

        this.recentMarker.setAlpha(0);
    }
}
