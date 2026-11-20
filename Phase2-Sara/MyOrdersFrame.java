package server;

/**
 * My Orders window (Phase 2 - Store Interface & Order History).
 * Shows the user's previous orders, the details of the selected order,
 * and a Cancel Order button. Orders are identified by their timestamp.
 *
 * This class only displays orders. Sending the cancel request and
 * storing orders on the server are done by other parts of the project.
 */
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import javax.swing.*;
import javax.swing.table.*;

public class MyOrdersFrame extends JFrame {
    // Same colors as CartFrame so both windows look the same.
    private final Color rose = new Color(0xC23B68);
    private final Color palePink = new Color(255, 235, 245);
    private final Color bg = new Color(255, 250, 253);
    private final Color borderPink = new Color(247, 220, 232);
    private final Color muted = new Color(145, 125, 135);

    // Order status values shown in the table.
    public static final String CONFIRMED = "Confirmed";
    public static final String CANCELLING = "Cancelling...";

    private final StoreGUI storeGUI;
    private final Inventory inventory;
    // All orders of this user, newest first.
    private final List<OrderRow> orders = new ArrayList<>();
    private final SimpleDateFormat timeFormat =
            new SimpleDateFormat("yyyy-MM-dd  hh:mm:ss a", Locale.US);

    private JTable ordersTable, detailsTable;
    private DefaultTableModel ordersModel, detailsModel;
    private JLabel detailsTitle, detailsTotal, countLabel;
    private JButton cancelButton;

    /** One product line inside an order. */
    public static class OrderLine {
        final int productId;
        final String name;
        final double unitPrice;
        final int quantity;

        OrderLine(int productId, String name, double unitPrice, int quantity) {
            this.productId = productId;
            this.name = name;
            this.unitPrice = unitPrice;
            this.quantity = quantity;
        }

        double getSubtotal() { return unitPrice * quantity; }
    }

    /** What the window shows for one order. */
    public static class OrderRow {
        final long timestamp;          // order ID
        final List<OrderLine> lines;
        String status;

        OrderRow(long timestamp, List<OrderLine> lines, String status) {
            this.timestamp = timestamp;
            this.lines = lines;
            this.status = status;
        }

        int getItemCount() {
            int count = 0;
            for (OrderLine line : lines) count += line.quantity;
            return count;
        }

        double getTotal() {
            double total = 0;
            for (OrderLine line : lines) total += line.getSubtotal();
            return total;
        }
    }

    public MyOrdersFrame(StoreGUI storeGUI, Inventory inventory) {
        this.storeGUI = storeGUI;
        this.inventory = inventory;

        setTitle("Charmify | My Orders");
        setSize(980, 620);
        setMinimumSize(new Dimension(820, 520));
        // Hide instead of dispose, so orders that arrive while closed are kept.
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(bg);
        setLayout(new BorderLayout(12, 12));
        createHeader();
        createCenter();
        createBottom();
        refreshOrders();
    }

    // ---------------------------------------------------------------- layout

    private void createHeader() {
        JPanel header = new JPanel(new BorderLayout(8, 0));
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createEmptyBorder(18, 24, 18, 24));
        JLabel title = new JLabel("Charmify - My Orders");
        title.setFont(new Font("Serif", Font.BOLD | Font.ITALIC, 28));
        title.setForeground(rose);
        JLabel sub = new JLabel("Select an order to see its details.");
        sub.setForeground(muted);
        JPanel titles = new JPanel(new GridLayout(2, 1, 0, 4));
        titles.setOpaque(false);
        titles.add(title);
        titles.add(sub);

        JButton back = button("Back to Store", rose, Color.WHITE);
        back.setFont(new Font("Arial", Font.BOLD, 12));
        back.setPreferredSize(new Dimension(130, 36));
        back.addActionListener(e -> backToStore());
        JPanel backHolder = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        backHolder.setOpaque(false);
        backHolder.add(back);

        header.add(titles, BorderLayout.CENTER);
        header.add(backHolder, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);
    }

    // Orders list on top, details of the selected order below.
    private void createCenter() {
        String[] orderColumns = {"Order Time", "Items", "Total", "Status"};
        ordersModel = new DefaultTableModel(orderColumns, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        ordersTable = styledTable(ordersModel);
        ordersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ordersTable.getColumnModel().getColumn(0).setPreferredWidth(260);
        ordersTable.getColumnModel().getColumn(3).setCellRenderer(new StatusRenderer());
        // Show details whenever the selected order changes.
        ordersTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) showDetails();
        });

        String[] detailColumns = {"Product", "Unit Price", "Quantity", "Subtotal"};
        detailsModel = new DefaultTableModel(detailColumns, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        detailsTable = styledTable(detailsModel);
        detailsTable.setRowSelectionAllowed(false);

        countLabel = new JLabel("Previous Orders");
        JPanel ordersBox = section(countLabel, ordersTable, null);

        detailsTitle = new JLabel("Order Details");
        detailsTotal = new JLabel(" ");
        detailsTotal.setFont(new Font("Arial", Font.BOLD, 18));
        detailsTotal.setForeground(rose);
        detailsTotal.setHorizontalAlignment(SwingConstants.RIGHT);
        JPanel detailsBox = section(detailsTitle, detailsTable, detailsTotal);

        JPanel center = new JPanel(new GridLayout(2, 1, 0, 12));
        center.setBackground(bg);
        center.setBorder(BorderFactory.createEmptyBorder(4, 22, 0, 22));
        center.add(ordersBox);
        center.add(detailsBox);
        add(center, BorderLayout.CENTER);
    }

    private void createBottom() {
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 9, 4));
        bottom.setBackground(bg);
        bottom.setBorder(BorderFactory.createEmptyBorder(0, 22, 18, 22));
        cancelButton = button("Cancel Order", rose, Color.WHITE);
        cancelButton.addActionListener(e -> cancelSelected());
        bottom.add(cancelButton);
        add(bottom, BorderLayout.SOUTH);
    }

    // A titled box that holds one table (and an optional footer).
    private JPanel section(JLabel title, JTable table, JComponent footer) {
        JPanel box = new JPanel(new BorderLayout(0, 6));
        box.setOpaque(false);
        title.setFont(new Font("Arial", Font.BOLD, 15));
        title.setForeground(rose);
        box.add(title, BorderLayout.NORTH);
        JScrollPane pane = new JScrollPane(table);
        pane.setBorder(BorderFactory.createLineBorder(borderPink));
        pane.getViewport().setBackground(Color.WHITE);
        box.add(pane, BorderLayout.CENTER);
        if (footer != null) box.add(footer, BorderLayout.SOUTH);
        return box;
    }

    private JTable styledTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setRowHeight(32);
        table.setFillsViewportHeight(true);
        table.setSelectionBackground(new Color(255, 220, 235));
        table.setSelectionForeground(Color.BLACK);
        table.setGridColor(borderPink);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setBackground(palePink);
        table.getTableHeader().setForeground(rose);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 1; i < table.getColumnCount(); i++)
            table.getColumnModel().getColumn(i).setCellRenderer(center);
        return table;
    }

    private JButton button(String text, Color background, Color foreground) {
        JButton b = new JButton(text);
        b.setFont(new Font("Arial", Font.BOLD, 13));
        b.setBackground(background);
        b.setForeground(foreground);
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderPink),
                BorderFactory.createEmptyBorder(9, 13, 9, 13)));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    // Colors the Status column: rose = confirmed, orange = waiting for server.
    private class StatusRenderer extends DefaultTableCellRenderer {
        StatusRenderer() { setHorizontalAlignment(SwingConstants.CENTER); }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean selected, boolean focus, int row, int col) {
            super.getTableCellRendererComponent(table, value, selected, focus, row, col);
            setFont(getFont().deriveFont(Font.BOLD));
            if (CANCELLING.equals(value)) setForeground(new Color(200, 120, 40));
            else setForeground(rose);
            return this;
        }
    }

    // ------------------------------------------------------- order updates

    /**
     * Turns "101-2,202-1" (productId-quantity pairs) into order lines.
     * Name and price are taken from Inventory.
     */
    public List<OrderLine> parseItems(String itemsSpec) {
        List<OrderLine> lines = new ArrayList<>();
        if (itemsSpec == null || itemsSpec.trim().isEmpty()) return lines;
        for (String pair : itemsSpec.split(",")) {
            String[] parts = pair.trim().split("-");
            if (parts.length != 2) continue;
            try {
                int productId = Integer.parseInt(parts[0].trim());
                int quantity = Integer.parseInt(parts[1].trim());
                Product product = inventory.getProductById(productId);
                if (product == null || quantity <= 0) continue;
                lines.add(new OrderLine(productId, product.getName().trim(),
                        product.getPrice(), quantity));
            } catch (NumberFormatException e) {
                System.out.println("Invalid order item: " + pair);
            }
        }
        return lines;
    }

    /** Add a new order, or update it if the timestamp already exists. */
    public void addOrUpdateOrder(long timestamp, String itemsSpec, String status) {
        List<OrderLine> lines = parseItems(itemsSpec);
        OrderRow existing = findOrder(timestamp);
        if (existing != null) {
            existing.lines.clear();
            existing.lines.addAll(lines);
            existing.status = status;
        } else {
            orders.add(new OrderRow(timestamp, lines, status));
            // Newest order first.
            orders.sort((a, b) -> Long.compare(b.timestamp, a.timestamp));
        }
        refreshOrders();
    }

    /** Change only the status of an order (e.g. after cancel reply). */
    public void setOrderStatus(long timestamp, String status) {
        OrderRow order = findOrder(timestamp);
        if (order == null) return;
        order.status = status;
        refreshOrders();
    }

    /** Remove a cancelled order from the list completely. */
    public void removeOrder(long timestamp) {
        OrderRow order = findOrder(timestamp);
        if (order == null) return;
        orders.remove(order);
        refreshOrders();
    }

    /** Remove every order before reloading the list from the server. */
    public void clearOrders() {
        orders.clear();
        refreshOrders();
    }

    public List<OrderRow> getOrders() { return new ArrayList<>(orders); }

    /** Readable date and time for an order timestamp. */
    public String formatTime(long timestamp) {
        return timeFormat.format(new Date(timestamp));
    }

    private OrderRow findOrder(long timestamp) {
        for (OrderRow order : orders)
            if (order.timestamp == timestamp) return order;
        return null;
    }

    // Rebuild the orders table and keep the same order selected.
    public void refreshOrders() {
        OrderRow selected = selectedOrder();
        ordersModel.setRowCount(0);
        for (OrderRow order : orders) {
            ordersModel.addRow(new Object[]{
                timeFormat.format(new Date(order.timestamp)),
                order.getItemCount(),
                String.format(Locale.US, "%.2f SAR", order.getTotal()),
                order.status});
        }
        countLabel.setText("Previous Orders (" + orders.size() + ")");

        int row = selected == null ? -1 : orders.indexOf(selected);
        if (row < 0 && !orders.isEmpty()) row = 0;   // select newest by default
        if (row >= 0) ordersTable.setRowSelectionInterval(row, row);
        showDetails();
    }

    private OrderRow selectedOrder() {
        int row = ordersTable == null ? -1 : ordersTable.getSelectedRow();
        return row >= 0 && row < orders.size() ? orders.get(row) : null;
    }

    // Fill the details table for the selected order.
    private void showDetails() {
        detailsModel.setRowCount(0);
        OrderRow order = selectedOrder();
        if (order == null) {
            detailsTitle.setText(orders.isEmpty()
                    ? "You have no orders yet." : "Order Details");
            detailsTotal.setText(" ");
            return;
        }
        for (OrderLine line : order.lines) {
            detailsModel.addRow(new Object[]{
                line.name,
                String.format(Locale.US, "%.2f SAR", line.unitPrice),
                line.quantity,
                String.format(Locale.US, "%.2f SAR", line.getSubtotal())});
        }
        detailsTitle.setText("Order Details - " + timeFormat.format(new Date(order.timestamp))
                + "   (" + order.status + ")");
        detailsTotal.setText(String.format(Locale.US, "Total: %.2f SAR", order.getTotal()));
    }

    // ------------------------------------------------------------ actions

    private void cancelSelected() {
        OrderRow order = selectedOrder();
        // The button stays enabled (so its text stays white),
        // so the checks are done here instead.
        if (order == null) {
            CartFrame.showPinkMessage(this, "No order selected",
                    "Please select an order first.");
            return;
        }
        if (!CONFIRMED.equals(order.status)) {
            CartFrame.showPinkMessage(this, "Please wait",
                    "This order is already being cancelled.");
            return;
        }
        String when = timeFormat.format(new Date(order.timestamp));
        if (!confirm("Cancel Order", "Cancel the order placed at " + when + "?")) return;

        // Wait for the server reply before removing it from the list.
        order.status = CANCELLING;
        refreshOrders();
        storeGUI.requestCancelOrder(order.timestamp);
    }

    private void backToStore() {
        storeGUI.setVisible(true);
        storeGUI.toFront();
        setVisible(false);
    }

    // Pink Yes/No dialog in the same style as CartFrame.showPinkMessage.
    private boolean confirm(String title, String message) {
        JDialog dialog = new JDialog(this, title, true);
        dialog.setSize(440, 190);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);
        final boolean[] answer = {false};

        JPanel panel = new JPanel(new BorderLayout(10, 15));
        panel.setBackground(palePink);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        JLabel label = new JLabel(message, SwingConstants.CENTER);
        label.setForeground(rose);
        label.setFont(new Font("Arial", Font.BOLD, 13));

        JButton yes = button("Yes, cancel it", rose, Color.WHITE);
        JButton no = button("No", Color.WHITE, rose);
        yes.addActionListener(e -> { answer[0] = true; dialog.dispose(); });
        no.addActionListener(e -> dialog.dispose());
        JPanel buttons = new JPanel();
        buttons.setOpaque(false);
        buttons.add(yes);
        buttons.add(no);

        panel.add(label, BorderLayout.CENTER);
        panel.add(buttons, BorderLayout.SOUTH);
        dialog.add(panel);
        dialog.setVisible(true);   // modal: waits here until closed
        return answer[0];
    }
}
