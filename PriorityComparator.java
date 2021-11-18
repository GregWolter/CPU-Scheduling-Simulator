import java.io.Serializable;
import java.util.Comparator;

public class PriorityComparator implements Comparator<ProcessControlBlock>, Serializable {

    @Override
    public int compare(ProcessControlBlock o1, ProcessControlBlock o2) {
    	if(o1.getPriority() < o2.getPriority()) return -1;
        return 1;
        
    }
}
//https://www.freecodecamp.org/news/priority-queue-implementation-in-java/
