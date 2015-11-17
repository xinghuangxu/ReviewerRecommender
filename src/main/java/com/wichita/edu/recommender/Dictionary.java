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
  * This class contains SAXParser for parsing the review log and creates different HashMaps
  * mPathReviewDict- key: File name with absolute path, value: an object from class of ReviewTuple (review id-reviewer-date)
  * mPathReviewDict- this HashMap is used by class XFinder for calculating XFactor value for each file
  *
  * mRevReviewrInfo-key: review id, value: all the reviewers related to that review id.
  * mRevReviewrInfo- this hashMap is used by  Accuracy.java for calculating precision and recall.
  *
  * mRevDate-key:review id, value: creation date for that review id
  * mRevDate- this HashMap is used by DevRecommender.java to filter train set based on review's creation date.
  *
  * mRevStatusOwner-key:reviewid, value:an object from class StatusOwner ( owner and status of  that review id)
  * mRevStatusOwner- this HashMap is used by Accuracy.java for calculating precision and recall for both cases: considering
  * owner as one of reviewers and discarding owner
  *
  */
 public class Dictionary extends DefaultHandler{
     //key: File name with absolute path, value: an object from class of ReviewTuple
     HashMap<String, ArrayList<ReviewTuple>> mPathReviewDict;
     //key: bug id, value: all the reviewers related to that bug id
     HashMap<String, ArrayList<String>> mBugReviewrInfo;
     //HashMap<String,ArrayList<StatusOwner>> mBugStatusOwner;
     //key: bug id, value: all of the files in patch set related to that bug id.
     HashMap<String, ArrayList<String>> mBugFileNameDict;
     //key: review id, value: all of the files in patch set related to that review id.
     HashMap<String, ArrayList<String>> mRevFileNameDict;
     //key: review id, value: all the reviewers related to that review id.
     HashMap<String, ArrayList<String>> mRevReviewrInfo;
     //key:review id, value:an object from class StatusOwner ( owner and status of  that review id)
     HashMap<String, StatusOwner> mRevStatusOwner;
     //key:review id, value: creation date for that review id
     HashMap<String,Date> mRevDate;

     ArrayList<ReviewTuple> reviews;
     ArrayList<String> reviewerNames;
     ArrayList<String> fileNames;
     ArrayList<StatusOwner> statuesownerlist;
     //keeps the list of files in each patch set for each review
     ArrayList<String> files;
     String bugid;
     String status;
     String owner;
     String xmlLogFilepath;
     String tmpValue;
     ReviewTuple reviewNode;
     StatusOwner statusOwnerNode;
     String revno;
     Date CreationDate;

     public Dictionary(String xmlLogFilepath)
     {
         this.xmlLogFilepath = xmlLogFilepath;
         this.mPathReviewDict =  new HashMap<String, ArrayList<ReviewTuple>>();
         this.mBugReviewrInfo=new HashMap<String, ArrayList<String>>();
        // this.mBugStatusOwner=new HashMap<String,ArrayList<StatusOwner>>();
         this.mBugFileNameDict=new HashMap<String, ArrayList<String>>();
         this.mRevReviewrInfo=new HashMap<String, ArrayList<String>>();
         this.mRevFileNameDict=new HashMap<String, ArrayList<String>>();
         this.mRevStatusOwner=new HashMap<String, StatusOwner>();
         this.mRevDate=new HashMap<String, Date>();
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
         if (element.equalsIgnoreCase("BugId"))
         {
             files=new ArrayList<String>();
             bugid=attributes.getValue("Id");
             status=attributes.getValue("Status");
         }

         if(element.equalsIgnoreCase("Owner"))
         {
             try {
                 CreationDate=formatDate(attributes.getValue("Date"));
             } catch (ParseException e) {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             }
             owner=attributes.getValue("name");
             //AddElementtomBugStatusOwner(bugid,status,owner);

         }



         if(element.equalsIgnoreCase("Subject"))
         {
             revno=attributes.getValue("number");
             mRevDate.put(revno,CreationDate);
             AddElementtomRevStatusOwner(revno,status,owner);
         }



         if (element.equalsIgnoreCase("File"))
         {
             files.add(attributes.getValue("path"));
             addElementtomBugFileName(bugid,attributes.getValue("path"));
             addElementtomRevFileName(revno,attributes.getValue("path"));
         }

         if (element.equalsIgnoreCase("Info"))
         {
             reviewNode = new ReviewTuple();
             //reviewNode.setmBugID(Integer.parseInt(bugid));
             //reviewNode.setmBugID(Integer.parseInt(revno));
             //Get the Review ID instead of the Bug ID. I changed this because for Microsoft project we don't have Bug Id
             // related to review for all the reviews.
             reviewNode.setmBugID(revno);
             try {
                 reviewNode.setmDate(formatDate(attributes.getValue("date")));
             } catch (ParseException e) {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             }
             reviewNode.setmReviewer(attributes.getValue("name"));
             AddElementtomBugReviewrInfo(bugid,attributes.getValue("name"));
             AddElementtomRevReviewrInfo(revno,attributes.getValue("name"));
         }



     }
     //Related function for SAX Parser
     public void endElement(String s, String s1, String element) throws SAXException
     {
         if(element.equals("BugId"))
         {

             files.clear();
         }

         //if element is Info, add the reviewer names and dates to dictionary "mPathReviewDict"
         if(element.equalsIgnoreCase("Info"))
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
         String pattern="yyyy-MM-dd HH:mm:ss";
         //String pattern="yyyy-MM-dd";
          SimpleDateFormat format=new SimpleDateFormat(pattern);
          Date daterightformat=format.parse(date);
         return daterightformat;
     }
     //GET
     public HashMap<String,Date> getmRevDate()
     {
         return mRevDate;
     }
     public HashMap<String, ArrayList<ReviewTuple>> getmPathReviewDict()
     {
         return mPathReviewDict;
     }

     public HashMap<String, ArrayList<String>> getmBugReviewrInfo()
     {
         return mBugReviewrInfo;
     }

     public HashMap<String, ArrayList<String>> getmRevReviewrInfo()
     {
         return mRevReviewrInfo;
     }

   /*  public HashMap<String, ArrayList<StatusOwner>> getmBugStatusOwner()
     {
         return mBugStatusOwner;
     }
     */
     public HashMap<String, ArrayList<String>> getmBugFileNameDict()
     {
         return mBugFileNameDict;
     }

     public HashMap<String, ArrayList<String>> getmRevFileNameDict()
     {
         return mRevFileNameDict;
     }

     public HashMap<String,StatusOwner> getmRevStatusOwner()
     {
         return mRevStatusOwner;
     }

     //Adds bug id and reviewers for each bug id to HashMap for using later in Utility Package
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

     //Adds review number and reviewers for each review number to HashMap mRevReviewrInfo for using later in Utility Package
     private void AddElementtomRevReviewrInfo(String revno,String name)
     {

         if (!mRevReviewrInfo.containsKey(revno))
         {
             reviewerNames=new ArrayList<String>();

         }
         else if (mRevReviewrInfo.containsKey(revno))

         {
             reviewerNames= mRevReviewrInfo.get(revno);

         }
         reviewerNames.add(name);
         mRevReviewrInfo.put(revno,reviewerNames);
     }


   //Adds bugid and status for each bugid to Doctionary mBugStatus for using later in Utility Package
   /* private void AddElementtomBugStatusOwner(String bugid,String status,String owner)
     {


         if (!mBugStatusOwner.containsKey(bugid))
         {
             statuesownerlist=new ArrayList<StatusOwner>();

         }
         else if (mBugStatusOwner.containsKey(bugid))

         {
             statuesownerlist= mBugStatusOwner.get(bugid);

         }

         statusOwnerNode=new StatusOwner(status,owner);
         //System.out.println(bugid);
         statuesownerlist.add(statusOwnerNode);
         mBugStatusOwner.put(bugid,statuesownerlist);

     }
     */

     //for each review id adds status and owner of that review id to HashMap
     private void AddElementtomRevStatusOwner(String revno,String status,String owner)
     {

         StatusOwner Node=new StatusOwner(status,owner);
          mRevStatusOwner.put(revno, Node);
     }


     //Adds bug id and filenames for each bug id to HashMap mBugFileNameDict for using later in Utility Package
    private void addElementtomBugFileName(String bugid,String fileName)
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

   //Adds review id and filenames for each review id to HashMap mRevFileNameDict for using later in Utility Package
     private void addElementtomRevFileName(String revno,String fileName)
     {
         if(!mRevFileNameDict.containsKey(revno))
         {
              fileNames=new ArrayList<String>() ;
         }


         else if(mRevFileNameDict.containsKey(revno))
         {
             fileNames=mRevFileNameDict.get(revno);
         }

         if(!fileNames.contains(fileName))
         {
             fileNames.add(fileName);
         }


         mRevFileNameDict.put(revno,fileNames);
     }


 }