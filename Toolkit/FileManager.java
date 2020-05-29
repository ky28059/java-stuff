import java.io.*;

public class FileManager {
	
	public static void writeFile(String content, String fileName) throws IOException {
		FileWriter fw = new FileWriter(fileName + ".txt"); 
  
        // read character wise from string and write into FileWriter  
        for (int i = 0; i < content.length(); i++) {
            fw.write(content.charAt(i)); 
		}
        fw.close();
	}
	
	public static void writeFile(String[][] content, String fileName) throws IOException {
		FileWriter fw = new FileWriter(fileName + ".txt"); 
		
		for (String[] row : content) { //tabular style matrix reading
			String rowData = "";
			
			for (String column : row) {
				rowData = rowData + column + " ";
			}
			// read character wise from string and write into FileWriter
			for (int i = 0; i < rowData.length(); i++) {
	            fw.write(rowData.charAt(i)); 
			}
			fw.write("\n");
		}
        fw.close();
	}
	
	public static String readFile(String path) throws IOException { 
        int ch; 
  
        // check if File exists or not 
        FileReader fr = null; 

        fr = new FileReader(path); 
		StringBuilder sb = new StringBuilder();
        
        // read from FileReader till the end of file 
        while ((ch = fr.read()) != -1) 
            sb.append((char)ch); 
		
        fr.close(); 
		return sb.toString();
    } 
	
}
