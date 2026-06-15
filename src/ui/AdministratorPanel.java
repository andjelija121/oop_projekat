package ui;

import enums.NivoSpreme;
import enums.Pol;
import menadzment.ZaposleniMenadzer;
import model.Administrator;
import model.Agent;
import model.Korisnik;
import model.Zaposleni;
import repozitorijum.KorisnikRepozitorijum;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.GridBagLayout;

public class AdministratorPanel extends JPanel {
    private final Korisnik administrator;
    private final KorisnikRepozitorijum korisnici;
    private final ZaposleniMenadzer zaposleniMenadzer;
    private final Runnable osvezi;

    public AdministratorPanel(Korisnik administrator, KorisnikRepozitorijum korisnici,
                              ZaposleniMenadzer zaposleniMenadzer, Runnable osvezi, Runnable odjava) {
        super(new BorderLayout());
        this.administrator = administrator;
        this.korisnici = korisnici;
        this.zaposleniMenadzer = zaposleniMenadzer;
        this.osvezi = osvezi;

        JTabbedPane tabs = UiKomponente.tabovi();
        tabs.addTab("Zaposleni", zaposleniTabelaPanel());
        tabs.addTab("Dodaj zaposlenog", dodajZaposlenogPanel());
        add(UiKomponente.okvirAplikacije(tabs, "Administracija", administrator, odjava));
    }

    private JPanel zaposleniTabelaPanel() {
        JPanel panel = UiKomponente.kartica(new BorderLayout(0, 12));
        panel.add(UiKomponente.naslovSekcije("Zaposleni"), BorderLayout.NORTH);
        DefaultTableModel model = new DefaultTableModel(
                new String[]{"Tip", "Ime", "Prezime", "Nivo", "Staz", "Plata"}, 0);

        for (Administrator zaposleni : korisnici.ucitajAdministratore()) {
            dodajZaposlenogUTabelu(model, "Administrator", zaposleni);
        }
        for (Agent zaposleni : korisnici.ucitajAgente()) {
            dodajZaposlenogUTabelu(model, "Agent", zaposleni);
        }
        panel.add(new JScrollPane(UiKomponente.tabela(model)), BorderLayout.CENTER);
        return panel;
    }

    private void dodajZaposlenogUTabelu(DefaultTableModel model, String tip, Zaposleni zaposleni) {
        model.addRow(new Object[]{tip, zaposleni.getIme(), zaposleni.getPrezime(),
                zaposleni.getNivoSpreme(), zaposleni.getGodineStaza(),
                String.format("%.2f", zaposleni.getPlata())});
    }

    private JPanel dodajZaposlenogPanel() {
        JPanel panel = UiKomponente.kartica(new BorderLayout(0, 14));
        panel.add(UiKomponente.naslovSekcije("Novi zaposleni"), BorderLayout.NORTH);
        JPanel forma = new JPanel(new GridBagLayout());
        forma.setBackground(UiKomponente.PANEL);

        JComboBox<String> tipBox = new JComboBox<>(new String[]{"AGENT", "ADMINISTRATOR"});
        JTextField ime = new JTextField();
        JTextField prezime = new JTextField();
        JComboBox<Pol> pol = new JComboBox<>(Pol.values());
        JTextField datumRodjenja = new JTextField("1990-01-01");
        JTextField telefon = new JTextField();
        JTextField adresa = new JTextField();
        JTextField korisnickoIme = new JTextField();
        JPasswordField lozinka = new JPasswordField();
        JComboBox<NivoSpreme> nivo = new JComboBox<>(NivoSpreme.values());
        JTextField staz = new JTextField();
        JTextField osnova = new JTextField("50000");
        JButton sacuvaj = UiKomponente.primarnoDugme("Sacuvaj zaposlenog");

        int red = 0;
        red = UiKomponente.dodajPolje(forma, red, "Tip", tipBox);
        red = UiKomponente.dodajPolje(forma, red, "Ime", ime);
        red = UiKomponente.dodajPolje(forma, red, "Prezime", prezime);
        red = UiKomponente.dodajPolje(forma, red, "Pol", pol);
        red = UiKomponente.dodajPolje(forma, red, "Datum rodjenja", datumRodjenja);
        red = UiKomponente.dodajPolje(forma, red, "Telefon", telefon);
        red = UiKomponente.dodajPolje(forma, red, "Adresa", adresa);
        red = UiKomponente.dodajPolje(forma, red, "Korisnicko ime", korisnickoIme);
        red = UiKomponente.dodajPolje(forma, red, "Lozinka", lozinka);
        red = UiKomponente.dodajPolje(forma, red, "Nivo spreme", nivo);
        red = UiKomponente.dodajPolje(forma, red, "Godine staza", staz);
        UiKomponente.dodajPolje(forma, red, "Osnovna plata", osnova);

        sacuvaj.addActionListener(e -> {
            try {
                boolean uspesno;
                if ("AGENT".equals(tipBox.getSelectedItem())) {
                    uspesno = zaposleniMenadzer.dodajAgenta(administrator, ime.getText(), prezime.getText(),
                            (Pol) pol.getSelectedItem(), datumRodjenja.getText(), telefon.getText(), adresa.getText(),
                            korisnickoIme.getText(), new String(lozinka.getPassword()),
                            (NivoSpreme) nivo.getSelectedItem(), Integer.parseInt(staz.getText().trim()),
                            Double.parseDouble(osnova.getText().trim().replace(',', '.')));
                } else {
                    uspesno = zaposleniMenadzer.dodajAdministratora(administrator, ime.getText(), prezime.getText(),
                            (Pol) pol.getSelectedItem(), datumRodjenja.getText(), telefon.getText(), adresa.getText(),
                            korisnickoIme.getText(), new String(lozinka.getPassword()),
                            (NivoSpreme) nivo.getSelectedItem(), Integer.parseInt(staz.getText().trim()),
                            Double.parseDouble(osnova.getText().trim().replace(',', '.')));
                }
                JOptionPane.showMessageDialog(this, uspesno ? "Zaposleni je dodat." : "Zaposleni nije dodat.");
                if (uspesno) osvezi.run();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this,
                        "Godine staza moraju biti ceo broj, a osnovna plata broj (na primer 50000).");
            }
        });

        panel.add(forma, BorderLayout.CENTER);
        panel.add(sacuvaj, BorderLayout.SOUTH);
        return panel;
    }
}
