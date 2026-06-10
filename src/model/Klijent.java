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
        LocalDate trenutni_datum = LocalDate.now();
        LocalDate datumVozacke = LocalDate.parse(this.getDatumDozvole());
        if ((trenutni_datum.getYear()-datumVozacke.getYear())>1 || trenutni_datum.getMonthValue()>datumVozacke.getMonthValue()){
            return true;
        }
        else return trenutni_datum.getYear() - datumVozacke.getYear() > 2;
    }


    @Override
    public String toString() {
        return "Klijent{" +
                "vozackaDozvola='" + datumDozvole + '\'' +
                ", posebnaKategorija='" + posebnaKategorija + '\'' +
                '}';
    }
}
