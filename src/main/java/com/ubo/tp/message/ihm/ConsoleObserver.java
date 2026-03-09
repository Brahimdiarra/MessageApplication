package main.java.com.ubo.tp.message.ihm;

import main.java.com.ubo.tp.message.core.database.IDatabaseObserver;
import main.java.com.ubo.tp.message.datamodel.Channel;
import main.java.com.ubo.tp.message.datamodel.Message;
import main.java.com.ubo.tp.message.datamodel.User;



public class ConsoleObserver implements IDatabaseObserver {
    @Override
    public void notifyMessageAdded(Message addedMessage) {
        System.out.println("[DB] MESSAGE AJOUTÉ : " + addedMessage);
    }

    @Override
    public void notifyMessageDeleted(Message deletedMessage) {
        System.out.println("[DB] MESSAGE SUPPRIMÉ : " + deletedMessage);

    }

    @Override
    public void notifyMessageModified(Message modifiedMessage) {
        System.out.println("[DB] MESSAGE Modifier  : " + modifiedMessage);
    }

    @Override
    public void notifyUserAdded(User addedUser) {
        System.out.println("[DB] UTILISATEUR AJOUTÉ : " + addedUser);
    }

    @Override
    public void notifyUserDeleted(User deletedUser) {
        System.out.println("[DB] UTILISATEUR Supprime  : " + deletedUser);
    }

    @Override
    public void notifyUserModified(User modifiedUser) {
        System.out.println("[DB] UTILISATEUR MODIFIER   : " + modifiedUser);
    }

    @Override
    public void notifyChannelAdded(Channel addedChannel) {
        System.out.println("[DB] CANAL AJOUTÉ : " + addedChannel);
    }

    @Override
    public void notifyChannelDeleted(Channel deletedChannel) {
        System.out.println("[DB] CANAL SUPPRIME  : " + deletedChannel);

    }

    @Override
    public void notifyChannelModified(Channel modifiedChannel) {
        System.out.println("[DB] CANAL Modifier   : " + modifiedChannel);
    }
}
