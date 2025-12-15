package view.components;

import javax.swing.*;
import javax.swing.Timer;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Gestionnaire de thèmes avec transition fluide entre clair et sombre
 * Support de thèmes personnalisés et animation de changement
 */
public class ThemeManager {
    
    private static ThemeManager instance;
    private Theme currentTheme;
    private List<Component> registeredComponents;
    private Timer transitionTimer;
    
    private ThemeManager() {
        this.currentTheme = Theme.LIGHT;
        this.registeredComponents = new ArrayList<>();
    }
    
    public static ThemeManager getInstance() {
        if (instance == null) {
            instance = new ThemeManager();
        }
        return instance;
    }
    
    // ============================================================================
    // DÉFINITION DES THÈMES
    // ============================================================================
    
    public static class Theme {
        // Couleurs principales
        public Color background;
        public Color surface;
        public Color primary;
        public Color primaryDark;
        public Color secondary;
        public Color textPrimary;
        public Color textSecondary;
        public Color border;
        public Color hover;
        public Color success;
        public Color error;
        public Color warning;
        public Color info;
        public Color sidebar;
        public Color sidebarText;
        
        public String name;
        
        public Theme(String name) {
            this.name = name;
        }
        
        // Thème Clair (Par défaut)
        public static final Theme LIGHT = new Theme("Light") {{
            background = new Color(248, 249, 250);
            surface = Color.WHITE;
            primary = new Color(70, 130, 240);
            primaryDark = new Color(50, 100, 200);
            secondary = new Color(108, 117, 125);
            textPrimary = new Color(33, 37, 41);
            textSecondary = new Color(108, 117, 125);
            border = new Color(220, 220, 220);
            hover = new Color(240, 245, 250);
            success = new Color(40, 167, 69);
            error = new Color(220, 53, 69);
            warning = new Color(255, 193, 7);
            info = new Color(23, 162, 184);
            sidebar = new Color(52, 58, 64);
            sidebarText = Color.WHITE;
        }};
        
        // Thème Sombre
        public static final Theme DARK = new Theme("Dark") {{
            background = new Color(18, 18, 18);
            surface = new Color(30, 30, 30);
            primary = new Color(100, 160, 255);
            primaryDark = new Color(80, 140, 235);
            secondary = new Color(158, 158, 158);
            textPrimary = new Color(255, 255, 255);
            textSecondary = new Color(189, 189, 189);
            border = new Color(60, 60, 60);
            hover = new Color(45, 45, 45);
            success = new Color(56, 193, 114);
            error = new Color(244, 67, 54);
            warning = new Color(255, 152, 0);
            info = new Color(3, 169, 244);
            sidebar = new Color(24, 24, 24);
            sidebarText = new Color(230, 230, 230);
        }};
        
        // Thème Océan
        public static final Theme OCEAN = new Theme("Ocean") {{
            background = new Color(230, 245, 255);
            surface = new Color(245, 252, 255);
            primary = new Color(0, 150, 215);
            primaryDark = new Color(0, 120, 180);
            secondary = new Color(100, 140, 180);
            textPrimary = new Color(20, 40, 60);
            textSecondary = new Color(80, 100, 120);
            border = new Color(180, 220, 240);
            hover = new Color(220, 240, 255);
            success = new Color(30, 180, 120);
            error = new Color(220, 60, 80);
            warning = new Color(255, 180, 50);
            info = new Color(50, 160, 220);
            sidebar = new Color(10, 80, 130);
            sidebarText = new Color(240, 250, 255);
        }};
        
        // Thème Forêt
        public static final Theme FOREST = new Theme("Forest") {{
            background = new Color(240, 245, 235);
            surface = new Color(250, 252, 248);
            primary = new Color(56, 142, 60);
            primaryDark = new Color(46, 125, 50);
            secondary = new Color(120, 144, 156);
            textPrimary = new Color(33, 37, 41);
            textSecondary = new Color(97, 97, 97);
            border = new Color(200, 220, 200);
            hover = new Color(232, 245, 233);
            success = new Color(76, 175, 80);
            error = new Color(229, 115, 115);
            warning = new Color(255, 183, 77);
            info = new Color(79, 195, 247);
            sidebar = new Color(27, 94, 32);
            sidebarText = new Color(245, 250, 245);
        }};
        
        // Thème Sunset
        public static final Theme SUNSET = new Theme("Sunset") {{
            background = new Color(255, 245, 240);
            surface = new Color(255, 250, 245);
            primary = new Color(255, 87, 34);
            primaryDark = new Color(230, 74, 25);
            secondary = new Color(255, 138, 101);
            textPrimary = new Color(50, 30, 20);
            textSecondary = new Color(120, 100, 90);
            border = new Color(255, 224, 178);
            hover = new Color(255, 235, 220);
            success = new Color(102, 187, 106);
            error = new Color(239, 83, 80);
            warning = new Color(255, 167, 38);
            info = new Color(66, 165, 245);
            sidebar = new Color(191, 54, 12);
            sidebarText = new Color(255, 248, 240);
        }};
    }
    
    // ============================================================================
    // GESTION DES THÈMES
    // ============================================================================
    
    public void registerComponent(Component component) {
        if (!registeredComponents.contains(component)) {
            registeredComponents.add(component);
        }
    }
    
    public void unregisterComponent(Component component) {
        registeredComponents.remove(component);
    }
    
    public Theme getCurrentTheme() {
        return currentTheme;
    }
    
    public void setTheme(Theme newTheme, boolean animate) {
        if (animate) {
            animateThemeTransition(currentTheme, newTheme);
        } else {
            currentTheme = newTheme;
            applyThemeToAll();
        }
    }
    
    public void toggleDarkMode() {
        Theme newTheme = (currentTheme == Theme.LIGHT) ? Theme.DARK : Theme.LIGHT;
        setTheme(newTheme, true);
    }
    
    private void animateThemeTransition(Theme fromTheme, Theme toTheme) {
        if (transitionTimer != null && transitionTimer.isRunning()) {
            transitionTimer.stop();
        }
        
        final int steps = 20;
        final int[] currentStep = {0};
        
        transitionTimer = new Timer(20, e -> {
            currentStep[0]++;
            float progress = (float) currentStep[0] / steps;
            
            // Interpoler les couleurs
            currentTheme = interpolateTheme(fromTheme, toTheme, progress);
            applyThemeToAll();
            
            if (currentStep[0] >= steps) {
                currentTheme = toTheme;
                applyThemeToAll();
                ((Timer) e.getSource()).stop();
            }
        });
        
        transitionTimer.start();
    }
    
    private Theme interpolateTheme(Theme from, Theme to, float progress) {
        Theme interpolated = new Theme("Interpolated");
        
        interpolated.background = interpolateColor(from.background, to.background, progress);
        interpolated.surface = interpolateColor(from.surface, to.surface, progress);
        interpolated.primary = interpolateColor(from.primary, to.primary, progress);
        interpolated.primaryDark = interpolateColor(from.primaryDark, to.primaryDark, progress);
        interpolated.secondary = interpolateColor(from.secondary, to.secondary, progress);
        interpolated.textPrimary = interpolateColor(from.textPrimary, to.textPrimary, progress);
        interpolated.textSecondary = interpolateColor(from.textSecondary, to.textSecondary, progress);
        interpolated.border = interpolateColor(from.border, to.border, progress);
        interpolated.hover = interpolateColor(from.hover, to.hover, progress);
        interpolated.success = interpolateColor(from.success, to.success, progress);
        interpolated.error = interpolateColor(from.error, to.error, progress);
        interpolated.warning = interpolateColor(from.warning, to.warning, progress);
        interpolated.info = interpolateColor(from.info, to.info, progress);
        interpolated.sidebar = interpolateColor(from.sidebar, to.sidebar, progress);
        interpolated.sidebarText = interpolateColor(from.sidebarText, to.sidebarText, progress);
        
        return interpolated;
    }
    
    private Color interpolateColor(Color from, Color to, float progress) {
        int r = (int) (from.getRed() + (to.getRed() - from.getRed()) * progress);
        int g = (int) (from.getGreen() + (to.getGreen() - from.getGreen()) * progress);
        int b = (int) (from.getBlue() + (to.getBlue() - from.getBlue()) * progress);
        int a = (int) (from.getAlpha() + (to.getAlpha() - from.getAlpha()) * progress);
        
        return new Color(
            Math.max(0, Math.min(255, r)),
            Math.max(0, Math.min(255, g)),
            Math.max(0, Math.min(255, b)),
            Math.max(0, Math.min(255, a))
        );
    }
    
    private void applyThemeToAll() {
        for (Component component : registeredComponents) {
            applyThemeToComponent(component);
        }
    }
    
    private void applyThemeToComponent(Component component) {
        if (component instanceof JPanel) {
            JPanel panel = (JPanel) component;
            
            // Déterminer le type de panel
            if (panel.getName() != null) {
                switch (panel.getName()) {
                    case "sidebar":
                        panel.setBackground(currentTheme.sidebar);
                        break;
                    case "surface":
                        panel.setBackground(currentTheme.surface);
                        break;
                    default:
                        panel.setBackground(currentTheme.background);
                }
            } else {
                panel.setBackground(currentTheme.background);
            }
        }
        
        if (component instanceof JLabel) {
            JLabel label = (JLabel) component;
            if (label.getName() != null && label.getName().equals("secondary")) {
                label.setForeground(currentTheme.textSecondary);
            } else {
                label.setForeground(currentTheme.textPrimary);
            }
        }
        
        if (component instanceof JButton) {
            JButton button = (JButton) component;
            if (button.getName() != null && button.getName().equals("primary")) {
                button.setBackground(currentTheme.primary);
            }
        }
        
        // Appliquer récursivement aux enfants
        if (component instanceof Container) {
            for (Component child : ((Container) component).getComponents()) {
                applyThemeToComponent(child);
            }
        }
        
        component.repaint();
    }
    
    // ============================================================================
    // WIDGET DE SÉLECTION DE THÈME
    // ============================================================================
    
    public static class ThemeSelector extends JPanel {
        private ThemeManager themeManager;
        private JToggleButton darkModeToggle;
        private JComboBox<String> themeComboBox;
        
        public ThemeSelector() {
            this.themeManager = ThemeManager.getInstance();
            
            setLayout(new FlowLayout(FlowLayout.LEFT, 10, 5));
            setOpaque(false);
            
            // Toggle Dark Mode
            darkModeToggle = new JToggleButton("🌙 Mode Sombre");
            darkModeToggle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            darkModeToggle.setFocusPainted(false);
            darkModeToggle.setCursor(new Cursor(Cursor.HAND_CURSOR));
            darkModeToggle.addActionListener(e -> {
                themeManager.toggleDarkMode();
                updateToggleText();
            });
            
            // Sélecteur de thème
            String[] themes = {"Light", "Dark", "Ocean", "Forest", "Sunset"};
            themeComboBox = new JComboBox<>(themes);
            themeComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            themeComboBox.addActionListener(e -> {
                String selected = (String) themeComboBox.getSelectedItem();
                applySelectedTheme(selected);
            });
            
            add(new JLabel("Thème:"));
            add(themeComboBox);
            add(darkModeToggle);
        }
        
        private void updateToggleText() {
            boolean isDark = themeManager.getCurrentTheme() == Theme.DARK;
            darkModeToggle.setText(isDark ? "☀️ Mode Clair" : "🌙 Mode Sombre");
            darkModeToggle.setSelected(isDark);
        }
        
        private void applySelectedTheme(String themeName) {
            Theme theme;
            switch (themeName) {
                case "Dark":
                    theme = Theme.DARK;
                    break;
                case "Ocean":
                    theme = Theme.OCEAN;
                    break;
                case "Forest":
                    theme = Theme.FOREST;
                    break;
                case "Sunset":
                    theme = Theme.SUNSET;
                    break;
                default:
                    theme = Theme.LIGHT;
            }
            
            themeManager.setTheme(theme, true);
            updateToggleText();
        }
    }
    
    // ============================================================================
    // UTILITAIRES
    // ============================================================================
    
    public static Color getThemedColor(String colorName) {
        Theme theme = getInstance().getCurrentTheme();
        
        switch (colorName.toLowerCase()) {
            case "background": return theme.background;
            case "surface": return theme.surface;
            case "primary": return theme.primary;
            case "primarydark": return theme.primaryDark;
            case "secondary": return theme.secondary;
            case "textprimary": return theme.textPrimary;
            case "textsecondary": return theme.textSecondary;
            case "border": return theme.border;
            case "hover": return theme.hover;
            case "success": return theme.success;
            case "error": return theme.error;
            case "warning": return theme.warning;
            case "info": return theme.info;
            case "sidebar": return theme.sidebar;
            case "sidebartext": return theme.sidebarText;
            default: return theme.textPrimary;
        }
    }
    
    /**
     * Applique le thème à un frame entier
     */
    public void applyThemeToFrame(JFrame frame) {
        registerComponent(frame.getContentPane());
        applyThemeToComponent(frame.getContentPane());
    }
    
    /**
     * Sauvegarde les préférences de thème
     */
    public void saveThemePreference() {
        java.util.prefs.Preferences prefs = java.util.prefs.Preferences.userRoot();
        prefs.put("theme", currentTheme.name);
    }
    
    /**
     * Charge les préférences de thème
     */
    public void loadThemePreference() {
        java.util.prefs.Preferences prefs = java.util.prefs.Preferences.userRoot();
        String themeName = prefs.get("theme", "Light");
        
        Theme theme;
        switch (themeName) {
            case "Dark": theme = Theme.DARK; break;
            case "Ocean": theme = Theme.OCEAN; break;
            case "Forest": theme = Theme.FOREST; break;
            case "Sunset": theme = Theme.SUNSET; break;
            default: theme = Theme.LIGHT;
        }
        
        setTheme(theme, false);
    }
}