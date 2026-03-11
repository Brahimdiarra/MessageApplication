package main.java.com.ubo.tp.message.ihm;

import main.java.com.ubo.tp.message.core.database.IDatabase;
import main.java.com.ubo.tp.message.datamodel.Channel;
import main.java.com.ubo.tp.message.datamodel.User;
import main.java.com.ubo.tp.message.ihm.dialog.UserProfileDialog;
import main.java.com.ubo.tp.message.ihm.notification.NotificationManager;
import main.java.com.ubo.tp.message.ihm.panels.ChannelListPanel;
import main.java.com.ubo.tp.message.ihm.panels.MessageListPanel;
import main.java.com.ubo.tp.message.ihm.panels.MessageSendPanel;
import main.java.com.ubo.tp.message.ihm.panels.UserListPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;
import main.java.com.ubo.tp.message.ihm.easteregg.EasterEggManager;
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

    // ── Palette claire (ancienne, gardée pour compatibilité) ─────────────
    public static final Color COLOR_PRIMARY    = new Color(32, 34, 37);   // dark header
    public static final Color COLOR_ACCENT     = new Color(88, 101, 242); // Discord blurple
    public static final Color COLOR_BG         = new Color(54, 57, 63);   // Discord message area
    public static final Color COLOR_PANEL_BG   = new Color(47, 49, 54);   // Discord sidebar

    // ── Palette Discord ───────────────────────────────────────────────────
    public static final Color DISCORD_SIDEBAR       = new Color(47, 49, 54);
    public static final Color DISCORD_SIDEBAR_DARK  = new Color(32, 34, 37);
    public static final Color DISCORD_CHAT_BG       = new Color(54, 57, 63);
    public static final Color DISCORD_TEXT          = new Color(220, 221, 222);
    public static final Color DISCORD_TEXT_MUTED    = new Color(148, 155, 164);
    public static final Color DISCORD_ONLINE        = new Color(59, 165, 92);
    public static final Color DISCORD_OFFLINE       = new Color(116, 127, 141);

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

    /** Label d'en-tête de la conversation active (centre). */
    private JLabel conversationTitleLabel;

    /** Panel droit : membres en ligne. */
    private DefaultListModel<User> onlineMembersModel;
    private JList<User> onlineMembersList;

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
                // Charger les aperçus de derniers messages dans les sidebars
                userListPanel.refreshLastMessages(messageApp.mDataManager);
                channelListPanel.refreshLastMessages(messageApp.mDataManager);
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
     */
    private void registerObservers(IDatabase database) {
        if (database != null) {
            database.addObserver(userListPanel);
            database.addObserver(channelListPanel);
            database.addObserver(messageListPanel);
            if (sendPanel != null) database.addObserver(sendPanel);
            // Membres en ligne : observer les users
            database.addObserver(new main.java.com.ubo.tp.message.core.database.IDatabaseObserver() {
                public void notifyUserAdded(User u)       { updateOnlineMembers(); }
                public void notifyUserDeleted(User u)     { updateOnlineMembers(); }
                public void notifyUserModified(User u)    { updateOnlineMembers(); }
                public void notifyMessageAdded(main.java.com.ubo.tp.message.datamodel.Message m)    { }
                public void notifyMessageDeleted(main.java.com.ubo.tp.message.datamodel.Message m)  { }
                public void notifyMessageModified(main.java.com.ubo.tp.message.datamodel.Message m) { }
                public void notifyChannelAdded(Channel c)    { }
                public void notifyChannelDeleted(Channel c)  { }
                public void notifyChannelModified(Channel c) { }
            });
            database.addObserver(new NotificationManager(this,
                    sendPanel != null ? sendPanel::getCurrentRecipientUuid : () -> null));
            database.addObserver(new EasterEggManager(this));
            System.out.println("[INFO] Observateurs IHM enregistrés avec succès");
        } else {
            System.err.println("[ERREUR] Database null - impossible d'enregistrer les observateurs");
        }
    }

    /** Rafraîchit la liste des membres en ligne dans la sidebar droite. */
    private void updateOnlineMembers() {
        if (onlineMembersModel == null || messageApp == null || messageApp.mDataManager == null) return;
        SwingUtilities.invokeLater(() -> {
            onlineMembersModel.clear();
            User me = main.java.com.ubo.tp.message.core.SessionManager.getInstance().getCurrentUser();
            for (User u : messageApp.mDataManager.getUsers()) {
                if (u.isOnline() && (me == null || !u.getUuid().equals(me.getUuid()))) {
                    onlineMembersModel.addElement(u);
                }
            }
        });
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
        JPanel header = new JPanel(new BorderLayout(10, 0)) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new GradientPaint(0, 0, new Color(40, 43, 51), getWidth(), 0, new Color(28, 30, 36)));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        header.setOpaque(false);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(15, 15, 20)),
                BorderFactory.createEmptyBorder(9, 16, 9, 16)));

        JLabel appLabel = new JLabel("💬  " + APP_TITLE);
        appLabel.setFont(new Font("SansSerif", Font.BOLD, 15));
        appLabel.setForeground(Color.WHITE);

        headerUserLabel = new JLabel("");
        headerUserLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        headerUserLabel.setForeground(new Color(114, 137, 218)); // Discord blurple clair

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
        UserProfileDialog.showDialog(this, messageApp.mDataManager, () -> {
            setVisible(false);
            if (authenticationView != null) {
                authenticationView.displayWindow();
            }
        });
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
     * Crée le panel principal — layout 3 colonnes style Discord.
     */
    private JPanel createMainContentPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.setBackground(DISCORD_CHAT_BG);

        // ── SIDEBAR GAUCHE (canaux + utilisateurs) ────────────────────────
        JPanel leftSidebar = new JPanel(new BorderLayout(0, 0));
        leftSidebar.setPreferredSize(new Dimension(240, 0));
        leftSidebar.setBackground(DISCORD_SIDEBAR);
        leftSidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(32, 34, 37)));

        // En-tête sidebar
        JPanel sidebarHeader = new JPanel(new BorderLayout());
        sidebarHeader.setBackground(DISCORD_SIDEBAR_DARK);
        sidebarHeader.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(20, 20, 20)),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)));
        JLabel sidebarTitle = new JLabel("💬 Messagerie");
        sidebarTitle.setFont(new Font("SansSerif", Font.BOLD, 14));
        sidebarTitle.setForeground(Color.WHITE);
        sidebarHeader.add(sidebarTitle, BorderLayout.WEST);
        leftSidebar.add(sidebarHeader, BorderLayout.NORTH);

        // Section CANAUX
        JLabel channelsSectionLabel = makeSectionHeader("CANAUX");
        channelListPanel = new ChannelListPanel();
        channelListPanel.setDarkMode(true);
        if (messageApp != null && messageApp.mDataManager != null) {
            channelListPanel.setDataManager(messageApp.mDataManager);
        }

        // Section MESSAGES PRIVÉS
        JLabel dmSectionLabel = makeSectionHeader("MESSAGES PRIVÉS");
        userListPanel = new UserListPanel();
        userListPanel.setDarkMode(true);

        // Split channels / users dans le sidebar
        JSplitPane sidebarSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        sidebarSplit.setBorder(null);
        sidebarSplit.setDividerSize(3);
        sidebarSplit.setBackground(DISCORD_SIDEBAR);
        sidebarSplit.setDividerLocation(280);
        sidebarSplit.setResizeWeight(0.5);

        JPanel channelSection = new JPanel(new BorderLayout());
        channelSection.setBackground(DISCORD_SIDEBAR);
        channelSection.add(channelsSectionLabel, BorderLayout.NORTH);
        channelSection.add(channelListPanel, BorderLayout.CENTER);

        JPanel dmSection = new JPanel(new BorderLayout());
        dmSection.setBackground(DISCORD_SIDEBAR);
        dmSection.add(dmSectionLabel, BorderLayout.NORTH);
        dmSection.add(userListPanel, BorderLayout.CENTER);

        sidebarSplit.setTopComponent(channelSection);
        sidebarSplit.setBottomComponent(dmSection);
        leftSidebar.add(sidebarSplit, BorderLayout.CENTER);

        // ── CENTRE (header + messages + envoi) ───────────────────────────
        JPanel centerPanel = new JPanel(new BorderLayout(0, 0));
        centerPanel.setBackground(DISCORD_CHAT_BG);

        // Header de conversation
        JPanel conversationHeader = new JPanel(new BorderLayout());
        conversationHeader.setBackground(new Color(44, 47, 53));
        conversationHeader.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(24, 26, 30)),
                BorderFactory.createEmptyBorder(10, 16, 10, 16)));
        conversationTitleLabel = new JLabel("  Sélectionnez une conversation...");
        conversationTitleLabel.setFont(new Font("SansSerif", Font.BOLD, 15));
        conversationTitleLabel.setForeground(DISCORD_TEXT_MUTED);
        conversationHeader.add(conversationTitleLabel, BorderLayout.WEST);
        centerPanel.add(conversationHeader, BorderLayout.NORTH);

        // Liste de messages
        messageListPanel = new MessageListPanel();
        messageListPanel.setMessageApp(messageApp);
        centerPanel.add(messageListPanel, BorderLayout.CENTER);

        // Panel d'envoi
        if (messageApp != null && messageApp.mDataManager != null) {
            sendPanel = new MessageSendPanel(messageApp.mDataManager);
            sendPanel.setPreferredSize(new Dimension(0, 160));
            centerPanel.add(sendPanel, BorderLayout.SOUTH);
        }

        // ── SIDEBAR DROITE (membres en ligne) ─────────────────────────────
        JPanel rightSidebar = createOnlineMembersPanel();
        rightSidebar.setPreferredSize(new Dimension(200, 0));

        // ── Assemblage ────────────────────────────────────────────────────
        mainPanel.add(leftSidebar, BorderLayout.WEST);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(rightSidebar, BorderLayout.EAST);

        // ── Listeners de sélection ────────────────────────────────────────

        // Clic sur un utilisateur → ouvre la conversation DM
        userListPanel.addSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                User selectedUser = userListPanel.getSelectedUser();
                if (selectedUser != null) {
                    String display = "@" + selectedUser.getUserTag();
                    conversationTitleLabel.setText("  " + display);
                    conversationTitleLabel.setForeground(DISCORD_TEXT);
                    messageListPanel.filterByUser(selectedUser.getUuid());
                    userListPanel.markAsRead(selectedUser.getUuid());
                    if (sendPanel != null)
                        sendPanel.setRecipient(selectedUser.getUuid(), display);
                    // Désélectionner la liste des canaux
                    channelListPanel.clearSelection();
                }
            }
        });

        // Clic sur un canal → ouvre la conversation canal
        channelListPanel.addSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Channel selectedChannel = channelListPanel.getSelectedChannel();
                if (selectedChannel != null) {
                    User me = main.java.com.ubo.tp.message.core.SessionManager.getInstance().getCurrentUser();
                    String display = (selectedChannel.isPrivate() ? "🔒 #" : "# ") + selectedChannel.getName();
                    conversationTitleLabel.setText("  " + display);
                    conversationTitleLabel.setForeground(DISCORD_TEXT);
                    messageListPanel.filterByChannel(selectedChannel.getUuid());
                    channelListPanel.markAsRead(selectedChannel.getUuid());
                    if (sendPanel != null) {
                        if (selectedChannel.isMember(me)) {
                            sendPanel.setRecipient(selectedChannel.getUuid(), display);
                        } else {
                            sendPanel.clearRecipient();
                        }
                    }
                    userListPanel.clearSelection();
                }
            }
        });

        return mainPanel;
    }

    /** Crée un label de section style Discord (ex: "CANAUX"). */
    private JLabel makeSectionHeader(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 10));
        lbl.setForeground(new Color(148, 155, 164));
        lbl.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(24, 26, 30)),
                BorderFactory.createEmptyBorder(10, 14, 6, 14)));
        lbl.setOpaque(true);
        lbl.setBackground(new Color(32, 34, 37));
        return lbl;
    }

    /** Crée le panel de droite affichant les membres en ligne. */
    private JPanel createOnlineMembersPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 0));
        panel.setBackground(DISCORD_SIDEBAR);
        panel.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, new Color(32, 34, 37)));

        // En-tête
        JLabel header = new JLabel("  EN LIGNE");
        header.setFont(new Font("SansSerif", Font.BOLD, 10));
        header.setForeground(new Color(148, 155, 164));
        header.setOpaque(true);
        header.setBackground(new Color(32, 34, 37));
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(20, 20, 20)),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        panel.add(header, BorderLayout.NORTH);

        // Liste
        onlineMembersModel = new DefaultListModel<>();
        onlineMembersList = new JList<>(onlineMembersModel);
        onlineMembersList.setBackground(DISCORD_SIDEBAR);
        onlineMembersList.setFixedCellHeight(44);
        onlineMembersList.setCellRenderer(new ListCellRenderer<User>() {
            private final Color[] AVATAR_COLORS = {
                new Color(88, 101, 242), new Color(59, 165, 92),  new Color(237, 66, 69),
                new Color(250, 168, 26), new Color(235, 69, 158), new Color(149, 128, 255),
                new Color(32, 200, 255), new Color(255, 115, 55),
            };
            @Override
            public Component getListCellRendererComponent(JList<? extends User> list, User user,
                                                          int index, boolean isSelected, boolean cellHasFocus) {
                JPanel cell = new JPanel(new BorderLayout(8, 0));
                cell.setBackground(DISCORD_SIDEBAR);
                cell.setOpaque(true);
                cell.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

                // Avatar
                JPanel avatar = new JPanel(null) {
                    @Override protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        int ci = Math.abs(user.getUserTag().hashCode()) % AVATAR_COLORS.length;
                        g2.setColor(AVATAR_COLORS[ci]);
                        g2.fillOval(0, 0, 32, 32);
                        g2.setColor(Color.WHITE);
                        g2.setFont(new Font("SansSerif", Font.BOLD, 14));
                        String letter = user.getName().isEmpty() ? "?" :
                                String.valueOf(user.getName().charAt(0)).toUpperCase();
                        FontMetrics fm = g2.getFontMetrics();
                        g2.drawString(letter, (32 - fm.stringWidth(letter)) / 2,
                                (32 + fm.getAscent() - fm.getDescent()) / 2);
                        g2.setColor(DISCORD_SIDEBAR);
                        g2.fillOval(20, 20, 14, 14);
                        g2.setColor(DISCORD_ONLINE);
                        g2.fillOval(22, 22, 10, 10);
                        g2.dispose();
                    }
                };
                avatar.setOpaque(false);
                avatar.setPreferredSize(new Dimension(36, 36));

                JPanel txt = new JPanel();
                txt.setOpaque(false);
                txt.setLayout(new BoxLayout(txt, BoxLayout.Y_AXIS));
                JLabel name = new JLabel("@" + user.getUserTag());
                name.setFont(new Font("SansSerif", Font.PLAIN, 13));
                name.setForeground(DISCORD_TEXT);
                JLabel sub = new JLabel(user.getName());
                sub.setFont(new Font("SansSerif", Font.PLAIN, 10));
                sub.setForeground(DISCORD_TEXT_MUTED);
                txt.add(name); txt.add(sub);

                cell.add(avatar, BorderLayout.WEST);
                cell.add(txt, BorderLayout.CENTER);
                return cell;
            }
        });
        panel.add(new JScrollPane(onlineMembersList), BorderLayout.CENTER);
        return panel;
    }

    // ─── Easter Eggs (Séance 6) ──────────────────────────────────────────────

    /**
     * /party — animation confetti pendant ~3 secondes.
     */
    public void triggerParty() {
        int w = getWidth(), h = getHeight();
        int NUM = 90;
        float[] x  = new float[NUM]; float[] y  = new float[NUM];
        float[] vx = new float[NUM]; float[] vy = new float[NUM];
        Color[] palette = {
            new Color(255,  87,  51), new Color(255, 195,   0),
            new Color( 47, 213, 133), new Color( 37,  99, 235),
            new Color(168,  85, 247), new Color(255, 110, 199)
        };
        Random rand = new Random();
        for (int i = 0; i < NUM; i++) {
            x[i]  = rand.nextFloat() * w;
            y[i]  = -rand.nextFloat() * h;
            vx[i] = (rand.nextFloat() - 0.5f) * 5;
            vy[i] = 3 + rand.nextFloat() * 5;
        }
        JWindow overlay = new JWindow(this);
        overlay.setBounds(getBounds());
        overlay.setBackground(new Color(0, 0, 0, 0));
        JPanel canvas = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                // fond transparent
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(0, 0, 0, 0));
                g2.clearRect(0, 0, getWidth(), getHeight());
                for (int i = 0; i < NUM; i++) {
                    g2.setColor(palette[i % palette.length]);
                    int size = 8 + (i % 5) * 2;
                    if (i % 3 == 0) g2.fillOval((int)x[i], (int)y[i], size, size);
                    else            g2.fillRect((int)x[i], (int)y[i], size+2, size/2+3);
                }
                g2.dispose();
            }
        };
        canvas.setOpaque(false);
        overlay.add(canvas);
        overlay.setVisible(true);
        int[] frames = {0};
        Timer t = new Timer(30, null);
        t.addActionListener(ae -> {
            for (int i = 0; i < NUM; i++) {
                x[i] += vx[i]; y[i] += vy[i];
                if (y[i] > h) { y[i] = -20; x[i] = rand.nextFloat() * w; }
            }
            canvas.repaint();
            if (++frames[0] > 100) { t.stop(); overlay.dispose(); }
        });
        t.start();
    }

    /**
     * /flip — retourne l'interface à 180° puis revient en place.
     */
    public void triggerFlip() {
        int w = getWidth(), h = getHeight();
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        printAll(img.createGraphics());
        JWindow overlay = new JWindow(this);
        overlay.setBounds(getBounds());
        double[] angle = {0};
        int[]    state = {0}; // 0=aller 1=pause 2=retour
        int[]    pause = {0};
        JPanel canvas = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(20, 20, 20));
                g2.fillRect(0, 0, getWidth(), getHeight());
                double cx = getWidth() / 2.0, cy = getHeight() / 2.0;
                g2.translate(cx, cy);
                g2.rotate(Math.toRadians(angle[0]));
                g2.translate(-cx, -cy);
                g2.drawImage(img, 0, 0, null);
                g2.dispose();
            }
        };
        overlay.add(canvas);
        overlay.setVisible(true);
        Timer t = new Timer(12, null);
        t.addActionListener(ae -> {
            switch (state[0]) {
                case 0: angle[0] += 7; if (angle[0] >= 180) { angle[0] = 180; state[0] = 1; } break;
                case 1: if (++pause[0] >= 35) state[0] = 2; break;
                case 2: angle[0] -= 7; if (angle[0] <= 0) { t.stop(); overlay.dispose(); return; } break;
            }
            canvas.repaint();
        });
        t.start();
    }

    /**
     * /earthquake — la fenêtre tremble pendant 2 secondes.
     */
    public void triggerEarthquake() {
        Point orig = getLocation();
        int[] offsets = {0, 14, -14, 10, -10, 16, -16, 8, -8, 12, -12, 6, -6, 0};
        int[] tick = {0};
        Timer t = new Timer(30, null);
        t.addActionListener(ae -> {
            int dx = offsets[tick[0] % offsets.length];
            int dy = offsets[(tick[0] + 3) % offsets.length] / 2;
            setLocation(orig.x + dx, orig.y + dy);
            if (++tick[0] >= 66) { t.stop(); setLocation(orig); }
        });
        t.start();
    }
}
