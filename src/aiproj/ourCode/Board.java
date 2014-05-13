package aiproj.ourCode;

/* Alasdair Norton (ajnorton)
 * Mostafa Rizk (mrizk) */
import java.util.ArrayList;
import java.util.Scanner;
import aiproj.fencemaster.Piece;

public class Board implements Piece{
	/* The length of the board's sides */
	private int size;
	/* Length of the sides of the board the program stores
	 * The program considers a board one size larger than it actually is-
	 * Pieces cannot be placed on the outer edge, but it is used in
	 * computation
	 */
	private int arraySize;
	
	/* Array containing all the positions on the board
	 * First dimension is y (rows), second is x (position in row) */
	private Position[][] nodes;
	
	/* Arraylist containing all clusters (continuous groups of same-coloured pieces) */
	private ArrayList<Cluster> clusters;
	
	/* Creates a new board defined by the standard input 
	 * Not used in Part B, not guaranteed to be compatible with rest
	 * of code */
	public Board (){
		/* Initialise instance variables */
		clusters = new ArrayList<Cluster>();		
		Scanner sc = new Scanner(System.in);
		int i, j;
		
		/* Get size from input */
		if(sc.hasNextInt()){
			this.size = sc.nextInt();
		} else {
			System.out.println("Error");
			System.exit(1);
		}	
		
		/* Set arraysize, initialise array */
		this.arraySize = size+1;
		nodes = new Position[2*arraySize-1][2*arraySize-1];
		
		/* For each cell in the array, initialise to INVALID piece */
		for(i=0;i<2*arraySize-1;i++){
			for(j=0;j<2*arraySize-1;j++){
				nodes[i][j] = new Position(i,j,INVALID);
			}
		}

		/* For each cell in the board */
		for(i=0;i<2*arraySize-1;i++){
			for(j=Math.max(0, i-arraySize+1); j< Math.min(arraySize+i, 2*arraySize-1) ;j++){
				if(i==0 || i== 2*arraySize-2
						|| j==Math.max(0, i-arraySize+1)
						|| j==Math.min(arraySize+i, 2*arraySize-1)-1){
					/* Skip (Used to initialise invalid, but doing that for all non-board cells now)
					 * Should update loop to ignore these cells at some point */
				}else{
					if(sc.hasNext()){
						/* Otherwise add a new position with colour given by the input */
						int color=0;
						if(sc.next().charAt(0) == '-'){
							color = EMPTY;
						} else if(sc.next().charAt(0) == 'B'){
							color = BLACK;
						} else if(sc.next().charAt(0) == 'W'){
							color = WHITE;
						} else {
							System.out.println("Syntax Error");
							System.exit(1);
						}
						nodes[i][j] = new Position(i,j,color);
					} else {
						System.out.println("Error");
						System.exit(1);
					}
				}
			}
		}
		sc.close();
	}
	
	/* Creates a new empty board, with sidelength size */
	public Board (int size){
		/* Initialise instance variables */
		clusters = new ArrayList<Cluster>();	
		int i, j;
		
		/* Get size from input */
		this.size = size;

		
		/* Set arraysize, initialise array */
		this.arraySize = size+1;
		nodes = new Position[2*arraySize-1][2*arraySize-1];
 
		/* For each cell in the array, initialise to INVALID piece */
		for(i=0;i<2*arraySize-1;i++){
			for(j=0;j<2*arraySize-1;j++){
				nodes[i][j] = new Position(i,j,INVALID);
			}
		}

		/* For each cell in the board */
		for(i=1;i<2*arraySize-2;i++){
			for(j=Math.max(1, i-arraySize+2); j< Math.min(arraySize+i-1, 2*arraySize-2) ;j++){
					nodes[i][j] = new Position(i,j,EMPTY);
			}
		}
	}
	
	public ArrayList<Cluster> getClusters() {
		return clusters;
	}

	public void setClusters(ArrayList<Cluster> clusters) {
		this.clusters = clusters;
	}

	/* Takes the board state, determines individual groups of pieces */
	public void makeClusters(){
		int i, j;
		Position node;
		for(i=0;i<2*arraySize-1;i++){
			for(j=Math.max(0, i-arraySize+1);j< Math.min(arraySize+i, 2*arraySize-1);j++){
				node = nodes[i][j];
				if(node.getParentCluster() == null
						&& node.getColour()!='-' && node.getColour()!='O'){
					// Node is not in a cluster and is not empty
					fillCluster(node);
				}
			}
		}
	}
	
	/* Given a piece, finds all connected pieces of the same colour,
	 * creates a cluster object containing them
	 * 
	 * Method: Initially adds the node it is passed to the cluster.
	 * Populates the toAdd list with all the adjacent nodes.
	 * The first adjacent node is popped off, if it is the right colour,
	 * and not already in a cluster, it is added to the cluster, and all its
	 * adjacent nodes are added to the list.
	 * Repeat until list is empty.
	 */
	public void fillCluster(Position node){
		Cluster newCluster = new Cluster(node.getColour());
		ArrayList<Position> toAdd = new ArrayList<Position>(0);
		Position tempNode;
		
		newCluster.addNode(node);
		node.setParentCluster(newCluster);
		toAdd.addAll(node.getAdjacents(this));
		
		while(!toAdd.isEmpty()){
			tempNode = toAdd.remove(0);
			if(tempNode.getParentCluster()==null &&
					tempNode.getColour()==node.getColour()){
				newCluster.addNode(tempNode);
				tempNode.setParentCluster(newCluster);
				toAdd.addAll(tempNode.getAdjacents(this));
			}
		}
		clusters.add(newCluster);
	}
	
	public void clearClusters(){
		int i, j;
		for(i=1;i<2*arraySize-2;i++){
			for(j=Math.max(1, i-arraySize+2); j< Math.min(arraySize+i-1, 2*arraySize-2) ;j++){
				nodes[i][j].setParentCluster(null);
			}
		}
		this.clusters.clear();
	}
	
	public int getNumPieces(){
		int i, j, count=0;
		Position node;
		for(i=0;i<2*arraySize-1;i++){
			for(j=Math.max(0, i-arraySize+1);j< Math.min(arraySize+i, 2*arraySize-1);j++){
				node = nodes[i][j];
				if(node.getColour() == 'B' || node.getColour()=='W'){
					count++;
				}
			}
		}
		return count;
	}
	
	public int getSize() {
		return size;
	}


	public void setSize(int size) {
		this.size = size;
	}


	public Position[][] getNodes() {
		return nodes;
	}


	public void setNodes(Position[][] nodes) {
		this.nodes = nodes;
	}
	
	public void setNode(int Piece, int Row, int Col){
		this.nodes[Col][Row].setColour(Piece);
	}
	
	public int getArraySize() {
		return arraySize;
	}

	public void setArraySize(int arraySize) {
		this.arraySize = arraySize;
	}
}
