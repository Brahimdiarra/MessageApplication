package main.java.com.ubo.tp.message.ihm;

import java.io.File;
import java.util.Properties;

import main.java.com.ubo.tp.message.core.DataManager;
import main.java.com.ubo.tp.message.core.SessionManager;
import main.java.com.ubo.tp.message.common.Constants;
import main.java.com.ubo.tp.message.common.PropertiesManager;

import javax.swing.*;

/**
 * Classe principale l'application.
 *
 * @author S.Lucas
 */
public class MessageApp {
	/**
	 * Gestionnaire de données.
	 */
	protected DataManager mDataManager;

	/**
	 * Vue d'authentification (login/signup).
	 */
	protected AuthenticationView mAuthView;

	/**
	 * Vue principale de l'application.
	 */
	protected MessageAppMainView mMainView;

	/**
	 * Constructeur.
	 *
	 * @param dataManager Le gestionnaire de données
	 */
	public MessageApp(DataManager dataManager) {
		this.mDataManager = dataManager;
	}

	/**
	 * Initialisation de l'application.
	 */
	public void init() {
		// Init du look and feel de l'application
		this.initLookAndFeel();

		// Initialisation de l'IHM
		this.initGui();

		// Initialisation du répertoire d'échange
		this.initDirectory();
	}

	/**
	 * Initialisation du look and feel de l'application.
	 */
	protected void initLookAndFeel() {
		try {
			// Utilisation du Look & Feel Nimbus (moderne et agréable)
			for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					UIManager.setLookAndFeel(info.getClassName());
					System.out.println("[INFO] Look & Feel Nimbus activé");
					return;
				}
			}
			// Fallback sur le système si Nimbus n'est pas disponible
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			System.out.println("[INFO] Look & Feel système activé");
		} catch (Exception e) {
			System.err.println("[ERREUR] Initialisation du Look & Feel : " + e.getMessage());
		}
	}

	/**
	 * Initialisation de l'interface graphique.
	 *
	 * Étapes :
	 * 1. Créer la vue principale
	 * 2. Créer la vue d'authentification (qui contient le LoginPanel)
	 * 3. Afficher la vue d'authentification (le login sera affiché en premier)
	 */
	protected void initGui() {
		// 1. Créer la vue principale (mais ne pas l'afficher tout de suite)
		this.mMainView = new MessageAppMainView(this, null);

		// 2. Créer la vue d'authentification
		this.mAuthView = new AuthenticationView(this, mMainView);

		// Mettre à jour la référence dans mMainView
		mMainView.setAuthenticationView(mAuthView);

		System.out.println("[APP] Interfaces graphiques créées");
	}

	/**
	 * Initialisation du répertoire d'échange (depuis la conf ou depuis un file
	 * chooser). <br/>
	 * <b>Le chemin doit obligatoirement avoir été saisi et être valide avant de
	 * pouvoir utiliser l'application</b>
	 */
	protected void initDirectory() {
		String configFilePath = "src/main/resources/configuration.properties";
		Properties properties = PropertiesManager.loadProperties(configFilePath);

		String exchangeDirectory = properties.getProperty(Constants.CONFIGURATION_KEY_EXCHANGE_DIRECTORY, "").trim();

		// Si un répertoire valide est configuré, l'utiliser
		if (!exchangeDirectory.isEmpty()) {
			File directory = new File(exchangeDirectory);
			if (isValidExchangeDirectory(directory)) {
				initDirectory(exchangeDirectory);
				System.out.println("[INFO] Répertoire d'échange chargé depuis la configuration : " + exchangeDirectory);
				showWindow();
				return;
			} else {
				System.out.println("[AVERTISSEMENT] Répertoire configuré invalide : " + exchangeDirectory);
			}
		}

		System.out.println("[INFO] Aucun répertoire d'échange valide configuré. Affichage de la fenêtre...");
		showWindow();
	}

	/**
	 * Indique si le fichier donné est valide pour servir de répertoire d'échange
	 *
	 * @param directory Répertoire à tester.
	 * @return true si valide
	 */
	public boolean isValidExchangeDirectory(File directory) {
		return directory != null && directory.exists() && directory.isDirectory()
				&& directory.canRead() && directory.canWrite();
	}

	/**
	 * Initialisation du répertoire d'échange.
	 *
	 * @param directoryPath Chemin du répertoire
	 */
	public void initDirectory(String directoryPath) {
		mDataManager.setExchangeDirectory(directoryPath);
		mDataManager.initializeUserPersistence(
				mDataManager.getDataFilesManager(),
				directoryPath);
		String configFilePath = "src/main/resources/configuration.properties";
		Properties properties = PropertiesManager.loadProperties(configFilePath);
		properties.setProperty(Constants.CONFIGURATION_KEY_EXCHANGE_DIRECTORY, directoryPath);
		PropertiesManager.writeProperties(properties, configFilePath);

		System.out.println("[INFO] Répertoire d'échange configuré et sauvegardé : " + directoryPath);
	}

	/**
	 * Affiche la fenêtre d'authentification au démarrage.
	 */
	private void showWindow() {
		if (mAuthView != null) {
			mAuthView.displayWindow();
			System.out.println("[APP] Fenêtre d'authentification affichée");
		}
	}

	/**
	 * Affiche l'application (point d'entrée public).
	 */
	public void show() {
		showWindow();
	}

	/**
	 * Authentifie un utilisateur avec son tag et son mot de passe.
	 *
	 * @param tag      Le tag de l'utilisateur
	 * @param password Le mot de passe
	 * @return true si la connexion a réussi, false sinon
	 */
	public boolean loginUser(String tag, String password) {
		System.out.println("[AUTH] Tentative de connexion pour : " + tag);

		java.util.Set<main.java.com.ubo.tp.message.datamodel.User> users = mDataManager.getUsers();

		for (main.java.com.ubo.tp.message.datamodel.User user : users) {
			if (user.getUserTag().equals(tag)) {
				if (user.verifyPassword(password)) {
					System.out.println("[AUTH] Connexion réussie pour : " + tag);

					SessionManager.getInstance().setCurrentUser(user);
					mAuthView.onLoginSuccess(tag);
					mMainView.showMainView(tag);

					return true;
				} else {
					System.out.println("[AUTH] Mot de passe incorrect pour : " + tag);
					return false;
				}
			}
		}

		System.out.println("[AUTH] Aucun utilisateur trouvé avec le tag : " + tag);
		return false;
	}

	/**
	 * Crée un nouvel utilisateur dans la base de données.
	 *
	 * @param tag      Le tag unique de l'utilisateur
	 * @param password Le mot de passe
	 * @param name     Le nom de l'utilisateur
	 * @return true si l'inscription a réussi, false sinon
	 */
	public boolean registerUser(String tag, String password, String name) {
		System.out.println("[AUTH] Tentative de création de compte pour : " + tag);

		java.util.Set<main.java.com.ubo.tp.message.datamodel.User> users = mDataManager.getUsers();

		for (main.java.com.ubo.tp.message.datamodel.User existingUser : users) {
			if (existingUser.getUserTag().equals(tag)) {
				System.out.println("[AUTH] Le tag " + tag + " est déjà utilisé");
				return false;
			}
		}

		System.out.println("[AUTH] Création d'un nouvel utilisateur : " + tag);

		main.java.com.ubo.tp.message.datamodel.User newUser =
				new main.java.com.ubo.tp.message.datamodel.User(tag, password, name);

		mDataManager.sendUser(newUser);

		System.out.println("[AUTH] Utilisateur créé avec succès : " + tag);
		return true;
	}

	// get mdatamanger


	public DataManager getmDataManager() {
		return mDataManager;
	}

	public void setmDataManager(DataManager mDataManager) {
		this.mDataManager = mDataManager;
	}
}