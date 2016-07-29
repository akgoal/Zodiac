package zodiacdesktopapp.gui.mainwindow.decryptingpanel;

import java.awt.Color;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author Dmitry Akishin
 */
public class ColorChooser extends JColorChooser{
    /*
        Панель выбора цвета. Является усеченной модификацией
        JColorChooser.
    */
    
    private JPanel previewPanel;
    private JLabel previewLabel;
    
    public ColorChooser() {
        super();
        
        for (AbstractColorChooserPanel accp:this.getChooserPanels()){
            if (!accp.getDisplayName().equals("Swatches"))
                this.removeChooserPanel(accp);
        }
        
        JPanel p = (JPanel)this.getChooserPanels()[0].getComponent(0);
        p.remove(2);
        p.remove(1);
        previewPanel = new JPanel();
        previewLabel = new JLabel("Sample Text Sample Text");
        previewPanel.add(previewLabel);
        this.setPreviewPanel(previewPanel);
        
        this.getSelectionModel().addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                previewPanel.setBackground(ColorChooser.this.getColor());
                previewLabel.setForeground(ColorChooser.this.getColor());
            }
        });
    }
    
    /* Установка начального цвета. */
    public void setInitColor(Color initColor) {
        this.setColor(initColor);
        previewPanel.setBackground(initColor);
    }
    
    public Color getChosenColor() {
        return this.getColor();
    }
    
}
