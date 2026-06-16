package ui;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.util.ArrayList;

class StackedBarChartPanel extends JPanel {
    private final ArrayList<String> labels;
    private final ArrayList<String> series;
    private final double[][] values;
    private final Color[] colors = {
            new Color(26, 115, 96),
            new Color(27, 94, 161),
            new Color(184, 91, 32),
            new Color(124, 72, 154)
    };

    StackedBarChartPanel(ArrayList<String> labels, ArrayList<String> series, double[][] values) {
        this.labels = labels;
        this.series = series;
        this.values = values;
        setBackground(UiKomponente.PANEL);
        setPreferredSize(new Dimension(920, 360));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int left = 55;
        int top = 24;
        int bottom = 76;
        int chartHeight = getHeight() - top - bottom;
        int chartWidth = getWidth() - left - 30;
        int barArea = chartWidth / Math.max(1, labels.size());
        int barWidth = Math.max(18, barArea - 16);
        double max = maksimalanZbir();
        FontMetrics fm = g.getFontMetrics();

        g.setColor(new Color(215, 221, 228));
        g.drawLine(left, top + chartHeight, left + chartWidth, top + chartHeight);
        g.drawLine(left, top, left, top + chartHeight);

        for (int i = 0; i < labels.size(); i++) {
            int x = left + i * barArea + (barArea - barWidth) / 2;
            int yBottom = top + chartHeight;

            for (int s = 0; s < series.size(); s++) {
                int segmentHeight = (int) Math.round(chartHeight * values[s][i] / max);
                yBottom -= segmentHeight;
                g.setColor(colors[s % colors.length]);
                g.fillRect(x, yBottom, barWidth, segmentHeight);
            }

            g.setColor(new Color(45, 52, 60));
            String label = labels.get(i);
            g.drawString(label, x + (barWidth - fm.stringWidth(label)) / 2, top + chartHeight + 20);
        }

        int legendX = left;
        int legendY = getHeight() - 32;
        for (int s = 0; s < series.size(); s++) {
            g.setColor(colors[s % colors.length]);
            g.fillRect(legendX, legendY - 10, 12, 12);
            g.setColor(new Color(45, 52, 60));
            g.drawString(series.get(s), legendX + 18, legendY);
            legendX += fm.stringWidth(series.get(s)) + 42;
        }
    }

    private double maksimalanZbir() {
        double max = 1;

        for (int i = 0; i < labels.size(); i++) {
            double zbir = 0;
            for (int s = 0; s < series.size(); s++) {
                zbir += values[s][i];
            }
            if (zbir > max) {
                max = zbir;
            }
        }

        return max;
    }
}
