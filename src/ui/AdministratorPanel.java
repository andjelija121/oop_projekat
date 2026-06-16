package ui;

import enums.KategorijaKlijenta;
import enums.KategorijaVozila;
import enums.NivoSpreme;
import enums.Pol;
import enums.StatusRezervacije;
import enums.TipCene;
import menadzment.CenovnikMenadzer;
import menadzment.IzvestajMenadzer;
import menadzment.ZaposleniMenadzer;
import model.Administrator;
import model.Agent;
import model.Cenovnik;
import model.DodatnaUsluga;
import model.Korisnik;
import model.StavkaCenovnika;
import model.Zaposleni;
import repozitorijum.DodatnaUslugaRepozitorijum;
import repozitorijum.KorisnikRepozitorijum;

import javax.swing.JButton;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
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
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class AdministratorPanel extends JPanel {
    private final Korisnik administrator;
    private final KorisnikRepozitorijum korisnici;
    private final ZaposleniMenadzer zaposleniMenadzer;
    private final CenovnikMenadzer cenovnikMenadzer;
    private final IzvestajMenadzer izvestajMenadzer;
    private final DodatnaUslugaRepozitorijum dodatnaUslugaRepozitorijum;
    private final Runnable osvezi;

    public AdministratorPanel(Korisnik administrator, KorisnikRepozitorijum korisnici,
                              ZaposleniMenadzer zaposleniMenadzer,
                              CenovnikMenadzer cenovnikMenadzer,
                              IzvestajMenadzer izvestajMenadzer,
                              DodatnaUslugaRepozitorijum dodatnaUslugaRepozitorijum,
                              Runnable osvezi, Runnable odjava) {
        super(new BorderLayout());
        this.administrator = administrator;
        this.korisnici = korisnici;
        this.zaposleniMenadzer = zaposleniMenadzer;
        this.cenovnikMenadzer = cenovnikMenadzer;
        this.izvestajMenadzer = izvestajMenadzer;
        this.dodatnaUslugaRepozitorijum = dodatnaUslugaRepozitorijum;
        this.osvezi = osvezi;

        JTabbedPane tabs = UiKomponente.tabovi();
        tabs.addTab("Zaposleni", zaposleniTabelaPanel());
        tabs.addTab("Izvestaji", izvestajiPanel());
        tabs.addTab("Chartovi", chartoviPanel());
        tabs.addTab("Dodaj zaposlenog", dodajZaposlenogPanel());
        tabs.addTab("Cenovnik", cenovnikPanel());
        tabs.addTab("Podaci sistema", podaciSistemaPanel());
        tabs.addTab("Podesavanja", podesavanjaPanel());
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

    private JPanel izvestajiPanel() {
        JPanel panel = UiKomponente.kartica(new BorderLayout(0, 12));
        panel.add(UiKomponente.naslovSekcije("Izvestaji"), BorderLayout.NORTH);

        JTextField datumOd = new JTextField(LocalDate.now().minusMonths(1).toString());
        JTextField datumDo = new JTextField(LocalDate.now().toString());
        JButton prikazi = UiKomponente.primarnoDugme("Prikazi");

        JPanel filter = new JPanel(new GridBagLayout());
        filter.setBackground(UiKomponente.PANEL);
        int red = 0;
        red = UiKomponente.dodajPolje(filter, red, "Datum od", datumOd);
        UiKomponente.dodajPolje(filter, red, "Datum do", datumDo);

        DefaultTableModel izdavanjaModel = UiKomponente.modelTabele(
                new String[]{"Agent", "Broj izdavanja"});
        DefaultTableModel rezervacijeModel = UiKomponente.modelTabele(
                new String[]{"Status", "Broj rezervacija"});
        DefaultTableModel modeliModel = UiKomponente.modelTabele(
                new String[]{"Model", "Proizvodjac", "Kategorija", "Iznajmljivanja", "Rezervacije"});
        DefaultTableModel finansijeModel = UiKomponente.modelTabele(
                new String[]{"Stavka", "Iznos"});

        prikazi.addActionListener(e -> {
            try {
                LocalDate od = LocalDate.parse(datumOd.getText().trim());
                LocalDate doDatuma = LocalDate.parse(datumDo.getText().trim());
                if (doDatuma.isBefore(od)) {
                    JOptionPane.showMessageDialog(this, "Datum do ne sme biti pre datuma od.");
                    return;
                }
                popuniIzvestaje(od, doDatuma, izdavanjaModel, rezervacijeModel, modeliModel, finansijeModel);
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(this, "Datumi moraju biti u formatu GGGG-MM-DD.");
            }
        });

        JPanel filterPanel = new JPanel(new BorderLayout(8, 0));
        filterPanel.setBackground(UiKomponente.PANEL);
        filterPanel.add(filter, BorderLayout.CENTER);
        filterPanel.add(prikazi, BorderLayout.EAST);

        JTabbedPane taboviIzvestaja = UiKomponente.tabovi();
        taboviIzvestaja.addTab("Izdavanja", new JScrollPane(UiKomponente.tabela(izdavanjaModel)));
        taboviIzvestaja.addTab("Rezervacije", new JScrollPane(UiKomponente.tabela(rezervacijeModel)));
        taboviIzvestaja.addTab("Modeli vozila", new JScrollPane(UiKomponente.tabela(modeliModel)));
        taboviIzvestaja.addTab("Prihodi i rashodi", new JScrollPane(UiKomponente.tabela(finansijeModel)));

        panel.add(filterPanel, BorderLayout.NORTH);
        panel.add(taboviIzvestaja, BorderLayout.CENTER);
        popuniIzvestaje(LocalDate.now().minusMonths(1), LocalDate.now(),
                izdavanjaModel, rezervacijeModel, modeliModel, finansijeModel);
        return panel;
    }

    private void popuniIzvestaje(LocalDate datumOd, LocalDate datumDo,
                                 DefaultTableModel izdavanjaModel,
                                 DefaultTableModel rezervacijeModel,
                                 DefaultTableModel modeliModel,
                                 DefaultTableModel finansijeModel) {
        ocisti(izdavanjaModel);
        ocisti(rezervacijeModel);
        ocisti(modeliModel);
        ocisti(finansijeModel);

        for (IzvestajMenadzer.IzdavanjeAgenta red : izvestajMenadzer.izvestajIzdavanja(datumOd, datumDo)) {
            izdavanjaModel.addRow(new Object[]{imePrezime(red.getAgent()), red.getBrojIzdavanja()});
        }

        IzvestajMenadzer.RezervacijeStatistika statusi = izvestajMenadzer.izvestajRezervacija(datumOd, datumDo);
        rezervacijeModel.addRow(new Object[]{"POTVRDJENA", statusi.getPotvrdjene()});
        rezervacijeModel.addRow(new Object[]{"ODBIJENA", statusi.getOdbijene()});
        rezervacijeModel.addRow(new Object[]{"OTKAZANA", statusi.getOtkazane()});
        rezervacijeModel.addRow(new Object[]{"ZAVRSENA", statusi.getZavrsene()});

        for (IzvestajMenadzer.ModelVozilaStatistika red : izvestajMenadzer.izvestajModela(datumOd, datumDo)) {
            modeliModel.addRow(new Object[]{red.getModel().getNaziv(), red.getModel().getProizvodjac(),
                    red.getModel().getKategorijaVozila(), red.getBrojIznajmljivanja(), red.getBrojRezervacija()});
        }

        IzvestajMenadzer.PrihodiRashodi finansije = izvestajMenadzer.izvestajPrihodaIRashoda(datumOd, datumDo);
        finansijeModel.addRow(new Object[]{"Pretplate", formatIznos(finansije.getPretplate())});
        finansijeModel.addRow(new Object[]{"Najmovi vozila", formatIznos(finansije.getNajmovi())});
        finansijeModel.addRow(new Object[]{"Dodatne usluge", formatIznos(finansije.getDodatneUsluge())});
        finansijeModel.addRow(new Object[]{"Kazne", formatIznos(finansije.getKazne())});
        finansijeModel.addRow(new Object[]{"Prihodi ukupno", formatIznos(finansije.getPrihodiUkupno())});
        finansijeModel.addRow(new Object[]{"Rashodi - plate", formatIznos(finansije.getRashodi())});
        finansijeModel.addRow(new Object[]{"Profit", formatIznos(finansije.getProfit())});
    }

    private JPanel chartoviPanel() {
        JPanel panel = UiKomponente.kartica(new BorderLayout(0, 12));
        panel.add(UiKomponente.naslovSekcije("Chartovi"), BorderLayout.NORTH);

        JTabbedPane tabs = UiKomponente.tabovi();
        tabs.addTab("Prihodi 12 meseci", prihodiChartPanel());
        tabs.addTab("Rezervacije 30 dana", rezervacijeChartPanel());
        panel.add(tabs, BorderLayout.CENTER);
        return panel;
    }

    private JPanel prihodiChartPanel() {
        JPanel panel = UiKomponente.kartica(new BorderLayout(0, 10));
        LinkedHashMap<YearMonth, LinkedHashMap<KategorijaKlijenta, Double>> podaci =
                izvestajMenadzer.prihodiPoMesecimaIKategoriji();
        ArrayList<String> meseci = new ArrayList<>();
        ArrayList<String> kategorije = new ArrayList<>();
        KategorijaKlijenta[] kategorijeEnum = KategorijaKlijenta.values();
        double[][] vrednosti = new double[kategorijeEnum.length + 1][podaci.size()];
        double ukupno = 0;

        for (KategorijaKlijenta kategorija : kategorijeEnum) {
            kategorije.add(nazivKategorije(kategorija));
        }
        kategorije.add("Ukupno");

        int indeksMeseca = 0;
        for (Map.Entry<YearMonth, LinkedHashMap<KategorijaKlijenta, Double>> entry : podaci.entrySet()) {
            meseci.add(nazivMeseca(entry.getKey()));
            double ukupnoMesec = 0;
            for (int i = 0; i < kategorijeEnum.length; i++) {
                double iznos = entry.getValue().get(kategorijeEnum[i]);
                vrednosti[i][indeksMeseca] = iznos;
                ukupnoMesec += iznos;
                ukupno += iznos;
            }
            vrednosti[kategorijeEnum.length][indeksMeseca] = ukupnoMesec;
            indeksMeseca++;
        }

        JLabel ukupnoLabel = new JLabel("Ukupan prihod: " + formatIznos(ukupno));
        ukupnoLabel.setFont(ukupnoLabel.getFont().deriveFont(Font.BOLD));
        panel.add(ukupnoLabel, BorderLayout.NORTH);
        panel.add(new JScrollPane(new LineChartPanel("Prihodi po kategoriji klijenta",
                meseci, kategorije, vrednosti)), BorderLayout.CENTER);
        return panel;
    }

    private JPanel rezervacijeChartPanel() {
        JPanel panel = UiKomponente.kartica(new BorderLayout(0, 12));
        JPanel sadrzaj = new JPanel(new GridBagLayout());
        sadrzaj.setBackground(UiKomponente.PANEL);

        LinkedHashMap<Agent, Integer> agenti = izvestajMenadzer.opterecenjeAgenataZaPrethodnih30Dana();
        ArrayList<String> agentLabels = new ArrayList<>();
        ArrayList<Double> agentValues = new ArrayList<>();
        for (Map.Entry<Agent, Integer> entry : agenti.entrySet()) {
            agentLabels.add(imePrezime(entry.getKey()));
            agentValues.add((double) entry.getValue());
        }

        IzvestajMenadzer.RezervacijeStatistika statusi =
                izvestajMenadzer.statusiRezervacijaKreiranihZaPrethodnih30Dana();
        ArrayList<String> statusLabels = new ArrayList<>();
        ArrayList<Double> statusValues = new ArrayList<>();
        statusLabels.add("NA_CEKANJU");
        statusValues.add((double) statusi.getNaCekanju());
        statusLabels.add("POTVRDJENA");
        statusValues.add((double) statusi.getPotvrdjene());
        statusLabels.add("ODBIJENA");
        statusValues.add((double) statusi.getOdbijene());
        statusLabels.add("OTKAZANA");
        statusValues.add((double) statusi.getOtkazane());
        statusLabels.add("ZAVRSENA");
        statusValues.add((double) statusi.getZavrsene());

        java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.fill = java.awt.GridBagConstraints.BOTH;
        gbc.insets = new java.awt.Insets(12, 12, 12, 12);
        sadrzaj.add(new PieChartPanel("Status rezervacija u prethodnih 30 dana",
                statusLabels, statusValues), gbc);

        gbc.gridx = 1;
        sadrzaj.add(new PieChartPanel("Opterecenje agenata u prethodnih 30 dana",
                agentLabels, agentValues), gbc);

        panel.add(new JScrollPane(sadrzaj), BorderLayout.CENTER);
        return panel;
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

    private void ocisti(DefaultTableModel model) {
        model.setRowCount(0);
    }

    private String imePrezime(Korisnik korisnik) {
        return korisnik == null ? "" : korisnik.getIme() + " " + korisnik.getPrezime();
    }

    private String formatIznos(double iznos) {
        return String.format("%.2f", iznos);
    }

    private String nazivKategorije(KategorijaKlijenta kategorija) {
        if (kategorija == KategorijaKlijenta.STUDENT) {
            return "Student";
        } else if (kategorija == KategorijaKlijenta.PENZIONER) {
            return "Penzioner";
        } else if (kategorija == KategorijaKlijenta.FIRMA) {
            return "Firma";
        }

        return "Bez kategorije";
    }

    private String nazivMeseca(YearMonth mesec) {
        String[] nazivi = {"Januar", "Februar", "Mart", "April", "Maj", "Jun",
                "Jul", "Avgust", "Septembar", "Oktobar", "Novembar", "Decembar"};
        return nazivi[mesec.getMonthValue() - 1];
    }

    private JPanel podaciSistemaPanel() {
        JPanel panel = UiKomponente.kartica(new BorderLayout(0, 12));
        panel.add(UiKomponente.naslovSekcije("Podaci sistema"), BorderLayout.NORTH);

        JTabbedPane tabovi = UiKomponente.tabovi();
        for (CsvEntitet entitet : csvEntiteti()) {
            tabovi.addTab(entitet.naziv, csvCrudPanel(entitet));
        }

        panel.add(tabovi, BorderLayout.CENTER);
        return panel;
    }

    private ArrayList<CsvEntitet> csvEntiteti() {
        ArrayList<CsvEntitet> entiteti = new ArrayList<>();
        entiteti.add(new CsvEntitet("Korisnici", "src/fajlovi/korisnici.csv"));
        entiteti.add(new CsvEntitet("Pretplate", "src/fajlovi/pretplate.csv"));
        entiteti.add(new CsvEntitet("Zahtevi pretplate", "src/fajlovi/zahtevi_pretplate.csv"));
        entiteti.add(new CsvEntitet("Rezervacije", "src/fajlovi/rezervacije.csv"));
        entiteti.add(new CsvEntitet("Rezervacija-usluge", "src/fajlovi/rezervacija_usluge.csv"));
        entiteti.add(new CsvEntitet("Izdavanja", "src/fajlovi/izdavanja.csv"));
        entiteti.add(new CsvEntitet("Modeli vozila", "src/fajlovi/modeli_vozila.csv"));
        entiteti.add(new CsvEntitet("Vozila", "src/fajlovi/vozila.csv"));
        entiteti.add(new CsvEntitet("Dodatne usluge", "src/fajlovi/dodatne_usluge.csv"));
        entiteti.add(new CsvEntitet("Cenovnik", "src/fajlovi/cenovnik.csv"));
        entiteti.add(new CsvEntitet("Podesavanja", "src/fajlovi/podesavanja.csv"));
        return entiteti;
    }

    private JPanel csvCrudPanel(CsvEntitet entitet) {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(UiKomponente.PANEL);

        DefaultTableModel model = ucitajCsvModel(entitet);
        JTable tabela = UiKomponente.tabela(model);
        JButton dodaj = new JButton("Dodaj red");
        JButton izmeni = new JButton("Izmeni izabrani red");
        JButton obrisi = new JButton("Obrisi izabrani red");
        JButton sacuvaj = UiKomponente.primarnoDugme("Sacuvaj izmene");

        dodaj.addActionListener(e -> dodajPrazanRed(model));
        izmeni.addActionListener(e -> izmeniIzabraniRed(tabela, model));
        obrisi.addActionListener(e -> obrisiIzabraniRed(tabela, model));
        sacuvaj.addActionListener(e -> sacuvajCsvModel(entitet, model));

        JPanel dugmad = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        dugmad.setBackground(UiKomponente.PANEL);
        dugmad.add(dodaj);
        dugmad.add(izmeni);
        dugmad.add(obrisi);
        dugmad.add(sacuvaj);

        panel.add(new JScrollPane(tabela), BorderLayout.CENTER);
        panel.add(dugmad, BorderLayout.SOUTH);
        return panel;
    }

    private DefaultTableModel ucitajCsvModel(CsvEntitet entitet) {
        try {
            ArrayList<String> linije = new ArrayList<>(Files.readAllLines(entitet.putanja, StandardCharsets.UTF_8));
            if (linije.isEmpty()) {
                return new DefaultTableModel();
            }

            String[] kolone = linije.get(0).split(",", -1);
            DefaultTableModel model = new DefaultTableModel(kolone, 0);
            for (int i = 1; i < linije.size(); i++) {
                if (!linije.get(i).isBlank()) {
                    model.addRow(redSaTacnimBrojemKolona(linije.get(i), kolone.length));
                }
            }

            return model;
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Ne mogu da ucitam fajl: " + entitet.putanja);
            return new DefaultTableModel();
        }
    }

    private String[] redSaTacnimBrojemKolona(String linija, int brojKolona) {
        String[] delovi = linija.split(",", -1);
        String[] red = new String[brojKolona];
        for (int i = 0; i < brojKolona; i++) {
            red[i] = i < delovi.length ? delovi[i] : "";
        }
        return red;
    }

    private void dodajPrazanRed(DefaultTableModel model) {
        Object[] red = new Object[model.getColumnCount()];
        for (int i = 0; i < red.length; i++) {
            red[i] = "";
        }

        if (model.getColumnCount() > 0 && "id".equalsIgnoreCase(model.getColumnName(0))) {
            red[0] = sledeciIdIzTabele(model);
        }

        model.addRow(red);
    }

    private void izmeniIzabraniRed(JTable tabela, DefaultTableModel model) {
        int redTabele = tabela.getSelectedRow();
        if (redTabele == -1) {
            JOptionPane.showMessageDialog(this, "Izaberite red u tabeli.");
            return;
        }

        int redModela = tabela.convertRowIndexToModel(redTabele);
        JPanel forma = new JPanel(new GridBagLayout());
        forma.setBackground(UiKomponente.PANEL);
        ArrayList<JTextField> polja = new ArrayList<>();

        for (int i = 0; i < model.getColumnCount(); i++) {
            JTextField polje = new JTextField(vrednostCelije(model, redModela, i));
            polja.add(polje);
            UiKomponente.dodajPolje(forma, i, model.getColumnName(i), polje);
        }

        int izbor = JOptionPane.showConfirmDialog(this, new JScrollPane(forma),
                "Izmena reda", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (izbor != JOptionPane.OK_OPTION) {
            return;
        }

        for (int i = 0; i < polja.size(); i++) {
            model.setValueAt(polja.get(i).getText().trim(), redModela, i);
        }
    }

    private String vrednostCelije(DefaultTableModel model, int red, int kolona) {
        Object vrednost = model.getValueAt(red, kolona);
        return vrednost == null ? "" : vrednost.toString();
    }

    private int sledeciIdIzTabele(DefaultTableModel model) {
        int najveciId = 0;
        for (int i = 0; i < model.getRowCount(); i++) {
            Object vrednost = model.getValueAt(i, 0);
            if (vrednost == null || vrednost.toString().isBlank()) {
                continue;
            }

            try {
                int id = Integer.parseInt(vrednost.toString().trim());
                if (id > najveciId) {
                    najveciId = id;
                }
            } catch (NumberFormatException ignored) {
            }
        }

        return najveciId + 1;
    }

    private void obrisiIzabraniRed(JTable tabela, DefaultTableModel model) {
        int red = tabela.getSelectedRow();
        if (red == -1) {
            JOptionPane.showMessageDialog(this, "Izaberite red u tabeli.");
            return;
        }

        int izbor = GraphicsEnvironment.isHeadless()
                ? JOptionPane.YES_OPTION
                : JOptionPane.showConfirmDialog(this, "Da li zelite da obrisete izabrani red?",
                "Brisanje", JOptionPane.YES_NO_OPTION);
        if (izbor == JOptionPane.YES_OPTION) {
            model.removeRow(tabela.convertRowIndexToModel(red));
        }
    }

    private void sacuvajCsvModel(CsvEntitet entitet, DefaultTableModel model) {
        ArrayList<String> linije = new ArrayList<>();
        linije.add(zaglavljeCsv(model));
        for (int i = 0; i < model.getRowCount(); i++) {
            linije.add(redCsv(model, i));
        }

        try {
            Files.write(entitet.putanja, linije, StandardCharsets.UTF_8);
            if (!GraphicsEnvironment.isHeadless()) {
                JOptionPane.showMessageDialog(this, "Sacuvane su izmene za: " + entitet.naziv);
            }
            osvezi.run();
        } catch (IOException e) {
            if (!GraphicsEnvironment.isHeadless()) {
                JOptionPane.showMessageDialog(this, "Ne mogu da sacuvam fajl: " + entitet.putanja);
            }
        }
    }

    private String zaglavljeCsv(DefaultTableModel model) {
        ArrayList<String> kolone = new ArrayList<>();
        for (int i = 0; i < model.getColumnCount(); i++) {
            kolone.add(model.getColumnName(i));
        }
        return String.join(",", kolone);
    }

    private String redCsv(DefaultTableModel model, int red) {
        ArrayList<String> vrednosti = new ArrayList<>();
        for (int i = 0; i < model.getColumnCount(); i++) {
            Object vrednost = model.getValueAt(red, i);
            vrednosti.add(vrednost == null ? "" : vrednost.toString().trim());
        }
        return String.join(",", vrednosti);
    }

    private static class CsvEntitet {
        private final String naziv;
        private final Path putanja;

        private CsvEntitet(String naziv, String putanja) {
            this.naziv = naziv;
            this.putanja = Path.of(putanja);
        }
    }

    private JPanel cenovnikPanel() {
        JPanel panel = UiKomponente.kartica(new BorderLayout(0, 14));
        panel.add(UiKomponente.naslovSekcije("Cenovnik"), BorderLayout.NORTH);

        JPanel sadrzaj = new JPanel();
        sadrzaj.setLayout(new BoxLayout(sadrzaj, BoxLayout.Y_AXIS));
        sadrzaj.setBackground(UiKomponente.PANEL);

        for (Cenovnik cenovnik : cenovnikMenadzer.ucitajCenovnike()) {
            sadrzaj.add(cenovnikBlok(cenovnik));
        }

        JButton novi = UiKomponente.primarnoDugme("Novi cenovnik");
        novi.addActionListener(e -> prikaziFormuNovogCenovnika());

        JPanel dugmad = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        dugmad.setBackground(UiKomponente.PANEL);
        dugmad.add(novi);

        panel.add(new JScrollPane(sadrzaj), BorderLayout.CENTER);
        panel.add(dugmad, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel podesavanjaPanel() {
        JPanel panel = UiKomponente.kartica(new BorderLayout(0, 14));
        panel.add(UiKomponente.naslovSekcije("Podesavanja"), BorderLayout.NORTH);

        JPanel forma = new JPanel(new GridBagLayout());
        forma.setBackground(UiKomponente.PANEL);
        JTextField trajanjeNajma = new JTextField(
                String.valueOf(cenovnikMenadzer.ucitajPodrazumevanoTrajanjeNajma()));
        UiKomponente.dodajPolje(forma, 0, "Podrazumevano trajanje najma", trajanjeNajma);

        JButton sacuvaj = UiKomponente.primarnoDugme("Sacuvaj podesavanja");
        sacuvaj.addActionListener(e -> {
            try {
                int brojDana = Integer.parseInt(trajanjeNajma.getText().trim());
                boolean uspesno = cenovnikMenadzer.promeniPodrazumevanoTrajanjeNajma(administrator, brojDana);
                JOptionPane.showMessageDialog(this, uspesno
                        ? "Podrazumevano trajanje najma je promenjeno."
                        : "Trajanje najma mora biti pozitivan ceo broj.");
                if (uspesno) osvezi.run();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Trajanje najma mora biti ceo broj.");
            }
        });

        JPanel dugmad = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        dugmad.setBackground(UiKomponente.PANEL);
        dugmad.add(sacuvaj);

        panel.add(forma, BorderLayout.CENTER);
        panel.add(dugmad, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel cenovnikBlok(Cenovnik cenovnik) {
        JPanel blok = new JPanel(new BorderLayout(0, 6));
        blok.setBackground(UiKomponente.PANEL);

        JPanel zaglavlje = new JPanel(new BorderLayout());
        zaglavlje.setBackground(UiKomponente.PANEL);
        JLabel naslov = new JLabel("Vazi od " + cenovnik.getDatumOd() + " do " + cenovnik.getDatumDo());
        naslov.setFont(naslov.getFont().deriveFont(Font.BOLD));
        JButton obrisi = new JButton("Obrisi cenovnik");
        obrisi.addActionListener(e -> obrisiCenovnik(cenovnik));
        zaglavlje.add(naslov, BorderLayout.WEST);
        zaglavlje.add(obrisi, BorderLayout.EAST);
        blok.add(zaglavlje, BorderLayout.NORTH);

        DefaultTableModel model = UiKomponente.modelTabele(
                new String[]{"Tip", "Vozilo", "Klijent/Usluga", "Vrednost"});
        for (StavkaCenovnika stavka : cenovnik.getStavke()) {
            model.addRow(new Object[]{opisTipaCene(stavka), opisVozila(stavka),
                    opisKlijentaIliUsluge(stavka), opisVrednosti(stavka)});
        }

        JTable tabela = UiKomponente.tabela(model);
        JPanel tabelaPanel = new JPanel(new BorderLayout());
        tabelaPanel.setBackground(UiKomponente.PANEL);
        tabelaPanel.add(tabela.getTableHeader(), BorderLayout.NORTH);
        tabelaPanel.add(tabela, BorderLayout.CENTER);
        blok.add(tabelaPanel, BorderLayout.CENTER);

        return blok;
    }

    private void obrisiCenovnik(Cenovnik cenovnik) {
        int izbor = JOptionPane.showConfirmDialog(this,
                "Da li zelite da obrisete cenovnik koji vazi od " + cenovnik.getDatumOd()
                        + " do " + cenovnik.getDatumDo() + "?",
                "Brisanje cenovnika", JOptionPane.YES_NO_OPTION);
        if (izbor != JOptionPane.YES_OPTION) {
            return;
        }

        boolean uspesno = cenovnikMenadzer.obrisiCenovnik(administrator, cenovnik.getId());
        JOptionPane.showMessageDialog(this, uspesno
                ? "Cenovnik je obrisan."
                : "Cenovnik nije obrisan. Mora ostati bar jedan cenovnik u sistemu.");
        if (uspesno) {
            osvezi.run();
        }
    }

    private String opisTipaCene(StavkaCenovnika stavka) {
        if (stavka.getTipCene() == TipCene.GODISNJA_PRETPLATA) {
            return "Godisnja pretplata";
        } else if (stavka.getTipCene() == TipCene.NAJAM_PO_DANU) {
            return "Najam po danu";
        } else if (stavka.getTipCene() == TipCene.POPUST_KLIJENTA) {
            return "Popust";
        } else if (stavka.getTipCene() == TipCene.DODATNA_USLUGA) {
            return "Dodatna usluga";
        } else if (stavka.getTipCene() == TipCene.KAZNA_KASNJENJA) {
            return "Kazna kasnjenja";
        }

        return String.valueOf(stavka.getTipCene());
    }

    private String opisVozila(StavkaCenovnika stavka) {
        return stavka.getKategorijaVozila() == null ? "" : String.valueOf(stavka.getKategorijaVozila());
    }

    private String opisKlijentaIliUsluge(StavkaCenovnika stavka) {
        if (stavka.getKategorijaKlijenta() != null) {
            return String.valueOf(stavka.getKategorijaKlijenta());
        }

        if (stavka.getDodatnaUsluga() != null) {
            return stavka.getDodatnaUsluga().getNaziv();
        }

        return "";
    }

    private String opisVrednosti(StavkaCenovnika stavka) {
        if (stavka.getTipCene() == TipCene.POPUST_KLIJENTA) {
            return String.format("%.2f%%", stavka.getVrednost());
        }

        return String.format("%.2f", stavka.getVrednost());
    }

    private void prikaziFormuNovogCenovnika() {
        JPanel forma = new JPanel(new GridBagLayout());
        forma.setBackground(UiKomponente.PANEL);

        JTextField datumOd = new JTextField(LocalDate.now().toString());
        JTextField datumDo = new JTextField(LocalDate.now().plusYears(1).minusDays(1).toString());
        JTextField pretplata = new JTextField(String.valueOf(vrednostAktuelneStavke(TipCene.GODISNJA_PRETPLATA,
                null, null, null)));
        JTextField kazna = new JTextField(String.valueOf(vrednostAktuelneStavke(TipCene.KAZNA_KASNJENJA,
                null, null, null)));
        HashMap<KategorijaVozila, JTextField> najamPolja = new HashMap<>();
        HashMap<KategorijaKlijenta, JTextField> popustPolja = new HashMap<>();
        HashMap<DodatnaUsluga, JTextField> uslugaPolja = new HashMap<>();

        int red = 0;
        red = UiKomponente.dodajPolje(forma, red, "Datum od", datumOd);
        red = UiKomponente.dodajPolje(forma, red, "Datum do", datumDo);
        red = UiKomponente.dodajPolje(forma, red, "Godisnja pretplata", pretplata);

        for (KategorijaVozila kategorija : KategorijaVozila.values()) {
            JTextField polje = new JTextField(String.valueOf(vrednostAktuelneStavke(TipCene.NAJAM_PO_DANU,
                    kategorija, null, null)));
            najamPolja.put(kategorija, polje);
            red = UiKomponente.dodajPolje(forma, red, "Najam " + kategorija, polje);
        }

        for (KategorijaKlijenta kategorija : KategorijaKlijenta.values()) {
            JTextField polje = new JTextField(String.valueOf(vrednostAktuelneStavke(TipCene.POPUST_KLIJENTA,
                    null, kategorija, null)));
            popustPolja.put(kategorija, polje);
            red = UiKomponente.dodajPolje(forma, red, "Popust " + kategorija, polje);
        }

        for (DodatnaUsluga usluga : dodatnaUslugaRepozitorijum.ucitajSve()) {
            JTextField polje = new JTextField(String.valueOf(vrednostAktuelneStavke(TipCene.DODATNA_USLUGA,
                    null, null, usluga)));
            uslugaPolja.put(usluga, polje);
            red = UiKomponente.dodajPolje(forma, red, "Usluga " + usluga.getNaziv(), polje);
        }

        UiKomponente.dodajPolje(forma, red, "Kazna kasnjenja", kazna);

        int izbor = JOptionPane.showConfirmDialog(this, new JScrollPane(forma), "Novi cenovnik",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (izbor != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            boolean uspesno = cenovnikMenadzer.dodajNoviCenovnik(administrator,
                    LocalDate.parse(datumOd.getText().trim()),
                    LocalDate.parse(datumDo.getText().trim()),
                    parsirajIznos(pretplata),
                    parsirajCeneNajma(najamPolja),
                    parsirajPopuste(popustPolja),
                    parsirajCeneUsluga(uslugaPolja),
                    parsirajIznos(kazna));

            JOptionPane.showMessageDialog(this, uspesno
                    ? "Cenovnik je dodat. Prethodni vazeci cenovnik je zatvoren dan pre novog."
                    : "Cenovnik nije dodat. Novi cenovnik mora poceti posle pocetka poslednjeg cenovnika.");
            if (uspesno) osvezi.run();
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Datumi moraju biti u formatu GGGG-MM-DD.");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Sve cene i popusti moraju biti brojevi.");
        }
    }

    private double vrednostAktuelneStavke(TipCene tipCene, KategorijaVozila kategorijaVozila,
                                          KategorijaKlijenta kategorijaKlijenta, DodatnaUsluga dodatnaUsluga) {
        ArrayList<Cenovnik> cenovnici = cenovnikMenadzer.ucitajCenovnike();
        if (cenovnici.isEmpty()) {
            return 0;
        }

        Cenovnik poslednji = cenovnici.get(cenovnici.size() - 1);
        for (StavkaCenovnika stavka : poslednji.getStavke()) {
            if (stavka.getTipCene() == tipCene
                    && stavka.getKategorijaVozila() == kategorijaVozila
                    && stavka.getKategorijaKlijenta() == kategorijaKlijenta
                    && istaDodatnaUsluga(stavka.getDodatnaUsluga(), dodatnaUsluga)) {
                return stavka.getVrednost();
            }
        }

        return 0;
    }

    private boolean istaDodatnaUsluga(DodatnaUsluga prva, DodatnaUsluga druga) {
        if (prva == null || druga == null) {
            return prva == druga;
        }

        return prva.getId() == druga.getId();
    }

    private HashMap<KategorijaVozila, Double> parsirajCeneNajma(HashMap<KategorijaVozila, JTextField> polja) {
        HashMap<KategorijaVozila, Double> rezultat = new HashMap<>();
        for (KategorijaVozila kategorija : polja.keySet()) {
            rezultat.put(kategorija, parsirajIznos(polja.get(kategorija)));
        }
        return rezultat;
    }

    private HashMap<KategorijaKlijenta, Double> parsirajPopuste(HashMap<KategorijaKlijenta, JTextField> polja) {
        HashMap<KategorijaKlijenta, Double> rezultat = new HashMap<>();
        for (KategorijaKlijenta kategorija : polja.keySet()) {
            rezultat.put(kategorija, parsirajIznos(polja.get(kategorija)));
        }
        return rezultat;
    }

    private HashMap<DodatnaUsluga, Double> parsirajCeneUsluga(HashMap<DodatnaUsluga, JTextField> polja) {
        HashMap<DodatnaUsluga, Double> rezultat = new HashMap<>();
        for (DodatnaUsluga usluga : polja.keySet()) {
            rezultat.put(usluga, parsirajIznos(polja.get(usluga)));
        }
        return rezultat;
    }

    private double parsirajIznos(JTextField polje) {
        return Double.parseDouble(polje.getText().trim().replace(',', '.'));
    }
}
