package no.uib.probe.quicksearchprot.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.Skewness;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.apache.commons.math3.stat.inference.MannWhitneyUTest;
import org.apache.commons.math3.stat.inference.OneWayAnova;
import org.apache.commons.math3.stat.inference.TTest;
import org.apache.commons.math3.stat.inference.WilcoxonSignedRankTest;

public class ScoreComparison {

    public static double compareSameDistributionData(double[] reference, double[] test) {
        // Calculate skewness
        Skewness skewness = new Skewness();
        double skewnessValue = skewness.evaluate(reference);
        // Output the result
        System.out.println("ref  Skewness: " + skewnessValue);
        double skewnessValue1 = skewness.evaluate(test);
        // Output the result
        System.out.println("test share Skewness: " + skewnessValue1);
        System.out.println("final results " + (skewnessValue1 - skewnessValue) + "  pvalue " + StatisticsTests.unpairedZTest(reference, test));
        if (StatisticsTests.unpairedZTest(reference, test) < 0.05) {
            return (skewnessValue1 - skewnessValue);
        } else {
            return 0;
        }

    }

    public static double comparediffrentDistributionData(double[] reference, double[] test) {

        // Calculate means
        Mean mean = new Mean();
        double mean1 = mean.evaluate(reference);
        double mean2 = mean.evaluate(test);

        // Calculate standard deviations
        StandardDeviation sd = new StandardDeviation();
        double sd1 = sd.evaluate(reference);
        double sd2 = sd.evaluate(test);

        // Calculate pooled standard deviation
        double pooledSD = Math.sqrt(((sd1 * sd1) + (sd2 * sd2)) / 2);

        // Calculate Cohen's d
        double cohenD = (mean2 - mean1) / pooledSD;

        // Perform Mann-Whitney U Test
        MannWhitneyUTest mannWhitneyUTest = new MannWhitneyUTest();
        double pValue = mannWhitneyUTest.mannWhitneyUTest(reference, test);

        // Output the results
        System.out.println("Mean of sample 1: " + mean1);
        System.out.println("Mean of sample 2: " + mean2);
        System.out.println("Standard deviation of sample 1: " + sd1);
        System.out.println("Standard deviation of sample 2: " + sd2);
        System.out.println("Pooled standard deviation: " + pooledSD);
        System.out.println("Cohen's d: " + cohenD);
        System.out.println("P-value: " + pValue);
        if (pValue < 0.05) {
            System.out.println("The samples are significantly different.");
        } else {
            System.out.println("The samples are not significantly different.");
        }

        // Determine which sample is better based on the defined criteria
        if (mean2 > mean1) {
            System.out.println("Sample 2 has better performance based on the mean.");
        } else {
            System.out.println("Sample 1 has better performance based on the mean.");
        }
        if (pValue < 0.05 && Math.abs(cohenD) >= 0.5) {
            return cohenD;
        } else {
            return 0;
        }

    }

    private double calculateMean(double[] scores) {
        double sum = 0;
        for (double score : scores) {
            sum += score;
        }
        return sum / scores.length;
    }

    private double percentageImprovementIndependenet(double referenceMedian, double improvedMedian) { //(improvedMedian - referenceMedian) * 100.0) / referenceMedian)
        if (referenceMedian == 0) {
            return (improvedMedian * 100.0);
        }
        return (((improvedMedian - referenceMedian) * 100.0) / referenceMedian);//
    }

    private double percentageImprovementPaired(double[] referenceData, double[] improvedData) {
        if (improvedData.length == 0) {
            return 0;
        }
        double[] dArr = new double[referenceData.length];
        for (int i = 0; i < referenceData.length; i++) {
            double improvment = (improvedData[i] - referenceData[i]) / referenceData[i];
            improvment = improvment * 100.0;
            dArr[i] = improvment;
        }
        DescriptiveStatistics ds = new DescriptiveStatistics(dArr);
        if (ds.getPercentile(50.0) == 0.0) {//|| 
            return ds.getMean();
        }

        return ds.getGeometricMean();//ds.getPercentile(50.0);
    }
//rank-based Cohen's d

    public double medianBasedEffectSize(DescriptiveStatistics referenceSamples, DescriptiveStatistics improvedSample) {
// Step 1: Calculate the median of each group
        double medianGroup1 = referenceSamples.getPercentile(50);
        double medianGroup2 = improvedSample.getPercentile(50);

        // Step 2: Calculate the MAD for each group
        double madGroup1 = calculateMAD(referenceSamples);
        double madGroup2 = calculateMAD(improvedSample);

        // Step 3: Calculate the pooled MAD
        double pooledMAD = (madGroup1 + madGroup2) / 2;
        // Step 4: Calculate the median-based effect size
        double dMedian = (medianGroup2 - medianGroup1) / pooledMAD;
        if (Double.isInfinite(dMedian)) {
            dMedian = 0;
        }

        return dMedian;
    }

    private double calculateMAD(DescriptiveStatistics samples) {
        // Step 1: Calculate the median of the dataset
        double medianValue = samples.getPercentile(50);

        // Step 2: Calculate the absolute deviations from the median
        double[] absoluteDeviations = Arrays.stream(samples.getValues())
                .map(value -> Math.abs(value - medianValue))
                .toArray();

        // Step 3: Calculate the median of the absolute deviations
        double mad = new DescriptiveStatistics(absoluteDeviations).getPercentile(50);

        return mad;
    }

    public double calculateCohensD(DescriptiveStatistics referenceSamples, DescriptiveStatistics improvedSample) {

        double mean1 = referenceSamples.getMean();
        double mean2 = improvedSample.getMean();
        double variance1 = referenceSamples.getVariance();
        double variance2 = improvedSample.getVariance();
        double pooledStdDev = Math.sqrt(((referenceSamples.getN() - 1) * variance1 + (improvedSample.getN() - 1) * variance2) / (referenceSamples.getN() + improvedSample.getN() - 2));
        double normCohenD = ((mean2 - mean1) / pooledStdDev);
        return normCohenD;
    }

    public double calculateStandardDeviation(double[] scores) {
        double mean = calculateMean(scores);
        double sum = 0;
        for (double score : scores) {
            sum += Math.pow(score - mean, 2);
        }
        return Math.sqrt(sum / scores.length);
    }
    // Create a normal distribution with mean 0 and standard deviation 1

    public double dataSizeEffect(double dataSizeFrom, double dataSizeTo) {
        dataSizeFrom++;
        dataSizeTo++;
        double effect = (dataSizeTo - dataSizeFrom) * 100.0 / Math.min(dataSizeFrom, dataSizeTo);
        return logScaleNormalize(effect, Math.E);

    }

    public double performTTest(double[] s1, double[] s2, boolean switchData) {

        double[] scores1, scores2;
        if (switchData) {
            scores1 = s2;
            scores2 = s1;
        } else {
            scores1 = s1;
            scores2 = s2;
        }
        if (scores1.length < 2 || scores2.length < 2) {
            return 1;
        }
        TTest tTest = new TTest();

        return tTest.tTest(scores2, scores1);
    }

    /**
     * Applies log scale normalization to an array of data.
     *
     * @param value The array of data to be normalized.
     * @param base The base of the logarithm to use.
     * @return The log scale normalized array of data.
     */
    public static double logScaleNormalize(double value, double base) {

        double sign = 1;
        if (value < 0) {
            sign = -1;
        }
        value = Math.abs(value);
        double normalizedData = (Math.log(value + 1) / Math.log(base)) * sign;
        if (Double.isNaN(normalizedData)) {
            normalizedData = 0;
        }
        return normalizedData;
    }

    /**
     * Scale improvement score based on z-score range.
     *
     * @param score The improvement score.
     * @return The normalized scare.
     */
    public static double scoreScaling(double score, double scoreMin, double scoreMax) {
        double minValue = -1.0;
        double maxValue = 1.0;

        score = Math.min(score, scoreMax);
        score = Math.max(score, scoreMin);

        double normalizedScore = ((score - scoreMin) / (scoreMax - scoreMin)) * (maxValue - minValue);
        normalizedScore += minValue;

        normalizedScore = Math.max(normalizedScore, minValue);
        normalizedScore = Math.min(normalizedScore, maxValue);

        return normalizedScore;
    }

    public double calculateScore(double[] referenceInputData, double[] testInputData, boolean paired) {

        double[] referenceScores = referenceInputData;
        double[] testScores = testInputData;
        if (referenceScores.length < 2 && testScores.length < 2) {
            return 0;//new double[]{0, 0, 0, 0, 0, 0};
        }

        DescriptiveStatistics referenceData = new DescriptiveStatistics(referenceScores);
        DescriptiveStatistics improvedData = new DescriptiveStatistics(testScores);
        if (Double.isNaN(referenceData.getPercentile(50)) && Double.isNaN(improvedData.getPercentile(50))) {
            System.out.println("both nan " + referenceData.getGeometricMean() + "   " + referenceData.getN() + "  " + improvedData.getN());
            return 0;
        }
        double improvmentScorePersentage;
        if (paired) {
            improvmentScorePersentage = percentageImprovementPaired(referenceScores, testScores);
//            System.out.println("improvment paired score % "+improvmentScorePersentage);
            if (Double.isNaN(improvmentScorePersentage)) {
                return 0;
            }

            return improvmentScorePersentage;
        } else {
            if (Double.isNaN(improvedData.getGeometricMean()) || Double.isNaN(referenceData.getGeometricMean())) {//|| (improvedData.getN() <= 4 && referenceData.getN() > 20)) {
                if (referenceData.getN() > improvedData.getN()) {
                    System.out.println("return -100 ------------2  " + improvedData.getGeometricMean());
                    return -100;
                } else if (referenceData.getN() < improvedData.getN()) {
                    return 100;
                } else {
                    return 0;
                }

            }

            int df = StatisticsTests.calculateDegreesOfFreedom((int) referenceData.getN(), (int) improvedData.getN());
            if (df == 0) {
                return 0;
            }
            double zRefTest = StatisticsTests.independentZTest(referenceData, improvedData);
            // Significance level (alpha)
            double alpha = 0.05;
            // Degrees of freedom
            // Calculate the critical value for a two-tailed test
            double criticalValue = StatisticsTests.getCriticalValue(alpha, df);

            if (!(Math.abs(zRefTest) > Math.abs(criticalValue)) || Double.isNaN(zRefTest)) {//|| (improvedData.getN() <= 4 && referenceData.getN() > 20)) {
                System.out.println("unpaired  refer " + referenceData.getGeometricMean() + " test.getGeometricMean() " + improvedData.getGeometricMean());
                if (improvedData.getGeometricMean() == referenceData.getGeometricMean()) {
                    System.out.println("Strangeeeee equal data " + improvedData.getN() + "  " + referenceData.getN() + "  " + paired);
                    return 0.0;

                }
//                double prec = ((double) Math.abs(referenceData.getN() - improvedData.getN())) / (double) Math.max(referenceData.getN(), improvedData.getN());
//                prec *= 100.0;
                double referenceMedian = referenceData.getGeometricMean();// referenceData.getPercentile(50);
                double improvedMedian = improvedData.getGeometricMean();//improvedData.getPercentile(50);
                improvmentScorePersentage = percentageImprovementIndependenet(referenceMedian, improvedMedian);

//                double cohnD = StatisticsTests.calculateCohensD(referenceData.getMean(), improvedData.getMean(), referenceData.getStandardDeviation(), improvedData.getStandardDeviation());
//               
//                double skewDiff = improvedData.getSkewness()-referenceData.getSkewness();
//                 System.out.println("cohenD value "+cohnD+"   skw "+skewDiff+"  impScore "+improvmentScorePersentage);
//                if (referenceData.getN() > improvedData.getN()) {
//                    System.out.println("improvment pers " + improvmentScorePersentage + "  " + (-1.0 * prec));
//                    return -1.0 * prec;
//                } else {
//                    System.out.println("improvment pers " + improvmentScorePersentage + "  " + (prec));
//                    return prec;
//                }
                return improvmentScorePersentage;//improvmentScorePersentage;
            } else {
                double referenceMedian = referenceData.getGeometricMean();// referenceData.getPercentile(50);
                double improvedMedian = improvedData.getGeometricMean();//improvedData.getPercentile(50);
                System.out.println("reach level 3 " + referenceMedian + "   " + improvedMedian + "   " + referenceData.getN() + "    test " + improvedData.getN());
                improvmentScorePersentage = percentageImprovementIndependenet(referenceMedian, improvedMedian);
                return improvmentScorePersentage;

            }

        }

    }

    public static double measuringEffectPercentage(double[] sharedData, double[] independedntData) {

        if (independedntData.length == 0) {
            return 0;
        }
        DescriptiveStatistics sharedStatData = new DescriptiveStatistics(sharedData);
        DescriptiveStatistics independentStatData = new DescriptiveStatistics(independedntData);
        double mean1 = sharedStatData.getPercentile(50);
        double mean2 = independentStatData.getPercentile(50);
        double effect = Math.abs(mean1 - mean2) / mean1;
        effect *= 100.0;
        if (mean2 < mean1) {
            effect *= -1.0;
        }
        System.out.println("effect is " + effect + "  " + mean1 + "  " + mean2 + "  " + sharedStatData.getN() + "   " + independentStatData.getN());
        return effect;

    }

    public double calculateScore1(double[] referenceInputData, double[] testInputData, boolean paired) {

        double[] referenceScores = referenceInputData;
        double[] testScores = testInputData;
        if (referenceScores.length < 2 && testScores.length < 2) {
            return 0;//new double[]{0, 0, 0, 0, 0, 0};
        }

        List<Double> valuesToScore = new ArrayList<>();
        DescriptiveStatistics referenceData = new DescriptiveStatistics(referenceScores);
        DescriptiveStatistics improvedData = new DescriptiveStatistics(testScores);
        if (Double.isNaN(referenceData.getPercentile(50)) && Double.isNaN(improvedData.getPercentile(50))) {
            System.out.println("both nan " + referenceData.getGeometricMean() + "   " + referenceData.getN() + "  " + improvedData.getN());
            return 0;//new double[]{0, 0, 0, 0, 0, 0};
        }

        double improvmentScorePersentage;
//        double comparisonScore;
//        double sizeEffect;
//        double cohensD = 0;
        if (paired) {
            improvmentScorePersentage = percentageImprovementPaired(referenceScores, testScores);
            return improvmentScorePersentage;
//            if (improvmentScorePersentage == 0) {
//                return 0;
//            }
//            comparisonScore = wilcoxonSignedRankTestPair(referenceData.getValues(), improvedData.getValues());
//            if (improvmentScorePersentage < 0) {
//                comparisonScore *= -1.0;
//            }
//            sizeEffect = medianBasedEffectSize(referenceData, improvedData);
//            cohensD = 0;//calculateCohensD(referenceData, improvedData);

        } else {
            int df = StatisticsTests.calculateDegreesOfFreedom((int) referenceData.getN(), (int) improvedData.getN());
            if (df == 0) {
                return 0;
            }
            double zRefTest = StatisticsTests.independentZTest(referenceData, improvedData);
            // Significance level (alpha)
            double alpha = 0.05;
            // Degrees of freedom
            // Calculate the critical value for a two-tailed test
            double criticalValue = StatisticsTests.getCriticalValue(alpha, df);

//            if ((Math.abs(zRefTest) > Math.abs(criticalValue)) != (Math.abs(mwTest) > Math.abs(criticalValue))) {
//            }
            // Calculate the critical value for a two-tailed test
//            if (!(Math.abs(zRefTest) > Math.abs(criticalValue))) {
//                double criticalValue2 = StatisticsTests.getCriticalValue(0.5, df);
//                System.out.println("update Critical value: " + criticalValue + "  diffrent is sig  " + (Math.abs(zRefTest) > Math.abs(criticalValue2)) + "  " + Math.abs(zRefTest) + " > " + Math.abs(criticalValue2));
//            }
//            if (!(Math.abs(zRefTest) > Math.abs(criticalValue)) || Double.isNaN(zRefTest)) {
//                System.out.println("not sig diffrenet " + referenceData.getN() + "  " + improvedData.getN() + "   ");
//                double prec = (Math.abs(referenceData.getN() - improvedData.getN())) / Math.max(referenceData.getN(), improvedData.getN());
//                if (referenceData.getN() > improvedData.getN()) {
//                    return -1.0 * prec;
//                } else {
//                    return prec;
//                }
//            }
//            if (Double.isNaN(referenceData.getGeometricMean()) || (referenceData.getN() <= 4 && improvedData.getN() > 20)) {
//                System.out.println("ref data was nan " + referenceData.getGeometricMean() + "   " + referenceData.getN() + "  " + improvedData.getN());
//                improvmentScorePersentage = 100.0;
//                return improvmentScorePersentage;//logScaleNormalize(improvmentScorePersentage, 10);
//            } else
            if (!(Math.abs(zRefTest) > Math.abs(criticalValue)) || Double.isNaN(zRefTest) || Double.isNaN(improvedData.getGeometricMean()) || Double.isNaN(referenceData.getGeometricMean())) {//|| (improvedData.getN() <= 4 && referenceData.getN() > 20)) {

                System.out.println("unpatired data un balancd is here ------");
                double prec = ((double) Math.abs(referenceData.getN() - improvedData.getN())) / (double) Math.max(referenceData.getN(), improvedData.getN());
                prec *= 100.0;
                double referenceMedian = referenceData.getGeometricMean();// referenceData.getPercentile(50);
                double improvedMedian = improvedData.getGeometricMean();//improvedData.getPercentile(50);
                improvmentScorePersentage = percentageImprovementIndependenet(referenceMedian, improvedMedian);
                if (referenceData.getN() > improvedData.getN()) {
                    System.out.println("improvment pers " + improvmentScorePersentage + "  " + (-1.0 * prec));
                    return -1.0 * prec;
                } else {
                    System.out.println("improvment pers " + improvmentScorePersentage + "  " + (prec));
                    return prec;
                }
            } else {
                double referenceMedian = referenceData.getGeometricMean();// referenceData.getPercentile(50);
                double improvedMedian = improvedData.getGeometricMean();//improvedData.getPercentile(50);
//                if (referenceMedian == 0 || improvedMedian == 0) {
////                    System.out.println("swich to mean before " + referenceMedian + "  " + improvedMedian);
//                    referenceMedian = referenceData.getMean();
//                    improvedMedian = improvedData.getMean();
////                    System.out.println("swich to mean after " + referenceMedian + "  " + improvedMedian);
//                }
//                comparisonScore = mannWhitneyTestIndependent(referenceData.getValues(), improvedData.getValues());
                improvmentScorePersentage = percentageImprovementIndependenet(referenceMedian, improvedMedian);
                return improvmentScorePersentage;
//                if (improvmentScorePersentage < 0) {
//                    comparisonScore *= -1.0;
//                }
//                cohensD = calculateCohensD(referenceData, improvedData);
//                sizeEffect = medianBasedEffectSize(referenceData, improvedData);
            }

        }

//        if (!paired && (improvmentScorePersentage > 0 != cohensD > 0)) {
//            System.out.println("final scores " + improvmentScorePersentage + "   SE  " + "  cohn d " + cohensD + "  ref: " + referenceData.getN() + "  target  " + improvedData.getN());
//            double referenceMedian = referenceData.getMean();
//            double improvedMedian = improvedData.getMean();
//            double improvmentScorePersentage2 = percentageImprovementIndependenet(referenceMedian, improvedMedian);
//            System.out.println("updated use avg  scores " + improvmentScorePersentage2);
//
////            for (double d : referenceInputData) {
////                System.out.print(d + ",");
////            }
////            System.out.println("");
////            System.out.println("vs");
////            for (double d : improvedInputData) {
////                System.out.print(d + ",");
////            }
////            System.out.println("");
////            System.exit(0);
//        }
//        if (cohensD < 0 && ((comparisonScore > 0) || (improvmentScorePersentage > 0) || (sizeEffect > 0))) {
//            System.out.println("------------------------------------------------------>>>>> cohen d negative " + cohensD + "  " + comparisonScore + "  " + improvmentScorePersentage + "  " + sizeEffect);
//            if (comparisonScore > 0) {
//                comparisonScore *= -1.0;
//            }
//            if (improvmentScorePersentage > 0) {
//                improvmentScorePersentage *= -1.0;
//            }
//            if (sizeEffect > 0) {
//                sizeEffect *= -1.0;
//            }
//        }
//        double normalisedComparisonScore = logScaleNormalize(comparisonScore, 10);
//        double normalisedImprovmentScore = improvmentScorePersentage;//logScaleNormalize(improvmentScorePersentage, 10);//scoreScaling(percentageImprovement,-30,30);
//        double normalisedSizeEffectScore = logScaleNormalize(sizeEffect, 10);
//        if (referenceData.getN() > 1 && improvedData.getN() > 1) {
////            valuesToScore.add(normalisedComparisonScore);
//            valuesToScore.add(normalisedImprovmentScore);
////            valuesToScore.add(normalisedSizeEffectScore);
////            valuesToScore.add(normalisedCohensDScore);
//        } else {
//            referenceData.addValue(0);
//            valuesToScore.add(0.0);
//        }
//
//        double finalScore = 0;
//        for (double ss : valuesToScore) {
//            finalScore += ss;
//        }
//        finalScore = finalScore / (double) valuesToScore.size();
//        if(comparisonScore<0.05)
//            finalScore=0;
//        if (flip) {
//            finalScore *= -1.0;
//        }
//        return improvmentScorePersentage;//finalScore;
    }

    public double calculateCohensD(double[] s1, double[] s2, boolean switchData) {
        double[] from, to;
        if (switchData) {
            from = s2;
            to = s1;
        } else {
            from = s1;
            to = s2;
        }
        double mean1 = Arrays.stream(from).average().orElse(0.0);
        double mean2 = Arrays.stream(to).average().orElse(0.0);
        double variance1 = Arrays.stream(from).map(x -> Math.pow(x - mean1, 2)).sum() / (from.length - 1);
        double variance2 = Arrays.stream(to).map(x -> Math.pow(x - mean2, 2)).sum() / (to.length - 1);
        double pooledStdDev = Math.sqrt(((from.length - 1) * variance1 + (to.length - 1) * variance2) / (from.length + to.length - 2));
        double normCohenD = ((mean2 - mean1) / pooledStdDev);
        return (normCohenD);
    }

    public double oneToManyScore(double fromScore, DescriptiveStatistics stats) {
        double oldAverage = stats.getMean();
        // Step 2: Calculate the improvement score (percentage improvement)
        double improvementPercentage;
        if (fromScore == 0) {
            improvementPercentage = 1;
        } else {
            improvementPercentage = ((oldAverage - fromScore) / fromScore) * 100;
        }
        // Step 3: Compare the size of the arrays (if there are more or fewer old scores)
        int newSize = 1; // Single new score vs. old scores array
        int oldSize = (int) stats.getN();
        // Optional: Adjust the improvement calculation based on size
        double sizeFactor = (double) oldSize / newSize;
        double adjustedImprovement = improvementPercentage * sizeFactor;
        return adjustedImprovement;
    }

    public double mannWhitneyTestIndependent(double[] sample1, double[] sample2) {

        MannWhitneyUTest mannWhitneyUTest = new MannWhitneyUTest();
        if (sample1.length == 0 || sample2.length == 0) {
            return Double.NaN;
        }

//        double pValue = mannWhitneyUTest.mannWhitneyUTest(sample1, sample2);
//        if (pValue <= 0.05) {
//            int n1 = sample1.length;
//            int n2 = sample2.length;
//            double N = n1 + n2;
        double U = mannWhitneyUTest.mannWhitneyU(sample1, sample2);
////            Calculate rank  -biserial correlation
//            double rankBiserial = 1 - (2 * U) / (n1 * n2);
//
//            // Calculate Z-score (approximation)
//            double meanU = n1 * n2 / 2.0;
//            double stdU = Math.sqrt(n1 * n2 * (n1 + n2 + 1) / 12.0);
//            double Z = (U - meanU) / stdU;
//
//            // Calculate effect size r
//            double r = Z / Math.sqrt(N);
//            System.out.println("at r is " + r);
        return U;
//            
//            return r;
//        }
//        return 0;
    }

    public double wilcoxonSignedRankTestPair(double[] referenceData, double[] improvedData) {

        WilcoxonSignedRankTest wilcoxonSignedRankTest = new WilcoxonSignedRankTest();
        double pValue = wilcoxonSignedRankTest.wilcoxonSignedRankTest(referenceData, improvedData, false);

        return pValue;
    }

    public static void main(String[] args) {
        double[] scores1 = {24.181788868689083, 26.381088683588953, 26.932822348455282, 28.183098374208907, 26.502771452416063, 26.313842050319227, 23.30745235741934, 24.101928241008064, 25.981828068689527, 23.034502191750313, 23.16842976349253};
        double[] scores2 = {23.85216786366755, 24.052747807215674, 22.680980745877108, 23.174512846898736, 31.306301299262387, 27.555997926642448, 23.74433648517002, 29.399078749253825, 26.16724952495946, 24.702858615186784};
//        double[] scores3 = {91.0, 89.0, 95.0, 87.0, 93.5, 90.0, 95.0, 89.0, 95.0, 87.0, 93.5, 90.0, 89.0, 95.0, 87.0, 93.5, 90.0, 20, 05};
//        double[] beforeWeights = {85, 78, 92, 70, 65, 90, 72, 88, 76, 82};
//        double[] afterWeights = {80, 75, 88, 68, 63, 85, 70, 84, 73, 78};
//
        DescriptiveStatistics ds1 = new DescriptiveStatistics(scores1);
        DescriptiveStatistics ds2 = new DescriptiveStatistics(scores2);
        ScoreComparison sc = new ScoreComparison();
        double cohenD = (sc.calculateCohensD(ds1, ds2));
        double medianImpro = sc.percentageImprovementIndependenet(ds1.getPercentile(50), ds2.getPercentile(50));
        double meanImpro = sc.percentageImprovementIndependenet(ds1.getGeometricMean(), ds2.getGeometricMean());
        double mean2Impro = sc.percentageImprovementIndependenet(ds1.getMean(), ds2.getMean());
        double normZ = StatisticsTests.independentZTest(ds1, ds2);
        System.out.println("improvment " + medianImpro + "   mean " + meanImpro + "  " + cohenD + "  " + normZ + "  " + mean2Impro);
//       
//        double normImpro = sc.logScaleNormalize(;
//        
//        sc.calculateCohensD(ds1, ds2);
//        double dMedian = (sc.medianBasedEffectSize(ds1, ds2));
//        double finalScore = (cohenD + normZ + normImpro) / 3.0;
//        double finalScore2 = (dMedian + normZ + normImpro) / 3.0;
//
////        double normZ2 = sc.mannWhitneyTestIndependent(ds1.getValues(), ds2.getValues());
//        double normZ3 = sc.wilcoxonSignedRankTestPair(scores3, scores2);
//        double normZ4 = StatisticsTests.WilcoxonSignedRankTest(scores3, scores2);
////        System.out.println("Final Score: " + normImpro + "  " + normZ + "  " + cohenD + "   " + dMedian + "------------>> " + finalScore + "  vs " + finalScore2);
////        System.out.println("z Score: " + "  " + normZ3 + "  vs " + normZ4);
//
////         Interpretation
//        if (finalScore > 0.75) {

//        } else if (finalScore > 0.5) {
//            System.out.println("Moderate positive enhancement in scores.");
//        } else if (finalScore > 0.25) {
//            System.out.println("Slight positive enhancement in scores.");
//        } else {
//            System.out.println("No significant enhancement or decline in scores.");
//        }
//
    }

    public static double compareReferenceToTest1(double[] sample1, double[] sample2, boolean potintialFP) {
        boolean comparableSamples = isSamplesComparable(sample1, sample2);
        if (!comparableSamples && !potintialFP) {
            System.out.println("sample was uncomparable " + sample1.length + "  " + sample2.length);
            return sample2.length - sample1.length;
        }
        double[] refernce = sample1;
        double[] test = sample2;
        boolean flip = false;
        if (potintialFP) {
            if (sample1.length > sample2.length) {
                refernce = sample2;
                test = sample1;
                flip = true;
                System.out.println("flipped data " + sample1.length + "  " + sample2.length);
            }

        }

        DescriptiveStatistics refernceDescriptiveStatistics = new DescriptiveStatistics(refernce);
//        double[] ref_weights = quartileAnalysis(refernceDescriptiveStatistics);

//        DescriptiveStatistics testDescriptiveStatistics = new DescriptiveStatistics(test);
//        double[] test_weights = quartileAnalysis(testDescriptiveStatistics,refernceDescriptiveStatistics.getPercentile(25),refernceDescriptiveStatistics.getPercentile(50),refernceDescriptiveStatistics.getPercentile(75));
//        System.out.println("ref " + refernceDescriptiveStatistics.getPercentile(25) + "  " + refernceDescriptiveStatistics.getPercentile(50) + "  " + refernceDescriptiveStatistics.getPercentile(75));
//        System.out.println("tes " + testDescriptiveStatistics.getPercentile(25) + "  " + testDescriptiveStatistics.getPercentile(50) + "  " + testDescriptiveStatistics.getPercentile(75));
        double referenceQ1 = refernceDescriptiveStatistics.getPercentile(25);
        double referenceMedian = refernceDescriptiveStatistics.getPercentile(50);
        double referenceQ3 = refernceDescriptiveStatistics.getPercentile(75);
        double min = refernceDescriptiveStatistics.getMin();
        double max = refernceDescriptiveStatistics.getMax();
        List<Double> refQ1List = new ArrayList<>();
        List<Double> refQ2List = new ArrayList<>();
        List<Double> refQ3List = new ArrayList<>();
        List<Double> refQ4List = new ArrayList<>();
        for (double d : refernce) {
            if (d < referenceQ1) {
                refQ1List.add(d);
            } else if (d >= referenceQ1 && d < referenceMedian) {
                refQ2List.add(d);
            } else if (d >= referenceMedian && d < referenceQ3) {
                refQ3List.add(d);
            } else {
                refQ4List.add(d);
            }
        }
        // evaluate test based on quartile reference
        List<Double> testQ1List = new ArrayList<>();
        List<Double> testQ2List = new ArrayList<>();
        List<Double> testQ3List = new ArrayList<>();
        List<Double> testQ4List = new ArrayList<>();
        for (double d : test) {
            if (d < referenceQ1) {
                testQ1List.add(d);
            } else if (d >= referenceQ1 && d < referenceMedian) {
                testQ2List.add(d);
            } else if (d >= referenceMedian && d < referenceQ3) {
                testQ3List.add(d);
            } else {
                testQ4List.add(d);
            }
        }

        double scoreQ1 = testQ1List.size() - refQ1List.size();
        double scoreQ2 = (testQ2List.size() - refQ2List.size());
        double scoreQ3 = (testQ3List.size() - refQ3List.size());
        double scoreQ4 = testQ4List.size() - refQ4List.size();
        double finalScore = 0;
        if (potintialFP && false) {
            System.out.print(" Q1 test " + scoreQ1);
            if (!isSegnificantDifferent(refQ1List, testQ1List)) {
                scoreQ1 = 0;
            }
            System.out.print(" Q2 test " + scoreQ2);
            if (!isSegnificantDifferent(refQ2List, testQ2List)) {
                scoreQ2 = 0;
            }
            System.out.print(" Q3 test " + scoreQ3);
            if (!isSegnificantDifferent(refQ3List, testQ3List)) {
                scoreQ3 = 0;
            }
            System.out.print(" Q4 test " + scoreQ4);
            if (!isSegnificantDifferent(refQ4List, testQ4List)) {
                scoreQ4 = 0;
            }

            finalScore = scoreQ4 + scoreQ3 - (scoreQ2 + scoreQ1);
            System.out.println("diffrent in length is  " + (test.length - refernce.length) + "   " + scoreQ4 + "  " + scoreQ3 + "   " + ((scoreQ2 + scoreQ1)));

        } else {
            scoreQ1 = scoreQ1 * 0.5;
//            System.out.print(" Q2 test " + scoreQ2);
//            if (!isSegnificantDifferent(refQ2List, testQ2List)) {
//                scoreQ2 = 0;
//            }
//            System.out.print(" Q3 test " + scoreQ3);
//            if (!isSegnificantDifferent(refQ3List, testQ3List)) {
//                scoreQ3 = 0;
//            }
//            System.out.print(" Q4 test " + scoreQ4);
//            if (!isSegnificantDifferent(refQ4List, testQ4List)) {
//                scoreQ4 = 0;
//            }

            scoreQ2 = scoreQ2 * 1.0;
            scoreQ3 = scoreQ3 * 1.5;
            scoreQ3 = scoreQ4 * 2.0;
            finalScore = scoreQ4 + scoreQ3 + scoreQ2 + scoreQ1;
        }
////check if data comparable 
//        double rightSideScore = 0;
//        double[] referenceoverData = refOverAvg.stream().mapToDouble(Double::doubleValue).toArray();
//        double[] testOverData = testOverAvg.stream().mapToDouble(Double::doubleValue).toArray();
//        double zScore1 = StatisticsTests.unPairedZTEst(referenceoverData, testOverData);
//        double pValue1 = StatisticsTests.calculatePValue(zScore1);
//       
//
//        double leftSideScore = 0;
//        double[] referenceunderData = refUnderAvg.stream().mapToDouble(Double::doubleValue).toArray();
//        double[] testUnderData = testUnderAvg.stream().mapToDouble(Double::doubleValue).toArray();
//        double zScore2 = StatisticsTests.unPairedZTEst(referenceunderData, testUnderData);
//        double pValue2 = StatisticsTests.calculatePValue(zScore2);
////        if (pValue2 <= 0.05) {
//        leftSideScore = score2under - score1under;
////        }
//        double centerScore = score2 - score1;

        if (finalScore == 0 && !potintialFP) {
            finalScore = test.length - refernce.length;
        }
        System.out.println("final refer value " + finalScore + "   " + refQ1List.size() + " " + refQ2List.size() + " " + refQ3List.size() + " " + refQ4List.size());
        System.out.println("final test  value " + finalScore + "   " + testQ1List.size() + " " + testQ2List.size() + " " + testQ3List.size() + " " + testQ4List.size());
        System.out.println("final score value " + flip + "   " + scoreQ1 + " " + scoreQ2 + " " + scoreQ3 + " " + scoreQ4);
        if (flip) {
            finalScore *= -1.0;
        }
        System.out.println("score to submit " + finalScore);
        return finalScore;
    }

    private static int mapScoreToQuartil(double score, double q1, double median, double q3) {

        if (score < q1) {
            return 1;
        } else if (score >= q1 && score <= median) {
            return 2;
        } else if (score > median && score <= q3) {
            return 3;
        } else {
            return 4;
        }

    }

    public static double compareReferenceToTest(double[] referenceData, double[] testData, Map<String, Double> sharedReferenceData, Map<String, Double> uniqueReferenceData, Map<String, Double> sharedTestData, Map<String, Double> uniqueTestData, boolean potintialFP, boolean fdrApplied) {
        double finalScore = 0;
//            potintialFP = false;
        if (potintialFP && (referenceData.length * 1.05 > testData.length)) {
            return testData.length - (referenceData.length * 1.05);
        }

        DescriptiveStatistics refernceDescriptiveStatistics = new DescriptiveStatistics(referenceData);

        double referenceQ1 = refernceDescriptiveStatistics.getPercentile(25);
        double referenceMedian = refernceDescriptiveStatistics.getPercentile(50);
        double referenceQ3 = refernceDescriptiveStatistics.getPercentile(75);
        int sharedDataScore = 0;
        for (String sharedKey : sharedTestData.keySet()) {
            double refScore = sharedReferenceData.get(sharedKey);
            int beforeCat = mapScoreToQuartil(refScore, referenceQ1, referenceMedian, referenceQ3);
            double testScore = sharedTestData.get(sharedKey);
            int afterCat = mapScoreToQuartil(testScore, referenceQ1, referenceMedian, referenceQ3);
            if ((afterCat - beforeCat > 1)) {
                sharedDataScore += (afterCat - beforeCat);
            }
        }
        double lostScoreData = 0;
        for (String uniqueReferenceKey : uniqueReferenceData.keySet()) {
            double refScore = uniqueReferenceData.get(uniqueReferenceKey);
            int scoreRank = mapScoreToQuartil(refScore, referenceQ1, referenceMedian, referenceQ3);
            if (scoreRank > 2) {
                lostScoreData -= scoreRank;
            }
        }
        double gainedScoreData = 0;
        for (String uniqueTestKey : uniqueTestData.keySet()) {
            double testScore = uniqueTestData.get(uniqueTestKey);
            int scoreRank = mapScoreToQuartil(testScore, referenceQ1, referenceMedian, referenceQ3);
            if (scoreRank > 1) {
                gainedScoreData += scoreRank;
            }
        }

        finalScore = sharedDataScore + gainedScoreData + lostScoreData;
        double updatedScore = testData.length - (referenceData.length);

//            finalScore=finalScore/Math.max(updatedScore,1);
//        System.out.println("------------------->>>" + finalScore + " VS " + updatedScore + " ---score shared " + sharedDataScore + "  reference lost " + lostScoreData + "   test gained " + gainedScoreData + "  ###  " + referenceData.length + " vs  " + testData.length);
        if (potintialFP && finalScore == 0) {
            return -1;
        }
        if ((finalScore > 0 && updatedScore == 0) || (finalScore < 0 && updatedScore == 0)) {
            System.out.println("here is the case ");
          
        }

        return finalScore;
    }

    public static boolean isSegnificantDifferent(List<Double> reference, List<Double> test) {
        double[] referenceData = reference.stream().mapToDouble(Double::doubleValue).toArray();
        double[] testData = test.stream().mapToDouble(Double::doubleValue).toArray();
        double zScore = StatisticsTests.unPairedZTEst(referenceData, testData);
        double pValue = StatisticsTests.calculatePValue(zScore);
        System.out.println(" z score " + zScore + " pvale " + pValue + "   " + (pValue <= 0.05));
        return pValue <= 0.05;
    }

    public static boolean isSegnificantDifferent(double[] referenceData, double[] testData) {

        double zScore = StatisticsTests.unPairedZTEst(referenceData, testData);
        double pValue = StatisticsTests.calculatePValue(zScore);
        System.out.println(" z score " + zScore + " pvale " + pValue + "   " + (pValue <= 0.05));
        return pValue <= 0.05;
    }

    public static double compareIndependentDataEffect(double minPairedValue, double[] independentSample) {
        TreeSet<Double> underAvg = new TreeSet<>();
        TreeSet<Double> overAvg = new TreeSet<>();

        for (double d : independentSample) {
            if (d < minPairedValue) {
                underAvg.add(d);
            }
            if (d >= minPairedValue) {
                overAvg.add(d);
            }
        }
        double[] underAvgData = underAvg.stream().mapToDouble(Double::doubleValue).toArray();
        double[] overAvgData = overAvg.stream().mapToDouble(Double::doubleValue).toArray();

        return 0;
    }

    public static boolean isSamplesComparable(double[] sample1, double[] sample2) {
        if (sample1.length < 2 || sample2.length < 2) {
            return false;
        }
        double sizeDiff = Math.abs(sample1.length - sample2.length);
        double sizeDiffPerc = sizeDiff * 100.0 / (double) Math.max(sample1.length, sample2.length);

        if (sizeDiffPerc < 5.0) {

            return true;
        }

        // Perform Welch's t-test
        TTest tTest = new TTest();
        double pValueWelch = tTest.tTest(sample1, sample2);

        // Perform Levene's test (using OneWayAnova as a proxy)
        OneWayAnova anova = new OneWayAnova();
        double pValueLevene = anova.anovaPValue(Arrays.asList(sample1, sample2));

//         Output the results
        System.out.println("Welch's t-test P-value: " + pValueWelch);
        System.out.println("Levene's test P-value: " + pValueLevene);
        // Determine if the samples are comparable
        double alpha = 0.05;
        return !(pValueWelch < alpha && pValueLevene < alpha);
    }

    public static double[] quartileAnalysis(DescriptiveStatistics sampeStat) {
        double[] sample = sampeStat.getSortedValues();
        // Calculate quartiles
        double q1 = sampeStat.getPercentile(25);
        double q2 = sampeStat.getPercentile(50);
        double q3 = sampeStat.getPercentile(75);

        // Divide data into quartiles
        double[] quartile1 = Arrays.copyOfRange(sample, 0, sample.length / 4);
        double[] quartile2 = Arrays.copyOfRange(sample, sample.length / 4, sample.length / 2);
        double[] quartile3 = Arrays.copyOfRange(sample, sample.length / 2, 3 * sample.length / 4);
        double[] quartile4 = Arrays.copyOfRange(sample, 3 * sample.length / 4, sample.length);

        // Calculate mean of each quartile
        double meanQ1 = StatisticsTests.calculateMean(quartile1);
        double meanQ2 = StatisticsTests.calculateMean(quartile2);
        double meanQ3 = StatisticsTests.calculateMean(quartile3);
        double meanQ4 = StatisticsTests.calculateMean(quartile4);

//         double meanQ5 =  StatisticsTests.calculateMean(quartileAvg);
        // Output the results
        System.out.println("Q1 (First Quartile): " + q1 + ", Mean: " + meanQ1);
        System.out.println("Q2 (Second Quartile): " + q2 + ", Mean: " + meanQ2);
        System.out.println("Q3 (Third Quartile): " + q3 + ", Mean: " + meanQ3);
        System.out.println("Q4 (Fourth Quartile): Mean: " + meanQ4);

        // Calculate weights based on mean contribution
        double totalMean = meanQ1 + meanQ2 + meanQ3 + meanQ4;
        double weightQ1 = meanQ1 / totalMean;
        double weightQ2 = meanQ2 / totalMean;
        double weightQ3 = meanQ3 / totalMean;
        double weightQ4 = meanQ4 / totalMean;

        // Output the weights
        System.out.println("Weight of Q1: " + weightQ1 + "  #  " + quartile1.length);
        System.out.println("Weight of Q2: " + weightQ2 + "  #  " + quartile2.length);
        System.out.println("Weight of Q3: " + weightQ3 + "  #  " + quartile3.length);
        System.out.println("Weight of Q4: " + weightQ4 + "  #  " + quartile4.length);
        return new double[]{weightQ1, weightQ2, weightQ3, weightQ4};
    }

    public static double[] quartileAnalysis(DescriptiveStatistics sampeStat, double q1, double q2, double q3) {
        double[] sample = sampeStat.getSortedValues();
        // Calculate quartiles
        List<Double> Q1Data = new ArrayList<>();
        List<Double> Q2Data = new ArrayList<>();
        List<Double> Q3Data = new ArrayList<>();
        List<Double> Q4Data = new ArrayList<>();

        for (double d : sample) {
            if (d < q1) {
                Q1Data.add(d);
            } else if (d >= q1 && d <= q2) {
                Q2Data.add(d);
            } else if (d > q2 && d <= q3) {
                Q3Data.add(d);
            } else {
                Q4Data.add(d);
            }
        }

        // Divide data into quartiles
        double[] quartile1 = Q1Data.stream().mapToDouble(Double::doubleValue).toArray();
        double[] quartile2 = Q2Data.stream().mapToDouble(Double::doubleValue).toArray();
        double[] quartile3 = Q3Data.stream().mapToDouble(Double::doubleValue).toArray();
        double[] quartile4 = Q4Data.stream().mapToDouble(Double::doubleValue).toArray();

        // Calculate mean of each quartile
        double meanQ1 = StatisticsTests.calculateMean(quartile1);
        double meanQ2 = StatisticsTests.calculateMean(quartile2);
        double meanQ3 = StatisticsTests.calculateMean(quartile3);
        double meanQ4 = StatisticsTests.calculateMean(quartile4);

//         double meanQ5 =  StatisticsTests.calculateMean(quartileAvg);
        // Output the results
        System.out.println("Q1 (First Quartile): " + q1 + ", Mean: " + meanQ1);
        System.out.println("Q2 (Second Quartile): " + q2 + ", Mean: " + meanQ2);
        System.out.println("Q3 (Third Quartile): " + q3 + ", Mean: " + meanQ3);
        System.out.println("Q4 (Fourth Quartile): Mean: " + meanQ4);

        // Calculate weights based on mean contribution
        double totalMean = meanQ1 + meanQ2 + meanQ3 + meanQ4;
        double weightQ1 = meanQ1 / totalMean;
        double weightQ2 = meanQ2 / totalMean;
        double weightQ3 = meanQ3 / totalMean;
        double weightQ4 = meanQ4 / totalMean;

        // Output the weights
        System.out.println("Weight of Q1: " + weightQ1 + "  #  " + quartile1.length);
        System.out.println("Weight of Q2: " + weightQ2 + "  #  " + quartile2.length);
        System.out.println("Weight of Q3: " + weightQ3 + "  #  " + quartile3.length);
        System.out.println("Weight of Q4: " + weightQ4 + "  #  " + quartile4.length);
        return new double[]{weightQ1, weightQ2, weightQ3, weightQ4};
    }

}
