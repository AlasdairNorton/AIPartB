package aiproj.ourCode;

/* Alasdair Norton (ajnorton)
 * Mostafa Rizk (mrizk) */
import java.util.ArrayList;
import aiproj.fencemaster.Piece;

/* A group of connected pieces of the same colour. */
public class Cluster implements Piece{
	private ArrayList<Position> nodes;
	private Boolean didChange;

	/* Colour of the pieces in the cluster defined by piece interface*/
	private int colour;


	public Cluster(int colour){
		nodes = new ArrayList<Position>(0);
		this.colour = colour;
		this.didChange = true;
	}
	
	public int getColour() {
		return colour;
	}

	public void setColour(int colour) {
		this.colour = colour;
	}
	
	public void addNode(Position newNode){
		nodes.add(newNode);
	}
	
	public ArrayList<Position> getNodes() {
		return nodes;
	}

	public void setNodes(ArrayList<Position> nodes) {
		this.nodes = nodes;
	}
	
	/**
	 * Returns true if cluster is a loop and false otherwise
	 * @param board
	 * @return
	 */
	public Boolean testLoop(Board board)
	{
		LoopChecker checker = new LoopChecker(board);
		boolean loop = checker.isLoop(this);
		
		return loop;
	}

	public Boolean testTripod(Board board){
		/* Returns true if cluster forms a tripod
		 	Currently prints out the colour of the cluster and
		 	the number of edges it connects */
		int size = board.getSize();
		int[] edges = {0,0,0,0,0,0};
		int tripodSum = 0;
		
		/* Tripod test: for each node in the cluster, check if it's on an
		 * edge, and not a corner. Set the value in edges[] representing 
		 * that edge to 1.
		 * Notation: edges are numbered starting from 0, going clockwise from
		 * the top (e.g., [0,1] is on edge 0, and [1,0] is on edge 5)
		 */
		for(Position node: nodes){
			if(node.getY() == 1 && node.getX()!=1 && node.getX()!=size){
				// If node is on top edge, and not a corner, set edges[0] to 1
				edges[0] = 1;
			}
			if(node.getY() > 1 && node.getY() < size &&
					node.getX()==size+node.getY()-1){
				/* If node is on upper right edge,
				   and not a corner, set edges[1] to 1 */
				edges[1] = 1;
			}
			if(node.getY() > size && node.getY() < 2*size-1 &&
					node.getX()==2*size-1){
				/* If node is on lower right edge,
				   and not a corner, set edges[2] to 1 */
				edges[2] = 1;
			}
			if(node.getY() == 2*size-1 && node.getX()!=size
					&& node.getX()!=2*size-1){
				/* If node is on bottom edge, and not a corner, 
				 * set edges[3] to 1
				 */
				edges[3] = 1;
			}
			if(node.getY()>size && node.getY()<2*size-1 &&
					node.getX()==node.getY()-size+1){
				/* If node is on lower left edge, and not a corner, 
				 * set edges[4] to 1
				 */
				edges[4] = 1;
			}
			if(node.getY()>1 && node.getY()<size && node.getX()==1){
				/* If node is on upper left edge, and not a corner,
				 *  set edges[5] to 1
				 */
				edges[5] = 1;
			}
		}
		/* Add up all entries in edges[], if the sum is >=3, there is a tripod. */
		for(int i=0;i<6;i++){
			tripodSum+=edges[i];
		}
		return tripodSum>=3;
	}

	public Boolean getDidChange() {
		return didChange;
	}

	public void setDidChange(Boolean didChange) {
		this.didChange = didChange;
	}
}
