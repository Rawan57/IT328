import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/** Product details, quantity selection, and Add button. */
public class ProductCard extends JPanel {
    private final Color rosePink = new Color(0xC23B68);
    private final Color pink = new Color(230, 50, 130);
    private final Color muted = new Color(145, 125, 135);
    private final Color dark = new Color(75, 55, 65);

    // Send selection and cart requests to the store window.
    private final StoreGUI storeGUI;
    private final int category;
    private final int index;
    private final int stock;
    private JSpinner quantitySpinner;
    private JButton addButton;

    public ProductCard(StoreGUI storeGUI, int category, int index,
            String productName, String imagePath, double price,
            int stock, int quantityInCart) {
        this.storeGUI = storeGUI;
        this.category = category;
        this.index = index;
        this.stock = Math.max(0, stock);
        // Exclude quantities already added to the cart.
        int available = Math.max(0, this.stock - Math.max(0, quantityInCart));

        setLayout(new BorderLayout(0, 9));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(247, 220, 232)),
                BorderFactory.createEmptyBorder(12, 14, 14, 14)));

        // Show the product image from project resources.
        JLabel imageLabel = new JLabel("", SwingConstants.CENTER);
        imageLabel.setPreferredSize(new Dimension(0, 145));
        ImageIcon image = storeGUI.loadImage(imagePath, 145, 140);
        if (image != null) imageLabel.setIcon(image);
        else {
            imageLabel.setText("Image not found");
            imageLabel.setForeground(muted);
        }
        imageLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        imageLabel.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { selectForPreview(); }
        });

        JLabel nameLabel = new JLabel(productName, SwingConstants.CENTER);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 16));
        nameLabel.setForeground(pink);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        nameLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        nameLabel.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { selectForPreview(); }
        });

        JLabel priceLabel = new JLabel(String.format("%.2f SAR", price));
        priceLabel.setFont(new Font("Arial", Font.BOLD, 15));
        priceLabel.setForeground(dark);
        JLabel stockLabel = new JLabel(this.stock == 0 ? "Out of Stock" : "Stock: " + this.stock);
        stockLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        stockLabel.setForeground(this.stock == 0 ? Color.RED : muted);

        // The spinner never allows a quantity greater than remaining stock.
        quantitySpinner = new JSpinner(new SpinnerNumberModel(
                available > 0 ? 1 : 0,
                available > 0 ? 1 : 0,
                Math.max(1, available), 1));
        quantitySpinner.setPreferredSize(new Dimension(57, 29));
        JLabel qtyLabel = new JLabel("Qty:");
        qtyLabel.setForeground(dark);
        qtyLabel.setFont(new Font("Arial", Font.BOLD, 12));
        JPanel quantityPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 7));
        quantityPanel.setOpaque(false);
        quantityPanel.add(qtyLabel);
        quantityPanel.add(quantitySpinner);

        JPanel priceInfo = new JPanel();
        priceInfo.setOpaque(false);
        priceInfo.setLayout(new BoxLayout(priceInfo, BoxLayout.Y_AXIS));
        priceInfo.add(priceLabel);
        priceInfo.add(Box.createVerticalStrut(5));
        priceInfo.add(stockLabel);
        JPanel details = new JPanel(new BorderLayout());
        details.setBackground(new Color(255, 247, 250));
        details.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        details.add(priceInfo, BorderLayout.WEST);
        details.add(quantityPanel, BorderLayout.EAST);

        // Send the selected quantity to CartFrame.
        addButton = new JButton("Add to Cart");
        addButton.setBackground(rosePink); // Deep Rose Pink: #C23B68
        addButton.setForeground(Color.WHITE);
        addButton.setFont(new Font("Arial", Font.BOLD, 14));
        addButton.setOpaque(true);
        addButton.setContentAreaFilled(true);
        addButton.setFocusPainted(false);
        addButton.setBorder(BorderFactory.createEmptyBorder(9, 13, 9, 13));
        addButton.setPreferredSize(new Dimension(0, 36));
        addButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        updateAvailability(available);
        addButton.addActionListener(e -> {
            // Commit any number typed manually into the spinner before reading it.
            try {
                quantitySpinner.commitEdit();
            } catch (java.text.ParseException ex) {
                JOptionPane.showMessageDialog(storeGUI, "Please enter a valid quantity.");
                return;
            }
            int quantity = ((Number) quantitySpinner.getValue()).intValue();
            if (quantity < 1 || quantity > available) {
                JOptionPane.showMessageDialog(storeGUI, "Not enough stock!");
                return;
            }
            storeGUI.requestAddToCart(category, index, quantity);
        });

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.add(nameLabel);
        center.add(Box.createVerticalStrut(12));
        center.add(details);
        add(imageLabel, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);
        add(addButton, BorderLayout.SOUTH);
    }

    // Update the preview when a product is selected.
    private void selectForPreview() {
        storeGUI.selectProduct(category, index);
    }

    // Disable quantity and Add when no stock remains.
    private void updateAvailability(int available) {
        boolean inStock = available > 0;
        quantitySpinner.setEnabled(inStock);
        addButton.setEnabled(inStock);
        if (inStock) {
            addButton.setText("Add to Cart");
            addButton.setBackground(rosePink);
            addButton.setForeground(Color.WHITE);
        } else {
            addButton.setText(stock == 0 ? "Sold Out" : "Limit Reached");
            addButton.setBackground(new Color(220, 220, 220));
            addButton.setForeground(new Color(110, 110, 110));
        }
    }
}
