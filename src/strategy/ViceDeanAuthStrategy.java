package strategy;
import model.entities.User;
import model.entities.ViceDean;
import model.dao.DataManager;

public class ViceDeanAuthStrategy implements AuthenticationStrategy {

    @Override
    public boolean validateCodeFormat(String code){
        if(code == null || code.isEmpty()){
            return false;
        }
        return code.matches("^3\\d{7}$");
    }

    @Override
    public boolean authenticate(String code , String password){
        if(!validateCodeFormat(code)){
            System.out.println("❌ Format de code invalide pour un vice-doyen");
            return false;
        }

        DataManager dataManager = DataManager.getInstance();
        User user = dataManager.getUser(code);

        if(user == null){
            System.out.println("❌ Aucun utilisateur trouvé avec le code: " + code);
            return false;
        }
        if(user.getPassword().equals(password)){
            System.out.println("✅ Authentification réussie pour le vice-doyen: " + user.getFullName());
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
