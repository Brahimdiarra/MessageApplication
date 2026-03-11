package main.java.com.ubo.tp.message.ihm.notification;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Fenêtre flottante de notification (toast) non bloquante.
 *
 * Apparaît en bas à droite de la fenêtre parente pendant 5 secondes,
 * puis se ferme automatiquement. Un clic dessus la ferme immédiatement.
 *
 * @author BRAHIM
 */
public class ToastNotification extends JWindow {

    private static final Color COLOR_BG      = new Color(30, 58, 138);  // bleu foncé
    private static final Color COLOR_BODY_BG = new Color(239, 246, 255); // bleu très clair
    private static final Color COLOR_BORDER  = new Color(59, 130, 246);  // bleu vif
    private static final int   DISPLAY_MS    = 5000;
    private static final int   WIDTH         = 320;

    private ToastNotification(Window parent, String title, String body) {
        super(parent);

        JPanel content = new JPanel(new BorderLayout(0, 0));
        content.setBorder(BorderFactory.createLineBorder(COLOR_BORDER, 2));

        // ── En-tête ───────────────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout(8, 0));
        header.setBackground(COLOR_BG);
        header.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        titleLabel.setForeground(Color.WHITE);

        JLabel closeHint = new JLabel("✕");
        closeHint.setFont(new Font("SansSerif", Font.PLAIN, 11));
        closeHint.setForeground(new Color(186, 207, 255));

        header.add(titleLabel, BorderLayout.CENTER);
        header.add(closeHint, BorderLayout.EAST);

        // ── Corps ─────────────────────────────────────────────────────────
        JPanel bodyPanel = new JPanel(new BorderLayout());
        bodyPanel.setBackground(COLOR_BODY_BG);
        bodyPanel.setBorder(BorderFactory.createEmptyBorder(8, 12, 10, 12));

        String preview = body.length() > 80 ? body.substring(0, 77) + "..." : body;
        JTextArea bodyLabel = new JTextArea(preview);
        bodyLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        bodyLabel.setForeground(new Color(30, 41, 59));
        bodyLabel.setBackground(COLOR_BODY_BG);
        bodyLabel.setEditable(false);
        bodyLabel.setLineWrap(true);
        bodyLabel.setWrapStyleWord(true);
        bodyLabel.setOpaque(false);
        bodyPanel.add(bodyLabel, BorderLayout.CENTER);

        content.add(header, BorderLayout.NORTH);
        content.add(bodyPanel, BorderLayout.CENTER);

        setContentPane(content);
        setPreferredSize(new Dimension(WIDTH, 0));
        pack();

        // Positionner en bas à droite de la fenêtre parente
        positionBottomRight(parent);

        // Fermer au clic
        addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { dispose(); }
        });
        content.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { dispose(); }
        });

        // Auto-fermeture après DISPLAY_MS ms
        Timer timer = new Timer(DISPLAY_MS, e -> dispose());
        timer.setRepeats(false);
        timer.start();
    }

    private void positionBottomRight(Window parent) {
        if (parent != null && parent.isVisible()) {
            Rectangle parentBounds = parent.getBounds();
            int x = parentBounds.x + parentBounds.width  - getWidth()  - 16;
            int y = parentBounds.y + parentBounds.height - getHeight() - 48;
            setLocation(x, y);
        } else {
            // Fallback : coin bas-droit de l'écran
            Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
            setLocation(screen.width - getWidth() - 16, screen.height - getHeight() - 48);
        }
    }

    /**
     * Affiche une notification toast.
     *
     * @param parent Fenêtre parente (pour le positionnement)
     * @param title  Titre de la notification
     * @param body   Corps du message
     */
    public static void show(Window parent, String title, String body) {
        ToastNotification toast = new ToastNotification(parent, title, body);
        toast.setVisible(true);
    }
}
