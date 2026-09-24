/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package castledefense;

import java.awt.Color;

/**
 * Dữ liệu 6 loại vật phẩm tiêu hao mua bằng Kim Cương ở Cửa Hàng, dùng thật trong lúc chơi.
 * Mỗi lần mua nhận thêm QUANTITY[id] lượt dùng, lưu vào kho đồ vĩnh viễn trong GameProgress.
 */
public class ItemCatalog {
    public static final int HEAL = 0;
    public static final int BUILD_BOOST = 1;
    public static final int BOMB = 2;
    public static final int FROST = 3;
    public static final int SHIELD = 4;
    public static final int REVIVE = 5;
    public static final int COUNT = 6;

    public static final String[] NAMES = {"Hồi Máu", "Tăng Tốc Xây", "Bom", "Băng Giá", "Khiên", "Hồi Sinh"};

    public static final String[] DESCS = {
            "Hồi ngay 30% máu tối đa của thành.",
            "Nhận ngay 100 vàng để xây tháp nhanh hơn.",
            "Sát thương diện rộng cực mạnh lên toàn bộ quái đang có mặt trên bản đồ.",
            "Làm chậm mạnh toàn bộ quái vật trong một khoảng thời gian.",
            "Chặn 5 điểm sát thương tiếp theo lên thành (không tốn máu).",
            "Tự động hồi sinh 1 lần nếu thành thất thủ, hồi lại 30% máu tối đa."
    };

    public static final int[] QUANTITY = {5, 3, 5, 3, 3, 1};
    public static final int[] GEM_COST = {20, 30, 40, 30, 40, 50};

    public static final Color[] COLOR = {
            new Color(210, 70, 70), new Color(200, 150, 60), new Color(60, 60, 65),
            new Color(140, 200, 240), new Color(90, 140, 210), new Color(230, 210, 140)
    };
}