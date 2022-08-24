package meanestSheep;

import java.util.ArrayList;
import java.util.Random;

import javafx.util.Pair;

import java.awt.Point;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
public class Grid {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Grid g = new Grid();
		//Generate a 51 x 51 grid
		Node[][] grid = g.generate_Grid();
		//Create sheep and sheepdog bot objects for Meanest Sheep
		AngrySheep sheep = new AngrySheep(0,0);
		SheepdogBot bot = new SheepdogBot(0,0);


 
		// valueIteration(grid, sheep, bot);
		//generatePolicy(grid, sheep, bot);
		
		trapSheep(grid, sheep, bot);

	}

	public static void generatePolicy(Node[][] grid, AngrySheep sheep, SheepdogBot bot) {
		System.out.println("Start Generate Policy");
		//Loading the file which contains the minimum T*(State) for every state
		HashMap<Key, Double> states = loadinitialestimates();
		System.out.println("Done loading Estimated values");
		//policy states the state and returns the best possible next move for the bot depending on the current state (sheep_location, bot_location)
		HashMap<Key, Node> policy = new HashMap<Key, Node>();
		double beta = 0.98;
		
		int count = 1;
		//Iterate through all states and determine which node to go to is the best for bot
		for(Key state: states.keySet()) {
			System.out.println(count++ + " / " + states.keySet().size());
			Point s = state.key1;
			//initialize the position of the sheep
			sheep.row = s.x;
			sheep.col = s.y;
			//Initialize the position of the bot
			Point b = state.key2;
			bot.row = b.x; 
			bot.col = b.y;
			//There is program end when the two following conditions are met, so there is not possible next Node for these cases
			if(sheep.row == bot.row && sheep.col == bot.col) {
				continue;
			}
			else if((sheep.row == 26 && sheep.col == 26) && !(bot.row == 26 && bot.col == 26)) {
				continue;
			}
		  //x,y to update the locations of the bot in the algorithm
			int x = bot.row;
			int y = bot.col;
			//initial min value is set to state in which we start out from 
			Key initial = new Key(s,b);
			double finalmin = states.get(initial);
			//get neighbors of both the sheep and the bot
			getBotNeighbors(grid,bot);
			getSheepNeighbors(grid, sheep);
			if(sheep.view.contains(bot)) {
				modSheepNeighbors(grid, sheep, bot);
			}
			//for all the neighbors of the bot iterate through every possible location in which the sheep may go and update the T*(State) to minimum possible value
			//And at the end of iteration set the policy for the state to be the bot neighbor which yielded the min value
			for(Node bot_neighbor: bot.neighbors) {
				double reward = -1;
				double val = 0;
				double sum = 0;
				double prob = 1/(double)sheep.neighbors.size();
				for(Node sheep_neighbor: sheep.neighbors) {

					Point p1 = new Point(sheep_neighbor.row, sheep_neighbor.col);
					Point p2 = new Point(bot_neighbor.row, bot_neighbor.col);
					Key entry = new Key(p1,p2);
					sum += states.get(entry) * prob;

				}
				val = reward + (beta * sum);
				//if val is lesser than the finalmi set val to the finalmin and update the x,y values to the state of bot_neighbor
				if(val < finalmin) {
					finalmin = val;
					x = bot_neighbor.row;
					y = bot_neighbor.col;
				}
			}
			policy.put(state, grid[x][y]);
		}
		System.out.println("Writing Policy");
		//writepolicy to a file in order to be used in trapsheep 
		writePolicy(policy);

	}

	public static void valueIteration(Node[][] grid, AngrySheep sheep, SheepdogBot bot) {
		//assign T* estimated values
		System.out.println("Start Value Iteration");
		HashMap<Key, Double> prev = estimatedTvalues(grid);
		System.out.println("Done loading Estimated values");
		HashMap<Key, Double> curr = new HashMap<Key, Double>();

		double beta = 0.98;
		boolean convergence = false;
		double smallval = 0.01;
		int i = 0;
		//if the max difference between prev and curr falls below 0.01 we can say that the values have converged and we can exit the algorithm 
		while(!convergence) {
			int count = 1;
			double max = -100;
           // for all the states in previous iterate through the valueiteration algorithm to find the min value of each state
			for(Key keys: prev.keySet()) {
				//keeps track of completion within each cycle of the algorithm 
				System.out.println(count++ + " / " + prev.keySet().size());
				//sheep position
				Point s = keys.key1;
				sheep.row = s.x;
				sheep.col = s.y;
				//bot position
				Point b = keys.key2;
				bot.row = b.x; 
				bot.col = b.y;

				//if the sheep and the bot occupy the same location initialize state to infinity else if goal state initialize to 0
				if((sheep.row == bot.row && sheep.col == bot.col) ) {
					curr.put(keys, Double.POSITIVE_INFINITY);
					continue;
				}
				else if((sheep.row == 26 && sheep.col == 26) && !(bot.row == 26 && bot.col == 26)) {
					curr.put(keys, (double)0);
					continue;
				}
				Key initial = new Key(s,b);
				//finalmin is set to the value of the starting state
				double finalmin = prev.get(initial);
				//get neighbors for both the sheep and bot
				getBotNeighbors(grid,bot);
				getSheepNeighbors(grid, sheep);
				if(sheep.view.contains(bot)) {
					modSheepNeighbors(grid, sheep, bot);
				}
				//for every bot_neighbor in combination with the average of every possible sheep location use value-iteration algorithm to get minimum T*(State)
				for(Node bot_neighbor: bot.neighbors) {
				
					double reward = -1;
					double val = 0;
					double sum = 0;
					double prob = 1/(double)sheep.neighbors.size();
					//Determine the average T*(State) of all possible next steps of sheep
					for(Node sheep_neighbor: sheep.neighbors) {

						Point p1 = new Point(sheep_neighbor.row, sheep_neighbor.col);
						Point p2 = new Point(bot_neighbor.row, bot_neighbor.col);
						Key entry = new Key(p1,p2);
						sum += prev.get(entry) * prob;
					}
					
					val = reward + (beta * sum);
					//if val is less than the finalmin then set val to the finalmin
					if(val < finalmin) {
						finalmin = val;
					}
				}
				//Put the finalmin in current
				curr.put(keys, finalmin);
				//On each iteration get the curr val and prev val, calculate the absolute difference between the two and get the max difference, if the max is less than smallval (0.01) set convergence to true
				double old = prev.get(keys);
				double updated = curr.get(keys);
				//max is listed as -100
				if(Math.abs(old - updated) > max ) {
					max = Math.abs(old - updated);
				}

			}
			//Print max difference between curr and prev
			System.out.println("max: " + max);
			if(max < smallval) {
				convergence = true;
			}
			//Saves curr to a separate file for later use
			method1(curr);
			//set curr to prev and if convergence is false begin again else end of valueiteration 
			prev.putAll(curr);
		}
		System.out.println("Value Iteration Complete!");
	}
	
	//If the bot is view of the sheep we modify the neighbors for the valueiteration and generatepolicy algorithm to give more informed results
	public static void modSheepNeighbors(Node[][] grid, AngrySheep sheep, SheepdogBot bot) {
		int mindist = Integer.MAX_VALUE;
		for(Node neighbor: sheep.neighbors) {
			int val	= Math.abs(bot.row - neighbor.row) + Math.abs(bot.col - neighbor.col);
			if(val < mindist) {
				sheep.neighbors.clear();
				sheep.neighbors.add(neighbor);
			}
			else if(val == mindist) {
				sheep.neighbors.add(neighbor);
			}
		}
		
	}
//Grabs the initial estimated T* Values based on a heuristic that uses the Manhattan Distance from the bot to the sheep and then from the sheep to the goal plus 2 to take into account the barrier of the sheep pen
	public static HashMap<Key, Double> estimatedTvalues(Node[][] grid) {
		HashMap<Key, Double> prev = new HashMap<Key, Double>();
		for(int i = 0; i < 51 ; i++) {
			for(int j = 0; j < 51 ; j++) {
				for(int m = 0; m < 51 ; m++) {
					for(int n = 0; n < 51 ; n++) {
						//sheep position
						Point p1 = new Point(i, j);
						//bot position 
						Point p2 = new Point(m, n);
						Key key = new Key(p1, p2);
						//goal states
						if((p1.x == 26 && p1.y == 26) && !(p2.x == 26 && p2.y == 26) ) {
							prev.put(key, (double) 0);
						}else if (p1.x == p2.x && p1.y == p2.y) {//sheep and bot are at the same positon 
							prev.put(key, Double.POSITIVE_INFINITY);	
						}
						else {
							double estimate =  Math.abs(p2.y-p1.y) + Math.abs(p2.x - p1.x)+ Math.abs(p1.x - 26) + Math.abs(p1.y - 26) + 2;
							prev.put(key, estimate);
						}
					}
				}
			}
		}
		return prev;
	}
	//Method used to catch the sheep
	public static void trapSheep(Node[][] grid, AngrySheep sheep, SheepdogBot bot) {
		System.out.println("Loading Policy... ");
		//Load the Policy for the bot
		HashMap<Key, Node> policy = loadPolicy(grid);
		//generate random sheep and bot positions
		generateSheepPosition(grid, sheep);
		generateSheepdogPosition(grid, bot);
		int startbotrow = bot.row;
		int startbotcol = bot.col;
		int startsheeprow = sheep.row;
		int startsheepcol = sheep.col;
		
		//keeps track of the number of steps
		int steps = 1;
		//while true run until the sheep is captured at location (26,26) or the sheep crushes the bot
		while(true) {
			Point p1 = new Point(sheep.row, sheep.col);
			Point p2 = new Point(bot.row, bot.col);
			//create the current state
			Key key = new Key(p1,p2);
			//based on the state retrieve the policy for the next postion of bot
			Node next = policy.get(key);
			bot.row = next.row;
			bot.col = next.col;
			sheepMove(grid, sheep, bot);
			System.out.println("step: " + steps++ +" botrow: " + bot.row + " botcol: " + bot.col + " sheeprow: " + sheep.row + " sheepcol " + sheep.col);
			//if sheeplocation == botlocation the bot is considered crushed
			if(sheep.row == bot.row && sheep.col == bot.col) {
				System.out.println("Robot Crushed!");
				System.out.println("startbotrow: " + startbotrow + " startcolrow: " + startbotcol + " startsheeprow: " + startsheeprow + " startsheepcol " + startsheepcol);
				return;
			}
			//if the sheep row and col is at pos(26,26) we have successfully trapped the sheep
			if(sheep.row == 26 && sheep.col == 26) {
				System.out.println("Successfully trapped sheep!");
				System.out.println("startbotrow: " + startbotrow + " startcolrow: " + startbotcol + " startsheeprow: " + startsheeprow + " startsheepcol " + startsheepcol);
				return;
			}
			
		}
	} 

//generate the grid
	public Node[][] generate_Grid() {
		Node[][] grid = new Node[51][51];
		populate_Grid(grid);
		return grid;
	}
//populate the grid with Node objects that contain row and col vals
	public void populate_Grid(Node[][] grid) {
		for(int i = 0; i < grid.length; i++) {
			for(int j = 0; j < grid[i].length; j++) {		
				grid[i][j] = new Node(i,j);
			}	
		}	
		//Iniitalize blocked nodes to keep simulate the pen of the sheep
		grid[26][25].blocked = true;
		grid[26][27].blocked = true;
		grid[25][25].blocked = true;
		grid[25][27].blocked = true;
		grid[27][25].blocked = true;
		grid[27][27].blocked = true;
		grid[27][26].blocked = true;
	}

	//Generate random sheep position, if initial position is blocked Node then generate another position 
	public static void generateSheepPosition(Node[][] grid, AngrySheep sheep) {
		Random rand = new Random();
		int row = rand.nextInt(51);
		int col = rand.nextInt(51);
		while(grid[row][col].blocked) {
			row = rand.nextInt(51);
			col = rand.nextInt(51);
		}
		sheep.row = row;
		sheep.col = col;
		grid[row][col] = sheep;
	}
	//Generate random sheepdog position, if initial position is blocked Node then generate another position
	public static void generateSheepdogPosition(Node[][] grid, SheepdogBot bot) {
		Random rand = new Random();
		int row = rand.nextInt(51);
		int col = rand.nextInt(51);
		while(grid[row][col].blocked || (grid[row][col] instanceof AngrySheep ) ) {
			row = rand.nextInt(51);
			col = rand.nextInt(51);
		}
		bot.row = row;
		bot.col = col;
		grid[row][col] = bot;
	}
	//Generate a 5x5 sheepview centered on the sheep's positon 
	private static void generateSheepView(Node[][] grid, AngrySheep sheep) {
		int vrow = sheep.row - 2;
		int vcol = sheep.col - 2;
		int vrowend = vrow + 5;
		int vcolend = vcol + 5;
		if(sheep.view != null) {
			sheep.view.clear();
		} 

		//set vrow and vcol to the top corner of the 5x5 view and work our way down to vrowend, vcolend
		while(vrow < vrowend) { 
			while(vcol < vcolend) {
				//if dimensions are out of bounds skip
				if(vrow < 0 || vcol < 0 || vrow > 50 || vcol > 50) {
					vcol++;
					continue;	
				}
				if(sheep.row == vrow && sheep.col == vcol) {
					vcol++;
					continue;
				}
				sheep.view.add(grid[vrow][vcol]);
				vcol++;
			}
			vcol = sheep.col - 2;
			vrow++;
		}	
	}

	//get neighbors of the sheep, including self because the sheep can also decide to stay put
	public static void getSheepNeighbors(Node[][] grid, AngrySheep sheep ) {
		if(!sheep.neighbors.isEmpty()) {
			sheep.neighbors.clear();
		}
		int row = sheep.row;
		int col = sheep.col;

		sheep.neighbors.add(sheep);

		if(row - 1 >= 0) {
			sheep.neighbors.add(grid[row - 1 ][col]);
		}
		if(row + 1 <= 50) {
			sheep.neighbors.add(grid[row + 1][col]);
		}
		if(col - 1 >= 0) {
			sheep.neighbors.add(grid[row][col - 1]);
		}
		if(col + 1 <= 50) {
			sheep.neighbors.add(grid[row][col + 1]);
		}
	}
	
	//get neighbors of the bot, including self because the bot can also decide to stay put
	public static void getBotNeighbors(Node[][] grid, SheepdogBot bot ) {
		if(!bot.neighbors.isEmpty()) {
			bot.neighbors.clear();
		}
		int row = bot.row;
		int col = bot.col;

		bot.neighbors.add(bot);

		if(row - 1 >= 0 && !grid[row - 1 ][col].blocked) {
			bot.neighbors.add(grid[row - 1 ][col]);
		}
		if(row + 1 <= 50 && !grid[row + 1][col].blocked) {
			bot.neighbors.add(grid[row + 1][col]);
		}
		if(col - 1 >= 0 && !grid[row][col - 1].blocked) {
			bot.neighbors.add(grid[row][col - 1]);
		}
		if(col + 1 <= 50 && !grid[row][col + 1].blocked) {
			bot.neighbors.add(grid[row][col + 1]);
		}
		if (row - 1 >= 0 && col - 1 >= 0 && !grid[row - 1][col - 1].blocked) {
			bot.neighbors.add(grid[row - 1][col - 1]);
		}
		if (row - 1 >= 0 && col + 1 <= 50 && !grid[row - 1][col + 1].blocked) {
			bot.neighbors.add(grid[row - 1][col + 1]);
		}
		if (row + 1 <= 50 && col + 1 <= 50 && !grid[row + 1][col + 1].blocked) {
			bot.neighbors.add(grid[row + 1][col + 1]);
		}
		if (row + 1 <= 50 && col - 1 >= 0 && !grid[row + 1][col - 1].blocked) {
			bot.neighbors.add(grid[row + 1][col - 1]);
		}

	}
	//Algorithm that determines how the sheep moves
	private static void sheepMove(Node[][] grid, AngrySheep sheep, SheepdogBot bot) {
		Node move;
		ArrayList<Node> attack = new ArrayList<Node>();
		Random rand = new Random();
		int index = 0;
		getSheepNeighbors(grid, sheep);
		generateSheepView(grid, sheep);
		//if the bot is in the sheep's view the sheep will take the move to reduce its distance to the bot
		if(sheep.view.contains(bot)) {
			int mindist = Integer.MAX_VALUE;
			for(Node neighbor: sheep.neighbors) {
				int val	= Math.abs(bot.row - neighbor.row) + Math.abs(bot.col - neighbor.col);
				if(val < mindist) {
					attack.clear();
					attack.add(neighbor);
				}
				else if(val == mindist) {
					attack.add(neighbor);
				}
			}
			index = rand.nextInt(attack.size());
			move = attack.get(index);
			if(!grid[move.row][move.col].blocked) {
			sheep.row = move.row;
			sheep.col = move.col;
			}
		}//else the sheep moves randomly amongst its neighbors
		else {
			index = rand.nextInt(sheep.neighbors.size());
			move = sheep.neighbors.get(index);
			if(!grid[move.row][move.col].blocked) {
			sheep.row = move.row;
			sheep.col = move.col;
			} 

		}
	}

	//method1 writes T*(State) to a .txt file
	public static void method1(HashMap<Key,Double> map) {

		try {
			File fileTwo=new File("initial.txt");
			FileOutputStream fos=new FileOutputStream(fileTwo);
			PrintWriter pw=new PrintWriter(fos);

			for(Map.Entry<Key,Double> m :map.entrySet()){
				int x1 = m.getKey().key1.x;
				int y1 = m.getKey().key1.y;
				int x2 = m.getKey().key2.x;
				int y2 = m.getKey().key2.y;
				pw.println(x1 + " " + y1 + " " +  x2 + " " + y2 + " " + m.getValue());
			}

			pw.flush();
			pw.close();
			fos.close();
		} catch(Exception e) {}

	}
	//method1 writes the policy obtained by generatePolicy to a .txt file
	public static void writePolicy(HashMap<Key,Node> map) {

		try {
			File initialTval =new File("policy.txt");
			FileOutputStream fos=new FileOutputStream(initialTval);
			PrintWriter pw=new PrintWriter(fos);

			for(Map.Entry<Key,Node> m :map.entrySet()){
				int x1 = m.getKey().key1.x;
				int y1 = m.getKey().key1.y;
				int x2 = m.getKey().key2.x;
				int y2 = m.getKey().key2.y;
				int bot_row = m.getValue().row;
				int bot_col = m.getValue().col;
				pw.println(x1 + " " + y1 + " " +  x2 + " " + y2 + " " + bot_row + " " + bot_col);
			}

			pw.flush();
			pw.close();
			fos.close();
		} catch(Exception e) {}

	}
	//loadinitialestimates loads the data from the T*(State) into a hashmap<Key,Double> which is returned to either valueIteration or generatePolicy  
	public static HashMap<Key, Double> loadinitialestimates() {
		try {
			File toRead=new File("initial.txt");
			FileInputStream fis=new FileInputStream(toRead);

			Scanner sc = new Scanner(fis);

			HashMap<Key,Double> mapInFile = new HashMap<Key,Double>();

			//read data from file line by line:
			String currentLine;
			while(sc.hasNextLine()) {
				currentLine=sc.nextLine();
				//now tokenize the currentLine:
				StringTokenizer st= new StringTokenizer(currentLine," ",false);
				int x1 = Integer.parseInt(st.nextToken());
				int y1 = Integer.parseInt(st.nextToken());
				int x2 = Integer.parseInt(st.nextToken());
				int y2 = Integer.parseInt(st.nextToken());
				double val = Double.parseDouble(st.nextToken());
				Key key = new Key(new Point(x1, y1), new Point(x2,y2));
				//put tokens on currentLine in map
				mapInFile.put(key,val);
			}
			fis.close();
			return mapInFile;
		}catch(Exception e) {

		}
		return null;	
	}
	//Loads the Policy from generatedPolicy to be used in TrapSheep to guide the movements of the bot
	public static HashMap<Key, Node> loadPolicy(Node[][] grid) {
		try {
			File toRead=new File("policy.txt");
			FileInputStream fis=new FileInputStream(toRead);

			Scanner sc = new Scanner(fis);

			HashMap<Key,Node> mapInFile=new HashMap<Key,Node>();

			//read data from file line by line:
			String currentLine;
			while(sc.hasNextLine()) {
				currentLine=sc.nextLine();
				//now tokenize the currentLine:
				StringTokenizer st= new StringTokenizer(currentLine," ",false);
				int x1 = Integer.parseInt(st.nextToken());
				int y1 = Integer.parseInt(st.nextToken());
				int x2 = Integer.parseInt(st.nextToken());
				int y2 = Integer.parseInt(st.nextToken());
				int bot_row = Integer.parseInt(st.nextToken());
				int bot_col = Integer.parseInt(st.nextToken());
				Key key = new Key(new Point(x1, y1), new Point(x2,y2));
				//put tokens on currentLine in map
				mapInFile.put(key, grid[bot_row][bot_col]);
			}
			fis.close();
			return mapInFile;

		}catch(Exception e) {

		}
		return null;	
	}

}




