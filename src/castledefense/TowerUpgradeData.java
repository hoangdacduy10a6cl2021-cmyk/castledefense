/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package castledefense;

/**
 * Lưu tiến trình nâng cấp vĩnh viễn cho từng loại tháp (dùng Kim Cương, xem GameProgress).
 * Mỗi tháp có 6 node chia 2 tầng:
 * - Tầng 1 (luôn mở): Tăng Sát Thương, Tăng Tầm Bắn, Tốc Độ Bắn.
 * - Tầng 2 (mở khi node tầng 1 tương ứng đạt cấp 2): Chí Mạng, Xuyên Giáp, Bắn Lan.
 * Khi cả 6 node đạt cấp tối đa, tháp trở thành "Tinh Nhuệ" và nhận thêm hệ số cộng dồn.
 */
public class TowerUpgradeData {
    public static final int NODE_DAMAGE = 0;
    public static final int NODE_RANGE = 1;
    public static final int NODE_FIRE_RATE = 2;
    public static final int NODE_CRIT = 3;
    public static final int NODE_PIERCE = 4;    // "Xuyên Giáp" - thêm % sát thương lên Boss/Siêu Boss
    public static final int NODE_MULTISHOT = 5; // "Bắn Lan" - % cơ hội bắn thêm 1 mũi vào địch gần đó
    public static final int NODE_COUNT = 6;
    public static final int MAX_LEVEL = 5;

    public static final String[] NODE_NAMES = {
            "Tăng Sát Thương", "Tăng Tầm Bắn", "Tốc Độ Bắn",
            "Sát Thương Chí Mạng", "Xuyên Giáp", "Bắn Lan"
    };

    // levels[loại tháp][node] = cấp hiện tại (0-5), mặc định 0, sống theo phiên chơi hiện tại
    private static final int[][] levels = new int[TowerCatalog.COUNT][NODE_COUNT];

    public static int getLevel(int type, int node) {
        return levels[type][node];
    }

    // Node tầng 2 chỉ mở khi node tầng 1 tương ứng đã đạt cấp 2 trở lên
    public static boolean isNodeUnlocked(int type, int node) {
        switch (node) {
            case NODE_CRIT: return getLevel(type, NODE_DAMAGE) >= 2;
            case NODE_PIERCE: return getLevel(type, NODE_RANGE) >= 2;
            case NODE_MULTISHOT: return getLevel(type, NODE_FIRE_RATE) >= 2;
            default: return true;
        }
    }

    // Giá Kim Cương để nâng node từ cấp hiện tại lên cấp kế tiếp; -1 nếu đã tối đa
    public static int getCost(int type, int node) {
        int lvl = getLevel(type, node);
        if (lvl >= MAX_LEVEL) return -1;
        boolean tier2 = (node == NODE_CRIT || node == NODE_PIERCE || node == NODE_MULTISHOT);
        return tier2 ? 5 + lvl * 3 : 3 + lvl * 2;
    }

    // Thử nâng 1 node lên 1 cấp - tự trừ Kim Cương nếu đủ điều kiện; trả về true nếu thành công
    public static boolean tryUpgrade(int type, int node) {
        if (!isNodeUnlocked(type, node)) return false;
        int lvl = getLevel(type, node);
        if (lvl >= MAX_LEVEL) return false;
        int cost = getCost(type, node);
        if (!GameProgress.spendGems(cost)) return false;
        levels[type][node] = lvl + 1;
        return true;
    }

    public static boolean isElite(int type) {
        for (int n = 0; n < NODE_COUNT; n++) {
            if (levels[type][n] < MAX_LEVEL) return false;
        }
        return true;
    }

    public static int totalInvestedLevels(int type) {
        int total = 0;
        for (int n = 0; n < NODE_COUNT; n++) total += levels[type][n];
        return total;
    }

    // Cấp độ tổng quát hiển thị trong danh sách tháp (1 = chưa nâng gì)
    public static int overallLevel(int type) {
        return 1 + totalInvestedLevels(type);
    }

    // --- Hệ số cộng dồn áp dụng lên chỉ số GỐC (TowerCatalog) khi tạo tháp mới trong GamePanel ---

    public static double damageMultiplier(int type) {
        double mult = 1.0 + getLevel(type, NODE_DAMAGE) * 0.08;
        if (isElite(type)) mult += 0.15;
        return mult;
    }

    public static int rangeBonus(int type) {
        int bonus = getLevel(type, NODE_RANGE) * 6;
        if (isElite(type)) bonus += 10;
        return bonus;
    }

    // Nhân trực tiếp vào fireRate (số khung hình giữa 2 lần bắn) - số CÀNG NHỎ nghĩa là bắn CÀNG NHANH
    public static double fireRateMultiplier(int type) {
        double mult = Math.pow(0.94, getLevel(type, NODE_FIRE_RATE));
        if (isElite(type)) mult *= 0.9;
        return mult;
    }

    public static double critChance(int type) {
        return getLevel(type, NODE_CRIT) * 0.06;
    }

    public static double bossBonusDamageRatio(int type) {
        return getLevel(type, NODE_PIERCE) * 0.10;
    }

    public static double multishotChance(int type) {
        return getLevel(type, NODE_MULTISHOT) * 0.06;
    }

    // ================== Chỉ dùng bởi Admin Panel (Ctrl+Shift+A) để test nhanh ==================

    // Đưa cả 6 node của TẤT CẢ loại tháp lên cấp tối đa ngay lập tức (mọi tháp thành Tinh Nhuệ), không tốn Kim Cương
    public static void adminMaxAllTowers() {
        for (int t = 0; t < TowerCatalog.COUNT; t++) {
            for (int n = 0; n < NODE_COUNT; n++) {
                levels[t][n] = MAX_LEVEL;
            }
        }
    }

    // Đặt lại toàn bộ nâng cấp tháp về 0 (dùng khi Admin bấm "RESET toàn bộ tiến trình")
    public static void adminResetAllTowers() {
        for (int t = 0; t < TowerCatalog.COUNT; t++) {
            for (int n = 0; n < NODE_COUNT; n++) {
                levels[t][n] = 0;
            }
        }
    }
}