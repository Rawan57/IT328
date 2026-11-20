package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class ConnectionManager {

    private final Socket socket;
    private final BufferedReader reader;
    private final PrintWriter writer;
    private final String username;
    private final JFrame ownerFrame;
    private StoreGUI storeGUI;
    private CartFrame cartFrame;

    public ConnectionManager(Socket socket, String username, JFrame ownerFrame) throws IOException {
        this.socket = socket;
        this.username = username;
        this.ownerFrame = ownerFrame;
        this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.writer = new PrintWriter(socket.getOutputStream(), true);
    }

    public void attachFrames(StoreGUI storeGUI, CartFrame cartFrame) {
        this.storeGUI = storeGUI;
        this.cartFrame = cartFrame;
        if (this.cartFrame != null) {
            this.cartFrame.setOrderHandler(this::sendOrderRequest);
        }
    }

    public void sendCartRequest(int productId, int quantity) {
        if (writer != null) {
            writer.println("CART:" + productId + ":" + quantity);
        }
    }

    public void sendOrderRequest(List<Cart.CartItem> items) {
        if (writer == null || items == null || items.isEmpty()) {
            return;
        }
        StringBuilder sb = new StringBuilder("ORDER:" + username);
        for (Cart.CartItem item : items) {
            int productId = (item.getCategory() + 1) * 100 + item.getIndex() + 1;
            sb.append(";").append(productId).append(",").append(item.getQuantity());
        }
        writer.println(sb.toString());
    }

    public void sendCancelOrderRequest(String orderTimestampOrId) {
        if (writer == null || orderTimestampOrId == null || orderTimestampOrId.trim().isEmpty()) {
            return;
        }
        writer.println("CANCEL_ORDER:" + username + ":" + orderTimestampOrId.trim());
    }

    public void onOrderConfirmed() {
        SwingUtilities.invokeLater(() -> {
            if (cartFrame != null) {
                cartFrame.clearAfterSuccessfulOrder();
            }
            JFrame target = (cartFrame != null && cartFrame.isDisplayable()) ? cartFrame : ownerFrame;
            CartFrame.showPinkMessage(
                target,
                "Order Confirmed",
                "Your order has been confirmed!"
            );
        });
    }

    public void onOrderCancelled() {
        SwingUtilities.invokeLater(() -> {
            JFrame target = (cartFrame != null && cartFrame.isDisplayable()) ? cartFrame : ownerFrame;
            CartFrame.showPinkMessage(
                target,
                "Order Cancelled",
                "Your order has been cancelled successfully."
            );
        });
    }

    public void onCancelRejected() {
        SwingUtilities.invokeLater(() -> {
            JFrame target = (cartFrame != null && cartFrame.isDisplayable()) ? cartFrame : ownerFrame;
            CartFrame.showPinkMessage(
                target,
                "Cancellation Failed",
                "Sorry, this order could not be cancelled."
            );
        });
    }

    public void handleServerMessage(String serverMessage) {
        if (serverMessage == null) {
            return;
        }

        if (serverMessage.startsWith("stock:")) {
            try {
                String[] parts = serverMessage.split(":");
                int productId = Integer.parseInt(parts[1]);
                int newStock = Integer.parseInt(parts[2]);
                if (storeGUI != null) {
                    int category = (productId / 100) - 1;
                    int index = (productId % 100) - 1;
                    storeGUI.updateProductStock(category, index, newStock);
                }
            } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                System.out.println("Invalid stock update");
            }
        } else if (serverMessage.startsWith("UPDATED_CART:")) {
            try {
                String[] parts = serverMessage.split(":");
                int productId = Integer.parseInt(parts[1]);
                int updatedQuantity = Integer.parseInt(parts[2]);
                if (storeGUI != null) {
                    int category = (productId / 100) - 1;
                    int index = (productId % 100) - 1;
                    storeGUI.applyServerCartUpdate(category, index, updatedQuantity);
                }
            } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                System.out.println("Invalid cart update");
            }
        } else if (serverMessage.equals("ORDER_CONFIRMED") || serverMessage.startsWith("ORDER_CONFIRMED:")) {
            onOrderConfirmed();
        } else if (serverMessage.startsWith("ORDER_REJECTED")) {
            String reason = serverMessage.contains(":")
                    ? serverMessage.substring(serverMessage.indexOf(":") + 1).trim()
                    : "Your order could not be completed.";
            SwingUtilities.invokeLater(() -> {
                if (cartFrame != null) {
                    cartFrame.handleOrderRejected(reason);
                }
            });
        } else if (serverMessage.equals("ORDER_CANCELLED") || serverMessage.startsWith("ORDER_CANCELLED:")) {
            onOrderCancelled();
        } else if (serverMessage.equals("CANCEL_REJECTED") || serverMessage.startsWith("CANCEL_REJECTED:")) {
            onCancelRejected();
        }
    }

    public void startListeningThread() {
        Thread listenerThread = new Thread(() -> {
            try {
                String serverMessage;
                while ((serverMessage = reader.readLine()) != null) {
                    handleServerMessage(serverMessage);
                }
            } catch (IOException e) {
                System.out.println("Connection closed or lost.");
            }
        });
        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    public Socket getSocket() {
        return socket;
    }

    public BufferedReader getReader() {
        return reader;
    }

    public PrintWriter getWriter() {
        return writer;
    }
}