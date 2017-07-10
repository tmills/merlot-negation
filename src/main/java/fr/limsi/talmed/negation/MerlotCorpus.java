package fr.limsi.talmed.negation;

import org.apache.ctakes.assertion.pipelines.PreprocessingPipeline;
import org.apache.ctakes.core.cc.XmiWriterCasConsumerCtakes;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.fit.factory.AggregateBuilder;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.cleartk.util.ae.UriToDocumentTextAnnotator;
import org.cleartk.util.cr.UriCollectionReader;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static fr.limsi.talmed.negation.MerlotCorpus.CORPUS_SECTION.DEV;
import static fr.limsi.talmed.negation.MerlotCorpus.CORPUS_SECTION.TEST;
import static fr.limsi.talmed.negation.MerlotCorpus.CORPUS_SECTION.TRAIN;

/**
 * Created by miller on 7/3/17.
 */
public class MerlotCorpus {
    private static Logger logger = Logger.getLogger(MerlotCorpus.class.getSimpleName());

    // According to the creators, 00-01 were corpus development sets
    // 02-21 are double annotated, so they serve as the test set
    // 22- are single annotated, so they serve as the training set
    // The train/dev split here is thus arbitrary and not part of
    // the official corpus
    enum CORPUS_SECTION {TRAIN, TEST, DEV}

    public static void readMerlot(File rawDir, File preprocessedDirectory) throws UIMAException, IOException {

        for(MerlotCorpus.CORPUS_SECTION section : MerlotCorpus.CORPUS_SECTION.values()) {
            List<File> files = MerlotCorpus.getFilesForSection(rawDir, section);

            logger.info(String.format("Added %d files from %s section with Merlot Brat corpus reader.", files.size(), section));

            CollectionReader reader = UriCollectionReader.getCollectionReaderFromFiles(files);
            AggregateBuilder aggregate = new AggregateBuilder();
            aggregate.add(UriToDocumentTextAnnotator.getDescription());
            aggregate.add(AnalysisEngineFactory.createEngineDescription(MerlotBratReaderAnnotator.class));
            aggregate.add(PreprocessingPipeline.getTokenPreprocessingDescription());
            if (preprocessedDirectory != null) {
                AnalysisEngineDescription xWriter2 = AnalysisEngineFactory.createEngineDescription(
                        XmiWriterCasConsumerCtakes.class,
                        XmiWriterCasConsumerCtakes.PARAM_OUTPUTDIR,
                        new File(preprocessedDirectory, section.toString().toLowerCase()));
                aggregate.add(xWriter2);
            }
            System.out.println("Starting pipeline for preprocessing!");
            SimplePipeline.runPipeline(reader, aggregate.createAggregate());
        }
    }

    public static List<File> getFilesForSection(File rootDir, CORPUS_SECTION section){
        List<File> files = new ArrayList<>();

        // get all the .ann files in the directory, then
        File[] subdirs = rootDir.listFiles();
        for(File subdir : subdirs){
            if(subdir.isDirectory()){
                String subdirName = subdir.getName();
                int setNum = Integer.parseInt(subdirName.substring(3));
                if(setNum < 2) continue; // 00 and 01 not part of any set
                else if(setNum < 22 && section != TEST) continue;
                else if(setNum >= 22 && setNum < 35 && section != DEV) continue;
                else if(setNum >= 35 && section != TRAIN) continue;

                File setDir = new File(subdir, subdirName + ".relations");
                File[] annFiles = setDir.listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        return name.endsWith(".ann");
                    }
                });
                for(File annFile : annFiles){
                    File txtFile = new File(annFile.getAbsolutePath().replace(".ann", ".txt"));
                    if(txtFile.exists()){
                        files.add(txtFile);
                    }
                }
            }
        }
        return files;
    }
}
