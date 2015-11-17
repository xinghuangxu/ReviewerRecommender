package com.wichita.edu.recommender; /*
  Copyright Software Engineering Research laboratory <serl@cs.wichita.edu>

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
 
 */

 import org.xml.sax.Attributes;
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.DefaultHandler;

 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 import java.io.IOException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map.Entry;

 /*
   * @author: Sara Bahrami <mxbahramizanjani@wichita.edu>
   *
   * This class contains SAXParser for parsing the commit log and creates different HashMaps
   * Names are similar to Dictionary.java but values are different
   *
   * mPathReviewDict- key: File name with absolute path, value: an object from class of ReviewTuple (Change id-author name- date)
   * mPathReviewDict- this HashMap is used by class XFinder for calculating XFactor value for each file
   *
   * mBugReviewrInfo-key:BugId , value: Author name of each commit related to that bug id.
   *
   * mBugFileNameDict-key:bug id, value: all the files in commit history related to that bug id.
   *
   */
 public class DictionaryGitLog extends DefaultHandler{
     //key: File name with absolute path, value: an object from class of ReviewTuple (Change id-author name- date)
     HashMap<String, ArrayList<ReviewTuple>> mPathReviewDict;
     //key:BugId , value: Author name of each commit related to that bug id
     HashMap<String, ArrayList<String>> mBugReviewrInfo;
     //key:bug id, value: all the files in commit history related to that bug id.
     HashMap<String, ArrayList<String>> mBugFileNameDict;
     ArrayList<ReviewTuple> reviews;
     ArrayList<String> reviewerNames;
     ArrayList<String> fileNames;
     HashMap<String,String>mappedList;
     //ArrayList<StatusOwner> statuesownerlist;
     //keep the list of files in each patch set for each review
     ArrayList<String> files;
     String bugid;
     String status;
     String owner;
     String xmlLogFilepath;
     String tmpValue;
     ReviewTuple reviewNode;
     StatusOwner statusOwnerNode;
     String ChangeId;

     public DictionaryGitLog(String xmlLogFilepath) throws IOException
     {
         this.xmlLogFilepath = xmlLogFilepath;
         //System.out.println(this.xmlLogFilepath);
         this.mPathReviewDict =  new HashMap<String, ArrayList<ReviewTuple>>();
         this.mBugReviewrInfo=new HashMap<String, ArrayList<String>>();
         this.mBugFileNameDict=new HashMap<String, ArrayList<String>>();
         parseDocument();

     }

     private void parseDocument()
     {
         SAXParserFactory factory = SAXParserFactory.newInstance();
         try
         {
             SAXParser parser = factory.newSAXParser();
             parser.parse(xmlLogFilepath,this);
         }
         catch (ParserConfigurationException e)
         {
             e.printStackTrace();
         }
         catch (SAXException e)
         {
             e.printStackTrace();
         }
         catch (IOException e)
         {
             e.printStackTrace();
         }

     }

    public void printmPathReviewDict()
     {
        for (Entry<String, ArrayList<ReviewTuple>> entry : mPathReviewDict.entrySet())
           for(int i=0;i<entry.getValue().size();i++ )
             {

                   System.out.print(entry.getKey() + " ");
                 System.out.print(entry.getValue().get(i).mBugID + " ");
                 System.out.print(entry.getValue().get(i).mReviewer + " ");
                 System.out.println(entry.getValue().get(i).mDate);
             }
     }

    public void printmBugReviewrInfo()
    {
        for (Entry<String, ArrayList<String>> entry : mBugReviewrInfo.entrySet())
           for(int i=0;i<entry.getValue().size();i++ )
             {
                 System.out.print(entry.getKey() + " ");
                 System.out.println(entry.getValue().get(i));
             }
    }


    //Related function for SAX Parser
     public void startElement(String s, String s1, String element, Attributes attributes) throws SAXException
     {


         if(element.equalsIgnoreCase("Commit"))
         {
             ChangeId=attributes.getValue("ChangeId");
         }

         if (element.equalsIgnoreCase("BugId"))
         {
             files=new ArrayList<String>();
             bugid=attributes.getValue("Id");

         }


         if (element.equalsIgnoreCase("author"))
         {

             reviewNode = new ReviewTuple();
             //reviewNode.setmBugID(Integer.parseInt(bugid));
             // I changed this because for Microsoft project this is not the case that for each and every commit a related bug id exist.
             reviewNode.setmBugID(ChangeId);
             try {
                 reviewNode.setmDate(formatDate(attributes.getValue("Date")));
             } catch (ParseException e) {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             }
             reviewNode.setmReviewer(attributes.getValue("name"));
             AddElementtomBugReviewrInfo(bugid,attributes.getValue("name"));
         }

         if (element.equalsIgnoreCase("File"))
         {

             if(attributes.getValue("path").endsWith(".java"))

                 {
                     files.add(attributes.getValue("path"));
                     addElementtomBugFileName(bugid,attributes.getValue("path"));

                 }
         }

     }
     //Related function for SAX Parser
     public void endElement(String s, String s1, String element) throws SAXException
     {
         if(element.equals("Commit"))
         {
             files.clear();
         }

         //if element is author, add the author name and date to HasMap "mPathReviewDict"
         if(element.equalsIgnoreCase("Files"))
         {

             for(int k=0;k<files.size();k++)
             {
                 String fileName = files.get(k);
                 if (!mPathReviewDict.containsKey(fileName))
                     {
                         reviews=new ArrayList<ReviewTuple>();

                     }
                     else if (mPathReviewDict.containsKey(fileName))

                     {
                         reviews= mPathReviewDict.get(fileName);

                     }
                 reviews.add(reviewNode);
                 mPathReviewDict.put(fileName,reviews);

             }
         }

     }
    // Related function for SAX Parser
     public void characters(char[] ac, int i, int j) throws SAXException
     {
         tmpValue = new String(ac,i,j);
     }

     //Convert String to Date
     public Date formatDate(String date) throws ParseException
     {
         String pattern="E MMM dd HH:mm:ss Z yyyy";
          SimpleDateFormat format=new SimpleDateFormat(pattern);
          Date daterightformat=format.parse(date);
         return daterightformat;
     }
     //GET
     public HashMap<String, ArrayList<ReviewTuple>> getmPathReviewDict()
     {
         return mPathReviewDict;
     }

     public HashMap<String, ArrayList<String>> getmBugReviewrInfo()
     {
         return mBugReviewrInfo;
     }

     public HashMap<String, ArrayList<String>> getmBugFileNameDict()
     {
         return mBugFileNameDict;
     }

     //Adds bug id and commit author names for each bug id to HasMap "mBugReviewrInfo"  for using later in Utility Package
     private void AddElementtomBugReviewrInfo(String bugid,String name)
     {

         if (!mBugReviewrInfo.containsKey(bugid))
         {
             reviewerNames=new ArrayList<String>();

         }
         else if (mBugReviewrInfo.containsKey(bugid))

         {
             reviewerNames= mBugReviewrInfo.get(bugid);

         }
         reviewerNames.add(name);
         mBugReviewrInfo.put(bugid,reviewerNames);

     }



     public void addElementtomBugFileName(String bugid,String fileName)
     {
         if(!mBugFileNameDict.containsKey(bugid))
         {
              fileNames=new ArrayList<String>() ;
         }


         else if(mBugFileNameDict.containsKey(bugid))
         {
             fileNames=mBugFileNameDict.get(bugid);
         }

         if(!fileNames.contains(fileName))
         {
             fileNames.add(fileName);
         }


         mBugFileNameDict.put(bugid,fileNames);
     }





 }