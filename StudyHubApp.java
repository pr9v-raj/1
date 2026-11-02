import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class StudyHubApp extends JFrame {

    // Timer Components (Interactive Pomodoro)
    private JLabel timerLabel;
    private JProgressBar timerProgress;
    private JButton startBtn, pauseBtn, resetBtn;
    private Timer swingTimer;
    private int totalSeconds = 25 * 60; // 25 minutes default
    private int remainingSeconds = totalSeconds;
    private boolean isRunning = false;

    // Mock Test & Backlog Tracker Components
    private JComboBox<String> subjectBox;
    private JTextField topicField, correctField, wrongField;
    private DefaultTableModel tableModel;
    private JLabel statsLabel;

    private int totalTests = 0;
    private int cumulativeScore = 0;

    public StudyHubApp() {
        setTitle("Smart Study & Mock Score Analyzer - Class 12 IT Project");
        setSize(850, 560);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(12, 12));

        // Main container with padding
        JPanel mainPanel = new JPanel(new BorderLayout(12, 12));
        mainPanel.setBorder(new EmptyBorder(12, 12, 12, 12));
        mainPanel.setBackground(new Color(245, 247, 250));
        setContentPane(mainPanel);

        // Header Banner
        JLabel header = new JLabel("STUDENT FOCUS & MOCK SCORE ANALYZER", SwingConstants.CENTER);
        header.setFont(new Font("SansSerif", Font.BOLD, 20));
        header.setOpaque(true);
        header.setBackground(new Color(30, 64, 175));
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(800, 48));
        mainPanel.add(header, BorderLayout.NORTH);

        // Left Panel: Interactive Focus Timer
        JPanel timerPanel = createTimerPanel();
        mainPanel.add(timerPanel, BorderLayout.WEST);

        // Center Panel: Mock Score Calculator & Logger
        JPanel analyzerPanel = createAnalyzerPanel();
        mainPanel.add(analyzerPanel, BorderLayout.CENTER);

        initTimerLogic();
    }

    private JPanel createTimerPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setPreferredSize(new Dimension(270, 400));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(30, 64, 175), 2),
                "Pomodoro Focus Timer",
                TitledBorder.CENTER,
                TitledBorder.TOP,
                new Font("SansSerif", Font.BOLD, 14),
                new Color(30, 64, 175)
        ));

        timerLabel = new JLabel("25:00", SwingConstants.CENTER);
        timerLabel.setFont(new Font("Monospaced", Font.BOLD, 48));
        timerLabel.setForeground(new Color(17, 24, 39));

        timerProgress = new JProgressBar(0, totalSeconds);
        timerProgress.setValue(totalSeconds);
        timerProgress.setStringPainted(true);
        timerProgress.setForeground(new Color(34, 197, 94));
        timerProgress.setString("Ready to Focus");

        JPanel btnPanel = new JPanel(new GridLayout(3, 1, 8, 8));
        btnPanel.setBackground(Color.WHITE);
        btnPanel.setBorder(new EmptyBorder(10, 20, 15, 20));

        startBtn = new JButton("Start 25-Min Session");
        pauseBtn = new JButton("Pause Timer");
        resetBtn = new JButton("Reset Timer");

        styleButton(startBtn, new Color(22, 163, 74));
        styleButton(pauseBtn, new Color(217, 119, 6));
        styleButton(resetBtn, new Color(220, 38, 38));

        btnPanel.add(startBtn);
        btnPanel.add(pauseBtn);
        btnPanel.add(resetBtn);

        JPanel centerBox = new JPanel(new GridLayout(2, 1, 5, 5));
        centerBox.setBackground(Color.WHITE);
        centerBox.setBorder(new EmptyBorder(20, 15, 10, 15));
        centerBox.add(timerLabel);
        centerBox.add(timerProgress);

        panel.add(centerBox, BorderLayout.CENTER);
        panel.add(btnPanel, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createAnalyzerPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(30, 64, 175), 2),
                "Exam Negative Marking Calculator & Topic Tracker (+4 / -1 Scheme)",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("SansSerif", Font.BOLD, 14),
                new Color(30, 64, 175)
        ));

        // Form Panel
        JPanel formPanel = new JPanel(new GridLayout(5, 2, 8, 8));
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(new EmptyBorder(10, 12, 10, 12));

        formPanel.add(new JLabel("Subject:"));
        subjectBox = new JComboBox<>(new String[]{"Physics", "Mathematics", "Chemistry", "Information Tech"});
        formPanel.add(subjectBox);

        formPanel.add(new JLabel("Chapter / Topic Name:"));
        topicField = new JTextField();
        formPanel.add(topicField);

        formPanel.add(new JLabel("Correct Answers (+4 each):"));
        correctField = new JTextField();
        formPanel.add(correctField);

        formPanel.add(new JLabel("Wrong Answers (-1 each):"));
        wrongField = new JTextField();
        formPanel.add(wrongField);

        JButton addRecordBtn = new JButton("Calculate & Log Score");
        JButton deleteBtn = new JButton("Delete Selected Row");
        styleButton(addRecordBtn, new Color(37, 99, 235));
        styleButton(deleteBtn, new Color(107, 114, 128));

        formPanel.add(addRecordBtn);
        formPanel.add(deleteBtn);

        // Table to Display Records
        String[] columns = {"Subject", "Topic", "Correct", "Wrong", "Net Score", "Accuracy %"};
        tableModel = new DefaultTableModel(columns, 0);
        JTable table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);

        // Bottom Summary Bar
        statsLabel = new JLabel("Logged Sessions: 0 | Average Net Score: 0.0");
        statsLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        statsLabel.setBorder(new EmptyBorder(6, 12, 6, 12));

        // Button Actions
        addRecordBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logMockScore();
            }
        });

        deleteBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow != -1) {
                    int rowScore = Integer.parseInt(tableModel.getValueAt(selectedRow, 4).toString());
                    cumulativeScore -= rowScore;
                    totalTests--;
                    tableModel.removeRow(selectedRow);
                    updateSummary();
                } else {
                    JOptionPane.showMessageDialog(StudyHubApp.this,
                            "Please click on a row in the table to delete it.",
                            "No Selection", JOptionPane.WARNING_MESSAGE);
                }
            }
        });

        panel.add(formPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(statsLabel, BorderLayout.SOUTH);
        return panel;
    }

    private void initTimerLogic() {
        swingTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (remainingSeconds > 0) {
                    remainingSeconds--;
                    int mins = remainingSeconds / 60;
                    int secs = remainingSeconds % 60;
                    timerLabel.setText(String.format("%02d:%02d", mins, secs));
                    timerProgress.setValue(remainingSeconds);
                    timerProgress.setString("Focusing... " + (mins + 1) + "m left");
                } else {
                    swingTimer.stop();
                    isRunning = false;
                    timerProgress.setString("Session Complete!");
                    Toolkit.getDefaultToolkit().beep();
                    JOptionPane.showMessageDialog(StudyHubApp.this,
                            "25-Minute Focus Session Complete! Take a 5-minute break.",
                            "Pomodoro Finished", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });

        startBtn.addActionListener(e -> {
            if (!isRunning) {
                swingTimer.start();
                isRunning = true;
            }
        });

        pauseBtn.addActionListener(e -> {
            if (isRunning) {
                swingTimer.stop();
                isRunning = false;
                timerProgress.setString("Paused");
            }
        });

        resetBtn.addActionListener(e -> {
            swingTimer.stop();
            isRunning = false;
            remainingSeconds = totalSeconds;
            timerLabel.setText("25:00");
            timerProgress.setValue(totalSeconds);
            timerProgress.setString("Ready to Focus");
        });
    }

    private void logMockScore() {
        String subject = subjectBox.getSelectedItem().toString();
        String topic = topicField.getText().trim();

        if (topic.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a chapter or topic name!", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            int correct = Integer.parseInt(correctField.getText().trim());
            int wrong = Integer.parseInt(wrongField.getText().trim());

            if (correct < 0 || wrong < 0) {
                JOptionPane.showMessageDialog(this, "Counts cannot be negative!", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int totalAttempted = correct + wrong;
            int netScore = (correct * 4) - (wrong * 1);
            double accuracy = totalAttempted == 0 ? 0.0 : (correct * 100.0) / totalAttempted;

            tableModel.addRow(new Object[]{
                    subject,
                    topic,
                    correct,
                    wrong,
                    netScore,
                    String.format("%.1f%%", accuracy)
            });

            totalTests++;
            cumulativeScore += netScore;
            updateSummary();

            // Clear inputs for next entry
            topicField.setText("");
            correctField.setText("");
            wrongField.setText("");

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Please enter valid whole numbers for Correct and Wrong answers.",
                    "Number Format Exception", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateSummary() {
        double avg = totalTests == 0 ? 0.0 : (double) cumulativeScore / totalTests;
        statsLabel.setText(String.format("Logged Sessions: %d | Cumulative Points: %d | Average Net Score: %.1f",
                totalTests, cumulativeScore, avg));
    }

    private void styleButton(JButton btn, Color bg) {
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new StudyHubApp().setVisible(true);
        });
    }
}
