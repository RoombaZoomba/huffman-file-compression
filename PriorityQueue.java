
/*  Student information for assignment:
 *
 *  On <OUR> honor, <Mohammad Hakim> and <Nathan Cheng>, this programming assignment is <OUR>
 *  own work and <WE> have not provided this code to any other student.
 *
 *  Number of slip days used:1
 *
 *  Student 1 (Student whose turnin account is being used)
 *  UTEID:nyc278
 *  email address:nathanchengus@gmail.com
 *  Grader name:David K
 *
 *  Student 2
 *  UTEID: msh3573
 *  email address: mohammad_hakim@utexas.edu
 *
 */

import java.util.Iterator;
import java.util.LinkedList;

public class PriorityQueue<E extends Comparable<E>> {
	private LinkedList<E> pQ;

	// constructor
	public PriorityQueue() {
		pQ = new LinkedList<>();
	}

	// add to priority queue
	// param: node - what we are adding
	// return: nothing, modifies priority queue
	public void enqueue(E node) {
		boolean notInserted = true;
		Iterator<E> it = pQ.iterator();
		int index = 0;
		// compare with all values currently in queue
		while (notInserted && it.hasNext()) {
			if (it.next().compareTo(node) > 0) {
				pQ.add(index, node);
				notInserted = false;
			}
			index++;
		}
		// add at end
		if (notInserted) {
			pQ.addLast(node);
		}
	}

	// remove first from queue
	public E dequeue() {
		// pre
		if (pQ.size() == 0) {
			throw new IllegalStateException("Cannot dequeue! size is 0");
		}
		return pQ.removeFirst();
	}

	// size of queue
	public int size() {
		return pQ.size();
	}

	// get the value of the first item in the queue
	public E peek() {
		// pre
		if (pQ.size() == 0) {
			throw new IllegalStateException("Cannot peek! size is 0");
		}
		return pQ.getFirst();
	}
}

ssh-keygen -f /u/nyc278/.ssh/known_hosts" -R "git.gheith.com"
ssh-keygen -f "/u/nyc278/.ssh/known_hosts" -R "git.gheith.com"