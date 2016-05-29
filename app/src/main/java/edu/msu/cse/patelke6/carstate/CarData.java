package edu.msu.cse.patelke6.carstate;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 * Created by keyurpatel on 3/22/16.
 * CarData Class reads values from the XML CarData file and performs operations on this file and
 * its content
 */
public class CarData {

    private Document doc;
    private File carDataXML;
    private Node mCarNode;
    private Context mContext;
    CarData(String xmlFilePath, Context context){
        carDataXML = new File(xmlFilePath);
        mContext = context;
        getCarDataXML();

    }

    //Reads the CarData.xml into a Document
    public void getCarDataXML(){

        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            doc = docBuilder.parse(carDataXML);
            mCarNode = doc.getElementsByTagName(edu.msu.cse.patelke6.carstate.XMLNodeNames.CarNode).item(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getMaxSpeed(){

        Node maxSpeedNode = getSettingNodeByType(mCarNode, edu.msu.cse.patelke6.carstate.XMLNodeNames.MaxSpeed);
        if(maxSpeedNode == null)
            return 0;
        //Log.i("CarData", "MaxSpeedSet: " + maxSpeedNode.getTextContent());
        return Integer.parseInt(maxSpeedNode.getTextContent());
    }

    public Boolean setMaxSpeed(int maxSpeed){
        Node maxSpeedNode = getSettingNodeByType(mCarNode, edu.msu.cse.patelke6.carstate.XMLNodeNames.MaxSpeed);
        if(maxSpeedNode == null)
            return false;
        else {
            maxSpeedNode.setTextContent(Integer.toString(maxSpeed));
            return true;
        }
    }

    public Boolean getEnforceSeatBelt(){
        getCarDataXML(); //upate the document incase it was written into

        Node enforceSeatBeltNode = getSettingNodeByType(mCarNode, edu.msu.cse.patelke6.carstate.XMLNodeNames.EnforceSeatBelt);
        if(enforceSeatBeltNode == null)
            return false;
        else if (enforceSeatBeltNode.getTextContent().equals("0")) {
            return false;
        }
        else {
            return true;
        }
    }

    public Boolean setEnforceSeatBelt(String value){
        Node enforceSeatBeltNode = getSettingNodeByType(mCarNode, edu.msu.cse.patelke6.carstate.XMLNodeNames.EnforceSeatBelt);
        if(enforceSeatBeltNode == null)
            return false;
        else {
            enforceSeatBeltNode.setTextContent(value);
            return true;
        }

    }


     public int getLowerSeatPosition(){
         Node lowerSeatPosition = getSettingNodeByType(mCarNode, edu.msu.cse.patelke6.carstate.XMLNodeNames.SeatPositionX);
         if(lowerSeatPosition == null)
             return 50;
         else
             return Integer.parseInt(lowerSeatPosition.getTextContent());
     }

    public boolean setLowerSeatPosition(int newValue){
        Node lowerSeatPosition = getSettingNodeByType(mCarNode, edu.msu.cse.patelke6.carstate.XMLNodeNames.SeatPositionX);
        if(lowerSeatPosition == null)
            return false;
        else {
            lowerSeatPosition.setTextContent(Integer.toString(newValue));
            updateXMLFile();
            return true;
        }
    }

    public String getRadioStations(){
        Node node = getSettingNodeByType(mCarNode, edu.msu.cse.patelke6.carstate.XMLNodeNames.RadioStations);
        if(node == null)
            return null;
        else
            return node.getTextContent();
    }

    public boolean setRadioStations(String stations){
        Node node = getSettingNodeByType(mCarNode, edu.msu.cse.patelke6.carstate.XMLNodeNames.RadioStations);
        if(node == null)
            return false;
        else {
            node.setTextContent(stations);
            return true;
        }
    }



    public Node getSettingNodeByType(Node node, String typeStr) {

        NodeList childrenNode = node.getChildNodes();
        Node n ;
        NodeList cNL = null;
        for (int i = 0; i < childrenNode.getLength(); i++) {
            n = childrenNode.item(i);
            if (n.hasChildNodes()) {
                if (n.getNodeName().equals(edu.msu.cse.patelke6.carstate.XMLNodeNames.Settings)) {
                    cNL = n.getChildNodes();
                    for (int b = 0; b < cNL.getLength(); b++) {
                        if (cNL.item(b).hasAttributes()) {
                            Attr attr = (Attr) cNL.item(b).getAttributes().getNamedItem(edu.msu.cse.patelke6.carstate.XMLNodeNames.TypeAttribute);
                            if (attr.getValue().equals(typeStr)) {
                                return cNL.item(b);
                            }
                        }
                    }
                }
            }
        }
        return null;
    }


    private Node getChildNodeByTag(Node node, String tagName){
        NodeList cNL = null;
            if (node.hasChildNodes()) {
                cNL = node.getChildNodes();
                for (int b = 0; b < cNL.getLength(); b++) {
                    if (cNL.item(b).getNodeName().equals(tagName)) {
                        return cNL.item(b);
                    }
                }

            }
        return null;
    }

    //Write the updated values back to the xml file.
    public void updateXMLFile(){
        try{
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(carDataXML);
            transformer.transform(source, result);
        }
        catch (Exception ex){
            Log.i("UpdateXMLFile Error", ex.getMessage());
        }
    }

    public void personalizeSettings(){
        String authUserID = getAuthenticatedUserID();
        if(!authUserID.isEmpty()){
            Node authUserNode = getUserNode(authUserID);
            if(authUserNode != null) {
               // Log.i("CarData", "personalizedSettings for Auth User Node " + authUserNode.getTextContent());
                setLowerSeatPosition(Integer.parseInt(getSettingNodeByType(authUserNode, edu.msu.cse.patelke6.carstate.XMLNodeNames.SeatPositionX).getTextContent()));
                setEnforceSeatBelt(getSettingNodeByType(authUserNode, edu.msu.cse.patelke6.carstate.XMLNodeNames.EnforceSeatBelt).getTextContent());
                setMaxSpeed(Integer.parseInt(getSettingNodeByType(authUserNode, edu.msu.cse.patelke6.carstate.XMLNodeNames.MaxSpeed).getTextContent()));
                if(!getSettingNodeByType(authUserNode, edu.msu.cse.patelke6.carstate.XMLNodeNames.RadioStations).getTextContent().equals("") || !getSettingNodeByType(authUserNode, edu.msu.cse.patelke6.carstate.XMLNodeNames.RadioStations).getTextContent().isEmpty())
                    setRadioStations(getSettingNodeByType(authUserNode, edu.msu.cse.patelke6.carstate.XMLNodeNames.RadioStations).getTextContent());

                //Once a user has been authenticated set the value to null so user is only authenticated once.
                setAuthenticatedUserID("");
                Toast.makeText(this.mContext, "User Authenticated: " + getChildNodeByTag(authUserNode, edu.msu.cse.patelke6.carstate.XMLNodeNames.FirstName).getTextContent(), Toast.LENGTH_LONG).show();
                updateXMLFile();
            }


        }

    }

    private Node getUserNode(String userBssid){
        NodeList users = doc.getElementsByTagName(edu.msu.cse.patelke6.carstate.XMLNodeNames.UserNode);
        Node n = null;
        NodeList cNL = null;
        for (int i = 0; i < users.getLength(); i++) {
            n = users.item(i);
            if (n.hasChildNodes()) {
                cNL = n.getChildNodes();
                for (int b = 0; b < cNL.getLength(); b++) {
                    if (cNL.item(b).getNodeName().equals(edu.msu.cse.patelke6.carstate.XMLNodeNames.BSSID)) {
                        if (cNL.item(b).getTextContent().equals(userBssid)) {
                            return n;
                        }

                    }
                }

            }
        }

    return null;
    }


    public String getAuthenticatedUserID(){
        Node node = doc.getElementsByTagName(edu.msu.cse.patelke6.carstate.XMLNodeNames.UserAuthenticated).item(0);
        if(node.getTextContent().equals("0"))
            return "";
        return node.getTextContent();
    }

    public void setAuthenticatedUserID(String userID){
        Node node = doc.getElementsByTagName(edu.msu.cse.patelke6.carstate.XMLNodeNames.UserAuthenticated).item(0);
        node.setTextContent(userID);
    }

}
