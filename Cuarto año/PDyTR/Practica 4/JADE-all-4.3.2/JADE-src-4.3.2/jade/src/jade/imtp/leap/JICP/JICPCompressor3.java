
/**
 * Byte Array Compressor
 *
 * The (de-)compression algorthm is quite simple and memory efficient.
 *
 * the compression is done using a dictionary of words. words are detected
 * by predefined seperators (see isSep()). the dictionary of the words is
 * the (de-)compression array itself.
 *
 * @author Steffen Rusitschka, Siemens AG, CT IC 6
 * @author Dmitri Toropov, Siemens AG, CT IC 6
 *
 */

package jade.imtp.leap.JICP;

import java.io.*;


public class JICPCompressor3 {

    private static final int WORD_MAGIC    = 200;
    private static final int RLE_MAGIC     = 201;
    private static final int MAX_WORDS     = 254;

    // internal fields
    private static final int RLE_FLUSH     = 0x100;
    private int              rleOldValue   = RLE_FLUSH;
    private int              rleOccurrence = 0;
    private int count = 0;
    private byte[]           ba;


    private static boolean isSep(int value) {
        return !((value >= 'A' && value <= 'Z') ||
                 (value >= 'a' && value <= 'z') ||
                 (value >= '0' && value <= '9') ||
                  value == '-' ||
                  value == '_');
    }


    /**
     * run length encoding write
     * if value is RLE_FLUSH, the stream will be flushed.
     */
    private void rleWrite(ByteArrayOutputStream baos, int value) {
//        System.out.println("" + (count++) + ": " + (char)value + " " + value);
        if (rleOldValue == RLE_FLUSH) {
            rleOldValue   = value;
            rleOccurrence = 1;
            return;
        }

        if (value != rleOldValue || rleOccurrence == 255) {
            if (rleOldValue == RLE_MAGIC || rleOccurrence > 2) {
                baos.write(RLE_MAGIC);
                baos.write(rleOccurrence);
//                System.out.println("rle: " + rleOccurrence + " times " + (char)rleOldValue);
                if (rleOccurrence > 2) {
                    baos.write(rleOldValue);
                }
            } else {
                for (int i=0; i<rleOccurrence; ++i) {
                   baos.write(rleOldValue);
                }
            }
            rleOccurrence = 0;
        }

        rleOccurrence++;
        rleOldValue = value;
    }

    /**
     * run length encoding read
     */
    private int rleRead(ByteArrayInputStream bais) {
        if (rleOccurrence == 0) {
            rleOldValue = bais.read();
            if (rleOldValue == RLE_MAGIC) {
                rleOccurrence = bais.read();
                if (rleOccurrence > 2) {
                    rleOldValue = bais.read();
                }
            } else {
                rleOccurrence = 1;
            }
        }
        rleOccurrence--;
//        System.out.println("" + (count++) + ": " + (char)rleOldValue + " " + rleOldValue);
        return rleOldValue;
    }

    private int getValue(int index) {
        return index >= ba.length || index < 0 ?
               RLE_FLUSH :
               ((int)(char)ba[index]) & 255;
    }

    private void setValue(int index, int value) {
        if (index >= ba.length) {
            byte[] newba = new byte[index * 5/4];
            System.arraycopy(ba, 0, newba, 0, ba.length);
//            System.out.println("[r]");
            ba = newba;
        }
        ba[index] = (byte)value;
    }


/**
 * compress()
 *
 * algorithm:
 *
 * if during compression a word is detected, its position inside the array
 * is stored in the wordIndexes[] array. but only, if the word itself was not
 * found in the array before. if so, only a magic byte (WORD_MAGIC=255) and the index of
 * the word in the wordIndexes[] array is stored as a byte. the magic byte is
 * encoded as (255, 255). this limits the number of words that are possible
 * inside the wordIndexs[] array to 254 (=MAX_WORD constant).
 *
 */
    public static byte[] compress(byte[] ba) {
        return new JICPCompressor3().compressHelper(ba);
    }

    private byte[] compressHelper(byte[] uba) {

        int                   wordIndexes[] = new int[MAX_WORDS];
//        int                   beginIndex    = 0;
        int                   wordIndex     = 0;
        int                   lastWordIndex = 0;
        ByteArrayOutputStream baos          = new ByteArrayOutputStream();

        ba = uba; // set ba, so it is accessable through getValue().

        // go through array
        int i = 0;
        while (i < ba.length) {
//            System.out.println("" + i + ": " + (char)ba[i] + " " + (int)(char)ba[i]);
            int lastValue  = getValue(i - 1);
            int value      = getValue(i);

            if (isSep(lastValue) && !isSep(value)) {
                int maxJ              = 0;
                int maxJIndex         = -1;

                // find the word
                for (int wi=0; wi<lastWordIndex; ++wi) {

                    int existingWordIndex = wordIndexes[wi];
                    int j                 = 0;

                    while (true) {
                        int ch1 = getValue(existingWordIndex + j);
                        int ch2 = getValue(i + j);

                        if (ch1 != ch2) {
                            break; // words are different
                        }
                        if (++j == 255) {
                            break; // j has its maximum value
                        }
                    }
                    if (j > maxJ) {
                        maxJ = j;
                        maxJIndex = wi;
                    }
                }

                if (maxJ >= 4) { // word has been found if there are 4 or more
                                 // characters we can replace
//                    System.out.println(" w! " + maxJIndex + " len= " + maxJ);
                    rleWrite(baos, WORD_MAGIC);
                    rleWrite(baos, maxJIndex);
                    rleWrite(baos, maxJ);
                    i += maxJ;
                } else {
                    // add new word to the wordIndexes
                    if (wordIndex == MAX_WORDS) {
                        wordIndex = 0;
                    }
//                    System.out.println("wi["+wordIndex+"]=" + i);
                    wordIndexes[wordIndex++] = i;
                    if (wordIndex > lastWordIndex) {
                        lastWordIndex = wordIndex;
                    }

                    rleWrite(baos, value);
                    i++;
                }
            } else { // if isSep(lastValue) && !isSep(value)
                rleWrite(baos, value);
                if (value == WORD_MAGIC) {
                    rleWrite(baos, 255);
                }
                i++;
            }
        }

        rleWrite(baos, RLE_FLUSH);

        byte[] result = baos.toByteArray();
//        System.out.println("" + ba.length + "->" + result.length + " = " + (result.length*100)/ba.length+"%");
        return result;
    }


/**
 * decompress()
 *
 * algorithm:
 *
 * during decompression, the wordIndexes[] array will be built. this is done by reading
 * the compressed array, decode it and look for words. if a magic byte (WORD_MAGIC=255) is detected,
 * it will be decoded to a "real" 255 if the following byte is also 255 (255, 255 - sequence).
 * if the following byte is not 255, it is treated as an index in the wordIndexes[] array.
 * the index stored inside there is used as the beginning of a word. this word will be copied
 * to the end of the decoded stream. the end of the word is detected by a separator (see
 * isSep()).
 */
    public static byte[] decompress(byte[] cba) {
        return new JICPCompressor3().decompressHelper(cba);
    }

    private byte[] decompressHelper(byte[] cba) {
        int                  wordIndexes[] = new int[MAX_WORDS];
        int                  wordIndex     = 0;
        int                  currentIndex  = 0;
//        int                  beginIndex    = 0;
        ByteArrayInputStream bais          = new ByteArrayInputStream(cba);
        int                  ch;
        int                  oldCh         = -1;

        ba = new byte[cba.length * 3/2]; // init ba for setValue() access.

        while ((ch = rleRead(bais)) != -1) {

//            System.out.println("" + (char)ch + " " + (int)(char)ch);
            if (ch == WORD_MAGIC) {
                int wi = rleRead(bais);
                if (wi == 255) {
//                    System.out.print(" m ");
                    setValue(currentIndex++, WORD_MAGIC);
                } else {
                    int refWordIndex = wordIndexes[wi];
                    int len          = rleRead(bais);

                    for (int i=0; i<len; ++i) {
//                        System.out.print(" w ");
                        setValue(currentIndex++, ba[refWordIndex++]);
                    }
                }
            } else {
                if (isSep(getValue(currentIndex - 1)) && !isSep(ch)) {
                    if (wordIndex == MAX_WORDS) {
                        wordIndex = 0;
                    }
//                    System.out.println("wi["+wordIndex+"]=" + currentIndex);
                    wordIndexes[wordIndex++] = currentIndex;
                }

//                System.out.print(" r ");
                setValue(currentIndex++, ch);
            }
        }

        byte[] newba = new byte[currentIndex];
        System.arraycopy(ba, 0, newba, 0, currentIndex);
//        System.out.println("[r]");
        return newba;
    }
}
