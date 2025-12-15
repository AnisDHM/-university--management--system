package model.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Student extends User implements  Serializable {

    private static final long serialVersionUID = 1L;

    private String speciality;
    private int year;
    private List<Inscription> inscriptions;
    private List<Grade> grades;
    private List<Absence> absences;

    public Student(String code, String password, String firstName, String lastName) {
        super(code, password, firstName, lastName);

        this.inscriptions = new ArrayList<>();
        this.grades = new ArrayList<>();
        this.absences = new ArrayList<>();

        this.year = 3;
        this.speciality = "IA";
    }

    public Student(String code, String password, String firstName, String lastName, String speciality, int year) {
        this(code, password, firstName, lastName);
        this.speciality = speciality;
        this.year = year;
    }

    @Override
    public String getRole() {
        return "Student";
    }

    public String getSpeciality() {
        return speciality;
    }

    public void setSpeciality(String speciality) {
        this.speciality = speciality;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        if (year >= 1 && year <= 5) {
            this.year = year;
        } else {
            throw new IllegalArgumentException("Year must be between 1 and 5.");
        }
    }

    public List<Inscription> getInscriptions() {
        return inscriptions;
    }

    public void addInscription(Inscription inscription) {
        if (inscription != null && !inscriptions.contains(inscription)) {
            inscriptions.add(inscription);
        }
    }

    public void removeInscription(Inscription inscription) {
        inscriptions.remove(inscription);
    }

    public List<Grade> getGrades() {
        return grades;
    }

    public void addGrade(Grade grade) {
        if (grade != null && !grades.contains(grade)) {
            grades.add(grade);
        }
    }

    public void removeGrade(Grade grade) {
        grades.remove(grade);
    }

    public List<Absence> getAbsences() {
        return absences;
    }

    public void addAbsence(Absence absence) {
        if (absence != null && !absences.contains(absence)) {
            absences.add(absence);
        }
    }

    public void removeAbsence(Absence absence) {
        absences.remove(absence);
    }

    public double calculateAverage() {
        if (grades.isEmpty()) return 0.0;

        double sum = 0.0;
        for (Grade grade : grades) {
            sum += grade.getValue();
        }
        return sum / grades.size();
    }

    public double calculateModuleAverage(String moduleCode) {
        List<Grade> moduleGrades = new ArrayList<>();

        for (Grade grade : grades) {
            if (grade.getModuleCode().equals(moduleCode)) {
                moduleGrades.add(grade);
            }
        }

        if (moduleGrades.isEmpty()) return 0.0;

        double sum = 0.0;
        for (Grade grade : moduleGrades) {
            sum += grade.getValue();
        }

        return sum / moduleGrades.size();
    }

    public int getTotalAbsences() {
        return absences.size();
    }

    public int getUnjustifiedAbsences() {
        int count = 0;
        for (Absence absence : absences) {
            if (!absence.isJustified()) {
                count++;
            }
        }
        return count;
    }

    public boolean isEnrolledIn(String moduleCode) {
        for (Inscription inscription : inscriptions) {
            if (inscription.getModuleCode().equals(moduleCode) && inscription.isValidated()) {
                return true;
            }
        }
        return false;
    }

    public int getValidatedCredits() {
        return 0;
    }

    @Override
    public String toString() {
        return "Student{" +
                "code='" + code + '\'' +
                ", name='" + getFullName() + '\'' +
                ", speciality='" + speciality + '\'' +
                ", year=" + year +
                ", average=" + String.format("%.2f", calculateAverage()) +
                ", absences=" + getTotalAbsences() +
                '}';
    }
}
