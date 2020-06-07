package com.antonioalejandro.explorerapp;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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
        mScannerView = new ZXingScannerView(this);
        mScannerView.setFormats(Arrays.asList(BarcodeFormat.QR_CODE));
        setContentView(mScannerView);
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this);
        mScannerView.startCamera();
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();           // Stop camera on pause
    }

    @Override
    public void handleResult(Result rawResult) {
        Log.v("TAG", rawResult.getText());
        Log.v("TAG", rawResult.getBarcodeFormat().toString());
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
