package repository;

import enums.KategorijaKlijenta;
import enums.NivoSpreme;
import enums.Pol;
import enums.TipKorisnika;
import model.Administrator;
import model.Agent;
import model.Klijent;
import model.Korisnik;
import model.Zaposleni;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;

public class KorisnikRepository {
    private String putanjaDoFajla;

    public KorisnikRepository() {
        this.putanjaDoFajla = "src/data/korisnici.csv";
    }

    public KorisnikRepository(String putanjaDoFajla) {
        this.putanjaDoFajla = putanjaDoFajla;
    }

    public ArrayList<Korisnik> ucitajSve() {
        ArrayList<Korisnik> korisnici = new ArrayList<>();

        try {
            ArrayList<String> linije = new ArrayList<>(Files.readAllLines(Path.of(putanjaDoFajla)));

            for (int i = 1; i < linije.size(); i++) {
                Korisnik korisnik = napraviKorisnika(linije.get(i));

                if (korisnik != null) {
                    korisnici.add(korisnik);
                }
            }
        } catch (IOException e) {
            System.out.println("Greska prilikom citanja fajla: " + putanjaDoFajla);
        }

        return korisnici;
    }

    public ArrayList<Agent> ucitajAgente() {
        ArrayList<Agent> agenti = new ArrayList<>();

        for (Korisnik korisnik : ucitajSve()) {
            if (korisnik instanceof Agent) {
                agenti.add((Agent) korisnik);
            }
        }

        return agenti;
    }

    public ArrayList<Administrator> ucitajAdministratore() {
        ArrayList<Administrator> administratori = new ArrayList<>();

        for (Korisnik korisnik : ucitajSve()) {
            if (korisnik instanceof Administrator) {
                administratori.add((Administrator) korisnik);
            }
        }

        return administratori;
    }

    public ArrayList<Klijent> ucitajKlijente() {
        ArrayList<Klijent> klijenti = new ArrayList<>();

        for (Korisnik korisnik : ucitajSve()) {
            if (korisnik instanceof Klijent) {
                klijenti.add((Klijent) korisnik);
            }
        }

        return klijenti;
    }

    public boolean korisnickoImePostoji(String korisnickoIme) {
        for (Korisnik korisnik : ucitajSve()) {
            if (korisnik.getKorisnickoIme().equals(korisnickoIme)) {
                return true;
            }
        }

        return false;
    }

    public void dodaj(Korisnik korisnik) {
        try {
            String linija = napraviCsvLiniju(korisnik);
            Files.writeString(Path.of(putanjaDoFajla), System.lineSeparator() + linija, StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.out.println("Greska prilikom cuvanja korisnika u fajl: " + putanjaDoFajla);
        }
    }

    private Korisnik napraviKorisnika(String linija) {
        String[] delovi = linija.split(",", -1);

        TipKorisnika tip = TipKorisnika.valueOf(delovi[1]);
        String ime = delovi[2];
        String prezime = delovi[3];
        Pol pol = Pol.valueOf(delovi[4]);
        String datumRodjenja = delovi[5];
        String telefon = delovi[6];
        String adresa = delovi[7];
        String korisnickoIme = delovi[8];
        String lozinka = delovi[9];

        if (tip == TipKorisnika.KLIJENT) {
            String datumDozvole = delovi[10];
            KategorijaKlijenta posebnaKategorija = KategorijaKlijenta.valueOf(delovi[11]);

            return new Klijent(ime, prezime, pol, datumRodjenja, telefon, adresa, korisnickoIme, lozinka,
                    datumDozvole, posebnaKategorija);
        }

        NivoSpreme nivoSpreme = NivoSpreme.valueOf(delovi[12]);
        int godineStaza = Integer.parseInt(delovi[13]);
        double osnova = Double.parseDouble(delovi[14]);

        if (tip == TipKorisnika.ADMINISTRATOR) {
            return new Administrator(ime, prezime, pol, datumRodjenja, telefon, adresa, korisnickoIme, lozinka,
                    nivoSpreme, godineStaza, osnova);
        }

        return new Agent(ime, prezime, pol, datumRodjenja, telefon, adresa, korisnickoIme, lozinka, nivoSpreme,
                godineStaza, osnova);
    }

    private String napraviCsvLiniju(Korisnik korisnik) {
        int id = sledeciId();

        if (korisnik instanceof Klijent) {
            Klijent klijent = (Klijent) korisnik;
            return id + "," + TipKorisnika.KLIJENT + "," + osnovnaPolja(korisnik) + ","
                    + klijent.getDatumDozvole() + "," + klijent.getPosebnaKategorija() + ",,,,";
        }

        Zaposleni zaposleni = (Zaposleni) korisnik;
        TipKorisnika tip = korisnik instanceof Administrator ? TipKorisnika.ADMINISTRATOR : TipKorisnika.AGENT;

        return id + "," + tip + "," + osnovnaPolja(korisnik) + ",,,"
                + zaposleni.getNivoSpreme() + "," + zaposleni.getGodineStaza() + ","
                + zaposleni.getOsnova() + "," + zaposleni.getNivoSpreme().getKoeficijent();
    }

    private String osnovnaPolja(Korisnik korisnik) {
        return korisnik.getIme() + "," + korisnik.getPrezime() + "," + korisnik.getPol() + ","
                + korisnik.getDatumRodjenja() + "," + korisnik.getTelefon() + "," + korisnik.getAdresa() + ","
                + korisnik.getKorisnickoIme() + "," + korisnik.getLozinka();
    }

    private int sledeciId() {
        try {
            ArrayList<String> linije = new ArrayList<>(Files.readAllLines(Path.of(putanjaDoFajla)));
            int najveciId = 0;

            for (int i = 1; i < linije.size(); i++) {
                String[] delovi = linije.get(i).split(",", -1);
                int id = Integer.parseInt(delovi[0]);

                if (id > najveciId) {
                    najveciId = id;
                }
            }

            return najveciId + 1;
        } catch (IOException e) {
            return 1;
        }
    }
}
