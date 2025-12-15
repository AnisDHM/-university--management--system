package strategy;
import model.entities.Professor;
import model.dao.DataManager;
import model.entities.User;
public class ProfessorAuthStrategy implements AuthenticationStrategy{
    @Override

    public boolean validateCodeFormat(String code){
        if(code == null || code.isEmpty()){
            return false;
        }

        return code.matches("^2\\d{7}$");
    }

    @Override

    public boolean authenticate(String code , String password){
        if(!validateCodeFormat(code)){
            System.out.println("❌ Format de code invalide pour un professeur");
            return false;
        }
        DataManager dataManager = DataManager.getInstance();
        User user = dataManager.getUser(code);
        if(user == null){
            System.out.println("❌ Aucun utilisateur trouvé avec le code: " + code);
            return false;
        }
        if(user.getPassword().equals(password)){
            System.out.println("✅ Authentification réussie pour le professeur: " + user.getFullName());
            return true;
        }else{
            System.out.println("❌ Mot de passe incorrect" );
            return false;
        }
    }
    @Override
    public User getAuthenticatedUser(String code) {
        DataManager dataManager = DataManager.getInstance();
        return dataManager.getUser(code);
    }
}
