package zodiacdesktopapp.gui.mainwindow;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import zodiacdesktopapp.gui.drawables.Image;

/**
 *
 * @author Dmitry Akishin
 */
public class SrcImgPanel extends JPanel {
    /*
        Панель исходного изображения.
    */
    
    /* Изображение. */
    private Image img;

    public void setImg(Image img) {
        this.img = img;
    }

    public Image getImg() {
        return img;
    }

    /* Установка изображения из файла. */
    public boolean setImgFile(File file) {
        if (file != null) {
            BufferedImage bufImg;
            try {
                bufImg = ImageIO.read(file);
                return setBufImg(bufImg);
            } catch (IOException ex) {
                return setBufImg(null);
            }
        } else {
            return setBufImg(null);
        }
    }

    /* Установка изображения из картинки BufferedImage. */
    public boolean setBufImg(BufferedImage bufferedImg) {
        if (bufferedImg == null) {
            JOptionPane.showMessageDialog(this,
                    "Unable to load source image.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        } else {
            img = new Image(bufferedImg);
            repaint();
            return true;
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (img != null) {
            img.draw(g, 0, 0, this.getWidth(), this.getHeight(), false);
        }
    }
}
