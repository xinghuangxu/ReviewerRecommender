/*
 Copyright Motahareh Bahrami Zanjani <mxbahramizanjani@wichita.edu>

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU Library General Public
License as published by the Free Software Foundation; either
version 2 of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Library General Public License for more details.

You should have received a copy of the GNU Library General Public
License along with this program; if not, write to the Free
Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

Author: Sara Bahrami
   mxbahramizanjani@wichita.edu
*/
package com.wichita.edu.crawler;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.json.JSONException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class RunMain {

    private static final int PAGE_SIZE = 1;

    public static String dirPath1 = "/home/sara/research/codereview/";

    public static void main(String args[]) throws JSONException, ParserConfigurationException, TransformerException {

        String urlpath = "https://git.eclipse.org/r/changes/?q=mylyn&o=DETAILED_LABELS&o=ALL_REVISIONS&o=ALL_FILES&o=MESSAGES";
        //String urlpath="https://gerrit-review.googlesource.com/changes/?q=gerrit&o=DETAILED_LABELS&o=ALL_REVISIONS&o=ALL_FILES&o=MESSAGES";
        //String urlpath="https://git.eclipse.org/r/changes/?q=platform&o=DETAILED_LABELS&o=ALL_REVISIONS&o=ALL_FILES&o=MESSAGES";
        ///making docnew for writing to new XML file
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document docnew = docBuilder.newDocument();
        //Adding rootelement "Reviews" to docnew
        Element rootElement = docnew.createElement("Reviews");
        docnew.appendChild(rootElement);
        int skipCount = 0;
        JsonArrayParser jsonArrayParser = getJsonArrayParserWithUrl(urlpath, skipCount);
        Set<String> visited = new HashSet();
        while (jsonArrayParser.hasMoreChange()) {
            if(visited.contains(jsonArrayParser.getChangeId())){
                System.out.println("Duplicate Found!!");
            }
            visited.add(jsonArrayParser.getChangeId());
            skipCount += PAGE_SIZE;
            jsonArrayParser = getJsonArrayParserWithUrl(urlpath, skipCount);
//            System.out.println(skipCount);
//
//            if (reviewElement(docnew, urlpath, skipCount) != null)
//                rootElement.appendChild(reviewElement(docnew, urlpath, skipCount));
//            skipCount = returnskipCount(urlpath, skipCount);
//            //System.out.println(skipCount);
//
//            hasmorechange = returnhasMoreChange(urlpath, skipCount);
            //System.out.println(morechange);

        }
    }

    public static String returnskipCount(String urlpath, String skipCount) throws JSONException {    //String url="https://git.eclipse.org/r/changes/?q=mylyn&o=ALL_REVISIONS&o=ALL_FILES&o=MESSAGES&n=2&N="+skipCount+"\"";
        String url = urlpath + "&n=1&N=" + skipCount;
        //String url=urlpath+"&n=1";
        JsonArrayParser obj = new JsonArrayParser(url);
        String newskipCount = obj.getSortkey();
        return newskipCount;
    }

    public static JsonArrayParser getJsonArrayParserWithUrl(String urlpath, Integer skipCount) throws JSONException {    //String url="https://git.eclipse.org/r/changes/?q=mylyn&o=ALL_REVISIONS&o=ALL_FILES&o=MESSAGES&n=2&N="+skipCount+"\"";
        String url = buildQueryUrl(urlpath, PAGE_SIZE, skipCount);
        return new JsonArrayParser(url);
    }

    private static String buildQueryUrl(String baseUrl, int pageSize, int skipCount) {
        return baseUrl + "&n=" + pageSize + "&start=" + skipCount;
    }

    //for each review in Gerrit returns related review Element
    public static Element reviewElement(Document docnew, String urlpath, String skipCount) throws JSONException, ParserConfigurationException, TransformerException {
        //String url="https://git.eclipse.org/r/changes/?q=mylyn&o=ALL_REVISIONS&o=ALL_FILES&o=MESSAGES&n=1&N="+skipCount+"\"";
        String url = urlpath + "&n=1&S=" + skipCount;
        //String url=urlpath+"&n=1";
        System.out.println(url);
        WritingJsontoXML obj = new WritingJsontoXML(url, docnew);
        return (obj.reviewJSONtoXML());
    }

    //Writing docnew to result.xml file
    public static void documenttoFile(Document docnew) throws TransformerException {
        //Writing docnew to result.xml file
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "Yes");
        StreamResult result = new StreamResult(new File(dirPath1 + "mylyncodereviewtest.xml"));
        DOMSource source = new DOMSource(docnew);
        transformer.transform(source, result);
    }

}
