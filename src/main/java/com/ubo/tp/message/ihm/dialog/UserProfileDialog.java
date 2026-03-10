package main.java.com.ubo.tp.message.ihm.dialog;

import main.java.com.ubo.tp.message.core.DataManager;
import main.java.com.ubo.tp.message.core.SessionManager;
import main.java.com.ubo.tp.message.datamodel.User;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

/**
 * Dialogue de modification du profil de l'utilisateur connecté (USR-009).
 *
 * @author BRAHIM
 */
public class UserProfileDialog extends JDialog {

    // ── Palette de couleurs ───────────────────────────────────────────────
    private static final Color COLOR_HEADER_BG  = new Color(30, 58, 138);   // bleu foncé
    private static final Color COLOR_HEADER_FG  = Color.WHITE;
    private static final Color COLOR_SECTION_BG = new Color(241, 245, 249); // gris très clair
    private static final Color COLOR_ACCENT     = new Color(59, 130, 246);  // bleu vif
    private static final Color COLOR_BORDER     = new Color(203, 213, 225); // gris bordure

    private final DataManager dataManager;

    private JTextField nameField;
    private JPasswordField currentPasswordField;
    private JPasswordField newPasswordField;
    private JPasswordField confirmPasswordField;

    public UserProfileDialog(Frame parent, DataManager dataManager) {
        super(parent, "Mon profil", true);
        this.dataManager = dataManager;
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setResizable(false);

        User user = SessionManager.getInstance().getCurrentUser();

        // ── En-tête coloré ────────────────────────────────────────────────
        JPanel headerPanel = new JPanel(new BorderLayout(12, 0));
        headerPanel.setBackground(COLOR_HEADER_BG);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        JLabel avatarLabel = new JLabel("👤");
        avatarLabel.setFont(new Font("SansSerif", Font.PLAIN, 36));
        avatarLabel.setForeground(COLOR_HEADER_FG);

        JPanel headerText = new JPanel();
        headerText.setLayout(new BoxLayout(headerText, BoxLayout.Y_AXIS));
        headerText.setOpaque(false);

        JLabel tagLabel = new JLabel("@" + (user != null ? user.getUserTag() : "—"));
        tagLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        tagLabel.setForeground(COLOR_HEADER_FG);

        JLabel nameHintLabel = new JLabel(user != null ? user.getName() : "");
        nameHintLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        nameHintLabel.setForeground(new Color(186, 207, 255));

        headerText.add(tagLabel);
        headerText.add(Box.createVerticalStrut(3));
        headerText.add(nameHintLabel);

        headerPanel.add(avatarLabel, BorderLayout.WEST);
        headerPanel.add(headerText, BorderLayout.CENTER);
        add(headerPanel, BorderLayout.NORTH);

        // ── Corps principal ───────────────────────────────────────────────
        JPanel bodyPanel = new JPanel(new GridBagLayout());
        bodyPanel.setBackground(Color.WHITE);
        bodyPanel.setBorder(BorderFactory.createEmptyBorder(16, 20, 8, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Section informations
        JPanel infoSection = createSection("Informations");
        infoSection.setLayout(new GridBagLayout());
        GridBagConstraints igbc = new GridBagConstraints();
        igbc.insets = new Insets(5, 8, 5, 8);
        igbc.fill = GridBagConstraints.HORIZONTAL;

        // Tag (lecture seule)
        igbc.gridx = 0; igbc.gridy = 0; igbc.weightx = 0.35;
        infoSection.add(makeLabel("Tag (@) :"), igbc);
        igbc.gridx = 1; igbc.weightx = 0.65;
        JTextField tagField = new JTextField(user != null ? user.getUserTag() : "");
        tagField.setEditable(false);
        tagField.setBackground(COLOR_SECTION_BG);
        tagField.setForeground(Color.GRAY);
        tagField.setBorder(BorderFactory.createLineBorder(COLOR_BORDER));
        tagField.setToolTipText("Le tag ne peut pas être modifié");
        infoSection.add(tagField, igbc);

        // Nom
        igbc.gridx = 0; igbc.gridy = 1; igbc.weightx = 0.35;
        infoSection.add(makeLabel("Nom :"), igbc);
        igbc.gridx = 1; igbc.weightx = 0.65;
        nameField = new JTextField(user != null ? user.getName() : "");
        nameField.setBorder(BorderFactory.createLineBorder(COLOR_BORDER));
        infoSection.add(nameField, igbc);

        // Section mot de passe
        JPanel pwdSection = createSection("Changer le mot de passe");
        pwdSection.setLayout(new GridBagLayout());
        GridBagConstraints pgbc = new GridBagConstraints();
        pgbc.insets = new Insets(5, 8, 5, 8);
        pgbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel hint = new JLabel("Laisser vide pour conserver le mot de passe actuel");
        hint.setFont(hint.getFont().deriveFont(Font.ITALIC, 11f));
        hint.setForeground(Color.GRAY);
        pgbc.gridx = 0; pgbc.gridy = 0; pgbc.gridwidth = 2;
        pwdSection.add(hint, pgbc);
        pgbc.gridwidth = 1;

        pgbc.gridx = 0; pgbc.gridy = 1; pgbc.weightx = 0.4;
        pwdSection.add(makeLabel("Mot de passe actuel :"), pgbc);
        pgbc.gridx = 1; pgbc.weightx = 0.6;
        currentPasswordField = new JPasswordField();
        currentPasswordField.setBorder(BorderFactory.createLineBorder(COLOR_BORDER));
        pwdSection.add(currentPasswordField, pgbc);

        pgbc.gridx = 0; pgbc.gridy = 2; pgbc.weightx = 0.4;
        pwdSection.add(makeLabel("Nouveau mot de passe :"), pgbc);
        pgbc.gridx = 1; pgbc.weightx = 0.6;
        newPasswordField = new JPasswordField();
        newPasswordField.setBorder(BorderFactory.createLineBorder(COLOR_BORDER));
        pwdSection.add(newPasswordField, pgbc);

        pgbc.gridx = 0; pgbc.gridy = 3; pgbc.weightx = 0.4;
        pwdSection.add(makeLabel("Confirmer :"), pgbc);
        pgbc.gridx = 1; pgbc.weightx = 0.6;
        confirmPasswordField = new JPasswordField();
        confirmPasswordField.setBorder(BorderFactory.createLineBorder(COLOR_BORDER));
        pwdSection.add(confirmPasswordField, pgbc);

        // Assemblage corps
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 1;
        bodyPanel.add(infoSection, gbc);
        gbc.gridy = 1;
        bodyPanel.add(Box.createVerticalStrut(8), gbc);
        gbc.gridy = 2;
        bodyPanel.add(pwdSection, gbc);

        add(bodyPanel, BorderLayout.CENTER);

        // ── Boutons ───────────────────────────────────────────────────────
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBackground(COLOR_SECTION_BG);
        buttonPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, COLOR_BORDER));

        JButton cancelButton = new JButton("Annuler");
        cancelButton.addActionListener(e -> dispose());

        JButton saveButton = new JButton("Enregistrer");
        saveButton.setBackground(COLOR_ACCENT);
        saveButton.setForeground(Color.WHITE);
        saveButton.setOpaque(true);
        saveButton.setBorderPainted(false);
        saveButton.addActionListener(e -> saveProfile());

        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);
        add(buttonPanel, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(saveButton);

        pack();
        setMinimumSize(new Dimension(480, getHeight()));
        setLocationRelativeTo(getParent());
    }

    /** Crée un JPanel avec un titre en bordure colorée. */
    private JPanel createSection(String title) {
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        TitledBorder border = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(COLOR_ACCENT, 1, true),
                title,
                TitledBorder.LEFT, TitledBorder.TOP
        );
        border.setTitleColor(COLOR_ACCENT);
        border.setTitleFont(new Font("SansSerif", Font.BOLD, 12));
        panel.setBorder(BorderFactory.createCompoundBorder(
                border,
                BorderFactory.createEmptyBorder(4, 4, 6, 4)
        ));
        return panel;
    }

    /** Crée un JLabel stylisé pour les libellés de formulaire. */
    private JLabel makeLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.PLAIN, 12));
        return label;
    }

    private void saveProfile() {
        User user = SessionManager.getInstance().getCurrentUser();
        if (user == null) {
            JOptionPane.showMessageDialog(this, "Aucun utilisateur connecté.", "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String newName = nameField.getText().trim();
        String currentPassword = new String(currentPasswordField.getPassword());
        String newPassword = new String(newPasswordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        if (newName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Le nom ne peut pas être vide.", "Erreur", JOptionPane.ERROR_MESSAGE);
            nameField.requestFocus();
            return;
        }

        boolean changingPassword = !newPassword.isEmpty() || !currentPassword.isEmpty();

        if (changingPassword) {
            if (!user.verifyPassword(currentPassword)) {
                JOptionPane.showMessageDialog(this, "Mot de passe actuel incorrect.", "Erreur", JOptionPane.ERROR_MESSAGE);
                currentPasswordField.setText("");
                currentPasswordField.requestFocus();
                return;
            }
            if (newPassword.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Le nouveau mot de passe ne peut pas être vide.", "Erreur", JOptionPane.ERROR_MESSAGE);
                newPasswordField.requestFocus();
                return;
            }
            if (!newPassword.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(this, "Les mots de passe ne correspondent pas.", "Erreur", JOptionPane.ERROR_MESSAGE);
                confirmPasswordField.setText("");
                confirmPasswordField.requestFocus();
                return;
            }
            user.setUserPassword(newPassword);
        }

        user.setName(newName);
        dataManager.sendUser(user);

        System.out.println("[PROFIL] Profil mis à jour : @" + user.getUserTag() + " / " + newName);
        JOptionPane.showMessageDialog(this, "Profil mis à jour avec succès !", "Succès", JOptionPane.INFORMATION_MESSAGE);
        dispose();
    }

    public static void showDialog(Frame parent, DataManager dataManager) {
        new UserProfileDialog(parent, dataManager).setVisible(true);
    }
}
