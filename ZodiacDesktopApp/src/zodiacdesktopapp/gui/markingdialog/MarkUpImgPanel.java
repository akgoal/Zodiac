package zodiacdesktopapp.gui.markingdialog;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import javax.swing.JPanel;
import zodiacdesktopapp.gui.drawables.Grid;
import zodiacdesktopapp.gui.drawables.Image;
import zodiacdesktopapp.gui.drawables.Square;
import zodiacdesktopapp.model.marking.MarkUpManager;

/**
 *
 * @author Dmitry Akishin
 */
public class MarkUpImgPanel extends JPanel implements MouseListener {
    /*
        Панель размечаемого изображения.
        Разметка происходит путем выделения одинаковых символов.
    */
    
    /* Изображение и решетка. */
    private Image img;
    private Grid grid;
    /* Квадраты, наносимые поверх изоражения.
        Определяют, соответственно, текущий символ, размеченный символ, 
        первый символ в текущей последовательности и первый символ в уже размеченной 
        последовательности. Первый символ последовательности заменит в разметке все 
        другие символы в последовательности. */
    private Square currSquare, markedSquare, currFirstSquare, markedFirstSquare;

    /* Коэффициенты масштабирования, позиция и размеры изображения. */
    private float scaleX = 1, scaleY = 1;
    private int imgX, imgY, imgWidth, imgHeight;

    /* Менеджер разметки. */
    private MarkUpManager markingManager;

    public MarkUpImgPanel() {
        grid = new Grid();

        currSquare = new Square();
        currSquare.setColor(Color.red);
        currSquare.setOpacity(0.4f);

        currFirstSquare = new Square();
        currFirstSquare.setColor(Color.red);
        currFirstSquare.setOpacity(0.6f);

        markedSquare = new Square();
        markedSquare.setColor(Color.gray);
        markedSquare.setOpacity(0.4f);
        
        markedFirstSquare = new Square();
        markedFirstSquare.setColor(Color.gray);
        markedFirstSquare.setOpacity(0.6f);

        this.addMouseListener(this);
    }

    /* Установка менеджера разметки. */
    public void setMarkingManager(MarkUpManager manager) {
        this.markingManager = manager;

        img = new Image(manager.getImg().getSubimage(manager.getOffsetX(), manager.getOffsetY(),
                manager.getGridWidth() * manager.getCellWidth(), manager.getGridHeight() * manager.getCellHeight()));

        grid.set(0, 0, manager.getGridWidth(),
                manager.getGridHeight(), manager.getCellWidth(), manager.getCellHeight());
        repaint();
    }

    /* Переход к следующей последовательности. */
    public void goToNextSequence() {
        markingManager.goToNextSequence();
        repaint();
    }

    /* Возврат к предыдущей последовательности. */
    public void goToPrevSequence() {
        markingManager.goToPrevSequence();
        repaint();
    }

    /* Проверяет, является ли текущая последовательности первой. */
    public boolean isFirstSeq() {
        return markingManager.isCurrSeqFirst();
    }

    private Square squareToDraw;

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.gray);
        g.fillRect(0, 0, this.getWidth(), this.getHeight());
        if (img != null) {
            img.draw(g, 0, 0, this.getWidth(), this.getHeight(), false);
            scaleX = img.getScale()[0];
            scaleY = img.getScale()[1];
            imgX = img.getX();
            imgY = img.getY();
            imgWidth = img.getWidth();
            imgHeight = img.getHeight();
        }
        grid.setScale(scaleX, scaleY);
        grid.draw(g, imgX, imgY, imgWidth, imgHeight, false);

        if (markingManager != null) {

            squareToDraw = markedSquare;
            // рисуем помеченные квадраты
            for (ArrayList<MarkUpManager.Position> seq : markingManager.getSequences()) {
                for (int i = 0; i < seq.size(); i++) {
                    MarkUpManager.Position pos = seq.get(i);
                    if (i == 0) {
                        squareToDraw = markedFirstSquare;
                    } else {
                        squareToDraw = markedSquare;
                    }
                    squareToDraw.draw(g, imgX + (int) (pos.x * grid.getCellWidth() * scaleX),
                            imgY + (int) (pos.y * grid.getCellHeight() * scaleY),
                            (int) (grid.getCellWidth() * scaleX), (int) (grid.getCellHeight() * scaleY), true);
                }
            }

            //рисуем квадраты текущей последовательности
            for (int i = 0; i < markingManager.getCurrentSequence().size(); i++) {
                MarkUpManager.Position pos = markingManager.getCurrentSequence().get(i);
                if (i == 0) {
                    squareToDraw = currFirstSquare;
                } else {
                    squareToDraw = currSquare;
                }
                squareToDraw.draw(g, imgX + (int) (pos.x * grid.getCellWidth() * scaleX),
                        imgY + (int) (pos.y * grid.getCellHeight() * scaleY),
                        (int) (grid.getCellWidth() * scaleX), (int) (grid.getCellHeight() * scaleY), true);
            }
        }
    }

    /* При клике на символ, информация о символе передается менеджеру разметки. */
    @Override
    public void mouseClicked(MouseEvent e) {
        markingManager.addCoordToSequence((int) ((e.getX() - imgX) / scaleX), (int) ((e.getY() - imgY) / scaleY));
        repaint();
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }
}
