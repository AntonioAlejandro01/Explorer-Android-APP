package com.antonioalejandro.explorerapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {


    private final String TAG = "ZZZ";
    private MapView map = null;
    private LocationManager locationManager;
    private Location location;

    private boolean focus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        setContentView(R.layout.activity_main);
        map = findViewById(R.id.mvOSM);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setBuiltInZoomControls(false);
        map.setMultiTouchControls(true);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Permissions.askPermission(this, this);

        //GPS
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        } else if (locationManager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER)) {
            location = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
        }

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,2000,10,new LocationListener() {
            @Override
            public void onLocationChanged(Location loca) {
                location = loca;
                setPlayerMarker(new GeoPoint(loca.getLatitude(),loca.getLongitude()));
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
        });
        GeoPoint player = new GeoPoint(location.getLatitude(), location.getLongitude());
        setPlayerMarker(player);

        map.getController().setCenter(player);
        map.getController().setZoom(12);
        focus = false;
    }


    public void onClickMarcar(View view) {
    }

    public void onClickCreator(View view) {
    }

    private void setPlayerMarker(GeoPoint playerPosition){
        Marker player = new Marker(map);
        player.setIcon(getDrawable(R.drawable.ic_my_location));
        player.setPosition(playerPosition);
        player.setOnMarkerClickListener((x,y)->{
            map.getController().animateTo(playerPosition);
            map.getController().setCenter(playerPosition);
            map.getController().zoomTo(map.getMaxZoomLevel() - 1);
            return false;
        });
        if (focus) {
            map.getController().setCenter(playerPosition);
        }
        map.getOverlays().clear();
        setMarkersToMap(places);
        map.getOverlays().add(player);
    }

    private void setMarkersToMap(ArrayList<Place> places) {
        map.getOverlays().clear();
        for (int i = 0; i < places.size(); i++) {
            addMarker(places.get(i));
        }
    }
    private void addMarker(Place place) {

        Marker marker = new Marker(map);
        GeoPoint point = new GeoPoint(place.getCoordinates().getLatitude(), place.getCoordinates().getLongitude());
        marker.setPosition(point);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_LEFT);
        marker.setImage(new BitmapDrawable(getResources(), camera.loadSavedPhoto(place.getPathPhoto())));
        marker.setTitle(place.getName());
        marker.setIcon(getDrawable(R.drawable.ic_marker));
        marker.setSubDescription(place.getDescription());
        //TODO crear basicInfoWindow personalizado
        map.getOverlays().add(marker);

    }

    public void onClickFocus(View view) {
        if (focus){
            focus = false;
            ((ImageButton)view).setImageResource(R.drawable.ic_my_location_focus);
        }
        else{
            map.getController().animateTo(new GeoPoint(location.getLatitude(),location.getLongitude()));
            focus = true;
            ((ImageButton)view).setImageResource(R.drawable.ic_my_location_focus_enabled);
        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //TODO if permission for get location is refused , the app finish with a message
        if (requestCode == Permissions.REQUEST_CODE_PERMISSIONS) {
            for (int i = 0; i < permissions.length; i++) {
                Log.d(TAG, "onRequestPermissionsResult: " + permissions[i] + ((grantResults[i] == PackageManager.PERMISSION_GRANTED) ? " ACCEPT" : " DENIE"));
            }

        }

    }
}
