/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package castledefense;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.function.Consumer;

/**
 * Màn hình chọn độ khó trước khi vào 1 màn (hoặc Vô Hạn): Easy/Hard/Nightmare.
 * Độ khó ảnh hưởng sức mạnh quái vật và vàng thưởng khi hoàn thành màn.
 */
public class DifficultySelectPanel extends JPanel {
    public static final int WIDTH = GamePanel.WIDTH;
    public static final int HEIGHT = GamePanel.HEIGHT;

    private final String levelTitle;
    private final Runnable onBack;
    private final Consumer<Difficulty> onStart;

    private final Rectangle backBtn = new Rectangle(20, 20, 56, 44);
    private final Difficulty[] options = {Difficulty.EASY, Difficulty.HARD, Difficulty.NIGHTMARE};
    private final Color[] cardColors = {
            new Color(70, 140, 80), new Color(190, 130, 40), new Color(120, 70, 160)
    };
    private final Rectangle[] cards = new Rectangle[options.length];
    private final Rectangle startBtn;
    private int hoveredCard = -1;
    private int selected = 1; // mặc định chọn Hard

    public DifficultySelectPanel(String levelTitle, Runnable onBack, Consumer<Difficulty> onStart) {
        this.levelTitle = levelTitle;
        this.onBack = onBack;
        this.onStart = onStart;
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setFocusable(true);

        int cardW = 200, cardH = 300, gap = 30;
        int totalW = cards.length * cardW + (cards.length - 1) * gap;
        int startX = (WIDTH - totalW) / 2;
        int topY = 150;
        for (int i = 0; i < cards.length; i++) {
            cards[i] = new Rectangle(startX + i * (cardW + gap), topY, cardW, cardH);
        }
        startBtn = new Rectangle(WIDTH / 2 - 110, topY + cardH + 30, 220, 56);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleClick(e.getX(), e.getY());
            }
        });
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int prev = hoveredCard;
                hoveredCard = -1;
                for (int i = 0; i < cards.length; i++) {
                    if (cards[i].contains(e.getPoint())) hoveredCard = i;
                }
                if (prev != hoveredCard) repaint();
            }
        });
    }

    private void handleClick(int mx, int my) {
        if (backBtn.contains(mx, my)) {
            AudioEngine.playClick();
            onBack.run();
            return;
        }
        for (int i = 0; i < cards.length; i++) {
            if (cards[i].contains(mx, my)) {
                selected = i;
                AudioEngine.playClick();
                repaint();
                return;
            }
        }
        if (startBtn.contains(mx, my)) {
            AudioEngine.playClick();
            onStart.accept(options[selected]);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        GradientPaint bg = new GradientPaint(0, 0, new Color(25, 22, 35), 0, HEIGHT, new Color(10, 9, 15));
        g2.setPaint(bg);
        g2.fillRect(0, 0, WIDTH, HEIGHT);

        drawTitleBanner(g2);
        drawBackButton(g2);
        for (int i = 0; i < cards.length; i++) drawCard(g2, i);
        drawStartButton(g2);
    }

    private void drawTitleBanner(Graphics2D g2) {
        int cx = WIDTH / 2, y = 20, w = 260, h = 56;
        g2.setColor(new Color(15, 20, 15, 220));
        g2.fill(new RoundRectangle2D.Double(cx - w / 2.0, y, w, h, 14, 14));
        g2.setColor(new Color(230, 190, 90));
        g2.setStroke(new BasicStroke(2.5f));
        g2.draw(new RoundRectangle2D.Double(cx - w / 2.0, y, w, h, 14, 14));
        g2.setStroke(new BasicStroke(1));
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 24));
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(levelTitle, cx - fm.stringWidth(levelTitle) / 2, y + 37);

        g2.setColor(new Color(220, 220, 220));
        g2.setFont(new Font("SansSerif", Font.PLAIN, 14));
        String sub = "CHỌN CHẾ ĐỘ";
        fm = g2.getFontMetrics();
        g2.drawString(sub, cx - fm.stringWidth(sub) / 2, y + h + 24);
    }

    private void drawBackButton(Graphics2D g2) {
        g2.setColor(new Color(90, 65, 40, 230));
        g2.fill(new RoundRectangle2D.Double(backBtn.x, backBtn.y, backBtn.width, backBtn.height, 12, 12));
        g2.setColor(new Color(230, 190, 90));
        g2.setStroke(new BasicStroke(2f));
        g2.draw(new RoundRectangle2D.Double(backBtn.x, backBtn.y, backBtn.width, backBtn.height, 12, 12));
        g2.setStroke(new BasicStroke(1));
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 20));
        String arrow = "\u2190";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(arrow, backBtn.x + (backBtn.width - fm.stringWidth(arrow)) / 2, backBtn.y + 29);
    }

    private void drawCard(Graphics2D g2, int index) {
        Rectangle r = cards[index];
        Difficulty d = options[index];
        boolean isSelected = (index == selected);
        boolean hovered = (index == hoveredCard);

        Color base = cardColors[index];
        g2.setColor(new Color(20, 20, 25, 220));
        g2.fill(new RoundRectangle2D.Double(r.x, r.y, r.width, r.height, 16, 16));
        g2.setColor(isSelected ? base.brighter() : (hovered ? base : new Color(90, 90, 95)));
        g2.setStroke(new BasicStroke(isSelected ? 4f : 2f));
        g2.draw(new RoundRectangle2D.Double(r.x, r.y, r.width, r.height, 16, 16));
        g2.setStroke(new BasicStroke(1));

        int cx = r.x + r.width / 2;
        g2.setColor(base);
        g2.setFont(new Font("SansSerif", Font.BOLD, 22));
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(d.label, cx - fm.stringWidth(d.label) / 2, r.y + 40);

        drawSkullIcon(g2, cx, r.y + 110, base, d == Difficulty.NIGHTMARE);

        g2.setColor(new Color(220, 220, 220));
        g2.setFont(new Font("SansSerif", Font.PLAIN, 13));
        fm = g2.getFontMetrics();
        g2.drawString(d.desc, cx - fm.stringWidth(d.desc) / 2, r.y + 190);

        g2.setColor(new Color(180, 180, 180));
        g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
        String rewardLabel = "Phần thưởng";
        fm = g2.getFontMetrics();
        g2.drawString(rewardLabel, cx - fm.stringWidth(rewardLabel) / 2, r.y + 230);

        g2.setColor(new Color(255, 210, 60));
        g2.setFont(new Font("SansSerif", Font.BOLD, 18));
        String rewardValue = "\u25CF x" + d.completionReward;
        fm = g2.getFontMetrics();
        g2.drawString(rewardValue, cx - fm.stringWidth(rewardValue) / 2, r.y + 256);
    }

    // Đầu lâu tượng trưng độ khó; Nightmare có thêm 2 sừng cho dữ tợn hơn
    private void drawSkullIcon(Graphics2D g2, int cx, int cy, Color color, boolean withHorns) {
        g2.setColor(color);
        g2.fillOval(cx - 26, cy - 26, 52, 48);
        g2.setColor(Color.BLACK);
        g2.fillOval(cx - 16, cy - 10, 12, 14);
        g2.fillOval(cx + 4, cy - 10, 12, 14);
        g2.fillRoundRect(cx - 6, cy + 10, 12, 10, 3, 3);

        if (withHorns) {
            g2.setColor(color.darker());
            Polygon leftHorn = new Polygon();
            leftHorn.addPoint(cx - 18, cy - 20);
            leftHorn.addPoint(cx - 34, cy - 42);
            leftHorn.addPoint(cx - 12, cy - 28);
            g2.fillPolygon(leftHorn);
            Polygon rightHorn = new Polygon();
            rightHorn.addPoint(cx + 18, cy - 20);
            rightHorn.addPoint(cx + 34, cy - 42);
            rightHorn.addPoint(cx + 12, cy - 28);
            g2.fillPolygon(rightHorn);
        }
    }

    private void drawStartButton(Graphics2D g2) {
        Rectangle r = startBtn;
        Color base = new Color(70, 160, 90);
        GradientPaint grad = new GradientPaint(r.x, r.y, base.brighter(), r.x, r.y + r.height, base.darker());
        g2.setPaint(grad);
        g2.fill(new RoundRectangle2D.Double(r.x, r.y, r.width, r.height, 14, 14));
        g2.setColor(new Color(230, 190, 90));
        g2.setStroke(new BasicStroke(2.5f));
        g2.draw(new RoundRectangle2D.Double(r.x, r.y, r.width, r.height, 14, 14));
        g2.setStroke(new BasicStroke(1));

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 20));
        String label = "BẮT ĐẦU";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(label, r.x + (r.width - fm.stringWidth(label)) / 2, r.y + r.height / 2 + 7);
    }
}