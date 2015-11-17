package com.wichita.edu.recommender;
 /* 
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

 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.text.ParseException;
 import java.util.*;
 import java.util.Map.Entry;
 import java.util.concurrent.TimeUnit;

 /*
  * Author: Sara Bahrami
  * (The original version was for interaction project, newer version works for code review)
  * Latest change: changing Integer BugId to String BugId for ReviewTuple , so BugId can be replaced by Review Id for code review history and
  * similarly can be replaced by Change Id for commit history.
  *
  * Purpose:
  * 	Does the main work of this program. Finds the reviewers who reviewed a
  *  file that contained the desired file/path, then it finds the
  * xfactors for each reviewer, and then prints out the list of reviewers
  * with their xfactors in descending order based on their xfactor.
  */
 public class XFinder
 {
     //CONSTANTS
     //this needs to be changed to two different directories. Directory which houses the Review Log and Directory which houses
     // the Commit log. If we set the path to directory of Review Log, XFinder calculates the xfactor score based on review history
     // and if we set the path to directory of Commit log the XFinder calculates the xfactor score based on commit history.
     final private String interactionDictXMLDir = "CodeReviewLog.xml";
     //  final private String interactionDictXMLDir = "/home/sara/research/codereview/MylynNew/PatchReviewRecommendation/commitlogMylyn.xml";
     //MEMBER VARIABLES
     HashMap<String, ArrayList<ReviewTuple>> mPathReviewDict = new HashMap<String, ArrayList<ReviewTuple>>();
     String mOutputDir = new String();

     //CONSTRUCTORS
     /*
      * Creates an object which is used to find the xfactor
      */
     public XFinder (String outputDir)
     {
         mOutputDir = outputDir;
     }


     /*
      * Matches a path with all its matches in the dictionary of all of the paths.
      * The information vectors associated with these paths are then combined to
      * create a vector containing all the information about the provided path. This
      * information is then used to also create a dictionary of vectors containing
      * all of the information concerning the individual authors who interacted with
      * this path.
      */
     public List<Object> __formCodeVectorMap (String path, Date CreationDate)
     {
         //Declarations
         HashMap<String, ArrayList<ReviewTuple>> possibleMatchPaths = new HashMap<String, ArrayList<ReviewTuple>>();
         ArrayList<ReviewTuple> infoVectors;
         //ArrayList<Integer> interactionList;
         ArrayList<String> interactionList;
         ArrayList<String> authorList;
         ArrayList<Date> dayList;
         ReviewTupleList codeVectorMap;
         HashMap<String, ReviewTupleList> authorCodeMap;

         try
         {

             try
             {
                 //search through HashMap of mPathReviewDict<path, info> for the path that matches with the query path and filter all the info tuples
                 // which their date is after the creation date of the input review id.
                 for (Entry<String, ArrayList<ReviewTuple>> entry : mPathReviewDict.entrySet())
                     if(path.endsWith(entry.getKey()))
                     {
                         ArrayList<ReviewTuple> reviews=new ArrayList<ReviewTuple>();

                         for (int k=0;k<entry.getValue().size();k++)
                         {
                             if(entry.getValue().get(k).mDate.before(CreationDate))
                             {
                                 reviews.add(entry.getValue().get(k));


                             }
                         }
                         possibleMatchPaths.put(entry.getKey(), reviews);
                     }
             }


             catch(Exception e)
             {
                 System.err.println("Searching for path in mPathReviewDict:: " + e.getMessage());
             }

             //create a list of all tuples that match the path
             infoVectors = new ArrayList<ReviewTuple>();
             for (List<ReviewTuple> eachMatch : possibleMatchPaths.values())
             {
                 for (ReviewTuple tuple : eachMatch)
                 {
                     infoVectors.add(tuple);
                 }
             }

             //create codeVectorMap - contains all info about this path
             //interactionList = new ArrayList<Integer>(); //list of bugIDs that contain this path
             interactionList = new ArrayList<String>(); //list of bugIDs that contain this path
             authorList = new ArrayList<String>(); //list of all the attachers of this path
             dayList = new ArrayList<Date>(); //list of dates this path was attached/interacted
             for (ReviewTuple tuple : infoVectors)
             {
                 interactionList.add(tuple.mBugID);
                 if (!authorList.contains(tuple.mReviewer)) //prevents duplicates
                     authorList.add(tuple.mReviewer);
                 if (!dayList.contains(formatDate(tuple.mDate))) //prevents duplicates
                     dayList.add(formatDate(tuple.mDate));
             }
             codeVectorMap = new ReviewTupleList(interactionList, authorList, dayList);

             //create authorCodeMap - contains all authors for this path and their associated info
             authorCodeMap = new HashMap<String, ReviewTupleList>();
             for (String author : authorList)
             {
                 //interactionList = new ArrayList<Integer>(); //list of bugIDs containing path attached by author
                 interactionList = new ArrayList<String>(); //list of bugIDs containing path attached by author
                 dayList = new ArrayList<Date>(); //list of dates path was attached by author
                 for (ReviewTuple tuple : infoVectors)
                     if (tuple.mReviewer.equals(author))
                     {
                         interactionList.add(tuple.mBugID);
                         if (!dayList.contains(formatDate(tuple.mDate))) //prevents duplicates
                             dayList.add(formatDate(tuple.mDate));
                     }
                 authorCodeMap.put(author, new ReviewTupleList(interactionList, null, dayList));
             }

             return Arrays.asList(codeVectorMap, authorCodeMap);
         }
         catch (Exception e)
         {
             System.err.println("Main portion of __formCodeVectorMap():: " + e.getMessage());
             return null;
         }
     }

     /*
      * Computes the xfactor for each author for each path.
      * The xfactor is composed of three factors each with a weight of
      * 1.0, so it has a maximum of 3.0. The factors are:
      *  - the interactionFactor: this is the number of interactions which
      * 		include the desired file/path that the developer attached
      * 		divided by the total number of interactions which include
      * 		the file/path.
      *  - the dayFactor: this is the number of unique dates that the
      * 		developer interacted with the file/path divided by the
      * 		total number of datese the file/path was interacted with.
      *  - the mostRecentInteractionFactor: this takes into consideration
      * 		which developer interacted with the desired file/path the
      * 		most recently. This factor is found by dividing 1 by the
      * 		difference between the most recent date the developer
      * 		interacted with the file/path and the most recent date that
      * 		anyone interacted with the file/path.
      */
     public ArrayList<Developer> __computeXfactor (ReviewTupleList codeVectorMap, HashMap<String, ReviewTupleList> authorCodeMap)
     {
         //Declarations
         ArrayList<Developer> xfactorMap = new ArrayList<Developer>();
         Integer interactionFactor = 0;
         Integer dayFactor = 0;
         Double mostRecentInteractionFactor = 0.0;
         Double xfactorScore = 0.0;
         Date totalMostRecent;
         Date authorMostRecent;

         for (Entry<String, ReviewTupleList> entry : authorCodeMap.entrySet())
         {
             interactionFactor = entry.getValue().mBugIDList.size();
             xfactorScore = (double) interactionFactor / codeVectorMap.mBugIDList.size();

             dayFactor = entry.getValue().mDateList.size();
             xfactorScore += (double) dayFactor / codeVectorMap.mDateList.size();

             //mostRecentInteractionFactor
             totalMostRecent = codeVectorMap.mDateList.get(0);
             for (Date day : codeVectorMap.mDateList)
                 if (day.after(totalMostRecent))
                     totalMostRecent = day;
             authorMostRecent = entry.getValue().mDateList.get(0);
             for (Date day : entry.getValue().mDateList)
                 if (day.after(authorMostRecent))
                     authorMostRecent = day;
             mostRecentInteractionFactor = (double) TimeUnit.MILLISECONDS.toDays(Math.abs(totalMostRecent.getTime() - authorMostRecent.getTime()));
             if (mostRecentInteractionFactor != 0)
                 xfactorScore += (1.0 / mostRecentInteractionFactor);
             else
                 xfactorScore += 1.0;


             //add author's xfactor to xfactorMap
             xfactorMap.add(new Developer(entry.getKey(), xfactorScore));

             //reset factors for next author
             interactionFactor = 0;
             dayFactor = 0;
             mostRecentInteractionFactor = 0.0;
             xfactorScore = 0.0;
         }

         return xfactorMap;
     }

     /*
      * Main method used to find recommendations. Computes the xfactor
      * for each developer that has attached/interacted with each path
      * for each level of the path, sorts, and prints them to a file.
      */

     public void run (List<String> queriedPathList, String outputFile, Date CreationDate, com.wichita.edu.recommender.Dictionary Dict) throws IOException
     {
         //Declarations
         PrintWriter writer;
         List<Object> infoVectors;
         ArrayList<Developer> developerRecs;

         //for training based on code review history
 //		Dictionary Dict = new Dictionary(interactionDictXMLDir);
         //for training based on commit history
         //DictionaryGitLog Dict = new DictionaryGitLog(interactionDictXMLDir);

         mPathReviewDict = Dict.getmPathReviewDict();

         try
         {
             writer = new PrintWriter(new FileOutputStream(this.mOutputDir + outputFile, false));

             //find recommendation for each path from queriedPathList
             for (String queriedPath : queriedPathList)
             {
                     writer.print(queriedPath + "\t");
                     try
                     {
                         //form a code-vector and a developer-vector for this path
                         infoVectors = this.__formCodeVectorMap(queriedPath,CreationDate);
                         //compute the xfactor the vectors
                         developerRecs = this.__computeXfactor((ReviewTupleList) infoVectors.get(0), (HashMap<String, ReviewTupleList>) infoVectors.get(1));
                         //sort the recommendations by their xfactor score.
                         Collections.sort(developerRecs);
                         //write the recommendations to the output file
                         for(Developer record : developerRecs)
                             writer.print(record.mName + ":" + record.mXfactor + "\t" );
                     }
                     catch (Exception e)
                     {
                         System.out.print("Expert not found for path = " + queriedPath);
                     }
                     writer.println();
                 writer.println();
             }
             writer.close();
         }
         catch(Exception e)
         {
             System.err.println("Writing authors and xfactors to file:: " + e.getMessage());
             e.printStackTrace();
         }
     }

     //removing time from date for calculating dayFactor
     public Date formatDate(Date date1) throws ParseException
     {

         Date date = new Date();
         Calendar cal = Calendar.getInstance();
         cal.setTime(date1);
         cal.set(Calendar.HOUR_OF_DAY, 0);
         cal.set(Calendar.MINUTE, 0);
         cal.set(Calendar.SECOND, 0);
         cal.set(Calendar.MILLISECOND, 0);
         date = cal.getTime();
         return date;

     }

 }
