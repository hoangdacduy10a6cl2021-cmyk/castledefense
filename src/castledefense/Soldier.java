/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package castledefense;

import java.awt.*;
import java.util.List;

/**
 * Lớp Soldier đại diện cho 1 người lính cận chiến do Trại Lính (Tower type 6) triệu hồi.
 * Lính tự tìm quái gần nhất trong phạm vi tuần tra quanh trại, lao tới đánh giáp lá cà,
 * có máu riêng nên có thể "hy sinh" nếu đánh nhau với quái quá mạnh/quá lâu.
 */
public class Soldier {
    double x, y;
    final double homeX, homeY;   // vị trí trại lính đã triệu hồi ra lính này - dùng làm tâm tuần tra
    final Tower homeTower;       // trại lính chủ quản, dùng để đếm số lính còn sống của từng trại
    double speed = 1.6;
    int hp, maxHp;
    int damage;
    int range;                   // bán kính tuần tra quanh trại (lấy từ range của Trại Lính)
    Enemy target;
    boolean dead = false;

    private int attackCooldown = 0;
    private static final int ATTACK_INTERVAL = 20; // khung hình giữa mỗi đòn đánh giáp lá cà
    private static final double MELEE_RANGE = 16;
    private double bobPhase = Math.random() * Math.PI * 2;
    private boolean isMoving = false;

    public Soldier(Tower homeTower, double homeX, double homeY, int hp, int damage, int range) {
        this.homeTower = homeTower;
        this.homeX = homeX;
        this.homeY = homeY;
        this.x = homeX;
        this.y = homeY;
        this.hp = hp;
        this.maxHp = hp;
        this.damage = damage;
        this.range = range;
    }

    public void update(List<Enemy> enemies) {
        bobPhase += 0.3;
        if (attackCooldown > 0) attackCooldown--;

        // Đổi mục tiêu nếu chưa có, mục tiêu đã chết, hoặc đã đi ra ngoài phạm vi tuần tra
        if (target == null || target.dead || Math.hypot(target.x - homeX, target.y - homeY) > range + 30) {
            target = findTarget(enemies);
        }

        if (target != null) {
            double dx = target.x - x, dy = target.y - y;
            double dist = Math.hypot(dx, dy);
            if (dist > MELEE_RANGE) {
                isMoving = true;
                x += dx / dist * speed;
                y += dy / dist * speed;
            } else {
                isMoving = false;
                if (attackCooldown <= 0) {
                    target.takeDamage(damage);
                    // Quái vật còn sống thì đánh trả lại một ít, quái càng khỏe phản đòn càng đau
                    if (!target.dead) {
                        int counter = Math.max(1, target.maxHp / 60);
                        takeDamage(counter);
                    }
                    attackCooldown = ATTACK_INTERVAL;
                    AudioEngine.playSoldierClash();
                }
            }
        } else {
            // Không có quái trong tầm -> quay về vị trí trại lính, đứng gác
            double dx = homeX - x, dy = homeY - y;
            double dist = Math.hypot(dx, dy);
            if (dist > 3) {
                isMoving = true;
                x += dx / dist * speed;
                y += dy / dist * speed;
            } else {
                isMoving = false;
            }
        }
    }

    private Enemy findTarget(List<Enemy> enemies) {
        Enemy best = null;
        double bestDist = Double.MAX_VALUE;
        for (Enemy e : enemies) {
            if (e.dead) continue;
            double d = Math.hypot(e.x - homeX, e.y - homeY);
            if (d <= range && d < bestDist) {
                bestDist = d;
                best = e;
            }
        }
        return best;
    }

    public void takeDamage(int dmg) {
        hp -= dmg;
        if (hp <= 0) dead = true;
    }

    public void draw(Graphics2D g) {
        int bob = (int) (Math.sin(bobPhase) * 1.5);
        int legSwing = isMoving ? (int) (Math.sin(bobPhase * 1.6) * 3) : 0;

        g.setColor(new Color(0, 0, 0, 70));
        g.fillOval((int) x - 8, (int) y + 6, 16, 6);

        // Áo choàng nhỏ sau lưng, bay phất theo nhịp bước
        int capeSway = (int) (Math.sin(bobPhase * 0.8) * 2);
        g.setColor(new Color(120, 30, 30));
        Polygon cape = new Polygon();
        cape.addPoint((int) x - 4, (int) y - 9 + bob);
        cape.addPoint((int) x + 1, (int) y - 9 + bob);
        cape.addPoint((int) x + 3 + capeSway, (int) y + 4 + bob);
        cape.addPoint((int) x - 6 + capeSway, (int) y + 4 + bob);
        g.fillPolygon(cape);

        // 2 chân nhỏ bước so le khi đang di chuyển, đứng yên khi giao chiến/đứng gác
        g.setColor(new Color(45, 40, 35));
        g.fillRoundRect((int) x - 4, (int) y + 2 - legSwing, 3, 6, 2, 2);
        g.fillRoundRect((int) x + 1, (int) y + 2 + legSwing, 3, 6, 2, 2);

        // Thân giáp - dùng gradient cho có ánh kim loại thay vì màu phẳng
        GradientPaint armorPaint = new GradientPaint(
                (float) x - 5, (float) y - 8 + bob, new Color(95, 85, 75),
                (float) x + 5, (float) y + 4 + bob, new Color(55, 48, 40));
        g.setPaint(armorPaint);
        g.fillRoundRect((int) x - 5, (int) y - 8 + bob, 10, 12, 3, 3);
        g.setPaint(null);
        g.setColor(new Color(210, 175, 140));
        g.fillOval((int) x - 4, (int) y - 14 + bob, 8, 8);

        // Mào mũ nhỏ trên đầu
        g.setColor(new Color(150, 30, 30));
        g.fillRect((int) x - 1, (int) y - 17 + bob, 2, 4);

        // Khiên nhỏ bên trái
        g.setColor(new Color(130, 130, 140));
        g.fillRoundRect((int) x - 8, (int) y - 4 + bob, 4, 9, 2, 2);
        g.setColor(new Color(70, 70, 80));
        g.drawRoundRect((int) x - 8, (int) y - 4 + bob, 4, 9, 2, 2);

        // Kiếm nhỏ bên phải, chĩa ra ngoài lúc đang đánh
        boolean attacking = attackCooldown > ATTACK_INTERVAL - 8;
        g.setColor(new Color(200, 200, 210));
        g.setStroke(new BasicStroke(1.5f));
        if (attacking) {
            g.drawLine((int) x + 4, (int) y - 6 + bob, (int) x + 11, (int) y - 10 + bob);
        } else {
            g.drawLine((int) x + 4, (int) y - 6 + bob, (int) x + 8, (int) y + 1 + bob);
        }
        g.setStroke(new BasicStroke(1));

        // Thanh máu nhỏ phía trên đầu
        int barW = 16, barH = 4;
        int barX = (int) x - barW / 2, barY = (int) y - 20 + bob;
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(barX - 1, barY - 1, barW + 2, barH + 2);
        double pct = Math.max(0, (double) hp / maxHp);
        Color hpColor = pct > 0.5 ? new Color(90, 200, 90)
                : pct > 0.25 ? new Color(230, 200, 60)
                : new Color(210, 70, 60);
        g.setColor(hpColor);
        g.fillRect(barX, barY, (int) (barW * pct), barH);
    }
}