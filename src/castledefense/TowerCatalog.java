/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package castledefense;

import java.awt.Color;

/**
 * Bảng dữ liệu GỐC (chưa cộng nâng cấp) của 7 loại tháp, dùng chung cho:
 * - GamePanel: đặt tháp mới trong lúc chơi (cộng thêm hệ số từ TowerUpgradeData)
 * - UpgradePanel: hiển thị tên/mô tả/chỉ số trước-sau khi nâng cấp
 * Index 0-6 tương ứng: Cung Thủ, Pháp Sư, Đại Bác, Nỏ Liên Hoàn, Phù Thủy Độc, Tháp Sét, Trại Lính.
 */
public class TowerCatalog {
    public static final int COUNT = 7;

    public static final String[] NAMES = {
            "Cung Thủ", "Pháp Sư", "Đại Bác", "Nỏ Liên Hoàn", "Phù Thủy Độc", "Tháp Sét", "Trại Lính"
    };

    public static final String[] DESCS_SHORT = {
            "Cân bằng", "Bắn nhanh", "Sát thương cao", "Xuyên lan", "Gây độc", "Sét lan", "Triệu hồi lính"
    };

    public static final String[] DESCS_LONG = {
            "Tháp cân bằng, 2 cung thủ đứng trên đỉnh tháp bắn tên vào quái vật.",
            "Tháp bắn nhanh, pháp sư tung cầu phép gây sát thương liên tục.",
            "Sát thương cực cao nhưng bắn chậm, pháo thủ nạp đạn từng phát.",
            "Bắn nhanh, mũi tên lan thêm sát thương sang địch đứng gần điểm trúng.",
            "Sát thương thấp nhưng gây độc, quái mất máu dần theo thời gian.",
            "Tia sét lan sang tối đa 2 địch đứng gần mục tiêu.",
            "Định kỳ triệu hồi lính cận chiến ra tuần tra quanh trại."
    };

    public static final int[] BASE_RANGE = {115, 140, 100, 130, 120, 140, 150};
    public static final int[] BASE_DAMAGE = {18, 8, 45, 14, 6, 10, 12};
    public static final int[] BASE_FIRE_RATE = {35, 14, 70, 20, 45, 30, 90};
    public static final int[] COST = {50, 90, 130, 120, 100, 150, 140};

    public static final Color[] COLOR = {
            new Color(190, 170, 140), new Color(80, 200, 210), new Color(160, 60, 50),
            new Color(200, 190, 170), new Color(90, 170, 70), new Color(150, 190, 230),
            new Color(150, 60, 55)
    };
}