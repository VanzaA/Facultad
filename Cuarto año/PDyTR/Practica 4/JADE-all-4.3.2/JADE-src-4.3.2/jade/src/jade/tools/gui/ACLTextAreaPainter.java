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
 *  The text area repaint manager. It performs double buffering and paints
 *  lines of text.The original file is written by Slava Pestov (www.gjt.org)
 *  and altered to fit ACL/SL.
 *
 * @author     Chris van Aart - Acklin B.V., the Netherlands & Slava Pestov
 * @created    June 14, 2002
 */
public class ACLTextAreaPainter extends JComponent implements TabExpander {
  /**
   *  Creates a new repaint manager. This should be not be called directly.
   *
   * @param  textArea  Description of Parameter
   */
  public ACLTextAreaPainter(ACLTextArea textArea) {
    this.textArea = textArea;

    currentLine = new Segment();
    currentLineIndex = -1;

    firstInvalid = lastInvalid = -1;

    setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));

    setFont(new Font("Dialog", Font.PLAIN, 11));
    setForeground(Color.black);
    setBackground(Color.white);

    styles = ACLSyntaxUtilities.getDefaultSyntaxStyles(this);
    cols = 80;
    rows = 5;
    caretColor = Color.red;
    selectionColor = new Color(0xccccff);
    lineHighlightColor = new Color(0xe0e0e0);
    ;
    lineHighlight = true;
    bracketHighlightColor = Color.black;
    bracketHighlight = true;
    eolMarkerColor = new Color(0x009999);
    eolMarkers = true;

    copyAreaBroken = true;
  }


  /**
   *  Returns if this component can be traversed by pressing the Tab key. This
   *  returns false.
   *
   * @return    The ManagingFocus value
   */
  public final boolean isManagingFocus() {
    return false;
  }


  /**
   *  Returns the syntax styles used to paint colorized text. Entry <i>n</i>
   *  will be used to paint tokens with id = <i>n</i> .
   *
   * @return    The Styles value
   * @see       org.gjt.sp.jedit.syntax.Token
   */
  public final ACLSytntaxStyle[] getStyles() {
    return styles;
  }


  /**
   *  Returns the caret color.
   *
   * @return    The CaretColor value
   */
  public final Color getCaretColor() {
    return caretColor;
  }


  /**
   *  Returns the selection color.
   *
   * @return    The SelectionColor value
   */
  public final Color getSelectionColor() {
    return selectionColor;
  }


  /**
   *  Returns the line highlight color.
   *
   * @return    The LineHighlightColor value
   */
  public final Color getLineHighlightColor() {
    return lineHighlightColor;
  }


  /**
   *  Returns true if line highlight is enabled, false otherwise.
   *
   * @return    The LineHighlightEnabled value
   */
  public final boolean isLineHighlightEnabled() {
    return lineHighlight;
  }


  /**
   *  Returns the bracket highlight color.
   *
   * @return    The BracketHighlightColor value
   */
  public final Color getBracketHighlightColor() {
    return bracketHighlightColor;
  }


  /**
   *  Returns true if bracket highlighting is enabled, false otherwise. When
   *  bracket highlighting is enabled, the bracket matching the one before the
   *  caret (if any) is highlighted.
   *
   * @return    The BracketHighlightEnabled value
   */
  public final boolean isBracketHighlightEnabled() {
    return bracketHighlight;
  }


  /**
   *  Returns true if the caret should be drawn as a block, false otherwise.
   *
   * @return    The BlockCaretEnabled value
   */
  public final boolean isBlockCaretEnabled() {
    return blockCaret;
  }


  /**
   *  Returns the EOL marker color.
   *
   * @return    The EOLMarkerColor value
   */
  public final Color getEOLMarkerColor() {
    return eolMarkerColor;
  }


  /**
   *  Returns true if EOL markers are drawn, false otherwise.
   *
   * @return    The EOLMarkerEnabled value
   */
  public final boolean isEOLMarkerEnabled() {
    return eolMarkers;
  }


  /**
   *  Sets the syntax styles used to paint colorized text. Entry <i>n</i> will
   *  be used to paint tokens with id = <i>n</i> .
   *
   * @param  styles  The syntax styles
   * @see            org.gjt.sp.jedit.syntax.Token
   */
  public final void setStyles(ACLSytntaxStyle[] styles) {
    this.styles = styles;
    invalidateOffscreen();
    repaint();
  }


  /**
   *  Sets the caret color.
   *
   * @param  caretColor  The caret color
   */
  public final void setCaretColor(Color caretColor) {
    this.caretColor = caretColor;
    invalidateSelectedLines();
  }


  /**
   *  Sets the selection color.
   *
   * @param  selectionColor  The selection color
   */
  public final void setSelectionColor(Color selectionColor) {
    this.selectionColor = selectionColor;
    invalidateSelectedLines();
  }


  /**
   *  Sets the line highlight color.
   *
   * @param  lineHighlightColor  The line highlight color
   */
  public final void setLineHighlightColor(Color lineHighlightColor) {
    this.lineHighlightColor = lineHighlightColor;
    invalidateSelectedLines();
  }


  /**
   *  Enables or disables current line highlighting.
   *
   * @param  lineHighlight  True if current line highlight should be enabled,
   *      false otherwise
   */
  public final void setLineHighlightEnabled(boolean lineHighlight) {
    this.lineHighlight = lineHighlight;
    invalidateSelectedLines();
  }


  /**
   *  Sets the bracket highlight color.
   *
   * @param  bracketHighlightColor  The bracket highlight color
   */
  public final void setBracketHighlightColor(Color bracketHighlightColor) {
    this.bracketHighlightColor = bracketHighlightColor;
    invalidateLine(textArea.getBracketLine());
  }


  /**
   *  Enables or disables bracket highlighting. When bracket highlighting is
   *  enabled, the bracket matching the one before the caret (if any) is
   *  highlighted.
   *
   * @param  bracketHighlight  True if bracket highlighting should be enabled,
   *      false otherwise
   */
  public final void setBracketHighlightEnabled(boolean bracketHighlight) {
    this.bracketHighlight = bracketHighlight;
    invalidateLine(textArea.getBracketLine());
  }


  /**
   *  Sets if the caret should be drawn as a block, false otherwise.
   *
   * @param  blockCaret  True if the caret should be drawn as a block, false
   *      otherwise.
   */
  public final void setBlockCaretEnabled(boolean blockCaret) {
    this.blockCaret = blockCaret;
    invalidateSelectedLines();
  }


  /**
   *  Sets the EOL marker color.
   *
   * @param  eolMarkerColor  The EOL marker color
   */
  public final void setEOLMarkerColor(Color eolMarkerColor) {
    this.eolMarkerColor = eolMarkerColor;
    invalidateOffscreen();
    repaint();
  }


  /**
   *  Sets if EOL markers are to be drawn.
   *
   * @param  eolMarkers  True if EOL markers should be dranw, false otherwise
   */
  public final void setEOLMarkerEnabled(boolean eolMarkers) {
    this.eolMarkers = eolMarkers;
    invalidateOffscreen();
    repaint();
  }


  /**
   *  Queues a repaint of the changed lines only.
   */
  public final void fastRepaint() {
    if (firstInvalid == -1 && lastInvalid == -1) {
      repaint();
    }

    else {
      repaint(0, textArea.lineToY(firstInvalid)
         + fm.getLeading() + fm.getMaxDescent(),
        getWidth(), (lastInvalid - firstInvalid + 1)
         * fm.getHeight());
    }

  }


  /**
   *  Repaints the specified line. This is equivalent to calling <code>_invalidateLine()</code>
   *  and <code>repaint()</code>.
   *
   * @param  line  The line
   * @see          #_invalidateLine(int)
   */
  public final void invalidateLine(int line) {
    _invalidateLine(line);
    fastRepaint();
  }


  /**
   *  Repaints the specified line range. This is equivalent to calling <code>_invalidateLineRange()</code>
   *  then <code>repaint()</code>.
   *
   * @param  firstLine  The first line to repaint
   * @param  lastLine   The last line to repaint
   */
  public final void invalidateLineRange(int firstLine, int lastLine) {
    _invalidateLineRange(firstLine, lastLine);
    fastRepaint();
  }


  /**
   *  Repaints the lines containing the selection.
   */
  public final void invalidateSelectedLines() {
    invalidateLineRange(textArea.getSelectionStartLine(),
      textArea.getSelectionEndLine());
  }


  /**
   *  Invalidates the offscreen graphics context. This should not be called
   *  directly.
   */
  public final void invalidateOffscreen() {
    offImg = null;
    offGfx = null;
  }


  /**
   *  Returns the font metrics used by this component.
   *
   * @return    The FontMetrics value
   */
  public FontMetrics getFontMetrics() {
    return fm;
  }


  /**
   *  Returns if the copyArea() should not be used.
   *
   * @return    The CopyAreaBroken value
   */
  public boolean isCopyAreaBroken() {
    return copyAreaBroken;
  }


  /**
   *  Returns the painter's preferred size.
   *
   * @return    The PreferredSize value
   */
  public Dimension getPreferredSize() {
    return super.getPreferredSize();
    /*
        Dimension dim = new Dimension();
        dim.width = fm.charWidth('w') * cols;
        dim.height = fm.getHeight() * rows;
        return dim;
      */
  }


  /**
   *  Returns the painter's minimum size.
   *
   * @return    The MinimumSize value
   */
  public Dimension getMinimumSize() {
    return super.getMinimumSize();
  }


  /**
   *  Sets the font for this component. This is overridden to update the
   *  cached font metrics and to recalculate which lines are visible.
   *
   * @param  font  The font
   */
  public void setFont(Font font) {
    super.setFont(font);
    fm = textArea.getFontMetrics(font);
    textArea.recalculateVisibleLines();
  }


  /**
   *  Disables the use of the copyArea() function (which is broken in JDK
   *  1.2).
   *
   * @param  copyAreaBroken  The new CopyAreaBroken value
   */
  public void setCopyAreaBroken(boolean copyAreaBroken) {
    this.copyAreaBroken = copyAreaBroken;
  }


  /**
   *  Paints any lines that changed since the last paint to the offscreen
   *  graphics, then repaints the offscreen to the specified graphics context.
   *
   * @param  g  The graphics context
   */
  public void update(Graphics g) {
    tabSize = fm.charWidth('w') * ((Integer)textArea.getDocument().getProperty(
      PlainDocument.tabSizeAttribute)).intValue();

    // returns true if offscreen was created. When it's created,
    // all lines, not just the invalid ones, need to be painted.
    if (ensureOffscreenValid()) {
      firstInvalid = textArea.getFirstLine();
      lastInvalid = firstInvalid + textArea.getVisibleLines();
    }

    if (firstInvalid != -1 && lastInvalid != -1) {
      int lineCount;
      try {
        if (firstInvalid == lastInvalid) {
          lineCount = offscreenRepaintLine(firstInvalid,
            textArea.getHorizontalOffset());
        }

        else {
          lineCount = offscreenRepaintLineRange(
            firstInvalid, lastInvalid);
        }

        if (lastInvalid - firstInvalid + 1 != lineCount) {
          // XXX stupid hack
          Rectangle clip = g.getClipBounds();
          if (!clip.equals(getBounds())) {
            repaint();
          }

        }
      }
      catch (Exception e) {
        System.err.println("Error repainting line"
           + " range {" + firstInvalid + ","
           + lastInvalid + "}:");
        e.printStackTrace();
      }
      firstInvalid = lastInvalid = -1;
    }

    g.drawImage(offImg, 0, 0, null);
  }


  /**
   *  Same as <code>update(g)</code>.
   *
   * @param  g  Description of Parameter
   */
  public void paint(Graphics g) {
    update(g);
  }


  /**
   *  Marks a line as needing a repaint, but doesn't actually repaint it until
   *  <code>repaint()</code> is called manually.
   *
   * @param  line  The line to invalidate
   */
  public void _invalidateLine(int line) {
    int firstVisible = textArea.getFirstLine();
    int lastVisible = firstVisible + textArea.getVisibleLines();

    if (line < firstVisible || line > lastVisible) {
      return;
    }

    if (line >= firstInvalid && line <= lastInvalid) {
      return;
    }

    if (firstInvalid == -1 && lastInvalid == -1) {
      firstInvalid = lastInvalid = line;
    }

    else {
      firstInvalid = Math.min(line, firstInvalid);
      lastInvalid = Math.max(line, lastInvalid);
    }
  }


  /**
   *  Marks a range of lines as needing a repaint, but doesn't actually
   *  repaint them until <code>repaint()</code> is called.
   *
   * @param  firstLine  The first line to invalidate
   * @param  lastLine   The last line to invalidate
   */
  public void _invalidateLineRange(int firstLine, int lastLine) {
    int firstVisible = textArea.getFirstLine();
    int lastVisible = firstVisible + textArea.getVisibleLines();

    if (firstLine > lastLine) {
      int tmp = firstLine;
      firstLine = lastLine;
      lastLine = tmp;
    }

    if (lastLine < firstVisible || firstLine > lastVisible) {
      return;
    }

    if (firstInvalid == -1 && lastInvalid == -1) {
      firstInvalid = firstLine;
      lastInvalid = lastLine;
    }
    else {
      if (firstLine >= firstInvalid && lastLine <= lastInvalid) {
        return;
      }

      firstInvalid = Math.min(firstInvalid, firstLine);
      lastInvalid = Math.max(lastInvalid, lastLine);
    }

    firstInvalid = Math.max(firstInvalid, firstVisible);
    lastInvalid = Math.min(lastInvalid, lastVisible);
  }


  /**
   *  Simulates scrolling from <code>oldFirstLine</code> to <code>newFirstLine</code>
   *  by shifting the offscreen graphics and repainting any revealed lines.
   *  This should not be called directly; use <code>JEditTextArea.setFirstLine()</code>
   *  instead.
   *
   * @param  oldFirstLine  The old first line
   * @param  newFirstLine  The new first line
   * @see                  org.gjt.sp.jedit.textarea.JEditTextArea#setFirstLine(int)
   */
  public void scrollRepaint(int oldFirstLine, int newFirstLine) {
    if (offGfx == null) {
      return;
    }

    int visibleLines = textArea.getVisibleLines();

    // No point doing this crap if the user scrolled by >= visibleLines
    if (copyAreaBroken || oldFirstLine + visibleLines <= newFirstLine
       || newFirstLine + visibleLines <= oldFirstLine) {
      _invalidateLineRange(newFirstLine, newFirstLine + visibleLines + 1);
    }

    else {
      int y = fm.getHeight() * (oldFirstLine - newFirstLine);
      offGfx.copyArea(0, 0, offImg.getWidth(this), offImg.getHeight(this), 0, y);

      if (oldFirstLine < newFirstLine) {
        _invalidateLineRange(oldFirstLine + visibleLines - 1,
          newFirstLine + visibleLines + 1);
      }

      else {
        _invalidateLineRange(newFirstLine, oldFirstLine);
      }

    }
  }


  /**
   *  Implementation of TabExpander interface. Returns next tab stop after a
   *  specified point.
   *
   * @param  x          The x co-ordinate
   * @param  tabOffset  Ignored
   * @return            The next tab stop after <i>x</i>
   */
  public float nextTabStop(float x, int tabOffset) {
    int offset = textArea.getHorizontalOffset();
    int ntabs = ((int)x - offset) / tabSize;
    return (ntabs + 1) * tabSize + offset;
  }


  protected boolean ensureOffscreenValid() {
    if (offImg == null || offGfx == null) {
      offImg = textArea.createImage(getWidth(), getHeight());
      offGfx = offImg.getGraphics();
      return true;
    }
    else {
      return false;
    }
  }


  protected int offscreenRepaintLineRange(int firstLine, int lastLine) {
    if (offGfx == null) {
      return 0;
    }

    int x = textArea.getHorizontalOffset();

    int line;
    for (line = firstLine; line <= lastLine; ) {
      line += offscreenRepaintLine(line, x);
    }

    return line - firstLine;
  }


  protected int offscreenRepaintLine(int line, int x) {
    ACLSLTokenMarker tokenMarker = textArea.getTokenMarker();
    Font defaultFont = getFont();
    Color defaultColor = getForeground();

    int y = textArea.lineToY(line);

    if (line < 0 || line >= textArea.getLineCount()) {
      paintHighlight(line, y);
      styles[ACLToken.INVALID].setGraphicsFlags(offGfx, defaultFont);
      offGfx.drawString(".", 0, y + fm.getHeight());
      return 1;
    }

    if (tokenMarker == null) {
      currentLineIndex = line;
      paintPlainLine(line, defaultFont, defaultColor, x, y);
      return 1;
    }
    else {
      int count = 0;
      int lastVisibleLine = Math.min(textArea.getLineCount(),
        textArea.getFirstLine() + textArea.getVisibleLines());
      do {
        currentLineIndex = line + count;
        paintSyntaxLine(tokenMarker, currentLineIndex,
          defaultFont, defaultColor, x, y);
        y += fm.getHeight();

        count++;
      } while (tokenMarker.isNextLineRequested()
         && line + count < lastVisibleLine);
      return count;
    }
  }


  protected void paintPlainLine(int line, Font defaultFont,
                                Color defaultColor, int x, int y) {
    paintHighlight(line, y);
    textArea.getLineText(line, currentLine);

    offGfx.setFont(defaultFont);
    offGfx.setColor(defaultColor);

    y += fm.getHeight();
    x = Utilities.drawTabbedText(currentLine, x, y, offGfx, this, 0);

    if (eolMarkers) {
      offGfx.setColor(eolMarkerColor);
      offGfx.drawString(".", x, y);
    }
  }


  protected void paintSyntaxLine(ACLSLTokenMarker tokenMarker, int line,
                                 Font defaultFont, Color defaultColor, int x, int y) {
    textArea.getLineText(currentLineIndex, currentLine);
    currentLineTokens = tokenMarker.markTokens(currentLine,
      currentLineIndex);

    paintHighlight(line, y);

    offGfx.setFont(defaultFont);
    offGfx.setColor(defaultColor);
    styles = ACLSyntaxUtilities.getDefaultSyntaxStyles(this);
    y += fm.getHeight();
    x = ACLSyntaxUtilities.paintSyntaxLine(currentLine,
      currentLineTokens, styles, this, offGfx, x, y);

    if (eolMarkers) {
      offGfx.setColor(eolMarkerColor);
      offGfx.drawString(".", x, y);
    }
  }


  protected void paintHighlight(int line, int y) {
    /*
        Clear the line's bounding rectangle
      */
    int gap = fm.getMaxDescent() + fm.getLeading();
    offGfx.setColor(getBackground());
    offGfx.fillRect(0, y + gap, offImg.getWidth(this), fm.getHeight());

    if (line >= textArea.getSelectionStartLine()
       && line <= textArea.getSelectionEndLine()) {
      paintLineHighlight(line, y);
    }

    if (bracketHighlight && line == textArea.getBracketLine()) {
      paintBracketHighlight(line, y);
    }

    if (line == textArea.getCaretLine()) {
      paintCaret(line, y);
    }

  }


  protected void paintLineHighlight(int line, int y) {
    int height = fm.getHeight();
    y += fm.getLeading() + fm.getMaxDescent();

    int selectionStart = textArea.getSelectionStart();
    int selectionEnd = textArea.getSelectionEnd();

    if (selectionStart == selectionEnd) {
      if (lineHighlight) {
        offGfx.setColor(lineHighlightColor);
        offGfx.fillRect(0, y, offImg.getWidth(this), height);
      }

      else {
        offGfx.setColor(selectionColor);

        int selectionStartLine = textArea.getSelectionStartLine();
        int selectionEndLine = textArea.getSelectionEndLine();
        int lineStart = textArea.getLineStartOffset(line);

        int x1;

        int x2;
        if (selectionStartLine == selectionEndLine) {
          x1 = textArea.offsetToX(line,
            selectionStart - lineStart);
          x2 = textArea.offsetToX(line,
            selectionEnd - lineStart);
        }
        else if (line == selectionStartLine) {
          x1 = textArea.offsetToX(line,
            selectionStart - lineStart);
          x2 = offImg.getWidth(this);
        }
        else if (line == selectionEndLine) {
          x1 = 0;
          x2 = textArea.offsetToX(line,
            selectionEnd - lineStart);
        }
        else {
          x1 = 0;
          x2 = offImg.getWidth(this);
        }

        offGfx.fillRect(x1, y, x2 - x1, height);
      }
    }

  }


  protected void paintBracketHighlight(int line, int y) {
    int position = textArea.getBracketPosition();
    if (position == -1) {
      return;
    }
    y += fm.getLeading() + fm.getMaxDescent();
    int x = textArea.offsetToX(line, position);
    offGfx.setColor(bracketHighlightColor);
    // Hack!!! Since there is no fast way to get the character
    // from the bracket matching routine, we use ( since all
    // brackets probably have the same width anyway
    offGfx.drawRect(x, y, fm.charWidth('(') - 1,
      fm.getHeight() - 1);
  }


  protected void paintCaret(int line, int y) {
    if (textArea.isCaretVisible()) {
      int offset = textArea.getCaretPosition()
         - textArea.getLineStartOffset(line);
      int caretX = textArea.offsetToX(line, offset);
      int caretWidth = ((blockCaret ||
        textArea.isOverwriteEnabled()) ?
        fm.charWidth('w') : 1);
      y += fm.getLeading() + fm.getMaxDescent();
      int height = fm.getHeight();

      offGfx.setColor(caretColor);

      if (textArea.isOverwriteEnabled()) {
        offGfx.fillRect(caretX, y + height - 1,
          caretWidth, 1);
      }

      else {
        offGfx.drawRect(caretX, y, caretWidth - 1, height - 1);
      }

    }
  }


  protected static boolean copyAreaBroken;

  // protected members
  protected ACLTextArea textArea;

  protected ACLSytntaxStyle[] styles;
  protected Color caretColor;
  protected Color selectionColor;
  protected Color lineHighlightColor;
  protected Color bracketHighlightColor;
  protected Color eolMarkerColor;

  protected boolean blockCaret;
  protected boolean lineHighlight;
  protected boolean bracketHighlight;
  protected boolean eolMarkers;
  protected int cols;
  protected int rows;

  protected int tabSize;
  protected FontMetrics fm;
  protected Graphics offGfx;
  protected Image offImg;

  protected int firstInvalid;
  protected int lastInvalid;

  // package-private members
  int currentLineIndex;
  ACLToken currentLineTokens;
  Segment currentLine;
}
//  ***EOF***
