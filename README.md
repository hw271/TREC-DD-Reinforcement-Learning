This project aims to perform Text REtrieval Conferance Dynamic Domain ([TREC DD 2015](http://trec-dd.org/2015.html)) task. 

#**_Task_**

It is an interactive search task. The participating systems (your systems) will start from an initial query (the only query provided), retrieve 5 documents and submit them to a simulater program that the Track organizers provided. The simulator (we call it jig) will provide graded relevance judgments to the 5 submitted documents. With the judgments, the system decides if it would like to retrieve more documents or stop. If the system decides to submit more documents, they would need to submit 5 more documents and the jig will provide relevance judgments to those documents again. The retrieval loop continues until the system decides to stop. 

All the interactions, aka, the multiple cycles of retrieval results will be used to evaluate the system's performance in the entire process. The metrics include Cube Test, Average Cube Test, nDCG, etc. This is not a one-shot retrieval, but a whole process of multiple retrievals. An effective participating system is expected to be able to find the relevant documents as many as possible, using less runs of interactions.

#**_Classes and Interfaces_**

Class **Experiment** contains the main method for the task.

Interface **Model** defines one method: search()

Class **Boltzmann**, **Epsilon**, **UCB** and **POMDP** implements interface **Model**. These four classes correspond to the four models we mentioned above: Boltzmann Exploration, epsilon-greedy, UCB-1 and POMDP.

Class **ModelFactory** is the factory to generate models. 
The method
> public Model getModel(String model_name)

accepts String including:
  * "Epsilon_epsilon": generate Epsilon-greedy model, the parameter epsilon usually ranges between 0 and 0.1.
  * "Boltzmann_tau": generate the Boltzmann Exploration model, where tau indicates the temperature parameter.
  * "UCB": generate the UCB-1 model.
  * "POMDP_horizon": generate the POMDP model. The parameter horizon should be an integer.

Class **Evaluator** evaluate the retrieved results, calculating the Cube Test (CT) score, Average Cube Test (ACT) score and nDCG.  

