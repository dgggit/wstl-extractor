package detection;

import java.util.List;

public class Evaluate {

    public static void evaluate(List<Integer> label, List<Integer> result){

        int TP = 0;
        int FP = 0;
        int TN = 0;
        int FN = 0;

        int size = label.size();

        for(int i=0; i<size; i++){
            if(label.get(i) == 0) { // TN, FP
                if (result.get(i) == 0) // TN
                    TN++;
                else
                    FP++;
            }
            else { // TP, FN
                if(result.get(i) == 1) // TP
                    TP++;
                else
                    FN++;

            }
        }
        double precision = (double) TP / (double) (TP+FP);
        double recall = (double) TP / (double) (TP+FN);
        double fscore = 2.0*( precision*recall / (precision+recall) );

        System.out.println("TP:"+TP+" FP:"+FP+" TN:"+TN+" FN:"+FN);
        System.out.println("PRECISION : "+precision);
        System.out.println("RECALL    : "+recall);
        System.out.println("F-SCORE   : "+fscore);
    }

}
