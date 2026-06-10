package model;

import enums.NivoSpreme;
import enums.Pol;

public class Agent extends Zaposleni {
    public Agent(String ime, String prezime, Pol pol, String datumRodjenja, String telefon, String adresa,
                 String korisnickoIme, String lozinka, NivoSpreme nivoSpreme, int godineStaza,
                 double osnova) {
        super(ime, prezime, pol, datumRodjenja, telefon, adresa, korisnickoIme, lozinka, nivoSpreme, godineStaza,
                osnova);
    }
}
