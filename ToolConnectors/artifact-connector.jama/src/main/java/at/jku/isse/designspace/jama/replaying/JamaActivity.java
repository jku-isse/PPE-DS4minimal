package at.jku.isse.designspace.jama.replaying;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.Map;

import at.jku.isse.designspace.jama.utility.AccessToolsJSON;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
public class JamaActivity {

	public enum EventTypes {UPDATE, CREATE, DELETE, BATCH_UPDATE, ADDED_COMMENT, INTEGRATION, MOVE, BATCH_SUMMARY, BATCH_DELETE, COPY, BATCH_CREATE, UNSUPPORTED};
	public enum ObjectTypes {ITEM, REVISION_ITEM, COMMENT, RELATIONSHIP, INTEGRATION, MISCELLANEOUS, ITEM_TAG, PROJECT, UNSUPPORTED};
	
    protected Integer id;
	
    @EqualsAndHashCode.Exclude protected ZonedDateTime date;
    @EqualsAndHashCode.Exclude protected Integer itemId;
    @EqualsAndHashCode.Exclude protected String action;
    @EqualsAndHashCode.Exclude protected EventTypes eventType;
    @EqualsAndHashCode.Exclude protected Integer userId;
    @EqualsAndHashCode.Exclude protected ObjectTypes objectType;
    @EqualsAndHashCode.Exclude protected String details;
	

	
	protected String getResourceUrl() {
		return "activities/" + id;
	}

	@Override
	public String toString() {
		return "JamaActivity [date=" + date + ", item=" + itemId + ", action=" + action + ", eventType=" + eventType
				+ ", causedBy=" + userId + ", objectType=" + objectType + ", getId()=" + id + "]";
	}

	public Integer getItemId() {
		return itemId;
	}

	public Integer getUserId() {
		return userId;
	}

	public String getDetails() {
		return details;
	}


	
	public static JamaActivity fromJson(Map<String, Object> actJson) {
		JamaActivity ja = new JamaActivity();
		Integer itemId = AccessToolsJSON.accessInteger(actJson, "itemId");
		if (itemId == -1) // new format needs to be used
			itemId = AccessToolsJSON.accessInteger(actJson, "item");
		ja.itemId=itemId;

		Integer modifiedById = AccessToolsJSON.accessInteger(actJson, "userId");
		if (modifiedById == -1) // new format needs to be used
			modifiedById = AccessToolsJSON.accessInteger(actJson, "user");
		ja.userId = modifiedById;

		ja.action = AccessToolsJSON.accessString(actJson, "action");
		String objectTypeStr = AccessToolsJSON.accessString(actJson, "objectType");
		String eventTypeStr = AccessToolsJSON.accessString(actJson, "eventType");
		EventTypes et;
		try {
			et = EventTypes.valueOf(eventTypeStr);
		} catch(Exception e) {
			et = EventTypes.UNSUPPORTED;
		}
		try {
			ja.objectType = ObjectTypes.valueOf(objectTypeStr);
		} catch(Exception e) {
			ja.objectType = ObjectTypes.UNSUPPORTED;
		}	

		if (ja.getObjectType().equals(JamaActivity.ObjectTypes.COMMENT)) {
			ja.eventType=JamaActivity.EventTypes.ADDED_COMMENT;
		} else if (ja.getObjectType().equals(JamaActivity.ObjectTypes.INTEGRATION)) { 
			ja.eventType =JamaActivity.EventTypes.INTEGRATION;
		} else {
			ja.eventType = et;
		}
		ja.date = requestZonedDate(actJson, "date");
		ja.details = AccessToolsJSON.accessString(actJson, "details");  

		ja.id = AccessToolsJSON.accessInteger(actJson, "id");	        
		return ja;
	}
	
	static DateTimeFormatter formatter = new DateTimeFormatterBuilder()
		    // case insensitive to parse JAN and FEB
		    .parseCaseInsensitive()
		    // add pattern
		    .appendPattern("MMM d, yyyy h:mm:ss a")
		    // create formatter (use English Locale to parse month names)
		    .toFormatter(Locale.ENGLISH);
	
	static DateTimeFormatter formatterISO = new DateTimeFormatterBuilder()
		    .parseCaseInsensitive()
		    // add pattern
		    .appendPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
		    .toFormatter(Locale.ENGLISH);
	
    private static ZonedDateTime requestZonedDate(Map<String, Object> actJson, String key) throws DateTimeParseException {
    		String dateString = AccessToolsJSON.accessString(actJson, key);
    		if (dateString != null && dateString.length() > 0) {
    			if (Character.isDigit(dateString.charAt(0))) {
    				// iso date
    				OffsetDateTime odt = OffsetDateTime.parse(dateString, formatterISO);
    				ZonedDateTime zdt = odt.toZonedDateTime();
    			} else {
    				LocalDateTime parsedDate = LocalDateTime.parse(dateString, formatter);
    	    		return parsedDate.atZone(ZoneId.of("UTC"));
    			}
    		}
    		return ZonedDateTime.now(ZoneId.of("UTC"));
    		
    }
	
}
