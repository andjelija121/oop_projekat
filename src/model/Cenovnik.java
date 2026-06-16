package model;

import java.time.LocalDate;
import java.util.ArrayList;

public class Cenovnik {
    private int id;
    private LocalDate datumOd;
    private LocalDate datumDo;
    private ArrayList<StavkaCenovnika> stavke;

    public Cenovnik(int id, LocalDate datumOd, LocalDate datumDo) {
        this.id = id;
        this.datumOd = datumOd;
        this.datumDo = datumDo;
        this.stavke = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public LocalDate getDatumOd() {
        return datumOd;
    }

    public LocalDate getDatumDo() {
        return datumDo;
    }

    public void setDatumDo(LocalDate datumDo) {
        this.datumDo = datumDo;
    }

    public ArrayList<StavkaCenovnika> getStavke() {
        return stavke;
    }

    public void dodajStavku(StavkaCenovnika stavka) {
        stavke.add(stavka);
    }

    public boolean vaziNaDatum(LocalDate datum) {
        return !datum.isBefore(datumOd) && !datum.isAfter(datumDo);
    }
}
