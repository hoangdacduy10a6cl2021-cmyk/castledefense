/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package castledefense;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;

/**
 * Lớp Tower đại diện cho trụ phòng thủ do người chơi đặt xuống.
 * Mỗi loại (type) có hình dáng vẽ riêng, kèm nhân vật vận hành, để dễ phân biệt.
 *
 * type = 0: Cung Thủ   | 1: Pháp Sư | 2: Đại Bác
 *        3: Nỏ Liên Hoàn (xuyên/lan sang địch gần) | 4: Phù Thủy Độc (gây độc theo thời gian)
 *        5: Tháp Sét (sét lan sang địch gần mục tiêu) | 6: Trại Lính (triệu hồi lính cận chiến)
 */
public class Tower {
    int x, y;       // vị trí trung tâm trụ
    int range;      // tầm bắn
    int damage;     // sát thương mỗi lần bắn
    int fireRate;   // số khung hình giữa mỗi lần bắn (nhỏ = bắn nhanh)
    int cooldown = 0;
    int cost;
    int type;
    Color color;

    // Bán kính coi là "đang trỏ vào trụ" để hiện vòng tầm bắn + bảng thông số
    static final int HOVER_RADIUS = 26;

    double facingAngle = 0;
    int muzzleFlash = 0; // đếm ngược hiệu ứng lóe sáng khi bắn
    private double idlePhase = Math.random() * Math.PI * 2; // nhịp riêng cho cờ phất/lửa nhấp nháy khi rảnh

    public Tower(int x, int y, int range, int damage, int fireRate, int cost, int type, Color color) {
        this.x = x;
        this.y = y;
        this.range = range;
        this.damage = damage;
        this.fireRate = fireRate;
        this.cost = cost;
        this.type = type;
        this.color = color;
    }

    public void update() {
        if (cooldown > 0) cooldown--;
        if (muzzleFlash > 0) muzzleFlash--;
        idlePhase += 0.07;
    }

    public boolean canFire() {
        return cooldown <= 0;
    }

    public void fire() {
        cooldown = fireRate;
        muzzleFlash = 6;
    }

    public double distanceTo(Enemy e) {
        double dx = e.x - x;
        double dy = e.y - y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    public void aimAt(Enemy e) {
        if (e == null) return;
        facingAngle = Math.atan2(e.y - y, e.x - x) + Math.PI / 2;
    }

    // Chuột có đang ở gần trụ đủ để coi là "đang trỏ vào" hay không
    public boolean isHovered(int mx, int my) {
        double dx = mx - x, dy = my - y;
        return Math.sqrt(dx * dx + dy * dy) <= HOVER_RADIUS;
    }

    // Tên hiển thị + màu đạn đặc trưng cho từng loại trụ, dùng chung cho tooltip và đạn
    public String typeName() {
        switch (type) {
            case 1: return "Pháp Sư";
            case 2: return "Đại Bác";
            case 3: return "Nỏ Liên Hoàn";
            case 4: return "Phù Thủy Độc";
            case 5: return "Tháp Sét";
            case 6: return "Trại Lính";
            default: return "Cung Thủ";
        }
    }

    public Color projectileColor() {
        switch (type) {
            case 1: return new Color(90, 230, 255);   // đạn phép - xanh dương/cyan phát sáng
            case 2: return new Color(90, 60, 40);     // đạn đại bác - viên đá/sắt tối màu
            case 3: return new Color(210, 200, 180);  // mũi nỏ - sáng bạc
            case 4: return new Color(110, 200, 80);   // lọ độc - xanh lá
            case 5: return new Color(190, 225, 255);  // tia điện - trắng xanh
            case 6: return new Color(150, 120, 90);   // không bắn đạn, chỉ dùng cho đồng bộ dữ liệu
            default: return new Color(255, 200, 60);  // đạn cung thủ - mũi tên vàng cam
        }
    }

    // Ghi chú hiệu ứng đặc biệt của từng loại trụ, hiện thêm trong tooltip
    public String effectNote() {
        switch (type) {
            case 3: return "Xuyên lan sang địch gần";
            case 4: return "Gây độc theo thời gian";
            case 5: return "Sét lan sang địch gần";
            case 6: return "Triệu hồi lính cận chiến";
            default: return null;
        }
    }

    // Chỉ vẽ vòng tầm bắn (AOE) khi đang hover, gọi riêng trước khi vẽ thân trụ
    public void drawRangeCircle(Graphics2D g) {
        g.setColor(new Color(255, 255, 255, 35));
        g.fill(new Ellipse2D.Double(x - range, y - range, range * 2, range * 2));
        g.setColor(new Color(255, 255, 255, 130));
        g.setStroke(new BasicStroke(1.5f));
        g.draw(new Ellipse2D.Double(x - range, y - range, range * 2, range * 2));
        g.setStroke(new BasicStroke(1));
    }

    // Bảng thông số nhỏ hiện phía trên trụ khi hover: tên, sát thương, tầm bắn, tốc độ bắn
    public void drawTooltip(Graphics2D g) {
        String title = typeName();
        String l1 = type == 6 ? "Sát thương lính: " + damage : "Sát thương: " + damage;
        String l2 = type == 6 ? "Tầm tuần tra: " + range : "Tầm bắn: " + range;
        String l3 = type == 6
                ? "Triệu hồi mỗi: " + String.format("%.1f", fireRate / 62.0) + "s"
                : "Tốc độ bắn: " + String.format("%.1f", 60.0 / fireRate) + "/s";
        String l4 = effectNote();

        g.setFont(new Font("SansSerif", Font.BOLD, 13));
        FontMetrics fmTitle = g.getFontMetrics();
        g.setFont(new Font("SansSerif", Font.PLAIN, 12));
        FontMetrics fm = g.getFontMetrics();

        int w = Math.max(fmTitle.stringWidth(title), Math.max(fm.stringWidth(l1),
                Math.max(fm.stringWidth(l2), Math.max(fm.stringWidth(l3), l4 != null ? fm.stringWidth(l4) : 0)))) + 24;
        int h = l4 != null ? 94 : 78;
        int boxX = x - w / 2;
        int boxY = y - 62 - h;

        g.setColor(new Color(15, 20, 15, 225));
        g.fill(new RoundRectangle2D.Double(boxX, boxY, w, h, 10, 10));
        g.setColor(color);
        g.setStroke(new BasicStroke(2f));
        g.draw(new RoundRectangle2D.Double(boxX, boxY, w, h, 10, 10));
        g.setStroke(new BasicStroke(1));

        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 13));
        g.drawString(title, boxX + 12, boxY + 18);

        g.setColor(new Color(220, 220, 220));
        g.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g.drawString(l1, boxX + 12, boxY + 36);
        g.drawString(l2, boxX + 12, boxY + 52);
        g.drawString(l3, boxX + 12, boxY + 68);
        if (l4 != null) {
            g.setColor(new Color(150, 230, 170));
            g.drawString(l4, boxX + 12, boxY + 84);
        }

        // Mũi chỏ nhỏ nối bảng xuống trụ cho dễ nhìn
        Polygon arrow = new Polygon();
        arrow.addPoint(x - 6, boxY + h);
        arrow.addPoint(x + 6, boxY + h);
        arrow.addPoint(x, boxY + h + 8);
        g.setColor(new Color(15, 20, 15, 225));
        g.fillPolygon(arrow);
    }

    public void draw(Graphics2D g) {
        // Bóng đổ dưới chân trụ
        g.setColor(new Color(0, 0, 0, 70));
        g.fill(new Ellipse2D.Double(x - 20, y + 14, 40, 12));

        if (TowerUpgradeData.isElite(type)) {
            drawEliteAura(g);
        }

        switch (type) {
            case 1: drawMageTower(g); break;
            case 2: drawCannonTower(g); break;
            case 3: drawBallistaTower(g); break;
            case 4: drawPoisonTower(g); break;
            case 5: drawTeslaTower(g); break;
            case 6: drawBarracksTower(g); break;
            default: drawArcherTower(g);
        }
    }

    // Trụ "cân bằng" - chòi gỗ/đá với 2 cung thủ đứng trên đỉnh, quay bắn theo mục tiêu
    private void drawArcherTower(Graphics2D g) {
        GradientPaint base = new GradientPaint(x - 18, y, new Color(150, 150, 150),
                x + 18, y + 20, new Color(90, 90, 95));
        g.setPaint(base);
        g.fillRoundRect(x - 18, y - 5, 36, 25, 8, 8);
        g.setColor(new Color(60, 60, 65));
        g.drawRoundRect(x - 18, y - 5, 36, 25, 8, 8);

        GradientPaint tower = new GradientPaint(x - 13, y - 34, new Color(190, 170, 140),
                x + 13, y - 5, new Color(140, 120, 95));
        g.setPaint(tower);
        g.fillRoundRect(x - 13, y - 34, 26, 32, 6, 6);
        g.setColor(new Color(90, 75, 55));
        g.drawRoundRect(x - 13, y - 34, 26, 32, 6, 6);

        // Lan can răng cưa trên đỉnh tháp - bệ đứng của cung thủ
        g.setColor(new Color(170, 150, 120));
        for (int i = -1; i <= 1; i++) {
            g.fillRect(x - 13 + (i + 1) * 9 - 3, y - 40, 6, 8);
        }

        // 2 cung thủ đứng trên đỉnh tháp, kéo cung khi bắn
        drawArcherFigure(g, x - 7, y - 40);
        drawArcherFigure(g, x + 7, y - 40);

        g.setColor(new Color(220, 60, 60));
        drawWavingFlag(g, x, y - 60, 12, 8);
        g.setColor(Color.DARK_GRAY);
        g.drawLine(x, y - 60, x, y - 46);
    }

    // Lá cờ nhỏ phất theo gió bằng đa giác lượn sóng dựa trên idlePhase, dùng chung cho các tháp có cờ
    private void drawWavingFlag(Graphics2D g, int poleX, int topY, int w, int h) {
        double wave1 = Math.sin(idlePhase) * 3;
        double wave2 = Math.sin(idlePhase + 1.4) * 3;
        Polygon flag = new Polygon();
        flag.addPoint(poleX, topY);
        flag.addPoint(poleX + w + (int) wave1, topY + h / 3 + (int) (wave1 * 0.3));
        flag.addPoint(poleX + w + (int) wave2, topY + h);
        flag.addPoint(poleX, topY + h);
        g.fillPolygon(flag);
    }

    // Bong bóng nhỏ nổi lên rồi tan biến theo chu kỳ phase, dùng cho nồi độc lúc rảnh (chưa bắn)
    private void drawRisingBubble(Graphics2D g, int baseX, int baseY, double phase) {
        double cycle = (phase % (Math.PI * 2)) / (Math.PI * 2); // 0..1
        int riseY = (int) (cycle * 14);
        int alpha = (int) (120 * (1 - cycle));
        int size = 3 + (int) (cycle * 3);
        g.setColor(new Color(120, 220, 90, Math.max(0, alpha)));
        g.fillOval(baseX - size / 2, baseY - riseY, size, size);
    }

    // Một cung thủ nhỏ đứng trên đỉnh tháp, quay theo facingAngle, kéo dây cung khi vừa bắn
    private void drawArcherFigure(Graphics2D g, int px, int py) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.translate(px, py);
        g2.rotate(facingAngle);

        g2.setColor(new Color(70, 60, 50));
        g2.fillRoundRect(-3, -9, 6, 9, 2, 2);
        g2.setColor(new Color(210, 175, 140));
        g2.fillOval(-3, -14, 6, 6);

        boolean drawing = muzzleFlash > 0;
        g2.setColor(new Color(120, 85, 50));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawArc(2, -13, 9, 14, -55, 110);
        g2.setColor(new Color(235, 235, 225));
        if (drawing) {
            g2.drawLine(6, -13, 1, -6);
            g2.drawLine(6, 1, 1, -6);
        } else {
            g2.drawLine(6, -13, 6, 1);
        }
        g2.setStroke(new BasicStroke(1));
        g2.dispose();
    }

    // Trụ "bắn nhanh" - tháp pha lê xanh với 1 pháp sư đứng tung phép
    private void drawMageTower(Graphics2D g) {
        g.setColor(new Color(80, 80, 90));
        g.fillRoundRect(x - 17, y - 2, 34, 22, 8, 8);
        g.setColor(new Color(50, 50, 60));
        g.drawRoundRect(x - 17, y - 2, 34, 22, 8, 8);

        int idleShimmer = (int) (Math.sin(idlePhase) * 15);
        int glow = 120 + idleShimmer + (muzzleFlash > 0 ? 100 : 0);
        Color crystalColor = new Color(80, 220, Math.min(255, glow));
        Polygon crystal = new Polygon();
        crystal.addPoint(x, y - 45);
        crystal.addPoint(x - 12, y - 15);
        crystal.addPoint(x, y - 2);
        crystal.addPoint(x + 12, y - 15);
        g.setColor(crystalColor);
        g.fillPolygon(crystal);
        g.setColor(new Color(20, 100, 120));
        g.drawPolygon(crystal);

        drawMageFigure(g);

        if (muzzleFlash > 0) {
            g.setColor(new Color(150, 255, 255, 150));
            g.fillOval(x - 6, y - 40, 12, 12);
        }
    }

    // Pháp sư đứng phía trước pha lê, giơ gậy phép sáng lên khi vừa bắn
    private void drawMageFigure(Graphics2D g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.translate(x, y + 8);
        g2.rotate(facingAngle);

        g2.setColor(new Color(60, 50, 90));
        Polygon robe = new Polygon();
        robe.addPoint(-6, 0);
        robe.addPoint(6, 0);
        robe.addPoint(4, -14);
        robe.addPoint(-4, -14);
        g2.fillPolygon(robe);
        g2.setColor(new Color(220, 190, 160));
        g2.fillOval(-4, -20, 8, 8);
        g2.setColor(new Color(45, 35, 70));
        Polygon hat = new Polygon();
        hat.addPoint(-5, -19);
        hat.addPoint(5, -19);
        hat.addPoint(0, -30);
        g2.fillPolygon(hat);

        boolean casting = muzzleFlash > 0;
        g2.setColor(new Color(110, 80, 60));
        g2.drawLine(7, -16, 7, casting ? -24 : -6);
        g2.setColor(casting ? new Color(170, 255, 255) : new Color(80, 200, 210));
        int orbY = casting ? -24 : -6;
        g2.fillOval(4, orbY - 3, 6, 6);
        g2.dispose();
    }

    // Trụ "sát thương cao" - bệ đại bác đá nặng, có pháo thủ đứng giật lùi khi khai hỏa
    private void drawCannonTower(Graphics2D g) {
        g.setColor(new Color(120, 100, 90));
        g.fillOval(x - 19, y - 8, 38, 30);
        g.setColor(new Color(70, 55, 50));
        g.drawOval(x - 19, y - 8, 38, 30);

        Graphics2D g2c = (Graphics2D) g.create();
        g2c.translate(x, y);
        g2c.rotate(facingAngle);
        int barrelLen = muzzleFlash > 0 ? 34 : 30;
        g2c.setColor(new Color(45, 45, 50));
        g2c.fillRoundRect(-6, -barrelLen, 12, barrelLen, 5, 5);
        g2c.setColor(Color.BLACK);
        g2c.drawRoundRect(-6, -barrelLen, 12, barrelLen, 5, 5);

        // Pháo thủ đứng cạnh nòng súng, ngả người ra sau lúc khai hỏa (giật lùi)
        int recoil = muzzleFlash > 0 ? 3 : 0;
        g2c.setColor(new Color(70, 55, 45));
        g2c.fillRoundRect(-17, -8 - recoil, 8, 13, 3, 3);
        g2c.setColor(new Color(210, 175, 140));
        g2c.fillOval(-16, -14 - recoil, 6, 6);
        g2c.setColor(new Color(90, 20, 20));
        g2c.fillRect(-17, -8 - recoil, 8, 3);
        g2c.dispose();

        if (muzzleFlash > 0) {
            g.setColor(new Color(255, 200, 60, 200));
            g.fillOval(x - 10, y - 40, 20, 20);
            g.setColor(new Color(200, 200, 200, 120));
            g.fillOval(x - 14, y - 46, 28, 20);
        }

        g.setColor(new Color(160, 60, 50));
        g.fillOval(x - 8, y - 3, 16, 16);
    }

    // Trụ mới: Nỏ Liên Hoàn - bắn nhanh, mũi tên xuyên/lan sang địch đứng gần, 2 lính vận hành
    private void drawBallistaTower(Graphics2D g) {
        g.setColor(new Color(110, 90, 70));
        g.fillRoundRect(x - 18, y - 6, 36, 26, 6, 6);
        g.setColor(new Color(60, 45, 35));
        g.drawRoundRect(x - 18, y - 6, 36, 26, 6, 6);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.translate(x, y - 6);
        g2.rotate(facingAngle);

        // Thân nỏ dọc
        g2.setColor(new Color(90, 65, 45));
        g2.fillRect(-3, -24, 6, 26);

        // Cánh nỏ ngang - căng ra khi bắn
        boolean drawn = muzzleFlash > 0;
        g2.setColor(new Color(70, 55, 40));
        g2.setStroke(new BasicStroke(3f));
        g2.drawLine(-20, -4, 20, -4);
        g2.setStroke(new BasicStroke(1.5f));
        g2.setColor(new Color(210, 200, 180));
        if (drawn) {
            g2.drawLine(-20, -4, 0, -12);
            g2.drawLine(20, -4, 0, -12);
        } else {
            g2.drawLine(-20, -4, 20, -4);
        }
        g2.setStroke(new BasicStroke(1));

        // 2 lính vận hành quay tay quay đứng 2 bên
        g2.setColor(new Color(70, 60, 50));
        g2.fillOval(-27, -8, 8, 10);
        g2.fillOval(19, -8, 8, 10);
        g2.setColor(new Color(210, 175, 140));
        g2.fillOval(-26, -13, 5, 5);
        g2.fillOval(21, -13, 5, 5);
        g2.dispose();
    }

    // Trụ mới: Phù Thủy Độc - đạn độc gây sát thương theo thời gian, 1 phù thủy khuấy nồi độc
    private void drawPoisonTower(Graphics2D g) {
        g.setColor(new Color(70, 90, 60));
        g.fillRoundRect(x - 17, y - 4, 34, 24, 8, 8);
        g.setColor(new Color(40, 55, 35));
        g.drawRoundRect(x - 17, y - 4, 34, 24, 8, 8);

        // Nồi độc sôi sục
        g.setColor(new Color(40, 35, 30));
        g.fillOval(x - 12, y - 18, 24, 16);
        g.setColor(muzzleFlash > 0 ? new Color(140, 235, 95) : new Color(90, 170, 70));
        g.fillOval(x - 9, y - 16, 18, 10);
        if (muzzleFlash > 0) {
            g.setColor(new Color(120, 220, 90, 160));
            g.fillOval(x - 7, y - 32, 14, 14);
        } else {
            // 2 bong bóng độc nhỏ nổi lên rồi tan liên tục, so le nhịp cho tự nhiên
            drawRisingBubble(g, x - 3, y - 12, idlePhase);
            drawRisingBubble(g, x + 3, y - 12, idlePhase + Math.PI);
        }

        // Phù thủy độc đứng cạnh nồi, khuấy đũa phép, quay theo mục tiêu
        Graphics2D g2 = (Graphics2D) g.create();
        g2.translate(x + 15, y - 4);
        g2.rotate(facingAngle);
        g2.setColor(new Color(55, 75, 50));
        Polygon robe = new Polygon();
        robe.addPoint(-5, 0);
        robe.addPoint(5, 0);
        robe.addPoint(3, -13);
        robe.addPoint(-3, -13);
        g2.fillPolygon(robe);
        g2.setColor(new Color(200, 210, 170));
        g2.fillOval(-3, -19, 6, 6);
        g2.setColor(new Color(90, 170, 70));
        g2.drawLine(6, -12, 6, 2);
        g2.dispose();
    }

    // Trụ mới: Tháp Sét - cột thu lôi phóng điện lan sang địch gần mục tiêu, 1 kỹ sư vận hành
    private void drawTeslaTower(Graphics2D g) {
        g.setColor(new Color(70, 70, 90));
        g.fillRoundRect(x - 17, y - 4, 34, 24, 8, 8);
        g.setColor(new Color(35, 35, 55));
        g.drawRoundRect(x - 17, y - 4, 34, 24, 8, 8);

        // Cột thu sét
        g.setColor(new Color(120, 120, 140));
        g.fillRect(x - 3, y - 34, 6, 30);
        int glow = muzzleFlash > 0 ? 255 : 140 + (int) (Math.sin(idlePhase * 1.5) * 20);
        g.setColor(new Color(180, 220, glow));
        g.fillOval(x - 6, y - 40, 12, 12);

        if (muzzleFlash > 0) {
            g.setColor(new Color(200, 235, 255));
            g.setStroke(new BasicStroke(2f));
            g.drawLine(x, y - 34, x - 11, y - 20);
            g.drawLine(x - 11, y - 20, x - 4, y - 16);
            g.drawLine(x, y - 34, x + 10, y - 22);
            g.drawLine(x + 10, y - 22, x + 3, y - 15);
            g.setStroke(new BasicStroke(1));
        }

        // Kỹ sư vận hành đứng cạnh bảng điều khiển
        Graphics2D g2 = (Graphics2D) g.create();
        g2.translate(x - 15, y);
        g2.rotate(facingAngle);
        g2.setColor(new Color(60, 60, 80));
        g2.fillRoundRect(-4, -12, 8, 12, 3, 3);
        g2.setColor(new Color(210, 175, 140));
        g2.fillOval(-3, -18, 6, 6);
        g2.setColor(new Color(150, 210, 255));
        g2.fillRect(-3, -3, 6, 2);
        g2.dispose();
    }

    // Vòng hào quang vàng + ngôi sao nhỏ phía trên, đánh dấu tháp đã nâng cấp tối đa (Tinh Nhuệ)
    private void drawEliteAura(Graphics2D g) {
        g.setColor(new Color(255, 215, 90, 90));
        g.setStroke(new BasicStroke(2.5f));
        g.drawOval(x - 30, y - 30, 60, 60);
        g.setStroke(new BasicStroke(1));
        g.setColor(new Color(255, 230, 150));
        g.setFont(new Font("SansSerif", Font.BOLD, 14));
        g.drawString("\u2605", x - 6, y - 34);
    }

    // Trụ mới: Trại Lính - lều trại có cờ hiệu, định kỳ triệu hồi 1 lính cận chiến ra tuần tra
    private void drawBarracksTower(Graphics2D g) {
        g.setColor(new Color(110, 90, 65));
        g.fillRoundRect(x - 19, y - 4, 38, 24, 6, 6);
        g.setColor(new Color(65, 50, 35));
        g.drawRoundRect(x - 19, y - 4, 38, 24, 6, 6);

        // Mái lều hình tam giác
        Polygon tent = new Polygon();
        tent.addPoint(x - 22, y - 2);
        tent.addPoint(x + 22, y - 2);
        tent.addPoint(x, y - 30);
        g.setColor(new Color(150, 60, 55));
        g.fillPolygon(tent);
        g.setColor(new Color(90, 30, 25));
        g.drawPolygon(tent);

        // Cửa lều
        g.setColor(new Color(40, 30, 25));
        g.fillRect(x - 6, y - 4, 12, 18);

        // Cờ hiệu trên đỉnh lều
        g.setColor(Color.DARK_GRAY);
        g.drawLine(x, y - 30, x, y - 44);
        g.setColor(new Color(90, 150, 70));
        drawWavingFlag(g, x, y - 44, 14, 9);

        // Hiệu ứng phát sáng khi vừa triệu hồi thêm 1 lính mới
        if (muzzleFlash > 0) {
            g.setColor(new Color(255, 240, 150, 160));
            g.fillOval(x - 12, y - 14, 24, 24);
        }

        // 2 ngọn giáo dựng cạnh lều cho có không khí doanh trại
        g.setColor(new Color(120, 100, 80));
        g.drawLine(x - 24, y + 14, x - 24, y - 10);
        g.drawLine(x + 24, y + 14, x + 24, y - 10);
        g.setColor(new Color(180, 180, 190));
        g.fillPolygon(new int[]{x - 26, x - 22, x - 24}, new int[]{y - 10, y - 10, y - 18}, 3);
        g.fillPolygon(new int[]{x + 22, x + 26, x + 24}, new int[]{y - 10, y - 10, y - 18}, 3);
    }
}