import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class CricketScoreboardApp extends JFrame {

    private int runs = 0;
    private int wickets = 0;
    private int legalBalls = 0;
    private int maxOvers = 20;
    private int targetScore = 0;
    private int currentOverRuns = 0;

    private JLabel scoreDisplay, oversDisplay, crrDisplay, targetStatusLabel;
    private JTextField maxOversField, targetField;
    private DefaultTableModel overLogModel;

    public CricketScoreboardApp() {
        setTitle("Live Cricket Digital Scoreboard & Run-Rate Predictor - Class 12 IT");
        setSize(880, 560);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(12, 12));
        mainPanel.setBorder(new EmptyBorder(12, 12, 12, 12));
        mainPanel.setBackground(new Color(240, 253, 244));
        setContentPane(mainPanel);

        // Stadium Scoreboard Banner
        JPanel topBoard = new JPanel(new GridLayout(2, 3, 10, 5));
        topBoard.setBackground(new Color(6, 78, 59));
        topBoard.setBorder(new EmptyBorder(12, 18, 12, 18));

        scoreDisplay = new JLabel("SCORE: 0 / 0", SwingConstants.LEFT);
        scoreDisplay.setFont(new Font("Monospaced", Font.BOLD, 30));
        scoreDisplay.setForeground(new Color(250, 204, 21));

        oversDisplay = new JLabel("OVERS: 0.0 / 20", SwingConstants.CENTER);
        oversDisplay.setFont(new Font("Monospaced", Font.BOLD, 24));
        oversDisplay.setForeground(Color.WHITE);

        crrDisplay = new JLabel("CRR: 0.00", SwingConstants.RIGHT);
        crrDisplay.setFont(new Font("Monospaced", Font.BOLD, 24));
        crrDisplay.setForeground(new Color(167, 243, 208));

        targetStatusLabel = new JLabel("1st Innings Mode (Set Target below for Chase Mode)", SwingConstants.LEFT);
        targetStatusLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        targetStatusLabel.setForeground(Color.WHITE);

        topBoard.add(scoreDisplay);
        topBoard.add(oversDisplay);
        topBoard.add(crrDisplay);
        topBoard.add(targetStatusLabel);
        topBoard.add(new JLabel(""));
        topBoard.add(new JLabel(""));

        mainPanel.add(topBoard, BorderLayout.NORTH);

        // Left Panel: Match Settings & Ball-by-Ball Control Deck
        mainPanel.add(createControlDeck(), BorderLayout.WEST);

        // Center Panel: Ball-by-Ball & Over History Table
        mainPanel.add(createHistoryPanel(), BorderLayout.CENTER);
    }

    private JPanel createControlDeck() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setPreferredSize(new Dimension(380, 420));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(6, 78, 59), 2),
                "Match Settings & Ball Control Deck",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("SansSerif", Font.BOLD, 13), new Color(6, 78, 59)
        ));

        // Settings Row
        JPanel settingsPanel = new JPanel(new GridLayout(3, 2, 6, 6));
        settingsPanel.setBackground(Color.WHITE);
        settingsPanel.setBorder(new EmptyBorder(8, 10, 8, 10));

        settingsPanel.add(new JLabel("Total Match Overs:"));
        maxOversField = new JTextField("20");
        settingsPanel.add(maxOversField);

        settingsPanel.add(new JLabel("Target to Chase (0 = 1st Inn):"));
        targetField = new JTextField("0");
        settingsPanel.add(targetField);

        JButton applyBtn = new JButton("Apply Settings");
        JButton resetMatchBtn = new JButton("New Match Reset");
        styleBtn(applyBtn, new Color(37, 99, 235));
        styleBtn(resetMatchBtn, new Color(107, 114, 128));

        applyBtn.addActionListener(e -> applySettings());
        resetMatchBtn.addActionListener(e -> resetMatch());

        settingsPanel.add(applyBtn);
        settingsPanel.add(resetMatchBtn);

        // Ball Buttons Grid
        JPanel btnGrid = new JPanel(new GridLayout(3, 3, 8, 8));
        btnGrid.setBackground(Color.WHITE);
        btnGrid.setBorder(new EmptyBorder(10, 10, 10, 10));

        JButton b0 = createScoreBtn("0 (Dot)", 0, true, false, new Color(71, 85, 105));
        JButton b1 = createScoreBtn("+1 Run", 1, true, false, new Color(22, 163, 74));
        JButton b2 = createScoreBtn("+2 Runs", 2, true, false, new Color(22, 163, 74));
        JButton b3 = createScoreBtn("+3 Runs", 3, true, false, new Color(22, 163, 74));
        JButton b4 = createScoreBtn("FOUR (+4)", 4, true, false, new Color(37, 99, 235));
        JButton b6 = createScoreBtn("SIX (+6)", 6, true, false, new Color(147, 51, 234));
        JButton bWide = createScoreBtn("Wide (+1)", 1, false, false, new Color(217, 119, 6));
        JButton bNb = createScoreBtn("No Ball (+1)", 1, false, false, new Color(217, 119, 6));
        JButton bWicket = createScoreBtn("WICKET!", 0, true, true, new Color(220, 38, 38));

        btnGrid.add(b0);
        btnGrid.add(b1);
        btnGrid.add(b2);
        btnGrid.add(b3);
        btnGrid.add(b4);
        btnGrid.add(b6);
        btnGrid.add(bWide);
        btnGrid.add(bNb);
        btnGrid.add(bWicket);

        panel.add(settingsPanel, BorderLayout.NORTH);
        panel.add(btnGrid, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(6, 78, 59), 2),
                "Ball-by-Ball Commentary & Over Log",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("SansSerif", Font.BOLD, 13), new Color(6, 78, 59)
        ));

        String[] cols = {"Over.Ball", "Delivery Event", "Total Score", "CRR"};
        overLogModel = new DefaultTableModel(cols, 0);
        JTable table = new JTable(overLogModel);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private JButton createScoreBtn(String label, int runAdded, boolean isLegal, boolean isWicket, Color bg) {
        JButton btn = new JButton(label);
        styleBtn(btn, bg);
        btn.addActionListener(e -> processDelivery(label, runAdded, isLegal, isWicket));
        return btn;
    }

    private void processDelivery(String eventName, int runAdded, boolean isLegal, boolean isWicket) {
        if (wickets >= 10 || legalBalls >= maxOvers * 6) {
            JOptionPane.showMessageDialog(this, "Innings is already complete! Click 'New Match Reset' to start again.");
            return;
        }

        runs += runAdded;
        currentOverRuns += runAdded;
        if (isWicket) wickets++;
        if (isLegal) legalBalls++;

        int completedOvers = legalBalls / 6;
        int ballsInOver = legalBalls % 6;
        String overStr = completedOvers + "." + ballsInOver;

        double oversDecimal = legalBalls / 6.0;
        double crr = oversDecimal == 0 ? 0.0 : runs / oversDecimal;

        scoreDisplay.setText("SCORE: " + runs + " / " + wickets);
        oversDisplay.setText("OVERS: " + overStr + " / " + maxOvers);
        crrDisplay.setText(String.format("CRR: %.2f", crr));

        overLogModel.insertRow(0, new Object[]{
                overStr,
                eventName,
                runs + "/" + wickets,
                String.format("%.2f", crr)
        });

        if (isLegal && ballsInOver == 0) {
            overLogModel.insertRow(0, new Object[]{
                    "END OVER " + completedOvers,
                    "Runs in Over: " + currentOverRuns,
                    runs + "/" + wickets,
                    String.format("%.2f", crr)
            });
            currentOverRuns = 0;
        }

        updateChaseStatus();
    }

    private void updateChaseStatus() {
        if (targetScore <= 0) {
            double projected = (legalBalls == 0) ? 0 : (runs / (legalBalls / 6.0)) * maxOvers;
            targetStatusLabel.setText(String.format("1st Innings | Projected Total at current rate: %.0f runs", projected));
            return;
        }

        int runsNeeded = targetScore - runs;
        int ballsRemaining = (maxOvers * 6) - legalBalls;

        if (runsNeeded <= 0) {
            targetStatusLabel.setText("MATCH WON BY " + (10 - wickets) + " WICKETS!");
            JOptionPane.showMessageDialog(this, "Target Chased Successfully! Batting team wins by " + (10 - wickets) + " wickets!");
        } else if (wickets >= 10 || ballsRemaining <= 0) {
            targetStatusLabel.setText("INNINGS OVER! Bowling team wins by " + (runsNeeded - 1) + " runs.");
        } else {
            double rrr = (runsNeeded * 6.0) / ballsRemaining;
            targetStatusLabel.setText(String.format("Need %d runs in %d balls | Required Run Rate (RRR): %.2f",
                    runsNeeded, ballsRemaining, rrr));
        }
    }

    private void applySettings() {
        try {
            maxOvers = Integer.parseInt(maxOversField.getText().trim());
            targetScore = Integer.parseInt(targetField.getText().trim());
            if (maxOvers <= 0) maxOvers = 20;
            oversDisplay.setText("OVERS: " + (legalBalls / 6) + "." + (legalBalls % 6) + " / " + maxOvers);
            updateChaseStatus();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Enter valid integers for Overs and Target!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void resetMatch() {
        runs = 0;
        wickets = 0;
        legalBalls = 0;
        currentOverRuns = 0;
        overLogModel.setRowCount(0);
        applySettings();
        scoreDisplay.setText("SCORE: 0 / 0");
        crrDisplay.setText("CRR: 0.00");
    }

    private void styleBtn(JButton btn, Color bg) {
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CricketScoreboardApp().setVisible(true));
    }
}
