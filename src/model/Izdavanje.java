package model;

import java.time.LocalDate;

public class Izdavanje {
    private int id;
    private Rezervacija rezervacija;
    private Agent agent;
    private Vozilo vozilo;
    private LocalDate datumIzdavanja;
    private LocalDate datumVracanjaPlanirano;
    private LocalDate datumVracanjaStvarno;
    private int kilometrazaPreuzimanje;
    private Integer kilometrazaVracanje;

    public Izdavanje(int id, Rezervacija rezervacija, Agent agent, Vozilo vozilo,
                     LocalDate datumIzdavanja, LocalDate datumVracanjaPlanirano,
                     LocalDate datumVracanjaStvarno, int kilometrazaPreuzimanje,
                     Integer kilometrazaVracanje) {
        this.id = id;
        this.rezervacija = rezervacija;
        this.agent = agent;
        this.vozilo = vozilo;
        this.datumIzdavanja = datumIzdavanja;
        this.datumVracanjaPlanirano = datumVracanjaPlanirano;
        this.datumVracanjaStvarno = datumVracanjaStvarno;
        this.kilometrazaPreuzimanje = kilometrazaPreuzimanje;
        this.kilometrazaVracanje = kilometrazaVracanje;
    }

    public int getId() {
        return id;
    }

    public Rezervacija getRezervacija() {
        return rezervacija;
    }

    public Agent getAgent() {
        return agent;
    }

    public Vozilo getVozilo() {
        return vozilo;
    }

    public LocalDate getDatumIzdavanja() {
        return datumIzdavanja;
    }

    public LocalDate getDatumVracanjaPlanirano() {
        return datumVracanjaPlanirano;
    }

    public LocalDate getDatumVracanjaStvarno() {
        return datumVracanjaStvarno;
    }

    public void setDatumVracanjaStvarno(LocalDate datumVracanjaStvarno) {
        this.datumVracanjaStvarno = datumVracanjaStvarno;
    }

    public int getKilometrazaPreuzimanje() {
        return kilometrazaPreuzimanje;
    }

    public Integer getKilometrazaVracanje() {
        return kilometrazaVracanje;
    }

    public void setKilometrazaVracanje(Integer kilometrazaVracanje) {
        this.kilometrazaVracanje = kilometrazaVracanje;
    }

    public boolean aktivno() {
        return datumVracanjaStvarno == null;
    }
}
