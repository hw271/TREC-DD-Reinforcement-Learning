import java.util.*;
import java.io.*;
import java.lang.Math;

class DD implements Task{
	
	final String jig_cmd = "python jig/jig.py -c jig/config.yaml step";
	
	public List<Document> exploit(Topic topic, Set<String> visited, String dir) throws IOException{
		//get the first 5 document from retrieved document list and sent to jig
		List<Document> top5 = exploit(topic.topic_id, visited, dir);
		return top5;
	}

	public List<Document> exploit(String topic_id, Set<String> visited, String dir) throws IOException{
		//System.out.println("\t"+topic_id+"\t<-from DD.java exploit");
		List<Document> docs = getDocs(topic_id, dir);
		List<Document> top5 = new ArrayList<>();
		int i=0;
		while(top5.size()<5 && i<docs.size()){
			Document doc = docs.get(i++);
			if(!visited.contains(doc.doc_id))
				top5.add(doc);
		}
		return top5;
	}
	
	public List<Document> explore(Topic topic, Set<String> visited, String dir, List<List<Document>> subtopicDocs) throws IOException{
		int n = subtopicDocs.size();
		int rand = (int)(Math.random()*n);
		List<Document> docs = subtopicDocs.get(rand);
		List<Document> top5 = new ArrayList<>();
		int i=0;
                while(top5.size()<5 && i<docs.size()){
                        Document doc = docs.get(i++);
                        if(!visited.contains(doc.doc_id))
                                top5.add(doc);
                }
                return top5;
	}
	

	/*
	public List<Document> explore(Topic topic, Set<String> visited, String dir, List<List<Document>> subtopicDocs) throws IOException{
		return exploreHelper(topic, visited, dir, subtopicDocs);
	}

	private List<Document> exploreHelper(Topic topic, Set<String> visited, String dir, List<List<Document>> subtopicDocs) throws IOException{
		PriorityQueue<Document> heap = new PriorityQueue<>();
		List<Document> top5 = new ArrayList<>();
		//initialize: from each subtopic retrieved document, put the first one into heap
		for(List<Document> oneSubDocs: subtopicDocs){
			if(oneSubDocs.size()>0){
				Document doc = oneSubDocs.get(0);
				while(visited.contains(doc.doc_id)){
					oneSubDocs.remove(0);
					if(oneSubDocs.size()>0){
						doc = oneSubDocs.get(0);
					}else{
						doc = null;
						break;
					}
				}
				if(doc!=null) heap.add(doc);
			} 
		}

		//get the top 5 documents
		while(top5.size()<5 && heap.size()>0){
			Document largest = heap.poll();
			if(!visited.contains(largest.doc_id));
				top5.add(largest);
			for(List<Document> list:subtopicDocs){
				if(list.size()>0 && largest == list.get(0)){
					list.remove(0);
					if(list.size()>0) heap.add(list.get(0));
					break;
				}
			}
		}
		return top5;
	}
	*/
	public List<Document> getDocs(String id, String dir) throws IOException{
		List<Document> docs = new ArrayList<>();

		String file = dir+id+".doc";
		BufferedReader br = new BufferedReader(new FileReader(new File(file)));
		String line = "";
		//0 Q0 ebola-59b43ae48ab3a58ae892752567fef054731a76e8b35b96cf43dd8bb908c8948e 1 -1.62791 indri
		while((line = br.readLine())!=null){
			String[] elem = line.split("\\s");
			Document doc = new Document(elem[2], elem[4]);
			docs.add(doc);
		}
		return docs;
	}

	public List<List<Document>> retrieveSubtopics(Topic topic, Set<String> visited, String dir) throws IOException{
		List<List<Document>> result = new ArrayList<>();
		for(Subtopic subtopic:topic.subtopics){
			List<Document> sub_docs = exploit(subtopic.subtopic_id, visited, dir);
			//if(sub_docs.size()>0) heap.add(sub_docs.get(0));
			result.add(sub_docs);
		}
		return result;
	}

	public List<Feedback> getFeedbacks(List<Document> top5, String topic_id) throws IOException{
		String cmd = jig_cmd+" "+topic_id;
		for(Document doc:top5) cmd+=" "+doc.doc_id;
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File("feedback.sh")));
		bw.write("cd ../feedback\n"+cmd+"\n"+"cd ../src");
		bw.close();

		Process p = Runtime.getRuntime().exec("./feedback.sh");
		BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
		String s = "";
		FeedbackParser parser = new FeedbackParser();
		List<Feedback> feedbacks = new ArrayList<>();
		//System.out.println("2. get feedback:");
		while ((s = stdInput.readLine()) != null){
			feedbacks.addAll(parser.parse(s));
		}
		return feedbacks;
	}

	public int getRelScore(List<Feedback> feedbacks){
		int relevance = 0;
		for(Feedback feedback:feedbacks){
			relevance+=feedback.rel;
		}
		return relevance;
	}

	public String getCurTopic(Topic topic, Set<String> visited_subtopics, List<Feedback> feedbacks){
		Map<String, Double> map = new HashMap<>();
		for(Feedback feedback:feedbacks){
			//if(visited_subtopics.contains(feedback.subtopic_id)) continue;
			String subtopic = feedback.subtopic_id;
			double rel = feedback.rel;

			if(map.containsKey(subtopic)){
				map.put(subtopic, map.get(subtopic)+rel);
			}else{
				map.put(subtopic, rel);
			}
		}

		String best_subtopic=null;
		double best_rel = 0;

		String second_subtopic = null;
		double second_rel = 0;
		//System.out.print("Feedbacks cover subtopics including:");
		for(String subtopic: map.keySet()){
			//System.out.print(" "+subtopic);
			double rel = map.get(subtopic);
			if(rel>best_rel){
				second_rel = best_rel;
				second_subtopic = best_subtopic;
				best_rel = rel;
				best_subtopic = subtopic;
			}else if(rel>second_rel){
				second_rel = rel;
				second_subtopic = subtopic;
			}
		}
		//System.out.println("\t<-DD.java getCurTopic");
		if(best_subtopic==null){
			return topic.topic_id;
		}else if(!visited_subtopics.contains(best_subtopic) || second_subtopic==null){
			return best_subtopic;
		}else{
			return second_subtopic;
		}
	}
}


