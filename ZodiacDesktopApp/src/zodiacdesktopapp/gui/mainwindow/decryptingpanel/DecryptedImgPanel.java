package zodiacdesktopapp.gui.mainwindow.decryptingpanel;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import javax.swing.JPanel;
import zodiacdesktopapp.gui.drawables.Image;
import zodiacdesktopapp.model.Model;
import zodiacdesktopapp.model.SymbModel;
import zodiacdesktopapp.model.settings.Settings;

/**
 *
 * @author Dmitry Akishin
 */
public class DecryptedImgPanel extends JPanel implements MouseListener {
    /*
        Панель расшифруемого изображения.
        Выводит изображение и поверх него буквы для каждого символа в подходящих местах.
        Также обрабатывается нажатие мышью на тот или иной символ. 
    */
    
    /* Основное изображение. */
    private Image image;

    /* Модель. */
    private Model model;

    /* Список символов для прорисовки. Каждый символ может содержать
        букву, которую надо вывести вместо символа. */
    private ArrayList<CharToDraw> charsToDraw = new ArrayList<>();

    /* Слушатель нажатия мышью. */
    private OnImageClickedListener listener;

    /* Коэффициенты масштабирования и позиция выведенного изображения.
        Необходимы для корректного расположения символов. */
    private float scaleX = 1, scaleY = 1;
    private int imgX, imgY;

    /* Установка модели, вместе с не и изображения из модели. */
    public void setModel(Model model) {
        this.model = model;
        this.image = new Image(model.getImage());
        createCharsToDraw();
        this.addMouseListener(this);
        repaint();
    }
    
    /* Очистка панели. */
    public void clearAll(){
        model = null;
        image = null;
        charsToDraw.clear();
        this.addMouseListener(null);
        repaint();
    }

    public void setListener(OnImageClickedListener listener) {
        this.listener = listener;
    }

    /* Создание списка символов для прорисовки. Пока юез установления 
        букв для символов. */
    private void createCharsToDraw() {
        charsToDraw.clear();
        ArrayList<SymbModel.SymbInfo> symbols = model.getSymbols();
        for (SymbModel.SymbInfo s : symbols) {
            charsToDraw.add(new CharToDraw(s));
        }
    }

    /* Удаление буквы для символа. */
    public void clearSymb(SymbModel.SymbInfo symb) {
        for (CharToDraw chr : charsToDraw) {
            if (chr.isEqual(symb)) {
                chr.removeCharacter();
            }
        }
        repaint();
    }

    /* Замена буквы для символа. */
    public void replaceSymb(SymbModel.SymbInfo symb, char chr) {
        for (CharToDraw chrToDraw : charsToDraw) {
            if (chrToDraw.isEqual(symb)) {
                chrToDraw.setCharacter(chr);
            }
        }
        repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (image != null) {
            image.draw(g, 0, 0, this.getWidth(), this.getHeight(), false);
            imgX = image.getX();
            imgY = image.getY();
            scaleX = image.getScale()[0];
            scaleY = image.getScale()[1];
        }

        for (CharToDraw chr : charsToDraw) {
            chr.draw(g, imgX, imgY, model.getCellWidth(), model.getCellHeight(), 
                    scaleX, scaleY, 1);
        }
    }

    private int symbId;

    /* При нажатии клавиши мыши на изображении, через модель узнается id символа,
        находящегося в соответствующей позиции, и передается слушателю. */
    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.isMetaDown()) {
            if (image != null) {
                PopupMenu menu = new PopupMenu(new PopupMenu.ImageHolder() {
                    @Override
                    public BufferedImage getImage() {
                        return createImage();
                    }
                }, this);
                menu.show(e.getComponent(), e.getX(), e.getY());
                return;
            }
        }
        symbId = model.getSymbIdByCoord((int) ((e.getX() - imgX) / scaleX), (int) ((e.getY() - imgY) / scaleY));
        listener.action(symbId);
    }

    /* Создаение изображения для последующего сохранения.
        Содержит подходящие буквы вместо символов. */
    private BufferedImage createImage() {
        BufferedImage res = new BufferedImage(image.getImg().getWidth(),
                image.getImg().getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = res.createGraphics();
        g.drawImage(image.getImg(), 0, 0, res.getWidth(), res.getHeight(), null);
        for (CharToDraw chr : charsToDraw) {
            chr.draw(g, 0, 0, model.getCellWidth(), model.getCellHeight(), 1, 1, scaleY);
        }
        g.dispose();
        return res;
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

    public interface OnImageClickedListener {
        void action(int symbId);
    }

    /* Класс, содержащий информацию о символе и его позициях, 
        а также букву, которой надо заменить символ. */
    private class CharToDraw {

        /* Идентификатор символа. */
        private int id;
        /* Позиции символа. */
        private ArrayList<SymbModel.Location> locations;
        /* Заменяющая буква. */
        private Character character;

        CharToDraw(SymbModel.SymbInfo symb) {
            this.id = symb.getId();
            this.locations = symb.getLocations();
        }

        void removeCharacter() {
            this.character = null;
        }

        void setCharacter(Character character) {
            this.character = character;
        }

        boolean isEqual(SymbModel.SymbInfo symb) {
            return this.id == symb.getId();
        }

        private int x, y, w, h, dx, dy;
        private Color prevColor, currColor;
        private Font currFont, prevFont;
        private String textToDraw;
        private FontMetrics fontMetrics;

        /* Рисование буквы на месте символа. 
            scaleFont - во сколько надо уменьшить шрифт
            =1, если шрифт подгонять не надо */
        public void draw(Graphics g, int backImgX, int backImgY, int width,
                int height, float scaleX, float scaleY, float scaleFont) {
            if (character != null) {
                prevColor = g.getColor();
                prevFont = g.getFont();
                currFont = Settings.getInstance().getFont();
                currFont = currFont.deriveFont(currFont.getSize() / scaleFont);
                fontMetrics = g.getFontMetrics(currFont);
                currColor = Settings.getInstance().getFontColor();
                for (SymbModel.Location loc : locations) {
                    x = backImgX + (int) (loc.getX() * width * scaleX);
                    y = backImgY + (int) (loc.getY() * height * scaleY);
                    w = (int) (width * scaleX);
                    h = (int) (height * scaleY);

                    g.setColor(Color.white);
                    g.fillRect(x, y, w, h);

                    g.setFont(currFont);
                    g.setColor(currColor);

                    textToDraw = Character.toString(character);
                    dx = (w - fontMetrics.stringWidth(textToDraw)) / 2;
                    dy = ((h - fontMetrics.getHeight()) / 2) + fontMetrics.getAscent();
                    g.drawString(textToDraw, x + dx, y + dy);
                }
                g.setColor(prevColor);
                g.setFont(prevFont);
            }
        }

        public int getId() {
            return id;
        }

    }
}
