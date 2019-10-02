package detection;

import detection.model.ListDetectionModel;
import detection.model.TableDetectionModel;
import org.json.simple.parser.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.json.simple.*;
import data.Example;

import java.io.FileReader;
import java.io.File;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;


/**
 * Created by Petar on 07.07.2016.
 */
public class Detect {

    public static boolean debug = false;
    public static void main(String[] args){

        // get model
        TableDetectionModel table_detect = new TableDetectionModel();
        ListDetectionModel list_detect = new ListDetectionModel();

        // get all inputs from html files.
        JSONArray infos = htmlIdGetter();
        Iterator<JSONObject> json_iter = infos.iterator();

        // extract elements and label them.
        int fileID = 0;
        List<Element> table_elems_valid = new ArrayList<Element>();
        List<Element> list_elems_valid = new ArrayList<Element>();
        List<Integer> table_labels_valid = new ArrayList<Integer>();
        List<Integer> list_labels_valid = new ArrayList<Integer>();

        while(json_iter.hasNext()) {
            fileID += 1;
            JSONObject js = json_iter.next();

            //open html
            String html = js.get("id_self").toString();
            File htmlfile = new
                    File("../wdc-dataset/fextraction-subset/" + html + ".html");

            Document doc = null;
            try {
                doc = Jsoup.parse(htmlfile, "UTF-8", "");
            } catch (Exception e) {
                System.out.println("No file for : " + html);
                continue;
            }

            // extract
            Elements table_elems = tableElementsExtractor(doc);
            Elements list_elems = listElementsExtractor(doc);

            // label data (1 positive, 0 negative, -1 invalid)
            List<Integer> table_labels = new ArrayList<Integer>();
            List<Integer> list_labels = new ArrayList<Integer>();
            setLabels(js, table_elems, list_elems, table_labels, list_labels);

            // validate dataset & add to _valid.
            for(int i=0; i<table_labels.size(); i++){
                if(table_labels.get(i) > -1){ // 1 or 0
                    table_elems_valid.add(table_elems.get(i));
                    table_labels_valid.add(table_labels.get(i));
                }
            }
            for(int i=0; i<list_labels.size(); i++){
                if(list_labels.get(i) > -1){ // 1 or 0
                    list_elems_valid.add(list_elems.get(i));
                    list_labels_valid.add(list_labels.get(i));
                }
            }
        }

        int label0 = 0;
        int label1 = 0;
        for(int i=0;  i<table_labels_valid.size(); i++){
            if(table_labels_valid.get(i) == 1)
                label1+=1;
            else
                label0+=1;
        }

        System.out.println("There are "+table_labels_valid.size()+"datas in TABLE.");
        System.out.println(label1+" datas are labeled 1");
        System.out.println(label0+" datas are labeled 0");

        label0 = 0;
        label1 = 0;
        for(int i=0;  i<list_labels_valid.size(); i++){
            if(list_labels_valid.get(i) == 1)
                label1+=1;
            else
                label0+=1;
        }

        System.out.println("There are "+list_labels_valid.size()+"datas in LISTS.");
        System.out.println(label1+" datas are labeled 1");
        System.out.println(label0+" datas are labeled 0");

        // setExamples , split into train / test.
        List<Element> table_elems_train = new ArrayList<Element>();
        List<Element> list_elems_train = new ArrayList<Element>();
        List<Integer> table_labels_train = new ArrayList<Integer>();
        List<Integer> list_labels_train = new ArrayList<Integer>();
        List<Element> table_elems_test = new ArrayList<Element>();
        List<Element> list_elems_test = new ArrayList<Element>();
        List<Integer> table_labels_test = new ArrayList<Integer>();
        List<Integer> list_labels_test = new ArrayList<Integer>();

        // generate train / test dataset.
        generateTrainTest(table_elems_valid, table_labels_valid, table_elems_train, table_labels_train, table_elems_test, table_labels_test);
        table_detect.setExamples( 0, table_elems_train, table_labels_train, fileID );
        table_detect.setExamples( 1, table_elems_test, table_labels_test, fileID );
        generateTrainTest(list_elems_valid, list_labels_valid, list_elems_train, list_labels_train, list_elems_test, list_labels_test);
        list_detect.setExamples( 0, list_elems_train, list_labels_train, fileID );
        list_detect.setExamples( 1, list_elems_test, list_labels_test, fileID );


        // train TableDetection for all data.
        table_detect.train(table_detect.train_examples);
        // train ListDetection for all data.
        list_detect.train(list_detect.train_examples);

        // predict.
        List<Integer> result_table = new ArrayList<Integer>();
        table_detect.test(table_detect.test_examples, result_table);
        List<Integer> result_list = new ArrayList<Integer>();
        list_detect.test(list_detect.test_examples, result_list);

        //evaluate.
        Evaluate.evaluate(table_labels_test, result_table);
        Evaluate.evaluate(list_labels_test, result_list);

    }
    public static JSONArray htmlIdGetter(){

        JSONArray infos = null;
        try {
            String filename = "../wdc-dataset/all_labeled_entities_revised.json";
            FileReader file = new FileReader(filename);

            JSONParser parser = new JSONParser();
            Object obj = parser.parse(file);
            infos = (JSONArray) obj;

        } catch (Exception e){
            e.printStackTrace();
        }
        return infos;
    }
    public static Elements tableElementsExtractor(Document doc){
        Elements te = doc.select("table");
        return te;
    }
    public static Elements listElementsExtractor(Document doc){
        Elements le = doc.select("li");
        return le;
    }
    public static void setLabels(JSONObject js, Elements table_elems, Elements list_elems,
                             List<Integer> table_labels, List<Integer> list_labels){

        // table.
        for(int i=0; i<table_elems.size(); i++){

            Element element = table_elems.get(i);
            Elements rows = element.getElementsByTag("tr");


            // Eliminating some dirty data, which is now unnecessary

//            if(rows.size() == 0){
//                table_labels.add(-1);
//                continue;
//            }

            boolean found = false;
            for(Element row : rows){
                Elements tup = row.getElementsByTag("td");
                String first;
                if(tup.size() > 0){
                    // TODO we get compare number,letter.
                    first = tup.get(0).text().strip().replaceAll("[^0-9a-zA-Z]", "");

                    // search from JSON.
                    JSONArray table_atts = (JSONArray) js.get("table_atts");
                    Iterator<String> iter= table_atts.iterator();
                    while(iter.hasNext()){
                        String kvStr = iter.next();
                        String key = kvStr.split(":")[0];

                        // System.out.println(first+"\t\t\t\t"+key);
                        if(key.equals(first)){
                            found = true;
                        }
                    }
                }
            }
            // TODO Now, we just find one, then that's match.
            if(found)
                table_labels.add(1);
            else
                table_labels.add(0);
        }

        // list.
        for(int i=0; i<list_elems.size(); i++){
            Element element = list_elems.get(i);
            String text = element.text();

            // Eliminating some dirty data, which is now unnecessary

//            if(text.length() == 0){
//                list_labels.add(-1);
//                continue;
//            }
//            if(text.length() == 0 || !(text.contains(":") || text.contains(";")) ){
//                list_labels.add(-1);
//                continue;
//            }


            boolean found = false;
            String attr = text.split(":|;")[0].strip();

            //Search from JSON
            JSONArray list_atts = (JSONArray) js.get("list_atts");
            Iterator<String> iter = list_atts.iterator();

            while(iter.hasNext()){
                String kvStr = iter.next();
                String key = kvStr.split(":")[0];


                if(key.equals(attr)){
                    found = true;
                    //System.out.println("YES : "+text+"\t"+attr+"\t"+kvStr);
                }
            }
            if(found) {
                list_labels.add(1);
            }
            else{
                list_labels.add(0);
                //System.out.println("NO  : "+text+"\t"+attr);
            }

        }

    }
    public static void generateTrainTest(List<Element> el, List<Integer> la, List<Element> eltr, List<Integer> latr, List<Element> elte, List<Integer> late){

        int size = el.size();
        int DENOM = 5;
        int trCnt = 0;
        int teCnt = 0;

        for(int i=0; i<size; i++) {

            double rand = Math.random();
            if (rand < 1.0 / DENOM) {
                elte.add(el.get(i));
                late.add(la.get(i));
                teCnt++;
            } else {
                eltr.add(el.get(i));
                latr.add(la.get(i));
                trCnt++;
            }

        }
        System.out.println("train dataset # : "+trCnt);
        System.out.println("test dataset #  : "+teCnt);
    }

}