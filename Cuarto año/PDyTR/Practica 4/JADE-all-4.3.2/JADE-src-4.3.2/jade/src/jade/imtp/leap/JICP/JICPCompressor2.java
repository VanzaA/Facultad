/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

/**
 * ***************************************************************
 * The LEAP libraries, when combined with certain JADE platform components,
 * provide a run-time environment for enabling FIPA agents to execute on
 * lightweight devices running Java. LEAP and JADE teams have jointly
 * designed the API for ease of integration and hence to take advantage
 * of these dual developments and extensions so that users only see
 * one development platform and a
 * single homogeneous set of APIs. Enabling deployment to a wide range of
 * devices whilst still having access to the full development
 * environment and functionalities that JADE provides.
 * Copyright (C) 2001 Telecom Italia LAB S.p.A.
 * Copyright (C) 2001 Broadcom Eireann Research.
 * Copyright (C) 2001 Siemens AG.
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

package jade.imtp.leap.JICP;

import java.io.ByteArrayOutputStream;

/**
 * Class declaration
 * @author Dmitri Toropov - Siemens AG
 */

 class JICPCompressor2 {
  /**
   * check if the symbol is separator
   *
   * @param sym - byte to check
   * @return true if sym is separator, false otherwise
   *
   */
  public static boolean is_separator(byte sym)
  {
    return (sym <= 32) || (sym == '(') || (sym == ')') || (sym == '@') || (sym == ':') || (sym == '/') || (sym == '!');
  }

  /**
   * operation +1 by modulo 253
   */
  private static int next(int val)
  {
	return (val==252)?(0):(val+1);
  }

  /**
   * subtraction by modulo 253
   */
  private static int back(int val, int num)
  {
	return (num>val)?(253+val-num):(val-num);
  }

  /**
   * sum by modulo 253
   */
  private static int sum(int val, int num)
  {
	return (num + val >= 253)?(val + num - 253):(val + num);
  }

  /**
   * normal mimimum function
   */
  private static int min(int a, int b)
  {
	return (a > b)?(b):(a);
  }

  /**
   * Compress the byte array
   *
   * @param data the data to compress
   * @return compressed data
   *
   */
  public static byte[] compress(byte[] data) {
    if (data == null) return null;
    int dataSize=data.length;
    byte[] result=null;
    try {
    int[] words = new int[min(dataSize+1,253)];
	      //words are interleaved with separators
	      //minimal lenth of each word or separator is a 1 symbol
	      //this way maximal number of words is lenghth/2
	      //(+ may be 1 if the lenght is odd)
    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    stream.write(dataSize/256); //first 2 bytes is the array length
    stream.write(dataSize%256);
    int numWords=0;
	int pos=0;
    boolean inWord=false;
    boolean dataEnded=false;
    while((pos < dataSize)&&(numWords < 126))
    {
      if(is_separator(data[pos]) || !inWord)
      {
	inWord=!is_separator(data[pos]);
	words[numWords]=pos;
	numWords++;
      }
	  pos++;
    }
	if (numWords < 126) //pos==dataSize
    {
      dataEnded = true;
      words[numWords]=pos;
    }
    int countWords=0;
    int charCounter=0;
    boolean startFlag = true;
    for(int i=0; i!=numWords; i=next(i))   //looking through the words; the first word is for sure not compressed
    {
      if (!dataEnded)
      {
	while((pos < dataSize)&&(!(is_separator(data[pos]) || !inWord))) pos++;
	if ((pos < dataSize)&&(is_separator(data[pos]) || !inWord))
	{
	  inWord = !is_separator(data[pos]);
	  words[numWords]=pos;
	  numWords = next(numWords);
	  pos++;
	}
	else //pos == dataSize
	{
	  words[numWords]=pos;
	  dataEnded = true;
	    }
	  }
      int j;
      for(j=(startFlag && (i<126))?(0):(back(i,126)); j!=i; j=next(j))	//trying to find the occurance of the current word; the history lenght is 126 words
      {
	countWords=charCounter=0;
	while( (words[i]+charCounter < dataSize - 1) && 		    //if the position with which we are comparing
									    //is not yet achieved the end of the array
	       (words[j]+charCounter < words[i]) &&			    //and the search position not yet achieved
									    //the current position
	       (data[words[i]+charCounter]==data[words[j]+charCounter]))    //and the characters are the same
	{
	  charCounter++;
	  if( (sum(j,countWords+1) != i) &&
	      (words[j]+charCounter >= words[sum(j,countWords+1)]) &&
	      (sum(i,countWords+1) != numWords) &&
	      (words[i]+charCounter >= words[sum(i,countWords+1)]) )
	    countWords++;  //if we achieved the next word boundary
									  //'+1' means that we count the word only when we
									  //have aready check all of its symbols
									  //(each space is one symbol word)
	}
	if(countWords > 0) //the correspondance was found
	{
	  if((countWords == 1)&&(words[next(i)]-words[i]<=2))
	  {
	    int k;
	    for(k=0; k< words[next(i)]-words[i]; k++)
	    {
	      if(data[words[i]+k]>=0) stream.write(data[words[i]+k]);
	      else
	      {
		stream.write(-127);
		stream.write(data[words[i]+k]);
	      }
	    }
	  }
	  else
	  {
	    stream.write(-back(i,j)); //offset (backward) of the position where to find this word
	    stream.write(-countWords); //if we have more then 1 word - write the number
	    i=sum(i,countWords-1);
	  }
	  break; //exit the internal for()
	}
      }
      if (countWords == 0)
      {
	for(j=0; j< words[next(i)]-words[i]; j++)
	{
	  if(data[words[i]+j]>=0) stream.write(data[words[i]+j]);
	  else
	  {
	    stream.write(-127);
	    stream.write(data[words[i]+j]);
	  }
	}
      }
      if(i==252) startFlag=false;
    }
    result = stream.toByteArray();
    /**************************
    System.out.print("Uncompressed: ");
    System.out.print(dataSize);
    System.out.print(" Compressed: ");
    System.out.println(result.length);
    System.out.flush();
    //*************************/
//    System.out.println("" + dataSize + "->" + result.length + " = " + (result.length*100)/dataSize+"%");
    return result;
    }
    catch(Exception e)
    {
      System.out.println("Compression exception:"+e.toString());
      e.printStackTrace();
    }
    return null;
  }

  /**
   * Decompress the byte array
   *
   * @param data the data to compress
   * @return compressed data
   *
   */
  public static byte[] decompress(byte[] data) {
    if (data == null) return null;
    int dataSize = data.length;
    int outputSize = ((data[0]>=0)?(data[0]):(256 + data[0]))*256 + ((data[1]>=0)?(data[1]):(256 + data[1]));
    //System.out.print("Data size ");
    //System.out.println(outputSize);
    byte[] output = new byte[outputSize]; //first byte is the array length
    int[] words = new int[min(outputSize,253)];
	      //words are interleaved with separators
	      //minimal lenth of each word or separator is a 1 symbol
	      //this way maximal number of words is lenghth/2
	      //(+ may be 1 if the lenght is odd)
    int numWords=0;
    int j=0;
    int i=2; //data[0] and data[1] are not a compressed data but a size
    try {
    i=2;
    while(i < dataSize)
    {
      if( (data[i] < 0) && (data[i] > -127) )
      {
	int wordsCount = (((i+1 < dataSize)&&(data[i+1] < 0)&&(data[i+1] > -127))?(-data[i+1]):1);
	int symCount = words[sum(back(numWords,(-data[i])), wordsCount)] - words[back(numWords,(-data[i]))];
	int k;
	for(k=0; k < symCount; k++)
	{
	  output[j+k] = output[words[back(numWords,(-data[i]))] + k];
	}
	for(k=0; k < wordsCount; k++)
	  words[sum(numWords,k)] = words[sum(back(numWords, (-data[i])), k)] - words[back(numWords, (-data[i]))]+ j;
	j+=symCount;
	numWords=sum(numWords,wordsCount);
	i+=2;
      }
      else
      {
	if(!is_separator(data[i])) //not a separator
	{
	  words[numWords]=j;
	  numWords=next(numWords);
	  output[j]=data[i];
	  i++;
	  j++;
	}
	while( (i<dataSize)&&(!is_separator(data[i])) ) //limit ourself to the case where all the symbols have codes < 128
	{
	  output[j]=data[i];
	  i++;
	  j++;
	}
	while( (i< dataSize) && (is_separator(data[i])) && ((data[i]>=0)||(data[i] == -127)) ) //copying the separators
	{
	  if(data[i] == -127) i++;
	  words[numWords]=j;
	  numWords=next(numWords);
	  output[j]=data[i];
	  i++;
	  j++;
	}
      }
    }
    return output;
    }
	catch(Exception e)
    {
      System.out.println("Decompression exception:"+e.toString());
	  e.printStackTrace();
    }
    return null;
  }
}
