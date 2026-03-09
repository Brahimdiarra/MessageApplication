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
 * Dialogue de modification d'un canal.
 *
 * Règles :
 *  - Créateur d'un canal PUBLIC  → peut renommer le canal uniquement
 *  - Créateur d'un canal PRIVÉ   → peut renommer + ajouter/retirer des membres
 *  - Membre non-créateur (privé) → peut seulement quitter le canal
 *
 * @author BRAHIM
 */
public class ChannelEditDialog extends JDialog {

    private Channel channel;
    private DataManager dataManager;
    private User currentUser;

    // Champ de renommage (visible seulement pour le créateur)
    private JTextField nameField;

    public ChannelEditDialog(Frame parent, Channel channel, DataManager dataManager) {
        super(parent, "Modifier le canal #" + channel.getName(), true);
        this.channel = channel;
        this.dataManager = dataManager;
        this.currentUser = SessionManager.getInstance().getCurrentUser();
        initComponents();
    }

    private void initComponents() {
        setLocationRelativeTo(getParent());
        setResizable(false);
        setLayout(new BorderLayout(10, 10));

        boolean isPrivate = !channel.getUsers().isEmpty();
        boolean isCreator = channel.getCreator().getUuid().equals(currentUser.getUuid());
        boolean isMember  = channel.getUsers().stream()
                .anyMatch(u -> u.getUuid().equals(currentUser.getUuid()));

        // ── EN-TÊTE : infos du canal ─────────────────────────────────────────
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        String type = isPrivate ? "Privé" : "Public";
        headerPanel.add(new JLabel(
                "<html><b>#" + channel.getName() + "</b>  |  Type : " + type
                        + "  |  Créateur : @" + channel.getCreator().getUserTag() + "</html>"
        ));
        add(headerPanel, BorderLayout.NORTH);

        // ── CENTRE : contenu selon le rôle ───────────────────────────────────
        JPanel centerPanel;

        if (isCreator) {
            if (!isPrivate) {
                // Canal public : renommage uniquement
                setSize(420, 180);
                centerPanel = buildRenameOnlyPanel();
            } else {
                // Canal privé : renommage + gestion des membres
                setSize(540, 480);
                centerPanel = buildCreatorPrivatePanel();
            }
        } else if (isMember) {
            // Membre non-créateur : quitter seulement
            setSize(420, 180);
            centerPanel = buildMemberPanel();
        } else {
            setSize(420, 150);
            centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            centerPanel.add(new JLabel("Vous n'êtes pas membre de ce canal privé."));
        }

        add(centerPanel, BorderLayout.CENTER);

        // ── BAS : bouton Fermer ───────────────────────────────────────────────
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton closeButton = new JButton("Fermer");
        closeButton.addActionListener(e -> dispose());
        bottomPanel.add(closeButton);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    /**
     * Canal PUBLIC + créateur : juste le champ de renommage.
     */
    private JPanel buildRenameOnlyPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Renommer le canal"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Label
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.2;
        panel.add(new JLabel("Nouveau nom :"), gbc);

        // Champ texte pré-rempli avec le nom actuel
        gbc.gridx = 1; gbc.weightx = 0.6;
        nameField = new JTextField(channel.getName(), 15);
        panel.add(nameField, gbc);

        // Bouton Sauvegarder
        gbc.gridx = 2; gbc.weightx = 0.2;
        JButton saveButton = new JButton("Sauvegarder");
        saveButton.addActionListener(e -> renameChannel());
        panel.add(saveButton, gbc);

        return panel;
    }

    /**
     * Canal PRIVÉ + créateur : renommage en haut + gestion des membres en bas.
     */
    private JPanel buildCreatorPrivatePanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        // ─ Zone de renommage (haut) ─
        JPanel renamePanel = new JPanel(new GridBagLayout());
        renamePanel.setBorder(BorderFactory.createTitledBorder("Renommer le canal"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.2;
        renamePanel.add(new JLabel("Nouveau nom :"), gbc);

        gbc.gridx = 1; gbc.weightx = 0.6;
        nameField = new JTextField(channel.getName(), 15);
        renamePanel.add(nameField, gbc);

        gbc.gridx = 2; gbc.weightx = 0.2;
        JButton saveButton = new JButton("Sauvegarder");
        saveButton.addActionListener(e -> renameChannel());
        renamePanel.add(saveButton, gbc);

        panel.add(renamePanel, BorderLayout.NORTH);

        // ─ Zone de gestion des membres (bas) ─
        panel.add(buildMembersManagementPanel(), BorderLayout.CENTER);

        return panel;
    }

    /**
     * Les deux listes : membres actuels (retirer) + utilisateurs disponibles (ajouter).
     */
    private JPanel buildMembersManagementPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 10, 0));
        panel.setBorder(BorderFactory.createTitledBorder("Gestion des membres"));

        // ─── Liste gauche : membres actuels ───
        JPanel membersPanel = new JPanel(new BorderLayout(5, 5));
        membersPanel.setBorder(BorderFactory.createTitledBorder("Membres actuels"));

        DefaultListModel<User> membersModel = new DefaultListModel<>();
        JList<User> membersList = new JList<>(membersModel);
        membersList.setCellRenderer(new UserCellRenderer());
        membersList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Remplir — le créateur apparaît mais ne peut pas être retiré
        for (User u : channel.getUsers()) {
            if (!u.getUuid().equals(channel.getCreator().getUuid())) {
                membersModel.addElement(u);
            }
        }

        JButton removeButton = new JButton("◀ Retirer");
        removeButton.addActionListener(e -> {
            User selected = membersList.getSelectedValue();
            if (selected == null) {
                JOptionPane.showMessageDialog(this,
                        "Sélectionnez un membre à retirer.", "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            removeMember(selected);
            membersModel.removeElement(selected);
        });

        membersPanel.add(new JScrollPane(membersList), BorderLayout.CENTER);
        membersPanel.add(removeButton, BorderLayout.SOUTH);

        // ─── Liste droite : utilisateurs disponibles ───
        JPanel availablePanel = new JPanel(new BorderLayout(5, 5));
        availablePanel.setBorder(BorderFactory.createTitledBorder("Utilisateurs à ajouter"));

        DefaultListModel<User> availableModel = new DefaultListModel<>();
        JList<User> availableList = new JList<>(availableModel);
        availableList.setCellRenderer(new UserCellRenderer());
        availableList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        Set<User> allUsers = dataManager.getUsers();
        List<User> currentMembers = channel.getUsers();
        for (User u : allUsers) {
            boolean alreadyMember = currentMembers.stream()
                    .anyMatch(m -> m.getUuid().equals(u.getUuid()));
            if (!alreadyMember) {
                availableModel.addElement(u);
            }
        }

        JButton addButton = new JButton("Ajouter ▶");
        addButton.addActionListener(e -> {
            User selected = availableList.getSelectedValue();
            if (selected == null) {
                JOptionPane.showMessageDialog(this,
                        "Sélectionnez un utilisateur à ajouter.", "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            addMember(selected);
            membersModel.addElement(selected);
            availableModel.removeElement(selected);
        });

        availablePanel.add(new JScrollPane(availableList), BorderLayout.CENTER);
        availablePanel.add(addButton, BorderLayout.SOUTH);

        panel.add(membersPanel);
        panel.add(availablePanel);

        return panel;
    }

    /**
     * Canal privé + membre non-créateur : bouton "Quitter".
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

    // ── Actions ──────────────────────────────────────────────────────────────

    /**
     * Renomme le canal : crée un nouveau Channel avec le même UUID mais un nouveau nom.
     */
    private void renameChannel() {
        String newName = nameField.getText().trim();

        if (newName.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Le nom ne peut pas être vide.", "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!newName.matches("[a-zA-Z0-9_-]+")) {
            JOptionPane.showMessageDialog(this,
                    "Le nom ne peut contenir que des lettres, chiffres, tirets et underscores.",
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (newName.equals(channel.getName())) {
            JOptionPane.showMessageDialog(this,
                    "Le nom est identique à l'actuel.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Créer un Channel avec le MÊME UUID + MÊME membres mais nouveau nom
        Channel renamed = new Channel(
                channel.getUuid(),
                channel.getCreator(),
                newName,
                channel.getUsers()
        );
        dataManager.sendChannel(renamed);
        this.channel = renamed;

        // Mettre à jour le titre du dialogue
        setTitle("Modifier le canal #" + newName);

        JOptionPane.showMessageDialog(this,
                "Canal renommé en \"#" + newName + "\" avec succès !",
                "Succès", JOptionPane.INFORMATION_MESSAGE);

        System.out.println("[CHANNEL] Canal renommé : " + newName);
    }

    private void addMember(User userToAdd) {
        List<User> newMembers = new ArrayList<>(channel.getUsers());
        newMembers.add(userToAdd);
        Channel updated = new Channel(channel.getUuid(), channel.getCreator(), channel.getName(), newMembers);
        dataManager.sendChannel(updated);
        this.channel = updated;
        System.out.println("[CHANNEL] Membre ajouté : @" + userToAdd.getUserTag() + " dans #" + channel.getName());
    }

    private void removeMember(User userToRemove) {
        List<User> newMembers = new ArrayList<>(channel.getUsers());
        newMembers.removeIf(u -> u.getUuid().equals(userToRemove.getUuid()));
        Channel updated = new Channel(channel.getUuid(), channel.getCreator(), channel.getName(), newMembers);
        dataManager.sendChannel(updated);
        this.channel = updated;
        System.out.println("[CHANNEL] Membre retiré : @" + userToRemove.getUserTag() + " de #" + channel.getName());
    }

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

    // ── Renderer ─────────────────────────────────────────────────────────────

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

    public static void showDialog(Frame parent, Channel channel, DataManager dataManager) {
        ChannelEditDialog dialog = new ChannelEditDialog(parent, channel, dataManager);
        dialog.setVisible(true);
    }
}
