/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package castledefense;

/**
 * Lưu tiến trình chơi (số sao mỗi màn) trong phiên chơi hiện tại của cửa sổ game
 * (chưa lưu ra file - đóng game sẽ mất tiến trình). Hiện chỉ Chương 1 có nội dung thật;
 * Chương 2-5 luôn khoá vì chưa có màn chơi.
 */
public class GameProgress {
    public static final int CHAPTER_COUNT = 5;
    public static final int LEVELS_PER_CHAPTER = 6;

    // stars[chương][màn] = số sao đạt được (0 = chưa qua)
    private static final int[][] stars = new int[CHAPTER_COUNT][LEVELS_PER_CHAPTER];

    // Kim Cương - tiền tệ riêng để nâng cấp tháp vĩnh viễn (xem TowerUpgradeData), nhận khi hoàn thành màn
    private static int gems = 0;

    public static int getGems() {
        return gems;
    }

    public static void addGems(int amount) {
        if (amount > 0) gems += amount;
    }

    // Trừ Kim Cương nếu đủ, trả về false nếu không đủ (không trừ gì cả)
    public static boolean spendGems(int amount) {
        if (amount <= 0) return true;
        if (gems < amount) return false;
        gems -= amount;
        return true;
    }

    // --- Kho vật phẩm tiêu hao (mua ở Cửa Hàng, dùng thật trong lúc chơi - xem ItemCatalog) ---
    private static final int[] itemCounts = new int[ItemCatalog.COUNT];

    public static int getItemCount(int itemId) {
        if (itemId < 0 || itemId >= ItemCatalog.COUNT) return 0;
        return itemCounts[itemId];
    }

    // Mua 1 gói vật phẩm bằng Kim Cương; thành công thì cộng thêm số lượng tương ứng vào kho
    public static boolean buyItem(int itemId) {
        if (itemId < 0 || itemId >= ItemCatalog.COUNT) return false;
        if (!spendGems(ItemCatalog.GEM_COST[itemId])) return false;
        itemCounts[itemId] += ItemCatalog.QUANTITY[itemId];
        return true;
    }

    // Dùng 1 lượt vật phẩm (gọi từ GamePanel lúc chơi); trả về false nếu đã hết
    public static boolean useItem(int itemId) {
        if (itemId < 0 || itemId >= ItemCatalog.COUNT || itemCounts[itemId] <= 0) return false;
        itemCounts[itemId]--;
        return true;
    }

    // --- Xem Quảng Cáo giả lập: chỉ được 1 lần mỗi phiên chạy ứng dụng (đóng game sẽ reset) ---
    private static boolean adWatchedThisSession = false;

    public static boolean canWatchAd() {
        return !adWatchedThisSession;
    }

    public static void markAdWatched() {
        adWatchedThisSession = true;
    }

    public static int getStars(int chapter, int level) {
        if (chapter < 0 || chapter >= CHAPTER_COUNT || level < 0 || level >= LEVELS_PER_CHAPTER) return 0;
        return stars[chapter][level];
    }

    // Ghi lại kết quả 1 màn - chỉ cập nhật nếu số sao mới cao hơn kỷ lục cũ đang lưu
    public static void recordResult(int chapter, int level, int starsEarned) {
        if (chapter < 0 || chapter >= CHAPTER_COUNT || level < 0 || level >= LEVELS_PER_CHAPTER) return;
        if (starsEarned > stars[chapter][level]) {
            stars[chapter][level] = starsEarned;
        }
    }

    // Màn đầu của mỗi chương luôn mở; các màn sau chỉ mở khi màn liền trước đã qua (>=1 sao)
    public static boolean isLevelUnlocked(int chapter, int level) {
        if (!isChapterUnlocked(chapter)) return false;
        if (level == 0) return true;
        return stars[chapter][level - 1] > 0;
    }

    // Hiện chỉ Chương 1 có nội dung, các chương sau đang khoá chờ làm thêm
    public static boolean isChapterUnlocked(int chapter) {
        return chapter == 0;
    }

    public static int totalStarsInChapter(int chapter) {
        if (chapter < 0 || chapter >= CHAPTER_COUNT) return 0;
        int total = 0;
        for (int s : stars[chapter]) total += s;
        return total;
    }

    public static int maxStarsInChapter() {
        return LEVELS_PER_CHAPTER * 3;
    }

    // Chương được coi là "đã vượt" khi cả 6 màn đều đạt ít nhất 1 sao
    public static boolean isChapterFullyCompleted(int chapter) {
        if (chapter < 0 || chapter >= CHAPTER_COUNT) return false;
        for (int l = 0; l < LEVELS_PER_CHAPTER; l++) {
            if (stars[chapter][l] <= 0) return false;
        }
        return true;
    }

    public static int chaptersFullyCompletedCount() {
        int count = 0;
        for (int c = 0; c < CHAPTER_COUNT; c++) {
            if (isChapterFullyCompleted(c)) count++;
        }
        return count;
    }

    // Tổng số màn (trên mọi chương) đã đạt ít nhất 1 sao
    public static int totalLevelsCompleted() {
        int count = 0;
        for (int c = 0; c < CHAPTER_COUNT; c++) {
            for (int l = 0; l < LEVELS_PER_CHAPTER; l++) {
                if (stars[c][l] > 0) count++;
            }
        }
        return count;
    }

    // --- Thống kê trọn đời cho hệ thống Thành Tích, cộng dồn qua mọi lượt chơi (kể cả Vô Hạn/Chơi Nhanh) ---
    private static long totalEnemiesKilled = 0;
    private static long totalWavesSurvived = 0;
    private static long totalGoldEarnedLifetime = 0;
    private static int totalBattlesWon = 0;
    private static long totalTowersPlaced = 0;
    private static int totalItemsPurchased = 0;

    public static void addEnemiesKilled(int n) {
        totalEnemiesKilled += n;
    }

    public static void addWavesSurvived(int n) {
        totalWavesSurvived += n;
    }

    public static void addGoldEarned(int n) {
        if (n > 0) totalGoldEarnedLifetime += n;
    }

    public static void addBattleWon() {
        totalBattlesWon++;
    }

    public static void addTowerPlaced() {
        totalTowersPlaced++;
    }

    public static void addItemPurchased() {
        totalItemsPurchased++;
    }

    public static long getTotalEnemiesKilled() {
        return totalEnemiesKilled;
    }

    public static long getTotalWavesSurvived() {
        return totalWavesSurvived;
    }

    public static long getTotalGoldEarnedLifetime() {
        return totalGoldEarnedLifetime;
    }

    public static int getTotalBattlesWon() {
        return totalBattlesWon;
    }

    public static long getTotalTowersPlaced() {
        return totalTowersPlaced;
    }

    public static int getTotalItemsPurchased() {
        return totalItemsPurchased;
    }

    // --- Trạng thái đã nhận thưởng của từng Thành Tích (xem AchievementCatalog) ---
    private static final boolean[] achievementClaimed = new boolean[AchievementCatalog.COUNT];

    public static boolean isAchievementClaimed(int id) {
        if (id < 0 || id >= AchievementCatalog.COUNT) return false;
        return achievementClaimed[id];
    }

    // Nhận thưởng 1 thành tích đã đủ điều kiện - cộng Kim Cương và đánh dấu đã nhận
    public static boolean claimAchievement(int id) {
        if (id < 0 || id >= AchievementCatalog.COUNT) return false;
        if (achievementClaimed[id]) return false;
        if (AchievementCatalog.getProgress(id) < AchievementCatalog.TARGET[id]) return false;
        achievementClaimed[id] = true;
        addGems(AchievementCatalog.REWARD_GEMS[id]);
        return true;
    }

    public static int claimedAchievementCount() {
        int count = 0;
        for (boolean b : achievementClaimed) if (b) count++;
        return count;
    }

    // Tổng điểm thành tích = tổng Kim Cương thưởng của các thành tích ĐÃ NHẬN, dùng để xếp Danh Hiệu
    public static int totalAchievementPoints() {
        int total = 0;
        for (int i = 0; i < AchievementCatalog.COUNT; i++) {
            if (achievementClaimed[i]) total += AchievementCatalog.REWARD_GEMS[i];
        }
        return total;
    }

    // ================== Chỉ dùng bởi Admin Panel (Ctrl+Shift+A) để test nhanh ==================

    // Cộng thêm 1 số lượng cố định vào MỌI loại vật phẩm trong kho, không tốn Kim Cương
    public static void adminGrantAllItems(int amountEach) {
        for (int i = 0; i < ItemCatalog.COUNT; i++) {
            itemCounts[i] += amountEach;
        }
    }

    // Đặt tất cả 6 màn của Chương 1 (chương duy nhất có nội dung) về 3 sao, coi như đã hoàn thành trọn vẹn
    public static void adminUnlockAllLevelsWithStars() {
        for (int l = 0; l < LEVELS_PER_CHAPTER; l++) {
            stars[0][l] = 3;
        }
    }

    // Xoá sạch toàn bộ tiến trình: sao/màn, Kim Cương, kho vật phẩm, thành tích đã nhận, thống kê trọn đời
    public static void adminResetProgress() {
        for (int c = 0; c < CHAPTER_COUNT; c++) {
            for (int l = 0; l < LEVELS_PER_CHAPTER; l++) {
                stars[c][l] = 0;
            }
        }
        gems = 0;
        for (int i = 0; i < itemCounts.length; i++) itemCounts[i] = 0;
        for (int i = 0; i < achievementClaimed.length; i++) achievementClaimed[i] = false;
        totalEnemiesKilled = 0;
        totalWavesSurvived = 0;
        totalGoldEarnedLifetime = 0;
        totalBattlesWon = 0;
        totalTowersPlaced = 0;
        totalItemsPurchased = 0;
        adWatchedThisSession = false;
    }
}