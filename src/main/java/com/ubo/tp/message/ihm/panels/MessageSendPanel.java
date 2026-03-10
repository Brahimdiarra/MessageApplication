package main.java.com.ubo.tp.message.ihm.panels;

import main.java.com.ubo.tp.message.core.DataManager;
import main.java.com.ubo.tp.message.core.SessionManager;
import main.java.com.ubo.tp.message.core.database.IDatabaseObserver;
import main.java.com.ubo.tp.message.datamodel.Channel;
import main.java.com.ubo.tp.message.datamodel.Message;
import main.java.com.ubo.tp.message.datamodel.User;
import main.java.com.ubo.tp.message.ihm.MessageAppMainView;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;
import java.util.UUID;

/**
 * Panel de saisie et d'envoi de messages.
 *
 * @author BRAHIM
 */
public class MessageSendPanel extends JPanel implements IDatabaseObserver {

    private JTextArea messageTextArea;
    private JButton sendButton;
    private JComboBox<RecipientItem> recipientComboBox;
    private DefaultComboBoxModel<RecipientItem> recipientModel;
    private DataManager dataManager;
    private JLabel charCountLabel;

    /**
     * Constructeur.
     *
     * @param dataManager Gestionnaire de données pour envoyer les messages
     */
    public MessageSendPanel(DataManager dataManager) {
        this.dataManager = dataManager;
        initComponents();
        loadRecipients();
    }

    /**
     * Initialisation des composants.
     */
    private void initComponents() {
        setLayout(new BorderLayout(5, 5));
        TitledBorder sendBorder = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(MessageAppMainView.COLOR_ACCENT, 1, true),
                "Envoyer un message", TitledBorder.LEFT, TitledBorder.TOP);
        sendBorder.setTitleColor(MessageAppMainView.COLOR_ACCENT);
        sendBorder.setTitleFont(new Font("SansSerif", Font.BOLD, 12));
        setBorder(sendBorder);
        setBackground(MessageAppMainView.COLOR_PANEL_BG);

        // Panel du haut : sélection du destinataire
        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        topPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JLabel recipientLabel = new JLabel("À : ");
        recipientModel = new DefaultComboBoxModel<>();
        recipientComboBox = new JComboBox<>(recipientModel);
        recipientComboBox.setRenderer(new RecipientCellRenderer());

        topPanel.add(recipientLabel, BorderLayout.WEST);
        topPanel.add(recipientComboBox, BorderLayout.CENTER);

        // Panel central : zone de texte
        messageTextArea = new JTextArea(3, 40);
        messageTextArea.setLineWrap(true);
        messageTextArea.setWrapStyleWord(true);
        messageTextArea.setFont(new Font("SansSerif", Font.PLAIN, 13));
        messageTextArea.setBackground(new Color(248, 250, 252));
        messageTextArea.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        JScrollPane scrollPane = new JScrollPane(messageTextArea);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(4, 6, 4, 6),
                BorderFactory.createLineBorder(new Color(203, 213, 225))
        ));

        // Panel du bas : compteur de caractères
        charCountLabel = new JLabel("0/200");
        charCountLabel.setFont(new Font("SansSerif", Font.ITALIC, 11));
        charCountLabel.setForeground(Color.GRAY);

        // Panel du bas : bouton envoyer (bleu accent)
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        bottomPanel.setBackground(new Color(248, 250, 252));
        bottomPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(203, 213, 225)));
        bottomPanel.add(charCountLabel);
        sendButton = new JButton("  Envoyer  ");
        sendButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        sendButton.setBackground(MessageAppMainView.COLOR_ACCENT);
        sendButton.setForeground(Color.WHITE);
        sendButton.setOpaque(true);
        sendButton.setBorderPainted(false);
        sendButton.setFocusPainted(false);
        sendButton.setPreferredSize(new Dimension(110, 30));
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });
        bottomPanel.add(sendButton);

        // Ajout des panels
        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        // Raccourci clavier : Ctrl+Enter pour envoyer
        messageTextArea.getInputMap().put(KeyStroke.getKeyStroke("control ENTER"), "send");
        messageTextArea.getActionMap().put("send", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        // Listener pour mettre à jour le compteur à chaque frappe
        messageTextArea.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { updateCharCount(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { updateCharCount(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updateCharCount(); }

            private void updateCharCount() {
                int count = messageTextArea.getText().length();
                charCountLabel.setText(count + "/200");
                // Rouge si on dépasse ou approche la limite, gris sinon
                if (count > 200) {
                    charCountLabel.setForeground(Color.RED);
                } else if (count > 180) {
                    charCountLabel.setForeground(new Color(255, 140, 0)); // Orange
                } else {
                    charCountLabel.setForeground(Color.GRAY);
                }
            }
        });
    }

    /**
     * Charge la liste des destinataires (utilisateurs et canaux).
     */
    public void loadRecipients() {
        recipientModel.removeAllElements();

        // Ajouter un placeholder
        recipientModel.addElement(new RecipientItem(null, null, "-- Sélectionner un destinataire --"));

        // Ajouter les utilisateurs
        Set<User> users = dataManager.getUsers();
        User currentUser = SessionManager.getInstance().getCurrentUser();

        for (User user : users) {
            // Ne pas ajouter l'utilisateur connecté dans la liste
            if (currentUser != null && !user.getUuid().equals(currentUser.getUuid())) {
                recipientModel.addElement(new RecipientItem(user, null, null));
            }
        }

        // Ajouter les canaux
        Set<Channel> channels = dataManager.getChannels();
        for (Channel channel : channels) {
            recipientModel.addElement(new RecipientItem(null, channel, null));
        }
    }

    /**
     * Envoie le message au destinataire sélectionné.
     */
    private void sendMessage() {
        // Récupérer le texte du message
        String messageText = messageTextArea.getText().trim();

        if (messageText.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Le message ne peut pas être vide !",
                    "Erreur",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Vérification de la limite de 200 caractères (MSG-008)
        if (messageText.length() > 200) {
            JOptionPane.showMessageDialog(
                    this,
                    "Le message ne peut pas dépasser 200 caractères ! (" + messageText.length() + "/200)",
                    "Erreur",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Récupérer le destinataire sélectionné
        RecipientItem selectedItem = (RecipientItem) recipientComboBox.getSelectedItem();

        if (selectedItem == null || (selectedItem.user == null && selectedItem.channel == null)) {
            JOptionPane.showMessageDialog(
                    this,
                    "Veuillez sélectionner un destinataire !",
                    "Erreur",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Récupérer l'utilisateur connecté
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Erreur : aucun utilisateur connecté !",
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Déterminer l'UUID du destinataire
        UUID recipientUuid;
        String recipientName;

        if (selectedItem.user != null) {
            recipientUuid = selectedItem.user.getUuid();
            recipientName = "@" + selectedItem.user.getUserTag();
        } else {
            recipientUuid = selectedItem.channel.getUuid();
            recipientName = "#" + selectedItem.channel.getName();
        }

        try {
            // Créer le message
            Message message = new Message(currentUser, recipientUuid, messageText);

            // Envoyer via le DataManager
            dataManager.sendMessage(message);

            System.out.println("[SEND] Message envoyé à " + recipientName + " : " + messageText);

            // Effacer le champ de texte
            messageTextArea.setText("");

            // Message de confirmation
            JOptionPane.showMessageDialog(
                    this,
                    "Message envoyé à " + recipientName + " !",
                    "Succès",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            System.err.println("[ERREUR] Échec de l'envoi du message : " + e.getMessage());
            e.printStackTrace();

            JOptionPane.showMessageDialog(
                    this,
                    "Erreur lors de l'envoi du message : " + e.getMessage(),
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Classe interne représentant un élément de la liste des destinataires.
     */
    private static class RecipientItem {
        User user;
        Channel channel;
        String placeholder;

        public RecipientItem(User user, Channel channel, String placeholder) {
            this.user = user;
            this.channel = channel;
            this.placeholder = placeholder;
        }

        @Override
        public String toString() {
            if (placeholder != null) {
                return placeholder;
            }
            if (user != null) {
                return "👤 @" + user.getUserTag() + " (" + user.getName() + ")";
            }
            if (channel != null) {
                return "# " + channel.getName();
            }
            return "";
        }
    }

    // ── IDatabaseObserver : rafraîchit la liste des destinataires à chaque changement ──

    @Override
    public void notifyChannelAdded(Channel addedChannel) {
        SwingUtilities.invokeLater(this::loadRecipients);
    }

    @Override
    public void notifyChannelDeleted(Channel deletedChannel) {
        SwingUtilities.invokeLater(this::loadRecipients);
    }

    @Override
    public void notifyChannelModified(Channel modifiedChannel) {
        SwingUtilities.invokeLater(this::loadRecipients);
    }

    @Override
    public void notifyUserAdded(User addedUser) {
        SwingUtilities.invokeLater(this::loadRecipients);
    }

    @Override
    public void notifyUserDeleted(User deletedUser) {
        SwingUtilities.invokeLater(this::loadRecipients);
    }

    @Override
    public void notifyUserModified(User modifiedUser) {
        SwingUtilities.invokeLater(this::loadRecipients);
    }

    @Override public void notifyMessageAdded(Message m)    { /* Non utilisé */ }
    @Override public void notifyMessageDeleted(Message m)  { /* Non utilisé */ }
    @Override public void notifyMessageModified(Message m) { /* Non utilisé */ }

    /**
     * Renderer personnalisé pour la liste déroulante des destinataires.
     */
    private class RecipientCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (value instanceof RecipientItem) {
                RecipientItem item = (RecipientItem) value;

                if (item.placeholder != null) {
                    setForeground(Color.GRAY);
                    setFont(getFont().deriveFont(Font.ITALIC));
                } else {
                    if (item.user != null) {
                        setForeground(new Color(0, 102, 204)); // Bleu pour users
                    } else if (item.channel != null) {
                        setForeground(new Color(34, 139, 34)); // Vert pour channels
                    }
                }
            }

            return this;
        }
    }
}