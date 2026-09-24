/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package castledefense;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Lớp Projectile đại diện cho viên đạn bay từ trụ tới quái vật (bám mục tiêu).
 * Mỗi loại trụ (type) bắn ra đạn với màu sắc/hình dáng riêng, và một số loại
 * có hiệu ứng đặc biệt khi trúng đích (xuyên/lan sang địch gần, gây độc, sét lan).
 */
public class Projectile {
    double x, y;
    Enemy target;
    double speed = 9;
    int damage;
    int type;     // loại trụ đã bắn ra viên đạn này (0=cung thủ,1=pháp sư,2=đại bác,3=nỏ,4=độc,5=sét)
    Color color;
    boolean hit = false;
    private final List<Enemy> allEnemies; // tham chiếu toàn bộ quái, dùng cho hiệu ứng lan/xuyên (có thể null)

    // Vệt mờ dần phía sau đạn, ghi lại vài vị trí gần nhất để vẽ đường bay có "sức sống" hơn
    private final List<double[]> trail = new ArrayList<>();
    private static final int TRAIL_LENGTH = 5;

    public Projectile(double x, double y, Enemy target, int damage, int type, Color color, List<Enemy> allEnemies) {
        this.x = x;
        this.y = y;
        this.target = target;
        this.damage = damage;
        this.type = type;
        this.color = color;
        this.allEnemies = allEnemies;
    }

    public void update() {
        trail.add(new double[]{x, y});
        if (trail.size() > TRAIL_LENGTH) trail.remove(0);

        if (target.dead) {
            hit = true;
            return;
        }
        double dx = target.x - x;
        double dy = target.y - y;
        double dist = Math.sqrt(dx * dx + dy * dy);
        if (dist < speed) {
            target.takeDamage(damage);
            applyOnHitEffect();
            hit = true;
        } else {
            x += dx / dist * speed;
            y += dy / dist * speed;
        }
    }

    // Hiệu ứng phụ khi đạn trúng mục tiêu, tùy theo loại trụ đã bắn ra nó
    private void applyOnHitEffect() {
        switch (type) {
            case 3: // Nỏ Liên Hoàn: mũi tên xuyên qua, gây thêm sát thương cho địch đứng gần điểm trúng
                if (allEnemies != null) {
                    for (Enemy e : allEnemies) {
                        if (e != target && !e.dead && Math.hypot(e.x - target.x, e.y - target.y) < 40) {
                            e.takeDamage((int) Math.round(damage * 0.6));
                        }
                    }
                }
                break;
            case 4: // Phù Thủy Độc: gây thêm sát thương độc rải đều theo thời gian
                target.applyPoison(Math.max(1, damage / 3), 5);
                break;
            case 5: // Tháp Sét: tia sét lan sang tối đa 2 địch gần mục tiêu
                if (allEnemies != null) {
                    int chains = 0;
                    for (Enemy e : allEnemies) {
                        if (chains >= 2) break;
                        if (e != target && !e.dead && Math.hypot(e.x - target.x, e.y - target.y) < 90) {
                            e.takeDamage((int) Math.round(damage * 0.6));
                            chains++;
                        }
                    }
                }
                break;
            default:
                break;
        }
    }

    public void draw(Graphics2D g) {
        drawTrail(g);
        switch (type) {
            case 1: drawMagicOrb(g); break;
            case 2: drawCannonball(g); break;
            case 3: drawBallistaBolt(g); break;
            case 4: drawPoisonFlask(g); break;
            case 5: drawSpark(g); break;
            default: drawArrow(g);
        }
    }

    // Vệt các chấm nhỏ mờ dần phía sau đạn theo màu của chính viên đạn, tạo cảm giác đang lao nhanh
    private void drawTrail(Graphics2D g) {
        int n = trail.size();
        for (int i = 0; i < n; i++) {
            double[] p = trail.get(i);
            double t = (double) (i + 1) / (n + 1); // 0..1, càng gần vị trí hiện tại càng đậm/to
            int alpha = (int) (100 * t);
            int size = (int) (2 + 4 * t);
            g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), Math.max(0, Math.min(255, alpha))));
            g.fillOval((int) p[0] - size / 2, (int) p[1] - size / 2, size, size);
        }
    }

    // Đạn cung thủ: mũi tên nhỏ hướng theo chiều bay
    private void drawArrow(Graphics2D g) {
        double dx = target.x - x, dy = target.y - y;
        double angle = Math.atan2(dy, dx);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.translate(x, y);
        g2.rotate(angle);
        g2.setColor(new Color(120, 80, 40));
        g2.fillRect(-7, -1, 12, 2);
        g2.setColor(color);
        g2.fillPolygon(new int[]{5, 11, 5}, new int[]{-4, 0, 4}, 3);
        g2.dispose();
    }

    // Đạn pháp sư: quả cầu phép phát sáng, có quầng sáng mờ xung quanh
    private void drawMagicOrb(Graphics2D g) {
        g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 70));
        g.fillOval((int) x - 8, (int) y - 8, 16, 16);
        g.setColor(color);
        g.fillOval((int) x - 4, (int) y - 4, 8, 8);
        g.setColor(Color.WHITE);
        g.fillOval((int) x - 2, (int) y - 2, 4, 4);
    }

    // Đạn đại bác: viên đá/sắt to, tối màu, có viền đậm
    private void drawCannonball(Graphics2D g) {
        g.setColor(new Color(0, 0, 0, 70));
        g.fillOval((int) x - 6, (int) y - 5, 12, 12);
        g.setColor(color);
        g.fillOval((int) x - 6, (int) y - 6, 11, 11);
        g.setColor(Color.BLACK);
        g.drawOval((int) x - 6, (int) y - 6, 11, 11);
    }

    // Mũi nỏ liên hoàn: to và dài hơn mũi tên thường, ánh kim loại
    private void drawBallistaBolt(Graphics2D g) {
        double dx = target.x - x, dy = target.y - y;
        double angle = Math.atan2(dy, dx);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.translate(x, y);
        g2.rotate(angle);
        g2.setColor(new Color(90, 70, 50));
        g2.fillRect(-9, -1, 16, 2);
        g2.setColor(color);
        g2.fillPolygon(new int[]{7, 14, 7}, new int[]{-4, 0, 4}, 3);
        g2.setColor(new Color(80, 70, 60));
        g2.drawPolygon(new int[]{7, 14, 7}, new int[]{-4, 0, 4}, 3);
        g2.dispose();
    }

    // Lọ độc: bình thủy tinh nhỏ màu xanh lá, sủi bọt độc
    private void drawPoisonFlask(Graphics2D g) {
        g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 90));
        g.fillOval((int) x - 7, (int) y - 7, 14, 14);
        g.setColor(color);
        g.fillOval((int) x - 4, (int) y - 4, 8, 8);
        g.setColor(new Color(40, 70, 30));
        g.drawOval((int) x - 4, (int) y - 4, 8, 8);
    }

    // Tia sét: 2 nét chớp chéo tạo hình chữ X phát sáng cùng lõi trắng
    private void drawSpark(Graphics2D g) {
        g.setColor(color);
        g.setStroke(new BasicStroke(2f));
        g.drawLine((int) x - 5, (int) y - 5, (int) x + 5, (int) y + 5);
        g.drawLine((int) x + 5, (int) y - 5, (int) x - 5, (int) y + 5);
        g.setStroke(new BasicStroke(1));
        g.setColor(Color.WHITE);
        g.fillOval((int) x - 2, (int) y - 2, 4, 4);
    }
}