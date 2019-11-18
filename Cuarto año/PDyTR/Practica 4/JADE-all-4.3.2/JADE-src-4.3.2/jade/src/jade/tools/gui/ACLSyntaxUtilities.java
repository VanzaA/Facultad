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

import java.awt.*;
import javax.swing.JComponent;
import javax.swing.text.*;

/**
 *  Class with several utility functions used by jEdit's syntax colorizing
 *  subsystem. The original file is written by Slava Pestov (www.gjt.org) and
 *  altered to fit ACL/SL.
 *
 * @author     Chris van Aart - Acklin B.V., the Netherlands & Slava Pestov
 * @created    June 14, 2002
 */
public class ACLSyntaxUtilities {

  // private members
  private ACLSyntaxUtilities() { }


  /**
   *  Returns the default style table. This can be passed to the <code>setStyles()</code>
   *  method of <code>SyntaxDocument</code> to use the default syntax styles.
   *
   * @param  theComp  Description of Parameter
   * @return          The DefaultSyntaxStyles value
   */
  public static ACLSytntaxStyle[] getDefaultSyntaxStyles(JComponent theComp) {
    ACLSytntaxStyle[] styles = new ACLSytntaxStyle[ACLToken.ID_COUNT];

    styles[ACLToken.COMMENT1] = new ACLSytntaxStyle(Color.black, true, false, theComp);
    styles[ACLToken.COMMENT2] = new ACLSytntaxStyle(new Color(0x990033), true, false, theComp);
    styles[ACLToken.KEYWORD1] = new ACLSytntaxStyle(Color.darkGray, false, false, theComp);
    styles[ACLToken.KEYWORD2] = new ACLSytntaxStyle(new Color(17, 0, 154), false, false, theComp);
    styles[ACLToken.KEYWORD3] = new ACLSytntaxStyle(new Color(0x009600), false, false, theComp);
    styles[ACLToken.LITERAL1] = new ACLSytntaxStyle(new Color(0x650099), false, false, theComp);
    styles[ACLToken.LITERAL2] = new ACLSytntaxStyle(new Color(0x650099), false, false, theComp);
    styles[ACLToken.LABEL] = new ACLSytntaxStyle(new Color(0x990033), false, false, theComp);
    styles[ACLToken.OPERATOR] = new ACLSytntaxStyle(Color.black, false, true, theComp);
    styles[ACLToken.INVALID] = new ACLSytntaxStyle(Color.red, false, false, theComp);

    return styles;
  }


  /**
   *  Checks if a subregion of a <code>Segment</code> is equal to a string.
   *
   * @param  ignoreCase  True if case should be ignored, false otherwise
   * @param  text        The segment
   * @param  offset      The offset into the segment
   * @param  match       The string to match
   * @return             Description of the Returned Value
   */
  public static boolean regionMatches(boolean ignoreCase, Segment text,
                                      int offset, String match) {
    int length = offset + match.length();
    char[] textArray = text.array;
    if (length > text.offset + text.count) {
      return false;
    }
    for (int i = offset, j = 0; i < length; i++, j++) {
      char c1 = textArray[i];
      char c2 = match.charAt(j);
      if (ignoreCase) {
        c1 = Character.toUpperCase(c1);
        c2 = Character.toUpperCase(c2);
      }
      if (c1 != c2) {
        return false;
      }
    }
    return true;
  }


  /**
   *  Checks if a subregion of a <code>Segment</code> is equal to a character
   *  array.
   *
   * @param  ignoreCase  True if case should be ignored, false otherwise
   * @param  text        The segment
   * @param  offset      The offset into the segment
   * @param  match       The character array to match
   * @return             Description of the Returned Value
   */
  public static boolean regionMatches(boolean ignoreCase, Segment text,
                                      int offset, char[] match) {
    int length = offset + match.length;
    char[] textArray = text.array;
    if (length > text.offset + text.count) {
      return false;
    }
    for (int i = offset, j = 0; i < length; i++, j++) {
      char c1 = textArray[i];
      char c2 = match[j];
      if (ignoreCase) {
        c1 = Character.toUpperCase(c1);
        c2 = Character.toUpperCase(c2);
      }
      if (c1 != c2) {
        return false;
      }
    }
    return true;
  }


  /**
   *  Paints the specified line onto the graphics context. Note that this
   *  method munges the offset and count values of the segment.
   *
   * @param  line      The line segment
   * @param  tokens    The token list for the line
   * @param  styles    The syntax style list
   * @param  expander  The tab expander used to determine tab stops. May be
   *      null
   * @param  gfx       The graphics context
   * @param  x         The x co-ordinate
   * @param  y         The y co-ordinate
   * @return           The x co-ordinate, plus the width of the painted string
   */
  public static int paintSyntaxLine(Segment line, ACLToken tokens, ACLSytntaxStyle[] styles, TabExpander expander, Graphics gfx,
                                    int x, int y) {
    Font defaultFont = gfx.getFont();
    Color defaultColor = gfx.getColor();

    int offset = 0;
    for (; ; ) {
      byte id = tokens.id;
      if (id == ACLToken.END) {
        break;
      }

      int length = tokens.length;
      if (id == ACLToken.NULL) {
        if (!defaultColor.equals(gfx.getColor())) {
          gfx.setColor(defaultColor);
        }

        if (!defaultFont.equals(gfx.getFont())) {
          gfx.setFont(defaultFont);
        }

      }
      else {
        styles[id].setGraphicsFlags(gfx, defaultFont);
      }

      line.count = length;
      x = Utilities.drawTabbedText(line, x, y, gfx, expander, 0);
      line.offset += length;
      offset += length;

      tokens = tokens.next;
    }

    return x;
  }
}
//  ***EOF***
