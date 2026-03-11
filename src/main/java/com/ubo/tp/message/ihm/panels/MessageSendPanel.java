package main.java.com.ubo.tp.message.ihm.panels;

import main.java.com.ubo.tp.message.core.DataManager;
import main.java.com.ubo.tp.message.core.SessionManager;
import main.java.com.ubo.tp.message.core.database.IDatabaseObserver;
import main.java.com.ubo.tp.message.datamodel.Channel;
import main.java.com.ubo.tp.message.datamodel.Message;
import main.java.com.ubo.tp.message.datamodel.User;
import main.java.com.ubo.tp.message.ihm.MessageAppMainView;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.ArrayList;
import javax.imageio.ImageIO;

/**
 * Panel de saisie et d'envoi de messages.
 * Le destinataire est défini via setRecipient() depuis la sidebar.
 *
 * @author BRAHIM
 */
public class MessageSendPanel extends JPanel implements IDatabaseObserver {

    private JTextArea messageTextArea;
    private JButton sendButton;
    private DataManager dataManager;
    private JLabel charCountLabel;
    private JButton emojiPickerBtn;
    private JButton imageAttachBtn;
    private JLabel imagePreviewLabel;
    private TitledBorder sendBorder;

    /** Destinataire courant (défini par clic dans la sidebar). */
    private UUID currentRecipientUuid = null;
    private String currentRecipientName = null;

    /** Image en attente d'envoi (base64). */
    private String pendingImageData = null;

    private JPopupMenu autocompletePopup = new JPopupMenu();
    private int autocompleteColonPos = -1;

    // ── Couleurs Discord-like ─────────────────────────────────────────────
    private static final Color COLOR_INPUT_BG  = new Color(64, 68, 75);
    private static final Color COLOR_INPUT_TEXT = new Color(220, 221, 222);

    /** Table de correspondance raccourci → emoji. */
    private static final Map<String, String> EMOJI_MAP = new LinkedHashMap<>();
    static {
        EMOJI_MAP.put(":smile:",      "\uD83D\uDE0A");
        EMOJI_MAP.put(":smirk:",      "\uD83D\uDE0F");
        EMOJI_MAP.put(":sad:",        "\uD83D\uDE22");
        EMOJI_MAP.put(":heart:",      "\u2764\uFE0F");
        EMOJI_MAP.put(":fire:",       "\uD83D\uDD25");
        EMOJI_MAP.put(":thumbsup:",   "\uD83D\uDC4D");
        EMOJI_MAP.put(":thumbsdown:", "\uD83D\uDC4E");
        EMOJI_MAP.put(":laugh:",      "\uD83D\uDE02");
        EMOJI_MAP.put(":wink:",       "\uD83D\uDE09");
        EMOJI_MAP.put(":wow:",        "\uD83D\uDE2E");
        EMOJI_MAP.put(":party:",      "\uD83C\uDF89");
        EMOJI_MAP.put(":eyes:",       "\uD83D\uDC40");
        EMOJI_MAP.put(":star:",       "\u2B50");
        EMOJI_MAP.put(":check:",      "\u2705");
        EMOJI_MAP.put(":wave:",       "\uD83D\uDC4B");
        EMOJI_MAP.put(":clap:",       "\uD83D\uDC4F");
        EMOJI_MAP.put(":100:",        "\uD83D\uDCAF");
        EMOJI_MAP.put(":rocket:",     "\uD83D\uDE80");
    }
    private static final Map<String, Color> EMOJI_COLORS = new LinkedHashMap<>();
    static {
        EMOJI_COLORS.put(":smile:",      new Color(255, 230, 100));
        EMOJI_COLORS.put(":smirk:",      new Color(255, 200,  80));
        EMOJI_COLORS.put(":sad:",        new Color(147, 197, 253));
        EMOJI_COLORS.put(":heart:",      new Color(252, 165, 165));
        EMOJI_COLORS.put(":fire:",       new Color(253, 186,  74));
        EMOJI_COLORS.put(":thumbsup:",   new Color( 94, 234, 212));
        EMOJI_COLORS.put(":thumbsdown:", new Color(196, 181, 253));
        EMOJI_COLORS.put(":laugh:",      new Color(254, 240, 138));
        EMOJI_COLORS.put(":wink:",       new Color(253, 224, 132));
        EMOJI_COLORS.put(":wow:",        new Color(134, 239, 172));
        EMOJI_COLORS.put(":party:",      new Color(216, 180, 254));
        EMOJI_COLORS.put(":eyes:",       new Color(186, 230, 253));
        EMOJI_COLORS.put(":star:",       new Color(253, 224,  71));
        EMOJI_COLORS.put(":check:",      new Color(134, 239, 172));
        EMOJI_COLORS.put(":wave:",       new Color(147, 197, 253));
        EMOJI_COLORS.put(":clap:",       new Color(253, 186,  74));
        EMOJI_COLORS.put(":100:",        new Color(252, 165, 165));
        EMOJI_COLORS.put(":rocket:",     new Color(147, 197, 253));
    }

    public MessageSendPanel(DataManager dataManager) {
        this.dataManager = dataManager;
        initComponents();
    }

    /**
     * Définit le destinataire courant (appelé depuis la sidebar au clic sur user/canal).
     *
     * @param recipientUuid  UUID du destinataire
     * @param displayName    Nom affiché (ex: "@alice" ou "#general")
     */
    public void setRecipient(UUID recipientUuid, String displayName) {
        this.currentRecipientUuid = recipientUuid;
        this.currentRecipientName = displayName;
        sendBorder.setTitle("Envoyer à " + displayName);
        setEnabled(true);
        messageTextArea.setEnabled(true);
        messageTextArea.requestFocusInWindow();
        repaint();
    }

    /** Réinitialise le destinataire (aucune conversation sélectionnée). */
    public void clearRecipient() {
        this.currentRecipientUuid = null;
        this.currentRecipientName = null;
        sendBorder.setTitle("Sélectionnez une conversation...");
        repaint();
    }

    private void initComponents() {
        setLayout(new BorderLayout(0, 0));
        setBackground(new Color(54, 57, 63));

        sendBorder = BorderFactory.createTitledBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(32, 34, 37)),
                "Sélectionnez une conversation...",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("SansSerif", Font.BOLD, 12),
                new Color(148, 155, 164));
        setBorder(sendBorder);

        // ── Zone de saisie ────────────────────────────────────────────────
        messageTextArea = new JTextArea(3, 40);
        messageTextArea.setLineWrap(true);
        messageTextArea.setWrapStyleWord(true);
        messageTextArea.setFont(new Font("SansSerif", Font.PLAIN, 14));
        messageTextArea.setBackground(COLOR_INPUT_BG);
        messageTextArea.setForeground(COLOR_INPUT_TEXT);
        messageTextArea.setCaretColor(Color.WHITE);
        messageTextArea.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

        JScrollPane scrollPane = new JScrollPane(messageTextArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        scrollPane.setBackground(new Color(54, 57, 63));
        scrollPane.getViewport().setBackground(COLOR_INPUT_BG);

        // ── Panel de prévisualisation image ───────────────────────────────
        JPanel imagePreviewPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        imagePreviewPanel.setBackground(new Color(47, 49, 54));
        imagePreviewPanel.setVisible(false);

        imagePreviewLabel = new JLabel();
        imagePreviewLabel.setBorder(BorderFactory.createLineBorder(new Color(88, 101, 242), 1));
        imagePreviewPanel.add(imagePreviewLabel);

        JButton imageClearBtn = new JButton("✕");
        imageClearBtn.setFont(new Font("SansSerif", Font.BOLD, 11));
        imageClearBtn.setForeground(new Color(237, 66, 69));
        imageClearBtn.setBorderPainted(false);
        imageClearBtn.setFocusPainted(false);
        imageClearBtn.setOpaque(false);
        imageClearBtn.addActionListener(e -> {
            pendingImageData = null;
            imagePreviewLabel.setIcon(null);
            imagePreviewPanel.setVisible(false);
            revalidate();
        });
        imagePreviewPanel.add(imageClearBtn);

        // ── Barre de boutons en bas ───────────────────────────────────────
        JPanel bottomBar = new JPanel(new BorderLayout(0, 0));
        bottomBar.setBackground(new Color(54, 57, 63));
        bottomBar.setBorder(BorderFactory.createEmptyBorder(0, 10, 8, 10));

        JPanel leftButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        leftButtons.setOpaque(false);

        imageAttachBtn = makeIconButton("\uD83D\uDDBC", "Joindre une image");
        imageAttachBtn.addActionListener(e -> attachImage(imagePreviewPanel));
        leftButtons.add(imageAttachBtn);

        emojiPickerBtn = makeIconButton("\uD83D\uDE0A", "Insérer un emoji");
        emojiPickerBtn.addActionListener(e -> showEmojiPicker());
        leftButtons.add(emojiPickerBtn);

        charCountLabel = new JLabel("0/200");
        charCountLabel.setFont(new Font("SansSerif", Font.ITALIC, 11));
        charCountLabel.setForeground(new Color(114, 118, 125));
        leftButtons.add(charCountLabel);

        sendButton = new JButton("Envoyer ▶");
        sendButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        sendButton.setBackground(new Color(88, 101, 242));  // Discord blurple
        sendButton.setForeground(Color.WHITE);
        sendButton.setOpaque(true);
        sendButton.setBorderPainted(false);
        sendButton.setFocusPainted(false);
        sendButton.setPreferredSize(new Dimension(120, 32));
        sendButton.addActionListener(e -> sendMessage());

        bottomBar.add(leftButtons, BorderLayout.WEST);
        bottomBar.add(sendButton, BorderLayout.EAST);

        // ── Assemblage ────────────────────────────────────────────────────
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        centerPanel.add(imagePreviewPanel, BorderLayout.SOUTH);

        add(centerPanel, BorderLayout.CENTER);
        add(bottomBar, BorderLayout.SOUTH);

        // Ctrl+Enter pour envoyer
        messageTextArea.getInputMap().put(KeyStroke.getKeyStroke("control ENTER"), "send");
        messageTextArea.getActionMap().put("send", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { sendMessage(); }
        });

        // Compteur de caractères
        messageTextArea.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { updateCount(); checkAutocomplete(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { updateCount(); hideAutocomplete(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updateCount(); }
            private void updateCount() {
                int n = messageTextArea.getText().length();
                charCountLabel.setText(n + "/200");
                charCountLabel.setForeground(n > 200 ? new Color(237, 66, 69) :
                        n > 180 ? new Color(255, 160, 0) : new Color(114, 118, 125));
            }
        });
    }

    private JButton makeIconButton(String icon, String tooltip) {
        JButton btn = new JButton(icon);
        btn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        btn.setMargin(new Insets(2, 6, 2, 6));
        btn.setToolTipText(tooltip);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setOpaque(false);
        btn.setForeground(new Color(148, 155, 164));
        return btn;
    }

    // ── Logique d'envoi ──────────────────────────────────────────────────

    private void attachImage(JPanel imagePreviewPanel) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Choisir une image");
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Images (PNG, JPG, GIF)", "png", "jpg", "jpeg", "gif"));
        chooser.setAcceptAllFileFilterUsed(false);
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        try {
            File file = chooser.getSelectedFile();
            BufferedImage original = ImageIO.read(file);
            if (original == null) { showErr("Impossible de lire l'image."); return; }
            int tw = Math.min(original.getWidth(), 300);
            int th = (int) ((double) original.getHeight() * tw / original.getWidth());
            BufferedImage scaled = new BufferedImage(tw, th, BufferedImage.TYPE_INT_RGB);
            scaled.getGraphics().drawImage(original.getScaledInstance(tw, th, Image.SCALE_SMOOTH), 0, 0, null);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(scaled, "png", baos);
            pendingImageData = Base64.getEncoder().encodeToString(baos.toByteArray());
            int ph = 80, pw = (int) ((double) tw * ph / th);
            imagePreviewLabel.setIcon(new ImageIcon(scaled.getScaledInstance(pw, ph, Image.SCALE_SMOOTH)));
            imagePreviewPanel.setVisible(true);
            revalidate();
        } catch (Exception ex) {
            showErr("Erreur image : " + ex.getMessage());
        }
    }

    private void sendMessage() {
        if (currentRecipientUuid == null) {
            showErr("Sélectionnez d'abord un utilisateur ou un canal dans la liste de gauche.");
            return;
        }
        String text = applyEmojiShortcuts(messageTextArea.getText().trim());
        if (text.isEmpty() && pendingImageData == null) {
            showErr("Le message ne peut pas être vide !");
            return;
        }
        if (text.length() > 200) {
            showErr("Le message ne peut pas dépasser 200 caractères ! (" + text.length() + "/200)");
            return;
        }
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) { showErr("Aucun utilisateur connecté !"); return; }
        try {
            dataManager.sendMessage(new Message(currentUser, currentRecipientUuid, text, pendingImageData));
            messageTextArea.setText("");
            pendingImageData = null;
            imagePreviewLabel.setIcon(null);
            Container parent = imagePreviewLabel.getParent();
            if (parent != null) parent.setVisible(false);
            revalidate();
        } catch (Exception e) {
            showErr("Erreur d'envoi : " + e.getMessage());
        }
    }

    private void showErr(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Erreur", JOptionPane.WARNING_MESSAGE);
    }

    // ── IDatabaseObserver ────────────────────────────────────────────────
    @Override public void notifyUserAdded(User u)       { /* plus de liste destinataire */ }
    @Override public void notifyUserDeleted(User u)     { }
    @Override public void notifyUserModified(User u)    { }
    @Override public void notifyMessageAdded(Message m)    { }
    @Override public void notifyMessageDeleted(Message m)  { }
    @Override public void notifyMessageModified(Message m) { }
    @Override public void notifyChannelAdded(Channel c)    { }
    @Override public void notifyChannelDeleted(Channel c)  { }
    @Override public void notifyChannelModified(Channel c) { }

    // ── Emoji autocomplete ───────────────────────────────────────────────

    private String applyEmojiShortcuts(String text) {
        for (Map.Entry<String, String> e : EMOJI_MAP.entrySet())
            text = text.replace(e.getKey(), e.getValue());
        return text;
    }

    private void checkAutocomplete() {
        SwingUtilities.invokeLater(() -> {
            String text = messageTextArea.getText();
            int caret = messageTextArea.getCaretPosition();
            if (caret == 0) { hideAutocomplete(); return; }
            int colonPos = -1;
            for (int i = caret - 1; i >= 0; i--) {
                char ch = text.charAt(i);
                if (ch == ':') { colonPos = i; break; }
                if (ch == ' ' || ch == '\n') break;
            }
            if (colonPos < 0) { hideAutocomplete(); return; }
            String partial = text.substring(colonPos, caret);
            if (partial.length() > 1 && partial.endsWith(":")) { hideAutocomplete(); return; }
            List<String> matches = EMOJI_MAP.keySet().stream()
                    .filter(k -> k.startsWith(partial) && !k.equals(partial))
                    .collect(Collectors.toList());
            if (matches.isEmpty()) { hideAutocomplete(); return; }
            autocompleteColonPos = colonPos;
            showAutocompletePopup(matches);
        });
    }

    private void showAutocompletePopup(List<String> matches) {
        autocompletePopup.removeAll();
        for (String shortcut : matches) {
            String emoji = EMOJI_MAP.get(shortcut);
            JMenuItem item = new JMenuItem(emoji + "  " + shortcut);
            item.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));
            final int pos = autocompleteColonPos;
            item.addActionListener(ae -> SwingUtilities.invokeLater(() -> {
                try {
                    int c = messageTextArea.getCaretPosition();
                    messageTextArea.getDocument().remove(pos, c - pos);
                    messageTextArea.getDocument().insertString(pos, emoji, null);
                } catch (Exception ex) { /* ignore */ }
            }));
            autocompletePopup.add(item);
        }
        try {
            java.awt.Rectangle r = messageTextArea.modelToView(autocompleteColonPos);
            if (r != null)
                autocompletePopup.show(messageTextArea, r.x, r.y - autocompletePopup.getPreferredSize().height - 4);
        } catch (Exception ex) { /* ignore */ }
    }

    private void hideAutocomplete() {
        if (autocompletePopup.isVisible()) autocompletePopup.setVisible(false);
    }

    private void showEmojiPicker() {
        JPopupMenu picker = new JPopupMenu();
        JPanel grid = new JPanel(new java.awt.GridLayout(0, 4, 3, 3));
        grid.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        grid.setBackground(new Color(47, 49, 54));
        for (Map.Entry<String, String> entry : EMOJI_MAP.entrySet()) {
            Color bg = EMOJI_COLORS.getOrDefault(entry.getKey(), new Color(80, 80, 80));
            JLabel lbl = new JLabel(entry.getValue(), JLabel.CENTER);
            lbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
            lbl.setToolTipText(entry.getKey());
            lbl.setBackground(bg); lbl.setOpaque(true);
            lbl.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(bg.darker(), 1, true),
                    BorderFactory.createEmptyBorder(4, 6, 4, 6)));
            lbl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            lbl.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                    messageTextArea.insert(entry.getValue(), messageTextArea.getCaretPosition());
                    picker.setVisible(false);
                    messageTextArea.requestFocusInWindow();
                }
                @Override public void mouseEntered(java.awt.event.MouseEvent e) { lbl.setBackground(bg.darker()); }
                @Override public void mouseExited(java.awt.event.MouseEvent e)  { lbl.setBackground(bg); }
            });
            grid.add(lbl);
        }
        picker.add(grid);
        picker.show(emojiPickerBtn, 0, -picker.getPreferredSize().height - 2);
    }
}
