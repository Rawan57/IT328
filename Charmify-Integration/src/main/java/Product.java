/**
 * Represents a product available in Charmify store.
 * 
 * Stores product information such as ID, name, category, price,
 * stock quantity, and image path.
 */
 
public class Product {
    // product attributes: 
   private int id; 
   private String name; 
  private Category category; // fixed values ( PHONE_CASE, CHARM, STICKER)
   private double price; 
   private int stock; 
   private String imagePath; 


    public Product(int id, String name, Category category, double price, int stock, String imagePath){ // product constructor: 
     this.id = id; 
     this.name= name; 
     this.category = category; 
     this.price = price; 
     this.stock = stock; 
     this.imagePath = imagePath; 
     
    } 

   //Getters: 
    public int getID(){return id;}

    public String getName(){return name;}

    public Category getCategory(){return category;}

    public double getPrice(){return price;}

    public int getStock(){return stock;}

    public String getImagePath(){return imagePath;}
    
    //Setters: 
    public void setStock(int stock) {
    if (stock >= 0) {
        this.stock = stock;
    }
}



} // end class Prodcut
