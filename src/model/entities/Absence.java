package model.entities;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Absence implements Serializable{
    private static final long serialVersionUID = 1L;
    private String studentCode;
    private String moduleCode;
    private LocalDate date;
    private boolean justified;
    private String reason;
    private String sessionType;
    private LocalDate justificationDate;

    public static final String SESSION_COURSE = "Cours";
    public static final String SESSION_TD = "TD";
    public static final String SESSION_TP = "TP";
    
    public Absence(String studentCode, String moduleCode, LocalDate date) {
        this.studentCode = studentCode;
        this.moduleCode = moduleCode;
        this.date = date;
        this.justified = false; 
        this.sessionType = SESSION_COURSE; 
    }
    public Absence(String studentCode, String moduleCode, LocalDate date, 
                   String sessionType) {
        this(studentCode, moduleCode, date);
        this.sessionType = sessionType;
    }

    public Absence(String studentCode, String moduleCode, LocalDate date,
                   boolean justified, String reason, String sessionType) {
        this(studentCode, moduleCode, date, sessionType);
        this.justified = justified;
        this.reason = reason;
        if (justified) {
            this.justificationDate = LocalDate.now();
        }
    }

    public String getStudentCode() {
        return studentCode;
    }

    public String getModuleCode() {
        return moduleCode;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public boolean isJustified() {
        return justified;
    }

    public void setJustified(boolean justified) {
        this.justified = justified;
        if (justified && justificationDate == null) {
            justificationDate = LocalDate.now();
        }
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getSessionType() {
        return sessionType;
    }

    public void setSessionType(String sessionType) {
        this.sessionType = sessionType;
    }

    public LocalDate getJustificationDate() {
        return justificationDate;
    }
    public void setJustificationDate(LocalDate justificationDate) {
        this.justificationDate = justificationDate;
    }

    public void justify(String reason) {
        this.justified = true;
        this.reason = reason;
        this.justificationDate = LocalDate.now();
    }

    public void unjustify() {
        this.justified = false;
        this.reason = null;
        this.justificationDate = null;
    }

    public String getStatusText() {
        return justified ? "Justifiée" : "Non justifiée";
    }

    public String getFormattedDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return date.format(formatter);
    }

    public boolean isOld() {
        return LocalDate.now().minusDays(30).isAfter(date);
    }

    public boolean isRecent() {
        return LocalDate.now().minusDays(7).isBefore(date);
    }

    public long getDaysSince() {
        return java.time.temporal.ChronoUnit.DAYS.between(date, LocalDate.now());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Absence{");
        sb.append("student='").append(studentCode).append('\'');
        sb.append(", module='").append(moduleCode).append('\'');
        sb.append(", date=").append(getFormattedDate());
        sb.append(", type='").append(sessionType).append('\'');
        sb.append(", status='").append(getStatusText()).append('\'');
        if (justified && reason != null) {
            sb.append(", reason='").append(reason).append('\'');
        }
        sb.append('}');
        return sb.toString();
    }
	public Object getObservation() {
		// TODO Auto-generated method stub
		return null;
	}

}
