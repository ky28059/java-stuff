import java.io.*;
import java.net.*;
import java.util.*;


public class PageGrabber {
	
	private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.88 Safari/537.36";
	
	public Hashtable<String, String> cookies = new Hashtable<String, String>(); 
	public String newLocation = null;
	
	
	private static String read(HttpURLConnection con) throws IOException {
		
		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		
		try { 
			String line;
			StringBuilder sb = new StringBuilder();
	
			while ((line = in.readLine()) != null) {
				sb.append(line);
				sb.append(System.lineSeparator());		
				
			}
			return sb.toString();
			
		} finally {
			in.close();
		}
		
	}
	
	private String generateCookieString() {
		
		if (cookies == null){
			return null;
		}
		
		StringBuilder sb = new StringBuilder();
		boolean isFirst = true;
		
		//for each member of hashtable cookies, get key and value and append to string
		for (Map.Entry<String, String> entry:cookies.entrySet()) {    
			String key = entry.getKey();  
			String val = entry.getValue();  
			
			if (!isFirst) {
				sb.append("; ");
				
			} else {
				isFirst = false;
			}
			sb.append(key);
			sb.append("=");
			sb.append(val);
		}
		//return the hashtable
		return sb.toString();

	}
	
	private void setCookies(HttpURLConnection connection) {
		
		List<String> newCookies = connection.getHeaderFields().get("Set-Cookie");
		
		if (newCookies == null) {
			return;
		}
		
		for (String c : newCookies) {
			String s = c.split(";", 2)[0];
			int pos = s.indexOf("=");
			
			//splits the cookie into name and value
			String name = s.substring(0, pos);
			String val = s.substring(pos + 1, s.length());
			
			//puts the cookie into the cookies hashtable
			cookies.put(name, val);
		}
		
	}
	
	public String grab(String link, String requestMethod, String postBody) throws MalformedURLException, IOException {
		
		URL url = new URL(link);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod(requestMethod);
		connection.setRequestProperty("User-Agent", USER_AGENT);
		connection.setFollowRedirects(false);
		
		//if there are already valid cookies, use them
		String cookie = generateCookieString();
		
		if (cookie != null) {
			connection.addRequestProperty("Cookie", cookie);
		}
		
		//outputs form data
		if (requestMethod == "POST") {
			connection.setDoOutput(true);
			OutputStream os = connection.getOutputStream();
			os.write(postBody.getBytes());
			os.flush();
			os.close();
		}
		
		//sets cookies
		setCookies(connection);
		
		int responseCode = connection.getResponseCode();
		

		if (responseCode == HttpURLConnection.HTTP_OK) {
			//if code = 200, read page
			return read(connection);
			
		} else if (responseCode == HttpURLConnection.HTTP_MOVED_TEMP) {
			//if code = 302, find redirect location
			List<String> redirect = connection.getHeaderFields().get("Location");
			newLocation = redirect.get(0);
			
			return read(connection);
			
		} else {
			return "request not working, return code = " + responseCode;
			
		}

	}
	
}