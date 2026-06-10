package service;

import enums.KategorijaKlijenta;
import enums.Pol;
import model.Agent;
import model.Klijent;
import model.Korisnik;
import repository.KorisnikRepository;

public class KlijentService {
    private KorisnikRepository korisnikRepository;

    public KlijentService(KorisnikRepository korisnikRepository) {
        this.korisnikRepository = korisnikRepository;
    }

    public boolean dodajKlijenta(Korisnik ulogovaniKorisnik, String ime, String prezime, Pol pol,
                                String datumRodjenja, String telefon, String adresa, String email, String lozinka,
                                String datumDozvole, KategorijaKlijenta posebnaKategorija) {
        if (!(ulogovaniKorisnik instanceof Agent)) {
            return false;
        }

        if (korisnikRepository.korisnickoImePostoji(email)) {
            return false;
        }

        Klijent klijent = new Klijent(ime, prezime, pol, datumRodjenja, telefon, adresa, email, lozinka,
                datumDozvole, posebnaKategorija);
        korisnikRepository.dodaj(klijent);
        return true;
    }
}
