// view/components/NotificationCenter.java
package view.components;

import model.dao.NotificationManager;
import model.entities.Notification;
import model.observers.NotificationObserver;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.border.CompoundBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * Centre de notifications moderne avec :
 *  - bouton cloche + badge
 *  - popup de liste des notifications
 *  - toast flottante en bas à droite
 *  - écoute en temps réel via NotificationObserver
 */
public class NotificationCenter implements NotificationObserver {

    private final String userCode;
    private final NotificationManager notificationManager;
    private final Frame parentFrame;

    private JButton notificationButton;
    private JLabel badgeLabel;
    private JPopupMenu notificationPopup;
    private JPanel notificationPanel;

    // Toast
    private JWindow toastWindow;

    private static final Color BADGE_COLOR = new Color(220, 53, 69);
    private static final Color UNREAD_BG   = new Color(240, 245, 255);
    private static final Color READ_BG     = Color.WHITE;

    public NotificationCenter(Frame parentFrame, String userCode) {
        this.parentFrame = parentFrame;
        this.userCode = userCode;
        this.notificationManager = NotificationManager.getInstance();

        createNotificationButton();
        createNotificationPopup();

        // S'enregistrer comme observer (temps réel)
        notificationManager.addObserver(this);

        // Mettre à jour le badge au démarrage
        updateBadge();
    }

    // -------------------------------------------------------------------------
    // BOUTON (CLOCHE) + BADGE
    // -------------------------------------------------------------------------
    private void createNotificationButton() {
        notificationButton = new JButton("🔔") {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                // Effet pulse si non lues
                int unreadCount = notificationManager.getUnreadCount(userCode);
                if (unreadCount > 0) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);

                    float alpha = (float) (Math.sin(System.currentTimeMillis() / 500.0) * 0.3 + 0.7);
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                    g2.setColor(new Color(255, 200, 0));
                    g2.fillOval(2, 2, getWidth() - 4, getHeight() - 4);
                    g2.dispose();
                }
            }
        };

        notificationButton.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
        notificationButton.setFocusPainted(false);
        notificationButton.setBorderPainted(false);
        notificationButton.setContentAreaFilled(false);
        notificationButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        notificationButton.setToolTipText("Notifications");
        notificationButton.addActionListener(e -> toggleNotificationPopup());

        // Badge du compteur
        badgeLabel = new JLabel();
        badgeLabel.setFont(new Font("Segoe UI", Font.BOLD, 10));
        badgeLabel.setForeground(Color.WHITE);
        badgeLabel.setOpaque(true);
        badgeLabel.setBackground(BADGE_COLOR);
        badgeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        badgeLabel.setBorder(new EmptyBorder(2, 4, 2, 4));
        badgeLabel.setVisible(false);
    }

    /**
     * Retourne le composant à ajouter dans le header (cloche + badge).
     */
    public Component getComponent() {
        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(new Dimension(50, 40));

        notificationButton.setBounds(0, 0, 50, 40);
        badgeLabel.setBounds(30, 5, 20, 16);

        layeredPane.add(notificationButton, Integer.valueOf(0));
        layeredPane.add(badgeLabel, Integer.valueOf(1));

        return layeredPane;
    }

    // -------------------------------------------------------------------------
    // POPUP DE NOTIFICATIONS
    // -------------------------------------------------------------------------
    private void createNotificationPopup() {
        notificationPopup = new JPopupMenu();
        notificationPopup.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setPreferredSize(new Dimension(400, 500));
        mainPanel.setBackground(Color.WHITE);

        // Header
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Liste
        notificationPanel = new JPanel();
        notificationPanel.setLayout(new BoxLayout(notificationPanel, BoxLayout.Y_AXIS));
        notificationPanel.setBackground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(notificationPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Footer
        JPanel footerPanel = createFooterPanel();
        mainPanel.add(footerPanel, BorderLayout.SOUTH);

        notificationPopup.add(mainPanel);
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout(10, 0));
        header.setBackground(new Color(70, 130, 240));
        header.setBorder(new EmptyBorder(15, 20, 15, 20));

        JLabel titleLabel = new JLabel("🔔 Notifications");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);

        JButton markAllReadBtn = new JButton("Tout marquer lu");
        markAllReadBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        markAllReadBtn.setForeground(Color.WHITE);
        markAllReadBtn.setContentAreaFilled(false);
        markAllReadBtn.setBorderPainted(false);
        markAllReadBtn.setFocusPainted(false);
        markAllReadBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        markAllReadBtn.addActionListener(e -> markAllAsRead());

        header.add(titleLabel, BorderLayout.WEST);
        header.add(markAllReadBtn, BorderLayout.EAST);

        return header;
    }

    private JPanel createFooterPanel() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footer.setBackground(new Color(248, 249, 250));
        footer.setBorder(new EmptyBorder(10, 10, 10, 10));

        JButton viewAllBtn = new JButton("Voir toutes les notifications");
        viewAllBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        viewAllBtn.setForeground(new Color(70, 130, 240));
        viewAllBtn.setContentAreaFilled(false);
        viewAllBtn.setBorderPainted(false);
        viewAllBtn.setFocusPainted(false);
        viewAllBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        viewAllBtn.addActionListener(e -> openFullNotificationView());

        footer.add(viewAllBtn);
        return footer;
    }

    private void toggleNotificationPopup() {
        if (notificationPopup.isVisible()) {
            notificationPopup.setVisible(false);
        } else {
            updateNotificationList();
            notificationPopup.show(notificationButton,
                    -350,
                    notificationButton.getHeight() + 5);
        }
    }

    private void updateNotificationList() {
        notificationPanel.removeAll();

        List<Notification> notifications = notificationManager.getUserNotifications(userCode);

        if (notifications.isEmpty()) {
            JLabel emptyLabel = new JLabel("Aucune notification");
            emptyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            emptyLabel.setForeground(new Color(150, 150, 150));
            emptyLabel.setBorder(new EmptyBorder(50, 0, 50, 0));
            emptyLabel.setHorizontalAlignment(SwingConstants.CENTER);
            notificationPanel.add(emptyLabel);
        } else {
            // on limite à 10 par défaut
            List<Notification> recent = notifications.subList(
                    0, Math.min(10, notifications.size())
            );

            for (Notification n : recent) {
                notificationPanel.add(createNotificationItem(n));
            }
        }

        notificationPanel.revalidate();
        notificationPanel.repaint();
    }

    private JPanel createNotificationItem(Notification notification) {
        JPanel item = new JPanel(new BorderLayout(10, 5));
        item.setBackground(notification.isRead() ? READ_BG : UNREAD_BG);
        item.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, new Color(230, 230, 230)),
                new EmptyBorder(12, 15, 12, 15)
        ));
        item.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Icône
        JLabel iconLabel = new JLabel(notification.getIconEmoji());
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));

        // Texte
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);

        JLabel titleLabel = new JLabel(notification.getTitle());
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        titleLabel.setForeground(new Color(33, 37, 41));

        JLabel messageLabel = new JLabel("<html>" + notification.getShortMessage() + "</html>");
        messageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        messageLabel.setForeground(new Color(108, 117, 125));

        JLabel timeLabel = new JLabel(notification.getTimeAgo());
        timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        timeLabel.setForeground(new Color(150, 150, 150));

        contentPanel.add(titleLabel);
        contentPanel.add(Box.createVerticalStrut(3));
        contentPanel.add(messageLabel);
        contentPanel.add(Box.createVerticalStrut(3));
        contentPanel.add(timeLabel);

        JPanel indicatorPanel = new JPanel(new BorderLayout());
        indicatorPanel.setOpaque(false);

        if (!notification.isRead()) {
            JLabel unreadDot = new JLabel("●");
            unreadDot.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            unreadDot.setForeground(new Color(70, 130, 240));
            indicatorPanel.add(unreadDot, BorderLayout.NORTH);
        }

        item.add(iconLabel, BorderLayout.WEST);
        item.add(contentPanel, BorderLayout.CENTER);
        item.add(indicatorPanel, BorderLayout.EAST);

        // Hover
        item.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                item.setBackground(new Color(245, 245, 245));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                item.setBackground(notification.isRead() ? READ_BG : UNREAD_BG);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                handleNotificationClick(notification);
            }
        });

        return item;
    }

    // -------------------------------------------------------------------------
    // ACTIONS SUR ELEMENT
    // -------------------------------------------------------------------------
    private void handleNotificationClick(Notification notification) {
        // marquer lue
        if (!notification.isRead()) {
            notificationManager.markAsRead(notification.getId());
            updateBadge();
            updateNotificationList();
        }
        // montrer le détail
        showNotificationDetails(notification);
    }

    private void showNotificationDetails(Notification notification) {
        notificationPopup.setVisible(false);

        JDialog dialog = new JDialog(parentFrame, "Détails de la notification", true);
        dialog.setLayout(new BorderLayout(15, 15));
        dialog.setSize(500, 300);
        dialog.setLocationRelativeTo(parentFrame);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        contentPanel.setBackground(Color.WHITE);

        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        headerPanel.setOpaque(false);

        JLabel iconLabel = new JLabel(notification.getIconEmoji());
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 36));

        JLabel titleLabel = new JLabel(notification.getTitle());
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));

        headerPanel.add(iconLabel);
        headerPanel.add(titleLabel);

        JTextArea messageArea = new JTextArea(notification.getMessage());
        messageArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        messageArea.setEditable(false);
        messageArea.setOpaque(false);

        JLabel dateLabel = new JLabel("📅 " + notification.getFormattedTimestamp());
        dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        dateLabel.setForeground(new Color(108, 117, 125));

        contentPanel.add(headerPanel);
        contentPanel.add(Box.createVerticalStrut(15));
        contentPanel.add(messageArea);
        contentPanel.add(Box.createVerticalStrut(15));
        contentPanel.add(dateLabel);

        dialog.add(contentPanel, BorderLayout.CENTER);

        JButton closeBtn = new JButton("Fermer");
        closeBtn.addActionListener(e -> dialog.dispose());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(closeBtn);

        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void markAllAsRead() {
        notificationManager.markAllAsRead(userCode);
        updateBadge();
        updateNotificationList();
    }

    private void openFullNotificationView() {
        notificationPopup.setVisible(false);

        JDialog dialog = new JDialog(parentFrame, "Toutes les notifications", true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(550, 600);
        dialog.setLocationRelativeTo(parentFrame);

        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(Color.WHITE);

        List<Notification> notifications = notificationManager.getUserNotifications(userCode);
        if (notifications.isEmpty()) {
            JLabel empty = new JLabel("Aucune notification");
            empty.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            empty.setForeground(new Color(150, 150, 150));
            empty.setHorizontalAlignment(SwingConstants.CENTER);
            empty.setBorder(new EmptyBorder(50, 0, 50, 0));
            listPanel.add(empty);
        } else {
            for (Notification n : notifications) {
                listPanel.add(createNotificationItem(n));
            }
        }

        JScrollPane sp = new JScrollPane(listPanel);
        sp.setBorder(null);
        sp.getVerticalScrollBar().setUnitIncrement(16);

        dialog.add(sp, BorderLayout.CENTER);

        JButton closeBtn = new JButton("Fermer");
        closeBtn.addActionListener(e -> dialog.dispose());
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.add(closeBtn);
        dialog.add(btnPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);

        updateBadge();
    }

    // -------------------------------------------------------------------------
    // TOAST FLOTTANTE
    // -------------------------------------------------------------------------
    private void showToastNotification(Notification notification) {
        if (toastWindow != null && toastWindow.isVisible()) {
            toastWindow.setVisible(false);
            toastWindow.dispose();
            toastWindow = null;
        }

        toastWindow = new JWindow(parentFrame);
        JPanel toastContent = new JPanel(new BorderLayout(8, 8));
        toastContent.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        toastContent.setBackground(Color.WHITE);

        JLabel iconLabel = new JLabel(notification.getIconEmoji());
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 26));

        String text = notification.getTitle() + " " + notification.getShortMessage();
        JTextArea messageArea = new JTextArea(text);
        messageArea.setEditable(false);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        messageArea.setOpaque(false);
        messageArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        messageArea.setForeground(new Color(33, 37, 41));

        JButton closeBtn = new JButton("Fermer");
        closeBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        closeBtn.setFocusPainted(false);
        closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeBtn.addActionListener(e -> {
            if (toastWindow != null) {
                toastWindow.setVisible(false);
                toastWindow.dispose();
                toastWindow = null;
            }
        });

        JPanel textPanel = new JPanel(new BorderLayout());
        textPanel.setOpaque(false);
        textPanel.add(messageArea, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        bottomPanel.setOpaque(false);
        bottomPanel.add(closeBtn);

        toastContent.add(iconLabel, BorderLayout.WEST);
        toastContent.add(textPanel, BorderLayout.CENTER);
        toastContent.add(bottomPanel, BorderLayout.SOUTH);

        toastWindow.getContentPane().add(toastContent);
        toastWindow.pack();

        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int x = screen.width  - toastWindow.getWidth()  - 20;
        int y = screen.height - toastWindow.getHeight() - 40;
        toastWindow.setLocation(x, y);

        toastWindow.setAlwaysOnTop(true);
        toastWindow.setVisible(true);

        Timer autoClose = new Timer(8000, e -> {
            if (toastWindow != null) {
                toastWindow.setVisible(false);
                toastWindow.dispose();
                toastWindow = null;
            }
        });
        autoClose.setRepeats(false);
        autoClose.start();
    }

    // -------------------------------------------------------------------------
    // BADGE
    // -------------------------------------------------------------------------
    private void updateBadge() {
        int unreadCount = notificationManager.getUnreadCount(userCode);

        if (unreadCount > 0) {
            badgeLabel.setText(unreadCount > 99 ? "99+" : String.valueOf(unreadCount));
            badgeLabel.setVisible(true);
        } else {
            badgeLabel.setVisible(false);
        }
    }

    // -------------------------------------------------------------------------
    // OBSERVER IMPLEMENTATION
    // -------------------------------------------------------------------------
    @Override
    public void onNotificationReceived(Notification notification) {
        if (notification.getRecipientCode().equals(userCode)) {
            SwingUtilities.invokeLater(() -> {
                System.out.println("[NC] Prof " + userCode +
                        " a reçu une notif type=" + notification.getType());
                updateBadge();
                if (notificationPopup != null && notificationPopup.isVisible()) {
                    updateNotificationList();
                }
                showToastNotification(notification);
            });
        }
    }

    // -------------------------------------------------------------------------
    // CLEANUP
    // -------------------------------------------------------------------------
    public void dispose() {
        notificationManager.removeObserver(this);
        if (toastWindow != null) {
            toastWindow.setVisible(false);
            toastWindow.dispose();
            toastWindow = null;
        }
    }
}
