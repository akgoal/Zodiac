package zodiacdesktopapp.model;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import zodiacdesktopapp.model.marking.MarkUpManager;

/**
 *
 * @author Dmitry Akishin
 */
public class Model {
    /*
        Модель размеченного изображения.
    */
    
    /* Размеченное изображение */
    private BufferedImage image;

    /* Параметры решетки, накладываемой на исходное изображение. */
    private int offsetX, offsetY,
            cellWidth, cellHeight,
            gridWidth, gridHeight;

    /* Массив блоков, каждый содержит изображение символа и его id. */
    private Block[][] blocks;

    /* Модель символов */
    private SymbModel symbModel = new SymbModel();

    /* Помощник для поиска. */
    private SearchHelper searchHelper;

    public Model(BufferedImage image) {
        this.image = image;
    }

    private Model(BufferedImage image, int offsetX, int offsetY,
            int cellWidth, int cellHeight, int gridWidth, int gridHeight,
            Block[][] blocks, SymbModel symbModel) {
        this.image = image;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.cellWidth = cellWidth;
        this.cellHeight = cellHeight;
        this.gridWidth = gridWidth;
        this.gridHeight = gridHeight;
        this.blocks = blocks;
        this.symbModel = symbModel;
    }

    /* Копирование модели. */
    public Model cloneModel() {
        return new Model(image, offsetX, offsetY,
                cellWidth, cellHeight, gridWidth, gridHeight,
                blocks, symbModel);
    }

    /*
        Информация о частоте символов.
        Возвращается в порядке убывания.
     */
    public ArrayList<StatUnit> getStatistics() {
        ArrayList<StatUnit> res = new ArrayList<>();
        StatUnit stat;
        int i;
        for (SymbModel.SymbInfo s : symbModel.getSymbInfos()) {
            stat = new StatUnit(s.getImage(), s.getLocations().size());
            for (i = 0; i < res.size(); i++) {
                if (stat.getNumber() > res.get(i).getNumber()) {
                    res.add(i, stat);
                    break;
                }
            }
            if (i == res.size()) {
                res.add(stat);
            }
        }
        return res;
    }

    /* Получение статистики в виде изображения. */
    public BufferedImage getStatisticsAsImage() {
        ArrayList<StatUnit> stats = getStatistics();
        int x = 15, y = 15;
        int iw = 50, ih = 50, textw = 100;
        int marg = 5;
        int fsize = 24;
        BufferedImage res = new BufferedImage(x + iw + textw + x, y + stats.size() * (ih + marg) + y,
                BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = res.createGraphics();
        g2d.setColor(Color.white);
        g2d.fillRect(0, 0, res.getWidth(), res.getHeight());
        g2d.setFont(new Font(null, Font.BOLD, fsize));
        for (StatUnit stat : stats) {
            g2d.drawImage(stat.getImage(), x, y, iw, ih, Color.white, null);
            g2d.setColor(Color.black);
            g2d.drawString(" - " + stat.getNumber(), x + iw, y + ih / 2 + 5);
            g2d.setColor(Color.white);
            y += ih + marg;
        }
        g2d.dispose();

        return res;
    }

    /* Получение информации и символах. */
    public ArrayList<SymbModel.SymbInfo> getSymbols() {
        return symbModel.getSymbInfos();
    }

    /* Получение id символа по координатам в массиве блоков. */
    public int getSymbIdByCoord(int x, int y) {
        if (x < 0 || y < 0) {
            return -1;
        }
        int cellX = x / cellWidth;
        int cellY = y / cellHeight;
        if (cellX >= gridWidth || cellY >= gridHeight) {
            return -1;
        }
        Block b = blocks[cellX][cellY];
        return b.getImgId();
    }

    /*
        Установка модели из файла. 
        При отсутствии ошибок возвращает true
     */
    public boolean setModel(String file) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            return setModel(reader);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Model.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(Model.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /* Установка модели из потока InputStream.
        Необходимо для загрузки модели из внутреннего ресурса. */
    public boolean setModel(InputStream stream) {
        InputStreamReader isr = new InputStreamReader(stream);
        BufferedReader reader = new BufferedReader(isr);
        boolean res = setModel(reader);
        try {
            reader.close();
            isr.close();
            stream.close();
        } catch (IOException ex) {
            Logger.getLogger(Model.class.getName()).log(Level.SEVERE, null, ex);
        }
        return res;
    }

    /* Установка и настройка модели. */
    private boolean setModel(BufferedReader reader) {
        try {
            readInfoFromFile(reader);
            searchHelper = new SearchHelper(blocks);
            createImage();
            return true;

        } catch (Exception ex) {
            Logger.getLogger(Model.class
                    .getName()).log(Level.SEVERE, null, ex);
            return false;
        } finally {
            try {
                if (reader != null) {
                    reader.close();

                }
            } catch (IOException ex) {
                Logger.getLogger(Model.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /* Чтение информации из файла. */
    private void readInfoFromFile(BufferedReader br) throws Exception {
        String line = readNonComm(br);
        String[] gridParamsString = line.split(MarkUpManager.GRID_PARAMS_DELIMITER);

        offsetX = Integer.parseInt(gridParamsString[0]);
        offsetY = Integer.parseInt(gridParamsString[1]);
        cellWidth = Integer.parseInt(gridParamsString[2]);
        cellHeight = Integer.parseInt(gridParamsString[3]);
        gridWidth = Integer.parseInt(gridParamsString[4]);
        gridHeight = Integer.parseInt(gridParamsString[5]);

        line = readNonComm(br);
        String[] markedPositions = line.split(MarkUpManager.POSITIONS_DELIMITER);
        if (markedPositions.length != gridWidth * gridHeight) {
            throw new IOException("The data is not correct.");
        }

        blocks = new Block[gridWidth][gridHeight];
        Block block;
        int pos, posX, posY;
        int i = 0;
        for (int y = 0; y < gridHeight; y++) {
            for (int x = 0; x < gridWidth; x++, i++) {
                pos = Integer.parseInt(markedPositions[i]);
                block = new Block();
                block.setImgId(pos);
                posY = pos / gridWidth;
                posX = pos - posY * gridWidth;
                block.setImage(image.getSubimage(offsetX + posX * cellWidth,
                        offsetY + posY * cellHeight,
                        cellWidth, cellHeight));
                updateSymbModel(block, x, y);
                blocks[x][y] = block;
            }
        }
    }

    /* Обновление модели символов. */
    private void updateSymbModel(Block block, int x, int y) {
        symbModel.update(block, x, y);
    }

    /* Чтение следующей строки, не являющейся комментарием. */
    private String readNonComm(BufferedReader br) throws IOException {
        String line = br.readLine();
        if (line == null) {
            return null;
        }
        if (!line.equals(MarkUpManager.COMMENT_BEGIN)) {
            return line;
        } else {
            line = br.readLine();
            if (line == null) {
                return null;
            }
            while (!line.equals(MarkUpManager.COMMENT_END)) {
                line = br.readLine();
                if (line == null) {
                    return null;
                }
            }
            return br.readLine();
        }
    }

    /* Создание изображения модели. */
    private void createImage() {
        image = new BufferedImage(gridWidth * cellWidth, gridHeight * cellHeight,
                BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        for (int y = 0; y < gridHeight; y++) {
            for (int x = 0; x < gridWidth; x++) {
                g2d.drawImage(blocks[x][y].getImage(), x * cellWidth, y * cellHeight,
                        cellWidth, cellHeight, Color.gray, null);
            }
        }
        g2d.dispose();
    }

    /* Поиск слов words в модели.
        Производится поиск возможных привязок букв к символам, которые бы 
        обеспечили наличие данных слов. 
        initBinding - начальная разметка,
        isHomophonic - разрешается ли использовать одну букву для разных символов, 
        searchLeft, searchUp, searchRight, searchDown - определяют
        направление поиска, listener - слушатель прогресса поиска. */
    public ArrayList<Map<Integer, Character>> searchWords(String[] words,
            Map<Integer, Character> initBinding, boolean isHomophonic,
            boolean searchLeft, boolean searchUp, boolean searchRight,
            boolean searchDown, SearchProgressListener listener) {
        if (searchHelper != null) {
            ArrayList<SearchHelper.Direction> directions = new ArrayList<>();
            if (searchLeft) {
                directions.add(SearchHelper.Direction.LEFT);
            }
            if (searchUp) {
                directions.add(SearchHelper.Direction.UP);
            }
            if (searchRight) {
                directions.add(SearchHelper.Direction.RIGHT);
            }
            if (searchDown) {
                directions.add(SearchHelper.Direction.DOWN);
            }
            return searchHelper.search(words, initBinding, isHomophonic, directions,
                    new SearchHelper.ProgressListener() {
                @Override
                public void onProgressChanged(int value) {
                    listener.onProgressChanged(value);
                }
            });
        } else {
            return null;

        }
    }

    /* Интерфейс, определяющий слушателя изменения прогресса поиска. */
    public static interface SearchProgressListener {
        public void onProgressChanged(int progressValue);
    }
    
    /* Получение минимального значения прогресса выполнения поиска. */
    public static int getMinSearchProgress(){
        return SearchHelper.MIN_PROGRESS;
    }
    
    /* Получение максимального значения прогресса выполнения поиска. */
    public static int getMaxSearchProgress(){
        return SearchHelper.MAX_PROGRESS;
    }

    public BufferedImage getImage() {
        return image;
    }

    public void setImage(BufferedImage image) {
        this.image = image;
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
