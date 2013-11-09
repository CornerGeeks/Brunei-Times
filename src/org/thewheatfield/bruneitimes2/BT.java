package org.thewheatfield.bruneitimes2;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;



public class BT {
	protected final static String FOLDER_NAME = "bt";
	protected final static String IMAGE_POSITION = "org.thewheatfield.bruneitimes.IMAGE_POSITION";
	protected final static String DATE_DISPLAY_FORMAT_str = "dd/MMM";
	protected final static SimpleDateFormat DATE_DISPLAY_FORMAT = new SimpleDateFormat(DATE_DISPLAY_FORMAT_str);

	protected final static String DATE_FORMAT_str = "yyMMdd";
	protected final static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(DATE_FORMAT_str);

	protected final static String FOLDER_DATE_FORMAT_str = "yyyy-MM-dd";
	protected final static SimpleDateFormat FOLDER_DATE_FORMAT = new SimpleDateFormat(FOLDER_DATE_FORMAT_str);
	
	// protected final static String[] NUM_PAGES_URL  = new String[] {"http://epaper.bt.com.bn/bruneitimes/ipad/reader_json.jsp?id=", "", "bruneitimes"};
	protected final static String[] NUM_PAGES_URL  = new String[] {"http://epaper.bt.com.bn/bruneitimes/books/", "", "bruneitimes/"};

	protected static String getDisplayDate(String date){
		try {
			Date d = BT.DATE_FORMAT.parse(date);
			return BT.DATE_DISPLAY_FORMAT.format(d);
		} catch (ParseException e) {
			return "";
		}
	}
}
