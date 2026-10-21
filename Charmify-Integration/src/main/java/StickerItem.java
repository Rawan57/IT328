/**
 * Represents an individual sticker placed on a customized phone case.
 * Stores the sticker image and its position, allowing each sticker
 * to be moved independently within the preview.
 * 
 */
import javax.swing.ImageIcon; 

public class StickerItem {

  private ImageIcon image ;
  private int x; 
  private  int y ; 

  public StickerItem(ImageIcon image, int x, int y){ // constructor: 
    this.image = image; 
    this.x= x; 
    this.y = y; 
  }

  public ImageIcon getImage(){ // Getters: 
    return image;
 }

  public int getX(){
    return x; 
} 

   public int getY(){
     return y; 
    }
    
    public void setPosition(int x, int y ){
        this.x = x; 
        this.y = y; 

    }

 



} // end class StickerItem
