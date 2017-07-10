package fr.limsi.talmed.negation;

import org.apache.ctakes.core.util.DocumentIDAnnotationUtil;
import org.apache.ctakes.typesystem.type.constants.CONST;
import org.apache.ctakes.typesystem.type.textsem.*;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

/**
 * Created by miller on 7/10/17.
 */
public class BratMerlotWriter extends JCasAnnotator_ImplBase {

    public static final String PARAM_OUTPUT_DIR = "OutputDir";

    @ConfigurationParameter(name=PARAM_OUTPUT_DIR, description="Directory to write BRAT files")
    private String outputDir = null;

    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {
        String docId = DocumentIDAnnotationUtil.getDocumentID(jCas);
        File annFile = new File(outputDir, docId+".ann");
        int entityIndex = 1;

        try (PrintWriter out = new PrintWriter(annFile)) {
            for(IdentifiedAnnotation annot : JCasUtil.select(jCas, IdentifiedAnnotation.class)){
                String annType = null;
                if(annot instanceof ProcedureMention) annType = "MedicalProcedure";
                else if(annot instanceof DiseaseDisorderMention) annType = "Disorder";
                else if(annot instanceof SignSymptomMention) annType = "SignOrSymptom";
                else if(annot instanceof AnatomicalSiteMention) annType = "Anatomy";
                else if(annot instanceof MedicationEventMention) annType = "Chemicals_Drugs";
                else{
                    System.err.println("Unknown span type: " + annot.getClass().getCanonicalName());
                    annType = "UnknownSpan";
                }

                out.println(String.format("T%d\t%s %d %d\t%s",
                        entityIndex,
                        annType,
                        annot.getBegin(),
                        annot.getEnd(),
                        annot.getCoveredText().replace("\n", "\\n")));
                out.println(String.format("A%d\tNegated T%d %s",
                        entityIndex,
                        entityIndex,
                        Boolean.toString(annot.getPolarity() == CONST.NE_POLARITY_NEGATION_PRESENT)));
                entityIndex++;
            }
        }catch(FileNotFoundException e){
            throw new AnalysisEngineProcessException(e);
        }
    }
}
