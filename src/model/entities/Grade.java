package model.entities;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Grade implements Serializable{
    private static final long serialVersionUID = 1L;
    private String studentCode;
    private String moduleCode;
    private double value;
    private LocalDate date;
    private String type;
    public double coefficient;
    
    public static final String TYPE_EXAM = "Examen";
    public static final String TYPE_CC = "CC";
    public static final String TYPE_TP = "TP";
    public static final String TYPE_TD = "TD";

    public static final double MIN_GRADE = 0.0;
    public static final double MAX_GRADE = 20.0;
    
    public Grade(String studentCode , String moduleCode , double value , String type , LocalDate date ){
        this.studentCode = studentCode;
        this.moduleCode = moduleCode;
        this.value = value;
        this.type = type;
        this.date = date;
        this.coefficient = 1.0;
    }

    public Grade(String studentCode , String moduleCode , double value , String type , LocalDate date , double coefficient  ){
        this(studentCode , moduleCode , value , type , date);
        this.coefficient = coefficient;
    }

    private double validateGrade(double grade){
        if(grade < MIN_GRADE || grade > MAX_GRADE){
            throw new  IllegalArgumentException(
                "Grade must be between " + MIN_GRADE + " and " + MAX_GRADE
            );
        }
        return grade;
    }
    public String getStudentCode(){
        return studentCode;
    }

    public String getModuleCode() {
        return moduleCode;
    }

    public double getValue() {
        return value;
    }
    
    public void setValue(double value) {
        this.value = validateGrade(value);
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
    
    public double getCoefficient(){
        return coefficient;
    }

    public void setCoefficient(double coefficient){
        if(coefficient > 0){
            this.coefficient = coefficient;
        } else {
            throw new IllegalArgumentException("Coefficient must be positive.");
        }
    }

    public boolean isPassing(){
        return value >= 10.0;
    }
    
    public String getFormattedGrade() {
        return String.format("%.2f/20", value);
    }

    public String getFormattedDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return date.format(formatter);
    }

    public String getMention() {
        if (value >= 16) return "Excellent";
        if (value >= 14) return "Très Bien";
        if (value >= 12) return "Bien";
        if (value >= 10) return "Passable";
        if (value >= 5) return "Insuffisant";
        return "Éliminatoire";
    }

    public double getWeightedValue() {
        return value * coefficient;
    }

    @Override
    public String toString() {
        return "Grade{" +
                "student='" + studentCode + '\'' +
                ", module='" + moduleCode + '\'' +
                ", value=" + getFormattedGrade() +
                ", type='" + type + '\'' +
                ", date=" + getFormattedDate() +
                ", mention='" + getMention() + '\'' +
                '}';
    }
}
