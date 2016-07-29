package zodiacdesktopapp.model.resources;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

/**
 *
 * @author Dmitry Akishin
 */
public class ResourceHelper {
    /*
        Помощник для работы с ресурсами.
    */
    
    /* Загрузка ресурса изображения. */
    public static BufferedImage loadImage(String filepath) {
        try {
            ClassLoader cl = ResourceHelper.class.getClassLoader();
            URL url = cl.getResource(filepath);
            if (url != null) {
                return ImageIO.read(url);
            }
        } catch (IOException ex) {
            Logger.getLogger(ResourceHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /* Загрузка ресурса иконки. */
    public static ImageIcon loadImageIcon(String filepath) {
        ClassLoader cl = ResourceHelper.class.getClassLoader();
        return new ImageIcon(cl.getResource(filepath));
    }

    /* Загрузка ресурса как объекта InputStream. */
    public static InputStream loadFile(String filepath) {
        ClassLoader cl = ResourceHelper.class.getClassLoader();
        return cl.getResourceAsStream(filepath);
    }
    
    /* Загрузка ресурса как строки. */
    public static String loadFileAsString(String filepath) {
        InputStream is = loadFile(filepath);
        Scanner s = new Scanner(is).useDelimiter("\\A");
        String res = s.hasNext() ? s.next() : "";
        try {
            is.close();
        } catch (IOException ex) {
            Logger.getLogger(ResourceHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return res;
    }

    /* Загрузка внешнего файла, расположенного относительно jar-файла. */
    public static File loadExtFile(String filepath) {
        return new File(getJarDirectory().getAbsolutePath() + "/" + filepath);
    }

    /* Получение абсолютного пути к jar-файлу. */
    public static String getJarDirectoryPath() {
        return getJarDirectory().getAbsolutePath();
    }

    /* Получение каталога с jar-файлом. */
    private static File getJarDirectory() {
        URL url;
        String extUrl;
        try {
            url = ResourceHelper.class.getProtectionDomain().getCodeSource().getLocation();
        } catch (SecurityException ex) {
            url = ResourceHelper.class.getResource(ResourceHelper.class.getSimpleName() + ".class");
        }
        extUrl = url.toExternalForm();
        if (extUrl.endsWith(".jar")) {
            extUrl = extUrl.substring(0, extUrl.lastIndexOf("/"));
        } else if (extUrl.endsWith(".exe")) {
            extUrl = extUrl.substring(0, extUrl.lastIndexOf("/"));
        } else {
            String suffix = "/" + (ResourceHelper.class.getName()).replace(".", "/") + ".class";
            extUrl = extUrl.replace(suffix, "");
            if (extUrl.startsWith("jar:") && extUrl.endsWith(".jar!")) {
                extUrl = extUrl.substring(4, extUrl.lastIndexOf("/"));
            }
        }
        try {
            url = new URL(extUrl);
        } catch (MalformedURLException ex) {
        }
        File file;
        try {
            file = new File(url.toURI());
        } catch (URISyntaxException ex) {
            file = new File(url.getPath());
        }
        return file;
    }
}
