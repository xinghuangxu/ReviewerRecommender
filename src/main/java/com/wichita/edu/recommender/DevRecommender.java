package com.wichita.edu.recommender;/*
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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Scanner;

/*
 @author: Sara Bahrami <mxbahramizanjani@wichita.edu>
 * Purpose: Running XFinder.run from XFinder.java for all the reviews in the benchmark and recommending the ranked reviewers for each review id
 * output: List of ranked relevant reviewers sorted based on xfactor score for each file submitted for each review id
 * 
 */
public class DevRecommender {
    //CONSTANTS
    //directory which includes the reviews in the benchmark
    final static private String INDir = "./input/";
    //directory of output
    final static private String OUTDir = "./output/";
    //directory of review log file
    final static private String ReviewLogDir = "CodeReviewLog.xml";
    public static HashMap<String, Date> mRevDateDict;

    public static void main(String[] args) throws IOException {
        //Creating HashMap "mRevDateDic" from review log which saves review id and creation date for each review in review log
        Dictionary Dict = new Dictionary(ReviewLogDir);
        mRevDateDict = Dict.getmRevDate();

        XFinder xfinder = new XFinder(OUTDir);
        File folder = new File(INDir);
        //Walking through the input directory and passing list of files for each review id to XFinder
        for (final File fileEntry : folder.listFiles()) {
            ArrayList<String> pathList = new ArrayList<String>(1);

            try {
                Scanner fileReader = new Scanner(new FileInputStream(fileEntry));
                while (fileReader.hasNextLine())
                    pathList.add(fileReader.nextLine());
                fileReader.close();

            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
            System.out.println(fileEntry.getName());

            //running xfinder for list of files related to each review id
            //list of all files related to each review id, review id and creation date of review
            xfinder.run(pathList, fileEntry.getName(), ReturnCreationDate(fileEntry.getName()), Dict);
        }
    }

    //Returns the creation date of review Id. This date will be used by XFinder.java to filter the train set
    // all the review logs/commit logs before this date will be used to train the model.
    public static Date ReturnCreationDate(String revno) {
        Date CreationDate = new Date();
        for (Entry<String, Date> entry : mRevDateDict.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(revno)) {
                CreationDate = entry.getValue();
            }
        }

        return CreationDate;
    }

}
