package aiproj.ourCode;

//TODO
//Check that r and c aren't switched
//Check that priority queue isn't sorting in reverse order
//Change so that adjacent positions with more strategic value are recognized and given more points
//Change to be forward-looking, i.e. change utility points added if position has more influence on surroundings

import aiproj.fencemaster.Move;
import aiproj.fencemaster.Piece;
import aiproj.fencemaster.Player;

import java.io.PrintStream;
import java.util.PriorityQueue;
import java.util.Scanner;

public class TestPlayer2 implements Player, Piece {

	private Board board;
	private int piece;
	
	private PriorityQueue<Position> queue = new PriorityQueue<Position> ();
	
	@Override
	public int getWinner() {
		// Taken from Controller.java, from Part A
		Boolean[] win = {false, false, false, false};
		board.clearClusters();
		board.makeClusters();
		for(Cluster clust: board.getClusters()){
			/* For each cluster, test win conditions */
			if(clust.getColour() == BLACK){
				if(clust.testTripod(board)){
					win[0]=true;
				}
				
				if(clust.testLoop(board)){
					win[1]=true;
				}
			}
			if(clust.getColour() == WHITE){
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
	
	/**
	 * Rates utility of different board positions prior to any moves being made
	 */
	private void rateBoard()
	{
		int N = board.getSize();
		
		for(int i=0; i<2*N-1; i++)
		{
			for(int j=0; j<2*N-1; j++)
			{
				//Ignore out of bounds areas
				if(board.getNodes()[i][j].getColour() == INVALID)
					continue;
				
				//Add corner pieces
				if((i==0 && j==0) || //top left corner
					(i==0 && j==N-1) || //top right corner
					(i==(2*N -2) && j==(2*N -2)) || //bottom right corner
					(i==(2*N -2) && j==(N-1)) || //bottom left corner
					(i==(N-1) && j==0) || //middle left corner
					(i==(N-1) && j==(2*N-2))) //middle right corner
				{
					board.getNodes()[i][j].setUtility(0);
					queue.add(board.getNodes()[i][j]);
				}
				
				//Add edge pieces
				else if( (i==0) || //top edge
						(j==0) || //upper left edge
						(i==(2*N-2)) || //bottom edge
						(j==(2*N-2)) || //bottom right edge
						((i>=N) && (j<N)) || //bottom left edge
						((i<N) && (j>=N)) //upper right edge
						)
				{
					board.getNodes()[i][j].setUtility(2);
					queue.add(board.getNodes()[i][j]);
				}
				
				//Add near-corner pieces
				else if( ( i==1 && j==1) || //top left
						(i==(2*N-3) && j==(2*N-3)) || //bottom right
						(i==(2*N-3) && j==(N-1) ) || //bottom left
						(i==N-1 && j==1) || //middle left
						(i==N-1 && j==(2*N-3)) || //middle right
						(i==1 && j==N-1) //top right
						)
				{
					board.getNodes()[i][j].setUtility(3);
					queue.add(board.getNodes()[i][j]);
				}
				
				//Other pieces
				else
				{
					board.getNodes()[i][j].setUtility(1);
					queue.add(board.getNodes()[i][j]);
				}
			}
		}
	}
	
	/**
	 * Update utility of positions directly surrounding a move
	 * @param move
	 */
	private void updateQueue(Move move)
	{
		if(board.getNumPieces() == 0)
			rateBoard();
		
		//rate positions surrounding move
		int r[] = {0,-1,-1,0,1,1};
		int c[] = {-1,-1,0,1,1,0};
		
		for(int i=0;i<6;i++)
		{
			if(board.getNodes()[move.Row+r[i]][move.Col+c[i]].getColour() != EMPTY)
				continue;
			
			int newUtility = board.getNodes()[move.Row+r[i]][move.Col+c[i]].getUtility()+1;
			board.getNodes()[move.Row+r[i]][move.Col+c[i]].setUtility(newUtility);
			queue.add(board.getNodes()[move.Row+r[i]][move.Col+c[i]]);
		}
		
	}

	@Override
	public Move makeMove() {
		// The actual AI Component
		
		int r = 0, c = 0;

		Position p = queue.remove();//Get top value from Priority Queue
		while(!queue.isEmpty())
		{
			r = p.getX();
			c = p.getY();
			
			if(board.getNodes()[r][c].getColour()==EMPTY)
			{
				break;
			}
			
			p = queue.remove();
		}
		
		Move move = new Move(piece, false, r, c);
		board.getNodes()[r][c].setColour(piece);
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
		int i, j, k;
		int size = board.getArraySize();
		Position[][] nodes = board.getNodes();
		
		for(i=1;i<2*size-2;i++){
			for(k=0;k<Math.abs(size-i-1);k++){
				/* Indent row */
				output.print(" ");
			}
			for(j=Math.max(1, i-size+2); j< Math.min(size+i-1, 2*size-2) ;j++){
					output.print(cells[nodes[i][j].getColour()+1]+" ");
			}
			output.print("\n");
		}
	}

}
