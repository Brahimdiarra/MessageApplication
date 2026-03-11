package main.java.com.ubo.tp.message.ihm.easteregg;

import main.java.com.ubo.tp.message.core.database.IDatabaseObserver;
import main.java.com.ubo.tp.message.datamodel.Channel;
import main.java.com.ubo.tp.message.datamodel.Message;
import main.java.com.ubo.tp.message.datamodel.User;
import main.java.com.ubo.tp.message.ihm.MessageAppMainView;

import javax.swing.*;

/**
 * Gestionnaire des easter eggs (Séance 6).
 *
 * Détecte les commandes spéciales dans les messages reçus :
 *   /party      → animation confetti
 *   /flip       → l'interface se retourne à 180° puis revient
 *   /earthquake → la fenêtre tremble 2 secondes
 *
 * @author BRAHIM
 */
public class EasterEggManager implements IDatabaseObserver {

    private final MessageAppMainView mainView;

    public EasterEggManager(MessageAppMainView mainView) {
        this.mainView = mainView;
    }

    @Override
    public void notifyMessageAdded(Message message) {
        SwingUtilities.invokeLater(() -> {
            String text = message.getText().trim();
            switch (text) {
                case "/party":     mainView.triggerParty();     break;
                case "/flip":      mainView.triggerFlip();      break;
                case "/earthquake": mainView.triggerEarthquake(); break;
            }
        });
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
