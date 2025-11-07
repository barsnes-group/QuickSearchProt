package no.uib.probe.quicksearchprot.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import javax.swing.ImageIcon;
import javax.swing.JProgressBar;

/**
 * A custom progress bar that fills its completed portion with a tiled image,
 * instead of traditional solid color fill.
 * 
 * <p>
 * The fill image is loaded from the resource path "/progress.gif".
 * Text is not painted unless enabled. By default, the string painting is
 * disabled for a cleaner appearance.
 * </p>
 * 
 * <p>
 * Example usage:
 * <pre>
 *     ImageProgressBar progressBar = new ImageProgressBar();
 *     progressBar.setValue(50); // set progress to 50%
 * </pre>
 * </p>
 * 
 * @author Yehia Mokhtar Farag
 */
public class ImageProgressBar extends JProgressBar {

    /** Image used to fill the progress portion of the bar. */
    private final Image fillImage;

    /**
     * Constructs a new {@code ImageProgressBar} with an image fill.
     * The image is loaded from {@code /progress.gif} on the classpath.
     * Throws {@code IllegalArgumentException} if the resource is missing.
     */
    public ImageProgressBar() {
        super();
        ImageIcon icon = new ImageIcon(getClass().getResource("/progress.gif"));
        if (icon.getImage() == null) {
            throw new IllegalArgumentException("Resource '/progress.gif' not found.");
        }
        this.fillImage = icon.getImage();
        setStringPainted(false);
    }

    /**
     * Paints the progress bar with a tiled image pattern for the completed portion,
     * and optionally draws the progress text (if enabled).
     *
     * @param g the {@code Graphics} context in which to paint
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Calculate width of the completed region.
        int progressWidth = (int) (getPercentComplete() * getWidth());
        int barHeight = getHeight();

        if (fillImage != null) {
            // Tile the fill image horizontally inside the completed area.
            int tileWidth = 1; // Width of each image tile in pixels.
            int yOffset = 3;
            int height = Math.max(barHeight - 6, 0);
            for (int x = 3; x < progressWidth - 3; x += tileWidth) {
                g.drawImage(fillImage, x, yOffset, tileWidth, height, this);
            }
        }

        // Draw progress text if enabled (centered, default string).
//        if (isStringPainted()) {
            String text = getString();
            int strWidth = g.getFontMetrics().stringWidth(text);
            int strHeight = g.getFontMetrics().getAscent();
            int x = (getWidth() - strWidth) / 2;
            int y = (getHeight() + strHeight) / 2 - 2;
            g.setColor(Color.BLACK);
            g.drawString(text, x, y);
//        }
    }
}