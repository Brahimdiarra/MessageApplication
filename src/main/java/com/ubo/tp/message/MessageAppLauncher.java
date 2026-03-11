package main.java.com.ubo.tp.message;

import main.java.com.ubo.tp.message.core.DataManager;
import main.java.com.ubo.tp.message.core.database.Database;
import main.java.com.ubo.tp.message.core.database.DbConnector;
import main.java.com.ubo.tp.message.core.database.EntityManager;
import main.java.com.ubo.tp.message.ihm.ConsoleObserver;
import main.java.com.ubo.tp.message.ihm.MessageApp;


/**
 * Classe de lancement de l'application.
 *
 * @author S.Lucas
 */
public class MessageAppLauncher {

	/**
	 * Indique si le mode bouchoné est activé.
	 */
	protected static boolean IS_MOCK_ENABLED = false;

	/**
	 * Launcher.
	 *
	 * @param args
	 */
	public static void main(String[] args) {

		Database database = new Database();

		// Enregistrer l'observateur console
		ConsoleObserver consoleObserver = new ConsoleObserver();
		database.addObserver(consoleObserver);

		EntityManager entityManager = new EntityManager(database);

		DataManager dataManager = new DataManager(database, entityManager);

		DbConnector dbConnector = new DbConnector(database);



		MessageApp messageApp = new MessageApp(dataManager);
		messageApp.init();
		messageApp.show();

	}
}
