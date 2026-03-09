package main.java.com.ubo.tp.message.ihm.panels;

import main.java.com.ubo.tp.message.core.DataManager;
import main.java.com.ubo.tp.message.core.SessionManager;
import main.java.com.ubo.tp.message.core.database.IDatabaseObserver;
import main.java.com.ubo.tp.message.datamodel.Channel;
import main.java.com.ubo.tp.message.datamodel.Message;
import main.java.com.ubo.tp.message.datamodel.User;
import main.java.com.ubo.tp.message.ihm.dialog.ChannelCreationDialog;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

/**
 * Panel d'affichage de la liste des canaux.
 *
 * @author BRAHIM
 */
public class ChannelListPanel extends JPanel implements IDatabaseObserver {

    private DefaultListModel<Channel> channelListModel;
    private JList<Channel> channelList;
    private DataManager dataManager;
    private JLabel countLabel;

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
        setBorder(new TitledBorder("Canaux disponibles"));

        channelListModel = new DefaultListModel<>();
        channelList = new JList<>(channelListModel);
        channelList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        channelList.setCellRenderer(new ChannelListCellRenderer());

        JScrollPane scrollPane = new JScrollPane(channelList);
        add(scrollPane, BorderLayout.CENTER);

        // Panel du bas avec le bouton "Nouveau"
        JPanel bottomPanel = new JPanel(new BorderLayout());

        // Compteur
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        countLabel = new JLabel("0 canal(aux)");
        infoPanel.add(countLabel);

        // Boutons du bas (nouveau + supprimer)
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 2, 0));

        JButton newChannelButton = new JButton("+");
        newChannelButton.setToolTipText("Créer un nouveau canal");
        newChannelButton.setPreferredSize(new Dimension(45, 25));
        newChannelButton.addActionListener(e -> createNewChannel());

        JButton deleteChannelButton = new JButton("🗑");
        deleteChannelButton.setToolTipText("Supprimer le canal sélectionné");
        deleteChannelButton.setPreferredSize(new Dimension(45, 25));
        deleteChannelButton.addActionListener(e -> deleteSelectedChannel());

        buttonsPanel.add(newChannelButton);
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
     * Retourne le canal sélectionné.
     *
     * @return Le canal sélectionné ou null
     */
    public Channel getSelectedChannel() {
        return channelList.getSelectedValue();
    }

    @Override
    public void notifyChannelAdded(Channel addedChannel) {
        System.out.println("[CHANNEL_LIST] Notification reçue : canal ajouté - " + addedChannel.getName());

        SwingUtilities.invokeLater(() -> {
            if (!channelListModel.contains(addedChannel)) {
                channelListModel.addElement(addedChannel);
                System.out.println("[CHANNEL_LIST] Canal ajouté à la liste : " + addedChannel.getName());
            } else {
                System.out.println("[CHANNEL_LIST] Canal déjà présent dans la liste");
            }
        });
    }

    @Override
    public void notifyChannelDeleted(Channel deletedChannel) {
        SwingUtilities.invokeLater(() -> {
            channelListModel.removeElement(deletedChannel);
        });
    }

    @Override
    public void notifyChannelModified(Channel modifiedChannel) {
        SwingUtilities.invokeLater(() -> {
            int index = channelListModel.indexOf(modifiedChannel);
            if (index >= 0) {
                channelListModel.set(index, modifiedChannel);
            }
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
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                                                      int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (value instanceof Channel) {
                Channel channel = (Channel) value;
                setText("# " + channel.getName());
                setToolTipText("Créateur: " + channel.getCreator().getName());
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