//epsilon-greedy
import java.util.*;
import java.io.*;
import java.lang.Math;

class Epsilon implements Model{

	double epsilon;

	public Epsilon(double epsilon){
		this.epsilon = epsilon;
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
		boolean atRoot = true;

		List<Document> top5 = null;
		List<Feedback> top5_feedbacks = null;
		Action action = null;
		String cur = topic.topic_id, next;

		int negIter = 0;
		
		List<Action> actSeq = new ArrayList<>();

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
				if(action==Action.Up || (atRoot && action==Action.Stay)) atRoot = true;
				else atRoot = false;

				Random r = new Random();
				double x = r.nextDouble();
				if(x<epsilon){
					action = random(atRoot);
				}else{
					action = bestReward(dd, topic, visited, dir, cur, subtopicDocs, atRoot);
				}
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
			cur = dd.getCurTopic(topic, subtopics, top5_feedbacks);
			actSeq.add(action);
				
			if(++iter>=max_iter) break;

		}

		//record the action sequences
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File("../action/Epsilon_iter_"+max_iter+".actions"), true));
		for(Action a:actSeq){
			bw.write(a.toString()+",");
		}

		bw.write("\n");
		bw.close();

		return docs;
	}

	public Action random(boolean atRoot){
		Action[] actionValues = Action.values();
		Action[] actions = new Action[3];
		actions[0] = actionValues[1]; actions[1] = actionValues[2]; actions[2] = actionValues[3];
		if(!atRoot) actions[0] = actionValues[0];
		int choice = new Random().nextInt(actions.length);
		return actions[choice];
	}

	private Action bestReward(Task dd, Topic topic, 
		Set<String> visited, String dir, String cur_topic_id,
		List<List<Document>> subtopicDocs, boolean atRoot) throws IOException{
		Action bestAction = random(atRoot);
		int best_rel = 0;

		List<Document> top5 = null;
		List<Feedback> top5_feedbacks = null;
		String cur = topic.topic_id, next;
		int pre_rel = 0;

		for(Action action:Action.values()){
			if(atRoot && action==Action.Up) continue;
			if(!atRoot && action==Action.Down) continue;
			switch(action){
				case Up:
					top5 = dd.exploit(topic.topic_id, visited, dir);
					top5_feedbacks = dd.getFeedbacks(top5, topic.topic_id);
					pre_rel=dd.getRelScore(top5_feedbacks);
					break;
				case Stay:	
				case Down:
					top5 = dd.exploit(cur_topic_id, visited, dir);
					top5_feedbacks = dd.getFeedbacks(top5, topic.topic_id);
					pre_rel=dd.getRelScore(top5_feedbacks);
					break;					
				case Around:
					top5 = dd.explore(topic, visited, dir, subtopicDocs);
					top5_feedbacks = dd.getFeedbacks(top5, topic.topic_id);
					pre_rel=dd.getRelScore(top5_feedbacks);	
					break;
			}
			if(pre_rel>best_rel){
				best_rel = pre_rel;
				bestAction = action;
			}
		}
		return bestAction;
	}


	public static void main(String[] args){
		Epsilon model  = new Epsilon(0.1);
		for(int i=0;i<100;i++){
			Action action = model.random(true);
			System.out.println(action);
		}
	}
}

