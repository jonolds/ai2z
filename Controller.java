import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.LinkedList;
import javax.swing.Timer;

class Controller implements MouseListener {
	Agent agent;
	Model model; // holds all the game data
	View view; // the GUI
	LinkedList<MouseEvent> mouseEvents; // a queue of mouse events

	Controller() throws Exception {
		this.agent = new Agent();
		this.model = new Model(this);
		this.model.initGame();
		this.mouseEvents = new LinkedList<>();
	}
	
	static void playGame() throws Exception {
		Controller c = new Controller();
		c.view = new View(c, c.model); //creates JFrame-> spawns thread to pump events/keeps program running
		new Timer(20, c.view).start(); //creates ActionEvent at intervals: handled by View.actionPerformed
	}

	boolean update() throws IOException {
		agent.update(model);
		model.update();
		return true;
	}

	Model getModel() { return model; }

	MouseEvent nextMouseEvent() {
		if(mouseEvents.size() == 0)
			return null;
		return mouseEvents.remove();
	}

	public void mousePressed(MouseEvent e) {
		if(e.getY() < 600) {
			mouseEvents.add(e);
			if(mouseEvents.size() > 20) // discard events if the queue gets big
				mouseEvents.remove();
		}
	}
	public void mouseReleased(MouseEvent e) {    }
	public void mouseEntered(MouseEvent e) {    }
	public void mouseExited(MouseEvent e) {    }
	public void mouseClicked(MouseEvent e) {    }
}