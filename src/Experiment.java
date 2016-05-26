import java.util.*;
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class Experiment{
	//static int MAX_ITERATION = 40;
	public static void main(String[] args) throws IOException{
		ModelFactory factory = new ModelFactory();
		String[] models = {
			"Epsilon_0.1"
			,"Boltzmann_10"
			,"UCB" 
			,"POMDP_1","POMDP_2"
		};
		
		int[] iteration = {3};

		for(int max_iter: iteration){
			for(int i=0;i<models.length;i++){
					System.out.print(models[i]+":\t");
					Model model = factory.getModel(models[i]);
					Experiment exp = new Experiment();
					String run_file = models[i]+"_iter"+String.valueOf(max_iter);
					//use model to do experiment, the result is written in run_file;
					exp.dd_search(model, max_iter, run_file);
			}
		}
	}

	public void dd_search(Model model, int max_iter, String run_file) throws IOException{
		File f = new File("../eval/"+run_file);
		f.createNewFile();
		BufferedWriter writer = new BufferedWriter(new FileWriter(f));
		
		TopicFactory factory = new TopicFactory();
		List<Topic> topics = null;
		String dir = "";
			
		for(Domain domain:Domain.values()){
			//get topics and retrieved documents' file path
			topics = factory.getTopicList(domain);
			switch(domain){
				case Ebola:
					dir = "../data/retrieval_docs/Ebola/";
					break;
				case Illicit_Goods:
					dir = "../data/retrieval_docs/Illicit_Goods/";
					break;
				case Local_Politics:
					dir = "../data/retrieval_docs/Local_Politics/";
					break;
			}
			System.out.print(domain+",");

			DD dd = new DD();
			
			for(Topic topic:topics){
				Set<String> visited = new HashSet<String>();
				//System.out.println(topic.topic_id+": "+topic.topic_name);
				
				String topic_id = topic.topic_id;
				topic_id = topic_id.substring(5, topic_id.length());
				
				
				//System.out.println("\tExperiment.java:topic_id: "+topic_id);
				List<Document> retrieved_docs = model.search(dd, topic, visited, dir, max_iter);
				//System.out.println("\tExperiment.java:document list: "+retrieved_docs);
				for(int i=0;i<retrieved_docs.size();i++){
					Document doc = retrieved_docs.get(i);
					writer.write(topic_id+"\tQ0\t"+doc.doc_id+"\t"+(i+1)+"\t"+doc.rel_score+"\tindri\n");
				}
			}
		}
		writer.close();
		System.out.println("evaluation:"+run_file);
		Evaluator dd_eval = new Evaluator();
		dd_eval.evaluate(run_file);
		dd_eval.cubeTest(run_file);
		//dd_eval.evaluate2(run_file, 10);
		//dd_eval.evaluate2(run_file, 1000);
	}
}
