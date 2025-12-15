package view;

import view.components.ModernTheme;
import static view.components.ModernTheme.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.*;
import javax.swing.border.*;

/**
 * LoginView - Interface de Connexion Ultra-Moderne
 * Design inspiré des interfaces futuristes avec glassmorphism et animations
 */
public class LoginView extends JFrame {
    private static final long serialVersionUID = 1L;

    // Couleurs personnalisées pour le thème
    private static final Color BG_GRADIENT_START = new Color(15, 15, 35);
    private static final Color BG_GRADIENT_END   = new Color(30, 15, 60);
    private static final Color GLASS_BG          = new Color(255, 255, 255, 10);
    private static final Color GLASS_BORDER      = new Color(255, 255, 255, 30);
    private static final Color NEON_CYAN         = new Color(0, 255, 255);
    private static final Color NEON_PURPLE       = new Color(138, 43, 226);
    private static final Color NEON_PINK         = new Color(255, 20, 147);
    private static final Color TEXT_WHITE        = new Color(255, 255, 255);
    private static final Color TEXT_GRAY         = new Color(150, 150, 170);

    // Champs utilisés par le contrôleur
    public JTextField     user;
    public JPasswordField password;
    public JButton        loginButton;

    // Labels dynamiques pour les stats du panneau gauche
    private JLabel studentsStatLabel;
    private JLabel teachersStatLabel;
    private JLabel modulesStatLabel;

    private Timer animationTimer;
    private float animationProgress = 0f;

    public LoginView() {
        ModernTheme.fixPopupColors();

        setTitle("🚀 USTHB Portal - Future of Education");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setUndecorated(true);
        setSize(1200, 750);
        setLocationRelativeTo(null);

        // Panel principal avec gradient animé
        JPanel mainPanel = new AnimatedGradientPanel();
        mainPanel.setLayout(new GridBagLayout());

        // Glass card centrale
        JPanel glassCard = createGlassCard();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        mainPanel.add(glassCard, gbc);

        // Bouton fermer
        JButton closeBtn = createCloseButton();
        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setLayout(null);

        mainPanel.setBounds(0, 0, 1200, 750);
        closeBtn.setBounds(1150, 10, 40, 40);

        layeredPane.add(mainPanel, Integer.valueOf(0));
        layeredPane.add(closeBtn, Integer.valueOf(1));

        add(layeredPane);

        // Animation de fond
        startBackgroundAnimation();

        // Rendre la fenêtre draggable
        makeDraggable(layeredPane);
    }

    private JPanel createGlassCard() {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Effet glassmorphism
                g2.setColor(GLASS_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);

                // Bordure brillante
                g2.setStroke(new BasicStroke(2f));
                GradientPaint borderGradient = new GradientPaint(
                        0, 0, new Color(NEON_CYAN.getRed(), NEON_CYAN.getGreen(), NEON_CYAN.getBlue(), 100),
                        getWidth(), getHeight(), new Color(NEON_PURPLE.getRed(), NEON_PURPLE.getGreen(), NEON_PURPLE.getBlue(), 100)
                );
                g2.setPaint(borderGradient);
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 30, 30);

                // Reflet en haut
                GradientPaint shimmer = new GradientPaint(
                        0, 0, new Color(255, 255, 255, 40),
                        0, 100, new Color(255, 255, 255, 0)
                );
                g2.setPaint(shimmer);
                g2.fillRoundRect(10, 10, getWidth() - 20, 100, 25, 25);
            }
        };

        card.setOpaque(false);
        card.setPreferredSize(new Dimension(1000, 600));
        card.setLayout(new BorderLayout(0, 0));

        // Partie gauche - Branding
        JPanel leftPanel = createBrandingPanel();

        // Partie droite - Formulaire
        JPanel rightPanel = createFormPanel();

        card.add(leftPanel, BorderLayout.WEST);
        card.add(rightPanel, BorderLayout.CENTER);

        return card;
    }

    private JPanel createBrandingPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Gradient de fond
                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(NEON_PURPLE.getRed(), NEON_PURPLE.getGreen(), NEON_PURPLE.getBlue(), 150),
                        0, getHeight(), new Color(NEON_CYAN.getRed(), NEON_CYAN.getGreen(), NEON_CYAN.getBlue(), 100)
                );
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);

                // Cercles décoratifs animés
                float offset = animationProgress * 50;
                g2.setColor(new Color(255, 255, 255, 20));
                g2.fillOval(-50 + (int) offset, 100, 200, 200);
                g2.fillOval(200 - (int) offset, 300, 150, 150);
                g2.fillOval(50 + (int) offset, 450, 180, 180);
            }
        };

        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(450, 600));
        panel.setLayout(new GridBagLayout());

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        // Logo géant
        JLabel logoLabel = new JLabel("🎓");
        logoLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 120));
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Titre
        JLabel titleLabel = new JLabel("USTHB");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 56));
        titleLabel.setForeground(TEXT_WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Sous-titre avec effet néon
        JLabel subtitleLabel = new JLabel("PORTAIL ACADÉMIQUE") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(new Color(NEON_CYAN.getRed(), NEON_CYAN.getGreen(), NEON_CYAN.getBlue(), 50));
                g2.drawString(getText(), 2, getHeight() - 7);
                g2.drawString(getText(), -2, getHeight() - 7);
                g2.drawString(getText(), 0, getHeight() - 9);
                g2.drawString(getText(), 0, getHeight() - 5);

                super.paintComponent(g);
            }
        };
        subtitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        subtitleLabel.setForeground(NEON_CYAN);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel taglineLabel = new JLabel("« L'excellence à portée de clic »");
        taglineLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        taglineLabel.setForeground(TEXT_GRAY);
        taglineLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Stats animées
        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        statsPanel.setOpaque(false);
        statsPanel.setMaximumSize(new Dimension(400, 80));

        studentsStatLabel = new JLabel();
        teachersStatLabel = new JLabel();
        modulesStatLabel  = new JLabel();

        // valeurs par défaut (0), mises à jour par LoginController
        statsPanel.add(createStatCard("0", "Étudiants",  NEON_CYAN,   studentsStatLabel));
        statsPanel.add(createStatCard("0", "Enseignants", NEON_PURPLE, teachersStatLabel));
        statsPanel.add(createStatCard("0", "Modules",    NEON_PINK,   modulesStatLabel));

        content.add(logoLabel);
        content.add(Box.createVerticalStrut(20));
        content.add(titleLabel);
        content.add(Box.createVerticalStrut(10));
        content.add(subtitleLabel);
        content.add(Box.createVerticalStrut(10));
        content.add(taglineLabel);
        content.add(Box.createVerticalStrut(50));
        content.add(statsPanel);

        panel.add(content);

        return panel;
    }

    private JPanel createStatCard(String value, String label, Color accentColor, JLabel valueLabelOut) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(new Color(255, 255, 255, 20));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);

                g2.setColor(new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 100));
                g2.fillRoundRect(0, getHeight() - 3, getWidth(), 3, 3, 3);
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        valueLabel.setForeground(TEXT_WHITE);
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // si on a un label externe, on l'utilise pour pouvoir le mettre à jour ensuite
        if (valueLabelOut != null) {
            valueLabelOut.setFont(valueLabel.getFont());
            valueLabelOut.setForeground(valueLabel.getForeground());
            valueLabelOut.setAlignmentX(valueLabel.getAlignmentX());
            valueLabelOut.setText(value);
        }

        JLabel labelText = new JLabel(label);
        labelText.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        labelText.setForeground(TEXT_GRAY);
        labelText.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(valueLabelOut != null ? valueLabelOut : valueLabel);
        card.add(labelText);

        return card;
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(80, 60, 80, 60));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // Titre du formulaire
        JLabel welcomeLabel = new JLabel("Connexion");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 42));
        welcomeLabel.setForeground(TEXT_WHITE);
        welcomeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel infoLabel = new JLabel("Accédez à votre espace personnel");
        infoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        infoLabel.setForeground(TEXT_GRAY);
        infoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Champ Identifiant
        JLabel userLabel = new JLabel("IDENTIFIANT");
        userLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        userLabel.setForeground(NEON_CYAN);
        userLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        user = createModernTextField("Entrez votre identifiant");

        // Champ Mot de passe
        JLabel passLabel = new JLabel("MOT DE PASSE");
        passLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        passLabel.setForeground(NEON_PURPLE);
        passLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        password = createModernPasswordField("••••••••");

        // Options
        JPanel optionsPanel = new JPanel(new BorderLayout());
        optionsPanel.setOpaque(false);
        optionsPanel.setMaximumSize(new Dimension(450, 25));

        JCheckBox rememberMe = new JCheckBox("Se souvenir de moi");
        rememberMe.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        rememberMe.setForeground(TEXT_GRAY);
        rememberMe.setOpaque(false);
        rememberMe.setFocusPainted(false);

        JLabel forgotPassword = new JLabel("<html><u>Mot de passe oublié?</u></html>");
        forgotPassword.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        forgotPassword.setForeground(NEON_CYAN);
        forgotPassword.setCursor(new Cursor(Cursor.HAND_CURSOR));

        optionsPanel.add(rememberMe, BorderLayout.WEST);
        optionsPanel.add(forgotPassword, BorderLayout.EAST);

        // Bouton de connexion
        loginButton = createNeonButton();

        // Info supplémentaire
        JLabel helpLabel = new JLabel("Besoin d'aide? Contactez l'administration");
        helpLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        helpLabel.setForeground(new Color(TEXT_GRAY.getRed(), TEXT_GRAY.getGreen(), TEXT_GRAY.getBlue(), 150));
        helpLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Assemblage
        panel.add(welcomeLabel);
        panel.add(Box.createVerticalStrut(5));
        panel.add(infoLabel);
        panel.add(Box.createVerticalStrut(50));

        panel.add(userLabel);
        panel.add(Box.createVerticalStrut(8));
        panel.add(user);
        panel.add(Box.createVerticalStrut(25));

        panel.add(passLabel);
        panel.add(Box.createVerticalStrut(8));
        panel.add(password);
        panel.add(Box.createVerticalStrut(15));

        panel.add(optionsPanel);
        panel.add(Box.createVerticalStrut(35));

        panel.add(loginButton);
        panel.add(Box.createVerticalStrut(25));
        panel.add(helpLabel);

        return panel;
    }

    // --- Champs / Password / Boutons modernes (inchangés sauf facteur de forme) ---
    private JTextField createModernTextField(String placeholder) {
        JTextField field = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(new Color(255, 255, 255, 10));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);

                g2.setColor(hasFocus() ? NEON_CYAN : GLASS_BORDER);
                g2.setStroke(new BasicStroke(hasFocus() ? 2f : 1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);

                super.paintComponent(g);
            }
        };

        field.setOpaque(false);
        field.setForeground(TEXT_WHITE);
        field.setCaretColor(NEON_CYAN);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        field.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        field.setMaximumSize(new Dimension(450, 55));
        field.setPreferredSize(new Dimension(450, 55));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);

        field.setText(placeholder);
        field.setForeground(TEXT_GRAY);

        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(TEXT_WHITE);
                }
                field.repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (field.getText().isEmpty()) {
                    field.setText(placeholder);
                    field.setForeground(TEXT_GRAY);
                }
                field.repaint();
            }
        });

        return field;
    }

    private JPasswordField createModernPasswordField(String placeholder) {
        JPasswordField field = new JPasswordField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(new Color(255, 255, 255, 10));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);

                g2.setColor(hasFocus() ? NEON_PURPLE : GLASS_BORDER);
                g2.setStroke(new BasicStroke(hasFocus() ? 2f : 1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);

                super.paintComponent(g);
            }
        };

        field.setOpaque(false);
        field.setForeground(TEXT_WHITE);
        field.setCaretColor(NEON_PURPLE);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        field.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        field.setMaximumSize(new Dimension(450, 55));
        field.setPreferredSize(new Dimension(450, 55));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setEchoChar('•');

        return field;
    }

    private JButton createNeonButton() {
        JButton btn = new JButton("SE CONNECTER") {
            private boolean hover = false;

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gp = new GradientPaint(
                        0, 0, hover ? NEON_CYAN : NEON_PURPLE,
                        getWidth(), getHeight(), hover ? NEON_PURPLE : NEON_PINK
                );
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);

                if (hover) {
                    g2.setColor(new Color(255, 255, 255, 30));
                    g2.fillRoundRect(2, 2, getWidth() - 4, getHeight() / 2, 10, 10);
                }

                g2.setColor(TEXT_WHITE);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);
            }

            {
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

        btn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btn.setForeground(TEXT_WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(450, 55));
        btn.setPreferredSize(new Dimension(450, 55));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);

        return btn;
    }

    private JButton createCloseButton() {
        JButton btn = new JButton("✕") {
            private boolean hover = false;

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (hover) {
                    g2.setColor(new Color(255, 50, 50, 200));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                }

                g2.setColor(hover ? TEXT_WHITE : TEXT_GRAY);
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 20));
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth("✕")) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString("✕", x, y);
            }

            {
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

        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> System.exit(0));

        return btn;
    }

    private void startBackgroundAnimation() {
        animationTimer = new Timer(50, e -> {
            animationProgress += 0.02f;
            if (animationProgress > 1f) {
                animationProgress = 0f;
            }
            repaint();
        });
        animationTimer.start();
    }

    private void makeDraggable(Component component) {
        final Point[] dragOffset = {null};

        component.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                dragOffset[0] = e.getPoint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                dragOffset[0] = null;
            }
        });

        component.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (dragOffset[0] != null) {
                    Point location = getLocation();
                    setLocation(
                            location.x + e.getX() - dragOffset[0].x,
                            location.y + e.getY() - dragOffset[0].y
                    );
                }
            }
        });
    }

    // Panel avec gradient animé
    class AnimatedGradientPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            GradientPaint gp = new GradientPaint(
                    0, 0, BG_GRADIENT_START,
                    getWidth(), getHeight(), BG_GRADIENT_END
            );
            g2.setPaint(gp);
            g2.fillRect(0, 0, getWidth(), getHeight());

            float offset = animationProgress * 100;

            RadialGradientPaint rg1 = new RadialGradientPaint(
                    200 + offset, 200,
                    300,
                    new float[]{0f, 1f},
                    new Color[]{
                            new Color(NEON_CYAN.getRed(), NEON_CYAN.getGreen(), NEON_CYAN.getBlue(), 30),
                            new Color(NEON_CYAN.getRed(), NEON_CYAN.getGreen(), NEON_CYAN.getBlue(), 0)
                    }
            );
            g2.setPaint(rg1);
            g2.fillOval((int) (200 + offset - 300), -100, 600, 600);

            RadialGradientPaint rg2 = new RadialGradientPaint(
                    getWidth() - 200 - offset, getHeight() - 200,
                    350,
                    new float[]{0f, 1f},
                    new Color[]{
                            new Color(NEON_PURPLE.getRed(), NEON_PURPLE.getGreen(), NEON_PURPLE.getBlue(), 30),
                            new Color(NEON_PURPLE.getRed(), NEON_PURPLE.getGreen(), NEON_PURPLE.getBlue(), 0)
                    }
            );
            g2.setPaint(rg2);
            g2.fillOval(getWidth() - (int) (200 + offset) - 350, getHeight() - 550, 700, 700);

            g2.setColor(new Color(255, 255, 255, 10));
            for (int i = 0; i < getWidth(); i += 40) {
                for (int j = 0; j < getHeight(); j += 40) {
                    g2.fillOval(i, j, 2, 2);
                }
            }
        }
    }

    // ---------------------------------------------------------------------
    // Méthode appelée par LoginController pour mettre à jour les stats réelles
    // ---------------------------------------------------------------------
    public void updateLoginStats(int totalStudents, int totalTeachers, int totalModules) {
        if (studentsStatLabel != null) {
            studentsStatLabel.setText(String.valueOf(totalStudents));
        }
        if (teachersStatLabel != null) {
            teachersStatLabel.setText(String.valueOf(totalTeachers));
        }
        if (modulesStatLabel != null) {
            modulesStatLabel.setText(String.valueOf(totalModules));
        }
    }
}
