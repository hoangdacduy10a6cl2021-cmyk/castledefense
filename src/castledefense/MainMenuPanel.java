/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package castledefense;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

/**
 * MainMenuPanel là màn hình chính của game, phỏng theo giao diện tham khảo:
 * logo/khiên ở giữa trên, 5 nút chức năng (Chơi/Chương/Nâng Cấp/Tùy Chọn/Thoát),
 * và 2 nút icon nhỏ góc trên trái (Thành Tích/Cửa Hàng).
 * Chưa có hình ảnh nhân vật/model thật - chỉ vẽ vector đơn giản để đúng bố cục trước.
 */
public class MainMenuPanel extends JPanel {
    public static final int WIDTH = GamePanel.WIDTH;
    public static final int HEIGHT = GamePanel.HEIGHT;

    private final Runnable onPlay;
    private final Runnable onChapterSelect;
    private final Runnable onUpgrade;
    private final Runnable onOptions;
    private final Runnable onStore;
    private final Runnable onAchievements;

    private final Rectangle gearBtn = new Rectangle(20, 20, 62, 62);
    private final Rectangle achievementsBtn = new Rectangle(20, 96, 62, 62);
    private final Rectangle storeBtn = new Rectangle(20, 172, 62, 62);

    private final String[] menuLabels = {"CHƠI", "CHƯƠNG", "NÂNG CẤP", "TÙY CHỌN", "THOÁT"};
    private final Color[] menuColors = {
            new Color(230, 160, 40),  // Chơi - cam/vàng nổi bật
            new Color(60, 130, 200),  // Chương - xanh dương
            new Color(70, 160, 90),   // Nâng cấp - xanh lá
            new Color(140, 90, 190),  // Tùy chọn - tím
            new Color(200, 70, 60)    // Thoát - đỏ
    };
    private final Rectangle[] menuButtons = new Rectangle[5];
    private int hoveredButton = -1;

    public MainMenuPanel(Runnable onPlay, Runnable onChapterSelect, Runnable onUpgrade, Runnable onOptions, Runnable onStore, Runnable onAchievements) {
        this.onPlay = onPlay;
        this.onChapterSelect = onChapterSelect;
        this.onUpgrade = onUpgrade;
        this.onOptions = onOptions;
        this.onStore = onStore;
        this.onAchievements = onAchievements;
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setFocusable(true);

        int btnW = 280, btnH = 56, gap = 14;
        int startY = 340;
        int cx = WIDTH / 2 - btnW / 2;
        for (int i = 0; i < menuButtons.length; i++) {
            menuButtons[i] = new Rectangle(cx, startY + i * (btnH + gap), btnW, btnH);
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
                int prev = hoveredButton;
                hoveredButton = -1;
                for (int i = 0; i < menuButtons.length; i++) {
                    if (menuButtons[i].contains(e.getPoint())) hoveredButton = i;
                }
                if (prev != hoveredButton) repaint();
            }
        });
    }

    private void handleClick(int mx, int my) {
        if (gearBtn.contains(mx, my)) {
            AudioEngine.playClick();
            onOptions.run();
            return;
        }
        if (achievementsBtn.contains(mx, my)) {
            AudioEngine.playClick();
            onAchievements.run();
            return;
        }
        if (storeBtn.contains(mx, my)) {
            AudioEngine.playClick();
            onStore.run();
            return;
        }
        for (int i = 0; i < menuButtons.length; i++) {
            if (menuButtons[i].contains(mx, my)) {
                AudioEngine.playClick();
                onMenuClick(i);
                return;
            }
        }
    }

    private void onMenuClick(int index) {
        switch (index) {
            case 0 -> onPlay.run();          // CHƠI - vào thẳng chơi nhanh
            case 1 -> onChapterSelect.run();  // CHƯƠNG - mở màn Chọn Chương
            case 2 -> onUpgrade.run();        // NÂNG CẤP - mở màn Nâng Cấp
            case 3 -> onOptions.run();        // TÙY CHỌN - mở màn Tùy Chọn
            case 4 -> System.exit(0);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawBackground(g2);
        drawKnight(g2, 105, 470);
        drawOrcWithCatapult(g2, 700, 470);
        drawTitle(g2);
        drawTopLeftIcons(g2);
        drawMenuButtons(g2);
    }

    // Nền: bầu trời gradient, dãy núi mờ, lâu đài xa xa, mặt đất - toàn vector đơn giản
    private void drawBackground(Graphics2D g2) {
        GradientPaint sky = new GradientPaint(0, 0, new Color(120, 180, 230), 0, 320, new Color(180, 210, 200));
        g2.setPaint(sky);
        g2.fillRect(0, 0, WIDTH, 320);

        g2.setColor(new Color(150, 170, 190, 150));
        Polygon mountains = new Polygon();
        mountains.addPoint(0, 260);
        mountains.addPoint(150, 150);
        mountains.addPoint(300, 260);
        mountains.addPoint(460, 170);
        mountains.addPoint(620, 260);
        mountains.addPoint(770, 190);
        mountains.addPoint(WIDTH, 260);
        mountains.addPoint(WIDTH, 320);
        mountains.addPoint(0, 320);
        g2.fillPolygon(mountains);

        drawDistantCastle(g2, WIDTH - 150, 250);

        GradientPaint ground = new GradientPaint(0, 300, new Color(96, 158, 74), 0, HEIGHT, new Color(55, 100, 46));
        g2.setPaint(ground);
        g2.fillRect(0, 300, WIDTH, HEIGHT - 300);
    }

    private void drawDistantCastle(Graphics2D g2, int cx, int cy) {
        g2.setColor(new Color(150, 158, 172, 220));
        g2.fillRect(cx - 65, cy, 130, 95);
        g2.fillRect(cx - 95, cy + 18, 32, 77);
        g2.fillRect(cx + 63, cy + 18, 32, 77);

        g2.setColor(new Color(60, 100, 190, 220));
        g2.fillPolygon(new int[]{cx - 95, cx - 79, cx - 63}, new int[]{cy + 18, cy - 14, cy + 18}, 3);
        g2.fillPolygon(new int[]{cx + 63, cx + 79, cx + 95}, new int[]{cy + 18, cy - 14, cy + 18}, 3);

        g2.setColor(new Color(120, 128, 142, 220));
        for (int i = -1; i <= 1; i++) {
            g2.fillRect(cx - 65 + (i + 1) * 43 - 8, cy - 12, 16, 16);
        }
    }

    // Tiêu đề dạng khiên xanh dương giống ảnh tham khảo, chữ "CASTLE DEFENSE" 2 dòng
    private void drawTitle(Graphics2D g2) {
        int shieldCx = WIDTH / 2, shieldTopY = 36;
        Polygon shield = new Polygon();
        shield.addPoint(shieldCx - 200, shieldTopY);
        shield.addPoint(shieldCx + 200, shieldTopY);
        shield.addPoint(shieldCx + 200, shieldTopY + 68);
        shield.addPoint(shieldCx, shieldTopY + 108);
        shield.addPoint(shieldCx - 200, shieldTopY + 68);

        g2.setColor(new Color(15, 30, 70, 90));
        g2.fill(new RoundRectangle2D.Double(shieldCx - 205, shieldTopY - 5, 410, 118, 20, 20));

        GradientPaint shieldFill = new GradientPaint(shieldCx, shieldTopY, new Color(45, 85, 165),
                shieldCx, shieldTopY + 108, new Color(20, 45, 100));
        g2.setPaint(shieldFill);
        g2.fillPolygon(shield);
        g2.setColor(new Color(230, 190, 90));
        g2.setStroke(new BasicStroke(4));
        g2.drawPolygon(shield);
        g2.setStroke(new BasicStroke(1));

        g2.setFont(new Font("SansSerif", Font.BOLD, 42));
        String line1 = "CASTLE";
        String line2 = "DEFENSE";
        FontMetrics fm = g2.getFontMetrics();

        g2.setColor(new Color(10, 15, 35));
        g2.drawString(line1, shieldCx - fm.stringWidth(line1) / 2 + 2, shieldTopY + 40);
        g2.setColor(Color.WHITE);
        g2.drawString(line1, shieldCx - fm.stringWidth(line1) / 2, shieldTopY + 38);

        g2.setColor(new Color(10, 15, 35));
        g2.drawString(line2, shieldCx - fm.stringWidth(line2) / 2 + 2, shieldTopY + 82);
        g2.setColor(new Color(255, 210, 60));
        g2.drawString(line2, shieldCx - fm.stringWidth(line2) / 2, shieldTopY + 80);
    }

    // Hiệp sĩ đứng trên bệ đá, khiên tay trái, kiếm tay phải - vẽ vector đơn giản, chỉ trang trí
    private void drawKnight(Graphics2D g2, int x, int y) {
        // Bệ đá
        g2.setColor(new Color(120, 120, 128));
        g2.fillRoundRect(x - 42, y + 50, 92, 22, 8, 8);
        g2.setColor(new Color(90, 90, 98));
        g2.drawRoundRect(x - 42, y + 50, 92, 22, 8, 8);

        // Khiên
        g2.setColor(new Color(60, 100, 190));
        g2.fillRoundRect(x - 48, y + 6, 26, 34, 8, 8);
        g2.setColor(new Color(230, 190, 90));
        g2.drawRoundRect(x - 48, y + 6, 26, 34, 8, 8);
        g2.setColor(Color.WHITE);
        g2.fillOval(x - 40, y + 16, 10, 10);

        // Thân/giáp
        g2.setColor(new Color(70, 90, 140));
        g2.fillRoundRect(x - 14, y + 10, 30, 44, 10, 10);
        g2.setColor(new Color(40, 55, 100));
        g2.drawRoundRect(x - 14, y + 10, 30, 44, 10, 10);

        // Tay cầm kiếm
        g2.setColor(new Color(90, 90, 95));
        g2.fillRect(x + 16, y - 26, 6, 42);
        g2.setColor(new Color(220, 220, 225));
        Polygon blade = new Polygon();
        blade.addPoint(x + 13, y - 26);
        blade.addPoint(x + 22, y - 26);
        blade.addPoint(x + 17, y - 58);
        g2.fillPolygon(blade);
        g2.setColor(new Color(230, 190, 90));
        g2.fillRect(x + 10, y - 26, 16, 4);

        // Mũ giáp + lông mào đỏ
        g2.setColor(new Color(150, 155, 165));
        g2.fillOval(x - 13, y - 20, 26, 26);
        g2.setColor(new Color(90, 90, 98));
        g2.drawOval(x - 13, y - 20, 26, 26);
        g2.setColor(new Color(20, 20, 25));
        g2.fillRect(x - 8, y - 8, 16, 4);
        g2.setColor(new Color(200, 60, 60));
        g2.fillOval(x - 3, y - 30, 6, 12);
    }

    // Quái vật + máy bắn đá (catapult) đang bốc lửa, đứng góc phải - chỉ trang trí
    private void drawOrcWithCatapult(Graphics2D g2, int x, int y) {
        // Khung gỗ máy bắn đá
        g2.setColor(new Color(110, 80, 55));
        g2.fillRect(x - 10, y + 30, 92, 14);
        g2.fillRect(x - 10, y + 16, 12, 40);
        g2.fillRect(x + 70, y + 16, 12, 40);
        g2.setColor(new Color(90, 65, 45));
        g2.setStroke(new BasicStroke(6));
        g2.drawLine(x + 32, y + 36, x + 58, y - 8);
        g2.setStroke(new BasicStroke(1));

        // Cầu lửa trên đầu cần bắn
        g2.setColor(new Color(255, 150, 40));
        g2.fillOval(x + 50, y - 20, 18, 18);
        g2.setColor(new Color(255, 210, 60));
        g2.fillOval(x + 54, y - 16, 9, 9);

        // Thân quái (yêu tinh)
        g2.setColor(new Color(80, 140, 70));
        g2.fillOval(x - 46, y - 6, 44, 46);
        g2.setColor(new Color(50, 100, 45));
        g2.drawOval(x - 46, y - 6, 44, 46);
        g2.setColor(new Color(120, 80, 40));
        g2.fillRect(x - 44, y + 22, 40, 8);

        // Tay cầm chùy
        g2.setColor(new Color(80, 140, 70));
        g2.fillOval(x - 58, y + 2, 16, 16);
        g2.setColor(new Color(120, 90, 60));
        g2.fillRoundRect(x - 74, y - 16, 12, 32, 4, 4);
        g2.setColor(new Color(90, 90, 95));
        g2.fillOval(x - 78, y - 24, 20, 20);

        // Đầu, nanh, mắt
        g2.setColor(new Color(90, 150, 80));
        g2.fillOval(x - 38, y - 30, 30, 28);
        g2.setColor(Color.WHITE);
        g2.fillPolygon(new int[]{x - 30, x - 27, x - 33}, new int[]{y - 6, y - 6, y + 2}, 3);
        g2.fillPolygon(new int[]{x - 16, x - 13, x - 19}, new int[]{y - 6, y - 6, y + 2}, 3);
        g2.setColor(new Color(255, 220, 60));
        g2.fillOval(x - 32, y - 20, 6, 6);
        g2.fillOval(x - 20, y - 20, 6, 6);
        g2.setColor(Color.BLACK);
        g2.fillOval(x - 31, y - 19, 3, 3);
        g2.fillOval(x - 19, y - 19, 3, 3);

        // Vài đốm lửa nhỏ bay quanh cho có không khí chiến trận
        g2.setColor(new Color(255, 140, 40, 200));
        g2.fillOval(x + 90, y - 40, 8, 8);
        g2.fillOval(x + 30, y - 55, 6, 6);
        g2.setColor(new Color(255, 200, 80, 180));
        g2.fillOval(x + 92, y - 38, 4, 4);
    }

    private void drawTopLeftIcons(Graphics2D g2) {
        drawIconButton(g2, gearBtn, new Color(200, 200, 210), "\u2699", "TÙY CHỌN");
        drawIconButton(g2, achievementsBtn, new Color(230, 190, 50), "\u2605", "THÀNH TÍCH");
        drawIconButton(g2, storeBtn, new Color(200, 150, 90), "\u25A4", "CỬA HÀNG");
    }

    private void drawIconButton(Graphics2D g2, Rectangle r, Color iconColor, String icon, String label) {
        g2.setColor(new Color(20, 25, 20, 210));
        g2.fill(new RoundRectangle2D.Double(r.x, r.y, r.width, r.height, 14, 14));
        g2.setColor(new Color(255, 255, 255, 70));
        g2.draw(new RoundRectangle2D.Double(r.x, r.y, r.width, r.height, 14, 14));

        g2.setColor(iconColor);
        g2.setFont(new Font("SansSerif", Font.BOLD, 24));
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(icon, r.x + (r.width - fm.stringWidth(icon)) / 2, r.y + 34);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 9));
        fm = g2.getFontMetrics();
        g2.drawString(label, r.x + (r.width - fm.stringWidth(label)) / 2, r.y + r.height - 7);
    }

    // 5 nút menu chính, mỗi nút màu riêng, có hiệu ứng sáng lên khi hover
    private void drawMenuButtons(Graphics2D g2) {
        for (int i = 0; i < menuButtons.length; i++) {
            Rectangle r = menuButtons[i];
            boolean hovered = (i == hoveredButton);
            Color base = menuColors[i];
            Color top = hovered ? base.brighter() : base;
            Color bottom = base.darker();

            g2.setColor(new Color(0, 0, 0, 90));
            g2.fill(new RoundRectangle2D.Double(r.x + 2, r.y + 4, r.width, r.height, 16, 16));

            GradientPaint grad = new GradientPaint(r.x, r.y, top, r.x, r.y + r.height, bottom);
            g2.setPaint(grad);
            g2.fill(new RoundRectangle2D.Double(r.x, r.y, r.width, r.height, 16, 16));
            g2.setColor(new Color(255, 255, 255, hovered ? 210 : 110));
            g2.setStroke(new BasicStroke(hovered ? 3f : 2f));
            g2.draw(new RoundRectangle2D.Double(r.x, r.y, r.width, r.height, 16, 16));
            g2.setStroke(new BasicStroke(1));

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("SansSerif", Font.BOLD, 20));
            FontMetrics fm = g2.getFontMetrics();
            String label = menuLabels[i];
            g2.drawString(label, r.x + (r.width - fm.stringWidth(label)) / 2, r.y + r.height / 2 + 7);
        }
    }
}