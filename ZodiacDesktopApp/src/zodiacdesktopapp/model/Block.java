package zodiacdesktopapp.model;

import java.awt.image.BufferedImage;

/**
 *
 * @author Dmitry Akishin
 */
public class Block {
    /*
        Блок, содержащий id и изображения.
    */
    private BufferedImage image;
    private int imgId = -1;
    
    public Block(){
    }
    
    public Block(BufferedImage buffImg, int imgId) {
        this.image = buffImg;
        this.imgId = imgId;
    }

    public BufferedImage getImage() {
        return image;
    }

    public void setImage(BufferedImage image) {
        this.image = image;
    }

    public int getImgId() {
        return imgId;
    }

    public void setImgId(int imgId) {
        this.imgId = imgId;
    }
    
    
}
