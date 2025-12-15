// LoginController.java - VERSION CORRIGÉE & COMPLÈTE (stats login dynamiques)
package controller;

import model.dao.DataManager;
import model.entities.Professor;
import model.entities.Student;
import model.entities.User;
import model.entities.ViceDean;
import strategy.AuthenticationStrategy;
import strategy.StudentAuthStrategy;
import strategy.ProfessorAuthStrategy;
import strategy.ViceDeanAuthStrategy;
import view.EnhancedStudentView;
import view.LoginView;
import view.EnhancedProfessorView;
import view.EnhancedViceDeanView;

import javax.swing.*;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Field;

/**
 * LoginController
 *  - Authentifie l'utilisateur via des stratégies (Student, Professor, ViceDean)
 *  - Ouvre la vue correspondante EnhancedStudentView / EnhancedProfessorView / EnhancedViceDeanView
 *  - Met à jour les stats du panneau gauche du LoginView avec les données réelles (DataManager)
 */
public class LoginController {

    private final LoginView   view;
    private final DataManager dataManager;

    public LoginController(LoginView view) {
        this.view = view;
        this.dataManager = DataManager.getInstance();
        initController();
        updateLoginStatsFromData();  // <<--- chiffres réels sur l'écran de login
    }

    // -------------------------------------------------------------------------
    // INIT
    // -------------------------------------------------------------------------
    private void initController() {
        try {
            Field loginButtonField = LoginView.class.getDeclaredField("loginButton");
            loginButtonField.setAccessible(true);
            JButton loginButton = (JButton) loginButtonField.get(view);
            loginButton.addActionListener(new LoginAction());
        } catch (Exception e) {
            System.err.println("Utilisation du fallback pour trouver le bouton de connexion");
            findAndBindLoginButton();
        }
    }

    private void findAndBindLoginButton() {
        Container rootContainer = view.getContentPane();
        for (Component comp : rootContainer.getComponents()) {
            if (comp instanceof Container container) {
                findButtonInContainer(container, "SE CONNECTER"); // texte exact du bouton
            }
        }
    }

    private void findButtonInContainer(Container container, String buttonText) {
        for (Component comp : container.getComponents()) {
            if (comp instanceof JButton button) {
                if (buttonText.equalsIgnoreCase(button.getText())) {
                    button.addActionListener(new LoginAction());
                    return;
                }
            } else if (comp instanceof Container nested) {
                findButtonInContainer(nested, buttonText);
            }
        }
    }

    // -------------------------------------------------------------------------
    // STATS LOGIN RÉELLES
    // -------------------------------------------------------------------------
    /** Met à jour les stats sous le logo (Étudiants / Enseignants / Modules) avec les données réelles. */
    private void updateLoginStatsFromData() {
        try {
            int totalStudents = dataManager.getAllStudents().size();
            int totalTeachers = dataManager.getAllProfessors().size();
            int totalModules  = dataManager.getAllModules().size();

            view.updateLoginStats(totalStudents, totalTeachers, totalModules);
        } catch (Exception e) {
            System.err.println("Erreur mise à jour des stats login : " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // ACTION LOGIN
    // -------------------------------------------------------------------------
    private class LoginAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                Field userField     = LoginView.class.getDeclaredField("user");
                Field passwordField = LoginView.class.getDeclaredField("password");

                userField.setAccessible(true);
                passwordField.setAccessible(true);

                JTextField     usernameField    = (JTextField)     userField.get(view);
                JPasswordField passwordFieldObj = (JPasswordField) passwordField.get(view);

                String username = usernameField.getText().trim();
                String password = new String(passwordFieldObj.getPassword()).trim();

                if (username.isEmpty() || password.isEmpty()) {
                    showErrorMessage("Veuillez remplir tous les champs");
                    return;
                }

                AuthenticationResult result = authenticateUser(username, password);

                if (result.isSuccess()) {
                    openUserDashboard(result.getUser());
                    view.dispose();
                } else {
                    showErrorMessage(result.getMessage());
                }

            } catch (Exception ex) {
                ex.printStackTrace();
                showErrorMessage("Erreur lors de la connexion: " + ex.getMessage());
            }
        }
    }

    // -------------------------------------------------------------------------
    // AUTHENTIFICATION AVEC STRATÉGIES
    // -------------------------------------------------------------------------
    private AuthenticationResult authenticateUser(String username, String password) {
        AuthenticationStrategy strategy = getAuthStrategy(username);

        if (strategy == null) {
            return new AuthenticationResult(false, "Format de code utilisateur invalide", null);
        }

        if (!strategy.validateCodeFormat(username)) {
            return new AuthenticationResult(false, "Format de code invalide pour ce type d'utilisateur", null);
        }

        if (strategy.authenticate(username, password)) {
            User user = strategy.getAuthenticatedUser(username);
            return new AuthenticationResult(true, "Authentification réussie", user);
        } else {
            return new AuthenticationResult(false, "Code utilisateur ou mot de passe incorrect", null);
        }
    }

    private AuthenticationStrategy getAuthStrategy(String code) {
        if (code.matches("^1\\d{7}$")) {
            return new StudentAuthStrategy();
        } else if (code.matches("^2\\d{7}$")) {
            return new ProfessorAuthStrategy();
        } else if (code.matches("^3\\d{7}$")) {
            return new ViceDeanAuthStrategy();
        }
        return null;
    }

    // -------------------------------------------------------------------------
    // OUVERTURE DES DASHBOARDS
    // -------------------------------------------------------------------------
    private void openUserDashboard(User user) {
        if (user == null) {
            showErrorMessage("Utilisateur nul après authentification.");
            return;
        }

        String role = user.getRole();
        switch (role) {
            case "Student"   -> openStudentDashboard(user);
            case "Professor" -> openProfessorDashboard(user);
            case "Vice Dean" -> openViceDeanDashboard(user);
            default          -> showErrorMessage("Rôle utilisateur non reconnu: " + role);
        }
    }

    private void openStudentDashboard(User user) {
        try {
            if (!(user instanceof Student student)) {
                showErrorMessage("Type utilisateur invalide pour le rôle Student.");
                return;
            }

            EnhancedStudentView studentView = new EnhancedStudentView(
                    student.getFullName(),
                    student.getCode()
            );
            new EnhancedStudentController(studentView, student);
            studentView.setVisible(true);

        } catch (Exception e) {
            e.printStackTrace();
            showErrorMessage("Erreur lors de l'ouverture du tableau de bord étudiant: " + e.getMessage());
        }
    }

    private void openProfessorDashboard(User user) {
        try {
            if (!(user instanceof Professor professor)) {
                showErrorMessage("Type utilisateur invalide pour le rôle Professor.");
                return;
            }

            EnhancedProfessorView professorView = new EnhancedProfessorView(
                    professor.getFullName(),
                    professor.getCode()
            );
            new EnhancedProfessorController(professorView, professor);
            professorView.setVisible(true);

        } catch (Exception e) {
            e.printStackTrace();
            showErrorMessage("Erreur lors de l'ouverture du tableau de bord professeur: " + e.getMessage());
        }
    }

    private void openViceDeanDashboard(User user) {
        try {
            if (!(user instanceof ViceDean viceDean)) {
                showErrorMessage("Type utilisateur invalide pour le rôle Vice Dean.");
                return;
            }

            EnhancedViceDeanView viceDeanView = new EnhancedViceDeanView(
                    viceDean.getFullName(),
                    viceDean.getCode()
            );
            new EnhancedViceDeanController(viceDeanView, viceDean);
            viceDeanView.setVisible(true);

        } catch (Exception e) {
            e.printStackTrace();
            showErrorMessage("Erreur lors de l'ouverture du tableau de bord vice-doyen: " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // UI UTILS
    // -------------------------------------------------------------------------
    private void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(view, message, "Erreur", JOptionPane.ERROR_MESSAGE);
    }

    // -------------------------------------------------------------------------
    // RÉSULTAT D'AUTHENTIFICATION
    // -------------------------------------------------------------------------
    private static class AuthenticationResult {
        private final boolean success;
        private final String  message;
        private final User    user;

        public AuthenticationResult(boolean success, String message, User user) {
            this.success = success;
            this.message = message;
            this.user    = user;
        }

        public boolean isSuccess() { return success; }
        public String  getMessage() { return message; }
        public User    getUser()    { return user; }
    }
}
