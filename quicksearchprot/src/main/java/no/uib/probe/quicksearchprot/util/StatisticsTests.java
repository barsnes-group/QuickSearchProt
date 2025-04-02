/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package no.uib.probe.quicksearchprot.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import no.uib.probe.quicksearchprot.model.PSM;
import org.apache.commons.math.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.TDistribution;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.inference.ChiSquareTest;
import org.apache.commons.math3.stat.inference.KolmogorovSmirnovTest;
import org.apache.commons.math3.stat.inference.MannWhitneyUTest;
import org.apache.commons.math3.stat.inference.TTest;

public class StatisticsTests {

    public static boolean comparableIndependentSamples(DescriptiveStatistics referenceData, DescriptiveStatistics improvedData) {

        int df = StatisticsTests.calculateDegreesOfFreedom((int) referenceData.getN(), (int) improvedData.getN());

        if (df <= 0) {
            return false;
        }

        double zRefTest = StatisticsTests.independentZTest(referenceData, improvedData);
        // Significance level (alpha)
        double alpha = 0.05;
        // Degrees of freedom
        // Calculate the critical value for a two-tailed test
        double criticalValue = StatisticsTests.getCriticalValue(alpha, df);
        return ((Math.abs(zRefTest) > Math.abs(criticalValue)) || !Double.isNaN(zRefTest));

    }

    public static double[] benjaminiHochberg(double[] pValues, double alpha) {
        int n = pValues.length;
        double[] adjustedPValues = new double[n];
        Integer[] indices = new Integer[n];

        for (int i = 0; i < n; i++) {
            indices[i] = i;
        }

        Arrays.sort(indices, Comparator.comparingDouble(i -> pValues[i]));

        for (int i = 0; i < n; i++) {
            int index = indices[i];
            adjustedPValues[index] = pValues[index] * n / (i + 1);
        }

        for (int i = n - 2; i >= 0; i--) {
            adjustedPValues[indices[i]] = Math.min(adjustedPValues[indices[i]], adjustedPValues[indices[i + 1]]);
        }

        return adjustedPValues;
    }

    public static double independentZTest(DescriptiveStatistics referenceSamples, DescriptiveStatistics improvedSample) {
        double mean1 = referenceSamples.getMean();
        double mean2 = improvedSample.getMean();
        double variance1 = referenceSamples.getVariance();
        double variance2 = improvedSample.getVariance();

        double standardError = Math.sqrt((variance2 / improvedSample.getN()) + (variance1 / referenceSamples.getN()));
        double zScore = (mean2 - mean1) / standardError;
        return zScore;

    }

    public static double unpairedZTest(double[] referenceSamples, double[] improvedSample) {

        DescriptiveStatistics referenceSamplesstat = new DescriptiveStatistics(referenceSamples);
        DescriptiveStatistics improvedSamplestat = new DescriptiveStatistics(improvedSample);
        double z = independentZTest(referenceSamplesstat, improvedSamplestat);
        return calculatePValue(z);

    }

    public static int calculateDegreesOfFreedom(int n1, int n2) {
        return n1 + n2 - 2;
    }

    public static double getCriticalValue(double alpha, int df) {
        // Create a T-distribution object with the given degrees of freedom
        TDistribution tDist = new TDistribution(df);
        // Calculate the critical value for a two-tailed test
        double criticalValue = tDist.inverseCumulativeProbability(1 - alpha / 2);
        return criticalValue;
    }

    public static double pairedZTest(DescriptiveStatistics referenceSamples, DescriptiveStatistics improvedSample) {
        double meanDifference = calculatePairedMeanDifference(referenceSamples.getValues(), improvedSample.getValues());
        double standardDeviation = calculatePairedStandardDeviation(referenceSamples.getValues(), improvedSample.getValues(), meanDifference);
        double standardError = standardDeviation / Math.sqrt(improvedSample.getN());
        double zScore = meanDifference / standardError;
        System.out.println("Z score " + zScore);
        if (Double.isNaN(zScore)) {
            return 100;
        }
        return calculatePValue(zScore);
    }

    public static double pairedZTest(double[] referenceSamples, double[] improvedSample) {
        return pairedZTest(new DescriptiveStatistics(referenceSamples), new DescriptiveStatistics(improvedSample));
    }

    private static double calculatePairedMeanDifference(double[] refrenceSample, double[] improvedSample) {
        double sum = 0;
        for (int i = 0; i < improvedSample.length; i++) {
            sum += improvedSample[i] - refrenceSample[i];
        }
        return sum / improvedSample.length;
    }

    private static double calculatePairedStandardDeviation(double[] refrenceSample, double[] improvedSample, double meanDifference) {
        double sum = 0;
        for (int i = 0; i < improvedSample.length; i++) {
            sum += Math.pow((improvedSample[i] - refrenceSample[i]) - meanDifference, 2);
        }
        return Math.sqrt(sum / (improvedSample.length - 1));
    }

    public static double mean(double[] sample) {
        return Arrays.stream(sample).average().orElse(Double.NaN);
    }

    public static double stdDev(double[] sample) {
        double mean = mean(sample);
        double sum = 0;
        for (double num : sample) {
            sum += Math.pow(num - mean, 2);
        }
        return Math.sqrt(sum / (sample.length - 1));
    }

    public static double pValueForZTest(double z) {
        return 2 * (1 - cumulativeProbability(Math.abs(z)));
    }

    public static double cumulativeProbability(double z) {
        return 0.5 * (1 + erf(z / Math.sqrt(2)));
    }

    public static double calculatePValue(double zScore) {
        NormalDistribution normalDist = new NormalDistribution();
        return 2 * (1 - normalDist.cumulativeProbability(Math.abs(zScore)));
    }

    // Approximation of the error function
    public static double erf(double x) {
        double t = 1.0 / (1.0 + 0.5 * Math.abs(x));
        double tau = t * Math.exp(-x * x - 1.26551223 + t * (1.00002368
                + t * (0.37409196 + t * (0.09678418 + t * (-0.18628806
                + t * (0.27886807 + t * (-1.13520398 + t * (1.48851587
                + t * (-0.82215223 + t * 0.17087277)))))))));
        return x >= 0 ? 1 - tau : tau - 1;
    }

    public static double pValueForTTest(double[] fromData, double[] toData) {
        if (toData.length < 2 || fromData.length < 2) {
            return 100.0;
        }
        TTest tTest = new TTest();
        double pValue;
        if (toData.length == fromData.length) {
            pValue = tTest.pairedTTest(fromData, toData);
        } else {
            pValue = tTest.tTest(fromData, toData);//          
        }

        return pValue;
    }

    public static double tTest(double[] fromData, double[] toData) {

        if (toData.length < 2 || fromData.length < 2) {
            return -100.0;
        }

        // Paired t-test
        TTest tTest = new TTest();
        double tStatistic;
        if (toData.length > fromData.length) {
            double[] updatedData = new double[toData.length];
            System.arraycopy(fromData, 0, updatedData, 0, fromData.length);
            fromData = updatedData;
        } else if (toData.length < fromData.length) {
            double[] updatedData = new double[fromData.length];
            System.arraycopy(toData, 0, updatedData, 0, toData.length);
            toData = updatedData;
        }
        tStatistic = tTest.pairedT(fromData, toData);
        return tStatistic;
    }

    public static boolean isSignificatChange(double tStatistic, double pValue, int data1Size, int data2Size) {
        if (tStatistic < 0 && pValue <= 0.05) {//
//            System.out.println("-------------------------------------------->>> the t-test  " + tStatistic + "   pvalue " + pValue + " case 0  " + "FALSE");
            return false;
        }
        if ((Double.isNaN(tStatistic) || Double.isNaN(pValue)) && data1Size == data2Size) {
//            System.out.println("-------------------------------------------->>> the t-test  " + tStatistic + "   pvalue " + pValue + " case 1  " + "FALSE");
            return false;
        }
        if (tStatistic > 0 && data1Size * 1.02 < data2Size) {
//            System.out.println("-------------------------------------------->>> the t-test  " + tStatistic + "   pvalue " + pValue + "  case 2  FALSE");
            return false;
        }
        if (tStatistic < 0 && pValue > 0.05 && data1Size < data2Size * 1.1) {
//            System.out.println("-------------------------------------------->>> the t-test  " + tStatistic + "   pvalue " + pValue + "  case 3  FALSE");
            return false;

        }

//        
//        
//       else  else if ((tStatistic < 0 && pValueForZTest <= 0.05) || (((Double.isNaN(tStatistic) || Double.isNaN(pValueForZTest)) && resultScores.length != referenceScore.length))) {
//            System.out.println("-------------------------------------------->>> the t-test  " + tStatistic + "   pvalue " + pValueForZTest + "  case 4  FALSE");
//            return false;
//        } else if (resultScores.length * 1.02 > referenceScore.length && tStatistic > 0) {
//            System.out.println("-------------------------------------------->>> the t-test  " + tStatistic + "   pvalue " + pValueForZTest + "  case 5  TRUE");
////            Configurations.VALIDATED_ID_REF_DATA = resultScores;
//            return true;
//        }
//        System.out.println("-------------------------------------------->>> the t-test  " + tStatistic + "   pvalue " + pValue + "  case 6  TRUE");
//        System.out.println("else new condition tStatistic " + tStatistic + " Double.isNaN(tStatistic) " + Double.isNaN(tStatistic) + "  resultScores.length " + data1Size + "   " + data2Size);
        return true;

    }

    public static void ratioComparisons(String[] args) {
        // Example dataset: Revenue, Cost, and Employees for different departments
        double[] scores1 = {1000, 2600, 1900, 150};     // Revenue for Marketing, Sales, HR, IT
        double[] scores2 = {750, 2250, 1750, 75};        // Corresponding costs
        int[] spectraNumInQuartiles = {2750, 2750, 2750, 2750};
        String[] quartilesIds = {"Q1", "Q2", "Q3", "Q4"};
        System.out.println("Comparing Ratios in Same Category:");

        for (int i = 0; i < scores1.length; i++) {
            // Calculate Revenue-to-Cost Ratio
            double idToTagRatio = scores1[i] / scores2[i];

            // Calculate Revenue-to-Employee Ratio
            double idToTotal = scores1[i] / spectraNumInQuartiles[i];

            // Print the ratios for each department
            System.out.printf("\nQuartile: %s\n", quartilesIds[i]);
            System.out.printf("Identified spectra-to-Confedent Tag Ratio: %.2f\n", idToTagRatio);
            System.out.printf("identified spectra to total Ratio: %.2f\n", idToTotal);

            // Comparison (e.g., Ratio of Ratios)
            double ratioOfRatios = idToTagRatio / idToTotal;
            System.out.printf("Ratio of Ratios (identified vs total): %.2f\n", ratioOfRatios);

            // Alternatively, you can find the absolute difference between the ratios
            double difference = idToTagRatio - idToTotal;
            System.out.printf("Difference Between Ratios: %.2f\n", difference);
        }
    }

    public static double calculatePearsonCorrelationTest(double[] values1, double[] values2) {
        PearsonsCorrelation test = new PearsonsCorrelation();
        double correlation = test.correlation(values1, values2);
        correlation = Math.floor(correlation * 100.0) / 100.0;
        System.out.println("correlation is " + correlation);
        return correlation;

    }

    public static void modeMedian() {
        // Sample data
        double[] sample1 = {1.83, 0.50, 1.62, 2.48, 2.01, 1.32};
        double[] sample2 = {0.88, 0.65, 0.80, 1.15, 1.01, 0.75};

        // Combine samples to find the overall median
        double[] combined = new double[sample1.length + sample2.length];
        System.arraycopy(sample1, 0, combined, 0, sample1.length);
        System.arraycopy(sample2, 0, combined, sample1.length, sample2.length);
        double median = findMedian(combined);

        // Create contingency table
        long[][] contingencyTable = new long[2][2];
        for (double v : sample1) {
            if (v > median) {
                contingencyTable[0][0]++;
            } else {
                contingencyTable[1][0]++;
            }
        }
        for (double v : sample2) {
            if (v > median) {
                contingencyTable[0][1]++;
            } else {
                contingencyTable[1][1]++;
            }
        }

        // Perform chi-square test
        ChiSquareTest chiSquareTest = new ChiSquareTest();
        double pValue = chiSquareTest.chiSquareTest(contingencyTable);

        // Output the result
        System.out.println("P-value: " + pValue + "  value " + chiSquareTest.chiSquare(contingencyTable));
    }

    private static double findMedian(double[] data) {
        java.util.Arrays.sort(data);
        int middle = data.length / 2;
        if (data.length % 2 == 0) {
            return (data[middle - 1] + data[middle]) / 2.0;
        } else {
            return data[middle];
        }
    }

    public static double WilcoxonSignedRankTest(double[] before, double[] after) {

        double[] differences = new double[before.length];
        for (int i = 0; i < before.length; i++) {
            differences[i] = after[i] - before[i];
        }
        double[] absDifferences = Arrays.stream(differences).map(Math::abs).toArray();
        int[] ranks = rank(absDifferences);
        double positiveRankSum = 0;
        double negativeRankSum = 0;
        for (int i = 0; i < differences.length; i++) {
            if (differences[i] > 0) {
                positiveRankSum += ranks[i];
            } else if (differences[i] < 0) {
                negativeRankSum += ranks[i];
            }
        }
        double W = Math.min(positiveRankSum, negativeRankSum);
        return W;
    }

    private static int[] rank(double[] values) {
        int n = values.length;
        int[] ranks = new int[n];
        Double[] sortedValues = Arrays.stream(values).boxed().toArray(Double[]::new);
        Arrays.sort(sortedValues);

        for (int i = 0; i < n; i++) {
            ranks[i] = Arrays.asList(sortedValues).indexOf(values[i]) + 1;
        }
        return ranks;
    }

    public static void mannWhitneyUTest(double[] sample1, double[] sample2) {
        // Sample data

        // Create a MannWhitneyUTest instance
        MannWhitneyUTest mannWhitneyUTest = new MannWhitneyUTest();

        // Perform Mann-Whitney U Test
        double pValue = mannWhitneyUTest.mannWhitneyUTest(sample1, sample2);
        double diffrent = mannWhitneyUTest.mannWhitneyU(sample1, sample2);

        // Print the results
        System.out.println("Mann-Whitney U Test p-value: " + pValue + "  " + diffrent);
        if (pValue < 0.05) {
            System.out.println("There is a statistically significant difference between the two samples.");
        } else {
            System.out.println("There is no statistically significant difference between the two samples.");
        }

        DescriptiveStatistics sample1Stat = new DescriptiveStatistics(sample1);
        DescriptiveStatistics sample2Stat = new DescriptiveStatistics(sample2);

        System.out.println(" data direction is " + (sample1Stat.getMean() - sample2Stat.getMean()));
// Create a KolmogorovSmirnovTest instance
        KolmogorovSmirnovTest ksTest = new KolmogorovSmirnovTest();

        // Perform Kolmogorov-Smirnov Test
        double pValue2 = ksTest.kolmogorovSmirnovTest(sample1, sample2);

        // Print the results
        System.out.println("Kolmogorov-Smirnov Test p-value: " + pValue2);
        if (pValue2 < 0.05) {
            System.out.println("There is a statistically significant difference between the two samples.");
        } else {
            System.out.println("There is no statistically significant difference between the two samples.");
        }
    }

    public static double calculateCohensD(double mean1, double mean2, double sd1, double sd2) {
        return (mean1 - mean2) / Math.sqrt((Math.pow(sd1, 2) + Math.pow(sd2, 2)) / 2);
    }

    public static void main(String[] args) {

        double[] sample1 = {1, 2, 5, 9, 12, 20};
        double[] sample2 = {5, 9, 14, 17, 19, 20};
        StatisticsTests.mannWhitneyUTest(sample1, sample2);

    }

    public static double pairedZTEst(double[] reference, double[] test) {
        double[] differences = calculateDifferences(reference, test);
        double meanDifference = calculateMean(differences);
        double standardDeviationDifference = calculateStandardDeviation(differences, meanDifference);
        double zScore = calculateZScore(meanDifference, standardDeviationDifference, differences.length);
        if (Double.isNaN(zScore)) {
            zScore = 0;
        }
        return zScore;
    }

    public static double unPairedZTEst(double[] reference, double[] test) {
        double mean1 = calculateMean(reference);
        double mean2 = calculateMean(test);

        double variance1 = calculateVariance(reference, mean1);
        double variance2 = calculateVariance(test, mean2);
        double zScore = calculateZScore(mean1, mean2, variance1, variance2, reference.length, test.length);

        if (Double.isNaN(zScore)) {
            zScore = 0;
        }
        return zScore;
    }

    public static double calculateMean(double[] sample) {
        double sum = 0.0;
        for (double num : sample) {
            sum += num;
        }
        return sum / sample.length;
    }

    public static double calculateVariance(double[] sample, double mean) {
        double sum = 0.0;
        for (double num : sample) {
            sum += Math.pow(num - mean, 2);
        }
        return sum / sample.length;
    }

    public static double calculateZScore(double mean1, double mean2, double variance1, double variance2, int n1, int n2) {
        double standardError = Math.sqrt(variance1 / n1 + variance2 / n2);
        return (mean1 - mean2) / standardError;
    }

    public static double[] calculateDifferences(double[] before, double[] after) {
        double[] differences = new double[before.length];
        for (int i = 0; i < before.length; i++) {
            differences[i] = after[i] - before[i];
        }
        return differences;
    }

    public static double calculateStandardDeviation(double[] sample, double mean) {
        double sum = 0.0;
        for (double num : sample) {
            sum += Math.pow(num - mean, 2);
        }
        return Math.sqrt(sum / sample.length);
    }

    public static double calculateZScore(double meanDifference, double standardDeviationDifference, int n) {
        return meanDifference / (standardDeviationDifference / Math.sqrt(n));
    }

    public static double findThresholdForFDR(List<PSM> psms, double targtedFDR) {
        Collections.sort(psms);
        double decoyHits = 0;
        double targetHits = 0;
        double fdr;

        for (PSM psm : psms) {
//            if (psm.getScore() < threshold) break;
            if (psm.isIsTarget()) {
                targetHits++;
            }
            if (!psm.isIsTarget()) {
                decoyHits++;
            }
            fdr = (double) decoyHits / (targetHits + decoyHits);
            if (fdr >= targtedFDR) {
//                System.out.println("---------------->>> thr is  " + psm.getScore());
                return psm.getScore();

            }
        }
        return -1;
    }

   

}
