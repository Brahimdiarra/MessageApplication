package main.java.com.ubo.tp.message.ihm;

import javax.swing.*;
import java.awt.*;

/**
 * Vue d'authentification (login et inscription).
 * 
 * Cette classe gère :
 * - L'affichage de la fenêtre de connexion/inscription
 * - La logique de communication entre LoginPanel et MessageApp
 * - La transition vers la vue principale après connexion
 * - La déconnexion de l'utilisateur
 *
 * @author BRAHIM
 */
public class AuthenticationView extends JFrame {
    private static final long serialVersionUID = 1L;

    // ============================================
    // CONSTANTES
    // ============================================

    /**
     * Titre de la fenêtre d'authentification
     */
    private static final String WINDOW_TITLE = "Application de Messagerie - Authentification";

    /**
     * Largeur de la fenêtre
     */
    private static final int WINDOW_WIDTH = 500;

    /**
     * Hauteur de la fenêtre
     */
    private static final int WINDOW_HEIGHT = 400;

    // ============================================
    // ATTRIBUTS
    // ============================================

    /**
     * Référence à l'application principale (MessageApp).
     * Permet à AuthenticationView de communiquer avec la logique métier.
     */
    protected MessageApp messageApp;

    /**
     * Panneau de login (formulaire de connexion/inscription).
     */
    protected LoginPanel loginPanel;

    /**
     * Référence à la vue principale (MessageAppMainView).
     * Utilisée pour afficher la vue principale après connexion.
     */
    protected MessageAppMainView mainView;

    // ============================================
    // CONSTRUCTEUR
    // ============================================

    /**
     * Constructeur d'AuthenticationView.
     *
     * @param messageApp Référence à l'application principale
     * @param mainView   Référence à la vue principale (pour basculer après
     *                   connexion)
     */
    public AuthenticationView(MessageApp messageApp, MessageAppMainView mainView) {
        this.messageApp = messageApp;
        this.mainView = mainView;

        // Initialiser les composants
        initComponents();
    }

    // ============================================
    // INITIALISATION
    // ============================================

    /**
     * Initialise les composants de la fenêtre d'authentification.
     *
     * Étapes :
     * 1. Configuration de la fenêtre (titre, taille, etc.)
     * 2. Création du LoginPanel
     * 3. Affichage de la fenêtre
     */
    private void initComponents() {
        // Configuration de la fenêtre
        setTitle(WINDOW_TITLE);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Quitter l'app si on ferme cette fenêtre
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setLocationRelativeTo(null); // Centrer la fenêtre

        // Charger l'icône
        ImageIcon appIcon = loadIcon("logo_20.png");
        if (appIcon != null) {
            setIconImage(appIcon.getImage());
        }

        // Créer et afficher le LoginPanel
        // IMPORTANT : passer 'this' (AuthenticationView) au lieu de messageApp
        // Ainsi LoginPanel ne connaît que AuthenticationView
        loginPanel = new LoginPanel(this);

        // Ajouter le LoginPanel au centre de la fenêtre
        add(loginPanel, BorderLayout.CENTER);

        System.out.println("[AUTH_VIEW] Fenêtre d'authentification initialisée");
    }

    // ============================================
    // MÉTHODES PUBLIQUES
    // ============================================

    /**
     * Affiche la fenêtre d'authentification.
     *
     * Cette méthode est appelée au démarrage de l'application
     * pour afficher l'écran de login.
     */
    public void displayWindow() {
        setVisible(true);
        System.out.println("[AUTH_VIEW] Fenêtre d'authentification affichée");
    }

    /**
     * Ferme la fenêtre d'authentification.
     *
     * Cette méthode est appelée après une connexion réussie
     * pour passer à la vue principale.
     */
    public void close() {
        setVisible(false);
        dispose(); // Libérer les ressources de la fenêtre
        System.out.println("[AUTH_VIEW] Fenêtre d'authentification fermée");
    }

    // ============================================
    // MÉTHODES D'ORCHESTRATION
    // ============================================

    /**
     * Gère une tentative de connexion.
     *
     * AuthenticationView orchestre le processus :
     * 1. Appelle MessageApp.loginUser() pour authentifier
     * 2. Si succès : appelle onLoginSuccess()
     * 3. Si erreur : affiche un message d'erreur
     *
     * @param tag      Le tag de l'utilisateur
     * @param password Le mot de passe
     */
    public void handleLoginAttempt(String tag, String password) {
        System.out.println("[AUTH_VIEW] Tentative de connexion pour : " + tag);

        // Appeler MessageApp pour authentifier
        boolean loginSuccess = messageApp.loginUser(tag, password);

        if (loginSuccess) {
            // Succès : afficher un message et passer à la vue principale
            JOptionPane.showMessageDialog(
                    loginPanel,
                    "Connexion réussie ! Bienvenue " + tag,
                    "Succès",
                    JOptionPane.INFORMATION_MESSAGE);
            onLoginSuccess(tag);
        } else {
            // Erreur : afficher un message d'erreur
            JOptionPane.showMessageDialog(
                    loginPanel,
                    "Tag ou mot de passe incorrect !",
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
            System.out.println("[AUTH_VIEW] Connexion échouée pour : " + tag);
        }
    }

    /**
     * Gère une tentative d'inscription.
     *
     * AuthenticationView orchestre le processus :
     * 1. Appelle MessageApp.registerUser() pour créer l'utilisateur
     * 2. Si succès : affiche un message et bascule au mode connexion
     * 3. Si erreur : affiche un message d'erreur
     *
     * @param tag      Le tag unique de l'utilisateur
     * @param password Le mot de passe
     * @param name     Le nom de l'utilisateur
     */
    public void handleSignupAttempt(String tag, String password, String name) {
        System.out.println("[AUTH_VIEW] Tentative d'inscription pour : " + tag);

        // Appeler MessageApp pour créer le nouvel utilisateur
        boolean signupSuccess = messageApp.registerUser(tag, password, name);

        if (signupSuccess) {
            // Succès : afficher un message et retourner au mode connexion
            JOptionPane.showMessageDialog(
                    loginPanel,
                    "Compte créé avec succès !\n\nTag : " + tag + "\nNom : " + name
                            + "\n\nVous pouvez maintenant vous connecter.",
                    "Succès",
                    JOptionPane.INFORMATION_MESSAGE);

            // Retourner au mode connexion
            loginPanel.toggleMode();

            System.out.println("[AUTH_VIEW] Inscription réussie pour : " + tag);
        } else {
            // Erreur : le tag existe déjà ou autre problème
            JOptionPane.showMessageDialog(
                    loginPanel,
                    "Ce tag est déjà utilisé ou une erreur s'est produite. Veuillez réessayer.",
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
            System.out.println("[AUTH_VIEW] Inscription échouée pour : " + tag);
        }
    }

    /**
     * Gère une connexion réussie.
     *
     * Étapes :
     * 1. Fermer la fenêtre d'authentification
     * 2. Afficher la vue principale avec le nom d'utilisateur
     *
     * @param username Le nom d'utilisateur connecté
     */
    public void onLoginSuccess(String username) {
        System.out.println("[AUTH_VIEW] Connexion réussie pour : " + username);

        // Fermer cette fenêtre
        close();

        // Afficher la vue principale
        // (LoginPanel appelle MessageApp.loginUser() qui appelle cette méthode)
        if (mainView != null) {
            mainView.displayWindow();
        }
    }

    /**
     * Gère la déconnexion de l'utilisateur.
     *
     * Étapes :
     * 1. Fermer la vue principale
     * 2. Réinitialiser le LoginPanel
     * 3. Afficher la fenêtre d'authentification
     *
     * @param username Le nom d'utilisateur à déconnecter
     */
    public void onLogout(String username) {
        System.out.println("[AUTH_VIEW] Déconnexion de : " + username);

        // Fermer la session utilisateur (persiste online=false sur le disque)
        messageApp.logoutUser();

        // Fermer la vue principale
        if (mainView != null) {
            mainView.close();
        }

        // Créer un nouveau LoginPanel vierge
        loginPanel = new LoginPanel(this);

        // Retirer le contenu actuel et ajouter le nouveau LoginPanel
        getContentPane().removeAll();
        add(loginPanel, BorderLayout.CENTER);

        // Redessiner et afficher
        revalidate();
        repaint();
        setVisible(true);

        System.out.println("[AUTH_VIEW] Fenêtre d'authentification affichée pour nouvelle connexion");
    }

    // ============================================
    // MÉTHODES UTILITAIRES
    // ============================================

    /**
     * Charge une icône depuis les ressources.
     *
     * Cette méthode essaie plusieurs chemins pour charger l'icône :
     * 1. Depuis les ressources empaquetées (JAR)
     * 2. Depuis le classpath
     * 3. Depuis le système de fichiers (développement)
     *
     * @param filename Nom du fichier d'icône (ex: "logo_20.png")
     * @return L'icône chargée, ou null si non trouvée
     */
    private ImageIcon loadIcon(String filename) {
        java.net.URL imgURL = null;

        // Méthode 1 : ClassLoader avec "images/"
        imgURL = getClass().getClassLoader().getResource("images/" + filename);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        }

        // Méthode 2 : getResource avec "/images/"
        imgURL = getClass().getResource("/images/" + filename);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        }

        // Méthode 3 : ClassLoader sans préfixe
        imgURL = getClass().getClassLoader().getResource(filename);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        }

        // Méthode 4 : Depuis le système de fichiers (développement)
        try {
            String[] possiblePaths = {
                    "src/main/resources/images/" + filename,
                    "resources/images/" + filename,
                    "images/" + filename
            };

            for (String path : possiblePaths) {
                java.io.File imageFile = new java.io.File(path);
                if (imageFile.exists()) {
                    System.out.println("[AUTH_VIEW] Icône chargée depuis : " + imageFile.getAbsolutePath());
                    return new ImageIcon(imageFile.getAbsolutePath());
                }
            }
        } catch (Exception e) {
            System.err.println("[AUTH_VIEW] Erreur lors du chargement de l'icône : " + filename);
        }

        // Aucune méthode n'a fonctionné
        System.err.println("[AUTH_VIEW] Icône introuvable : " + filename);
        return null;
    }
}
