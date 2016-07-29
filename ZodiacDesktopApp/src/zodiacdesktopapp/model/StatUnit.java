package zodiacdesktopapp.model;

import java.awt.image.BufferedImage;

/**
 *
 * @author Dmitry Akishin
 */
public class StatUnit {
    /*
        Единица статистики. Содержит изображение символа и число повторений символа.
    */
    private BufferedImage image;
    private int number;
    
    public StatUnit(BufferedImage image, int number) {
        this.image = image;
        this.number = number;
    }

    public BufferedImage getImage() {
        return image;
    }

    public void setImage(BufferedImage image) {
        this.image = image;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }
    
}
