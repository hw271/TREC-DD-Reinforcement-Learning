import java.util.*;
import java.io.*;
import  java.lang.Math;

class UCB implements Model{
	/**API:
		List<List<Document>> subtopicDocs = dd.retrieveSubtopics(topic, visited, dir);
		List<Document> top5 = dd.exploit(topic.topic_id, visited, dir);
		List<Document> top5 = dd.explore(topic, visited, dir, subtopicDocs);
		List<Feedback> top5_feedbacks = dd.getFeedbacks(top5, topic.topic_id);

	*/


	//public List<Document> advanced_search(Task dd, Topic topic, Set<String> visited, String dir, int max_iter) throws IOException{
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
				if(action == Action.Up || (atRoot && action == Action.Stay)) atRoot = true;
				else atRoot = false;

				action = chooseAction(iter, actionMap, num_of_sub, atRoot);
				switch(action){
					case Up:
						top5 = dd.exploit(topic.topic_id, visited, dir);
						top5_feedbacks = dd.getFeedbacks(top5, topic.topic_id);
						//System.out.println("start:"+top5_feedbacks);
						pre_rel=dd.getRelScore(top5_feedbacks);
						action = Action.Up;
						if(pre_rel!=0){
							for(Document doc:top5) visited.add(doc.doc_id);
							docs.addAll(top5);
						}else{
							top5 = dd.explore(topic, visited, dir, subtopicDocs);
							top5_feedbacks = dd.getFeedbacks(top5, topic.topic_id);
							pre_rel=dd.getRelScore(top5_feedbacks);
							for(Document doc:top5) visited.add(doc.doc_id);
							docs.addAll(top5);
							action = Action.Around;
						}
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
				//update visted and subtopics
			}
			for(Document doc:top5) visited.add(doc.doc_id);
			for(Feedback feedback:top5_feedbacks) subtopics.add(feedback.subtopic_id);				

			//update docs
			docs.addAll(top5);
			//System.out.println("UCB.java: all retrieved documents:"+docs);
			
			//update actionMap
			double[] value = actionMap.get(action);
			value[0]+=1;
			value[1]+=pre_rel;
			actionMap.put(action, value);

			next = dd.getCurTopic(topic, subtopics, top5_feedbacks);

			if(pre_rel==0) negIter++;
			else negIter = 0;
			//System.out.println("\taction="+action+"\t|\treward="+pre_rel+"\t|\tcur_topic="+cur+"\t|\tnext_topic="+next+"\t <-UCB.java");

			iter++;
			cur = next;

			actSeq.add(action);

			if(iter>=max_iter){
				//if(iter>=max_iter || top5.size()==0){
				//System.out.println("\tconclusion for topic "+topic.topic_id+", total iter = "+iter+", total # of subtopics is "+topic.subtopics.size());
				break;
			}
		}

		//record the action sequences
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File("../action/UCB_iter_"+max_iter+".actions"), true));
		bw.write(" ");
		for(Action a:actSeq){
			bw.write(a.toString()+",");
		}

		bw.write("\n");
		bw.close();

		return docs;
	}

	private Action chooseAction(int iter, Map<Action, double[]> actionMap, int num_of_sub, boolean atRoot){
		Action bestAction = null;
		double bestReward = 0;
		for(Action action:Action.values()){
			if(atRoot && action==Action.Up) continue;
			if(!atRoot && action==Action.Down) continue;
			double[] info = actionMap.get(action);
			//info[0] is # of times the action is used, info[1]: total reward
			double num_of_action = info[0];
			double totoal_reward = info[1];
			/*
			double num_of_dest = 0;
			switch(action){
				case Up:
				case Down:
				case Stay:
					num_of_dest = 1;
					break;
				case Around:
					num_of_dest = num_of_sub-1;
			}
			*/
			double reward = totoal_reward/num_of_action
				+Math.sqrt(2.0*Math.log(iter+3.0)/(1.0+num_of_action));
			if(reward>bestReward){
				bestReward = reward;
				bestAction = action;
			}
		}
		if(bestReward == 0.0){
			return random(atRoot);
		}
		return bestAction;
	}

	private Action random(boolean atRoot){
		Action[] actionValues = Action.values();
		Action[] actions = new Action[3];
		actions[0] = actionValues[1]; actions[1] = actionValues[2]; actions[2] = actionValues[3];
		if(!atRoot) actions[0] = actionValues[0];
		int choice = new Random().nextInt(actions.length);
		return actions[choice];
	}

}

