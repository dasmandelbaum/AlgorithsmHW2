import java.util.Stack;

/**
 * Ems unit class representing an ems unit
 * @author david mandelbaum
 *
 */
public class EmsUnit {
	protected Vertex address;

	/*
	 * Constructor
	 */
	public EmsUnit(Vertex v)
	{
		this.address = v;
	}

	/*
	 * Treat patient and traverse path to them
	 * May encounter broken road
	 */
	public Patient treat(CityGraph graph, EmsUnit unit, Patient p, Stack<Vertex> closestPatientPath, DispatchHandler handler) 
	{
		//not yet dealing with broken roads
		System.out.println("Unit located at " + unit.address.getId() + " dispatched to treat patient \n of severity " + p.getSeverity() + " at location " + p.getAddress().getId());
		System.out.println("Directions used:");
		Vertex v = null;
		if(!handler.brokenRoads.containsKey(closestPatientPath.peek()) && !CityGraph.isAlongBrokenRoad(closestPatientPath.peek(), handler))//first vertex is not part of broken road
		{
			if(closestPatientPath.size() == 1)
			{
				v = closestPatientPath.pop();
				/*if(handler.brokenRoads.containsKey(v) || CityGraph.isAlongBrokenRoad(closestPatientPath.peek(), handler))
				{
					System.out.println("Found broken road");
					graph.discoverBrokenRoad(v, handler);
					return null;//<---- RETURN?
				}*/
				unit.address = v;//ems now located here
				//System.out.print(v.getId() + "\n");
			}
			else if(closestPatientPath.size() > 1)
			{
				int size = closestPatientPath.size();
				//v.emsUnits.remove(unit);
				for(int i = 0; i < size; i++)
				{
					v = closestPatientPath.pop();
					if(handler.brokenRoads.containsKey(v) || CityGraph.isAlongBrokenRoad(v, handler))
					{
						System.out.println("Found broken road");
						graph.discoverBrokenRoad(v, handler);
						return null;//<---- RETURN?
					}
					unit.address = v;//ems now located here
					//v.emsUnits.add(unit);  
					//--> ?? v.patients.remove(p);
					//---> //remove the unit from address it is leaving
					System.out.print(v.getId() + "->");
				}
			}
			else//if size = 0
			{
				v = p.getAddress();
				unit.address = v;//ems now located here
			}
			v.patients.remove(p);
			
			System.out.println("Patient at " + p.getAddress().getId() + " has been healed");
			return p;
		}
		else //is along broken road
		{
			System.out.println("Found broken road");
			graph.discoverBrokenRoad(closestPatientPath.peek(), handler);
		}
		return null;
	}

}
