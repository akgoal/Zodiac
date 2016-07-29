package zodiacdesktopapp.model.binding;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Dmitry Akishin
 */
public class BindingSaveLoadHelper {
    /*
        Помощник для сохранения и загрузки привязки букв к символам.
    */
    
    private static final String DELIMITER = "=";
    private static final String MARKUP_FILE_PREFIX = "Markup file";
    private static final String COMMENT_BEGIN = "/*";
    private static final String COMMENT_END = "*/";

    /* Класс, определяющий привязку, которую необходимо сохранить.
        Указывается также разметка, в соответствие с которой была создана привязка. */
    public static class SaveUnit {

        private Map<Integer, Character> binding;
        private String markupFile;

        public SaveUnit(Map<Integer, Character> binding, String markupFile) {
            this.binding = binding;
            this.markupFile = markupFile;
        }

        public Map<Integer, Character> getBinding() {
            return binding;
        }

        public void setBinding(Map<Integer, Character> binding) {
            this.binding = binding;
        }

        public String getMarkupFile() {
            return markupFile;
        }

        public void setMarkupFile(String markupFile) {
            this.markupFile = markupFile;
        }
    }

    /* Сохранение в файл. */
    public void saveToFile(File file, SaveUnit saveUnit) {
        FileWriter fw = null;
        try {
            fw = new FileWriter(file);
            writeInfoToFile(fw, saveUnit);
        } catch (IOException ex) {
            Logger.getLogger(BindingSaveLoadHelper.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (fw != null) {
                    fw.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(BindingSaveLoadHelper.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /* Запись информации в файл. */
    private void writeInfoToFile(FileWriter fw, SaveUnit saveUnit) throws IOException {
        fw.write(COMMENT_BEGIN + System.lineSeparator());
        fw.write("      Zodiac application file" + System.lineSeparator());
        fw.write("  Binding info, created: " + new Date() + System.lineSeparator());
        fw.write("  Schema:" + System.lineSeparator());
        fw.write("  markupFilePath" + System.lineSeparator());
        fw.write("  symbolId" + DELIMITER + "char" + System.lineSeparator());
        fw.write(COMMENT_END + System.lineSeparator());

        fw.write(MARKUP_FILE_PREFIX + DELIMITER + saveUnit.getMarkupFile() + System.lineSeparator());
        for (Integer i : saveUnit.getBinding().keySet()) {
            fw.write(i + DELIMITER + saveUnit.getBinding().get(i));
            fw.write(System.lineSeparator());
        }
    }

    /* Загрузка из файла. */
    public SaveUnit loadFromFile(File file) {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));
            return readInfoFromFile(br);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(BindingSaveLoadHelper.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(BindingSaveLoadHelper.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(BindingSaveLoadHelper.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }

    /* Чтение информации из файла. */
    private SaveUnit readInfoFromFile(BufferedReader br) throws IOException {
        String markupFileToLoad = null;
        Map<Integer, Character> bindingToLoad = new HashMap<>();

        String line = readNonComm(br);
        String[] lineInfo;
        if (line != null) {
            if (line.startsWith(MARKUP_FILE_PREFIX)) {
                lineInfo = line.split(DELIMITER);
                if (lineInfo.length > 1) {
                    markupFileToLoad = lineInfo[1];
                    line = readNonComm(br);
                }
            }
        }

        while (line != null) {
            lineInfo = line.split(DELIMITER);
            try {
                bindingToLoad.put(Integer.parseInt(lineInfo[0]), lineInfo[1].charAt(0));
            } catch (Exception ex) {
                Logger.getLogger(BindingSaveLoadHelper.class.getName()).log(Level.WARNING, null, ex);
            }
            line = readNonComm(br);
        }
        return new SaveUnit(bindingToLoad, markupFileToLoad);
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
}
