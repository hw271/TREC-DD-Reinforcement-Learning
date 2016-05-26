import xml.etree.ElementTree as ET
import re

tree = ET.parse('../data/topics/dynamic-domain-2015-truth-data-v5.xml')
root = tree.getroot()
domains = root.findall('domain')
for domain in domains:
	filename = domain.get('name')
	writer = open('../data/topics/'+filename+'.txt', "w")
	writer.write(domain.get('name')+' | '+domain.get('num_of_topics')+'\n')
	for topic in domain.findall('topic'):
		topic_name = re.sub('[^a-zA-Z0-9\s]', ' ', topic.get('name'))
		writer.write(topic_name+' | '+topic.get('id')+' | '+topic.get('num_of_subtopics')+'\n')
		for subtopic in topic.findall('subtopic'):
			subtopic_name = re.sub('[^a-zA-Z0-9\s]', ' ', subtopic.get('name'))
			writer.write(subtopic_name+' | '+subtopic.get('id')+'\n')

'''
This code will generate three txt files: Ebola.txt, Illicit_Goods.txt, Local_Politics.txt
Each file contains information about domain, topic and subtopic
The format is like:
domain_name | num_of_topics
topic_name | topic_id | num_of_subtopics
subtopic_name | subtopic_id
subtopic_name | subtopic_id
subtopic_name | subtopic_id
...
topic_name | topic_id | num_of_subtopics
subtopic_name | subtopic_id
subtopic_name | subtopic_id
subtopic_name | subtopic_id
...
'''
