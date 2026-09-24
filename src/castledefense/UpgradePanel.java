/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package castledefense;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.Arrays;

/**
 * Màn hình Nâng Cấp: cột trái chọn loại tháp (7 loại), giữa xem trước hình + chỉ số hiện tại,
 * phải là cây nâng cấp 6 node (2 tầng) dùng Kim Cương, cùng banner Tinh Nhuệ khi maxed cả cây.
 */
public class UpgradePanel extends JPanel {
    public static final int WIDTH = GamePanel.WIDTH;
    public static final int HEIGHT = GamePanel.HEIGHT;

    private final Runnable onBack;
    private final Rectangle backBtn = new Rectangle(20, 20, 56, 44);

    private int selectedType = 0;
    private String hoverMessage = "";

    private final Rectangle[] towerListItems = new Rectangle[TowerCatalog.COUNT];
    private final Rectangle[] nodeCards = new Rectangle[TowerUpgradeData.NODE_COUNT];
    private final Rectangle eliteBanner;

    private final int panelX = 230, panelY = 110, panelW = 280, panelH = 480;
    private final int treeX = 530, treeY = 150, cardW = 105, cardH = 100, cardGapX = 12, cardGapY = 20;

    public UpgradePanel(Runnable onBack) {
        this.onBack = onBack;
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setFocusable(true);

        int listX = 20, listY = 110, itemW = 190, itemH = 66, itemGap = 10;
        for (int i = 0; i < towerListItems.length; i++) {
            towerListItems[i] = new Rectangle(listX, listY + i * (itemH + itemGap), itemW, itemH);
        }

        for (int i = 0; i < nodeCards.length; i++) {
            int col = i % 3, row = i / 3;
            nodeCards[i] = new Rectangle(treeX + col * (cardW + cardGapX), treeY + row * (cardH + cardGapY), cardW, cardH);
        }
        eliteBanner = new Rectangle(treeX, treeY + 2 * (cardH + cardGapY), 3 * cardW + 2 * cardGapX, 100);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleClick(e.getX(), e.getY());
            }
        });
    }

    private void handleClick(int mx, int my) {
        if (backBtn.contains(mx, my)) {
            AudioEngine.playClick();
            onBack.run();
            return;
        }
        for (int i = 0; i < towerListItems.length; i++) {
            if (towerListItems[i].contains(mx, my)) {
                selectedType = i;
                hoverMessage = "";
                AudioEngine.playClick();
                repaint();
                return;
            }
        }
        for (int i = 0; i < nodeCards.length; i++) {
            if (nodeCards[i].contains(mx, my)) {
                attemptUpgrade(i);
                return;
            }
        }
    }

    private void attemptUpgrade(int node) {
        if (!TowerUpgradeData.isNodeUnlocked(selectedType, node)) {
            hoverMessage = "Cần nâng \"" + prereqName(node) + "\" lên cấp 2 trước!";
            AudioEngine.playError();
        } else if (TowerUpgradeData.getLevel(selectedType, node) >= TowerUpgradeData.MAX_LEVEL) {
            hoverMessage = "Chỉ số này đã đạt tối đa!";
            AudioEngine.playError();
        } else if (GameProgress.getGems() < TowerUpgradeData.getCost(selectedType, node)) {
            hoverMessage = "Không đủ Kim Cương!";
            AudioEngine.playError();
        } else {
            boolean ok = TowerUpgradeData.tryUpgrade(selectedType, node);
            hoverMessage = ok ? "Đã nâng cấp \"" + TowerUpgradeData.NODE_NAMES[node] + "\"!" : "Không thể nâng cấp.";
            if (ok) AudioEngine.playUpgradeSuccess(); else AudioEngine.playError();
        }
        repaint();
    }

    private String prereqName(int node) {
        switch (node) {
            case TowerUpgradeData.NODE_CRIT: return TowerUpgradeData.NODE_NAMES[TowerUpgradeData.NODE_DAMAGE];
            case TowerUpgradeData.NODE_PIERCE: return TowerUpgradeData.NODE_NAMES[TowerUpgradeData.NODE_RANGE];
            case TowerUpgradeData.NODE_MULTISHOT: return TowerUpgradeData.NODE_NAMES[TowerUpgradeData.NODE_FIRE_RATE];
            default: return "";
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        GradientPaint bg = new GradientPaint(0, 0, new Color(20, 18, 28), 0, HEIGHT, new Color(8, 7, 12));
        g2.setPaint(bg);
        g2.fillRect(0, 0, WIDTH, HEIGHT);

        drawTopBar(g2);
        drawBackButton(g2);
        drawTowerList(g2);
        drawDetailPanel(g2);
        drawUpgradeTree(g2);

        if (!hoverMessage.isEmpty()) {
            g2.setColor(new Color(0, 0, 0, 190));
            g2.setFont(new Font("SansSerif", Font.BOLD, 14));
            FontMetrics fm = g2.getFontMetrics();
            int w = fm.stringWidth(hoverMessage) + 30;
            g2.fillRoundRect((WIDTH - w) / 2, HEIGHT - 46, w, 30, 10, 10);
            g2.setColor(Color.WHITE);
            g2.drawString(hoverMessage, (WIDTH - fm.stringWidth(hoverMessage)) / 2, HEIGHT - 25);
        }
    }

    private void drawTopBar(Graphics2D g2) {
        int cx = WIDTH / 2, y = 20, w = 300, h = 56;
        g2.setColor(new Color(15, 20, 15, 220));
        g2.fill(new RoundRectangle2D.Double(cx - w / 2.0, y, w, h, 14, 14));
        g2.setColor(new Color(230, 190, 90));
        g2.setStroke(new BasicStroke(2.5f));
        g2.draw(new RoundRectangle2D.Double(cx - w / 2.0, y, w, h, 14, 14));
        g2.setStroke(new BasicStroke(1));
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 26));
        String title = "NÂNG CẤP";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(title, cx - fm.stringWidth(title) / 2, y + 38);

        // Kim Cương hiện có, góc phải trên
        int gw = 140, gh = 40, gx = WIDTH - gw - 20, gy = 26;
        g2.setColor(new Color(20, 20, 30, 220));
        g2.fill(new RoundRectangle2D.Double(gx, gy, gw, gh, 12, 12));
        g2.setColor(new Color(120, 200, 255));
        g2.setStroke(new BasicStroke(2f));
        g2.draw(new RoundRectangle2D.Double(gx, gy, gw, gh, 12, 12));
        g2.setStroke(new BasicStroke(1));
        g2.setFont(new Font("SansSerif", Font.BOLD, 18));
        g2.drawString("\u2666", gx + 14, gy + 27);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 17));
        g2.drawString(String.valueOf(GameProgress.getGems()), gx + 40, gy + 27);
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

    private void drawTowerList(Graphics2D g2) {
        for (int i = 0; i < towerListItems.length; i++) {
            Rectangle r = towerListItems[i];
            boolean selected = (i == selectedType);
            boolean elite = TowerUpgradeData.isElite(i);

            g2.setColor(selected ? new Color(90, 70, 40) : new Color(35, 32, 45));
            g2.fill(new RoundRectangle2D.Double(r.x, r.y, r.width, r.height, 12, 12));
            g2.setColor(selected ? new Color(230, 190, 90) : new Color(70, 70, 80));
            g2.setStroke(new BasicStroke(selected ? 2.5f : 1.5f));
            g2.draw(new RoundRectangle2D.Double(r.x, r.y, r.width, r.height, 12, 12));
            g2.setStroke(new BasicStroke(1));

            drawMiniIcon(g2, r.x + 30, r.y + r.height / 2, i);

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("SansSerif", Font.BOLD, 14));
            g2.drawString(TowerCatalog.NAMES[i], r.x + 56, r.y + 28);

            g2.setColor(elite ? new Color(255, 210, 60) : new Color(200, 200, 200));
            g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
            String lvlText = elite ? "\u2605 TINH NHUỆ" : "Lv." + TowerUpgradeData.overallLevel(i);
            g2.drawString(lvlText, r.x + 56, r.y + 46);
        }
    }

    private void drawMiniIcon(Graphics2D g2, int cx, int cy, int type) {
        g2.setColor(TowerCatalog.COLOR[type]);
        g2.fillOval(cx - 16, cy - 16, 32, 32);
        g2.setColor(new Color(0, 0, 0, 150));
        g2.drawOval(cx - 16, cy - 16, 32, 32);
    }

    private void drawDetailPanel(Graphics2D g2) {
        g2.setColor(new Color(20, 18, 28, 200));
        g2.fill(new RoundRectangle2D.Double(panelX, panelY, panelW, panelH, 14, 14));
        g2.setColor(new Color(90, 70, 40));
        g2.setStroke(new BasicStroke(2f));
        g2.draw(new RoundRectangle2D.Double(panelX, panelY, panelW, panelH, 14, 14));
        g2.setStroke(new BasicStroke(1));

        // Xem trước hình tháp - dùng lại đúng code vẽ tháp trong game cho nhất quán hình ảnh
        Tower preview = new Tower(panelX + panelW / 2, panelY + 110, 0, 0, 0, 0, selectedType, TowerCatalog.COLOR[selectedType]);
        preview.draw(g2);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 20));
        String name = TowerCatalog.NAMES[selectedType];
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(name, panelX + (panelW - fm.stringWidth(name)) / 2, panelY + 155);

        int descY = panelY + 178;
        if (TowerUpgradeData.isElite(selectedType)) {
            g2.setColor(new Color(255, 210, 60));
            g2.setFont(new Font("SansSerif", Font.BOLD, 13));
            String badge = "\u2605 ĐÃ THÀNH TINH NHUỆ";
            fm = g2.getFontMetrics();
            g2.drawString(badge, panelX + (panelW - fm.stringWidth(badge)) / 2, descY);
            descY += 20;
        }

        g2.setColor(new Color(210, 210, 210));
        g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
        wrapText(g2, TowerCatalog.DESCS_LONG[selectedType], panelX + 16, descY, panelW - 32);

        int baseDmg = TowerCatalog.BASE_DAMAGE[selectedType];
        int baseRange = TowerCatalog.BASE_RANGE[selectedType];
        int baseFireRate = TowerCatalog.BASE_FIRE_RATE[selectedType];
        int curDmg = (int) Math.round(baseDmg * TowerUpgradeData.damageMultiplier(selectedType));
        int curRange = baseRange + TowerUpgradeData.rangeBonus(selectedType);
        double curFireRate = baseFireRate * TowerUpgradeData.fireRateMultiplier(selectedType);

        int labelX = panelX + 16, rightX = panelX + panelW - 16, statY = panelY + 268;
        drawStatLine(g2, labelX, rightX, statY, "Sát thương", String.valueOf(curDmg));
        drawStatLine(g2, labelX, rightX, statY + 22, "Tầm bắn", String.valueOf(curRange));
        drawStatLine(g2, labelX, rightX, statY + 44, "Tốc độ bắn", String.format("%.1f/s", 60.0 / curFireRate));
        drawStatLine(g2, labelX, rightX, statY + 66, "Chí mạng", String.format("%.0f%%", TowerUpgradeData.critChance(selectedType) * 100));
        drawStatLine(g2, labelX, rightX, statY + 88, "S.thương Boss", "+" + String.format("%.0f%%", TowerUpgradeData.bossBonusDamageRatio(selectedType) * 100));
        drawStatLine(g2, labelX, rightX, statY + 110, "Bắn lan", String.format("%.0f%%", TowerUpgradeData.multishotChance(selectedType) * 100));

        int totalInvested = TowerUpgradeData.totalInvestedLevels(selectedType);
        int maxInvest = TowerUpgradeData.NODE_COUNT * TowerUpgradeData.MAX_LEVEL;
        int barY = panelY + panelH - 34;
        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRoundRect(panelX + 16, barY, panelW - 32, 16, 8, 8);
        g2.setColor(new Color(230, 190, 90));
        int fillW = (int) ((panelW - 32) * (totalInvested / (double) maxInvest));
        if (fillW > 0) g2.fillRoundRect(panelX + 16, barY, fillW, 16, 8, 8);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
        String progText = totalInvested + "/" + maxInvest + " điểm nâng cấp";
        fm = g2.getFontMetrics();
        g2.drawString(progText, panelX + (panelW - fm.stringWidth(progText)) / 2, barY + 12);
    }

    private void drawStatLine(Graphics2D g2, int x, int rightX, int y, String label, String value) {
        g2.setColor(new Color(180, 180, 180));
        g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g2.drawString(label, x, y);
        g2.setColor(new Color(255, 230, 150));
        g2.setFont(new Font("SansSerif", Font.BOLD, 13));
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(value, rightX - fm.stringWidth(value), y);
    }

    private void wrapText(Graphics2D g2, String text, int x, int y, int maxWidth) {
        FontMetrics fm = g2.getFontMetrics();
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();
        int curY = y;
        for (String w : words) {
            String test = line.length() == 0 ? w : line + " " + w;
            if (fm.stringWidth(test) > maxWidth && line.length() > 0) {
                g2.drawString(line.toString(), x, curY);
                line = new StringBuilder(w);
                curY += 16;
            } else {
                line = new StringBuilder(test);
            }
        }
        if (line.length() > 0) g2.drawString(line.toString(), x, curY);
    }

    private void drawUpgradeTree(Graphics2D g2) {
        for (int i = 0; i < nodeCards.length; i++) drawNodeCard(g2, i);
        drawEliteBanner(g2);
    }

    private void drawNodeCard(Graphics2D g2, int node) {
        Rectangle r = nodeCards[node];
        boolean unlocked = TowerUpgradeData.isNodeUnlocked(selectedType, node);
        int lvl = TowerUpgradeData.getLevel(selectedType, node);
        boolean maxed = lvl >= TowerUpgradeData.MAX_LEVEL;

        Color base = !unlocked ? new Color(40, 40, 45) : maxed ? new Color(55, 90, 50) : new Color(50, 46, 65);
        g2.setColor(base);
        g2.fill(new RoundRectangle2D.Double(r.x, r.y, r.width, r.height, 12, 12));
        g2.setColor(unlocked ? new Color(230, 190, 90) : new Color(80, 80, 85));
        g2.setStroke(new BasicStroke(2f));
        g2.draw(new RoundRectangle2D.Double(r.x, r.y, r.width, r.height, 12, 12));
        g2.setStroke(new BasicStroke(1));

        g2.setColor(unlocked ? Color.WHITE : new Color(130, 130, 130));
        g2.setFont(new Font("SansSerif", Font.BOLD, 13));
        drawCenteredWrapped(g2, TowerUpgradeData.NODE_NAMES[node], r.x + r.width / 2, r.y + 26, r.width - 16);

        g2.setColor(new Color(210, 210, 210));
        g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
        FontMetrics fm = g2.getFontMetrics();
        String lvlText = "Lv " + lvl + "/" + TowerUpgradeData.MAX_LEVEL;
        g2.drawString(lvlText, r.x + (r.width - fm.stringWidth(lvlText)) / 2, r.y + 62);

        String bottomText;
        Color bottomColor;
        if (!unlocked) {
            bottomText = "Đang khoá";
            bottomColor = new Color(150, 150, 150);
        } else if (maxed) {
            bottomText = "TỐI ĐA";
            bottomColor = new Color(150, 230, 150);
        } else {
            bottomText = "\u2666 " + TowerUpgradeData.getCost(selectedType, node);
            bottomColor = new Color(120, 200, 255);
        }
        g2.setColor(bottomColor);
        g2.setFont(new Font("SansSerif", Font.BOLD, 13));
        fm = g2.getFontMetrics();
        g2.drawString(bottomText, r.x + (r.width - fm.stringWidth(bottomText)) / 2, r.y + 86);
    }

    // Viết tên node vào giữa thẻ, tự xuống 2 dòng nếu tên dài hơn bề rộng thẻ
    private void drawCenteredWrapped(Graphics2D g2, String text, int cx, int y, int maxWidth) {
        FontMetrics fm = g2.getFontMetrics();
        if (fm.stringWidth(text) <= maxWidth) {
            g2.drawString(text, cx - fm.stringWidth(text) / 2, y);
            return;
        }
        String[] words = text.split(" ");
        int mid = Math.max(1, words.length / 2);
        String line1 = String.join(" ", Arrays.copyOfRange(words, 0, mid));
        String line2 = String.join(" ", Arrays.copyOfRange(words, mid, words.length));
        g2.drawString(line1, cx - fm.stringWidth(line1) / 2, y - 8);
        g2.drawString(line2, cx - fm.stringWidth(line2) / 2, y + 8);
    }

    private void drawEliteBanner(Graphics2D g2) {
        Rectangle r = eliteBanner;
        boolean elite = TowerUpgradeData.isElite(selectedType);

        g2.setColor(elite ? new Color(120, 90, 30) : new Color(35, 32, 40));
        g2.fill(new RoundRectangle2D.Double(r.x, r.y, r.width, r.height, 14, 14));
        g2.setColor(elite ? new Color(255, 215, 90) : new Color(80, 80, 85));
        g2.setStroke(new BasicStroke(elite ? 3f : 1.5f));
        g2.draw(new RoundRectangle2D.Double(r.x, r.y, r.width, r.height, 14, 14));
        g2.setStroke(new BasicStroke(1));

        String eliteName = TowerCatalog.NAMES[selectedType].toUpperCase() + " TINH NHUỆ";
        g2.setColor(elite ? new Color(255, 230, 150) : new Color(150, 150, 150));
        g2.setFont(new Font("SansSerif", Font.BOLD, 15));
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(eliteName, r.x + (r.width - fm.stringWidth(eliteName)) / 2, r.y + 24);

        g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g2.setColor(new Color(200, 200, 200));
        String sub = elite
                ? "Đã mở khoá! +15% sát thương/tầm bắn, bắn nhanh hơn"
                : "Nâng tối đa cả 6 chỉ số bên trên để mở khoá phiên bản Tinh Nhuệ";
        drawWrappedCentered(g2, sub, r.x + r.width / 2, r.y + 44, r.width - 20, 16);

        if (elite) {
            g2.setColor(new Color(255, 215, 90));
            g2.setFont(new Font("SansSerif", Font.BOLD, 20));
            String star = "\u2605 \u2605 \u2605";
            fm = g2.getFontMetrics();
            g2.drawString(star, r.x + (r.width - fm.stringWidth(star)) / 2, r.y + 88);
        }
    }

    // Viết 1 đoạn văn bản dài, tự xuống dòng và căn giữa từng dòng theo chiều ngang thẻ
    private void drawWrappedCentered(Graphics2D g2, String text, int cx, int startY, int maxWidth, int lineHeight) {
        FontMetrics fm = g2.getFontMetrics();
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();
        int curY = startY;
        for (String w : words) {
            String test = line.length() == 0 ? w : line + " " + w;
            if (fm.stringWidth(test) > maxWidth && line.length() > 0) {
                g2.drawString(line.toString(), cx - fm.stringWidth(line.toString()) / 2, curY);
                line = new StringBuilder(w);
                curY += lineHeight;
            } else {
                line = new StringBuilder(test);
            }
        }
        if (line.length() > 0) {
            g2.drawString(line.toString(), cx - fm.stringWidth(line.toString()) / 2, curY);
        }
    }
}