/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package castledefense;

/**
 * Cấu hình 1 màn chơi: số đợt cần vượt qua để hoàn thành, công thức máu/tốc độ quái
 * tăng dần theo đợt, số quái mỗi đợt, và vàng khởi đầu. Mỗi màn 1-1..1-6 có 1 cấu hình riêng.
 */
public class LevelConfig {
    public final String id;            // "1-1", "quick", "1-vohan"...
    public final String displayName;   // tên hiển thị khi hoàn thành / trong tiêu đề màn hình
    public final int totalWaves;       // số đợt để hoàn thành màn (số rất lớn nếu là Vô Hạn)
    public final boolean endless;      // true = không có điểm kết, chơi tới khi thành thất thủ
    public final int baseEnemyHp;
    public final int hpPerWave;
    public final double baseEnemySpeed;
    public final double speedPerWave;
    public final int enemiesBase;
    public final int enemiesPerWave;
    public final int startGold;

    public LevelConfig(String id, String displayName, int totalWaves, boolean endless,
                        int baseEnemyHp, int hpPerWave, double baseEnemySpeed, double speedPerWave,
                        int enemiesBase, int enemiesPerWave, int startGold) {
        this.id = id;
        this.displayName = displayName;
        this.totalWaves = totalWaves;
        this.endless = endless;
        this.baseEnemyHp = baseEnemyHp;
        this.hpPerWave = hpPerWave;
        this.baseEnemySpeed = baseEnemySpeed;
        this.speedPerWave = speedPerWave;
        this.enemiesBase = enemiesBase;
        this.enemiesPerWave = enemiesPerWave;
        this.startGold = startGold;
    }

    // Cấu hình cho nút CHƠI ở Menu chính - chơi nhanh, giữ nguyên hệt như bản gốc của game
    public static LevelConfig quickPlay() {
        return new LevelConfig("quick", "Ải Chơi Nhanh", 15, false, 30, 12, 1.4, 0.05, 5, 2, 150);
    }

    // 6 màn cố định của Chương 1, độ khó nền tăng dần theo từng màn
    public static final LevelConfig[] CHAPTER_1_LEVELS = {
            new LevelConfig("1-1", "Màn 1-1", 6, false, 26, 8, 1.30, 0.040, 4, 1, 160),
            new LevelConfig("1-2", "Màn 1-2", 7, false, 30, 9, 1.35, 0.045, 5, 1, 155),
            new LevelConfig("1-3", "Màn 1-3", 8, false, 34, 10, 1.40, 0.050, 5, 2, 150),
            new LevelConfig("1-4", "Màn 1-4", 9, false, 38, 11, 1.45, 0.050, 6, 2, 150),
            new LevelConfig("1-5", "Màn 1-5", 10, false, 42, 12, 1.50, 0.055, 6, 2, 145),
            new LevelConfig("1-6", "Màn 1-6", 12, false, 46, 13, 1.55, 0.060, 7, 2, 140),
    };

    // Chế độ Vô Hạn của Chương 1 - không có "đợt cuối", chơi tới khi thành thất thủ
    public static LevelConfig chapter1Endless() {
        return new LevelConfig("1-vohan", "Vô Hạn", 1_000_000, true, 30, 11, 1.4, 0.05, 5, 2, 150);
    }
}