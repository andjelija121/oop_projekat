package repository;

import enums.NivoSpreme;
import enums.Pol;
import enums.TipKorisnika;
import model.Administrator;
import model.Agent;
import model.Zaposleni;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public class ZaposleniRepository {
    private String putanjaDoFajla;

    public ZaposleniRepository() {
        this.putanjaDoFajla = "src/data/korisnici.csv";
    }

    public ZaposleniRepository(String putanjaDoFajla) {
        this.putanjaDoFajla = putanjaDoFajla;
    }

    public ArrayList<Zaposleni> ucitajSve() {
        ArrayList<Zaposleni> zaposleni = new ArrayList<>();

        try {
            ArrayList<String> linije = new ArrayList<>(Files.readAllLines(Path.of(putanjaDoFajla)));

            for (int i = 1; i < linije.size(); i++) {
                Zaposleni zaposleniIzFajla = napraviZaposlenog(linije.get(i));

                if (zaposleniIzFajla != null) {
                    zaposleni.add(zaposleniIzFajla);
                }
            }
        } catch (IOException e) {
            System.out.println("Greska prilikom citanja fajla: " + putanjaDoFajla);
        }

        return zaposleni;
    }

    public ArrayList<Agent> ucitajAgente() {
        ArrayList<Agent> agenti = new ArrayList<>();

        for (Zaposleni zaposleni : ucitajSve()) {
            if (zaposleni instanceof Agent) {
                agenti.add((Agent) zaposleni);
            }
        }

        return agenti;
    }

    public ArrayList<Administrator> ucitajAdministratore() {
        ArrayList<Administrator> administratori = new ArrayList<>();

        for (Zaposleni zaposleni : ucitajSve()) {
            if (zaposleni instanceof Administrator) {
                administratori.add((Administrator) zaposleni);
            }
        }

        return administratori;
    }

    private Zaposleni napraviZaposlenog(String linija) {
        String[] delovi = linija.split(",");
        TipKorisnika tipKorisnika = TipKorisnika.valueOf(delovi[1]);

        if (tipKorisnika == TipKorisnika.KLIJENT) {
            return null;
        }

        String ime = delovi[2];
        String prezime = delovi[3];
        Pol pol = Pol.valueOf(delovi[4]);
        String datumRodjenja = delovi[5];
        String telefon = delovi[6];
        String adresa = delovi[7];
        String korisnickoIme = delovi[8];
        String lozinka = delovi[9];
        NivoSpreme nivoSpreme = NivoSpreme.valueOf(delovi[12]);
        int godineStaza = Integer.parseInt(delovi[13]);
        double osnova = Double.parseDouble(delovi[14]);

        if (tipKorisnika == TipKorisnika.ADMINISTRATOR) {
            return new Administrator(ime, prezime, pol, datumRodjenja, telefon, adresa, korisnickoIme, lozinka,
                    nivoSpreme, godineStaza, osnova);
        }

        return new Agent(ime, prezime, pol, datumRodjenja, telefon, adresa, korisnickoIme, lozinka, nivoSpreme,
                godineStaza, osnova);
    }
}
