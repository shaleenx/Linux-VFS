import java.util.Comparator;


public class SpaceSizeCmp implements Comparator<Block>{

	@Override
	public int compare(Block o1, Block o2) {
		if(o1.size>o2.size)
			return 1;
		if(o1.size<o2.size)
			return -1;
		return 0;
	}

}
