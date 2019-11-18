/**
 * ***************************************************************
 * JADE - Java Agent DEvelopment Framework is a framework to develop
 * multi-agent systems in compliance with the FIPA specifications.
 * Copyright (C) 2000 CSELT S.p.A.
 * 
 * GNU Lesser General Public License
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation,
 * version 2.1 of the License.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 * **************************************************************
 */
package examples.content.mso.elements;

import jade.content.onto.annotations.AggregateSlot;
import jade.util.leap.Iterator;
import jade.util.leap.List;
import examples.content.eco.elements.Item;

public class CD extends Item {
	private static final long serialVersionUID = 1L;

	private String title = null;
	protected List tracks = null;

	public String getTitle() {
		return title;
	}

	public void setTitle(String t) {
		title = t;
	}

	@AggregateSlot(cardMin = 1)
	public List getTracks() {
		return tracks;
	}

	public void setTracks(List l) {
		tracks = l;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer(title);
		if (tracks != null) {
			Iterator it = tracks.iterator();
			int i = 0;
			while (it.hasNext()) {
				sb.append(" ");
				Track t = (Track) it.next();
				sb.append("track-" + i + ": " + t.toString());
				i++;
			}
		}
		return sb.toString();
	}
}
