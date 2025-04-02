/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package no.uib.probe.quicksearchprot.search.myrimatch;

import com.compomics.util.experiment.biology.enzymes.EnzymeFactory;
import com.compomics.util.experiment.biology.modifications.ModificationCategory;
import com.compomics.util.experiment.biology.modifications.ModificationFactory;
import com.compomics.util.experiment.identification.Advocate;
import com.compomics.util.experiment.identification.matches.SpectrumMatch;
import com.compomics.util.experiment.io.identification.IdfileReaderFactory;
import com.compomics.util.parameters.identification.IdentificationParameters;
import com.compomics.util.parameters.identification.search.DigestionParameters;
import com.compomics.util.parameters.identification.search.SearchParameters;
import com.compomics.util.parameters.identification.tool_specific.MyriMatchParameters;
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
public class MyrimatchSearchHandler extends CommonSearchHandler {

    /**
     * The identification file reader factory of compomics utilities.
     */
    private final IdfileReaderFactory readerFactory = IdfileReaderFactory.getInstance();
    /**
     * The compomics PTM factory.
     */
    private final ModificationFactory ptmFactory = ModificationFactory.getInstance();

    private final SearchingSubDataset optProtDataset;
    private final SearchInputSetting searchInputSetting;
    private final File identificationParametersFile;
    private final OptimisedSearchResults optimisedSearchResults;
    private final IdentificationParameters identificationParameters;
    private final Map<String, TreeSet<ParameterScoreModel>> parameterScoreMap;
    private final Set<String> potintialFalsePostiveParamSet = new HashSet<>();
    public MyrimatchSearchHandler(SearchingSubDataset optProtDataset, SearchInputSetting searchInputSetting, File identificationParametersFile) throws IOException {

        this.optProtDataset = optProtDataset;
        this.searchInputSetting = searchInputSetting;
        this.identificationParametersFile = identificationParametersFile;
        this.identificationParameters = IdentificationParameters.getIdentificationParameters(identificationParametersFile);
        MyriMatchParameters myriMatchParameters = (MyriMatchParameters) identificationParameters.getSearchParameters().getAlgorithmSpecificParameters().get(Advocate.myriMatch.getIndex());
        myriMatchParameters.setMaxDynamicMods(identificationParameters.getSearchParameters().getModificationParameters().getVariableModifications().size());
        this.optimisedSearchResults = new OptimisedSearchResults();
        this.parameterScoreMap = new LinkedHashMap<>();
        optProtDataset.setParameterScoreMap(parameterScoreMap);
        MainUtilities.cleanOutputFolder(searchInputSetting.getDatasetId());
          potintialFalsePostiveParamSet.add("precursorAccuracy");
        potintialFalsePostiveParamSet.add("fragmentAccuracy");
//        potintialFalsePostiveParamSet.addAll(ptmFactory.getModifications(ModificationCategory.Common));
        potintialFalsePostiveParamSet.addAll(ptmFactory.getModifications(ModificationCategory.Common_Biological));
        potintialFalsePostiveParamSet.addAll(ptmFactory.getModifications(ModificationCategory.Common_Artifact));
        for (String modId : ptmFactory.getModifications(ModificationCategory.Common_Biological)) {
            if (ptmFactory.getModification(modId).getModificationType().isNTerm() || ptmFactory.getModification(modId).getModificationType().isCTerm()) {
                potintialFalsePostiveParamSet.remove(modId);
            }
        }
        for (String modId : ptmFactory.getModifications(ModificationCategory.Common_Artifact)) {
            if (ptmFactory.getModification(modId).getModificationType().isNTerm() || ptmFactory.getModification(modId).getModificationType().isCTerm()) {
                potintialFalsePostiveParamSet.remove(modId);
            }
        }
        for (int i = 0; i < DigestionParameters.Specificity.values().length; i++) {
            final String option = DigestionParameters.Specificity.getSpecificity(i).name();
              potintialFalsePostiveParamSet.add(option);
        }
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

        parameterScoreMap.put("MyriMatchPeptideLengthParameter", new TreeSet<>(Collections.reverseOrder()));
        parameterScoreMap.put("MyriMatchPrecursorMassParameter", new TreeSet<>(Collections.reverseOrder()));
        parameterScoreMap.put("MyriMatchMaxVarPTMsParameter", new TreeSet<>(Collections.reverseOrder()));
        parameterScoreMap.put("MyriMatchFragmentationMethiodParameter", new TreeSet<>(Collections.reverseOrder()));

        parameterScoreMap.put("MyriMatchEnzymatricTerminalsParameter", new TreeSet<>(Collections.reverseOrder()));
        parameterScoreMap.put("MyriMatchUseSmartPlus3ModelParameter", new TreeSet<>(Collections.reverseOrder()));
//
        parameterScoreMap.put("MyriMatchComputeXCorrParameter", new TreeSet<>(Collections.reverseOrder()));
        parameterScoreMap.put("MyriMatchTICCutoffParameter", new TreeSet<>(Collections.reverseOrder()));
        parameterScoreMap.put("NumberOfIntensityClasses", new TreeSet<>(Collections.reverseOrder()));
//
        parameterScoreMap.put("MyriMatchNumberOfIntensityClassesParameter", new TreeSet<>(Collections.reverseOrder()));
        parameterScoreMap.put("MyriMatchClassSizeMultiplierParameter", new TreeSet<>(Collections.reverseOrder()));
        parameterScoreMap.put("MyriMatchNumberOfBatchesParameter", new TreeSet<>(Collections.reverseOrder()));
        parameterScoreMap.put("MyriMatchMaxPeakCountParameter", new TreeSet<>(Collections.reverseOrder()));
//        parameterScoreMap.put("XtandemPointMutations", new TreeSet<>(Collections.reverseOrder()));
//        parameterScoreMap.put("XtandemSnAPs", new TreeSet<>(Collections.reverseOrder()));
//        parameterScoreMap.put("XtandemSpectrumSynthesis", new TreeSet<>(Collections.reverseOrder()));
//        XtandemParameters xtandemParameters = (XtandemParameters) identificationParameters.getSearchParameters().getAlgorithmSpecificParameters().get(Advocate.xtandem.getIndex());
//        xtandemParameters.setQuickPyrolidone(false);
//        xtandemParameters.setProteinQuickAcetyl(false);
//        xtandemParameters.setStpBias(false);
//        IdentificationParameters.saveIdentificationParameters(identificationParameters, identificationParametersFile);
    }

    private String digestionParameterOpt;

    public void startProcess(List<String> paramOrder) throws IOException {
        digestionParameterOpt = identificationParameters.getSearchParameters().getDigestionParameters().getCleavageParameter().name();
        for (String param : paramOrder) {
            System.out.println("-------------------------------------------param " + param + "-------------------------------------------");
            if (param.equalsIgnoreCase("DigestionParameter_1") && searchInputSetting.isOptimizeDigestionParameter()) {
                optimisedSearchResults.setDigestionParameter("enzyme");
                String[] values = this.optimizeEnzymeParameter(optProtDataset, identificationParametersFile, searchInputSetting, parameterScoreMap.get("EnzymeParameter"));
                optimisedSearchResults.setEnzymeName(values[0]);
                if (!values[0].equalsIgnoreCase(identificationParameters.getSearchParameters().getDigestionParameters().getEnzymes().get(0).getName())) {
                    int nMissesCleavages = identificationParameters.getSearchParameters().getDigestionParameters().getnMissedCleavages(values[0]);
                    identificationParameters.getSearchParameters().getDigestionParameters().clearEnzymes();
                    identificationParameters.getSearchParameters().getDigestionParameters().addEnzyme(EnzymeFactory.getInstance().getEnzyme(values[0]));
                    identificationParameters.getSearchParameters().getDigestionParameters().setnMissedCleavages(values[0], nMissesCleavages);
                    IdentificationParameters.saveIdentificationParameters(identificationParameters, identificationParametersFile);
                }
                if (optProtDataset.getIdentificationRate() < 10) {
//                    values[0] = this.optimizeSpecificityParameter(optProtDataset, identificationParametersFile, searchInputSetting, parameterScoreMap.get("SpecificityParameter"));
                    if (!values[0].equalsIgnoreCase(identificationParameters.getSearchParameters().getDigestionParameters().getSpecificity(identificationParameters.getSearchParameters().getDigestionParameters().getEnzymes().get(0).getName()).name())) {
                        identificationParameters.getSearchParameters().getDigestionParameters().setSpecificity(identificationParameters.getSearchParameters().getDigestionParameters().getEnzymes().get(0).getName(), DigestionParameters.Specificity.valueOf(values[0]));
                        IdentificationParameters.saveIdentificationParameters(identificationParameters, identificationParametersFile);
                    }

                    digestionParameterOpt = this.optimizeDigestionCleavageParameter(optProtDataset, identificationParametersFile, searchInputSetting, parameterScoreMap.get("DigestionParameter"));
                }
                continue;

            }

            if (param.equalsIgnoreCase("FragmentIonTypesParameter") && searchInputSetting.isOptimizeFragmentIonTypesParameter()) {
                String value = this.optimizeFragmentIonTypesParameter(optProtDataset, identificationParametersFile, searchInputSetting, parameterScoreMap.get("FragmentIonTypesParameter"));
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
                    IdentificationParameters.saveIdentificationParameters(identificationParameters, identificationParametersFile);
                }
                continue;

            }

//confusing param
            if (param.equalsIgnoreCase("DigestionParameter_2") && searchInputSetting.isOptimizeDigestionParameter()) {
                int value = this.optimizeMaxMissCleavagesParameter(optProtDataset, identificationParametersFile, searchInputSetting, parameterScoreMap.get("MaxMissCleavagesParameter"));
                if (value != identificationParameters.getSearchParameters().getDigestionParameters().getnMissedCleavages(identificationParameters.getSearchParameters().getDigestionParameters().getEnzymes().get(0).getName())) {
                    identificationParameters.getSearchParameters().getDigestionParameters().setnMissedCleavages(identificationParameters.getSearchParameters().getDigestionParameters().getEnzymes().get(0).getName(), value);
                    IdentificationParameters.saveIdentificationParameters(identificationParameters, identificationParametersFile);
                }
                if (optProtDataset.getIdentificationRate() < 10) {
                    digestionParameterOpt = this.optimizeDigestionCleavageParameter(optProtDataset, identificationParametersFile, searchInputSetting, parameterScoreMap.get("DigestionParameter"));
                }
                continue;

            }

            if (param.equalsIgnoreCase("FragmentToleranceParameter") && searchInputSetting.isOptimizeFragmentToleranceParameter()) {
                double value = this.optimizeFragmentToleranceParameter(optProtDataset, identificationParametersFile, searchInputSetting, parameterScoreMap.get("FragmentToleranceParameter"));
                if (value != identificationParameters.getSearchParameters().getFragmentIonAccuracy()) {
                    identificationParameters.getSearchParameters().setFragmentIonAccuracy(value);
                    IdentificationParameters.saveIdentificationParameters(identificationParameters, identificationParametersFile);
                }
                continue;
            }

            if (param.equalsIgnoreCase("PrecursorChargeParameter") && searchInputSetting.isOptimizePrecursorChargeParameter()) {

                int[] values = this.optimizePrecursorChargeParameter(optProtDataset, identificationParametersFile, searchInputSetting, parameterScoreMap.get("PrecursorChargeParameter"));
                if (values[1] != identificationParameters.getSearchParameters().getMaxChargeSearched()) {
                    identificationParameters.getSearchParameters().setMinChargeSearched(values[0]);
                    identificationParameters.getSearchParameters().setMaxChargeSearched(values[1]);
                    IdentificationParameters.saveIdentificationParameters(identificationParameters, identificationParametersFile);
                }
                continue;
            }

            if (param.equalsIgnoreCase("ModificationParameter") && searchInputSetting.isOptimizeModificationParameter()) {
                Map<String, Set<String>> modificationsResults = this.optimizeModificationsParameter(optProtDataset, identificationParametersFile, searchInputSetting, parameterScoreMap.get("ModificationsParameter"));
                identificationParameters.getSearchParameters().getModificationParameters().clearFixedModifications();
                identificationParameters.getSearchParameters().getModificationParameters().clearVariableModifications();
                identificationParameters.getSearchParameters().getModificationParameters().clearRefinementModifications();
                identificationParameters.getSearchParameters().getModificationParameters().getRefinementFixedModifications().clear();
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
                MyriMatchParameters myriMatchParameters = (MyriMatchParameters) identificationParameters.getSearchParameters().getAlgorithmSpecificParameters().get(Advocate.myriMatch.getIndex());
                myriMatchParameters.setMaxDynamicMods(modificationsResults.get("variableModifications").size());
                IdentificationParameters.saveIdentificationParameters(identificationParameters, identificationParametersFile);
                continue;
            }

////        System.out.println("thershuld for id is " + optProtDataset.getAcceptedIDRatioThreshold());
//        if (searchInputSetting.isOptimizeAllParameters()) {
////            advancedParam = false;
//
//        }
//        digestionParameterOpt = identificationParameters.getSearchParameters().getDigestionParameters().getCleavageParameter().name();
//        for (String param : paramOrder) {
//
////        System.exit(0);
//            System.out.println("-------------------------------------------param " + param + "-------------------------------------------");
//            if (param.equalsIgnoreCase("DigestionParameter_1") && searchInputSetting.isOptimizeDigestionParameter()) {
//                optimisedSearchResults.setDigestionParameter("enzyme");                
//                String  value = this.optimizeSpecificityParameter(optProtDataset, identificationParametersFile, searchInputSetting, parameterScoreMap.get("SpecificityParameter"));
//                if (!value.equalsIgnoreCase(identificationParameters.getSearchParameters().getDigestionParameters().getSpecificity(identificationParameters.getSearchParameters().getDigestionParameters().getEnzymes().get(0).getName()).name())) {
//                    identificationParameters.getSearchParameters().getDigestionParameters().setSpecificity(identificationParameters.getSearchParameters().getDigestionParameters().getEnzymes().get(0).getName(), DigestionParameters.Specificity.valueOf(value));
//                    System.out.println("optimizeSpecificityParameter " + value + "------------------------------------------------------------------------->>> 3 id rate " + optProtDataset.getActiveIdentificationNum());
//                    IdentificationParameters.saveIdentificationParameters(identificationParameters, identificationParametersFile);
//                }
//                continue;
//                
//            }
//                 if (param.equalsIgnoreCase("DigestionParameter_2") && searchInputSetting.isOptimizeDigestionParameter()) {
//                int value = this.optimizeMaxMissCleavagesParameter(optProtDataset, identificationParametersFile, searchInputSetting, parameterScoreMap.get("MaxMissCleavagesParameter"));
//                if (value != identificationParameters.getSearchParameters().getDigestionParameters().getnMissedCleavages(identificationParameters.getSearchParameters().getDigestionParameters().getEnzymes().get(0).getName())) {
//                    identificationParameters.getSearchParameters().getDigestionParameters().setnMissedCleavages(identificationParameters.getSearchParameters().getDigestionParameters().getEnzymes().get(0).getName(), value);
//                    IdentificationParameters.saveIdentificationParameters(identificationParameters, identificationParametersFile);
//                    System.out.println("optimizeMaxMissCleavagesParameter " + value + "------------------------------------------------------------------------->>> 6 id rate " + optProtDataset.getActiveIdentificationNum());
//                }
//                
//                digestionParameterOpt = this.optimizeDigestionCleavageParameter(optProtDataset, identificationParametersFile, searchInputSetting, parameterScoreMap.get("DigestionParameter"));
//                
//            }
//              if (param.equalsIgnoreCase("DigestionParameter_3") && searchInputSetting.isOptimizeDigestionParameter()) {
//                optimisedSearchResults.setDigestionParameter("enzyme");
//                String value = this.optimizeEnzymeParameter(optProtDataset, identificationParametersFile, searchInputSetting, parameterScoreMap.get("EnzymeParameter"));
//                optimisedSearchResults.setEnzymeName(value);
//                if (!value.contains(identificationParameters.getSearchParameters().getDigestionParameters().getEnzymes().get(0).getName())) {
//                    int nMissesCleavages = identificationParameters.getSearchParameters().getDigestionParameters().getnMissedCleavages(identificationParameters.getSearchParameters().getDigestionParameters().getEnzymes().get(0).getName());
//                    identificationParameters.getSearchParameters().getDigestionParameters().clearEnzymes();
//                    identificationParameters.getSearchParameters().getDigestionParameters().addEnzyme(EnzymeFactory.getInstance().getEnzyme(value));
//                    identificationParameters.getSearchParameters().getDigestionParameters().setnMissedCleavages(value, nMissesCleavages);
//                    IdentificationParameters.saveIdentificationParameters(identificationParameters, identificationParametersFile);
//                    System.out.println("optimizeEnzymeParameter " + value + "------------------------------------------------------------------------->>> 1 id rate " + optProtDataset.getActiveIdentificationNum());
//                }
//                continue;
//            }
//            if (param.equalsIgnoreCase("FragmentIonTypesParameter") && searchInputSetting.isOptimizeFragmentIonTypesParameter()) {
//                String value = this.optimizeFragmentIonTypesParameter(optProtDataset, identificationParametersFile, searchInputSetting, parameterScoreMap.get("FragmentIonTypesParameter"));
//                int forward = Integer.parseInt(value.split("-")[0]);
//                int rewind = Integer.parseInt(value.split("-")[1]);
//                boolean update = false;
//                if (!identificationParameters.getSearchParameters().getForwardIons().contains(forward)) {
//                    ArrayList<Integer> forwardIonsList = new ArrayList<>();
//                    forwardIonsList.add(forward);
//                    identificationParameters.getSearchParameters().setForwardIons(forwardIonsList);
//                    update = true;
//                }
//                if (!identificationParameters.getSearchParameters().getRewindIons().contains(rewind)) {
//                    ArrayList<Integer> rewindIonsList = new ArrayList<>();
//                    rewindIonsList.add(rewind);
//                    identificationParameters.getSearchParameters().setRewindIons(rewindIonsList);
//                    update = true;
//                }
//                if (update) {
//                    System.out.println("OptimizeFragmentIonTypesParameter" + value + "------------------------------------------------------------------------->>> 4 id rate " + optProtDataset.getActiveIdentificationNum());
//                    IdentificationParameters.saveIdentificationParameters(identificationParameters, identificationParametersFile);
//                }
//                continue;
//                
//            }
//
////confusing param
//       
//            
//            if (param.equalsIgnoreCase("FragmentToleranceParameter") && searchInputSetting.isOptimizeFragmentToleranceParameter()) {
//                double value = this.optimizeFragmentToleranceParameter(optProtDataset, identificationParametersFile, searchInputSetting, parameterScoreMap.get("FragmentToleranceParameter"));
//                if (value != identificationParameters.getSearchParameters().getFragmentIonAccuracy()) {
//                    identificationParameters.getSearchParameters().setFragmentIonAccuracy(value);
//                    IdentificationParameters.saveIdentificationParameters(identificationParameters, identificationParametersFile);
//                    System.out.println("optimizeFragmentToleranceParameter" + value + "------------------------------------------------------------------------->>> 8 id rate " + optProtDataset.getActiveIdentificationNum());
//                }
//                continue;
//            }
//            
//            if (param.equalsIgnoreCase("PrecursorChargeParameter") && searchInputSetting.isOptimizePrecursorChargeParameter()) {
//                
//                int[] values = this.optimizePrecursorChargeParameter(optProtDataset, identificationParametersFile, searchInputSetting, parameterScoreMap.get("PrecursorChargeParameter"));
//                if (values[1] != identificationParameters.getSearchParameters().getMaxChargeSearched()) {
//                    identificationParameters.getSearchParameters().setMinChargeSearched(values[0]);
//                    identificationParameters.getSearchParameters().setMaxChargeSearched(values[1]);
//                    IdentificationParameters.saveIdentificationParameters(identificationParameters, identificationParametersFile);
//                    System.out.println("optimizePrecursorChargeParameter" + values[0] + "--" + values[1] + "------------------------------------------------------------------------->>> 9 id rate " + optProtDataset.getActiveIdentificationNum());
//                }
//                continue;
//                
//            }
//            if (param.equalsIgnoreCase("IsotopParameter") && searchInputSetting.isOptimizeIsotopsParameter()) {
//                int[] values = this.optimizeIsotopParameter(optProtDataset, identificationParametersFile, searchInputSetting, parameterScoreMap.get("IsotopParameter"));
//                if (values[1] != identificationParameters.getSearchParameters().getMaxIsotopicCorrection() || values[0] != identificationParameters.getSearchParameters().getMinIsotopicCorrection()) {
//                    identificationParameters.getSearchParameters().setMinIsotopicCorrection(values[0]);
//                    identificationParameters.getSearchParameters().setMaxIsotopicCorrection(values[1]);
//                    IdentificationParameters.saveIdentificationParameters(identificationParameters, identificationParametersFile);
//                    System.out.println("optimizeIsotopParameter" + values[0] + "--" + values[1] + "------------------------------------------------------------------------->>> 10 id rate " + optProtDataset.getActiveIdentificationNum());
//                }
//                continue;
//                
//            }
//            if (param.equalsIgnoreCase("ModificationParameter") && searchInputSetting.isOptimizeModificationParameter()) {
//                
//                Map<String, Set<String>> modificationsResults = this.optimizeModificationsParameter(optProtDataset, identificationParametersFile, searchInputSetting, parameterScoreMap.get("ModificationsParameter"));         
//                identificationParameters.getSearchParameters().getModificationParameters().clearFixedModifications();
//                identificationParameters.getSearchParameters().getModificationParameters().clearVariableModifications();
//                identificationParameters.getSearchParameters().getModificationParameters().clearRefinementModifications();
//                identificationParameters.getSearchParameters().getModificationParameters().getRefinementFixedModifications().clear();
//                for (String fixedMod : modificationsResults.get("fixedModifications")) {
//                    if (ptmFactory.getModification(fixedMod) != null) {
//                        identificationParameters.getSearchParameters().getModificationParameters().addFixedModification(ptmFactory.getModification(fixedMod));
//                        identificationParameters.getSearchParameters().getModificationParameters().addRefinementFixedModification(ptmFactory.getModification(fixedMod));
//                    }
//                }
//                for (String variableMod : modificationsResults.get("variableModifications")) {
//                    if (ptmFactory.getModification(variableMod) != null) {
//                        identificationParameters.getSearchParameters().getModificationParameters().addVariableModification(ptmFactory.getModification(variableMod));
//                    }
//                }
//                MyriMatchParameters myriMatchParameters = (MyriMatchParameters) identificationParameters.getSearchParameters().getAlgorithmSpecificParameters().get(Advocate.myriMatch.getIndex());
//                myriMatchParameters.setMaxDynamicMods(modificationsResults.get("variableModifications").size());
//                IdentificationParameters.saveIdentificationParameters(identificationParameters, identificationParametersFile);
//                continue;
//            }
//            if (param.equalsIgnoreCase("ModificationParameter") && searchInputSetting.isOptimizeModificationParameter()) {
//
//                Map<String, Set<String>> modificationsResults = this.optimizeModificationsParameter(optProtDataset, identificationParametersFile, searchInputSetting, parameterScoreMap.get("ModificationsParameter"));
//
//                identificationParameters.getSearchParameters().getModificationParameters().clearFixedModifications();
//                identificationParameters.getSearchParameters().getModificationParameters().clearVariableModifications();
//                identificationParameters.getSearchParameters().getModificationParameters().clearRefinementModifications();
//                identificationParameters.getSearchParameters().getModificationParameters().getRefinementFixedModifications().clear();
//                for (String fixedMod : modificationsResults.get("fixedModifications")) {
//                    if (ptmFactory.getModification(fixedMod) != null) {
//                        identificationParameters.getSearchParameters().getModificationParameters().addFixedModification(ptmFactory.getModification(fixedMod));
//
//                        identificationParameters.getSearchParameters().getModificationParameters().addRefinementFixedModification(ptmFactory.getModification(fixedMod));
//                    }
//                }
//                for (String variableMod : modificationsResults.get("variableModifications")) {
//                    if (ptmFactory.getModification(variableMod) != null) {
//                        identificationParameters.getSearchParameters().getModificationParameters().addVariableModification(ptmFactory.getModification(variableMod));
//                    }
//                }
////                for (String refinmentVariableMod : modificationsResults.get("refinmentVariableModifications")) {
////                    if (ptmFactory.getModification(refinmentVariableMod) != null) {
////                        identificationParameters.getSearchParameters().getModificationParameters().addRefinementVariableModification(ptmFactory.getModification(refinmentVariableMod));
////                    }
////                }
//                MyriMatchParameters myriMatchParameters = (MyriMatchParameters) identificationParameters.getSearchParameters().getAlgorithmSpecificParameters().get(Advocate.myriMatch.getIndex());
//                myriMatchParameters.setMaxDynamicMods(modificationsResults.get("variableModifications").size());
//                IdentificationParameters.saveIdentificationParameters(identificationParameters, identificationParametersFile);
//                continue;
//            }
            if (param.equalsIgnoreCase("MyriMatchAdvancedParameter") && searchInputSetting.isOptimizeMyriMatchAdvancedParameter()) {
//                MyriMatchParameters myriMatchParameters = (MyriMatchParameters) identificationParameters.getSearchParameters().getAlgorithmSpecificParameters().get(Advocate.myriMatch.getIndex());

//                int[] values = optimizePeptideLengthParameter(optProtDataset, identificationParametersFile, searchInputSetting, parameterScoreMap.get("MyriMatchPeptideLengthParameter"));
//                if (values[1] != myriMatchParameters.getMaxPeptideLength() || values[0] != myriMatchParameters.getMinPeptideLength()) {
//                    myriMatchParameters.setMinPeptideLength(values[0]);
//                    myriMatchParameters.setMaxPeptideLength(values[1]);
//                    IdentificationParameters.saveIdentificationParameters(identificationParameters, identificationParametersFile);
//                    System.out.println("peptide length " + values[0] + "--" + values[1] + "------------------------------------------------------------------------->>> 12 id rate " + optProtDataset.getActiveIdentificationNum());
//                }
//                
//                double[] dvalues = optimizePrecursorMassParameter(optProtDataset, identificationParametersFile, searchInputSetting, parameterScoreMap.get("MyriMatchPrecursorMassParameter"));
//                if (dvalues[1] != myriMatchParameters.getMaxPrecursorMass() || dvalues[0] != myriMatchParameters.getMinPrecursorMass()) {
//                    myriMatchParameters.setMinPrecursorMass(dvalues[0]);
//                    myriMatchParameters.setMaxPrecursorMass(dvalues[1]);
//                    IdentificationParameters.saveIdentificationParameters(identificationParameters, identificationParametersFile);
//                    System.out.println("PrecursorMass " + dvalues[0] + "--" + dvalues[1] + "------------------------------------------------------------------------->>> 12 id rate " + optProtDataset.getActiveIdentificationNum());
//                }
//                System.exit(0);
//                int value = optimizeMaxVarPTMsNumber(optProtDataset, identificationParametersFile, searchInputSetting, parameterScoreMap.get("MyriMatchMaxVarPTMsParameter"));
//                myriMatchParameters.setMaxDynamicMods(value);
//                IdentificationParameters.saveIdentificationParameters(identificationParameters, identificationParametersFile);
//                
//                String valueStr = optimizeFragmentationMethod(optProtDataset, identificationParametersFile, searchInputSetting, parameterScoreMap.get("MyriMatchFragmentationMethiodParameter"));
//                myriMatchParameters.setFragmentationRule(valueStr);
//                IdentificationParameters.saveIdentificationParameters(identificationParameters, identificationParametersFile);
//                
//                value = optimizeEnzymaticTerminals(optProtDataset, identificationParametersFile, searchInputSetting, parameterScoreMap.get("MyriMatchEnzymatricTerminalsParameter"));
//                myriMatchParameters.setMinTerminiCleavages(value);
//                IdentificationParameters.saveIdentificationParameters(identificationParameters, identificationParametersFile);
//                
//                boolean valueBoalen = optimizeUseSmartPlus3Model(optProtDataset, identificationParametersFile, searchInputSetting, parameterScoreMap.get("MyriMatchUseSmartPlus3ModelParameter"));
//                myriMatchParameters.setUseSmartPlusThreeModel(valueBoalen);
//                IdentificationParameters.saveIdentificationParameters(identificationParameters, identificationParametersFile);
//                
//                valueBoalen = optimizeComputeXCorr(optProtDataset, identificationParametersFile, searchInputSetting, parameterScoreMap.get("MyriMatchComputeXCorrParameter"));
//                myriMatchParameters.setComputeXCorr(valueBoalen);
//                IdentificationParameters.saveIdentificationParameters(identificationParameters, identificationParametersFile);
//                
//                double dvalue = optimizeoptimizeTICCutoff(optProtDataset, identificationParametersFile, searchInputSetting, parameterScoreMap.get("MyriMatchTICCutoffParameter"));
//                myriMatchParameters.setTicCutoffPercentage(dvalue);
//                IdentificationParameters.saveIdentificationParameters(identificationParameters, identificationParametersFile);
//                
//                value = optimizeNumberOfIntensityClasses(optProtDataset, identificationParametersFile, searchInputSetting, parameterScoreMap.get("MyriMatchNumberOfIntensityClassesParameter"));
//                myriMatchParameters.setNumIntensityClasses(value);
//                IdentificationParameters.saveIdentificationParameters(identificationParameters, identificationParametersFile);
//                
//                value = optimizeClassSizeMultiplier(optProtDataset, identificationParametersFile, searchInputSetting, parameterScoreMap.get("MyriMatchClassSizeMultiplierParameter"));
//                myriMatchParameters.setClassSizeMultiplier(value);
//                IdentificationParameters.saveIdentificationParameters(identificationParameters, identificationParametersFile);
//                
//                value = optimizeNumberOfBatches(optProtDataset, identificationParametersFile, searchInputSetting, parameterScoreMap.get("MyriMatchNumberOfBatchesParameter"));
//                myriMatchParameters.setNumberOfBatches(value);
//                IdentificationParameters.saveIdentificationParameters(identificationParameters, identificationParametersFile);
//                
//                value = optimizeMaxPeakCount(optProtDataset, identificationParametersFile, searchInputSetting, parameterScoreMap.get("MyriMatchMaxPeakCountParameter"));
//                myriMatchParameters.setMaxPeakCount(value);
//                IdentificationParameters.saveIdentificationParameters(identificationParameters, identificationParametersFile);
                continue;
            }

            if (param.equalsIgnoreCase("PrecursorToleranceParameter") && searchInputSetting.isOptimizePrecursorToleranceParameter()) {

                double value = this.optimizePrecursorToleranceParameter(optProtDataset, identificationParametersFile, searchInputSetting, parameterScoreMap.get("PrecursorToleranceParameter"));
                if (value != identificationParameters.getSearchParameters().getPrecursorAccuracy()) {
                    identificationParameters.getSearchParameters().setPrecursorAccuracy(value);
                    if (value > 1) {
                        identificationParameters.getSearchParameters().setPrecursorAccuracyType(SearchParameters.MassAccuracyType.PPM);
                    } else {
                        identificationParameters.getSearchParameters().setPrecursorAccuracyType(SearchParameters.MassAccuracyType.DA);
                    }
                    IdentificationParameters.saveIdentificationParameters(identificationParameters, identificationParametersFile);
                }
            }
        }
        if (!digestionParameterOpt.equalsIgnoreCase(identificationParameters.getSearchParameters().getDigestionParameters().getCleavageParameter().name())) {
            optimisedSearchResults.setDigestionParameter(digestionParameterOpt);
            identificationParameters.getSearchParameters().getDigestionParameters().setCleavageParameter(DigestionParameters.CleavageParameter.valueOf(digestionParameterOpt));
            IdentificationParameters.saveIdentificationParameters(identificationParameters, identificationParametersFile);
        }
    }

    @Override
    public synchronized RawScoreModel excuteSearch(SearchingSubDataset optProtDataset, String defaultOutputFileName, String paramOption, IdentificationParameters tempIdParam, boolean addSpectraList, SearchInputSetting optProtSearchSettings, File identificationParametersFile) {

        if (!optProtSearchSettings.getMyriMatchEnabledParameters().getParamsToOptimize().isEnabledParam(paramOption.split("_")[0])) {
            System.out.println("param " + paramOption + " is not supported " + paramOption);
            return new RawScoreModel(paramOption);
        }
        if (defaultOutputFileName.contains("_resultsf_Pyrolidon") || defaultOutputFileName.contains("_resultsf_Acetylation of protein N-term") || defaultOutputFileName.contains("_resultsf_Carbamilation of protein N-term")) {
            System.out.println("param " + paramOption + " is not supported " + paramOption);
            return new RawScoreModel(paramOption);
        }
         boolean potintialFP = false;
        if (potintialFalsePostiveParamSet.contains(paramOption.split("_")[0])) {
            potintialFP = true;
        }
        File resultOutput = SearchExecuter.executeSearch(defaultOutputFileName, optProtSearchSettings, optProtDataset.getSubMsFile(), optProtDataset.getSubFastaFile(), tempIdParam, identificationParametersFile);
        List<SpectrumMatch> validatedMaches = SpectraUtilities.getValidatedIdentificationResults(resultOutput, optProtDataset.getSubMsFile(), Advocate.myriMatch, tempIdParam);
        if (paramOption.contains("_")) {
            paramOption = paramOption.split("_")[1];
        }
        RawScoreModel rawScore = SpectraUtilities.getComparableRawScore(optProtDataset, validatedMaches, Advocate.myriMatch, addSpectraList, paramOption,potintialFP);//(optProtDataset, resultOutput, optProtDataset.getSubMsFile(), Advocate.sage, tempIdParam, updateDataReference);
        MainUtilities.deleteFolder(resultOutput);
        if (addSpectraList && rawScore.isAcceptedChange()) {
            rawScore.setSpectrumMatchResult(validatedMaches);
        }
        return (rawScore);
    }

//    public int[] optimizePeptideLengthParameter(SearchingSubDataset optProtDataset, File identificationParametersFile, SearchInputSetting optimisedSearchParameter, TreeSet<ParameterScoreModel> parameterScoreSet) throws IOException {
//        IdentificationParameters oreginaltempIdParam = IdentificationParameters.getIdentificationParameters(identificationParametersFile);
//        MyriMatchParameters myriMatchParameters = (MyriMatchParameters) oreginaltempIdParam.getSearchParameters().getAlgorithmSpecificParameters().get(Advocate.myriMatch.getIndex());
//        
//        String msFileName = IoUtil.removeExtension(optProtDataset.getSubMsFile().getName());
//        int selectedMaxPeptideLengthOption = myriMatchParameters.getMaxPeptideLength();
//        int selectedMinPeptideLengthOption = myriMatchParameters.getMinPeptideLength();
//        int idRate = optProtDataset.getActiveIdentificationNum();
//        Map<Integer, Integer> resultsMap = Collections.synchronizedMap(new LinkedHashMap<>());
//        IdentificationParameters tempIdParam = IdentificationParameters.getIdentificationParameters(identificationParametersFile);
//        MyriMatchParameters tempMyriMatchParameters = (MyriMatchParameters) tempIdParam.getSearchParameters().getAlgorithmSpecificParameters().get(Advocate.myriMatch.getIndex());
//        tempMyriMatchParameters.setMaxPeptideLength(selectedMaxPeptideLengthOption);
//        for (int i = 5; i < 11; i++) {
//            tempMyriMatchParameters.setMinPeptideLength(i);
//            final String option = "minPeptideLength_" + i;
//            final String updatedName = Configurations.DEFAULT_RESULT_NAME + "_" + option + "_" + msFileName;
//            int j = i;
//            final ParameterScoreModel paramScore = new ParameterScoreModel();
//            paramScore.setParamId("minPeptideLength");
//            Future future = executor.submit(() -> {
//                resultsMap.put(j, excuteSearch(optProtDataset, updatedName, option, tempIdParam, false, optimisedSearchParameter, identificationParametersFile).size());
//                System.out.println("minPeptideLength is top " + j + " results " + resultsMap.get(j) + "  " + optProtDataset.getActiveIdentificationNum());
//                paramScore.setScore(resultsMap.get(j));
//                paramScore.setParamValue(option);
//                parameterScoreSet.add(paramScore);
//            });
//            while (!future.isDone()) {
//            }
//            
//        }
//        
//        int localId = -1;
//        int localSelection = 0;
//        for (int option : resultsMap.keySet()) {
//            if (resultsMap.get(option) > localId) {
//                localId = resultsMap.get(option);
//                localSelection = option;
//                
//            }
//        }
//        double factor = (localId - idRate) * 100.0 / idRate;//* optProtDataset.getDataEpsilon();;
//        factor = Math.round(factor);
//        if (factor >= optProtDataset.getAcceptedIDRatioThreshold() && resultsMap.get(localSelection) > resultsMap.get(selectedMinPeptideLengthOption)) {
//            selectedMinPeptideLengthOption = localSelection;
//            optProtDataset.setActiveIdentificationNum(localId);
//            idRate = localId;
//        }
//        resultsMap.clear();
//        
//        tempMyriMatchParameters.setMinPeptideLength(selectedMinPeptideLengthOption);
//        for (int i = 20; i < 41;) {
//            tempMyriMatchParameters.setMaxPeptideLength(i);
//            final String option = "maxPeptideLength_" + i;
//            final String updatedName = Configurations.DEFAULT_RESULT_NAME + "_" + option + "_" + msFileName;
//            int j = i;
//            final ParameterScoreModel paramScore = new ParameterScoreModel();
//            paramScore.setParamId("maxPeptideLength");
//            
//            Future future = executor.submit(() -> {
//                resultsMap.put(j, excuteSearch(optProtDataset, updatedName, option, tempIdParam, false, optimisedSearchParameter, identificationParametersFile).size());
//                System.out.println("PeptideLength max  is top " + j + " results " + resultsMap.get(j) + "  " + optProtDataset.getActiveIdentificationNum());
//                paramScore.setScore(resultsMap.get(j));
//                paramScore.setParamValue(option);
//                parameterScoreSet.add(paramScore);
//            });
//            while (!future.isDone()) {
//            }
//            
//            i += 5;
//        }
//        localId = -1;
//        localSelection = 0;
//        for (int option : resultsMap.keySet()) {
//            if (resultsMap.get(option) > localId) {
//                localId = resultsMap.get(option);
//                localSelection = option;
//                
//            }
//        }
//        factor = (localId - idRate) * 100.0 / idRate;//* optProtDataset.getDataEpsilon();;
//        factor = Math.round(factor);
//        if (factor >= optProtDataset.getAcceptedIDRatioThreshold() && resultsMap.get(localSelection) > resultsMap.get(selectedMaxPeptideLengthOption)) {
//            selectedMaxPeptideLengthOption = localSelection;
//            optProtDataset.setActiveIdentificationNum(localId);
//        }
//        return new int[]{selectedMinPeptideLengthOption, selectedMaxPeptideLengthOption};
//    }
//    
//    public double[] optimizePrecursorMassParameter(SearchingSubDataset optProtDataset, File identificationParametersFile, SearchInputSetting optimisedSearchParameter, TreeSet<ParameterScoreModel> parameterScoreSet) throws IOException {
//        IdentificationParameters oreginaltempIdParam = IdentificationParameters.getIdentificationParameters(identificationParametersFile);
//        MyriMatchParameters myriMatchParameters = (MyriMatchParameters) oreginaltempIdParam.getSearchParameters().getAlgorithmSpecificParameters().get(Advocate.myriMatch.getIndex());
//        
//        String msFileName = IoUtil.removeExtension(optProtDataset.getSubMsFile().getName());
//        double selectedMaxPrecursorMassOption = myriMatchParameters.getMaxPrecursorMass();
//        double selectedMinPrecursorMassOption = myriMatchParameters.getMinPrecursorMass();
//        int idRate = optProtDataset.getActiveIdentificationNum();
//        Map<Double, Integer> resultsMap = Collections.synchronizedMap(new LinkedHashMap<>());
//        IdentificationParameters tempIdParam = IdentificationParameters.getIdentificationParameters(identificationParametersFile);
//        MyriMatchParameters tempMyriMatchParameters = (MyriMatchParameters) tempIdParam.getSearchParameters().getAlgorithmSpecificParameters().get(Advocate.myriMatch.getIndex());
//        tempMyriMatchParameters.setMaxPrecursorMass(selectedMaxPrecursorMassOption);
//        for (double i = 100; i < 801;) {
//            tempMyriMatchParameters.setMinPrecursorMass(i);
//            final String option = "minPrecursorMass_" + i;
//            final String updatedName = Configurations.DEFAULT_RESULT_NAME + "_" + option + "_" + msFileName;
//            double j = i;
//            final ParameterScoreModel paramScore = new ParameterScoreModel();
//            paramScore.setParamId("minPrecursorMass");
//            Future future = executor.submit(() -> {
//                resultsMap.put(j, excuteSearch(optProtDataset, updatedName, option, tempIdParam, false, optimisedSearchParameter, identificationParametersFile).size());
//                System.out.println("minPrecursorMass is top " + j + " results " + resultsMap.get(j) + "  " + optProtDataset.getActiveIdentificationNum());
//            });
//            while (!future.isDone()) {
//            }
//            paramScore.setScore(resultsMap.get(j));
//            paramScore.setParamValue(option);
//            parameterScoreSet.add(paramScore);
//            i += 100;
//        }
//        
//        int localId = -1;
//        double localSelection = 0;
//        for (double option : resultsMap.keySet()) {
//            if (resultsMap.get(option) > localId) {
//                localId = resultsMap.get(option);
//                localSelection = option;
//                
//            }
//        }
//        double factor = (localId - idRate) * 100.0 / idRate;//* optProtDataset.getDataEpsilon();;
//        factor = Math.round(factor);
//        if (factor >= optProtDataset.getAcceptedIDRatioThreshold() && resultsMap.get(localSelection) > resultsMap.get(selectedMinPrecursorMassOption)) {
//            selectedMinPrecursorMassOption = localSelection;
//            optProtDataset.setActiveIdentificationNum(localId);
//            idRate = localId;
//        }
//        resultsMap.clear();
//        
//        tempMyriMatchParameters.setMinPrecursorMass(selectedMinPrecursorMassOption);
//        for (double i = 1000; i < 6000;) {
//            tempMyriMatchParameters.setMaxPrecursorMass(i);
//            final String option = "maxPrecursorMass_" + i;
//            final String updatedName = Configurations.DEFAULT_RESULT_NAME + "_" + option + "_" + msFileName;
//            double j = i;
//            final ParameterScoreModel paramScore = new ParameterScoreModel();
//            paramScore.setParamId("maxPrecursorMass");
//            
//            Future future = executor.submit(() -> {
//                resultsMap.put(j, excuteSearch(optProtDataset, updatedName, option, tempIdParam, false, optimisedSearchParameter, identificationParametersFile).size());
//                System.out.println("PrecursorMass max  is top " + j + " results " + resultsMap.get(j) + "  " + optProtDataset.getActiveIdentificationNum());
//                paramScore.setScore(resultsMap.get(j));
//                paramScore.setParamValue(option);
//                parameterScoreSet.add(paramScore);
//            });
//            while (!future.isDone()) {
//            }
//            
//            i += 500;
//        }
//        localId = -1;
//        localSelection = 0;
//        for (double option : resultsMap.keySet()) {
//            if (resultsMap.get(option) > localId) {
//                localId = resultsMap.get(option);
//                localSelection = option;
//                
//            }
//        }
//        factor = (localId - idRate) * 100.0 / idRate;//* optProtDataset.getDataEpsilon();;
//        factor = Math.round(factor);
//        if (factor >= optProtDataset.getAcceptedIDRatioThreshold() && resultsMap.get(localSelection) > resultsMap.get(selectedMaxPrecursorMassOption)) {
//            selectedMaxPrecursorMassOption = localSelection;
//            optProtDataset.setActiveIdentificationNum(localId);
//        }
//        return new double[]{selectedMinPrecursorMassOption, selectedMaxPrecursorMassOption};
//    }
//    
//    public String optimizeFragmentationMethod(SearchingSubDataset optProtDataset, File identificationParametersFile, SearchInputSetting optimisedSearchParameter, TreeSet<ParameterScoreModel> parameterScoreSet) throws IOException {
//        Map<String, Integer> resultsMap = Collections.synchronizedMap(new LinkedHashMap<>());
//        int idRate = optProtDataset.getActiveIdentificationNum();
//        IdentificationParameters oreginaltempIdParam = IdentificationParameters.getIdentificationParameters(identificationParametersFile);
//        MyriMatchParameters myriMatchParameters = (MyriMatchParameters) oreginaltempIdParam.getSearchParameters().getAlgorithmSpecificParameters().get(Advocate.myriMatch.getIndex());
//        String msFileName = IoUtil.removeExtension(optProtDataset.getSubMsFile().getName());
//        String selectedOption = myriMatchParameters.getFragmentationRule();
//        String[] values = new String[]{"CID", "HCD", "ETD"};
//        
//        for (String str : values) {
//            final String option = "fragmentationMethod_" + str;
//            
//            final String updatedName = Configurations.DEFAULT_RESULT_NAME + "_" + option + "_" + msFileName;
//            myriMatchParameters.setFragmentationRule(str);
//            
//            final ParameterScoreModel paramScore = new ParameterScoreModel();
//            paramScore.setParamId("fragmentationMethod");
//            
//            Future future = executor.submit(() -> {
//                resultsMap.put(str, excuteSearch(optProtDataset, updatedName, option, oreginaltempIdParam, false, optimisedSearchParameter, identificationParametersFile).size());
//                paramScore.setScore(resultsMap.get(str));
//                paramScore.setParamValue(option);
//                parameterScoreSet.add(paramScore);
//            });
//            while (!future.isDone()) {
//            }
//        }
//        
//        int localId = -1;
//        String localSelection = "";
//        for (String fragmentionMethod : resultsMap.keySet()) {
//            System.out.println(" option fragmentionMethod" + fragmentionMethod + "  " + resultsMap.get(fragmentionMethod) + " > " + localId + "  " + idRate);
//            if (resultsMap.get(fragmentionMethod) > localId) {
//                localId = resultsMap.get(fragmentionMethod);
//                localSelection = fragmentionMethod;
//                
//            }
//        }
//        if (localId >= idRate) {
//            selectedOption = localSelection;
//            optProtDataset.setActiveIdentificationNum(localId);
//        }
//        return selectedOption;
//    }
//    
//    public int optimizeMaxVarPTMsNumber(SearchingSubDataset optProtDataset, File identificationParametersFile, SearchInputSetting optimisedSearchParameter, TreeSet<ParameterScoreModel> parameterScoreSet) throws IOException {
//        
//        IdentificationParameters oreginaltempIdParam = IdentificationParameters.getIdentificationParameters(identificationParametersFile);
//        MyriMatchParameters myriMatchParameters = (MyriMatchParameters) oreginaltempIdParam.getSearchParameters().getAlgorithmSpecificParameters().get(Advocate.myriMatch.getIndex());
//        Integer selectedOption = myriMatchParameters.getMaxDynamicMods();
//        int idRate = optProtDataset.getActiveIdentificationNum();
//        Map<Integer, Integer> resultsMap = Collections.synchronizedMap(new LinkedHashMap<>());
//        String msFileName = IoUtil.removeExtension(optProtDataset.getSubMsFile().getName());
//        
//        for (int i = oreginaltempIdParam.getSearchParameters().getModificationParameters().getVariableModifications().size(); i >= 0; i--) {
//            final String option = "maxVarPTMs_" + i;
//            IdentificationParameters tempIdParam = IdentificationParameters.getIdentificationParameters(identificationParametersFile);
//            myriMatchParameters = (MyriMatchParameters) tempIdParam.getSearchParameters().getAlgorithmSpecificParameters().get(Advocate.myriMatch.getIndex());
//            final String updatedName = Configurations.DEFAULT_RESULT_NAME + "_" + option + "_" + msFileName;
//            myriMatchParameters.setMaxDynamicMods(i);
//            final int j = i;
//            final ParameterScoreModel paramScore = new ParameterScoreModel();
//            paramScore.setParamId("maxVarPTMs");
//            
//            Future future = executor.submit(() -> {
//                resultsMap.put(j, excuteSearch(optProtDataset, updatedName, option, tempIdParam, false, optimisedSearchParameter, identificationParametersFile).size());
//                paramScore.setScore(resultsMap.get(j));
//                paramScore.setParamValue(option);
//                parameterScoreSet.add(paramScore);
//            });
//            while (!future.isDone()) {
//            }
//        }
//        int localId = -1;
//        int localSelection = 0;
//        for (int option : resultsMap.keySet()) {
//            System.out.println(" option #peaks " + option + "  " + resultsMap.get(option) + " > " + localId + "  " + idRate);
//            if (resultsMap.get(option) > localId) {
//                localId = resultsMap.get(option);
//                localSelection = option;
//                
//            }
//        }
//        if (localId >= idRate) {
//            selectedOption = localSelection;
//            optProtDataset.setActiveIdentificationNum(localId);
//        }
//        return selectedOption;
//    }
//    
//    public int optimizeEnzymaticTerminals(SearchingSubDataset optProtDataset, File identificationParametersFile, SearchInputSetting optimisedSearchParameter, TreeSet<ParameterScoreModel> parameterScoreSet) throws IOException {
//        Map<Integer, Integer> resultsMap = Collections.synchronizedMap(new LinkedHashMap<>());
//        int idRate = optProtDataset.getActiveIdentificationNum();
//        IdentificationParameters oreginaltempIdParam = IdentificationParameters.getIdentificationParameters(identificationParametersFile);
//        MyriMatchParameters myriMatchParameters = (MyriMatchParameters) oreginaltempIdParam.getSearchParameters().getAlgorithmSpecificParameters().get(Advocate.myriMatch.getIndex());
//        String msFileName = IoUtil.removeExtension(optProtDataset.getSubMsFile().getName());
//        int selectedOption = myriMatchParameters.getMinTerminiCleavages();
//        
//        for (int i = 0; i <= 2; i++) {
//            final String option = "enzymatricTerminals_" + i;
//            
//            final String updatedName = Configurations.DEFAULT_RESULT_NAME + "_" + option + "_" + msFileName;
//            myriMatchParameters.setMinTerminiCleavages(i);
//            
//            final ParameterScoreModel paramScore = new ParameterScoreModel();
//            paramScore.setParamId("enzymatricTerminals");
//            int j = i;
//            Future future = executor.submit(() -> {
//                resultsMap.put(j, excuteSearch(optProtDataset, updatedName, option, oreginaltempIdParam, false, optimisedSearchParameter, identificationParametersFile).size());
//                paramScore.setScore(resultsMap.get(j));
//                paramScore.setParamValue(option);
//                parameterScoreSet.add(paramScore);
//            });
//            while (!future.isDone()) {
//            }
//        }
//        
//        int localId = -1;
//        int localSelection = -1;
//        for (int enzymatricTerminals : resultsMap.keySet()) {
//            System.out.println(" option enzymatricTerminals" + enzymatricTerminals + "  " + resultsMap.get(enzymatricTerminals) + " > " + localId + "  " + idRate);
//            if (resultsMap.get(enzymatricTerminals) > localId) {
//                localId = resultsMap.get(enzymatricTerminals);
//                localSelection = enzymatricTerminals;
//                
//            }
//        }
//        if (localId >= idRate) {
//            selectedOption = localSelection;
//            optProtDataset.setActiveIdentificationNum(localId);
//        }
//        return selectedOption;
//    }
//    
//    public double optimizeoptimizeTICCutoff(SearchingSubDataset optProtDataset, File identificationParametersFile, SearchInputSetting optimisedSearchParameter, TreeSet<ParameterScoreModel> parameterScoreSet) throws IOException {
//        Map<Double, Integer> resultsMap = Collections.synchronizedMap(new LinkedHashMap<>());
//        int idRate = optProtDataset.getActiveIdentificationNum();
//        IdentificationParameters oreginaltempIdParam = IdentificationParameters.getIdentificationParameters(identificationParametersFile);
//        MyriMatchParameters myriMatchParameters = (MyriMatchParameters) oreginaltempIdParam.getSearchParameters().getAlgorithmSpecificParameters().get(Advocate.myriMatch.getIndex());
//        String msFileName = IoUtil.removeExtension(optProtDataset.getSubMsFile().getName());
//        double selectedOption = myriMatchParameters.getTicCutoffPercentage();
//        for (double i = 0.90; i <= 1;) {
//            final String option = "TICCutoff_" + i;
//            
//            final String updatedName = Configurations.DEFAULT_RESULT_NAME + "_" + option + "_" + msFileName;
//            myriMatchParameters.setTicCutoffPercentage(i);
//            final double j = i;
//            final ParameterScoreModel paramScore = new ParameterScoreModel();
//            paramScore.setParamId("TICCutoff");
//            
//            Future future = executor.submit(() -> {
//                resultsMap.put(j, excuteSearch(optProtDataset, updatedName, option, oreginaltempIdParam, false, optimisedSearchParameter, identificationParametersFile).size());
//                paramScore.setScore(resultsMap.get(j));
//                paramScore.setParamValue(option);
//                parameterScoreSet.add(paramScore);
//            });
//            while (!future.isDone()) {
//            }
//            i += 0.01;
//        }
//        int localId = -1;
//        double localSelection = 0;
//        for (double dRangeScore : resultsMap.keySet()) {
//            System.out.println(" option 1 " + dRangeScore + "  " + resultsMap.get(dRangeScore) + " > " + localId + "  " + idRate);
//            if (resultsMap.get(dRangeScore) > localId) {
//                localId = resultsMap.get(dRangeScore);
//                localSelection = dRangeScore;
//                
//            }
//        }
//        if (localId >= idRate) {
//            selectedOption = localSelection;
//            optProtDataset.setActiveIdentificationNum(localId);
//        }
//        
//        return selectedOption;
//    }
//    
//    public int optimizeNumberOfIntensityClasses(SearchingSubDataset optProtDataset, File identificationParametersFile, SearchInputSetting optimisedSearchParameter, TreeSet<ParameterScoreModel> parameterScoreSet) throws IOException {
//        
//        IdentificationParameters oreginaltempIdParam = IdentificationParameters.getIdentificationParameters(identificationParametersFile);
//        MyriMatchParameters myriMatchParameters = (MyriMatchParameters) oreginaltempIdParam.getSearchParameters().getAlgorithmSpecificParameters().get(Advocate.myriMatch.getIndex());
//        Integer selectedOption = myriMatchParameters.getNumIntensityClasses();
//        int idRate = optProtDataset.getActiveIdentificationNum();
//        Map<Integer, Integer> resultsMap = Collections.synchronizedMap(new LinkedHashMap<>());
//        String msFileName = IoUtil.removeExtension(optProtDataset.getSubMsFile().getName());
//        
//        for (int i = 1; i <= 6; i++) {
//            final String option = "NumberOfIntensityClasses_" + i;
//            final String updatedName = Configurations.DEFAULT_RESULT_NAME + "_" + option + "_" + msFileName;
//            myriMatchParameters.setNumIntensityClasses(i);
//            final int j = i;
//            final ParameterScoreModel paramScore = new ParameterScoreModel();
//            paramScore.setParamId("NumberOfIntensityClasses");
//            
//            Future future = executor.submit(() -> {
//                resultsMap.put(j, excuteSearch(optProtDataset, updatedName, option, oreginaltempIdParam, false, optimisedSearchParameter, identificationParametersFile).size());
//                paramScore.setScore(resultsMap.get(j));
//                paramScore.setParamValue(option);
//                parameterScoreSet.add(paramScore);
//            });
//            while (!future.isDone()) {
//            }
//        }
//        int localId = -1;
//        int localSelection = 0;
//        for (int option : resultsMap.keySet()) {
//            System.out.println(" option #NumberOfIntensityClasses " + option + "  " + resultsMap.get(option) + " > " + localId + "  " + idRate);
//            if (resultsMap.get(option) > localId) {
//                localId = resultsMap.get(option);
//                localSelection = option;
//                
//            }
//        }
//        if (localId >= idRate) {
//            selectedOption = localSelection;
//            optProtDataset.setActiveIdentificationNum(localId);
//        }
//        return selectedOption;
//    }
//    
//    public int optimizeClassSizeMultiplier(SearchingSubDataset optProtDataset, File identificationParametersFile, SearchInputSetting optimisedSearchParameter, TreeSet<ParameterScoreModel> parameterScoreSet) throws IOException {
//        
//        IdentificationParameters oreginaltempIdParam = IdentificationParameters.getIdentificationParameters(identificationParametersFile);
//        MyriMatchParameters myriMatchParameters = (MyriMatchParameters) oreginaltempIdParam.getSearchParameters().getAlgorithmSpecificParameters().get(Advocate.myriMatch.getIndex());
//        Integer selectedOption = myriMatchParameters.getClassSizeMultiplier();
//        int idRate = optProtDataset.getActiveIdentificationNum();
//        Map<Integer, Integer> resultsMap = Collections.synchronizedMap(new LinkedHashMap<>());
//        String msFileName = IoUtil.removeExtension(optProtDataset.getSubMsFile().getName());
//        
//        for (int i = 1; i <= 5; i++) {
//            final String option = "classSizeMultiplier_" + i;
//            final String updatedName = Configurations.DEFAULT_RESULT_NAME + "_" + option + "_" + msFileName;
//            myriMatchParameters.setClassSizeMultiplier(i);
//            final int j = i;
//            final ParameterScoreModel paramScore = new ParameterScoreModel();
//            paramScore.setParamId("classSizeMultiplier");
//            
//            Future future = executor.submit(() -> {
//                resultsMap.put(j, excuteSearch(optProtDataset, updatedName, option, oreginaltempIdParam, false, optimisedSearchParameter, identificationParametersFile).size());
//                paramScore.setScore(resultsMap.get(j));
//                paramScore.setParamValue(option);
//                parameterScoreSet.add(paramScore);
//            });
//            while (!future.isDone()) {
//            }
//        }
//        int localId = -1;
//        int localSelection = 0;
//        for (int option : resultsMap.keySet()) {
//            System.out.println(" option #classSizeMultiplier " + option + "  " + resultsMap.get(option) + " > " + localId + "  " + idRate);
//            if (resultsMap.get(option) > localId) {
//                localId = resultsMap.get(option);
//                localSelection = option;
//                
//            }
//        }
//        if (localId >= idRate) {
//            selectedOption = localSelection;
//            optProtDataset.setActiveIdentificationNum(localId);
//        }
//        return selectedOption;
//    }
//    
//    public int optimizeNumberOfBatches(SearchingSubDataset optProtDataset, File identificationParametersFile, SearchInputSetting optimisedSearchParameter, TreeSet<ParameterScoreModel> parameterScoreSet) throws IOException {
//        
//        IdentificationParameters oreginaltempIdParam = IdentificationParameters.getIdentificationParameters(identificationParametersFile);
//        MyriMatchParameters myriMatchParameters = (MyriMatchParameters) oreginaltempIdParam.getSearchParameters().getAlgorithmSpecificParameters().get(Advocate.myriMatch.getIndex());
//        Integer selectedOption = myriMatchParameters.getNumberOfBatches();
//        int idRate = optProtDataset.getActiveIdentificationNum();
//        Map<Integer, Integer> resultsMap = Collections.synchronizedMap(new LinkedHashMap<>());
//        String msFileName = IoUtil.removeExtension(optProtDataset.getSubMsFile().getName());
//        
//        for (int i = 30; i <= 80;) {
//            final String option = "NumberOfBatches_" + i;
//            final String updatedName = Configurations.DEFAULT_RESULT_NAME + "_" + option + "_" + msFileName;
//            myriMatchParameters.setNumberOfBatches(i);
//            final int j = i;
//            final ParameterScoreModel paramScore = new ParameterScoreModel();
//            paramScore.setParamId("NumberOfBatches");
//            
//            Future future = executor.submit(() -> {
//                resultsMap.put(j, excuteSearch(optProtDataset, updatedName, option, oreginaltempIdParam, false, optimisedSearchParameter, identificationParametersFile).size());
//                paramScore.setScore(resultsMap.get(j));
//                paramScore.setParamValue(option);
//                parameterScoreSet.add(paramScore);
//            });
//            while (!future.isDone()) {
//            }
//            i += 10;
//        }
//        int localId = -1;
//        int localSelection = 0;
//        for (int option : resultsMap.keySet()) {
//            System.out.println(" option #NumberOfBatches " + option + "  " + resultsMap.get(option) + " > " + localId + "  " + idRate);
//            if (resultsMap.get(option) > localId) {
//                localId = resultsMap.get(option);
//                localSelection = option;
//                
//            }
//        }
//        if (localId >= idRate) {
//            selectedOption = localSelection;
//            optProtDataset.setActiveIdentificationNum(localId);
//        }
//        return selectedOption;
//    }
//    
//    public int optimizeMaxPeakCount(SearchingSubDataset optProtDataset, File identificationParametersFile, SearchInputSetting optimisedSearchParameter, TreeSet<ParameterScoreModel> parameterScoreSet) throws IOException {
//        
//        IdentificationParameters oreginaltempIdParam = IdentificationParameters.getIdentificationParameters(identificationParametersFile);
//        MyriMatchParameters myriMatchParameters = (MyriMatchParameters) oreginaltempIdParam.getSearchParameters().getAlgorithmSpecificParameters().get(Advocate.myriMatch.getIndex());
//        Integer selectedOption = myriMatchParameters.getMaxPeakCount();
//        int idRate = optProtDataset.getActiveIdentificationNum();
//        Map<Integer, Integer> resultsMap = Collections.synchronizedMap(new LinkedHashMap<>());
//        String msFileName = IoUtil.removeExtension(optProtDataset.getSubMsFile().getName());
//        
//        for (int i = 200; i <= 400;) {
//            final String option = "maxPeakCount_" + i;
//            final String updatedName = Configurations.DEFAULT_RESULT_NAME + "_" + option + "_" + msFileName;
//            myriMatchParameters.setMaxPeakCount(i);
//            final int j = i;
//            final ParameterScoreModel paramScore = new ParameterScoreModel();
//            paramScore.setParamId("maxPeakCount");
//            
//            Future future = executor.submit(() -> {
//                resultsMap.put(j, excuteSearch(optProtDataset, updatedName, option, oreginaltempIdParam, false, optimisedSearchParameter, identificationParametersFile).size());
//                paramScore.setScore(resultsMap.get(j));
//                paramScore.setParamValue(option);
//                parameterScoreSet.add(paramScore);
//            });
//            while (!future.isDone()) {
//            }
//            i += 50;
//        }
//        int localId = -1;
//        int localSelection = 0;
//        for (int option : resultsMap.keySet()) {
//            System.out.println(" option #maxPeakCount " + option + "  " + resultsMap.get(option) + " > " + localId + "  " + idRate);
//            if (resultsMap.get(option) > localId) {
//                localId = resultsMap.get(option);
//                localSelection = option;
//                
//            }
//        }
//        if (localId >= idRate) {
//            selectedOption = localSelection;
//            optProtDataset.setActiveIdentificationNum(localId);
//        }
//        return selectedOption;
//    }
//
////
////    public int optimizeMinimumPeaks(SearchingSubDataset optProtDataset, File identificationParametersFile, SearchInputSetting optimisedSearchParameter, TreeSet<ParameterScoreModel> parameterScoreSet) throws IOException {
////        IdentificationParameters oreginaltempIdParam = IdentificationParameters.getIdentificationParameters(identificationParametersFile);
////        XtandemParameters xtandemParameters = (XtandemParameters) oreginaltempIdParam.getSearchParameters().getAlgorithmSpecificParameters().get(Advocate.xtandem.getIndex());
////        Integer selectedOption = xtandemParameters.getMinPeaksPerSpectrum();
////        int idRate = optProtDataset.getActiveIdentificationNum();
////        Map<Integer, Integer> resultsMap = Collections.synchronizedMap(new LinkedHashMap<>());
////        String msFileName = IoUtil.removeExtension(optProtDataset.getSubMsFile().getName());
////        int lastValue = 0;
////        for (int i = 5; i <= 100;) {
////            final String option = "minpeaksNum_" + i;
////            IdentificationParameters tempIdParam = IdentificationParameters.getIdentificationParameters(identificationParametersFile);
////            xtandemParameters = (XtandemParameters) tempIdParam.getSearchParameters().getAlgorithmSpecificParameters().get(Advocate.xtandem.getIndex());
////            final String updatedName = Configurations.DEFAULT_RESULT_NAME + "_" + option + "_" + msFileName;
////            xtandemParameters.setMinPeaksPerSpectrum(i);
////            final int j = i;
////            final ParameterScoreModel paramScore = new ParameterScoreModel();
////            paramScore.setParamId("minpeaksNum");
////
////            Future future = executor.submit(() -> {
////                resultsMap.put(j, excuteSearch(optProtDataset, updatedName, option, tempIdParam, false, optimisedSearchParameter, identificationParametersFile).size());
////                paramScore.setScore(resultsMap.get(j));
////                paramScore.setParamValue(option);
////                parameterScoreSet.add(paramScore);
////            });
////            while (!future.isDone()) {
////            }
////            if (lastValue > resultsMap.get(i)) {
////                break;
////            }
////            lastValue = resultsMap.get(i);
////            i += 10;
////
////        }
////        int localId = -1;
////        int localSelection = 0;
////        for (int option : resultsMap.keySet()) {
////            if (resultsMap.get(option) > localId) {
////                localId = resultsMap.get(option);
////                localSelection = option;
////
////            }
////        }
////        if (localId >= idRate) {
////            selectedOption = localSelection;
////            optProtDataset.setActiveIdentificationNum(localId);
////        }
////        return selectedOption;
////    }
////
////    public double optimizeNoiseSuppression(SearchingSubDataset optProtDataset, File identificationParametersFile, SearchInputSetting optimisedSearchParameter, TreeSet<ParameterScoreModel> parameterScoreSet) throws IOException {
////        Map<Double, Integer> resultsMap = Collections.synchronizedMap(new LinkedHashMap<>());
////        int idRate = optProtDataset.getActiveIdentificationNum();
////        IdentificationParameters oreginaltempIdParam = IdentificationParameters.getIdentificationParameters(identificationParametersFile);
////        XtandemParameters xtandemParameters = (XtandemParameters) oreginaltempIdParam.getSearchParameters().getAlgorithmSpecificParameters().get(Advocate.xtandem.getIndex());
////        String msFileName = IoUtil.removeExtension(optProtDataset.getSubMsFile().getName());
////        double selectedOption = xtandemParameters.getMinPrecursorMass();
////        final String option = "noiseSupression_" + false;
////        final String updatedName = Configurations.DEFAULT_RESULT_NAME + "_" + option + "_" + msFileName;
////        xtandemParameters.setUseNoiseSuppression(false);
////        final ParameterScoreModel paramScore = new ParameterScoreModel();
////        paramScore.setParamId("noiseSupression");
////
////        Future future = executor.submit(() -> {
////            resultsMap.put(0.0, excuteSearch(optProtDataset, updatedName, option, oreginaltempIdParam, false, optimisedSearchParameter, identificationParametersFile).size());
////            paramScore.setScore(resultsMap.get(0.0));
////            paramScore.setParamValue(option);
////            parameterScoreSet.add(paramScore);
////        });
////        while (!future.isDone()) {
////        }
////        xtandemParameters.setUseNoiseSuppression(true);
////        for (double j = 500; j < 1600;) {
////            final String suboption = "noiseSupression_" + true + "_" + j;
////            final String subupdatedName = Configurations.DEFAULT_RESULT_NAME + "_" + suboption + "_" + msFileName;
////            final double i = j;
////            xtandemParameters.setMinPrecursorMass(j);
////            final ParameterScoreModel paramScore2 = new ParameterScoreModel();
////            paramScore2.setParamId("noiseSupression");
////            future = executor.submit(() -> {
////                resultsMap.put(i, excuteSearch(optProtDataset, subupdatedName, suboption, oreginaltempIdParam, false, optimisedSearchParameter, identificationParametersFile).size());
////                paramScore2.setScore(resultsMap.get(i));
////                paramScore2.setParamValue(option);
////                parameterScoreSet.add(paramScore2);
////            });
////            while (!future.isDone()) {
////            }
////            j += 350;
////
////        }
////
////        int localId = -1;
////        double localSelection = 0;
////        for (double dRangeScore : resultsMap.keySet()) {
//////            System.out.println(" option 1 " + option + "  " + resultsMap.get(option) + " > " + localId + "  " + idRate);
////            if (resultsMap.get(dRangeScore) > localId) {
////                localId = resultsMap.get(dRangeScore);
////                localSelection = dRangeScore;
////
////            }
////        }
////        if (localId >= idRate) {
////            selectedOption = localSelection;
////            optProtDataset.setActiveIdentificationNum(localId);
////        }
////
////        return selectedOption;
////    }
//////
////
////    public boolean optimizeParentIsotopExpansion(SearchingSubDataset optProtDataset, File identificationParametersFile, SearchInputSetting optimisedSearchParameter, TreeSet<ParameterScoreModel> parameterScoreSet) throws IOException {
////        Map<Integer, Integer> resultsMap = Collections.synchronizedMap(new LinkedHashMap<>());
////        int idRate = optProtDataset.getActiveIdentificationNum();
////        IdentificationParameters oreginaltempIdParam = IdentificationParameters.getIdentificationParameters(identificationParametersFile);
////        XtandemParameters xtandemParameters = (XtandemParameters) oreginaltempIdParam.getSearchParameters().getAlgorithmSpecificParameters().get(Advocate.xtandem.getIndex());
////        String msFileName = IoUtil.removeExtension(optProtDataset.getSubMsFile().getName());
////        boolean selectedOption = xtandemParameters.getParentMonoisotopicMassIsotopeError();
////
////        for (int i = 0; i < 2; i++) {
////            final String option = "parentMonoisotopicMassIsotopeError_" + (i == 1);
////            final String updatedName = Configurations.DEFAULT_RESULT_NAME + "_" + option + "_" + msFileName;
////            xtandemParameters.setParentMonoisotopicMassIsotopeError(i == 1);
////            final int j = i;
////            final ParameterScoreModel paramScore = new ParameterScoreModel();
////            paramScore.setParamId("parentMonoisotopicMassIsotopeError");
////
////            Future future = executor.submit(() -> {
////                resultsMap.put(j, excuteSearch(optProtDataset, updatedName, option, oreginaltempIdParam, false, optimisedSearchParameter, identificationParametersFile).size());
////                paramScore.setScore(resultsMap.get(j));
////                paramScore.setParamValue(option);
////                parameterScoreSet.add(paramScore);
////            });
////            while (!future.isDone()) {
////            }
//////            System.out.println("at parent istop " + option + " " + resultsMap.get(i));
////        }
////
////        int localId = -1;
////        int localSelection = 0;
////        for (int dRangeScore : resultsMap.keySet()) {
////            if (resultsMap.get(dRangeScore) > localId) {
////                localId = resultsMap.get(dRangeScore);
////                localSelection = dRangeScore;
////
////            }
////        }
////        if (localId >= idRate) {
////            selectedOption = localSelection == 1;
////            optProtDataset.setActiveIdentificationNum(localId);
////        }
////        return selectedOption;
////    }
//////
////
////    public boolean optimizeQuickAcetyl(SearchingSubDataset optProtDataset, File identificationParametersFile, SearchInputSetting optimisedSearchParameter, TreeSet<ParameterScoreModel> parameterScoreSet) throws IOException {
////        Map<Integer, Integer> resultsMap = Collections.synchronizedMap(new LinkedHashMap<>());
////        int idRate = optProtDataset.getActiveIdentificationNum();
////        IdentificationParameters oreginaltempIdParam = IdentificationParameters.getIdentificationParameters(identificationParametersFile);
////        XtandemParameters xtandemParameters = (XtandemParameters) oreginaltempIdParam.getSearchParameters().getAlgorithmSpecificParameters().get(Advocate.xtandem.getIndex());
////        String msFileName = IoUtil.removeExtension(optProtDataset.getSubMsFile().getName());
////        boolean selectedOption = xtandemParameters.isProteinQuickAcetyl();
////
////        for (int i = 0; i < 2; i++) {
////            boolean useQuickAcetyl = (i == 1);
////            final String option = "useQuickAcetyl_" + useQuickAcetyl;
////            final String updatedName = Configurations.DEFAULT_RESULT_NAME + "_" + option + "_" + msFileName;
////            xtandemParameters.setProteinQuickAcetyl(useQuickAcetyl);
////            final int j = i;
////            final ParameterScoreModel paramScore = new ParameterScoreModel();
////            paramScore.setParamId("QuickAcetyl");
////            Future future = executor.submit(() -> {
////                resultsMap.put(j, excuteSearch(optProtDataset, updatedName, option, oreginaltempIdParam, false, optimisedSearchParameter, identificationParametersFile).size());
////                paramScore.setScore(resultsMap.get(j));
////                paramScore.setParamValue(option);
////                parameterScoreSet.add(paramScore);
////            });
////            while (!future.isDone()) {
////            }
//////            System.out.println("at quick acetyle " + useQuickAcetyl + " " + resultsMap.get(j));
////        }
////
////        int localId = -1;
////        int localSelection = 0;
////        for (int dRangeScore : resultsMap.keySet()) {
////            if (resultsMap.get(dRangeScore) > localId) {
////                localId = resultsMap.get(dRangeScore);
////                localSelection = dRangeScore;
////
////            }
////        }
////        if (localId >= idRate) {
////            selectedOption = localSelection == 1;
////            optProtDataset.setActiveIdentificationNum(localId);
////        }
////        return selectedOption;
////    }
////
////    public boolean optimizeQuickPyrolidone(SearchingSubDataset optProtDataset, File identificationParametersFile, SearchInputSetting optimisedSearchParameter, TreeSet<ParameterScoreModel> parameterScoreSet) throws IOException {
////        Map<Integer, Integer> resultsMap = Collections.synchronizedMap(new LinkedHashMap<>());
////        int idRate = optProtDataset.getActiveIdentificationNum();
////        IdentificationParameters oreginaltempIdParam = IdentificationParameters.getIdentificationParameters(identificationParametersFile);
////        XtandemParameters xtandemParameters = (XtandemParameters) oreginaltempIdParam.getSearchParameters().getAlgorithmSpecificParameters().get(Advocate.xtandem.getIndex());
////        String msFileName = IoUtil.removeExtension(optProtDataset.getSubMsFile().getName());
////        boolean selectedOption = xtandemParameters.isQuickPyrolidone();
////
////        for (int i = 0; i < 2; i++) {
////            boolean useQuickPyrolidone = (i == 1);
////            final String option = "useQuickPyrolidone_" + useQuickPyrolidone;
////            final String updatedName = Configurations.DEFAULT_RESULT_NAME + "_" + option + "_" + msFileName;
////            xtandemParameters.setQuickPyrolidone(useQuickPyrolidone);
////            final int j = i;
////            final ParameterScoreModel paramScore = new ParameterScoreModel();
////            paramScore.setParamId("QuickPyrolidone");
////
////            Future future = executor.submit(() -> {
////                resultsMap.put(j, excuteSearch(optProtDataset, updatedName, option, oreginaltempIdParam, false, optimisedSearchParameter, identificationParametersFile).size());
////                paramScore.setScore(resultsMap.get(j));
////                paramScore.setParamValue(option);
////                parameterScoreSet.add(paramScore);
////            });
////            while (!future.isDone()) {
////            }
////            System.out.println("at quick prolyien  " + useQuickPyrolidone + " " + resultsMap.get(j));
////        }
////
////        int localId = -1;
////        int localSelection = 0;
////        for (int dRangeScore : resultsMap.keySet()) {
////            if (resultsMap.get(dRangeScore) > localId) {
////                localId = resultsMap.get(dRangeScore);
////                localSelection = dRangeScore;
////
////            }
////        }
////        if (localId >= idRate) {
////            selectedOption = localSelection == 1;
////            optProtDataset.setActiveIdentificationNum(localId);
////        }
////
////        return selectedOption;
////    }
//////
////
////    public boolean optimizeStPBias(SearchingSubDataset optProtDataset, File identificationParametersFile, SearchInputSetting optimisedSearchParameter, TreeSet<ParameterScoreModel> parameterScoreSet) throws IOException {
////        Map<Integer, Integer> resultsMap = Collections.synchronizedMap(new LinkedHashMap<>());
////        int idRate = optProtDataset.getActiveIdentificationNum();
////        IdentificationParameters oreginaltempIdParam = IdentificationParameters.getIdentificationParameters(identificationParametersFile);
////        XtandemParameters xtandemParameters = (XtandemParameters) oreginaltempIdParam.getSearchParameters().getAlgorithmSpecificParameters().get(Advocate.xtandem.getIndex());
////        String msFileName = IoUtil.removeExtension(optProtDataset.getSubMsFile().getName());
////        boolean selectedOption = xtandemParameters.isStpBias();
////
////        for (int i = 0; i < 2; i++) {
////            boolean useStpBias = (i == 1);
////            final String option = "useStpBias_" + useStpBias;
////            final String updatedName = Configurations.DEFAULT_RESULT_NAME + "_" + option + "_" + msFileName;
////            xtandemParameters.setStpBias(useStpBias);
////            final int j = i;
////            final ParameterScoreModel paramScore = new ParameterScoreModel();
////            paramScore.setParamId("StpBias");
////
////            Future future = executor.submit(() -> {
////                resultsMap.put(j, excuteSearch(optProtDataset, updatedName, option, oreginaltempIdParam, false, optimisedSearchParameter, identificationParametersFile).size());
////                paramScore.setScore(resultsMap.get(j));
////                paramScore.setParamValue(option);
////                parameterScoreSet.add(paramScore);
////            });
////            while (!future.isDone()) {
////            }
////
//////            System.out.println("at useStpBias  " + useStpBias + " " + resultsMap.get(j));
////        }
////
////        int localId = -1;
////        int localSelection = 0;
////        for (int dRangeScore : resultsMap.keySet()) {
////            if (resultsMap.get(dRangeScore) > localId) {
////                localId = resultsMap.get(dRangeScore);
////                localSelection = dRangeScore;
////
////            }
////        }
////        if (localId >= idRate) {
////            selectedOption = localSelection == 1;
////            optProtDataset.setActiveIdentificationNum(localId);
////        }
////
////        return selectedOption;
////    }
//////
////
//    public boolean optimizeUseSmartPlus3Model(SearchingSubDataset optProtDataset, File identificationParametersFile, SearchInputSetting optimisedSearchParameter, TreeSet<ParameterScoreModel> parameterScoreSet) throws IOException {
//        Map<Integer, Integer> resultsMap = Collections.synchronizedMap(new LinkedHashMap<>());
//        int idRate = optProtDataset.getActiveIdentificationNum();
//        IdentificationParameters oreginaltempIdParam = IdentificationParameters.getIdentificationParameters(identificationParametersFile);
//        MyriMatchParameters myriMatchParameters = (MyriMatchParameters) oreginaltempIdParam.getSearchParameters().getAlgorithmSpecificParameters().get(Advocate.myriMatch.getIndex());
//        
//        String msFileName = IoUtil.removeExtension(optProtDataset.getSubMsFile().getName());
//        boolean selectedOption = myriMatchParameters.getUseSmartPlusThreeModel();
//        
//        for (int i = 0; i < 2; i++) {
//            boolean useSmartPlus3Model = (i == 1);
//            final String option = "useSmartPlus3Model_" + useSmartPlus3Model;
//            final String updatedName = Configurations.DEFAULT_RESULT_NAME + "_" + option + "_" + msFileName;
//            myriMatchParameters.setUseSmartPlusThreeModel(useSmartPlus3Model);
//            final int j = i;
//            final ParameterScoreModel paramScore = new ParameterScoreModel();
//            paramScore.setParamId("useSmartPlus3Model");
//            
//            Future future = executor.submit(() -> {
//                resultsMap.put(j, excuteSearch(optProtDataset, updatedName, option, oreginaltempIdParam, false, optimisedSearchParameter, identificationParametersFile).size());
//                paramScore.setScore(resultsMap.get(j));
//                paramScore.setParamValue(option);
//                parameterScoreSet.add(paramScore);
//            });
//            while (!future.isDone()) {
//            }
//        }
//        
//        int localId = -1;
//        int localSelection = 0;
//        for (int dRangeScore : resultsMap.keySet()) {
//            if (resultsMap.get(dRangeScore) > localId) {
//                localId = resultsMap.get(dRangeScore);
//                localSelection = dRangeScore;
//                
//            }
//        }
//        if (localId >= idRate) {
//            selectedOption = localSelection == 1;
//            optProtDataset.setActiveIdentificationNum(localId);
//        }
//        
//        return selectedOption;
//        
//    }
//    
//    public boolean optimizeComputeXCorr(SearchingSubDataset optProtDataset, File identificationParametersFile, SearchInputSetting optimisedSearchParameter, TreeSet<ParameterScoreModel> parameterScoreSet) throws IOException {
//        Map<Integer, Integer> resultsMap = Collections.synchronizedMap(new LinkedHashMap<>());
//        int idRate = optProtDataset.getActiveIdentificationNum();
//        IdentificationParameters oreginaltempIdParam = IdentificationParameters.getIdentificationParameters(identificationParametersFile);
//        MyriMatchParameters myriMatchParameters = (MyriMatchParameters) oreginaltempIdParam.getSearchParameters().getAlgorithmSpecificParameters().get(Advocate.myriMatch.getIndex());
//        
//        String msFileName = IoUtil.removeExtension(optProtDataset.getSubMsFile().getName());
//        boolean selectedOption = myriMatchParameters.getComputeXCorr();
//        
//        for (int i = 0; i < 2; i++) {
//            boolean computeXCorr = (i == 1);
//            final String option = "computeXCorr_" + computeXCorr;
//            final String updatedName = Configurations.DEFAULT_RESULT_NAME + "_" + option + "_" + msFileName;
//            myriMatchParameters.setUseSmartPlusThreeModel(computeXCorr);
//            final int j = i;
//            final ParameterScoreModel paramScore = new ParameterScoreModel();
//            paramScore.setParamId("computeXCorr");
//            
//            Future future = executor.submit(() -> {
//                resultsMap.put(j, excuteSearch(optProtDataset, updatedName, option, oreginaltempIdParam, false, optimisedSearchParameter, identificationParametersFile).size());
//                paramScore.setScore(resultsMap.get(j));
//                paramScore.setParamValue(option);
//                parameterScoreSet.add(paramScore);
//            });
//            while (!future.isDone()) {
//            }
//        }
//        
//        int localId = -1;
//        int localSelection = 0;
//        for (int dRangeScore : resultsMap.keySet()) {
//            if (resultsMap.get(dRangeScore) > localId) {
//                localId = resultsMap.get(dRangeScore);
//                localSelection = dRangeScore;
//                
//            }
//        }
//        if (localId >= idRate) {
//            selectedOption = localSelection == 1;
//            optProtDataset.setActiveIdentificationNum(localId);
//        }
//        
//        return selectedOption;
//        
//    }
//
////
////
////    public boolean optimizeRefineUnanticipatedCleavage(SearchingSubDataset optProtDataset, File identificationParametersFile, SearchInputSetting optimisedSearchParameter, TreeSet<ParameterScoreModel> parameterScoreSet) throws IOException {
////        Map<Integer, Integer> resultsMap = Collections.synchronizedMap(new LinkedHashMap<>());
////        int idRate = optProtDataset.getActiveIdentificationNum();
////        IdentificationParameters oreginaltempIdParam = IdentificationParameters.getIdentificationParameters(identificationParametersFile);
////        XtandemParameters xtandemParameters = (XtandemParameters) oreginaltempIdParam.getSearchParameters().getAlgorithmSpecificParameters().get(Advocate.xtandem.getIndex());
////        xtandemParameters.setRefine(true);
////        String msFileName = IoUtil.removeExtension(optProtDataset.getSubMsFile().getName());
////        boolean selectedOption = xtandemParameters.isRefineUnanticipatedCleavages();
////
////        for (int i = 0; i < 2; i++) {
////            boolean useRefineUnanticipatedCleavages = (i == 1);
////            final String option = "useRefineUnanticipatedCleavages_" + useRefineUnanticipatedCleavages;
////            final String updatedName = Configurations.DEFAULT_RESULT_NAME + "_" + option + "_" + msFileName;
////            xtandemParameters.setRefineUnanticipatedCleavages(useRefineUnanticipatedCleavages);
////            final int j = i;
////            final ParameterScoreModel paramScore = new ParameterScoreModel();
////            paramScore.setParamId("UnanticipatedCleavages");
////
////            Future future = executor.submit(() -> {
////                resultsMap.put(j, excuteSearch(optProtDataset, updatedName, option, oreginaltempIdParam, false, optimisedSearchParameter, identificationParametersFile).size());
////                paramScore.setScore(resultsMap.get(j));
////                paramScore.setParamValue(option);
////                parameterScoreSet.add(paramScore);
////            });
////            while (!future.isDone()) {
////            }
////        }
////
////        int localId = -1;
////        int localSelection = 0;
////        for (int dRangeScore : resultsMap.keySet()) {
////            if (resultsMap.get(dRangeScore) > localId) {
////                localId = resultsMap.get(dRangeScore);
////                localSelection = dRangeScore;
////
////            }
////        }
////        if (localId >= idRate) {
////            selectedOption = localSelection == 1;
////            optProtDataset.setActiveIdentificationNum(localId);
////        }
////
////        return selectedOption;
////
////    }
//////
////
////    public boolean optimizeRefineSimiEnzymaticCleavage(SearchingSubDataset optProtDataset, File identificationParametersFile, SearchInputSetting optimisedSearchParameter, TreeSet<ParameterScoreModel> parameterScoreSet) throws IOException {
////        Map<Integer, Integer> resultsMap = Collections.synchronizedMap(new LinkedHashMap<>());
////        int idRate = optProtDataset.getActiveIdentificationNum();
////        IdentificationParameters oreginaltempIdParam = IdentificationParameters.getIdentificationParameters(identificationParametersFile);
////        XtandemParameters xtandemParameters = (XtandemParameters) oreginaltempIdParam.getSearchParameters().getAlgorithmSpecificParameters().get(Advocate.xtandem.getIndex());
////        xtandemParameters.setRefine(true);
////        String msFileName = IoUtil.removeExtension(optProtDataset.getSubMsFile().getName());
////        boolean selectedOption = xtandemParameters.isRefineSemi();
////
////        for (int i = 0; i < 2; i++) {
////            boolean useRefineSimiEnzymaticCleavage = (i == 1);
////            final String option = "useRefineSimiEnzymaticCleavage_" + useRefineSimiEnzymaticCleavage;
////            final String updatedName = Configurations.DEFAULT_RESULT_NAME + "_" + option + "_" + msFileName;
////            xtandemParameters.setRefineSemi(useRefineSimiEnzymaticCleavage);
////            final int j = i;
////            final ParameterScoreModel paramScore = new ParameterScoreModel();
////            paramScore.setParamId("SimiEnzymaticCleavage");
////
////            Future future = executor.submit(() -> {
////                resultsMap.put(j, excuteSearch(optProtDataset, updatedName, option, oreginaltempIdParam, false, optimisedSearchParameter, identificationParametersFile).size());
////                paramScore.setScore(resultsMap.get(j));
////                paramScore.setParamValue(option);
////                parameterScoreSet.add(paramScore);
////            });
////            while (!future.isDone()) {
////            }
////        }
////
////        int localId = -1;
////        int localSelection = 0;
////        for (int dRangeScore : resultsMap.keySet()) {
////            if (resultsMap.get(dRangeScore) > localId) {
////                localId = resultsMap.get(dRangeScore);
////                localSelection = dRangeScore;
////
////            }
////        }
////        if (localId >= idRate) {
////            selectedOption = localSelection == 1;
////            optProtDataset.setActiveIdentificationNum(localId);
////        }
////
////        return selectedOption;
////
////    }
//////
////
////    public boolean optimizePotintialModification(SearchingSubDataset optProtDataset, File identificationParametersFile, SearchInputSetting optimisedSearchParameter, TreeSet<ParameterScoreModel> parameterScoreSet) throws IOException {
////        Map<Integer, Integer> resultsMap = Collections.synchronizedMap(new LinkedHashMap<>());
////        int idRate = optProtDataset.getActiveIdentificationNum();
////        IdentificationParameters oreginaltempIdParam = IdentificationParameters.getIdentificationParameters(identificationParametersFile);
////        XtandemParameters xtandemParameters = (XtandemParameters) oreginaltempIdParam.getSearchParameters().getAlgorithmSpecificParameters().get(Advocate.xtandem.getIndex());
////        xtandemParameters.setRefine(true);
////        String msFileName = IoUtil.removeExtension(optProtDataset.getSubMsFile().getName());
////        boolean selectedOption = xtandemParameters.isPotentialModificationsForFullRefinment();
////
////        for (int i = 0; i < 2; i++) {
////            boolean usePotintialModification = (i == 1);
////            final String option = "usePotintialModification_" + usePotintialModification;
////            final String updatedName = Configurations.DEFAULT_RESULT_NAME + "_" + option + "_" + msFileName;
////            xtandemParameters.setPotentialModificationsForFullRefinment(usePotintialModification);
////            final int j = i;
////            final ParameterScoreModel paramScore = new ParameterScoreModel();
////            paramScore.setParamId("PotintialModification");
////
////            Future future = executor.submit(() -> {
////                resultsMap.put(j, excuteSearch(optProtDataset, updatedName, option, oreginaltempIdParam, false, optimisedSearchParameter, identificationParametersFile).size());
////                paramScore.setScore(resultsMap.get(j));
////                paramScore.setParamValue(option);
////                parameterScoreSet.add(paramScore);
////            });
////            while (!future.isDone()) {
////            }
////        }
////
////        int localId = -1;
////        int localSelection = 0;
////        for (int dRangeScore : resultsMap.keySet()) {
////            if (resultsMap.get(dRangeScore) > localId) {
////                localId = resultsMap.get(dRangeScore);
////                localSelection = dRangeScore;
////
////            }
////        }
////        if (localId >= idRate) {
////            selectedOption = localSelection == 1;
////            optProtDataset.setActiveIdentificationNum(localId);
////        }
////
////        return selectedOption;
////
////    }
//////
////
////    public boolean optimizeRefinePointMutations(SearchingSubDataset optProtDataset, File identificationParametersFile, SearchInputSetting optimisedSearchParameter, TreeSet<ParameterScoreModel> parameterScoreSet) throws IOException {
////        Map<Integer, Integer> resultsMap = Collections.synchronizedMap(new LinkedHashMap<>());
////        int idRate = optProtDataset.getActiveIdentificationNum();
////        IdentificationParameters oreginaltempIdParam = IdentificationParameters.getIdentificationParameters(identificationParametersFile);
////        XtandemParameters xtandemParameters = (XtandemParameters) oreginaltempIdParam.getSearchParameters().getAlgorithmSpecificParameters().get(Advocate.xtandem.getIndex());
////        xtandemParameters.setRefine(true);
////        String msFileName = IoUtil.removeExtension(optProtDataset.getSubMsFile().getName());
////        boolean selectedOption = xtandemParameters.isRefinePointMutations();
////
////        for (int i = 0; i < 2; i++) {
////            boolean useRefinePointMutations = (i == 1);
////            final String option = "useRefinePointMutations_" + useRefinePointMutations;
////            final String updatedName = Configurations.DEFAULT_RESULT_NAME + "_" + option + "_" + msFileName;
////            xtandemParameters.setRefinePointMutations(useRefinePointMutations);
////            final int j = i;
////            final ParameterScoreModel paramScore = new ParameterScoreModel();
////            paramScore.setParamId("PointMutations");
////
////            Future future = executor.submit(() -> {
////                resultsMap.put(j, excuteSearch(optProtDataset, updatedName, option, oreginaltempIdParam, false, optimisedSearchParameter, identificationParametersFile).size());
////                paramScore.setScore(resultsMap.get(j));
////                paramScore.setParamValue(option);
////                parameterScoreSet.add(paramScore);
////            });
////            while (!future.isDone()) {
////            }
////        }
////
////        int localId = -1;
////        int localSelection = 0;
////        for (int dRangeScore : resultsMap.keySet()) {
////            if (resultsMap.get(dRangeScore) > localId) {
////                localId = resultsMap.get(dRangeScore);
////                localSelection = dRangeScore;
////
////            }
////        }
////        if (localId >= idRate) {
////            selectedOption = localSelection == 1;
////            optProtDataset.setActiveIdentificationNum(localId);
////        }
////
////        return selectedOption;
////
////    }
//////
//////
////
////    public boolean optimizeRefineSnAPs(SearchingSubDataset optProtDataset, File identificationParametersFile, SearchInputSetting optimisedSearchParameter, TreeSet<ParameterScoreModel> parameterScoreSet) throws IOException {
////        Map<Integer, Integer> resultsMap = Collections.synchronizedMap(new LinkedHashMap<>());
////        int idRate = optProtDataset.getActiveIdentificationNum();
////        IdentificationParameters oreginaltempIdParam = IdentificationParameters.getIdentificationParameters(identificationParametersFile);
////        XtandemParameters xtandemParameters = (XtandemParameters) oreginaltempIdParam.getSearchParameters().getAlgorithmSpecificParameters().get(Advocate.xtandem.getIndex());
////        xtandemParameters.setRefine(true);
////        String msFileName = IoUtil.removeExtension(optProtDataset.getSubMsFile().getName());
////        boolean selectedOption = xtandemParameters.isRefineSnaps();
////
////        for (int i = 0; i < 2; i++) {
////            boolean useRefineSnAPs = (i == 1);
////            final String option = "useRefineSnAPs_" + useRefineSnAPs;
////            final String updatedName = Configurations.DEFAULT_RESULT_NAME + "_" + option + "_" + msFileName;
////            xtandemParameters.setRefineSnaps(useRefineSnAPs);
////            final int j = i;
////            final ParameterScoreModel paramScore = new ParameterScoreModel();
////            paramScore.setParamId("SnAPs");
////
////            Future future = executor.submit(() -> {
////                resultsMap.put(j, excuteSearch(optProtDataset, updatedName, option, oreginaltempIdParam, false, optimisedSearchParameter, identificationParametersFile).size());
////                paramScore.setScore(resultsMap.get(j));
////                paramScore.setParamValue(option);
////                parameterScoreSet.add(paramScore);
////            });
////            while (!future.isDone()) {
////            }
////        }
////
////        int localId = -1;
////        int localSelection = 0;
////        for (int dRangeScore : resultsMap.keySet()) {
////            if (resultsMap.get(dRangeScore) > localId) {
////                localId = resultsMap.get(dRangeScore);
////                localSelection = dRangeScore;
////
////            }
////        }
////        if (localId >= idRate) {
////            selectedOption = localSelection == 1;
////            optProtDataset.setActiveIdentificationNum(localId);
////        }
////
////        return selectedOption;
////
////    }
////
////    public boolean optimizeRefineSpectrumSynthesis(SearchingSubDataset optProtDataset, File identificationParametersFile, SearchInputSetting optimisedSearchParameter, TreeSet<ParameterScoreModel> parameterScoreSet) throws IOException {
////        Map<Integer, Integer> resultsMap = Collections.synchronizedMap(new LinkedHashMap<>());
////        int idRate = optProtDataset.getActiveIdentificationNum();
////        IdentificationParameters oreginaltempIdParam = IdentificationParameters.getIdentificationParameters(identificationParametersFile);
////        XtandemParameters xtandemParameters = (XtandemParameters) oreginaltempIdParam.getSearchParameters().getAlgorithmSpecificParameters().get(Advocate.xtandem.getIndex());
////        xtandemParameters.setRefine(true);
////        String msFileName = IoUtil.removeExtension(optProtDataset.getSubMsFile().getName());
////        boolean selectedOption = xtandemParameters.isRefineSpectrumSynthesis();
////
////        for (int i = 0; i < 2; i++) {
////            boolean useRefineSpectrumSynthesis = (i == 1);
////            final String option = "useRefineSpectrumSynthesis_" + useRefineSpectrumSynthesis;
////            final String updatedName = Configurations.DEFAULT_RESULT_NAME + "_" + option + "_" + msFileName;
////            xtandemParameters.setRefineSpectrumSynthesis(useRefineSpectrumSynthesis);
////            final int j = i;
////            final ParameterScoreModel paramScore = new ParameterScoreModel();
////            paramScore.setParamId("SpectrumSynthesis");
////
////            Future future = executor.submit(() -> {
////                resultsMap.put(j, excuteSearch(optProtDataset, updatedName, option, oreginaltempIdParam, false, optimisedSearchParameter, identificationParametersFile).size());
////                paramScore.setScore(resultsMap.get(j));
////                paramScore.setParamValue(option);
////                parameterScoreSet.add(paramScore);
////            });
////            while (!future.isDone()) {
////            }
////        }
////
////        int localId = -1;
////        int localSelection = 0;
////        for (int dRangeScore : resultsMap.keySet()) {
////            if (resultsMap.get(dRangeScore) > localId) {
////                localId = resultsMap.get(dRangeScore);
////                localSelection = dRangeScore;
////
////            }
////        }
////        if (localId >= idRate) {
////            selectedOption = localSelection == 1;
////            optProtDataset.setActiveIdentificationNum(localId);
////        }
////
////        return selectedOption;
////
////    }
}
