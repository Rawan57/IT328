import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;

public class ClientLoginWindow extends JFrame {
    private JTextField usernameField;
    private JTextField ipField;
    private JButton connectButton;
    
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;

    // نفس ألوان الثيم وتنسيق المتجر
    private final Color THEME_BG = new Color(255, 245, 247); 
    private final Color BUTTON_BG = new Color(208, 79, 115);  // الوردي الغامق الخاص بالمتجر
    private final Color TEXT_COLOR = new Color(50, 50, 50);    

    public ClientLoginWindow() {
        setTitle("Charmify - Login");
        setSize(400, 240);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(THEME_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        JLabel userLabel = new JLabel("Username:");
        userLabel.setForeground(TEXT_COLOR);
        userLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        panel.add(userLabel, gbc);

        gbc.gridx = 1; gbc.gridy = 0;
        usernameField = new JTextField(15);
        panel.add(usernameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        JLabel ipLabel = new JLabel("Server IP:");
        ipLabel.setForeground(TEXT_COLOR);
        ipLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        panel.add(ipLabel, gbc);

        gbc.gridx = 1; gbc.gridy = 1;
        ipField = new JTextField("127.0.0.1", 15);
        panel.add(ipField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 2;
        connectButton = new JButton("Connect to Store");
        
        // إعدادات إضافية لضمان ظهور اللون والنص بوضوح تام على الماك
        connectButton.setBackground(BUTTON_BG);
        connectButton.setForeground(Color.WHITE);
        connectButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        connectButton.setOpaque(true);
        connectButton.setContentAreaFilled(false); // لحل مشكلة نظام الماك في إخفاء الألوان
        connectButton.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BUTTON_BG);
                g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 8, 8);
                g2.dispose();
                super.paint(g, c);
            }
        });
        
        connectButton.setFocusPainted(false);
        connectButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        panel.add(connectButton, gbc);

        add(panel);

        connectButton.addActionListener(e -> connectToServer());
    }

    private void connectToServer() {
        String username = usernameField.getText().trim();
        String serverIp = ipField.getText().trim();

        if (username.isEmpty() || serverIp.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both username and server IP.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            socket = new Socket(serverIp, 5000); 
            writer = new PrintWriter(socket.getOutputStream(), true);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            writer.println(username);

            String response = reader.readLine();
            if (response != null && response.contains("CONNECTED")) {
                JOptionPane.showMessageDialog(this, "Connected successfully to Charmify!", "Success", JOptionPane.INFORMATION_MESSAGE);
                this.dispose(); 
                startListeningThread();
            } else {
                JOptionPane.showMessageDialog(this, "Connection rejected by server.", "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Could not connect to server: " + ex.getMessage(), "Network Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void startListeningThread() {
        Thread listenerThread = new Thread(() -> {
            try {
                String serverMessage;
                while ((serverMessage = reader.readLine()) != null) {
                    System.out.println("Update received from server: " + serverMessage);
                }
            } catch (IOException e) {
                System.out.println("Connection closed or lost.");
            }
        });
        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ClientLoginWindow().setVisible(true));
    }
}
