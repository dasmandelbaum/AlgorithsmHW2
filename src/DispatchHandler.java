import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;
import java.util.Stack;

/**
 * This class handles dealing with the graph, parsing input, deligating to other classes.
 * Contains the main method for the program. Program should be run from here.
 * Example command line entry to start program: java DispatchHandler /Users/jennym/Desktop/AlgorithmsHW2/hw2test.txt
 * Reference and source: http://stackoverflow.com/questions/8542523/adjacency-list-for-undirected-graph
 */
public class DispatchHandler {
	private int count;
	CityGraph graph;
	private PatientQueue patientQueue;
	private Queue<EmsUnit> emsTeams;
	protected HashMap<Vertex, Vertex> brokenRoads;
	protected ArrayList<Vertex> unpassables;
	private boolean groupDispatchDone;
	protected int repairTeams;

	/*
	 * Constructor
	 */
	public DispatchHandler()
	{
		setCount(0);
		graph = new CityGraph();
		patientQueue = new PatientQueue(10, PatientSort.SEVERITY_ORDER);
		emsTeams = new LinkedList<EmsUnit>();
		groupDispatchDone = false;
		brokenRoads = new HashMap<Vertex, Vertex>();
		unpassables = new ArrayList<Vertex>();
		repairTeams = 0;
	}

	/*
	 * Main method for the program
	 * Example command line entry to start program: java DispatchHandler /Users/jennym/Desktop/AlgorithmsHW2/hw2test.txt
	 */
	public static void main(String[] args) throws Exception
	{
		DispatchHandler dHandler = new DispatchHandler();
		try {
			//source:http://stackoverflow.com/questions/22847805/passing-file-path-as-an-argument-in-java
			File inFile = new File(args[0]);//"hw2test.txt");////example command line entry to start program: java DispatchHandler /Users/jennym/Desktop/AlgorithmsHW2/hw2test.txt
			Scanner br = new Scanner (inFile);
			//String line = br.readLine();
			while (br.hasNextLine()) {//http://www.cs.utexas.edu/~mitra/csSummer2012/cs312/lectures/fileIO.html
				String line = br.nextLine();
				dHandler.parseInput(line);
				//line = br.nextLine();
			}
			br.close();
		}
		catch(Exception e) {
			throw new Exception("Exception occured while parsing/need to enter proper file path: " + e.getMessage());
		}
	}

	/*
	 * Parse the input from the file
	 */
	public void parseInput(String input) throws Exception
	{
		if(input.contains(","))//assuming a comma would not be part of typical address, must contain IDs
		{
			String[] idInput = input.split(",");
			if(idInput.length == 0)
			{
				throw new Exception("Exception occured while parsing: input line empty");
			}
			else if(idInput.length > 2)
			{
				//must be an intersection
				addNewIntersection(graph, idInput);
			}
			else if(idInput.length == 2)
			{
				//must be a road
				createRoad(graph, idInput);
			}
		}
		else //does not contain commas 
		{
			String[] inputPieces = input.split(" ");
			if(inputPieces.length == 0)
			{
				throw new Exception("Exception occured while parsing: input line empty");
			}
			else if(inputPieces.length > 2) //either a new address, a broken road, or a 911 call
			{
				if(isInt(inputPieces[0]) && isInt(inputPieces[1]) && !isInt(inputPieces[2]))//first section is id followed by address
				{
					addNewAddress(input);
				}
				else if(!isInt(inputPieces[0]) && isInt(inputPieces[1]) && isInt(inputPieces[2]) && !isInt(inputPieces[3]))//must be a broken road
				{
					//broken road
					int houseNumber1 = Integer.parseInt(inputPieces[1]);
					int houseNumber2 = Integer.parseInt(inputPieces[2]);
					String street = inputPieces[3];
					for(int i = 4; i < inputPieces.length; i++)
					{
						street += " " + inputPieces[i];
					}
					insertBrokenRoad(houseNumber1, houseNumber2, street);
				}
				else //must be a 911 call
				{
					addNewPatient(inputPieces);
					if(groupDispatchDone == true)
					{
						dispatch();
					}
				}
			}
			else if(inputPieces.length == 2)
			{
				if(inputPieces[0].equals("repair") && inputPieces[1].equals("team"))//it is a repair team
				{
					repairTeams++;
					System.out.println("Repair team " + repairTeams + " added");
					if(this.brokenRoads != null)
					{
						this.graph.repairRoad(this);
					}
				}
				else if(inputPieces[0].toUpperCase().equals("EMS") && isInt(inputPieces[1]))
				{
					//associate EMS with address with given ID
					ID id = graph.getId(Integer.parseInt(inputPieces[1]));
					EmsUnit ems = new EmsUnit(id.getAddress());
					id.getAddress().emsUnits.add(ems);
					id.getAddress().emsHospCount++;
					this.emsTeams.add(ems);
				}
				else if(inputPieces[0].toLowerCase().equals("hospital") && isInt(inputPieces[1]))
				{
					//associate hospital with address with given ID
					ID id = graph.getId(Integer.parseInt(inputPieces[1]));
					id.getAddress().getHospitals().add(new Hospital(id.getAddress()));
					id.getAddress().emsHospCount++;
				}
			}
			else if(inputPieces.length == 1) //either there were no spaces or it is a one-word input, or 911 call notice (begin/end)
			{
				if(inputPieces[0].toLowerCase().equals("begin911callgroup"))// it is the beginning of events 
				{
					//finalize addresses/edges/group addresses
					finalizeGraph();
					//graphBuilt = true;
					printGraph(this.graph);
				}
				else if(inputPieces[0].toLowerCase().equals("end911callgroup"))
				{
					printGraph(this.graph);
					printPatientQueue();
					dispatch();
					groupDispatchDone = true;
					//begin doing things
				}
			}
		}
	}

	/*
	 * Dispatch the EMS teams to the patients in the queue
	 */
	private void dispatch() throws Exception
	{
		while(!patientQueue.isEmpty())//run dispatch while there are people who need to be healed
		{
			/*for(int i = 0; i < this.emsTeams.size(); i++)//for all ems teams
			{*/
			//printPatientQueue();
			EmsUnit unit = this.emsTeams.remove();//grab an ems team
			System.out.println("\n#################################STARTING_DISPATCH_ROUND#########################################");
			System.out.println("Grabbed ems team from " + unit.address.getId());
			Stack<Vertex> closestPatient = new Stack<Vertex>();
			int smallest = 10000;//arbitrary high number
			Iterator<Patient> queueOfPatientsWaiting = patientQueue.iterator();
			Patient p = new Patient(null, -1);//initialize an empty patient
			Patient pHealed = shortestRouteCheck(p, unit, closestPatient, smallest, queueOfPatientsWaiting);
			//Patient pHealed = unit.treat(this.graph, unit, p, closestPatient);
			if(pHealed == null)//patient was not treated because encountered broken road
			{
				handleBrokenRoad(p, unit);
				break;//right? <------------------------------------------------------------------LOOK HERE
			}
			//Patient pTreated = this.patientQueue.delMax();
			Stack<Patient> helperStack = new Stack<Patient>();
			removeFromQueue(pHealed, helperStack);
			this.emsTeams.add(unit);
			System.out.println("#################################ENDING_DISPATCH_ROUND###########################################");
		}
		//}
		printGraph(this.graph);
		System.out.println("Ending dispatch");
	}

	/*
	 * Find the shortest route from unit to patient
	 */
	private Patient shortestRouteCheck(Patient p, EmsUnit unit, Stack<Vertex> closestPatient, int smallest, Iterator<Patient> queueOfPatientsWaiting) throws Exception {
		for(int j = 0; j < this.emsTeams.size(); j++)//for all patient's waiting
		{
			System.out.println("Testing directions");
			HashMap<Vertex, Boolean> vis = new HashMap<Vertex, Boolean>();//to check if it has been visited
			HashMap<Vertex, Vertex> prev = new HashMap<Vertex, Vertex>();//to record previous
			p = (Patient) queueOfPatientsWaiting.next(); //grab real patient
			Vertex patientLocation = p.getAddress(); //grab patient's location
			Stack<Vertex> directions = getDirections(unit.address, patientLocation, vis, prev); //see how far away the patient is from this ems
			//System.out.println(directions.size() + " vertices to reach destination");
			if(directions.size() < smallest) //see if this patient is the closest so far
			{
				closestPatient = directions;
				smallest = directions.size();
			}
		}
		Patient pHealed = unit.treat(this.graph, unit, p, closestPatient, this);
		return pHealed;
	}

	/*
	 * Remove patient from patient queue
	 */
	private void removeFromQueue(Patient pHealed, Stack<Patient> helperStack) {
		Patient toDelete = new Patient(null, -1);
		while(toDelete != pHealed)//haven't yet removed the patient just healed 
		{
			if(toDelete.getAddress() != null)
			{
				helperStack.push(toDelete);
			}
			toDelete = this.patientQueue.delMax();
			//System.out.println("Removing " + toDelete.getSeverity() + " severity temporarily and adding to stack of " + helperStack.size());
		}
		for(Patient toPutBackIn: helperStack)
		{
			toPutBackIn = helperStack.pop();
			if(toPutBackIn != pHealed)
			{
				//System.out.println("Putting " + toPutBackIn.getSeverity() + " back in the Patient Queue");
				this.patientQueue.insert(toPutBackIn);
			}
		}
		System.out.println("Patient of severity " + toDelete.getSeverity() + " is off the queue");
	}

	/*
	 * Handle the broken road once found
	 */
	private void handleBrokenRoad(Patient p, EmsUnit unit) throws Exception {
		//put patient back in queue
		patientQueue.insert(p);
		//new bfs for ems unit to closest patient amongst the first k patients in the queue - the second part of discover broken road
		Stack<Vertex> closestPatient = new Stack<Vertex>();
		int smallest = 10000;//arbitrary high number
		Iterator<Patient> queueOfPatientsWaiting = patientQueue.iterator();
		shortestRouteCheck(new Patient(null, -1), unit, closestPatient, smallest, queueOfPatientsWaiting);//will this just take us on broken road everytime????
	}

	/*
	 * BFS to the destination.
	 * http://stackoverflow.com/questions/1579399/shortest-path-fewest-nodes-for-unweighted-graph
	 */
	private Stack<Vertex> getDirections(Vertex start, Vertex finish, HashMap<Vertex, Boolean> vis, HashMap<Vertex, Vertex> prev) throws Exception{
		LinkedList<Vertex> directions = new LinkedList<Vertex>();
		Queue<Vertex> q = new LinkedList<Vertex>();
		Vertex current = start;
		/*if(current.adjList == null)// || !current.addresses.contains(finish))//not a group vertex
		{*/
		q.add(current);//place in queue
		vis.put(current, true);//mark as visited
		while(!q.isEmpty())//while there is something in queue
		{
			current = q.remove();//remove the first element of the queue
			//System.out.println("Testing vertex at " + current.getId());
			if (current.equals(finish))//if this is target
			{
				break;
			}
			else if(current.adjList != null && current.adjList.get(finish) != null)//if this is a group vertex, and  destination is vertex within it
			{
				for(Vertex v : current.groupStarter.adjList.get(current))//for all the vertices that this vertex is connected to
				{
					if(vis.get(v) == null)//if has not been visited yet
					{
						q.add(v);//add to the queue
						vis.put(v, true);//mark as visited
						prev.put(v, current);//record the previous vertex
					}
				}
				current = Vertex.addressBSearch(current, finish.houseNumber);//find destination from within group vertex
				break;
			}
			else//have not found target yet
			{
				if(this.graph.adjList.get(current) != null)
				{
					for(Vertex v : this.graph.adjList.get(current))//for all the vertices that this vertex is connected to
					{
						if(vis.get(v) == null)//if has not been visited yet
						{
							q.add(v);//add to the queue
							vis.put(v, true);//mark as visited
							prev.put(v, current);//record the previous vertex
						}
					}
				}
				else if (current.groupStarter != null)
				{
					for(Vertex v : current.groupStarter.adjList.get(current))//for all the vertices that this vertex is connected to
					{
						if(vis.get(v) == null)//if has not been visited yet
						{
							q.add(v);//add to the queue
							vis.put(v, true);//mark as visited
							prev.put(v, current);//record the previous vertex
						}
					}
				}
			}
		}
		if (!current.equals(finish))
		{
			System.out.println("can't reach destination");
			return null;
		}
		//transform list of nodes hit on way into directions
		//System.out.println("" + prev.size());
		for(Vertex v = finish; v != null; v = prev.get(v)) 
		{
			directions.add(v);
		}
		System.out.println("Directions are " + directions.size() + " vertices long");
		//reverse the list to get the correct order for the ems team to follow
		Stack<Vertex> finalDirections = new Stack<Vertex>();
		int size = directions.size();
		for(int i = 0; i < size; i++)
		{
			Vertex v1 = directions.removeFirst();
			finalDirections.push(v1);
		}
		return finalDirections;
	}


	/*
	 * Traverse the graph and group components when necessary, making edges accordingly.  
	 */
	private void finalizeGraph() 
	{
		System.out.println("#################################STARTING_GRAPH_BUILDING###########################################");
		mainForLoop: for (int i = 0; i <= this.graph.ids.size() - 1; i++)// (ID id: this.graph.ids)
		{
			Vertex v = this.graph.ids.get(i).getAddress(); // v is at location i
			//Vertex v2 = this.graph.ids.get(i + 1).getAddress();
			//System.out.println("V = " + v.getId());
			if(!v.isIntersection() && this.graph.ids.get(i + 1).getAddress().isIntersection() && v.getStreet().equals(this.graph.ids.get(i + 1).getAddress().getStreet())) //this vertex is not at an intersection but next one is, and on the same street
			{
				//connect this vertex to intersection next to it
				System.out.println("Connecting ID " + v.getId() + " to intersection "+ graph.ids.get(i + 1).getId());
				graph.adjList.get(v).add(this.graph.ids.get(i + 1).getAddress());
				graph.adjList.get(this.graph.ids.get(i + 1).getAddress()).add(v);
			}
			else if(v.isIntersection() && !this.graph.ids.get(i + 1).getAddress().isIntersection() && v.getStreet().equals(this.graph.ids.get(i + 1).getAddress().getStreet()))//the vertex is at intersection and next one is not, and on the same street
			{
				//connect this intersection to vertex next to it
				System.out.println("Connecting intersection " + v.getId() + " to ID "+ graph.ids.get(i + 1).getId());
				graph.adjList.get(v).add(this.graph.ids.get(i + 1).getAddress());
				graph.adjList.get(this.graph.ids.get(i + 1).getAddress()).add(v);
			}
			else if(!v.isIntersection() && !this.graph.ids.get(i + 1).getAddress().isIntersection() && v.getStreet().equals(this.graph.ids.get(i + 1).getAddress().getStreet()))//this vertex nor the next vertex are intersections
			{//group together all addresses until reach next intersection
				//v.addresses.add(v);
				//groupVertices(v, i);
				v.adjList = new HashMap<Vertex, LinkedList<Vertex>>();
				v.adjList.put(v, new LinkedList<Vertex>());
				v.groupStarter = v;
				while(!this.graph.ids.get(i).getAddress().isIntersection() && v.getStreet().equals(this.graph.ids.get(i).getAddress().getStreet()))
				{
					//System.out.println("ID: " + v.getId() + " Vertex street: " + v.getStreet());
					//System.out.println("ID: " + this.graph.ids.get(i).getId() + " Next vertex street: " + this.graph.ids.get(i).getAddress().getStreet());

					System.out.println("Adding " + graph.ids.get(i).getId() + " to group at " + v.getId());
					v.addresses.add(graph.ids.get(i).getAddress());
					v.adjList.put(graph.ids.get(i).getAddress(), new LinkedList<Vertex>());
					v.adjList.get(v).add(graph.ids.get(i).getAddress());
					v.adjList.get(graph.ids.get(i).getAddress()).add(v);
					graph.ids.get(i).getAddress().groupStarter = v;
					if(this.graph.ids.get(i).getAddress().adjList == null)
					{
						System.out.println("Removing ID " + graph.ids.get(i).getId() + " from main graph");
						this.graph.adjList.remove(graph.ids.get(i).getAddress());//take the vertex entered into group vertex out of main graph
					}
					if(i + 1 == this.graph.ids.size())
					{

						break mainForLoop;
					}
					//---> this.graph.ids.remove(graph.ids.get(i));//take vertex id out of the list of ids
					if(this.graph.ids.get(i).getAddress().getStreet().equals(this.graph.ids.get(i + 1).getAddress().getStreet()) && !this.graph.ids.get(i + 1).getAddress().isIntersection())
					{//if next node has the same street as this one
						System.out.println("Connecting address " + this.graph.ids.get(i).getAddress().getId() + " to ID "+ this.graph.ids.get(i + 1).getId());
						v.adjList.get(this.graph.ids.get(i).getAddress()).add(this.graph.ids.get(i + 1).getAddress());
						v.adjList.put(this.graph.ids.get(i + 1).getAddress(), new LinkedList<Vertex>());
						v.adjList.get(this.graph.ids.get(i + 1).getAddress()).add(this.graph.ids.get(i).getAddress());
					}
					/*if(i + 1 == this.graph.ids.size())
					{
						break mainForLoop;
					}*/
					if(!this.graph.ids.get(i).getAddress().isIntersection() && v.getStreet().equals(this.graph.ids.get(i).getAddress().getStreet()))
					{
						i++;
					}
					//System.out.println("i = " + i);
				}
				//connect last item in group to first item in group, if not already connected
				/*if(!v.adjList.get(v).contains(graph.ids.get(i - 1).getAddress()))
				{*/
				v.adjList.get(v).add(graph.ids.get(i - 1).getAddress());
				System.out.println("Beginning of group at " + v.getId() + " now connected to end of group at " + graph.ids.get(i - 1).getId());
				//}
				v.addresses.add(graph.ids.get(i).getAddress());
				if(this.graph.ids.get(i + 1).getAddress().isIntersection() && this.graph.ids.get(i + 1).getAddress().getStreet().equals(v.getStreet()))//next vertex is an intersection
				{
					//now that next vertex is an intersection, attach this grouping to intersection and intersection to the grouping 
					System.out.println("Creating connection between " + v.getId() + " and " + graph.ids.get(i).getId());
					graph.adjList.get(v).add(this.graph.ids.get(i).getAddress());
					graph.adjList.get(this.graph.ids.get(i).getAddress()).add(v);
				}
				else if(!this.graph.ids.get(i + 1).getAddress().getStreet().equals(v.getStreet()))//next vertex is a different road
				{
					i--; //decrease position so do not skip anywhere
					System.out.println("Continuing to next street...");
				}
			}
			//System.out.println("i = " + i);
		}
		System.out.println("#################################ENDING_GRAPH_BUILDING###########################################\n");
	}

	/*
	 * Print graph as reference (prints IDs)
	 */
	public void printGraph(CityGraph graph)
	{
		/* Prints the adjacency List representing the graph. - http://www.sanfoundry.com/java-program-describe-representation-graph-using-adjacency-list/*/
		System.out.println ("\nThe given Adjacency List for the graph");
		for (ID id: graph.ids)
		{
			Vertex v = id.getAddress();
			if(graph.adjList.get(v) != null)//vertex is still in main graph
			{
				System.out.print(v.getId() +"->");
				if(v.emsUnits.size() > 0)
				{
					System.out.print("(" + v.emsUnits.size() + " EMS unit(s))" +"->");
				}
				if(v.getHospitals().size() > 0)
				{
					System.out.print("(" + v.getHospitals().size() + " Hospital(s))" +"->");
				}
				if(v.patients.size() > 0)
				{
					System.out.print("(" + v.patients.size() + " Patient(s))" +"->");
				}
				LinkedList<Vertex> edgeList = graph.adjList.get(v);
				if(edgeList.size() > 0)
				{
					for (int j = 1 ; ; j++ )
					{
						if (j != edgeList.size() && edgeList.get(j - 1 ).getId() > 0)
						{
							System.out.print(edgeList.get(j - 1 ).getId()+"->");
						}else if(edgeList.get(j - 1 ).getId() > 0)
						{
							System.out.print(edgeList.get(j - 1 ).getId());
							System.out.print("\n");
							break;
						}						 
					}
				}
				else
				{
					System.out.print("\n");
				}
			}
		}
		System.out.println();	
	}


	/*
	 * Print current list of patients in queue, from highest severity to lowest
	 */
	private void printPatientQueue() 
	{
		Iterator<Patient> queueTest = patientQueue.iterator();
		Patient p = (Patient) queueTest.next();
		System.out.println("Patient queue ordered by severity (hi -> lo): ");
		while(queueTest.hasNext())
		{
			System.out.println("Patient severity: " + p.getSeverity());
			p = (Patient) queueTest.next();
		}
		System.out.println("Patient severity: " + p.getSeverity());//print the last patient of lowest severity
	}

	/*
	 * Insert broken road to graph
	 */
	private void insertBrokenRoad(int houseNumber1, int houseNumber2, String street) 
	{
		System.out.println("Input is a broken road");
		//build up street name
		
		Vertex v1 = this.graph.getVertex(houseNumber1, street);
		Vertex v2 = this.graph.getVertex(houseNumber2, street);
		//Create broken road
		this.brokenRoads.put(v1, v2);
		this.brokenRoads.put(v2, v1);
	}

	/*
	 * Insert new patient from input
	 */
	private void addNewPatient(String[] inputPieces) throws Exception 
	{
		int houseNumber = Integer.parseInt(inputPieces[0]);
		String streetName = inputPieces[1];
		int severity = Integer.parseInt(inputPieces[inputPieces.length - 1]);//last item is severity
		for(int i = 2; i < inputPieces.length - 1; i++)//adds the pieces of address back together until reach severity number
		{
			streetName = streetName + " " + inputPieces[i];
		}
		Vertex address = graph.getVertex(houseNumber, streetName);
		Patient patient = new Patient(address, severity);
		address.patients.add(patient);
		//For Group vertices: Vertex address = addressBSearch(graph.getVertexGroup(houseNumber, streetName), houseNumber);
		patientQueue.insert(patient);
	}

	/*
	 * Add new patient from input
	 */
	private static void addNewIntersection(CityGraph graph, String[] idInput) 
	{
		ID id1 = graph.getId(Integer.parseInt(idInput[0]));
		Vertex v1 = id1.getAddress();
		ID id2 = graph.getId(Integer.parseInt(idInput[1]));
		Vertex v2 = id2.getAddress();
		ID id3 = graph.getId(Integer.parseInt(idInput[2]));
		Vertex v3 = id3.getAddress();
		ID id4 = graph.getId(Integer.parseInt(idInput[3]));
		Vertex v4 = id4.getAddress();
		graph.getAdjList().get(v1).add(v2); //add to adjacency list
		graph.getAdjList().get(v1).add(v3);
		graph.getAdjList().get(v1).add(v4);
		graph.getAdjList().get(v2).add(v1);
		graph.getAdjList().get(v2).add(v3);
		graph.getAdjList().get(v2).add(v4);
		graph.getAdjList().get(v3).add(v1);
		graph.getAdjList().get(v3).add(v2);
		graph.getAdjList().get(v3).add(v4);
		graph.getAdjList().get(v4).add(v1);
		graph.getAdjList().get(v4).add(v2);
		graph.getAdjList().get(v4).add(v3);
		v1.setIntersection(true);
		v2.setIntersection(true);
		v3.setIntersection(true);
		v4.setIntersection(true);
		//Vertex intersection = new Vertex(v1, v2, v3, v4); //make intersection vertex
		//intersection.setId(graph.ids.size() + 1);
		//graph.adjList.put(intersection, new LinkedList<Vertex>());
		//graph.ids.add(intersection.getId(), graph.getId(intersection.getId()));
	}

	/*
	 * Add new road from input
	 */
	private static void createRoad(CityGraph graph, String[] idInput) 
	{
		ID id1 = graph.getId(Integer.parseInt(idInput[0]));
		Vertex v1 = id1.getAddress();
		ID id2 = graph.getId(Integer.parseInt(idInput[1]));
		Vertex v2 = id2.getAddress();
		graph.getAdjList().get(v1).add(v2); //add to adjacency list
		graph.getAdjList().get(v2).add(v1);
	}
	
	/*
	 * Add a new address to the graph. Create new graph if none exists.
	 */
	private void addNewAddress(String input) 
	{
		Vertex newAddress = new Vertex(input);
		ID newID = new ID(newAddress);
		this.graph.ids.add(newID);
		this.graph.getAdjList().put(newAddress, new LinkedList<Vertex>());
		this.setCount(this.getCount() + 1);
	}

	/*
	 * Test to see if the element is an integer
	 * @param newElement to test if it is an int
	 * @return true if newElement is an int
	 */
	static boolean isInt(String newElement) //{http://stackoverflow.com/questions/1102891/how-to-check-if-a-string-is-numeric-in-java
	{
		try  
		{  
			@SuppressWarnings("unused")
			int i = Integer.parseInt(newElement);  //is int
		}  
		catch(NumberFormatException nfe)  //is not int
		{  
			return false;  
		}  
		return true;  
	}

	/*
	 * Return count
	 */
	public int getCount() {
		return count;
	}

	/*
	 * Set count
	 */
	public void setCount(int count) {
		this.count = count;
	}
}
