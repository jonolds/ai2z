import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.util.ArrayList;
import javax.imageio.ImageIO;

class Model {  //max horizontal/vertical screen position. min is 0
	public static final float XMAX = 1199.9999f, YMAX = 600.0f - 0.0001f;
	private Controller controller;
	public byte[] terrain;
	private ArrayList<Sprite> sprites;

	Model(Controller c) { this.controller = c; }

	void initGame() throws Exception {
		BufferedImage bufImg = ImageIO.read(new File("terrain.png"));
		if(bufImg.getWidth() != 60 || bufImg.getHeight() != 60)
			throw new Exception("Expected the terrain image to have dimensions of 60-by-60");
		terrain = ((DataBufferByte)bufImg.getRaster().getDataBuffer()).getData();
		sprites = new ArrayList<>();
		sprites.add(new Sprite(100, 100));
	}

	byte[] getTerrain() { return this.terrain; }
	ArrayList<Sprite> getSprites() { return this.sprites; }

	void update() {// Update the agents
		for(int i = 0; i < sprites.size(); i++)
			sprites.get(i).update();
	}

	// 0 <= x < MAP_WIDTH. 0 <= y < MAP_HEIGHT.
	float getTravelSpeed(float x, float y) {	// 0 <= x < MAP_WIDTH; 0 <= y < MAP_HEIGHT.
		int xx = (int)(x * 0.1f); 
		int yy = (int)(y * 0.1f);
		if(xx >= 60) { 
			xx = 119 - xx; 
			yy = 59 - yy; 
		}
		int pos = 4 * (60 * yy + xx);
		return Math.max(0.2f, Math.min(3.5f, -0.01f * (terrain[pos + 1] & 0xff) + 0.02f * (terrain[pos + 3] & 0xff)));
	}

	Controller getController() { return controller; }
	float getX() { return sprites.get(0).x; }
	float getY() { return sprites.get(0).y; }
	float getDestX() { return sprites.get(0).destX; }
	float getDestY() { return sprites.get(0).destY; }

	void setDest(float x, float y) {
		Sprite s = sprites.get(0);
		s.destX = x;
		s.destY = y;
	}

	double distToDest(int sprite) {
		Sprite s = sprites.get(sprite);
		return Math.sqrt((s.x - s.destX) * (s.x - s.destX) + (s.y - s.destY) * (s.y - s.destY));
	}

	class Sprite {
		float x, y, destX, destY;

		Sprite(float x, float y) { 
			this.x = x; this.y = y; this.destX = x; this.destY = y; }

		void update() {
			float speed = Model.this.getTravelSpeed(this.x, this.y);
			float dx = this.destX - this.x;
			float dy = this.destY - this.y;
			float dist = (float)Math.sqrt(dx * dx + dy * dy);
			float t = speed / Math.max(speed, dist);
			dx *= t;
			dy *= t;
			this.x += dx;
			this.y += dy;
			this.x = Math.max(0.0f, Math.min(XMAX, this.x));
			this.y = Math.max(0.0f, Math.min(YMAX, this.y));
		}
	}
}