package aiproj.fencemaster;


import aiproj.partA.*;

import java.io.PrintStream;
import java.util.Random;

public class Ajnorton implements Player, Piece {

	private Board board;
	private int piece;
	
	@Override
	public int getWinner() {
		// Taken from Controller.java, from Part A
		Boolean[] win = {false, false, false, false};
		board.makeClusters();
		for(Cluster clust: board.getClusters()){
			/* For each cluster, test win conditions */
			if(clust.getColour() == 'B'){
				if(clust.testTripod(board)){
					win[0]=true;
				}
				
				if(clust.testLoop(board)){
					win[1]=true;
				}
			}
			if(clust.getColour() == 'W'){
				if(clust.testTripod(board)){
					win[2]=true;
				}
				
				if(clust.testLoop(board)){
					win[3]=true;
				}
			}
		}
		if((win[0] || win[1]) && !win[2] && !win[3]){
			// Black Wins
			return BLACK;
		}
		if(!win[0] && !win[1] && (win[2] || win[3])){
			// White wins
			return WHITE;
		}
		if((win[0] || win[1]) && (win[2] || win[3])){
			// Draw
			return EMPTY;
		}
		if(!win[0] && !win[1] && !win[2] && !win[3]){
			// Non-final state
			return INVALID;
		}
		return INVALID;
	}

	@Override
	public int init(int n, int p) {
		// TODO Auto-generated method stub
		board = new Board(n);
		piece = p;
		
		/*Your implementation of this function should return a negative value if 
		  it does not initialise successfully. - Are there any potential failures?
		 */
	
		return 0;
	}

	@Override
	public Move makeMove() {
		// The actual AI Component
		// Generates a random move for testing
		
		Random rng = new Random();
		int r = 0, c = 0, cycles=0;
		while(cycles<100 && (board.getNodes()[r][c].getColour()==INVALID
				|| board.getNodes()[r][c].getColour()==BLACK
				|| board.getNodes()[r][c].getColour()==WHITE)){
			r = rng.nextInt(board.getArraySize()*2-1);
			c = rng.nextInt(board.getArraySize()*2-1);
			cycles++;
		}
		Move move = new Move(piece, false, r, c);
		board.getNodes()[c+1][r+1].setColour(piece);
		return move;
	}

	@Override
	public int opponentMove(Move m) {
		// TODO Auto-generated method stub
		
		/* First, check if location specified is on the board */
		if(m.Row >= 2*board.getArraySize()+1 || m.Col >=2*board.getArraySize()+1
				|| m.Row<0 || m.Col<0){
			/* Coordinates out of bounds, return failure */
			return -1;
		}
		
		/* Second, if it is a swap, check swap is allowed */
		if(m.IsSwap){
			/* 1. There is only one piece on the board
			 * 2. That piece is at the coordinates specified
			 * 3. It is this player's piece
			 */
			if(board.getNumPieces()==1 && board.getNodes()[m.Col+1][m.Row+1].getColour()==this.piece){
				/* Perform swap, return success */
				board.getNodes()[m.Col+1][m.Row+1].setColour(m.P);
				return 0;
			}else{
				/* Swap is illegal- there are too many pieces on the board
				 * or there is no piece at the location specified
				 * Return failure.
				 */
				return -1;
			}
		}
		
		/* If not swap, check position specified is legal, empty */
		if(board.getNodes()[m.Col+1][m.Row+1].getColour() == EMPTY){
			board.getNodes()[m.Col+1][m.Row+1].setColour(m.P);
			return 0;
		}
		
		return -1;
	}

	@Override
	public void printBoard(PrintStream output) {
		// TODO Auto-generated method stub
		char[] cells = {'O', '-', 'W', 'B'};
		int i, j;
		int size = board.getArraySize();
		Position[][] nodes = board.getNodes();
		
		for(i=0;i<2*size-1;i++){
			for(j=Math.max(0, i-size+1); j< Math.min(size+i, 2*size-1) ;j++){
				if(i==0 || i== 2*size-2
						|| j==Math.max(0, i-size+1)
						|| j==Math.min(size+i, 2*size-1)-1){
					/* Skip (Used to initialise invalid, but doing that for all non-board cells now)
					 * Should update loop to ignore these cells at some point */
				}else{
					output.print(cells[nodes[i][j].getColour()+1]);
				}
			}
			output.print("\n");
		}
	}

}
