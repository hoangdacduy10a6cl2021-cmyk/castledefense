/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package castledefense;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;

/**
 * Tự tổng hợp 1 giai điệu ngắn (thang ngũ cung, kiểu chiptune nhẹ nhàng) rồi lặp lại vô hạn
 * làm nhạc chờ cho các màn hình menu - không dùng file nhạc nào. Âm lượng lấy theo
 * GameSettings.musicVolume, tắt hẳn khi GameSettings.soundEnabled = false.
 *
 * start()/stop() có thể gọi lại nhiều lần an toàn (start khi đã phát thì bỏ qua, stop khi
 * chưa phát cũng không lỗi) - GameFrame gọi start() mỗi lần hiện màn menu, stop() khi vào chơi.
 */
public class Musicplayer {
    private static final float SAMPLE_RATE = 44100f;
    private static final int NOTE_DURATION_MS = 420;

    // Giai điệu ngũ cung đơn giản, lặp vòng - đủ nhẹ nhàng để nghe lâu không chán ở màn menu
    private static final double[] MELODY = {
            261.63, 293.66, 349.23, 392.00, 440.00, 392.00, 349.23, 293.66,
            261.63, 329.63, 392.00, 440.00, 493.88, 440.00, 392.00, 329.63
    };

    private static byte[][] noteBuffers;
    private static Thread musicThread;
    private static volatile boolean playing = false;

    public static synchronized void start() {
        if (playing) return;
        if (noteBuffers == null) noteBuffers = buildNoteBuffers();
        playing = true;
        musicThread = new Thread(Musicplayer::playLoop);
        musicThread.setDaemon(true);
        musicThread.start();
    }

    public static synchronized void stop() {
        playing = false;
    }

    private static byte[][] buildNoteBuffers() {
        byte[][] buffers = new byte[MELODY.length][];
        for (int i = 0; i < MELODY.length; i++) {
            buffers[i] = buildNote(MELODY[i]);
        }
        return buffers;
    }

    // Tổng hợp 1 nốt nhạc: sóng sine cơ bản pha thêm hoạ âm bậc 2 nhẹ cho đỡ khô như tiếng bíp thuần
    private static byte[] buildNote(double freqHz) {
        int n = (int) (SAMPLE_RATE * NOTE_DURATION_MS / 1000.0);
        byte[] buf = new byte[n];
        int fade = Math.min(n / 8, 900);
        for (int i = 0; i < n; i++) {
            double env = 1.0;
            if (i < fade) env = (double) i / fade;
            else if (i > n - fade) env = (double) (n - i) / fade;
            double angle = 2.0 * Math.PI * i * freqHz / SAMPLE_RATE;
            double sample = Math.sin(angle) * 0.7 + Math.sin(angle * 2) * 0.2;
            buf[i] = (byte) (40 * env * sample);
        }
        return buf;
    }

    // Phát lần lượt từng nốt, lặp vòng vô hạn; kiểm tra âm lượng/tắt tiếng trước mỗi nốt (~0.4s)
    // nên khi người chơi chỉnh thanh trượt hoặc tắt tiếng trong Tuỳ Chọn, nhạc phản hồi gần như ngay.
    private static void playLoop() {
        try {
            AudioFormat format = new AudioFormat(SAMPLE_RATE, 8, 1, true, true);
            SourceDataLine line = AudioSystem.getSourceDataLine(format);
            line.open(format);
            line.start();

            int idx = 0;
            while (playing) {
                byte[] note = noteBuffers[idx % noteBuffers.length];
                idx++;

                if (!Gamesettings.soundEnabled || Gamesettings.musicVolume <= 0) {
                    Thread.sleep(NOTE_DURATION_MS); // vẫn giữ đúng nhịp để bật lại không bị lệch giai điệu
                    continue;
                }

                double vol = Math.max(0, Math.min(1.0, Gamesettings.musicVolume / 100.0));
                byte[] scaled = new byte[note.length];
                for (int i = 0; i < note.length; i++) {
                    scaled[i] = (byte) (note[i] * vol);
                }
                line.write(scaled, 0, scaled.length);
            }

            line.drain();
            line.close();
        } catch (Exception ignored) {
            // Máy không có thiết bị phát âm thanh thì bỏ qua, không được làm crash game
        }
    }
}