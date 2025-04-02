
package no.uib.probe.quicksearchprot.arc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.inference.TTest;

public class ScoreComparison1 {

    private double calculateMean(double[] scores) {
        double sum = 0;
        for (double score : scores) {
            sum += score;
        }
        return sum / scores.length;
    }

    private double percentageImprovement(double mean1, double mean2) {
        return ((mean2 - mean1) / mean1) * 100.0;
    }

    public double calculateStandardDeviation(double[] scores) {
        double mean = calculateMean(scores);
        double sum = 0;
        for (double score : scores) {
            sum += Math.pow(score - mean, 2);
        }
        return Math.sqrt(sum / scores.length);
    }

    public double performTStatTest(double[] s1, double[] s2, boolean switchData) {
        double[] scores1, scores2;
        if (switchData) {
            scores1 = s2;
            scores2 = s1;
        } else {
            scores1 = s1;
            scores2 = s2;
        }

        if (scores1.length < 2 || scores2.length < 2) {
            if (scores1.length > scores2.length) {
                return -1;
            } else {
                return 1;
            }
        }
        TTest tTest = new TTest();
        if (scores1.length == scores2.length) {
            return tTest.pairedT(scores2, scores1);
        } else {
            return tTest.t(scores2, scores1);
        }
    }

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
     * @param data The array of data to be normalized.
     * @param base The base of the logarithm to use.
     * @return The log scale normalized array of data.
     */
    public static double logScaleNormalize(double data, double base) {
        double sign = 1;
        if (data < 0) {
            sign = -1;
        }
        data = Math.abs(data);
        double normalizedData = (Math.log(data + 1) / Math.log(base)) * sign;
        if (Double.isNaN(normalizedData)) {
            normalizedData = 0;
        }
        return normalizedData;
    }

    public double[] calculateScore(double[] scores1, double[] scores2) {
        if (scores1.length < 2 && scores2.length < 2) {
            return new double[]{0, 0, 0, 0, 0, 0};
        }

        DescriptiveStatistics ds1;
        DescriptiveStatistics ds2;
        boolean switchData = false;
        List<Double> valuesToScore = new ArrayList<>();
        if (scores2.length >= scores1.length) {
            ds1 = new DescriptiveStatistics(scores1);
            ds2 = new DescriptiveStatistics(scores2);
        } else {
            switchData = true;
            ds1 = new DescriptiveStatistics(scores2);
            ds2 = new DescriptiveStatistics(scores1);
        }

        double mean1 = ds1.getMean();
        double mean2 = ds2.getMean();
        double percentageImprovement = percentageImprovement(mean1, mean2);
        if (scores1.length == scores2.length && mean1 == mean2 && percentageImprovement == 0) {
            return new double[]{0, 0, 0, 0, 0, 0};
        }
        double tStat = performTStatTest(scores1, scores2, switchData);
        double pValue = performTTest(scores1, scores2, switchData);
        double normTStat = logScaleNormalize(tStat, Math.E);
        double normPValue = logScaleNormalize((1.0 - pValue), Math.E); // Inverting p-value for interpretation
        if (tStat < 0) {
            normPValue *= -1.0;
        }

        double stdDev1 = ds1.getStandardDeviation();//calculateStandardDeviation(scores1);
        double stdDev2 = ds2.getStandardDeviation();//calculateStandardDeviation(scores2);

        double stdDevDifference = stdDev1 - stdDev2;
        double normStdDevDiff = logScaleNormalize(stdDevDifference, Math.E);
        // Assuming the possible range for these metrics is between -200 and 200 for normalization purposes.
        // Adjust these ranges based on your specific use case.
        double normPercentageImprovement = logScaleNormalize(percentageImprovement, Math.E);
        if (Double.isInfinite(Math.abs(normTStat))) {
            normTStat = normPercentageImprovement;
        }
        if (ds1.getN() > 1 && ds2.getN() > 1) {
            valuesToScore.add(normStdDevDiff);
            valuesToScore.add(normTStat);
            valuesToScore.add(normPValue);
            valuesToScore.add(normPercentageImprovement);
        } else {
            ds1.addValue(0);
            double compScore = oneToManyScore(ds1.getElement(0), ds2);
            double normScore = logScaleNormalize(compScore, Math.E);
            valuesToScore.add(normScore);

        }

        double dataSizeEffect = 0;
        if (scores1.length != scores2.length) {
            dataSizeEffect = calculateCohensD(scores1, scores2, switchData);//
            if (Double.isNaN(dataSizeEffect) || Double.isInfinite(dataSizeEffect)) {
                dataSizeEffect = dataSizeEffect(scores1.length, scores2.length);
            }
            valuesToScore.add(dataSizeEffect);

        }

        double finalScore = 0;
        for (double ss : valuesToScore) {
            finalScore += ss;
        }
        finalScore = finalScore / (double) valuesToScore.size();
        if (switchData) {
            finalScore *= -1.0;
        }

        return new double[]{percentageImprovement, tStat, pValue, dataSizeEffect, finalScore, 0};
    }
   
    public double[] calculateFinalScore(Double[] scores1, Double[] scores2) {
        double[] s1 = new double[scores1.length];
        for (int i = 0; i < s1.length; i++) {
            s1[i] = scores1[i];
        }
        double[] s2 = new double[scores2.length];
        for (int i = 0; i < s2.length; i++) {
            s2[i] = scores2[i];
        }
        return this.calculateScore(s1, s2);
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
        return logScaleNormalize(normCohenD, Math.E);
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

    public static void main(String[] args) {
        double scores1 = 91.0;// {80.0, 88.0, 94.0, 86.0, 92.5, 89.0};
        double[] scores2 = {91.0, 89.0, 95.0, 87.0, 93.5, 90.0, 89.0, 95.0, 87.0, 93.5, 90.0, 89.0, 95.0, 87.0, 93.5, 90.0};

        ScoreComparison1 sc = new ScoreComparison1();
        double finalScore = sc.oneToManyScore(scores1, new DescriptiveStatistics(scores2));

        System.out.println("Final Score: " + finalScore + "  ");

//         Interpretation
        if (finalScore > 0.75) {
            System.out.println("Significant positive enhancement in scores.");
        } else if (finalScore > 0.5) {
            System.out.println("Moderate positive enhancement in scores.");
        } else if (finalScore > 0.25) {
            System.out.println("Slight positive enhancement in scores.");
        } else {
            System.out.println("No significant enhancement or decline in scores.");
        }

    }

}
