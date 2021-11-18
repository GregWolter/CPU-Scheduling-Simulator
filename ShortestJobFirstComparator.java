

import java.io.Serializable;
import java.util.Comparator;

public class ShortestJobFirstComparator implements Comparator<ProcessControlBlock>, Serializable {

    @Override
    /* priority is the shortest remaining running time */
    public int compare(ProcessControlBlock o1, ProcessControlBlock o2) {
        if(o1.getRemainingTotalCPUbursts() < o2.getRemainingTotalCPUbursts()) return -1;
        return 1;    
    }
}


