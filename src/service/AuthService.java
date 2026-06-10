package service;

import model.Korisnik;
import repository.KorisnikRepository;

public class AuthService {
    private KorisnikRepository korisnikRepository;

    public AuthService(KorisnikRepository korisnikRepository) {
        this.korisnikRepository = korisnikRepository;
    }

    public Korisnik login(String korisnickoIme, String lozinka) {
        for (Korisnik korisnik : korisnikRepository.ucitajSve()) {
            if (korisnik.getKorisnickoIme().equals(korisnickoIme) && korisnik.getLozinka().equals(lozinka)) {
                return korisnik;
            }
        }

        return null;
    }
}
