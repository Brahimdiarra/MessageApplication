package main.java.com.ubo.tp.message.ihm.panels;

import main.java.com.ubo.tp.message.core.DataManager;
import main.java.com.ubo.tp.message.core.SessionManager;
import main.java.com.ubo.tp.message.core.database.IDatabaseObserver;
import main.java.com.ubo.tp.message.datamodel.Channel;
import main.java.com.ubo.tp.message.datamodel.Message;
import main.java.com.ubo.tp.message.datamodel.User;
import main.java.com.ubo.tp.message.ihm.MessageAppMainView;
import main.java.com.ubo.tp.message.ihm.dialog.ChannelCreationDialog;
import main.java.com.ubo.tp.message.ihm.dialog.ChannelEditDialog;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Panel d'affichage de la liste des canaux avec barre de recherche (CHN-002).
 *
 * @author BRAHIM
 */
public class ChannelListPanel extends JPanel implements IDatabaseObserver {

    private DefaultListModel<Channel> channelListModel;
    private JList<Channel> channelList;
    private DataManager dataManager;
    private JLabel countLabel;
    private JTextField searchField;

    private final List<Channel> allChannels = new ArrayList<>();
    private final Map<UUID, Integer> unreadCounts = new HashMap<>();
    private final Map<UUID, String> lastMessages = new HashMap<>();
    private UUID currentlyViewingUuid = null;

    // ── Dark mode ─────────────────────────────────────────────────────────
    private boolean darkMode = false;

    private static final Color DARK_BG        = new Color(47, 49, 54);
    private static final Color DARK_BG_ALT    = new Color(54, 57, 63);
    private static final Color DARK_SEL       = new Color(88, 101, 242);
    private static final Color DARK_TEXT      = new Color(148, 155, 164);
    private static final Color DARK_TEXT_ACT  = new Color(220, 221, 222);
    private static final Color DARK_BADGE     = new Color(237, 66, 69);
    private static final Color DARK_CHANNEL   = new Color(149, 128, 255);

    public ChannelListPanel() {
        initComponents();
    }

    public void setDataManager(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    /** Active le rendu sombre style Discord. */
    public void setDarkMode(boolean dark) {
        this.darkMode = dark;
        if (dark) {
            setBackground(DARK_BG);
            setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            channelList.setBackground(DARK_BG);
            channelList.setForeground(DARK_TEXT_ACT);
            channelList.setSelectionBackground(DARK_SEL);
            channelList.setSelectionForeground(Color.WHITE);
        }
        repaint();
    }

    private void createNewChannel() {
        if (dataManager == null) {
            JOptionPane.showMessageDialog(this, "Erreur : DataManager non initialisé", "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }
        Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(this);
        ChannelCreationDialog.showDialog(parentFrame, dataManager);
    }

    private void editSelectedChannel() {
        Channel selected = getSelectedChannel();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Sélectionnez un canal à modifier.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (dataManager == null) {
            JOptionPane.showMessageDialog(this, "Erreur : DataManager non initialisé", "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }
        Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(this);
        ChannelEditDialog.showDialog(parentFrame, selected, dataManager);
    }

    private void deleteSelectedChannel() {
        Channel selected = getSelectedChannel();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Sélectionnez un canal à supprimer.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null || !selected.getCreator().getUuid().equals(currentUser.getUuid())) {
            JOptionPane.showMessageDialog(this, "Seul le créateur du canal peut le supprimer.", "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Supprimer le canal \"" + selected.getName() + "\" ?", "Confirmation", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            dataManager.deleteChannel(selected);
        }
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        TitledBorder channelBorder = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(MessageAppMainView.COLOR_ACCENT, 1, true),
                "Canaux disponibles", TitledBorder.LEFT, TitledBorder.TOP);
        channelBorder.setTitleColor(MessageAppMainView.COLOR_ACCENT);
        channelBorder.setTitleFont(new Font("SansSerif", Font.BOLD, 12));
        setBorder(channelBorder);
        setBackground(MessageAppMainView.COLOR_PANEL_BG);

        JPanel searchPanel = new JPanel(new BorderLayout(5, 5));
        searchPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 2, 4));
        searchField = new JTextField();
        searchField.setToolTipText("Rechercher un canal par nom");
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { applyFilter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { applyFilter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
        });
        searchPanel.add(new JLabel("🔍 "), BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);
        add(searchPanel, BorderLayout.NORTH);

        channelListModel = new DefaultListModel<>();
        channelList = new JList<>(channelListModel);
        channelList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        channelList.setCellRenderer(new ChannelListCellRenderer());
        channelList.setFixedCellHeight(52);
        channelList.setBackground(Color.WHITE);
        add(new JScrollPane(channelList), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(new Color(248, 250, 252));
        bottomPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(203, 213, 225)));

        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        infoPanel.setOpaque(false);
        countLabel = new JLabel("0 canal(aux)");
        countLabel.setFont(new Font("SansSerif", Font.ITALIC, 11));
        countLabel.setForeground(Color.GRAY);
        infoPanel.add(countLabel);

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 3, 3));
        buttonsPanel.setOpaque(false);

        buttonsPanel.add(makeBtn("+", "Créer un nouveau canal", new Color(22, 163, 74), () -> createNewChannel()));
        buttonsPanel.add(makeBtn("✏", "Modifier le canal sélectionné", MessageAppMainView.COLOR_ACCENT, () -> editSelectedChannel()));
        buttonsPanel.add(makeBtn("🗑", "Supprimer un canal", new Color(220, 38, 38), () -> deleteSelectedChannel()));

        bottomPanel.add(infoPanel, BorderLayout.WEST);
        bottomPanel.add(buttonsPanel, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);

        channelListModel.addListDataListener(new javax.swing.event.ListDataListener() {
            public void intervalAdded(javax.swing.event.ListDataEvent e)   { updateCount(); }
            public void intervalRemoved(javax.swing.event.ListDataEvent e) { updateCount(); }
            public void contentsChanged(javax.swing.event.ListDataEvent e) { updateCount(); }
            private void updateCount() { countLabel.setText(channelListModel.getSize() + " canal(aux)"); }
        });
    }

    private JButton makeBtn(String label, String tooltip, Color bg, Runnable action) {
        JButton btn = new JButton(label);
        btn.setToolTipText(tooltip);
        btn.setPreferredSize(new Dimension(30, 24));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.addActionListener(e -> action.run());
        return btn;
    }

    private void applyFilter() {
        String query = searchField.getText().trim().toLowerCase();
        User currentUser = SessionManager.getInstance().getCurrentUser();
        channelListModel.clear();
        for (Channel c : allChannels) {
            // Ne montrer que les canaux accessibles à l'utilisateur connecté
            if (!c.isMember(currentUser)) continue;
            if (query.isEmpty() || c.getName().toLowerCase().contains(query))
                channelListModel.addElement(c);
        }
    }

    public Channel getSelectedChannel() { return channelList.getSelectedValue(); }

    @Override
    public void notifyChannelAdded(Channel addedChannel) {
        SwingUtilities.invokeLater(() -> {
            boolean exists = allChannels.stream().anyMatch(c -> c.getUuid().equals(addedChannel.getUuid()));
            if (!exists) allChannels.add(addedChannel);
            applyFilter();
        });
    }

    @Override
    public void notifyChannelDeleted(Channel deletedChannel) {
        SwingUtilities.invokeLater(() -> {
            allChannels.removeIf(c -> c.getUuid().equals(deletedChannel.getUuid()));
            applyFilter();
        });
    }

    @Override
    public void notifyChannelModified(Channel modifiedChannel) {
        SwingUtilities.invokeLater(() -> {
            for (int i = 0; i < allChannels.size(); i++) {
                if (allChannels.get(i).getUuid().equals(modifiedChannel.getUuid())) {
                    allChannels.set(i, modifiedChannel);
                    break;
                }
            }
            applyFilter();
        });
    }

    @Override
    public void notifyMessageAdded(Message addedMessage) {
        SwingUtilities.invokeLater(() -> {
            User currentUser = SessionManager.getInstance().getCurrentUser();
            if (currentUser == null) return;
            UUID recipient = addedMessage.getRecipient();
            Channel ch = allChannels.stream().filter(c -> c.getUuid().equals(recipient)).findFirst().orElse(null);
            if (ch == null || !ch.isMember(currentUser)) return;

            // Mettre à jour l'aperçu du dernier message
            String text = addedMessage.getText();
            if (text.isEmpty() && addedMessage.hasImage()) text = "📷 Image";
            if (text.length() > 30) text = text.substring(0, 27) + "...";
            boolean isMe = addedMessage.getSender().getUuid().equals(currentUser.getUuid());
            String prefix = isMe ? "Vous: " : "@" + addedMessage.getSender().getUserTag() + ": ";
            lastMessages.put(recipient, prefix + text);

            // Incrémenter le badge non-lu uniquement si je ne suis pas l'expéditeur et que ce n'est pas la conv active
            if (!isMe && !recipient.equals(currentlyViewingUuid)) {
                unreadCounts.merge(recipient, 1, Integer::sum);
            }
            channelList.repaint();
        });
    }

    public void markAsRead(UUID channelUuid) {
        currentlyViewingUuid = channelUuid;
        unreadCounts.remove(channelUuid);
        channelList.repaint();
    }

    /**
     * Charge les aperçus du dernier message pour chaque canal accessible à l'utilisateur connecté.
     * À appeler après le login une fois que la base est chargée.
     */
    public void refreshLastMessages(DataManager dm) {
        if (dm == null) return;
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) return;
        lastMessages.clear();
        List<Message> sorted = new ArrayList<>(dm.getMessages());
        sorted.sort(Comparator.comparingLong(Message::getEmissionDate));
        for (Message m : sorted) {
            UUID recipient = m.getRecipient();
            boolean isChannel = allChannels.stream().anyMatch(c -> c.getUuid().equals(recipient));
            if (!isChannel) continue;
            Channel ch = allChannels.stream().filter(c -> c.getUuid().equals(recipient)).findFirst().orElse(null);
            if (ch == null || !ch.isMember(currentUser)) continue;
            String text = m.getText();
            if (text.isEmpty() && m.hasImage()) text = "📷 Image";
            if (text.length() > 30) text = text.substring(0, 27) + "...";
            String prefix = m.getSender().getUuid().equals(currentUser.getUuid()) ? "Vous: " : "@" + m.getSender().getUserTag() + ": ";
            lastMessages.put(recipient, prefix + text);
        }
        SwingUtilities.invokeLater(channelList::repaint);
    }

    @Override public void notifyMessageDeleted(Message m)  { }
    @Override public void notifyMessageModified(Message m) { }
    @Override public void notifyUserAdded(User u)          { }
    @Override public void notifyUserDeleted(User u)        { }
    @Override public void notifyUserModified(User u)       { }

    public void clearSelection() { channelList.clearSelection(); }

    public void addSelectionListener(javax.swing.event.ListSelectionListener listener) {
        channelList.addListSelectionListener(listener);
    }

    // ── Renderer ──────────────────────────────────────────────────────────

    private class ChannelListCellRenderer implements ListCellRenderer<Channel> {
        @Override
        public Component getListCellRendererComponent(JList<? extends Channel> list, Channel channel,
                                                      int index, boolean isSelected, boolean cellHasFocus) {
            JPanel cell = new JPanel(new BorderLayout(8, 0));
            cell.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 8));
            int unread = unreadCounts.getOrDefault(channel.getUuid(), 0);

            if (darkMode) {
                cell.setBackground(isSelected ? DARK_SEL : (index % 2 == 0 ? DARK_BG : DARK_BG_ALT));
                cell.setOpaque(true);

                // Icône # ou 🔒
                String icon = channel.isPrivate() ? "🔒" : "#";
                JLabel hashLabel = new JLabel(icon);
                hashLabel.setFont(new Font("Segoe UI Emoji", Font.BOLD, channel.isPrivate() ? 13 : 18));
                hashLabel.setForeground(isSelected ? Color.WHITE : (channel.isPrivate() ? new Color(250, 168, 26) : DARK_TEXT));
                hashLabel.setPreferredSize(new Dimension(22, 20));
                cell.add(hashLabel, BorderLayout.WEST);

                String tooltip = "Créateur : " + channel.getCreator().getName()
                        + (channel.isPrivate() ? " | Canal privé (" + channel.getUsers().size() + " membres)" : " | Canal public");
                JPanel textPanel = new JPanel();
                textPanel.setOpaque(false);
                textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
                JLabel nameLabel = new JLabel(channel.getName());
                nameLabel.setFont(new Font("SansSerif", unread > 0 ? Font.BOLD : Font.PLAIN, 13));
                nameLabel.setForeground(isSelected ? Color.WHITE : (unread > 0 ? DARK_TEXT_ACT : DARK_TEXT));
                nameLabel.setToolTipText(tooltip);
                textPanel.add(nameLabel);
                String preview = lastMessages.get(channel.getUuid());
                if (preview != null) {
                    JLabel previewLabel = new JLabel(preview);
                    previewLabel.setFont(new Font("SansSerif", Font.PLAIN, 10));
                    previewLabel.setForeground(isSelected ? new Color(220, 220, 255) : DARK_TEXT);
                    textPanel.add(previewLabel);
                }
                cell.add(textPanel, BorderLayout.CENTER);

                if (unread > 0) {
                    JLabel badge = new JLabel(unread > 99 ? "99+" : String.valueOf(unread));
                    badge.setFont(new Font("SansSerif", Font.BOLD, 10));
                    badge.setForeground(Color.WHITE);
                    badge.setBackground(DARK_BADGE);
                    badge.setOpaque(true);
                    badge.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
                    cell.add(badge, BorderLayout.EAST);
                }
            } else {
                // Mode clair original
                cell.setBackground(isSelected ? list.getSelectionBackground()
                        : (index % 2 == 0 ? Color.WHITE : new Color(248, 250, 252)));
                cell.setOpaque(true);
                JPanel lightTextPanel = new JPanel();
                lightTextPanel.setOpaque(false);
                lightTextPanel.setLayout(new BoxLayout(lightTextPanel, BoxLayout.Y_AXIS));
                JLabel nameLabel = new JLabel("  #  " + channel.getName());
                nameLabel.setFont(new Font("SansSerif", unread > 0 ? Font.BOLD : Font.PLAIN, 12));
                nameLabel.setForeground(isSelected ? list.getSelectionForeground() : new Color(109, 40, 217));
                nameLabel.setToolTipText("Créateur : " + channel.getCreator().getName());
                lightTextPanel.add(nameLabel);
                String lightPreview = lastMessages.get(channel.getUuid());
                if (lightPreview != null) {
                    JLabel previewLabel = new JLabel("  " + lightPreview);
                    previewLabel.setFont(new Font("SansSerif", Font.PLAIN, 10));
                    previewLabel.setForeground(Color.GRAY);
                    lightTextPanel.add(previewLabel);
                }
                cell.add(lightTextPanel, BorderLayout.CENTER);
                if (unread > 0) {
                    JLabel badge = new JLabel(unread > 99 ? "99+" : String.valueOf(unread));
                    badge.setFont(new Font("SansSerif", Font.BOLD, 10));
                    badge.setForeground(Color.WHITE);
                    badge.setBackground(new Color(220, 38, 38));
                    badge.setOpaque(true);
                    badge.setBorder(BorderFactory.createEmptyBorder(1, 5, 1, 5));
                    cell.add(badge, BorderLayout.EAST);
                }
            }
            return cell;
        }
    }
}
