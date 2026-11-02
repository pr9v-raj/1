import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class SmartHomeBillSimulator extends JFrame {

    private final String[] names = {
            "BLDC Ceiling Fan (Energy Saver)",
            "Old Induction Ceiling Fan",
            "LED Tube Light (20W)",
            "1.5 Ton Split Inverter AC",
            "Desktop PC + Monitor Setup",
            "Electric Geyser / Water Heater"
    };
    private final int[] watts = {28, 75, 20, 1400, 180, 2000};
    private final int[] defaultHours = {12, 10, 8, 6, 5, 1};

    private JCheckBox[] switches = new JCheckBox[names.length];
    private JSlider[] hourSliders = new JSlider[names.length];
    private JLabel[] rowCostLabels = new JLabel[names.length];

    private JProgressBar loadMeter;
    private JLabel dailyKwhLabel, monthlyKwhLabel, monthlyBillLabel, savingTipLabel;

    public SmartHomeBillSimulator() {
        setTitle("Smart Home Wattage & Tariff Simulator - Class 12 IT Project");
        setSize(900, 580);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(12, 12));
        mainPanel.setBorder(new EmptyBorder(12, 12, 12, 12));
        mainPanel.setBackground(new Color(248, 250, 252));
        setContentPane(mainPanel);

        // Header
        JLabel header = new JLabel("SMART HOME APPLIANCE LOAD & ELECTRICITY TARIFF SIMULATOR", SwingConstants.CENTER);
        header.setFont(new Font("SansSerif", Font.BOLD, 19));
        header.setOpaque(true);
        header.setBackground(new Color(30, 64, 175));
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(800, 48));
        mainPanel.add(header, BorderLayout.NORTH);

        // Center Appliance Switchboard
        JPanel boardPanel = new JPanel(new GridLayout(names.length, 1, 8, 8));
        boardPanel.setBackground(Color.WHITE);
        boardPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(30, 64, 175), 2),
                "Interactive Room Switchboard (Toggle ON/OFF & Drag Daily Usage Sliders)",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("SansSerif", Font.BOLD, 13), new Color(30, 64, 175)
        ));

        for (int i = 0; i < names.length; i++) {
            JPanel row = new JPanel(new GridLayout(1, 3, 10, 5));
            row.setBackground(new Color(241, 245, 249));
            row.setBorder(new EmptyBorder(6, 12, 6, 12));

            switches[i] = new JCheckBox(names[i] + " [" + watts[i] + "W]");
            switches[i].setFont(new Font("SansSerif", Font.BOLD, 12));
            switches[i].setBackground(new Color(241, 245, 249));
            // Turn on BLDC fan, LED light, and PC by default
            if (i == 0 || i == 2 || i == 4) switches[i].setSelected(true);

            hourSliders[i] = new JSlider(0, 24, defaultHours[i]);
            hourSliders[i].setMajorTickSpacing(6);
            hourSliders[i].setMinorTickSpacing(1);
            hourSliders[i].setPaintTicks(true);
            hourSliders[i].setPaintLabels(true);
            hourSliders[i].setBackground(new Color(241, 245, 249));

            rowCostLabels[i] = new JLabel("0 hrs/day | 0.00 kWh", SwingConstants.RIGHT);
            rowCostLabels[i].setFont(new Font("Monospaced", Font.BOLD, 12));

            switches[i].addActionListener(e -> recalculateEnergy());
            hourSliders[i].addChangeListener(e -> recalculateEnergy());

            row.add(switches[i]);
            row.add(hourSliders[i]);
            row.add(rowCostLabels[i]);
            boardPanel.add(row);
        }

        mainPanel.add(boardPanel, BorderLayout.CENTER);

        // Bottom Live Digital Smart Meter
        JPanel meterPanel = new JPanel(new GridLayout(3, 2, 10, 8));
        meterPanel.setBackground(Color.WHITE);
        meterPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(22, 163, 74), 2),
                "Live Smart Energy Meter (Tiered Domestic Tariff Slabs)",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("SansSerif", Font.BOLD, 13), new Color(22, 163, 74)
        ));

        loadMeter = new JProgressBar(0, 4000);
        loadMeter.setStringPainted(true);

        dailyKwhLabel = new JLabel("Daily Energy: 0.00 kWh (Units)", SwingConstants.CENTER);
        dailyKwhLabel.setFont(new Font("SansSerif", Font.BOLD, 14));

        monthlyKwhLabel = new JLabel("30-Day Consumption: 0.0 Units", SwingConstants.CENTER);
        monthlyKwhLabel.setFont(new Font("SansSerif", Font.BOLD, 14));

        monthlyBillLabel = new JLabel("Est. Monthly Bill: Rs 0.00", SwingConstants.CENTER);
        monthlyBillLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        monthlyBillLabel.setForeground(new Color(185, 28, 28));

        savingTipLabel = new JLabel("Tip: Using a 28W BLDC Fan instead of a 75W Fan saves ~Rs 105/month per fan!", SwingConstants.CENTER);
        savingTipLabel.setForeground(new Color(21, 128, 61));
        savingTipLabel.setFont(new Font("SansSerif", Font.ITALIC, 12));

        meterPanel.add(loadMeter);
        meterPanel.add(dailyKwhLabel);
        meterPanel.add(monthlyKwhLabel);
        meterPanel.add(monthlyBillLabel);
        meterPanel.add(savingTipLabel);

        mainPanel.add(meterPanel, BorderLayout.SOUTH);

        recalculateEnergy();
    }

    private void recalculateEnergy() {
        int activeWattLoad = 0;
        double dailyKwh = 0;

        for (int i = 0; i < names.length; i++) {
            int hrs = hourSliders[i].getValue();
            if (switches[i].isSelected()) {
                activeWattLoad += watts[i];
                double itemKwh = (watts[i] * hrs) / 1000.0;
                dailyKwh += itemKwh;
                rowCostLabels[i].setText(String.format("%d hrs/day | %.2f kWh/day", hrs, itemKwh));
                rowCostLabels[i].setForeground(new Color(22, 101, 52));
            } else {
                rowCostLabels[i].setText("OFF (0.00 kWh/day)");
                rowCostLabels[i].setForeground(Color.GRAY);
            }
        }

        double monthlyUnits = dailyKwh * 30.0;
        double billAmount = calculateTieredBill(monthlyUnits);

        loadMeter.setValue(activeWattLoad);
        loadMeter.setString("Instant Active Load: " + activeWattLoad + " Watts / 4000W Max");
        loadMeter.setForeground(activeWattLoad > 3000 ? new Color(220, 38, 38) : new Color(22, 163, 74));

        dailyKwhLabel.setText(String.format("Daily Energy: %.2f kWh (Units)", dailyKwh));
        monthlyKwhLabel.setText(String.format("30-Day Consumption: %.1f Units", monthlyUnits));
        monthlyBillLabel.setText(String.format("Est. Monthly Bill: Rs %.2f", billAmount));
    }

    // Realistic Domestic Slab Calculation:
    // First 100 units @ Rs 4.50, 101-200 @ Rs 6.00, Above 200 @ Rs 7.50 + Rs 75 fixed meter charge
    private double calculateTieredBill(double units) {
        if (units <= 0) return 0.0;
        double cost = 75.0; // Fixed monthly meter charge
        if (units <= 100) {
            cost += units * 4.50;
        } else if (units <= 200) {
            cost += (100 * 4.50) + ((units - 100) * 6.00);
        } else {
            cost += (100 * 4.50) + (100 * 6.00) + ((units - 200) * 7.50);
        }
        return cost;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SmartHomeBillSimulator().setVisible(true));
    }
}
