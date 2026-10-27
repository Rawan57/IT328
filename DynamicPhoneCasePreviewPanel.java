/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package server;

import javax.swing.JPanel;
import javax.swing.ImageIcon; 
import java.awt.Graphics; 
  import java.awt.Graphics2D; 
import java.awt.Dimension; 
import java.awt.Color;
import java.awt.BasicStroke;
import java.awt.RenderingHints; 
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent; 
import java.util.ArrayList;
/**
Provides an interactive preview of a customized phone case.
Displays one selected charm and multiple draggable stickers. */ 
public class DynamicPhoneCasePreviewPanel extends JPanel {
private ImageIcon selectedCase; private ImageIcon selectedCharm;
private int dragOffsetX; private int dragOffsetY;
private ArrayList<StickerItem> stickers = new ArrayList<>();
private StickerItem draggedSticker = null; private StickerItem selectedStickerItem = null;
// Constructor 
public DynamicPhoneCasePreviewPanel() {
 setPreferredSize(new Dimension(300, 500));
 setBackground(Color.WHITE);

 addMouseListener(new MouseAdapter() {

     @Override
     public void mousePressed(MouseEvent e) {

         draggedSticker = null;

         // Check stickers from newest to oldest
         for (int i = stickers.size() - 1; i >= 0; i--) {

             StickerItem sticker = stickers.get(i);

             int stickerX = sticker.getX();
             int stickerY = sticker.getY();

             int width = sticker.getImage().getIconWidth();
             int height = sticker.getImage().getIconHeight();

             // Check whether the mouse clicked this sticker
             if (e.getX() >= stickerX &&
                 e.getX() < stickerX + width &&
                 e.getY() >= stickerY &&
                 e.getY() < stickerY + height) {

                 draggedSticker = sticker;
                 selectedStickerItem = sticker;

                 dragOffsetX = e.getX() - stickerX;
                 dragOffsetY = e.getY() - stickerY;

                 break;
             }
         }
     }

     @Override
     public void mouseReleased(MouseEvent e) {
         draggedSticker = null;
     }

 });

 addMouseMotionListener(new MouseAdapter() {

     @Override
     public void mouseDragged(MouseEvent e) {

         if (draggedSticker != null && selectedCase != null) {

             int caseX =
                 (getWidth() - selectedCase.getIconWidth()) / 2;

             int caseY =
                 (getHeight() - selectedCase.getIconHeight()) / 2;

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

 });
} // End constructor
@Override protected void paintComponent(Graphics g) {
 super.paintComponent(g);

 Graphics2D g2D = (Graphics2D) g;

 // Draw default case template
 if (selectedCase == null) {

     Graphics2D outline = (Graphics2D) g2D.create();

     outline.setRenderingHint(
         RenderingHints.KEY_ANTIALIASING,
         RenderingHints.VALUE_ANTIALIAS_ON
     );

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
     outline.fillRoundRect(
         x + 16, y + 16, 62, 84, 20, 20
     );

     outline.setColor(new Color(194, 59, 104));
     outline.drawRoundRect(
         x + 16, y + 16, 62, 84, 20, 20
     );

     outline.setFont(new Font("Arial", Font.BOLD, 13));

     String hint = "Choose a case to design";

     int tw = outline.getFontMetrics().stringWidth(hint);

     outline.drawString(
         hint,
         (getWidth() - tw) / 2,
         y + h / 2
     );

     outline.dispose();
 }

 if (selectedCase != null) {

     // Calculate case position
     int x =
         (getWidth() - selectedCase.getIconWidth()) / 2;

     int y =
         (getHeight() - selectedCase.getIconHeight()) / 2;

     // Draw phone case
     g2D.drawImage(
         selectedCase.getImage(), x, y, this
     );

     // Draw all stickers
     for (StickerItem sticker : stickers) {

         g2D.drawImage(
             sticker.getImage().getImage(),
             sticker.getX(),
             sticker.getY(),
             this
         );
     }

     // Original charm drawing - unchanged
     if (selectedCharm != null) {

         int charmX = x + selectedCase.getIconWidth()
             - selectedCharm.getIconWidth() / 2 - 15;

         int charmY = y + selectedCase.getIconHeight()
             - selectedCharm.getIconHeight() + 15;

         g2D.drawImage(
             selectedCharm.getImage(),
             charmX,
             charmY,
             this
         );
     }
 }
} // End paintComponent
// Add a new sticker without removing previous stickers
public void addSticker(String imagePath) {

    ImageIcon stickerImage =
        ImageManager.loadScaledImage(imagePath, 55, 55);

    if (stickerImage == null) {
        return;
    }

    // Center the first sticker
    int x =
        (getWidth() - stickerImage.getIconWidth()) / 2;

    int y =
        (getHeight() - stickerImage.getIconHeight()) / 2;

    // Place each new sticker slightly above the previous one
    y = y - (stickers.size() * 20);

    StickerItem newSticker =
        new StickerItem(stickerImage, x, y);

    // Keep all previous stickers
    stickers.add(newSticker);

    selectedStickerItem = newSticker;
    draggedSticker = null;

    repaint();
} // End addSticker

// Select phone case
public void setSelectedCase(String imagePath) {

    int width = Math.max(
        1, Math.min(250, getWidth() - 30)
    );

    int height = Math.max(
        1, Math.min(450, getHeight() - 30)
    );

    selectedCase = ImageManager.loadScaledImage(
        imagePath, width, height
    );

    repaint();
}

// Original charm method - unchanged
public void setSelectedCharm(String imagePath) {

    selectedCharm =
        ImageManager.loadScaledImage(imagePath, 120, 120);

    repaint();
}

// Select sticker
public void setSelectedSticker(String imagePath) {

    addSticker(imagePath);
}

// Remove selected sticker
public void removeSelectedSticker() {

    if (stickers.isEmpty()) {
        return;
    }

    StickerItem target =
        selectedStickerItem != null
        && stickers.contains(selectedStickerItem)
            ? selectedStickerItem
            : stickers.get(stickers.size() - 1);

    stickers.remove(target);

    if (draggedSticker == target) {
        draggedSticker = null;
    }

    selectedStickerItem = null;

    repaint();
}

// Clear the case and all decorations
public void clearDesign() {

    selectedCase = null;
    selectedCharm = null;

    stickers.clear();

    draggedSticker = null;
    selectedStickerItem = null;

    repaint();
}

// Original charm removal - unchanged
public void removeCharm() {

    selectedCharm = null;
    repaint();
}
} // End DynamicPhoneCasePreviewPanel class