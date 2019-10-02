package feature;

import org.jsoup.nodes.Element;

/**
 * Created by Petar on 28.06.2016.
 */
public class ContainsDelimiterFeature extends Feature {


    public ContainsDelimiterFeature(String type) {
        super(type, 12);
    }

    public void compute(Element element) {
        this.value = (double) (( element.text().contains(":") || element.text().contains(";") ) ? 1.0 : 0.0 ) ;
    }


}
