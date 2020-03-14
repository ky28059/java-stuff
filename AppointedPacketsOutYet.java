import java.io.*;
import java.net.*;

public class AppointedPacketsOutYet {
	
	public static void main(String[] args) {
		try {
			PageGrabber x = new PageGrabber();
			String source = x.grab("https://www.gunnsec.org/asb-elections--appointment-info.html", "GET", null);
			
			ParseResult y = Parser.parse(source, "<a title=\"Download file: (OLD) ASB Appointed Officer Application\"", "</a>", 0);
			
			if (y == null) {
				System.out.println("Appointed Packets have been updated!");
			} else {
				System.out.println("Not yet, I'm afraid.");
			}
			
		} catch (MalformedURLException e) {
			System.out.println(e.getMessage());
			e.printStackTrace(); 
			
		} catch (IOException e) {
			System.out.println(e.getMessage());
			e.printStackTrace(); 
			
		}
	}
}