/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package no.uib.probe.quicksearchprot.model;

/**
 *
 * @author Yehia
 */
public class ResultScoreModel {

    private double pvalue;
    private double zScore;
    private double percentage;
    private String percentageVis = "";
    private String paramName;
    private String paramValue;
    private boolean defaultParameterValue;

    public double getPvalue() {
        return pvalue;
    }

    public void setPvalue(double pvalue) {
        this.pvalue = pvalue;
    }

    public double getzScore() {
        return zScore;
    }

    public void setzScore(double zScore) {
        this.zScore = zScore;
    }

    public String getPercentage() {
        return percentageVis;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
//        for (int i = 0; i < percentage;) {
//            percentageVis += "I"; 
//            i+=5;
//        }

    }

    public String getParamName() {
        return paramName;
    }

    public void setParamName(String paramName) {
        this.paramName = paramName;
    }

    public String getParamValue() {
        return paramValue;
    }

    public void setParamValue(String paramValue) {
        this.paramValue = paramValue;
    }

    @Override
    public String toString() {
        String conf = "";

//        if (pvalue <= 0.05) {
//            conf = "";
//        } else 
        if (isDefaultParameterValue()) {
            conf = "Default";
        }
//            else {
//            conf = "";
//        }
        return ("\t"+((int)percentage) + " %\t" + conf + "");
    }

    public boolean isDefaultParameterValue() {
        return defaultParameterValue;
    }

    public void setDefaultParameterValue(boolean defaultParameterValue) {
        this.defaultParameterValue = defaultParameterValue;
    }

}
