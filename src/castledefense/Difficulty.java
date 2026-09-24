/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package castledefense;

/**
 * Độ khó của 1 lượt chơi: nhân hệ số vào máu/tốc độ quái vật,
 * và quyết định số vàng thưởng khi hoàn thành màn.
 * STANDARD chỉ dùng nội bộ cho chế độ "Chơi Nhanh" (nút CHƠI), không hiện trong màn chọn độ khó.
 */
public enum Difficulty {
    STANDARD(1.0, 1.0, 0, 0, "Tiêu Chuẩn", "Chơi nhanh"),
    EASY(0.75, 0.92, 100, 1, "EASY", "Kẻ địch yếu"),
    HARD(1.35, 1.05, 200, 2, "HARD", "Kẻ địch mạnh"),
    NIGHTMARE(1.9, 1.15, 300, 3, "NIGHTMARE", "Kẻ địch cực mạnh");

    public final double hpMult;
    public final double speedMult;
    public final int completionReward;
    public final int gemsPerStar; // Kim Cương nhận mỗi sao đạt được khi hoàn thành màn ở độ khó này
    public final String label;
    public final String desc;

    Difficulty(double hpMult, double speedMult, int completionReward, int gemsPerStar, String label, String desc) {
        this.hpMult = hpMult;
        this.speedMult = speedMult;
        this.completionReward = completionReward;
        this.gemsPerStar = gemsPerStar;
        this.label = label;
        this.desc = desc;
    }
}