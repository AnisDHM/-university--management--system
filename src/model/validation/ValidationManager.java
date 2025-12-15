package model.validation;

import model.entities.*;
import model.entities.Module;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Gestionnaire de validation centralisé pour toutes les entités
 * Implémente le pattern Strategy pour différents types de validation
 */
public class ValidationManager {
    
    private static ValidationManager instance;
    private Map<Class<?>, List<ValidationRule<?>>> validationRules;
    
    private ValidationManager() {
        validationRules = new HashMap<>();
        initializeValidationRules();
    }
    
    public static ValidationManager getInstance() {
        if (instance == null) {
            instance = new ValidationManager();
        }
        return instance;
    }
    
    /**
     * Interface pour les règles de validation
     */
    public interface ValidationRule<T> {
        ValidationResult validate(T entity);
    }
    
    /**
     * Classe pour le résultat de validation
     */
    public static class ValidationResult {
        private boolean valid;
        private List<String> errors;
        private List<String> warnings;
        
        public ValidationResult() {
            this.valid = true;
            this.errors = new ArrayList<>();
            this.warnings = new ArrayList<>();
        }
        
        public void addError(String error) {
            this.valid = false;
            this.errors.add(error);
        }
        
        public void addWarning(String warning) {
            this.warnings.add(warning);
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public List<String> getErrors() {
            return new ArrayList<>(errors);
        }
        
        public List<String> getWarnings() {
            return new ArrayList<>(warnings);
        }
        
        public String getErrorMessage() {
            return String.join("\n", errors);
        }
        
        public boolean hasWarnings() {
            return !warnings.isEmpty();
        }
    }
    
    /**
     * Initialise toutes les règles de validation
     */
    private void initializeValidationRules() {
        // Règles pour Student
        List<ValidationRule<Student>> studentRules = new ArrayList<>();
        studentRules.add(this::validateStudentCode);
        studentRules.add(this::validateStudentNames);
        studentRules.add(this::validateStudentEmail);
        studentRules.add(this::validateStudentYear);
        validationRules.put(Student.class, new ArrayList<>(studentRules));
        
        // Règles pour Professor
        List<ValidationRule<Professor>> professorRules = new ArrayList<>();
        professorRules.add(this::validateProfessorCode);
        professorRules.add(this::validateProfessorNames);
        professorRules.add(this::validateProfessorEmail);
        validationRules.put(Professor.class, new ArrayList<>(professorRules));
        
        // Règles pour Grade
        List<ValidationRule<Grade>> gradeRules = new ArrayList<>();
        gradeRules.add(this::validateGradeValue);
        gradeRules.add(this::validateGradeDate);
        validationRules.put(Grade.class, new ArrayList<>(gradeRules));
        
        
        List<ValidationRule<Module>> moduleRules = new ArrayList<>();
        moduleRules.add(this::validateModuleCode);
        moduleRules.add(this::validateModuleName);
        moduleRules.add(this::validateModuleCredits);
        validationRules.put(Module.class, new ArrayList<>(moduleRules));
    }
    
    /**
     * Valide une entité selon son type
     */
    @SuppressWarnings("unchecked")
    public <T> ValidationResult validate(T entity) {
        ValidationResult result = new ValidationResult();
        
        if (entity == null) {
            result.addError("L'entité ne peut pas être nulle");
            return result;
        }
        
        List<ValidationRule<?>> rules = validationRules.get(entity.getClass());
        if (rules == null) {
            result.addWarning("Aucune règle de validation définie pour " + entity.getClass().getSimpleName());
            return result;
        }
        
        for (ValidationRule<?> rule : rules) {
            ValidationResult ruleResult = ((ValidationRule<T>) rule).validate(entity);
            if (!ruleResult.isValid()) {
                result.errors.addAll(ruleResult.errors);
                result.valid = false;
            }
            result.warnings.addAll(ruleResult.warnings);
        }
        
        return result;
    }
    
    // ========== RÈGLES DE VALIDATION POUR STUDENT ==========
    
    private ValidationResult validateStudentCode(Student student) {
        ValidationResult result = new ValidationResult();
        String code = student.getCode();
        
        if (code == null || code.isEmpty()) {
            result.addError("Le code étudiant est obligatoire");
        } else if (!code.matches("^1\\d{7}$")) {
            result.addError("Le code étudiant doit commencer par 1 et contenir 8 chiffres");
        }
        
        return result;
    }
    
    private ValidationResult validateStudentNames(Student student) {
        ValidationResult result = new ValidationResult();
        
        if (student.getFirstName() == null || student.getFirstName().trim().isEmpty()) {
            result.addError("Le prénom de l'étudiant est obligatoire");
        } else if (student.getFirstName().length() < 2) {
            result.addError("Le prénom doit contenir au moins 2 caractères");
        }
        
        if (student.getLastName() == null || student.getLastName().trim().isEmpty()) {
            result.addError("Le nom de l'étudiant est obligatoire");
        } else if (student.getLastName().length() < 2) {
            result.addError("Le nom doit contenir au moins 2 caractères");
        }
        
        return result;
    }
    
    private ValidationResult validateStudentEmail(Student student) {
        ValidationResult result = new ValidationResult();
        String email = student.getEmail();
        
        if (email != null && !email.isEmpty()) {
            Pattern emailPattern = Pattern.compile(
                "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
            );
            if (!emailPattern.matcher(email).matches()) {
                result.addError("Format d'email invalide");
            }
            if (!email.endsWith("@usthb.dz")) {
                result.addWarning("L'email devrait se terminer par @usthb.dz");
            }
        }
        
        return result;
    }
    
    private ValidationResult validateStudentYear(Student student) {
        ValidationResult result = new ValidationResult();
        int year = student.getYear();
        
        if (year < 1 || year > 5) {
            result.addError("L'année doit être entre 1 et 5");
        }
        
        return result;
    }
    
    // ========== RÈGLES DE VALIDATION POUR PROFESSOR ==========
    
    private ValidationResult validateProfessorCode(Professor professor) {
        ValidationResult result = new ValidationResult();
        String code = professor.getCode();
        
        if (code == null || code.isEmpty()) {
            result.addError("Le code professeur est obligatoire");
        } else if (!code.matches("^2\\d{7}$")) {
            result.addError("Le code professeur doit commencer par 2 et contenir 8 chiffres");
        }
        
        return result;
    }
    
    private ValidationResult validateProfessorNames(Professor professor) {
        ValidationResult result = new ValidationResult();
        
        if (professor.getFirstName() == null || professor.getFirstName().trim().isEmpty()) {
            result.addError("Le prénom du professeur est obligatoire");
        }
        
        if (professor.getLastName() == null || professor.getLastName().trim().isEmpty()) {
            result.addError("Le nom du professeur est obligatoire");
        }
        
        return result;
    }
    
    private ValidationResult validateProfessorEmail(Professor professor) {
        ValidationResult result = new ValidationResult();
        String email = professor.getEmail();
        
        if (email != null && !email.isEmpty()) {
            Pattern emailPattern = Pattern.compile(
                "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
            );
            if (!emailPattern.matcher(email).matches()) {
                result.addError("Format d'email invalide");
            }
        }
        
        return result;
    }
    
    // ========== RÈGLES DE VALIDATION POUR GRADE ==========
    
    private ValidationResult validateGradeValue(Grade grade) {
        ValidationResult result = new ValidationResult();
        double value = grade.getValue();
        
        if (value < 0 || value > 20) {
            result.addError("La note doit être entre 0 et 20");
        }
        
        if (value < 10) {
            result.addWarning("Note inférieure à la moyenne");
        }
        
        return result;
    }
    
    private ValidationResult validateGradeDate(Grade grade) {
        ValidationResult result = new ValidationResult();
        
        if (grade.getDate() == null) {
            result.addError("La date de la note est obligatoire");
        } else if (grade.getDate().isAfter(java.time.LocalDate.now())) {
            result.addError("La date de la note ne peut pas être dans le futur");
        }
        
        return result;
    }
    
    // ========== RÈGLES DE VALIDATION POUR MODULE ==========
    
    private ValidationResult validateModuleCode(Module module) {
        ValidationResult result = new ValidationResult();
        String code = module.getCode();
        
        if (code == null || code.isEmpty()) {
            result.addError("Le code du module est obligatoire");
        } else if (code.length() < 2 || code.length() > 10) {
            result.addError("Le code du module doit contenir entre 2 et 10 caractères");
        }
        
        return result;
    }
    
    private ValidationResult validateModuleName(Module module) {
        ValidationResult result = new ValidationResult();
        String name = module.getName();
        
        if (name == null || name.trim().isEmpty()) {
            result.addError("Le nom du module est obligatoire");
        } else if (name.length() < 3) {
            result.addError("Le nom du module doit contenir au moins 3 caractères");
        }
        
        return result;
    }
    
    private ValidationResult validateModuleCredits(Module module) {
        ValidationResult result = new ValidationResult();
        int credits = module.getCredits();
        
        if (credits <= 0) {
            result.addError("Le nombre de crédits doit être positif");
        } else if (credits > 10) {
            result.addWarning("Nombre de crédits inhabituellement élevé");
        }
        
        return result;
    }
    
    /**
     * Valide un mot de passe
     */
    public ValidationResult validatePassword(String password) {
        ValidationResult result = new ValidationResult();
        
        if (password == null || password.isEmpty()) {
            result.addError("Le mot de passe est obligatoire");
            return result;
        }
        
        if (password.length() < 8) {
            result.addError("Le mot de passe doit contenir au moins 8 caractères");
        }
        
        if (!password.matches(".*[A-Z].*")) {
            result.addWarning("Le mot de passe devrait contenir au moins une majuscule");
        }
        
        if (!password.matches(".*[a-z].*")) {
            result.addWarning("Le mot de passe devrait contenir au moins une minuscule");
        }
        
        if (!password.matches(".*\\d.*")) {
            result.addWarning("Le mot de passe devrait contenir au moins un chiffre");
        }
        
        return result;
    }
    
    /**
     * Valide qu'un numéro de téléphone est valide
     */
    public ValidationResult validatePhoneNumber(String phoneNumber) {
        ValidationResult result = new ValidationResult();
        
        if (phoneNumber != null && !phoneNumber.isEmpty()) {
            // Format algérien: 05XX XX XX XX ou 06XX XX XX XX ou 07XX XX XX XX
            if (!phoneNumber.matches("^0[567]\\d{8}$") && 
                !phoneNumber.matches("^0[567]\\d{2}\\s?\\d{2}\\s?\\d{2}\\s?\\d{2}$")) {
                result.addError("Format de téléphone invalide (format attendu: 05XX XX XX XX)");
            }
        }
        
        return result;
    }
}