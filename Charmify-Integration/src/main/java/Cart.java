import java.util.ArrayList;
import java.util.List;

/**
 * Charmify shopping cart data and quantity management.
 * This class does not create any GUI.
 */
public class Cart {

    private final ArrayList<CartItem> items = new ArrayList<>();

    // Used by the Store integration: one ID for each category/index pair.
    private String makeStoreId(int category, int index) {
        return "STORE-" + category + "-" + index;
    }

    // Add an item from the Store using its category and product index.
    public void addStoreProduct(int category, int index, String name,
                                double price, int quantity, int availableStock) {
        if (category < 0 || index < 0 || quantity <= 0 || availableStock < 0) {
            return;
        }

        String id = makeStoreId(category, index);
        CartItem existing = findItem(id);

        if (existing != null) {
            existing.setName(name);
            existing.setPrice(price);
            existing.setAvailableStock(availableStock);

            int newQuantity = existing.getQuantity() + quantity;
            existing.setQuantity(Math.min(newQuantity, availableStock));
        } else if (availableStock > 0) {
            int safeQuantity = Math.min(quantity, availableStock);
            items.add(new CartItem(id, name, price, safeQuantity,
                                   availableStock, category, index));
        }
    }

    // Keep this method for simple tests or other code that uses a custom ID.
    public void addProduct(String id, String name, double price,
                           int quantity, int availableStock) {
        if (id == null || name == null || quantity <= 0 || availableStock < 0) {
            return;
        }

        CartItem existing = findItem(id);
        if (existing != null) {
            existing.setName(name);
            existing.setPrice(price);
            existing.setAvailableStock(availableStock);
            existing.setQuantity(
                Math.min(existing.getQuantity() + quantity, availableStock)
            );
        } else if (availableStock > 0) {
            items.add(new CartItem(id, name, price,
                Math.min(quantity, availableStock), availableStock, -1, -1));
        }
    }

    private CartItem findItem(String id) {
        for (CartItem item : items) {
            if (item.getId().equals(id)) {
                return item;
            }
        }
        return null;
    }

    public List<CartItem> getItems() {
        return new ArrayList<>(items);
    }

    public CartItem getStoreItem(int category, int index) {
        return findItem(makeStoreId(category, index));
    }

    public int getStoreQuantity(int category, int index) {
        CartItem item = getStoreItem(category, index);
        return item == null ? 0 : item.getQuantity();
    }

    public void changeQuantity(String id, int change) {
        CartItem item = findItem(id);
        if (item != null) {
            int newQuantity = item.getQuantity() + change;
            if (newQuantity >= 0 && newQuantity <= item.getAvailableStock()) {
                item.setQuantity(newQuantity);
            }
        }
    }

    public void removeProduct(String id) {
        CartItem item = findItem(id);
        if (item != null) {
            items.remove(item);
        }
    }

    // Called when a newer stock quantity arrives from the server.
    public void updateStock(String id, int newStock) {
        if (newStock < 0) {
            return;
        }

        CartItem item = findItem(id);
        if (item != null) {
            item.setAvailableStock(newStock);
            if (item.getQuantity() > newStock) {
                item.setQuantity(newStock);
            }
        }
    }

    public void updateStoreStock(int category, int index, int newStock) {
        if (newStock < 0) {
            return;
        }
        updateStock(makeStoreId(category, index), newStock);
    }

    public double getTotalPrice() {
        double total = 0;
        for (CartItem item : items) {
            total += item.getSubtotal();
        }
        return total;
    }

    public void clear() {
        items.clear();
    }

    public static class CartItem {
        private final String id;
        private final int category;
        private final int index;
        private String name;
        private double price;
        private int quantity;
        private int availableStock;

        public CartItem(String id, String name, double price,
                        int quantity, int availableStock,
                        int category, int index) {
            this.id = id;
            this.name = name;
            this.price = price;
            this.quantity = quantity;
            this.availableStock = availableStock;
            this.category = category;
            this.index = index;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public double getPrice() { return price; }
        public int getQuantity() { return quantity; }
        public int getAvailableStock() { return availableStock; }
        public int getCategory() { return category; }
        public int getIndex() { return index; }
        public double getSubtotal() { return price * quantity; }

        public void setName(String name) { this.name = name; }
        public void setPrice(double price) { this.price = price; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        public void setAvailableStock(int availableStock) {
            this.availableStock = availableStock;
        }
    }
}
