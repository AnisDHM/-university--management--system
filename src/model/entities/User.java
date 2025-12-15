package model.entities;

import java.io.Serializable;

public abstract  class User implements Serializable {
    
    private static final long serialVersionUID = 1L;

    protected String code ;
    protected String password;
    protected String firstName;
    protected String lastName;
    protected String email;
    protected String phoneNumber;
    
    /**
     * Constructeur de la classe User
     * 
     * @param code Le code d'identification unique
     * @param password Le mot de passe
     * @param firstName Le prénom
     * @param lastName Le nom de famille
     */
    public User (String code , String password , String firstName ,String lastName){
        this.code = code ;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
    }
     public String getCode() {
        return code;
     }
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public abstract String getRole();

    @Override

    public String toString(){
        return "User{" +
               "code='" + code + '\'' +
               ", firstName='" + firstName + '\'' +
               ", lastName='" + lastName + '\'' +
               ", email='" + email + '\'' +
               ", phoneNumber='" + phoneNumber + '\'' +
               '}';
    }

    @Override

    public boolean equals(Object obj){
        if(this == obj) return true;
        if ( obj == null || getClass() != obj.getClass()) return false;
        User user = (User) obj;
        return code.equals(user.code);    }

    @Override

    public int hashCode(){
        return code.hashCode();
    }
}
