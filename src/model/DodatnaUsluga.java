package model;

import enums.TipNaplate;

public class DodatnaUsluga {
    private int id;
    private String naziv;
    private TipNaplate tipNaplate;

    public DodatnaUsluga(int id, String naziv, TipNaplate tipNaplate) {
        this.id = id;
        this.naziv = naziv;
        this.tipNaplate = tipNaplate;
    }

    public int getId() {
        return id;
    }

    public String getNaziv() {
        return naziv;
    }

    public TipNaplate getTipNaplate() {
        return tipNaplate;
    }

    @Override
    public String toString() {
        return naziv;
    }
}
