
import java.util.*;
import java.io.*;

public class TopicFactory{
	/*Topic Factory:
	This code will generate a List<Topic>
	Topic: topic_id, topic_name, final num_of_subtopics
	Subtopic extends topic: subtopic_id, subtopic_name
	/Users/hw271/Google Drive/thesis/experiments/pomdp/src/step1_parseXML/
	/Users/hw271/Google Drive/thesis/experiments/pomdp/topcis
	*/
	List<Topic> getTopicList(Domain domain) throws IOException{
		List<Topic> topics = null;
		BufferedReader reader = null;
		switch(domain){
			case Ebola:
				//System.out.println("Ebola");
				reader = new BufferedReader(new FileReader(new File("../data/topics/Ebola.txt")));
				break;
			case Illicit_Goods:
				//System.out.println("Illicit_Goods");
				reader = new BufferedReader(new FileReader(new File("../data/topics/Illicit_Goods.txt")));
				break;
			case Local_Politics:
				//System.out.println("Local_Politics");
				reader = new BufferedReader(new FileReader(new File("../data/topics/Local_Politics.txt")));
				break;
		}
		if(reader==null){
			throw new FileNotFoundException();
		}else{
			String line = reader.readLine();
			//parse domain line: domain_name | # of topics
			String[] elem = line.split(" \\| ");
			String domain_name = elem[0];
			int num_of_topics = Integer.valueOf(elem[1]);

			topics = new ArrayList<>(num_of_topics);
			for(int i=0;i<num_of_topics;i++){
				line = reader.readLine();
				//parse topic line: topic_name | topic_id | # of subtopics
				elem = line.split(" \\| ");
				Topic topic = new Topic(elem[0], elem[1], Integer.valueOf(elem[2]));
				for(int j = 0;j<topic.num_of_subtopics;j++){
					line = reader.readLine();
					//parse subtopic line: subtopic_name | subtopic_id
					elem = line.split(" \\| ");
					Subtopic subtopic = new Subtopic(elem[0], elem[1], topic);
					topic.subtopics.add(subtopic);
				}
				topics.add(topic);
			}
		}
		return topics;
	}

	public static void main(String[] args) throws Exception{
		List<Topic> topics = new TopicFactory().getTopicList(Domain.Ebola);
		System.out.println();
	}

}


