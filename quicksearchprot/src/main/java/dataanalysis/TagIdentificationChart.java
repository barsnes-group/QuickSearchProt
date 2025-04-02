/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dataanalysis;

import com.compomics.util.experiment.identification.matches.SpectrumMatch;
import com.compomics.util.experiment.identification.spectrum_assumptions.PeptideAssumption;
import com.compomics.util.experiment.identification.spectrum_assumptions.TagAssumption;
import com.compomics.util.experiment.io.identification.IdfileReader;
import com.compomics.util.experiment.io.identification.IdfileReaderFactory;
import com.compomics.util.experiment.io.mass_spectrometry.MsFileHandler;
import com.compomics.util.parameters.identification.IdentificationParameters;
import com.compomics.util.parameters.identification.search.SearchParameters;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import no.uib.probe.quicksearchprot.util.MainUtilities;
import org.jfree.svg.SVGGraphics2D;
import org.jfree.svg.SVGUtils;
import org.xmlpull.v1.XmlPullParserException;

/**
 *
 * @author yfa041
 */
public class TagIdentificationChart {

    /**
     * The identification file reader factory of compomics utilities.
     */
    private static final IdfileReaderFactory readerFactory = IdfileReaderFactory.getInstance();

    public static void compareTagToIdentifications(String dataFolderURL) {
        try {
            File dataFolder = new File(dataFolderURL);
            Set<String> datasetName = new LinkedHashSet<>();
            datasetName.add("DS1-qExactive01819");    //1
            datasetName.add("DS2-Adult_CD8Tcells_Gel_Elite_44_f24");    //2
            datasetName.add("DS3-b1948_293T_proteinID_12B_QE3_122212");          //3
            datasetName.add("DS4-Nor_NTC_2");        //4
            datasetName.add("DS5-JurkatShotgunProteomicsReplicate3");        //5
            datasetName.add("DS6-20140711_EXQ00_KiSh_SA_Brain_4");        //6

            File denovoSearchParam = new File(dataFolder, "default_Denovo.par");
            Map<String, double[]> xtandDsFullMap = new LinkedHashMap<>();
            Map<String, double[]> sageDsFullMap = new LinkedHashMap<>();
            IdentificationParameters denovo_identificationParam = IdentificationParameters.getIdentificationParameters(denovoSearchParam);
            SearchParameters deNovo_searchParameters = denovo_identificationParam.getSearchParameters();
            double xtandIdRatio = 0;
            double sageIdRatio = 0;
            for (String datasetfId : datasetName) {
                String datasetId = datasetfId.split("-")[1];
                File msFile = new File(dataFolder, datasetId + ".mgf");
                File xTandemFile = new File(dataFolder, datasetId + ".t.xml");
                File tagFile = new File(dataFolder, datasetId + ".tags");
                File searchParamXtan = new File(dataFolder, datasetId + "_xtan.par");
                File searchParamSage = new File(dataFolder, datasetId + "_sage.par");
                File sageFile = new File(dataFolder, datasetId + ".sage.tsv");

                //get spec with confident tag
                IdfileReader idReader = readerFactory.getFileReader(tagFile);
                MsFileHandler msFileHandler = new MsFileHandler();
                msFileHandler.register(msFile, MainUtilities.OptProt_Waiting_Handler);
                ArrayList<SpectrumMatch> tagMatches = idReader.getAllSpectrumMatches(msFileHandler, MainUtilities.OptProt_Waiting_Handler, deNovo_searchParameters);
                Set<SpectrumMatch> matches = new LinkedHashSet<>();
                for (SpectrumMatch sm : tagMatches) {
                    TagAssumption tag = sm.getAllTagAssumptions().toList().get(0);
                    if (tag.getScore() <= 0.01) {
                        sm.setBestTagAssumption(tag);
                        matches.add(sm);
                    }
                }
                System.out.println("for ds " + datasetId + "  size " + matches.size());
                IdentificationParameters identificationParam = IdentificationParameters.getIdentificationParameters(searchParamXtan);
                SearchParameters searchParameters = identificationParam.getSearchParameters();
                idReader = readerFactory.getFileReader(xTandemFile);
                List<SpectrumMatch> spematches = Collections.synchronizedList(idReader.getAllSpectrumMatches(msFileHandler, MainUtilities.OptProt_Waiting_Handler, searchParameters));
                Map<String, SpectrumMatch> mapIds = new LinkedHashMap<>();
                for (SpectrumMatch sm : spematches) {
                    PeptideAssumption peptide = sm.getAllPeptideAssumptions().toList().get(0);
                    sm.setBestPeptideAssumption(peptide);
                    mapIds.put(sm.getSpectrumTitle(), sm);
                }
                for (SpectrumMatch sm : matches) {
                    if (mapIds.containsKey(sm.getSpectrumTitle()) && mapIds.get(sm.getSpectrumTitle()).getBestPeptideAssumption().getRawScore() > 0) {
                        sm.setBestPeptideAssumption(mapIds.get(sm.getSpectrumTitle()).getBestPeptideAssumption());
                    }
                }

                int agree = 0;

                for (SpectrumMatch sm : matches) {
                    if (sm.getBestPeptideAssumption() != null) {
                        agree++;

                    }
                    sm.setBestPeptideAssumption(null);
                }

                double perc = ((double) agree * 100.0 / (double) matches.size());
                xtandIdRatio += perc;
                System.out.println("for ds xtand " + datasetId + "  agree is " + agree + "  disagree " + (matches.size() - agree) + "  % " + perc + "   " + (100.0 - perc));
                xtandDsFullMap.put(datasetfId, new double[]{perc, (100.0 - perc)});

                //sage data
                identificationParam = IdentificationParameters.getIdentificationParameters(searchParamSage);
                searchParameters = identificationParam.getSearchParameters();
                idReader = readerFactory.getFileReader(sageFile);
                spematches = Collections.synchronizedList(idReader.getAllSpectrumMatches(msFileHandler, MainUtilities.OptProt_Waiting_Handler, searchParameters));
                mapIds = new LinkedHashMap<>();
                for (SpectrumMatch sm : spematches) {
                    PeptideAssumption peptide = sm.getAllPeptideAssumptions().toList().get(0);
                    sm.setBestPeptideAssumption(peptide);
                    mapIds.put(sm.getSpectrumTitle(), sm);
                }
                for (SpectrumMatch sm : matches) {
                    if (mapIds.containsKey(sm.getSpectrumTitle()) && mapIds.get(sm.getSpectrumTitle()).getBestPeptideAssumption().getRawScore() > 0) {

                        sm.setBestPeptideAssumption(mapIds.get(sm.getSpectrumTitle()).getBestPeptideAssumption());
//                        System.out.println("sm tit()" + sm.getBestPeptideAssumption().getScore() + "   " + sm.getBestPeptideAssumption().getRawScore());
                    }
                }

                agree = 0;

                for (SpectrumMatch sm : matches) {
                    if (sm.getBestPeptideAssumption() != null) {
                        agree++;
                        sm.setBestPeptideAssumption(null);
                    }
                }

                perc = ((double) agree * 100.0 / (double) matches.size());
                sageIdRatio += perc;
                System.out.println("for ds sage : " + datasetId + "  agree is " + agree + "  disagree " + (matches.size() - agree) + "   " + perc + " %  " + (100.0 - perc));
                sageDsFullMap.put(datasetfId, new double[]{perc, (100.0 - perc)});
            }
            xtandIdRatio = xtandIdRatio / (double) xtandDsFullMap.size();
            sageIdRatio = sageIdRatio / (double) sageDsFullMap.size();

            //plot data in barchart"Identification rate for Spectra with confident Tag"
            BarChartView bcv = new BarChartView(null, xtandDsFullMap, sageDsFullMap);
            SVGGraphics2D g2 = new SVGGraphics2D(720, 480); 
            g2.setFont(DataAnalysisHandler.Default_Font);
            Rectangle r1 = new Rectangle(0, 20, 700, 400);
            bcv.getChart().draw(g2, r1);
           
            g2.drawString("Average X!Tandem Identification Ratio for Spectra with Confident Tag  " + (int) xtandIdRatio + "  %", 10, 450);
            g2.drawString("Average Sage Identification Ratio for Spectra with Confident Tag  " + (int) sageIdRatio + "  %", 10, 470);

            File sub_f = new File(DataAnalysisHandler.Export_Data_Folder, "Figure_5.svg");
            SVGUtils.writeToSVG(sub_f, g2.getSVGElement());

        } catch (IOException | SQLException | ClassNotFoundException | InterruptedException | JAXBException | XmlPullParserException | XMLStreamException ex) {
            Logger.getLogger(DataAnalysisHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void main(String[] args) {
        TagIdentificationChart.compareTagToIdentifications("D:\\Manuscripts\\manuscript 2024\\SearchTagsResults");
    }

}
