package model.observers;



/**
 * Patron Observateur - Interface Subject
 */
public interface Subject {
    void attach(Observer observer);
    void detach(Observer observer);
    void notifyObservers(String message, Object data);
}