import java.io.*;
import java.util.List;

public class CallLemur {
    public static void main(String[] args){
	new CallLemur().retrieve();
    }

    public void retrieve() {
 	System.out.println("Retrieve documents:\t\t\t <- from CallLemur.java retrieve()");
        try {
        	for(Domain domain:Domain.values()){
        		System.out.println("\tdomain:"+domain+"\t\t\t <- from CallLemur.java retrieve()");
			String dir = "../data/retrieval_docs/";
			String index = "";
        		switch(domain){
        			case Ebola:
        				dir += "Ebola/";
					index = "/data1/trecdd/index/ebola_html_01_03_tweets_trecdd15"; 
					break;
        			case Illicit_Goods:
        				dir += "Illicit_Goods/";
					index = "/data1/trecdd/index/illicitgoods_alldata_0413_trecdd15";
					break;
        			case Local_Politics:
        				dir += "Local_Politics/";
					index = "/data1/trecdd/index/new_local_politics_50g_trecdd15";
        				break;
			}
            		List<Topic> topics = new TopicFactory().getTopicList(domain);
			for(Topic topic:topics){
            			String query = topic.topic_name;
				System.out.println(topic.topic_id+": "+topic.topic_name+"\t\t\t<- from CallLemur.java retrieve()");
				String[] cmd = new String[]{"/data1/home/jl1749/indri-5.0/runquery/IndriRunQuery", "-query="+query, "-trecFormat=true", "-index="+index, "-runID=indri"};
				String filepath = dir+topic.topic_id+".doc";
				retrieve(cmd, filepath);

				for(Subtopic subtopic:topic.subtopics){
					System.out.println("\t"+subtopic.subtopic_id+"\t\t\t<- from CallLemur.java retrieve()");
					query = subtopic.subtopic_name;
					System.out.println(query);
					cmd = new String[]{"/data1/home/jl1749/indri-5.0/runquery/IndriRunQuery", "-query="+query, "-trecFormat=true", "-index="+index, "-runID=indri"};
					filepath = dir+subtopic.subtopic_id+".doc";
					retrieve(cmd, filepath);
				}
            		}   	
       		}
		System.exit(0);
        }
        catch (IOException e) {
            System.out.println("exception happened - here's what I know: ");
            e.printStackTrace();
            System.exit(-1);
        }
    }

    private void retrieve(String[] cmd, String filepath) throws IOException{
	String s = "";
        Process p = Runtime.getRuntime().exec(cmd);
        BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
        File file = new File(filepath);
        file.createNewFile();
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        int i = 0;
        while ((s = stdInput.readLine()) != null && i<500) {
                writer.write(s+"\n");
                i++;
        }
	if(i!=500) System.out.println("--------> retrieve "+i+" documents.\t\t\t<- from CallLemur.java retrieve(cmd, filepath)");
        writer.close();
    }
}
