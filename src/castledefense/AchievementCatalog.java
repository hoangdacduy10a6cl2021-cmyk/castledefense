/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package castledefense;

/**
 * Dữ liệu tĩnh của toàn bộ Thành Tích: tên, mô tả, nhóm, mục tiêu, thưởng Kim Cương.
 * Tiến trình (getProgress) được tính trực tiếp từ thống kê trọn đời lưu trong GameProgress,
 * không lưu trùng lặp ở đây - luôn phản ánh đúng số liệu hiện tại.
 */
public class AchievementCatalog {
    public static final int CAT_MODE = 0;
    public static final int CAT_COMBAT = 1;
    public static final int CAT_DEFENSE = 2;
    public static final int CAT_OTHER = 3;
    public static final int CATEGORY_COUNT = 4;
    public static final String[] CATEGORY_NAMES = {"Chế Độ Chơi", "Chiến Đấu", "Phòng Thủ", "Khác"};

    public static final int CHAPTER1_WIN = 0;
    public static final int CHAPTER1_ALL_STARS = 1;
    public static final int SURVIVE_100_WAVES = 2;
    public static final int KILL_500 = 3;
    public static final int KILL_2000 = 4;
    public static final int WIN_10_BATTLES = 5;
    public static final int WIN_30_BATTLES = 6;
    public static final int SURVIVE_20_WAVES = 7;
    public static final int SURVIVE_50_WAVES = 8;
    public static final int BUILD_50_TOWERS = 9;
    public static final int GOLD_1000 = 10;
    public static final int GOLD_5000 = 11;
    public static final int BUY_5_ITEMS = 12;
    public static final int COUNT = 13;

    public static final int[] CATEGORY = {
            CAT_MODE, CAT_MODE, CAT_MODE,
            CAT_COMBAT, CAT_COMBAT, CAT_COMBAT, CAT_COMBAT,
            CAT_DEFENSE, CAT_DEFENSE, CAT_DEFENSE,
            CAT_OTHER, CAT_OTHER, CAT_OTHER
    };

    public static final String[] NAMES = {
            "Chiến Thắng Chương 1", "Trọn Vẹn Chương 1", "Bậc Thầy Sinh Tồn",
            "Tiêu Diệt 500 Quái Vật", "Tiêu Diệt 2000 Quái Vật", "Thắng 10 Trận", "Thắng 30 Trận",
            "Sống Sót Qua 20 Đợt", "Sống Sót Qua 50 Đợt", "Xây Dựng 50 Tháp",
            "Thu Thập 1000 Vàng", "Thu Thập 5000 Vàng", "Nhà Sưu Tầm"
    };

    public static final String[] DESCS = {
            "Hoàn thành cả 6 màn của Chương 1 với ít nhất 1 sao mỗi màn.",
            "Đạt 3 sao ở cả 6 màn của Chương 1.",
            "Sống sót tổng cộng 100 đợt quái vật, cộng dồn mọi lượt chơi.",
            "Tiêu diệt tổng cộng 500 quái vật.",
            "Tiêu diệt tổng cộng 2000 quái vật.",
            "Hoàn thành 10 trận đấu (màn cố định) bất kỳ.",
            "Hoàn thành 30 trận đấu (màn cố định) bất kỳ.",
            "Sống sót tổng cộng 20 đợt tấn công.",
            "Sống sót tổng cộng 50 đợt tấn công.",
            "Đặt tổng cộng 50 tháp phòng thủ.",
            "Tích lũy tổng cộng 1000 vàng kiếm được trong game.",
            "Tích lũy tổng cộng 5000 vàng kiếm được trong game.",
            "Mua tổng cộng 5 lượt vật phẩm ở Cửa Hàng."
    };

    public static final int[] TARGET = {
            1, 3 * 6, 100,
            500, 2000, 10, 30,
            20, 50, 50,
            1000, 5000, 5
    };

    public static final int[] REWARD_GEMS = {
            50, 80, 100,
            30, 80, 50, 120,
            40, 90, 40,
            30, 70, 40
    };

    // Tiến trình hiện tại của 1 thành tích, lấy trực tiếp từ thống kê trọn đời trong GameProgress
    public static int getProgress(int id) {
        long v;
        switch (id) {
            case CHAPTER1_WIN:
                return GameProgress.isChapterFullyCompleted(0) ? 1 : 0;
            case CHAPTER1_ALL_STARS:
                return GameProgress.totalStarsInChapter(0);
            case SURVIVE_100_WAVES:
            case SURVIVE_20_WAVES:
            case SURVIVE_50_WAVES:
                v = GameProgress.getTotalWavesSurvived();
                break;
            case KILL_500:
            case KILL_2000:
                v = GameProgress.getTotalEnemiesKilled();
                break;
            case WIN_10_BATTLES:
            case WIN_30_BATTLES:
                return GameProgress.getTotalBattlesWon();
            case BUILD_50_TOWERS:
                v = GameProgress.getTotalTowersPlaced();
                break;
            case GOLD_1000:
            case GOLD_5000:
                v = GameProgress.getTotalGoldEarnedLifetime();
                break;
            default:
                return GameProgress.getTotalItemsPurchased();
        }
        return (int) Math.min(Integer.MAX_VALUE, v);
    }

    // --- Danh hiệu xếp theo tổng điểm thành tích (tổng Kim Cương của các thành tích ĐÃ NHẬN) ---
    private static final String[] RANK_NAMES = {
            "Tân Binh", "Chiến Binh Tân Thủ", "Chiến Binh Dày Dạn", "Anh Hùng Phòng Thủ", "Huyền Thoại Bất Tử"
    };
    private static final int[] RANK_THRESHOLDS = {0, 100, 300, 600, 1000};

    public static String rankTitle(int points) {
        String rank = RANK_NAMES[0];
        for (int i = 0; i < RANK_THRESHOLDS.length; i++) {
            if (points >= RANK_THRESHOLDS[i]) rank = RANK_NAMES[i];
        }
        return rank;
    }

    // Ngưỡng điểm cần để lên danh hiệu kế tiếp; nếu đã đạt hạng cao nhất thì trả về chính điểm hiện tại
    public static int nextRankThreshold(int points) {
        for (int t : RANK_THRESHOLDS) {
            if (points < t) return t;
        }
        return Math.max(points, RANK_THRESHOLDS[RANK_THRESHOLDS.length - 1]);
    }

    // Mốc dưới của hạng hiện tại, dùng để vẽ thanh tiến trình "điểm trong hạng này / cần để lên hạng"
    public static int currentRankFloor(int points) {
        int floor = 0;
        for (int t : RANK_THRESHOLDS) {
            if (points >= t) floor = t;
        }
        return floor;
    }
}