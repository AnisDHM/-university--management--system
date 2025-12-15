// model/entities/Notification.java
package model.entities;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Entité représentant une notification dans le système
 */
public class Notification implements Serializable {
    private static final long serialVersionUID = 1L;
    
    // Types de notifications
    public static final String TYPE_GRADE_ADDED = "GRADE_ADDED";
    public static final String TYPE_GRADE_MODIFIED = "GRADE_MODIFIED";
    public static final String TYPE_ABSENCE_RECORDED = "ABSENCE_RECORDED";
    public static final String TYPE_MODULE_ASSIGNED = "MODULE_ASSIGNED";
    public static final String TYPE_ACCOUNT_CREATED = "ACCOUNT_CREATED";
    public static final String TYPE_ACCOUNT_MODIFIED = "ACCOUNT_MODIFIED";
    public static final String TYPE_INSCRIPTION_VALIDATED = "INSCRIPTION_VALIDATED";
    public static final String TYPE_SYSTEM_ANNOUNCEMENT = "SYSTEM_ANNOUNCEMENT";
    public static final String TYPE_PASSWORD_RESET = "PASSWORD_RESET";
    
    // Priorités
    public static final int PRIORITY_LOW = 1;
    public static final int PRIORITY_NORMAL = 2;
    public static final int PRIORITY_HIGH = 3;
    public static final int PRIORITY_URGENT = 4;
    
    private String id;
    private String recipientCode;        // Code de l'utilisateur destinataire
    private String senderCode;           // Code de l'utilisateur émetteur (peut être "SYSTEM")
    private String type;                 // Type de notification
    private String title;                // Titre court
    private String message;              // Message détaillé
    private LocalDateTime timestamp;     // Date/heure de création
    private boolean isRead;              // Statut lu/non lu
    private int priority;                // Priorité (1-4)
    private String actionUrl;            // URL ou action associée (optionnel)
    private String relatedEntityId;      // ID de l'entité liée (ex: code module, matricule)
    private String iconEmoji;            // Emoji pour l'icône
    
    /**
     * Constructeur complet
     */
    public Notification(String recipientCode, String senderCode, String type, 
                       String title, String message, int priority) {
        this.id = UUID.randomUUID().toString();
        this.recipientCode = recipientCode;
        this.senderCode = senderCode;
        this.type = type;
        this.title = title;
        this.message = message;
        this.timestamp = LocalDateTime.now();
        this.isRead = false;
        this.priority = priority;
        this.iconEmoji = getDefaultIconForType(type);
    }
    
    /**
     * Constructeur simplifié
     */
    public Notification(String recipientCode, String type, String title, String message) {
        this(recipientCode, "SYSTEM", type, title, message, PRIORITY_NORMAL);
    }
    
    // Getters et Setters
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getRecipientCode() {
        return recipientCode;
    }
    
    public void setRecipientCode(String recipientCode) {
        this.recipientCode = recipientCode;
    }
    
    public String getSenderCode() {
        return senderCode;
    }
    
    public void setSenderCode(String senderCode) {
        this.senderCode = senderCode;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public boolean isRead() {
        return isRead;
    }
    
    public void setRead(boolean read) {
        isRead = read;
    }
    
    public int getPriority() {
        return priority;
    }
    
    public void setPriority(int priority) {
        this.priority = priority;
    }
    
    public String getActionUrl() {
        return actionUrl;
    }
    
    public void setActionUrl(String actionUrl) {
        this.actionUrl = actionUrl;
    }
    
    public String getRelatedEntityId() {
        return relatedEntityId;
    }
    
    public void setRelatedEntityId(String relatedEntityId) {
        this.relatedEntityId = relatedEntityId;
    }
    
    public String getIconEmoji() {
        return iconEmoji;
    }
    
    public void setIconEmoji(String iconEmoji) {
        this.iconEmoji = iconEmoji;
    }
    
    // Méthodes utilitaires
    
    /**
     * Marque la notification comme lue
     */
    public void markAsRead() {
        this.isRead = true;
    }
    
    /**
     * Obtient l'âge de la notification en format lisible
     */
    public String getTimeAgo() {
        LocalDateTime now = LocalDateTime.now();
        long minutes = java.time.Duration.between(timestamp, now).toMinutes();
        
        if (minutes < 1) {
            return "À l'instant";
        } else if (minutes < 60) {
            return minutes + " min";
        } else if (minutes < 1440) {
            return (minutes / 60) + "h";
        } else {
            return (minutes / 1440) + "j";
        }
    }
    
    /**
     * Obtient la date formatée
     */
    public String getFormattedTimestamp() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return timestamp.format(formatter);
    }
    
    /**
     * Vérifie si la notification est récente (moins de 24h)
     */
    public boolean isRecent() {
        LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);
        return timestamp.isAfter(oneDayAgo);
    }
    
    /**
     * Obtient la couleur associée à la priorité
     */
    public java.awt.Color getPriorityColor() {
        switch (priority) {
            case PRIORITY_URGENT:
                return new java.awt.Color(220, 53, 69);  // Rouge
            case PRIORITY_HIGH:
                return new java.awt.Color(255, 193, 7);  // Orange
            case PRIORITY_NORMAL:
                return new java.awt.Color(23, 162, 184); // Bleu
            case PRIORITY_LOW:
            default:
                return new java.awt.Color(108, 117, 125); // Gris
        }
    }
    
    /**
     * Obtient l'icône par défaut selon le type
     */
    private String getDefaultIconForType(String type) {
        switch (type) {
            case TYPE_GRADE_ADDED:
            case TYPE_GRADE_MODIFIED:
                return "📝";
            case TYPE_ABSENCE_RECORDED:
                return "📅";
            case TYPE_MODULE_ASSIGNED:
                return "📚";
            case TYPE_ACCOUNT_CREATED:
            case TYPE_ACCOUNT_MODIFIED:
                return "👤";
            case TYPE_INSCRIPTION_VALIDATED:
                return "✓";
            case TYPE_SYSTEM_ANNOUNCEMENT:
                return "📢";
            case TYPE_PASSWORD_RESET:
                return "🔒";
            default:
                return "ℹ️";
        }
    }
    
    /**
     * Crée une description courte pour l'aperçu
     */
    public String getShortMessage() {
        if (message.length() <= 80) {
            return message;
        }
        return message.substring(0, 77) + "...";
    }
    
    @Override
    public String toString() {
        return "Notification{" +
                "id='" + id + '\'' +
                ", recipient='" + recipientCode + '\'' +
                ", type='" + type + '\'' +
                ", title='" + title + '\'' +
                ", read=" + isRead +
                ", timestamp=" + getFormattedTimestamp() +
                '}';
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Notification that = (Notification) obj;
        return id.equals(that.id);
    }
    
    @Override
    public int hashCode() {
        return id.hashCode();
    }
}