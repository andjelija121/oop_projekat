package repository;

import enums.KategorijaKlijenta;
import enums.Pol;
import enums.TipKorisnika;
import model.Klijent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class KlijentRepository {
    private String putanjaDoFajla;
    public KlijentRepository() {
        this.putanjaDoFajla = "src/data/korisnici.csv";
    }
    public KlijentRepository(String putanja){
        this.putanjaDoFajla = putanja;
    }

    public ArrayList<Klijent> ucitajSve(){
        ArrayList<Klijent> klijenti = new ArrayList<>();
        try{
            ArrayList<String> linije = new ArrayList<>(Files.readAllLines(Paths.get(this.putanjaDoFajla)));
            for (int i = 1; i < linije.size(); i++) {
                String linija = linije.get(i);

                Klijent korisnikIzFajla = napraviKlijenta(linija);

                if (korisnikIzFajla != null) {
                    klijenti.add(korisnikIzFajla);
                }
            }
        }catch (IOException e ){
            System.out.println("Greska prilikom citanja fajla: " + putanjaDoFajla);
        }
        return klijenti;
    }


    private Klijent napraviKlijenta(String linija){
        String[] delovi = linija.split(",");
        TipKorisnika tip = TipKorisnika.valueOf(delovi[1]);
        if (tip!=TipKorisnika.KLIJENT){
            return null;
        }

        String ime = delovi[2];
        String prezime = delovi[3];
        Pol pol = Pol.valueOf(delovi[4]);
        String datumRodjenja = delovi[5];
        String telefon = delovi[6];
        String adresa = delovi[7];
        String korisnickoIme = delovi[8];
        String lozinka = delovi[9];
        String datumDozvole = delovi[10];
        KategorijaKlijenta posebnaKategorija = KategorijaKlijenta.valueOf(delovi[11]);


        return new Klijent(ime,prezime,pol,datumRodjenja,telefon,adresa,korisnickoIme,lozinka,datumDozvole,posebnaKategorija);
    }

}
