import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

/**
 * This class stores street addresses. People, hospitals, and EMS units are objects associated with a given location
 */
public class Vertex implements Iterable<Vertex>, Comparator<Vertex>, Comparable<Vertex>
{
	private int id;
	protected int houseNumber;
	private String street;
	protected ArrayList<Patient> patients;
	private ArrayList<Hospital> hospitals;
	protected ArrayList<EmsUnit> emsUnits;
	protected ArrayList<Vertex> addresses;
	protected Map<Vertex, LinkedList<Vertex>> adjList; //adjacency list to hold edges between vertices
	private boolean isIntersection;
	protected Vertex groupStarter;
	protected Vertex componentVertex;
	protected boolean unpassable;
	protected Integer emsHospCount;

	/*
	 * Constructor 
	 */
	public Vertex(String address)
	{
		String fullAddress = address;
		String id = fullAddress.substring(0, fullAddress.indexOf(" "));
		this.setId(Integer.parseInt(id));
		String addressMinusId = fullAddress.substring(fullAddress.indexOf(" ") + 1);
		String houseNumber = addressMinusId.substring(0, addressMinusId.indexOf(" ")); // Grab the house number
		this.houseNumber = Integer.parseInt(houseNumber);
		String addressStreet = addressMinusId.substring(addressMinusId.indexOf(" ") + 1);
		this.setStreet(addressStreet);
		this.patients = new ArrayList<Patient>();
		this.setHospitals(new ArrayList<Hospital>());
		this.emsUnits = new ArrayList<EmsUnit>();
		addresses = new ArrayList<Vertex>();
		isIntersection = false;
		this.unpassable = false;
		emsHospCount = 0;
	}
	//id = new ID(parseInt(fullAddress.substring(0, " ")), this);

	/*
	 * For creating a group vertex / multiple addresses along single stretch without intersection
	 * @param address
	 */
	public Vertex(Vertex a1, Vertex a2, Vertex a3, Vertex a4) {
		addresses = new ArrayList<Vertex>();
		addresses.add(a1);
		addresses.add(a2);
		addresses.add(a3);
		addresses.add(a4);
		isIntersection = true;
	}

	/*
	 * Return id of vertex
	 */
	public int getId() {
		return id;
	}

	/*
	 * Set id of vertex
	 */
	public void setId(int id) {
		this.id = id;
	}

	/*
	 * Return hospitals at vertex
	 */
	public ArrayList<Hospital> getHospitals() {
		return hospitals;
	}

	/*
	 * Set hospitals at vertex
	 */
	public void setHospitals(ArrayList<Hospital> hospitals) {
		this.hospitals = hospitals;
	}

	/*
	 * Return string name of street of vertex
	 */
	public String getStreet() {
		return street;
	}

	/*
	 * Set string name of street of vertex
	 */
	public void setStreet(String street) {
		this.street = street;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<Vertex> iterator() {
		return this.iterator();
	}

	/*
	 * If vertex belongs to intersection
	 */
	public boolean isIntersection() {
		return isIntersection;
	}

	/*
	 * Set as member of an intersection
	 */
	public void setIntersection(boolean isIntersection) {
		this.isIntersection = isIntersection;
	}

	/*
	 * Binary search within group vertex
	 */
	protected static Vertex addressBSearch(Vertex vertex, int houseNumber) throws Exception {
		Collections.sort(vertex.addresses);
		Vertex v = bSearch(vertex.addresses, houseNumber, 0, vertex.addresses.size() - 1);
		System.out.println("Used Binary Search to find address within group vertex");
		return v;
	}

	/*
	 * Binary search with addresses
	 */
	private static Vertex bSearch(ArrayList<Vertex> addresses, int houseNumber, int lo, int hi) throws Exception 
	{
		if(hi < lo)
		{
			throw new Exception("Error: Binary address search failed.");
		}
		int mid = (lo + hi) / 2;
		if(addresses.get(mid).houseNumber == houseNumber)
		{
			return addresses.get(mid);
		}
		else if(addresses.get(mid).houseNumber > houseNumber)
		{
			return bSearch(addresses, houseNumber, lo, mid - 1);
		}
		else if(addresses.get(mid).houseNumber < houseNumber)
		{
			return bSearch(addresses, houseNumber, mid + 1, hi);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Vertex o) {
		return this.houseNumber - o.houseNumber;
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(Vertex o1, Vertex o2) {
		return o1.houseNumber - o2.houseNumber;
	}

	/*
	 * Unpassable if part of broken road
	 */
	public boolean isUnpassable() {
		return this.unpassable;
	}
}
