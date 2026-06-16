package repozitorijum;

import enums.StatusVozila;
import model.Vozilo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public class VoziloRepozitorijum {
    private String putanjaDoFajla;
    private ModelVozilaRepozitorijum modelVozilaRepozitorijum;

    public VoziloRepozitorijum() {
        this.putanjaDoFajla = "src/fajlovi/vozila.csv";
        this.modelVozilaRepozitorijum = new ModelVozilaRepozitorijum();
    }

    public VoziloRepozitorijum(String putanjaDoFajla) {
        this.putanjaDoFajla = putanjaDoFajla;
        this.modelVozilaRepozitorijum = new ModelVozilaRepozitorijum();
    }

    public VoziloRepozitorijum(String putanjaDoFajla, ModelVozilaRepozitorijum modelVozilaRepozitorijum) {
        this.putanjaDoFajla = putanjaDoFajla;
        this.modelVozilaRepozitorijum = modelVozilaRepozitorijum;
    }

    public ArrayList<Vozilo> ucitajSve() {
        ArrayList<Vozilo> vozila = new ArrayList<>();

        try {
            ArrayList<String> linije = new ArrayList<>(Files.readAllLines(Path.of(putanjaDoFajla)));

            for (int i = 1; i < linije.size(); i++) {
                String[] delovi = linije.get(i).split(",", -1);
                vozila.add(new Vozilo(
                        Integer.parseInt(delovi[0]),
                        modelVozilaRepozitorijum.pronadjiPoId(Integer.parseInt(delovi[1])),
                        delovi[2],
                        StatusVozila.valueOf(delovi[3]),
                        Integer.parseInt(delovi[4])
                ));
            }
        } catch (IOException e) {
            System.out.println("Greska prilikom citanja fajla: " + putanjaDoFajla);
        }

        return vozila;
    }

    public ArrayList<Vozilo> pronadjiPoModelu(int modelId) {
        ArrayList<Vozilo> vozilaModela = new ArrayList<>();

        for (Vozilo vozilo : ucitajSve()) {
            if (vozilo.getModelVozila() != null && vozilo.getModelVozila().getId() == modelId) {
                vozilaModela.add(vozilo);
            }
        }

        return vozilaModela;
    }

    public Vozilo pronadjiPoId(int id) {
        for (Vozilo vozilo : ucitajSve()) {
            if (vozilo.getId() == id) {
                return vozilo;
            }
        }

        return null;
    }

    public void dodaj(Vozilo vozilo) {
        try {
            ArrayList<String> linije = new ArrayList<>(Files.readAllLines(Path.of(putanjaDoFajla)));
            linije.removeIf(String::isBlank);
            linije.add(napraviCsvLiniju(vozilo));
            Files.write(Path.of(putanjaDoFajla), linije);
        } catch (IOException e) {
            System.out.println("Greska prilikom cuvanja vozila u fajl: " + putanjaDoFajla);
        }
    }

    public void azuriraj(Vozilo vozilo) {
        try {
            ArrayList<String> linije = new ArrayList<>(Files.readAllLines(Path.of(putanjaDoFajla)));

            for (int i = 1; i < linije.size(); i++) {
                if (linije.get(i).isBlank()) {
                    continue;
                }

                String[] delovi = linije.get(i).split(",", -1);
                int id = Integer.parseInt(delovi[0]);

                if (id == vozilo.getId()) {
                    linije.set(i, napraviCsvLiniju(vozilo));
                    break;
                }
            }

            linije.removeIf(String::isBlank);
            Files.write(Path.of(putanjaDoFajla), linije);
        } catch (IOException e) {
            System.out.println("Greska prilikom azuriranja vozila u fajlu: " + putanjaDoFajla);
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
            System.out.println("Greska prilikom brisanja vozila iz fajla: " + putanjaDoFajla);
        }
    }

    public int sledeciId() {
        int najveciId = 0;
        for (Vozilo vozilo : ucitajSve()) {
            if (vozilo.getId() > najveciId) {
                najveciId = vozilo.getId();
            }
        }

        return najveciId + 1;
    }

    private String napraviCsvLiniju(Vozilo vozilo) {
        return vozilo.getId() + "," + vozilo.getModelVozila().getId() + ","
                + vozilo.getRegistracija() + "," + vozilo.getStatus() + ","
                + vozilo.getKilometraza();
    }
}
