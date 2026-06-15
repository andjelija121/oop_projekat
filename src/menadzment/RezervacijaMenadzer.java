package menadzment;

import enums.StatusRezervacije;
import model.Agent;
import model.Klijent;
import model.Korisnik;
import model.ModelVozila;
import model.Rezervacija;
import repozitorijum.ModelVozilaRepozitorijum;
import repozitorijum.RezervacijaRepozitorijum;
import repozitorijum.VoziloRepozitorijum;

import java.time.LocalDate;
import java.util.ArrayList;

public class RezervacijaMenadzer {
    private RezervacijaRepozitorijum rezervacijaRepozitorijum;
    private VoziloRepozitorijum voziloRepozitorijum;
    private ModelVozilaRepozitorijum modelVozilaRepozitorijum;

    public RezervacijaMenadzer(RezervacijaRepozitorijum rezervacijaRepozitorijum,
                              VoziloRepozitorijum voziloRepozitorijum,
                              ModelVozilaRepozitorijum modelVozilaRepozitorijum) {
        this.rezervacijaRepozitorijum = rezervacijaRepozitorijum;
        this.voziloRepozitorijum = voziloRepozitorijum;
        this.modelVozilaRepozitorijum = modelVozilaRepozitorijum;
    }

    public boolean daLiJeModelDostupan(ModelVozila modelVozila, LocalDate datumOd, LocalDate datumDo) {
        if (modelVozila == null || !validanPeriod(datumOd, datumDo)) {
            return false;
        }

        int brojPrimeraka = voziloRepozitorijum.pronadjiPoModelu(modelVozila.getId()).size();

        if (brojPrimeraka == 0) {
            return false;
        }

        LocalDate dan = datumOd;

        while (!dan.isAfter(datumDo)) {
            int brojZauzetihPrimeraka = 0;

            for (Rezervacija rezervacija : rezervacijaRepozitorijum.ucitajSve()) {
                if (rezervacija.getModelVozila() == null) {
                    continue;
                }

                boolean istiModel = rezervacija.odnosiSeNaModel(modelVozila);
                boolean potvrdjena = rezervacija.getStatus() == StatusRezervacije.POTVRDJENA;
                boolean zauzetaTogDana = rezervacija.obuhvataDatum(dan);

                if (istiModel && potvrdjena && zauzetaTogDana) {
                    brojZauzetihPrimeraka++;
                }
            }

            if (brojZauzetihPrimeraka >= brojPrimeraka) {
                return false;
            }

            dan = dan.plusDays(1);
        }

        return true;
    }

    public boolean napraviZahtevZaRezervaciju(Klijent klijent, ModelVozila modelVozila,
                                               LocalDate datumOd, LocalDate datumDo) {
        if (klijent == null || modelVozila == null) {
            return false;
        }

        if (modelVozilaRepozitorijum.pronadjiPoId(modelVozila.getId()) == null) {
            return false;
        }

        if (!validanPeriodZaNoviZahtev(datumOd, datumDo)) {
            return false;
        }

        if (!klijent.vazecaDozvola()) {
            return false;
        }

        if (!daLiJeModelDostupan(modelVozila, datumOd, datumDo)) {
            return false;
        }

        Rezervacija rezervacija = new Rezervacija(
                rezervacijaRepozitorijum.sledeciId(),
                klijent,
                modelVozila,
                datumOd,
                datumDo,
                StatusRezervacije.NA_CEKANJU,
                0,
                0,
                0,
                0
        );

        rezervacijaRepozitorijum.dodaj(rezervacija);
        return true;
    }

    public boolean potvrdiRezervaciju(Korisnik ulogovaniKorisnik, int rezervacijaId) {
        if (!(ulogovaniKorisnik instanceof Agent)) {
            return false;
        }

        Rezervacija rezervacija = rezervacijaRepozitorijum.pronadjiPoId(rezervacijaId);

        if (rezervacija == null || !rezervacija.jeNaCekanju()) {
            return false;
        }

        if (!rezervacija.getDatumOd().isAfter(LocalDate.now())) {
            rezervacija.setStatus(StatusRezervacije.ODBIJENA);
            rezervacijaRepozitorijum.azuriraj(rezervacija);
            return false;
        }

        if (!daLiJeModelDostupan(rezervacija.getModelVozila(),
                rezervacija.getDatumOd(), rezervacija.getDatumDo())) {
            return false;
        }

        rezervacija.setStatus(StatusRezervacije.POTVRDJENA);
        rezervacijaRepozitorijum.azuriraj(rezervacija);
        return true;
    }

    public boolean odbijRezervaciju(Korisnik ulogovaniKorisnik, int rezervacijaId) {
        if (!(ulogovaniKorisnik instanceof Agent)) {
            return false;
        }

        Rezervacija rezervacija = rezervacijaRepozitorijum.pronadjiPoId(rezervacijaId);

        if (rezervacija == null || !rezervacija.jeNaCekanju()) {
            return false;
        }

        rezervacija.setStatus(StatusRezervacije.ODBIJENA);
        rezervacijaRepozitorijum.azuriraj(rezervacija);
        return true;
    }

    public boolean otkaziRezervaciju(Klijent klijent, int rezervacijaId) {
        if (klijent == null) {
            return false;
        }

        Rezervacija rezervacija = rezervacijaRepozitorijum.pronadjiPoId(rezervacijaId);

        if (rezervacija == null || !rezervacija.pripadaKlijentu(klijent)) {
            return false;
        }

        if (!rezervacija.mozeDaSeOtkaze()) {
            return false;
        }

        rezervacija.setStatus(StatusRezervacije.OTKAZANA);
        rezervacijaRepozitorijum.azuriraj(rezervacija);
        return true;
    }

    public void odbijIstekleRezervacije() {
        LocalDate danas = LocalDate.now();

        for (Rezervacija rezervacija : rezervacijaRepozitorijum.ucitajSve()) {
            boolean naCekanju = rezervacija.jeNaCekanju();
            boolean datumPocetkaStigao = !rezervacija.getDatumOd().isAfter(danas);

            if (naCekanju && datumPocetkaStigao) {
                rezervacija.setStatus(StatusRezervacije.ODBIJENA);
                rezervacijaRepozitorijum.azuriraj(rezervacija);
            }
        }
    }

    private boolean validanPeriodZaNoviZahtev(LocalDate datumOd, LocalDate datumDo) {
        if (!validanPeriod(datumOd, datumDo)) {
            return false;
        }

        LocalDate sutra = LocalDate.now().plusDays(1);
        return !datumOd.isBefore(sutra);
    }

    private boolean validanPeriod(LocalDate datumOd, LocalDate datumDo) {
        return datumOd != null && datumDo != null && !datumDo.isBefore(datumOd);
    }

    public ArrayList<ModelVozila> ucitajModeleVozila() {
        return modelVozilaRepozitorijum.ucitajSve();
    }

    public ArrayList<Rezervacija> ucitajSveRezervacije() {
        odbijIstekleRezervacije();
        return rezervacijaRepozitorijum.ucitajSve();
    }

    public ArrayList<Rezervacija> ucitajRezervacijeKlijenta(Klijent klijent) {
        ArrayList<Rezervacija> rezultat = new ArrayList<>();

        for (Rezervacija rezervacija : ucitajSveRezervacije()) {
            if (rezervacija.pripadaKlijentu(klijent)) {
                rezultat.add(rezervacija);
            }
        }

        return rezultat;
    }

}
