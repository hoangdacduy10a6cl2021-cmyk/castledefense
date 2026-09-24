/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package castledefense;

import javax.swing.JPanel;
import javax.swing.JOptionPane;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.function.BiConsumer;

/**
 * GamePanel là "trái tim" của game: chứa vòng lặp game (game loop),
 * xử lý sự kiện chuột/bàn phím, cập nhật trạng thái và vẽ mọi thứ lên màn hình.
 */
public class GamePanel extends JPanel implements ActionListener {
    public static final int WIDTH = 900;
    public static final int HEIGHT = 680;

    // Top bar cao hơn trước để chứa thêm hàng nút kỹ năng đặc biệt (Lửa/Sét/Băng)
    private static final int TOP_BAR_H = 90;
    private static final int SHOP_BAR_H = 90;

    private final Timer timer;
    private final Runnable onBackToMenu;
    private final Runnable onBackToLevelSelect;    // null nếu đang chơi nhanh (không qua hệ thống Chương/Màn)
    private final BiConsumer<Integer, Integer> onLevelComplete; // (số sao, vàng thưởng) khi hoàn thành 1 màn cố định
    private final List<Point> path;
    private final List<Enemy> enemies = new ArrayList<>();
    private final List<Tower> towers = new ArrayList<>();
    private final List<Projectile> projectiles = new ArrayList<>();
    private final List<Soldier> soldiers = new ArrayList<>();

    // Cấu hình Trại Lính: tối đa bao nhiêu lính sống cùng lúc mỗi trại, và máu mỗi lính
    private static final int BARRACKS_MAX_SOLDIERS = 2;
    private static final int BARRACKS_SOLDIER_HP = 70;

    // --- Cấu hình đợt quái của màn đang chơi (lấy từ LevelConfig) + hệ số độ khó (Difficulty) ---
    private final String levelDisplayName;
    private final int totalWaves;
    private final boolean endlessMode;
    private final int baseEnemyHp;
    private final int hpPerWave;
    private final double baseEnemySpeed;
    private final double speedPerWave;
    private final int enemiesBase;
    private final int enemiesPerWave;
    private final int startGold;
    private final double difficultyHpMult;
    private final double difficultySpeedMult;
    private final int completionRewardGold;
    private final int gemsPerStar;

    private int gold;
    private int castleHp = 20;
    private final int maxCastleHp = 20;
    private int wave = 1;
    private int spawnCooldown = 0;
    private int enemiesToSpawn = 0;
    private final int spawnInterval = 55;
    private boolean waveInProgress = false;
    private boolean gameOver = false;
    private boolean levelComplete = false;
    private int starsEarnedResult = 0;

    // Nút bấm hiện ra trong màn hình kết thúc (thắng/thua), tính lại mỗi khung hình theo số nút đang dùng được
    private final List<Rectangle> overlayButtonRects = new ArrayList<>();
    private final List<Runnable> overlayButtonActions = new ArrayList<>();

    private int selectedTowerType = 0;
    private String message = "";
    private int messageTimer = 0;

    // Tăng tốc x2: khi bật, mỗi khung hình timer sẽ chạy update logic 2 lần
    private int speedMultiplier = 1;
    private final Rectangle speedButtonRect = new Rectangle(400, 8, 60, 34);

    // Bật/tắt tự động qua đợt - người chơi chọn lúc bắt đầu, có thể đổi bất cứ lúc nào
    private boolean autoSkipWave = Gamesettings.autoSkipWaveDefault;
    private final Rectangle autoButtonRect = new Rectangle(470, 8, 110, 34);

    // Nút quay lại màn hình chính, đặt ở góc phải ngoài cùng của top bar
    private final Rectangle backButtonRect = new Rectangle(WIDTH - 100, 8, 88, 34);

    // --- Bán tháp: bấm vào 1 tháp đã đặt để chọn, hiện bảng xác nhận bán ngay dưới tháp đó ---
    private static final double SELL_REFUND_RATIO = 0.65; // bán lại được 65% giá gốc
    private static final int SELL_SELECT_RADIUS = 24;     // bán kính coi là "bấm trúng tháp"
    private Tower selectedTower = null;
    private Rectangle sellButtonRect = null; // tính lại mỗi khung hình theo vị trí tháp đang chọn

    // --- Thanh vật phẩm tiêu hao dọc theo rìa phải bản đồ (mua ở Cửa Hàng, dùng thật trong lúc chơi) ---
    private final Rectangle[] itemSlotRects = new Rectangle[ItemCatalog.COUNT];
    private int shieldCharges = 0; // số sát thương tiếp theo lên thành sẽ được chặn (từ vật phẩm Khiên)
    private int reviveCharges = 0; // số lần tự hồi sinh còn lại nếu thành thất thủ (từ vật phẩm Hồi Sinh)
    private int adminInvincibleTicks = 0; // còn lại bao nhiêu khung hình bất tử (kích hoạt từ Admin Panel)

    // Tự động qua đợt: đếm ngược sau khi 1 đợt kết thúc rồi tự startWave()
    private static final int AUTO_WAVE_DELAY = 90; // ~1.5s ở tốc độ thường
    private int autoWaveTimer = AUTO_WAVE_DELAY;
    private boolean bossSpawnedThisWave = false;

    // Dữ liệu 7 loại trụ để dùng chung cho cả logic đặt trụ và vẽ shop bar
    // Dữ liệu 7 loại trụ lấy từ TowerCatalog (dùng chung với UpgradePanel để không lệch số liệu)
    private final String[] towerNames = TowerCatalog.NAMES;
    private final int[] towerCosts = TowerCatalog.COST;
    private final Color[] towerShopColors = TowerCatalog.COLOR;
    private final String[] towerDescs = TowerCatalog.DESCS_SHORT;

    // --- 3 kỹ năng chủ động: Lửa / Sét / Băng, kích hoạt qua nút hoặc phím Q/W/E ---
    private static final int FIRE_COOLDOWN_MAX = 20 * 62;      // ~20s
    private static final int LIGHTNING_COOLDOWN_MAX = 25 * 62; // ~25s
    private static final int ICE_COOLDOWN_MAX = 30 * 62;       // ~30s
    private int skillFireCooldown = 0;
    private int skillLightningCooldown = 0;
    private int skillIceCooldown = 0;

    // Đếm ngược khung hình để vẽ hiệu ứng hình ảnh phủ lên bản đồ sau khi kích hoạt kỹ năng
    private int fireEffectTimer = 0;
    private int lightningEffectTimer = 0;
    private int iceEffectTimer = 0;
    private final List<Point> lightningStrikePoints = new ArrayList<>();

    private final Rectangle fireSkillRect = new Rectangle(12, 50, 150, 32);
    private final Rectangle lightningSkillRect = new Rectangle(172, 50, 150, 32);
    private final Rectangle iceSkillRect = new Rectangle(332, 50, 150, 32);

    // Cây cối / đá trang trí, sinh ngẫu nhiên một lần khi khởi tạo
    private final List<Point> trees = new ArrayList<>();
    private final List<Point> rocks = new ArrayList<>();

    // Vị trí chuột hiện tại, dùng để biết đang lia vào trụ nào (hiện AOE + thông số)
    private int mouseX = -1, mouseY = -1;
    private double ambientPhase = 0; // nhịp chung cho cây đung đưa, cờ phất, ánh sáng nhấp nháy trên bản đồ

    // Constructor cũ - dùng cho nút CHƠI ở Menu chính (chơi nhanh, không qua hệ thống Chương/Màn)
    public GamePanel(Runnable onBackToMenu) {
        this(LevelConfig.quickPlay(), Difficulty.STANDARD, onBackToMenu, null, null);
    }

    // Constructor chính - dùng khi vào 1 màn cụ thể (hoặc Vô Hạn) từ hệ thống Chương/Màn/Độ Khó
    public GamePanel(LevelConfig config, Difficulty difficulty, Runnable onBackToMenu,
                      Runnable onBackToLevelSelect, BiConsumer<Integer, Integer> onLevelComplete) {
        this.onBackToMenu = onBackToMenu;
        this.onBackToLevelSelect = onBackToLevelSelect;
        this.onLevelComplete = onLevelComplete;

        this.levelDisplayName = config.displayName;
        this.totalWaves = config.totalWaves;
        this.endlessMode = config.endless;
        this.baseEnemyHp = config.baseEnemyHp;
        this.hpPerWave = config.hpPerWave;
        this.baseEnemySpeed = config.baseEnemySpeed;
        this.speedPerWave = config.speedPerWave;
        this.enemiesBase = config.enemiesBase;
        this.enemiesPerWave = config.enemiesPerWave;
        this.startGold = config.startGold;
        this.difficultyHpMult = difficulty.hpMult;
        this.difficultySpeedMult = difficulty.speedMult;
        this.completionRewardGold = difficulty.completionReward;
        this.gemsPerStar = difficulty.gemsPerStar;
        this.gold = startGold;

        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(new Color(70, 130, 60));
        setFocusable(true);

        path = buildPath();
        generateDecorations();

        int slotW = 42, slotH = 50, slotGap = 6, slotX = WIDTH - slotW - 8, slotStartY = TOP_BAR_H + 10;
        for (int i = 0; i < itemSlotRects.length; i++) {
            itemSlotRects[i] = new Rectangle(slotX, slotStartY + i * (slotH + slotGap), slotW, slotH);
        }

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleClick(e.getX(), e.getY());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                mouseX = -1;
                mouseY = -1;
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                mouseX = e.getX();
                mouseY = e.getY();
            }
        });

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (gameOver || levelComplete) {
                    if (e.getKeyCode() == KeyEvent.VK_R) restartGame();
                    return;
                }
                if (e.getKeyCode() == KeyEvent.VK_SPACE && !waveInProgress) {
                    startWave();
                }
                int code = e.getKeyCode();
                for (int i = 0; i < 7; i++) {
                    if (code == Gamesettings.getKey(i)) selectedTowerType = i;
                }
                if (code == Gamesettings.getKey(Gamesettings.ACTION_SKILL_FIRE)) activateFireSkill();
                if (code == Gamesettings.getKey(Gamesettings.ACTION_SKILL_LIGHTNING)) activateLightningSkill();
                if (code == Gamesettings.getKey(Gamesettings.ACTION_SKILL_ICE)) activateIceSkill();
                if (code == Gamesettings.getKey(Gamesettings.ACTION_SELL_TOWER)) {
                    if (selectedTower != null) {
                        sellTower(selectedTower);
                        selectedTower = null;
                        sellButtonRect = null;
                    }
                }
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) confirmBackToMenu();
            }
        });


        timer = new Timer(16, this);
        timer.start();
    }

    // Hỏi xác nhận trước khi thoát về Menu chính, vì tiến trình chơi hiện tại sẽ bị mất
    private void confirmBackToMenu() {
        if (onBackToMenu == null) return;
        timer.stop();
        int choice = JOptionPane.showConfirmDialog(
                this,
                "Quay lại Menu chính sẽ mất tiến trình đợt hiện tại.\nBạn có chắc muốn thoát ra không?",
                "Quay lại Menu chính",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        if (choice == JOptionPane.YES_OPTION) {
            onBackToMenu.run();
        } else {
            timer.start();
        }
    }

    private List<Point> buildPath() {
        List<Point> p = new ArrayList<>();
        p.add(new Point(0, TOP_BAR_H + 270));
        p.add(new Point(200, TOP_BAR_H + 270));
        p.add(new Point(200, TOP_BAR_H + 70));
        p.add(new Point(500, TOP_BAR_H + 70));
        p.add(new Point(500, TOP_BAR_H + 450));
        p.add(new Point(750, TOP_BAR_H + 450));
        p.add(new Point(750, TOP_BAR_H + 250));
        p.add(new Point(855, TOP_BAR_H + 250));
        return p;
    }

    // Sinh cây và đá ở những vị trí không đè lên đường đi, dùng seed cố định
    // để bố cục giống nhau mỗi lần chạy (trông "được thiết kế" chứ không lộn xộn).
    private void generateDecorations() {
        Random rnd = new Random(42);
        int attempts = 0;
        while (trees.size() < 26 && attempts < 800) {
            attempts++;
            int px = 15 + rnd.nextInt(WIDTH - 30);
            int py = TOP_BAR_H + 15 + rnd.nextInt(HEIGHT - TOP_BAR_H - SHOP_BAR_H - 30);
            if (isOnPath(px, py, 42) || tooCloseToList(trees, px, py, 34)) continue;
            trees.add(new Point(px, py));
        }
        attempts = 0;
        while (rocks.size() < 14 && attempts < 800) {
            attempts++;
            int px = 15 + rnd.nextInt(WIDTH - 30);
            int py = TOP_BAR_H + 15 + rnd.nextInt(HEIGHT - TOP_BAR_H - SHOP_BAR_H - 30);
            if (isOnPath(px, py, 40) || tooCloseToList(trees, px, py, 24) || tooCloseToList(rocks, px, py, 30)) continue;
            rocks.add(new Point(px, py));
        }
    }

    private boolean tooCloseToList(List<Point> list, int px, int py, int minDist) {
        for (Point pt : list) {
            if (Math.hypot(pt.x - px, pt.y - py) < minDist) return true;
        }
        return false;
    }

    private void restartGame() {
        enemies.clear();
        towers.clear();
        projectiles.clear();
        soldiers.clear();
        gold = startGold;
        castleHp = maxCastleHp;
        wave = 1;
        waveInProgress = false;
        gameOver = false;
        levelComplete = false;
        starsEarnedResult = 0;
        bossSpawnedThisWave = false;
        autoWaveTimer = AUTO_WAVE_DELAY;
        skillFireCooldown = 0;
        skillLightningCooldown = 0;
        skillIceCooldown = 0;
        fireEffectTimer = 0;
        lightningEffectTimer = 0;
        iceEffectTimer = 0;
        selectedTower = null;
        sellButtonRect = null;
        shieldCharges = 0;
        reviveCharges = 0;
        adminInvincibleTicks = 0;
        showMessage("Ván mới bắt đầu! Đợt 1 sắp bắt đầu...");
    }

    // ================== Chỉ dùng bởi Admin Panel (Ctrl+Shift+A) để test nhanh ==================
    // Các hàm dưới đây không phải cơ chế chơi bình thường, chỉ phục vụ gian lận/kiểm thử thủ công.

    public void adminAddGold(int amount) {
        gold += amount;
        showMessage("[ADMIN] +" + amount + " vàng!");
    }

    public void adminFullHealCastle() {
        castleHp = maxCastleHp;
        showMessage("[ADMIN] Đã hồi đầy máu thành!");
    }

    public void adminActivateInvincibility(int seconds) {
        adminInvincibleTicks = seconds * 62;
        showMessage("[ADMIN] Bất tử trong " + seconds + " giây!");
    }

    // Kết thúc ngay đợt hiện tại: xoá hết quái còn sống + huỷ hàng chờ, để tick() tự xử lý
    // hoàn thành đợt (qua đợt mới hoặc hoàn thành màn) ở khung hình kế tiếp như bình thường.
    public void adminSkipWave() {
        enemies.clear();
        enemiesToSpawn = 0;
        showMessage("[ADMIN] Đã bỏ qua đợt hiện tại!");
    }

    // Giết toàn bộ quái đang có mặt trên bản đồ (không ảnh hưởng quái chưa xuất hiện trong hàng chờ)
    public void adminKillAllEnemies() {
        for (Enemy en : enemies) {
            en.takeDamage(Integer.MAX_VALUE / 2);
        }
        showMessage("[ADMIN] Đã tiêu diệt toàn bộ quái trên bản đồ!");
    }

    // Tạm dừng/khôi phục vòng lặp game khi Admin Panel đang mở, tránh game vẫn chạy ngầm phía sau
    public void pauseForAdmin() {
        timer.stop();
    }

    public void resumeFromAdmin() {
        timer.start();
    }

    // Xử lý click chuột: nút x2 tốc độ, nút kỹ năng, bấm vào shop bar để chọn loại trụ, hoặc đặt trụ trên bản đồ
    private void handleClick(int mx, int my) {
        if (gameOver || levelComplete) {
            for (int i = 0; i < overlayButtonRects.size(); i++) {
                if (overlayButtonRects.get(i).contains(mx, my)) {
                    overlayButtonActions.get(i).run();
                    return;
                }
            }
            return;
        }

        if (speedButtonRect.contains(mx, my)) {
            speedMultiplier = (speedMultiplier == 1) ? 2 : 1;
            AudioEngine.playClick();
            return;
        }

        if (autoButtonRect.contains(mx, my)) {
            autoSkipWave = !autoSkipWave;
            showMessage(autoSkipWave ? "Đã BẬT tự động qua đợt" : "Đã TẮT tự động qua đợt");
            AudioEngine.playClick();
            return;
        }

        if (backButtonRect.contains(mx, my)) {
            AudioEngine.playClick();
            confirmBackToMenu();
            return;
        }

        if (fireSkillRect.contains(mx, my)) {
            activateFireSkill();
            return;
        }
        if (lightningSkillRect.contains(mx, my)) {
            activateLightningSkill();
            return;
        }
        if (iceSkillRect.contains(mx, my)) {
            activateIceSkill();
            return;
        }

        // Bấm vào 1 ô vật phẩm dọc rìa phải bản đồ -> dùng ngay lập tức nếu còn hàng
        for (int i = 0; i < itemSlotRects.length; i++) {
            if (itemSlotRects[i].contains(mx, my)) {
                useItem(i);
                return;
            }
        }

        // Đang có tháp được chọn và người chơi bấm đúng nút "Bán" hiện ra dưới tháp đó -> bán luôn
        if (selectedTower != null && sellButtonRect != null && sellButtonRect.contains(mx, my)) {
            sellTower(selectedTower);
            selectedTower = null;
            sellButtonRect = null;
            return;
        }

        if (my >= HEIGHT - SHOP_BAR_H) {
            selectedTower = null;
            sellButtonRect = null;
            int boxW = 118, gap = 8, startX = 10;
            for (int i = 0; i < towerNames.length; i++) {
                int bx = startX + i * (boxW + gap);
                if (mx >= bx && mx <= bx + boxW) {
                    selectedTowerType = i;
                    AudioEngine.playClick();
                    return;
                }
            }
            return;
        }
        if (my < TOP_BAR_H) return;

        // Bấm trúng 1 tháp đã đặt trên bản đồ -> chọn để hiện bảng bán, không đè tháp mới lên
        for (Tower t : towers) {
            if (Math.hypot(t.x - mx, t.y - my) < SELL_SELECT_RADIUS) {
                selectedTower = t;
                return;
            }
        }

        // Bấm ra chỗ khác thì bỏ chọn tháp hiện tại, rồi xử lý đặt tháp mới như bình thường
        selectedTower = null;
        sellButtonRect = null;

        int cost = towerCosts[selectedTowerType];
        if (gold < cost) {
            showMessage("Không đủ vàng!");
            AudioEngine.playError();
            return;
        }
        if (isOnPath(mx, my, 32)) {
            showMessage("Không thể đặt trụ trên đường đi!");
            AudioEngine.playError();
            return;
        }
        for (Tower t : towers) {
            if (Math.hypot(t.x - mx, t.y - my) < 38) {
                showMessage("Quá gần trụ khác!");
                AudioEngine.playError();
                return;
            }
        }

        Tower t = createTower(mx, my, selectedTowerType);
        towers.add(t);
        gold -= cost;
        GameProgress.addTowerPlaced();
        AudioEngine.playPlaceTower();
    }

    // Tạo 1 tháp mới từ chỉ số GỐC trong TowerCatalog, cộng thêm hệ số từ các node đã nâng cấp
    // vĩnh viễn ở màn Nâng Cấp (TowerUpgradeData) - Sát Thương/Tầm Bắn/Tốc Độ Bắn + thưởng Tinh Nhuệ.
    private Tower createTower(int mx, int my, int type) {
        int damage = (int) Math.round(TowerCatalog.BASE_DAMAGE[type] * TowerUpgradeData.damageMultiplier(type));
        int range = TowerCatalog.BASE_RANGE[type] + TowerUpgradeData.rangeBonus(type);
        int fireRate = Math.max(4, (int) Math.round(TowerCatalog.BASE_FIRE_RATE[type] * TowerUpgradeData.fireRateMultiplier(type)));
        return new Tower(mx, my, range, damage, fireRate, TowerCatalog.COST[type], type, TowerCatalog.COLOR[type]);
    }

    private boolean isOnPath(int mx, int my, int threshold) {
        for (int i = 0; i < path.size() - 1; i++) {
            Point a = path.get(i);
            Point b = path.get(i + 1);
            if (distToSegment(mx, my, a.x, a.y, b.x, b.y) < threshold) return true;
        }
        return false;
    }

    private double distToSegment(double px, double py, double ax, double ay, double bx, double by) {
        double dx = bx - ax, dy = by - ay;
        double len2 = dx * dx + dy * dy;
        double t = len2 == 0 ? 0 : ((px - ax) * dx + (py - ay) * dy) / len2;
        t = Math.max(0, Math.min(1, t));
        double cx = ax + t * dx, cy = ay + t * dy;
        return Math.hypot(px - cx, py - cy);
    }

    private void startWave() {
        waveInProgress = true;
        enemiesToSpawn = enemiesBase + wave * enemiesPerWave;
        spawnCooldown = 0;
        bossSpawnedThisWave = false;
        autoWaveTimer = 0;
        AudioEngine.playWaveStart();
    }

    private void showMessage(String msg) {
        message = msg;
        messageTimer = 90;
    }

    // Bán 1 tháp: xoá khỏi bản đồ và hoàn lại một phần vàng theo SELL_REFUND_RATIO
    private void sellTower(Tower t) {
        int refund = (int) Math.round(t.cost * SELL_REFUND_RATIO);
        gold += refund;
        towers.remove(t);
        if (t.type == 6) {
            soldiers.removeIf(s -> s.homeTower == t);
        }
        showMessage("Đã bán " + t.typeName() + ", nhận lại " + refund + " vàng.");
        AudioEngine.playSellTower();
    }

    // --- Kích hoạt 3 kỹ năng chủ động ---

    // LỬA: gây sát thương tức thì cho toàn bộ quái đang trên bản đồ, kèm cháy lan thêm một ít theo thời gian
    private void activateFireSkill() {
        if (gameOver || levelComplete || skillFireCooldown > 0) return;
        skillFireCooldown = FIRE_COOLDOWN_MAX;
        fireEffectTimer = 40;
        for (Enemy en : enemies) {
            int dmg = Math.max(15, (int) Math.round(en.maxHp * 0.22));
            en.takeDamage(dmg);
            en.applyPoison(Math.max(2, dmg / 8), 6);
        }
        showMessage("Kỹ năng LỬA: Thiêu đốt toàn bộ quái vật trên bản đồ!");
        AudioEngine.playSkillFire();
    }

    // SẤM SÉT: đánh trúng tối đa 5 quái ngẫu nhiên với sát thương rất cao
    private void activateLightningSkill() {
        if (gameOver || levelComplete || skillLightningCooldown > 0) return;
        skillLightningCooldown = LIGHTNING_COOLDOWN_MAX;
        lightningEffectTimer = 30;
        lightningStrikePoints.clear();

        List<Enemy> alive = new ArrayList<>(enemies);
        Collections.shuffle(alive);
        int count = Math.min(5, alive.size());
        for (int i = 0; i < count; i++) {
            Enemy en = alive.get(i);
            int dmg = Math.max(40, (int) Math.round(en.maxHp * 0.35));
            en.takeDamage(dmg);
            lightningStrikePoints.add(new Point((int) en.x, (int) en.y));
        }
        showMessage("Kỹ năng SẤM SÉT: Đánh trúng " + count + " quái vật!");
        AudioEngine.playSkillLightning();
    }

    // BĂNG: làm chậm toàn bộ quái vật trên bản đồ trong vài giây
    private void activateIceSkill() {
        if (gameOver || levelComplete || skillIceCooldown > 0) return;
        skillIceCooldown = ICE_COOLDOWN_MAX;
        iceEffectTimer = 50;
        for (Enemy en : enemies) {
            en.applySlow(0.25, 240);
        }
        showMessage("Kỹ năng BĂNG: Đóng băng làm chậm toàn bộ quái vật!");
        AudioEngine.playSkillIce();
    }

    // Dùng 1 vật phẩm từ kho đồ (GameProgress) - trừ 1 lượt và áp dụng hiệu ứng thật ngay trong ván đang chơi
    private void useItem(int itemId) {
        if (gameOver || levelComplete) return;
        if (!GameProgress.useItem(itemId)) {
            showMessage("Đã hết " + ItemCatalog.NAMES[itemId] + "! Mua thêm ở Cửa Hàng.");
            AudioEngine.playError();
            return;
        }
        switch (itemId) {
            case ItemCatalog.HEAL:
                castleHp = Math.min(maxCastleHp, castleHp + (int) Math.round(maxCastleHp * 0.3));
                showMessage("Đã dùng Hồi Máu! Thành hồi phục một phần máu.");
                break;
            case ItemCatalog.BUILD_BOOST:
                gold += 100;
                GameProgress.addGoldEarned(100);
                showMessage("Đã dùng Tăng Tốc Xây! +100 vàng.");
                break;
            case ItemCatalog.BOMB:
                for (Enemy en : enemies) {
                    int dmg = Math.max(30, (int) Math.round(en.maxHp * 0.4));
                    en.takeDamage(dmg);
                }
                if (Gamesettings.effectsEnabled) fireEffectTimer = 40;
                showMessage("Đã dùng Bom! Gây sát thương diện rộng lên toàn bộ quái.");
                break;
            case ItemCatalog.FROST:
                for (Enemy en : enemies) {
                    en.applySlow(0.2, 260);
                }
                if (Gamesettings.effectsEnabled) iceEffectTimer = 50;
                showMessage("Đã dùng Băng Giá! Làm chậm toàn bộ quái vật.");
                break;
            case ItemCatalog.SHIELD:
                shieldCharges += 5;
                showMessage("Đã dùng Khiên! Chặn 5 sát thương tiếp theo lên thành.");
                break;
            default:
                reviveCharges += 1;
                showMessage("Đã dùng Hồi Sinh! Sẽ tự hồi sinh nếu thành thất thủ.");
        }
        AudioEngine.playUpgradeSuccess();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!gameOver && !levelComplete) update();
        repaint();
    }

    // Bọc ngoài tick(): khi bật x2 thì chạy logic game 2 lần mỗi khung hình
    // để mọi thứ (địch, trụ, đạn, đếm ngược...) đều nhanh gấp đôi đồng bộ.
    private void update() {
        for (int i = 0; i < speedMultiplier; i++) {
            tick();
            if (gameOver) break;
        }
    }

    private void tick() {
        // Đợi hết đợt: đếm ngược rồi tự bắt đầu đợt kế tiếp, không cần bấm SPACE
        if (!waveInProgress && !gameOver && !levelComplete && wave <= totalWaves && autoSkipWave && autoWaveTimer > 0) {
            autoWaveTimer--;
            if (autoWaveTimer <= 0) startWave();
        }

        if (waveInProgress) {
            spawnCooldown--;
            if (spawnCooldown <= 0 && enemiesToSpawn > 0) {
                // Con cuối cùng của mỗi đợt là boss
                boolean spawnBoss = (enemiesToSpawn == 1 && !bossSpawnedThisWave);
                spawnEnemy(spawnBoss);
                if (spawnBoss) bossSpawnedThisWave = true;
                enemiesToSpawn--;
                spawnCooldown = spawnInterval;
            }
            if (enemiesToSpawn == 0 && enemies.isEmpty()) {
                waveInProgress = false;
                GameProgress.addWavesSurvived(1);
                if (!endlessMode && wave >= totalWaves) {
                    triggerLevelComplete();
                } else {
                    wave++;
                    gold += 40;
                    GameProgress.addGoldEarned(40);
                    autoWaveTimer = AUTO_WAVE_DELAY;
                    showMessage("Hoàn thành đợt! Đợt " + wave + " sắp tự động bắt đầu...");
                }
            }
        }

        Iterator<Enemy> eit = enemies.iterator();
        while (eit.hasNext()) {
            Enemy en = eit.next();
            en.update();
            if (en.reachedEnd) {
                int dmg = en.isSuperBoss ? 5 : en.isBoss ? 2 : 1;
                // Bất Tử (Admin Panel): chặn hoàn toàn sát thương lên thành trong thời gian có hiệu lực
                if (adminInvincibleTicks > 0) dmg = 0;
                // Khiên (vật phẩm): chặn bớt sát thương sắp tới trước khi trừ vào máu thành
                if (shieldCharges > 0) {
                    int absorbed = Math.min(shieldCharges, dmg);
                    shieldCharges -= absorbed;
                    dmg -= absorbed;
                }
                castleHp -= dmg;
                eit.remove();
                if (dmg > 0) AudioEngine.playCastleHit();
                if (castleHp <= 0) {
                    if (reviveCharges > 0) {
                        // Hồi Sinh (vật phẩm): cứu thành vào phút chót thay vì thua ngay
                        reviveCharges--;
                        castleHp = (int) Math.round(maxCastleHp * 0.3);
                        showMessage("HỒI SINH! Thành đã được cứu vào phút chót!");
                        AudioEngine.playVictory();
                    } else {
                        castleHp = 0;
                        gameOver = true;
                        AudioEngine.playDefeat();
                    }
                }
            } else if (en.dead) {
                gold += en.goldReward;
                GameProgress.addGoldEarned(en.goldReward);
                GameProgress.addEnemiesKilled(1);
                eit.remove();
                AudioEngine.playEnemyDeath();
            }
        }

        for (Tower t : towers) {
            t.update();
            if (t.type == 6) {
                long aliveCount = soldiers.stream().filter(s -> s.homeTower == t && !s.dead).count();
                if (aliveCount < BARRACKS_MAX_SOLDIERS && t.canFire()) {
                    soldiers.add(new Soldier(t, t.x, t.y, BARRACKS_SOLDIER_HP, t.damage, t.range));
                    t.fire();
                }
                continue;
            }
            Enemy target = findTarget(t);
            t.aimAt(target);
            if (t.canFire() && target != null) {
                int dmg = t.damage;
                // Chí Mạng: cơ hội gây gấp đôi sát thương
                if (Math.random() < TowerUpgradeData.critChance(t.type)) {
                    dmg = (int) Math.round(dmg * 2.0);
                }
                // Xuyên Giáp: thêm % sát thương riêng khi mục tiêu là Boss/Siêu Boss
                if (target.isBoss || target.isSuperBoss) {
                    dmg += (int) Math.round(t.damage * TowerUpgradeData.bossBonusDamageRatio(t.type));
                }
                projectiles.add(new Projectile(t.x, t.y, target, dmg, t.type, t.projectileColor(), enemies));
                t.fire();
                AudioEngine.playShoot(t.type);

                // Bắn Lan: có cơ hội bắn thêm 1 mũi vào 1 địch khác đang đứng gần trong tầm
                if (Math.random() < TowerUpgradeData.multishotChance(t.type)) {
                    Enemy second = findSecondTarget(t, target);
                    if (second != null) {
                        projectiles.add(new Projectile(t.x, t.y, second, t.damage, t.type, t.projectileColor(), enemies));
                    }
                }
            }
        }

        Iterator<Soldier> sit = soldiers.iterator();
        while (sit.hasNext()) {
            Soldier s = sit.next();
            s.update(enemies);
            if (s.dead) sit.remove();
        }

        Iterator<Projectile> pit = projectiles.iterator();
        while (pit.hasNext()) {
            Projectile p = pit.next();
            p.update();
            if (p.hit) pit.remove();
        }

        if (messageTimer > 0) messageTimer--;
        ambientPhase += 0.03;
        if (adminInvincibleTicks > 0) adminInvincibleTicks--;

        if (skillFireCooldown > 0) skillFireCooldown--;
        if (skillLightningCooldown > 0) skillLightningCooldown--;
        if (skillIceCooldown > 0) skillIceCooldown--;
        if (fireEffectTimer > 0) fireEffectTimer--;
        if (lightningEffectTimer > 0) lightningEffectTimer--;
        if (iceEffectTimer > 0) iceEffectTimer--;
    }

    private Enemy findTarget(Tower t) {
        Enemy best = null;
        double bestProgress = -1;
        for (Enemy en : enemies) {
            if (t.distanceTo(en) <= t.range && en.pathIndex > bestProgress) {
                bestProgress = en.pathIndex;
                best = en;
            }
        }
        return best;
    }

    // Tìm 1 mục tiêu phụ khác với mục tiêu chính, dùng cho hiệu ứng Bắn Lan (node nâng cấp MULTISHOT)
    private Enemy findSecondTarget(Tower t, Enemy exclude) {
        Enemy best = null;
        double bestProgress = -1;
        for (Enemy en : enemies) {
            if (en == exclude) continue;
            if (t.distanceTo(en) <= t.range && en.pathIndex > bestProgress) {
                bestProgress = en.pathIndex;
                best = en;
            }
        }
        return best;
    }

    // Trụ mà chuột đang lia vào (nếu có) - dùng để hiện vòng AOE + bảng thông số
    private Tower findHoveredTower() {
        for (Tower t : towers) {
            if (t.isHovered(mouseX, mouseY)) return t;
        }
        return null;
    }

    private void spawnEnemy(boolean boss) {
        int hp = baseEnemyHp + wave * hpPerWave;
        double speed = baseEnemySpeed + wave * speedPerWave;
        int goldReward = 8 + wave;
        // Boss của đợt cuối (totalWaves) là super boss - mạnh hơn hẳn boss thường (không áp dụng ở Vô Hạn)
        boolean superBoss = boss && !endlessMode && wave >= totalWaves;
        if (superBoss) {
            hp *= 14;
            speed *= 0.65;
            goldReward *= 12;
        } else if (boss) {
            hp *= 6;
            speed *= 0.75;
            goldReward *= 5;
        }
        hp = (int) Math.round(hp * difficultyHpMult);
        speed *= difficultySpeedMult;
        enemies.add(new Enemy(path, hp, speed, goldReward, boss, superBoss));
        AudioEngine.playEnemyGrowl(boss, superBoss);
    }

    // Đánh giá số sao (1-3) dựa trên % máu thành còn lại khi vừa hoàn thành xong màn
    private int computeStars() {
        double pct = maxCastleHp <= 0 ? 0 : (double) castleHp / maxCastleHp;
        if (pct >= 0.9) return 3;
        if (pct >= 0.5) return 2;
        return 1;
    }

    // Đánh dấu đã hoàn thành màn: dừng gameplay, tính sao, cộng vàng thưởng, báo lên GameFrame để lưu tiến trình
    private void triggerLevelComplete() {
        levelComplete = true;
        starsEarnedResult = computeStars();
        gold += completionRewardGold;
        GameProgress.addGoldEarned(completionRewardGold);
        GameProgress.addBattleWon();
        if (onLevelComplete != null) onLevelComplete.accept(starsEarnedResult, completionRewardGold);
        AudioEngine.playVictory();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawBackground(g2);
        drawPath(g2);
        drawCastle(g2);

        // Trụ đang được chuột lia vào: vẽ vòng tầm bắn (AOE) phía dưới lớp trụ
        Tower hoveredTower = findHoveredTower();
        if (hoveredTower != null) hoveredTower.drawRangeCircle(g2);

        for (Tower t : towers) t.draw(g2);

        List<Enemy> sortedEnemies = new ArrayList<>(enemies);
        List<Soldier> sortedSoldiers = new ArrayList<>(soldiers);
        sortedEnemies.sort((a, b) -> Double.compare(a.y, b.y));
        sortedSoldiers.sort((a, b) -> Double.compare(a.y, b.y));

        // Trộn 2 danh sách đã sắp xếp theo y để lính và quái đè lên nhau đúng chiều sâu
        int ei = 0, si = 0;
        while (ei < sortedEnemies.size() && si < sortedSoldiers.size()) {
            if (sortedEnemies.get(ei).y <= sortedSoldiers.get(si).y) {
                sortedEnemies.get(ei++).draw(g2);
            } else {
                sortedSoldiers.get(si++).draw(g2);
            }
        }
        while (ei < sortedEnemies.size()) sortedEnemies.get(ei++).draw(g2);
        while (si < sortedSoldiers.size()) sortedSoldiers.get(si++).draw(g2);

        for (Projectile p : projectiles) p.draw(g2);

        // Hiệu ứng hình ảnh của 3 kỹ năng đặc biệt, phủ lên bản đồ khi vừa kích hoạt
        if (Gamesettings.effectsEnabled) {
            if (fireEffectTimer > 0) drawFireEffect(g2);
            if (lightningEffectTimer > 0) drawLightningEffect(g2);
            if (iceEffectTimer > 0) drawIceEffect(g2);
        }

        // Bảng thông số vẽ sau cùng (trừ top/shop bar) để không bị địch/đạn đè lên
        if (hoveredTower != null) hoveredTower.drawTooltip(g2);

        // Bảng xác nhận bán tháp, hiện ngay dưới tháp đang được chọn (nếu có)
        drawSellPanel(g2);

        drawTopBar(g2);
        drawShopBar(g2);
        drawItemBar(g2);

        if (messageTimer > 0) {
            g2.setColor(new Color(0, 0, 0, 150));
            g2.setFont(new Font("SansSerif", Font.BOLD, 20));
            FontMetrics fm = g2.getFontMetrics();
            int w = fm.stringWidth(message) + 30;
            g2.fillRoundRect((WIDTH - w) / 2, TOP_BAR_H + 12, w, 34, 12, 12);
            g2.setColor(Color.WHITE);
            g2.drawString(message, (WIDTH - fm.stringWidth(message)) / 2, TOP_BAR_H + 35);
        }

        if (Gamesettings.showHints && !waveInProgress && !gameOver && wave <= totalWaves) {
            g2.setColor(new Color(255, 230, 90));
            g2.setFont(new Font("SansSerif", Font.BOLD, 15));
            String hint;
            if (autoSkipWave) {
                int secs = autoWaveTimer / speedMultiplier / 62 + 1;
                hint = "Đợt tiếp theo tự động sau " + secs + "s  (SPACE để bắt đầu ngay)";
            } else {
                hint = "Tự động qua đợt đang TẮT — Nhấn SPACE để bắt đầu đợt tiếp theo";
            }
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(hint, (WIDTH - fm.stringWidth(hint)) / 2, HEIGHT - SHOP_BAR_H - 12);
        }

        if (gameOver) {
            drawEndOverlay(g2, "THÀNH ĐÃ THẤT THỦ!", Color.WHITE,
                    "Bạn trụ được " + (wave - 1) + " đợt.", false, 0);
        } else if (levelComplete) {
            int gemsEarned = starsEarnedResult * gemsPerStar;
            StringBuilder rewardLine = new StringBuilder();
            if (completionRewardGold > 0) rewardLine.append("+").append(completionRewardGold).append(" vàng");
            if (gemsEarned > 0) {
                if (rewardLine.length() > 0) rewardLine.append("  •  ");
                rewardLine.append("+").append(gemsEarned).append(" \u2666 Kim Cương");
            }
            if (rewardLine.length() == 0) rewardLine.append("Xuất sắc!");
            drawEndOverlay(g2, "HOÀN THÀNH " + levelDisplayName.toUpperCase() + "!", new Color(255, 210, 60),
                    rewardLine.toString(), true, starsEarnedResult);
        }
    }

    // Màn hình phủ mờ khi thắng/thua: tiêu đề lớn, dòng phụ, tuỳ chọn hàng sao, và các nút hành động
    // (Chơi Lại luôn có; Chọn Màn Khác chỉ hiện nếu đang chơi 1 màn cố định; Menu Chính luôn có)
    private void drawEndOverlay(Graphics2D g2, String bigTitle, Color titleColor, String subLine,
                                 boolean showStars, int starsEarned) {
        g2.setColor(new Color(0, 0, 0, 190));
        g2.fillRect(0, 0, WIDTH, HEIGHT);

        g2.setColor(titleColor);
        g2.setFont(new Font("SansSerif", Font.BOLD, 40));
        FontMetrics fmT = g2.getFontMetrics();
        g2.drawString(bigTitle, (WIDTH - fmT.stringWidth(bigTitle)) / 2, HEIGHT / 2 - 90);

        if (showStars) {
            int starSize = 34, starGap = 10;
            int totalW = starSize * 3 + starGap * 2;
            int sx = (WIDTH - totalW) / 2;
            int sy = HEIGHT / 2 - 60;
            for (int s = 0; s < 3; s++) {
                g2.setColor(s < starsEarned ? new Color(255, 210, 60) : new Color(255, 255, 255, 70));
                g2.setFont(new Font("SansSerif", Font.BOLD, starSize));
                g2.drawString("\u2605", sx + s * (starSize + starGap), sy + starSize);
            }
        }

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 18));
        FontMetrics fmS = g2.getFontMetrics();
        g2.drawString(subLine, (WIDTH - fmS.stringWidth(subLine)) / 2, HEIGHT / 2 - 6);

        // Xây danh sách nút còn dùng được rồi canh giữa theo chiều ngang
        List<String> labels = new ArrayList<>();
        List<Runnable> actions = new ArrayList<>();
        labels.add("Chơi Lại (R)");
        actions.add(() -> {
            AudioEngine.playClick();
            restartGame();
        });
        if (onBackToLevelSelect != null) {
            labels.add("Chọn Màn Khác");
            actions.add(() -> {
                AudioEngine.playClick();
                timer.stop();
                onBackToLevelSelect.run();
            });
        }
        labels.add("Menu Chính");
        actions.add(() -> {
            AudioEngine.playClick();
            timer.stop();
            if (onBackToMenu != null) onBackToMenu.run();
        });

        int btnW = 150, btnH = 44, btnGap = 16;
        int totalBtnW = labels.size() * btnW + (labels.size() - 1) * btnGap;
        int startX = (WIDTH - totalBtnW) / 2;
        int btnY = HEIGHT / 2 + 30;

        overlayButtonRects.clear();
        overlayButtonActions.clear();
        for (int i = 0; i < labels.size(); i++) {
            Rectangle r = new Rectangle(startX + i * (btnW + btnGap), btnY, btnW, btnH);
            overlayButtonRects.add(r);
            overlayButtonActions.add(actions.get(i));

            g2.setColor(new Color(70, 70, 80, 230));
            g2.fill(new RoundRectangle2D.Double(r.x, r.y, r.width, r.height, 10, 10));
            g2.setColor(new Color(230, 190, 90));
            g2.setStroke(new BasicStroke(2f));
            g2.draw(new RoundRectangle2D.Double(r.x, r.y, r.width, r.height, 10, 10));
            g2.setStroke(new BasicStroke(1));

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("SansSerif", Font.BOLD, 13));
            FontMetrics fmB = g2.getFontMetrics();
            String label = labels.get(i);
            g2.drawString(label, r.x + (r.width - fmB.stringWidth(label)) / 2, r.y + 28);
        }
    }

    // Hiệu ứng LỬA: nền đỏ cam mờ phủ toàn bản đồ + các đốm lửa ngẫu nhiên
    private void drawFireEffect(Graphics2D g2) {
        float alpha = Math.min(1f, fireEffectTimer / 40f);
        g2.setColor(new Color(255, 100, 30, (int) (90 * alpha)));
        g2.fillRect(0, TOP_BAR_H, WIDTH, HEIGHT - TOP_BAR_H - SHOP_BAR_H);

        Random rnd = new Random(fireEffectTimer * 31 + 1);
        for (int i = 0; i < 18; i++) {
            int px = rnd.nextInt(WIDTH);
            int py = TOP_BAR_H + rnd.nextInt(HEIGHT - TOP_BAR_H - SHOP_BAR_H);
            int s = 10 + rnd.nextInt(20);
            g2.setColor(new Color(255, 140 + rnd.nextInt(80), 30, (int) (160 * alpha)));
            g2.fillOval(px, py, s, s);
        }
    }

    // Hiệu ứng SẤM SÉT: tia chớp giáng từ trên trời xuống từng vị trí quái bị đánh trúng
    private void drawLightningEffect(Graphics2D g2) {
        g2.setColor(new Color(255, 255, 255, Math.min(255, lightningEffectTimer * 6)));
        g2.setStroke(new BasicStroke(3f));
        for (Point p : lightningStrikePoints) {
            g2.drawLine(p.x, TOP_BAR_H, p.x, p.y);
            g2.drawLine(p.x - 8, p.y - 20, p.x + 6, p.y - 6);
            g2.drawLine(p.x + 6, p.y - 6, p.x - 4, p.y + 4);
        }
        g2.setStroke(new BasicStroke(1));
    }

    // Hiệu ứng BĂNG: nền xanh lam mờ phủ toàn bản đồ, mô phỏng luồng khí lạnh
    private void drawIceEffect(Graphics2D g2) {
        float alpha = Math.min(1f, iceEffectTimer / 50f);
        g2.setColor(new Color(150, 220, 255, (int) (70 * alpha)));
        g2.fillRect(0, TOP_BAR_H, WIDTH, HEIGHT - TOP_BAR_H - SHOP_BAR_H);
    }

    // Vòng tròn nét đứt đánh dấu tháp đang chọn + bảng "Bán trụ này?" hiện ngay dưới tháp
    private void drawSellPanel(Graphics2D g2) {
        if (selectedTower == null || !towers.contains(selectedTower)) {
            selectedTower = null;
            sellButtonRect = null;
            return;
        }
        Tower t = selectedTower;
        int refund = (int) Math.round(t.cost * SELL_REFUND_RATIO);

        g2.setColor(new Color(255, 230, 90, 200));
        g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{6, 6}, 0));
        g2.drawOval(t.x - 26, t.y - 26, 52, 52);
        g2.setStroke(new BasicStroke(1));

        int boxW = 160, boxH = 42;
        int boxX = t.x - boxW / 2;
        int boxY = t.y + 30;
        boxX = Math.max(4, Math.min(WIDTH - boxW - 4, boxX));
        boxY = Math.min(HEIGHT - SHOP_BAR_H - boxH - 4, boxY);

        sellButtonRect = new Rectangle(boxX, boxY, boxW, boxH);

        g2.setColor(new Color(140, 30, 30, 235));
        g2.fill(new RoundRectangle2D.Double(boxX, boxY, boxW, boxH, 10, 10));
        g2.setColor(new Color(255, 255, 255, 110));
        g2.setStroke(new BasicStroke(1.5f));
        g2.draw(new RoundRectangle2D.Double(boxX, boxY, boxW, boxH, 10, 10));
        g2.setStroke(new BasicStroke(1));

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 13));
        String line1 = "Bán " + t.typeName() + "?";
        FontMetrics fm1 = g2.getFontMetrics();
        g2.drawString(line1, boxX + (boxW - fm1.stringWidth(line1)) / 2, boxY + 17);

        g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
        String line2 = "Nhận lại " + refund + " vàng (bấm để bán)";
        FontMetrics fm2 = g2.getFontMetrics();
        g2.setColor(new Color(255, 225, 130));
        g2.drawString(line2, boxX + (boxW - fm2.stringWidth(line2)) / 2, boxY + 33);
    }

    // Nền cỏ có gradient nhẹ + các mảng cỏ đậm nhạt + cây + đá cho đỡ trống trải
    private void drawBackground(Graphics2D g2) {
        GradientPaint grass = new GradientPaint(0, 0, new Color(96, 158, 74), 0, HEIGHT, new Color(66, 122, 54));
        g2.setPaint(grass);
        g2.fillRect(0, 0, WIDTH, HEIGHT);

        Random rnd = new Random(7);
        for (int i = 0; i < 60; i++) {
            int px = rnd.nextInt(WIDTH);
            int py = TOP_BAR_H + rnd.nextInt(HEIGHT - TOP_BAR_H - SHOP_BAR_H);
            g2.setColor(new Color(0, 0, 0, 12));
            g2.fillOval(px, py, 30, 18);
        }

        for (Point r : rocks) {
            g2.setColor(new Color(0, 0, 0, 50));
            g2.fillOval(r.x - 9, r.y - 4, 22, 12);
            g2.setColor(new Color(150, 150, 150));
            g2.fillOval(r.x - 10, r.y - 10, 20, 16);
            g2.setColor(new Color(110, 110, 110));
            g2.drawOval(r.x - 10, r.y - 10, 20, 16);
        }

        for (Point t : trees) drawTree(g2, t.x, t.y);
    }

    private void drawTree(Graphics2D g2, int x, int y) {
        // Mỗi cây đung đưa lệch nhịp riêng (dựa theo toạ độ) để cả rừng không lắc cùng lúc trông giả
        double sway = Math.sin(ambientPhase + x * 0.05 + y * 0.03) * 2.5;
        int leafOffset = (int) sway;

        g2.setColor(new Color(0, 0, 0, 60));
        g2.fillOval(x - 14, y + 8, 28, 10);
        g2.setColor(new Color(101, 67, 33));
        g2.fillRect(x - 4, y - 2, 8, 16);
        g2.setColor(new Color(40, 100, 45));
        g2.fillOval(x - 18 + leafOffset, y - 34, 36, 36);
        g2.setColor(new Color(55, 130, 60));
        g2.fillOval(x - 12 + leafOffset, y - 40, 26, 26);
        g2.setColor(new Color(30, 85, 40));
        g2.drawOval(x - 18 + leafOffset, y - 34, 36, 36);
    }

    private void drawPath(Graphics2D g2) {
        g2.setStroke(new BasicStroke(56, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(new Color(120, 95, 60));
        for (int i = 0; i < path.size() - 1; i++) {
            Point a = path.get(i), b = path.get(i + 1);
            g2.drawLine(a.x, a.y, b.x, b.y);
        }

        g2.setStroke(new BasicStroke(48, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(new Color(207, 178, 128));
        for (int i = 0; i < path.size() - 1; i++) {
            Point a = path.get(i), b = path.get(i + 1);
            g2.drawLine(a.x, a.y, b.x, b.y);
        }

        // Vệt mòn nhạt hơn ở giữa đường cho có chiều sâu
        g2.setStroke(new BasicStroke(10, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                10, new float[]{2, 14}, 0));
        g2.setColor(new Color(170, 145, 100, 150));
        for (int i = 0; i < path.size() - 1; i++) {
            Point a = path.get(i), b = path.get(i + 1);
            g2.drawLine(a.x, a.y, b.x, b.y);
        }
        g2.setStroke(new BasicStroke(1));
    }

    private void drawCastle(Graphics2D g2) {
        Point c = path.get(path.size() - 1);
        int cx = c.x - 15, cy = c.y;

        g2.setColor(new Color(0, 0, 0, 70));
        g2.fillOval(cx - 35, cy + 45, 90, 20);

        drawSmallTurret(g2, cx - 28, cy - 5);
        drawSmallTurret(g2, cx + 45, cy - 5);

        GradientPaint wall = new GradientPaint(cx - 30, cy - 60, new Color(180, 180, 185),
                cx + 30, cy + 40, new Color(120, 120, 128));
        g2.setPaint(wall);
        g2.fillRect(cx - 30, cy - 40, 60, 80);
        g2.setColor(new Color(80, 80, 90));
        g2.drawRect(cx - 30, cy - 40, 60, 80);

        g2.setColor(new Color(150, 150, 158));
        for (int i = -1; i <= 1; i++) {
            g2.fillRect(cx - 30 + (i + 1) * 20 - 6, cy - 50, 12, 12);
        }

        g2.setColor(new Color(60, 45, 35));
        g2.fillArc(cx - 12, cy + 4, 24, 30, 0, 180);
        g2.fillRect(cx - 12, cy + 19, 24, 15);

        g2.setColor(Color.DARK_GRAY);
        g2.drawLine(cx, cy - 70, cx, cy - 40);
        double castleFlagWave = Math.sin(ambientPhase * 1.5) * 4;
        g2.setColor(new Color(50, 90, 190));
        Polygon castleFlag = new Polygon();
        castleFlag.addPoint(cx, cy - 70);
        castleFlag.addPoint(cx + 16 + (int) castleFlagWave, cy - 64 + (int) (castleFlagWave * 0.4));
        castleFlag.addPoint(cx, cy - 58);
        g2.fillPolygon(castleFlag);
    }

    private void drawSmallTurret(Graphics2D g2, int x, int y) {
        g2.setColor(new Color(140, 140, 148));
        g2.fillRoundRect(x - 12, y - 20, 24, 60, 4, 4);
        g2.setColor(new Color(80, 80, 90));
        g2.drawRoundRect(x - 12, y - 20, 24, 60, 4, 4);
        Polygon roof = new Polygon(
                new int[]{x - 14, x + 14, x}, new int[]{y - 20, y - 20, y - 42}, 3);
        g2.setColor(new Color(60, 100, 190));
        g2.fillPolygon(roof);
        g2.setColor(new Color(30, 60, 130));
        g2.drawPolygon(roof);

        // Cờ nhỏ phất trên đỉnh chòi, lệch nhịp theo vị trí cho tự nhiên
        g2.setColor(Color.DARK_GRAY);
        g2.drawLine(x, y - 42, x, y - 54);
        double turretFlagWave = Math.sin(ambientPhase * 1.5 + x * 0.1) * 3;
        g2.setColor(new Color(200, 60, 60));
        Polygon turretFlag = new Polygon();
        turretFlag.addPoint(x, y - 54);
        turretFlag.addPoint(x + 12 + (int) turretFlagWave, y - 50 + (int) (turretFlagWave * 0.3));
        turretFlag.addPoint(x, y - 46);
        g2.fillPolygon(turretFlag);
    }

    // Thanh trên cùng: hàng 1 = Máu thành / Vàng / Đợt / x2 / Auto / Menu,
    // hàng 2 = 3 nút kỹ năng đặc biệt Lửa / Sét / Băng
    private void drawTopBar(Graphics2D g2) {
        g2.setColor(new Color(25, 30, 25, 230));
        g2.fillRect(0, 0, WIDTH, TOP_BAR_H);
        g2.setColor(new Color(90, 70, 40));
        g2.fillRect(0, TOP_BAR_H - 3, WIDTH, 3);

        drawStatPill(g2, 12, 8, new Color(200, 60, 60), "\u2764", castleHp + " / " + maxCastleHp);
        drawStatPill(g2, 150, 8, new Color(230, 190, 50), "\u25CF", String.valueOf(gold));
        drawStatPill(g2, 270, 8, new Color(200, 200, 200), "\u2620", "Đợt " + wave + "/" + (endlessMode ? "\u221E" : String.valueOf(totalWaves)));
        drawSpeedButton(g2);
        drawAutoButton(g2);
        drawBackButton(g2);

        drawSkillButton(g2, fireSkillRect, "Lửa (" + Gamesettings.keyName(Gamesettings.ACTION_SKILL_FIRE) + ")", new Color(220, 90, 40), skillFireCooldown, FIRE_COOLDOWN_MAX);
        drawSkillButton(g2, lightningSkillRect, "Sét (" + Gamesettings.keyName(Gamesettings.ACTION_SKILL_LIGHTNING) + ")", new Color(80, 110, 210), skillLightningCooldown, LIGHTNING_COOLDOWN_MAX);
        drawSkillButton(g2, iceSkillRect, "Băng (" + Gamesettings.keyName(Gamesettings.ACTION_SKILL_ICE) + ")", new Color(90, 180, 220), skillIceCooldown, ICE_COOLDOWN_MAX);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
        String hint = "SPACE bắt đầu ngay";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(hint, backButtonRect.x - fm.stringWidth(hint) - 14, 30);

        // Trạng thái Khiên/Hồi Sinh đang có hiệu lực (từ vật phẩm), hiện ngay dưới top bar
        int badgeX = 12;
        if (shieldCharges > 0) {
            String txt = "\uD83D\uDEE1 Khiên: chặn " + shieldCharges + " sát thương";
            g2.setColor(new Color(90, 160, 230));
            g2.setFont(new Font("SansSerif", Font.BOLD, 12));
            g2.drawString(txt, badgeX, TOP_BAR_H + 14);
            badgeX += g2.getFontMetrics().stringWidth(txt) + 16;
        }
        if (reviveCharges > 0) {
            String txt = "\u2728 Hồi Sinh sẵn sàng x" + reviveCharges;
            g2.setColor(new Color(230, 210, 140));
            g2.setFont(new Font("SansSerif", Font.BOLD, 12));
            g2.drawString(txt, badgeX, TOP_BAR_H + 14);
        }
    }

    // Vẽ 1 nút kỹ năng đặc biệt: sáng màu khi sẵn sàng, xám + đếm ngược khi đang hồi chiêu
    private void drawSkillButton(Graphics2D g2, Rectangle r, String label, Color col, int cooldown, int maxCooldown) {
        boolean ready = cooldown <= 0;
        g2.setColor(ready ? col : new Color(50, 50, 55));
        g2.fill(new RoundRectangle2D.Double(r.x, r.y, r.width, r.height, 12, 12));
        g2.setColor(ready ? Color.WHITE : new Color(150, 150, 150));
        g2.setStroke(new BasicStroke(ready ? 2.2f : 1f));
        g2.draw(new RoundRectangle2D.Double(r.x, r.y, r.width, r.height, 12, 12));
        g2.setStroke(new BasicStroke(1));

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 13));
        String text = ready ? label : label + "  " + (cooldown / 62 + 1) + "s";
        FontMetrics fm = g2.getFontMetrics();
        int tx = r.x + (r.width - fm.stringWidth(text)) / 2;
        g2.drawString(text, tx, r.y + 21);

        if (!ready) {
            double pct = 1.0 - (double) cooldown / maxCooldown;
            g2.setColor(new Color(255, 255, 255, 70));
            g2.fillRoundRect(r.x + 2, r.y + r.height - 5, (int) ((r.width - 4) * pct), 3, 3, 3);
        }
    }

    // Nút quay lại Menu chính, đặt ở góc phải ngoài cùng của top bar
    private void drawBackButton(Graphics2D g2) {
        g2.setColor(new Color(140, 40, 40, 200));
        g2.fill(new RoundRectangle2D.Double(backButtonRect.x, backButtonRect.y,
                backButtonRect.width, backButtonRect.height, 14, 14));
        g2.setColor(new Color(255, 255, 255, 90));
        g2.draw(new RoundRectangle2D.Double(backButtonRect.x, backButtonRect.y,
                backButtonRect.width, backButtonRect.height, 14, 14));

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 13));
        String label = "\u2190 Menu";
        FontMetrics fm = g2.getFontMetrics();
        int tx = backButtonRect.x + (backButtonRect.width - fm.stringWidth(label)) / 2;
        g2.drawString(label, tx, backButtonRect.y + 23);
    }

    // Nút bấm x2 tốc độ, đặt ngay sau 3 pill thông tin trên top bar
    private void drawSpeedButton(Graphics2D g2) {
        boolean active = speedMultiplier == 2;
        if (active) drawActiveGlow(g2, speedButtonRect, new Color(120, 220, 140));
        g2.setColor(active ? new Color(80, 170, 90) : new Color(0, 0, 0, 120));
        g2.fill(new RoundRectangle2D.Double(speedButtonRect.x, speedButtonRect.y,
                speedButtonRect.width, speedButtonRect.height, 14, 14));
        g2.setColor(active ? new Color(230, 255, 210) : new Color(255, 255, 255, 60));
        g2.setStroke(new BasicStroke(active ? 2.5f : 1f));
        g2.draw(new RoundRectangle2D.Double(speedButtonRect.x, speedButtonRect.y,
                speedButtonRect.width, speedButtonRect.height, 14, 14));
        g2.setStroke(new BasicStroke(1));

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 15));
        String label = "x2";
        FontMetrics fm = g2.getFontMetrics();
        int tx = speedButtonRect.x + (speedButtonRect.width - fm.stringWidth(label)) / 2;
        g2.drawString(label, tx, speedButtonRect.y + 23);
    }

    // Vòng hào quang mờ nhấp nháy nhẹ quanh 1 nút đang ở trạng thái BẬT, giúp dễ nhận biết hơn
    private void drawActiveGlow(Graphics2D g2, Rectangle r, Color glowColor) {
        int pulse = (int) (30 + Math.sin(ambientPhase * 2) * 20);
        g2.setColor(new Color(glowColor.getRed(), glowColor.getGreen(), glowColor.getBlue(), Math.max(0, pulse)));
        g2.fill(new RoundRectangle2D.Double(r.x - 3, r.y - 3, r.width + 6, r.height + 6, 16, 16));
    }

    // Nút bật/tắt tự động qua đợt, đặt ngay sau nút x2 tốc độ trên top bar
    private void drawAutoButton(Graphics2D g2) {
        if (autoSkipWave) drawActiveGlow(g2, autoButtonRect, new Color(120, 220, 140));
        g2.setColor(autoSkipWave ? new Color(80, 170, 90) : new Color(0, 0, 0, 120));
        g2.fill(new RoundRectangle2D.Double(autoButtonRect.x, autoButtonRect.y,
                autoButtonRect.width, autoButtonRect.height, 14, 14));
        g2.setColor(autoSkipWave ? new Color(230, 255, 210) : new Color(255, 255, 255, 60));
        g2.setStroke(new BasicStroke(autoSkipWave ? 2.5f : 1f));
        g2.draw(new RoundRectangle2D.Double(autoButtonRect.x, autoButtonRect.y,
                autoButtonRect.width, autoButtonRect.height, 14, 14));
        g2.setStroke(new BasicStroke(1));

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 13));
        String label = "Auto: " + (autoSkipWave ? "ON" : "OFF");
        FontMetrics fm = g2.getFontMetrics();
        int tx = autoButtonRect.x + (autoButtonRect.width - fm.stringWidth(label)) / 2;
        g2.drawString(label, tx, autoButtonRect.y + 23);
    }

    private void drawStatPill(Graphics2D g2, int x, int y, Color iconColor, String icon, String value) {
        int w = 118, h = 34;
        g2.setColor(new Color(0, 0, 0, 120));
        g2.fill(new RoundRectangle2D.Double(x, y, w, h, 14, 14));
        g2.setColor(new Color(255, 255, 255, 40));
        g2.draw(new RoundRectangle2D.Double(x, y, w, h, 14, 14));

        g2.setColor(iconColor);
        g2.setFont(new Font("SansSerif", Font.BOLD, 16));
        g2.drawString(icon, x + 10, y + 23);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 15));
        g2.drawString(value, x + 32, y + 23);
    }

    // Thanh chọn trụ ở dưới cùng (6 loại) - bấm chuột trực tiếp vào từng ô để chọn
    private void drawShopBar(Graphics2D g2) {
        int barY = HEIGHT - SHOP_BAR_H;
        g2.setColor(new Color(25, 30, 25, 235));
        g2.fillRect(0, barY, WIDTH, SHOP_BAR_H);
        g2.setColor(new Color(90, 70, 40));
        g2.fillRect(0, barY, WIDTH, 3);

        int boxW = 118, boxH = 70, gap = 8, startX = 10, boxY = barY + 12;

        for (int i = 0; i < towerNames.length; i++) {
            int bx = startX + i * (boxW + gap);
            boolean selected = (i == selectedTowerType);
            boolean affordable = gold >= towerCosts[i];

            if (selected) drawActiveGlow(g2, new Rectangle(bx, boxY, boxW, boxH), new Color(255, 220, 90));
            g2.setColor(selected ? new Color(80, 120, 60) : new Color(45, 45, 50));
            g2.fill(new RoundRectangle2D.Double(bx, boxY, boxW, boxH, 12, 12));
            g2.setColor(selected ? new Color(255, 230, 90) : new Color(90, 90, 95));
            g2.setStroke(new BasicStroke(selected ? 3f : 1.5f));
            g2.draw(new RoundRectangle2D.Double(bx, boxY, boxW, boxH, 12, 12));
            g2.setStroke(new BasicStroke(1));

            drawShopIcon(g2, bx + 20, boxY + 30, i);

            g2.setColor(affordable ? Color.WHITE : new Color(160, 160, 160));
            g2.setFont(new Font("SansSerif", Font.BOLD, 12));
            g2.drawString(towerNames[i], bx + 38, boxY + 19);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
            g2.drawString(towerDescs[i], bx + 38, boxY + 33);

            g2.setColor(affordable ? new Color(230, 190, 50) : new Color(160, 90, 90));
            g2.setFont(new Font("SansSerif", Font.BOLD, 12));
            g2.drawString(towerCosts[i] + " V", bx + 38, boxY + 54);
        }
    }

    private void drawShopIcon(Graphics2D g2, int cx, int cy, int type) {
        switch (type) {
            case 1:
                g2.setColor(new Color(80, 200, 210));
                g2.fillPolygon(new int[]{cx, cx - 10, cx, cx + 10}, new int[]{cy - 16, cy, cy + 16, cy}, 4);
                g2.setColor(new Color(20, 100, 120));
                g2.drawPolygon(new int[]{cx, cx - 10, cx, cx + 10}, new int[]{cy - 16, cy, cy + 16, cy}, 4);
                break;
            case 2:
                g2.setColor(new Color(90, 90, 95));
                g2.fillOval(cx - 14, cy - 6, 28, 20);
                g2.setColor(new Color(45, 45, 50));
                g2.fillRoundRect(cx - 4, cy - 20, 8, 20, 4, 4);
                break;
            case 3:
                g2.setColor(new Color(200, 190, 170));
                g2.drawLine(cx - 12, cy, cx + 12, cy);
                g2.drawLine(cx, cy - 12, cx - 8, cy + 4);
                g2.drawLine(cx, cy - 12, cx + 8, cy + 4);
                break;
            case 4:
                g2.setColor(new Color(90, 170, 70));
                g2.fillOval(cx - 9, cy - 6, 18, 14);
                g2.setColor(new Color(50, 90, 40));
                g2.fillRect(cx - 3, cy - 14, 6, 8);
                break;
            case 5:
                g2.setColor(new Color(190, 220, 255));
                Polygon bolt = new Polygon();
                bolt.addPoint(cx - 2, cy - 16);
                bolt.addPoint(cx + 4, cy - 2);
                bolt.addPoint(cx - 1, cy - 2);
                bolt.addPoint(cx + 3, cy + 16);
                bolt.addPoint(cx - 6, cy);
                bolt.addPoint(cx, cy);
                g2.fillPolygon(bolt);
                break;
            case 6:
                g2.setColor(new Color(150, 60, 55));
                Polygon tentIcon = new Polygon();
                tentIcon.addPoint(cx - 12, cy + 10);
                tentIcon.addPoint(cx + 12, cy + 10);
                tentIcon.addPoint(cx, cy - 14);
                g2.fillPolygon(tentIcon);
                g2.setColor(new Color(90, 30, 25));
                g2.drawPolygon(tentIcon);
                g2.setColor(new Color(90, 150, 70));
                g2.fillRect(cx, cy - 22, 8, 6);
                g2.setColor(Color.DARK_GRAY);
                g2.drawLine(cx, cy - 22, cx, cy - 14);
                break;
            default:
                g2.setColor(new Color(190, 170, 140));
                g2.fillRoundRect(cx - 9, cy - 18, 18, 22, 4, 4);
                Polygon roof = new Polygon(new int[]{cx - 12, cx + 12, cx}, new int[]{cy - 16, cy - 16, cy - 30}, 3);
                g2.setColor(new Color(60, 100, 190));
                g2.fillPolygon(roof);
        }
    }

    // Thanh vật phẩm tiêu hao dọc rìa phải bản đồ - bấm vào 1 ô để dùng ngay nếu còn hàng trong kho
    private void drawItemBar(Graphics2D g2) {
        for (int i = 0; i < itemSlotRects.length; i++) {
            Rectangle r = itemSlotRects[i];
            int count = GameProgress.getItemCount(i);
            boolean has = count > 0;

            g2.setColor(new Color(20, 18, 28, has ? 220 : 150));
            g2.fill(new RoundRectangle2D.Double(r.x, r.y, r.width, r.height, 10, 10));
            g2.setColor(has ? ItemCatalog.COLOR[i] : new Color(70, 70, 75));
            g2.setStroke(new BasicStroke(has ? 2f : 1.2f));
            g2.draw(new RoundRectangle2D.Double(r.x, r.y, r.width, r.height, 10, 10));
            g2.setStroke(new BasicStroke(1));

            drawSmallItemIcon(g2, r.x + r.width / 2, r.y + 18, i, has);

            g2.setColor(has ? Color.WHITE : new Color(140, 140, 140));
            g2.setFont(new Font("SansSerif", Font.BOLD, 11));
            String countText = "x" + count;
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(countText, r.x + (r.width - fm.stringWidth(countText)) / 2, r.y + r.height - 6);
        }
    }

    private void drawSmallItemIcon(Graphics2D g2, int cx, int cy, int id, boolean active) {
        g2.setColor(active ? ItemCatalog.COLOR[id] : new Color(90, 90, 95));
        switch (id) {
            case ItemCatalog.HEAL:
                g2.fillRect(cx - 6, cy - 2, 12, 4);
                g2.fillRect(cx - 2, cy - 6, 4, 12);
                break;
            case ItemCatalog.BUILD_BOOST:
                g2.fillRect(cx - 2, cy - 8, 4, 14);
                g2.fillRect(cx - 8, cy - 8, 16, 5);
                break;
            case ItemCatalog.BOMB:
                g2.fillOval(cx - 8, cy - 4, 16, 16);
                g2.fillRect(cx - 1, cy - 10, 2, 6);
                break;
            case ItemCatalog.FROST:
                g2.drawLine(cx, cy - 8, cx, cy + 8);
                g2.drawLine(cx - 7, cy - 4, cx + 7, cy + 4);
                g2.drawLine(cx - 7, cy + 4, cx + 7, cy - 4);
                break;
            case ItemCatalog.SHIELD:
                g2.fillRoundRect(cx - 7, cy - 8, 14, 11, 4, 4);
                Polygon tip = new Polygon();
                tip.addPoint(cx - 7, cy + 2);
                tip.addPoint(cx + 7, cy + 2);
                tip.addPoint(cx, cy + 9);
                g2.fillPolygon(tip);
                break;
            default:
                g2.fillOval(cx - 5, cy - 9, 10, 10);
                g2.fillPolygon(new int[]{cx - 8, cx, cx + 8}, new int[]{cy + 6, cy - 2, cy + 6}, 3);
        }
    }
}