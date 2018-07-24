/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.btmu.util;

import java.io.File;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author u000783
 */
public class FileConverter {

    public Element parseHTML(String filename) throws ParserConfigurationException, SAXException, IOException {
        File xmlFile = new File(filename);
        //SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        //Schema schema = factory.newSchema(new File(config.getProperty("xsd")));
        //Validator validator = schema.newValidator();
        //validator.validate(new StreamSource(xmlFile));

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(xmlFile);
        doc.getDocumentElement().normalize();

        return doc.getDocumentElement();
    }

    public void processStep(Node step) {
        NodeList nodes = step.getChildNodes();
        String[] stepData = new String[3];
        int j = 0;
        for (int i = 0; i < nodes.getLength(); i++) {
            Node n = nodes.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                stepData[j++] = n.getTextContent();
            }
        }
        if (j < 3) {
            return;
        }

        String type = stepData[0].replace("open", "url");
        type = type.replace("type", "input");
        type = type.replace("clickAndWait", "image");

        System.out.print("        <step type=\"" + type + "\"");

        if (type.equalsIgnoreCase("url")) {
            System.out.println(" value=\"" + stepData[1] + "\"/>");
        } else if (type.equalsIgnoreCase("image")) {
            System.out.println(" by=\"" + stepData[1].replaceFirst("=", ":").replace('"', '\'') + "\"/>");
        } else {
            System.out.println(" by=\"" + stepData[1].replaceFirst("=", ":").replace('"', '\'') + "\" value=\"" + stepData[2].replaceFirst("label=", "") + "\"/>");
        }

    }

    public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException {
        String testfile = "inFile.html";

        for (int i = 0; i < args.length; i++) {
            if (args[i].equalsIgnoreCase("-t")) {
                testfile = args[i + 1];
            }
        }

        FileConverter fc = new FileConverter();
        Element root = fc.parseHTML(testfile);
        NodeList nl = root.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE && n.getNodeName().equalsIgnoreCase("body")) {
                NodeList steps = n.getChildNodes();
                for (int j = 0; j < steps.getLength(); j++) {
                    Node nstep = steps.item(j);
                    if (nstep.getNodeType() == Node.ELEMENT_NODE && nstep.getNodeName().equalsIgnoreCase("table")) {
                        NodeList actions = nstep.getLastChild().getChildNodes();
                        System.out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
                        System.out.println("<testcase xmlns=\"com.mufg.ast\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"TestCase.xsd\">");
                        System.out.println("    <case id=\"000000\" type=\"-----\">");
                        for (int k = 0; k < actions.getLength(); k++) {
                            fc.processStep(actions.item(k));
                        }
                        System.out.println("    </case>");
                        System.out.println("</testcase>");
                    }
                }
            }
        }
    }
}
