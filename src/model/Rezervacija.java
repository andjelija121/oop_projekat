package model;

import enums.StatusRezervacije;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Rezervacija {
    private int id;
    private Klijent klijent;
    private Agent agentObrade;
    private ModelVozila modelVozila;
    private LocalDate datumOd;
    private LocalDate datumDo;
    private LocalDate datumKreiranja;
    private LocalDate datumObrade;
    private StatusRezervacije status;
    private double cenaNajma;
    private double cenaDodatnihUsluga;
    private double kazna;
    private double cenaUkupno;
    private LocalDateTime vremeOtkazivanja;

    public Rezervacija(int id, Klijent klijent, ModelVozila modelVozila, LocalDate datumOd, LocalDate datumDo,
                       StatusRezervacije status, double cenaNajma, double cenaDodatnihUsluga,
                       double kazna, double cenaUkupno) {
        this(id, klijent, modelVozila, datumOd, datumDo, status, cenaNajma, cenaDodatnihUsluga,
                kazna, cenaUkupno, null, null, LocalDate.now(), null);
    }

    public Rezervacija(int id, Klijent klijent, ModelVozila modelVozila, LocalDate datumOd, LocalDate datumDo,
                       StatusRezervacije status, double cenaNajma, double cenaDodatnihUsluga,
                       double kazna, double cenaUkupno, LocalDateTime vremeOtkazivanja) {
        this(id, klijent, modelVozila, datumOd, datumDo, status, cenaNajma, cenaDodatnihUsluga,
                kazna, cenaUkupno, vremeOtkazivanja, null, LocalDate.now(), null);
    }

    public Rezervacija(int id, Klijent klijent, ModelVozila modelVozila, LocalDate datumOd, LocalDate datumDo,
                       StatusRezervacije status, double cenaNajma, double cenaDodatnihUsluga,
                       double kazna, double cenaUkupno, LocalDateTime vremeOtkazivanja,
                       Agent agentObrade, LocalDate datumKreiranja, LocalDate datumObrade) {
        this.id = id;
        this.klijent = klijent;
        this.agentObrade = agentObrade;
        this.modelVozila = modelVozila;
        this.datumOd = datumOd;
        this.datumDo = datumDo;
        this.datumKreiranja = datumKreiranja;
        this.datumObrade = datumObrade;
        this.status = status;
        this.cenaNajma = cenaNajma;
        this.cenaDodatnihUsluga = cenaDodatnihUsluga;
        this.kazna = kazna;
        this.cenaUkupno = cenaUkupno;
        this.vremeOtkazivanja = vremeOtkazivanja;
    }

    public int getId() {
        return id;
    }

    public Klijent getKlijent() {
        return klijent;
    }

    public Agent getAgentObrade() {
        return agentObrade;
    }

    public void setAgentObrade(Agent agentObrade) {
        this.agentObrade = agentObrade;
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

    public LocalDate getDatumKreiranja() {
        return datumKreiranja;
    }

    public void setDatumKreiranja(LocalDate datumKreiranja) {
        this.datumKreiranja = datumKreiranja;
    }

    public LocalDate getDatumObrade() {
        return datumObrade;
    }

    public void setDatumObrade(LocalDate datumObrade) {
        this.datumObrade = datumObrade;
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

    public void setKazna(double kazna) {
        this.kazna = kazna;
    }

    public double getCenaUkupno() {
        return cenaUkupno;
    }

    public void setCenaUkupno(double cenaUkupno) {
        this.cenaUkupno = cenaUkupno;
    }

    public LocalDateTime getVremeOtkazivanja() {
        return vremeOtkazivanja;
    }

    public void setVremeOtkazivanja(LocalDateTime vremeOtkazivanja) {
        this.vremeOtkazivanja = vremeOtkazivanja;
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
