import csv
import re
from os import listdir



#extract model name from file name
def extract(s):
	elems = s[:-7].split("_");
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


def readnDCG(filename):
	f = open(filename, "rb")
	line = f.readline()
	#second line
	line = f.readline()
	elems = line.split()
	return (elems[5], elems[6])

def main():
	dict_nDCG = {'Boltzmann':{}, "Epsilon":{}, "POMDP1":{}, "POMDP2":{}, "UCB":{}}
	dict_nDCG10 = {'Boltzmann':{}, "Epsilon":{}, "POMDP1":{}, "POMDP2":{}, "UCB":{}}
	for filename in listdir("."):
		if filename.endswith("result"):
			(name, iteration) = extract(filename)
			(nDCG, nDCG10) = readnDCG(filename)
			dict_nDCG[name][iteration] = nDCG
			dict_nDCG[name]['modelname'] = name

			dict_nDCG10[name][iteration] = nDCG10
			dict_nDCG10[name]['modelname'] = name
			#print (name, iteration)

	#write nDCG.csv
	nDCGfile = open("nDCG.csv","wb")
	fieldnames = ['modelname','1','2','4','8','15','25']
	writer = csv.DictWriter(nDCGfile, fieldnames=fieldnames)

	writer.writeheader()
	for model in ['Boltzmann', 'Epsilon', 'POMDP1', 'POMDP2', 'UCB']:
		writer.writerow(dict_nDCG[model])

	nDCGfile.close()


	#write nDCG10.csv
	nDCG10file = open("nDCG10.csv","wb")
	fieldnames = ['modelname','1','2','4','8','15','25']
	writer = csv.DictWriter(nDCG10file, fieldnames=fieldnames)

	writer.writeheader()
	for model in ['Boltzmann', 'Epsilon', 'POMDP1', 'POMDP2', 'UCB']:
		writer.writerow(dict_nDCG10[model])
	nDCG10file.close()

main()

		