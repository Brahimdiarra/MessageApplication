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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Panel d'affichage de la liste des utilisateurs avec barre de recherche (USR-008).
 *
 * @author BRAHIM
 */
public class UserListPanel extends JPanel implements IDatabaseObserver {

    private DefaultListModel<User> userListModel;
    private JList<User> userList;
    private JLabel countLabel;
    private JTextField searchField;

    private final List<User> allUsers = new ArrayList<>();
    private final Map<UUID, Integer> unreadCounts = new HashMap<>();
    private final Map<UUID, String> lastMessages = new HashMap<>();
    private UUID currentlyViewingUuid = null;

    // ── Dark mode ─────────────────────────────────────────────────────────
    private boolean darkMode = false;

    static final Color DARK_BG       = new Color(47, 49, 54);
    static final Color DARK_BG_ALT   = new Color(54, 57, 63);
    static final Color DARK_SEL      = new Color(88, 101, 242);
    static final Color DARK_TEXT     = new Color(148, 155, 164);
    static final Color DARK_TEXT_ACT = new Color(220, 221, 222);
    static final Color DARK_ONLINE   = new Color(59, 165, 92);
    static final Color DARK_OFFLINE  = new Color(116, 127, 141);
    static final Color DARK_BADGE    = new Color(237, 66, 69);

    private static final Color[] AVATAR_COLORS = {
        new Color(88, 101, 242), new Color(59, 165, 92),  new Color(237, 66, 69),
        new Color(250, 168, 26), new Color(235, 69, 158), new Color(149, 128, 255),
        new Color(32, 200, 255), new Color(255, 115, 55),
    };

    public UserListPanel() {
        initComponents();
    }

    /** Active le rendu sombre style Discord. */
    public void setDarkMode(boolean dark) {
        this.darkMode = dark;
        if (dark) {
            setBackground(DARK_BG);
            setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            userList.setBackground(DARK_BG);
            userList.setForeground(DARK_TEXT_ACT);
            userList.setSelectionBackground(DARK_SEL);
            userList.setSelectionForeground(Color.WHITE);
        }
        repaint();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        TitledBorder userBorder = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(MessageAppMainView.COLOR_ACCENT, 1, true),
                "Utilisateurs", TitledBorder.LEFT, TitledBorder.TOP);
        userBorder.setTitleColor(MessageAppMainView.COLOR_ACCENT);
        userBorder.setTitleFont(new Font("SansSerif", Font.BOLD, 12));
        setBorder(userBorder);
        setBackground(MessageAppMainView.COLOR_PANEL_BG);

        JPanel searchPanel = new JPanel(new BorderLayout(5, 5));
        searchPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 2, 4));
        searchField = new JTextField();
        searchField.setToolTipText("Rechercher un utilisateur (@tag ou nom)");
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { applyFilter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { applyFilter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
        });
        searchPanel.add(new JLabel("🔍 "), BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);
        add(searchPanel, BorderLayout.NORTH);

        userListModel = new DefaultListModel<>();
        userList = new JList<>(userListModel);
        userList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userList.setCellRenderer(new UserListCellRenderer());
        userList.setFixedCellHeight(48);
        userList.setBackground(Color.WHITE);
        add(new JScrollPane(userList), BorderLayout.CENTER);

        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        infoPanel.setBackground(new Color(248, 250, 252));
        infoPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(203, 213, 225)));
        countLabel = new JLabel("0 utilisateur(s)");
        countLabel.setFont(new Font("SansSerif", Font.ITALIC, 11));
        countLabel.setForeground(Color.GRAY);
        infoPanel.add(countLabel);
        add(infoPanel, BorderLayout.SOUTH);

        userListModel.addListDataListener(new javax.swing.event.ListDataListener() {
            public void intervalAdded(javax.swing.event.ListDataEvent e)   { updateCount(); }
            public void intervalRemoved(javax.swing.event.ListDataEvent e) { updateCount(); }
            public void contentsChanged(javax.swing.event.ListDataEvent e) { updateCount(); }
            private void updateCount() { countLabel.setText(userListModel.getSize() + " utilisateur(s)"); }
        });
    }

    private void applyFilter() {
        String query = searchField.getText().trim().toLowerCase();
        userListModel.clear();
        for (User u : allUsers) {
            if (query.isEmpty()
                    || u.getUserTag().toLowerCase().contains(query)
                    || u.getName().toLowerCase().contains(query)) {
                userListModel.addElement(u);
            }
        }
    }

    private void sortUsers() {
        allUsers.sort((a, b) -> {
            if (a.isOnline() != b.isOnline()) return a.isOnline() ? -1 : 1;
            return a.getUserTag().compareToIgnoreCase(b.getUserTag());
        });
    }

    public User getSelectedUser() { return userList.getSelectedValue(); }

    @Override
    public void notifyUserAdded(User addedUser) {
        SwingUtilities.invokeLater(() -> {
            boolean exists = allUsers.stream().anyMatch(u -> u.getUuid().equals(addedUser.getUuid()));
            if (!exists) allUsers.add(addedUser);
            sortUsers();
            applyFilter();
        });
    }

    @Override
    public void notifyUserDeleted(User deletedUser) {
        SwingUtilities.invokeLater(() -> {
            allUsers.removeIf(u -> u.getUuid().equals(deletedUser.getUuid()));
            applyFilter();
        });
    }

    @Override
    public void notifyUserModified(User modifiedUser) {
        SwingUtilities.invokeLater(() -> {
            for (int i = 0; i < allUsers.size(); i++) {
                if (allUsers.get(i).getUuid().equals(modifiedUser.getUuid())) {
                    allUsers.set(i, modifiedUser);
                    break;
                }
            }
            sortUsers();
            applyFilter();
        });
    }

    @Override
    public void notifyMessageAdded(Message m) {
        SwingUtilities.invokeLater(() -> {
            User currentUser = SessionManager.getInstance().getCurrentUser();
            if (currentUser == null) return;
            UUID myUuid = currentUser.getUuid();
            UUID sender = m.getSender().getUuid();
            UUID recipient = m.getRecipient();

            if (sender.equals(myUuid)) {
                // Message que j'envoie à un utilisateur
                boolean recipientIsUser = allUsers.stream().anyMatch(u -> u.getUuid().equals(recipient));
                if (!recipientIsUser) return;
                lastMessages.put(recipient, buildPreview(m, "Vous: "));
            } else if (recipient.equals(myUuid)) {
                // Message reçu d'un autre utilisateur
                lastMessages.put(sender, buildPreview(m, ""));
                if (!sender.equals(currentlyViewingUuid)) {
                    unreadCounts.merge(sender, 1, Integer::sum);
                }
            } else {
                return; // pas mon message DM
            }
            userList.repaint();
        });
    }

    public void markAsRead(UUID userUuid) {
        currentlyViewingUuid = userUuid;
        unreadCounts.remove(userUuid);
        userList.repaint();
    }

    /**
     * Charge les aperçus du dernier message pour chaque conversation DM depuis la base.
     * À appeler après le login une fois que la base est chargée.
     */
    public void refreshLastMessages(DataManager dm) {
        if (dm == null) return;
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) return;
        UUID myUuid = currentUser.getUuid();
        lastMessages.clear();
        List<Message> sorted = new ArrayList<>(dm.getMessages());
        sorted.sort(Comparator.comparingLong(Message::getEmissionDate));
        for (Message m : sorted) {
            UUID sender = m.getSender().getUuid();
            UUID recipient = m.getRecipient();
            if (sender.equals(myUuid)) {
                boolean recipientIsUser = allUsers.stream().anyMatch(u -> u.getUuid().equals(recipient));
                if (!recipientIsUser) continue;
                lastMessages.put(recipient, buildPreview(m, "Vous: "));
            } else if (recipient.equals(myUuid)) {
                lastMessages.put(sender, buildPreview(m, ""));
            }
        }
        SwingUtilities.invokeLater(userList::repaint);
    }

    private String buildPreview(Message m, String prefix) {
        String text = m.getText();
        if (text.isEmpty() && m.hasImage()) text = "📷 Image";
        if (text.length() > 30) text = text.substring(0, 27) + "...";
        return prefix + text;
    }

    @Override public void notifyMessageDeleted(Message m)  { }
    @Override public void notifyMessageModified(Message m) { }
    @Override public void notifyChannelAdded(Channel c)    { }
    @Override public void notifyChannelDeleted(Channel c)  { }
    @Override public void notifyChannelModified(Channel c) { }

    public void clearSelection() { userList.clearSelection(); }

    public void addSelectionListener(javax.swing.event.ListSelectionListener listener) {
        userList.addListSelectionListener(listener);
    }

    // ── Renderer ──────────────────────────────────────────────────────────

    private class UserListCellRenderer implements ListCellRenderer<User> {
        @Override
        public Component getListCellRendererComponent(JList<? extends User> list, User user,
                                                      int index, boolean isSelected, boolean cellHasFocus) {
            JPanel cell = new JPanel(new BorderLayout(8, 0));
            cell.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 8));
            int unread = unreadCounts.getOrDefault(user.getUuid(), 0);

            if (darkMode) {
                cell.setBackground(isSelected ? DARK_SEL : (index % 2 == 0 ? DARK_BG : DARK_BG_ALT));
                cell.setOpaque(true);

                // Avatar avec initiale et pastille de statut
                JPanel avatarPanel = new JPanel(null) {
                    @Override protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        int idx = Math.abs(user.getUserTag().hashCode()) % AVATAR_COLORS.length;
                        g2.setColor(AVATAR_COLORS[idx]);
                        g2.fillOval(0, 0, 32, 32);
                        g2.setColor(Color.WHITE);
                        g2.setFont(new Font("SansSerif", Font.BOLD, 14));
                        String letter = user.getName().isEmpty() ? "?" :
                                String.valueOf(user.getName().charAt(0)).toUpperCase();
                        FontMetrics fm = g2.getFontMetrics();
                        g2.drawString(letter, (32 - fm.stringWidth(letter)) / 2,
                                (32 + fm.getAscent() - fm.getDescent()) / 2);
                        // Pastille statut
                        g2.setColor(cell.getBackground());
                        g2.fillOval(20, 20, 14, 14);
                        g2.setColor(user.isOnline() ? DARK_ONLINE : DARK_OFFLINE);
                        g2.fillOval(22, 22, 10, 10);
                        g2.dispose();
                    }
                };
                avatarPanel.setOpaque(false);
                avatarPanel.setPreferredSize(new Dimension(36, 36));

                // Texte
                JPanel textPanel = new JPanel();
                textPanel.setOpaque(false);
                textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
                JLabel nameLabel = new JLabel("@" + user.getUserTag());
                nameLabel.setFont(new Font("SansSerif", unread > 0 ? Font.BOLD : Font.PLAIN, 13));
                nameLabel.setForeground(isSelected ? Color.WHITE : (user.isOnline() ? DARK_TEXT_ACT : DARK_TEXT));
                String preview = lastMessages.getOrDefault(user.getUuid(), user.getName());
                JLabel subLabel = new JLabel(preview);
                subLabel.setFont(new Font("SansSerif", unread > 0 ? Font.BOLD : Font.PLAIN, 10));
                subLabel.setForeground(isSelected ? new Color(220, 220, 255) : (unread > 0 ? DARK_TEXT_ACT : DARK_TEXT));
                textPanel.add(nameLabel);
                textPanel.add(subLabel);

                cell.add(avatarPanel, BorderLayout.WEST);
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
                String statusIcon = user.isOnline() ? "🟢" : "⚫";
                JLabel nameLabel = new JLabel(statusIcon + "  @" + user.getUserTag() + "  (" + user.getName() + ")");
                nameLabel.setFont(new Font("SansSerif", unread > 0 ? Font.BOLD : Font.PLAIN, 12));
                nameLabel.setForeground(isSelected ? list.getSelectionForeground() : new Color(30, 41, 59));
                cell.add(nameLabel, BorderLayout.CENTER);
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
