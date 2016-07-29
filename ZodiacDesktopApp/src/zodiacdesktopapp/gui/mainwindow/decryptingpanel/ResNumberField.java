package zodiacdesktopapp.gui.mainwindow.decryptingpanel;

import java.awt.Font;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

/**
 *
 * @author Dmitry Akishin
 */
public class ResNumberField extends JTextField {
    /*
        Поле, содержащее номер результата поиска.
    */
   
    /* Максимальное и минимальное значения. */
    private int maxNumber = 0;
    private int minNumber = 0;

    /* Текущий номер. */
    private int currNumber = minNumber;

    /* Слушатель изменения номера. */
    private OnNumberChangedListener onNumberChangedListener;

    public interface OnNumberChangedListener {
        void onNumberChangedAction(int number, boolean isOnMin, boolean isOnMax);
    }

    /* При создании поля создается свой объект Document,
        контролирующий изменение номера. */
    public ResNumberField() {
        super(2);
                
        this.setFont(new Font(null, Font.PLAIN, 12));
        this.setHorizontalAlignment(JTextField.CENTER); 
        
        this.setDocument(new PlainDocument() {
            @Override
            public void insertString(int offset, String str, AttributeSet attr) throws BadLocationException {
                if (str == null) {
                    return;
                }
                try {
                    if (Integer.parseInt(str) < 0) {
                        return;
                    }
                } catch (Exception e) {
                    return;
                }
                super.insertString(offset, str, attr);
            }
        });
        this.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                actionOnTextChanged();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                //actionOnTextChanged();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
            }

            private void actionOnTextChanged() {
                try {
                    int i = Integer.parseInt(ResNumberField.this.getText());
                    currNumber = i;
                    fixCurrNum();
                } catch (Exception e) {
                    currNumber = minNumber;
                    updateText();
                }
                if (onNumberChangedListener != null) {
                    onNumberChangedListener.onNumberChangedAction(currNumber,
                            isOnMin(), isOnMax());
                }
            }
        });
        this.setEnabled(false);   
    }

    public void setOnNumberChangedListener(OnNumberChangedListener onNumberChangedListener) {
        this.onNumberChangedListener = onNumberChangedListener;
    }
    
    /* Поправка текущего номера в случае выхода за допустимые границы. */
    private void fixCurrNum() {
        if (currNumber < minNumber) {
            currNumber = minNumber;
            updateText();
        } else if (currNumber > maxNumber) {
            currNumber = maxNumber;
            updateText();
        }
    }

    /* Обновление текста поля. */
    private void updateText() {
        this.setText("" + currNumber);
    }

    /* Установка максимального значения. При этом остальные параметры сбрасываются. */
    public void setMaxNumber(int number) {
        if (number < 0) {
            return;
        }
        maxNumber = number;
        if (number == 0) {
            minNumber = 0;
        } else {
            minNumber = 1;
        }
        currNumber = minNumber;
        updateText();
    }
    
    public boolean isOnMin(){
        return currNumber == minNumber;
    }
    
    public boolean isOnMax(){
        return currNumber == maxNumber;
    }
    
    /* Увеличение номера на единицу. */
    public void increaseNumber(){
        if (!isOnMax()){
            currNumber ++;
            updateText();
        }
    }
    
    /* Уменьшение номера на единицу. */
    public void decreaseNumber(){
        if (!isOnMin()){
            currNumber --;
            updateText();
        }
    }

}
