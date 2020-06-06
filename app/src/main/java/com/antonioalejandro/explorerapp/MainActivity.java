package com.antonioalejandro.explorerapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class MainActivity extends AppCompatActivity {


    private final String TAG = "ZZZ";
    private final int ZOOM_LEVEL = 20;
    private final int METERS_TO_REFRESH = 10;
    private final int TIME_TO_REFRESH = 2000;
    private final String NAME_PREFERENCES = "preferences";
    private final String KEY_PREFERENCES_ID_RUTA = "key_prefenreces_id_ruta";

    private final int REQUEST_CODE_QR = 1;
    // views
        // map
    private MapView map = null;
        // textViews
    private TextView tvNumberPlace;
    private TextView tvTitlePlace;
    private TextView tvCommentPlace;
    private TextView tvRecomendation;
        // ImageButton
    private ImageButton ibLocation;
        // linearLayouts
    private LinearLayout llRandomPlace;
    //GPS
    private Location location;
    //
    private Ruta ruta;
    private boolean focus;
    private Place randomPlace;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        setContentView(R.layout.activity_main);
        // Maps
        map = findViewById(R.id.mvOSM);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setBuiltInZoomControls(false);
        map.setMultiTouchControls(true);
        // other views
        tvRecomendation = findViewById(R.id.tvRecomendation);
        tvNumberPlace = findViewById(R.id.tvNumberPlace);
        tvTitlePlace = findViewById(R.id.tvTitlePlace);
        tvCommentPlace = findViewById(R.id.tvCommentPlace);
        ibLocation = findViewById(R.id.ibLocation);
        llRandomPlace = findViewById(R.id.llRandomPlace);

        llRandomPlace.setOnLongClickListener(view -> {
            Collections.shuffle(ruta.getPlaces());
            randomPlace = ruta.getPlaces().get(0);
            tvTitlePlace.setText(randomPlace.getNombre());
            tvCommentPlace.setText(randomPlace.getComentario());
            return false;
        });

        // Geo permissions
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Permissions.askPermission(this, this);
        //GPS
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        } else if (locationManager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER)) {
            location = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
        }

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,TIME_TO_REFRESH,METERS_TO_REFRESH,new LocationListener() {
            @Override
            public void onLocationChanged(Location loca) {
                location = loca;
                setPlayerMarker(new GeoPoint(loca.getLatitude(),loca.getLongitude()));
                Log.d(TAG, "onLocationChanged: Location was updated");
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
        // preferencias
        SharedPreferences prefs = getSharedPreferences(NAME_PREFERENCES,Context.MODE_PRIVATE);
        final int id = prefs.getInt(KEY_PREFERENCES_ID_RUTA,-1);
        if(id == -1){ // no hay ninguna ruta
           tvNumberPlace.setText(R.string.sin_ruta);
           llRandomPlace.setVisibility(View.GONE);
           tvRecomendation.setVisibility(View.GONE);
           this.ruta = new Ruta();
           ruta.setPlaces(new ArrayList<>());
        }
        else{ // llamar a la base de datos
            // obtiene la ruta guardada en la base de datos con el id guardado el las prefs de antes de cerra la aplicacion la ultima vez. si no intenta buscar la ultima y en ultimo caso crea una vacia
            this.ruta = ExplorerDB.getInstance(this).getRuta(id).orElseGet(() ->  ExplorerDB.getInstance(this).getRuta(-1).orElse(new Ruta()));
            tvNumberPlace.setText(getString(R.string.template_ruta_numnber,(int)ruta.getProgress(),ruta.getPlaces().size()));
            Collections.shuffle(ruta.getPlaces());
            randomPlace = ruta.getPlaces().get(0);
            tvTitlePlace.setText(randomPlace.getNombre());
            tvCommentPlace.setText(randomPlace.getComentario());
        }
        // init position in map
        GeoPoint player = new GeoPoint(location.getLatitude(), location.getLongitude());
        setPlayerMarker(player);

        map.getController().setCenter(player);
        map.getController().setZoom(ZOOM_LEVEL);
        focus = false;

        ExplorerDB.getInstance(this).getRutas().ifPresent(list -> list.forEach(item -> Log.d("XXX",item.getTitle() + " " + item.getId() + "" + item.getPlaces().size())));
    }


    public void onClickMarcar(View view) {
        map.getController().animateTo(new GeoPoint(randomPlace.getCoordenadas().getLatitud(),randomPlace.getCoordenadas().getLongitud()));
    }

    public void onClickCreator(View view) {
        startActivity(new Intent(this,WebActivity.class));
    }

    public void onClickShowInfo(View view) {
    }

    public void onClickReadRuta(View view) {
        Intent intent = new Intent(this,QrActivity.class);
        startActivityForResult(intent,REQUEST_CODE_QR);
    }

    private void setPlayerMarker(GeoPoint playerPosition){
        Marker player = new Marker(map);
        player.setIcon(getDrawable(R.drawable.ic_my_location));
        player.setPosition(playerPosition);
        player.setOnMarkerClickListener((x,y)->{
            map.getController().animateTo(playerPosition);
            map.getController().setCenter(playerPosition);
            map.getController().zoomTo(ZOOM_LEVEL);
            return false;
        });
        if (focus) {
            map.getController().setCenter(playerPosition);
        }
        Log.d(TAG, "setPlayerMarker: " + ruta);
        setMarkersToMap(ruta.getPlaces());
        map.getOverlays().add(player);
    }

    private void setMarkersToMap(List<Place> places) {
        map.getOverlays().clear();
        places.forEach(this::addMarker);
    }
    private void addMarker(Place place) {
        Marker marker = new Marker(map);
        GeoPoint point = new GeoPoint(place.getCoordenadas().getLatitud(), place.getCoordenadas().getLongitud());
        marker.setPosition(point);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_LEFT);
        marker.setTitle(place.getNombre());
        marker.setIcon(getDrawable(place.isVisited() ? R.drawable.ic_marker_visited:R.drawable.ic_marker ));
        marker.setSubDescription(place.getComentario());
        map.getOverlays().add(marker);
    }

    public void onClickFocus(View view) {
       if (focus){
           map.getController().animateTo(new GeoPoint(location.getLatitude(),location.getLongitude()));
       }
       ibLocation.setImageResource(focus ? R.drawable.ic_my_location: R.drawable.ic_my_location_focus);
       focus = !focus;
    }

    public void initializeRoute(Ruta ruta){
        this.ruta = ruta;
        tvNumberPlace.setText(getString(R.string.template_ruta_numnber,(int)ruta.getProgress(),ruta.getPlaces().size()));
        Collections.shuffle(ruta.getPlaces());
        this.randomPlace = ruta.getPlaces().get(0);
        tvTitlePlace.setText(randomPlace.getNombre());
        tvCommentPlace.setText(randomPlace.getComentario());
        llRandomPlace.setVisibility(View.VISIBLE);
        tvRecomendation.setVisibility(View.GONE);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == Permissions.REQUEST_CODE_PERMISSIONS) {
            for (int i = 0; i < permissions.length; i++) {
                Log.d(TAG, "onRequestPermissionsResult: " + permissions[i] + ((grantResults[i] == PackageManager.PERMISSION_GRANTED) ? " ACCEPT" : " DENIED"));
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_QR) {
            if (resultCode == RESULT_OK) {
                ExplorerDB.getInstance(this).getRuta(-1).ifPresent(this::initializeRoute);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SharedPreferences prefs = getSharedPreferences(NAME_PREFERENCES,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_PREFERENCES_ID_RUTA,ruta.getId());
        editor.commit();

    }
}
