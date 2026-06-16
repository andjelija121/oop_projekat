package ui;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;

class PieChartPanel extends JPanel {
    private final String title;
    private final ArrayList<String> labels;
    private final ArrayList<Double> values;
    private final Color[] colors = {
            new Color(91, 155, 213),
            new Color(237, 125, 49),
            new Color(165, 165, 165),
            new Color(255, 192, 0),
            new Color(112, 173, 71),
            new Color(68, 114, 196)
    };

    PieChartPanel(String title, ArrayList<String> labels, ArrayList<Double> values) {
        this.title = title;
        this.labels = labels;
        this.values = values;
        setBackground(UiKomponente.PANEL);
        setPreferredSize(new Dimension(430, 330));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        FontMetrics fm = g2.getFontMetrics();
        g2.setColor(new Color(35, 35, 35));
        g2.setFont(g2.getFont().deriveFont(14f));
        g2.drawString(title, (getWidth() - g2.getFontMetrics().stringWidth(title)) / 2, 26);
        g2.setFont(g2.getFont().deriveFont(12f));

        int size = Math.min(getWidth() - 150, getHeight() - 120);
        size = Math.max(110, size);
        int x = (getWidth() - size) / 2;
        int y = 48;
        double total = ukupno();

        if (total <= 0) {
            g2.setColor(new Color(220, 220, 220));
            g2.fillOval(x, y, size, size);
        } else {
            int start = 90;
            int nacrtano = 0;
            for (int i = 0; i < values.size(); i++) {
                int angle = i == values.size() - 1
                        ? 360 - nacrtano
                        : (int) Math.round(values.get(i) * 360 / total);
                g2.setColor(colors[i % colors.length]);
                g2.fillArc(x, y, size, size, start, -angle);
                start -= angle;
                nacrtano += angle;
            }
        }

        g2.setColor(UiKomponente.PANEL);
        g2.drawOval(x, y, size, size);
        nacrtajLegendu(g2, fm, y + size + 42);
    }

    private double ukupno() {
        double total = 0;
        for (Double value : values) {
            total += value;
        }
        return total;
    }

    private void nacrtajLegendu(Graphics2D g2, FontMetrics fm, int y) {
        int kolonaSirina = getWidth() / 2;
        for (int i = 0; i < labels.size(); i++) {
            int kolona = i % 2;
            int red = i / 2;
            int x = 22 + kolona * kolonaSirina;
            int labelY = y + red * 18;

            g2.setColor(colors[i % colors.length]);
            g2.fillRect(x, labelY - 10, 14, 10);
            g2.setColor(new Color(35, 35, 35));
            String label = labels.get(i);
            if (fm.stringWidth(label) > kolonaSirina - 44) {
                label = skrati(label, fm, kolonaSirina - 44);
            }
            g2.drawString(label, x + 20, labelY);
        }
    }

    private String skrati(String tekst, FontMetrics fm, int sirina) {
        String rezultat = tekst;
        while (rezultat.length() > 3 && fm.stringWidth(rezultat + "...") > sirina) {
            rezultat = rezultat.substring(0, rezultat.length() - 1);
        }
        return rezultat + "...";
    }
}
