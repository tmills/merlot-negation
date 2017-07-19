package fr.limsi.talmed.negation;

import org.apache.ctakes.assertion.eval.XMIReader;
import org.apache.uima.UIMAException;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AggregateBuilder;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.resource.ResourceInitializationException;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by miller on 7/10/17.
 */
public class CasToBratWriter {
    public static void main(String[] args) throws UIMAException, IOException {
        if(args.length < 2){
            System.err.println("Error: Two required arguments: <Input (XMI) directory> <Output (Brat) directory>");
            System.exit(-1);
        }
        File inputDir = new File(args[0]);
        List<File> xmiFiles = Arrays.asList(inputDir.listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        return name.endsWith(".xmi");
                    }
                }));

        CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(
                XMIReader.class,
                XMIReader.PARAM_FILES,
                xmiFiles);

        AggregateBuilder builder = new AggregateBuilder();
        builder.add(AnalysisEngineFactory.createEngineDescription(BratMerlotWriter.class,
                BratMerlotWriter.PARAM_OUTPUT_DIR,
                args[1]));
        SimplePipeline.runPipeline(reader, builder.createAggregateDescription());
    }
}
