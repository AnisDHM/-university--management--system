package model.entities;
import java.io.Serializable;

public class ViceDean extends User implements Serializable {
    private static final long serialVersionUID = 1L;
    private String department;
    private String title;
    private String appointmentDate;

    public ViceDean(String code , String password , String firstName ,String lastName ){
        super(code , password , firstName , lastName);
        this.department = "General Administration";
        this.title = "Dr.";
        this.appointmentDate = java.time.LocalDate.now().toString();
    }

    public ViceDean(String code , String password , String firstName , String lastName ,String department){
        this(code , password , firstName , lastName);
        this.department = department;
    }

    @Override

    public String getRole(){
        return "Vice Dean";
    }

    public void setDepartment(String department){
        this.department = department;
    }

    public String getTitle(){
        return title;
    }
    public void setTitle(String title){
        this.title = title;
    }

    public String getAppointmentDate(){
        return appointmentDate;
    }

    public String getFullNameWithTitle(){
    return title + " " + getFullName();
    }

    @Override
    public String toString(){
        return "ViceDean{" +
                "code='" + code + '\'' +
                ", name='" + getFullNameWithTitle() + '\'' +
                ", department='" + department + '\'' +
                '}';
    }
}
