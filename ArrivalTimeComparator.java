import java.util.Comparator;

public class ArrivalTimeComparator implements Comparator<ProcessControlBlock> {

    @Override
    public int compare(ProcessControlBlock o1, ProcessControlBlock o2) {
        if(o1.getArrival() < o2.getArrival()) {
        	return -1;
        }
        
        if(o1.getArrival() == o2.getArrival() && o1.getPid() < o2.getPid()) return -1;
        return 1;
        
        
    }
}
//https://www.freecodecamp.org/news/priority-queue-implementation-in-java/
//https://stackoverflow.com/questions/15731967/priorityqueue-has-objects-with-the-same-priority