import os
import re

def clarify_model_name(model_name):
	if(model_name == "Boltzmann"):
		return "Boltzmann Exp ($\\tau$=10)"
	elif(model_name == "Epsilon"):
		return "$\\epsilon$-greedy ($\\epsilon$=0.1)"
	elif(model_name == "UCB"):
		return "UCB-1"
	elif(model_name == "POMDP1"):
		return "POMDP (step=1)"
	else :
		return "POMDP (step=2)"

def write_table(filename, tex):
	f = open(tex, "a")

	table_name = filename[:-4]
	
	table = "\\begin{table*}[t]\n"
	table += "\\centering\n"
	table += "\\caption{Change in \\textbf{"+table_name+"} Scores as A Function of Iterations for Each Model in  TREC DD}\n"
	table += "\\label{table:"+table_name+"}\n"
	table += "\\begin{tabular}{| l | l | l | l | l | l | l | l |}\n"
	table += "\\hline\n"
	
	data = open(filename, "rb")
	i = 0
	for line in data:
		
		if i==0:
			elems = line.split(",")+["change"]
			#print "\t&\t".join(elems)+"\\\\\\hline\n"
			table += "\t&\t".join(elems)+"\\\\\\hline\n"	
		else:
			elems = line.split(",")
			elems[0] = clarify_model_name(elems[0])
			change = str(float(elems[6])-float(elems[1]))
			elems = elems + [change]
			#print "\t&\t".join(elems)+"\\\\\\hline\n"
			table += "\t&\t".join(elems)+"\\\\\n"
		i+=1
	table += "\\hline\n"
	table += "\\end{tabular}\n"
	table += "\\end{table*}\n\n\n"
	f.write(table)
	f.close()
	
def main():
	dir_result = "."
	files = os.listdir(dir_result)
	tex = "tex_table.txt"
	f = open(tex, "wb")
	f.close()
	for filename in files:
		if filename.endswith(".csv"):
			write_table(filename, tex);

main()