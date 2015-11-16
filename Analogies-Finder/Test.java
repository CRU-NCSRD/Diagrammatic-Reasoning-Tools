/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.demokritos.iit.cru.creativity.reasoning.diagrammatic;

import eu.c2learn.crawlers.BingCrawler;
import eu.lingua.mts.services.Translator;
import static eu.lingua.mts.services.Translator.bingTranslate;
import gr.demokritos.iit.cru.creativity.utilities.Connect;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import org.apache.commons.lang3.StringEscapeUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.xml.sax.SAXException;

/**
 *
 * @author George
 */
public class Test {

    public static void main(String[] args) throws IOException, SQLException, ClassNotFoundException, InstantiationException, IllegalAccessException, FileNotFoundException, UnsupportedEncodingException, URISyntaxException, ParserConfigurationException, SAXException, XPathExpressionException, Exception {
      /*  Connect c = new Connect("en");
        DiagrammaticComputationalTools d = new DiagrammaticComputationalTools(c);
        ArrayList<String> s = new ArrayList<String>();
        s.add("george");
        s.add("thing");
        s.add("loves");
      //  System.out.println(d.FindConcepts("friend", 4, "supersumption"));
        System.out.println(d.FindRelations(s,4, "supersumption"));
        /*    String text = "white";
         String src = "en";
         String trg = "el";
         int id = 1;
         String response = Translator.bingTranslate(text, src, trg, "general");
         System.out.println(response);  
         */

        JSONArray a = new JSONArray();
        JSONObject obj1 = new JSONObject();
        obj1.put("c1", "cat");
        obj1.put("rel", "isA");
        obj1.put("c2", "animal");
        a.add(obj1);
        JSONObject obj2 = new JSONObject();
        obj2.put("c1", "animal");
        obj2.put("rel", "likes");
        obj2.put("c2", "animal");
        a.add(obj2);
        JSONObject obj3 = new JSONObject();
        obj3.put("c1", "dog");
        obj3.put("rel", "isA");
        obj3.put("c2", "animal");
        a.add(obj3);
        JSONObject obj4 = new JSONObject();
        obj4.put("c1", "wolf");
        obj4.put("rel", "isA");
        obj4.put("c2", "animal");
        a.add(obj4);

        System.out.println(a);

       /* JSONArray b = new JSONArray();
        obj1 = new JSONObject();
        obj1.put("c1", "cat");
        obj1.put("rel", "isA");
        obj1.put("c2", "animal");
        b.add(obj1);
        obj2 = new JSONObject();
        obj2.put("c1", "cat");
        obj2.put("rel", "likes");
        obj2.put("c2", "fish");
        b.add(obj2);
        obj3 = new JSONObject();
        obj3.put("c1", "dog");
        obj3.put("rel", "isA");
        obj3.put("c2", "animal");
        b.add(obj3);
        obj4 = new JSONObject();
        obj4.put("c1", "wolf");
        obj4.put("rel", "isA");
        obj4.put("c2", "animal");
        b.add(obj4);
        JSONObject obj5 = new JSONObject();
        obj5.put("c1", "wolf");
        obj5.put("rel", "likes");
        obj5.put("c2", "meat");
        b.add(obj5);
        FileWriter file = new FileWriter("C:\\Users\\George\\Desktop\\animals.json");
        file.write(b.toJSONString());
        file.flush();
        file.close();*/

        //  System.out.println(a);
        // System.out.println(b);
     /*   d.Graphs(a, b);
         List<String> urls = new ArrayList<String>();
         List<String> urls_temp = new ArrayList<String>();
         */
        // String bingAppId = "a1Q3b3YdomwYyknyDHIykZx0A5Xc5n445mYiXoCfXC8=";
        //  BingCrawler bc = new BingCrawler(bingAppId, "en");
        //  List<String> urls = bc.crawlImages(phrase);

        /*         
         urls_temp = HTMLUtilities.linkExtractor("http://www.dmoz.org/search?q=" + phrase + "&cat=Kids_and_Teens&all=yes", "UTF-8", 1);
         for (String url : urls_temp) {
         urls.add(StringEscapeUtils.unescapeHtml4(url));
         }*/
         // HashSet<String> p=d.FactRetriever("haswikipediaurl","relation");
        // HashSet<String> kl = d.FindConcepts("love",2, "concept");//.Relations(s, 2, "relation");
        // System.out.println(kl.size());*/
        // System.out.println(list.toString());
        /* HashSet<String> p=d.Relations(s, 3, "equivalence");//.Facts("haswikipediaurl","relation");//.Concepts("vouzon",3,"relation");//.ConceptFinder("love",3, "supersumption");//FactRetriever("vouzon","subject"); // 
         //ConceptGraphAbstraction("love");//.FactRetriever("vouzon", "subject");//.ConceptGraphPolymerismEngine("automobile");    
         for(String j:p){
         System.out.println(j);
         }
         for (String g : urls) {
         System.out.println(g);
         }*/
    }
}
