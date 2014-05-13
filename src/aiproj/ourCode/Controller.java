package aiproj.ourCode;

/* CONTROLLER NOT UPDATED FOR INTEGER BOARD.
 * Will not be used in this part (should be deleted before submission, kept around because code is useful elsewhere)
 */


/* Alasdair Norton (ajnorton)
 * Mostafa Rizk (mrizk) */
 
public class Controller {
	private static Board board;
	
	public static void main(String[] args) {
		/* Array to store which win conditions have been met.
		 * index 0 represents a black tripod, 1 a black loop
		 * 2 a white tripod, 3 a white loop
		 */
		Boolean[] win = {false, false, false, false};
		/* Initialise board, generate clusters */
		board = new Board();
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
			// Black wins
			System.out.println("Black");
			if(win[0] && !win[1]){
				System.out.println("Tripod");
			} else if (win[1] && !win[0]){
				System.out.println("Loop");
			} else {
				System.out.println("Both");
			}
		}
		
		if(!win[0] && !win[1] && (win[2] || win[3])){
			// White wins
			System.out.println("White");
			if(win[2] && !win[3]){
				System.out.println("Tripod");
			} else if (win[3] && !win[2]){
				System.out.println("Loop");
			} else {
				System.out.println("Both");
			}
		}
		
		if((win[0] || win[1]) && (win[2] || win[3])){
			// Draw
			System.out.println("Draw");
			System.out.println("Nil");
		}
		if(!win[0] && !win[1] && !win[2] && !win[3]){
			// Non-final state
			System.out.println("None");
			System.out.println("Nil");
		}
		
	}
		
	

}
