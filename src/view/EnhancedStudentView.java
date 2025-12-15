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
 * EnhancedStudentView - Interface Étudiant Ultra-Moderne
 * Design futuriste avec glassmorphism, animations et effets néon
 */
public class EnhancedStudentView extends JFrame {
    private static final long serialVersionUID = 1L;

    // Couleurs futuristes
    private final Color BG_DARK      = new Color(15, 15, 35);
    private final Color GLASS_BG     = new Color(255, 255, 255, 8);
    private final Color GLASS_BORDER = new Color(255, 255, 255, 25);
    private final Color NEON_CYAN    = new Color(0, 255, 255);
    private final Color NEON_PURPLE  = new Color(138, 43, 226);
    private final Color NEON_PINK    = new Color(255, 20, 147);
    private final Color NEON_GREEN   = new Color(57, 255, 20);
    private final Color TEXT_WHITE   = new Color(255, 255, 255);
    private final Color TEXT_GRAY    = new Color(150, 150, 170);
    private final Color SIDEBAR_BG   = new Color(20, 20, 45);

    // Navigation
    private JPanel  dynamicContent;
    private JPanel  dashboardPanel;
    private JPanel  gradesPanel;
    private JPanel  absencesPanel;
    private JPanel  profilePanel;

    private JButton dashboardBtn;
    private JButton gradesBtn;
    private JButton absencesBtn;
    private JButton profileBtn;
    private JButton logoutBtn;

    // Données étudiant
    private final String studentName;
    private final String studentCode;

    // Cards
    private ModernCard averageCard;
    private ModernCard creditsCard;
    private ModernCard absencesCard;
    private ModernCard rankCard;
    private ModernTable gradesTable;
    private ModernTable absencesTable;

    // Contrôles
    private ModernButton exportBtn;
    private ModernButton changePasswordBtn;
    private JComboBox<String> semesterFilter;

    // Animation fond
    private Timer animationTimer;
    private float animationProgress = 0f;

    private final NotificationCenter notificationCenter;

    public EnhancedStudentView(String studentName, String studentCode) {
        this.studentName  = studentName;
        this.studentCode  = studentCode;
        this.notificationCenter = new NotificationCenter(this, studentCode);

        fixAllPopupColors();
        initializeComponents();
        setupUI();

        setTitle("🎓 Espace Étudiant - USTHB Portal");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);

        startBackgroundAnimation();
    }

    /**
     * Force les couleurs Swing par défaut des popups et composants génériques
     * pour garantir noir sur fond clair (lisibilité).
     */
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
        averageCard  = new ModernCard("Moyenne Générale", "15.50", "📊", NEON_CYAN);
        creditsCard  = new ModernCard("Crédits Validés", "45/60", "✓", NEON_GREEN);
        absencesCard = new ModernCard("Absences", "3", "⚠", NEON_PINK);
        rankCard     = new ModernCard("Classement", "12/150", "🏆", NEON_PURPLE);
    }

    private void setupUI() {
        JPanel container = new AnimatedBackgroundPanel();
        container.setLayout(new BorderLayout());

        JPanel sidebar = createFuturisticSidebar();
        container.add(sidebar, BorderLayout.WEST);

        JPanel contentArea = new JPanel(new BorderLayout());
        contentArea.setOpaque(false);
        contentArea.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        JPanel header = createGlassHeaderWithNotifications();
        contentArea.add(header, BorderLayout.NORTH);

        dynamicContent = new JPanel(new CardLayout());
        dynamicContent.setOpaque(false);

        dashboardPanel = createDashboardPanel();
        gradesPanel    = createGradesPanel();
        absencesPanel  = createAbsencesPanel();
        profilePanel   = createProfilePanel();

        dynamicContent.add(dashboardPanel, "dashboard");
        dynamicContent.add(gradesPanel,    "grades");
        dynamicContent.add(absencesPanel,  "absences");
        dynamicContent.add(profilePanel,   "profile");

        contentArea.add(dynamicContent, BorderLayout.CENTER);

        container.add(contentArea, BorderLayout.CENTER);
        add(container);

        setupNavigation();
        showPanel("dashboard");
    }

    // -------------------------------------------------------------------------
    // HEADER
    // -------------------------------------------------------------------------
    private JPanel createGlassHeaderWithNotifications() {
        JPanel header = new GlassPanel();
        header.setLayout(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        JLabel titleLabel = new JLabel("Bienvenue, " + studentName);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(TEXT_WHITE);

        JLabel codeLabel = new JLabel("Matricule: " + studentCode);
        codeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        codeLabel.setForeground(TEXT_GRAY);

        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setOpaque(false);
        leftPanel.add(titleLabel);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        leftPanel.add(codeLabel);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 0));
        rightPanel.setOpaque(false);

        // Notifications
        rightPanel.add(notificationCenter.getComponent());

        JLabel dateLabel = new JLabel("📅 " + java.time.LocalDate.now());
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

    // -------------------------------------------------------------------------
    // BACKGROUND ANIMÉ + GLASS PANEL
    // -------------------------------------------------------------------------
    class AnimatedBackgroundPanel extends JPanel {
        private final java.util.List<Particle> particles = new java.util.ArrayList<>();

        public AnimatedBackgroundPanel() {
            setOpaque(true);
            for (int i = 0; i < 50; i++) {
                particles.add(new Particle());
            }
            new Timer(50, e -> repaint()).start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(15, 15, 35),
                    getWidth(), getHeight(), new Color(30, 15, 50)
                );
                g2.setPaint(gradient);
                g2.fillRect(0, 0, getWidth(), getHeight());

                for (Particle p : particles) {
                    p.update(getWidth(), getHeight());
                    p.draw(g2);
                }
            } finally {
                g2.dispose();
            }
        }
    }

    class Particle {
        float x, y, vx, vy, size;
        Color color;

        public Particle() {
            reset((float) Math.random() * 1920, (float) Math.random() * 1080);
        }

        void reset(float px, float py) {
            x = px;
            y = py;
            vx = (float) (Math.random() - 0.5) * 0.5f;
            vy = (float) (Math.random() - 0.5) * 0.5f;
            size = (float) Math.random() * 3 + 1;

            Color[] colors = {NEON_CYAN, NEON_PURPLE, NEON_PINK, NEON_GREEN};
            color = colors[(int) (Math.random() * colors.length)];
        }

        void update(int width, int height) {
            x += vx;
            y += vy;
            if (x < 0 || x > width || y < 0 || y > height) {
                reset((float) Math.random() * width, (float) Math.random() * height);
            }
        }

        void draw(Graphics2D g2) {
            g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 100));
            g2.fill(new Ellipse2D.Float(x, y, size, size));

            g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 30));
            g2.fill(new Ellipse2D.Float(x - size, y - size, size * 3, size * 3));
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
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
            } finally {
                g2.dispose();
            }
            super.paintChildren(g);
        }
    }

    private void startBackgroundAnimation() {
        animationTimer = new Timer(50, e -> {
            animationProgress += 0.02f;
            if (animationProgress > 1f) animationProgress = 0f;
            repaint();
        });
        animationTimer.start();
    }

    // -------------------------------------------------------------------------
    // NAVIGATION LATERALE
    // -------------------------------------------------------------------------
    private void setupNavigation() {
        dashboardBtn.addActionListener(e -> showPanel("dashboard"));
        gradesBtn.addActionListener(e -> showPanel("grades"));
        absencesBtn.addActionListener(e -> showPanel("absences"));
        profileBtn.addActionListener(e -> showPanel("profile"));
    }

    private void showPanel(String panelName) {
        CardLayout cl = (CardLayout) dynamicContent.getLayout();
        cl.show(dynamicContent, panelName);
        updateNavButtons(panelName);
    }

    private void updateNavButtons(String active) {
        JButton[] buttons = {dashboardBtn, gradesBtn, absencesBtn, profileBtn};

        for (JButton btn : buttons) {
            btn.setForeground(TEXT_GRAY);
            btn.setBackground(SIDEBAR_BG);
        }

        JButton activeBtn = switch (active) {
            case "dashboard" -> dashboardBtn;
            case "grades"    -> gradesBtn;
            case "absences"  -> absencesBtn;
            case "profile"   -> profileBtn;
            default -> null;
        };

        if (activeBtn != null) {
            activeBtn.setForeground(NEON_CYAN);
            activeBtn.setBackground(new Color(40, 40, 70));
        }
    }

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
                g2.setColor(new Color(NEON_CYAN.getRed(), NEON_CYAN.getGreen(), NEON_CYAN.getBlue(), 30));
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

        JLabel subtitle = new JLabel("Espace Étudiant");
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
        gradesBtn    = createNeonNavButton("📝  Mes Notes", false);
        absencesBtn  = createNeonNavButton("📅  Absences", false);
        profileBtn   = createNeonNavButton("👤  Mon Profil", false);

        sidebar.add(dashboardBtn);
        sidebar.add(Box.createRigidArea(new Dimension(0, 12)));
        sidebar.add(gradesBtn);
        sidebar.add(Box.createRigidArea(new Dimension(0, 12)));
        sidebar.add(absencesBtn);
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
                        g2.setColor(new Color(255, 255, 255, 15));
                        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                    }
                    if (selected) {
                        g2.setColor(NEON_CYAN);
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
                        if (!getForeground().equals(NEON_CYAN)) {
                            hover = true;
                            repaint();
                        }
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
        btn.setForeground(selected ? NEON_CYAN : TEXT_GRAY);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        btn.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));
        return btn;
    }

    private JSeparator createNeonSeparator() {
        return new JSeparator() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(
                    0, 0, new Color(NEON_CYAN.getRed(), NEON_CYAN.getGreen(), NEON_CYAN.getBlue(), 0),
                    getWidth() / 2, 0, new Color(NEON_CYAN.getRed(), NEON_CYAN.getGreen(), NEON_CYAN.getBlue(), 100)
                );
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth() / 2, 2);

                gp = new GradientPaint(
                    getWidth() / 2, 0, new Color(NEON_CYAN.getRed(), NEON_CYAN.getGreen(), NEON_CYAN.getBlue(), 100),
                    getWidth(), 0, new Color(NEON_CYAN.getRed(), NEON_CYAN.getGreen(), NEON_CYAN.getBlue(), 0)
                );
                g2.setPaint(gp);
                g2.fillRect(getWidth() / 2, 0, getWidth() / 2, 2);
            }
        };
    }

    // -------------------------------------------------------------------------
    // STYLES TABLES / COMBOS
    // -------------------------------------------------------------------------
    private void styleComboBox(JComboBox<?> combo) {
        combo.setForeground(TEXT_WHITE);
        combo.setBackground(new Color(30, 30, 60));
        combo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(GLASS_BORDER, 1),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
    }

    private void styleTableHeader(JTable table) {
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(new Color(30, 30, 60));
        header.setForeground(NEON_CYAN);
        header.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        header.setPreferredSize(new Dimension(header.getWidth(), 50));

        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer();
        headerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        headerRenderer.setBackground(new Color(30, 30, 60));
        headerRenderer.setForeground(NEON_CYAN);
        table.getTableHeader().setDefaultRenderer(headerRenderer);
    }

    private void styleTable(JTable table) {
        table.setRowHeight(45);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setBackground(new Color(20, 20, 40, 200));
        table.setForeground(TEXT_WHITE);
        table.setSelectionBackground(new Color(NEON_CYAN.getRed(), NEON_CYAN.getGreen(), NEON_CYAN.getBlue(), 30));
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
                    c.setBackground(new Color(NEON_CYAN.getRed(), NEON_CYAN.getGreen(), NEON_CYAN.getBlue(), 30));
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
 private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout(25, 25));
        panel.setOpaque(false);
        
        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 20, 20));
        statsPanel.setOpaque(false);
        statsPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 25, 0));
        
        statsPanel.add(averageCard);
        statsPanel.add(creditsCard);
        statsPanel.add(absencesCard);
        statsPanel.add(rankCard);
        
        panel.add(statsPanel, BorderLayout.NORTH);
        
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 25, 25));
        centerPanel.setOpaque(false);
        
        centerPanel.add(createRecentGradesPanel());
        centerPanel.add(createCalendarPanel());
        
        panel.add(centerPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createRecentGradesPanel() {
        JPanel panel = new GlassPanel();
        panel.setLayout(new BorderLayout(15, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        
        JLabel title = new JLabel("📝 Notes Récentes");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(TEXT_WHITE);
        
        String[] columns = {"Module", "Note", "Type", "Date"};
        Object[][] data = {
            {"Génie Logiciel", "16.50", "Examen", "2024-11-20"},
            {"Base de Données", "15.00", "CC", "2024-11-18"},
            {"Intelligence Artificielle", "17.00", "TP", "2024-11-15"},
            {"Réseaux", "14.50", "TD", "2024-11-10"}
        };
        
        DefaultTableModel model = new DefaultTableModel(data, columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        ModernTable table = new ModernTable(model);
        table.setOpaque(false);
        styleTableHeader(table);
        styleTable(table);
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        
        panel.add(title, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createCalendarPanel() {
        JPanel panel = new GlassPanel();
        panel.setLayout(new BorderLayout(15, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        
        JLabel title = new JLabel("📅 Événements à Venir");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(TEXT_WHITE);
        
        JPanel eventsPanel = new JPanel();
        eventsPanel.setLayout(new BoxLayout(eventsPanel, BoxLayout.Y_AXIS));
        eventsPanel.setOpaque(false);
        
        eventsPanel.add(createEventItem("Examen Génie Logiciel", "05 Déc 2024", NEON_PINK));
        eventsPanel.add(Box.createRigidArea(new Dimension(0, 12)));
        eventsPanel.add(createEventItem("TP Intelligence Artificielle", "08 Déc 2024", NEON_CYAN));
        eventsPanel.add(Box.createRigidArea(new Dimension(0, 12)));
        eventsPanel.add(createEventItem("TD Base de Données", "10 Déc 2024", NEON_PURPLE));
        
        panel.add(title, BorderLayout.NORTH);
        panel.add(eventsPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createEventItem(String event, String date, Color accentColor) {
        JPanel panel = new JPanel(new BorderLayout(15, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                g2.setColor(new Color(255, 255, 255, 10));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                
                g2.setColor(accentColor);
                g2.fillRoundRect(0, 0, 5, getHeight(), 12, 12);
                
                super.paintComponent(g);
            }
        };
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        JLabel eventLabel = new JLabel(event);
        eventLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        eventLabel.setForeground(TEXT_WHITE);
        
        JLabel dateLabel = new JLabel(date);
        dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        dateLabel.setForeground(TEXT_GRAY);
        
        panel.add(eventLabel, BorderLayout.CENTER);
        panel.add(dateLabel, BorderLayout.EAST);
        
        return panel;
    }
    
    private JPanel createGradesPanel() {
        JPanel panel = new JPanel(new BorderLayout(25, 25));
        panel.setOpaque(false);
        
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        
        JLabel title = new JLabel("📝 Mes Notes");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(TEXT_WHITE); // Utilisation de la couleur de texte claire définie [cite: 124]
        
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        filterPanel.setOpaque(false);
        
        semesterFilter = new JComboBox<>(new String[]{"Tous les semestres", "Semestre 1", "Semestre 2"});
        semesterFilter.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        styleComboBox(semesterFilter); // Application du style futuriste à la ComboBox [cite: 205]
        
        exportBtn = new ModernButton("📥 Exporter", NEON_CYAN); // Bouton avec accent Cyan néon [cite: 122]
        
        JLabel filterLabel = new JLabel("Semestre: ");
        filterLabel.setForeground(TEXT_GRAY);
        filterLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        filterPanel.add(filterLabel);
        filterPanel.add(semesterFilter);
        filterPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        filterPanel.add(exportBtn);
        
        topPanel.add(title, BorderLayout.WEST);
        topPanel.add(filterPanel, BorderLayout.EAST);
        
        panel.add(topPanel, BorderLayout.NORTH);
        
        // Création de la table avec le style "Glass" et néon
        JPanel tableContainer = new GlassPanel();
        tableContainer.setLayout(new BorderLayout());
        tableContainer.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        String[] columns = {"Module", "Code", "Note", "Coef", "Type", "Date", "Mention"};
        Object[][] data = {
            {"Génie Logiciel", "GL01", "16.50", "2.0", "Examen", "2024-11-20", "Très Bien"},
            {"Base de Données", "BD02", "15.00", "1.5", "CC", "2024-11-18", "Bien"},
            {"Intelligence Artificielle", "IA03", "17.00", "2.0", "TP", "2024-11-15", "Très Bien"},
            {"Réseaux", "RS04", "14.50", "1.0", "TD", "2024-11-10", "Bien"},
            {"Systèmes d'Exploitation", "SE05", "13.00", "1.5", "Examen", "2024-11-05", "Bien"}
        };
        
        DefaultTableModel model = new DefaultTableModel(data, columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
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

        tableContainer.add(scrollPane, BorderLayout.CENTER);
        panel.add(tableContainer, BorderLayout.CENTER);

        return panel;
    }
    
    private JPanel createAbsencesPanel() {
        JPanel panel = new JPanel(new BorderLayout(25, 25));
        panel.setOpaque(false);
        
        JLabel title = new JLabel("📅 Mes Absences");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(TEXT_WHITE);
        panel.add(title, BorderLayout.NORTH);
        
        // Conteneur Glassmorphism pour la table
        JPanel tableContainer = new GlassPanel();
        tableContainer.setLayout(new BorderLayout());
        tableContainer.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        String[] columns = {"Module", "Date", "Type", "Statut", "Motif"};
        Object[][] data = {
            {"Génie Logiciel", "2024-11-15", "Cours", "Non justifiée", "-"},
            {"Base de Données", "2024-11-10", "TD", "Justifiée", "Maladie"},
            {"Intelligence Artificielle", "2024-11-05", "TP", "Non justifiée", "-"}
        };
        
        DefaultTableModel model = new DefaultTableModel(data, columns) {
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

        tableContainer.add(scrollPane, BorderLayout.CENTER);
        panel.add(tableContainer, BorderLayout.CENTER);

        return panel;
    }
    
    private JPanel createProfilePanel() {
        JPanel panel = new JPanel(new BorderLayout(25, 25));
        panel.setOpaque(false);
        
        JLabel title = new JLabel("👤 Mon Profil");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(TEXT_WHITE);
        panel.add(title, BorderLayout.NORTH);
        
        // Utilisation du GlassPanel au lieu du JPanel blanc standard [cite: 161]
        JPanel formPanel = new GlassPanel();
        formPanel.setLayout(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        
        // Ajout des champs avec le style de texte blanc/gris défini [cite: 124]
        addProfileField(formPanel, gbc, 0, "Nom complet:", studentName);
        addProfileField(formPanel, gbc, 1, "Matricule:", studentCode);
        addProfileField(formPanel, gbc, 2, "Spécialité:", "Intelligence Artificielle");
        addProfileField(formPanel, gbc, 3, "Année:", "3ème année Master");
        
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(30, 15, 15, 15);
        
        // Bouton de mot de passe avec accent néon violet [cite: 122]
        changePasswordBtn = new ModernButton("🔒 Changer le mot de passe", NEON_PURPLE);
        formPanel.add(changePasswordBtn, gbc);
        
        // Ajout d'un panneau décoratif "Statistiques Rapides" à droite pour imiter l'image cible
        JPanel contentGrid = new JPanel(new GridLayout(1, 2, 25, 0));
        contentGrid.setOpaque(false);
        contentGrid.add(formPanel);
        contentGrid.add(createEfficiencyChartPanel());
        
        panel.add(contentGrid, BorderLayout.CENTER);
        return panel;
    }
    
    // Helper pour les champs de profil
    private void addProfileField(JPanel panel, GridBagConstraints gbc, int row, String label, String value) {
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1;
        JLabel labelComp = new JLabel(label);
        labelComp.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        labelComp.setForeground(TEXT_GRAY);
        panel.add(labelComp, gbc);
        
        gbc.gridx = 1;
        JLabel valueComp = new JLabel(value);
        valueComp.setFont(new Font("Segoe UI", Font.BOLD, 16));
        valueComp.setForeground(TEXT_WHITE);
        panel.add(valueComp, gbc);
    }

    // Création d'un panneau de type "Efficiency" (graphiques circulaires) comme sur l'image
    private JPanel createEfficiencyChartPanel() {
        JPanel panel = new GlassPanel();
        panel.setLayout(new BorderLayout(20, 20));
        panel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        
        JLabel title = new JLabel("EFFICACITÉ");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(TEXT_GRAY);
        panel.add(title, BorderLayout.NORTH);
        
        JPanel chartsContainer = new JPanel(new GridLayout(1, 3, 15, 0));
        chartsContainer.setOpaque(false);
        // Utilisation des couleurs néon définies pour les graphiques [cite: 122, 123]
        chartsContainer.add(createDonutChart("75%", NEON_CYAN));
        chartsContainer.add(createDonutChart("88%", NEON_PURPLE));
        chartsContainer.add(createDonutChart("62%", NEON_PINK));
        
        panel.add(chartsContainer, BorderLayout.CENTER);
        return panel;
    }
    
    // Composant personnalisé pour un graphique circulaire néon
    private JPanel createDonutChart(String percentage, Color color) {
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int s = Math.min(getWidth(), getHeight());
                int thickness = 10;
                int x = (getWidth() - s) / 2 + thickness/2;
                int y = (getHeight() - s) / 2 + thickness/2;
                int diameter = s - thickness;
                
                // Cercle de fond (gris foncé)
                g2.setColor(new Color(40, 40, 80));
                g2.setStroke(new BasicStroke(thickness));
                g2.drawOval(x, y, diameter, diameter);
                
                // Arc de progression (couleur néon avec effet de lueur)
                g2.setColor(color);
                int angle = (int)(Double.parseDouble(percentage.replace("%", "")) * 3.6);
                g2.drawArc(x, y, diameter, diameter, 90, -angle);
                
                // Texte central
                g2.setFont(new Font("Segoe UI", Font.BOLD, 18));
                g2.setBackground(TEXT_WHITE);
                FontMetrics fm = g2.getFontMetrics();
                int textX = (getWidth() - fm.stringWidth(percentage)) / 2;
                int textY = (getHeight() + fm.getAscent()) / 2 - 2;
                g2.drawString(percentage, textX, textY);
            }
            { setOpaque(false); setPreferredSize(new Dimension(100, 100)); }
        };
    }

    // Getters (similaires à la source 1 mais adaptés au contexte)
    public JButton getDashboardBtn() { return dashboardBtn; }
    public JButton getGradesBtn() { return gradesBtn; }
    public JButton getAbsencesBtn() { return absencesBtn; }
    public JButton getProfileBtn() { return profileBtn; }
    public JButton getLogoutBtn() { return logoutBtn; }
    public ModernButton getExportBtn() { return exportBtn; }
    public ModernButton getChangePasswordBtn() { return changePasswordBtn; }
    public JComboBox<String> getSemesterFilter() { return semesterFilter; }
    
    public void updateAverageCard(String value) { averageCard.setValue(value); }
    public void updateCreditsCard(String value) { creditsCard.setValue(value); }
    public void updateAbsencesCard(String value) { absencesCard.setValue(value); }
    public void updateRankCard(String value) { rankCard.setValue(value); }
    public ModernTable getGradesTable()   { return gradesTable; }
    public ModernTable getAbsencesTable() { return absencesTable; }

}