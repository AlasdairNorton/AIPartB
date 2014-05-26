package aiproj.ourCode;

//TODO
//Determine closeness of either player to a win
//Further refine utility values to accurately represent priorities

import aiproj.fencemaster.Move;
import aiproj.fencemaster.Piece;
import aiproj.fencemaster.Player;

import java.io.PrintStream;
import java.util.Collections;
import java.util.PriorityQueue;
import java.util.Scanner;

public class TestPlayer2 implements Player, Piece {

	private Board board;
	private int piece;
	
	//private int board_size = board.getSize();
	private PriorityQueue<Position> queue = new PriorityQueue<Position> (10, Collections.reverseOrder());
	
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
	 * Given row i and column j, determines whether the position is a corner, edge, etc. 
	 * @param i
	 * @param j
	 * @return type of position
	 */
	private String checkPositionType(int i,int j)
	{
		String type;
		int N = board.getSize();
		
		if(i < 0 || j < 0 || i >= 2*N || j >= 2*N || board.getNodes()[j+1][i+1].getColour() == INVALID)
			type = "invalid";
		
		else if((i==0 && j==0) || //top left corner
				(i==0 && j==N-1) || //top right corner
				(i==(2*N -2) && j==(2*N -2)) || //bottom right corner
				(i==(2*N -2) && j==(N-1)) || //bottom left corner
				(i==(N-1) && j==0) || //middle left corner
				(i==(N-1) && j==(2*N-2))) //middle right corner
			{
				type = "corner";
			}

		else if( (i==0) || //top edge
				(j==0) || //upper left edge
				(i==(2*N-2)) || //bottom edge
				(j==(2*N-2)) || //bottom right edge
				((i>=N) && (j<N)) || //bottom left edge
				((i<N) && (j>=N)) //upper right edge
				)
		{
			type = "edge";
		}
		
		else if( ( i==1 && j==1) || //top left
				(i==(2*N-3) && j==(2*N-3)) || //bottom right
				(i==(2*N-3) && j==(N-1) ) || //bottom left
				(i==N-1 && j==1) || //middle left
				(i==N-1 && j==(2*N-3)) || //middle right
				(i==1 && j==N-1) //top right
				)
		{
			type = "near_edge";
		}
		
		else type = "normal";
		
		return type;
	}
	
	/**
	 * Rates utility of different board positions prior to any moves being made
	 */
	private void rateBoard()
	{
		int N = board.getSize();
		
		for(int i=0; i<2*N; i++)
		{
			for(int j=0; j<2*N; j++)
			{
				String type = checkPositionType(i,j);
				
				//Ignore out of bounds areas
				if(type.equals("invalid"))
					continue;
				
				//Add corner pieces
				else if(type.equals("corner"))
				{
					board.getNodes()[j+1][i+1].setUtility(1);
					queue.add(board.getNodes()[j+1][i+1]);
				}
				
				//Add edge pieces
				else if(type.equals("edge"))
				{
					board.getNodes()[j+1][i+1].setUtility(300);
					queue.add(board.getNodes()[j+1][i+1]);
				}
				
				//Add near-corner pieces
				else if(type.equals("near_edge"))
				{
					board.getNodes()[j+1][i+1].setUtility(400);
					queue.add(board.getNodes()[j+1][i+1]);
				}
				
				//Other pieces
				else
				{
					board.getNodes()[j+1][i+1].setUtility(2);
					queue.add(board.getNodes()[j+1][i+1]);
				}
			}
		}
	}
	
	/**
	 * Get utility of a certain type of position
	 * @param type
	 * @return
	 */
	private int getUtilityForPositionType(String type)
	{
		int newUtility = 0;
		
		if(type.equals("near_edge"))
			newUtility += 400;
		else if(type.equals("edge"))
			newUtility += 1000;
		else if(type.equals("normal"))
			newUtility += 200;
		else if(type.equals("corner"))
			newUtility += 100;
		
		return newUtility;
	}
	
	/**
	 * Update utility of positions directly surrounding a move
	 * @param move
	 */
	private void updateQueue(Move move)
	{
		//rate positions surrounding move
		int r[] = {0,-1,-1,0,1,1};
		int c[] = {-1,-1,0,1,1,0};
		
		for(int i=0;i<6;i++)
		{
			int this_r = move.Row + r[i];
			int this_c = move.Col +c[i];
			
			if(this_r < 0 || this_c < 0 || board.getNodes()[this_c][this_r].getColour() != EMPTY)
				continue;
			
			int newUtility = board.getNodes()[this_c][this_r].getUtility();
			
			//Add higher utility value if the move was made by an opponent (creates an aggressive agent) 
			if(move.P != this.piece)
				newUtility += 2000;
			else
				newUtility += 1000;
			
			String type = checkPositionType(this_r,this_c);
			newUtility += getUtilityForPositionType(type);
			
			//Increase utility of this position if it is next to valid non-taken high utility positions
			for(int j=0;j<6;j++)
			{
				int next_r = this_r + r[j];
				int next_c = this_c + c[j];
				
				if(next_r < 0 || next_c < 0 || board.getNodes()[next_c][next_r].getColour() != EMPTY)
					continue;
				
				String next_type = checkPositionType(next_r,next_c);
				newUtility += getUtilityForPositionType(next_type)/10;
			}
			
			
				
			board.getNodes()[this_c][this_r].setUtility(newUtility);
			queue.add(board.getNodes()[this_c][this_r]);
		}
		
	}

	@Override
	public Move makeMove() {
		// The actual AI Component
		
		if(board.getNumPieces() == 0)
			rateBoard();
		
		int r = 0, c = 0;

		Position p = queue.remove();//Get top value from Priority Queue
		while(!queue.isEmpty())
		{
			r = p.getX();
			c = p.getY();
			
			if(board.getNodes()[c+1][r+1].getColour()==EMPTY)
			{
				break;
			}
			
			p = queue.remove();
		}
		
		Move move = new Move(piece, false, r, c);
		board.getNodes()[c+1][r+1].setColour(piece);
		updateQueue(move);
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
				updateQueue(m);
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
			updateQueue(m);
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
