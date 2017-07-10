= cTAKES Negation Detector for the Merlot Corpus of French Medical Texts =

This project is intended to be independent of the main cTAKES distribution. When
it has successfully trained a model, that model could be included in cTAKES if
licenses allow. In the meantime, the intention is that the project can be used
to build and evaluate negation models with cTAKES without necessarily downloading
and installing cTAKES manually.

Instructions for getting started:

1) Preprocess the brat annotation format into UIMA xmi files:
```./preprocess.sh &lt;Brat location&gt; &lt;Directory for gold XMi files&gt;```

2) Run the cTAKES cross-validation script to get results for 5-fold cross-validation on the training set:
```./cv.sh &lt;Directory with gold XMI files&gt; &lt;Directory to store output xmi for evaluation&gt;```

(At this point your terminal should show an evaluation. We continue on to get output in Brat format so that it may more easily be compared to other system outputs.)

3) Convert the cTAKES output XMI into Brat format for running another evaluation:
```./postprocess.sh &lt;Directory with xmi evaluation outputs&gt; &lt;Directory to store Brat output&gt;```


