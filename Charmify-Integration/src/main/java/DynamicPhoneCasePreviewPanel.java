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
import java.awt.BasicStroke;
import java.awt.RenderingHints;
import java.awt.Font;
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

    if (selectedCase == null) {
        // Show a visible case template before a product is selected.
        Graphics2D outline = (Graphics2D) g2D.create();
        outline.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        int w = Math.min(205, getWidth() - 44);
        int h = Math.min(365, getHeight() - 60);
        int x = (getWidth() - w) / 2;
        int y = (getHeight() - h) / 2;
        outline.setColor(new Color(255, 238, 245));
        outline.fillRoundRect(x, y, w, h, 38, 38);
        outline.setColor(new Color(194, 59, 104));
        outline.setStroke(new BasicStroke(3f));
        outline.drawRoundRect(x, y, w, h, 38, 38);
        outline.setColor(new Color(242, 204, 221));
        outline.fillRoundRect(x + 16, y + 16, 62, 84, 20, 20);
        outline.setColor(new Color(194, 59, 104));
        outline.drawRoundRect(x + 16, y + 16, 62, 84, 20, 20);
        outline.setFont(new Font("Arial", Font.BOLD, 13));
        String hint = "Choose a case to design";
        int tw = outline.getFontMetrics().stringWidth(hint);
        outline.drawString(hint, (getWidth() - tw) / 2, y + h / 2);
        outline.dispose();
    }
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
  
     selectedCharm = ImageManager.loadScaledImage(imagePath,150 ,150 );
     repaint();
      
     } // end method setSelectedCharm()  
     
    
     public void setSelectedSticker(String imagePath){
       
            addSticker(imagePath);
      } // end method setSelectedSticker()


    public void removeSelectedSticker(){
        // Remove the selected sticker, or the newest one on each click.
        if (stickers.isEmpty()) return;
        StickerItem target = selectedStickerItem != null && stickers.contains(selectedStickerItem)
                ? selectedStickerItem : stickers.get(stickers.size() - 1);
        stickers.remove(target);
        if (draggedSticker == target) draggedSticker = null;
        selectedStickerItem = null;
        repaint();
    }

    // Clear the case and its decorations from the preview.
    public void clearDesign() {
        selectedCase = null;
        selectedCharm = null;
        stickers.clear();
        draggedSticker = null;
        selectedStickerItem = null;
        repaint();
    }

    public void removeCharm(){
        selectedCharm = null; 
        repaint(); 

    } // end method removeCharm()
    




}// end DynamicPhoneCasePreviewPanel class
 