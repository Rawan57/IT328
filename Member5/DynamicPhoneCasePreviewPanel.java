/**
 *Provides an interactive preview of a customized phone case.
 * Displays the selected phone case, charm, and multiple stickers.
 * Allows customers to add, drag, select, and remove stickers
 */

import javax.swing.JPanel; 
import java.awt.Graphics; 
import java.awt.Graphics2D;
import javax.swing.ImageIcon; 
import java.awt.Dimension;
import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

public class DynamicPhoneCasePreviewPanel extends JPanel{

    private ImageIcon selectedCase; 
    private ImageIcon selectedCharm; 
    
    private int dragOffsetX; 
    private int dragOffsetY; 

    private ArrayList<StickerItem>  stickers = new ArrayList<>(); 

    private StickerItem draggedSticker = null; 
    private StickerItem selectedStickerItem = null;




    public DynamicPhoneCasePreviewPanel(){ // Constructor: 
 
        setPreferredSize(new Dimension(300, 500));
        setBackground(Color.WHITE);

        addMouseListener(new MouseAdapter() {

    @Override
    public void mousePressed(MouseEvent e) {

        draggedSticker=null; 


        for (int i = stickers.size() - 1 ; i>=0 ; i--){
            StickerItem sticker = stickers.get(i); 
int stickerX = sticker.getX();
        int stickerY = sticker.getY();

        int width = sticker.getImage().getIconWidth();
        int height = sticker.getImage().getIconHeight();

        // Check whether the mouse clicked this sticker
        if (e.getX() >= stickerX &&  e.getX() < stickerX + width && e.getY() >= stickerY &&
          e.getY() < stickerY + height) {

            draggedSticker = sticker;
            selectedStickerItem  = sticker ; 

            dragOffsetX = e.getX() - stickerX;
            dragOffsetY = e.getY() - stickerY;

            break;
        } // end if 
    } // end for 
} //end method mousePressed()



@Override
public void mouseReleased(MouseEvent e) {
    draggedSticker = null;
}
} ); // End addMouseListener

addMouseMotionListener(new MouseAdapter() {

    @Override
    public void mouseDragged(MouseEvent e) {

        if (draggedSticker != null && selectedCase != null) {

            int caseX = (getWidth() - selectedCase.getIconWidth()) / 2;
            int caseY = (getHeight() - selectedCase.getIconHeight()) / 2;

            int minX = caseX;
            int maxX = caseX + selectedCase.getIconWidth()
                    - draggedSticker.getImage().getIconWidth();

            int minY = caseY;
            int maxY = caseY + selectedCase.getIconHeight()
                    - draggedSticker.getImage().getIconHeight();

            int newX = Math.max(minX,
                    Math.min(e.getX() - dragOffsetX, maxX));

            int newY = Math.max(minY,
                    Math.min(e.getY() - dragOffsetY, maxY));

            draggedSticker.setPosition(newX, newY);

            repaint();
        }
    }
}); // End addMouseMotionListener

    }// end DynamicPhoneCasePreviewPanel constructor


   @Override 
   protected void paintComponent(Graphics g){

     super.paintComponent(g);

     Graphics2D g2D = (Graphics2D) g; 

    if (selectedCase != null) {

    // Calculate case position:
    int x = (getWidth() - selectedCase.getIconWidth()) / 2;
    int y = (getHeight() - selectedCase.getIconHeight()) / 2;

    //  Draw case:
    g2D.drawImage(selectedCase.getImage(), x, y, this);

    // Draw all stickers from the ArrayList
for (StickerItem sticker : stickers) {
    g2D.drawImage(
        sticker.getImage().getImage(),
        sticker.getX(),
        sticker.getY(),
        this
    );
}

    
    // Draw charm:
    if (selectedCharm != null) {
        int charmX = x + selectedCase.getIconWidth()
                     - selectedCharm.getIconWidth() / 2-15;

        int charmY = y + selectedCase.getIconHeight()
                     - selectedCharm.getIconHeight() + 15;

        g2D.drawImage(selectedCharm.getImage(), charmX, charmY, this);
    } // end Charm if 
} // end pohneCase if 
    

  }  // end method paintComponent()

  public void addSticker(String imagePath){
    ImageIcon stickerImage = ImageManager.loadScaledImage(imagePath,100, 100 );

    if (stickerImage == null){
        return ; 
    }

    int x = (getWidth() - stickerImage.getIconWidth()) / 2; 
    int y = (getHeight() - stickerImage.getIconHeight()) / 2; 

    StickerItem newSticker =  new StickerItem(stickerImage, x, y);

    stickers.add(newSticker);

    repaint(); 

  } // end method addSticker()




  
   public void setSelectedCase(String imagePath){
    selectedCase= ImageManager.loadScaledImage(imagePath, 250, 450);
    repaint();


   } // end method setSelectedCase()


     public void setSelectedCharm(String imagePath){
  
     selectedCharm = ImageManager.loadScaledImage(imagePath,100 ,100 );
     repaint();
      
     } // end method setSelectedCharm()  
     
    
     public void setSelectedSticker(String imagePath){
       
            addSticker(imagePath);
      } // end method setSelectedSticker()


    public void removeSelectedSticker(){

      if (selectedStickerItem != null){
          stickers.remove(selectedStickerItem);

          if (draggedSticker == selectedStickerItem){
            draggedSticker = null;  }
            selectedStickerItem = null; 
            repaint();

        }// end if 

    } // end method removeSelectedStickr()


    public void removeCharm(){
        selectedCharm = null; 
        repaint(); 

    } // end method removeCharm()
    




}// end DynamicPhoneCasePreviewPanel class
 