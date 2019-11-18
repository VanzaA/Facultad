package jade.gui;

/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop 
multi-agent systems in compliance with the FIPA specifications.
Copyright (C) 2000 CSELT S.p.A. 

GNU Lesser General Public License

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation, 
version 2.1 of the License. 

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the
Free Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA  02111-1307, USA.
*****************************************************************/


//#APIDOC_EXCLUDE_FILE
//#J2ME_EXCLUDE_FILE

import java.awt.image.RGBImageFilter;
import java.awt.Color;
import java.awt.image.ImageFilter;

/**
   Javadoc documentation for the file
   @author Francisco Regi, Andrea Soracchi - Universita` di Parma
   @version $Date: 2003-11-20 11:55:37 +0100 (gio, 20 nov 2003) $ $Revision: 4572 $
 */
public class MyFilterImage extends RGBImageFilter {

  Color colorPixel;
  Color colorNewPixel;


  public MyFilterImage() {
     canFilterIndexColorModel = true;
  }

  public int filterRGB(int x, int y, int rgb) {
   int intensity;
   int alpha=128;
   int redComponent,greenComponent,bluComponent;

   redComponent=(rgb & 0xFF0000) >> 16;
   greenComponent=(rgb & 0xff00) >> 8;
   bluComponent=rgb & 0xFF;
   intensity=(int) (redComponent * 0.299 + greenComponent * 0.587 +bluComponent * 0.114) ;
   redComponent= intensity << 16;
   greenComponent=intensity << 8;
   bluComponent=intensity;
   alpha=alpha << 24;

   return (alpha + redComponent + greenComponent + bluComponent);
  }

}
