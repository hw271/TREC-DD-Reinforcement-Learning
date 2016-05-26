class ModelFactory{
	public Model getModel(String model_name){

		/**
			model_name format:
				Base1~Base3: "Base1", "Base2", "Base3"
				UCB: "UCB"
				POMDP: "POMDP_horizon"
				Epsilon: "Epsilon_epsilon"
				Boltzmann: "Boltzmann_temperature"
		*/



		String[] elems = model_name.split("_");
		String model = elems[0];

		switch(model){
			case "UCB":
				return new UCB();
			case "POMDP":
				int horizon = Integer.valueOf(elems[1]);
				return new POMDP(horizon);
			case "Base1":
				return new Base1();
			case "Base2":
				return new Base2();
			case "Epsilon":
				double epsilon = Double.valueOf(elems[1]);
				return new Epsilon(epsilon);
			case "Boltzmann":
				double t = Double.valueOf(elems[1]);
				return new Boltzmann(t);
			default:
				return null;
		}
	}

}
