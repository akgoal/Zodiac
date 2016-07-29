package zodiacdesktopapp.gui.mainwindow.decryptingpanel;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import zodiacdesktopapp.model.SymbModel;
import zodiacdesktopapp.model.binding.BindingSaveLoadHelper;

/**
 *
 * @author Dmitry Akishin
 */
public class SymbolsPanel extends JPanel implements DecryptedImgPanel.OnImageClickedListener {
    /*
        Панель символов. Управляет привязкой символов и букв, их заменяющих.
    */
    
    /* Слушатель изменения буквы для символа. */
    private OnTextChangedListener listener;

    /* Карта полей ввода с идентификатором символа в качестве ключа. */
    private Map<Integer, JTextField> fields = new HashMap<>();

    /* Запомненная привязка букв к символам. */
    private Map<Integer, Character> rememberedBinding;

    /* Помощник для сохранения и загрузки привязки. */
    private BindingSaveLoadHelper saveLoadHelper = new BindingSaveLoadHelper();

    public interface OnTextChangedListener {
        void action(SymbModel.SymbInfo symbol, String text);
    }

    public void setListener(OnTextChangedListener listener) {
        this.listener = listener;
    }

    /* Установка панелей конкретных символов. */
    public void setSymbols(ArrayList<SymbModel.SymbInfo> symbols) {
        this.removeAll();
        fields.clear();

        this.setLayout(new GridLayout(0, 2, 10, 10));
        for (SymbModel.SymbInfo s : symbols) {
            this.add(new SymbPanel(s));
        }
        revalidate();
    }
    
    /* Очистка информации о символах. */
    public void clearAll(){
        this.removeAll();
        fields.clear();
    }

    /* Реакция на клик по символу на панели изображения.
        Сопровождается запрашиванием фокуса соответствующим символу полем. */
    @Override
    public void action(int symbId) {
        JTextField tf = fields.get(symbId);
        if (tf != null) {
            if (this.getParent() instanceof JViewport) {
                Rectangle bounds = tf.getParent().getBounds();
                ((JViewport) this.getParent()).setViewPosition(new Point((int) bounds.getX(), (int) bounds.getY()));
            }
            tf.requestFocus();
            tf.select(0, 1);
        }
    }

    /* Сохранение привязки в файл.
        При этом указывается информация о файле разметки, в соответствие 
        с которой привязка была создана. */
    public void saveToFile(File file, String markupFile) {
        Map<Integer, Character> binding = new HashMap<>();
        Character chr;
        String text;
        for (Integer i : fields.keySet()) {
            text = fields.get(i).getText();
            if (text != null) {
                if (text.length() > 0) {
                    binding.put(i, text.charAt(0));
                }
            }
        }

        BindingSaveLoadHelper.SaveUnit saveUnit = new BindingSaveLoadHelper.SaveUnit(binding, markupFile);
        saveLoadHelper.saveToFile(file, saveUnit);
    }

    /* Загрузка привязки из файла.
        Если разметка, указанная в файле, не совпадает с текущей, выводится предупреждение. */
    public void loadFromFile(File file, String markupFile, Component parentForWarningDialog) {
        BindingSaveLoadHelper.SaveUnit saveUnit = saveLoadHelper.loadFromFile(file);
        Map<Integer, Character> binding = saveUnit.getBinding();
        String loadedMarkupFile = saveUnit.getMarkupFile();

        if (!markupFile.equals(loadedMarkupFile)) {
            Component comp = parentForWarningDialog;
            if (comp == null) {
                comp = this;
            }
            int res = JOptionPane.showConfirmDialog(comp,
                    "The binding file was propaply created for another markup. Continue anyway?",
                    "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            switch (res) {
                case JOptionPane.YES_OPTION:
                    break;
                case JOptionPane.NO_OPTION:
                    return;
                case JOptionPane.CLOSED_OPTION:
                    return;
                default:
                    return;
            }
        }

        setBinding(binding);
    }

    /* Установка привязки в поля символов. */
    public void setBinding(Map<Integer, Character> binding) {
        if (binding != null) {
            for (Integer i : fields.keySet()) {
                fields.get(i).setText("");
                if (binding.containsKey(i)) {
                    fields.get(i).setText(binding.get(i).toString());
                }
            }
        }
    }

    /* Получение привязки из текущего состояния панелей символов. */
    public Map<Integer, Character> getBinding() {
        Map<Integer, Character> res = new HashMap<>();
        String s;
        for (Integer i : fields.keySet()) {
            s = fields.get(i).getText();
            if (s != null && s.length() > 0) {
                res.put(i, s.charAt(0));
            }
        }
        return res;
    }

    /* Стирание запомненной разметки. */
    public void clearRememberedBinding() {
        rememberedBinding = null;
    }

    /* Запомнить текущую разметку. */
    public void rememberBinding() {
        rememberedBinding = new HashMap<>();
        String s;
        for (Integer i : fields.keySet()) {
            s = fields.get(i).getText();
            if (s != null && s.length() > 0) {
                rememberedBinding.put(i, s.charAt(0));
            }
        }
    }

    /* Возврат к запомненной разметке. */
    public void retrieveBinding() {
        setBinding(rememberedBinding);
    }

    /* Панель отдельного символа. Содержит изображение символа и поля для ввода буквы. */
    private class SymbPanel extends JPanel {

        /* При создании поля ввода переопределяется стандартный Document поля
            для ограничения длины вводимой информации одной буквой. */
        SymbPanel(SymbModel.SymbInfo symb) {
            super();
            this.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
            JLabel icon = new JLabel();
            icon.setIcon(new ImageIcon(symb.getImage()));
            JTextField symbField = new JTextField(2);
            symbField.setFont(new Font(null, Font.PLAIN, 22));
            symbField.setHorizontalAlignment(JTextField.CENTER);
            symbField.setDocument(new PlainDocument() {
                @Override
                public void insertString(int offset, String str, AttributeSet attr) throws BadLocationException {
                    if (str == null) {
                        return;
                    }
                    if ((getLength() + str.length()) <= 1) {
                        super.insertString(offset, str, attr);
                    }
                }
            });
            symbField.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    actionOnTextChanged();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    actionOnTextChanged();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                }

                private void actionOnTextChanged() {
                    listener.action(symb, symbField.getText());
                }
            });
            this.add(icon);
            this.add(symbField);

            fields.put(symb.getId(), symbField);
        }

    }
}
