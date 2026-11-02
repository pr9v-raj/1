import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Random;

public class CricVisionAI extends JFrame {

    private final Color BG_STADIUM = new Color(9, 18, 16);
    private final Color CARD_DARK = new Color(17, 34, 29);
    private final Color BORDER_EMERALD = new Color(16, 185, 129);
    private final Color GOLD_ACCENT = new Color(250, 204, 21);
    private final Color TEXT_LIGHT = new Color(240, 253, 244);

    private int runs = 0, wickets = 0, legalBalls = 0, maxOvers = 20, target = 175;
    private JLabel scoreBigLabel, oversBigLabel, crrBigLabel, rrrBigLabel, aiCommentaryLabel;
    private JProgressBar winProbBar;
    private DefaultTableModel ballLogModel;
    private JTextField targetInput, oversInput;
    private final Random aiRng = new Random();

    public CricVisionAI() {
        setTitle("CricVision AI - Broadcast Stadium Telemetry & Win-Probability Predictor");
        setSize(1020, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout(14, 14));
        root.setBackground(BG_STADIUM);
        root.setBorder(new EmptyBorder(16, 16, 16, 16));
        setContentPane(root);

        root.add(buildBroadcastBanner(), BorderLayout.NORTH);
        root.add(buildOperatorDeck(), BorderLayout.WEST);
        root.add(buildTelemetryFeed(), BorderLayout.CENTER);

        recalculateWinProbability("Match Ready! Target set to 175 in 20.0 Overs.");
    }

    private JPanel buildBroadcastBanner() {
        SmoothCard banner = new SmoothCard(20, CARD_DARK, BORDER_EMERALD);
        banner.setLayout(new BorderLayout(12, 10));
        banner.setBorder(new EmptyBorder(14, 20, 14, 20));

        JPanel metrics = new JPanel(new GridLayout(1, 4, 15, 0));
        metrics.setOpaque(false);

        scoreBigLabel = createHeroStat("0 / 0", GOLD_ACCENT, 32);
        oversBigLabel = createHeroStat("OVERS: 0.0 / 20", TEXT_LIGHT, 20);
        crrBigLabel = createHeroStat("CRR: 0.00", new Color(52, 211, 153), 20);
        rrrBigLabel = createHeroStat("RRR: 8.75", new Color(251, 146, 60), 20);

        metrics.add(scoreBigLabel);
        metrics.add(oversBigLabel);
        metrics.add(crrBigLabel);
        metrics.add(rrrBigLabel);

        // AI Win Probability Bar
        JPanel winBox = new JPanel(new BorderLayout(6, 4));
        winBox.setOpaque(false);

        aiCommentaryLabel = new JLabel("AI TELEMETRY: Waiting for opening delivery...");
        aiCommentaryLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        aiCommentaryLabel.setForeground(GOLD_ACCENT);

        winProbBar = new JProgressBar(0, 100);
        winProbBar.setValue(50);
        winProbBar.setStringPainted(true);
        winProbBar.setPreferredSize(new Dimension(800, 24));
        winProbBar.setForeground(BORDER_EMERALD);
        winProbBar.setBackground(new Color(185, 28, 28));
        winProbBar.setString("AI Batting Win Probability: 50% | Bowling Team: 50%");

        winBox.add(aiCommentaryLabel, BorderLayout.NORTH);
        winBox.add(winProbBar, BorderLayout.SOUTH);

        banner.add(metrics, BorderLayout.CENTER);
        banner.add(winBox, BorderLayout.SOUTH);
        return banner;
    }

    private JPanel buildOperatorDeck() {
        SmoothCard deck = new SmoothCard(18, CARD_DARK, new Color(4, 120, 87));
        deck.setPreferredSize(new Dimension(400, 460));
        deck.setLayout(new BorderLayout(10, 10));
        deck.setBorder(new EmptyBorder(14, 14, 14, 14));

        // Top Match Config
        JPanel cfg = new JPanel(new GridLayout(2, 3, 8, 8));
        cfg.setOpaque(false);

        oversInput = createStyledField("20");
        targetInput = createStyledField("175");
        GlowButton setBtn = new GlowButton("Set Target", new Color(37, 99, 235));
        GlowButton resetBtn = new GlowButton("Reset Match", new Color(100, 116, 139));

        setBtn.addActionListener(e -> applyConfig());
        resetBtn.addActionListener(e -> resetMatch());

        cfg.add(new JLabel("<html><font color='#A7F3D0'>Overs:</font></html>"));
        cfg.add(oversInput);
        cfg.add(setBtn);
        cfg.add(new JLabel("<html><font color='#A7F3D0'>Target:</font></html>"));
        cfg.add(targetInput);
        cfg.add(resetBtn);

        // Center Ball Pad
        JPanel pad = new JPanel(new GridLayout(3, 3, 8, 8));
        pad.setOpaque(false);

        pad.add(makeBallButton("0 (Dot Ball)", 0, true, false, new Color(51, 65, 85)));
        pad.add(makeBallButton("+1 Single", 1, true, false, new Color(16, 185, 129)));
        pad.add(makeBallButton("+2 Double", 2, true, false, new Color(16, 185, 129)));
        pad.add(makeBallButton("+3 Triple", 3, true, false, new Color(5, 150, 105)));
        pad.add(makeBallButton("FOUR (+4)", 4, true, false, new Color(37, 99, 235)));
        pad.add(makeBallButton("SIXER (+6)", 6, true, false, new Color(147, 51, 234)));
        pad.add(makeBallButton("Wide (+1)", 1, false, false, new Color(217, 119, 6)));
        pad.add(makeBallButton("No Ball (+1)", 1, false, false, new Color(217, 119, 6)));
        pad.add(makeBallButton("WICKET!", 0, true, true, new Color(220, 38, 38)));

        // Bottom AI Auto-Simulator Controls
        JPanel aiSimRow = new JPanel(new GridLayout(1, 2, 10, 0));
        aiSimRow.setOpaque(false);

        GlowButton aiBallBtn = new GlowButton("AI Simulate Next Ball", new Color(13, 148, 136));
        GlowButton aiOverBtn = new GlowButton("AI Simulate Full Over", new Color(124, 58, 237));

        aiBallBtn.addActionListener(e -> simulateAiBall());
        aiOverBtn.addActionListener(e -> {
            for (int i = 0; i < 6; i++) {
                if (wickets < 10 && legalBalls < maxOvers * 6 && runs < target) simulateAiBall();
            }
        });

        aiSimRow.add(aiBallBtn);
        aiSimRow.add(aiOverBtn);

        deck.add(cfg, BorderLayout.NORTH);
        deck.add(pad, BorderLayout.CENTER);
        deck.add(aiSimRow, BorderLayout.SOUTH);
        return deck;
    }

    private JPanel buildTelemetryFeed() {
        SmoothCard feed = new SmoothCard(18, CARD_DARK, new Color(4, 120, 87));
        feed.setLayout(new BorderLayout(8, 8));
        feed.setBorder(new EmptyBorder(14, 14, 14, 14));

        JLabel title = new JLabel("LIVE BALL-BY-BALL BROADCAST TELEMETRY LOG");
        title.setForeground(GOLD_ACCENT);
        title.setFont(new Font("SansSerif", Font.BOLD, 13));

        String[] cols = {"Over", "Event", "Score", "CRR", "Win Prob %"};
        ballLogModel = new DefaultTableModel(cols, 0);
        JTable table = new JTable(ballLogModel);
        table.setBackground(BG_STADIUM);
        table.setForeground(TEXT_LIGHT);
        table.setRowHeight(26);
        table.setGridColor(new Color(6, 78, 59));
        table.getTableHeader().setBackground(CARD_DARK);
        table.getTableHeader().setForeground(GOLD_ACCENT);

        JScrollPane sp = new JScrollPane(table);
        sp.getViewport().setBackground(BG_STADIUM);

        feed.add(title, BorderLayout.NORTH);
        feed.add(sp, BorderLayout.CENTER);
        return feed;
    }

    private JButton makeBallButton(String label, int r, boolean legal, boolean wkt, Color c) {
        GlowButton b = new GlowButton(label, c);
        b.addActionListener(e -> recordDelivery(label, r, legal, wkt));
        return b;
    }

    private void simulateAiBall() {
        int roll = aiRng.nextInt(100);
        if (roll < 32) recordDelivery("AI: 0 (Dot Ball)", 0, true, false);
        else if (roll < 64) recordDelivery("AI: +1 Single", 1, true, false);
        else if (roll < 74) recordDelivery("AI: +2 Double", 2, true, false);
        else if (roll < 86) recordDelivery("AI: FOUR (+4)", 4, true, false);
        else if (roll < 93) recordDelivery("AI: SIXER (+6)", 6, true, false);
        else if (roll < 96) recordDelivery("AI: Wide (+1)", 1, false, false);
        else recordDelivery("AI: WICKET!", 0, true, true);
    }

    private void recordDelivery(String label, int r, boolean legal, boolean wkt) {
        if (wickets >= 10 || legalBalls >= maxOvers * 6 || (target > 0 && runs >= target)) {
            JOptionPane.showMessageDialog(this, "Match Concluded! Click 'Reset Match' to start a new game.");
            return;
        }

        runs += r;
        if (wkt) wickets++;
        if (legal) legalBalls++;

        String overStr = (legalBalls / 6) + "." + (legalBalls % 6);
        double oversDec = legalBalls / 6.0;
        double crr = oversDec == 0 ? 0.0 : runs / oversDec;

        scoreBigLabel.setText(runs + " / " + wickets);
        oversBigLabel.setText("OVERS: " + overStr + " / " + maxOvers);
        crrBigLabel.setText(String.format("CRR: %.2f", crr));

        int winProb = recalculateWinProbability(label);

        ballLogModel.insertRow(0, new Object[]{
                overStr, label, runs + "/" + wickets, String.format("%.2f", crr), winProb + "%"
        });
    }

    private int recalculateWinProbability(String lastAction) {
        int ballsLeft = (maxOvers * 6) - legalBalls;
        int runsNeeded = target - runs;

        if (runsNeeded <= 0) {
            rrrBigLabel.setText("CHASING TEAM WON!");
            winProbBar.setValue(100);
            winProbBar.setString("MATCH WON BY " + (10 - wickets) + " WICKETS! (Win Prob: 100%)");
            aiCommentaryLabel.setText("AI VERDICT: Target successfully overhauled with " + ballsLeft + " balls remaining!");
            return 100;
        }
        if (wickets >= 10 || ballsLeft <= 0) {
            rrrBigLabel.setText("DEFENDING WON!");
            winProbBar.setValue(0);
            winProbBar.setString("BOWLING TEAM WINS BY " + (runsNeeded - 1) + " RUNS! (Batting Win Prob: 0%)");
            aiCommentaryLabel.setText("AI VERDICT: Innings closed! Defending side restricts chase.");
            return 0;
        }

        double rrr = (runsNeeded * 6.0) / ballsLeft;
        rrrBigLabel.setText(String.format("RRR: %.2f", rrr));

        // Logistic Heuristic Win Probability Model (RRR + Wickets in Hand)
        double resourceScore = 62.0 - ((rrr - 8.0) * 6.5) - (wickets * 5.2);
        int prob = (int) Math.max(2, Math.min(98, Math.round(resourceScore)));

        winProbBar.setValue(prob);
        winProbBar.setString("AI Batting Win Probability: " + prob + "%  |  Bowling Defend Probability: " + (100 - prob) + "%");

        String paceAdvice = rrr > 11.5 ? "High Pressure! Batters need ~2 boundaries per over."
                : (rrr < 6.5 ? "Cruising comfortably. Rotate strike & avoid risky lofted shots."
                : "Balanced contest! Need " + runsNeeded + " off " + ballsLeft + " balls.");
        aiCommentaryLabel.setText("AI TELEMETRY [" + lastAction + "]: " + paceAdvice);
        return prob;
    }

    private void applyConfig() {
        try {
            maxOvers = Math.max(1, Integer.parseInt(oversInput.getText().trim()));
            target = Math.max(1, Integer.parseInt(targetInput.getText().trim()));
            resetMatch();
        } catch (NumberFormatException ignored) {}
    }

    private void resetMatch() {
        runs = 0;
        wickets = 0;
        legalBalls = 0;
        ballLogModel.setRowCount(0);
        scoreBigLabel.setText("0 / 0");
        oversBigLabel.setText("OVERS: 0.0 / " + maxOvers);
        crrBigLabel.setText("CRR: 0.00");
        recalculateWinProbability("New Match Initialized");
    }

    private JLabel createHeroStat(String txt, Color c, int size) {
        JLabel l = new JLabel(txt, SwingConstants.CENTER);
        l.setFont(new Font("Monospaced", Font.BOLD, size));
        l.setForeground(c);
        return l;
    }

    private JTextField createStyledField(String txt) {
        JTextField f = new JTextField(txt);
        f.setBackground(BG_STADIUM);
        f.setForeground(GOLD_ACCENT);
        f.setHorizontalAlignment(JTextField.CENTER);
        f.setFont(new Font("Monospaced", Font.BOLD, 14));
        f.setBorder(BorderFactory.createLineBorder(BORDER_EMERALD));
        return f;
    }

    static class SmoothCard extends JPanel {
        private final int rad;
        private final Color bg, border;
        public SmoothCard(int rad, Color bg, Color border) {
            this.rad = rad; this.bg = bg; this.border = border;
            setOpaque(false);
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, rad, rad);
            g2.setColor(border);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, rad, rad);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    static class GlowButton extends JButton {
        private final Color col;
        private boolean hover = false;
        public GlowButton(String text, Color col) {
            super(text);
            this.col = col;
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setForeground(Color.WHITE);
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
            g2.setColor(hover ? col.brighter() : col);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CricVisionAI().setVisible(true));
    }
}
