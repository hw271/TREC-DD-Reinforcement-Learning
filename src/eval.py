import csv
import os

fe = open("../eval.csv","wb")
we = csv.writer(fe);
we.writerow(['model','nDCG', 'nDCG@10', 'AP', 'PC@10'])

fc = open("../cube.csv","wb")
wc = csv.writer(fc);
wc.writerow(['model', 'CT', 'ACT@10'])
for file in os.listdir("../result"):
	f = open("../result/"+file)
	
	lines = f.readlines()

	if(file.endswith(".cb")):
		#print "cb:"+file
		#../eval/run_file_Boltzmann_0.0000000001_iter2.cb,all,0.0876120260,0.0644432513
		line = lines[-1][0:-1]
		print line
		row = [file]+line.split(",")[2:]
		wc.writerow(row)
	else:
		#print "not cb:"+file
		#all	0.1446	0.1446	0.1537	0.1537	0.0803	0.1228	0.0419	0.1314
		line = lines[1][0:-1]
		print line
		row = [file]+line.split("\t")[5:]
		we.writerow(row)