package main.java.com.ubo.tp.message.core;

import main.java.com.ubo.tp.message.datamodel.User;
import main.java.com.ubo.tp.message.common.DataFilesManager;
import main.java.com.ubo.tp.message.common.Constants;

import java.io.File;
import java.util.Set;
import java.util.HashSet;

/**
 * Gestionnaire de persistance des utilisateurs.
 * 
 * Cette classe gère :
 * 1. Le chargement de tous les utilisateurs au démarrage
 * 2. La sauvegarde des utilisateurs après création/modification
 * 3. La suppression des utilisateurs
 * 
 * Architecture :
 * - UserPersistenceManager appelle DataFilesManager pour lire/écrire des
 * fichiers
 * - DataFilesManager gère les détails techniques (Properties, encodage, etc.)
 * - UserPersistenceManager gère la logique de chargement/sauvegarde
 * 
 * @author BRAHIM
 */
public class UserPersistenceManager {

    // ============================================
    // ATTRIBUTS
    // ============================================

    /**
     * Gestionnaire de fichiers (pour lire/écrire les fichiers utilisateurs)
     */
    protected DataFilesManager dataFilesManager;

    /**
     * Répertoire de base pour les fichiers utilisateurs
     */
    protected String basePath;

    // ============================================
    // CONSTRUCTEUR
    // ============================================

    /**
     * Constructeur du UserPersistenceManager.
     * 
     * @param dataFilesManager Le gestionnaire de fichiers
     * @param basePath         Chemin de base pour les fichiers
     */
    public UserPersistenceManager(DataFilesManager dataFilesManager, String basePath) {
        this.dataFilesManager = dataFilesManager;
        this.basePath = basePath;
    }

    // ============================================
    // MÉTHODES PUBLIQUES
    // ============================================

    /**
     * Charge tous les utilisateurs depuis les fichiers.
     * 
     * Étapes :
     * 1. Lister tous les fichiers dans le répertoire des utilisateurs
     * 2. Filtrer les fichiers ".user"
     * 3. Charger chaque utilisateur avec DataFilesManager
     * 4. Retourner une collection de tous les utilisateurs
     * 
     * @return Ensemble de tous les utilisateurs chargés
     */
    public Set<User> loadAllUsers() {
        Set<User> users = new HashSet<>();

        // Créer le répertoire s'il n'existe pas
        File userDirectory = new File(basePath);
        if (!userDirectory.exists()) {
            userDirectory.mkdirs();
            System.out.println("[PERSISTENCE] Répertoire créé : " + basePath);
            return users; // Aucun utilisateur à charger
        }

        // Lister tous les fichiers du répertoire
        File[] files = userDirectory.listFiles();
        if (files == null) {
            System.out.println("[PERSISTENCE] Erreur lors de la lecture du répertoire : " + basePath);
            return users;
        }

        // Charger chaque fichier utilisateur
        int loadedCount = 0;
        for (File file : files) {
            // Vérifier que c'est un fichier utilisateur
            if (file.isFile() && file.getName().endsWith(Constants.USER_FILE_EXTENSION)) {
                try {
                    User user = dataFilesManager.readUser(file);
                    if (user != null) {
                        users.add(user);
                        loadedCount++;
                        System.out.println("[PERSISTENCE] Utilisateur chargé : " + user.getUserTag());
                    }
                } catch (Exception e) {
                    System.err.println("[PERSISTENCE] Erreur lors du chargement du fichier : " + file.getName());
                    e.printStackTrace();
                }
            }
        }

        System.out.println("[PERSISTENCE] " + loadedCount + " utilisateur(s) chargé(s) au démarrage");
        return users;
    }

    /**
     * Sauvegarde un utilisateur dans un fichier.
     * 
     * @param user L'utilisateur à sauvegarder
     */
    public void saveUser(User user) {
        try {
            dataFilesManager.writeUserFile(user);
            System.out.println("[PERSISTENCE] Utilisateur sauvegardé : " + user.getUserTag());
        } catch (Exception e) {
            System.err.println("[PERSISTENCE] Erreur lors de la sauvegarde de l'utilisateur : " + user.getUserTag());
            e.printStackTrace();
        }
    }

    /**
     * Sauvegarde tous les utilisateurs dans des fichiers.
     * 
     * @param users Ensemble des utilisateurs à sauvegarder
     */
    public void saveAllUsers(Set<User> users) {
        for (User user : users) {
            saveUser(user);
        }
        System.out.println("[PERSISTENCE] " + users.size() + " utilisateur(s) sauvegardé(s)");
    }

    /**
     * Supprime un utilisateur (enlève son fichier).
     * 
     * @param user L'utilisateur à supprimer
     */
    public void deleteUser(User user) {
        try {
            // Créer le chemin du fichier à supprimer
            String fileName = dataFilesManager.getFileName(user.getUuid(), Constants.USER_FILE_EXTENSION);
            File file = new File(fileName);

            if (file.exists()) {
                if (file.delete()) {
                    System.out.println("[PERSISTENCE] Utilisateur supprimé : " + user.getUserTag());
                } else {
                    System.err.println("[PERSISTENCE] Impossible de supprimer le fichier : " + fileName);
                }
            } else {
                System.out.println("[PERSISTENCE] Fichier utilisateur introuvable : " + fileName);
            }
        } catch (Exception e) {
            System.err.println("[PERSISTENCE] Erreur lors de la suppression de l'utilisateur : " + user.getUserTag());
            e.printStackTrace();
        }
    }
}
