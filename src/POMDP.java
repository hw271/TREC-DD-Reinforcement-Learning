import java.util.*;
import java.io.*;
import java.lang.Math;

class POMDP implements Model{
	int horizon;

	public POMDP(int horizon){
		this.horizon = horizon;
	}

	//search using topic_name
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
				if(action==Action.Up || (atRoot && action==Action.Stay)) atRoot = true;
				else atRoot = false;

				action = chooseAction(horizon, dd, topic, visited, dir, cur, subtopicDocs, atRoot);
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
			//update actionMap
			double[] value = actionMap.get(action);
			value[0]+=1;
			value[1]+=pre_rel;

			next = dd.getCurTopic(topic, subtopics, top5_feedbacks);
			actionMap.put(action, value);
			
			if(pre_rel==0) negIter++;
			else negIter = 0;
			
			iter++;
			cur = next;

			actSeq.add(action);

			if(iter>=max_iter || top5.size()==0){
				break;
			}

		}

		//record the action sequences
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File("../action/POMDP_"+horizon+"_iter_"+max_iter+".actions"),true));
		for(Action a:actSeq){
			//System.out.println(a);
			bw.write(a.toString()+",");
		}
		bw.write("\n");
		bw.close();

		return docs;
	}


	

	private Action chooseAction(int horizon, Task dd, Topic topic, 
		Set<String> visted_documents, String dir, String cur_topic_id,
		List<List<Document>> subtopicDocs,
		boolean atRoot) throws IOException{

		Action best_action = null;
		double best_value = 0;
		
		List<List<Action>> action_plan = generateActionPlan(atRoot);
		for(List<Action> action_seq:action_plan){
			Action action0 = action_seq.get(0);
			double value = 0;
			Set<String> visited = new HashSet<String>(visted_documents);

			for(Action action:action_seq){
				int pre_rel = 0;
				List<Document> top5 = null;
				List<Feedback> top5_feedbacks = null;
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
				for(Document doc:top5) visited.add(doc.doc_id);
				value+=pre_rel;
			}

			if(value>best_value){
				best_value = value;
				best_action = action0;
			}
		}

		return best_action==null?random(atRoot):best_action;
	}

	private Action random(boolean atRoot){
		Action[] actionValues = Action.values();
		Action[] actions = new Action[3];
		actions[0] = actionValues[1]; actions[1] = actionValues[2]; actions[2] = actionValues[3];
		if(!atRoot) actions[0] = actionValues[0];
		int choice = new Random().nextInt(actions.length);
		return actions[choice];
	}

	public List<List<Action>> generateActionPlan(boolean atRoot){
		List<List<Action>> action_plan = new ArrayList<>();
		List<Action> action_seq = new ArrayList<>();
		generator(atRoot, horizon, action_seq, action_plan);
		return action_plan;
	}

	private void generator(boolean atRoot, int h, List<Action> action_seq, List<List<Action>> action_plan){
		if(h==0){
			action_plan.add(new ArrayList<Action>(action_seq));
		}else{
			for(Action action:Action.values()){
				if(atRoot && action==Action.Up) continue;
				if(!atRoot && action==Action.Down) continue;
				action_seq.add(action);
				boolean curAtRoot = false;
				if((atRoot && action==Action.Stay) || action==Action.Up) curAtRoot = true;
				generator(curAtRoot, h-1, action_seq, action_plan);
				action_seq.remove(action_seq.size()-1);
			}
		}
	}

	public static void main(String[] args){
		POMDP p = new POMDP(2);
		List<List<Action>> action_plan = p.generateActionPlan(true);
		
		for(List<Action> action_seq:action_plan){
			System.out.println(action_seq);
		}
	}
}
