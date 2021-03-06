package aiproj.ourCode;

/* Alasdair Norton (ajnorton)
 * Mostafa Rizk (mrizk) */

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
	
	//Priority queue for keeping track of which positions have the highest utility
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
	 * Given row i and column j, determines whether the position is of type corner, edge, near-edge or normal 
	 * @param i
	 * @param j
	 * @return type of position
	 */
	private String checkPositionType(int i,int j)
	{
		String type;
		int N = board.getSize();
		
		if(i < 0 || j < 0 || i >= 2*N || j >= 2*N || board.getNodes()[i+1][j+1].getColour() == INVALID)
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
			type = "near_edge";//This is a position that is 1 move away from two edges e.g. (1,1)
		}
		
		else type = "normal";
		
		return type;
	}
	
	/**
	 * Gets total count of enemy pieces surrounding a position
	 * @param row
	 * @param col
	 * @param enemyColour
	 * @return
	 */
	private int getEnemyPieceCount(int row,int col, int enemyColour)
	{
		int enemyCount = 0;
		
		int r[] = {0,-1,-1,0,1,1};
		int c[] = {-1,-1,0,1,1,0};
		
		for(int i=0;i<6;i++)
		{
			int this_r = row + r[i];
			int this_c = col +c[i];
			
			if(this_r < 0 || this_c < 0)
				continue;
			
			if(board.getNodes()[this_r][this_c].getColour() == enemyColour)
				enemyCount++;
			
		}
		
		return enemyCount;
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
					board.getNodes()[i+1][j+1].setUtility(1);
					queue.add(board.getNodes()[i+1][j+1]);
				}
				
				//Add edge pieces
				else if(type.equals("edge"))
				{
					board.getNodes()[i+1][j+1].setUtility(300);
					queue.add(board.getNodes()[i+1][j+1]);
				}
				
				//Add near-corner pieces
				else if(type.equals("near_edge"))
				{
					board.getNodes()[i+1][j+1].setUtility(400);
					queue.add(board.getNodes()[i+1][j+1]);
				}
				
				//Other pieces
				else
				{
					board.getNodes()[i+1][j+1].setUtility(2);
					queue.add(board.getNodes()[i+1][j+1]);
				}
			}
		}
	}
	
	/**
	 * Get utility of a position with a certain type.
	 * When function is used for 'forward looking' from an empty position, returns high utility values for positions occupied by
	 * opponent pieces. This gives the empty position a higher value if it's surrounded by enemy pieces because it's a potential 
	 * loop blocker
	 * @param type
	 * @return
	 */
	private int getUtilityForPositionType(String type,int colour)
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
		
		//Higher utility is given to enemy pieces, meaning the agent is aggressive and tries to block the opponent before trying to 
		//complete its own loops
		if(colour != this.piece && colour != EMPTY)
			newUtility += 5000;
		else if (colour != EMPTY)
			newUtility += 2000;
		
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
		
		int utilityMultiplier = 1;
		
		
		for(int i=0;i<6;i++)
		{
			int this_r = move.Row + r[i];
			int this_c = move.Col +c[i];
			
			if(this_r < 0 || this_c < 0 || board.getNodes()[this_r][this_c].getColour() != EMPTY)
				continue;
			
			int newUtility = board.getNodes()[this_r][this_c].getUtility();
			
			String type = checkPositionType(this_r,this_c);
			newUtility += getUtilityForPositionType(type,move.P);
			
			//If move was made by opponent, check how many same coloured pieces are adjacent to each surrounding position
			//More pieces indicate higher likelihood of trying to complete a loop.
			//Increment the utilityMultiplier to highly weight these positions as they are potential loop-blockers
			if(move.P != this.piece)
				utilityMultiplier += getEnemyPieceCount(this_r,this_c,move.P);
			
			//Increase utility of this position if it's next to many high utility empty positions or opponent pieces
			for(int j=0;j<6;j++)
			{
				int next_r = this_r + r[j];
				int next_c = this_c + c[j];
				
				if(next_r < 0 || next_c < 0)
					continue;
				
				String next_type = checkPositionType(next_r,next_c);
				int next_colour = board.getNodes()[next_r][next_c].getColour();
				
				//Divided by 100 to avoid penalizing edges (which can't gain much utility here because they have fewer neighbors)
				newUtility += getUtilityForPositionType(next_type,next_colour)/100;
			}
			
			
			newUtility *= utilityMultiplier;	
			board.getNodes()[this_r][this_c].setUtility(newUtility);
			queue.add(board.getNodes()[this_r][this_c]);
		}
		
	}

	@Override
	public Move makeMove() {
		// The actual AI Component
		
		if(board.getNumPieces() == 0)
			rateBoard();
		
		int r = 0, c = 0;

		Position p = queue.remove();//Get top value from Priority Queue
		
		//Traverse queue until find the highest valued empty position and move there.
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
		
		Move move = new Move(piece, false, c, r);
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
			if(board.getNumPieces()==1 && board.getNodes()[m.Row+1][m.Col+1].getColour()==this.piece){
				/* Perform swap, return success */
				board.getNodes()[m.Row+1][m.Col+1].setColour(m.P);
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
		if(board.getNodes()[m.Row+1][m.Col+1].getColour() == EMPTY){
			board.getNodes()[m.Row+1][m.Col+1].setColour(m.P);
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
