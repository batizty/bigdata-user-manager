package com.weibo.datasys.HadoopConfigs;


import org.apache.commons.lang.StringUtils;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.StringWriter;


/**
 * Created by tuoyu on 12/8/16.
 */
public class HadoopPolicy {
    /**
     * most import property
     **/
    private static final String dfs_acl = "security.client.protocol.acl";

    NodeList nList;
    Document doc;


    public String getStringFromDocument() {
        try {
            DOMSource domSource = new DOMSource(doc);
            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.transform(domSource, result);
            return writer.toString();
        } catch (TransformerException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public HadoopPolicy(String file) {
        try {
            File fXmlFile = new File("hadoop-policy.xml");
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            doc = dBuilder.parse(fXmlFile);
            doc.getDocumentElement().normalize();
            nList = doc.getElementsByTagName("property");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String toXML() {
        return getStringFromDocument();
    }

    private String getNodeName(Element e) {
        return e.getElementsByTagName("name").item(0).getTextContent();
    }

    private String getNodeValue(Element e) {
        return e.getElementsByTagName("value").item(0).getTextContent();
    }

    public void setNodeValue(Element e, String value) throws DOMException {
        e.getElementsByTagName("value").item(0).setTextContent(value);
    }

    public String getValueByName(String name) {
        if (StringUtils.isBlank(name)) {
            return null;
        }

        Element e = getElementByName(name);
        if (e != null) {
            return getNodeValue(e);
        }

        return null;
    }

    public Element getElementByName(String name) {
        if (StringUtils.isBlank(name)) {
            return null;
        }

        if (nList.getLength() <= 0) {
            return null;
        }

        for (int temp = 0; temp < nList.getLength(); temp++) {
            Node nNode = nList.item(temp);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) nNode;
                if (StringUtils.equals(name, getNodeName(eElement))) {
                    return eElement;
                }
            }
        }

        return null;
    }

    public void setValueByName(String name, String value) throws DOMException {
        if (StringUtils.isBlank(name)) {
            //TODO throw error
            return;
        }

        if (StringUtils.isBlank(value)) {
            // TODO throw error
            return;
        }

        Element e = getElementByName(name);
        if (e != null) {
            setNodeValue(e, value);
            return;
        }

        return;
    }

    public void setHDFSAcl(String[] users, String[] groups) {
        String us = StringUtils.join(users, ",");
        String gs = StringUtils.join(groups, ",");
        String users_and_groups = StringUtils.join(new String[]{us, gs}, " ");
        setValueByName(dfs_acl, users_and_groups);
    }

    public static void main(String[] args) {
        HadoopPolicy hdp = new HadoopPolicy("hadoop-policy.xml");

        String value = hdp.getValueByName(HadoopPolicy.dfs_acl);
        System.out.println(" before dfs_acl = " + value);

        hdp.setHDFSAcl(new String[]{"tuoyu", "facai"}, new String[]{"hadoop", "tuoyu"});
        value = hdp.getValueByName(HadoopPolicy.dfs_acl);
        System.out.println(" after dfs_acl = " + value);


        System.out.print(hdp.toXML());
        return;
    }

}
