package model.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class Professor extends User implements Serializable {
    private static final long serialVersionUID = 1L;
    private String department;
    private String academicRank;
    private List<Module> taughtModules;

    public Professor(String code , String password , String firstName ,String lastName ){
        super(code , password , firstName , lastName);
        this.taughtModules = new ArrayList<>();
        this.department = "Computer Science";
        this.academicRank = "Assistant Professor";
    }

    public Professor(String code , String password , String firstName , String lastName , String department ){
        this(code , password , firstName , lastName);
        this.department = department;
    }

    @Override
    public String getRole(){
        return "Professor";
    }
    public String getDepartment() {
        return department;
    }
    public void setDepartment(String department) {
        this.department = department;
    }
    public String getAcademicRank() {
        return academicRank;
    }
    public void setAcademicRank(String academicRank) {
        this.academicRank = academicRank;
    }
    public List<Module> getTaughtModules() {
        return taughtModules;
    }
    public void addTaughtModule(Module module){
        if(module != null && !taughtModules.contains(module)){
            taughtModules.add(module);
        }
        module.setProfessorCode(this.code);
    }
    public void removeModule(Module module){
        taughtModules.remove(module);
        if(module != null && this.code.equals(module.getProfessorCode())){
            module.setProfessorCode(null);
        }
    }

    public boolean teachesModule(String moduleCode){
        for(Module module : taughtModules){
            if(module.getCode().equals(moduleCode)){
                return true;
            }
        }
        return false;
    }
    public int getTotalCredits(){
        int total =0;
        for(Module module  : taughtModules){
            total += module.getCredits();
        }
        return total;
    }
    public List<String> getModuleCodes(){
        List<String> moduleCodes = new ArrayList<>();
        for(Module module : taughtModules){
            moduleCodes.add(module.getCode());
        }
        return moduleCodes;
    }
    public int getModuleCount() {
        return taughtModules.size();
    }

    @Override
    public String toString(){
        return "Professor{" +
               "code='" + code + '\'' +
               ", name ='" + getFullName() + '\'' +
               ", department='" + department + '\'' +
               ", Rank='" + academicRank + '\'' +
               ", Modules=" + getModuleCount() +
               '}';
    }
    
}
