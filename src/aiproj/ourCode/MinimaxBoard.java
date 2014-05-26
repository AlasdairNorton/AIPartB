package aiproj.ourCode;

import java.util.ArrayList;

import aiproj.fencemaster.Move;

/* Class for populating minimax tree.
 * Extends board, adding 2 traits;
 * Max, indicating whether this board should pick the lowest or highest valued
 * child
 * parent, the parent board state
 * and children, an arraylist of the child board states
 */
public class MinimaxBoard extends Board {
	
	private boolean max;
	private MinimaxBoard parent;
	private ArrayList<MinimaxBoard> children;
	private int colour;
	private int depth;
	
	
	/**
	 * Constructor for root node for minimax tree 
	 * @param initial The present board state
	 */
	public MinimaxBoard(Board initial, int colour){
		/* initialise Board variables */
		super(initial.getSize());
		setMax(true);
		setParent(null);
		setChildren(new ArrayList<MinimaxBoard>(0));
		this.setDepth(0);
		this.setColour(colour);
		/* Set board state to the input's */
		for(int i=0; i<2*this.getArraySize()-1;i++){
			for(int j=0; j<2*this.getArraySize()-1;j++){
				this.setNode(initial.getNodes()[i][j].getColour(), i, j);
			}
		}
	}
	
	
	/**
	 * @param parent The parent node in the minimax tree
	 * @param move The move made to get from the parent state to this child state
	 */
	public MinimaxBoard(MinimaxBoard prev, Move move){
		super(prev.getSize());
		if(prev.isMax()){
			this.max = false;
		} else {
			this.max = true;
		}
		this.parent = prev;
		this.children = new ArrayList<MinimaxBoard>(0);
		this.setDepth(parent.getDepth()+1);
		if(parent.getColour()==WHITE){
			this.colour = BLACK;
		}else{
			this.colour = WHITE;
		}
		
		/* Set board state to the input's */
		for(int i=0; i<2*this.getArraySize()-1;i++){
			for(int j=0; j<2*this.getArraySize()-1;j++){
				this.setNode(prev.getNodes()[i][j].getColour(), i, j);
			}
		}
		
		/* Update board state.
		 * Only legal moves will be sent to this function, 
		 * so no need to guard against bad moves here
		 */
		setNode(move.P, move.Row+1, move.Col+1);
	}


	public boolean isMax() {
		return max;
	}


	public void setMax(boolean max) {
		this.max = max;
	}


	public MinimaxBoard getParent() {
		return parent;
	}


	public void setParent(MinimaxBoard parent) {
		this.parent = parent;
	}


	public ArrayList<MinimaxBoard> getChildren() {
		return children;
	}


	public void setChildren(ArrayList<MinimaxBoard> children) {
		this.children = children;
	}


	public int getColour() {
		return colour;
	}


	public void setColour(int colour) {
		this.colour = colour;
	}


	public int getDepth() {
		return depth;
	}


	public void setDepth(int depth) {
		this.depth = depth;
	}
}
