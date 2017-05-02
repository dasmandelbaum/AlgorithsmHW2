import java.util.Comparator;

public class PatientSort {
	static final Comparator<Patient> SEVERITY_ORDER = new Comparator<Patient>() 
    {
            public int compare(Patient p1, Patient p2) {
                return p1.getSeverity().compareTo(p2.getSeverity());
            }
    };
}