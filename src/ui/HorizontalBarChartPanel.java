package ui;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.util.ArrayList;

class HorizontalBarChartPanel extends JPanel {
    private final ArrayList<String> labels;
    private final ArrayList<Double> values;
    private final Color color;

    HorizontalBarChartPanel(ArrayList<String> labels, ArrayList<Double> values, Color color) {
        this.labels = labels;
        this.values = values;
        this.color = color;
        setBackground(UiKomponente.PANEL);
        setPreferredSize(new Dimension(520, Math.max(220, labels.size() * 34 + 50)));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int left = 140;
        int top = 24;
        int barHeight = 20;
        int gap = 14;
        int width = getWidth() - left - 70;
        double max = 1;

        for (Double value : values) {
            if (value > max) {
                max = value;
            }
        }

        FontMetrics fm = g.getFontMetrics();
        for (int i = 0; i < labels.size(); i++) {
            int y = top + i * (barHeight + gap);
            double value = values.get(i);
            int barWidth = (int) Math.round(width * value / max);

            g.setColor(new Color(70, 78, 86));
            g.drawString(labels.get(i), 8, y + barHeight - 5);
            g.setColor(new Color(232, 236, 240));
            g.fillRect(left, y, width, barHeight);
            g.setColor(color);
            g.fillRect(left, y, barWidth, barHeight);
            g.setColor(new Color(45, 52, 60));
            String valueText = String.format("%.0f", value);
            g.drawString(valueText, left + barWidth + 8,
                    y + (barHeight + fm.getAscent()) / 2 - 3);
        }
    }
}
