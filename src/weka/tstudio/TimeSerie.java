/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package weka.tstudio;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import weka.core.AbstractInstance;
import weka.core.Attribute;
import weka.core.BinarySparseInstance;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SparseInstance;

/**
 *
 * @author manum
 */
public class TimeSerie {

    LinkedList<List> dataset = new LinkedList<List>();
    List<Instances> lista = new ArrayList();
    List<List<List>> list = new ArrayList<List<List>>();

    /*public void time(List<Instances> inst, int salto, List<Integer> horizon, List<Attribute> att) {

        for (int m = 0; m < inst.size(); m++) {
            int h = 0;
            int at = 0;
            System.out.println("HORIZONTE: " + horizon.get(m));
            Instances r = new Instances("Test", (ArrayList<Attribute>) att, 0);
            while (at < att.size()) {
                double[] instanceValue2 = new double[r.numAttributes()];
                for (int prueba = 0; prueba < att.size(); prueba++) {
                    int i = 0;
                    while (i < inst.get(m).numInstances()) {
                        int j = 0;

                        while (at < horizon.size() && j < horizon.get(at) && (i + j) < inst.get(m).numInstances()) {

                            instanceValue2[prueba] = r.attribute(prueba).addStringValue(inst.get(m).get(i + j).toString(prueba));

                            r.add(new DenseInstance(1.0, instanceValue2));
                            j++;
                        }
                    }

                    //instanceValue2[1] = r.attribute(1).addStringValue(inst.get(m).get(i + j).toString(1));
                    i = i + salto;
                    if (i >= inst.get(m).numInstances()) {
                        at++;
                        System.out.println("AT: " + at);

                    }
                }
            }
            lista.add(r);
        }

        for (int i = 0; i < lista.size(); i++) {
            System.out.println("Instancia nº" + i);
            System.out.println(lista.get(i));
        }
    }
     */
  /*  public void target(List<Instances> inst, int salto, List<Integer> horizon, int lag, List<Attribute> att) {

        for (int i = 0; i < att.size() * 5; i++) {
            dataset.add(new ArrayList<>());
        }

        System.out.println("Salto ----> " + salto);
        for (int m = 0; m < inst.size(); m++) {
            int at = 0;
            int a = 0;
            // System.out.println("HORIZONTE: " + horizon.get(a));

            while (at < att.size() * 2) {

                System.out.println("at ----> " + at);
                /*if(at < att.size()){
                   dataset.get(at).add(att.get(at).name()+"_Lag"+(at+1)+ ";");
                }
                else{
                    dataset.get(at).add(att.get(a).name()+"_Ahead"+(a+1)+ ";");
                  
                }*/
/*
                int i = 0;
                while (i < inst.get(m).numInstances()) {
                    int cont = 0;
                    int q = 2;
                    if (at < att.size()) {
                        while (cont < (Math.abs(lag))) {
                            int j = lag;
                            while (j < 0 && (i + j) < inst.get(m).numInstances()) {
                                // System.out.println("J= " + j);
                                if (lag < 0) {
                                    // System.out.println("Lag: " + lag);
                                    // System.out.println("Lag valor abosluto: " + (Math.abs(lag)));
                                    if ((i + j) < 0) {
                                        if (at <= 0) {
                                            dataset.get(cont).add("?" + ";");
                                            //   System.out.println("MISSING");
                                            j++;
                                        } else {
                                            dataset.get(cont + at + 1).add("?" + ";");
                                            //   System.out.println("MISSING");
                                            j++;
                                        }
                                    } else {
                                        if (at <= 0) {

                                            dataset.get(cont).add(inst.get(m).get(i + j).toString(at) + ";");
                                            System.out.println("Negativo: " + inst.get(m).get(i + j).toString(at));
                                            j++;
                                        } else {
                                            dataset.get((cont + at + 1)).add(inst.get(m).get(i + j).toString(at) + ";");
                                            System.out.println("Negativo: " + inst.get(m).get(i + j).toString(at));
                                            j++;
                                        }

                                    }

                                }

                                System.out.println("Contador------- " + cont);
                                cont++;
                                System.out.println("Q----->" + q);
                                q++;
                            }
                            i = i + salto;
                            if (i >= inst.get(m).numInstances()) {
                                at++;
                                //  System.out.println("AT: " + at);

                            }
                        }

                    } else {
                        //System.out.println("FUERA LAG---DENTRO HORIZON");
                        int j = 0;
                        int v = 4;
                        int z = 6;
                        int l = 8;
                        while (a < horizon.size() && j < horizon.get(a) && (i + j) < inst.get(m).numInstances()) {
                            if (at == 2) {
                                System.out.println(inst.get(m).get(i + j).toString(a));
                                dataset.get(v).add(inst.get(m).get(i + j).toString(a) + ";");
                                System.out.println("J en horizon-->" + j);
                                j++;
                            } else if (at == 3) {
                                System.out.println(inst.get(m).get(i + j).toString(a));
                                dataset.get((z)).add(inst.get(m).get(i + j).toString(a) + ";");
                                System.out.println("J en horizon-->" + j);
                                j++;
                            } else {
                                System.out.println(inst.get(m).get(i + j).toString(a));
                                dataset.get((l)).add(inst.get(m).get(i + j).toString(a) + ";");
                                System.out.println("J en horizon-->" + j);
                                j++;
                            }
                            System.out.println("V-->" + v);
                            v++;
                            System.out.println("Z-->" + z);
                            z++;
                            l++;
                        }

                        i = i + salto;

                        if (i >= inst.get(m).numInstances()) {
                            a++;
                            at++;

                            //   System.out.println("AT: " + at);
                        }
                    }
                }
            }
            System.out.println("SIZE(0)---> " + dataset.get(0).size());
            System.out.println("Size---> " + dataset.size());
            for (int i = 0; i <= dataset.get(0).size() - 1; i++) {
                for (int j = 0; j < dataset.size(); j++) {
                    System.out.print(dataset.get(j).get(i));
                }
                System.out.println("");
            }
//            list.add(dataset);
        }
        for (int i = 0; i < list.size(); i++) {
            System.out.println("Instancia nº" + i);
            System.out.println(list.get(i));
        }

    }
*/
    public List<Instances> nuevo(List<Instances> inst, int salto, List<Integer> horizon, int lag, List<Attribute> att, String formato, List<String> features, String variable,List<String> nombreCustomFeature) throws ParseException {
        System.out.println("Salto: " + salto);
        List<Instances> listaPrepositionalInstances = new ArrayList();
        int indexFecha = 0;
        int sumaHorizon = 0;
        int contadorCustomFeature = 0;

        CustomFeature cf = new CustomFeature();
        SimpleDateFormat formatter = null;
         for (int m = 0; m < inst.size(); m++) {
            int j = 0;
             while(j < inst.get(m).numAttributes()){
                if(inst.get(m).attribute(j).name().toLowerCase().contains("fecha") || inst.get(m).attribute(j).name().toLowerCase().contains("date")){
                    indexFecha = j;
                    j = inst.get(m).numAttributes();
                }
                else{
                    j++;
                }
             }
         }
         System.out.println("indexFecha--> "+indexFecha+" y el nombre del atributo--> "+inst.get(0).attribute(indexFecha).name());
        
        for (int m = 0; m < inst.size(); m++) {
            int j = 0;
             while(j < inst.get(m).numAttributes()){
                 if(inst.get(m).attribute(j).isDate()){
                     formatter = new SimpleDateFormat(inst.get(m).attribute(j).getDateFormat());
                     j = inst.get(m).numAttributes();
                     System.out.println("formato de la fecha");
                 }
                 else{
                     formatter = new SimpleDateFormat();
                     j++;

                 }
             }
        }
        
        SimpleDateFormat formatter2 = new SimpleDateFormat(formato);
        System.out.println("horizon--> "+horizon);
        for (int i = 0; i < horizon.size(); i++) {

            sumaHorizon = sumaHorizon + horizon.get(i);

        }

        int numeroVariables = (att.size() * Math.abs(lag)) + sumaHorizon;
        System.out.println("numeroVariables--> " + numeroVariables);
        for (int m = 0; m < inst.size(); m++) {
            System.out.println("M--> "+m);
            List<Integer> index = new ArrayList();
            List<Integer> listaIndex = new ArrayList();
            for (int i = 0; i < att.size(); i++) {
                if (variable.equals(inst.get(m).attribute(i).name())) {
                    index.add(i);
                }
                for (int j = 0; j < inst.get(m).numAttributes(); j++) {
                    if (att.get(i).name().trim().equals(inst.get(m).attribute(j).name().trim())) {
                        listaIndex.add(j);
                        System.out.println(inst.get(m).attribute(j).name());
                    }
                }
            }
            System.out.println("-->"+listaIndex);
            int contadorAhead = 0;
            int contadorLagged = Math.abs(lag);
            int contadorFeature;
            ArrayList<Attribute> atts = new ArrayList<>();

            for (int k = 0; k < inst.get(m).numAttributes(); k++) {
                if (inst.get(m).attribute(k).isDate() || inst.get(m).attribute(k).name().contains("FECHA") || inst.get(m).attribute(k).name().toLowerCase().contains("date")) {
                    atts.add(new Attribute(inst.get(m).attribute(k).name(), formato));
                }
            }
            int ll = 0;
            int a = 0;
            System.out.println("Math.abs(lag) * att.size()--> "+Math.abs(lag) * att.size());
            for (int j = 0; j < numeroVariables; j++) {
                System.out.println("j--> "+j);
                if (contadorLagged == 0) {
                    ll++;
                    contadorLagged = Math.abs(lag);
                }
                if (j < Math.abs(lag) * att.size()) {
                    atts.add(new Attribute(att.get(ll).name() + "_lagged_" + (contadorLagged)));
                    contadorLagged--;
                    System.out.println("Lagged_" + contadorLagged);
                } else {
                    if (contadorAhead == horizon.get(a)) {
                        a++;
                        contadorAhead = 0;
                    }
                    contadorAhead++;
                    atts.add(new Attribute(att.get(a).name() + "_ahead_" + contadorAhead));
                    System.out.println("ahead_" + contadorAhead);
                }

            }
            System.out.println("features--> "+features);
            for (int i = 0; i < features.size(); i++) {
                atts.add(new Attribute(nombreCustomFeature.get(i)));
            }
            //System.out.println("holi");
            listaPrepositionalInstances.add(new Instances(inst.get(m).relationName() + "_" + m, atts, 0));
            //System.out.println("añade los atributos");
            for (int i = 0; i < inst.get(m).numInstances(); i = i + salto) {
                contadorAhead = 1;

                contadorLagged = lag;
                contadorFeature = 0;
                double[] vals = new double[listaPrepositionalInstances.get(m).numAttributes()];
                int k = 0;
                int kFeature = 0;
                int ah = 0;
                for (int j = 0; j < (numeroVariables + 1 + features.size()); j++) {
                    if (listaPrepositionalInstances.get(m).attribute(j).isDate()) {
                        //System.out.println("deberia entrar");
                        String fff = inst.get(m).get(i).toString(indexFecha).replaceAll("'", "");
                        Date prueba = formatter.parse(fff);
                        //System.out.println("date--> "+prueba);
                        String prueba2 = formatter2.format(prueba);
                        //System.out.println("fecha2--> "+prueba2);
                        vals[j] = (listaPrepositionalInstances.get(m).attribute(0).parseDate(prueba2));
                        //System.out.println("entra en la fecha");
                    } 
                    else if(listaPrepositionalInstances.get(m).attribute(j).name().trim().equals(nombreCustomFeature.get(contadorCustomFeature).trim())){
                        //System.out.println("CUSTOM FEATURE");
                        vals[j] = cf.eval(features.get(contadorCustomFeature), i, inst, index.get(kFeature), m);
                        contadorCustomFeature++;
                        //System.out.println("contadorCustomFeature--> "+contadorCustomFeature);
                        //System.out.println("features.size()--> "+features.size());
                        if(contadorCustomFeature == features.size()){
                            contadorCustomFeature = 0;
                            kFeature++;
                        }
                        
                }
                    else  {
                        if (j < (att.size() * Math.abs(lag)) + 1) {
                            if (i + contadorLagged < 0) {
                                vals[j] = Double.NaN;
                            } else {
                                vals[j] = (inst.get(m).get(i + contadorLagged).value(listaIndex.get(k)));
                            }
                            contadorLagged++;
                            if (contadorLagged == 0) {
                                k++;
                                contadorLagged = lag;
                            }
                        } else  {
                            if (i + contadorAhead >= inst.get(m).numInstances()) {
                                vals[j] = Double.NaN;
                            } else {
                                vals[j] = (inst.get(m).get(i + contadorAhead).value(listaIndex.get(ah)));
                                contadorAhead++;
                                if (contadorAhead > horizon.get(ah)) {
                                    ah++;
                                    contadorAhead = 1;
                                }
                            }
                        }
                    }
                        
                
                       
                    }
                //System.out.println("llega al final del for");
                listaPrepositionalInstances.get(m).add(new DenseInstance(1.0, vals));

                }

            

        }
            //System.out.println("Llega al final");

            return listaPrepositionalInstances;

    }
    public List hori(List<Instances> inst, int salto, List<Integer> horizon, int lag, List<Attribute> att) {
        System.out.println("Salto: " + salto);
        int sumaHorizon = 0;
        int j_marker = 0;
        for (int i = 0; i < horizon.size(); i++) {

            sumaHorizon = sumaHorizon + horizon.get(i);

        }
        //System.out.println("Suma Horizon: "+sumaHorizon);

        //System.out.println("Numero de variables: " + ((att.size() * Math.abs(lag)) + sumaHorizon));
        for (int i = 0; i < ((att.size() * (Math.abs(lag))) + sumaHorizon); i++) {
            dataset.add(new ArrayList<>());
        }

        for (int m = 0; m < inst.size(); m++) {
            int at = 0;
            int contVariables = 0;
            int n = 0;

            int t = 0;
            int nombre = 0;
            while (t >= 0) {
                dataset.get(t).add(att.get(n).name() + "_Lag" + (Math.abs(lag) - nombre) + ";");
                //System.out.println((att.get(n).name() + "_Lag" + (Math.abs(lag) - nombre) + ";"));
                t++;
                nombre++;
                if (t % Math.abs(lag) == 0) {
                    n++;
                    nombre = 0;

                }
                if (t == (att.size() * Math.abs(lag))) {
                    t = -1;
                }
            }
            t = (att.size() * Math.abs(lag));
            nombre = 0;
            n = 0;

            while (t >= 0 && n < att.size()) {
                int h = horizon.get(n);
                dataset.get(t).add(att.get(n).name() + "_Ahead" + (h - nombre) + ";");
                //System.out.println((att.get(n).name() + "_Ahead" + (h - nombre) + ";"));
                t++;
                nombre++;
                if (nombre % horizon.get(n) == 0) {
                    n++;
                    nombre = 0;

                }
                if (t == ((att.size() * (Math.abs(lag))) + sumaHorizon)) {
                    t = -1;
                }
            }

            while (at < (att.size() * 2)) {
                int i = 0;
                while (i < inst.get(m).numInstances()) {
                    if (at < (att.size())) {
                        int contador = lag;
                        int j = 0;
                        if (at == 0) {
                            j = 0;
                        } else {
                            j = at + (Math.abs(lag) - 1);
                        }
                        while (contador < 0 && (i + contador) < inst.get(m).numInstances()) {
                            if (lag < 0) {
                                if ((i + contador) < 0) {
                                    dataset.get(j).add("?" + ";");
                                    contador++;
                                } else {
                                    dataset.get(j).add(inst.get(m).get(i + contador).toString(at));
                                    contador++;
                                }
                            }
                            // System.out.println("J_lag: " + j);
                            j++;
                            j_marker = j;
                        }

                        i = i + salto;
                        if (i >= inst.get(m).numInstances()) {
                            at++;
                            //  System.out.println("AT: " + at);

                        }
                    } else {
                        int contador = horizon.get(contVariables) - 1;
                        //System.out.println("HORIZON: " + horizon.get(contVariables));
                        int j = 0;
                        if (contVariables == 0) {
                            j = j_marker;
                        } else {
                            j = j_marker + horizon.get(contVariables - 1);
                        }
                        while (contador >= 0 && (i + contador) < inst.get(m).numInstances()) {
                            dataset.get(j).add(inst.get(m).get(i + contador).toString(contVariables));
                            // System.out.println(inst.get(m).get(i + contador).toString(contVariables));
                            contador--;
                            //  System.out.println("J: " + j);
                            j++;
                        }
                        i = i + salto;

                        if (i >= inst.get(m).numInstances()) {
                            contVariables++;
                            at++;
                        }

                    }
                }

            }

            // list.add(dataset);
            // System.out.println("Dataset.get(0)---> "+dataset.get(0));
            //System.out.println("Dataset.get(1)---> "+dataset.get(1));
            //System.out.println("DONE");
            //System.out.println("Dataset.size()--> "+dataset.size());
        }
        for (int i = 0; i <= dataset.get(0).size() - 1; i++) {
            for (int j = 0; j < dataset.size(); j++) {
                System.out.print(dataset.get(j).get(i) + ";");
            }
            System.out.println("");
        }
        System.out.println("FINISH");

        //System.out.println("Dataset de 1,0--> "+dataset.get(1).get(2)+"|------>Se espera 132");
        // System.out.println("DATASET-----<> "+dataset);
        // System.out.println("Size timeSeries--> "+dataset.size());
        // System.out.println("Size dataset.get(0)--> "+dataset.get(0).size());
        return dataset;
    }

    public int lag(List<Instances> inst, int salto, int lag, List<Attribute> att) {
        int p = 0;
        for (int m = 0; m < inst.size(); m++) {
            int at = 0;

            //System.out.println("HORIZONTE: " + lag.get(m));
            for (int i = 0; i < att.size(); i++) {
                dataset.add(new ArrayList<>());
            }

            while (at < att.size() * 2) {

                System.out.println("Size att: " + att.size());
                dataset.get(at).add(att.get(at).name() + ";");
                int i = 0;
                //int cont = 0;
                while (i < inst.get(m).numInstances()) {
                    int cont = 0;
                    while (cont < (Math.abs(lag))) {
                        int j = lag;

                        while (j < 0 && (i + j) < inst.get(m).numInstances()) {
                            System.out.println("J= " + j);
                            if (lag < 0) {
                                System.out.println("Lag: " + lag);
                                System.out.println("Lag valor abosluto: " + (Math.abs(lag)));
                                if ((i + j) < 0) {
                                    dataset.get(at).add("?");
                                    System.out.println("MISSING");
                                    j++;
                                } else {
                                    dataset.get(at).add(inst.get(m).get(i + j).toString(at) + ";");
                                    System.out.println("Negativo: " + inst.get(m).get(i + j).toString(at));
                                    j++;

                                }

                            } else {
                                dataset.get(at).add(inst.get(m).get(i + j).toString(at) + ";");
                                System.out.println("Positivo: " + inst.get(m).get(i + j).toString());

                                j++;
                                cont++;
                            }
                            cont++;
                        }

                        i = i + salto;
                        if (i >= inst.get(m).numInstances()) {
                            at++;
                            System.out.println("AT: " + at);

                        }
                    }
                }
            }
            for (int i = 0; i <= dataset.get(0).size(); i++) {
                for (int j = 0; j < dataset.size(); j++) {
                    System.out.print(dataset.get(j).get(i));
                }
                System.out.println("");
            }
//            list.add(dataset);
            p = at;
        }
        for (int i = 0; i < list.size(); i++) {
            System.out.println("Instancia nº" + i);
            System.out.println(list.get(i));
        }
        return p;

    }

    public void lagged(List<Instances> inst, int size, List<Attribute> att) {
        System.out.println("SIZE: " + size);
        // System.out.println("____________TIMES______________" + inst.attribute(1).name());
        for (int m = 0; m < inst.size(); m++) {
            //String att = inst.attribute(1).name();
            //ArrayList<Attribute> atts = new ArrayList<Attribute>();
            //ArrayList<String> classVal = new ArrayList<String>();
            //classVal.add("A");
            //classVal.add("B");
            //atts.add(new Attribute(inst.attribute(0).name(), (ArrayList<String>) null));
            Instances r = new Instances("LAGGED", (ArrayList<Attribute>) att, size);
            //double[] instanceValue1 = new double[r.numAttributes()];
            //double[] instanceValue2 = new double[r.numAttributes()];

            // instanceValue1[0] = r.attribute(0).addStringValue("holi");
            // r.add(1, new DenseInstance(1.0, instanceValue1));
            // instanceValue1[0] = r.attribute(0).addStringValue("eeee");
            // r.add(2, new DenseInstance(1.0, instanceValue1));
            int i = 0;
            while (i < inst.get(m).numInstances()) {
                int j = 0;

                double[] instanceValue1 = new double[r.numAttributes()];
                //instanceValue1[0] = r.attribute(0).addStringValue(inst.get(i).toString(0));
                //r.add( new DenseInstance(1.0, instanceValue1));
                while (j < size && (i + j) < inst.get(m).numInstances()) {
                    double[] instanceValue2 = new double[r.numAttributes()];
                    instanceValue2[0] = r.attribute(0).addStringValue(inst.get(m).get(i + j).toString(0));
                    instanceValue2[1] = r.attribute(1).addStringValue(inst.get(m).get(i + j).toString(1));
                    r.add(new DenseInstance(1.0, instanceValue2));
                    j++;
                    //System.out.println(r.get(i).toString(1));

                }
                //System.out.println(inst.get(m).get(i).toString(0));
                i = i + size;

            }
            lista.add(r);
            //System.out.println("_______R____" + r);
        }
    }
}
