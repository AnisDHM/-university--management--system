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

/**
 * EnhancedProfessorView - Interface Professeur Ultra-Moderne
 * Design futuriste avec glassmorphism, animations et effets néon
 */
public class EnhancedProfessorView extends JFrame {
    private NotificationCenter notificationCenter;
    private static final long serialVersionUID = 1L;
    
    // Couleurs futuristes
    private final Color BG_DARK = new Color(15, 15, 35);
    private final Color GLASS_BG = new Color(255, 255, 255, 8);
    private final Color GLASS_BORDER = new Color(255, 255, 255, 25);
    private final Color NEON_CYAN = new Color(0, 255, 255);
    private final Color NEON_PURPLE = new Color(138, 43, 226);
    private final Color NEON_PINK = new Color(255, 20, 147);
    private final Color NEON_GREEN = new Color(57, 255, 20);
    private final Color NEON_ORANGE = new Color(255, 140, 0);
    private final Color TEXT_WHITE = new Color(255, 255, 255);
    private final Color TEXT_GRAY = new Color(150, 150, 170);
    
    // Navigation
    private JButton dashboardBtn;
    private JButton unitsBtn;
    private JButton studentsBtn;
    private JButton gradesBtn;
    private JButton absencesBtn;
    private JButton reportsBtn;
    private JButton profileBtn;
    private JButton logoutBtn;
    
    // Panels
    private JPanel dashboardPanel;
    private JPanel unitsPanel;
    private JPanel studentsPanel;
    private JPanel gradesPanel;
    private JPanel absencesPanel;
    private JPanel reportsPanel;
    private JPanel profilePanel;
    
    // Tables
    private ModernTable studentsTable;
    private ModernTable gradesTable;
    private ModernTable unitsTable;
    private ModernTable absencesTable;
    
    // Cartes du dashboard
    private FuturisticCard totalStudentsCard;
    private FuturisticCard modulesCard;
    private FuturisticCard averageCard;
    private FuturisticCard toGradeCard;

    // Cartes Rapports & Statistiques
    private JPanel successRateCardPanel;
    private JPanel averageCardPanelReports;
    private JPanel presenceCardPanel;
    private JPanel difficultyCardPanel;

    // Profil : labels dynamiques
    private JLabel profileModulesValueLabel;
    private JLabel profileStudentsValueLabel;
    
    // Composants d'action
    private ModernButton addBtn, editBtn, deleteBtn;
    private ModernButton searchBtn;
    private ModernButton loadBtn, saveBtn, cancelBtn;
    private ModernButton exportBtn, generateReportBtn;
    private ModernButton recordAbsenceBtn, viewAbsenceDetailsBtn;
    private JTextField searchField;
    private JComboBox<String> moduleFilter;
    private JComboBox<String> moduleCombo, typeCombo;
    
    private String professorName;
    private String professorCode;
    
    private Timer animationTimer;
    private float animationProgress = 0f;
    
    public EnhancedProfessorView(String professorName, String professorCode) {
    	System.out.println("[VIEW] EnhancedProfessorView créé pour " + professorCode);
    	this.professorName = professorName;
        this.professorCode = professorCode;
        this.notificationCenter = new NotificationCenter(this, professorCode);
        
        fixAllPopupColors();
        initializeComponents();
        setupUI();
        
        setTitle("👨‍🏫 Espace Professeur - USTHB Portal");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);
        
        startBackgroundAnimation();
    }

    // ============================================================
    // GLOBAL UI FIXES
    // ============================================================
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
        totalStudentsCard = new FuturisticCard("Total Étudiants", "0", "👥", NEON_CYAN);
        modulesCard       = new FuturisticCard("Mes Modules", "0", "📚", NEON_PURPLE);
        averageCard       = new FuturisticCard("Moyenne Classe", "0.00", "📊", NEON_GREEN);
        toGradeCard       = new FuturisticCard("À Noter", "0", "✏️", NEON_ORANGE);
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
        unitsPanel     = createUnitsPanel();
        studentsPanel  = createStudentsPanel();
        gradesPanel    = createGradesPanel();
        absencesPanel  = createAbsencesPanel();
        reportsPanel   = createReportsPanel();
        profilePanel   = createProfilePanel();
        
        dynamicContent.add(dashboardPanel, "dashboard");
        dynamicContent.add(unitsPanel, "units");
        dynamicContent.add(studentsPanel, "students");
        dynamicContent.add(gradesPanel, "grades");
        dynamicContent.add(absencesPanel, "absences");
        dynamicContent.add(reportsPanel, "reports");
        dynamicContent.add(profilePanel, "profile");
        
        contentArea.add(dynamicContent, BorderLayout.CENTER);
        container.add(contentArea, BorderLayout.CENTER);
        
        add(container);
        
        setupNavigation(dynamicContent);
    }

    // ============================================================
    // SIDEBAR & HEADER
    // ============================================================
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
                g2.setColor(new Color(NEON_PURPLE.getRed(), NEON_PURPLE.getGreen(), NEON_PURPLE.getBlue(), 30));
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
        
        JLabel subtitle = new JLabel("Espace Professeur");
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
        unitsBtn     = createNeonNavButton("📚  Mes Unités", false);
        studentsBtn  = createNeonNavButton("👥  Étudiants", false);
        gradesBtn    = createNeonNavButton("📝  Noter", false);
        absencesBtn  = createNeonNavButton("📅  Absences", false);
        reportsBtn   = createNeonNavButton("📈  Rapports", false);
        profileBtn   = createNeonNavButton("👤  Profil", false);
        
        sidebar.add(dashboardBtn);
        sidebar.add(Box.createRigidArea(new Dimension(0, 12)));
        sidebar.add(unitsBtn);
        sidebar.add(Box.createRigidArea(new Dimension(0, 12)));
        sidebar.add(studentsBtn);
        sidebar.add(Box.createRigidArea(new Dimension(0, 12)));
        sidebar.add(gradesBtn);
        sidebar.add(Box.createRigidArea(new Dimension(0, 12)));
        sidebar.add(absencesBtn);
        sidebar.add(Box.createRigidArea(new Dimension(0, 12)));
        sidebar.add(reportsBtn);
        sidebar.add(Box.createRigidArea(new Dimension(0, 12)));
        sidebar.add(profileBtn);
        
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
                        g2.setColor(new Color(255, 255, 255, 20));
                        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                    }

                    if (selected) {
                        g2.setColor(NEON_PURPLE);
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
        btn.setForeground(selected ? NEON_PURPLE : TEXT_GRAY);
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
                GradientPaint gp = new GradientPaint(
                    0, 0, new Color(NEON_PURPLE.getRed(), NEON_PURPLE.getGreen(), NEON_PURPLE.getBlue(), 0),
                    getWidth()/2, 0, new Color(NEON_PURPLE.getRed(), NEON_PURPLE.getGreen(), NEON_PURPLE.getBlue(), 100)
                );
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth()/2, 2);
                
                gp = new GradientPaint(
                    getWidth()/2, 0, new Color(NEON_PURPLE.getRed(), NEON_PURPLE.getGreen(), NEON_PURPLE.getBlue(), 100),
                    getWidth(), 0, new Color(NEON_PURPLE.getRed(), NEON_PURPLE.getGreen(), NEON_PURPLE.getBlue(), 0)
                );
                g2.setPaint(gp);
                g2.fillRect(getWidth()/2, 0, getWidth()/2, 2);
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
        
        JLabel titleLabel = new JLabel("Bienvenue, Prof. " + professorName);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(TEXT_WHITE);
        
        JLabel codeLabel = new JLabel("Code: " + professorCode);
        codeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        codeLabel.setForeground(TEXT_GRAY);
        
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setOpaque(false);
        leftPanel.add(titleLabel);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        leftPanel.add(codeLabel);
        
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        rightPanel.setOpaque(false);
        rightPanel.add(notificationCenter.getComponent());
        
        JLabel dateLabel = new JLabel("📅 " + java.time.LocalDate.now().toString());
        dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        dateLabel.setForeground(TEXT_GRAY);
        rightPanel.add(dateLabel);
        
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

    // ============================================================
    // TABLES
    // ============================================================
    private void styleTableHeader(JTable table) {
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(new Color(30, 30, 60));
        header.setForeground(NEON_PURPLE);
        header.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        header.setPreferredSize(new Dimension(header.getWidth(), 50));
        
        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer();
        headerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        headerRenderer.setBackground(new Color(30, 30, 60));
        headerRenderer.setForeground(NEON_PURPLE);
        table.getTableHeader().setDefaultRenderer(headerRenderer);
    }
    
    private void styleTable(JTable table) {
        table.setRowHeight(45);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setBackground(new Color(20, 20, 40, 200));
        table.setForeground(TEXT_WHITE);
        table.setSelectionBackground(new Color(NEON_PURPLE.getRed(), NEON_PURPLE.getGreen(), NEON_PURPLE.getBlue(), 30));
        table.setSelectionForeground(TEXT_WHITE);
        
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, 
                    isSelected, hasFocus, row, column);
                
                setHorizontalAlignment(SwingConstants.CENTER);
                c.setForeground(TEXT_WHITE);
                
                if (isSelected) {
                    c.setBackground(new Color(NEON_PURPLE.getRed(), NEON_PURPLE.getGreen(), NEON_PURPLE.getBlue(), 30));
                } else {
                    if (row % 2 == 0) {
                        c.setBackground(new Color(20, 20, 40, 200));
                    } else {
                        c.setBackground(new Color(30, 30, 50, 200));
                    }
                }
                return c;
            }
        };
        
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
    }

    // ============================================================
    // PANELS (dashboard, unités, étudiants, notes, absences)
    // ============================================================
    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout(25, 25));
        panel.setOpaque(false);
        
        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 20, 20));
        statsPanel.setOpaque(false);
        statsPanel.add(totalStudentsCard);
        statsPanel.add(modulesCard);
        statsPanel.add(averageCard);
        statsPanel.add(toGradeCard);
        
        panel.add(statsPanel, BorderLayout.NORTH);
        
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 25, 25));
        centerPanel.setOpaque(false);
        
        centerPanel.add(createRecentActivitiesPanel());
        centerPanel.add(createUpcomingClassesPanel());
        
        panel.add(centerPanel, BorderLayout.CENTER);
        
        return panel;
    }

    private JPanel createRecentActivitiesPanel() {
        JPanel panel = new GlassPanel();
        panel.setLayout(new BorderLayout(15, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        
        JLabel title = new JLabel("📋 Activités Récentes");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(TEXT_WHITE);
        
        JPanel activitiesPanel = new JPanel();
        activitiesPanel.setLayout(new BoxLayout(activitiesPanel, BoxLayout.Y_AXIS));
        activitiesPanel.setOpaque(false);

        activitiesPanel.add(createActivityItem("Consultation des notes", "Aujourd'hui", NEON_GREEN));
        activitiesPanel.add(Box.createRigidArea(new Dimension(0, 12)));
        activitiesPanel.add(createActivityItem("Saisie d'absences", "Cette semaine", NEON_ORANGE));
        activitiesPanel.add(Box.createRigidArea(new Dimension(0, 12)));
        activitiesPanel.add(createActivityItem("Rapport généré", "Récemment", NEON_CYAN));
        
        panel.add(title, BorderLayout.NORTH);
        panel.add(activitiesPanel, BorderLayout.CENTER);
        
        return panel;
    }

    private JPanel createActivityItem(String activity, String time, Color accentColor) {
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
        
        JLabel activityLabel = new JLabel(activity);
        activityLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        activityLabel.setForeground(TEXT_WHITE);
        
        JLabel timeLabel = new JLabel(time);
        timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        timeLabel.setForeground(TEXT_GRAY);
        
        panel.add(activityLabel, BorderLayout.CENTER);
        panel.add(timeLabel, BorderLayout.EAST);
        
        return panel;
    }

    private JPanel createUpcomingClassesPanel() {
        JPanel panel = new GlassPanel();
        panel.setLayout(new BorderLayout(15, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        
        JLabel title = new JLabel("📅 Cours à Venir");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(TEXT_WHITE);
        
        JPanel classesPanel = new JPanel();
        classesPanel.setLayout(new BoxLayout(classesPanel, BoxLayout.Y_AXIS));
        classesPanel.setOpaque(false);

        classesPanel.add(createClassItem("Module 1", "Prochain cours", NEON_GREEN));
        classesPanel.add(Box.createRigidArea(new Dimension(0, 12)));
        classesPanel.add(createClassItem("Module 2", "À venir", NEON_CYAN));
        classesPanel.add(Box.createRigidArea(new Dimension(0, 12)));
        classesPanel.add(createClassItem("Module 3", "À planifier", NEON_ORANGE));
        
        panel.add(title, BorderLayout.NORTH);
        panel.add(classesPanel, BorderLayout.CENTER);
        
        return panel;
    }

    private JPanel createClassItem(String className, String time, Color color) {
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
        
        JLabel classLabel = new JLabel(className);
        classLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        classLabel.setForeground(TEXT_WHITE);
        
        JLabel timeLabel = new JLabel(time);
        timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        timeLabel.setForeground(TEXT_GRAY);
        
        panel.add(classLabel, BorderLayout.CENTER);
        panel.add(timeLabel, BorderLayout.EAST);
        
        return panel;
    }

    private JPanel createUnitsPanel() {
        JPanel panel = new JPanel(new BorderLayout(25, 25));
        panel.setOpaque(false);
        
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        
        JLabel title = new JLabel("📚 Mes Unités Pédagogiques");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(TEXT_WHITE);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        buttonPanel.setOpaque(false);
        
        addBtn    = new ModernButton("➕ Ajouter", NEON_GREEN);
        editBtn   = new ModernButton("✏️ Modifier", NEON_CYAN);
        deleteBtn = new ModernButton("🗑️ Supprimer", NEON_PINK);
        
        buttonPanel.add(addBtn);
        buttonPanel.add(editBtn);
        buttonPanel.add(deleteBtn);
        
        topPanel.add(title, BorderLayout.WEST);
        topPanel.add(buttonPanel, BorderLayout.EAST);
        
        String[] columns = {"Code", "Intitulé", "Semestre", "Crédits", "Étudiants", "Heures/Sem", "Actions"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 6;
            }
        };
        
        unitsTable = new ModernTable(model);
        unitsTable.setOpaque(false);
        styleTableHeader(unitsTable);
        styleTable(unitsTable);
        
        JScrollPane scrollPane = new JScrollPane(unitsTable);
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

    private JPanel createStudentsPanel() {
        JPanel panel = new JPanel(new BorderLayout(25, 25));
        panel.setOpaque(false);
        
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        
        JLabel title = new JLabel("👥 Gestion des Étudiants");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(TEXT_WHITE);
        
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        searchPanel.setOpaque(false);
        
        searchField = createModernTextField("Rechercher...");
        
        moduleFilter = new JComboBox<>(new String[]{"Tous les modules"});
        styleComboBox(moduleFilter);
        
        searchBtn = new ModernButton("🔍 Rechercher", NEON_CYAN);
        
        searchPanel.add(new JLabel("Module: ") {{
            setForeground(TEXT_GRAY);
        }});
        searchPanel.add(moduleFilter);
        searchPanel.add(searchField);
        searchPanel.add(searchBtn);
        
        topPanel.add(title, BorderLayout.WEST);
        topPanel.add(searchPanel, BorderLayout.EAST);
        
        String[] columns = {"Matricule", "Nom", "Prénom", "Email", "Niveau", "Moyenne", "Absences", "Statut"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
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

    private JPanel createGradesPanel() {
        JPanel panel = new JPanel(new BorderLayout(25, 25));
        panel.setOpaque(false);
        
        JLabel title = new JLabel("📝 Saisie des Notes");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(TEXT_WHITE);
        
        JPanel formPanel = createGradeFormPanel();
        
        panel.add(title, BorderLayout.NORTH);
        panel.add(formPanel, BorderLayout.CENTER);
        
        return panel;
    }

    private JPanel createGradeFormPanel() {
        JPanel panel = new GlassPanel();
        panel.setLayout(new BorderLayout(20, 20));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        
        JPanel selectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        selectionPanel.setOpaque(false);
        
        moduleCombo = new JComboBox<>();
        styleComboBox(moduleCombo);
        
        typeCombo = new JComboBox<>(new String[]{"Examen", "CC", "TP", "TD"});
        styleComboBox(typeCombo);
        
        loadBtn = new ModernButton("📥 Charger Étudiants", NEON_CYAN);
        
        selectionPanel.add(new JLabel("Module: ") {{
            setForeground(TEXT_GRAY);
            setFont(new Font("Segoe UI", Font.BOLD, 14));
        }});
        selectionPanel.add(moduleCombo);
        selectionPanel.add(new JLabel("Type: ") {{
            setForeground(TEXT_GRAY);
            setFont(new Font("Segoe UI", Font.BOLD, 14));
        }});
        selectionPanel.add(typeCombo);
        selectionPanel.add(loadBtn);
        
        String[] columns = {"Matricule", "Nom", "Prénom", "Note (/20)", "Observation"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 3 || column == 4;
            }
        };
        
        gradesTable = new ModernTable(model);
        gradesTable.setOpaque(false);
        styleTableHeader(gradesTable);
        styleTable(gradesTable);
        
        JScrollPane scrollPane = new JScrollPane(gradesTable);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        actionPanel.setOpaque(false);
        
        saveBtn   = new ModernButton("💾 Enregistrer", NEON_GREEN);
        cancelBtn = new ModernButton("❌ Annuler", NEON_PINK);
        
        actionPanel.add(cancelBtn);
        actionPanel.add(saveBtn);
        
        panel.add(selectionPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(actionPanel, BorderLayout.SOUTH);
        
        return panel;
    }

    private JPanel createAbsencesPanel() {
        JPanel panel = new JPanel(new BorderLayout(25, 25));
        panel.setOpaque(false);
        
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        
        JLabel title = new JLabel("📅 Gestion des Absences");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(TEXT_WHITE);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        buttonPanel.setOpaque(false);
        
        recordAbsenceBtn      = new ModernButton("➕ Enregistrer Absence", NEON_ORANGE);
        viewAbsenceDetailsBtn = new ModernButton("👁️ Détails", NEON_CYAN);
        
        buttonPanel.add(viewAbsenceDetailsBtn);
        buttonPanel.add(recordAbsenceBtn);
        
        topPanel.add(title, BorderLayout.WEST);
        topPanel.add(buttonPanel, BorderLayout.EAST);
        
        String[] columns = {"Date", "Module", "Matricule", "Étudiant", "Type", "Justifiée", "Observation"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        absencesTable = new ModernTable(model);
        absencesTable.setOpaque(false);
        styleTableHeader(absencesTable);
        styleTable(absencesTable);
        
        JScrollPane scrollPane = new JScrollPane(absencesTable);
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
        
        exportBtn         = new ModernButton("📤 Exporter PDF", NEON_CYAN);
        generateReportBtn = new ModernButton("🔄 Générer Rapport", NEON_PURPLE);
        
        buttonPanel.add(generateReportBtn);
        buttonPanel.add(exportBtn);
        
        topPanel.add(title, BorderLayout.WEST);
        topPanel.add(buttonPanel, BorderLayout.EAST);
        
        JPanel centerPanel = new JPanel(new GridLayout(2, 2, 20, 20));
        centerPanel.setOpaque(false);

        successRateCardPanel    = createStatCard("Taux de Réussite", "-", "📊", NEON_GREEN);
        averageCardPanelReports = createStatCard("Moyenne Générale", "-", "📈", NEON_CYAN);
        presenceCardPanel       = createStatCard("Taux de Présence", "-", "✓", NEON_PURPLE);
        difficultyCardPanel     = createStatCard("Étudiants en Difficulté", "-", "⚠", NEON_ORANGE);
        
        centerPanel.add(successRateCardPanel);
        centerPanel.add(averageCardPanelReports);
        centerPanel.add(presenceCardPanel);
        centerPanel.add(difficultyCardPanel);
        
        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(centerPanel, BorderLayout.CENTER);
        
        return panel;
    }

    private JPanel createStatCard(String label, String value, String icon, Color color) {
        JPanel card = new GlassPanel();
        card.setLayout(new BorderLayout(15, 15));
        card.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        valueLabel.setForeground(color);
        
        JLabel titleLabel = new JLabel(label);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        titleLabel.setForeground(TEXT_GRAY);
        
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);
        textPanel.add(valueLabel);
        textPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        textPanel.add(titleLabel);
        
        card.add(iconLabel, BorderLayout.WEST);
        card.add(textPanel, BorderLayout.CENTER);

        card.putClientProperty("valueLabel", valueLabel);
        
        return card;
    }

    private JPanel createProfilePanel() {
        JPanel panel = new JPanel(new BorderLayout(25, 25));
        panel.setOpaque(false);
        
        JLabel title = new JLabel("👤 Mon Profil");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(TEXT_WHITE);
        
        panel.add(title, BorderLayout.NORTH);
        
        JPanel profileContent = new GlassPanel();
        profileContent.setLayout(new BorderLayout(20, 20));
        profileContent.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        
        JPanel avatarSection = new JPanel();
        avatarSection.setLayout(new BoxLayout(avatarSection, BoxLayout.Y_AXIS));
        avatarSection.setOpaque(false);
        
        JLabel avatarLabel = new JLabel("👨‍🏫");
        avatarLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 80));
        avatarLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel nameLabel = new JLabel(professorName);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        nameLabel.setForeground(TEXT_WHITE);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel codeLabel = new JLabel("Code: " + professorCode);
        codeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        codeLabel.setForeground(TEXT_GRAY);
        codeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        avatarSection.add(avatarLabel);
        avatarSection.add(Box.createRigidArea(new Dimension(0, 15)));
        avatarSection.add(nameLabel);
        avatarSection.add(Box.createRigidArea(new Dimension(0, 5)));
        avatarSection.add(codeLabel);
        
        JPanel infoSection = new JPanel();
        infoSection.setLayout(new BoxLayout(infoSection, BoxLayout.Y_AXIS));
        infoSection.setOpaque(false);
        
        infoSection.add(createInfoRow("📧 Email:", professorCode + "@usthb.dz"));
        infoSection.add(Box.createRigidArea(new Dimension(0, 15)));

        profileModulesValueLabel = new JLabel("—");
        profileModulesValueLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        profileModulesValueLabel.setForeground(TEXT_WHITE);
        infoSection.add(createInfoRow("📚 Modules:", profileModulesValueLabel));
        infoSection.add(Box.createRigidArea(new Dimension(0, 15)));

        profileStudentsValueLabel = new JLabel("—");
        profileStudentsValueLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        profileStudentsValueLabel.setForeground(TEXT_WHITE);
        infoSection.add(createInfoRow("👥 Étudiants:", profileStudentsValueLabel));
        infoSection.add(Box.createRigidArea(new Dimension(0, 15)));

        infoSection.add(createInfoRow("🏢 Département:", "Informatique"));
        
        profileContent.add(avatarSection, BorderLayout.NORTH);
        profileContent.add(infoSection, BorderLayout.CENTER);
        
        panel.add(profileContent, BorderLayout.CENTER);
        
        return panel;
    }

    private JPanel createInfoRow(String label, String value) {
        return createInfoRow(label, new JLabel(value));
    }

    private JPanel createInfoRow(String label, JLabel valueLabel) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        row.setOpaque(false);
        
        JLabel labelComp = new JLabel(label);
        labelComp.setFont(new Font("Segoe UI", Font.BOLD, 15));
        labelComp.setForeground(TEXT_GRAY);
        labelComp.setPreferredSize(new Dimension(150, 25));
        
        valueLabel.setPreferredSize(new Dimension(250, 25));
        
        row.add(labelComp);
        row.add(valueLabel);
        
        return row;
    }

    // ============================================================
    // UTILITAIRES VISUELS
    // ============================================================
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
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 10, 10);
                
                super.paintComponent(g);
            }
        };
        
        field.setOpaque(false);
        field.setForeground(Color.BLACK);
        field.setCaretColor(Color.BLACK);
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

    // ============================================================
    // NAVIGATION (CardLayout)
    // ============================================================
    private void setupNavigation(JPanel dynamicContent) {
        CardLayout cardLayout = (CardLayout) dynamicContent.getLayout();
        
        dashboardBtn.addActionListener(e -> switchPanel(cardLayout, dynamicContent, "dashboard"));
        unitsBtn.addActionListener(e     -> switchPanel(cardLayout, dynamicContent, "units"));
        studentsBtn.addActionListener(e  -> switchPanel(cardLayout, dynamicContent, "students"));
        gradesBtn.addActionListener(e    -> switchPanel(cardLayout, dynamicContent, "grades"));
        absencesBtn.addActionListener(e  -> switchPanel(cardLayout, dynamicContent, "absences"));
        reportsBtn.addActionListener(e   -> switchPanel(cardLayout, dynamicContent, "reports"));
        profileBtn.addActionListener(e   -> switchPanel(cardLayout, dynamicContent, "profile"));
    }

    private void switchPanel(CardLayout layout, JPanel container, String panelName) {
        layout.show(container, panelName);
        updateNavButtons(panelName);
    }

    private void updateNavButtons(String active) {
        JButton[] buttons = {dashboardBtn, unitsBtn, studentsBtn, gradesBtn, absencesBtn, reportsBtn, profileBtn};
        
        for (JButton btn : buttons) {
            btn.setForeground(TEXT_GRAY);
        }
        
        JButton activeBtn = null;
        switch (active) {
            case "dashboard": activeBtn = dashboardBtn; break;
            case "units":     activeBtn = unitsBtn;     break;
            case "students":  activeBtn = studentsBtn;  break;
            case "grades":    activeBtn = gradesBtn;    break;
            case "absences":  activeBtn = absencesBtn;  break;
            case "reports":   activeBtn = reportsBtn;   break;
            case "profile":   activeBtn = profileBtn;   break;
        }
        
        if (activeBtn != null) {
            activeBtn.setForeground(NEON_PURPLE);
        }
    }

    // ============================================================
    // ARRIÈRE-PLAN ANIMÉ & PANELS GLASS
    // ============================================================
    private void startBackgroundAnimation() {
        animationTimer = new Timer(50, e -> {
            animationProgress += 0.01f;
            if (animationProgress > 1f) {
                animationProgress = 0f;
            }
            repaint();
        });
        animationTimer.start();
    }

    class AnimatedBackgroundPanel extends JPanel {
        public AnimatedBackgroundPanel() {
            setOpaque(true);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gp = new GradientPaint(
                    0, 0, new Color(15, 15, 35),
                    getWidth(), getHeight(), new Color(30, 15, 60)
                );
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());

                float offset = animationProgress * 150;

                RadialGradientPaint rg1 = new RadialGradientPaint(
                    300 + offset, 200,
                    400,
                    new float[]{0f, 1f},
                    new Color[]{
                        new Color(NEON_PURPLE.getRed(), NEON_PURPLE.getGreen(), NEON_PURPLE.getBlue(), 20),
                        new Color(NEON_PURPLE.getRed(), NEON_PURPLE.getGreen(), NEON_PURPLE.getBlue(), 0)
                    }
                );
                g2.setPaint(rg1);
                g2.fillOval((int) (300 + offset - 400), -200, 800, 800);

                RadialGradientPaint rg2 = new RadialGradientPaint(
                    getWidth() - 300 - offset, getHeight() - 200,
                    450,
                    new float[]{0f, 1f},
                    new Color[]{
                        new Color(NEON_CYAN.getRed(), NEON_CYAN.getGreen(), NEON_CYAN.getBlue(), 20),
                        new Color(NEON_CYAN.getRed(), NEON_CYAN.getGreen(), NEON_CYAN.getBlue(), 0)
                    }
                );
                g2.setPaint(rg2);
                g2.fillOval(getWidth() - (int) (300 + offset) - 450, getHeight() - 650, 900, 900);
            } finally {
                g2.dispose();
            }
        }
    }

    class GlassPanel extends JPanel {
        public GlassPanel() {
            setOpaque(false);
        }

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
                    0, getHeight() / 3, new Color(255, 255, 255, 0)
                );
                g2.setPaint(shimmer);
                g2.fillRoundRect(0, 0, getWidth(), getHeight() / 3, 20, 20);
            } finally {
                g2.dispose();
            }

            super.paintChildren(g);
        }
    }

    class FuturisticCard extends JPanel {
        private JLabel valueLabel;
        private Color accentColor;
        
        public FuturisticCard(String title, String value, String icon, Color accentColor) {
            this.accentColor = accentColor;
            
            setOpaque(false);
            setLayout(new BorderLayout(15, 15));
            setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
            
            JLabel iconLabel = new JLabel(icon);
            iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 40));
            
            valueLabel = new JLabel(value);
            valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
            valueLabel.setForeground(accentColor);
            
            JLabel titleLabel = new JLabel(title);
            titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            titleLabel.setForeground(TEXT_GRAY);
            
            JPanel textPanel = new JPanel();
            textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
            textPanel.setOpaque(false);
            textPanel.add(valueLabel);
            textPanel.add(Box.createRigidArea(new Dimension(0, 5)));
            textPanel.add(titleLabel);
            
            add(iconLabel, BorderLayout.WEST);
            add(textPanel, BorderLayout.CENTER);
        }
        
        public void setValue(String value) {
            valueLabel.setText(value);
            repaint();
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            g2.setColor(GLASS_BG);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
            
            g2.setColor(new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 60));
            g2.setStroke(new BasicStroke(2f));
            g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 20, 20);
            
            GradientPaint shimmer = new GradientPaint(
                0, 0, new Color(255, 255, 255, 20),
                0, getHeight()/2, new Color(255, 255, 255, 0)
            );
            g2.setPaint(shimmer);
            g2.fillRoundRect(0, 0, getWidth(), getHeight()/2, 20, 20);
        }
    }

    // ============================================================
    // MÉTHODES APPELÉES PAR LE CONTRÔLEUR
    // ============================================================
    public void updateDashboardCards(int totalStudents, int totalModules,
                                     double averageGrade, int toGrade) {
        if (totalStudentsCard != null) totalStudentsCard.setValue(String.valueOf(totalStudents));
        if (modulesCard != null)       modulesCard.setValue(String.valueOf(totalModules));
        if (averageCard != null)       averageCard.setValue(String.format("%.2f", averageGrade));
        if (toGradeCard != null)       toGradeCard.setValue(String.valueOf(toGrade));
    }

    @SuppressWarnings("unchecked")
    public void updateReportStats(double successRate,
                                  double globalAverage,
                                  double presenceRate,
                                  long studentsInDifficulty) {

        updateStatCardValue(successRateCardPanel,
                successRate > 0 ? String.format("%.1f%%", successRate) : "-");
        updateStatCardValue(averageCardPanelReports,
                globalAverage > 0 ? String.format("%.2f/20", globalAverage) : "-");
        updateStatCardValue(presenceCardPanel,
                presenceRate > 0 ? String.format("%.1f%%", presenceRate) : "-");
        updateStatCardValue(difficultyCardPanel,
                String.valueOf(studentsInDifficulty));
    }

    private void updateStatCardValue(JPanel card, String value) {
        if (card == null) return;
        Object comp = card.getClientProperty("valueLabel");
        if (comp instanceof JLabel label) {
            label.setText(value);
            card.revalidate();
            card.repaint();
        }
    }

    // ⭐ Profil statique mis à jour par le contrôleur
    public void updateProfileStats(int moduleCount, int studentCount) {
        if (profileModulesValueLabel != null) {
            profileModulesValueLabel.setText(String.valueOf(moduleCount));
        }
        if (profileStudentsValueLabel != null) {
            profileStudentsValueLabel.setText(String.valueOf(studentCount));
        }
    }

    // ============================================================
    // GETTERS POUR LE CONTRÔLEUR
    // ============================================================
    public JButton getDashboardBtn()           { return dashboardBtn; }
    public JButton getUnitsBtn()               { return unitsBtn; }
    public JButton getStudentsBtn()            { return studentsBtn; }
    public JButton getGradesBtn()              { return gradesBtn; }
    public JButton getAbsencesBtn()            { return absencesBtn; }
    public JButton getReportsBtn()             { return reportsBtn; }
    public JButton getProfileBtn()             { return profileBtn; }
    public JButton getLogoutBtn()              { return logoutBtn; }
    public ModernButton getAddUnitBtn()        { return addBtn; }
    public ModernButton getEditUnitBtn()       { return editBtn; }
    public ModernButton getDeleteUnitBtn()     { return deleteBtn; }
    public ModernButton getSearchStudentBtn()  { return searchBtn; }
    public ModernButton getLoadStudentsBtn()   { return loadBtn; }
    public ModernButton getSaveGradesBtn()     { return saveBtn; }
    public ModernButton getCancelGradesBtn()   { return cancelBtn; }
    public ModernButton getExportBtn()         { return exportBtn; }
    public ModernButton getGenerateReportBtn() { return generateReportBtn; }
    public ModernButton getRecordAbsenceBtn()  { return recordAbsenceBtn; }
    public ModernButton getViewAbsenceDetailsBtn() { return viewAbsenceDetailsBtn; }
    public JTextField getSearchField()         { return searchField; }
    public JComboBox<String> getModuleFilter() { return moduleFilter; }
    public JComboBox<String> getModuleCombo()  { return moduleCombo; }
    public JComboBox<String> getTypeCombo()    { return typeCombo; }
    public ModernTable getStudentsTable()      { return studentsTable; }
    public ModernTable getGradesTable()        { return gradesTable; }
    public ModernTable getUnitsTable()         { return unitsTable; }
    public ModernTable getAbsencesTable()      { return absencesTable; }
}
