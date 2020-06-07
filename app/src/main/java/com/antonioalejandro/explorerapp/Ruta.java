package com.antonioalejandro.explorerapp;

import java.util.ArrayList;
import java.util.List;

public class Ruta {

    private int id;
    private String title;
    private String author;
    private String location;
    private String topic;
    private List<Place> places;

    public Ruta(String title, String author, String location, String topic, List<Place> places) {
        this.title = title;
        this.author = author;
        this.location = location;
        this.topic = topic;
        this.places = places;
    }

    public Ruta(String title, String author, String location, String topic) {
        this.title = title;
        this.author = author;
        this.location = location;
        this.topic = topic;
    }

    public Ruta() {
        places = new ArrayList<>();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public List<Place> getPlaces() {
        return places;
    }

    public void setPlaces(List<Place> places) {
        this.places = places;
    }

    public long getProgress() {
        return places.stream().filter(Place::isVisited).count();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
