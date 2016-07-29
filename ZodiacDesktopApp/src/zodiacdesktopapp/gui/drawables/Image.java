package zodiacdesktopapp.gui.drawables;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 *
 * @author Dmitry Akishin
 */
public class Image implements Drawable {
    /*
        Изображение.
    */
    
    /* Рисуемая картинка. */
    private BufferedImage img;
    
    /* Позиция и размеры. */
    private int x,y,width,height;
    
    /* Фактические размеры. */
    private int imgWidth, imgHeight;

    public Image() {
    }

    public Image(String imgFileName) {
        try {
            img = ImageIO.read(new File(imgFileName));
            imgWidth = img.getWidth();
            imgHeight = img.getHeight();
        } catch (IOException ex) {
            Logger.getLogger(Image.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public Image(BufferedImage img) {
        this.img = img;
        imgWidth = img.getWidth();
        imgHeight = img.getHeight();
    }

    @Override
    public void draw(Graphics g, int x, int y, int w, int h, boolean toFit) {
        Color bgColor = Color.black;
        if (toFit) {
            this.x = x;
            this.y = y;
            this.width = w;
            this.height = h;
        } else {
            float ratio = w / (float) h;
            float imgRatio = img.getWidth() / (float) img.getHeight();        
            if (ratio > imgRatio) {
                // Окно шире чем изображение
                this.y = y;
                this.height = h;
                this.width = (int) (imgRatio * this.height);
                this.x = x + (w - this.width) / 2;
            } else {
                this.x = x;
                this.width = w;
                this.height = (int) (this.width / imgRatio);
                this.y = y + (h - this.height) / 2;
            }
        }
        g.drawImage(img, this.x, this.y, this.width, this.height, bgColor, null);
    }
    
    /* Масштаб, с которым выведено изображение. */
    public float[] getScale() {
        float[] res = new float[2];
        res[0] = width/(float)imgWidth;
        res[1] = height/(float)imgHeight;
        return res;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
    
    public int getImgWidth() {
        return imgWidth;
    }
    
    public int getImgHeight() {
        return imgHeight;
    }

    public BufferedImage getImg() {
        return img;
    }
    
    
}

