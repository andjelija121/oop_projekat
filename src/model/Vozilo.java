package model;

import enums.StatusVozila;

public class Vozilo {
    private int id;
    private ModelVozila modelVozila;
    private String registracija;
    private StatusVozila status;
    private int kilometraza;

    public Vozilo(int id, ModelVozila modelVozila, String registracija, StatusVozila status, int kilometraza) {
        this.id = id;
        this.modelVozila = modelVozila;
        this.registracija = registracija;
        this.status = status;
        this.kilometraza = kilometraza;
    }

    public int getId() {
        return id;
    }

    public ModelVozila getModelVozila() {
        return modelVozila;
    }

    public String getRegistracija() {
        return registracija;
    }

    public StatusVozila getStatus() {
        return status;
    }

    public int getKilometraza() {
        return kilometraza;
    }
}
