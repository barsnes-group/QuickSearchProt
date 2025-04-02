/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package no.uib.probe.quicksearchprot.model;

/**
 *
 * @author yfa041
 */
public class PSM implements Comparable<PSM> {

    private double score;
    private boolean isTarget;

   public  PSM(double score, boolean isTarget) {
        this.score = score;
        this.isTarget = isTarget;
    }

    @Override
    public int compareTo(PSM other) {
        return Double.compare(other.score, this.score);
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public boolean isIsTarget() {
        return isTarget;
    }

    public void setIsTarget(boolean isTarget) {
        this.isTarget = isTarget;
    }
}
