import java.io.*;
import java.util.*;

class Evaluator{

	Map<String, Set<String>> ground_truth = new HashMap<>();

	public Evaluator(){
		initialize();
	}

	private void initialize(){
		try{
			File f = new File("../eval/qrels.txt");
			BufferedReader br = new BufferedReader(new FileReader(f));
			String line;
			while((line=br.readLine())!=null){
				String[] elems = line.split("\\s");
				String topic_id = elems[0];
				String doc_id = elems[2];
				if(ground_truth.containsKey(topic_id)){
					ground_truth.get(topic_id).add(doc_id);
				}else{
					Set<String> docs = new HashSet<>();
					docs.add(doc_id);
					ground_truth.put(topic_id, docs);
				}
			}

		}catch(IOException e){
			System.out.println(e);
			return ;
		}
	}

	/**
		nDCG, nDCG@10, AP
	*/
	public void evaluate(String run_file) throws IOException{
		//./session_eval_main.py --qrel_file=2012.qrels.txt --mapping_file=data-sessiontopicmap.txt --run_file=L2CRLoopV2_10.RL4
		//./session_eval_main.py --qrel_file=qrels.txt --mapping_file=mapping.txt 
		BufferedWriter sh = new BufferedWriter(new FileWriter(new File("./eval.sh")));
		sh.write("cd ../eval\n");
		sh.write("./session_eval_main.py --qrel_file=qrels.txt --mapping_file=mapping.txt --run_file="+run_file);
		sh.close();

		Process p = Runtime.getRuntime().exec("./eval.sh");

		File f = new File("../result/"+run_file+".result");
		f.createNewFile();
		BufferedWriter bw = new BufferedWriter(new FileWriter(f));
		BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
		String s = "";
		
		while ((s = stdInput.readLine()) != null){
			bw.write(s+"\n");
		}
		bw.close();
	}


	public void cubeTest(String run_file) throws IOException{
		BufferedReader br = new BufferedReader(new FileReader(new File("../eval/"+run_file)));
		String line = "";
		Map<String, List<String>> map = new HashMap<>();

		while((line = br.readLine())!=null){
			//line format: 49	Q0	ebola-cff05d7ae7b0340b13151dca20ac18c4d5339276a8da7ad6fc7f438cdd34f749	1	-3.78918	indri
			String[] elem = line.split("\t");
			String topic_id = "DD15-"+elem[0];
			String doc_id = elem[2];
			if(map.containsKey(topic_id)){
				map.get(topic_id).add(doc_id);
			}else{
				List<String> docs = new ArrayList<>();
				docs.add(doc_id);
				map.put(topic_id, docs);
			}
		}
		br.close();

		BufferedWriter bw = new BufferedWriter(new FileWriter(new File("../eval/"+run_file+".cb")));
		//topic 1~118
		//cb format: DD15-1  0       1335214753-ef51a01b167c0093da0e7418556b387a     624.000000      0       NULL

		for(int i=1;i<119;i++){
			String topic_id = "DD15-"+String.valueOf(i);
			List<String> docs = map.get(topic_id);
			for(int j=0;j<docs.size();j++){
				String doc_id = docs.get(j);
				bw.write(topic_id+"\t"+String.valueOf(j/5)+"\t"+doc_id+"\t"+String.valueOf((double)1/(j+0.1))+"\t0\t"+"\tNULL\n");
			}
		}
		bw.close();

		int[] iters = {25};
		for(int i=0;i<iters.length;i++){
			int iter = iters[i];
			String[] cmd = {"perl", "cubeTest_dd.pl", "cubetest-qrels-v5", "../eval/"+run_file+".cb", String.valueOf(iter)};
			Process p = Runtime.getRuntime().exec(cmd);
			File f = new File("../result/"+run_file+".cb");
			f.createNewFile();
			BufferedWriter cbw = new BufferedWriter(new FileWriter(f));
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String s = "";
			
			while ((s = stdInput.readLine()) != null){
				cbw.write(s+"\n");
			}
			cbw.close();
		}
	}
	
	/*
	public void evalCubeTest(File f1) throws IOException{
		String run_file = f1.getName();
		System.out.println(run_file);
		String[] e1 = run_file.split("\\.cb");
		String[] e2 = e1[0].split("iter");
		int iterNum = Integer.valueOf(e2[1]);

		int[] iters = {iterNum};
                for(int i=0;i<iters.length;i++){
                        int iter = iters[i];
                        String[] cmd = {"perl", "cubeTest_dd.pl", "cubetest-qrels-v5", "../cb/"+run_file, String.valueOf(iter)};
                        Process p = Runtime.getRuntime().exec(cmd);
                        File f = new File("../result/"+run_file+"_"+String.valueOf(iter)+".cb");
                        f.createNewFile();
                        BufferedWriter cbw = new BufferedWriter(new FileWriter(f));
                        BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
			BufferedReader bre = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                        String s = "";

                        while ((s = stdInput.readLine()) != null){
                                cbw.write(s+"\n");
                        }
			stdInput.close();
                        cbw.close();

			while ((s = bre.readLine()) != null) {
        			System.out.println(s);
      			}
      			bre.close();
                }
	}

	/**
	MAP, Precision and Recall
	*/
	/*
	public void evaluate2(String run_file, int topN) throws IOException{
		double[] results =  evaluate2(new File("../eval/"+run_file), topN);
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File("../result/"+run_file+".eval")));
		bw.write("MAP\tprecision\trecall\n");
		bw.write(String.valueOf(results[0])+" "+String.valueOf(results[1])+" "+String.valueOf(results[2])+"\n");
		bw.close();
	}

	public double[] evaluate2(File run_file, int topN) throws IOException{
		Map<String, List<String>> map = new HashMap<>();

		File f = run_file;
		BufferedReader br = new BufferedReader(new FileReader(f));
		String line;
		while((line=br.readLine())!=null){
			String[] elems = line.split("\\s");
			String topic_id = elems[0];
			String doc_id = elems[2];
			if(map.containsKey(topic_id)){
				map.get(topic_id).add(doc_id);
			}else{
				List<String> docs = new ArrayList<>();
				docs.add(doc_id);
				map.put(topic_id, docs);
			}
		}

		double overallMAP = 0;
		double overallPrecision = 0;
		double overallRecall = 0;

		for(String topic_id:map.keySet()){
			List<String> docs = map.get(topic_id);
			Set<String> relDocs = ground_truth.get(topic_id);

			double MAP = 0;
			double numOfRel = 0;
			//for(int i=0;i<topN;i++){
			//	if(i>=docs.size()) break;
			for(int i=0;i<docs.size();i++){
				String doc_id = docs.get(i);
				if(relDocs.contains(doc_id)){
				//	System.out.print("1 ");
					numOfRel+=1;
					MAP+=numOfRel/(i+1);
				}
				//else{
				//	System.out.print("0 ");
				//}
			}
			MAP = numOfRel==0?0:MAP/numOfRel;
			overallMAP += MAP;
			//System.out.println(MAP);

			double numOfRR = 0;
			for(int i=0;i<docs.size();i++){
				String doc_id = docs.get(i);
				if(relDocs.contains(doc_id)) numOfRR+=1;
			}
			double precision = numOfRR/docs.size();
			double recall = numOfRR/relDocs.size();

			overallPrecision += precision;
			overallRecall += recall;
		}
		overallMAP /= map.size();
		overallPrecision /= map.size();
		overallRecall /= map.size();

		return new double[]{overallMAP, overallPrecision, overallRecall};
	}
/*
	public static void main(String[] args) throws IOException{
		String[] models = {
			"Boltzmann_0.0000000001", 
			"Epsilon_1", 
			"UCB", 
			"POMDP_1",
			"Boltzmann_9999999999", 
			"Boltzmann_10","Base2", 
			"Epsilon_0.0000000001", 
			"Epsilon_0.25", 
			"POMDP_2"
		};

		int topN = 10;	
		Evaluator eval = new Evaluator();	
		File f = new File("../result/evalMPR.txt");
		f.createNewFile();
		BufferedWriter bw = new BufferedWriter(new FileWriter(f));
		bw.write("model\tMAP\tprecision\trecall\n");

		int[] iteration = {2, 4, 8, 15, 25, 40};
		for(int max_iter: iteration){
			for(int i=0;i<models.length;i++){
				String run_file = "run_file_"+models[i]+"_iter"+String.valueOf(max_iter);
				double[] values = eval.evaluate2("../eval/"+run_file, topN);
				double MAP = values[0];
				double precision = values[1];
				double recall = values[2];

				bw.write(run_file+"\t"+String.valueOf(MAP)+"\t"+String.valueOf(precision)+"\t"+String.valueOf(recall)+"\n");
			}
		}
		bw.close();
	}


	public static void main(String[] args) throws IOException{
		Evaluator eval = new Evaluator();

		
		
		File folder = new File("../cb");
		File[] files = folder.listFiles();
		for(File f:files){
			if(f.isDirectory()) continue;
			//String filename = f.getName();
			eval.evalCubeTest(f);			
		}
		/*
		int topN = 10;	
		Evaluator eval = new Evaluator();	
		File f = new File("../evalMPR.txt");
		f.createNewFile();
		BufferedWriter bw = new BufferedWriter(new FileWriter(f));
		bw.write("model\tMAP\tprecision\trecall\n");

		//File folder = new File("../docs");
		//File[] files = folder.listFiles();
		//File[] files = {new File("../docs/ucb.docs"), new File("../docs/run_file_Boltzmann_10_iter15"), new File("../docs/run_file_Epsilon_0.25_iter15"), new File("../docs/run_file_POMDP_step4")};
		File[] files = {new File("../docs/run_file_POMDP_2_iter25")};
		for(File run_file:files){
		
				//String run_file = "run_file_"+models[i]+"_iter"+String.valueOf(max_iter);
				//if(!run_file.getName().endsWith( "docs" )) continue;
				double[] values = eval.evaluate2(run_file, topN);
				double MAP = values[0];
				double precision = values[1];
				double recall = values[2];

				bw.write(run_file+"\t"+String.valueOf(MAP)+"\t"+String.valueOf(precision)+"\t"+String.valueOf(recall)+"\n");
		}
		bw.close();

		
	}
*/
}
