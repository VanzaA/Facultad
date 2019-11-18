package jade.gui;

/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop 
multi-agent systems in compliance with the FIPA specifications.
Copyright (C) 2000 CSELT S.p.A. 

GNU Lesser General Public License

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation, 
version 2.1 of the License. 

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the
Free Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA  02111-1307, USA.
*****************************************************************/

//#J2ME_EXCLUDE_FILE

//#DOTNET_EXCLUDE_BEGIN
import javax.swing.JPopupMenu;
//#DOTNET_EXCLUDE_END
/*#DOTNET_INCLUDE_BEGIN
import System.Windows.Forms.MenuItem;
#DOTNET_INCLUDE_END*/

/**
   Javadoc documentation for the file
   @author Francisco Regi, Andrea Soracchi - Universita` di Parma
   @version $Date: 2005-04-15 17:45:02 +0200 (ven, 15 apr 2005) $ $Revision: 5669 $
 */

class NodeDescriptor 
{
  //#DOTNET_EXCLUDE_BEGIN
  JPopupMenu popupMenu;
  //#DOTNET_EXCLUDE_END
  /*#DOTNET_INCLUDE_BEGIN
  MenuItem	popupMenu;
  #DOTNET_INCLUDE_END*/
  String pathImage;

  //#DOTNET_EXCLUDE_BEGIN
  protected NodeDescriptor(JPopupMenu popupMenu,String pathImage){
  //#DOTNET_EXCLUDE_END
  /*#DOTNET_INCLUDE_BEGIN
  protected NodeDescriptor(MenuItem popupMenu,String pathImage){
  #DOTNET_INCLUDE_END*/
   this.popupMenu=popupMenu;
   this.pathImage=pathImage;
  }

  //#DOTNET_EXCLUDE_BEGIN
  protected JPopupMenu getPopupMenu() {
  //#DOTNET_EXCLUDE_END
  /*#DOTNET_INCLUDE_BEGIN
  protected MenuItem getPopupMenu() {
  #DOTNET_INCLUDE_END*/
   return popupMenu;
  }

  protected String getPathImage() {
   return pathImage;
  }
  
  //#DOTNET_EXCLUDE_BEGIN
  protected void setNewPopupMenu(JPopupMenu p){
  //#DOTNET_EXCLUDE_END
  /*#DOTNET_INCLUDE_BEGIN
  protected void setNewPopupMenu(MenuItem p){
  #DOTNET_INCLUDE_END*/
  this.popupMenu =p;
  }
} 
