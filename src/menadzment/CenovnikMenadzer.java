package menadzment;

import enums.TipCene;
import model.Cenovnik;
import model.DodatnaUsluga;
import model.Klijent;
import model.ModelVozila;
import model.StavkaCenovnika;
import repozitorijum.CenovnikRepozitorijum;

import java.time.LocalDate;
import java.util.ArrayList;

public class CenovnikMenadzer {
    private CenovnikRepozitorijum cenovnikRepozitorijum;

    public CenovnikMenadzer(CenovnikRepozitorijum cenovnikRepozitorijum) {
        this.cenovnikRepozitorijum = cenovnikRepozitorijum;
    }

    public double izracunajCenuNajma(Klijent klijent, ModelVozila modelVozila,
                                     LocalDate datumOd, LocalDate datumDo,
                                     int brojDodatnihDana) {
        if (klijent == null || modelVozila == null || datumOd == null || datumDo == null
                || datumDo.isBefore(datumOd)) {
            return 0;
        }

        Cenovnik cenovnik = cenovnikRepozitorijum.pronadjiVazeciCenovnik(datumOd);
        if (cenovnik == null) {
            return 0;
        }

        int brojDana = izracunajBrojDana(datumOd, datumDo);
        if (!validanBrojDodatnihDana(brojDana, brojDodatnihDana)) {
            return 0;
        }

        int brojObicnihDana = brojDana - brojDodatnihDana;
        double cenaPoDanu = pronadjiVrednostNajma(cenovnik, modelVozila);
        double cenaNajma = cenaPoDanu * brojObicnihDana;
        double popust = pronadjiPopust(cenovnik, klijent);

        return cenaNajma - (cenaNajma * popust / 100);
    }

    public double izracunajCenuDodatnihUsluga(LocalDate datum,
                                              ArrayList<DodatnaUsluga> dodatneUsluge,
                                              int brojDodatnihDana) {
        if (datum == null || dodatneUsluge == null || brojDodatnihDana < 0) {
            return 0;
        }

        Cenovnik cenovnik = cenovnikRepozitorijum.pronadjiVazeciCenovnik(datum);
        if (cenovnik == null) {
            return 0;
        }

        double cenaProduzenogKoriscenja = pronadjiCenuProduzenogKoriscenja(cenovnik) * brojDodatnihDana;
        double cenaDodatnihUsluga = izracunajCenuDodatnihUsluga(cenovnik, dodatneUsluge);

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

        int osnovniBrojDana = 3;
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
}
