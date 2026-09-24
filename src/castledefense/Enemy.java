/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package castledefense;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.List;

/**
 * Lớp Enemy đại diện cho quái vật (yêu tinh) đi theo đường hướng về phía thành.
 */
public class Enemy {
    double x, y;
    double speed;
    double baseSpeed; // tốc độ gốc, dùng để khôi phục sau khi hết hiệu ứng làm chậm
    int hp, maxHp;
    int goldReward;
    int pathIndex = 0;
    List<Point> path;
    boolean reachedEnd = false;
    boolean dead = false;
    final boolean isBoss;
    final boolean isSuperBoss;

    // Màu da và độ lớn ngẫu nhiên nhẹ để đàn quái trông đa dạng hơn
    private final Color skinColor;
    private final int size;
    private double bobPhase; // tạo hiệu ứng nhún nhảy khi đi

    // --- Hiệu ứng trúng độc (từ Tháp Phù Thủy Độc hoặc kỹ năng Lửa) ---
    private int poisonDamagePerTick = 0;
    private int poisonTicksLeft = 0;
    private int poisonTickTimer = 0;
    private static final int POISON_TICK_INTERVAL = 15; // ~0.25s giữa mỗi lần trúng độc

    // --- Hiệu ứng bị làm chậm (từ Tháp Sét lan hoặc kỹ năng Băng) ---
    private double slowFactor = 1.0;
    private int slowTimer = 0;

    public Enemy(List<Point> path, int hp, double speed, int goldReward) {
        this(path, hp, speed, goldReward, false, false);
    }

    public Enemy(List<Point> path, int hp, double speed, int goldReward, boolean isBoss) {
        this(path, hp, speed, goldReward, isBoss, false);
    }

    public Enemy(List<Point> path, int hp, double speed, int goldReward, boolean isBoss, boolean isSuperBoss) {
        this.path = path;
        this.hp = hp;
        this.maxHp = hp;
        this.speed = speed;
        this.baseSpeed = speed;
        this.goldReward = goldReward;
        this.isBoss = isBoss;
        this.isSuperBoss = isSuperBoss;
        Point start = path.get(0);
        this.x = start.x;
        this.y = start.y;

        if (isSuperBoss) {
            // Super boss (đợt cuối): to lực lưỡng, da tím đen
            skinColor = new Color(55, 15, 65);
            size = 64 + (int) (Math.random() * 12);
        } else if (isBoss) {
            // Boss thường có màu đỏ thẫm riêng và to hơn hẳn quái thường
            skinColor = new Color(150, 30, 45);
            size = 46 + (int) (Math.random() * 10);
        } else {
            int variant = (int) (Math.random() * 3);
            skinColor = switch (variant) {
                case 1 -> new Color(90, 150, 70);
                case 2 -> new Color(120, 100, 150);
                default -> new Color(70, 140, 80);
            };
            size = 20 + (int) (Math.random() * 8);
        }
        bobPhase = Math.random() * Math.PI * 2;
    }

    public void update() {
        bobPhase += 0.35;
        updateStatusEffects();

        if (pathIndex >= path.size() - 1) {
            reachedEnd = true;
            return;
        }
        Point target = path.get(pathIndex + 1);
        double dx = target.x - x;
        double dy = target.y - y;
        double dist = Math.sqrt(dx * dx + dy * dy);
        if (dist < speed) {
            x = target.x;
            y = target.y;
            pathIndex++;
        } else {
            x += dx / dist * speed;
            y += dy / dist * speed;
        }
    }

    // Xử lý độc (sát thương theo thời gian) và làm chậm (giảm tốc độ tạm thời) mỗi khung hình
    private void updateStatusEffects() {
        if (slowTimer > 0) {
            slowTimer--;
            speed = baseSpeed * slowFactor;
            if (slowTimer <= 0) {
                speed = baseSpeed;
                slowFactor = 1.0;
            }
        }

        if (poisonTicksLeft > 0) {
            poisonTickTimer--;
            if (poisonTickTimer <= 0) {
                takeDamage(poisonDamagePerTick);
                poisonTicksLeft--;
                poisonTickTimer = POISON_TICK_INTERVAL;
            }
        }
    }

    // Gắn hiệu ứng trúng độc: gây dmgPerTick mỗi lần "tick", lặp lại 'ticks' lần
    public void applyPoison(int dmgPerTick, int ticks) {
        this.poisonDamagePerTick = Math.max(1, dmgPerTick);
        this.poisonTicksLeft = ticks;
        this.poisonTickTimer = POISON_TICK_INTERVAL;
    }

    // Gắn hiệu ứng làm chậm: speed còn lại = baseSpeed * factor, kéo dài durationFrames khung hình
    public void applySlow(double factor, int durationFrames) {
        if (slowTimer <= 0 || factor < slowFactor) {
            slowFactor = factor;
        }
        slowTimer = Math.max(slowTimer, durationFrames);
    }

    public boolean isSlowed() {
        return slowTimer > 0;
    }

    public boolean isPoisoned() {
        return poisonTicksLeft > 0;
    }

    public void takeDamage(int dmg) {
        hp -= dmg;
        if (hp <= 0) dead = true;
    }

    public void draw(Graphics2D g) {
        int bob = (int) (Math.sin(bobPhase) * 2);
        int legSwing = (int) (Math.sin(bobPhase * 1.3) * Math.max(2, size / 8)); // biên độ bước chân so le

        // Bóng đổ dưới chân
        g.setColor(new Color(0, 0, 0, 70));
        g.fillOval((int) x - size / 2, (int) y + size / 2 - 3, size, size / 3);

        // Vai cơ bắp lực lưỡng - chỉ super boss mới có, vẽ trước thân để lộ ra hai bên
        if (isSuperBoss) {
            g.setColor(skinColor.darker());
            g.fillOval((int) x - size / 2 - 10, (int) y - size / 6 + bob, size / 3 + 6, size / 2);
            g.fillOval((int) x + size / 2 - size / 3 - 6, (int) y - size / 6 + bob, size / 3 + 6, size / 2);
            g.setColor(new Color(0, 0, 0, 90));
            g.drawOval((int) x - size / 2 - 10, (int) y - size / 6 + bob, size / 3 + 6, size / 2);
            g.drawOval((int) x + size / 2 - size / 3 - 6, (int) y - size / 6 + bob, size / 3 + 6, size / 2);
        }

        // Chân: 2 chân nhỏ bước so le (vẽ trước thân để thân che phần đùi, chỉ lộ bàn chân)
        drawLegs(g, bob, legSwing);

        // Thân - dùng gradient cầu tròn (sáng ở góc trên-trái) thay vì màu phẳng, tạo cảm giác khối 3D
        Point2D bodyCenter = new Point2D.Float((float) (x - size * 0.22), (float) (y - size / 2 + bob - size * 0.22));
        RadialGradientPaint bodyPaint = new RadialGradientPaint(bodyCenter, size * 0.95f,
                new float[]{0f, 1f}, new Color[]{skinColor.brighter(), skinColor.darker()});
        g.setPaint(bodyPaint);
        g.fillOval((int) x - size / 2, (int) y - size / 2 + bob, size, size);
        g.setPaint(null);

        // Lớp phủ băng mờ khi bị làm chậm
        if (isSlowed()) {
            g.setColor(new Color(150, 220, 255, 90));
            g.fillOval((int) x - size / 2, (int) y - size / 2 + bob, size, size);
        }
        // Sắc xanh độc nhẹ phủ lên thân khi trúng độc
        if (isPoisoned()) {
            g.setColor(new Color(90, 200, 90, 70));
            g.fillOval((int) x - size / 2, (int) y - size / 2 + bob, size, size);
        }

        g.setColor(skinColor.darker().darker());
        g.drawOval((int) x - size / 2, (int) y - size / 2 + bob, size, size);

        // Tay cầm vũ khí nhỏ, vung theo nhịp bước chân - vẽ sau thân, trước mắt/sừng
        drawArmWeapon(g, bob, legSwing);

        int hornY = (int) y - size / 2 + bob;
        if (isSuperBoss) {
            // Cặp sừng to, cong ra ngoài - dữ tợn hơn hẳn boss thường
            g.setColor(new Color(215, 205, 185));
            Polygon leftHorn = new Polygon();
            leftHorn.addPoint((int) x - size / 4, hornY + 8);
            leftHorn.addPoint((int) x - size / 2 - 14, hornY - 22);
            leftHorn.addPoint((int) x - size / 4 + 8, hornY - 2);
            g.fillPolygon(leftHorn);
            g.setColor(new Color(120, 110, 90));
            g.drawPolygon(leftHorn);

            g.setColor(new Color(215, 205, 185));
            Polygon rightHorn = new Polygon();
            rightHorn.addPoint((int) x + size / 4, hornY + 8);
            rightHorn.addPoint((int) x + size / 2 + 14, hornY - 22);
            rightHorn.addPoint((int) x + size / 4 - 8, hornY - 2);
            g.fillPolygon(rightHorn);
            g.setColor(new Color(120, 110, 90));
            g.drawPolygon(rightHorn);
        } else {
            // Hai sừng nhỏ
            g.setColor(new Color(230, 230, 210));
            g.fillPolygon(
                    new int[]{(int) x - size / 4, (int) x - size / 4 - 4, (int) x - size / 4 + 3},
                    new int[]{hornY + 2, hornY - 6, hornY - 6},
                    3);
            g.fillPolygon(
                    new int[]{(int) x + size / 4, (int) x + size / 4 - 3, (int) x + size / 4 + 4},
                    new int[]{hornY + 2, hornY - 6, hornY - 6},
                    3);
        }

        // Mắt
        int eyeY = (int) y - 2 + bob;
        g.setColor(Color.WHITE);
        g.fillOval((int) x - 7, eyeY, 5, 5);
        g.fillOval((int) x + 2, eyeY, 5, 5);
        g.setColor(Color.BLACK);
        g.fillOval((int) x - 6, eyeY + 1, 2, 2);
        g.fillOval((int) x + 3, eyeY + 1, 2, 2);

        // Răng nanh nhỏ nhô ra dưới mắt
        g.setColor(new Color(240, 240, 225));
        g.fillPolygon(new int[]{(int) x - 4, (int) x - 1, (int) x - 4}, new int[]{eyeY + 7, eyeY + 7, eyeY + 12}, 3);
        g.fillPolygon(new int[]{(int) x + 4, (int) x + 1, (int) x + 4}, new int[]{eyeY + 7, eyeY + 7, eyeY + 12}, 3);

        if (isBoss) drawCrown(g, bob);

        // Thanh máu (bo góc, viền đen, có gradient đỏ->vàng->xanh theo % máu)
        int barW = isSuperBoss ? 74 : isBoss ? 54 : 32;
        int barH = isSuperBoss ? 10 : isBoss ? 8 : 6;
        int barX = (int) x - barW / 2;
        int barY = (int) y - size / 2 - (isSuperBoss ? 26 : isBoss ? 18 : 14) + bob;
        g.setColor(new Color(0, 0, 0, 160));
        g.fillRoundRect(barX - 1, barY - 1, barW + 2, barH + 2, 4, 4);
        double pct = Math.max(0, (double) hp / maxHp);
        Color hpColor = pct > 0.5 ? new Color(90, 200, 90)
                : pct > 0.25 ? new Color(230, 200, 60)
                : new Color(210, 70, 60);
        g.setColor(hpColor);
        g.fillRoundRect(barX, barY, (int) (barW * pct), barH, 3, 3);

        // Chấm nhỏ báo trạng thái (độc / chậm) cạnh thanh máu
        int statusX = barX + barW + 4;
        if (isPoisoned()) {
            g.setColor(new Color(90, 200, 90, 220));
            g.fillOval(statusX, barY - 1, 6, 6);
            statusX += 8;
        }
        if (isSlowed()) {
            g.setColor(new Color(150, 220, 255, 220));
            g.fillOval(statusX, barY - 1, 6, 6);
        }

        if (isBoss) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("SansSerif", Font.BOLD, isSuperBoss ? 13 : 11));
            String label = isSuperBoss ? "SIÊU BOSS" : "BOSS";
            FontMetrics fm = g.getFontMetrics();
            g.drawString(label, (int) x - fm.stringWidth(label) / 2, barY - 4);
        }
    }

    // 2 chân nhỏ bước so le theo bobPhase - vẽ trước thân nên chỉ lộ phần bàn chân bên dưới
    private void drawLegs(Graphics2D g, int bob, int legSwing) {
        int legW = Math.max(4, size / 6), legH = Math.max(7, size / 4);
        int footY = (int) y + size / 2 - 3 + bob;
        g.setColor(skinColor.darker());
        g.fillRoundRect((int) x - size / 3, footY - legSwing, legW, legH, 3, 3);
        g.fillRoundRect((int) x + size / 3 - legW, footY + legSwing, legW, legH, 3, 3);
        g.setColor(skinColor.darker().darker());
        g.drawRoundRect((int) x - size / 3, footY - legSwing, legW, legH, 3, 3);
        g.drawRoundRect((int) x + size / 3 - legW, footY + legSwing, legW, legH, 3, 3);
    }

    // Cánh tay cầm vũ khí nhỏ (chùy gỗ), vung lên xuống theo nhịp bước chân cho có sức sống
    private void drawArmWeapon(Graphics2D g, int bob, int armSwing) {
        int armX = (int) x + size / 2 - 4;
        int armY = (int) y - 2 + bob;
        int weaponLen = isSuperBoss ? 22 : isBoss ? 16 : 10;
        int wx2 = armX + weaponLen;
        int wy2 = armY - weaponLen / 2 + armSwing;

        g.setColor(new Color(110, 80, 55));
        g.setStroke(new BasicStroke(Math.max(2, size / 10)));
        g.drawLine(armX, armY, wx2, wy2);
        g.setStroke(new BasicStroke(1));

        g.setColor(new Color(90, 90, 95));
        int headSize = isSuperBoss ? 10 : isBoss ? 8 : 6;
        g.fillOval(wx2 - headSize / 2, wy2 - headSize / 2, headSize, headSize);
        g.setColor(new Color(50, 50, 55));
        g.drawOval(wx2 - headSize / 2, wy2 - headSize / 2, headSize, headSize);

        // Nắm tay/vai cầm vũ khí
        g.setColor(skinColor.darker());
        g.fillOval(armX - 4, armY - 4, 8, 8);
    }


    // Vương miện vẽ phía trên đầu boss để phân biệt với quái thường
    // (vàng cho boss thường, tím-đỏ gai góc cho super boss)
    private void drawCrown(Graphics2D g, int bob) {
        double scale = isSuperBoss ? 1.4 : 1.0;
        int cx = (int) x;
        int topY = (int) (y - size / 2 - 6 * scale + bob);
        int w = (int) (12 * scale), spikeUp = (int) (12 * scale), sideUp = (int) (6 * scale), sideDown = (int) (8 * scale);
        Polygon crown = new Polygon();
        crown.addPoint(cx - w, topY + sideDown);
        crown.addPoint(cx - w, topY);
        crown.addPoint(cx - w / 2, topY + sideUp);
        crown.addPoint(cx, topY - spikeUp);
        crown.addPoint(cx + w / 2, topY + sideUp);
        crown.addPoint(cx + w, topY);
        crown.addPoint(cx + w, topY + sideDown);

        Color crownMain = isSuperBoss ? new Color(85, 25, 95) : new Color(255, 210, 60);
        Color crownEdge = isSuperBoss ? new Color(35, 10, 45) : new Color(160, 120, 20);
        Color gem = isSuperBoss ? new Color(255, 60, 60) : new Color(200, 40, 60);

        g.setColor(crownMain);
        g.fillPolygon(crown);
        g.setColor(crownEdge);
        g.drawPolygon(crown);
        g.setColor(gem);
        int gemSize = (int) (4 * scale);
        g.fillOval(cx - gemSize / 2, topY - gemSize / 2, gemSize, gemSize);
    }
}