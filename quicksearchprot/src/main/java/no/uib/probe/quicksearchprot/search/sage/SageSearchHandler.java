package no.uib.probe.quicksearchprot.search.sage;

import com.compomics.util.experiment.biology.enzymes.EnzymeFactory;
import com.compomics.util.experiment.biology.modifications.ModificationFactory;
import com.compomics.util.experiment.identification.Advocate;
import com.compomics.util.experiment.identification.matches.SpectrumMatch;
import com.compomics.util.io.IoUtil;
import com.compomics.util.parameters.identification.IdentificationParameters;
import com.compomics.util.parameters.identification.search.DigestionParameters;
import com.compomics.util.parameters.identification.search.SearchParameters;
//import com.compomics.util.parameters.identification.tool_specific.MyriMatchParameters;
import com.compomics.util.parameters.identification.tool_specific.SageParameters;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import no.uib.probe.quicksearchprot.configurations.Configurations;
import no.uib.probe.quicksearchprot.dataset.model.SearchingSubDataset;
import no.uib.probe.quicksearchprot.model.OptimisedSearchResults;
import no.uib.probe.quicksearchprot.model.ParameterScoreModel;
import no.uib.probe.quicksearchprot.model.RawScoreModel;
import no.uib.probe.quicksearchprot.model.SearchInputSetting;
import no.uib.probe.quicksearchprot.search.CommonSearchHandler;
import no.uib.probe.quicksearchprot.search.SearchExecuter;
import no.uib.probe.quicksearchprot.util.MainUtilities;
import no.uib.probe.quicksearchprot.util.SpectraUtilities;

/**
 *
 * @author yfa041
 */
public class SageSearchHandler extends CommonSearchHandler {

    /**
     * The compomics PTM factory.
     */
    private final ModificationFactory ptmFactory = ModificationFactory.getInstance();

    private final SearchingSubDataset optProtDataset;
    private final SearchInputSetting searchInputSetting;
    private final File generatedIdentificationParametersFile;
    private final OptimisedSearchResults optimisedSearchResults;
    private final IdentificationParameters identificationParameters;
    private final Map<String, TreeSet<ParameterScoreModel>> parameterScoreMap;
    private final Set<String> potintialFalsePostiveParamSet = new HashSet<>();

    public Map<String, TreeSet<ParameterScoreModel>> getParameterScoreMap() {
        return parameterScoreMap;
    }

    public SageSearchHandler(SearchingSubDataset optProtDataset, SearchInputSetting searchInputSetting, File generatedIdentificationParametersFile) throws IOException {

        this.optProtDataset = optProtDataset;
        this.searchInputSetting = searchInputSetting;
        this.generatedIdentificationParametersFile = generatedIdentificationParametersFile;
        this.identificationParameters = IdentificationParameters.getIdentificationParameters(generatedIdentificationParametersFile);
        this.optimisedSearchResults = new OptimisedSearchResults();
        this.parameterScoreMap = new LinkedHashMap<>();
        optProtDataset.setParameterScoreMap(parameterScoreMap);
        MainUtilities.cleanOutputFolder(searchInputSetting.getDatasetId());
        if (searchInputSetting.isOptimizeAllParameters()) {
            SageParameters sageParameters = (SageParameters) identificationParameters.getSearchParameters().getAlgorithmSpecificParameters().get(Advocate.sage.getIndex());
            sageParameters.setMaxVariableMods(2);
            sageParameters.setNumPsmsPerSpectrum(1);
            sageParameters.setGenerateDecoys(false);
            sageParameters.setWideWindow(false);
            sageParameters.setPredictRt(true);
              IdentificationParameters.saveIdentificationParameters(identificationParameters, generatedIdentificationParametersFile);

        }
        potintialFalsePostiveParamSet.add("precursorAccuracy");
        potintialFalsePostiveParamSet.add("fragmentAccuracy");
        potintialFalsePostiveParamSet.add("WideWindow");
//        potintialFalsePostiveParamSet.add("minPeaks");
        potintialFalsePostiveParamSet.add("maxFragmentCharge");
        for (int i = 0; i < DigestionParameters.Specificity.values().length; i++) {
            final String option = DigestionParameters.Specificity.getSpecificity(i).name();
            potintialFalsePostiveParamSet.add(option);
        }
//        potintialFalsePostiveParamSet.addAll(ptmFactory.getModifications(ModificationCategory.Common));
//        potintialFalsePostiveParamSet.addAll(ptmFactory.getModifications(ModificationCategory.Common_Biological));
//        potintialFalsePostiveParamSet.addAll(ptmFactory.getModifications(ModificationCategory.Common_Artifact));
//        for (String modId : ptmFactory.getModifications(ModificationCategory.Common_Biological)) {
//            if (ptmFactory.getModification(modId).getModificationType().isNTerm() || ptmFactory.getModification(modId).getModificationType().isCTerm()) {
//                potintialFalsePostiveParamSet.remove(modId);
//            }
//        }
//        for (String modId : ptmFactory.getModifications(ModificationCategory.Common_Artifact)) {
//            if (ptmFactory.getModification(modId).getModificationType().isNTerm() || ptmFactory.getModification(modId).getModificationType().isCTerm()) {
//                potintialFalsePostiveParamSet.remove(modId);
//            }
//        }

//        System.out.println(" identificationParameters.getFastaParameters().getDecoyFlag() " + identificationParameters.getFastaParameters().getDecoyFlag() + "  oreginal size  " + optProtDataset.getOreginalDatasize() + "  total subsize " + optProtDataset.getTotalSpectraNumber());
//        sageParameters.setMinFragmentMz(150.0);
//        sageParameters.setMaxFragmentMz(1500.0);
        MainUtilities.cleanOutputFolder(searchInputSetting.getDatasetId());
        parameterScoreMap.put("DigestionParameter", new TreeSet<>(Collections.reverseOrder()));
        parameterScoreMap.put("EnzymeParameter", new TreeSet<>(Collections.reverseOrder()));
        parameterScoreMap.put("SpecificityParameter", new TreeSet<>(Collections.reverseOrder()));
        parameterScoreMap.put("MaxMissCleavagesParameter", new TreeSet<>(Collections.reverseOrder()));
        parameterScoreMap.put("FragmentIonTypesParameter", new TreeSet<>(Collections.reverseOrder()));
        parameterScoreMap.put("PrecursorToleranceParameter", new TreeSet<>(Collections.reverseOrder()));
        parameterScoreMap.put("FragmentToleranceParameter", new TreeSet<>(Collections.reverseOrder()));
        parameterScoreMap.put("PrecursorChargeParameter", new TreeSet<>(Collections.reverseOrder()));
        parameterScoreMap.put("IsotopParameter", new TreeSet<>(Collections.reverseOrder()));
        parameterScoreMap.put("ModificationsParameter", new TreeSet<>(Collections.reverseOrder()));

        parameterScoreMap.put("SagePeptideLengthParameter", new TreeSet<>(Collections.reverseOrder()));
        parameterScoreMap.put("SagePeptideMassParameter", new TreeSet<>(Collections.reverseOrder()));
        parameterScoreMap.put("SageBucketSizeParameter", new TreeSet<>(Collections.reverseOrder()));
        parameterScoreMap.put("SageFragmentMzParameter", new TreeSet<>(Collections.reverseOrder()));
        parameterScoreMap.put("SageIonMinIndexParameter", new TreeSet<>(Collections.reverseOrder()));
        parameterScoreMap.put("SageMaxVariableModificationParameter", new TreeSet<>(Collections.reverseOrder()));
        parameterScoreMap.put("SageGenerateDecoyParameter", new TreeSet<>(Collections.reverseOrder()));
        parameterScoreMap.put("SageDeisotopParameter", new TreeSet<>(Collections.reverseOrder()));
        parameterScoreMap.put("SageChimericSpectraParameter", new TreeSet<>(Collections.reverseOrder()));//
        parameterScoreMap.put("SageWideWindowParameter", new TreeSet<>(Collections.reverseOrder()));
        parameterScoreMap.put("SagePredectRetentionTimeParameter", new TreeSet<>(Collections.reverseOrder()));
        parameterScoreMap.put("SageNumberOfPeakParameter", new TreeSet<>(Collections.reverseOrder()));
        parameterScoreMap.put("SageMinMatchedPeaksPeakParameter", new TreeSet<>(Collections.reverseOrder()));
        parameterScoreMap.put("SageMaxFragmentChargeParameter", new TreeSet<>(Collections.reverseOrder()));
        parameterScoreMap.put("SageBatchSizeParameter", new TreeSet<>(Collections.reverseOrder()));

    }

    private String digestionParameterOpt;
    private double fragmentTol = 0.02;
    private double precursorTol = 10.0;
    private boolean wideWindow = false;
    private String enzymeSpecificityOpt = "specific";

    public void startProcess(List<String> paramOrder) throws IOException {
        digestionParameterOpt = identificationParameters.getSearchParameters().getDigestionParameters().getCleavageParameter().name();
        searchInputSetting.setDigestionParameterOpt(digestionParameterOpt);
        MainUtilities.cleanOutputFolder(searchInputSetting.getDatasetId());
//        String bestEnzyme = CalculateEnzymeComparisonsBasedThreshold(optProtDataset, generatedIdentificationParametersFile, searchInputSetting);
////        if (!searchInputSetting.isOptimizeAllParameters()) {
//        //run refrence search 
//        String preEnzyme = identificationParameters.getSearchParameters().getDigestionParameters().getEnzymes().get(0).getName();
//        if (!bestEnzyme.equalsIgnoreCase(preEnzyme)) {
//            enzymeSpecificityOpt = identificationParameters.getSearchParameters().getDigestionParameters().getSpecificity(preEnzyme).name();
//            int nMissesCleavages = identificationParameters.getSearchParameters().getDigestionParameters().getnMissedCleavages(preEnzyme);
//            identificationParameters.getSearchParameters().getDigestionParameters().clearEnzymes();
//            optimisedSearchResults.setEnzymeName(bestEnzyme);
//            identificationParameters.getSearchParameters().getDigestionParameters().addEnzyme(EnzymeFactory.getInstance().getEnzyme(bestEnzyme));
//            identificationParameters.getSearchParameters().getDigestionParameters().setSpecificity(bestEnzyme, DigestionParameters.Specificity.valueOf(enzymeSpecificityOpt));
//            identificationParameters.getSearchParameters().getDigestionParameters().setnMissedCleavages(bestEnzyme, nMissesCleavages);
//            IdentificationParameters.saveIdentificationParameters(identificationParameters, generatedIdentificationParametersFile);
//        }

        runReferenceRun(optProtDataset, identificationParameters, searchInputSetting);
//        }

        for (String param : paramOrder) {
//            System.out.println("last active " + MainUtilities.getParamScoreSet().size() + "   " + MainUtilities.getParamScoreSet().last());
//
//            optProtDataset.updateMaxScore(MainUtilities.getParamScoreSet().last());
            System.out.println("-------------------------------------------param " + param + "-------------------------------------------  last max ");
            MainUtilities.cleanOutputFolder(searchInputSetting.getDatasetId());
            if (param.equalsIgnoreCase("DigestionParameter") && searchInputSetting.isOptimizeDigestionParameter()) {
                String[] values = this.optimizeEnzymeParameter(optProtDataset, generatedIdentificationParametersFile, searchInputSetting, parameterScoreMap.get("EnzymeParameter"));

                if (!values[0].equalsIgnoreCase("")) {
                    identificationParameters.getSearchParameters().getDigestionParameters().clearEnzymes();
                    optimisedSearchResults.setEnzymeName(values[0]);
                    int nMissesCleavages = Integer.parseInt(values[2]);// identificationParameters.getSearchParameters().getDigestionParameters().getnMissedCleavages(value);                   
                    identificationParameters.getSearchParameters().getDigestionParameters().addEnzyme(EnzymeFactory.getInstance().getEnzyme(values[0]));
                    enzymeSpecificityOpt = values[1];
//                    identificationParameters.getSearchParameters().getDigestionParameters().setSpecificity(values[0], DigestionParameters.Specificity.valueOf(values[1]));
                    identificationParameters.getSearchParameters().getDigestionParameters().setnMissedCleavages(values[0], nMissesCleavages);
                    IdentificationParameters.saveIdentificationParameters(identificationParameters, generatedIdentificationParametersFile);
                }

                continue;
            }
            if (param.equalsIgnoreCase("DigestionTypeParameter") && searchInputSetting.isOptimizeDigestionParameter()) {
                digestionParameterOpt = this.optimizeDigestionCleavageParameter(optProtDataset, generatedIdentificationParametersFile, searchInputSetting, parameterScoreMap.get("DigestionParameter"));
                searchInputSetting.setDigestionParameterOpt(digestionParameterOpt);
                continue;
            }

            if (param.equalsIgnoreCase("FragmentIonTypesParameter") && searchInputSetting.isOptimizeFragmentIonTypesParameter()) {
                String value = this.optimizeFragmentIonTypesParameter(optProtDataset, generatedIdentificationParametersFile, searchInputSetting, parameterScoreMap.get("FragmentIonTypesParameter"));
                int forward = Integer.parseInt(value.split("-")[0]);
                int rewind = Integer.parseInt(value.split("-")[1]);
                boolean update = false;
                if (!identificationParameters.getSearchParameters().getForwardIons().contains(forward)) {
                    ArrayList<Integer> forwardIonsList = new ArrayList<>();
                    forwardIonsList.add(forward);
                    identificationParameters.getSearchParameters().setForwardIons(forwardIonsList);
                    update = true;
                }
                if (!identificationParameters.getSearchParameters().getRewindIons().contains(rewind)) {
                    ArrayList<Integer> rewindIonsList = new ArrayList<>();
                    rewindIonsList.add(rewind);
                    identificationParameters.getSearchParameters().setRewindIons(rewindIonsList);
                    update = true;
                }
                if (update) {
                    IdentificationParameters.saveIdentificationParameters(identificationParameters, generatedIdentificationParametersFile);
                }
                continue;

            }

//confusing param
            if (param.equalsIgnoreCase("FragmentToleranceParameter") && searchInputSetting.isOptimizeFragmentToleranceParameter()) {
                fragmentTol = this.optimizeFragmentToleranceParameter(optProtDataset, generatedIdentificationParametersFile, searchInputSetting, parameterScoreMap.get("FragmentToleranceParameter"));
                continue;
            }

            if (param.equalsIgnoreCase("PrecursorChargeParameter") && searchInputSetting.isOptimizePrecursorChargeParameter()) {

                int[] values = this.optimizePrecursorChargeParameter(optProtDataset, generatedIdentificationParametersFile, searchInputSetting, parameterScoreMap.get("PrecursorChargeParameter"));
                if (values[1] != identificationParameters.getSearchParameters().getMaxChargeSearched()) {
                    identificationParameters.getSearchParameters().setMinChargeSearched(values[0]);
                    identificationParameters.getSearchParameters().setMaxChargeSearched(values[1]);
                    IdentificationParameters.saveIdentificationParameters(identificationParameters, generatedIdentificationParametersFile);
                }
                continue;
            }

            if (param.equalsIgnoreCase("PrecursorToleranceParameter") && searchInputSetting.isOptimizePrecursorToleranceParameter()) {

                precursorTol = this.optimizePrecursorToleranceParameter(optProtDataset, generatedIdentificationParametersFile, searchInputSetting, parameterScoreMap.get("PrecursorToleranceParameter"));
                continue;
            }

            if (param.equalsIgnoreCase("ModificationParameter") && searchInputSetting.isOptimizeModificationParameter()) {
                Map<String, Set<String>> modificationsResults = this.optimizeModificationsParameter(optProtDataset, generatedIdentificationParametersFile, searchInputSetting, parameterScoreMap.get("ModificationsParameter"));
                identificationParameters.getSearchParameters().getModificationParameters().clearFixedModifications();
                identificationParameters.getSearchParameters().getModificationParameters().clearVariableModifications();
                SageParameters sageParameters = (SageParameters) identificationParameters.getSearchParameters().getAlgorithmSpecificParameters().get(Advocate.sage.getIndex());

                for (String fixedMod : modificationsResults.get("fixedModifications")) {
                    if (ptmFactory.getModification(fixedMod) != null) {
                        identificationParameters.getSearchParameters().getModificationParameters().addFixedModification(ptmFactory.getModification(fixedMod));
                        identificationParameters.getSearchParameters().getModificationParameters().addRefinementFixedModification(ptmFactory.getModification(fixedMod));
                    }
                }
                for (String variableMod : modificationsResults.get("variableModifications")) {
                    if (ptmFactory.getModification(variableMod) != null) {
                        identificationParameters.getSearchParameters().getModificationParameters().addVariableModification(ptmFactory.getModification(variableMod));
                    }
                }
                IdentificationParameters.saveIdentificationParameters(identificationParameters, generatedIdentificationParametersFile);
                MainUtilities.resetExecutorService();
                continue;
            }
            if (param.equalsIgnoreCase("SageAdvancedParameter_A") && searchInputSetting.isOptimizeSageAdvancedParameter()) {
                SageParameters sageParameters = (SageParameters) identificationParameters.getSearchParameters().getAlgorithmSpecificParameters().get(Advocate.sage.getIndex());
                sageParameters.setMaxVariableMods(0);
                double[] dvalues = optimizeFragmentMzParameter(optProtDataset, identificationParameters, searchInputSetting, sageParameters, parameterScoreMap.get("SageFragmentMzParameter"));
                if (dvalues[1] != sageParameters.getMaxFragmentMz() || dvalues[0] != sageParameters.getMinFragmentMz()) {
                    sageParameters.setMinFragmentMz(dvalues[0]);
                    sageParameters.setMaxFragmentMz(dvalues[1]);
                    System.out.println("optimizeFragmentMzParameter " + dvalues[0] + "--" + dvalues[1] + "------------------------------------------------------------------------->>> 12 id rate " + optProtDataset.getActiveIdentificationNum());
                }
                int value = optimizeIonMinIndexParameter(optProtDataset, identificationParameters, searchInputSetting, sageParameters, parameterScoreMap.get("SageIonMinIndexParameter"));
                if (value != sageParameters.getMinIonIndex()) {
                    sageParameters.setMinIonIndex(value);
                    System.out.println("optimizeIonMinIndexParameter " + value + "------------------------------------------------------------------------->>> 12 id rate " + optProtDataset.getActiveIdentificationNum());
                }
//
                int[] values = optimizePeptideLengthParameter(optProtDataset, identificationParameters, searchInputSetting, sageParameters, parameterScoreMap.get("SagePeptideLengthParameter"));
                if (values[1] != sageParameters.getMaxPeptideLength() || values[0] != sageParameters.getMinPeptideLength()) {
                    sageParameters.setMinPeptideLength(values[0]);
                    sageParameters.setMaxPeptideLength(values[1]);
                    System.out.println("peptide length " + values[0] + "--" + values[1] + "------------------------------------------------------------------------->>> 12 id rate " + optProtDataset.getActiveIdentificationNum());
                }

                sageParameters.setMaxVariableMods(2);
                IdentificationParameters.saveIdentificationParameters(identificationParameters, generatedIdentificationParametersFile);

                continue;
            }

            if (param.equalsIgnoreCase("SageAdvancedParameter_B") && searchInputSetting.isOptimizeSageAdvancedParameter()) {
                SageParameters sageParameters = (SageParameters) identificationParameters.getSearchParameters().getAlgorithmSpecificParameters().get(Advocate.sage.getIndex());
                double[] dvalues = optimizePeptideMassParameter(optProtDataset, identificationParameters, sageParameters, searchInputSetting, parameterScoreMap.get("SagePeptideMassParameter"));
                if (dvalues[1] != sageParameters.getMaxPeptideMass() || dvalues[0] != sageParameters.getMinPeptideMass()) {
                    sageParameters.setMinPeptideMass(dvalues[0]);
                    sageParameters.setMaxPeptideMass(dvalues[1]);
                    System.out.println("optimizePeptideMassParameter " + dvalues[0] + "--" + dvalues[1] + "------------------------------------------------------------------------->>> 12 id rate " + optProtDataset.getActiveIdentificationNum());
                }

                int[] values = optimizeNumberOfPeakParameter(optProtDataset, identificationParameters, sageParameters, searchInputSetting, parameterScoreMap.get("SageNumberOfPeakParameter"));
                if (values[1] != sageParameters.getMaxPeaks() || values[0] != sageParameters.getMinPeaks()) {
                    sageParameters.setMinPeaks(values[0]);
                    sageParameters.setMaxPeaks(values[1]);
                }

                boolean valueBoolean = optimizeDeisotopParameter(optProtDataset, identificationParameters, sageParameters, searchInputSetting, parameterScoreMap.get("SageDeisotopParameter"));
                if (valueBoolean != sageParameters.getDeisotope()) {
                    sageParameters.setDeisotope(valueBoolean);
                }
                System.out.println("optimizeDeisotopParameter " + valueBoolean + "------------------------------------------------------------------------->>> 12 id rate " + optProtDataset.getActiveIdentificationNum());
                valueBoolean = optimizeChimericSpectraParameter(optProtDataset, identificationParameters, sageParameters, searchInputSetting, parameterScoreMap.get("SageChimericSpectraParameter"));
                if (valueBoolean != sageParameters.getChimera()) {
                    sageParameters.setChimera(valueBoolean);
                }
                System.out.println("SageChimericSpectraParameter " + valueBoolean + "------------------------------------------------------------------------->>> 12 id rate " + optProtDataset.getActiveIdentificationNum());
                valueBoolean = optimizePredectRetentionTimeParameter(optProtDataset, identificationParameters, sageParameters, searchInputSetting, parameterScoreMap.get("SagePredectRetentionTimeParameter"));
                if (valueBoolean != sageParameters.getPredictRt()) {
                    sageParameters.setPredictRt(valueBoolean);
                }
                System.out.println("SagePredectRetentionTimeParameter " + valueBoolean + "------------------------------------------------------------------------->>> 12 id rate " + optProtDataset.getActiveIdentificationNum());

                int intvalue = optimizeMaxVariableModificationParameter(optProtDataset, identificationParameters, sageParameters, searchInputSetting, parameterScoreMap.get("SageMaxVariableModificationParameter"));
                if (intvalue != sageParameters.getMaxVariableMods()) {
                    sageParameters.setMaxVariableMods(intvalue);//
                }
                intvalue = optimizeMinMatchedPeaksParameter(optProtDataset, identificationParameters, sageParameters, searchInputSetting, parameterScoreMap.get("SageMinMatchedPeaksPeakParameter"));
                if (intvalue != sageParameters.getMinMatchedPeaks()) {
                    sageParameters.setMinMatchedPeaks(intvalue);
                }
//                      
                intvalue = optimizeMaxFragmentChargeParameter(optProtDataset, identificationParameters, sageParameters, searchInputSetting, parameterScoreMap.get("SageMaxFragmentChargeParameter"));
                sageParameters.setMaxFragmentCharge(intvalue);

                //need adjustment
//                sageParameters.setGenerateDecoys(true);
                valueBoolean = optimizeGenerateDecoyParameter(optProtDataset, identificationParameters, sageParameters, searchInputSetting, parameterScoreMap.get("SageGenerateDecoyParameter"));
                if (valueBoolean != sageParameters.getGenerateDecoys()) {
                    sageParameters.setGenerateDecoys(valueBoolean);
                }
                wideWindow = optimizeWideWindowParameter(optProtDataset, identificationParameters, sageParameters, searchInputSetting, parameterScoreMap.get("SageWideWindowParameter"));
                sageParameters.setWideWindow(wideWindow);
                IdentificationParameters.saveIdentificationParameters(identificationParameters, generatedIdentificationParametersFile);

            }

        }
        if (!digestionParameterOpt.equalsIgnoreCase(identificationParameters.getSearchParameters().getDigestionParameters().getCleavageParameter().name())) {
            optimisedSearchResults.setDigestionParameter(digestionParameterOpt);
            identificationParameters.getSearchParameters().getDigestionParameters().clearEnzymes();
            identificationParameters.getSearchParameters().getDigestionParameters().setCleavageParameter(DigestionParameters.CleavageParameter.valueOf(digestionParameterOpt));
            IdentificationParameters.saveIdentificationParameters(identificationParameters, generatedIdentificationParametersFile);
        }

        if (!enzymeSpecificityOpt.equalsIgnoreCase("specific")) {
            identificationParameters.getSearchParameters().getDigestionParameters().setSpecificity(identificationParameters.getSearchParameters().getDigestionParameters().getEnzymes().get(0).getName(), DigestionParameters.Specificity.valueOf(enzymeSpecificityOpt));
            IdentificationParameters.saveIdentificationParameters(identificationParameters, generatedIdentificationParametersFile);

        }
        if (wideWindow) {
            SageParameters sageParameters = (SageParameters) identificationParameters.getSearchParameters().getAlgorithmSpecificParameters().get(Advocate.sage.getIndex());
            sageParameters.setWideWindow(wideWindow);
            System.out.println("add wide window ");
            IdentificationParameters.saveIdentificationParameters(identificationParameters, generatedIdentificationParametersFile);

        }
        if (fragmentTol != 0.02) {
            if (fragmentTol != identificationParameters.getSearchParameters().getFragmentIonAccuracy()) {
                identificationParameters.getSearchParameters().setFragmentIonAccuracy(fragmentTol);
                IdentificationParameters.saveIdentificationParameters(identificationParameters, generatedIdentificationParametersFile);
            }
        }
        if (precursorTol != 10.0) {
            if (precursorTol != identificationParameters.getSearchParameters().getPrecursorAccuracy()) {
                identificationParameters.getSearchParameters().setPrecursorAccuracy(precursorTol);
                if (precursorTol > 1) {
                    identificationParameters.getSearchParameters().setPrecursorAccuracyType(SearchParameters.MassAccuracyType.PPM);
                } else {
                    identificationParameters.getSearchParameters().setPrecursorAccuracyType(SearchParameters.MassAccuracyType.DA);
                }
                IdentificationParameters.saveIdentificationParameters(identificationParameters, generatedIdentificationParametersFile);
            }

        }

        for (String key
                : parameterScoreMap.keySet()) {
            System.out.println(key + "  " + parameterScoreMap.get(key));
        }

    }

    @Override
    public synchronized RawScoreModel excuteSearch(SearchingSubDataset optProtDataset, String defaultOutputFileName, String paramOption, IdentificationParameters tempIdParam, boolean addSpectraList, SearchInputSetting optProtSearchSettings, File identificationParametersFile) {
        if (!optProtSearchSettings.getSageEnabledParameters().getParamsToOptimize().isEnabledParam(paramOption.split("_")[0])) {
            System.out.println("param " + paramOption + " is not supported " + paramOption);
            return new RawScoreModel(paramOption);
        }
//        if (defaultOutputFileName.contains("_resultsf_Carbamilation of protein N-term") || defaultOutputFileName.contains("_resultsf_Acetylation of protein N-term") || defaultOutputFileName.contains("_resultsf_Pyrolidone from carbamidomethylated C")) {
//            System.out.println("param " + paramOption + " is not supported " + paramOption);
//            return new RawScoreModel();
//        }

        Future<File> f = MainUtilities.getLongExecutorService().submit(() -> {
            File resultOutput = SearchExecuter.executeSearch(defaultOutputFileName, optProtSearchSettings, optProtDataset.getSubMsFile(), optProtDataset.getSubFastaFile(), tempIdParam, identificationParametersFile);
            return resultOutput;
        });
        File resultOutput = null;
        try {
            while (!f.isDone()) {
            }
            resultOutput = f.get();
        } catch (InterruptedException | ExecutionException ex) {
            ex.printStackTrace();
        }
        List<SpectrumMatch> validatedMaches = SpectraUtilities.getValidatedIdentificationResults(resultOutput, optProtDataset.getSubMsFile(), Advocate.sage, tempIdParam);
        boolean potintialFP = false;
        if (potintialFalsePostiveParamSet.contains(paramOption.split("_")[0]) && !defaultOutputFileName.contains("optsearch_results0v_") && !defaultOutputFileName.contains("optsearch_results1v_")) {
            potintialFP = true;
            System.out.println("at param name is " + paramOption + "  " + validatedMaches.size());
        }
        if (paramOption.contains("charge-")) {
//            potintialFP = true;
        }

        if (paramOption.contains("_")) {
            paramOption = paramOption.split("_")[1];
        }

        RawScoreModel rawScore = SpectraUtilities.getComparableRawScore(optProtDataset, validatedMaches, Advocate.sage, addSpectraList, paramOption, potintialFP);//(optProtDataset, resultOutput, optProtDataset.getSubMsFile(), Advocate.sage, tempIdParam, updateDataReference);

        if (addSpectraList || rawScore.isSensitiveChange()) {
            rawScore.setSpectrumMatchResult(validatedMaches);
        }
        return (rawScore);
    }

    public int[] optimizePeptideLengthParameter(SearchingSubDataset optProtDataset, IdentificationParameters oreginaltempIdParam, SearchInputSetting optimisedSearchParameter, SageParameters sageParameter, TreeSet<ParameterScoreModel> parameterScoreSet) throws IOException {
        final ParameterScoreModel paramScore = new ParameterScoreModel();
        paramScore.setParamId("peptideLength");

        String msFileName = IoUtil.removeExtension(optProtDataset.getSubMsFile().getName());
        int selectedMaxPeptideLengthOption = sageParameter.getMaxPeptideLength();
        int selectedMinPeptideLengthOption = sageParameter.getMinPeptideLength();
        Map<String, RawScoreModel> resultsMap = Collections.synchronizedMap(new LinkedHashMap<>());

        sageParameter.setMaxPeptideLength(35);
        int selectedV = selectedMinPeptideLengthOption;
        for (int i = 5; i <= 10; i++) {
            if (i == selectedMinPeptideLengthOption) {
                continue;
            }
            sageParameter.setMinPeptideLength(i);
            final String option = "minPeptideLength_" + i;
            final String updatedName = Configurations.DEFAULT_RESULT_NAME + "_" + option + "_" + msFileName;
            int j = i;

            Future<RawScoreModel> f = MainUtilities.getExecutorService().submit(() -> {
                RawScoreModel scoreModel = excuteSearch(optProtDataset, updatedName, option, oreginaltempIdParam, true, optimisedSearchParameter, generatedIdentificationParametersFile);
                return scoreModel;
            });
            try {
                RawScoreModel scoreModel = f.get();
//                if (scoreModel.isSensitiveChange() && scoreModel.getSpectrumMatchResult().size() >= optProtDataset.getCurrentScoreModel().getSpectrumMatchResult().size()) {
//                    System.out.println("add as selected score " + j);
                if (scoreModel.isAcceptedChange()) {
                    resultsMap.put(j + "", scoreModel);
//                    selectedV = j;
                } else {
                    break;
                }

            } catch (ExecutionException | InterruptedException ex) {
                ex.printStackTrace();
            }
        }

        if (!resultsMap.isEmpty()) {
            String bestOption = SpectraUtilities.compareScoresSet(resultsMap, optProtDataset.getSubsetSize(), false, true);//  
            selectedMinPeptideLengthOption = Integer.parseInt(bestOption);
            optProtDataset.setActiveScoreModel(resultsMap.get(bestOption));
        }

        resultsMap.clear();
        sageParameter.setMinPeptideLength(selectedMinPeptideLengthOption);
        for (int i = 25; i < 35; i++) {
            if (i == selectedMaxPeptideLengthOption) {
                continue;
            }
            sageParameter.setMaxPeptideLength(i);
            final String option = "maxPeptideLength_" + i;
            final String updatedName = Configurations.DEFAULT_RESULT_NAME + "_" + option + "_" + msFileName;
            int j = i;
            Future<RawScoreModel> f = MainUtilities.getExecutorService().submit(() -> {
                RawScoreModel scoreModel = excuteSearch(optProtDataset, updatedName, option, oreginaltempIdParam, false, optimisedSearchParameter, generatedIdentificationParametersFile);
                return scoreModel;
            });
            try {
                RawScoreModel scoreModel = f.get();
                System.out.println("at indix " + i + "  " + j + "  " + scoreModel + "  vs  " + optProtDataset.getCurrentScoreModel());
                if (scoreModel.isAcceptedChange()) {
//                if (scoreModel.isSensitiveChange() && scoreModel.getSpectrumMatchResult().size() >= optProtDataset.getCurrentScoreModel().getSpectrumMatchResult().size()) {
                    resultsMap.put(j + "", scoreModel);
                } else if (!scoreModel.isSameData()) {
                    break;
                }
            } catch (ExecutionException | InterruptedException ex) {
                ex.printStackTrace();
            }
        }
        if (!resultsMap.isEmpty()) {
            selectedMaxPeptideLengthOption = Integer.parseInt(SpectraUtilities.compareScoresSet(resultsMap, optProtDataset.getSubsetSize(), false, true));
            optProtDataset.setActiveScoreModel(resultsMap.get(selectedMaxPeptideLengthOption + ""));
        }
        sageParameter.setMaxPeptideLength(selectedMaxPeptideLengthOption);
        paramScore.setScore(optProtDataset.getActiveIdentificationNum());
        paramScore.setParamValue(selectedMinPeptideLengthOption + "-" + selectedMaxPeptideLengthOption + "");
        parameterScoreSet.add(paramScore);

        return new int[]{selectedMinPeptideLengthOption, selectedMaxPeptideLengthOption};
    }

    public double[] optimizePeptideMassParameter(SearchingSubDataset optProtDataset, IdentificationParameters oreginaltempIdParam, SageParameters sageParameter, SearchInputSetting optimisedSearchParameter, TreeSet<ParameterScoreModel> parameterScoreSet) throws IOException {

        final ParameterScoreModel paramScore = new ParameterScoreModel();
        paramScore.setParamId("PeptideMass");
        String msFileName = IoUtil.removeExtension(optProtDataset.getSubMsFile().getName());
        double selectedMaxPeptideMassOption = sageParameter.getMaxPeptideMass();
        double selectedMinPeptideMassOption = sageParameter.getMinPeptideMass();
        Map<String, RawScoreModel> resultsMap = Collections.synchronizedMap(new LinkedHashMap<>());
        sageParameter.setMaxPeptideMass(selectedMaxPeptideMassOption);
        double lastScore = -100000.0;

        for (double i = 400; i <= 600;) {
            if (i == selectedMinPeptideMassOption) {
                i += 50.0;
            }
            sageParameter.setMinPeptideMass(i);
            final String option = "minPeptideMass_" + i;
            final String updatedName = Configurations.DEFAULT_RESULT_NAME + "_" + option + "_" + msFileName;
            double j = i;

            Future<RawScoreModel> f = MainUtilities.getExecutorService().submit(() -> {
                RawScoreModel scoreModel = excuteSearch(optProtDataset, updatedName, option, oreginaltempIdParam, false, optimisedSearchParameter, generatedIdentificationParametersFile);
                return scoreModel;
            });
            try {
                RawScoreModel scoreModel = f.get();
                if (scoreModel.isSensitiveChange() && lastScore < scoreModel.getFinalScore()) {
                    lastScore = scoreModel.getFinalScore();
                    resultsMap.put(j + "", scoreModel);
                } else {
                    break;
                }

            } catch (ExecutionException | InterruptedException ex) {
                ex.printStackTrace();
            }
            i += 50.0;
        }

        if (!resultsMap.isEmpty()) {
            String bestOption = SpectraUtilities.compareScoresSet(resultsMap, optProtDataset.getSubsetSize(), false, true);//  
            selectedMinPeptideMassOption = Double.parseDouble(bestOption);
            optProtDataset.setActiveScoreModel(resultsMap.get(bestOption));
        }
        sageParameter.setMinPeptideMass(selectedMinPeptideMassOption);
        resultsMap.clear();
        for (double i = 4000; i <= 6000;) {
            if (i == selectedMaxPeptideMassOption) {
                i += 500;
            }
            sageParameter.setMaxPeptideMass(i);
            final String option = "maxPeptideMass_" + i;
            final String updatedName = Configurations.DEFAULT_RESULT_NAME + "_" + option + "_" + msFileName;
            double j = i;
            Future<RawScoreModel> f = MainUtilities.getExecutorService().submit(() -> {
                RawScoreModel scoreModel = excuteSearch(optProtDataset, updatedName, option, oreginaltempIdParam, false, optimisedSearchParameter, generatedIdentificationParametersFile);
                return scoreModel;
            });
            try {
                RawScoreModel scoreModel = f.get();
                if (scoreModel.isSensitiveChange()) {
                    resultsMap.put(j + "", scoreModel);
                }
            } catch (ExecutionException | InterruptedException ex) {
                ex.printStackTrace();
            }

            i += 500;
        }

        if (!resultsMap.isEmpty()) {
            String bestOption = SpectraUtilities.compareScoresSet(resultsMap, optProtDataset.getSubsetSize(), false, true);//  
            selectedMaxPeptideMassOption = Double.parseDouble(bestOption);
            optProtDataset.setActiveScoreModel(resultsMap.get(bestOption));
        }
        paramScore.setScore(optProtDataset.getActiveIdentificationNum());
        paramScore.setParamValue(selectedMinPeptideMassOption + " - " + selectedMaxPeptideMassOption + "");
        parameterScoreSet.add(paramScore);
        sageParameter.setMaxPeptideMass(selectedMaxPeptideMassOption);

        return new double[]{selectedMinPeptideMassOption, selectedMaxPeptideMassOption};
    }

    public int optimizeMaxVariableModificationParameter(SearchingSubDataset optProtDataset, IdentificationParameters oreginaltempIdParam, SageParameters sageParameters, SearchInputSetting optimisedSearchParameter, TreeSet<ParameterScoreModel> parameterScoreSet) throws IOException {
        final ParameterScoreModel paramScore = new ParameterScoreModel();
        paramScore.setParamId("maxVarPTMs");

        int selectedOption = sageParameters.getMaxVariableMods();
        Map<String, RawScoreModel> resultsMap = Collections.synchronizedMap(new LinkedHashMap<>());
        String msFileName = IoUtil.removeExtension(optProtDataset.getSubMsFile().getName());

        for (int i = 1; i <= 3; i++) {
            if (i == selectedOption) {
                continue;
            }
            final String option = "maxVarPTMs_" + i;
            final String updatedName = Configurations.DEFAULT_RESULT_NAME + "_" + option + "_" + msFileName;
            sageParameters.setMaxVariableMods(i);
            final int j = i;

            Future<RawScoreModel> f = MainUtilities.getExecutorService().submit(() -> {
                RawScoreModel scoreModel = excuteSearch(optProtDataset, updatedName, option, oreginaltempIdParam, false, optimisedSearchParameter, generatedIdentificationParametersFile);
                return scoreModel;
            });
            try {
                RawScoreModel scoreModel = f.get();
                if (scoreModel.isAcceptedChange() || (scoreModel.isSensitiveChange() && j < selectedOption)) {
                    resultsMap.put(j + "", scoreModel);
                }

            } catch (ExecutionException | InterruptedException ex) {
                ex.printStackTrace();
            }
        }
        if (!resultsMap.isEmpty()) {
            String bestOption = SpectraUtilities.compareScoresSet(resultsMap, optProtDataset.getSubsetSize(), false, true);//  
            selectedOption = Integer.parseInt(bestOption);
            optProtDataset.setActiveScoreModel(resultsMap.get(bestOption));
        }

        paramScore.setScore(optProtDataset.getActiveIdentificationNum());
        paramScore.setParamValue(selectedOption + "");
        parameterScoreSet.add(paramScore);
        sageParameters.setMaxVariableMods(selectedOption);

        return selectedOption;
    }

    public double[] optimizeFragmentMzParameter(SearchingSubDataset optProtDataset, IdentificationParameters oreginaltempIdParam, SearchInputSetting optimisedSearchParameter, SageParameters sageParameter, TreeSet<ParameterScoreModel> parameterScoreSet) throws IOException {
        final ParameterScoreModel paramScore = new ParameterScoreModel();
        paramScore.setParamId("FragmentMz");
        String msFileName = IoUtil.removeExtension(optProtDataset.getSubMsFile().getName());
        double selectedMaxFragmentMzOption = sageParameter.getMaxFragmentMz();
        double selectedMinFragmentMzOption = sageParameter.getMinFragmentMz();
        Map<String, RawScoreModel> resultsMap = Collections.synchronizedMap(new LinkedHashMap<>());
        sageParameter.setMaxFragmentMz(selectedMaxFragmentMzOption);
        for (double i = 150; i <= 300;) {
            if (i == selectedMinFragmentMzOption) {
                i += 25.0;
                continue;
            }
            sageParameter.setMinFragmentMz(i);
            final String option = "minFragmentMz_" + i;
            final String updatedName = Configurations.DEFAULT_RESULT_NAME + "_" + option + "_" + msFileName;
            double j = i;

            Future<RawScoreModel> f = MainUtilities.getExecutorService().submit(() -> {
                RawScoreModel scoreModel = excuteSearch(optProtDataset, updatedName, option, oreginaltempIdParam, false, optimisedSearchParameter, generatedIdentificationParametersFile);
                return scoreModel;
            });
            try {
                RawScoreModel scoreModel = f.get();
                System.out.println("Min Fragment MZ " + j + "  " + scoreModel.getFinalScore()+"   "+scoreModel.getIdPSMNumber());
                if (scoreModel.isAcceptedChange()) {
//                if (scoreModel.isSensitiveChange() && scoreModel.getSpectrumMatchResult().size() >= optProtDataset.getCurrentScoreModel().getSpectrumMatchResult().size()) {
                    resultsMap.put(j + "", scoreModel);
                } else if (i > selectedMinFragmentMzOption) {
                    break;
                }

            } catch (ExecutionException | InterruptedException ex) {
                ex.printStackTrace();
            }
            i += 25.0;
        }

        if (!resultsMap.isEmpty()) {

            String bestOption = SpectraUtilities.compareScoresSet(resultsMap, optProtDataset.getSubsetSize(), false, true);//  
            selectedMinFragmentMzOption = Double.parseDouble(bestOption);
            optProtDataset.setActiveScoreModel(resultsMap.get(bestOption));
        }
        sageParameter.setMinFragmentMz(selectedMinFragmentMzOption);
        resultsMap.clear();
        for (double i = 1500; i <= 3000;) {
            if (i == selectedMaxFragmentMzOption) {
                i += 250;
                continue;
            }
            sageParameter.setMaxFragmentMz(i);
            final String option = "maxFragmentMz_" + i;
            final String updatedName = Configurations.DEFAULT_RESULT_NAME + "_" + option + "_" + msFileName;
            double j = i;
            Future<RawScoreModel> f = MainUtilities.getExecutorService().submit(() -> {
                RawScoreModel scoreModel = excuteSearch(optProtDataset, updatedName, option, oreginaltempIdParam, false, optimisedSearchParameter, generatedIdentificationParametersFile);
                return scoreModel;
            });
            try {
                RawScoreModel scoreModel = f.get();
                 System.out.println("Max Fragment MZ " + j + "  " + scoreModel.getFinalScore()+"   "+scoreModel.getIdPSMNumber());
                if (j < selectedMaxFragmentMzOption && (scoreModel.isAcceptedChange() && scoreModel.getSpectrumMatchResult().size() >= optProtDataset.getCurrentScoreModel().getSpectrumMatchResult().size())) {
                    resultsMap.put(j + "", scoreModel);
                } else if (j > selectedMaxFragmentMzOption && !scoreModel.isSensitiveChange()) {
                    break;
                }

            } catch (ExecutionException | InterruptedException ex) {
                ex.printStackTrace();
            }

            i += 250;
        }
        if (!resultsMap.isEmpty()) {
            String bestOption = SpectraUtilities.compareScoresSet(resultsMap, optProtDataset.getSubsetSize(), false, true);//  
            selectedMaxFragmentMzOption = Double.parseDouble(bestOption);
            optProtDataset.setActiveScoreModel(resultsMap.get(bestOption));
        }
        paramScore.setScore(optProtDataset.getActiveIdentificationNum());
        paramScore.setParamValue(selectedMinFragmentMzOption + " - " + selectedMaxFragmentMzOption + "");
        parameterScoreSet.add(paramScore);
        sageParameter.setMaxFragmentMz(selectedMaxFragmentMzOption);
//        System.exit(0);
        return new double[]{selectedMinFragmentMzOption, selectedMaxFragmentMzOption};
    }

    public int optimizeIonMinIndexParameter(SearchingSubDataset optProtDataset, IdentificationParameters oreginaltempIdParam, SearchInputSetting optimisedSearchParameter, SageParameters sageParameters, TreeSet<ParameterScoreModel> parameterScoreSet) throws IOException {
        final ParameterScoreModel paramScore = new ParameterScoreModel();
        paramScore.setParamId("ionMinIndex");
        int selectedOption = sageParameters.getMinIonIndex();
        Map<String, RawScoreModel> resultsMap = Collections.synchronizedMap(new LinkedHashMap<>());
        String msFileName = IoUtil.removeExtension(optProtDataset.getSubMsFile().getName());
        int psmNum = 0;
        for (int i = 0; i <= 5; i++) {
            if (i == selectedOption) {
                continue;
            }
            final String option = "ionMinIndex_" + i;
            final String updatedName = Configurations.DEFAULT_RESULT_NAME + "_" + option + "_" + msFileName;
            sageParameters.setMinIonIndex(i);
            final int j = i;

            Future<RawScoreModel> f = MainUtilities.getExecutorService().submit(() -> {
                RawScoreModel scoreModel = excuteSearch(optProtDataset, updatedName, option, oreginaltempIdParam, true, optimisedSearchParameter, generatedIdentificationParametersFile);
                return scoreModel;
            });
            try {
                RawScoreModel scoreModel = f.get();
                System.out.println("ionindex : " + i + "  " + scoreModel.isSensitiveChange() + "  current psm " + psmNum + "   " + (((double) scoreModel.getIdPSMNumber() * 1.01) >= psmNum));
//                if (scoreModel.isSensitiveChange() && (((double)scoreModel.getIdPSMNumber()*1.01) >= psmNum)) {
//                    resultsMap.put(j + "", scoreModel);
//                    psmNum = scoreModel.getIdPSMNumber();
//                }
//                else

                if (scoreModel.isAcceptedChange()) {
//                    psmNum = scoreModel.getIdPSMNumber();
                    resultsMap.put(j + "", scoreModel);
                } else if (i > selectedOption) {
                    break;
                }

//                if (scoreModel.getRawFinalScore() >= -0.05 && (((double) scoreModel.getIdPSMNumber()) >= psmNum)) {
//                    psmNum = scoreModel.getIdPSMNumber();
//                    resultsMap.put(j + "", scoreModel);
//                } else if (scoreModel.getIdPSMNumber() < psmNum) {
//                    break;
//                }
            } catch (ExecutionException | InterruptedException ex) {
                ex.printStackTrace();
            }
        }
        if (!resultsMap.isEmpty()) {
            String bestOption = SpectraUtilities.compareScoresSet(resultsMap, optProtDataset.getSubsetSize(), false, true);//  
            selectedOption = Integer.parseInt(bestOption);
            optProtDataset.setActiveScoreModel(resultsMap.get(bestOption));
        }
        paramScore.setScore(optProtDataset.getActiveIdentificationNum());
        paramScore.setParamValue(selectedOption + "");
        parameterScoreSet.add(paramScore);
        sageParameters.setMinIonIndex(selectedOption);
        return selectedOption;
    }

    public boolean optimizeGenerateDecoyParameter(SearchingSubDataset optProtDataset, IdentificationParameters oreginaltempIdParam, SageParameters sageParameters, SearchInputSetting optimisedSearchParameter, TreeSet<ParameterScoreModel> parameterScoreSet) throws IOException {
        Map<String, RawScoreModel> resultsMap = Collections.synchronizedMap(new LinkedHashMap<>());
        final ParameterScoreModel paramScore = new ParameterScoreModel();
        paramScore.setParamId("generateDecoy");
        String msFileName = IoUtil.removeExtension(optProtDataset.getSubMsFile().getName());
        boolean selectedOption = sageParameters.getGenerateDecoys();

        for (int i = 0; i < 2; i++) {
            boolean generateDecoy = (i == 1);
            if (generateDecoy == selectedOption) {
                continue;
            }

            final String option = "generateDecoy_" + generateDecoy;
            final String updatedName = Configurations.DEFAULT_RESULT_NAME + "_" + option + "_" + msFileName;
            sageParameters.setGenerateDecoys(generateDecoy);
            final int j = i;
            Future<RawScoreModel> f = MainUtilities.getExecutorService().submit(() -> {
                RawScoreModel scoreModel = excuteSearch(optProtDataset, updatedName, option, oreginaltempIdParam, true, optimisedSearchParameter, generatedIdentificationParametersFile);
                return scoreModel;
            });
            try {
                RawScoreModel scoreModel = f.get();
                System.out.println("generate decoy result " + generateDecoy + "  " + scoreModel);
                if (scoreModel.getFinalScore()>=0 || scoreModel.getDiffrentInSize()>=0) {
                    resultsMap.put(j + "", scoreModel);
                }
            } catch (ExecutionException | InterruptedException ex) {
                ex.printStackTrace();
            }
        }

        if (!resultsMap.isEmpty()) {
            int bestOption = Integer.parseInt(SpectraUtilities.compareScoresSet(resultsMap, optProtDataset.getSubsetSize(), false, true));
            selectedOption = bestOption == 1;
            double impact = Math.round((double) (resultsMap.get(bestOption + "").getSpectrumMatchResult().size() - optProtDataset.getActiveIdentificationNum()) * 100.0 / (double) optProtDataset.getActiveIdentificationNum()) / 100.0;
            paramScore.setImpact(impact);
            optProtDataset.setActiveScoreModel(resultsMap.get(bestOption + ""));
        }
        paramScore.setScore(optProtDataset.getActiveIdentificationNum());
        paramScore.setParamValue(selectedOption + "");
        parameterScoreSet.add(paramScore);
        sageParameters.setGenerateDecoys(selectedOption);
        return selectedOption;

    }

    public boolean optimizeDeisotopParameter(SearchingSubDataset optProtDataset, IdentificationParameters oreginaltempIdParam, SageParameters sageParameters, SearchInputSetting optimisedSearchParameter, TreeSet<ParameterScoreModel> parameterScoreSet) throws IOException {
        final ParameterScoreModel paramScore = new ParameterScoreModel();
        paramScore.setParamId("Deisotope");
        Map<String, RawScoreModel> resultsMap = Collections.synchronizedMap(new LinkedHashMap<>());

        String msFileName = IoUtil.removeExtension(optProtDataset.getSubMsFile().getName());
        boolean selectedOption = sageParameters.getDeisotope();
        for (int i = 0; i < 2; i++) {
            if (selectedOption == (i == 1)) {
                continue;
            }
            final String option = "Deisotope_" + (i == 1);
            final String updatedName = Configurations.DEFAULT_RESULT_NAME + "_" + option + "_" + msFileName;
            sageParameters.setDeisotope(i == 1);
            final int j = i;
            Future<RawScoreModel> f = MainUtilities.getExecutorService().submit(() -> {
                RawScoreModel scoreModel = excuteSearch(optProtDataset, updatedName, option, oreginaltempIdParam, true, optimisedSearchParameter, generatedIdentificationParametersFile);
                return scoreModel;
            });
            try {
                RawScoreModel scoreModel = f.get();
                System.out.println("deistop score " + scoreModel + "   " + selectedOption + "   " + (i == 1) + "   " + (selectedOption == (i == 1)));
                if (scoreModel.isAcceptedChange()) {
                    resultsMap.put((j==1) + "", scoreModel);
//                resultsMap.put(0 + "", optProtDataset.getCurrentScoreModel());
                }
            } catch (ExecutionException | InterruptedException ex) {
                ex.printStackTrace();
            }
        }

        if (!resultsMap.isEmpty()) {
            String bestOption = SpectraUtilities.compareScoresSet(resultsMap, optProtDataset.getSubsetSize(), false, true);//  
            System.out.println("---->> best option is " + bestOption + "   " + resultsMap.keySet());
            selectedOption = Boolean.parseBoolean(bestOption);
            
                optProtDataset.setActiveScoreModel(resultsMap.get(selectedOption + ""));
           
        }
        paramScore.setScore(optProtDataset.getActiveIdentificationNum());
        paramScore.setParamValue(selectedOption + "");
        parameterScoreSet.add(paramScore);
        sageParameters.setDeisotope(selectedOption);
        return selectedOption;

    }

    public boolean optimizeChimericSpectraParameter(SearchingSubDataset optProtDataset, IdentificationParameters oreginaltempIdParam, SageParameters sageParameters, SearchInputSetting optimisedSearchParameter, TreeSet<ParameterScoreModel> parameterScoreSet) throws IOException {
        final ParameterScoreModel paramScore = new ParameterScoreModel();
        paramScore.setParamId("Chimera");
        Map<String, RawScoreModel> resultsMap = Collections.synchronizedMap(new LinkedHashMap<>());
        String msFileName = IoUtil.removeExtension(optProtDataset.getSubMsFile().getName());
        boolean selectedOption = sageParameters.getChimera();
        for (int i = 0; i < 2; i++) {
            if (selectedOption == (i == 1)) {
                continue;
            }
            final String option = "Chimera_" + (i == 1);
            final String updatedName = Configurations.DEFAULT_RESULT_NAME + "_" + option + "_" + msFileName;
            sageParameters.setChimera(i == 1);
            final int j = i;
            Future<RawScoreModel> f = MainUtilities.getExecutorService().submit(() -> {
                RawScoreModel scoreModel = excuteSearch(optProtDataset, updatedName, option, oreginaltempIdParam, false, optimisedSearchParameter, generatedIdentificationParametersFile);
                return scoreModel;
            });
            try {
                RawScoreModel scoreModel = f.get();
                if (scoreModel.isAcceptedChange()) {
                    resultsMap.put(j + "", scoreModel);
                }
            } catch (ExecutionException | InterruptedException ex) {
                ex.printStackTrace();
            }
        }
        if (!resultsMap.isEmpty()) {
            String bestOption = SpectraUtilities.compareScoresSet(resultsMap, optProtDataset.getSubsetSize(), false, true);//  
            selectedOption = Integer.parseInt(bestOption) == 1;
            optProtDataset.setActiveScoreModel(resultsMap.get(bestOption));
        }
        paramScore.setScore(optProtDataset.getActiveIdentificationNum());
        paramScore.setParamValue(selectedOption + "");
        parameterScoreSet.add(paramScore);
        sageParameters.setChimera(selectedOption);
        return selectedOption;

    }

    public boolean optimizeWideWindowParameter(SearchingSubDataset optProtDataset, IdentificationParameters oreginaltempIdParam, SageParameters sageParameters, SearchInputSetting optimisedSearchParameter, TreeSet<ParameterScoreModel> parameterScoreSet) throws IOException {

        final ParameterScoreModel paramScore = new ParameterScoreModel();
        paramScore.setParamId("WideWindow");
        Map<String, RawScoreModel> resultsMap = Collections.synchronizedMap(new LinkedHashMap<>());
        String msFileName = IoUtil.removeExtension(optProtDataset.getSubMsFile().getName());
        boolean selectedOption = sageParameters.getWideWindow();

        for (int i = 0; i < 2; i++) {
            if (selectedOption == (i == 1)) {
                continue;
            }
            final String option = "WideWindow_" + (i == 1);
            final String updatedName = Configurations.DEFAULT_RESULT_NAME + "_" + option + "_" + msFileName;
            sageParameters.setWideWindow(i == 1);
            final int j = i;
            Future<RawScoreModel> f = MainUtilities.getExecutorService().submit(() -> {

                RawScoreModel scoreModel = excuteSearch(optProtDataset, updatedName, option, oreginaltempIdParam, true, optimisedSearchParameter, generatedIdentificationParametersFile);
                return scoreModel;
            });
            try {
                RawScoreModel scoreModel = f.get();
                if (scoreModel.isAcceptedChange()) {
                    resultsMap.put(j + "", scoreModel);

                }
            } catch (ExecutionException | InterruptedException ex) {
                ex.printStackTrace();
            }
        }

        if (!resultsMap.isEmpty()) {
            String bestOption = SpectraUtilities.compareScoresSet(resultsMap, optProtDataset.getSubsetSize(), false, true);//  
            selectedOption = Integer.parseInt(bestOption) == 1;
//            optProtDataset.setActiveScoreModel(resultsMap.get(bestOption));
            paramScore.setComments("Slow processing");
        }
        paramScore.setScore(optProtDataset.getActiveIdentificationNum());
        paramScore.setParamValue(selectedOption + "");
        parameterScoreSet.add(paramScore);
        sageParameters.setWideWindow(false);
        return selectedOption;

    }

    public boolean optimizePredectRetentionTimeParameter(SearchingSubDataset optProtDataset, IdentificationParameters oreginaltempIdParam, SageParameters sageParameters, SearchInputSetting optimisedSearchParameter, TreeSet<ParameterScoreModel> parameterScoreSet) throws IOException {
        final ParameterScoreModel paramScore = new ParameterScoreModel();
        paramScore.setParamId("PredictRt");
        Map<String, RawScoreModel> resultsMap = Collections.synchronizedMap(new LinkedHashMap<>());
        String msFileName = IoUtil.removeExtension(optProtDataset.getSubMsFile().getName());
        boolean selectedOption = sageParameters.getPredictRt();

        for (int i = 0; i < 2; i++) {
            if (selectedOption == (i == 1)) {
                continue;
            }
            final String option = "PredictRt_" + (i == 1);
            final String updatedName = Configurations.DEFAULT_RESULT_NAME + "_" + option + "_" + msFileName;
            sageParameters.setPredictRt(i == 1);
            final int j = i;

            Future<RawScoreModel> f = MainUtilities.getExecutorService().submit(() -> {

                RawScoreModel scoreModel = excuteSearch(optProtDataset, updatedName, option, oreginaltempIdParam, false, optimisedSearchParameter, generatedIdentificationParametersFile);
                return scoreModel;
            });
            try {
                RawScoreModel scoreModel = f.get();
                if (scoreModel.isAcceptedChange()) {
                    resultsMap.put(j + "", scoreModel);
                }
            } catch (ExecutionException | InterruptedException ex) {
                ex.printStackTrace();
            }
        }
        sageParameters.setPredictRt(selectedOption);
        if (!resultsMap.isEmpty()) {
            String bestOption = SpectraUtilities.compareScoresSet(resultsMap, optProtDataset.getSubsetSize(), false, true);//  
            selectedOption = Integer.parseInt(bestOption) == 1;
            optProtDataset.setActiveScoreModel(resultsMap.get(bestOption));
            sageParameters.setPredictRt(selectedOption);
        }
        paramScore.setScore(optProtDataset.getActiveIdentificationNum());
        paramScore.setParamValue(selectedOption + "");
        parameterScoreSet.add(paramScore);
        sageParameters.setPredictRt(selectedOption);
        return selectedOption;

    }

    public int[] optimizeNumberOfPeakParameter(SearchingSubDataset optProtDataset, IdentificationParameters oreginaltempIdParam, SageParameters sageParameter, SearchInputSetting optimisedSearchParameter, TreeSet<ParameterScoreModel> parameterScoreSet) throws IOException {
        final ParameterScoreModel paramScore = new ParameterScoreModel();
        paramScore.setParamId("#Peaks");
        String msFileName = IoUtil.removeExtension(optProtDataset.getSubMsFile().getName());
        int selectedMaxPeaksNumberOption = sageParameter.getMaxPeaks();
        int selectedMinPeaksNumberOption = sageParameter.getMinPeaks();
        Map<String, RawScoreModel> resultsMap = Collections.synchronizedMap(new LinkedHashMap<>());
        sageParameter.setMaxPeaks(selectedMaxPeaksNumberOption);
        double topScore=0;
        for (int i = 10; i <= 20; i++) {
            if (i == selectedMinPeaksNumberOption) {
                continue;
            }
            sageParameter.setMinPeaks(i);
            final String option = "minPeaks_" + i;
            final String updatedName = Configurations.DEFAULT_RESULT_NAME + "_" + option + "_" + msFileName;
            int j = i;

            Future<RawScoreModel> f = MainUtilities.getExecutorService().submit(() -> {
                RawScoreModel scoreModel = excuteSearch(optProtDataset, updatedName, option, oreginaltempIdParam, true, optimisedSearchParameter, generatedIdentificationParametersFile);
                return scoreModel;
            });
            try {
                RawScoreModel scoreModel = f.get();
//                if (scoreModel.isSensitiveChange() && scoreModel.getSpectrumMatchResult().size() >= optProtDataset.getCurrentScoreModel().getSpectrumMatchResult().size()) {
//                    System.out.println("add as selected score " + j);
                if (scoreModel.isAcceptedChange() && scoreModel.getIdPSMNumber()>topScore) {
                    resultsMap.put(j + "", scoreModel);
                    topScore=scoreModel.getIdPSMNumber();
//                    selectedV = j;
                } else {
                    break;
                }
//                if (scoreModel.isSensitiveChange() && scoreModel.getIdPSMNumber() >= optProtDataset.getCurrentScoreModel().getIdPSMNumber()) { //|| scoreModel.isSameData()
//                    if (prescoreModel == null) {
//                        prescoreModel = scoreModel;
//                    } else if (prescoreModel.getFinalScore() < scoreModel.getFinalScore()) {
//                        prescoreModel = scoreModel;
//                    } else {
//                        continue;
//                    }
//                    resultsMap.put(j + "", scoreModel);
//                } else if (j > selectedMinPeaksNumberOption || scoreModel.getIdPSMNumber() < optProtDataset.getCurrentScoreModel().getIdPSMNumber()) {
//                    break;
//                }

            } catch (ExecutionException | InterruptedException ex) {
                ex.printStackTrace();
            }
        }

        if (!resultsMap.isEmpty()) {
            String bestOption = SpectraUtilities.compareScoresSet(resultsMap, optProtDataset.getSubsetSize(), false, true);//            
            selectedMinPeaksNumberOption = Integer.parseInt(bestOption);
            optProtDataset.setActiveScoreModel(resultsMap.get(bestOption));

        }
        resultsMap.clear();
        sageParameter.setMinPeaks(selectedMinPeaksNumberOption);
        for (int i = 100; i <= 200;) {
            if (i == selectedMaxPeaksNumberOption) {
                i += 10;
                continue;
            }
            sageParameter.setMaxPeaks(i);
            final String option = "maxPeaks_" + i;
            final String updatedName = Configurations.DEFAULT_RESULT_NAME + "_" + option + "_" + msFileName;
            int j = i;
            Future<RawScoreModel> f = MainUtilities.getExecutorService().submit(() -> {
                RawScoreModel scoreModel = excuteSearch(optProtDataset, updatedName, option, oreginaltempIdParam, false, optimisedSearchParameter, generatedIdentificationParametersFile);
                return scoreModel;
            });
            try {
                RawScoreModel scoreModel = f.get();
                System.out.println("max # "+j+" vs "+selectedMaxPeaksNumberOption+"   "+scoreModel);
                if ((scoreModel.isAcceptedChange())) {
                    resultsMap.put(j + "", scoreModel);
                } 
//                else if (j > selectedMaxPeaksNumberOption) {
//                    break;
//                }
            } catch (ExecutionException | InterruptedException ex) {
                ex.printStackTrace();
            }
            i += 10;
        }

        if (!resultsMap.isEmpty()) {
            selectedMaxPeaksNumberOption = Integer.parseInt(SpectraUtilities.compareScoresSet(resultsMap, optProtDataset.getSubsetSize(), false, true));
            optProtDataset.setActiveScoreModel(resultsMap.get(selectedMaxPeaksNumberOption + ""));
        }
        paramScore.setScore(optProtDataset.getActiveIdentificationNum());
        paramScore.setParamValue(selectedMinPeaksNumberOption + "-" + selectedMaxPeaksNumberOption + "");
        parameterScoreSet.add(paramScore);
        sageParameter.setMaxPeaks(selectedMaxPeaksNumberOption);
        return new int[]{selectedMinPeaksNumberOption, selectedMaxPeaksNumberOption};
    }

    public int optimizeMinMatchedPeaksParameter(SearchingSubDataset optProtDataset, IdentificationParameters oreginaltempIdParam, SageParameters sageParameters, SearchInputSetting optimisedSearchParameter, TreeSet<ParameterScoreModel> parameterScoreSet) throws IOException {
        final ParameterScoreModel paramScore = new ParameterScoreModel();
        paramScore.setParamId("minMatchedPeaks");
        int selectedOption = sageParameters.getMinMatchedPeaks();
        Map<String, RawScoreModel> resultsMap = Collections.synchronizedMap(new LinkedHashMap<>());
        String msFileName = IoUtil.removeExtension(optProtDataset.getSubMsFile().getName());
        for (int i = 3; i <= 6; i++) {
            if (i == selectedOption) {
                continue;
            }
            final String option = "minMatchedPeaks_" + i;
            final String updatedName = Configurations.DEFAULT_RESULT_NAME + "_" + option + "_" + msFileName;
            sageParameters.setMinMatchedPeaks(i);
            final int j = i;
            Future<RawScoreModel> f = MainUtilities.getExecutorService().submit(() -> {
                RawScoreModel scoreModel = excuteSearch(optProtDataset, updatedName, option, oreginaltempIdParam, true, optimisedSearchParameter, generatedIdentificationParametersFile);
                return scoreModel;
            });
            try {
                RawScoreModel scoreModel = f.get();
                if (scoreModel.isAcceptedChange() || j<selectedOption){// && scoreModel.getIdPSMNumber() >= optProtDataset.getActiveIdentificationNum()) {
                    resultsMap.put(j + "", scoreModel);
                } else if (j > selectedOption) {
                    break;
                }

            } catch (ExecutionException | InterruptedException ex) {
                ex.printStackTrace();
            }
        }
        if (!resultsMap.isEmpty()) {
              resultsMap.put(selectedOption+ "", optProtDataset.getCurrentScoreModel());
            String bestOption = SpectraUtilities.compareScoresSet(resultsMap, optProtDataset.getSubsetSize(), true, true);//  
            selectedOption = Integer.parseInt(bestOption);
            optProtDataset.setActiveScoreModel(resultsMap.get(bestOption));
        }
        paramScore.setScore(optProtDataset.getActiveIdentificationNum());
        paramScore.setParamValue(selectedOption + "");
        parameterScoreSet.add(paramScore);
        sageParameters.setMinMatchedPeaks(selectedOption);
        return selectedOption;
    }

    public int optimizeMaxFragmentChargeParameter(SearchingSubDataset optProtDataset, IdentificationParameters oreginaltempIdParam, SageParameters sageParameters, SearchInputSetting optimisedSearchParameter, TreeSet<ParameterScoreModel> parameterScoreSet) throws IOException {
        final ParameterScoreModel paramScore = new ParameterScoreModel();
        paramScore.setParamId("maxFragmentCharge");
        int selectedOption = 1;
        if (sageParameters.getMaxFragmentCharge() != null) {
            selectedOption = sageParameters.getMaxFragmentCharge();
        }
        Map<String, RawScoreModel> resultsMap = Collections.synchronizedMap(new LinkedHashMap<>());
        String msFileName = IoUtil.removeExtension(optProtDataset.getSubMsFile().getName());
        for (int i = 1; i <= 5; i++) {
            if (i == selectedOption) {
                continue;
            }
            final String option = "maxFragmentCharge_" + i;
            final String updatedName = Configurations.DEFAULT_RESULT_NAME + "_" + option + "_" + msFileName;
            sageParameters.setMaxFragmentCharge(i);
            final int j = i;

            Future<RawScoreModel> f = MainUtilities.getExecutorService().submit(() -> {
                RawScoreModel scoreModel = excuteSearch(optProtDataset, updatedName, option, oreginaltempIdParam, false, optimisedSearchParameter, generatedIdentificationParametersFile);
                return scoreModel;
            });
            try {
                RawScoreModel scoreModel = f.get();
                if (scoreModel.getFinalScore() > 0) {
                    resultsMap.put(j + "", scoreModel);
                } else if (j > selectedOption) {
                    break;
                }

            } catch (ExecutionException | InterruptedException ex) {
                ex.printStackTrace();
            }
        }

        if (!resultsMap.isEmpty()) {
            String bestOption = SpectraUtilities.compareScoresSet(resultsMap, optProtDataset.getSubsetSize(), false, true);//  
            selectedOption = Integer.parseInt(bestOption);
            optProtDataset.setActiveScoreModel(resultsMap.get(bestOption));
        }
        paramScore.setScore(optProtDataset.getActiveIdentificationNum());
        paramScore.setParamValue(selectedOption + "");
        parameterScoreSet.add(paramScore);
        sageParameters.setMaxFragmentCharge(selectedOption);
        return selectedOption;
    }

    public void runReferenceRun(SearchingSubDataset optProtDataset, IdentificationParameters oreginaltempIdParam, SearchInputSetting optimisedSearchParameter) throws IOException {

        String msFileName = IoUtil.removeExtension(optProtDataset.getSubMsFile().getName());
        final String option = "reference_run_default_";
        final String updatedName = Configurations.DEFAULT_RESULT_NAME + "_" + option + "_" + msFileName;
        Future<RawScoreModel> f = MainUtilities.getExecutorService().submit(() -> {
            RawScoreModel scoreModel = excuteSearch(optProtDataset, updatedName, option, oreginaltempIdParam, true, optimisedSearchParameter, generatedIdentificationParametersFile);
            return scoreModel;
        });
        try {
            RawScoreModel scoreModel = f.get();
            optProtDataset.setActiveScoreModel(scoreModel);
        } catch (ExecutionException | InterruptedException ex) {
            ex.printStackTrace();
        }
    }

}
