package fr.limsi.talmed.negation;

import org.apache.uima.UIMAException;

import java.io.File;
import java.io.IOException;

/**
 * Created by miller on 7/10/17.
 */
public class BratToCasWriter {
    public static void main(String[] args) throws UIMAException, IOException {
        if(args.length < 2){
            System.err.println("Error: Two required arguments: <Input (Brat) directory> <Output (XMI) directory>");
        }

        MerlotCorpus.readMerlot(new File(args[0]), new File(args[1]));
    }
}
