package service;

import enums.NivoSpreme;
import enums.Pol;
import model.Administrator;
import model.Agent;
import model.Korisnik;
import repository.KorisnikRepository;

public class ZaposleniService {
    private KorisnikRepository korisnikRepository;

    public ZaposleniService(KorisnikRepository korisnikRepository) {
        this.korisnikRepository = korisnikRepository;
    }

    public boolean dodajAgenta(Korisnik ulogovaniKorisnik, String ime, String prezime, Pol pol, String datumRodjenja,
                              String telefon, String adresa, String korisnickoIme, String lozinka,
                              NivoSpreme nivoSpreme, int godineStaza, double osnova) {
        if (!(ulogovaniKorisnik instanceof Administrator)) {
            return false;
        }

        if (korisnikRepository.korisnickoImePostoji(korisnickoIme)) {
            return false;
        }

        Agent agent = new Agent(ime, prezime, pol, datumRodjenja, telefon, adresa, korisnickoIme, lozinka,
                nivoSpreme, godineStaza, osnova);
        korisnikRepository.dodaj(agent);
        return true;
    }

    public boolean dodajAdministratora(Korisnik ulogovaniKorisnik, String ime, String prezime, Pol pol,
                                       String datumRodjenja, String telefon, String adresa, String korisnickoIme,
                                       String lozinka, NivoSpreme nivoSpreme, int godineStaza, double osnova) {
        if (!(ulogovaniKorisnik instanceof Administrator)) {
            return false;
        }

        if (korisnikRepository.korisnickoImePostoji(korisnickoIme)) {
            return false;
        }

        Administrator administrator = new Administrator(ime, prezime, pol, datumRodjenja, telefon, adresa,
                korisnickoIme, lozinka, nivoSpreme, godineStaza, osnova);
        korisnikRepository.dodaj(administrator);
        return true;
    }
}
