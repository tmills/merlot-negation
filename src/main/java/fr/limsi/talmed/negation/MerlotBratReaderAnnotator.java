package fr.limsi.talmed.negation;

import opennlp.tools.formats.brat.*;
import org.apache.ctakes.typesystem.type.constants.CONST;
import org.apache.ctakes.typesystem.type.structured.DocumentID;
import org.apache.ctakes.typesystem.type.textsem.*;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.util.ViewUriUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by miller on 7/3/17.
 */
public class MerlotBratReaderAnnotator extends JCasAnnotator_ImplBase {

    private static Logger LOGGER = Logger.getLogger(MerlotBratReaderAnnotator.class.getSimpleName());

    // modifiers for negation/uncertainty/etc.
    public static final String ASSERT_TYPE = "Assertion";

    // annotations that correspond to ctakes semantic types
    public static final String PROC_TYPE = "MedicalProcedure";
    public static final String DISORDER_TYPE = "Disorder";
    public static final String SS_TYPE = "SignOrSymptom";
    public static final String DRUG_TYPE = "Chemicals_Drugs";
    public static final String CONCEPT_TYPE = "Concept_Idea";
    public static final String BIO_TYPE = "BiologicalProcessOrFunction";

    // relation type for negation:
    public static final String NEG_TYPE = "Negation";
    public static final String UNC_TYPE = "Possible";

    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException {
        super.initialize(context);
    }

    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {
        File txtFile = new File(ViewUriUtil.getURI(jCas));
        File annFile = new File(txtFile.getAbsolutePath().replace(".txt", ".ann"));
        File annDir = txtFile.getParentFile();
        File configFile = new File(annDir, "annotation.conf");
        String docId = txtFile.getName();

        LOGGER.info(String.format("Processing doc id: %s, including annotation offset file %s and raw text file %s",
                docId, annFile.getAbsolutePath(), txtFile.getAbsolutePath()));

        // set document id for downstream components:
        DocumentID docIdAnnotation = new DocumentID(jCas);
        docIdAnnotation.setDocumentID(docId);
        docIdAnnotation.addToIndexes();

        // read in annotation files with opennlp library:
        BratDocument bratDoc = null;
        try {
            bratDoc = BratDocument.parseDocument(
                    AnnotationConfiguration.parse(configFile),
                    docId,
                    new FileInputStream(txtFile),
                    new FileInputStream(annFile));
        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.severe("Skipping document: " + docId + " due to Brat parsing failure.");
            return;
        }

        Map<String,IdentifiedAnnotation> id2annotation = new HashMap<>();
        List<BratAnnotation> delayedRelations = new ArrayList<>();

        // iterate through annotations we are interested in and add them to the CAS
        for (BratAnnotation annotation : bratDoc.getAnnotations()) {
            LOGGER.fine("Processing id " + annotation.getId());
            if (annotation instanceof RelationAnnotation){
                delayedRelations.add(annotation);
            }else if (annotation.getType().equals(ASSERT_TYPE)) {
                // this can be a polarity, uncertainty, etc modifier, but
                // since we don't know yet we can't create a more specific
                // sub-type
                Modifier mod = new Modifier(jCas);
                setSpanAndDefaults(annotation, mod, jCas);
                mod.addToIndexes();
                id2annotation.put(annotation.getId(), mod);
            } else if (annotation.getType().equals(PROC_TYPE)) {
                ProcedureMention proc = new ProcedureMention(jCas);
                setSpanAndDefaults(annotation, proc, jCas);
                proc.addToIndexes();
                id2annotation.put(annotation.getId(), proc);
            } else if (annotation.getType().equals(SS_TYPE)) {
                SignSymptomMention ss = new SignSymptomMention(jCas);
                setSpanAndDefaults(annotation, ss, jCas);
                ss.addToIndexes();
                id2annotation.put(annotation.getId(), ss);
            } else if (annotation.getType().equals(DISORDER_TYPE)) {
                DiseaseDisorderMention dd = new DiseaseDisorderMention(jCas);
                setSpanAndDefaults(annotation, dd, jCas);
                dd.addToIndexes();
                id2annotation.put(annotation.getId(), dd);
            } else if (annotation.getType().equals(DRUG_TYPE)) {
                MedicationEventMention med = new MedicationEventMention(jCas);
                setSpanAndDefaults(annotation, med, jCas);
                med.addToIndexes();
                id2annotation.put(annotation.getId(), med);
            } else if (annotation.getType().equals(CONCEPT_TYPE)) {
                EventMention concept = new EventMention(jCas);
                setSpanAndDefaults(annotation, concept, jCas);
                concept.setSubject(CONCEPT_TYPE);
                concept.addToIndexes();
                id2annotation.put(annotation.getId(), concept);
            } else if(annotation.getType().equals(BIO_TYPE)) {
                EventMention bio = new EventMention(jCas);
                setSpanAndDefaults(annotation, bio, jCas);
                bio.setSubject(BIO_TYPE);
                bio.addToIndexes();
                id2annotation.put(annotation.getId(), bio);
            }
        }
        for(BratAnnotation annotation : delayedRelations) {
            if (annotation.getType().equals(NEG_TYPE)) {
                // convert the relation into an attribute of the entity
                RelationAnnotation rel = (RelationAnnotation) annotation;
                String conceptId = rel.getArg1();
                String assertId = rel.getArg2();
                IdentifiedAnnotation annot = id2annotation.get(conceptId);
                if (annot == null) {
                    LOGGER.warning("Found negation applied to null annotation: " + conceptId);
                } else {
                    annot.setPolarity(CONST.NE_POLARITY_NEGATION_PRESENT);
                }
            } else if (annotation.getType().equals(UNC_TYPE)) {
                // convert the relation into an attribute of the entity
                RelationAnnotation rel = (RelationAnnotation) annotation;
                String conceptId = rel.getArg1();
                String assertId = rel.getArg2();
                IdentifiedAnnotation annot = id2annotation.get(conceptId);
                if (annot == null) {
                    LOGGER.warning("Found uncertainty applied to null concept: " + conceptId);
                } else {
                    annot.setUncertainty(CONST.NE_UNCERTAINTY_PRESENT);
                }
            }
        }
    }

    private static void setSpanAndDefaults(BratAnnotation bratAnnot, IdentifiedAnnotation uimaAnnot, JCas jcas){
        SpanAnnotation span = (SpanAnnotation) bratAnnot;
        uimaAnnot.setBegin(span.getSpan().getStart());
        uimaAnnot.setEnd(span.getSpan().getEnd());
        uimaAnnot.setPolarity(CONST.NE_POLARITY_NEGATION_ABSENT);
    }
}
