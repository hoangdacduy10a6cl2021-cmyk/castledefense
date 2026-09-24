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
 * Màn hình Tuỳ Chọn trung tâm: 5 thẻ danh mục (Cài Đặt Chung/Đồ Hoạ/Âm Thanh/Điều Khiển/Ngôn Ngữ)
 * dẫn tới OptionsDetailPanel tương ứng, cùng 1 hàng Tài Khoản riêng (hiện là placeholder).
 */
public class OptionsPanel extends JPanel {
    public static final int WIDTH = GamePanel.WIDTH;
    public static final int HEIGHT = GamePanel.HEIGHT;

    public static final int CATEGORY_GENERAL = 0;
    public static final int CATEGORY_GRAPHICS = 1;
    public static final int CATEGORY_AUDIO = 2;
    public static final int CATEGORY_CONTROLS = 3;
    public static final int CATEGORY_LANGUAGE = 4;
    private static final int CATEGORY_COUNT = 5;

    private static final String[] NAMES = {"CÀI ĐẶT CHUNG", "ĐỒ HOẠ", "ÂM THANH", "ĐIỀU KHIỂN", "NGÔN NGỮ"};
    private static final String[] DESCS = {
            "Gợi ý và tự động\nqua đợt", "Bật/tắt hiệu ứng\nhình ảnh",
            "Điều chỉnh âm lượng\nvà âm thanh", "Tuỳ chỉnh phím\nvà thao tác", "Ngôn ngữ\nhiển thị"
    };
    private static final Color[] COLORS = {
            new Color(200, 160, 60), new Color(90, 150, 210), new Color(90, 170, 100),
            new Color(150, 100, 190), new Color(90, 160, 170)
    };

    private final Runnable onBack;
    private final IntConsumer onCategorySelected;
    private final Runnable onAccountClicked;

    private final Rectangle backBtn = new Rectangle(20, 20, 56, 44);
    private final Rectangle[] cards = new Rectangle[CATEGORY_COUNT];
    private final Rectangle accountRow;
    private int hoveredCard = -1;

    public OptionsPanel(Runnable onBack, IntConsumer onCategorySelected, Runnable onAccountClicked) {
        this.onBack = onBack;
        this.onCategorySelected = onCategorySelected;
        this.onAccountClicked = onAccountClicked;
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setFocusable(true);

        int cardW = 150, cardH = 190, gap = 16;
        int totalW = CATEGORY_COUNT * cardW + (CATEGORY_COUNT - 1) * gap;
        int startX = (WIDTH - totalW) / 2;
        int topY = 130;
        for (int i = 0; i < cards.length; i++) {
            cards[i] = new Rectangle(startX + i * (cardW + gap), topY, cardW, cardH);
        }
        accountRow = new Rectangle(startX, topY + cardH + 24, totalW, 74);

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
                AudioEngine.playClick();
                onCategorySelected.accept(i);
                return;
            }
        }
        if (accountRow.contains(mx, my)) {
            AudioEngine.playClick();
            onAccountClicked.run();
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
        for (int i = 0; i < cards.length; i++) drawCard(g2, i);
        drawAccountRow(g2);
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
        g2.setFont(new Font("SansSerif", Font.BOLD, 26));
        String title = "TÙY CHỌN";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(title, cx - fm.stringWidth(title) / 2, y + 38);
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

    private void drawCard(Graphics2D g2, int i) {
        Rectangle r = cards[i];
        boolean hovered = (i == hoveredCard);
        Color base = COLORS[i];

        g2.setColor(new Color(20, 20, 25, 220));
        g2.fill(new RoundRectangle2D.Double(r.x, r.y, r.width, r.height, 16, 16));
        g2.setColor(hovered ? base.brighter() : base);
        g2.setStroke(new BasicStroke(hovered ? 3.5f : 2f));
        g2.draw(new RoundRectangle2D.Double(r.x, r.y, r.width, r.height, 16, 16));
        g2.setStroke(new BasicStroke(1));

        drawCategoryIcon(g2, r.x + r.width / 2, r.y + 60, i, base);

        g2.setColor(base);
        g2.setFont(new Font("SansSerif", Font.BOLD, 14));
        FontMetrics fm = g2.getFontMetrics();
        String name = NAMES[i];
        // Xuống dòng nếu tên danh mục dài hơn bề rộng thẻ
        if (fm.stringWidth(name) > r.width - 16) {
            String[] parts = name.split(" ", 2);
            g2.drawString(parts[0], r.x + (r.width - fm.stringWidth(parts[0])) / 2, r.y + 118);
            if (parts.length > 1) {
                g2.drawString(parts[1], r.x + (r.width - fm.stringWidth(parts[1])) / 2, r.y + 136);
            }
        } else {
            g2.drawString(name, r.x + (r.width - fm.stringWidth(name)) / 2, r.y + 126);
        }

        g2.setColor(new Color(210, 210, 210));
        g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
        String[] descLines = DESCS[i].split("\n");
        int dy = r.y + 156;
        for (String line : descLines) {
            fm = g2.getFontMetrics();
            g2.drawString(line, r.x + (r.width - fm.stringWidth(line)) / 2, dy);
            dy += 15;
        }
    }

    private void drawCategoryIcon(Graphics2D g2, int cx, int cy, int category, Color color) {
        g2.setColor(color);
        g2.setStroke(new BasicStroke(3f));
        switch (category) {
            case CATEGORY_GENERAL:
                // Bánh răng đơn giản
                g2.drawOval(cx - 18, cy - 18, 36, 36);
                g2.fillOval(cx - 6, cy - 6, 12, 12);
                for (int a = 0; a < 8; a++) {
                    double ang = a * Math.PI / 4;
                    int x1 = cx + (int) (Math.cos(ang) * 20), y1 = cy + (int) (Math.sin(ang) * 20);
                    int x2 = cx + (int) (Math.cos(ang) * 26), y2 = cy + (int) (Math.sin(ang) * 26);
                    g2.drawLine(x1, y1, x2, y2);
                }
                break;
            case CATEGORY_GRAPHICS:
                // Màn hình
                g2.drawRoundRect(cx - 22, cy - 16, 44, 30, 4, 4);
                g2.drawLine(cx - 8, cy + 14, cx - 8, cy + 22);
                g2.drawLine(cx + 8, cy + 14, cx + 8, cy + 22);
                g2.drawLine(cx - 14, cy + 22, cx + 14, cy + 22);
                break;
            case CATEGORY_AUDIO:
                // Loa phát
                g2.fillRect(cx - 18, cy - 6, 8, 12);
                g2.fillPolygon(new int[]{cx - 10, cx - 10, cx + 2}, new int[]{cy - 6, cy + 6, cy + 14}, 3);
                g2.fillPolygon(new int[]{cx - 10, cx - 10, cx + 2}, new int[]{cy - 6, cy + 6, cy - 14}, 3);
                g2.drawArc(cx + 4, cy - 14, 20, 28, -50, 100);
                break;
            case CATEGORY_CONTROLS:
                // Tay cầm điều khiển
                g2.drawRoundRect(cx - 24, cy - 10, 48, 22, 14, 14);
                g2.drawLine(cx - 16, cy, cx - 8, cy);
                g2.drawLine(cx - 12, cy - 4, cx - 12, cy + 4);
                g2.fillOval(cx + 6, cy - 4, 6, 6);
                g2.fillOval(cx + 14, cy + 2, 6, 6);
                break;
            default:
                // Ngôn ngữ - hình cầu kèm kinh tuyến
                g2.drawOval(cx - 20, cy - 20, 40, 40);
                g2.drawOval(cx - 10, cy - 20, 20, 40);
                g2.drawLine(cx - 20, cy, cx + 20, cy);
        }
        g2.setStroke(new BasicStroke(1));
    }

    private void drawAccountRow(Graphics2D g2) {
        Rectangle r = accountRow;
        g2.setColor(new Color(30, 28, 38, 220));
        g2.fill(new RoundRectangle2D.Double(r.x, r.y, r.width, r.height, 14, 14));
        g2.setColor(new Color(90, 90, 100));
        g2.setStroke(new BasicStroke(1.5f));
        g2.draw(new RoundRectangle2D.Double(r.x, r.y, r.width, r.height, 14, 14));
        g2.setStroke(new BasicStroke(1));

        int iconCx = r.x + 44, iconCy = r.y + r.height / 2;
        g2.setColor(new Color(180, 180, 190));
        g2.fillOval(iconCx - 14, iconCy - 20, 28, 28);
        g2.fillArc(iconCx - 20, iconCy, 40, 30, 0, 180);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 16));
        g2.drawString("TÀI KHOẢN", r.x + 90, r.y + 30);
        g2.setColor(new Color(190, 190, 190));
        g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g2.drawString("Chưa đăng nhập - tính năng đăng nhập sẽ được làm sau", r.x + 90, r.y + 50);
    }
}