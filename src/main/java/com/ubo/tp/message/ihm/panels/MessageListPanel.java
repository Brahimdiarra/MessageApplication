package main.java.com.ubo.tp.message.ihm.panels;

import main.java.com.ubo.tp.message.core.SessionManager;
import main.java.com.ubo.tp.message.core.database.IDatabaseObserver;
import main.java.com.ubo.tp.message.datamodel.Channel;
import main.java.com.ubo.tp.message.datamodel.Message;
import main.java.com.ubo.tp.message.datamodel.User;
import main.java.com.ubo.tp.message.ihm.MessageApp;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

/**
 * Panel d'affichage des messages.
 *
 * @author BRAHIM
 */
public class MessageListPanel extends JPanel implements IDatabaseObserver {

    private DefaultListModel<Message> messageListModel;
    private JList<Message> messageList;
    private SimpleDateFormat dateFormat;
    private JLabel countLabel;
    private MessageApp messageApp;

    /**
     * Constructeur.
     */
    public MessageListPanel() {
        this.messageApp = null;
        dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        initComponents();
    }

    /**
     * Définit la référence à l'application (pour le filtrage).
     *
     * @param messageApp L'application principale
     */
    public void setMessageApp(MessageApp messageApp) {
        this.messageApp = messageApp;
    }

    /**
     * Initialisation des composants.
     */
    private void initComponents() {
        setLayout(new BorderLayout());
        setBorder(new TitledBorder("Messages"));

        // Modèle de liste
        messageListModel = new DefaultListModel<>();

        // Liste des messages
        messageList = new JList<>(messageListModel);
        messageList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        messageList.setCellRenderer(new MessageListCellRenderer());

        // ScrollPane pour la liste
        JScrollPane scrollPane = new JScrollPane(messageList);
        add(scrollPane, BorderLayout.CENTER);

        // Panel d'information en bas
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        countLabel = new JLabel("0 message(s)");
        infoPanel.add(countLabel);
        add(infoPanel, BorderLayout.SOUTH);

        // Mise à jour du compteur
        messageListModel.addListDataListener(new javax.swing.event.ListDataListener() {
            @Override
            public void intervalAdded(javax.swing.event.ListDataEvent e) {
                updateCount();
            }

            @Override
            public void intervalRemoved(javax.swing.event.ListDataEvent e) {
                updateCount();
            }

            @Override
            public void contentsChanged(javax.swing.event.ListDataEvent e) {
                updateCount();
            }

            private void updateCount() {
                countLabel.setText(messageListModel.getSize() + " message(s)");
            }
        });
    }

    /**
     * Retourne le message sélectionné.
     *
     * @return Le message sélectionné ou null
     */
    public Message getSelectedMessage() {
        return messageList.getSelectedValue();
    }

    /**
     * Efface tous les messages affichés.
     */
    public void clearMessages() {
        messageListModel.clear();
    }

    /**
     * Filtre les messages pour n'afficher que ceux d'un utilisateur spécifique.
     *
     * @param userUuid UUID de l'utilisateur
     */
    public void filterByUser(UUID userUuid) {
        messageListModel.clear();

        if (messageApp != null && messageApp.getmDataManager() != null) {
            Set<Message> allMessages = messageApp.getmDataManager().getMessages();
            User currentUser = SessionManager.getInstance().getCurrentUser();

            for (Message message : allMessages) {
                // Messages de la conversation entre currentUser et userUuid
                if (currentUser != null) {
                    // Messages envoyés PAR currentUser À userUuid
                    boolean sentToUser = message.getSender().getUuid().equals(currentUser.getUuid()) &&
                            message.getRecipient().equals(userUuid);

                    // Messages envoyés PAR userUuid À currentUser
                    boolean receivedFromUser = message.getSender().getUuid().equals(userUuid) &&
                            message.getRecipient().equals(currentUser.getUuid());

                    if (sentToUser || receivedFromUser) {
                        messageListModel.addElement(message);
                    }
                }
            }
        }
    }

    /**
     * Filtre les messages pour n'afficher que ceux d'un canal spécifique.
     *
     * @param channelUuid UUID du canal
     */
    public void filterByChannel(UUID channelUuid) {
        messageListModel.clear();

        if (messageApp != null && messageApp.getmDataManager() != null) {
            Set<Message> allMessages = messageApp.getmDataManager().getMessages();

            for (Message message : allMessages) {
                // Messages envoyés AU canal
                if (message.getRecipient().equals(channelUuid)) {
                    messageListModel.addElement(message);
                }
            }
        }
    }

    /**
     * Affiche tous les messages sans filtre.
     */
    public void showAllMessages() {
        messageListModel.clear();

        if (messageApp != null && messageApp.getmDataManager() != null) {
            Set<Message> allMessages = messageApp.getmDataManager().getMessages();
            for (Message message : allMessages) {
                messageListModel.addElement(message);
            }
        }
    }

    @Override
    public void notifyMessageAdded(Message addedMessage) {
        SwingUtilities.invokeLater(() -> {
            if (!messageListModel.contains(addedMessage)) {
                messageListModel.addElement(addedMessage);
                // Auto-scroll vers le dernier message
                messageList.ensureIndexIsVisible(messageListModel.getSize() - 1);
            }
        });
    }

    @Override
    public void notifyMessageDeleted(Message deletedMessage) {
        SwingUtilities.invokeLater(() -> {
            messageListModel.removeElement(deletedMessage);
        });
    }

    @Override
    public void notifyMessageModified(Message modifiedMessage) {
        SwingUtilities.invokeLater(() -> {
            int index = messageListModel.indexOf(modifiedMessage);
            if (index >= 0) {
                messageListModel.set(index, modifiedMessage);
            }
        });
    }

    @Override
    public void notifyUserAdded(User addedUser) {
        // Non utilisé
    }

    @Override
    public void notifyUserDeleted(User deletedUser) {
        // Non utilisé
    }

    @Override
    public void notifyUserModified(User modifiedUser) {
        // Non utilisé
    }

    @Override
    public void notifyChannelAdded(Channel addedChannel) {
        // Non utilisé
    }

    @Override
    public void notifyChannelDeleted(Channel deletedChannel) {
        // Non utilisé
    }

    @Override
    public void notifyChannelModified(Channel modifiedChannel) {
        // Non utilisé
    }

    /**
     * Renderer personnalisé pour les messages.
     */
    private class MessageListCellRenderer extends JPanel implements ListCellRenderer<Message> {

        private JLabel authorLabel;
        private JTextArea contentArea;
        private JLabel dateLabel;

        public MessageListCellRenderer() {
            setLayout(new BorderLayout(5, 5));
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY),
                    BorderFactory.createEmptyBorder(5, 5, 5, 5)
            ));

            // Auteur
            authorLabel = new JLabel();
            authorLabel.setFont(authorLabel.getFont().deriveFont(Font.BOLD));

            // Contenu
            contentArea = new JTextArea();
            contentArea.setEditable(false);
            contentArea.setLineWrap(true);
            contentArea.setWrapStyleWord(true);
            contentArea.setOpaque(false);

            // Date
            dateLabel = new JLabel();
            dateLabel.setFont(dateLabel.getFont().deriveFont(Font.ITALIC, 10f));
            dateLabel.setForeground(Color.GRAY);

            // Panel du haut (auteur + date)
            JPanel topPanel = new JPanel(new BorderLayout());
            topPanel.setOpaque(false);
            topPanel.add(authorLabel, BorderLayout.WEST);
            topPanel.add(dateLabel, BorderLayout.EAST);

            add(topPanel, BorderLayout.NORTH);
            add(contentArea, BorderLayout.CENTER);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends Message> list, Message message,
                                                      int index, boolean isSelected, boolean cellHasFocus) {
            if (message != null) {
                // Utilisation des bonnes méthodes
                User sender = message.getSender();
                authorLabel.setText("👤 @" + sender.getUserTag() + " (" + sender.getName() + ")");
                contentArea.setText(message.getText());

                // Conversion du timestamp (long) en Date
                Date messageDate = new Date(message.getEmissionDate());
                dateLabel.setText(dateFormat.format(messageDate));

                if (isSelected) {
                    setBackground(list.getSelectionBackground());
                    contentArea.setForeground(list.getSelectionForeground());
                } else {
                    setBackground(list.getBackground());
                    contentArea.setForeground(list.getForeground());
                }
            }

            return this;
        }
    }
}