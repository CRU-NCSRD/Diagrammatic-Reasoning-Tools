/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.demokritos.iit.cru.creativity.reasoning.diagrammatic;

import eu.lingua.mts.services.Translator;
import gr.demokritos.iit.cru.creativity.utilities.Connect;
import gr.demokritos.iit.cru.creativity.utilities.WNAccess;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author George
 */
public class DiagrammaticComputationalTools {

    private String language;
    private WNAccess wn;
    private Set<String> stop;
    private Set<String> off;
    private Class stemCLass;
    private Connection conn;

    public DiagrammaticComputationalTools(Connect x) throws ClassNotFoundException, SQLException {
        Connect c = x;
        this.language = c.getLanguage();
        this.wn = c.getWn();//only english
        this.stop = c.getStop();
        this.off = c.getOff();
        // this.stemCLass = c.getStemCLass();
        this.conn = c.getConn();
    }

    public HashSet<String> ConceptGraphAbstractionEngine(String entity) throws InstantiationException, IllegalAccessException {
        HashSet<String> GraphAbstraction = new HashSet<String>();
        if ((!this.stop.contains(entity)) && (!this.off.contains(entity))) {
            GraphAbstraction = wn.getHyponymsAndHypernyms(entity);
        }
        return GraphAbstraction;
    }

    public HashSet<String> ConceptGraphPolymerismEngine(String entity) throws InstantiationException, IllegalAccessException {
        HashSet<String> graphPolymerism = new HashSet<String>();
        if ((!this.stop.contains(entity)) && (!this.off.contains(entity))) {
            graphPolymerism = wn.getMeronyms(entity);
        }
        return graphPolymerism;
    }

    public HashSet<String> FactRetriever(String term, String role) throws SQLException {
        HashSet<String> Facts = new HashSet<String>();
        Queries q = new Queries(this.conn);
        if (role.equalsIgnoreCase("relation")) {
            Facts = q.getEntitiesWithRel(term);
        } else if (role.equalsIgnoreCase("object")) {
            Facts = q.getRelOfObject(term);
        } else if (role.equalsIgnoreCase("subject")) {
            Facts = q.getRelOfSubject(term);
        }
        return Facts;
    }

    public HashSet<String> FindConcepts(String concept, int difficulty, String category) throws InstantiationException, IllegalAccessException, SQLException, ClassNotFoundException, Exception {
        HashSet<String> concepts = new HashSet<String>();
        HashSet<String> TempConcepts = new HashSet<String>();

        ///---------translate
        concept = Translator.bingTranslate(concept, this.language, "en", "general");
        if (wn.getCommonPos(concept) == null) { ////new addition if the word cannot be translated to english
            return concepts;
        }

        if (category.equalsIgnoreCase("equivalence")) {
            //to be done
        } else if (category.equalsIgnoreCase("subsumption")) {
            for (int i = 0; i < difficulty; i++) {//for the given dificculty, abstract as many times
                TempConcepts = ConceptGraphPolymerismEngine(concept); //polymerism doesn't return many
                if (TempConcepts.isEmpty()) {
                    break;
                }
                concepts = TempConcepts;//define the temporary conecpts as new conecpts in case the next concepts are empty
                //take one concept at random from the abstraction, and make it the new concept
                int pointer = new Random().nextInt(concepts.size());
                int c = 0;
                for (String k : concepts) {
                    if (c == pointer) {
                        concept = k;
                    }
                    c = c + 1;
                }
            }
        } else if (category.equalsIgnoreCase("supersumption")) {
            for (int i = 0; i < difficulty; i++) {
                TempConcepts = ConceptGraphAbstractionEngine(concept);
                if (TempConcepts.isEmpty()) {
                    break;
                }
                concepts = TempConcepts;//define the temporary concepts as new conecpts in case the next concepts are empty
                //take one concept at random from the abstraction, and make it the new concept
                int pointer = new Random().nextInt(concepts.size());
                int c = 0;
                for (String k : concepts) {
                    if (c == pointer) {
                        concept = k;
                    }
                    c = c + 1;
                }
            }
        } else {
            String type = "subject";
            //take alternatively the object of the subject and the subject of the object until diff is reached
            for (int i = 0; i < difficulty; i++) {
                TempConcepts = FactRetriever(concept, type);
                if (TempConcepts.isEmpty()) {
                    break;
                }
                concepts = TempConcepts;
                int pointer = new Random().nextInt(concepts.size());
                int c = 0;
                for (String k : concepts) {
                    if (c == pointer) {
                        if (type.equalsIgnoreCase("subject")) {
                            if (!k.split("---")[1].isEmpty()) {
                                concept = k.split("---")[1];//take the object
                                type = "object";
                            }
                        } else {
                            if (!k.split("---")[0].isEmpty()) {
                                concept = k.split("---")[0];//take the subject
                                type = "subject";
                            }
                        }
                    }
                    c = c + 1;
                }
            }
        }
        HashSet<String> finCon = new HashSet<String>();

        ///---------translate
        if (!this.language.equalsIgnoreCase("en")) {
            for (String s : concepts) {
                String n = Translator.bingTranslate(s, "en", this.language, "general");
                if (wn.getCommonPos(n) == null) {//if the word is not english
                    finCon.add(n);
                }
            }
        } else {
            finCon.addAll(concepts);
        }
        return finCon;
    }

    public HashSet<String> FindRelations(ArrayList<String> tri, int difficulty, String category) throws InstantiationException, IllegalAccessException, SQLException, ClassNotFoundException, Exception {
        HashSet<String> relations = new HashSet<String>();
        HashSet<String> TempRelations = new HashSet<String>();
        HashSet<String> newConcepts = new HashSet<String>();
        ArrayList<String> triple = new ArrayList<String>();

        ///---------translate
        for (String s : tri) {
            String h = Translator.bingTranslate(s, this.language, "en", "general");
            if (wn.getCommonPos(h) == null) {
               // System.out.println(s + " out " + h);
                return newConcepts; //if any element of the triple cannot be translated, relations cannot be found
            }
            triple.add(h);
        }
        // System.out.println(triple);
//run abstraction for relation to find new relations and work with them
        String relation = triple.get(2);
        for (int i = 0; i < difficulty; i++) {
            TempRelations = ConceptGraphAbstractionEngine(relation);
            if (TempRelations.isEmpty()) {
                break;
            }
            relations = TempRelations;
            int pointer = new Random().nextInt(relations.size());
            int c = 0;
            for (String k : relations) {
                if (c == pointer) {
                    relation = k;
                }
                c = c + 1;
            }
        }
        //take the similar concepts to the subject and the object of the triple
        if (category.equalsIgnoreCase("subsumption")) {
            newConcepts = ConceptGraphPolymerismEngine(triple.get(0));//wn.getMeronyms(triple.get(0));
            newConcepts.addAll(ConceptGraphPolymerismEngine(triple.get(1)));//wn.getMeronyms(triple.get(1));
        } else if (category.equalsIgnoreCase("supersumption")) {
            newConcepts = ConceptGraphAbstractionEngine(triple.get(0));// wn.getHyponymsAndHypernyms(triple.get(0));
            newConcepts.addAll(ConceptGraphAbstractionEngine(triple.get(1)));//wn.getHyponymsAndHypernyms(triple.get(1)));
        }
        System.out.println("new concepts "+newConcepts);
        //take concepts that have the relations found
      //  System.out.println("newConc " + newConcepts);
        for (String r : relations) {
            System.out.println(r);
            //System.out.println("relations " + r);
            HashSet<String> temp = FactRetriever(r, "relation");
            for (String g : temp) {
                //keep the subject and the object of the triples
                newConcepts.add(g.split("---")[0]);
                newConcepts.add(g.split("---")[1]);///////////////!!!!!!check if subj/obj=''
            }
        }

        //find relations based on the new concepts
        HashSet<String> newRelations = new HashSet<String>();
        for (String s : newConcepts) {
            // System.out.println("concepts " + s);
            newRelations.addAll(FactRetriever(s, "object"));
            newRelations.addAll(FactRetriever(s, "subject"));
        }
       // System.out.println("newRels " + newRelations);
        /*
         HashSet<String> rels = new HashSet<String>();
         ///---------translate
         if (!this.language.equalsIgnoreCase("en")) {
         for (String s : newRelations) {
         System.out.println(s);
         String n = Translator.bingTranslate(s, "en", this.language, "general");
         if (wn.getCommonPos(n) == null) {//if the word is not english
         rels.add(n);
         }
         }
         } else {
         rels.addAll(newRelations);
         }*/
        return newRelations;
    }

    public HashSet<String> Graphs(JSONArray triples1, JSONArray triples2) throws FileNotFoundException, UnsupportedEncodingException, URISyntaxException, IOException, ParserConfigurationException, SAXException, XPathExpressionException, SQLException {
        // JSON a=new JSON();
        /*
         JSONArray users = new JSONArray();
         JSONObject obj1 = new JSONObject();
         obj1.put("c1", "rambo");
         obj1.put("rel", "likes");
         obj1.put("c2", "mambo");
         users.add(obj1);
         JSONObject obj2 = new JSONObject();
         obj2.put("c1", "rambo");
         obj2.put("rel", "likes");
         obj2.put("c2", "mambo");
         users.add(obj2);
         Object[] m = users.toArray();*/
        String filenameSource = "C:\\Users\\George\\Desktop\\projects\\diagrammatic\\triples1.rdf";// + System.currentTimeMillis() + 
        PrintWriter writer = new PrintWriter(filenameSource, "UTF-8");
        writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n"
                + "         xmlns=\"http://c2learn.eu/onto1#\"\n"///http://www.semagrow.eu/schemas/family#
                + "xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\"\n"
                + "         xmlns:owl=\"http://www.w3.org/2002/07/owl#\">\n"
                + "\n"
                + "<owl:Ontology rdf:about=\"\">\n"
                + "</owl:Ontology>\n"
                + "\n");

        for (Object triple : triples1) {
            // System.out.println(triple.toString());
            JSONObject jtriple = (JSONObject) triple;
            String concept = jtriple.get("c1").toString().toLowerCase();
            String relation = jtriple.get("rel").toString().toLowerCase();
            String subject = jtriple.get("c2").toString().toLowerCase();
            //  writer.write(" <owl:Class rdf:about=" + concept + ">\n"
            //          + "</owl:Class>\n");
            if (relation.equalsIgnoreCase("isA")) {
                writer.write("<owl:Class rdf:ID=\"" + concept + "\">\n");
                writer.write("<rdfs:label>" + concept + "</rdfs:label>\n");
                writer.write("<owl:subclassof rdf:resource=\"#" + subject + "\"/>\n");
                writer.write("</owl:Class>\n");
            }
            /*else {
             writer.write("<owl:ObjectProperty rdf:ID=\"" + relation + "\">\n");
             writer.write("<rdfs:range rdf:resource=\"#" + subject + "\"/>\n");
             writer.write("<rdfs:domain rdf:resource=\"#" + concept + "\"/>\n");
             writer.write("<rdfs:label>" + relation + "</rdfs:label>\n");
             writer.write("</owl:ObjectProperty>\n");
             }*/
            writer.write("\n");
        }
        writer.write("</rdf:RDF>");
        writer.close();

        String filenameTarget = "C:\\Users\\George\\Desktop\\projects\\diagrammatic\\triples2.rdf";////" + System.currentTimeMillis() + "

        writer = new PrintWriter(filenameTarget, "UTF-8");
        writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n"
                + "         xmlns=\"http://c2learn.eu/onto2#\"\n"//http://www.semagrow.eu/schemas/family#
                + "xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\"\n"
                + "         xmlns:owl=\"http://www.w3.org/2002/07/owl#\">\n"
                + "\n"
                + "<owl:Ontology rdf:about=\"\">\n"
                + "</owl:Ontology>\n"
                + "\n");
        for (Object triple : triples1) {
            // System.out.println(triple.toString());
            JSONObject jtriple = (JSONObject) triple;
            String concept = jtriple.get("c1").toString().toLowerCase();
            String relation = jtriple.get("rel").toString().toLowerCase();
            String subject = jtriple.get("c2").toString().toLowerCase();
            //  writer.write(" <owl:Class rdf:about=" + concept + ">\n"
            //          + "</owl:Class>\n");
            if (relation.equalsIgnoreCase("isA")) {
                writer.write("<owl:Class rdf:ID=\"" + concept + "\">\n");
                writer.write("<rdfs:label>" + concept + "</rdfs:label>\n");
                writer.write("<owl:subclassof rdf:resource=\"#" + subject + "\"/>\n");
                writer.write("</owl:Class>\n");
            }
            /*else {
             writer.write("<owl:ObjectProperty rdf:ID=\"" + relation + "\">\n");
             writer.write("<rdfs:range rdf:resource=\"#" + subject + "\"/>\n");
             writer.write("<rdfs:domain rdf:resource=\"#" + concept + "\"/>\n");
             writer.write("<rdfs:label>" + relation + "</rdfs:label>\n");
             writer.write("</owl:ObjectProperty>\n");
             }*/
            writer.write("\n");
        }
        writer.write("</rdf:RDF>");
        writer.close();

        String fileSeparator = "\\";
        //String oaeiDirectorySubsConceptsOnly = "C:/Users/antonis/Documents/NetBeansProjects/Synthesis/benchmarks";
        //   String oaeiDirectoryEqualsConceptsOnly = "C:/Users/antonis/Documents/NetBeansProjects/Synthesis/benchmarks";
        //String onto1 = "http://oaei.ontologymatching.org/2011/benchmarks/101/onto.rdf";
        //String onto2 = "http://oaei.ontologymatching.org/2011/benchmarks/248/onto.rdf";
        String onto1 = filenameSource;// oaeiDirectoryEqualsConceptsOnly + fileSeparator + source + fileSeparator + "onto.rdf";;
        String onto2 = filenameTarget;// oaeiDirectoryEqualsConceptsOnly + fileSeparator + target + fileSeparator + "onto.rdf";;;

        onto1 = new File(onto1).toURI().toString();
        onto2 = new File(onto2).toURI().toString();

        String outputPath = System.getProperty("user.dir") + fileSeparator;
        // System.out.println(outputPath + "refalign.rdf");
/*
         ResultsStorage storePrecisionCSR = new ResultsStorage("precisionCSR.txt");
         ResultsStorage storeRecalCSR = new ResultsStorage("recallCSR.txt");

         ResultsStorage storePrecisionVSM = new ResultsStorage("precisionVSM.txt");
         ResultsStorage storeRecalVSM = new ResultsStorage("recallVSM.txt");

         ResultsStorage storePrecisionCSRFactor = new ResultsStorage("precisionCSRFactor.txt");
         ResultsStorage storeRecalCSRFactor = new ResultsStorage("recallCSRFactor.txt");

         ResultsStorage storePrecisionVSMFactor = new ResultsStorage("precisionVSMFactor.txt");
         ResultsStorage storeRecalVSMFactor = new ResultsStorage("recallVSMFactor.txt");

         ResultsStorage storePrecisionCocluFactor = new ResultsStorage("precisionCocluFactor.txt");
         ResultsStorage storeRecalCocluFactor = new ResultsStorage("recallCocluFactor.txt");

         ResultsStorage storePrecisionCoclu = new ResultsStorage("precisionCoclu.txt");
         ResultsStorage storeRecalCoclu = new ResultsStorage("recallCoclu.txt");

         ResultsStorage storeCorrectCSR = new ResultsStorage("correctCSR.txt");
         ResultsStorage storeErrorCSR = new ResultsStorage("errorCSR.txt");

         ResultsStorage storeCorrectCoclu = new ResultsStorage("correctCoclu.txt");
         ResultsStorage storeErrorCoclu = new ResultsStorage("errorCoclu.txt");

         ResultsStorage storeCorrectCocluFactor = new ResultsStorage("correctCocluFactor.txt");
         ResultsStorage storeErrorCocluFactor = new ResultsStorage("errorCocluFactor.txt");

         ResultsStorage storeConfuzedEqualsForSubsCoclu = new ResultsStorage("ConfuzedEqualsForSubsCoclu.txt");
         ResultsStorage storeConfuzedEqualsForSubsCocluFactor = new ResultsStorage("ConfuzedEqualsForSubsCocluFactor.txt");

         ResultsStorage storeCorrectVSM = new ResultsStorage("correctVSM.txt");
         ResultsStorage storeErrorVSM = new ResultsStorage("errorVSM.txt");
         ResultsStorage storeCorrectCSRFactor = new ResultsStorage("correctCSRFactor.txt");
         ResultsStorage storeErrorCSRFactor = new ResultsStorage("errorCSRFactor.txt");
         ResultsStorage storeCorrectVSMFactor = new ResultsStorage("correctVSMFactor.txt");
         ResultsStorage storeErrorVSMFactor = new ResultsStorage("errorVSMFactor.txt");
         ResultsStorage storeConfuzedEqualsForSubsVSM = new ResultsStorage("ConfuzedEqualsForSubsVSM.txt");
         ResultsStorage storeConfuzedEqualsForSubsVSMFactor = new ResultsStorage("ConfuzedEqualsForSubsVSMFactor.txt");
         ResultsStorage storeConflictsEquals = new ResultsStorage("ConflictsEquals.txt");
         ResultsStorage storeConflictsSubs = new ResultsStorage("ConflictsSubs.txt");
         ResultsStorage storeMessages = new ResultsStorage("Messages.txt");

         ResultsStorage timeDuration = new ResultsStorage("timeDuration.txt");

         try {
         RunCoclouVsmCsrFactorGraphs synthesis = new RunCoclouVsmCsrFactorGraphs(onto1, onto2, outputPath,
         outputPath + "refalign.rdf", outputPath + "refalign.rdf", false, false, true,
         true,
         1, false, false, 0, 0.3, true, false, "over", "j48", false, false,
         10000, false, storePrecisionCSR, storeRecalCSR, storePrecisionVSM, storeRecalVSM,
         storePrecisionCSRFactor, storeRecalCSRFactor, storePrecisionVSMFactor, storeRecalVSMFactor,
         storeCorrectCSR, storeErrorCSR, storeCorrectVSM, storeErrorVSM, storeCorrectCSRFactor,
         storeErrorCSRFactor, storeCorrectVSMFactor, storeErrorVSMFactor, storeConfuzedEqualsForSubsVSM,
         storeConfuzedEqualsForSubsVSMFactor, storePrecisionCoclu, storeRecalCoclu, storeCorrectCoclu,
         storeErrorCoclu, storeConfuzedEqualsForSubsCoclu, storePrecisionCocluFactor, storeRecalCocluFactor,
         storeCorrectCocluFactor, storeErrorCocluFactor, storeConfuzedEqualsForSubsCocluFactor,
         timeDuration, storeConflictsEquals, storeConflictsSubs, storeMessages, true);
         synthesis.run();
         System.out.println(filenameTarget + " finished...");
         } catch (Exception e) {
         e.printStackTrace();
         }

         storePrecisionCSR.exportToFile();
         storeRecalCSR.exportToFile();
         storePrecisionVSM.exportToFile();
         storeRecalVSM.exportToFile();
         storePrecisionCSRFactor.exportToFile();
         storeRecalCSRFactor.exportToFile();
         storePrecisionVSMFactor.exportToFile();
         storeRecalVSMFactor.exportToFile();
         storeCorrectCSR.exportToFile();
         storeErrorCSR.exportToFile();
         storeCorrectVSM.exportToFile();
         storeErrorVSM.exportToFile();
         storeCorrectCSRFactor.exportToFile();
         storeErrorCSRFactor.exportToFile();
         storeCorrectVSMFactor.exportToFile();
         storeErrorVSMFactor.exportToFile();
         storeConfuzedEqualsForSubsVSM.exportToFile();
         storeConfuzedEqualsForSubsVSMFactor.exportToFile();
         storePrecisionCoclu.exportToFile();
         storeRecalCoclu.exportToFile();
         storeCorrectCoclu.exportToFile();
         storeErrorCoclu.exportToFile();
         storeConfuzedEqualsForSubsCoclu.exportToFile();
         storePrecisionCocluFactor.exportToFile();
         storeRecalCocluFactor.exportToFile();
         storeCorrectCocluFactor.exportToFile();
         storeErrorCocluFactor.exportToFile();
         storeConfuzedEqualsForSubsCocluFactor.exportToFile();
         timeDuration.exportToFile();
         storeConflictsEquals.exportToFile();
         storeConflictsSubs.exportToFile();
         storeMessages.exportToFile();

         storePrecisionCSR.close();
         storeRecalCSR.close();
         storePrecisionVSM.close();
         storeRecalVSM.close();
         storePrecisionCSRFactor.close();
         storeRecalCSRFactor.close();
         storePrecisionVSMFactor.close();
         storeRecalVSMFactor.close();
         storeCorrectCSR.close();
         storeErrorCSR.close();
         storeCorrectVSM.close();
         storeErrorVSM.close();
         storeCorrectCSRFactor.close();
         storeErrorCSRFactor.close();
         storeCorrectVSMFactor.close();
         storeErrorVSMFactor.close();
         storeConfuzedEqualsForSubsVSM.close();
         storeConfuzedEqualsForSubsVSMFactor.close();
         storePrecisionCoclu.close();
         storeRecalCoclu.close();
         storeCorrectCoclu.close();
         storeErrorCoclu.close();
         storeConfuzedEqualsForSubsCoclu.close();
         storePrecisionCocluFactor.close();
         storeRecalCocluFactor.close();
         storeCorrectCocluFactor.close();
         storeErrorCocluFactor.close();
         storeConfuzedEqualsForSubsCocluFactor.close();
         timeDuration.close();
         storeConflictsEquals.close();
         storeConflictsSubs.close();
         storeMessages.close();*/

        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;
        Document document = null;
        try {
            builder = builderFactory.newDocumentBuilder();
            document = (Document) builder.parse(new FileInputStream(outputPath + "alignment.rdf"));
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        document.getDocumentElement().normalize();
        System.out.println("Root element :" + document.getDocumentElement().getNodeName());

        NodeList nl = document.getElementsByTagName("Cell");

        System.out.println("----------------------------");
        for (int temp = 0; temp < nl.getLength(); temp++) {

            Node nNode = nl.item(temp);

            System.out.println("\nCurrent Element :" + nNode.getNodeName());

            if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                Element eElement = (Element) nNode;
                Element ent1 = (Element) eElement.getElementsByTagName("entity1").item(0);
                Element ent2 = (Element) eElement.getElementsByTagName("entity2").item(0);
                System.out.println(ent1.getAttribute("rdf:resource").split("#")[1]);
                System.out.println(ent2.getAttribute("rdf:resource").split("#")[1]);
                //  System.out.println("Staff id : " + eElement.getAttribute("rdf:resource"));
                System.out.println("relation : " + eElement.getElementsByTagName("relation").item(0).getTextContent());
                System.out.println("measure : " + eElement.getElementsByTagName("measure").item(0).getTextContent());
                if (eElement.getElementsByTagName("relation").item(0).getTextContent().equalsIgnoreCase("=")) {
                    Queries q = new Queries(this.conn);
                    q.Insert(ent1.getAttribute("rdf:resource").split("#")[1], ent2.getAttribute("rdf:resource").split("#")[1], eElement.getElementsByTagName("measure").item(0).getTextContent());
                }
            }
        }
      //  String expression = "//Alignment//Cell";
        //   XPath xPath = XPathFactory.newInstance().newXPath();
        //   XPathExpression expr = xPath.compile(expression);

        //   NodeList nl = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
        //   for (Node n : nl) {
        //         System.out.println(n.getNodeName());
        //   }
        //  Object[] idsNode = node.evaluateXPath("//Alignment/Cell");
//read a string value
        // Node node = (Node) xPath.compile(expression).evaluate(document, XPathConstants.NODE);
        /*
         BufferedReader in = new BufferedReader(new FileReader(outputPath + "alignment.rdf"));
         String line;
         while ((line = in.readLine()) != null) {

         }*/
        return new HashSet<String>();
    }
}
