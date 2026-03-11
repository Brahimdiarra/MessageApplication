package main.java.com.ubo.tp.message.ihm.panels;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Panel d'affichage de la liste des utilisateurs avec barre de recherche
 * (USR-008).
 *
 * @author BRAHIM
 */
public class UserListPanel extends JPanel implements IDatabaseObserver {
    private static final long serialVersionUID = 1L;

    private DefaultListModel<User> userListModel;
    private JList<User> userList;
    private JLabel countLabel;
    private JTextField searchField;

    /**
     * Liste complète de tous les utilisateurs reçus (avant filtre de recherche).
     * On garde cette liste séparée pour pouvoir refilter à chaque frappe
     * sans perdre les éléments qui ne correspondent pas au filtre actuel.
     */
    private final List<User> allUsers = new ArrayList<>();

    /** Compteurs de DM non lus par UUID d'expéditeur (CHN-009). */
    private final Map<UUID, Integer> unreadCounts = new HashMap<>();

    /** UUID de l'utilisateur dont la conversation est actuellement affichée. */
    private UUID currentlyViewingUuid = null;

    public UserListPanel() {
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        TitledBorder userBorder = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(MessageAppMainView.COLOR_ACCENT, 1, true),
                "Utilisateurs connectés", TitledBorder.LEFT, TitledBorder.TOP);
        userBorder.setTitleColor(MessageAppMainView.COLOR_ACCENT);
        userBorder.setTitleFont(new Font("SansSerif", Font.BOLD, 12));
        setBorder(userBorder);
        setBackground(MessageAppMainView.COLOR_PANEL_BG);

        // ── HAUT : barre de recherche ─────────────────────────────────────────
        JPanel searchPanel = new JPanel(new BorderLayout(5, 5));
        searchPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 2, 4));
        searchField = new JTextField();
        searchField.setToolTipText("Rechercher un utilisateur (@tag ou nom)");
        // Hint visuel en placeholder
        searchField.putClientProperty("JTextField.placeholderText", "🔍 Rechercher...");

        // À chaque frappe, on refiltre la liste
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                applyFilter();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                applyFilter();
            }

            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                applyFilter();
            }
        });

        searchPanel.add(new JLabel("🔍 "), BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);
        add(searchPanel, BorderLayout.NORTH);

        // ── CENTRE : liste des utilisateurs ──────────────────────────────────
        userListModel = new DefaultListModel<>();
        userList = new JList<>(userListModel);
        userList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userList.setCellRenderer(new UserListCellRenderer());
        userList.setFixedCellHeight(40);
        userList.setBackground(Color.WHITE);
        add(new JScrollPane(userList), BorderLayout.CENTER);

        // ── BAS : compteur ────────────────────────────────────────────────────
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        infoPanel.setBackground(new Color(248, 250, 252));
        infoPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(203, 213, 225)));
        countLabel = new JLabel("0 utilisateur(s)");
        countLabel.setFont(new Font("SansSerif", Font.ITALIC, 11));
        countLabel.setForeground(Color.GRAY);
        infoPanel.add(countLabel);
        add(infoPanel, BorderLayout.SOUTH);

        userListModel.addListDataListener(new javax.swing.event.ListDataListener() {
            public void intervalAdded(javax.swing.event.ListDataEvent e) {
                updateCount();
            }

            public void intervalRemoved(javax.swing.event.ListDataEvent e) {
                updateCount();
            }

            public void contentsChanged(javax.swing.event.ListDataEvent e) {
                updateCount();
            }

            private void updateCount() {
                countLabel.setText(userListModel.getSize() + " utilisateur(s)");
            }
        });
    }

    /**
     * Refiltre la liste affichée selon le texte dans le champ de recherche.
     * On cherche dans le tag ET le nom de l'utilisateur (insensible à la casse).
     */
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

    public User getSelectedUser() {
        return userList.getSelectedValue();
    }

    @Override
    public void notifyUserAdded(User addedUser) {
        SwingUtilities.invokeLater(() -> {
            // Ajouter à la liste complète si pas déjà présent
            boolean exists = allUsers.stream().anyMatch(u -> u.getUuid().equals(addedUser.getUuid()));
            if (!exists) {
                allUsers.add(addedUser);
            }
            applyFilter(); // refilter pour mettre à jour l'affichage
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
            // Remplacer dans la liste complète
            for (int i = 0; i < allUsers.size(); i++) {
                if (allUsers.get(i).getUuid().equals(modifiedUser.getUuid())) {
                    allUsers.set(i, modifiedUser);
                    break;
                }
            }
            applyFilter();
        });
    }

    @Override
    public void notifyMessageAdded(Message m) {
        SwingUtilities.invokeLater(() -> {
            User currentUser = SessionManager.getInstance().getCurrentUser();
            if (currentUser == null)
                return;

            // Ignorer ses propres messages
            if (m.getSender().getUuid().equals(currentUser.getUuid()))
                return;

            // Ne compter que les DM adressés à l'utilisateur connecté
            if (!m.getRecipient().equals(currentUser.getUuid()))
                return;

            UUID senderUuid = m.getSender().getUuid();

            // Ne pas compter si la conversation avec cet expéditeur est déjà ouverte
            if (senderUuid.equals(currentlyViewingUuid))
                return;

            unreadCounts.merge(senderUuid, 1, Integer::sum);
            userList.repaint();
        });
    }

    /**
     * Marque tous les DM d'un utilisateur comme lus (CHN-009).
     *
     * @param userUuid UUID de l'utilisateur dont la conversation est ouverte
     */
    public void markAsRead(UUID userUuid) {
        currentlyViewingUuid = userUuid;
        unreadCounts.remove(userUuid);
        userList.repaint();
    }

    @Override
    public void notifyMessageDeleted(Message m) {
        /* Non utilisé */ }

    @Override
    public void notifyMessageModified(Message m) {
        /* Non utilisé */ }

    @Override
    public void notifyChannelAdded(Channel c) {
        /* Non utilisé */ }

    @Override
    public void notifyChannelDeleted(Channel c) {
        /* Non utilisé */ }

    @Override
    public void notifyChannelModified(Channel c) {
        /* Non utilisé */ }

    private class UserListCellRenderer extends DefaultListCellRenderer {
        private static final long serialVersionUID = 1L;
        private final Color COLOR_BADGE_BG = new Color(220, 38, 38);
        private final Color COLOR_TEXT = new Color(30, 41, 59);

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
            if (!(value instanceof User)) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                return this;
            }
            User user = (User) value;
            int unread = unreadCounts.getOrDefault(user.getUuid(), 0);

            JPanel cell = new JPanel(new BorderLayout(4, 0));
            cell.setOpaque(true);
            cell.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
            cell.setBackground(isSelected ? list.getSelectionBackground()
                    : (index % 2 == 0 ? Color.WHITE : new Color(248, 250, 252)));

            String statusIcon = user.isOnline() ? "🟢" : "⚫";
            JLabel nameLabel = new JLabel(statusIcon + "  @" + user.getUserTag() + "  (" + user.getName() + ")");
            nameLabel.setFont(new Font("SansSerif", unread > 0 ? Font.BOLD : Font.PLAIN, 12));
            nameLabel.setForeground(isSelected ? list.getSelectionForeground() : COLOR_TEXT);
            nameLabel.setToolTipText("UUID: " + user.getUuid());
            cell.add(nameLabel, BorderLayout.CENTER);

            if (unread > 0) {
                JLabel badge = new JLabel(unread > 99 ? "99+" : String.valueOf(unread));
                badge.setFont(new Font("SansSerif", Font.BOLD, 10));
                badge.setForeground(Color.WHITE);
                badge.setBackground(COLOR_BADGE_BG);
                badge.setOpaque(true);
                badge.setBorder(BorderFactory.createEmptyBorder(1, 5, 1, 5));
                cell.add(badge, BorderLayout.EAST);
            }

            return cell;
        }
    }

    public void addSelectionListener(javax.swing.event.ListSelectionListener listener) {
        userList.addListSelectionListener(listener);
    }
}
