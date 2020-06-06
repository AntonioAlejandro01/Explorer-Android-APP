package com.antonioalejandro.explorerapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ExplorerDB extends SQLiteOpenHelper {
    private static final String TAG = "EXPLORER_DB";
    private SQLiteDatabase db;
    private static ExplorerDB explorerDB = null;

    public static ExplorerDB getInstance(Context context) {
        if (explorerDB == null) {
            explorerDB = new ExplorerDB(context);
        }
        return explorerDB;
    }

    private ExplorerDB(Context context) {
        super(context, "explorer.db", null, 1);
        db = getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE Ruta(_id integer PRIMARY KEY AUTOINCREMENT, title text ,author text,location text,topic text)");
        db.execSQL("CREATE TABLE Place(_id integer PRIMARY KEY AUTOINCREMENT, name text ,latitude text, longitude text, comment text,visited integer, ruta integer)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public Optional<Ruta> getRuta(int id){
        Cursor cursor = db.rawQuery("SELECT _id,title,author,location,topic FROM Ruta" + ( id == -1 ? "order by _id DESC LIMIT 1": ("WHERE _id=" + id)),null);
        Ruta ruta = null;
        if (cursor.moveToFirst()){
            do {
                ruta = new Ruta();
                ruta.setId(cursor.getInt(0));
                ruta.setTitle(cursor.getString(1));
                ruta.setAuthor(cursor.getString(2));
                ruta.setLocation(cursor.getString(3));
                ruta.setTopic(cursor.getString(4));
                ruta.setPlaces(getPlacesFromRoute(ruta.getId()).orElseGet(ArrayList::new));
            }while (cursor.moveToNext());
            cursor.close();
        }
        return Optional.ofNullable(ruta);
    }


    public Optional<List<Ruta>> getRutas(){
        Cursor cursor = db.rawQuery("SELECT _id,title,author,location,topic FROM Ruta",null);
        ArrayList<Ruta> alRutas = null;
        Ruta ruta;
        if (cursor.moveToFirst()){
            alRutas = new ArrayList<>(cursor.getCount());
            do {
                ruta = new Ruta();
                ruta.setId(cursor.getInt(0));
                ruta.setTitle(cursor.getString(1));
                ruta.setAuthor(cursor.getString(2));
                ruta.setLocation(cursor.getString(3));
                ruta.setTopic(cursor.getString(4));
                ruta.setPlaces(getPlacesFromRoute(ruta.getId()).orElseGet(ArrayList::new));
                alRutas.add(ruta);
            }while (cursor.moveToNext());
            cursor.close();
        }
        return Optional.ofNullable(alRutas);
    }

    public Optional<List<Place>> getPlacesFromRoute(int id){
        Cursor cursor = db.rawQuery("SELECT _id,name,latitude,longitude,comment,visited FROM Place WHERE ruta = " + id,null);
        ArrayList<Place> alPlaces = null;
        Place place;
        if (cursor.moveToFirst()){
            alPlaces = new ArrayList<>(cursor.getCount());
            do {
                place = new Place();
                place.setId(cursor.getInt(0));
                place.setNombre(cursor.getString(1));
                place.setCoordenadas(new Coordenadas(cursor.getDouble(2),cursor.getDouble(3)));
                place.setComentario(cursor.getString(4));
                place.setVisited(cursor.getInt(5) == 0 ? Boolean.FALSE:Boolean.TRUE );
                alPlaces.add(place);
            }while (cursor.moveToNext());
            cursor.close();
        }
        return Optional.ofNullable(alPlaces);
    }

    public void insertRuta(Ruta ruta) {
        ContentValues cv = new ContentValues();
        cv.put("title", ruta.getTitle());
        cv.put("author", ruta.getAuthor());
        cv.put("location", ruta.getLocation());
        cv.put("topic", ruta.getTopic());


        final long id = db.insert("Ruta", null, cv);;
        if (id != -1){
            ruta.getPlaces().forEach(place -> insertPlace(place,(int)id));
        }

    }

    private void insertPlace(Place place,int id){
        ContentValues cv = new ContentValues();
        cv.put("name",place.getNombre());
        cv.put("latitude",String.valueOf(place.getCoordenadas().getLatitud()));
        cv.put("longitude",String.valueOf(place.getCoordenadas().getLongitud()));
        cv.put("comment",place.getComentario());
        cv.put("visited" ,0);
        cv.put("ruta",id);

        long num = db.insert("Place",null,cv);
        System.out.println(num);

    }

    public void borrarRutasCompletadas(){
        getRutas().orElseGet(ArrayList::new).forEach(ruta -> {
            if (ruta.getProgress() == ruta.getPlaces().size()) deleteRuta(ruta);
            Log.d(TAG, "borrarRutasCompletadas: Ruta" + ruta.getTitle() + "Borrada por terminada");
        });
    }

    private void deleteRuta(Ruta ruta){
        ruta.getPlaces().forEach(place -> deletePlace(ruta));
        db.execSQL("DELETE FROM Ruta WHERE _id=" + ruta.getId());
    }

    private void deletePlace(Ruta ruta) {
        db.execSQL("DELETE FROM Place WHERE ruta=" + ruta.getId() );
    }


}
