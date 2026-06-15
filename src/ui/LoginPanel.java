package ui;

import menadzment.PrijavaMenadzer;
import model.Korisnik;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.function.Consumer;

public class LoginPanel extends JPanel {
    public LoginPanel(PrijavaMenadzer prijavaMenadzer, Consumer<Korisnik> uspesnaPrijava) {
        super(new GridBagLayout());
        setBackground(UiKomponente.POZADINA);

        JPanel kartica = new JPanel(new GridBagLayout());
        kartica.setBackground(UiKomponente.PANEL);
        kartica.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UiKomponente.IVICA), new EmptyBorder(35, 45, 35, 45)));

        JLabel naslov = new JLabel("Rent a Car");
        naslov.setFont(UiKomponente.NASLOV_FONT);
        naslov.setForeground(UiKomponente.PRIMARNA);
        naslov.setHorizontalAlignment(SwingConstants.CENTER);
        JLabel podnaslov = new JLabel("Prijava na sistem");
        podnaslov.setHorizontalAlignment(SwingConstants.CENTER);
        JTextField korisnickoImeField = new JTextField(22);
        JPasswordField lozinkaField = new JPasswordField(22);
        JButton loginButton = UiKomponente.primarnoDugme("Login");

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        kartica.add(naslov, gbc);
        gbc.gridy++;
        kartica.add(podnaslov, gbc);
        gbc.gridwidth = 1;
        gbc.gridy++;
        kartica.add(new JLabel("Korisnicko ime"), gbc);
        gbc.gridx = 1;
        kartica.add(korisnickoImeField, gbc);
        gbc.gridx = 0;
        gbc.gridy++;
        kartica.add(new JLabel("Lozinka"), gbc);
        gbc.gridx = 1;
        kartica.add(lozinkaField, gbc);
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        kartica.add(loginButton, gbc);

        loginButton.addActionListener(e -> {
            Korisnik korisnik = prijavaMenadzer.login(
                    korisnickoImeField.getText(), new String(lozinkaField.getPassword()));
            if (korisnik == null) {
                JOptionPane.showMessageDialog(this, "Pogresno korisnicko ime ili lozinka.");
            } else {
                uspesnaPrijava.accept(korisnik);
            }
        });

        add(kartica);
    }
}
