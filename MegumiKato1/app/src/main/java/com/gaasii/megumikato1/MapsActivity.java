package com.gaasii.megumikato1;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.model.Leg;
import com.akexorcist.googledirection.model.Route;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static android.R.attr.direction;

public class MapsActivity extends AppCompatActivity
        implements
        GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMapClickListener,
        OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback{


    private int GETHZ = 10000;
    private final int COUNTMAX = 10000000;

    private GoogleMap mMap;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private boolean mPermissionDenied = false;
    public Marker recentMarker;
    private Timer t = new Timer();

    //位置データのArrayList
    private List<PositionData> pDatas = new ArrayList<PositionData>();


    final Handler handler = new Handler();//タイマーで使用


    private LocationManager mLocationManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        // GPS
        mLocationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        boolean gpsFlg = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        Log.d("GPS Enabled", gpsFlg?"OK":"NG");



        TimerTask task = new TimerTask() {
            int count = 0;
            @Override
            public void run() {
                // Timerのスレッド
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        // UIスレッド
                        if (count > COUNTMAX) { // 5回実行したら終了
                            cancel();
                        }
                        getPosition();
                        count++;

                    }
                });
            }
        };

        t.scheduleAtFixedRate(task, 0, GETHZ);
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

        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMapClickListener(this);
        enableMyLocation();

        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else {
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mPermissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            mPermissionDenied = false;
        }
    }


    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }

    @Override
    public void onMapClick(LatLng p){
        //Toast.makeText(this, "Map clicked", Toast.LENGTH_SHORT).show();
        //LatLng hyogo = new LatLng(35, 135);
        //mMap.addMarker(new MarkerOptions().position(hyogo).title("Marker in hyogo"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(hyogo));


    }

    public void drawline(Direction direction, String rawBody){
        String status = direction.getStatus();
        Log.d("Direction", status);
        Route route = direction.getRouteList().get(0);
        Leg leg = route.getLegList().get(0);
        ArrayList<LatLng> directionPositionList = leg.getDirectionPoint();
        PolylineOptions polylineOptions = DirectionConverter.createPolyline(this, directionPositionList, 5, Color.RED);
        mMap.addPolyline(polylineOptions);
    }

    public void setRecentMarker(Marker recentMarker){
        this.recentMarker = recentMarker;
    }

    public void getPosition(){
        HttpGetTask task = new HttpGetTask(this, mMap, pDatas);
        task.execute();
    }

    public void DirecCallBack(){

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //main.xmlの内容を読み込む
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.map_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d("Menu Item Selected", item.toString());

        switch (item.getItemId()) {
            case R.id.item1:
                GETHZ = 1000;
                this.t.cancel();

                TimerTask task = new TimerTask() {
                    int count = 0;
                    @Override
                    public void run() {
                        // Timerのスレッド
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                // UIスレッド
                                if (count > COUNTMAX) { // 5回実行したら終了
                                    cancel();
                                }
                                getPosition();
                                count++;

                            }
                        });
                    }
                };

                this.t = new Timer();
                this.t.scheduleAtFixedRate(task, 0, GETHZ);

                return true;
            case R.id.item2:
                GETHZ = 10000;
                this.t.cancel();

                TimerTask task2 = new TimerTask() {
                    int count = 0;
                    @Override
                    public void run() {
                        // Timerのスレッド
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                // UIスレッド
                                if (count > COUNTMAX) { // 5回実行したら終了
                                    cancel();
                                }
                                getPosition();
                                count++;

                            }
                        });
                    }
                };

                this.t = new Timer();
                this.t.scheduleAtFixedRate(task2, 0, GETHZ);


                return true;
            case R.id.item3:
                //getPosition();
                GoogleDirection.withServerKey("AIzaSyA0L2ijVKW918DMyhvafIvOig54g9883AM")
                        .from(getUsersPosition())
                        .to(pDatas.get(pDatas.size()-1).getLat())
                        .execute(new DirectionCallback() {
                            @Override
                            public void onDirectionSuccess(Direction direction, String rawBody) {
                                drawline(direction, rawBody);
                            }

                            @Override
                            public void onDirectionFailure(Throwable t) {
                                // Do something here
                                Log.d("Fail", "root method failure");
                            }
                        });

                return true;
            case R.id.item4:

                return true;
            case R.id.item5:

                return true;
            case R.id.item6:

                return true;
        }



        return super.onOptionsItemSelected(item);
    }

    public LatLng getUsersPosition(){


        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!=
                PackageManager.PERMISSION_GRANTED){
            return new LatLng(0, 0);
        }

        //mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);

        Location location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        Log.d("GETUSER POSITION", "Lat" + location.getLatitude() + "Lon" + location.getLongitude());

        return new LatLng(location.getLatitude(), location.getLongitude());


    }


}
