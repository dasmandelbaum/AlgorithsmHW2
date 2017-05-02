import java.util.Comparator;

/**
 * Sort components based on number of ems teams/hospitals contained within
 */
public class ComponentSort {
	static final Comparator<Vertex> EMSHOSP_ORDER = new Comparator<Vertex>() 
    {
            public int compare(Vertex v1, Vertex v2) {
                return v2.emsHospCount.compareTo(v1.emsHospCount);//return the lower value
            }
    };
}
