import java.util.*;
import java.io.*;
import java.lang.Math;

class Base1 implements Model{
	//search using topic_name
	public List<Document> search(Task dd, Topic topic, Set<String> visited, String dir, int max_iter) throws IOException{
	//public List<Document> search(Task dd, Topic topic, Set<String> visited, String dir, int max_iter) throws IOException{
		List<Document> docs = new ArrayList<>();

		List<Document> top5;
		List<Feedback> top5_feedbacks;
		List<List<Document>> subtopicDocs = dd.retrieveSubtopics(topic, visited, dir);			
		
		for(int i=0;i<max_iter;i++){
			top5 = dd.exploit(topic, visited, dir);
			for(Document doc:top5) visited.add(doc.doc_id);
			docs.addAll(top5);
		}
		return docs;
	}
}
