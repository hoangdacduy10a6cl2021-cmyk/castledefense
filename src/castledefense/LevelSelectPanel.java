/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package castledefense;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.function.IntConsumer;

/**
 * Màn hình Chọn Màn trong 1 chương: 6 màn cố định (lưới 3x2) + nút Vô Hạn.
 * Màn khoá hiển thị mờ kèm ổ khoá; số sao đã đạt hiện dưới mỗi màn đã mở.
 */
public class LevelSelectPanel extends JPanel {
    public static final int WIDTH = GamePanel.WIDTH;
    public static final int HEIGHT = GamePanel.HEIGHT;

    private final int chapterIndex;
    private final Runnable onBack;
    private final IntConsumer onLevelSelected; // trả về index màn (0..5)
    private final Runnable onEndlessSelected;

    private final Rectangle backBtn = new Rectangle(20, 20, 56, 44);
    private final Rectangle[] levelBoxes = new Rectangle[GameProgress.LEVELS_PER_CHAPTER];
    private final Rectangle endlessBtn;
    private int hoveredLevel = -1;
    private boolean hoveredEndless = false;

    public LevelSelectPanel(int chapterIndex, Runnable onBack, IntConsumer onLevelSelected, Runnable onEndlessSelected) {
        this.chapterIndex = chapterIndex;
        this.onBack = onBack;
        this.onLevelSelected = onLevelSelected;
        this.onEndlessSelected = onEndlessSelected;
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setFocusable(true);

        int cols = 3, rows = 2;
        int boxW = 190, boxH = 150, gapX = 24, gapY = 24;
        int totalW = cols * boxW + (cols - 1) * gapX;
        int startX = (WIDTH - totalW) / 2;
        int startY = 130;
        for (int i = 0; i < levelBoxes.length; i++) {
            int col = i % cols, row = i / cols;
            levelBoxes[i] = new Rectangle(startX + col * (boxW + gapX), startY + row * (boxH + gapY), boxW, boxH);
        }
        endlessBtn = new Rectangle(startX, startY + rows * (boxH + gapY) + 10, totalW, 60);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleClick(e.getX(), e.getY());
            }
        });
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int prevL = hoveredLevel;
                boolean prevE = hoveredEndless;
                hoveredLevel = -1;
                hoveredEndless = false;
                for (int i = 0; i < levelBoxes.length; i++) {
                    if (levelBoxes[i].contains(e.getPoint())) hoveredLevel = i;
                }
                if (endlessBtn.contains(e.getPoint())) hoveredEndless = true;
                if (prevL != hoveredLevel || prevE != hoveredEndless) repaint();
            }
        });
    }

    private void handleClick(int mx, int my) {
        if (backBtn.contains(mx, my)) {
            AudioEngine.playClick();
            onBack.run();
            return;
        }
        if (endlessBtn.contains(mx, my)) {
            AudioEngine.playClick();
            onEndlessSelected.run();
            return;
        }
        for (int i = 0; i < levelBoxes.length; i++) {
            if (levelBoxes[i].contains(mx, my)) {
                AudioEngine.playClick();
                if (GameProgress.isLevelUnlocked(chapterIndex, i)) {
                    onLevelSelected.accept(i);
                } else {
                    JOptionPane.showMessageDialog(this, "Hãy hoàn thành màn trước để mở khoá.", "Đang khoá", JOptionPane.INFORMATION_MESSAGE);
                }
                return;
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        GradientPaint bg = new GradientPaint(0, 0, new Color(60, 110, 65), 0, HEIGHT, new Color(30, 60, 35));
        g2.setPaint(bg);
        g2.fillRect(0, 0, WIDTH, HEIGHT);

        drawTitleBanner(g2);
        drawBackButton(g2);
        for (int i = 0; i < levelBoxes.length; i++) drawLevelBox(g2, i);
        drawEndlessButton(g2);
    }

    private void drawTitleBanner(Graphics2D g2) {
        int cx = WIDTH / 2, y = 20, w = 300, h = 56;
        g2.setColor(new Color(15, 20, 15, 220));
        g2.fill(new RoundRectangle2D.Double(cx - w / 2.0, y, w, h, 14, 14));
        g2.setColor(new Color(230, 190, 90));
        g2.setStroke(new BasicStroke(2.5f));
        g2.draw(new RoundRectangle2D.Double(cx - w / 2.0, y, w, h, 14, 14));
        g2.setStroke(new BasicStroke(1));
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 24));
        String title = "CHƯƠNG " + (chapterIndex + 1);
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(title, cx - fm.stringWidth(title) / 2, y + 37);
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

    private void drawLevelBox(Graphics2D g2, int index) {
        Rectangle r = levelBoxes[index];
        boolean unlocked = GameProgress.isLevelUnlocked(chapterIndex, index);
        boolean hovered = (index == hoveredLevel) && unlocked;
        int stars = GameProgress.getStars(chapterIndex, index);

        Color base = unlocked ? new Color(70, 130, 190) : new Color(60, 60, 62);
        GradientPaint grad = new GradientPaint(r.x, r.y, hovered ? base.brighter() : base, r.x, r.y + r.height, base.darker());
        g2.setPaint(grad);
        g2.fill(new RoundRectangle2D.Double(r.x, r.y, r.width, r.height, 14, 14));
        g2.setColor(unlocked ? new Color(230, 190, 90) : new Color(100, 100, 105));
        g2.setStroke(new BasicStroke(hovered ? 3f : 2f));
        g2.draw(new RoundRectangle2D.Double(r.x, r.y, r.width, r.height, 14, 14));
        g2.setStroke(new BasicStroke(1));

        String label = LevelConfig.CHAPTER_1_LEVELS[index].id;
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 22));
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(label, r.x + (r.width - fm.stringWidth(label)) / 2, r.y + r.height / 2 - 4);

        if (!unlocked) {
            int lockX = r.x + r.width / 2, lockY = r.y + r.height / 2 + 24;
            g2.setColor(new Color(20, 20, 20, 210));
            g2.fillRoundRect(lockX - 14, lockY - 4, 28, 22, 6, 6);
            g2.setColor(new Color(200, 200, 200));
            g2.setStroke(new BasicStroke(3f));
            g2.drawArc(lockX - 8, lockY - 18, 16, 18, 0, 180);
            g2.setStroke(new BasicStroke(1));
        } else {
            int starSize = 16, starGap = 4;
            int totalStarW = starSize * 3 + starGap * 2;
            int sx = r.x + (r.width - totalStarW) / 2;
            int sy = r.y + r.height - 30;
            for (int s = 0; s < 3; s++) {
                g2.setColor(s < stars ? new Color(255, 210, 60) : new Color(255, 255, 255, 60));
                g2.setFont(new Font("SansSerif", Font.BOLD, starSize));
                g2.drawString("\u2605", sx + s * (starSize + starGap), sy + starSize);
            }
        }
    }

    private void drawEndlessButton(Graphics2D g2) {
        Rectangle r = endlessBtn;
        Color base = new Color(110, 70, 150);
        GradientPaint grad = new GradientPaint(r.x, r.y, hoveredEndless ? base.brighter() : base, r.x, r.y + r.height, base.darker());
        g2.setPaint(grad);
        g2.fill(new RoundRectangle2D.Double(r.x, r.y, r.width, r.height, 14, 14));
        g2.setColor(new Color(230, 190, 90));
        g2.setStroke(new BasicStroke(hoveredEndless ? 3f : 2f));
        g2.draw(new RoundRectangle2D.Double(r.x, r.y, r.width, r.height, 14, 14));
        g2.setStroke(new BasicStroke(1));

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 20));
        String label = "\u221E  VÔ HẠN";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(label, r.x + (r.width - fm.stringWidth(label)) / 2, r.y + r.height / 2 + 7);
    }
}