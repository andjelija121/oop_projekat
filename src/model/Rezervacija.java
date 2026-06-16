package model;

import enums.StatusRezervacije;

import java.time.LocalDate;

public class Rezervacija {
    private int id;
    private Klijent klijent;
    private ModelVozila modelVozila;
    private LocalDate datumOd;
    private LocalDate datumDo;
    private StatusRezervacije status;
    private double cenaNajma;
    private double cenaDodatnihUsluga;
    private double kazna;
    private double cenaUkupno;

    public Rezervacija(int id, Klijent klijent, ModelVozila modelVozila, LocalDate datumOd, LocalDate datumDo,
                       StatusRezervacije status, double cenaNajma, double cenaDodatnihUsluga,
                       double kazna, double cenaUkupno) {
        this.id = id;
        this.klijent = klijent;
        this.modelVozila = modelVozila;
        this.datumOd = datumOd;
        this.datumDo = datumDo;
        this.status = status;
        this.cenaNajma = cenaNajma;
        this.cenaDodatnihUsluga = cenaDodatnihUsluga;
        this.kazna = kazna;
        this.cenaUkupno = cenaUkupno;
    }

    public int getId() {
        return id;
    }

    public Klijent getKlijent() {
        return klijent;
    }

    public ModelVozila getModelVozila() {
        return modelVozila;
    }

    public LocalDate getDatumOd() {
        return datumOd;
    }

    public LocalDate getDatumDo() {
        return datumDo;
    }

    public StatusRezervacije getStatus() {
        return status;
    }

    public void setStatus(StatusRezervacije status) {
        this.status = status;
    }

    public double getCenaNajma() {
        return cenaNajma;
    }

    public double getCenaDodatnihUsluga() {
        return cenaDodatnihUsluga;
    }

    public void setCenaDodatnihUsluga(double cenaDodatnihUsluga) {
        this.cenaDodatnihUsluga = cenaDodatnihUsluga;
    }

    public double getKazna() {
        return kazna;
    }

    public double getCenaUkupno() {
        return cenaUkupno;
    }

    public void setCenaUkupno(double cenaUkupno) {
        this.cenaUkupno = cenaUkupno;
    }

    public boolean jeNaCekanju() {
        return status == StatusRezervacije.NA_CEKANJU;
    }

    public boolean mozeDaSeOtkaze() {
        return status == StatusRezervacije.NA_CEKANJU
                || status == StatusRezervacije.POTVRDJENA;
    }

    public boolean obuhvataDatum(LocalDate datum) {
        return !datum.isBefore(datumOd) && !datum.isAfter(datumDo);
    }

    public boolean pripadaKlijentu(Klijent klijent) {
        return this.klijent != null && klijent != null && this.klijent.getId() == klijent.getId();
    }

    public boolean odnosiSeNaModel(ModelVozila modelVozila) {
        return this.modelVozila != null && modelVozila != null
                && this.modelVozila.getId() == modelVozila.getId();
    }
}
