import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
* Priority queue of people calling for medical help. From Sedgewick/DS: http://algs4.cs.princeton.edu/24pq/MaxPQ.java.html 
*/
public class PatientQueue implements Iterable<Patient>
{
	    private Patient[] pq;                    // store items at indices 1 to n
	    private int n;                       // number of items on priority queue
	    private Comparator<Patient> comparator;  // optional Comparator

	    /**
	     * Initializes an empty priority queue with the given initial capacity.
	     *
	     * @param  initCapacity the initial capacity of this priority queue
	     */
	    public PatientQueue(int initCapacity) {
	        pq = (Patient[]) new Patient[initCapacity + 1];
	        n = 0;
	    }

	    /**
	     * Initializes an empty priority queue.
	     */
	    public PatientQueue() {
	        this(1);
	    }

	    /**
	     * Initializes an empty priority queue with the given initial capacity,
	     * using the given comparator.
	     *
	     * @param  initCapacity the initial capacity of this priority queue
	     * @param  comparator the order in which to compare the keys
	     */
	    public PatientQueue(int initCapacity, Comparator<Patient> comparator) {
	        this.comparator = comparator;
	        pq = (Patient[]) new Patient[initCapacity + 1];
	        n = 0;
	    }

	    /**
	     * Initializes an empty priority queue using the given comparator.
	     *
	     * @param  comparator the order in which to compare the keys
	     */
	    public PatientQueue(Comparator<Patient> comparator) {
	        this(1, comparator);
	    }

	    /**
	     * Initializes a priority queue from the array of keys.
	     * Takes time proportional to the number of keys, using sink-based heap construction.
	     *
	     * @param  keys the array of keys
	     */
	    public PatientQueue(Patient[] keys) {
	        n = keys.length;
	        pq = (Patient[]) new Patient[keys.length + 1]; 
	        for (int i = 0; i < n; i++)
	            pq[i+1] = keys[i];
	        for (int k = n/2; k >= 1; k--)
	            sink(k);
	        assert isMaxHeap();
	    }
	      


	    /**
	     * Returns true if this priority queue is empty.
	     *
	     * @return {@code true} if this priority queue is empty;
	     *         {@code false} otherwise
	     */
	    public boolean isEmpty() {
	        return n == 0;
	    }

	    /**
	     * Returns the number of keys on this priority queue.
	     *
	     * @return the number of keys on this priority queue
	     */
	    public int size() {
	        return n;
	    }

	    /**
	     * Returns a largest key on this priority queue.
	     *
	     * @return a largest key on this priority queue
	     * @throws NoSuchElementException if this priority queue is empty
	     */
	    public Patient max() {
	        if (isEmpty()) throw new NoSuchElementException("Priority queue underflow");
	        return pq[1];
	    }

	    // helper function to double the size of the heap array
	    private void resize(int capacity) {
	        assert capacity > n;
	        Patient[] temp = (Patient[]) new Patient[capacity];
	        for (int i = 1; i <= n; i++) {
	            temp[i] = pq[i];
	        }
	        pq = temp;
	    }


	    /**
	     * Adds a new key to this priority queue.
	     *
	     * @param  x the new key to add to this priority queue
	     */
	    public void insert(Patient x) {

	        // double size of array if necessary
	        if (n >= pq.length - 1) resize(2 * pq.length);

	        // add x, and percolate it up to maintain heap invariant
	        pq[++n] = x;
	        swim(n);
	        assert isMaxHeap();
	    }

	    /**
	     * Removes and returns a largest key on this priority queue.
	     *
	     * @return a largest key on this priority queue
	     * @throws NoSuchElementException if this priority queue is empty
	     */
	    public Patient delMax() {
	        if (isEmpty()) throw new NoSuchElementException("Priority queue underflow");
	        Patient max = pq[1];
	        exch(1, n--);
	        sink(1);
	        pq[n+1] = null;     // to avoid loiterig and help with garbage collection
	        if ((n > 0) && (n == (pq.length - 1) / 4)) resize(pq.length / 2);
	        assert isMaxHeap();
	        return max;
	    }


	   /***************************************************************************
	    * Helper functions to restore the heap invariant.
	    ***************************************************************************/

	    private void swim(int k) {
	        while (k > 1 && less(k/2, k)) {
	            exch(k, k/2);
	            k = k/2;
	        }
	    }

	    private void sink(int k) {
	        while (2*k <= n) {
	            int j = 2*k;
	            if (j < n && less(j, j+1)) j++;
	            if (!less(k, j)) break;
	            exch(k, j);
	            k = j;
	        }
	    }

	   /***************************************************************************
	    * Helper functions for compares and swaps.
	    ***************************************************************************/
	    private boolean less(int i, int j) {
	        if (comparator == null) {
	            return ((Comparable<Patient>) pq[i]).compareTo(pq[j]) < 0;
	        }
	        else {
	            return comparator.compare(pq[i], pq[j]) < 0;
	        }
	    }

	    private void exch(int i, int j) {
	        Patient swap = pq[i];
	        pq[i] = pq[j];
	        pq[j] = swap;
	    }

	    // is pq[1..N] a max heap?
	    private boolean isMaxHeap() {
	        return isMaxHeap(1);
	    }

	    // is subtree of pq[1..n] rooted at k a max heap?
	    private boolean isMaxHeap(int k) {
	        if (k > n) return true;
	        int left = 2*k;
	        int right = 2*k + 1;
	        if (left  <= n && less(k, left))  return false;
	        if (right <= n && less(k, right)) return false;
	        return isMaxHeap(left) && isMaxHeap(right);
	    }


	   /***************************************************************************
	    * Iterator.
	    ***************************************************************************/

	    /**
	     * Returns an iterator that iterates over the keys on this priority queue
	     * in descending order.
	     * The iterator doesn't implement {@code remove()} since it's optional.
	     *
	     * @return an iterator that iterates over the keys in descending order
	     */
	    public Iterator<Patient> iterator() {
	        return new HeapIterator();
	    }

	    private class HeapIterator implements Iterator<Patient> {

	        // create a new pq
	        private PatientQueue copy;

	        // add all items to copy of heap
	        // takes linear time since already in heap order so no keys move
	        public HeapIterator() {
	            if (comparator == null) copy = new PatientQueue(size());
	            else                    copy = new PatientQueue(size(), comparator);
	            for (int i = 1; i <= n; i++)
	                copy.insert(pq[i]);
	        }

	        public boolean hasNext()  { return !copy.isEmpty();                     }
	        public void remove()      { throw new UnsupportedOperationException();  }

	        public Patient next() {
	            if (!hasNext()) throw new NoSuchElementException();
	            return copy.delMax();
	        }
	    }
}