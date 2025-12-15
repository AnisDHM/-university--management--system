package model.dao;

import model.entities.Notification;
import model.observers.NotificationObserver;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Gestionnaire centralisé des notifications avec pattern Observer.
 * Gère la création, stockage et distribution des notifications.
 */
public class NotificationManager {
    private static NotificationManager instance;
    private static final String NOTIFICATIONS_FILE = "data/notifications.dat";

    private Map<String, List<Notification>> userNotifications;
    private final List<NotificationObserver> observers;

    private NotificationManager() {
        this.userNotifications = new HashMap<>();
        this.observers = new ArrayList<>();
        loadNotifications();
    }

    public static NotificationManager getInstance() {
        if (instance == null) {
            synchronized (NotificationManager.class) {
                if (instance == null) {
                    instance = new NotificationManager();
                }
            }
        }
        return instance;
    }

    // OBSERVERS ===============================================================
    public void addObserver(NotificationObserver observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
        }
    }

    public void removeObserver(NotificationObserver observer) {
        observers.remove(observer);
    }

    private void notifyObservers(Notification notification) {
        for (NotificationObserver observer : observers) {
            observer.onNotificationReceived(notification);
        }
    }

    // GÉNÉRIQUE ===============================================================
    public Notification sendNotification(String recipientCode, String senderCode,
                                         String type, String title, String message,
                                         int priority) {
        Notification notification = new Notification(
                recipientCode, senderCode, type, title, message, priority
        );
        addNotification(notification);
        return notification;
    }

    public Notification sendSystemNotification(String recipientCode, String type,
                                               String title, String message) {
        return sendNotification(recipientCode, "SYSTEM", type, title, message,
                Notification.PRIORITY_NORMAL);
    }

    public void addNotification(Notification notification) {
        String recipientCode = notification.getRecipientCode();

        userNotifications.computeIfAbsent(recipientCode, k -> new ArrayList<>());
        userNotifications.get(recipientCode).add(notification);

        System.out.println("[NM] addNotification: stored for " + recipientCode +
                ", total=" + userNotifications.get(recipientCode).size());

        saveNotifications();
        notifyObservers(notification);

        System.out.println("📬 Notification envoyée à " + recipientCode + ": " + notification.getTitle());
    }


    public void sendBulkNotification(List<String> recipientCodes, String type,
                                     String title, String message, int priority) {
        if (recipientCodes == null) return;
        for (String code : recipientCodes) {
            sendNotification(code, "SYSTEM", type, title, message, priority);
        }
    }

    // HELPERS MÉTIERS ========================================================
    public void notifyGradeAdded(String studentCode, String moduleCode,
                                 String moduleName, double grade, String profName) {
        String title = "📝 Nouvelle note disponible";
        String message = String.format(
                "Une nouvelle note a été saisie par %s pour le module %s (%s). Note: %.2f/20",
                profName, moduleName, moduleCode, grade
        );

        Notification notification = sendSystemNotification(
                studentCode, Notification.TYPE_GRADE_ADDED, title, message
        );
        notification.setRelatedEntityId(moduleCode);
        notification.setPriority(Notification.PRIORITY_HIGH);
        saveNotifications();
    }

    public void notifyGradeModified(String studentCode, String moduleCode,
                                    String moduleName, double newGrade) {
        String title = "📝 Note modifiée";
        String message = String.format(
                "Votre note pour le module %s (%s) a été modifiée. Nouvelle note: %.2f/20",
                moduleName, moduleCode, newGrade
        );

        Notification notification = sendSystemNotification(
                studentCode, Notification.TYPE_GRADE_MODIFIED, title, message
        );
        notification.setRelatedEntityId(moduleCode);
        notification.setPriority(Notification.PRIORITY_HIGH);
        saveNotifications();
    }

    public void notifyAbsenceRecorded(String studentCode, String moduleCode,
                                      String moduleName, String date) {
        String title = "📅 Absence enregistrée";
        String message = String.format(
                "Une absence a été enregistrée pour le module %s (%s) le %s",
                moduleName, moduleCode, date
        );

        Notification notification = sendSystemNotification(
                studentCode, Notification.TYPE_ABSENCE_RECORDED, title, message
        );
        notification.setRelatedEntityId(moduleCode);
        notification.setPriority(Notification.PRIORITY_NORMAL);
        saveNotifications();
    }

    public void notifyModuleAssigned(String professorCode, String moduleCode,
            String moduleName, String viceDeanName) {
String title = "📚 Nouveau module assigné";
String message = String.format(
"Le module %s (%s) vous a été assigné par %s",
moduleName, moduleCode, viceDeanName
);

System.out.println("[NM] notifyModuleAssigned: dest=" + professorCode +
" module=" + moduleCode);

Notification notification = sendSystemNotification(
professorCode, Notification.TYPE_MODULE_ASSIGNED, title, message
);
notification.setSenderCode("VICE_DEAN");
notification.setRelatedEntityId(moduleCode);
notification.setPriority(Notification.PRIORITY_HIGH);
saveNotifications();
}


    public void notifyAccountCreated(String userCode, String accountType,
                                     String temporaryPassword) {
        String title = "👤 Compte créé";
        String message = String.format(
                "Votre compte %s a été créé. Mot de passe temporaire: %s. " +
                        "Veuillez le changer lors de votre première connexion.",
                accountType, temporaryPassword
        );

        Notification notification = sendSystemNotification(
                userCode, Notification.TYPE_ACCOUNT_CREATED, title, message
        );
        notification.setPriority(Notification.PRIORITY_URGENT);
        saveNotifications();
    }

    public void notifyAccountModified(String userCode, String modificationType) {
        String title = "👤 Compte modifié";
        String message = String.format(
                "Votre compte a été modifié: %s. Si vous n'êtes pas à l'origine " +
                        "de cette modification, contactez l'administration.",
                modificationType
        );

        Notification notification = sendSystemNotification(
                userCode, Notification.TYPE_ACCOUNT_MODIFIED, title, message
        );
        notification.setPriority(Notification.PRIORITY_HIGH);
        saveNotifications();
    }

    public void notifyInscriptionValidated(String studentCode, List<String> moduleNames) {
        String title = "✓ Inscription validée";
        String modules = String.join(", ", moduleNames);
        String message = String.format(
                "Vos inscriptions ont été validées pour les modules suivants: %s",
                modules
        );

        Notification notification = sendSystemNotification(
                studentCode, Notification.TYPE_INSCRIPTION_VALIDATED, title, message
        );
        notification.setPriority(Notification.PRIORITY_HIGH);
        saveNotifications();
    }

    public void sendSystemAnnouncement(List<String> recipients, String title,
                                       String message, int priority) {
        sendBulkNotification(recipients, Notification.TYPE_SYSTEM_ANNOUNCEMENT,
                title, message, priority);
    }

    // LECTURE ================================================================
    public List<Notification> getUserNotifications(String userCode) {
        List<Notification> notifications = userNotifications.get(userCode);
        if (notifications == null) return new ArrayList<>();
        notifications.sort((n1, n2) -> n2.getTimestamp().compareTo(n1.getTimestamp()));
        return new ArrayList<>(notifications);
    }

    public List<Notification> getUnreadNotifications(String userCode) {
        return getUserNotifications(userCode).stream()
                .filter(n -> !n.isRead())
                .collect(Collectors.toList());
    }

    public int getUnreadCount(String userCode) {
        return getUnreadNotifications(userCode).size();
    }

    public List<Notification> getRecentNotifications(String userCode) {
        return getUserNotifications(userCode).stream()
                .filter(Notification::isRecent)
                .collect(Collectors.toList());
    }

    public List<Notification> getNotificationsByType(String userCode, String type) {
        return getUserNotifications(userCode).stream()
                .filter(n -> n.getType().equals(type))
                .collect(Collectors.toList());
    }

    // GESTION (read/delete) ==================================================
    public void markAsRead(String notificationId) {
        for (List<Notification> notifications : userNotifications.values()) {
            for (Notification notification : notifications) {
                if (notification.getId().equals(notificationId)) {
                    notification.markAsRead();
                    saveNotifications();
                    return;
                }
            }
        }
    }

    public void markAllAsRead(String userCode) {
        List<Notification> notifications = userNotifications.get(userCode);
        if (notifications != null) {
            notifications.forEach(Notification::markAsRead);
            saveNotifications();
        }
    }

    public void deleteNotification(String userCode, String notificationId) {
        List<Notification> notifications = userNotifications.get(userCode);
        if (notifications != null) {
            notifications.removeIf(n -> n.getId().equals(notificationId));
            saveNotifications();
        }
    }

    public void deleteAllNotifications(String userCode) {
        userNotifications.remove(userCode);
        saveNotifications();
    }

    public void cleanOldNotifications() {
        java.time.LocalDateTime thirtyDaysAgo = java.time.LocalDateTime.now().minusDays(30);
        for (List<Notification> notifications : userNotifications.values()) {
            notifications.removeIf(n -> n.getTimestamp().isBefore(thirtyDaysAgo));
        }
        saveNotifications();
    }

    // PERSISTENCE ============================================================
    private void saveNotifications() {
        try {
            new File("data").mkdirs();
            try (ObjectOutputStream oos = new ObjectOutputStream(
                    new FileOutputStream(NOTIFICATIONS_FILE))) {
                oos.writeObject(userNotifications);
            }
        } catch (IOException e) {
            System.err.println("❌ Erreur sauvegarde notifications: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void loadNotifications() {
        File file = new File(NOTIFICATIONS_FILE);
        if (!file.exists()) return;

        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(NOTIFICATIONS_FILE))) {
            userNotifications = (Map<String, List<Notification>>) ois.readObject();
            System.out.println("✅ " + getTotalNotificationsCount() + " notifications chargées");
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("❌ Erreur chargement notifications: " + e.getMessage());
        }
    }

    // STATISTIQUES ===========================================================
    public int getTotalNotificationsCount() {
        return userNotifications.values().stream()
                .mapToInt(List::size)
                .sum();
    }

    public NotificationStats getStats(String userCode) {
        List<Notification> notifications = getUserNotifications(userCode);

        int total  = notifications.size();
        int unread = (int) notifications.stream().filter(n -> !n.isRead()).count();
        int recent = (int) notifications.stream().filter(Notification::isRecent).count();

        return new NotificationStats(total, unread, recent);
    }

    public static class NotificationStats {
        public final int total;
        public final int unread;
        public final int recent;

        public NotificationStats(int total, int unread, int recent) {
            this.total = total;
            this.unread = unread;
            this.recent = recent;
        }

        @Override
        public String toString() {
            return String.format("Total: %d, Non lues: %d, Récentes: %d",
                    total, unread, recent);
        }
    }
}
