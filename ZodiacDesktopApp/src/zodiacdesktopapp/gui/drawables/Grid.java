package zodiacdesktopapp.gui.drawables;

import java.awt.Graphics;

/**
 *
 * @author Dmitry Akishin
 */
public class Grid implements Drawable {
    /*
        Решетка.
    */
    
    /* Ширина и высота решетки в ячейках. */
    private int gridWidth = 1, gridHeight = 1;

    /* Ширина и высота ячейки в пикселях */
    private int cellWidth = 1, cellHeight = 1;

    /* Отступы. */
    private int offsetX, offsetY;

    /* Коэффициенты масштабирования. */
    private float scaleX = 1, scaleY = 1;

    public Grid() {
    }

    public void setScale(float scaleX, float scaleY) {
        this.scaleX = scaleX;
        this.scaleY = scaleY;
    }

    @Override
    public void draw(Graphics g, int x, int y, int w, int h, boolean toFit) {
        
        for (int i = 0; i < gridWidth; i++) {
            for (int j = 0; j < gridHeight; j++) {
                g.drawRect((int) (x + (offsetX + i * cellWidth) * scaleX), (int) (y + (offsetY + j * cellHeight) * scaleY),
                        (int) (cellWidth * scaleX), (int) (cellHeight * scaleY));
            }
        }
    }

    public void set(int offsetX, int offsetY, int width, int height, int cellWidth, int cellHeight) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.gridWidth = width;
        this.gridHeight = height;
        this.cellWidth = cellWidth;
        this.cellHeight = cellHeight;
    }

    public int getCellWidth() {
        return cellWidth;
    }

    public int getCellHeight() {
        return cellHeight;
    }

    public int getOffsetX() {
        return offsetX;
    }

    public int getOffsetY() {
        return offsetY;
    }

    public int getGridWidth() {
        return gridWidth;
    }

    public void setGridWidth(int gridWidth) {
        this.gridWidth = gridWidth;
    }

    public int getGridHeight() {
        return gridHeight;
    }

    public void setGridHeight(int gridHeight) {
        this.gridHeight = gridHeight;
    }

    
    
}
