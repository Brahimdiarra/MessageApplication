package main.java.com.ubo.tp.message.ihm.panels;

import main.java.com.ubo.tp.message.core.database.IDatabaseObserver;
import main.java.com.ubo.tp.message.datamodel.Channel;
import main.java.com.ubo.tp.message.datamodel.Message;
import main.java.com.ubo.tp.message.datamodel.User;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

/**
 * Panel d'affichage de la liste des utilisateurs.
 *
 * @author BRAHIM
 */
public class UserListPanel extends JPanel implements IDatabaseObserver {

    private DefaultListModel<User> userListModel;
    private JList<User> userList;

    /**
     * Constructeur.
     */
    public UserListPanel() {
        initComponents();
    }

    /**
     * Initialisation des composants.
     */
    private void initComponents() {
        setLayout(new BorderLayout());
        setBorder(new TitledBorder("Utilisateurs connectés"));

        // Modèle de liste
        userListModel = new DefaultListModel<>();

        // Liste des utilisateurs
        userList = new JList<>(userListModel);
        userList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userList.setCellRenderer(new UserListCellRenderer());

        // ScrollPane pour la liste
        JScrollPane scrollPane = new JScrollPane(userList);
        add(scrollPane, BorderLayout.CENTER);

        // Panel d'information en bas
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel countLabel = new JLabel("0 utilisateur(s)");
        infoPanel.add(countLabel);
        add(infoPanel, BorderLayout.SOUTH);

        // Mise à jour du compteur quand la liste change
        userListModel.addListDataListener(new javax.swing.event.ListDataListener() {
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
                countLabel.setText(userListModel.getSize() + " utilisateur(s)");
            }
        });
    }

    /**
     * Retourne l'utilisateur sélectionné.
     *
     * @return L'utilisateur sélectionné ou null
     */
    public User getSelectedUser() {
        return userList.getSelectedValue();
    }

    @Override
    public void notifyUserAdded(User addedUser) {
        SwingUtilities.invokeLater(() -> {
            if (!userListModel.contains(addedUser)) {
                userListModel.addElement(addedUser);
            }
        });
    }

    @Override
    public void notifyUserDeleted(User deletedUser) {
        SwingUtilities.invokeLater(() -> {
            userListModel.removeElement(deletedUser);
        });
    }

    @Override
    public void notifyUserModified(User modifiedUser) {
        SwingUtilities.invokeLater(() -> {
            int index = userListModel.indexOf(modifiedUser);
            if (index >= 0) {
                userListModel.set(index, modifiedUser);
            }
        });
    }

    @Override
    public void notifyMessageAdded(Message addedMessage) {
        // Non utilisé dans ce panel
    }

    @Override
    public void notifyMessageDeleted(Message deletedMessage) {
        // Non utilisé dans ce panel
    }

    @Override
    public void notifyMessageModified(Message modifiedMessage) {
        // Non utilisé dans ce panel
    }

    @Override
    public void notifyChannelAdded(Channel addedChannel) {
        // Non utilisé dans ce panel
    }

    @Override
    public void notifyChannelDeleted(Channel deletedChannel) {
        // Non utilisé dans ce panel
    }

    @Override
    public void notifyChannelModified(Channel modifiedChannel) {
        // Non utilisé dans ce panel
    }

    /**
     * Renderer personnalisé pour les utilisateurs.
     */
    private class UserListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                                                      int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (value instanceof User) {
                User user = (User) value;
                String statusIcon = user.isOnline() ? "🟢" : "⚫";
                setText(statusIcon + " @" + user.getUserTag() + " (" + user.getName() + ")");
                setToolTipText("UUID: " + user.getUuid());
            }

            return this;
        }
    }

    /**
     * Ajoute un listener pour détecter la sélection d'un utilisateur.
     *
     * @param listener Le listener à ajouter
     */
    public void addSelectionListener(javax.swing.event.ListSelectionListener listener) {
        userList.addListSelectionListener(listener);
    }
}