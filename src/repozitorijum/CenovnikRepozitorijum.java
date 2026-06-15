package repozitorijum;

import enums.KategorijaKlijenta;
import enums.KategorijaVozila;
import enums.TipCene;
import model.Cenovnik;
import model.DodatnaUsluga;
import model.StavkaCenovnika;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;

public class CenovnikRepozitorijum {
    private String putanjaDoFajla;
    private DodatnaUslugaRepozitorijum dodatnaUslugaRepozitorijum;

    public CenovnikRepozitorijum() {
        this.putanjaDoFajla = "src/fajlovi/cenovnik.csv";
        this.dodatnaUslugaRepozitorijum = new DodatnaUslugaRepozitorijum();
    }
    public ArrayList<Cenovnik> ucitajSve() {
        ArrayList<Cenovnik> cenovnici = new ArrayList<>();
        try{
            ArrayList<String> linije = new ArrayList<>(Files.readAllLines(Path.of(putanjaDoFajla)));
            for (int i=1;i<linije.size();i++){

                String[] delovi=linije.get(i).trim().split(",",-1);
                LocalDate datumOd = LocalDate.parse(delovi[6]);
                LocalDate datumDo = LocalDate.parse(delovi[7]);

                Cenovnik noviCenovnik = null;
                for (Cenovnik c:cenovnici){
                    if (c.getDatumOd().equals(datumOd) && c.getDatumDo().equals(datumDo)) {
                        noviCenovnik = c;
                        break;
                    }
                }
                if(noviCenovnik==null){
                    noviCenovnik = new Cenovnik(Integer.parseInt(delovi[0]),datumOd,datumDo);
                    cenovnici.add(noviCenovnik);
                }



                KategorijaVozila kategorijaVozila = null;
                if (!delovi[3].isBlank()) {
                    kategorijaVozila = KategorijaVozila.valueOf(delovi[3]);
                }

                KategorijaKlijenta kategorijaKlijenta = null;
                if (!delovi[4].isBlank()) {
                    kategorijaKlijenta = KategorijaKlijenta.valueOf(delovi[4]);
                }

                DodatnaUsluga dodatnaUsluga = null;
                if (!delovi[5].isBlank()) {
                    dodatnaUsluga = dodatnaUslugaRepozitorijum.pronadjiPoId(Integer.parseInt(delovi[5]));
                }

                StavkaCenovnika stavka = new StavkaCenovnika(TipCene.valueOf(delovi[1]),Double.parseDouble(delovi[2]),
                        kategorijaVozila,kategorijaKlijenta,dodatnaUsluga);

                noviCenovnik.dodajStavku(stavka);
            }
        }catch(IOException e){
            System.out.println("Greska prilikom citanja fajla: " + putanjaDoFajla);
        }
        return cenovnici;
    }

    public Cenovnik pronadjiVazeciCenovnik(LocalDate datum) {
        for (Cenovnik cenovnik : ucitajSve()) {
            if (cenovnik.vaziNaDatum(datum)) {
                return cenovnik;
            }
        }

        return null;
    }
}
