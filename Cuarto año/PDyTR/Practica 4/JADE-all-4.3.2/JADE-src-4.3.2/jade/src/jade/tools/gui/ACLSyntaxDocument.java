/******************************************************************
 * JADE - Java Agent DEvelopment Framework is a framework to develop
 * multi-agent systems in compliance with the FIPA specifications.
 * Copyright (C) 2002 TILAB S.p.A.
 *
 * This file is donated by Acklin B.V. to the JADE project.
 *
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
 * ***************************************************************/
package jade.tools.gui;

import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.text.Document;

/**
 *  The interface a document must implement to be colorizable by the jEdit
 *  text area component. The original file is written by Slava Pestov
 *  (www.gjt.org) and altered to fit ACL/SL.
 *
 * @author     Chris van Aart - Acklin B.V., the Netherlands & Slava Pestov
 * @created    June 14, 2002
 */
public class ACLSyntaxDocument extends PlainDocument {
  /**
   *  Returns the token marker that is to be used to split lines of this
   *  document up into tokens. May return null if this document is not to be
   *  colorized.
   *
   * @return    The TokenMarker value
   */
  public ACLSLTokenMarker getTokenMarker() {
    return tokenMarker;
  }


  /**
   *  Sets the token marker that is to be used to split lines of this document
   *  up into tokens. May throw an exception if this is not supported for this
   *  type of document.
   *
   * @param  tm  The new token marker
   */
  public void setTokenMarker(ACLSLTokenMarker tm) {
    tokenMarker = tm;
    if (tm == null) {
      return;
    }
    tokenMarker.insertLines(0, getDefaultRootElement()
      .getElementCount());
    tokenizeLines();
  }


  /**
   *  Reparses the document, by passing all lines to the token marker. This
   *  should be called after the document is first loaded.
   */
  public void tokenizeLines() {
    tokenizeLines(0, getDefaultRootElement().getElementCount());
  }


  /**
   *  Reparses the document, by passing the specified lines to the token
   *  marker. This should be called after a large quantity of text is first
   *  inserted.
   *
   * @param  start  The first line to parse
   * @param  len    The number of lines, after the first one to parse
   */
  public void tokenizeLines(int start, int len) {
    if (tokenMarker == null || !tokenMarker.supportsMultilineTokens()) {
      return;
    }

    Segment lineSegment = new Segment();
    Element map = getDefaultRootElement();

    len += start;

    try {
      for (int i = start; i < len; i++) {
        Element lineElement = map.getElement(i);
        int lineStart = lineElement.getStartOffset();
        getText(lineStart, lineElement.getEndOffset()
           - lineStart - 1, lineSegment);
        tokenMarker.markTokens(lineSegment, i);
      }
    }
    catch (BadLocationException bl) {
    }
  }


  /**
   *  We overwrite this method to update the token marker state immediately so
   *  that any event listeners get a consistent token marker.
   *
   * @param  evt  Description of Parameter
   */
  protected void fireInsertUpdate(DocumentEvent evt) {
    if (tokenMarker != null) {
      DocumentEvent.ElementChange ch = evt.getChange(
        getDefaultRootElement());
      if (ch != null) {
        tokenMarker.insertLines(ch.getIndex() + 1,
          ch.getChildrenAdded().length -
          ch.getChildrenRemoved().length);
      }

    }

    super.fireInsertUpdate(evt);
  }


  /**
   *  We overwrite this method to update the token marker state immediately so
   *  that any event listeners get a consistent token marker.
   *
   * @param  evt  Description of Parameter
   */
  protected void fireRemoveUpdate(DocumentEvent evt) {
    if (tokenMarker != null) {
      DocumentEvent.ElementChange ch = evt.getChange(
        getDefaultRootElement());
      if (ch != null) {
        tokenMarker.deleteLines(ch.getIndex() + 1,
          ch.getChildrenRemoved().length -
          ch.getChildrenAdded().length);
      }

    }

    super.fireRemoveUpdate(evt);
  }

  // protected members
  protected ACLSLTokenMarker tokenMarker;

}
//  ***EOF***
