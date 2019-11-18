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
import java.util.StringTokenizer;
import javax.swing.JComponent;

/**
 *  A simple text style class. It can specify the color, italic flag, and bold
 *  flag of a run of text. The original file is written by Slava Pestov
 *  (www.gjt.org) and altered to fit ACL/SL.
 *
 * @author     Chris van Aart - Acklin B.V., the Netherlands & Slava Pestov
 * @created    June 14, 2002
 */

public class ACLSytntaxStyle {
  public ACLSytntaxStyle(Color color, boolean italics, boolean bold, JComponent theComp) {
    this.color = color;
    this.italics = italics;
    this.bold = bold;
    this.theComp = theComp;
  }


  /**
   *  Returns the color specified in this style.
   *
   * @return    The Color value
   */
  public Color getColor() {
    return color;
  }


  /**
   *  Returns true if italics is enabled for this style.
   *
   * @return    The Italics value
   */
  public boolean isItalics() {
    return italics;
  }


  /**
   *  Returns true if boldface is enabled for this style.
   *
   * @return    The Bold value
   */
  public boolean isBold() {
    return bold;
  }


  /**
   *  Returns the specified font, but with the style's bold and italic flags
   *  applied.
   *
   * @param  font  Description of Parameter
   * @return       The StyledFont value
   */
  public Font getStyledFont(Font font) {
    if (font == null) {
      throw new NullPointerException("font param must not"
         + " be null");
    }
    if (font.equals(lastFont)) {
      return lastStyledFont;
    }
    lastFont = font;
    lastStyledFont = new Font(font.getFamily(),
      (bold ? Font.BOLD : 0)
       | (italics ? Font.ITALIC : 0),
      font.getSize());
    return lastStyledFont;
  }


  /**
   *  Returns the font metrics for the styled font.
   *
   * @param  font  Description of Parameter
   * @return       The FontMetrics value
   */
  public FontMetrics getFontMetrics(Font font) {
    if (font == null) {
      throw new NullPointerException("font param must not"
         + " be null");
    }
    if (font.equals(lastFont) && fontMetrics != null) {
      return fontMetrics;
    }
    lastFont = font;
    lastStyledFont = new Font(font.getFamily(),
      (bold ? Font.BOLD : 0)
       | (italics ? Font.ITALIC : 0),
      font.getSize());
    fontMetrics = theComp.getFontMetrics(font);
    return fontMetrics;
  }


  /**
   *  Sets the foreground color and font of the specified graphics context to
   *  that specified in this style.
   *
   * @param  gfx   The graphics context
   * @param  font  The font to add the styles to
   */
  public void setGraphicsFlags(Graphics gfx, Font font) {
    Font _font = getStyledFont(font);
    gfx.setFont(_font);
    gfx.setColor(color);
  }


  /**
   *  Returns a string representation of this object.
   *
   * @return    Description of the Returned Value
   */
  public String toString() {
    return getClass().getName() + "[color=" + color +
      (italics ? ",italics" : "") +
      (bold ? ",bold" : "") + "]";
  }


  /**
   *  Creates a new SyntaxStyle.
   *
   * @param  color    The text color
   * @param  italics  True if the text should be italics
   * @param  bold     True if the text should be bold
   */

  JComponent theComp;

  // private members
  private Color color;
  private boolean italics;
  private boolean bold;
  private Font lastFont;
  private Font lastStyledFont;
  private FontMetrics fontMetrics;
}
//  ***EOF***
