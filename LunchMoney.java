import java.io.*;
import java.net.*;
import java.util.*;
import java.text.*;
import java.lang.Math;
import java.util.concurrent.TimeUnit;


public class LunchMoney {
	
	public double momBalance;
	public double dadBalance;
	public String oldBalance;
	public double momBalChanged = 0;
	public double dadBalChanged = 0;
	
	public Date lastDate;
	public String newDate;
	
	private Hashtable<String, String> formData = new Hashtable<String, String>(); //Hashtable of all <input> values
	private List<LunchMoneyEntry> list = new ArrayList<LunchMoneyEntry>(); //List of all LunchMoneyEntries created by program
	
	public SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
	
	private boolean nextPage = true;
	private boolean isFirstDate = true;
	
	
	public void getFormData(String src) { //find all <input> fields and their name and values and put to a hashtable
		
		int pos = 0;
		
		while (true) {
			ParseResult input = Parser.parse(src, "<input", ">", pos);
			
			if (input == null) {
				break;
			}
			ParseResult name = Parser.parse(input.substring, "name=\"", "\"", 0);
			
			if (name != null) {
				ParseResult value = Parser.parse(input.substring, "value=\"", "\"", 0);
				
				if (value != null) {
					formData.put(name.substring, value.substring);
				} else {
					formData.put(name.substring, "");
				}
			}
			pos = input.index;
		}
	}
	
	public String createFormString() throws UnsupportedEncodingException { //turns formData hashtable into String used for POST
		
		StringBuilder sb = new StringBuilder();
		boolean isFirst = true;
		
		for (Map.Entry<String, String> entry:formData.entrySet()) {    
			String key = URLEncoder.encode(entry.getKey(), "UTF-8");   
			String val = URLEncoder.encode(entry.getValue(), "UTF-8");
			
			if (!isFirst) {
				sb.append("&");
			} else {
				isFirst = false;
			}
			sb.append(key);
			sb.append("=");
			sb.append(val);
		}
		return sb.toString();
	}
	
	public void createLunchMoneyEntries(String source) throws ParseException { //reads page source and gets transaction data
		
		int pos = 0;
		
		while (true) {
			ParseResult transaction = Parser.parse (source, "<td align=\"right\" valign=\"top\"", "</tr>", pos);
			
			if (transaction == null) {
				break;
			}
			ParseResult date = Parser.parse(transaction.substring, ">", "</td>", 0);
			
			LunchMoneyEntry entry = new LunchMoneyEntry();
			entry.date = dateFormat.parse(date.substring);
			
			//gets most recent date
			if (isFirstDate) {
				newDate = date.substring;
			}
			
			if (entry.date.equals(lastDate)) {
				nextPage = false;
				break;
			}
			
			//find if transaction happened at dads house
			Date control = dateFormat.parse("10/28/2019");
			long diffInMill = Math.abs(entry.date.getTime() - control.getTime());
			long diff = TimeUnit.DAYS.convert(diffInMill, TimeUnit.MILLISECONDS);
			
			if (Math.floor(diff / 7) % 2 == 0) {
				entry.isAtDads = true;
			}
			
			ParseResult action = Parser.parse(transaction.substring, "<td style=\"width:300px;\">", "<br/>", 0);
			
			if (action != null) { //if purchase...
				ParseResult value = Parser.parse(transaction.substring, "<td align=\"right\" valign=\"top\"></td><td align=\"right\" valign=\"top\">$", "</td>", 0);
				entry.isPurchase = true;
				
				if (value != null) {
					entry.amount = Double.parseDouble(value.substring);
				}
				
			} else { //if deposit...
				action = Parser.parse(transaction.substring, ">", "</td>", 0);
				
				if (action != null) {
					ParseResult value = Parser.parse(transaction.substring, "<td align=\"right\" valign=\"top\"></td><td align=\"right\" valign=\"top\">$", "</td>", 0);
					entry.isPurchase = false;
					
					if (value != null) {
						entry.amount = Double.parseDouble(value.substring);
					}
				}
			}
			list.add(entry);
			pos = transaction.index;
			
			isFirstDate = false;
		}
	}
	
	public void getOldData() throws ParseException, IOException {
		
		String lunchMoneyData = FileManager.readFile("C:\\Users\\Kevin Yu\\codes\\LunchMoneyData.txt");
		
		ParseResult momBal = Parser.parse(lunchMoneyData, "Mom's Balance: [", "]", 0);
		ParseResult dadBal = Parser.parse(lunchMoneyData, "Dad's Balance: [", "]", 0);
		ParseResult oldBal = Parser.parse(lunchMoneyData, "Total Account Balance: [", "]", 0);
		ParseResult lastAccessDate = Parser.parse(lunchMoneyData, "Date Last Updated: [", "]", 0);
		
		momBalance = Double.parseDouble(momBal.substring);
		dadBalance = Double.parseDouble(dadBal.substring);
		oldBalance = oldBal.substring;
		lastDate = dateFormat.parse(lastAccessDate.substring);
	}
	
	public void doLunchMoneyCalc() { //gos through list of transactions and does calculations
		
		for (LunchMoneyEntry e : list) {
			if ((e.date).after(lastDate)) { //if the transaction date is after the last accessed date
				if (e.isAtDads) {
					if (e.isPurchase) {
						dadBalance = dadBalance - e.amount;
						dadBalChanged = dadBalChanged - e.amount;
					} else {
						dadBalance = dadBalance + e.amount;
						dadBalChanged = dadBalChanged + e.amount;
					}
				} else {
					if (e.isPurchase) {
						momBalance = momBalance - e.amount;
						momBalChanged = momBalChanged - e.amount;
					} else {
						momBalance = momBalance + e.amount;
						momBalChanged = momBalChanged + e.amount;
					}
				}
			}
		}
	}
	
    public static void main(String[] args) {
		
		try { 
			//get the login page and its formdata
			PageGrabber x = new PageGrabber();
			String source = x.grab("https://sendmoneytoschool.com/Dashboard/Login.aspx", "GET", null);
					
			LunchMoney y = new LunchMoney();
			y.getFormData(source);
			
			y.formData.put("ctl00$content$email", "[censored for obvious reasons]");
			y.formData.put("ctl00$content$password", "[censored for obvious reasons]");
			
			//send in form data
			source = x.grab("https://sendmoneytoschool.com/Dashboard/Login.aspx", "POST", y.createFormString());
			source = x.grab("https://sendmoneytoschool.com" + x.newLocation, "GET", null);
			
			ParseResult bal = Parser.parse(source, "<span class=\"student-balance\">", "</span>", 0);
			
			//to cover negative account balance
			if (bal == null) {
				bal = Parser.parse(source, "<span class=\"student-balance negative\">", "</span>", 0);
			}
			
			//find redirect link for next page and go there
			ParseResult loc = Parser.parse(source, "<a id=\"ctl00_content_students_ctl00_ctl04\" class=\"super-link activity\" href=\"", "\" target=\"\" title=\"\">", 0);
			source = x.grab("https://sendmoneytoschool.com" + loc.substring, "GET", null);
			
			
			y.getOldData();
			
			System.out.println("Old Account Balance = " + y.oldBalance);
			System.out.println("Old Mom's Balance = " + y.momBalance);
			System.out.println("Old Dad's Balance = " + y.dadBalance);

			
			while (y.nextPage) {
				y.createLunchMoneyEntries(source);
				
				y.formData.clear();
				y.getFormData(source);
				y.formData.remove("ctl00$content$ctl00");
				y.formData.remove("ctl00$content$RequestHistoryButton");
				y.formData.remove("ctl00$content$newerLink");
				
				y.formData.put("ctl00$content$olderLink", "Older >>"); //as a contingency
				
				source = x.grab("https://sendmoneytoschool.com" + loc.substring, "POST", y.createFormString());
			}
			
			y.doLunchMoneyCalc();
			
			System.out.println("\nAmount Dad's Bal Changed Since Last Update = " + y.dadBalChanged);
			System.out.println("Amount Mom's Bal Changed Since Last Update = " + y.momBalChanged);
			
			System.out.println("\nNew Account Balance = " + bal.substring);
			System.out.println("New Mom's Balance = " + y.momBalance);
			System.out.println("New Dad's Balance = " + y.dadBalance);
			
			//write to file to be used again
			FileManager.writeFile("Mom's Balance: [" + y.momBalance + "]\nDad's Balance: [" + y.dadBalance + "]\nTotal Account Balance: [" + bal.substring + "]\nDate Last Updated: [" + y.newDate + "]", "LunchMoneyData");
			
		} catch (MalformedURLException e) {
			System.out.println(e.getMessage());
			e.printStackTrace(); 
			
		} catch (IOException e) {
			System.out.println(e.getMessage());
			e.printStackTrace(); 
			
		} catch (ParseException e) {
			System.out.println(e.getMessage());
			e.printStackTrace(); 
			
		} 


	}
                     
}