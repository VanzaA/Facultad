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
package examples.content.musicShopOntology;

import examples.content.ecommerceOntology.ECommerceVocabulary;

/**
 * Vocabulary containing constants used by the MusicShopOntology.
 * @author Giovanni Caire - TILAB
 */
public interface MusicShopVocabulary extends ECommerceVocabulary {
  public static final String CD = "CD";
  public static final String CD_TITLE = "title";
  public static final String CD_TRACKS = "tracks";

  public static final String TRACK = "TRACK";
  public static final String TRACK_NAME = "name";
  public static final String TRACK_DURATION = "duration";
  public static final String TRACK_PCM = "pcm";
  
  public static final String SINGLE = "SINGLE";
}
