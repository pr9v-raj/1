import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class BusFuelManagerApp extends JFrame {

    private JTextField busNoField, routeField, distanceField, fuelPriceField;
    private JComboBox<String> terrainBox, loadBox;
    private JCheckBox acCheck;
    private DefaultTableModel tableModel;
    private JLabel summaryLabel, effectiveMileageLabel;
    private JProgressBar efficiencyBar;

    private double totalFleetCost = 0;
    private double totalFleetFuel = 0;
    private double totalFleetKm = 0;

    public BusFuelManagerApp() {
        setTitle("Smart Bus Fleet Fuel & Cost Estimator (~90% Real-World Model) - Class 12 IT");
        setSize(940, 580);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(12, 12));
        mainPanel.setBorder(new EmptyBorder(12, 12, 12, 12));
        mainPanel.setBackground(new Color(241, 245, 249));
        setContentPane(mainPanel);

        // Header
        JLabel header = new JLabel("BUS FLEET KM-BASED FUEL & TRIP COST ANALYZER", SwingConstants.CENTER);
        header.setFont(new Font("SansSerif", Font.BOLD, 20));
        header.setOpaque(true);
        header.setBackground(new Color(15, 23, 42));
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(800, 48));
        mainPanel.add(header, BorderLayout.NORTH);

        // Left Control Panel
        mainPanel.add(createInputPanel(), BorderLayout.WEST);

        // Center Table & Analytics Panel
        mainPanel.add(createTablePanel(), BorderLayout.CENTER);
    }

    private JPanel createInputPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setPreferredSize(new Dimension(340, 450));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(30, 58, 138), 2),
                "Trip Parameters (Base Mileage: 4.2 km/L)",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("SansSerif", Font.BOLD, 13), new Color(30, 58, 138)
        ));

        JPanel form = new JPanel(new GridLayout(7, 2, 8, 10));
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(12, 12, 12, 12));

        form.add(new JLabel("Bus Reg. Number:"));
        busNoField = new JTextField("JH-01-AB-1024");
        form.add(busNoField);

        form.add(new JLabel("Route Name:"));
        routeField = new JTextField("City Depot - Highway");
        form.add(routeField);

        form.add(new JLabel("Trip Distance (km):"));
        distanceField = new JTextField("120");
        form.add(distanceField);

        form.add(new JLabel("Diesel Price (Rs/L):"));
        fuelPriceField = new JTextField("92.50");
        form.add(fuelPriceField);

        form.add(new JLabel("Route Terrain:"));
        terrainBox = new JComboBox<>(new String[]{
                "Highway (100% Eff.)",
                "City Traffic (-15% Eff.)",
                "Hilly / Ghat (-25% Eff.)"
        });
        form.add(terrainBox);

        form.add(new JLabel("Passenger Load:"));
        loadBox = new JComboBox<>(new String[]{
                "Half Load (-5% Eff.)",
                "Full Seated (-12% Eff.)",
                "Overloaded (-20% Eff.)"
        });
        form.add(loadBox);

        form.add(new JLabel("HVAC / AC System:"));
        acCheck = new JCheckBox("AC Running (-12% Eff.)");
        acCheck.setBackground(Color.WHITE);
        form.add(acCheck);

        // Live Efficiency Meter + Action Buttons
        JPanel bottomBox = new JPanel(new GridLayout(4, 1, 6, 6));
        bottomBox.setBackground(Color.WHITE);
        bottomBox.setBorder(new EmptyBorder(5, 12, 12, 12));

        effectiveMileageLabel = new JLabel("Est. Real Mileage: -- km/L", SwingConstants.CENTER);
        effectiveMileageLabel.setFont(new Font("SansSerif", Font.BOLD, 13));

        efficiencyBar = new JProgressBar(0, 50); // 0.0 to 5.0 km/L scaled x10
        efficiencyBar.setValue(42);
        efficiencyBar.setStringPainted(true);
        efficiencyBar.setForeground(new Color(22, 163, 74));
        efficiencyBar.setString("Base Heavy Bus: 4.2 km/L");

        JButton calcBtn = new JButton("Calculate & Log Trip (~90% Est.)");
        calcBtn.setBackground(new Color(37, 99, 235));
        calcBtn.setForeground(Color.WHITE);
        calcBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        calcBtn.setFocusPainted(false);

        JButton clearBtn = new JButton("Delete Selected Trip");
        clearBtn.setBackground(new Color(100, 116, 139));
        clearBtn.setForeground(Color.WHITE);
        clearBtn.setFocusPainted(false);

        calcBtn.addActionListener(e -> calculateAndLogTrip());

        bottomBox.add(effectiveMileageLabel);
        bottomBox.add(efficiencyBar);
        bottomBox.add(calcBtn);
        bottomBox.add(clearBtn);

        // Store reference to clearBtn action in table setup
        panel.putClientProperty("deleteBtn", clearBtn);

        panel.add(form, BorderLayout.CENTER);
        panel.add(bottomBox, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(30, 58, 138), 2),
                "Active Fleet Trip Log (Includes +10% Maintenance/Oil Buffer)",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("SansSerif", Font.BOLD, 13), new Color(30, 58, 138)
        ));

        String[] cols = {"Bus No", "Route", "Km", "Real km/L", "Fuel (L)", "Fuel Cost (Rs)", "Total Est. Cost (Rs)", "Rs/Km"};
        tableModel = new DefaultTableModel(cols, 0);
        JTable table = new JTable(tableModel);
        JScrollPane scroll = new JScrollPane(table);

        summaryLabel = new JLabel("Total Distance: 0.0 km | Total Fuel: 0.0 L | Total Fleet Expense: Rs 0.00");
        summaryLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        summaryLabel.setBorder(new EmptyBorder(8, 10, 8, 10));

        // Connect delete button from left panel
        JPanel westPanel = (JPanel) ((JPanel) getContentPane()).getComponent(1);
        JButton deleteBtn = (JButton) westPanel.getClientProperty("deleteBtn");
        deleteBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1) {
                double km = Double.parseDouble(tableModel.getValueAt(row, 2).toString());
                double fuel = Double.parseDouble(tableModel.getValueAt(row, 4).toString());
                double cost = Double.parseDouble(tableModel.getValueAt(row, 6).toString());
                totalFleetKm -= km;
                totalFleetFuel -= fuel;
                totalFleetCost -= cost;
                tableModel.removeRow(row);
                updateFleetSummary();
            } else {
                JOptionPane.showMessageDialog(this, "Select a trip row in the table to remove.", "Selection Needed", JOptionPane.WARNING_MESSAGE);
            }
        });

        panel.add(scroll, BorderLayout.CENTER);
        panel.add(summaryLabel, BorderLayout.SOUTH);
        return panel;
    }

    private void calculateAndLogTrip() {
        try {
            String busNo = busNoField.getText().trim();
            String route = routeField.getText().trim();
            double distance = Double.parseDouble(distanceField.getText().trim());
            double dieselPrice = Double.parseDouble(fuelPriceField.getText().trim());

            if (busNo.isEmpty() || route.isEmpty() || distance <= 0 || dieselPrice <= 0) {
                JOptionPane.showMessageDialog(this, "Please enter valid positive numbers and route details.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Base standard heavy diesel bus mileage in km/L
            double baseMileage = 4.2;

            // Terrain Multiplier
            double terrainFactor = 1.0;
            int tIdx = terrainBox.getSelectedIndex();
            if (tIdx == 1) terrainFactor = 0.85; // City Traffic
            else if (tIdx == 2) terrainFactor = 0.75; // Hilly / Ghat

            // Load Multiplier
            double loadFactor = 0.95; // Half Load
            int lIdx = loadBox.getSelectedIndex();
            if (lIdx == 1) loadFactor = 0.88; // Full Seated
            else if (lIdx == 2) loadFactor = 0.80; // Overloaded

            // AC Compressor Penalty
            double acFactor = acCheck.isSelected() ? 0.88 : 1.0;

            // 90% Realistic Effective Mileage Calculation
            double realMileage = baseMileage * terrainFactor * loadFactor * acFactor;
            double fuelConsumed = distance / realMileage;
            double pureFuelCost = fuelConsumed * dieselPrice;

            // Add 10% buffer for engine oil, AdBlue, tires & idling wear
            double totalEstimatedCost = pureFuelCost * 1.10;
            double costPerKm = totalEstimatedCost / distance;

            // Update Progress Bar & Label
            effectiveMileageLabel.setText(String.format("Est. Real Mileage: %.2f km/L", realMileage));
            efficiencyBar.setValue((int) (realMileage * 10));
            efficiencyBar.setString(String.format("%.2f km/L (Cost: Rs %.1f/km)", realMileage, costPerKm));

            tableModel.addRow(new Object[]{
                    busNo,
                    route,
                    String.format("%.1f", distance),
                    String.format("%.2f", realMileage),
                    String.format("%.2f", fuelConsumed),
                    String.format("%.2f", pureFuelCost),
                    String.format("%.2f", totalEstimatedCost),
                    String.format("Rs %.2f", costPerKm)
            });

            totalFleetKm += distance;
            totalFleetFuel += fuelConsumed;
            totalFleetCost += totalEstimatedCost;
            updateFleetSummary();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Distance and Diesel Price must be valid numbers!", "Format Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateFleetSummary() {
        summaryLabel.setText(String.format("Total Distance: %.1f km | Est. Diesel Used: %.2f L | Total Operational Cost (~90%% Acc.): Rs %.2f",
                totalFleetKm, totalFleetFuel, totalFleetCost));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new BusFuelManagerApp().setVisible(true));
    }
}
