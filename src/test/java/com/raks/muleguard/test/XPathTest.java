package com.raks.muleguard.test;

import org.dom4j.Document;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.util.List;

/**
 * Quick test to verify XPath attribute selection
 */
public class XPathTest {
    public static void main(String[] args) throws Exception {
        String xmlFile = "C:\\muleguard-fixed\\tmp\\muleguard\\testData\\rakstestmuleapi\\src\\main\\mule\\rakstestmuleapi.xml";
        String xpath = "//ibm-mq:connection/@cipherSuite";

        SAXReader reader = new SAXReader();
        Document document = reader.read(new File(xmlFile));
        List<Node> nodes = document.selectNodes(xpath);

        System.out.println("Found " + nodes.size() + " nodes");
        for (Node node : nodes) {
            System.out.println("Node type: " + node.getNodeType());
            System.out.println("Node name: " + node.getName());
            System.out.println("Node text: " + node.getText());
            System.out.println("Node string value: " + node.getStringValue());
        }
    }
}
