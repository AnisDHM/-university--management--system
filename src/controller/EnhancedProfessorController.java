package controller;

import model.dao.DataManager;
import model.dao.NotificationManager;
import model.entities.*;
import model.entities.Module;
import view.EnhancedProfessorView;
import view.LoginView;
import view.components.ModernUIComponents.ModernTable;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

/**
 * EnhancedProfessorController - Version rigoureuse et enrichie
 * - Données 100% alignées avec DataManager (notes, absences, inscriptions)
 * - Filtrage par modules du professeur
 * - CRUD complet sur modules, notes, absences
 * - Notifications automatiques (via DataManager, sans doublons)
 * - Validation stricte + retours clairs
 * - Statistiques poussées (dashboard + rapports + cartes de la vue)
 * - Synchronisation forte avec EnhancedProfessorView
 */
public class EnhancedProfessorController {

    private final EnhancedProfessorView view;
    private final Professor professor;
    private final DataManager dataManager;
    private final NotificationManager notificationManager;

    private List<Module>  cachedModules   = new ArrayList<>();
    private List<Student> cachedStudents  = new ArrayList<>();
    private List<Grade>   cachedGrades    = new ArrayList<>();
    private List<Absence> cachedAbsences  = new ArrayList<>();

    // Statistiques calculées pour rapports & cartes
    private static class ProfStats {
        double globalAverage;
        double successRate;
        double presenceRate;
        long   studentsInDifficulty;
    }

    public EnhancedProfessorController(EnhancedProfessorView view, User user) {
        this.view = Objects.requireNonNull(view, "view ne doit pas être null");
        this.professor = (Professor) Objects.requireNonNull(user, "user ne doit pas être null");
        this.dataManager = DataManager.getInstance();
        this.notificationManager = dataManager.getNotificationManager();

        initController();
        loadAllData();
    }

    // ========================================================================
    // INITIALISATION
    // ========================================================================
    private void initController() {
        setupNavigationButtons();
        setupUnitsActions();
        setupStudentsActions();
        setupGradesActions();
        setupAbsencesActions();
        setupReportsActions();
        initModuleComboBoxes();
    }

    /**
     * Charge toutes les données du professeur depuis DataManager,
     * met les caches à jour, puis synchronise la vue (tables + dashboard + stats).
     */
    private void loadAllData() {
        try {
            // 1) Données brutes
            cachedModules = Optional.ofNullable(
                    dataManager.getProfessorModules(professor.getCode())
            ).orElseGet(ArrayList::new);

            cachedStudents = Optional.ofNullable(
                    dataManager.getStudentsForProfessor(professor.getCode())
            ).orElseGet(ArrayList::new);

            List<Grade> allGrades = Optional.ofNullable(
                    dataManager.getAllGrades()
            ).orElseGet(ArrayList::new);

            Set<String> professorModuleCodes =
                    cachedModules.stream().map(Module::getCode).collect(Collectors.toSet());

            cachedGrades = allGrades.stream()
                    .filter(g -> professorModuleCodes.contains(g.getModuleCode()))
                    .collect(Collectors.toList());

            List<Absence> allAbs = Optional.ofNullable(
                    dataManager.getAllAbsences()
            ).orElseGet(ArrayList::new);

            cachedAbsences = allAbs.stream()
                    .filter(a -> professorModuleCodes.contains(a.getModuleCode()))
                    .collect(Collectors.toList());

            // 2) MAJ UI
            refreshUnitsTable();
            refreshStudentsTable(cachedStudents);
            refreshAbsencesTable();
            updateDashboardStats();
            initModuleComboBoxes();
            view.updateProfileStats(cachedModules.size(), cachedStudents.size());


            ProfStats stats = computeStats();
            view.updateReportStats(
                    stats.successRate,
                    stats.globalAverage,
                    stats.presenceRate,
                    stats.studentsInDifficulty
            );

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur lors du chargement des données : " + e.getMessage());
        }
    }

    private void initModuleComboBoxes() {
        if (view.getModuleCombo() != null) {
            JComboBox<String> box = view.getModuleCombo();
            box.removeAllItems();
            for (Module m : cachedModules) {
                box.addItem(m.getCode() + " - " + m.getName());
            }
        }

        if (view.getModuleFilter() != null) {
            JComboBox<String> filter = view.getModuleFilter();
            filter.removeAllItems();
            filter.addItem("Tous les modules");
            for (Module m : cachedModules) {
                filter.addItem(m.getCode() + " - " + m.getName());
            }
        }
    }

    // ========================================================================
    // NAVIGATION
    // ========================================================================
    private void setupNavigationButtons() {
        if (view.getDashboardBtn() != null) view.getDashboardBtn().addActionListener(e -> showDashboard());
        if (view.getUnitsBtn()     != null) view.getUnitsBtn().addActionListener(e     -> showUnits());
        if (view.getStudentsBtn()  != null) view.getStudentsBtn().addActionListener(e  -> showStudents());
        if (view.getGradesBtn()    != null) view.getGradesBtn().addActionListener(e    -> showGrades());
        if (view.getAbsencesBtn()  != null) view.getAbsencesBtn().addActionListener(e  -> showAbsences());
        if (view.getReportsBtn()   != null) view.getReportsBtn().addActionListener(e   -> showReports());
        if (view.getProfileBtn()   != null) view.getProfileBtn().addActionListener(e   -> showProfile());
        if (view.getLogoutBtn()    != null) view.getLogoutBtn().addActionListener(e    -> logout());
    }

    private void showDashboard() { updateDashboardStats(); }
    private void showUnits()     { refreshUnitsTable(); }
    private void showStudents()  { refreshStudentsTable(cachedStudents); }
    private void showGrades()    { /* contenu déjà prêt, géré par les boutons */ }
    private void showAbsences()  { refreshAbsencesTable(); }
    private void showReports()   { /* panel statique, stats déjà mises à jour */ }
    private void showProfile()   { createProfileDialog(); }

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
            LoginView loginView = new LoginView();
            new LoginController(loginView);
            loginView.setVisible(true);
        });
    }

    // ========================================================================
    // UNITÉS (MODULES) - CRUD
    // ========================================================================
    private void setupUnitsActions() {
        if (view.getAddUnitBtn()    != null) view.getAddUnitBtn().addActionListener(e    -> handleAddUnit());
        if (view.getEditUnitBtn()   != null) view.getEditUnitBtn().addActionListener(e   -> handleEditUnit());
        if (view.getDeleteUnitBtn() != null) view.getDeleteUnitBtn().addActionListener(e -> handleDeleteUnit());
    }

    private void handleAddUnit() {
        JDialog dialog = createUnitDialog("Ajouter une Unité", null);
        dialog.setVisible(true);
    }

    private void handleEditUnit() {
        ModernTable table = view.getUnitsTable();
        if (table == null) {
            showError("Table des unités non initialisée.");
            return;
        }

        int row = table.getSelectedRow();
        if (row == -1) {
            showInfo("Veuillez sélectionner une unité à modifier.");
            return;
        }

        String code = safeToString(table.getValueAt(row, 0));
        Module module = findModuleByCode(code);
        if (module == null) {
            showError("Module introuvable. Rechargez les données.");
            return;
        }

        JDialog dialog = createUnitDialog("Modifier l'Unité", module);
        dialog.setVisible(true);
    }

    private void handleDeleteUnit() {
        ModernTable table = view.getUnitsTable();
        if (table == null) {
            showError("Table des unités non initialisée.");
            return;
        }

        int row = table.getSelectedRow();
        if (row == -1) {
            showInfo("Veuillez sélectionner une unité à supprimer.");
            return;
        }

        String code = safeToString(table.getValueAt(row, 0));
        String name = safeToString(table.getValueAt(row, 1));

        if (code.isEmpty()) {
            showError("Code de l'unité invalide.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                view,
                "Êtes-vous sûr de vouloir supprimer l'unité ?\n\n" +
                        "Code : " + code + "\n" +
                        "Nom : " + name + "\n\n" +
                        "⚠ Toutes les notes, absences et inscriptions associées seront aussi supprimées.",
                "Confirmation de suppression",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            boolean ok = dataManager.deleteModule(code);
            if (!ok) {
                showError("Suppression échouée (module introuvable).");
                return;
            }

            loadAllData();
            showSuccess("Unité supprimée avec succès.");

        } catch (Exception ex) {
            ex.printStackTrace();
            showError("Erreur lors de la suppression : " + ex.getMessage());
        }
    }

    private JDialog createUnitDialog(String title, Module existingModule) {
        JDialog dialog = new JDialog(view, title, true);
        dialog.setLayout(new GridBagLayout());
        dialog.setSize(550, 500);
        dialog.setLocationRelativeTo(view);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 15, 10, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel(title, JLabel.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        dialog.add(titleLabel, gbc);
        gbc.gridwidth = 1;

        // Code
        gbc.gridx = 0; gbc.gridy = 1;
        dialog.add(new JLabel("Code de l'unité :"), gbc);
        JTextField codeField = new JTextField(20);
        if (existingModule != null) {
            codeField.setText(existingModule.getCode());
            codeField.setEnabled(false);
        }
        gbc.gridx = 1;
        dialog.add(codeField, gbc);

        // Intitulé
        gbc.gridx = 0; gbc.gridy = 2;
        dialog.add(new JLabel("Intitulé :"), gbc);
        JTextField nameField = new JTextField(20);
        if (existingModule != null) nameField.setText(existingModule.getName());
        gbc.gridx = 1;
        dialog.add(nameField, gbc);

        // Crédits
        gbc.gridx = 0; gbc.gridy = 3;
        dialog.add(new JLabel("Crédits :"), gbc);
        SpinnerNumberModel creditsModel = new SpinnerNumberModel(
                existingModule != null ? existingModule.getCredits() : 3,
                1, 30, 1
        );
        JSpinner creditsSpinner = new JSpinner(creditsModel);
        gbc.gridx = 1;
        dialog.add(creditsSpinner, gbc);

        // Semestre
        gbc.gridx = 0; gbc.gridy = 4;
        dialog.add(new JLabel("Semestre :"), gbc);
        String[] semesters = {"1", "2"};
        JComboBox<String> semesterCombo = new JComboBox<>(semesters);
        if (existingModule != null) {
            semesterCombo.setSelectedItem(String.valueOf(existingModule.getSemester()));
        }
        gbc.gridx = 1;
        dialog.add(semesterCombo, gbc);

        // Coefficient
        gbc.gridx = 0; gbc.gridy = 5;
        dialog.add(new JLabel("Coefficient :"), gbc);
        SpinnerNumberModel coeffModel = new SpinnerNumberModel(
                existingModule != null ? existingModule.getCoefficient() : 1.0,
                0.5, 5.0, 0.5
        );
        JSpinner coeffSpinner = new JSpinner(coeffModel);
        gbc.gridx = 1;
        dialog.add(coeffSpinner, gbc);

        // Description
        gbc.gridx = 0; gbc.gridy = 6;
        dialog.add(new JLabel("Description :"), gbc);
        JTextArea descArea = new JTextArea(4, 20);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        if (existingModule != null && existingModule.getDescription() != null) {
            descArea.setText(existingModule.getDescription());
        }
        JScrollPane scroll = new JScrollPane(descArea);
        gbc.gridx = 1;
        dialog.add(scroll, gbc);

        // Boutons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        JButton cancelBtn = new JButton("Annuler");
        JButton saveBtn   = new JButton(existingModule != null ? "Modifier" : "Ajouter");

        cancelBtn.addActionListener(e -> dialog.dispose());

        saveBtn.addActionListener(e -> {
            String code        = safeToString(codeField.getText());
            String name        = safeToString(nameField.getText());
            int    credits     = (int) creditsSpinner.getValue();
            int    semester    = Integer.parseInt((String) semesterCombo.getSelectedItem());
            double coefficient = (double) coeffSpinner.getValue();
            String description = safeToString(descArea.getText());

            if (code.isEmpty())      { showError("Le code de l'unité est obligatoire."); return; }
            if (name.isEmpty())      { showError("L'intitulé de l'unité est obligatoire."); return; }
            if (credits <= 0)        { showError("Les crédits doivent être > 0."); return; }
            if (coefficient <= 0)    { showError("Le coefficient doit être > 0."); return; }
            if (semester != 1 && semester != 2) { showError("Le semestre doit être 1 ou 2."); return; }

            try {
                boolean isCreation = (existingModule == null);

                if (isCreation) {
                    if (dataManager.getModule(code) != null) {
                        showError("Un module avec ce code existe déjà.");
                        return;
                    }
                    Module newModule = new Module(
                            code, name, credits,
                            professor.getCode(),
                            coefficient, semester,
                            description.isEmpty() ? null : description
                    );
                    dataManager.addModule(newModule);
                    professor.addTaughtModule(newModule);

                } else {
                    Module updated = new Module(
                            existingModule.getCode(),
                            name,
                            credits,
                            professor.getCode(),
                            coefficient,
                            semester,
                            description.isEmpty() ? null : description
                    );
                    dataManager.updateModule(updated);
                    professor.removeModule(existingModule);
                    professor.addTaughtModule(updated);
                }

                loadAllData();
                showSuccess(isCreation ?
                        "Unité ajoutée avec succès." :
                        "Unité modifiée avec succès."
                );
                dialog.dispose();

            } catch (Exception ex) {
                ex.printStackTrace();
                showError("Erreur lors de l'enregistrement de l'unité : " + ex.getMessage());
            }
        });

        btnPanel.add(cancelBtn);
        btnPanel.add(saveBtn);

        gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 2;
        dialog.add(btnPanel, gbc);

        return dialog;
    }

    private Module findModuleByCode(String code) {
        if (cachedModules == null) return null;
        return cachedModules.stream()
                .filter(m -> m.getCode().equalsIgnoreCase(code))
                .findFirst()
                .orElse(null);
    }

    // ========================================================================
    // ÉTUDIANTS
    // ========================================================================
    private void setupStudentsActions() {
        if (view.getSearchStudentBtn() != null)
            view.getSearchStudentBtn().addActionListener(e -> searchStudents());

        if (view.getStudentsTable() != null) {
            view.getStudentsTable().addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                    if (evt.getClickCount() == 2) {
                        showStudentDetails();
                    }
                }
            });
        }
    }

    private void searchStudents() {
        String searchTerm = view.getSearchField().getText().trim().toLowerCase();

        String selectedFilter = (String) view.getModuleFilter().getSelectedItem();
        final String moduleFilter = (selectedFilter == null) ? "Tous les modules" : selectedFilter;

        List<Student> filtered = cachedStudents.stream()
                .filter(s -> {
                    boolean matchesSearch =
                            searchTerm.isEmpty() ||
                            s.getCode().toLowerCase().contains(searchTerm) ||
                            s.getFullName().toLowerCase().contains(searchTerm) ||
                            (s.getEmail() != null && s.getEmail().toLowerCase().contains(searchTerm));

                    if (!matchesSearch) return false;

                    if ("Tous les modules".equals(moduleFilter)) return true;

                    String moduleCode = moduleFilter.split(" - ")[0].trim();
                    return dataManager.isStudentRegistered(s.getCode(), moduleCode);
                })
                .collect(Collectors.toList());

        refreshStudentsTable(filtered);
        showInfo(filtered.size() + " étudiant(s) trouvé(s).");
    }

    private void showStudentDetails() {
        ModernTable table = view.getStudentsTable();
        if (table == null) return;

        int row = table.getSelectedRow();
        if (row == -1) return;

        String code = safeToString(table.getValueAt(row, 0));
        Student student = cachedStudents.stream()
                .filter(s -> s.getCode().equals(code))
                .findFirst()
                .orElse(null);

        if (student == null) {
            showError("Étudiant introuvable.");
            return;
        }

        createStudentDetailsDialog(student);
    }

    private void createStudentDetailsDialog(Student student) {
        JDialog dialog = new JDialog(view, "Détails de l'étudiant", true);
        dialog.setLayout(new BorderLayout(15, 15));
        dialog.setSize(700, 600);
        dialog.setLocationRelativeTo(view);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("📋 Informations de l'étudiant");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        addInfoRow(mainPanel, "Matricule :", student.getCode());
        addInfoRow(mainPanel, "Nom complet :", student.getFullName());
        addInfoRow(mainPanel, "Email :", student.getEmail() != null ? student.getEmail() : "Non renseigné");
        addInfoRow(mainPanel, "Spécialité :", student.getSpeciality());
        addInfoRow(mainPanel, "Année :", "L" + student.getYear());

        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        JLabel gradesLabel = new JLabel("📝 Notes dans mes modules");
        gradesLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        gradesLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(gradesLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        Set<String> profModuleCodes = cachedModules.stream()
                .map(Module::getCode)
                .collect(Collectors.toSet());

        List<Grade> studentGrades = cachedGrades.stream()
                .filter(g -> g.getStudentCode().equals(student.getCode())
                        && profModuleCodes.contains(g.getModuleCode()))
                .collect(Collectors.toList());

        if (studentGrades.isEmpty()) {
            JLabel noGradesLabel = new JLabel("Aucune note enregistrée dans vos modules");
            noGradesLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
            noGradesLabel.setForeground(Color.GRAY);
            noGradesLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            mainPanel.add(noGradesLabel);
        } else {
            for (Grade grade : studentGrades) {
                Module module = cachedModules.stream()
                        .filter(m -> m.getCode().equals(grade.getModuleCode()))
                        .findFirst()
                        .orElse(null);
                String moduleName = module != null ? module.getName() : grade.getModuleCode();
                addInfoRow(
                        mainPanel,
                        moduleName + " (" + grade.getType() + ") :",
                        grade.getFormattedGrade() + " - " + grade.getMention()
                );
            }
        }

        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        JLabel absLabel = new JLabel("📅 Absences dans mes modules");
        absLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        absLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(absLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        List<Absence> studentAbsences = cachedAbsences.stream()
                .filter(a -> a.getStudentCode().equals(student.getCode()))
                .collect(Collectors.toList());

        if (studentAbsences.isEmpty()) {
            JLabel noAbsLabel = new JLabel("Aucune absence enregistrée dans vos modules");
            noAbsLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
            noAbsLabel.setForeground(Color.GRAY);
            noAbsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            mainPanel.add(noAbsLabel);
        } else {
            for (Absence a : studentAbsences) {
                Module module = cachedModules.stream()
                        .filter(m -> m.getCode().equals(a.getModuleCode()))
                        .findFirst()
                        .orElse(null);
                String moduleName = module != null ? module.getName() : a.getModuleCode();
                String status = a.isJustified() ? "✓ Justifiée" : "✗ Non justifiée";
                addInfoRow(
                        mainPanel,
                        moduleName + " - " + a.getFormattedDate() + " :",
                        a.getSessionType() + " - " + status
                );
            }
        }

        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        dialog.add(scrollPane, BorderLayout.CENTER);

        JButton closeBtn = new JButton("Fermer");
        closeBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        closeBtn.addActionListener(e -> dialog.dispose());
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buttonPanel.add(closeBtn);

        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void addInfoRow(JPanel panel, String label, String value) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel labelComp = new JLabel(label);
        labelComp.setFont(new Font("Segoe UI", Font.BOLD, 14));
        labelComp.setPreferredSize(new Dimension(220, 25));

        JLabel valueComp = new JLabel(value);
        valueComp.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        row.add(labelComp);
        row.add(valueComp);
        panel.add(row);
    }

    // ========================================================================
    // NOTES - CRUD
    // ========================================================================
    private void setupGradesActions() {
        if (view.getLoadStudentsBtn() != null) view.getLoadStudentsBtn().addActionListener(e -> loadStudentsForGrading());
        if (view.getSaveGradesBtn()   != null) view.getSaveGradesBtn().addActionListener(e   -> handleSaveGrades());
        if (view.getCancelGradesBtn() != null) view.getCancelGradesBtn().addActionListener(e -> cancelGrading());
    }

    private void loadStudentsForGrading() {
        String moduleItem = (String) view.getModuleCombo().getSelectedItem();
        String type       = (String) view.getTypeCombo().getSelectedItem();

        if (moduleItem == null || type == null) {
            showInfo("Veuillez sélectionner un module et un type de note.");
            return;
        }

        String moduleCode = moduleItem.split(" - ")[0].trim();
        Module module = findModuleByCode(moduleCode);
        if (module == null) {
            showError("Module invalide ou non assigné à ce professeur.");
            return;
        }

        DefaultTableModel model = (DefaultTableModel) view.getGradesTable().getModel();
        model.setRowCount(0);

        List<Student> moduleStudents = cachedStudents.stream()
                .filter(s -> dataManager.isStudentRegistered(s.getCode(), moduleCode))
                .collect(Collectors.toList());

        if (moduleStudents.isEmpty()) {
            showInfo("Aucun étudiant inscrit dans ce module.");
            return;
        }

        for (Student s : moduleStudents) {
            Grade existing = findGrade(s.getCode(), moduleCode, type);
            Object gradeStr = existing != null ? String.format("%.2f", existing.getValue()) : "";
            model.addRow(new Object[] {
                    s.getCode(),
                    s.getLastName(),
                    s.getFirstName(),
                    gradeStr,
                    "" // observation libre
            });
        }

        showInfo(moduleStudents.size() + " étudiant(s) chargé(s) pour la saisie des notes.");
    }

    private void handleSaveGrades() {
        String moduleItem = (String) view.getModuleCombo().getSelectedItem();
        String type       = (String) view.getTypeCombo().getSelectedItem();

        if (moduleItem == null || type == null) {
            showInfo("Veuillez sélectionner un module et un type.");
            return;
        }

        String moduleCode = moduleItem.split(" - ")[0].trim();
        Module module = findModuleByCode(moduleCode);
        if (module == null) {
            showError("Module invalide ou non assigné à ce professeur.");
            return;
        }

        DefaultTableModel model = (DefaultTableModel) view.getGradesTable().getModel();

        int confirm = JOptionPane.showConfirmDialog(
                view,
                "Enregistrer toutes les notes saisies ?\nModule : " + moduleItem + "\nType : " + type,
                "Confirmation",
                JOptionPane.YES_NO_OPTION
        );
        if (confirm != JOptionPane.YES_OPTION) return;

        int saved  = 0;
        int errors = 0;
        StringBuilder errorMsg = new StringBuilder();
        LocalDate today = LocalDate.now();

        for (int i = 0; i < model.getRowCount(); i++) {
            String studentCode = safeToString(model.getValueAt(i, 0));
            String gradeStr    = safeToString(model.getValueAt(i, 3));

            if (gradeStr.isEmpty()) continue;

            try {
                double value = Double.parseDouble(gradeStr.replace(',', '.'));
                Grade existing = findGrade(studentCode, moduleCode, type);
                boolean isUpdate = (existing != null);

                if (isUpdate) {
                    existing.setValue(value);
                    existing.setDate(today);
                    dataManager.updateGrade(existing); // notifications + observer gérés dedans
                } else {
                    Grade tmp = new Grade(studentCode, moduleCode, value, type, today);
                    dataManager.addGrade(tmp);         // notifications + observer gérés dedans
                    cachedGrades.add(tmp);
                }
                saved++;

            } catch (NumberFormatException nfe) {
                errors++;
                errorMsg.append("Ligne ").append(i + 1)
                        .append(" : format de note invalide (").append(gradeStr).append(")\n");
            } catch (IllegalArgumentException iae) {
                errors++;
                errorMsg.append("Ligne ").append(i + 1)
                        .append(" : ").append(iae.getMessage()).append("\n");
            } catch (Exception ex) {
                errors++;
                errorMsg.append("Ligne ").append(i + 1)
                        .append(" : Erreur ").append(ex.getMessage()).append("\n");
            }
        }

        loadAllData();

        StringBuilder msg = new StringBuilder();
        msg.append("✓ ").append(saved).append(" note(s) enregistrée(s).\n");
        if (errors > 0) {
            msg.append("\n⚠ ").append(errors).append(" erreur(s) :\n").append(errorMsg);
            showWarning(msg.toString());
        } else {
            showSuccess(msg.toString());
        }

        loadStudentsForGrading();
    }

    private void cancelGrading() {
        DefaultTableModel model = (DefaultTableModel) view.getGradesTable().getModel();
        model.setRowCount(0);
        showInfo("Saisie des notes annulée (aucune modification enregistrée).");
    }

    private Grade findGrade(String studentCode, String moduleCode, String type) {
        if (cachedGrades == null) return null;
        return cachedGrades.stream()
                .filter(g -> g.getStudentCode().equals(studentCode)
                        && g.getModuleCode().equals(moduleCode)
                        && g.getType().equalsIgnoreCase(type))
                .findFirst()
                .orElse(null);
    }

    // ========================================================================
    // ABSENCES - CRUD
    // ========================================================================
    private void setupAbsencesActions() {
        if (view.getRecordAbsenceBtn()      != null)
            view.getRecordAbsenceBtn().addActionListener(e      -> handleRecordAbsence());

        if (view.getViewAbsenceDetailsBtn() != null)
            view.getViewAbsenceDetailsBtn().addActionListener(e -> handleViewAbsenceDetails());

        if (view.getAbsencesTable() != null) {
            view.getAbsencesTable().addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                    if (evt.getClickCount() == 2) {
                        handleEditAbsence();
                    }
                }
            });
        }
    }

    private void handleRecordAbsence() {
        JDialog dialog = createAbsenceDialog("Enregistrer une Absence", null);
        dialog.setVisible(true);
    }

    private void handleViewAbsenceDetails() {
        Absence a = getSelectedAbsenceFromTable();
        if (a == null) return;
        createAbsenceDetailsDialog(a);
    }

    private void handleEditAbsence() {
        Absence a = getSelectedAbsenceFromTable();
        if (a == null) return;
        JDialog dialog = createAbsenceDialog("Modifier l'Absence", a);
        dialog.setVisible(true);
    }

    private Absence getSelectedAbsenceFromTable() {
        ModernTable table = view.getAbsencesTable();
        if (table == null) {
            showError("Table des absences non initialisée.");
            return null;
        }

        int row = table.getSelectedRow();
        if (row == -1) {
            showInfo("Veuillez sélectionner une absence.");
            return null;
        }

        String dateStr     = safeToString(table.getValueAt(row, 0));
        String moduleCode  = safeToString(table.getValueAt(row, 1));
        String studentCode = safeToString(table.getValueAt(row, 2));
        String sessionType = safeToString(table.getValueAt(row, 4));

        if (dateStr.isEmpty() || moduleCode.isEmpty() || studentCode.isEmpty()) {
            showError("Données d'absence invalides.");
            return null;
        }

        LocalDate date;
        try {
            date = LocalDate.parse(dateStr);
        } catch (Exception ex) {
            showError("Format de date invalide (AAAA-MM-JJ requis).");
            return null;
        }

        return cachedAbsences.stream()
                .filter(a -> a.getStudentCode().equals(studentCode)
                        && a.getModuleCode().equals(moduleCode)
                        && a.getDate().equals(date)
                        && a.getSessionType().equals(sessionType))
                .findFirst()
                .orElseGet(() -> {
                    showError("Absence introuvable. Rechargez les données.");
                    return null;
                });
    }

    private JDialog createAbsenceDialog(String title, Absence existingAbsence) {
        JDialog dialog = new JDialog(view, title, true);
        dialog.setLayout(new GridBagLayout());
        dialog.setSize(550, 500);
        dialog.setLocationRelativeTo(view);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 15, 10, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JLabel lblTitle = new JLabel(title, JLabel.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        dialog.add(lblTitle, gbc);
        gbc.gridwidth = 1;

        gbc.gridx = 0; gbc.gridy = 1;
        dialog.add(new JLabel("Module :"), gbc);
        JComboBox<String> moduleCombo = new JComboBox<>(
                cachedModules.stream()
                        .map(m -> m.getCode() + " - " + m.getName())
                        .toArray(String[]::new)
        );
        gbc.gridx = 1;
        dialog.add(moduleCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        dialog.add(new JLabel("Étudiant :"), gbc);
        JComboBox<String> studentCombo = new JComboBox<>(
                cachedStudents.stream()
                        .map(s -> s.getCode() + " - " + s.getFullName())
                        .toArray(String[]::new)
        );
        gbc.gridx = 1;
        dialog.add(studentCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        dialog.add(new JLabel("Date (AAAA-MM-JJ) :"), gbc);
        JTextField dateField = new JTextField(20);
        if (existingAbsence != null) {
            dateField.setText(existingAbsence.getDate().toString());
            dateField.setEnabled(false);
        } else {
            dateField.setText(LocalDate.now().toString());
        }
        gbc.gridx = 1;
        dialog.add(dateField, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        dialog.add(new JLabel("Type de session :"), gbc);
        JComboBox<String> typeCombo = new JComboBox<>(new String[]{
                Absence.SESSION_COURSE, Absence.SESSION_TD, Absence.SESSION_TP
        });
        if (existingAbsence != null) {
            typeCombo.setSelectedItem(existingAbsence.getSessionType());
        }
        gbc.gridx = 1;
        dialog.add(typeCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 5;
        dialog.add(new JLabel("Justifiée :"), gbc);
        JCheckBox justifiedCheck = new JCheckBox();
        if (existingAbsence != null) justifiedCheck.setSelected(existingAbsence.isJustified());
        gbc.gridx = 1;
        dialog.add(justifiedCheck, gbc);

        gbc.gridx = 0; gbc.gridy = 6;
        dialog.add(new JLabel("Raison / Observation :"), gbc);
        JTextArea reasonArea = new JTextArea(4, 20);
        reasonArea.setLineWrap(true);
        reasonArea.setWrapStyleWord(true);
        if (existingAbsence != null && existingAbsence.getReason() != null) {
            reasonArea.setText(existingAbsence.getReason());
        }
        JScrollPane scroll = new JScrollPane(reasonArea);
        gbc.gridx = 1;
        dialog.add(scroll, gbc);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        JButton cancelBtn = new JButton("Annuler");
        JButton saveBtn   = new JButton(existingAbsence != null ? "Modifier" : "Enregistrer");

        cancelBtn.addActionListener(e -> dialog.dispose());

        saveBtn.addActionListener(e -> {
            String moduleCode  = getSelectedCodeFromCombo(moduleCombo);
            String studentCode = getSelectedCodeFromCombo(studentCombo);
            String dateStr     = safeToString(dateField.getText());
            String sessionType = (String) typeCombo.getSelectedItem();
            boolean justified  = justifiedCheck.isSelected();
            String reason      = safeToString(reasonArea.getText());

            if (moduleCode.isEmpty() || studentCode.isEmpty()) {
                showError("Module et étudiant sont obligatoires.");
                return;
            }

            if (findModuleByCode(moduleCode) == null) {
                showError("Ce module n'est pas assigné à ce professeur.");
                return;
            }

            LocalDate date;
            try {
                date = LocalDate.parse(dateStr);
            } catch (Exception ex) {
                showError("Date invalide. Format attendu : AAAA-MM-JJ.");
                return;
            }

            try {
                boolean isCreation = (existingAbsence == null);

                if (isCreation) {
                    boolean exists = cachedAbsences.stream().anyMatch(a ->
                            a.getStudentCode().equals(studentCode) &&
                                    a.getModuleCode().equals(moduleCode) &&
                                    a.getDate().equals(date) &&
                                    a.getSessionType().equals(sessionType)
                    );
                    if (exists) {
                        showInfo("Une absence identique existe déjà.");
                        return;
                    }

                    Absence newAbs = new Absence(
                            studentCode, moduleCode, date,
                            justified, reason.isEmpty() ? null : reason,
                            sessionType
                    );
                    dataManager.addAbsence(newAbs); // notification déjà gérée

                } else {
                    existingAbsence.setSessionType(sessionType);
                    existingAbsence.setJustified(justified);
                    existingAbsence.setReason(reason.isEmpty() ? null : reason);
                    dataManager.updateAbsence(existingAbsence);
                }

                loadAllData();
                showSuccess(existingAbsence == null ?
                        "Absence enregistrée avec succès." :
                        "Absence modifiée avec succès."
                );
                dialog.dispose();

            } catch (Exception ex2) {
                ex2.printStackTrace();
                showError("Erreur lors de l'enregistrement de l'absence : " + ex2.getMessage());
            }
        });

        btnPanel.add(cancelBtn);
        btnPanel.add(saveBtn);

        if (existingAbsence != null) {
            JButton deleteBtn = new JButton("Supprimer");
            deleteBtn.addActionListener(e -> {
                int c = JOptionPane.showConfirmDialog(
                        dialog,
                        "Supprimer définitivement cette absence ?",
                        "Confirmation",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                );
                if (c != JOptionPane.YES_OPTION) return;

                try {
                    boolean ok = dataManager.deleteAbsence(existingAbsence);
                    if (!ok) {
                        showError("Suppression échouée.");
                        return;
                    }
                    loadAllData();
                    showSuccess("Absence supprimée avec succès.");
                    dialog.dispose();
                } catch (Exception ex3) {
                    ex3.printStackTrace();
                    showError("Erreur lors de la suppression : " + ex3.getMessage());
                }
            });
            btnPanel.add(deleteBtn);
        }

        gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 2;
        dialog.add(btnPanel, gbc);

        return dialog;
    }

    private void createAbsenceDetailsDialog(Absence absence) {
        JDialog dialog = new JDialog(view, "Détails de l'absence", true);
        dialog.setLayout(new BorderLayout(15, 15));
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(view);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("📅 Détails de l'absence");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        Student student = cachedStudents.stream()
                .filter(s -> s.getCode().equals(absence.getStudentCode()))
                .findFirst()
                .orElse(null);
        Module module = cachedModules.stream()
                .filter(m -> m.getCode().equals(absence.getModuleCode()))
                .findFirst()
                .orElse(null);

        addInfoRow(mainPanel, "Étudiant :", student != null ? student.getFullName() : absence.getStudentCode());
        addInfoRow(mainPanel, "Module :", module != null ? module.getName() : absence.getModuleCode());
        addInfoRow(mainPanel, "Date :", absence.getFormattedDate());
        addInfoRow(mainPanel, "Type de session :", absence.getSessionType());
        addInfoRow(mainPanel, "Statut :", absence.getStatusText());
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        if (absence.getReason() != null && !absence.getReason().isEmpty()) {
            JLabel reasonLabel = new JLabel("Raison :");
            reasonLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
            reasonLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            mainPanel.add(reasonLabel);

            JTextArea reasonArea = new JTextArea(absence.getReason());
            reasonArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            reasonArea.setLineWrap(true);
            reasonArea.setWrapStyleWord(true);
            reasonArea.setEditable(false);
            reasonArea.setBackground(dialog.getBackground());
            reasonArea.setAlignmentX(Component.LEFT_ALIGNMENT);
            mainPanel.add(reasonArea);
        }

        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        addInfoRow(mainPanel, "Ancienneté :", absence.getDaysSince() + " jour(s)");

        dialog.add(mainPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        JButton editBtn = new JButton("Modifier");
        editBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        editBtn.addActionListener(e -> {
            dialog.dispose();
            handleEditAbsence();
        });
        JButton closeBtn = new JButton("Fermer");
        closeBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        closeBtn.addActionListener(e -> dialog.dispose());
        buttonPanel.add(editBtn);
        buttonPanel.add(closeBtn);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        bottomPanel.add(buttonPanel, BorderLayout.CENTER);

        dialog.add(bottomPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    // ========================================================================
    // RAPPORTS & STATISTIQUES (computeStats + buildReportText)
    // ========================================================================
    private void setupReportsActions() {
        if (view.getGenerateReportBtn() != null)
            view.getGenerateReportBtn().addActionListener(e -> generateReportDialog());

        if (view.getExportBtn() != null)
            view.getExportBtn().addActionListener(e -> exportReportToFile());
    }

    private ProfStats computeStats() {
        ProfStats stats = new ProfStats();

        stats.globalAverage = cachedGrades.stream()
                .mapToDouble(Grade::getValue)
                .average()
                .orElse(0.0);

        long passingGrades = cachedGrades.stream()
                .filter(Grade::isPassing)
                .count();
        stats.successRate = cachedGrades.isEmpty() ? 0 :
                (passingGrades * 100.0) / cachedGrades.size();

        int totalAbsences = cachedAbsences.size();

        int plannedSessions = 0;
        for (Module m : cachedModules) {
            long studentsInModule = cachedStudents.stream()
                    .filter(s -> dataManager.isStudentRegistered(s.getCode(), m.getCode()))
                    .count();
            plannedSessions += studentsInModule * 15; // heuristique : 15 séances par module
        }
        stats.presenceRate = plannedSessions > 0
                ? ((plannedSessions - totalAbsences) * 100.0) / plannedSessions
                : 100.0;

        stats.studentsInDifficulty = cachedStudents.stream()
                .filter(s -> {
                    double avg = cachedGrades.stream()
                            .filter(g -> g.getStudentCode().equals(s.getCode()))
                            .mapToDouble(Grade::getValue)
                            .average()
                            .orElse(20.0);
                    return avg < 10.0;
                })
                .count();

        return stats;
    }

    private String buildReportText() {
        int totalStudents = cachedStudents.size();
        int totalModules  = cachedModules.size();

        ProfStats stats = computeStats();

        StringBuilder report = new StringBuilder();
        report.append("RAPPORT STATISTIQUE DU PROFESSEUR\n");
        report.append("Professeur : ").append(professor.getFullName()).append("\n");
        report.append("Code : ").append(professor.getCode()).append("\n");
        report.append("Date : ").append(LocalDate.now().format(
                DateTimeFormatter.ofPattern("dd/MM/yyyy"))).append("\n\n");

        report.append("STATISTIQUES GÉNÉRALES\n");
        report.append("Nombre d'étudiants : ").append(totalStudents).append("\n");
        report.append("Modules enseignés : ").append(totalModules).append("\n");
        report.append("Notes enregistrées : ").append(cachedGrades.size()).append("\n");
        report.append("Absences enregistrées : ").append(cachedAbsences.size()).append("\n\n");

        report.append("PERFORMANCE ACADÉMIQUE\n");
        report.append(String.format("Moyenne générale : %.2f/20\n", stats.globalAverage));
        report.append(String.format("Taux de réussite : %.2f%%\n", stats.successRate));
        report.append(String.format("Taux de présence (approx.) : %.2f%%\n", stats.presenceRate));
        report.append("Étudiants en difficulté (moyenne < 10) : ").append(stats.studentsInDifficulty).append("\n\n");

        report.append("DÉTAILS PAR MODULE\n");
        for (Module m : cachedModules) {
            List<Grade> mGrades = cachedGrades.stream()
                    .filter(g -> g.getModuleCode().equals(m.getCode()))
                    .collect(Collectors.toList());
            if (!mGrades.isEmpty()) {
                double mAvg = mGrades.stream()
                        .mapToDouble(Grade::getValue)
                        .average()
                        .orElse(0.0);
                long mPass = mGrades.stream().filter(Grade::isPassing).count();
                double mRate = mGrades.isEmpty() ? 0 : (mPass * 100.0) / mGrades.size();

                report.append(String.format("- %s (%s) : moyenne %.2f/20, %d notes, %.2f%% de réussite\n",
                        m.getName(), m.getCode(), mAvg, mGrades.size(), mRate));
            } else {
                report.append(String.format("- %s (%s) : aucune note enregistrée\n",
                        m.getName(), m.getCode()));
            }
        }

        return report.toString();
    }

    private void generateReportDialog() {
        String reportText = buildReportText();

        JDialog dialog = new JDialog(view, "Rapport Statistique", true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(600, 700);
        dialog.setLocationRelativeTo(view);

        JTextArea textArea = new JTextArea(reportText);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        textArea.setEditable(false);
        textArea.setMargin(new Insets(15, 15, 15, 15));
        JScrollPane scrollPane = new JScrollPane(textArea);
        dialog.add(scrollPane, BorderLayout.CENTER);

        JButton closeBtn = new JButton("Fermer");
        closeBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        closeBtn.addActionListener(e -> dialog.dispose());
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buttonPanel.add(closeBtn);

        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void exportReportToFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Exporter le rapport du professeur");
        chooser.setSelectedFile(new File("rapport_prof_" + professor.getCode() + "_" + LocalDate.now() + ".txt"));

        int result = chooser.showSaveDialog(view);
        if (result != JFileChooser.APPROVE_OPTION) return;

        File file = chooser.getSelectedFile();
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            writer.print(buildReportText());
            showSuccess("Rapport exporté avec succès : " + file.getName());
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur lors de l'export du rapport : " + e.getMessage());
        }
    }

    // ========================================================================
    // DASHBOARD, TABLES & PROFIL
    // ========================================================================
    private void updateDashboardStats() {
        int totalStudents = cachedStudents != null ? cachedStudents.size() : 0;
        int totalModules  = cachedModules != null ? cachedModules.size()  : 0;
        double avg = (cachedGrades == null || cachedGrades.isEmpty())
                ? 0.0
                : cachedGrades.stream().mapToDouble(Grade::getValue).average().orElse(0.0);

        int toGrade = 0;
        if (cachedModules != null && cachedStudents != null) {
            for (Module m : cachedModules) {
                for (Student s : cachedStudents) {
                    boolean hasExam = cachedGrades.stream().anyMatch(
                            g -> g.getModuleCode().equals(m.getCode())
                                    && g.getStudentCode().equals(s.getCode())
                                    && g.getType().equalsIgnoreCase(Grade.TYPE_EXAM)
                    );
                    if (!hasExam && dataManager.isStudentRegistered(s.getCode(), m.getCode())) {
                        toGrade++;
                    }
                }
            }
        }

        view.updateDashboardCards(totalStudents, totalModules, avg, toGrade);
    }

    private void refreshUnitsTable() {
        ModernTable table = view.getUnitsTable();
        if (table == null) return;

        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0);

        for (Module m : cachedModules) {
            long studentCount = cachedStudents.stream()
                    .filter(s -> dataManager.isStudentRegistered(s.getCode(), m.getCode()))
                    .count();

            double hoursPerWeek = m.getCredits() * 1.5; // heuristique

            model.addRow(new Object[] {
                    m.getCode(),
                    m.getName(),
                    "S" + m.getSemester(),
                    String.valueOf(m.getCredits()),
                    String.valueOf(studentCount),
                    String.format("%.1fh", hoursPerWeek),
                    "Voir"
            });
        }
    }

    private void refreshStudentsTable(List<Student> students) {
        ModernTable table = view.getStudentsTable();
        if (table == null) return;

        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0);

        for (Student s : students) {
            double avg = cachedGrades.stream()
                    .filter(g -> g.getStudentCode().equals(s.getCode()))
                    .mapToDouble(Grade::getValue)
                    .average()
                    .orElse(0.0);

            long absCount = cachedAbsences.stream()
                    .filter(a -> a.getStudentCode().equals(s.getCode()))
                    .count();

            String status;
            if (avg >= 10 && absCount <= 5) status = "✓";
            else if (avg < 10 && absCount > 5) status = "✗";
            else status = "⚠";

            model.addRow(new Object[] {
                    s.getCode(),
                    s.getLastName(),
                    s.getFirstName(),
                    s.getEmail() != null ? s.getEmail() : "",
                    "L" + s.getYear(),
                    String.format("%.2f", avg),
                    String.valueOf(absCount),
                    status
            });
        }
    }

    private void refreshAbsencesTable() {
        ModernTable table = view.getAbsencesTable();
        if (table == null) return;

        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0);

        for (Absence a : cachedAbsences) {
            Student s = cachedStudents.stream()
                    .filter(st -> st.getCode().equals(a.getStudentCode()))
                    .findFirst()
                    .orElse(null);

            String fullName = s != null ? s.getLastName() + " " + s.getFirstName() : a.getStudentCode();

            model.addRow(new Object[]{
                    a.getDate().toString(),
                    a.getModuleCode(),
                    a.getStudentCode(),
                    fullName,
                    a.getSessionType(),
                    a.isJustified() ? "Oui" : "Non",
                    a.getReason() != null ? a.getReason() : ""
            });
        }
    }

    private void createProfileDialog() {
        JDialog dialog = new JDialog(view, "Mon Profil", true);
        dialog.setLayout(new BorderLayout(15, 15));
        dialog.setSize(550, 550);
        dialog.setLocationRelativeTo(view);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel avatarLabel = new JLabel("👨‍🏫", JLabel.CENTER);
        avatarLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 80));
        avatarLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(avatarLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        JLabel nameLabel = new JLabel(professor.getFullName(), JLabel.CENTER);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(nameLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 5)));

        JLabel codeLabel = new JLabel("Code : " + professor.getCode(), JLabel.CENTER);
        codeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        codeLabel.setForeground(Color.GRAY);
        codeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(codeLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 25)));

        addInfoRow(mainPanel, "Département :", professor.getDepartment());
        addInfoRow(mainPanel, "Grade :", professor.getAcademicRank());
        addInfoRow(mainPanel, "Email :",
                professor.getEmail() != null ? professor.getEmail() : "Non renseigné");
        addInfoRow(mainPanel, "Téléphone :",
                professor.getPhoneNumber() != null ? professor.getPhoneNumber() : "Non renseigné");

        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        JLabel statsLabel = new JLabel("📊 Statistiques");
        statsLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        statsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(statsLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        addInfoRow(mainPanel, "Modules enseignés :", String.valueOf(cachedModules.size()));
        addInfoRow(mainPanel, "Étudiants :", String.valueOf(cachedStudents.size()));
        addInfoRow(mainPanel, "Notes enregistrées :", String.valueOf(cachedGrades.size()));
        addInfoRow(mainPanel, "Absences enregistrées :", String.valueOf(cachedAbsences.size()));
        addInfoRow(mainPanel, "Total crédits enseignés :", String.valueOf(professor.getTotalCredits()));
        addInfoRow(mainPanel, "Codes modules :", String.join(", ", professor.getModuleCodes()));

        double globalAvg = cachedGrades.isEmpty()
                ? 0.0
                : cachedGrades.stream().mapToDouble(Grade::getValue).average().orElse(0.0);
        addInfoRow(mainPanel, "Moyenne globale des étudiants :", String.format("%.2f/20", globalAvg));

        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        dialog.add(scrollPane, BorderLayout.CENTER);

        JButton closeBtn = new JButton("Fermer");
        closeBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        closeBtn.addActionListener(e -> dialog.dispose());
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buttonPanel.add(closeBtn);

        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    // ========================================================================
    // UTILITAIRES & FEEDBACK
    // ========================================================================
    private String safeToString(Object o) {
        return o == null ? "" : o.toString().trim();
    }

    private String getSelectedCodeFromCombo(JComboBox<String> combo) {
        Object sel = combo.getSelectedItem();
        if (sel == null) return "";
        String s = sel.toString();
        int idx = s.indexOf(" - ");
        return idx == -1 ? s.trim() : s.substring(0, idx).trim();
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(view, msg, "Erreur", JOptionPane.ERROR_MESSAGE);
    }

    private void showWarning(String msg) {
        JOptionPane.showMessageDialog(view, msg, "Attention", JOptionPane.WARNING_MESSAGE);
    }

    private void showInfo(String msg) {
        JOptionPane.showMessageDialog(view, msg, "Information", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showSuccess(String msg) {
        JOptionPane.showMessageDialog(view, msg, "Succès", JOptionPane.INFORMATION_MESSAGE);
    }
}
