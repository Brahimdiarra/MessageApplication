package main.java.com.ubo.tp.message.ihm.reactions;

import java.util.*;

/**
 * Store en mémoire des réactions emoji sur les messages.
 * Singleton — les réactions sont conservées pendant toute la session.
 *
 * @author BRAHIM
 */
public class ReactionStore {

    private static final ReactionStore INSTANCE = new ReactionStore();

    /** messageUUID → (emoji → Set‹userUUID›) */
    private final Map<UUID, Map<String, Set<UUID>>> data = new HashMap<>();

    /** Observateurs UI à notifier à chaque changement. */
    private final List<Runnable> listeners = new ArrayList<>();

    private ReactionStore() {}

    public static ReactionStore getInstance() { return INSTANCE; }

    // ─────────────────────────────────────────────────────────────────────────
    // Manipulation des réactions
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Ajoute ou retire une réaction (toggle).
     * Si l'utilisateur a déjà réagi avec cet emoji, la réaction est supprimée.
     */
    public void toggle(UUID messageId, String emoji, UUID userId) {
        Map<String, Set<UUID>> emojiMap =
                data.computeIfAbsent(messageId, k -> new LinkedHashMap<>());
        Set<UUID> users = emojiMap.computeIfAbsent(emoji, k -> new HashSet<>());

        if (!users.remove(userId)) {
            users.add(userId);
        }
        if (users.isEmpty()) emojiMap.remove(emoji);

        notifyListeners();
    }

    /**
     * Retourne les réactions d'un message sous la forme emoji → nombre de réactions.
     */
    public Map<String, Integer> getCountsFor(UUID messageId) {
        Map<String, Set<UUID>> emojiMap = data.get(messageId);
        if (emojiMap == null || emojiMap.isEmpty()) return Collections.emptyMap();

        Map<String, Integer> result = new LinkedHashMap<>();
        for (Map.Entry<String, Set<UUID>> e : emojiMap.entrySet()) {
            if (!e.getValue().isEmpty()) result.put(e.getKey(), e.getValue().size());
        }
        return result;
    }

    /** Indique si un utilisateur a déjà réagi avec un emoji donné. */
    public boolean hasReacted(UUID messageId, String emoji, UUID userId) {
        Map<String, Set<UUID>> emojiMap = data.get(messageId);
        if (emojiMap == null) return false;
        Set<UUID> users = emojiMap.get(emoji);
        return users != null && users.contains(userId);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Observateurs
    // ─────────────────────────────────────────────────────────────────────────

    public void addListener(Runnable listener) { listeners.add(listener); }

    private void notifyListeners() {
        for (Runnable l : listeners) l.run();
    }
}
