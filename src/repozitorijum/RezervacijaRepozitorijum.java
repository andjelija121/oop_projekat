package repozitorijum;

import enums.StatusRezervacije;
import model.Agent;
import model.Rezervacija;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class RezervacijaRepozitorijum {
    private String putanjaDoFajla;
    private KorisnikRepozitorijum korisnikRepozitorijum;
    private ModelVozilaRepozitorijum modelVozilaRepozitorijum;

    public RezervacijaRepozitorijum() {
        this.putanjaDoFajla = "src/fajlovi/rezervacije.csv";
        this.korisnikRepozitorijum = new KorisnikRepozitorijum();
        this.modelVozilaRepozitorijum = new ModelVozilaRepozitorijum();
    }

    public RezervacijaRepozitorijum(String putanjaDoFajla) {
        this.putanjaDoFajla = putanjaDoFajla;
        this.korisnikRepozitorijum = new KorisnikRepozitorijum();
        this.modelVozilaRepozitorijum = new ModelVozilaRepozitorijum();
    }

    public RezervacijaRepozitorijum(String putanjaDoFajla, KorisnikRepozitorijum korisnikRepozitorijum,
                                 ModelVozilaRepozitorijum modelVozilaRepozitorijum) {
        this.putanjaDoFajla = putanjaDoFajla;
        this.korisnikRepozitorijum = korisnikRepozitorijum;
        this.modelVozilaRepozitorijum = modelVozilaRepozitorijum;
    }

    public ArrayList<Rezervacija> ucitajSve() {
        ArrayList<Rezervacija> rezervacije = new ArrayList<>();

        try {
            ArrayList<String> linije = new ArrayList<>(Files.readAllLines(Path.of(putanjaDoFajla)));

            for (int i = 1; i < linije.size(); i++) {
                String linija = linije.get(i);

                if (!linija.isBlank()) {
                    rezervacije.add(napraviRezervaciju(linija));
                }
            }
        } catch (IOException e) {
            System.out.println("Greska prilikom citanja fajla: " + putanjaDoFajla);
        }

        return rezervacije;
    }

    public Rezervacija pronadjiPoId(int id) {
        for (Rezervacija rezervacija : ucitajSve()) {
            if (rezervacija.getId() == id) {
                return rezervacija;
            }
        }

        return null;
    }

    public void dodaj(Rezervacija rezervacija) {
        try {
            ArrayList<String> linije = new ArrayList<>(Files.readAllLines(Path.of(putanjaDoFajla)));
            linije.removeIf(String::isBlank);
            linije.add(napraviCsvLiniju(rezervacija));
            Files.write(Path.of(putanjaDoFajla), linije);
        } catch (IOException e) {
            System.out.println("Greska prilikom cuvanja rezervacije u fajl: " + putanjaDoFajla);
        }
    }

    public void azuriraj(Rezervacija rezervacija) {
        try {
            ArrayList<String> linije = new ArrayList<>(Files.readAllLines(Path.of(putanjaDoFajla)));

            for (int i = 1; i < linije.size(); i++) {
                String linija = linije.get(i);

                if (linija.isBlank()) {
                    continue;
                }

                String[] delovi = linija.split(",", -1);
                int id = Integer.parseInt(delovi[0]);

                if (id == rezervacija.getId()) {
                    linije.set(i, napraviCsvLiniju(rezervacija));
                    break;
                }
            }

            linije.removeIf(String::isBlank);
            Files.write(Path.of(putanjaDoFajla), linije);
        } catch (IOException e) {
            System.out.println("Greska prilikom azuriranja rezervacije u fajlu: " + putanjaDoFajla);
        }
    }

    public int sledeciId() {
        int najveciId = 0;

        for (Rezervacija rezervacija : ucitajSve()) {
            if (rezervacija.getId() > najveciId) {
                najveciId = rezervacija.getId();
            }
        }

        return najveciId + 1;
    }

    private Rezervacija napraviRezervaciju(String linija) {
        String[] delovi = linija.split(",", -1);
        LocalDate datumOd = LocalDate.parse(delovi[3]);
        Agent agentObrade = delovi.length > 11 && !delovi[11].isBlank()
                ? korisnikRepozitorijum.pronadjiAgentaPoId(Integer.parseInt(delovi[11]))
                : null;
        LocalDate datumKreiranja = delovi.length > 12 && !delovi[12].isBlank()
                ? LocalDate.parse(delovi[12])
                : datumOd;
        LocalDate datumObrade = delovi.length > 13 && !delovi[13].isBlank()
                ? LocalDate.parse(delovi[13])
                : null;

        return new Rezervacija(
                Integer.parseInt(delovi[0]),
                korisnikRepozitorijum.pronadjiKlijentaPoId(Integer.parseInt(delovi[1])),
                modelVozilaRepozitorijum.pronadjiPoId(Integer.parseInt(delovi[2])),
                datumOd,
                LocalDate.parse(delovi[4]),
                StatusRezervacije.valueOf(delovi[5]),
                Double.parseDouble(delovi[6]),
                Double.parseDouble(delovi[7]),
                Double.parseDouble(delovi[8]),
                Double.parseDouble(delovi[9]),
                delovi.length > 10 && !delovi[10].isBlank() ? LocalDateTime.parse(delovi[10]) : null,
                agentObrade,
                datumKreiranja,
                datumObrade
        );
    }

    private String napraviCsvLiniju(Rezervacija rezervacija) {
        String vremeOtkazivanja = rezervacija.getVremeOtkazivanja() == null
                ? "" : rezervacija.getVremeOtkazivanja().toString();
        String agentObradeId = rezervacija.getAgentObrade() == null
                ? "" : String.valueOf(rezervacija.getAgentObrade().getId());
        String datumKreiranja = rezervacija.getDatumKreiranja() == null
                ? "" : rezervacija.getDatumKreiranja().toString();
        String datumObrade = rezervacija.getDatumObrade() == null
                ? "" : rezervacija.getDatumObrade().toString();

        return rezervacija.getId() + "," + rezervacija.getKlijent().getId() + ","
                + rezervacija.getModelVozila().getId() + ","
                + rezervacija.getDatumOd() + "," + rezervacija.getDatumDo() + "," + rezervacija.getStatus() + ","
                + rezervacija.getCenaNajma() + "," + rezervacija.getCenaDodatnihUsluga() + ","
                + rezervacija.getKazna() + "," + rezervacija.getCenaUkupno() + "," + vremeOtkazivanja + ","
                + agentObradeId + "," + datumKreiranja + "," + datumObrade;
    }
}
