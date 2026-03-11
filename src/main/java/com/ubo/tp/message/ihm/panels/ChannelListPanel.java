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
import java.util.List;

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

    /** Liste complète de tous les canaux (avant filtre de recherche). */
    private final List<Channel> allChannels = new ArrayList<>();

    /**
     * Constructeur.
     */
    public ChannelListPanel() {
        initComponents();
    }

    //  setter
    public void setDataManager(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    /**
     * Ouvre le dialogue de création de canal.
     */
    private void createNewChannel() {
        if (dataManager == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Erreur : DataManager non initialisé",
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        // Récupérer la fenêtre parente
        Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(this);

        // Afficher le dialogue
        ChannelCreationDialog.showDialog(parentFrame, dataManager);
    }

    /**
     * Ouvre le dialogue de modification du canal sélectionné.
     */
    private void editSelectedChannel() {
        Channel selected = getSelectedChannel();

        if (selected == null) {
            JOptionPane.showMessageDialog(this,
                    "Sélectionnez un canal à modifier.",
                    "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        if (dataManager == null) {
            JOptionPane.showMessageDialog(this,
                    "Erreur : DataManager non initialisé",
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(this);
        ChannelEditDialog.showDialog(parentFrame, selected, dataManager);
    }

    /**
     * Supprime le canal sélectionné (uniquement si l'utilisateur connecté en est le créateur).
     */
    private void deleteSelectedChannel() {
        Channel selected = getSelectedChannel();

        if (selected == null) {
            JOptionPane.showMessageDialog(this,
                    "Sélectionnez un canal à supprimer.",
                    "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null || !selected.getCreator().getUuid().equals(currentUser.getUuid())) {
            JOptionPane.showMessageDialog(this,
                    "Seul le créateur du canal peut le supprimer.",
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Supprimer le canal \"" + selected.getName() + "\" ?",
                "Confirmation", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            dataManager.deleteChannel(selected);
            System.out.println("[CHANNEL] Canal supprimé : " + selected.getName());
        }
    }

    /**
     * Initialisation des composants.
     */
    private void initComponents() {
        setLayout(new BorderLayout());
        TitledBorder channelBorder = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(MessageAppMainView.COLOR_ACCENT, 1, true),
                "Canaux disponibles", TitledBorder.LEFT, TitledBorder.TOP);
        channelBorder.setTitleColor(MessageAppMainView.COLOR_ACCENT);
        channelBorder.setTitleFont(new Font("SansSerif", Font.BOLD, 12));
        setBorder(channelBorder);
        setBackground(MessageAppMainView.COLOR_PANEL_BG);

        // ── HAUT : barre de recherche ─────────────────────────────────────────
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
        channelList.setFixedCellHeight(36);
        channelList.setBackground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(channelList);
        add(scrollPane, BorderLayout.CENTER);

        // Panel du bas
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(new Color(248, 250, 252));
        bottomPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(203, 213, 225)));

        // Compteur
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        infoPanel.setOpaque(false);
        countLabel = new JLabel("0 canal(aux)");
        countLabel.setFont(new Font("SansSerif", Font.ITALIC, 11));
        countLabel.setForeground(Color.GRAY);
        infoPanel.add(countLabel);

        // Boutons colorés
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 3, 3));
        buttonsPanel.setOpaque(false);

        JButton newChannelButton = new JButton("+");
        newChannelButton.setToolTipText("Créer un nouveau canal");
        newChannelButton.setPreferredSize(new Dimension(30, 24));
        newChannelButton.setBackground(new Color(22, 163, 74));  // vert
        newChannelButton.setForeground(Color.WHITE);
        newChannelButton.setOpaque(true);
        newChannelButton.setBorderPainted(false);
        newChannelButton.setFocusPainted(false);
        newChannelButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        newChannelButton.addActionListener(e -> createNewChannel());

        JButton editChannelButton = new JButton("✏");
        editChannelButton.setToolTipText("Modifier le canal sélectionné");
        editChannelButton.setPreferredSize(new Dimension(30, 24));
        editChannelButton.setBackground(MessageAppMainView.COLOR_ACCENT);  // bleu
        editChannelButton.setForeground(Color.WHITE);
        editChannelButton.setOpaque(true);
        editChannelButton.setBorderPainted(false);
        editChannelButton.setFocusPainted(false);
        editChannelButton.addActionListener(e -> editSelectedChannel());

        JButton deleteChannelButton = new JButton("🗑");
        deleteChannelButton.setToolTipText("Supprimer un canal");
        deleteChannelButton.setPreferredSize(new Dimension(30, 24));
        deleteChannelButton.setBackground(new Color(220, 38, 38));  // rouge
        deleteChannelButton.setForeground(Color.WHITE);
        deleteChannelButton.setOpaque(true);
        deleteChannelButton.setBorderPainted(false);
        deleteChannelButton.setFocusPainted(false);
        deleteChannelButton.addActionListener(e -> deleteSelectedChannel());

        buttonsPanel.add(newChannelButton);
        buttonsPanel.add(editChannelButton);
        buttonsPanel.add(deleteChannelButton);

        bottomPanel.add(infoPanel, BorderLayout.WEST);
        bottomPanel.add(buttonsPanel, BorderLayout.EAST);

        add(bottomPanel, BorderLayout.SOUTH);

        // Mise à jour du compteur
        channelListModel.addListDataListener(new javax.swing.event.ListDataListener() {
            public void intervalAdded(javax.swing.event.ListDataEvent e) { updateCount(); }
            public void intervalRemoved(javax.swing.event.ListDataEvent e) { updateCount(); }
            public void contentsChanged(javax.swing.event.ListDataEvent e) { updateCount(); }
            private void updateCount() {
                countLabel.setText(channelListModel.getSize() + " canal(aux)");
            }
        });
    }

    /**
     * Refiltre la liste affichée selon le texte dans le champ de recherche.
     * On cherche dans le nom du canal (insensible à la casse).
     */
    private void applyFilter() {
        String query = searchField.getText().trim().toLowerCase();
        channelListModel.clear();
        for (Channel c : allChannels) {
            if (query.isEmpty() || c.getName().toLowerCase().contains(query)) {
                channelListModel.addElement(c);
            }
        }
    }

    /**
     * Retourne le canal sélectionné.
     *
     * @return Le canal sélectionné ou null
     */
    public Channel getSelectedChannel() {
        return channelList.getSelectedValue();
    }

    @Override
    public void notifyChannelAdded(Channel addedChannel) {
        SwingUtilities.invokeLater(() -> {
            boolean exists = allChannels.stream().anyMatch(c -> c.getUuid().equals(addedChannel.getUuid()));
            if (!exists) {
                allChannels.add(addedChannel);
            }
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
        // Non utilisé
    }

    @Override
    public void notifyMessageDeleted(Message deletedMessage) {
        // Non utilisé
    }

    @Override
    public void notifyMessageModified(Message modifiedMessage) {
        // Non utilisé
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

    /**
     * Renderer personnalisé pour les canaux.
     */
    private class ChannelListCellRenderer extends DefaultListCellRenderer {
        private  final Color COLOR_CHANNEL = new Color(109, 40, 217);

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                                                      int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (value instanceof Channel) {
                Channel channel = (Channel) value;
                setText("  #  " + channel.getName());
                setFont(new Font("SansSerif", Font.PLAIN, 12));
                setToolTipText("Créateur : " + channel.getCreator().getName());
            }

            setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
            if (!isSelected) {
                setBackground(index % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                setForeground(COLOR_CHANNEL);
            }
            return this;
        }
    }


    /**
     * Ajoute un listener pour détecter la sélection d'un canal.
     *
     * @param listener Le listener à ajouter
     */
    public void addSelectionListener(javax.swing.event.ListSelectionListener listener) {
        channelList.addListSelectionListener(listener);
    }
}