
/**
* ID's relate to addresses.
*/
public class ID
{
	private int id;
	private Vertex address;
	//private VertexGroup vertexGroup;
	
	/*
	 * Constructor.
	 */
	public ID(Vertex address)
	{
		this.setId(address.getId());
		this.address = address;
		//this.vertexGroup = null;
	}
	
	/*
	 * Return address relating to ID
	 */
	public Vertex getAddress()
	{
		return this.address;
	}

	/*
	 * Get int form of ID number
	 */
	public int getId() {
		return id;
	}

	/*
	 * Set ID with int number
	 */
	public void setId(int id) {
		this.id = id;
	}

	/*public void setVertexGroup(VertexGroup v) {
		this.vertexGroup = v;
	}

	public VertexGroup getVertexGroup() {
		// TODO Auto-generated method stub
		return this.vertexGroup;
	}*/
}