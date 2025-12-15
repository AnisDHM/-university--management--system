package view.components;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.*;
import java.awt.*;

public class ModernTheme {

    // --- PALETTE DE COULEURS CYBERPUNK ---
    public static final Color BG_DARK       = new Color(18, 18, 24);    // Fond Profond
    public static final Color BG_PANEL      = new Color(30, 30, 40);    // Cartes
    public static final Color BG_SIDEBAR    = new Color(25, 25, 35);    // Sidebar
    public static final Color BG_INPUT      = new Color(40, 40, 50);    // Inputs
    
    public static final Color TEXT_WHITE    = new Color(255, 255, 255);
    public static final Color TEXT_MUTED    = new Color(160, 160, 175);
    public static final Color TEXT_DARK     = new Color(0, 0, 0);       // Pour le texte sur fond clair si besoin
    
    // Accents Néons
    public static final Color NEON_CYAN     = new Color(0, 243, 255);
    public static final Color NEON_PURPLE   = new Color(188, 19, 254);
    public static final Color NEON_GREEN    = new Color(0, 255, 157);
    public static final Color NEON_YELLOW   = new Color(255, 209, 102);
    public static final Color NEON_RED      = new Color(255, 59, 48);

    // Fontes
    public static final Font FONT_HEADER    = new Font("Segoe UI", Font.BOLD, 26);
    public static final Font FONT_TITLE     = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font FONT_REGULAR   = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_BOLD      = new Font("Segoe UI", Font.BOLD, 14);

    // --- COMPOSANTS ---

    // 1. Panel Arrondi Sombre
    public static class DarkPanel extends JPanel {
        public DarkPanel() {
            setOpaque(false);
            setBackground(BG_PANEL);
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // 2. Bouton Néon
    public static class NeonButton extends JButton {
        private Color color;
        public NeonButton(String text, Color color) {
            super(text);
            this.color = color;
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setForeground(Color.WHITE); // Texte blanc par défaut sur bouton sombre
            setFont(FONT_BOLD);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(10, 20, 10, 20));
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (getModel().isPressed()) {
                g2.setColor(color.darker());
            } else if (getModel().isRollover()) {
                g2.setColor(color);
                setForeground(Color.BLACK); // Texte noir au survol pour contraste
            } else {
                g2.setColor(BG_INPUT);
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(1, 1, getWidth()-3, getHeight()-3, 15, 15); // Bordure néon
                g2.setColor(color); // Juste la bordure ou fond léger ? Faisons fond plein pour l'impact
                g2.setColor(color.darker().darker()); 
                setForeground(color);
            }
            // Style solide pour l'impact visuel "Breathtaking"
            g2.setColor(getModel().isRollover() ? color : BG_INPUT);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
            
            // Bordure Néon
            g2.setColor(color);
            g2.setStroke(new BasicStroke(1));
            g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 15, 15);
            
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // 3. Champ Texte Sombre
    public static class DarkTextField extends JTextField {
        public DarkTextField() {
            setOpaque(false);
            setForeground(TEXT_WHITE);
            setCaretColor(NEON_CYAN);
            setFont(FONT_REGULAR);
            setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
            setBackground(BG_INPUT);
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // 4. Style des Tables (CRITIQUE pour l'affichage des données)
    public static void applyDarkTableStyle(JTable table) {
        table.setBackground(BG_PANEL);
        table.setForeground(TEXT_WHITE);
        table.setSelectionBackground(BG_INPUT.brighter());
        table.setSelectionForeground(NEON_CYAN);
        table.setGridColor(BG_DARK);
        table.setRowHeight(45);
        table.setFont(FONT_REGULAR);
        table.setShowVerticalLines(false);
        table.setBorder(null);

        JTableHeader header = table.getTableHeader();
        header.setBackground(BG_DARK);
        header.setForeground(TEXT_MUTED);
        header.setFont(FONT_BOLD);
        header.setBorder(null);
        header.setPreferredSize(new Dimension(header.getWidth(), 40));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        centerRenderer.setBackground(BG_PANEL);
        centerRenderer.setForeground(TEXT_WHITE);

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
        
        // ScrollPane
        if (table.getParent() instanceof JViewport) {
            JScrollPane scroll = (JScrollPane) table.getParent().getParent();
            scroll.getViewport().setBackground(BG_PANEL);
            scroll.setBorder(BorderFactory.createEmptyBorder());
            scroll.getVerticalScrollBar().setBackground(BG_DARK);
            scroll.getVerticalScrollBar().setUI(new BasicScrollBarUI() {
                @Override protected void configureScrollBarColors() { this.thumbColor = BG_INPUT; }
            });
        }
    }

    // 5. ComboBox Style
    public static void styleComboBox(JComboBox<?> box) {
        box.setBackground(BG_INPUT);
        box.setForeground(TEXT_WHITE);
        box.setUI(new BasicComboBoxUI() {
            @Override protected JButton createArrowButton() {
                JButton b = new JButton("▼");
                b.setContentAreaFilled(false);
                b.setBorder(null);
                b.setForeground(NEON_CYAN);
                return b;
            }
        });
    }

    // 6. Correction Popups
    public static void fixPopupColors() {
        UIManager.put("OptionPane.background", BG_PANEL);
        UIManager.put("Panel.background", BG_PANEL);
        UIManager.put("OptionPane.messageForeground", TEXT_WHITE);
        UIManager.put("Button.background", NEON_PURPLE);
        UIManager.put("Button.foreground", Color.BLACK);
        UIManager.put("Label.foreground", TEXT_WHITE);
        UIManager.put("TextField.background", BG_INPUT);
        UIManager.put("TextField.foreground", TEXT_WHITE);
        UIManager.put("ComboBox.background", BG_INPUT);
        UIManager.put("ComboBox.foreground", TEXT_WHITE);
        UIManager.put("ComboBox.selectionBackground", NEON_CYAN);
        UIManager.put("ComboBox.selectionForeground", Color.BLACK);
    }
}