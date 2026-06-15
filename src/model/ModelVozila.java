package model;

import enums.KategorijaVozila;

public class ModelVozila {
    private int id;
    private String naziv;
    private String proizvodjac;
    private KategorijaVozila kategorijaVozila;

    public ModelVozila(int id, String naziv, String proizvodjac, KategorijaVozila kategorijaVozila) {
        this.id = id;
        this.naziv = naziv;
        this.proizvodjac = proizvodjac;
        this.kategorijaVozila = kategorijaVozila;
    }

    public int getId() {
        return id;
    }

    public String getNaziv() {
        return naziv;
    }

    public String getProizvodjac() {
        return proizvodjac;
    }

    public KategorijaVozila getKategorijaVozila() {
        return kategorijaVozila;
    }

    @Override
    public String toString() {
        return proizvodjac + " " + naziv;
    }
}
