package main.java.com.ubo.tp.message.ihm.dialog;

import main.java.com.ubo.tp.message.core.DataManager;
import main.java.com.ubo.tp.message.core.SessionManager;
import main.java.com.ubo.tp.message.datamodel.Channel;
import main.java.com.ubo.tp.message.datamodel.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Dialogue de création d'un nouveau canal.
 *
 * @author BRAHIM
 */
public class ChannelCreationDialog extends JDialog {
    private static final long serialVersionUID = 1L;

    private JTextField channelNameField;
    private JRadioButton publicRadio;
    private JRadioButton privateRadio;
    private JButton createButton;
    private JButton cancelButton;

    private DataManager dataManager;
    private boolean confirmed = false;

    /**
     * Constructeur.
     *
     * @param parent      Fenêtre parente
     * @param dataManager Gestionnaire de données
     */
    public ChannelCreationDialog(Frame parent, DataManager dataManager) {
        super(parent, "Créer un nouveau canal", true);
        this.dataManager = dataManager;
        initComponents();
    }

    /**
     * Initialisation des composants.
     */
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setSize(400, 200);
        setLocationRelativeTo(getParent());
        setResizable(false);

        // Panel principal
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Nom du canal
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.3;
        mainPanel.add(new JLabel("Nom du canal :"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        channelNameField = new JTextField(20);
        mainPanel.add(channelNameField, gbc);

        // Type de canal
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.3;
        mainPanel.add(new JLabel("Type :"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        JPanel typePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        publicRadio = new JRadioButton("Public", true);
        privateRadio = new JRadioButton("Privé");

        ButtonGroup typeGroup = new ButtonGroup();
        typeGroup.add(publicRadio);
        typeGroup.add(privateRadio);

        typePanel.add(publicRadio);
        typePanel.add(privateRadio);
        mainPanel.add(typePanel, gbc);

        add(mainPanel, BorderLayout.CENTER);

        // Panel des boutons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 15, 15, 15));

        cancelButton = new JButton("Annuler");
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                confirmed = false;
                dispose();
            }
        });

        createButton = new JButton("Créer");
        createButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                createChannel();
            }
        });

        buttonPanel.add(cancelButton);
        buttonPanel.add(createButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // Valider avec Entrée
        getRootPane().setDefaultButton(createButton);
    }

    /**
     * Crée le canal.
     */
    private void createChannel() {
        String channelName = channelNameField.getText().trim();

        // Validation
        if (channelName.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Le nom du canal ne peut pas être vide !",
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Vérifier que le nom ne contient que des caractères alphanumériques et tirets
        if (!channelName.matches("[a-zA-Z0-9_-]+")) {
            JOptionPane.showMessageDialog(
                    this,
                    "Le nom du canal ne peut contenir que des lettres, chiffres, tirets et underscores.",
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
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

        try {
            // Créer le canal
            Channel newChannel;

            if (publicRadio.isSelected()) {
                // Canal public
                newChannel = new Channel(currentUser, channelName);
            } else {
                // Canal privé (pour l'instant, juste avec le créateur)
                List<User> members = new ArrayList<>();
                members.add(currentUser);
                newChannel = new Channel(currentUser, channelName, members);
            }

            // Envoyer le canal via le DataManager
            dataManager.sendChannel(newChannel);

            System.out.println("[CHANNEL] Canal créé : " + channelName +
                    " (type: " + (publicRadio.isSelected() ? "public" : "privé") + ")");

            // Confirmation
            confirmed = true;
            dispose();

            JOptionPane.showMessageDialog(
                    getParent(),
                    "Canal \"" + channelName + "\" créé avec succès !",
                    "Succès",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            System.err.println("[ERREUR] Échec de la création du canal : " + e.getMessage());
            e.printStackTrace();

            JOptionPane.showMessageDialog(
                    this,
                    "Erreur lors de la création du canal : " + e.getMessage(),
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Indique si l'utilisateur a confirmé la création.
     *
     * @return true si confirmé
     */
    public boolean isConfirmed() {
        return confirmed;
    }

    /**
     * Affiche le dialogue et retourne true si un canal a été créé.
     *
     * @param parent      Fenêtre parente
     * @param dataManager Gestionnaire de données
     * @return true si un canal a été créé
     */
    public static boolean showDialog(Frame parent, DataManager dataManager) {
        ChannelCreationDialog dialog = new ChannelCreationDialog(parent, dataManager);
        dialog.setVisible(true);
        return dialog.isConfirmed();
    }
}