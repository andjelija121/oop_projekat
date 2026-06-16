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

    public DodatnaUslugaRepozitorijum(String putanjaDoFajla) {
        this.putanjaDoFajla = putanjaDoFajla;
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

    public void dodaj(DodatnaUsluga dodatnaUsluga) {
        try {
            ArrayList<String> linije = new ArrayList<>(Files.readAllLines(Path.of(putanjaDoFajla)));
            linije.removeIf(String::isBlank);
            linije.add(napraviCsvLiniju(dodatnaUsluga));
            Files.write(Path.of(putanjaDoFajla), linije);
        } catch (IOException e) {
            System.out.println("Greska prilikom cuvanja dodatne usluge u fajl: " + putanjaDoFajla);
        }
    }

    public void azuriraj(DodatnaUsluga dodatnaUsluga) {
        try {
            ArrayList<String> linije = new ArrayList<>(Files.readAllLines(Path.of(putanjaDoFajla)));
            for (int i = 1; i < linije.size(); i++) {
                if (linije.get(i).isBlank()) {
                    continue;
                }

                String[] delovi = linije.get(i).split(",", -1);
                if (Integer.parseInt(delovi[0]) == dodatnaUsluga.getId()) {
                    linije.set(i, napraviCsvLiniju(dodatnaUsluga));
                    break;
                }
            }

            linije.removeIf(String::isBlank);
            Files.write(Path.of(putanjaDoFajla), linije);
        } catch (IOException e) {
            System.out.println("Greska prilikom azuriranja dodatne usluge u fajlu: " + putanjaDoFajla);
        }
    }

    public void obrisi(int id) {
        try {
            ArrayList<String> linije = new ArrayList<>(Files.readAllLines(Path.of(putanjaDoFajla)));
            for (int i = linije.size() - 1; i >= 1; i--) {
                if (linije.get(i).isBlank()) {
                    linije.remove(i);
                    continue;
                }

                String[] delovi = linije.get(i).split(",", -1);
                if (Integer.parseInt(delovi[0]) == id) {
                    linije.remove(i);
                    break;
                }
            }

            Files.write(Path.of(putanjaDoFajla), linije);
        } catch (IOException e) {
            System.out.println("Greska prilikom brisanja dodatne usluge iz fajla: " + putanjaDoFajla);
        }
    }

    public int sledeciId() {
        int najveciId = 0;
        for (DodatnaUsluga dodatnaUsluga : ucitajSve()) {
            if (dodatnaUsluga.getId() > najveciId) {
                najveciId = dodatnaUsluga.getId();
            }
        }

        return najveciId + 1;
    }

    private String napraviCsvLiniju(DodatnaUsluga dodatnaUsluga) {
        int id = dodatnaUsluga.getId() > 0 ? dodatnaUsluga.getId() : sledeciId();
        return id + "," + dodatnaUsluga.getNaziv() + "," + dodatnaUsluga.getTipNaplate();
    }
}
