package model.observers;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe concrète implémentant Subject
 * Notifie les observateurs lors de changements de notes
 */
public class GradeSubject implements Subject {
    private List<Observer> observers;
    
    public GradeSubject() {
        this.observers = new ArrayList<>();
    }
    
    @Override
    public void attach(Observer observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }
    
    @Override
    public void detach(Observer observer) {
        observers.remove(observer);
    }
    
    @Override
    public void notifyObservers(String message, Object data) {
        for (Observer observer : observers) {
            observer.update(message, data);
        }
    }
    
    public void gradeAdded(String studentCode, String moduleCode, double value) {
        String message = "New note added for the module " + moduleCode;
        notifyObservers(message, value);
    }
    
    public void gradeModified(String studentCode, String moduleCode, double newValue) {
        String message = "Note modified for the module" + moduleCode;
        notifyObservers(message, newValue);
    }
}