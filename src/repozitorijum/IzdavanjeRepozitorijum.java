package repozitorijum;

import model.Agent;
import model.Izdavanje;
import model.Rezervacija;
import model.Vozilo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public class IzdavanjeRepozitorijum {
    private String putanjaDoFajla;
    private RezervacijaRepozitorijum rezervacijaRepozitorijum;
    private KorisnikRepozitorijum korisnikRepozitorijum;
    private VoziloRepozitorijum voziloRepozitorijum;

    public IzdavanjeRepozitorijum() {
        this("src/fajlovi/izdavanja.csv", new RezervacijaRepozitorijum(),
                new KorisnikRepozitorijum(), new VoziloRepozitorijum());
    }

    public IzdavanjeRepozitorijum(String putanjaDoFajla, RezervacijaRepozitorijum rezervacijaRepozitorijum,
                                  KorisnikRepozitorijum korisnikRepozitorijum,
                                  VoziloRepozitorijum voziloRepozitorijum) {
        this.putanjaDoFajla = putanjaDoFajla;
        this.rezervacijaRepozitorijum = rezervacijaRepozitorijum;
        this.korisnikRepozitorijum = korisnikRepozitorijum;
        this.voziloRepozitorijum = voziloRepozitorijum;
    }

    public ArrayList<Izdavanje> ucitajSve() {
        ArrayList<Izdavanje> izdavanja = new ArrayList<>();

        try {
            ArrayList<String> linije = new ArrayList<>(Files.readAllLines(Path.of(putanjaDoFajla)));

            for (int i = 1; i < linije.size(); i++) {
                if (!linije.get(i).isBlank()) {
                    izdavanja.add(napraviIzdavanje(linije.get(i)));
                }
            }
        } catch (IOException e) {
            System.out.println("Greska prilikom citanja fajla: " + putanjaDoFajla);
        }

        return izdavanja;
    }

    public Izdavanje pronadjiPoId(int id) {
        for (Izdavanje izdavanje : ucitajSve()) {
            if (izdavanje.getId() == id) {
                return izdavanje;
            }
        }

        return null;
    }

    public Izdavanje pronadjiPoRezervaciji(int rezervacijaId) {
        for (Izdavanje izdavanje : ucitajSve()) {
            if (izdavanje.getRezervacija() != null && izdavanje.getRezervacija().getId() == rezervacijaId) {
                return izdavanje;
            }
        }

        return null;
    }

    public void dodaj(Izdavanje izdavanje) {
        try {
            ArrayList<String> linije = new ArrayList<>(Files.readAllLines(Path.of(putanjaDoFajla)));
            linije.removeIf(String::isBlank);
            linije.add(napraviCsvLiniju(izdavanje));
            Files.write(Path.of(putanjaDoFajla), linije);
        } catch (IOException e) {
            System.out.println("Greska prilikom cuvanja izdavanja u fajl: " + putanjaDoFajla);
        }
    }

    public void azuriraj(Izdavanje izdavanje) {
        try {
            ArrayList<String> linije = new ArrayList<>(Files.readAllLines(Path.of(putanjaDoFajla)));

            for (int i = 1; i < linije.size(); i++) {
                if (linije.get(i).isBlank()) {
                    continue;
                }

                String[] delovi = linije.get(i).split(",", -1);
                int id = Integer.parseInt(delovi[0]);

                if (id == izdavanje.getId()) {
                    linije.set(i, napraviCsvLiniju(izdavanje));
                    break;
                }
            }

            linije.removeIf(String::isBlank);
            Files.write(Path.of(putanjaDoFajla), linije);
        } catch (IOException e) {
            System.out.println("Greska prilikom azuriranja izdavanja u fajlu: " + putanjaDoFajla);
        }
    }

    public int sledeciId() {
        int najveciId = 0;

        for (Izdavanje izdavanje : ucitajSve()) {
            if (izdavanje.getId() > najveciId) {
                najveciId = izdavanje.getId();
            }
        }

        return najveciId + 1;
    }

    private Izdavanje napraviIzdavanje(String linija) {
        String[] delovi = linija.split(",", -1);
        Rezervacija rezervacija = rezervacijaRepozitorijum.pronadjiPoId(Integer.parseInt(delovi[1]));
        Agent agent = korisnikRepozitorijum.pronadjiAgentaPoId(Integer.parseInt(delovi[2]));
        Vozilo vozilo = voziloRepozitorijum.pronadjiPoId(Integer.parseInt(delovi[3]));

        return new Izdavanje(
                Integer.parseInt(delovi[0]),
                rezervacija,
                agent,
                vozilo,
                java.time.LocalDate.parse(delovi[4]),
                java.time.LocalDate.parse(delovi[5]),
                delovi[6].isBlank() ? null : java.time.LocalDate.parse(delovi[6]),
                Integer.parseInt(delovi[7]),
                delovi[8].isBlank() ? null : Integer.parseInt(delovi[8])
        );
    }

    private String napraviCsvLiniju(Izdavanje izdavanje) {
        String datumVracanjaStvarno = izdavanje.getDatumVracanjaStvarno() == null
                ? "" : izdavanje.getDatumVracanjaStvarno().toString();
        String kilometrazaVracanje = izdavanje.getKilometrazaVracanje() == null
                ? "" : izdavanje.getKilometrazaVracanje().toString();

        return izdavanje.getId() + "," + izdavanje.getRezervacija().getId() + ","
                + izdavanje.getAgent().getId() + "," + izdavanje.getVozilo().getId() + ","
                + izdavanje.getDatumIzdavanja() + "," + izdavanje.getDatumVracanjaPlanirano() + ","
                + datumVracanjaStvarno + "," + izdavanje.getKilometrazaPreuzimanje() + ","
                + kilometrazaVracanje;
    }
}
