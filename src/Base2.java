import java.util.*;
import java.io.*;
import java.lang.Math;

class Base2 implements Model{

	public List<Document> search(Task dd, Topic topic, Set<String> visited, String dir, int max_iter) throws IOException{
	//public List<Document> search(Task dd, Topic topic, Set<String> visited, String dir, int max_iter) throws IOException{
		List<Document> docs = new ArrayList<>();

		List<Document> top5;
		List<Feedback> top5_feedbacks;
		List<List<Document>> subtopicDocs = dd.retrieveSubtopics(topic, visited, dir);

		for(int i=0;i<15;i++){
			top5 = dd.explore(topic, visited, dir, subtopicDocs);
			for(Document doc:top5) visited.add(doc.doc_id);
			docs.addAll(top5);
		}
		return docs;
	}
}
