package com.example.zodiac.model;

import java.util.ArrayList;

import android.graphics.Bitmap;

public class SymbModel {
	/*
	 * Модель совпадения символов.
	 */

	/*
	 * Список информаций о символах. Для каждого символа хранится его id и
	 * позиции, в которых этот символ находится.
	 */
	private ArrayList<SymbInfo> mSymbInfos = new ArrayList<SymbInfo>();

	public SymbModel() {
	}

	/*
	 * Добавление в модель информации о блоке, который встретился в позиции x,y.
	 */
	public void update(Block block, int x, int y) {
		for (SymbInfo s : mSymbInfos) {
			if (s.getId() == block.getImgId()) {
				s.addLocation(new Location(x, y));
				return;
			}
		}
		SymbInfo s = new SymbInfo();
		s.setId(block.getImgId());
		s.setImage(block.getImage());
		s.addLocation(new Location(x, y));
		mSymbInfos.add(s);
	}

	public ArrayList<SymbInfo> getSymbInfos() {
		return mSymbInfos;
	}
	
	/* Получение изображения символа по его id. */
	public Bitmap getSymbImageById(int symbId) {
		for (SymbInfo s:mSymbInfos){
			if (s.getId() == symbId)
				return s.getImage();
		}
		return null;
	}
	
	/* Информация о символе. */
	public class SymbInfo {

		/* Изображение символа. */
		private Bitmap mImage;
		/* Идентификатор символа. */
		private int mId;
		/* Список позиций, в которых встречается символ. */
		private ArrayList<Location> mLocations = new ArrayList<Location>();

		public SymbInfo() {
		}

		public void addLocation(Location l) {
			mLocations.add(l);
		}

		public Bitmap getImage() {
			return mImage;
		}

		public void setImage(Bitmap image) {
			this.mImage = image;
		}

		public ArrayList<Location> getLocations() {
			return mLocations;
		}

		public void setLocations(ArrayList<Location> locations) {
			this.mLocations = locations;
		}

		public int getId() {
			return mId;
		}

		public void setId(int id) {
			this.mId = id;
		}

	}

	/*
	 * Позиция, содержит координаты x,y. Координаты считаюся с левого верхнего
	 * угла, x - вправо, y - вниз.
	 */
	public class Location {

		private int mX, mY;

		public Location(int x, int y) {
			this.mX = x;
			this.mY = y;
		}

		public int getX() {
			return mX;
		}

		public void setX(int x) {
			this.mX = x;
		}

		public int getY() {
			return mY;
		}

		public void setY(int y) {
			this.mY = y;
		}

	}
}
