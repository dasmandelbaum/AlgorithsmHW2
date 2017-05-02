import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * This program contains a graph of a city containing addresses, people/hospitals/ems units that reside as those addresses
 * along with the roads between them. The addresses function as vertices (often compound vertices) and the roads are edges.  
 * There is a dispatch algorithm to send the ems units to people who need help, and a broken road fixing algorithm.
 * Broken road repair was worked on but not completed as per email with Judah regarding using compound vertices instead.
 * Hashmaps were implemented before it was advised against doing so.
 * The graph is composed of an adjacency list.
 * Main method for program is in DispatchHandler class. 
 * Reference: http://stackoverflow.com/questions/14783831/java-implementation-of-adjacency-list and
 * http://stackoverflow.com/questions/8542523/adjacency-list-for-undirected-graph
 * @author David Mandelbaum
 * @version 3/25/17 
 */
public class CityGraph
{
	protected Map<Vertex, LinkedList<Vertex>> adjList; //adjacency list to hold edges between vertices
	protected ArrayList<ID> ids;
	protected int componentCount;
	protected boolean[] marked;
	private int[] componentIds;
	//private int[] hospitalCount;
	private int[] emsHospCount;


	/*
	 * Get adjacency list
	 */
	public Map<Vertex, LinkedList<Vertex>> getAdjList() {
		return adjList;
	}


	/*
	 * Set adjacency list
	 */
	public void setAdjList(Map<Vertex, LinkedList<Vertex>> adjList) {
		this.adjList = adjList;
	}


	/*
	 * Construct graph with origin vertex
	 */
	public CityGraph(Vertex origin)
	{
		adjList= new HashMap<Vertex, LinkedList<Vertex>>();
		adjList.put(origin, new LinkedList<Vertex>());//add the first vertex
		ids = new ArrayList<ID>();
		componentCount = 0;
	}

	/*
	 * Create empty graph
	 */
	public CityGraph()
	{
		adjList= new HashMap<Vertex, LinkedList<Vertex>>();
		ids = new ArrayList<ID>();
	}


	/*
	 * Add id to ID collection
	 */
	protected void addId(ID id)
	{
		this.ids.add(id);
	}

	/*
	 * Get ID from ID collection
	 */
	protected ID getId(int id) 
	{
		for(int i = 0; i < ids.size(); i++)
		{
			if(ids.get(i).getId() == id)
			{
				return ids.get(i);
			}
		}
		return null;
	}


	/*public void addNeighbor(Address a1, Address a2) 
	{
		adjList.get(getVertex(a1.getStreet())).add(getVertex(a2.getStreet()));
	}

	public LinkedList<Vertex> getNeighbors(Address a) 
	{
		return adjList.get(getVertex(a.getStreet()));
	}
	/*for(HashMap.Entry<ID, LinkedList<ID>> e : CityGraph.getAdjList().entrySet())
	{
		if(e.getValue().getAddress().getStreet().equals(newAddress.getStreet()))
		{
			v = e;
			break;
		}
		else
		{

		}
	}*/


	/*
	 * See if a vertex exists on specified street
	 */
	public boolean vertexExists(String street) {
		for(ID id: ids)
		{
			if(id.getAddress().getStreet().equals(street))
			{
				return true;
			}
		}
		return false;
	}


	/*
	 * Try to split this method into two, one for items known to be in main graph one for items known to be in subgraph
	 * @param houseNumber
	 * @param street
	 * @return
	 */
	public Vertex getVertex(int houseNumber, String street) 
	{
		for(ID id: this.ids)
		{
			//either the vertex is in main graph
			if(this.adjList.get(id.getAddress()) != null)
			{
				if(id.getAddress().houseNumber == houseNumber && id.getAddress().getStreet().equals(street))
				{
					//System.out.println("Found vertex at ID " + id.getId());
					return id.getAddress();
				}
			}
			//or it is in a subgraph/group vertex
			else if(id.getAddress().groupStarter != null && id.getAddress().groupStarter.getStreet().equals(street))
			{
				Vertex groupHead = id.getAddress().groupStarter;
				for(Vertex v: groupHead.addresses)
				{
					if(v.houseNumber == houseNumber && v.getStreet().equals(street))
					{
						//System.out.println("Found vertex at ID " + v.getId());
						return v;
					}
				}

			}
		}
		return null;
	}

	/*
	 * Discover/mark broken road
	 */
	protected void discoverBrokenRoad(Vertex vertex, DispatchHandler handler) {
		//mark broken road
		Vertex v1 = null;
		Vertex v2 = null;
		for(Vertex v: handler.brokenRoads.keySet())
		{
			if(vertex.getStreet().equals(v.getStreet()) 
					&& ((v.houseNumber > vertex.houseNumber && handler.brokenRoads.get(v).houseNumber < vertex.houseNumber) 
							|| (v.houseNumber < vertex.houseNumber && handler.brokenRoads.get(v).houseNumber > vertex.houseNumber)))
			{
				v1 = v;
				v2 = handler.brokenRoads.get(v);
				break;
			}
		}
		markBrokenRoad(v1, v2, handler);
	}

	/*
	 * Mark broken road
	 */
	private void markBrokenRoad(Vertex v1, Vertex v2, DispatchHandler handler)
	{
		//MARK vertices of broken road as unpassable
		v1.unpassable = true;
		handler.unpassables.add(v1);
		v2.unpassable = true;
		handler.unpassables.add(v2);
		//MARK vertices in between as unpassable
		//for all vertices on the graph whose address is between v1 and v2
		Vertex v;
		for(ID id : handler.graph.ids)
		{
			v = id.getAddress();
			if(v.getStreet().equals(v1.getStreet()) 
					&& ((v.houseNumber < v2.houseNumber && v.houseNumber > v1.houseNumber) 
							|| (v.houseNumber > v2.houseNumber && v.houseNumber < v1.houseNumber)))
			{
				v.unpassable = true;
				handler.unpassables.add(v);
				//now check to see if it is an intersection - if it is, mark the other parts of the intersection as unpassable
				if(v.isIntersection())
				{
					//if in main graph
					if(handler.graph.adjList.get(v) != null)
					{
						for(Vertex vInIntersection: handler.graph.adjList.get(v))
						{
							if(vInIntersection.isIntersection())
							{
								vInIntersection.unpassable = true;
								handler.unpassables.add(vInIntersection);
							}
						}
					}
					else if(handler.graph.adjList.get(v) == null && v.groupStarter != null)
					{
						for(Vertex vInIntersection: v.groupStarter.adjList.get(v))
						{
							if(vInIntersection.isIntersection())
							{
								vInIntersection.unpassable = true;
								handler.unpassables.add(vInIntersection);
							}
						}
					}

				}
			}
		}
		while(handler.repairTeams > 0)
		{
			System.out.println("Repair team available. Beginning to look for first road to fix now");
			repairRoad(handler);
		}
	}

	/*
	 * See if vertex is along a broken road
	 */
	protected static boolean isAlongBrokenRoad(Vertex v, DispatchHandler handler) {
		for(ID id: handler.graph.ids)
		{
			//either the vertex is in main graph
			Vertex vertex = id.getAddress();
			if(handler.graph.adjList.get(vertex) != null)
			{
				//check if this vertex is the start the broken road our vertex lies on
				if(handler.brokenRoads.get(vertex) != null && vertex.getStreet().equals(v.getStreet()) 
						&& ((vertex.houseNumber > v.houseNumber && handler.brokenRoads.get(vertex).houseNumber < v.houseNumber) 
								|| (vertex.houseNumber < v.houseNumber && handler.brokenRoads.get(vertex).houseNumber > v.houseNumber)))
				{
					return true;
				}
			}
			//or it is in a subgraph/group vertex
			else if(vertex.groupStarter != null)
			{
				Vertex groupHead = id.getAddress().groupStarter;
				for(Vertex groupMember: groupHead.addresses)
				{
					if(handler.brokenRoads.get(groupMember) != null && groupMember.getStreet().equals(v.getStreet()) 
							&& ((groupMember.houseNumber > v.houseNumber && handler.brokenRoads.get(groupMember).houseNumber < v.houseNumber) 
									|| (groupMember.houseNumber < v.houseNumber && handler.brokenRoads.get(groupMember).houseNumber > v.houseNumber)))
					{
						return true;
					}
				}
			}
		}
		return false;
	}

	/*
	 * Repair road - this was not finished, using compound vertices instead
	 * -->>Worked on repair for 3+ hours as delineated in email with Judah on April 29th<<--
	 */
	protected void repairRoad(DispatchHandler handler)
	{
		//do dfs to detect connected components
		ArrayList<Vertex> orderedComponents = new ArrayList<Vertex>();
		int componentCount = countConnectedComponents(handler);
		//if more than 1 component
		if(componentCount > 1)
		{
			//sort components based on # of ems units and hospitals - and count them
			CityGraph componentGraph = new CityGraph();
			orderedComponents = sortComponents(handler, orderedComponents, componentGraph);
			//for all components in sorted collection
			for(int i = 0; i < orderedComponents.size(); i++)
			{
				//start from the component with smallest # of units/hospitals
				//Vertex v = orderedComponents.get(i);
				//do bfs from component with smallest number of ems units and hospitals to find nearest ems units and hospital
				//HashMap<Vertex, Boolean> vis = new HashMap<Vertex, Boolean>();//to check if it has been visited
				//HashMap<Vertex, Vertex> prev = new HashMap<Vertex, Vertex>();//to record previous
				//HashMap<Vertex, Vertex> brokenRoadCollection = new HashMap<Vertex, Vertex>();
				//brokenRoadCollection = bfs(v, handler.graph, vis, prev, brokenRoadCollection);
				//record each broken road used to get there
				//repair the roads needed to connect the given component to the nearest ems units and hospital
				//hospital connecting takes priority if number of roads (but different roads) are the same
			}
			//road repair takes the same amount of time as 2k patients being treated
			//once a component becomes reconnected to at least one ems unit or hospital, loop back to beginning of repairRoad
		}
		//else if 1 component
		else if(componentCount == 1)
		{
			//multisource bfs from hospitals to broken roads, broken roads closest to hospitals get fixed first 
			//can use the set of broken roads and the existing connectivity to connect hospital to broken road and see which the closest roads are
		}
	}

	/*
	 * Source: http://www.geeksforgeeks.org/breadth-first-traversal-for-a-graph/
	 * UNFINISHED PART OF REPAIR
	 *
	private HashMap<Vertex, Vertex> bfs(Vertex v, CityGraph graph,HashMap<Vertex, Boolean> vis, HashMap<Vertex, Vertex> prev, HashMap<Vertex, Vertex> brokenRoadCollection) 
	{
		// Mark the current node as visited and enqueue it
		Vertex current = v;
		vis.put(current, true);
		Queue<Vertex> q = new LinkedList<Vertex>();
		q.add(v);
		while (q.size() != 0)
		{
			// Dequeue a vertex from queue and print it
			current = q.remove();
			// Get all adjacent vertices of the dequeued vertex s
			// If a adjacent has not been visited, then mark it
			// visited and enqueue it
			if (current.emsHospCount > 0)//if this is target - found hospital/ems unit
			{
				break;
			}
			else if(current.adjList != null)//if this is a group vertex
			{
				for(Vertex v1 : current.groupStarter.adjList.get(current))//for all the vertices that this vertex is connected to
				{
					if(vis.get(v1) == null)//if has not been visited yet
					{
						q.add(v1);//add to the queue
						vis.put(v1, true);//mark as visited
						prev.put(v1, current);//record the previous vertex
					}
				}
				//current = Vertex.addressBSearch(current, finish.houseNumber);//find destination from within group vertex
				//break;
			}
			else//have not found target yet
			{
				if(graph.adjList.get(current) != null)
				{
					for(Vertex v2 : graph.adjList.get(current))//for all the vertices that this vertex is connected to
					{
						if(vis.get(v2) == null)//if has not been visited yet
						{
							q.add(v2);//add to the queue
							vis.put(v2, true);//mark as visited
							prev.put(v2, current);//record the previous vertex
						}
					}
				}
				else if (current.groupStarter != null)
				{
					for(Vertex v3 : current.groupStarter.adjList.get(current))//for all the vertices that this vertex is connected to
					{
						if(vis.get(v3) == null)//if has not been visited yet
						{
							q.add(v3);//add to the queue
							vis.put(v3, true);//mark as visited
							prev.put(v3, current);//record the previous vertex
						}
					}
				}
			}
		}
		if (current.emsHospCount < 1)
		{
			System.out.println("can't reach destination");
			return null;
		}
		//transform list of nodes hit on way into directions - Count broken roads
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
		return brokenRoadCollection;
	}*/


	/*
	 * Sort the component vertices by number of ems/hospitals in each component
	 */
	private ArrayList<Vertex> sortComponents(DispatchHandler handler, ArrayList<Vertex> orderedComponents, CityGraph componentGraph) 
	{
		System.out.println("Sorting Components...");
		componentGraph = new CityGraph();//put this as a field?
		//hospitalCount = new int[componentCount];
		//Arrays.fill(hospitalCount, 0);
		emsHospCount = new int[componentCount];
		Arrays.fill(emsHospCount, 0);
		//group components into group vertices 
		for(int i = 0; i < componentCount; i++)//for all components
		{
			//make group vertex
			Vertex newVertex = new Vertex("Vertex at component " + i);
			newVertex.setId(i);
			orderedComponents.add(newVertex);
			componentGraph.adjList.put(newVertex, new LinkedList<Vertex>());
			//for all members of component i 
			for(ID id : ids)
			{
				if(componentIds[handler.graph.ids.indexOf(id)] == i)
				{
					newVertex.addresses.add(id.getAddress());//add vertex to component vertex
					id.getAddress().componentVertex = newVertex;//relate component vertex to vertex
					if(id.getAddress().emsUnits != null)//add to ems total for that component, if exists
					{
						newVertex.emsHospCount += id.getAddress().emsUnits.size();
					}
					if(id.getAddress().getHospitals() != null)//add to ems total for that component, if exists
					{
						newVertex.emsHospCount += id.getAddress().getHospitals().size();
					}
				}
			}
		}
		//sort the component vertices by hospital count/ems count 
		Collections.sort(orderedComponents, ComponentSort.EMSHOSP_ORDER);
		//return the vertex with the smallest number of hospitals and ems units
		return orderedComponents;
	}


	/*
	 * Will ignore the ems teams, hospitals, and patients that lie along a broken road, if there are any, as 
	 * graph will not include the broken roads in any components.
	 * UNFINISHED
	 * @param handler
	 * @return
	 */
	private int countConnectedComponents(DispatchHandler handler) 
	{
		System.out.println("Worked on repair for required amount of time. See comment at beginning of countConnectedComponents");
		boolean pastBrokenRoad = false; 
		this.marked = new boolean[handler.graph.ids.size()];
		componentIds = new int[handler.graph.ids.size()];
		for(ID id: handler.graph.ids)
		{
			int s = handler.graph.ids.indexOf(id);
			if (!this.marked[s])
			{
				dfs(handler.graph.adjList, s, handler.graph, pastBrokenRoad);
				//System.out.println("Component++");
				componentCount++;
			}
		}	

		//System.out.println("Number of components: " + componentCount);
		return componentCount;
	}


	/*
	 * DFS for repair.
	 */
	private void dfs(Map<Vertex, LinkedList<Vertex>> currentAdjList, int s, CityGraph graph, boolean pastBrokenRoad) {
		Vertex vertex = graph.ids.get(s).getAddress();
		if(!vertex.unpassable)//not a broken road - base case 1
		{
			this.marked[s] = true;
			if(pastBrokenRoad != false)
			{
				//System.out.println("Incrementing component count by 1");
				componentCount++;
				//pastBrokenRoad = false;
			}
			//System.out.println("DFS Marking id = " + graph.ids.get(s).getId() + " of component " + componentCount);
			this.componentIds[s] = componentCount;
		}
		else//is a broken road - base case 2
		{
			this.marked[s] = true;
			//System.out.println("Detected broken road at " + vertex.getId() + " while counting connected components");
			vertex = null;

			pastBrokenRoad = true;
			//System.out.println("PastBrokenRoad = " + pastBrokenRoad);
			/*while(vertex.isUnpassable())
			{
				System.out.println("Looking for start of non-broken road");
				vertex = graph.ids.get(s++).getAddress();
				if(vertex.isUnpassable())
				{
					System.out.println("Detected broken road at " + vertex.getId() + " while counting connected components");
					s = graph.ids.indexOf(vertex.getId());
					this.marked[s] = true;
				}
				vertex = graph.ids.get(s--).getAddress();
				if(vertex.isUnpassable())
				{
					System.out.println("Detected broken road at " + vertex.getId() + " while counting connected components");
					s = graph.ids.indexOf(vertex.getId());
					this.marked[s] = true;
				}
			}*/
			//componentCount++;
		}
		//if s is in main graph - recurse through to mark non-broken roads
		if(graph.adjList.get(vertex) != null)
		{
			//pastBrokenRoad = false;
			LinkedList<Vertex> connections = currentAdjList.get(graph.ids.get(s).getAddress());
			for(Vertex v : connections) //for all vertices that s has path to
			{
				if (!marked[graph.ids.indexOf(graph.getId(v.getId()))])//if that vertex has not been marked yet
				{
					s = graph.ids.indexOf(graph.getId(v.getId()));
					dfs(currentAdjList, s, graph, pastBrokenRoad);
				}
				if(vertex.groupStarter != null)//part of a group vertex - now mark all the vertices inside 
				{
					Vertex groupHead = vertex.groupStarter;
					for(Vertex groupMember: groupHead.addresses)
					{
						if (!marked[graph.ids.indexOf(graph.getId(groupMember.getId()))])//if that vertex has not been marked yet
						{
							s = graph.ids.indexOf(graph.getId(groupMember.getId()));
							dfs(currentAdjList, s, graph, pastBrokenRoad);
						}
					}
				}
			}

		}
	}
}
