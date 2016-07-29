package zodiacdesktopapp.gui.mainwindow.decryptingpanel;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import zodiacdesktopapp.gui.filedialogs.SaveFileDialog;
import zodiacdesktopapp.model.settings.Settings;

/**
 *
 * @author Dmitry Akishin
 */
public class PopupMenu extends JPopupMenu {
    /*
        Всплывающее меню сохранения изображения.
    */

    /* Держатель изображения. */
    private ImageHolder holder;
    /* Родительский компонент. Необоходим на корректного вывода меню. */
    private Component parent;
    
    /* Разрешение сохраняемого изображения. */
    private String imgExt;

    public interface ImageHolder {
        BufferedImage getImage();
    }

    public PopupMenu(ImageHolder holder, Component parent) {
        super();
        this.holder = holder;
        this.parent = parent;
        imgExt = Settings.getInstance().getExportImageExtension();
        
        JMenuItem item = new JMenuItem("Save image");
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                BufferedImage image = holder.getImage();
                SaveFileDialog saveDialog = new SaveFileDialog("Save image");
                saveDialog.setCurrentDirectory(Settings.getInstance().getLastDirectoryForImage());
                saveDialog.setFileFilter(imgExt.toUpperCase() + " files", imgExt);
                int retrival = saveDialog.showSaveDialog(parent);
                if (retrival == SaveFileDialog.APPROVE_OPTION) {
                    try {
                        ImageIO.write(image, imgExt, saveDialog.getSelectedFile());
                    } catch (IOException ex) {
                        Logger.getLogger(PopupMenu.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    Settings.getInstance().setLastDirectoryForImage(saveDialog.getCurrentDirectory());
                }
            }
        });
        add(item);
    }

}
