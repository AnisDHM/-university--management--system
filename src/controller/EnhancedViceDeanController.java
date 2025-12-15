// EnhancedViceDeanController.java - VERSION RIGOUREUSE & MODERNE (avec stats réalistes)
package controller;

import model.dao.DataManager;
import model.dao.NotificationManager;
import model.entities.*;
import model.entities.Module;
import view.EnhancedViceDeanView;
import view.LoginView;
import view.components.AnimatedComponents;
import view.components.ModernUIComponents.ModernTable;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Contrôleur pour EnhancedViceDeanView.
 * Gère :
 *  - Dashboard (statistiques réelles + cartes + stats rapides + tâches + actions récentes)
 *  - Affectation de modules
 *  - Étudiants
 *  - Enseignants
 *  - Comptes
 *  - Rapports (CSV réels)
 *  - Paramètres (mot de passe + email)
 *  - Notifications via NotificationManager
 */
public class EnhancedViceDeanController {

    // =========================================================================
    // CHAMPS
    // =========================================================================
    private final EnhancedViceDeanView view;
    private final ViceDean             viceDean;
    private final DataManager          dataManager;
    private final NotificationManager  notificationManager;

    private List<Student>   cachedStudents   = new ArrayList<>();
    private List<Professor> cachedProfessors = new ArrayList<>();
    private List<Module>    cachedModules    = new ArrayList<>();

    // =========================================================================
    // CONSTRUCTEUR
    // =========================================================================
    public EnhancedViceDeanController(EnhancedViceDeanView view, User user) {
        this.view = Objects.requireNonNull(view, "view ne doit pas être null");
        this.viceDean = (ViceDean) Objects.requireNonNull(user, "user ne doit pas être null");
        this.dataManager = DataManager.getInstance();
        this.notificationManager = dataManager.getNotificationManager();

        initController();
        loadInitialData();
    }

    // =========================================================================
    // INITIALISATION
    // =========================================================================
    private void initController() {
        bindNavigationButtons();
        bindAssignmentButtons();
        bindStudentButtons();
        bindTeacherButtons();
        bindAccountButtons();
        bindReportButtons();
    }

    private void loadInitialData() {
        reloadCaches();
        refreshDashboardStats();  // met à jour cartes + quick stats + tâches + actions récentes
        loadAvailableModules();
        loadStudentsTable();
        loadTeachersTable();
        loadAccountsTable();
    }

    private void reloadCaches() {
        try {
            cachedStudents   = Optional.ofNullable(dataManager.getAllStudents()).orElseGet(ArrayList::new);
            cachedProfessors = Optional.ofNullable(dataManager.getAllProfessors()).orElseGet(ArrayList::new);
            cachedModules    = Optional.ofNullable(dataManager.getAllModules()).orElseGet(ArrayList::new);
        } catch (Exception e) {
            cachedStudents   = new ArrayList<>();
            cachedProfessors = new ArrayList<>();
            cachedModules    = new ArrayList<>();
            System.err.println("Erreur rechargement des caches : " + e.getMessage());
        }
    }

    // =========================================================================
    // NAVIGATION
    // =========================================================================
    private void bindNavigationButtons() {
        if (view.getDashboardBtn() != null) view.getDashboardBtn().addActionListener(e -> showDashboard());
        if (view.getAssignBtn()    != null) view.getAssignBtn().addActionListener(e    -> showAssignments());
        if (view.getStudentsBtn()  != null) view.getStudentsBtn().addActionListener(e  -> showStudents());
        if (view.getTeachersBtn()  != null) view.getTeachersBtn().addActionListener(e  -> showTeachers());
        if (view.getAccountsBtn()  != null) view.getAccountsBtn().addActionListener(e  -> showAccounts());
        if (view.getReportsBtn()   != null) view.getReportsBtn().addActionListener(e   -> showReports());
        if (view.getSettingsBtn()  != null) view.getSettingsBtn().addActionListener(e  -> showSettings());
        if (view.getLogoutBtn()    != null) view.getLogoutBtn().addActionListener(e    -> logout());
    }

    private void showDashboard() {
        reloadCaches();
        refreshDashboardStats();
    }

    private void showAssignments() {
        reloadCaches();
        loadAvailableModules();
    }

    private void showStudents() {
        reloadCaches();
        loadStudentsTable();
    }

    private void showTeachers() {
        reloadCaches();
        loadTeachersTable();
    }

    private void showAccounts() {
        reloadCaches();
        loadAccountsTable();
    }

    private void showReports() {
        generateStatistics();
    }

    private void showSettings() {
        showSettingsDialog();
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(
                view,
                "Voulez-vous vraiment vous déconnecter ?",
                "Confirmation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        if (confirm != JOptionPane.YES_OPTION) return;

        view.dispose();
        SwingUtilities.invokeLater(() -> {
            try {
                LoginView loginView = new LoginView();
                new LoginController(loginView);
                loginView.setVisible(true);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(
                        null,
                        "Impossible de revenir à l'écran de connexion.",
                        "Erreur",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });
    }

    // =========================================================================
    // AFFECTATION DE MODULES
    // =========================================================================
    private void bindAssignmentButtons() {
        if (view.getSearchBtn()       != null) view.getSearchBtn().addActionListener(e       -> searchTeacherForAssignment());
        if (view.getAssignModuleBtn() != null) view.getAssignModuleBtn().addActionListener(e -> assignModule());
        if (view.getCancelBtn()       != null) view.getCancelBtn().addActionListener(e       -> cancelAssignment());
    }

    private void loadAvailableModules() {
        try {
            JComboBox<String> moduleCombo = view.getModuleCombo();
            if (moduleCombo == null) return;

            moduleCombo.removeAllItems();
            List<Module> allModules = dataManager.getAllModules();
            if (allModules.isEmpty()) {
                moduleCombo.addItem("Aucun module disponible");
                moduleCombo.setEnabled(false);
                return;
            }

            for (Module m : allModules) {
                String label = m.getCode() + " - " + m.getName();
                if (m.getProfessorCode() != null && !m.getProfessorCode().isEmpty()) {
                    User profUser = dataManager.getUser(m.getProfessorCode());
                    String profName =
                            (profUser instanceof Professor)
                                    ? ((Professor) profUser).getFullName()
                                    : m.getProfessorCode();
                    label += "  [affecté à " + profName + "]";
                } else {
                    label += "  [non affecté]";
                }
                moduleCombo.addItem(label);
            }
            moduleCombo.setEnabled(true);
        } catch (Exception e) {
            showError("Erreur lors du chargement des modules : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void searchTeacherForAssignment() {
        JTextField tf = view.getTeacherField();
        if (tf == null) return;

        String searchTerm = tf.getText().trim();
        if (searchTerm.isEmpty()) {
            showInfo("Veuillez entrer un nom, prénom ou code d'enseignant.");
            return;
        }

        try {
            List<Professor> profs = dataManager.searchTeachers(searchTerm);
            if (profs == null || profs.isEmpty()) {
                showInfo("Aucun enseignant trouvé pour : " + searchTerm);
                return;
            }
            showTeacherSelectionDialog(profs);
        } catch (Exception e) {
            showError("Erreur lors de la recherche d'enseignant : " + e.getMessage());
        }
    }

    private void showTeacherSelectionDialog(List<Professor> professors) {
        String[] options = professors.stream()
                .map(p -> String.format("%s - %s (%s)",
                        p.getCode(),
                        p.getFullName(),
                        p.getDepartment() != null ? p.getDepartment() : ""))
                .toArray(String[]::new);

        String selected = (String) JOptionPane.showInputDialog(
                view,
                "Sélectionnez un enseignant :",
                "Résultats de recherche",
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
        );
        if (selected == null) return;

        String code = selected.split(" - ")[0];
        view.getTeacherField().setText(code);

        Professor p = professors.stream()
                .filter(pr -> pr.getCode().equals(code))
                .findFirst()
                .orElse(null);

        if (p != null) {
            int moduleCount = dataManager.getProfessorModules(p.getCode()).size();
            showInfo(String.format(
                    "✓ Enseignant sélectionné :\n\n" +
                            "Code : %s\n" +
                            "Nom : %s\n" +
                            "Département : %s\n" +
                            "Grade : %s\n" +
                            "Modules actuels : %d",
                    p.getCode(),
                    p.getFullName(),
                    p.getDepartment(),
                    p.getAcademicRank(),
                    moduleCount
            ));
        }
    }

    private void assignModule() {
        try {
            JComboBox<String> moduleCombo = view.getModuleCombo();
            JTextField teacherField = view.getTeacherField();
            if (moduleCombo == null || teacherField == null) {
                showError("Éléments d'affectation indisponibles.");
                return;
            }

            String selected = (String) moduleCombo.getSelectedItem();
            String teacherCode = teacherField.getText().trim();

            if (selected == null || selected.isEmpty() || selected.startsWith("Aucun module")) {
                showError("Veuillez sélectionner un module.");
                return;
            }
            if (teacherCode.isEmpty()) {
                showError("Veuillez saisir le code d'un enseignant.");
                return;
            }

            String moduleCode = selected.split(" - ")[0].trim();
            System.out.println("[VD] Affectation demandée : module=" + moduleCode + " -> prof=" + teacherCode);

            User u = dataManager.getUser(teacherCode);
            if (!(u instanceof Professor professor)) {
                showError("Enseignant introuvable : " + teacherCode);
                return;
            }

            Module module = dataManager.getModule(moduleCode);
            if (module == null) {
                showError("Module introuvable : " + moduleCode);
                return;
            }

            String oldProf = module.getProfessorCode();
            if (oldProf != null && !oldProf.isEmpty() && !oldProf.equals(teacherCode)) {
                int confirm = JOptionPane.showConfirmDialog(
                        view,
                        "Ce module est déjà affecté à : " + oldProf + ".\n" +
                                "Voulez-vous le réaffecter ?",
                        "Confirmation",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                );
                if (confirm != JOptionPane.YES_OPTION) return;
            }

            int currentModules = dataManager.getProfessorModules(teacherCode).size();
            if (currentModules >= 5) {
                int confirm = JOptionPane.showConfirmDialog(
                        view,
                        "Cet enseignant a déjà " + currentModules + " modules.\n" +
                                "Confirmer l'ajout d'un module supplémentaire ?",
                        "Charge horaire élevée",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                );
                if (confirm != JOptionPane.YES_OPTION) return;
            }

            module.setProfessorCode(teacherCode);
            System.out.println("[VD] Appel DataManager.updateModule(" + module.getCode() +
                    "), oldProf=" + oldProf + ", newProf=" + teacherCode);
            boolean ok = dataManager.updateModule(module);
            if (!ok) {
                showError("Impossible de mettre à jour le module (voir logs).");
                return;
            }

            showSuccess(String.format(
                    "✅ Module affecté avec succès !\n\n" +
                            "Module : %s - %s\n" +
                            "Enseignant : %s (%s)\n\n" +
                            "📬 Une notification lui a été envoyée automatiquement.",
                    module.getCode(), module.getName(),
                    professor.getFullName(), professor.getCode()
            ));

            teacherField.setText("");
            reloadCaches();
            loadAvailableModules();
            loadTeachersTable();
            refreshDashboardStats();

        } catch (Exception e) {
            showError("Erreur lors de l'affectation : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void cancelAssignment() {
        JTextField teacherField = view.getTeacherField();
        if (teacherField != null) {
            teacherField.setText("");
        }
        showInfo("Formulaire d'affectation réinitialisé.");
    }

    // =========================================================================
    // ÉTUDIANTS (MOYENNES RÉELLES)
    // =========================================================================
    private void bindStudentButtons() {
        if (view.getAddStudentBtn()    != null) view.getAddStudentBtn().addActionListener(e    -> addStudent());
        if (view.getEditStudentBtn()   != null) view.getEditStudentBtn().addActionListener(e   -> editStudent());
        if (view.getDeleteStudentBtn() != null) view.getDeleteStudentBtn().addActionListener(e -> deleteStudent());
        if (view.getSearchStudentField() != null)
            view.getSearchStudentField().addActionListener(e -> searchStudents());
        if (view.getLevelFilter() != null)  view.getLevelFilter().addActionListener(e  -> filterStudents());
        if (view.getStatusFilter() != null) view.getStatusFilter().addActionListener(e -> filterStudents());
    }

    private void addStudent() {
        JDialog dialog = createStudentDialog(null);
        dialog.setVisible(true);
    }

    private void editStudent() {
        ModernTable table = view.getStudentsTable();
        if (table == null || table.getSelectedRow() == -1) {
            showInfo("Veuillez sélectionner un étudiant.");
            return;
        }

        int row = table.getSelectedRow();
        String code = String.valueOf(table.getValueAt(row, 0));
        User u = dataManager.getUser(code);
        if (!(u instanceof Student student)) {
            showError("Étudiant introuvable : " + code);
            return;
        }

        JDialog dialog = createStudentDialog(student);
        dialog.setVisible(true);
    }

    private JDialog createStudentDialog(Student existingStudent) {
        boolean isEdit = existingStudent != null;

        JDialog dialog = new JDialog(view, isEdit ? "Modifier Étudiant" : "Ajouter Étudiant", true);
        dialog.setLayout(new GridBagLayout());
        dialog.setSize(600, 450);
        dialog.setLocationRelativeTo(view);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField codeField   = new JTextField(20);
        JTextField lastField   = new JTextField(20);
        JTextField firstField  = new JTextField(20);
        JTextField emailField  = new JTextField(20);
        JTextField phoneField  = new JTextField(20);
        JTextField specField   = new JTextField(20);
        SpinnerNumberModel yearModel = new SpinnerNumberModel(1, 1, 5, 1);
        JSpinner yearSpinner = new JSpinner(yearModel);

        if (isEdit) {
            codeField.setText(existingStudent.getCode());
            codeField.setEditable(false);
            lastField.setText(existingStudent.getLastName());
            firstField.setText(existingStudent.getFirstName());
            if (existingStudent.getEmail() != null)       emailField.setText(existingStudent.getEmail());
            if (existingStudent.getPhoneNumber() != null) phoneField.setText(existingStudent.getPhoneNumber());
            if (existingStudent.getSpeciality() != null)  specField.setText(existingStudent.getSpeciality());
            yearSpinner.setValue(existingStudent.getYear());
        }

        int row = 0;
        addDialogField(dialog, gbc, row++, "Matricule* :",   codeField);
        addDialogField(dialog, gbc, row++, "Nom* :",         lastField);
        addDialogField(dialog, gbc, row++, "Prénom* :",      firstField);
        addDialogField(dialog, gbc, row++, "Année (1..5)* :", yearSpinner);
        addDialogField(dialog, gbc, row++, "Email :",        emailField);
        addDialogField(dialog, gbc, row++, "Téléphone :",    phoneField);
        addDialogField(dialog, gbc, row++, "Spécialité :",   specField);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        JButton cancelBtn = new JButton("✗ Annuler");
        JButton saveBtn   = new JButton(isEdit ? "✓ Modifier" : "✓ Ajouter");

        cancelBtn.addActionListener(e -> dialog.dispose());
        saveBtn.addActionListener(e -> {
            String code   = codeField.getText().trim();
            String last   = lastField.getText().trim();
            String first  = firstField.getText().trim();
            int year      = (int) yearSpinner.getValue();
            String email  = emailField.getText().trim();
            String phone  = phoneField.getText().trim();
            String spec   = specField.getText().trim();

            if (code.isEmpty() || last.isEmpty() || first.isEmpty()) {
                showError("Remplissez au minimum matricule, nom et prénom.");
                return;
            }
            if (!code.matches("^1\\d{7}$")) {
                showError("Matricule invalide (doit commencer par 1 et contenir 8 chiffres).");
                return;
            }
            if (!email.isEmpty() && !email.matches("^[^@]+@[^@]+\\.[^@]+$")) {
                showError("Adresse e-mail invalide.");
                return;
            }
            if (!phone.isEmpty() && !phone.matches("^0\\d{9}$")) {
                showError("Téléphone invalide (format 0XXXXXXXXX).");
                return;
            }

            try {
                if (!isEdit) {
                    if (dataManager.getUser(code) != null) {
                        showError("Un utilisateur avec ce matricule existe déjà.");
                        return;
                    }
                    Student s = new Student(code, "password123", first, last);
                    s.setYear(year);
                    if (!email.isEmpty())  s.setEmail(email);
                    if (!phone.isEmpty())  s.setPhoneNumber(phone);
                    if (!spec.isEmpty())   s.setSpeciality(spec);

                    if (!dataManager.addUser(s)) {
                        showError("Impossible d'ajouter l'étudiant (voir logs).");
                        return;
                    }
                    showSuccess("✅ Étudiant ajouté (compte créé avec mot de passe temporaire).");
                } else {
                    existingStudent.setLastName(last);
                    existingStudent.setFirstName(first);
                    existingStudent.setYear(year);
                    existingStudent.setEmail(email.isEmpty() ? null : email);
                    existingStudent.setPhoneNumber(phone.isEmpty() ? null : phone);
                    existingStudent.setSpeciality(spec.isEmpty() ? null : spec);

                    if (!dataManager.updateUser(existingStudent)) {
                        showError("Impossible de modifier l'étudiant (voir logs).");
                        return;
                    }
                    showSuccess("✅ Étudiant modifié avec succès.");
                }

                reloadCaches();
                loadStudentsTable();
                loadAccountsTable();
                refreshDashboardStats();
                dialog.dispose();

            } catch (Exception ex) {
                ex.printStackTrace();
                showError("Erreur lors de l'enregistrement de l'étudiant : " + ex.getMessage());
            }
        });

        btnPanel.add(cancelBtn);
        btnPanel.add(saveBtn);
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        dialog.add(btnPanel, gbc);

        return dialog;
    }

    private void deleteStudent() {
        ModernTable table = view.getStudentsTable();
        if (table == null || table.getSelectedRow() == -1) {
            showInfo("Veuillez sélectionner un étudiant à supprimer.");
            return;
        }

        int row = table.getSelectedRow();
        String code = String.valueOf(table.getValueAt(row, 0));

        int confirm = JOptionPane.showConfirmDialog(
                view,
                "Voulez-vous vraiment supprimer l'étudiant " + code + " ?\n" +
                        "Toutes ses notes, absences et inscriptions seront également supprimées.",
                "Confirmation de suppression",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            boolean ok = dataManager.deleteUser(code);
            if (ok) {
                showSuccess("✅ Étudiant supprimé avec succès.");
                reloadCaches();
                loadStudentsTable();
                loadAccountsTable();
                refreshDashboardStats();
            } else {
                showError("Impossible de supprimer l'étudiant (voir logs).");
            }
        } catch (Exception e) {
            showError("Erreur lors de la suppression de l'étudiant : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void searchStudents() {
        JTextField field = view.getSearchStudentField();
        String term = field != null ? field.getText().trim() : "";
        filterAndDisplayStudents(term);
    }

    private void filterStudents() {
        JTextField field = view.getSearchStudentField();
        String term = field != null ? field.getText().trim() : "";
        filterAndDisplayStudents(term);
    }

    private void loadStudentsTable() {
        updateStudentsTableWithGrades(cachedStudents, dataManager.getAllGrades());
    }

    private void filterAndDisplayStudents(String searchTerm) {
        try {
            List<Student> students = new ArrayList<>(cachedStudents);
            List<Grade> allGrades  = dataManager.getAllGrades();

            String levelFilter  = (String) (view.getLevelFilter()  != null ? view.getLevelFilter().getSelectedItem()  : "Tous niveaux");
            String statusFilter = (String) (view.getStatusFilter() != null ? view.getStatusFilter().getSelectedItem() : "Tous statuts");

            if (levelFilter != null && !"Tous niveaux".equals(levelFilter)) {
                int year;
                if (levelFilter.startsWith("L"))      year = Character.getNumericValue(levelFilter.charAt(1));
                else if (levelFilter.startsWith("M")) year = Character.getNumericValue(levelFilter.charAt(1)) + 3;
                else year = -1;

                if (year != -1) {
                    students = students.stream()
                            .filter(s -> s.getYear() == year)
                            .collect(Collectors.toList());
                }
            }

            if (statusFilter != null && !"Tous statuts".equals(statusFilter)) {
                students = students.stream()
                        .filter(s -> {
                            double avg = calculateRealStudentAverage(s.getCode(), allGrades);
                            return switch (statusFilter) {
                                case "Admis"    -> avg >= 10.0;
                                case "Redouble" -> avg >= 7.0 && avg < 10.0;
                                case "Exclus"   -> avg < 7.0;
                                default -> true;
                            };
                        })
                        .collect(Collectors.toList());
            }

            if (searchTerm != null && !searchTerm.isEmpty()) {
                String lower = searchTerm.toLowerCase();
                students = students.stream()
                        .filter(s ->
                                s.getCode().toLowerCase().contains(lower) ||
                                        s.getFullName().toLowerCase().contains(lower) ||
                                        (s.getEmail() != null && s.getEmail().toLowerCase().contains(lower))
                        )
                        .collect(Collectors.toList());
            }

            updateStudentsTableWithGrades(students, allGrades);
        } catch (Exception e) {
            showError("Erreur lors du filtrage des étudiants : " + e.getMessage());
        }
    }

    private void updateStudentsTableWithGrades(List<Student> students, List<Grade> allGrades) {
        ModernTable table = view.getStudentsTable();
        if (table == null) return;

        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0);

        for (Student s : students) {
            double avg = calculateRealStudentAverage(s.getCode(), allGrades);
            String status = getStudentStatus(avg);

            String levelLabel;
            if (s.getYear() <= 3) {
                levelLabel = "L" + s.getYear();
            } else {
                levelLabel = "M" + (s.getYear() - 3);
            }

            model.addRow(new Object[]{
                    s.getCode(),
                    s.getLastName(),
                    s.getFirstName(),
                    levelLabel,
                    String.format("%.2f", avg),
                    status,
                    "Voir"
            });
        }
    }

    // =========================================================================
    // ENSEIGNANTS
    // =========================================================================
    private void bindTeacherButtons() {
        if (view.getAddTeacherBtn()    != null) view.getAddTeacherBtn().addActionListener(e    -> addTeacher());
        if (view.getEditTeacherBtn()   != null) view.getEditTeacherBtn().addActionListener(e   -> editTeacher());
        if (view.getDeleteTeacherBtn() != null) view.getDeleteTeacherBtn().addActionListener(e -> deleteTeacher());
        if (view.getSearchTeacherField() != null)
            view.getSearchTeacherField().addActionListener(e -> searchTeachers());
    }

    private void addTeacher() {
        JDialog dialog = createTeacherDialog(null);
        dialog.setVisible(true);
    }

    private void editTeacher() {
        ModernTable table = view.getTeachersTable();
        if (table == null || table.getSelectedRow() == -1) {
            showInfo("Veuillez sélectionner un enseignant.");
            return;
        }

        int row = table.getSelectedRow();
        String code = String.valueOf(table.getValueAt(row, 0));

        User u = dataManager.getUser(code);
        if (!(u instanceof Professor professor)) {
            showError("Enseignant introuvable : " + code);
            return;
        }

        JDialog dialog = createTeacherDialog(professor);
        dialog.setVisible(true);
    }

    private void deleteTeacher() {
        ModernTable table = view.getTeachersTable();
        if (table == null || table.getSelectedRow() == -1) {
            showInfo("Veuillez sélectionner un enseignant à supprimer.");
            return;
        }

        int row = table.getSelectedRow();
        String code = String.valueOf(table.getValueAt(row, 0));

        int moduleCount = dataManager.getProfessorModules(code).size();

        int confirm = JOptionPane.showConfirmDialog(
                view,
                "Voulez-vous vraiment supprimer l'enseignant " + code + " ?\n" +
                        "Les " + moduleCount + " modules qui lui sont affectés seront détachés (professeur = null).",
                "Confirmation de suppression",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            boolean ok = dataManager.deleteUser(code);
            if (ok) {
                showSuccess("✅ Enseignant supprimé avec succès.");
                reloadCaches();
                loadTeachersTable();
                loadAvailableModules();
                loadAccountsTable();
                refreshDashboardStats();
            } else {
                showError("Impossible de supprimer l'enseignant (voir logs).");
            }
        } catch (Exception e) {
            showError("Erreur lors de la suppression de l'enseignant : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void searchTeachers() {
        JTextField field = view.getSearchTeacherField();
        String term = field != null ? field.getText().trim() : "";

        try {
            List<Professor> list = new ArrayList<>(cachedProfessors);
            if (!term.isEmpty()) {
                String s = term.toLowerCase();
                list = list.stream()
                        .filter(p ->
                                p.getCode().toLowerCase().contains(s) ||
                                        p.getFullName().toLowerCase().contains(s) ||
                                        (p.getDepartment() != null && p.getDepartment().toLowerCase().contains(s))
                        )
                        .collect(Collectors.toList());
            }
            updateTeachersTable(list);
        } catch (Exception e) {
            showError("Erreur lors de la recherche d'enseignant : " + e.getMessage());
        }
    }

    private void loadTeachersTable() {
        updateTeachersTable(cachedProfessors);
    }

    private void updateTeachersTable(List<Professor> professors) {
        ModernTable table = view.getTeachersTable();
        if (table == null) return;

        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0);

        for (Professor p : professors) {
            int moduleCount = dataManager.getProfessorModules(p.getCode()).size();
            String status   = moduleCount > 0 ? "Actif" : "Inactif";
            String hours    = String.format("%.1fh", moduleCount * 3.0); // heuristique

            model.addRow(new Object[]{
                    p.getCode(),
                    p.getLastName(),
                    p.getFirstName(),
                    moduleCount,
                    hours,
                    status,
                    "Voir"
            });
        }
    }

    private JDialog createTeacherDialog(Professor professor) {
        boolean isEdit = (professor != null);
        JDialog dialog = new JDialog(view, isEdit ? "Modifier Enseignant" : "Ajouter Enseignant", true);
        dialog.setLayout(new GridBagLayout());
        dialog.setSize(600, 500);
        dialog.setLocationRelativeTo(view);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField codeField       = new JTextField(20);
        JTextField lastNameField   = new JTextField(20);
        JTextField firstNameField  = new JTextField(20);
        JTextField emailField      = new JTextField(20);
        JTextField phoneField      = new JTextField(20);
        JTextField departmentField = new JTextField(20);
        JComboBox<String> rankCombo = new JComboBox<>(new String[]{
                "Professeur", "Maître de Conférences A", "Maître de Conférences B",
                "Maître Assistant A", "Maître Assistant B", "Assistant"
        });

        if (isEdit) {
            codeField.setText(professor.getCode());
            codeField.setEditable(false);
            lastNameField.setText(professor.getLastName());
            firstNameField.setText(professor.getFirstName());
            emailField.setText(professor.getEmail() != null ? professor.getEmail() : "");
            phoneField.setText(professor.getPhoneNumber() != null ? professor.getPhoneNumber() : "");
            departmentField.setText(professor.getDepartment());
            rankCombo.setSelectedItem(professor.getAcademicRank());
        }

        int row = 0;
        addDialogField(dialog, gbc, row++, "Code* :",        codeField);
        addDialogField(dialog, gbc, row++, "Nom* :",         lastNameField);
        addDialogField(dialog, gbc, row++, "Prénom* :",      firstNameField);
        addDialogField(dialog, gbc, row++, "Email :",        emailField);
        addDialogField(dialog, gbc, row++, "Téléphone :",    phoneField);
        addDialogField(dialog, gbc, row++, "Département* :", departmentField);
        addDialogField(dialog, gbc, row++, "Grade* :",       rankCombo);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        JButton saveBtn   = new JButton(isEdit ? "✓ Modifier" : "✓ Ajouter");
        JButton cancelBtn = new JButton("✗ Annuler");

        saveBtn.addActionListener(e -> {
            if (saveTeacher(professor,
                    codeField.getText().trim(),
                    lastNameField.getText().trim(),
                    firstNameField.getText().trim(),
                    emailField.getText().trim(),
                    phoneField.getText().trim(),
                    departmentField.getText().trim(),
                    (String) rankCombo.getSelectedItem())) {
                dialog.dispose();
                reloadCaches();
                loadTeachersTable();
                loadAccountsTable();
                refreshDashboardStats();
            }
        });
        cancelBtn.addActionListener(e -> dialog.dispose());

        btnPanel.add(cancelBtn);
        btnPanel.add(saveBtn);

        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        dialog.add(btnPanel, gbc);

        return dialog;
    }

    private boolean saveTeacher(Professor existingProf,
                                String code, String lastName, String firstName,
                                String email, String phone,
                                String department, String rank) {
        try {
            if (code.isEmpty() || lastName.isEmpty() || firstName.isEmpty()
                    || department.isEmpty() || rank == null) {
                showError("Veuillez remplir tous les champs obligatoires (*).");
                return false;
            }
            if (!code.matches("^2\\d{7}$")) {
                showError("Code enseignant invalide (doit commencer par 2 et contenir 8 chiffres).");
                return false;
            }
            if (!email.isEmpty() && !email.matches("^[^@]+@[^@]+\\.[^@]+$")) {
                showError("Adresse e-mail invalide.");
                return false;
            }
            if (!phone.isEmpty() && !phone.matches("^0\\d{9}$")) {
                showError("Téléphone invalide (format 0XXXXXXXXX).");
                return false;
            }

            if (existingProf == null) {
                if (dataManager.getUser(code) != null) {
                    showError("Un utilisateur avec ce code existe déjà.");
                    return false;
                }
                Professor p = new Professor(code, "password123", firstName, lastName);
                p.setEmail(email.isEmpty() ? null : email);
                p.setPhoneNumber(phone.isEmpty() ? null : phone);
                p.setDepartment(department);
                p.setAcademicRank(rank);

                if (dataManager.addUser(p)) {
                    showSuccess("✅ Enseignant ajouté (compte créé avec mot de passe temporaire).");
                    return true;
                } else {
                    showError("Impossible d'ajouter l'enseignant (voir logs).");
                }
            } else {
                existingProf.setLastName(lastName);
                existingProf.setFirstName(firstName);
                existingProf.setEmail(email.isEmpty() ? null : email);
                existingProf.setPhoneNumber(phone.isEmpty() ? null : phone);
                existingProf.setDepartment(department);
                existingProf.setAcademicRank(rank);

                if (dataManager.updateUser(existingProf)) {
                    showSuccess("✅ Enseignant modifié avec succès.");
                    return true;
                } else {
                    showError("Impossible de mettre à jour l'enseignant (voir logs).");
                }
            }
        } catch (Exception e) {
            showError("Erreur lors de l'enregistrement de l'enseignant : " + e.getMessage());
        }
        return false;
    }

    // =========================================================================
    // COMPTES
    // =========================================================================
    private void bindAccountButtons() {
        if (view.getAddAccountBtn()    != null) view.getAddAccountBtn().addActionListener(e    -> createAccount());
        if (view.getResetPasswordBtn() != null) view.getResetPasswordBtn().addActionListener(e -> resetPassword());
        if (view.getDeleteAccountBtn() != null) view.getDeleteAccountBtn().addActionListener(e -> deleteAccount());
    }

    private void loadAccountsTable() {
        try {
            List<User> users = dataManager.getAllUsers();
            ModernTable table = view.getAccountsTable();
            if (table == null) return;

            DefaultTableModel model = (DefaultTableModel) table.getModel();
            model.setRowCount(0);

            int id = 1;
            for (User u : users) {
                model.addRow(new Object[]{
                        "ACC" + String.format("%03d", id++),
                        u.getRole(),
                        u.getCode(),
                        u.getFullName(),
                        "2024-09-01",  // si tu as une vraie date, remplace ici
                        "Actif",
                        "Gérer"
                });
            }

        } catch (Exception e) {
            showError("Erreur chargement comptes : " + e.getMessage());
        }
    }

    private void createAccount() {
        JDialog dialog = new JDialog(view, "Créer un Compte", true);
        dialog.setLayout(new GridBagLayout());
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(view);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JComboBox<String> typeCombo = new JComboBox<>(new String[]{"Étudiant", "Enseignant"});
        JTextField codeField        = new JTextField(20);
        JPasswordField passField    = new JPasswordField(20);
        JPasswordField confirmField = new JPasswordField(20);

        int row = 0;
        addDialogField(dialog, gbc, row++, "Type de compte* :", typeCombo);
        addDialogField(dialog, gbc, row++, "Code* :",           codeField);
        addDialogField(dialog, gbc, row++, "Mot de passe* :",   passField);
        addDialogField(dialog, gbc, row++, "Confirmer* :",      confirmField);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        JButton createBtn = new JButton("✓ Créer");
        JButton cancelBtn = new JButton("✗ Annuler");

        createBtn.addActionListener(e -> {
            String type    = (String) typeCombo.getSelectedItem();
            String code    = codeField.getText().trim();
            String pass    = new String(passField.getPassword());
            String confirm = new String(confirmField.getPassword());

            if (createNewAccount(type, code, pass, confirm)) {
                dialog.dispose();
                reloadCaches();
                loadAccountsTable();
                refreshDashboardStats();
            }
        });
        cancelBtn.addActionListener(e -> dialog.dispose());

        btnPanel.add(cancelBtn);
        btnPanel.add(createBtn);

        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        dialog.add(btnPanel, gbc);

        dialog.setVisible(true);
    }

    private boolean createNewAccount(String type, String code, String password, String confirm) {
        try {
            if (code.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
                showError("Veuillez remplir tous les champs.");
                return false;
            }
            if (!password.equals(confirm)) {
                showError("Les mots de passe ne correspondent pas.");
                return false;
            }
            if (password.length() < 6) {
                showError("Mot de passe trop court (≥ 6 caractères).");
                return false;
            }

            if (dataManager.getUser(code) != null) {
                showError("Un utilisateur avec ce code existe déjà.");
                return false;
            }

            User user;
            if ("Étudiant".equals(type)) {
                if (!code.matches("^1\\d{7}$")) {
                    showError("Code étudiant invalide (1 + 7 chiffres).");
                    return false;
                }
                user = new Student(code, password, "", "");
            } else {
                if (!code.matches("^2\\d{7}$")) {
                    showError("Code enseignant invalide (2 + 7 chiffres).");
                    return false;
                }
                user = new Professor(code, password, "", "");
            }

            if (dataManager.addUser(user)) {
                showSuccess("✅ Compte créé avec succès (notification envoyée).");
                return true;
            } else {
                showError("Impossible de créer le compte (voir logs).");
            }

        } catch (Exception e) {
            showError("Erreur lors de la création du compte : " + e.getMessage());
        }
        return false;
    }

    private void resetPassword() {
        ModernTable table = view.getAccountsTable();
        if (table == null || table.getSelectedRow() == -1) {
            showInfo("Veuillez sélectionner un compte.");
            return;
        }

        int row = table.getSelectedRow();
        String code = String.valueOf(table.getValueAt(row, 2));

        String newPass = JOptionPane.showInputDialog(
                view,
                "Entrez le nouveau mot de passe pour " + code + " :",
                "Réinitialisation du mot de passe",
                JOptionPane.QUESTION_MESSAGE
        );
        if (newPass == null || newPass.isEmpty()) return;
        if (newPass.length() < 6) {
            showError("Mot de passe trop court (≥ 6 caractères).");
            return;
        }

        try {
            User user = dataManager.getUser(code);
            if (user == null) {
                showError("Utilisateur introuvable : " + code);
                return;
            }
            user.setPassword(newPass);

            dataManager.updateUser(user);

            notificationManager.sendNotification(
                    user.getCode(),
                    viceDean.getCode(),
                    Notification.TYPE_PASSWORD_RESET,
                    "🔒 Mot de passe réinitialisé",
                    "Votre mot de passe a été réinitialisé par l'administration. " +
                            "Si vous n'êtes pas à l'origine de cette demande, contactez rapidement le service scolarité.",
                    Notification.PRIORITY_URGENT
            );

            showSuccess("✅ Mot de passe réinitialisé (notification envoyée).");

        } catch (Exception e) {
            showError("Erreur lors de la réinitialisation : " + e.getMessage());
        }
    }

    private void deleteAccount() {
        ModernTable table = view.getAccountsTable();
        if (table == null || table.getSelectedRow() == -1) {
            showInfo("Veuillez sélectionner un compte à supprimer.");
            return;
        }

        int row = table.getSelectedRow();
        String code = String.valueOf(table.getValueAt(row, 2));

        User user = dataManager.getUser(code);
        if (user == null) {
            showError("Utilisateur introuvable : " + code);
            return;
        }

        String role = user.getRole();

        int confirm = JOptionPane.showConfirmDialog(
                view,
                "Voulez-vous vraiment supprimer le compte de " + role + " (" + code + ") ?\n" +
                        "Toutes les données liées à cet utilisateur seront nettoyées.",
                "Confirmation de suppression",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            boolean ok = dataManager.deleteUser(code);
            if (ok) {
                showSuccess("✅ Compte supprimé avec succès.");
                reloadCaches();
                loadAccountsTable();
                if (user instanceof Student) {
                    loadStudentsTable();
                } else if (user instanceof Professor) {
                    loadTeachersTable();
                    loadAvailableModules();
                }
                refreshDashboardStats();
            } else {
                showError("Impossible de supprimer le compte (voir logs).");
            }
        } catch (Exception e) {
            showError("Erreur lors de la suppression du compte : " + e.getMessage());
            e.printStackTrace();
        }
    }

    // =========================================================================
    // RAPPORTS & STATISTIQUES
    // =========================================================================
    private void bindReportButtons() {
        if (view.getExportReportBtn()  != null) view.getExportReportBtn().addActionListener(e  -> exportReport());
        if (view.getGenerateStatsBtn() != null) view.getGenerateStatsBtn().addActionListener(e -> generateStatistics());
    }

    private void exportReport() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Exporter le rapport au format CSV");
        chooser.setSelectedFile(new File("rapport_" + LocalDate.now() + ".csv"));
        if (chooser.showSaveDialog(view) != JFileChooser.APPROVE_OPTION) return;

        File file = chooser.getSelectedFile();
        try (PrintWriter writer = new PrintWriter(file)) {
            List<Student> students            = dataManager.getAllStudents();
            List<Professor> professors        = dataManager.getAllProfessors();
            List<Module> modules              = dataManager.getAllModules();
            List<Grade> allGrades             = dataManager.getAllGrades();
            List<Inscription> allInscriptions = dataManager.getAllInscriptions();

            writer.println("RAPPORT ADMINISTRATIF," + LocalDate.now());
            writer.println();

            int totalStudents = students.size();
            int totalTeachers = professors.size();
            int totalModules  = modules.size();

            long nbAdmis = students.stream()
                    .filter(s -> calculateRealStudentAverage(s.getCode(), allGrades) >= 10.0)
                    .count();
            double tauxReussite = (totalStudents > 0)
                    ? (nbAdmis * 100.0 / totalStudents)
                    : 0.0;

            writer.println("STATISTIQUES GÉNÉRALES");
            writer.println("Nombre d'étudiants," + totalStudents);
            writer.println("Nombre d'enseignants," + totalTeachers);
            writer.println("Nombre de modules," + totalModules);
            writer.println("Étudiants admis," + nbAdmis);
            writer.println("Taux de réussite," + String.format("%.1f%%", tauxReussite));
            writer.println();

            writer.println("DÉTAILS PAR MODULE");
            writer.println("Code,Nom,Professeur,Crédits,Inscrits,Notes,Moyenne,Succès (%)");

            for (Module m : modules) {
                String code = m.getCode();
                User profUser = (m.getProfessorCode() != null)
                        ? dataManager.getUser(m.getProfessorCode())
                        : null;
                String profName = profUser != null ? profUser.getFullName() : "Non affecté";

                long nbInscrits = allInscriptions.stream()
                        .filter(ins -> ins.getModuleCode().equals(code))
                        .count();

                List<Grade> moduleGrades = allGrades.stream()
                        .filter(g -> g.getModuleCode().equals(code))
                        .toList();

                int nbNotes = moduleGrades.size();
                double moyenneModule = moduleGrades.stream()
                        .mapToDouble(Grade::getValue)
                        .average()
                        .orElse(0.0);

                long nbNotesAdmissibles = moduleGrades.stream()
                        .filter(Grade::isPassing)
                        .count();

                double tauxSuccesModule = (nbNotes > 0)
                        ? (nbNotesAdmissibles * 100.0 / nbNotes)
                        : 0.0;

                writer.println(String.join(",",
                        code,
                        safeCsv(m.getName()),
                        safeCsv(profName),
                        String.valueOf(m.getCredits()),
                        String.valueOf(nbInscrits),
                        String.valueOf(nbNotes),
                        String.format("%.2f", moyenneModule),
                        String.format("%.1f", tauxSuccesModule)
                ));
            }

            showSuccess("✅ Rapport exporté avec succès : " + file.getName());
        } catch (Exception e) {
            showError("Erreur lors de l'export du rapport : " + e.getMessage());
        }
    }

    private void generateStatistics() {
        try {
            List<Student> students   = dataManager.getAllStudents();
            List<Professor> profs    = dataManager.getAllProfessors();
            List<Module> modules     = dataManager.getAllModules();
            List<Grade> allGrades    = dataManager.getAllGrades();

            int totalStudents = students.size();
            int totalTeachers = profs.size();
            int totalModules  = modules.size();

            long nbAdmis = students.stream()
                    .filter(s -> calculateRealStudentAverage(s.getCode(), allGrades) >= 10.0)
                    .count();
            double tauxReussite = (totalStudents > 0)
                    ? (nbAdmis * 100.0 / totalStudents)
                    : 0.0;

            double moyenneGenerale = students.stream()
                    .mapToDouble(s -> calculateRealStudentAverage(s.getCode(), allGrades))
                    .average()
                    .orElse(0.0);

            long modulesAffectes = modules.stream()
                    .filter(m -> m.getProfessorCode() != null && !m.getProfessorCode().isEmpty())
                    .count();
            double tauxAffectation = (totalModules > 0)
                    ? (modulesAffectes * 100.0 / totalModules)
                    : 0.0;

            showInfo(String.format(
                    "📊 STATISTIQUES COMPLÈTES\n\n" +
                            "👥 ÉTUDIANTS\n" +
                            "  Total : %d\n" +
                            "  Admis : %d (%.1f%%)\n" +
                            "  Moyenne générale : %.2f / 20\n\n" +
                            "👨‍🏫 ENSEIGNANTS\n" +
                            "  Total : %d\n\n" +
                            "📚 MODULES\n" +
                            "  Total : %d\n" +
                            "  Affectés : %d / %d (%.1f%%)\n",
                    totalStudents,
                    nbAdmis, tauxReussite,
                    moyenneGenerale,
                    totalTeachers,
                    totalModules,
                    modulesAffectes, totalModules, tauxAffectation
            ));
        } catch (Exception e) {
            showError("Erreur lors de la génération des statistiques : " + e.getMessage());
        }
    }

    private String safeCsv(String s) {
        if (s == null) return "";
        if (s.contains(",") || s.contains("\"")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }

    // =========================================================================
    // PARAMÈTRES
    // =========================================================================
    private void showSettingsDialog() {
        JDialog dialog = new JDialog(view, "Paramètres", true);
        dialog.setLayout(new BorderLayout(15, 15));
        dialog.setSize(500, 380);
        dialog.setLocationRelativeTo(view);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        content.add(createSettingItem("🔔 Notifications", "Activer les notifications", null));
        content.add(Box.createRigidArea(new Dimension(0, 10)));

        content.add(createSettingItem("🌐 Langue", "Français", null));
        content.add(Box.createRigidArea(new Dimension(0, 10)));

        content.add(createSettingItem("🔐 Sécurité", "Modifier le mot de passe", this::openChangePasswordDialog));
        content.add(Box.createRigidArea(new Dimension(0, 10)));

        String currentEmail = viceDean.getEmail() != null ? viceDean.getEmail() : "Non renseigné";
        content.add(createSettingItem("📧 Email", currentEmail, this::openChangeEmailDialog));

        dialog.add(content, BorderLayout.CENTER);

        JButton closeBtn = new JButton("Fermer");
        closeBtn.addActionListener(e -> dialog.dispose());
        JPanel btnPanel = new JPanel();
        btnPanel.add(closeBtn);
        dialog.add(btnPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    private JPanel createSettingItem(String title, String description, Runnable onClick) {
        JPanel panel = new JPanel(new BorderLayout(10, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        panel.setBackground(new Color(20, 20, 50));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        titleLbl.setForeground(Color.WHITE);

        JLabel descLbl = new JLabel(description);
        descLbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        descLbl.setForeground(new Color(160, 160, 190));

        panel.add(titleLbl, BorderLayout.NORTH);
        panel.add(descLbl, BorderLayout.CENTER);

        JLabel arrow = new JLabel("→");
        arrow.setFont(new Font("Segoe UI", Font.BOLD, 18));
        arrow.setForeground(new Color(0, 150, 255));
        panel.add(arrow, BorderLayout.EAST);

        if (onClick != null) {
            panel.setCursor(new Cursor(Cursor.HAND_CURSOR));
            panel.addMouseListener(new java.awt.event.MouseAdapter() {
                Color base = panel.getBackground();
                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    panel.setBackground(new Color(30, 30, 70));
                }
                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    panel.setBackground(base);
                }
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    onClick.run();
                }
            });
        }

        return panel;
    }

    private void openChangePasswordDialog() {
        JDialog dialog = new JDialog(view, "Changer le mot de passe", true);
        dialog.setLayout(new GridBagLayout());
        dialog.setSize(420, 260);
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
                showError("Veuillez remplir tous les champs.");
                return;
            }
            if (!newPass.equals(confirm)) {
                showError("Les mots de passe ne correspondent pas.");
                return;
            }
            if (newPass.length() < 6) {
                showError("Le mot de passe doit contenir au moins 6 caractères.");
                return;
            }
            if (!oldPass.equals(viceDean.getPassword())) {
                showError("Ancien mot de passe incorrect.");
                return;
            }

            try {
                viceDean.setPassword(newPass);
                dataManager.updateUser(viceDean);

                notificationManager.sendNotification(
                        viceDean.getCode(),
                        viceDean.getCode(),
                        Notification.TYPE_PASSWORD_RESET,
                        "🔒 Mot de passe modifié",
                        "Votre mot de passe de compte Vice-Doyen a été modifié avec succès.",
                        Notification.PRIORITY_NORMAL
                );

                JOptionPane.showMessageDialog(
                        dialog,
                        "✓ Mot de passe changé avec succès.",
                        "Succès",
                        JOptionPane.INFORMATION_MESSAGE
                );
                dialog.dispose();
            } catch (Exception ex) {
                showError("Erreur lors de la mise à jour : " + ex.getMessage());
            }
        });

        btnPanel.add(cancelBtn);
        btnPanel.add(saveBtn);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        dialog.add(btnPanel, gbc);

        dialog.setVisible(true);
    }

    private void openChangeEmailDialog() {
        String currentEmail = viceDean.getEmail() != null ? viceDean.getEmail() : "";

        String newEmail = JOptionPane.showInputDialog(
                view,
                "Nouvelle adresse email :",
                currentEmail
        );

        if (newEmail == null) return;
        newEmail = newEmail.trim();
        if (newEmail.isEmpty()) {
            showError("L'adresse email ne peut pas être vide.");
            return;
        }
        if (!newEmail.matches("^[^@]+@[^@]+\\.[^@]+$")) {
            showError("Adresse email invalide.");
            return;
        }

        try {
            viceDean.setEmail(newEmail);
            dataManager.updateUser(viceDean);
            showSuccess("Adresse email mise à jour.");
        } catch (Exception ex) {
            showError("Erreur lors de la mise à jour de l'email : " + ex.getMessage());
        }
    }

    // =========================================================================
    // DASHBOARD (STATISTIQUES RÉELLES + QUICK STATS + TÂCHES + ACTIONS)
    // =========================================================================
    private void refreshDashboardStats() {
        try {
            List<Student>   students   = dataManager.getAllStudents();
            List<Professor> professors = dataManager.getAllProfessors();
            List<Module>    modules    = dataManager.getAllModules();
            List<Grade>     allGrades  = dataManager.getAllGrades();
            List<Inscription> ins      = dataManager.getAllInscriptions();
            List<User>      users      = dataManager.getAllUsers();

            int totalStudents = students.size();
            int totalTeachers = professors.size();
            int totalModules  = modules.size();

            long nbAdmis = students.stream()
                    .filter(s -> calculateRealStudentAverage(s.getCode(), allGrades) >= 10.0)
                    .count();
            double successRate = (totalStudents > 0)
                    ? (nbAdmis * 100.0 / totalStudents)
                    : 0.0;

            // Cartes principales
            view.updateDashboardCards(
                    totalStudents,
                    totalTeachers,
                    totalModules,
                    successRate
            );

            // -------- Statistiques rapides réalistes --------
            EnhancedViceDeanView.QuickStats qs = new EnhancedViceDeanView.QuickStats();
            qs.totalInscriptions     = ins.size();
            qs.validatedInscriptions = (int) ins.stream().filter(Inscription::isValidated).count();
            qs.totalModules          = totalModules;
            qs.assignedModules       = (int) modules.stream()
                    .filter(m -> m.getProfessorCode() != null && !m.getProfessorCode().isEmpty())
                    .count();
            qs.activeAccounts        = users.size(); // si tu as un flag "actif", adapte ici

            view.updateQuickStats(qs);

            // -------- Tâches en attente réalistes --------
            int pendingInsc = (int) ins.stream().filter(i -> !i.isValidated()).count();
            int unassigned  = (int) modules.stream()
                    .filter(m -> m.getProfessorCode() == null || m.getProfessorCode().isEmpty())
                    .count();

            view.updatePendingTasks(pendingInsc, unassigned, successRate);

            // -------- Actions récentes réalistes --------
            List<String> actions = new ArrayList<>();

            // Dernier compte créé (code le plus "grand" -> dernier créé dans nos démos)
            users.stream()
                    .max(Comparator.comparing(User::getCode))
                    .ifPresent(u -> actions.add("Compte créé : " + u.getCode() + " (" + u.getRole() + ")"));

            // Dernier module affecté (module avec professeur)
            modules.stream()
                    .filter(m -> m.getProfessorCode() != null && !m.getProfessorCode().isEmpty())
                    .max(Comparator.comparing(Module::getCode))
                    .ifPresent(m -> {
                        User prof = dataManager.getUser(m.getProfessorCode());
                        String profName = (prof != null) ? prof.getFullName() : m.getProfessorCode();
                        actions.add("Module " + m.getCode() + " affecté à " + profName);
                    });

            // Info sur les validations d'inscriptions
            if (qs.validatedInscriptions > 0) {
                actions.add(qs.validatedInscriptions + " inscription(s) validée(s)");
            } else {
                actions.add("Aucune inscription validée pour le moment");
            }

            if (!actions.isEmpty()) {
                view.updateRecentActions(actions.subList(0, Math.min(3, actions.size())));
            } else {
                view.updateRecentActions(Collections.emptyList());
            }

        } catch (Exception e) {
            System.err.println("Erreur refresh dashboard : " + e.getMessage());
        }
    }

    private double calculateRealStudentAverage(String studentCode, List<Grade> allGrades) {
        return allGrades.stream()
                .filter(g -> g.getStudentCode().equals(studentCode))
                .mapToDouble(Grade::getValue)
                .average()
                .orElse(0.0);
    }

    private String getStudentStatus(double avg) {
        if (avg >= 10.0) return "Admis";
        if (avg >= 7.0)  return "Redouble";
        return "Exclus";
    }

    private void addDialogField(JDialog dialog, GridBagConstraints gbc,
                                int row, String label, Component field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        dialog.add(lbl, gbc);

        gbc.gridx = 1;
        dialog.add(field, gbc);
    }

    // =========================================================================
    // FEEDBACK UI
    // =========================================================================
    private void showError(String msg) {
        JOptionPane.showMessageDialog(view, msg, "❌ Erreur", JOptionPane.ERROR_MESSAGE);
    }

    private void showInfo(String msg) {
        JOptionPane.showMessageDialog(view, msg, "ℹ️ Information", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showSuccess(String msg) {
        AnimatedComponents.showSlideNotification(
                view,
                msg,
                AnimatedComponents.NotificationType.SUCCESS
        );
    }
}
