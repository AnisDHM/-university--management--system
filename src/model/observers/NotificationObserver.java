// model/observers/NotificationObserver.java
package model.observers;

import model.entities.Notification;

/**
 * Interface Observer pour le pattern Observer des notifications
 * Permet aux vues de réagir en temps réel aux nouvelles notifications
 */
public interface NotificationObserver {
    /**
     * Appelé quand une nouvelle notification est reçue
     * @param notification La nouvelle notification
     */
    void onNotificationReceived(Notification notification);
}