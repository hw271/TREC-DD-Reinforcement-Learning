//List[List[document-id, subtopic-id, passage]]
//extract content between [doc_id, subtopic_id, passage]
//1.replaceAll "[[" as"[" and "]]" as "]"
//2.extract all content between [ and ], each will be a triplet of doc_id, subtopic_id and passage

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.*;

public class FeedbackParser{
	public List<Feedback> parse(String line){
		List<Feedback> feedbacks = new ArrayList<>();
		//pattern: valid feedback always start with [" and end with \\d]
		Pattern p = Pattern.compile("\\[\".*?\\d\\]");
        	Matcher m = p.matcher(line);
        while(m.find()) {
            String l1 = m.group();
            l1 = l1.substring(1, l1.length()-1);
            int length = l1.length();
            if(length<=5) continue;
            String[] elems = parseHelper(l1);
            if(elems!=null){
		try{
 	       	double rel = Double.valueOf(l1.substring(length-1, length));
            	feedbacks.add(new Feedback(elems[0], elems[1], elems[2], rel));
        	}catch(Exception e){
			System.out.println(line);
			System.out.println(l1);
			System.err.println(e.getMessage());
		}
		}
        }
        return feedbacks;
	}

	private String[] parseHelper(String l){
		//l = "\"com_blackhatworld_www_574bcc3331c2d17dbdc595acd530ee3756a9307a_1422586088058\", \"DD15-89.1\", \"It is certainly illegal in the UK; unless you can prove the email was obtained through the normal course of business. \", 2";
		String regex = "\"([^\"]*)\"";
        Pattern p = Pattern.compile(regex);
    	Matcher m = p.matcher(l);
    	String doc_id = null, subtopic_id = null, passage = null;
    	if(m.find()) {
	    	doc_id = m.group();
	    	//System.out.println(doc_id);
	    }
	    if(m.find()) {
	    	subtopic_id = m.group();
	    	//System.out.println(subtopic_id);
    	}
    	if(m.find()) {
	    	passage = m.group();
	    	//System.out.println(passage);
    	}
    	if(doc_id!=null && subtopic_id!=null && passage!=null)
    		return new String[]{doc_id, subtopic_id, passage};
    	else return null;

	}
	
	public static void main(String[] args) throws IOException{
		BufferedReader br = new BufferedReader(new FileReader(new File("test.txt")));
		String input;
		while((input=br.readLine())!=null){
			List<Feedback> feedbacks = new FeedbackParser().parse(input);
			System.out.println(feedbacks);
		}
		
	}
	


}
	
