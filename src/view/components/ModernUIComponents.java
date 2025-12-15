package view.components;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Bibliothèque de composants UI modernes et réutilisables
 * Design moderne avec animations et effets visuels
 */
public class ModernUIComponents {

    // =========================================================================
    // PALETTE DE COULEURS
    // =========================================================================
    public static class ColorPalette {
        public static final Color PRIMARY   = new Color(70, 130, 240);
        public static final Color PRIMARY_DARK = new Color(50, 100, 200);
        public static final Color SECONDARY = new Color(108, 117, 125);
        public static final Color SUCCESS   = new Color(40, 167, 69);
        public static final Color DANGER    = new Color(220, 53, 69);
        public static final Color WARNING   = new Color(255, 193, 7);
        public static final Color INFO      = new Color(23, 162, 184);
        public static final Color LIGHT     = new Color(248, 249, 250);
        public static final Color DARK      = new Color(52, 58, 64);
        public static final Color WHITE     = Color.WHITE;
        public static final Color BORDER    = new Color(220, 220, 220);
        public static final Color HOVER     = new Color(240, 245, 250);
        public static final Color TEXT_PRIMARY   = new Color(33, 37, 41);
        public static final Color TEXT_SECONDARY = new Color(108, 117, 125);
    }

    // =========================================================================
    // BOUTON MODERNE
    // =========================================================================
    /**
     * Bouton moderne avec effets hover et animations
     * - Couleur de texte automatique (noir sur fond clair, blanc sur fond sombre)
     */
    public static class ModernButton extends JButton {

        private Color backgroundColor;
        private Color hoverColor;
        private boolean isHovered = false;

        public ModernButton(String text) {
            this(text, ColorPalette.PRIMARY);
        }

        public ModernButton(String text, Color backgroundColor) {
            super(text);
            this.backgroundColor = backgroundColor;
            this.hoverColor = darkerColor(backgroundColor, 0.85f);

            setupButton();
            setupListeners();
        }

        private void setupButton() {
            // Couleur de fond initiale
            setBackground(backgroundColor);

            // Couleur du texte en fonction de la luminosité du fond
            updateForegroundColor();

            setFont(new Font("Segoe UI", Font.BOLD, 14));
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(true);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setBorder(BorderFactory.createEmptyBorder(12, 24, 12, 24));
        }

        private void setupListeners() {
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    isHovered = true;
                    animateColor(backgroundColor, hoverColor);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    isHovered = false;
                    animateColor(hoverColor, backgroundColor);
                }
            });
        }

        private void animateColor(Color from, Color to) {
            Timer timer = new Timer(10, null);
            final int steps = 10;
            final int[] step = {0};

            timer.addActionListener(e -> {
                step[0]++;
                float ratio = (float) step[0] / steps;

                int r = (int) (from.getRed()   + ratio * (to.getRed()   - from.getRed()));
                int g = (int) (from.getGreen() + ratio * (to.getGreen() - from.getGreen()));
                int b = (int) (from.getBlue()  + ratio * (to.getBlue()  - from.getBlue()));

                setBackground(new Color(r, g, b));

                if (step[0] >= steps) {
                    ((Timer) e.getSource()).stop();
                }
            });

            timer.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Ombre portée
            if (isHovered) {
                g2.setColor(new Color(0, 0, 0, 30));
                g2.fillRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 8, 8);
            }

            // Fond du bouton
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);

            g2.dispose();
            super.paintComponent(g);
        }

        /**
         * Met à jour la couleur du texte en fonction de la luminosité de backgroundColor.
         */
        private void updateForegroundColor() {
            if (isColorLight(backgroundColor)) {
                setForeground(Color.BLACK);
            } else {
                setForeground(Color.WHITE);
            }
        }

        /**
         * Permet de changer dynamiquement la couleur de fond tout en gardant le contraste.
         */
        public void setBackgroundColor(Color color) {
            this.backgroundColor = color;
            this.hoverColor = darkerColor(color, 0.85f);
            updateForegroundColor();
            setBackground(color);
            repaint();
        }

        public Color getBackgroundColor() {
            return backgroundColor;
        }

        /**
         * Détermine si une couleur est "claire" (true) ou "sombre".
         */
        private static boolean isColorLight(Color c) {
            // Luminance relative approximative
            double luminance = (0.2126 * c.getRed()
                              + 0.7152 * c.getGreen()
                              + 0.0722 * c.getBlue()) / 255.0;
            // Seuil : > 0.6 => couleur claire -> texte noir, sinon texte blanc
            return luminance > 0.6;
        }
    }

    // =========================================================================
    // CHAMP DE TEXTE MODERNE
    // =========================================================================
    /**
     * Champ de texte moderne avec label flottant
     */
    public static class ModernTextField extends JPanel {
        private JLabel label;
        private JTextField textField;
        private String placeholder;

        public ModernTextField(String labelText) {
            this(labelText, "");
        }

        public ModernTextField(String labelText, String placeholder) {
            this.placeholder = placeholder;

            setLayout(new BorderLayout(0, 5));
            setBackground(Color.WHITE);

            // Label
            label = new JLabel(labelText);
            label.setFont(new Font("Segoe UI", Font.BOLD, 12));
            label.setForeground(ColorPalette.TEXT_SECONDARY);

            // TextField
            textField = new JTextField() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    if (getText().isEmpty() && !placeholder.isEmpty() && !isFocusOwner()) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setColor(ColorPalette.TEXT_SECONDARY);
                        g2.setFont(getFont().deriveFont(Font.ITALIC));
                        g2.drawString(
                            placeholder,
                            getInsets().left,
                            g.getFontMetrics().getMaxAscent() + getInsets().top
                        );
                        g2.dispose();
                    }
                }
            };

            textField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            textField.setForeground(ColorPalette.TEXT_PRIMARY);
            textField.setBackground(ColorPalette.LIGHT);
            textField.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(ColorPalette.BORDER, 1, 8),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
            ));

            // Focus listener pour animation
            textField.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    textField.setBorder(BorderFactory.createCompoundBorder(
                        new RoundedBorder(ColorPalette.PRIMARY, 2, 8),
                        BorderFactory.createEmptyBorder(10, 15, 10, 15)
                    ));
                    label.setForeground(ColorPalette.PRIMARY);
                    repaint();
                }

                @Override
                public void focusLost(FocusEvent e) {
                    textField.setBorder(BorderFactory.createCompoundBorder(
                        new RoundedBorder(ColorPalette.BORDER, 1, 8),
                        BorderFactory.createEmptyBorder(10, 15, 10, 15)
                    ));
                    label.setForeground(ColorPalette.TEXT_SECONDARY);
                    repaint();
                }
            });

            add(label, BorderLayout.NORTH);
            add(textField, BorderLayout.CENTER);
        }

        public String getText() {
            return textField.getText();
        }

        public void setText(String text) {
            textField.setText(text);
        }

        public JTextField getTextField() {
            return textField;
        }
    }

    // =========================================================================
    // CARD MODERNE
    // =========================================================================
    /**
     * Card moderne pour afficher des informations
     */
    public static class ModernCard extends JPanel {
        private JLabel titleLabel;
        private JLabel valueLabel;
        private JLabel iconLabel;

        public ModernCard(String title, String value, String icon, Color accentColor) {
            setLayout(new BorderLayout(15, 15));
            setBackground(Color.WHITE);
            setBorder(new CompoundBorder(
                new RoundedBorder(ColorPalette.BORDER, 1, 12),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
            ));

            // Panel gauche avec icône
            JPanel leftPanel = new JPanel(new BorderLayout());
            leftPanel.setOpaque(false);

            iconLabel = new JLabel(icon);
            iconLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
            iconLabel.setForeground(accentColor);
            leftPanel.add(iconLabel, BorderLayout.CENTER);

            // Panel droit avec titre et valeur
            JPanel rightPanel = new JPanel();
            rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
            rightPanel.setOpaque(false);

            titleLabel = new JLabel(title);
            titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            titleLabel.setForeground(ColorPalette.TEXT_SECONDARY);

            valueLabel = new JLabel(value);
            valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
            valueLabel.setForeground(ColorPalette.TEXT_PRIMARY);

            rightPanel.add(titleLabel);
            rightPanel.add(Box.createRigidArea(new Dimension(0, 5)));
            rightPanel.add(valueLabel);

            add(leftPanel, BorderLayout.WEST);
            add(rightPanel, BorderLayout.CENTER);

            // Effet hover
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    setBackground(ColorPalette.HOVER);
                    setCursor(new Cursor(Cursor.HAND_CURSOR));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    setBackground(Color.WHITE);
                    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                }
            });
        }

        public void setValue(String value) {
            valueLabel.setText(value);
        }
    }

    // =========================================================================
    // TABLE MODERNE
    // =========================================================================
    /**
     * Table moderne avec style personnalisé
     */
    public static class ModernTable extends JTable {

        public ModernTable(DefaultTableModel model) {
            super(model);
            setupTable();
        }

        private void setupTable() {
            // Style de la table
            setFont(new Font("Segoe UI", Font.PLAIN, 13));
            setRowHeight(40);
            setShowGrid(false);
            setIntercellSpacing(new Dimension(0, 0));
            setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

            // Header personnalisé
            JTableHeader header = getTableHeader();
            header.setFont(new Font("Segoe UI", Font.BOLD, 13));
            header.setBackground(ColorPalette.PRIMARY);
            header.setForeground(Color.WHITE);
            header.setPreferredSize(new Dimension(0, 40));

            // Renderer personnalisé pour les cellules
            setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value,
                                                               boolean isSelected, boolean hasFocus,
                                                               int row, int column) {

                    Component c = super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column
                    );

                    if (isSelected) {
                        c.setBackground(ColorPalette.PRIMARY);
                        c.setForeground(Color.WHITE);
                    } else {
                        c.setBackground(row % 2 == 0 ? Color.WHITE : ColorPalette.LIGHT);
                        c.setForeground(ColorPalette.TEXT_PRIMARY);
                    }

                    setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
                    return c;
                }
            });
        }
    }

    // =========================================================================
    // BORDURE ARRONDIE
    // =========================================================================
    /**
     * Bordure arrondie personnalisée
     */
    public static class RoundedBorder extends AbstractBorder {
        private Color color;
        private int thickness;
        private int radius;

        public RoundedBorder(Color color, int thickness, int radius) {
            this.color = color;
            this.thickness = thickness;
            this.radius = radius;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(thickness));
            g2.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(thickness, thickness, thickness, thickness);
        }

        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            insets.left = insets.top = insets.right = insets.bottom = thickness;
            return insets;
        }
    }

    // =========================================================================
    // UTILITAIRE COULEUR PLUS SOMBRE
    // =========================================================================
    /**
     * Utilitaire pour créer des couleurs plus sombres
     */
    private static Color darkerColor(Color color, float factor) {
        return new Color(
            Math.max((int) (color.getRed() * factor),   0),
            Math.max((int) (color.getGreen() * factor), 0),
            Math.max((int) (color.getBlue() * factor),  0),
            color.getAlpha()
        );
    }

    // =========================================================================
    // TOAST MODERNE
    // =========================================================================
    /**
     * Notification toast moderne
     */
    public static void showToast(Component parent, String message, ToastType type) {
        JWindow toast = new JWindow();
        toast.setAlwaysOnTop(true);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new CompoundBorder(
            new RoundedBorder(type.color, 2, 8),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        panel.setBackground(Color.WHITE);

        JLabel iconLabel = new JLabel(type.icon);
        iconLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        iconLabel.setForeground(type.color);

        JLabel messageLabel = new JLabel(message);
        messageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        messageLabel.setForeground(ColorPalette.TEXT_PRIMARY);

        panel.add(iconLabel, BorderLayout.WEST);
        panel.add(messageLabel, BorderLayout.CENTER);

        toast.add(panel);
        toast.pack();

        // Position au centre-bas de l'écran
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        toast.setLocation(
            (screenSize.width - toast.getWidth()) / 2,
            screenSize.height - toast.getHeight() - 100
        );

        toast.setVisible(true);

        // Fermeture après 3 secondes
        Timer timer = new Timer(3000, e -> toast.dispose());
        timer.setRepeats(false);
        timer.start();
    }

    public enum ToastType {
        SUCCESS("✓", ColorPalette.SUCCESS),
        ERROR("✗", ColorPalette.DANGER),
        WARNING("⚠", ColorPalette.WARNING),
        INFO("ⓘ", ColorPalette.INFO);

        final String icon;
        final Color color;

        ToastType(String icon, Color color) {
            this.icon = icon;
            this.color = color;
        }
    }
}
