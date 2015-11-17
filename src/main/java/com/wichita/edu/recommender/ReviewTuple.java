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

 import java.util.Date;

 /*
 @author: Sara Bahrami <mxbahramizanjani@wichita.edu>
 purpose: each tuple save 3 different values: (Review id, reviewer and date) in case of code review history
 and (change id, author and date) in case of commit history.

 */
 public class ReviewTuple
 {
     //MEMBER VARIABLES
     //public Integer mBugID = 0;
     public String mBugID ="";
     public String mReviewer = "";
     public Date mDate;

     //Setters
     //public void setmBugID(Integer mBugID)
     public void setmBugID(String mBugID)
     {
         this.mBugID=mBugID;
     }

     public void setmReviewer(String mReviewer)
     {
         this.mReviewer=mReviewer;
     }

     public void setmDate(Date mDate)
     {
         this.mDate=mDate;
     }
 }