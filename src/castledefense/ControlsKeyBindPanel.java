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
 * Màn hình đổi phím tắt: liệt kê tất cả hành động có thể gán phím (chọn tháp, 3 kỹ năng, bán tháp).
 * Bấm nút phím của 1 hành động rồi nhấn phím bất kỳ để gán; nếu phím đó đang được hành động khác
 * dùng, GameSettings sẽ tự hoán đổi 2 phím cho nhau. Nhấn phím "cố định" (Space/Esc/R) sẽ huỷ chờ gán.
 */
public class ControlsKeyBindPanel extends JPanel {
    public static final int WIDTH = GamePanel.WIDTH;
    public static final int HEIGHT = GamePanel.HEIGHT;

    private final Runnable onBack;
    private final Rectangle backBtn = new Rectangle(20, 20, 56, 44);
    private final Rectangle resetBtn = new Rectangle(WIDTH - 260, 24, 240, 40);

    private final Rectangle[] rebindButtons = new Rectangle[Gamesettings.ACTION_COUNT];
    private int listeningAction = -1; // action đang chờ nhấn phím mới để gán, -1 = không có

    private final int listX = 100, listY = 110, rowH = 46, rowW = WIDTH - 200;

    public ControlsKeyBindPanel(Runnable onBack) {
        this.onBack = onBack;
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setFocusable(true);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleClick(e.getX(), e.getY());
            }
        });
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (listeningAction < 0) return;
                int code = e.getKeyCode();
                // Phím cố định của hệ thống (Space/Esc/R) không gán được - coi như huỷ chờ gán
                if (Gamesettings.isReserved(code)) {
                    listeningAction = -1;
                    repaint();
                    return;
                }
                Gamesettings.setKey(listeningAction, code);
                listeningAction = -1;
                AudioEngine.playClick();
                repaint();
            }
        });
    }

    private void handleClick(int mx, int my) {
        if (backBtn.contains(mx, my)) {
            AudioEngine.playClick();
            onBack.run();
            return;
        }
        if (resetBtn.contains(mx, my)) {
            Gamesettings.resetKeysToDefault();
            listeningAction = -1;
            AudioEngine.playClick();
            repaint();
            return;
        }
        for (int i = 0; i < rebindButtons.length; i++) {
            if (rebindButtons[i] != null && rebindButtons[i].contains(mx, my)) {
                listeningAction = i;
                AudioEngine.playClick();
                repaint();
                return;
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        GradientPaint bg = new GradientPaint(0, 0, new Color(22, 20, 30), 0, HEIGHT, new Color(9, 8, 13));
        g2.setPaint(bg);
        g2.fillRect(0, 0, WIDTH, HEIGHT);

        drawTitleBanner(g2);
        drawBackButton(g2);
        drawResetButton(g2);

        for (int i = 0; i < Gamesettings.ACTION_COUNT; i++) {
            drawRow(g2, i, listY + i * rowH);
        }

        if (listeningAction >= 0) {
            g2.setColor(new Color(0, 0, 0, 190));
            g2.setFont(new Font("SansSerif", Font.PLAIN, 13));
            String note = "Đang chờ nhấn phím cho \"" + Gamesettings.ACTION_NAMES[listeningAction] + "\"... (Esc để huỷ)";
            FontMetrics fm = g2.getFontMetrics();
            int w = fm.stringWidth(note) + 30;
            g2.fillRoundRect((WIDTH - w) / 2, HEIGHT - 46, w, 30, 10, 10);
            g2.setColor(Color.WHITE);
            g2.drawString(note, (WIDTH - fm.stringWidth(note)) / 2, HEIGHT - 25);
        }
    }

    private void drawRow(Graphics2D g2, int action, int y) {
        boolean listening = (action == listeningAction);
        boolean altRow = action % 2 == 1;

        g2.setColor(altRow ? new Color(30, 28, 38, 160) : new Color(24, 22, 32, 160));
        g2.fillRoundRect(listX, y, rowW, rowH - 6, 8, 8);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 14));
        g2.drawString(Gamesettings.ACTION_NAMES[action], listX + 16, y + 27);

        int btnW = 150, btnH = 32;
        Rectangle r = new Rectangle(listX + rowW - btnW - 12, y + (rowH - 6 - btnH) / 2, btnW, btnH);
        rebindButtons[action] = r;

        g2.setColor(listening ? new Color(200, 150, 40) : new Color(55, 52, 68));
        g2.fill(new RoundRectangle2D.Double(r.x, r.y, r.width, r.height, 8, 8));
        g2.setColor(new Color(230, 190, 90));
        g2.setStroke(new BasicStroke(listening ? 2.5f : 1.5f));
        g2.draw(new RoundRectangle2D.Double(r.x, r.y, r.width, r.height, 8, 8));
        g2.setStroke(new BasicStroke(1));

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 13));
        String label = listening ? "Nhấn phím..." : Gamesettings.keyName(action);
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(label, r.x + (r.width - fm.stringWidth(label)) / 2, r.y + 21);
    }

    private void drawTitleBanner(Graphics2D g2) {
        int cx = WIDTH / 2, y = 20, w = 320, h = 56;
        g2.setColor(new Color(15, 20, 15, 220));
        g2.fill(new RoundRectangle2D.Double(cx - w / 2.0, y, w, h, 14, 14));
        g2.setColor(new Color(230, 190, 90));
        g2.setStroke(new BasicStroke(2.5f));
        g2.draw(new RoundRectangle2D.Double(cx - w / 2.0, y, w, h, 14, 14));
        g2.setStroke(new BasicStroke(1));
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 22));
        String title = "TÙY CHỈNH PHÍM";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(title, cx - fm.stringWidth(title) / 2, y + 36);
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

    private void drawResetButton(Graphics2D g2) {
        Rectangle r = resetBtn;
        g2.setColor(new Color(110, 40, 40, 230));
        g2.fill(new RoundRectangle2D.Double(r.x, r.y, r.width, r.height, 10, 10));
        g2.setColor(new Color(230, 190, 90));
        g2.setStroke(new BasicStroke(1.5f));
        g2.draw(new RoundRectangle2D.Double(r.x, r.y, r.width, r.height, 10, 10));
        g2.setStroke(new BasicStroke(1));
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 13));
        String label = "Đặt Lại Phím Mặc Định";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(label, r.x + (r.width - fm.stringWidth(label)) / 2, r.y + r.height / 2 + 5);
    }
}