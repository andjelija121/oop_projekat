package model;

import enums.Pol;

public class Korisnik {
    private String ime;
    private String prezime;
    private Pol pol;
    private String datumRodjenja;
    private String telefon;
    private String adresa;
    private String korisnickoIme;
    private String lozinka;

    public Korisnik(String ime, String prezime, Pol pol, String datumRodjenja, String telefon, String adresa, String korisnickoIme, String lozinka) {
        this.ime = ime;
        this.prezime = prezime;
        this.pol = pol;
        this.datumRodjenja = datumRodjenja;
        this.telefon = telefon;
        this.adresa = adresa;
        this.korisnickoIme = korisnickoIme;
        this.lozinka = lozinka;
    }


    public String getIme() {
        return ime;
    }

    public void setIme(String ime) {
        this.ime = ime;
    }

    public String getPrezime() {
        return prezime;
    }

    public void setPrezime(String prezime) {
        this.prezime = prezime;
    }

    public Pol getPol() {
        return pol;
    }

    public void setPol(Pol pol) {
        this.pol = pol;
    }

    public String getDatumRodjenja() {
        return datumRodjenja;
    }

    public void setDatumRodjenja(String datumRodjenja) {
        this.datumRodjenja = datumRodjenja;
    }

    public String getTelefon() {
        return telefon;
    }

    public void setTelefon(String telefon) {
        this.telefon = telefon;
    }

    public String getAdresa() {
        return adresa;
    }

    public void setAdresa(String adresa) {
        this.adresa = adresa;
    }

    public String getKorisnickoIme() {
        return korisnickoIme;
    }

    public void setKorisnickoIme(String korisnickoIme) {
        this.korisnickoIme = korisnickoIme;
    }

    public void setLozinka(String lozinka) {
        this.lozinka = lozinka;
    }

    public String getLozinka() {
        return lozinka;
    }
}
