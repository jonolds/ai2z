import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class View extends JFrame implements ActionListener {
	Controller controller;
	Model model;
	private MyPanel panel;
	int[] t2;

	public View(Controller c, Model m) throws Exception {
		this.controller = c;
		this.model = m;
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setTitle("Moving Robot");
		this.setSize(1203, 636);
		this.panel = new MyPanel();
		this.panel.addMouseListener(controller);
		this.getContentPane().add(this.panel);
		this.setVisible(true);
	}

	public void actionPerformed(ActionEvent evt) { repaint(); }// indirectly calls MyPanel.paintComponent

	class MyPanel extends JPanel {
		Image image_robot, redDot;

		MyPanel() throws Exception {
			image_robot = ImageIO.read(new File("robot_blue.png"));
			redDot = ImageIO.read(new File("redDot.png"));
		}

		void drawTerrain(Graphics g) {
			byte[] terrain = model.getTerrain();
			int posBlue = 0;
			int posRed = (60 * 60 - 1) * 4;
			for(int y = 0; y < 60; y++) {
				for(int x = 0; x < 60; x++) {
					int bb = terrain[posBlue + 1] & 0xff;
					int gg = terrain[posBlue + 2] & 0xff;
					int rr = terrain[posBlue + 3] & 0xff;
					g.setColor(new Color(rr, gg, bb));
					g.fillRect(10 * x, 10 * y, 10, 10);
					posBlue += 4;
				}
				for(int x = 60; x < 120; x++) {
					int bb = terrain[posRed + 1] & 0xff;
					int gg = terrain[posRed + 2] & 0xff;
					int rr = terrain[posRed + 3] & 0xff;
					g.setColor(new Color(rr, gg, bb));
					g.fillRect(10 * x, 10 * y, 10, 10);
					posRed -= 4;
				}
			}
		}

		void drawSprites(Graphics g) {
			ArrayList<Model.Sprite> sprites = model.getSprites();
			for(int i = 0; i < sprites.size(); i++) {
				Model.Sprite s = sprites.get(i);
				g.drawImage(redDot, (int)s.destX - 3, (int)s.destY - 3, null);
			}
		}

		public void paintComponent(Graphics g) {
			// Give the agents a chance to make decisions
			try {
				if(!controller.update())
					View.this.dispatchEvent(new WindowEvent(View.this, WindowEvent.WINDOW_CLOSING));
			} catch (IOException e) {
				e.printStackTrace();
			} // Close this window
			drawTerrain(g);
			
			drawSprites(g);
			
			controller.agent.drawPlan(g, model);
		}
	}
}