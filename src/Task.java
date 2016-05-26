import java.util.*;
import java.io.*;

interface Task{
	List<Document> exploit(Topic topic, Set<String> visited, String dir) throws IOException;
	List<Document> exploit(String topic_id, Set<String> visited, String dir) throws IOException;
	
	List<Document> explore(Topic topic, Set<String> visited, String dir, List<List<Document>> subtopicDocs) throws IOException;
	List<List<Document>> retrieveSubtopics(Topic topic, Set<String> visited, String dir) throws IOException;
	List<Feedback> getFeedbacks(List<Document> top5, String topic_id) throws IOException;
	List<Document> getDocs(String id, String dir) throws IOException;
	
	int getRelScore(List<Feedback> feedbacks);
	String getCurTopic(Topic topic, Set<String> visited_subtopics, List<Feedback> feedbacks);
}
