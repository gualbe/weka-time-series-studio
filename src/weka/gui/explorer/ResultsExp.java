/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package weka.gui.explorer;

import java.util.List;
import weka.attributeSelection.ASEvaluation;
import weka.attributeSelection.ASSearch;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Instances;
import weka.filters.Filter;

/**
 *
 * @author manum
 */
public class ResultsExp {
    
    private Evaluation evalClassifier;
    private Instances test;
    private Instances datasets;
    
   // private final Filter filter;
    private final Classifier classifier;
    private final int numAttr;
    //private final String feature;

    public ResultsExp(Evaluation evalClassifier, Instances test, Instances datasets, /*Filter filter*/Classifier classifier, int numAttr){
        this.evalClassifier = evalClassifier;
        this.test = test;
        this.datasets = datasets;
        //this.filter = filter;
        this.classifier = classifier;
        this.numAttr = numAttr;
    }

    public Evaluation getEvalClassifier() {
        return evalClassifier;
    }

    public List getTest() {
        return test;
    }
    
    public List getDatasets() {
        return datasets;
    }

    //public Filter getFilter() {
    //    return filter;
    //}


    public Classifier getClassifier() {
        return classifier;
    }
    
    public int getNumAttr(){
        return numAttr;
    }
    
}
