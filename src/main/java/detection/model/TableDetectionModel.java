package detection.model;

import data.Example;
import feature.*;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Petar on 07.07.2016.
 */
public class TableDetectionModel extends DetectionModel {

    public int id = 0;
    public void setExamples(int mode, List<Element> elements, List<Integer> labels, int fileID){
        List<Feature> features;
        this.id = 0;
        int tableID = 0;

        for(int idx=0; idx<elements.size(); idx++){
            tableID+=1;
            Element element = elements.get(idx);
            features = new ArrayList<Feature>();

            features.add(new AlphaNumRatioFeature("continuous"));
            features.add(new AvgColumnsFeature("continuous"));
            features.add(new CountNonChildTagsFeature("continuous"));
            features.add(new ImgCountFeature("continuous"));
            features.add(new LinkCountFeature("continuous"));
            features.add(new MaxColumnsFeature("continuous"));
            features.add(new MaxRowsFeature("continuous"));
            features.add(new RowAvgTextLengthFeature("continuous"));
            features.add(new StdDevColumnsFeature("continuous"));
            features.add(new TextLengthFeature("continuous"));
            features.add(new WordFreqFeature("continuous", "specification"));
            for(Feature feature : features){
                feature.compute(element);
            }
            // TODO label not always 1

            int exampleID = fileID*1000000 + tableID;
            Example example = new Example(this.id, element, labels.get(idx), features);
            if(mode == 0){
                train_examples.add(example);
            }
            else if(mode == 1){
                test_examples.add(example);
            }
            this.id+=1;
        }
    }
}
