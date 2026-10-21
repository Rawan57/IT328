import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/** Storefront and product selection interface. */
public class StoreGUI extends JFrame {
    private final Color pink = new Color(230, 50, 130);
    private final Color lightPink = new Color(255, 235, 245);
    private final Color background = new Color(255, 250, 253);
    private final Color muted = new Color(145, 125, 135);

    // Load products from Inventory.
    private final Inventory inventory;
    private final Cart cart;
    private final DynamicPhoneCasePreviewPanel previewPanel;
    private final List<Product>[] productsByCategory;
    // Track cart quantities and latest stock for each product.
    private final int[][] cartQuantities = new int[3][4];
    private final int[][] displayedStock = new int[3][4];
    private final String[] categoryNames = {"Phone Cases", "Charms", "Stickers"};

    private int currentCategory = 0;
    private JPanel productsGrid;
    private JLabel categoryTitle;
    private JTextField searchField;
    private JLabel cartBadge;
    private JLabel previewMessage;
    private CartFrame cartFrame;
    private JScrollPane productsScroll;

    /** Optional integration callback for a network-backed cart. */
    public interface CartHandler {
        void onAddToCart(int category, int index, int quantity);
    }

    /** Optional callback if the preview selection must be shared elsewhere. */
    public interface PreviewHandler {
        void onProductSelected(int category, int index);
    }

    private CartHandler cartHandler;
    private PreviewHandler previewHandler;

    @SuppressWarnings("unchecked")
    public StoreGUI(Inventory inventory, Cart cart) {
        this.inventory = inventory;
        this.cart = cart;
        this.previewPanel = new DynamicPhoneCasePreviewPanel();
        productsByCategory = (List<Product>[]) new List[3];
        for (int i = 0; i < 3; i++) productsByCategory[i] = new ArrayList<>();

        // Match the category order used by CartFrame.
        for (Product p : inventory.getAllProducts()) {
            int category = categoryIndex(p.getCategory());
            if (category >= 0) productsByCategory[category].add(p);
        }
        for (int c = 0; c < 3; c++) {
            if (productsByCategory[c].size() != 4)
                throw new IllegalStateException("Expected 4 products in " + categoryNames[c]);
            for (int i = 0; i < 4; i++) displayedStock[c][i] = productsByCategory[c].get(i).getStock();
        }

        setTitle("Charmify");
        setSize(1400, 850);
        setMinimumSize(new Dimension(1050, 680));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(background);
        setContentPane(root);
        createHeader();
        createSidebar();
        createProductsArea();
        createPreviewArea();
        showProducts(0);
    }

    public StoreGUI() { this(new Inventory(), new Cart()); }

    private int categoryIndex(Category category) {
        if (category == Category.PHONE_CASE) return 0;
        if (category == Category.CHARM) return 1;
        if (category == Category.STICKER) return 2;
        return -1;
    }

    /** Load existing image resources without modifying the original artwork. */
    public ImageIcon loadImage(String path, int maxWidth, int maxHeight) {
        if (path == null) return null;
        String resourcePath = path.startsWith("/") ? path : "/images/" + path;
        URL url = getClass().getResource(resourcePath);
        if (url == null) {
            System.out.println("Image not found: " + resourcePath);
            return null;
        }
        ImageIcon original = new ImageIcon(url);
        int w = original.getIconWidth(), h = original.getIconHeight();
        if (w <= 0 || h <= 0) return null;
        double scale = Math.min((double) maxWidth / w, (double) maxHeight / h);
        return new ImageIcon(original.getImage().getScaledInstance(
                Math.max(1, (int) Math.round(w * scale)),
                Math.max(1, (int) Math.round(h * scale)), Image.SCALE_SMOOTH));
    }

    // Build the logo, search bar, and cart icon.
    private void createHeader() {
        JPanel header = new JPanel(new BorderLayout(24, 0));
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createEmptyBorder(12, 10, 12, 22));
        header.setPreferredSize(new Dimension(0, 105));

        JLabel logo = new JLabel();
        logo.setPreferredSize(new Dimension(252, 86));
        logo.setHorizontalAlignment(SwingConstants.LEFT);
        ImageIcon logoIcon = loadImage("logo.png", 248, 85);
        if (logoIcon != null) logo.setIcon(logoIcon);
        else {
            logo.setText("Charmify");
            logo.setFont(new Font("Serif", Font.BOLD | Font.ITALIC, 30));
            logo.setForeground(pink);
        }

        JPanel searchPanel = new JPanel(new BorderLayout(8, 0));
        searchPanel.setOpaque(false);
        searchPanel.setBorder(BorderFactory.createEmptyBorder(18, 0, 18, 0));
        JLabel magnifier = new JLabel("⌕", SwingConstants.CENTER);
        magnifier.setFont(new Font("Dialog", Font.BOLD, 29));
        magnifier.setForeground(pink);
        magnifier.setPreferredSize(new Dimension(30, 30));
        // Show a category-specific hint without affecting search text.
        searchField = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setColor(new Color(170, 155, 164));
                    g2.setFont(getFont());
                    String hint = "Search for " + new String[]{
                        "phone cases...", "charms...", "stickers..."
                    }[currentCategory];
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(hint, getInsets().left,
                            (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
                    g2.dispose();
                }
            }
        };
        searchField.setFont(new Font("Arial", Font.PLAIN, 15));
        searchField.setToolTipText("Search for products...");
        searchField.setBorder(BorderFactory.createEmptyBorder(9, 12, 9, 5));
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { refreshProducts(); }
            public void removeUpdate(DocumentEvent e) { refreshProducts(); }
            public void changedUpdate(DocumentEvent e) { refreshProducts(); }
        });
        // Clear the search with the X button.
        JButton clearSearch = new JButton("×");
        clearSearch.setFont(new Font("Arial", Font.BOLD, 18));
        clearSearch.setForeground(pink);
        clearSearch.setBackground(Color.WHITE);
        clearSearch.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 12));
        clearSearch.setContentAreaFilled(false);
        clearSearch.setFocusPainted(false);
        clearSearch.setToolTipText("Clear search");
        clearSearch.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        clearSearch.addActionListener(e -> searchField.setText(""));
        searchPanel.add(magnifier, BorderLayout.WEST);
        JPanel searchBox = new JPanel(new BorderLayout());
        searchBox.setBackground(Color.WHITE);
        searchBox.setBorder(BorderFactory.createLineBorder(new Color(245, 210, 225)));
        searchBox.add(searchField, BorderLayout.CENTER);
        searchBox.add(clearSearch, BorderLayout.EAST);
        searchPanel.add(searchBox, BorderLayout.CENTER);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 18, 12));
        right.setOpaque(false);
        JLabel welcome = new JLabel("Welcome!");
        welcome.setFont(new Font("Arial", Font.BOLD, 16));
        welcome.setForeground(pink);
        JButton cartButton = new JButton();
        cartButton.setPreferredSize(new Dimension(75, 65));
        cartButton.setLayout(null);
        cartButton.setBorderPainted(false);
        cartButton.setContentAreaFilled(false);
        cartButton.setFocusPainted(false);
        cartButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        cartButton.setToolTipText("Open shopping cart");
        ImageIcon cartIcon = loadImage("cart.png", 55, 47);
        if (cartIcon != null) cartButton.setIcon(cartIcon);
        else cartButton.setText("Cart");
        cartBadge = new JLabel("0", SwingConstants.CENTER);
        cartBadge.setBounds(47, 0, 27, 27);
        cartBadge.setOpaque(true);
        cartBadge.setBackground(pink);
        cartBadge.setForeground(Color.WHITE);
        cartBadge.setFont(new Font("Arial", Font.BOLD, 12));
        cartBadge.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
        cartButton.add(cartBadge);
        cartButton.addActionListener(e -> openCart());
        right.add(welcome);
        right.add(cartButton);
        header.add(logo, BorderLayout.WEST);
        header.add(searchPanel, BorderLayout.CENTER);
        header.add(right, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);
    }

    // Build the three category buttons.
    private void createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(lightPink);
        sidebar.setPreferredSize(new Dimension(205, 0));
        sidebar.setBorder(BorderFactory.createEmptyBorder(30, 13, 25, 13));
        JLabel title = new JLabel("Categories");
        title.setFont(new Font("Arial", Font.BOLD, 21));
        title.setForeground(pink);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(title);
        sidebar.add(Box.createVerticalStrut(30));
        for (int i = 0; i < categoryNames.length; i++) {
            final int category = i;
            JButton button = new JButton(categoryNames[i]);
            button.setBackground(lightPink);
            button.setForeground(pink);
            button.setFont(new Font("Arial", Font.BOLD, 14));
            button.setFocusPainted(false);
            button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(245, 185, 211)),
                    BorderFactory.createEmptyBorder(8, 13, 8, 13)));
            button.setMaximumSize(new Dimension(178, 48));
            button.setAlignmentX(Component.CENTER_ALIGNMENT);
            button.addActionListener(e -> showProducts(category));
            sidebar.add(button);
            sidebar.add(Box.createVerticalStrut(15));
        }
        sidebar.add(Box.createVerticalGlue());
        JLabel slogan = new JLabel("<html><div style='text-align:center;'>Little details,<br>big charm! &#9829;</div></html>", SwingConstants.CENTER);
        slogan.setFont(new Font("Serif", Font.BOLD | Font.ITALIC, 19));
        slogan.setForeground(pink);
        slogan.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel sub = new JLabel("<html><div style='text-align:center;'>Make your case.<br>Make it yours.</div></html>", SwingConstants.CENTER);
        sub.setFont(new Font("Arial", Font.PLAIN, 12));
        sub.setForeground(muted);
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(slogan);
        sidebar.add(Box.createVerticalStrut(12));
        sidebar.add(sub);
        add(sidebar, BorderLayout.WEST);
    }

    // Create the scrollable product grid.
    private void createProductsArea() {
        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(background);
        categoryTitle = new JLabel("Phone Cases");
        categoryTitle.setFont(new Font("Arial", Font.BOLD, 25));
        categoryTitle.setForeground(pink);
        categoryTitle.setBorder(BorderFactory.createEmptyBorder(22, 22, 12, 22));
        productsGrid = new JPanel(new GridLayout(0, 2, 16, 16));
        productsGrid.setBackground(background);
        productsGrid.setBorder(BorderFactory.createEmptyBorder(10, 15, 20, 15));
        productsScroll = new JScrollPane(productsGrid);
        productsScroll.setBorder(null);
        productsScroll.getVerticalScrollBar().setUnitIncrement(18);
        productsScroll.getViewport().setBackground(background);
        content.add(categoryTitle, BorderLayout.NORTH);
        content.add(productsScroll, BorderLayout.CENTER);
        add(content, BorderLayout.CENTER);
    }

    // Show a framed design preview with removal controls.
    private void createPreviewArea() {
        JPanel side = new JPanel(new BorderLayout(0, 12));
        side.setBackground(Color.WHITE);
        side.setPreferredSize(new Dimension(335, 0));
        side.setBorder(BorderFactory.createEmptyBorder(18, 13, 18, 13));

        JPanel heading = new JPanel();
        heading.setOpaque(false);
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Your Design");
        title.setFont(new Font("Arial", Font.BOLD, 25));
        title.setForeground(pink);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel subtitle = new JLabel("Live Preview - Customize your case");
        subtitle.setFont(new Font("Arial", Font.PLAIN, 12));
        subtitle.setForeground(muted);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        heading.add(title);
        heading.add(Box.createVerticalStrut(8));
        heading.add(subtitle);

        // Soft, uncluttered preview area.
        JPanel previewHolder = new JPanel(new BorderLayout());
        previewHolder.setBackground(new Color(255, 246, 250));
        previewHolder.setBorder(BorderFactory.createLineBorder(new Color(244, 218, 230)));
        previewPanel.setBackground(new Color(255, 246, 250));
        previewHolder.add(previewPanel, BorderLayout.CENTER);

        previewMessage = new JLabel("Choose a phone case", SwingConstants.CENTER);
        previewMessage.setForeground(pink);
        previewMessage.setFont(new Font("Arial", Font.BOLD, 13));

        // Keep all remove icons aligned in one column.
        JPanel controls = new JPanel(new GridLayout(3, 1, 0, 3));
        controls.setOpaque(false);
        controls.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        controls.add(previewActionRow("Remove Sticker", () -> previewPanel.removeSelectedSticker()));
        controls.add(previewActionRow("Remove Charm", () -> previewPanel.removeCharm()));
        controls.add(previewActionRow("Clear Design", () -> {
            previewPanel.clearDesign();
            previewMessage.setText("Choose a phone case");
        }));

        JPanel footer = new JPanel(new BorderLayout(0, 7));
        footer.setOpaque(false);
        footer.add(previewMessage, BorderLayout.NORTH);
        footer.add(controls, BorderLayout.CENTER);
        side.add(heading, BorderLayout.NORTH);
        side.add(previewHolder, BorderLayout.CENTER);
        side.add(footer, BorderLayout.SOUTH);
        add(side, BorderLayout.EAST);
    }

    private JPanel previewActionRow(String text, Runnable action) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        JButton label = new JButton(text);
        label.setHorizontalAlignment(SwingConstants.LEFT);
        JButton remove = new JButton("×");
        for (JButton button : new JButton[]{label, remove}) {
            button.setFont(new Font("Arial", Font.PLAIN, 12));
            button.setForeground(new Color(145, 90, 112));
            button.setBorder(BorderFactory.createEmptyBorder(5, 3, 5, 3));
            button.setContentAreaFilled(false);
            button.setFocusPainted(false);
            button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            button.addActionListener(e -> action.run());
        }
        remove.setPreferredSize(new Dimension(24, 27));
        row.add(label, BorderLayout.CENTER);
        row.add(remove, BorderLayout.EAST);
        return row;
    }

    // Switch the visible product category.
    private void showProducts(int category) {
        currentCategory = category;
        categoryTitle.setText(categoryNames[category]);
        searchField.repaint(); // Update the search hint for this category.
        refreshProducts();
        productsScroll.getVerticalScrollBar().setValue(0);
    }

    // Rebuild cards after search, cart, or stock changes.
    private void refreshProducts() {
        if (productsGrid == null || searchField == null) return;
        productsGrid.removeAll();
        String query = searchField.getText().trim().toLowerCase();
        int found = 0;
        for (int i = 0; i < productsByCategory[currentCategory].size(); i++) {
            Product p = productsByCategory[currentCategory].get(i);
            if (!p.getName().trim().toLowerCase().contains(query)) continue;
            productsGrid.add(new ProductCard(this, currentCategory, i,
                    p.getName().trim(), p.getImagePath(), p.getPrice(),
                    displayedStock[currentCategory][i], cartQuantities[currentCategory][i]));
            found++;
        }
        if (found == 0) {
            JLabel empty = new JLabel("No products found", SwingConstants.CENTER);
            empty.setForeground(muted);
            productsGrid.add(empty);
        }
        int rows = Math.max(2, (found + 1) / 2);
        productsGrid.setPreferredSize(new Dimension(0, rows * 345));
        productsGrid.revalidate();
        productsGrid.repaint();
    }

    /** Update DynamicPhoneCasePreviewPanel on selection. */
    public void selectProduct(int category, int index) {
        if (!valid(category, index)) return;
        Product p = productsByCategory[category].get(index);
        if (category == 0) {
            previewPanel.setSelectedCase(p.getImagePath());
            previewMessage.setText(p.getName().trim());
        } else if (category == 1) {
            previewPanel.setSelectedCharm(p.getImagePath());
        } else {
            previewPanel.setSelectedSticker(p.getImagePath());
        }
        if (previewHandler != null) previewHandler.onProductSelected(category, index);
    }

    /** Send the selected quantity to CartFrame. */
    public void requestAddToCart(int category, int index, int quantity) {
        if (!valid(category, index) || quantity < 1) return;
        int remaining = displayedStock[category][index] - cartQuantities[category][index];
        if (quantity > remaining) {
            JOptionPane.showMessageDialog(this, "Not enough stock!");
            refreshProducts();
            return;
        }
        if (cartHandler != null) {
            cartHandler.onAddToCart(category, index, quantity);
        } else {
            ensureCartFrame().addFromStore(category, index, quantity);
        }
    }

    // Reuse the cart window instead of creating duplicates.
    private CartFrame ensureCartFrame() {
        if (cartFrame == null || !cartFrame.isDisplayable()) {
            cartFrame = new CartFrame(cart, this, inventory);
        }
        return cartFrame;
    }

    // Open CartFrame.
    private void openCart() {
        CartFrame frame = ensureCartFrame();
        frame.refreshCart();
        frame.setVisible(true);
        frame.toFront();
    }

    /** Called when the server supplies an updated stock quantity. */
    public void updateProductStock(int category, int index, int newStock) {
        if (!valid(category, index) || newStock < 0) return;
        SwingUtilities.invokeLater(() -> {
            displayedStock[category][index] = newStock;
            productsByCategory[category].get(index).setStock(newStock);
            refreshProducts();
        });
    }

    /** Refresh the store after Cart updates its quantity. */
    public void updateCartQuantity(int category, int index, int quantityInCart) {
        if (!valid(category, index)) return;
        SwingUtilities.invokeLater(() -> {
            cartQuantities[category][index] = Math.max(0, quantityInCart);
            int total = 0;
            for (int[] row : cartQuantities) for (int count : row) total += count;
            cartBadge.setText(String.valueOf(total));
            refreshProducts();
        });
    }

    private boolean valid(int category, int index) {
        return category >= 0 && category < 3 && index >= 0
                && index < productsByCategory[category].size();
    }

    public void setCartHandler(CartHandler handler) { this.cartHandler = handler; }
    public void setPreviewHandler(PreviewHandler handler) { this.previewHandler = handler; }
    public DynamicPhoneCasePreviewPanel getPreviewPanel() { return previewPanel; }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new StoreGUI().setVisible(true));
    }
}
