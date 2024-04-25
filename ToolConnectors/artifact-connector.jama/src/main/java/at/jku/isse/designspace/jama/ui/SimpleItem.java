package at.jku.isse.designspace.jama.ui;

import java.util.Comparator;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SimpleItem {

	private String id;
	private String docKey;
	private String name;
	private String type;
	private String status;
	private String internalId;
	
	
	public static class SimpleItemComparator implements Comparator<SimpleItem> {

		@Override
		public int compare(SimpleItem o1, SimpleItem o2) {
			int typeComp = o1.getType().compareTo(o2.getType());
			if (typeComp == 0) // same type
				return o1.getDocKey().compareTo(o2.getDocKey());
			else
				return typeComp;
		}
		
	}
	
	public static SimpleItemComparator comparator = new SimpleItemComparator();
}
