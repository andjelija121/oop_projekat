package menadzment;

import enums.StatusRezervacije;
import enums.StatusVozila;
import model.Agent;
import model.Izdavanje;
import model.Korisnik;
import model.Rezervacija;
import model.Vozilo;
import repozitorijum.IzdavanjeRepozitorijum;
import repozitorijum.RezervacijaRepozitorijum;
import repozitorijum.VoziloRepozitorijum;

import java.time.LocalDate;
import java.util.ArrayList;

public class IzdavanjeMenadzer {
    private IzdavanjeRepozitorijum izdavanjeRepozitorijum;
    private RezervacijaRepozitorijum rezervacijaRepozitorijum;
    private VoziloRepozitorijum voziloRepozitorijum;
    private CenovnikMenadzer cenovnikMenadzer;

    public IzdavanjeMenadzer(IzdavanjeRepozitorijum izdavanjeRepozitorijum,
                             RezervacijaRepozitorijum rezervacijaRepozitorijum,
                             VoziloRepozitorijum voziloRepozitorijum,
                             CenovnikMenadzer cenovnikMenadzer) {
        this.izdavanjeRepozitorijum = izdavanjeRepozitorijum;
        this.rezervacijaRepozitorijum = rezervacijaRepozitorijum;
        this.voziloRepozitorijum = voziloRepozitorijum;
        this.cenovnikMenadzer = cenovnikMenadzer;
    }

    public boolean izdajVozilo(Korisnik ulogovaniKorisnik, int rezervacijaId, int voziloId,
                               int kilometrazaPreuzimanje) {
        if (!(ulogovaniKorisnik instanceof Agent)) {
            return false;
        }

        Rezervacija rezervacija = rezervacijaRepozitorijum.pronadjiPoId(rezervacijaId);
        Vozilo vozilo = voziloRepozitorijum.pronadjiPoId(voziloId);

        if (rezervacija == null || vozilo == null || rezervacija.getStatus() != StatusRezervacije.POTVRDJENA) {
            return false;
        }

        if (izdavanjeRepozitorijum.pronadjiPoRezervaciji(rezervacijaId) != null) {
            return false;
        }

        if (vozilo.getStatus() != StatusVozila.DOSTUPNO || !rezervacija.odnosiSeNaModel(vozilo.getModelVozila())) {
            return false;
        }

        if (kilometrazaPreuzimanje < vozilo.getKilometraza()) {
            return false;
        }

        LocalDate danas = LocalDate.now();
        if (danas.isBefore(rezervacija.getDatumOd()) || danas.isAfter(rezervacija.getDatumDo())) {
            return false;
        }

        Izdavanje izdavanje = new Izdavanje(
                izdavanjeRepozitorijum.sledeciId(),
                rezervacija,
                (Agent) ulogovaniKorisnik,
                vozilo,
                danas,
                rezervacija.getDatumDo(),
                null,
                kilometrazaPreuzimanje,
                null
        );

        vozilo.setStatus(StatusVozila.IZDATO);
        vozilo.setKilometraza(kilometrazaPreuzimanje);
        voziloRepozitorijum.azuriraj(vozilo);
        izdavanjeRepozitorijum.dodaj(izdavanje);
        return true;
    }

    public boolean vratiVozilo(Korisnik ulogovaniKorisnik, int izdavanjeId, LocalDate datumVracanja,
                               int kilometrazaVracanje) {
        if (!(ulogovaniKorisnik instanceof Agent) || datumVracanja == null) {
            return false;
        }

        Izdavanje izdavanje = izdavanjeRepozitorijum.pronadjiPoId(izdavanjeId);
        if (izdavanje == null || !izdavanje.aktivno()) {
            return false;
        }

        if (datumVracanja.isBefore(izdavanje.getDatumIzdavanja()) || datumVracanja.isAfter(LocalDate.now())) {
            return false;
        }

        if (kilometrazaVracanje < izdavanje.getKilometrazaPreuzimanje()) {
            return false;
        }

        Vozilo vozilo = izdavanje.getVozilo();
        Rezervacija rezervacija = izdavanje.getRezervacija();

        if (vozilo == null || rezervacija == null) {
            return false;
        }

        izdavanje.setDatumVracanjaStvarno(datumVracanja);
        izdavanje.setKilometrazaVracanje(kilometrazaVracanje);
        izdavanjeRepozitorijum.azuriraj(izdavanje);

        vozilo.setStatus(StatusVozila.DOSTUPNO);
        vozilo.setKilometraza(kilometrazaVracanje);
        voziloRepozitorijum.azuriraj(vozilo);

        double kazna = cenovnikMenadzer.izracunajKaznuKasnjenja(
                izdavanje.getDatumVracanjaPlanirano(), datumVracanja);
        rezervacija.setKazna(kazna);
        rezervacija.setCenaUkupno(rezervacija.getCenaNajma()
                + rezervacija.getCenaDodatnihUsluga() + kazna);
        rezervacijaRepozitorijum.azuriraj(rezervacija);
        return true;
    }

    public ArrayList<Vozilo> ucitajVozila() {
        return voziloRepozitorijum.ucitajSve();
    }

    public ArrayList<Vozilo> ucitajDostupnaVozilaZaRezervaciju(int rezervacijaId) {
        ArrayList<Vozilo> rezultat = new ArrayList<>();
        Rezervacija rezervacija = rezervacijaRepozitorijum.pronadjiPoId(rezervacijaId);

        if (rezervacija == null) {
            return rezultat;
        }

        for (Vozilo vozilo : voziloRepozitorijum.pronadjiPoModelu(rezervacija.getModelVozila().getId())) {
            if (vozilo.getStatus() == StatusVozila.DOSTUPNO) {
                rezultat.add(vozilo);
            }
        }

        return rezultat;
    }

    public ArrayList<Izdavanje> ucitajAktivnaIzdavanja() {
        ArrayList<Izdavanje> rezultat = new ArrayList<>();

        for (Izdavanje izdavanje : izdavanjeRepozitorijum.ucitajSve()) {
            if (izdavanje.aktivno()) {
                rezultat.add(izdavanje);
            }
        }

        return rezultat;
    }

    public String opisIzdavanjaRezervacije(int rezervacijaId) {
        Izdavanje izdavanje = izdavanjeRepozitorijum.pronadjiPoRezervaciji(rezervacijaId);

        if (izdavanje == null) {
            return "Nije izdato";
        }

        return izdavanje.aktivno() ? "Izdato" : "Vraceno";
    }
}
