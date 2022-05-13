/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package weka.gui.explorer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import javax.swing.JProgressBar;
import weka.attributeSelection.ASEvaluation;
import weka.attributeSelection.ASSearch;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Instances;
import weka.filters.Filter;
import weka.gui.Logger;
import weka.gui.SysErrLog;

/**
 *
 * @author manum
 */
public class FeatureClassifierAndValidations implements Callable<ResultsExp> {

    /*private Logger log = new SysErrLog();
    private final List inst;
    private final int testMode;
    private final int numFolds;
    private final int seed; 
    private final int classIndex;
    private final double percent;
    private final Filter filter;
    private final Classifier classifier;
    private final JProgressBar progressBar;
    private final int percentThreads;
    private final boolean selectedPreserveOrder;
     */
    private final Instances datasets;
    private final JProgressBar progressBar;
    private final int numThreads;
    private final Classifier classifier;
    private final int percent;
    private final int eleccion;
    private final int percent2;
    private final int fechaFrom;
    private final int fechaTo;
    private final int trainingIncrement;
    private final int eleccionTrainingIncrement;

    /*
     public FeatureClassifierAndValidations(Logger m_Log, List inst, int testMode, int numFolds, int seed, int classIndex, 
            double percent, Filter filter,Classifier classifier, 
            JProgressBar progressBar, int pThreads, boolean selectedPreserveOrder) throws Exception {
        this.log = m_Log;
        this.inst = new ArrayList(inst);
        this.testMode = testMode;
        this.numFolds = numFolds;
        this.filter = filter;
        this.seed = seed;
        this.classIndex = classIndex;
        this.percent = percent;
        this.classifier = AbstractClassifier.makeCopy(classifier);
        this.progressBar = progressBar;
        this.percentThreads = pThreads;
        this.selectedPreserveOrder = selectedPreserveOrder;
        this.string = string;
    }
     */
    public FeatureClassifierAndValidations(Instances datasets, JProgressBar progressBar, int numThreads, Logger m_Log, Classifier classifier, int percent, int eleccion, int percent2, int fechaFrom, int fechaTo, int trainingIncrement, int eleccionTrainingIncrement) throws Exception {
        this.datasets = datasets;
        this.progressBar = progressBar;
        this.numThreads = numThreads;
        this.classifier = AbstractClassifier.makeCopy(classifier);
        this.percent = percent;
        this.eleccion = eleccion;
        this.percent2 = percent2;
        this.fechaFrom = fechaFrom;
        this.fechaTo = fechaTo;
        this.trainingIncrement = trainingIncrement;
        this.eleccionTrainingIncrement = trainingIncrement;
    }

    public ResultsExp run() throws Exception {
        // Collection c = null;
        System.out.println("Entra en el RESULTS EXP A TOPE");
        Instances train = null, newTrain, test = null, newTest = null;

        Classifier cls = null;

        try {
            cls = AbstractClassifier.makeCopy(classifier);
            System.out.println("Pasa el cls");
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(FeatureClassifierAndValidations.class.getName()).log(Level.SEVERE, null, ex);
        }

        Evaluation eval = null;
        int numAttr = 0;
        int trainSize = 0;
        int testSize = 0;

        if (eleccion == 0) {
            if(eleccionTrainingIncrement == 0){
            trainSize = percent;
            testSize = Math.abs(datasets.numInstances() - trainSize);
            train = new Instances(datasets, 0, trainSize);
            test = new Instances(datasets, percent, testSize);
            System.out.println("ELECCION ABSOLUTA");
            }
            else{
            trainSize = percent;
            testSize = trainingIncrement;
            train = new Instances(datasets, 0, trainSize);
            test = new Instances(datasets, percent, testSize);
            System.out.println("ELECCION ABSOLUTA CON TRAINING INCREMENET");
            System.out.println("TRAIN SIZE--> "+trainSize);
            }

        } else if (eleccion == 1) {
            trainSize = (int) Math.round(datasets.numInstances() * percent2 / 100);
            System.out.println("trainSize--> " + trainSize);
            testSize = datasets.numInstances() - trainSize;
            train = new Instances(datasets, 0, trainSize);
            test = new Instances(datasets, trainSize, testSize);
            System.out.println("ELECCION RELATIVA");
        } else {
            testSize = Math.abs(datasets.numInstances() - fechaTo);
            train = new Instances(datasets, fechaFrom, fechaTo);
            test = new Instances(datasets, fechaTo, testSize);
            System.out.println("ELECCION FECHA");

        }

        newTrain = train;
        newTest = test;
        System.out.println("Antes del build");
        cls.buildClassifier(newTrain);
        System.out.println("Despues del build");
        eval = new Evaluation(newTrain);
        eval.evaluateModel(cls, newTest);

        numAttr = newTest.numAttributes();

        ResultsExp result = new ResultsExp(eval, newTest, datasets, classifier, numAttr);

        synchronized (progressBar) {
            progressBar.setValue(progressBar.getValue() + numThreads);
        }
        System.out.println("Termina el resultsExp");
        return result;

    }

    @Override
    public ResultsExp call() throws Exception {
        ResultsExp res = this.run();

        return res;
    }
}
