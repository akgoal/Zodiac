package zodiacdesktopapp.gui.filedialogs;

import java.awt.Component;
import java.io.File;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 *
 * @author Dmitry Akishin
 */
public class OpenFileDialog extends JFileChooser{
    /*
        Диалог открытия файла.
    */
    
    public OpenFileDialog(String title){
        super();
        this.setDialogTitle(title);      
        this.setAcceptAllFileFilterUsed(false);
    }
    
    /* Установка фильтра расширений файлов. */
    public void setFileFilter(String name, String extension) {
        this.addChoosableFileFilter(new FileNameExtensionFilter(name, extension));
    }
    
    /* Установка начального каталога. */
    public void setDirectory(File directory) {
        this.setCurrentDirectory(directory);
    }
    
    /* Уставока разрешения на выбор файла произвольного расширения. */
    public void enableAllFilesFilter() {
        this.setAcceptAllFileFilterUsed(true);
    }
    
    @Override
    protected JDialog createDialog(Component parent) {
        JDialog dialog = super.createDialog(parent);
        dialog.setResizable(false);
        return dialog;
    }
    
    @Override
    public void approveSelection() {
        File f = getSelectedFile();
        if (!f.exists()) {
            JOptionPane.showMessageDialog(this, "The file does not exist.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        super.approveSelection();
    }
    
}
