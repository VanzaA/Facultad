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
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.awt.event.*;
import java.awt.event.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;
import java.lang.*;
import java.lang.reflect.*;
import java.util.EventObject;
import java.util.Hashtable;
import java.util.StringTokenizer;
import javax.swing.*;
import javax.swing.*;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;

import javax.swing.border.EtchedBorder;
import javax.swing.event.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.text.*;

import jade.lang.acl.ACLMessage;
import jade.tools.sl.SLFormatter;

/**
 *  jEdit's text area component. The original file is written by Slava Pestov
 *  and altered to fit ACL/SL
 *
 * @author     Slava Pestov
 * @created    June 8, 2002
 * @version    $Id: ACLTextArea.java 5581 2005-02-23 08:59:06Z caire $
 */
public class ACLTextArea extends JComponent {
  /**
   *  Creates a new JEditTextArea with the default settings.
   */
  public ACLTextArea() {
    // Enable the necessary events
    enableEvents(AWTEvent.KEY_EVENT_MASK);

    // Initialize some misc. stuff
    painter = new ACLTextAreaPainter(this);
    AutoScroll scroller = new AutoScroll();
    scrollTimer = new Timer(200, scroller);
    documentHandler = new DocumentHandler();
    listenerList = new EventListenerList();
    caretEvent = new MutableCaretEvent();
    lineSegment = new Segment();
    bracketLine = bracketPosition = -1;
    blink = true;
    caretTimer = new Timer(500, new CaretBlinker());
    caretTimer.setInitialDelay(500);
    caretTimer.start();

    // Initialize the GUI
    setBorder(new EtchedBorder(EtchedBorder.LOWERED));
    setLayout(new java.awt.BorderLayout());

    add(painter, BorderLayout.CENTER);

    add(vertical = new JScrollBar(JScrollBar.VERTICAL), BorderLayout.EAST);
    add(horizontal = new JScrollBar(JScrollBar.HORIZONTAL), BorderLayout.SOUTH);
//    vertical.setVisibleAmount(20);
//    horizontal.setVisibleAmount(50);
    // Add some event listeners
    vertical.addAdjustmentListener(new AdjustHandler());
    horizontal.addAdjustmentListener(new AdjustHandler());
    painter.addComponentListener(new ComponentHandler());
    painter.addMouseListener(new MouseHandler());
    painter.addMouseMotionListener(scroller);
    addFocusListener(new FocusHandler());

    // Load the defaults
    InputHandler DEFAULT_INPUT_HANDLER = new InputHandler();
    DEFAULT_INPUT_HANDLER.addDefaultKeyBindings();

    setInputHandler(DEFAULT_INPUT_HANDLER);
    setDocument(new ACLSyntaxDocument());
    editable = true;
    caretVisible = true;
    caretBlinks = true;
    electricScroll = 3;

    // We don't seem to get the initial focus event?
    focusedComponent = this;

    //set tokenMarker
    setTokenMarker(new ACLSLTokenMarker());
  }


  /**
   *  Returns if this component can be traversed by pressing the Tab key. This
   *  returns false.
   *
   * @return    The ManagingFocus value
   */
  public final boolean isManagingFocus() {
    return true;
  }


  /**
   *  Returns the object responsible for painting this text area.
   *
   * @return    The Painter value
   */
  public final ACLTextAreaPainter getPainter() {
    return painter;
  }


  /**
   *  Returns the input handler.
   *
   * @return    The InputHandler value
   */
  public final InputHandler getInputHandler() {
    return inputHandler;
  }


  /**
   *  Returns true if the caret is blinking, false otherwise.
   *
   * @return    The CaretBlinkEnabled value
   */
  public final boolean isCaretBlinkEnabled() {
    return caretBlinks;
  }


  /**
   *  Returns true if the caret is visible, false otherwise.
   *
   * @return    The CaretVisible value
   */
  public final boolean isCaretVisible() {
    return (!caretBlinks || blink) && caretVisible;
  }


  /**
   *  Returns the number of lines from the top and button of the text area
   *  that are always visible.
   *
   * @return    The ElectricScroll value
   */
  public final int getElectricScroll() {
    return electricScroll;
  }


  /**
   *  Returns the line displayed at the text area's origin.
   *
   * @return    The FirstLine value
   */
  public final int getFirstLine() {
    return firstLine;
  }


  /**
   *  Returns the number of lines visible in this text area.
   *
   * @return    The VisibleLines value
   */
  public final int getVisibleLines() {
    return visibleLines;
  }


  /**
   *  Returns the horizontal offset of drawn lines.
   *
   * @return    The HorizontalOffset value
   */
  public final int getHorizontalOffset() {
    return horizontalOffset;
  }


  /**
   *  Returns the document this text area is editing.
   *
   * @return    The Document value
   */
  public final ACLSyntaxDocument getDocument() {
    return document;
  }


  /**
   *  Returns the document's token marker. Equivalent to calling <code>getDocument().getTokenMarker()</code>
   *  .
   *
   * @return    The TokenMarker value
   */
  public final ACLSLTokenMarker getTokenMarker() {
    return document.getTokenMarker();
  }


  /**
   *  Returns the length of the document. Equivalent to calling <code>getDocument().getLength()</code>
   *  .
   *
   * @return    The DocumentLength value
   */
  public final int getDocumentLength() {
    return document.getLength();
  }


  /**
   *  Returns the number of lines in the document.
   *
   * @return    The LineCount value
   */
  public final int getLineCount() {
    return document.getDefaultRootElement().getElementCount();
  }


  /**
   *  Returns the line containing the specified offset.
   *
   * @param  offset  The offset
   * @return         The LineOfOffset value
   */
  public final int getLineOfOffset(int offset) {
    return document.getDefaultRootElement().getElementIndex(offset);
  }


  /**
   *  Returns the specified substring of the document.
   *
   * @param  start  The start offset
   * @param  len    The length of the substring
   * @return        The substring, or null if the offsets are invalid
   */
  public final String getText(int start, int len) {
    try {
      return document.getText(start, len);
    }
    catch (BadLocationException bl) {
      return null;
    }
  }


  /**
   *  Copies the specified substring of the document into a segment. If the
   *  offsets are invalid, the segment will contain a null string.
   *
   * @param  start    The start offset
   * @param  len      The length of the substring
   * @param  segment  The segment
   */
  public final void getText(int start, int len, Segment segment) {
    try {
      document.getText(start, len, segment);
    }
    catch (BadLocationException bl) {
      segment.offset = segment.count = 0;
    }
  }


  /**
   *  Returns the text on the specified line.
   *
   * @param  lineIndex  The line
   * @return            The text, or null if the line is invalid
   */
  public final String getLineText(int lineIndex) {
    int start = getLineStartOffset(lineIndex);
    return getText(start, getLineEndOffset(lineIndex) - start - 1);
  }


  /**
   *  Copies the text on the specified line into a segment. If the line is
   *  invalid, the segment will contain a null string.
   *
   * @param  lineIndex  The line
   * @param  segment    Description of Parameter
   */
  public final void getLineText(int lineIndex, Segment segment) {
    int start = getLineStartOffset(lineIndex);
    getText(start, getLineEndOffset(lineIndex) - start - 1, segment);
  }


  /**
   *  Returns the selection start offset.
   *
   * @return    The SelectionStart value
   */
  public final int getSelectionStart() {
    return selectionStart;
  }


  /**
   *  Returns the selection start line.
   *
   * @return    The SelectionStartLine value
   */
  public final int getSelectionStartLine() {
    return selectionStartLine;
  }


  /**
   *  Returns the selection end offset.
   *
   * @return    The SelectionEnd value
   */
  public final int getSelectionEnd() {
    return selectionEnd;
  }


  /**
   *  Returns the selection end line.
   *
   * @return    The SelectionEndLine value
   */
  public final int getSelectionEndLine() {
    return selectionEndLine;
  }


  /**
   *  Returns the caret position. This will either be the selection start or
   *  the selection end, depending on which direction the selection was made
   *  in.
   *
   * @return    The CaretPosition value
   */
  public final int getCaretPosition() {
    return (biasLeft ? selectionStart : selectionEnd);
  }


  /**
   *  Returns the caret line.
   *
   * @return    The CaretLine value
   */
  public final int getCaretLine() {
    return (biasLeft ? selectionStartLine : selectionEndLine);
  }


  /**
   *  Returns the mark position. This will be the opposite selection bound to
   *  the caret position.
   *
   * @return    The MarkPosition value
   * @see       #getCaretPosition()
   */
  public final int getMarkPosition() {
    return (biasLeft ? selectionEnd : selectionStart);
  }


  /**
   *  Returns the mark line.
   *
   * @return    The MarkLine value
   */
  public final int getMarkLine() {
    return (biasLeft ? selectionEndLine : selectionStartLine);
  }


  /**
   *  Returns the selected text, or null if no selection is active.
   *
   * @return    The SelectedText value
   */
  public final String getSelectedText() {
    if (selectionStart == selectionEnd) {
      return null;
    }
    return getText(selectionStart,
      selectionEnd - selectionStart);
  }


  /**
   *  Returns true if this text area is editable, false otherwise.
   *
   * @return    The Editable value
   */
  public final boolean isEditable() {
    return editable;
  }


  /**
   *  Returns the right click popup menu.
   *
   * @return    The RightClickPopup value
   */
  public final JPopupMenu getRightClickPopup() {
    return popup;
  }


  /**
   *  Returns the `magic' caret position. This can be used to preserve the
   *  column position when moving up and down lines.
   *
   * @return    The MagicCaretPosition value
   */
  public final int getMagicCaretPosition() {
    return magicCaret;
  }


  /**
   *  Returns true if overwrite mode is enabled, false otherwise.
   *
   * @return    The OverwriteEnabled value
   */
  public final boolean isOverwriteEnabled() {
    return overwrite;
  }


  /**
   *  Returns the position of the highlighted bracket (the bracket matching
   *  the one before the caret)
   *
   * @return    The BracketPosition value
   */
  public final int getBracketPosition() {
    return bracketPosition;
  }


  /**
   *  Returns the line of the highlighted bracket (the bracket matching the
   *  one before the caret)
   *
   * @return    The BracketLine value
   */
  public final int getBracketLine() {
    return bracketLine;
  }


  /**
   *  Sets the number of lines from the top and bottom of the text area that
   *  are always visible
   *
   * @param  electricScroll  The number of lines always visible from the top
   *      or bottom
   */
  public final void setElectricScroll(int electricScroll) {
    this.electricScroll = electricScroll;
  }


  /**
   *  Sets the document's token marker. Equivalent to caling <code>getDocument().setTokenMarker()</code>
   *  .
   *
   * @param  tokenMarker  The token marker
   */
  public final void setTokenMarker(ACLSLTokenMarker tokenMarker) {
    document.setTokenMarker(tokenMarker);
  }


  /**
   *  Sets the selection start. The new selection will be the new selection
   *  start and the old selection end.
   *
   * @param  selectionStart  The selection start
   * @see                    #select(int,int)
   */
  public final void setSelectionStart(int selectionStart) {
    select(selectionStart, selectionEnd);
  }


  /**
   *  Sets the selection end. The new selection will be the old selection
   *  start and the bew selection end.
   *
   * @param  selectionEnd  The selection end
   * @see                  #select(int,int)
   */
  public final void setSelectionEnd(int selectionEnd) {
    select(selectionStart, selectionEnd);
  }


  /**
   *  Sets the caret position. The new selection will consist of the caret
   *  position only (hence no text will be selected)
   *
   * @param  caret  The caret position
   * @see           #select(int,int)
   */
  public final void setCaretPosition(int caret) {
    select(caret, caret);
  }


  /**
   *  Sets if this component is editable.
   *
   * @param  editable  True if this text area should be editable, false
   *      otherwise
   */
  public final void setEditable(boolean editable) {
    this.editable = editable;
  }


  /**
   *  Sets the right click popup menu.
   *
   * @param  popup  The popup
   */
  public final void setRightClickPopup(JPopupMenu popup) {
    this.popup = popup;
  }


  /**
   *  Sets the `magic' caret position. This can be used to preserve the column
   *  position when moving up and down lines.
   *
   * @param  magicCaret  The magic caret position
   */
  public final void setMagicCaretPosition(int magicCaret) {
    this.magicCaret = magicCaret;
  }


  /**
   *  Sets if overwrite mode should be enabled.
   *
   * @param  overwrite  True if overwrite mode should be enabled, false
   *      otherwise.
   */
  public final void setOverwriteEnabled(boolean overwrite) {
    this.overwrite = overwrite;
    painter.invalidateSelectedLines();
  }


  /**
   *  Blinks the caret.
   */
  public final void blinkCaret() {
    if (caretBlinks) {
      blink = !blink;
      painter.invalidateSelectedLines();
    }
    else {
      blink = true;
    }

  }


  /**
   *  Recalculates the number of visible lines. This should not be called
   *  directly.
   */
  public final void recalculateVisibleLines() {
    if (painter == null) {
      return;
    }
    int height = painter.getHeight();
    int lineHeight = painter.getFontMetrics().getHeight();
    int oldVisibleLines = visibleLines;
    visibleLines = height / lineHeight;
    painter.invalidateOffscreen();
    painter.repaint();
    updateScrollBars();
  }


  /**
   *  Selects all text in the document.
   */
  public final void selectAll() {
    select(0, getDocumentLength());
  }


  /**
   *  Adds a caret change listener to this text area.
   *
   * @param  listener  The listener
   */
  public final void addCaretListener(CaretListener listener) {
    listenerList.add(CaretListener.class, listener);
  }


  /**
   *  Removes a caret change listener from this text area.
   *
   * @param  listener  The listener
   */
  public final void removeCaretListener(CaretListener listener) {
    listenerList.remove(CaretListener.class, listener);
  }


  /**
   *  Returns the start offset of the specified line.
   *
   * @param  line  The line
   * @return       The start offset of the specified line, or -1 if the line
   *      is invalid
   */
  public int getLineStartOffset(int line) {
    Element lineElement = document.getDefaultRootElement()
      .getElement(line);
    if (lineElement == null) {
      return -1;
    }
    else {
      return lineElement.getStartOffset();
    }
  }


  /**
   *  Returns the end offset of the specified line.
   *
   * @param  line  The line
   * @return       The end offset of the specified line, or -1 if the line is
   *      invalid.
   */
  public int getLineEndOffset(int line) {
    Element lineElement = document.getDefaultRootElement()
      .getElement(line);
    if (lineElement == null) {
      return -1;
    }
    else {
      return lineElement.getEndOffset();
    }
  }


  /**
   *  Returns the length of the specified line.
   *
   * @param  line  The line
   * @return       The LineLength value
   */
  public int getLineLength(int line) {
    Element lineElement = document.getDefaultRootElement()
      .getElement(line);
    if (lineElement == null) {
      return -1;
    }
    else {
      return lineElement.getEndOffset()
         - lineElement.getStartOffset() - 1;
    }
  }


  /**
   *  Returns the entire text of this text area.
   *
   * @return    The Text value
   */
  public String getText() {
    try {
      return document.getText(0, document.getLength());
    }
    catch (BadLocationException bl) {
      return null;
    }
  }


  /**
   *  Sets the input handler.
   *
   * @param  inputHandler  The new input handler
   */
  public void setInputHandler(InputHandler inputHandler) {
    if (this.inputHandler != null) {
      removeKeyListener(this.inputHandler);
    }

    if (inputHandler != null) {
      addKeyListener(inputHandler);
    }

    this.inputHandler = inputHandler;
  }


  /**
   *  Toggles caret blinking.
   *
   * @param  caretBlinks  True if the caret should blink, false otherwise
   */
  public void setCaretBlinkEnabled(boolean caretBlinks) {
    this.caretBlinks = caretBlinks;
    if (!caretBlinks) {
      blink = false;
    }

    painter.invalidateSelectedLines();
  }


  /**
   *  Sets if the caret should be visible.
   *
   * @param  caretVisible  True if the caret should be visible, false
   *      otherwise
   */
  public void setCaretVisible(boolean caretVisible) {
    this.caretVisible = caretVisible;
    blink = true;

    painter.invalidateSelectedLines();
  }


  /**
   *  Sets the line displayed at the text area's origin without updating the
   *  scroll bars.
   *
   * @param  firstLine  The new FirstLine value
   */
  public void setFirstLine(int firstLine) {
    if (firstLine == this.firstLine) {
      return;
    }
    int oldFirstLine = this.firstLine;
    this.firstLine = firstLine;
    if (firstLine != vertical.getValue()) {
      updateScrollBars();
    }
    painter.scrollRepaint(oldFirstLine, firstLine);
    painter.repaint();
  }


  /**
   *  Sets the horizontal offset of drawn lines. This can be used to implement
   *  horizontal scrolling.
   *
   * @param  horizontalOffset  offset The new horizontal offset
   */
  public void setHorizontalOffset(int horizontalOffset) {
    if (horizontalOffset == this.horizontalOffset) {
      return;
    }
    this.horizontalOffset = horizontalOffset;
    if (horizontalOffset != horizontal.getValue()) {
      updateScrollBars();
    }

    painter.invalidateLineRange(firstLine, firstLine + visibleLines);
    painter.repaint();
  }


  /**
   *  A fast way of changing both the first line and horizontal offset.
   *
   * @param  firstLine         The new first line
   * @param  horizontalOffset  The new horizontal offset
   * @return                   True if any of the values were changed, false
   *      otherwise
   */
  public boolean setOrigin(int firstLine, int horizontalOffset) {
    boolean changed = false;
    boolean fullRepaint = false;
    int oldFirstLine = this.firstLine;

    if (horizontalOffset != this.horizontalOffset) {
      this.horizontalOffset = horizontalOffset;
      changed = fullRepaint = true;
    }

    if (firstLine != this.firstLine) {
      this.firstLine = firstLine;
      changed = true;
    }

    if (changed) {
      updateScrollBars();
      if (fullRepaint) {
        painter._invalidateLineRange(firstLine,
          firstLine + visibleLines);
      }

      else {
        painter.scrollRepaint(oldFirstLine, firstLine);
      }

      painter.repaint();
    }

    return changed;
  }


  /**
   *  Sets the document this text area is editing.
   *
   * @param  document  The document
   */
  public void setDocument(ACLSyntaxDocument document) {
    if (this.document == document) {
      return;
    }
    if (this.document != null) {
      this.document.removeDocumentListener(documentHandler);
    }

    this.document = document;

    document.addDocumentListener(documentHandler);

    select(0, 0);
    updateScrollBars();
    painter.invalidateOffscreen();
    painter.repaint();
  }


  /**
   *  Sets the entire text of this text area.
   *
   * @param  text  The new Text value
   */
  public void setText(String text) {
    try {
      document.remove(0, document.getLength());
      document.insertString(0, text, null);
    }
    catch (BadLocationException bl) {
      bl.printStackTrace();
    }
  }


  /**
   *  Replaces the selection with the specified text.
   *
   * @param  selectedText  The replacement text for the selection
   */
  public void setSelectedText(String selectedText) {
    if (!editable) {
      throw new InternalError("Text component"
         + " read only");
    }

    try {
      document.remove(selectionStart,
        selectionEnd - selectionStart);
      document.insertString(selectionStart,
        selectedText, null);
      setCaretPosition(selectionEnd);
    }
    catch (BadLocationException bl) {
      bl.printStackTrace();
      throw new InternalError("Cannot replace"
         + " selection");
    }
  }


  public void update() {
    this.register(msg, "Content");
  }


  /**
   *  Description of the Method
   *
   * @param  arg        Description of Parameter
   * @param  fieldName  Description of Parameter
   */
  public void register(Object arg, String fieldName) {
    this.msg = (ACLMessage)arg;
    this.fieldName = fieldName;
    contentLanguage = (msg.getLanguage() != null ? msg.getLanguage() : "<unknown>");
    String methodName = "get" + fieldName;
    String content = "";

    try {
      Method sn = msg.getClass().getMethod(methodName, (Class[]) null);
      content = (String)sn.invoke(msg, new Object[]{});
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
    if (contentLanguage.indexOf("SL") >= 0) {
      //Only format when SL
      try {
        content = (String)new SLFormatter().format(content);
      }
      catch (Exception ex) {
        //too bad!
      }
    }

    while ((content != null) && (content.indexOf('\n')) == 0) {
      content = content.substring(1);
    }

    setText(content);
    this.setCaretPosition(0);
  }


  /**
   *  Description of the Method
   *
   * @param  arg  Description of Parameter
   * @param  str  Description of Parameter
   */
  public void unregister(Object arg, String str) {
//    msg.deleteObserver(this);
  }


  /**
   *  Description of the Method
   *
   * @param  e  Description of Parameter
   */
  public void focusLost(FocusEvent e) {
    String value = getText();
    while ((value != null) && ((value.indexOf('\n') == 0) || (value.indexOf(' ') == 0))) {
      value = value.substring(1);
    }

    String methodName = "set" + fieldName;
    String theType = "java.lang.String";
    try {
      Method sn = msg.getClass().getMethod(methodName, new Class[]{Class.forName(theType)});
      Object os = value;
      sn.invoke(msg, new Object[]{os});
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
  }


  /**
   *  Updates the state of the scroll bars. This should be called if the
   *  number of lines in the document changes, or when the size of the text
   *  are changes.
   */
  public void updateScrollBars() {

    if (vertical != null && visibleLines != 0) {
      vertical.setValues(firstLine, visibleLines, 0, getLineCount());
      vertical.setUnitIncrement(2);
      vertical.setBlockIncrement(visibleLines);
    }
    int width = painter.getWidth();
    if (horizontal != null && width != 0) {
      horizontal.setValues(-horizontalOffset, width, 0, width * 5);
      horizontal.setUnitIncrement(painter.getFontMetrics()
        .charWidth('w'));
      horizontal.setBlockIncrement(width / 2);
    }
  }


  /**
   *  Ensures that the caret is visible by scrolling the text area if
   *  necessary.
   *
   * @return    True if scrolling was actually performed, false if the caret
   *      was already visible
   */
  public boolean scrollToCaret() {
    int line = getCaretLine();
    int lineStart = getLineStartOffset(line);
    int offset = Math.max(0, Math.min(getLineLength(line) - 1,
      getCaretPosition() - lineStart));

    return scrollTo(line, offset);
  }


  /**
   *  Ensures that the specified line and offset is visible by scrolling the
   *  text area if necessary.
   *
   * @param  line    The line to scroll to
   * @param  offset  The offset in the line to scroll to
   * @return         True if scrolling was actually performed, false if the
   *      line and offset was already visible
   */
  public boolean scrollTo(int line, int offset) {
    // visibleLines == 0 before the component is realized
    // we can't do any proper scrolling then, so we have
    // this hack...
    if (visibleLines == 0) {
      setFirstLine(Math.max(0, line - electricScroll));
      return true;
    }

    int newFirstLine = firstLine;
    int newHorizontalOffset = horizontalOffset;

    if (line < firstLine + electricScroll) {
      newFirstLine = Math.max(0, line - electricScroll);
    }

    else if (line + electricScroll >= firstLine + visibleLines) {
      newFirstLine = (line - visibleLines) + electricScroll + 1;
      if (newFirstLine + visibleLines >= getLineCount()) {
        newFirstLine = getLineCount() - visibleLines;
      }

      if (newFirstLine < 0) {
        newFirstLine = 0;
      }

    }

    int x = offsetToX(line, offset);
    int width = painter.getFontMetrics().charWidth('W');

    if (x < 0) {
      newHorizontalOffset = Math.min(0, horizontalOffset
         - x + width);
    }

    else if (x + width >= painter.getWidth()) {
      newHorizontalOffset = horizontalOffset +
        (painter.getWidth() - x) - width;
    }

    return setOrigin(newFirstLine, newHorizontalOffset);
  }


  /**
   *  Converts a line index to a y co-ordinate.
   *
   * @param  line  The line
   * @return       Description of the Returned Value
   */
  public int lineToY(int line) {
    FontMetrics fm = painter.getFontMetrics();
    return (line - firstLine) * fm.getHeight()
       - (fm.getLeading() + fm.getMaxDescent());
  }


  /**
   *  Converts a y co-ordinate to a line index.
   *
   * @param  y  The y co-ordinate
   * @return    Description of the Returned Value
   */
  public int yToLine(int y) {
    FontMetrics fm = painter.getFontMetrics();
    int height = fm.getHeight();
    return Math.max(0, Math.min(getLineCount() - 1,
      y / height + firstLine));
  }


  /**
   *  Converts an offset in a line into an x co-ordinate.
   *
   * @param  line    The line
   * @param  offset  The offset, from the start of the line
   * @return         Description of the Returned Value
   */
  public int offsetToX(int line, int offset) {
    ACLSLTokenMarker tokenMarker = getTokenMarker();

    /*
        Use painter's cached info for speed
      */
    FontMetrics fm = painter.getFontMetrics();

    getLineText(line, lineSegment);

    int segmentOffset = lineSegment.offset;
    int x = horizontalOffset;

    /*
        If syntax coloring is disabled, do simple translation
      */
    if (tokenMarker == null) {
      lineSegment.count = offset;
      return x + Utilities.getTabbedTextWidth(lineSegment,
        fm, x, painter, 0);
    }
    /*
        If syntax coloring is enabled, we have to do this because
        tokens can vary in width
      */
    else {
      ACLToken tokens;
      if (painter.currentLineIndex == line) {
        tokens = painter.currentLineTokens;
      }

      else {
        painter.currentLineIndex = line;
        tokens = painter.currentLineTokens
           = tokenMarker.markTokens(lineSegment, line);
      }

      Toolkit toolkit = painter.getToolkit();
      Font defaultFont = painter.getFont();
      ACLSytntaxStyle[] styles = painter.getStyles();

      for (; ; ) {
        byte id = tokens.id;
        if (id == ACLToken.END) {
          return x;
        }

        if (id == ACLToken.NULL) {
          fm = painter.getFontMetrics();
        }

        else {
          fm = styles[id].getFontMetrics(defaultFont);
        }

        int length = tokens.length;

        if (offset + segmentOffset < lineSegment.offset + length) {
          lineSegment.count = offset - (lineSegment.offset - segmentOffset);
          return x + Utilities.getTabbedTextWidth(
            lineSegment, fm, x, painter, 0);
        }
        else {
          lineSegment.count = length;
          x += Utilities.getTabbedTextWidth(
            lineSegment, fm, x, painter, 0);
          lineSegment.offset += length;
        }
        tokens = tokens.next;
      }
    }
  }


  /**
   *  Converts an x co-ordinate to an offset within a line.
   *
   * @param  line  The line
   * @param  x     The x co-ordinate
   * @return       Description of the Returned Value
   */
  public int xToOffset(int line, int x) {
    ACLSLTokenMarker tokenMarker = getTokenMarker();

    /*
        Use painter's cached info for speed
      */
    FontMetrics fm = painter.getFontMetrics();

    getLineText(line, lineSegment);

    char[] segmentArray = lineSegment.array;
    int segmentOffset = lineSegment.offset;
    int segmentCount = lineSegment.count;

    int width = horizontalOffset;

    if (tokenMarker == null) {
      for (int i = 0; i < segmentCount; i++) {
        char c = segmentArray[i + segmentOffset];
        int charWidth;
        if (c == '\t') {
          charWidth = (int)painter.nextTabStop(width, i)
             - width;
        }

        else {
          charWidth = fm.charWidth(c);
        }

        if (painter.isBlockCaretEnabled()) {
          if (x - charWidth <= width) {
            return i;
          }

          else
            if (x - charWidth / 2 <= width) {
            return i;
          }
        }

        width += charWidth;
      }

      return segmentCount;
    }
    else {
      ACLToken tokens;
      if (painter.currentLineIndex == line) {
        tokens = painter.currentLineTokens;
      }

      else {
        painter.currentLineIndex = line;
        tokens = painter.currentLineTokens
           = tokenMarker.markTokens(lineSegment, line);
      }

      int offset = 0;
      Toolkit toolkit = painter.getToolkit();
      Font defaultFont = painter.getFont();
      ACLSytntaxStyle[] styles = painter.getStyles();

      for (; ; ) {
        byte id = tokens.id;
        if (id == ACLToken.END) {
          return offset;
        }

        if (id == ACLToken.NULL) {
          fm = painter.getFontMetrics();
        }

        else {
          fm = styles[id].getFontMetrics(defaultFont);
        }

        int length = tokens.length;

        for (int i = 0; i < length; i++) {
          char c = segmentArray[segmentOffset + offset + i];
          int charWidth;
          if (c == '\t') {
            charWidth = (int)painter.nextTabStop(width, offset + i)
               - width;
          }

          else {
            charWidth = fm.charWidth(c);
          }

          if (painter.isBlockCaretEnabled()) {
            if (x - charWidth <= width) {
              return offset + i;
            }

            else
              if (x - charWidth / 2 <= width) {
              return offset + i;
            }
          }

          width += charWidth;
        }

        offset += length;
        tokens = tokens.next;
      }
    }
  }


  /**
   *  Converts a point to an offset, from the start of the text.
   *
   * @param  x  The x co-ordinate of the point
   * @param  y  The y co-ordinate of the point
   * @return    Description of the Returned Value
   */
  public int xyToOffset(int x, int y) {
    int line = yToLine(y);
    int start = getLineStartOffset(line);
    return start + xToOffset(line, x);
  }


  /**
   *  Selects from the start offset to the end offset. This is the general
   *  selection method used by all other selecting methods. The caret position
   *  will be start if start &lt; end, and end if end &gt; start.
   *
   * @param  start  The start offset
   * @param  end    The end offset
   */
  public void select(int start, int end) {
    int newStart;
    int newEnd;
    boolean newBias;
    if (start <= end) {
      newStart = start;
      newEnd = end;
      newBias = false;
    }
    else {
      newStart = end;
      newEnd = start;
      newBias = true;
    }

    if (newStart < 0 || newEnd > getDocumentLength()) {
      throw new IllegalArgumentException("Bounds out of"
         + " range: " + newStart + "," +
        newEnd);
    }

    // If the new position is the same as the old, we don't
    // do all this crap, however we still do the stuff at
    // the end (clearing magic position, scrolling)
    if (newStart != selectionStart || newEnd != selectionEnd
       || newBias != biasLeft) {
      int newStartLine = getLineOfOffset(newStart);
      int newEndLine = getLineOfOffset(newEnd);

      if (painter.isBracketHighlightEnabled()) {
        if (bracketLine != -1) {
          painter._invalidateLine(bracketLine);
        }

        updateBracketHighlight(end);
        if (bracketLine != -1) {
          painter._invalidateLine(bracketLine);
        }

      }

      painter._invalidateLineRange(selectionStartLine, selectionEndLine);
      painter._invalidateLineRange(newStartLine, newEndLine);

      selectionStart = newStart;
      selectionEnd = newEnd;
      selectionStartLine = newStartLine;
      selectionEndLine = newEndLine;
      biasLeft = newBias;

      fireCaretEvent();
    }

    // When the user is typing, etc, we don't want the caret
    // to blink
    blink = true;
    caretTimer.restart();

    // Clear the `magic' caret position used by up/down
    magicCaret = -1;

    if (!scrollToCaret()) {
      painter.fastRepaint();
    }

  }


  /**
   *  Similar to <code>setSelectedText()</code>, but overstrikes the
   *  appropriate number of characters if overwrite mode is enabled.
   *
   * @param  str  The string
   * @see         #setSelectedText(String)
   * @see         #isOverwriteEnabled()
   */
  public void overwriteSetSelectedText(String str) {
    // Don't overstrike if there is a selection
    if (!overwrite || selectionStart != selectionEnd) {
      setSelectedText(str);
      return;
    }

    // Don't overstrike if we're on the end of
    // the line
    int caret = getCaretPosition();
    int caretLineEnd = getLineEndOffset(getCaretLine());
    if (caretLineEnd - caret <= str.length()) {
      setSelectedText(str);
      return;
    }

    try {
      document.remove(caret, str.length());
      document.insertString(caret, str, null);
    }
    catch (BadLocationException bl) {
      bl.printStackTrace();
    }
  }


  /**
   *  Deletes the selected text from the text area and places it into the
   *  clipboard.
   */
  public void cut() {
    if (editable) {
      copy();
      setSelectedText("");
    }
  }


  /**
   *  Places the selected text into the clipboard.
   */
  public void copy() {
    if (selectionStart != selectionEnd) {
      Clipboard clipboard = getToolkit().getSystemClipboard();
      StringSelection selection = new StringSelection(
        getSelectedText());
      clipboard.setContents(selection, null);
    }
  }


  /**
   *  Inserts the clipboard contents into the text.
   */
  public void paste() {
    if (editable) {
      Clipboard clipboard = getToolkit().getSystemClipboard();
      try {
        String selection = (String)(clipboard.getContents(this).getTransferData(
          DataFlavor.stringFlavor));

        // The MacOS MRJ doesn't convert \r to \n,
        // so do it here
        setSelectedText(selection.replace('\r', '\n'));
      }
      catch (Exception e) {
        getToolkit().beep();
        System.err.println("Clipboard does not"
           + " contain a string");
      }
    }
  }


  /**
   *  Called by the AWT when this component is removed from it's parent. This
   *  stops any autoscrolling and clears the currently focused component.
   */
  public void removeNotify() {
    super.removeNotify();
    if (focusedComponent == this) {
      focusedComponent = null;
    }

    if (scrollTimer.isRunning()) {
      scrollTimer.stop();
    }

  }


  /**
   *  Description of the Method
   *
   * @param  e  Description of Parameter
   */
  protected void processFocusEvent(FocusEvent e) {
    super.processFocusEvent(e);
    if (e.getID() == e.FOCUS_LOST) {
      focusLost(e);
    }

  }


  protected void fireCaretEvent() {
    Object[] listeners = listenerList.getListenerList();
    for (int i = listeners.length - 2; i >= 0; i--) {
      if (listeners[i] == CaretListener.class) {
        ((CaretListener)listeners[i + 1]).caretUpdate(caretEvent);
      }
    }

  }


  protected void updateBracketHighlight(int newCaretPosition) {
    if (newCaretPosition == 0) {
      bracketPosition = bracketLine = -1;
      return;
    }

    try {
      int offset = TextUtilities.findMatchingBracket(
        document, newCaretPosition - 1);
      if (offset != -1) {
        bracketLine = getLineOfOffset(offset);
        bracketPosition = offset - getLineStartOffset(bracketLine);
        return;
      }
    }
    catch (BadLocationException bl) {
      bl.printStackTrace();
    }

    bracketLine = bracketPosition = -1;
  }


  protected void documentChanged(DocumentEvent evt) {
    DocumentEvent.ElementChange ch = evt.getChange(
      document.getDefaultRootElement());

    int count;
    if (ch == null) {
      count = 0;
    }

    else {
      count = ch.getChildrenAdded().length -
        ch.getChildrenRemoved().length;
    }

    if (count == 0) {
      int line = getLineOfOffset(evt.getOffset());
      painter._invalidateLine(line);
    }
    else {
      int index = ch.getIndex();
      painter._invalidateLineRange(index, Math.max(getLineCount(),
        firstLine + visibleLines));
      updateScrollBars();
    }
  }


  public static class TextUtilities {
    /**
     *  Returns the offset of the bracket matching the one at the specified
     *  offset of the document, or -1 if the bracket is unmatched (or if the
     *  character is not a bracket).
     *
     * @param  doc                       The document
     * @param  offset                    The offset
     * @return                           Description of the Returned Value
     * @exception  BadLocationException  If an out-of-bounds access was
     *      attempted on the document text
     */
    public static int findMatchingBracket(Document doc, int offset)
       throws BadLocationException {
      if (doc.getLength() == 0) {
        return -1;
      }
      char c = doc.getText(offset, 1).charAt(0);
      char cprime;// c` - corresponding character
      boolean direction;// true = back, false = forward

      switch (c) {
        case '(':
          cprime = ')';
          direction = false;
          break;
        case ')':
          cprime = '(';
          direction = true;
          break;
        case '[':
          cprime = ']';
          direction = false;
          break;
        case ']':
          cprime = '[';
          direction = true;
          break;
        case '{':
          cprime = '}';
          direction = false;
          break;
        case '}':
          cprime = '{';
          direction = true;
          break;
        default:
          return -1;
      }

      int count;

      // How to merge these two cases is left as an exercise
      // for the reader.

      // Go back or forward
      if (direction) {
        // Count is 1 initially because we have already
        // `found' one closing bracket
        count = 1;

        // Get text[0,offset-1];
        String text = doc.getText(0, offset);

        // Scan backwards
        for (int i = offset - 1; i >= 0; i--) {
          // If text[i] == c, we have found another
          // closing bracket, therefore we will need
          // two opening brackets to complete the
          // match.
          char x = text.charAt(i);
          if (x == c) {
            count++;
          }

          // If text[i] == cprime, we have found a
          // opening bracket, so we return i if
          // --count == 0
          else if (x == cprime) {
            if (--count == 0) {
              return i;
            }
          }

        }
      }
      else {
        // Count is 1 initially because we have already
        // `found' one opening bracket
        count = 1;

        // So we don't have to + 1 in every loop
        offset++;

        // Number of characters to check
        int len = doc.getLength() - offset;

        // Get text[offset+1,len];
        String text = doc.getText(offset, len);

        // Scan forwards
        for (int i = 0; i < len; i++) {
          // If text[i] == c, we have found another
          // opening bracket, therefore we will need
          // two closing brackets to complete the
          // match.
          char x = text.charAt(i);

          if (x == c) {
            count++;
          }

          // If text[i] == cprime, we have found an
          // closing bracket, so we return i if
          // --count == 0
          else if (x == cprime) {
            if (--count == 0) {
              return i + offset;
            }
          }

        }
      }

      // Nothing found
      return -1;
    }
  }


  static class CaretBlinker implements ActionListener {
    public void actionPerformed(ActionEvent evt) {
      if (focusedComponent != null
         && focusedComponent.hasFocus()) {
        focusedComponent.blinkCaret();
      }

    }
  }


  private static class InputHandler implements KeyListener {

    /**
     *  Creates a new input handler with no key bindings defined.
     */
    public InputHandler() {
      bindings = currentBindings = new Hashtable();
    }


    public static ACLTextArea getTextArea(EventObject evt) {

      if (evt != null) {
        Object o = evt.getSource();
        if (o instanceof Component) {
          // find the parent text area
          Component c = (Component)o;
          for (; ; ) {
            if (c instanceof ACLTextArea) {
              return (ACLTextArea)c;
            }
            else if (c == null) {
              break;
            }
            if (c instanceof JPopupMenu) {
              c = ((JPopupMenu)c)
                .getInvoker();
            }

            else {
              c = c.getParent();
            }

          }
        }
      }

      // this shouldn't happen
      System.err.println("BUG: getTextArea() returning null");
      System.err.println("Report this to Slava Pestov <sp@gjt.org>");
      return null;
    }


    /**
     *  Converts a string to a keystroke. The string should be of the form <i>
     *  modifiers</i> +<i>shortcut</i> where <i>modifiers</i> is any
     *  combination of A for Alt, C for Control, S for Shift or M for Meta,
     *  and <i>shortcut </i> is either a single character, or a keycode name
     *  from the <code>KeyEvent</code> class, without the <code>VK_</code>
     *  prefix.
     *
     * @param  keyStroke  A string description of the key stroke
     * @return            Description of the Returned Value
     */
    public static KeyStroke parseKeyStroke(String keyStroke) {
      if (keyStroke == null) {
        return null;
      }
      int modifiers = 0;
      int ch = '\0';
      int index = keyStroke.indexOf('+');
      if (index != -1) {
        for (int i = 0; i < index; i++) {
          switch (Character.toUpperCase(keyStroke.charAt(i))) {
            case 'A':
              modifiers |= InputEvent.ALT_MASK;
              break;
            case 'C':
              modifiers |= InputEvent.CTRL_MASK;
              break;
            case 'M':
              modifiers |= InputEvent.META_MASK;
              break;
            case 'S':
              modifiers |= InputEvent.SHIFT_MASK;
              break;
          }
        }
      }

      String key = keyStroke.substring(index + 1);
      if (key.length() == 1) {
        ch = Character.toUpperCase(key.charAt(0));
      }

      else if (key.length() == 0) {
        System.err.println("Invalid key stroke: " + keyStroke);
        return null;
      }
      else {
        try {
          ch = KeyEvent.class.getField("VK_".concat(key))
            .getInt(null);
        }
        catch (Exception e) {
          System.err.println("Invalid key stroke: "
             + keyStroke);
          return null;
        }
      }

      return KeyStroke.getKeyStroke(ch, modifiers);
    }


    /**
     *  Sets up the default key bindings.
     */
    public void addDefaultKeyBindings() {
      addKeyBinding("BACK_SPACE", BACKSPACE);
      addKeyBinding("DELETE", DELETE);

      addKeyBinding("ENTER", INSERT_BREAK);
      addKeyBinding("TAB", INSERT_TAB);

      addKeyBinding("INSERT", OVERWRITE);

      addKeyBinding("HOME", HOME);
      addKeyBinding("END", END);
      addKeyBinding("S+HOME", SELECT_HOME);
      addKeyBinding("S+END", SELECT_END);

      addKeyBinding("PAGE_UP", PREV_PAGE);
      addKeyBinding("PAGE_DOWN", NEXT_PAGE);
      addKeyBinding("S+PAGE_UP", SELECT_PREV_PAGE);
      addKeyBinding("S+PAGE_DOWN", SELECT_NEXT_PAGE);

      addKeyBinding("LEFT", PREV_CHAR);
      addKeyBinding("S+LEFT", SELECT_PREV_CHAR);
      addKeyBinding("C+LEFT", PREV_WORD);
      addKeyBinding("CS+LEFT", SELECT_PREV_WORD);
      addKeyBinding("RIGHT", NEXT_CHAR);
      addKeyBinding("S+RIGHT", SELECT_NEXT_CHAR);
      addKeyBinding("C+RIGHT", NEXT_WORD);
      addKeyBinding("CS+RIGHT", SELECT_NEXT_WORD);
      addKeyBinding("UP", PREV_LINE);
      addKeyBinding("S+UP", SELECT_PREV_LINE);
      addKeyBinding("DOWN", NEXT_LINE);
      addKeyBinding("S+DOWN", SELECT_NEXT_LINE);
    }


    /**
     *  Adds a key binding to this input handler. The key binding is a list of
     *  white space separated key strokes of the form <i>[modifiers+]key</i>
     *  where modifier is C for Control, A for Alt, or S for Shift, and key is
     *  either a character (a-z) or a field name in the KeyEvent class
     *  prefixed with VK_ (e.g., BACK_SPACE)
     *
     * @param  keyBinding  The key binding
     * @param  action      The action
     */
    public void addKeyBinding(String keyBinding, ActionListener action) {
      Hashtable current = bindings;

      StringTokenizer st = new StringTokenizer(keyBinding);
      while (st.hasMoreTokens()) {
        KeyStroke keyStroke = parseKeyStroke(st.nextToken());
        if (keyStroke == null) {
          return;
        }

        if (st.hasMoreTokens()) {
          Object o = current.get(keyStroke);
          if (o instanceof Hashtable) {
            current = (Hashtable)o;
          }

          else {
            o = new Hashtable();
            current.put(keyStroke, o);
            current = (Hashtable)o;
          }
        }
        else {
          current.put(keyStroke, action);
        }

      }
    }


    /**
     *  Removes a key binding from this input handler. This is not yet
     *  implemented.
     *
     * @param  keyBinding  The key binding
     */
    public void removeKeyBinding(String keyBinding) {
      throw new InternalError("Not yet implemented");
    }


    /**
     *  Removes all key bindings from this input handler.
     */
    public void removeAllKeyBindings() {
      bindings.clear();
    }


    /**
     *  Handle a key pressed event. This will look up the binding for the key
     *  stroke and execute it.
     *
     * @param  evt  Description of Parameter
     */
    public void keyPressed(KeyEvent evt) {
      int keyCode = evt.getKeyCode();
      int modifiers = evt.getModifiers();
      if ((modifiers & ~KeyEvent.SHIFT_MASK) != 0
         || evt.isActionKey()
         || keyCode == KeyEvent.VK_BACK_SPACE
         || keyCode == KeyEvent.VK_DELETE
         || keyCode == KeyEvent.VK_ENTER
         || keyCode == KeyEvent.VK_TAB) {
        KeyStroke keyStroke = KeyStroke.getKeyStroke(keyCode,
          modifiers);
        Object o = currentBindings.get(keyStroke);
        if (o == null) {
          // Don't beep if the user presses some
          // key we don't know about unless a
          // prefix is active. Otherwise it will
          // beep when caps lock is pressed, etc.
          if (currentBindings != bindings) {
            Toolkit.getDefaultToolkit().beep();
            // F10 should be passed on, but C+e F10
            // shouldn't
            evt.consume();
          }
          currentBindings = bindings;
          return;
        }
        else if (o instanceof ActionListener) {
          ((ActionListener)o).actionPerformed(
            new ActionEvent(evt.getSource(),
            ActionEvent.ACTION_PERFORMED,
            null, modifiers));
          currentBindings = bindings;
          evt.consume();
          return;
        }
        else if (o instanceof Hashtable) {
          currentBindings = (Hashtable)o;
          evt.consume();
          return;
        }
        else if (keyCode != KeyEvent.VK_ALT
           && keyCode != KeyEvent.VK_CONTROL
           && keyCode != KeyEvent.VK_SHIFT
           && keyCode != KeyEvent.VK_META) {
          return;
        }
      }
    }


    /**
     *  Handle a key released event. These are ignored.
     *
     * @param  evt  Description of Parameter
     */
    public void keyReleased(KeyEvent evt) {
    }


    /**
     *  Handle a key typed event. This inserts the key into the text area.
     *
     * @param  evt  Description of Parameter
     */
    public void keyTyped(KeyEvent evt) {

      int modifiers = evt.getModifiers();
      char c = evt.getKeyChar();
      if (c != KeyEvent.CHAR_UNDEFINED &&
        (modifiers & KeyEvent.ALT_MASK) == 0) {
        if (c >= 0x20 && c != 0x7f) {
          ACLTextArea textArea = getTextArea(evt);
          if (!textArea.isEditable()) {
            textArea.getToolkit().beep();
            return;
          }

          currentBindings = bindings;

          textArea.overwriteSetSelectedText(String.valueOf(c));
        }
      }

    }


    public static class backspace implements ActionListener {
      public void actionPerformed(ActionEvent evt) {
        ACLTextArea textArea = getTextArea(evt);

        if (!textArea.isEditable()) {
          textArea.getToolkit().beep();
          return;
        }

        if (textArea.getSelectionStart()
           != textArea.getSelectionEnd()) {
          textArea.setSelectedText("");
        }

        else {
          int caret = textArea.getCaretPosition();
          if (caret == 0) {
            textArea.getToolkit().beep();
            return;
          }
          try {
            textArea.getDocument().remove(caret - 1, 1);
          }
          catch (BadLocationException bl) {
            bl.printStackTrace();
          }
        }
      }
    }


    public static class delete implements ActionListener {
      public void actionPerformed(ActionEvent evt) {
        ACLTextArea textArea = getTextArea(evt);

        if (!textArea.isEditable()) {
          textArea.getToolkit().beep();
          return;
        }

        if (textArea.getSelectionStart()
           != textArea.getSelectionEnd()) {
          textArea.setSelectedText("");
        }

        else {
          int caret = textArea.getCaretPosition();
          if (caret == textArea.getDocumentLength()) {
            textArea.getToolkit().beep();
            return;
          }
          try {
            textArea.getDocument().remove(caret, 1);
          }
          catch (BadLocationException bl) {
            bl.printStackTrace();
          }
        }
      }
    }


    public static class end implements ActionListener {

      public end(boolean select) {
        this.select = select;
      }


      public void actionPerformed(ActionEvent evt) {

        ACLTextArea textArea = getTextArea(evt);

        int caret = textArea.getCaretPosition();

        int lastOfLine = textArea.getLineEndOffset(
          textArea.getCaretLine()) - 1;
        int lastVisibleLine = textArea.getFirstLine()
           + textArea.getVisibleLines();
        if (lastVisibleLine >= textArea.getLineCount()) {
          lastVisibleLine = Math.min(textArea.getLineCount() - 1,
            lastVisibleLine);
        }

        else {
          lastVisibleLine -= (textArea.getElectricScroll() + 1);
        }

        int lastVisible = textArea.getLineEndOffset(lastVisibleLine) - 1;
        int lastDocument = textArea.getDocumentLength();

        if (caret == lastDocument) {
          textArea.getToolkit().beep();
          return;
        }
        else if (caret == lastVisible) {
          caret = lastDocument;
        }

        else if (caret == lastOfLine) {
          caret = lastVisible;
        }

        else {
          caret = lastOfLine;
        }

        if (select) {
          textArea.select(textArea.getMarkPosition(), caret);
        }

        else {
          textArea.setCaretPosition(caret);
        }

      }


      private boolean select;
    }


    public static class home implements ActionListener {

      public home(boolean select) {
        this.select = select;
      }


      public void actionPerformed(ActionEvent evt) {
        ACLTextArea textArea = getTextArea(evt);

        int caret = textArea.getCaretPosition();

        int firstLine = textArea.getFirstLine();

        int firstOfLine = textArea.getLineStartOffset(
          textArea.getCaretLine());
        int firstVisibleLine = (firstLine == 0 ? 0 :
          firstLine + textArea.getElectricScroll());
        int firstVisible = textArea.getLineStartOffset(
          firstVisibleLine);

        if (caret == 0) {
          textArea.getToolkit().beep();
          return;
        }
        else if (caret == firstVisible) {
          caret = 0;
        }

        else if (caret == firstOfLine) {
          caret = firstVisible;
        }

        else {
          caret = firstOfLine;
        }

        if (select) {
          textArea.select(textArea.getMarkPosition(), caret);
        }

        else {
          textArea.setCaretPosition(caret);
        }

      }


      private boolean select;
    }


    public static class insert_break implements ActionListener {
      public void actionPerformed(ActionEvent evt) {
        ACLTextArea textArea = getTextArea(evt);

        if (!textArea.isEditable()) {
          textArea.getToolkit().beep();
          return;
        }

        textArea.setSelectedText("\n");
      }
    }


    public static class insert_tab implements ActionListener {
      public void actionPerformed(ActionEvent evt) {
        ACLTextArea textArea = getTextArea(evt);

        if (!textArea.isEditable()) {
          textArea.getToolkit().beep();
          return;
        }

        textArea.overwriteSetSelectedText("\t");
      }
    }


    public static class next_char implements ActionListener {

      public next_char(boolean select) {
        this.select = select;
      }


      public void actionPerformed(ActionEvent evt) {
        ACLTextArea textArea = getTextArea(evt);
        int caret = textArea.getCaretPosition();
        if (caret == textArea.getDocumentLength()) {
          textArea.getToolkit().beep();
          return;
        }

        if (select) {
          textArea.select(textArea.getMarkPosition(),
            caret + 1);
        }

        else {
          textArea.setCaretPosition(caret + 1);
        }

      }


      private boolean select;
    }


    public static class next_line implements ActionListener {

      public next_line(boolean select) {
        this.select = select;
      }


      public void actionPerformed(ActionEvent evt) {
        ACLTextArea textArea = getTextArea(evt);
        int caret = textArea.getCaretPosition();
        int line = textArea.getCaretLine();

        if (line == textArea.getLineCount() - 1) {
          textArea.getToolkit().beep();
          return;
        }

        int magic = textArea.getMagicCaretPosition();
        if (magic == -1) {
          magic = textArea.offsetToX(line,
            caret - textArea.getLineStartOffset(line));
        }

        caret = textArea.getLineStartOffset(line + 1)
           + textArea.xToOffset(line + 1, magic);
        if (select) {
          textArea.select(textArea.getMarkPosition(), caret);
        }

        else {
          textArea.setCaretPosition(caret);
        }

        textArea.setMagicCaretPosition(magic);
      }


      private boolean select;
    }


    public static class next_page implements ActionListener {

      public next_page(boolean select) {
        this.select = select;
      }


      public void actionPerformed(ActionEvent evt) {
        ACLTextArea textArea = getTextArea(evt);
        int lineCount = textArea.getLineCount();
        int firstLine = textArea.getFirstLine();
        int visibleLines = textArea.getVisibleLines();
        int line = textArea.getCaretLine();

        firstLine += visibleLines;

        if (firstLine + visibleLines >= lineCount - 1) {
          firstLine = lineCount - visibleLines;
        }

        textArea.setFirstLine(firstLine);

        int caret = textArea.getLineStartOffset(
          Math.min(textArea.getLineCount() - 1,
          line + visibleLines));
        if (select) {
          textArea.select(textArea.getMarkPosition(), caret);
        }

        else {
          textArea.setCaretPosition(caret);
        }

      }


      private boolean select;
    }


    public static class next_word implements ActionListener {

      public next_word(boolean select) {
        this.select = select;
      }


      public void actionPerformed(ActionEvent evt) {
        ACLTextArea textArea = getTextArea(evt);
        int caret = textArea.getCaretPosition();
        int line = textArea.getCaretLine();
        int lineStart = textArea.getLineStartOffset(line);
        caret -= lineStart;

        String lineText = textArea.getLineText(textArea.getCaretLine());

        if (caret == lineText.length()) {
          if (lineStart + caret == textArea.getDocumentLength()) {
            textArea.getToolkit().beep();
            return;
          }
          caret++;
        }
        else {

          char ch = lineText.charAt(caret);

          String noWordSep = (String)textArea.getDocument()
            .getProperty("noWordSep");
          boolean selectNoLetter = (!Character.isLetterOrDigit(ch)
             && noWordSep.indexOf(ch) == -1);

          int wordEnd = lineText.length();
          for (int i = caret; i < lineText.length(); i++) {
            ch = lineText.charAt(i);
            if (selectNoLetter ^ (!Character.isLetterOrDigit(ch) &&
              noWordSep.indexOf(ch) == -1)) {
              wordEnd = i;
              break;
            }
          }
          caret = wordEnd;
        }

        if (select) {
          textArea.select(textArea.getMarkPosition(),
            lineStart + caret);
        }

        else {
          textArea.setCaretPosition(lineStart + caret);
        }

      }


      private boolean select;
    }


    public static class overwrite implements ActionListener {
      public void actionPerformed(ActionEvent evt) {
        ACLTextArea textArea = getTextArea(evt);
        textArea.setOverwriteEnabled(
          !textArea.isOverwriteEnabled());
      }
    }


    public static class prev_char implements ActionListener {

      public prev_char(boolean select) {
        this.select = select;
      }


      public void actionPerformed(ActionEvent evt) {
        ACLTextArea textArea = getTextArea(evt);
        int caret = textArea.getCaretPosition();
        if (caret == 0) {
          textArea.getToolkit().beep();
          return;
        }

        if (select) {
          textArea.select(textArea.getMarkPosition(),
            caret - 1);
        }

        else {
          textArea.setCaretPosition(caret - 1);
        }

      }


      private boolean select;
    }


    public static class prev_line implements ActionListener {

      public prev_line(boolean select) {
        this.select = select;
      }


      public void actionPerformed(ActionEvent evt) {
        ACLTextArea textArea = getTextArea(evt);
        int caret = textArea.getCaretPosition();
        int line = textArea.getCaretLine();

        if (line == 0) {
          textArea.getToolkit().beep();
          return;
        }

        int magic = textArea.getMagicCaretPosition();
        if (magic == -1) {
          magic = textArea.offsetToX(line,
            caret - textArea.getLineStartOffset(line));
        }

        caret = textArea.getLineStartOffset(line - 1)
           + textArea.xToOffset(line - 1, magic);
        if (select) {
          textArea.select(textArea.getMarkPosition(), caret);
        }

        else {
          textArea.setCaretPosition(caret);
        }

        textArea.setMagicCaretPosition(magic);
      }


      private boolean select;
    }


    public static class prev_page implements ActionListener {

      public prev_page(boolean select) {
        this.select = select;
      }


      public void actionPerformed(ActionEvent evt) {
        ACLTextArea textArea = getTextArea(evt);
        int firstLine = textArea.getFirstLine();
        int visibleLines = textArea.getVisibleLines();
        int line = textArea.getCaretLine();

        if (firstLine < visibleLines) {
          firstLine = visibleLines;
        }

        textArea.setFirstLine(firstLine - visibleLines);

        int caret = textArea.getLineStartOffset(
          Math.max(0, line - visibleLines));
        if (select) {
          textArea.select(textArea.getMarkPosition(), caret);
        }

        else {
          textArea.setCaretPosition(caret);
        }

      }


      private boolean select;
    }


    public static class prev_word implements ActionListener {

      public prev_word(boolean select) {
        this.select = select;
      }


      public void actionPerformed(ActionEvent evt) {
        ACLTextArea textArea = getTextArea(evt);
        int caret = textArea.getCaretPosition();
        int line = textArea.getCaretLine();
        int lineStart = textArea.getLineStartOffset(line);
        caret -= lineStart;

        String lineText = textArea.getLineText(textArea.getCaretLine());

        if (caret == 0) {
          if (lineStart == 0) {
            textArea.getToolkit().beep();
            return;
          }
          caret--;
        }
        else {
          char ch = lineText.charAt(caret - 1);

          String noWordSep = (String)textArea.getDocument()
            .getProperty("noWordSep");
          boolean selectNoLetter = (!Character.isLetterOrDigit(ch)
             && noWordSep.indexOf(ch) == -1);

          int wordStart = 0;
          for (int i = caret - 1; i >= 0; i--) {
            ch = lineText.charAt(i);
            if (selectNoLetter ^ (!Character.isLetterOrDigit(ch) &&
              noWordSep.indexOf(ch) == -1)) {
              wordStart = i + 1;
              break;
            }
          }
          caret = wordStart;
        }

        if (select) {
          textArea.select(textArea.getMarkPosition(),
            lineStart + caret);
        }

        else {
          textArea.setCaretPosition(lineStart + caret);
        }

      }


      private boolean select;
    }


    public final static ActionListener BACKSPACE = new backspace();
    public final static ActionListener DELETE = new delete();
    public final static ActionListener END = new end(false);
    public final static ActionListener SELECT_END = new end(true);
    public final static ActionListener INSERT_BREAK = new insert_break();
    public final static ActionListener INSERT_TAB = new insert_tab();
    public final static ActionListener HOME = new home(false);
    public final static ActionListener SELECT_HOME = new home(true);
    public final static ActionListener NEXT_CHAR = new next_char(false);
    public final static ActionListener NEXT_LINE = new next_line(false);
    public final static ActionListener NEXT_PAGE = new next_page(false);
    public final static ActionListener NEXT_WORD = new next_word(false);
    public final static ActionListener SELECT_NEXT_CHAR = new next_char(true);
    public final static ActionListener SELECT_NEXT_LINE = new next_line(true);
    public final static ActionListener SELECT_NEXT_PAGE = new next_page(true);
    public final static ActionListener SELECT_NEXT_WORD = new next_word(true);
    public final static ActionListener OVERWRITE = new overwrite();
    public final static ActionListener PREV_CHAR = new prev_char(false);
    public final static ActionListener PREV_LINE = new prev_line(false);
    public final static ActionListener PREV_PAGE = new prev_page(false);
    public final static ActionListener PREV_WORD = new prev_word(false);
    public final static ActionListener SELECT_PREV_CHAR = new prev_char(true);
    public final static ActionListener SELECT_PREV_LINE = new prev_line(true);
    public final static ActionListener SELECT_PREV_PAGE = new prev_page(true);
    public final static ActionListener SELECT_PREV_WORD = new prev_word(true);

    public final static ActionListener[] ACTIONS = {
      BACKSPACE, DELETE, END, SELECT_END, INSERT_BREAK,
      INSERT_TAB, HOME, SELECT_HOME, NEXT_CHAR, NEXT_LINE,
      NEXT_PAGE, NEXT_WORD, SELECT_NEXT_CHAR, SELECT_NEXT_LINE,
      SELECT_NEXT_PAGE, SELECT_NEXT_WORD, OVERWRITE, PREV_CHAR,
      PREV_LINE, PREV_PAGE, PREV_WORD, SELECT_PREV_CHAR,
      SELECT_PREV_LINE, SELECT_PREV_PAGE, SELECT_PREV_WORD};

    public final static String[] ACTION_NAMES = {
      "backspace", "delete", "end", "select-end", "insert-break",
      "insert-tab", "home", "select-home", "next-char", "next-line",
      "next-page", "next-word", "select-next-char", "select-next-line",
      "select-next-page", "select-next-word", "overwrite", "prev-char",
      "prev-line", "prev-page", "prev-word", "select-prev-char",
      "select-prev-line", "select-prev-page", "select-prev-word"};

    // private members
    private Hashtable bindings;
    private Hashtable currentBindings;
  }


  class ScrollLayout implements LayoutManager {
    public void addLayoutComponent(String name, Component comp) {
      if (name.equals(CENTER)) {
        center = comp;
      }

      else if (name.equals(RIGHT)) {
        right = comp;
      }

      else if (name.equals(BOTTOM)) {
        bottom = comp;
      }

    }


    public void removeLayoutComponent(Component comp) {
      if (center == comp) {
        center = null;
      }

      if (right == comp) {
        right = null;
      }

      if (bottom == comp) {
        bottom = null;
      }

    }


    public Dimension preferredLayoutSize(Container parent) {
      Dimension dim = new Dimension();
      Insets insets = getInsets();
      dim.width = insets.left + insets.right;
      dim.height = insets.top + insets.bottom;

      Dimension centerPref = center.getPreferredSize();
      dim.height += centerPref.height;
      dim.width += centerPref.width;
      Dimension rightPref = right.getPreferredSize();
      dim.width += rightPref.width;
      /*
          Dimension bottomPref = bottom.getPreferredSize();
          dim.height += bottomPref.height;
        */
      return dim;
    }


    public Dimension minimumLayoutSize(Container parent) {
      Dimension dim = new Dimension();
      Insets insets = getInsets();
      dim.width = insets.left + insets.right;
      dim.height = insets.top + insets.bottom;

      Dimension centerPref = center.getMinimumSize();
      dim.height += centerPref.height;
      dim.width += centerPref.width;
      Dimension rightPref = right.getMinimumSize();
      dim.width += rightPref.width;
      Dimension bottomPref = bottom.getMinimumSize();
      dim.height += bottomPref.height;

      return dim;
    }


    public void layoutContainer(Container parent) {
      Dimension size = parent.getSize();
      Insets insets = getInsets();
      int itop = insets.top;
      int ileft = insets.left;
      int ibottom = insets.bottom;
      int iright = insets.right;

      int rightWidth = right.getPreferredSize().width;
      int bottomHeight = center.getPreferredSize().height;

      center.setBounds(
        ileft,
        itop,
        size.width - rightWidth - ileft - iright,
        size.height - bottomHeight - itop - ibottom);
      right.setBounds(
        size.width - rightWidth - iright,
        itop,
        rightWidth,
        size.height - bottomHeight - itop - ibottom);
      /*
          bottom.setBounds(
          ileft,
          size.height - bottomHeight - ibottom,
          size.width - rightWidth - ileft - iright,
          bottomHeight);
        */
    }

    // private members
    private Component center;
    private Component right;
    private Component bottom;
  }


  class AutoScroll implements ActionListener, MouseMotionListener {

    public void actionPerformed(ActionEvent evt) {
      select(getMarkPosition(), xyToOffset(x, y));
    }


    public void mouseDragged(MouseEvent evt) {
      if (popup != null && popup.isVisible()) {
        return;
      }

      x = evt.getX();
      y = evt.getY();
      if (!scrollTimer.isRunning()) {
        scrollTimer.start();
      }

    }


    public void mouseMoved(MouseEvent evt) { }


    private int x, y;
  }


  class MutableCaretEvent extends CaretEvent {
    MutableCaretEvent() {
      super(ACLTextArea.this);
    }


    public int getDot() {
      return getCaretPosition();
    }


    public int getMark() {
      return getMarkPosition();
    }
  }


  class AdjustHandler implements AdjustmentListener {
    public void adjustmentValueChanged(AdjustmentEvent evt) {
      if (!scrollBarsInitialized) {
        return;
      }

      if (evt.getAdjustable() == vertical) {
        setFirstLine(vertical.getValue());
      }

      else {
        setHorizontalOffset(-horizontal.getValue());
      }

    }
  }


  class ComponentHandler extends ComponentAdapter {
    public void componentResized(ComponentEvent evt) {
      recalculateVisibleLines();
      scrollBarsInitialized = true;
    }
  }


  class DocumentHandler implements DocumentListener {
    public void insertUpdate(DocumentEvent evt) {
      documentChanged(evt);

      int offset = evt.getOffset();
      int length = evt.getLength();

      boolean repaint = true;

      int newStart;
      int newEnd;

      if (selectionStart >= offset) {
        newStart = selectionStart + length;
        repaint = false;
      }
      else {
        newStart = selectionStart;
      }

      if (selectionEnd >= offset) {
        newEnd = selectionEnd + length;
        repaint = false;
      }
      else {
        newEnd = selectionEnd;
      }

      select(newStart, newEnd);

      if (repaint) {
        painter.fastRepaint();
      }

    }


    public void removeUpdate(DocumentEvent evt) {
      documentChanged(evt);

      int offset = evt.getOffset();
      int length = evt.getLength();

      boolean repaint = true;

      int newStart;
      int newEnd;

      if (selectionStart > offset) {
        if (selectionStart > offset + length) {
          newStart = selectionStart - length;
        }

        else {
          newStart = offset;
        }

        repaint = false;
      }
      else {
        newStart = selectionStart;
      }

      if (selectionEnd > offset) {
        if (selectionEnd > offset + length) {
          newEnd = selectionEnd - length;
        }

        else {
          newEnd = offset;
        }

        repaint = false;
      }
      else {
        newEnd = selectionEnd;
      }

      select(newStart, newEnd);

      if (repaint) {
        painter.fastRepaint();
      }

    }


    public void changedUpdate(DocumentEvent evt) {
    }
  }


  class FocusHandler implements FocusListener {
    public void focusGained(FocusEvent evt) {
      setCaretVisible(true);
      focusedComponent = ACLTextArea.this;
    }


    public void focusLost(FocusEvent evt) {
      setCaretVisible(false);
      focusedComponent = null;
    }
  }


  class MouseHandler extends MouseAdapter {
    public void mousePressed(MouseEvent evt) {
      requestFocus();

      if ((evt.getModifiers() & InputEvent.BUTTON3_MASK) != 0
         && popup != null) {
        popup.show(painter, evt.getX(), evt.getY());
        return;
      }

      int line = yToLine(evt.getY());
      int offset = xToOffset(line, evt.getX());
      int dot = getLineStartOffset(line) + offset;

      switch (evt.getClickCount()) {
        case 1:
          doSingleClick(evt, line, offset, dot);
          break;
        case 2:
          // It uses the bracket matching stuff, so
          // it can throw a BLE
          try {
            doDoubleClick(evt, line, offset, dot);
          }
          catch (BadLocationException bl) {
            bl.printStackTrace();
          }
          break;
        case 3:
          doTripleClick(evt, line, offset, dot);
          break;
      }
    }


    public void mouseReleased(MouseEvent evt) {
      if (scrollTimer.isRunning()) {
        scrollTimer.stop();
      }

    }


    private void doSingleClick(MouseEvent evt, int line,
                               int offset, int dot) {
      if ((evt.getModifiers() & InputEvent.SHIFT_MASK) != 0) {
        setSelectionEnd(dot);
      }

      else {
        setCaretPosition(dot);
      }

    }


    private void doDoubleClick(MouseEvent evt, int line,
                               int offset, int dot) throws BadLocationException {
      // Ignore empty lines
      if (getLineLength(line) == 0) {
        return;
      }

      try {
        int bracket = TextUtilities.findMatchingBracket(
          document, Math.max(0, dot - 1));
        if (bracket != -1) {
          int mark = getMarkPosition();
          // Hack
          if (bracket > mark) {
            bracket++;
            mark--;
          }
          select(mark, bracket);
          return;
        }
      }
      catch (BadLocationException bl) {
        bl.printStackTrace();
      }

      // Ok, it's not a bracket... select the word
      String lineText = getLineText(line);
      char ch = lineText.charAt(offset - 1);

      String noWordSep = (String)document.getProperty("noWordSep");
      if (noWordSep == null) {
        noWordSep = "";
      }

      // If the user clicked on a non-letter char,
      // we select the surrounding non-letters
      boolean selectNoLetter = (!Character.isLetterOrDigit(ch)
         && noWordSep.indexOf(ch) == -1);

      int wordStart = 0;

      for (int i = offset - 1; i >= 0; i--) {
        ch = lineText.charAt(i);
        if (selectNoLetter ^ (!Character.isLetterOrDigit(ch) &&
          noWordSep.indexOf(ch) == -1)) {
          wordStart = i + 1;
          break;
        }
      }

      int wordEnd = lineText.length();
      for (int i = offset; i < lineText.length(); i++) {
        ch = lineText.charAt(i);
        if (selectNoLetter ^ (!Character.isLetterOrDigit(ch) &&
          noWordSep.indexOf(ch) == -1)) {
          wordEnd = i;
          break;
        }
      }

      int lineStart = getLineStartOffset(line);
      select(lineStart + wordStart, lineStart + wordEnd);
    }


    private void doTripleClick(MouseEvent evt, int line,
                               int offset, int dot) {
      select(getLineStartOffset(line), getLineEndOffset(line) - 1);
    }
  }


  // protected members
  protected static String CENTER = "center";
  protected static String RIGHT = "right";
  protected static String BOTTOM = "bottom";

  protected static ACLTextArea focusedComponent;
  protected static Timer caretTimer;

  protected ACLTextAreaPainter painter;

  protected JPopupMenu popup;

  protected Timer scrollTimer;

  protected EventListenerList listenerList;
  protected MutableCaretEvent caretEvent;

  protected boolean caretBlinks;
  protected boolean caretVisible;
  protected boolean blink;

  protected boolean editable;

  protected int firstLine;
  protected int visibleLines;
  protected int electricScroll;

  protected int horizontalOffset;

  protected JScrollBar vertical;
  protected JScrollBar horizontal;
  protected boolean scrollBarsInitialized;

  protected InputHandler inputHandler;
  protected ACLSyntaxDocument document;
  protected DocumentHandler documentHandler;

  protected Segment lineSegment;

  protected int selectionStart;
  protected int selectionStartLine;
  protected int selectionEnd;
  protected int selectionEndLine;
  protected boolean biasLeft;

  protected int bracketPosition;
  protected int bracketLine;

  protected int magicCaret;
  protected boolean overwrite;

  private String contentLanguage = "";

  private ACLMessage msg;
  private String fieldName;

}
//  ***EOF***
