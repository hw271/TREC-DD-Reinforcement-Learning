import java.util.*;
enum Domain {
    Ebola, Illicit_Goods, Local_Politics
}

enum Action{
        Up, Down, Around, Stay
}

class Topic{
	String topic_id;
	String topic_name;
	final int num_of_subtopics;
	List<Subtopic> subtopics;

	public Topic(String topic_name, String topic_id, int num_of_subtopics){
		this.topic_id = topic_id;
		this.topic_name = topic_name;
		this.num_of_subtopics = num_of_subtopics;
		subtopics = new ArrayList<>();
	}
}

class Subtopic{
	String subtopic_id;
	String subtopic_name;
	Topic topic;
	public Subtopic(String subtopic_name, String subtopic_id, Topic topic){
		this.subtopic_id = subtopic_id;
		this.subtopic_name = subtopic_name;
		this.topic = topic;
	}

	@Override
	public String toString(){
		return subtopic_id;
	}
} 

class Document implements Comparable<Document>{
	String doc_id;
	double rel_score;
	public Document(String doc_id, String rel_score){
		this.doc_id = doc_id;
		this.rel_score = Double.valueOf(rel_score);
	}

	@Override
	public int compareTo(Document d1){
		if(rel_score<d1.rel_score) return 1;
		else if(rel_score==d1.rel_score) return 0;
		else return -1;
	}

	@Override 
	public String toString(){
		return "\n\t("+doc_id+" "+rel_score+")";
	}
}

class Feedback{
	String doc_id;
	String subtopic_id;
	String passage;
	double rel;
	public Feedback(String doc_id, String subtopic_id, String passage, double rel){
		this.doc_id = doc_id.replaceAll("\"", "");
		this.subtopic_id = subtopic_id.replaceAll("\"", "");
		this.passage = passage.replaceAll("\"", "");
		this.rel = rel;
	}

	@Override
	public String toString(){
		return "\n\t{doc_id="+this.doc_id+"\tsubtopic_id="+this.subtopic_id+"\trel_score="+rel+"}";
	}
}
