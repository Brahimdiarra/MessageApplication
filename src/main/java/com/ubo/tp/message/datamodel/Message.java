package main.java.com.ubo.tp.message.datamodel;

import java.util.UUID;

/**
 * Classe du modèle représentant un message.
 *
 * @author S.Lucas
 */
public class Message extends AbstractMessageAppObject {

	/**
	 * Utilisateur source du message.
	 */
	protected final User mSender;

	/**
	 * Destinataire du message.
	 */
	protected final UUID mRecipient;

	/**
	 * Date d'émission du message.
	 */
	protected final long mEmissionDate;

	/**
	 * Corps du message.
	 */
	protected final String mText;

	/**
	 * Données de l'image en base64 (null si pas d'image).
	 */
	protected final String mImageData;

	/**
	 * Constructeur sans image.
	 *
	 * @param sender    utilisateur à l'origine du message.
	 * @param recipient destinataire du message.
	 * @param text      corps du message.
	 */
	public Message(User sender, UUID recipient, String text) {
		this(UUID.randomUUID(), sender, recipient, System.currentTimeMillis(), text, null);
	}

	/**
	 * Constructeur avec image.
	 *
	 * @param sender    utilisateur à l'origine du message.
	 * @param recipient destinataire du message.
	 * @param text      corps du message.
	 * @param imageData données image encodées en base64 (peut être null).
	 */
	public Message(User sender, UUID recipient, String text, String imageData) {
		this(UUID.randomUUID(), sender, recipient, System.currentTimeMillis(), text, imageData);
	}

	/**
	 * Constructeur complet.
	 *
	 * @param messageUuid  identifiant du message.
	 * @param sender       utilisateur à l'origine du message.
	 * @param recipient    destinataire du message.
	 * @param emissionDate date d'émission du message.
	 * @param text         corps du message.
	 */
	public Message(UUID messageUuid, User sender, UUID recipient, long emissionDate, String text) {
		this(messageUuid, sender, recipient, emissionDate, text, null);
	}

	/**
	 * Constructeur complet avec image.
	 *
	 * @param messageUuid  identifiant du message.
	 * @param sender       utilisateur à l'origine du message.
	 * @param recipient    destinataire du message.
	 * @param emissionDate date d'émission du message.
	 * @param text         corps du message.
	 * @param imageData    données image encodées en base64 (peut être null).
	 */
	public Message(UUID messageUuid, User sender, UUID recipient, long emissionDate, String text, String imageData) {
		super(messageUuid);
		mSender = sender;
		mRecipient = recipient;
		mEmissionDate = emissionDate;
		mText = text;
		mImageData = imageData;
	}

	/**
	 * @return l'utilisateur source du message.
	 */
	public User getSender() {
		return mSender;
	}

	/**
	 * @return le destinataire du message.
	 */
	public UUID getRecipient() {
		return mRecipient;
	}

	/**
	 * @return le corps du message.
	 */
	public String getText() {
		return mText;
	}

	/**
	 * @return les données image encodées en base64, ou null si pas d'image.
	 */
	public String getImageData() {
		return mImageData;
	}

	/**
	 * @return true si ce message contient une image.
	 */
	public boolean hasImage() {
		return mImageData != null && !mImageData.isEmpty();
	}

	/**
	 * Retourne la date d'émission.
	 */
	public long getEmissionDate() {
		return this.mEmissionDate;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();

		sb.append("[");
		sb.append(this.getClass().getName());
		sb.append("] : ");
		sb.append(this.getUuid());
		sb.append(" {");
		sb.append(this.getText());
		sb.append("}");

		return sb.toString();
	}
}
