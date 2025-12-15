package controller;

import model.dao.DataManager;
import model.entities.Absence;
import model.entities.Grade;
import model.entities.Module;
import model.entities.Student;
import view.EnhancedStudentView;
import view.LoginView;
import view.components.ModernUIComponents.ModernTable;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * EnhancedStudentController - Version complète & synchronisée avec EnhancedStudentView.
 *
 * - Dashboard dynamique (moyenne, crédits, absences, classement)
 * - Table "Mes Notes" réelle + filtre par semestre + export CSV
 * - Table "Mes Absences" réelle
 * - JDialogs de détails (notes & absences) conservés
 * - Profil + changement de mot de passe sécurisé
 */
public class EnhancedStudentController {

    private final EnhancedStudentView view;
    private final Student student;
    private final DataManager dataManager;

    // Caches
    private List<Grade>   cachedGrades;
    private List<Absence> cachedAbsences;

    public EnhancedStudentController(EnhancedStudentView view, Student student) {
        this.view = Objects.requireNonNull(view, "view ne doit pas être null");
        this.student = Objects.requireNonNull(student, "student ne doit pas être null");
        this.dataManager = DataManager.getInstance();

        initController();
        reloadCaches();
        updateStatistics();
        refreshGradesTable(cachedGrades);
        refreshAbsencesTable(cachedAbsences);
    }

    // ========================================================================
    // INITIALISATION & NAVIGATION
    // ========================================================================

    private void initController() {
        // Navigation
        if (view.getDashboardBtn() != null)
            view.getDashboardBtn().addActionListener(e -> safeRun(this::onDashboard, "Erreur dashboard"));

        if (view.getGradesBtn() != null)
            view.getGradesBtn().addActionListener(e -> safeRun(this::onGrades, "Erreur notes"));

        if (view.getAbsencesBtn() != null)
            view.getAbsencesBtn().addActionListener(e -> safeRun(this::onAbsences, "Erreur absences"));

        if (view.getProfileBtn() != null)
            view.getProfileBtn().addActionListener(e -> safeRun(this::onProfile, "Erreur profil"));

        if (view.getLogoutBtn() != null)
            view.getLogoutBtn().addActionListener(e -> logout());

        // Notes : export + filtre
        if (view.getExportBtn() != null)
            view.getExportBtn().addActionListener(e -> safeRun(this::exportGrades, "Erreur export notes"));

        if (view.getSemesterFilter() != null)
            view.getSemesterFilter().addActionListener(e -> safeRun(this::applySemesterFilter, "Erreur filtre notes"));

        // Profil : mot de passe
        if (view.getChangePasswordBtn() != null)
            view.getChangePasswordBtn().addActionListener(e -> safeRun(this::changePassword, "Erreur mot de passe"));
    }

    private void onDashboard() {
        reloadCaches();
        updateStatistics();
    }

    private void onGrades() {
        reloadCaches();
        refreshGradesTable(cachedGrades);
    }

    private void onAbsences() {
        reloadCaches();
        refreshAbsencesTable(cachedAbsences);
    }

    private void onProfile() {
        StringBuilder sb = new StringBuilder();
        sb.append("Nom complet : ").append(student.getFullName()).append("\n");
        sb.append("Matricule   : ").append(student.getCode()).append("\n");
        sb.append("Spécialité  : ").append(
                student.getSpeciality() != null ? student.getSpeciality() : "Non renseignée"
        ).append("\n");
        sb.append("Année       : L").append(student.getYear()).append("\n");
        sb.append("Email       : ").append(
                student.getEmail() != null ? student.getEmail() : "Non renseigné"
        );

        JOptionPane.showMessageDialog(view, sb.toString(), "Mon Profil", JOptionPane.INFORMATION_MESSAGE);
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(
                view,
                "Voulez-vous vraiment vous déconnecter ?",
                "Confirmation",
                JOptionPane.YES_NO_OPTION
        );
        if (confirm != JOptionPane.YES_OPTION) return;

        view.dispose();
        SwingUtilities.invokeLater(() -> {
            LoginView loginView = new LoginView();
            new LoginController(loginView);
            loginView.setVisible(true);
        });
    }

    // ========================================================================
    // CACHES & DASHBOARD
    // ========================================================================

    private void reloadCaches() {
        cachedGrades   = dataManager.getStudentGrades(student.getCode());
        cachedAbsences = dataManager.getStudentAbsences(student.getCode());
        if (cachedGrades   == null) cachedGrades   = List.of();
        if (cachedAbsences == null) cachedAbsences = List.of();
    }

    private void updateStatistics() {
        double average       = calculateAverage(cachedGrades);
        int validatedCredits = calculateValidatedCredits(cachedGrades);
        int absencesCount    = cachedAbsences.size();
        String rankText      = calculateRankText(average, absencesCount);

        view.updateAverageCard(String.format("%.2f", average));
        view.updateCreditsCard(validatedCredits + "/60"); // 60 = valeur cible
        view.updateAbsencesCard(String.valueOf(absencesCount));
        view.updateRankCard(rankText);
    }

    private double calculateAverage(List<Grade> grades) {
        if (grades == null || grades.isEmpty()) return 0.0;
        return grades.stream().mapToDouble(Grade::getValue).average().orElse(0.0);
    }

    private int calculateValidatedCredits(List<Grade> grades) {
        if (grades == null || grades.isEmpty()) return 0;

        Map<String, List<Grade>> byModule = grades.stream()
                .collect(Collectors.groupingBy(Grade::getModuleCode));

        int totalCredits = 0;
        for (Map.Entry<String, List<Grade>> e : byModule.entrySet()) {
            String moduleCode = e.getKey();
            List<Grade> moduleGrades = e.getValue();

            double moduleAvg = moduleGrades.stream()
                    .mapToDouble(Grade::getValue)
                    .average()
                    .orElse(0.0);

            if (moduleAvg >= 10.0) {
                Module m = dataManager.getModule(moduleCode);
                if (m != null) totalCredits += m.getCredits();
            }
        }
        return totalCredits;
    }

    private String calculateRankText(double average, int absencesCount) {
        if (average >= 16 && absencesCount <= 2) return "Top 5%";
        if (average >= 14 && absencesCount <= 5) return "Top 20%";
        if (average >= 10)                       return "Dans la moyenne";
        return "À surveiller";
    }

    // ========================================================================
    // TABLE "MES NOTES" (onglet)
    // ========================================================================

    private void refreshGradesTable(List<Grade> grades) {
        ModernTable table = view.getGradesTable();
        if (table == null) return;

        String[] columns = {"Module", "Code", "Note", "Coef", "Type", "Date", "Mention"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        for (Grade g : grades) {
            Module m = dataManager.getModule(g.getModuleCode());
            String moduleName = m != null ? m.getName() : g.getModuleCode();
            model.addRow(new Object[]{
                    moduleName,
                    g.getModuleCode(),
                    String.format("%.2f", g.getValue()),
                    g.getCoefficient(),
                    g.getType(),
                    g.getFormattedDate(),
                    g.getMention()
            });
        }

        table.setModel(model);
    }

    private void applySemesterFilter() {
        JComboBox<String> filter = view.getSemesterFilter();
        if (filter == null) return;

        String selected = (String) filter.getSelectedItem();
        if (selected == null || selected.equals("Tous les semestres")) {
            refreshGradesTable(cachedGrades);
            return;
        }

        int semester = selected.equals("Semestre 1") ? 1 : 2;

        List<Grade> filtered = cachedGrades.stream()
                .filter(g -> {
                    Module m = dataManager.getModule(g.getModuleCode());
                    return m != null && m.getSemester() == semester;
                })
                .collect(Collectors.toList());

        refreshGradesTable(filtered);
    }

    private void exportGrades() {
        if (cachedGrades.isEmpty()) {
            showInfo("Aucune note à exporter.");
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Exporter les notes");
        fileChooser.setSelectedFile(new File("mes_notes_" + student.getCode() + ".csv"));

        int result = fileChooser.showSaveDialog(view);
        if (result != JFileChooser.APPROVE_OPTION) return;

        File file = fileChooser.getSelectedFile();
        try (PrintWriter writer = new PrintWriter(file, "UTF-8")) {
            writer.println("Module;Code;Note;Coefficient;Type;Date;Mention");
            for (Grade grade : cachedGrades) {
                Module m = dataManager.getModule(grade.getModuleCode());
                String moduleName = m != null ? m.getName() : grade.getModuleCode();
                writer.printf(
                        "%s;%s;%.2f;%s;%s;%s;%s%n",
                        moduleName,
                        grade.getModuleCode(),
                        grade.getValue(),
                        grade.getCoefficient(),
                        grade.getType(),
                        grade.getFormattedDate(),
                        grade.getMention()
                );
            }
            showInfo("✓ Notes exportées avec succès.\nFichier : " + file.getAbsolutePath());
        } catch (Exception e) {
            showError("Erreur lors de l'écriture du fichier : " + e.getMessage());
        }
    }

    // ========================================================================
    // DIALOG DETAIL NOTES / ABSENCES (optionnel mais conservé)
    // ========================================================================

    /** Fonctionnalité existante conservée : affiche un popup détaillé des notes */
    private void loadGradesData() {
        List<Grade> grades = dataManager.getStudentGrades(student.getCode());
        showGradesDialog(grades, "Mes Notes");
    }

    private void showGradesDialog(List<Grade> grades, String title) {
        if (grades == null || grades.isEmpty()) {
            showInfo("Aucune note disponible pour le moment.");
            return;
        }

        JDialog dialog = new JDialog(view, title, true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(780, 520);
        dialog.setLocationRelativeTo(view);

        JLabel titleLabel = new JLabel("Notes de " + student.getFullName(), JLabel.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        dialog.add(titleLabel, BorderLayout.NORTH);

        String[] columns = {"Module", "Code", "Note", "Coefficient", "Type", "Date", "Mention"};
        Object[][] data = new Object[grades.size()][columns.length];

        double total = 0;
        for (int i = 0; i < grades.size(); i++) {
            Grade g = grades.get(i);
            Module m = dataManager.getModule(g.getModuleCode());
            String moduleName = m != null ? m.getName() : g.getModuleCode();

            data[i][0] = moduleName;
            data[i][1] = g.getModuleCode();
            data[i][2] = String.format("%.2f/20", g.getValue());
            data[i][3] = g.getCoefficient();
            data[i][4] = g.getType();
            data[i][5] = g.getFormattedDate();
            data[i][6] = g.getMention();

            total += g.getValue();
        }

        JTable table = new JTable(data, columns);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setRowHeight(30);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(30, 30, 60));
        table.getTableHeader().setForeground(Color.WHITE);

        JScrollPane sp = new JScrollPane(table);
        dialog.add(sp, BorderLayout.CENTER);

        JPanel summaryPanel = new JPanel(new GridLayout(2, 2, 20, 10));
        summaryPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        summaryPanel.setBackground(new Color(248, 249, 250));

        double avg = total / grades.size();
        JLabel nbNotesLabel = new JLabel("Nombre de notes :");
        nbNotesLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        JLabel nbNotesValue = new JLabel(String.valueOf(grades.size()));

        JLabel avgLabel = new JLabel("Moyenne générale :");
        avgLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        JLabel avgValue = new JLabel(String.format("%.2f/20", avg));
        avgValue.setFont(new Font("Segoe UI", Font.BOLD, 18));
        avgValue.setForeground(avg >= 10 ? new Color(40, 167, 69)
                                         : new Color(220, 53, 69));

        summaryPanel.add(nbNotesLabel);
        summaryPanel.add(nbNotesValue);
        summaryPanel.add(avgLabel);
        summaryPanel.add(avgValue);

        dialog.add(summaryPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    // ========================================================================
    // MES ABSENCES (onglet + dialog)
    // ========================================================================

    private void refreshAbsencesTable(List<Absence> absences) {
        ModernTable table = view.getAbsencesTable();
        if (table == null) return;

        String[] columns = {"Module", "Date", "Type", "Statut", "Motif"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        for (Absence a : absences) {
            Module m = dataManager.getModule(a.getModuleCode());
            String moduleName = m != null ? m.getName() : a.getModuleCode();
            model.addRow(new Object[]{
                    moduleName,
                    a.getFormattedDate(),
                    a.getSessionType(),
                    a.isJustified() ? "Justifiée" : "Non justifiée",
                    a.getReason() != null ? a.getReason() : "-"
            });
        }

        table.setModel(model);
    }

    private void showAbsencesDialog() {
        List<Absence> absences = dataManager.getStudentAbsences(student.getCode());
        if (absences == null || absences.isEmpty()) {
            showInfo("Aucune absence enregistrée pour le moment.");
            return;
        }

        JDialog dialog = new JDialog(view, "Mes Absences", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(700, 460);
        dialog.setLocationRelativeTo(view);

        JLabel titleLabel = new JLabel("Absences de " + student.getFullName(), JLabel.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        dialog.add(titleLabel, BorderLayout.NORTH);

        String[] columns = {"Module", "Code", "Date", "Type", "Justifiée", "Motif"};
        Object[][] data = new Object[absences.size()][columns.length];

        for (int i = 0; i < absences.size(); i++) {
            Absence a = absences.get(i);
            Module m = dataManager.getModule(a.getModuleCode());
            String moduleName = m != null ? m.getName() : a.getModuleCode();

            data[i][0] = moduleName;
            data[i][1] = a.getModuleCode();
            data[i][2] = a.getFormattedDate();
            data[i][3] = a.getSessionType();
            data[i][4] = a.isJustified() ? "Oui" : "Non";
            data[i][5] = a.getReason() != null ? a.getReason() : "-";
        }

        JTable table = new JTable(data, columns);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setRowHeight(30);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(30, 30, 60));
        table.getTableHeader().setForeground(Color.WHITE);

        JScrollPane sp = new JScrollPane(table);
        dialog.add(sp, BorderLayout.CENTER);

        dialog.setVisible(true);
    }

    // ========================================================================
    // MOT DE PASSE
    // ========================================================================

    private void changePassword() {
        JDialog dialog = new JDialog(view, "Changer le mot de passe", true);
        dialog.setLayout(new GridBagLayout());
        dialog.setSize(420, 280);
        dialog.setLocationRelativeTo(view);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        dialog.add(new JLabel("Ancien mot de passe :"), gbc);
        JPasswordField oldField = new JPasswordField(15);
        gbc.gridx = 1;
        dialog.add(oldField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        dialog.add(new JLabel("Nouveau mot de passe :"), gbc);
        JPasswordField newField = new JPasswordField(15);
        gbc.gridx = 1;
        dialog.add(newField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        dialog.add(new JLabel("Confirmer :"), gbc);
        JPasswordField confirmField = new JPasswordField(15);
        gbc.gridx = 1;
        dialog.add(confirmField, gbc);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        JButton cancelBtn = new JButton("✗ Annuler");
        JButton saveBtn   = new JButton("✓ Enregistrer");
        saveBtn.setBackground(new Color(40, 167, 69));
        saveBtn.setForeground(Color.WHITE);

        cancelBtn.addActionListener(e -> dialog.dispose());

        saveBtn.addActionListener(e -> {
            String oldPass = new String(oldField.getPassword());
            String newPass = new String(newField.getPassword());
            String confirm = new String(confirmField.getPassword());

            if (oldPass.isEmpty() || newPass.isEmpty() || confirm.isEmpty()) {
                showErrorDialog(dialog, "Veuillez remplir tous les champs.");
                return;
            }
            if (!newPass.equals(confirm)) {
                showErrorDialog(dialog, "Les mots de passe ne correspondent pas.");
                return;
            }
            if (newPass.length() < 6) {
                showErrorDialog(dialog, "Le mot de passe doit contenir au moins 6 caractères.");
                return;
            }
            if (!oldPass.equals(student.getPassword())) {
                showErrorDialog(dialog, "Ancien mot de passe incorrect.");
                return;
            }

            try {
                student.setPassword(newPass);
                dataManager.updateUser(student);
                JOptionPane.showMessageDialog(
                        dialog,
                        "✓ Mot de passe changé avec succès.",
                        "Succès",
                        JOptionPane.INFORMATION_MESSAGE
                );
                dialog.dispose();
            } catch (Exception ex) {
                showErrorDialog(dialog, "Erreur lors de la mise à jour : " + ex.getMessage());
            }
        });

        btnPanel.add(cancelBtn);
        btnPanel.add(saveBtn);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        dialog.add(btnPanel, gbc);

        dialog.setVisible(true);
    }

    // ========================================================================
    // UTILITAIRES
    // ========================================================================

    private void safeRun(Runnable action, String errorPrefix) {
        try {
            action.run();
        } catch (Exception ex) {
            ex.printStackTrace();
            showError(errorPrefix + " : " + ex.getMessage());
        }
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(view, msg, "Erreur", JOptionPane.ERROR_MESSAGE);
    }

    private void showInfo(String msg) {
        JOptionPane.showMessageDialog(view, msg, "Information", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showErrorDialog(Component parent, String msg) {
        JOptionPane.showMessageDialog(parent, msg, "Erreur", JOptionPane.ERROR_MESSAGE);
    }
}
