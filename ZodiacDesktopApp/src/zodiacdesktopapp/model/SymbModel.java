package zodiacdesktopapp.model;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

/**
 *
 * @author Dmitry Akishin
 */
public class SymbModel {

    /*
        Модель совпадения символов.
     */
    
    /* Список информаций о символах.
        Для каждого символа хранится его id и позиции, в которых этот
        символ находится. */
    private ArrayList<SymbInfo> symbInfos = new ArrayList<>();

    public SymbModel() {
    }

    /* Добавление в модель информации о блоке, который встретился в
        позиции x,y. */
    public void update(Block block, int x, int y) {
        for (SymbInfo s:symbInfos){
            if (s.getId()==block.getImgId()) {
                s.addLocation(new Location(x,y));
                return;
            }
        }
        SymbInfo s = new SymbInfo();
        s.setId(block.getImgId());
        s.setImage(block.getImage());
        s.addLocation(new Location(x,y));
        symbInfos.add(s);
    }

    public ArrayList<SymbInfo> getSymbInfos() {
        return symbInfos;
    }

    /* Информация о символе. */
    public class SymbInfo {

        /* Изображение символа. */
        private BufferedImage image;
        /* Идентификатор символа. */
        private int id;
        /* Список позиций, в которых встречается символ. */
        private ArrayList<Location> locations = new ArrayList<>();

        public SymbInfo() {
        }

        public void addLocation(Location l){
            locations.add(l);
        }
        
        public BufferedImage getImage() {
            return image;
        }

        public void setImage(BufferedImage image) {
            this.image = image;
        }

        public ArrayList<Location> getLocations() {
            return locations;
        }

        public void setLocations(ArrayList<Location> locations) {
            this.locations = locations;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

    }

    /* Позиция, содержит координаты x,y.
        Координаты считаюся с левого верхнего угла,
        x - вправо, y - вниз. */
    public class Location {

        private int x, y;

        public Location(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public int getX() {
            return x;
        }

        public void setX(int x) {
            this.x = x;
        }

        public int getY() {
            return y;
        }

        public void setY(int y) {
            this.y = y;
        }
        
        
    }

}
