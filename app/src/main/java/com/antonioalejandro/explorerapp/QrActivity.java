package com.antonioalejandro.explorerapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.PointF;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

import me.dm7.barcodescanner.zxing.ZXingScannerView;


public class QrActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {

    private ZXingScannerView mScannerView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mScannerView = new ZXingScannerView(this);   // Programmatically initialize the scanner view
        mScannerView.setFormats(Arrays.asList(BarcodeFormat.QR_CODE));
        setContentView(mScannerView);                // Set the scanner view as the content view
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        mScannerView.startCamera();          // Start camera on resume
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();           // Stop camera on pause
    }

    @Override
    public void handleResult(Result rawResult) {
        // Do something with the result here
        Log.v("TAG", rawResult.getText()); // Prints scan results
        Log.v("TAG", rawResult.getBarcodeFormat().toString()); // Prints the scan format (qrcode, pdf417 etc.)
        try {
            ExplorerDB.getInstance(this).insertRuta(parseJsonToRuta(rawResult.getText()));
            this.setResult(RESULT_OK);
            this.finish();
        } catch (JSONException e) {
            Toast.makeText(this, R.string.qr_not_valid, Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

    }

    private Ruta parseJsonToRuta(String json) throws JSONException {
        JSONObject jsonObject = new JSONObject(json);
        Ruta ruta = new Ruta();
        ruta.setTitle(jsonObject.getString("title"));
        ruta.setAuthor(jsonObject.getString("author"));
        ruta.setLocation(jsonObject.getString("location"));
        ruta.setTopic(jsonObject.getString("topic"));
        ArrayList<Place> places = new ArrayList<>();
        JSONObject jsonPlaces = jsonObject.getJSONObject("places");
        JSONArray nombres = jsonPlaces.getJSONArray("nombres");
        JSONArray latitudes = jsonPlaces.getJSONArray("latitudes");
        JSONArray longitudes = jsonPlaces.getJSONArray("longitudes");
        JSONArray comments = jsonPlaces.getJSONArray("comments");
        for (int i = 0; i < nombres.length(); i++) {
            places.add(new Place(nombres.getString(i), new Coordenadas(latitudes.getDouble(i), longitudes.getDouble(i)), comments.getString(i)));
        }
        ruta.setPlaces(places);
        return ruta;
    }
}
