package main.java.com.ubo.tp.message.ihm;

import javax.swing.*;
import javax.swing.border.LineBorder;
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

    // ============================================
    // MÉTHODES D'INITIALISATION
    // ============================================

    /**
     * Initialise tous les composants visuels du panneau.
     * 
     * Cette méthode :
     * 1. Configure le layout du panneau (GridBagLayout)
     * 2. Crée tous les composants (labels, textfields, boutons)
     * 3. Ajoute les composants au panneau avec les bonnes contraintes
     * 4. Configure les listeners pour les boutons
     */
    private void initComponents() {
        // Utiliser GridBagLayout pour un positionnement flexible
        this.setLayout(new GridBagLayout());

        // Définir l'apparence générale du panneau
        this.setBackground(new Color(240, 240, 240));
        this.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

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
        // Label pour le tag
        // Le tag est l'identifiant unique de l'utilisateur (ex: "john_doe")
        labelTag = new JLabel("Tag :");
        labelTag.setFont(new Font("Arial", Font.BOLD, 14));

        // Label pour le mot de passe
        labelPassword = new JLabel("Mot de passe :");
        labelPassword.setFont(new Font("Arial", Font.BOLD, 14));

        // Label pour le nom (visible seulement en mode inscription)
        labelName = new JLabel("Nom :");
        labelName.setFont(new Font("Arial", Font.BOLD, 14));
    }

    /**
     * Crée tous les champs de saisie texte (JTextField et JPasswordField).
     * 
     * - JTextField : affiche le texte en clair (pour le tag et le nom)
     * - JPasswordField : affiche des points à la place des caractères (pour le mot
     * de passe)
     */
    private void createTextFields() {
        // Champ pour le tag
        textFieldTag = new JTextField(20);
        textFieldTag.setFont(new Font("Arial", Font.PLAIN, 12));

        // Champ pour le mot de passe
        passwordField = new JPasswordField(20);
        passwordField.setFont(new Font("Arial", Font.PLAIN, 12));

        // Champ pour le nom
        textFieldName = new JTextField(20);
        textFieldName.setFont(new Font("Arial", Font.PLAIN, 12));
    }

    /**
     * Crée tous les boutons d'action.
     * 
     * Deux boutons :
     * 1. buttonSubmit : pour se connecter ou créer un compte
     * 2. buttonToggleMode : pour basculer entre les deux modes
     */
    private void createButtons() {
        // Bouton principal (Connexion ou Créer un compte selon le mode)
        buttonSubmit = new JButton("Connexion");
        buttonSubmit.setFont(new Font("Arial", Font.BOLD, 12));
        buttonSubmit.setBackground(new Color(70, 130, 180));
        buttonSubmit.setForeground(Color.WHITE);
        buttonSubmit.setFocusPainted(false);
        buttonSubmit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleSubmit();
            }
        });

        // Bouton pour basculer le mode
        buttonToggleMode = new JButton("Créer un compte");
        buttonToggleMode.setFont(new Font("Arial", Font.PLAIN, 11));
        buttonToggleMode.setBackground(new Color(150, 150, 150));
        buttonToggleMode.setForeground(Color.WHITE);
        buttonToggleMode.setFocusPainted(false);
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
        // Effacer tous les composants précédents (en cas de rappel)
        this.removeAll();

        // Titre du panneau
        JLabel titleLabel = new JLabel(isSignupMode ? "Créer un compte" : "Connexion");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        this.add(titleLabel, new GridBagConstraints(
                0, 0, // Position: colonne 0, ligne 0
                2, 1, // Taille: 2 colonnes, 1 ligne
                1.0, 0.0, // Poids: largeur élastique, hauteur fixe
                GridBagConstraints.CENTER, // Ancrage: centré
                GridBagConstraints.HORIZONTAL, // Remplissage: horizontal
                new Insets(0, 0, 20, 0), // Espacement: 20px en bas
                0, 0 // Espacement interne: aucun
        ));

        // --- Ligne 1 : Label Tag ---
        this.add(labelTag, new GridBagConstraints(
                0, 1, // Position: colonne 0, ligne 1
                1, 1, // Taille: 1 colonne, 1 ligne
                0.0, 0.0, // Poids: taille fixe
                GridBagConstraints.EAST, // Ancrage: à droite
                GridBagConstraints.NONE, // Pas de remplissage
                new Insets(5, 5, 5, 5), // Espacement: 5px partout
                0, 0));

        // --- Ligne 1 : Champ Tag ---
        this.add(textFieldTag, new GridBagConstraints(
                1, 1, // Position: colonne 1, ligne 1
                1, 1, // Taille: 1 colonne, 1 ligne
                1.0, 0.0, // Poids: largeur élastique
                GridBagConstraints.WEST, // Ancrage: à gauche
                GridBagConstraints.HORIZONTAL, // Remplissage: horizontal
                new Insets(5, 5, 5, 5), // Espacement: 5px partout
                0, 0));

        // --- Ligne 2 : Label Mot de passe ---
        this.add(labelPassword, new GridBagConstraints(
                0, 2, // Position: colonne 0, ligne 2
                1, 1,
                0.0, 0.0,
                GridBagConstraints.EAST,
                GridBagConstraints.NONE,
                new Insets(5, 5, 5, 5),
                0, 0));

        // --- Ligne 2 : Champ Mot de passe ---
        this.add(passwordField, new GridBagConstraints(
                1, 2, // Position: colonne 1, ligne 2
                1, 1,
                1.0, 0.0,
                GridBagConstraints.WEST,
                GridBagConstraints.HORIZONTAL,
                new Insets(5, 5, 5, 5),
                0, 0));

        // --- Ligne 3 : Label Nom (seulement en mode inscription) ---
        if (isSignupMode) {
            this.add(labelName, new GridBagConstraints(
                    0, 3,
                    1, 1,
                    0.0, 0.0,
                    GridBagConstraints.EAST,
                    GridBagConstraints.NONE,
                    new Insets(5, 5, 5, 5),
                    0, 0));

            // --- Ligne 3 : Champ Nom ---
            this.add(textFieldName, new GridBagConstraints(
                    1, 3, // Position: colonne 1, ligne 3
                    1, 1,
                    1.0, 0.0,
                    GridBagConstraints.WEST,
                    GridBagConstraints.HORIZONTAL,
                    new Insets(5, 5, 5, 5),
                    0, 0));
        }

        // --- Boutons en bas du panneau ---
        int lastRow = isSignupMode ? 4 : 3;

        // Bouton Submit (Connexion ou Créer un compte)
        this.add(buttonSubmit, new GridBagConstraints(
                0, lastRow, // Position: colonne 0, ligne après les champs
                1, 1,
                1.0, 0.0,
                GridBagConstraints.CENTER,
                GridBagConstraints.HORIZONTAL,
                new Insets(20, 5, 5, 5),
                0, 0));

        // Bouton Toggle Mode (Créer un compte ou Connexion)
        this.add(buttonToggleMode, new GridBagConstraints(
                1, lastRow, // Position: colonne 1, même ligne que Submit
                1, 1,
                1.0, 0.0,
                GridBagConstraints.CENTER,
                GridBagConstraints.HORIZONTAL,
                new Insets(20, 5, 5, 5),
                0, 0));

        // Espace vide en bas pour pousser les contenus vers le haut
        this.add(new JPanel(), new GridBagConstraints(
                0, lastRow + 1, // Position: colonne 0, ligne après les boutons
                2, 1, // Taille: 2 colonnes
                1.0, 1.0, // Poids: élastique en largeur ET hauteur
                GridBagConstraints.CENTER,
                GridBagConstraints.BOTH, // Remplissage: partout
                new Insets(0, 0, 0, 0),
                0, 0));

        // Redessiner le panneau
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
