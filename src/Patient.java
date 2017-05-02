/**
* A patient has an address and a severity of issue level.
*/
public class Patient implements Comparable<Patient>
{
	private Vertex address;
	private int severity;
	/*
	 * Constructor
	 */
	public Patient(Vertex address, int severity)
	{
		this.setAddress(address);
		this.setSeverity(severity);
	}
	
	/*
	 * Return address of patient.
	 */
	public Vertex getAddress() {
		return address;
	}
	
	/*
	 * Set address of patient.
	 */
	public void setAddress(Vertex address) {
		this.address = address;
	}
	
	/*
	 * Return severity level of patient
	 */
	public Integer getSeverity() {
		return severity;
	}
	
	/*
	 * Set severity level of patient 
	 */
	public void setSeverity(int severity) {
		this.severity = severity;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Patient o) {
		// TODO Auto-generated method stub
		return this.getSeverity() - o.getSeverity() ;
	}
}