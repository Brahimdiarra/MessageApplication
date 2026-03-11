package main.java.com.ubo.tp.message.ihm.notification;

import main.java.com.ubo.tp.message.core.SessionManager;
import main.java.com.ubo.tp.message.core.database.IDatabaseObserver;
import main.java.com.ubo.tp.message.datamodel.Channel;
import main.java.com.ubo.tp.message.datamodel.Message;
import main.java.com.ubo.tp.message.datamodel.User;

import javax.swing.*;
import java.awt.*;

/**
 * Gestionnaire de notifications (MSG-010).
 *
 * Affiche une notification flottante quand l'utilisateur connecté reçoit :
 * - Un message direct (DM)
 * - Une mention (@tag) dans un canal
 *
 * @author BRAHIM
 */
public class NotificationManager implements IDatabaseObserver {

    /** Fenêtre parente (pour positionner les toasts). */
    private final Window parentWindow;

    public NotificationManager(Window parentWindow) {
        this.parentWindow = parentWindow;
    }

    @Override
    public void notifyMessageAdded(Message addedMessage) {
        SwingUtilities.invokeLater(() -> checkAndNotify(addedMessage));
    }

    /**
     * Vérifie si le message concerne l'utilisateur connecté et affiche une notification.
     */
    private void checkAndNotify(Message message) {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) return;

        // Ignorer ses propres messages
        if (message.getSender().getUuid().equals(currentUser.getUuid())) return;

        boolean isDM      = message.getRecipient().equals(currentUser.getUuid());
        boolean isMention = message.getText().contains("@" + currentUser.getUserTag());

        if (!isDM && !isMention) return;

        // Construire le contenu de la notification
        String senderName = "@" + message.getSender().getUserTag();
        String title;
        String body;

        if (isDM) {
            title = "💬 Message privé de " + senderName;
            body  = message.getText();
        } else {
            title = "🔔 Mention de " + senderName;
            body  = message.getText();
        }

        ToastNotification.show(parentWindow, title, body);
    }

    // ── Méthodes non utilisées ────────────────────────────────────────────
    @Override public void notifyMessageDeleted(Message m)  {}
    @Override public void notifyMessageModified(Message m) {}
    @Override public void notifyUserAdded(User u)          {}
    @Override public void notifyUserDeleted(User u)        {}
    @Override public void notifyUserModified(User u)       {}
    @Override public void notifyChannelAdded(Channel c)    {}
    @Override public void notifyChannelDeleted(Channel c)  {}
    @Override public void notifyChannelModified(Channel c) {}
}
