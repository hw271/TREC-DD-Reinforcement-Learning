import java.util.*;
import java.io.*;
import java.lang.Math;

class Boltzmann implements Model{
	
	double t;
	public Boltzmann(double t){
		this.t = t;
	}

	public List<Document> search(Task dd, Topic topic, Set<String> visited, String dir, int max_iter) throws IOException{
		List<Document> docs = new ArrayList<>();

		Set<String> subtopics = new HashSet<>();
		List<List<Document>> subtopicDocs = dd.retrieveSubtopics(topic, visited, dir);
		
		//double[0]: # of times the action is used, double[1]: total reward
		Map<Action, double[]> actionMap = new HashMap<>();
		for(Action action:Action.values()){
			actionMap.put(action, new double[]{0,0});
		}

		boolean start=true;
		int pre_rel = 0;
		int num_of_sub = topic.subtopics.size();
		int iter = 0;
		boolean atRoot=true;

		List<Document> top5 = null;
		List<Feedback> top5_feedbacks = null;
		Action action = null;
		String cur = topic.topic_id, next;

		int negIter = 0;
		
		List<Action> actSeq = new ArrayList<>();
		//while(start || pre_rel!=0 || subtopics.size()<num_of_sub){
		while(true){
			if(start){
				start = false;
				top5 = dd.exploit(topic.topic_id, visited, dir);
				top5_feedbacks = dd.getFeedbacks(top5, topic.topic_id);
				pre_rel=dd.getRelScore(top5_feedbacks);
				action = Action.Up;
				actionMap.put(action, new double[]{1,pre_rel});
				next = dd.getCurTopic(topic, subtopics, top5_feedbacks);
				atRoot = true;
			}else{
				if(action==Action.Up ||(action==Action.Stay && atRoot)) atRoot =true;
				else atRoot = false;
				
				action = chooseAction(actionMap, atRoot);
	
				switch(action){
					case Up:
						top5 = dd.exploit(topic.topic_id, visited, dir);
						top5_feedbacks = dd.getFeedbacks(top5, topic.topic_id);
						pre_rel=dd.getRelScore(top5_feedbacks);
						break;
					case Stay:	
					case Down:
						top5 = dd.exploit(cur, visited, dir);
						top5_feedbacks = dd.getFeedbacks(top5, topic.topic_id);
						pre_rel=dd.getRelScore(top5_feedbacks);
						break;					
					case Around:
						top5 = dd.explore(topic, visited, dir, subtopicDocs);
						top5_feedbacks = dd.getFeedbacks(top5, topic.topic_id);
						pre_rel=dd.getRelScore(top5_feedbacks);	
						break;
				}
			}
			//update visted and subtopics
			for(Document doc:top5) visited.add(doc.doc_id);
			for(Feedback feedback:top5_feedbacks) subtopics.add(feedback.subtopic_id);
			//update docs
			docs.addAll(top5);

			double[] value = actionMap.get(action);
			value[0]+=1;
			value[1]+=pre_rel;
			actionMap.put(action, value);

			cur = dd.getCurTopic(topic, subtopics, top5_feedbacks);
			next = cur;

			actSeq.add(action);
			if(++iter>=max_iter)break;
		}

		BufferedWriter bw = new BufferedWriter(new FileWriter(new File("../action/Boltzmann_iter_"+max_iter+".actions"), true));
		for(Action a:actSeq){
			bw.write(a.toString()+",");
		}

		bw.write("\n");
		bw.close();

		return docs;
	}

	private Action chooseAction(Map<Action, double[]> actionMap, boolean atRoot){
		double x = new Random().nextDouble();

		double denominator = 0;
		for(Action action: Action.values()){
			if(atRoot && action==Action.Up) continue;
			if(!atRoot && action==Action.Down) continue;
			double[] values = actionMap.get(action);
			double mean = values[0]!=0?values[1]/values[0]:0;
			denominator+=Math.exp(mean/t);
		}

		double numerator = 0;
		for(Action action:Action.values()){
			if(atRoot && action==Action.Up) continue;
			if(!atRoot && action==Action.Down) continue;
			double[] values = actionMap.get(action);
			double mean = values[0]!=0?values[1]/values[0]:0;
			numerator+=Math.exp(mean/t);

			if(x<numerator/denominator){
				return action;
			}
		}
		return Action.Around;
	}
}
