/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package castledefense;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tự tổng hợp âm thanh cho game bằng toán học (không dùng file nhạc/âm thanh nào):
 * - buildTone: 1 nốt sine thuần, dùng cho tiếng "tít" đơn giản (click, thông báo).
 * - buildSweep: quét tần số theo thời gian + rung (vibrato) + pha nhiễu, dùng để giả lập
 *   tiếng rít/tia điện/gầm gừ thay vì chỉ 1 tiếng bíp phẳng.
 * - buildImpact: nhiễu trắng lọc thô + 1 nhịp trầm bên dưới, dùng cho tiếng nổ/va chạm.
 * Mỗi tiếng phát trên 1 thread daemon riêng để không làm khựng game; có giới hạn tần suất
 * theo từng "kênh" để tránh dồn quá nhiều tiếng cùng lúc khi nhiều sự kiện xảy ra liên tiếp.
 */
public class AudioEngine {
    private static final float SAMPLE_RATE = 44100f;
    private static final Random RNG = new Random();
    private static final Map<String, Long> lastPlayed = new ConcurrentHashMap<>();

    private static boolean allowPlay(String channel, int minGapMs) {
        long now = System.currentTimeMillis();
        Long last = lastPlayed.get(channel);
        if (last != null && now - last < minGapMs) return false;
        lastPlayed.put(channel, now);
        return true;
    }

    private static boolean canPlay() {
        return Gamesettings.soundEnabled && Gamesettings.sfxVolume > 0;
    }

    private static double vol() {
        return Math.max(0, Math.min(1.0, Gamesettings.sfxVolume / 100.0));
    }

    private static double envelope(int i, int n, int fade) {
        if (i < fade) return (double) i / fade;
        if (i > n - fade) return (double) (n - i) / fade;
        return 1.0;
    }

    private static byte[] buildTone(double freqHz, int durationMs, double amplitude01) {
        int n = (int) (SAMPLE_RATE * durationMs / 1000.0);
        byte[] buf = new byte[n];
        int fade = Math.min(n / 6, 400);
        double amp = amplitude01 * 90;
        for (int i = 0; i < n; i++) {
            double env = envelope(i, n, fade);
            double angle = 2.0 * Math.PI * i * freqHz / SAMPLE_RATE;
            buf[i] = (byte) (amp * env * Math.sin(angle));
        }
        return buf;
    }

    // Quét tần số startFreq -> endFreq, có thể pha rung (vibrato) và nhiễu (noiseMix 0-1)
    // để tạo cảm giác gầm gừ/rít/tia điện thay vì chỉ 1 tiếng bíp phẳng.
    private static byte[] buildSweep(double startFreq, double endFreq, int durationMs, double amplitude01,
                                      double vibratoHz, double vibratoDepth, double noiseMix) {
        int n = (int) (SAMPLE_RATE * durationMs / 1000.0);
        byte[] buf = new byte[n];
        int fade = Math.min(n / 6, 500);
        double amp = amplitude01 * 90;
        double phase = 0;
        for (int i = 0; i < n; i++) {
            double t = (double) i / n;
            double freq = startFreq + (endFreq - startFreq) * t;
            if (vibratoDepth > 0) {
                freq += Math.sin(2 * Math.PI * vibratoHz * i / SAMPLE_RATE) * vibratoDepth;
            }
            phase += 2.0 * Math.PI * freq / SAMPLE_RATE;
            double env = envelope(i, n, fade);
            double tone = Math.sin(phase);
            double noise = RNG.nextDouble() * 2 - 1;
            double sample = tone * (1 - noiseMix) + noise * noiseMix;
            buf[i] = (byte) (amp * env * sample);
        }
        return buf;
    }

    // Nhiễu trắng lọc thô (trung bình trượt cho bớt chói) + 1 nhịp trầm bên dưới, dùng cho tiếng nổ/va chạm
    private static byte[] buildImpact(int durationMs, double amplitude01, double lowThumpFreq) {
        int n = (int) (SAMPLE_RATE * durationMs / 1000.0);
        byte[] buf = new byte[n];
        int fade = Math.min(n / 5, 300);
        double amp = amplitude01 * 90;
        double prevNoise = 0;
        for (int i = 0; i < n; i++) {
            double env = envelope(i, n, fade);
            double noise = RNG.nextDouble() * 2 - 1;
            double filtered = (noise + prevNoise) / 2.0;
            prevNoise = noise;
            double thump = lowThumpFreq > 0 ? Math.sin(2 * Math.PI * i * lowThumpFreq / SAMPLE_RATE) * 0.6 : 0;
            buf[i] = (byte) (amp * env * (filtered * 0.7 + thump));
        }
        return buf;
    }

    private static void play(byte[] buffer) {
        Thread t = new Thread(() -> {
            try {
                AudioFormat format = new AudioFormat(SAMPLE_RATE, 8, 1, true, true);
                try (SourceDataLine line = AudioSystem.getSourceDataLine(format)) {
                    line.open(format);
                    line.start();
                    line.write(buffer, 0, buffer.length);
                    line.drain();
                }
            } catch (Exception ignored) {
                // Máy không có thiết bị phát âm thanh thì bỏ qua, không được làm crash game
            }
        });
        t.setDaemon(true);
        t.start();
    }

    public static void playTone(double freqHz, int durationMs) {
        if (!canPlay()) return;
        play(buildTone(freqHz, durationMs, vol()));
    }

    // --- Hiệu ứng âm thanh cho các sự kiện giao diện chung ---
    public static void playClick() {
        if (canPlay()) play(buildTone(880, 55, vol()));
    }

    public static void playPlaceTower() {
        if (canPlay() && allowPlay("place", 30)) play(buildTone(520, 90, vol()));
    }

    public static void playSellTower() {
        if (canPlay()) play(buildSweep(500, 260, 140, vol(), 0, 0, 0.15));
    }

    public static void playWaveStart() {
        if (canPlay()) play(buildSweep(500, 900, 220, vol(), 0, 0, 0.05));
    }

    public static void playSkillFire() {
        if (canPlay()) play(buildImpact(320, vol(), 90));
    }

    public static void playSkillLightning() {
        if (canPlay()) play(buildSweep(1600, 400, 200, vol(), 40, 300, 0.35));
    }

    public static void playSkillIce() {
        if (canPlay()) play(buildSweep(1200, 700, 280, vol(), 0, 0, 0.15));
    }

    public static void playCastleHit() {
        if (canPlay() && allowPlay("castlehit", 60)) play(buildImpact(90, vol(), 140));
    }

    public static void playUpgradeSuccess() {
        if (canPlay()) play(buildSweep(700, 1100, 160, vol(), 0, 0, 0));
    }

    public static void playError() {
        if (canPlay()) play(buildTone(150, 90, vol()));
    }

    public static void playVictory() {
        if (canPlay()) play(buildSweep(600, 1100, 260, vol(), 0, 0, 0));
    }

    public static void playDefeat() {
        if (canPlay()) play(buildSweep(300, 90, 450, vol(), 5, 20, 0.1));
    }

    // --- Tiếng bắn riêng cho từng loại tháp, phát mỗi khi tháp khai hoả (t.fire()) ---
    public static void playShoot(int towerType) {
        if (!canPlay() || !allowPlay("shoot", 35)) return;
        switch (towerType) {
            case 0: play(buildTone(1400, 40, vol() * 0.7)); break;                     // Cung Thủ - dây cung "phựt"
            case 1: play(buildSweep(900, 1300, 90, vol() * 0.8, 0, 0, 0.1)); break;     // Pháp Sư - phép chói
            case 2: play(buildImpact(160, vol(), 70)); break;                          // Đại Bác - "ẦM"
            case 3: play(buildTone(1100, 35, vol() * 0.7)); break;                     // Nỏ Liên Hoàn
            case 4: play(buildSweep(300, 220, 110, vol() * 0.7, 18, 25, 0.3)); break;   // Phù Thủy Độc - gợn độc
            case 5: play(buildSweep(1800, 2600, 60, vol() * 0.8, 0, 0, 0.5)); break;    // Tháp Sét - tia điện rè
            default: break; // Trại Lính không bắn đạn
        }
    }

    // --- Tiếng quái vật ---
    // Gầm gừ trầm khi 1 quái xuất hiện; boss/siêu boss gầm trầm và dài hơn hẳn quái thường
    public static void playEnemyGrowl(boolean boss, boolean superBoss) {
        if (!canPlay()) return;
        if (superBoss) {
            if (!allowPlay("growl", 0)) return;
            play(buildSweep(140, 90, 520, vol(), 7, 18, 0.25));
        } else if (boss) {
            if (!allowPlay("growl", 150)) return;
            play(buildSweep(160, 110, 340, vol() * 0.9, 8, 15, 0.2));
        } else {
            if (!allowPlay("growl", 260)) return;
            play(buildSweep(220, 160, 160, vol() * 0.55, 10, 10, 0.2));
        }
    }

    // Tiếng "hự" ngắn khi 1 quái bị hạ gục
    public static void playEnemyDeath() {
        if (!canPlay() || !allowPlay("death", 45)) return;
        play(buildSweep(260, 90, 130, vol() * 0.6, 0, 0, 0.35));
    }

    // Tiếng va chạm ngắn khi lính Trại Lính giao chiến giáp lá cà với quái
    public static void playSoldierClash() {
        if (!canPlay() || !allowPlay("clash", 90)) return;
        play(buildImpact(60, vol() * 0.6, 0));
    }
}