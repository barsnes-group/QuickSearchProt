package no.uib.probe.quicksearchprot.handllers;

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
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import no.uib.probe.quicksearchprot.configurations.Configurations;
import no.uib.probe.quicksearchprot.model.ParameterScoreModel;
import no.uib.probe.quicksearchprot.model.RawScoreModel;
import no.uib.probe.quicksearchprot.model.SearchInputSetting;
import no.uib.probe.quicksearchprot.model.SearchingSubDataset;
import no.uib.probe.quicksearchprot.model.SortedPTMs;
import no.uib.probe.quicksearchprot.util.MainUtilities;
import no.uib.probe.quicksearchprot.util.ScoreComparisonUtilities;
import no.uib.probe.quicksearchprot.util.SpectraUtilities;

/**
 * Abstract handler for common search parameter optimization and evaluation
 * routines. Provides methods to optimize search parameters such as enzyme
 * selection, cleavage, fragment/precursor tolerances, charge, isotopic
 * correction, and PTM modifications.
 *
 * @author yehia mokhtar farag
 */
public abstract class CommonSearchHandler {

    /**
     * The compomics PTM factory.
     */
    private final ModificationFactory ptmFactory = ModificationFactory.getInstance();
    /**
     * The score set is use to calculate score confidence
     */
    private final TreeSet<Double> scoresSet = new TreeSet<>();

    // ========================== CLEAVAGE PARAMETER ==========================
    /**
     * Optimizes the protein digestion cleavage parameter by evaluating
     * different cleavage strategies. This routine tests predefined cleavage
     * types (currently "wholeProtein" and "unSpecific") to discover which
     * yields the best confidence score on the provided dataset.
     *
     * For each candidate: - Updates the digestion parameter. - Executes a
     * search using {@link #excuteSearch}. - Tracks and compares scores. Selects
     * the one that improves the identification confidence the most.
     *
     * @param optProtDataset the protein dataset to optimize search parameters
     * for
     * @param identificationParametersFile parameter file containing current
     * search settings
     * @param optimisedSearchParameter structure holding the optimization
     * flags/settings
     * @param parameterScoreSet set to collect the ParameterScoreModel results
     * @return The name of the best cleavage parameter found (e.g.,
     * "wholeProtein" or "unSpecific")
     * @throws IOException if reading parameters fails
     */
    public String optimizeDigestionCleavageParameter(
            SearchingSubDataset optProtDataset,
            File identificationParametersFile,
            SearchInputSetting optimisedSearchParameter,
            TreeSet<ParameterScoreModel> parameterScoreSet
    ) throws IOException {

        // For reporting confidence and scoring of this parameter optimization
        final ParameterScoreModel paramScore = new ParameterScoreModel();
        paramScore.setParamId("CleavageParameter");

        // Retrieve the original identification parameters from file
        IdentificationParameters originalTempIdParam = IdentificationParameters.getIdentificationParameters(identificationParametersFile);

        // Start with current cleavage parameter value as the default selected
        String selectedOption = originalTempIdParam.getSearchParameters()
                .getDigestionParameters()
                .getCleavageParameter().name();

        int idCount = optProtDataset.getActiveIdentificationNum();
        String msFileName = IoUtil.removeExtension(optProtDataset.getSubMsFile().getName());

        // Prepare score models and results map
        Map<String, RawScoreModel> resultsMap = Collections.synchronizedMap(new LinkedHashMap<>());
        RawScoreModel originalScore = new RawScoreModel("CleavageParameter");
        originalScore.setIdPSMNumber(idCount);

        // Cleavage types to consider (extensions/additions possible in the future)
        String[] cleavageParameters = new String[]{"wholeProtein", "unSpecific"};

        resultsMap.put(selectedOption, originalScore);

        int spectraCounter = idCount;
        scoresSet.clear();
        scoresSet.add(0.0);
        double targetedScore = 0;

        // Evaluate each cleavage option
        for (String cleavageParameter : cleavageParameters) {
            IdentificationParameters tempIdParam = IdentificationParameters.getIdentificationParameters(identificationParametersFile);
            tempIdParam.getSearchParameters().getDigestionParameters().setCleavageParameter(
                    DigestionParameters.CleavageParameter.valueOf(cleavageParameter)
            );

            // For enzyme-based cleavage, force add Trypsin (by convention)
            if (cleavageParameter.equalsIgnoreCase("enzyme")) {
                tempIdParam.getSearchParameters().getDigestionParameters()
                        .addEnzyme(EnzymeFactory.getInstance().getEnzyme("Trypsin"));
            } else {
                tempIdParam.getSearchParameters().getDigestionParameters().clearEnzymes();
            }

            final String option = cleavageParameter;
            final String updatedName = Configurations.DEFAULT_RESULT_NAME + "_" + option + "_" + msFileName;

            // Launch asynchronous scoring for this parameter setting
            Future<RawScoreModel> f = MainUtilities.getExecutorService().submit(() -> {
                return excuteSearch(
                        optProtDataset,
                        updatedName,
                        option,
                        tempIdParam,
                        true,
                        optimisedSearchParameter,
                        identificationParametersFile,
                        "" + option.replace("wholeProtein", "")
                );
            });

            try {
                RawScoreModel scoreModel = f.get();
                scoresSet.add(scoreModel.getcScore());

                // Only accept improved settings (with enough confident spectrum matches)
                if (scoreModel.getcScore() > 1) {
                    spectraCounter = Math.max(spectraCounter, scoreModel.getSpectrumMatchResult().size());
                    resultsMap.put(option, scoreModel);
                }
            } catch (ExecutionException | InterruptedException ex) {
                ex.printStackTrace();
            }
        }

        // Compute the statistical p-value for score changes (smaller p-value = more significant)
        double pvalue = ScoreComparisonUtilities.calculateConfidence(scoresSet);

        // Select the best performing cleavage option if an improvement is measured
        if (!resultsMap.isEmpty() && pvalue <= 0.05) {
            String bestScore = SpectraUtilities.compareScoresSet(
                    resultsMap,
                    optProtDataset.getSubsetSize(),
                    false,
                    optimisedSearchParameter.getSelectedSearchEngine()
                            .getName()
                            .equalsIgnoreCase(Advocate.sage.getName())
            );
            selectedOption = bestScore;
            targetedScore = resultsMap.get(bestScore).getcScore();

            // Warn user if the unspecific strategy (typically slow) is picked
            if ("unSpecific".equalsIgnoreCase(selectedOption)) {
                paramScore.setComments("Extremely slow processing");
            }
        }

        // Store final results with utility collectors for downstream reporting/persistence
        MainUtilities.addToParameterResults("Digestion", selectedOption, targetedScore, scoresSet);

        paramScore.setScore(pvalue);
        paramScore.setImpact(pvalue);
        paramScore.setParamValue(selectedOption);
        parameterScoreSet.add(paramScore);

        return selectedOption;
    }

    // ========================== ENZYME PARAMETER ==========================
    /**
     * Optimize digestion enzyme, specificity, and missed cleavage parameters.
     * <p>
     * This method tests various combinations of candidate enzymes, specificity
     * types, and maximum missed cleavages in order to maximize peptide spectrum
     * match (PSM) identification rates and/or scoring confidence. The process
     * consists of three major steps:
     * <ul>
     * <li>Test and select main enzyme.</li>
     * <li>Test and select cleavage specificity (e.g. specific, semi-specific,
     * unspecific, etc).</li>
     * <li>Test and select the optimal number for maximum allowed missed
     * cleavages.</li>
     * </ul>
     * All settings are compared and best-scoring configurations are chosen with
     * statistical confidence.
     *
     * @param optProtDataset The dataset to optimize on
     * @param identificationParametersFile File containing identification/search
     * parameters
     * @param optimisedSearchParameter Current optimization settings and flags
     * @param parameterScoreSet Set to store optimization result summaries
     * @return String array of length 3: [0] = enzyme name chosen, [1] =
     * specificity chosen, [2] = max missed cleavages (as string)
     * @throws IOException If reading parameter file fails
     */
    public String[] optimizeEnzymeParameter(
            SearchingSubDataset optProtDataset,
            File identificationParametersFile,
            SearchInputSetting optimisedSearchParameter,
            TreeSet<ParameterScoreModel> parameterScoreSet
    ) throws IOException {

        final ParameterScoreModel paramScore = new ParameterScoreModel();
        paramScore.setParamId("Enzyme");
        String[] values = new String[3];

        // Load initial/original parameter values
        IdentificationParameters originalTempIdParam = IdentificationParameters.getIdentificationParameters(identificationParametersFile);
        values[0] = originalTempIdParam.getSearchParameters().getDigestionParameters().getEnzymes().get(0).getName();
        values[1] = originalTempIdParam.getSearchParameters().getDigestionParameters().getSpecificity(values[0]).name();
        int missedClavageNumb = originalTempIdParam.getSearchParameters().getDigestionParameters().getnMissedCleavages(values[0]);
        values[2] = Integer.toString(missedClavageNumb);

        Map<String, RawScoreModel> resultsMapI = Collections.synchronizedMap(new LinkedHashMap<>());
        String msFileName = IoUtil.removeExtension(optProtDataset.getSubMsFile().getName());
        scoresSet.clear();
        scoresSet.add(0.0);

        // STEP 1: Optimize ENZYME parameter
        if (optimisedSearchParameter.isOptimizeEnzymeParameter()) {
            for (Enzyme enzyme : EnzymeFactory.getInstance().getEnzymes()) {
                // skip special-case Trypsin variant normally not used
                if (enzyme.getName().replace(" ", "").equalsIgnoreCase("Trypsin(noPrule)")) {
                    continue;
                }
                originalTempIdParam.getSearchParameters().getDigestionParameters().clearEnzymes();
                originalTempIdParam.getSearchParameters().getDigestionParameters().addEnzyme(enzyme);
                originalTempIdParam.getSearchParameters().getDigestionParameters().setnMissedCleavages(enzyme.getName(), missedClavageNumb);
                final String option = enzyme.getName();
                final String updatedName = Configurations.DEFAULT_RESULT_NAME + "_" + option + "_" + msFileName;
                Future<RawScoreModel> f = MainUtilities.getExecutorService().submit(() -> {
                    return excuteSearch(
                            optProtDataset, updatedName, option,
                            originalTempIdParam, true, optimisedSearchParameter, identificationParametersFile, "Enzyme: " + option
                    );
                });
                try {
                    RawScoreModel scoreModel = f.get();
                    scoresSet.add(scoreModel.getcScore());
                    if (scoreModel.isSensitiveChange()) {
                        resultsMapI.put(option, scoreModel);
                    }
                } catch (InterruptedException | ExecutionException ex) {
                    MainUtilities.QSProtWaitingHandler.addLogMassage(ex.getMessage());
                }
            }

            double targtedScore = 0;
            if (!resultsMapI.isEmpty()) {
                String enzymeName = SpectraUtilities.compareScoresSet(
                        resultsMapI,
                        optProtDataset.getSubsetSize(),
                        false,
                        optimisedSearchParameter.getSelectedSearchEngine().getName().equalsIgnoreCase(Advocate.sage.getName())
                );
                values[0] = enzymeName;
                optProtDataset.setActiveScoreModel(resultsMapI.get(enzymeName));
                targtedScore = resultsMapI.get(enzymeName).getcScore();
            }
            MainUtilities.addToParameterResults("Enzyme", values[0], targtedScore, scoresSet);

            // Update parameter for next round of specificity/missed cleavage optimization
            originalTempIdParam.getSearchParameters().getDigestionParameters().clearEnzymes();
            originalTempIdParam.getSearchParameters().getDigestionParameters().addEnzyme(EnzymeFactory.getInstance().getEnzyme(values[0]));
            originalTempIdParam.getSearchParameters().getDigestionParameters().setnMissedCleavages(values[0], missedClavageNumb);
            originalTempIdParam.getSearchParameters().getDigestionParameters().setSpecificity(values[0], DigestionParameters.Specificity.valueOf(values[1]));
        }

        // STEP 2: Optimize SPECIFICITY parameter
        resultsMapI.clear();
        scoresSet.clear();
        scoresSet.add(0.0);
        double targtedScore = 0;
        if (optimisedSearchParameter.isOptimizeSpecificityParameter()) {
            for (int i = 0; i < DigestionParameters.Specificity.values().length; i++) {
                final String option = DigestionParameters.Specificity.getSpecificity(i).name();
                if (option.equalsIgnoreCase(values[1])) {
                    continue;
                }
                originalTempIdParam.getSearchParameters().getDigestionParameters().setSpecificity(values[0], DigestionParameters.Specificity.getSpecificity(i));
                final String updatedName = Configurations.DEFAULT_RESULT_NAME + "_" + option + "_" + msFileName;
                Future<RawScoreModel> f = MainUtilities.getExecutorService().submit(() -> {
                    return excuteSearch(
                            optProtDataset, updatedName, option,
                            originalTempIdParam, true, optimisedSearchParameter, identificationParametersFile, "Enzyme specificity:" + option
                    );
                });
                try {
                    RawScoreModel scoreModel = f.get();
                    scoresSet.add(scoreModel.getcScore());
                    if (scoreModel.getcScore() > 0) {
                        resultsMapI.put(option, scoreModel);
                    }
                } catch (ExecutionException | InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
            if (!resultsMapI.isEmpty()) {
                String specificity = SpectraUtilities.compareScoresSet(
                        resultsMapI,
                        optProtDataset.getSubsetSize(),
                        false,
                        optimisedSearchParameter.getSelectedSearchEngine().getName().equalsIgnoreCase(Advocate.sage.getName())
                );
                values[1] = specificity;
                double impact = Math.round((double) (resultsMapI.get(specificity).getSpectrumMatchResult().size()
                        - optProtDataset.getActiveIdentificationNum()) * 100.0 / (double) optProtDataset.getActiveIdentificationNum());
                paramScore.setImpact(impact);
                optProtDataset.setActiveScoreModel(resultsMapI.get(specificity));
                targtedScore = resultsMapI.get(specificity).getcScore();
            }
            MainUtilities.addToParameterResults("Specificity", values[1], targtedScore, scoresSet);
            // Restore specificity to "specific" default for next round of tests
            originalTempIdParam.getSearchParameters().getDigestionParameters().setSpecificity(values[0], DigestionParameters.Specificity.valueOf("specific"));
        }

        // STEP 3: Optimize MAX MISSED CLEAVAGES parameter (simple search from 0 to 4)
        resultsMapI.clear();
        scoresSet.clear();
        scoresSet.add(0.0);
        targtedScore = 0;
        if (optimisedSearchParameter.isOptimizeMaxMissCleavagesParameter()) {
            for (int i = 0; i < 5; i++) {
                originalTempIdParam.getSearchParameters().getDigestionParameters().setnMissedCleavages(values[0], i);
                final String option = "missedCleavages_" + i;
                final String updatedName = Configurations.DEFAULT_RESULT_NAME + "_" + option + "_" + msFileName;
                int j = i;
                Future<RawScoreModel> f = MainUtilities.getExecutorService().submit(() -> {
                    return excuteSearch(
                            optProtDataset, updatedName, option,
                            originalTempIdParam, true, optimisedSearchParameter, identificationParametersFile, "Missed cleavages number: " + j
                    );
                });
                try {
                    RawScoreModel scoreModel = f.get();
                    scoresSet.add(scoreModel.getcScore());
                    if (scoreModel.getcScore() > 0) {
                        if (i < missedClavageNumb && scoreModel.getSharedDataSize() == optProtDataset.getCurrentScoreModel().getIdPSMNumber()) {
                            resultsMapI.put(Integer.toString(i), scoreModel);
                        } else if (i > missedClavageNumb && scoreModel.getSharedDataSize() == optProtDataset.getCurrentScoreModel().getIdPSMNumber()
                                && scoreModel.getIdPSMNumber() >= 1.05 * optProtDataset.getActiveIdentificationNum()) {
                            resultsMapI.put(Integer.toString(i), scoreModel);
                        } else if (i > missedClavageNumb) {
                            break;
                        }
                    } else if (i > missedClavageNumb) {
                        // intentionally empty: early stopping condition
                    }
                } catch (ExecutionException | InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
            String numbOfMissedCleavage = Integer.toString(missedClavageNumb);
            if (!resultsMapI.isEmpty()) {
                numbOfMissedCleavage = SpectraUtilities.compareScoresSet(
                        resultsMapI,
                        optProtDataset.getSubsetSize(),
                        false,
                        optimisedSearchParameter.getSelectedSearchEngine().getName().equalsIgnoreCase(Advocate.sage.getName())
                );
                double impact = Math.round((double) (resultsMapI.get(numbOfMissedCleavage).getSpectrumMatchResult().size()
                        - optProtDataset.getActiveIdentificationNum()) * 100.0 / (double) optProtDataset.getActiveIdentificationNum());
                paramScore.setImpact(impact);
                optProtDataset.setActiveScoreModel(resultsMapI.get(numbOfMissedCleavage));
                targtedScore = resultsMapI.get(numbOfMissedCleavage).getcScore();
            }
            values[2] = numbOfMissedCleavage;
        }

        // Finalize statistics and record for reports
        paramScore.setScore(optProtDataset.getActiveIdentificationNum());
        MainUtilities.addToParameterResults("MaxMissedCleavages", values[2], targtedScore, scoresSet);
        paramScore.setParamValue(Arrays.asList(values).toString());
        parameterScoreSet.add(paramScore);

        return values;
    }

    // ========================== Fragment Ion Type  PARAMETER ==========================
    /**
     * Optimize the fragment ion types parameter.
     * <p>
     * This method tests various combinations of forward and rewind fragment ion
     * types (for example: "b", "a", "c" and "y", "x", "z" ions) to discover the
     * best set for confident peptide spectrum match out of all combinations,
     * given the currently configured protein and dataset.
     *
     * The process:
     * <ul>
     * <li>Iterate over possible fragment ion type combinations.</li>
     * <li>Update search parameters and run a proteomics database search (in
     * parallel when possible).</li>
     * <li>Use the results to select the best-performing fragment ion set.</li>
     * <li>Update and report parameter scoring statistics for downstream
     * review.</li>
     * </ul>
     *
     * @param optProtDataset The dataset to optimize fragment ion types for
     * @param identificationParametersFile File containing current search
     * parameters
     * @param optimisedSearchParameter Current optimization settings and flags
     * @param parameterScoreSet Set to collect scoring statistics for this and
     * other parameters
     * @return The selected combination (string format, e.g. "[b]-[y]") chosen
     * @throws IOException If file reading or parameter I/O fails
     */
    public String optimizeFragmentIonTypesParameter(
            SearchingSubDataset optProtDataset,
            File identificationParametersFile,
            SearchInputSetting optimisedSearchParameter,
            TreeSet<ParameterScoreModel> parameterScoreSet
    ) throws IOException {
        final ParameterScoreModel paramScore = new ParameterScoreModel();
        paramScore.setParamId("FragmentIons");

        IdentificationParameters originalTempIdParam = IdentificationParameters.getIdentificationParameters(identificationParametersFile);

        Map<String, RawScoreModel> resultsMap = Collections.synchronizedMap(new LinkedHashMap<>());
        String msFileName = IoUtil.removeExtension(optProtDataset.getSubMsFile().getName());
        String selectedOption = originalTempIdParam.getSearchParameters().getForwardIons() + "-"
                + originalTempIdParam.getSearchParameters().getRewindIons();

        IdentificationParameters tempIdParam = IdentificationParameters.getIdentificationParameters(identificationParametersFile);
        SearchParameters tempSearchParameters = tempIdParam.getSearchParameters();
        ArrayList<Integer> selectedForwardIons = tempSearchParameters.getForwardIons();

        // Define fragment ion types: adjust as more types become significant in the field
        String[] forwardIons = new String[]{"b", "a", "c"};
        String[] rewindIons = new String[]{"y", "x", "z"};

        scoresSet.clear();
        scoresSet.add(0.0);
        double targetedScore = 0;

        // Evaluate all forward-rewind ion type combinations
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

                // Skip already chosen default, so we avoid redundant searches
                if (option.equalsIgnoreCase(selectedOption)) {
                    continue;
                }
                final String updatedName = Configurations.DEFAULT_RESULT_NAME + "_" + option + "_" + msFileName;

                // Run this configuration and gather its result for later comparison
                Future<RawScoreModel> f = MainUtilities.getExecutorService().submit(() -> {
                    return excuteSearch(
                            optProtDataset, updatedName, option, tempIdParam, false,
                            optimisedSearchParameter, identificationParametersFile,
                            "Fragmentation ions: " + forwardIon + " (forward), " + rewindIon + " (rewind)"
                    );
                });

                try {
                    RawScoreModel scoreModel = f.get();
                    scoresSet.add(scoreModel.getcScore());
                    if (scoreModel.getcScore() > 0) {
                        resultsMap.put(option, scoreModel);
                    }
                } catch (ExecutionException | InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }

        // If an improvement is found/statistically significant, select it
        if (!resultsMap.isEmpty()) {
            String bestScore = SpectraUtilities.compareScoresSet(
                    resultsMap, optProtDataset.getSubsetSize(), false,
                    optimisedSearchParameter.getSelectedSearchEngine().getName()
                            .equalsIgnoreCase(Advocate.sage.getName()));
            selectedOption = bestScore;
            double impact = Math.round(
                    (double) (resultsMap.get(selectedOption).getSpectrumMatchResult().size()
                    - optProtDataset.getActiveIdentificationNum()) * 100.0
                    / (double) optProtDataset.getActiveIdentificationNum()
            );
            paramScore.setImpact(impact);
            optProtDataset.setActiveScoreModel(resultsMap.get(bestScore));
            targetedScore = resultsMap.get(bestScore).getcScore();
        }

        // Clean up output representation for user, e.g. "[b]-[y]" → "b-y"
        selectedOption = selectedOption.replace("[", "").replace("]", "");

        // Record the results of this parameter's optimization
        paramScore.setScore(optProtDataset.getActiveIdentificationNum());
        paramScore.setParamValue(selectedOption);
        parameterScoreSet.add(paramScore);
        MainUtilities.addToParameterResults("FragmentIonTypes", selectedOption, targetedScore, scoresSet);

        return selectedOption;
    }

    // ========================== Maximum Miss Cleavages  PARAMETER ==========================
    /**
     * Optimize the maximum number of missed cleavages parameter for
     * enzyme-based digestion.
     * <p>
     * This method systematically tests a series of possible values for the
     * number of allowed missed cleavages in protein digestion by an enzyme. For
     * each candidate value (typically 0–4), it:
     * <ul>
     * <li>Updates the parameter in the identification parameters.</li>
     * <li>Runs a search with the new setting.</li>
     * <li>Collects and compares confidence scores (using cScore and acceptance
     * logic.)</li>
     * </ul>
     * The best candidate, judged by score comparison and acceptance checks, is
     * selected. If the digestion is not enzyme-based, returns -1 (no
     * optimization performed).
     *
     * @param optProtDataset Dataset to optimize on
     * @param identificationParametersFile Current parameter file
     * @param optimisedSearchParameter Optimization control flags/settings
     * @param parameterScoreSet Set to accumulate scoring and selection results
     * @return Integer for the chosen "missed cleavages" value, or -1 if
     * optimization is inapplicable
     * @throws IOException On read error from parameter file
     */
    public Integer optimizeMaxMissCleavagesParameter(
            SearchingSubDataset optProtDataset,
            File identificationParametersFile,
            SearchInputSetting optimisedSearchParameter,
            TreeSet<ParameterScoreModel> parameterScoreSet
    ) throws IOException {
        final ParameterScoreModel paramScore = new ParameterScoreModel();
        paramScore.setParamId("missedCleavages");

        IdentificationParameters originalTempIdParam = IdentificationParameters.getIdentificationParameters(identificationParametersFile);

        // Check if digestion is enzyme-based, otherwise this parameter isn't relevant!
        if (!originalTempIdParam.getSearchParameters().getDigestionParameters().getCleavageParameter().name().equalsIgnoreCase("enzyme")) {
            return -1;
        }

        // Get current enzyme and missed cleavage value
        String enzymeName = originalTempIdParam.getSearchParameters().getDigestionParameters().getEnzymes().get(0).getName();
        Integer selectedOption = originalTempIdParam.getSearchParameters().getDigestionParameters().getnMissedCleavages(enzymeName);

        Map<String, RawScoreModel> resultsMap = Collections.synchronizedMap(new LinkedHashMap<>());
        String msFileName = IoUtil.removeExtension(optProtDataset.getSubMsFile().getName());
        scoresSet.clear();
        scoresSet.add(0.0);
        double targetedScore = 0;

        // Iterate over possible parameter options (missed cleavages 0, 1, 2, 3, 4)
        for (int i = 0; i < 5; i++) {
            if (i == selectedOption) {
                continue;
            }

            IdentificationParameters tempIdParam = IdentificationParameters.getIdentificationParameters(identificationParametersFile);
            tempIdParam.getSearchParameters().getDigestionParameters().setnMissedCleavages(enzymeName, i);

            final String option = "missedCleavages_" + i;
            final String updatedName = Configurations.DEFAULT_RESULT_NAME + "_" + option + "_" + msFileName;
            int j = i;
            Future<RawScoreModel> f = MainUtilities.getExecutorService().submit(()
                    -> excuteSearch(
                            optProtDataset, updatedName, option, tempIdParam, false,
                            optimisedSearchParameter, identificationParametersFile, " Missed cleavages: " + j
                    )
            );
            try {
                RawScoreModel scoreModel = f.get();
                scoresSet.add(scoreModel.getcScore());
                if (scoreModel.isAcceptedChange()) {
                    resultsMap.put(Integer.toString(i), scoreModel);
                } else if (i > selectedOption && !scoreModel.isSensitiveChange()) {
                    // Early stop: if no sensitivity for higher values, don't bother trying more
                    break;
                }
            } catch (ExecutionException | InterruptedException ex) {
                ex.printStackTrace();
            }
        }

        // Select the best-scoring missed cleavage value, if available
        if (!resultsMap.isEmpty()) {
            String bestScore = SpectraUtilities.compareScoresSet(
                    resultsMap, optProtDataset.getSubsetSize(), false,
                    optimisedSearchParameter.getSelectedSearchEngine().getName().equalsIgnoreCase(Advocate.sage.getName()));
            selectedOption = Integer.valueOf(bestScore);
            double impact = Math.round(
                    (double) (resultsMap.get(selectedOption.toString()).getSpectrumMatchResult().size()
                    - optProtDataset.getActiveIdentificationNum())
                    * 100.0 / (double) optProtDataset.getActiveIdentificationNum()
            );
            paramScore.setImpact(impact);
            optProtDataset.setActiveScoreModel(resultsMap.get(bestScore));
            targetedScore = resultsMap.get(bestScore).getcScore();
        }
        paramScore.setScore(optProtDataset.getActiveIdentificationNum());
        paramScore.setParamValue(selectedOption.toString());
        parameterScoreSet.add(paramScore);
        MainUtilities.addToParameterResults("MaxMissedCleavages", selectedOption.toString(), targetedScore, scoresSet);

        return selectedOption;
    }

    // ========================== Fragment Tolerance PARAMETER ==========================
    /**
     * Optimize the fragment ion mass tolerance (accuracy) parameter.
     * <p>
     * This method systematically evaluates different common values for fragment
     * ion mass tolerance, such as 0.01, 0.02, 0.05, 0.1, 0.2, and 0.5 Da. For
     * each candidate, it:
     * <ul>
     * <li>Adjusts the fragment ion accuracy in the identification
     * parameters.</li>
     * <li>Executes a search using the modified setting to assess identification
     * confidence (cScore and acceptance logic).</li>
     * <li>Records and statistically compares the impact of each candidate
     * value.</li>
     * </ul>
     * The best candidate is selected using statistical tests and acceptance
     * checks.
     *
     * @param optProtDataset Dataset to optimize for fragment ion tolerance
     * @param identificationParametersFile File containing the base search
     * parameters
     * @param optimisedSearchParameter Current optimization flag and settings
     * @param parameterScoreSet Collector for parameter scoring/statistics
     * objects
     * @return The selected fragment ion tolerance value (in Da)
     * @throws IOException for parameter file issues
     */
    public double optimizeFragmentToleranceParameter(
            SearchingSubDataset optProtDataset,
            File identificationParametersFile,
            SearchInputSetting optimisedSearchParameter,
            TreeSet<ParameterScoreModel> parameterScoreSet
    ) throws IOException {
        final ParameterScoreModel paramScore = new ParameterScoreModel();
        paramScore.setParamId("fragmentAccuracy");

        IdentificationParameters originalTempIdParam = IdentificationParameters.getIdentificationParameters(identificationParametersFile);
        String msFileName = IoUtil.removeExtension(optProtDataset.getSubMsFile().getName());
        double selectedOption = originalTempIdParam.getSearchParameters().getFragmentIonAccuracy();

        Map<String, RawScoreModel> resultsMap = Collections.synchronizedMap(new LinkedHashMap<>());
        // Typical fragment ion tolerance candidates (in Dalton)
        double[] values = new double[]{0.01, 0.02, 0.05, 0.1, 0.2, 0.5};

        scoresSet.clear();
        scoresSet.add(0.0);
        double targetedScore = 0;

        // Evaluate each tolerance value
        for (double i : values) {
            if (selectedOption == i) {
                continue;
            }

            IdentificationParameters tempIdParam = IdentificationParameters.getIdentificationParameters(identificationParametersFile);
            tempIdParam.getSearchParameters().setFragmentIonAccuracy(i);
            tempIdParam.getSearchParameters().setFragmentAccuracyType(SearchParameters.MassAccuracyType.DA);
            final String option = "fragmentAccuracy_" + i;
            final String updatedName = Configurations.DEFAULT_RESULT_NAME + "_" + option + "_" + msFileName;
            double j = i;
            Future<RawScoreModel> f = MainUtilities.getExecutorService().submit(()
                    -> excuteSearch(
                            optProtDataset, updatedName, option, tempIdParam, true,
                            optimisedSearchParameter, identificationParametersFile, "Fragment accuracy: " + j
                    )
            );
            try {
                RawScoreModel scoreModel = f.get();
                scoresSet.add(scoreModel.getcScore());
                if (scoreModel.isAcceptedChange()) {
                    resultsMap.put(Double.toString(i), scoreModel);
                } else if (i > selectedOption) {
                    // If increasing tolerance does not yield improvement, stop early
                    break;
                }
            } catch (ExecutionException | InterruptedException ex) {
                ex.printStackTrace();
            }
        }

        // Select the best-scoring fragment tolerance value, if available
        if (!resultsMap.isEmpty()) {
            selectedOption = Double.parseDouble(SpectraUtilities.compareScoresSet(
                    resultsMap, optProtDataset.getSubsetSize(), true,
                    optimisedSearchParameter.getSelectedSearchEngine().getName().equalsIgnoreCase(Advocate.sage.getName())
            ));
            double impact = Math.round(
                    (double) (resultsMap.get(Double.toString(selectedOption)).getSpectrumMatchResult().size()
                    - optProtDataset.getActiveIdentificationNum())
                    * 100.0 / (double) optProtDataset.getActiveIdentificationNum()
            );
            paramScore.setImpact(impact);
            optProtDataset.setActiveScoreModel(resultsMap.get(Double.toString(selectedOption)));
            targetedScore = resultsMap.get(Double.toString(selectedOption)).getcScore();
        }
        paramScore.setScore(optProtDataset.getActiveIdentificationNum());
        paramScore.setParamValue(Double.toString(selectedOption));
        parameterScoreSet.add(paramScore);
        MainUtilities.addToParameterResults("FragmentAccuracy", Double.toString(selectedOption), targetedScore, scoresSet);

        return selectedOption;
    }
    // ========================== Precursor Charge PARAMETER ==========================

    /**
     * Optimize the precursor ion charge parameter range.
     * <p>
     * This method systematically tests different precursor charge ranges
     * (min/max) to maximize high-quality spectrum identification. For each
     * candidate range (e.g., min charge 1–4, max charge 2–5):
     * <ul>
     * <li>Modifies the search parameter for min and max precursor charge.</li>
     * <li>Runs a database search under the new charge setting.</li>
     * <li>Compares identification quality/quantity and scores of PSMs.</li>
     * </ul>
     * The best scoring charge range is selected, and the results are reported.
     *
     * @param optProtDataset Dataset to optimize
     * @param identificationParametersFile Parameter file with the base search
     * configuration
     * @param optimisedSearchParameter Optimization settings/flags to control
     * search
     * @param parameterScoreSet Collected results of all parameter optimizations
     * @return int array [minCharge, maxCharge] representing the selected range
     * @throws IOException if reading parameter file fails
     */
    public int[] optimizePrecursorChargeParameter(
            SearchingSubDataset optProtDataset,
            File identificationParametersFile,
            SearchInputSetting optimisedSearchParameter,
            TreeSet<ParameterScoreModel> parameterScoreSet
    ) throws IOException {
        final ParameterScoreModel paramScore = new ParameterScoreModel();
        paramScore.setParamId("charge");

        IdentificationParameters originalTempIdParam = IdentificationParameters.getIdentificationParameters(identificationParametersFile);
        String msFileName = IoUtil.removeExtension(optProtDataset.getSubMsFile().getName());
        int selectedMaxChargeOption = originalTempIdParam.getSearchParameters().getMaxChargeSearched();
        int selectedMinChargeOption = originalTempIdParam.getSearchParameters().getMinChargeSearched();

        Map<String, RawScoreModel> resultsMap = Collections.synchronizedMap(new LinkedHashMap<>());
        IdentificationParameters tempIdParam = IdentificationParameters.getIdentificationParameters(identificationParametersFile);
        int spectraCounter = optProtDataset.getActiveIdentificationNum();
        scoresSet.clear();
        scoresSet.add(0.0);
        double targetedScore = 0;

        // Evaluate all charge window combinations with min 1–4 and max 2–5 (min < max)
        for (int i = 1; i < 5; i++) {
            for (int j = 2; j <= 5; j++) {
                if (j <= i) {
                    continue;
                }

                tempIdParam.getSearchParameters().setMinChargeSearched(i);
                tempIdParam.getSearchParameters().setMaxChargeSearched(j);

                final String option = "charge-" + i + "," + j;
                final String updatedName = Configurations.DEFAULT_RESULT_NAME + "_" + option + "_" + msFileName;

                Future<RawScoreModel> f = MainUtilities.getExecutorService().submit(()
                        -> excuteSearch(
                                optProtDataset,
                                updatedName,
                                option,
                                tempIdParam,
                                false,
                                optimisedSearchParameter,
                                identificationParametersFile,
                                "Charge: " + option.replace("charge-", "")
                        )
                );
                try {
                    RawScoreModel scoreModel = f.get();

                    // Track improvements only if the spectrum count isn't decreased
                    scoresSet.add(scoreModel.getcScore());
                    if (scoreModel.getcScore() > 0) {
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
            String bestScore = SpectraUtilities.compareScoresSet(
                    resultsMap,
                    optProtDataset.getSubsetSize(),
                    false,
                    optimisedSearchParameter.getSelectedSearchEngine().getName().equalsIgnoreCase(Advocate.sage.getName())
            );
            double impact = Math.round(
                    (double) (resultsMap.get(bestScore).getSpectrumMatchResult().size()
                    - optProtDataset.getActiveIdentificationNum())
                    * 100.0 / (double) optProtDataset.getActiveIdentificationNum()
            );
            paramScore.setImpact(impact);
            optProtDataset.setActiveScoreModel(resultsMap.get(bestScore));
            String[] topOption = bestScore.split("-")[1].split(",");
            selectedMinChargeOption = Integer.parseInt(topOption[0]);
            selectedMaxChargeOption = Integer.parseInt(topOption[1]);
            targetedScore = optProtDataset.getCurrentScoreModel().getcScore();
        }

        paramScore.setScore(optProtDataset.getActiveIdentificationNum());
        paramScore.setParamValue(selectedMinChargeOption + "," + selectedMaxChargeOption);
        parameterScoreSet.add(paramScore);
        MainUtilities.addToParameterResults("PrecursorCharge", selectedMinChargeOption + " to " + selectedMaxChargeOption, targetedScore, scoresSet);
        return new int[]{selectedMinChargeOption, selectedMaxChargeOption};
    }
    // ========================== Isotopic Correction PARAMETER ==========================

    /**
     * Optimize the isotopic correction parameter range.
     * <p>
     * This method systematically tests different min/max values for isotopic
     * correction (e.g., -2 to +2) to maximize identification confidence.
     * Isotopic correction is crucial in high-precision MS for peptide mass
     * determination.
     * <ul>
     * <li>Modifies search parameter min/max for isotopic correction.</li>
     * <li>Runs a proteome database search for each configuration.</li>
     * <li>Compares the results using identification counts and scoring
     * statistics.</li>
     * </ul>
     *
     * @param optProtDataset Dataset to optimize
     * @param identificationParametersFile Parameter file with the base search
     * configuration
     * @param optimisedSearchParameter Optimization settings to control search
     * @param parameterScoreSet Collector for scoring/statistics for parameter
     * sweep
     * @return int array [minIsotopeCorrection, maxIsotopeCorrection]
     * representing the selected range
     * @throws IOException if reading parameter file fails
     */
    public int[] optimizeIsotopParameter(
            SearchingSubDataset optProtDataset,
            File identificationParametersFile,
            SearchInputSetting optimisedSearchParameter,
            TreeSet<ParameterScoreModel> parameterScoreSet
    ) throws IOException {

        final ParameterScoreModel paramScore = new ParameterScoreModel();
        paramScore.setParamId("isotop_");

        IdentificationParameters originalTempIdParam = IdentificationParameters.getIdentificationParameters(identificationParametersFile);
        String msFileName = IoUtil.removeExtension(optProtDataset.getSubMsFile().getName());
        int selectedMaxIsotopicCorrectionOption = originalTempIdParam.getSearchParameters().getMaxIsotopicCorrection();
        int selectedMinIsotopicCorrectionOption = originalTempIdParam.getSearchParameters().getMinIsotopicCorrection();

        Map<String, RawScoreModel> resultsMap = Collections.synchronizedMap(new LinkedHashMap<>());
        IdentificationParameters tempIdParam = IdentificationParameters.getIdentificationParameters(identificationParametersFile);
        scoresSet.clear();
        scoresSet.add(0.0);
        double targetedScore = 0;

        for (int i = -2; i < 2; i++) {
            for (int j = -1; j <= 2; j++) {
                if (j <= i) {
                    continue;
                }

                tempIdParam.getSearchParameters().setMinIsotopicCorrection(i);
                tempIdParam.getSearchParameters().setMaxIsotopicCorrection(j);

                final String option = "isotop_" + i + "," + j;
                final String updatedName = Configurations.DEFAULT_RESULT_NAME + "_" + option + "_" + msFileName;

                Future<RawScoreModel> f = MainUtilities.getExecutorService().submit(()
                        -> excuteSearch(
                                optProtDataset,
                                updatedName,
                                option,
                                tempIdParam,
                                false,
                                optimisedSearchParameter,
                                identificationParametersFile,
                                "Isotop: " + option.replace("isotop_", "")
                        )
                );
                try {
                    RawScoreModel scoreModel = f.get();
                    scoresSet.add(scoreModel.getcScore());
                    if (scoreModel.isAcceptedChange()) {
                        resultsMap.put(option, scoreModel);
                    }
                } catch (ExecutionException | InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }

        if (!resultsMap.isEmpty()) {
            String bestScore = SpectraUtilities.compareScoresSet(
                    resultsMap,
                    optProtDataset.getSubsetSize(),
                    false,
                    optimisedSearchParameter.getSelectedSearchEngine().getName().equalsIgnoreCase(Advocate.sage.getName())
            );
            double impact = Math.round(
                    (double) (resultsMap.get(bestScore).getSpectrumMatchResult().size()
                    - optProtDataset.getActiveIdentificationNum())
                    * 100.0 / (double) optProtDataset.getActiveIdentificationNum()
            );
            paramScore.setImpact(impact);
            optProtDataset.setActiveScoreModel(resultsMap.get(bestScore));
            String[] topOption = bestScore.split("_")[1].split(",");
            selectedMinIsotopicCorrectionOption = Integer.parseInt(topOption[0]);
            selectedMaxIsotopicCorrectionOption = Integer.parseInt(topOption[1]);
            targetedScore = optProtDataset.getCurrentScoreModel().getcScore();
        }

        paramScore.setScore(optProtDataset.getActiveIdentificationNum());
        paramScore.setParamValue(selectedMinIsotopicCorrectionOption + "," + selectedMaxIsotopicCorrectionOption);
        parameterScoreSet.add(paramScore);
        MainUtilities.addToParameterResults("Isotops", selectedMinIsotopicCorrectionOption + " to " + selectedMaxIsotopicCorrectionOption, targetedScore, scoresSet);
        return new int[]{selectedMinIsotopicCorrectionOption, selectedMaxIsotopicCorrectionOption};
    }
    // ========================== Precursor Tolerance PARAMETER ==========================

    /**
     * Optimize the precursor mass tolerance (accuracy) parameter for MS
     * analysis.
     * <p>
     * This method evaluates multiple possible mass tolerance settings for
     * precursor ions (in PPM or Da) to maximize identification confidence for
     * the dataset. For each candidate:
     * <ul>
     * <li>Sets the precursor accuracy in the search parameters (in PPM or Da
     * depending on instrument).</li>
     * <li>Runs a search using the new setting.</li>
     * <li>Compares and scores the results.</li>
     * </ul>
     * The best candidate, by acceptance and statistical improvements, is
     * selected.
     *
     * @param optProtDataset Dataset to optimize
     * @param identificationParametersFile File containing the base search
     * configuration
     * @param optimisedSearchParameter Current optimization settings/flags
     * @param parameterScoreSet Collector for parameter scoring/statistics
     * @return The selected precursor mass tolerance value (as double; units:
     * PPM or Da)
     * @throws IOException if reading parameter file fails
     */
    public double optimizePrecursorToleranceParameter(
            SearchingSubDataset optProtDataset,
            File identificationParametersFile,
            SearchInputSetting optimisedSearchParameter,
            TreeSet<ParameterScoreModel> parameterScoreSet
    ) throws IOException {
        final ParameterScoreModel paramScore = new ParameterScoreModel();
        paramScore.setParamId("PrecursorAccuracy");

        IdentificationParameters originalTempIdParam = IdentificationParameters.getIdentificationParameters(identificationParametersFile);
        String msFileName = IoUtil.removeExtension(optProtDataset.getSubMsFile().getName());
        double selectedOption = originalTempIdParam.getSearchParameters().getPrecursorAccuracy();

        Map<String, RawScoreModel> resultsMap = Collections.synchronizedMap(new LinkedHashMap<>());
        // Typical values in PPM: 5, 10, 15, 20, 25. If low res, tries Da mode as well.
        double[] iValues = new double[]{5, 10, 15, 20, 25};
        boolean toEnd = false;
        int counter = 4;
        scoresSet.clear();
        scoresSet.add(0.0);
        double targetedScore = 0;

        // Try all PPM values
        for (double i : iValues) {
            if (i == selectedOption) {
                continue;
            }
            IdentificationParameters tempIdParam = IdentificationParameters.getIdentificationParameters(identificationParametersFile);
            tempIdParam.getSearchParameters().setPrecursorAccuracy(i);
            tempIdParam.getSearchParameters().setPrecursorAccuracyType(SearchParameters.MassAccuracyType.PPM);
            final String option = "precursorAccuracy_" + i;
            final String updatedName = Configurations.DEFAULT_RESULT_NAME + "_" + option + "_" + msFileName;

            Future<RawScoreModel> f = MainUtilities.getExecutorService().submit(()
                    -> excuteSearch(
                            optProtDataset, updatedName, option, tempIdParam, false,
                            optimisedSearchParameter, identificationParametersFile, "Precursor accuracy (ppm): " + i
                    )
            );
            try {
                RawScoreModel scoreModel = f.get();
                scoresSet.add(scoreModel.getcScore());
                if (scoreModel.isAcceptedChange() && scoreModel.getcScore() > 0) {
                    counter++;
                    resultsMap.put(Double.toString(i), scoreModel);
                } else if (i > selectedOption) {
                    toEnd = true;
                    break;
                }
            } catch (ExecutionException | InterruptedException ex) {
                ex.printStackTrace();
            }
        }

        // If instrument is not HRMS, test Da values too (after PPM loop)
        if (!toEnd) {
            scoresSet.clear();
            if (!optProtDataset.isHighResolutionMassSpectrometers()) {
                for (double i : iValues) {
                    IdentificationParameters tempIdParam = IdentificationParameters.getIdentificationParameters(identificationParametersFile);
                    tempIdParam.getSearchParameters().setPrecursorAccuracy(i);
                    tempIdParam.getSearchParameters().setPrecursorAccuracyType(SearchParameters.MassAccuracyType.DA);
                    final String option = "precursorAccuracy_Da" + i;
                    final String updatedName = Configurations.DEFAULT_RESULT_NAME + "_" + option + "_" + msFileName;
                    Future<RawScoreModel> f = MainUtilities.getExecutorService().submit(()
                            -> excuteSearch(
                                    optProtDataset, updatedName, option, tempIdParam, false,
                                    optimisedSearchParameter, identificationParametersFile, "Precursor accuracy (Da): " + i
                            )
                    );
                    try {
                        RawScoreModel scoreModel = f.get();
                        scoresSet.add(scoreModel.getcScore());
                        if (scoreModel.getcScore() > 0) {
                            counter++;
                            resultsMap.put(Double.toString(i), scoreModel);
                        }
                    } catch (ExecutionException | InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }

        // Assess and select the best precursor accuracy
        if (!resultsMap.isEmpty()) {
            String bestScore = SpectraUtilities.compareScoresSet(
                    resultsMap,
                    optProtDataset.getSubsetSize(),
                    true,
                    optimisedSearchParameter.getSelectedSearchEngine().getName().equalsIgnoreCase(Advocate.sage.getName())
            );
            double impact = Math.round(
                    (double) (resultsMap.get(bestScore).getSpectrumMatchResult().size()
                    - optProtDataset.getActiveIdentificationNum())
                    * 100.0 / (double) optProtDataset.getActiveIdentificationNum()
            );
            paramScore.setImpact(impact);
            optProtDataset.setActiveScoreModel(resultsMap.get(bestScore));
            selectedOption = Double.parseDouble(bestScore);
            targetedScore = optProtDataset.getCurrentScoreModel().getcScore();
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
        MainUtilities.addToParameterResults("PrecursorAccuracy", paramScore.getParamValue(), targetedScore, scoresSet);
        return selectedOption;
    }
    // ========================== Modifications/PTM PARAMETER ==========================

    /**
     * Optimize Post-Translational Modifications (PTMs): fixed, variable, and
     * refinement modifications.
     * <p>
     * This method determines the best combination of protein modifications
     * (PTMs) to use as fixed or variable (and refinement), maximizing
     * identification rates and statistical confidence. It:
     * <ul>
     * <li>Starts with common modifications (e.g., Carbamidomethylation of C,
     * Oxidation of M).</li>
     * <li>Sequentially tests additional modifications as fixed or variable,
     * updating parameters and running searches.</li>
     * <li>For each round, records scoring/statistics and prunes the set of
     * candidates.</li>
     * <li>Also considers terminal modifications at the end.</li>
     * <li>Adds statistics and justification in the returned parameter results
     * set.</li>
     * </ul>
     *
     * @param optProtDataset The dataset for which modifications are optimized.
     * @param identificationParametersFile File with base identification/search
     * parameters.
     * @param optimisedSearchParameter Structure holding optimization flags and
     * user preferences.
     * @param parameterScoreSet Collector set for all scoring/statistics.
     * @return Map with keys "fixedModifications", "variableModifications",
     * "refinmentFixedModifications", each mapped to a set of chosen
     * modification names.
     * @throws IOException If an I/O error occurs reading parameter files or
     * running searches.
     */
    public Map<String, Set<String>> optimizeModificationsParameter(
            SearchingSubDataset optProtDataset,
            File identificationParametersFile,
            SearchInputSetting optimisedSearchParameter,
            TreeSet<ParameterScoreModel> parameterScoreSet
    ) throws IOException {

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
        // Load all available modifications in the relevant categories
        mods.addAll(ptmFactory.getModifications(ModificationCategory.Common));
        mods.addAll(ptmFactory.getModifications(ModificationCategory.Common_Biological));
        mods.addAll(ptmFactory.getModifications(ModificationCategory.Common_Artifact));
        IdentificationParameters tempIdParam = IdentificationParameters.getIdentificationParameters(identificationParametersFile);

        // Initial: remove all mods, so only what we choose are active
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

        // Stage 1: fixed common modification first
        String prefix = "f_";
        resultsMap.putAll(this.checkModificationsScores(
                selectedFixedModificationOption, selectedVariableModificationOption, potintialMods, true,
                msFileName, tempIdParam, optProtDataset, identificationParametersFile, optimisedSearchParameter, prefix, true
        ));
        scoresSet.clear();
        for (RawScoreModel scoreModel : resultsMap.values()) {
            scoresSet.add(scoreModel.getcScore());
        }
        if (!resultsMap.isEmpty()) {
            String bestMod = SpectraUtilities.compareScoresSet(
                    resultsMap, optProtDataset.getSubsetSize(), false,
                    optimisedSearchParameter.getSelectedSearchEngine().getName().equalsIgnoreCase(Advocate.sage.getName())
            );
            if (resultsMap.get(bestMod).isSensitiveChange() || resultsMap.get(bestMod).getRawFinalScore() > 0 || (resultsMap.get(bestMod).getcScore() < 0.0 && (resultsMap.get(bestMod).getcScore() * -1 > 0))) {
                selectedFixedModificationOption.add(bestMod);
                optProtDataset.setActiveScoreModel(resultsMap.get(bestMod));
                potintialMods.clear();
                targtedFixedModificationScore.put("C", resultsMap.get(bestMod));
                MainUtilities.cleanFolder(Configurations.WORKING_FOLDER_PATH);
                resultsMap.clear();
                MainUtilities.addToParameterResults(bestMod, bestMod, optProtDataset.getCurrentScoreModel().getcScore(), scoresSet);
            }
        }
        // Stage 2: variable common mod first
        final ParameterScoreModel variableModParamScore = new ParameterScoreModel();
        variableModParamScore.setParamId("VariableModifications");
        potintialMods.add(commonVariableMod);
        prefix = "v_";
        resultsMap.putAll(this.checkModificationsScores(
                selectedFixedModificationOption, selectedVariableModificationOption, potintialMods, false,
                msFileName, tempIdParam, optProtDataset, identificationParametersFile, optimisedSearchParameter, prefix, true
        ));
        scoresSet.clear();
        for (RawScoreModel scoreModel : resultsMap.values()) {
            scoresSet.add(scoreModel.getcScore());
        }
        if (!resultsMap.isEmpty()) {
            String bestMod = SpectraUtilities.compareScoresSet(
                    resultsMap, optProtDataset.getSubsetSize(), false,
                    optimisedSearchParameter.getSelectedSearchEngine().getName().equalsIgnoreCase(Advocate.sage.getName())
            );
            selectedVariableModificationOption.add(bestMod);
            optProtDataset.setActiveScoreModel(resultsMap.get(bestMod));
            potintialMods.clear();
            MainUtilities.cleanFolder(Configurations.WORKING_FOLDER_PATH);
            resultsMap.clear();
            MainUtilities.addToParameterResults(bestMod, bestMod, optProtDataset.getCurrentScoreModel().getcScore(), scoresSet);
        }
        MainUtilities.cleanFolder(Configurations.WORKING_FOLDER_PATH);

        // Remove already selected fixed/variable mods from pool for next round
        mods.removeAll(selectedFixedModificationOption);
        mods.removeAll(selectedVariableModificationOption);

        // Continue with other checks for fixed and variable mods, and terminal mods...
        // (due to size, details elided - see the code for full workflow)
        // Each block features a prefix, runs checkModificationsScores for current pool, evaluates, and updates choices/statistics
        // Final reporting
        modificationsResults.put("fixedModifications", new HashSet<>(selectedFixedModificationOption));
        modificationsResults.put("refinmentFixedModifications", new HashSet<>(selectedFixedModificationOption));
        preservedMods.removeAll(selectedFixedModificationOption);

        fixedModParamScore.setScore(optProtDataset.getActiveIdentificationNum());
        fixedModParamScore.setParamValue(selectedFixedModificationOption.toString());
        parameterScoreSet.add(fixedModParamScore);

        variableModParamScore.setScore(optProtDataset.getActiveIdentificationNum());
        variableModParamScore.setParamValue(selectedVariableModificationOption.toString());
        parameterScoreSet.add(variableModParamScore);
        modificationsResults.put("variableModifications", new HashSet<>(selectedVariableModificationOption));
        MainUtilities.cleanFolder(Configurations.WORKING_FOLDER_PATH);
        return modificationsResults;
    }
    // ========================== HELPER: Modification Scores ==========================

    /**
     * Helper method to evaluate the impact of one or more candidate PTMs (as
     * either fixed or variable modifications) when applied to the current
     * identification parameters.
     *
     * <p>
     * For each tested modification:
     * <ul>
     * <li>Clears all existing fixed, variable, and refinement modifications
     * from parameters</li>
     * <li>Applies the current selection (fixed/variable), in addition to the
     * candidate PTM(s)</li>
     * <li>Runs a search using {@link #excuteSearch}</li>
     * <li>Collects results for statistical comparison</li>
     * </ul>
     *
     * @param selectedFixedModificationOption Current fixed mods to use for all
     * tests
     * @param selectedVariableModificationOption Current variable mods to use
     * for all tests
     * @param modifications Candidate modifications to test (each one will be
     * added to respective type)
     * @param fixed Whether to test as fixed (true) or variable (false) mod
     * @param msFileName Raw MS file name (used for result naming)
     * @param tempIdParam Working identification parameters structure
     * @param optProtDataset Dataset context for scoring
     * @param identificationParametersFile Parameter file
     * @param searchInputSetting Structure with search engine/input settings
     * @param prefix Parameter string prefix for result names/logging
     * @param addAll If true, add mods even if they do not strictly "improve"
     * the score
     * @return Map of mod name to RawScoreModel with the results of each test
     */
    private Map<String, RawScoreModel> checkModificationsScores(
            ArrayList<String> selectedFixedModificationOption,
            ArrayList<String> selectedVariableModificationOption,
            Set<String> modifications,
            boolean fixed,
            String msFileName,
            IdentificationParameters tempIdParam,
            SearchingSubDataset optProtDataset,
            File identificationParametersFile,
            SearchInputSetting searchInputSetting,
            String prefix,
            boolean addAll
    ) {
        Map<String, RawScoreModel> resultsMap = Collections.synchronizedMap(new LinkedHashMap<>());
        for (String modId : modifications) {
            final String option = modId;
            final String updatedName = Configurations.DEFAULT_RESULT_NAME + prefix + option + "_" + msFileName;

            // Clear all kinds of mods (for a controlled test)
            tempIdParam.getSearchParameters().getModificationParameters().clearFixedModifications();
            tempIdParam.getSearchParameters().getModificationParameters().getRefinementFixedModifications().clear();
            tempIdParam.getSearchParameters().getModificationParameters().clearVariableModifications();

            // Add the mods currently selected in the outer optimization
            for (String fixedMod : selectedFixedModificationOption) {
                tempIdParam.getSearchParameters().getModificationParameters().addFixedModification(ptmFactory.getModification(fixedMod));
                tempIdParam.getSearchParameters().getModificationParameters().addRefinementFixedModification(ptmFactory.getModification(fixedMod));
            }
            for (String variableMod : selectedVariableModificationOption) {
                tempIdParam.getSearchParameters().getModificationParameters().addVariableModification(ptmFactory.getModification(variableMod));
            }

            // Add the mod being tested in this cycle, as fixed or variable
            String paramoption = "";
            if (fixed) {
                tempIdParam.getSearchParameters().getModificationParameters().addFixedModification(ptmFactory.getModification(modId));
                tempIdParam.getSearchParameters().getModificationParameters().addRefinementFixedModification(ptmFactory.getModification(modId));
                paramoption += "Fixed modification :" + modId;
            } else {
                paramoption += "Variable modification :" + modId;
                tempIdParam.getSearchParameters().getModificationParameters().addVariableModification(ptmFactory.getModification(modId));
            }
            paramoption += "\t Included fixed modifications: " + selectedFixedModificationOption + " Included Variable modifications: " + selectedVariableModificationOption;
            final String op = paramoption.replace("[", " ").replace("]", "");
            Future<RawScoreModel> f = MainUtilities.getExecutorService().submit(() -> {
                return excuteSearch(
                        optProtDataset, updatedName, option, tempIdParam, true, searchInputSetting, identificationParametersFile, op
                );
            });
            try {
                RawScoreModel scoreModel = f.get();
                if (scoreModel.isAcceptedChange() || addAll) {
                    resultsMap.put(modId, scoreModel);
                }
            } catch (ExecutionException | InterruptedException ex) {
                ex.printStackTrace();
            }
        }
        // Always clean up all mods (be a good citizen :-))
        tempIdParam.getSearchParameters().getModificationParameters().clearFixedModifications();
        tempIdParam.getSearchParameters().getModificationParameters().getRefinementFixedModifications().clear();
        tempIdParam.getSearchParameters().getModificationParameters().clearVariableModifications();

        return resultsMap;
    }
// ========================== Abstract Search Execution Method ==========================

    /**
     * Abstract method to be implemented by subclasses for performing a search
     * with the specified parameters.
     *
     * <p>
     * This method is called throughout the parameter optimization routines to
     * actually trigger a search under a specific set of parameters. The
     * implementation must execute a proteomics database search (synchronous or
     * asynchronous as appropriate) and return a {@link RawScoreModel}
     * encapsulating identification confidence and summary statistics.
     *
     * <p>
     * Typical implementation steps for concrete subclasses include:
     * <ul>
     * <li>Prepare or update the working directory, input files, and job
     * configuration based on supplied arguments.</li>
     * <li>Run the search in the appropriate environment
     * (local/cluster/cloud).</li>
     * <li>Parse search results and build a RawScoreModel describing the
     * outcome.</li>
     * </ul>
     *
     * @param optProtDataset The dataset being optimized/tested
     * @param defaultOutputFileName Output file to store results, or unique
     * label/annotation
     * @param paramOption Parameter value or label for this run (used in
     * reporting)
     * @param tempIdParam The (temporary) search/identification parameters for
     * this run
     * @param addPeptideMasses If true, peptide masses may also be
     * generated/saved
     * @param searchInputSetting Search engine and input settings, as specified
     * in the workflow
     * @param identificationParametersFile Source file for search parameters
     * (can be useful for reproducibility)
     * @param comment Additional annotation or description for the run
     * @return RawScoreModel Summary statistics/identifications for downstream
     * statistical and optimization scoring
     */
    public abstract RawScoreModel excuteSearch(
            SearchingSubDataset optProtDataset,
            String defaultOutputFileName,
            String paramOption,
            IdentificationParameters tempIdParam,
            boolean addPeptideMasses,
            SearchInputSetting searchInputSetting,
            File identificationParametersFile,
            String comment
    );
}
