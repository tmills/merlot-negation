#!/bin/bash

mvn exec:java -Dexec.mainClass="fr.limsi.talmed.negation.CasToBratWriter" \
  -Dexec.args="$*"

