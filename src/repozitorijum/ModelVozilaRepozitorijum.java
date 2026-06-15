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
}
