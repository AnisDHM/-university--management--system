// view/components/NotificationViewDialog.java
package view.components;

import model.dao.NotificationManager;
import model.entities.Notification;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

/**
 * Dialog complet pour visualiser et gérer toutes les notifications
 */
public class NotificationViewDialog extends JDialog {
    
    private String userCode;
    private NotificationManager notificationManager;
    private JTable notificationTable;
    private DefaultTableModel tableModel;
    private JLabel statsLabel;
    private JComboBox<String> filterCombo;
    
    public NotificationViewDialog(Frame parent, String userCode, 
                                  NotificationManager notificationManager) {
        super(parent, "Mes Notifications", true);
        this.userCode = userCode;
        this.notificationManager = notificationManager;
        
        setupDialog();
        loadNotifications();
    }
    
    private void setupDialog() {
        setLayout(new BorderLayout(15, 15));
        setSize(900, 600);
        setLocationRelativeTo(getParent());
        
        // Header
        add(createHeaderPanel(), BorderLayout.NORTH);
        
        // Table
        add(createTablePanel(), BorderLayout.CENTER);
        
        // Footer
        add(createFooterPanel(), BorderLayout.SOUTH);
    }
    
    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout(15, 10));
        header.setBorder(new EmptyBorder(20, 20, 10, 20));
        header.setBackground(Color.WHITE);
        
        // Titre et stats
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel("📬 Toutes mes notifications");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        
        statsLabel = new JLabel();
        statsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        statsLabel.setForeground(new Color(108, 117, 125));
        
        titlePanel.add(titleLabel, BorderLayout.NORTH);
        titlePanel.add(Box.createVerticalStrut(5), BorderLayout.CENTER);
        titlePanel.add(statsLabel, BorderLayout.SOUTH);
        
        // Filtres et actions
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        controlPanel.setOpaque(false);
        
        JLabel filterLabel = new JLabel("Filtrer:");
        filterLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        
        filterCombo = new JComboBox<>(new String[]{
            "Toutes", "Non lues", "Lues", "Récentes", 
            "Notes", "Absences", "Système"
        });
        filterCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        filterCombo.addActionListener(e -> applyFilter());
        
        JButton refreshBtn = new JButton("🔄 Actualiser");
        refreshBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        refreshBtn.setFocusPainted(false);
        refreshBtn.addActionListener(e -> loadNotifications());
        
        JButton markAllBtn = new JButton("✓ Tout marquer lu");
        markAllBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        markAllBtn.setFocusPainted(false);
        markAllBtn.addActionListener(e -> markAllAsRead());
        
        JButton deleteAllBtn = new JButton("🗑️ Tout supprimer");
        deleteAllBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        deleteAllBtn.setFocusPainted(false);
        deleteAllBtn.addActionListener(e -> deleteAllNotifications());
        
        controlPanel.add(filterLabel);
        controlPanel.add(filterCombo);
        controlPanel.add(refreshBtn);
        controlPanel.add(markAllBtn);
        controlPanel.add(deleteAllBtn);
        
        header.add(titlePanel, BorderLayout.WEST);
        header.add(controlPanel, BorderLayout.EAST);
        
        return header;
    }
    
    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(0, 20, 0, 20));
        panel.setBackground(Color.WHITE);
        
        // Colonnes
        String[] columns = {"", "Type", "Titre", "Message", "Date", "Statut", "Actions"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 6; // Seulement la colonne Actions
            }
            
            @Override
            public Class<?> getColumnClass(int column) {
                if (column == 6) return JButton.class;
                return String.class;
            }
        };
        
        notificationTable = new JTable(tableModel);
        notificationTable.setRowHeight(50);
        notificationTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        notificationTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        notificationTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        notificationTable.getTableHeader().setBackground(new Color(70, 130, 240));
        notificationTable.getTableHeader().setForeground(Color.WHITE);
        notificationTable.setShowGrid(true);
        notificationTable.setGridColor(new Color(230, 230, 230));
        
        // Largeurs des colonnes
        notificationTable.getColumnModel().getColumn(0).setPreferredWidth(40);  // Icône
        notificationTable.getColumnModel().getColumn(1).setPreferredWidth(100); // Type
        notificationTable.getColumnModel().getColumn(2).setPreferredWidth(180); // Titre
        notificationTable.getColumnModel().getColumn(3).setPreferredWidth(300); // Message
        notificationTable.getColumnModel().getColumn(4).setPreferredWidth(120); // Date
        notificationTable.getColumnModel().getColumn(5).setPreferredWidth(80);  // Statut
        notificationTable.getColumnModel().getColumn(6).setPreferredWidth(80);  // Actions
        
        // Renderer personnalisé
        notificationTable.setDefaultRenderer(Object.class, new NotificationCellRenderer());
        
        // Listener pour double-clic
        notificationTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = notificationTable.getSelectedRow();
                    if (row >= 0) {
                        showNotificationDetails(row);
                    }
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(notificationTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createFooterPanel() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        footer.setBorder(new EmptyBorder(10, 20, 20, 20));
        footer.setBackground(Color.WHITE);
        
        JButton closeBtn = new JButton("Fermer");
        closeBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        closeBtn.setFocusPainted(false);
        closeBtn.addActionListener(e -> dispose());
        
        footer.add(closeBtn);
        
        return footer;
    }
    
    // ============================================================================
    // CHARGEMENT DES DONNÉES
    // ============================================================================
    
    private void loadNotifications() {
        tableModel.setRowCount(0);
        
        List<Notification> notifications = notificationManager.getUserNotifications(userCode);
        
        for (Notification notification : notifications) {
            Object[] row = {
                notification.getIconEmoji(),
                getTypeLabel(notification.getType()),
                notification.getTitle(),
                notification.getShortMessage(),
                notification.getTimeAgo(),
                notification.isRead() ? "✓ Lu" : "● Non lu",
                notification.getId() // On stocke l'ID pour les actions
            };
            tableModel.addRow(row);
        }
        
        updateStats();
    }
    
    private void applyFilter() {
        String filter = (String) filterCombo.getSelectedItem();
        tableModel.setRowCount(0);
        
        List<Notification> notifications = switch (filter) {
            case "Non lues" -> notificationManager.getUnreadNotifications(userCode);
            case "Lues" -> notificationManager.getUserNotifications(userCode).stream()
                    .filter(Notification::isRead)
                    .toList();
            case "Récentes" -> notificationManager.getRecentNotifications(userCode);
            case "Notes" -> notificationManager.getNotificationsByType(
                    userCode, Notification.TYPE_GRADE_ADDED);
            case "Absences" -> notificationManager.getNotificationsByType(
                    userCode, Notification.TYPE_ABSENCE_RECORDED);
            case "Système" -> notificationManager.getNotificationsByType(
                    userCode, Notification.TYPE_SYSTEM_ANNOUNCEMENT);
            default -> notificationManager.getUserNotifications(userCode);
        };
        
        for (Notification notification : notifications) {
            Object[] row = {
                notification.getIconEmoji(),
                getTypeLabel(notification.getType()),
                notification.getTitle(),
                notification.getShortMessage(),
                notification.getTimeAgo(),
                notification.isRead() ? "✓ Lu" : "● Non lu",
                notification.getId()
            };
            tableModel.addRow(row);
        }
        
        updateStats();
    }
    
    private void updateStats() {
        NotificationManager.NotificationStats stats = 
            notificationManager.getStats(userCode);
        statsLabel.setText(String.format(
            "%d notification%s • %d non lue%s • %d récente%s",
            stats.total, stats.total > 1 ? "s" : "",
            stats.unread, stats.unread > 1 ? "s" : "",
            stats.recent, stats.recent > 1 ? "s" : ""
        ));
    }
    
    // ============================================================================
    // ACTIONS
    // ============================================================================
    
    private void showNotificationDetails(int row) {
        String notificationId = (String) tableModel.getValueAt(row, 6);
        
        // Trouver la notification
        Notification notification = notificationManager.getUserNotifications(userCode)
                .stream()
                .filter(n -> n.getId().equals(notificationId))
                .findFirst()
                .orElse(null);
        
        if (notification != null) {
            // Marquer comme lue
            if (!notification.isRead()) {
                notificationManager.markAsRead(notification.getId());
                loadNotifications();
            }
            
            // Afficher les détails
            JDialog detailDialog = new JDialog(this, "Détails", true);
            detailDialog.setLayout(new BorderLayout(15, 15));
            detailDialog.setSize(500, 350);
            detailDialog.setLocationRelativeTo(this);
            
            JPanel contentPanel = new JPanel();
            contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
            contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
            
            JLabel iconLabel = new JLabel(notification.getIconEmoji());
            iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
            iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            
            JLabel titleLabel = new JLabel(notification.getTitle());
            titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
            titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            
            JTextArea messageArea = new JTextArea(notification.getMessage());
            messageArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            messageArea.setLineWrap(true);
            messageArea.setWrapStyleWord(true);
            messageArea.setEditable(false);
            messageArea.setOpaque(false);
            
            JLabel dateLabel = new JLabel("📅 " + notification.getFormattedTimestamp());
            dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            dateLabel.setForeground(new Color(108, 117, 125));
            dateLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            
            contentPanel.add(iconLabel);
            contentPanel.add(Box.createVerticalStrut(10));
            contentPanel.add(titleLabel);
            contentPanel.add(Box.createVerticalStrut(15));
            contentPanel.add(messageArea);
            contentPanel.add(Box.createVerticalStrut(15));
            contentPanel.add(dateLabel);
            
            detailDialog.add(contentPanel, BorderLayout.CENTER);
            
            JButton closeBtn = new JButton("Fermer");
            closeBtn.addActionListener(e -> detailDialog.dispose());
            JPanel btnPanel = new JPanel();
            btnPanel.add(closeBtn);
            detailDialog.add(btnPanel, BorderLayout.SOUTH);
            
            detailDialog.setVisible(true);
        }
    }
    
    private void markAllAsRead() {
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Marquer toutes les notifications comme lues?",
            "Confirmation",
            JOptionPane.YES_NO_OPTION
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            notificationManager.markAllAsRead(userCode);
            loadNotifications();
        }
    }
    
    private void deleteAllNotifications() {
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Supprimer TOUTES les notifications?\nCette action est irréversible.",
            "Confirmation",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            notificationManager.deleteAllNotifications(userCode);
            loadNotifications();
        }
    }
    
    // ============================================================================
    // HELPERS
    // ============================================================================
    
    private String getTypeLabel(String type) {
        return switch (type) {
            case Notification.TYPE_GRADE_ADDED -> "Note ajoutée";
            case Notification.TYPE_GRADE_MODIFIED -> "Note modifiée";
            case Notification.TYPE_ABSENCE_RECORDED -> "Absence";
            case Notification.TYPE_MODULE_ASSIGNED -> "Module";
            case Notification.TYPE_ACCOUNT_CREATED -> "Compte créé";
            case Notification.TYPE_ACCOUNT_MODIFIED -> "Compte modifié";
            case Notification.TYPE_INSCRIPTION_VALIDATED -> "Inscription";
            case Notification.TYPE_SYSTEM_ANNOUNCEMENT -> "Annonce";
            default -> "Autre";
        };
    }
    
    // ============================================================================
    // RENDERER PERSONNALISÉ
    // ============================================================================
    
    private class NotificationCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            
            Component c = super.getTableCellRendererComponent(
                table, value, isSelected, hasFocus, row, column
            );
            
            // Couleur de fond pour les non lues
            String status = (String) table.getValueAt(row, 5);
            if (!isSelected && status.contains("Non lu")) {
                c.setBackground(new Color(240, 245, 255));
            } else if (!isSelected) {
                c.setBackground(Color.WHITE);
            }
            
            // Alignement
            if (column == 0) { // Icône
                setHorizontalAlignment(CENTER);
                setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
            } else {
                setHorizontalAlignment(LEFT);
                setFont(new Font("Segoe UI", Font.PLAIN, 13));
            }
            
            // Colonne actions
            if (column == 6) {
                JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
                panel.setOpaque(false);
                
                JButton viewBtn = new JButton("👁️");
                viewBtn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
                viewBtn.setFocusPainted(false);
                viewBtn.setBorderPainted(false);
                viewBtn.setContentAreaFilled(false);
                viewBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
                viewBtn.setToolTipText("Voir");
                viewBtn.addActionListener(e -> showNotificationDetails(row));
                
                JButton deleteBtn = new JButton("🗑️");
                deleteBtn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
                deleteBtn.setFocusPainted(false);
                deleteBtn.setBorderPainted(false);
                deleteBtn.setContentAreaFilled(false);
                deleteBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
                deleteBtn.setToolTipText("Supprimer");
                deleteBtn.addActionListener(e -> {
                    String id = (String) table.getValueAt(row, 6);
                    notificationManager.deleteNotification(userCode, id);
                    loadNotifications();
                });
                
                panel.add(viewBtn);
                panel.add(deleteBtn);
                
                return panel;
            }
            
            return c;
        }
    }
}