/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package no.uib.probe.quicksearchprot.model;

/**
 *
 * @author yfa041
 */
public class ParameterScoreModel implements Comparable<ParameterScoreModel> {

    private String paramId;
    private String paramValue;
    private int paramIdentAdditive;
    private double delayTime;
    private Double score;
    private double impact;
    private String comments;
    private long start;
    private long end;
    private RawScoreModel rawScoreModel;

    public ParameterScoreModel() {
        start = System.currentTimeMillis();
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getParamId() {
        return paramId;
    }

    public void setParamId(String paramId) {
        this.paramId = paramId;
    }

    public String getParamValue() {
        return paramValue;
    }

    public void setParamValue(String paramValue) {
        this.paramValue = paramValue;
    }

    public int getParamIdentAdditive() {
        return paramIdentAdditive;
    }

    public void setParamIdentAdditive(int paramIdentAdditive) {
        this.paramIdentAdditive = paramIdentAdditive;
    }

    public double getDelayTime() {
        return delayTime;
    }

    public void setDelayTime(double delayTime) {
        this.delayTime = delayTime;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
        this.end = System.currentTimeMillis();
        this.setDelayTime((end - start) / 1000.0);
    }

    @Override
    public int compareTo(ParameterScoreModel o) {
        return this.score.compareTo(o.getScore());
    }

    @Override
    public String toString() {
        return "Parameter: " + paramId + "  value: " + paramValue + "  delay:" + delayTime + " sec  score: " + score + "  impact: " + impact+" %";
    }

    public RawScoreModel getRawScoreModel() {
        return rawScoreModel;
    }

    public void setRawScoreModel(RawScoreModel rawScoreModel) {
        this.rawScoreModel = rawScoreModel;
    }

    public double getImpact() {
        return impact;
    }

    public void setImpact(double impact) {
        this.impact = impact*100.0;
    }

}
