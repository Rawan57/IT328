/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package server;

/**
 *
 * @author reem
 */
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;       
import java.util.List;
public class OrderStorage {
    
    private static final String ORDERS_FILE= "orders.txt";
    
    private final List<Order> orders = new ArrayList<>(); 
    
    public OrderStorage(){
        
    }
    
    public synchronized void addOrder(Order order){
        if (order == null){
            return;}
        orders.add(order);
        saveToFile(order, "PLACED");
        
    }
    
    
    public synchronized void recordCancell(Order order){
        if (order !=null){
            saveToFile(order, "CANCELLED");
        }
        
        }
        
    
    
    public synchronized Order findOrder(String username, String timestampId){
        
        for (Order order : orders){
            if (order.getUsername().equals(username) && order.getTimestampId().equals(timestampId) ){
            return order;
            }
        }
       
    return null; 
     
    }
    
    
    
    public synchronized List<Order> getOrderByUser(String username){ 
        List <Order> result = new ArrayList<>();
        for (Order order : orders ){
            if (order.getUsername().equals(username)){
                result.add(order);
            }
        }
        
        return result; 
    }
    
    
    public synchronized List<Order> getAllOrders(){
        
        return new ArrayList<>(orders) ;
            }
    
    
    private void saveToFile(Order order, String action){
        
        try (PrintWriter out = new PrintWriter(new FileWriter(ORDERS_FILE, true))){
            out.println("[" + action +" ]" + order);
        }catch(IOException e){
            System.out.println("Cancel not save order: "+ e.getMessage());
        }
    
    }
    
    
    
} // end clsss OrderStorage
