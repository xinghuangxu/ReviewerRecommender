
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.json.JSONException;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
public class WritingJsontoXML {
	
	 //MEMBERS
	 public static HashMap<Integer, String> fileVector= new HashMap<Integer, String>();
	 public static ArrayList<ArrayList<String>> reviewMessageInfo=new ArrayList<ArrayList<String>>();
	 public static HashMap<String, String> AssigneeMap=new HashMap<String, String>();
	 public static String owner;
	 public static String subject;
	 public static String status;
	 public static String number;
	// public static Boolean more_changes;
	// public static String sortkey;
	 public static String revisionid;
	 public static Document docnew;
	 public static String patchsize;
	 public static String NumberofPatches;
	 public static String CreatedDate;
	 public static String UpdatedDate;
	 public static String Project;

	//CONSTRUCTOR
	 public WritingJsontoXML(String url, Document doc) throws JSONException
	{
	    
		 JsonArrayParser jsonobj=new JsonArrayParser(url);
	    owner=jsonobj.getOwner();
	    subject=jsonobj.getSubject();
	   // System.out.println(subject);
	    status=jsonobj.getStatus();
	    number=jsonobj.getNumber();
	   // more_changes=jsonobj.getMoreChange();
	   // sortkey=jsonobj.getSortkey();
		fileVector=jsonobj.reviewPatchFiles();
		reviewMessageInfo=jsonobj.reviewMessageExtractor();
		revisionid=jsonobj.getCurrentRevisionkey();
		patchsize=jsonobj.getpatchsize();
		CreatedDate=jsonobj.getDateCreated();
		UpdatedDate=jsonobj.getDateUpdated();
		AssigneeMap=jsonobj.ReviewerAssignee();
		NumberofPatches=jsonobj.getNumberofPatches();
		Project=jsonobj.getproject();
		docnew=doc;
	}
	 
	 //For each review creates related node in xml file
	 public Element reviewJSONtoXML() throws ParserConfigurationException, TransformerException
	 {
		 	//System.out.println(subject);
		 	//just those reviews which have a traceable bug id in review's subject
		 	String BugId=regexBugId(subject);
		 	//System.out.println(BugId);
		 	if ((BugId.length()>0)&&(fileVector.size()>0)) 
		 	{
				//creating BugId Element
				Element bug = createElementAttrib("BugId", "Id", BugId, "Status",status, docnew);
				//rootElement.appendChild(bug);
				//create Elements Owner and Subject
				bug.appendChild(createElementAttrib("Owner", "name", owner, "Date",CreatedDate, docnew));
				bug.appendChild(createElementAttrib("RevisionId","Id",revisionid,"UpdatedDate",UpdatedDate,docnew));
				bug.appendChild(createElementAttrib("PatchSize","NumberofPatches",NumberofPatches,"Size",patchsize,docnew));
				bug.appendChild(createElementAttrib("Subject", "text", subject,"number", number, docnew));
				bug.appendChild(createElementAttrib("Project", "Product", Project,"","", docnew));
				// Create Element files which includes all of the files in patch set
				Element files = docnew.createElement("Files");
				for (int j = 1; j <= fileVector.size(); j++) {
					Element file = createElementAttrib("File", "path",fileVector.get(j), "", "", docnew);
					files.appendChild(file);
				}
				bug.appendChild(files);
				//Create reviewer element + name and date attribute.
				Element reviewers = docnew.createElement("Reviewers");
				for (int k = 0; k < reviewMessageInfo.size(); k++) {

					//removing "Hudson CI" from reviewers which is a patch builder
					if (!(reviewMessageInfo.get(k).get(1).contains("Hudson CI")))
					{
						Element info = createElementAttrib("Info", "name",reviewMessageInfo.get(k).get(1), "date",reviewMessageInfo.get(k).get(2), docnew);
						reviewers.appendChild(info);
					}

				}
				bug.appendChild(reviewers);
				//Create Reviewer Assignee: is a list of reviewers have been invited to review the patch.
				Element Assignee=docnew.createElement("Assignee");
				for(Entry<String,String> entry:AssigneeMap.entrySet())
				{
					Element info=createElementAttrib("AssigneeInfo", "name",entry.getKey(), "date",entry.getValue(), docnew);
					Assignee.appendChild(info);
				}
				
			bug.appendChild(Assignee);
				
				return bug;
			}
		 	else
		 	{
		 		return null;
		 	}

		 
	 }
	 
	 
		//Creates an element with at most two different attributes
		public static Element createElementAttrib(String elementName, String attribName1,String attribValue1,String attribName2,String attribValue2,Document docnew) throws ParserConfigurationException
		{
			Element elm = docnew.createElement(elementName);
			Attr Id = docnew.createAttribute(attribName1);
			Id.setValue(attribValue1);
			elm.setAttributeNode(Id);
			if((attribName2.length()>0) &&(attribValue2.length()>0))
			{
				Attr Idd = docnew.createAttribute(attribName2);
				Idd.setValue(attribValue2);
				elm.setAttributeNode(Idd);
				
			}
			
			return elm;
		}
		
		//Extracting bugid from review subject
		public static String regexBugId(String subject)
		{
			String x="00000";
			Pattern pattern =Pattern.compile("^([0-9]+)");
			Matcher matcher=pattern.matcher(subject);
			if(matcher.find())
			{
				x=matcher.group(1);
			}

			else
			{
				Pattern pattern1 =Pattern.compile("Bug ([0-9]+)");
				Matcher matcher1=pattern1.matcher(subject);
				if(matcher1.find())
				{
					x=matcher1.group(1);
				}
				else
				{
					Pattern pattern2 =Pattern.compile("bug ([0-9]+)");
					Matcher matcher2=pattern2.matcher(subject);
					if(matcher2.find())
					{
						x=matcher2.group(1);
					}
					else
					{
						Pattern pattern3 =Pattern.compile("Bug#([0-9]+)");
						Matcher matcher3=pattern3.matcher(subject);
						if(matcher3.find())
						{
							x=matcher3.group(1);
						}
						else
						{
							Pattern pattern4 =Pattern.compile("\\[([0-9]+)");
							Matcher matcher4=pattern4.matcher(subject);
							if(matcher4.find())
							{
								x=matcher4.group(1);
							}
							
							else
							{
								Pattern pattern5 =Pattern.compile("([0-9]{5,6})");
								Matcher matcher5=pattern5.matcher(subject);
								if(matcher5.find())
								{
									x=matcher5.group(1);
								}
							}
							
						}
					}
					
				}
				
			}
			return x;
		}
	
}
