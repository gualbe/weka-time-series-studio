/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package weka.gui.explorer;

import com.orsonpdf.PDFDocument;
import com.orsonpdf.PDFGraphics2D;
import com.orsonpdf.Page;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import weka.filters.unsupervised.attribute.Add;
import com.toedter.calendar.JDateChooser;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.data.time.Day;
import org.jfree.data.time.Hour;
import org.jfree.data.time.Quarter;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimeSeriesDataItem;
import org.jfree.data.time.Year;

import weka.attributeSelection.ASEvaluation;
import weka.attributeSelection.ASSearch;
import weka.attributeSelection.AttributeEvaluator;
import weka.attributeSelection.Ranker;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.evaluation.Prediction;
import weka.classifiers.pmml.consumer.PMMLClassifier;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.CapabilitiesHandler;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.OptionHandler;
import weka.core.Range;
import weka.core.SerializationHelper;
import weka.core.Utils;
import weka.core.converters.AbstractFileLoader;
import weka.core.converters.CSVSaver;
import weka.core.converters.ConverterUtils;
import weka.filters.Filter;
import weka.gui.ConverterFileChooser;
import weka.gui.GenericObjectEditor;
import weka.core.converters.Loader;
import weka.core.pmml.PMMLFactory;
import weka.core.pmml.PMMLModel;
import weka.gui.ExtensionFileFilter;
import weka.gui.Logger;
import weka.gui.PropertyDialog;
import weka.gui.SysErrLog;
import weka.gui.PropertyPanel;
import weka.gui.WekaFileChooser;
import static weka.gui.explorer.ClassifierPanel.MODEL_FILE_EXTENSION;
import static weka.gui.explorer.ClassifierPanel.PMML_FILE_EXTENSION;
import static weka.gui.explorer.ExplorerDefaults.get;
import weka.tstudio.CustomFeature;
import weka.tstudio.MaxMin;
import weka.tstudio.Mdelta;
import weka.tstudio.MeanAndCount;
import weka.tstudio.Suma;
import weka.tstudio.TimeSerie;

/**
 *
 * @author manum
 */
public class tsStudio extends javax.swing.JPanel implements Explorer.ExplorerPanel, Explorer.LogHandler {

    String nombreProyecto;

    int contadorGuardarClassifier = 0;
    String calculated = "NONE";
    protected FileFilter m_PMMLModelFilter = new ExtensionFileFilter(
            PMML_FILE_EXTENSION, "PMML model files");

    protected FileFilter m_ModelFilter = new ExtensionFileFilter(
            MODEL_FILE_EXTENSION, "Model object files");

    int eleccion;

    int eleccionTrainingIncrement;

    JFreeChart chart;

    Instances instPredictions;

    List<List<List<List>>> maeGeneral = new ArrayList();
    List<List<List<List<List>>>> maeGeneralTrainingIncrement = new ArrayList();
    List<List<List<List>>> rmseGeneral = new ArrayList();
    List<List<List<List<List>>>> rmseGeneralTrainingIncrement = new ArrayList();

    List<List<List<List>>> r2General = new ArrayList();
    List<List<List<List<List>>>> r2GeneralTrainingIncrement = new ArrayList();

    List<List<String>> classifiersPrintGeneral = new ArrayList();

    List<List<List<List<List<Prediction>>>>> valuesPredictPlot = new ArrayList();
    List<List<List<List<List<List<Prediction>>>>>> valuesPredictPlotTrainingIncrement = new ArrayList();

    List<List<String>> fechasPrepositionalDatasets = new ArrayList<List<String>>();
    List<Instances> prepositionalDatasets = new ArrayList();

    LinkedList<Future<ResultsExp>> resultsExp;
    List<Instances> prepositionalInstances = new ArrayList();
    List<Object> functions = new ArrayList();
    protected List<Future<ResultsExp>> res;
    // Parametros Weka
    protected Explorer m_Explorer;
    protected weka.filters.Sourcable m_filters;
    protected Instances m_Instances;
    protected weka.gui.Logger m_log = new SysErrLog();
    List<String> nombreCustomFeature = new ArrayList();
    // Parametros para abrir desde fichero
    protected ConverterFileChooser file_chooser;
    protected JFileChooser chooser = new JFileChooser();

    // Parametros filtros
    protected GenericObjectEditor m_FilterEditor = new GenericObjectEditor();
    protected PropertyPanel m_FilterPanel = new PropertyPanel(m_FilterEditor);
    protected FileNameExtensionFilter filter;

    //Parametros algoritmos
    protected GenericObjectEditor m_ClassifierEditor = new GenericObjectEditor();
    protected PropertyPanel m_CEPanel = new PropertyPanel(m_ClassifierEditor);
    protected GenericObjectEditor savedClassifier = new GenericObjectEditor();
    //Click to apply filters and save the results
    protected JButton m_ApplyFilterBut = new JButton("Apply");
    // Click to stop a running filter
    protected JButton m_StopBut = new JButton("Stop");
    /**
     * Click to start running the classifier.
     */
    protected JButton m_StartBut = new JButton("Start");

    /**
     * Click to add the classifier to table.
     */
    protected JButton add_Algo = new JButton("Add");

    // Modelos de tablas y listas
    protected DefaultTableModel datasetTableModel;
    protected DefaultTableModel tablaTargetModel;
    protected DefaultTableModel tablaFeatureModel;
    protected DefaultTableModel tablaAlgoritmosModel;
    protected DefaultTableModel predictionTableModel;
    protected DefaultTableModel overallMetricsTableModel;
    protected DefaultTableModel metricsByHorizonTableModel;

    protected DefaultListModel modeloLista;
    protected DefaultListModel modeloInputVaribales;

    protected WekaFileChooser m_FileChooser = new WekaFileChooser(new File(
            System.getProperty("user.dir")));
    protected Thread m_IOThread;

    // Parametros para atributos
    protected String[] attributeNames;
    protected String atNames[][] = new String[100][100];
    protected Attribute attribute[][];
    protected GenericObjectEditor m_AttributeEvaluatorEditor;
    protected ExecutorService executor;

    // Listas
    protected List<Instances> listInstances;
    protected List<Object> listEvaluators;
    protected List<Object> listSearchAlgorithms;
    protected List<Object> listClassifier;
    protected List attEquals;
    protected List<Attribute> targets;
    protected List<Integer> horizon;
    protected List<Attribute> atributosFeatures;
    protected ArrayList<Attribute> equalAttributes;
    protected List<List<List>> trainDataset = new ArrayList<List<List>>();
    protected List<String> listFeatures = new ArrayList<String>();
    //Parametros int
    protected int contadorDataset = 0;
    protected int mHorizon;
    protected String fff;
    private JDateChooser dateChooserFrom;
    private JDateChooser dateChooserTo;
    //private JCalendar calendar;
    //Clases de los features
    MaxMin s = new MaxMin();
    CustomFeature cf = new CustomFeature();
    MeanAndCount me = new MeanAndCount();
    Suma sum = new Suma();
    Mdelta mdelta = new Mdelta();

    SwingWorker worker;
    LinkedList<List> datasets = new LinkedList<List>();

// Instanciar Componente
    public tsStudio() {
        initComponents();
        dateChooserFrom = new JDateChooser();
        dateChooserFrom.setDateFormatString("yyyy-MM-dd");
        dateChooserTo = new JDateChooser();
        dateChooserFrom.setDateFormatString("yyyy-MM-dd");
        jPanel2.add(dateChooserFrom, BorderLayout.CENTER);
        jPanel3.add(dateChooserTo, BorderLayout.CENTER);
        m_AttributeEvaluatorEditor = new GenericObjectEditor();

        //Modelos tabla
        datasetTableModel = (DefaultTableModel) datasetTable.getModel();
        tablaTargetModel = (DefaultTableModel) tablaTarget.getModel();
        tablaFeatureModel = (DefaultTableModel) tablaFeatures.getModel();
        tablaAlgoritmosModel = (DefaultTableModel) tablaAlgo.getModel();
        predictionTableModel = (DefaultTableModel) predictionTable.getModel();
        overallMetricsTableModel = (DefaultTableModel) overallMetricsTable.getModel();
        metricsByHorizonTableModel = (DefaultTableModel) metricsByHorizonTable.getModel();

        //Inicializacion de listas
        listInstances = new ArrayList<>();
        attEquals = new ArrayList();
        targets = new ArrayList<Attribute>();
        atributosFeatures = new ArrayList<Attribute>();
        horizon = new ArrayList<Integer>();
        equalAttributes = new ArrayList<Attribute>();
        listClassifier = new ArrayList();

        //Paneles (Vista)
        usedDatasetPanel.setBorder(BorderFactory.createTitledBorder("Datasets to be used"));
        datasetConfigurationPanel.setBorder(BorderFactory.createTitledBorder("Dataset configuration"));
        panelLagged.setBorder(BorderFactory.createTitledBorder("Lagged"));
        customFeaturePanel.setBorder(BorderFactory.createTitledBorder("Custom Feature"));
        jPanel1.setBorder(BorderFactory.createTitledBorder("Features to be used"));
        //panelAlgoritmos.setBorder(BorderFactory.createTitledBorder("Select Algorithms"));
        algoToBeUsedPanel.setBorder(BorderFactory.createTitledBorder("Algorithms to be used"));
        formulaPanel.setBorder(BorderFactory.createTitledBorder("Add to formula"));
        initialTrainingPanel.setBorder(BorderFactory.createTitledBorder("Initial training size:"));
        trainingVariationsPanel.setBorder(BorderFactory.createTitledBorder("Training variations:"));
        slidingWindowPanel.setBorder(BorderFactory.createTitledBorder("Sliding window"));
        runExperimentPanel.setBorder(BorderFactory.createTitledBorder("Run current experiment"));
        predictionTablePanel.setBorder(BorderFactory.createTitledBorder("Prediction table"));
        overallMetricsPanel.setBorder(BorderFactory.createTitledBorder("Overall metrics"));
        metricsByHorizonPanel.setBorder(BorderFactory.createTitledBorder("Metrics by Horizon"));
        forecastPlotPanel.setBorder(BorderFactory.createTitledBorder("Forecast Plot"));
        experimentPanel.setBorder(BorderFactory.createTitledBorder("Current experiment"));

        //Modelo de listas
        modeloLista = new DefaultListModel();
        modeloInputVaribales = new DefaultListModel();
        listVariFeatures.setModel(modeloInputVaribales);

        mHorizon = Integer.parseInt(textFieldTimeSeries.getText().trim());

        add_Algo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addAlgo(evt);
            }
        });

        //TOOLTIPS
        createButton.setToolTipText("Crea un experimento nuevo");
        loadExperimentButton.setToolTipText("Carga un experimento");
        saveExperimentButton.setToolTipText("Guarda el experimento actual");
        addPreprocess.setToolTipText("Carga el dataset cargado en la pestaña preprocess de Weka");
        addFile.setToolTipText("Carga un dataset desde un archivo local");
        addFolder.setToolTipText("Carga una lista de datasets desde un directorio o carpeta");
        selectAllButton.setToolTipText("Selecciona todos los datasets de la tabla");
        removeButton.setToolTipText("Elimina el dataset seleccionado");
        addInputVariablesButton.setToolTipText("Añade las variables comunes a la lista de input variables");
        addTargetButton.setToolTipText("Añade las variables comunes a la tabla target");
        selectAllInputButton.setToolTipText("Selecciona todas las input variables de la lista");
        removeInputButton.setToolTipText("Elimina la input variable seleccionada");
        selectAllTargetsButton.setToolTipText("Selecciona todos los targets");
        removeTargetsButton.setToolTipText("Elimina el target seleccionado");
        radioButtonMaximum.setToolTipText("Relaciona el salto de la serie temporal con el horizonte maximo");
        radioButtonMinimum.setToolTipText("Relaciona el salto de la serie temporal con el horizonte minimo");
        radioButtonValue.setToolTipText("Relaciona el salto de la serie temporal con el valor introducido");
        timeFormatComboBox.setToolTipText("Formato en el que se mostrará la fecha de las series temporales.\n"
                + "CUIDADO: Para que se muestre la gráfica correctamente, si la fecha tiene también hora, elegir"
                + "algun formato con fecha y hora.");
        
        selectAllVariFeatures.setToolTipText("Selecciona todos los valores de la lista");
        addButtonFeature.setToolTipText("Añade a la tabla de features las funciones lagged configuradas");
        addFunctionButtonFeature.setToolTipText("Añade a la formula la custom feature elegida");
        addCustomFeatureButton.setToolTipText("Añade a la lista la custom feature actual");
        nameTextFeature.setToolTipText("Nombre de la custom feature");
        formulaTextFeature.setToolTipText("Formula de la custom feature");
        selectAllButtonTablaFeatures.setToolTipText("Selecciona todos los valores de la tabla");
        removeSelectButtonTablaFeature.setToolTipText("Elimina el valor seleccionado");
        saveButtonFeature.setToolTipText("Guarda la lista de features en un archivo local en disco");
        loadButtonFeature.setToolTipText("Carga una lista de features de un archivo local en disco");
        
        //Algoritmos
        jButton1.setToolTipText("Elimina el valor seleccionado");
        jButton2.setToolTipText("Selecciona todos los valores de la tabla");
        saveAlgoBtn.setToolTipText("Guarda la lista de algoritmos en un archivo local");
        loadAlgoBtn.setToolTipText("Carga una lista de algoritmos de un archivo local");
        
        //Validations
        absoluteRadioButton.setToolTipText("Modo de validación absoluta");
        relativeRadioButton.setToolTipText("Modo de validación relativa");
        fromToRadioButton.setToolTipText("Modo de validación de rango de fechas. From(desde)|To(hasta)");
        jRadioButton4.setToolTipText("El incremento del training es el valor introducido");
        jRadioButton5.setToolTipText("No hay variaciones en el training");
        
        //Results
        runStopButton.setToolTipText("Comenzar la experimentación");
        numThreadsTextField.setToolTipText("Numero de hilos con los que ejecutar la experimentación");
        predictionTable.setToolTipText("Tabla de predicciones");
        jButton3.setToolTipText("Guarda en formato .csv la tabla de predicciones");
        jButton4.setToolTipText("Guarda en formato .arff la tabla de predicciones");
        jButton5.setToolTipText("Manda la tabla de predicciones a la pestaña 'preprocess' de Weka");
        overallMetricsTable.setToolTipText("Tabla de metricas generales");
        saveCsvOverallMetrics.setToolTipText("Guarda en formato .csv la tabla de metricas generales");
        saveArffOverallMetrics.setToolTipText("Guarda en formato .arff la tabla de metricas generales");
        jButton8.setToolTipText("Guarda en formato .latex la tabla de metricas generales");
        jButton9.setToolTipText("Manda la tabla de metricas generales a la pestaña 'preprocess' de Weka");
        metricsByHorizonTable.setToolTipText("Tabla de metricas por horizonte");
        jButton10.setToolTipText("Guarda en formato .csv la tabla de metricas por horizonte");
        jButton11.setToolTipText("Guarda en formato .arff la tabla de metricas por horizonte");
        jButton12.setToolTipText("Guarda en formato .latex la tabla de metricas por horizonte");
        jButton13.setToolTipText("Manda la tabla de metricas por horizonte a la pestaña 'preprocess' de Weka");
        
        

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        grupoTimeSeries = new javax.swing.ButtonGroup();
        buttonGroupTrainingIncrements = new javax.swing.ButtonGroup();
        buttonGroupTrainingSize = new javax.swing.ButtonGroup();
        tfgTabbedPane = new javax.swing.JTabbedPane();
        tabExp = new javax.swing.JPanel();
        newLabel = new javax.swing.JLabel();
        createButton = new javax.swing.JButton();
        loadExperimentButton = new javax.swing.JButton();
        newTextField = new javax.swing.JTextField();
        experimentPanel = new javax.swing.JPanel();
        experimentLabel = new javax.swing.JLabel();
        saveExperimentButton = new javax.swing.JButton();
        tabData = new javax.swing.JPanel();
        usedDatasetPanel = new javax.swing.JPanel();
        addPreprocess = new javax.swing.JButton();
        addFile = new javax.swing.JButton();
        addFolder = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        datasetTable = new javax.swing.JTable();
        selectAllButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        filterSelectedLabel = new javax.swing.JLabel();
        filterPanel = new javax.swing.JPanel();
        datasetConfigurationPanel = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        timeFieldComboBox = new javax.swing.JComboBox<>();
        jLabel6 = new javax.swing.JLabel();
        timeFormatComboBox = new javax.swing.JComboBox<>();
        jLabel7 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        listVariables = new javax.swing.JList<>();
        addInputVariablesButton = new javax.swing.JButton();
        addTargetButton = new javax.swing.JButton();
        jLabel9 = new javax.swing.JLabel();
        jScrollPane4 = new javax.swing.JScrollPane();
        tablaTarget = new javax.swing.JTable();
        selectAllTargetsButton = new javax.swing.JButton();
        removeTargetsButton = new javax.swing.JButton();
        jLabel10 = new javax.swing.JLabel();
        radioButtonValue = new javax.swing.JRadioButton();
        radioButtonMinimum = new javax.swing.JRadioButton();
        radioButtonMaximum = new javax.swing.JRadioButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        listaInputVariables = new javax.swing.JList<>();
        selectAllInputButton = new javax.swing.JButton();
        removeInputButton = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        textFieldTimeSeries = new javax.swing.JTextField();
        jLabel21 = new javax.swing.JLabel();
        jLayeredPane1 = new javax.swing.JLayeredPane();
        saveDataConfigurationButton = new javax.swing.JButton();
        loadDatasetConfiguration = new javax.swing.JButton();
        tabFea = new javax.swing.JPanel();
        panelLagged = new javax.swing.JPanel();
        variablesLabel = new javax.swing.JLabel();
        jScrollPane6 = new javax.swing.JScrollPane();
        listVariFeatures = new javax.swing.JList<>();
        sizeLabelFeature = new javax.swing.JLabel();
        selectAllVariFeatures = new javax.swing.JButton();
        addButtonFeature = new javax.swing.JButton();
        sizeTextFieldFeature = new javax.swing.JTextField();
        customFeaturePanel = new javax.swing.JPanel();
        nameLabelFeature = new javax.swing.JLabel();
        nameTextFeature = new javax.swing.JTextField();
        formulaLabelFeature = new javax.swing.JLabel();
        formulaTextFeature = new javax.swing.JTextField();
        formulaPanel = new javax.swing.JPanel();
        variableLabelFeature = new javax.swing.JLabel();
        variableComboFeature = new javax.swing.JComboBox<>();
        functionLabelFeature = new javax.swing.JLabel();
        functionComboFeature = new javax.swing.JComboBox<>();
        variableLabelFunctionFeature = new javax.swing.JLabel();
        variableComboFunctionFeature = new javax.swing.JComboBox<>();
        functionCambiaLabelFeature = new javax.swing.JLabel();
        shiftTextFeature = new javax.swing.JTextField();
        shiftTextFeature1 = new javax.swing.JTextField();
        addFunctionButtonFeature = new javax.swing.JButton();
        addVariableButtonFeature = new javax.swing.JButton();
        addCustomFeatureButton = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane5 = new javax.swing.JScrollPane();
        tablaFeatures = new javax.swing.JTable();
        selectAllButtonTablaFeatures = new javax.swing.JButton();
        removeSelectButtonTablaFeature = new javax.swing.JButton();
        saveButtonFeature = new javax.swing.JButton();
        loadButtonFeature = new javax.swing.JButton();
        tabAlgo = new javax.swing.JPanel();
        panelAlgoritmos = new javax.swing.JPanel();
        algoToBeUsedPanel = new javax.swing.JPanel();
        jScrollPane7 = new javax.swing.JScrollPane();
        tablaAlgo = new javax.swing.JTable();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        saveAlgoBtn = new javax.swing.JButton();
        loadAlgoBtn = new javax.swing.JButton();
        tabVal = new javax.swing.JPanel();
        slidingWindowPanel = new javax.swing.JPanel();
        initialTrainingPanel = new javax.swing.JPanel();
        absoluteRadioButton = new javax.swing.JRadioButton();
        relativeRadioButton = new javax.swing.JRadioButton();
        fromToRadioButton = new javax.swing.JRadioButton();
        fromTSOriginCheckBox = new javax.swing.JCheckBox();
        absoluteTextField = new javax.swing.JTextField();
        relativeTextField = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        trainingVariationsPanel = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jRadioButton4 = new javax.swing.JRadioButton();
        jRadioButton5 = new javax.swing.JRadioButton();
        jCheckBox2 = new javax.swing.JCheckBox();
        trainingIncrementTextField = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        tabRe = new javax.swing.JPanel();
        runExperimentPanel = new javax.swing.JPanel();
        runStopButton = new javax.swing.JToggleButton();
        progressExp = new javax.swing.JProgressBar();
        jLabel14 = new javax.swing.JLabel();
        numThreadsTextField = new javax.swing.JTextField();
        predictionTablePanel = new javax.swing.JPanel();
        jScrollPane8 = new javax.swing.JScrollPane();
        predictionTable = new javax.swing.JTable();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        overallMetricsPanel = new javax.swing.JPanel();
        jScrollPane9 = new javax.swing.JScrollPane();
        overallMetricsTable = new javax.swing.JTable();
        saveCsvOverallMetrics = new javax.swing.JButton();
        saveArffOverallMetrics = new javax.swing.JButton();
        jButton8 = new javax.swing.JButton();
        jButton9 = new javax.swing.JButton();
        metricsByHorizonPanel = new javax.swing.JPanel();
        datasetComboBox = new javax.swing.JComboBox<>();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        targetComboBox = new javax.swing.JComboBox<>();
        jLabel17 = new javax.swing.JLabel();
        metricsComboBox = new javax.swing.JComboBox<>();
        jScrollPane10 = new javax.swing.JScrollPane();
        metricsByHorizonTable = new javax.swing.JTable();
        jButton10 = new javax.swing.JButton();
        jButton11 = new javax.swing.JButton();
        jButton12 = new javax.swing.JButton();
        jButton13 = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        targetPlotComboBox = new javax.swing.JComboBox<>();
        targetPlotLabel = new javax.swing.JLabel();
        horizonPlotLabel = new javax.swing.JLabel();
        horizonPlotComboBox = new javax.swing.JComboBox<>();
        datasetPlotLabel = new javax.swing.JLabel();
        datasetPlotComboBox = new javax.swing.JComboBox<>();
        algorithmPlotLabel = new javax.swing.JLabel();
        algorithmPlotComboBox = new javax.swing.JComboBox<>();
        jButton6 = new javax.swing.JButton();
        jButton7 = new javax.swing.JButton();
        forecastPlotPanel = new javax.swing.JPanel();
        jButton14 = new javax.swing.JButton();

        setLayout(new java.awt.BorderLayout());

        tfgTabbedPane.setMinimumSize(new java.awt.Dimension(1010, 407));

        newLabel.setText("New:");

        createButton.setText("Create");
        createButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createButtonActionPerformed(evt);
            }
        });

        loadExperimentButton.setText("Load");
        loadExperimentButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadExperimentButtonActionPerformed(evt);
            }
        });

        newTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newTextFieldActionPerformed(evt);
            }
        });

        saveExperimentButton.setText("Save");
        saveExperimentButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveExperimentButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout experimentPanelLayout = new javax.swing.GroupLayout(experimentPanel);
        experimentPanel.setLayout(experimentPanelLayout);
        experimentPanelLayout.setHorizontalGroup(
            experimentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(experimentPanelLayout.createSequentialGroup()
                .addGroup(experimentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(experimentPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(experimentLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(experimentPanelLayout.createSequentialGroup()
                        .addGap(66, 66, 66)
                        .addComponent(saveExperimentButton)))
                .addContainerGap(19, Short.MAX_VALUE))
        );
        experimentPanelLayout.setVerticalGroup(
            experimentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(experimentPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(experimentLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(saveExperimentButton)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout tabExpLayout = new javax.swing.GroupLayout(tabExp);
        tabExp.setLayout(tabExpLayout);
        tabExpLayout.setHorizontalGroup(
            tabExpLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tabExpLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(tabExpLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(tabExpLayout.createSequentialGroup()
                        .addComponent(newLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(newTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(createButton))
                    .addComponent(loadExperimentButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 512, Short.MAX_VALUE)
                .addComponent(experimentPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(591, 591, 591))
        );
        tabExpLayout.setVerticalGroup(
            tabExpLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tabExpLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(tabExpLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(tabExpLayout.createSequentialGroup()
                        .addGroup(tabExpLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(newLabel)
                            .addComponent(newTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(createButton))
                        .addGap(18, 18, 18)
                        .addComponent(loadExperimentButton))
                    .addComponent(experimentPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(2334, Short.MAX_VALUE))
        );

        tfgTabbedPane.addTab("Experiments", tabExp);

        usedDatasetPanel.setLayout(new java.awt.GridBagLayout());

        addPreprocess.setText("Add from Preprocess");
        addPreprocess.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addPreprocessActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        usedDatasetPanel.add(addPreprocess, gridBagConstraints);

        addFile.setText("Add from file...");
        addFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addFileActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        usedDatasetPanel.add(addFile, gridBagConstraints);

        addFolder.setText("Add from folder...");
        addFolder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addFolderActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        usedDatasetPanel.add(addFolder, gridBagConstraints);

        datasetTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Name", "Instances", "Variables"
            }
        ));
        datasetTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        datasetTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                datasetTableMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(datasetTable);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        usedDatasetPanel.add(jScrollPane2, gridBagConstraints);

        selectAllButton.setText("Select all");
        selectAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectAllButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        usedDatasetPanel.add(selectAllButton, gridBagConstraints);

        removeButton.setText("Remove");
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        usedDatasetPanel.add(removeButton, gridBagConstraints);

        filterSelectedLabel.setText("Filter selected:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        usedDatasetPanel.add(filterSelectedLabel, gridBagConstraints);

        filterPanel.setLayout(new java.awt.BorderLayout());
        filterPanel();
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 1.0;
        usedDatasetPanel.add(filterPanel, gridBagConstraints);

        datasetConfigurationPanel.setLayout(new java.awt.GridBagLayout());

        jLabel3.setText("Time field: ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        datasetConfigurationPanel.add(jLabel3, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        datasetConfigurationPanel.add(timeFieldComboBox, gridBagConstraints);

        jLabel6.setText("Time format: ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        datasetConfigurationPanel.add(jLabel6, gridBagConstraints);

        timeFormatComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "yyyy-MM-dd", "dd/MM/yyyy", "yyyy-MM", "hh:mm", "yyyy-MM-dd HH:mm", "yyyy-MM-dd HH:mm:ss", "dd/MM/yyyy HH:mm:ss" }));
        timeFormatComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                timeFormatComboBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 9;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        datasetConfigurationPanel.add(timeFormatComboBox, gridBagConstraints);

        jLabel7.setText("Variables ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
        datasetConfigurationPanel.add(jLabel7, gridBagConstraints);

        jScrollPane1.setViewportView(listVariables);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 1.0;
        datasetConfigurationPanel.add(jScrollPane1, gridBagConstraints);

        addInputVariablesButton.setText("Add>>");
        addInputVariablesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addInputVariablesButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        datasetConfigurationPanel.add(addInputVariablesButton, gridBagConstraints);

        addTargetButton.setText("Add to target>>");
        addTargetButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addTargetButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        datasetConfigurationPanel.add(addTargetButton, gridBagConstraints);

        jLabel9.setText("Targets");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
        datasetConfigurationPanel.add(jLabel9, gridBagConstraints);

        tablaTarget.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Target", "Horizon"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jScrollPane4.setViewportView(tablaTarget);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.weighty = 1.0;
        datasetConfigurationPanel.add(jScrollPane4, gridBagConstraints);

        selectAllTargetsButton.setText("Select all");
        selectAllTargetsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectAllTargetsButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 9;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        datasetConfigurationPanel.add(selectAllTargetsButton, gridBagConstraints);

        removeTargetsButton.setText("Remove");
        removeTargetsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeTargetsButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 9;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        datasetConfigurationPanel.add(removeTargetsButton, gridBagConstraints);

        jLabel10.setText("Time series scan step: ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        datasetConfigurationPanel.add(jLabel10, gridBagConstraints);

        grupoTimeSeries.add(radioButtonValue);
        radioButtonValue.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                radioButtonValueMouseClicked(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        datasetConfigurationPanel.add(radioButtonValue, gridBagConstraints);

        grupoTimeSeries.add(radioButtonMinimum);
        radioButtonMinimum.setText("Same as minimum horizon");
        radioButtonMinimum.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                radioButtonMinimumMouseClicked(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        datasetConfigurationPanel.add(radioButtonMinimum, gridBagConstraints);

        grupoTimeSeries.add(radioButtonMaximum);
        radioButtonMaximum.setText("Same as maximum horizon");
        radioButtonMaximum.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                radioButtonMaximumMouseClicked(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 13;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        datasetConfigurationPanel.add(radioButtonMaximum, gridBagConstraints);

        jScrollPane3.setViewportView(listaInputVariables);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        datasetConfigurationPanel.add(jScrollPane3, gridBagConstraints);

        selectAllInputButton.setText("Select all");
        selectAllInputButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectAllInputButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 9;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        datasetConfigurationPanel.add(selectAllInputButton, gridBagConstraints);

        removeInputButton.setText("Remove");
        removeInputButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeInputButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 9;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        datasetConfigurationPanel.add(removeInputButton, gridBagConstraints);

        jLabel4.setText("Input variables");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = 7;
        datasetConfigurationPanel.add(jLabel4, gridBagConstraints);

        jLabel20.setText("Every");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 23, 0, 23);
        datasetConfigurationPanel.add(jLabel20, gridBagConstraints);

        textFieldTimeSeries.setText("1");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.ipadx = 34;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 55, 0, 55);
        datasetConfigurationPanel.add(textFieldTimeSeries, gridBagConstraints);

        jLabel21.setText("value/s");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.insets = new java.awt.Insets(0, 98, 0, 98);
        datasetConfigurationPanel.add(jLabel21, gridBagConstraints);

        javax.swing.GroupLayout jLayeredPane1Layout = new javax.swing.GroupLayout(jLayeredPane1);
        jLayeredPane1.setLayout(jLayeredPane1Layout);
        jLayeredPane1Layout.setHorizontalGroup(
            jLayeredPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jLayeredPane1Layout.setVerticalGroup(
            jLayeredPane1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        datasetConfigurationPanel.add(jLayeredPane1, new java.awt.GridBagConstraints());

        saveDataConfigurationButton.setText("Save Configuration...");
        saveDataConfigurationButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveDataConfigurationButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 9;
        gridBagConstraints.gridy = 12;
        datasetConfigurationPanel.add(saveDataConfigurationButton, gridBagConstraints);

        loadDatasetConfiguration.setText("Load Configuration...");
        loadDatasetConfiguration.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadDatasetConfigurationActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 9;
        gridBagConstraints.gridy = 13;
        datasetConfigurationPanel.add(loadDatasetConfiguration, gridBagConstraints);

        javax.swing.GroupLayout tabDataLayout = new javax.swing.GroupLayout(tabData);
        tabData.setLayout(tabDataLayout);
        tabDataLayout.setHorizontalGroup(
            tabDataLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tabDataLayout.createSequentialGroup()
                .addGroup(tabDataLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(usedDatasetPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 1472, Short.MAX_VALUE)
                    .addComponent(datasetConfigurationPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 1472, Short.MAX_VALUE))
                .addContainerGap())
        );
        tabDataLayout.setVerticalGroup(
            tabDataLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tabDataLayout.createSequentialGroup()
                .addComponent(usedDatasetPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 236, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(datasetConfigurationPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 357, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        tfgTabbedPane.addTab("Datasets", tabData);

        variablesLabel.setText("Variables");

        jScrollPane6.setViewportView(listVariFeatures);

        sizeLabelFeature.setText("Size:");

        selectAllVariFeatures.setText("Select all");
        selectAllVariFeatures.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectAllVariFeaturesActionPerformed(evt);
            }
        });

        addButtonFeature.setText("Add");
        addButtonFeature.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonFeatureActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelLaggedLayout = new javax.swing.GroupLayout(panelLagged);
        panelLagged.setLayout(panelLaggedLayout);
        panelLaggedLayout.setHorizontalGroup(
            panelLaggedLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelLaggedLayout.createSequentialGroup()
                .addGroup(panelLaggedLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelLaggedLayout.createSequentialGroup()
                        .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(selectAllVariFeatures))
                    .addGroup(panelLaggedLayout.createSequentialGroup()
                        .addGap(25, 25, 25)
                        .addComponent(variablesLabel))
                    .addGroup(panelLaggedLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(sizeLabelFeature)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(sizeTextFieldFeature, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(addButtonFeature)))
                .addGap(0, 0, Short.MAX_VALUE))
        );
        panelLaggedLayout.setVerticalGroup(
            panelLaggedLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelLaggedLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(variablesLabel)
                .addGroup(panelLaggedLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 230, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(selectAllVariFeatures, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(panelLaggedLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(sizeTextFieldFeature, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(addButtonFeature)
                    .addComponent(sizeLabelFeature))
                .addGap(2, 2, 2))
        );

        nameLabelFeature.setText("Name:");

        nameTextFeature.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                nameTextFeatureKeyTyped(evt);
            }
        });

        formulaLabelFeature.setText("Formula:");

        variableLabelFeature.setText("Variable:");

        functionLabelFeature.setText("Function:");

        functionComboFeature.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "shift", "mean", "sum", "count", "max", "min", "sd", "mdelta" }));
        functionComboFeature.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                functionComboFeatureItemStateChanged(evt);
            }
        });

        variableLabelFunctionFeature.setText("Variable:");

        functionCambiaLabelFeature.setText(functionComboFeature.getSelectedItem().toString());

        addFunctionButtonFeature.setText("Add");
        addFunctionButtonFeature.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addFunctionButtonFeatureActionPerformed(evt);
            }
        });

        addVariableButtonFeature.setText("Add");
        addVariableButtonFeature.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addVariableButtonFeatureActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout formulaPanelLayout = new javax.swing.GroupLayout(formulaPanel);
        formulaPanel.setLayout(formulaPanelLayout);
        formulaPanelLayout.setHorizontalGroup(
            formulaPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(formulaPanelLayout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(formulaPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(formulaPanelLayout.createSequentialGroup()
                        .addGroup(formulaPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, formulaPanelLayout.createSequentialGroup()
                                .addComponent(functionLabelFeature)
                                .addGap(18, 18, 18)
                                .addComponent(functionComboFeature, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(formulaPanelLayout.createSequentialGroup()
                                .addComponent(variableLabelFeature)
                                .addGap(18, 18, 18)
                                .addComponent(variableComboFeature, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(42, 42, 42)
                        .addComponent(addVariableButtonFeature))
                    .addGroup(formulaPanelLayout.createSequentialGroup()
                        .addGroup(formulaPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(formulaPanelLayout.createSequentialGroup()
                                .addComponent(variableLabelFunctionFeature)
                                .addGap(18, 18, 18)
                                .addComponent(variableComboFunctionFeature, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(formulaPanelLayout.createSequentialGroup()
                                .addComponent(functionCambiaLabelFeature)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(shiftTextFeature, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(shiftTextFeature1, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(18, 18, 18)
                        .addComponent(addFunctionButtonFeature)))
                .addGap(0, 42, Short.MAX_VALUE))
        );
        formulaPanelLayout.setVerticalGroup(
            formulaPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(formulaPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(formulaPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(variableLabelFeature)
                    .addComponent(variableComboFeature, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(addVariableButtonFeature))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(formulaPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(functionLabelFeature)
                    .addComponent(functionComboFeature, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(formulaPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(formulaPanelLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(formulaPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(variableLabelFunctionFeature)
                            .addComponent(variableComboFunctionFeature, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(formulaPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(functionCambiaLabelFeature)
                            .addComponent(shiftTextFeature, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(shiftTextFeature1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(formulaPanelLayout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(addFunctionButtonFeature)))
                .addGap(0, 39, Short.MAX_VALUE))
        );

        if (functionComboFeature.getSelectedItem() == "shift") {
            shiftTextFeature1.setVisible(false);
        } else {
            shiftTextFeature1.setVisible(true);
        }

        addCustomFeatureButton.setText("Add");
        addCustomFeatureButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addCustomFeatureButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout customFeaturePanelLayout = new javax.swing.GroupLayout(customFeaturePanel);
        customFeaturePanel.setLayout(customFeaturePanelLayout);
        customFeaturePanelLayout.setHorizontalGroup(
            customFeaturePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(customFeaturePanelLayout.createSequentialGroup()
                .addGroup(customFeaturePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(customFeaturePanelLayout.createSequentialGroup()
                        .addComponent(formulaLabelFeature)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(formulaTextFeature))
                    .addGroup(customFeaturePanelLayout.createSequentialGroup()
                        .addGap(117, 117, 117)
                        .addComponent(addCustomFeatureButton)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(customFeaturePanelLayout.createSequentialGroup()
                        .addComponent(nameLabelFeature, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(nameTextFeature)))
                .addContainerGap())
            .addComponent(formulaPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        customFeaturePanelLayout.setVerticalGroup(
            customFeaturePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(customFeaturePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(customFeaturePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(nameLabelFeature)
                    .addComponent(nameTextFeature, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(21, 21, 21)
                .addGroup(customFeaturePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(formulaLabelFeature)
                    .addComponent(formulaTextFeature, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(formulaPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(addCustomFeatureButton)
                .addContainerGap())
        );

        jPanel1.setLayout(new java.awt.GridBagLayout());

        tablaFeatures.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Name", "Description"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, true
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tablaFeatures.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tablaFeaturesMouseClicked(evt);
            }
        });
        jScrollPane5.setViewportView(tablaFeatures);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanel1.add(jScrollPane5, gridBagConstraints);

        selectAllButtonTablaFeatures.setText("Select all");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        jPanel1.add(selectAllButtonTablaFeatures, gridBagConstraints);

        removeSelectButtonTablaFeature.setText("Remove selected");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        jPanel1.add(removeSelectButtonTablaFeature, gridBagConstraints);

        saveButtonFeature.setText("Save...");
        saveButtonFeature.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveButtonFeatureActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        jPanel1.add(saveButtonFeature, gridBagConstraints);

        loadButtonFeature.setText("Load...");
        loadButtonFeature.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadButtonFeatureActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        jPanel1.add(loadButtonFeature, gridBagConstraints);

        javax.swing.GroupLayout tabFeaLayout = new javax.swing.GroupLayout(tabFea);
        tabFea.setLayout(tabFeaLayout);
        tabFeaLayout.setHorizontalGroup(
            tabFeaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tabFeaLayout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 1008, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(1, 474, Short.MAX_VALUE))
            .addGroup(tabFeaLayout.createSequentialGroup()
                .addComponent(panelLagged, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(customFeaturePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(308, 308, 308))
        );
        tabFeaLayout.setVerticalGroup(
            tabFeaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tabFeaLayout.createSequentialGroup()
                .addGroup(tabFeaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(panelLagged, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(customFeaturePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 188, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        tfgTabbedPane.addTab("Features", tabFea);

        tabAlgo.setLayout(new java.awt.GridBagLayout());

        panelAlgoritmos.setLayout(new java.awt.BorderLayout());
        algorithmPanel();

        panelAlgoritmos.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Classifier"),
            BorderFactory.createEmptyBorder(0, 5, 5, 5)));
    panelAlgoritmos.add(m_CEPanel, BorderLayout.CENTER);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 1.0;
    tabAlgo.add(panelAlgoritmos, gridBagConstraints);

    tablaAlgo.setModel(new javax.swing.table.DefaultTableModel(
        new Object [][] {

        },
        new String [] {
            "Algorithms"
        }
    ) {
        boolean[] canEdit = new boolean [] {
            false
        };

        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return canEdit [columnIndex];
        }
    });
    tablaAlgo.addMouseListener(new java.awt.event.MouseAdapter() {
        public void mouseClicked(java.awt.event.MouseEvent evt) {
            tablaAlgoMouseClicked(evt);
        }
    });
    jScrollPane7.setViewportView(tablaAlgo);

    jButton1.setText("Remove");
    jButton1.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            jButton1ActionPerformed(evt);
        }
    });

    jButton2.setText("Select all");
    jButton2.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            jButton2ActionPerformed(evt);
        }
    });

    saveAlgoBtn.setText("Save...");
    saveAlgoBtn.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            saveAlgoBtnActionPerformed(evt);
        }
    });

    loadAlgoBtn.setText("Load...");
    loadAlgoBtn.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            loadAlgoBtnActionPerformed(evt);
        }
    });

    javax.swing.GroupLayout algoToBeUsedPanelLayout = new javax.swing.GroupLayout(algoToBeUsedPanel);
    algoToBeUsedPanel.setLayout(algoToBeUsedPanelLayout);
    algoToBeUsedPanelLayout.setHorizontalGroup(
        algoToBeUsedPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(algoToBeUsedPanelLayout.createSequentialGroup()
            .addComponent(jScrollPane7, javax.swing.GroupLayout.PREFERRED_SIZE, 934, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(algoToBeUsedPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(algoToBeUsedPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton1)
                    .addComponent(jButton2, javax.swing.GroupLayout.Alignment.TRAILING))
                .addComponent(loadAlgoBtn)
                .addComponent(saveAlgoBtn))
            .addContainerGap())
    );
    algoToBeUsedPanelLayout.setVerticalGroup(
        algoToBeUsedPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(algoToBeUsedPanelLayout.createSequentialGroup()
            .addGroup(algoToBeUsedPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                .addGroup(algoToBeUsedPanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jButton2)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addComponent(jButton1)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(saveAlgoBtn)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addComponent(loadAlgoBtn)
                    .addGap(444, 444, 444))
                .addComponent(jScrollPane7, javax.swing.GroupLayout.PREFERRED_SIZE, 886, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGap(0, 1634, Short.MAX_VALUE))
    );

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.weighty = 1.0;
    tabAlgo.add(algoToBeUsedPanel, gridBagConstraints);

    tfgTabbedPane.addTab("Algorithms", tabAlgo);

    buttonGroupTrainingSize.add(absoluteRadioButton);
    absoluteRadioButton.setText("Absolute:");
    absoluteRadioButton.addMouseListener(new java.awt.event.MouseAdapter() {
        public void mouseClicked(java.awt.event.MouseEvent evt) {
            absoluteRadioButtonMouseClicked(evt);
        }
    });

    buttonGroupTrainingSize.add(relativeRadioButton);
    relativeRadioButton.setText("Relative:");
    relativeRadioButton.addMouseListener(new java.awt.event.MouseAdapter() {
        public void mouseClicked(java.awt.event.MouseEvent evt) {
            relativeRadioButtonMouseClicked(evt);
        }
    });

    buttonGroupTrainingSize.add(fromToRadioButton);
    fromToRadioButton.setText("From:");

    fromTSOriginCheckBox.setText("From time series origin");

    jLabel11.setText("instances");

    jLabel12.setText("%");

    jLabel13.setText("To: ");

    jPanel3.setLayout(new java.awt.BorderLayout());

    jPanel2.setLayout(new java.awt.BorderLayout());

    javax.swing.GroupLayout initialTrainingPanelLayout = new javax.swing.GroupLayout(initialTrainingPanel);
    initialTrainingPanel.setLayout(initialTrainingPanelLayout);
    initialTrainingPanelLayout.setHorizontalGroup(
        initialTrainingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(initialTrainingPanelLayout.createSequentialGroup()
            .addGroup(initialTrainingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(initialTrainingPanelLayout.createSequentialGroup()
                    .addComponent(absoluteRadioButton)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(absoluteTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jLabel11)
                    .addGap(0, 0, Short.MAX_VALUE))
                .addGroup(initialTrainingPanelLayout.createSequentialGroup()
                    .addGap(2, 2, 2)
                    .addGroup(initialTrainingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(initialTrainingPanelLayout.createSequentialGroup()
                            .addGap(21, 21, 21)
                            .addComponent(fromTSOriginCheckBox))
                        .addGroup(initialTrainingPanelLayout.createSequentialGroup()
                            .addComponent(relativeRadioButton)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(relativeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jLabel12))
                        .addGroup(initialTrainingPanelLayout.createSequentialGroup()
                            .addComponent(fromToRadioButton, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 20, Short.MAX_VALUE)
                            .addComponent(jLabel13)))
                    .addGap(6, 6, 6)))
            .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addContainerGap())
    );
    initialTrainingPanelLayout.setVerticalGroup(
        initialTrainingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, initialTrainingPanelLayout.createSequentialGroup()
            .addGap(10, 10, 10)
            .addGroup(initialTrainingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(absoluteRadioButton)
                .addComponent(absoluteTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jLabel11))
            .addGap(8, 8, 8)
            .addGroup(initialTrainingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(relativeRadioButton)
                .addComponent(relativeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jLabel12))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(initialTrainingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(initialTrainingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(fromToRadioButton)
                    .addComponent(jLabel13))
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(fromTSOriginCheckBox)
            .addContainerGap(25, Short.MAX_VALUE))
    );

    jLabel5.setText("Training increments:");

    buttonGroupTrainingIncrements.add(jRadioButton4);
    jRadioButton4.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            jRadioButton4ActionPerformed(evt);
        }
    });

    buttonGroupTrainingIncrements.add(jRadioButton5);
    jRadioButton5.setText("No variations(fixed training)");
    jRadioButton5.addMouseListener(new java.awt.event.MouseAdapter() {
        public void mouseClicked(java.awt.event.MouseEvent evt) {
            jRadioButton5MouseClicked(evt);
        }
    });

    jCheckBox2.setText("Fixed training origin");

    jLabel8.setText("instances");

    javax.swing.GroupLayout trainingVariationsPanelLayout = new javax.swing.GroupLayout(trainingVariationsPanel);
    trainingVariationsPanel.setLayout(trainingVariationsPanelLayout);
    trainingVariationsPanelLayout.setHorizontalGroup(
        trainingVariationsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(trainingVariationsPanelLayout.createSequentialGroup()
            .addContainerGap()
            .addGroup(trainingVariationsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jCheckBox2)
                .addComponent(jLabel5)
                .addGroup(trainingVariationsPanelLayout.createSequentialGroup()
                    .addGap(10, 10, 10)
                    .addGroup(trainingVariationsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jRadioButton5)
                        .addGroup(trainingVariationsPanelLayout.createSequentialGroup()
                            .addComponent(jRadioButton4)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(trainingIncrementTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jLabel8)))))
            .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );
    trainingVariationsPanelLayout.setVerticalGroup(
        trainingVariationsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(trainingVariationsPanelLayout.createSequentialGroup()
            .addContainerGap()
            .addComponent(jLabel5)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addGroup(trainingVariationsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jRadioButton4)
                .addGroup(trainingVariationsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(trainingIncrementTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8)))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addComponent(jRadioButton5)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jCheckBox2))
    );

    javax.swing.GroupLayout slidingWindowPanelLayout = new javax.swing.GroupLayout(slidingWindowPanel);
    slidingWindowPanel.setLayout(slidingWindowPanelLayout);
    slidingWindowPanelLayout.setHorizontalGroup(
        slidingWindowPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(slidingWindowPanelLayout.createSequentialGroup()
            .addContainerGap()
            .addGroup(slidingWindowPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(trainingVariationsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(initialTrainingPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
    );
    slidingWindowPanelLayout.setVerticalGroup(
        slidingWindowPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(slidingWindowPanelLayout.createSequentialGroup()
            .addComponent(initialTrainingPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(30, 30, 30)
            .addComponent(trainingVariationsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addContainerGap(81, Short.MAX_VALUE))
    );

    javax.swing.GroupLayout tabValLayout = new javax.swing.GroupLayout(tabVal);
    tabVal.setLayout(tabValLayout);
    tabValLayout.setHorizontalGroup(
        tabValLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(tabValLayout.createSequentialGroup()
            .addGap(5, 5, 5)
            .addComponent(slidingWindowPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addContainerGap(1063, Short.MAX_VALUE))
    );
    tabValLayout.setVerticalGroup(
        tabValLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(tabValLayout.createSequentialGroup()
            .addComponent(slidingWindowPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addContainerGap())
    );

    tfgTabbedPane.addTab("Validations", tabVal);

    runStopButton.setText("Run");
    runStopButton.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            runStopButtonActionPerformed(evt);
        }
    });

    progressExp.setStringPainted(true);

    jLabel14.setText("Num.threads: ");

    javax.swing.GroupLayout runExperimentPanelLayout = new javax.swing.GroupLayout(runExperimentPanel);
    runExperimentPanel.setLayout(runExperimentPanelLayout);
    runExperimentPanelLayout.setHorizontalGroup(
        runExperimentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(runExperimentPanelLayout.createSequentialGroup()
            .addContainerGap()
            .addGroup(runExperimentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(runExperimentPanelLayout.createSequentialGroup()
                    .addGap(10, 10, 10)
                    .addComponent(jLabel14)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(numThreadsTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(runExperimentPanelLayout.createSequentialGroup()
                    .addComponent(runStopButton, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addComponent(progressExp, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
            .addContainerGap(145, Short.MAX_VALUE))
    );
    runExperimentPanelLayout.setVerticalGroup(
        runExperimentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(runExperimentPanelLayout.createSequentialGroup()
            .addContainerGap()
            .addGroup(runExperimentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                .addComponent(runStopButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(progressExp, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addGroup(runExperimentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabel14)
                .addComponent(numThreadsTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );

    numThreadsTextField.setText("4");

    predictionTable.setModel(new javax.swing.table.DefaultTableModel(
        new Object [][] {

        },
        new String [] {
            "Algorithms", "Dataset", "Time", "Target", "Horizon", "Pred", "Actual", "Error"
        }
    ) {
        Class[] types = new Class [] {
            java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Integer.class, java.lang.Double.class, java.lang.Double.class, java.lang.Double.class
        };
        boolean[] canEdit = new boolean [] {
            false, false, false, false, false, false, false, false
        };

        public Class getColumnClass(int columnIndex) {
            return types [columnIndex];
        }

        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return canEdit [columnIndex];
        }
    });
    jScrollPane8.setViewportView(predictionTable);

    jButton3.setText("Save to csv...");
    jButton3.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            jButton3ActionPerformed(evt);
        }
    });

    jButton4.setText("Save to arff...");
    jButton4.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            jButton4ActionPerformed(evt);
        }
    });

    jButton5.setText("Put to preprocess");
    jButton5.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            jButton5ActionPerformed(evt);
        }
    });

    javax.swing.GroupLayout predictionTablePanelLayout = new javax.swing.GroupLayout(predictionTablePanel);
    predictionTablePanel.setLayout(predictionTablePanelLayout);
    predictionTablePanelLayout.setHorizontalGroup(
        predictionTablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(predictionTablePanelLayout.createSequentialGroup()
            .addContainerGap()
            .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 142, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addComponent(jButton5, javax.swing.GroupLayout.DEFAULT_SIZE, 139, Short.MAX_VALUE)
            .addContainerGap())
        .addGroup(predictionTablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(predictionTablePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane8, javax.swing.GroupLayout.DEFAULT_SIZE, 412, Short.MAX_VALUE)
                .addContainerGap()))
    );
    predictionTablePanelLayout.setVerticalGroup(
        predictionTablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(predictionTablePanelLayout.createSequentialGroup()
            .addContainerGap(137, Short.MAX_VALUE)
            .addGroup(predictionTablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jButton3)
                .addComponent(jButton4)
                .addComponent(jButton5))
            .addGap(0, 0, 0))
        .addGroup(predictionTablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, predictionTablePanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jScrollPane8, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(24, Short.MAX_VALUE)))
    );

    overallMetricsTable.setModel(new javax.swing.table.DefaultTableModel(
        new Object [][] {

        },
        new String [] {
            "Algorithms", "Dataset", "MAE", "RMSE", "MAPE", "SMAPE", "R2"
        }
    ) {
        boolean[] canEdit = new boolean [] {
            false, false, false, false, false, true, false
        };

        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return canEdit [columnIndex];
        }
    });
    jScrollPane9.setViewportView(overallMetricsTable);

    saveCsvOverallMetrics.setText("Save to csv...");
    saveCsvOverallMetrics.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            saveCsvOverallMetricsActionPerformed(evt);
        }
    });

    saveArffOverallMetrics.setText("Save to arff...");
    saveArffOverallMetrics.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            saveArffOverallMetricsActionPerformed(evt);
        }
    });

    jButton8.setText("Save to latex...");
    jButton8.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            jButton8ActionPerformed(evt);
        }
    });

    jButton9.setText("Put to preprocess");
    jButton9.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            jButton9ActionPerformed(evt);
        }
    });

    javax.swing.GroupLayout overallMetricsPanelLayout = new javax.swing.GroupLayout(overallMetricsPanel);
    overallMetricsPanel.setLayout(overallMetricsPanelLayout);
    overallMetricsPanelLayout.setHorizontalGroup(
        overallMetricsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(overallMetricsPanelLayout.createSequentialGroup()
            .addContainerGap()
            .addGroup(overallMetricsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jScrollPane9, javax.swing.GroupLayout.DEFAULT_SIZE, 460, Short.MAX_VALUE)
                .addGroup(overallMetricsPanelLayout.createSequentialGroup()
                    .addComponent(saveCsvOverallMetrics)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(saveArffOverallMetrics)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jButton8)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jButton9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGap(4, 4, 4)))
            .addContainerGap())
    );
    overallMetricsPanelLayout.setVerticalGroup(
        overallMetricsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, overallMetricsPanelLayout.createSequentialGroup()
            .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jScrollPane9, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(overallMetricsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(saveCsvOverallMetrics)
                .addComponent(saveArffOverallMetrics)
                .addComponent(jButton8)
                .addComponent(jButton9))
            .addGap(252, 252, 252))
    );

    datasetComboBox.addItemListener(new java.awt.event.ItemListener() {
        public void itemStateChanged(java.awt.event.ItemEvent evt) {
            datasetComboBoxItemStateChanged(evt);
        }
    });

    jLabel15.setText("Dataset:");

    jLabel16.setText("Target:");

    targetComboBox.addItemListener(new java.awt.event.ItemListener() {
        public void itemStateChanged(java.awt.event.ItemEvent evt) {
            targetComboBoxItemStateChanged(evt);
        }
    });
    targetComboBox.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            targetComboBoxActionPerformed(evt);
        }
    });

    jLabel17.setText("Metric:");

    metricsComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "MAE", "RMSE", "R2" }));
    metricsComboBox.addItemListener(new java.awt.event.ItemListener() {
        public void itemStateChanged(java.awt.event.ItemEvent evt) {
            metricsComboBoxItemStateChanged(evt);
        }
    });

    metricsByHorizonTable.setModel(new javax.swing.table.DefaultTableModel(
        new Object [][] {

        },
        new String [] {
            "Algorithms"
        }
    ));
    jScrollPane10.setViewportView(metricsByHorizonTable);

    jButton10.setText("Save to csv...");
    jButton10.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            jButton10ActionPerformed(evt);
        }
    });

    jButton11.setText("Save to arff...");
    jButton11.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            jButton11ActionPerformed(evt);
        }
    });

    jButton12.setText("Save to latex...");
    jButton12.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            jButton12ActionPerformed(evt);
        }
    });

    jButton13.setText("Put to preprocess...");
    jButton13.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            jButton13ActionPerformed(evt);
        }
    });

    javax.swing.GroupLayout metricsByHorizonPanelLayout = new javax.swing.GroupLayout(metricsByHorizonPanel);
    metricsByHorizonPanel.setLayout(metricsByHorizonPanelLayout);
    metricsByHorizonPanelLayout.setHorizontalGroup(
        metricsByHorizonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(metricsByHorizonPanelLayout.createSequentialGroup()
            .addContainerGap()
            .addGroup(metricsByHorizonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(metricsByHorizonPanelLayout.createSequentialGroup()
                    .addComponent(jLabel15)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(datasetComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(18, 18, 18)
                    .addComponent(jLabel16)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(targetComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(18, 18, 18)
                    .addComponent(jLabel17)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(metricsComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGroup(metricsByHorizonPanelLayout.createSequentialGroup()
                    .addGroup(metricsByHorizonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(jScrollPane10, javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(metricsByHorizonPanelLayout.createSequentialGroup()
                            .addComponent(jButton10, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jButton11)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jButton12)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jButton13)))
                    .addGap(0, 11, Short.MAX_VALUE))))
    );
    metricsByHorizonPanelLayout.setVerticalGroup(
        metricsByHorizonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(metricsByHorizonPanelLayout.createSequentialGroup()
            .addContainerGap()
            .addGroup(metricsByHorizonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(datasetComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jLabel15)
                .addComponent(jLabel16)
                .addComponent(targetComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jLabel17)
                .addComponent(metricsComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addComponent(jScrollPane10, javax.swing.GroupLayout.PREFERRED_SIZE, 106, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(metricsByHorizonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jButton10)
                .addComponent(jButton11)
                .addComponent(jButton12)
                .addComponent(jButton13))
            .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );

    targetPlotComboBox.addItemListener(new java.awt.event.ItemListener() {
        public void itemStateChanged(java.awt.event.ItemEvent evt) {
            targetPlotComboBoxItemStateChanged(evt);
        }
    });

    targetPlotLabel.setText("Target:");

    horizonPlotLabel.setText("Horizon:");

    horizonPlotComboBox.addItemListener(new java.awt.event.ItemListener() {
        public void itemStateChanged(java.awt.event.ItemEvent evt) {
            horizonPlotComboBoxItemStateChanged(evt);
        }
    });

    datasetPlotLabel.setText("Dataset:");

    datasetPlotComboBox.addItemListener(new java.awt.event.ItemListener() {
        public void itemStateChanged(java.awt.event.ItemEvent evt) {
            datasetPlotComboBoxItemStateChanged(evt);
        }
    });

    algorithmPlotLabel.setText("Algorithm:");

    algorithmPlotComboBox.addItemListener(new java.awt.event.ItemListener() {
        public void itemStateChanged(java.awt.event.ItemEvent evt) {
            algorithmPlotComboBoxItemStateChanged(evt);
        }
    });

    jButton6.setText("Save to PNG...");
    jButton6.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            jButton6ActionPerformed(evt);
        }
    });

    jButton7.setText("Save to PDF...");
    jButton7.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            jButton7ActionPerformed(evt);
        }
    });

    javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
    jPanel4.setLayout(jPanel4Layout);
    jPanel4Layout.setHorizontalGroup(
        jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(jPanel4Layout.createSequentialGroup()
            .addContainerGap()
            .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel4Layout.createSequentialGroup()
                    .addComponent(targetPlotLabel)
                    .addGap(13, 13, 13)
                    .addComponent(targetPlotComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGroup(jPanel4Layout.createSequentialGroup()
                    .addComponent(jButton6)
                    .addGap(0, 13, Short.MAX_VALUE)))
            .addGap(18, 18, 18)
            .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel4Layout.createSequentialGroup()
                    .addComponent(horizonPlotLabel)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(horizonPlotComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(18, 18, 18)
                    .addComponent(datasetPlotLabel)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(datasetPlotComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(18, 18, 18)
                    .addComponent(algorithmPlotLabel)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addComponent(algorithmPlotComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(jPanel4Layout.createSequentialGroup()
                    .addGap(10, 10, 10)
                    .addComponent(jButton7)))
            .addGap(3, 3, 3))
    );
    jPanel4Layout.setVerticalGroup(
        jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(jPanel4Layout.createSequentialGroup()
            .addContainerGap()
            .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(targetPlotComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(targetPlotLabel)
                .addComponent(horizonPlotLabel)
                .addComponent(horizonPlotComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(datasetPlotLabel)
                .addComponent(datasetPlotComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(algorithmPlotLabel)
                .addComponent(algorithmPlotComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jButton6)
                .addComponent(jButton7))
            .addContainerGap(22, Short.MAX_VALUE))
    );

    javax.swing.GroupLayout forecastPlotPanelLayout = new javax.swing.GroupLayout(forecastPlotPanel);
    forecastPlotPanel.setLayout(forecastPlotPanelLayout);
    forecastPlotPanelLayout.setHorizontalGroup(
        forecastPlotPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGap(0, 391, Short.MAX_VALUE)
    );
    forecastPlotPanelLayout.setVerticalGroup(
        forecastPlotPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGap(0, 194, Short.MAX_VALUE)
    );

    jButton14.setText("Save propositional datasets to ARFF...");
    jButton14.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            jButton14ActionPerformed(evt);
        }
    });

    javax.swing.GroupLayout tabReLayout = new javax.swing.GroupLayout(tabRe);
    tabRe.setLayout(tabReLayout);
    tabReLayout.setHorizontalGroup(
        tabReLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(tabReLayout.createSequentialGroup()
            .addGroup(tabReLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(runExperimentPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(overallMetricsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(predictionTablePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(tabReLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jButton14)))
            .addGroup(tabReLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(tabReLayout.createSequentialGroup()
                    .addGap(48, 48, 48)
                    .addGroup(tabReLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(metricsByHorizonPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGroup(tabReLayout.createSequentialGroup()
                    .addGap(108, 108, 108)
                    .addComponent(forecastPlotPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
            .addContainerGap(467, Short.MAX_VALUE))
    );
    tabReLayout.setVerticalGroup(
        tabReLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(tabReLayout.createSequentialGroup()
            .addGroup(tabReLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(tabReLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(runExperimentPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(predictionTablePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(18, 18, 18)
                    .addComponent(overallMetricsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 188, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jButton14))
                .addGroup(tabReLayout.createSequentialGroup()
                    .addComponent(metricsByHorizonPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(forecastPlotPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
            .addContainerGap(2030, Short.MAX_VALUE))
    );

    forecastPlotPanel.getAccessibleContext().setAccessibleDescription("");

    tfgTabbedPane.addTab("Results", tabRe);

    add(tfgTabbedPane, java.awt.BorderLayout.PAGE_START);
    tfgTabbedPane.addTab("Experiments",tabExp);
    tfgTabbedPane.addTab("Datasets",tabData);
    tfgTabbedPane.addTab("Features",tabFea);
    tfgTabbedPane.addTab("Algorithms",tabAlgo);
    tfgTabbedPane.addTab("Validation",tabVal);
    tfgTabbedPane.addTab("Result",tabRe);
    }// </editor-fold>//GEN-END:initComponents

    private void newTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newTextFieldActionPerformed

    }//GEN-LAST:event_newTextFieldActionPerformed

    private void createButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createButtonActionPerformed
        nombreProyecto = newTextField.getText();
        experimentLabel.setText("<html><p style=\"width:100px\">" + "Name: " + newTextField.getText() + "<br> Datasets: " + listInstances.size() + "<br> Features: " + listFeatures.size() + "<br>  Classifiers: " + listClassifier.size() + "<br>  Validation: Absolute" + "<br> Result: " + calculated + "</p></html>");

    }//GEN-LAST:event_createButtonActionPerformed
    private void addInstancesToDatasetList(Instances dataset, int positiveClass) {
        dataset.setClassIndex(dataset.numAttributes() - 1);

        datasetTableModel.addRow(new Object[]{dataset.relationName(), dataset.numInstances(),
            dataset.numAttributes()});

    }


    private void timeFormatComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_timeFormatComboBoxActionPerformed
    }//GEN-LAST:event_timeFormatComboBoxActionPerformed

    private void radioButtonMinimumMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_radioButtonMinimumMouseClicked
        for (int i = 0; i < tablaTarget.getRowCount(); i++) {
            String a = (String) tablaTarget.getValueAt(i, 1);
            int prueba = Integer.parseInt(a.trim());
            horizon.add(i, prueba);
            System.out.println("Horizon: " + horizon.get(i));
        }
        for (int i = 0; i < horizon.size(); i++) {
            for (int j = i + 1; j < horizon.size(); j++) {
                if (horizon.get(i) > horizon.get(j)) {
                    mHorizon = horizon.get(j);
                } else {
                    mHorizon = horizon.get(i);
                }
            }
        }
    }//GEN-LAST:event_radioButtonMinimumMouseClicked

    private void radioButtonValueMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_radioButtonValueMouseClicked
        textFieldTimeSeries.setEnabled(true);

        mHorizon = Integer.parseInt(textFieldTimeSeries.getText().trim());

    }//GEN-LAST:event_radioButtonValueMouseClicked

    private void radioButtonMaximumMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_radioButtonMaximumMouseClicked
        textFieldTimeSeries.setEnabled(false);
        for (int i = 0; i < tablaTarget.getRowCount(); i++) {
            String a = (String) tablaTarget.getValueAt(i, 1);
            int prueba = Integer.parseInt(a.trim());
            horizon.add(i, prueba);
            System.out.println("Horizon: " + horizon.get(i));
        }
        for (int i = 0; i < horizon.size(); i++) {
            for (int j = i + 1; j < horizon.size(); j++) {
                if (horizon.get(i) > horizon.get(j)) {
                    mHorizon = horizon.get(i);
                } else {
                    mHorizon = horizon.get(j);
                }
            }
        }
        //timeSeries s = new TimeSerie();
        //s.target(listInstances, maxHorizon, horizon,-1 targets);
    }//GEN-LAST:event_radioButtonMaximumMouseClicked

    private void addTargetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addTargetButtonActionPerformed
        tablaTargetModel.addRow(new Object[]{listVariables.getSelectedValue(), 8});
        targets.add(new Attribute(listVariables.getSelectedValue(), (ArrayList<String>) null));


    }//GEN-LAST:event_addTargetButtonActionPerformed

    private void removeTargetsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeTargetsButtonActionPerformed
        if (tablaTarget.getSelectedRow() != -1) {
            targets.remove(tablaTarget.getSelectedRow());
            tablaTargetModel.removeRow(tablaTarget.getSelectedRow());
        } else {
            m_log.logMessage("No row is selected");
            m_log.statusMessage("See erro log");
        }
    }//GEN-LAST:event_removeTargetsButtonActionPerformed

    private void selectAllTargetsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectAllTargetsButtonActionPerformed
        tablaTarget.selectAll();
        String dato = String.valueOf(tablaTargetModel.getValueAt(tablaTarget.getSelectedRow(), 1));
        System.out.println("Horizon: " + dato);
        System.out.println("Probando___ " + tablaTarget.getValueAt(0, 1));

        //timeSeries s = new TimeSerie();
        //s.time(listInstances,2,2, targets);
    }//GEN-LAST:event_selectAllTargetsButtonActionPerformed

    private void removeInputButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeInputButtonActionPerformed
        modeloInputVaribales.removeElement(listaInputVariables.getSelectedValue());
        for (int i = 0; i < modeloInputVaribales.size(); i++) {
            System.out.println("_____ " + modeloInputVaribales.get(i));
        }


    }//GEN-LAST:event_removeInputButtonActionPerformed

    private void addInputVariablesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addInputVariablesButtonActionPerformed
        modeloInputVaribales.addElement(listVariables.getSelectedValue());
        int start = 0;
        int end = listVariFeatures.getModel().getSize() - 1;
        if (end >= 0) {
            listVariFeatures.setSelectionInterval(start, end);
        }

    }//GEN-LAST:event_addInputVariablesButtonActionPerformed

    private void addButtonFeatureActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonFeatureActionPerformed
        /*  for (int i = 0; i < tablaTarget.getRowCount(); i++) {
            String a =  (String) tablaTarget.getValueAt(i, 1);
            int prueba = Integer.parseInt(a.trim());
            horizon.add(i,prueba);
            System.out.println("Horizon: " + horizon.get(i));
        }*/
        int lag = Integer.parseInt(sizeTextFieldFeature.getText().trim());

        List<String> names = new ArrayList<>();
        if (horizon.size() == 1) {
            mHorizon = horizon.get(0);
        }

        names = listVariFeatures.getSelectedValuesList();
        for (int i = 0; i < names.size(); i++) {
            for (int j = 0; j < lag; j++) {
                // tablaFeatureModel.addRow(new Object[]{"V" + (i + 1) + "_lagged_" + (j + 1), "Shift(" + (i + 1) + "," + (j + 1) + ")"});
                tablaFeatureModel.addRow(new Object[]{"V(" + (names.get(i)) + ")_lagged_" + (j + 1), "Shift(" + (i + 1) + "," + (j + 1) + ")"});

            }
        }
        for (int c = 0; c < tablaFeatures.getColumnCount(); c++) {
            Class<?> col_class = tablaFeatures.getColumnClass(c);
            tablaFeatures.setDefaultEditor(col_class, null);        // remove editor
        }

        //String lag = sizeTextFieldFeature.getText().trim();
        atributosFeatures.add(new Attribute(listaInputVariables.getSelectedValue(), (ArrayList<String>) null));
        TimeSerie s = new TimeSerie();
        //s.target(listInstances, mHorizon, horizon, lag * (-1), targets);
        //trainDataset = s.hori(listInstances, mHorizon, horizon, lag * (-1), targets);
        //  System.out.println("///////////////////////////////////////");

    }//GEN-LAST:event_addButtonFeatureActionPerformed

    private void selectAllInputButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectAllInputButtonActionPerformed
        int start = 0;
        int end = listaInputVariables.getModel().getSize() - 1;
        if (end >= 0) {
            listaInputVariables.setSelectionInterval(start, end);
        }
    }//GEN-LAST:event_selectAllInputButtonActionPerformed

    private void selectAllVariFeaturesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectAllVariFeaturesActionPerformed
        int start = 0;
        int end = listVariFeatures.getModel().getSize() - 1;
        if (end >= 0) {
            listVariFeatures.setSelectionInterval(start, end);
        }

    }//GEN-LAST:event_selectAllVariFeaturesActionPerformed

    private void tablaFeaturesMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tablaFeaturesMouseClicked
        if (evt.getClickCount() > 1) {
            String nombre = String.valueOf(tablaFeatures.getValueAt(tablaFeatures.getSelectedRow(), 0));
            nameTextFeature.setText(nombre);
            String formula = String.valueOf(tablaFeatures.getValueAt(tablaFeatures.getSelectedRow(), 1));
            formulaTextFeature.setText(formula);
        }
    }//GEN-LAST:event_tablaFeaturesMouseClicked

    private void functionComboFeatureItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_functionComboFeatureItemStateChanged
        functionCambiaLabelFeature.setText(functionComboFeature.getSelectedItem().toString());
        if (functionComboFeature.getSelectedItem() == "shift") {
            shiftTextFeature1.setVisible(false);
        } else {
            shiftTextFeature1.setVisible(true);
        }

    }//GEN-LAST:event_functionComboFeatureItemStateChanged

    private void addFunctionButtonFeatureActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addFunctionButtonFeatureActionPerformed
        MaxMin s = new MaxMin();
        CustomFeature cf = new CustomFeature();
        MeanAndCount me = new MeanAndCount();
        Suma sum = new Suma();
        Mdelta mdelta = new Mdelta();
        List<Integer> indexes = new ArrayList();
        double res;
        int i = 0;

        switch (functionComboFeature.getSelectedItem().toString()) {
            case "mean":
                System.out.println("Numero instancias: " + listInstances.get(0).numInstances());
                for (int m = 0; m < listInstances.size(); m++) {
                    i = 0;
                    while (i < listInstances.get(m).numAttributes()) {
                        if (variableComboFunctionFeature.getSelectedItem().toString().equals(listInstances.get(m).attribute(i).name())) {
                            indexes.add(listInstances.get(m).attribute(i).index());
                        }
                        i++;
                    }
                    formulaTextFeature.setText(functionComboFeature.getSelectedItem() + "(" + (indexes.get(m) + 1) + "," + shiftTextFeature.getText() + "," + shiftTextFeature1.getText() + ")");
                }
                functions.add(functionComboFeature.getSelectedItem());

                break;

            case "max":
                for (int m = 0; m < listInstances.size(); m++) {
                    i = 0;
                    while (i < listInstances.get(m).numAttributes()) {
                        if (variableComboFunctionFeature.getSelectedItem().toString().equals(listInstances.get(m).attribute(i).name())) {
                            indexes.add(listInstances.get(m).attribute(i).index());
                        }
                        i++;
                    }

                    formulaTextFeature.setText(functionComboFeature.getSelectedItem() + "(" + (indexes.get(m) + 1) + "," + shiftTextFeature.getText() + "," + shiftTextFeature1.getText() + ")");
                }
                functions.add(functionComboFeature.getSelectedItem());
                //s.max(listInstances, Integer.parseInt(shiftTextFeature.getText()), Integer.parseInt(shiftTextFeature1.getText()), variableComboFunctionFeature.getSelectedItem().toString(), trainDataset, mHorizon);
                break;
            case "min":
                for (int m = 0; m < listInstances.size(); m++) {
                    i = 0;
                    while (i < listInstances.get(m).numAttributes()) {
                        if (variableComboFunctionFeature.getSelectedItem().toString().equals(listInstances.get(m).attribute(i).name())) {
                            indexes.add(listInstances.get(m).attribute(i).index());
                        }
                        i++;
                    }

                    formulaTextFeature.setText(functionComboFeature.getSelectedItem() + "(" + (indexes.get(m) + 1) + "," + shiftTextFeature.getText() + "," + shiftTextFeature1.getText() + ")");
                }
                functions.add(functionComboFeature.getSelectedItem());
                //s.min(listInstances, Integer.parseInt(shiftTextFeature.getText()), Integer.parseInt(shiftTextFeature1.getText()), variableComboFunctionFeature.getSelectedItem().toString(), trainDataset, mHorizon);
                break;
            case "sum":
                for (int m = 0; m < listInstances.size(); m++) {
                    i = 0;
                    while (i < listInstances.get(m).numAttributes()) {
                        if (variableComboFunctionFeature.getSelectedItem().toString().equals(listInstances.get(m).attribute(i).name())) {
                            indexes.add(listInstances.get(m).attribute(i).index());
                        }
                        i++;
                    }

                    formulaTextFeature.setText(functionComboFeature.getSelectedItem() + "(" + (indexes.get(m) + 1) + "," + shiftTextFeature.getText() + "," + shiftTextFeature1.getText() + ")");
                }
                functions.add(functionComboFeature.getSelectedItem());
                break;

            case "mdelta":
                for (int m = 0; m < listInstances.size(); m++) {
                    i = 0;
                    while (i < listInstances.get(m).numAttributes()) {
                        if (variableComboFunctionFeature.getSelectedItem().toString().equals(listInstances.get(m).attribute(i).name())) {
                            indexes.add(listInstances.get(m).attribute(i).index());
                        }
                        i++;
                    }

                    formulaTextFeature.setText(functionComboFeature.getSelectedItem() + "(" + (indexes.get(m) + 1) + "," + shiftTextFeature.getText() + "," + shiftTextFeature1.getText() + ")");
                }
                functions.add(functionComboFeature.getSelectedItem());
                break;

            case "count":
                for (int m = 0; m < listInstances.size(); m++) {
                    i = 0;
                    while (i < listInstances.get(m).numAttributes()) {
                        if (variableComboFunctionFeature.getSelectedItem().toString().equals(listInstances.get(m).attribute(i).name())) {
                            indexes.add(listInstances.get(m).attribute(i).index());
                        }
                        i++;
                    }

                    formulaTextFeature.setText(functionComboFeature.getSelectedItem() + "(" + (indexes.get(m) + 1) + "," + shiftTextFeature.getText() + "," + shiftTextFeature1.getText() + ")");
                }
                functions.add(functionComboFeature.getSelectedItem());
                break;
            case "sd":
                for (int m = 0; m < listInstances.size(); m++) {
                    i = 0;
                    while (i < listInstances.get(m).numAttributes()) {
                        if (variableComboFunctionFeature.getSelectedItem().toString().equals(listInstances.get(m).attribute(i).name())) {
                            indexes.add(listInstances.get(m).attribute(i).index());
                        }
                        i++;
                    }

                    formulaTextFeature.setText(functionComboFeature.getSelectedItem() + "(" + (indexes.get(m) + 1) + "," + shiftTextFeature.getText() + "," + shiftTextFeature1.getText() + ")");
                }
                functions.add(functionComboFeature.getSelectedItem());
                break;
            case "shift":
                for (int m = 0; m < listInstances.size(); m++) {
                    i = 0;
                    while (i < listInstances.get(m).numAttributes()) {
                        if (variableComboFunctionFeature.getSelectedItem().toString().equals(listInstances.get(m).attribute(i).name())) {
                            indexes.add(listInstances.get(m).attribute(i).index());
                        }
                        i++;
                    }

                    formulaTextFeature.setText(functionComboFeature.getSelectedItem() + "(" + (indexes.get(m) + 1) + ",-" + shiftTextFeature.getText()+")");
                }
                functions.add(functionComboFeature.getSelectedItem());
            //falta case "shift"
            }

    }//GEN-LAST:event_addFunctionButtonFeatureActionPerformed

    private void saveAlgoBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveAlgoBtnActionPerformed
        contadorGuardarClassifier = 0;
        File sFile = null;
        boolean saveOK = true;

        m_FileChooser.removeChoosableFileFilter(m_PMMLModelFilter);
        m_FileChooser.setFileFilter(m_ModelFilter);
        int returnVal = m_FileChooser.showSaveDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            sFile = m_FileChooser.getSelectedFile();
            if (!sFile.getName().toLowerCase().endsWith(MODEL_FILE_EXTENSION)) {
                sFile
                        = new File(sFile.getParent(), sFile.getName() + MODEL_FILE_EXTENSION);
            }
            m_log.statusMessage("Saving model to file...");

            try {
                OutputStream os = new FileOutputStream(sFile);
                if (sFile.getName().endsWith(".gz")) {
                    os = new GZIPOutputStream(os);
                }
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(os);
                for (int i = 0; i < listClassifier.size(); i++) {
                    objectOutputStream.writeObject(listClassifier.get(i));
                    contadorGuardarClassifier++;
                }
                //fw.write("\n");

                // fw.close();
            } catch (IOException ex) {
                java.util.logging.Logger.getLogger(tsStudio.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }//GEN-LAST:event_saveAlgoBtnActionPerformed


    private void loadAlgoBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadAlgoBtnActionPerformed
        m_FileChooser.addChoosableFileFilter(m_PMMLModelFilter);
        m_FileChooser.setFileFilter(m_ModelFilter);
        int returnVal = m_FileChooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            InputStream is = null;
            try {
                File selected = m_FileChooser.getSelectedFile();
                Classifier classifier = null;
                m_log.statusMessage("Loading model from file...");
                is = new FileInputStream(selected);

                if (selected.getName().endsWith(PMML_FILE_EXTENSION)) {
                    PMMLModel model = PMMLFactory.getPMMLModel(is, m_log);
                    if (model instanceof PMMLClassifier) {
                        classifier = (PMMLClassifier) model;
                        /*
                        * trainHeader = ((PMMLClassifier)classifier).getMiningSchema().
                        * getMiningSchemaAsInstances();
                         */
                    } else {
                        throw new Exception(
                                "PMML model is not a classification/regression model!");
                    }
                } else {
                    if (selected.getName().endsWith(".gz")) {
                        is = new GZIPInputStream(is);
                    }
                    // ObjectInputStream objectInputStream = new ObjectInputStream(is);
                    ObjectInputStream objectInputStream
                            = SerializationHelper.getObjectInputStream(is);
                    System.out.println("STRING--> " + objectInputStream.toString());
                    while (true) {
                        try {
                            listClassifier.add((Classifier) objectInputStream.readObject());
                        } catch (EOFException ex) {
                            break;
                        }
                    }
                    objectInputStream.close();

                    //System.out.println(listClassifier);
                    /*Classifier cl;
                    String initialDir = ExplorerDefaults.getInitialDirectory();
                    JFileChooser fileChooser = new JFileChooser(new File(initialDir));
                    fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                    FileNameExtensionFilter filter = new FileNameExtensionFilter("txt file(*.txt)", "txt");
                    fileChooser.setFileFilter(filter);
                    int returnVal = fileChooser.showSaveDialog(this);
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                    try {
                    BufferedReader obj = new BufferedReader(new FileReader(fileChooser.getSelectedFile()));
                    Object strng;
                    
                    while ((strng = obj.readLine()) != null) {
                    
                    tablaAlgoritmosModel.addRow(new Object[]{(strng)});

                    listClassifier.add(strng);

                    }
                    } catch (IOException ex) {
                    java.util.logging.Logger.getLogger(tsStudio.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    }
                     */
                }
            } catch (FileNotFoundException ex) {
                java.util.logging.Logger.getLogger(tsStudio.class.getName()).log(Level.SEVERE, null, ex);
            } catch (Exception ex) {
                java.util.logging.Logger.getLogger(tsStudio.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    is.close();
                } catch (IOException ex) {
                    java.util.logging.Logger.getLogger(tsStudio.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        for (int i = 0; i < listClassifier.size(); i++) {
            tablaAlgoritmosModel.addRow(new Object[]{(getSpec(listClassifier.get(i)))});
        }
        experimentLabel.setText("<html><p style=\"width:100px\">" + "Current experiment: " + "Name: " + newTextField.getText() + " Datasets: " + listInstances.size() + "</br> Features: " + listFeatures.size() + " Classifiers: " + listClassifier.size() + " Validation: none Result: " + calculated + "</p></html>");


    }//GEN-LAST:event_loadAlgoBtnActionPerformed

    private void tablaAlgoMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tablaAlgoMouseClicked
        if (evt.getClickCount() > 1) {
            //String algo = String.valueOf(tablaAlgo.getValueAt(tablaAlgo.getSelectedRow(), 0));
            m_ClassifierEditor.setValue(listClassifier.get(tablaAlgo.getSelectedRow()));

        }
    }//GEN-LAST:event_tablaAlgoMouseClicked

    private void nameTextFeatureKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_nameTextFeatureKeyTyped
        Character letra = evt.getKeyChar();
        if (evt.getKeyChar() == KeyEvent.VK_SPACE) {
            evt.setKeyChar('_');
        }
        if (evt.getKeyChar() == 'ñ') {
            evt.setKeyChar('y');
        }
    }//GEN-LAST:event_nameTextFeatureKeyTyped

    private void addVariableButtonFeatureActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addVariableButtonFeatureActionPerformed
        formulaTextFeature.setText("var(" + variableComboFeature.getSelectedItem().toString() + ")");
    }//GEN-LAST:event_addVariableButtonFeatureActionPerformed

    private void addCustomFeatureButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addCustomFeatureButtonActionPerformed
        if (nameTextFeature.getText().isEmpty() || formulaTextFeature.getText().isEmpty()) {

        } else {
            tablaFeatureModel.addRow(new Object[]{nameTextFeature.getText(), formulaTextFeature.getText()});
            listFeatures.add(formulaTextFeature.getText());
            nombreCustomFeature.add(nameTextFeature.getText());
        }
        /*ArrayList<Attribute> atts = new ArrayList<>();

        for (int j = 0; j < datasets.size(); j++) {
            if (isNumeric(datasets.get(j).get(10).toString())) {
                atts.add(new Attribute(datasets.get(j).get(0).toString()));

            } else {
                atts.add(new Attribute(datasets.get(j).get(0).toString(), "yyyy-MM-dd"));

            }
        }
         */

        //double[] vals = new double[p.numAttributes()];
        //p.add(pepe);
        experimentLabel.setText("<html><p style=\"width:100px\">" + "Name: " + newTextField.getText() + "<br> Datasets: " + listInstances.size() + "<br> Features: " + listFeatures.size() + "<br>  Classifiers: " + listClassifier.size() + "<br>  Validation: Absolute" + "<br> Result: " + calculated + "</p></html>");

    }//GEN-LAST:event_addCustomFeatureButtonActionPerformed

    private void jRadioButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton4ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jRadioButton4ActionPerformed

    private void jRadioButton5MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jRadioButton5MouseClicked
        jCheckBox2.setSelected(true);
        jCheckBox2.setEnabled(false);
    }//GEN-LAST:event_jRadioButton5MouseClicked

    private void runStopButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_runStopButtonActionPerformed
        runStopButton.setText("Stop");

        if (progressExp.getValue() != 0) {
            progressExp.setValue(0);
        }

        //to avoid interface blocking
        worker = new SwingWorker() {
            @Override
            protected Object doInBackground() throws Exception {
                startExp();
                return null;
            }
        };
        System.out.println("Done");
        worker.execute();
    }//GEN-LAST:event_runStopButtonActionPerformed

    private void saveButtonFeatureActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveButtonFeatureActionPerformed
        try {
            String initialDir = ExplorerDefaults.getInitialDirectory();
            JFileChooser fileChooser = new JFileChooser(new File(initialDir));
            fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            FileNameExtensionFilter filter = new FileNameExtensionFilter("txt file(*.txt)", "txt");
            fileChooser.setFileFilter(filter);
            int returnVal = fileChooser.showSaveDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                FileWriter fw = new FileWriter(fileChooser.getSelectedFile() + ".txt");
                for (int i = 0; i < tablaFeatures.getRowCount(); i++) //realiza un barrido por filas.
                {
                    for (int j = 0; j < tablaFeatures.getColumnCount(); j++) //realiza un barrido por columnas.
                    {
                        fw.write((String) tablaFeatures.getValueAt(i, j));
                        if (j < tablaFeatures.getColumnCount() - 1) { //agrega separador "," si no es el ultimo elemento de la fila.
                            fw.write("/");
                        }
                    }
                    //inserta nueva linea.
                    fw.write("\n");

                    // Si el archivo no existe es creado
                }
                fw.close();
            }

        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(tsStudio.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_saveButtonFeatureActionPerformed

    private void loadButtonFeatureActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadButtonFeatureActionPerformed
        String initialDir = ExplorerDefaults.getInitialDirectory();
        JFileChooser fileChooser = new JFileChooser(new File(initialDir));
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        FileNameExtensionFilter filter = new FileNameExtensionFilter("txt file(*.txt)", "txt");
        fileChooser.setFileFilter(filter);
        int returnVal = fileChooser.showSaveDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            try {
                BufferedReader obj = new BufferedReader(new FileReader(fileChooser.getSelectedFile()));
                String strng;
                while ((strng = obj.readLine()) != null) {
                    System.out.println(strng);
                    String[] parts = strng.split("/");
                    String part1 = parts[0];
                    String part2 = parts[1];
                    tablaFeatureModel.addRow(new Object[]{part1, part2});

                }
            } catch (IOException ex) {
                java.util.logging.Logger.getLogger(tsStudio.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_loadButtonFeatureActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        instPredictions = createObjectInstances(predictionTableModel, "Predictions");
        saveCSVTable(instPredictions);
    }//GEN-LAST:event_jButton3ActionPerformed

    private Instances createObjectInstances(DefaultTableModel tableModel, String name) {
        ArrayList<Attribute> listAttributes = new ArrayList<>();

        for (int i = 0; i < tableModel.getColumnCount(); i++) {
            Attribute a = null;

            if (tableModel.getColumnName(i).equals("h1") || tableModel.getColumnName(i).equals("Average") || tableModel.getColumnName(i).equals("h2") || tableModel.getColumnName(i).equals("h3")
                    || tableModel.getColumnName(i).equals("Horizon") || tableModel.getColumnName(i).equals("Pred") || tableModel.getColumnName(i).equals("Actual") || tableModel.getColumnName(i).equals("Error")
                    || tableModel.getColumnName(i).equals("SMAPE") || tableModel.getColumnName(i).equals("AUC") || tableModel.getColumnName(i).equals("MAE") || tableModel.getColumnName(i).equals("MSE") || tableModel.getColumnName(i).equals("RMSE")
                    || tableModel.getColumnName(i).equals("MAPE") || tableModel.getColumnName(i).equals("R2")) {
                a = new Attribute(tableModel.getColumnName(i));
            } else {
                a = new Attribute(tableModel.getColumnName(i), (ArrayList<String>) null);
            }

            listAttributes.add(a);
        }

        Instances tableInst = new Instances(name, listAttributes, 0);

        double[] values = null;

        for (int r = 0; r < tableModel.getRowCount(); r++) {
            values = new double[tableInst.numAttributes()];

            for (int c = 0; c < tableModel.getColumnCount(); c++) {
                if (tableModel.getValueAt(r, c) instanceof Double) {
                    values[c] = (double) tableModel.getValueAt(r, c);
                } else if (tableModel.getValueAt(r, c) instanceof Integer) {
                    values[c] = (int) tableModel.getValueAt(r, c);
                } else if (tableModel.getValueAt(r, c) instanceof String) {
                    values[c] = tableInst.attribute(c).addStringValue((String) tableModel.getValueAt(r, c));
                }
            }

            Instance row = new DenseInstance(1.0, values);
            tableInst.add(row);
        }

        return tableInst;
    }

    private void saveCSVTable(Instances saveInst) {
        String initialDir = ExplorerDefaults.getInitialDirectory();
        JFileChooser fileChooser = new JFileChooser(new File(initialDir));
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        FileNameExtensionFilter filter = new FileNameExtensionFilter("CSV file: comma separated file (*.csv)", "csv");
        fileChooser.setFileFilter(filter);
        int returnVal = fileChooser.showSaveDialog(this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            CSVSaver saver = new CSVSaver();
            saver.setInstances(saveInst);
            try {
                File out = new File(fileChooser.getSelectedFile() + ".csv");
                saver.setFile(out);
                saver.writeBatch();
            } catch (IOException ex) {
                //java.util.logging.Logger.getLogger(AttrSelExp.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void saveCsvOverallMetricsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveCsvOverallMetricsActionPerformed
        instPredictions = createObjectInstances(overallMetricsTableModel, "Predictions");
        saveCSVTable(instPredictions);
    }//GEN-LAST:event_saveCsvOverallMetricsActionPerformed

    private void jButton8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton8ActionPerformed
        instPredictions = createObjectInstances(overallMetricsTableModel, "Predictions");
        String initialDir = ExplorerDefaults.getInitialDirectory();
        JFileChooser fileChooser = new JFileChooser(new File(initialDir));
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Latex file: (*.latex)", "latex");
        fileChooser.setFileFilter(filter);
        int returnVal = fileChooser.showSaveDialog(this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {

            CSVSaver saver = new CSVSaver();
            saver.setInstances(instPredictions);
            try {
                File out = new File(fileChooser.getSelectedFile() + ".latex");
                saver.setFile(out);
                saver.writeBatch();
            } catch (IOException ex) {
                //java.util.logging.Logger.getLogger(AttrSelExp.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_jButton8ActionPerformed

    private void targetComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_targetComboBoxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_targetComboBoxActionPerformed

    private void targetComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_targetComboBoxItemStateChanged

        if (evt.getStateChange() == ItemEvent.SELECTED) {
            if (eleccionTrainingIncrement == 0) {
                List<List<List<List<List>>>> metric = new ArrayList();
                if (metricsComboBox.getSelectedItem().toString() == "MAE") {
                    metric = maeGeneralTrainingIncrement;
                } else if (metricsComboBox.getSelectedItem().toString() == "R2") {
                    metric = r2GeneralTrainingIncrement;
                } else {
                    metric = rmseGeneralTrainingIncrement;
                }
                metricsByHorizonTableModel = new DefaultTableModel();
                metricsByHorizonTable.setModel(metricsByHorizonTableModel);
                metricsByHorizonTableModel.addColumn("Algorithms");
                System.out.println("Entra en el item state changed");
                int indexDataset = datasetComboBox.getSelectedIndex();
                int indexTarget = targetComboBox.getSelectedIndex();
                double suma = 0;
                double numeroH = 0;
                //POR DEFECTO APARECE EL PRIMER TARGET
                for (int i = 0; i < horizon.get(indexTarget); i++) {
                    metricsByHorizonTableModel.addColumn("h" + (i + 1));
                    numeroH = i;
                }
                //Una vez metidas todas las columnas de horizontes, se crea la de average.
                metricsByHorizonTableModel.addColumn("Average");
                //System.out.println("mae.get(0).size()--> " + maeGeneral.get(0).size());
                System.out.println("maeGeneralTrainingIncrement--> " + maeGeneralTrainingIncrement);
                int columnCount = metricsByHorizonTableModel.getColumnCount();
                for (int i = 0; i < classifiersPrintGeneral.get(0).size(); i++) {
                    for (int k = 0; k < metric.get(indexDataset).get(i).get(indexTarget).get(0).size(); k++) {
                        suma = 0;
                        Object[] fila = new Object[metricsByHorizonTableModel.getColumnCount()];
                        for (int j = 0; j < columnCount; j++) {
                            if (j == 0) {
                                fila[j] = classifiersPrintGeneral.get(indexTarget).get(i);
                            } else if (columnCount - j != 1) {
                                System.out.println("||--> " + metric.get(indexDataset).get(i).get(indexTarget).get((j - 1)).get(k));
                                fila[j] = metric.get(indexDataset).get(i).get(indexTarget).get((j - 1)).get(k);
                                suma = suma + Double.parseDouble(metric.get(indexDataset).get(i).get(indexTarget).get((j - 1)).get(k).toString());
                            } else {
                                // for (int z = 0; z < metric.get(indexDataset).get(i).get(indexTarget).get(0).size(); z++) {
                                //     suma = suma + Double.parseDouble(metric.get(indexDataset).get(i).get(indexTarget).get(z).toString());
                                // }
                                fila[j] = suma / numeroH;//suma / numeroH;
                            }
                        }
                        metricsByHorizonTableModel.addRow(fila);
                    }

                }
            } else {
                List<List<List<List>>> metric = new ArrayList();
                if (metricsComboBox.getSelectedItem().toString() == "MAE") {
                    metric = maeGeneral;
                } else if (metricsComboBox.getSelectedItem().toString() == "R2") {
                    metric = r2General;
                } else {
                    metric = rmseGeneral;
                }
                metricsByHorizonTableModel = new DefaultTableModel();
                metricsByHorizonTable.setModel(metricsByHorizonTableModel);
                metricsByHorizonTableModel.addColumn("Algorithms");
                System.out.println("Entra en el item state changed");
                int indexDataset = datasetComboBox.getSelectedIndex();
                int indexTarget = targetComboBox.getSelectedIndex();
                double suma = 0;
                double numeroH = 0;
                //POR DEFECTO APARECE EL PRIMER TARGET
                for (int i = 0; i < horizon.get(indexTarget); i++) {
                    metricsByHorizonTableModel.addColumn("h" + (i + 1));
                    numeroH = i;
                }
                //Una vez metidas todas las columnas de horizontes, se crea la de average.
                metricsByHorizonTableModel.addColumn("Average");
                System.out.println("mae.get(0).size()--> " + maeGeneral.get(0).size());
                System.out.println("maeByHorizon--> " + maeGeneral);
                int columnCount = metricsByHorizonTableModel.getColumnCount();
                for (int i = 0; i < classifiersPrintGeneral.get(0).size(); i++) {
                    Object[] fila = new Object[metricsByHorizonTableModel.getColumnCount()];
                    for (int j = 0; j < columnCount; j++) {
                        if (j == 0) {
                            fila[j] = classifiersPrintGeneral.get(indexTarget).get(i);
                        } else if (columnCount - j != 1) {
                            fila[j] = metric.get(indexDataset).get(i).get(indexTarget).get((j - 1));
                        } else {
                            for (int z = 0; z < metric.get(indexDataset).get(i).get(indexTarget).size(); z++) {
                                suma = suma + Double.parseDouble(metric.get(indexDataset).get(i).get(indexTarget).get(z).toString());
                            }
                            fila[j] = suma / numeroH;
                        }
                    }
                    metricsByHorizonTableModel.addRow(fila);

                }
            }
        }


    }//GEN-LAST:event_targetComboBoxItemStateChanged

    private void datasetComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_datasetComboBoxItemStateChanged
        if (evt.getStateChange() == ItemEvent.SELECTED) {
            if (eleccionTrainingIncrement == 0) {
                System.out.println("PRUEBA TRAINING INCREMENT");
                pruebaTrainingIncrement(targetComboBox.getItemCount());
            } else {
                prueba(targetComboBox.getItemCount());
            }
            /*
                metricsByHorizonTableModel = new DefaultTableModel();
                metricsByHorizonTable.setModel(metricsByHorizonTableModel);
                metricsByHorizonTableModel.addColumn("Algorithms");
                System.out.println("Entra en el item state changed");
                int indexTarget = targetComboBox.getSelectedIndex();
                int indexDataset = datasetComboBox.getSelectedIndex();
                double suma = 0;
                double numeroH = 0;
                //POR DEFECTO APARECE EL PRIMER TARGET
                for (int i = 0; i < horizon.get(indexTarget); i++) {
                    metricsByHorizonTableModel.addColumn("h" + (i + 1));
                    numeroH = i;
                }
                //Una vez metidas todas las columnas de horizontes, se crea la de average.
                metricsByHorizonTableModel.addColumn("Average");
                System.out.println("mae.get(0).size()--> " + maeGeneral.get(0).size());
                System.out.println("maeByHorizon--> " + maeGeneral);
                int columnCount = metricsByHorizonTableModel.getColumnCount();
                for (int i = 0; i < classifiersPrintGeneral.get(0).size(); i++) {
                    Object[] fila = new Object[metricsByHorizonTableModel.getColumnCount()];
                    for (int j = 0; j < columnCount; j++) {
                        if (j == 0) {
                            fila[j] = classifiersPrintGeneral.get(indexTarget).get(i);
                        } else if (columnCount - j != 1) {
                            fila[j] = maeGeneral.get(indexDataset).get(i).get(indexTarget).get((j - 1));
                        } else {
                            for (int z = 0; z < maeGeneral.get(indexDataset).get(i).get(indexTarget).size(); z++) {
                                suma = suma + Double.parseDouble(maeGeneral.get(indexDataset).get(i).get(indexTarget).get(z).toString());
                            }
                            fila[j] = suma / numeroH;
                        }
                    }
                    metricsByHorizonTableModel.addRow(fila);

                }
            }

             */
        }

    }//GEN-LAST:event_datasetComboBoxItemStateChanged

    private void metricsComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_metricsComboBoxItemStateChanged
        if (evt.getStateChange() == ItemEvent.SELECTED) {
            if (eleccionTrainingIncrement == 0) {
                pruebaTrainingIncrement(datasetComboBox.getItemCount());
            } else {
                prueba(datasetComboBox.getItemCount());
            }
        }
    }//GEN-LAST:event_metricsComboBoxItemStateChanged

    private void targetPlotComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_targetPlotComboBoxItemStateChanged
        if (evt.getStateChange() == ItemEvent.SELECTED) {

            try {
                if (eleccionTrainingIncrement == 0) {
                    plotTrainingIncrement(datasetPlotComboBox.getItemCount(), valuesPredictPlotTrainingIncrement, fechasPrepositionalDatasets);

                } else {
                    pruebaPlot(datasetPlotComboBox.getItemCount(), valuesPredictPlot, fechasPrepositionalDatasets);
                }
            } catch (ParseException ex) {
                java.util.logging.Logger.getLogger(tsStudio.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }//GEN-LAST:event_targetPlotComboBoxItemStateChanged

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        instPredictions = createObjectInstances(predictionTableModel, "Predictions");
        try {
            saveArffTable(instPredictions);
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(tsStudio.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jButton4ActionPerformed

    private void saveArffOverallMetricsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveArffOverallMetricsActionPerformed
        instPredictions = createObjectInstances(overallMetricsTableModel, "Predictions");
        try {
            saveArffTable(instPredictions);
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(tsStudio.class.getName()).log(Level.SEVERE, null, ex);
         }    }//GEN-LAST:event_saveArffOverallMetricsActionPerformed

    private void jButton10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton10ActionPerformed
        instPredictions = createObjectInstances(metricsByHorizonTableModel, "Predictions");

        saveCSVTable(instPredictions);

    }//GEN-LAST:event_jButton10ActionPerformed

    private void jButton11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton11ActionPerformed
        instPredictions = createObjectInstances(metricsByHorizonTableModel, "Predictions");
        try {
            saveArffTable(instPredictions);
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(tsStudio.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jButton11ActionPerformed

    private void jButton12ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton12ActionPerformed
        instPredictions = createObjectInstances(metricsByHorizonTableModel, "Predictions");
        String initialDir = ExplorerDefaults.getInitialDirectory();
        JFileChooser fileChooser = new JFileChooser(new File(initialDir));
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Latex file: (*.latex)", "latex");
        fileChooser.setFileFilter(filter);
        int returnVal = fileChooser.showSaveDialog(this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {

            CSVSaver saver = new CSVSaver();
            saver.setInstances(instPredictions);
            try {
                File out = new File(fileChooser.getSelectedFile() + ".latex");
                saver.setFile(out);
                saver.writeBatch();
            } catch (IOException ex) {
                //java.util.logging.Logger.getLogger(AttrSelExp.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_jButton12ActionPerformed

    private void horizonPlotComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_horizonPlotComboBoxItemStateChanged
        if (evt.getStateChange() == ItemEvent.SELECTED) {

            try {
                if (horizonPlotComboBox.getItemCount() == 2) {
                    pruebaPlot(targetPlotComboBox.getItemCount(), valuesPredictPlot, fechasPrepositionalDatasets);
                }
            } catch (ParseException ex) {
                java.util.logging.Logger.getLogger(tsStudio.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }//GEN-LAST:event_horizonPlotComboBoxItemStateChanged

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        instPredictions = createObjectInstances(predictionTableModel, "Predictions");
        getExplorer().getPreprocessPanel().setInstances(instPredictions);
    }//GEN-LAST:event_jButton5ActionPerformed

    private void jButton9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton9ActionPerformed
        instPredictions = createObjectInstances(overallMetricsTableModel, "Overall Metrics");
        getExplorer().getPreprocessPanel().setInstances(instPredictions);
    }//GEN-LAST:event_jButton9ActionPerformed

    private void jButton13ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton13ActionPerformed
        instPredictions = createObjectInstances(metricsByHorizonTableModel, "Metrics By Horizon");
        getExplorer().getPreprocessPanel().setInstances(instPredictions);
    }//GEN-LAST:event_jButton13ActionPerformed

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
        if (forecastPlotPanel.getComponents().length != 0) {
            savePNGGraph();
        }

    }//GEN-LAST:event_jButton6ActionPerformed

    private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed
        if (forecastPlotPanel.getComponents().length != 0) {
            savePDFGraph();
        }    }//GEN-LAST:event_jButton7ActionPerformed

    private void absoluteRadioButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_absoluteRadioButtonMouseClicked
        fromTSOriginCheckBox.setSelected(true);
        experimentLabel.setText("<html><p style=\"width:100px\">" + "Name: " + newTextField.getText() + " Datasets: " + listInstances.size() + "<br> Features: " + listFeatures.size() + "<br>  Classifiers: " + listClassifier.size() + "<br>  Validation: " + "<br> Result: " + calculated + "</p></html>");

    }//GEN-LAST:event_absoluteRadioButtonMouseClicked

    private void relativeRadioButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_relativeRadioButtonMouseClicked
        fromTSOriginCheckBox.setSelected(true);
        jRadioButton4.setEnabled(false);
        experimentLabel.setText("<html><p style=\"width:100px\">" + "Name: " + newTextField.getText() + " Datasets: " + listInstances.size() + "<br> Features: " + listFeatures.size() + "<br>  Classifiers: " + listClassifier.size() + "<br>  Validation: " + "<br> Result: " + calculated + "</p></html>");
    }//GEN-LAST:event_relativeRadioButtonMouseClicked

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        tablaAlgo.selectAll();
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        if (tablaAlgo.getSelectedRow() != -1) {
            listClassifier.remove(tablaAlgo.getSelectedRow());
            tablaAlgoritmosModel.removeRow(tablaAlgo.getSelectedRow());
            System.out.println("Tamaño de lista: " + listClassifier.size());
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void datasetPlotComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_datasetPlotComboBoxItemStateChanged
        if (evt.getStateChange() == ItemEvent.SELECTED) {

            try {
                pruebaPlot(targetPlotComboBox.getItemCount(), valuesPredictPlot, fechasPrepositionalDatasets);

            } catch (ParseException ex) {
                java.util.logging.Logger.getLogger(tsStudio.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }//GEN-LAST:event_datasetPlotComboBoxItemStateChanged

    private void algorithmPlotComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_algorithmPlotComboBoxItemStateChanged
        if (evt.getStateChange() == ItemEvent.SELECTED) {

            try {
                pruebaPlot(targetPlotComboBox.getItemCount(), valuesPredictPlot, fechasPrepositionalDatasets);

            } catch (ParseException ex) {
                java.util.logging.Logger.getLogger(tsStudio.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }//GEN-LAST:event_algorithmPlotComboBoxItemStateChanged

    private void jButton14ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton14ActionPerformed
        String initialDir = ExplorerDefaults.getInitialDirectory();
        JFileChooser fileChooser = new JFileChooser(new File(initialDir));
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Arff data files (*.arff)", "arff");
        fileChooser.setFileFilter(filter);
        int returnVal = fileChooser.showSaveDialog(this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            for (int m = 0; m < listInstances.size(); m++) {
                try {
                    ConverterUtils.DataSink.write(fileChooser.getSelectedFile() + ".arff", prepositionalInstances.get(m));
                } catch (Exception ex) {
                    java.util.logging.Logger.getLogger(tsStudio.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }//GEN-LAST:event_jButton14ActionPerformed

    private void removeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
        if (datasetTable.getSelectedRow() != -1) {
            listInstances.remove(datasetTable.getSelectedRow());
            datasetTableModel.removeRow(datasetTable.getSelectedRow());
            contadorDataset--;
            experimentLabel.setText("<html><p style=\"width:100px\">" + "Current experiment: " + "Name: " + newTextField.getText() + "Datasets: " + datasets.size() + "</p></html>");
            timeFieldComboBox.removeAllItems();
            System.out.println("Tamaño de lista: " + listInstances.size());
            //Borrado de las variables comunes en la lista de variables(input tambien)
            if (listInstances.size() == 1) {
                modeloLista.removeAllElements();
                modeloInputVaribales.removeAllElements();
            } else {

            }

        } else {
            m_log.logMessage("No row is selected");
            m_log.statusMessage("See erro log");
        }
    }//GEN-LAST:event_removeButtonActionPerformed

    private void selectAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectAllButtonActionPerformed

        datasetTable.selectAll();
    }//GEN-LAST:event_selectAllButtonActionPerformed

    private void datasetTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_datasetTableMouseClicked
        Instances inst = listInstances.get(datasetTable.getSelectedRow());
        int cont = 0;
        attributeNames = new String[inst.numAttributes()];

        for (int i = 0; i < inst.numAttributes(); i++) {
            String type = Attribute.typeToString(inst.attribute(i));
            String attnm = inst.attribute(i).name();
            if (type.equals("string") || type.equals("date") || attnm.contains("FECHA")) {
                attributeNames[cont] = attnm;
                cont++;
            }
        }
        timeFieldComboBox.setModel(new DefaultComboBoxModel(attributeNames));
    }//GEN-LAST:event_datasetTableMouseClicked

    private void addFolderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addFolderActionPerformed
        int contador = 0;
        int numeroAtributos = 0;
        String aEquals;
        boolean repetido = false;
        
        String initialDir = ExplorerDefaults.getInitialDirectory();
        ConverterFileChooser m_FileChooser = new ConverterFileChooser(new File(initialDir));
        Instances instances;
        m_FileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnVal = m_FileChooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File[] files = m_FileChooser.getSelectedFile().listFiles();
            for (File file : files) {
                try {
                    instances = ConverterUtils.DataSource.read(file.toString());
                    addInstancesToDatasetList(instances, 1);
                    listInstances.add(instances);
                    radioButtonValue.setSelected(true);
                listVariables.setModel(modeloLista);
                listaInputVariables.setModel(modeloInputVaribales);
                } catch (Exception ex) {
                    java.util.logging.Logger.getLogger(tsStudio.class
                            .getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        for (int i = 0; i < listInstances.size() - 1; i++) {
                if (listInstances.get(i).numAttributes() > listInstances.get(i + 1).numAttributes()) {
                    numeroAtributos = listInstances.get(i).numAttributes();
                } else {
                    numeroAtributos = listInstances.get(i + 1).numAttributes();
                }
            }
            System.out.println("Numero de atributos: " + numeroAtributos);
            for (int i = 0; i < listInstances.get(0).numAttributes(); i++) {
                if (listInstances.get(0).attribute(i).isDate() == true || listInstances.get(0).attribute(i).name().contains("FECHA")) {
                    System.out.println("Es fecha");
                } else {
                    aEquals = listInstances.get(0).attribute(i).name();

                    for (int j = 1; j < listInstances.size(); j++) {
                        for (int k = 0; k < listInstances.get(j).numAttributes(); k++) {
                            if (aEquals.equals(listInstances.get(j).attribute(k).name())) {

                                if (attEquals.isEmpty()) {
                                    attEquals.add(aEquals);
                                    modeloLista.addElement(attEquals.get(contador));
                                    modeloInputVaribales.addElement(attEquals.get(contador));
                                    contador++;
                                } else {
                                    int c = 0;
                                    while (c < attEquals.size() && !repetido) {
                                        if (attEquals.get(c) == aEquals) {
                                            System.out.println("Repetido");
                                            repetido = true;
                                        } else {
                                            c++;
                                        }

                                    }
                                    if (!repetido) {
                                        System.out.println("No repetido");
                                        attEquals.add(aEquals);
                                        modeloLista.addElement(attEquals.get(contador));
                                        modeloInputVaribales.addElement(attEquals.get(contador));
                                        contador++;
                                    }
                                }

                            }
                        }
                    }

                }
            }
        
        variableComboFunctionFeature.setModel(new DefaultComboBoxModel());
        for (int i = 0; i < modeloInputVaribales.size(); i++) {
            variableComboFunctionFeature.addItem(modeloInputVaribales.get(i).toString());
        }
        variableComboFeature.setModel(new DefaultComboBoxModel());
        for (int i = 0; i < modeloInputVaribales.size(); i++) {
            variableComboFeature.addItem(modeloInputVaribales.get(i).toString());
        }

        /*Reader reader = null;
        //List attEquals = new ArrayList();
        listVariables.setModel(modeloLista);
        listaInputVariables.setModel(modeloLista);
        chooser.setFileSelectionMode(file_chooser.FILES_ONLY);
        FileNameExtensionFilter filtro = new FileNameExtensionFilter("Archivos CSV o ARFF ", "arff", "csv");
        chooser.setFileFilter(filtro);
        chooser.showOpenDialog(this);
        File archivo = chooser.getSelectedFile();
        List<String> aux = new ArrayList<>();
        String a;
         */
        //Reader fileReader = new FileReader(m_FileChooser.getSelectedFile());
        experimentLabel.setText("<html><p style=\"width:100px\">" + "Name: " + newTextField.getText() + "<br> Datasets: " + listInstances.size() + "<br> Features: " + listFeatures.size() + "<br>  Classifiers: " + listClassifier.size() + "<br>  Validation: Absolute" + "<br> Result: " + calculated + "</p></html>");

        System.out.println("Tamaño lista: " + listInstances.size());

        int start = 0;
        int end = listVariFeatures.getModel().getSize() - 1;
        if (end >= 0) {
            listVariFeatures.setSelectionInterval(start, end);
        }
        /*int start = 0;
        int end = listVariFeatures.getModel().getSize() - 1;
        if (end >= 0) {
            listVariFeatures.setSelectionInterval(start, end);
        }
        */
    }//GEN-LAST:event_addFolderActionPerformed

    private void addFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addFileActionPerformed
        int contador = 0;
        int numeroAtributos = 0;
        String aEquals;
        boolean repetido = false;
        String initialDir = ExplorerDefaults.getInitialDirectory();
        m_FileChooser = new ConverterFileChooser(new File(initialDir));
        ConverterFileChooser m_FileChooser = new ConverterFileChooser(new File(initialDir));
        Instances instances;
        m_FileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        int returnVal = m_FileChooser.showOpenDialog(this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {

            File file = m_FileChooser.getSelectedFile();

            try {
                instances = ConverterUtils.DataSource.read(file.toString());
                addInstancesToDatasetList(instances, 1);
                listInstances.add(instances);
                radioButtonValue.setSelected(true);
                listVariables.setModel(modeloLista);
                listaInputVariables.setModel(modeloInputVaribales);
            } catch (Exception ex) {

            }
        }

        if (listInstances.size() > 1) {
            modeloLista.removeAllElements();
            for (int i = 0; i < listInstances.size() - 1; i++) {
                if (listInstances.get(i).numAttributes() > listInstances.get(i + 1).numAttributes()) {
                    numeroAtributos = listInstances.get(i).numAttributes();
                } else {
                    numeroAtributos = listInstances.get(i + 1).numAttributes();
                }
            }
            System.out.println("Numero de atributos: " + numeroAtributos);
            for (int i = 0; i < listInstances.get(0).numAttributes(); i++) {
                if (listInstances.get(0).attribute(i).isDate() == true || listInstances.get(0).attribute(i).name().contains("FECHA")) {
                    System.out.println("Es fecha");
                } else {
                    aEquals = listInstances.get(0).attribute(i).name();

                    for (int j = 1; j < listInstances.size(); j++) {
                        for (int k = 0; k < listInstances.get(j).numAttributes(); k++) {
                            if (aEquals.equals(listInstances.get(j).attribute(k).name())) {

                                if (attEquals.isEmpty()) {
                                    attEquals.add(aEquals);
                                    modeloLista.addElement(attEquals.get(contador));
                                    modeloInputVaribales.addElement(attEquals.get(contador));
                                    contador++;
                                } else {
                                    int c = 0;
                                    while (c < attEquals.size() && !repetido) {
                                        if (attEquals.get(c) == aEquals) {
                                            System.out.println("Repetido");
                                            repetido = true;
                                        } else {
                                            c++;
                                        }

                                    }
                                    if (!repetido) {
                                        System.out.println("No repetido");
                                        attEquals.add(aEquals);
                                        modeloLista.addElement(attEquals.get(contador));
                                        modeloInputVaribales.addElement(attEquals.get(contador));
                                        contador++;
                                    }
                                }

                            }
                        }
                    }

                }
            }
        }
        else{
            for(int i = 0; i<listInstances.get(0).numAttributes();i++){
                if(listInstances.get(0).attribute(i).isDate()){
                    
                }
                else{
                  modeloLista.addElement(listInstances.get(0).attribute(i).name());
  
            }
        }
        }
        variableComboFunctionFeature.setModel(new DefaultComboBoxModel());
        for (int i = 0; i < modeloInputVaribales.size(); i++) {
            variableComboFunctionFeature.addItem(modeloInputVaribales.get(i).toString());
        }
        variableComboFeature.setModel(new DefaultComboBoxModel());
        for (int i = 0; i < modeloInputVaribales.size(); i++) {
            variableComboFeature.addItem(modeloInputVaribales.get(i).toString());
        }

        /*Reader reader = null;
        //List attEquals = new ArrayList();
        listVariables.setModel(modeloLista);
        listaInputVariables.setModel(modeloLista);
        chooser.setFileSelectionMode(file_chooser.FILES_ONLY);
        FileNameExtensionFilter filtro = new FileNameExtensionFilter("Archivos CSV o ARFF ", "arff", "csv");
        chooser.setFileFilter(filtro);
        chooser.showOpenDialog(this);
        File archivo = chooser.getSelectedFile();
        List<String> aux = new ArrayList<>();
        String a;
         */
        //Reader fileReader = new FileReader(m_FileChooser.getSelectedFile());
        experimentLabel.setText("<html><p style=\"width:100px\">" + "Name: " + newTextField.getText() + "<br> Datasets: " + listInstances.size() + "<br> Features: " + listFeatures.size() + "<br>  Classifiers: " + listClassifier.size() + "<br>  Validation: Absolute" + "<br> Result: " + calculated + "</p></html>");

        System.out.println("Tamaño lista: " + listInstances.size());

        int start = 0;
        int end = listVariFeatures.getModel().getSize() - 1;
        if (end >= 0) {
            listVariFeatures.setSelectionInterval(start, end);
        }
    }//GEN-LAST:event_addFileActionPerformed

    private void addPreprocessActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addPreprocessActionPerformed
        String aEquals;
        boolean repetido = false;

        m_Instances.setClassIndex(m_Instances.numAttributes() - 1);
        int contador = 0;
        int numeroAtributos = 0;
        datasetTableModel.addRow(new Object[]{m_Instances.relationName(), m_Instances.numInstances(), m_Instances.numAttributes()});
        listInstances.add(m_Instances);
        radioButtonValue.setSelected(true);
        listVariables.setModel(modeloLista);
        listaInputVariables.setModel(modeloInputVaribales);

        if (listInstances.size() > 1) {
            modeloLista.removeAllElements();
            for (int i = 0; i < listInstances.size() - 1; i++) {
                if (listInstances.get(i).numAttributes() > listInstances.get(i + 1).numAttributes()) {
                    numeroAtributos = listInstances.get(i).numAttributes();
                } else {
                    numeroAtributos = listInstances.get(i + 1).numAttributes();
                }
            }
            System.out.println("Numero de atributos: " + numeroAtributos);
            for (int i = 0; i < listInstances.get(0).numAttributes(); i++) {
                aEquals = listInstances.get(0).attribute(i).name();
                for (int j = 1; j < listInstances.size(); j++) {
                    for (int k = 0; k < listInstances.get(j).numAttributes(); k++) {
                        if (aEquals.equals(listInstances.get(j).attribute(k).name())) {
                            if (attEquals.isEmpty()) {
                                attEquals.add(aEquals);
                                modeloLista.addElement(attEquals.get(contador));
                                modeloInputVaribales.addElement(attEquals.get(contador));
                                contador++;
                            } else {
                                int c = 0;
                                while (c < attEquals.size() && !repetido) {
                                    if (attEquals.get(c) == aEquals) {
                                        System.out.println("Repetido");
                                        repetido = true;
                                    } else {
                                        c++;
                                    }

                                }
                                if (!repetido) {
                                    System.out.println("No repetido");
                                    attEquals.add(aEquals);
                                    modeloLista.addElement(attEquals.get(contador));
                                    modeloInputVaribales.addElement(attEquals.get(contador));
                                    contador++;
                                }
                            }
                        }
                    }
                }
            }
        }
        else{
            for(int i = 0; i<listInstances.get(0).numAttributes();i++){
                if(listInstances.get(0).attribute(i).isDate()){
                    
                }
                else{
                  modeloLista.addElement(listInstances.get(0).attribute(i).name());
  
            }
        }
        }
        variableComboFunctionFeature.setModel(new DefaultComboBoxModel());
        for (int i = 0; i < modeloInputVaribales.size(); i++) {
            variableComboFunctionFeature.addItem(modeloInputVaribales.get(i).toString());
        }
        variableComboFeature.setModel(new DefaultComboBoxModel());
        for (int i = 0; i < modeloInputVaribales.size(); i++) {
            variableComboFeature.addItem(modeloInputVaribales.get(i).toString());
        }

        int start = 0;
        int end = listVariFeatures.getModel().getSize() - 1;
        if (end >= 0) {
            listVariFeatures.setSelectionInterval(start, end);
        }
        experimentLabel.setText("<html><p style=\"width:100px\">" + "Name: " + newTextField.getText() + "<br> Datasets: " + listInstances.size() + "<br> Features: " + listFeatures.size() + "<br>  Classifiers: " + listClassifier.size() + "<br>  Validation: Absolute" + "<br> Result: " + calculated + "</p></html>");

    }//GEN-LAST:event_addPreprocessActionPerformed

    private void saveExperimentButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveExperimentButtonActionPerformed
        String initialDir = ExplorerDefaults.getInitialDirectory();
        //JFileChooser fileChooser = new JFileChooser(new File(initialDir));
        //fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        //FileNameExtensionFilter filter = new FileNameExtensionFilter("PDF", "pdf");
        //fileChooser.setFileFilter(filter);
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int option = fileChooser.showOpenDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {
            FileWriter fw = null;
            try {
                File file = fileChooser.getSelectedFile();
                System.out.println("carpeta seleccionada--> " + file.getAbsolutePath());
                for (int m = 0; m < listInstances.size(); m++) {
                    try {
                        ConverterUtils.DataSink.write(fileChooser.getSelectedFile() + "/" + nombreProyecto + "/Datasets" + "/" + listInstances.get(m).relationName() + ".arff", listInstances.get(m));

                    } catch (Exception ex) {
                        java.util.logging.Logger.getLogger(tsStudio.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                if (calculated.equals("calculated")) {
                    instPredictions = createObjectInstances(predictionTableModel, "Predictions");
                    try {
                        ConverterUtils.DataSink.write(fileChooser.getSelectedFile() + "/" + nombreProyecto + "/Results" + "/Prediction Table" + ".arff", instPredictions);
                    } catch (Exception ex) {
                        java.util.logging.Logger.getLogger(tsStudio.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    instPredictions = createObjectInstances(overallMetricsTableModel, "Overall metrics");
                    try {
                        ConverterUtils.DataSink.write(fileChooser.getSelectedFile() + "/" + nombreProyecto + "/Results" + "/Overall Metrics" + ".arff", instPredictions);
                    } catch (Exception ex) {
                        java.util.logging.Logger.getLogger(tsStudio.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    instPredictions = createObjectInstances(metricsByHorizonTableModel, "Metrics By Horizon");
                    try {
                        ConverterUtils.DataSink.write(fileChooser.getSelectedFile() + "/" + nombreProyecto + "/Results" + "/Metrics By Horizon" + ".arff", instPredictions);
                    } catch (Exception ex) {
                        java.util.logging.Logger.getLogger(tsStudio.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    //GRAFICA
                    File f = new File(fileChooser.getSelectedFile() + "/" + nombreProyecto + "/Results" + "/Plot" + ".pdf");
                    PDFDocument pdfDoc = new PDFDocument();
                    Page page = pdfDoc.createPage(new Rectangle(1280, 720));
                    PDFGraphics2D g2 = page.getGraphics2D();
                    chart.draw(g2, new Rectangle(0, 0, 1280, 720));
                    pdfDoc.writeToFile(f);
                    //PROPOSITIONAL DATASETS
                    for (int m = 0; m < listInstances.size(); m++) {
                        try {
                            ConverterUtils.DataSink.write(fileChooser.getSelectedFile() + "/" + nombreProyecto + "/Results" + "/Propositional Datasets" + ".arff", prepositionalInstances.get(m));
                        } catch (Exception ex) {
                            java.util.logging.Logger.getLogger(tsStudio.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                } else {

                }
                //Classifier
                contadorGuardarClassifier = 0;
                File sFile = null;
                boolean saveOK = true;
                m_FileChooser.removeChoosableFileFilter(m_PMMLModelFilter);
                m_FileChooser.setFileFilter(m_ModelFilter);
                fileChooser.setFileFilter(m_ModelFilter);
                OutputStream os = null;
                try {
                    sFile = new File(fileChooser.getSelectedFile() + "/" + nombreProyecto + "/Classifiers");
                    if (!sFile.getName().toLowerCase().endsWith(MODEL_FILE_EXTENSION)) {
                        sFile
                                = new File(sFile.getParent(), sFile.getName() + MODEL_FILE_EXTENSION);
                    }
                    m_log.statusMessage("Saving model to file...");
                    os = new FileOutputStream(sFile);
                    if (sFile.getName().endsWith(".gz")) {
                        os = new GZIPOutputStream(os);
                    }
                    ObjectOutputStream objectOutputStream = new ObjectOutputStream(os);
                    for (int i = 0; i < listClassifier.size(); i++) {
                        objectOutputStream.writeObject(listClassifier.get(i));
                        contadorGuardarClassifier++;
                    }
                } catch (FileNotFoundException ex) {
                    java.util.logging.Logger.getLogger(tsStudio.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    java.util.logging.Logger.getLogger(tsStudio.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    try {
                        os.close();
                    } catch (IOException ex) {
                        java.util.logging.Logger.getLogger(tsStudio.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                //Features
                fw = new FileWriter(fileChooser.getSelectedFile() + "/" + nombreProyecto + "/Features");
                for (int i = 0; i < tablaFeatures.getRowCount(); i++) //realiza un barrido por filas.
                {
                    for (int j = 0; j < tablaFeatures.getColumnCount(); j++) //realiza un barrido por columnas.
                    {
                        fw.write((String) tablaFeatures.getValueAt(i, j));
                        if (j < tablaFeatures.getColumnCount() - 1) { //agrega separador "," si no es el ultimo elemento de la fila.
                            fw.write("/");
                        }
                    }
                    //inserta nueva linea.
                    fw.write("\n");

                    // Si el archivo no existe es creado
                }
                fw.close();
            } catch (IOException ex) {
                java.util.logging.Logger.getLogger(tsStudio.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    fw.close();
                } catch (IOException ex) {
                    java.util.logging.Logger.getLogger(tsStudio.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            try {
                

                File file = (fileChooser.getSelectedFile());
                fw = new FileWriter(fileChooser.getSelectedFile() +"/" + nombreProyecto+"/Variables");
                for (int i = 0; i < listVariables.getModel().getSize(); i++) {
                    fw.write((String) listVariables.getModel().getElementAt(i));
                    fw.write("\n");
                }
                fw.close();

                fw = new FileWriter(fileChooser.getSelectedFile() +"/"+nombreProyecto+ "/Input Variables");
                for (int i = 0; i < listaInputVariables.getModel().getSize(); i++) {
                    fw.write((String) listaInputVariables.getModel().getElementAt(i));
                    fw.write("\n");
                }
                fw.close();

                fw = new FileWriter(fileChooser.getSelectedFile() +"/"+nombreProyecto+ "/Targets");
                for (int i = 0; i < tablaTarget.getRowCount(); i++) //realiza un barrido por filas.
                {
                    for (int j = 0; j < tablaTarget.getColumnCount(); j++) //realiza un barrido por columnas.
                    {
                        fw.write((String) tablaTarget.getValueAt(i, j));
                        if (j < tablaFeatures.getColumnCount() - 1) { //agrega separador "," si no es el ultimo elemento de la fila.
                            fw.write(",");
                        }

                    }
                    fw.write("\n");
                }
                fw.close();
                fw = new FileWriter(fileChooser.getSelectedFile() +"/"+nombreProyecto+ "/Scan Step");

                if (radioButtonMaximum.isSelected()) {
                    System.out.println("Maximum--> " + radioButtonMaximum.getText());
                    fw.write(radioButtonMaximum.getText());
                } else if (radioButtonMinimum.isSelected()) {
                    fw.write(radioButtonMinimum.getText());

                } else {
                    fw.write(radioButtonValue.getText());
                    fw.write(",");
                    fw.write(textFieldTimeSeries.getText());
                }
                fw.close();

            } catch (IOException ex) {
                java.util.logging.Logger.getLogger(tsStudio.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {

        }


    }//GEN-LAST:event_saveExperimentButtonActionPerformed

    private void loadExperimentButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadExperimentButtonActionPerformed
        String initialDir = ExplorerDefaults.getInitialDirectory();
        m_FileChooser = new ConverterFileChooser(new File(initialDir));
        ConverterFileChooser m_FileChooser = new ConverterFileChooser(new File(initialDir));
        Instances instances;
        m_FileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnVal = m_FileChooser.showOpenDialog(this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {

            try {
                
                File folder = new File(m_FileChooser.getSelectedFile() + "/Datasets");
                for (File file : folder.listFiles()) {
                    if (file.isDirectory()) {
                        //System.out.println("Hola");
                    } else {

                        try {
                            //System.out.println("entra");
                            instances = ConverterUtils.DataSource.read(file.toString());
                            addInstancesToDatasetList(instances, 1);
                            listInstances.add(instances);
                            radioButtonValue.setSelected(true);
                            listVariables.setModel(modeloLista);
                            listaInputVariables.setModel(modeloInputVaribales);
                            
                        } catch (Exception ex) {
                            
                        }
                    }
                }
                //FEATURES
                try {
                    BufferedReader obj = new BufferedReader(new FileReader(m_FileChooser.getSelectedFile() + "/Features"));
                    String strng;
                    
                    while ((strng = obj.readLine()) != null) {
                        System.out.println(strng);
                        if (strng.contains("lagged")) {
                            
                        } else {
                            listFeatures.add(strng.replaceAll("/", ","));
                        }
                        String[] parts = strng.split("/");
                        String part1 = parts[0];
                        String part2 = parts[1];
                        tablaFeatureModel.addRow(new Object[]{part1, part2});
                        
                    }

                } catch (IOException ex) {
                    java.util.logging.Logger.getLogger(tsStudio.class.getName()).log(Level.SEVERE, null, ex);
                }
                //Classifiers
                InputStream is = null;
                try {
                    File selected = new File(m_FileChooser.getSelectedFile() + "/Classifiers.model");
                    Classifier classifier = null;
                    m_log.statusMessage("Loading model from file...");
                    is = new FileInputStream(selected);
                    
                    if (selected.getName().endsWith(PMML_FILE_EXTENSION)) {
                        PMMLModel model = PMMLFactory.getPMMLModel(is, m_log);
                        if (model instanceof PMMLClassifier) {
                            classifier = (PMMLClassifier) model;
                            /*
                            * trainHeader = ((PMMLClassifier)classifier).getMiningSchema().
                            * getMiningSchemaAsInstances();
                            */
                        } else {
                            throw new Exception(
                                    "PMML model is not a classification/regression model!");
                        }
                    } else {
                        if (selected.getName().endsWith(".gz")) {
                            is = new GZIPInputStream(is);
                        }
                        // ObjectInputStream objectInputStream = new ObjectInputStream(is);
                        ObjectInputStream objectInputStream
                                = SerializationHelper.getObjectInputStream(is);
                        //System.out.println("STRING--> " + objectInputStream.toString());
                        while (true) {
                            try {
                                listClassifier.add((Classifier) objectInputStream.readObject());
                            } catch (EOFException ex) {
                                break;
                            }
                        }
                        objectInputStream.close();
                        
                        //System.out.println(listClassifier);
                        /*Classifier cl;
                        String initialDir = ExplorerDefaults.getInitialDirectory();
                        JFileChooser fileChooser = new JFileChooser(new File(initialDir));
                        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                        FileNameExtensionFilter filter = new FileNameExtensionFilter("txt file(*.txt)", "txt");
                        fileChooser.setFileFilter(filter);
                        int returnVal = fileChooser.showSaveDialog(this);
                        if (returnVal == JFileChooser.APPROVE_OPTION) {
                        try {
                        BufferedReader obj = new BufferedReader(new FileReader(fileChooser.getSelectedFile()));
                        Object strng;
                        
                        while ((strng = obj.readLine()) != null) {
                        
                        tablaAlgoritmosModel.addRow(new Object[]{(strng)});
                        
                        listClassifier.add(strng);
                        
                        }
                        } catch (IOException ex) {
                        java.util.logging.Logger.getLogger(tsStudio.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        }
                        */
                    }
                } catch (FileNotFoundException ex) {
                    java.util.logging.Logger.getLogger(tsStudio.class.getName()).log(Level.SEVERE, null, ex);
                } catch (Exception ex) {
                    java.util.logging.Logger.getLogger(tsStudio.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    try {
                        is.close();
                    } catch (IOException ex) {
                        java.util.logging.Logger.getLogger(tsStudio.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                
                for (int i = 0; i < listClassifier.size(); i++) {
                    tablaAlgoritmosModel.addRow(new Object[]{(getSpec(listClassifier.get(i)))});
                }
                experimentLabel.setText("<html><p style=\"width:100px\">" + "Current experiment: " + "Name: " + newTextField.getText() + " Datasets: " + listInstances.size() + "</br> Features: " + listFeatures.size() + " Classifiers: " + listClassifier.size() + " Validation: none Result: " + calculated + "</p></html>");
                
                //RESULTS
                folder = new File(m_FileChooser.getSelectedFile() + "/Results");
                for (File file : folder.listFiles()) {
                    System.out.println(file.getName());
                    if (file.getName().equals("Prediction Table.arff")) {
                        try {
                            //System.out.println("PREDICTION TABLE");
                            instances = ConverterUtils.DataSource.read(file.toString());
                            //System.out.println(instances);
                            for (int i = 0; i < instances.numInstances(); i++) {
                                if (instances.get(i).toString(5).equals("?") || instances.get(i).toString(6).equals("?") || instances.get(i).toString(7).equals("?")) {
                                    predictionTableModel.addRow(new Object[]{instances.get(i).toString(0), instances.get(i).toString(1), instances.get(i).toString(2), instances.get(i).toString(3), instances.get(i).toString(4), (Double.NaN), Double.NaN, Double.NaN});
                                    
                                } else {
                                    predictionTableModel.addRow(new Object[]{instances.get(i).toString(0), instances.get(i).toString(1), instances.get(i).toString(2), instances.get(i).toString(3), instances.get(i).toString(4), Double.parseDouble(instances.get(i).toString(5)), Double.parseDouble(instances.get(i).toString(6)), Double.parseDouble(instances.get(i).toString(7))});
                                }
                            }

                        } catch (Exception ex) {
                            java.util.logging.Logger.getLogger(tsStudio.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        
                    } else if (file.getName().equals("Overall Metrics.arff")) {
                        try {
                            instances = ConverterUtils.DataSource.read(file.toString());
                            //System.out.println(instances);
                            for (int i = 0; i < instances.numInstances(); i++) {
                                if (instances.get(i).toString(4).equals("?")) {
                                    if (instances.get(i).toString(5).equals("?")) {
                                        overallMetricsTableModel.addRow(new Object[]{instances.get(i).toString(0), instances.get(i).toString(1), instances.get(i).toString(2), instances.get(i).toString(3), Double.NaN, Double.NaN, instances.get(i).toString(6)});
                                    } else {
                                        overallMetricsTableModel.addRow(new Object[]{instances.get(i).toString(0), instances.get(i).toString(1), instances.get(i).toString(2), instances.get(i).toString(3), Double.NaN, instances.get(i).toString(5), instances.get(i).toString(6)});
                                        
                                    }
                                } else if (instances.get(i).toString(5).equals("?")) {
                                    overallMetricsTableModel.addRow(new Object[]{instances.get(i).toString(0), instances.get(i).toString(1), instances.get(i).toString(2), instances.get(i).toString(3), instances.get(i).toString(4), Double.NaN, instances.get(i).toString(6)});
                                }
                            }
                            
                        } catch (Exception ex) {
                            java.util.logging.Logger.getLogger(tsStudio.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    } else {
                        
                       // System.out.println("ehfefhe");
                    }
                    
                }
                BufferedReader obj = null;
                //TARGETS
                try {
                    obj = new BufferedReader(new FileReader(m_FileChooser.getSelectedFile() +"/Targets"));
                    String strng;
                    while ((strng = obj.readLine()) != null) {
                        //System.out.println(strng);
                        
                        String[] parts = strng.split(",");
                        String part1 = parts[0];
                        targets.add(new Attribute(part1, (ArrayList<String>) null));
                       // System.out.println("parte 1--> " + part1);
                        String part2 = parts[1];
                        tablaTargetModel.addRow(new Object[]{part1, part2});
                    }
                } catch (FileNotFoundException ex) {
                    java.util.logging.Logger.getLogger(tsStudio.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    java.util.logging.Logger.getLogger(tsStudio.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    try {
                        obj.close();
                    } catch (IOException ex) {
                        java.util.logging.Logger.getLogger(tsStudio.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                obj = new BufferedReader(new FileReader(m_FileChooser.getSelectedFile()+ "/Variables"));
                String strng;
                while ((strng = obj.readLine()) != null) {
                    //System.out.println(strng);
                    listVariables.setModel(modeloLista);
                    modeloLista.addElement(strng);
                }
               
                        //INPUT VARIABLES
                        
                        obj = new BufferedReader(new FileReader(m_FileChooser.getSelectedFile() + "/Input Variables"));
                        while ((strng = obj.readLine()) != null) {
                           // System.out.println("Input Variables");
                           // System.out.println(strng);
                            listaInputVariables.setModel(modeloInputVaribales);
                            modeloInputVaribales.addElement(strng);
                        }
                        
                                //SCAN STEP
                                
                                obj = new BufferedReader(new FileReader(m_FileChooser.getSelectedFile()+  "/Scan Step"));
                                while ((strng = obj.readLine()) != null) {
                                    //System.out.println(strng);
                                    if (strng.equals(radioButtonMaximum.getText())) {
                                        radioButtonMaximum.setSelected(true);
                                    } else if (strng.equals(radioButtonMinimum.getText())) {
                                        radioButtonMinimum.setSelected(true);
                                    } else {
                                        String[] parts = strng.split(",");
                                        String part1 = parts[0];
                                        //System.out.println("parte 1--> " + part1);
                                        String part2 = parts[1];
                                        radioButtonValue.setSelected(true);
                                        textFieldTimeSeries.setText(part2);
                                    }
                                }
                                
            } catch (FileNotFoundException ex) {
                java.util.logging.Logger.getLogger(tsStudio.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                java.util.logging.Logger.getLogger(tsStudio.class.getName()).log(Level.SEVERE, null, ex);
            }

        }


    }//GEN-LAST:event_loadExperimentButtonActionPerformed

    private void saveDataConfigurationButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveDataConfigurationButtonActionPerformed
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int option = fileChooser.showOpenDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {

            try {
                FileWriter fw = null;

                File file = (fileChooser.getSelectedFile());
                fw = new FileWriter(fileChooser.getSelectedFile() + "/Variables");
                for (int i = 0; i < listVariables.getModel().getSize(); i++) {
                    fw.write((String) listVariables.getModel().getElementAt(i));
                    fw.write("\n");
                }
                fw.close();

                fw = new FileWriter(fileChooser.getSelectedFile() + "/Input Variables");
                for (int i = 0; i < listaInputVariables.getModel().getSize(); i++) {
                    fw.write((String) listaInputVariables.getModel().getElementAt(i));
                    fw.write("\n");
                }
                fw.close();

                fw = new FileWriter(fileChooser.getSelectedFile() + "/Targets");
                for (int i = 0; i < tablaTarget.getRowCount(); i++) //realiza un barrido por filas.
                {
                    for (int j = 0; j < tablaTarget.getColumnCount(); j++) //realiza un barrido por columnas.
                    {
                        fw.write((String) tablaTarget.getValueAt(i, j));
                        if (j < tablaFeatures.getColumnCount() - 1) { //agrega separador "," si no es el ultimo elemento de la fila.
                            fw.write(",");
                        }

                    }
                    fw.write("\n");
                }
                fw.close();
                fw = new FileWriter(fileChooser.getSelectedFile() + "/Scan Step");

                if (radioButtonMaximum.isSelected()) {
                    //System.out.println("Maximum--> " + radioButtonMaximum.getText());
                    fw.write(radioButtonMaximum.getText());
                } else if (radioButtonMinimum.isSelected()) {
                    fw.write(radioButtonMinimum.getText());

                } else {
                    fw.write(radioButtonValue.getText());
                    fw.write(",");
                    fw.write(textFieldTimeSeries.getText());
                }
                fw.close();

            } catch (IOException ex) {
                java.util.logging.Logger.getLogger(tsStudio.class.getName()).log(Level.SEVERE, null, ex);
            }
        }


    }//GEN-LAST:event_saveDataConfigurationButtonActionPerformed

    private void loadDatasetConfigurationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadDatasetConfigurationActionPerformed
        String initialDir = ExplorerDefaults.getInitialDirectory();
        m_FileChooser = new ConverterFileChooser(new File(initialDir));
        ConverterFileChooser m_FileChooser = new ConverterFileChooser(new File(initialDir));
        Instances instances;
        m_FileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnVal = m_FileChooser.showOpenDialog(this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            try {
                try {
                    try {
                        BufferedReader obj = null;
                        //TARGETS
                        try {
                            obj = new BufferedReader(new FileReader(m_FileChooser.getSelectedFile() + "/Targets"));
                            String strng;
                            while ((strng = obj.readLine()) != null) {
                                //System.out.println(strng);

                                String[] parts = strng.split(",");
                                String part1 = parts[0];
                                targets.add(new Attribute(part1, (ArrayList<String>) null));
                                //System.out.println("parte 1--> " + part1);
                                String part2 = parts[1];
                                tablaTargetModel.addRow(new Object[]{part1, part2});
                            }
                        } catch (FileNotFoundException ex) {
                            java.util.logging.Logger.getLogger(tsStudio.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (IOException ex) {
                            java.util.logging.Logger.getLogger(tsStudio.class.getName()).log(Level.SEVERE, null, ex);
                        } finally {
                            try {
                                obj.close();
                            } catch (IOException ex) {
                                java.util.logging.Logger.getLogger(tsStudio.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                        obj = new BufferedReader(new FileReader(m_FileChooser.getSelectedFile() + "/Variables"));
                        String strng;
                        while ((strng = obj.readLine()) != null) {
                            //System.out.println(strng);
                            listVariables.setModel(modeloLista);
                            modeloLista.addElement(strng);
                        }
                    } catch (FileNotFoundException ex) {
                        java.util.logging.Logger.getLogger(tsStudio.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IOException ex) {
                        java.util.logging.Logger.getLogger(tsStudio.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    //INPUT VARIABLES
                    BufferedReader obj = null;

                    obj = new BufferedReader(new FileReader(m_FileChooser.getSelectedFile() + "/Input Variables"));
                    String strng;
                    while ((strng = obj.readLine()) != null) {
                        //System.out.println("Input Variables");
                        //System.out.println(strng);
                        listaInputVariables.setModel(modeloInputVaribales);
                        modeloInputVaribales.addElement(strng);
                    }
                } catch (FileNotFoundException ex) {
                    java.util.logging.Logger.getLogger(tsStudio.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    java.util.logging.Logger.getLogger(tsStudio.class.getName()).log(Level.SEVERE, null, ex);
                }
                //SCAN STEP
                BufferedReader obj = null;

                obj = new BufferedReader(new FileReader(m_FileChooser.getSelectedFile() + "/Scan Step"));
                String strng;
                while ((strng = obj.readLine()) != null) {
                    //System.out.println(strng);
                    if (strng.equals(radioButtonMaximum.getText())) {
                        radioButtonMaximum.setSelected(true);
                    } else if (strng.equals(radioButtonMinimum.getText())) {
                        radioButtonMinimum.setSelected(true);
                    } else {
                        String[] parts = strng.split(",");
                        String part1 = parts[0];
                        //System.out.println("parte 1--> " + part1);
                        String part2 = parts[1];
                        radioButtonValue.setSelected(true);
                        textFieldTimeSeries.setText(part2);
                    }
                }
            } catch (FileNotFoundException ex) {
                java.util.logging.Logger.getLogger(tsStudio.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                java.util.logging.Logger.getLogger(tsStudio.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_loadDatasetConfigurationActionPerformed

    private void savePDFGraph() {
        String initialDir = ExplorerDefaults.getInitialDirectory();
        JFileChooser fileChooser = new JFileChooser(new File(initialDir));
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        FileNameExtensionFilter filter = new FileNameExtensionFilter("PDF", "pdf");
        fileChooser.setFileFilter(filter);
        int returnVal = fileChooser.showSaveDialog(this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File f = new File(fileChooser.getSelectedFile() + ".pdf");
            PDFDocument pdfDoc = new PDFDocument();

            Page page = pdfDoc.createPage(new Rectangle(1280, 720));
            PDFGraphics2D g2 = page.getGraphics2D();

            chart.draw(g2, new Rectangle(0, 0, 1280, 720));
            pdfDoc.writeToFile(f);
        }
    }

    private void savePNGGraph() {
        String initialDir = ExplorerDefaults.getInitialDirectory();
        JFileChooser fileChooser = new JFileChooser(new File(initialDir));
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        FileNameExtensionFilter filter = new FileNameExtensionFilter("PNG", "png");
        fileChooser.setFileFilter(filter);
        int returnVal = fileChooser.showSaveDialog(this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File f = new File(fileChooser.getSelectedFile() + ".png");
            try {
                //ChartUtilities.saveChartAsPNG(f, chart, 600, 400);
                ChartUtilities.saveChartAsPNG(f, chart, 1280, 720);
            } catch (IOException ex) {
                // java.util.logging.Logger.getLogger(AttrSelExp.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void saveArffTable(Instances saveInst) throws Exception {
        String initialDir = ExplorerDefaults.getInitialDirectory();
        JFileChooser fileChooser = new JFileChooser(new File(initialDir));
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Arff data files (*.arff)", "arff");
        fileChooser.setFileFilter(filter);
        int returnVal = fileChooser.showSaveDialog(this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            ConverterUtils.DataSink.write(fileChooser.getSelectedFile() + ".arff", saveInst);
        }
    }

    protected void startExp() throws InterruptedException, ExecutionException, Exception {
        m_CEPanel.addToHistory();
        Date dateFrom = new Date();
        Date dateTo = new Date();
        String dateFromString = new String();
        String dateToString = new String();
        SimpleDateFormat format = new SimpleDateFormat(timeFormatComboBox.getSelectedItem().toString());
        //System.out.println("ENTRA EN STARTEXP");
        if (absoluteRadioButton.isSelected()) {
            eleccion = 0;
        } else if (relativeRadioButton.isSelected()) {
            eleccion = 1;

            //System.out.println("RELATIVE");
        } else {
            eleccion = 2;

            dateFrom = dateChooserFrom.getDate();
            dateFromString = format.format(dateFrom);
            dateTo = dateChooserTo.getDate();
            dateToString = format.format(dateTo);

        }

        if (jRadioButton4.isSelected()) {
            eleccionTrainingIncrement = 0;
            //System.out.println("OPCION TRAINING INCREMENT");
        } else if (jRadioButton5.isSelected()) {
            eleccionTrainingIncrement = 1;
            //System.out.println("OPCION NO");

        }

        //System.out.println("Eleccion--> " + eleccion);
        //System.out.println("absoluteRadioButton--> " + absoluteTextField.getText());
        double result;
        TimeSerie ts = new TimeSerie();
        m_log.logMessage("Start of experimentation");
        LinkedList<List> data = (LinkedList<List>) datasets.clone();

        int numThreads = 4;
        int numObjects = listInstances.size() * listClassifier.size();

        if (!numThreadsTextField.getText().trim().equals("")) {
            try {
                numThreads = Integer.parseInt(numThreadsTextField.getText().trim());
            } catch (NumberFormatException ex) {
                m_log.logMessage(ex.getMessage());
            }
        }
        //int finalPosition = data.size() - 1;
        //for (int z = 0; z < listFeatures.size(); z++) {
        //    data.addFirst(new ArrayList());
        // }
        int lag = Integer.parseInt(sizeTextFieldFeature.getText().trim());
        if (radioButtonValue.isSelected()) {
            mHorizon = Integer.parseInt(textFieldTimeSeries.getText());
            for (int i = 0; i < tablaTarget.getRowCount(); i++) {
                String a = (String) tablaTarget.getValueAt(i, 1);
                int prueba = Integer.parseInt(a.trim());
                horizon.add(i, prueba);
                //System.out.println("Horizon: " + horizon.get(i));
            }
        }
        prepositionalInstances = ts.nuevo(listInstances, mHorizon, horizon, lag * (-1), targets, timeFormatComboBox.getSelectedItem().toString(), listFeatures, variableComboFunctionFeature.getSelectedItem().toString(), nombreCustomFeature);

        //System.out.println("Numero instancias: " + listInstances.get(0).numInstances());
        /* for (int m = 0; m < listInstances.size(); m++) {

            System.out.println("size de listInstances-->  " + listInstances.size());
            for (int z = 0; z < listFeatures.size(); z++) {
                System.out.println("Functions[" + z + "]--> " + functions.get(z));
                System.out.println("AAAAAAAAAAAAAAAAA");
                //trainDataset.get(m).add(new ArrayList());
                System.out.println("EEEEEEEEEEEE");
                //int finalPosition = datasets.size() - 1;
                //datasets.get(finalPosition+(z+1)).add(variableComboFunctionFeature.getSelectedItem().toString() + "_"+functions.get(z)+";");
                //trainDataset.get(m).get(finalPosition).add(variableComboFunctionFeature.getSelectedItem().toString() + "_"+functions.get(z)+";");
                int i = 0;
                System.out.println("i--> " + i);
                while (i < listInstances.get(m).numAttributes()) {
                    if (variableComboFunctionFeature.getSelectedItem().toString().equals(listInstances.get(m).attribute(i).name())) {

                        for (int k = 0; k < listInstances.get(m).numInstances(); k = k + mHorizon) {
                            int index = listInstances.get(m).attribute(i).index();
                            result = cf.eval(listFeatures.get(z), k, listInstances, index, m);
                            // datasets.get(finalPosition+(z+1)).add(result + ";");
                            data.get(z).add((Double.toString(result)));
                            // sum.sum(listInstances, Integer.parseInt(shiftTextFeature.getText()), Integer.parseInt(shiftTextFeature1.getText()), variableComboFunctionFeature.getSelectedItem().toString(), trainDataset, mHorizon);
                        }

                    }

                    i++;
                }

            }
            //for (int i = 0; i <= trainDataset.get(m).get(m).size() - 1; i++) {
            //for (int j = 0; j < trainDataset.get(m).size(); j++) {

            //    System.out.print(trainDataset.get(m).get(j).get(i));
            // }
            // System.out.println("");
            // }
            System.out.println("FINISH");

       /Final}*/
        // for (int i = 0; i <= trainDataset.get(1).get(0).size() - 1; i++) {
        //   for (int j = 0; j < trainDataset.get(0).size(); j++) {
        //     System.out.print(trainDataset.get(1).get(j).get(i));
        //  }
        //  System.out.println("");
        //  }
        /* for (int i = 0; i <= datasets.get(0).size() - 1; i++) {
            for (int j = 0; j < datasets.size(); j++) {
                System.out.print(datasets.get(j).get(i));
            }
            System.out.println("");
        }
         */
        //List<List> prueba = new ArrayList();
        //ArrayList<Attribute> atts = new ArrayList<>();
        //Instances> listaPrepositionalInstances = new ArrayList();
        //Instances p;
        //System.out.println("DATASETS.SIZE()--> " + data.size());
        //p = new Instances("P", atts, 0);
        //listaPrepositionalInstances.add(new Instances("P1", atts, 0));
        //Instance pepe = new DenseInstance(p.numAttributes());
        //System.out.println("Llega aqui");
        //double[] vals = new double[p.numAttributes()];
        /* for (int i = 1; i <= 40; i++) {
            for (int j = 0; j < datasets.size(); j++) {
                if(datasets.get(j).get(i) == "?;"){
                    System.out.println("Primer if--> "+datasets.get(j).get(i));
                    pepe.setMissing(atts.get(j));
                }
                else if(atts.get(j).isString()){

                    pepe.setValue(atts.get(j),(String) datasets.get(j).get(i));
            }
                else{
                    pepe.setValue(atts.get(j), Double.parseDouble((String) datasets.get(j).get(i)));
                }
            }
            p.add(pepe);
        }
         */
 /* for (int i = 1; i < data.get(0).size(); i++) {
            double[] vals = new double[p.numAttributes()];
            for (int j = 0; j < data.size(); j++) {
                if (data.get(j).get(i).toString().contains("_")) {

                } else {
                    if (data.get(j).get(i) == "?;") {
                        System.out.println("Primer if--> " + data.get(j).get(i));
                        // pepe.setMissing(atts.get(j));
                        vals[j] = Double.NaN;

                    } else if (atts.get(j).isDate()) {
                        try {
                            if (data.get(j).get(i).toString().contains("'")) {
                                String valor = data.get(j).get(i).toString().replaceAll("'", "");
                                vals[j] = p.attribute(j).parseDate(valor);
                                //System.out.println(valor);
                            } else {
                                vals[j] = p.attribute(j).parseDate((String) data.get(j).get(i));
                            }
                            // pepe.setValue(atts.get(j),(String) datasets.get(j).get(i));
                        } catch (ParseException ex) {
                            java.util.logging.Logger.getLogger(tsStudio.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    } else {
                        vals[j] = Double.parseDouble((String) data.get(j).get(i));

                        //pepe.setValue(atts.get(j), Double.parseDouble((String) datasets.get(j).get(i)));
                    }
                }
            }

            // System.out.println("Vals--> " + vals[0]);
            p.add(new DenseInstance(1.0, vals));
            //listaPrepositionalInstances.get(0).add(new DenseInstance(1.0, vals));
            //System.out.println("ListPrepositionalInstances-->: " + listaPrepositionalInstances.get(0));
       //final }
         */
       // System.out.println("Llega hasta aqui");
        int contadorTargetGeneral = 0;

        List<List<List<List>>> maeByHorizon = new ArrayList();
        List<List<List<List<List>>>> maeTrainingIncrement = new ArrayList();
        List<List<List<List>>> rmseByHorizon = new ArrayList();
        List<List<List<List<List>>>> rmseTrainingIncrement = new ArrayList();
        List<List<List<List>>> r2ByHorizon = new ArrayList();
        List<List<List<List<List>>>> r2TrainingIncrement = new ArrayList();
        List<List<List<List<List<List<Prediction>>>>>> valuesPredictTrainingIncrement = new ArrayList();

        List<List<String>> classifiersPrintByHorizon = new ArrayList();
        List<List<List<List<List<Prediction>>>>> valuesPredictGeneral = new ArrayList();
        List<List<String>> fechasInstances = new ArrayList<List<String>>();
        List<Instances> datasetsSinFecha = new ArrayList();

        for (int m = 0; m < prepositionalInstances.size(); m++) {
            List<String> fechas = new ArrayList();
            for (int i = 0; i < prepositionalInstances.get(m).numInstances(); i++) {
                fechas.add(prepositionalInstances.get(m).get(i).toString(0));

            }
            fechasInstances.add(fechas);
        }
        //System.out.println("Llega hasta aqui");
        //System.out.println("FechasInstances--> " + fechasInstances);
        for (int m = 0; m < prepositionalInstances.size(); m++) {
            ArrayList<Attribute> atts = new ArrayList<>();
            for (int i = 1; i < prepositionalInstances.get(m).numAttributes(); i++) {
                atts.add(new Attribute(prepositionalInstances.get(m).attribute(i).name()));
            }
            datasetsSinFecha.add(new Instances(prepositionalInstances.get(m).relationName(), atts, 0));
        }
        for (int m = 0; m < prepositionalInstances.size(); m++) {

            for (int i = 0; i < prepositionalInstances.get(m).numInstances(); i++) {
                double[] vals = new double[datasetsSinFecha.get(m).numAttributes()];

                for (int j = 0; j < datasetsSinFecha.get(m).numAttributes(); j++) {
                    vals[j] = prepositionalInstances.get(m).get(i).value((j + 1));
                }
                datasetsSinFecha.get(m).add(new DenseInstance(1.0, vals));

            }
        }

        for (int m = 0; m < prepositionalInstances.size(); m++) {
            int indiceFechaFrom = 0;
            int indiceFechaTo = 0;
            for (int i = 0; i < prepositionalInstances.get(m).numInstances(); i++) {
                if (dateFromString.equals(prepositionalInstances.get(m).get(i).toString(0))) {
                    //System.out.println("DateFromString--> " + prepositionalInstances.get(m).get(i).toString(0));
                    indiceFechaFrom = i;
                }
                if (dateToString.equals(prepositionalInstances.get(m).get(i).toString(0))) {
                    //System.out.println("DateToString--> " + prepositionalInstances.get(m).get(i).toString(0));
                    indiceFechaTo = i;

                }
            }
            maeByHorizon.add(new ArrayList());
            maeTrainingIncrement.add(new ArrayList());
            rmseTrainingIncrement.add(new ArrayList());
            r2TrainingIncrement.add(new ArrayList());
            rmseByHorizon.add(new ArrayList());
            r2ByHorizon.add(new ArrayList());
            valuesPredictGeneral.add(new ArrayList());
            valuesPredictTrainingIncrement.add(new ArrayList());

            for (int z = 0; z < listClassifier.size(); z++) {
                maeTrainingIncrement.get(m).add(new ArrayList());
                rmseTrainingIncrement.get(m).add(new ArrayList());
                r2TrainingIncrement.get(m).add(new ArrayList());
                maeByHorizon.get(m).add(new ArrayList());
                rmseByHorizon.get(m).add(new ArrayList());
                r2ByHorizon.get(m).add(new ArrayList());
                valuesPredictGeneral.get(m).add(new ArrayList());
                valuesPredictTrainingIncrement.get(m).add(new ArrayList());

                for (int i = 0; i < targets.size(); i++) {
                    maeByHorizon.get(m).get(z).add(new ArrayList());
                    maeTrainingIncrement.get(m).get(z).add(new ArrayList());
                    rmseTrainingIncrement.get(m).get(z).add(new ArrayList());
                    r2TrainingIncrement.get(m).get(z).add(new ArrayList());

                    rmseByHorizon.get(m).get(z).add(new ArrayList());
                    r2ByHorizon.get(m).get(z).add(new ArrayList());
                    valuesPredictGeneral.get(m).get(z).add(new ArrayList());
                    valuesPredictTrainingIncrement.get(m).get(z).add(new ArrayList());

                    //System.out.println("horizon--> " + horizon.get(i));
                    for (int ho = 0; ho < horizon.get(i); ho++) {
                        maeTrainingIncrement.get(m).get(z).get(i).add(new ArrayList());
                        rmseTrainingIncrement.get(m).get(z).get(i).add(new ArrayList());
                        r2TrainingIncrement.get(m).get(z).get(i).add(new ArrayList());
                        valuesPredictTrainingIncrement.get(m).get(z).get(i).add(new ArrayList());

                    }
                }
            }
            classifiersPrintByHorizon.add(new ArrayList());
            List<String> targets = new ArrayList();
            List<Integer> horizons = new ArrayList();
            Collection<FeatureClassifierAndValidations> concurrentExp = new LinkedList<>();
            List<List<Prediction>> valuesPredict = new ArrayList();
            List mae = new ArrayList();
            List RMSE = new ArrayList();
            List R2 = new ArrayList();
            //System.out.println("Instance de prueba--> " + p);
            //System.out.println("Dataset.size()--> " + p.numInstances());
            int contadorObject = 0;

            List<String> classifiersPrint = new ArrayList();
            while (contadorObject < numObjects) {
                for (int z = 0; z < listClassifier.size(); z++) {

                    int contadorTarget = 0;
                    int contadorHorizon = 0;
                    int cc = targets.size();
                    int ccc = 0;
                    for (int i = 0; i < datasetsSinFecha.get(0).numAttributes(); i++) {
                        if (datasetsSinFecha.get(m).attribute(i).name().contains("ahead")) {

                           // System.out.println("\\\\\\\\\\ " + datasetsSinFecha.get(m).attribute(i).name().replaceAll("_ahead", ""));
                            datasetsSinFecha.get(m).setClassIndex(i);
                            targets.add(datasetsSinFecha.get(m).attribute(i).name().replaceAll("_ahead", ""));
                            contadorHorizon++;
                            res = new LinkedList<Future<ResultsExp>>();
                            //System.out.println("Entra hasta aqui");
                            executor = Executors.newFixedThreadPool(numThreads);

                            Classifier cls = (Classifier) listClassifier.get(z);
                           // System.out.println("JUSTO ANTES DEL CONCURRENTEXP");

                            if (eleccion == 0) {
                                if (eleccionTrainingIncrement == 0) {
                                    concurrentExp = new LinkedList<>();
                                    //System.out.println("OPCION CON TRAINING INCREMENT");
                                    for (int per = Integer.parseInt(absoluteTextField.getText()); per < datasetsSinFecha.get(m).numInstances(); per = per + Integer.parseInt(trainingIncrementTextField.getText())) {
                                       // System.out.println("per--> " + per);
                                        concurrentExp.add(new FeatureClassifierAndValidations(datasetsSinFecha.get(m), progressExp, numThreads, m_log, cls, per, eleccion, 50, 0, 0, Integer.parseInt(trainingIncrementTextField.getText()), 1));
                                    }
                                    //System.out.println("Vuelve al run principal");
                                    res = executor.invokeAll(concurrentExp);
                                    //System.out.println("Res.size()--> " + res.size());
                                    //System.out.println("Hace el executor");
                                    for (int resSize = 0; resSize < res.size(); resSize++) {
                                        //System.out.println("--- " + res.get(resSize));
                                        //Hacert lista para guardar los objectResults
                                        Future<ResultsExp> objectResult = res.get(resSize);
                                        //System.out.println("Classifier--> " + getSpec(objectResult.get().getClassifier()));
                                        classifiersPrint.add(getSpec(objectResult.get().getClassifier()));
                                        //System.out.println("classifierPrint.size()--> " + classifiersPrint.size());
                                        Evaluation evalResult = objectResult.get().getEvalClassifier();
                                        mae.add(evalResult.meanAbsoluteError());
                                       // System.out.println("MAE NORMAL--> " + mae);
                                        // System.out.println("contadorTarget--> " + contadorTarget);
                                        //System.out.println("ContadorHorizon--> " + contadorHorizon + "|| horizon.get(contadorTarget)--> " + horizon.get(contadorTarget));
                                        //en el maebyhorizon falla
                                        //maeByHorizon.get(m).get(z).get(contadorTarget).add(evalResult.meanAbsoluteError());
                                        maeTrainingIncrement.get(m).get(z).get(contadorTarget).get(contadorHorizon - 1).add(evalResult.meanAbsoluteError());

                                        //System.out.println("M--> "+maeByHorizon.get(m).get(z).get(contadorTarget));
                                        //System.out.println("MaeTrainingIncrement--> " + maeTrainingIncrement);

                                        RMSE.add(evalResult.rootMeanSquaredError());
                                       // System.out.println("RMSE Normal--> " + RMSE);
                                        // System.out.println("pasa el rmse");
                                        rmseTrainingIncrement.get(m).get(z).get(contadorTarget).get(contadorHorizon - 1).add(evalResult.rootMeanSquaredError());
                                        // System.out.println("pasa el rmseByHorizon");
                                        //System.out.println("RMSETrainingIncrement--> " + rmseTrainingIncrement);
                                        R2.add(evalResult.correlationCoefficient());
                                        r2TrainingIncrement.get(m).get(z).get(contadorTarget).get(contadorHorizon - 1).add(evalResult.correlationCoefficient());
                                        valuesPredictGeneral.get(m).get(z).get(contadorTarget).add(evalResult.predictions());
                                        valuesPredictTrainingIncrement.get(m).get(z).get(contadorTarget).get(contadorHorizon - 1).add(evalResult.predictions());
                                        //System.out.println("valuesPredictTrainingIncrement--> " + valuesPredictTrainingIncrement.get(m).get(z).get(contadorTarget).get(contadorHorizon - 1).size());
                                        //System.out.println("valuesPredictTrainingIncrement--> " + valuesPredictTrainingIncrement.get(m).get(z).get(contadorTarget).size());
                                        //System.out.println("valuesPredictTrainingIncrement--> " + valuesPredictTrainingIncrement.get(m).get(z).get(contadorTarget).get(contadorHorizon - 1).get(0).size());
                                        //System.out.println("valuesPredictTrainingIncrement--> " + valuesPredictTrainingIncrement.get(m).get(z).get(contadorTarget).get(contadorHorizon - 1));

                                        //System.out.println("valuesPredictGeneral--> " + valuesPredictGeneral);
                                        //System.out.println("contadorTarget--> " + contadorTarget);
                                        //System.out.println("ContadorHorizon--> " + contadorHorizon + "|| horizon.get(contadorTarget)--> " + horizon.get(contadorTarget));
                                        valuesPredict.add(evalResult.predictions());
                                        //System.out.println("valuesPredict--> " + valuesPredict);
                                    }
                                    if (contadorHorizon == horizon.get(contadorTarget)) {
                                        contadorTarget++;
                                        contadorHorizon = 0;
                                    }
                                    contadorTargetGeneral = contadorTarget;

                                    //System.out.println("contadorObject--> " + contadorObject);
                                    contadorObject++;
                                } else {
                                    concurrentExp.add(new FeatureClassifierAndValidations(datasetsSinFecha.get(m), progressExp, numThreads, m_log, cls, Integer.parseInt(absoluteTextField.getText()), eleccion, 50, 0, 0, 0, eleccionTrainingIncrement));
                                    res = executor.invokeAll(concurrentExp);
                                    //System.out.println("Res.size()--> " + res.size());
                                    //System.out.println("Hace el executor");

                                   // System.out.println("--- " + res.get(contadorObject));
                                    //Hacert lista para guardar los objectResults
                                    Future<ResultsExp> objectResult = res.get(contadorObject);
                                   // System.out.println("Classifier--> " + getSpec(objectResult.get().getClassifier()));
                                   // System.out.println("pruebaSout");
                                    classifiersPrint.add(getSpec(objectResult.get().getClassifier()));

                                    Evaluation evalResult = objectResult.get().getEvalClassifier();
                                    mae.add(evalResult.meanAbsoluteError());
                                   // System.out.println("pasa el mae");
                                   // System.out.println("contadorTarget--> " + contadorTarget);
                                   // System.out.println("ContadorHorizon--> " + contadorHorizon + "|| horizon.get(contadorTarget)--> " + horizon.get(contadorTarget));
                                    //en el maebyhorizon falla
                                    maeByHorizon.get(m).get(z).get(contadorTarget).add(evalResult.meanAbsoluteError());
                                    //System.out.println("MaeByHorizon--> " + maeByHorizon);
                                   // System.out.println("1");
                                    RMSE.add(evalResult.rootMeanSquaredError());
                                   // System.out.println("pasa el rmse");
                                    rmseByHorizon.get(m).get(z).get(contadorTarget).add(evalResult.rootMeanSquaredError());
                                   // System.out.println("pasa el rmseByHorizon");
                                   // System.out.println("2");
                                    R2.add(evalResult.correlationCoefficient());
                                    r2ByHorizon.get(m).get(z).get(contadorTarget).add(evalResult.correlationCoefficient());
                                    valuesPredictGeneral.get(m).get(z).get(contadorTarget).add(evalResult.predictions());
                                   // System.out.println("contadorTarget--> " + contadorTarget);
                                   // System.out.println("ContadorHorizon--> " + contadorHorizon + "|| horizon.get(contadorTarget)--> " + horizon.get(contadorTarget));
                                    if (contadorHorizon == horizon.get(contadorTarget)) {
                                        contadorTarget++;
                                        contadorHorizon = 0;
                                    }

                                    valuesPredict.add(evalResult.predictions());
                                    //System.out.println("valuePredict--> "+valuesPredict);
                                   // System.out.println("contadorObject--> " + contadorObject);
                                    contadorObject++;
                                }
                            } else if (eleccion == 1) {
                                concurrentExp.add(new FeatureClassifierAndValidations(datasetsSinFecha.get(m), progressExp, numThreads, m_log, cls, 25, eleccion, Integer.parseInt(relativeTextField.getText()), 0, 0, 0, 0));
                                res = executor.invokeAll(concurrentExp);
                                //System.out.println("Res.size()--> " + res.size());
                               // System.out.println("Hace el executor");

                               // System.out.println("--- " + res.get(contadorObject));
                                //Hacert lista para guardar los objectResults
                                Future<ResultsExp> objectResult = res.get(contadorObject);
                               // System.out.println("Classifier--> " + getSpec(objectResult.get().getClassifier()));
                               // System.out.println("pruebaSout");
                                classifiersPrint.add(getSpec(objectResult.get().getClassifier()));

                                Evaluation evalResult = objectResult.get().getEvalClassifier();
                                mae.add(evalResult.meanAbsoluteError());
                                //System.out.println("pasa el mae");
                                //System.out.println("contadorTarget--> " + contadorTarget);
                                //System.out.println("ContadorHorizon--> " + contadorHorizon + "|| horizon.get(contadorTarget)--> " + horizon.get(contadorTarget));
                                //en el maebyhorizon falla
                                maeByHorizon.get(m).get(z).get(contadorTarget).add(evalResult.meanAbsoluteError());
                                //System.out.println("MaeByHorizon--> " + maeByHorizon);
                               // System.out.println("1");
                                RMSE.add(evalResult.rootMeanSquaredError());
                               // System.out.println("pasa el rmse");
                                rmseByHorizon.get(m).get(z).get(contadorTarget).add(evalResult.rootMeanSquaredError());
                               // System.out.println("pasa el rmseByHorizon");
                               // System.out.println("2");
                                R2.add(evalResult.correlationCoefficient());
                                r2ByHorizon.get(m).get(z).get(contadorTarget).add(evalResult.correlationCoefficient());
                                valuesPredictGeneral.get(m).get(z).get(contadorTarget).add(evalResult.predictions());
                                //System.out.println("contadorTarget--> " + contadorTarget);
                               // System.out.println("ContadorHorizon--> " + contadorHorizon + "|| horizon.get(contadorTarget)--> " + horizon.get(contadorTarget));
                                if (contadorHorizon == horizon.get(contadorTarget)) {
                                    contadorTarget++;
                                    contadorHorizon = 0;
                                }

                                valuesPredict.add(evalResult.predictions());
                                //System.out.println("valuePredict--> " + valuesPredict);
                                //System.out.println("contadorObject--> " + contadorObject);
                                contadorObject++;
                            } else {
                                concurrentExp.add(new FeatureClassifierAndValidations(datasetsSinFecha.get(m), progressExp, numThreads, m_log, cls, 25, eleccion, 50, indiceFechaFrom, indiceFechaTo, 0, 0));

                            }
                            /*
                            System.out.println("Vuelve al run principal");
                            res = executor.invokeAll(concurrentExp);
                            System.out.println("Res.size()--> " + res.size());
                            System.out.println("Hace el executor");

                            System.out.println("--- " + res.get(contadorObject));
                            //Hacert lista para guardar los objectResults
                            Future<ResultsExp> objectResult = res.get(contadorObject);
                            System.out.println("Classifier--> " + getSpec(objectResult.get().getClassifier()));
                            System.out.println("pruebaSout");
                            classifiersPrint.add(getSpec(objectResult.get().getClassifier()));

                            Evaluation evalResult = objectResult.get().getEvalClassifier();
                            mae.add(evalResult.meanAbsoluteError());
                            System.out.println("pasa el mae");
                            System.out.println("contadorTarget--> " + contadorTarget);
                            System.out.println("ContadorHorizon--> " + contadorHorizon + "|| horizon.get(contadorTarget)--> " + horizon.get(contadorTarget));
                            //en el maebyhorizon falla
                            maeByHorizon.get(m).get(z).get(contadorTarget).add(evalResult.meanAbsoluteError());
                            System.out.println("MaeByHorizon--> " + maeByHorizon);
                            System.out.println("1");
                            RMSE.add(evalResult.rootMeanSquaredError());
                            System.out.println("pasa el rmse");
                            rmseByHorizon.get(m).get(z).get(contadorTarget).add(evalResult.rootMeanSquaredError());
                            System.out.println("pasa el rmseByHorizon");
                            System.out.println("2");
                            R2.add(evalResult.correlationCoefficient());
                            r2ByHorizon.get(m).get(z).get(contadorTarget).add(evalResult.correlationCoefficient());
                            valuesPredictGeneral.get(m).get(z).get(contadorTarget).add(evalResult.predictions());
                            System.out.println("contadorTarget--> " + contadorTarget);
                            System.out.println("ContadorHorizon--> " + contadorHorizon + "|| horizon.get(contadorTarget)--> " + horizon.get(contadorTarget));
                            if (contadorHorizon == horizon.get(contadorTarget)) {
                                contadorTarget++;
                                contadorHorizon = 0;
                            }

                            valuesPredict.add(evalResult.predictions());
                            System.out.println("valuePredict--> "+valuesPredict);
                            System.out.println("contadorObject--> " + contadorObject);
                            contadorObject++;
                             */
                        }
                    }
                    classifiersPrintByHorizon.get(m).add(getSpec(listClassifier.get(z)));
                    // Atributos, diciendo nombre y posibles valores. Si no se indica
                    // nada, son numéricos.
                    // Creamos el conjunto de datos, de momento vacío, indicando qué atributos
                    // van a llevar.        
                    // for (int j = 0; j < datasets.size(); j++) {
                    //     prueba.add(new ArrayList());
                    //  }

                    /*for (int i = 0; i < datasets.size(); i++) {
            for (int j = 0; j <= datasets.get(0).size() - 1; j++) {
                if (isNumeric(datasets.get(i).get(j).toString())) {
                    prueba.get(i).add((datasets.get(i).get(j)));
                } else {
                    prueba.get(i).add((datasets.get(i).get(j)));
                }
            }
        }

        System.out.println("--------PRUEBA---------");
        for (int i = 0; i <= prueba.get(0).size() - 1; i++) {
            for (int j = 0; j < prueba.size(); j++) {

                System.out.print(prueba.get(j).get(i) + ";");
            }
            System.out.println("");
        }

        /* System.out.println("trainDataset.get(1)--> "+trainDataset.get(1));
        System.out.println("trainDataset.get(1)--->"+trainDataset.get(1));
        System.out.println("trainDataset.get(0).get(1)---> "+trainDataset.get(0).get(1));
        System.out.println("trainDataset.size()--> "+trainDataset.size());
        // me.sd(listInstances, Integer.parseInt(shiftTextFeature.getText()), Integer.parseInt(shiftTextFeature1.getText()), variableComboFunctionFeature.getSelectedItem().toString(), trainDataset, mHorizon);
                     */
                    //falta case "shift"
                    /* String results = "Esto es una prueba";
        System.out.println("Entra en el RUN A TOPE");
        Instances train,newTrain,newTest;
        Instances test;
        Classifier cls = null;
        Evaluation eval = null;

        try {
            cls = AbstractClassifier.makeCopy((Classifier) listClassifier.get(0));
            System.out.println("Pasa el cls");
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(FeatureClassifierAndValidations.class.getName()).log(Level.SEVERE, null, ex);
        }
                    int trainSize = (int) Math.round(p.numInstances() -30);
                    int testSize = p.numInstances() - trainSize;
                    
                    //build train and test
                    train = new Instances(p, 0, trainSize);
                    test = new Instances(p, trainSize, testSize);
                    
                    //build filter for AttributeSelection
                     //no attribute selection
                        newTrain = train;
                        newTest = test;
                    
                        System.out.println("Justo antes del classifier");
                    //Classifier
                    cls.buildClassifier(newTrain);
                    System.out.println("CONSTRUYE EL CLASSIFIER");
                    eval = new Evaluation(newTrain);
                    eval.evaluateModel(cls, newTest);
                    
                    System.out.println("NEW TEST---> "+newTest);
                    System.out.println("NEW TRAIN--> "+newTrain);
                    
                    System.out.println("Actual "+eval.predictions());
                    

                    int numAttr = newTest.numAttributes();
                     */
                    //System.out.println("DS--> "+dS);
                }
            }
            //System.out.println("Size valuePredict--> " + valuesPredict.size());
            //System.out.println("PRUEBA---> " + valuesPredict.get(0));
            // System.out.println(valuesPredict);

            executor.shutdown();
            //System.out.println("datasetsSinFechaInstances--> " + datasetsSinFecha.get(0).numInstances());
            progressExp.setValue(100);
            calculated = "calculated";
            experimentLabel.setText("<html><p style=\"width:100px\">" + "Name: " + newTextField.getText() + "<br> Datasets: " + listInstances.size() + "<br> Features: " + listFeatures.size() + "<br>  Classifiers: " + listClassifier.size() + "<br>  Validation: Absolute" + "<br> Result: " + calculated + "</p></html>");

            System.out.println("Llega al 100%");
            runStopButton.setFocusPainted(false);
            runStopButton.setText("Run");
            prepositionalDatasets.addAll(datasetsSinFecha);
            fechasPrepositionalDatasets.addAll(fechasInstances);
            classifiersPrintGeneral.addAll(classifiersPrintByHorizon);
            if (eleccionTrainingIncrement == 0) {
                printPredictionTrainingIncrementTable(valuesPredictTrainingIncrement, classifiersPrint, datasetsSinFecha.get(m), targets, fechasInstances, m, contadorTargetGeneral);
                printOverallMetricsTrainingIncrementTable(maeTrainingIncrement, rmseTrainingIncrement, r2TrainingIncrement, valuesPredict, classifiersPrint, datasetsSinFecha.get(m), targets, fechasInstances, m, contadorTargetGeneral);

            } else {
                printPredictionTable(valuesPredict, classifiersPrint, datasetsSinFecha.get(m), targets, fechasInstances, m);
                printOverallMetricsTable(mae, RMSE, R2, valuesPredict, classifiersPrint, datasetsSinFecha.get(m), targets, fechasInstances, m);

            }
        }
        maeGeneral.addAll(maeByHorizon);
        maeGeneralTrainingIncrement.addAll(maeTrainingIncrement);
        rmseGeneral.addAll(rmseByHorizon);
        rmseGeneralTrainingIncrement.addAll(rmseTrainingIncrement);
        r2General.addAll(r2ByHorizon);
        r2GeneralTrainingIncrement.addAll(r2TrainingIncrement);
        valuesPredictPlotTrainingIncrement.addAll(valuesPredictTrainingIncrement);
        valuesPredictPlot.addAll(valuesPredictGeneral);
        printMetricsByHorizonTable(maeByHorizon, rmseByHorizon, r2ByHorizon, datasetsSinFecha, targets, classifiersPrintByHorizon);
        printGrafica(valuesPredictGeneral, fechasInstances, datasetsSinFecha);

        //BufferedImage image = chart.createBufferedImage(600, 400);
        //ImageIO.write(image, "png", new File("xy-chart.png"));
    }

    private void printGrafica(List<List<List<List<List<Prediction>>>>> valuesPredict, List<List<String>> fechas, List<Instances> datasets) throws ParseException {
        int numeroDatasets = 0;
        int numeroTargets = 0;
        for (int m = 0; m < datasets.size(); m++) {
            datasetPlotComboBox.addItem(datasets.get(m).relationName());

        }
        for (int z = 0; z < listClassifier.size(); z++) {
            algorithmPlotComboBox.addItem(getSpec(listClassifier.get(z)));
        }

        for (int i = 0; i < targets.size(); i++) {
            targetPlotComboBox.addItem(targets.get(i).name());
            numeroTargets = i;
        }
        numeroDatasets = datasets.size();

        /*DefaultXYDataset datasetPrueba = new DefaultXYDataset();
        List<String> fechaPrueba = new ArrayList();
        TimeSeries ts = new TimeSeries("Prueba");
        TimeSeriesCollection tsc = new TimeSeriesCollection();
        int indexTarget;
        SimpleDateFormat formato = new SimpleDateFormat(timeFormatComboBox.getSelectedItem().toString());
        
        for (int i = 0; i < fechas.get(0).size(); i++) {
            fechaPrueba.add((fechas.get(0).get(i)));
        }
        
        System.out.println("fechaPrueba--> " + fechaPrueba);
        System.out.println(fechaPrueba.get(0));
        
        for (int k = 0; k < 20; k++) {
            System.out.println("entra al for");
            String[] g = fechaPrueba.get(k).split("-");
            int dia = Integer.parseInt(g[2])+k;
            int mes = Integer.parseInt(g[1]);
            int año = Integer.parseInt(g[0]);
            System.out.println("Dia/Mes/Año: " + dia + "/" + mes + "/" + año);
            ts.add(new Day(dia, mes, año), 50+k);
            System.out.println("k-> " + k);
        }
        System.out.println("llega");
        tsc.addSeries(ts);
        System.out.println("2");
        double[][] prueba = new double[2][valuesPredict.get(0).get(0).get(0).get(0).size()];

        System.out.println("ENTRA EN PINTAR LA GRÁFICA");

        //for de numero de datasets + numero de classifiers + numero de valores en el valuesPredict
        //ARREGLAR
        System.out.println("Values Predict SIZE--> " + valuesPredict.get(0).get(0).get(0).get(0).size());
        System.out.println("Values Predict--> " + valuesPredict.get(0).get(0).get(0).get(0).get(0));

        for (int m = 0; m < datasets.size(); m++) {
            for (int z = 0; z < listClassifier.size(); z++) {
                for (int j = 0; j < targets.size(); j++) {
                    for (int i = 0; i < valuesPredict.get(m).get(z).size(); i++) {
                        //datasetPrueba.addSeries("actual",  valuesPredict.get(m).get(z).get(j).get(i).get(0).actual());
                        System.out.println("Values Predict--> " + valuesPredict.get(m).get(z).get(j).get(i).get(0).actual());

                    }
                }
            }
        }

        // datasetPrueba.addSeries("firefox", new double[][]{{2007, 2008, 2009, 2010, 2011, 2012, 2013, 2014, 2015, 2016, 2017}, {25, 29.1, 32.1, 32.9, 31.9, 25.5, 20.1, 18.4, 15.3, 11.4, 9.5}});
        // datasetPrueba.addSeries("ie", new double[][]{{2007, 2008, 2009, 2010, 2011, 2012, 2013, 2014, 2015, 2016, 2017}, {67.7, 63.1, 60.2, 50.6, 41.1, 31.8, 27.6, 20.4, 17.3, 12.3, 8.1}});
        //datasetPrueba.addSeries("chrome", new double[][]{{2009, 2010, 2011, 2012, 2013, 2014, 2015, 2016, 2017}, {0.2, 6.4, 14.6, 25.3, 30.1, 34.3, 43.2, 47.3, 58.4}});
        //datasetPrueba.addSeries("prueba",prueba);
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesPaint(0, Color.ORANGE);
        //renderer.setSeriesPaint(1, Color.BLUE);
        //renderer.setSeriesPaint(2, Color.GREEN);
        renderer.setSeriesStroke(0, new BasicStroke(2));
        //renderer.setSeriesStroke(1, new BasicStroke(2));
        //renderer.setSeriesStroke(2, new BasicStroke(2));
        System.out.println("llega aqui");
        JFreeChart chart = ChartFactory.createTimeSeriesChart("Prueba", "Time", "Target", tsc);
        chart.getXYPlot().getRangeAxis().setRange(0, 100);
        chart.getXYPlot().setRenderer(renderer);
        Dimension d = new Dimension(370, 195);
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(d);
        chartPanel.setMaximumSize(d);
        chartPanel.setSize(d);

        forecastPlotPanel.removeAll();
        forecastPlotPanel.setLayout(new java.awt.BorderLayout());
        forecastPlotPanel.add(chartPanel);
        forecastPlotPanel.validate();
         */
    }

    private void printPredictionTrainingIncrementTable(List<List<List<List<List<List<Prediction>>>>>> valuesPredict, List<String> classifiersPrint, Instances datasets, List<String> targets, List<List<String>> fechas, int m, int contadorTarget) throws ExecutionException, InterruptedException {
        //System.out.println("ValuesPredict.get(0).size()--> " + valuesPredict.get(0).size());
        System.out.println("valuesPredict.size()--> " + valuesPredict.size());
        int contador = 0;
        int relativeIndice = 0;
        if (eleccion == 1) {
            relativeIndice = (datasets.numInstances() * Integer.parseInt(relativeTextField.getText())) / 100;
            //System.out.println("indiceRelativo--> " + relativeIndice);
        }
        int c = 0;
        int contadorFecha = 0;
        int indexFecha = Integer.parseInt(absoluteTextField.getText());;

        System.out.println("classifier--> " + classifiersPrint);
        System.out.println("fechas.size---> " + fechas.get(m).size());
        //System.out.println("fecha.get(m)--> " + fechas.get(m));
        System.out.println("targets--> "+targets);
        int mitad = valuesPredict.size() / listClassifier.size();
        for (int z = 0; z < listClassifier.size(); z++) {
            System.out.println("entra en classifier");
            for (int t = 0; t < contadorTarget; t++) {
                for (int h = 0; h < horizon.get(t); h++) {
                    //System.out.println("h--> " + h);
                    for (int i = 0; i < valuesPredict.get(m).get(z).get(t).get(h).size(); i++) {
                        //System.out.println("i--> " + i);
                        for (int j = 0; j < valuesPredict.get(m).get(z).get(t).get(h).get(i).size(); j++) {
                           // System.out.println(valuesPredict.get(m).get(z).get(t).get(h).get(i));
                            predictionTableModel.addRow(new Object[]{getSpec(listClassifier.get(z)), datasets.relationName(), fechas.get(m).get((indexFecha + j)), targets.get(t), horizon.get(t), valuesPredict.get(m).get(z).get(t).get(h).get(i).get(j).predicted(), valuesPredict.get(m).get(z).get(t).get(h).get(i).get(j).actual(), (valuesPredict.get(m).get(z).get(t).get(h).get(i).get(j).predicted() - valuesPredict.get(m).get(z).get(t).get(h).get(i).get(j).actual())});
                            //System.out.println("termina");
                        }
                        // predictionTableModel.addRow(new Object[]{classifiersPrint.get(z), datasets.relationName(), fechas.get(m).get((indexFecha)), targets.get(t), 0, valuesPredict.get(i).get(j).predicted(), valuesPredict.get(i).get(j).actual(), (valuesPredict.get(i).get(j).predicted() - valuesPredict.get(i).get(j).actual())});

                        //System.out.println("huhuhu");
                    }
                }
                //System.out.println("hohoho");
            }
           // System.out.println("22222");

        }

        // if (eleccion == 0) {
        //    indexFecha = Integer.parseInt(absoluteTextField.getText());
        //} else if (eleccion == 1) {
        //    indexFecha = relativeIndice;
        // }
        // c++;
        // int horizons = horizon.get(contador);
        //if (c == horizon.get(contador)) {
        //   contador++;
        //   if (contador == horizon.size()) {
        //       contador = 0;
        //   }
        //   c = 0;
        // }
        // String cls = getSpec(listClassifier.get(0));
        //System.out.println("ValuesPredict.get(i).size()--> " + valuesPredict.get(i).size());
        // System.out.println("Llega justo antes del for");
        // for (int j = 0; j < valuesPredict.get(i).size(); j++) {
        //  predictionTableModel.addRow(new Object[]{classifiersPrint.get(i), datasets.relationName(), fechas.get(m).get((indexFecha + j)), targets.get(0), horizons, valuesPredict.get(i).get(j).predicted(), valuesPredict.get(i).get(j).actual(), (valuesPredict.get(i).get(j).predicted() - valuesPredict.get(i).get(j).actual())});
        //      System.out.println("i---> " + i);
        // }
        System.out.println("Termina el prediction Table");
    }

    private void printPredictionTable(List<List<Prediction>> valuesPredict, List<String> classifiersPrint, Instances datasets, List<String> targets, List<List<String>> fechas, int m) throws ExecutionException, InterruptedException {
       // System.out.println("ValuesPredict.get(0).size()--> " + valuesPredict.get(0).size());
       // System.out.println("valuesPredict.size()--> " + valuesPredict.size());
        int contador = 0;
        int relativeIndice = 0;
        if (eleccion == 1) {
            relativeIndice = (datasets.numInstances() * Integer.parseInt(relativeTextField.getText())) / 100;
           // System.out.println("indiceRelativo--> " + relativeIndice);
        }
        int c = 0;
        int contadorFecha = 0;
        int indexFecha = 0;
        //System.out.println("classifier--> " + classifiersPrint);
        System.out.println("fechas.size---> " + fechas.get(m).size());
       // System.out.println("fecha.get(m)--> " + fechas.get(m));
        //System.out.println(targets);
        int mitad = valuesPredict.size() / listClassifier.size();
        for (int i = 0; i < valuesPredict.size(); i++) {
            if (eleccion == 0) {
                indexFecha = Integer.parseInt(absoluteTextField.getText());
            } else if (eleccion == 1) {
                indexFecha = relativeIndice;

            }
            c++;
            int horizons = horizon.get(contador);
            if (c == horizon.get(contador)) {
                contador++;
                if (contador == horizon.size()) {
                    contador = 0;
                }
                c = 0;
            }

            String cls = getSpec(listClassifier.get(0));
            //System.out.println("ValuesPredict.get(i).size()--> " + valuesPredict.get(i).size());
            System.out.println("Llega justo antes del for");
            for (int j = 0; j < valuesPredict.get(i).size(); j++) {

                predictionTableModel.addRow(new Object[]{classifiersPrint.get(i), datasets.relationName(), fechas.get(m).get((indexFecha + j)), targets.get(i), horizons, valuesPredict.get(i).get(j).predicted(), valuesPredict.get(i).get(j).actual(), (valuesPredict.get(i).get(j).predicted() - valuesPredict.get(i).get(j).actual())});

            }
            System.out.println("i---> " + i);
        }
        System.out.println("Termina el prediction Table");
    }

    private void printOverallMetricsTable(List mae, List RMSE, List R2, List<List<Prediction>> valuesPredict, List<String> classifiersPrint, Instances datasets, List<String> targets, List<List<String>> fechas, int m) throws ExecutionException, InterruptedException {
        System.out.println("mae.size()--> " + mae.size());
        //System.out.println("MAE--> " + mae);
        //System.out.println("RMSE--> " + RMSE);
        //System.out.println("R2- " + R2);

        double aux = 0;
        double auxSmape = 0;
        int size = 0;
        int sizeSmape = 0;
        List mape = new ArrayList();
        List smape = new ArrayList();
        for (int i = 0; i < valuesPredict.size(); i++) {
            for (int j = 0; j < valuesPredict.get(i).size(); j++) {
                aux += Math.abs(valuesPredict.get(i).get(j).actual() - valuesPredict.get(i).get(j).predicted()) / valuesPredict.get(i).get(j).predicted();
                size++;
            }
            mape.add((aux / size) * 100);
        }
        for (int i = 0; i < valuesPredict.size(); i++) {
            for (int j = 0; j < valuesPredict.get(i).size(); j++) {
                auxSmape += Math.abs(valuesPredict.get(i).get(j).actual() - valuesPredict.get(i).get(j).predicted()) / ((valuesPredict.get(i).get(j).actual() + valuesPredict.get(i).get(j).predicted()) / 2);
                sizeSmape++;
            }
            smape.add(auxSmape / sizeSmape);
        }
        //System.out.println("MAPE--> " + mape);
        System.out.println("mape.size()--> " + mape.size());

        for (int i = 0; i < mae.size(); i++) {
            overallMetricsTableModel.addRow(new Object[]{classifiersPrint.get(i), datasets.relationName(), mae.get(i), RMSE.get(i), mape.get(i), smape.get(i), R2.get(i)});

        }

    }

    private void printOverallMetricsTrainingIncrementTable(List<List<List<List<List>>>> mae, List<List<List<List<List>>>> RMSE, List<List<List<List<List>>>> R2, List<List<Prediction>> valuesPredict, List<String> classifiersPrint, Instances datasets, List<String> targets, List<List<String>> fechas, int m, int contadorTarget) throws ExecutionException, InterruptedException {
        System.out.println("mae.size()--> " + mae.size());
        //System.out.println("MAE--> " + mae);
        //System.out.println("RMSE--> " + RMSE);
        //System.out.println("R2- " + R2);

        double aux = 0;
        double auxSmape = 0;
        int size = 0;
        int sizeSmape = 0;
        List mape = new ArrayList();
        List smape = new ArrayList();
        for (int i = 0; i < valuesPredict.size(); i++) {
            for (int j = 0; j < valuesPredict.get(i).size(); j++) {
                aux += Math.abs(valuesPredict.get(i).get(j).actual() - valuesPredict.get(i).get(j).predicted()) / valuesPredict.get(i).get(j).predicted();
                size++;

            }
            mape.add((aux / size) * 100);
        }
        for (int i = 0; i < valuesPredict.size(); i++) {
            for (int j = 0; j < valuesPredict.get(i).size(); j++) {
                auxSmape += Math.abs(valuesPredict.get(i).get(j).actual() - valuesPredict.get(i).get(j).predicted()) / ((valuesPredict.get(i).get(j).actual() + valuesPredict.get(i).get(j).predicted()) / 2);
                sizeSmape++;
            }
            smape.add(auxSmape / sizeSmape);
        }
        //System.out.println("MAPE--> " + mape);
        System.out.println("mape.size()--> " + mape.size());
        //int mitad = valuesPredict.size() / listClassifier.size();
        for (int z = 0; z < listClassifier.size(); z++) {
            System.out.println("entra en classifier");
            for (int t = 0; t < contadorTarget; t++) {
                for (int h = 0; h < horizon.get(t); h++) {
                    System.out.println("h--> " + h);
                    for (int i = 0; i < mae.get(m).get(z).get(t).get(h).size(); i++) {
                        System.out.println("i--> " + i);
                        //System.out.println("MAE--> " + mae.get(m).get(z).get(t).get(h).get(i));
                        overallMetricsTableModel.addRow(new Object[]{getSpec(listClassifier.get(z)), datasets.relationName(), mae.get(m).get(z).get(t).get(h).get(i), RMSE.get(m).get(z).get(t).get(h).get(i), mape.get(i), smape.get(i), R2.get(m).get(z).get(t).get(h).get(i)});
                    }

                }
            }
        }
    }

    public void printMetricsByHorizonTable(List<List<List<List>>> mae, List<List<List<List>>> rmse, List<List<List<List>>> r2, List<Instances> datasets, List<Attribute> targets, List<List<String>> classifiersPrintByHorizon) {
        double suma = 0;
        double numeroH = 0;
        int numeroDatasets = 0;
        for (int m = 0; m < datasets.size(); m++) {
            datasetComboBox.addItem(datasets.get(m).relationName());
            numeroDatasets = m;
        }

        // for (int i = 0; i < targets.size(); i++) {
        //     targetComboBox.addItem(targets.get(i).name());
        // }

        /*
        System.out.println("mae.get(0).size()--> " + mae.get(0).size());
        System.out.println("maeByHorizon--> " + mae);
        int columnCount = metricsByHorizonTableModel.getColumnCount();
        for (int i = 0; i < classifiersPrintByHorizon.get(0).size(); i++) {
            Object[] fila = new Object[metricsByHorizonTableModel.getColumnCount()];
            for (int j = 0; j < columnCount; j++) {
                if (j == 0) {
                    fila[j] = classifiersPrintByHorizon.get(0).get(i);
                } else if (columnCount - j != 1) {
                    fila[j] = mae.get(0).get(0).get((j - 1));
                } else {
                    for (int z = 0; z < mae.get(0).get(0).size(); z++) {
                        suma = suma + Double.parseDouble(mae.get(0).get(0).get(z).toString());
                    }
                    fila[j] = suma / numeroH;
                }
            }
            metricsByHorizonTableModel.addRow(fila);

        }
         */
    }

    public void pruebaPlot(int m, List<List<List<List<List<Prediction>>>>> valuesPredict, List<List<String>> fechas) throws ParseException {
        List<List<Date>> date = new ArrayList();
        SimpleDateFormat formato = new SimpleDateFormat(timeFormatComboBox.getSelectedItem().toString());
        if (m != 2) {
            System.out.println("No entra");
        } else {
            System.out.println("Entra en el plot");
            horizonPlotComboBox.removeAllItems();
            int indexTarget = targetPlotComboBox.getSelectedIndex();
            System.out.println("indexTarget--> " + indexTarget);
            int indexDataset = datasetPlotComboBox.getSelectedIndex();
            System.out.println("indexTarget--> " + indexTarget);
            int relative = 0;
            int indexClassifier = algorithmPlotComboBox.getSelectedIndex();
            System.out.println("indexClassifier--> " + indexClassifier);

            DefaultXYDataset datasetPrueba = new DefaultXYDataset();
            List<String> fechaPrueba = new ArrayList();
            TimeSeries ts1 = new TimeSeries("Actual");
            TimeSeries ts2 = new TimeSeries("Predicted");
            TimeSeriesCollection tsc = new TimeSeriesCollection();

            int numeroH = 0;
            if (horizonPlotComboBox.getItemCount() != horizon.get(indexTarget)) {

                for (int i = 0; i < horizon.get(indexTarget); i++) {
                    horizonPlotComboBox.addItem(String.valueOf(i + 1));
                    numeroH = i;
                }
            }

            int indexHorizon = horizonPlotComboBox.getSelectedIndex();

            System.out.println("indexHorizon--> " + indexHorizon);
            for (int b = 0; b < listInstances.size(); b++) {
                List<Date> aux = new ArrayList();
                String sinComas;
                for (int v = 0; v < fechas.get(b).size(); v++) {
                    sinComas = fechas.get(b).get(v).replace("'", "");
                    aux.add(formato.parse(sinComas));
                }
                date.add(aux);
            }

            //System.out.println("Date--> " + date);

            for (int i = 0; i < fechas.get(0).size(); i++) {
                fechaPrueba.add((fechas.get(0).get(i)));
            }

            //System.out.println("fechaPrueba--> " + fechaPrueba);
            //System.out.println("||--> " + valuesPredict.get(indexDataset).get(indexClassifier).get(indexTarget).get(indexHorizon).get(0));
            //System.out.println("||--> " + valuesPredict.get(indexDataset).get(indexClassifier).get(indexTarget).size());
            //System.out.println("||--> " + valuesPredict.get(indexDataset).get(indexClassifier).get(1).size());

            // System.out.println("|1|--> " + valuesPredict.get(indexDataset).get(indexClassifier).get(indexTarget).get(indexHorizon).get(0));
            //System.out.println("|2|-->" + valuesPredict.get(indexDataset).get(indexClassifier).get(indexTarget).get(indexHorizon));
            //fechas.get(indexDataset).size();
            System.out.println("size valuesPredict.get(indexDataset).get(indexClassifier).get(indexTarget).get(indexHorizon)--> " + valuesPredict.get(indexDataset).get(indexClassifier).get(indexTarget).get(indexHorizon).size());
            if (eleccion == 1) {
                relative = (int) Math.round(date.get(indexDataset).size() * Integer.parseInt(relativeTextField.getText()) / 100);
            }
            for (int k = 0; k < valuesPredict.get(indexDataset).get(indexClassifier).get(indexTarget).get(indexHorizon).size(); k++) {
                //String[] g = fechas.get(indexDataset).get(k).split("-");
                //int dia = Integer.parseInt(g[2]);
                //int mes = Integer.parseInt(g[1]);
                //int año = Integer.parseInt(g[0]);
                //System.out.println("Dia/Mes/Año: " + dia + "/" + mes + "/" + año);
                //ts.add(new Day(dia, mes, año),valuesPredict.get(indexDataset).get(indexClassifier).get(indexTarget).get(indexHorizon).get(k).actual());
                if (timeFormatComboBox.getSelectedItem().toString().contains("HH")) {
                    ts1.add(new Hour(date.get(indexDataset).get(k + (Integer.parseInt(absoluteTextField.getText())))), valuesPredict.get(indexDataset).get(indexClassifier).get(indexTarget).get(indexHorizon).get(k).actual());
                } else {
                    if (eleccion == 0) {
                        ts1.add(new Day(date.get(indexDataset).get(k + (Integer.parseInt(absoluteTextField.getText())))), valuesPredict.get(indexDataset).get(indexClassifier).get(indexTarget).get(indexHorizon).get(k).actual());

                    } else {
                        ts1.add(new Day(date.get(indexDataset).get(k + relative)), valuesPredict.get(indexDataset).get(indexClassifier).get(indexTarget).get(indexHorizon).get(k).actual());

                    }

                }
                //System.out.println("k-> " + k);
            }
            System.out.println("llega");
            tsc.addSeries(ts1);
            for (int k = 0; k < valuesPredict.get(indexDataset).get(indexClassifier).get(indexTarget).get(indexHorizon).size(); k++) {
                //System.out.println("entra al for");
                //String[] g = fechas.get(indexDataset).get(k).split("-");
                //int dia = Integer.parseInt(g[2]);
                //int mes = Integer.parseInt(g[1]);
                //int año = Integer.parseInt(g[0]);
                //System.out.println("Dia/Mes/Año: " + dia + "/" + mes + "/" + año);
                //ts.add(new Day(dia, mes, año),valuesPredict.get(indexDataset).get(indexClassifier).get(indexTarget).get(indexHorizon).get(k).actual());
                if (timeFormatComboBox.getSelectedItem().toString().contains("HH")) {
                    ts2.add(new Hour(date.get(indexDataset).get(k + (Integer.parseInt(absoluteTextField.getText())))), valuesPredict.get(indexDataset).get(indexClassifier).get(indexTarget).get(indexHorizon).get(k).predicted());
                } else {
                    if (eleccion == 0) {
                        ts2.add(new Day(date.get(indexDataset).get(k + (Integer.parseInt(absoluteTextField.getText())))), valuesPredict.get(indexDataset).get(indexClassifier).get(indexTarget).get(indexHorizon).get(k).predicted());
                    } else {
                        ts2.add(new Day(date.get(indexDataset).get(k + relative)), valuesPredict.get(indexDataset).get(indexClassifier).get(indexTarget).get(indexHorizon).get(k).predicted());

                    }
                }
                //System.out.println("k-> " + k);
            }
            tsc.addSeries(ts2);
            System.out.println("2");
            //double[][] prueba = new double[2][valuesPredict.get(0).get(0).get(0).get(0).size()];

            System.out.println("ENTRA EN PINTAR LA GRÁFICA");

            //for de numero de datasets + numero de classifiers + numero de valores en el valuesPredict
            //ARREGLAR
            // datasetPrueba.addSeries("firefox", new double[][]{{2007, 2008, 2009, 2010, 2011, 2012, 2013, 2014, 2015, 2016, 2017}, {25, 29.1, 32.1, 32.9, 31.9, 25.5, 20.1, 18.4, 15.3, 11.4, 9.5}});
            // datasetPrueba.addSeries("ie", new double[][]{{2007, 2008, 2009, 2010, 2011, 2012, 2013, 2014, 2015, 2016, 2017}, {67.7, 63.1, 60.2, 50.6, 41.1, 31.8, 27.6, 20.4, 17.3, 12.3, 8.1}});
            //datasetPrueba.addSeries("chrome", new double[][]{{2009, 2010, 2011, 2012, 2013, 2014, 2015, 2016, 2017}, {0.2, 6.4, 14.6, 25.3, 30.1, 34.3, 43.2, 47.3, 58.4}});
            //datasetPrueba.addSeries("prueba",prueba);
            XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
            renderer.setSeriesPaint(0, Color.GREEN);
            renderer.setSeriesPaint(1, Color.RED);
            //renderer.setSeriesPaint(2, Color.GREEN);
            renderer.setSeriesStroke(0, new BasicStroke(2));
            renderer.setSeriesStroke(1, new BasicStroke(2));
            //renderer.setSeriesStroke(2, new BasicStroke(2));
            System.out.println("llega aqui");
            chart = ChartFactory.createTimeSeriesChart("Prueba", "Time", "Target", tsc);
            chart.getXYPlot().getRangeAxis().setAutoRange(true);

            chart.getXYPlot().setRenderer(renderer);
            Dimension d = new Dimension(390, 194);
            ChartPanel chartPanel = new ChartPanel(chart);
            chartPanel.setPreferredSize(d);
            chartPanel.setMaximumSize(d);
            chartPanel.setSize(d);
            chartPanel.setToolTipText("Gráfica de pronostico");
            forecastPlotPanel.removeAll();
            forecastPlotPanel.setLayout(new java.awt.BorderLayout());
            forecastPlotPanel.add(chartPanel);
            forecastPlotPanel.validate();
        }
    }

    public void plotTrainingIncrement(int m, List<List<List<List<List<List<Prediction>>>>>> valuesPredict, List<List<String>> fechas) throws ParseException {
        List<List<Date>> date = new ArrayList();
        SimpleDateFormat formato = new SimpleDateFormat(timeFormatComboBox.getSelectedItem().toString());
        if (m != 2) {
            System.out.println("No entra");
        } else {
            System.out.println("Entra en el plot");
            horizonPlotComboBox.removeAllItems();
            int indexTarget = targetPlotComboBox.getSelectedIndex();
            System.out.println("indexTarget--> " + indexTarget);
            int indexDataset = datasetPlotComboBox.getSelectedIndex();
            System.out.println("indexTarget--> " + indexTarget);

            int indexClassifier = algorithmPlotComboBox.getSelectedIndex();
            System.out.println("indexClassifier--> " + indexClassifier);

            DefaultXYDataset datasetPrueba = new DefaultXYDataset();
            List<String> fechaPrueba = new ArrayList();
            TimeSeries ts1 = new TimeSeries("Actual");
            TimeSeries ts2 = new TimeSeries("Predicted");
            TimeSeriesCollection tsc = new TimeSeriesCollection();
            int numeroH = 0;
            if (horizonPlotComboBox.getItemCount() != horizon.get(indexTarget)) {

                for (int i = 0; i < horizon.get(indexTarget); i++) {
                    horizonPlotComboBox.addItem(String.valueOf(i + 1));
                    numeroH = i;
                }
            }

            int indexHorizon = horizonPlotComboBox.getSelectedIndex();

            System.out.println("indexHorizon--> " + indexHorizon);
            for (int b = 0; b < listInstances.size(); b++) {
                List<Date> aux = new ArrayList();
                String sinComas;
                for (int v = 0; v < fechas.get(b).size(); v++) {
                    sinComas = fechas.get(b).get(v).replace("'", "");
                    aux.add(formato.parse(sinComas));
                }
                date.add(aux);
            }

            System.out.println("Date--> " + date);

            for (int i = 0; i < fechas.get(0).size(); i++) {
                fechaPrueba.add((fechas.get(0).get(i)));
            }

            System.out.println("fechaPrueba--> " + fechaPrueba);
            //System.out.println("||--> " + valuesPredict.get(indexDataset).get(indexClassifier).get(indexTarget).get(indexHorizon).get(0));
            //System.out.println("||--> " + valuesPredict.get(indexDataset).get(indexClassifier).get(indexTarget).size());
            //System.out.println("||--> " + valuesPredict.get(indexDataset).get(indexClassifier).get(1).size());

            // System.out.println("|1|--> " + valuesPredict.get(indexDataset).get(indexClassifier).get(indexTarget).get(indexHorizon).get(0));
            //System.out.println("|2|-->" + valuesPredict.get(indexDataset).get(indexClassifier).get(indexTarget).get(indexHorizon));
            //fechas.get(indexDataset).size();
            System.out.println("size valuesPredict.get(indexDataset).get(indexClassifier).get(indexTarget).get(indexHorizon)--> " + valuesPredict.get(indexDataset).get(indexClassifier).get(indexTarget).get(indexHorizon).size());
            for (int k = 0; k < valuesPredict.get(indexDataset).get(indexClassifier).get(indexTarget).get(indexHorizon).size(); k++) {
                for (int j = 0; j < valuesPredict.get(indexDataset).get(indexClassifier).get(indexTarget).get(indexHorizon).get(k).size(); j++) {

                    //System.out.println("entra al for");
                    //String[] g = fechas.get(indexDataset).get(k).split("-");
                    //int dia = Integer.parseInt(g[2]);
                    //int mes = Integer.parseInt(g[1]);
                    //int año = Integer.parseInt(g[0]);
                    //System.out.println("Dia/Mes/Año: " + dia + "/" + mes + "/" + año);
                    //ts.add(new Day(dia, mes, año),valuesPredict.get(indexDataset).get(indexClassifier).get(indexTarget).get(indexHorizon).get(k).actual());
                    if (timeFormatComboBox.getSelectedItem().toString().contains("HH")) {
                        ts1.add(new Hour(date.get(indexDataset).get(k + (Integer.parseInt(absoluteTextField.getText())))), valuesPredict.get(indexDataset).get(indexClassifier).get(indexTarget).get(indexHorizon).get(k).get(j).actual());
                    } else {
                        ts1.add(new Day(date.get(indexDataset).get(k + (Integer.parseInt(absoluteTextField.getText())))), valuesPredict.get(indexDataset).get(indexClassifier).get(indexTarget).get(indexHorizon).get(k).get(j).actual());

                    }
                    //System.out.println("k-> " + k);
                }
            }
            System.out.println("llega");
            tsc.addSeries(ts1);
            for (int k = 0; k < valuesPredict.get(indexDataset).get(indexClassifier).get(indexTarget).get(indexHorizon).size(); k++) {
                for (int j = 0; j < valuesPredict.get(indexDataset).get(indexClassifier).get(indexTarget).get(indexHorizon).get(k).size(); j++) {
                    //System.out.println("entra al for");
                    //String[] g = fechas.get(indexDataset).get(k).split("-");
                    //int dia = Integer.parseInt(g[2]);
                    //int mes = Integer.parseInt(g[1]);
                    //int año = Integer.parseInt(g[0]);
                    //System.out.println("Dia/Mes/Año: " + dia + "/" + mes + "/" + año);
                    //ts.add(new Day(dia, mes, año),valuesPredict.get(indexDataset).get(indexClassifier).get(indexTarget).get(indexHorizon).get(k).actual());
                    if (timeFormatComboBox.getSelectedItem().toString().contains("HH")) {
                        ts2.add(new Hour(date.get(indexDataset).get(k + (Integer.parseInt(absoluteTextField.getText())))), valuesPredict.get(indexDataset).get(indexClassifier).get(indexTarget).get(indexHorizon).get(k).get(j).predicted());
                    } else {
                        ts2.add(new Day(date.get(indexDataset).get(k + (Integer.parseInt(absoluteTextField.getText())))), valuesPredict.get(indexDataset).get(indexClassifier).get(indexTarget).get(indexHorizon).get(k).get(j).predicted());

                    }
                    //System.out.println("k-> " + k);
                }
            }
            tsc.addSeries(ts2);
            System.out.println("2");
            //double[][] prueba = new double[2][valuesPredict.get(0).get(0).get(0).get(0).size()];

            System.out.println("ENTRA EN PINTAR LA GRÁFICA");

            //for de numero de datasets + numero de classifiers + numero de valores en el valuesPredict
            //ARREGLAR
            // datasetPrueba.addSeries("firefox", new double[][]{{2007, 2008, 2009, 2010, 2011, 2012, 2013, 2014, 2015, 2016, 2017}, {25, 29.1, 32.1, 32.9, 31.9, 25.5, 20.1, 18.4, 15.3, 11.4, 9.5}});
            // datasetPrueba.addSeries("ie", new double[][]{{2007, 2008, 2009, 2010, 2011, 2012, 2013, 2014, 2015, 2016, 2017}, {67.7, 63.1, 60.2, 50.6, 41.1, 31.8, 27.6, 20.4, 17.3, 12.3, 8.1}});
            //datasetPrueba.addSeries("chrome", new double[][]{{2009, 2010, 2011, 2012, 2013, 2014, 2015, 2016, 2017}, {0.2, 6.4, 14.6, 25.3, 30.1, 34.3, 43.2, 47.3, 58.4}});
            //datasetPrueba.addSeries("prueba",prueba);
            XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
            renderer.setSeriesPaint(0, Color.GREEN);
            renderer.setSeriesPaint(1, Color.RED);
            //renderer.setSeriesPaint(2, Color.GREEN);
            renderer.setSeriesStroke(0, new BasicStroke(2));
            renderer.setSeriesStroke(1, new BasicStroke(2));
            //renderer.setSeriesStroke(2, new BasicStroke(2));
            System.out.println("llega aqui");
            chart = ChartFactory.createTimeSeriesChart("Prueba", "Time", "Target", tsc);
            chart.getXYPlot().getRangeAxis().setAutoRange(true);

            chart.getXYPlot().setRenderer(renderer);
            Dimension d = new Dimension(390, 194);
            ChartPanel chartPanel = new ChartPanel(chart);
            chartPanel.setPreferredSize(d);
            chartPanel.setMaximumSize(d);
            chartPanel.setSize(d);

            forecastPlotPanel.removeAll();
            forecastPlotPanel.setLayout(new java.awt.BorderLayout());
            forecastPlotPanel.add(chartPanel);
            forecastPlotPanel.validate();
        }
    }

    public void prueba(int m) {
        List<List<List<List>>> metric = new ArrayList();
        if (metricsComboBox.getSelectedItem().toString() == "MAE") {
            metric = maeGeneral;
        } else if (metricsComboBox.getSelectedItem().toString() == "R2") {
            metric = r2General;
        } else {
            metric = rmseGeneral;
        }
        System.out.println("METRIC---> " + metricsComboBox.getSelectedItem());
        System.out.println("ENTRA A LA PRUEBA");
        if (m != 2) {
            System.out.println("Target vacio");
            for (int i = 0; i < targets.size(); i++) {
                targetComboBox.addItem(targets.get(i).name());
            }

        } else {
            metricsByHorizonTableModel = new DefaultTableModel();
            metricsByHorizonTable.setModel(metricsByHorizonTableModel);
            metricsByHorizonTableModel.addColumn("Algorithms");
            System.out.println("Entra en el item state changed");
            int indexTarget = targetComboBox.getSelectedIndex();
            int indexDataset = datasetComboBox.getSelectedIndex();
            System.out.println("indexDataset--> " + indexDataset);
            double suma = 0;
            double numeroH = 0;
            //POR DEFECTO APARECE EL PRIMER TARGET
            for (int i = 0; i < horizon.get(indexTarget); i++) {
                metricsByHorizonTableModel.addColumn("h" + (i + 1));
                numeroH = i;
            }
            //Una vez metidas todas las columnas de horizontes, se crea la de average.
            metricsByHorizonTableModel.addColumn("Average");
            System.out.println("mae.get(0).size()--> " + metric.get(0).size());
            System.out.println("maeByHorizon--> " + maeGeneral);
            int columnCount = metricsByHorizonTableModel.getColumnCount();
            for (int i = 0; i < classifiersPrintGeneral.get(0).size(); i++) {
                Object[] fila = new Object[metricsByHorizonTableModel.getColumnCount()];
                for (int j = 0; j < columnCount; j++) {
                    if (j == 0) {
                        fila[j] = classifiersPrintGeneral.get(indexTarget).get(i);
                    } else if (columnCount - j != 1) {
                        fila[j] = metric.get(indexDataset).get(i).get(indexTarget).get((j - 1));
                        System.out.println("fila--> " + fila[j]);
                    } else {
                        for (int z = 0; z < metric.get(indexDataset).get(i).get(indexTarget).size(); z++) {
                            suma = suma + Double.parseDouble(metric.get(indexDataset).get(i).get(indexTarget).get(z).toString());
                        }
                        fila[j] = suma / numeroH;
                    }
                }
                metricsByHorizonTableModel.addRow(fila);

            }
        }

    }

    public void pruebaTrainingIncrement(int m) {
        List<List<List<List<List>>>> metric = new ArrayList();
        if (metricsComboBox.getSelectedItem().toString() == "MAE") {
            metric = maeGeneralTrainingIncrement;
        } else if (metricsComboBox.getSelectedItem().toString() == "R2") {
            metric = r2GeneralTrainingIncrement;
        } else {
            metric = rmseGeneralTrainingIncrement;
        }
        System.out.println("METRIC---> " + metricsComboBox.getSelectedItem());
        System.out.println("ENTRA A LA PRUEBA TRAINING INCREMENT");
        if (m != 2) {
            System.out.println("Target vacio");
            for (int i = 0; i < targets.size(); i++) {
                targetComboBox.addItem(targets.get(i).name());
            }

        } else {
            metricsByHorizonTableModel = new DefaultTableModel();
            metricsByHorizonTable.setModel(metricsByHorizonTableModel);
            metricsByHorizonTableModel.addColumn("Algorithms");
            System.out.println("Entra en el item state changed");
            int indexTarget = targetComboBox.getSelectedIndex();
            int indexDataset = datasetComboBox.getSelectedIndex();
            System.out.println("indexDataset--> " + indexDataset);
            double suma = 0;
            double numeroH = 0;
            //POR DEFECTO APARECE EL PRIMER TARGET
            for (int i = 0; i < horizon.get(indexTarget); i++) {
                metricsByHorizonTableModel.addColumn("h" + (i + 1));
                numeroH = i;
            }
            //Una vez metidas todas las columnas de horizontes, se crea la de average.
            metricsByHorizonTableModel.addColumn("Average");
            //System.out.println("mae.get(0).size()--> " + metric.get(0).size());
            System.out.println("maeGeneralTrainingIncrement--> " + maeGeneralTrainingIncrement);
            System.out.println("class-->" + classifiersPrintGeneral);
            int columnCount = metricsByHorizonTableModel.getColumnCount();
            for (int i = 0; i < classifiersPrintGeneral.get(0).size(); i++) {
                for (int k = 0; k < metric.get(indexDataset).get(i).get(indexTarget).get(0).size(); k++) {
                    suma = 0;
                    Object[] fila = new Object[metricsByHorizonTableModel.getColumnCount()];
                    for (int j = 0; j < columnCount; j++) {
                        if (j == 0) {
                            fila[j] = classifiersPrintGeneral.get(indexTarget).get(i);
                        } else if (columnCount - j != 1) {
                            System.out.println("||--> " + metric.get(indexDataset).get(i).get(indexTarget).get((j - 1)).get(k));
                            fila[j] = metric.get(indexDataset).get(i).get(indexTarget).get((j - 1)).get(k);
                            suma = suma + Double.parseDouble(metric.get(indexDataset).get(i).get(indexTarget).get((j - 1)).get(k).toString());
                        } else {
                            // for (int z = 0; z < metric.get(indexDataset).get(i).get(indexTarget).get(0).size(); z++) {
                            //     suma = suma + Double.parseDouble(metric.get(indexDataset).get(i).get(indexTarget).get(z).toString());
                            // }
                            fila[j] = suma / numeroH;//suma / numeroH;
                        }
                    }
                    metricsByHorizonTableModel.addRow(fila);
                }

            }
        }

    }

    private static boolean isNumeric(String cadena) {

        try {
            Double.parseDouble(cadena);
            return true;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }

    @Override
    public void setExplorer(Explorer explr) {
        this.m_Explorer = explr;
    }

    @Override
    public Explorer getExplorer() {
        return this.m_Explorer;
    }

    @Override
    public void setInstances(Instances i) {
        this.m_Instances = i;
    }

    @Override
    public String getTabTitle() {
        return "Time Series Studio";
    }

    @Override
    public String getTabTitleToolTip() {
        return "Pestaña para el TFG de Ingenieria informatica en sistemas de informacion";
    }

    @Override
    public void setLog(Logger logger) {
        this.m_log = logger;
    }

    /* public void evaluatorAndSearchPanels() {
        m_AttributeEvaluatorEditor.setClassType(ASEvaluation.class);
        m_AttributeEvaluatorEditor.setValue(ExplorerDefaults.getASEvaluator());
        m_AttributeEvaluatorEditor
                .addPropertyChangeListener(new PropertyChangeListener() {
                    @Override
                    public void propertyChange(PropertyChangeEvent e) {
                        if (m_AttributeEvaluatorEditor.getValue() instanceof AttributeEvaluator) {
                                Object backup = m_AttributeEvaluatorEditor.getBackup();
                                int result
                                        = JOptionPane.showConfirmDialog(null,
                                                "You must use use the Ranker search method "
                                                + "in order to use\n"
                                                + m_AttributeEvaluatorEditor.getValue().getClass()
                                                        .getName()
                                                + ".\nShould I select the Ranker search method for you?",
                                                "Alert!", JOptionPane.YES_NO_OPTION);
                                
                        } else {
                            
                        }

                        // check capabilities...
                        Capabilities currentFilter
                                = m_AttributeEvaluatorEditor.getCapabilitiesFilter();
                        ASEvaluation evaluator
                                = (ASEvaluation) m_AttributeEvaluatorEditor.getValue();
                        Capabilities currentSchemeCapabilities = null;
                        if (evaluator != null && currentFilter != null
                                && (evaluator instanceof CapabilitiesHandler)) {
                            currentSchemeCapabilities
                                    = ((CapabilitiesHandler) evaluator).getCapabilities();

                            
                        }
                        repaint();
                    }
                });
      }
     */
    public void filterPanel() {
        m_FilterEditor.setClassType(weka.filters.Filter.class
        );
        m_FilterEditor.addPropertyChangeListener(new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent e) {
                m_ApplyFilterBut.setEnabled(m_Instances != null);
                Capabilities currentCapabilitiesFilter
                        = m_FilterEditor.getCapabilitiesFilter();
                Filter filter = (Filter) m_FilterEditor.getValue();
                Capabilities currentFilterCapabilities = null;
                if (filter != null && currentCapabilitiesFilter != null
                        && (filter instanceof CapabilitiesHandler)) {
                    currentFilterCapabilities
                            = ((CapabilitiesHandler) filter).getCapabilities();

                    if (!currentFilterCapabilities
                            .supportsMaybe(currentCapabilitiesFilter)
                            && !currentFilterCapabilities.supports(currentCapabilitiesFilter)) {
                        try {
                            filter.setInputFormat(m_Instances);
                        } catch (Exception ex) {
                            m_ApplyFilterBut.setEnabled(false);
                        }
                    }
                }
            }
        }
        );
        filterPanel.setLayout(new BorderLayout());
        filterPanel.add(m_FilterPanel, BorderLayout.CENTER);
        JPanel ssButs = new JPanel();
        ssButs.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        ssButs.setLayout(new GridLayout(1, 2, 2, 0));
        ssButs.add(m_ApplyFilterBut);
        ssButs.add(m_StopBut);
        filterPanel.add(ssButs, BorderLayout.EAST);

    }

    public void algorithmPanel() {
        m_ClassifierEditor.setClassType(Classifier.class
        );
        m_ClassifierEditor.setValue(ExplorerDefaults.getClassifier());
        m_ClassifierEditor.addPropertyChangeListener(
                new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent e
            ) {
                // Check capabilities
                Capabilities currentFilter = m_ClassifierEditor.getCapabilitiesFilter();
                Classifier classifier = (Classifier) m_ClassifierEditor.getValue();
                Capabilities currentSchemeCapabilities = null;
                if (classifier != null && currentFilter != null
                        && (classifier instanceof CapabilitiesHandler)) {
                    currentSchemeCapabilities
                            = ((CapabilitiesHandler) classifier).getCapabilities();

                    if (!currentSchemeCapabilities.supportsMaybe(currentFilter)
                            && !currentSchemeCapabilities.supports(currentFilter)) {
                        //runExpBtn.setEnabled(false);
                    }
                }
                repaint();
            }
        }
        );

        JPanel ssButs = new JPanel();

        ssButs.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        ssButs.setLayout(
                new GridLayout(1, 2, 2, 0));
        ssButs.add(add_Algo);

        panelAlgoritmos.add(ssButs, BorderLayout.EAST);

    }

    public static String getSpec(Object object) {
        if (object instanceof OptionHandler) {
            return object.getClass().getSimpleName() + " "
                    + Utils.joinOptions(((OptionHandler) object).getOptions());
        }

        return object.getClass().getSimpleName();
    }

    public void addAlgo(java.awt.event.ActionEvent evt) {
        tablaAlgoritmosModel.addRow(new Object[]{getSpec(m_ClassifierEditor.getValue())});
        listClassifier.add(m_ClassifierEditor.getValue());
        experimentLabel.setText("<html><p style=\"width:100px\">" + "Name: " + newTextField.getText() + "<br> Datasets: " + listInstances.size() + "<br> Features: " + listFeatures.size() + "<br>  Classifiers: " + listClassifier.size() + "<br>  Validation: Absolute" + "<br> Result: " + calculated + "</p></html>");

    }

    private void converterQuery(final File f) {
        final GenericObjectEditor convEd = new GenericObjectEditor(true);

        try {
            convEd.setClassType(weka.core.converters.Loader.class
            );
            convEd.setValue(
                    new weka.core.converters.CSVLoader());
            ActionListener al = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    tryConverter((Loader) convEd.getValue(), f);
                }
            };

            ((GenericObjectEditor.GOEPanel) convEd.getCustomEditor()).addOkListener(al);

            PropertyDialog pd;

            if (PropertyDialog.getParentDialog(
                    this) != null) {
                pd
                        = new PropertyDialog(PropertyDialog.getParentDialog(this), convEd, -1, -1);
            } else {
                pd
                        = new PropertyDialog(PropertyDialog.getParentFrame(this), convEd, -1, -1);
            }

            pd.addWindowListener(
                    new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e
                ) {
                    ((GenericObjectEditor.GOEPanel) convEd.getCustomEditor()).removeOkListener(al);
                    pd.dispose();
                }

                @Override
                public void windowClosed(WindowEvent e
                ) {
                    pd.setContentPane(new JPanel());
                    pd.removeWindowListener(this);
                }
            }
            );
            pd.setVisible(
                    true);
        } catch (Exception ex) {

        }
    }

    private void tryConverter(final Loader cnv, final File f) {

        if (m_IOThread == null) {
            m_IOThread = new Thread() {
                @Override
                public void run() {
                    try {
                        cnv.setSource(f);
                        m_Instances = cnv.getDataSet();
                        setInstancesToList(m_Instances);
                    } catch (Exception ex) {
                        m_log.statusMessage(cnv.getClass().getName() + " failed to load "
                                + f.getName());
                        JOptionPane.showMessageDialog(tsStudio.this, cnv.getClass()
                                .getName()
                                + " failed to load '"
                                + f.getName()
                                + "'.\n"
                                + "Reason:\n" + ex.getMessage(), "Convert File",
                                JOptionPane.ERROR_MESSAGE);
                        m_IOThread = null;
                        converterQuery(f);
                    }
                    m_IOThread = null;
                }
            };
            m_IOThread.setPriority(Thread.MIN_PRIORITY); // UI has most priority
            m_IOThread.start();
        }
    }

    public void setInstancesFromFile(final AbstractFileLoader loader) {

        if (m_IOThread == null) {
            m_IOThread = new Thread() {
                @Override
                public void run() {
                    try {
                        m_log.statusMessage("Reading from file...");
                        m_Instances = loader.getDataSet();
                        setInstancesToList(m_Instances);
                    } catch (Exception ex) {
                        m_log.statusMessage("File '" + loader.retrieveFile()
                                + "' not recognised as an '" + loader.getFileDescription()
                                + "' file.");
                        m_IOThread = null;
                        if (JOptionPane.showOptionDialog(tsStudio.this,
                                "File '" + loader.retrieveFile() + "' not recognised as an '"
                                + loader.getFileDescription() + "' file.\n" + "Reason:\n"
                                + ex.getMessage(), "Load Instances", 0,
                                JOptionPane.ERROR_MESSAGE, null, new String[]{"OK",
                                    "Use Converter"}, null) == 1) {

                            converterQuery(loader.retrieveFile());
                        }
                    }
                    m_IOThread = null;
                }
            };
            m_IOThread.setPriority(Thread.MIN_PRIORITY); // UI has most priority
            m_IOThread.start();
        } else {
            JOptionPane.showMessageDialog(this, "Can't load at this time,\n"
                    + "currently busy with other IO", "Load Instances",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    public static String getInitialDirectory() {
        String result;

        result = get("InitialDirectory", "%c");
        result = result.replaceAll("%t", System.getProperty("java.io.tmpdir"));
        result = result.replaceAll("%h", System.getProperty("user.home"));
        result = result.replaceAll("%c", System.getProperty("user.dir"));
        result = result.replaceAll("%%", System.getProperty("%"));

        return result;
    }

    public void setInstancesToList(Instances inst) {

        m_Instances = inst;
        try {
            Runnable r = new Runnable() {
                public void run() {
                    listVariables.setModel(modeloLista);
                    listaInputVariables.setModel(modeloLista);
                    datasetTableModel.addRow(new Object[]{m_Instances.relationName(), m_Instances.numInstances(), m_Instances.numAttributes()});
                    contadorDataset++;
                }
            };
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Problem setting base instances:\n"
                    + ex, "Instances", JOptionPane.ERROR_MESSAGE);
        }
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton absoluteRadioButton;
    private javax.swing.JTextField absoluteTextField;
    private javax.swing.JButton addButtonFeature;
    private javax.swing.JButton addCustomFeatureButton;
    private javax.swing.JButton addFile;
    private javax.swing.JButton addFolder;
    private javax.swing.JButton addFunctionButtonFeature;
    private javax.swing.JButton addInputVariablesButton;
    private javax.swing.JButton addPreprocess;
    private javax.swing.JButton addTargetButton;
    private javax.swing.JButton addVariableButtonFeature;
    private javax.swing.JPanel algoToBeUsedPanel;
    private javax.swing.JComboBox<String> algorithmPlotComboBox;
    private javax.swing.JLabel algorithmPlotLabel;
    private javax.swing.ButtonGroup buttonGroupTrainingIncrements;
    private javax.swing.ButtonGroup buttonGroupTrainingSize;
    private javax.swing.JButton createButton;
    private javax.swing.JPanel customFeaturePanel;
    private javax.swing.JComboBox<String> datasetComboBox;
    private javax.swing.JPanel datasetConfigurationPanel;
    private javax.swing.JComboBox<String> datasetPlotComboBox;
    private javax.swing.JLabel datasetPlotLabel;
    private javax.swing.JTable datasetTable;
    private javax.swing.JLabel experimentLabel;
    private javax.swing.JPanel experimentPanel;
    private javax.swing.JPanel filterPanel;
    private javax.swing.JLabel filterSelectedLabel;
    private javax.swing.JPanel forecastPlotPanel;
    private javax.swing.JLabel formulaLabelFeature;
    private javax.swing.JPanel formulaPanel;
    private javax.swing.JTextField formulaTextFeature;
    private javax.swing.JCheckBox fromTSOriginCheckBox;
    private javax.swing.JRadioButton fromToRadioButton;
    private javax.swing.JLabel functionCambiaLabelFeature;
    private javax.swing.JComboBox<String> functionComboFeature;
    private javax.swing.JLabel functionLabelFeature;
    private javax.swing.ButtonGroup grupoTimeSeries;
    private javax.swing.JComboBox<String> horizonPlotComboBox;
    private javax.swing.JLabel horizonPlotLabel;
    private javax.swing.JPanel initialTrainingPanel;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton10;
    private javax.swing.JButton jButton11;
    private javax.swing.JButton jButton12;
    private javax.swing.JButton jButton13;
    private javax.swing.JButton jButton14;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JButton jButton9;
    private javax.swing.JCheckBox jCheckBox2;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLayeredPane jLayeredPane1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JRadioButton jRadioButton4;
    private javax.swing.JRadioButton jRadioButton5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane10;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JScrollPane jScrollPane8;
    private javax.swing.JScrollPane jScrollPane9;
    private javax.swing.JList<String> listVariFeatures;
    private javax.swing.JList<String> listVariables;
    private javax.swing.JList<String> listaInputVariables;
    private javax.swing.JButton loadAlgoBtn;
    private javax.swing.JButton loadButtonFeature;
    private javax.swing.JButton loadDatasetConfiguration;
    private javax.swing.JButton loadExperimentButton;
    private javax.swing.JPanel metricsByHorizonPanel;
    private javax.swing.JTable metricsByHorizonTable;
    private javax.swing.JComboBox<String> metricsComboBox;
    private javax.swing.JLabel nameLabelFeature;
    private javax.swing.JTextField nameTextFeature;
    private javax.swing.JLabel newLabel;
    private javax.swing.JTextField newTextField;
    private javax.swing.JTextField numThreadsTextField;
    private javax.swing.JPanel overallMetricsPanel;
    private javax.swing.JTable overallMetricsTable;
    private javax.swing.JPanel panelAlgoritmos;
    private javax.swing.JPanel panelLagged;
    private javax.swing.JTable predictionTable;
    private javax.swing.JPanel predictionTablePanel;
    private javax.swing.JProgressBar progressExp;
    private javax.swing.JRadioButton radioButtonMaximum;
    private javax.swing.JRadioButton radioButtonMinimum;
    private javax.swing.JRadioButton radioButtonValue;
    private javax.swing.JRadioButton relativeRadioButton;
    private javax.swing.JTextField relativeTextField;
    private javax.swing.JButton removeButton;
    private javax.swing.JButton removeInputButton;
    private javax.swing.JButton removeSelectButtonTablaFeature;
    private javax.swing.JButton removeTargetsButton;
    private javax.swing.JPanel runExperimentPanel;
    private javax.swing.JToggleButton runStopButton;
    private javax.swing.JButton saveAlgoBtn;
    private javax.swing.JButton saveArffOverallMetrics;
    private javax.swing.JButton saveButtonFeature;
    private javax.swing.JButton saveCsvOverallMetrics;
    private javax.swing.JButton saveDataConfigurationButton;
    private javax.swing.JButton saveExperimentButton;
    private javax.swing.JButton selectAllButton;
    private javax.swing.JButton selectAllButtonTablaFeatures;
    private javax.swing.JButton selectAllInputButton;
    private javax.swing.JButton selectAllTargetsButton;
    private javax.swing.JButton selectAllVariFeatures;
    private javax.swing.JTextField shiftTextFeature;
    private javax.swing.JTextField shiftTextFeature1;
    private javax.swing.JLabel sizeLabelFeature;
    private javax.swing.JTextField sizeTextFieldFeature;
    private javax.swing.JPanel slidingWindowPanel;
    private javax.swing.JPanel tabAlgo;
    private javax.swing.JPanel tabData;
    private javax.swing.JPanel tabExp;
    private javax.swing.JPanel tabFea;
    private javax.swing.JPanel tabRe;
    private javax.swing.JPanel tabVal;
    private javax.swing.JTable tablaAlgo;
    private javax.swing.JTable tablaFeatures;
    private javax.swing.JTable tablaTarget;
    private javax.swing.JComboBox<String> targetComboBox;
    private javax.swing.JComboBox<String> targetPlotComboBox;
    private javax.swing.JLabel targetPlotLabel;
    private javax.swing.JTextField textFieldTimeSeries;
    private javax.swing.JTabbedPane tfgTabbedPane;
    private javax.swing.JComboBox<String> timeFieldComboBox;
    private javax.swing.JComboBox<String> timeFormatComboBox;
    private javax.swing.JTextField trainingIncrementTextField;
    private javax.swing.JPanel trainingVariationsPanel;
    private javax.swing.JPanel usedDatasetPanel;
    private javax.swing.JComboBox<String> variableComboFeature;
    private javax.swing.JComboBox<String> variableComboFunctionFeature;
    private javax.swing.JLabel variableLabelFeature;
    private javax.swing.JLabel variableLabelFunctionFeature;
    private javax.swing.JLabel variablesLabel;
    // End of variables declaration//GEN-END:variables
}
