package ui;

import enums.KategorijaKlijenta;
import enums.Pol;
import enums.TipNaplate;
import menadzment.CenovnikMenadzer;
import menadzment.KlijentMenadzer;
import menadzment.RezervacijaMenadzer;
import model.DodatnaUsluga;
import model.Klijent;
import model.Korisnik;
import model.Rezervacija;
import model.RezervacijaUsluga;
import repozitorijum.DodatnaUslugaRepozitorijum;
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
import java.time.LocalDate;
import java.util.ArrayList;

public class AgentPanel extends JPanel {
    private final Korisnik agent;
    private final KorisnikRepozitorijum korisnici;
    private final KlijentMenadzer klijentMenadzer;
    private final RezervacijaMenadzer rezervacijaMenadzer;
    private final CenovnikMenadzer cenovnikMenadzer;
    private final DodatnaUslugaRepozitorijum dodatnaUslugaRepozitorijum;
    private final Runnable osvezi;

    public AgentPanel(Korisnik agent, KorisnikRepozitorijum korisnici, KlijentMenadzer klijentMenadzer,
                      RezervacijaMenadzer rezervacijaMenadzer, CenovnikMenadzer cenovnikMenadzer,
                      DodatnaUslugaRepozitorijum dodatnaUslugaRepozitorijum,
                      Runnable osvezi, Runnable odjava) {
        super(new BorderLayout());
        this.agent = agent;
        this.korisnici = korisnici;
        this.klijentMenadzer = klijentMenadzer;
        this.rezervacijaMenadzer = rezervacijaMenadzer;
        this.cenovnikMenadzer = cenovnikMenadzer;
        this.dodatnaUslugaRepozitorijum = dodatnaUslugaRepozitorijum;
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
                new String[]{"ID", "Klijent", "Model", "Datum od", "Datum do", "Status", "Dodatne usluge", "Ukupno"});
        for (Rezervacija rezervacija : rezervacijaMenadzer.ucitajSveRezervacije()) {
            model.addRow(new Object[]{rezervacija.getId(),
                    rezervacija.getKlijent().getIme() + " " + rezervacija.getKlijent().getPrezime(),
                    rezervacija.getModelVozila(), rezervacija.getDatumOd(), rezervacija.getDatumDo(),
                    rezervacija.getStatus(), opisDodatnihUsluga(rezervacija.getId()),
                    rezervacija.getCenaUkupno()});
        }

        JTable tabela = UiKomponente.tabela(model);
        JButton potvrdi = UiKomponente.primarnoDugme("Potvrdi");
        JButton odbij = new JButton("Odbij");
        JButton dodajUslugu = new JButton("Dodaj dodatnu uslugu");
        potvrdi.addActionListener(e -> obradiRezervaciju(tabela, model, true));
        odbij.addActionListener(e -> obradiRezervaciju(tabela, model, false));
        dodajUslugu.addActionListener(e -> dodajDodatnuUslugu(tabela, model));
        JPanel dugmad = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        dugmad.setBackground(UiKomponente.PANEL);
        dugmad.add(dodajUslugu);
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

    private void dodajDodatnuUslugu(JTable tabela, DefaultTableModel model) {
        int red = tabela.getSelectedRow();
        if (red == -1) {
            JOptionPane.showMessageDialog(this, "Izaberite rezervaciju u tabeli.");
            return;
        }

        int rezervacijaId = (int) model.getValueAt(red, 0);
        JComboBox<DodatnaUsluga> uslugaBox = new JComboBox<>();
        for (DodatnaUsluga dodatnaUsluga : dodatnaUslugaRepozitorijum.ucitajSve()) {
            uslugaBox.addItem(dodatnaUsluga);
        }

        JPanel forma = new JPanel(new GridBagLayout());
        forma.setBackground(UiKomponente.PANEL);
        UiKomponente.dodajPolje(forma, 0, "Dodatna usluga", uslugaBox);

        int izbor = JOptionPane.showConfirmDialog(this, forma, "Dodaj dodatnu uslugu",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (izbor != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            DodatnaUsluga dodatnaUsluga = (DodatnaUsluga) uslugaBox.getSelectedItem();

            if (dodatnaUsluga == null) {
                return;
            }

            int izabranaKolicina = odrediKolicinuDodatneUsluge(dodatnaUsluga);
            if (izabranaKolicina <= 0) {
                return;
            }

            double cenaPoJedinici = cenovnikMenadzer.pronadjiCenuDodatneUsluge(LocalDate.now(), dodatnaUsluga);
            boolean uspesno = rezervacijaMenadzer.dodajDodatnuUsluguNaRezervaciju(
                    agent, rezervacijaId, dodatnaUsluga, izabranaKolicina, cenaPoJedinici);

            JOptionPane.showMessageDialog(this, uspesno ? "Dodatna usluga je dodata."
                    : "Dodatna usluga nije dodata. Rezervacija mora biti potvrdjena.");
            if (uspesno) osvezi.run();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Broj dana mora biti broj.");
        }
    }

    private int odrediKolicinuDodatneUsluge(DodatnaUsluga dodatnaUsluga) {
        if (dodatnaUsluga.getTipNaplate() != TipNaplate.PO_DANU) {
            return 1;
        }

        JTextField brojDanaField = new JTextField("1");
        JPanel forma = new JPanel(new GridBagLayout());
        forma.setBackground(UiKomponente.PANEL);
        UiKomponente.dodajPolje(forma, 0, "Broj dodatnih dana", brojDanaField);

        int izbor = JOptionPane.showConfirmDialog(this, forma, "Produzeno koriscenje",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (izbor != JOptionPane.OK_OPTION) {
            return 0;
        }

        String unos = brojDanaField.getText();
        if (unos == null) {
            return 0;
        }

        int brojDana = Integer.parseInt(unos);
        if (brojDana <= 0) {
            JOptionPane.showMessageDialog(this, "Broj dana mora biti pozitivan broj.");
            return 0;
        }

        return brojDana;
    }

    private String opisDodatnihUsluga(int rezervacijaId) {
        ArrayList<RezervacijaUsluga> usluge = rezervacijaMenadzer.ucitajDodatneUslugeRezervacije(rezervacijaId);

        if (usluge.isEmpty()) {
            return "";
        }

        StringBuilder opis = new StringBuilder();
        for (RezervacijaUsluga usluga : usluge) {
            if (opis.length() > 0) {
                opis.append("; ");
            }

            opis.append(usluga.getDodatnaUsluga().getNaziv());

            if (usluga.getDodatnaUsluga().getTipNaplate() == TipNaplate.PO_DANU) {
                opis.append(" x").append(usluga.getKolicina());
            }
        }

        return opis.toString();
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
