package com.antonioalejandro.explorerapp;

public class Place {

    private int id;
    private String nombre;
    private Coordenadas coordenadas;
    private String comentario;

    private boolean visited;

    public Place(String nombre, Coordenadas coordenadas, String comentario) {
        this.nombre = nombre;
        this.coordenadas = coordenadas;
        this.comentario = comentario;
        visited = false;
    }

    public Place() {
        visited = false;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Coordenadas getCoordenadas() {
        return coordenadas;
    }

    public void setCoordenadas(Coordenadas coordenadas) {
        this.coordenadas = coordenadas;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    public boolean isVisited() {
        return visited;
    }

    public void setVisited(boolean visited) {
        this.visited = visited;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
