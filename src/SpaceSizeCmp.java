import java.util.Comparator;

/*
 * This class implements the Comparator interface.
 * It is used to sort the ArrayList of Blocks.
 * 
 * Currently it is designed to simulate best-fit
 * contiguous memory allocation technique which is
 * found to be very optimized with respect to memory
 * allocation.
 * 
 * Reversing the comparator by switching return value of 1 and -1
 * will switch the implementation from Best-fit contiguous memory 
 * allocation to worst-fit contiguous memory allocation.
 * 
 * Also, not using this comparator at all to sort the 
 * ArrayList will convert the implementation to first-fit
 * contiguous memory allocation technique, which is found
 * to be very time efficient. However, the worst-case time 
 * complexity of this implementation would also be O(n) just
 * like other implementations.
 */
public class SpaceSizeCmp implements Comparator<Block>{
	
	@Override
	public int compare(Block o1, Block o2) {
		if(o1.size>o2.size)
			return -1;
		if(o1.size<o2.size)
			return 1;
		return 0;
	}

}
