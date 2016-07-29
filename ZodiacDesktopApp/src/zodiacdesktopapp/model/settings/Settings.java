package zodiacdesktopapp.model.settings;

import com.sun.glass.events.KeyEvent;
import java.awt.Color;
import java.awt.Font;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import zodiacdesktopapp.model.resources.ResourceHelper;

/**
 *
 * @author Dmitry Akishin
 */
public class Settings {

    /* Singleton-класс настроек приложения. */

 /* Имя файла настроек. */
    private static final String SETTINGS_FILENAME = "zodiac_settings.zdcs";

    /* Пути к ресурсам изображения и разметки по умолчанию. */
    public static final String DEFAULT_SOURCE_IMAGE_PATH = "res/ZodiacCipher.png";
    public static final String DEFAULT_MARKUP_PATH = "res/markup.zdcm";

    /* Константы для записи в файл. */
    private static final String NO_INFO = "-";
    private static final String COLOR_COMP_DELIMITER = ",";
    private static final String COMMENT_BEGIN = "/*";
    private static final String COMMENT_END = "*/";
    private static final String DELIMITER = "=";

    /* Последный каталог выбора исходного изображения. */
    private File lastDirectoryForSourceImage;
    /* Последный каталог выбора файла разметки. */
    private File lastDirectoryForMarkUpFile;
    /* Последный каталог выбора файла привязки букв к символам. */
    private File lastDirectoryForBindingFile;
    /* Последный каталог для сохранения изображения. */
    private File lastDirectoryForImage;

    /* Последнее исходное изображение. */
    private File lastSourceImage;
    /* Последний файл разметки. */
    private File lastMarkUpFile;

    /* Расширения для экспортируемого изображения, файла
        размети и файла привязки. */
    private final String exportImageExtension = "png";
    private final String markUpFileExtension = "zdcm";
    private final String bindingFileExtension = "zdcb";

    /* Параметры шрифта по умолчанию. */
    private final int defaultFontSize = 22;
    private final Color defaultFontColor = Color.black;

    /* Параметры шрифта. */
    private int fontSize = defaultFontSize;
    private Color fontColor = defaultFontColor;
    private Font font = new Font(null, Font.BOLD, fontSize);

    private final int programInfoKeyCode = KeyEvent.VK_F1;
    private String programInfoText;
    private static final String PROGRAM_INFO_FILE = "res/info.txt";

    private static Settings settings;

    public static Settings getInstance() {
        if (settings == null) {
            settings = new Settings();
        }
        return settings;
    }

    private Settings() {
        adjustFont();
        loadProgramInfo();
    }

    /* Сохранения в файл. */
    public void save() {
        File file = ResourceHelper.loadExtFile(SETTINGS_FILENAME);
        if (file != null) {
            FileWriter fw = null;
            try {
                String[] linesToWrite = {
                    "Directory for source image file" + DELIMITER
                    + ((lastDirectoryForSourceImage != null)
                    ? lastDirectoryForSourceImage.getAbsolutePath() : NO_INFO),
                    "Directory for markup file" + DELIMITER
                    + ((lastDirectoryForMarkUpFile != null)
                    ? lastDirectoryForMarkUpFile.getAbsolutePath() : NO_INFO),
                    "Directory for binding file" + DELIMITER
                    + ((lastDirectoryForBindingFile != null)
                    ? lastDirectoryForBindingFile.getAbsolutePath() : NO_INFO),
                    "Directory for exported image" + DELIMITER
                    + ((lastDirectoryForImage != null)
                    ? lastDirectoryForImage.getAbsolutePath() : NO_INFO),
                    "Font size" + DELIMITER + fontSize,
                    "Font color" + DELIMITER
                    + fontColor.getRed() + COLOR_COMP_DELIMITER + fontColor.getGreen()
                    + COLOR_COMP_DELIMITER + fontColor.getBlue(),
                    "Source image file" + DELIMITER
                    + ((lastSourceImage != null)
                    ? lastSourceImage.getAbsolutePath() : NO_INFO),
                    "Markup file" + DELIMITER
                    + ((lastMarkUpFile != null)
                    ? lastMarkUpFile.getAbsolutePath() : NO_INFO),};

                fw = new FileWriter(file);
                fw.write(COMMENT_BEGIN + System.lineSeparator());
                fw.write("  Personal settings for Zodiac application" + System.lineSeparator());
                fw.write(COMMENT_END + System.lineSeparator());
                for (String l : linesToWrite) {
                    fw.write(l + System.lineSeparator());
                }
            } catch (IOException ex) {
                Logger.getLogger(Settings.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    if (fw != null) {
                        fw.close();
                    }
                } catch (IOException ex) {
                    Logger.getLogger(Settings.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    /* Загрузка из файла. */
    public void load() {
        File file = ResourceHelper.loadExtFile(SETTINGS_FILENAME);
        if (file != null && file.exists()) {
            BufferedReader br = null;
            try {
                String[] readLines = new String[8];

                br = new BufferedReader(new FileReader(file));
                String line = readNonComm(br);
                String[] split;
                for (int i = 0; i < readLines.length; i++) {
                    if (line == null) {
                        for (int j = i; j < readLines.length; j++) {
                            readLines[j] = NO_INFO;
                        }
                        break;
                    }
                    split = line.split(DELIMITER);
                    if (split.length < 2) {
                        readLines[i] = NO_INFO;
                    } else {
                        readLines[i] = split[1];
                    }
                    line = br.readLine();
                }
                int i = 0;
                if (!readLines[i].equals(NO_INFO)) {
                    lastDirectoryForSourceImage = new File(readLines[i]);
                }
                i++;
                if (!readLines[i].equals(NO_INFO)) {
                    lastDirectoryForMarkUpFile = new File(readLines[i]);
                }
                i++;
                if (!readLines[i].equals(NO_INFO)) {
                    lastDirectoryForBindingFile = new File(readLines[i]);
                }
                i++;
                if (!readLines[i].equals(NO_INFO)) {
                    lastDirectoryForImage = new File(readLines[i]);
                }
                i++;
                if (!readLines[i].equals(NO_INFO)) {
                    try {
                        fontSize = Integer.parseInt(readLines[i]);
                    } catch (Exception e) {
                        Logger.getLogger(Settings.class.getName()).log(Level.WARNING, null, e);
                        fontSize = defaultFontSize;
                    }
                }
                i++;
                if (!readLines[i].equals(NO_INFO)) {
                    try {
                        String[] components = readLines[i].split(COLOR_COMP_DELIMITER);
                        fontColor = new Color(Integer.parseInt(components[0]),
                                Integer.parseInt(components[1]), Integer.parseInt(components[2]));
                    } catch (Exception e) {
                        Logger.getLogger(Settings.class.getName()).log(Level.WARNING, null, e);
                        fontColor = defaultFontColor;
                    }
                }
                i++;
                if (!readLines[i].equals(NO_INFO)) {
                    lastSourceImage = new File(readLines[i]);
                }
                i++;
                if (!readLines[i].equals(NO_INFO)) {
                    lastMarkUpFile = new File(readLines[i]);
                }

            } catch (FileNotFoundException ex) {
                Logger.getLogger(Settings.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(Settings.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    if (br != null) {
                        br.close();
                    }
                } catch (IOException ex) {
                    Logger.getLogger(Settings.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    /* Чтение следующей строки, не являющейся комментарием. */
    private String readNonComm(BufferedReader br) throws IOException {
        String line = br.readLine();
        if (line == null) {
            return null;
        }
        if (!line.equals(COMMENT_BEGIN)) {
            return line;
        } else {
            line = br.readLine();
            if (line == null) {
                return null;
            }
            while (!line.equals(COMMENT_END)) {
                line = br.readLine();
                if (line == null) {
                    return null;
                }
            }
            return br.readLine();
        }
    }

    /* Поправка объекта класса шрифта. */
    private void adjustFont() {
        font = new Font(null, Font.BOLD, fontSize);
    }

    private void loadProgramInfo() {
        programInfoText = ResourceHelper.loadFileAsString(PROGRAM_INFO_FILE);
    }

    public File getLastDirectoryForSourceImage() {
        return lastDirectoryForSourceImage;
    }

    public void setLastDirectoryForSourceImage(File lastDirectoryForSourceImage) {
        this.lastDirectoryForSourceImage = lastDirectoryForSourceImage;
    }

    public File getLastDirectoryForMarkUpFile() {
        return lastDirectoryForMarkUpFile;
    }

    public void setLastDirectoryForMarkUpFile(File lastDirectoryForMarkingFile) {
        this.lastDirectoryForMarkUpFile = lastDirectoryForMarkingFile;
    }

    public File getLastDirectoryForBindingFile() {
        return lastDirectoryForBindingFile;
    }

    public void setLastDirectoryForBindingFile(File lastDirectoryForBindingFile) {
        this.lastDirectoryForBindingFile = lastDirectoryForBindingFile;
    }

    public File getLastDirectoryForImage() {
        return lastDirectoryForImage;
    }

    public void setLastDirectoryForImage(File lastDirectoryForImage) {
        this.lastDirectoryForImage = lastDirectoryForImage;
    }

    public int getFontSize() {
        return fontSize;
    }

    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
        adjustFont();
    }

    public Color getFontColor() {
        return fontColor;
    }

    public void setFontColor(Color fontColor) {
        this.fontColor = fontColor;
    }

    public Font getFont() {
        return font;
    }

    public String getExportImageExtension() {
        return exportImageExtension;
    }

    public String getMarkUpFileExtension() {
        return markUpFileExtension;
    }

    public String getBindingFileExtension() {
        return bindingFileExtension;
    }

    public File getLastSourceImage() {
        return lastSourceImage;
    }

    public void setLastSourceImage(File lastSourceImage) {
        this.lastSourceImage = lastSourceImage;
    }

    public File getLastMarkUpFile() {
        return lastMarkUpFile;
    }

    public void setLastMarkUpFile(File lastMarkingFile) {
        this.lastMarkUpFile = lastMarkingFile;
    }

    public int getProgramInfoKeyCode() {
        return programInfoKeyCode;
    }

    public String getProgramInfoText() {
        return programInfoText;
    }
}
