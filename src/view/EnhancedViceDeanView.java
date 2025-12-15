package view;

import view.components.ModernUIComponents.*;
import view.components.NotificationCenter;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.util.List;

/**
 * EnhancedViceDeanView - Interface Vice-Doyen
 * Version dynamique : dashboard, stats rapides, tâches, actions récentes
 * sont alimentés par EnhancedViceDeanController + DataManager.
 */
public class EnhancedViceDeanView extends JFrame {

    private NotificationCenter notificationCenter;
    private static final long serialVersionUID = 1L;

    // Couleurs
    private final Color BG_DARK      = new Color(15, 15, 35);
    private final Color GLASS_BG     = new Color(255, 255, 255, 8);
    private final Color GLASS_BORDER = new Color(255, 255, 255, 25);
    private final Color NEON_CYAN    = new Color(0, 255, 255);
    private final Color NEON_PURPLE  = new Color(138, 43, 226);
    private final Color NEON_PINK    = new Color(255, 20, 147);
    private final Color NEON_GREEN   = new Color(57, 255, 20);
    private final Color NEON_ORANGE  = new Color(255, 140, 0);
    private final Color NEON_BLUE    = new Color(0, 150, 255);
    private final Color TEXT_WHITE   = new Color(255, 255, 255);
    private final Color TEXT_GRAY    = new Color(150, 150, 170);

    // Navigation
    private JButton dashboardBtn;
    private JButton assignBtn;
    private JButton studentsBtn;
    private JButton teachersBtn;
    private JButton accountsBtn;
    private JButton reportsBtn;
    private JButton settingsBtn;
    private JButton logoutBtn;

    // Panels
    private JPanel dashboardPanel;
    private JPanel assignPanel;
    private JPanel studentsPanel;
    private JPanel teachersPanel;
    private JPanel accountsPanel;
    private JPanel reportsPanel;
    private JPanel settingsPanel;

    // Tables
    private ModernTable studentsTable;
    private ModernTable teachersTable;
    private ModernTable accountsTable;

    // Cartes dashboard
    private FuturisticCard totalStudentsCard;
    private FuturisticCard totalTeachersCard;
    private FuturisticCard modulesCard;
    private FuturisticCard successRateCard;

    // Contrôles
    private JTextField teacherField;
    private JTextField searchStudentField;
    private JTextField searchTeacherField;
    private JComboBox<String> moduleCombo;
    private JComboBox<String> levelFilter;
    private JComboBox<String> statusFilter;
    private ModernButton searchBtn;
    private ModernButton assignModuleBtn;
    private ModernButton cancelBtn;
    private ModernButton addStudentBtn;
    private ModernButton editStudentBtn;
    private ModernButton deleteStudentBtn;
    private ModernButton addTeacherBtn;
    private ModernButton editTeacherBtn;
    private ModernButton deleteTeacherBtn;
    private ModernButton addAccountBtn;
    private ModernButton resetPasswordBtn;
    private ModernButton deleteAccountBtn;
    private ModernButton exportReportBtn;
    private ModernButton generateStatsBtn;

    // Données d’en‑tête
    private final String viceDeanName;
    private final String viceDeanCode;

    // Animation fond
    private Timer animationTimer;
    private float animationProgress = 0f;

    // --- Nouveaux champs pour stats rapides, tâches, actions récentes ---
    // Stats rapides
    public static class QuickStats {
        public int validatedInscriptions;
        public int totalInscriptions;
        public int assignedModules;
        public int totalModules;
        public int activeAccounts;
    }
    private JLabel lblInscriptionsValue;
    private JLabel lblModulesValue;
    private JLabel lblAccountsValue;

    // Tâches en attente
    private JLabel task1Label, task2Label, task3Label;
    private JCheckBox task1Check, task2Check, task3Check;

    // Actions récentes
    private JPanel recentActionsContent;

    public EnhancedViceDeanView(String viceDeanName, String viceDeanCode) {
        this.viceDeanName = viceDeanName;
        this.viceDeanCode = viceDeanCode;
        this.notificationCenter = new NotificationCenter(this, viceDeanCode);

        fixAllPopupColors();
        initializeComponents();
        setupUI();

        setTitle("👔 Espace Vice-Doyen - USTHB Portal");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);

        startBackgroundAnimation();
    }

    // =====================================================================
    // INITIALISATION
    // =====================================================================

    private void fixAllPopupColors() {
        UIManager.put("OptionPane.messageForeground", Color.BLACK);
        UIManager.put("OptionPane.messageFont", new Font("Segoe UI", Font.PLAIN, 14));
        UIManager.put("OptionPane.background", Color.WHITE);
        UIManager.put("Panel.background", Color.WHITE);
        UIManager.put("Label.foreground", Color.BLACK);
        UIManager.put("Label.font", new Font("Segoe UI", Font.PLAIN, 13));
        UIManager.put("Button.background", new Color(230, 230, 230));
        UIManager.put("Button.foreground", Color.BLACK);
        UIManager.put("Button.font", new Font("Segoe UI", Font.BOLD, 12));
        UIManager.put("Button.select", new Color(200, 200, 200));
        UIManager.put("ComboBox.foreground", Color.BLACK);
        UIManager.put("ComboBox.background", Color.WHITE);
        UIManager.put("ComboBox.selectionForeground", Color.BLACK);
        UIManager.put("ComboBox.selectionBackground", new Color(220, 220, 220));
        UIManager.put("TextField.foreground", Color.BLACK);
        UIManager.put("TextField.background", Color.WHITE);
        UIManager.put("TextField.caretForeground", Color.BLACK);
        UIManager.put("List.foreground", Color.BLACK);
        UIManager.put("List.background", Color.WHITE);
        UIManager.put("List.selectionForeground", Color.WHITE);
        UIManager.put("List.selectionBackground", new Color(51, 153, 255));
        UIManager.put("ToolTip.foreground", Color.BLACK);
        UIManager.put("ToolTip.background", Color.WHITE);
    }

    private void initializeComponents() {
        // Valeurs neutres : seront remplacées par le controller
        totalStudentsCard = new FuturisticCard("Total Étudiants", "0", "🎓", NEON_BLUE);
        totalTeachersCard = new FuturisticCard("Enseignants", "0", "👨‍🏫", NEON_GREEN);
        modulesCard       = new FuturisticCard("Modules", "0", "📚", NEON_PURPLE);
        successRateCard   = new FuturisticCard("Taux Réussite", "0.0%", "📈", NEON_ORANGE);
    }

    private void setupUI() {
        JPanel container = new AnimatedBackgroundPanel();
        container.setLayout(new BorderLayout());

        JPanel sidebar = createFuturisticSidebar();
        container.add(sidebar, BorderLayout.WEST);

        JPanel contentArea = new JPanel(new BorderLayout());
        contentArea.setOpaque(false);
        contentArea.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        JPanel header = createGlassHeader();
        contentArea.add(header, BorderLayout.NORTH);

        JPanel dynamicContent = new JPanel(new CardLayout());
        dynamicContent.setOpaque(false);

        dashboardPanel = createDashboardPanel();
        assignPanel    = createAssignPanel();
        studentsPanel  = createStudentsPanel();
        teachersPanel  = createTeachersPanel();
        accountsPanel  = createAccountsPanel();
        reportsPanel   = createReportsPanel();
        settingsPanel  = createSettingsPanel();

        dynamicContent.add(dashboardPanel, "dashboard");
        dynamicContent.add(assignPanel,    "assign");
        dynamicContent.add(studentsPanel,  "students");
        dynamicContent.add(teachersPanel,  "teachers");
        dynamicContent.add(accountsPanel,  "accounts");
        dynamicContent.add(reportsPanel,   "reports");
        dynamicContent.add(settingsPanel,  "settings");

        contentArea.add(dynamicContent, BorderLayout.CENTER);
        container.add(contentArea, BorderLayout.CENTER);

        add(container);

        setupNavigation(dynamicContent);
    }

    // =====================================================================
    // SIDEBAR & HEADER
    // =====================================================================

    private JPanel createFuturisticSidebar() {
        JPanel sidebar = new GlassPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(280, 0));
        sidebar.setBorder(BorderFactory.createEmptyBorder(30, 20, 30, 20));

        JLabel logo = new JLabel("🎓") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(NEON_BLUE.getRed(), NEON_BLUE.getGreen(), NEON_BLUE.getBlue(), 30));
                g2.fillOval(-10, -10, 80, 80);
                super.paintComponent(g);
            }
        };
        logo.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 50));
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = new JLabel("USTHB");
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
        title.setForeground(TEXT_WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Administration");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(TEXT_GRAY);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        sidebar.add(logo);
        sidebar.add(Box.createRigidArea(new Dimension(0, 15)));
        sidebar.add(title);
        sidebar.add(Box.createRigidArea(new Dimension(0, 5)));
        sidebar.add(subtitle);
        sidebar.add(Box.createRigidArea(new Dimension(0, 40)));

        sidebar.add(createNeonSeparator());
        sidebar.add(Box.createRigidArea(new Dimension(0, 25)));

        dashboardBtn = createNeonNavButton("📊  Dashboard", true);
        assignBtn    = createNeonNavButton("🔗  Affectations", false);
        studentsBtn  = createNeonNavButton("🎓  Étudiants", false);
        teachersBtn  = createNeonNavButton("👨‍🏫  Enseignants", false);
        accountsBtn  = createNeonNavButton("🔑  Comptes", false);
        reportsBtn   = createNeonNavButton("📈  Rapports", false);
        settingsBtn  = createNeonNavButton("⚙️  Paramètres", false);

        sidebar.add(dashboardBtn);
        sidebar.add(Box.createRigidArea(new Dimension(0, 12)));
        sidebar.add(assignBtn);
        sidebar.add(Box.createRigidArea(new Dimension(0, 12)));
        sidebar.add(studentsBtn);
        sidebar.add(Box.createRigidArea(new Dimension(0, 12)));
        sidebar.add(teachersBtn);
        sidebar.add(Box.createRigidArea(new Dimension(0, 12)));
        sidebar.add(accountsBtn);
        sidebar.add(Box.createRigidArea(new Dimension(0, 12)));
        sidebar.add(reportsBtn);
        sidebar.add(Box.createRigidArea(new Dimension(0, 12)));
        sidebar.add(settingsBtn);

        sidebar.add(Box.createVerticalGlue());
        sidebar.add(createNeonSeparator());
        sidebar.add(Box.createRigidArea(new Dimension(0, 25)));

        logoutBtn = createNeonNavButton("🚪  Déconnexion", false);
        logoutBtn.setForeground(NEON_PINK);
        sidebar.add(logoutBtn);

        return sidebar;
    }

    private JButton createNeonNavButton(String text, boolean selected) {
        JButton btn = new JButton(text) {
            private boolean hover = false;

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                try {
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    if (selected || hover) {
                        g2.setColor(new Color(255, 255, 255, 15));
                        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                    }
                    if (selected) {
                        g2.setColor(NEON_BLUE);
                        g2.setStroke(new BasicStroke(2f));
                        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                    }
                } finally {
                    g2.dispose();
                }
                super.paintComponent(g);
            }

            {
                setOpaque(false);
                setContentAreaFilled(false);
                setBorderPainted(false);
                setFocusPainted(false);
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        hover = true;
                        repaint();
                    }
                    @Override
                    public void mouseExited(MouseEvent e) {
                        hover = false;
                        repaint();
                    }
                });
            }
        };

        btn.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        btn.setForeground(selected ? NEON_BLUE : TEXT_GRAY);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        btn.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));
        return btn;
    }

    private JSeparator createNeonSeparator() {
        JSeparator sep = new JSeparator() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                GradientPaint gp1 = new GradientPaint(
                        0, 0,
                        new Color(NEON_BLUE.getRed(), NEON_BLUE.getGreen(), NEON_BLUE.getBlue(), 0),
                        getWidth() / 2, 0,
                        new Color(NEON_BLUE.getRed(), NEON_BLUE.getGreen(), NEON_BLUE.getBlue(), 100));
                g2.setPaint(gp1);
                g2.fillRect(0, 0, getWidth() / 2, 2);

                GradientPaint gp2 = new GradientPaint(
                        getWidth() / 2, 0,
                        new Color(NEON_BLUE.getRed(), NEON_BLUE.getGreen(), NEON_BLUE.getBlue(), 100),
                        getWidth(), 0,
                        new Color(NEON_BLUE.getRed(), NEON_BLUE.getGreen(), NEON_BLUE.getBlue(), 0));
                g2.setPaint(gp2);
                g2.fillRect(getWidth() / 2, 0, getWidth() / 2, 2);
            }
        };
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));
        sep.setOpaque(false);
        return sep;
    }

    private JPanel createGlassHeader() {
        JPanel header = new GlassPanel();
        header.setLayout(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        JLabel titleLabel = new JLabel("Bienvenue, " + viceDeanName);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(TEXT_WHITE);

        JLabel roleLabel = new JLabel("Vice-Doyen • Code: " + viceDeanCode);
        roleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        roleLabel.setForeground(TEXT_GRAY);

        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setOpaque(false);
        leftPanel.add(titleLabel);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        leftPanel.add(roleLabel);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        rightPanel.setOpaque(false);
        rightPanel.add(notificationCenter.getComponent());

        header.add(leftPanel, BorderLayout.WEST);
        header.add(rightPanel, BorderLayout.EAST);

        return header;
    }

    @Override
    public void dispose() {
        if (notificationCenter != null) {
            notificationCenter.dispose();
        }
        super.dispose();
    }

    // =====================================================================
    // STYLES TABLES
    // =====================================================================

    private void styleTableHeader(JTable table) {
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(new Color(30, 30, 60));
        header.setForeground(NEON_BLUE);
        header.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        header.setPreferredSize(new Dimension(header.getWidth(), 50));

        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer();
        headerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        headerRenderer.setBackground(new Color(30, 30, 60));
        headerRenderer.setForeground(NEON_BLUE);
        table.getTableHeader().setDefaultRenderer(headerRenderer);
    }

    private void styleTable(JTable table) {
        table.setRowHeight(45);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setBackground(new Color(20, 20, 40, 200));
        table.setForeground(TEXT_WHITE);
        table.setSelectionBackground(new Color(NEON_BLUE.getRed(), NEON_BLUE.getGreen(), NEON_BLUE.getBlue(), 30));
        table.setSelectionForeground(TEXT_WHITE);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(SwingConstants.CENTER);
                c.setForeground(TEXT_WHITE);
                if (isSelected) {
                    c.setBackground(new Color(NEON_BLUE.getRed(), NEON_BLUE.getGreen(), NEON_BLUE.getBlue(), 30));
                } else {
                    c.setBackground(row % 2 == 0
                            ? new Color(20, 20, 40, 200)
                            : new Color(30, 30, 50, 200));
                }
                return c;
            }
        };
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
    }

    // =====================================================================
    // PANELS DE CONTENU
    // =====================================================================

    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout(25, 25));
        panel.setOpaque(false);

        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 20, 20));
        statsPanel.setOpaque(false);
        statsPanel.add(totalStudentsCard);
        statsPanel.add(totalTeachersCard);
        statsPanel.add(modulesCard);
        statsPanel.add(successRateCard);

        panel.add(statsPanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridLayout(2, 2, 25, 25));
        centerPanel.setOpaque(false);

        centerPanel.add(createQuickStatsPanel());
        centerPanel.add(createAlertsPanel());
        centerPanel.add(createRecentActionsPanel());
        centerPanel.add(createPendingTasksPanel());

        panel.add(centerPanel, BorderLayout.CENTER);
        return panel;
    }

    // --------- STATISTIQUES RAPIDES (dynamiques) ---------
    private JPanel createQuickStatsPanel() {
        JPanel panel = new GlassPanel();
        panel.setLayout(new BorderLayout(15, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        JLabel title = new JLabel("📊 Statistiques Rapides");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(TEXT_WHITE);

        JPanel statsContent = new JPanel();
        statsContent.setLayout(new BoxLayout(statsContent, BoxLayout.Y_AXIS));
        statsContent.setOpaque(false);

        lblInscriptionsValue = new JLabel();
        lblModulesValue      = new JLabel();
        lblAccountsValue     = new JLabel();

        statsContent.add(createStatItem("Inscriptions validées", lblInscriptionsValue, NEON_GREEN));
        statsContent.add(Box.createRigidArea(new Dimension(0, 12)));
        statsContent.add(createStatItem("Modules affectés", lblModulesValue, NEON_CYAN));
        statsContent.add(Box.createRigidArea(new Dimension(0, 12)));
        statsContent.add(createStatItem("Comptes actifs", lblAccountsValue, NEON_BLUE));

        panel.add(title, BorderLayout.NORTH);
        panel.add(statsContent, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createStatItem(String label, JLabel valueLabel, Color accentColor) {
        JPanel panel = new JPanel(new BorderLayout(15, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 10));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(accentColor);
                g2.fillRoundRect(0, 0, 5, getHeight(), 12, 12);
            }
        };
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel labelComp = new JLabel(label);
        labelComp.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        labelComp.setForeground(TEXT_WHITE);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        valueLabel.setForeground(accentColor);
        valueLabel.setText("…");

        panel.add(labelComp, BorderLayout.CENTER);
        panel.add(valueLabel, BorderLayout.EAST);
        return panel;
    }

    // --------- ALERTES (peuvent rester plus générales) ---------
    private JPanel createAlertsPanel() {
        JPanel panel = new GlassPanel();
        panel.setLayout(new BorderLayout(15, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        JLabel title = new JLabel("⚠️ Alertes");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(TEXT_WHITE);

        JPanel alertsContent = new JPanel();
        alertsContent.setLayout(new BoxLayout(alertsContent, BoxLayout.Y_AXIS));
        alertsContent.setOpaque(false);

        alertsContent.add(createAlertItem("Surveillez les taux de réussite et les redoublements", NEON_PINK));
        alertsContent.add(Box.createRigidArea(new Dimension(0, 12)));
        alertsContent.add(createAlertItem("Vérifiez les modules non affectés", NEON_ORANGE));
        alertsContent.add(Box.createRigidArea(new Dimension(0, 12)));
        alertsContent.add(createAlertItem("Suivez les inscriptions en attente", NEON_CYAN));

        panel.add(title, BorderLayout.NORTH);
        panel.add(alertsContent, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createAlertItem(String text, Color color) {
        JPanel panel = new JPanel(new BorderLayout(15, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 10));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(color);
                g2.fillRoundRect(0, 0, 5, getHeight(), 12, 12);
            }
        };
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        panel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        label.setForeground(TEXT_WHITE);

        JLabel arrow = new JLabel("→");
        arrow.setFont(new Font("Segoe UI", Font.BOLD, 18));
        arrow.setForeground(color);

        panel.add(label, BorderLayout.CENTER);
        panel.add(arrow, BorderLayout.EAST);
        return panel;
    }

    // --------- ACTIONS RÉCENTES (dynamiques) ---------
    private JPanel createRecentActionsPanel() {
        JPanel panel = new GlassPanel();
        panel.setLayout(new BorderLayout(15, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        JLabel title = new JLabel("📋 Actions Récentes");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(TEXT_WHITE);

        recentActionsContent = new JPanel();
        recentActionsContent.setLayout(new BoxLayout(recentActionsContent, BoxLayout.Y_AXIS));
        recentActionsContent.setOpaque(false);

        // contenu initial
        recentActionsContent.add(createActionItem("Chargement des actions…", NEON_BLUE));

        panel.add(title, BorderLayout.NORTH);
        panel.add(recentActionsContent, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createActionItem(String text, Color accentColor) {
        JPanel panel = new JPanel(new BorderLayout(10, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 8));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 100));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
            }
        };
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 15, 12, 15));

        JLabel bullet = new JLabel("•");
        bullet.setFont(new Font("Segoe UI", Font.BOLD, 20));
        bullet.setForeground(accentColor);

        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        label.setForeground(TEXT_WHITE);

        panel.add(bullet, BorderLayout.WEST);
        panel.add(label, BorderLayout.CENTER);
        return panel;
    }

    // --------- TÂCHES EN ATTENTE (dynamiques) ---------
    private JPanel createPendingTasksPanel() {
        JPanel panel = new GlassPanel();
        panel.setLayout(new BorderLayout(15, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        JLabel title = new JLabel("✓ Tâches en Attente");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(TEXT_WHITE);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);

        task1Check = new JCheckBox();
        task1Label = new JLabel("Aucune tâche");
        content.add(createTaskItem(task1Check, task1Label));
        content.add(Box.createRigidArea(new Dimension(0, 10)));

        task2Check = new JCheckBox();
        task2Label = new JLabel("Aucune tâche");
        content.add(createTaskItem(task2Check, task2Label));
        content.add(Box.createRigidArea(new Dimension(0, 10)));

        task3Check = new JCheckBox();
        task3Label = new JLabel("Aucune tâche");
        content.add(createTaskItem(task3Check, task3Label));

        panel.add(title, BorderLayout.NORTH);
        panel.add(content, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createTaskItem(JCheckBox checkbox, JLabel label) {
        JPanel panel = new JPanel(new BorderLayout(12, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bgColor = checkbox.isSelected()
                        ? new Color(NEON_GREEN.getRed(), NEON_GREEN.getGreen(), NEON_GREEN.getBlue(), 15)
                        : new Color(NEON_ORANGE.getRed(), NEON_ORANGE.getGreen(), NEON_ORANGE.getBlue(), 15);
                g2.setColor(bgColor);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                Color borderColor = checkbox.isSelected() ? NEON_GREEN : NEON_ORANGE;
                g2.setColor(borderColor);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
            }
        };
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 15, 12, 15));

        checkbox.setOpaque(false);
        checkbox.setFocusPainted(false);

        label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        label.setForeground(TEXT_WHITE);

        panel.add(checkbox, BorderLayout.WEST);
        panel.add(label, BorderLayout.CENTER);
        return panel;
    }

    // --------- PANELS AUTRES (assign, students, teachers, accounts, reports, settings) ---------
    private JPanel createAssignPanel() {
        JPanel panel = new JPanel(new BorderLayout(25, 25));
        panel.setOpaque(false);

        JLabel title = new JLabel("🔗 Affectation des Modules");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(TEXT_WHITE);

        JPanel formPanel = createAssignFormPanel();

        panel.add(title, BorderLayout.NORTH);
        panel.add(formPanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createAssignFormPanel() {
        JPanel panel = new GlassPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        JLabel lblModule = new JLabel("Module:");
        lblModule.setForeground(TEXT_WHITE);
        lblModule.setFont(new Font("Segoe UI", Font.BOLD, 15));
        panel.add(lblModule, gbc);

        gbc.gridx = 1; gbc.gridy = 0; gbc.gridwidth = 2;
        moduleCombo = new JComboBox<>();
        styleComboBox(moduleCombo);
        panel.add(moduleCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        JLabel lblEnseignant = new JLabel("Enseignant:");
        lblEnseignant.setForeground(TEXT_WHITE);
        lblEnseignant.setFont(new Font("Segoe UI", Font.BOLD, 15));
        panel.add(lblEnseignant, gbc);

        gbc.gridx = 1; gbc.gridy = 1;
        teacherField = createModernTextField("Code ou nom enseignant...");
        panel.add(teacherField, gbc);

        gbc.gridx = 2; gbc.gridy = 1;
        searchBtn = new ModernButton("🔍 Rechercher", NEON_CYAN);
        panel.add(searchBtn, gbc);

        gbc.gridx = 1; gbc.gridy = 2; gbc.gridwidth = 2;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        buttonPanel.setOpaque(false);
        assignModuleBtn = new ModernButton("✓ Affecter", NEON_GREEN);
        cancelBtn       = new ModernButton("✗ Annuler", NEON_PINK);
        buttonPanel.add(cancelBtn);
        buttonPanel.add(assignModuleBtn);
        panel.add(buttonPanel, gbc);

        return panel;
    }

    private JPanel createStudentsPanel() {
        JPanel panel = new JPanel(new BorderLayout(25, 25));
        panel.setOpaque(false);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        JLabel title = new JLabel("🎓 Gestion des Étudiants");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(TEXT_WHITE);

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        controlPanel.setOpaque(false);

        searchStudentField = createModernTextField("Rechercher étudiant...");
        searchStudentField.setPreferredSize(new Dimension(200, 40));

        levelFilter = new JComboBox<>(new String[]{"Tous niveaux", "L1", "L2", "L3", "M1", "M2"});
        styleComboBox(levelFilter);

        statusFilter = new JComboBox<>(new String[]{"Tous statuts", "Admis", "Redouble", "Exclus"});
        styleComboBox(statusFilter);

        addStudentBtn    = new ModernButton("➕ Ajouter", NEON_GREEN);
        editStudentBtn   = new ModernButton("✏️ Modifier", NEON_CYAN);
        deleteStudentBtn = new ModernButton("🗑️ Supprimer", NEON_PINK);

        controlPanel.add(new JLabel("Niveau: ") {{ setForeground(TEXT_GRAY); }});
        controlPanel.add(levelFilter);
        controlPanel.add(new JLabel("Statut: ") {{ setForeground(TEXT_GRAY); }});
        controlPanel.add(statusFilter);
        controlPanel.add(searchStudentField);
        controlPanel.add(addStudentBtn);
        controlPanel.add(editStudentBtn);
        controlPanel.add(deleteStudentBtn);

        topPanel.add(title, BorderLayout.WEST);
        topPanel.add(controlPanel, BorderLayout.EAST);

        String[] columns = {"Matricule", "Nom", "Prénom", "Niveau", "Moyenne", "Statut", "Actions"};

        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 6;
            }
        };

        studentsTable = new ModernTable(model);
        studentsTable.setOpaque(false);
        styleTableHeader(studentsTable);
        styleTable(studentsTable);

        JScrollPane scrollPane = new JScrollPane(studentsTable);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);

        JPanel tableContainer = new GlassPanel();
        tableContainer.setLayout(new BorderLayout());
        tableContainer.add(scrollPane, BorderLayout.CENTER);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(tableContainer, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createTeachersPanel() {
        JPanel panel = new JPanel(new BorderLayout(25, 25));
        panel.setOpaque(false);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        JLabel title = new JLabel("👨‍🏫 Gestion des Enseignants");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(TEXT_WHITE);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        buttonPanel.setOpaque(false);

        searchTeacherField = createModernTextField("Rechercher enseignant...");
        searchTeacherField.setPreferredSize(new Dimension(200, 40));

        addTeacherBtn    = new ModernButton("➕ Ajouter", NEON_GREEN);
        editTeacherBtn   = new ModernButton("✏️ Modifier", NEON_CYAN);
        deleteTeacherBtn = new ModernButton("🗑️ Supprimer", NEON_PINK);

        buttonPanel.add(searchTeacherField);
        buttonPanel.add(addTeacherBtn);
        buttonPanel.add(editTeacherBtn);
        buttonPanel.add(deleteTeacherBtn);

        topPanel.add(title, BorderLayout.WEST);
        topPanel.add(buttonPanel, BorderLayout.EAST);

        String[] columns = {"Code", "Nom", "Prénom", "Modules", "Heures/Sem", "Statut", "Actions"};

        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 6;
            }
        };

        teachersTable = new ModernTable(model);
        teachersTable.setOpaque(false);
        styleTableHeader(teachersTable);
        styleTable(teachersTable);

        JScrollPane scrollPane = new JScrollPane(teachersTable);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);

        JPanel tableContainer = new GlassPanel();
        tableContainer.setLayout(new BorderLayout());
        tableContainer.add(scrollPane, BorderLayout.CENTER);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(tableContainer, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createAccountsPanel() {
        JPanel panel = new JPanel(new BorderLayout(25, 25));
        panel.setOpaque(false);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        JLabel title = new JLabel("🔑 Gestion des Comptes");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(TEXT_WHITE);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        buttonPanel.setOpaque(false);

        addAccountBtn    = new ModernButton("➕ Créer Compte", NEON_GREEN);
        resetPasswordBtn = new ModernButton("🔄 Réinitialiser", NEON_ORANGE);
        deleteAccountBtn = new ModernButton("🗑️ Supprimer", NEON_PINK);

        buttonPanel.add(addAccountBtn);
        buttonPanel.add(resetPasswordBtn);
        buttonPanel.add(deleteAccountBtn);

        topPanel.add(title, BorderLayout.WEST);
        topPanel.add(buttonPanel, BorderLayout.EAST);

        String[] columns = {"ID", "Type", "Nom d'utilisateur", "Propriétaire", "Date création", "Statut", "Actions"};

        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 6;
            }
        };

        accountsTable = new ModernTable(model);
        accountsTable.setOpaque(false);
        styleTableHeader(accountsTable);
        styleTable(accountsTable);

        JScrollPane scrollPane = new JScrollPane(accountsTable);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);

        JPanel tableContainer = new GlassPanel();
        tableContainer.setLayout(new BorderLayout());
        tableContainer.add(scrollPane, BorderLayout.CENTER);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(tableContainer, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createReportsPanel() {
        JPanel panel = new JPanel(new BorderLayout(25, 25));
        panel.setOpaque(false);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        JLabel title = new JLabel("📈 Rapports et Statistiques");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(TEXT_WHITE);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        buttonPanel.setOpaque(false);

        exportReportBtn  = new ModernButton("📤 Exporter", NEON_CYAN);
        generateStatsBtn = new ModernButton("🔄 Générer", NEON_PURPLE);

        buttonPanel.add(generateStatsBtn);
        buttonPanel.add(exportReportBtn);

        topPanel.add(title, BorderLayout.WEST);
        topPanel.add(buttonPanel, BorderLayout.EAST);

        JPanel statsGrid = new JPanel(new GridLayout(2, 3, 20, 20));
        statsGrid.setOpaque(false);

        statsGrid.add(createStatCard("Taux de Réussite Global", "0.0%", "📊", NEON_GREEN));
        statsGrid.add(createStatCard("Moyenne Générale", "0.00/20", "📈", NEON_CYAN));
        statsGrid.add(createStatCard("Taux de Présence", "0.0%", "✓", NEON_BLUE));
        statsGrid.add(createStatCard("Étudiants en Difficulté", "0", "⚠", NEON_ORANGE));
        statsGrid.add(createStatCard("Modules Complets", "0/0", "📚", NEON_PURPLE));
        statsGrid.add(createStatCard("Inscriptions Validées", "0.0%", "✓", NEON_GREEN));

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(statsGrid, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createSettingsPanel() {
        JPanel panel = new JPanel(new BorderLayout(25, 25));
        panel.setOpaque(false);

        JLabel title = new JLabel("⚙️ Paramètres");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(TEXT_WHITE);

        JPanel settingsContent = new GlassPanel();
        settingsContent.setLayout(new BoxLayout(settingsContent, BoxLayout.Y_AXIS));
        settingsContent.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        settingsContent.add(createSettingItem("🔔 Notifications", "Activer les notifications"));
        settingsContent.add(Box.createRigidArea(new Dimension(0, 20)));
        settingsContent.add(createSettingItem("🌐 Langue", "Français"));
        settingsContent.add(Box.createRigidArea(new Dimension(0, 20)));
        settingsContent.add(createSettingItem("🔐 Sécurité", "Modifier le mot de passe"));
        settingsContent.add(Box.createRigidArea(new Dimension(0, 20)));
        settingsContent.add(createSettingItem("📧 Email", viceDeanCode + "@usthb.dz"));

        panel.add(title, BorderLayout.NORTH);
        panel.add(settingsContent, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createSettingItem(String title, String description) {
        JPanel panel = new JPanel(new BorderLayout(15, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 10));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(GLASS_BORDER);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
            }
        };
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(TEXT_WHITE);

        JLabel descLabel = new JLabel(description);
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        descLabel.setForeground(TEXT_GRAY);

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);
        textPanel.add(titleLabel);
        textPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        textPanel.add(descLabel);

        JLabel arrow = new JLabel("→");
        arrow.setFont(new Font("Segoe UI", Font.BOLD, 20));
        arrow.setForeground(NEON_BLUE);

        panel.add(textPanel, BorderLayout.CENTER);
        panel.add(arrow, BorderLayout.EAST);
        return panel;
    }

    private JPanel createStatCard(String label, String value, String icon, Color color) {
        JPanel card = new GlassPanel();
        card.setLayout(new BorderLayout(15, 15));
        card.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JLabel iconLabel = new JLabel(icon) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 20));
                g2.fillOval(-5, -5, 60, 60);
                super.paintComponent(g);
            }
        };
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        valueLabel.setForeground(color);

        JLabel titleLabel = new JLabel(label);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        titleLabel.setForeground(TEXT_GRAY);

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);
        textPanel.add(valueLabel);
        textPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        textPanel.add(titleLabel);

        card.add(iconLabel, BorderLayout.WEST);
        card.add(textPanel, BorderLayout.CENTER);
        return card;
    }

    private JTextField createModernTextField(String placeholder) {
        JTextField field = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 10));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(hasFocus() ? NEON_CYAN : GLASS_BORDER);
                g2.setStroke(new BasicStroke(hasFocus() ? 2f : 1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                super.paintComponent(g);
            }
        };
        field.setOpaque(false);
        field.setForeground(TEXT_WHITE);
        field.setCaretColor(NEON_CYAN);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        field.setPreferredSize(new Dimension(250, 40));
        return field;
    }

    private void styleComboBox(JComboBox<String> combo) {
        combo.setBackground(new Color(255, 255, 255, 10));
        combo.setForeground(TEXT_WHITE);
        combo.setBorder(BorderFactory.createLineBorder(GLASS_BORDER, 1));
        combo.setFocusable(false);
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    }

    // =====================================================================
    // NAVIGATION
    // =====================================================================

    private void setupNavigation(JPanel dynamicContent) {
        CardLayout cardLayout = (CardLayout) dynamicContent.getLayout();

        dashboardBtn.addActionListener(e -> switchPanel(cardLayout, dynamicContent, "dashboard"));
        assignBtn.addActionListener(e    -> switchPanel(cardLayout, dynamicContent, "assign"));
        studentsBtn.addActionListener(e  -> switchPanel(cardLayout, dynamicContent, "students"));
        teachersBtn.addActionListener(e  -> switchPanel(cardLayout, dynamicContent, "teachers"));
        accountsBtn.addActionListener(e  -> switchPanel(cardLayout, dynamicContent, "accounts"));
        reportsBtn.addActionListener(e   -> switchPanel(cardLayout, dynamicContent, "reports"));
        settingsBtn.addActionListener(e  -> switchPanel(cardLayout, dynamicContent, "settings"));
    }

    private void switchPanel(CardLayout layout, JPanel container, String panelName) {
        layout.show(container, panelName);
        updateNavButtons(panelName);
    }

    private void updateNavButtons(String active) {
        JButton[] buttons = {dashboardBtn, assignBtn, studentsBtn, teachersBtn, accountsBtn, reportsBtn, settingsBtn};
        for (JButton btn : buttons) btn.setForeground(TEXT_GRAY);

        JButton activeBtn = switch (active) {
            case "dashboard" -> dashboardBtn;
            case "assign"    -> assignBtn;
            case "students"  -> studentsBtn;
            case "teachers"  -> teachersBtn;
            case "accounts"  -> accountsBtn;
            case "reports"   -> reportsBtn;
            case "settings"  -> settingsBtn;
            default          -> null;
        };
        if (activeBtn != null) activeBtn.setForeground(NEON_BLUE);
    }

    // =====================================================================
    // ANIMATION & GLASS
    // =====================================================================

    private void startBackgroundAnimation() {
        animationTimer = new Timer(50, e -> {
            animationProgress += 0.01f;
            if (animationProgress > 1f) animationProgress = 0f;
            repaint();
        });
        animationTimer.start();
    }

    public void updateDashboardCards(int totalStudents, int totalTeachers,
                                     int totalModules, double successRate) {
        totalStudentsCard.setValue(String.valueOf(totalStudents));
        totalTeachersCard.setValue(String.valueOf(totalTeachers));
        modulesCard.setValue(String.valueOf(totalModules));
        successRateCard.setValue(String.format("%.1f%%", successRate));
    }

    // --- Méthodes DYNAMIQUES utilisées par le contrôleur ---

    /** Statistiques rapides (inscriptions, modules, comptes). */
    public void updateQuickStats(QuickStats stats) {
        if (lblInscriptionsValue != null) {
            lblInscriptionsValue.setText(
                    String.format("%d / %d", stats.validatedInscriptions, stats.totalInscriptions)
            );
        }
        if (lblModulesValue != null) {
            lblModulesValue.setText(
                    String.format("%d / %d", stats.assignedModules, stats.totalModules)
            );
        }
        if (lblAccountsValue != null) {
            lblAccountsValue.setText(String.valueOf(stats.activeAccounts));
        }
    }

    /** Tâches en attente (inscriptions à valider, modules à affecter, taux réussite). */
    public void updatePendingTasks(int pendingInscriptions, int unassignedModules, double successRate) {
        if (task1Label != null && task1Check != null) {
            task1Label.setText("Valider " + pendingInscriptions + " inscription(s)");
            task1Check.setSelected(pendingInscriptions == 0);
        }
        if (task2Label != null && task2Check != null) {
            task2Label.setText("Affecter " + unassignedModules + " module(s)");
            task2Check.setSelected(unassignedModules == 0);
        }
        if (task3Label != null && task3Check != null) {
            boolean ok = successRate >= 80.0;
            task3Label.setText(ok
                    ? "Taux de réussite satisfaisant (" + String.format("%.1f", successRate) + "%)"
                    : "Réviser les résultats (taux actuel " + String.format("%.1f", successRate) + "%)");
            task3Check.setSelected(ok);
        }
    }

    /** Actions récentes (3 lignes max). */
    public void updateRecentActions(List<String> actions) {
        if (recentActionsContent == null) return;
        recentActionsContent.removeAll();

        if (actions == null || actions.isEmpty()) {
            recentActionsContent.add(createActionItem("Aucune action récente", NEON_BLUE));
        } else {
            int i = 0;
            for (String a : actions) {
                Color c = (i == 0) ? NEON_GREEN : (i == 1 ? NEON_CYAN : NEON_PURPLE);
                recentActionsContent.add(createActionItem(a, c));
                recentActionsContent.add(Box.createRigidArea(new Dimension(0, 10)));
                i++;
                if (i >= 3) break;
            }
        }
        recentActionsContent.revalidate();
        recentActionsContent.repaint();
    }

    // --- Classes internes pour fond et glass ---

    class AnimatedBackgroundPanel extends JPanel {
        public AnimatedBackgroundPanel() { setOpaque(true); }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, BG_DARK, getWidth(), getHeight(), new Color(25, 25, 55));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());

                float offset = animationProgress * 150;

                RadialGradientPaint rg1 = new RadialGradientPaint(
                        300 + offset, 200,
                        400,
                        new float[]{0f, 1f},
                        new Color[]{
                                new Color(NEON_BLUE.getRed(), NEON_BLUE.getGreen(), NEON_BLUE.getBlue(), 20),
                                new Color(NEON_BLUE.getRed(), NEON_BLUE.getGreen(), NEON_BLUE.getBlue(), 0)
                        });
                g2.setPaint(rg1);
                g2.fillOval((int) (300 + offset - 400), -200, 800, 800);

                RadialGradientPaint rg2 = new RadialGradientPaint(
                        getWidth() - 300 - offset, getHeight() - 200,
                        450,
                        new float[]{0f, 1f},
                        new Color[]{
                                new Color(NEON_PURPLE.getRed(), NEON_PURPLE.getGreen(), NEON_PURPLE.getBlue(), 20),
                                new Color(NEON_PURPLE.getRed(), NEON_PURPLE.getGreen(), NEON_PURPLE.getBlue(), 0)
                        });
                g2.setPaint(rg2);
                g2.fillOval(getWidth() - (int) (300 + offset) - 450, getHeight() - 650, 900, 900);
            } finally {
                g2.dispose();
            }
        }
    }

    class GlassPanel extends JPanel {
        public GlassPanel() { setOpaque(false); }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(GLASS_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.setColor(GLASS_BORDER);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                GradientPaint shimmer = new GradientPaint(
                        0, 0, new Color(255, 255, 255, 25),
                        0, getHeight(), new Color(255, 255, 255, 5));
                g2.setPaint(shimmer);
                g2.fillRoundRect(0, 0, getWidth(), 2, 20, 20);
            } finally {
                g2.dispose();
            }
            super.paintChildren(g);
        }
    }

    class FuturisticCard extends JPanel {
        private String title;
        private String value;
        private String icon;
        private Color accentColor;

        private JLabel valueLabel;
        private JLabel titleLabel;
        private JLabel iconLabel;

        private float pulsePhase = 0f;
        private Timer pulseTimer;

        public FuturisticCard(String title, String value, String icon, Color accentColor) {
            this.title = title;
            this.value = value;
            this.icon = icon;
            this.accentColor = accentColor;

            setOpaque(false);
            setLayout(new BorderLayout(20, 15));
            setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            iconLabel = new JLabel(icon) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    int pulse = (int) (Math.sin(pulsePhase) * 10 + 50);
                    g2.setColor(new Color(
                            accentColor.getRed(),
                            accentColor.getGreen(),
                            accentColor.getBlue(),
                            pulse
                    ));
                    g2.fillOval(-10, -10, 80, 80);
                    super.paintComponent(g);
                }
            };
            iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 50));

            valueLabel = new JLabel(value);
            valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 42));
            valueLabel.setForeground(TEXT_WHITE);

            titleLabel = new JLabel(title);
            titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            titleLabel.setForeground(TEXT_GRAY);

            JPanel textPanel = new JPanel();
            textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
            textPanel.setOpaque(false);
            textPanel.add(valueLabel);
            textPanel.add(Box.createRigidArea(new Dimension(0, 8)));
            textPanel.add(titleLabel);

            add(iconLabel, BorderLayout.WEST);
            add(textPanel, BorderLayout.CENTER);

            pulseTimer = new Timer(50, e -> {
                pulsePhase += 0.1f;
                repaint();
            });
            pulseTimer.start();
        }

        public void setValue(String newValue) {
            this.value = newValue;
            if (valueLabel != null) {
                valueLabel.setText(newValue);
            }
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(GLASS_BG);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

            g2.setColor(new Color(
                    accentColor.getRed(),
                    accentColor.getGreen(),
                    accentColor.getBlue(),
                    100
            ));
            g2.setStroke(new BasicStroke(2f));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);

            GradientPaint glow = new GradientPaint(
                    0, 0, new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 30),
                    0, getHeight(), new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 5)
            );
            g2.setPaint(glow);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

            super.paintComponent(g);
        }
    }

    // =====================================================================
    // GETTERS POUR CONTROLLER
    // =====================================================================

    public JButton getDashboardBtn()  { return dashboardBtn; }
    public JButton getAssignBtn()     { return assignBtn; }
    public JButton getStudentsBtn()   { return studentsBtn; }
    public JButton getTeachersBtn()   { return teachersBtn; }
    public JButton getAccountsBtn()   { return accountsBtn; }
    public JButton getReportsBtn()    { return reportsBtn; }
    public JButton getSettingsBtn()   { return settingsBtn; }
    public JButton getLogoutBtn()     { return logoutBtn; }

    public ModernButton getSearchBtn()         { return searchBtn; }
    public ModernButton getAssignModuleBtn()   { return assignModuleBtn; }
    public ModernButton getCancelBtn()         { return cancelBtn; }
    public ModernButton getAddStudentBtn()     { return addStudentBtn; }
    public ModernButton getEditStudentBtn()    { return editStudentBtn; }
    public ModernButton getDeleteStudentBtn()  { return deleteStudentBtn; }
    public ModernButton getAddTeacherBtn()     { return addTeacherBtn; }
    public ModernButton getEditTeacherBtn()    { return editTeacherBtn; }
    public ModernButton getDeleteTeacherBtn()  { return deleteTeacherBtn; }
    public ModernButton getAddAccountBtn()     { return addAccountBtn; }
    public ModernButton getResetPasswordBtn()  { return resetPasswordBtn; }
    public ModernButton getDeleteAccountBtn()  { return deleteAccountBtn; }
    public ModernButton getExportReportBtn()   { return exportReportBtn; }
    public ModernButton getGenerateStatsBtn()  { return generateStatsBtn; }

    public JTextField getTeacherField()        { return teacherField; }
    public JTextField getSearchStudentField()  { return searchStudentField; }
    public JTextField getSearchTeacherField()  { return searchTeacherField; }

    public JComboBox<String> getModuleCombo()  { return moduleCombo; }
    public JComboBox<String> getLevelFilter()  { return levelFilter; }
    public JComboBox<String> getStatusFilter() { return statusFilter; }

    public ModernTable getStudentsTable()  { return studentsTable; }
    public ModernTable getTeachersTable()  { return teachersTable; }
    public ModernTable getAccountsTable()  { return accountsTable; }

    // main de test (optionnel)
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            EnhancedViceDeanView view = new EnhancedViceDeanView("Dr. Ahmed Benziane", "30000001");
            view.setVisible(true);
        });
    }
}
