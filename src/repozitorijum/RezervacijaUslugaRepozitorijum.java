package repozitorijum;

import model.DodatnaUsluga;
import model.RezervacijaUsluga;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public class RezervacijaUslugaRepozitorijum {
    private String putanjaDoFajla;
    private DodatnaUslugaRepozitorijum dodatnaUslugaRepozitorijum;

    public RezervacijaUslugaRepozitorijum() {
        this.putanjaDoFajla = "src/fajlovi/rezervacija_usluge.csv";
        this.dodatnaUslugaRepozitorijum = new DodatnaUslugaRepozitorijum();
    }

    public RezervacijaUslugaRepozitorijum(String putanjaDoFajla) {
        this.putanjaDoFajla = putanjaDoFajla;
        this.dodatnaUslugaRepozitorijum = new DodatnaUslugaRepozitorijum();
    }

    public RezervacijaUslugaRepozitorijum(String putanjaDoFajla,
                                          DodatnaUslugaRepozitorijum dodatnaUslugaRepozitorijum) {
        this.putanjaDoFajla = putanjaDoFajla;
        this.dodatnaUslugaRepozitorijum = dodatnaUslugaRepozitorijum;
    }

    public ArrayList<RezervacijaUsluga> ucitajSve() {
        ArrayList<RezervacijaUsluga> rezervacijaUsluge = new ArrayList<>();

        try {
            ArrayList<String> linije = new ArrayList<>(Files.readAllLines(Path.of(putanjaDoFajla)));

            for (int i = 1; i < linije.size(); i++) {
                String linija = linije.get(i);

                if (!linija.isBlank()) {
                    rezervacijaUsluge.add(napraviRezervacijaUslugu(linija));
                }
            }
        } catch (IOException e) {
            System.out.println("Greska prilikom citanja fajla: " + putanjaDoFajla);
        }

        return rezervacijaUsluge;
    }

    public ArrayList<RezervacijaUsluga> pronadjiPoRezervaciji(int rezervacijaId) {
        ArrayList<RezervacijaUsluga> rezultat = new ArrayList<>();

        for (RezervacijaUsluga rezervacijaUsluga : ucitajSve()) {
            if (rezervacijaUsluga.getRezervacijaId() == rezervacijaId) {
                rezultat.add(rezervacijaUsluga);
            }
        }

        return rezultat;
    }

    public RezervacijaUsluga pronadjiPoId(int id) {
        for (RezervacijaUsluga rezervacijaUsluga : ucitajSve()) {
            if (rezervacijaUsluga.getId() == id) {
                return rezervacijaUsluga;
            }
        }

        return null;
    }

    public void dodaj(RezervacijaUsluga rezervacijaUsluga) {
        try {
            Path putanja = Path.of(putanjaDoFajla);
            ArrayList<String> linije = new ArrayList<>(Files.readAllLines(putanja));
            linije.removeIf(String::isBlank);
            linije.add(napraviCsvLiniju(rezervacijaUsluga));
            Files.write(putanja, linije);
        } catch (IOException e) {
            System.out.println("Greska prilikom cuvanja usluge rezervacije u fajl: " + putanjaDoFajla);
        }
    }

    public void azuriraj(RezervacijaUsluga rezervacijaUsluga) {
        try {
            ArrayList<String> linije = new ArrayList<>(Files.readAllLines(Path.of(putanjaDoFajla)));
            for (int i = 1; i < linije.size(); i++) {
                if (linije.get(i).isBlank()) {
                    continue;
                }

                String[] delovi = linije.get(i).split(",", -1);
                if (Integer.parseInt(delovi[0]) == rezervacijaUsluga.getId()) {
                    linije.set(i, napraviCsvLiniju(rezervacijaUsluga));
                    break;
                }
            }

            linije.removeIf(String::isBlank);
            Files.write(Path.of(putanjaDoFajla), linije);
        } catch (IOException e) {
            System.out.println("Greska prilikom azuriranja usluge rezervacije u fajlu: " + putanjaDoFajla);
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
            System.out.println("Greska prilikom brisanja usluge rezervacije iz fajla: " + putanjaDoFajla);
        }
    }

    public int sledeciId() {
        int najveciId = 0;

        for (RezervacijaUsluga rezervacijaUsluga : ucitajSve()) {
            if (rezervacijaUsluga.getId() > najveciId) {
                najveciId = rezervacijaUsluga.getId();
            }
        }

        return najveciId + 1;
    }

    private RezervacijaUsluga napraviRezervacijaUslugu(String linija) {
        String[] delovi = linija.split(",", -1);
        DodatnaUsluga dodatnaUsluga = dodatnaUslugaRepozitorijum.pronadjiPoId(Integer.parseInt(delovi[2]));

        return new RezervacijaUsluga(
                Integer.parseInt(delovi[0]),
                Integer.parseInt(delovi[1]),
                dodatnaUsluga,
                Integer.parseInt(delovi[3]),
                Double.parseDouble(delovi[4]),
                Double.parseDouble(delovi[5])
        );
    }

    private String napraviCsvLiniju(RezervacijaUsluga rezervacijaUsluga) {
        return rezervacijaUsluga.getId() + "," + rezervacijaUsluga.getRezervacijaId() + ","
                + rezervacijaUsluga.getDodatnaUsluga().getId() + "," + rezervacijaUsluga.getKolicina() + ","
                + rezervacijaUsluga.getCenaPoJedinici() + "," + rezervacijaUsluga.getUkupno();
    }
}
