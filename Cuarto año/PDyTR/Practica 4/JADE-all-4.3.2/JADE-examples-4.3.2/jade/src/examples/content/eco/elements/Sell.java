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
package examples.content.eco.elements;

import jade.content.AgentAction;
import jade.core.AID;

public class Sell implements AgentAction {
	private static final long serialVersionUID = 1L;

	private AID buyer;
	private Item item;
	private CreditCard creditCard;

	public Sell() {
	}

	public Sell(AID buyer, Item item, CreditCard cc) {
		setBuyer(buyer);
		setItem(item);
		setCreditCard(cc);
	}

	public AID getBuyer() {
		return buyer;
	}

	public void setBuyer(AID id) {
		buyer = id;
	}

	public Item getItem() {
		return item;
	}

	public void setItem(Item i) {
		item = i;
	}

	public CreditCard getCreditCard() {
		return creditCard;
	}

	public void setCreditCard(CreditCard creditCard) {
		this.creditCard = creditCard;
	}
}
