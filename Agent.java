import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.TreeSet;
import java.util.Vector;
import javax.imageio.ImageIO;

class Agent {
	public Vector<int[]> path = new Vector<int[]>(); //elem 0 is goal. path.get(path.size()-1) is init pos
	int[] bigGoal = new int[] {100, 100};
	PriorityQueue<State> frontier = new PriorityQueue<>();
	Image bigGoalGrn = ImageIO.read(new File("grnDot.png")), curPosPur= ImageIO.read(new File("purDot.png"));
	boolean useAStar = false;
	double minSpeed = -1.0;

	public static void main(String[] args) throws Exception { Controller.playGame(); }
	Agent() throws IOException {}
	
	void drawPlan(Graphics g, Model m) {
		drawGridLines(g);
		drawDots(g, m);
		drawFrontier(g);
		drawPath(g, m);
	}

	void update(Model m) throws IOException {
		Controller c = m.getController();
		if(minSpeed < 0)
			minSpeed = getMinSpeed(m);
		
		Planner.PathFrontCombo combo;
		if(useAStar)
			combo = (new Planner(m, bigGoal[0], bigGoal[1])).aStar(minSpeed);
		else
			combo = (new Planner(m, bigGoal[0], bigGoal[1])).ucs();
		path = combo.path;
		frontier = combo.frontier;
		if(m.getX() == m.getDestX() && m.getY() == m.getDestY()) {
			if(path.size() > 0)
				m.setDest(path.get(path.size()-1)[0], path.get(path.size()-1)[1]);
			else if(isWithinTen(bigGoal[0], bigGoal[1], (int)m.getX(), (int)m.getY()))
				m.setDest((float)bigGoal[0], (float)bigGoal[1]);
		}
		while(true) {
			MouseEvent e = c.nextMouseEvent();
			if(e == null)
				break;
			if(e.getButton() == 1)
				useAStar = false;
			else if(e.getButton() == 3) {
				useAStar = true;
				if(minSpeed < 0)
					getMinSpeed(m);
			}
			bigGoal = new int[] {e.getX(), e.getY()};
		}
	}
	
	double getMinSpeed(Model m) {
		double currentMin = m.getTravelSpeed(0,0);
		for(int i = 0; i < Model.XMAX; i+=10)
			for(int k = 0; k < Model.YMAX; k+=10)
				if(m.getTravelSpeed(i, k) < currentMin)
					currentMin = m.getTravelSpeed(i, k);
		return currentMin;
	}
	
	boolean isWithinTen(int x1, int y1, int x2, int y2) {
		return Math.abs(x1 - x2) < 10 && Math.abs(y1 - y2) < 10;
	}
	
	void drawDots(Graphics g, Model m) {
		g.drawImage(bigGoalGrn, bigGoal[0]-3, (int)bigGoal[1] -3, null);
		g.drawImage(curPosPur, (int)m.getX() - 3, (int)m.getY() - 3, null);
	}
	void drawPath(Graphics g, Model m) {
			g.setColor(Color.white);
			int[] last = bigGoal;
			for(int i = 0; i < path.size(); i++) {
				g.drawLine(last[0], last[1], path.get(i)[0], path.get(i)[1]);
				last = new int[] {path.get(i)[0], path.get(i)[1]};
			}
			if(!path.isEmpty())
				g.drawLine((int)m.getX(), (int)m.getY(), path.get(path.size()-1)[0], path.get(path.size()-1)[1]);
	}
	
	void drawFrontier(Graphics g) {
		g.setColor(Color.YELLOW);
		for(State s : frontier)
			g.fillOval(s.pos[0], s.pos[1], 10, 10);
	}
	
	void drawGridLines(Graphics g) {
		int squareLen = 10; 
		g.setColor(Color.lightGray);
		for(int i = 0; i < g.getClipBounds().width; i+=squareLen)
			g.drawLine(i, 0, i, g.getClipBounds().height);
		for(int i = 0; i < g.getClipBounds().height; i+=squareLen)
			g.drawLine(0, i, g.getClipBounds().width, i);
	}
	
	class State {
		double cost, est;
		State parent;
		int[] pos;
		
		State(State s) {
			this.cost = s.cost;
			this.est = s.est;
			this.parent = s.parent;
			this.pos[0] = s.pos[0];
			this.pos[1] = s.pos[1];
		}
		State(double cost, State parent, int[] pos) {
			this.cost = cost;
			this.parent = parent;
			this.pos = pos;
		}
		State(double cost, double est, State parent, int[] pos) {
			this(cost, parent, pos);
			this.est = est;
			
		}
		Double getTotal() {
			return cost + est;
		}
	}
	
	class Planner {
		PathFrontCombo ucs() {
			PriorityQueue<State> frontier = new PriorityQueue<State>(new CostComp()) {{add(new State(0.0, null, startPos));}};
			TreeSet<State> visited = new TreeSet<State>(new PosComp()) {{add(new State(0.0, null, startPos));}};
			while(!frontier.isEmpty()) {
				State s = frontier.poll();
				if(isWithinTen(s))
					return new PathFrontCombo(state2moves(s), frontier);
				for(int i = 0; i < 16; i+=2) {
					int[] newPos = new int[] {s.pos[0] + act[i], s.pos[1] + act[i+1]};
					if(newPos[0] >= 0 && newPos[0] < 1200 && newPos[1] >= 0 && newPos[1] < 600) {
						float speed = m.getTravelSpeed((float)newPos[0], (float)newPos[1]);
						double actCost = this.getDistance(s.pos[0], s.pos[1], newPos[0], newPos[1])/speed;
						State newChild = new State(actCost, s, newPos);
						if(visited.contains(newChild)) {
							State oldChild = visited.floor(newChild);
							if(oldChild != null) if (s.cost + actCost < oldChild.cost) {
								oldChild.cost = s.cost + actCost;
								oldChild.parent = s;
							}
						}
						else {
							newChild.cost += s.cost;
							frontier.add(newChild);
							visited.add(newChild);
						}
					}
				}
			}
			return null;
		}
		
		Model m;
		int[] startPos, goalPos, act = new int[] {10,-10, 10,0, 10,10, 0,10, 0,-10, -10,-10, -10,0, -10,10};
		
		Planner(Model m, int destX, int destY) throws IOException {
			this.m = m;
			this.startPos = new int[] {(int)m.getX(), (int)m.getY()};
			this.goalPos = new int[] {destX, destY};
		}
		
		PathFrontCombo aStar(Double minSpeed) {
			PriorityQueue<State> frontier = new PriorityQueue<State>(new CostComp()) {{add(new State(0.0, null, startPos));}};
			TreeSet<State> visited = new TreeSet<State>(new PosComp()) {{add(new State(0.0, 0.0, null, startPos));}};
			while(!frontier.isEmpty()) {
				State s = frontier.poll();
				if(isWithinTen(s))
					return new PathFrontCombo(state2moves(s), frontier);
				for(int i = 0; i < 16; i+=2) {
					int[] newPos = new int[] {s.pos[0] + act[i], s.pos[1] + act[i+1]};
					if(newPos[0] >= 0 && newPos[0] < 1200 && newPos[1] >= 0 && newPos[1] < 600) {
						float speed = m.getTravelSpeed((float)newPos[0], (float)newPos[1]);
						double actCost = this.getDistance(s.pos[0], s.pos[1], newPos[0], newPos[1])/speed;
						double extraCost = this.getDistance(newPos[0], newPos[1], goalPos[0], goalPos[1])/minSpeed;
						State newChild = new State(actCost + s.cost, extraCost, s, newPos);
						if(visited.contains(newChild)) {
							State oldChild = visited.floor(newChild);
							if(oldChild != null) if (s.cost + actCost < oldChild.cost) {
								oldChild.cost = s.cost + actCost;
								oldChild.parent = s;
							}
						}
						else {
							frontier.add(newChild);
							visited.add(newChild);
						}
					}
				}
			}
			return null;
		}

		boolean isWithinTen(State s) {
			return Math.abs(s.pos[0] - goalPos[0]) < 10 && Math.abs(s.pos[1] - goalPos[1]) < 10;
		}
		public Vector<int[]> state2moves(State s) {
			Vector<int[]> moves = new Vector<>();
			if(s != null)
				while(s.parent != null) {
					moves.add(new int[]{s.pos[0], s.pos[1]});
					s = s.parent;
				}
			return moves;
		}
		
		public class PathFrontCombo {
			Vector<int[]> path;
			PriorityQueue<State> frontier;
			PathFrontCombo(Vector<int[]> path, PriorityQueue<State> frontier) {
				this.path = path; this.frontier = frontier;
			}
			PathFrontCombo() {
				path = null;
				frontier = new PriorityQueue<>();
			}
		}
		
		public float getDistance(float x1, float y1, float x2, float y2) {
			return (float)Math.sqrt(Math.pow(x2-x1, 2) + (float)Math.pow(y2-y1, 2));
		}
	}
	
	class CostComp implements Comparator<State> {
		public int compare(State a, State b) {
			return Double.compare(a.getTotal(), b.getTotal());
		}
	}
	class PosComp implements Comparator<State> {
		public int compare(State a, State b) {
			for(int i = 0; i < 2; i++)
				if(a.pos[i] < b.pos[i])
					return -1;
				else if(a.pos[i] > b.pos[i])
					return 1;
			return 0;
		}
	}
}