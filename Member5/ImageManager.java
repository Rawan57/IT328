/**
 * 
 * Handles loading and scaling product images for the Charmify store.
 * Provides reusable methods to display images while maintaining
 * their original aspect ratios.
 */

import javax.swing.ImageIcon;
import java.awt.Image;
import java.net.URL;

public class ImageManager {

    // this method load images:
    public static ImageIcon loadImage(String imagePath){

       URL imageURL =ImageManager.class.getResource(imagePath);

       if (imageURL == null){
        System.out.println("image not found: " + imagePath);

        return null; 

            } // end if 

        return new ImageIcon(imageURL);
       
    } // end loadImage() method


    public static ImageIcon loadScaledImage(String imagePath, int maxWidth, int maxHeight){
        
        ImageIcon original = loadImage(imagePath);

        if (original == null || maxWidth <= 0 || maxHeight <= 0){ // image not found
            return null; 

        } 
        int originalWidth = (int) original.getIconWidth();
        int originalHeight = (int) original.getIconHeight();

         if (originalWidth <= 0 || originalHeight <= 0) {
            return null;
           }
        double scale = Math.min((double) maxWidth/originalWidth, (double) maxHeight / originalHeight );


         int newWidth = (int) (originalWidth * scale);
         int newHeight = (int) (originalHeight * scale);

         Image scaled = original.getImage().getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);

        return new ImageIcon(scaled); 



    } // end method loadScaledImage()






} // end imageManager class 
