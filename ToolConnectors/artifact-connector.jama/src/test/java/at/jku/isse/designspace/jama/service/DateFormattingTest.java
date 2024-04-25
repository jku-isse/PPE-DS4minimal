package at.jku.isse.designspace.jama.service;

import static org.junit.jupiter.api.Assertions.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.junit.jupiter.api.Test;

class DateFormattingTest {

	static DateTimeFormatter formatter = new DateTimeFormatterBuilder()
		    // case insensitive to parse JAN and FEB
		    .parseCaseInsensitive()
		    // add pattern
		    .appendPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
		    // create formatter (use English Locale to parse month names)
		    .toFormatter(Locale.ENGLISH);
	
	static String date = "2023-03-07T12:29:10.033+0000";
	
	@Test
	void test() {
		
		
		OffsetDateTime odt = OffsetDateTime.parse(date, formatter);
		ZonedDateTime zdt = odt.toZonedDateTime();
		//ZonedDateTime.from(temporal)
		System.out.println(zdt);
	}

	
	@Test
	void testSTF() throws Exception {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
		    //sdf.setTimeZone(TimeZone.getTimeZone(ZoneId.of("Z")));
			Date result = sdf.parse(date);
			
		    System.out.println("date:"+result); //prints date in current locale
			ZonedDateTime zdt =  ZonedDateTime.ofInstant(result.toInstant(), ZoneOffset.UTC);

		    System.out.println(zdt);
	}
}
