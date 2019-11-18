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

/**
 *  A linked list of tokens. Each token has three fields - a token identifier,
 *  which is a byte value that can be looked up in the array returned by
 *  <code>SyntaxDocument.getColors()</code> to get a color value, a length
 *  value which is the length of the token in the text, and a pointer to the
 *  next token in the list. The original file is written by Slava Pestov
 *  (www.gjt.org) and altered to fit ACL/SL.
 *
 * @author     Chris van Aart - Acklin B.V., the Netherlands & Slava Pestov
 * @created    June 14, 2002
 */
public class ACLToken {

  /**
   *  Creates a new token.
   *
   * @param  length  The length of the token
   * @param  id      The id of the token
   */
  public ACLToken(int length, byte id) {
    this.length = length;
    this.id = id;
  }


  /**
   *  Returns a string representation of this token.
   *
   * @return    Description of the Returned Value
   */
  public String toString() {
    return "[id=" + id + ",length=" + length + "]";
  }


  /**
   *  Normal text token id. This should be used to mark normal text.
   */
  public final static byte NULL = 0;

  /**
   *  Comment 1 token id. This can be used to mark a comment.
   */
  public final static byte COMMENT1 = 1;

  /**
   *  Comment 2 token id. This can be used to mark a comment.
   */
  public final static byte COMMENT2 = 2;

  /**
   *  Literal 1 token id. This can be used to mark a string literal (eg, C
   *  mode uses this to mark "..." literals)
   */
  public final static byte LITERAL1 = 3;

  /**
   *  Literal 2 token id. This can be used to mark an object literal (eg, Java
   *  mode uses this to mark true, false, etc)
   */
  public final static byte LITERAL2 = 4;

  /**
   *  Label token id. This can be used to mark labels (eg, C mode uses this to
   *  mark ...: sequences)
   */
  public final static byte LABEL = 5;

  /**
   *  Keyword 1 token id. This can be used to mark a keyword. This should be
   *  used for general language constructs.
   */
  public final static byte KEYWORD1 = 6;

  /**
   *  Keyword 2 token id. This can be used to mark a keyword. This should be
   *  used for preprocessor commands, or variables.
   */
  public final static byte KEYWORD2 = 7;

  /**
   *  Keyword 3 token id. This can be used to mark a keyword. This should be
   *  used for data types.
   */
  public final static byte KEYWORD3 = 8;

  /**
   *  Operator token id. This can be used to mark an operator. (eg, SQL mode
   *  marks +, -, etc with this token type)
   */
  public final static byte OPERATOR = 9;

  /**
   *  Invalid token id. This can be used to mark invalid or incomplete tokens,
   *  so the user can easily spot syntax errors.
   */
  public final static byte INVALID = 10;

  /**
   *  The total number of defined token ids.
   */
  public final static byte ID_COUNT = 11;

  /**
   *  The first id that can be used for internal state in a token marker.
   */
  public final static byte INTERNAL_FIRST = 100;

  /**
   *  The last id that can be used for internal state in a token marker.
   */
  public final static byte INTERNAL_LAST = 126;

  /**
   *  The token type, that along with a length of 0 marks the end of the token
   *  list.
   */
  public final static byte END = 127;

  /**
   *  The length of this token.
   */
  public int length;

  /**
   *  The id of this token.
   */
  public byte id;

  /**
   *  The next token in the linked list.
   */
  public ACLToken next;
}
//  ***EOF***
