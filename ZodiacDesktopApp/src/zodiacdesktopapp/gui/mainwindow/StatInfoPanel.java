package zodiacdesktopapp.gui.mainwindow;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import zodiacdesktopapp.model.Model;
import zodiacdesktopapp.model.StatUnit;

/**
 *
 * @author Dmitry Akishin
 */
public class StatInfoPanel extends JPanel {
    /*
        Панель статистики. Показывает количество повторений для каждого символа.
    */
    
    /* Лейблы для изображения символа и числа повторений. */
    private JLabel imgLabel;
    private JLabel numberLabel;
    /* Панель информации об одном символе. */
    private JPanel symbPanel;

    /* Список статистических данных. */
    ArrayList<StatUnit> stats;

    /* Статистика в виде изображения. */
    BufferedImage img;

    /* Обновление по переданной модели. */
    public void update(Model model) {
        if (model == null) {
            img = null;
            stats = null;
            this.removeAll();
        } else {
            img = model.getStatisticsAsImage();
            stats = model.getStatistics();

            addPanels(this);
        }
        revalidate();
    }

    /* Добавление панелей, по панели на каждый символ. */
    private void addPanels(JPanel panel) {
        if (stats != null) {
            panel.removeAll();
            panel.setLayout(new GridLayout(0, 1, 10, 10));
            Font font = new Font(null, Font.BOLD, 22);
            for (StatUnit s : stats) {
                symbPanel = new JPanel();
                symbPanel.setLayout(new BoxLayout(symbPanel, BoxLayout.X_AXIS));
                imgLabel = new JLabel();
                imgLabel.setIcon(new ImageIcon(s.getImage()));
                numberLabel = new JLabel();
                numberLabel.setFont(font);
                numberLabel.setHorizontalAlignment(JLabel.CENTER);
                numberLabel.setText(" - " + s.getNumber());
                symbPanel.add(imgLabel);
                symbPanel.add(numberLabel);
                panel.add(symbPanel);
            }
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
    }

}
