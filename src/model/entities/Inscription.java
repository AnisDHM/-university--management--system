package model.entities;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;


public class Inscription implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    
    private String studentCode;
    private String moduleCode;
    private LocalDate dateInscription;
    private boolean validated;
    private LocalDate dateValidation;
    private String validatedBy;
    private String academicYear;
    private int semester;
    private String remarks;

    public Inscription(String studentCode, String moduleCode) {
        this.studentCode = studentCode;
        this.moduleCode = moduleCode;
        this.dateInscription = LocalDate.now();
        this.validated = false; // Par défaut: en attente
        this.semester = getCurrentSemester();
        this.academicYear = getCurrentAcademicYear();
    }
    
    public Inscription(String studentCode, String moduleCode, int semester) {
        this(studentCode, moduleCode);
        this.semester = semester;
    }
    
    public Inscription(String studentCode, String moduleCode, 
                      LocalDate dateInscription, boolean validated,
                      int semester, String academicYear) {
        this.studentCode = studentCode;
        this.moduleCode = moduleCode;
        this.dateInscription = dateInscription;
        this.validated = validated;
        this.semester = semester;
        this.academicYear = academicYear;
    }
    
    public String getStudentCode() {
        return studentCode;
    }
    
    public String getModuleCode() {
        return moduleCode;
    }
    public LocalDate getDateInscription() {
        return dateInscription;
    }

    public void setDateInscription(LocalDate dateInscription) {
        this.dateInscription = dateInscription;
    }
    
    public boolean isValidated() {
        return validated;
    }
    
    public void setValidated(boolean validated) {
        this.validated = validated;
        if (validated && dateValidation == null) {
            dateValidation = LocalDate.now();
        }
    }

    public LocalDate getDateValidation() {
        return dateValidation;
    }
    

    public void setDateValidation(LocalDate dateValidation) {
        this.dateValidation = dateValidation;
    }
 
    public String getValidatedBy() {
        return validatedBy;
    }

    public void setValidatedBy(String validatedBy) {
        this.validatedBy = validatedBy;
    }

    public String getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(String academicYear) {
        this.academicYear = academicYear;
    }

    public int getSemester() {
        return semester;
    }

    public void setSemester(int semester) {
        if (semester == 1 || semester == 2) {
            this.semester = semester;
        } else {
            throw new IllegalArgumentException("Le semestre doit être 1 ou 2");
        }
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
//======================================================    

    public void validate(String viceDeanCode) {
        this.validated = true;
        this.dateValidation = LocalDate.now();
        this.validatedBy = viceDeanCode;
    }
    

    public void cancelValidation() {
        this.validated = false;
        this.dateValidation = null;
        this.validatedBy = null;
    }

    public String getStatusText() {
        return validated ? "Validée" : "En attente";
    }
    
    public String getFormattedInscriptionDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return dateInscription.format(formatter);
    }

    public String getFormattedValidationDate() {
        if (dateValidation == null) {
            return "Non validée";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return dateValidation.format(formatter);
    }

    public boolean isRecent() {
        return LocalDate.now().minusDays(7).isBefore(dateInscription);
    }
    

    public long getDaysSinceInscription() {
        return java.time.temporal.ChronoUnit.DAYS.between(dateInscription, LocalDate.now());
    }

    public boolean needsAction() {
        return !validated && getDaysSinceInscription() > 7;
    }
    
    // ========================

    private static int getCurrentSemester() {
        int month = LocalDate.now().getMonthValue();
        // Semestre 1: Septembre (9) à Janvier (1)
        // Semestre 2: Février (2) à Juin (6)
        if (month >= 9 || month <= 1) {
            return 1;
        } else {
            return 2;
        }
    }
    

    private static String getCurrentAcademicYear() {
        int year = LocalDate.now().getYear();
        int month = LocalDate.now().getMonthValue();
        
        // Avant septembre = année précédente
        if (month < 9) {
            return (year - 1) + "-" + year;
        } else {
            return year + "-" + (year + 1);
        }
    }
    
   
    @Override
    public String toString() {
        return "Inscription{" +
                "student='" + studentCode + '\'' +
                ", module='" + moduleCode + '\'' +
                ", date=" + getFormattedInscriptionDate() +
                ", status='" + getStatusText() + '\'' +
                ", semester=" + semester +
                ", year='" + academicYear + '\'' +
                '}';
    }
    
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Inscription that = (Inscription) obj;
        return studentCode.equals(that.studentCode) &&
               moduleCode.equals(that.moduleCode) &&
               academicYear.equals(that.academicYear);
    }
    
    
    @Override
    public int hashCode() {
        int result = studentCode.hashCode();
        result = 31 * result + moduleCode.hashCode();
        result = 31 * result + academicYear.hashCode();
        return result;
    }
}

