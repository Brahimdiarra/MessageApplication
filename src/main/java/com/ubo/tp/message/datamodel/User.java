package main.java.com.ubo.tp.message.datamodel;

import java.util.UUID;
import main.java.com.ubo.tp.message.common.PasswordEncoder;

/**
 * Classe du modèle représentant un utilisateur.
 *
 * @author S.Lucas
 */
public class User extends AbstractMessageAppObject implements IMessageRecipient {

	/**
	 * Tag non modifiable correspondant à l'utilisateur. <br/>
	 * <i>Doit être unique dans le système</i>
	 */
	protected final String mUserTag;

	/**
	 * Mot de passe de l'utilisateur.
	 * IMPORTANT : Ce n'est JAMAIS le password en clair !
	 * C'est le hash du password généré par PasswordEncoder.
	 */
	protected String mUserPasswordHash;

	/**
	 * Nom de l'utilisateur.
	 */
	protected String mName;

	/**
	 * Booléen indiquant si l'utilisateur est connecté.
	 */
	protected boolean mOnline = false;

	/**
	 * Constructeur.
	 *
	 * @param userTag      Tag correspondant à l'utilisateur.
	 * @param userPassword mot de passe de l'utilisateur (en clair).
	 * @param name         Nom de l'utilisateur.
	 */
	public User(String userTag, String userPassword, String name) {
		this(UUID.randomUUID(), userTag, userPassword, name);
	}

	/**
	 * Constructeur.
	 *
	 * @param uuid         Identifiant unique de l'utilisateur.
	 * @param userTag      Tag correspondant à l'utilisateur.
	 * @param userPassword mot de passe de l'utilisateur (en clair).
	 * @param name         Nom de l'utilisateur.
	 */
	public User(UUID uuid, String userTag, String userPassword, String name) {
		super(uuid);
		mUserTag = userTag;
		// IMPORTANT : Hasher le password en clair avec PasswordEncoder
		mUserPasswordHash = PasswordEncoder.hashPassword(userPassword);
		mName = name;
	}

	/**
	 * Constructeur interne pour charger un utilisateur depuis le fichier
	 * persistent.
	 * 
	 * IMPORTANT : Ce constructeur prend le hash du password, pas le password en
	 * clair.
	 * Il est utilisé par DataFilesManager quand on charge un utilisateur depuis un
	 * fichier.
	 * 
	 * @param uuid         Identifiant unique de l'utilisateur.
	 * @param userTag      Tag correspondant à l'utilisateur.
	 * @param passwordHash Le HASH du password (déjà hasé, pas le password en
	 *                     clair).
	 * @param name         Nom de l'utilisateur.
	 * @return Un nouvel objet User avec le hash du password
	 */
	public static User createFromStoredHash(UUID uuid, String userTag, String passwordHash, String name) {
		User user = new User(uuid, userTag, "temp", name);
		// Remplacer le hash temporaire par le vrai hash chargé du fichier
		user.mUserPasswordHash = passwordHash;
		return user;
	}

	/**
	 * Retourne le nom de l'utilisateur.
	 */
	public String getName() {
		return mName;
	}

	/**
	 * Assigne le nom de l'utilisateur.
	 *
	 * @param name
	 */
	public void setName(String name) {
		this.mName = name;
	}

	/**
	 * Retourne le tag correspondant à l'utilisateur.
	 */
	public String getUserTag() {
		return this.mUserTag;
	}

	/**
	 * Retourne le HASH du mot de passe de l'utilisateur.
	 * 
	 * IMPORTANT : Ce n'est PAS le password en clair !
	 * C'est le hash généré par PasswordEncoder.
	 */
	public String getUserPasswordHash() {
		return this.mUserPasswordHash;
	}

	/**
	 * Vérifie si un mot de passe en clair correspond au hash stocké.
	 * 
	 * @param passwordClear Le mot de passe en clair saisi par l'utilisateur
	 * @return true si le password est correct, false sinon
	 */
	public boolean verifyPassword(String passwordClear) {
		return PasswordEncoder.verifyPassword(passwordClear, this.mUserPasswordHash);
	}

	/**
	 * Modifie le mot de passe de l'utilisateur.
	 * 
	 * @param newPassword Le nouveau mot de passe (en clair)
	 */
	public void setUserPassword(String newPassword) {
		// Hasher le nouveau password avant de le stocker
		this.mUserPasswordHash = PasswordEncoder.hashPassword(newPassword);
	}

	/**
	 * Retourne le statut de connection.
	 */
	public boolean isOnline() {
		return this.mOnline;
	}

	/**
	 * Assigne le statut de connection.
	 *
	 * @param online
	 */
	public void setOnline(boolean online) {
		this.mOnline = online;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();

		sb.append("[");
		sb.append(this.getClass().getName());
		sb.append("] : ");
		sb.append(this.getUuid());
		sb.append(" {@");
		sb.append(this.getUserTag());
		sb.append(" / ");
		sb.append(this.getName());
		sb.append("}");

		return sb.toString();
	}
}
