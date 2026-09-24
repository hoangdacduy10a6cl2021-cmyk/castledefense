/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package castledefense;

import javax.swing.*;
import java.awt.*;

/**
 * Cửa sổ Admin (cheat/test): mở bằng tổ hợp phím bí mật Ctrl+Shift+A ở bất kỳ màn nào (xem GameFrame).
 * Dùng Swing component chuẩn (JButton/JLabel) thay vì vẽ Graphics2D vì đây là công cụ dành cho
 * người phát triển/test, không cần đẹp như các màn hình chính của game.
 *
 * - Nhóm "Tài Nguyên Vĩnh Viễn": luôn dùng được, tác động lên GameProgress/TowerUpgradeData.
 * - Nhóm "Trong Ván Đang Chơi": chỉ hoạt động khi đang có 1 GamePanel đang chạy (activeGame != null).
 */
public class AdminPanel extends JDialog {
    public AdminPanel(Frame owner, GamePanel activeGame) {
        super(owner, "Admin Panel", true);
        setResizable(false);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        content.add(sectionLabel("TÀI NGUYÊN VĨNH VIỄN"));
        content.add(Box.createVerticalStrut(6));
        addButton(content, actionButton("+1.000 Kim Cương", () -> GameProgress.addGems(1000)));
        addButton(content, actionButton("+50 mỗi loại Vật Phẩm", () -> GameProgress.adminGrantAllItems(50)));
        addButton(content, actionButton("Mở khoá tất cả màn Chương 1 (3 sao)", GameProgress::adminUnlockAllLevelsWithStars));
        addButton(content, actionButton("Nâng tối đa mọi tháp (Tinh Nhuệ)", TowerUpgradeData::adminMaxAllTowers));
        content.add(Box.createVerticalStrut(4));

        JButton resetBtn = actionButton("XOÁ TOÀN BỘ TIẾN TRÌNH", () -> {
            int choice = JOptionPane.showConfirmDialog(this,
                    "Xoá hết sao/màn, Kim Cương, vật phẩm, nâng cấp tháp và thống kê?\nKHÔNG THỂ hoàn tác.",
                    "Xác nhận Reset", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (choice == JOptionPane.YES_OPTION) {
                GameProgress.adminResetProgress();
                TowerUpgradeData.adminResetAllTowers();
                JOptionPane.showMessageDialog(this, "Đã xoá toàn bộ tiến trình.");
            }
        });
        resetBtn.setForeground(new Color(160, 30, 30));
        addButton(content, resetBtn);

        content.add(Box.createVerticalStrut(18));
        content.add(sectionLabel("TRONG VÁN ĐANG CHƠI"));
        content.add(Box.createVerticalStrut(6));

        if (activeGame == null) {
            JLabel note = new JLabel("Không có ván nào đang chơi lúc này.");
            note.setForeground(Color.GRAY);
            note.setAlignmentX(Component.LEFT_ALIGNMENT);
            content.add(note);
        } else {
            addButton(content, actionButton("+1.000 Vàng", () -> activeGame.adminAddGold(1000)));
            addButton(content, actionButton("Hồi Đầy Máu Thành", activeGame::adminFullHealCastle));
            addButton(content, actionButton("Bất Tử 60 Giây", () -> activeGame.adminActivateInvincibility(60)));
            addButton(content, actionButton("Qua Đợt Ngay Lập Tức", activeGame::adminSkipWave));
            addButton(content, actionButton("Giết Sạch Quái Trên Bản Đồ", activeGame::adminKillAllEnemies));
        }

        content.add(Box.createVerticalGlue());

        JButton closeBtn = new JButton("Đóng (Ctrl+Shift+A)");
        closeBtn.addActionListener(e -> dispose());
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottom.add(closeBtn);

        setLayout(new BorderLayout());
        add(new JScrollPane(content), BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        setSize(400, 560);
        setLocationRelativeTo(owner);
    }

    private void addButton(JPanel content, JButton b) {
        content.add(b);
        content.add(Box.createVerticalStrut(4));
    }

    private JLabel sectionLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.BOLD, 14));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JButton actionButton(String label, Runnable action) {
        JButton b = new JButton(label);
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        b.setMargin(new Insets(4, 8, 4, 8));
        b.addActionListener(e -> {
            action.run();
            AudioEngine.playUpgradeSuccess();
        });
        return b;
    }
}