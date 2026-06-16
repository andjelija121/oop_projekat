package model;

import enums.StatusPretplate;

import java.time.LocalDate;

public class ZahtevPretplate {
    private int id;
    private Klijent klijent;
    private Agent agent;
    private LocalDate datumZahteva;
    private StatusPretplate status;

    public ZahtevPretplate(int id, Klijent klijent, Agent agent, LocalDate datumZahteva, StatusPretplate status) {
        this.id = id;
        this.klijent = klijent;
        this.agent = agent;
        this.datumZahteva = datumZahteva;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public Klijent getKlijent() {
        return klijent;
    }

    public Agent getAgent() {
        return agent;
    }

    public void setAgent(Agent agent) {
        this.agent = agent;
    }

    public LocalDate getDatumZahteva() {
        return datumZahteva;
    }

    public StatusPretplate getStatus() {
        return status;
    }

    public void setStatus(StatusPretplate status) {
        this.status = status;
    }

    public boolean naCekanju() {
        return status == StatusPretplate.NA_CEKANJU;
    }
}
