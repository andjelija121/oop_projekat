package repozitorijum;

import enums.StatusPretplate;
import model.Agent;
import model.Klijent;
import model.ZahtevPretplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public class ZahtevPretplateRepozitorijum {
    private String putanjaDoFajla;
    private KorisnikRepozitorijum korisnikRepozitorijum;

    public ZahtevPretplateRepozitorijum() {
        this("src/fajlovi/zahtevi_pretplate.csv", new KorisnikRepozitorijum());
    }

    public ZahtevPretplateRepozitorijum(String putanjaDoFajla, KorisnikRepozitorijum korisnikRepozitorijum) {
        this.putanjaDoFajla = putanjaDoFajla;
        this.korisnikRepozitorijum = korisnikRepozitorijum;
    }

    public ArrayList<ZahtevPretplate> ucitajSve() {
        ArrayList<ZahtevPretplate> zahtevi = new ArrayList<>();

        try {
            ArrayList<String> linije = new ArrayList<>(Files.readAllLines(Path.of(putanjaDoFajla)));

            for (int i = 1; i < linije.size(); i++) {
                if (!linije.get(i).isBlank()) {
                    zahtevi.add(napraviZahtev(linije.get(i)));
                }
            }
        } catch (IOException e) {
            System.out.println("Greska prilikom citanja fajla: " + putanjaDoFajla);
        }

        return zahtevi;
    }

    public ZahtevPretplate pronadjiPoId(int id) {
        for (ZahtevPretplate zahtev : ucitajSve()) {
            if (zahtev.getId() == id) {
                return zahtev;
            }
        }

        return null;
    }

    public void dodaj(ZahtevPretplate zahtev) {
        try {
            ArrayList<String> linije = new ArrayList<>(Files.readAllLines(Path.of(putanjaDoFajla)));
            linije.removeIf(String::isBlank);
            linije.add(napraviCsvLiniju(zahtev));
            Files.write(Path.of(putanjaDoFajla), linije);
        } catch (IOException e) {
            System.out.println("Greska prilikom cuvanja zahteva pretplate u fajl: " + putanjaDoFajla);
        }
    }

    public void azuriraj(ZahtevPretplate zahtev) {
        try {
            ArrayList<String> linije = new ArrayList<>(Files.readAllLines(Path.of(putanjaDoFajla)));

            for (int i = 1; i < linije.size(); i++) {
                if (linije.get(i).isBlank()) {
                    continue;
                }

                String[] delovi = linije.get(i).split(",", -1);
                if (Integer.parseInt(delovi[0]) == zahtev.getId()) {
                    linije.set(i, napraviCsvLiniju(zahtev));
                    break;
                }
            }

            linije.removeIf(String::isBlank);
            Files.write(Path.of(putanjaDoFajla), linije);
        } catch (IOException e) {
            System.out.println("Greska prilikom azuriranja zahteva pretplate u fajlu: " + putanjaDoFajla);
        }
    }

    public int sledeciId() {
        int najveciId = 0;

        for (ZahtevPretplate zahtev : ucitajSve()) {
            if (zahtev.getId() > najveciId) {
                najveciId = zahtev.getId();
            }
        }

        return najveciId + 1;
    }

    private ZahtevPretplate napraviZahtev(String linija) {
        String[] delovi = linija.split(",", -1);
        Klijent klijent = korisnikRepozitorijum.pronadjiKlijentaPoId(Integer.parseInt(delovi[1]));
        Agent agent = delovi[2].isBlank() ? null : korisnikRepozitorijum.pronadjiAgentaPoId(Integer.parseInt(delovi[2]));

        return new ZahtevPretplate(
                Integer.parseInt(delovi[0]),
                klijent,
                agent,
                java.time.LocalDate.parse(delovi[3]),
                StatusPretplate.valueOf(delovi[4])
        );
    }

    private String napraviCsvLiniju(ZahtevPretplate zahtev) {
        String agentId = zahtev.getAgent() == null ? "" : String.valueOf(zahtev.getAgent().getId());
        return zahtev.getId() + "," + zahtev.getKlijent().getId() + "," + agentId + ","
                + zahtev.getDatumZahteva() + "," + zahtev.getStatus();
    }
}
