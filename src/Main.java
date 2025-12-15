
// À adapter selon votre structure

import view.LoginView;
import controller.LoginController;
import model.dao.DataManager;
import model.dao.NotificationManager;
import model.dao.CacheManager;
import model.validation.ValidationManager;
import view.components.ModernUIComponents;

import javax.swing.*;
import java.awt.*;

public class Main {
    
    // Configuration de l'application
    private static final String APP_NAME = "Système de Gestion USTHB";
    private static final String APP_VERSION = "2.0.0";
    
    public static void main(String[] args) {
        // Configuration du Look and Feel
        configureLookAndFeel();
        
        // Initialisation des managers
        initializeManagers();
        
        // Affichage du splash screen
        showSplashScreen();
        
        // Démarrage de l'application
        SwingUtilities.invokeLater(() -> {
            try {
                startApplication();
            } catch (Exception e) {
                handleStartupError(e);
            }
        });
    }
    
    /**
     * Configure le Look and Feel de l'application
     */
    private static void configureLookAndFeel() {
        try {
            // Utiliser le Look and Feel du système
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            
            // Personnalisation des éléments UI
            customizeUIDefaults();
            
            System.out.println("✓ Look and Feel configuré");
            
        } catch (Exception e) {
            System.err.println("✗ Erreur configuration Look and Feel: " + e.getMessage());
        }
    }
    
    /**
     * Personnalise les paramètres UI par défaut
     */
    private static void customizeUIDefaults() {
        // Polices
        Font defaultFont = new Font("Segoe UI", Font.PLAIN, 13);
        Font boldFont = new Font("Segoe UI", Font.BOLD, 13);
        
        UIManager.put("Button.font", boldFont);
        UIManager.put("Label.font", defaultFont);
        UIManager.put("TextField.font", defaultFont);
        UIManager.put("Table.font", defaultFont);
        UIManager.put("TableHeader.font", boldFont);
        
        // Couleurs
        UIManager.put("Button.background", ModernUIComponents.ColorPalette.PRIMARY);
        UIManager.put("Button.foreground", Color.WHITE);
        UIManager.put("Button.select", ModernUIComponents.ColorPalette.PRIMARY_DARK);
        
        // Autres paramètres
        UIManager.put("Table.gridColor", ModernUIComponents.ColorPalette.BORDER);
        UIManager.put("Table.selectionBackground", ModernUIComponents.ColorPalette.PRIMARY);
        UIManager.put("Table.selectionForeground", Color.WHITE);
    }
    
    /**
     * Initialise tous les managers
     */
    private static void initializeManagers() {
        System.out.println("=".repeat(60));
        System.out.println("  " + APP_NAME + " v" + APP_VERSION);
        System.out.println("=".repeat(60));
        System.out.println();
        
        try {
            // DataManager
            System.out.print("Initialisation du DataManager... ");
            DataManager dataManager = DataManager.getInstance();
            System.out.println("✓");
            
            // CacheManager
            System.out.print("Initialisation du CacheManager... ");
            CacheManager cacheManager = CacheManager.getInstance();
            System.out.println("✓");
            
            // ValidationManager
            System.out.print("Initialisation du ValidationManager... ");
            ValidationManager validationManager = ValidationManager.getInstance();
            System.out.println("✓");
            System.out.print("Initialisation du NotificationManager... ");
            NotificationManager notificationManager = NotificationManager.getInstance();
            System.out.println("✓");
            
            // Affichage des statistiques
            printStartupStats(dataManager);
            
        } catch (Exception e) {
            System.err.println("✗ ERREUR");
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    /**
     * Affiche les statistiques au démarrage
     */
    private static void printStartupStats(DataManager dataManager) {
        System.out.println();
        System.out.println("Données chargées:");
        System.out.println("  • Utilisateurs: " + dataManager.getTotalUsers());
        System.out.println("  • Modules: " + dataManager.getTotalModules());
        System.out.println("  • Notes: " + dataManager.getTotalGrades());
        System.out.println("  • Absences: " + dataManager.getTotalAbsences());
        System.out.println("  • Inscriptions: " + dataManager.getTotalInscriptions());
        System.out.println();
        System.out.println("Comptes de test:");
        System.out.println("  • Étudiant: 10000001 / password");
        System.out.println("  • Professeur: 20000001 / password");
        System.out.println("  • Vice-Doyen: 30000001 / password");
        System.out.println("=".repeat(60));
        
        System.out.println();
    }
    
    /**
     * Affiche un splash screen pendant le chargement
     */
    private static void showSplashScreen() {
        // Création du splash screen
        JWindow splash = new JWindow();
        splash.setSize(500, 300);
        splash.setLocationRelativeTo(null);
        
        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(Color.WHITE);
        content.setBorder(BorderFactory.createLineBorder(
            ModernUIComponents.ColorPalette.PRIMARY, 2));
        
        // Logo et titre
        JLabel titleLabel = new JLabel("🎓 " + APP_NAME, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(ModernUIComponents.ColorPalette.PRIMARY);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(40, 20, 20, 20));
        
        // Version
        JLabel versionLabel = new JLabel("Version " + APP_VERSION, SwingConstants.CENTER);
        versionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        versionLabel.setForeground(ModernUIComponents.ColorPalette.TEXT_SECONDARY);
        
        // Loading bar
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setStringPainted(true);
        progressBar.setString("Chargement en cours...");
        progressBar.setBorder(BorderFactory.createEmptyBorder(20, 40, 40, 40));
        
        content.add(titleLabel, BorderLayout.NORTH);
        content.add(versionLabel, BorderLayout.CENTER);
        content.add(progressBar, BorderLayout.SOUTH);
        
        splash.setContentPane(content);
        splash.setVisible(true);
        
        // Fermeture après 2 secondes
        Timer timer = new Timer(2000, e -> splash.dispose());
        timer.setRepeats(false);
        timer.start();
    }
    
    /**
     * Démarre l'application principale
     */
    private static void startApplication() {
        // Créer et afficher la fenêtre de connexion
        LoginView loginView = new LoginView();
        LoginController loginController = new LoginController(loginView);
        
        // Configurer la fermeture de l'application
        loginView.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        loginView.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                handleApplicationExit(loginView);
            }
        });
        
        // Centrer et afficher
        loginView.setLocationRelativeTo(null);
        loginView.setVisible(true);
        
        System.out.println("✓ Application démarrée avec succès");
    }
    
    /**
     * Gère la fermeture de l'application
     */
    private static void handleApplicationExit(JFrame frame) {
        int confirm = JOptionPane.showConfirmDialog(
            frame,
            "Êtes-vous sûr de vouloir quitter l'application?",
            "Confirmation de sortie",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            System.out.println();
            System.out.println("Fermeture de l'application...");
            
            // Sauvegarder les données
            System.out.print("  Sauvegarde des données... ");
            DataManager.getInstance().saveAllData();
            System.out.println("✓");
            
            // Afficher les stats du cache
            System.out.println("  Statistiques du cache:");
            CacheManager.CacheStats stats = CacheManager.getInstance().getStats();
            System.out.println("    " + stats);
            
            // Nettoyer
            System.out.print("  Nettoyage... ");
            DataManager.getInstance().cleanup();
            System.out.println("✓");
            
            System.out.println();
            System.out.println("Application fermée correctement.");
            System.exit(0);
        }
    }
    
    /**
     * Gère les erreurs au démarrage
     */
    private static void handleStartupError(Exception e) {
        System.err.println("✗ Erreur fatale au démarrage:");
        e.printStackTrace();
        
        JOptionPane.showMessageDialog(
            null,
            "Erreur fatale lors du démarrage de l'application:\n" + e.getMessage(),
            "Erreur",
            JOptionPane.ERROR_MESSAGE
        );
        
        System.exit(1);
    }
    
    /**
     * Point d'entrée pour les tests
     */
    public static void runTests() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("  EXÉCUTION DES TESTS");
        System.out.println("=".repeat(60) + "\n");
        
        // Test 1: Cache
        testCache();
        
        // Test 2: Validation
        testValidation();
        
        // Test 3: DataManager
        testDataManager();
        
        System.out.println("\n" + "=".repeat(60));
        System.out.println("  TESTS TERMINÉS");
        System.out.println("=".repeat(60) + "\n");
    }
    
    private static void testCache() {
        System.out.println("Test 1: CacheManager");
        System.out.println("-".repeat(40));
        
        CacheManager cache = CacheManager.getInstance();
        
        // Mettre en cache un objet
        cache.cacheUser("TEST001", "Test User", 60000);
        
        // Récupérer du cache
        String user = cache.getCachedUser("TEST001", String.class);
        
        if (user != null) {
            System.out.println("✓ Cache fonctionne correctement");
        } else {
            System.out.println("✗ Erreur cache");
        }
        
        // Stats
        System.out.println("  Stats: " + cache.getStats());
        System.out.println();
    }
    
    private static void testValidation() {
        System.out.println("Test 2: ValidationManager");
        System.out.println("-".repeat(40));
        
        ValidationManager validator = ValidationManager.getInstance();
        
        // Test email
        ValidationManager.ValidationResult result = validator.validatePassword("Test123!");
        
        if (result.isValid()) {
            System.out.println("✓ Validation fonctionne");
        } else {
            System.out.println("✗ Erreur validation");
            System.out.println("  Erreurs: " + result.getErrorMessage());
        }
        System.out.println();
    }
    
    private static void testDataManager() {
        System.out.println("Test 3: DataManager");
        System.out.println("-".repeat(40));
        
        DataManager dm = DataManager.getInstance();
        
        // Test récupération utilisateur
        model.entities.User user = dm.getUser("10000001");
        
        if (user != null) {
            System.out.println("✓ DataManager fonctionne");
            System.out.println("  Utilisateur trouvé: " + user.getFullName());
        } else {
            System.out.println("✗ Erreur DataManager");
        }
        System.out.println();
    }
}

// ============================================================================
// ÉTAPE 2: EXEMPLE D'UTILISATION DES COMPOSANTS MODERNES
// ============================================================================

/**
 * Classe d'exemple montrant comment utiliser ModernUIComponents
 */
class ComponentsDemo extends JFrame {
    
    public ComponentsDemo() {
        setTitle("Demo - Modern UI Components");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(ModernUIComponents.ColorPalette.LIGHT);
        
        // 1. Modern Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        buttonPanel.setOpaque(false);
        
        ModernUIComponents.ModernButton primaryBtn = 
            new ModernUIComponents.ModernButton("Primary");
        ModernUIComponents.ModernButton successBtn = 
            new ModernUIComponents.ModernButton("Success", 
                ModernUIComponents.ColorPalette.SUCCESS);
        ModernUIComponents.ModernButton dangerBtn = 
            new ModernUIComponents.ModernButton("Danger", 
                ModernUIComponents.ColorPalette.DANGER);
        
        buttonPanel.add(primaryBtn);
        buttonPanel.add(successBtn);
        buttonPanel.add(dangerBtn);
        
        // 2. Modern TextFields
        ModernUIComponents.ModernTextField nameField = 
            new ModernUIComponents.ModernTextField("Nom complet", "Entrez votre nom");
        ModernUIComponents.ModernTextField emailField = 
            new ModernUIComponents.ModernTextField("Email", "exemple@usthb.dz");
        
        // 3. Modern Cards
        JPanel cardsPanel = new JPanel(new GridLayout(1, 3, 15, 15));
        cardsPanel.setOpaque(false);
        
        ModernUIComponents.ModernCard card1 = new ModernUIComponents.ModernCard(
            "Étudiants", "1247", "👥", ModernUIComponents.ColorPalette.PRIMARY);
        ModernUIComponents.ModernCard card2 = new ModernUIComponents.ModernCard(
            "Professeurs", "89", "👨‍🏫", ModernUIComponents.ColorPalette.SUCCESS);
        ModernUIComponents.ModernCard card3 = new ModernUIComponents.ModernCard(
            "Modules", "156", "📚", ModernUIComponents.ColorPalette.INFO);
        
        cardsPanel.add(card1);
        cardsPanel.add(card2);
        cardsPanel.add(card3);
        
        // 4. Toast Demo Button
        ModernUIComponents.ModernButton toastBtn = 
            new ModernUIComponents.ModernButton("Afficher Toast");
        toastBtn.addActionListener(e -> {
            ModernUIComponents.showToast(this, 
                "Ceci est une notification toast!", 
                ModernUIComponents.ToastType.SUCCESS);
        });
        
        // Assemblage
        mainPanel.add(new JLabel("Boutons Modernes:"));
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(buttonPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        mainPanel.add(new JLabel("Champs de Texte Modernes:"));
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(nameField);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(emailField);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        mainPanel.add(new JLabel("Cards Modernes:"));
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(cardsPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        mainPanel.add(toastBtn);
        
        add(new JScrollPane(mainPanel));
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ComponentsDemo().setVisible(true);
        });
    }
}

// ============================================================================
// ÉTAPE 3: HELPER UTILITIES
// ============================================================================

/**
 * Classe utilitaire pour des opérations courantes
 */
class AppUtils {
    
    /**
     * Formate une date
     */
    public static String formatDate(java.time.LocalDate date) {
        java.time.format.DateTimeFormatter formatter = 
            java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return date.format(formatter);
    }
    
    /**
     * Valide un email
     */
    public static boolean isValidEmail(String email) {
        String regex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email != null && email.matches(regex);
    }
    
    /**
     * Génère un mot de passe aléatoire
     */
    public static String generatePassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%";
        StringBuilder password = new StringBuilder();
        java.util.Random random = new java.util.Random();
        
        for (int i = 0; i < length; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
        
        return password.toString();
    }
    
    /**
     * Export vers CSV
     */
    public static void exportToCSV(String[][] data, String[] headers, String filename) {
        try (java.io.PrintWriter writer = new java.io.PrintWriter(filename)) {
            // Headers
            writer.println(String.join(",", headers));
            
            // Data
            for (String[] row : data) {
                writer.println(String.join(",", row));
            }
            
            System.out.println("✓ Export CSV réussi: " + filename);
            
        } catch (Exception e) {
            System.err.println("✗ Erreur export CSV: " + e.getMessage());
        }
    }

}
