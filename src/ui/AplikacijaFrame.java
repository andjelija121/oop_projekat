package ui;

import menadzment.IzdavanjeMenadzer;
import menadzment.IzvestajMenadzer;
import menadzment.KlijentMenadzer;
import menadzment.PretplataMenadzer;
import menadzment.PrijavaMenadzer;
import menadzment.RezervacijaMenadzer;
import menadzment.ZaposleniMenadzer;
import menadzment.CenovnikMenadzer;
import model.Administrator;
import model.Agent;
import model.Klijent;
import model.Korisnik;
import repozitorijum.KorisnikRepozitorijum;
import repozitorijum.ModelVozilaRepozitorijum;
import repozitorijum.PretplataRepozitorijum;
import repozitorijum.RezervacijaRepozitorijum;
import repozitorijum.VoziloRepozitorijum;
import repozitorijum.CenovnikRepozitorijum;
import repozitorijum.DodatnaUslugaRepozitorijum;
import repozitorijum.IzdavanjeRepozitorijum;
import repozitorijum.RezervacijaUslugaRepozitorijum;
import repozitorijum.ZahtevPretplateRepozitorijum;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import java.awt.Dimension;

public class AplikacijaFrame extends JFrame {
    private final KorisnikRepozitorijum korisnikRepozitorijum;
    private final PrijavaMenadzer prijavaMenadzer;
    private final ZaposleniMenadzer zaposleniMenadzer;
    private final KlijentMenadzer klijentMenadzer;
    private final RezervacijaMenadzer rezervacijaMenadzer;
    private final IzdavanjeMenadzer izdavanjeMenadzer;
    private final PretplataMenadzer pretplataMenadzer;
    private final IzvestajMenadzer izvestajMenadzer;
    private final CenovnikMenadzer cenovnikMenadzer;
    private final DodatnaUslugaRepozitorijum dodatnaUslugaRepozitorijum;
    private final RezervacijaUslugaRepozitorijum rezervacijaUslugaRepozitorijum;
    private Korisnik ulogovaniKorisnik;

    public AplikacijaFrame() {
        korisnikRepozitorijum = new KorisnikRepozitorijum();
        prijavaMenadzer = new PrijavaMenadzer(korisnikRepozitorijum);
        zaposleniMenadzer = new ZaposleniMenadzer(korisnikRepozitorijum);
        klijentMenadzer = new KlijentMenadzer(korisnikRepozitorijum);

        ModelVozilaRepozitorijum modeli = new ModelVozilaRepozitorijum();
        VoziloRepozitorijum vozila = new VoziloRepozitorijum("src/fajlovi/vozila.csv", modeli);
        RezervacijaRepozitorijum rezervacije = new RezervacijaRepozitorijum(
                "src/fajlovi/rezervacije.csv", korisnikRepozitorijum, modeli);
        rezervacijaUslugaRepozitorijum = new RezervacijaUslugaRepozitorijum();
        rezervacijaMenadzer = new RezervacijaMenadzer(rezervacije, vozila, modeli, rezervacijaUslugaRepozitorijum);
        cenovnikMenadzer = new CenovnikMenadzer(new CenovnikRepozitorijum());
        IzdavanjeRepozitorijum izdavanja = new IzdavanjeRepozitorijum(
                "src/fajlovi/izdavanja.csv", rezervacije, korisnikRepozitorijum, vozila);
        rezervacijaMenadzer.setIzdavanjeRepozitorijum(izdavanja);
        izdavanjeMenadzer = new IzdavanjeMenadzer(izdavanja, rezervacije, vozila, cenovnikMenadzer);
        PretplataRepozitorijum pretplate = new PretplataRepozitorijum("src/fajlovi/pretplate.csv",
                korisnikRepozitorijum);
        pretplataMenadzer = new PretplataMenadzer(
                pretplate,
                new ZahtevPretplateRepozitorijum("src/fajlovi/zahtevi_pretplate.csv", korisnikRepozitorijum),
                izdavanja,
                cenovnikMenadzer);
        izvestajMenadzer = new IzvestajMenadzer(korisnikRepozitorijum, rezervacije, izdavanja, pretplate, modeli);
        rezervacijaMenadzer.setPretplataMenadzer(pretplataMenadzer);
        dodatnaUslugaRepozitorijum = new DodatnaUslugaRepozitorijum();
        rezervacijaMenadzer.odbijIstekleRezervacije();

        setTitle("Rent a Car");
        setMinimumSize(new Dimension(1000, 650));
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        prikaziLogin();
        setVisible(true);
    }

    private void prikaziLogin() {
        ulogovaniKorisnik = null;
        setJMenuBar(null);
        setContentPane(new LoginPanel(prijavaMenadzer, this::prijaviKorisnika));
        osveziProzor();
    }

    private void prijaviKorisnika(Korisnik korisnik) {
        ulogovaniKorisnik = korisnik;
        setJMenuBar(kreirajMeni());
        prikaziGlavniEkran();
    }

    private void prikaziGlavniEkran() {
        if (ulogovaniKorisnik instanceof Administrator) {
            setContentPane(new AdministratorPanel(ulogovaniKorisnik, korisnikRepozitorijum,
                    zaposleniMenadzer, cenovnikMenadzer, izvestajMenadzer, dodatnaUslugaRepozitorijum,
                    this::prikaziGlavniEkran, this::prikaziLogin));
        } else if (ulogovaniKorisnik instanceof Agent) {
            rezervacijaMenadzer.odbijIstekleRezervacije();
            setContentPane(new AgentPanel(ulogovaniKorisnik, korisnikRepozitorijum, klijentMenadzer,
                    rezervacijaMenadzer, izdavanjeMenadzer, pretplataMenadzer,
                    cenovnikMenadzer, dodatnaUslugaRepozitorijum,
                    this::prikaziGlavniEkran, this::prikaziLogin));
        } else if (ulogovaniKorisnik instanceof Klijent) {
            setContentPane(new KlijentPanel((Klijent) ulogovaniKorisnik, rezervacijaMenadzer,
                    pretplataMenadzer, cenovnikMenadzer, dodatnaUslugaRepozitorijum,
                    this::prikaziGlavniEkran, this::prikaziLogin));
        }
        osveziProzor();
    }

    private JMenuBar kreirajMeni() {
        JMenuBar menuBar = new JMenuBar();
        JMenu nalogMenu = new JMenu("Nalog");
        JMenuItem odjavaItem = new JMenuItem("Odjava");
        JMenuItem izlazItem = new JMenuItem("Izlaz");

        odjavaItem.addActionListener(e -> prikaziLogin());
        izlazItem.addActionListener(e -> dispose());
        nalogMenu.add(odjavaItem);
        nalogMenu.add(izlazItem);
        menuBar.add(nalogMenu);
        return menuBar;
    }

    private void osveziProzor() {
        revalidate();
        repaint();
    }
}
