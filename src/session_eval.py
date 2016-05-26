#!/usr/bin/python2.7


################################################################################
# Copyright (c) 2012 Evangelos Kanoulas
#
# Permission is hereby granted, free of charge, to any person obtaining a copy
# of this software and associated documentation files (the "Software"), to deal
# in the Software without restriction, including without limitation the rights
# to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
# copies of the Software, and to permit persons to whom the Software is
# furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in
# all copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
# THE SOFTWARE.
################################################################################


"""Library that defines several classes for session track evaluation.
 """

__author__ = 'ekanoulas@gmail.com (Evangelos Kanoulas)'

import os
import math
import logging
from collections import defaultdict


def cumulative_sum(a_list):
  """Method to compute cumulative sum.
  """
  cum_sum = []
  y = 0.0
  for i in a_list:   # <--- i will contain elements (not indices) from n
    y += i    # <--- so you need to add i, not n[i]
    cum_sum.append(y)
  return cum_sum


def dsum(a_dict):
  """Method to compute the sum of the values in a dictionary.
    
  Args:
    a_dict: key -> value
  """
  y = 0.0
  for value in a_dict.itervalues():
    y += value
  return y


class QrelReader:
  """Class to read the QREL file and map judgments to relevance grades."""
  qrel_file = None

  def __init__(self, qrel_file=None):
    self.qrel_file = qrel_file

  def ReadQrels(self):
    """Method to read qrel files.
  
    The method reads the qrel file and returns a two dimentional dictionary
    with the topic id and the doc id as keys and the relevance judgment as
    value. The qrel_file contains: topic subtopic document judgment

    Returns:
      rel_judments: topic, document -> judgment (integer)
    """
    count = 0
    with open(self.qrel_file ,'r') as f:
      rel_judgments = defaultdict(dict)
      for line in f:
        if not line.strip():
          continue
        else:
          qrel_line = line.strip().split()
          # The following line simply considers the largest relevance grade
          # for the particular document, if there are several judgements for
          # different subtopics.
          if (rel_judgments[qrel_line[0]].has_key(qrel_line[2]) and
            rel_judgments[qrel_line[0]][qrel_line[2]]>int(qrel_line[3])):
            continue  
          rel_judgments[qrel_line[0]][qrel_line[2]] = int(qrel_line[3])
    return rel_judgments

  def MapJudgmentsToGrades(self, rel_judgments, mapping):
    """Method that maps relevance judgments to relevance grades.
    
    The method that maps relevance judgments to relevance grades.
    For instance, spam can be mapped to non-relevant.

    Args:
      rel_judgments: topic, document -> judgment.
      mapping: relevance judgments -> relevance grades as values.

    Returns: 
      rel_judgments: topic, document -> grade (integer).
    """
    for topic, document_rel in rel_judgments.iteritems():
      for document, relevance in document_rel.iteritems():
        if mapping.has_key(str(rel_judgments[topic][document])):
          rel_judgments[topic][document] = (
              int(mapping[str(rel_judgments[topic][document])]))
        else:
          logging.error('The judgment of grade %s is not mapped to any '
                        'relevance grade.',
                        str(rel_judgments[topic][document]))
          exit()
    return rel_judgments    

class RankingReader:
  """Class to read the run file and convert rank lists to relevance lists."""
  run_file = None
  mapping_file = None
  dup_file = None
  session_topic = defaultdict(int)

  def __init__(self, mapping_file=None, run_file=None, dup_file=None):
    self.run_file = run_file
    self.mapping_file = mapping_file
    self.dup_file = dup_file
    self._MapSessionsToTopics()

  def _MapSessionsToTopics(self):
    """Method to map sessions to topics.

    The method maps sessions against which the ranking functions have been
    tested to sessions. The method produces a dictionary to be used when
    mapping documents returned by the different runs to relevance judgements.

    Populates the session_topic dictionary; session_topic: session -> topic
    """
    with open(self.mapping_file ,'r') as f:
      for line in f:
        if not line.strip():
          continue
        else:
          mapping_line = line.strip().split()
          if not mapping_line[1].isdigit():
            continue
          else:
            self.session_topic[mapping_line[0]] = mapping_line[1]

  def ReadRanking(self):
    """Method to read run files.
  
    The method reads the run file and returns a dictionary of lists with the
    topic id as a key and the list of the document ids ranked by the method
    as values.

    Returns:
      ranking_docs: session -> (doc1, doc2, ...)
    """
    with open(self.run_file ,'r') as f:
      ranking_docs = defaultdict(list)
      for line in f:
        if not line.strip():
          continue
        else:
          run_line = line.strip().split()
          ranking_docs[run_line[0]].append(run_line[2])
    return ranking_docs

  def _MapDupDocumentsToNonrelevance(self):
    """Method to downgrade the relevance of any duplicate document to 0.

    The method downgrades the relevance of any duplicate document to 0.
    Duplicate documents can be defined as those returned in the previous
    queries in the session, as those returned up to the last clicked one
    (if no clicks then we can assume that the first one was observed) or
    returned and clicked 

    Returns:
      dup: session, document -> 1
    """
    dup = defaultdict(dict)
    with open(self.dup_file ,'r') as f:
      for line in f:
        if not line.strip():
          continue
        else:
          dup_line = line.strip().split()
          dup[dup_line[0]][dup_line[2]] = 1
    return dup

  def MapDocumentsToRelevance(self, ranking_docs, rel_judgments):
    """Method to map document ids to relevance.

    The method maps rankings of document ids to rankings of relevance.

    Args:
      ranking_docs: session -> (doc1, doc2, ...)
      rel_judments: topic, document -> judgment

    Returns:
      ranking_rel: session -> (rel_doc1, rel_doc2, ...)
    """
    dup = defaultdict(dict)
    if self.dup_file is not None:
      dup = self._MapDupDocumentsToNonrelevance()
    ranking_rel = defaultdict(list)
    for session, documents in ranking_docs.iteritems():
      for document in documents:
        topic = self.session_topic[session]
        if (not dict(rel_judgments[topic]).has_key(document) or
            dup[session].has_key(document)):
          ranking_rel[session].append(0)
        else:
          ranking_rel[session].append(rel_judgments[topic][document])
    return ranking_rel


class MeasureEvaluator:
  """Class to compute a number of evaluation measures."""

  def PrecisionAtK(self, ranking, cutoff):
    """Method to compute precision at cut-off k.

    Args:
      ranking_rel: session -> (rel_doc1, rel_doc2, ...).
      cutoff: the cut-off rank
    """
    return sum(map(float, map(lambda x: x > 0,
                              map(float, ranking[:cutoff]))))/cutoff

  def AveragePrecision(self, ranking, R):
    """Method to compute Average Precision

    Args:
      ranking_rel: session -> (rel_doc1, rel_doc2, ...).
    """
    # Get binary relevance at each rank.
    ranking = map(float, map(lambda x: x > 0, map(float, ranking)))
    cum_relevance = cumulative_sum(ranking)
    sum_precisions = 0.0
    rank = 1
    for rel in ranking:
      sum_precisions += rel * (cum_relevance[rank-1]/rank)
      rank += 1
    return sum_precisions/R    
        
  def ERR(self, ranking, cutoff=None, max_grade=4):
    """Method to compute ERR.

    Args:
      ranking_rel: session -> (rel_doc1, rel_doc2, ...).
      cutoff: the cut-off rank.
    """
    if cutoff is None:
      cutoff = len(ranking)

    ranking = map(float, ranking[:cutoff])
    err = 0
    rank = 1
    prob_step_down = 1.0
    for rel in ranking:
      utility = (pow(2, rel) - 1) / pow(2, max_grade)
      err += prob_step_down * utility / rank
      prob_step_down *= (1 - utility) 
      rank += 1
    return err

    
  def DCG(self, ranking, cutoff=None):
    """Method to compute DCG and DCG@cutoff.

    Args:
      ranking_rel: session -> (rel_doc1, rel_doc2, ...).
      cutoff: the cut-off rank.
    """
    if cutoff is None:
      cutoff = len(ranking)

    ranking = map(float, ranking[:cutoff])
    disc_cum_gain = 0.0
    rank = 1
    for rel in ranking:
      disc_cum_gain += (pow(2, rel) - 1) / math.log(rank + 1, 2)
      rank += 1
    return disc_cum_gain


class SessionEvaluator(MeasureEvaluator):  
  """Class to compute results for the session track."""
  mapping_file = None  
  rel_judgments = defaultdict(dict)
  ranking_rel = defaultdict(list)
  ideal_ranking = defaultdict(list)
  relevance_counts = defaultdict(dict)
  session_topic = defaultdict(int)
  measures = defaultdict(dict)

  def __init__(self, mapping_file, rel_judgments, ranking_rel, grades):
    self.rel_judgments = rel_judgments
    self.ranking_rel = ranking_rel
    self.mapping_file = mapping_file
    self._MapSessionsToTopics()
    self._GenerateIdealRanking()
    self._GenerateRelevanceCounts(grades)

  def _MapSessionsToTopics(self):
    """Method to map sessions to topics.

    The method maps sessions against which the ranking functions have been
    tested to sessions. The method produces a dictionary to be used when
    mapping documents returned by the different runs to relevance judgements.

    It assumes that the mapping between sessions and topics is contained in
    a mapping file with two columns, session and topic, separated by tab,
    (or space).

    Populates the session_topic dictionary; session_topic: session -> topic
    """
    with open(self.mapping_file ,'r') as f:
      for line in f:
        if not line.strip():
          continue
        else:
          mapping_line = line.strip().split()
          if not mapping_line[1].isdigit():
            continue
          else:
            self.session_topic[mapping_line[0]] = mapping_line[1]

  def _GenerateIdealRanking(self):
    """Method that generates the ideal ranking for each topic.

    The method generates the ideal ranking for each topic by considering the
    relevance judgments in the qrel (rel_judgments) and sorting them in a
    descending order.
    """
    ideal_ranking_topic = defaultdict(list)
    for topic, document_rel in self.rel_judgments.iteritems():
      for document, relevance in document_rel.iteritems():
        ideal_ranking_topic[topic].append(relevance)
      ideal_ranking_topic[topic].sort(reverse=True)
    for session, topic in self.session_topic.iteritems():
      self.ideal_ranking[session] = ideal_ranking_topic[topic]
        
  def _GenerateRelevanceCounts(self, grades):
    """Method that counts the numbe of documents at each relevance grade.

    The method counts the number of documents that belong to each relevance
    rade for each topic by considering the relevance judgments in the qrel
    (rel_judgments).

    Args:
      grades: [0, 1, ...], the relevance grades in the qrel 
    """
    relevance_counts_topic = defaultdict(dict)
    for topic, document_rel in self.rel_judgments.iteritems():
      # The loop just initializes the counts for all grades.  
      for grade in grades:
        relevance_counts_topic[topic][str(grade)] = 0
      relevance_counts_topic[topic]['rel'] = 0
      relevance_counts_topic[topic]['nonrel'] = 0
      for document, relevance in document_rel.iteritems():
        relevance_counts_topic[topic][str(relevance)] += 1
        if int(relevance) > 0:
          relevance_counts_topic[topic]['rel'] += 1
        else:
          relevance_counts_topic[topic]['nonrel'] += 1
    for session, topic in self.session_topic.iteritems():
      self.relevance_counts[session] = relevance_counts_topic[topic]
  
  def ComputeMeasures(self, cutoff=10):
    """Method to compute all measures for a run.
    """
    num_sessions_rel = 0 # <-- number of sessions with non-zero rel docs.

    precision_at_k = {}
    average_precision = {}
    ndcg = {}
    ndcg_at_k = {}
    err = {}
    err_at_k = {}
    nerr = {}
    nerr_at_k = {}
    for session, rel_list in self.ranking_rel.iteritems():
      if self.relevance_counts[session]['rel'] > 0:
        num_sessions_rel += 1  
        precision_at_k[session] = self.PrecisionAtK(rel_list, cutoff)
        average_precision[session] = (
            self.AveragePrecision(rel_list, 
                                     self.relevance_counts[session]['rel']))
        ndcg[session] = (self.DCG(rel_list)/
                         self.DCG(self.ideal_ranking[session]))
        ndcg_at_k[session] = (self.DCG(rel_list, cutoff)/
                              self.DCG(self.ideal_ranking[session],
                                               cutoff))
        err[session] = self.ERR(rel_list)
        err_at_k[session] = self.ERR(rel_list, cutoff)
        nerr[session] = (self.ERR(rel_list) / 
                         self.ERR(self.ideal_ranking[session]))
        nerr_at_k[session] = (self.ERR(rel_list, cutoff) /
                              self.ERR(self.ideal_ranking[session],
                                               cutoff))
      else:
        precision_at_k[session] = 0
        average_precision[session] = 0
        ndcg[session] = 0
        ndcg_at_k[session] = 0
        err[session] = 0
        err_at_k[session] = 0
        nerr[session] = 0
        nerr_at_k[session] = 0

    measures = {'err': err, 'err_at_k': err_at_k, 'nerr': nerr,
                'nerr_at_k': nerr_at_k, 'ndcg': ndcg, 'ndcg_at_k': ndcg_at_k,
                'average_precision': average_precision,
                'precision_at_k': precision_at_k}
    return measures

  def PrettyPrint(self, measures, per_session=True):
    """Method to print the measures.
    
    Args:
      measures: measure -> list of values, a dictionary with measure names
      as keys and list of values for each measure for all sessions.
      per_session: boolean, true if we want to print the measure values for
      each session separately
    """  
    num_sessions = len(self.ideal_ranking)
    print 'session\tERR\tERR@10\tnERR\tnERR@10\tnDCG\tnDCG@10\tAP\tPC@10'    
    print 'all\t%.4f\t%.4f\t%.4f\t%.4f\t%.4f\t%.4f\t%.4f\t%.4f' % (
        dsum(measures['err'])/num_sessions,
        dsum(measures['err_at_k'])/num_sessions,
        dsum(measures['nerr'])/num_sessions,
        dsum(measures['nerr_at_k'])/num_sessions,
        dsum(measures['ndcg'])/num_sessions,
        dsum(measures['ndcg_at_k'])/num_sessions,
        dsum(measures['average_precision'])/num_sessions,
        dsum(measures['precision_at_k'])/num_sessions,
        )

    if per_session:
      for session in sorted(measures['err'], key=int):
        print '%s\t%.4f\t%.4f\t%.4f\t%.4f\t%.4f\t%.4f\t%.4f\t%.4f' % (
            session,
            measures['err'][session],
            measures['err_at_k'][session],
            measures['nerr'][session],
            measures['nerr_at_k'][session],
            measures['ndcg'][session],
            measures['ndcg_at_k'][session],
            measures['average_precision'][session],
            measures['precision_at_k'][session],
            )

  def PrettyPrintRLs(self, measures, per_session=True, to_file=None):
    """Method to print the measures in all RLs per run.
    
    Args:
      measures: RL -> measure -> list of values, a dictionary with measure
         names as keys and list of values for each measure for all sessions.
      per_session: boolean, true if we want to print the measure values for
         each session separately.
      to_file: filename to write to the results of the evaluation.   
    """
    if to_file is not None:
      f = open(to_file, 'a')

    num_sessions = len(self.ideal_ranking)
    header_to_print = 'session'
    string_to_print = 'all'
    for measure in sorted(measures['RL1']):
      for RL in sorted(measures):
        header_to_print += '\t%s.%s' % (measure, RL)
        string_to_print += '\t%.4f' % (dsum(measures[RL][measure]) /
                                       num_sessions)

    if to_file is None:
      print header_to_print
      print string_to_print
    else:
      header_to_print += '\n'
      string_to_print += '\n'
      f.write(header_to_print)
      f.write(string_to_print)
  
    if per_session:
      for session in sorted(measures['RL1']['err'], key=int):
        string_to_print = '%s' % session
        for measure in sorted(measures['RL1']):
          for RL in sorted(measures):
            string_to_print += '\t%.4f' % measures[RL][measure][session]
        if to_file is None:
          print string_to_print
        else:
          string_to_print += '\n'          
          f.write(string_to_print)
