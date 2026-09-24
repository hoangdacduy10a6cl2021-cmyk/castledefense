/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package castledefense;

import javax.swing.*;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.util.function.BiConsumer;

/**
 * GameFrame là cửa sổ (JFrame) chứa toàn bộ game, điều phối các màn hình:
 * Menu chính -> (Chọn Chương -> Chọn Màn -> Chọn Độ Khó) -> GamePanel.
 * Nút CHƠI ở Menu chính đi thẳng vào chơi nhanh, không qua hệ thống Chương/Màn.
 */
public class GameFrame extends JFrame {
    // Theo dõi GamePanel đang hiển thị (nếu có) để Admin Panel biết có ván nào đang chơi hay không
    private GamePanel activeGamePanel = null;
    private boolean adminPanelOpen = false;

    public GameFrame() {
        setTitle("Thủ Thành - Castle Defense 2D");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        // Phím tắt bí mật Ctrl+Shift+A mở Admin Panel từ BẤT KỲ màn hình nào trong game,
        // đăng ký ở cấp toàn cục (KeyEventDispatcher) nên không cần sửa từng panel riêng lẻ.
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(e -> {
            if (e.getID() == KeyEvent.KEY_PRESSED && e.getKeyCode() == KeyEvent.VK_A
                    && e.isControlDown() && e.isShiftDown()) {
                showAdminPanel();
                return true; // đã xử lý, không cho lan xuống panel đang focus
            }
            return false;
        });

        showMainMenu();
    }

    // Đặt panel mới làm nội dung cửa sổ, căn giữa lại và focus để bắt được phím ngay
    private void showPanel(JPanel panel, Runnable afterShown) {
        activeGamePanel = (panel instanceof GamePanel) ? (GamePanel) panel : null;

        // Nhạc chờ chỉ phát ở các màn menu, tự tắt khi vào chơi thật (GamePanel)
        if (panel instanceof GamePanel) {
            Musicplayer.stop();
        } else {
            Musicplayer.start();
        }

        setContentPane(panel);
        pack();
        setLocationRelativeTo(null);
        revalidate();
        panel.requestFocusInWindow();
        if (afterShown != null) SwingUtilities.invokeLater(afterShown);
    }

    // Mở cửa sổ Admin (cheat/test) - tạm dừng game đang chạy (nếu có) trong lúc mở, resume khi đóng
    private void showAdminPanel() {
        if (adminPanelOpen) return;
        adminPanelOpen = true;

        boolean wasRunning = activeGamePanel != null;
        if (wasRunning) activeGamePanel.pauseForAdmin();

        AdminPanel dialog = new AdminPanel(this, activeGamePanel);
        dialog.setVisible(true); // modal - chặn tới khi người dùng đóng cửa sổ này

        if (wasRunning) activeGamePanel.resumeFromAdmin();
        adminPanelOpen = false;
        getContentPane().repaint();
    }

    private void showMainMenu() {
        MainMenuPanel menu = new MainMenuPanel(this::startQuickPlay, this::showChapterSelect, this::showUpgradePanel, this::showOptions, this::showShop, this::showAchievements);
        showPanel(menu, null);
    }

    private void showAchievements() {
        Achievementspanel panel = new Achievementspanel(this::showMainMenu);
        showPanel(panel, null);
    }

    private void showShop() {
        ShopPanel panel = new ShopPanel(this::showMainMenu);
        showPanel(panel, null);
    }

    private void showUpgradePanel() {
        UpgradePanel panel = new UpgradePanel(this::showMainMenu);
        showPanel(panel, null);
    }

    private void showOptions() {
        OptionsPanel panel = new OptionsPanel(this::showMainMenu, this::showOptionsDetail, this::showAccountPlaceholder);
        showPanel(panel, null);
    }

    private void showOptionsDetail(int category) {
        Optionsdetailpanel panel = new Optionsdetailpanel(category, this::showOptions, this::showKeyBind);
        showPanel(panel, null);
    }

    private void showKeyBind() {
        ControlsKeyBindPanel panel = new ControlsKeyBindPanel(this::showOptionsDetailControls);
        showPanel(panel, null);
    }

    private void showOptionsDetailControls() {
        showOptionsDetail(OptionsPanel.CATEGORY_CONTROLS);
    }

    private void showAccountPlaceholder() {
        JOptionPane.showMessageDialog(this, "Tính năng đăng nhập sẽ được làm sau.", "Tài Khoản", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showChapterSelect() {
        ChapterSelectPanel panel = new ChapterSelectPanel(this::showMainMenu, this::showLevelSelect);
        showPanel(panel, null);
    }

    private void showLevelSelect(int chapterIndex) {
        LevelSelectPanel panel = new LevelSelectPanel(
                chapterIndex,
                this::showChapterSelect,
                levelIndex -> showDifficultySelect(chapterIndex, levelIndex),
                () -> showDifficultySelect(chapterIndex, -1)
        );
        showPanel(panel, null);
    }

    // levelIndex = -1 nghĩa là đang chọn độ khó cho chế độ Vô Hạn, không phải 1 màn cố định
    private void showDifficultySelect(int chapterIndex, int levelIndex) {
        boolean endless = levelIndex < 0;
        String title = endless ? "VÔ HẠN" : LevelConfig.CHAPTER_1_LEVELS[levelIndex].id;
        DifficultySelectPanel panel = new DifficultySelectPanel(
                title,
                () -> showLevelSelect(chapterIndex),
                difficulty -> startLevel(chapterIndex, levelIndex, difficulty)
        );
        showPanel(panel, null);
    }

    // Nút CHƠI ở Menu chính - chơi nhanh, giữ nguyên trải nghiệm gốc, không qua hệ thống Chương/Màn
    private void startQuickPlay() {
        GamePanel panel = new GamePanel(this::showMainMenu);
        showPanel(panel, null);
    }

    // Bắt đầu 1 màn cụ thể (hoặc Vô Hạn nếu levelIndex < 0) với độ khó đã chọn
    private void startLevel(int chapterIndex, int levelIndex, Difficulty difficulty) {
        LevelConfig config = levelIndex < 0
                ? LevelConfig.chapter1Endless()
                : LevelConfig.CHAPTER_1_LEVELS[levelIndex];

        // Chế độ Vô Hạn không có "hoàn thành màn" nên không cần ghi lại số sao / thưởng Kim Cương
        BiConsumer<Integer, Integer> onLevelComplete = levelIndex < 0 ? null :
                (starsEarned, rewardGold) -> {
                    GameProgress.recordResult(chapterIndex, levelIndex, starsEarned);
                    GameProgress.addGems(starsEarned * difficulty.gemsPerStar);
                };

        GamePanel panel = new GamePanel(
                config, difficulty,
                this::showMainMenu,
                () -> showLevelSelect(chapterIndex),
                onLevelComplete
        );
        showPanel(panel, null);
    }
}