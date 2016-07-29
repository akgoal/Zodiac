package zodiacdesktopapp.model.marking;

import java.awt.image.BufferedImage;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Dmitry Akishin
 */
public class MarkUpManager {

    /*
        Класс для хранения данных о разметке изображения, а также по
        управлению разметкой.
        Разметка заключается в выборе одинаковых символов и последующая замена
        этих символов первым символов из последовательности.
     */
    
    /* Константы для записи в файл. */
    public static final String COMMENT_BEGIN = "/*";
    public static final String COMMENT_END = "*/";
    public static final String GRID_PARAMS_DELIMITER = ",";
    public static final String POSITIONS_DELIMITER = "-";

    /* Размеченное изображение. */
    private BufferedImage img;

    /* Параметры решетки, накладываемой на изображение. */
    private int offsetX, offsetY;
    private int cellWidth, cellHeight;
    private int gridWidth, gridHeight;

    /* Текущая последовательность позиций в сетке */
    private ArrayList<Position> currSequence = new ArrayList<>();

    /* Список всех последовательностей */
    private ArrayList<ArrayList<Position>> sequences = new ArrayList<>();

    /* порядковый номер текущей последовательности 
        в списке всех последовательностей */
    private int currSeqPos = 0;

    /* Позиция, содержащая координаты x,y. */
    public class Position {

        public int x, y;

        Position(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    public MarkUpManager(BufferedImage img) {
        this.img = img;
        sequences.add(currSequence);
    }

    /* Добавление переданных координат
        в текущую серию. возвращает true, если добавление прошло успешно. */
    public boolean addCoordToSequence(int x, int y) {
        if (x < 0 || x > cellWidth * gridWidth || y < 0 || y > cellHeight * gridHeight) {
            return false;
        }
        int posX = x / cellWidth;
        int posY = y / cellHeight;
        if (posX >= gridWidth || posY >= gridHeight) {
            return false;
        }
        Position pos = new Position(posX, posY);
        addPosToSeq(pos);
        return true;
    }

    /*
        Добавление позиции в текущую последовательность,
        если ее там еще нет. Если она есть, позиция удаляется.
        Также проверяется отсутствие позиции во всех последовательностях,
        чтобы позиция не оказалась в нескольких последовательностях.
     */
    private void addPosToSeq(Position pos) {
        for (ArrayList<Position> seq : sequences) {
            if (seq != currSequence) {
                for (Position posInSeq : seq) {
                    if (posInSeq.x == pos.x && posInSeq.y == pos.y) {
                        return;
                    }
                }
            }
        }
        for (Position posInSeq : currSequence) {
            if (posInSeq.x == pos.x && posInSeq.y == pos.y) {
                currSequence.remove(posInSeq);
                return;
            }
        }
        currSequence.add(pos);
    }

    /* Завершение текущей последовательности. */
    public void goToNextSequence() {
        if (currSeqPos < sequences.size() - 1) {
            if (currSequence.isEmpty()) {
                sequences.remove(currSequence);
                currSequence = sequences.get(currSeqPos);
            } else {
                currSeqPos++;
                currSequence = sequences.get(currSeqPos);
            }
        } else if (!currSequence.isEmpty()) {
            currSequence = new ArrayList<>();
            sequences.add(currSequence);
            currSeqPos++;
        }
    }

    /* Возврат к предыдущей последовательности. */
    public void goToPrevSequence() {
        if (currSeqPos > 0) {
            if (currSequence.isEmpty()) {
                sequences.remove(currSequence);
                currSeqPos--;
                currSequence = sequences.get(currSeqPos);
            } else {
                currSeqPos--;
                currSequence = sequences.get(currSeqPos);
            }
        }
    }

    /* Проверяет, является ли текущая последовательность первой. */
    public boolean isCurrSeqFirst() {
        return currSeqPos == 0;
    }

    /* Очистка всей информации. */
    public void clear() {
        currSequence.clear();
        sequences.clear();
    }

    /* Сохранение в файл. */
    public void saveToFile(String path) {
        if (currSequence.isEmpty()) {
            sequences.remove(currSequence);
            if (currSeqPos > sequences.size() - 1) {
                currSeqPos = sequences.size() - 1;
            }
        }
        FileWriter fw = null;
        try {
            fw = new FileWriter(path);
            writeInfoToFile(fw);
        } catch (IOException ex) {
            Logger.getLogger(MarkUpManager.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (fw != null) {
                    fw.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(MarkUpManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /* Запись информации о разметке в файл. */
    private void writeInfoToFile(FileWriter fw) throws IOException {
        fw.write(COMMENT_BEGIN + System.lineSeparator());
        fw.write("      Zodiac application file " + System.lineSeparator());
        fw.write("  DO NOT MODIFY!" + System.lineSeparator());
        fw.write("  MarkUp info, created: " + new Date() + System.lineSeparator());
        fw.write("  offsetX,offsetY,cellWidth,cellHeight,gridWidth,gridHeight" + System.lineSeparator());
        fw.write(COMMENT_END + System.lineSeparator());
        fw.write(offsetX + GRID_PARAMS_DELIMITER + offsetY + GRID_PARAMS_DELIMITER
                + cellWidth + GRID_PARAMS_DELIMITER + cellHeight + GRID_PARAMS_DELIMITER
                + gridWidth + GRID_PARAMS_DELIMITER + gridHeight + System.lineSeparator());

        fw.write(COMMENT_BEGIN + System.lineSeparator());
        fw.write("  positions in grid as (y*gridWidth+x)" + System.lineSeparator());
        fw.write(COMMENT_END + System.lineSeparator());
        //записываем инфо о каждой позиции в сетке
        for (int y = 0; y < gridHeight; y++) {
            for (int x = 0; x < gridWidth; x++) {
                fw.write(getHeadPositionCode(x, y) + POSITIONS_DELIMITER);
            }
        }
    }

    /* Код главной позиции в последовательности.
        Код вычисляется как порядковый номер. */
    private int getHeadPositionCode(int x, int y) {
        for (ArrayList<Position> seq : sequences) {
            for (Position pos : seq) {
                if (pos.x == x && pos.y == y) {
                    return seq.get(0).x + gridWidth * seq.get(0).y;
                }
            }
        }
        return y * gridWidth + x;
    }

    public ArrayList<Position> getCurrentSequence() {
        return currSequence;
    }

    public ArrayList<ArrayList<Position>> getSequences() {
        return sequences;
    }

    public BufferedImage getImg() {
        return img;
    }

    public void setImg(BufferedImage img) {
        this.img = img;
    }

    /* Установка параметров решетки. */
    public void setGridParams(int[] gridParams) {
        offsetX = gridParams[0];
        offsetY = gridParams[1];
        cellWidth = gridParams[2];
        cellHeight = gridParams[3];
        gridWidth = gridParams[4];
        gridHeight = gridParams[5];
    }

    public int getOffsetX() {
        return offsetX;
    }

    public void setOffsetX(int offsetX) {
        this.offsetX = offsetX;
    }

    public int getOffsetY() {
        return offsetY;
    }

    public void setOffsetY(int offsetY) {
        this.offsetY = offsetY;
    }

    public int getCellWidth() {
        return cellWidth;
    }

    public void setCellWidth(int cellWidth) {
        this.cellWidth = cellWidth;
    }

    public int getCellHeight() {
        return cellHeight;
    }

    public void setCellHeight(int cellHeight) {
        this.cellHeight = cellHeight;
    }

    public int getGridWidth() {
        return gridWidth;
    }

    public void setGridWidth(int gridWidth) {
        this.gridWidth = gridWidth;
    }

    public int getGridHeight() {
        return gridHeight;
    }

    public void setGridHeight(int gridHeight) {
        this.gridHeight = gridHeight;
    }

}
