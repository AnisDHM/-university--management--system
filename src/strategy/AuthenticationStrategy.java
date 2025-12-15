package strategy;
import model.entities.User;

public interface AuthenticationStrategy {
    boolean authenticate(String code, String password);
    boolean validateCodeFormat(String code);
    User getAuthenticatedUser(String code);
}
