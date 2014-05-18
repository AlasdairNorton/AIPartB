package aiproj.ourCode;
import aiproj.fencemaster.Piece;
/* Alasdair Norton (ajnorton)
 * Mostafa Rizk (mrizk) */
 
 
import java.util.ArrayList;
import java.util.Comparator;


public class Position implements Piece,Comparator<Position>, Comparable<Position>{
	/* The tile's x coordinate */
	private int x;
	/* The tile's y coordinate */
	private int y;
	/* The colour of the piece on the tile as defined by the piece interface */
	private int colour;
	/* The cluster this node belongs to (null by default) */
	private Cluster parentCluster;
	/* The utility of this position to the player*/
	private int utility;
	
	public Position(int y, int x, int colour) {
		super();
		this.x = x;
		this.y = y;
		this.colour = colour;
		this.parentCluster = null;
	}
	
	public Cluster getParentCluster() {
		return parentCluster;
	}
	public void setParentCluster(Cluster parentCluster) {
		this.parentCluster = parentCluster;
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
	public int getColour() {
		return colour;
	}
	public void setColour(int colour) {
		this.colour = colour;
	}
	public int getUtility() {
		return utility;
	}
	public void setUtility(int utility) {
		this.utility = utility;
	}
	
	/* Takes the board containing this position, returns the
	 * 3-6 adjacent positions in an ArrayList
	 */
	public ArrayList<Position> getAdjacents(Board board){
		ArrayList<Position> adjacents = new ArrayList<Position>(0);
		/* First get tiles to left and right, testing that those tiles exist */
		if(this.x>0){
			adjacents.add(board.getNodes()[this.y][this.x-1]);
		}

		if(this.x<Math.min(board.getArraySize()+this.y, 2*board.getArraySize()-2)){
			adjacents.add(board.getNodes()[this.y][this.x+1]);
		}

		/* Next positions above and below */
		if(this.y>0){
			// Row above exists
			if(this.x < board.getArraySize()-1+this.y){
				// Check tile above and to the right exists
				adjacents.add(board.getNodes()[this.y-1][this.x]);
			}
			if(this.x > 0){
				// Check tile above and to the left exists
				adjacents.add(board.getNodes()[this.y-1][this.x-1]);
			}
		}

		if(this.y<2*board.getArraySize()-2){
			// Row below exists
			if(this.x < 2*board.getArraySize()-2){
				// Check tile below and to the right exists
				adjacents.add(board.getNodes()[y+1][x+1]);
			}
			if(this.x > this.y-board.getArraySize()+1){
				// Check tile below and to the left exists
				adjacents.add(board.getNodes()[y+1][x]);
			}
		}

		return adjacents;
	}

	@Override
	public int compareTo(Position p) {
		return this.getUtility() - p.getUtility();
	}

	@Override
	public int compare(Position p1, Position p2) {
		return p1.getUtility() - p2.getUtility();
	}
	
}
