package no.uib.probe.quicksearchprot.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

/**
 *
 * @author Yehia
 */
public class ImageProgressBar extends JProgressBar {

    private Image fillImage;

    public ImageProgressBar() {
        super();
        ImageIcon icon = new ImageIcon(getClass().getResource("/progress.gif"));
        this.fillImage = icon.getImage();
        setStringPainted(false);
    }

    @Override
    protected void paintComponent(Graphics g) {

        super.paintComponent(g);
        int width = (int) ((getPercentComplete()) * getWidth());
        if (fillImage != null) {
            int tileWidth = 1;
            for (int x = 3; x < width-3; x += tileWidth) {
                g.drawImage(fillImage, x, 3, tileWidth, 19, this);
            }
        }
        g.setColor(Color.BLACK); g.drawString(getString(), getWidth() / 2 - 20, getHeight() / 2 + 5); 
    }

   
}
