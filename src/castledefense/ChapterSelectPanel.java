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
 * Màn hình Chọn Chương: hiện 5 banner dọc tượng trưng cho 5 chương.
 * Hiện chỉ Chương 1 có nội dung và mở khoá; các chương còn lại khoá chờ làm thêm.
 */
public class ChapterSelectPanel extends JPanel {
    public static final int WIDTH = GamePanel.WIDTH;
    public static final int HEIGHT = GamePanel.HEIGHT;

    private final Runnable onBack;
    private final IntConsumer onChapterSelected;

    private final Rectangle backBtn = new Rectangle(20, 20, 56, 44);
    private final Rectangle[] chapterBanners = new Rectangle[GameProgress.CHAPTER_COUNT];
    private int hovered = -1;

    private final Color[] chapterColors = {
            new Color(60, 130, 200), new Color(70, 100, 90), new Color(90, 80, 70),
            new Color(70, 70, 90), new Color(150, 70, 40)
    };

    public ChapterSelectPanel(Runnable onBack, IntConsumer onChapterSelected) {
        this.onBack = onBack;
        this.onChapterSelected = onChapterSelected;
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setFocusable(true);

        int bannerW = 150, bannerH = 340, gap = 20;
        int totalW = GameProgress.CHAPTER_COUNT * bannerW + (GameProgress.CHAPTER_COUNT - 1) * gap;
        int startX = (WIDTH - totalW) / 2;
        int topY = 130;
        for (int i = 0; i < chapterBanners.length; i++) {
            chapterBanners[i] = new Rectangle(startX + i * (bannerW + gap), topY, bannerW, bannerH);
        }

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleClick(e.getX(), e.getY());
            }
        });
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int prev = hovered;
                hovered = -1;
                for (int i = 0; i < chapterBanners.length; i++) {
                    if (chapterBanners[i].contains(e.getPoint())) hovered = i;
                }
                if (prev != hovered) repaint();
            }
        });
    }

    private void handleClick(int mx, int my) {
        if (backBtn.contains(mx, my)) {
            AudioEngine.playClick();
            onBack.run();
            return;
        }
        for (int i = 0; i < chapterBanners.length; i++) {
            if (chapterBanners[i].contains(mx, my)) {
                AudioEngine.playClick();
                if (GameProgress.isChapterUnlocked(i)) {
                    onChapterSelected.accept(i);
                } else {
                    JOptionPane.showMessageDialog(this, "Chương này chưa mở khoá.", "Đang khoá", JOptionPane.INFORMATION_MESSAGE);
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

        GradientPaint bg = new GradientPaint(0, 0, new Color(35, 30, 45), 0, HEIGHT, new Color(15, 12, 20));
        g2.setPaint(bg);
        g2.fillRect(0, 0, WIDTH, HEIGHT);

        drawTitleBanner(g2);
        drawBackButton(g2);
        for (int i = 0; i < chapterBanners.length; i++) drawChapterBanner(g2, i);
    }

    private void drawTitleBanner(Graphics2D g2) {
        int cx = WIDTH / 2, y = 20, w = 420, h = 60;
        g2.setColor(new Color(15, 20, 15, 220));
        g2.fill(new RoundRectangle2D.Double(cx - w / 2.0, y, w, h, 14, 14));
        g2.setColor(new Color(230, 190, 90));
        g2.setStroke(new BasicStroke(2.5f));
        g2.draw(new RoundRectangle2D.Double(cx - w / 2.0, y, w, h, 14, 14));
        g2.setStroke(new BasicStroke(1));

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 26));
        String title = "CHỌN CHƯƠNG";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(title, cx - fm.stringWidth(title) / 2, y + 40);
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

    private void drawChapterBanner(Graphics2D g2, int index) {
        Rectangle r = chapterBanners[index];
        boolean unlocked = GameProgress.isChapterUnlocked(index);
        boolean hoveredNow = (index == hovered) && unlocked;

        Polygon banner = new Polygon();
        banner.addPoint(r.x, r.y);
        banner.addPoint(r.x + r.width, r.y);
        banner.addPoint(r.x + r.width, r.y + r.height - 30);
        banner.addPoint(r.x + r.width / 2, r.y + r.height);
        banner.addPoint(r.x, r.y + r.height - 30);

        Color base = unlocked ? chapterColors[index] : new Color(55, 55, 58);
        Color top = hoveredNow ? base.brighter() : base;
        GradientPaint grad = new GradientPaint(r.x, r.y, top, r.x, r.y + r.height, base.darker());
        g2.setPaint(grad);
        g2.fillPolygon(banner);
        g2.setColor(unlocked ? new Color(230, 190, 90) : new Color(90, 90, 95));
        g2.setStroke(new BasicStroke(hoveredNow ? 3f : 2f));
        g2.drawPolygon(banner);
        g2.setStroke(new BasicStroke(1));

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 15));
        String label = "CHƯƠNG";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(label, r.x + (r.width - fm.stringWidth(label)) / 2, r.y + 32);

        g2.setFont(new Font("SansSerif", Font.BOLD, 42));
        String num = String.valueOf(index + 1);
        fm = g2.getFontMetrics();
        g2.drawString(num, r.x + (r.width - fm.stringWidth(num)) / 2, r.y + 84);

        if (!unlocked) {
            int lockX = r.x + r.width / 2, lockY = r.y + r.height / 2 + 20;
            g2.setColor(new Color(20, 20, 20, 200));
            g2.fillRoundRect(lockX - 18, lockY - 8, 36, 28, 8, 8);
            g2.setColor(new Color(200, 200, 200));
            g2.setStroke(new BasicStroke(4f));
            g2.drawArc(lockX - 10, lockY - 26, 20, 24, 0, 180);
            g2.setStroke(new BasicStroke(1));
            g2.setColor(new Color(40, 40, 40));
            g2.fillOval(lockX - 4, lockY, 8, 8);
        } else {
            int earned = GameProgress.totalStarsInChapter(index);
            int max = GameProgress.maxStarsInChapter();
            String starText = "\u2605 " + earned + "/" + max;
            g2.setColor(new Color(15, 20, 15, 220));
            g2.fillRoundRect(r.x + 8, r.y + r.height - 26, r.width - 16, 22, 8, 8);
            g2.setColor(new Color(255, 210, 60));
            g2.setFont(new Font("SansSerif", Font.BOLD, 13));
            fm = g2.getFontMetrics();
            g2.drawString(starText, r.x + (r.width - fm.stringWidth(starText)) / 2, r.y + r.height - 10);
        }
    }
}