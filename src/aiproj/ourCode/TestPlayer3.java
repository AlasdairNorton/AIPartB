package aiproj.ourCode;


import aiproj.fencemaster.Move;
import aiproj.fencemaster.Piece;
import aiproj.fencemaster.Player;

import java.io.PrintStream;
import java.util.Scanner;

/* Second test player class, attempt to implement minimax search */
public class TestPlayer3 implements Player, Piece {

	private Board board;
	private int piece;
	
	@Override
	public int getWinner() {
		/* Win condition testing placed in Board object so that logic
		 * does not have to be replicated for minimax algorithm to access.
		 */
		return board.testWin();
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
		Move move;
		if(board.getNumPieces()==0 || board.getNumPieces()==1){
			/* Opening move should always be one space in from the corner
			 * (i.e. (1,1), (1,5), (5,1) etc.)
			 */
			if(board.getNodes()[2][2].getColour()==EMPTY){
				move = new Move(piece, false, 1, 1);
			} else {
				move = new Move(piece, false, board.getSize()*2-3, board.getSize()*2-3);
			}
		} else {
			Minimax searchAgent = new Minimax(board, 1, piece);
			move = searchAgent.minimaxDecision();
		}

		board.getNodes()[move.Row+1][move.Col+1].setColour(piece);
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