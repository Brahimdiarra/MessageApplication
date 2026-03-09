package main.java.com.ubo.tp.message.core;

import main.java.com.ubo.tp.message.datamodel.User;

/**
 * Gestionnaire de session utilisateur.
 * 
 * Cette classe gère la session de l'utilisateur connecté :
 * - Qui est connecté actuellement ?
 * - Accès aux infos de l'utilisateur connecté
 * - Déconnexion
 * 
 * Singleton Pattern :
 * Il y a toujours qu'une seule session active à la fois.
 * Utiliser SessionManager.getInstance() pour y accéder partout.
 * 
 * @author BRAHIM
 */
public class SessionManager {

    // ============================================
    // SINGLETON
    // ============================================

    /**
     * Instance unique du SessionManager (Singleton Pattern)
     */
    private static SessionManager instance = null;

    /**
     * Récupère l'instance unique du SessionManager.
     * 
     * Si elle n'existe pas, elle est créée.
     * 
     * @return L'instance unique du SessionManager
     */
    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    // ============================================
    // ATTRIBUTS
    // ============================================

    /**
     * L'utilisateur actuellement connecté.
     * null = aucun utilisateur connecté
     */
    protected User currentUser = null;

    // ============================================
    // CONSTRUCTEUR PRIVÉ (Singleton)
    // ============================================

    /**
     * Constructeur privé (ne peut être appelé qu'une fois).
     * 
     * Le Singleton Pattern assure qu'il y a toujours qu'une seule instance.
     */
    private SessionManager() {
        this.currentUser = null;
        System.out.println("[SESSION] SessionManager initialisé");
    }

    // ============================================
    // MÉTHODES PUBLIQUES
    // ============================================

    /**
     * Établit la session pour un utilisateur.
     * 
     * Appelé quand un utilisateur se connecte avec succès.
     * 
     * @param user L'utilisateur qui se connecte
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;

        if (user != null) {
            // Marquer l'utilisateur comme en ligne
            user.setOnline(true);
            System.out.println("[SESSION] Utilisateur connecté : " + user.getUserTag());
        } else {
            System.out.println("[SESSION] Session réinitialisée (pas d'utilisateur)");
        }
    }

    /**
     * Récupère l'utilisateur actuellement connecté.
     * 
     * @return L'utilisateur connecté, ou null si aucun utilisateur
     */
    public User getCurrentUser() {
        return this.currentUser;
    }

    /**
     * Récupère le tag de l'utilisateur connecté.
     * 
     * Utile pour afficher "Bienvenue {username}" dans l'interface.
     * 
     * @return Le tag de l'utilisateur connecté, ou null si aucun utilisateur
     */
    public String getCurrentUserTag() {
        if (currentUser != null) {
            return currentUser.getUserTag();
        }
        return null;
    }

    /**
     * Vérifie si un utilisateur est connecté.
     * 
     * @return true si un utilisateur est connecté, false sinon
     */
    public boolean isUserConnected() {
        return this.currentUser != null;
    }

    /**
     * Déconnecte l'utilisateur actuel.
     * 
     * Appelé quand l'utilisateur clique sur "Déconnexion".
     */
    public void logout() {
        if (currentUser != null) {
            // Marquer l'utilisateur comme hors ligne
            currentUser.setOnline(false);
            System.out.println("[SESSION] Utilisateur déconnecté : " + currentUser.getUserTag());

            // Supprimer la référence
            currentUser = null;
        } else {
            System.out.println("[SESSION] Aucun utilisateur à déconnecter");
        }
    }

    /**
     * Affiche les infos de session (pour débogage).
     */
    public void printSessionInfo() {
        if (currentUser != null) {
            System.out.println("[SESSION] === INFO SESSION ===");
            System.out.println("[SESSION] Tag : " + currentUser.getUserTag());
            System.out.println("[SESSION] Nom : " + currentUser.getName());
            System.out.println("[SESSION] En ligne : " + currentUser.isOnline());
            System.out.println("[SESSION] UUID : " + currentUser.getUuid());
        } else {
            System.out.println("[SESSION] Aucun utilisateur connecté");
        }
    }
}
