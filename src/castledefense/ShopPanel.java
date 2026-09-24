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
 * Màn hình Cửa Hàng: 3 tab GÓI KIM CƯƠNG / GÓI VÀNG (mua bằng tiền thật - chưa khả dụng vì
 * game không xử lý thanh toán thật) và VẬT PHẨM (mua thật bằng Kim Cương, dùng trong lúc chơi).
 * Sidebar phải: THẺ THÁNG (chưa khả dụng) và XEM QUẢNG CÁO (giả lập, 1 lần/phiên).
 */
public class ShopPanel extends JPanel {
    public static final int WIDTH = GamePanel.WIDTH;
    public static final int HEIGHT = GamePanel.HEIGHT;

    private static final int TAB_GEMS = 0;
    private static final int TAB_GOLD = 1;
    private static final int TAB_ITEMS = 2;
    private static final String[] TAB_NAMES = {"GÓI KIM CƯƠNG", "GÓI VÀNG", "VẬT PHẨM"};

    private static final String[] GEM_PACK_NAMES = {"GÓI NHỎ", "GÓI VỪA", "GÓI LỚN", "GÓI SIÊU LỚN", "GÓI SIÊU KHỦNG"};
    private static final int[] GEM_PACK_AMOUNT = {100, 550, 1250, 2800, 6500};
    private static final String[] GEM_PACK_PRICE = {"22.000 đ", "109.000 đ", "219.000 đ", "439.000 đ", "1.099.000 đ"};
    private static final String[] GEM_PACK_BADGE = {null, null, "HOT", null, "BEST"};

    private static final int[] GOLD_PACK_AMOUNT = {10000, 50000, 110000, 250000, 600000};
    private static final int[] GOLD_PACK_COST_GEMS = {100, 400, 800, 1600, 3000};

    private final Runnable onBack;
    private int activeTab = TAB_GEMS;

    private final Rectangle backBtn = new Rectangle(20, 20, 56, 44);
    private final Rectangle[] tabRects = new Rectangle[3];
    private final Rectangle[] cardRects = new Rectangle[6];
    private final Rectangle[] buyBtnRects = new Rectangle[6]; // riêng cho tab Vật Phẩm

    private final Rectangle monthlyCardBtn = new Rectangle(WIDTH - 190, 250, 150, 42);
    private final Rectangle adBtn = new Rectangle(WIDTH - 190, 470, 150, 42);

    private String message = "";

    // Trạng thái giả lập xem quảng cáo: đếm ngược khung hình rồi mới thưởng, không có quảng cáo thật nào
    private boolean adWatching = false;
    private int adTicksLeft = 0;
    private static final int AD_TOTAL_TICKS = 20; // ~2 giây ở 100ms/tick
    private final Timer adTimer;

    public ShopPanel(Runnable onBack) {
        this.onBack = onBack;
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setFocusable(true);

        int tabW = 150, tabH = 40, tabGap = 10;
        int totalTabW = 3 * tabW + 2 * tabGap;
        int tabStartX = (WIDTH - 210) / 2 - totalTabW / 2; // hơi lệch trái để chừa chỗ sidebar phải
        for (int i = 0; i < tabRects.length; i++) {
            tabRects[i] = new Rectangle(tabStartX + i * (tabW + tabGap), 90, tabW, tabH);
        }

        adTimer = new Timer(100, e -> tickAd());

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleClick(e.getX(), e.getY());
            }
        });
    }

    private void tickAd() {
        adTicksLeft--;
        if (adTicksLeft <= 0) {
            adTimer.stop();
            adWatching = false;
            GameProgress.markAdWatched();
            GameProgress.addGems(15);
            message = "Xem xong! Nhận được +15 Kim Cương.";
            AudioEngine.playUpgradeSuccess();
        }
        repaint();
    }

    private void handleClick(int mx, int my) {
        if (adWatching) return; // đang "xem quảng cáo" thì khoá thao tác khác cho giống thật

        if (backBtn.contains(mx, my)) {
            AudioEngine.playClick();
            onBack.run();
            return;
        }
        for (int i = 0; i < tabRects.length; i++) {
            if (tabRects[i].contains(mx, my)) {
                activeTab = i;
                message = "";
                AudioEngine.playClick();
                repaint();
                return;
            }
        }
        if (monthlyCardBtn.contains(mx, my)) {
            AudioEngine.playClick();
            JOptionPane.showMessageDialog(this, "Tính năng thanh toán thật chưa khả dụng trong bản này.",
                    "Thẻ Tháng", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (adBtn.contains(mx, my)) {
            AudioEngine.playClick();
            if (!GameProgress.canWatchAd()) {
                message = "Bạn đã xem quảng cáo trong phiên này rồi!";
                repaint();
                return;
            }
            adWatching = true;
            adTicksLeft = AD_TOTAL_TICKS;
            message = "";
            adTimer.start();
            repaint();
            return;
        }

        if (activeTab == TAB_ITEMS) {
            for (int i = 0; i < ItemCatalog.COUNT; i++) {
                if (buyBtnRects[i] != null && buyBtnRects[i].contains(mx, my)) {
                    AudioEngine.playClick();
                    if (GameProgress.buyItem(i)) {
                        GameProgress.addItemPurchased();
                        message = "Đã mua " + ItemCatalog.NAMES[i] + " x" + ItemCatalog.QUANTITY[i] + "!";
                        AudioEngine.playUpgradeSuccess();
                    } else {
                        message = "Không đủ Kim Cương!";
                        AudioEngine.playError();
                    }
                    repaint();
                    return;
                }
            }
        } else {
            int count = activeTab == TAB_GEMS ? GEM_PACK_NAMES.length : GOLD_PACK_AMOUNT.length;
            for (int i = 0; i < count; i++) {
                if (cardRects[i] != null && cardRects[i].contains(mx, my)) {
                    AudioEngine.playClick();
                    JOptionPane.showMessageDialog(this, "Tính năng thanh toán thật chưa khả dụng trong bản này.",
                            "Chưa khả dụng", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
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
        drawGemCounter(g2);
        drawTabs(g2);

        switch (activeTab) {
            case TAB_GEMS: drawGemPacks(g2); break;
            case TAB_GOLD: drawGoldPacks(g2); break;
            default: drawItems(g2);
        }

        drawSidebar(g2);

        if (!message.isEmpty()) {
            g2.setColor(new Color(0, 0, 0, 190));
            g2.setFont(new Font("SansSerif", Font.BOLD, 14));
            FontMetrics fm = g2.getFontMetrics();
            int w = fm.stringWidth(message) + 30;
            g2.fillRoundRect((WIDTH - w) / 2, HEIGHT - 46, w, 30, 10, 10);
            g2.setColor(Color.WHITE);
            g2.drawString(message, (WIDTH - fm.stringWidth(message)) / 2, HEIGHT - 25);
        }

        if (adWatching) {
            g2.setColor(new Color(0, 0, 0, 210));
            g2.fillRect(0, 0, WIDTH, HEIGHT);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("SansSerif", Font.BOLD, 22));
            String txt = "Đang xem quảng cáo giả lập...";
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(txt, (WIDTH - fm.stringWidth(txt)) / 2, HEIGHT / 2 - 20);

            int barW = 300, barH = 16;
            int barX = (WIDTH - barW) / 2, barY = HEIGHT / 2;
            double pct = 1.0 - (double) adTicksLeft / AD_TOTAL_TICKS;
            g2.setColor(new Color(60, 60, 65));
            g2.fillRoundRect(barX, barY, barW, barH, 8, 8);
            g2.setColor(new Color(90, 200, 120));
            g2.fillRoundRect(barX, barY, (int) (barW * pct), barH, 8, 8);
        }
    }

    private void drawTitleBanner(Graphics2D g2) {
        int cx = WIDTH / 2, y = 20, w = 260, h = 56;
        g2.setColor(new Color(70, 25, 25, 230));
        g2.fill(new RoundRectangle2D.Double(cx - w / 2.0, y, w, h, 14, 14));
        g2.setColor(new Color(230, 190, 90));
        g2.setStroke(new BasicStroke(2.5f));
        g2.draw(new RoundRectangle2D.Double(cx - w / 2.0, y, w, h, 14, 14));
        g2.setStroke(new BasicStroke(1));
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 26));
        String title = "CỬA HÀNG";
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

    private void drawGemCounter(Graphics2D g2) {
        int gw = 130, gh = 40, gx = WIDTH - gw - 20, gy = 26;
        g2.setColor(new Color(20, 20, 30, 220));
        g2.fill(new RoundRectangle2D.Double(gx, gy, gw, gh, 12, 12));
        g2.setColor(new Color(120, 200, 255));
        g2.setStroke(new BasicStroke(2f));
        g2.draw(new RoundRectangle2D.Double(gx, gy, gw, gh, 12, 12));
        g2.setStroke(new BasicStroke(1));
        g2.setFont(new Font("SansSerif", Font.BOLD, 18));
        g2.drawString("\u2666", gx + 12, gy + 27);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 16));
        g2.drawString(String.valueOf(GameProgress.getGems()), gx + 36, gy + 27);
    }

    private void drawTabs(Graphics2D g2) {
        for (int i = 0; i < tabRects.length; i++) {
            Rectangle r = tabRects[i];
            boolean active = (i == activeTab);
            g2.setColor(active ? new Color(90, 70, 40) : new Color(35, 32, 45));
            g2.fill(new RoundRectangle2D.Double(r.x, r.y, r.width, r.height, 10, 10));
            g2.setColor(active ? new Color(230, 190, 90) : new Color(80, 80, 90));
            g2.setStroke(new BasicStroke(active ? 2.5f : 1.5f));
            g2.draw(new RoundRectangle2D.Double(r.x, r.y, r.width, r.height, 10, 10));
            g2.setStroke(new BasicStroke(1));
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("SansSerif", Font.BOLD, 13));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(TAB_NAMES[i], r.x + (r.width - fm.stringWidth(TAB_NAMES[i])) / 2, r.y + 25);
        }
    }

    // --- Tab GÓI KIM CƯƠNG: mua bằng tiền thật (VNĐ) - hiện đẹp nhưng bấm vào báo "chưa khả dụng" ---
    private void drawGemPacks(Graphics2D g2) {
        int n = GEM_PACK_NAMES.length;
        int contentW = (WIDTH - 210) - 40; // trừ sidebar phải (210) và lề trái/phải (20 mỗi bên)
        int gap = 12;
        int cardW = (contentW - (n - 1) * gap) / n;
        int cardH = 240;
        int totalW = n * cardW + (n - 1) * gap;
        int startX = 20 + (contentW - totalW) / 2;
        int y = 150;

        for (int i = 0; i < n; i++) {
            int x = startX + i * (cardW + gap);
            Rectangle r = new Rectangle(x, y, cardW, cardH);
            cardRects[i] = r;

            g2.setColor(new Color(20, 18, 28, 220));
            g2.fill(new RoundRectangle2D.Double(r.x, r.y, r.width, r.height, 14, 14));
            g2.setColor(new Color(90, 70, 40));
            g2.setStroke(new BasicStroke(2f));
            g2.draw(new RoundRectangle2D.Double(r.x, r.y, r.width, r.height, 14, 14));
            g2.setStroke(new BasicStroke(1));

            if (GEM_PACK_BADGE[i] != null) {
                g2.setColor(new Color(210, 60, 60));
                g2.fillRoundRect(r.x + r.width - 50, r.y - 6, 50, 22, 8, 8);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("SansSerif", Font.BOLD, 11));
                FontMetrics fmB = g2.getFontMetrics();
                g2.drawString(GEM_PACK_BADGE[i], r.x + r.width - 50 + (50 - fmB.stringWidth(GEM_PACK_BADGE[i])) / 2, r.y + 9);
            }

            g2.setColor(new Color(230, 190, 90));
            g2.setFont(new Font("SansSerif", Font.BOLD, 12));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(GEM_PACK_NAMES[i], r.x + (r.width - fm.stringWidth(GEM_PACK_NAMES[i])) / 2, r.y + 26);

            drawGemStack(g2, r.x + r.width / 2, r.y + 100, i);

            g2.setColor(new Color(120, 200, 255));
            g2.setFont(new Font("SansSerif", Font.BOLD, 15));
            String amt = "\u2666 " + GEM_PACK_AMOUNT[i];
            fm = g2.getFontMetrics();
            g2.drawString(amt, r.x + (r.width - fm.stringWidth(amt)) / 2, r.y + 160);

            Rectangle priceBtn = new Rectangle(r.x + 10, r.y + r.height - 42, r.width - 20, 32);
            g2.setColor(new Color(70, 150, 80));
            g2.fill(new RoundRectangle2D.Double(priceBtn.x, priceBtn.y, priceBtn.width, priceBtn.height, 8, 8));
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("SansSerif", Font.BOLD, 13));
            fm = g2.getFontMetrics();
            g2.drawString(GEM_PACK_PRICE[i], priceBtn.x + (priceBtn.width - fm.stringWidth(GEM_PACK_PRICE[i])) / 2, priceBtn.y + 21);
        }
    }

    // --- Tab GÓI VÀNG: đổi bằng Kim Cương nhưng game chưa có ví Vàng vĩnh viễn -> cũng để "chưa khả dụng" ---
    private void drawGoldPacks(Graphics2D g2) {
        int n = GOLD_PACK_AMOUNT.length;
        int contentW = (WIDTH - 210) - 40;
        int gap = 12;
        int cardW = (contentW - (n - 1) * gap) / n;
        int cardH = 200;
        int totalW = n * cardW + (n - 1) * gap;
        int startX = 20 + (contentW - totalW) / 2;
        int y = 170;

        for (int i = 0; i < n; i++) {
            int x = startX + i * (cardW + gap);
            Rectangle r = new Rectangle(x, y, cardW, cardH);
            cardRects[i] = r;

            g2.setColor(new Color(20, 18, 28, 220));
            g2.fill(new RoundRectangle2D.Double(r.x, r.y, r.width, r.height, 14, 14));
            g2.setColor(new Color(90, 70, 40));
            g2.setStroke(new BasicStroke(2f));
            g2.draw(new RoundRectangle2D.Double(r.x, r.y, r.width, r.height, 14, 14));
            g2.setStroke(new BasicStroke(1));

            g2.setColor(new Color(230, 190, 90));
            g2.setFont(new Font("SansSerif", Font.BOLD, 15));
            String amt = String.format("%,d", GOLD_PACK_AMOUNT[i]).replace(",", ".") + " VÀNG";
            drawCenteredWrap(g2, amt, r.x + r.width / 2, r.y + 30, r.width - 16);

            drawCoinStack(g2, r.x + r.width / 2, r.y + 100, i);

            Rectangle priceBtn = new Rectangle(r.x + 10, r.y + r.height - 42, r.width - 20, 32);
            g2.setColor(new Color(70, 130, 190));
            g2.fill(new RoundRectangle2D.Double(priceBtn.x, priceBtn.y, priceBtn.width, priceBtn.height, 8, 8));
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("SansSerif", Font.BOLD, 13));
            String priceTxt = "\u2666 " + GOLD_PACK_COST_GEMS[i];
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(priceTxt, priceBtn.x + (priceBtn.width - fm.stringWidth(priceTxt)) / 2, priceBtn.y + 21);
        }
    }

    // --- Tab VẬT PHẨM: mua thật bằng Kim Cương, hiện số lượng đang có trong kho ---
    private void drawItems(Graphics2D g2) {
        int n = ItemCatalog.COUNT;
        int contentW = (WIDTH - 210) - 40;
        int gap = 10;
        int cardW = (contentW - (n - 1) * gap) / n;
        int cardH = 260;
        int totalW = n * cardW + (n - 1) * gap;
        int startX = 20 + (contentW - totalW) / 2;
        int y = 150;

        for (int i = 0; i < n; i++) {
            int x = startX + i * (cardW + gap);
            Rectangle r = new Rectangle(x, y, cardW, cardH);

            g2.setColor(new Color(20, 18, 28, 220));
            g2.fill(new RoundRectangle2D.Double(r.x, r.y, r.width, r.height, 14, 14));
            g2.setColor(ItemCatalog.COLOR[i]);
            g2.setStroke(new BasicStroke(2f));
            g2.draw(new RoundRectangle2D.Double(r.x, r.y, r.width, r.height, 14, 14));
            g2.setStroke(new BasicStroke(1));

            drawItemIcon(g2, r.x + r.width / 2, r.y + 50, i);

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("SansSerif", Font.BOLD, 13));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(ItemCatalog.NAMES[i], r.x + (r.width - fm.stringWidth(ItemCatalog.NAMES[i])) / 2, r.y + 92);

            g2.setColor(new Color(190, 190, 190));
            g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
            drawCenteredWrap(g2, ItemCatalog.DESCS[i], r.x + r.width / 2, r.y + 110, r.width - 14);

            g2.setColor(new Color(255, 230, 150));
            g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
            String owned = "Đang có: " + GameProgress.getItemCount(i);
            fm = g2.getFontMetrics();
            g2.drawString(owned, r.x + (r.width - fm.stringWidth(owned)) / 2, r.y + r.height - 60);

            Rectangle buyBtn = new Rectangle(r.x + 8, r.y + r.height - 42, r.width - 16, 32);
            buyBtnRects[i] = buyBtn;
            g2.setColor(new Color(70, 150, 80));
            g2.fill(new RoundRectangle2D.Double(buyBtn.x, buyBtn.y, buyBtn.width, buyBtn.height, 8, 8));
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("SansSerif", Font.BOLD, 12));
            String label = "x" + ItemCatalog.QUANTITY[i] + "  \u2666" + ItemCatalog.GEM_COST[i];
            fm = g2.getFontMetrics();
            g2.drawString(label, buyBtn.x + (buyBtn.width - fm.stringWidth(label)) / 2, buyBtn.y + 21);
        }
    }

    private void drawItemIcon(Graphics2D g2, int cx, int cy, int id) {
        g2.setColor(ItemCatalog.COLOR[id]);
        switch (id) {
            case ItemCatalog.HEAL:
                g2.fillRoundRect(cx - 14, cy - 18, 28, 34, 8, 8);
                g2.setColor(Color.WHITE);
                g2.fillRect(cx - 3, cy - 12, 6, 20);
                g2.fillRect(cx - 10, cy - 5, 20, 6);
                break;
            case ItemCatalog.BUILD_BOOST:
                g2.fillRoundRect(cx - 4, cy - 16, 8, 30, 3, 3);
                g2.setColor(new Color(180, 180, 185));
                g2.fillRect(cx - 14, cy - 16, 28, 10);
                break;
            case ItemCatalog.BOMB:
                g2.fillOval(cx - 14, cy - 10, 28, 28);
                g2.setColor(new Color(90, 60, 40));
                g2.fillRect(cx - 2, cy - 20, 4, 12);
                g2.setColor(new Color(255, 150, 40));
                g2.fillOval(cx + 2, cy - 24, 6, 6);
                break;
            case ItemCatalog.FROST:
                g2.setStroke(new BasicStroke(3f));
                g2.drawLine(cx, cy - 16, cx, cy + 16);
                g2.drawLine(cx - 14, cy - 8, cx + 14, cy + 8);
                g2.drawLine(cx - 14, cy + 8, cx + 14, cy - 8);
                g2.setStroke(new BasicStroke(1));
                break;
            case ItemCatalog.SHIELD:
                g2.fillRoundRect(cx - 14, cy - 16, 28, 22, 8, 8);
                Polygon tip = new Polygon();
                tip.addPoint(cx - 14, cy + 4);
                tip.addPoint(cx + 14, cy + 4);
                tip.addPoint(cx, cy + 18);
                g2.fillPolygon(tip);
                g2.setColor(Color.WHITE);
                g2.fillOval(cx - 4, cy - 8, 8, 8);
                break;
            default:
                g2.fillOval(cx - 10, cy - 18, 20, 20);
                g2.fillPolygon(new int[]{cx - 16, cx, cx + 16}, new int[]{cy + 10, cy - 4, cy + 10}, 3);
        }
    }

    private void drawGemStack(Graphics2D g2, int cx, int cy, int packIndex) {
        int size = 16 + packIndex * 4;
        g2.setColor(new Color(110, 200, 255));
        for (int k = 0; k <= packIndex; k++) {
            int ox = (k % 3 - 1) * (size / 2);
            int oy = (k / 3) * (size / 3) - (packIndex >= 3 ? size / 4 : 0);
            Polygon gem = new Polygon();
            gem.addPoint(cx + ox, cy + oy - size / 2);
            gem.addPoint(cx + ox - size / 2, cy + oy);
            gem.addPoint(cx + ox, cy + oy + size / 2);
            gem.addPoint(cx + ox + size / 2, cy + oy);
            g2.fillPolygon(gem);
            g2.setColor(new Color(30, 100, 160));
            g2.drawPolygon(gem);
            g2.setColor(new Color(110, 200, 255));
        }
    }

    private void drawCoinStack(Graphics2D g2, int cx, int cy, int packIndex) {
        int rows = 2 + Math.min(packIndex, 3);
        g2.setColor(new Color(230, 190, 90));
        for (int row = 0; row < rows; row++) {
            int w = 44 - row * 4;
            int y = cy + 24 - row * 8;
            g2.fillOval(cx - w / 2, y, w, 14);
            g2.setColor(new Color(160, 120, 40));
            g2.drawOval(cx - w / 2, y, w, 14);
            g2.setColor(new Color(230, 190, 90));
        }
    }

    private void drawSidebar(Graphics2D g2) {
        int sx = WIDTH - 210, sw = 190;

        // Thẻ Tháng - mua bằng tiền thật, chưa khả dụng
        g2.setColor(new Color(60, 40, 90, 220));
        g2.fill(new RoundRectangle2D.Double(sx, 150, sw, 160, 14, 14));
        g2.setColor(new Color(180, 140, 230));
        g2.setStroke(new BasicStroke(2f));
        g2.draw(new RoundRectangle2D.Double(sx, 150, sw, 160, 14, 14));
        g2.setStroke(new BasicStroke(1));
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 15));
        FontMetrics fm = g2.getFontMetrics();
        String t1 = "THẺ THÁNG";
        g2.drawString(t1, sx + (sw - fm.stringWidth(t1)) / 2, 178);
        g2.setColor(new Color(220, 210, 235));
        g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
        drawCenteredWrap(g2, "Nhận Kim Cương mỗi ngày khi đăng ký", sx + sw / 2, 205, sw - 20);

        monthlyCardBtn.setLocation(sx + 20, 150 + 160 - 50);
        g2.setColor(new Color(140, 90, 190));
        g2.fill(new RoundRectangle2D.Double(monthlyCardBtn.x, monthlyCardBtn.y, monthlyCardBtn.width, monthlyCardBtn.height, 8, 8));
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 13));
        String priceTxt = "109.000 đ";
        fm = g2.getFontMetrics();
        g2.drawString(priceTxt, monthlyCardBtn.x + (monthlyCardBtn.width - fm.stringWidth(priceTxt)) / 2, monthlyCardBtn.y + 27);

        // Xem Quảng Cáo - giả lập, 1 lần/phiên
        boolean canWatch = GameProgress.canWatchAd();
        int adBoxY = 380;
        g2.setColor(new Color(30, 60, 40, 220));
        g2.fill(new RoundRectangle2D.Double(sx, adBoxY, sw, 160, 14, 14));
        g2.setColor(new Color(120, 200, 140));
        g2.setStroke(new BasicStroke(2f));
        g2.draw(new RoundRectangle2D.Double(sx, adBoxY, sw, 160, 14, 14));
        g2.setStroke(new BasicStroke(1));
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 15));
        String t2 = "XEM QUẢNG CÁO";
        fm = g2.getFontMetrics();
        g2.drawString(t2, sx + (sw - fm.stringWidth(t2)) / 2, adBoxY + 28);
        g2.setColor(new Color(210, 230, 210));
        g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
        drawCenteredWrap(g2, canWatch ? "Xem để nhận ngay +15 Kim Cương miễn phí!" : "Bạn đã xem trong phiên chơi này rồi.",
                sx + sw / 2, adBoxY + 55, sw - 20);

        adBtn.setLocation(sx + 20, adBoxY + 160 - 50);
        g2.setColor(canWatch ? new Color(70, 160, 90) : new Color(70, 70, 75));
        g2.fill(new RoundRectangle2D.Double(adBtn.x, adBtn.y, adBtn.width, adBtn.height, 8, 8));
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 14));
        String label = canWatch ? "XEM" : "ĐÃ XEM";
        fm = g2.getFontMetrics();
        g2.drawString(label, adBtn.x + (adBtn.width - fm.stringWidth(label)) / 2, adBtn.y + 27);
    }

    private void drawCenteredWrap(Graphics2D g2, String text, int cx, int startY, int maxWidth) {
        FontMetrics fm = g2.getFontMetrics();
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();
        int curY = startY;
        for (String w : words) {
            String test = line.length() == 0 ? w : line + " " + w;
            if (fm.stringWidth(test) > maxWidth && line.length() > 0) {
                g2.drawString(line.toString(), cx - fm.stringWidth(line.toString()) / 2, curY);
                line = new StringBuilder(w);
                curY += 14;
            } else {
                line = new StringBuilder(test);
            }
        }
        if (line.length() > 0) {
            g2.drawString(line.toString(), cx - fm.stringWidth(line.toString()) / 2, curY);
        }
    }
}