package org.thewheatfield.bruneitimes2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import android.os.AsyncTask;
import android.util.Log;
/*
 * Specify date in yyyy-mm-dd
 */
public class DownloadNumberOfPages extends AsyncTask<String, Void, String> {
	private IDownloadPageCallback mCallback = null;
	private String mDate = null;
	public DownloadNumberOfPages(IDownloadPageCallback callback){
		mCallback = callback;
	}
	public void setCallback(IDownloadPageCallback c){
		mCallback = c;
	}
	
	String mode = "";
	// http://epaper.bt.com.bn/bruneitimes/ipad/reader_json.jsp?id=131017bruneitimes
	// http://epaper.bt.com.bn/bruneitimes/ipad/reader_json.jsp?id=yymmddbruneitimes
	@Override
	protected String doInBackground(String... arg) {
		int pages = 0;
		
		if(arg.length > 0 && arg[0] != null && arg[0].trim() != "" )
			mDate = arg[0];
		else{
			
			Date d = new Date();
			mDate = BT.DATE_FORMAT.format(d);
		}
		
		// BTConstants.URL_METADATA
		try {
			String theURL = "";
			for(String str : BT.NUM_PAGES_URL){
				if(str == "")
					theURL += mDate;
				else
					theURL += str;
			}
			Log.i("PageDownloader", "Download Pages from: " + theURL);
			
            HttpClient client = new DefaultHttpClient();
            HttpPost request = new HttpPost(theURL);               
            request.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.2; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/32.0.1667.0 Safari/537.36");               

            HttpResponse response = client.execute(request);			
			BufferedReader in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			StringBuffer sb = new StringBuffer();
			for (String line; (line = in.readLine()) != null;) {
				sb.append(line);
			}			
			in.close();
			Pattern pattern = Pattern.compile("images/thumbnails/", Pattern.CASE_INSENSITIVE);
			Matcher  matcher = pattern.matcher(sb.toString());
			Log.d("PageDownlaoder", "Entire page dump" + sb.toString());
	        while (matcher.find())
	            pages++;
			Log.i("PageDownloader", "Found " + pages + " pages");
		} catch (MalformedURLException e) {
			Log.e("PageDownloader", "EXPTN! -1 " + e.getMessage() + "\n" + e.getStackTrace());
			return "-1 : " + e.getMessage();
		} catch (IOException e) {
			Log.e("PageDownloader", "EXPTN! -2 " + e.getMessage() + "\n" + e.getStackTrace());
			return "-2 : " + e.getMessage();
		}
		return "" + pages;
	}

	@Override
	protected void onPostExecute(String result) {
		if(mCallback != null)
			mCallback.process(result);
	}
}