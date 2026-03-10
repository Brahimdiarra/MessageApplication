package main.java.com.ubo.tp.message.ihm.panels;

import main.java.com.ubo.tp.message.core.DataManager;
import main.java.com.ubo.tp.message.core.SessionManager;
import main.java.com.ubo.tp.message.core.database.IDatabaseObserver;
import main.java.com.ubo.tp.message.datamodel.Channel;
import main.java.com.ubo.tp.message.datamodel.Message;
import main.java.com.ubo.tp.message.datamodel.User;
import main.java.com.ubo.tp.message.ihm.MessageApp;
import main.java.com.ubo.tp.message.ihm.MessageAppMainView;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
    private DataManager dataManager;
    private JTextField searchField;

    /**
     * Liste complète des messages pour le filtre actif (user ou canal sélectionné).
     * On garde cette liste pour pouvoir refilter par texte sans reparcourir toute la DB.
     */
    private final List<Message> currentMessages = new ArrayList<>();

    /** Type de filtre actif : "all", "channel", "user". */
    private String currentFilterType = "all";

    /** UUID du canal ou utilisateur filtré (null si filterType == "all"). */
    private UUID currentFilterUuid = null;

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
        this.dataManager = messageApp != null ? messageApp.getmDataManager() : null;
    }

    /**
     * Initialisation des composants.
     */
    private void initComponents() {
        setLayout(new BorderLayout());
        TitledBorder msgBorder = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(MessageAppMainView.COLOR_ACCENT, 1, true),
                "Messages", TitledBorder.LEFT, TitledBorder.TOP);
        msgBorder.setTitleColor(MessageAppMainView.COLOR_ACCENT);
        msgBorder.setTitleFont(new Font("SansSerif", Font.BOLD, 12));
        setBorder(msgBorder);
        setBackground(MessageAppMainView.COLOR_PANEL_BG);

        // ── HAUT : barre de recherche ─────────────────────────────────────────
        JPanel searchPanel = new JPanel(new BorderLayout(5, 5));
        searchPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 2, 4));
        searchField = new JTextField();
        searchField.setToolTipText("Rechercher dans les messages (texte ou @auteur)");
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { applySearch(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { applySearch(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { applySearch(); }
        });
        searchPanel.add(new JLabel("🔍 "), BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);
        add(searchPanel, BorderLayout.NORTH);

        // Modèle de liste
        messageListModel = new DefaultListModel<>();

        // Liste des messages
        messageList = new JList<>(messageListModel);
        messageList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        messageList.setCellRenderer(new MessageListCellRenderer());

        // ScrollPane pour la liste
        JScrollPane scrollPane = new JScrollPane(messageList);
        add(scrollPane, BorderLayout.CENTER);

        // Panel du bas : compteur + bouton supprimer
        JPanel bottomPanel = new JPanel(new BorderLayout());

        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        countLabel = new JLabel("0 message(s)");
        infoPanel.add(countLabel);

        JButton deleteButton = new JButton("🗑 Supprimer");
        deleteButton.setToolTipText("Supprimer mon message sélectionné (MSG-006)");
        deleteButton.addActionListener(e -> deleteSelectedMessage());

        bottomPanel.add(infoPanel, BorderLayout.WEST);
        bottomPanel.add(deleteButton, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);

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
     * Supprime le message sélectionné si l'utilisateur connecté en est l'auteur (MSG-006).
     */
    private void deleteSelectedMessage() {
        Message selected = getSelectedMessage();

        if (selected == null) {
            JOptionPane.showMessageDialog(this,
                    "Sélectionnez un message à supprimer.",
                    "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null || !selected.getSender().getUuid().equals(currentUser.getUuid())) {
            JOptionPane.showMessageDialog(this,
                    "Vous ne pouvez supprimer que vos propres messages.",
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Supprimer ce message ?",
                "Confirmation", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            dataManager.deleteMessage(selected);
            System.out.println("[MESSAGE] Message supprimé : " + selected.getUuid());
        }
    }

    /**
     * Efface tous les messages affichés.
     */
    public void clearMessages() {
        currentMessages.clear();
        messageListModel.clear();
    }

    /**
     * Refiltre la liste affichée selon le texte dans le champ de recherche.
     * Cherche dans le texte du message ET dans le tag de l'auteur.
     */
    private void applySearch() {
        String query = searchField.getText().trim().toLowerCase();
        messageListModel.clear();
        for (Message m : currentMessages) {
            if (query.isEmpty()
                    || m.getText().toLowerCase().contains(query)
                    || m.getSender().getUserTag().toLowerCase().contains(query)) {
                messageListModel.addElement(m);
            }
        }
    }

    /**
     * Filtre les messages pour n'afficher que ceux d'un utilisateur spécifique.
     *
     * @param userUuid UUID de l'utilisateur
     */
    public void filterByUser(UUID userUuid) {
        currentFilterType = "user";
        currentFilterUuid = userUuid;
        currentMessages.clear();

        if (messageApp != null && messageApp.getmDataManager() != null) {
            Set<Message> allMessages = messageApp.getmDataManager().getMessages();
            User currentUser = SessionManager.getInstance().getCurrentUser();

            for (Message message : allMessages) {
                if (currentUser != null) {
                    boolean sentToUser = message.getSender().getUuid().equals(currentUser.getUuid())
                            && message.getRecipient().equals(userUuid);
                    boolean receivedFromUser = message.getSender().getUuid().equals(userUuid)
                            && message.getRecipient().equals(currentUser.getUuid());

                    if (sentToUser || receivedFromUser) {
                        currentMessages.add(message);
                    }
                }
            }
        }
        applySearch(); // applique aussi le filtre texte en cours
    }

    /**
     * Filtre les messages pour n'afficher que ceux d'un canal spécifique.
     *
     * @param channelUuid UUID du canal
     */
    public void filterByChannel(UUID channelUuid) {
        currentFilterType = "channel";
        currentFilterUuid = channelUuid;
        currentMessages.clear();

        if (messageApp != null && messageApp.getmDataManager() != null) {
            Set<Message> allMessages = messageApp.getmDataManager().getMessages();
            for (Message message : allMessages) {
                if (message.getRecipient().equals(channelUuid)) {
                    currentMessages.add(message);
                }
            }
        }
        applySearch();
    }

    /**
     * Affiche tous les messages sans filtre.
     */
    public void showAllMessages() {
        currentFilterType = "all";
        currentFilterUuid = null;
        currentMessages.clear();

        if (messageApp != null && messageApp.getmDataManager() != null) {
            currentMessages.addAll(messageApp.getmDataManager().getMessages());
        }
        applySearch();
    }

    @Override
    public void notifyMessageAdded(Message addedMessage) {
        SwingUtilities.invokeLater(() -> {
            // Vérifier si le message correspond au filtre actif
            boolean matchesFilter = false;
            if ("all".equals(currentFilterType)) {
                matchesFilter = true;
            } else if ("channel".equals(currentFilterType) && currentFilterUuid != null) {
                matchesFilter = addedMessage.getRecipient().equals(currentFilterUuid);
            } else if ("user".equals(currentFilterType) && currentFilterUuid != null) {
                User currentUser = main.java.com.ubo.tp.message.core.SessionManager.getInstance().getCurrentUser();
                if (currentUser != null) {
                    boolean sentToUser = addedMessage.getSender().getUuid().equals(currentUser.getUuid())
                            && addedMessage.getRecipient().equals(currentFilterUuid);
                    boolean receivedFromUser = addedMessage.getSender().getUuid().equals(currentFilterUuid)
                            && addedMessage.getRecipient().equals(currentUser.getUuid());
                    matchesFilter = sentToUser || receivedFromUser;
                }
            }

            if (!matchesFilter) return;

            // Ajouter aux messages courants si pas déjà présent
            boolean exists = currentMessages.stream()
                    .anyMatch(m -> m.getUuid().equals(addedMessage.getUuid()));
            if (!exists) {
                currentMessages.add(addedMessage);
            }
            // Reappliquer le filtre texte pour décider si on l'affiche
            applySearch();
            // Auto-scroll vers le dernier message visible
            if (messageListModel.getSize() > 0) {
                messageList.ensureIndexIsVisible(messageListModel.getSize() - 1);
            }
        });
    }

    @Override
    public void notifyMessageDeleted(Message deletedMessage) {
        SwingUtilities.invokeLater(() -> {
            currentMessages.removeIf(m -> m.getUuid().equals(deletedMessage.getUuid()));
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
        private JPanel topPanel;

        public MessageListCellRenderer() {
            setLayout(new BorderLayout(5, 5));
            setOpaque(true); // seul ce JPanel est opaque — les enfants héritent de sa couleur
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY),
                    BorderFactory.createEmptyBorder(5, 5, 5, 5)
            ));

            // Auteur
            authorLabel = new JLabel();
            authorLabel.setFont(authorLabel.getFont().deriveFont(Font.BOLD));
            authorLabel.setOpaque(false);

            // Contenu — DOIT rester non-opaque dans un JList renderer
            contentArea = new JTextArea();
            contentArea.setEditable(false);
            contentArea.setLineWrap(true);
            contentArea.setWrapStyleWord(true);
            contentArea.setOpaque(false);

            // Date
            dateLabel = new JLabel();
            dateLabel.setFont(dateLabel.getFont().deriveFont(Font.ITALIC, 10f));
            dateLabel.setOpaque(false);

            // Panel du haut (auteur + date) — transparent, montre la couleur du JPanel parent
            topPanel = new JPanel(new BorderLayout());
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
                User sender = message.getSender();
                authorLabel.setText("👤 @" + sender.getUserTag() + " (" + sender.getName() + ")");
                contentArea.setText(message.getText());

                Date messageDate = new Date(message.getEmissionDate());
                dateLabel.setText(dateFormat.format(messageDate));
            }

            // Couleur de fond : seul le JPanel externe change — les enfants (opaque=false) héritent
            Color bg = isSelected ? list.getSelectionBackground() : list.getBackground();
            Color fg = isSelected ? list.getSelectionForeground() : list.getForeground();

            setBackground(bg);
            authorLabel.setForeground(fg);
            contentArea.setForeground(fg);
            dateLabel.setForeground(isSelected ? fg : Color.GRAY);

            return this;
        }
    }
}