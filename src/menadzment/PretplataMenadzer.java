package menadzment;

import enums.StatusPretplate;
import model.Agent;
import model.Izdavanje;
import model.Klijent;
import model.Korisnik;
import model.Pretplata;
import model.ZahtevPretplate;
import repozitorijum.IzdavanjeRepozitorijum;
import repozitorijum.PretplataRepozitorijum;
import repozitorijum.ZahtevPretplateRepozitorijum;

import java.time.LocalDate;
import java.util.ArrayList;

public class PretplataMenadzer {
    private PretplataRepozitorijum pretplataRepozitorijum;
    private ZahtevPretplateRepozitorijum zahtevPretplateRepozitorijum;
    private IzdavanjeRepozitorijum izdavanjeRepozitorijum;
    private CenovnikMenadzer cenovnikMenadzer;

    public PretplataMenadzer(PretplataRepozitorijum pretplataRepozitorijum,
                             ZahtevPretplateRepozitorijum zahtevPretplateRepozitorijum,
                             IzdavanjeRepozitorijum izdavanjeRepozitorijum,
                             CenovnikMenadzer cenovnikMenadzer) {
        this.pretplataRepozitorijum = pretplataRepozitorijum;
        this.zahtevPretplateRepozitorijum = zahtevPretplateRepozitorijum;
        this.izdavanjeRepozitorijum = izdavanjeRepozitorijum;
        this.cenovnikMenadzer = cenovnikMenadzer;
    }

    public boolean imaAktivnuPretplatu(Klijent klijent) {
        if (klijent == null) {
            return false;
        }

        LocalDate danas = LocalDate.now();
        for (Pretplata pretplata : pretplataRepozitorijum.pronadjiPoKlijentu(klijent.getId())) {
            if (pretplata.aktivnaNaDatum(danas)) {
                return true;
            }
        }

        return false;
    }

    public Pretplata pronadjiPretplatuKlijenta(Klijent klijent) {
        if (klijent == null) {
            return null;
        }

        Pretplata najnovija = null;
        for (Pretplata pretplata : pretplataRepozitorijum.pronadjiPoKlijentu(klijent.getId())) {
            if (najnovija == null || pretplata.getDatumKraja().isAfter(najnovija.getDatumKraja())) {
                najnovija = pretplata;
            }
        }

        return najnovija;
    }

    public ZahtevPretplate napraviZahtev(Klijent klijent) {
        if (klijent == null || imaAktivnuPretplatu(klijent) || imaZahtevNaCekanju(klijent)) {
            return null;
        }

        ZahtevPretplate zahtev = new ZahtevPretplate(
                zahtevPretplateRepozitorijum.sledeciId(),
                klijent,
                null,
                LocalDate.now(),
                StatusPretplate.NA_CEKANJU
        );
        zahtevPretplateRepozitorijum.dodaj(zahtev);
        return zahtev;
    }

    public boolean odobriZahtev(Korisnik ulogovaniKorisnik, int zahtevId) {
        if (!(ulogovaniKorisnik instanceof Agent)) {
            return false;
        }

        ZahtevPretplate zahtev = zahtevPretplateRepozitorijum.pronadjiPoId(zahtevId);
        if (zahtev == null || !zahtev.naCekanju() || zahtev.getKlijent() == null) {
            return false;
        }

        zahtev.setAgent((Agent) ulogovaniKorisnik);
        if (brojKasnjenja(zahtev.getKlijent()) > 5) {
            zahtev.setStatus(StatusPretplate.ODBIJENA);
            zahtevPretplateRepozitorijum.azuriraj(zahtev);
            return false;
        }

        LocalDate datumPocetka = LocalDate.now();
        LocalDate datumKraja = datumPocetka.plusYears(1);
        double cena = cenovnikMenadzer.pronadjiCenuGodisnjePretplate(datumPocetka);
        Pretplata pretplata = new Pretplata(pretplataRepozitorijum.sledeciId(), zahtev.getKlijent(),
                datumPocetka, datumKraja, StatusPretplate.AKTIVNA, cena);

        zahtev.setStatus(StatusPretplate.ODOBREN);
        zahtevPretplateRepozitorijum.azuriraj(zahtev);
        pretplataRepozitorijum.dodaj(pretplata);
        return true;
    }

    public boolean odbijZahtev(Korisnik ulogovaniKorisnik, int zahtevId) {
        if (!(ulogovaniKorisnik instanceof Agent)) {
            return false;
        }

        ZahtevPretplate zahtev = zahtevPretplateRepozitorijum.pronadjiPoId(zahtevId);
        if (zahtev == null || !zahtev.naCekanju()) {
            return false;
        }

        zahtev.setAgent((Agent) ulogovaniKorisnik);
        zahtev.setStatus(StatusPretplate.ODBIJENA);
        zahtevPretplateRepozitorijum.azuriraj(zahtev);
        return true;
    }

    public ArrayList<Pretplata> ucitajSvePretplate() {
        return pretplataRepozitorijum.ucitajSve();
    }

    public ArrayList<ZahtevPretplate> ucitajSveZahteve() {
        return zahtevPretplateRepozitorijum.ucitajSve();
    }

    public ArrayList<ZahtevPretplate> ucitajZahteveKlijenta(Klijent klijent) {
        ArrayList<ZahtevPretplate> rezultat = new ArrayList<>();

        if (klijent == null) {
            return rezultat;
        }

        for (ZahtevPretplate zahtev : zahtevPretplateRepozitorijum.ucitajSve()) {
            if (zahtev.getKlijent() != null && zahtev.getKlijent().getId() == klijent.getId()) {
                rezultat.add(zahtev);
            }
        }

        return rezultat;
    }

    private boolean imaZahtevNaCekanju(Klijent klijent) {
        for (ZahtevPretplate zahtev : ucitajZahteveKlijenta(klijent)) {
            if (zahtev.naCekanju()) {
                return true;
            }
        }

        return false;
    }

    private int brojKasnjenja(Klijent klijent) {
        int brojKasnjenja = 0;

        for (Izdavanje izdavanje : izdavanjeRepozitorijum.ucitajSve()) {
            if (izdavanje.getRezervacija() == null
                    || !izdavanje.getRezervacija().pripadaKlijentu(klijent)
                    || izdavanje.getDatumVracanjaStvarno() == null) {
                continue;
            }

            if (izdavanje.getDatumVracanjaStvarno().isAfter(izdavanje.getDatumVracanjaPlanirano())) {
                brojKasnjenja++;
            }
        }

        return brojKasnjenja;
    }
}
