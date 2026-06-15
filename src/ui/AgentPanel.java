package ui;

import enums.KategorijaKlijenta;
import enums.Pol;
import menadzment.KlijentMenadzer;
import menadzment.RezervacijaMenadzer;
import model.Klijent;
import model.Korisnik;
import model.Rezervacija;
import repozitorijum.KorisnikRepozitorijum;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;

public class AgentPanel extends JPanel {
    private final Korisnik agent;
    private final KorisnikRepozitorijum korisnici;
    private final KlijentMenadzer klijentMenadzer;
    private final RezervacijaMenadzer rezervacijaMenadzer;
    private final Runnable osvezi;

    public AgentPanel(Korisnik agent, KorisnikRepozitorijum korisnici, KlijentMenadzer klijentMenadzer,
                      RezervacijaMenadzer rezervacijaMenadzer, Runnable osvezi, Runnable odjava) {
        super(new BorderLayout());
        this.agent = agent;
        this.korisnici = korisnici;
        this.klijentMenadzer = klijentMenadzer;
        this.rezervacijaMenadzer = rezervacijaMenadzer;
        this.osvezi = osvezi;

        JTabbedPane tabs = UiKomponente.tabovi();
        tabs.addTab("Rezervacije", rezervacijePanel());
        tabs.addTab("Klijenti", klijentiPanel());
        tabs.addTab("Dodaj klijenta", dodajKlijentaPanel());
        add(UiKomponente.okvirAplikacije(tabs, "Agent", agent, odjava));
    }

    private JPanel rezervacijePanel() {
        JPanel panel = UiKomponente.kartica(new BorderLayout(0, 12));
        panel.add(UiKomponente.naslovSekcije("Rezervacije"), BorderLayout.NORTH);
        DefaultTableModel model = UiKomponente.modelTabele(
                new String[]{"ID", "Klijent", "Model", "Datum od", "Datum do", "Status"});
        for (Rezervacija rezervacija : rezervacijaMenadzer.ucitajSveRezervacije()) {
            model.addRow(new Object[]{rezervacija.getId(),
                    rezervacija.getKlijent().getIme() + " " + rezervacija.getKlijent().getPrezime(),
                    rezervacija.getModelVozila(), rezervacija.getDatumOd(), rezervacija.getDatumDo(),
                    rezervacija.getStatus()});
        }

        JTable tabela = UiKomponente.tabela(model);
        JButton potvrdi = UiKomponente.primarnoDugme("Potvrdi");
        JButton odbij = new JButton("Odbij");
        potvrdi.addActionListener(e -> obradiRezervaciju(tabela, model, true));
        odbij.addActionListener(e -> obradiRezervaciju(tabela, model, false));
        JPanel dugmad = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        dugmad.setBackground(UiKomponente.PANEL);
        dugmad.add(odbij);
        dugmad.add(potvrdi);
        panel.add(new JScrollPane(tabela), BorderLayout.CENTER);
        panel.add(dugmad, BorderLayout.SOUTH);
        return panel;
    }

    private void obradiRezervaciju(JTable tabela, DefaultTableModel model, boolean potvrda) {
        int red = tabela.getSelectedRow();
        if (red == -1) {
            JOptionPane.showMessageDialog(this, "Izaberite rezervaciju u tabeli.");
            return;
        }
        int id = (int) model.getValueAt(red, 0);
        boolean uspesno = potvrda ? rezervacijaMenadzer.potvrdiRezervaciju(agent, id)
                : rezervacijaMenadzer.odbijRezervaciju(agent, id);
        String poruka = uspesno ? (potvrda ? "Rezervacija je potvrdjena." : "Rezervacija je odbijena.")
                : (potvrda ? "Rezervacija nije potvrdjena. Proverite status, datum i dostupnost."
                : "Rezervacija nije odbijena. Proverite njen status.");
        JOptionPane.showMessageDialog(this, poruka);
        if (uspesno) osvezi.run();
    }

    private JPanel klijentiPanel() {
        JPanel panel = UiKomponente.kartica(new BorderLayout(0, 12));
        panel.add(UiKomponente.naslovSekcije("Klijenti"), BorderLayout.NORTH);
        DefaultTableModel model = new DefaultTableModel(
                new String[]{"Ime", "Prezime", "Email", "Datum dozvole", "Kategorija"}, 0);
        for (Klijent klijent : korisnici.ucitajKlijente()) {
            model.addRow(new Object[]{klijent.getIme(), klijent.getPrezime(), klijent.getKorisnickoIme(),
                    klijent.getDatumDozvole(), klijent.getPosebnaKategorija()});
        }
        panel.add(new JScrollPane(UiKomponente.tabela(model)), BorderLayout.CENTER);
        return panel;
    }

    private JPanel dodajKlijentaPanel() {
        JPanel panel = UiKomponente.kartica(new BorderLayout(0, 14));
        panel.add(UiKomponente.naslovSekcije("Novi klijent"), BorderLayout.NORTH);
        JPanel forma = new JPanel(new GridBagLayout());
        forma.setBackground(UiKomponente.PANEL);
        JTextField ime = new JTextField();
        JTextField prezime = new JTextField();
        JComboBox<Pol> pol = new JComboBox<>(Pol.values());
        JTextField datumRodjenja = new JTextField("2000-01-01");
        JTextField telefon = new JTextField();
        JTextField adresa = new JTextField();
        JTextField email = new JTextField();
        JPasswordField lozinka = new JPasswordField();
        JTextField datumDozvole = new JTextField("2020-01-01");
        JComboBox<KategorijaKlijenta> kategorija = new JComboBox<>(KategorijaKlijenta.values());
        JButton sacuvaj = UiKomponente.primarnoDugme("Sacuvaj klijenta");

        int red = 0;
        red = UiKomponente.dodajPolje(forma, red, "Ime", ime);
        red = UiKomponente.dodajPolje(forma, red, "Prezime", prezime);
        red = UiKomponente.dodajPolje(forma, red, "Pol", pol);
        red = UiKomponente.dodajPolje(forma, red, "Datum rodjenja", datumRodjenja);
        red = UiKomponente.dodajPolje(forma, red, "Telefon", telefon);
        red = UiKomponente.dodajPolje(forma, red, "Adresa", adresa);
        red = UiKomponente.dodajPolje(forma, red, "Email", email);
        red = UiKomponente.dodajPolje(forma, red, "Lozinka", lozinka);
        red = UiKomponente.dodajPolje(forma, red, "Datum dozvole", datumDozvole);
        UiKomponente.dodajPolje(forma, red, "Kategorija", kategorija);

        sacuvaj.addActionListener(e -> {
            boolean uspesno = klijentMenadzer.dodajKlijenta(agent, ime.getText(), prezime.getText(),
                    (Pol) pol.getSelectedItem(), datumRodjenja.getText(), telefon.getText(), adresa.getText(),
                    email.getText(), new String(lozinka.getPassword()), datumDozvole.getText(),
                    (KategorijaKlijenta) kategorija.getSelectedItem());
            JOptionPane.showMessageDialog(this, uspesno ? "Klijent je dodat." : "Klijent nije dodat.");
            if (uspesno) osvezi.run();
        });
        panel.add(forma, BorderLayout.CENTER);
        panel.add(sacuvaj, BorderLayout.SOUTH);
        return panel;
    }
}
