package zodiacdesktopapp.gui.drawables;

import java.awt.Color;
import java.awt.Graphics;

/**
 *
 * @author Dmitry Akishin
 */
public class Square implements Drawable{
    /*
        Квадрат.
    */
    
    /* Цвет. */
    private Color color;
    
    /* прозрачность, от 0 до 1 */
    private float opacity;
    
    public Square() {
    }
    
    private Color prevColor;
    
    @Override
    public void draw(Graphics g, int x, int y, int w, int h, boolean toFit) {
        prevColor = g.getColor();
        g.setColor(color);
        g.fillRect(x, y, w, h);
        g.setColor(prevColor);
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = new Color(color.getRed(), color.getGreen(), color.getBlue(), (int)(opacity * 255));
    }

    public float getOpacity() {
        return opacity;
    }

    public void setOpacity(float opacity) {
        this.opacity = opacity;
        setColor(color);
    }
    
    
    
}
