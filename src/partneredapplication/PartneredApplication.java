/*
 * Program Development Start Date : 10/23/2018
 * Partnered Application Project
 * Problem :
 */
package partneredapplication;
// Main Utility
import java.util.*;
import java.util.ArrayList;

// XML Parsing Packages
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;

// File Handling Packages
import java.io.IOException;
import java.io.File;
import java.io.FileReader;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

// XML Handlers
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

// Encryption Packages
import java.io.IOException;
import javax.crypto.KeyGenerator;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

/**
 * Group Members
 * @author cs.evsnyder
 * @author cs.cwberg
 */

public class PartneredApplication {
 
    public static String computerusername = System.getProperty("user.name");
    // Account Information Database
    public static ArrayList<String> StoredUsernames = new ArrayList<String>();
    public static ArrayList<String> StoredPins = new ArrayList<String>();
    public static ArrayList<String> StoredIds = new ArrayList<String>();
    public static ArrayList<SecretKey> StoredKeys = new ArrayList<SecretKey>();
    public static ArrayList<String> StoredActClasses = new ArrayList<String>();
    public static ArrayList<String> StoredActUsernames = new ArrayList<String>();
    public static ArrayList<String> StoredActPasswords = new ArrayList<String>();
    public static ArrayList<String> StoredActExtras = new ArrayList<String>();
    public static boolean accountFound = false;  
    
    // Main Method
    public static void main(String[] args) throws Exception {
        // Show Loading Status UI
        ServerConnect.displayGUI();
         try{
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            String db_url      = "jdbc:mysql://den1.mysql6.gear.host/csaver";
            String db_username = "csaver";
            String db_password = "Hi2T_Tb?fZV6";
            Connection con = DriverManager.getConnection(db_url, db_username, db_password);
            //Connection con = DriverManager.getConnection("jdbc:mysql://den1.mysql6.gear.host" + "usercsaver&password=Hi2T_Tb?fZV6&autoReconnect=true&failOverReadOnly=false&maxReconnects=10");
            String query = "SELECT * FROM accountdata";
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(query);
            
            while(rs.next()){
               String currentAccount = rs.getString("username");
               if(currentAccount.equals(computerusername)){
                   accountFound = true;
                   break;
               }
            }
            
            if(accountFound){
                Thread.sleep(240);
                ServerConnect.hideGUI();
                
                System.out.println("Account found, prompting PIN.");
                ExistingAccount.displayGUI();
            }
            else{
                Thread.sleep(240);
                ServerConnect.hideGUI();
                System.out.println("Account not found, prompting creation.");
                NewAccount.displayGUI();
            }
            con.close();
        }
        catch(ClassNotFoundException | IllegalAccessException | InstantiationException | InterruptedException | SQLException e){
            e.printStackTrace();
        }
        // Pull XML File from SQL Server and save locally.
        
        // Load XML File and create local storage.
        try{
          // Instance a document-builder in order to read file.
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(false);
            factory.setValidating(false);
            DocumentBuilder builder = factory.newDocumentBuilder();

            File file = new File("src/partneredapplication/accountStorage.xml");
            Document document = builder.parse(file);
            Element accountStorage = document.getDocumentElement();
            NodeList accounts = accountStorage.getChildNodes();
            for(int i = 0, ii = 0, n = accounts.getLength(); i<n; i++){
                Node child = accounts.item(i);
                if(child.getNodeType() != Node.ELEMENT_NODE)
                    continue;
               Element account = (Element)child;
               ii++;

               // Add items / attributes to local storage.
               StoredIds.add(account.getAttribute("id"));
               StoredUsernames.add(getAccountData(findFirstNamedElement(child,"username")));
               StoredPins.add(getAccountData(findFirstNamedElement(child,"pin")));

            }

        }  catch(IOException | ParserConfigurationException | SAXException e){
            e.printStackTrace();
        }

      // Encryption Example

      /*
       * SecretKey key = KeyGenerator.getInstance("DES").generateKey();
       * DesEncrypter encrypter = new DesEncrypter(key);
       * String encrypted = encrypter.encrypt("1h8f&n101");
       * String decrypted = encrypter.decrypt(encrypted);
      */

      /*
          ServerConnect.hideGUI();
        
          // Creates account if one is not created.
          String computerUsername = System.getProperty("user.name");
          boolean accountExists = verifyExistance(computerUsername);
          
           // Prompt UI in both cases.
          if(accountExists){
            System.out.println("Account found, prompting PIN.");
            ExistingAccount.displayGUI();
          }
          else{
            System.out.println("Account not found, prompting creation.");
            NewAccount.displayGUI();
          }
      */

    }
    //Hide previous UI
   

    public static void removeActEntry(String username, String actclass, String actusername, String actpassword, String actextras){
        String previousActExtras = StoredActExtras.get(returnIndex(username));
        String previousActUsernames = StoredActUsernames.get(returnIndex(username));
        String previousActPasswords = StoredActPasswords.get(returnIndex(username));
        String previousActClasses = StoredActClasses.get(returnIndex(username));

        String editedActExtras = previousActExtras.replaceAll(actextras+"\n", "");
        String editedActUsernames = previousActUsernames.replaceAll(actusername+"\n", "");
        String editedActPasswords = previousActPasswords.replaceAll(actpassword+"\n", "");
        String editedActClasses = previousActClasses.replaceAll(actclass+"\n", "");

        StoredActExtras.set(returnIndex(username), editedActExtras);
        StoredActClasses.set(returnIndex(username), editedActClasses);
        StoredActUsernames.set(returnIndex(username), editedActUsernames);
        StoredActPasswords.set(returnIndex(username), editedActPasswords);
    }

    // Split compiled information into an array.
    public static String[] returnSplitInformation(String username, String aclass){
        if(aclass.equals("usernames")){
            String compiledData = StoredActUsernames.get(returnIndex(username));
            String[] splitData = compiledData.split("\n");
            return splitData;
        }
        if(aclass.equals("passwords")){
            String compiledData = StoredActPasswords.get(returnIndex(username));
            String[] splitData = compiledData.split("\n");
            return splitData;
        }
        if(aclass.equals("classes")){
            String compiledData = StoredActClasses.get(returnIndex(username));
            String[] splitData = compiledData.split("\n");
            return splitData;
        }
        if(aclass.equals("extras")){
            String compiledData = StoredActExtras.get(returnIndex(username));
            String[] splitData = compiledData.split("\n");
            return splitData;
        }
        return null;
    }

    // Prerequisite Verification / 4-Digit Pin Code.
    public static boolean verifyCredentials(String username, String pin){
        for(int i=0; i<(StoredUsernames.size()); i++){
            if(username.equals((String)StoredUsernames.get(i))){
                if(((String)StoredPins.get(i)).equals(pin)){
                    //System.out.println("PinCorrectCredentials");
                    return true;
                }
                if(((String)StoredPins.get(i)).equals("NoPin")){
                    //System.out.println("NoPinCorrectCredentials");
                    return true;
                }
                else{
                    System.out.println("Incorrect Pin.");
                    return false;
                }
            }
        }
        System.out.println("Account does not exist, prompting account creation for "+username);
        return false;
    }

    // Add account data entry.
    public static void addActEntry(String username, String actclass, String actusername, String actpassword, String actextras){
      if(StoredActExtras.get(returnIndex(username)).equals("")){
        StoredActExtras.set(returnIndex(username), actextras);
        StoredActClasses.set(returnIndex(username), actclass);
        StoredActUsernames.set(returnIndex(username), actusername);
        StoredActPasswords.set(returnIndex(username), actpassword);
      }
      else{
        StoredActExtras.set(returnIndex(username), StoredActExtras.get(returnIndex(username))+"\n"+actextras);
        StoredActClasses.set(returnIndex(username), StoredActClasses.get(returnIndex(username))+"\n"+actclass);
        StoredActUsernames.set(returnIndex(username), StoredActUsernames.get(returnIndex(username))+"\n"+actusername);
        StoredActPasswords.set(returnIndex(username), StoredActPasswords.get(returnIndex(username))+"\n"+actpassword);
      }
    }

    public static int returnIndex(String username){
      for(int i=0; i<(StoredUsernames.size()); i++){
        if(username.equals((String)StoredUsernames.get(i))){
          //System.out.println("Account Exists.");
          return i;
        }
        else{
          //System.out.println("Account does not exist.");
          return i;
        }
      }
      return 0;
    }

    // Verify Existance in case of duplicate prevention.
    public static boolean verifyExistance(String username){
        for(int i=0; i<(StoredUsernames.size()); i++){
          if(username.equals((String)StoredUsernames.get(i))){
            //System.out.println("Account Exists.");
            return true;
          }
          else{
            //System.out.println("Account does not exist.");
            return false;
          }
        }
        return false;
      }

    // Function for creating account if account is not already created.
    public static void createAccount(String username, String pin){
      boolean existingaccount = verifyExistance(username);
      if(existingaccount == true){
        return;
      }
      else{
        StoredUsernames.add(username);
        if(pin != null){
          StoredPins.add(pin);
          StoredActUsernames.add("");
          StoredActPasswords.add("");
          StoredActClasses.add("");
          StoredActExtras.add("");
        }
        else{
          StoredPins.add("NoPin");
          StoredActUsernames.add("");
          StoredActPasswords.add("");
          StoredActClasses.add("");
          StoredActExtras.add("");
        }
      }
    }

    // Find XML Node at the Given Element Name.
    static private Node findFirstNamedElement(Node parent, String tagName){
        NodeList children = parent.getChildNodes();
        for (int i = 0, in = children.getLength(); i<in; i++){
            Node child = children.item(i);
             if ( child.getNodeType() != Node.ELEMENT_NODE )
                continue;
             if ( child.getNodeName().equals(tagName) )
                return child;
        }
        return null;
    }

    // Find the Main Node for the Account
    static private String getAccountData(Node parent){
        StringBuilder text = new StringBuilder();
        if(parent == null)
            return text.toString();
        NodeList children = parent.getChildNodes();
        for(int k = 0, kn = children.getLength(); k < kn; k++){
            Node child = children.item(k);
            if(child.getNodeType() != Node.TEXT_NODE)
                break;
            text.append(child.getNodeValue());
        }
        return text.toString();
    }

    // Clear Local Data for Updating.
    public static void removeAll(Node node, short nodeType, String name, Object id) {

        if (node.getNodeType() == nodeType && (name == null || node.getNodeName().equals(name))) {
          Element acID = (Element)node;
          if(acID.getAttribute("id").equals(id)){
            node.getParentNode().removeChild(node);
            }
        } else {
          NodeList list = node.getChildNodes();
          for (int i = 0; i < list.getLength(); i++) {
            removeAll(list.item(i), nodeType, name, id);
          }
        }
    }

    private static final String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
       public static String randomAlphaNumeric(int count) {
           StringBuilder builder = new StringBuilder();
       while (count-- != 0) {
           int character = (int)(Math.random()*ALPHA_NUMERIC_STRING.length());
           builder.append(ALPHA_NUMERIC_STRING.charAt(character));
       }
       return builder.toString();
    }
}
