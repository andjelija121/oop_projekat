package model;

import enums.NivoSpreme;
import enums.Pol;

public abstract class Zaposleni extends Korisnik {
    private NivoSpreme nivoSpreme;
    private int godineStaza;
    private double osnova;

    public Zaposleni(String ime, String prezime, Pol pol, String datumRodjenja, String telefon, String adresa,
                     String korisnickoIme, String lozinka, NivoSpreme nivoSpreme, int godineStaza,
                     double osnova) {
        super(ime, prezime, pol, datumRodjenja, telefon, adresa, korisnickoIme, lozinka);
        this.nivoSpreme = nivoSpreme;
        this.godineStaza = godineStaza;
        this.osnova = osnova;
    }

    public NivoSpreme getNivoSpreme() {
        return nivoSpreme;
    }

    public void setNivoSpreme(NivoSpreme nivoSpreme) {
        this.nivoSpreme = nivoSpreme;
    }

    public int getGodineStaza() {
        return godineStaza;
    }

    public void setGodineStaza(int godineStaza) {
        this.godineStaza = godineStaza;
    }

    public double getOsnova() {
        return osnova;
    }

    public void setOsnova(double osnova) {
        this.osnova = osnova;
    }

    public double getPlata() {
        return osnova * (nivoSpreme.getKoeficijent() + 0.004 * godineStaza);
    }
}
