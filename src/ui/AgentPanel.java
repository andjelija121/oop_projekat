package ui;

import enums.KategorijaKlijenta;
import enums.Pol;
import enums.TipNaplate;
import menadzment.CenovnikMenadzer;
import menadzment.IzdavanjeMenadzer;
import menadzment.KlijentMenadzer;
import menadzment.PretplataMenadzer;
import menadzment.RezervacijaMenadzer;
import model.DodatnaUsluga;
import model.Izdavanje;
import model.Klijent;
import model.Korisnik;
import model.Pretplata;
import model.Rezervacija;
import model.RezervacijaUsluga;
import model.Vozilo;
import model.ZahtevPretplate;
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
import java.time.format.DateTimeParseException;
import java.util.ArrayList;

public class AgentPanel extends JPanel {
    private final Korisnik agent;
    private final KorisnikRepozitorijum korisnici;
    private final KlijentMenadzer klijentMenadzer;
    private final RezervacijaMenadzer rezervacijaMenadzer;
    private final IzdavanjeMenadzer izdavanjeMenadzer;
    private final PretplataMenadzer pretplataMenadzer;
    private final CenovnikMenadzer cenovnikMenadzer;
    private final DodatnaUslugaRepozitorijum dodatnaUslugaRepozitorijum;
    private final Runnable osvezi;

    public AgentPanel(Korisnik agent, KorisnikRepozitorijum korisnici, KlijentMenadzer klijentMenadzer,
                      RezervacijaMenadzer rezervacijaMenadzer, IzdavanjeMenadzer izdavanjeMenadzer,
                      PretplataMenadzer pretplataMenadzer,
                      CenovnikMenadzer cenovnikMenadzer, DodatnaUslugaRepozitorijum dodatnaUslugaRepozitorijum,
                      Runnable osvezi, Runnable odjava) {
        super(new BorderLayout());
        this.agent = agent;
        this.korisnici = korisnici;
        this.klijentMenadzer = klijentMenadzer;
        this.rezervacijaMenadzer = rezervacijaMenadzer;
        this.izdavanjeMenadzer = izdavanjeMenadzer;
        this.pretplataMenadzer = pretplataMenadzer;
        this.cenovnikMenadzer = cenovnikMenadzer;
        this.dodatnaUslugaRepozitorijum = dodatnaUslugaRepozitorijum;
        this.osvezi = osvezi;

        JTabbedPane tabs = UiKomponente.tabovi();
        tabs.addTab("Rezervacije", rezervacijePanel());
        tabs.addTab("Izdavanja", izdavanjaPanel());
        tabs.addTab("Vozila", vozilaPanel());
        tabs.addTab("Klijenti", klijentiPanel());
        tabs.addTab("Pretplate", pretplatePanel());
        tabs.addTab("Dodaj klijenta", dodajKlijentaPanel());
        add(UiKomponente.okvirAplikacije(tabs, "Agent", agent, odjava));
    }

    private JPanel rezervacijePanel() {
        JPanel panel = UiKomponente.kartica(new BorderLayout(0, 12));
        panel.add(UiKomponente.naslovSekcije("Rezervacije"), BorderLayout.NORTH);
        DefaultTableModel model = UiKomponente.modelTabele(
                new String[]{"ID", "Klijent", "Model", "Datum od", "Datum do", "Status",
                        "Izdavanje", "Dodatne usluge", "Kazna", "Ukupno"});
        for (Rezervacija rezervacija : rezervacijaMenadzer.ucitajSveRezervacije()) {
            model.addRow(new Object[]{rezervacija.getId(),
                    rezervacija.getKlijent().getIme() + " " + rezervacija.getKlijent().getPrezime(),
                    rezervacija.getModelVozila(), rezervacija.getDatumOd(), rezervacija.getDatumDo(),
                    rezervacija.getStatus(), izdavanjeMenadzer.opisIzdavanjaRezervacije(rezervacija.getId()),
                    opisDodatnihUsluga(rezervacija.getId()),
                    rezervacija.getKazna(),
                    rezervacija.getCenaUkupno()});
        }

        JTable tabela = UiKomponente.tabela(model);
        JButton potvrdi = UiKomponente.primarnoDugme("Potvrdi");
        JButton odbij = new JButton("Odbij");
        JButton dodajUslugu = new JButton("Dodaj dodatnu uslugu");
        JButton izdaj = new JButton("Izdaj vozilo");
        potvrdi.addActionListener(e -> obradiRezervaciju(tabela, model, true));
        odbij.addActionListener(e -> obradiRezervaciju(tabela, model, false));
        dodajUslugu.addActionListener(e -> dodajDodatnuUslugu(tabela, model));
        izdaj.addActionListener(e -> izdajVozilo(tabela, model));
        JPanel dugmad = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        dugmad.setBackground(UiKomponente.PANEL);
        dugmad.add(dodajUslugu);
        dugmad.add(izdaj);
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

    private void izdajVozilo(JTable tabela, DefaultTableModel model) {
        int red = tabela.getSelectedRow();
        if (red == -1) {
            JOptionPane.showMessageDialog(this, "Izaberite rezervaciju u tabeli.");
            return;
        }

        int rezervacijaId = (int) model.getValueAt(red, 0);
        ArrayList<Vozilo> dostupnaVozila = izdavanjeMenadzer.ucitajDostupnaVozilaZaRezervaciju(rezervacijaId);
        if (dostupnaVozila.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nema dostupnih vozila za izabranu rezervaciju.");
            return;
        }

        JComboBox<Vozilo> voziloBox = new JComboBox<>();
        for (Vozilo vozilo : dostupnaVozila) {
            voziloBox.addItem(vozilo);
        }

        JTextField kilometraza = new JTextField(String.valueOf(dostupnaVozila.get(0).getKilometraza()));
        JPanel forma = new JPanel(new GridBagLayout());
        forma.setBackground(UiKomponente.PANEL);
        int formaRed = 0;
        formaRed = UiKomponente.dodajPolje(forma, formaRed, "Vozilo", voziloBox);
        UiKomponente.dodajPolje(forma, formaRed, "Kilometraza", kilometraza);

        int izbor = JOptionPane.showConfirmDialog(this, forma, "Izdavanje vozila",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (izbor != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            Vozilo vozilo = (Vozilo) voziloBox.getSelectedItem();
            boolean uspesno = vozilo != null && izdavanjeMenadzer.izdajVozilo(
                    agent, rezervacijaId, vozilo.getId(), Integer.parseInt(kilometraza.getText().trim()));

            JOptionPane.showMessageDialog(this, uspesno ? "Vozilo je izdato."
                    : "Vozilo nije izdato. Rezervacija mora biti potvrdjena i ne sme vec imati izdavanje.");
            if (uspesno) osvezi.run();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Kilometraza mora biti ceo broj.");
        }
    }

    private JPanel izdavanjaPanel() {
        JPanel panel = UiKomponente.kartica(new BorderLayout(0, 12));
        panel.add(UiKomponente.naslovSekcije("Aktivna izdavanja"), BorderLayout.NORTH);
        DefaultTableModel model = UiKomponente.modelTabele(
                new String[]{"ID", "Rezervacija", "Klijent", "Vozilo", "Planirano vracanje", "Km preuzimanje"});

        for (Izdavanje izdavanje : izdavanjeMenadzer.ucitajAktivnaIzdavanja()) {
            model.addRow(new Object[]{izdavanje.getId(), izdavanje.getRezervacija().getId(),
                    izdavanje.getRezervacija().getKlijent().getIme() + " "
                            + izdavanje.getRezervacija().getKlijent().getPrezime(),
                    izdavanje.getVozilo().getRegistracija(), izdavanje.getDatumVracanjaPlanirano(),
                    izdavanje.getKilometrazaPreuzimanje()});
        }

        JTable tabela = UiKomponente.tabela(model);
        JButton vrati = UiKomponente.primarnoDugme("Vrati vozilo");
        vrati.addActionListener(e -> vratiVozilo(tabela, model));
        JPanel dugmad = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        dugmad.setBackground(UiKomponente.PANEL);
        dugmad.add(vrati);
        panel.add(new JScrollPane(tabela), BorderLayout.CENTER);
        panel.add(dugmad, BorderLayout.SOUTH);
        return panel;
    }

    private void vratiVozilo(JTable tabela, DefaultTableModel model) {
        int red = tabela.getSelectedRow();
        if (red == -1) {
            JOptionPane.showMessageDialog(this, "Izaberite izdavanje u tabeli.");
            return;
        }

        int izdavanjeId = (int) model.getValueAt(red, 0);
        JTextField datumVracanja = new JTextField(LocalDate.now().toString());
        JTextField kilometraza = new JTextField();
        JPanel forma = new JPanel(new GridBagLayout());
        forma.setBackground(UiKomponente.PANEL);
        int formaRed = 0;
        formaRed = UiKomponente.dodajPolje(forma, formaRed, "Datum vracanja", datumVracanja);
        UiKomponente.dodajPolje(forma, formaRed, "Kilometraza", kilometraza);

        int izbor = JOptionPane.showConfirmDialog(this, forma, "Vracanje vozila",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (izbor != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            boolean uspesno = izdavanjeMenadzer.vratiVozilo(agent, izdavanjeId,
                    LocalDate.parse(datumVracanja.getText().trim()),
                    Integer.parseInt(kilometraza.getText().trim()));
            JOptionPane.showMessageDialog(this, uspesno ? "Vozilo je vraceno."
                    : "Vozilo nije vraceno. Proverite datum, kilometrazu i podatke izdavanja.");
            if (uspesno) osvezi.run();
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Datum mora biti u formatu GGGG-MM-DD.");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Kilometraza mora biti ceo broj.");
        }
    }

    private JPanel vozilaPanel() {
        JPanel panel = UiKomponente.kartica(new BorderLayout(0, 12));
        panel.add(UiKomponente.naslovSekcije("Vozila"), BorderLayout.NORTH);
        DefaultTableModel model = UiKomponente.modelTabele(
                new String[]{"ID", "Model", "Registracija", "Status", "Kilometraza"});

        for (Vozilo vozilo : izdavanjeMenadzer.ucitajVozila()) {
            model.addRow(new Object[]{vozilo.getId(), vozilo.getModelVozila(),
                    vozilo.getRegistracija(), vozilo.getStatus(), vozilo.getKilometraza()});
        }

        panel.add(new JScrollPane(UiKomponente.tabela(model)), BorderLayout.CENTER);
        return panel;
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

    private JPanel pretplatePanel() {
        JPanel panel = UiKomponente.kartica(new BorderLayout(0, 12));
        panel.add(UiKomponente.naslovSekcije("Zahtevi za pretplatu"), BorderLayout.NORTH);
        DefaultTableModel model = UiKomponente.modelTabele(
                new String[]{"ID", "Klijent", "Agent", "Datum zahteva", "Status"});

        for (ZahtevPretplate zahtev : pretplataMenadzer.ucitajSveZahteve()) {
            String klijent = zahtev.getKlijent() == null ? "" :
                    zahtev.getKlijent().getIme() + " " + zahtev.getKlijent().getPrezime();
            String agentZahteva = zahtev.getAgent() == null ? "" :
                    zahtev.getAgent().getIme() + " " + zahtev.getAgent().getPrezime();
            model.addRow(new Object[]{zahtev.getId(), klijent, agentZahteva,
                    zahtev.getDatumZahteva(), zahtev.getStatus()});
        }

        JTable tabela = UiKomponente.tabela(model);
        JButton odobri = UiKomponente.primarnoDugme("Odobri");
        JButton odbij = new JButton("Odbij");
        odobri.addActionListener(e -> obradiZahtevPretplate(tabela, model, true));
        odbij.addActionListener(e -> obradiZahtevPretplate(tabela, model, false));

        JPanel dugmad = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        dugmad.setBackground(UiKomponente.PANEL);
        dugmad.add(odbij);
        dugmad.add(odobri);
        panel.add(new JScrollPane(tabela), BorderLayout.CENTER);
        panel.add(dugmad, BorderLayout.SOUTH);
        return panel;
    }

    private void obradiZahtevPretplate(JTable tabela, DefaultTableModel model, boolean odobravanje) {
        int red = tabela.getSelectedRow();
        if (red == -1) {
            JOptionPane.showMessageDialog(this, "Izaberite zahtev u tabeli.");
            return;
        }

        int id = (int) model.getValueAt(red, 0);
        boolean uspesno = odobravanje ? pretplataMenadzer.odobriZahtev(agent, id)
                : pretplataMenadzer.odbijZahtev(agent, id);
        String poruka = uspesno ? (odobravanje ? "Pretplata je odobrena." : "Zahtev je odbijen.")
                : (odobravanje ? "Zahtev nije odobren. Proverite status i broj kasnjenja."
                : "Zahtev nije odbijen. Proverite status.");
        JOptionPane.showMessageDialog(this, poruka);
        if (uspesno) osvezi.run();
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
