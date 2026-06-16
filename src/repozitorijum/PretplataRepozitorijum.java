package repozitorijum;

import enums.StatusPretplate;
import model.Klijent;
import model.Pretplata;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public class PretplataRepozitorijum {
    private String putanjaDoFajla;
    private KorisnikRepozitorijum korisnikRepozitorijum;

    public PretplataRepozitorijum() {
        this("src/fajlovi/pretplate.csv", new KorisnikRepozitorijum());
    }

    public PretplataRepozitorijum(String putanjaDoFajla, KorisnikRepozitorijum korisnikRepozitorijum) {
        this.putanjaDoFajla = putanjaDoFajla;
        this.korisnikRepozitorijum = korisnikRepozitorijum;
    }

    public ArrayList<Pretplata> ucitajSve() {
        ArrayList<Pretplata> pretplate = new ArrayList<>();

        try {
            ArrayList<String> linije = new ArrayList<>(Files.readAllLines(Path.of(putanjaDoFajla)));

            for (int i = 1; i < linije.size(); i++) {
                if (!linije.get(i).isBlank()) {
                    pretplate.add(napraviPretplatu(linije.get(i)));
                }
            }
        } catch (IOException e) {
            System.out.println("Greska prilikom citanja fajla: " + putanjaDoFajla);
        }

        return pretplate;
    }

    public ArrayList<Pretplata> pronadjiPoKlijentu(int klijentId) {
        ArrayList<Pretplata> rezultat = new ArrayList<>();

        for (Pretplata pretplata : ucitajSve()) {
            if (pretplata.getKlijent() != null && pretplata.getKlijent().getId() == klijentId) {
                rezultat.add(pretplata);
            }
        }

        return rezultat;
    }

    public Pretplata pronadjiPoId(int id) {
        for (Pretplata pretplata : ucitajSve()) {
            if (pretplata.getId() == id) {
                return pretplata;
            }
        }

        return null;
    }

    public void dodaj(Pretplata pretplata) {
        try {
            ArrayList<String> linije = new ArrayList<>(Files.readAllLines(Path.of(putanjaDoFajla)));
            linije.removeIf(String::isBlank);
            linije.add(napraviCsvLiniju(pretplata));
            Files.write(Path.of(putanjaDoFajla), linije);
        } catch (IOException e) {
            System.out.println("Greska prilikom cuvanja pretplate u fajl: " + putanjaDoFajla);
        }
    }

    public void azuriraj(Pretplata pretplata) {
        try {
            ArrayList<String> linije = new ArrayList<>(Files.readAllLines(Path.of(putanjaDoFajla)));

            for (int i = 1; i < linije.size(); i++) {
                if (linije.get(i).isBlank()) {
                    continue;
                }

                String[] delovi = linije.get(i).split(",", -1);
                if (Integer.parseInt(delovi[0]) == pretplata.getId()) {
                    linije.set(i, napraviCsvLiniju(pretplata));
                    break;
                }
            }

            linije.removeIf(String::isBlank);
            Files.write(Path.of(putanjaDoFajla), linije);
        } catch (IOException e) {
            System.out.println("Greska prilikom azuriranja pretplate u fajlu: " + putanjaDoFajla);
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
            System.out.println("Greska prilikom brisanja pretplate iz fajla: " + putanjaDoFajla);
        }
    }

    public int sledeciId() {
        int najveciId = 0;

        for (Pretplata pretplata : ucitajSve()) {
            if (pretplata.getId() > najveciId) {
                najveciId = pretplata.getId();
            }
        }

        return najveciId + 1;
    }

    private Pretplata napraviPretplatu(String linija) {
        String[] delovi = linija.split(",", -1);
        Klijent klijent = korisnikRepozitorijum.pronadjiKlijentaPoId(Integer.parseInt(delovi[1]));

        return new Pretplata(
                Integer.parseInt(delovi[0]),
                klijent,
                java.time.LocalDate.parse(delovi[2]),
                java.time.LocalDate.parse(delovi[3]),
                StatusPretplate.valueOf(delovi[4]),
                Double.parseDouble(delovi[5])
        );
    }

    private String napraviCsvLiniju(Pretplata pretplata) {
        return pretplata.getId() + "," + pretplata.getKlijent().getId() + ","
                + pretplata.getDatumPocetka() + "," + pretplata.getDatumKraja() + ","
                + pretplata.getStatus() + "," + pretplata.getCena();
    }
}
