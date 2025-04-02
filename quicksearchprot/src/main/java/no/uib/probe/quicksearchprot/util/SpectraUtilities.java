package no.uib.probe.quicksearchprot.util;

import com.compomics.util.experiment.biology.modifications.Modification;
import com.compomics.util.experiment.biology.modifications.ModificationFactory;
import com.compomics.util.experiment.biology.proteins.Protein;
import com.compomics.util.experiment.identification.Advocate;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.experiment.identification.matches.SpectrumMatch;
import com.compomics.util.experiment.identification.protein_inference.fm_index.FMIndex;
import com.compomics.util.experiment.identification.spectrum_assumptions.PeptideAssumption;
import com.compomics.util.experiment.identification.spectrum_assumptions.TagAssumption;
import com.compomics.util.experiment.io.biology.protein.Header;
import com.compomics.util.experiment.io.biology.protein.iterators.FastaIterator;
import com.compomics.util.experiment.io.identification.IdfileReader;
import com.compomics.util.experiment.io.identification.IdfileReaderFactory;
import com.compomics.util.experiment.io.mass_spectrometry.MsFileHandler;
import com.compomics.util.experiment.io.mass_spectrometry.mgf.MgfFileWriter;
import com.compomics.util.experiment.mass_spectrometry.spectra.Spectrum;
import com.compomics.util.io.IoUtil;
import com.compomics.util.parameters.identification.IdentificationParameters;
import com.compomics.util.parameters.identification.search.SearchParameters;
import com.compomics.util.parameters.identification.tool_specific.DirecTagParameters;
import com.compomics.util.parameters.identification.tool_specific.MyriMatchParameters;
import eu.isas.searchgui.SearchHandler;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import no.uib.probe.quicksearchprot.configurations.Configurations;
import no.uib.probe.quicksearchprot.dataset.model.SearchingSubDataset;
import no.uib.probe.quicksearchprot.model.PSM;
import no.uib.probe.quicksearchprot.model.RawScoreModel;
import no.uib.probe.quicksearchprot.model.SearchInputSetting;
import no.uib.probe.quicksearchprot.search.SearchExecuter;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.xmlpull.v1.XmlPullParserException;

/**
 *
 * @author yfa041
 */
public class SpectraUtilities {

    private final SearchInputSetting searchOptimizerParameters;
    /**
     * The compomics PTM factory.
     */
    private static final ModificationFactory ptmFactory = ModificationFactory.getInstance();

    public SpectraUtilities(String datasetId) {
        searchOptimizerParameters = new SearchInputSetting();
        searchOptimizerParameters.setDatasetId(datasetId);
    }

    private File resultsFolder;
    private File oreginalFastaFile;
    private File oreginalMsFile;
    private File oreginalIdentificationFile;
    private IdentificationParameters identificationParameters;
    private int startIndex = 0;
    private final Map<String, Spectrum> spectrumMap = new LinkedHashMap<>();

    ;

    public File[] initInputSubSetFiles(File msFile, File fastaFile, File identificationParametersFile, int fullSpectrumSize) {
        try {

            long start1 = System.currentTimeMillis();
            String spectraFileName = IoUtil.removeExtension(msFile.getName());
            oreginalFastaFile = fastaFile;
            oreginalMsFile = msFile;
            oreginalIdentificationFile = identificationParametersFile;
            identificationParameters = IdentificationParameters.getIdentificationParameters(identificationParametersFile);
            SearchParameters searchParameters = identificationParameters.getSearchParameters();
            searchParameters.getModificationParameters().clearVariableModifications();
            searchParameters.getModificationParameters().clearFixedModifications();
            final String updatedName = Configurations.DEFAULT_RESULT_NAME + "_" + spectraFileName + Configurations.get_current_file_fingerprent() + "_" + fullSpectrumSize;
            File filteredSubMsFile = null;
            for (File f : oreginalMsFile.getParentFile().listFiles()) {
                if (f.getName().startsWith(Configurations.DEFAULT_RESULT_NAME + Configurations.get_current_file_fingerprent() + "_") && f.getName().endsWith("_" + oreginalMsFile.getName())) {
                    filteredSubMsFile = f;
                    break;
                }
            }
            if (filteredSubMsFile == null) {
                filteredSubMsFile = new File(msFile.getParent(), Configurations.DEFAULT_RESULT_NAME + Configurations.get_current_file_fingerprent() + "_" + fullSpectrumSize + "_" + msFile.getName());
            }
//            System.out.println("file name ms " + filteredSubMsFile.getName() + "   " + filteredSubMsFile.exists());
            if (!filteredSubMsFile.exists()) {

                /**
                 * ***
                 */
                MsFileHandler msFileHandler = new MsFileHandler();
                msFileHandler.register(msFile, new OptProtWaitingHandler());
                String fileNameWithoutExtension = IoUtil.removeExtension(msFile.getName());
                String[] spectrumTitles = msFileHandler.getSpectrumTitles(fileNameWithoutExtension);

                DirecTagParameters direcTagParameters = (DirecTagParameters) searchParameters.getIdentificationAlgorithmParameter(Advocate.direcTag.getIndex());
                direcTagParameters.setMaxTagCount(1);
                direcTagParameters.setTagLength(4);
                direcTagParameters.setNumChargeStates(4);
                searchOptimizerParameters.setRunDirecTag(true);
//               

                int step = spectrumTitles.length / (fullSpectrumSize);
                step = step / 4;
                while (spectrumMap.size() < fullSpectrumSize) {
                    int currentSpectrumSize = fullSpectrumSize - spectrumMap.size();
                    if (currentSpectrumSize <= 0) {
                        break;
                    }
                    currentSpectrumSize += (currentSpectrumSize * 0.1);
                    spectrumMap.putAll(substractSpectraWithConfidentTag(startIndex, currentSpectrumSize, msFileHandler, spectrumTitles, fileNameWithoutExtension));
                    startIndex += step;
//                    System.out.println("final size is " + spectrumMap.size() + "   " + currentSpectrumSize);

                }

                writeSpectraToFile(spectrumMap, filteredSubMsFile);

//                System.exit(0);
                /**
                 * **
                 */
//                File unfilteredSubMsFile = subsetSpectraFile(msFile, Configurations.EXTRACT_MS_TYPE.equals("TA"));
//                long end1st = System.currentTimeMillis();
//                double total = (end1st - start1) / 1000.0;
//                System.out.println("process I ( subsetSpectraFile) in seconds: " + total + "  generated file size ");
//                long start2 = System.currentTimeMillis();
//                DirecTagParameters direcTagParameters = (DirecTagParameters) searchParameters.getIdentificationAlgorithmParameter(Advocate.direcTag.getIndex());
//                direcTagParameters.setMaxTagCount(1);
//                direcTagParameters.setTagLength(4);
//                direcTagParameters.setNumChargeStates(4);
//                searchOptimizerParameters.setRunDirecTag(true);
//                resultsFolder = SearchExecuter.executeSearch(updatedName, searchOptimizerParameters, unfilteredSubMsFile, fastaFile, tempIdParam, identificationParametersFile);
//                File direcTagFile = new File(resultsFolder, IoUtil.removeExtension(unfilteredSubMsFile.getName()) + ".tags");
//                if (!direcTagFile.exists()) {
//                    //delete previos sub mgf and cms files 
//                    unfilteredSubMsFile.delete();
//                    File cms = new File(unfilteredSubMsFile.getParent(), unfilteredSubMsFile.getName().replace(".mgf", ".cms"));
//                    cms.delete();
//                    Configurations.EXTRACT_MAX_MS_SIZE += 1000;
//                    MainUtilities.cleanOutputFolder();
//                    return initInputSubSetFiles(msFile, fastaFile, identificationParametersFile);
//                }
//
//                IdfileReader idReader = IdfileReaderFactory.getInstance().getFileReader(direcTagFile);
//                MsFileHandler msFileHandler = new MsFileHandler();
//                msFileHandler.register(unfilteredSubMsFile, MainUtilities.OptProt_Waiting_Handler);
//                ArrayList<SpectrumMatch> matches = idReader.getAllSpectrumMatches(msFileHandler, MainUtilities.OptProt_Waiting_Handler, searchParameters);
//                Map<String, String> specTagMap = new LinkedHashMap<>();
//                for (SpectrumMatch sm : matches) {
//                    TagAssumption tag = sm.getAllTagAssumptions().toList().get(0);
//                    if (tag.getScore() < 0.01) {
//                        specTagMap.put(sm.getSpectrumTitle(), tag.getTag().getContent().get(1).asSequence());
//                    }
//                }
//                System.out.println("tag size " + specTagMap.size());
//                if (specTagMap.size() < Configurations.MIN_TAG_SIZE) {
//                    //delete previos sub mgf and cms files 
//                    unfilteredSubMsFile.delete();
//                    msFileHandler.close();
//                    File cms = new File(unfilteredSubMsFile.getParent(), unfilteredSubMsFile.getName().replace(".mgf", ".cms"));
//                    cms.delete();
////                    Configurations.EXTRACT_MAX_MS_SIZE += 500;
//                    Configurations.EXTRACT_MIN_MS_SIZE += 500;
//                    if (Configurations.EXTRACT_MAX_MS_SIZE <= Configurations.EXTRACT_MIN_MS_SIZE) {
//                        Configurations.EXTRACT_MAX_MS_SIZE = Configurations.EXTRACT_MIN_MS_SIZE + 500;
//                    }
//                    MainUtilities.cleanOutputFolder();
//                    return initInputSubSetFiles(msFile, fastaFile, identificationParametersFile);
//
//                }
//                filteredSubMsFile = subsetSpectraFile(unfilteredSubMsFile, specTagMap.keySet());
//                long end2ndst = System.currentTimeMillis();
//                total = (end2ndst - start2) / 1000.0;
//                System.out.println("process II ( DirecTag+Generated subMGF) in seconds: " + total);
//                File cms = new File(unfilteredSubMsFile.getParent(), unfilteredSubMsFile.getName().replace(".mgf", ".cms"));
//                cms.delete();
//                unfilteredSubMsFile.delete();
//                MainUtilities.deleteFolder(direcTagFile.getParentFile());
            }

            File subFasta = null;

            for (File f : oreginalFastaFile.getParentFile().listFiles()) {
                if (f.getName().startsWith(Configurations.DEFAULT_RESULT_NAME + Configurations.get_current_file_fingerprent() + "_") && f.getName().endsWith("_" + oreginalFastaFile.getName())) {
                    subFasta = f;
                    break;
                }
            }
            if (subFasta == null) {
                subFasta = new File(fastaFile.getParent(), Configurations.DEFAULT_RESULT_NAME + Configurations.get_current_file_fingerprent() + "_" + fullSpectrumSize + "_" + fastaFile.getName());
            }

            if (!subFasta.exists()) {
                long start3 = System.currentTimeMillis();
                searchOptimizerParameters.setRunNovor(true);
                resultsFolder = SearchExecuter.executeSearch(updatedName, searchOptimizerParameters, filteredSubMsFile, fastaFile, identificationParameters, identificationParametersFile);
                File NovorFile = new File(resultsFolder, IoUtil.removeExtension(filteredSubMsFile.getName()) + ".novor.csv");
                Set<String> sequences = SpectraUtilities.getSequences(NovorFile);
                System.out.println("sequence from nover " + sequences.size());
                long end3rd = System.currentTimeMillis();
                double total = (end3rd - start3) / 1000.0;
                System.out.println("process III ( Novor ) in seconds: " + total);
                long start4 = System.currentTimeMillis();
                subFasta = initSubFastaFile(fastaFile, sequences);
                long end4th = System.currentTimeMillis();
                total = (end4th - start4) / 1000.0;
                System.out.println("process IV ( Generated Fasta) in seconds: " + total);
                long end = System.currentTimeMillis();
                total = (end - start1) / 1000.0;
                System.out.println("Total Elapsed Time for initInputSubSetFiles in seconds: " + total);
                MainUtilities.deleteFolder(NovorFile.getParentFile());
            }

            return new File[]{filteredSubMsFile, subFasta};
        } catch (IOException ex) {  //ex) {//
            ex.printStackTrace();
            MainUtilities.cleanOutputFolder(searchOptimizerParameters.getDatasetId());
//            System.exit(0);
        }
        return null;

    }

    public static Map<String, Integer> initSpectraIndexMap(File msFile) {
        Map<String, Integer> spectraIndexMap = new HashMap<>();
        try {

            String fileNameWithoutExtension = IoUtil.removeExtension(msFile.getName());
            MsFileHandler msFileHandler = new MsFileHandler();
            msFileHandler.register(msFile, new OptProtWaitingHandler());
            String[] spectrumTitles = msFileHandler.getSpectrumTitles(fileNameWithoutExtension);
            for (int i = 0; i < spectrumTitles.length; i++) {
                spectraIndexMap.put(spectrumTitles[i], i);
            }
        } catch (IOException ex) {
            Logger.getLogger(SpectraUtilities.class.getName()).log(Level.SEVERE, null, ex);
        }
        return spectraIndexMap;

    }

    private File initSubFastaFile(File fastaFile, Set<String> sequences) {

        File subFastaFile = new File(fastaFile.getParent(), Configurations.DEFAULT_RESULT_NAME + Configurations.get_current_file_fingerprent() + "_" + fastaFile.getName());
        if (subFastaFile.exists()) {
            subFastaFile.delete();
        }
        SpectraUtilities.createSubFastaFile(fastaFile, subFastaFile, sequences);
//        SpectraUtilities.createSubFastaFile(fastaFile, subFastaFile,true);
        return subFastaFile;

    }

    private File subsetSpectraFile(File oreginalMsFile, Set<String> spectraTags) {

        try {
            String name = oreginalMsFile.getName().replace(Configurations.DEFAULT_RESULT_NAME + "_sub" + Configurations.get_current_file_fingerprent() + "_", "");
            File subMsFile = new File(oreginalMsFile.getParent(), Configurations.DEFAULT_RESULT_NAME + Configurations.get_current_file_fingerprent() + "_" + name);
            if (subMsFile.exists()) {
                subMsFile.delete();
                File subSampleCMS = new File(oreginalMsFile.getParent(), subMsFile.getName().replace(".mgf", ".cms"));
                subSampleCMS.delete();
            } else {
                subMsFile.createNewFile();
            }
            SpectraUtilities.writeFilteredMgfFile(oreginalMsFile, subMsFile, spectraTags);
            return subMsFile;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return oreginalMsFile;
    }

//    private File subsetSpectraFile(File oreginalMsFile, boolean targtedArea) {
//        try {
//            File subMsFile = new File(oreginalMsFile.getParent(), Configurations.DEFAULT_RESULT_NAME + "_sub" + Configurations.get_current_file_fingerprent() + "_" + oreginalMsFile.getName());
//            if (subMsFile.exists()) {
//                subMsFile.delete();
//                File subSampleCMS = new File(oreginalMsFile.getParent(), subMsFile.getName().replace(".mgf", ".cms"));
//                subSampleCMS.delete();
//            } else {
//                subMsFile.createNewFile();
//            }
//
//            if (targtedArea) {
//                SpectraUtilities.writeSubSetTargtedFileAreaMgfFile(oreginalMsFile, subMsFile, Configurations.EXTRACT_MIN_MS_SIZE, Configurations.EXTRACT_MAX_MS_SIZE);
//            } else {
//                SpectraUtilities.writeSubSetEveryNMgfFile(oreginalMsFile, subMsFile, Configurations.EXTRACT_MIN_MS_SIZE, Configurations.EXTRACT_MAX_MS_SIZE);
//            }
//            return subMsFile;
//        } catch (IOException ex) {
//            ex.printStackTrace();
//        }
//        return oreginalMsFile;
//    }
    /**
     * Writes the spectra of a file in the Mascot Generic File (mgf) format.
     *
     * @param mgfFile The spectrum file to use to get the spectra.
     * @param destinationFile The file where to write.
     * @param maxSpectNum number of spectra in the final file
     */
    public static void writeSubSetEveryNMgfFile(
            File mgfFile,
            File destinationFile,
            double minSpectNum,
            double maxSpectNum
    ) {
        try {
            String fileNameWithoutExtension = IoUtil.removeExtension(mgfFile.getName());
            MsFileHandler msFileHandler = new MsFileHandler();
            msFileHandler.register(mgfFile, new OptProtWaitingHandler());
            String[] spectrumTitles = msFileHandler.getSpectrumTitles(fileNameWithoutExtension);

            int initiSize = Math.min((int) maxSpectNum, (int) Math.round(spectrumTitles.length * 20 / 100));
            initiSize = Math.max(initiSize, (int) minSpectNum);
//            System.out.println("initial zer " + initiSize + "  vs " + maxSpectNum);

            int n = (int) Math.round(spectrumTitles.length / initiSize);
            if (spectrumTitles == null) {
                throw new IllegalArgumentException(
                        fileNameWithoutExtension + " not loaded."
                );
            }
            MgfFileWriter writer = new MgfFileWriter(destinationFile);
            for (int i = 0; i < spectrumTitles.length;) {
                String spectrumTitle = spectrumTitles[i];
                Spectrum spectrum = msFileHandler.getSpectrum(fileNameWithoutExtension, spectrumTitle);
                writer.writeSpectrum(spectrumTitle, spectrum);
                i += n;
            }

            writer.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    /**
     * Writes the spectra of a file in the Mascot Generic File (mgf) format.
     *
     * @param mgfFile The spectrum file to use to get the spectra.
     * @param destinationFile The file where to write.
     * @param ids
     */
    public static void writeFilteredMgfFile(
            File mgfFile,
            File destinationFile,
            Set<String> ids
    ) {
        try {
            String fileNameWithoutExtension = IoUtil.removeExtension(mgfFile.getName());
            MsFileHandler msFileHandler = new MsFileHandler();
            msFileHandler.register(mgfFile, new OptProtWaitingHandler());
            int counter = 0;
            String[] spectrumTitles = msFileHandler.getSpectrumTitles(fileNameWithoutExtension);

            if (spectrumTitles == null) {
                throw new IllegalArgumentException(
                        fileNameWithoutExtension + " not loaded."
                );
            }
            MgfFileWriter writer = new MgfFileWriter(destinationFile);
            for (int i = 0; i < (spectrumTitles.length); i++) {
                String spectrumTitle = spectrumTitles[i];
                if (ids.contains(spectrumTitle)) {
                    Spectrum spectrum = msFileHandler.getSpectrum(fileNameWithoutExtension, spectrumTitle);
                    writer.writeSpectrum(spectrumTitle, spectrum);
                    counter++;
                }
            }
//            System.out.println("total filtered subset mgf " + counter);
            writer.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    /**
     * Writes the spectra of a file in the Mascot Generic File (mgf) format.
     *
     * @param mgfFile The spectrum file to use to get the spectra.
     * @param destinationFile The file where to write.
     * @param minSpecNum
     *
     * @param maxSpectNum number of spectra in the final file
     */
    public static void writeSubSetTargtedFileAreaMgfFile(
            File mgfFile,
            File destinationFile,
            double minSpecNum,
            double maxSpectNum
    ) {
        try {
            String fileNameWithoutExtension = IoUtil.removeExtension(mgfFile.getName());
            MsFileHandler msFileHandler = new MsFileHandler();
            msFileHandler.register(mgfFile, new OptProtWaitingHandler());

            String[] spectrumTitles = msFileHandler.getSpectrumTitles(fileNameWithoutExtension);
            int targetedAreaStep = spectrumTitles.length / 4;

            int initiSize = Math.min((int) maxSpectNum, (int) Math.round(targetedAreaStep * 20 / 100));
            initiSize = Math.max(initiSize, (int) minSpecNum);
//            System.out.println("initial zer " + initiSize + "  vs " + maxSpectNum);

            int n = (int) Math.round(targetedAreaStep * 2 / initiSize);

            if (spectrumTitles == null) {
                throw new IllegalArgumentException(
                        fileNameWithoutExtension + " not loaded."
                );
            }
            MgfFileWriter writer = new MgfFileWriter(destinationFile);
            for (int i = targetedAreaStep; i < (spectrumTitles.length - targetedAreaStep);) {
                String spectrumTitle = spectrumTitles[i];
                Spectrum spectrum = msFileHandler.getSpectrum(fileNameWithoutExtension, spectrumTitle);
                writer.writeSpectrum(spectrumTitle, spectrum);
                i += n;
            }

            writer.close();
//            System.out.println("at n: " + n + "  " + targetedAreaStep * 2 + "  " + spectrumTitles.length);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    /**
     * Writes the spectra of a file in the Mascot Generic File (mgf) format.
     *
     * @param spectrumResource
     * @param destinationFile The file where to write.
     */
    public static void writeSpectraToFile(
            Map<String, Spectrum> spectrumResource,
            File destinationFile) {
        MgfFileWriter writer = new MgfFileWriter(destinationFile);
        for (String spectrumTitle : spectrumResource.keySet()) {
            Spectrum spectrum = spectrumResource.get(spectrumTitle);
            writer.writeSpectrum(spectrumTitle, spectrum);
        }
        writer.close();

    }

    /**
     * Appends decoy sequences to the provided FASTA file.
     *
     * @param fastaIn the FASTA file to read
     * @param fastaOut the FASTA file to write displaying progress
     * @param micro
     */
    public static void createSubFastaFile(
            File fastaIn,
            File fastaOut,
            boolean micro
    ) {

        try {
            int total = 0;
            int left = 0;
            int specIndex = 0;
            FastaIterator fastaIterator = new FastaIterator(fastaIn);

            try (BufferedWriter bw = new BufferedWriter(new FileWriter(fastaOut))) {
                Protein protein;
                while ((protein = fastaIterator.getNextProtein()) != null) {
                    Header header = fastaIterator.getLastHeader();
                    total++;
                    if (header.getProteinEvidence() != 1 || protein.getLength() <= 400) { //&& !header.asGenericHeader().contains("SV=2")
                        continue;
                    }
                    specIndex++;
                    if (micro) {
                        if (specIndex == 3) {
                            specIndex = 0;
                            continue;
                        }
                    }
                    left++;
                    String sequence = protein.getSequence();
                    String rawHeader = header.getRawHeader();
                    bw.write(rawHeader);
                    bw.newLine();
                    bw.write(sequence);

                    bw.newLine();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }
    }

    public static void createSubFastaFile(
            File fastaIn,
            File fastaOut,
            Set<String> sequences
    ) {

        try {
            int total = 0;
            int included = 0;
            int specIndex = 0;
            FastaIterator fastaIterator = new FastaIterator(fastaIn);

            try (BufferedWriter bw = new BufferedWriter(new FileWriter(fastaOut))) {
                Protein protein;
                while ((protein = fastaIterator.getNextProtein()) != null) {
                    Header header = fastaIterator.getLastHeader();
                    total++;
                    if (header.getProteinEvidence() != 1 || protein.getAccession().endsWith("_REVERSED")) { //&& !header.asGenericHeader().contains("SV=2")|| protein.getLength() <= 400
                        continue;
                    }
                    boolean escape = true;
                    for (String seq : sequences) {
                        if (protein.getSequence().replace("I", "L").contains(seq.replace("I", "L"))) {
                            escape = false;
                            break;
                        }
                    }
                    if (escape) {
                        specIndex++;
                        continue;
                    }

                    included++;
                    String sequence = protein.getSequence();
                    String rawHeader = header.getRawHeader();
                    bw.write(rawHeader);
                    bw.newLine();
                    bw.write(sequence);
                    bw.newLine();

                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            if (included < 50) {
                copySubFastaFile(fastaIn, fastaOut);
            }
            System.out.println("total # prot sequence number " + total + "  sub fasta Size " + included);

        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }

    }

    public static void copySubFastaFile(
            File fastaIn,
            File fastaOut
    ) {

        try {
            fastaOut.delete();
            fastaOut.createNewFile();
            int total = 0;
            int left = 0;
            FastaIterator fastaIterator = new FastaIterator(fastaIn);

            try (BufferedWriter bw = new BufferedWriter(new FileWriter(fastaOut))) {
                Protein protein;
                while ((protein = fastaIterator.getNextProtein()) != null) {
                    Header header = fastaIterator.getLastHeader();
                    total++;
                    if (header.getProteinEvidence() != 1 || protein.getAccession().endsWith("_REVERSED")) { //&& !header.asGenericHeader().contains("SV=2")|| protein.getLength() <= 400
                        continue;
                    }

                    left++;
                    String sequence = protein.getSequence();
                    String rawHeader = header.getRawHeader();
                    bw.write(rawHeader);
                    bw.newLine();
                    bw.write(sequence);
                    bw.newLine();

                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            Logger.getLogger(SpectraUtilities.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public static Set<String> getSequences(File novorOutputFile) {
        String line;
        String splitBy = ",";
        Set<String> sequences = new HashSet<>();
        try {
//parsing a CSV file into BufferedReader class constructor  
            BufferedReader br = new BufferedReader(new FileReader(novorOutputFile));
            while ((line = br.readLine()) != null) //returns a Boolean value 
            {
                if (line.startsWith("# id,")) {
                    break;
                }
            }

            while ((line = br.readLine()) != null) //returns a Boolean value 
            {
                String[] values = line.split(splitBy);    // use comma as separator  
                sequences.add(values[9].replaceAll("[()0123456]", "").trim());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sequences;
    }

    public static Set<String> getTaggedSequences(File novorOutputFile) {
        String line;
        String splitBy = ",";
        Set<String> sequences = new HashSet<>();
        try {
//parsing a CSV file into BufferedReader class constructor  
            BufferedReader br = new BufferedReader(new FileReader(novorOutputFile));
            while ((line = br.readLine()) != null) //returns a Boolean value 
            {
                if (line.startsWith("# id,")) {
                    break;
                }
            }
            while ((line = br.readLine()) != null) //returns a Boolean value 
            {
                String[] values = line.split(splitBy);    // use comma as separator  
                sequences.add(values[9].trim());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sequences;
    }

    /**
     * Lets the user select an output folder and starts the recalibration of
     * spectra.
     *
     * @param sourceMgfFile
     * @param fastaFile
     * @param spectrumMatches
     * @param identificationParameters
     * @return
     */
    public static File recalibrateSpectra(File sourceMgfFile, File fastaFile, ArrayList<SpectrumMatch> spectrumMatches, IdentificationParameters identificationParameters) {

        try {
            final File selectedFolder = new File(sourceMgfFile.getParent());
            MsFileHandler spectrumProvider = new MsFileHandler();
            spectrumProvider.register(sourceMgfFile, new OptProtWaitingHandler());
            final boolean precursors = true;
            final boolean fragments = true;
            FMIndex sequenceProvider = new FMIndex(fastaFile, null, new OptProtWaitingHandler(), false, identificationParameters);
            try {
                return SpectralRecalibrationUtilitiy.writeRecalibratedSpectra(
                        precursors,
                        fragments,
                        selectedFolder,
                        spectrumMatches,
                        sequenceProvider,
                        spectrumProvider,
                        identificationParameters
                ).get(0);

            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (IOException ex) {
            Logger.getLogger(SpectraUtilities.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public Map<String, Spectrum> substractSpectraWithConfidentTag(int startIndex, int maxSpectraNumber, MsFileHandler msFileHandler, String[] spectrumTitles, String msFileNameWithoutExtension) {
        Map<String, Spectrum> spectraMap = new LinkedHashMap<>();
        try {

            int targetedAreaStep = spectrumTitles.length / 4;
            int n = targetedAreaStep * 2 / (maxSpectraNumber);
            File destinationFile = new File(oreginalMsFile.getParentFile(), Configurations.DEFAULT_RESULT_NAME + "_" + msFileNameWithoutExtension + ".mgf");
            destinationFile.createNewFile();
            try (MgfFileWriter writer = new MgfFileWriter(destinationFile)) {
                for (int i = startIndex + targetedAreaStep; i < (spectrumTitles.length - targetedAreaStep);) {
                    Spectrum spectrum = msFileHandler.getSpectrum(msFileNameWithoutExtension, spectrumTitles[i]);
                    writer.writeSpectrum(spectrumTitles[i], spectrum);
                    spectraMap.put(spectrumTitles[i], spectrum);
                    i += n;
                }
            }
            String updatedName = Configurations.DEFAULT_RESULT_NAME + "_" + msFileNameWithoutExtension + Configurations.get_current_file_fingerprent();

            File tempResultsFolder = SearchExecuter.executeSearch(updatedName, searchOptimizerParameters, destinationFile, oreginalFastaFile, identificationParameters, oreginalIdentificationFile);

            File direcTagFile = new File(tempResultsFolder, IoUtil.removeExtension(destinationFile.getName()) + ".tags");

            if (!direcTagFile.exists()) {
                //delete previos sub mgf and cms files
                destinationFile.delete();
                File cms = new File(destinationFile.getParent(), destinationFile.getName().replace(".mgf", ".cms"));
                cms.delete();
                substractSpectraWithConfidentTag(startIndex, maxSpectraNumber + 500, msFileHandler, spectrumTitles, msFileNameWithoutExtension);
                return spectraMap;
            }

            IdfileReader idReader = IdfileReaderFactory.getInstance().getFileReader(direcTagFile);
            MsFileHandler subMsFileHandler = new MsFileHandler();
            subMsFileHandler.register(destinationFile, MainUtilities.OptProt_Waiting_Handler);
            ArrayList<SpectrumMatch> matches = idReader.getAllSpectrumMatches(subMsFileHandler, MainUtilities.OptProt_Waiting_Handler, identificationParameters.getSearchParameters());
            for (SpectrumMatch sm : matches) {
                TagAssumption tag = sm.getAllTagAssumptions().toList().get(0);
                if (tag.getScore() > 0.01) {
                    spectraMap.remove(sm.getSpectrumTitle());
                }
            }
            destinationFile.delete();
            File cms = new File(destinationFile.getParent(), destinationFile.getName().replace(".mgf", ".cms"));
            cms.delete();

        } catch (IOException | SQLException | ClassNotFoundException | InterruptedException | JAXBException | XmlPullParserException | XMLStreamException ex) {
            ex.printStackTrace();
        }
        return spectraMap;
    }

    public static RawScoreModel getComparableRawScore(SearchingSubDataset optProtDataset, List<SpectrumMatch> matches, Advocate searchEngine, boolean addData, String scoreId, boolean potintialFP) {

//        List<Double> sharedTestData = new ArrayList<>();
//        List<Double> sharedReferenceData = new ArrayList<>();
//        List<Double> uniqueReferenceData = new ArrayList<>();
//        List<Double> uniqueTestData = new ArrayList<>();
//        Map<String, Double> testPsmScores = new HashMap<>();
        RawScoreModel rawScore = new RawScoreModel(scoreId);
        rawScore.setDiffrentInSize(matches.size() - optProtDataset.getVaildatedPsmMaches().size());
//        for (SpectrumMatch sm : matches) {
//            testPsmScores.put(sm.getSpectrumTitle(), sm.getBestPeptideAssumption().getRawScore());
//        }

        double score = compareDataScores(optProtDataset.getVaildatedPsmMaches(), matches, optProtDataset.getSubsetSize(), potintialFP, searchEngine.getName().equalsIgnoreCase(Advocate.sage.getName()));

//        for (String titile : optProtDataset.getFullSpectraScore().keySet()) {
//            double referenceScore = optProtDataset.getFullSpectraScore().get(titile);
//            if (testPsmScores.containsKey(titile)) {
//                double toScore = testPsmScores.get(titile);
//                if (referenceScore > 0) {
//                    sharedTestData.add(toScore);
//                    sharedReferenceData.add(referenceScore);
//                } else {
//                    uniqueTestData.add(toScore);
//                }
//
//            } else if (referenceScore > 0) {
//                uniqueReferenceData.add(referenceScore);
//            }
//        }
//        if (sharedReferenceData.isEmpty() && matches.size() < ((double) uniqueReferenceData.size() * 0.2)) {
//            rawScore.setIdPSMNumber(matches.size());
//            rawScore.setFinalScore(-10);
//            return rawScore;
//        }
//        Collections.sort(sharedReferenceData);
//        //devide the TODATA data into 3 category         
//        double[] referenceSharedData = sharedReferenceData.stream().mapToDouble(Double::doubleValue).toArray();
//        double[] toSharedData = sharedTestData.stream().mapToDouble(Double::doubleValue).toArray();
//        double[] referenceUniqueData = (uniqueReferenceData.stream().mapToDouble(Double::doubleValue).toArray());
//        double[] testUniqueData = (uniqueTestData.stream().mapToDouble(Double::doubleValue).toArray());
//        boolean escapeScore = false;
//        if (potintialFP) {//(referenceUniqueData.length <10 && toOnlyData.length > 10 && potintialFP) {
//            DescriptiveStatistics ds2 = new DescriptiveStatistics(referenceSharedData);
//            for (double d : referenceUniqueData) {
//                ds2.addValue(d);
//            }
//            int counter = 0;
//            for (double d : testUniqueData) {
//                if (d > ds2.getPercentile(50)) {
//                    counter++;
//                }
//            }
//            double noiseData = testUniqueData.length - counter;
//
//            if (counter < (0.05 * referenceSharedData.length) || noiseData > counter) {
//                rawScore.setS1(-1);
//                rawScore.setS2(-1);
//                rawScore.setIdPSMNumber(matches.size());
//                rawScore.setFinalScore(-1);
//                escapeScore = true;
//            } else {
//                double[] updatedToOnlyData = new double[counter];
//                int i = 0;
//                for (double d : testUniqueData) {
//                    if (d > ds2.getPercentile(50)) {
//                        updatedToOnlyData[i] = d;
//                        i++;
//                    }
//                }
//
//                System.out.println(counter + " noise  " + noiseData + "  reference size " + (referenceSharedData.length) + "    test size  " + testUniqueData.length + "   to --> " + updatedToOnlyData.length + "----------------------------------------------------------------------------------------------------------------------------------------->>> adding new spec only potintial fb " + testUniqueData.length + "  ");
//
//                testUniqueData = updatedToOnlyData;
//
//            }
//        }
//        if (!escapeScore) {
//            double score1 = SpectraUtilities.performcomparison(referenceSharedData, toSharedData, true);
////        double totalSampleSize = toSharedData.length + uniqueReferenceData.size();
////        double ratio1 = (double) sharedReferenceData.size() / totalSampleSize; //1;//
////        double ratio2 = (double) Math.max(onlyToData.size(), uniqueReferenceData.size()) / totalSampleSize; //Math.max(onlyToData.size(), onlyFromData.size()) / (double) optProtDataset.getSubsetSize(); //
////        
//
//            double totalSampleSize = 2.0 * optProtDataset.getSubsetSize();// uniqueReferenceData.size() + uniqueTestData.size() + sharedTestData.size() + sharedReferenceData.size();// toSharedData.length + uniqueReferenceData.size();
//            double ratio1 = (double) (2.0 * sharedReferenceData.size()) / totalSampleSize; //1;//
////        double ratio2 = (1.0 - ratio1);// (double) Math.max(onlyToData.size(), uniqueReferenceData.size()) / totalSampleSize;   //Math.max(onlyToData.size(), onlyFromData.size()) / (double) optProtDataset.getSubsetSize();//Math.max(onlyToData.size(), onlyFromData.size()) / totalSampleSize;   //
//
//            double uniqueSize = (testUniqueData.length + referenceUniqueData.length);
//            if (testUniqueData.length <= 4 && referenceUniqueData.length > 4) {
//                uniqueSize = (2.0 * referenceUniqueData.length);
//            } else if (referenceUniqueData.length <= 4 && testUniqueData.length > 4) {
//                uniqueSize = (2.0 * testUniqueData.length);
//            }
//
//            double ratio2 = uniqueSize / totalSampleSize;// (double) Math.max(onlyToData.size(), uniqueReferenceData.size()) / totalSampleSize;   //Math.max(onlyToData.size(), onlyFromData.size()) / (double) optProtDataset.getSubsetSize();//Math.max(onlyToData.size(), onlyFromData.size()) / totalSampleSize;   //
//
//            double score2 = SpectraUtilities.performcomparison(referenceUniqueData, testUniqueData, false);
//            double adjustedScore1 = score1 * ratio1;
//            double adjustedScore2 = score2 * ratio2;
//            if (referenceUniqueData.length <= 4 && testUniqueData.length <= 4) {
//                System.out.println("------------------------------ignor S2 ");
//                adjustedScore2 = adjustedScore1;
//            }
//
//            double finalScore = adjustedScore1 + adjustedScore2;
//            System.out.println("at uniqueTestData.size() " + uniqueTestData.size() + "   uniqueReferenceData.size() " + uniqueReferenceData.size() + "  sharedReferenceData " + sharedReferenceData.size());
////     
////        finalScore = logScaleNormalize(finalScore, 2);
//            MainUtilities.addToParamScoreSet(finalScore);
//            System.out.println("------------------------------------------------------------>>>> final scaled score " + finalScore + "  " + score);
//            rawScore.setS1(adjustedScore1);
//            rawScore.setS2(adjustedScore2);
        rawScore.setIdPSMNumber(matches.size());
        rawScore.setFinalScore(score);

//        }
//        rawScore.setSharedDataSize(referenceSharedData.length);
        rawScore.setSameData(rawScore.getFinalScore() == 0.0 && matches.size() == (optProtDataset.getVaildatedPsmMaches().size()));
        if (!rawScore.isSameData()) {
            boolean senstive = rawScore.getRawFinalScore() > 0;
            if (rawScore.getRawFinalScore() == 0 && matches.size() > (optProtDataset.getVaildatedPsmMaches().size()) && !potintialFP) {
                System.out.println("****** new senstive measurment applied *******");
                senstive = true;
            }
            rawScore.setAcceptedChange(senstive);
            if (rawScore.getRawFinalScore() > 0) {
                senstive = true;
                System.out.println("senstive 1");
            } else if (optProtDataset.getCurrentScoreModel() != null && (rawScore.getRawFinalScore() == 0 && matches.size() > optProtDataset.getIdentifiedPSMsNumber())) {
                senstive = true;
                System.out.println("senstive 2 " + rawScore.getRawFinalScore() + "  " +  "  " + matches.size() + "  " + optProtDataset.getIdentifiedPSMsNumber());
            } else if (optProtDataset.getCurrentScoreModel() != null && (rawScore.getRawFinalScore() >= (0) && matches.size() > (double) optProtDataset.getIdentifiedPSMsNumber())) {
                senstive = true;
                System.out.println("senstive 3 were to apply " + rawScore.getRawFinalScore() + "  ");
            }
//            else if ((rawScore.getFinalScore() >= (-1.0 * optProtDataset.getBasicComparisonThreshold()) && matches.size() > (double) optProtDataset.getIdentifiedPSMsNumber())) {
//                System.out.println("senstive 3");
//                senstive = true;
//            }
//            else if ((fs1 > 0 && matches.size() > optProtDataset.getActiveIdentificationNum())) {
//                senstive = true;
//            }
//            else if (fs>0){
//                senstive = true;
//            }

            rawScore.setSensitiveChange(senstive);
        }
        if (rawScore.isAcceptedChange() || rawScore.isSensitiveChange() || rawScore.isSameData() || addData) {
            rawScore.setSpectrumMatchResult(matches);
        }
        return rawScore;

    }

    public static double compareDataScores2(List<SpectrumMatch> referencePsmList, List<SpectrumMatch> testPsmList, double totalSize, boolean potintialFP) {

        if (testPsmList.isEmpty()) {
            return -1.0;
        }
        if (referencePsmList.isEmpty()) {
            return 1.0;
        }
        if (testPsmList.isEmpty() && referencePsmList.isEmpty()) {
            return 0;
        }
        double cScore2 = compareDataScores2(referencePsmList, testPsmList, totalSize, potintialFP);

        System.out.println("-------------------------------------------------------------------");
        if (referencePsmList == null && testPsmList != null) {
            return 100;
        }
        if (referencePsmList != null && testPsmList == null) {
            return -100;
        }
        if (referencePsmList == null && testPsmList == null) {
            return 0;
        }
        List<Double> sharedTestData = new ArrayList<>();
        List<Double> sharedReferenceData = new ArrayList<>();
        List<Double> uniqueTestData = new ArrayList<>();
        List<Double> uniqueReferenceData = new ArrayList<>();
        Map<String, Double> testPsmScores = new HashMap<>();
        Map<String, Double> referencePsmScores = new HashMap<>();
        for (SpectrumMatch sm : referencePsmList) {
            referencePsmScores.put(sm.getSpectrumTitle(), sm.getBestPeptideAssumption().getRawScore());
        }
        for (SpectrumMatch sm : testPsmList) {
            testPsmScores.put(sm.getSpectrumTitle(), sm.getBestPeptideAssumption().getRawScore());
        }
        Set<String> combinedSpecHeader = new LinkedHashSet<>(testPsmScores.keySet());
        combinedSpecHeader.addAll(referencePsmScores.keySet());
        for (String spectrumTitle : combinedSpecHeader) {
            if (testPsmScores.containsKey(spectrumTitle) && referencePsmScores.containsKey(spectrumTitle)) {
                sharedTestData.add(testPsmScores.get(spectrumTitle));
                sharedReferenceData.add(referencePsmScores.get(spectrumTitle));
            } else if (testPsmScores.containsKey(spectrumTitle) && !referencePsmScores.containsKey(spectrumTitle)) {
                uniqueTestData.add(testPsmScores.get(spectrumTitle));
            } else {
                uniqueReferenceData.add(referencePsmScores.get(spectrumTitle));

            }

        }
        double[] referenceSharedData = sharedReferenceData.stream().mapToDouble(Double::doubleValue).toArray();
        double[] testSharedData = sharedTestData.stream().mapToDouble(Double::doubleValue).toArray();
        double[] referenceFullData = referencePsmScores.values().stream().mapToDouble(Double::doubleValue).toArray();
        double[] referenceIndependentData = uniqueReferenceData.stream().mapToDouble(Double::doubleValue).toArray();
        double[] testIndependentData = uniqueTestData.stream().mapToDouble(Double::doubleValue).toArray();
        double[] testFullData = testPsmScores.values().stream().mapToDouble(Double::doubleValue).toArray();
        System.out.println("----------------------data preparation is done ---------------------------");
//        KolmogorovSmirnovTest ksTest = new KolmogorovSmirnovTest();
//        double pValue = ksTest.kolmogorovSmirnovTest(referenceFullData, testFullData);
//        System.out.println("P-value: " + pValue);
        double yallaScore;
//        if (pValue < 0.05) {
        System.out.println("The samples do not belong to the same distribution.");
        yallaScore = ScoreComparison.comparediffrentDistributionData(referenceFullData, testFullData);
//            if (potintialFP) {
//                System.out.println("pot FP in not same dist");
//                System.exit(0);
//            }
//        } else {
//        if (potintialFP) {
//            System.out.println("The samples belong to the same distribution.");
//            ScoreComparison.compareSameDistributionData(referenceFullData, testFullData);
//            yallaScore = ScoreComparison.comparediffrentDistributionData(referenceFullData, testFullData);
//        } else {
//            yallaScore = ScoreComparison.comparediffrentDistributionData(referenceFullData, testFullData);
//        }
//        }
        if (potintialFP && yallaScore == 0) {
            yallaScore = -1;
        } else if (yallaScore == 0 && referenceFullData.length < testFullData.length) {
            yallaScore = 1;
        } else if (yallaScore == 0 && referenceFullData.length > testFullData.length) {
            System.out.println("referenceFullData.length " + referenceFullData.length + "   " + testFullData.length);
            yallaScore = -1;
        }
//        double score1 = SpectraUtilities.performcomparison(referenceSharedData, testSharedData, true);
//        System.out.println("-----------------end paired-----------------------");
//        double pairedDataRatio = ((double) referenceSharedData.length * 2.0) / (double) (referencePsmList.size() + testPsmList.size());
//        double uniqueDataSize = uniqueReferenceData.size() + uniqueTestData.size();
//
//        //effect on reference compare shred vs all data
//        double referenceIndependedntDataEffect = SpectraUtilities.performcomparison(referenceSharedData, referenceFullData, false) * -1.0;
//        System.out.println("reference data effect *" + referenceIndependedntDataEffect + "*");
//        double refIndependentRatio = ((double) uniqueReferenceData.size()) / (double) referenceFullData.length;
//        //effect on reference compare shred vs all data
//
//        double testIndependedntDataEffect = SpectraUtilities.performcomparison(testSharedData, testFullData, false);
//        double testIndependentRatio = ((double) uniqueTestData.size()) / (double) testFullData.length;
//
//        double yallaScore = (score1 * pairedDataRatio) + (referenceIndependedntDataEffect * refIndependentRatio) + (testIndependedntDataEffect * testIndependentRatio);
        System.out.println("Score_old : " + cScore2 + " vs yalla_score : " + yallaScore);//+ " pratio " + pairedDataRatio + "   paired " + score1 + "  refInEff " + referenceIndependedntDataEffect + " tesInEff " + testIndependedntDataEffect);
//        if ((cScore2 > 0 && yallaScore < 0) || (cScore2 < 0 && yallaScore > 0)) {
//            System.out.println("oposit results check -------------------------->>>>>");
//            System.out.println("paired data size " + sharedReferenceData.size() + "  ref ind " + uniqueReferenceData.size() + "  " + uniqueTestData.size() + "  ratios " + refIndependentRatio + " , " + testIndependentRatio);
//
//            // Calculate skewness
//            Skewness skewness = new Skewness();
//            double skewnessValue = skewness.evaluate(referenceFullData);
//            // Output the result
//            System.out.println("ref full Skewness: " + skewnessValue);
//            double skewnessValue1 = skewness.evaluate(referenceSharedData);
//            // Output the result
//            System.out.println("ref share Skewness: " + skewnessValue1);
//
//            double skewnessValue2 = skewness.evaluate(referenceIndependentData);
//            // Output the result
//            System.out.println("ref independent Skewness: " + skewnessValue2);
//
//            double skewnessValue3 = skewness.evaluate(testFullData);
//            // Output the result
//            System.out.println("test full Skewness: " + skewnessValue3);
//            double skewnessValue4 = skewness.evaluate(testSharedData);
//            // Output the result
//            System.out.println("test share Skewness: " + skewnessValue4);
//
//            double skewnessValue5 = skewness.evaluate(testIndependentData);
//            // Output the result
//            System.out.println("test independent Skewness: " + skewnessValue5);

//            System.exit(0);
//        }
//        double uniqeToTotal = ((double) uniqueTestData.size() + (double) uniqueReferenceData.size()) / ((double) referencePsmList.size() + (double) testPsmList.size());
//        
//        double[] referenceUniqueData = uniqueReferenceData.stream().mapToDouble(Double::doubleValue).toArray();
//        double[] testUniqueData = uniqueTestData.stream().mapToDouble(Double::doubleValue).toArray();
//
//        
//        double score2 = 0;
//        if (potintialFP) {
//            System.out.println("improvment percintage " + "  pv " + "  ztestPvalue " + referenceSharedData.length + "   " + testSharedData.length);
//            double pValueZtest = StatisticsTests.pairedZTest(referenceSharedData, testSharedData);
//            if ((pValueZtest < 0.05) != TestUtils.tTest(referenceSharedData, testSharedData, 0.05)) {
//                System.out.println("improvment percintage " + "  pv " + "  ztestPvalue " + pValueZtest + "  tpvalue " + TestUtils.tTest(referenceSharedData, testSharedData, 0.05) + "   ");
//            }
//            if (!TestUtils.tTest(referenceSharedData, testSharedData, 0.05)) {
//                score1 = 0;
//            } else {
//                score1 = SpectraUtilities.performcomparison(referenceSharedData, testSharedData, true);
//            }
//            DescriptiveStatistics refDescStat = new DescriptiveStatistics(referenceSharedData);
//            for (double d : uniqueReferenceData) {
//                refDescStat.addValue(d);
//            }
//            double quart1 = refDescStat.getPercentile(25);
//            List<Double> goodSampleUniqueTestDataList = new ArrayList<>();
//            for (double d : uniqueTestData) {
//                if (d > quart1) {
//                    goodSampleUniqueTestDataList.add(d);
//                }
//            }
//            double[] goodSampleUniqueTestData = goodSampleUniqueTestDataList.stream().mapToDouble(Double::doubleValue).toArray();
//            //ttest
//
//            if (uniqueTestData.size() < 30 && goodSampleUniqueTestData.length > 2) {
////           measure the low quaity effect on data 
//                if (TestUtils.tTest(testUniqueData, goodSampleUniqueTestData, 0.05)) {
//                    score2 = -100;
//                }
//            } else if (goodSampleUniqueTestData.length > 2) {
//                //ztest
//                double p = StatisticsTests.unpairedZTest(testUniqueData, goodSampleUniqueTestData);
//                if ((p < 0.05)) {
//                    score2 = -100;
//                }
//            } else if (uniqueTestData.size() > 2 && goodSampleUniqueTestData.length <= 2) {
//                score2 = -100;
//            }
//            if (score2 != -100.0) {
//                testUniqueData = goodSampleUniqueTestData;
//                if (StatisticsTests.comparableIndependentSamples(new DescriptiveStatistics(referenceUniqueData), new DescriptiveStatistics(testUniqueData))) {
//                    System.out.println("uniqueq data is comparable " + referenceUniqueData.length + " -- " + testUniqueData.length);
//
//                } else if (goodSampleUniqueTestData.length < (uniqueTestData.size() - goodSampleUniqueTestData.length + uniqueReferenceData.size())) {
//                    System.out.println("return -100 ------------1----filterd test " + goodSampleUniqueTestData.length + "  ---full test  " + uniqueTestData.size() + "    uni ref " + uniqueReferenceData.size());
//                    score2 = -100.0;
//                } else {
//                    System.out.println("normal calculation for score 2");
//                }
//
//                if (referenceUniqueData.length == 0) {
////                    System.out.println("shoud never be applied ------------------------>>>>>>>>>>>>>>>>>>>>>");
////                    uniqueReferenceData.addAll(sharedReferenceData);
////                    referenceUniqueData = uniqueReferenceData.stream().mapToDouble(Double::doubleValue).toArray();
//                }
//
//            }
//
//        } else {
//            score1 = SpectraUtilities.performcomparison(referenceSharedData, testSharedData, true);
//        }
//
//        if (potintialFP && score2 == -100.0) {
//        } else if (uniqeToTotal < 0.05 && !StatisticsTests.comparableIndependentSamples(new DescriptiveStatistics(referenceUniqueData), new DescriptiveStatistics(testUniqueData))) {
//            System.out.println("---->> new test used ");
//            score2 = 0;
//        } else {
//            score2 = SpectraUtilities.performcomparison(referenceUniqueData, testUniqueData, false);
//        }
//
//        double totalSampleSize = 2.0 * totalSize;// uniqueReferenceData.size() + uniqueTestData.size() + sharedTestData.size() + sharedReferenceData.size();// toSharedData.length + uniqueReferenceData.size();
//        double ratio1 = (double) (2.0 * sharedReferenceData.size()) / totalSampleSize; //1;//
//        uniqueDataSize = (testUniqueData.length + referenceUniqueData.length);
//        double ratio2 = uniqueDataSize / totalSampleSize;
//        double adjustedScore1 = score1 * ratio1;//* (1.0 - ratio);
//        double adjustedScore2 = score2 * ratio2;//ratio;
//        double finalScore = adjustedScore1 + adjustedScore2;
//        if (Double.isNaN(finalScore)) {
//            System.out.println(" is there Nan? " + finalScore + " = " + adjustedScore1 + " + " + adjustedScore2 + "  " + uniqueTestData.size() + "  " + uniqueReferenceData.size() + "   " + ratio1 + "   " + ratio2 + "  " + referencePsmList.size() + "  " + testPsmList.size());
//
//        }
//        System.out.println("Ptintial FP param --->>>  " + potintialFP + "  final score " + finalScore + " = " + score1 + " + " + score2 + "  ( test uniq size: " + uniqueTestData.size() + " ) ( reference uniq size: " + uniqueReferenceData.size() + " ) ratio 1: " + ratio1 + "  ratio 2:  " + ratio2 + "  ");
//////         
////        }
////        finalScore = logScaleNormalize(finalScore, 2);
//// MainUtilities.finalScoreThrSet.add(finalScore);
//        MainUtilities.addToParamScoreSet(finalScore);
        return yallaScore;

    }

    public static double compareDataScores(List<SpectrumMatch> referencePsmList, List<SpectrumMatch> testPsmList, double totalSize, boolean potintialFP, boolean appliedFDR) {

        if (referencePsmList == null && testPsmList != null) {
            return 100;
        }
        if (referencePsmList != null && testPsmList == null) {
            return -100;
        }
        if (referencePsmList == null && testPsmList == null) {
            return 0;
        }
        Map<String, Double> sharedTestData = new HashMap<>();
        Map<String, Double> sharedReferenceData = new HashMap<>();
        Map<String, Double> uniqueTestData = new HashMap<>();
        Map<String, Double> uniqueReferenceData = new HashMap<>();
        Map<String, Double> testPsmScores = new HashMap<>();
        Map<String, Double> referencePsmScores = new HashMap<>();

        for (SpectrumMatch sm : referencePsmList) {
            referencePsmScores.put(sm.getSpectrumTitle(), sm.getBestPeptideAssumption().getRawScore());
        }
        for (SpectrumMatch sm : testPsmList) {
            testPsmScores.put(sm.getSpectrumTitle(), sm.getBestPeptideAssumption().getRawScore());
        }

        Set<String> combinedSpecHeader = new LinkedHashSet<>(testPsmScores.keySet());
        combinedSpecHeader.addAll(referencePsmScores.keySet());
        for (String spectrumTitle : combinedSpecHeader) {
            if (testPsmScores.containsKey(spectrumTitle) && referencePsmScores.containsKey(spectrumTitle)) {
                sharedTestData.put(spectrumTitle, testPsmScores.get(spectrumTitle));
                sharedReferenceData.put(spectrumTitle, referencePsmScores.get(spectrumTitle));
            } else if (testPsmScores.containsKey(spectrumTitle) && !referencePsmScores.containsKey(spectrumTitle)) {
                uniqueTestData.put(spectrumTitle, testPsmScores.get(spectrumTitle));
            } else {
                uniqueReferenceData.put(spectrumTitle, referencePsmScores.get(spectrumTitle));

            }

        }

        double[] referenceFullData = referencePsmScores.values().stream().mapToDouble(Double::doubleValue).toArray();
        double[] testFullData = testPsmScores.values().stream().mapToDouble(Double::doubleValue).toArray();
        double updatedScore = ScoreComparison.compareReferenceToTest(referenceFullData, testFullData, sharedReferenceData, uniqueReferenceData, sharedTestData, uniqueTestData, potintialFP, appliedFDR);
        return updatedScore;

    }

//    public static double compareDataScores_super(List<SpectrumMatch> referencePsmList, List<SpectrumMatch> testPsmList, double totalSize, boolean potintialFP) {
//
//        if (referencePsmList == null && testPsmList != null) {
//            return 100;
//        }
//        if (referencePsmList != null && testPsmList == null) {
//            return -100;
//        }
//        if (referencePsmList == null && testPsmList == null) {
//            return 0;
//        }
//        List<Double> sharedTestData = new ArrayList<>();
//        List<Double> sharedReferenceData = new ArrayList<>();
//        List<Double> uniqueTestData = new ArrayList<>();
//        List<Double> uniqueReferenceData = new ArrayList<>();
//        Map<String, Double> testPsmScores = new HashMap<>();
//        Map<String, Double> referencePsmScores = new HashMap<>();
//
//        for (SpectrumMatch sm : referencePsmList) {
//            referencePsmScores.put(sm.getSpectrumTitle(), sm.getBestPeptideAssumption().getRawScore());
//        }
//        for (SpectrumMatch sm : testPsmList) {
//            testPsmScores.put(sm.getSpectrumTitle(), sm.getBestPeptideAssumption().getRawScore());
//        }
//
////        Set<String> combinedSpecHeader = new LinkedHashSet<>(testPsmScores.keySet());
////        combinedSpecHeader.addAll(referencePsmScores.keySet());
////        for (String spectrumTitle : combinedSpecHeader) {
////            if (testPsmScores.containsKey(spectrumTitle) && referencePsmScores.containsKey(spectrumTitle)) {
////                sharedTestData.add(testPsmScores.get(spectrumTitle));
////                sharedReferenceData.add(referencePsmScores.get(spectrumTitle));
////            } else if (testPsmScores.containsKey(spectrumTitle) && !referencePsmScores.containsKey(spectrumTitle)) {
////                uniqueTestData.add(testPsmScores.get(spectrumTitle));
////            } else {
////                uniqueReferenceData.add(referencePsmScores.get(spectrumTitle));
////
////            }
////
////        }
////        double uniqeToTotal = ((double) uniqueTestData.size() + (double) uniqueReferenceData.size()) / ((double) referencePsmList.size() + (double) testPsmList.size());
////        double[] referenceSharedData = sharedReferenceData.stream().mapToDouble(Double::doubleValue).toArray();
////        double[] testSharedData = sharedTestData.stream().mapToDouble(Double::doubleValue).toArray();
////        double[] referenceUniqueData = uniqueReferenceData.stream().mapToDouble(Double::doubleValue).toArray();
////        double[] testUniqueData = uniqueTestData.stream().mapToDouble(Double::doubleValue).toArray();
//        double[] referenceFullData = referencePsmScores.values().stream().mapToDouble(Double::doubleValue).toArray();
//        double[] testFullData = testPsmScores.values().stream().mapToDouble(Double::doubleValue).toArray();
//        double updatedScore = ScoreComparison.compareReferenceToTest(referenceFullData, testFullData,potintialFP);
////        double refIndRatio = (double) referenceUniqueData.length / (double) referencePsmList.size();
////        double testIndRatio = (double) testUniqueData.length / (double) testPsmList.size();
////        double pairedRatio = (double) (testSharedData.length * 2.0) / (double) (testPsmList.size() + referencePsmScores.size());
////
////        double score1;
////        double score2 = 0;
////        double updatedScore = 0;
////        if (true) {
////            double zScore1 = StatisticsTests.pairedZTEst(referenceSharedData, testSharedData);
////            double pValue1 = StatisticsTests.calculatePValue(zScore1);
////            if (pValue1 > 0.05) {
////                score1 = 0;
////            } else {
////                score1 = zScore1;
////            }
////            //handel score 2 and 3
////            DescriptiveStatistics dSRef = new DescriptiveStatistics(referenceSharedData);
////            score2 = ScoreComparison.compareIndependentDataEffect(dSRef.getPercentile(50), referenceUniqueData);
////            
////            
////            DescriptiveStatistics dSTest = new DescriptiveStatistics(testSharedData);
////            double score3 = ScoreComparison.compareIndependentDataEffect(dSTest.getPercentile(50), testUniqueData);
////            
//
////            double zScore2 = StatisticsTests.unPairedZTEst(referenceSharedData, referenceUniqueData);
////            double pValue2 = StatisticsTests.calculatePValue(zScore2);
////            DescriptiveStatistics dsp = new DescriptiveStatistics(referenceSharedData);
////            TreeSet<Double> underMin = new TreeSet<>();
////            TreeSet<Double> overMax = new TreeSet<>();
////            TreeSet<Double> withinRnge = new TreeSet<>();
////            for (double d : referenceUniqueData) {
////                if (d < dsp.getMean()) {
////                    underMin.add(d);
////                }
////                if (d > dsp.getMax()) {
////                    overMax.add(d);
////            
////                } else {
////                    withinRnge.add(d);
////                }
////            }
////            System.out.println("under "+underMin.size()+"  within "+withinRnge.size()+"  "+overMax.size());
////
////            System.out.println("improvment percintage 2 zscore " + zScore2 + "  ztestPvalue " + pValue2 + "  size " + referenceSharedData.length + " vs  " + referenceFullData.length);
////            if (pValue2 > 0.05) {
//////                double tScore = TestUtils.tTest(referenceSharedData, referenceFullData);
//////                System.out.println("t stat is "+tScore+"  "+TestUtils.t(referenceSharedData, referenceUniqueData));
////
////                //no diffrent between the paired data
////                score2 = 0;
////            } else {
////                score2 = zScore2;//SpectraUtilities.performcomparison(referenceSharedData, referenceFullData, false);
//////                System.out.println("score 2 = " + score2 + "  zscore " + zScore2);
////            }
////
////            double zScore3 = StatisticsTests.unPairedZTEst(testSharedData, testUniqueData);
////            double pValue3 = StatisticsTests.calculatePValue(zScore3);
//////            System.out.println("improvment percintage 3 zscore " + zScore3 + "  ztestPvalue " + pValue3 + "  size " + testSharedData.length + " vs  " + testFullData.length);
////            double score3 = 0;
////            if (pValue3 > 0.05) {
////                //no diffrent between the paired data
////                score3 = 0;
////            } else {
////                score3 = zScore3;//SpectraUtilities.performcomparison(testSharedData, testFullData, false);
//////                System.out.println("score 3= " + score3 + "  zscore " + zScore3);
////            }
////            System.out.println("score 1= " + score1 + "  score2 " + score2 + "  score3 " + score3);
//////            System.out.println("zscore 1= " + zScore1 + "  zscore2 " + zScore2 + "  zscore3 " + zScore3);
////            System.out.println("ratio 1= " + pairedRatio + "  ratio 2 " + refIndRatio + "  ratio 3 " + testIndRatio);
////            updatedScore = (score1 * pairedRatio) - (score2 * refIndRatio) + (score3 * testIndRatio);
////            if (updatedScore == 0 && referenceFullData.length < testFullData.length) {
////                updatedScore = 1;
////            } else if (updatedScore == 0 && referenceFullData.length > testFullData.length) {
////                updatedScore = -1;
////            }
////            DescriptiveStatistics refDescStat = new DescriptiveStatistics(referenceSharedData);
////            for (double d : uniqueReferenceData) {
////                refDescStat.addValue(d);
////            }
////            double quart1 = refDescStat.getPercentile(25);
////            List<Double> goodSampleUniqueTestDataList = new ArrayList<>();
////            for (double d : uniqueTestData) {
////                if (d > quart1) {
////                    goodSampleUniqueTestDataList.add(d);
////                }
////            }
////            double[] goodSampleUniqueTestData = goodSampleUniqueTestDataList.stream().mapToDouble(Double::doubleValue).toArray();
////            //ttest
////            if (uniqueTestData.size() < 30 && goodSampleUniqueTestData.length > 2) {
//////           measure the low quaity effect on data 
////                if (TestUtils.tTest(testUniqueData, goodSampleUniqueTestData, 0.05)) {
////                    score2 = -100;
////                }
////            } else if (goodSampleUniqueTestData.length > 2) {
////                //ztest
////                double p = StatisticsTests.unpairedZTest(testUniqueData, goodSampleUniqueTestData);
////                if ((p < 0.05)) {
////                    score2 = -100;
////                }
////            } else if (uniqueTestData.size() > 2 && goodSampleUniqueTestData.length <= 2) {
////                score2 = -100;
////            }
////            if (score2 != -100.0) {
////                testUniqueData = goodSampleUniqueTestData;
////                if (StatisticsTests.comparableIndependentSamples(new DescriptiveStatistics(referenceUniqueData), new DescriptiveStatistics(testUniqueData))) {
////                    System.out.println("uniqueq data is comparable " + referenceUniqueData.length + " -- " + testUniqueData.length);
////
////                } else if (goodSampleUniqueTestData.length < (uniqueTestData.size() - goodSampleUniqueTestData.length + uniqueReferenceData.size())) {
////                    System.out.println("return -100 ------------1----filterd test " + goodSampleUniqueTestData.length + "  ---full test  " + uniqueTestData.size() + "    uni ref " + uniqueReferenceData.size());
////                    score2 = -100.0;
////                } else {
////                    System.out.println("normal calculation for score 2");
////                }
////
////                if (referenceUniqueData.length == 0) {
//////                    System.out.println("shoud never be applied ------------------------>>>>>>>>>>>>>>>>>>>>>");
//////                    uniqueReferenceData.addAll(sharedReferenceData);
//////                    referenceUniqueData = uniqueReferenceData.stream().mapToDouble(Double::doubleValue).toArray();
////                }
////
////            }
////
////        }
//////        else {
//////            score1 = SpectraUtilities.performcomparison(referenceSharedData, testSharedData, true);
//////        }
////
////        if (potintialFP && score2 == -100.0) {
////        } else if (uniqeToTotal < 0.05 && !StatisticsTests.comparableIndependentSamples(new DescriptiveStatistics(referenceUniqueData), new DescriptiveStatistics(testUniqueData))) {
////            System.out.println("---->> new test used ");
////            score2 = 0;
////        } else {
////            score2 = SpectraUtilities.performcomparison(referenceUniqueData, testUniqueData, false);
////        }
////
////        double totalSampleSize = 2.0 * totalSize;// uniqueReferenceData.size() + uniqueTestData.size() + sharedTestData.size() + sharedReferenceData.size();// toSharedData.length + uniqueReferenceData.size();
////        double ratio1 = (double) (2.0 * sharedReferenceData.size()) / totalSampleSize; //1;//
////        double uniqueDataSize = (testUniqueData.length + referenceUniqueData.length);
////        double ratio2 = uniqueDataSize / totalSampleSize;
////        double adjustedScore1 = score1 * ratio1;//* (1.0 - ratio);
////        double adjustedScore2 = score2 * ratio2;//ratio;
////        double finalScore = adjustedScore1 + adjustedScore2;
////        if (Double.isNaN(finalScore)) {
////            System.out.println(" is there Nan? " + finalScore + " = " + adjustedScore1 + " + " + adjustedScore2 + "  " + uniqueTestData.size() + "  " + uniqueReferenceData.size() + "   " + ratio1 + "   " + ratio2 + "  " + referencePsmList.size() + "  " + testPsmList.size());
////
////        }
////        System.out.println("Old score comparison  FP param --->>>  " + potintialFP + "  final score " + finalScore + " = " + score1 + " + " + score2 + "  ( test uniq size: " + uniqueTestData.size() + " ) ( reference uniq size: " + uniqueReferenceData.size() + " ) ratio 1: " + ratio1 + "  ratio 2:  " + ratio2 + "  ");
////////         
//////        }
//////        finalScore = logScaleNormalize(finalScore, 2);
////// MainUtilities.finalScoreThrSet.add(finalScore);
////        MainUtilities.addToParamScoreSet(finalScore);
////        double zScore4 = StatisticsTests.unPairedZTEst(referenceFullData, testFullData);
////        double pValue4 = StatisticsTests.calculatePValue(zScore4);
////        double finalScore = compareDataScores_best_backup(referencePsmList, testPsmList, totalSize, potintialFP);
////        if ((finalScore > 0 && updatedScore < 0) || (finalScore < 0 && updatedScore > 0)) {
////            System.out.println("BEst pre score " + finalScore + " vs " + updatedScore + "  " + "  ");
////        }
//
//        return updatedScore;
//
//    }
//    public static double compareDataScores_best_backup(List<SpectrumMatch> referencePsmList, List<SpectrumMatch> testPsmList, double totalSize, boolean potintialFP) {
//
//        if (referencePsmList == null && testPsmList != null) {
//            return 100;
//        }
//        if (referencePsmList != null && testPsmList == null) {
//            return -100;
//        }
//        if (referencePsmList == null && testPsmList == null) {
//            return 0;
//        }
//        List<Double> sharedTestData = new ArrayList<>();
//        List<Double> sharedReferenceData = new ArrayList<>();
//        List<Double> uniqueTestData = new ArrayList<>();
//        List<Double> uniqueReferenceData = new ArrayList<>();
//        Map<String, Double> testPsmScores = new HashMap<>();
//        Map<String, Double> referencePsmScores = new HashMap<>();
//
//        for (SpectrumMatch sm : referencePsmList) {
//            referencePsmScores.put(sm.getSpectrumTitle(), sm.getBestPeptideAssumption().getRawScore());
//        }
//
//        for (SpectrumMatch sm : testPsmList) {
//            testPsmScores.put(sm.getSpectrumTitle(), sm.getBestPeptideAssumption().getRawScore());
//        }
//
//        Set<String> combinedSpecHeader = new LinkedHashSet<>(testPsmScores.keySet());
//        combinedSpecHeader.addAll(referencePsmScores.keySet());
//        for (String spectrumTitle : combinedSpecHeader) {
//            if (testPsmScores.containsKey(spectrumTitle) && referencePsmScores.containsKey(spectrumTitle)) {
//                sharedTestData.add(testPsmScores.get(spectrumTitle));
//                sharedReferenceData.add(referencePsmScores.get(spectrumTitle));
//            } else if (testPsmScores.containsKey(spectrumTitle) && !referencePsmScores.containsKey(spectrumTitle)) {
//                uniqueTestData.add(testPsmScores.get(spectrumTitle));
//            } else {
//                uniqueReferenceData.add(referencePsmScores.get(spectrumTitle));
//
//            }
//
//        }
//        double uniqeToTotal = ((double) uniqueTestData.size() + (double) uniqueReferenceData.size()) / ((double) referencePsmList.size() + (double) testPsmList.size());
//        double[] referenceSharedData = sharedReferenceData.stream().mapToDouble(Double::doubleValue).toArray();
//        double[] testSharedData = sharedTestData.stream().mapToDouble(Double::doubleValue).toArray();
//        double[] referenceUniqueData = uniqueReferenceData.stream().mapToDouble(Double::doubleValue).toArray();
//        double[] testUniqueData = uniqueTestData.stream().mapToDouble(Double::doubleValue).toArray();
//
//        double score1;
//        double score2 = 0;
//        if (potintialFP) {
//            System.out.println("improvment percintage " + "  pv " + "  ztestPvalue " + referenceSharedData.length + "   " + testSharedData.length);
//            double pValueZtest = StatisticsTests.pairedZTest(referenceSharedData, testSharedData);
//            if ((pValueZtest < 0.05) != TestUtils.tTest(referenceSharedData, testSharedData, 0.05)) {
//                System.out.println("improvment percintage " + "  pv " + "  ztestPvalue " + pValueZtest + "  tpvalue " + TestUtils.tTest(referenceSharedData, testSharedData, 0.05) + "   ");
//            }
//            if (!TestUtils.tTest(referenceSharedData, testSharedData, 0.05)) {
//                score1 = 0;
//            } else {
//                score1 = SpectraUtilities.performcomparison(referenceSharedData, testSharedData, true);
//            }
//            DescriptiveStatistics refDescStat = new DescriptiveStatistics(referenceSharedData);
//            for (double d : uniqueReferenceData) {
//                refDescStat.addValue(d);
//            }
//            double quart1 = refDescStat.getPercentile(25);
//            List<Double> goodSampleUniqueTestDataList = new ArrayList<>();
//            for (double d : uniqueTestData) {
//                if (d > quart1) {
//                    goodSampleUniqueTestDataList.add(d);
//                }
//            }
//            double[] goodSampleUniqueTestData = goodSampleUniqueTestDataList.stream().mapToDouble(Double::doubleValue).toArray();
//            //ttest
//
//            if (uniqueTestData.size() < 30 && goodSampleUniqueTestData.length > 2) {
////           measure the low quaity effect on data 
//                if (TestUtils.tTest(testUniqueData, goodSampleUniqueTestData, 0.05)) {
//                    score2 = -100;
//                }
//            } else if (goodSampleUniqueTestData.length > 2) {
//                //ztest
//                double p = StatisticsTests.unpairedZTest(testUniqueData, goodSampleUniqueTestData);
//                if ((p < 0.05)) {
//                    score2 = -100;
//                }
//            } else if (uniqueTestData.size() > 2 && goodSampleUniqueTestData.length <= 2) {
//                score2 = -100;
//            }
//            if (score2 != -100.0) {
//                testUniqueData = goodSampleUniqueTestData;
//                if (StatisticsTests.comparableIndependentSamples(new DescriptiveStatistics(referenceUniqueData), new DescriptiveStatistics(testUniqueData))) {
//                    System.out.println("uniqueq data is comparable " + referenceUniqueData.length + " -- " + testUniqueData.length);
//
//                } else if (goodSampleUniqueTestData.length < (uniqueTestData.size() - goodSampleUniqueTestData.length + uniqueReferenceData.size())) {
//                    System.out.println("return -100 ------------1----filterd test " + goodSampleUniqueTestData.length + "  ---full test  " + uniqueTestData.size() + "    uni ref " + uniqueReferenceData.size());
//                    score2 = -100.0;
//                } else {
//                    System.out.println("normal calculation for score 2");
//                }
//
//                if (referenceUniqueData.length == 0) {
////                    System.out.println("shoud never be applied ------------------------>>>>>>>>>>>>>>>>>>>>>");
////                    uniqueReferenceData.addAll(sharedReferenceData);
////                    referenceUniqueData = uniqueReferenceData.stream().mapToDouble(Double::doubleValue).toArray();
//                }
//
//            }
//
//        } else {
//            score1 = SpectraUtilities.performcomparison(referenceSharedData, testSharedData, true);
//        }
//
//        if (potintialFP && score2 == -100.0) {
//        } else if (uniqeToTotal < 0.05 && !StatisticsTests.comparableIndependentSamples(new DescriptiveStatistics(referenceUniqueData), new DescriptiveStatistics(testUniqueData))) {
//            System.out.println("---->> new test used ");
//            score2 = 0;
//        } else {
//            score2 = SpectraUtilities.performcomparison(referenceUniqueData, testUniqueData, false);
//        }
//
//        double totalSampleSize = 2.0 * totalSize;// uniqueReferenceData.size() + uniqueTestData.size() + sharedTestData.size() + sharedReferenceData.size();// toSharedData.length + uniqueReferenceData.size();
//        double ratio1 = (double) (2.0 * sharedReferenceData.size()) / totalSampleSize; //1;//
//        double uniqueDataSize = (testUniqueData.length + referenceUniqueData.length);
//        double ratio2 = uniqueDataSize / totalSampleSize;
//        double adjustedScore1 = score1 * ratio1;//* (1.0 - ratio);
//        double adjustedScore2 = score2 * ratio2;//ratio;
//        double finalScore = adjustedScore1 + adjustedScore2;
//        if (Double.isNaN(finalScore)) {
//            System.out.println(" is there Nan? " + finalScore + " = " + adjustedScore1 + " + " + adjustedScore2 + "  " + uniqueTestData.size() + "  " + uniqueReferenceData.size() + "   " + ratio1 + "   " + ratio2 + "  " + referencePsmList.size() + "  " + testPsmList.size());
//
//        }
//        System.out.println("Old score comparison  FP param --->>>  " + potintialFP + "  final score " + finalScore + " = " + score1 + " + " + score2 + "  ( test uniq size: " + uniqueTestData.size() + " ) ( reference uniq size: " + uniqueReferenceData.size() + " ) ratio 1: " + ratio1 + "  ratio 2:  " + ratio2 + "  ");
//////         
////        }
////        finalScore = logScaleNormalize(finalScore, 2);
//// MainUtilities.finalScoreThrSet.add(finalScore);
//        MainUtilities.addToParamScoreSet(finalScore);
//
//        return finalScore;
//
//    }
//
//    public static double compareDataScores_t(List<SpectrumMatch> referencePsmList, List<SpectrumMatch> testPsmList, double totalSize, boolean potintialFP) {
//
//        if (referencePsmList == null && testPsmList != null) {
//            return 100;
//        }
//        if (referencePsmList != null && testPsmList == null) {
//            return -100;
//        }
//        if (referencePsmList == null && testPsmList == null) {
//            return 0;
//        }
//        List<Double> sharedTestData = new ArrayList<>();
//        List<Double> sharedReferenceData = new ArrayList<>();
//        List<Double> uniqueTestData = new ArrayList<>();
//        List<Double> uniqueReferenceData = new ArrayList<>();
//        Map<String, Double> testPsmScores = new HashMap<>();
//        Map<String, Double> referencePsmScores = new HashMap<>();
//
//        for (SpectrumMatch sm : referencePsmList) {
//            referencePsmScores.put(sm.getSpectrumTitle(), sm.getBestPeptideAssumption().getRawScore());
//        }
//
//        for (SpectrumMatch sm : testPsmList) {
//            testPsmScores.put(sm.getSpectrumTitle(), sm.getBestPeptideAssumption().getRawScore());
//        }
//
//        Set<String> combinedSpecHeader = new LinkedHashSet<>(testPsmScores.keySet());
//        combinedSpecHeader.addAll(referencePsmScores.keySet());
//        for (String spectrumTitle : combinedSpecHeader) {
//            if (testPsmScores.containsKey(spectrumTitle) && referencePsmScores.containsKey(spectrumTitle)) {
//                sharedTestData.add(testPsmScores.get(spectrumTitle));
//                sharedReferenceData.add(referencePsmScores.get(spectrumTitle));
//            } else if (testPsmScores.containsKey(spectrumTitle) && !referencePsmScores.containsKey(spectrumTitle)) {
//                uniqueTestData.add(testPsmScores.get(spectrumTitle));
//            } else {
//                uniqueReferenceData.add(referencePsmScores.get(spectrumTitle));
//
//            }
//
//        }
//        double uniqeToTotal = ((double) uniqueTestData.size() + (double) uniqueReferenceData.size()) / ((double) referencePsmList.size() + (double) testPsmList.size());
//        double[] referenceSharedData = sharedReferenceData.stream().mapToDouble(Double::doubleValue).toArray();
//        double[] testSharedData = sharedTestData.stream().mapToDouble(Double::doubleValue).toArray();
//        double[] referenceUniqueData = uniqueReferenceData.stream().mapToDouble(Double::doubleValue).toArray();
//        double[] testUniqueData = uniqueTestData.stream().mapToDouble(Double::doubleValue).toArray();
//        double[] referenceFullData = referencePsmScores.values().stream().mapToDouble(Double::doubleValue).toArray();
//        double[] testFullData = testPsmScores.values().stream().mapToDouble(Double::doubleValue).toArray();
//
//        double refIndRatio = (double) referenceUniqueData.length / (double) referencePsmList.size();
//        double testIndRatio = (double) testUniqueData.length / (double) testPsmList.size();
//        double pairedRatio = (double) (testSharedData.length + referenceSharedData.length) / (double) (testPsmList.size() + referencePsmScores.size());
//
//        double pairedDataScore = SpectraUtilities.performcomparison(referenceSharedData, testSharedData, true) * pairedRatio;
//
//        //pair data 
//        if (referenceSharedData.length > 0) {
//
//            DescriptiveStatistics referenceData = new DescriptiveStatistics(referenceSharedData);
//            DescriptiveStatistics improvedData = new DescriptiveStatistics(testSharedData);
//            double skw = improvedData.getSkewness() - referenceData.getSkewness();
//            double cohnD = StatisticsTests.calculateCohensD(referenceData.getMean(), improvedData.getMean(), referenceData.getStandardDeviation(), improvedData.getStandardDeviation());
//
//            // pairedDataScore + testIndEffectScore + referenceIndEffectScore;
//            pairedDataScore = cohnD * pairedRatio;
//            System.out.println("1. comparison over all score " + pairedDataScore + "    skw " + skw + " cohnD " + cohnD + "  ");
//
//        }
//
////
//        double referenceIndEffectScore = 0;
//        double testIndEffectScore = 0;
//        if (referenceUniqueData.length > 0) {
//            DescriptiveStatistics referenceData = new DescriptiveStatistics(referenceSharedData);
//            DescriptiveStatistics improvedData = new DescriptiveStatistics(referenceFullData);
//            double skw = improvedData.getSkewness() - referenceData.getSkewness();
//            double cohnD = StatisticsTests.calculateCohensD(referenceData.getMean(), improvedData.getMean(), referenceData.getStandardDeviation(), improvedData.getStandardDeviation());
//            // pairedDataScore + testIndEffectScore + referenceIndEffectScore;
//            referenceIndEffectScore = cohnD * refIndRatio;//SpectraUtilities.performcomparison(referenceSharedData, referenceFullData, false) * -1.0 * refIndRatio;
//            System.out.println("2. comparison over all score " + referenceIndEffectScore + "    skw " + skw + " cohnD " + cohnD + "  ");
//        }
//        if (testSharedData.length > 0 && testSharedData.length > 0) {
//            DescriptiveStatistics referenceData = new DescriptiveStatistics(testSharedData);
//            DescriptiveStatistics improvedData = new DescriptiveStatistics(testFullData);
//            double skw = improvedData.getSkewness() - referenceData.getSkewness();
//            double cohnD = StatisticsTests.calculateCohensD(referenceData.getMean(), improvedData.getMean(), referenceData.getStandardDeviation(), improvedData.getStandardDeviation());
//            // pairedDataScore + testIndEffectScore + referenceIndEffectScore;
//
//            testIndEffectScore = cohnD * testIndRatio;
//            System.out.println("3. comparison over all score " + testIndEffectScore + "    skw " + skw + " cohnD " + cohnD + "  ");
////            testIndEffectScore = SpectraUtilities.performcomparison(testSharedData, testFullData, false) * testIndRatio;
//        }
//        DescriptiveStatistics referenceData = new DescriptiveStatistics(referenceFullData);
//        DescriptiveStatistics improvedData = new DescriptiveStatistics(testFullData);
//        double overallScore = pairedDataScore - testIndEffectScore + referenceIndEffectScore;
//        System.out.println("comparison over all score " + overallScore);
//        System.out.println("--------------------------------------------------");
//
//        double totalSampleSize = 2.0 * totalSize;// uniqueReferenceData.size() + uniqueTestData.size() + sharedTestData.size() + sharedReferenceData.size();// toSharedData.length + uniqueReferenceData.size();
//        double ratio = (double) (testPsmList.size() + referencePsmScores.size()) / totalSampleSize; //1;//
//
//        double ratio1 = (double) (2.0 * sharedReferenceData.size()) / totalSampleSize; //1;//
//
//        double uniqueDataSize = (testUniqueData.length + referenceUniqueData.length);
//        double ratio2 = uniqueDataSize / totalSampleSize;
////        double adjustedScore1 = pairedDataScore * ratio1;//* (1.0 - ratio);
////        double adjustedScore2 = referenceIndEffectScore * ratio2;//ratio;
//        double finalScore = overallScore * ratio;
//        if (Double.isNaN(finalScore)) {
////            System.out.println(" is there Nan? " + finalScore + " = " + adjustedScore1 + " + " + adjustedScore2 + "  " + uniqueTestData.size() + "  " + uniqueReferenceData.size() + "   " + ratio1 + "   " + ratio2 + "  " + referencePsmList.size() + "  " + testPsmList.size());
//
//        }
////        System.out.println("Old score comparison  FP param --->>>  " + potintialFP + "  final score " + finalScore + " = " + pairedDataScore + " + " + referenceIndEffectScore + " test "+testIndEffectScore );
//////         
////        }
////        finalScore = logScaleNormalize(finalScore, 2);
//// MainUtilities.finalScoreThrSet.add(finalScore);
//        MainUtilities.addToParamScoreSet(finalScore);
//
//        return finalScore;
//
//    }
//    public static double compareDataScores1(List<SpectrumMatch> referencePsmList, List<SpectrumMatch> testPsmList, double totalSize, boolean potintialFP) {
//
//        if (referencePsmList == null && testPsmList != null) {
//            return 100;
//        }
//        if (referencePsmList != null && testPsmList == null) {
//            return -100;
//        }
//        if (referencePsmList == null && testPsmList == null) {
//            return 0;
//        }
//        List<Double> sharedTestData = new ArrayList<>();
//        List<Double> sharedReferenceData = new ArrayList<>();
//        List<Double> uniqueTestData = new ArrayList<>();
//        List<Double> uniqueReferenceData = new ArrayList<>();
//        Map<String, Double> testPsmScores = new HashMap<>();
//        Map<String, Double> referencePsmScores = new HashMap<>();
//
//        for (SpectrumMatch sm : referencePsmList) {
//            referencePsmScores.put(sm.getSpectrumTitle(), sm.getBestPeptideAssumption().getRawScore());
//        }
//
//        for (SpectrumMatch sm : testPsmList) {
//            testPsmScores.put(sm.getSpectrumTitle(), sm.getBestPeptideAssumption().getRawScore());
//        }
//
//        Set<String> combinedSpecHeader = new LinkedHashSet<>(testPsmScores.keySet());
//        combinedSpecHeader.addAll(referencePsmScores.keySet());
//
//        for (String spectrumTitle : combinedSpecHeader) {
//            if (testPsmScores.containsKey(spectrumTitle) && referencePsmScores.containsKey(spectrumTitle)) {
//                sharedTestData.add(testPsmScores.get(spectrumTitle));
//                sharedReferenceData.add(referencePsmScores.get(spectrumTitle));
//            } else if (testPsmScores.containsKey(spectrumTitle) && !referencePsmScores.containsKey(spectrumTitle)) {
//                uniqueTestData.add(testPsmScores.get(spectrumTitle));
//            } else {
//                uniqueReferenceData.add(referencePsmScores.get(spectrumTitle));
//
//            }
//
//        }
//        double[] referenceSharedData = sharedReferenceData.stream().mapToDouble(Double::doubleValue).toArray();
//        double[] testSharedData = sharedTestData.stream().mapToDouble(Double::doubleValue).toArray();
//        double[] referenceUniqueData = uniqueReferenceData.stream().mapToDouble(Double::doubleValue).toArray();
//        System.out.println("Potintial False postive parameters " + potintialFP);
//
//        //tested paired is totally fine
//        //
//        System.out.println("----------------start-------------");
//        for (double d : referenceUniqueData) {
//            System.out.print(d + " , ");
//        }
//        System.out.println("");
//        System.out.println("-----------------------------");
//        for (double d : uniqueTestData) {
//            System.out.print(d + " , ");
//        }
//        System.out.println("");
//        System.out.println("----------------end-------------");
//        if (potintialFP) {//(referenceUniqueData.length <10 && toOnlyData.length > 10 && potintialFP) {
//
//            //if the reference unique < test nique
//            TreeSet<Double> mergeRefScore = new TreeSet<>(uniqueReferenceData);
//            mergeRefScore.addAll(sharedReferenceData);
//            double median = mergeRefScore.first();
//            List<Double> filteredUniqueTestData = new ArrayList<>();
//            for (double d : uniqueTestData) {
//                if (d > median) {
//                    filteredUniqueTestData.add(d);
//                }
//            }
////            double oAll = (double) filteredUniqueTestData.size() / ((double) uniqueTestData.size() + uniqueReferenceData.size());
////            oAll *= 100.0;
////            System.out.println("overall effect " + oAll + "   " + filteredUniqueTestData.size() + "   " + (uniqueTestData.size() - filteredUniqueTestData.size()));
//            if (filteredUniqueTestData.size() > (uniqueTestData.size() - filteredUniqueTestData.size() + uniqueReferenceData.size())) {
//                return -1;
//            }
//            if (referenceUniqueData.length == 0) {
//                uniqueReferenceData.addAll(sharedReferenceData);
//                referenceUniqueData = uniqueReferenceData.stream().mapToDouble(Double::doubleValue).toArray();
//            }
//            uniqueTestData.clear();
//            uniqueTestData.addAll(filteredUniqueTestData);
//        }
//
//        double[] testUniqueData = uniqueTestData.stream().mapToDouble(Double::doubleValue).toArray();
//
//        double score1 = SpectraUtilities.performcomparison(referenceSharedData, testSharedData, true);
//        double score2 = SpectraUtilities.performcomparison(referenceUniqueData, testUniqueData, false);
//
//        double totalSampleSize = 2.0 * totalSize;// uniqueReferenceData.size() + uniqueTestData.size() + sharedTestData.size() + sharedReferenceData.size();// toSharedData.length + uniqueReferenceData.size();
//        double ratio1 = (double) (2.0 * sharedReferenceData.size()) / totalSampleSize; //1;//
//        double uniqueSize = (testUniqueData.length + referenceUniqueData.length);
//        double ratio2 = uniqueSize / totalSampleSize;
//        double adjustedScore1 = score1 * ratio1;//* (1.0 - ratio);
//        double adjustedScore2 = score2 * ratio2;//ratio;
//        double finalScore = adjustedScore1 + adjustedScore2;
//        if (Double.isNaN(finalScore)) {
//            System.out.println(" is there Nan? " + finalScore + " = " + adjustedScore1 + " + " + adjustedScore2 + "  " + uniqueTestData.size() + "  " + uniqueReferenceData.size() + "   " + ratio1 + "   " + ratio2 + "  " + referencePsmList.size() + "  " + testPsmList.size());
////             System.exit(0);
//        }
//        System.out.println("paired is " + referenceSharedData.length + " ( " + score1 + ")   lost " + referenceUniqueData.length + "  gein " + uniqueTestData.size() + " score 2 " + (score2) + " <<<<" + finalScore + ">>>>");
//
////        if (potintialFP) {
////            System.out.println("at uniqueTestData.size() " + uniqueTestData.size() + "   uniqueReferenceData.size() " + uniqueReferenceData.size() + "  sharedReferenceData " + sharedReferenceData.size() + "  thr ");
////        System.out.println("Ptintial FP param --->>>  " + finalScore + " = " + adjustedScore1 + " + " + adjustedScore2 + "  " + uniqueTestData.size() + "  " + uniqueReferenceData.size() + "   " + ratio1 + "   " + ratio2 + "  " + referencePsmList.size() + "  " + testPsmList.size());
//////         
////        }
////        finalScore = logScaleNormalize(finalScore, 2);
//// MainUtilities.finalScoreThrSet.add(finalScore);
//        MainUtilities.addToParamScoreSet(finalScore);
//
//        return finalScore;
//
//    }
    public static List<Double> getTagSectionRatios(String[] titiles, List<SpectrumMatch> matches) {

        double partCount = SpectraUtilities.getSpectraSectionsNumber(titiles.length);
        System.out.println("suggestedTag section number " + partCount);

        Map<String, Double> fullSpectraMap = new LinkedHashMap<>();
        for (String titel : titiles) {
            fullSpectraMap.put(titel, 100.0);
        }
        for (SpectrumMatch sm : matches) {
            TagAssumption tag = sm.getAllTagAssumptions().toList().get(0);
            fullSpectraMap.replace(sm.getSpectrumTitle(), tag.getScore());
        }
        double countId = 0;
        double countUnId = 0;
        double mainSectionsSize = Math.round((double) titiles.length / partCount);
        double lastSectionsSize = (double) titiles.length - (mainSectionsSize * (partCount - 1));

        double[][] quartileData = new double[(int) partCount][2];
        int counter = 0;
        int quartileIndex = 0;
        for (String titile : titiles) {
            double v = fullSpectraMap.get(titile);
            if (v <= 0.01) {
                countId++;
            } else {
                countUnId++;
            }
            counter++;
            if (counter == mainSectionsSize) {
                //initpart I 
                quartileData[quartileIndex++] = new double[]{countId, countUnId};
                counter = 0;
                countId = 0;
                countUnId = 0;
                if (quartileIndex == partCount - 1) {
                    mainSectionsSize = lastSectionsSize;
                }

            }
        }

        List<Double> quartileRatios = new ArrayList<>();
        for (double[] qData : quartileData) {
            double qRatio = qData[0] / (qData[0] + qData[1]);
            quartileRatios.add(qRatio);
        }
        return quartileRatios;
    }

    public static List<SpectrumMatch> getValidatedIdentificationResults(File resultOutput, File msFile, Advocate searchEngine, IdentificationParameters identificationParameters) {
        List<SpectrumMatch> validatedMaches = Collections.synchronizedList(new ArrayList<>());

        if (resultOutput == null) {
            System.out.println("output result file was null");
            return validatedMaches;
        }
        try {
            boolean enableEScore = false;
            boolean enableFDRCalculation = false;
            File idResultsFile = null;
            if (searchEngine.getIndex() == Advocate.myriMatch.getIndex()) {
                MyriMatchParameters myriMatchParameters = (MyriMatchParameters) identificationParameters.getSearchParameters().getAlgorithmSpecificParameters().get(Advocate.myriMatch.getIndex());
                idResultsFile = new File(resultOutput, SearchHandler.getMyriMatchFileName(IoUtil.removeExtension(msFile.getName()), myriMatchParameters));
            } else if (searchEngine.getIndex() == Advocate.xtandem.getIndex()) {
                idResultsFile = new File(resultOutput, SearchHandler.getXTandemFileName(IoUtil.removeExtension(msFile.getName())));
                enableEScore = true;
            } else if (searchEngine.getIndex() == Advocate.sage.getIndex()) {
                idResultsFile = new File(resultOutput, (IoUtil.removeExtension(msFile.getName()) + ".sage.tsv"));
                enableFDRCalculation = true;
            }
            if (idResultsFile == null || !idResultsFile.exists()) {
                System.out.println("id result file was null " + resultOutput.getAbsolutePath() + "   ---  " + searchEngine.getName() + "  " + idResultsFile);
                System.exit(0);
                return validatedMaches;
            }
            IdfileReader idReader = IdfileReaderFactory.getInstance().getFileReader(idResultsFile);
            MsFileHandler subMsFileHandler = new MsFileHandler();
            subMsFileHandler.register(msFile, MainUtilities.OptProt_Waiting_Handler);
            ArrayList<SpectrumMatch> matches = idReader.getAllSpectrumMatches(subMsFileHandler, MainUtilities.OptProt_Waiting_Handler, identificationParameters.getSearchParameters());

            List<PSM> targetDecoyMaches = Collections.synchronizedList(new ArrayList<>());
//            System.out.println("maches size "+matches.size());
            for (SpectrumMatch sm : matches) {
                for (PeptideAssumption peptideAssumtion : sm.getAllPeptideAssumptions().toList()) {
                    if (peptideAssumtion.getRank() != 1) {
                        continue;
                    }
                    sm.setBestPeptideAssumption(peptideAssumtion);
//                    if (peptideAssumtion.getRawScore() > 0) {
//                        testData.addValue(peptideAssumtion.getRawScore());
//                        if (enableEScore && peptideAssumtion.getScore() <= 0.05) {
//                            if (peptideAssumtion.getScore() < 0) {
//                                decoyCounter++;
//                            }
//
//                            validatedMaches.add(sm);
//                        } else if (!enableEScore) {
//                            validatedMaches.add(sm);
//                            targetDecoyMaches.add(new PSM(peptideAssumtion.getRawScore(), true));
//                        }
//                    } 
                    if (peptideAssumtion.getRawScore() > 0) {
                        if (enableEScore) {
                            targetDecoyMaches.add(new PSM(peptideAssumtion.getRawScore(), (peptideAssumtion.getScore() > 0.0)));
                            validatedMaches.add(sm);
                        } else {
                            validatedMaches.add(sm);
                            targetDecoyMaches.add(new PSM(peptideAssumtion.getRawScore(), true));
                        }
                    } else {

                        targetDecoyMaches.add(new PSM(peptideAssumtion.getRawScore() * -1.0, false));
                    }
                    break;
                }
            }
            if (true) {//!targetDecoyMaches.isEmpty() && enableFDRCalculation) {
                double thr = StatisticsTests.findThresholdForFDR(targetDecoyMaches, 0.01);
//                System.out.println("thr is " + thr + "   " + validatedMaches.size());
                if (thr > 0) {
                    List<SpectrumMatch> uvalidatedMaches = Collections.synchronizedList(new ArrayList<>(validatedMaches));
                    validatedMaches.clear();
                    for (SpectrumMatch sm : uvalidatedMaches) {
                        if (sm.getBestPeptideAssumption().getRawScore() >= thr) {
                            validatedMaches.add(sm);
                        }

                    }
                }
//                System.out.println("thr is  after " + thr + "   " + validatedMaches.size());
            }

        } catch (IOException | SQLException | ClassNotFoundException | InterruptedException | JAXBException | XmlPullParserException | XMLStreamException ex) {
            System.out.println("no.uib.probe.optprot.util.SpectraFileUtilities.getValidatedIdentificationResults() " + ex.getLocalizedMessage());
            ex.printStackTrace();
        }
        return validatedMaches;
    }

    public static Set<String> getModifiedSpectrumSet(List<SpectrumMatch> spectraResults) {
//        System.out.println("mod id is ---------- "+mod);
        Set<String> modSpectrumSet = new HashSet<>();
        for (SpectrumMatch sm : spectraResults) {
            if (sm == null) {
                continue;
            }
            PeptideAssumption peptideAssm = sm.getBestPeptideAssumption();
            if (peptideAssm.getPeptide().getVariableModifications().length > 0) {
//                for (ModificationMatch mm : peptideAssm.getPeptide().getVariableModifications()) {
//                    if (mm.getModification().endsWith(mod.getPattern().toString()) && Precision.equals(mod.getMass(), Double.parseDouble(mm.getModification().split("@")[0]), 0.01)) {
                modSpectrumSet.add(sm.getSpectrumTitle());

//                    }
//                }
            }
        }

        return modSpectrumSet;

    }

    public static Set<String> getIntersectionSet(Set<String> setI, Set<String> setII) {
        Set<String> intersection = new HashSet<>();
        Set<String> full = new HashSet<>(setI);
        full.addAll(setII);
        for (String title : full) {
            if (setI.contains(title) && setII.contains(title)) {
                intersection.add(title);
            }
        }

        return intersection;

    }

    public static String compareScoresSet(Map<String, RawScoreModel> resultsMap, double fullDataSize, boolean PFP, boolean applyFDR) {
        System.out.println("compare score set " + resultsMap.keySet());

        List<RawScoreModel> scoreModelSorter = new ArrayList<>();

        for (String rs1Key : resultsMap.keySet()) {
            RawScoreModel rscoreModel = resultsMap.get(rs1Key);
            rscoreModel.setPotintialFalsePostive(PFP);
            scoreModelSorter.add(rscoreModel);
        }
        Collections.sort(scoreModelSorter);
        if (!PFP) {
            Collections.reverse(scoreModelSorter);
        }
        int index1 = -1;
        List<String> topScoreSet = new ArrayList<>(resultsMap.keySet());
        for (RawScoreModel score1 : scoreModelSorter) {
            String testKey = score1.getParameterId();
            index1++;
            if (!topScoreSet.contains(testKey)) {
                continue;
            }
            int index2 = -1;
            for (RawScoreModel score2 : scoreModelSorter) {
                String referenceKey = score2.getParameterId();
                index2++;
                if (index2 <= index1 || !topScoreSet.contains(referenceKey) || referenceKey.equals(testKey)) {
                    continue;
                }
                double comparisonScore = compareDataScores(resultsMap.get(referenceKey).getSpectrumMatchResult(), resultsMap.get(testKey).getSpectrumMatchResult(), fullDataSize, PFP, applyFDR);
                System.out.println(testKey + " better " + referenceKey + "  " + comparisonScore + "  roundComparisonScore" + comparisonScore);
                if (comparisonScore == -1) {
                } else if ((comparisonScore > 0) || Double.isNaN(comparisonScore)) {// && (resultsMap.get(rs1Key).getFinalScore() > resultsMap.get(rs2Key).getFinalScore())
                    topScoreSet.remove(referenceKey);
                    System.out.println("-----------------------------------remove 2" + referenceKey + "-------------------------------" + comparisonScore + "   " + (comparisonScore < -0.05));
                } else if (comparisonScore < 0) {
                    System.out.println("-----------------------------------remove1 " + testKey + "-------------------------------" + comparisonScore + "   " + (comparisonScore < -0.05));
                    topScoreSet.remove(testKey);
                    break;
                } else {
                    System.out.println(PFP + "-- comparison score was zero --> " + testKey + "  " + resultsMap.get(testKey) + "   " + referenceKey + "  " + resultsMap.get(referenceKey));
                    if (PFP) {
                        double ref = Double.parseDouble(referenceKey);
                        double tes = Double.parseDouble(testKey);
                        if (ref > tes) {
                            topScoreSet.remove(referenceKey);
                        } else {
                            topScoreSet.remove(testKey);
                        }
                        System.out.println("ref is " + ref + "  " + tes + "  " + ((ref > tes)) + "  " + topScoreSet);
//                        if (score2.getRawFinalScore() > score1.getRawFinalScore()) {
//                            topScoreSet.remove(testKey);
//                            break;
//                        } else if (score2.getRawFinalScore() < score1.getRawFinalScore()) {
//                            topScoreSet.remove(referenceKey);
//                        } else if (score2.getIdPSMNumber() > score1.getIdPSMNumber()) {
//                            topScoreSet.remove(testKey);
//                            break;
//
//                        } else if (score2.getIdPSMNumber() < score1.getIdPSMNumber()) {
//                            topScoreSet.remove(referenceKey);
//                            break;
//
//                        }

                    } else {
                        if (score2.getIdPSMNumber() > score1.getIdPSMNumber()) {
                            topScoreSet.remove(testKey);
                            break;

                        } else if (score2.getIdPSMNumber() < score1.getIdPSMNumber()) {
                            topScoreSet.remove(referenceKey);
                            break;

                        } else if (score2.getRawFinalScore() > score1.getRawFinalScore()) {
                            topScoreSet.remove(testKey);
                            break;
                        } else if (score2.getRawFinalScore() < score1.getRawFinalScore()) {
                            topScoreSet.remove(referenceKey);
                            break;
                        }
//                        else if (score2.getS1() < score1.getS1()) {
//                            topScoreSet.remove(referenceKey);
//                        } else if (score2.getS1() > score1.getS1()) {
//                            topScoreSet.remove(testKey);
//                            break;
//
//                        }
                    }
//                    else {
//                        int indexI = topScoreSet.indexOf(rs1Key);
//                        int indexII = topScoreSet.indexOf(rs2Key);
//                        int toRemove = Math.max(indexI, indexII);
//                        
//                        topScoreSet.remove(toRemove);
//                        System.out.println("2<<<>>>>comparison was even what should we do with " + rs1Key + " and " + rs2Key + "  " + topScoreSet);
//                    }

                }
            }
        }
        String topSelection = topScoreSet.get(0);
        System.out.println("top selection is " + topSelection + "   " + topScoreSet);

        if (topScoreSet.size() > 1) {
            System.out.println("§§§§ error should be only one value " + topScoreSet);
            List<String> adjustedScore = new ArrayList<>(topScoreSet);

            for (int i = 0; i < topScoreSet.size(); i++) {
                RawScoreModel scoreI = resultsMap.get(topScoreSet.get(i));
                for (int j = 0; j < topScoreSet.size(); j++) {
                    if (i >= j) {
                        continue;
                    }
                    RawScoreModel scoreII = resultsMap.get(topScoreSet.get(j));                 
                    if (scoreI.getDiffrentInSize() < scoreII.getDiffrentInSize()) {
                        adjustedScore.remove(topScoreSet.get(j));
                    } else {
                        adjustedScore.remove(topScoreSet.get(i));
                    }
                }
            }
            System.out.println("final adjust score " + adjustedScore);

//            for (String topScore : topScoreSet) {
//                RawScoreModel score1 = resultsMap.get(topScore);
//                double scoreRatioI = Math.max(score1.getFinalScore(),1) / score1.getIdPSMNumber();
//                
//                adjustedScoreMap.put(topScore, scoreRatioI);
//            }
//            for (String topScore : topScoreSet) {
//                for (String topScore2 : topScoreSet) {
//                    if (topScore.equalsIgnoreCase(topScore2) || !adjustedScoreMap.containsKey(topScore) || !adjustedScoreMap.containsKey(topScore2)) {
//                        continue;
//                    }
//                    if (adjustedScoreMap.get(topScore) > adjustedScoreMap.get(topScore2)) {
//                        adjustedScoreMap.remove(topScore2);
//                    }
//
//                }
//            }

            topSelection = adjustedScore.get(0);
        } else if (topScoreSet.isEmpty()) {
            System.out.println("2error should be only one value " + topScoreSet);
            return "";
        }

//        if (!scoreModelSorter.get(0).getParameterId().equalsIgnoreCase(topSelection) && resultsMap.get(topSelection) != null && resultsMap.get(scoreModelSorter.get(0).getParameterId()) != null) {
//            System.out.println("-----------------1--------------->> disagree with both methods " + scoreModelSorter.get(0).getParameterId() + "  " + topSelection);
//            double comparisonScore = compareDataScores(resultsMap.get(topSelection).getSpectrumMatchResult(), resultsMap.get(scoreModelSorter.get(0).getParameterId()).getSpectrumMatchResult(), fullDataSize, PFP, applyFDR);
//            if (comparisonScore > 0 || Double.isNaN(comparisonScore)) {
//                topSelection = scoreModelSorter.get(0).getParameterId();
//            }
//        }
        return topSelection;
    }

//    public static String compareScoresSet(Map<String, RawScoreModel> resultsMap, int totalSpecNumber, boolean ignorS2) {
//
//        List<RawScoreModel> scoreModelSorter = new ArrayList<>();
//
//        for (String rs1Key : resultsMap.keySet()) {
////            sorter.put(resultsMap.get(rs1Key), rs1Key);
//            scoreModelSorter.add(resultsMap.get(rs1Key));
//        }
//        Collections.sort(scoreModelSorter);
//        Collections.reverse(scoreModelSorter);
////        return sorter.lastEntry().getValue();
//        int index1 = -1;
//        List<String> topScoreSet = new ArrayList<>(resultsMap.keySet());
//        for (RawScoreModel score1 : scoreModelSorter) {
//            String rs1Key = score1.getComparisonId();
//            index1++;
//            if (!topScoreSet.contains(rs1Key)) {
//                continue;
//            }
//            int index2 = -1;
//            for (RawScoreModel score2 : scoreModelSorter) {
//                String rs2Key = score2.getComparisonId();
//                index2++;
//                if (index2 <= index1 || !topScoreSet.contains(rs2Key)) {
//                    continue;
//                }
//                double key1Better = isBetterScore(resultsMap.get(rs2Key).getSpectrumMatchResult(), resultsMap.get(rs1Key).getSpectrumMatchResult());
////                System.out.println("resultsMap.get(rs1Key) " + resultsMap.get(rs1Key).getFinalScore() + "   " + resultsMap.get(rs2Key).getFinalScore());
////                System.out.println(rs1Key + " better " + rs2Key + "  " + key1Better);
//                if ((key1Better > 0) || Double.isNaN(key1Better)) {// && (resultsMap.get(rs1Key).getFinalScore() > resultsMap.get(rs2Key).getFinalScore())
//                    topScoreSet.remove(rs2Key);
//                } else {
//                    System.out.println("-----------------------------------remove " + rs1Key + "-------------------------------");
//                    topScoreSet.remove(rs1Key);
//                    break;
//                }
//            }
//        }
//        if (topScoreSet.size() > 1) {
//            System.out.println("error should be only oine value " + topScoreSet);
//        } else if (topScoreSet.isEmpty()) {
//            System.out.println("error should be only oine value " + topScoreSet);
//            return "";
//        }
//        String topSelection = topScoreSet.get(0);
//        if (!scoreModelSorter.get(0).getComparisonId().equalsIgnoreCase(topSelection)) {
//            System.out.println("---------------2----------------->> disagree with both methods " + scoreModelSorter.get(0).getComparisonId() + "  " + topSelection);
//        }
//        return topSelection;
//    }
    public static String getTopScoresSet(Map<String, RawScoreModel> resultsMap, Set<String> refMatches, double fullDataSize, boolean applyFDR) {
//        TreeMap<RawScoreModel, String> sorter = new TreeMap<>(Collections.reverseOrder());
        List<RawScoreModel> scoreModelSorter = new ArrayList<>();

        for (String rs1Key : resultsMap.keySet()) {
            scoreModelSorter.add(resultsMap.get(rs1Key));
            List<SpectrumMatch> updatedAddOnly = SpectraUtilities.getModificationFrequentScore(rs1Key, resultsMap.get(rs1Key).getSpecTitles(), refMatches, resultsMap.get(rs1Key).getSpectrumMatchResult());
            resultsMap.get(rs1Key).setSpectrumMatchResult(updatedAddOnly);
        }
        Collections.sort(scoreModelSorter);
        Collections.reverse(scoreModelSorter);
        int index1 = -1;
        List<String> topScoreSet = new ArrayList<>(resultsMap.keySet());
        for (RawScoreModel score1 : scoreModelSorter) {
            String rs1Key = score1.getParameterId();
            index1++;
            if (!topScoreSet.contains(rs1Key)) {
                continue;
            }
            int index2 = -1;
            for (RawScoreModel score2 : scoreModelSorter) {
                String rs2Key = score2.getParameterId();
                index2++;
                if (index2 <= index1 || !topScoreSet.contains(rs2Key)) {
                    continue;
                }
                Set<String> total = new HashSet<>(resultsMap.get(rs1Key).getSpecTitles());
                total.addAll(resultsMap.get(rs2Key).getSpecTitles());
                double key1Better = compareDataScores(resultsMap.get(rs2Key).getSpectrumMatchResult(), resultsMap.get(rs1Key).getSpectrumMatchResult(), fullDataSize, false, applyFDR);
                System.out.println(rs1Key + " better " + rs2Key + "  " + key1Better);
                System.out.println("resultsMap.get(rs1Key) " + resultsMap.get(rs1Key).getIdPSMNumber() + "(" + resultsMap.get(rs1Key).getFinalScore() + ")   vs " + resultsMap.get(rs2Key).getIdPSMNumber() + "(" + resultsMap.get(rs2Key).getFinalScore() + ")" + "   " + key1Better);

                if ((key1Better > 0) || Double.isNaN(key1Better)) {// && (resultsMap.get(rs1Key).getFinalScore() > resultsMap.get(rs2Key).getFinalScore())
                    System.out.println("-----------------------------------remove " + rs2Key + "-------------------------------");
                    topScoreSet.remove(rs2Key);
                } else if ((key1Better < 0)) {
                    System.out.println("-----------------------------------remove " + rs1Key + "-------------------------------");
                    topScoreSet.remove(rs1Key);
                    break;
                } else {
                    if (score2.getFinalScore() > score1.getFinalScore()) {
                        topScoreSet.remove(rs1Key);
                        break;
                    } else if (score2.getFinalScore() < score1.getFinalScore()) {
                        topScoreSet.remove(rs2Key);
                    } else if (score2.getIdPSMNumber() > score1.getIdPSMNumber()) {
                        topScoreSet.remove(rs1Key);
                        break;

                    } else {
                        topScoreSet.remove(rs2Key);
                    }
                    System.out.println("1<<<>>>>comparison was even what should we do with " + rs1Key + " and " + rs2Key + "  " + topScoreSet);
                }
            }
        }
        if (topScoreSet.size() > 1) {
            System.out.println("error should be only oine ---->> value " + topScoreSet);

        } else if (topScoreSet.isEmpty()) {
            System.out.println("error should be only oine value " + topScoreSet);
            return "";
        }
        String topSelection = topScoreSet.get(0);
        if (!scoreModelSorter.get(0).getParameterId().equalsIgnoreCase(topSelection)) {
            topSelection = topSelection + "_-_" + scoreModelSorter.get(0).getParameterId();
            System.out.println("---------------3----------------->> disagree with both methods " + scoreModelSorter.get(0).getParameterId() + "  " + topSelection);
        }
        return topSelection;
    }

    public static void checkScoreForParam(String paramName) {
        System.out.println("Param is " + paramName);
        //estimate scores now to compare 
        DescriptiveStatistics ds = new DescriptiveStatistics();
        for (double zSc : MainUtilities.getParamScoreSet()) {
            ds.addValue(zSc);

        }
        MainUtilities.getParamScoreSet().clear();
        System.out.println("----------------------------------------Scores ------------------------------------------");
        System.out.println(" quartiles ds 5% " + ds.getPercentile(5) + "    ds 25% " + ds.getPercentile(25) + "   median " + ds.getPercentile(50) + "  large change " + ds.getPercentile(75) + "  max " + ds.getMax() + "   Range " + (ds.getMax() - ds.getMin()));
//        MainUtilities.paramScoreRange.put((ds.getMax() - ds.getMin()), paramName);

    }
//
//    public static double isBetterScore1(List<SpectrumMatch> referenceData, List<SpectrumMatch> toData, double totalSize) {
//        List<Double> sharedTestData = new ArrayList<>();
//        List<Double> sharedReferenceData = new ArrayList<>();
//        List<Double> uniqueTestData = new ArrayList<>();
//        List<Double> uniqueReferenceData = new ArrayList<>();
//        Map<String, Double> fullMatchScores = new HashMap<>();
//        Map<String, Double> toMatchScores = new HashMap<>();
//        if (referenceData == null && toData != null) {
//            return 1;
//        }
//        if (referenceData != null && toData == null) {
//            return -1;
//        }
//        if (referenceData == null && toData == null) {
//            return -1;
//        }
//        toData.stream().map(sm -> {
//            fullMatchScores.put(sm.getSpectrumTitle(), 0.0);
//            return sm;
//        }).forEachOrdered(sm -> {
//            toMatchScores.put(sm.getSpectrumTitle(), sm.getBestPeptideAssumption().getRawScore());
//        });
//        referenceData.forEach(sm -> {
//            fullMatchScores.put(sm.getSpectrumTitle(), sm.getBestPeptideAssumption().getRawScore());
//        });
//
//        for (String titile : fullMatchScores.keySet()) {
//            double referenceScore = fullMatchScores.get(titile);
//            if (toMatchScores.containsKey(titile)) {
//                double toScore = toMatchScores.get(titile);
//                if (referenceScore > 0) {
//                    sharedTestData.add(toScore);
//                    sharedReferenceData.add(referenceScore);
//                } else {
//                    uniqueTestData.add(toScore);
//                }
//            } else if (referenceScore > 0) {
//                uniqueReferenceData.add(referenceScore);
//            }
//        }
//        double[] referenceSharedData = sharedReferenceData.stream().mapToDouble(Double::doubleValue).toArray();
//        double[] toSharedData = sharedTestData.stream().mapToDouble(Double::doubleValue).toArray();
//        double[] toOnlyData = uniqueTestData.stream().mapToDouble(Double::doubleValue).toArray();
//        double[] referenceUniqueData = uniqueReferenceData.stream().mapToDouble(Double::doubleValue).toArray();
//        double score1 = SpectraUtilities.performcomparison(referenceSharedData, toSharedData, true);
//        double score2 = SpectraUtilities.performcomparison(referenceUniqueData, toOnlyData, false);
////        if (referenceUniqueData.length == 0 && toOnlyData.length > 0) {
////            if (referenceUniqueData.length == 0 && toOnlyData.length > 0) {
////                System.out.println("-------------------------->>> adding new spec only potintial fb " + toOnlyData.length + "  ");
////
////                DescriptiveStatistics ds2 = new DescriptiveStatistics(referenceSharedData);
////                int counter = 0;
////                for (double d : toOnlyData) {
////                    if (d > ds2.getPercentile(75)) {
////                        counter++;
////                    }
////                }
////                if (counter < (0.05 * referenceSharedData.length)) {
////                    return -1;
////                }
////
////            }
////        }
////        double totalSampleSize = toData.size()+referenceData.size();// toSharedData.length + uniqueReferenceData.size();
////        double ratio1 = (double) (2.0*sharedReferenceData.size()) / totalSampleSize; //1;//
////        double ratio2 =(1.0-ratio1);// (double) Math.max(onlyToData.size(), uniqueReferenceData.size()) / totalSampleSize;   //Math.max(onlyToData.size(), onlyFromData.size()) / (double) optProtDataset.getSubsetSize();//Math.max(onlyToData.size(), onlyFromData.size()) / totalSampleSize;   //
//
//        double totalSampleSize = 2.0 * totalSize;// uniqueReferenceData.size() + uniqueTestData.size() + sharedTestData.size() + sharedReferenceData.size();// toSharedData.length + uniqueReferenceData.size();
//        double ratio1 = (double) (2.0 * sharedReferenceData.size()) / totalSampleSize; //1;//
//        double uniqueSize = (toOnlyData.length + referenceUniqueData.length);
//        if (toOnlyData.length <= 4 && referenceUniqueData.length > 4) {
//            uniqueSize = (2.0 * referenceUniqueData.length);
//        } else if (referenceUniqueData.length <= 4 && toOnlyData.length > 4) {
//            uniqueSize = (2.0 * toOnlyData.length);
//        }
//        double ratio2 = uniqueSize / totalSampleSize;
//
//        double adjustedScore1 = score1 * ratio1;//* (1.0 - ratio);
//        double adjustedScore2 = score2 * ratio2;//ratio;
//
//        if (referenceUniqueData.length <= 4 && toOnlyData.length <= 4) {
//            adjustedScore2 = adjustedScore1;
//        } else if (sharedTestData.size() <= 4) {
//            adjustedScore1 = adjustedScore2;
//        }
//
//        double finalScore = adjustedScore1 + adjustedScore2;
//        if (Double.isNaN(finalScore)) {
//            System.out.println(" is there Nan? " + finalScore + " = " + adjustedScore1 + " + " + adjustedScore2 + "  " + uniqueTestData.size() + "  " + uniqueReferenceData.size() + "   " + ratio1 + "   " + ratio2 + "  " + referenceData.size() + "  " + toData.size());
////             System.exit(0);
//        }
//
//        System.out.println("at uniqueTestData.size() " + uniqueTestData.size() + "   uniqueReferenceData.size() " + uniqueReferenceData.size() + "  sharedReferenceData " + sharedReferenceData.size());
////        finalScore = logScaleNormalize(finalScore, 2);
//// MainUtilities.finalScoreThrSet.add(finalScore);
//        MainUtilities.addToParamScoreSet(finalScore);
//
//        return finalScore;
//
//    }

    public static double performcomparison(double[] referenceData, double[] testData, boolean pairData) {
        ScoreComparison sc = new ScoreComparison();
        double finalScores = sc.calculateScore(referenceData, testData, pairData);
        return finalScores;

    }

    public static double calculateDatasetScoreThreshold(double oreginalDataSize, double subDataSize, double identificationRate, double idNumber) {
        double comparisonsThreshold = oreginalDataSize / subDataSize;
        comparisonsThreshold = comparisonsThreshold / 10.0;

        double idToFullFactor = oreginalDataSize / (idNumber * 100.0);

//        System.out.println("at level one " + comparisonsThreshold + "   identificationRate " + identificationRate + "  " + idToFullFactor);
        comparisonsThreshold = ScoreComparison.logScaleNormalize(comparisonsThreshold, Math.E);
        idToFullFactor = ScoreComparison.logScaleNormalize(idToFullFactor, Math.E);
        identificationRate = ScoreComparison.logScaleNormalize(identificationRate, Math.E);
//        System.out.println("at level one normalised " + comparisonsThreshold + "   identificationRate " + identificationRate + "  " + idToFullFactor);
        comparisonsThreshold = (comparisonsThreshold + identificationRate + idToFullFactor) / 3.0;
//        System.out.println("at level two normalised " + comparisonsThreshold);
        comparisonsThreshold = Math.floor(comparisonsThreshold * 100.0) / 100.0;
//        System.out.println("at finalnormalised " + comparisonsThreshold);
        return comparisonsThreshold;

    }

    public static int getSpectraSectionsNumber(int oreginalFileSize) {
        double minVal = 5000;
        double maxVal = 100000;
        double a = 4;
        double b = 15;
//        if (se.getIndex() == Advocate.sage.getIndex()) {
//            a = 2000;
//            b = 8000;
//        }

        if (maxVal <= oreginalFileSize) {
            return (int) b;
        }
        if (minVal >= oreginalFileSize) {
            return (int) a;
        }
        // Normalize to [0, 1]
        double normalized = (oreginalFileSize - minVal) / (maxVal - minVal);
        // Scale to [a, b]
        return (int) Math.round((a + (b - a) * normalized));
    }

    public static int scaleSubsetSize(int oreginalFileSize, Advocate se) {
        double minVal = 2000;
        double maxVal = 60000;
        double a = 1000;
        double b = 1500;
//        if (se.getIndex() == Advocate.sage.getIndex()) {
//            a = 2000;
//            b = 8000;
//        }

        if (maxVal <= oreginalFileSize) {
            return (int) 2000;
        }
        if (minVal >= oreginalFileSize) {
            return (int) oreginalFileSize;
        }
        // Normalize to [0, 1]
        double normalized = (oreginalFileSize - minVal) / (maxVal - minVal);
        // Scale to [a, b]
        return (int) Math.round((a + (b - a) * normalized));
    }

    public static Set<String> getModifiedSpectraSubset(String mod, List<SpectrumMatch> matches) {

        Set<String> subset = new LinkedHashSet<>();
        Modification modification = ptmFactory.getModification(mod);

        double mass = Math.round(modification.getMass());
        String updatedModName = mass + "@" + modification.getPattern().toString();
        boolean terminal = (modification.getModificationType().isCTerm() || modification.getModificationType().isNTerm()) && modification.getPattern().toString().trim().equals("");
//        System.out.println("mod " + mod + "  " + modification.getMass() + "  " + updatedModName + "  ");   
//        System.out.println("at mod "+mod+"  "+modification.getPattern().toString());
        for (SpectrumMatch sm : matches) {
            if (sm.getBestPeptideAssumption().getPeptide().getNVariableModifications() > 0) {
                ModificationMatch[] mms = sm.getBestPeptideAssumption().getPeptide().getVariableModifications();
                for (ModificationMatch mm : mms) {
                    double rMass = Math.round(Double.parseDouble(mm.getModification().split("@")[0]));//    mm.getModification().split("@")[0].split("\\.")[1].length();
                    String modAsString;
                    if (terminal) {
                        modAsString = rMass + "@";
                    } else {
                        modAsString = rMass + "@" + mm.getModification().split("@")[1];
                    }
//                    System.out.println("mod maching is " + modAsString + "  oreginal--> " + mm.getModification());
                    if (modAsString.equalsIgnoreCase(updatedModName)) {
                        subset.add(sm.getSpectrumTitle());
                        break;
                    }
                }

            }
        }
        return subset;
    }

    public static List<SpectrumMatch> getModificationFrequentScore(String mod, Set<String> modMaches, Set<String> refMatches, List<SpectrumMatch> specList) {

        double newAdded = 0;
        double lost = 0;
        double shared = 0;
        Set<String> fullSpecTitiles = new HashSet<>(refMatches);
        fullSpecTitiles.addAll(modMaches);
        Set<String> sharedSpec = new HashSet<>();
        Set<String> newAddedSpec = new HashSet<>();
        List<SpectrumMatch> newAddedspecList = new ArrayList<>();
        for (String sm : fullSpecTitiles) {
            if (!refMatches.contains(sm)) {
                newAdded++;
                newAddedSpec.add(sm);
            } else if (!modMaches.contains(sm)) {
                lost++;
            } else {
                shared++;
                sharedSpec.add(sm);
            }
        }
        double effect = ((double) (newAdded) / (double) (shared + lost));
//        if (mod.contains("of K")) {
        System.out.println(mod + "---->>> \t" + effect);
//        }
        for (SpectrumMatch sm : specList) {
            if (newAddedSpec.contains(sm.getSpectrumTitle())) {
                newAddedspecList.add(sm);
            }

        }

        return newAddedspecList;//((lost) / (double) shared)*(newAdded);
    }

    public static double[] isPotintialVariableModification(String modPattern, Map<String, SpectrumMatch> modSubSet, Map<String, SpectrumMatch> nonModRefsubSet) {

        double frequency = 0;
        char m = modPattern.charAt(0);
        for (String key : nonModRefsubSet.keySet()) {
            SpectrumMatch refMach = nonModRefsubSet.get(key);
            String refSequence = refMach.getBestPeptideAssumption().getPeptide().getSequence();
            if (refSequence.contains(modPattern)) {
//                frequency++;
            }

        }
        double potintialPSM = 0;
        for (String key : modSubSet.keySet()) {
            SpectrumMatch vmMach = modSubSet.get(key);
            String vmodSequence = vmMach.getBestPeptideAssumption().getPeptide().getSequence();
            if (vmodSequence.contains(modPattern) && !nonModRefsubSet.containsKey(key)) {
                int freq = SpectraUtilities.getFraquent(modPattern.charAt(0), vmodSequence);
                if (freq == 2 || freq == 1) {
                    potintialPSM++;
                }
                frequency += freq;
            }
        }
        return new double[]{potintialPSM, frequency};
    }

    private static int getFraquent(char pattern, String seq) {
        int freq = 0;
        for (char c : seq.toCharArray()) {
            if (c == pattern) {
                freq++;
            }
        }

        return freq;
    }

    public static double getQuartileValue(List<Double> sortedList, double percentile) {
        // Sort the list to find the median
        Collections.sort(sortedList);
        int n = sortedList.size();
        double position = (n + 1) * percentile;
        int integerPart = (int) position;
        double fractionalPart = position - integerPart;

        if (integerPart == 0) {
            return sortedList.get(0);
        } else if (integerPart >= n) {
            return sortedList.get(n - 1);
        } else {
            return sortedList.get(integerPart - 1) + fractionalPart
                    * (sortedList.get(integerPart) - sortedList.get(integerPart - 1));
        }
    }

    public static int getMedianIndex(List<Double> list) {
        // Sort the list to find the median
        Collections.sort(list);
        int size = list.size();
        int medianIndex;
        if (size % 2 == 1) {
            // Odd number of elements, median is the middle one
            medianIndex = size / 2;
        } else {
            // Even number of elements, median is the average of the two middle ones
            // Here, we'll return the lower median index (size / 2 - 1)
            medianIndex = (size / 2) - 1;
        }

        return medianIndex;
    }

    public static void main(String[] args) {

        double[] referenceUniqueData = new double[]{31.2, 31.2, 31.2, 31.2, 31.2, 31.2, 31.2};
        double[] testUniqueData = new double[]{32.7, 44.8, 55.0, 42.4, 46.8, 45.5, 40.2, 44.7, 37.6};
        double score2 = SpectraUtilities.performcomparison(referenceUniqueData, testUniqueData, false);
//        double value = isBetterScore(to, to, false);
        System.out.println("value is " + score2);
    }

    public static int findMainDrop(int[] numbers) {
        if (numbers.length < 2) {
            System.out.println("Not enough data points to detect a drop.");
            return 0;
        }

        double maxDrop = 0;  // Largest drop value
        double previous = 0; // Previous number in the sequence
        double next = 0;     // Next number in the sequence
        int selectedIndex = 0;
        for (int i = 1; i < numbers.length; i++) {
            double drop = numbers[i - 1] - numbers[i];  // Calculate the drop between consecutive elements

            if (drop > maxDrop) {
                maxDrop = drop;
                previous = numbers[i - 1];
                selectedIndex = (i - 1);
                next = numbers[i];
            }
        }

        if (maxDrop > 0) {
            System.out.println("The main drop is from " + previous + " to " + next + " with a drop of " + maxDrop + "  " + selectedIndex);
        } else {
            System.out.println("No drop detected.");
        }
//        System.exit(0);
        return selectedIndex;
    }

}
