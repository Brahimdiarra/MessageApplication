package main.java.com.ubo.tp.message.ihm.dialog;

import main.java.com.ubo.tp.message.core.DataManager;
import main.java.com.ubo.tp.message.core.SessionManager;
import main.java.com.ubo.tp.message.datamodel.Channel;
import main.java.com.ubo.tp.message.datamodel.User;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Dialogue de modification d'un canal :
 * - Le créateur peut ajouter / retirer des membres (CHN-007, CHN-087)
 * - Un membre non-créateur peut quitter le canal (CHN-005)
 *
 * @author BRAHIM
 */
public class ChannelEditDialog extends JDialog {

    private Channel channel;
    private DataManager dataManager;
    private User currentUser;

    /**
     * Constructeur.
     *
     * @param parent      Fenêtre parente
     * @param channel     Canal à modifier
     * @param dataManager Gestionnaire de données
     */
    public ChannelEditDialog(Frame parent, Channel channel, DataManager dataManager) {
        super(parent, "Modifier le canal #" + channel.getName(), true);
        this.channel = channel;
        this.dataManager = dataManager;
        this.currentUser = SessionManager.getInstance().getCurrentUser();
        initComponents();
    }

    /**
     * Initialisation des composants selon le rôle de l'utilisateur connecté.
     */
    private void initComponents() {
        setSize(520, 400);
        setLocationRelativeTo(getParent());
        setResizable(false);
        setLayout(new BorderLayout(10, 10));

        // En-tête : infos du canal
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        boolean isPrivate = !channel.getUsers().isEmpty();
        String type = isPrivate ? "Privé" : "Public";
        headerPanel.add(new JLabel(
                "<html><b>#" + channel.getName() + "</b>  |  Type : " + type
                        + "  |  Créateur : @" + channel.getCreator().getUserTag() + "</html>"
        ));
        add(headerPanel, BorderLayout.NORTH);

        // Déterminer le rôle de l'utilisateur connecté
        boolean isCreator = channel.getCreator().getUuid().equals(currentUser.getUuid());
        boolean isMember = channel.getUsers().stream()
                .anyMatch(u -> u.getUuid().equals(currentUser.getUuid()));

        JPanel centerPanel;

        if (!isPrivate) {
            // Canal public : pas de gestion de membres
            centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            centerPanel.add(new JLabel("Ce canal est public. Tous les utilisateurs peuvent y accéder."));
        } else if (isCreator) {
            // Créateur : peut ajouter / retirer des membres
            centerPanel = buildCreatorPanel();
        } else if (isMember) {
            // Membre non-créateur : peut seulement quitter
            centerPanel = buildMemberPanel();
        } else {
            centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            centerPanel.add(new JLabel("Vous n'êtes pas membre de ce canal privé."));
        }

        add(centerPanel, BorderLayout.CENTER);

        // Bouton Fermer
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton closeButton = new JButton("Fermer");
        closeButton.addActionListener(e -> dispose());
        bottomPanel.add(closeButton);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    /**
     * Construit le panel pour le créateur : deux listes (membres actuels + utilisateurs à ajouter).
     */
    private JPanel buildCreatorPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 10, 0));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        // --- Panneau gauche : membres actuels ---
        JPanel membersPanel = new JPanel(new BorderLayout(5, 5));
        membersPanel.setBorder(BorderFactory.createTitledBorder("Membres actuels"));

        DefaultListModel<User> membersModel = new DefaultListModel<>();
        JList<User> membersList = new JList<>(membersModel);
        membersList.setCellRenderer(new UserCellRenderer());
        membersList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Remplir avec les membres (sauf le créateur, qui ne peut pas être retiré)
        for (User u : channel.getUsers()) {
            if (!u.getUuid().equals(channel.getCreator().getUuid())) {
                membersModel.addElement(u);
            }
        }

        JScrollPane membersScroll = new JScrollPane(membersList);

        JButton removeButton = new JButton("Retirer");
        removeButton.addActionListener(e -> {
            User selected = membersList.getSelectedValue();
            if (selected == null) {
                JOptionPane.showMessageDialog(this,
                        "Sélectionnez un membre à retirer.", "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            removeMember(selected);
            membersModel.removeElement(selected);
            JOptionPane.showMessageDialog(this,
                    "@" + selected.getUserTag() + " a été retiré du canal.",
                    "Succès", JOptionPane.INFORMATION_MESSAGE);
        });

        membersPanel.add(membersScroll, BorderLayout.CENTER);
        membersPanel.add(removeButton, BorderLayout.SOUTH);

        // --- Panneau droit : utilisateurs disponibles à ajouter ---
        JPanel availablePanel = new JPanel(new BorderLayout(5, 5));
        availablePanel.setBorder(BorderFactory.createTitledBorder("Ajouter des membres"));

        DefaultListModel<User> availableModel = new DefaultListModel<>();
        JList<User> availableList = new JList<>(availableModel);
        availableList.setCellRenderer(new UserCellRenderer());
        availableList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Remplir avec les utilisateurs qui ne sont PAS encore membres
        Set<User> allUsers = dataManager.getUsers();
        List<User> currentMembers = channel.getUsers();
        for (User u : allUsers) {
            boolean alreadyMember = currentMembers.stream()
                    .anyMatch(m -> m.getUuid().equals(u.getUuid()));
            if (!alreadyMember) {
                availableModel.addElement(u);
            }
        }

        JScrollPane availableScroll = new JScrollPane(availableList);

        JButton addButton = new JButton("Ajouter");
        addButton.addActionListener(e -> {
            User selected = availableList.getSelectedValue();
            if (selected == null) {
                JOptionPane.showMessageDialog(this,
                        "Sélectionnez un utilisateur à ajouter.", "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            addMember(selected);
            // Déplacer l'utilisateur de la liste droite vers la liste gauche
            membersModel.addElement(selected);
            availableModel.removeElement(selected);
            JOptionPane.showMessageDialog(this,
                    "@" + selected.getUserTag() + " a été ajouté au canal.",
                    "Succès", JOptionPane.INFORMATION_MESSAGE);
        });

        availablePanel.add(availableScroll, BorderLayout.CENTER);
        availablePanel.add(addButton, BorderLayout.SOUTH);

        panel.add(membersPanel);
        panel.add(availablePanel);

        return panel;
    }

    /**
     * Construit le panel pour un membre non-créateur : bouton "Quitter".
     */
    private JPanel buildMemberPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 10, 30, 10));

        panel.add(new JLabel("Vous êtes membre de ce canal.    "));

        JButton leaveButton = new JButton("Quitter ce canal");
        leaveButton.setForeground(Color.RED);
        leaveButton.addActionListener(e -> leaveChannel());
        panel.add(leaveButton);

        return panel;
    }

    /**
     * Ajoute un membre au canal.
     * Crée un nouveau Channel avec le même UUID mais la liste de membres mise à jour,
     * puis écrit le fichier sur le disque (le WatchableDirectory notifiera la modification).
     */
    private void addMember(User userToAdd) {
        List<User> newMembers = new ArrayList<>(channel.getUsers());
        newMembers.add(userToAdd);

        // Créer un Channel mis à jour avec le MÊME UUID (écrasement du fichier existant)
        Channel updatedChannel = new Channel(
                channel.getUuid(),
                channel.getCreator(),
                channel.getName(),
                newMembers
        );
        dataManager.sendChannel(updatedChannel);

        // Mettre à jour la référence locale pour les opérations suivantes dans ce dialogue
        this.channel = updatedChannel;

        System.out.println("[CHANNEL] Membre ajouté : @" + userToAdd.getUserTag()
                + " dans #" + channel.getName());
    }

    /**
     * Retire un membre du canal.
     */
    private void removeMember(User userToRemove) {
        List<User> newMembers = new ArrayList<>(channel.getUsers());
        newMembers.removeIf(u -> u.getUuid().equals(userToRemove.getUuid()));

        Channel updatedChannel = new Channel(
                channel.getUuid(),
                channel.getCreator(),
                channel.getName(),
                newMembers
        );
        dataManager.sendChannel(updatedChannel);
        this.channel = updatedChannel;

        System.out.println("[CHANNEL] Membre retiré : @" + userToRemove.getUserTag()
                + " de #" + channel.getName());
    }

    /**
     * L'utilisateur connecté quitte le canal (non-créateur seulement).
     */
    private void leaveChannel() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Voulez-vous vraiment quitter le canal \"#" + channel.getName() + "\" ?",
                "Confirmation", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            removeMember(currentUser);
            JOptionPane.showMessageDialog(this,
                    "Vous avez quitté le canal #" + channel.getName(),
                    "Succès", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        }
    }

    /**
     * Renderer pour afficher les utilisateurs dans les listes.
     */
    private class UserCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                                                      int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof User) {
                User u = (User) value;
                setText("@" + u.getUserTag() + "  (" + u.getName() + ")");
            }
            return this;
        }
    }

    /**
     * Méthode statique pour afficher le dialogue.
     */
    public static void showDialog(Frame parent, Channel channel, DataManager dataManager) {
        ChannelEditDialog dialog = new ChannelEditDialog(parent, channel, dataManager);
        dialog.setVisible(true);
    }
}
