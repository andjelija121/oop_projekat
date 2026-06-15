package model;

import enums.NivoSpreme;
import enums.Pol;

public class Administrator extends Zaposleni {
    public Administrator(String ime, String prezime, Pol pol, String datumRodjenja, String telefon, String adresa,
                         String korisnickoIme, String lozinka, NivoSpreme nivoSpreme, int godineStaza,
                         double osnova) {
        super(ime, prezime, pol, datumRodjenja, telefon, adresa, korisnickoIme, lozinka, nivoSpreme, godineStaza,
                osnova);
    }

    public Administrator(int id, String ime, String prezime, Pol pol, String datumRodjenja, String telefon, String adresa,
                         String korisnickoIme, String lozinka, NivoSpreme nivoSpreme, int godineStaza,
                         double osnova) {
        super(id, ime, prezime, pol, datumRodjenja, telefon, adresa, korisnickoIme, lozinka, nivoSpreme, godineStaza,
                osnova);
    }
}
