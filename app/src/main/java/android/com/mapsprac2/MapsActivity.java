package android.com.mapsprac2;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private GoogleMap mMap;
    LocationManager locationManager;
    LocationListener locationListener;
    List<LatLng> trackerList;
    boolean tracker = true;
    Marker marker, marker1;
    LatLng currLoc;
    LatLng loc;
    PolylineOptions showTracker;
    Polyline line;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        int permCheck = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION);
        if (permCheck != PackageManager.PERMISSION_GRANTED) {
            PermissionUtils.requestPermission(MapsActivity.this, LOCATION_PERMISSION_REQUEST_CODE,
                    android.Manifest.permission.ACCESS_FINE_LOCATION, true);
        }
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
//        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        showTracker = new PolylineOptions();
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        trackerList = new ArrayList<>();
        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                currLoc = new LatLng(location.getLatitude(),location.getLongitude());
                marker1 = mMap.addMarker(new MarkerOptions().position(currLoc).title("My Location"));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(currLoc));
                return false;
            }
        });
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                if(tracker){
                    mMap.clear();
                    trackerList.clear();
                    trackerList.add(currLoc);
                    marker1 = mMap.addMarker(new MarkerOptions().position(currLoc).title("My Location"));
                    marker1.setPosition(currLoc);
                    Toast.makeText(getApplicationContext(),"Tracking started",Toast.LENGTH_SHORT).show();
                    tracker = false;
                }else{
                    Toast.makeText(getApplicationContext(),"Tracking stopped",Toast.LENGTH_SHORT).show();
                    tracker = true;
                    trackerList.clear();
                    currLoc = loc;
                }

            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("GPS disabled")
                    .setMessage("Would you like to enable the GPS?")
                    .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent settings = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(settings);
                        }
                    })
                    .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            finish();
                        }
                    });
            AlertDialog dialog = builder.create();
            dialog.show();
        } else {
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    if(!tracker) {
                        Log.d("TAG", location.getLatitude() + "===" + location.getLongitude());
                        loc = new LatLng(location.getLatitude(), location.getLongitude());
                        trackerList.add(loc);
                        drawTracker(trackerList);
                        LatLngBounds.Builder builder = new LatLngBounds.Builder();
                        builder.include(loc);
                        LatLngBounds bounds = builder.build();
                        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 5);
                        mMap.moveCamera(cameraUpdate);
                        mMap.moveCamera(CameraUpdateFactory.zoomTo(18));

//                        if(currLoc!=null){
//                            showTracker.add(currLoc);
//                            mMap.addPolyline(showTracker);
//                            currLoc = null;
//                        }else {
//                            showTracker.add(loc);
//                            mMap.addPolyline(showTracker);
//                        }
                        marker = mMap.addMarker(new MarkerOptions().position(loc));
                        marker.setAlpha((float) 0.87);

                    }
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            };
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 10, locationListener);

        }
    }

    private void drawTracker(List<LatLng> trackerList) {
        mMap.clear();
//        showTracker.addAll(trackerList);
//        showTracker.geodesic(true);
//        mMap.addPolyline(showTracker);
        PolylineOptions options = new PolylineOptions().width(5).color(Color.BLACK).geodesic(true);
        for (int i = 0; i < trackerList.size(); i++) {
            LatLng point = trackerList.get(i);
            options.add(point);
        }
        line = mMap.addPolyline(options);
    }


}
