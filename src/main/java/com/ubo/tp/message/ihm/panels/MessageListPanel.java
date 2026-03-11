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
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.text.SimpleDateFormat;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.imageio.ImageIO;
import main.java.com.ubo.tp.message.ihm.reactions.ReactionStore;

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
        setBackground(MessageAppMainView.COLOR_BG);

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

        // Réactions : repaint à chaque changement dans le ReactionStore
        ReactionStore.getInstance().addListener(
                () -> SwingUtilities.invokeLater(messageList::repaint));

        // Clic droit sur un message → sélecteur de réaction
        messageList.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    int idx = messageList.locationToIndex(e.getPoint());
                    if (idx >= 0) {
                        messageList.setSelectedIndex(idx);
                        showReactionPicker(messageListModel.getElementAt(idx), e.getComponent(), e.getX(), e.getY());
                    }
                }
            }
        });

        // Panel du bas : compteur + bouton supprimer
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(new Color(47, 49, 54));
        bottomPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(32, 34, 37)));

        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        infoPanel.setBackground(new Color(47, 49, 54));
        infoPanel.setOpaque(true);
        countLabel = new JLabel("0 message(s)");
        countLabel.setFont(new Font("SansSerif", Font.ITALIC, 11));
        countLabel.setForeground(new Color(148, 155, 164));
        infoPanel.add(countLabel);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        rightPanel.setOpaque(false);
        JButton deleteButton = new JButton("🗑  Supprimer");
        deleteButton.setFont(new Font("SansSerif", Font.BOLD, 11));
        deleteButton.setBackground(new Color(220, 38, 38));
        deleteButton.setForeground(Color.WHITE);
        deleteButton.setOpaque(true);
        deleteButton.setBorderPainted(false);
        deleteButton.setFocusPainted(false);
        deleteButton.setToolTipText("Supprimer mon message sélectionné (MSG-006)");
        deleteButton.addActionListener(e -> deleteSelectedMessage());
        rightPanel.add(deleteButton);

        bottomPanel.add(infoPanel, BorderLayout.WEST);
        bottomPanel.add(rightPanel, BorderLayout.EAST);
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
     * Vérifie que l'utilisateur connecté est membre du canal avant d'afficher.
     *
     * @param channelUuid UUID du canal
     */
    public void filterByChannel(UUID channelUuid) {
        currentFilterType = "channel";
        currentFilterUuid = channelUuid;
        currentMessages.clear();

        if (messageApp == null || messageApp.getmDataManager() == null) { applySearch(); return; }

        // Trouver le canal et vérifier l'accès
        User currentUser = main.java.com.ubo.tp.message.core.SessionManager.getInstance().getCurrentUser();
        main.java.com.ubo.tp.message.datamodel.Channel targetChannel = null;
        for (main.java.com.ubo.tp.message.datamodel.Channel ch : messageApp.getmDataManager().getChannels()) {
            if (ch.getUuid().equals(channelUuid)) { targetChannel = ch; break; }
        }

        if (targetChannel != null && !targetChannel.isMember(currentUser)) {
            // L'utilisateur n'est pas membre : on n'affiche rien
            applySearch();
            return;
        }

        Set<Message> allMessages = messageApp.getmDataManager().getMessages();
        for (Message message : allMessages) {
            if (message.getRecipient().equals(channelUuid)) {
                currentMessages.add(message);
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
     * Affiche le sélecteur de réaction (popup) pour un message donné.
     */
    private void showReactionPicker(Message msg, Component source, int x, int y) {
        String[] emojis = {
            "👍", "❤️", "😂", "😮",
            "😢", "🔥", "🎉", "👀"
        };
        Color[] palette = {
            new Color( 94, 234, 212), new Color(252, 165, 165),
            new Color(254, 240, 138), new Color(134, 239, 172),
            new Color(147, 197, 253), new Color(253, 186,  74),
            new Color(216, 180, 254), new Color(186, 230, 253)
        };
        JPopupMenu popup = new JPopupMenu();
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 4));
        row.setBackground(new Color(248, 250, 252));
        User currentUser = SessionManager.getInstance().getCurrentUser();
        for (int idx = 0; idx < emojis.length; idx++) {
            final String emoji = emojis[idx];
            Color bg = palette[idx % palette.length];
            boolean reacted = currentUser != null &&
                    ReactionStore.getInstance().hasReacted(msg.getUuid(), emoji, currentUser.getUuid());
            // JLabel respecte setBackground sous Nimbus, contrairement à JButton
            JLabel lbl = new JLabel(emoji, JLabel.CENTER);
            lbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));
            lbl.setBackground(reacted ? bg.darker() : bg);
            lbl.setOpaque(true);
            lbl.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(bg.darker(), 2, true),
                    BorderFactory.createEmptyBorder(4, 8, 4, 8)));
            lbl.setToolTipText(emoji);
            lbl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            lbl.addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent me) {
                    if (currentUser != null)
                        ReactionStore.getInstance().toggle(msg.getUuid(), emoji, currentUser.getUuid());
                    popup.setVisible(false);
                }
                @Override public void mouseEntered(MouseEvent me) {
                    lbl.setBackground(bg.darker());
                }
                @Override public void mouseExited(MouseEvent me) {
                    lbl.setBackground(bg);
                }
            });
            row.add(lbl);
        }
        popup.add(row);
        popup.show(source, x, y);
    }

    /**
     * Décode une image base64 et la retourne redimensionnée à maxWidth pixels de large.
     *
     * @param base64Data données image en base64
     * @param maxWidth   largeur maximale en pixels
     * @return ImageIcon prête à l'affichage, ou null en cas d'erreur
     */
    private ImageIcon decodeImage(String base64Data, int maxWidth) {
        try {
            byte[] bytes = Base64.getDecoder().decode(base64Data);
            BufferedImage img = ImageIO.read(new ByteArrayInputStream(bytes));
            if (img == null) return null;
            int w = Math.min(img.getWidth(), maxWidth);
            int h = (int) ((double) img.getHeight() * w / img.getWidth());
            Image scaled = img.getScaledInstance(w, h, Image.SCALE_SMOOTH);
            return new ImageIcon(scaled);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Renderer de style "bulle de conversation" (Messenger / Discord).
     *
     * – Messages du user connecté : bulle bleue alignée à DROITE
     * – DM reçus               : bulle colorée alignée à GAUCHE
     * – Messages de canal      : chaque auteur a sa couleur (à GAUCHE),
     *                              sauf le user connecté (à DROITE en bleu).
     */
    private class MessageListCellRenderer extends JPanel implements ListCellRenderer<Message> {

        // Palette de couleurs pour distinguer les auteurs dans un canal
        private final Color[] PALETTE = {
            new Color(16, 185, 129),  // émeraude
            new Color(245, 158, 11),  // ambre
            new Color(239,  68,  68), // rouge vif
            new Color(168,  85, 247), // violet
            new Color(236,  72, 153), // rose
            new Color( 20, 184, 166), // teal
            new Color(249, 115,  22), // orange
            new Color( 99, 102, 241), // indigo
        };

        // Bleu pour les messages de l'utilisateur connecté
        private final Color BLUE_ME = new Color(37, 99, 235);

        public MessageListCellRenderer() {
            setOpaque(true);
        }

        /** Détermine la couleur de bulle d'un auteur via le hash de son tag. */
        private Color colorFor(User sender) {
            int idx = Math.abs(sender.getUserTag().hashCode()) % PALETTE.length;
            return PALETTE[idx];
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends Message> list, Message message,
                                                      int index, boolean isSelected, boolean cellHasFocus) {
            if (message == null) return this;

            removeAll();
            setLayout(new BorderLayout());
            setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
            setBackground(isSelected ? new Color(64, 68, 75) : new Color(54, 57, 63));

            User currentUser = SessionManager.getInstance().getCurrentUser();
            boolean isMe = currentUser != null
                    && message.getSender().getUuid().equals(currentUser.getUuid());

            Color bubbleColor = isMe ? BLUE_ME : colorFor(message.getSender());

            // ── Bulle ────────────────────────────────────────────────
            BubblePanel bubble = new BubblePanel(bubbleColor, 14);
            bubble.setLayout(new BorderLayout(0, 2));
            bubble.setBorder(BorderFactory.createEmptyBorder(7, 11, 7, 11));

            // Auteur uniquement pour les messages des autres
            if (!isMe) {
                JLabel author = new JLabel("@" + message.getSender().getUserTag());
                author.setFont(new Font("SansSerif", Font.BOLD, 11));
                author.setForeground(new Color(255, 255, 255, 210));
                bubble.add(author, BorderLayout.NORTH);
            }

            // Contenu de la bulle (texte + image éventuelle)
            JPanel contentPanel = new JPanel();
            contentPanel.setOpaque(false);
            contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

            if (!message.getText().isEmpty()) {
                JTextArea textArea = new JTextArea(message.getText());
                textArea.setEditable(false);
                textArea.setLineWrap(true);
                textArea.setWrapStyleWord(true);
                textArea.setOpaque(true);
                textArea.setBackground(bubbleColor);
                textArea.setForeground(Color.WHITE);
                textArea.setFont(new Font("SansSerif", Font.PLAIN, 13));
                textArea.setBorder(null);
                // Force le JTextArea à calculer sa hauteur sur la vraie largeur
                int maxW2 = list.getWidth() > 80 ? (int) (list.getWidth() * 0.72) - 22 : 378;
                textArea.setSize(maxW2, Short.MAX_VALUE);
                contentPanel.add(textArea);
            }

            if (message.hasImage()) {
                ImageIcon imageIcon = decodeImage(message.getImageData(), 240);
                if (imageIcon != null) {
                    JLabel imgLabel = new JLabel(imageIcon);
                    imgLabel.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
                    imgLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                    contentPanel.add(imgLabel);
                }
            }

            bubble.add(contentPanel, BorderLayout.CENTER);

            // Pied de bulle : badge destination + horodatage
            JPanel footer = new JPanel(new FlowLayout(isMe ? FlowLayout.RIGHT : FlowLayout.LEFT, 4, 0));
            footer.setOpaque(false);

            String dest = resolveDestination(message);
            if (dest != null) {
                JLabel badge = new JLabel(dest);
                badge.setFont(new Font("SansSerif", Font.BOLD, 9));
                badge.setForeground(new Color(255, 255, 255, 170));
                footer.add(badge);
            }

            JLabel dateLabel = new JLabel(dateFormat.format(new Date(message.getEmissionDate())));
            dateLabel.setFont(new Font("SansSerif", Font.ITALIC, 10));
            dateLabel.setForeground(new Color(255, 255, 255, 170));
            footer.add(dateLabel);

            bubble.add(footer, BorderLayout.SOUTH);

            // ── Largeur max de la bulle (72 % du panneau) ─────────────────
            int listW = list.getWidth();
            int maxW  = listW > 80 ? (int) (listW * 0.72) : 400;
            bubble.setMaximumSize(new Dimension(maxW, Short.MAX_VALUE));

            // ── Rangée avec alignement gauche ou droite ───────────────────
            JPanel row = new JPanel();
            row.setOpaque(false);
            row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
            if (isMe) {
                row.add(Box.createHorizontalGlue());
                row.add(bubble);
            } else {
                row.add(bubble);
                row.add(Box.createHorizontalGlue());
            }
            add(row, BorderLayout.CENTER);

            // ── Badges de réactions sous la bulle ─────────────────────────
            Map<String, Integer> reactions =
                    ReactionStore.getInstance().getCountsFor(message.getUuid());
            if (!reactions.isEmpty()) {
                JPanel reactionRow = new JPanel(
                        new FlowLayout(isMe ? FlowLayout.RIGHT : FlowLayout.LEFT, 3, 0));
                reactionRow.setOpaque(false);
                User cu = SessionManager.getInstance().getCurrentUser();
                for (Map.Entry<String, Integer> re : reactions.entrySet()) {
                    boolean mine = cu != null && ReactionStore.getInstance()
                            .hasReacted(message.getUuid(), re.getKey(), cu.getUuid());
                    JLabel badge = new JLabel(re.getKey() + " " + re.getValue());
                    badge.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 12));
                    badge.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(new Color(203, 213, 225), 1),
                            BorderFactory.createEmptyBorder(1, 5, 1, 5)));
                    badge.setBackground(mine
                            ? new Color(219, 234, 254)
                            : new Color(243, 244, 246));
                    badge.setOpaque(true);
                    reactionRow.add(badge);
                }
                add(reactionRow, BorderLayout.SOUTH);
            }
            return this;
        }

        /** Résout le destinataire du message en une chaîne lisible. */
        private String resolveDestination(Message message) {
            if (dataManager == null) return null;
            UUID id = message.getRecipient();
            for (Channel ch : dataManager.getChannels()) {
                if (ch.getUuid().equals(id)) return "# " + ch.getName();
            }
            for (User u : dataManager.getUsers()) {
                if (u.getUuid().equals(id)) return "→ @" + u.getUserTag();
            }
            return null;
        }
    }

    /** Panneau avec fond en rectangle arrondi — utilisé pour les bulles de messages. */
    private static class BubblePanel extends JPanel {
        private final Color color;
        private final int   radius;

        BubblePanel(Color color, int radius) {
            this.color  = color;
            this.radius = radius;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            g2.dispose();
        }
    }
}
