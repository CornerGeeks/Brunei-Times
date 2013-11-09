package org.thewheatfield.bruneitimes2;
import java.text.ParseException;
import java.util.Date;

import android.util.Log;

import com.nostra13.universalimageloader.cache.disc.naming.*;;
public class BTFileGenerator implements FileNameGenerator{

	private String EXTENSION = ".jpg";
	@Override
	// "http://epaper.bt.com.bn/bruneitimes/ipad/getZoom.jsp?id=131014bruneitimes&file=Zoom-1.jpg",
	public String generate(String imageURL) {
		try{
			int startOfDate = imageURL.indexOf("id=")+3;
			String date = imageURL.substring(startOfDate, startOfDate+6);
			if(imageURL.indexOf("Zoom-") != -1){
				String page = imageURL.substring(imageURL.indexOf("Zoom-")+5).replace(EXTENSION, "");
				return BTFileGenerator.getFileName(date, page);
			}
		}
		catch(Exception e){
			Log.d("filename", "Exception" + e.getMessage());	 
		}
		
		return String.valueOf(imageURL.hashCode());
	}
	public static String getFolderName(String date){
		try {
			return BT.FOLDER_DATE_FORMAT.format(BT.DATE_FORMAT.parse(date));
		} catch (ParseException e) {
			return "";
		}
	}
	
	public static String getFileName(String date, String page){
		try{
			return getFileName(date, Integer.parseInt(page));
		}
		catch(Exception e){
			return (date+page).hashCode()+"";
		}
	}
	public static String getFileName(String date, int page){
		try{
			String folder = getFolderName(date);
			if(folder != "")
				return folder + "/" + (page<10?"0":"")+page + ".jpg";
		}catch(Exception e){
		}
		return (date+page).hashCode()+"";
	}

}
