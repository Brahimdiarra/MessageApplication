package main.java.com.ubo.tp.message.ihm;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Panneau de connexion et d'inscription des utilisateurs.
 * 
 * Ce panneau permet à l'utilisateur de :
 * - Se connecter avec un tag et un mot de passe existants
 * - Créer un nouveau compte avec un tag, mot de passe et nom
 *
 * @author BRAHIM
 */
public class LoginPanel extends JPanel {

    // --- Attributs ---
    /**
     * Référence à AuthenticationView (vue parente).
     * 
     * IMPORTANT : LoginPanel communique SEULEMENT avec AuthenticationView,
     * pas directement avec MessageApp. Cela permet une meilleure séparation.
     */
    protected AuthenticationView authenticationView;

    /**
     * Mode d'affichage : true = créer un compte, false = connexion
     */
    protected boolean isSignupMode = false;

    // --- Composants pour le mode CONNEXION ---
    /**
     * Label pour le champ "Tag"
     * JLabel : affiche du texte simple
     */
    protected JLabel labelTag;

    /**
     * Champ de saisie pour le tag de l'utilisateur
     * JTextField : permet à l'utilisateur de taper du texte
     */
    protected JTextField textFieldTag;

    /**
     * Label pour le champ "Mot de passe"
     */
    protected JLabel labelPassword;

    /**
     * Champ de saisie pour le mot de passe
     * JPasswordField : affiche des points à la place des caractères (sécurité)
     */
    protected JPasswordField passwordField;

    // --- Composants supplémentaires pour le mode INSCRIPTION ---
    /**
     * Label pour le champ "Nom d'utilisateur"
     * (uniquement visible en mode inscription)
     */
    protected JLabel labelName;

    /**
     * Champ de saisie pour le nom d'utilisateur
     * (uniquement visible en mode inscription)
     */
    protected JTextField textFieldName;

    // --- Boutons d'action ---
    /**
     * Bouton pour se connecter ou créer un compte
     * Son label change selon le mode (Connexion / Créer un compte)
     */
    protected JButton buttonSubmit;

    /**
     * Bouton pour basculer entre les deux modes
     * Mode connexion -> cliquer pour créer un compte
     * Mode inscription -> cliquer pour aller à la connexion
     */
    protected JButton buttonToggleMode;

    /**
     * Constructeur du LoginPanel.
     * 
     * @param authenticationView Référence à AuthenticationView (vue parente)
     */
    public LoginPanel(AuthenticationView authenticationView) {
        this.authenticationView = authenticationView;

        // Initialiser tous les composants Swing
        initComponents();
    }

    // Palette de couleurs (même que MessageAppMainView)
    private static final Color COLOR_PRIMARY  = new Color(30, 58, 138);
    private static final Color COLOR_ACCENT   = new Color(59, 130, 246);
    private static final Color COLOR_BG       = new Color(248, 250, 252);

    // ============================================
    // MÉTHODES D'INITIALISATION
    // ============================================

    private void initComponents() {
        // Layout principal : centrer la carte dans le fond coloré
        this.setLayout(new GridBagLayout());
        this.setBackground(COLOR_BG);

        // Créer les composants
        createLabels();
        createTextFields();
        createButtons();

        // Ajouter les composants au panneau
        addComponents();
    }

    /**
     * Crée tous les JLabel (textes d'étiquettes).
     * 
     * Les labels servent à expliquer à l'utilisateur à quoi servent les champs.
     */
    private void createLabels() {
        labelTag = new JLabel("Tag :");
        labelTag.setFont(new Font("SansSerif", Font.BOLD, 13));
        labelTag.setForeground(new Color(30, 41, 59));

        labelPassword = new JLabel("Mot de passe :");
        labelPassword.setFont(new Font("SansSerif", Font.BOLD, 13));
        labelPassword.setForeground(new Color(30, 41, 59));

        labelName = new JLabel("Nom :");
        labelName.setFont(new Font("SansSerif", Font.BOLD, 13));
        labelName.setForeground(new Color(30, 41, 59));
    }

    /**
     * Crée tous les champs de saisie texte (JTextField et JPasswordField).
     * 
     * - JTextField : affiche le texte en clair (pour le tag et le nom)
     * - JPasswordField : affiche des points à la place des caractères (pour le mot
     * de passe)
     */
    private void createTextFields() {
        textFieldTag = new JTextField(20);
        textFieldTag.setFont(new Font("SansSerif", Font.PLAIN, 13));
        textFieldTag.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225)),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)));

        passwordField = new JPasswordField(20);
        passwordField.setFont(new Font("SansSerif", Font.PLAIN, 13));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225)),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)));

        textFieldName = new JTextField(20);
        textFieldName.setFont(new Font("SansSerif", Font.PLAIN, 13));
        textFieldName.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225)),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)));
    }

    /**
     * Crée tous les boutons d'action.
     * 
     * Deux boutons :
     * 1. buttonSubmit : pour se connecter ou créer un compte
     * 2. buttonToggleMode : pour basculer entre les deux modes
     */
    private void createButtons() {
        buttonSubmit = new JButton("Connexion");
        buttonSubmit.setFont(new Font("SansSerif", Font.BOLD, 13));
        buttonSubmit.setBackground(COLOR_ACCENT);
        buttonSubmit.setForeground(Color.WHITE);
        buttonSubmit.setOpaque(true);
        buttonSubmit.setBorderPainted(false);
        buttonSubmit.setFocusPainted(false);
        buttonSubmit.setPreferredSize(new Dimension(140, 34));
        buttonSubmit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleSubmit();
            }
        });

        buttonToggleMode = new JButton("Créer un compte");
        buttonToggleMode.setFont(new Font("SansSerif", Font.PLAIN, 12));
        buttonToggleMode.setBackground(new Color(100, 116, 139));
        buttonToggleMode.setForeground(Color.WHITE);
        buttonToggleMode.setOpaque(true);
        buttonToggleMode.setBorderPainted(false);
        buttonToggleMode.setFocusPainted(false);
        buttonToggleMode.setPreferredSize(new Dimension(140, 34));
        buttonToggleMode.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toggleMode();
            }
        });
    }

    /**
     * Ajoute tous les composants au panneau avec le layout GridBagLayout.
     * 
     * GridBagLayout fonctionne comme une grille invisible (lignes, colonnes).
     * Chaque composant a une position (x, y) et des contraintes (taille,
     * espacement, etc.)
     */
    private void addComponents() {
        this.removeAll();

        // ── Carte centrale blanche ─────────────────────────────────────────────
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225), 1),
                BorderFactory.createEmptyBorder(0, 0, 20, 0)
        ));

        // ── En-tête coloré dans la carte ──────────────────────────────────────
        JPanel cardHeader = new JPanel(new BorderLayout());
        cardHeader.setBackground(COLOR_PRIMARY);
        cardHeader.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));
        JLabel titleLabel = new JLabel(isSignupMode ? "💬  Créer un compte" : "💬  Connexion");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);
        cardHeader.add(titleLabel, BorderLayout.WEST);

        GridBagConstraints gbc = new GridBagConstraints();

        // Header dans la carte (ligne 0)
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0; gbc.insets = new Insets(0, 0, 16, 0);
        card.add(cardHeader, gbc);

        // ── Champs ────────────────────────────────────────────────────────────
        gbc.gridwidth = 1; gbc.insets = new Insets(6, 16, 2, 8);

        // Tag label
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.EAST;
        card.add(labelTag, gbc);

        // Tag field
        gbc.gridx = 1; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST; gbc.insets = new Insets(6, 0, 2, 16);
        card.add(textFieldTag, gbc);

        // Password label
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.EAST; gbc.insets = new Insets(6, 16, 2, 8);
        card.add(labelPassword, gbc);

        // Password field
        gbc.gridx = 1; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST; gbc.insets = new Insets(6, 0, 2, 16);
        card.add(passwordField, gbc);

        if (isSignupMode) {
            // Name label
            gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
            gbc.anchor = GridBagConstraints.EAST; gbc.insets = new Insets(6, 16, 2, 8);
            card.add(labelName, gbc);

            // Name field
            gbc.gridx = 1; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.anchor = GridBagConstraints.WEST; gbc.insets = new Insets(6, 0, 2, 16);
            card.add(textFieldName, gbc);
        }

        // ── Boutons ───────────────────────────────────────────────────────────
        int buttonRow = isSignupMode ? 4 : 3;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.add(buttonSubmit);
        buttonPanel.add(buttonToggleMode);

        gbc.gridx = 0; gbc.gridy = buttonRow; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(20, 16, 0, 16);
        card.add(buttonPanel, gbc);

        // ── Centrer la carte dans le panel principal ───────────────────────────
        GridBagConstraints outer = new GridBagConstraints();
        outer.anchor = GridBagConstraints.CENTER;
        outer.fill = GridBagConstraints.NONE;
        outer.weightx = 1.0; outer.weighty = 1.0;
        this.add(card, outer);

        this.revalidate();
        this.repaint();
    }

    /**
     * Bascule entre le mode connexion et le mode inscription.
     * 
     * - En mode connexion (false) : on affiche seulement tag + password
     * - En mode inscription (true) : on affiche tag + password + nom
     */
    protected void toggleMode() {
        isSignupMode = !isSignupMode;

        // Mettre à jour les labels des boutons
        buttonSubmit.setText(isSignupMode ? "Créer un compte" : "Connexion");
        buttonToggleMode.setText(isSignupMode ? "Retour à la connexion" : "Créer un compte");

        // Réinitialiser les champs
        textFieldTag.setText("");
        passwordField.setText("");
        textFieldName.setText("");

        // Redessiner le panneau avec les nouveaux composants
        addComponents();
    }

    /**
     * Gère l'événement du bouton principal (Connexion ou Créer un compte).
     * 
     * Cette méthode :
     * 1. Récupère les données saisies par l'utilisateur
     * 2. Valide les données (SRS-MAP-USR-002 : tag et nom obligatoires)
     * 3. Appelle la méthode appropriée (connexion ou inscription)
     */
    private void handleSubmit() {
        if (isSignupMode) {
            handleSignup();
        } else {
            handleLogin();
        }
    }

    /**
     * Gère la CONNEXION d'un utilisateur existant.
     * 
     * Étapes :
     * 1. Récupérer le tag et le mot de passe
     * 2. Valider que ces champs ne sont pas vides
     * 3. Appeler messageApp pour authentifier l'utilisateur
     * 4. Si succès : fermer le LoginPanel et afficher l'application
     * 5. Si erreur : afficher un message d'erreur
     */
    private void handleLogin() {
        // Récupérer les données du formulaire
        String tag = textFieldTag.getText().trim();

        // Les caractères d'un mot de passe : on doit utiliser getPassword()
        String password = new String(passwordField.getPassword());

        // Vérifier que le tag n'est pas vide
        if (tag.isEmpty()) {
            showErrorDialog("Le tag est obligatoire !");
            return;
        }

        // Vérifier que le mot de passe n'est pas vide
        if (password.isEmpty()) {
            showErrorDialog("Le mot de passe est obligatoire !");
            return;
        }

        System.out.println("[LOGIN] Tentative de connexion pour le tag : " + tag);

        // Appeler la méthode handleLoginAttempt() dans AuthenticationView
        // Cette méthode orchestrera le processus de connexion
        // (vérification des identifiants + affichage de l'application si succès)
        authenticationView.handleLoginAttempt(tag, password);
    }

    /**
     * Gère l'INSCRIPTION d'un nouvel utilisateur.
     * 
     * Étapes :
     * 1. Récupérer le tag, le mot de passe et le nom
     * 2. Valider que ces champs ne sont pas vides (SRS-MAP-USR-002)
     * 3. Appeler messageApp pour créer le nouvel utilisateur
     * 4. Si succès : afficher un message et basculer en mode connexion
     * 5. Si erreur : afficher un message d'erreur (ex: tag déjà existant)
     */
    private void handleSignup() {
        // Récupérer les données du formulaire
        String tag = textFieldTag.getText().trim();
        String password = new String(passwordField.getPassword());
        String name = textFieldName.getText().trim();

        // ============================================
        // VALIDATION DES DONNÉES (SRS-MAP-USR-002)
        // ============================================

        // Vérifier que le tag n'est pas vide
        if (tag.isEmpty()) {
            showErrorDialog("Le tag est obligatoire !");
            return;
        }

        // Vérifier que le mot de passe n'est pas vide
        if (password.isEmpty()) {
            showErrorDialog("Le mot de passe est obligatoire !");
            return;
        }

        // Vérifier que le nom n'est pas vide
        if (name.isEmpty()) {
            showErrorDialog("Le nom est obligatoire !");
            return;
        }

        // ============================================
        // APPELER LA MÉTHODE D'INSCRIPTION
        // ============================================
        System.out.println("[SIGNUP] Création d'un compte");
        System.out.println("  Tag: " + tag);
        System.out.println("  Nom: " + name);

        // Appeler la méthode handleSignupAttempt() dans AuthenticationView
        // Cette méthode orchestrera le processus d'inscription
        // (création du compte + retour à la vue login si succès)
        authenticationView.handleSignupAttempt(tag, password, name);
    }

    /**
     * Affiche une boîte de dialogue d'erreur.
     * 
     * Cette méthode est appelée quand il y a un problème
     * (champ vide, tag déjà existant, mauvais mot de passe, etc.)
     * 
     * @param errorMessage Le message d'erreur à afficher
     */
    private void showErrorDialog(String errorMessage) {
        JOptionPane.showMessageDialog(
                this, // Fenêtre parente
                errorMessage, // Le message à afficher
                "Erreur", // Le titre de la boîte
                JOptionPane.ERROR_MESSAGE // Type d'icône (rouge avec X)
        );
    }

    /**
     * Affiche une boîte de dialogue de confirmation.
     * 
     * Utilisée pour demander à l'utilisateur de confirmer une action
     * (ex: suppression du compte, déconnexion, etc.)
     * 
     * @param title   Le titre de la boîte
     * @param message Le message à afficher
     * @return true si l'utilisateur a cliqué "Oui", false sinon
     */
    protected boolean showConfirmDialog(String title, String message) {
        int result = JOptionPane.showConfirmDialog(
                this, // Fenêtre parente
                message, // Le message
                title, // Le titre
                JOptionPane.YES_NO_OPTION, // Boutons "Oui" et "Non"
                JOptionPane.QUESTION_MESSAGE // Type d'icône (bleu avec ?)
        );

        // Si l'utilisateur a cliqué "Oui" (YES_OPTION), retourner true
        return result == JOptionPane.YES_OPTION;
    }
}
