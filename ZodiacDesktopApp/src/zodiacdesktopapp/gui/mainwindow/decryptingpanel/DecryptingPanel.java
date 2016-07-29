package zodiacdesktopapp.gui.mainwindow.decryptingpanel;

import javax.swing.JPanel;
import zodiacdesktopapp.model.Model;
import zodiacdesktopapp.model.SymbModel;

/**
 *
 * @author Dmitry Akishin
 */
public class DecryptingPanel extends JPanel implements SymbolsPanel.OnTextChangedListener {
    /*
        Панель расшифровки, содержит панель управления символами и панель с изображением.
    */
    
    private SymbolsPanel symbolsPanel;
    private DecryptedImgPanel imgPanel;

    private Model model;

    public DecryptingPanel() {
        super();
    }

    /* Установка дочерних панелей. */
    public void setChildPanels(SymbolsPanel symbolsPanel,
            DecryptedImgPanel decryptedImgPanel) {
        this.symbolsPanel = symbolsPanel;
        this.imgPanel = decryptedImgPanel;

        this.symbolsPanel.setListener(this);
    }

    public void setModel(Model model) {
        this.model = model;
    }

    /* Обновление дочерних панелей по модели. */
    public void update() {
        if (model == null) {
            symbolsPanel.clearAll();
            imgPanel.clearAll();
        } else {
            symbolsPanel.setSymbols(model.getSymbols());
            imgPanel.setModel(model);
            imgPanel.setListener(symbolsPanel);
        }
    }

    /* Реакция на изменение буквы для символа на панели символов. */
    @Override
    public void action(SymbModel.SymbInfo symbol, String text) {
        if (text != null) {
            if (text.length() == 0) {
                imgPanel.clearSymb(symbol);
            } else {
                imgPanel.replaceSymb(symbol, text.charAt(0));
            }
        }
    }
}
