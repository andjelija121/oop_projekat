package menadzment;

import enums.KategorijaKlijenta;
import enums.Pol;
import model.Agent;
import model.Klijent;
import model.Korisnik;
import repozitorijum.KorisnikRepozitorijum;

public class KlijentMenadzer {
    private KorisnikRepozitorijum korisnikRepozitorijum;

    public KlijentMenadzer(KorisnikRepozitorijum korisnikRepozitorijum) {
        this.korisnikRepozitorijum = korisnikRepozitorijum;
    }

    public boolean dodajKlijenta(Korisnik ulogovaniKorisnik, String ime, String prezime, Pol pol,
                                String datumRodjenja, String telefon, String adresa, String email, String lozinka,
                                String datumDozvole, KategorijaKlijenta posebnaKategorija) {
        if (!(ulogovaniKorisnik instanceof Agent)) {
            return false;
        }

        if (korisnikRepozitorijum.korisnickoImePostoji(email)) {
            return false;
        }

        Klijent klijent = new Klijent(ime, prezime, pol, datumRodjenja, telefon, adresa, email, lozinka,
                datumDozvole, posebnaKategorija);
        korisnikRepozitorijum.dodaj(klijent);
        return true;
    }
}
