package ui;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;

class LineChartPanel extends JPanel {
    private final String title;
    private final ArrayList<String> labels;
    private final ArrayList<String> series;
    private final double[][] values;
    private final Color[] colors = {
            new Color(91, 155, 213),
            new Color(237, 125, 49),
            new Color(165, 165, 165),
            new Color(68, 114, 196),
            new Color(112, 173, 71)
    };

    LineChartPanel(String title, ArrayList<String> labels, ArrayList<String> series, double[][] values) {
        this.title = title;
        this.labels = labels;
        this.series = series;
        this.values = values;
        setBackground(UiKomponente.PANEL);
        setPreferredSize(new Dimension(920, 430));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int left = 76;
        int right = 42;
        int top = 64;
        int bottom = 110;
        int chartWidth = getWidth() - left - right;
        int chartHeight = getHeight() - top - bottom;
        double max = zaokruziMaksimum();
        FontMetrics fm = g2.getFontMetrics();

        g2.setColor(new Color(35, 35, 35));
        g2.setFont(g2.getFont().deriveFont(16f));
        g2.drawString(title, (getWidth() - g2.getFontMetrics().stringWidth(title)) / 2, 28);
        g2.setFont(g2.getFont().deriveFont(12f));

        g2.setColor(new Color(228, 228, 228));
        for (int i = 0; i <= 4; i++) {
            int y = top + chartHeight - (chartHeight * i / 4);
            g2.drawLine(left, y, left + chartWidth, y);
            g2.setColor(new Color(75, 75, 75));
            String oznaka = String.format("%.0f", max * i / 4);
            g2.drawString(oznaka, left - fm.stringWidth(oznaka) - 8, y + 4);
            g2.setColor(new Color(228, 228, 228));
        }

        g2.setColor(new Color(185, 185, 185));
        g2.drawLine(left, top + chartHeight, left + chartWidth, top + chartHeight);

        for (int i = 0; i < labels.size(); i++) {
            int x = xZaIndeks(i, left, chartWidth);
            g2.setColor(new Color(75, 75, 75));
            String label = labels.get(i);
            g2.drawString(label, x - fm.stringWidth(label) / 2, top + chartHeight + 20);
        }

        for (int s = 0; s < series.size(); s++) {
            g2.setColor(colors[s % colors.length]);
            g2.setStroke(new BasicStroke(2f));

            for (int i = 0; i < labels.size() - 1; i++) {
                g2.drawLine(
                        xZaIndeks(i, left, chartWidth),
                        yZaVrednost(values[s][i], max, top, chartHeight),
                        xZaIndeks(i + 1, left, chartWidth),
                        yZaVrednost(values[s][i + 1], max, top, chartHeight)
                );
            }

            for (int i = 0; i < labels.size(); i++) {
                int x = xZaIndeks(i, left, chartWidth);
                int y = yZaVrednost(values[s][i], max, top, chartHeight);
                g2.fillOval(x - 4, y - 4, 8, 8);
            }
        }

        nacrtajLegendu(g2, top + chartHeight + 48);
    }

    private int xZaIndeks(int indeks, int left, int chartWidth) {
        if (labels.size() <= 1) {
            return left + chartWidth / 2;
        }

        return left + indeks * chartWidth / (labels.size() - 1);
    }

    private int yZaVrednost(double value, double max, int top, int chartHeight) {
        return top + chartHeight - (int) Math.round(chartHeight * value / max);
    }

    private double zaokruziMaksimum() {
        double max = 1;
        for (int s = 0; s < series.size(); s++) {
            for (int i = 0; i < labels.size(); i++) {
                if (values[s][i] > max) {
                    max = values[s][i];
                }
            }
        }

        double korak = Math.pow(10, Math.max(0, String.format("%.0f", max).length() - 2));
        return Math.ceil(max / korak) * korak;
    }

    private void nacrtajLegendu(Graphics2D g2, int y) {
        FontMetrics fm = g2.getFontMetrics();
        int ukupnaSirina = 0;
        for (String naziv : series) {
            ukupnaSirina += fm.stringWidth(naziv) + 56;
        }

        int x = Math.max(10, (getWidth() - ukupnaSirina) / 2);
        for (int i = 0; i < series.size(); i++) {
            g2.setColor(colors[i % colors.length]);
            g2.drawLine(x, y - 4, x + 22, y - 4);
            g2.fillOval(x + 9, y - 8, 8, 8);
            g2.setColor(new Color(35, 35, 35));
            g2.drawString(series.get(i), x + 30, y);
            x += fm.stringWidth(series.get(i)) + 56;
        }
    }
}
