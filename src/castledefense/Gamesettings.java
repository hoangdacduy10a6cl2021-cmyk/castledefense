/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package castledefense;

import java.awt.event.KeyEvent;

/**
 * Lưu toàn bộ thiết lập của người chơi (Đồ Hoạ/Âm Thanh/Điều Khiển) trong phiên chơi hiện tại.
 * Tĩnh (static) để mọi màn hình cùng đọc/ghi chung 1 nơi, không cần truyền qua lại giữa các panel.
 */
public class Gamesettings {
    // --- Đồ Hoạ ---
    public static boolean effectsEnabled = true; // bật/tắt hiệu ứng hình ảnh của 3 kỹ năng Lửa/Sét/Băng

    // --- Âm Thanh ---
    public static int musicVolume = 70; // 0-100
    public static int sfxVolume = 80;   // 0-100
    public static boolean soundEnabled = true; // tắt tiếng tổng (mute)

    // --- Điều Khiển ---
    public static boolean showHints = true;          // hiện các dòng gợi ý trong lúc chơi
    public static boolean autoSkipWaveDefault = true; // giá trị mặc định của "Tự động qua đợt" khi vào ván mới

    // --- Phím tắt có thể tuỳ chỉnh ---
    public static final int ACTION_TOWER_1 = 0;
    public static final int ACTION_TOWER_2 = 1;
    public static final int ACTION_TOWER_3 = 2;
    public static final int ACTION_TOWER_4 = 3;
    public static final int ACTION_TOWER_5 = 4;
    public static final int ACTION_TOWER_6 = 5;
    public static final int ACTION_TOWER_7 = 6;
    public static final int ACTION_SKILL_FIRE = 7;
    public static final int ACTION_SKILL_LIGHTNING = 8;
    public static final int ACTION_SKILL_ICE = 9;
    public static final int ACTION_SELL_TOWER = 10;
    public static final int ACTION_COUNT = 11;

    public static final String[] ACTION_NAMES = {
            "Chọn: Cung Thủ", "Chọn: Pháp Sư", "Chọn: Đại Bác", "Chọn: Nỏ Liên Hoàn",
            "Chọn: Phù Thủy Độc", "Chọn: Tháp Sét", "Chọn: Trại Lính",
            "Kỹ Năng: Lửa", "Kỹ Năng: Sấm Sét", "Kỹ Năng: Băng", "Bán Tháp Đang Chọn"
    };

    private static final int[] DEFAULT_KEYS = {
            KeyEvent.VK_1, KeyEvent.VK_2, KeyEvent.VK_3, KeyEvent.VK_4,
            KeyEvent.VK_5, KeyEvent.VK_6, KeyEvent.VK_7,
            KeyEvent.VK_Q, KeyEvent.VK_W, KeyEvent.VK_E, KeyEvent.VK_X
    };

    // Không dùng lại các phím này khi đổi, vì đã có chức năng cố định riêng trong game
    private static final int[] RESERVED_KEYS = {
            KeyEvent.VK_SPACE, KeyEvent.VK_ESCAPE, KeyEvent.VK_R
    };

    private static final int[] keys = DEFAULT_KEYS.clone();

    public static int getKey(int action) {
        return keys[action];
    }

    public static String keyName(int action) {
        return KeyEvent.getKeyText(keys[action]);
    }

    public static boolean isReserved(int keyCode) {
        for (int r : RESERVED_KEYS) if (r == keyCode) return true;
        return false;
    }

    // Gán phím mới cho 1 hành động; nếu phím đó đang được hành động khác dùng thì hoán đổi 2 phím cho nhau
    public static void setKey(int action, int keyCode) {
        int oldKey = keys[action];
        for (int i = 0; i < ACTION_COUNT; i++) {
            if (i != action && keys[i] == keyCode) {
                keys[i] = oldKey;
                break;
            }
        }
        keys[action] = keyCode;
    }

    public static void resetKeysToDefault() {
        System.arraycopy(DEFAULT_KEYS, 0, keys, 0, DEFAULT_KEYS.length);
    }
}