package main.java.com.ubo.tp.message.common;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

/**
 * Classe pour hasher et vérifier les mots de passe de manière sécurisée.
 * 
 * Utilise l'algorithme PBKDF2 (Password-Based Key Derivation Function 2)
 * avec du salt aléatoire pour éviter les attaques par rainbow table.
 * 
 * Fonctionnement :
 * 1. hashPassword(password) → crée un hash sécurisé + salt
 * 2. verifyPassword(password, hash) → compare le password avec le hash stocké
 * 
 * @author BRAHIM
 */
public class PasswordEncoder {

    // ============================================
    // CONSTANTES
    // ============================================

    /**
     * Algorithme utilisé pour le hash
     */
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";

    /**
     * Nombre d'itérations (plus = plus sécurisé mais plus lent)
     * 65536 est le standard NIST
     */
    private static final int ITERATIONS = 65536;

    /**
     * Longueur de la clé générée en bits
     */
    private static final int KEY_LENGTH = 256;

    /**
     * Longueur du salt en bytes
     * Plus le salt est long, meilleure est la sécurité
     */
    private static final int SALT_LENGTH = 16;

    // ============================================
    // MÉTHODES PUBLIQUES
    // ============================================

    /**
     * Hashe un mot de passe avec un salt aléatoire.
     * 
     * Étapes :
     * 1. Générer un salt aléatoire
     * 2. Appliquer PBKDF2 au password
     * 3. Retourner "salt:hash" encodé en Base64
     * 
     * @param password Le mot de passe en clair
     * @return Le hash du mot de passe (contient salt + hash)
     */
    public static String hashPassword(String password) {
        try {
            // Étape 1 : Générer un salt aléatoire
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[SALT_LENGTH];
            random.nextBytes(salt);

            // Étape 2 : Hasher le password avec le salt
            byte[] hash = hashPasswordWithSalt(password, salt);

            // Étape 3 : Combiner salt + hash et encoder en Base64
            // Format : salt:hash (séparés pour pouvoir les extraire plus tard)
            String saltEncoded = Base64.getEncoder().encodeToString(salt);
            String hashEncoded = Base64.getEncoder().encodeToString(hash);

            return saltEncoded + ":" + hashEncoded;

        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Erreur lors du hachage du password", e);
        }
    }

    /**
     * Vérifie si un mot de passe en clair correspond au hash stocké.
     * 
     * Étapes :
     * 1. Extraire le salt du hash stocké
     * 2. Hasher le password en clair avec ce salt
     * 3. Comparer les deux hashes
     * 
     * @param password   Le mot de passe en clair saisi par l'utilisateur
     * @param storedHash Le hash stocké dans la base de données
     * @return true si le password est correct, false sinon
     */
    public static boolean verifyPassword(String password, String storedHash) {
        try {
            // Étape 1 : Extraire salt et hash du storedHash
            String[] parts = storedHash.split(":");
            if (parts.length != 2) {
                System.err.println("[ERROR] Format de hash invalide");
                return false;
            }

            byte[] salt = Base64.getDecoder().decode(parts[0]);
            byte[] storedHashBytes = Base64.getDecoder().decode(parts[1]);

            // Étape 2 : Hasher le password en clair avec le salt extrait
            byte[] computedHash = hashPasswordWithSalt(password, salt);

            // Étape 3 : Comparer les deux hashes de manière sécurisée
            // (pas de == ou .equals() car on peut être victime d'attaques par timing)
            return slowEquals(storedHashBytes, computedHash);

        } catch (IllegalArgumentException e) {
            System.err.println("[ERROR] Erreur lors du décodage du hash");
            return false;
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Erreur lors de la vérification du password", e);
        }
    }

    // ============================================
    // MÉTHODES PRIVÉES
    // ============================================

    /**
     * Applique l'algorithme PBKDF2 au password avec un salt.
     * 
     * PBKDF2 = "Password-Based Key Derivation Function 2"
     * C'est un algorithme qui rend très difficile d'inverser le hash
     * (attaques par brute-force très lentes).
     * 
     * @param password Le mot de passe
     * @param salt     Le salt aléatoire
     * @return Le hash du password
     */
    private static byte[] hashPasswordWithSalt(String password, byte[] salt)
            throws NoSuchAlgorithmException, InvalidKeySpecException {

        // Créer la spécification de la clé
        PBEKeySpec spec = new PBEKeySpec(
                password.toCharArray(), // Le password
                salt, // Le salt aléatoire
                ITERATIONS, // Nombre d'itérations
                KEY_LENGTH // Longueur de la clé (bits)
        );

        // Utiliser le SecretKeyFactory pour générer le hash
        SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
        return factory.generateSecret(spec).getEncoded();
    }

    /**
     * Compare deux arrays de bytes de manière sécurisée (résiste aux attaques par
     * timing).
     * 
     * Une attaque par timing :
     * - Comparer "abcdefgh" avec "abcd1234" s'arrête vite (première différence au
     * byte 5)
     * - Comparer "abcdefgh" avec "abcdefgh" s'arrête tard (tous les bytes comparés)
     * Un attaquant peut mesurer le temps et deviner le hash progressivement
     * 
     * Solution : Toujours comparer TOUS les bytes même si une différence est
     * détectée.
     * 
     * @param a Première array de bytes
     * @param b Deuxième array de bytes
     * @return true si identiques, false sinon
     */
    private static boolean slowEquals(byte[] a, byte[] b) {
        int diff = a.length ^ b.length;

        for (int i = 0; i < a.length && i < b.length; i++) {
            diff |= a[i] ^ b[i];
        }

        return diff == 0;
    }
}
