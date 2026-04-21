package client.gui;

import client.network.ClientConnection;
import common.network.CommandRequest;
import common.network.CommandResponse;
import server.auth.PasswordHasher;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class LoginFrame extends JFrame {
    private static Map<String, Map<String, String>> languageResources;
    private static String currentLanguage = "ru";
    private JLabel titleLabel, nameLabel, passwordLabel;
    private JButton loginButton, registerButton;
    private JTextField nameField;
    private JPasswordField passwordField;
    private ClientConnection connection;
    private BufferedImage backgroundImage;

    private static class OutlinedLabel extends JLabel {
        private final Color strokeColor;

        public OutlinedLabel(String text, Color strokeColor) {
            super(text);
            this.strokeColor = strokeColor;
            setOpaque(false);
            setHorizontalAlignment(SwingConstants.CENTER);
        }

        @Override
        public Dimension getPreferredSize() {
            FontMetrics fm = getFontMetrics(getFont());
            int width = fm.stringWidth(getText()) + 20;
            int height = fm.getHeight() + 10;
            return new Dimension(width, height);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setFont(getFont());
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            FontMetrics fm = g2.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(getText())) / 2;
            int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;

            // Чёрная "тень"
            g2.setColor(strokeColor);
            g2.setStroke(new BasicStroke(2));
            g2.drawString(getText(), x - 1, y);
            g2.drawString(getText(), x + 1, y);
            g2.drawString(getText(), x, y - 1);
            g2.drawString(getText(), x, y + 1);

            // Белый текст
            g2.setColor(Color.WHITE);
            g2.drawString(getText(), x, y);

            g2.dispose();
        }
    }

    public LoginFrame(ClientConnection connection) {
        this.connection = connection;
        initializeLanguages();
        loadBackgroundImage();
        createAndShowGUI();
    }

    private void loadBackgroundImage() {
        try {
            backgroundImage = ImageIO.read(new File("back1.jpg"));
        } catch (Exception e) {
            backgroundImage = null;
            System.err.println("Не удалось загрузить изображение фона: " + e.getMessage());
        }
    }

    private void initializeLanguages() {
        languageResources = new HashMap<>();

        Map<String, String> ru = Map.ofEntries(
                Map.entry("title", "АВТОРИЗАЦИЯ"),
                Map.entry("name", "ЛОГИН"),
                Map.entry("name_placeholder", "Введите логин"),
                Map.entry("password", "ПАРОЛЬ"),
                Map.entry("password_placeholder", "Введите пароль"),
                Map.entry("login_button", "ВОЙТИ"),
                Map.entry("register_button", "РЕГИСТРАЦИЯ")
        );

        Map<String, String> en = Map.ofEntries(
                Map.entry("title", "LOGIN"),
                Map.entry("name", "USERNAME"),
                Map.entry("name_placeholder", "Enter username"),
                Map.entry("password", "PASSWORD"),
                Map.entry("password_placeholder", "Enter password"),
                Map.entry("login_button", "LOGIN"),
                Map.entry("register_button", "REGISTER")
        );

        Map<String, String> et = Map.ofEntries(
                Map.entry("title", "LOGI SISSE"),
                Map.entry("name", "KASUTAJANIMI"),
                Map.entry("name_placeholder", "Sisesta kasutajanimi"),
                Map.entry("password", "PAROOL"),
                Map.entry("password_placeholder", "Sisesta parool"),
                Map.entry("login_button", "LOGI SISSE"),
                Map.entry("register_button", "REGISTREERU")
        );

        Map<String, String> sq = Map.ofEntries(
                Map.entry("title", "HYRJE"),
                Map.entry("name", "EMRI I PËRDORUESIT"),
                Map.entry("name_placeholder", "Vendosni emrin e përdoruesit"),
                Map.entry("password", "FJALËKALIMI"),
                Map.entry("password_placeholder", "Vendosni fjalëkalimin"),
                Map.entry("login_button", "HYRJE"),
                Map.entry("register_button", "REGJISTRIMI")
        );

        languageResources.put("ru", ru);
        languageResources.put("en", en);
        languageResources.put("et", et);
        languageResources.put("sq", sq);
    }

    private void createAndShowGUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            setDefaultCloseOperation(EXIT_ON_CLOSE);
            setSize(500, 400);
            setLocationRelativeTo(null);

            JPanel backgroundPanel = new JPanel(new BorderLayout()) {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    if (backgroundImage != null) {
                        g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
                    } else {
                        g.setColor(Color.LIGHT_GRAY);
                        g.fillRect(0, 0, getWidth(), getHeight());
                    }
                }
            };

            JPanel languagePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            languagePanel.setOpaque(false);
            String[] languages = {"Русский", "English", "Eesti", "Shqip"};
            JComboBox<String> languageCombo = new JComboBox<>(languages);
            languageCombo.addActionListener(e -> {
                switch (languageCombo.getSelectedIndex()) {
                    case 0 -> currentLanguage = "ru";
                    case 1 -> currentLanguage = "en";
                    case 2 -> currentLanguage = "et";
                    case 3 -> currentLanguage = "sq";
                }
                updateTexts();
            });
            languagePanel.add(languageCombo);
            backgroundPanel.add(languagePanel, BorderLayout.NORTH);

            JPanel formPanel = createLoginPanel();
            backgroundPanel.add(formPanel, BorderLayout.CENTER);

            setContentPane(backgroundPanel);
            updateTexts();
            setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.insets = new Insets(10, 0, 10, 0);
        gbc.anchor = GridBagConstraints.CENTER;

        titleLabel = new JLabel();
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        panel.add(titleLabel, gbc);

        nameLabel = new OutlinedLabel("", Color.BLACK);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(nameLabel, gbc);

        nameField = new JTextField(20);
        styleTextField(nameField);
        panel.add(nameField, gbc);

        passwordLabel = new OutlinedLabel("", Color.BLACK);
        passwordLabel.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(passwordLabel, gbc);

        passwordField = new JPasswordField(20);
        stylePasswordField(passwordField);
        panel.add(passwordField, gbc);

        loginButton = new JButton();
        styleButton(loginButton, new Color(49, 123, 147));
        loginButton.addActionListener(e -> attemptLogin());

        registerButton = new JButton();
        styleButton(registerButton, new Color(100, 100, 100));
        registerButton.addActionListener(e -> attemptRegister());

        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);
        panel.add(buttonPanel, gbc);

        return panel;
    }

    private void attemptLogin() {
        String username = nameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Введите логин и пароль");
            return;
        }

        try {
            String hash = PasswordHasher.hash(password);
            CommandRequest request = new CommandRequest("login", new String[]{username, password}, null, username, hash);
            connection.sendRequest(request);
            CommandResponse response = connection.receiveResponse();

            if (response.isSuccess()) {
                dispose();
                new MainFrame(username, hash, connection);
            } else {
                JOptionPane.showMessageDialog(this, response.getMessage());
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Ошибка входа: " + ex.getMessage());
        }
    }

    private void attemptRegister() {
        String username = nameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Введите логин и пароль");
            return;
        }

        try {
            String hash = PasswordHasher.hash(password);
            CommandRequest request = new CommandRequest("register", new String[]{username, password}, null, username, hash);
            connection.sendRequest(request);
            CommandResponse response = connection.receiveResponse();
            JOptionPane.showMessageDialog(this, response.getMessage());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Ошибка регистрации: " + ex.getMessage());
        }
    }

    private void updateTexts() {
        Map<String, String> res = languageResources.get(currentLanguage);
        titleLabel.setText(res.get("title"));
        nameLabel.setText(res.get("name"));
        passwordLabel.setText(res.get("password"));
        loginButton.setText(res.get("login_button"));
        registerButton.setText(res.get("register_button"));
        nameField.setText(res.get("name_placeholder"));
        passwordField.setText(res.get("password_placeholder"));
        nameField.setForeground(Color.GRAY);
        passwordField.setForeground(Color.GRAY);
        passwordField.setEchoChar((char) 0);
    }

    private void styleTextField(JTextField field) {
        field.setFont(new Font("Arial", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(100, 100, 100)),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        field.setBackground(new Color(255, 255, 255, 240));

        field.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (field.getText().equals(languageResources.get(currentLanguage).get("name_placeholder"))) {
                    field.setText("");
                    field.setForeground(Color.BLACK);
                }
            }

            public void focusLost(java.awt.event.FocusEvent evt) {
                if (field.getText().isEmpty()) {
                    field.setText(languageResources.get(currentLanguage).get("name_placeholder"));
                    field.setForeground(Color.GRAY);
                }
            }
        });
    }

    private void stylePasswordField(JPasswordField field) {
        field.setFont(new Font("Arial", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(100, 100, 100)),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        field.setBackground(new Color(255, 255, 255, 240));

        field.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (String.valueOf(field.getPassword()).equals(languageResources.get(currentLanguage).get("password_placeholder"))) {
                    field.setText("");
                    field.setForeground(Color.BLACK);
                    field.setEchoChar('•');
                }
            }

            public void focusLost(java.awt.event.FocusEvent evt) {
                if (field.getPassword().length == 0) {
                    field.setText(languageResources.get(currentLanguage).get("password_placeholder"));
                    field.setForeground(Color.GRAY);
                    field.setEchoChar((char) 0);
                }
            }
        });
    }

    private void styleButton(JButton button, Color color) {
        button.setFont(new Font("Arial", Font.BOLD, 24));
        button.setForeground(Color.WHITE);
        button.setBackground(color);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(color.darker());
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(color);
            }
        });
    }
}
