/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop
multi-agent systems in compliance with the FIPA specifications.
Copyright (C) 2002 TILAB S.p.A.

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

package jade.security;


/**
	The <code>JADEPrincipal</code> interface represents all principals
	acting on the platform. They will be held responsible for their
	own agents and containers.

	@author Michele Tomaiuolo - Universita` di Parma
	@author Giosue Vitaglione - Telecom Italia LAB

	@version $Date: 2004-05-10 17:36:04 +0200 (lun, 10 mag 2004) $ $Revision: 5069 $
*/
public interface JADEPrincipal
//#ALL_EXCLUDE_BEGIN
		extends java.security.Principal
//#ALL_EXCLUDE_END
{
  
	/**
   * The name which marks an unidentified principal.
   */
	public static final String NONE = "none";
  
	/**
   * Returns the name of this principal as known to its platform.
   * @return The name.
   */
	public String getName();

	/**
   * Returns the SDSI name of this principal (if any)
   * @return The SDSIName
   */
	public  SDSIName getSDSIName();
  
  
	/**
   *	Checks the hierarchical relationship between two principals, i.e.
   *tells if this principal belongs to group <code>p</code>.
   *If this holds, all permissions granted to <code>p</code> will be
   *automatically granted to this principal, too.
   *@param p The principal to check.
   *@return True if this principal is a member of group <code>p</code>.
   */
  //public boolean implies(JADEPrincipal p);
  
  public byte[] getEncoded();
  public String toString();
  public int hashCode ();
  public boolean equals(Object p);
}
