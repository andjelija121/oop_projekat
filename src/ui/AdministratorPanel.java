package ui;

import enums.KategorijaKlijenta;
import enums.KategorijaVozila;
import enums.NivoSpreme;
import enums.Pol;
import enums.TipCene;
import menadzment.CenovnikMenadzer;
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
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;

public class AdministratorPanel extends JPanel {
    private final Korisnik administrator;
    private final KorisnikRepozitorijum korisnici;
    private final ZaposleniMenadzer zaposleniMenadzer;
    private final CenovnikMenadzer cenovnikMenadzer;
    private final DodatnaUslugaRepozitorijum dodatnaUslugaRepozitorijum;
    private final Runnable osvezi;

    public AdministratorPanel(Korisnik administrator, KorisnikRepozitorijum korisnici,
                              ZaposleniMenadzer zaposleniMenadzer,
                              CenovnikMenadzer cenovnikMenadzer,
                              DodatnaUslugaRepozitorijum dodatnaUslugaRepozitorijum,
                              Runnable osvezi, Runnable odjava) {
        super(new BorderLayout());
        this.administrator = administrator;
        this.korisnici = korisnici;
        this.zaposleniMenadzer = zaposleniMenadzer;
        this.cenovnikMenadzer = cenovnikMenadzer;
        this.dodatnaUslugaRepozitorijum = dodatnaUslugaRepozitorijum;
        this.osvezi = osvezi;

        JTabbedPane tabs = UiKomponente.tabovi();
        tabs.addTab("Zaposleni", zaposleniTabelaPanel());
        tabs.addTab("Dodaj zaposlenog", dodajZaposlenogPanel());
        tabs.addTab("Cenovnik", cenovnikPanel());
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

    private JPanel cenovnikBlok(Cenovnik cenovnik) {
        JPanel blok = new JPanel(new BorderLayout(0, 6));
        blok.setBackground(UiKomponente.PANEL);

        JLabel naslov = new JLabel("Vazi od " + cenovnik.getDatumOd() + " do " + cenovnik.getDatumDo());
        naslov.setFont(naslov.getFont().deriveFont(Font.BOLD));
        blok.add(naslov, BorderLayout.NORTH);

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
