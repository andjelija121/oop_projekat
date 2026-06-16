package menadzment;

import enums.TipCene;
import enums.KategorijaKlijenta;
import enums.KategorijaVozila;
import model.Administrator;
import model.Cenovnik;
import model.DodatnaUsluga;
import model.Klijent;
import model.Korisnik;
import model.ModelVozila;
import model.StavkaCenovnika;
import repozitorijum.CenovnikRepozitorijum;
import repozitorijum.PodesavanjaRepozitorijum;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

public class CenovnikMenadzer {
    private CenovnikRepozitorijum cenovnikRepozitorijum;
    private PodesavanjaRepozitorijum podesavanjaRepozitorijum;

    public CenovnikMenadzer(CenovnikRepozitorijum cenovnikRepozitorijum) {
        this.cenovnikRepozitorijum = cenovnikRepozitorijum;
        this.podesavanjaRepozitorijum = new PodesavanjaRepozitorijum();
    }

    public double izracunajCenuNajma(Klijent klijent, ModelVozila modelVozila,
                                     LocalDate datumOd, LocalDate datumDo,
                                     int brojDodatnihDana) {
        if (klijent == null || modelVozila == null || datumOd == null || datumDo == null
                || datumDo.isBefore(datumOd)) {
            return 0;
        }

        int brojDana = izracunajBrojDana(datumOd, datumDo);
        if (!validanBrojDodatnihDana(brojDana, brojDodatnihDana)) {
            return 0;
        }

        int brojObicnihDana = brojDana - brojDodatnihDana;
        double ukupno = 0;

        for (int i = 0; i < brojObicnihDana; i++) {
            LocalDate datum = datumOd.plusDays(i);
            Cenovnik cenovnik = cenovnikRepozitorijum.pronadjiVazeciCenovnik(datum);
            if (cenovnik == null) {
                return 0;
            }

            double cenaPoDanu = pronadjiVrednostNajma(cenovnik, modelVozila);
            double popust = pronadjiPopust(cenovnik, klijent);
            ukupno += cenaPoDanu - (cenaPoDanu * popust / 100);
        }

        return ukupno;
    }

    public double izracunajCenuDodatnihUsluga(LocalDate datum,
                                              ArrayList<DodatnaUsluga> dodatneUsluge,
                                              int brojDodatnihDana) {
        if (datum == null || dodatneUsluge == null || brojDodatnihDana < 0) {
            return 0;
        }

        double cenaProduzenogKoriscenja = 0;
        for (int i = 0; i < brojDodatnihDana; i++) {
            LocalDate datumDodatnogDana = datum.plusDays(ucitajPodrazumevanoTrajanjeNajma() + i);
            Cenovnik cenovnik = cenovnikRepozitorijum.pronadjiVazeciCenovnik(datumDodatnogDana);
            if (cenovnik == null) {
                return 0;
            }

            cenaProduzenogKoriscenja += pronadjiCenuProduzenogKoriscenja(cenovnik);
        }

        Cenovnik cenovnikZaUsluge = cenovnikRepozitorijum.pronadjiVazeciCenovnik(datum);
        if (cenovnikZaUsluge == null) {
            return 0;
        }

        double cenaDodatnihUsluga = izracunajCenuDodatnihUsluga(cenovnikZaUsluge, dodatneUsluge);

        return cenaProduzenogKoriscenja + cenaDodatnihUsluga;
    }

    public double izracunajUkupnuCenuRezervacije(Klijent klijent, ModelVozila modelVozila,
                                                 LocalDate datumOd, LocalDate datumDo,
                                                 ArrayList<DodatnaUsluga> dodatneUsluge,
                                                 int brojDodatnihDana) {
        double cenaNajma = izracunajCenuNajma(klijent, modelVozila, datumOd, datumDo, brojDodatnihDana);
        double cenaDodatnihUsluga = izracunajCenuDodatnihUsluga(datumOd, dodatneUsluge, brojDodatnihDana);

        return cenaNajma + cenaDodatnihUsluga;
    }

    public double pronadjiCenuDodatneUsluge(LocalDate datum, DodatnaUsluga dodatnaUsluga) {
        if (datum == null || dodatnaUsluga == null) {
            return 0;
        }

        Cenovnik cenovnik = cenovnikRepozitorijum.pronadjiVazeciCenovnik(datum);
        if (cenovnik == null) {
            return 0;
        }

        for (StavkaCenovnika stavka : cenovnik.getStavke()) {
            if (stavka.getTipCene() == TipCene.DODATNA_USLUGA
                    && stavka.getDodatnaUsluga() != null
                    && stavka.getDodatnaUsluga().getId() == dodatnaUsluga.getId()) {
                return stavka.getVrednost();
            }
        }

        return 0;
    }

    public double izracunajKaznuKasnjenja(LocalDate datumPlaniranogVracanja, LocalDate datumStvarnogVracanja) {
        if (datumPlaniranogVracanja == null || datumStvarnogVracanja == null
                || !datumStvarnogVracanja.isAfter(datumPlaniranogVracanja)) {
            return 0;
        }

        int brojDanaKasnjenja = (int) ChronoUnit.DAYS.between(datumPlaniranogVracanja, datumStvarnogVracanja);
        double ukupno = 0;

        for (int i = 1; i <= brojDanaKasnjenja; i++) {
            LocalDate datumKasnjenja = datumPlaniranogVracanja.plusDays(i);
            Cenovnik cenovnik = cenovnikRepozitorijum.pronadjiVazeciCenovnik(datumKasnjenja);
            if (cenovnik == null) {
                return 0;
            }

            ukupno += pronadjiKaznuKasnjenja(cenovnik);
        }

        return ukupno;
    }

    private int izracunajBrojDana(LocalDate datumOd, LocalDate datumDo) {
        int brojDana = 1;
        LocalDate datum = datumOd;

        while (datum.isBefore(datumDo)) {
            brojDana++;
            datum = datum.plusDays(1);
        }

        return brojDana;
    }

    private boolean validanBrojDodatnihDana(int brojDana, int brojDodatnihDana) {
        if (brojDodatnihDana < 0) {
            return false;
        }

        int osnovniBrojDana = ucitajPodrazumevanoTrajanjeNajma();
        int ocekivaniDodatniDani = brojDana - osnovniBrojDana;

        if (ocekivaniDodatniDani < 0) {
            ocekivaniDodatniDani = 0;
        }

        return brojDodatnihDana == ocekivaniDodatniDani;
    }

    private double pronadjiVrednostNajma(Cenovnik cenovnik, ModelVozila modelVozila) {
        for (StavkaCenovnika stavka : cenovnik.getStavke()) {
            if (stavka.getTipCene() == TipCene.NAJAM_PO_DANU
                    && stavka.getKategorijaVozila() == modelVozila.getKategorijaVozila()) {
                return stavka.getVrednost();
            }
        }

        return 0;
    }

    private double pronadjiPopust(Cenovnik cenovnik, Klijent klijent) {
        if (klijent.getPosebnaKategorija() == null) {
            return 0;
        }

        for (StavkaCenovnika stavka : cenovnik.getStavke()) {
            if (stavka.getTipCene() == TipCene.POPUST_KLIJENTA
                    && stavka.getKategorijaKlijenta() == klijent.getPosebnaKategorija()) {
                return stavka.getVrednost();
            }
        }

        return 0;
    }

    private double izracunajCenuDodatnihUsluga(Cenovnik cenovnik, ArrayList<DodatnaUsluga> dodatneUsluge) {
        double ukupno = 0;

        for (DodatnaUsluga dodatnaUsluga : dodatneUsluge) {
            for (StavkaCenovnika stavka : cenovnik.getStavke()) {
                if (stavka.getTipCene() == TipCene.DODATNA_USLUGA
                        && stavka.getDodatnaUsluga() != null
                        && stavka.getDodatnaUsluga().getId() == dodatnaUsluga.getId()) {
                    ukupno += stavka.getVrednost();
                }
            }
        }

        return ukupno;
    }

    private double pronadjiCenuProduzenogKoriscenja(Cenovnik cenovnik) {
        for (StavkaCenovnika stavka : cenovnik.getStavke()) {
            if (stavka.getTipCene() == TipCene.DODATNA_USLUGA
                    && stavka.getDodatnaUsluga() != null
                    && stavka.getDodatnaUsluga().getNaziv().equals("PRODUZENO_KORISCENJE")) {
                return stavka.getVrednost();
            }
        }

        return 0;
    }

    private double pronadjiKaznuKasnjenja(Cenovnik cenovnik) {
        for (StavkaCenovnika stavka : cenovnik.getStavke()) {
            if (stavka.getTipCene() == TipCene.KAZNA_KASNJENJA) {
                return stavka.getVrednost();
            }
        }

        return 0;
    }

    public double pronadjiCenuGodisnjePretplate(LocalDate datum) {
        if (datum == null) {
            return 0;
        }

        Cenovnik cenovnik = cenovnikRepozitorijum.pronadjiVazeciCenovnik(datum);
        if (cenovnik == null) {
            return 0;
        }

        for (StavkaCenovnika stavka : cenovnik.getStavke()) {
            if (stavka.getTipCene() == TipCene.GODISNJA_PRETPLATA) {
                return stavka.getVrednost();
            }
        }

        return 0;
    }

    public int ucitajPodrazumevanoTrajanjeNajma() {
        return podesavanjaRepozitorijum.ucitajPodrazumevanoTrajanjeNajma();
    }

    public boolean promeniPodrazumevanoTrajanjeNajma(Korisnik korisnik, int brojDana) {
        if (!(korisnik instanceof Administrator) || brojDana <= 0) {
            return false;
        }

        podesavanjaRepozitorijum.sacuvajPodrazumevanoTrajanjeNajma(brojDana);
        return true;
    }

    public ArrayList<Cenovnik> ucitajCenovnike() {
        ArrayList<Cenovnik> cenovnici = cenovnikRepozitorijum.ucitajSve();
        cenovnici.sort(Comparator.comparing(Cenovnik::getDatumOd));
        return cenovnici;
    }

    public boolean dodajNoviCenovnik(Korisnik korisnik, LocalDate datumOd, LocalDate datumDo,
                                     double cenaGodisnjePretplate,
                                     HashMap<KategorijaVozila, Double> ceneNajma,
                                     HashMap<KategorijaKlijenta, Double> popusti,
                                     HashMap<DodatnaUsluga, Double> ceneDodatnihUsluga,
                                     double kaznaKasnjenja) {
        if (!(korisnik instanceof Administrator) || datumOd == null || datumDo == null
                || datumDo.isBefore(datumOd) || cenaGodisnjePretplate < 0 || kaznaKasnjenja < 0
                || ceneNajma == null || popusti == null || ceneDodatnihUsluga == null) {
            return false;
        }

        ArrayList<Cenovnik> cenovnici = ucitajCenovnike();
        Cenovnik prethodni = cenovnici.isEmpty() ? null : cenovnici.get(cenovnici.size() - 1);

        if (prethodni != null && !datumOd.isAfter(prethodni.getDatumOd())) {
            return false;
        }

        if (prethodni != null && !datumOd.isAfter(prethodni.getDatumDo())) {
            prethodni.setDatumDo(datumOd.minusDays(1));
        }

        Cenovnik novi = new Cenovnik(cenovnikRepozitorijum.sledeciId(), datumOd, datumDo);
        novi.dodajStavku(new StavkaCenovnika(TipCene.GODISNJA_PRETPLATA, cenaGodisnjePretplate,
                null, null, null));

        for (KategorijaVozila kategorija : KategorijaVozila.values()) {
            Double cena = ceneNajma.get(kategorija);
            if (cena == null || cena < 0) {
                return false;
            }

            novi.dodajStavku(new StavkaCenovnika(TipCene.NAJAM_PO_DANU, cena, kategorija, null, null));
        }

        for (KategorijaKlijenta kategorija : KategorijaKlijenta.values()) {
            Double popust = popusti.get(kategorija);
            if (popust == null || popust < 0 || popust > 100) {
                return false;
            }

            novi.dodajStavku(new StavkaCenovnika(TipCene.POPUST_KLIJENTA, popust, null, kategorija, null));
        }

        for (DodatnaUsluga usluga : ceneDodatnihUsluga.keySet()) {
            Double cena = ceneDodatnihUsluga.get(usluga);
            if (cena == null || cena < 0) {
                return false;
            }

            novi.dodajStavku(new StavkaCenovnika(TipCene.DODATNA_USLUGA, cena, null, null, usluga));
        }

        novi.dodajStavku(new StavkaCenovnika(TipCene.KAZNA_KASNJENJA, kaznaKasnjenja, null, null, null));
        cenovnici.add(novi);
        cenovnikRepozitorijum.sacuvajSve(cenovnici);
        return true;
    }

    public boolean obrisiCenovnik(Korisnik korisnik, int cenovnikId) {
        if (!(korisnik instanceof Administrator)) {
            return false;
        }

        ArrayList<Cenovnik> cenovnici = ucitajCenovnike();
        if (cenovnici.size() <= 1) {
            return false;
        }

        boolean obrisan = false;
        for (int i = 0; i < cenovnici.size(); i++) {
            if (cenovnici.get(i).getId() == cenovnikId) {
                cenovnici.remove(i);
                obrisan = true;
                break;
            }
        }

        if (!obrisan) {
            return false;
        }

        cenovnikRepozitorijum.sacuvajSve(cenovnici);
        return true;
    }
}
