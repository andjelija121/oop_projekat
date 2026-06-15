package repozitorijum;

import enums.TipNaplate;
import model.DodatnaUsluga;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public class DodatnaUslugaRepozitorijum {
    private String putanjaDoFajla;

    public DodatnaUslugaRepozitorijum() {
        this.putanjaDoFajla = "src/fajlovi/dodatne_usluge.csv";
    }

    public ArrayList<DodatnaUsluga> ucitajSve() {
        ArrayList<DodatnaUsluga> dodatneUsluge = new ArrayList<>();
        try{
            ArrayList<String> linije = new ArrayList<>(Files.readAllLines(Path.of(putanjaDoFajla)));
            for (int i=1;i<linije.size();i++){
                String [] delovi = linije.get(i).trim().split(",",-1);
                dodatneUsluge.add(new DodatnaUsluga(Integer.parseInt(delovi[0]),delovi[1], TipNaplate.valueOf(delovi[2])));
            }
        }catch  (IOException e) {
            System.out.println("Greska prilikom citanja fajla: " + putanjaDoFajla);
        }
        return dodatneUsluge;
    }

    public DodatnaUsluga pronadjiPoId(int id) {
        ArrayList<DodatnaUsluga> dodatneUsluge = ucitajSve();
        for(DodatnaUsluga u:dodatneUsluge){
            if(u.getId()==id){
                return u;
            }
        }
        return null;
    }
}