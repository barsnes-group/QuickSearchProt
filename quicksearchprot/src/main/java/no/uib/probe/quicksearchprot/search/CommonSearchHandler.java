package no.uib.probe.quicksearchprot.search;

import com.compomics.util.experiment.biology.enzymes.Enzyme;
import com.compomics.util.experiment.biology.enzymes.EnzymeFactory;
import com.compomics.util.experiment.biology.ions.impl.PeptideFragmentIon;
import com.compomics.util.experiment.biology.modifications.Modification;
import com.compomics.util.experiment.biology.modifications.ModificationCategory;
import com.compomics.util.experiment.biology.modifications.ModificationFactory;
import com.compomics.util.experiment.identification.Advocate;
import com.compomics.util.io.IoUtil;
import com.compomics.util.parameters.identification.IdentificationParameters;
import com.compomics.util.parameters.identification.search.DigestionParameters;
import com.compomics.util.parameters.identification.search.SearchParameters;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import no.uib.probe.quicksearchprot.configurations.Configurations;
import no.uib.probe.quicksearchprot.dataset.model.SearchingSubDataset;
import no.uib.probe.quicksearchprot.model.ParameterScoreModel;
import no.uib.probe.quicksearchprot.model.RawScoreModel;
import no.uib.probe.quicksearchprot.model.SearchInputSetting;
import no.uib.probe.quicksearchprot.model.SortedPTMs;
import no.uib.probe.quicksearchprot.util.MainUtilities;
import no.uib.probe.quicksearchprot.util.SpectraUtilities;

/**
 *
 * @author yfa041
 */
public abstract class CommonSearchHandler {

    /**
     * The compomics PTM factory.
     */
    private final ModificationFactory ptmFactory = ModificationFactory.getInstance();

//    /**
//     * Optimize digestion enzyme
//     *
//     * @param optProtDataset
//     * @param identificationParametersFile
//     * @param optimisedSearchParameter
//     * @param parameterScoreSet
//     * @return
//     * @throws IOException
//     */
//    public String CalculateEnzymeComparisonsBasedThreshold(SearchingSubDataset optProtDataset, File identificationParametersFile, SearchInputSetting optimisedSearchParameter) throws IOException {
//
//        IdentificationParameters oreginaltempIdParam = IdentificationParameters.getIdentificationParameters(identificationParametersFile);
//        Map<RawScoreModel, String> resultsMapI = Collections.synchronizedMap(new TreeMap<>());
//        String msFileName = IoUtil.removeExtension(optProtDataset.getSubMsFile().getName());
//        String bestPerformanceEnzyme = "Trypsin";
//        //optimise enzyme  
//
//        //rerernce search with    Chymotrypsin (no P rule)   delete after get the data
//        boolean done = false;
//        ArrayList<Enzyme> enz = EnzymeFactory.getInstance().getEnzymes();
//        while (true) {
//            for (Enzyme enzyme : enz) {
//                if (enzyme.getName().replace(" ", "").equalsIgnoreCase("Trypsin(noPrule)")) {//|| enzyme.getName().replace(" ", "").contains("(noPrule)")
//                    continue;
//                }
//                oreginaltempIdParam.getSearchParameters().getDigestionParameters().clearEnzymes();
//                oreginaltempIdParam.getSearchParameters().getDigestionParameters().addEnzyme(enzyme);
//                oreginaltempIdParam.getSearchParameters().getDigestionParameters().setnMissedCleavages(enzyme.getName(), 2);
//                final String option = enzyme.getName();
//                final String updatedName = Configurations.DEFAULT_RESULT_NAME + "_" + option + "_" + msFileName;
//                Future<RawScoreModel> f = MainUtilities.getExecutorService().submit(() -> {
//                    RawScoreModel scoreModel = excuteSearch(optProtDataset, updatedName, option, oreginaltempIdParam, true, optimisedSearchParameter, identificationParametersFile);
//                    return scoreModel;
//                });
//                try {
//                    RawScoreModel scoreModel = f.get();
//                    System.out.println(done + " Enzyme: " + enzyme.getName() + "    Score: " + scoreModel.getFinalScore() + "    #PSMs: " + scoreModel.getIdPSMNumber() + "   " + "   ");
//
//                    resultsMapI.put(scoreModel, option);
//                } catch (InterruptedException | ExecutionException ex) {
//                    ex.printStackTrace();
//                }
//            }
//
//            if (done) {
//                SpectraUtilities.updateComparisonsThreshold(optProtDataset);
//                break;
//            }
//            TreeMap<RawScoreModel, String> sortMap = new TreeMap<>(resultsMapI);
//            optProtDataset.setActiveScoreModel(sortMap.firstKey());
//            bestPerformanceEnzyme = sortMap.lastEntry().getValue();
//            done = true;
////            TreeMap<Integer, RawScoreModel> sortMap2 = new TreeMap<>();
////            for (RawScoreModel key : resultsMapI.keySet()) {
////                sortMap2.put(key.getIdPSMNumber(), key);
////            }
////             optProtDataset.setActiveScoreModel(sortMap2.firstEntry().getValue());
////            bestPerformanceEnzyme = sortMap.lastEntry().getValue();
//            System.out.println("lowest enzyme score is " + sortMap.firstEntry().getValue() + "   bestEnzyme " + bestPerformanceEnzyme);
//
//            //optimize specifty 
//        }
//        return bestPerformanceEnzyme;
//
//    }
    public String optimizeDigestionCleavageParameter(SearchingSubDataset optProtDataset, File identificationParametersFile, SearchInputSetting optimisedSearchParameter, TreeSet<ParameterScoreModel> parameterScoreSet) throws IOException {
        final ParameterScoreModel paramScore = new ParameterScoreModel();
        paramScore.setParamId("CleavageParameter");
        IdentificationParameters oreginaltempIdParam = IdentificationParameters.getIdentificationParameters(identificationParametersFile);
        String selectedOption = oreginaltempIdParam.getSearchParameters().getDigestionParameters().getCleavageParameter().name();
        int idRate = optProtDataset.getActiveIdentificationNum();
        Map<String, RawScoreModel> resultsMap = Collections.synchronizedMap(new LinkedHashMap<>());
        String msFileName = IoUtil.removeExtension(optProtDataset.getSubMsFile().getName());
        RawScoreModel oreginalScore = new RawScoreModel("CleavageParameter");
        oreginalScore.setIdPSMNumber(idRate);
        String[] cleavageParameters = new String[]{"wholeProtein", "unSpecific"};

        resultsMap.put(selectedOption, oreginalScore);
        int spectraCounter = optProtDataset.getActiveIdentificationNum();
        for (String cleavageParameter : cleavageParameters) {
            IdentificationParameters tempIdParam = IdentificationParameters.getIdentificationParameters(identificationParametersFile);
            tempIdParam.getSearchParameters().getDigestionParameters().setCleavageParameter(DigestionParameters.CleavageParameter.valueOf(cleavageParameter));
            if (cleavageParameter.equalsIgnoreCase("enzyme")) {
                tempIdParam.getSearchParameters().getDigestionParameters().addEnzyme(EnzymeFactory.getInstance().getEnzyme("Trypsin"));
            } else {
                tempIdParam.getSearchParameters().getDigestionParameters().clearEnzymes();
            }
            final String option = cleavageParameter;
            final String updatedName = Configurations.DEFAULT_RESULT_NAME + "_" + option + "_" + msFileName;

            Future<RawScoreModel> f = MainUtilities.getExecutorService().submit(() -> {
                RawScoreModel scoreModel = excuteSearch(optProtDataset, updatedName, option, tempIdParam, true, optimisedSearchParameter, identificationParametersFile);
                return scoreModel;
            });
            try {
                RawScoreModel scoreModel = f.get();
                if (scoreModel.getFinalScore() > 1) {
                    spectraCounter = Math.max(spectraCounter, scoreModel.getSpectrumMatchResult().size());
                    resultsMap.put(option, f.get());

                }
            } catch (ExecutionException | InterruptedException ex) {
                ex.printStackTrace();
            }
        }

        int total = optProtDataset.getActiveIdentificationNum();
        if (!resultsMap.isEmpty()) {
            String bestScore = SpectraUtilities.compareScoresSet(resultsMap, optProtDataset.getSubsetSize(), false, optimisedSearchParameter.getSelectedSearchEngine().getName().equalsIgnoreCase(Advocate.sage.getName()));
            selectedOption = bestScore;
            if (selectedOption.equalsIgnoreCase("unSpecific")) {
                double impact = Math.round((double) (resultsMap.get(selectedOption).getSpectrumMatchResult().size() - optProtDataset.getActiveIdentificationNum()) * 100.0 / (double) optProtDataset.getActiveIdentificationNum()) / 100.0;
                paramScore.setImpact(impact);
                total = resultsMap.get(selectedOption).getSpectrumMatchResult().size();
                paramScore.setComments("Extremely slow processing");
            }
        }
        paramScore.setScore(total);
        paramScore.setParamValue(selectedOption);
        parameterScoreSet.add(paramScore);
        return selectedOption;

    }

    /**
     * Optimize digestion enzyme
     *
     * @param optProtDataset
     * @param identificationParametersFile
     * @param optimisedSearchParameter
     * @param parameterScoreSet
     * @return
     * @throws IOException
     */
    public String[] optimizeEnzymeParameter(SearchingSubDataset optProtDataset, File identificationParametersFile, SearchInputSetting optimisedSearchParameter, TreeSet<ParameterScoreModel> parameterScoreSet) throws IOException {
        final ParameterScoreModel paramScore = new ParameterScoreModel();
        paramScore.setParamId("Enzyme");
        String[] values = new String[3];
        IdentificationParameters oreginaltempIdParam = IdentificationParameters.getIdentificationParameters(identificationParametersFile);
        values[0] = oreginaltempIdParam.getSearchParameters().getDigestionParameters().getEnzymes().get(0).getName();
        values[1] = oreginaltempIdParam.getSearchParameters().getDigestionParameters().getSpecificity(values[0]).name();
        int missedClavageNumb = oreginaltempIdParam.getSearchParameters().getDigestionParameters().getnMissedCleavages(values[0]);
        values[2] = "" + missedClavageNumb;
        System.out.println("Enzyme is " + values[0]);
        Map<String, RawScoreModel> resultsMapI = Collections.synchronizedMap(new LinkedHashMap<>());
        String msFileName = IoUtil.removeExtension(optProtDataset.getSubMsFile().getName());
//        double speciftyThreshold = optProtDataset.getComparisonsThreshold(3);
        //optimise enzyme  

        //rerernce search with    Chymotrypsin (no P rule)   delete after get the data
//        {
//            Enzyme enzyme = EnzymeFactory.getInstance().getEnzyme("CNBr");
//            oreginaltempIdParam.getSearchParameters().getDigestionParameters().clearEnzymes();
//            oreginaltempIdParam.getSearchParameters().getDigestionParameters().addEnzyme(enzyme);
//            oreginaltempIdParam.getSearchParameters().getDigestionParameters().setnMissedCleavages(enzyme.getName(), missedClavageNumb);
//            final String option = enzyme.getName();
//            final String updatedName = Configurations.DEFAULT_RESULT_NAME + "_" + option + "_" + msFileName;
//            Future<RawScoreModel> f = MainUtilities.getExecutorService().submit(() -> {
//                RawScoreModel scoreModel = excuteSearch(optProtDataset, updatedName, option, oreginaltempIdParam, true, optimisedSearchParameter, identificationParametersFile, false);
//                return scoreModel;
//            });
//            try {
//                RawScoreModel scoreModel = f.get();
//                System.out.println("Enzyme: " + enzyme.getName() + "    Score: " + scoreModel.getFinalScore() + "    #PSMs: " + scoreModel.getIdPSMNumber() + "   " + scoreModel + "   ");
//                optProtDataset.setActiveScoreModel(scoreModel);
//            } catch (InterruptedException | ExecutionException ex) {
//                ex.printStackTrace();
//            }
//        }
        if (optimisedSearchParameter.isOptimizeEnzymeParameter()) {
            for (Enzyme enzyme : EnzymeFactory.getInstance().getEnzymes()) {
                if (enzyme.getName().replace(" ", "").equalsIgnoreCase("Trypsin(noPrule)")) {
                    continue;
                }
                oreginaltempIdParam.getSearchParameters().getDigestionParameters().clearEnzymes();
                oreginaltempIdParam.getSearchParameters().getDigestionParameters().addEnzyme(enzyme);
                oreginaltempIdParam.getSearchParameters().getDigestionParameters().setnMissedCleavages(enzyme.getName(), missedClavageNumb);
                final String option = enzyme.getName();
                final String updatedName = Configurations.DEFAULT_RESULT_NAME + "_" + option + "_" + msFileName;
                Future<RawScoreModel> f = MainUtilities.getExecutorService().submit(() -> {
                    RawScoreModel scoreModel = excuteSearch(optProtDataset, updatedName, option, oreginaltempIdParam, true, optimisedSearchParameter, identificationParametersFile);
                    return scoreModel;
                });
                try {
                    RawScoreModel scoreModel = f.get();
                    System.out.println("Enzyme: " + enzyme.getName() + "    Score: " + scoreModel.getFinalScore() + "    #PSMs: " + scoreModel.getIdPSMNumber() + " vs   " + optProtDataset.getCurrentScoreModel().getIdPSMNumber() + "   " + scoreModel + "   ");

                    if (scoreModel.isSensitiveChange()) {
                        resultsMapI.put(option, scoreModel);
                    }
                } catch (InterruptedException | ExecutionException ex) {
                    ex.printStackTrace();
                }
            }
            if (!resultsMapI.isEmpty()) {

                String enzymeName = SpectraUtilities.compareScoresSet(resultsMapI, optProtDataset.getSubsetSize(), false, optimisedSearchParameter.getSelectedSearchEngine().getName().equalsIgnoreCase(Advocate.sage.getName()));
                values[0] = enzymeName;
                optProtDataset.setActiveScoreModel(resultsMapI.get(enzymeName));
            } else {
                System.out.println("case 3");
//                values[0] = "Trypsin";
            }
            //optimize specifty 
            oreginaltempIdParam.getSearchParameters().getDigestionParameters().clearEnzymes();
            oreginaltempIdParam.getSearchParameters().getDigestionParameters().addEnzyme(EnzymeFactory.getInstance().getEnzyme(values[0]));
            oreginaltempIdParam.getSearchParameters().getDigestionParameters().setnMissedCleavages(values[0], missedClavageNumb);
            oreginaltempIdParam.getSearchParameters().getDigestionParameters().setSpecificity(values[0], DigestionParameters.Specificity.valueOf(values[1]));
        }
        resultsMapI.clear();
        if (optimisedSearchParameter.isOptimizeSpecificityParameter()) {
            for (int i = 0; i < DigestionParameters.Specificity.values().length; i++) {
                final String option = DigestionParameters.Specificity.getSpecificity(i).name();
                oreginaltempIdParam.getSearchParameters().getDigestionParameters().setSpecificity(values[0], DigestionParameters.Specificity.getSpecificity(i));
                final String updatedName = Configurations.DEFAULT_RESULT_NAME + "_" + option + "_" + msFileName;
                Future<RawScoreModel> f = MainUtilities.getExecutorService().submit(() -> {
                    RawScoreModel scoreModel = excuteSearch(optProtDataset, updatedName, option, oreginaltempIdParam, true, optimisedSearchParameter, identificationParametersFile);
                    return scoreModel;
                });
                try {
                    RawScoreModel scoreModel = f.get();
                    if (scoreModel.getFinalScore() > 0) {
                        resultsMapI.put(option, scoreModel);
                    }

                } catch (ExecutionException | InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
            if (!resultsMapI.isEmpty()) {
                String specifty = SpectraUtilities.compareScoresSet(resultsMapI, optProtDataset.getSubsetSize(), false, optimisedSearchParameter.getSelectedSearchEngine().getName().equalsIgnoreCase(Advocate.sage.getName()));
                values[1] = specifty;
                double impact = Math.round((double) (resultsMapI.get(specifty).getSpectrumMatchResult().size() - optProtDataset.getActiveIdentificationNum()) * 100.0 / (double) optProtDataset.getActiveIdentificationNum()) / 100.0;
                paramScore.setImpact(impact);
                optProtDataset.setActiveScoreModel(resultsMapI.get(specifty));

            }
            oreginaltempIdParam.getSearchParameters().getDigestionParameters().setSpecificity(values[0], DigestionParameters.Specificity.valueOf("specific"));
        }

///number op missed cleavage
        resultsMapI.clear();
        if (optimisedSearchParameter.isOptimizeMaxMissCleavagesParameter()) {
            for (int i = 0; i < 5; i++) {
                oreginaltempIdParam.getSearchParameters().getDigestionParameters().setnMissedCleavages(values[0], i);
                final String option = "missedCleavages_" + i;
                final String updatedName = Configurations.DEFAULT_RESULT_NAME + "_" + option + "_" + msFileName;

                Future<RawScoreModel> f = MainUtilities.getExecutorService().submit(() -> {
                    RawScoreModel scoreModel = excuteSearch(optProtDataset, updatedName, option, oreginaltempIdParam, true, optimisedSearchParameter, identificationParametersFile);
                    return scoreModel;
                });
                try {
                    RawScoreModel scoreModel = f.get();
//                    System.out.println("mmissed " + i + "--->" + scoreModel.getFinalScore() + " \\t  "+scoreModel.getIdPSMNumber() );
//                System.exit(0);
                    if (scoreModel.getFinalScore() > 0) {
                        if (i < missedClavageNumb && scoreModel.getSharedDataSize() == optProtDataset.getCurrentScoreModel().getIdPSMNumber()) {
                            resultsMapI.put(i + "", scoreModel);
                        } else if (i > missedClavageNumb && scoreModel.getSharedDataSize() == optProtDataset.getCurrentScoreModel().getIdPSMNumber() && scoreModel.getIdPSMNumber() >= 1.05 * optProtDataset.getCurrentScoreModel().getIdPSMNumber()) {
                            resultsMapI.put(i + "", scoreModel);
                        } else if (i > missedClavageNumb) {
                            break;
                        }
                    } else if (i > missedClavageNumb) {
//                        break;
                    }
                } catch (ExecutionException | InterruptedException ex) {
                    ex.printStackTrace();
                }
            }

            String numbOfMissedCleavage = missedClavageNumb + "";
            if (!resultsMapI.isEmpty()) {
                numbOfMissedCleavage = (SpectraUtilities.compareScoresSet(resultsMapI, optProtDataset.getSubsetSize(), false, optimisedSearchParameter.getSelectedSearchEngine().getName().equalsIgnoreCase(Advocate.sage.getName())));
                double impact = Math.round((double) (resultsMapI.get(numbOfMissedCleavage).getSpectrumMatchResult().size() - optProtDataset.getActiveIdentificationNum()) * 100.0 / (double) optProtDataset.getActiveIdentificationNum()) / 100.0;
                paramScore.setImpact(impact);
                optProtDataset.setActiveScoreModel(resultsMapI.get(numbOfMissedCleavage));
            }
            values[2] = numbOfMissedCleavage;
        }
        paramScore.setScore(optProtDataset.getActiveIdentificationNum());
        paramScore.setParamValue(Arrays.asList(values).toString());
        parameterScoreSet.add(paramScore);
        return values;

    }

    /**
     *
     * @param optProtDataset
     * @param identificationParametersFile
     * @param optimisedSearchParameter
     * @param parameterScoreSet
     * @return
     * @throws IOException
     */
    public String optimizeFragmentIonTypesParameter(SearchingSubDataset optProtDataset, File identificationParametersFile, SearchInputSetting optimisedSearchParameter, TreeSet<ParameterScoreModel> parameterScoreSet) throws IOException {
        final ParameterScoreModel paramScore = new ParameterScoreModel();
        paramScore.setParamId("FragmentIons");
        IdentificationParameters oreginaltempIdParam = IdentificationParameters.getIdentificationParameters(identificationParametersFile);
        Map<String, RawScoreModel> resultsMap = Collections.synchronizedMap(new LinkedHashMap<>());
        String msFileName = IoUtil.removeExtension(optProtDataset.getSubMsFile().getName());
        String selectedOption = oreginaltempIdParam.getSearchParameters().getForwardIons() + "-" + oreginaltempIdParam.getSearchParameters().getRewindIons();
        IdentificationParameters tempIdParam = IdentificationParameters.getIdentificationParameters(identificationParametersFile);
        SearchParameters tempSearchParameters = tempIdParam.getSearchParameters();
        ArrayList<Integer> selectedForwardIons = tempSearchParameters.getForwardIons();
        String[] forwardIons = new String[]{"b", "a", "c"};
        String[] rewindIons = new String[]{"y", "x", "z"};

        for (String forwardIon : forwardIons) {
            selectedForwardIons.clear();
            Integer forwardIonType = PeptideFragmentIon.getIonType(forwardIon);
            selectedForwardIons.add(forwardIonType);
            for (String rewindIon : rewindIons) {
                Integer rewindIonType = PeptideFragmentIon.getIonType(rewindIon);
                ArrayList<Integer> selectedRewindIons = new ArrayList<>();
                selectedRewindIons.add(rewindIonType);
                tempSearchParameters.setRewindIons(selectedRewindIons);
                String option = selectedForwardIons + "-" + selectedRewindIons;
                if (option.equalsIgnoreCase(selectedOption)) {
                    continue;
                }

                final String updatedName = Configurations.DEFAULT_RESULT_NAME + "_" + option + "_" + msFileName;

                Future<RawScoreModel> f = MainUtilities.getExecutorService().submit(() -> {
                    RawScoreModel scoreModel = excuteSearch(optProtDataset, updatedName, option, tempIdParam, false, optimisedSearchParameter, identificationParametersFile);
                    return scoreModel;
                });
                try {
                    RawScoreModel scoreModel = f.get();
//                    System.out.println("ion types "+option+"  "+scoreModel);

                    if (scoreModel.getFinalScore()>0) {
                        resultsMap.put(option, scoreModel);
                    }
                } catch (ExecutionException | InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }

        if (!resultsMap.isEmpty()) {
            String bestScore = SpectraUtilities.compareScoresSet(resultsMap, optProtDataset.getSubsetSize(), false, optimisedSearchParameter.getSelectedSearchEngine().getName().equalsIgnoreCase(Advocate.sage.getName()));
            selectedOption = bestScore;
            double impact = Math.round((double) (resultsMap.get(selectedOption + "").getSpectrumMatchResult().size() - optProtDataset.getActiveIdentificationNum()) * 100.0 / (double) optProtDataset.getActiveIdentificationNum()) / 100.0;
            paramScore.setImpact(impact);
            optProtDataset.setActiveScoreModel(resultsMap.get(bestScore));
        }

        selectedOption = selectedOption.replace("[", "").replace("]", "");
        paramScore.setScore(optProtDataset.getActiveIdentificationNum());
        paramScore.setParamValue(selectedOption);
        parameterScoreSet.add(paramScore);
        return selectedOption;
    }

    public Integer optimizeMaxMissCleavagesParameter(SearchingSubDataset optProtDataset, File identificationParametersFile, SearchInputSetting optimisedSearchParameter, TreeSet<ParameterScoreModel> parameterScoreSet) throws IOException {
        final ParameterScoreModel paramScore = new ParameterScoreModel();
        paramScore.setParamId("missedCleavages");
        IdentificationParameters oreginaltempIdParam = IdentificationParameters.getIdentificationParameters(identificationParametersFile);
        if (!oreginaltempIdParam.getSearchParameters().getDigestionParameters().getCleavageParameter().name().equalsIgnoreCase("enzyme")) {
            return -1;
        }
        String enzymeName = oreginaltempIdParam.getSearchParameters().getDigestionParameters().getEnzymes().get(0).getName();
        Integer selectedOption = oreginaltempIdParam.getSearchParameters().getDigestionParameters().getnMissedCleavages(enzymeName);
        Map<String, RawScoreModel> resultsMap = Collections.synchronizedMap(new LinkedHashMap<>());
        String msFileName = IoUtil.removeExtension(optProtDataset.getSubMsFile().getName());

        for (int i = 0; i < 5; i++) {
            if (i == selectedOption) {
                continue;
            }
            IdentificationParameters tempIdParam = IdentificationParameters.getIdentificationParameters(identificationParametersFile);
            tempIdParam.getSearchParameters().getDigestionParameters().setnMissedCleavages(enzymeName, i);
            final String option = "missedCleavages_" + i;
            final String updatedName = Configurations.DEFAULT_RESULT_NAME + "_" + option + "_" + msFileName;

            Future<RawScoreModel> f = MainUtilities.getExecutorService().submit(() -> {
                RawScoreModel scoreModel = excuteSearch(optProtDataset, updatedName, option, tempIdParam, false, optimisedSearchParameter, identificationParametersFile);
                return scoreModel;
            });
            try {
                RawScoreModel scoreModel = f.get();
                if (scoreModel.isAcceptedChange()) {
                    resultsMap.put(i + "", scoreModel);
                } else if (i > selectedOption && !scoreModel.isSensitiveChange()) {
                    break;
                }
            } catch (ExecutionException | InterruptedException ex) {
                ex.printStackTrace();
            }
        }

        if (!resultsMap.isEmpty()) {
            String bestScore = SpectraUtilities.compareScoresSet(resultsMap, optProtDataset.getSubsetSize(), false, optimisedSearchParameter.getSelectedSearchEngine().getName().equalsIgnoreCase(Advocate.sage.getName()));
            selectedOption = Integer.valueOf(bestScore);
            double impact = Math.round((double) (resultsMap.get(selectedOption + "").getSpectrumMatchResult().size() - optProtDataset.getActiveIdentificationNum()) * 100.0 / (double) optProtDataset.getActiveIdentificationNum()) / 100.0;
            paramScore.setImpact(impact);
            optProtDataset.setActiveScoreModel(resultsMap.get(bestScore));
        }
        paramScore.setScore(optProtDataset.getActiveIdentificationNum());
        paramScore.setParamValue(selectedOption + "");
        parameterScoreSet.add(paramScore);

        return selectedOption;

    }

    public double optimizeFragmentToleranceParameter(SearchingSubDataset optProtDataset, File identificationParametersFile, SearchInputSetting optimisedSearchParameter, TreeSet<ParameterScoreModel> parameterScoreSet) throws IOException {
        final ParameterScoreModel paramScore = new ParameterScoreModel();
        paramScore.setParamId("fragmentAccuracy");
        IdentificationParameters oreginaltempIdParam = IdentificationParameters.getIdentificationParameters(identificationParametersFile);
        String msFileName = IoUtil.removeExtension(optProtDataset.getSubMsFile().getName());
        double selectedOption = oreginaltempIdParam.getSearchParameters().getFragmentIonAccuracy();
        Map<String, RawScoreModel> resultsMap = Collections.synchronizedMap(new LinkedHashMap<>());
        double[] values = new double[]{0.01, 0.02, 0.05, 0.1, 0.2, 0.5};

        int counter = 4;
//        double threshold = optProtDataset.getComparisonsThreshold(counter);
        for (double i : values) {
            if (selectedOption == i) {
                continue;
            }
            IdentificationParameters tempIdParam = IdentificationParameters.getIdentificationParameters(identificationParametersFile);
            tempIdParam.getSearchParameters().setFragmentIonAccuracy(i);
            tempIdParam.getSearchParameters().setFragmentAccuracyType(SearchParameters.MassAccuracyType.DA);
            final String option = "fragmentAccuracy_" + i;
            final String updatedName = Configurations.DEFAULT_RESULT_NAME + "_" + option + "_" + msFileName;

            Future<RawScoreModel> f = MainUtilities.getExecutorService().submit(() -> {
                RawScoreModel scoreModel = excuteSearch(optProtDataset, updatedName, option, tempIdParam, true, optimisedSearchParameter, identificationParametersFile);
                return scoreModel;
            });
            try {
                RawScoreModel scoreModel = f.get();
                System.out.println("Fragment  " + i + "   " + scoreModel + "    " );
//                if ((i < selectedOption && scoreModel.isSensitiveChange()) || (scoreModel.getFinalScore() > threshold && scoreModel.getIdPSMNumber() >= optProtDataset.getCurrentScoreModel().getIdPSMNumber())) {
                if (scoreModel.isAcceptedChange()) {
                    resultsMap.put(i + "", scoreModel);
                } else if (i > selectedOption) {
                    break;
                }

            } catch (ExecutionException | InterruptedException ex) {
                ex.printStackTrace();
            }

        }
        if (!resultsMap.isEmpty()) {
            selectedOption = Double.parseDouble(SpectraUtilities.compareScoresSet(resultsMap, optProtDataset.getSubsetSize(), true, optimisedSearchParameter.getSelectedSearchEngine().getName().equalsIgnoreCase(Advocate.sage.getName())));
            double impact = Math.round((double) (resultsMap.get(selectedOption + "").getSpectrumMatchResult().size() - optProtDataset.getActiveIdentificationNum()) * 100.0 / (double) optProtDataset.getActiveIdentificationNum()) / 100.0;
            paramScore.setImpact(impact);
            optProtDataset.setActiveScoreModel(resultsMap.get(selectedOption + ""));

        }
        paramScore.setScore(optProtDataset.getActiveIdentificationNum());
        paramScore.setParamValue(selectedOption + "");
        parameterScoreSet.add(paramScore);
        return selectedOption;

    }

    public int[] optimizePrecursorChargeParameter(SearchingSubDataset optProtDataset, File identificationParametersFile, SearchInputSetting optimisedSearchParameter, TreeSet<ParameterScoreModel> parameterScoreSet) throws IOException {
        final ParameterScoreModel paramScore = new ParameterScoreModel();
        paramScore.setParamId("charge");
        IdentificationParameters oreginaltempIdParam = IdentificationParameters.getIdentificationParameters(identificationParametersFile);
        String msFileName = IoUtil.removeExtension(optProtDataset.getSubMsFile().getName());
        int selectedMaxChargeOption = oreginaltempIdParam.getSearchParameters().getMaxChargeSearched();
        int selectedMinChargeOption = oreginaltempIdParam.getSearchParameters().getMinChargeSearched();
        Map<String, RawScoreModel> resultsMap = Collections.synchronizedMap(new LinkedHashMap<>());
        IdentificationParameters tempIdParam = IdentificationParameters.getIdentificationParameters(identificationParametersFile);
        int spectraCounter = optProtDataset.getActiveIdentificationNum();
        for (int i = 1; i < 5; i++) {

            for (int j = 2; j <= 5; j++) {
                if (j <= i) {
                    continue;
                }
                tempIdParam.getSearchParameters().setMinChargeSearched(i);
                tempIdParam.getSearchParameters().setMaxChargeSearched(j);

                final String option = "charge-" + i + "," + j;
                final String updatedName = Configurations.DEFAULT_RESULT_NAME + "_" + option + "_" + msFileName;

                Future<RawScoreModel> f = MainUtilities.getExecutorService().submit(() -> {

                    RawScoreModel scoreModel = excuteSearch(optProtDataset, updatedName, option, tempIdParam, false, optimisedSearchParameter, identificationParametersFile);
                    return scoreModel;
                });
                try {

                    RawScoreModel scoreModel = f.get();
//                    System.out.println("charge is " + j + "  " + scoreModel);
                    if (scoreModel.getFinalScore() > 0) {
                        if (scoreModel.getSpectrumMatchResult().size() < spectraCounter) {
                            continue;
                        }
                        spectraCounter = Math.max(spectraCounter, scoreModel.getSpectrumMatchResult().size());
                        resultsMap.put(option, scoreModel);

                    } else if (i > selectedMinChargeOption) {
                        break;
                    }
                } catch (ExecutionException | InterruptedException ex) {
                    ex.printStackTrace();
                }
            }

        }

        if (!resultsMap.isEmpty()) {
            String bestScore = SpectraUtilities.compareScoresSet(resultsMap, optProtDataset.getSubsetSize(), false, optimisedSearchParameter.getSelectedSearchEngine().getName().equalsIgnoreCase(Advocate.sage.getName()));
            double impact = Math.round((double) (resultsMap.get(bestScore + "").getSpectrumMatchResult().size() - optProtDataset.getActiveIdentificationNum()) * 100.0 / (double) optProtDataset.getActiveIdentificationNum()) / 100.0;
            paramScore.setImpact(impact);
            optProtDataset.setActiveScoreModel(resultsMap.get(bestScore));
            String[] topOption = bestScore.split("-")[1].split(",");
            selectedMinChargeOption = Integer.parseInt(topOption[0]);
            selectedMaxChargeOption = Integer.parseInt(topOption[1]);
        }
        paramScore.setScore(optProtDataset.getActiveIdentificationNum());
        paramScore.setParamValue(selectedMinChargeOption + "," + selectedMaxChargeOption);
        parameterScoreSet.add(paramScore);
        return new int[]{selectedMinChargeOption, selectedMaxChargeOption};

    }

    public int[] optimizeIsotopParameter(SearchingSubDataset optProtDataset, File identificationParametersFile, SearchInputSetting optimisedSearchParameter, TreeSet<ParameterScoreModel> parameterScoreSet) throws IOException {

        final ParameterScoreModel paramScore = new ParameterScoreModel();
        paramScore.setParamId("isotop_");
        IdentificationParameters oreginaltempIdParam = IdentificationParameters.getIdentificationParameters(identificationParametersFile);
        String msFileName = IoUtil.removeExtension(optProtDataset.getSubMsFile().getName());
        int selectedMaxIsotopicCorrectionOption = oreginaltempIdParam.getSearchParameters().getMaxIsotopicCorrection();
        int selectedMinIsotopicCorrectioneOption = oreginaltempIdParam.getSearchParameters().getMinIsotopicCorrection();
        Map<String, RawScoreModel> resultsMap = Collections.synchronizedMap(new LinkedHashMap<>());
        IdentificationParameters tempIdParam = IdentificationParameters.getIdentificationParameters(identificationParametersFile);

        for (int i = -2; i < 2; i++) {
            for (int j = -1; j <= 2; j++) {
                if (j <= i) {
                    continue;
                }
                tempIdParam.getSearchParameters().setMinIsotopicCorrection(i);
                tempIdParam.getSearchParameters().setMaxIsotopicCorrection(j);
                final String option = "isotop_" + i + "," + j;
                final String updatedName = Configurations.DEFAULT_RESULT_NAME + "_" + option + "_" + msFileName;

                Future<RawScoreModel> f = MainUtilities.getExecutorService().submit(() -> {
                    RawScoreModel scoreModel = excuteSearch(optProtDataset, updatedName, option, tempIdParam, false, optimisedSearchParameter, identificationParametersFile);
                    return scoreModel;
                });
                try {
                    RawScoreModel scoreModel = f.get();
                    if (scoreModel.isAcceptedChange()) {
                        resultsMap.put(option, scoreModel);
                    }
                } catch (ExecutionException | InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }

        if (!resultsMap.isEmpty()) {
            String bestScore = SpectraUtilities.compareScoresSet(resultsMap, optProtDataset.getSubsetSize(), false, optimisedSearchParameter.getSelectedSearchEngine().getName().equalsIgnoreCase(Advocate.sage.getName()));
            double impact = Math.round((double) (resultsMap.get(bestScore + "").getSpectrumMatchResult().size() - optProtDataset.getActiveIdentificationNum()) * 100.0 / (double) optProtDataset.getActiveIdentificationNum()) / 100.0;
            paramScore.setImpact(impact);
            optProtDataset.setActiveScoreModel(resultsMap.get(bestScore));
            String[] topOption = bestScore.split("_")[1].split(",");
            selectedMinIsotopicCorrectioneOption = Integer.parseInt(topOption[0]);
            selectedMaxIsotopicCorrectionOption = Integer.parseInt(topOption[1]);
        }
        paramScore.setScore(optProtDataset.getActiveIdentificationNum());
        paramScore.setParamValue(selectedMinIsotopicCorrectioneOption + "," + selectedMaxIsotopicCorrectionOption);
        parameterScoreSet.add(paramScore);

        return new int[]{selectedMinIsotopicCorrectioneOption, selectedMaxIsotopicCorrectionOption};
    }

    public double optimizePrecursorToleranceParameter(SearchingSubDataset optProtDataset, File identificationParametersFile, SearchInputSetting optimisedSearchParameter, TreeSet<ParameterScoreModel> parameterScoreSet) throws IOException {
        final ParameterScoreModel paramScore = new ParameterScoreModel();
        paramScore.setParamId("PrecursorAccuracy");
        IdentificationParameters oreginaltempIdParam = IdentificationParameters.getIdentificationParameters(identificationParametersFile);
        String msFileName = IoUtil.removeExtension(optProtDataset.getSubMsFile().getName());
        double selectedOption = oreginaltempIdParam.getSearchParameters().getPrecursorAccuracy();
        Map<String, RawScoreModel> resultsMap = Collections.synchronizedMap(new LinkedHashMap<>());
        double[] iValues = new double[]{5, 10, 15, 20, 25};
        boolean toEnd = false;
        int counter = 4;
//        double threshold = optProtDataset.getComparisonsThreshold(counter);
        for (double i : iValues) {
            if (i == selectedOption) {
                continue;
            }
            IdentificationParameters tempIdParam = IdentificationParameters.getIdentificationParameters(identificationParametersFile);
            tempIdParam.getSearchParameters().setPrecursorAccuracy(i);
            tempIdParam.getSearchParameters().setPrecursorAccuracyType(SearchParameters.MassAccuracyType.PPM);
            final String option = "precursorAccuracy_" + i;
            final String updatedName = Configurations.DEFAULT_RESULT_NAME + "_" + option + "_" + msFileName;

            Future<RawScoreModel> f = MainUtilities.getExecutorService().submit(() -> {
                RawScoreModel scoreModel = excuteSearch(optProtDataset, updatedName, option, tempIdParam, false, optimisedSearchParameter, identificationParametersFile);
                return scoreModel;
            });
            try {

                RawScoreModel scoreModel = f.get();
//                if (scoreModel.isAcceptedChange() && scoreModel.getFinalScore() > threshold) {
                System.out.println("score model " + i + "  " + scoreModel);
                if (scoreModel.isAcceptedChange()&& scoreModel.getFinalScore() > 0) {
                    counter++;
                    resultsMap.put(i + "", scoreModel);
                } else if (i > selectedOption) {
                    toEnd = true;
                    break;

                }
            } catch (ExecutionException | InterruptedException ex) {
                ex.printStackTrace();
            }

        }

        if (!toEnd) {
            iValues = new double[]{0.1, 0.3, 0.5, 0.7, 0.9};
            if (!optProtDataset.isHighResolutionMassSpectrometers()) {
                for (double i : iValues) {
                    IdentificationParameters tempIdParam = IdentificationParameters.getIdentificationParameters(identificationParametersFile);
                    tempIdParam.getSearchParameters().setPrecursorAccuracy(i);
                    tempIdParam.getSearchParameters().setPrecursorAccuracyType(SearchParameters.MassAccuracyType.DA);
                    final String option = "precursorAccuracy_Da" + i;
                    final String updatedName = Configurations.DEFAULT_RESULT_NAME + "_" + option + "_" + msFileName;
                    Future<RawScoreModel> f = MainUtilities.getExecutorService().submit(() -> {
                        RawScoreModel scoreModel = excuteSearch(optProtDataset, updatedName, option, tempIdParam, false, optimisedSearchParameter, identificationParametersFile);
                        return scoreModel;
                    });
                    try {
                        RawScoreModel scoreModel = f.get();
                        if (scoreModel.getFinalScore() > 0) {
                            counter++;
//                            threshold = optProtDataset.getComparisonsThreshold(counter);
                            resultsMap.put(i + "", scoreModel);
                        }
                    } catch (ExecutionException | InterruptedException ex) {
                        ex.printStackTrace();
                    }

                }
            }
        }
        if (!resultsMap.isEmpty()) {
            String bestScore = SpectraUtilities.compareScoresSet(resultsMap, optProtDataset.getSubsetSize(), true, optimisedSearchParameter.getSelectedSearchEngine().getName().equalsIgnoreCase(Advocate.sage.getName()));
            double impact = Math.round((double) (resultsMap.get(bestScore + "").getSpectrumMatchResult().size() - optProtDataset.getActiveIdentificationNum()) * 100.0 / (double) optProtDataset.getActiveIdentificationNum()) / 100.0;
            paramScore.setImpact(impact);
            optProtDataset.setActiveScoreModel(resultsMap.get(bestScore));
            selectedOption = Double.parseDouble(bestScore);
        }

        paramScore.setScore(optProtDataset.getActiveIdentificationNum());

        parameterScoreSet.add(paramScore);
        if (selectedOption >= 5) {
            paramScore.setParamValue(selectedOption + "PPM");
            paramScore.setComments("High-Resolution Mass Spectrometers: Instruments like Orbitrap or Fourier Transform Ion Cyclotron Resonance (FT-ICR)");
        } else {
            paramScore.setParamValue(selectedOption + "Da");
            paramScore.setComments("Low-Resolution Mass Spectrometers: Quadrupole and ion trap mass spectrometers have lower mass accuracy");
        }
        return selectedOption;
    }

    /**
     * This function responsible for selection best combination of
     * (fixed,variable,refinement ) modifications-PTMs
     *
     * @param optProtDataset input data-set
     * @param identificationParametersFile identification file
     * @param optimisedSearchParameter
     * @param parameterScoreSet set to store the parameter information object
     * @return modification map
     * @throws
     * IOException,optimisedSearchParameter.getSelectedSearchEngine().getName().equalsIgnoreCase(Advocate.sage.getName())
     */
    public Map<String, Set<String>> optimizeModificationsParameter(SearchingSubDataset optProtDataset, File identificationParametersFile, SearchInputSetting optimisedSearchParameter, TreeSet<ParameterScoreModel> parameterScoreSet) throws IOException {

        Set<String> preservedMods = new HashSet<>();
        preservedMods.add("Deamidation of N");
        preservedMods.add("Deamidation of Q");
        preservedMods.add("Dimethylation of K");
        preservedMods.add("Methylation of K");
        preservedMods.add("Formylation of K");

        Set<String> terminalMods = new HashSet<>();

        final ParameterScoreModel fixedModParamScore = new ParameterScoreModel();
        fixedModParamScore.setParamId("FixedModifications");

        String msFileName = IoUtil.removeExtension(optProtDataset.getSubMsFile().getName());
        ArrayList<String> selectedFixedModificationOption = new ArrayList<>();
        ArrayList<String> selectedVariableModificationOption = new ArrayList<>();
        Map<String, Set<String>> modificationsResults = new HashMap<>();
        List<String> mods = new ArrayList<>();
        mods.addAll(ptmFactory.getModifications(ModificationCategory.Common));
        mods.addAll(ptmFactory.getModifications(ModificationCategory.Common_Biological));
        mods.addAll(ptmFactory.getModifications(ModificationCategory.Common_Artifact));
        IdentificationParameters tempIdParam = IdentificationParameters.getIdentificationParameters(identificationParametersFile);

        tempIdParam.getSearchParameters().getModificationParameters().clearFixedModifications();
        tempIdParam.getSearchParameters().getModificationParameters().clearVariableModifications();
        tempIdParam.getSearchParameters().getModificationParameters().clearRefinementModifications();
        tempIdParam.getSearchParameters().getModificationParameters().getRefinementFixedModifications().clear();
        Map<String, RawScoreModel> resultsMap = Collections.synchronizedMap(new LinkedHashMap<>());
        Set<String> potintialMods = new LinkedHashSet<>();
        String commonFixedMod = "Carbamidomethylation of C";
        potintialMods.add(commonFixedMod);
        String commonVariableMod = "Oxidation of M";
        Map<String, RawScoreModel> targtedFixedModificationScore = new TreeMap<>();
        Map<String, RawScoreModel> fullFixedModificationScore = new LinkedHashMap<>();
        //first stage common fixed modification
        String prefix = "f_";
        resultsMap.putAll(this.checkModificationsScores(selectedFixedModificationOption, selectedVariableModificationOption, potintialMods, true, msFileName, tempIdParam, optProtDataset, identificationParametersFile, optimisedSearchParameter, prefix, true));
        if (!resultsMap.isEmpty()) {
            String bestMod = SpectraUtilities.compareScoresSet(resultsMap, optProtDataset.getSubsetSize(), false, optimisedSearchParameter.getSelectedSearchEngine().getName().equalsIgnoreCase(Advocate.sage.getName()));
            if (resultsMap.get(bestMod).isSensitiveChange() || resultsMap.get(bestMod).getRawFinalScore() > 0 || (resultsMap.get(bestMod).getFinalScore() < 0.0 && (resultsMap.get(bestMod).getFinalScore() * -1.0) < 0.1)) {
                selectedFixedModificationOption.add(bestMod);
                optProtDataset.setActiveScoreModel(resultsMap.get(bestMod));
                potintialMods.clear();
                targtedFixedModificationScore.put("C", resultsMap.get(bestMod));
                MainUtilities.cleanOutputFolder(optimisedSearchParameter.getDatasetId());
                resultsMap.clear();
            }

        }
        //try variable coomon mod first
        // stage 2 test for common variable modification   
        final ParameterScoreModel variableModParamScore = new ParameterScoreModel();
        variableModParamScore.setParamId("VariableModifications");
        potintialMods.add(commonVariableMod);
        prefix = "v_";
        resultsMap.putAll(this.checkModificationsScores(selectedFixedModificationOption, selectedVariableModificationOption, potintialMods, false, msFileName, tempIdParam, optProtDataset, identificationParametersFile, optimisedSearchParameter, prefix, true));

        if (!resultsMap.isEmpty()) {
            String bestMod = SpectraUtilities.compareScoresSet(resultsMap, optProtDataset.getSubsetSize(), false, optimisedSearchParameter.getSelectedSearchEngine().getName().equalsIgnoreCase(Advocate.sage.getName()));
            selectedVariableModificationOption.add(bestMod);
            optProtDataset.setActiveScoreModel(resultsMap.get(bestMod));
            potintialMods.clear();
            MainUtilities.cleanOutputFolder(optimisedSearchParameter.getDatasetId());
            resultsMap.clear();
        }
        MainUtilities.cleanOutputFolder(optimisedSearchParameter.getDatasetId());
        //process variable modifications
        mods.removeAll(selectedFixedModificationOption);
        mods.removeAll(selectedVariableModificationOption);
        //second stage fixed modifications
        for (String modId : mods) {
            String modPattern = ptmFactory.getModification(modId).getPattern().toString();
            if (ptmFactory.getModification(modId).getModificationType().isNTerm() || ptmFactory.getModification(modId).getModificationType().isCTerm()) {
                terminalMods.add(modId);
                continue;
            }
            if (targtedFixedModificationScore.containsKey(modPattern)) {
                continue;
            }

            if (!selectedVariableModificationOption.isEmpty() && modPattern.equalsIgnoreCase("M")) {
                continue;
            }
            potintialMods.add(modId);
        }
        prefix = "f_";
        Map<String, Map<String, RawScoreModel>> filterPotintialPtmMap = new LinkedHashMap<>();
        fullFixedModificationScore.putAll(this.checkModificationsScores(selectedFixedModificationOption, selectedVariableModificationOption, potintialMods, true, msFileName, tempIdParam, optProtDataset, identificationParametersFile, optimisedSearchParameter, prefix, false));

        for (String modId : fullFixedModificationScore.keySet()) {
            RawScoreModel scoreModel = fullFixedModificationScore.get(modId);

            if (scoreModel.isSensitiveChange()) {
                resultsMap.put(modId, scoreModel);

            }
            Modification mod = ptmFactory.getModification(modId);
            //get modified intersection with avctive spectra 
            String modPattern = mod.getPattern().toString();
            if (!filterPotintialPtmMap.containsKey(modPattern)) {
                filterPotintialPtmMap.put(modPattern, new TreeMap<>(Collections.reverseOrder()));
            }
//           
            filterPotintialPtmMap.get(modPattern).put(modId, scoreModel);
        }
        potintialMods.clear();
        potintialMods.addAll(resultsMap.keySet());
        prefix = "FAV_";
        Map<String, RawScoreModel> clearResults = this.checkModificationsScores(selectedFixedModificationOption, selectedVariableModificationOption, potintialMods, false, msFileName, tempIdParam, optProtDataset, identificationParametersFile, optimisedSearchParameter, prefix, false);
        for (String modId : clearResults.keySet()) {
            RawScoreModel vScore = clearResults.get(modId);
            if ((ptmFactory.getModification(modId).getModificationType().isNTerm() || ptmFactory.getModification(modId).getModificationType().isCTerm())) {
                resultsMap.remove(modId);
            } else if (vScore.isSensitiveChange()) {
                if (vScore.getFinalScore() > 1.1 * resultsMap.get(modId).getFinalScore() && vScore.getIdPSMNumber() > 1.02 * resultsMap.get(modId).getIdPSMNumber()) {
                    resultsMap.remove(modId);
                }
            }
        }
        //2nd fixed modifications
        if (!resultsMap.isEmpty()) {
            String bestMod = SpectraUtilities.compareScoresSet(resultsMap, optProtDataset.getSubsetSize(), false, optimisedSearchParameter.getSelectedSearchEngine().getName().equalsIgnoreCase(Advocate.sage.getName()));
            String modPattern = ptmFactory.getModification(bestMod).getPattern().toString();
            selectedFixedModificationOption.add(bestMod);
            optProtDataset.setActiveScoreModel(resultsMap.get(bestMod));
            potintialMods.clear();
            targtedFixedModificationScore.put(modPattern, resultsMap.get(bestMod));
            MainUtilities.cleanOutputFolder(optimisedSearchParameter.getDatasetId());
            resultsMap.remove(bestMod);
        }

        //3rd and 4th  fixed modification processing 
        boolean test = !resultsMap.isEmpty();
        int counter = selectedFixedModificationOption.size();
        while (test) {
            for (String modId : resultsMap.keySet()) {
                String modPattern = ptmFactory.getModification(modId).getPattern().toString();
                if (targtedFixedModificationScore.containsKey(modPattern)) {
                    continue;
                }
                potintialMods.add(modId);
            }
            resultsMap.clear();
            if (!potintialMods.isEmpty()) {
                prefix = "f_";
                resultsMap.putAll(this.checkModificationsScores(selectedFixedModificationOption, selectedVariableModificationOption, potintialMods, true, msFileName, tempIdParam, optProtDataset, identificationParametersFile, optimisedSearchParameter, prefix, false));
                if (!resultsMap.isEmpty()) {
                    String bestMod = SpectraUtilities.compareScoresSet(resultsMap, optProtDataset.getSubsetSize(), false, optimisedSearchParameter.getSelectedSearchEngine().getName().equalsIgnoreCase(Advocate.sage.getName()));
                    selectedFixedModificationOption.add(bestMod);
                    optProtDataset.setActiveScoreModel(resultsMap.get(bestMod));
                    potintialMods.clear();
                    String modPattern = ptmFactory.getModification(bestMod).getPattern().toString();
                    targtedFixedModificationScore.put(modPattern, resultsMap.get(bestMod));
                    MainUtilities.cleanOutputFolder(optimisedSearchParameter.getDatasetId());
                    resultsMap.remove(bestMod);
                    test = !resultsMap.isEmpty() || counter <= 4;
                    counter++;
                } else {
                    test = false;
                }
            } else {
                break;
            }

        }
        modificationsResults.put("fixedModifications", new HashSet<>(selectedFixedModificationOption));
        modificationsResults.put("refinmentFixedModifications", new HashSet<>(selectedFixedModificationOption));
        preservedMods.removeAll(selectedFixedModificationOption);

        fixedModParamScore.setScore(optProtDataset.getActiveIdentificationNum());
        fixedModParamScore.setParamValue(selectedFixedModificationOption.toString());
        parameterScoreSet.add(fixedModParamScore);
        resultsMap.clear();
        potintialMods.clear();

        for (String fixedMod : selectedFixedModificationOption) {
            String modPattern = ptmFactory.getModification(fixedMod).getPattern().toString();
            if (filterPotintialPtmMap.containsKey(modPattern)) {
                filterPotintialPtmMap.remove(modPattern);
            }

        }

        for (String modPatternKey : filterPotintialPtmMap.keySet()) {
            if (modPatternKey.contains("-")) {
                for (String ptm : filterPotintialPtmMap.get(modPatternKey).keySet()) {
                    potintialMods.add(ptm);
                }
            } else {
                String bestTargeted = SpectraUtilities.getTopScoresSet(filterPotintialPtmMap.get(modPatternKey), optProtDataset.getCurrentScoreModel().getSpecTitles(), optProtDataset.getSubsetSize(), optimisedSearchParameter.getSelectedSearchEngine().getName().equalsIgnoreCase(Advocate.sage.getName()));
                potintialMods.add(bestTargeted.split("_-_")[0]);
                if (bestTargeted.contains("_-_")) {
                    potintialMods.add(bestTargeted.split("_-_")[1]);
                }
            }

        }

        double thre = 0;
        Map<String, TreeMap<Double, String>> filterVMMap = new LinkedHashMap<>();

        for (String modId : preservedMods) {
            Modification mod = ptmFactory.getModification(modId);
//                    get modified intersection with avctive spectra 
            String modPattern = mod.getPattern().toString();
            if (targtedFixedModificationScore.containsKey(modPattern)) {
                continue;
            }
            potintialMods.add(modId);

        }
        resultsMap.clear();
        prefix = "v_";
        counter = selectedVariableModificationOption.size();
        int limit = 4 - selectedVariableModificationOption.size();
        thre = 0; //optProtDataset.getComparisonsThreshold(counter);
        filterVMMap.clear();
        while (counter < limit) {
            resultsMap.putAll(this.checkModificationsScores(selectedFixedModificationOption, selectedVariableModificationOption, potintialMods, false, msFileName, tempIdParam, optProtDataset, identificationParametersFile, optimisedSearchParameter, counter + "" + prefix, false));
            filterVMMap.clear();
            System.out.println("modification will be included "+resultsMap.keySet());
            
            if (!resultsMap.isEmpty()) {
                for (String modId : resultsMap.keySet()) {
                    Modification mod = ptmFactory.getModification(modId);
//                    get modified intersection with avctive spectra 
                    String modPattern = mod.getPattern().toString();
//                    if (modPattern.equals("") || mod.getModificationType().isNTerm() || mod.getModificationType().isCTerm()) {
//                        modPattern = modPattern + "-" + mod.getModificationType().isNTerm() + "-" + mod.getModificationType().isCTerm();
//                    }
//                    if (modPattern.equalsIgnoreCase("true-false") && resultsMap.containsKey("Acetylation of protein N-term") && resultsMap.containsKey("Carbamilation of protein N-term")) {
//                        if (resultsMap.get(modId).getFinalScore() <= resultsMap.get("Acetylation of protein N-term").getFinalScore() * 1.05 || resultsMap.get(modId).getIdPSMNumber() <= resultsMap.get("Acetylation of protein N-term").getIdPSMNumber()) {
//                            modId = "Acetylation of protein N-term";
//                        }
//                    }

                    if (!filterVMMap.containsKey(modPattern)) {
                        filterVMMap.put(modPattern, new TreeMap<>(Collections.reverseOrder()));
                    }
                    filterVMMap.get(modPattern).put(resultsMap.get(modId).getFinalScore(), modId);
                    System.out.println("mod id " + modId + "  " + resultsMap.get(modId));

                }
                Set<String> toRemove = new HashSet();
                for (String patteren : filterVMMap.keySet()) {
                    if (patteren.contains("-")) {
                        continue;
                    }
                    TreeMap<Double, String> modPatMap = filterVMMap.get(patteren);
                    Map<String, RawScoreModel> subresultsMap = new LinkedHashMap<>();
                    for (String mod : modPatMap.values()) {
                        subresultsMap.put(mod, resultsMap.get(mod));
                        toRemove.add(mod);
                    }
                    String tokeepMod = SpectraUtilities.compareScoresSet(subresultsMap, optProtDataset.getSubsetSize(), false, optimisedSearchParameter.getSelectedSearchEngine().getName().equalsIgnoreCase(Advocate.sage.getName()));
                    toRemove.remove(tokeepMod);
                }

                for (String remove : toRemove) {
                    resultsMap.remove(remove);
                }
                String bestMod = SpectraUtilities.compareScoresSet(resultsMap, optProtDataset.getSubsetSize(), false, optimisedSearchParameter.getSelectedSearchEngine().getName().equalsIgnoreCase(Advocate.sage.getName()));
                System.out.println("best mod " + bestMod + "   ");
                if (resultsMap.get(bestMod).isAcceptedChange()) {
                    System.out.println("add new vm " + bestMod + "  " + thre + "  " + counter + "   " + resultsMap.get(bestMod).getFinalScore() + "   " );
                    selectedVariableModificationOption.add(bestMod);
                    optProtDataset.setActiveScoreModel(resultsMap.get(bestMod));
                    MainUtilities.cleanOutputFolder(optimisedSearchParameter.getDatasetId());
//                    if (!optProtDataset.isFullDataSpectaInput()) {
                    thre = 0; //optProtDataset.getComparisonsThreshold(counter + 1);
                    resultsMap.remove(bestMod);
                } else {
                    System.out.println("resultsMap.get(bestMod).getFinalScore()  " + resultsMap.get(bestMod).getFinalScore() + "   " + thre);
                    resultsMap.clear();
                }
                if (resultsMap.isEmpty()) {
                    break;
                }

                TreeSet<SortedPTMs> sorePtms = new TreeSet<>(Collections.reverseOrder());
                for (String mod : resultsMap.keySet()) {
                    if (resultsMap.get(mod).isSensitiveChange()) {
                        sorePtms.add(new SortedPTMs(mod, resultsMap.get(mod).getRawFinalScore(), 0));

                    }
                }
                resultsMap.clear();
                potintialMods.clear();
                int subCounter = selectedVariableModificationOption.size();
                for (SortedPTMs mod : sorePtms) {
                    if (subCounter > 4) {
                        break;
                    }
                    potintialMods.add(mod.getName());
                    subCounter++;
                    System.out.println(counter + "-->> mod " + mod.getName() + "  " + mod.getScore());

                }
                counter++;

            } else {
                break;
            }
        }
        /**
         ********************************************
         * terminal test *******************************************
         */
        Set<String> potintialTerminalMods = new LinkedHashSet<>();
        for (String str : terminalMods) {
            potintialTerminalMods.add(str);

        }
        resultsMap.clear();
        prefix = "v_";
        counter = 0;
        thre = 0;
        filterVMMap.clear();
        while (selectedVariableModificationOption.size() < 4 && counter < 4) {
            resultsMap.putAll(this.checkModificationsScores(selectedFixedModificationOption, selectedVariableModificationOption, potintialTerminalMods, false, msFileName, tempIdParam, optProtDataset, identificationParametersFile, optimisedSearchParameter, counter + "" + prefix, false));
            filterVMMap.clear();
            if (!resultsMap.isEmpty()) {
                for (String modId : resultsMap.keySet()) {
                    Modification mod = ptmFactory.getModification(modId);
//                    get modified intersection with avctive spectra 
                    String modPattern = mod.getPattern().toString();
                    modPattern = modPattern + "-" + mod.getModificationType().isNTerm() + "-" + mod.getModificationType().isCTerm();

                    if (modPattern.equalsIgnoreCase("true-false") && resultsMap.containsKey("Acetylation of protein N-term") && resultsMap.containsKey("Carbamilation of protein N-term")) {
//                        if (resultsMap.get(modId).getFinalScore() <= resultsMap.get("Acetylation of protein N-term").getFinalScore() * 1.05 || resultsMap.get(modId).getIdPSMNumber() <= resultsMap.get("Acetylation of protein N-term").getIdPSMNumber()) {
                            modId = "Acetylation of protein N-term";
//                        }
                    }

                    if (!filterVMMap.containsKey(modPattern)) {
                        filterVMMap.put(modPattern, new TreeMap<>(Collections.reverseOrder()));
                    }
                    filterVMMap.get(modPattern).put(resultsMap.get(modId).getFinalScore(), modId);

                }
                System.out.println("filtered --------------->> " + counter + "  vm are " + filterVMMap);
                Set<String> toRemove = new HashSet();
                for (String patteren : filterVMMap.keySet()) {
                    TreeMap<Double, String> modPatMap = filterVMMap.get(patteren);
                    Map<String, RawScoreModel> subresultsMap = new LinkedHashMap<>();
                    for (String mod : modPatMap.values()) {
                        subresultsMap.put(mod, resultsMap.get(mod));
                        toRemove.add(mod);
                    }

                    String tokeepMod = SpectraUtilities.compareScoresSet(subresultsMap, optProtDataset.getSubsetSize(), false, optimisedSearchParameter.getSelectedSearchEngine().getName().equalsIgnoreCase(Advocate.sage.getName()));
                    System.out.println("pattern " + patteren + "  " + subresultsMap.keySet() + "  --->>bets " + tokeepMod);
                    toRemove.remove(tokeepMod);
                }

                System.out.println("to remove: " + toRemove);
                for (String remove : toRemove) {
                    resultsMap.remove(remove);
                }
                String bestMod = SpectraUtilities.compareScoresSet(resultsMap, optProtDataset.getSubsetSize(), false, optimisedSearchParameter.getSelectedSearchEngine().getName().equalsIgnoreCase(Advocate.sage.getName()));
                System.out.println("best left : " + toRemove);
                if (resultsMap.get(bestMod).isSensitiveChange()) {
                    System.out.println("add new vm " + bestMod + " thr " + thre + " counter  " + counter + "  score: " + resultsMap.get(bestMod));
                    selectedVariableModificationOption.add(bestMod);
                    optProtDataset.setActiveScoreModel(resultsMap.get(bestMod));
                    MainUtilities.cleanOutputFolder(optimisedSearchParameter.getDatasetId());

//                    if (!optProtDataset.isFullDataSpectaInput()) {
//                    thre = optProtDataset.getBasicComparisonThreshold();
                    resultsMap.remove(bestMod);
                } else {
                    System.out.println("termina ended by resultsMap.get(bestMod).getFinalScore()  " + resultsMap.get(bestMod).getFinalScore() + "  thr:  " + thre);
                    resultsMap.clear();
                }
                if (resultsMap.isEmpty()) {
                    break;
                }

                TreeSet<SortedPTMs> sorePtms = new TreeSet<>(Collections.reverseOrder());
                for (String mod : resultsMap.keySet()) {
                    if (resultsMap.get(mod).isSensitiveChange()) {
                        sorePtms.add(new SortedPTMs(mod, resultsMap.get(mod).getRawFinalScore(), 0));

                    }
                }
                resultsMap.clear();
                potintialTerminalMods.clear();
                int subCounter = selectedVariableModificationOption.size();
                for (SortedPTMs mod : sorePtms) {
                    if (subCounter >= 4) {
                        break;
                    }
                    potintialTerminalMods.add(mod.getName());
                    subCounter++;
                    System.out.println(counter + "-->> mod " + mod.getName() + "  " + mod.getScore());

                }
                counter++;

            } else {
                break;
            }
        }
        /**
         * ********************************end********************************************************
         */
        variableModParamScore.setScore(optProtDataset.getActiveIdentificationNum());
        variableModParamScore.setParamValue(selectedVariableModificationOption.toString());
        parameterScoreSet.add(variableModParamScore);
        modificationsResults.put("variableModifications", new HashSet<>(selectedVariableModificationOption));
        MainUtilities.cleanOutputFolder(optimisedSearchParameter.getDatasetId());
        return modificationsResults;

    }

    private Map<String, RawScoreModel> checkModificationsScores(ArrayList<String> selectedFixedModificationOption, ArrayList<String> selectedVariableModificationOption, Set<String> modifications, boolean fixed, String msFileName, IdentificationParameters tempIdParam, SearchingSubDataset optProtDataset, File identificationParametersFile, SearchInputSetting searchInputSetting, String prefix, boolean addAll) {
        Map<String, RawScoreModel> resultsMap = Collections.synchronizedMap(new LinkedHashMap<>());
        for (String modId : modifications) {
            final String option = modId;
            final String updatedName = Configurations.DEFAULT_RESULT_NAME + prefix + option + "_" + msFileName;
            tempIdParam.getSearchParameters().getModificationParameters().clearFixedModifications();
            tempIdParam.getSearchParameters().getModificationParameters().getRefinementFixedModifications().clear();
            tempIdParam.getSearchParameters().getModificationParameters().clearVariableModifications();
//            tempIdParam.getSearchParameters().getModificationParameters().addVariableModification(ptmFactory.getModification(modId));
            for (String fixedMod : selectedFixedModificationOption) {
                tempIdParam.getSearchParameters().getModificationParameters().addFixedModification(ptmFactory.getModification(fixedMod));
                tempIdParam.getSearchParameters().getModificationParameters().addRefinementFixedModification(ptmFactory.getModification(fixedMod));
            }
            for (String variableMod : selectedVariableModificationOption) {
                tempIdParam.getSearchParameters().getModificationParameters().addVariableModification(ptmFactory.getModification(variableMod));
            }
            if (fixed) {
                tempIdParam.getSearchParameters().getModificationParameters().addFixedModification(ptmFactory.getModification(modId));
                tempIdParam.getSearchParameters().getModificationParameters().addRefinementFixedModification(ptmFactory.getModification(modId));
            } else {
                tempIdParam.getSearchParameters().getModificationParameters().addVariableModification(ptmFactory.getModification(modId));
            }
            System.out.println("modification " + modId + "  " + selectedFixedModificationOption + "   " + selectedVariableModificationOption + "   " + optProtDataset.getIdentifiedPSMsNumber());
            Future<RawScoreModel> f = MainUtilities.getExecutorService().submit(() -> {
                RawScoreModel scoreModel = excuteSearch(optProtDataset, updatedName, option, tempIdParam, true, searchInputSetting, identificationParametersFile);
                return scoreModel;
            });
            try {
                RawScoreModel scoreModel = f.get();
                System.out.println("score model " + scoreModel);
                if(scoreModel.isAcceptedChange()|| addAll){
//                if ((scoreModel.getFinalScore() > 0 && scoreModel.getIdPSMNumber() >= optProtDataset.getIdentifiedPSMsNumber()) || addAll) {
                    resultsMap.put(modId, scoreModel);//              
                }

            } catch (ExecutionException | InterruptedException ex) {
                ex.printStackTrace();
            }

        }
        tempIdParam.getSearchParameters().getModificationParameters().clearFixedModifications();
        tempIdParam.getSearchParameters().getModificationParameters().getRefinementFixedModifications().clear();
        tempIdParam.getSearchParameters().getModificationParameters().clearVariableModifications();

        return resultsMap;

    }

    public abstract RawScoreModel excuteSearch(SearchingSubDataset optProtDataset, String defaultOutputFileName, String paramOption, IdentificationParameters tempIdParam, boolean addPeptideMasses, SearchInputSetting optProtSearchSettings, File identificationParametersFile);

}
