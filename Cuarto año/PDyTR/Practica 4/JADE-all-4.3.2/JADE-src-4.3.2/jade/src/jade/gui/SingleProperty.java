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


package jade.gui;

//#J2ME_EXCLUDE_FILE

/**
Type using to store key and  value  of a user defined property.
@author Tiziana Trucco - CSELT S.p.A
@version $Date: 2002-12-11 10:39:45 +0100 (mer, 11 dic 2002) $ $Revision: 3515 $
*/

class SingleProperty
 	{
 		String key;
 		String value;
 		
 		/**
 		* Constructor.
 		* @param k the key of the property
 		* @param v the value ot f the property.
 		*/
 		SingleProperty(String k, String v)
 		{
 			key = k;
 			value = v;
 		}
 		
 		/**
 		Returns the key. 
 		*/
 		String getKey()
 		{
 			return this.key;
 		}
 		/**
 		Returns th value.
 		*/
 		String getValue()
 		{
 			return this.value;
 		}
 		/**
 		To set the key.
 		*/
 		void setKey(String k)
 		{
 			this.key = k;
 		}
 		
 		/**
 		To set the value.
 		*/
 		void setValue(String v)
 		{
 			this.value = v;
 		}
 	}