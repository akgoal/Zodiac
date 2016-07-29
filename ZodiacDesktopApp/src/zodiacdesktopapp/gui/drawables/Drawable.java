package zodiacdesktopapp.gui.drawables;

import java.awt.Graphics;

/**
 *
 * @author Dmitry Akishin
 */
public interface Drawable {
    /*
        Интерфейс описывает объекты, которые можно нарисовать
    */
    
    /* Рисование объекта. x,y - позиции левого верхнего угла,
        w - ширина, h - высота, toFit определяет, должен ли объект заполнить 
        выделенное ему место. */
    void draw(Graphics g, int x, int y, int w, int h, boolean toFit);
}
