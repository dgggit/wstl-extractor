package detection.model;


import data.Example;
import detection.Detect;
import feature.Feature;
import libsvm.*;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.*;

/**
 * Created by Petar on 07.07.2016.
 */

public abstract class DetectionModel {

    svm_model model;
    public List<Example> train_examples;
    public List<Example> test_examples;

    public DetectionModel() {
        this.train_examples = new ArrayList<Example>();
        this.test_examples = new ArrayList<Example>();

    }

    public abstract void setExamples(int mode, List<Element> elements, List<Integer> labels, int fileID);

    public void train(List<Example> examples){

        // Preparing the SVM param
        svm_parameter param=new svm_parameter();
        param.svm_type=svm_parameter.C_SVC;
        param.kernel_type=svm_parameter.RBF;
        param.gamma=0.01; // orig 0.5
        param.nu=0.5;
        param.cache_size=2000;
        param.C=1;  // orig 1
        param.eps=0.1; // orig 0.001
        param.p=0.01; // orig 0.1


        //prepare data
        HashMap<Integer, HashMap<Integer, Double>> featuresTraining=new HashMap<Integer, HashMap<Integer, Double>>();
        HashMap<Integer, Integer> labelTraining=new HashMap<Integer, Integer>();

        HashSet<Integer> features=new HashSet<Integer>();

        for(Example example : examples){
            featuresTraining.put(example.getId(), new HashMap<Integer, Double>());
            labelTraining.put(example.getId(), example.getLabel());
            for(Feature feature : example.getFeatures()){
                features.add(feature.getId());
                featuresTraining.get(example.getId()).put(feature.getId(), feature.getValue());
            }
        }

        //train model

        svm_problem prob=new svm_problem();
        int numTrainingInstances=featuresTraining.keySet().size();
        prob.l=numTrainingInstances;
        prob.y=new double[prob.l];
        prob.x=new svm_node[prob.l][];

        for(int i=0;i<numTrainingInstances;i++) {
            prob.x[i] = new svm_node[featuresTraining.get(i).keySet().size()];
            int indx=0;
            for(Integer id:featuresTraining.get(i).keySet()){
                svm_node node=new svm_node();
                node.index=id;
                node.value=featuresTraining.get(i).get(id);
                prob.x[i][indx]=node;
                indx++;
            }
            prob.y[i]=labelTraining.get(i);
        }

        System.out.println(numTrainingInstances);
        //System.out.println(Arrays.toString(prob.y));

        if(Detect.debug == true){
            for(int i=0; i<prob.x.length ; i++){
                for( int j=0; j<prob.x[i].length; j++){
                    System.out.print(prob.x[i][j].value);
                    System.out.print(" ");
                }
                System.out.println();

            }
        }
        this.model=svm.svm_train(prob,param);
    }

    public void test(List<Example> examples, List<Integer> result){
        //prepare data
        HashMap<Integer, HashMap<Integer, Double>> featuresTesting=new HashMap<Integer, HashMap<Integer, Double>>();

        for(Example example : examples){
            featuresTesting.put(example.getId(), new HashMap<Integer, Double>());
            for(Feature feature : example.getFeatures()){
                featuresTesting.get(example.getId()).put(feature.getId(), feature.getValue());
            }
        }

        for(Integer testInstance:featuresTesting.keySet()){
            int numFeatures = featuresTesting.get(testInstance).keySet().size();
            svm_node[] x = new svm_node[numFeatures];
            int featureIndx = 0;
            for(Integer feature : featuresTesting.get(testInstance).keySet()){
                x[featureIndx] = new svm_node();
                x[featureIndx].index = feature;
                x[featureIndx].value = featuresTesting.get(testInstance).get(feature);
                featureIndx++;
            }

            double d=svm.svm_predict(this.model, x);
            result.add((int)d);
        }
    }
}
