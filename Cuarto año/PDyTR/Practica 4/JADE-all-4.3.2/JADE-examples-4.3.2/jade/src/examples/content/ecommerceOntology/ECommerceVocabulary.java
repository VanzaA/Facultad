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
package examples.content.ecommerceOntology;

/**
 * Vocabulary containing constants used by the ECommerceOntology.
 * @author Giovanni Caire - TILAB
 */
public interface ECommerceVocabulary {
  public static final String ITEM = "ITEM";
  public static final String ITEM_SERIALID = "serialID";
  
  public static final String PRICE = "PRICE";
  public static final String PRICE_VALUE = "value";
  public static final String PRICE_CURRENCY = "currency";
  
  public static final String CREDIT_CARD = "CREDITCARD";
  public static final String CREDIT_CARD_TYPE = "type";
  public static final String CREDIT_CARD_NUMBER = "number";
  public static final String CREDIT_CARD_EXPIRATION_DATE = "expirationdate";
  
  public static final String OWNS = "OWNS";
  public static final String OWNS_OWNER = "Owner";
  public static final String OWNS_ITEM = "item";
  
  public static final String SELL = "SELL";
  public static final String SELL_BUYER = "buyer";
  public static final String SELL_ITEM = "item";
  public static final String SELL_CREDIT_CARD = "creditcard";
  
  public static final String COSTS = "COSTS";
  public static final String COSTS_ITEM = "item";
  public static final String COSTS_PRICE = "price";
}  
