package ui;

import enums.KategorijaKlijenta;
import enums.NivoSpreme;
import enums.Pol;
import model.Administrator;
import model.Agent;
import model.Klijent;
import model.Korisnik;
import model.Zaposleni;
import repository.KorisnikRepository;
import service.AuthService;
import service.KlijentService;
import service.ZaposleniService;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;

public class AplikacijaFrame extends JFrame {
    private KorisnikRepository korisnikRepository;
    private AuthService authService;
    private ZaposleniService zaposleniService;
    private KlijentService klijentService;
    private Korisnik ulogovaniKorisnik;

    public AplikacijaFrame() {
        korisnikRepository = new KorisnikRepository();
        authService = new AuthService(korisnikRepository);
        zaposleniService = new ZaposleniService(korisnikRepository);
        klijentService = new KlijentService(korisnikRepository);

        setTitle("Rent a Car");
        setSize(800, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        prikaziLogin();
        setVisible(true);
    }

    private void prikaziLogin() {
        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));
        JTextField korisnickoImeField = new JTextField();
        JPasswordField lozinkaField = new JPasswordField();
        JButton loginButton = new JButton("Login");

        panel.add(new JLabel("Korisnicko ime"));
        panel.add(korisnickoImeField);
        panel.add(new JLabel("Lozinka"));
        panel.add(lozinkaField);
        panel.add(new JLabel(""));
        panel.add(loginButton);

        loginButton.addActionListener(e -> {
            String korisnickoIme = korisnickoImeField.getText();
            String lozinka = new String(lozinkaField.getPassword());
            Korisnik korisnik = authService.login(korisnickoIme, lozinka);

            if (korisnik == null) {
                JOptionPane.showMessageDialog(this, "Pogresno korisnicko ime ili lozinka.");
                return;
            }

            ulogovaniKorisnik = korisnik;
            prikaziGlavniEkran();
        });

        setContentPane(panel);
        revalidate();
        repaint();
    }

    private void prikaziGlavniEkran() {
        if (ulogovaniKorisnik instanceof Administrator) {
            prikaziAdminPanel();
        } else if (ulogovaniKorisnik instanceof Agent) {
            prikaziAgentPanel();
        } else if (ulogovaniKorisnik instanceof Klijent) {
            prikaziKlijentPanel();
        }
    }

    private void prikaziAdminPanel() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Zaposleni", zaposleniTabelaPanel());
        tabs.addTab("Dodaj zaposlenog", dodajZaposlenogPanel());

        setContentPane(okvirSaOdjavom(tabs));
        revalidate();
        repaint();
    }

    private void prikaziAgentPanel() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Klijenti", klijentiTabelaPanel());
        tabs.addTab("Dodaj klijenta", dodajKlijentaPanel());

        setContentPane(okvirSaOdjavom(tabs));
        revalidate();
        repaint();
    }

    private void prikaziKlijentPanel() {
        JPanel panel = new JPanel(new GridLayout(6, 2, 10, 10));
        Klijent klijent = (Klijent) ulogovaniKorisnik;

        panel.add(new JLabel("Ime"));
        panel.add(new JLabel(klijent.getIme()));
        panel.add(new JLabel("Prezime"));
        panel.add(new JLabel(klijent.getPrezime()));
        panel.add(new JLabel("Korisnicko ime"));
        panel.add(new JLabel(klijent.getKorisnickoIme()));
        panel.add(new JLabel("Datum dozvole"));
        panel.add(new JLabel(klijent.getDatumDozvole()));
        panel.add(new JLabel("Kategorija"));
        panel.add(new JLabel(String.valueOf(klijent.getPosebnaKategorija())));

        setContentPane(okvirSaOdjavom(panel));
        revalidate();
        repaint();
    }

    private JPanel okvirSaOdjavom(Component sredina) {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel header = new JPanel(new BorderLayout());
        JLabel korisnikLabel = new JLabel("Ulogovan: " + ulogovaniKorisnik.getIme() + " "
                + ulogovaniKorisnik.getPrezime());
        JButton odjavaButton = new JButton("Odjava");

        odjavaButton.addActionListener(e -> {
            ulogovaniKorisnik = null;
            prikaziLogin();
        });

        header.add(korisnikLabel, BorderLayout.WEST);
        header.add(odjavaButton, BorderLayout.EAST);
        panel.add(header, BorderLayout.NORTH);
        panel.add(sredina, BorderLayout.CENTER);
        return panel;
    }

    private JPanel zaposleniTabelaPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        DefaultTableModel model = new DefaultTableModel(
                new String[]{"Tip", "Ime", "Prezime", "Nivo", "Staz", "Plata"}, 0);

        for (Administrator administrator : korisnikRepository.ucitajAdministratore()) {
            dodajZaposlenogUTabelu(model, "Administrator", administrator);
        }

        for (Agent agent : korisnikRepository.ucitajAgente()) {
            dodajZaposlenogUTabelu(model, "Agent", agent);
        }

        panel.add(new JScrollPane(new JTable(model)), BorderLayout.CENTER);
        return panel;
    }

    private void dodajZaposlenogUTabelu(DefaultTableModel model, String tip, Zaposleni zaposleni) {
        model.addRow(new Object[]{
                tip,
                zaposleni.getIme(),
                zaposleni.getPrezime(),
                zaposleni.getNivoSpreme(),
                zaposleni.getGodineStaza(),
                zaposleni.getPlata()
        });
    }

    private JPanel klijentiTabelaPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        DefaultTableModel model = new DefaultTableModel(
                new String[]{"Ime", "Prezime", "Email", "Datum dozvole", "Kategorija"}, 0);

        for (Klijent klijent : korisnikRepository.ucitajKlijente()) {
            model.addRow(new Object[]{
                    klijent.getIme(),
                    klijent.getPrezime(),
                    klijent.getKorisnickoIme(),
                    klijent.getDatumDozvole(),
                    klijent.getPosebnaKategorija()
            });
        }

        panel.add(new JScrollPane(new JTable(model)), BorderLayout.CENTER);
        return panel;
    }

    private JPanel dodajZaposlenogPanel() {
        JPanel panel = new JPanel(new GridLayout(13, 2, 8, 8));
        JComboBox<String> tipBox = new JComboBox<>(new String[]{"AGENT", "ADMINISTRATOR"});
        JTextField imeField = new JTextField();
        JTextField prezimeField = new JTextField();
        JComboBox<Pol> polBox = new JComboBox<>(Pol.values());
        JTextField datumRodjenjaField = new JTextField("1990-01-01");
        JTextField telefonField = new JTextField();
        JTextField adresaField = new JTextField();
        JTextField korisnickoImeField = new JTextField();
        JPasswordField lozinkaField = new JPasswordField();
        JComboBox<NivoSpreme> nivoSpremeBox = new JComboBox<>(NivoSpreme.values());
        JTextField godineStazaField = new JTextField();
        JTextField osnovaField = new JTextField();
        JButton sacuvajButton = new JButton("Sacuvaj");

        panel.add(new JLabel("Tip"));
        panel.add(tipBox);
        panel.add(new JLabel("Ime"));
        panel.add(imeField);
        panel.add(new JLabel("Prezime"));
        panel.add(prezimeField);
        panel.add(new JLabel("Pol"));
        panel.add(polBox);
        panel.add(new JLabel("Datum rodjenja"));
        panel.add(datumRodjenjaField);
        panel.add(new JLabel("Telefon"));
        panel.add(telefonField);
        panel.add(new JLabel("Adresa"));
        panel.add(adresaField);
        panel.add(new JLabel("Korisnicko ime"));
        panel.add(korisnickoImeField);
        panel.add(new JLabel("Lozinka"));
        panel.add(lozinkaField);
        panel.add(new JLabel("Nivo spreme"));
        panel.add(nivoSpremeBox);
        panel.add(new JLabel("Godine staza"));
        panel.add(godineStazaField);
        panel.add(new JLabel("Osnova"));
        panel.add(osnovaField);
        panel.add(new JLabel(""));
        panel.add(sacuvajButton);

        sacuvajButton.addActionListener(e -> {
            try {
                boolean uspesno;
                if (tipBox.getSelectedItem().equals("AGENT")) {
                    uspesno = zaposleniService.dodajAgenta(ulogovaniKorisnik, imeField.getText(),
                            prezimeField.getText(), (Pol) polBox.getSelectedItem(), datumRodjenjaField.getText(),
                            telefonField.getText(), adresaField.getText(), korisnickoImeField.getText(),
                            new String(lozinkaField.getPassword()), (NivoSpreme) nivoSpremeBox.getSelectedItem(),
                            Integer.parseInt(godineStazaField.getText()), Double.parseDouble(osnovaField.getText()));
                } else {
                    uspesno = zaposleniService.dodajAdministratora(ulogovaniKorisnik, imeField.getText(),
                            prezimeField.getText(), (Pol) polBox.getSelectedItem(), datumRodjenjaField.getText(),
                            telefonField.getText(), adresaField.getText(), korisnickoImeField.getText(),
                            new String(lozinkaField.getPassword()), (NivoSpreme) nivoSpremeBox.getSelectedItem(),
                            Integer.parseInt(godineStazaField.getText()), Double.parseDouble(osnovaField.getText()));
                }

                prikaziPorukuIUcitajAdmin(uspesno, "Zaposleni je dodat.", "Zaposleni nije dodat.");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Godine staza i osnova moraju biti brojevi.");
            }
        });

        return panel;
    }

    private JPanel dodajKlijentaPanel() {
        JPanel panel = new JPanel(new GridLayout(11, 2, 8, 8));
        JTextField imeField = new JTextField();
        JTextField prezimeField = new JTextField();
        JComboBox<Pol> polBox = new JComboBox<>(Pol.values());
        JTextField datumRodjenjaField = new JTextField("2000-01-01");
        JTextField telefonField = new JTextField();
        JTextField adresaField = new JTextField();
        JTextField emailField = new JTextField();
        JPasswordField lozinkaField = new JPasswordField();
        JTextField datumDozvoleField = new JTextField("2020-01-01");
        JComboBox<KategorijaKlijenta> kategorijaBox = new JComboBox<>(KategorijaKlijenta.values());
        JButton sacuvajButton = new JButton("Sacuvaj");

        panel.add(new JLabel("Ime"));
        panel.add(imeField);
        panel.add(new JLabel("Prezime"));
        panel.add(prezimeField);
        panel.add(new JLabel("Pol"));
        panel.add(polBox);
        panel.add(new JLabel("Datum rodjenja"));
        panel.add(datumRodjenjaField);
        panel.add(new JLabel("Telefon"));
        panel.add(telefonField);
        panel.add(new JLabel("Adresa"));
        panel.add(adresaField);
        panel.add(new JLabel("Email"));
        panel.add(emailField);
        panel.add(new JLabel("Lozinka"));
        panel.add(lozinkaField);
        panel.add(new JLabel("Datum dozvole"));
        panel.add(datumDozvoleField);
        panel.add(new JLabel("Kategorija"));
        panel.add(kategorijaBox);
        panel.add(new JLabel(""));
        panel.add(sacuvajButton);

        sacuvajButton.addActionListener(e -> {
            boolean uspesno = klijentService.dodajKlijenta(ulogovaniKorisnik, imeField.getText(),
                    prezimeField.getText(), (Pol) polBox.getSelectedItem(), datumRodjenjaField.getText(),
                    telefonField.getText(), adresaField.getText(), emailField.getText(),
                    new String(lozinkaField.getPassword()), datumDozvoleField.getText(),
                    (KategorijaKlijenta) kategorijaBox.getSelectedItem());

            prikaziPorukuIUcitajAgenta(uspesno, "Klijent je dodat.", "Klijent nije dodat.");
        });

        return panel;
    }

    private void prikaziPorukuIUcitajAdmin(boolean uspesno, String dobraPoruka, String losaPoruka) {
        JOptionPane.showMessageDialog(this, uspesno ? dobraPoruka : losaPoruka);
        if (uspesno) {
            prikaziAdminPanel();
        }
    }

    private void prikaziPorukuIUcitajAgenta(boolean uspesno, String dobraPoruka, String losaPoruka) {
        JOptionPane.showMessageDialog(this, uspesno ? dobraPoruka : losaPoruka);
        if (uspesno) {
            prikaziAgentPanel();
        }
    }
}
