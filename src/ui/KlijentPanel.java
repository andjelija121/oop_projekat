package ui;

import enums.TipNaplate;
import menadzment.CenovnikMenadzer;
import menadzment.RezervacijaMenadzer;
import model.DodatnaUsluga;
import model.Klijent;
import model.ModelVozila;
import model.Rezervacija;
import repozitorijum.DodatnaUslugaRepozitorijum;

import javax.swing.JButton;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;

public class KlijentPanel extends JPanel {
    private final Klijent klijent;
    private final RezervacijaMenadzer rezervacijaMenadzer;
    private final CenovnikMenadzer cenovnikMenadzer;
    private final DodatnaUslugaRepozitorijum dodatnaUslugaRepozitorijum;
    private final Runnable osvezi;

    public KlijentPanel(Klijent klijent, RezervacijaMenadzer rezervacijaMenadzer,
                        CenovnikMenadzer cenovnikMenadzer,
                        DodatnaUslugaRepozitorijum dodatnaUslugaRepozitorijum,
                        Runnable osvezi, Runnable odjava) {
        super(new BorderLayout());
        this.klijent = klijent;
        this.rezervacijaMenadzer = rezervacijaMenadzer;
        this.cenovnikMenadzer = cenovnikMenadzer;
        this.dodatnaUslugaRepozitorijum = dodatnaUslugaRepozitorijum;
        this.osvezi = osvezi;

        JTabbedPane tabs = UiKomponente.tabovi();
        tabs.addTab("Moji podaci", podaciPanel());
        tabs.addTab("Nova rezervacija", novaRezervacijaPanel());
        tabs.addTab("Moje rezervacije", rezervacijePanel());
        add(UiKomponente.okvirAplikacije(tabs, "Klijent", klijent, odjava));
    }

    private JPanel podaciPanel() {
        JPanel panel = UiKomponente.kartica(new GridBagLayout());
        dodajRed(panel, 0, "Ime", klijent.getIme());
        dodajRed(panel, 1, "Prezime", klijent.getPrezime());
        dodajRed(panel, 2, "Korisnicko ime", klijent.getKorisnickoIme());
        dodajRed(panel, 3, "Datum dozvole", klijent.getDatumDozvole());
        dodajRed(panel, 4, "Kategorija", String.valueOf(klijent.getPosebnaKategorija()));
        return panel;
    }

    private JPanel novaRezervacijaPanel() {
        JPanel panel = UiKomponente.kartica(new BorderLayout(0, 14));
        panel.add(UiKomponente.naslovSekcije("Nova rezervacija"), BorderLayout.NORTH);
        JPanel forma = new JPanel(new GridBagLayout());
        forma.setBackground(UiKomponente.PANEL);
        JComboBox<ModelVozila> modelBox = new JComboBox<>();
        for (ModelVozila model : rezervacijaMenadzer.ucitajModeleVozila()) modelBox.addItem(model);
        JTextField datumOd = new JTextField(LocalDate.now().plusDays(1).toString());
        JTextField datumDo = new JTextField(LocalDate.now().plusDays(3).toString());
        datumDo.setEditable(false);
        JTextField dodatniDani = new JTextField("0");
        JPanel uslugePanel = new JPanel();
        uslugePanel.setLayout(new BoxLayout(uslugePanel, BoxLayout.Y_AXIS));
        uslugePanel.setBackground(UiKomponente.PANEL);
        ArrayList<JCheckBox> uslugaCheckBoxovi = new ArrayList<>();
        for (DodatnaUsluga dodatnaUsluga : dodatnaUslugaRepozitorijum.ucitajSve()) {
            if (dodatnaUsluga.getTipNaplate() == TipNaplate.PO_DANU) {
                continue;
            }

            JCheckBox checkBox = new JCheckBox(dodatnaUsluga.getNaziv());
            checkBox.setBackground(UiKomponente.PANEL);
            checkBox.putClientProperty("dodatnaUsluga", dodatnaUsluga);
            uslugaCheckBoxovi.add(checkBox);
            uslugePanel.add(checkBox);
        }
        JLabel dostupnost = new JLabel(" ");
        JLabel cenaLabela = new JLabel(" ");
        JButton proveri = new JButton("Proveri dostupnost");
        JButton izracunajCenu = UiKomponente.primarnoDugme("Izracunaj cenu");
        JButton rezervisi = UiKomponente.primarnoDugme("Napravi zahtev");

        int red = 0;
        red = UiKomponente.dodajPolje(forma, red, "Model vozila", modelBox);
        red = UiKomponente.dodajPolje(forma, red, "Datum od", datumOd);
        red = UiKomponente.dodajPolje(forma, red, "Datum do", datumDo);
        red = UiKomponente.dodajPolje(forma, red, "Dodatne usluge", uslugePanel);
        uslugePanel.setPreferredSize(new Dimension(220, 75));
        uslugePanel.setMinimumSize(new Dimension(220, 75));
        red = UiKomponente.dodajPolje(forma, red, "Dodatni dani", dodatniDani);
        red = UiKomponente.dodajPolje(forma, red, "Dostupnost", dostupnost);
        UiKomponente.dodajPolje(forma, red, "Cena", cenaLabela);

        proveri.addActionListener(e -> {
            try {
                LocalDate izabraniDatumOd = LocalDate.parse(datumOd.getText());
                int brojDodatnihDana = Integer.parseInt(dodatniDani.getText());
                if (brojDodatnihDana < 0) {
                    JOptionPane.showMessageDialog(this, "Dodatni dani ne mogu biti negativni.");
                    return;
                }

                LocalDate izabraniDatumDo = izracunajDatumDo(izabraniDatumOd, brojDodatnihDana);
                datumDo.setText(izabraniDatumDo.toString());
                boolean dostupno = rezervacijaMenadzer.daLiJeModelDostupan(
                        (ModelVozila) modelBox.getSelectedItem(), izabraniDatumOd, izabraniDatumDo);
                dostupnost.setText(dostupno ? "Dostupno" : "Nije dostupno");
                dostupnost.setForeground(dostupno ? new Color(35, 120, 70) : new Color(180, 45, 45));
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(this, "Datumi moraju biti u formatu GGGG-MM-DD.");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Dodatni dani moraju biti broj.");
            }
        });

        izracunajCenu.addActionListener(e -> {
            try {
                LocalDate izabraniDatumOd = LocalDate.parse(datumOd.getText());
                int brojDodatnihDana = Integer.parseInt(dodatniDani.getText());
                if (brojDodatnihDana < 0) {
                    JOptionPane.showMessageDialog(this, "Dodatni dani ne mogu biti negativni.");
                    return;
                }

                LocalDate izabraniDatumDo = izracunajDatumDo(izabraniDatumOd, brojDodatnihDana);
                datumDo.setText(izabraniDatumDo.toString());

                double cena = izracunajCenu((ModelVozila) modelBox.getSelectedItem(),
                        izabraniDatumOd, izabraniDatumDo,
                        izabraneDodatneUsluge(uslugaCheckBoxovi), brojDodatnihDana);
                cenaLabela.setText(String.format("%.2f RSD", cena));
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(this, "Datumi moraju biti u formatu GGGG-MM-DD.");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Dodatni dani moraju biti broj.");
            }
        });

        rezervisi.addActionListener(e -> {
            try {
                LocalDate izabraniDatumOd = LocalDate.parse(datumOd.getText());
                int brojDodatnihDana = Integer.parseInt(dodatniDani.getText());
                if (brojDodatnihDana < 0) {
                    JOptionPane.showMessageDialog(this, "Dodatni dani ne mogu biti negativni.");
                    return;
                }

                LocalDate izabraniDatumDo = izracunajDatumDo(izabraniDatumOd, brojDodatnihDana);
                datumDo.setText(izabraniDatumDo.toString());
                boolean uspesno = rezervacijaMenadzer.napraviZahtevZaRezervaciju(klijent,
                        (ModelVozila) modelBox.getSelectedItem(), izabraniDatumOd, izabraniDatumDo);
                JOptionPane.showMessageDialog(this, uspesno ? "Zahtev za rezervaciju je napravljen."
                        : "Zahtev nije napravljen. Proverite datume, dostupnost i vozacku dozvolu.");
                if (uspesno) osvezi.run();
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(this, "Datumi moraju biti u formatu GGGG-MM-DD.");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Dodatni dani moraju biti broj.");
            }
        });

        JPanel dugmad = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        dugmad.setBackground(UiKomponente.PANEL);
        dugmad.add(proveri);
        dugmad.add(izracunajCenu);
        dugmad.add(rezervisi);
        panel.add(forma, BorderLayout.CENTER);
        panel.add(dugmad, BorderLayout.SOUTH);
        return panel;
    }

    private ArrayList<DodatnaUsluga> izabraneDodatneUsluge(ArrayList<JCheckBox> checkBoxovi) {
        ArrayList<DodatnaUsluga> izabrane = new ArrayList<>();

        for (JCheckBox checkBox : checkBoxovi) {
            if (checkBox.isSelected()) {
                izabrane.add((DodatnaUsluga) checkBox.getClientProperty("dodatnaUsluga"));
            }
        }

        return izabrane;
    }

    private double izracunajCenu(ModelVozila modelVozila, LocalDate datumOd, LocalDate datumDo,
                                ArrayList<DodatnaUsluga> dodatneUsluge, int brojDodatnihDana) {
        return cenovnikMenadzer.izracunajCenuRezervacije(klijent, modelVozila, datumOd, datumDo,
                dodatneUsluge, brojDodatnihDana);
    }

    private LocalDate izracunajDatumDo(LocalDate datumOd, int brojDodatnihDana) {
        int osnovniBrojDana = 3;
        return datumOd.plusDays(osnovniBrojDana + brojDodatnihDana - 1);
    }

    private JPanel rezervacijePanel() {
        JPanel panel = UiKomponente.kartica(new BorderLayout(0, 12));
        panel.add(UiKomponente.naslovSekcije("Moje rezervacije"), BorderLayout.NORTH);
        DefaultTableModel model = UiKomponente.modelTabele(
                new String[]{"ID", "Model", "Datum od", "Datum do", "Status", "Ukupno"});
        for (Rezervacija rezervacija : rezervacijaMenadzer.ucitajRezervacijeKlijenta(klijent)) {
            model.addRow(new Object[]{rezervacija.getId(), rezervacija.getModelVozila(),
                    rezervacija.getDatumOd(), rezervacija.getDatumDo(), rezervacija.getStatus(),
                    rezervacija.getCenaUkupno()});
        }
        JTable tabela = UiKomponente.tabela(model);
        JButton otkazi = new JButton("Otkazi izabranu rezervaciju");
        otkazi.addActionListener(e -> {
            int red = tabela.getSelectedRow();
            if (red == -1) {
                JOptionPane.showMessageDialog(this, "Izaberite rezervaciju u tabeli.");
                return;
            }
            boolean uspesno = rezervacijaMenadzer.otkaziRezervaciju(klijent, (int) model.getValueAt(red, 0));
            JOptionPane.showMessageDialog(this, uspesno ? "Rezervacija je otkazana."
                    : "Izabrana rezervacija ne moze da se otkaze.");
            if (uspesno) osvezi.run();
        });
        JPanel dugmad = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        dugmad.setBackground(UiKomponente.PANEL);
        dugmad.add(otkazi);
        panel.add(new JScrollPane(tabela), BorderLayout.CENTER);
        panel.add(dugmad, BorderLayout.SOUTH);
        return panel;
    }

    private void dodajRed(JPanel panel, int red, String labela, String vrednost) {
        GridBagConstraints labelGbc = new GridBagConstraints();
        labelGbc.gridx = 0;
        labelGbc.gridy = red;
        labelGbc.insets = new Insets(9, 9, 9, 16);
        labelGbc.anchor = GridBagConstraints.EAST;
        panel.add(new JLabel(labela), labelGbc);
        GridBagConstraints valueGbc = new GridBagConstraints();
        valueGbc.gridx = 1;
        valueGbc.gridy = red;
        valueGbc.insets = new Insets(9, 9, 9, 9);
        valueGbc.anchor = GridBagConstraints.WEST;
        JLabel value = new JLabel(vrednost);
        value.setFont(value.getFont().deriveFont(Font.BOLD));
        panel.add(value, valueGbc);
    }
}
