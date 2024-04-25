package at.jku.isse.designspace.jama.replaying;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.text.CaseUtils;

import at.jku.isse.designspace.jama.replaying.JamaActivity.EventTypes;
import at.jku.isse.designspace.jama.replaying.JamaActivity.ObjectTypes;


public class ChangeParser {

//	public static void parseRelationChange(InternalActivity change) {
//		// parse out from: before/after, to: before/after
//		//
//	}
//	
//	public static void parseFieldChange(InternalActivity change) {
//		// parse field name, before text, after text
//		// later determine field id, and optional/where applicable before text selection id, after text selection/option id
//	}
	
//	public static Optional<JamaActivity.EventTypes> parseEventType(InternalActivity change) {
//		JamaActivity.EventTypes et = null;
//		try {
//			et = JamaActivity.EventTypes.valueOf(change.getEventType());
//		} catch (Exception e) {			
//			e.printStackTrace();
//		}		
//		return Optional.ofNullable(et);		
//	}
//	
//	public static Optional<JamaActivity.ObjectTypes> parseItemType(InternalActivity change) {
//		JamaActivity.ObjectTypes et = null;
//		try {
//			et = change.getObjectType();
//		} catch (Exception e) {			
//			e.printStackTrace();
//		}		
//		return Optional.ofNullable(et);		
//	}

//	
//	public static String parseItemDetails(String str) {
//		
//		
//		if (str.contains("changed from")) { // change of typed field with pick option or check button
//			
//		}
//		if (str.contains("\"Description\""))
//			return "description";
//		
//		
//		return "";
//	}
	
	public static List<? extends JamaActivity> parseToChange(JamaActivity ia) {
		if (ia.getObjectType().equals(ObjectTypes.RELATIONSHIP)) {
			Optional<RelationChange> optC = parseToRelationshipChange(ia);
			return optC.isPresent() ? Arrays.asList(optC.get()) : Collections.emptyList();
		} else if (ia.getObjectType().equals(ObjectTypes.ITEM) && !ia.getEventType().equals(EventTypes.BATCH_UPDATE)  ) {
			return parseToPropertyChanges(ia);
		}
		return Collections.emptyList();
	}
	
	public static Optional<RelationChange> parseToRelationshipChange(JamaActivity ia) {
		if (!ia.getObjectType().equals(ObjectTypes.RELATIONSHIP) ||
				!( ia.getEventType().equals(EventTypes.CREATE) || ia.getEventType().equals(EventTypes.DELETE) || ia.getEventType().equals(EventTypes.UPDATE))) {
			System.out.println("Error: asked to parse relationship change for unsuitable JamaActivity: "+ia.toString());
			return Optional.empty();
		}		
		String str = ia.getDetails();
		int fromPos = str.indexOf("Affected Relationship(s):");
		int toPos = str.indexOf("\" --> \"");
		if (fromPos >= 0 && toPos >= 0) {			
			String fromValue = str.substring(fromPos+39, toPos);
			String toValue = str.substring(toPos+7, str.length()-11);
			return Optional.of(new RelationChange(ia, fromValue.trim(), toValue.trim()));				
		} else {
			System.out.println("Failure to locate relation DocKeys from String: "+str);
		}
		return Optional.empty();
	}
	
//	public static List<PropertyChange> parseToPropertyChanges(JamaActivity ia) {
//		// count how many changes, we assume that a text in a field such as name, doesn't contain a comma <,>
//		return Arrays.stream(ia.getDetails().split(","))
//			.map(part -> part.trim()) 
//			.map(part -> parseToChange(part, ia))
//			.filter(opC -> opC.isPresent())
//			.map(opC -> opC.get())
//			.collect(Collectors.toList());				
//	}
	
//	public static Optional<PropertyChange> parseToChange(String str, JamaActivity ia) {
//		if (str.contains("\"Description\" changed")) { 
//			return Optional.of(new PropertyChange(ia, "description", "", ""));
//		} else if (str.contains("\"Short Name\" changed")) { 
//				return Optional.of(new PropertyChange(ia, "shortName", "", ""));
//		} else if (str.startsWith("Initial creation") || str.startsWith("Initial Creation")) { 
//			return Optional.of(new CreationChange(ia));
//		} else {
//			int fromPos = str.indexOf("\" changed from \"");
//			if (fromPos >= 0) {
//				String fieldName = str.substring(1, fromPos);
//				fieldName = fieldName.replaceAll("\\s", ""); // removes all whitespace
//				if (fieldName.length() < 1) return Optional.empty(); // as there is no fieldName string to be extracted
//				fieldName = String.valueOf(fieldName.charAt(0)).toLowerCase()+fieldName.substring(1); // make first letter lowercase
//				int toPos = str.indexOf("\" to \"");
//				if (toPos >= 0) {
//					String fromValue = str.substring(fromPos+16, toPos);
//					String toValue = str.substring(toPos+6, str.length()-1);
//					return Optional.of(new PropertyChange(ia, fieldName.trim(), fromValue.trim(), toValue.trim()));
//				} else {
//					System.out.println("Failure to locate <toField> from String: "+str);
//				}
//			} else {
//				System.out.println("Failure to locate <fromField> from String: "+str);
//			}
//		}
//		return Optional.empty();		
//	}
	
	private static List<List<String>> tokenize(String str) {
		List<List<String>> scopes = new LinkedList<>();
		boolean withinQuote = false;
		if (!str.startsWith("\"")) { 
			scopes.add(List.of(str));
			return scopes;
		}
		withinQuote = true;		
		List<String> tokens = new LinkedList<>(); 
		scopes.add(tokens);
		int prevQ = 0;
		int posQ = str.indexOf("\"", 1);
		while (posQ >= 0) {
			String token = str.substring(prevQ+1,posQ).trim();
			if (token.length() == 0 && posQ ==str.length()) break;
			if (withinQuote) { // we found the end quote				
				tokens.add(token);				
				withinQuote = false;
			} else { // we found the beginning of the next quote part
				// now check if we need to split this token into two at a ','
				int sepPos = token.indexOf(",");
				if (sepPos < 0) { // no such separator
					tokens.add(token);										
				} else { // there is a separator, hence a separate change
					String tokenPrev = token.substring(0, sepPos).trim();
					if (tokenPrev.length() > 0)
						tokens.add(tokenPrev);
					scopes.add(tokens);
					tokens = new LinkedList<>();
					String tokenNext = token.substring(sepPos+1).trim();
					if (tokenNext.length() > 0)
						tokens.add(tokenNext);
				}	
				withinQuote = true;
			}
			prevQ = posQ;
			posQ = str.indexOf("\"", prevQ+1);
			if (posQ == -1 && prevQ < str.length()) // the last nonquoted token
				posQ = str.length();
		}		
		return scopes;
	}
	
	public static List<PropertyChange> parseToPropertyChanges(JamaActivity ia) {
		return tokenize(ia.getDetails().replace("\n", ", ")).stream()
			.map(tokens -> parseToChange(tokens, ia))
			.filter(opC -> opC.isPresent())
			.map(opC -> opC.get())
			.collect(Collectors.toList());				
	}
	
	public static Optional<PropertyChange> parseToChange(List<String> tokens, JamaActivity ia) {
		// tokenlist is of form: 
		// "property" changed
		// "property" changed from "value" to "value"
		if (tokens.isEmpty()) {
			return Optional.empty();		
		} else if (tokens.size() == 1) { 
			 if (tokens.get(0).startsWith("Initial creation") || tokens.get(0).startsWith("Initial Creation")) {
				 return Optional.of(new CreationChange(ia));
			 }	else
				 return Optional.empty();			//convertion of item to diff type, deletion and restoring events, or 'no change'
		} else	if (tokens.get(1).equalsIgnoreCase("changed"))
			return	 Optional.of(new PropertyChange(ia, toLowerFirst(tokens.get(0)), "", ""));
		else if (tokens.size() == 5){
			return Optional.of(new PropertyChange(ia, 
					CaseUtils.toCamelCase(tokens.get(0), false, ' ', '_', '.', '-', '/', '\\').trim(), 
					tokens.get(2), 
					tokens.get(4)));						
		} else
			return Optional.empty();
	}
	
	public static String toLowerFirst(String str) {
		if (str == null || str.length() == 0)
			return str;
		else 
			return String.valueOf(str.charAt(0)).toLowerCase()+str.substring(1);
	}
	
	
	public static class RelationChange extends JamaActivity {
		private JamaActivity delegate;
		private String sourceDocKey; // from upstream item,
		private String destinationDocKey;	// to downstream item
		
		public RelationChange(JamaActivity delegate, String sourceDocKey,
				String destinationDocKey) {
			super();
			this.delegate = delegate;
			this.sourceDocKey = sourceDocKey;
			this.destinationDocKey = destinationDocKey;
		}
		
		public String getSourceDocKey() {
			return sourceDocKey;
		}
		public String getDestinationDocKey() {
			return destinationDocKey;
		}
		public ZonedDateTime getDate() {
			return delegate.getDate();
		}
		public void setDate(ZonedDateTime date) {
			delegate.setDate(date);
		}
		public Integer getItemId() {
			return delegate.getItemId();
		}
		public void setItemId(Integer itemId) {
			delegate.setItemId(itemId);
		}
		public String getAction() {
			return delegate.getAction();
		}
		public void setAction(String action) {
			delegate.setAction(action);
		}
		public EventTypes getEventType() {
			return delegate.getEventType();
		}
		public void setEventType(EventTypes eventType) {
			delegate.setEventType(eventType);
		}
		public Integer getUserId() {
			return delegate.getUserId();
		}
		public void setUserId(Integer userId) {
			delegate.setUserId(userId);
		}
		public ObjectTypes getObjectType() {
			return delegate.getObjectType();
		}
		public void setObjectType(ObjectTypes objectType) {
			delegate.setObjectType(objectType);
		}
		public String getDetails() {
			return delegate.getDetails();
		}
		public void setDetails(String details) {
			delegate.setDetails(details);
		}
		public Integer getId() {
			return delegate.getId();
		}
		public void setId(Integer id) {
			delegate.setId(id);
		}
		public int hashCode() {
			return delegate.hashCode();
		}
		public boolean equals(Object obj) {
			return delegate.equals(obj);
		}
	
		@Override
		public String toString() {
			return "RelationChange "+getEventType()+" "+ getItemId() +" [sourceDocKey=" + sourceDocKey + ", destinationDocKey=" + destinationDocKey
					+ ", date=" + getDate() + "]";
		}
		
	}
	
	public static class CreationChange extends PropertyChange {

		public CreationChange(JamaActivity delegate) {
			super(delegate, null, null, null);
		}
		
	}
	
	public static class PropertyChange extends JamaActivity {
		private JamaActivity delegate;
		private String field;
		private String previousValue;
		private String newValue;				
		
		public PropertyChange(JamaActivity delegate, String field, String previousValue,
				String newValue) {
			super();
			this.delegate = delegate;
			this.field = field;
			this.previousValue = previousValue;
			this.newValue = newValue;
		}
				
		public String getField() {
			return field;
		}

		public String getPreviousValue() {
			return previousValue;
		}

		public String getNewValue() {
			return newValue;
		}

		public ZonedDateTime getDate() {
			return delegate.getDate();
		}

		public void setDate(ZonedDateTime date) {
			delegate.setDate(date);
		}

		public Integer getItemId() {
			return delegate.getItemId();
		}

		public void setItemId(Integer itemId) {
			delegate.setItemId(itemId);
		}

		public String getAction() {
			return delegate.getAction();
		}

		public void setAction(String action) {
			delegate.setAction(action);
		}

		public EventTypes getEventType() {
			return delegate.getEventType();
		}
		public void setEventType(EventTypes eventType) {
			delegate.setEventType(eventType);
		}
		public Integer getUserId() {
			return delegate.getUserId();
		}
		public void setUserId(Integer userId) {
			delegate.setUserId(userId);
		}
		public ObjectTypes getObjectType() {
			return delegate.getObjectType();
		}
		public void setObjectType(ObjectTypes objectType) {
			delegate.setObjectType(objectType);
		}

		public String getDetails() {
			return delegate.getDetails();
		}

		public void setDetails(String details) {
			delegate.setDetails(details);
		}

		public Integer getId() {
			return delegate.getId();
		}

		public void setId(Integer id) {
			delegate.setId(id);
		}

		public int hashCode() {
			return delegate.hashCode();
		}

		public boolean equals(Object obj) {
			return delegate.equals(obj);
		}

		@Override
		public String toString() {
			return "PropertyChange "+ getItemId() +" [field=" + field + ", previousValue=" + previousValue + ", newValue=" + newValue
					+ ", date=" + getDate() + "]";
		}
		
		
		
	}
}
