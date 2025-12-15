package model.entities;
import java.io.Serializable;

public class Module implements Serializable {
    private static final long serialVersionUID = 1L;
    private String code;
    private String name;
    private int credits;
    private String professorCode;
    private double coefficient;
    private int semester;
    private String description;
   
    public Module (String code, String name , int credits){
        this.code = code;
        this.name = name;
        this.credits = credits;
        this.coefficient = 1.0;
        this.semester = 1;
    }

    public Module(String code , String name , int credits , String professorCode, double coefficient, int semester , String description){
        this.code = code;
        this.name = name;
        this.credits = credits;
        this.professorCode = professorCode;
        this.coefficient = coefficient;
        this.semester = semester;
        this.description = description;
    }

    public String getCode(){
        return code;
    }

    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    public int getCredits(){
        return credits;
    }

    public void setCredits(int credits){
        if(credits > 0){
            this.credits = credits;
        }
        else{
            throw new IllegalArgumentException("Credits must be positive.");
        }
    }

    public String getProfessorCode(){
        return professorCode;
    }

    public void setProfessorCode(String professorCode){
        this.professorCode = professorCode; 
    }

    public double getCoefficient(){
        return coefficient;
    }
    public void setCoefficient (double coefficient){
        if(coefficient > 0){
            this.coefficient = coefficient;
        }
        else{
            throw new IllegalArgumentException("Coefficient must be positive.");
        }
    }
    public int getSemester(){
        return semester;
    }

    public void setSemester(int semester){
        if(semester == 1 || semester ==2){
            this.semester = semester;
        }else{
            throw new IllegalArgumentException("Semester must be 1 or 2.");
        }
    }

    public String getDescription(){
        return description;
    }

    public void setDescription(String description){
        this.description = description;
    }

    public boolean hasProfessor(){
        return professorCode != null && !professorCode.isEmpty();
    }

}
