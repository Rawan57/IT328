/**
 Manages the products and stock quantities in the Charmify store.
 * stored 12 products: 4 phone cases, 4 charms, and 4 stickers.
 * Provides methods to search for products, check stock availability,
 * reduce stock after purchases, and restore stock when needed.
 */
import java.util.ArrayList;  // to use ArrayList
import java.lang.Math;
public class Inventory {

    private ArrayList<Product> products; 


    public Inventory(){
        products = new ArrayList<>(); 

        // Phone Cases (Quantity: 4): 
         products.add(new Product(101, "Pearl Case", Category.PHONE_CASE, 45.0, 10, "/images/pearl_case.png")); 
         products.add(new Product(102, "Pink Glitter Case", Category.PHONE_CASE, 45.0, 10, "/images/pink_glitter_case.png")); 
         products.add(new Product(103, "Hearts Case", Category.PHONE_CASE, 45.0, 10, "/images/hearts_case.png")); 
         products.add(new Product(104, "Blue Wave Case", Category.PHONE_CASE, 45.0, 10, "/images/blue_wave_case.png")); 
        




        // Charms (Quantity: 4): 
        products.add (new Product(201, "White Bow Charm", Category.CHARM, 8.0, 15, "/images/white_bow_charm.png"));
        products.add (new Product(202, " Pink Hearts Charm", Category.CHARM, 8.0, 15, "/images/pink_hearts_charm.png"));
        products.add (new Product(203, "Pink Stars Charm", Category.CHARM, 8.0, 15, "/images/pink_stars_charm.png"));
        products.add (new Product(204, "Seashell Fan Charm", Category.CHARM, 8.0, 15, "/images/seashell_fan_charm.png"));






        //Stickers ((Quantity: 4)): 
        products.add(new Product(301, "Butterfly Sticker", Category.STICKER, 10.0, 20, "/images/butterfly_sticker.png"));
        products.add(new Product(302, "Star Sticker", Category.STICKER, 10.0, 20, "/images/star_sticker.png"));
        products.add(new Product(303, "Blue Shell Sticker", Category.STICKER, 10.0, 20, "/images/blue_shell_sticker.png"));
        products.add(new Product(304, "Gold Heart Sticker", Category.STICKER, 10.0, 20, "/images/gold_heart_sticker.png"));

       
    } // end constructor Inventory()
    
    // this method search for a product by its id.

    public Product getProductById(int id){
        for (Product product : products ){
            if (product.getID() == id){
                return product; } 
        } // end for loop.

       return null; // no product is found

    } // end method getProductById
    
    public synchronized int getStock(int id){
        Product p = getProductById(id);

        if(p != null){
            return p.getStock();
        }

        return -1; // product not found
     }


    // this method returns all products.
    public ArrayList<Product> getAllProducts(){
        return new ArrayList<> (products);} 

  

    
    // this method return the numbers of quantites in stock 
    public synchronized int getAvailableQuantity(int id, int requestedQuantity){
       Product p = getProductById(id);

       if (p == null || requestedQuantity <= 0){
        return 0;
       }
         return Math.min(requestedQuantity, p.getStock()); // get all available stock

    } 
    
    // this method reduce product in stock
    public synchronized boolean reduceStock(int id, int quantity) {
        Product p = getProductById(id);

        if (p != null && quantity > 0 && p.getStock()>= quantity ){
             p.setStock(p.getStock() - quantity);
             return true; 
        }
         
        return false; 
    }
    
     // this method restore product stock
    public synchronized boolean restoreStock(int id, int quantity){
         Product p = getProductById(id);

         if (p !=null && quantity > 0 ){
             p.setStock(p.getStock() + quantity);  
            return true; 
         }

           return false; 
    }
    







} // end class Inventory
