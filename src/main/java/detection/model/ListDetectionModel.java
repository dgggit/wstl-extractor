package detection.model;

import data.Example;
import feature.*;
import libsvm.svm_parameter;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Petar on 07.07.2016.
 */
public class ListDetectionModel extends DetectionModel {

    public int id = 0;

    public ListDetectionModel(){

        super();
        this.param.svm_type= svm_parameter.C_SVC;
        this.param.kernel_type=svm_parameter.RBF;
        this.param.gamma=2; // orig 0.5
        this.param.nu=0.5; // orig 0.5
        this.param.cache_size=2000;
        this.param.C=1;  // ori g 1
        this.param.eps=0.001; // orig 0.001
        this.param.p=1; // orig 0.1


    }

    public void setExamples(int mode, List<Element> elements, List<Integer> labels, int fileID){
        List<Feature> features;
        this.id = 0;
        int tableID = 0;

        for(int idx=0; idx<elements.size(); idx++){
            tableID+=1;
            Element element = elements.get(idx);
            features = new ArrayList<Feature>();

            try {
                features.add(new AlphaNumRatioFeature("continuous"));
                features.add(new CountNonChildTagsFeature("continuous"));
                features.add(new ImgCountFeature("continuous"));
                features.add(new LinkCountFeature("continuous"));
                features.add(new MaxRowsFeature("continuous"));
                features.add(new RowAvgTextLengthFeature("continuous"));
                features.add(new TextLengthFeature("continuous"));
                features.add(new WordFreqFeature("continuous", "specification"));
                features.add(new ContainsDelimiterFeature("continuous"));
                for (Feature feature : features) {
                    feature.compute(element);
                }
                // TODO label not always 1
                Example example = new Example(this.id, element, labels.get(idx), features);
                if(mode == 0){
                    train_examples.add(example);
                }
                else if(mode == 1){
                    test_examples.add(example);
                }
                this.id += 1;

            } catch (Exception e) {

            }
        }
    }

}
