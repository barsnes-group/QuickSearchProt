package dataanalysis;

import com.compomics.util.experiment.identification.Advocate;
import com.compomics.util.experiment.identification.matches.SpectrumMatch;
import com.compomics.util.experiment.identification.spectrum_assumptions.PeptideAssumption;
import com.compomics.util.experiment.io.identification.IdfileReader;
import com.compomics.util.experiment.io.identification.IdfileReaderFactory;
import com.compomics.util.experiment.io.mass_spectrometry.MsFileHandler;
import com.compomics.util.parameters.identification.IdentificationParameters;
import com.compomics.util.parameters.identification.search.SearchParameters;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import no.uib.probe.quicksearchprot.util.MainUtilities;
import org.xmlpull.v1.XmlPullParserException;

/**
 *
 * @author yfa041
 */
public class GenerateIdentifiedSpectraListAsFiles {

    /**
     * The identification file reader factory of compomics utilities.
     */
    private final IdfileReaderFactory readerFactory = IdfileReaderFactory.getInstance();

    public GenerateIdentifiedSpectraListAsFiles(String dataFolderUrl) {
        File dataFolder = new File(dataFolderUrl);
        String[] dsTypes = new String[]{"subset", "fullset"};
        for (File dsFolder : dataFolder.listFiles()) {
            try {
                MsFileHandler msFileHandler = new MsFileHandler();
                File msFile = new File(dsFolder, "ms.mgf");
                msFileHandler.register(msFile, MainUtilities.OptProt_Waiting_Handler);
                for (String type : dsTypes) {
                    Map<String, Double> spectraIdScore = readResults(dsFolder, Advocate.xtandem, type, msFileHandler);
                    System.out.println("filnal spec size " + dsFolder.getName() + "   " + type + "  " + spectraIdScore.size());
                }
            } catch (IOException ex) {
                Logger.getLogger(GenerateIdentifiedSpectraListAsFiles.class.getName()).log(Level.SEVERE, null, ex);
            }

        }

    }

    public Map<String, Double> readResults(File dsFolder, Advocate SE, String dsType, MsFileHandler msFileHandler) {
        Map<String, Double> spectraIdScore = new LinkedHashMap<>();
        try {
            System.out.println("SE " + SE.getName());
            File searchParam = new File(dsFolder, dsType + "_" + SE.getName() + ".par");
            IdentificationParameters identificationParam = IdentificationParameters.getIdentificationParameters(searchParam);
            SearchParameters searchParameters = identificationParam.getSearchParameters();
            File searchOutput;
            if (SE.getName().equalsIgnoreCase(Advocate.sage.getName())) {
                searchOutput = new File(dsFolder, dsType + ".sage.tsv");

            } else {
                searchOutput = new File(dsFolder, dsType + ".t.xml");
            }

            IdfileReader idReader = readerFactory.getFileReader(searchOutput);
            final List<SpectrumMatch> spematches = Collections.synchronizedList(idReader.getAllSpectrumMatches(msFileHandler, MainUtilities.OptProt_Waiting_Handler, searchParameters));

            for (SpectrumMatch sm : spematches) {
                PeptideAssumption peptide = sm.getAllPeptideAssumptions().toList().get(0);
                if (peptide.getRawScore() > 0) {
                    sm.setBestPeptideAssumption(peptide);
                    spectraIdScore.put(sm.getSpectrumTitle(), peptide.getRawScore());
                }

            }
        } catch (IOException ex) {
            Logger.getLogger(GenerateIdentifiedSpectraListAsFiles.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(GenerateIdentifiedSpectraListAsFiles.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(GenerateIdentifiedSpectraListAsFiles.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(GenerateIdentifiedSpectraListAsFiles.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JAXBException ex) {
            Logger.getLogger(GenerateIdentifiedSpectraListAsFiles.class.getName()).log(Level.SEVERE, null, ex);
        } catch (XmlPullParserException ex) {
            Logger.getLogger(GenerateIdentifiedSpectraListAsFiles.class.getName()).log(Level.SEVERE, null, ex);
        } catch (XMLStreamException ex) {
            Logger.getLogger(GenerateIdentifiedSpectraListAsFiles.class.getName()).log(Level.SEVERE, null, ex);
        }
        return spectraIdScore;

    }

    public static void main(String[] args) {
        GenerateIdentifiedSpectraListAsFiles exportIds = new GenerateIdentifiedSpectraListAsFiles("D:\\Manuscripts\\manuscript 2024\\Analysis");
    }
}
