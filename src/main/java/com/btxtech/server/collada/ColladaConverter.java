package com.btxtech.server.collada;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * 14.08.2015.
 */
public class ColladaConverter {

    public void read() throws ParserConfigurationException, IOException, SAXException, ColladaException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(new File("C:\\dev\\projects\\razarion\\code\\experimental-webgl\\src\\main\\resources\\collada\\cube1.dae"));

        NodeList geometryLibraryNodeList = doc.getElementsByTagName(ColladaXml.ELEMENT_LIBRARY_GEOMETRIES);
        if (geometryLibraryNodeList.getLength() != 1) {
            throw new ColladaException("Only one " + ColladaXml.ELEMENT_LIBRARY_GEOMETRIES + " in collada file expected. Found: " + geometryLibraryNodeList.getLength());
        }

        NodeList geometryNodeList = geometryLibraryNodeList.item(0).getChildNodes();
        List<Geometry> geometries = new ArrayList<>();
        for (int i = 0; i < geometryNodeList.getLength(); i++) {
            Node node = geometryNodeList.item(i);
            if (node.getNodeName().toUpperCase().equals(ColladaXml.ELEMENT_GEOMETRY.toUpperCase())) {
                // geometries.add(new Geometry(node));
                System.out.println("Geometry: " + new Geometry(node));
            }
        }

    }

    class TreeDumper {
        public void dump(Document doc) {
            dumpLoop(doc, "");
        }

        private void dumpLoop(Node node, String indent) {
            switch (node.getNodeType()) {
                case Node.CDATA_SECTION_NODE:
                    System.out.println(indent + "CDATA_SECTION_NODE");
                    break;
                case Node.COMMENT_NODE:
                    System.out.println(indent + "COMMENT_NODE");
                    break;
                case Node.DOCUMENT_FRAGMENT_NODE:
                    System.out.println(indent + "DOCUMENT_FRAGMENT_NODE");
                    break;
                case Node.DOCUMENT_NODE:
                    System.out.println(indent + "DOCUMENT_NODE");
                    break;
                case Node.DOCUMENT_TYPE_NODE:
                    System.out.println(indent + "DOCUMENT_TYPE_NODE");
                    break;
                case Node.ELEMENT_NODE:
                    System.out.println(indent + "ELEMENT_NODE");
                    break;
                case Node.ENTITY_NODE:
                    System.out.println(indent + "ENTITY_NODE");
                    break;
                case Node.ENTITY_REFERENCE_NODE:
                    System.out.println(indent + "ENTITY_REFERENCE_NODE");
                    break;
                case Node.NOTATION_NODE:
                    System.out.println(indent + "NOTATION_NODE");
                    break;
                case Node.PROCESSING_INSTRUCTION_NODE:
                    System.out.println(indent + "PROCESSING_INSTRUCTION_NODE");
                    break;
                case Node.TEXT_NODE:
                    System.out.println(indent + "TEXT_NODE");
                    break;
                default:
                    System.out.println(indent + "Unknown node");
                    break;
            }

            NodeList list = node.getChildNodes();
            for (int i = 0; i < list.getLength(); i++)
                dumpLoop(list.item(i), indent + "   ");

        }
    }
}
