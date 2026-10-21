import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.table.*;

/** Cart interface, quantity controls, total, and stock updates. */
public class CartFrame extends JFrame {
    private final Color rose = new Color(0xC23B68);
    private final Color palePink = new Color(255, 235, 245);
    private final Color bg = new Color(255, 250, 253);
    private final Color borderPink = new Color(247, 220, 232);
    private final Cart cart;
    private final StoreGUI storeGUI;
    private final Inventory inventory;
    private final List<Product>[] productsByCategory;
    private JTable table;
    private DefaultTableModel model;
    private JLabel totalLabel, countLabel;

    public CartFrame(Cart cart) {
        this(cart, null, new Inventory());
    }

    public CartFrame(Cart cart, StoreGUI storeGUI) {
        this(cart, storeGUI, new Inventory());
    }

    @SuppressWarnings("unchecked")
    public CartFrame(Cart cart, StoreGUI storeGUI, Inventory inventory) {
        this.cart = cart;
        this.storeGUI = storeGUI;
        this.inventory = inventory;
        productsByCategory = (List<Product>[]) new List[3];
        for (int i = 0; i < 3; i++) productsByCategory[i] = new ArrayList<>();

        // Read products from Inventory in category order.
        for (Product product : inventory.getAllProducts()) {
            int category = categoryIndex(product.getCategory());
            if (category >= 0) productsByCategory[category].add(product);
        }

        setTitle("Charmify | My Cart");
        setSize(920, 550);
        setMinimumSize(new Dimension(760, 450));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(bg);
        setLayout(new BorderLayout(12, 12));
        createHeader();
        createTable();
        createBottom();
        refreshCart();
    }

    private int categoryIndex(Category category) {
        if (category == Category.PHONE_CASE) return 0;
        if (category == Category.CHARM) return 1;
        if (category == Category.STICKER) return 2;
        return -1;
    }

    private Product getStoreProduct(int category, int index) {
        if (category < 0 || category >= productsByCategory.length
                || index < 0 || index >= productsByCategory[category].size()) return null;
        return productsByCategory[category].get(index);
    }

    private void createHeader() {
        JPanel header = new JPanel(new BorderLayout(8, 0));
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createEmptyBorder(18, 24, 18, 24));
        JLabel title = new JLabel("Charmify - My Cart");
        title.setFont(new Font("Serif", Font.BOLD | Font.ITALIC, 28));
        title.setForeground(rose);
        JLabel sub = new JLabel("Little details, big charm!  ♥");
        sub.setForeground(new Color(145, 125, 135));
        JPanel titles = new JPanel(new GridLayout(2, 1, 0, 4));
        titles.setOpaque(false);
        titles.add(title);
        titles.add(sub);
        JButton backTop = button("Back to Store", rose, Color.WHITE);
        backTop.setFont(new Font("Arial", Font.BOLD, 12));
        backTop.setBorder(BorderFactory.createEmptyBorder(7, 12, 7, 12));
        backTop.setPreferredSize(new Dimension(130, 36));
        backTop.addActionListener(e -> backToStore());
        header.add(titles, BorderLayout.CENTER);
        JPanel backHolder = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        backHolder.setOpaque(false);
        backHolder.add(backTop);
        header.add(backHolder, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);
    }

    private void createTable() {
        String[] columns = {"Product", "Unit Price", "Quantity", "Available Stock", "Subtotal"};
        model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(model);
        table.setRowHeight(34);
        table.setSelectionBackground(new Color(255, 220, 235));
        table.setGridColor(borderPink);
        table.getTableHeader().setBackground(palePink);
        table.getTableHeader().setForeground(rose);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 13));
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 1; i < table.getColumnCount(); i++)
            table.getColumnModel().getColumn(i).setCellRenderer(center);
        JScrollPane pane = new JScrollPane(table);
        pane.setBorder(BorderFactory.createLineBorder(borderPink));
        JPanel holder = new JPanel(new BorderLayout());
        holder.setBackground(bg);
        holder.setBorder(BorderFactory.createEmptyBorder(12, 22, 8, 22));
        holder.add(pane);
        add(holder, BorderLayout.CENTER);
    }

    private void createBottom() {
        JPanel bottom = new JPanel(new BorderLayout(8, 12));
        bottom.setBackground(bg);
        bottom.setBorder(BorderFactory.createEmptyBorder(0, 22, 20, 22));
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 9, 4));
        buttons.setOpaque(false);
        JButton plus = button("+ Quantity", palePink, rose);
        JButton minus = button("- Quantity", palePink, rose);
        JButton remove = button("Remove Item", Color.WHITE, rose);
        plus.addActionListener(e -> changeQuantity(1));
        minus.addActionListener(e -> changeQuantity(-1));
        remove.addActionListener(e -> removeSelected());
        buttons.add(plus); buttons.add(minus); buttons.add(remove);

        JPanel summary = new JPanel(new BorderLayout());
        summary.setBackground(Color.WHITE);
        summary.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(borderPink),
            BorderFactory.createEmptyBorder(12, 16, 12, 16)));
        countLabel = new JLabel("Items: 0");
        totalLabel = new JLabel("Total: 0.00 SAR");
        totalLabel.setFont(new Font("Arial", Font.BOLD, 21));
        totalLabel.setForeground(rose);
        summary.add(countLabel, BorderLayout.WEST);
        summary.add(totalLabel, BorderLayout.EAST);
        bottom.add(buttons, BorderLayout.NORTH);
        bottom.add(summary, BorderLayout.SOUTH);
        add(bottom, BorderLayout.SOUTH);
    }

    // Return to the store from the top-right button.
    private void backToStore() {
        if (storeGUI != null) {
            storeGUI.setVisible(true);
            storeGUI.toFront();
        }
        dispose();
    }

    private JButton button(String text, Color background, Color foreground) {
        JButton b = new JButton(text);
        b.setFont(new Font("Arial", Font.BOLD, 13));
        b.setBackground(background); b.setForeground(foreground);
        b.setFocusPainted(false); b.setOpaque(true);
        b.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(borderPink),
            BorderFactory.createEmptyBorder(9, 13, 9, 13)));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    // Add a product using Inventory data.
    public void addFromStore(int category, int index, int quantity) {
        Product product = getStoreProduct(category, index);
        if (product == null || quantity < 1) return;
        int currentStock = product.getStock();
        int already = cart.getStoreQuantity(category, index);
        int remaining = Math.max(0, currentStock - already);
        if (quantity > remaining) {
            JOptionPane.showMessageDialog(this, "Not enough stock available.",
                    "Stock limit", JOptionPane.WARNING_MESSAGE);
            return;
        }
        cart.addStoreProduct(category, index, product.getName().trim(),
                product.getPrice(), quantity, currentStock);
        syncStoreQuantity(category, index);
        refreshCart();
    }

    // Apply new stock to Inventory and the cart.
    public void updateStock(int category, int index, int newStock) {
        if (newStock < 0) return;
        Product product = getStoreProduct(category, index);
        if (product == null) return;
        SwingUtilities.invokeLater(() -> {
            product.setStock(newStock);
            cart.updateStoreStock(category, index, newStock);
            syncStoreQuantity(category, index);
            if (storeGUI != null) storeGUI.updateProductStock(category, index, newStock);
            refreshCart();
        });
    }

    private void changeQuantity(int delta) {
        int row = table.getSelectedRow();
        if (row < 0) { noSelection(); return; }
        Cart.CartItem item = itemAt(row);
        if (item == null) return;
        int before = item.getQuantity();
        cart.changeQuantity(item.getId(), delta);
        Cart.CartItem after = find(item.getId());
        if (delta > 0 && after != null && before == after.getQuantity()
                && before >= after.getAvailableStock()) {
            JOptionPane.showMessageDialog(this, "You cannot exceed available stock.",
                    "Stock limit", JOptionPane.INFORMATION_MESSAGE);
        }
        syncStoreQuantity(item.getCategory(), item.getIndex());
        refreshCart();
    }

    private void removeSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { noSelection(); return; }
        Cart.CartItem item = itemAt(row);
        if (item == null) return;
        cart.removeProduct(item.getId());
        syncStoreQuantity(item.getCategory(), item.getIndex());
        refreshCart();
    }

    private Cart.CartItem itemAt(int row) {
        List<Cart.CartItem> items = cart.getItems();
        int modelRow = table.convertRowIndexToModel(row);
        return modelRow >= 0 && modelRow < items.size() ? items.get(modelRow) : null;
    }

    private Cart.CartItem find(String id) {
        for (Cart.CartItem item : cart.getItems())
            if (item.getId().equals(id)) return item;
        return null;
    }

    private void noSelection() {
        JOptionPane.showMessageDialog(this, "Please select a product first.",
                "No product selected", JOptionPane.INFORMATION_MESSAGE);
    }

    private void syncStoreQuantity(int category, int index) {
        if (storeGUI != null && getStoreProduct(category, index) != null)
            storeGUI.updateCartQuantity(category, index,
                    cart.getStoreQuantity(category, index));
    }

    public void refreshCart() {
        model.setRowCount(0);
        for (Cart.CartItem item : cart.getItems()) {
            model.addRow(new Object[] {item.getName(),
                String.format("%.2f SAR", item.getPrice()), item.getQuantity(),
                item.getAvailableStock(), String.format("%.2f SAR", item.getSubtotal())});
        }
        int totalItems = 0;
        for (Cart.CartItem item : cart.getItems()) totalItems += item.getQuantity();
        countLabel.setText("Items: " + totalItems);
        totalLabel.setText(String.format("Total: %.2f SAR", cart.getTotalPrice()));
    }

    // Standalone visual test only.
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Inventory inventory = new Inventory();
            Cart cart = new Cart();
            Product pearl = inventory.getProductById(101);
            Product hearts = inventory.getProductById(202);
            cart.addStoreProduct(0, 0, pearl.getName().trim(), pearl.getPrice(), 1, pearl.getStock());
            cart.addStoreProduct(1, 1, hearts.getName().trim(), hearts.getPrice(), 2, hearts.getStock());
            new CartFrame(cart, null, inventory).setVisible(true);
        });
    }
}
