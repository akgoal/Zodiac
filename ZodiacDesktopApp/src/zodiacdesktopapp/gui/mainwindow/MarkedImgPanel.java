package zodiacdesktopapp.gui.mainwindow;

import java.awt.Graphics;
import javax.swing.JPanel;
import zodiacdesktopapp.gui.drawables.Grid;
import zodiacdesktopapp.gui.drawables.Image;
import zodiacdesktopapp.model.Model;

/**
 *
 * @author Dmitry
 */
public class MarkedImgPanel extends JPanel {
    /*
        Панель размеченного изображения.
    */
    
    /* Изображение и решетка на нем. */
    private Image image;
    private Grid grid;

    /* Коэф-ы масштабирования, позиция и размеры выведенного изображения. */
    private float scaleX = 1, scaleY = 1;
    private int imgX, imgY, imgWidth, imgHeight;

    public MarkedImgPanel() {
        super();
    }

    /* Обновления панели по переданной модели. */
    public void update(Model model) {
        if (model == null) {
            image = null;
            grid = null;
        } else {
            image = new Image(model.getImage());
            grid = new Grid();
            grid.set(0, 0, model.getGridWidth(),
                    model.getGridHeight(), model.getCellWidth(), model.getCellHeight());
        }
        repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (image != null) {
            image.draw(g, 0, 0, this.getWidth(), this.getHeight(), false);
            scaleX = image.getScale()[0];
            scaleY = image.getScale()[1];
            imgX = image.getX();
            imgY = image.getY();
            imgWidth = image.getWidth();
            imgHeight = image.getHeight();
        }
        if (grid != null) {
            grid.setScale(scaleX, scaleY);
            grid.draw(g, imgX, imgY, imgWidth, imgHeight, false);
        }
    }

}
