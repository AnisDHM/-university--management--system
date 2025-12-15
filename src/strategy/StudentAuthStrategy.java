package strategy;
import model.entities.User;
import model.dao.DataManager;
import model.entities.Student;

public class StudentAuthStrategy implements AuthenticationStrategy{
    @Override
    public boolean validateCodeFormat(String code) {
        // Vérifier que le code n'est pas null ou vide
        if (code == null || code.isEmpty()) {
            return false;
        }
        return code.matches("^1\\d{7}$");
    }

    @Override
    public boolean authenticate(String code , String password){
        if(!validateCodeFormat(code)){
            System.out.println("❌ Format de code invalide pour un étudiant");
            return false;
        }

        DataManager dataManager = DataManager.getInstance();
        User user = dataManager.getUser(code);

        if(user == null){
            System.out.println("❌ Aucun utilisateur trouvé avec le code: " + code);
            return false;
        }
        if(user.getPassword().equals(password)){
            System.out.println("✅ Authentification réussie pour l'étudiant: " + user.getFullName());
            return true;
        }else{
            System.out.println("❌ Mot de passe incorrect" );
            return false;
        }

    }

    @Override
    public User getAuthenticatedUser(String code){
        DataManager dataManager = DataManager.getInstance();
        return dataManager.getUser(code);
    }

}
