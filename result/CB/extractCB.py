import os
import csv


def extractCT(filename):
	f = open(filename, "rb")
	
	for line in f:
		pass
	last = line
	# last line
	elems = last.split(",")
	CT = round(float(elems[2]),3)
	ACT = round(float(elems[3]),3)
	f.close()
	return (CT, ACT)

def extractModel(filename):
	elems = filename[:-3].split("_");
	model = elems[0]

	if model == "UCB":
		iteration = elems[1][4:]
	else:
		iteration = elems[2][4:]


	if model == "POMDP":
		step = elems[1]
		return (model+step, iteration)
	else:
		return (model, iteration)

def getScore(dirpath):
	dict_CT = {'Boltzmann':{}, "Epsilon":{}, "POMDP1":{}, "POMDP2":{}, "UCB":{}}
	dict_ACT = {'Boltzmann':{}, "Epsilon":{}, "POMDP1":{}, "POMDP2":{}, "UCB":{}}

	for filename in os.listdir(dirpath):
		if filename.endswith(".cb"):
			(name, iteration) = extractModel(filename)
			(CT, ACT) = extractCT(filename)
			dict_CT[name][iteration] = CT
			dict_CT[name]['modelname'] = name

			dict_ACT[name][iteration] = ACT
			dict_ACT[name]['modelname'] = name
	return (dict_CT, dict_ACT)

def writeCSV(dict, filename):
	f = open(filename, "wb")
	fieldnames = ['modelname','1','2','4','8','15','25']
	writer = csv.DictWriter(f, fieldnames=fieldnames)

	writer.writeheader()
	for model in ['Boltzmann', 'Epsilon', 'POMDP1', 'POMDP2', 'UCB']:
		writer.writerow(dict[model])
	f.close()


def main():
	(dict_CT, dict_ACT) = getScore(".")
	writeCSV(dict_CT, "CT.csv")
	writeCSV(dict_ACT, "ACT.csv")

main()