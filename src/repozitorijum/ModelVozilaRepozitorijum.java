package repozitorijum;

import enums.KategorijaVozila;
import model.ModelVozila;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public class ModelVozilaRepozitorijum {
    private String putanjaDoFajla;

    public ModelVozilaRepozitorijum() {
        this.putanjaDoFajla = "src/fajlovi/modeli_vozila.csv";
    }

    public ModelVozilaRepozitorijum(String putanjaDoFajla) {
        this.putanjaDoFajla = putanjaDoFajla;
    }

    public ArrayList<ModelVozila> ucitajSve() {
        ArrayList<ModelVozila> modeli = new ArrayList<>();

        try {
            ArrayList<String> linije = new ArrayList<>(Files.readAllLines(Path.of(putanjaDoFajla)));

            for (int i = 1; i < linije.size(); i++) {
                String[] delovi = linije.get(i).split(",", -1);
                modeli.add(new ModelVozila(
                        Integer.parseInt(delovi[0]),
                        delovi[1],
                        delovi[2],
                        KategorijaVozila.valueOf(delovi[3])
                ));
            }
        } catch (IOException e) {
            System.out.println("Greska prilikom citanja fajla: " + putanjaDoFajla);
        }

        return modeli;
    }

    public ModelVozila pronadjiPoId(int id) {
        for (ModelVozila model : ucitajSve()) {
            if (model.getId() == id) {
                return model;
            }
        }

        return null;
    }

    public void dodaj(ModelVozila modelVozila) {
        try {
            ArrayList<String> linije = new ArrayList<>(Files.readAllLines(Path.of(putanjaDoFajla)));
            linije.removeIf(String::isBlank);
            linije.add(napraviCsvLiniju(modelVozila));
            Files.write(Path.of(putanjaDoFajla), linije);
        } catch (IOException e) {
            System.out.println("Greska prilikom cuvanja modela vozila u fajl: " + putanjaDoFajla);
        }
    }

    public void azuriraj(ModelVozila modelVozila) {
        try {
            ArrayList<String> linije = new ArrayList<>(Files.readAllLines(Path.of(putanjaDoFajla)));
            for (int i = 1; i < linije.size(); i++) {
                if (linije.get(i).isBlank()) {
                    continue;
                }

                String[] delovi = linije.get(i).split(",", -1);
                if (Integer.parseInt(delovi[0]) == modelVozila.getId()) {
                    linije.set(i, napraviCsvLiniju(modelVozila));
                    break;
                }
            }

            linije.removeIf(String::isBlank);
            Files.write(Path.of(putanjaDoFajla), linije);
        } catch (IOException e) {
            System.out.println("Greska prilikom azuriranja modela vozila u fajlu: " + putanjaDoFajla);
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
            System.out.println("Greska prilikom brisanja modela vozila iz fajla: " + putanjaDoFajla);
        }
    }

    public int sledeciId() {
        int najveciId = 0;
        for (ModelVozila modelVozila : ucitajSve()) {
            if (modelVozila.getId() > najveciId) {
                najveciId = modelVozila.getId();
            }
        }

        return najveciId + 1;
    }

    private String napraviCsvLiniju(ModelVozila modelVozila) {
        int id = modelVozila.getId() > 0 ? modelVozila.getId() : sledeciId();
        return id + "," + modelVozila.getNaziv() + "," + modelVozila.getProizvodjac()
                + "," + modelVozila.getKategorijaVozila();
    }
}
