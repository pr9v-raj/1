import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class EcoGridAI extends JFrame {

    private final Color BG_NIGHT = new Color(15, 23, 42);
    private final Color CARD_SLATE = new Color(30, 41, 59);
    private final Color BORDER_SLATE = new Color(71, 85, 105);
    private final Color NEON_GREEN = new Color(34, 197, 94);
    private final Color NEON_CYAN = new Color(56, 189, 248);
    private final Color WARN_RED = new Color(239, 68, 68);

    private final String[] deviceNames = {
            "BLDC Smart Ceiling Fan (IoT Eco)",
            "Legacy Induction Ceiling Fan",
            "LED Smart Batten Array (20W)",
            "1.5T Dual-Inverter Split AC",
            "Gaming / Coding Workstation PC",
            "2000W Rapid Water Heater Geyser"
    };
    private final int[] watts = {28, 75, 20, 1350, 190, 2000};
    private final int[] initHours = {14, 10, 8, 7, 6, 2};

    private final JCheckBox[] powerSwitches = new JCheckBox[deviceNames.length];
    private final JSlider[] usageSliders = new JSlider[deviceNames.length];
    private final JLabel[] deviceStatLabels = new JLabel[deviceNames.length];

    private JLabel kpiWattLoad, kpiMonthlyUnits, kpiMonthlyBill, aiAdvisorLabel;
    private JProgressBar gridLoadBar;

    public EcoGridAI() {
        setTitle("EcoGrid AI - Smart Home IoT Wattage & Tariff Automation Console");
        setSize(1000, 640);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout(14, 14));
        root.setBackground(BG_NIGHT);
        root.setBorder(new EmptyBorder(16, 16, 16, 16));
        setContentPane(root);

        root.add(buildTopKpiBar(), BorderLayout.NORTH);
        root.add(buildApplianceMatrix(), BorderLayout.CENTER);
        root.add(buildAiAutomationFooter(), BorderLayout.SOUTH);

        recomputeGridTelemetry();
    }

    private JPanel buildTopKpiBar() {
        JPanel top = new JPanel(new GridLayout(1, 3, 14, 0));
        top.setOpaque(false);

        kpiWattLoad = new JLabel("0 W");
        kpiMonthlyUnits = new JLabel("0.0 kWh");
        kpiMonthlyBill = new JLabel("Rs 0.00");

        top.add(makeStatCard("INSTANT ACTIVE GRID LOAD", kpiWattLoad, NEON_CYAN));
        top.add(makeStatCard("30-DAY PROJECTED ENERGY", kpiMonthlyUnits, NEON_GREEN));
        top.add(makeStatCard("TIERED SLAB MONTHLY BILL", kpiMonthlyBill, new Color(250, 204, 21)));
        return top;
    }

    private JPanel buildApplianceMatrix() {
        GlassPanel matrix = new GlassPanel(18, CARD_SLATE, BORDER_SLATE);
        matrix.setLayout(new GridLayout(deviceNames.length, 1, 8, 8));
        matrix.setBorder(new EmptyBorder(14, 18, 14, 18));

        for (int i = 0; i < deviceNames.length; i++) {
            GlassPanel row = new GlassPanel(12, BG_NIGHT, new Color(51, 65, 85));
            row.setLayout(new GridLayout(1, 3, 12, 0));
            row.setBorder(new EmptyBorder(8, 14, 8, 14));

            powerSwitches[i] = new JCheckBox(deviceNames[i] + " [" + watts[i] + "W]", true);
            powerSwitches[i].setOpaque(false);
            powerSwitches[i].setForeground(Color.WHITE);
            powerSwitches[i].setFont(new Font("SansSerif", Font.BOLD, 12));
            powerSwitches[i].setFocusPainted(false);

            usageSliders[i] = new JSlider(0, 24, initHours[i]);
            usageSliders[i].setOpaque(false);
            usageSliders[i].setMajorTickSpacing(6);
            usageSliders[i].setPaintTicks(true);

            deviceStatLabels[i] = new JLabel("0 hrs/day | Rs 0/mo", SwingConstants.RIGHT);
            deviceStatLabels[i].setFont(new Font("Monospaced", Font.BOLD, 12));
            deviceStatLabels[i].setForeground(NEON_GREEN);

            powerSwitches[i].addActionListener(e -> recomputeGridTelemetry());
            usageSliders[i].addChangeListener(e -> recomputeGridTelemetry());

            row.add(powerSwitches[i]);
            row.add(usageSliders[i]);
            row.add(deviceStatLabels[i]);
            matrix.add(row);
        }
        return matrix;
    }

    private JPanel buildAiAutomationFooter() {
        GlassPanel footer = new GlassPanel(18, CARD_SLATE, NEON_CYAN);
        footer.setLayout(new BorderLayout(12, 10));
        footer.setBorder(new EmptyBorder(14, 18, 14, 18));

        gridLoadBar = new JProgressBar(0, 4000);
        gridLoadBar.setStringPainted(true);
        gridLoadBar.setPreferredSize(new Dimension(800, 22));
        gridLoadBar.setBackground(BG_NIGHT);

        aiAdvisorLabel = new JLabel("AI ENERGY ADVISOR: Analyzing appliance draw...");
        aiAdvisorLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        aiAdvisorLabel.setForeground(Color.WHITE);

        PillButton aiOptimizeBtn = new PillButton("Engage AI Smart Eco-Saver Mode", NEON_GREEN);
        aiOptimizeBtn.addActionListener(e -> engageAiSmartMode());

        footer.add(gridLoadBar, BorderLayout.NORTH);
        footer.add(aiAdvisorLabel, BorderLayout.CENTER);
        footer.add(aiOptimizeBtn, BorderLayout.EAST);
        return footer;
    }

    private void recomputeGridTelemetry() {
        int activeWatts = 0;
        double dailyKwh = 0;

        for (int i = 0; i < deviceNames.length; i++) {
            int hrs = usageSliders[i].getValue();
            if (powerSwitches[i].isSelected() && hrs > 0) {
                activeWatts += watts[i];
                double kwhDay = (watts[i] * hrs) / 1000.0;
                double kwhMonth = kwhDay * 30.0;
                dailyKwh += kwhDay;
                deviceStatLabels[i].setText(String.format("%02d hrs/d | %.1f u/mo (Rs %.0f)", hrs, kwhMonth, kwhMonth * 6.0));
                deviceStatLabels[i].setForeground(NEON_GREEN);
            } else {
                deviceStatLabels[i].setText("STANDBY (0.0 kWh)");
                deviceStatLabels[i].setForeground( new Color(100, 116, 139));
            }
        }

        double monthlyUnits = dailyKwh * 30.0;
        double bill = calculateSlabBill(monthlyUnits);

        kpiWattLoad.setText(activeWatts + " Watts");
        kpiMonthlyUnits.setText(String.format("%.1f Units", monthlyUnits));
        kpiMonthlyBill.setText(String.format("Rs %.2f", bill));

        gridLoadBar.setValue(activeWatts);
        gridLoadBar.setForeground(activeWatts > 3000 ? WARN_RED : NEON_CYAN);
        gridLoadBar.setString("Real-Time Smart Meter Load: " + activeWatts + "W / 4000W Sanctioned Limit");

        if (powerSwitches[1].isSelected() || monthlyUnits > 250) {
            aiAdvisorLabel.setText("AI ALERT: High tariff slab (>200 units) & 75W Induction Fan detected! Click 'Engage AI Smart Eco-Saver' to cut bill by ~32%.");
        } else {
            aiAdvisorLabel.setText("AI STATUS: Home running on BLDC & Eco-Slab optimum. Carbon & tariff footprint minimized!");
        }
    }

    private void engageAiSmartMode() {
        double beforeBill = calculateSlabBill(
                Double.parseDouble(kpiMonthlyUnits.getText().replace(" Units", ""))
        );

        // AI shuts off old 75W Induction Fan, prioritizes 28W BLDC Fan, caps AC to 5h Eco sleep cycle & Geyser to 1h
        powerSwitches[0].setSelected(true);
        usageSliders[0].setValue(14);
        powerSwitches[1].setSelected(false); // Turn off 75W fan
        if (usageSliders[3].getValue() > 5) usageSliders[3].setValue(5); // Optimize AC
        if (usageSliders[5].getValue() > 1) usageSliders[5].setValue(1); // Optimize Geyser

        recomputeGridTelemetry();

        double afterBill = calculateSlabBill(
                Double.parseDouble(kpiMonthlyUnits.getText().replace(" Units", ""))
        );
        double saved = Math.max(0, beforeBill - afterBill);

        JOptionPane.showMessageDialog(this,
                String.format("EcoGrid AI Automation Complete!\n\n" +
                        "1. Deactivated 75W Induction Fan -> Routed airflow to 28W BLDC Fan.\n" +
                        "2. Scheduled Inverter AC & Water Heater to off-peak thermal cycles.\n" +
                        "3. Estimated Monthly Savings: Rs %.2f / month (Rs %.0f / year)!", saved, saved * 12),
                "AI Smart Load Optimization", JOptionPane.INFORMATION_MESSAGE);
    }

    private double calculateSlabBill(double u) {
        if (u <= 0) return 0;
        double bill = 80.0; // Fixed grid charge
        if (u <= 100) bill += u * 4.50;
        else if (u <= 200) bill += (100 * 4.50) + ((u - 100) * 6.00);
        else bill += (100 * 4.50) + (100 * 6.00) + ((u - 200) * 7.80);
        return bill;
    }

    private GlassPanel makeStatCard(String label, JLabel val, Color accent) {
        GlassPanel c = new GlassPanel(16, CARD_SLATE, accent);
        c.setLayout(new GridLayout(2, 1, 4, 4));
        c.setBorder(new EmptyBorder(12, 16, 12, 16));

        JLabel l = new JLabel(label);
        l.setFont(new Font("SansSerif", Font.BOLD, 11));
        l.setForeground(new Color(148, 163, 184));

        val.setFont(new Font("Monospaced", Font.BOLD, 24));
        val.setForeground(accent);

        c.add(l);
        c.add(val);
        return c;
    }

    static class GlassPanel extends JPanel {
        private final int r;
        private final Color bg, border;
        public GlassPanel(int r, Color bg, Color border) {
            this.r = r; this.bg = bg; this.border = border;
            setOpaque(false);
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, r, r);
            g2.setColor(border);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, r, r);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    static class PillButton extends JButton {
        private final Color c;
        private boolean hover = false;
        public PillButton(String txt, Color c) {
            super(txt);
            this.c = c;
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setForeground(new Color(11, 15, 25));
            setFont(new Font("SansSerif", Font.BOLD, 12));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                public void mouseExited(MouseEvent e) { hover = false; repaint(); }
            });
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(hover ? c.brighter() : c);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new EcoGridAI().setVisible(true));
    }
}
