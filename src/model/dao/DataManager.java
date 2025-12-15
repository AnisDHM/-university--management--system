package model.dao;

import model.entities.*;
import model.entities.Module;
import model.observers.GradeSubject;
import model.observers.Subject;
import model.validation.ValidationManager;

import java.io.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * DataManager - Gestion centrale des données applicatives
 * - Singleton
 * - Persistance par sérialisation
 * - Notifications (NotificationManager) CENTRALISÉES
 * - Cache (CacheManager)
 */
public class DataManager {
    private static DataManager instance;

    // Stockage principal
    private Map<String, User>   users;
    private Map<String, Module> modules;
    private List<Grade>         grades;
    private List<Absence>       absences;
    private List<Inscription>   inscriptions;

    // Observer pattern pour les notes
    private Subject gradeSubject;

    // Notifications
    private NotificationManager notificationManager;

    // Cache & validation
    private CacheManager      cacheManager;
    private ValidationManager validationManager;

    // Fichiers de persistance
    private static final String USERS_FILE        = "data/users.dat";
    private static final String MODULES_FILE      = "data/modules.dat";
    private static final String GRADES_FILE       = "data/grades.dat";
    private static final String ABSENCES_FILE     = "data/absences.dat";
    private static final String INSCRIPTIONS_FILE = "data/inscriptions.dat";

    private DataManager() {
        initializeData();
        loadData();
        this.gradeSubject        = new GradeSubject();
        this.notificationManager = NotificationManager.getInstance();
    }

    // =========================================================================
    // SINGLETON
    // =========================================================================
    public static DataManager getInstance() {
        if (instance == null) {
            synchronized (DataManager.class) {
                if (instance == null) {
                    instance = new DataManager();
                }
            }
        }
        return instance;
    }

    // =========================================================================
    // INITIALISATION
    // =========================================================================
    private void initializeData() {
        this.cacheManager      = CacheManager.getInstance();
        this.validationManager = ValidationManager.getInstance();

        users        = new HashMap<>();
        modules      = new HashMap<>();
        grades       = new ArrayList<>();
        absences     = new ArrayList<>();
        inscriptions = new ArrayList<>();

        createSampleData();
    }

    /**
     * Données de démo riches pour Vice-Doyen & Professeurs.
     */
    private void createSampleData() {
        users.clear();
        modules.clear();
        grades.clear();
        absences.clear();
        inscriptions.clear();

        // === PROFESSEURS ===
        Professor prof1 = new Professor("20000001", "password", "Jean", "Petit");
        prof1.setEmail("jean.petit@usthb.dz");
        prof1.setPhoneNumber("0550111111");
        prof1.setDepartment("Informatique");
        prof1.setAcademicRank("Professeur");

        Professor prof2 = new Professor("20000002", "password", "Marie", "Grand");
        prof2.setEmail("marie.grand@usthb.dz");
        prof2.setPhoneNumber("0550111112");
        prof2.setDepartment("Informatique");
        prof2.setAcademicRank("Maître de Conférences A");

        Professor prof3 = new Professor("20000003", "password", "Sofiane", "Benali");
        prof3.setEmail("sofiane.benali@usthb.dz");
        prof3.setPhoneNumber("0550111113");
        prof3.setDepartment("IA");
        prof3.setAcademicRank("Maître Assistant B");

        Professor prof4 = new Professor("20000004", "password", "Imane", "Rahmani");
        prof4.setEmail("imane.rahmani@usthb.dz");
        prof4.setPhoneNumber("0550111114");
        prof4.setDepartment("Réseaux");
        prof4.setAcademicRank("Maître Assistant A");

        // === VICE-DOYEN ===
        ViceDean viceDean = new ViceDean("30000001", "password", "Ahmed", "Benziane");
        viceDean.setEmail("ahmed.benziane@usthb.dz");
        viceDean.setPhoneNumber("0550222222");
        viceDean.setDepartment("Faculté d'Informatique");
        viceDean.setTitle("Prof.");

        users.put(prof1.getCode(), prof1);
        users.put(prof2.getCode(), prof2);
        users.put(prof3.getCode(), prof3);
        users.put(prof4.getCode(), prof4);
        users.put(viceDean.getCode(), viceDean);

        // === ÉTUDIANTS (20) ===
        String[] fnames = {"Alice", "Bob", "Clara", "David", "Emma", "Farid", "Nadia", "Yassine", "Lina", "Karim",
                "Sara", "Rayan", "Meriem", "Oussama", "Noémie", "Selim", "Ines", "Amine", "Leila", "Hakim"};
        String[] lnames = {"Martin", "Durand", "Leroy", "Dubois", "Moreau", "Bernard", "Rousseau", "Petit", "Garcia", "Lopez",
                "Kaci", "Bensalah", "Tahar", "Belkacem", "Yahia", "Saadi", "Mokdad", "Hamdi", "Lounis", "Haddad"};
        int[] years = {1,1,1,2,2,2,3,3,3,3,2,3,1,2,3,1,2,3,2,3};

        for (int i = 0; i < 20; i++) {
            String code = String.format("1%07d", i + 1); // 10000001...
            Student s = new Student(code, "password", fnames[i], lnames[i]);
            s.setEmail((fnames[i] + "." + lnames[i] + "@usthb.dz").toLowerCase());
            s.setPhoneNumber("0550" + String.format("%06d", i + 123));
            s.setSpeciality(i % 2 == 0 ? "Informatique" : "IA");
            s.setYear(years[i]);
            users.put(s.getCode(), s);
        }

        // === MODULES ===
        Module m1 = new Module("GL01", "Génie Logiciel",            5, prof1.getCode(), 1.5, 2, "Conception, UML, tests");
        Module m2 = new Module("BD02", "Base de Données",           4, prof1.getCode(), 1.5, 2, "SQL, modèle relationnel");
        Module m3 = new Module("IA03", "Intelligence Artificielle", 6, prof3.getCode(), 2.0, 2, "Apprentissage supervisé");
        Module m4 = new Module("RS04", "Réseaux",                   4, prof4.getCode(), 1.0, 2, "Réseaux & protocoles");
        Module m5 = new Module("SE05", "Systèmes d'Exploitation",   5, prof2.getCode(), 1.5, 1, "Processus, mémoire, fichiers");
        Module m6 = new Module("AL06", "Algorithmique avancée",     4, prof2.getCode(), 1.5, 1, "Graphes, complexité");
        Module m7 = new Module("IA07", "IA Avancée",                3, prof3.getCode(), 1.0, 1, "Réseaux de neurones");
        Module m8 = new Module("WEB8", "Développement Web",         3, null,            1.0, 2, "HTML/CSS/JS, Spring");

        modules.put(m1.getCode(), m1);
        modules.put(m2.getCode(), m2);
        modules.put(m3.getCode(), m3);
        modules.put(m4.getCode(), m4);
        modules.put(m5.getCode(), m5);
        modules.put(m6.getCode(), m6);
        modules.put(m7.getCode(), m7);
        modules.put(m8.getCode(), m8);

        prof1.addTaughtModule(m1);
        prof1.addTaughtModule(m2);
        prof2.addTaughtModule(m5);
        prof2.addTaughtModule(m6);
        prof3.addTaughtModule(m3);
        prof3.addTaughtModule(m7);
        prof4.addTaughtModule(m4);

        LocalDate now = LocalDate.now();
        Random rand = new Random();
        List<Module> allModules = new ArrayList<>(modules.values());

        // Inscriptions
        for (User u : users.values()) {
            if (!(u instanceof Student s)) continue;

            Collections.shuffle(allModules, rand);
            int count = 3 + rand.nextInt(2);
            for (int i = 0; i < count; i++) {
                Module mm = allModules.get(i);
                Inscription ins = new Inscription(s.getCode(), mm.getCode());
                if (rand.nextDouble() < 0.6) {
                    ins.validate(viceDean.getCode());
                }
                inscriptions.add(ins);
            }
        }

        // Notes
        for (Inscription ins : inscriptions) {
            int n = 1 + rand.nextInt(2);
            for (int k = 0; k < n; k++) {
                double val = 6 + rand.nextDouble() * 10;
                String type = (k == 0) ? Grade.TYPE_EXAM : Grade.TYPE_CC;
                LocalDate d = now.minusDays(rand.nextInt(60));
                grades.add(new Grade(ins.getStudentCode(), ins.getModuleCode(), val, type, d));
            }
        }

        // Absences
        String[] sessionTypes = {Absence.SESSION_COURSE, Absence.SESSION_TD, Absence.SESSION_TP};
        for (Inscription ins : inscriptions) {
            if (rand.nextDouble() < 0.25) {
                int nbAbs = 1 + rand.nextInt(3);
                for (int k = 0; k < nbAbs; k++) {
                    LocalDate d = now.minusDays(5 + rand.nextInt(40));
                    String type = sessionTypes[rand.nextInt(sessionTypes.length)];
                    boolean justified = rand.nextDouble() < 0.4;
                    String reason = justified ? "Certificat médical" : null;
                    absences.add(new Absence(ins.getStudentCode(), ins.getModuleCode(), d, justified, reason, type));
                }
            }
        }
    }

    // =========================================================================
    // UTILISATEURS
    // =========================================================================
    public User getUser(String code) {
        User cachedUser = cacheManager.getCachedUser(code, User.class);
        if (cachedUser != null) return cachedUser;

        User user = users.get(code);
        if (user != null) {
            cacheManager.cacheUser(code, user, 300_000); // 5 min
        }
        return user;
    }

    public boolean addUser(User user) {
        if (user != null && !users.containsKey(user.getCode())) {
            users.put(user.getCode(), user);
            saveUsers();

            notificationManager.notifyAccountCreated(
                    user.getCode(),
                    user.getRole(),
                    "password123"
            );
            return true;
        }
        return false;
    }

    public boolean updateUser(User user) {
        if (user != null && users.containsKey(user.getCode())) {
            users.put(user.getCode(), user);
            saveUsers();

            notificationManager.notifyAccountModified(
                    user.getCode(),
                    "Informations du compte mises à jour"
            );
            cacheManager.invalidateUser(user.getCode());
            return true;
        }
        return false;
    }

    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    public boolean deleteUser(String code) {
        if (code == null || !users.containsKey(code)) {
            return false;
        }

        User user = users.get(code);

        if (user instanceof Student) {
            grades.removeIf(g -> g.getStudentCode().equals(code));
            absences.removeIf(a -> a.getStudentCode().equals(code));
            inscriptions.removeIf(i -> i.getStudentCode().equals(code));
        } else if (user instanceof Professor) {
            modules.values().forEach(m -> {
                if (code.equals(m.getProfessorCode())) {
                    m.setProfessorCode(null);
                }
            });
        }

        users.remove(code);

        saveUsers();
        saveGrades();
        saveAbsences();
        saveInscriptions();
        saveModules();

        cacheManager.invalidateUser(code);
        return true;
    }

    // =========================================================================
    // ÉTUDIANTS
    // =========================================================================
    public List<Grade> getStudentGrades(String studentCode) {
        return grades.stream()
                .filter(grade -> grade.getStudentCode().equals(studentCode))
                .collect(Collectors.toList());
    }

    public List<Absence> getStudentAbsences(String studentCode) {
        return absences.stream()
                .filter(absence -> absence.getStudentCode().equals(studentCode))
                .collect(Collectors.toList());
    }

    public List<Inscription> getStudentInscriptions(String studentCode) {
        return inscriptions.stream()
                .filter(inscription -> inscription.getStudentCode().equals(studentCode))
                .collect(Collectors.toList());
    }

    public List<Module> getAvailableModulesForStudent(String studentCode) {
        Set<String> enrolledModuleCodes = getStudentInscriptions(studentCode).stream()
                .map(Inscription::getModuleCode)
                .collect(Collectors.toSet());

        return modules.values().stream()
                .filter(module -> !enrolledModuleCodes.contains(module.getCode()))
                .collect(Collectors.toList());
    }

    public boolean isStudentRegistered(String studentCode, String moduleCode) {
        return inscriptions.stream()
                .anyMatch(inscription ->
                        inscription.getStudentCode().equals(studentCode)
                                && inscription.getModuleCode().equals(moduleCode)
                );
    }

    public boolean addInscription(Inscription inscription) {
        if (inscription != null && !inscriptions.contains(inscription)) {
            inscriptions.add(inscription);
            saveInscriptions();

            if (inscription.isValidated()) {
                Module module = getModule(inscription.getModuleCode());
                if (module != null) {
                    notificationManager.notifyInscriptionValidated(
                            inscription.getStudentCode(),
                            List.of(module.getName())
                    );
                }
            }
            return true;
        }
        return false;
    }

    // =========================================================================
    // PROFESSEURS
    // =========================================================================
    public List<Module> getProfessorModules(String professorCode) {
        return modules.values().stream()
                .filter(module -> professorCode.equals(module.getProfessorCode()))
                .collect(Collectors.toList());
    }

    public List<Student> getStudentsForProfessor(String professorCode) {
        List<String> profModuleCodes = getProfessorModules(professorCode).stream()
                .map(Module::getCode)
                .collect(Collectors.toList());

        Set<String> studentCodes = inscriptions.stream()
                .filter(ins -> profModuleCodes.contains(ins.getModuleCode()))
                .map(Inscription::getStudentCode)
                .collect(Collectors.toSet());

        return studentCodes.stream()
                .map(this::getUser)
                .filter(u -> u instanceof Student)
                .map(u -> (Student) u)
                .collect(Collectors.toList());
    }

    public List<Professor> getAllProfessors() {
        return users.values().stream()
                .filter(u -> u instanceof Professor)
                .map(u -> (Professor) u)
                .collect(Collectors.toList());
    }

    /** Recherche d'enseignants par code / nom / département. */
    public List<Professor> searchTeachers(String searchTerm) {
        String lower = searchTerm.toLowerCase();
        return getAllProfessors().stream()
                .filter(p ->
                        p.getFirstName().toLowerCase().contains(lower) ||
                        p.getLastName().toLowerCase().contains(lower)  ||
                        p.getCode().contains(searchTerm)               ||
                        (p.getDepartment() != null && p.getDepartment().toLowerCase().contains(lower))
                )
                .collect(Collectors.toList());
    }

    // =========================================================================
    // VICE-DEAN / ADMIN
    // =========================================================================
    public List<Module> getAllModules() {
        return new ArrayList<>(modules.values());
    }

    public List<Student> getAllStudents() {
        return users.values().stream()
                .filter(u -> u instanceof Student)
                .map(u -> (Student) u)
                .collect(Collectors.toList());
    }

    public List<Grade> getAllGrades() {
        return new ArrayList<>(grades);
    }

    public List<Absence> getAllAbsences() {
        return new ArrayList<>(absences);
    }

    public List<Inscription> getAllInscriptions() {
        return new ArrayList<>(inscriptions);
    }

    // =========================================================================
    // MODULES
    // =========================================================================
    public Module getModule(String code) {
        Module cached = cacheManager.getCachedModule(code, Module.class);
        if (cached != null) return cached;

        Module module = modules.get(code);
        if (module != null) {
            cacheManager.cacheModule(code, module, 300_000);
        }
        return module;
    }

    public boolean addModule(Module module) {
        if (module != null && !modules.containsKey(module.getCode())) {
            modules.put(module.getCode(), module);
            saveModules();
            cacheManager.invalidateModule(module.getCode());

            if (module.hasProfessor()) {
                notificationManager.notifyModuleAssigned(
                        module.getProfessorCode(),
                        module.getCode(),
                        module.getName(),
                        "Vice-Doyen"
                );
            }
            return true;
        }
        return false;
    }

    /** Mise à jour IN-PLACE d'un module + notification d'affectation. */
    public boolean updateModule(Module updatedModule) {
        if (updatedModule == null) {
            System.out.println("[DM] updateModule: updatedModule null");
            return false;
        }

        String code = updatedModule.getCode();
        Module existing = modules.get(code);
        if (existing == null) {
            System.out.println("[DM] updateModule: module inexistant " + code);
            return false;
        }

        String oldProfCode = existing.getProfessorCode();
        String newProfCode = updatedModule.getProfessorCode();

        System.out.println("[DM] updateModule: " + code +
                " oldProf=" + oldProfCode + " newProf=" + newProfCode);

        existing.setName(updatedModule.getName());
        existing.setCredits(updatedModule.getCredits());
        existing.setCoefficient(updatedModule.getCoefficient());
        existing.setSemester(updatedModule.getSemester());
        existing.setDescription(updatedModule.getDescription());
        existing.setProfessorCode(newProfCode);

        saveModules();
        cacheManager.invalidateModule(code);

        if (newProfCode != null && !newProfCode.isEmpty()) {
            System.out.println("[DM] Envoi notif MODULE_ASSIGNED à " + newProfCode);
            notificationManager.notifyModuleAssigned(
                    newProfCode,
                    existing.getCode(),
                    existing.getName(),
                    "Vice-Doyen"
            );
        } else {
            System.out.println("[DM] Pas de prof affecté -> pas de notification.");
        }

        return true;
    }

    public boolean deleteModule(String code) {
        if (!modules.containsKey(code)) return false;

        modules.remove(code);

        grades.removeIf(g -> g.getModuleCode().equals(code));
        absences.removeIf(a -> a.getModuleCode().equals(code));
        inscriptions.removeIf(i -> i.getModuleCode().equals(code));

        saveModules();
        saveGrades();
        saveAbsences();
        saveInscriptions();

        cacheManager.invalidateModule(code);
        return true;
    }

    // =========================================================================
    // NOTES
    // =========================================================================
    public boolean addGrade(Grade grade) {
        if (grade == null) return false;

        grades.add(grade);
        saveGrades();

        if (gradeSubject instanceof GradeSubject) {
            ((GradeSubject) gradeSubject).gradeAdded(
                    grade.getStudentCode(),
                    grade.getModuleCode(),
                    grade.getValue()
            );
        }

        Module module = getModule(grade.getModuleCode());
        User profUser = (module != null && module.getProfessorCode() != null)
                ? getUser(module.getProfessorCode())
                : null;
        String profName = profUser != null ? profUser.getFullName() : "Professeur";

        notificationManager.notifyGradeAdded(
                grade.getStudentCode(),
                grade.getModuleCode(),
                module != null ? module.getName() : grade.getModuleCode(),
                grade.getValue(),
                profName
        );

        return true;
    }

    public boolean updateGrade(Grade grade) {
        if (grade == null) return false;

        grades.removeIf(g ->
                g.getStudentCode().equals(grade.getStudentCode()) &&
                        g.getModuleCode().equals(grade.getModuleCode()) &&
                        g.getType().equals(grade.getType())
        );
        grades.add(grade);
        saveGrades();

        if (gradeSubject instanceof GradeSubject) {
            ((GradeSubject) gradeSubject).gradeModified(
                    grade.getStudentCode(),
                    grade.getModuleCode(),
                    grade.getValue()
            );
        }

        Module module = getModule(grade.getModuleCode());
        notificationManager.notifyGradeModified(
                grade.getStudentCode(),
                grade.getModuleCode(),
                module != null ? module.getName() : grade.getModuleCode(),
                grade.getValue()
        );

        return true;
    }

    // =========================================================================
    // ABSENCES
    // =========================================================================
    public boolean addAbsence(Absence absence) {
        if (absence == null) return false;

        absences.add(absence);
        saveAbsences();

        Module module = getModule(absence.getModuleCode());
        notificationManager.notifyAbsenceRecorded(
                absence.getStudentCode(),
                absence.getModuleCode(),
                module != null ? module.getName() : absence.getModuleCode(),
                absence.getFormattedDate()
        );

        return true;
    }

    public boolean updateAbsence(Absence absence) {
        if (absence == null) return false;

        absences.removeIf(a ->
                a.getStudentCode().equals(absence.getStudentCode()) &&
                        a.getModuleCode().equals(absence.getModuleCode()) &&
                        a.getDate().equals(absence.getDate())
        );
        absences.add(absence);
        saveAbsences();
        return true;
    }

    public boolean deleteAbsence(Absence absence) {
        if (absence == null) return false;

        boolean removed = absences.removeIf(a ->
                a.getStudentCode().equals(absence.getStudentCode()) &&
                        a.getModuleCode().equals(absence.getModuleCode()) &&
                        a.getDate().equals(absence.getDate()) &&
                        a.getSessionType().equals(absence.getSessionType())
        );
        if (removed) {
            saveAbsences();
        }
        return removed;
    }

    // =========================================================================
    // OBSERVER & NOTIFICATIONS
    // =========================================================================
    public Subject getGradeSubject() {
        return gradeSubject;
    }

    public NotificationManager getNotificationManager() {
        return notificationManager;
    }

    // =========================================================================
    // PERSISTENCE
    // =========================================================================
    @SuppressWarnings("unchecked")
    private void loadData() {
        try {
            new File("data").mkdirs();
            boolean anyFile = false;

            if (new File(USERS_FILE).exists()) {
                anyFile = true;
                try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(USERS_FILE))) {
                    users = (Map<String, User>) ois.readObject();
                }
            }
            if (new File(MODULES_FILE).exists()) {
                anyFile = true;
                try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(MODULES_FILE))) {
                    modules = (Map<String, Module>) ois.readObject();
                }
            }
            if (new File(GRADES_FILE).exists()) {
                anyFile = true;
                try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(GRADES_FILE))) {
                    grades = (List<Grade>) ois.readObject();
                }
            }
            if (new File(ABSENCES_FILE).exists()) {
                anyFile = true;
                try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(ABSENCES_FILE))) {
                    absences = (List<Absence>) ois.readObject();
                }
            }
            if (new File(INSCRIPTIONS_FILE).exists()) {
                anyFile = true;
                try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(INSCRIPTIONS_FILE))) {
                    inscriptions = (List<Inscription>) ois.readObject();
                }
            }

            if (!anyFile) {
                saveAllData();
            }

        } catch (Exception e) {
            System.err.println("Error loading data: " + e.getMessage());
        }
    }

    private void saveUsers() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(USERS_FILE))) {
            oos.writeObject(users);
        } catch (Exception e) {
            System.err.println("Error saving users: " + e.getMessage());
        }
    }

    private void saveModules() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(MODULES_FILE))) {
            oos.writeObject(modules);
        } catch (Exception e) {
            System.err.println("Error saving modules: " + e.getMessage());
        }
    }

    private void saveGrades() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(GRADES_FILE))) {
            oos.writeObject(grades);
        } catch (Exception e) {
            System.err.println("Error saving grades: " + e.getMessage());
        }
    }

    private void saveAbsences() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(ABSENCES_FILE))) {
            oos.writeObject(absences);
        } catch (Exception e) {
            System.err.println("Error saving absences: " + e.getMessage());
        }
    }

    private void saveInscriptions() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(INSCRIPTIONS_FILE))) {
            oos.writeObject(inscriptions);
        } catch (Exception e) {
            System.err.println("Error saving inscriptions: " + e.getMessage());
        }
    }

    public void saveAllData() {
        saveUsers();
        saveModules();
        saveGrades();
        saveAbsences();
        saveInscriptions();
    }

    // =========================================================================
    // STATISTIQUES & UTILITAIRES
    // =========================================================================
    public int getTotalUsers()        { return users.size(); }
    public int getTotalModules()      { return modules.size(); }
    public int getTotalGrades()       { return grades.size(); }
    public int getTotalAbsences()     { return absences.size(); }
    public int getTotalInscriptions() { return inscriptions.size(); }

    public void clearAllData() {
        createSampleData();
        saveAllData();
    }

    public void cleanup() {
        saveAllData();
        cacheManager.clearAll();
        notificationManager.cleanOldNotifications();
    }

    public void printCacheStats() {
        CacheManager.CacheStats stats = cacheManager.getStats();
        System.out.println(stats);
    }
}
