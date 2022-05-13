/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package weka.tstudio;
import java.util.ArrayList;
import java.util.List;
import net.objecthunter.exp4j.ExpressionBuilder;
import net.objecthunter.exp4j.function.Function;
import weka.core.Instances;
/**
 *
 * @author manum
 */
public class CustomFeature {
    Suma s = new Suma();
    MaxMin mm = new MaxMin();
    MeanAndCount mac = new MeanAndCount();
    Mdelta mdel = new Mdelta();
    Shift sh = new Shift();
    // private double [][] dataset = {{1,2,3},
        //                           {4,5,6},
        //                           {7,8,9},
         //                          {10,11,12}};

    //public static void main (String [] args) {
      //  CustomFeature custom = new CustomFeature();
       // double res;

       // for (int i = 0; i < 4; i++) {
           // res = custom.eval("sum(3, 1, 2)", i); /* indicaria variable 3 y desplazamiento una fila menos */
           // System.out.println(res);
      //  }
    //}

    public double eval (String expression, int row,List<Instances> inst,int indexAttribute,int m) {
        Function sum = new Function("sum", 3) {
            @Override
            public double apply(double... args) {
                double res = Double.NaN;
                double suma = 0;
                int final_row = row + (int)args[1];
               // System.out.println("Inst.size()---> "+inst.get(0).size());
               // System.out.println("Final_row: "+final_row);
               // System.out.println("Indice del atributo--> "+(int)args[0]);
                if (final_row >= 0 && final_row < inst.get(m).size()){
                   res = s.sum2(inst, name,(int)args[1],(int)args[2], row,(int)args[0]-1,m);
                    
                    
                  //  for(int i =(int)args[1]; i<=(int)args[2];i++ ){
                  //  if((i+row)<dataset.length){
                  //  suma = suma + dataset[i+row][(int)args[0]-1];
                  //  res = suma;
                  //  }
                  //  }
                }
               // System.out.println("Res: "+res);
                return res;
            }
        };
        
       Function min = new Function("min", 3) {
            @Override
            public double apply(double... args) {
                double res = Double.NaN;
                int final_row = row + (int)args[1];
                System.out.println("Inst.size()---> "+inst.get(0).size());
                System.out.println("Final_row: "+final_row);
                if (final_row >= 0 && final_row < inst.get(m).size()){
                   res = mm.min2(inst, name,(int)args[1],(int)args[2], row,(int)args[0]-1,m);
                    
                    
                  //  for(int i =(int)args[1]; i<=(int)args[2];i++ ){
                  //  if((i+row)<dataset.length){
                  //  suma = suma + dataset[i+row][(int)args[0]-1];
                  //  res = suma;
                  //  }
                  //  }
                }
                System.out.println("Res: "+res);
                return res;
            }
       };
       
       Function max = new Function("max", 3) {
            @Override
            public double apply(double... args) {
                double res = Double.NaN;
                System.out.println("ROW--> "+row);
                int final_row = row + (int)args[1];
                System.out.println("Inst.size()---> "+inst.get(0).size());
                System.out.println("Final_row: "+final_row);
                if (final_row >= 0 && final_row < inst.get(m).size()){
                   System.out.println("entra en el res");
                   res = mm.max2(inst, name,(int)args[1],(int)args[2], row,(int)args[0]-1,m);
                    
                    
                  //  for(int i =(int)args[1]; i<=(int)args[2];i++ ){
                  //  if((i+row)<dataset.length){
                  //  suma = suma + dataset[i+row][(int)args[0]-1];
                  //  res = suma;
                  //  }
                  //  }
                }
                System.out.println("Res: "+res);
                return res;
            }
       };
       
       Function mean = new Function("mean", 3) {
            @Override
            public double apply(double... args) {
                double res = Double.NaN;
                
                int final_row = row + (int)args[1];
                //System.out.println("Inst.size()---> "+inst.get(0).size());
               // System.out.println("Final_row: "+final_row);
                if (final_row >= 0 && final_row < inst.get(m).size()){
                   res = mac.mean2(inst, name,(int)args[1],(int)args[2], row,(int)args[0]-1,m);
                    
                    
                  //  for(int i =(int)args[1]; i<=(int)args[2];i++ ){
                  //  if((i+row)<dataset.length){
                  //  suma = suma + dataset[i+row][(int)args[0]-1];
                  //  res = suma;
                  //  }
                  //  }
                }
                //System.out.println("Res: "+res);
                return res;
            }
       };
       
        Function count = new Function("count", 3) {
            @Override
            public double apply(double... args) {
                double res = Double.NaN;
                
                int final_row = row + (int)args[1];
                //System.out.println("Inst.size()---> "+inst.get(0).size());
               // System.out.println("Final_row: "+final_row);
                if (final_row >= 0 && final_row < inst.get(m).size()){
                   res = mac.count2(inst, name,(int)args[1],(int)args[2], row,(int)args[0]-1,m);
                    
                    
                  //  for(int i =(int)args[1]; i<=(int)args[2];i++ ){
                  //  if((i+row)<dataset.length){
                  //  suma = suma + dataset[i+row][(int)args[0]-1];
                  //  res = suma;
                  //  }
                  //  }
                }
                System.out.println("Res: "+res);
                return res;
            }
       };
        
        Function sd = new Function("sd", 3) {
            @Override
            public double apply(double... args) {
                double res = Double.NaN;
                
                int final_row = row + (int)args[1];
                //System.out.println("Inst.size()---> "+inst.get(0).size());
                //System.out.println("Final_row: "+final_row);
                if (final_row >= 0 && final_row < inst.get(m).size()){
                   res = mac.sd2(inst, name,(int)args[1],(int)args[2], row,(int)args[0]-1,m);
                    
                    
                  //  for(int i =(int)args[1]; i<=(int)args[2];i++ ){
                  //  if((i+row)<dataset.length){
                  //  suma = suma + dataset[i+row][(int)args[0]-1];
                  //  res = suma;
                  //  }
                  //  }
                }
                System.out.println("Res: "+res);
                return res;
            }
       };
        
        Function mdelta = new Function("mdelta", 3) {
            @Override
            public double apply(double... args) {
                double res = Double.NaN;
                
                int final_row = row + (int)args[1];
               // System.out.println("Inst.size()---> "+inst.get(0).size());
               // System.out.println("Final_row: "+final_row);
                if (final_row >= 0 && final_row < inst.get(m).size()){
                   res = mdel.mdelta2(inst, name,(int)args[1],(int)args[2], row,(int)args[0]-1,m);
                    
                    
                  //  for(int i =(int)args[1]; i<=(int)args[2];i++ ){
                  //  if((i+row)<dataset.length){
                  //  suma = suma + dataset[i+row][(int)args[0]-1];
                  //  res = suma;
                  //  }
                  //  }
                }
                System.out.println("Res: "+res);
                return res;
            }
       };
        
        Function shift = new Function("shift", 2) {
            @Override
            public double apply(double... args) {
                double res = Double.NaN;
                
                int final_row = row + (int)args[1];
               // System.out.println("Inst.size()---> "+inst.get(0).size());
               // System.out.println("Final_row: "+final_row);
                if (final_row >= 0 && final_row < inst.get(m).size()){
                   res = sh.shift(inst, name,(int)args[1], row,(int)args[0]-1,m);
                   
                    
                  //  for(int i =(int)args[1]; i<=(int)args[2];i++ ){
                  //  if((i+row)<dataset.length){
                  //  suma = suma + dataset[i+row][(int)args[0]-1];
                  //  res = suma;
                  //  }
                  //  }
                }
                System.out.println("Res: "+res);
                return res;
            }
       };
        
        
        double result = new ExpressionBuilder(expression)
                .function(sum)
                .function(min)
                .function(max)
                .function(mean)
                .function(count)
                .function(sd)
                .function(mdelta)
                .function(shift)
                .build()
                .evaluate();
        return result;
    }
}
