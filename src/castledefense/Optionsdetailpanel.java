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
 * Nội dung chi tiết của 1 danh mục Tuỳ Chọn (Cài Đặt Chung/Đồ Hoạ/Âm Thanh/Điều Khiển/Ngôn Ngữ).
 * Dùng chung 1 class cho cả 5 danh mục vì phần lớn chỉ là các hàng toggle/slider đơn giản,
 * gắn thẳng vào GameSettings - đổi là có tác dụng ngay trong ván chơi tiếp theo.
 */
public class Optionsdetailpanel extends JPanel {
    public static final int WIDTH = GamePanel.WIDTH;
    public static final int HEIGHT = GamePanel.HEIGHT;

    private static final String[] TITLES = {"CÀI ĐẶT CHUNG", "ĐỒ HOẠ", "ÂM THANH", "ĐIỀU KHIỂN", "NGÔN NGỮ"};

    private final int category;
    private final Runnable onBack;
    private final Runnable onOpenKeyBind;

    private final Rectangle backBtn = new Rectangle(20, 20, 56, 44);
    private final Rectangle panelRect = new Rectangle((WIDTH - 600) / 2, 110, 600, 480);

    // Vùng tương tác, tính lại mỗi lần vẽ (giống cách sellButtonRect hoạt động bên GamePanel)
    private Rectangle hintToggleRect, autoWaveToggleRect, resetGeneralBtnRect;
    private Rectangle effectsToggleRect;
    private Rectangle musicSliderRect, sfxSliderRect, soundToggleRect, testSfxBtnRect;
    private Rectangle keyBindBtnRect, resetKeysBtnRect;

    private boolean draggingMusic = false, draggingSfx = false;

    public Optionsdetailpanel(int category, Runnable onBack, Runnable onOpenKeyBind) {
        this.category = category;
        this.onBack = onBack;
        this.onOpenKeyBind = onOpenKeyBind;
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setFocusable(true);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                handlePress(e.getX(), e.getY());
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                draggingMusic = false;
                draggingSfx = false;
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                handleClick(e.getX(), e.getY());
            }
        });
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                handleDrag(e.getX());
            }
        });
    }

    private void handlePress(int mx, int my) {
        if (musicSliderRect != null && musicSliderRect.contains(mx, my)) {
            draggingMusic = true;
            updateMusicFromX(mx);
        } else if (sfxSliderRect != null && sfxSliderRect.contains(mx, my)) {
            draggingSfx = true;
            updateSfxFromX(mx);
        }
    }

    private void handleDrag(int mx) {
        if (draggingMusic) updateMusicFromX(mx);
        if (draggingSfx) updateSfxFromX(mx);
    }

    private void updateMusicFromX(int mx) {
        int pct = (int) Math.round(100.0 * (mx - musicSliderRect.x) / musicSliderRect.width);
        Gamesettings.musicVolume = Math.max(0, Math.min(100, pct));
        repaint();
    }

    private void updateSfxFromX(int mx) {
        int pct = (int) Math.round(100.0 * (mx - sfxSliderRect.x) / sfxSliderRect.width);
        Gamesettings.sfxVolume = Math.max(0, Math.min(100, pct));
        repaint();
    }

    private void handleClick(int mx, int my) {
        if (backBtn.contains(mx, my)) {
            AudioEngine.playClick();
            onBack.run();
            return;
        }

        switch (category) {
            case OptionsPanel.CATEGORY_GENERAL:
                if (contains(hintToggleRect, mx, my)) {
                    Gamesettings.showHints = !Gamesettings.showHints;
                    AudioEngine.playClick();
                } else if (contains(autoWaveToggleRect, mx, my)) {
                    Gamesettings.autoSkipWaveDefault = !Gamesettings.autoSkipWaveDefault;
                    AudioEngine.playClick();
                } else if (contains(resetGeneralBtnRect, mx, my)) {
                    Gamesettings.showHints = true;
                    Gamesettings.autoSkipWaveDefault = true;
                    AudioEngine.playClick();
                }
                break;
            case OptionsPanel.CATEGORY_GRAPHICS:
                if (contains(effectsToggleRect, mx, my)) {
                    Gamesettings.effectsEnabled = !Gamesettings.effectsEnabled;
                    AudioEngine.playClick();
                }
                break;
            case OptionsPanel.CATEGORY_AUDIO:
                if (contains(soundToggleRect, mx, my)) {
                    Gamesettings.soundEnabled = !Gamesettings.soundEnabled;
                    AudioEngine.playClick();
                } else if (contains(testSfxBtnRect, mx, my)) {
                    AudioEngine.playClick();
                }
                break;
            case OptionsPanel.CATEGORY_CONTROLS:
                if (contains(keyBindBtnRect, mx, my)) {
                    AudioEngine.playClick();
                    onOpenKeyBind.run();
                } else if (contains(resetKeysBtnRect, mx, my)) {
                    Gamesettings.resetKeysToDefault();
                    AudioEngine.playClick();
                }
                break;
            default:
                break; // Ngôn Ngữ chưa có control tương tác
        }
        repaint();
    }

    private boolean contains(Rectangle r, int x, int y) {
        return r != null && r.contains(x, y);
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

        g2.setColor(new Color(20, 18, 28, 210));
        g2.fill(new RoundRectangle2D.Double(panelRect.x, panelRect.y, panelRect.width, panelRect.height, 16, 16));
        g2.setColor(new Color(90, 70, 40));
        g2.setStroke(new BasicStroke(2f));
        g2.draw(new RoundRectangle2D.Double(panelRect.x, panelRect.y, panelRect.width, panelRect.height, 16, 16));
        g2.setStroke(new BasicStroke(1));

        switch (category) {
            case OptionsPanel.CATEGORY_GENERAL: drawGeneral(g2); break;
            case OptionsPanel.CATEGORY_GRAPHICS: drawGraphics(g2); break;
            case OptionsPanel.CATEGORY_AUDIO: drawAudio(g2); break;
            case OptionsPanel.CATEGORY_CONTROLS: drawControls(g2); break;
            default: drawLanguage(g2);
        }
    }

    private void drawGeneral(Graphics2D g2) {
        int x = panelRect.x + 40, y = panelRect.y + 50, w = panelRect.width - 80;
        hintToggleRect = drawToggleRow(g2, x, y, w, "Hiện gợi ý khi chơi", Gamesettings.showHints);
        y += 60;
        autoWaveToggleRect = drawToggleRow(g2, x, y, w, "Tự động qua đợt (mặc định)", Gamesettings.autoSkipWaveDefault);
        y += 50;
        g2.setColor(new Color(180, 180, 180));
        g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
        wrapAndDraw(g2, "Áp dụng cho các ván chơi bắt đầu sau khi thay đổi, không ảnh hưởng ván đang chơi dở.", x, y, w);
        y += 60;
        resetGeneralBtnRect = drawActionButton(g2, x, y, 220, 44, "Đặt Lại Mặc Định", new Color(140, 40, 40));
    }

    private void drawGraphics(Graphics2D g2) {
        int x = panelRect.x + 40, y = panelRect.y + 50, w = panelRect.width - 80;
        effectsToggleRect = drawToggleRow(g2, x, y, w, "Hiệu Ứng Kỹ Năng (Lửa/Sét/Băng)", Gamesettings.effectsEnabled);
        y += 55;
        g2.setColor(new Color(180, 180, 180));
        g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
        wrapAndDraw(g2, "Tắt bớt hiệu ứng phủ màn hình khi dùng kỹ năng đặc biệt, giúp đỡ chớp và nhẹ máy hơn. "
                + "Sát thương của kỹ năng không đổi, chỉ ẩn phần hình ảnh.", x, y, w);
    }

    private void drawAudio(Graphics2D g2) {
        int x = panelRect.x + 40, y = panelRect.y + 50, w = panelRect.width - 80;
        musicSliderRect = drawSliderRow(g2, x, y, w, "Nhạc Nền", Gamesettings.musicVolume);
        y += 60;
        sfxSliderRect = drawSliderRow(g2, x, y, w, "Hiệu Ứng Âm Thanh", Gamesettings.sfxVolume);
        y += 55;
        testSfxBtnRect = drawActionButton(g2, x, y, 160, 40, "Nghe Thử", new Color(70, 110, 180));
        y += 70;
        soundToggleRect = drawToggleRow(g2, x, y, w, "Âm Thanh", Gamesettings.soundEnabled);
    }

    private void drawControls(Graphics2D g2) {
        int x = panelRect.x + 40, y = panelRect.y + 50, w = panelRect.width - 80;
        g2.setColor(new Color(210, 210, 210));
        g2.setFont(new Font("SansSerif", Font.PLAIN, 13));
        wrapAndDraw(g2, "Đổi phím tắt chọn loại tháp, kích hoạt 3 kỹ năng đặc biệt, và bán tháp đang chọn trong lúc chơi.", x, y, w);
        y += 55;
        keyBindBtnRect = drawActionButton(g2, x, y, 260, 50, "TÙY CHỈNH PHÍM", new Color(200, 120, 40));
        y += 70;
        resetKeysBtnRect = drawActionButton(g2, x, y, 230, 44, "Đặt Lại Phím Mặc Định", new Color(90, 90, 100));
    }

    private void drawLanguage(Graphics2D g2) {
        int x = panelRect.x + 40, y = panelRect.y + 50, w = panelRect.width - 80;
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 15));
        g2.drawString("Ngôn ngữ hiện tại:", x, y);

        int boxY = y + 18, boxW = 200, boxH = 40;
        g2.setColor(new Color(50, 48, 60));
        g2.fill(new RoundRectangle2D.Double(x, boxY, boxW, boxH, 10, 10));
        g2.setColor(new Color(230, 190, 90));
        g2.setStroke(new BasicStroke(1.5f));
        g2.draw(new RoundRectangle2D.Double(x, boxY, boxW, boxH, 10, 10));
        g2.setStroke(new BasicStroke(1));
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 14));
        String lang = "Tiếng Việt";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(lang, x + (boxW - fm.stringWidth(lang)) / 2, boxY + 26);

        g2.setColor(new Color(180, 180, 180));
        g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
        wrapAndDraw(g2, "Game hiện chỉ hỗ trợ Tiếng Việt. Các ngôn ngữ khác sẽ được bổ sung trong bản cập nhật sau.", x, boxY + 70, w);
    }

    // --- Các widget dùng chung: toggle BẬT/TẮT, thanh trượt %, nút hành động ---

    private Rectangle drawToggleRow(Graphics2D g2, int x, int y, int w, String label, boolean value) {
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 15));
        g2.drawString(label, x, y + 22);

        int toggleW = 90, toggleH = 32;
        Rectangle r = new Rectangle(x + w - toggleW, y, toggleW, toggleH);
        g2.setColor(value ? new Color(80, 170, 90) : new Color(70, 70, 78));
        g2.fill(new RoundRectangle2D.Double(r.x, r.y, r.width, r.height, 16, 16));
        g2.setColor(value ? new Color(230, 255, 210) : new Color(180, 180, 180));
        g2.setStroke(new BasicStroke(1.5f));
        g2.draw(new RoundRectangle2D.Double(r.x, r.y, r.width, r.height, 16, 16));
        g2.setStroke(new BasicStroke(1));
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 13));
        String txt = value ? "BẬT" : "TẮT";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(txt, r.x + (r.width - fm.stringWidth(txt)) / 2, r.y + 21);
        return r;
    }

    private Rectangle drawSliderRow(Graphics2D g2, int x, int y, int w, String label, int valuePercent) {
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 15));
        g2.drawString(label, x, y + 22);
        g2.setColor(new Color(255, 230, 150));
        g2.setFont(new Font("SansSerif", Font.PLAIN, 13));
        String pctText = valuePercent + "%";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(pctText, x + w - fm.stringWidth(pctText), y + 22);

        int sliderY = y + 32, sliderH = 10;
        Rectangle r = new Rectangle(x, sliderY, w, sliderH);
        g2.setColor(new Color(50, 48, 60));
        g2.fillRoundRect(r.x, r.y, r.width, r.height, 6, 6);
        g2.setColor(new Color(230, 190, 90));
        int fillW = (int) (r.width * (valuePercent / 100.0));
        if (fillW > 0) g2.fillRoundRect(r.x, r.y, fillW, r.height, 6, 6);

        int handleX = r.x + fillW;
        g2.setColor(Color.WHITE);
        g2.fillOval(handleX - 7, r.y - 3, 16, 16);
        g2.setColor(new Color(120, 100, 60));
        g2.drawOval(handleX - 7, r.y - 3, 16, 16);
        return r;
    }

    private Rectangle drawActionButton(Graphics2D g2, int x, int y, int w, int h, String label, Color color) {
        Rectangle r = new Rectangle(x, y, w, h);
        g2.setColor(color);
        g2.fill(new RoundRectangle2D.Double(r.x, r.y, r.width, r.height, 10, 10));
        g2.setColor(new Color(230, 190, 90));
        g2.setStroke(new BasicStroke(1.5f));
        g2.draw(new RoundRectangle2D.Double(r.x, r.y, r.width, r.height, 10, 10));
        g2.setStroke(new BasicStroke(1));
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 14));
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(label, r.x + (r.width - fm.stringWidth(label)) / 2, r.y + h / 2 + 5);
        return r;
    }

    private void wrapAndDraw(Graphics2D g2, String text, int x, int y, int maxWidth) {
        FontMetrics fm = g2.getFontMetrics();
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();
        int curY = y;
        for (String word : words) {
            String test = line.length() == 0 ? word : line + " " + word;
            if (fm.stringWidth(test) > maxWidth && line.length() > 0) {
                g2.drawString(line.toString(), x, curY);
                line = new StringBuilder(word);
                curY += 16;
            } else {
                line = new StringBuilder(test);
            }
        }
        if (line.length() > 0) g2.drawString(line.toString(), x, curY);
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
        g2.setFont(new Font("SansSerif", Font.BOLD, 22));
        String title = TITLES[category];
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
}