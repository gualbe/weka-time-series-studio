/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package weka.tstudio;

import java.util.ArrayList;
import java.util.List;
import weka.core.Instances;

/**
 *
 * @author manum
 */
public class MeanAndCount {

    public List count(List<Instances> inst, int rango1, int rango2, String attName, List<List<List>> dataset, int salto) {
        List<Integer> listaConteo = new ArrayList();
        for (int m = 0; m < inst.size(); m++) {
            int i = 0;
            int cont = 0;
            dataset.get(m).add(new ArrayList());
            int finalPosition = dataset.get(m).size() - 1;
            dataset.get(m).get(finalPosition).add("Count;");
            while (i < inst.get(m).numAttributes()) {
                if (attName.equals(inst.get(m).attribute(i).name())) {
                    for (int k = 0; k < inst.get(m).numInstances(); k = k + salto) {
                        int index = inst.get(m).attribute(i).index();
                        cont = Math.abs(rango1 - rango2);
                        // System.out.println("Conteo: " + cont);
                        listaConteo.add(cont);
                        dataset.get(m).get(finalPosition).add(cont + ";");

                    }

                }
                i++;
            }

        }
        for (int i = 0; i <= dataset.get(0).size() - 1; i++) {
            for (int j = 0; j < dataset.size(); j++) {

                System.out.print(dataset.get(j).get(i));

            }
            System.out.println("");
        }
        return dataset;
    }

    public List mean(List<Instances> inst, int rango1, int rango2, String attName, List<List<List>> dataset, int salto) {
        List<Double> listaMedia = new ArrayList();

        for (int m = 0; m < inst.size(); m++) {

            int i = 0;
            double media = 0;
            dataset.get(m).add(new ArrayList());
            int finalPosition = dataset.get(m).size() - 1;
            dataset.get(m).get(finalPosition).add(attName + "_Media;");
            while (i < inst.get(m).numAttributes()) {
                if (attName.equals(inst.get(m).attribute(i).name())) {
                    for (int k = 0; k < inst.get(m).numInstances(); k = k + salto) {
                        int index = inst.get(m).attribute(i).index();
                        int j = rango1;
                        double suma = 0;

                        while (j <= (rango2) && (k + j) < inst.get(m).numInstances()) {
                            suma = suma + Double.parseDouble(inst.get(m).get(k + j).toString(index));
                            //System.out.println("Suma: " + suma);
                            j++;
                        }
                        media = suma / (Math.abs(rango1 - rango2) + 1);
                        listaMedia.add(media);
                        dataset.get(m).get(finalPosition).add(media + ";");
                    }

                }

                // System.out.println("Media ---> suma =  " + suma + "/nº valores= " + (Math.abs(rango1 - rango2) + 1) + "|||| Resultado = " + media);
                // System.out.println("Lista media: " + listaMedia);
                i++;
            }

        }
        for (int i = 0; i <= dataset.get(0).size() - 1; i++) {
            for (int j = 0; j < dataset.size(); j++) {

                System.out.print(dataset.get(j).get(i));

            }
            System.out.println("");
        }
        return dataset;
    }

    public List sd(List<Instances> inst, int rango1, int rango2, String attName, List<List<List>> dataset, int salto) {
        List<Float> listaSd = new ArrayList();
        for (int m = 0; m < inst.size(); m++) {

            int index = 0;
            int i = 0;
            float media;
            float result;
            dataset.get(m).add(new ArrayList());
            int finalPosition = dataset.get(m).size() - 1;
            dataset.get(m).get(finalPosition).add("Standar_D;");
            while (i < inst.get(m).numAttributes()) {

                if (attName.equals(inst.get(m).attribute(i).name())) {
                    index = inst.get(m).attribute(i).index();
                    for (int k = 0; k < inst.get(m).numInstances(); k = k + salto) {
                        float suma = 0;
                        float standardDeviation = 0;

                        int j = rango1;
                        while (j <= (rango2) && (k + j) < inst.get(m).numInstances()) {
                            suma = suma + Integer.parseInt(inst.get(m).get(j + k).toString(index));
                            // System.out.println("Suma: " + suma);
                            j++;
                        }
                        media = suma / (Math.abs(rango1 - rango2) + 1);
                        // System.out.println("Media ---> suma =  " + suma + "/nº valores= " + (Math.abs(rango1 - rango2) + 1) + "|||| Resultado = " + media);
                        int h = rango1;
                        while (h <= (rango2) && (k + h) < inst.get(m).numInstances()) {
                            standardDeviation += Math.pow(Integer.parseInt(inst.get(m).get(k + h).toString(index)) - media, 2);
                            h++;
                        }
                        result = (float) Math.sqrt(standardDeviation / (Math.abs(rango1 - rango2)));
                        dataset.get(m).get(finalPosition).add(result + ";");
                    }

                }

                //System.out.println("StandarDeviation: " + standardDeviation);
                // System.out.println("SD--> " + result);
                //listaSd.add(result);
                i++;
            }

        }
        for (int i = 0; i <= dataset.get(0).size() - 1; i++) {
            for (int j = 0; j < dataset.size(); j++) {

                System.out.print(dataset.get(j).get(i));

            }
            System.out.println("");
        }
        return dataset;
    }

    public double mean2(List<Instances> inst, String attName, double rango1, double rango2, int row, int indexAttribute, int m) {
        double suma = 0;
        double mean = 0;
        double res;
        int i = (int) rango1;
        while (i <= (int) rango2 && (row + i) < inst.get(m).numInstances()) {

           // System.out.println("EEE-->" + inst.get(m).get(i + row).toString(indexAttribute));
            suma = suma + Double.parseDouble(inst.get(m).get(i + row).toString(indexAttribute));
            i++;
        }
        mean = suma / (Math.abs(rango1 - rango2) + 1);
        res = mean;
        return res;

    }

    public double count2(List<Instances> inst, String attName, double rango1, double rango2, int row, int indexAttribute, int m) {
        double cont = 0;
        double res;
        cont = Math.abs(rango1 - rango2);
        res = cont;
        return res;

    }

    public double sd2(List<Instances> inst, String attName, double rango1, double rango2, int row, int indexAttribute, int m) {
        double suma = 0;
        double mean = 0;
        double res;
        double standardDeviation = 0;
        int i = (int)rango1;
        while(i <= (int) rango2 && (row + i) < inst.get(m).numInstances()){

            System.out.println("EEE-->" + inst.get(m).get(i + row).toString(indexAttribute));
            suma = suma + Double.parseDouble(inst.get(m).get(i + row).toString(indexAttribute));
            i++;
        }
        mean = suma / (Math.abs(rango1 - rango2) + 1);
        double h = rango1;
        while (h <= (rango2) && (row + h) < inst.get(m).numInstances()) {
            standardDeviation += Math.pow(Integer.parseInt(inst.get(m).get((int) (row + h)).toString(indexAttribute)) - mean, 2);
            h++;
        }
        res = (float) Math.sqrt(standardDeviation / (Math.abs(rango1 - rango2)));

        return res;

    }

}
