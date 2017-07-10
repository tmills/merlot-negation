#!/bin/bash

mvn exec:java -Dexec.mainClass="fr.limsi.talmed.negation.BratToCasWriter" \
  -Dexec.args="$*"
  

