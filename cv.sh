#!/bin/bash

ARGS="--train-dir $1 --cross-validation 5 --ignore-subject --ignore-uncertainty --ignore-history --ignore-generic --ignore-conditional --feats NO_SYN --models-dir target/evals --evaluation-output-dir $2"

mvn exec:java -Dexec.mainClass="org.apache.ctakes.assertion.eval.AssertionEvaluation" \
  -Dexec.args="$ARGS"

