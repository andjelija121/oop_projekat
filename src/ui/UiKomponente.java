package ui;

import model.Korisnik;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.LayoutManager;

final class UiKomponente {
    static final Color POZADINA = new Color(230, 240, 250);
    static final Color PANEL = Color.WHITE;
    static final Color PRIMARNA = new Color(24, 79, 122);
    static final Color IVICA = new Color(220, 225, 232);
    static final Font NASLOV_FONT = new Font("Arial", Font.BOLD, 26);

    private UiKomponente() {
    }

    static JPanel okvirAplikacije(Component sredina, String naslov, Korisnik korisnik, Runnable odjava) {
        JPanel root = new JPanel(new BorderLayout(0, 10));
        root.setBackground(POZADINA);
        root.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(POZADINA);
        JLabel title = new JLabel(naslov);
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setForeground(PRIMARNA);

        JPanel desno = new JPanel(new BorderLayout(12, 0));
        desno.setBackground(POZADINA);
        desno.add(new JLabel("Ulogovan: " + korisnik.getIme() + " " + korisnik.getPrezime()),
                BorderLayout.CENTER);
        JButton odjavaButton = new JButton("Odjava");
        odjavaButton.addActionListener(e -> odjava.run());
        desno.add(odjavaButton, BorderLayout.EAST);

        header.add(title, BorderLayout.WEST);
        header.add(desno, BorderLayout.EAST);
        root.add(header, BorderLayout.NORTH);
        root.add(sredina, BorderLayout.CENTER);
        return root;
    }

    static JPanel kartica(LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.setBackground(PANEL);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        return panel;
    }

    static JLabel naslovSekcije(String tekst) {
        JLabel label = new JLabel(tekst);
        label.setFont(new Font("Arial", Font.BOLD, 16));
        label.setForeground(PRIMARNA);
        return label;
    }

    static JButton primarnoDugme(String tekst) {
        JButton button = new JButton(tekst);
        button.setBackground(PRIMARNA);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(button.getFont().deriveFont(Font.BOLD));
        return button;
    }

    static JTabbedPane tabovi() {
        return new JTabbedPane();
    }

    static DefaultTableModel modelTabele(String[] kolone) {
        return new DefaultTableModel(kolone, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    static JTable tabela(DefaultTableModel model) {
        JTable tabela = new JTable(model);
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.setFillsViewportHeight(true);
        tabela.getTableHeader().setReorderingAllowed(false);
        return tabela;
    }

    static int dodajPolje(JPanel panel, int red, String labela, Component komponenta) {
        GridBagConstraints labelGbc = new GridBagConstraints();
        labelGbc.gridx = 0;
        labelGbc.gridy = red;
        labelGbc.insets = new Insets(5, 5, 5, 10);
        labelGbc.anchor = GridBagConstraints.EAST;
        panel.add(new JLabel(labela), labelGbc);

        GridBagConstraints fieldGbc = new GridBagConstraints();
        fieldGbc.gridx = 1;
        fieldGbc.gridy = red;
        fieldGbc.insets = new Insets(5, 5, 5, 5);
        fieldGbc.fill = GridBagConstraints.HORIZONTAL;
        fieldGbc.weightx = 1;
        komponenta.setPreferredSize(new Dimension(220, 25));
        panel.add(komponenta, fieldGbc);
        return red + 1;
    }
}
