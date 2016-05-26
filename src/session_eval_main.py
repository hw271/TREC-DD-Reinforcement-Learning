#!/usr/bin/python


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

"""Command-line script to evaluate session track runs.

Command-line script that given a qrel file, a mapping file between topics
and sessions, and a run, it produces evaluation measures.

Usage:
  $ python session_eval_main.py \
  --qrel_file=qrels \
  --mapping_file=topics_sessions \
  --run_file=runs/ \
  --dup_file=dups \
  --per_session=0 \
  --cutoff=10 \
  --write_to_file=0

You can also get help on all the command-line flags the program understands
by running:

  $ python session_eval_main.py --help
"""
__author__ = 'ekanoulas@gmail.com (Evangelos Kanoulas)'

import gflags
import logging
import os
import sys
import glob
import re

from collections import defaultdict

import session_eval as session


FLAGS = gflags.FLAGS

# The gflags module makes defining command-line options easy for
# applications. Run this program with the '--help' argument to see
# all the flags that it understands.
gflags.DEFINE_string('qrel_file',
                     None,
                     'the file containing the qrels.')

gflags.DEFINE_string('run_file',
                     None,
                     'the file or folder containing the rank lists'
                     'of a run.')

gflags.DEFINE_string('mapping_file',
                     None,
                     'the file containing the mapping between sessions'
                     ' and topics')

gflags.DEFINE_boolean('per_session',
                      1,
                      'to print results per session')

gflags.DEFINE_integer('cutoff',
                      10,
                      'the cutoff to compute different measures')

gflags.DEFINE_boolean('write_to_file',
                      0,
                      'to print the results in files')

gflags.DEFINE_string('dup_file',
                     None,
                     'the file containing all doc ids per session/query in'
                     'session that we would like to consider as duplicates'
                     'and downgrade their relevance to 0; the format of the'
                     'file should be: session query_in_session doc_id')

gflags.MarkFlagAsRequired('qrel_file')
gflags.MarkFlagAsRequired('mapping_file')
gflags.RegisterValidator('run_file',
                         lambda value: (os.path.isdir(value) or 
                                        os.path.isfile(value)),
                         message=('--run_file must be either a file or'
                                  'a folder'))

def main(argv):
  try:
    argv = FLAGS(argv)  # parse flags
  except gflags.FlagsError, e:
    print '%s\nUsage: %s ARGS\n%s' % (e, sys.argv[0], FLAGS)
    sys.exit(1)

  mapping = {'-2':'0', '0':'0', '1':'1', '4':'2', '2':'3', '3':'4'}
  logging.info('The mapping between judgments and relevance grades is %s' % (
      str(mapping)))
  grades = list(set(map(int, mapping.values())))

  logging.info('Reading the qrels')
  qrelreader = session.QrelReader(FLAGS.qrel_file)
  qrels = qrelreader.ReadQrels()
  qrels = qrelreader.MapJudgmentsToGrades(qrels, mapping)

  logging.info('Reading runs and computing measures')
  all_values = defaultdict(dict)
  if os.path.isdir(FLAGS.run_file): # <-- if a directory of runs
    measures = defaultdict(dict)
    run_names = glob.glob(FLAGS.run_file + '*RL1')
    for run_name in run_names:
      base = re.sub(r'(.*)\.RL1',r'\1',run_name)

      for RL in ['RL1', 'RL2', 'RL3', 'RL4']:
        run_file = base + '.' + RL
        runreader = session.RankingReader(FLAGS.mapping_file, run_file, FLAGS.dup_file)
        run = runreader.ReadRanking()
        run = runreader.MapDocumentsToRelevance(run, qrels)

        sessioneval = session.SessionEvaluator(FLAGS.mapping_file, qrels,
                                               run, grades)
        measures[RL] = sessioneval.ComputeMeasures(FLAGS.cutoff)
      if FLAGS.write_to_file:
        output_file = base + '_results.txt'
        sessioneval.PrettyPrintRLs(measures, FLAGS.per_session, output_file)
      else:
        sessioneval.PrettyPrintRLs(measures, FLAGS.per_session)
  else: # <-- if a single file (run)
    runreader = session.RankingReader(FLAGS.mapping_file, FLAGS.run_file, FLAGS.dup_file)
    run = runreader.ReadRanking()
    run = runreader.MapDocumentsToRelevance(run, qrels)

    sessioneval = session.SessionEvaluator(FLAGS.mapping_file, qrels, run,
                                           grades)
    measures = sessioneval.ComputeMeasures(FLAGS.cutoff)
    sessioneval.PrettyPrint(measures, FLAGS.per_session)


if __name__ == '__main__':
  main(sys.argv)
