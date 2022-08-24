package meanestSheep;

public class Node {
//Node is comprised of variables x and y which represent location on the grid
	int row;
	int col;
	int x = 0;
	boolean blocked = false;
	
	//Node constructor
	public Node(int row, int col) {
		this.row = row;
		this.col = col;
	}
	
}
