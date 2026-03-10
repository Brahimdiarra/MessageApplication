package main.java.com.ubo.tp.message.ihm;

import main.java.com.ubo.tp.message.core.database.IDatabase;
import main.java.com.ubo.tp.message.datamodel.Channel;
import main.java.com.ubo.tp.message.datamodel.User;
import main.java.com.ubo.tp.message.ihm.dialog.UserProfileDialog;
import main.java.com.ubo.tp.message.ihm.panels.ChannelListPanel;
import main.java.com.ubo.tp.message.ihm.panels.MessageListPanel;
import main.java.com.ubo.tp.message.ihm.panels.MessageSendPanel;
import main.java.com.ubo.tp.message.ihm.panels.UserListPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

/**
 * Vue principale de l'application de messagerie.
 *
 * @author S.Lucas / BRAHIM
 */
public class MessageAppMainView extends JFrame {

    private static final String APP_TITLE = "Application de Messagerie";
    private static final String APP_VERSION = "Version 1.0";
    private static final String APP_AUTHOR = "Développé par BRAHIM";

    // ── Palette de couleurs de l'application ─────────────────────────────
    public static final Color COLOR_PRIMARY    = new Color(30, 58, 138);  // bleu foncé header
    public static final Color COLOR_ACCENT     = new Color(59, 130, 246); // bleu vif bordures/titres
    public static final Color COLOR_BG         = new Color(248, 250, 252); // fond général très clair
    public static final Color COLOR_PANEL_BG   = Color.WHITE;

    private JLabel headerUserLabel;

    private JMenuItem menuItemQuit;
    private JMenuItem menuItemAbout;
    private JMenuItem menuItemSelectDirectory;
    private JMenuItem menuItemLogout;
    private JMenuItem menuItemProfile;

    private MessageApp messageApp;
    private AuthenticationView authenticationView;
    private User currentUser;

    // Panels d'affichage
    private UserListPanel userListPanel;
    private ChannelListPanel channelListPanel;
    private MessageListPanel messageListPanel;
    private MessageSendPanel sendPanel;

    /**
     * Constructeur.
     *
     * @param messageApp  Instance de l'application
     * @param currentUser Utilisateur connecté (peut être null avant connexion)
     */
    public MessageAppMainView(MessageApp messageApp, User currentUser) {
        this.messageApp = messageApp;
        this.currentUser = currentUser;
        initComponents();
    }

    /**
     * Initialisation des composants de la fenêtre.
     */
    private void initComponents() {
        // Configuration de la fenêtre principale
        setTitle(APP_TITLE);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);

        // Icône de l'application
        ImageIcon appIcon = loadIcon("logo_20.png");
        if (appIcon != null) {
            setIconImage(appIcon.getImage());
        }

        // Création de la barre de menu
        setJMenuBar(createMenuBar());

        // Barre d'en-tête colorée
        add(createHeaderPanel(), BorderLayout.NORTH);

        // Panel principal avec les listes
        JPanel mainPanel = createMainContentPanel();
        mainPanel.setBackground(COLOR_BG);
        add(mainPanel, BorderLayout.CENTER);

        // Ne pas afficher la fenêtre tout de suite (sera affichée après login)
        setVisible(false);
    }

    /**
     * Définit la vue d'authentification.
     *
     * @param authView La vue d'authentification
     */
    public void setAuthenticationView(AuthenticationView authView) {
        this.authenticationView = authView;
    }

    /**
     * Affiche la vue principale après connexion réussie.
     *
     * @param userTag Tag de l'utilisateur connecté
     */
    public void showMainView(String userTag) {
        // Mettre à jour le titre et le header
        setTitle(APP_TITLE + " - Connecté en tant que @" + userTag);
        if (headerUserLabel != null) {
            headerUserLabel.setText("Connecté en tant que  @" + userTag);
        }

        // Enregistrer les observateurs
        try {
            if (messageApp != null && messageApp.mDataManager != null) {
                IDatabase database = messageApp.mDataManager.getDatabase();
                registerObservers(database);
            }
        } catch (Exception e) {
            System.err.println("[ERREUR] Impossible d'enregistrer les observateurs : " + e.getMessage());
            e.printStackTrace();
        }

        // Afficher la fenêtre principale
        setVisible(true);

        System.out.println("[INFO] Vue principale affichée pour : " + userTag);
    }



    /**
     * Enregistre les panels comme observateurs de la base de données.
     *
     * @param database La base de données
     */
    private void registerObservers(IDatabase database) {
        if (database != null) {
            database.addObserver(userListPanel);
            database.addObserver(channelListPanel);
            database.addObserver(messageListPanel);
            if (sendPanel != null) {
                database.addObserver(sendPanel);
            }
            System.out.println("[INFO] Observateurs IHM enregistrés avec succès");
        } else {
            System.err.println("[ERREUR] Database null - impossible d'enregistrer les observateurs");
        }
    }

    /**
     * Crée la barre de menu.
     *
     * @return La barre de menu
     */
    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // Menu Fichier
        JMenu menuFile = new JMenu("Fichier");
        menuFile.setMnemonic('F');

        // Menu item : Sélectionner répertoire
        menuItemSelectDirectory = new JMenuItem("Sélectionner le répertoire d'échange");
        menuItemSelectDirectory.setIcon(loadIcon("editIcon_20.png"));
        menuItemSelectDirectory.setAccelerator(KeyStroke.getKeyStroke("ctrl D"));
        menuItemSelectDirectory.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectExchangeDirectory();
            }
        });
        menuFile.add(menuItemSelectDirectory);

        menuFile.addSeparator();

        // Menu item : Mon profil
        menuItemProfile = new JMenuItem("Mon profil");
        menuItemProfile.setIcon(loadIcon("editIcon_20.png"));
        menuItemProfile.setAccelerator(KeyStroke.getKeyStroke("ctrl P"));
        menuItemProfile.addActionListener(e -> openUserProfile());
        menuFile.add(menuItemProfile);

        menuFile.addSeparator();

        // Menu item : Déconnexion
        menuItemLogout = new JMenuItem("Déconnexion");
        menuItemLogout.setIcon(loadIcon("exitIcon_20.png"));
        menuItemLogout.setAccelerator(KeyStroke.getKeyStroke("ctrl L"));
        menuItemLogout.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logout();
            }
        });
        menuFile.add(menuItemLogout);

        menuFile.addSeparator();

        // Menu item : Quitter
        menuItemQuit = new JMenuItem("Quitter");
        menuItemQuit.setIcon(loadIcon("exitIcon_20.png"));
        menuItemQuit.setAccelerator(KeyStroke.getKeyStroke("ctrl Q"));
        menuItemQuit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                quitApplication();
            }
        });
        menuFile.add(menuItemQuit);

        // Menu Aide
        JMenu menuHelp = new JMenu("Aide");
        menuHelp.setMnemonic('A');

        // Menu item : À propos
        menuItemAbout = new JMenuItem("À propos");
        menuItemAbout.setIcon(loadIcon("editIcon_20.png"));
        menuItemAbout.setAccelerator(KeyStroke.getKeyStroke("F1"));
        menuItemAbout.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showAboutDialog();
            }
        });
        menuHelp.add(menuItemAbout);

        menuBar.add(menuFile);
        menuBar.add(menuHelp);

        return menuBar;
    }

    /**
     * Affiche la boîte de dialogue "À propos".
     */
    private void showAboutDialog() {
        JPanel aboutPanel = new JPanel();
        aboutPanel.setLayout(new BoxLayout(aboutPanel, BoxLayout.Y_AXIS));
        aboutPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        ImageIcon logoIcon = loadIcon("logo_20.png");
        JLabel logoLabel = new JLabel(logoIcon != null ? logoIcon : new ImageIcon());
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        aboutPanel.add(logoLabel);

        aboutPanel.add(Box.createVerticalStrut(20));

        JLabel titleLabel = new JLabel(APP_TITLE);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        aboutPanel.add(titleLabel);

        aboutPanel.add(Box.createVerticalStrut(10));

        JLabel versionLabel = new JLabel(APP_VERSION);
        versionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        aboutPanel.add(versionLabel);

        aboutPanel.add(Box.createVerticalStrut(10));

        JLabel authorLabel = new JLabel(APP_AUTHOR);
        authorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        aboutPanel.add(authorLabel);

        aboutPanel.add(Box.createVerticalStrut(10));

        JTextArea descriptionArea = new JTextArea(
                "Application de messagerie développée dans le cadre\n" +
                        "du Master 2 TIIL-A à l'Université de Bretagne Occidentale.\n\n" +
                        "Cette application permet l'échange de messages\n" +
                        "entre utilisateurs via un répertoire partagé."
        );
        descriptionArea.setEditable(false);
        descriptionArea.setOpaque(false);
        descriptionArea.setAlignmentX(Component.CENTER_ALIGNMENT);
        descriptionArea.setFont(new Font("Arial", Font.PLAIN, 12));
        aboutPanel.add(descriptionArea);

        JOptionPane.showMessageDialog(
                this,
                aboutPanel,
                "À propos",
                JOptionPane.PLAIN_MESSAGE
        );
    }

    /**
     * Crée la barre d'en-tête colorée affichant l'utilisateur connecté.
     */
    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout(10, 0));
        header.setBackground(COLOR_PRIMARY);
        header.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));

        // Logo / titre à gauche
        JLabel appLabel = new JLabel("💬  " + APP_TITLE);
        appLabel.setFont(new Font("SansSerif", Font.BOLD, 15));
        appLabel.setForeground(Color.WHITE);

        // Utilisateur connecté à droite
        headerUserLabel = new JLabel("");
        headerUserLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        headerUserLabel.setForeground(new Color(186, 207, 255));

        header.add(appLabel, BorderLayout.WEST);
        header.add(headerUserLabel, BorderLayout.EAST);
        return header;
    }

    /**
     * Ouvre le dialogue de modification du profil utilisateur (USR-009).
     */
    private void openUserProfile() {
        if (messageApp == null || messageApp.mDataManager == null) {
            JOptionPane.showMessageDialog(this,
                    "Erreur : DataManager non initialisé.",
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }
        UserProfileDialog.showDialog(this, messageApp.mDataManager);
    }

    /**
     * Affiche le sélecteur de répertoire d'échange.
     */
    private void selectExchangeDirectory() {
        JFileChooser fileChooser = new JFileChooser();

        fileChooser.setDialogTitle("Sélectionner le répertoire d'échange");
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setAcceptAllFileFilterUsed(false);

        String userHome = System.getProperty("user.home");
        fileChooser.setCurrentDirectory(new File(userHome));

        int result = fileChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedDirectory = fileChooser.getSelectedFile();

            if (messageApp.isValidExchangeDirectory(selectedDirectory)) {
                messageApp.initDirectory(selectedDirectory.getAbsolutePath());
                JOptionPane.showMessageDialog(
                        this,
                        "Répertoire d'échange configuré : \n" + selectedDirectory.getAbsolutePath(),
                        "Succès",
                        JOptionPane.INFORMATION_MESSAGE
                );
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "Le répertoire sélectionné n'est pas valide.\n" +
                                "Assurez-vous qu'il existe et qu'il est accessible en lecture/écriture.",
                        "Erreur",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }

    /**
     * Déconnexion de l'utilisateur.
     */
    private void logout() {
        int result = JOptionPane.showConfirmDialog(
                this,
                "Voulez-vous vraiment vous déconnecter ?",
                "Confirmation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (result == JOptionPane.YES_OPTION) {
            System.out.println("[INFO] Déconnexion de l'utilisateur");

            // Cacher la vue principale
            setVisible(false);

            // Afficher la vue d'authentification
            if (authenticationView != null) {
                authenticationView.displayWindow();
            }
        }
    }

    /**
     * Quitte l'application après confirmation.
     */
    private void quitApplication() {
        int result = JOptionPane.showConfirmDialog(
                this,
                "Voulez-vous vraiment quitter l'application ?",
                "Confirmation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (result == JOptionPane.YES_OPTION) {
            System.out.println("[INFO] Fermeture de l'application...");
            System.exit(0);
        }
    }

    /**
     * Charge une icône depuis les ressources avec plusieurs méthodes de fallback.
     *
     * @param filename Nom du fichier d'icône
     * @return L'icône chargée ou une icône par défaut
     */
    private ImageIcon loadIcon(String filename) {
        java.net.URL imgURL = null;

        imgURL = getClass().getClassLoader().getResource("images/" + filename);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        }

        imgURL = getClass().getResource("/images/" + filename);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        }

        imgURL = getClass().getClassLoader().getResource(filename);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        }

        try {
            String[] possiblePaths = {
                    "src/main/resources/images/" + filename,
                    "resources/images/" + filename,
                    "images/" + filename
            };

            for (String path : possiblePaths) {
                File imageFile = new File(path);
                if (imageFile.exists()) {
                    return new ImageIcon(imageFile.getAbsolutePath());
                }
            }
        } catch (Exception e) {
            // Silencieux
        }

        return createDefaultIcon();
    }

    /**
     * Crée une icône par défaut.
     *
     * @return Une icône colorée simple
     */
    private ImageIcon createDefaultIcon() {
        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(
                16, 16, java.awt.image.BufferedImage.TYPE_INT_ARGB
        );
        Graphics2D g2d = img.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(new Color(70, 130, 180));
        g2d.fillOval(2, 2, 12, 12);
        g2d.dispose();
        return new ImageIcon(img);
    }

    /**
     * Affiche la fenêtre principale.
     * (Appelé par AuthenticationView)
     */
    public void displayWindow() {
        setVisible(true);
        System.out.println("[INFO] Fenêtre principale affichée");
    }

    /**
     * Ferme la fenêtre principale.
     * (Appelé par AuthenticationView lors de la déconnexion)
     */
    public void close() {
        setVisible(false);
        System.out.println("[INFO] Fenêtre principale fermée");
    }

    /**
     * Crée le panel principal avec les listes.
     */
    private JPanel createMainContentPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(5, 5));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Panel de gauche : Utilisateurs + Canaux
        JPanel leftPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        leftPanel.setPreferredSize(new Dimension(250, 0));

        userListPanel = new UserListPanel();
        channelListPanel = new ChannelListPanel();
        if (messageApp != null && messageApp.mDataManager != null) {
            channelListPanel.setDataManager(messageApp.mDataManager);
        }

        leftPanel.add(userListPanel);
        leftPanel.add(channelListPanel);

        // Panel central : Messages + Panel d'envoi
        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));

        messageListPanel = new MessageListPanel();
        messageListPanel.setMessageApp(messageApp); // IMPORTANT
        centerPanel.add(messageListPanel, BorderLayout.CENTER);

        // AJOUTER LE PANEL D'ENVOI ICI
        if (messageApp != null && messageApp.mDataManager != null) {
            sendPanel = new MessageSendPanel(messageApp.mDataManager);
            sendPanel.setPreferredSize(new Dimension(0, 150));
            centerPanel.add(sendPanel, BorderLayout.SOUTH);
        }

        // Ajout des panels
        mainPanel.add(leftPanel, BorderLayout.WEST);
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // INTERACTEURS : Sélection d'un utilisateur
        userListPanel.addSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                User selectedUser = userListPanel.getSelectedUser();
                if (selectedUser != null) {
                    System.out.println("[INFO] Utilisateur sélectionné : " + selectedUser.getUserTag());
                    messageListPanel.filterByUser(selectedUser.getUuid());
                }
            }
        });

        // INTERACTEURS : Sélection d'un canal
        channelListPanel.addSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Channel selectedChannel = channelListPanel.getSelectedChannel();
                if (selectedChannel != null) {
                    System.out.println("[INFO] Canal sélectionné : " + selectedChannel.getName());
                    messageListPanel.filterByChannel(selectedChannel.getUuid());
                }
            }
        });

        return mainPanel;
    }
}