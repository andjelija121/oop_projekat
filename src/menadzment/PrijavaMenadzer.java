package menadzment;

import model.Korisnik;
import repozitorijum.KorisnikRepozitorijum;

public class PrijavaMenadzer {
    private KorisnikRepozitorijum korisnikRepozitorijum;

    public PrijavaMenadzer(KorisnikRepozitorijum korisnikRepozitorijum) {
        this.korisnikRepozitorijum = korisnikRepozitorijum;
    }

    public Korisnik login(String korisnickoIme, String lozinka) {
        for (Korisnik korisnik : korisnikRepozitorijum.ucitajSve()) {
            if (korisnik.getKorisnickoIme().equals(korisnickoIme) && korisnik.getLozinka().equals(lozinka)) {
                return korisnik;
            }
        }

        return null;
    }
}
