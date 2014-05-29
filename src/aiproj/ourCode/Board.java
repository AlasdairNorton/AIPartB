package aiproj.ourCode;

/* Alasdair Norton (ajnorton)
 * Mostafa Rizk (mrizk) */
import java.util.ArrayList;
import java.util.Collections;
import java.util.PriorityQueue;

import aiproj.fencemaster.Move;
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
	private ArrayList<Cluster> clustRemoveList;




	/* Creates a new empty board, with sidelength size */
	public Board (int size){
		/* Initialise instance variables */
		clusters = new ArrayList<Cluster>();	
		clustRemoveList = new ArrayList<Cluster>(0);
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
						&& node.getColour()!=EMPTY && node.getColour()!=INVALID){
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
	

	public void mergeClusters(Cluster c1, Cluster c2){
		c1.setDidChange(true);
		c1.getNodes().addAll(c2.getNodes());
		for(Position p: c2.getNodes()){
			p.setParentCluster(c1);
		}
		clustRemoveList.add(c2);
	}
	
	public void updateNearMove(Move m){
		Position p = nodes[m.Row+1][m.Col+1];
		Cluster currCluster = new Cluster(p.getColour());
		currCluster.addNode(p);
		p.setParentCluster(currCluster);
		clusters.add(currCluster);
		for(Position adj: p.getAdjacents(this)){
			if(adj.getColour()==p.getColour()){
				mergeClusters(adj.getParentCluster(), currCluster);
				currCluster = adj.getParentCluster();
			}
		}
		
		for(Cluster c: clustRemoveList){
			clusters.remove(c);
		}
		clustRemoveList.clear();
		
		p.setParentCluster(currCluster);
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
				if(node.getColour() == BLACK || node.getColour()==WHITE){
					count++;
				}
			}
		}
		return count;
	}
	
	public int testWin(){
		// Taken from Controller.java, from Part A
		Boolean[] win = {false, false, false, false};	
		for(Cluster clust: this.getClusters()){
			/* If the cluster has updated since the last check for win.
			 * didChange is always true for all clusters in the players'
			 * board representation, so every cluster is checked there
			 * but in the minimax search, only clusters immediately affected
			 * by a change are searched.
			 */
			if(clust.getDidChange()){
				/* For each cluster, test win conditions */
				if(clust.getColour() == BLACK){
					if(clust.testTripod(this)){
						win[0]=true;
					}
					
					if(clust.testLoop(this)){
						win[1]=true;
					}
				}
				if(clust.getColour() == WHITE){
					if(clust.testTripod(this)){
						win[2]=true;
					}
					
					if(clust.testLoop(this)){
						win[3]=true;
					}
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
		this.nodes[Row][Col].setColour(Piece);
	}
	
	public int getArraySize() {
		return arraySize;
	}

	public void setArraySize(int arraySize) {
		this.arraySize = arraySize;
	}
	
}
