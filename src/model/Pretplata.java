package model;

import enums.StatusPretplate;

import java.time.LocalDate;

public class Pretplata {
    private int id;
    private Klijent klijent;
    private LocalDate datumPocetka;
    private LocalDate datumKraja;
    private StatusPretplate status;
    private double cena;

    public Pretplata(int id, Klijent klijent, LocalDate datumPocetka, LocalDate datumKraja,
                     StatusPretplate status, double cena) {
        this.id = id;
        this.klijent = klijent;
        this.datumPocetka = datumPocetka;
        this.datumKraja = datumKraja;
        this.status = status;
        this.cena = cena;
    }

    public int getId() {
        return id;
    }

    public Klijent getKlijent() {
        return klijent;
    }

    public LocalDate getDatumPocetka() {
        return datumPocetka;
    }

    public LocalDate getDatumKraja() {
        return datumKraja;
    }

    public StatusPretplate getStatus() {
        return status;
    }

    public void setStatus(StatusPretplate status) {
        this.status = status;
    }

    public double getCena() {
        return cena;
    }

    public boolean aktivnaNaDatum(LocalDate datum) {
        return status == StatusPretplate.AKTIVNA
                && !datum.isBefore(datumPocetka)
                && !datum.isAfter(datumKraja);
    }
}
