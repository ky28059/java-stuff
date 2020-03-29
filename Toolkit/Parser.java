public class Parser {
	public static ParseResult parse(String src, String parseForL, String parseForR, int startIndex) {
		
		//find left and right
		int left = src.indexOf(parseForL, startIndex);
		
		if (left == -1) {
			return null;
		}
		
		int right = src.indexOf(parseForR, left + parseForL.length());
			
		if (right == -1) {
			return null;
		}
		
		String sub = src.substring(left + parseForL.length(), right);
		
		ParseResult x = new ParseResult();
		x.substring = sub;
		x.index = right + parseForR.length();
		return x;
		
	}
}