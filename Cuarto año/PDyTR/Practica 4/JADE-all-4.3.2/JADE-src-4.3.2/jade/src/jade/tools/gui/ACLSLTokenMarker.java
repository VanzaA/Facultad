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

import javax.swing.text.Segment;
import jade.domain.FIPAAgentManagement.FIPAManagementOntology;


/**
 *  ACL/SL token marker. The original file is written by Slava Pestov
 *  (www.gjt.org) and altered to fit ACL/SL.
 *
 * @author     Chris van Aart - Acklin B.V., the Netherlands & Slava Pestov
 * @created    June 8, 2002
 */

public class ACLSLTokenMarker {

  public ACLSLTokenMarker() {
    this(getKeywords());
    lastLine = -1;
  }


  public ACLSLTokenMarker(KeywordMap keywords) {
    lastLine = -1;
    this.keywords = keywords;
  }


  private static KeywordMap getKeywords() {
    if (aclSLKeywords == null) {
      aclSLKeywords = new KeywordMap(false);

      /* FIXME Need new method to get a vocabulary from an ontology
      jade.util.leap.List agentMgt = FIPAAgentManagementOntology.instance().getVocabulary();
      for (int i = 0; i < agentMgt.size(); i++) {
        String roleName = (String)agentMgt.get(i);
        aclSLKeywords.add(roleName, ACLToken.KEYWORD2);
        try {
          SlotDescriptor[] slots = FIPAAgentManagementOntology.instance().getSlots(roleName);
          for (int j = 0; j < slots.length; j++) {
            aclSLKeywords.add(slots[j].getName(), ACLToken.LABEL);
          }
        }
        catch (Exception ex) {
        }
      } */

      aclSLKeywords.add("inv", ACLToken.INVALID);
      aclSLKeywords.add("ams", ACLToken.LABEL);
      aclSLKeywords.add("df", ACLToken.LABEL);
      aclSLKeywords.add("rma", ACLToken.LABEL);
      aclSLKeywords.add("l1", ACLToken.LITERAL1);
      aclSLKeywords.add("l2", ACLToken.LITERAL2);

	  /* FIXME Need new method to get a vocabulary from an ontology
      jade.util.leap.List li = BasicOntology.instance().getVocabulary();
      for (int i = 0; i < li.size(); i++) {
        aclSLKeywords.add((String)li.get(i), ACLToken.OPERATOR);
      } */

      aclSLKeywords.add("wantarray", ACLToken.KEYWORD3);
      aclSLKeywords.add("warn", ACLToken.KEYWORD3);
      aclSLKeywords.add("write", ACLToken.KEYWORD3);

      aclSLKeywords.add("m", S_ONE);
      aclSLKeywords.add("q", S_ONE);
      aclSLKeywords.add("qq", S_ONE);
      aclSLKeywords.add("qw", S_ONE);
      aclSLKeywords.add("qx", S_ONE);
      aclSLKeywords.add("s", S_TWO);
      aclSLKeywords.add("tr", S_TWO);
      aclSLKeywords.add("y", S_TWO);
    }
    return aclSLKeywords;
  }


  /**
   *  Returns true if the next line should be repainted. This will return true
   *  after a line has been tokenized that starts a multiline token that
   *  continues onto the next line.
   *
   * @return    The NextLineRequested value
   */
  public boolean isNextLineRequested() {
    return nextLineRequested;
  }


  public ACLToken markTokens(Segment line, int lineIndex) {
    if (lineIndex >= length) {
      throw new IllegalArgumentException("Tokenizing invalid line: "
         + lineIndex);
    }

    lastToken = null;

    LineInfo info = lineInfo[lineIndex];
    LineInfo prev;
    if (lineIndex == 0) {
      prev = null;
    }

    else {
      prev = lineInfo[lineIndex - 1];
    }

    byte oldToken = info.token;
    byte token = markTokensImpl(prev == null ? ACLToken.NULL : prev.token, line, lineIndex);

    info.token = token;

    /*
        This is a foul hack. It stops nextLineRequested
        from being cleared if the same line is marked twice.
        Why is this necessary? It's all JEditTextArea's fault.
        When something is inserted into the text, firing a
        document event, the insertUpdate() method shifts the
        caret (if necessary) by the amount inserted.
        All caret movement is handled by the select() method,
        which eventually pipes the new position to scrollTo()
        and calls repaint().
        Note that at this point in time, the new line hasn't
        yet been painted; the caret is moved first.
        scrollTo() calls offsetToX(), which tokenizes the line
        unless it is being called on the last line painted
        (in which case it uses the text area's painter cached
        token list). What scrollTo() does next is irrelevant.
        After scrollTo() has done it's job, repaint() is
        called, and eventually we end up in paintLine(), whose
        job is to paint the changed line. It, too, calls
        markTokens().
        The problem was that if the line started a multiline
        token, the first markTokens() (done in offsetToX())
        would set nextLineRequested (because the line end
        token had changed) but the second would clear it
        (because the line was the same that time) and therefore
        paintLine() would never know that it needed to repaint
        subsequent lines.
        This bug took me ages to track down, that's why I wrote
        all the relevant info down so that others wouldn't
        duplicate it.
      */
    if (!(lastLine == lineIndex && nextLineRequested)) {
      nextLineRequested = (oldToken != token);
    }

    lastLine = lineIndex;

    addToken(0, ACLToken.END);

    return firstToken;
  }


  /**
   *  Returns if the token marker supports tokens that span multiple lines. If
   *  this is true, the object using this token marker is required to pass all
   *  lines in the document to the <code>markTokens()</code> method (in turn).
   *  <p>
   *
   *  The default implementation returns true; it should be overridden to
   *  return false on simpler token markers for increased speed.
   *
   * @return    Description of the Returned Value
   */
  public boolean supportsMultilineTokens() {
    return true;
  }


  /**
   *  Informs the token marker that lines have been inserted into the
   *  document. This inserts a gap in the <code>lineInfo</code> array.
   *
   * @param  index  The first line number
   * @param  lines  The number of lines
   */
  public void insertLines(int index, int lines) {
    if (lines <= 0) {
      return;
    }
    length += lines;
    ensureCapacity(length);
    int len = index + lines;
    System.arraycopy(lineInfo, index, lineInfo, len,
      lineInfo.length - len);

    for (int i = index + lines - 1; i >= index; i--) {
      lineInfo[i] = new LineInfo();
    }

  }


  /**
   *  Informs the token marker that line have been deleted from the document.
   *  This removes the lines in question from the <code>lineInfo</code> array.
   *
   * @param  index  The first line number
   * @param  lines  The number of lines
   */
  public void deleteLines(int index, int lines) {
    if (lines <= 0) {
      return;
    }
    int len = index + lines;
    length -= lines;
    System.arraycopy(lineInfo, len, lineInfo,
      index, lineInfo.length - len);
  }


  public byte markTokensImpl(byte _token, Segment line, int lineIndex) {
    char[] array = line.array;
    int offset = line.offset;
    token = _token;
    lastOffset = offset;
    lastKeyword = offset;
    matchChar = '\0';
    matchCharBracket = false;
    matchSpacesAllowed = false;
    int length = line.count + offset;

    if (token == ACLToken.LITERAL1 && lineIndex != 0
       && lineInfo[lineIndex - 1].obj != null) {
      String str = (String)lineInfo[lineIndex - 1].obj;
      if (str != null && str.length() == line.count
         && ACLSyntaxUtilities.regionMatches(false, line,
        offset, str)) {
        addToken(line.count, token);
        return ACLToken.NULL;
      }
      else {
        addToken(line.count, token);
        lineInfo[lineIndex].obj = str;
        return token;
      }
    }

    boolean backslash = false;
    loop :
    for (int i = offset; i < length; i++) {
      int i1 = (i + 1);

      char c = array[i];
      if (c == '\\') {
        backslash = !backslash;
        continue;
      }

      switch (token) {
        case ACLToken.NULL:
          switch (c) {
            case '#':
              if (backslash) {
                backslash = false;
              }

              else {
                if (doKeyword(line, i, c)) {
                  break;
                }
                addToken(i - lastOffset, token);
                addToken(length - i, ACLToken.COMMENT1);
                lastOffset = lastKeyword = length;
                break loop;
              }
              break;
            case '.':
            case '@':

              backslash = false;
              if (length - i > 1) {
                if (doKeyword(line, i, c)) {
                  break;
                }
                else {

                  addToken(i - lastOffset, token);
                  lastOffset = lastKeyword = i;
                  token = ACLToken.KEYWORD2;
                }
              }

              break;
            /*
                case '.':
                if (backslash)
                backslash = false;
                else
                {
                if (doKeyword(line, i, c))
                break;
                addToken(i - lastOffset, token);
                addToken(length - i, Token.OPERATOR);
                lastOffset = lastKeyword = length;
                break loop;
                }
                break;
              */
            /*
                /            case '.':
                /            case ':':
                /            case '/':
                if (backslash) {
                backslash = false;
                }
                else {
                if (doKeyword(line, i, c)) {
                break;
                }
                token = ACLToken.KEYWORD2;
                addToken(i - lastOffset, token);
                addToken(length - i, ACLToken.KEYWORD2);
                lastOffset = lastKeyword = length;
                /      break loop;
                token = ACLToken.NULL;
                }
                break;
              */
            /*
                case '@':
                if (backslash)
                {
                backslash = false;
                }
                else
                {
                if (doKeyword(line, i, c))
                {
                break;
                }
                addToken(length - i, Token.LITERAL1);
                lastOffset = lastKeyword = i;
                break loop;
                }
                break;
              */
            case '=':
              backslash = false;
              if (i == offset) {
                token = ACLToken.COMMENT2;
                addToken(length - i, token);
                lastOffset = lastKeyword = length;
                break loop;
              }
              else {
                doKeyword(line, i, c);
              }

              break;
            case '$':
            case '&':
            case '%':
              backslash = false;
              if (length - i > 1) {
                if (doKeyword(line, i, c)) {
                  break;
                }
                if (c == '&' && (array[i1] == '&'
                   || Character.isWhitespace(
                  array[i1]))) {
                  i++;
                }

                else {
                  addToken(i - lastOffset, token);
                  lastOffset = lastKeyword = i;
                  token = ACLToken.KEYWORD2;
                }
              }
              break;
            case '"':
              if (backslash) {
                backslash = false;
              }

              else {
                if (doKeyword(line, i, c)) {
                  break;
                }
                addToken(i - lastOffset, token);
                token = ACLToken.LITERAL1;
                lineInfo[lineIndex].obj = null;
                lastOffset = lastKeyword = i;
              }
              break;
            case '\'':
              if (backslash) {
                backslash = false;
              }

              else {
                int oldLastKeyword = lastKeyword;
                if (doKeyword(line, i, c)) {
                  break;
                }
                if (i != oldLastKeyword) {
                  break;
                }
                addToken(i - lastOffset, token);
                token = ACLToken.LITERAL2;
                lastOffset = lastKeyword = i;
              }
              break;
            case '`':
              if (backslash) {
                backslash = false;
              }

              else {
                if (doKeyword(line, i, c)) {
                  break;
                }
                addToken(i - lastOffset, token);
                token = ACLToken.OPERATOR;
                lastOffset = lastKeyword = i;
              }
              break;
            case '<':
              if (backslash) {
                backslash = false;
              }

              else {
                if (doKeyword(line, i, c)) {
                  break;
                }
                if (length - i > 2 && array[i1] == '<'
                   && !Character.isWhitespace(array[i + 2])) {
                  addToken(i - lastOffset, token);
                  lastOffset = lastKeyword = i;
                  token = ACLToken.LITERAL1;
                  int len = length - (i + 2);
                  if (array[length - 1] == ';') {
                    len--;
                  }

                  lineInfo[lineIndex].obj =
                    new String(array, i + 2, len);
                }
              }
              break;
            // case '.':
            /*
                case '@':
                backslash = false;
                if (doKeyword(line, i, c))
                break;
                if (i == lastKeyword)
                break;
                addToken(i1 - lastOffset, Token.KEYWORD1);
                lastOffset = lastKeyword = i1;
                token = Token.KEYWORD2;
                break;// loop;
              */
            /*
                case '-':
                backslash = false;
                if (doKeyword(line, i, c))
                {
                break;
                }
                if (i != lastKeyword || length - i <= 1)
                {
                break;
                }
                switch (array[i1])
                {
                case 'r':
                case 'w':
                case 'x':
                case 'o':
                case 'R':
                case 'W':
                case 'X':
                case 'O':
                case9 'e':
                case 'z':
                case 's':
                case 'f':
                case 'd':
                case 'l':
                case 'p':
                case 'S':
                case 'b':
                case 'c':
                case 't':
                case 'u':
                case 'g':
                case 'k':
                case 'T':
                case 'B':
                case 'M':
                case 'A':
                case 'C':
                addToken(i - lastOffset, token);
                addToken(2, Token.KEYWORD3);
                lastOffset = lastKeyword = i + 2;
                i++;
                }
                break;
              */
            //      case '/':
            case '?':
              if (i == lastKeyword && length - i > 1) {
                if (doKeyword(line, i, c)) {
                  break;
                }
                backslash = false;
                char ch = array[i1];
                if (Character.isWhitespace(ch)) {
                  break;
                }
                matchChar = c;
                matchSpacesAllowed = false;
                addToken(i - lastOffset, token);
                token = S_ONE;
                lastOffset = lastKeyword = i;
              }
              break;
            default:
              backslash = false;
              if (!Character.isLetterOrDigit(c)
                 && c != '_' && c != '-') {
                doKeyword(line, i, c);
              }

              break;
          }
          break;
        case ACLToken.KEYWORD2:
          backslash = false;
          if (!Character.isLetterOrDigit(c) && c != '_'
             && c != '#' && c != '\'' && c != '-' && c != '.') {
            if (i != offset && array[i - 1] == '$') {
              addToken(i1 - lastOffset, token);
              lastOffset = lastKeyword = i1;
              break;
            }
            else {
              addToken(i - lastOffset, token);
              lastOffset = lastKeyword = i;
            }
            token = ACLToken.NULL;
          }
          break;
        case S_ONE:
        case S_TWO:
          if (backslash) {
            backslash = false;
          }

          else
            if (matchChar == '\0') {
            if (Character.isWhitespace(matchChar)
               && !matchSpacesAllowed) {
              break;
            }
            else {
              matchChar = c;
            }
          }

          else {
            switch (matchChar) {
              case '(':
                matchChar = ')';
                matchCharBracket = true;
                break;
              case '[':
                matchChar = ']';
                matchCharBracket = true;
                break;
              case '{':
                matchChar = '}';
                matchCharBracket = true;
                break;
              case '<':
                matchChar = '>';
                matchCharBracket = true;
                break;
              default:
                matchCharBracket = false;
                break;
            }
            if (c != matchChar) {
              break;
            }
            if (token == S_TWO) {
              token = S_ONE;
              if (matchCharBracket) {
                matchChar = '\0';
              }

            }
            else {
              token = S_END;
              addToken(i1 - lastOffset, ACLToken.LITERAL2);
              lastOffset = lastKeyword = i1;
            }
          }

          break;
        case S_END:
          backslash = false;
          if (!Character.isLetterOrDigit(c)
             && c != '_' && c != '-') {
            doKeyword(line, i, c);
          }

          break;
        case ACLToken.COMMENT2:
          backslash = false;
          if (i == offset) {
            addToken(line.count, token);
            if (length - i > 3 && ACLSyntaxUtilities.regionMatches(false, line, offset, "=cut")) {
              token = ACLToken.NULL;
            }

            lastOffset = lastKeyword = length;
            break loop;
          }
          break;
        case ACLToken.LITERAL1:
          if (backslash) {
            backslash = false;
          }

          else if (c == '$') {
            backslash = true;
          }

          else if (c == '"') {
            addToken(i1 - lastOffset, token);
            token = ACLToken.NULL;
            lastOffset = lastKeyword = i1;
          }
          break;
        case ACLToken.LITERAL2:
          if (backslash) {
            backslash = false;
          }

          else if (c == '$') {
            backslash = true;
          }

          else if (c == '\'') {
            addToken(i1 - lastOffset, ACLToken.LITERAL1);
            token = ACLToken.NULL;
            lastOffset = lastKeyword = i1;
          }
          break;
        case ACLToken.OPERATOR:
          if (backslash) {
            backslash = false;
          }

          else if (c == '`') {
            addToken(i1 - lastOffset, token);
            token = ACLToken.NULL;
            lastOffset = lastKeyword = i1;
          }
          break;
        default:
          throw new InternalError("Invalid state: "
             + token);
      }
    }

    if (token == ACLToken.NULL) {
      doKeyword(line, length, '\0');
    }

    switch (token) {
      case ACLToken.NULL:
        addToken(length - lastOffset, token);
        break;
      case ACLToken.KEYWORD2:
        addToken(length - lastOffset, token);
        token = ACLToken.NULL;
        break;
      case S_END:
        addToken(length - lastOffset, ACLToken.LITERAL2);
        token = ACLToken.NULL;
        break;
      case S_ONE:
      case S_TWO:
        addToken(length - lastOffset, ACLToken.INVALID);// XXX
        token = ACLToken.NULL;
        break;
      default:
        addToken(length - lastOffset, token);
        break;
    }
    return token;
  }


  /**
   *  Creates a new <code>TokenMarker</code>. This DOES NOT create a lineInfo
   *  array; an initial call to <code>insertLines()</code> does that.
   *
   * @param  index  Description of Parameter
   */

  /**
   *  Creates a new <code>TokenMarker</code>. This DOES NOT create a lineInfo
   *  array; an initial call to <code>insertLines()</code> does that.
   *
   *  Creates a new <code>TokenMarker</code>. This DOES NOT create a lineInfo
   *  array; an initial call to <code>insertLines()</code> does that. Creates
   *  a new <code>TokenMarker</code>. This DOES NOT create a lineInfo array;
   *  an initial call to <code>insertLines()</code> does that. Creates a new
   *  <code>TokenMarker</code>. This DOES NOT create a lineInfo array; an
   *  initial call to <code>insertLines()</code> does that. Creates a new
   *  <code>TokenMarker</code>. This DOES NOT create a lineInfo array; an
   *  initial call to <code>insertLines()</code> does that. Creates a new
   *  <code>TokenMarker</code>. This DOES NOT create a lineInfo array; an
   *  initial call to <code>insertLines()</code> does that. Creates a new
   *  <code>TokenMarker</code>. This DOES NOT create a lineInfo array; an
   *  initial call to <code>insertLines()</code> does that. Creates a new
   *  <code>TokenMarker</code>. This DOES NOT create a lineInfo array; an
   *  initial call to <code>insertLines()</code> does that. Creates a new
   *  <code>TokenMarker</code>. This DOES NOT create a lineInfo array; an
   *  initial call to <code>insertLines()</code> does that. Creates a new
   *  <code>TokenMarker</code>. This DOES NOT create a lineInfo array; an
   *  initial call to <code>insertLines()</code> does that. Creates a new
   *  <code>TokenMarker</code>. This DOES NOT create a lineInfo array; an
   *  initial call to <code>insertLines()</code> does that. Ensures that the
   *  <code>lineInfo</code> array can contain the specified index. This
   *  enlarges it if necessary. No action is taken if the array is large
   *  enough already.<p>
   *
   *  It should be unnecessary to call this under normal circumstances; <code>insertLine()</code>
   *  should take care of enlarging the line info array automatically.
   *
   * @param  index  Description of Parameter
   */
  protected void ensureCapacity(int index) {
    if (lineInfo == null) {
      lineInfo = new LineInfo[index + 1];
    }

    else if (lineInfo.length <= index) {
      LineInfo[] lineInfoN = new LineInfo[(index + 1) * 2];
      System.arraycopy(lineInfo, 0, lineInfoN, 0,
        lineInfo.length);
      lineInfo = lineInfoN;
    }
  }


  /**
   *  Adds a token to the token list.
   *
   * @param  length  The length of the token
   * @param  id      The id of the token
   */
  protected void addToken(int length, byte id) {
    if (id >= ACLToken.INTERNAL_FIRST && id <= ACLToken.INTERNAL_LAST) {
      throw new InternalError("Invalid id: " + id);
    }

    if (length == 0 && id != ACLToken.END) {
      return;
    }

    if (firstToken == null) {
      firstToken = new ACLToken(length, id);
      lastToken = firstToken;
    }
    else if (lastToken == null) {
      lastToken = firstToken;
      firstToken.length = length;
      firstToken.id = id;
    }
    else if (lastToken.next == null) {
      lastToken.next = new ACLToken(length, id);
      lastToken = lastToken.next;
    }
    else {
      lastToken = lastToken.next;
      lastToken.length = length;
      lastToken.id = id;
    }
  }


  private boolean doKeyword(Segment line, int i, char c) {
    int i1 = i + 1;

    if (token == S_END) {
      addToken(i - lastOffset, ACLToken.LITERAL2);
      token = ACLToken.NULL;
      lastOffset = i;
      lastKeyword = i1;
      return false;
    }

    int len = i - lastKeyword;
    byte id = keywords.lookup(line, lastKeyword, len);
    if (id == S_ONE || id == S_TWO) {
      if (lastKeyword != lastOffset) {
        addToken(lastKeyword - lastOffset, ACLToken.NULL);
      }

      addToken(len, ACLToken.LITERAL2);
      lastOffset = i;
      lastKeyword = i1;
      if (Character.isWhitespace(c)) {
        matchChar = '\0';
      }

      else {
        matchChar = c;
      }

      matchSpacesAllowed = true;
      token = id;
      return true;
    }
    else if (id != ACLToken.NULL) {
      if (lastKeyword != lastOffset) {
        addToken(lastKeyword - lastOffset, ACLToken.NULL);
      }

      addToken(len, id);
      lastOffset = i;
    }
    lastKeyword = i1;
    return false;
  }


//  ***EOF***
  private static class KeywordMap {
    /**
     *  Creates a new <code>KeywordMap</code>.
     *
     * @param  ignoreCase  True if keys are case insensitive
     */
    public KeywordMap(boolean ignoreCase) {
      this(ignoreCase, 52);
      this.ignoreCase = ignoreCase;
    }


    /**
     *  Creates a new <code>KeywordMap</code>.
     *
     * @param  ignoreCase  True if the keys are case insensitive
     * @param  mapLength   The number of `buckets' to create. A value of 52
     *      will give good performance for most maps.
     */
    public KeywordMap(boolean ignoreCase, int mapLength) {
      this.mapLength = mapLength;
      this.ignoreCase = ignoreCase;
      map = new Keyword[mapLength];
    }


    /**
     *  Returns true if the keyword map is set to be case insensitive, false
     *  otherwise.
     *
     * @return    The IgnoreCase value
     */
    public boolean getIgnoreCase() {
      return ignoreCase;
    }


    /**
     *  Sets if the keyword map should be case insensitive.
     *
     * @param  ignoreCase  True if the keyword map should be case insensitive,
     *      false otherwise
     */
    public void setIgnoreCase(boolean ignoreCase) {
      this.ignoreCase = ignoreCase;
    }


    /**
     *  Looks up a key.
     *
     * @param  text    The text segment
     * @param  offset  The offset of the substring within the text segment
     * @param  length  The length of the substring
     * @return         Description of the Returned Value
     */
    public byte lookup(Segment text, int offset, int length) {
      if (length == 0) {
        return ACLToken.NULL;
      }
      Keyword k = map[getSegmentMapKey(text, offset, length)];
      while (k != null) {
        if (length != k.keyword.length) {
          k = k.next;
          continue;
        }
        if (ACLSyntaxUtilities.regionMatches(ignoreCase, text, offset,
          k.keyword)) {
          return k.id;
        }
        k = k.next;
      }
      return ACLToken.NULL;
    }


    /**
     *  Adds a key-value mapping.
     *
     * @param  keyword  The key
     * @param  id       Description of Parameter
     * @Param           id The value
     */
    public void add(String keyword, byte id) {
      int key = getStringMapKey(keyword);
      map[key] = new Keyword(keyword.toCharArray(), id, map[key]);
    }


    protected int getStringMapKey(String s) {
      return (Character.toUpperCase(s.charAt(0)) +
        Character.toUpperCase(s.charAt(s.length() - 1)))
         % mapLength;
    }


    protected int getSegmentMapKey(Segment s, int off, int len) {
      return (Character.toUpperCase(s.array[off]) +
        Character.toUpperCase(s.array[off + len - 1]))
         % mapLength;
    }

    // private members
    class Keyword {
      public Keyword(char[] keyword, byte id, Keyword next) {
        this.keyword = keyword;
        this.id = id;
        this.next = next;
      }


      public char[] keyword;
      public byte id;
      public Keyword next;
    }

    // protected members
    protected int mapLength;

    private Keyword[] map;
    private boolean ignoreCase;
  }


  /**
   *  Inner class for storing information about tokenized lines.
   *
   * @author     chris
   * @created    June 8, 2002
   */
  public class LineInfo {
    /**
     *  Creates a new LineInfo object with token = Token.NULL and obj = null.
     */
    public LineInfo() { }


    /**
     *  Creates a new LineInfo object with the specified parameters.
     *
     * @param  token  Description of Parameter
     * @param  obj    Description of Parameter
     */
    public LineInfo(byte token, Object obj) {
      this.token = token;
      this.obj = obj;
    }


    /**
     *  The id of the last token of the line.
     */
    public byte token;

    /**
     *  This is for use by the token marker implementations themselves. It can
     *  be used to store anything that is an object and that needs to exist on
     *  a per-line basis.
     */
    public Object obj;
  }


  // public members
  public final static byte S_ONE = ACLToken.INTERNAL_FIRST;
  public final static byte S_TWO = (byte)(ACLToken.INTERNAL_FIRST + 1);
  public final static byte S_END = (byte)(ACLToken.INTERNAL_FIRST + 2);

  private static KeywordMap aclSLKeywords;

  // protected members

  /**
   *  The first token in the list. This should be used as the return value
   *  from <code>markTokens()</code>.
   */
  protected ACLToken firstToken;

  /**
   *  The last token in the list. New tokens are added here. This should be
   *  set to null before a new line is to be tokenized.
   */
  protected ACLToken lastToken;

  /**
   *  An array for storing information about lines. It is enlarged and shrunk
   *  automatically by the <code>insertLines()</code> and <code>deleteLines()</code>
   *  methods.
   */
  protected LineInfo[] lineInfo;

  /**
   *  The length of the <code>lineInfo</code> array.
   */
  protected int length;

  /**
   *  The last tokenized line.
   */
  protected int lastLine;

  /**
   *  True if the next line should be painted.
   */
  protected boolean nextLineRequested;

  // private members
  private KeywordMap keywords;
  private byte token;
  private int lastOffset;
  private int lastKeyword;
  private char matchChar;
  private boolean matchCharBracket;
  private boolean matchSpacesAllowed;

}
//  ***EOF***
