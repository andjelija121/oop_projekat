package model;

import enums.KategorijaKlijenta;
import enums.Pol;

import java.time.LocalDate;

public class Klijent extends Korisnik {
    private String datumDozvole;
    private KategorijaKlijenta posebnaKategorija;

    public Klijent(String ime,String prezime, Pol pol,String datumRodjenja,String telefon,String adresa,String korisnickoIme,String lozinka,String vozacka){
        super(ime,prezime,pol,datumRodjenja,telefon,adresa,korisnickoIme,lozinka);
        this.datumDozvole = vozacka;
    }

    public Klijent(int id, String ime, String prezime, Pol pol, String datumRodjenja, String telefon, String adresa,
                   String korisnickoIme, String lozinka, String vozacka, KategorijaKlijenta posebnaKategorija) {
        super(id, ime, prezime, pol, datumRodjenja, telefon, adresa, korisnickoIme, lozinka);
        this.datumDozvole = vozacka;
        this.posebnaKategorija = posebnaKategorija;
    }

    public Klijent(String ime,String prezime, Pol pol,String datumRodjenja,String telefon,String adresa,String korisnickoIme,String lozinka,String vozacka,KategorijaKlijenta posebnaKategorija){
        super(ime,prezime,pol,datumRodjenja,telefon,adresa,korisnickoIme,lozinka);
        this.datumDozvole = vozacka;
        this.posebnaKategorija= posebnaKategorija;
    }

    public String getDatumDozvole() {
        return datumDozvole;
    }

    public void setDatumDozvole(String vozackaDozvola) {
        this.datumDozvole = vozackaDozvola;
    }

    public KategorijaKlijenta getPosebnaKategorija() {
        return posebnaKategorija;
    }

    public void setPosebnaKategorija(KategorijaKlijenta posebnaKategorija) {
        this.posebnaKategorija = posebnaKategorija;
    }

    public boolean vazecaDozvola(){
        LocalDate trenutniDatum = LocalDate.now();
        LocalDate datumIzdavanja = LocalDate.parse(datumDozvole);
        int razlikaUGodinama = trenutniDatum.getYear() - datumIzdavanja.getYear();

        if (razlikaUGodinama > 2) {
            return true;
        }

        if (razlikaUGodinama < 2) {
            return false;
        }

        if (trenutniDatum.getMonthValue() > datumIzdavanja.getMonthValue()) {
            return true;
        }

        if (trenutniDatum.getMonthValue() < datumIzdavanja.getMonthValue()) {
            return false;
        }

        return trenutniDatum.getDayOfMonth() >= datumIzdavanja.getDayOfMonth();
    }


    @Override
    public String toString() {
        return "Klijent{" +
                "vozackaDozvola='" + datumDozvole + '\'' +
                ", posebnaKategorija='" + posebnaKategorija + '\'' +
                '}';
    }
}
