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


 /*
  * @author: Sara Bahrami <mxbahramizanjani@wichita.edu>
  * Date Created: 11/2013
  *
  * Purpose:
  * 	Allows the storage of authors/attachers and their xfactor together
  * so that they can be sorted based on xfactor.
  */
 public class Developer implements Comparable<Developer>
 {
     //MEMBER VARIABLES
     String mName;
     Double mXfactor;

     //CONSTRUCTORS
     public Developer(String name)
     {
         mName = name;
     }
     public Developer(String name, Double xfactor)
     {
         mName = name;
         mXfactor = xfactor;
     }

     //INHERITED METHODS
     @Override
     public int compareTo(Developer arg)
     {
         //this is backward in order to use Collections.sort and get descending order
         if (this.mXfactor > arg.mXfactor)
             return -1;
         else if (this.mXfactor < arg.mXfactor)
             return 1;
         else
             return 0;
     }

 }
