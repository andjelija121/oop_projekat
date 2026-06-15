package model;

import enums.KategorijaKlijenta;
import enums.KategorijaVozila;
import enums.TipCene;

public class StavkaCenovnika {
    private TipCene tipCene;
    private double vrednost;
    private KategorijaVozila kategorijaVozila;
    private KategorijaKlijenta kategorijaKlijenta;
    private DodatnaUsluga dodatnaUsluga;

    public StavkaCenovnika(TipCene tipCene, double vrednost, KategorijaVozila kategorijaVozila,
                           KategorijaKlijenta kategorijaKlijenta, DodatnaUsluga dodatnaUsluga) {
        this.tipCene = tipCene;
        this.vrednost = vrednost;
        this.kategorijaVozila = kategorijaVozila;
        this.kategorijaKlijenta = kategorijaKlijenta;
        this.dodatnaUsluga = dodatnaUsluga;
    }

    public TipCene getTipCene() {
        return tipCene;
    }

    public double getVrednost() {
        return vrednost;
    }

    public KategorijaVozila getKategorijaVozila() {
        return kategorijaVozila;
    }

    public KategorijaKlijenta getKategorijaKlijenta() {
        return kategorijaKlijenta;
    }

    public DodatnaUsluga getDodatnaUsluga() {
        return dodatnaUsluga;
    }
}
