package aiproj.ourCode;

import java.util.ArrayList;
import java.util.Arrays;

import aiproj.fencemaster.Piece;
import aiproj.fencemaster.Move;

public class Minimax implements Piece {
	private MinimaxBoard root;
	private int maxDepth;
	private int nodesSeen;
	private int maxColour;
	
	public Minimax (Board initial, int maxDepth, int colour){
		root = new MinimaxBoard(initial, colour);
		this.maxDepth = maxDepth;
		nodesSeen=1;
		maxColour = colour;
	}
	
	public Move minimaxDecision(){
		Move[] moveList = generateMoves(root, root.getColour());
		double[] moveVals = new double[moveList.length];
		int j, index=0;
		double maxVal=-1;
		for(int i=0;i<moveList.length;i++){
			moveVals[i] = minimaxValue(moveList[i], root);
		}
		for(j=0;j<moveVals.length;j++){
			if(moveVals[j]>maxVal){
				maxVal = moveVals[j];
				index = j;
			}
		}
		System.out.println(maxVal);
		return moveList[index];
	}
	
	public double minimaxValue(Move move, MinimaxBoard prev){
		nodesSeen+=1;
		MinimaxBoard current = new MinimaxBoard(prev, move);
			
		
		/* Check terminal, maxDepth */
		if(current.testWin() == BLACK || current.testWin() == WHITE){
			/* Board in final state */
			if(current.testWin() == root.getColour()){
				/* Winning state for current player */
				return 1;
			} else {
				/* Losing state for current player, or draw
				 * Draws will be considered as losses by this
				 * heuristic */
				return -1;
			}
		}
		if(current.getDepth() == maxDepth){
			return getScore(current);
		}
		/* Else recurse
		 * 1. Make list of next moves
		 * 2. Get scores for each move
		 * 3. if max, return highest score, if min return lowest */
		Move[] moveList = generateMoves(current, current.getColour());
		double[] moveVals = new double[moveList.length];
		double maxVal = -1, minVal = 1;
		for(int i=0;i<moveList.length;i++){
			moveVals[i] = minimaxValue(moveList[i], current);
		}
		
		if(current.isMax()){
			for(double i: moveVals){
				if(i>maxVal){
					maxVal = i;
				}
			}
			return maxVal;
		}
		if(!current.isMax()){
			for(double i: moveVals){
				if(i<minVal){
					minVal = i;
				}
			}
			return minVal;
		}
		
		/* To placate error messages;
		 * control cannot actually reach this statement */
		return 0;
	}
	
	public double getScore(MinimaxBoard board){
		/* Function to calculate the heuristic value of a given board state
		 * Value is normalised onto [-1,1] with -1 representing a loss
		 * and 1 representing a win.
		 * Placeholder returns 0 to test rest of minimax algo. 
		 */
		double score=0;
		board.clearClusters();
		board.makeClusters();
		int numPieces = board.getNumPieces();
		/* Score each cluster separately */
		/* Scoring tripods: each cluster is given a score out of 3;
		 * the reciprocal of the shortest straight line distance to
		 * the three closest edges
		 * Each cluster's score is weighted by the 
		 * number of pieces in the cluster/number of pieces on the board
		 */
		for(Cluster c: board.getClusters()){
			double clustScore = 0;
			clustScore +=tripodScore(c, board);
			if(c.getColour() == maxColour){
				score += clustScore*c.getNodes().size(); 
			} else {
				score -= clustScore*c.getNodes().size(); 
			}
		}
		score= score/numPieces;
		return score;
	}
	
	public double tripodScore(Cluster c, Board board){
		int[] edgeDists = {-1, -1, -1, -1, -1, -1};
		int x, y;
		double score;
		for(Position p: c.getNodes()){
			x = p.getX();
			y = p.getY();
			/* Dist from top edge */
			if(x!=1 && x!=board.getSize()+y-1){
				/* Dist from top edge is the y coord */
				if(edgeDists[0]>y || edgeDists[0]==-1){
					edgeDists[0]=y;
				}
			} else {
				/* Dist from top edge is y coord +1 */
				if(edgeDists[0]>y+1 || edgeDists[0]==-1){
					edgeDists[0]=y+1;
				}
			}
			/* Top Right */
			if(y!=1 && x!=2*board.getSize()-1){
				if(edgeDists[1]>x+board.getSize()-y || edgeDists[1]==-1){
					edgeDists[1]=x+board.getSize()-y;
				}
			} else {
				if(edgeDists[1]>y+board.getSize()-x+1 || edgeDists[1]==-1){
					edgeDists[1]=y+board.getSize()-x+1;
				}
			}
			/* Bottom Right */
			if(y!=2*board.getSize()-1 || x!=board.getSize()+y-1){
				/* Dist from bottom right edge is x dist from edge */
				if(edgeDists[2]>2*board.getSize()-x || edgeDists[2]==-1){
					edgeDists[2]=2*board.getSize()-x;
				}
			} else {
				/* Dist from bottom right edge is x dist from edge +1 */
				if(edgeDists[2]>2*board.getSize()-x+1 || edgeDists[2]==-1){
					edgeDists[2]=2*board.getSize()-x+1;
				}
			}
			/* Bottom */
			if(x!=2*board.getSize()-1 && x!=y-board.getArraySize()+2){
				/* Dist from top edge is the y dist from bottom */
				if(edgeDists[3]>2*board.getSize()-y || edgeDists[3]==-1){
					edgeDists[3]=2*board.getSize()-y;
				}
			} else {
				/* Dist from top edge is y coord +1 */
				if(edgeDists[3]>2*board.getSize()-y+1 || edgeDists[3]==-1){
					edgeDists[3]=2*board.getSize()-y+1;
				}
			}
			/* Bottom Left */
			if(x!=1 && y!=2*board.getSize()-1){
				if(edgeDists[4]>x+board.getSize()-y || edgeDists[4]==-1){
					edgeDists[4]=x+board.getSize()-y;
				}
			} else {
				if(edgeDists[4]>x+board.getSize()-y+1 || edgeDists[4]==-1){
					edgeDists[4]=x+board.getSize()-y+1;
				}
			}
			/* Top Left */
			if(y!=1 || x!=y-board.getArraySize()+2){
				/* Dist from top left edge is x coord */
				if(edgeDists[5]>x || edgeDists[5]==-1){
					edgeDists[5]=x;
				}
			} else {
				/* Dist from top left edge is x coord +1 */
				if(edgeDists[5]>x+1 || edgeDists[5]==-1){
					edgeDists[5]=x+1;
				}
			}
		}
		/* Have now found the minimum distance from the cluster
		 * to each edge. 
		 * Return the sum of the reciprocal distances from the three
		 * closest, divided by three to normalise onto [0,1]
		 */
		Arrays.sort(edgeDists);
		score = 1/(double)edgeDists[0]+1/(double)edgeDists[1]+1/(double)edgeDists[2];
		return score/3;
	}
	
	/* For the sake of efficiency, only checking positions
	 * adjacent to existing clusters
	 */
	public Move[] generateMoves(MinimaxBoard board, int colour){
		board.clearClusters();
		board.makeClusters();
		
		
		ArrayList<Move> moves = new ArrayList<Move>(0);
		Move[] demoArray = new Move[1];
		Move newMove;
		for(Cluster clust: board.getClusters()){
			for(Position pos: clust.getNodes()){
				for(Position node: pos.getAdjacents(board)){
					if(node.getColour() == EMPTY){
						newMove = new Move(colour, false, node.getY()-1, node.getX()-1);
						if(!moves.contains(newMove)){
							moves.add(newMove);
						}
					}
				}
			}
		}	
		return moves.toArray(demoArray);
	}

	public MinimaxBoard getRoot() {
		return root;
	}

	public void setRoot(MinimaxBoard root) {
		this.root = root;
	}
}
