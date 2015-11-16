/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.demokritos.iit.cru.creativity.reasoning.diagrammatic;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;

/**
 *
 * @author George
 */
public class Queries {

    private Connection conn;

    public Queries(Connection conn) {
        this.conn = conn;
    }
    /*
     public static void main(String[] args) throws IOException, SQLException, ClassNotFoundException {
     Class.forName("com.mysql.jdbc.Driver");
     String connection_string = "jdbc:mysql://127.0.0.1:3306/diagrammatic";
     Connection conn = DriverManager.getConnection(connection_string, "root", "pass");
     Queries q = new Queries(conn);
     HashSet<String> rels = q.getCategoryEntities("book");//latitudelongitude tropicana vouzon recipe
     for (String s : rels) {
     System.out.println(s);
     }
     }*/

    //return the relations of a subject
    public HashSet<String> getRelOfSubject(String subj) throws SQLException {
        Statement statement = conn.createStatement();
        String q = "SELECT distinct Best_Entity_literalString,Best_Value_literalString,relation,probability FROM cmnell where match(Best_Entity_literalString) against('" + subj + "') and Best_Value_literalString!=''"; //like '%subj%' and levenstein(Best_Entity_literalString,'" + subj + "')<=" + d;
        System.out.println(q);
        // System.out.println(q);
        ResultSet rs;
        rs = statement.executeQuery(q);
        HashSet<String> s = new HashSet<String>();
        while (rs.next()) {
            String t = rs.getString("relation").trim().replace("concept:", "");
            if (!t.isEmpty()) {
                s.add(rs.getString("Best_Entity_literalString") + "---" + rs.getString("Best_Value_literalString") + "---" + t + "---" + rs.getString("probability"));
            }
        }
        return s;
    }

    //return the relations of an object
    public HashSet<String> getRelOfObject(String obj) throws SQLException {
        Statement statement = conn.createStatement();
        String q = "SELECT distinct Best_Entity_literalString, Best_Value_literalString, relation, probability FROM cmnell where match(Best_Value_literalString) against('" + obj + "') and Best_Entity_literalString!=''";//levenstein(Best_Value_literalString,'" + obj + "')<=" + d;

        //  System.out.println(q);
        ResultSet rs;
        rs = statement.executeQuery(q);
        HashSet<String> s = new HashSet<String>();
        while (rs.next()) {
            String t = rs.getString("relation").trim().replace("concept:", "");;
            if (!t.isEmpty()) {
                s.add(rs.getString("Best_Entity_literalString") + "---" + rs.getString("Best_Value_literalString") + "---" + t + "---" + rs.getString("probability"));
            }
        }
        return s;
    }

    //return the relations of an object and a subject
    public HashSet<String> getRelOfPair(String subj, String obj) throws SQLException {
        Statement statement = conn.createStatement();
        String q = "SELECT distinct Best_Entity_literalString,Best_Value_literalString,relation, probability FROM cmnell where match(Best_Entity_literalString) against('" + subj + "') and match(Best_Value_literalString) against('" + obj + "')";//levenstein(Best_Entity_literalString,'" + subj + "')<=" + d + " and levenstein(Best_Value_literalString,'" + obj + "')<=" + d;
        ResultSet rs;
        rs = statement.executeQuery(q);
        HashSet<String> s = new HashSet<String>();
        while (rs.next()) {
            String t = rs.getString("relation").trim().replace("concept:", "");;
            if (!t.isEmpty()) {
                s.add(rs.getString("Best_Entity_literalString") + "---" + rs.getString("Best_Value_literalString") + "---" + t + "---" + rs.getString("probability"));
            }
        }
        return s;
    }

    //return the values and objects that have this relation
    public HashSet<String> getEntitiesWithRel(String rel) throws SQLException {
        Statement statement = conn.createStatement();//relation has consept:!!!!!!!!!!!!!!!!!
        String q = "SELECT distinct Best_Entity_literalString,Best_Value_literalString,relation, probability  FROM cmnell where match(relation) against('" + rel + "')"; //levenstein(relation,'" + rel + "')<=" + d;
        ResultSet rs;
        rs = statement.executeQuery(q);
        //System.out.println(q);
        HashSet s = new HashSet<String>();
        while (rs.next()) {
            String t = rs.getString("relation").trim().replace("concept:", "");;
            if (!t.isEmpty()) {
                s.add(rs.getString("Best_Entity_literalString") + "---" + rs.getString("Best_Value_literalString") + "---" + t + "---" + rs.getString("probability"));
            }
        }
        return s;
    }

    //return the categories of an object or value
    public HashSet<String> getEntityCategories(String ent) throws SQLException {
        Statement statement = conn.createStatement();
        String q = "SELECT distinct Categories_for_Entity as cat FROM cmnell where match(Best_Entity_literalString) against('" + ent + "') UNION distinct SELECT Categories_for_Value as cat FROM diagrammatic.cmnell where match(Best_Value_literalString) against('" + ent + "')";//levenstein(Best_Entity_literalString,'" + ent + "')<=" + d + " UNION SELECT Categories_for_Value as cat FROM diagrammatic.cmnell where levenstein(Best_Value_literalString,'" + ent + "')<=" + d;
        System.out.println(q);
        ResultSet rs;
        rs = statement.executeQuery(q);
        HashSet<String> s = new HashSet<String>();
        while (rs.next()) {
            String cat = rs.getString("cat").replace("concept:", "").trim();
            if (!cat.isEmpty()) {
                s.add(cat);
            }
        }
        return s;
    }

    //return the objects or values that have this category
    public HashSet<String> getCategoryEntities(String cat) throws SQLException {
        Statement statement = conn.createStatement(); /// category has concept:!!!!!!!!!!!!!!!!!!!!!!
        String q = "SELECT distinct Best_Entity_literalString as st FROM cmnell where match(Categories_for_Entity) against('" + cat + "') UNION SELECT distinct Best_Value_literalString as st FROM diagrammatic.cmnell where match(Categories_for_Value) against('" + cat + "')";//levenstein(Categories_for_Entity,'" + cat + "')<=" + d + " UNION SELECT Best_Value_Literal_String as st FROM diagrammatic.cmnell where levenstein (Categories_for_Value,'" + cat + "')<=" + d;
        // System.out.println(q);
        ResultSet rs;
        rs = statement.executeQuery(q);
        HashSet<String> s = new HashSet<String>();
        while (rs.next()) {
            String st = rs.getString("st").trim();
            if (!st.isEmpty()) {
                s.add(st);
            }
        }
        return s;
    }

    public void Insert(String ent1, String ent2, String prob) throws SQLException {
        Statement statement = conn.createStatement();
        //CHANGE TEMP TO CMNELL
        String q = "INSERT INTO temp (entity,relation,value,probability) VALUES (\"" + ent1 + "\",\"isA\",\"" + ent2 + "\",\"" + prob + "\")";
        System.out.println(q);
        statement.executeUpdate(q);

    }
}
