/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package no.uib.probe.quicksearchprot.model;

import com.compomics.util.experiment.identification.matches.SpectrumMatch;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author yfa041
 */
public class RawScoreModel implements Comparable<RawScoreModel> {

    private int idPSMNumber = 0;
    private double tTestStat;
    private double pValue;
    private boolean sameData;
    private double s1;
    private double s2;
    private final String parameterId;
    private int sharedDataSize = 0;
     private int diffrentInSize = 0;

    public int getDiffrentInSize() {
        return diffrentInSize;
    }

    public void setDiffrentInSize(int diffrentInSize) {
        this.diffrentInSize = diffrentInSize;
    }

    public RawScoreModel(String comparisonId) {
        this.parameterId = comparisonId;
    }

    public boolean isSameData() {
        return sameData;
    }

    public void setSameData(boolean sameData) {
        this.sameData = sameData;
        if (sameData) {
            s1 = 0;
            s2 = 0;
            finalScore = 0;
            this.setSensitiveChange(false);
        }

    }

    public void setSensitiveChange(boolean sensitiveChange) {
        this.sensitiveChange = sensitiveChange;
    }
    private boolean acceptedChange;
    private boolean sensitiveChange;

    public boolean isSensitiveChange() {
        return sensitiveChange;
    }
    private List<SpectrumMatch> spectrumMatchResult = new ArrayList<>();
    private double improvmentScore;
    private double finalScore;
    private double rawFinalScore;

    public double getRawFinalScore() {
        return rawFinalScore;
    }
    private double sizeEffect;
    private double dataLengthFactor;

    public double getSizeEffect() {
        return sizeEffect;
    }

    public void setSizeEffect(double sizeEffect) {
        this.sizeEffect = Math.round(sizeEffect * 100.0) / 100.0;
    }

    public double getFinalScore() {
        return finalScore;
    }

    public void setFinalScore(double finalScore) {
        this.rawFinalScore = finalScore;
        this.finalScore = Math.round(finalScore * 1000.0) / 1000.0;
//        MainUtilities.fullScoreSet.add( this.finalScore);
    }
    private Set<String> specTitles;

    public Set<String> getSpecTitles() {
        if (specTitles == null && spectrumMatchResult != null) {
            specTitles = new HashSet<>();
            for (SpectrumMatch sm : spectrumMatchResult) {
                specTitles.add(sm.getSpectrumTitle());
            }
        }
        return specTitles;
    }

    public double getImprovmentScore() {
        return improvmentScore;
    }

    public void setImprovmentScore(double improvmentScore) {
        this.improvmentScore = improvmentScore;
    }

    public List<SpectrumMatch> getSpectrumMatchResult() {
        return spectrumMatchResult;
    }

    public void setSpectrumMatchResult(List<SpectrumMatch> spectrumMatchResult) {
        this.spectrumMatchResult = spectrumMatchResult;
        this.idPSMNumber = spectrumMatchResult.size();
        this.specTitles = null;
    }

    public int getIdPSMNumber() {
        return idPSMNumber;
    }

    public void setIdPSMNumber(int idPSMNumber) {
        this.idPSMNumber = idPSMNumber;
    }

    public double gettTestStat() {
        return tTestStat;
    }

    public void settTestStat(double tTestStat) {
        this.tTestStat = tTestStat;
    }

    public double getpValue() {
        return pValue;
    }

    public void setpValue(double pValue) {
        this.pValue = pValue;
    }

    public boolean isAcceptedChange() {
        return acceptedChange;
    }

    public void setAcceptedChange(boolean acceptedChange) {
        this.acceptedChange = acceptedChange;
        if (acceptedChange) {
            sensitiveChange = true;
        }

    }
    private boolean potintialFalsePostive;

    public boolean isPotintialFalsePostive() {
        return potintialFalsePostive;
    }

    public void setPotintialFalsePostive(boolean potintialFalsePostive) {
        this.potintialFalsePostive = potintialFalsePostive;
    }

    @Override
    public int compareTo(RawScoreModel rs) {
        if (potintialFalsePostive) {
            return Double.compare(idPSMNumber, rs.idPSMNumber);
        }
        if (finalScore == rs.finalScore) {
            return Double.compare(idPSMNumber, rs.idPSMNumber);
        }
//        double compscore = SpectraUtilities.isBetterScore(spectrumMatchResult, rs.spectrumMatchResult, fullSpectraSize);
//        if (compscore == 0) {
//            return 0;
//        } else if (compscore > 0) {
//            return 1;
//        } else {
//            return -1;
//        }
        return Double.compare(rawFinalScore, rs.getRawFinalScore());

    }

    @Override
    public String toString() {
        return "Param accepted: " + acceptedChange + "  final score: " + finalScore + " shared data size: " + sharedDataSize + "  #Spectra: " + idPSMNumber + "  senstive improvment " + sensitiveChange + "  same data " + sameData + "   S1: " + s1 + "   S2:" + s2;
    }

    public double getDataLengthFactor() {
        return dataLengthFactor;
    }

    public String getParameterId() {
        return parameterId;
    }

    public void setDataLengthFactor(double dataLengthFactor) {
        this.dataLengthFactor = dataLengthFactor;
    }

    public double getS1() {
        return s1;
    }

    public void setS1(double s1) {
        this.s1 = s1;
    }

    public double getS2() {

        return s2;
    }

    public void setS2(double s2) {
        if (s1 == 0 && s2 != 0) {
//            System.out.println("------------------------------------------------------>>>>> filtering parameter applied here " + parameterId + "  " + s2);
        }
        this.s2 = s2;
    }

    public int getSharedDataSize() {
        return sharedDataSize;
    }

    public void setSharedDataSize(int sharedDataSize) {
        this.sharedDataSize = sharedDataSize;
    }

}
