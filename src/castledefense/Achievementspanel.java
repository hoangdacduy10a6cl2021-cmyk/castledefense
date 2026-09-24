/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package castledefense;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Màn hình Thành Tích: sidebar trái chọn nhóm (Tất Cả/Chế Độ Chơi/Chiến Đấu/Phòng Thủ/Khác),
 * cột giữa hiện Danh Hiệu + Thống Kê tổng quan (luôn hiện, không đổi theo bộ lọc),
 * cột phải là danh sách thành tích có thể lọc theo trạng thái + nhận thưởng Kim Cương.
 */
public class Achievementspanel extends JPanel {
    public static final int WIDTH = GamePanel.WIDTH;
    public static final int HEIGHT = GamePanel.HEIGHT;

    private static final int FILTER_ALL_CATEGORY = -1;
    private static final String[] SIDEBAR_LABELS = {"Tất Cả", "Chế Độ Chơi", "Chiến Đấu", "Phòng Thủ", "Khác"};

    private static final int STATUS_IN_PROGRESS = 0;
    private static final int STATUS_COMPLETED = 1;
    private static final int STATUS_ALL = 2;
    private static final String[] STATUS_LABELS = {"Đang Tiến Hành", "Đã Hoàn Thành", "Tất Cả"};

    private static final int CARD_H = 78, CARD_GAP = 8, CARDS_PER_PAGE = 5;

    private final Runnable onBack;
    private int categoryFilter = FILTER_ALL_CATEGORY;
    private int statusFilter = STATUS_IN_PROGRESS;
    private int page = 0;
    private String message = "";

    private final Rectangle backBtn = new Rectangle(20, 20, 56, 44);
    private final Rectangle[] sidebarRects = new Rectangle[SIDEBAR_LABELS.length];
    private final Rectangle[] statusTabRects = new Rectangle[STATUS_LABELS.length];
    private final Rectangle[] cardRects = new Rectangle[CARDS_PER_PAGE];
    private final Rectangle[] claimBtnRects = new Rectangle[CARDS_PER_PAGE];
    private final Rectangle prevPageBtn, nextPageBtn;

    private final int listX = 340, listY = 190, listW = 540;

    public Achievementspanel(Runnable onBack) {
        this.onBack = onBack;
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setFocusable(true);

        int sbW = 140, sbH = 40, sbGap = 8, sbX = 20, sbY = 100;
        for (int i = 0; i < sidebarRects.length; i++) {
            sidebarRects[i] = new Rectangle(sbX, sbY + i * (sbH + sbGap), sbW, sbH);
        }

        int tabW = 172, tabH = 36, tabGap = 8;
        for (int i = 0; i < statusTabRects.length; i++) {
            statusTabRects[i] = new Rectangle(listX + i * (tabW + tabGap), 150, tabW, tabH);
        }

        for (int i = 0; i < cardRects.length; i++) {
            int y = listY + i * (CARD_H + CARD_GAP);
            cardRects[i] = new Rectangle(listX, y, listW, CARD_H);
        }

        int pagerY = listY + CARDS_PER_PAGE * (CARD_H + CARD_GAP) + 6;
        prevPageBtn = new Rectangle(listX, pagerY, 60, 32);
        nextPageBtn = new Rectangle(listX + listW - 60, pagerY, 60, 32);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleClick(e.getX(), e.getY());
            }
        });
    }

    private List<Integer> filteredAchievements() {
        List<Integer> result = new ArrayList<>();
        for (int id = 0; id < AchievementCatalog.COUNT; id++) {
            if (categoryFilter != FILTER_ALL_CATEGORY && AchievementCatalog.CATEGORY[id] != categoryFilter) continue;
            boolean completed = AchievementCatalog.getProgress(id) >= AchievementCatalog.TARGET[id];
            if (statusFilter == STATUS_IN_PROGRESS && completed) continue;
            if (statusFilter == STATUS_COMPLETED && !completed) continue;
            result.add(id);
        }
        return result;
    }

    private void handleClick(int mx, int my) {
        if (backBtn.contains(mx, my)) {
            AudioEngine.playClick();
            onBack.run();
            return;
        }
        for (int i = 0; i < sidebarRects.length; i++) {
            if (sidebarRects[i].contains(mx, my)) {
                categoryFilter = (i == 0) ? FILTER_ALL_CATEGORY : i - 1;
                page = 0;
                AudioEngine.playClick();
                repaint();
                return;
            }
        }
        for (int i = 0; i < statusTabRects.length; i++) {
            if (statusTabRects[i].contains(mx, my)) {
                statusFilter = i;
                page = 0;
                AudioEngine.playClick();
                repaint();
                return;
            }
        }

        List<Integer> list = filteredAchievements();
        int maxPage = Math.max(0, (list.size() - 1) / CARDS_PER_PAGE);
        if (prevPageBtn.contains(mx, my) && page > 0) {
            page--;
            AudioEngine.playClick();
            repaint();
            return;
        }
        if (nextPageBtn.contains(mx, my) && page < maxPage) {
            page++;
            AudioEngine.playClick();
            repaint();
            return;
        }

        for (int i = 0; i < claimBtnRects.length; i++) {
            if (claimBtnRects[i] != null && claimBtnRects[i].contains(mx, my)) {
                int idx = page * CARDS_PER_PAGE + i;
                if (idx >= list.size()) continue;
                int id = list.get(idx);
                if (GameProgress.claimAchievement(id)) {
                    message = "Đã nhận +" + AchievementCatalog.REWARD_GEMS[id] + " Kim Cương từ \"" + AchievementCatalog.NAMES[id] + "\"!";
                    AudioEngine.playUpgradeSuccess();
                } else {
                    message = "Chưa đủ điều kiện nhận thưởng.";
                    AudioEngine.playError();
                }
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
        drawGemCounter(g2);
        drawSidebar(g2);
        drawOverview(g2);
        drawStatusTabs(g2);
        drawAchievementList(g2);

        if (!message.isEmpty()) {
            g2.setColor(new Color(0, 0, 0, 190));
            g2.setFont(new Font("SansSerif", Font.BOLD, 13));
            FontMetrics fm = g2.getFontMetrics();
            int w = fm.stringWidth(message) + 30;
            g2.fillRoundRect((WIDTH - w) / 2, HEIGHT - 42, w, 28, 10, 10);
            g2.setColor(Color.WHITE);
            g2.drawString(message, (WIDTH - fm.stringWidth(message)) / 2, HEIGHT - 23);
        }
    }

    private void drawTitleBanner(Graphics2D g2) {
        int cx = WIDTH / 2, y = 20, w = 280, h = 56;
        g2.setColor(new Color(25, 45, 80, 230));
        g2.fill(new RoundRectangle2D.Double(cx - w / 2.0, y, w, h, 14, 14));
        g2.setColor(new Color(230, 190, 90));
        g2.setStroke(new BasicStroke(2.5f));
        g2.draw(new RoundRectangle2D.Double(cx - w / 2.0, y, w, h, 14, 14));
        g2.setStroke(new BasicStroke(1));
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 26));
        String title = "THÀNH TÍCH";
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

    private void drawSidebar(Graphics2D g2) {
        for (int i = 0; i < sidebarRects.length; i++) {
            Rectangle r = sidebarRects[i];
            boolean active = (i == 0 && categoryFilter == FILTER_ALL_CATEGORY) || (i - 1 == categoryFilter);
            g2.setColor(active ? new Color(90, 70, 40) : new Color(30, 28, 38));
            g2.fill(new RoundRectangle2D.Double(r.x, r.y, r.width, r.height, 10, 10));
            g2.setColor(active ? new Color(230, 190, 90) : new Color(70, 70, 80));
            g2.setStroke(new BasicStroke(active ? 2.5f : 1.5f));
            g2.draw(new RoundRectangle2D.Double(r.x, r.y, r.width, r.height, 10, 10));
            g2.setStroke(new BasicStroke(1));
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("SansSerif", Font.BOLD, 13));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(SIDEBAR_LABELS[i], r.x + (r.width - fm.stringWidth(SIDEBAR_LABELS[i])) / 2, r.y + 25);
        }
    }

    // Cột giữa: Danh Hiệu (xếp theo tổng điểm thành tích đã nhận) + Thống Kê tổng quan, luôn hiện bất kể bộ lọc
    private void drawOverview(Graphics2D g2) {
        int x = 20, y = 360, w = 300;

        int points = GameProgress.totalAchievementPoints();
        String rank = AchievementCatalog.rankTitle(points);
        int floor = AchievementCatalog.currentRankFloor(points);
        int next = AchievementCatalog.nextRankThreshold(points);

        g2.setColor(new Color(20, 18, 28, 220));
        g2.fill(new RoundRectangle2D.Double(x, y, w, 108, 14, 14));
        g2.setColor(new Color(90, 70, 40));
        g2.setStroke(new BasicStroke(2f));
        g2.draw(new RoundRectangle2D.Double(x, y, w, 108, 14, 14));
        g2.setStroke(new BasicStroke(1));

        g2.setColor(new Color(255, 210, 90));
        g2.setFont(new Font("SansSerif", Font.BOLD, 16));
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(rank, x + (w - fm.stringWidth(rank)) / 2, y + 26);

        int barW = w - 32, barH = 12, barX = x + 16, barY = y + 40;
        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRoundRect(barX, barY, barW, barH, 7, 7);
        double pct = next > floor ? (double) (points - floor) / (next - floor) : 1.0;
        g2.setColor(new Color(90, 200, 120));
        g2.fillRoundRect(barX, barY, (int) (barW * Math.max(0, Math.min(1, pct))), barH, 7, 7);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
        String ptsText = points + " / " + next + " điểm";
        fm = g2.getFontMetrics();
        g2.drawString(ptsText, x + (w - fm.stringWidth(ptsText)) / 2, barY + barH + 15);

        g2.setColor(new Color(200, 200, 200));
        g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
        String claimedText = GameProgress.claimedAchievementCount() + "/" + AchievementCatalog.COUNT + " thành tích đã nhận";
        fm = g2.getFontMetrics();
        g2.drawString(claimedText, x + (w - fm.stringWidth(claimedText)) / 2, y + 96);

        // Thống Kê
        int sy = y + 124;
        g2.setColor(new Color(20, 18, 28, 220));
        g2.fill(new RoundRectangle2D.Double(x, sy, w, 150, 14, 14));
        g2.setColor(new Color(90, 70, 40));
        g2.setStroke(new BasicStroke(2f));
        g2.draw(new RoundRectangle2D.Double(x, sy, w, 150, 14, 14));
        g2.setStroke(new BasicStroke(1));

        g2.setColor(new Color(230, 190, 90));
        g2.setFont(new Font("SansSerif", Font.BOLD, 13));
        g2.drawString("Thống Kê", x + 16, sy + 22);

        String[] labels = {"Chương đã vượt", "Màn đã hoàn thành", "Quái vật tiêu diệt", "Đợt đã sống sót"};
        String[] values = {
                GameProgress.chaptersFullyCompletedCount() + "/" + GameProgress.CHAPTER_COUNT,
                GameProgress.totalLevelsCompleted() + "/" + (GameProgress.CHAPTER_COUNT * GameProgress.LEVELS_PER_CHAPTER),
                String.valueOf(GameProgress.getTotalEnemiesKilled()),
                String.valueOf(GameProgress.getTotalWavesSurvived())
        };
        for (int i = 0; i < labels.length; i++) {
            int ly = sy + 46 + i * 26;
            g2.setColor(new Color(190, 190, 190));
            g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
            g2.drawString(labels[i], x + 16, ly);
            g2.setColor(new Color(255, 230, 150));
            g2.setFont(new Font("SansSerif", Font.BOLD, 12));
            FontMetrics fmv = g2.getFontMetrics();
            g2.drawString(values[i], x + w - 16 - fmv.stringWidth(values[i]), ly);
        }
    }

    private void drawStatusTabs(Graphics2D g2) {
        for (int i = 0; i < statusTabRects.length; i++) {
            Rectangle r = statusTabRects[i];
            boolean active = (i == statusFilter);
            g2.setColor(active ? new Color(90, 70, 40) : new Color(30, 28, 38));
            g2.fill(new RoundRectangle2D.Double(r.x, r.y, r.width, r.height, 10, 10));
            g2.setColor(active ? new Color(230, 190, 90) : new Color(70, 70, 80));
            g2.setStroke(new BasicStroke(active ? 2.5f : 1.5f));
            g2.draw(new RoundRectangle2D.Double(r.x, r.y, r.width, r.height, 10, 10));
            g2.setStroke(new BasicStroke(1));
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("SansSerif", Font.BOLD, 12));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(STATUS_LABELS[i], r.x + (r.width - fm.stringWidth(STATUS_LABELS[i])) / 2, r.y + 23);
        }
    }

    private void drawAchievementList(Graphics2D g2) {
        List<Integer> list = filteredAchievements();
        int start = page * CARDS_PER_PAGE;

        for (int i = 0; i < CARDS_PER_PAGE; i++) {
            claimBtnRects[i] = null;
            int idx = start + i;
            if (idx >= list.size()) continue;
            drawAchievementCard(g2, cardRects[i], list.get(idx), i);
        }

        if (list.isEmpty()) {
            g2.setColor(new Color(180, 180, 180));
            g2.setFont(new Font("SansSerif", Font.PLAIN, 14));
            String empty = "Không có thành tích nào trong mục này.";
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(empty, listX + (listW - fm.stringWidth(empty)) / 2, listY + 40);
        }

        // Thanh chuyển trang
        int maxPage = Math.max(0, (list.size() - 1) / CARDS_PER_PAGE);
        boolean canPrev = page > 0, canNext = page < maxPage;
        drawPagerButton(g2, prevPageBtn, "\u2190", canPrev);
        drawPagerButton(g2, nextPageBtn, "\u2192", canNext);

        g2.setColor(new Color(190, 190, 190));
        g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
        String pageText = "Trang " + (page + 1) + "/" + (maxPage + 1);
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(pageText, listX + (listW - fm.stringWidth(pageText)) / 2, prevPageBtn.y + 21);
    }

    private void drawPagerButton(Graphics2D g2, Rectangle r, String label, boolean enabled) {
        g2.setColor(enabled ? new Color(70, 70, 80) : new Color(35, 35, 40));
        g2.fill(new RoundRectangle2D.Double(r.x, r.y, r.width, r.height, 8, 8));
        g2.setColor(enabled ? new Color(230, 190, 90) : new Color(70, 70, 75));
        g2.setStroke(new BasicStroke(1.5f));
        g2.draw(new RoundRectangle2D.Double(r.x, r.y, r.width, r.height, 8, 8));
        g2.setStroke(new BasicStroke(1));
        g2.setColor(enabled ? Color.WHITE : new Color(110, 110, 110));
        g2.setFont(new Font("SansSerif", Font.BOLD, 15));
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(label, r.x + (r.width - fm.stringWidth(label)) / 2, r.y + 22);
    }

    private void drawAchievementCard(Graphics2D g2, Rectangle r, int id, int slotIndex) {
        boolean claimed = GameProgress.isAchievementClaimed(id);
        int progress = AchievementCatalog.getProgress(id);
        int target = AchievementCatalog.TARGET[id];
        boolean completed = progress >= target;
        boolean readyToClaim = completed && !claimed;

        Color borderColor = readyToClaim ? new Color(255, 210, 60) : new Color(80, 80, 90);
        g2.setColor(readyToClaim ? new Color(55, 48, 20, 235) : new Color(22, 20, 30, 220));
        g2.fill(new RoundRectangle2D.Double(r.x, r.y, r.width, r.height, 12, 12));
        g2.setColor(borderColor);
        g2.setStroke(new BasicStroke(readyToClaim ? 2.5f : 1.5f));
        g2.draw(new RoundRectangle2D.Double(r.x, r.y, r.width, r.height, 12, 12));
        g2.setStroke(new BasicStroke(1));

        drawCategoryIcon(g2, r.x + 32, r.y + r.height / 2, AchievementCatalog.CATEGORY[id]);

        int textX = r.x + 60;
        g2.setColor(readyToClaim ? new Color(255, 220, 110) : Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 13));
        g2.drawString(AchievementCatalog.NAMES[id], textX, r.y + 19);

        g2.setColor(new Color(180, 180, 180));
        g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
        g2.drawString(AchievementCatalog.DESCS[id], textX, r.y + 33);

        int barW = 250, barH = 8, barY = r.y + 43;
        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRoundRect(textX, barY, barW, barH, 5, 5);
        double pct = Math.max(0, Math.min(1.0, (double) progress / target));
        g2.setColor(completed ? new Color(90, 200, 120) : new Color(90, 150, 210));
        int fillW = (int) (barW * pct);
        if (fillW > 0) g2.fillRoundRect(textX, barY, fillW, barH, 5, 5);
        g2.setColor(new Color(210, 210, 210));
        g2.setFont(new Font("SansSerif", Font.PLAIN, 9));
        String progText = progress + "/" + target;
        g2.drawString(progText, textX + barW + 6, barY + 8);

        // Cột thưởng + nút hành động, canh sát mép phải thẻ
        int rewardX = r.x + r.width - 140;
        g2.setColor(new Color(120, 200, 255));
        g2.setFont(new Font("SansSerif", Font.BOLD, 12));
        String rewardText = "\u2666 x" + AchievementCatalog.REWARD_GEMS[id];
        g2.drawString(rewardText, rewardX, r.y + 22);

        Rectangle btn = new Rectangle(rewardX - 4, r.y + r.height - 30, 110, 24);
        String btnLabel;
        Color btnColor;
        if (claimed) {
            btnLabel = "Đã Nhận";
            btnColor = new Color(60, 60, 65);
        } else if (readyToClaim) {
            btnLabel = "Nhận";
            btnColor = new Color(210, 160, 50);
            claimBtnRects[slotIndex] = btn;
        } else {
            btnLabel = "Chưa Đạt";
            btnColor = new Color(55, 52, 65);
        }
        g2.setColor(btnColor);
        g2.fill(new RoundRectangle2D.Double(btn.x, btn.y, btn.width, btn.height, 8, 8));
        if (readyToClaim) {
            g2.setColor(new Color(255, 230, 150));
            g2.setStroke(new BasicStroke(1.5f));
            g2.draw(new RoundRectangle2D.Double(btn.x, btn.y, btn.width, btn.height, 8, 8));
            g2.setStroke(new BasicStroke(1));
        }
        g2.setColor(readyToClaim ? new Color(30, 25, 10) : Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 11));
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(btnLabel, btn.x + (btn.width - fm.stringWidth(btnLabel)) / 2, btn.y + 16);
    }

    private void drawCategoryIcon(Graphics2D g2, int cx, int cy, int category) {
        switch (category) {
            case AchievementCatalog.CAT_MODE:
                g2.setColor(new Color(90, 140, 210));
                g2.fillRoundRect(cx - 14, cy - 14, 28, 22, 6, 6);
                Polygon tip = new Polygon();
                tip.addPoint(cx - 14, cy + 8);
                tip.addPoint(cx + 14, cy + 8);
                tip.addPoint(cx, cy + 18);
                g2.fillPolygon(tip);
                break;
            case AchievementCatalog.CAT_COMBAT:
                g2.setColor(new Color(210, 90, 90));
                g2.setStroke(new BasicStroke(3f));
                g2.drawLine(cx - 12, cy - 12, cx + 12, cy + 12);
                g2.drawLine(cx - 12, cy + 12, cx + 12, cy - 12);
                g2.setStroke(new BasicStroke(1));
                break;
            case AchievementCatalog.CAT_DEFENSE:
                g2.setColor(new Color(160, 160, 170));
                g2.fillRect(cx - 14, cy - 4, 28, 16);
                for (int i = -1; i <= 1; i++) g2.fillRect(cx - 14 + (i + 1) * 9 - 3, cy - 12, 6, 8);
                break;
            default:
                g2.setColor(new Color(230, 190, 90));
                g2.fillPolygon(new int[]{cx, cx - 5, cx - 16, cx - 7, cx - 10, cx, cx + 10, cx + 7, cx + 16, cx + 5},
                        new int[]{cy - 16, cy - 4, cy - 4, cy + 4, cy + 16, cy + 8, cy + 16, cy + 4, cy - 4, cy - 4}, 10);
        }
    }
}