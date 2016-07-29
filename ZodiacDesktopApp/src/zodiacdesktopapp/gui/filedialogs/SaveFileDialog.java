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
public class SaveFileDialog extends JFileChooser {
    /*
        Диалог сохранения файла.
    */
    
    /* Расширение сохраняемого файла. */
    private String extension;

    public SaveFileDialog(String title) {
        super();
        this.setDialogTitle(title);
        this.setAcceptAllFileFilterUsed(false);
    }

    /* Установка фильтра файлов по расширению. */
    public void setFileFilter(String name, String extension) {
        this.extension = extension;
        this.addChoosableFileFilter(new FileNameExtensionFilter(name, extension));
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
        if (f.exists()) {
            int res = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to override existing file?",
                    "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            switch (res) {
                case JOptionPane.YES_OPTION:
                    super.approveSelection();
                    return;
                case JOptionPane.NO_OPTION:
                    return;
                case JOptionPane.CLOSED_OPTION:
                    return;
                default:
                    return;
            }
        }
        super.approveSelection();
    }

    /* До сохранения файлу приписывается расширение, если его нет. */
    @Override
    public File getSelectedFile() {
        if (super.getSelectedFile() != null) {
            String filename = super.getSelectedFile().toString();
            if (extension != null) {
                if (!filename.endsWith(("." + extension))) {
                    filename += ("." + extension);
                }
            }
            return new File(filename);
        } else {
            return super.getSelectedFile();
        }
    }
}
