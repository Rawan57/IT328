/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package server;

/**
 *
 * @author reem
 */
import java.time.LocalDateTime; 
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Order {
    
   public enum Status {PLACED, CANCELLED}

   /// Display format, and a precise formar used to identify order:
      private static final DateTimeFormatter TIME_FORMAT= 
              DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
      private static final DateTimeFormatter ID_FORMAT= 
               DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
       private static LocalDateTime lastTimestamp = null;
   
   
                                     
       private final String username;       //Order's atributes: 
       private final List<OrderItem> items;
       private final LocalDateTime timestamp;
       private LocalDateTime cancelldAt; 
       private Status status;
      
      
         

    
        public Order(String username, List<OrderItem> items){
            this.username = username;
            this.items= new ArrayList<OrderItem>(items);
            this.timestamp = nextUniqueTimestamp();
            this.status= Status.PLACED; 
              
        }
        
        
        public String getUserName(){return username;}       //Getters:
        public List<OrderItem> getItems(){return Collections.unmodifiableList(items);}
        public LocalDateTime getTimestamp(){return timestamp;}
        public LocalDateTime getCancelldAt(){return cancelldAt;}
        public synchronized Status getStatus(){return status;}
        public String getFormattedTimestamp(){return timestamp.format(TIME_FORMAT);}
        
        public String getTimestampId(){return timestamp.format(ID_FORMAT); }
        
        //this method make sure no two orders get the same timestamp, even if placed in same millisecond
        private static synchronized LocalDateTime nextUniqueTimestamp(){
            LocalDateTime now= LocalDateTime.now();
            now = now.withNano((now.getNano()/1_000_000) *1_000_000); // milliseconds only
            
            if (lastTimestamp != null && !now.isAfter(lastTimestamp)){
                now = lastTimestamp.plusNanos(1_000_000); //+1 millisecond
                
            } 
          lastTimestamp = now;
          return now;
            
        }
        
        public double getTotalPrice(){
            double total= 0;
            for (OrderItem item : items){
                total += item.getSubtotal();
            
            } return total;
            
        }
        
        public synchronized boolean markCncelled(){
            if (status == Status.CANCELLED){
                return false;
            }
            status = Status.CANCELLED;
             cancelldAt= LocalDateTime.now();
            return true; 
        }
        
        public boolean applyToInventory(Inventory inventory){
            synchronized (inventory){
                for(OrderItem item: items){
                    if (inventory.getStock(item.getProductId()) < item.getQuantity()){
                        return false; }
                }
                
                for(OrderItem item: items){
                    inventory.reduceStock(item.getProductId(),item.getQuantity()); 
            }
                return true; }
        }
        
        
        public boolean cancel(Inventory inventory){
            synchronized (inventory) {
                if (!markCncelled()){
                    return false;
                }
                
                for (OrderItem item : items){
                    inventory.restoreStock(item.getProductId(), item.getQuantity());
                }
                return true; 
                    
            }
        }
        
        
        @Override
        public String toString(){
            
            StringBuilder sb = new StringBuilder();
            sb.append("Order ").append(getTimestampId())
            .append(" | ").append(username)
            .append(" | ").append(status)
            .append(String.format(java.util.Locale.US, " | Total: %.2f SAR", getTotalPrice()));
            for ( OrderItem item: items){
                sb.append("\n   -").append(item);
            }
                
            
            return sb.toString();
        }
                
                
        
        
        
        
        
        
     
        
public static class OrderItem{
    
    
      private final int productId;               //Attribute:
      private final String productName;
      private final double unitPrice;
      private final int quantity;
      
      
      public OrderItem(int productId, String productName, double unitPrice, int quantity){
          this.productId= productId;
          this.productName = productName;
          this.unitPrice = unitPrice ; 
          this.quantity = quantity;
          
      }
      
      public int getProductId(){return productId;}                             //Getters:
      
      public String getProductname(){return productName;}
      
      public double getUnitPrice(){return unitPrice;}
      
      public int getQuantity(){return quantity;}
     
      public double getSubtotal(){return unitPrice* quantity;}
      
      @Override
      public String toString(){
          return String.format(java.util.Locale.US, "%s x%d (%.2f SAR)",
             productName.trim(), quantity, getSubtotal()); 
      }
     
      
     
} // end class OrderItem

        
}// end class Order
