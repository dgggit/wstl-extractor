package feature;

import detection.Detect;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Petar on 07.07.2016.
 */
public class AvgColumnsFeature extends Feature {

    public AvgColumnsFeature(String type) {
        super(type, 2);
    }

    public void compute(Element element) {
        List<Integer> counts = new ArrayList<Integer>();
        for(Element row : element.getElementsByTag("tr")){
            counts.add(row.getElementsByTag("td").size());
        }
        this.value = Math.min(avg(counts) , 10000.0) ;
    }

    private double avg(List<Integer> counts) {
        int sum = 0;
        for(int count: counts) sum += count;

        try{
            float value = sum/counts.size();
            return value;
        } catch (Exception e) {
            if(Detect.debug == true)
                System.out.print("divzero ");
            return 0.0;
        }
    }


}
