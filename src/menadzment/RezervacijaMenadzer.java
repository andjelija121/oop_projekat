package menadzment;

import enums.StatusRezervacije;
import model.Agent;
import model.DodatnaUsluga;
import model.Klijent;
import model.Korisnik;
import model.ModelVozila;
import model.Rezervacija;
import model.RezervacijaUsluga;
import repozitorijum.ModelVozilaRepozitorijum;
import repozitorijum.IzdavanjeRepozitorijum;
import repozitorijum.RezervacijaRepozitorijum;
import repozitorijum.RezervacijaUslugaRepozitorijum;
import repozitorijum.VoziloRepozitorijum;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import enums.KategorijaVozila;

public class RezervacijaMenadzer {
    private RezervacijaRepozitorijum rezervacijaRepozitorijum;
    private VoziloRepozitorijum voziloRepozitorijum;
    private ModelVozilaRepozitorijum modelVozilaRepozitorijum;
    private RezervacijaUslugaRepozitorijum rezervacijaUslugaRepozitorijum;
    private IzdavanjeRepozitorijum izdavanjeRepozitorijum;
    private PretplataMenadzer pretplataMenadzer;

    public RezervacijaMenadzer(RezervacijaRepozitorijum rezervacijaRepozitorijum,
                              VoziloRepozitorijum voziloRepozitorijum,
                              ModelVozilaRepozitorijum modelVozilaRepozitorijum) {
        this(rezervacijaRepozitorijum, voziloRepozitorijum, modelVozilaRepozitorijum,
                new RezervacijaUslugaRepozitorijum());
    }

    public RezervacijaMenadzer(RezervacijaRepozitorijum rezervacijaRepozitorijum,
                              VoziloRepozitorijum voziloRepozitorijum,
                              ModelVozilaRepozitorijum modelVozilaRepozitorijum,
                              RezervacijaUslugaRepozitorijum rezervacijaUslugaRepozitorijum) {
        this.rezervacijaRepozitorijum = rezervacijaRepozitorijum;
        this.voziloRepozitorijum = voziloRepozitorijum;
        this.modelVozilaRepozitorijum = modelVozilaRepozitorijum;
        this.rezervacijaUslugaRepozitorijum = rezervacijaUslugaRepozitorijum;
    }

    public void setIzdavanjeRepozitorijum(IzdavanjeRepozitorijum izdavanjeRepozitorijum) {
        this.izdavanjeRepozitorijum = izdavanjeRepozitorijum;
    }

    public void setPretplataMenadzer(PretplataMenadzer pretplataMenadzer) {
        this.pretplataMenadzer = pretplataMenadzer;
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

    public Rezervacija napraviZahtevZaRezervaciju(Klijent klijent, ModelVozila modelVozila,
                                               LocalDate datumOd, LocalDate datumDo,
                                               double cenaNajma, double cenaDodatnihUsluga,
                                               double cenaUkupno) {
        if (klijent == null || modelVozila == null) {
            return null;
        }

        if (modelVozilaRepozitorijum.pronadjiPoId(modelVozila.getId()) == null) {
            return null;
        }

        if (!validanPeriodZaNoviZahtev(datumOd, datumDo)) {
            return null;
        }

        if (!klijent.vazecaDozvola()) {
            return null;
        }

        if (pretplataMenadzer != null && !pretplataMenadzer.imaAktivnuPretplatu(klijent)) {
            return null;
        }

        if (klijentImaZabranuRezervisanja(klijent)) {
            return null;
        }

        if (!daLiJeModelDostupan(modelVozila, datumOd, datumDo)) {
            return null;
        }

        Rezervacija rezervacija = new Rezervacija(
                rezervacijaRepozitorijum.sledeciId(),
                klijent,
                modelVozila,
                datumOd,
                datumDo,
                StatusRezervacije.NA_CEKANJU,
                cenaNajma,
                cenaDodatnihUsluga,
                0,
                cenaUkupno
        );

        rezervacijaRepozitorijum.dodaj(rezervacija);
        return rezervacija;
    }

    public boolean evidentirajDodatnuUsluguRezervacije(int rezervacijaId, DodatnaUsluga dodatnaUsluga,
                                                       int kolicina, double cenaPoJedinici) {
        return evidentirajDodatnuUslugu(rezervacijaId, dodatnaUsluga, kolicina, cenaPoJedinici, false);
    }

    public boolean dodajDodatnuUsluguNaRezervaciju(Korisnik ulogovaniKorisnik, int rezervacijaId,
                                                   DodatnaUsluga dodatnaUsluga, int kolicina,
                                                   double cenaPoJedinici) {
        if (!(ulogovaniKorisnik instanceof Agent)) {
            return false;
        }

        Rezervacija rezervacija = rezervacijaRepozitorijum.pronadjiPoId(rezervacijaId);
        if (rezervacija == null || rezervacija.getStatus() != StatusRezervacije.POTVRDJENA) {
            return false;
        }

        return evidentirajDodatnuUslugu(rezervacijaId, dodatnaUsluga, kolicina, cenaPoJedinici, true);
    }

    private boolean evidentirajDodatnuUslugu(int rezervacijaId, DodatnaUsluga dodatnaUsluga,
                                             int kolicina, double cenaPoJedinici, boolean azurirajCenu) {
        Rezervacija rezervacija = rezervacijaRepozitorijum.pronadjiPoId(rezervacijaId);

        if (rezervacija == null || dodatnaUsluga == null || kolicina <= 0 || cenaPoJedinici < 0) {
            return false;
        }

        double ukupno = cenaPoJedinici * kolicina;
        RezervacijaUsluga rezervacijaUsluga = new RezervacijaUsluga(
                rezervacijaUslugaRepozitorijum.sledeciId(),
                rezervacijaId,
                dodatnaUsluga,
                kolicina,
                cenaPoJedinici,
                ukupno
        );

        rezervacijaUslugaRepozitorijum.dodaj(rezervacijaUsluga);

        if (azurirajCenu) {
            rezervacija.setCenaDodatnihUsluga(rezervacija.getCenaDodatnihUsluga() + ukupno);
            rezervacija.setCenaUkupno(rezervacija.getCenaUkupno() + ukupno);
            rezervacijaRepozitorijum.azuriraj(rezervacija);
        }

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
        rezervacija.setVremeOtkazivanja(LocalDateTime.now());
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

            boolean potvrdjena = rezervacija.getStatus() == StatusRezervacije.POTVRDJENA;
            boolean datumPocetkaProsao = rezervacija.getDatumOd().isBefore(danas);
            boolean nijePreuzeta = izdavanjeRepozitorijum != null
                    && izdavanjeRepozitorijum.pronadjiPoRezervaciji(rezervacija.getId()) == null;

            if (potvrdjena && datumPocetkaProsao && nijePreuzeta) {
                rezervacija.setStatus(StatusRezervacije.OTKAZANA);
                rezervacija.setVremeOtkazivanja(LocalDateTime.now());
                rezervacijaRepozitorijum.azuriraj(rezervacija);
            }
        }
    }

    public boolean klijentImaZabranuRezervisanja(Klijent klijent) {
        if (klijent == null) {
            return false;
        }

        LocalDateTime sada = LocalDateTime.now();
        for (Rezervacija rezervacija : rezervacijaRepozitorijum.ucitajSve()) {
            if (!rezervacija.pripadaKlijentu(klijent)
                    || rezervacija.getStatus() != StatusRezervacije.OTKAZANA
                    || rezervacija.getVremeOtkazivanja() == null) {
                continue;
            }

            if (rezervacija.getVremeOtkazivanja().plusHours(24).isAfter(sada)) {
                return true;
            }
        }

        return false;
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

    public ArrayList<ModelVozila> filtrirajModele(String nazivModela, String proizvodjac,
                                                  KategorijaVozila kategorijaVozila) {
        ArrayList<ModelVozila> rezultat = new ArrayList<>();

        for (ModelVozila modelVozila : modelVozilaRepozitorijum.ucitajSve()) {
            if (!poklapaNaziv(modelVozila, nazivModela)) {
                continue;
            }

            if (!poklapaProizvodjaca(modelVozila, proizvodjac)) {
                continue;
            }

            if (kategorijaVozila != null && modelVozila.getKategorijaVozila() != kategorijaVozila) {
                continue;
            }

            rezultat.add(modelVozila);
        }

        return rezultat;
    }

    private boolean poklapaNaziv(ModelVozila modelVozila, String nazivModela) {
        if (nazivModela == null || nazivModela.isBlank()) {
            return true;
        }

        return modelVozila.getNaziv().toLowerCase().contains(nazivModela.trim().toLowerCase());
    }

    private boolean poklapaProizvodjaca(ModelVozila modelVozila, String proizvodjac) {
        if (proizvodjac == null || proizvodjac.isBlank()) {
            return true;
        }

        return modelVozila.getProizvodjac().toLowerCase().contains(proizvodjac.trim().toLowerCase());
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

    public ArrayList<RezervacijaUsluga> ucitajDodatneUslugeRezervacije(int rezervacijaId) {
        return rezervacijaUslugaRepozitorijum.pronadjiPoRezervaciji(rezervacijaId);
    }

}
