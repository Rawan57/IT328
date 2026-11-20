/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package server;

/**
 *
 * @author Dell
 */
import java.awt.*;
import java.io.*;
import java.net.*;
import javax.swing.*;

public class ClientLoginWindow extends JFrame {
    private JTextField usernameField;
    private JTextField ipField;
    private JButton connectButton;
    private StoreGUI store;
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private ConnectionManager connectionManager;

    // نفس ألوان الثيم وتنسيق المتجر
    private final Color THEME_BG = new Color(255, 245, 247);
    private final Color BUTTON_BG = new Color(208, 79, 115);  // الوردي الغامق الخاص بالمتجر
    private final Color TEXT_COLOR = new Color(50, 50, 50);

    public ClientLoginWindow() {
        setTitle("Charmify - Login");
        setSize(460, 390);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(THEME_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(25, 35, 25, 35));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 8, 10, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Logo
        ImageIcon icon = new ImageIcon(
            getClass().getResource("/images/logo2.png")
        );

        Image scaledImage = icon.getImage().getScaledInstance(
            180, 100, Image.SCALE_SMOOTH
        );

        JLabel logo = new JLabel(new ImageIcon(scaledImage));
        logo.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(logo, gbc);

        // Tagline
        JLabel tagline = new JLabel(
            "Little details, big charm!", SwingConstants.CENTER
        );
        tagline.setFont(new Font("SansSerif", Font.ITALIC, 12));
        tagline.setForeground(BUTTON_BG);

        gbc.gridy = 1;
        panel.add(tagline, gbc);

        // Username
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = 2;

        JLabel userLabel = new JLabel("Username:");
        userLabel.setForeground(TEXT_COLOR);
        userLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        panel.add(userLabel, gbc);

        gbc.gridx = 1;
        usernameField = createRoundedField("");
        panel.add(usernameField, gbc);

        // Server IP
        gbc.gridx = 0;
        gbc.gridy = 3;

        JLabel ipLabel = new JLabel("Server IP:");
        ipLabel.setForeground(TEXT_COLOR);
        ipLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        panel.add(ipLabel, gbc);

        gbc.gridx = 1;
        ipField = createRoundedField("127.0.0.1");
        panel.add(ipField, gbc);

        // Connect Button
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 8, 5, 8);

        connectButton = new JButton("Connect to Store") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(BUTTON_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
                g2.dispose();

                super.paintComponent(g);
            }
        };

        connectButton.setForeground(Color.WHITE);
        connectButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        connectButton.setPreferredSize(new Dimension(250, 42));
        connectButton.setContentAreaFilled(false);
        connectButton.setBorderPainted(false);
        connectButton.setFocusPainted(false);
        connectButton.setOpaque(false);
        connectButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        panel.add(connectButton, gbc);

        add(panel);

        connectButton.addActionListener(e -> connectToServer());
    }

    private JTextField createRoundedField(String text) {
        JTextField field = new JTextField(text, 15) {

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();

                g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
                );

                g2.setColor(Color.WHITE);
                g2.fillRoundRect(
                    1, 1, getWidth() - 2, getHeight() - 2, 20, 20
                );

                g2.dispose();
                super.paintComponent(g);
            }

            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();

                g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
                );

                g2.setColor(new Color(230, 150, 180));
                g2.setStroke(new BasicStroke(1.5f));

                g2.drawRoundRect(
                    1, 1, getWidth() - 3, getHeight() - 3, 20, 20
                );

                g2.dispose();
            }
        };

        field.setOpaque(false);
        field.setBorder(
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        );
        field.setFont(new Font("SansSerif", Font.PLAIN, 14));

        return field;
    }

    private void connectToServer() {
        String username = usernameField.getText().trim();
        String serverIp = ipField.getText().trim();

        if (username.isEmpty() || serverIp.isEmpty()) {
            CartFrame.showPinkMessage(this, "Error",
                "Please enter both username and server IP.");
            return;
        }

        try {
            socket = new Socket(serverIp, 5000);
            writer = new PrintWriter(socket.getOutputStream(), true);
            reader = new BufferedReader(
                new InputStreamReader(socket.getInputStream())
            );

            writer.println(username);
            String response = reader.readLine();

            if ("CONNECTED".equals(response)) {

                CartFrame.showPinkMessage(this, "Success",
                    "Connected successfully to Charmify!");

                connectionManager = new ConnectionManager(socket, username, this);
                store = new StoreGUI();
                store.setCartRequestSender(connectionManager::sendCartRequest);
                connectionManager.attachFrames(store, null);
                store.setVisible(true);

                this.dispose();

                writer.println("get stock");

                connectionManager.startListeningThread();

            } else {
                CartFrame.showPinkMessage(this, "Error",
                    "Connection rejected by server.");
            }

        } catch (IOException ex) {
            CartFrame.showPinkMessage(this, "Network Error",
                "Could not connect to server: " + ex.getMessage());
        }
    }

    public ConnectionManager getConnectionManager() {
        return connectionManager;
    }

    public void sendCartRequest(int productId, int quantity) {
        if (connectionManager != null) {
            connectionManager.sendCartRequest(productId, quantity);
        } else if (writer != null) {
            writer.println("CART:" + productId + ":" + quantity);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ClientLoginWindow().setVisible(true));
    }
}