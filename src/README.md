Model Codes:
interface Model: the following classes implements Model
class Base1 - search with topic name
class Base2 - search with subtopics names
class Boltzmann - Boltzmann Explortaion
class Epsilon - Epsilon Greedy
class UCB - UCB
class POMDP - POMDP

class ModelFactory: generate model object

class Evaluator: evalute the results.

class Experiment: The top level. It calls Model and Evaluator to do experiments.

How to run code:
javac *.java
java Experiment
