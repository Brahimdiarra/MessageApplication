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
import java.awt.geom.Rectangle2D;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Panel de saisie et d'envoi de messages.
 *
 * @author BRAHIM
 */
public class MessageSendPanel extends JPanel implements IDatabaseObserver {
    private static final long serialVersionUID = 1L;

    private JTextArea messageTextArea;
    private JButton sendButton;
    private JComboBox<RecipientItem> recipientComboBox;
    private DefaultComboBoxModel<RecipientItem> recipientModel;
    private DataManager dataManager;
    private JLabel charCountLabel;
    private JButton emojiPickerBtn;
    private JPopupMenu autocompletePopup = new JPopupMenu();
    private int autocompleteColonPos = -1;

    /** Table de correspondance raccourci → emoji. */
    private static final Map<String, String> EMOJI_MAP = new LinkedHashMap<>();
    static {
        EMOJI_MAP.put(":smile:", "\uD83D\uDE0A");
        EMOJI_MAP.put(":smirk:", "\uD83D\uDE0F");
        EMOJI_MAP.put(":sad:", "\uD83D\uDE22");
        EMOJI_MAP.put(":heart:", "\u2764\uFE0F");
        EMOJI_MAP.put(":fire:", "\uD83D\uDD25");
        EMOJI_MAP.put(":thumbsup:", "\uD83D\uDC4D");
        EMOJI_MAP.put(":thumbsdown:", "\uD83D\uDC4E");
        EMOJI_MAP.put(":laugh:", "\uD83D\uDE02");
        EMOJI_MAP.put(":wink:", "\uD83D\uDE09");
        EMOJI_MAP.put(":wow:", "\uD83D\uDE2E");
        EMOJI_MAP.put(":party:", "\uD83C\uDF89");
        EMOJI_MAP.put(":eyes:", "\uD83D\uDC40");
        EMOJI_MAP.put(":star:", "\u2B50");
        EMOJI_MAP.put(":check:", "\u2705");
        EMOJI_MAP.put(":wave:", "\uD83D\uDC4B");
        EMOJI_MAP.put(":clap:", "\uD83D\uDC4F");
        EMOJI_MAP.put(":100:", "\uD83D\uDCAF");
        EMOJI_MAP.put(":rocket:", "\uD83D\uDE80");
    }
    /** Couleur de fond pour chaque emoji dans le sélecteur (compatible Java 8). */
    private static final Map<String, Color> EMOJI_COLORS = new LinkedHashMap<>();
    static {
        EMOJI_COLORS.put(":smile:", new Color(255, 230, 100));
        EMOJI_COLORS.put(":smirk:", new Color(255, 200, 80));
        EMOJI_COLORS.put(":sad:", new Color(147, 197, 253));
        EMOJI_COLORS.put(":heart:", new Color(252, 165, 165));
        EMOJI_COLORS.put(":fire:", new Color(253, 186, 74));
        EMOJI_COLORS.put(":thumbsup:", new Color(94, 234, 212));
        EMOJI_COLORS.put(":thumbsdown:", new Color(196, 181, 253));
        EMOJI_COLORS.put(":laugh:", new Color(254, 240, 138));
        EMOJI_COLORS.put(":wink:", new Color(253, 224, 132));
        EMOJI_COLORS.put(":wow:", new Color(134, 239, 172));
        EMOJI_COLORS.put(":party:", new Color(216, 180, 254));
        EMOJI_COLORS.put(":eyes:", new Color(186, 230, 253));
        EMOJI_COLORS.put(":star:", new Color(253, 224, 71));
        EMOJI_COLORS.put(":check:", new Color(134, 239, 172));
        EMOJI_COLORS.put(":wave:", new Color(147, 197, 253));
        EMOJI_COLORS.put(":clap:", new Color(253, 186, 74));
        EMOJI_COLORS.put(":100:", new Color(252, 165, 165));
        EMOJI_COLORS.put(":rocket:", new Color(147, 197, 253));
    }

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
                BorderFactory.createLineBorder(new Color(203, 213, 225))));

        // Panel du bas : compteur de caractères
        charCountLabel = new JLabel("0/200");
        charCountLabel.setFont(new Font("SansSerif", Font.ITALIC, 11));
        charCountLabel.setForeground(Color.GRAY);

        // Panel du bas : bouton envoyer (bleu accent)
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        bottomPanel.setBackground(new Color(248, 250, 252));
        bottomPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(203, 213, 225)));
        emojiPickerBtn = new JButton("\uD83D\uDE0A");
        emojiPickerBtn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        emojiPickerBtn.setMargin(new java.awt.Insets(2, 4, 2, 4));
        emojiPickerBtn.setToolTipText("Insérer un emoji (ou tapez :raccourci:)");
        emojiPickerBtn.setBorderPainted(false);
        emojiPickerBtn.setFocusPainted(false);
        emojiPickerBtn.addActionListener(e2 -> showEmojiPicker());
        bottomPanel.add(emojiPickerBtn);
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
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                updateCharCount();
                SwingUtilities.invokeLater(MessageSendPanel.this::checkAutocomplete);
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                updateCharCount();
                SwingUtilities.invokeLater(MessageSendPanel.this::hideAutocomplete);
            }

            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                updateCharCount();
            }

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

        // Remplacer les raccourcis :emoji: par le vrai emoji
        messageText = applyEmojiShortcuts(messageText);

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

    // ── IDatabaseObserver : rafraîchit la liste des destinataires à chaque
    // changement ──

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

    @Override
    public void notifyMessageAdded(Message m) {
        /* Non utilisé */ }

    @Override
    public void notifyMessageDeleted(Message m) {
        /* Non utilisé */ }

    @Override
    public void notifyMessageModified(Message m) {
        /* Non utilisé */ }

    /**
     * Renderer personnalisé pour la liste déroulante des destinataires.
     */
    private class RecipientCellRenderer extends DefaultListCellRenderer {
        private static final long serialVersionUID = 1L;

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

    // ── Emoji autocomplete ───────────────────────────────────────────────────

    /** Remplace tous les raccourcis :shortcut: par leur emoji correspondant. */
    private String applyEmojiShortcuts(String text) {
        for (Map.Entry<String, String> entry : EMOJI_MAP.entrySet()) {
            text = text.replace(entry.getKey(), entry.getValue());
        }
        return text;
    }

    /**
     * Vérifie le texte courant et affiche le popup d'autocomplétion si pertinent.
     */
    private void checkAutocomplete() {
        String text = messageTextArea.getText();
        int caretPos = messageTextArea.getCaretPosition();
        if (caretPos == 0) {
            hideAutocomplete();
            return;
        }

        // Trouver le ":" le plus récent avant le caret
        int colonPos = -1;
        for (int i = caretPos - 1; i >= 0; i--) {
            char ch = text.charAt(i);
            if (ch == ':') {
                colonPos = i;
                break;
            }
            if (ch == ' ' || ch == '\n')
                break;
        }
        if (colonPos < 0) {
            hideAutocomplete();
            return;
        }

        String partial = text.substring(colonPos, caretPos); // ex: ":sm"
        // On n'affiche pas si le raccourci est déjà fermé
        if (partial.length() > 1 && partial.endsWith(":")) {
            hideAutocomplete();
            return;
        }

        List<String> matches = EMOJI_MAP.keySet().stream()
                .filter(k -> k.startsWith(partial) && !k.equals(partial))
                .collect(Collectors.toList());
        if (matches.isEmpty()) {
            hideAutocomplete();
            return;
        }

        autocompleteColonPos = colonPos;
        showAutocompletePopup(matches);
    }

    /** Affiche le popup d'autocomplétion avec les raccourcis correspondants. */
    private void showAutocompletePopup(List<String> matches) {
        autocompletePopup.removeAll();
        for (String shortcut : matches) {
            String emoji = EMOJI_MAP.get(shortcut);
            JMenuItem item = new JMenuItem(emoji + "  " + shortcut);
            item.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));
            final int pos = autocompleteColonPos;
            item.addActionListener(ae -> SwingUtilities.invokeLater(() -> {
                try {
                    int caret = messageTextArea.getCaretPosition();
                    messageTextArea.getDocument().remove(pos, caret - pos);
                    messageTextArea.getDocument().insertString(pos, emoji, null);
                } catch (Exception ex) {
                    /* ignore */ }
            }));
            autocompletePopup.add(item);
        }
        try {
            Rectangle2D r2d = messageTextArea.modelToView2D(autocompleteColonPos);
            if (r2d != null) {
                int x = (int) r2d.getX();
                int y = (int) r2d.getY();
                autocompletePopup.show(messageTextArea, x, y - autocompletePopup.getPreferredSize().height - 4);
            }
        } catch (Exception ex) {
            /* ignore */ }
    }

    /** Cache le popup d'autocomplétion. */
    private void hideAutocomplete() {
        if (autocompletePopup.isVisible())
            autocompletePopup.setVisible(false);
    }

    /** Affiche un sélecteur d'emoji visuel (grille). */
    private void showEmojiPicker() {
        JPopupMenu picker = new JPopupMenu();
        JPanel grid = new JPanel(new java.awt.GridLayout(0, 4, 3, 3));
        grid.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        grid.setBackground(new Color(248, 250, 252));
        for (Map.Entry<String, String> entry : EMOJI_MAP.entrySet()) {
            Color bg = EMOJI_COLORS.getOrDefault(entry.getKey(), new Color(220, 220, 220));
            // JLabel respecte setBackground sous Nimbus, contrairement à JButton
            JLabel lbl = new JLabel(entry.getValue(), JLabel.CENTER);
            lbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
            lbl.setToolTipText(entry.getKey());
            lbl.setBackground(bg);
            lbl.setOpaque(true);
            lbl.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(bg.darker(), 1, true),
                    BorderFactory.createEmptyBorder(4, 6, 4, 6)));
            lbl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            lbl.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    messageTextArea.insert(entry.getValue(), messageTextArea.getCaretPosition());
                    picker.setVisible(false);
                    messageTextArea.requestFocusInWindow();
                }

                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    lbl.setBackground(bg.darker());
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    lbl.setBackground(bg);
                }
            });
            grid.add(lbl);
        }
        picker.add(grid);
        picker.show(emojiPickerBtn, 0, -picker.getPreferredSize().height - 2);
    }
}
