package zodiacdesktopapp.gui.markingdialog;

import java.awt.Color;
import java.awt.Graphics;
import javax.swing.JPanel;
import zodiacdesktopapp.gui.drawables.Grid;
import zodiacdesktopapp.gui.drawables.Image;

/**
 *
 * @author Dmitry Akishin
 */
public class GridImgPanel extends JPanel {
    /*
        Панель изображения с нанесенной поверх решетки.
    */
    
    /* Изображение. */
    private Image img;
    /* Решетка. */
    private Grid grid;

    public GridImgPanel() {
        grid = new Grid();
    }

    public void setImg(Image img) {
        this.img = img;
        repaint();
    }

    public void setGrid(int offsetX, int offsetY, int cellWidth, int cellHeight,
            int gridWidth, int gridHeight) {
        grid.set(offsetX, offsetY, gridWidth, gridHeight, cellWidth, cellHeight);
        repaint();
    }
    
    /* Получение параметров решетки. */
    public int[] getGridParams() {
        int[] res = new int[6];
        res[0]  = grid.getOffsetX();
        res[1]  = grid.getOffsetY();
        res[2]  = grid.getCellWidth();
        res[3]  = grid.getCellHeight();
        res[4]  = grid.getGridWidth();
        res[5]  = grid.getGridHeight();
        return res;
    }

    /* Получение максимального размера решетки в ячейках. */
    public int[] getMaxGridSize() {
        int[] res = new int[2];
        res[0] = (int) Math.ceil((img.getImgWidth() - grid.getOffsetX()) / (float) grid.getCellWidth());
        res[1] = (int) Math.ceil((img.getImgHeight() - grid.getOffsetY()) / (float) grid.getCellHeight());
        return res;
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.gray);
        g.fillRect(0, 0, this.getWidth(), this.getHeight());
        if (img != null) {
            img.draw(g, 0, 0, this.getWidth(), this.getHeight(), false);
            grid.setScale(img.getScale()[0], img.getScale()[1]);
            grid.draw(g, img.getX(), img.getY(), img.getWidth(), img.getHeight(), false);
        } 
    }
}
