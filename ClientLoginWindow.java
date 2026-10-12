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

    public ClientLoginWindow() {
        setTitle("Charmify - Login");
        setSize(350, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // تصميم الواجهة البسيطة
        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        panel.add(new JLabel("Username:"));
        usernameField = new JTextField();
        panel.add(usernameField);

        panel.add(new JLabel("Server IP:"));
        ipField = new JTextField("127.0.0.1");
        panel.add(ipField);

        connectButton = new JButton("Connect");
        panel.add(new JLabel()); // فراغ لتنسيق الزر
        panel.add(connectButton);

        add(panel);

        // الحدث عند الضغط على زر الاتصال
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
            // 1. فتح الـ Socket والاتصال بالسيرفر (البنت 1)
            socket = new Socket(serverIp, 5000); // استخدام رقم البورت المتفق عليه مع البنت 1
            writer = new PrintWriter(socket.getOutputStream(), true);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // 2. إرسال اسم المستخدم للسيرفر للتسجيل
            writer.println(username);

            // 3. قراءة استجابة السيرفر الأولى (تأكيد الاتصال)
            String response = reader.readLine();
            if (response != null && response.contains("CONNECTED")) {
                JOptionPane.showMessageDialog(this, "Connected successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                
                // الانتقال لواجهة المتجر لاحقاً وإغلاق نافذة الدخول
                this.dispose();

                // 4. تشغيل خيط الاستماع بالخلفية لاستقبال تحديثات المخزون المستمرة من السيرفر
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
                    // استقبال التحديثات القادمة من السيرفر (مثل تغير المخزون)
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