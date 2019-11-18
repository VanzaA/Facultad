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

package jade.tools.sniffer;

//#DOTNET_EXCLUDE_BEGIN
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;

import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseEvent;

import java.io.Serializable;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.ConcurrentModificationException;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
//#DOTNET_EXCLUDE_END
/*#DOTNET_INCLUDE_BEGIN
import java.util.*;
import System.Windows.Forms.*;
import System.Drawing.*;
#DOTNET_INCLUDE_END*/

import jade.core.AID;

import jade.gui.AclGui;

import jade.lang.acl.ACLMessage;

/**
   Javadoc documentation for the file
   @author Francisco Regi, Andrea Soracchi - Universita` di Parma
   <Br>
   <a href="mailto:a_soracchi@libero.it"> Andrea Soracchi(e-mail) </a>
   @version $Date: 2010-03-22 14:34:52 +0100 (lun, 22 mar 2010) $ $Revision: 6288 $
   @version $Date: 2010-03-22 14:34:52 +0100 (lun, 22 mar 2010) $ Modified by RR Kessler and ML Griss
   @version $Date: 2010-03-22 14:34:52 +0100 (lun, 22 mar 2010) $ Mofified by ML Griss - 3 line folding
 */

 /**
  * Manages agents and messages on both canvas. It holds an agent list, a message
  * list and all necessary methods for adding, removing and drawing these object.
  * It also registers ad handles events from mouse
  *
  * @see javax.swing.JPanel
  * @see java.awt.event.MouseListener
 */

public class MMCanvas 
	//#DOTNET_EXCLUDE_BEGIN
	extends JPanel implements MouseListener, MouseMotionListener, Serializable
	//#DOTNET_EXCLUDE_END
  {

  private static final int V_TOL = 4;
  private static final int H_TOL = 4;
  private static final int timeUnitWidth = 20;
  private static final int xOffset = 38;
  private int positionAgent=0;


  private int x1,x2,y;
  private MainWindow mWnd;
  private PanelCanvas panCan; /* To resize and modify the scroll bars */
  private MainPanel mPan;
  private int horDim = 400;
  private int vertDim = 200;
  private boolean typeCanv;
  private boolean nameShown = false;
  private List noSniffAgents=new ArrayList();
  //#DOTNET_EXCLUDE_BEGIN
  private Font font1 = new Font("Helvetica",Font.ITALIC,12);
  private Font font2 = new Font("SanSerif",Font.BOLD,12);
  // font3 is used to display the name of the performative above the messages.
  // Needed something a bit smaller than 1 or 2 above so it isn't too obtrusive.
  private Font font3 = new Font("SanSerif", Font.PLAIN, 10);
  //#DOTNET_EXCLUDE_END
  /*#DOTNET_INCLUDE_BEGIN
  private Font font1 = new Font("Helvetica", 12, FontStyle.Italic);
  private Font font2 = new Font("SanSerif",12, FontStyle.Bold);
  // font3 is used to display the name of the performative above the messages.
  // Needed something a bit smaller than 1 or 2 above so it isn't too obtrusive.
  private Font font3 = new Font("SanSerif", 10, FontStyle.Regular);
  private Font font4 = new Font("Helvetica", 10, FontStyle.Italic);
  private Panel myPanel;
  #DOTNET_INCLUDE_END*/
  private MMCanvas otherCanv;
  public AgentList al;
  public MessageList ml;
  
  // These vars are used to make messages grouped by conversationID appear as the
  // same color.  It makes it easier to pick out various conversations.
  private HashMap mapToColor = new HashMap();
  // Removed green, orange, and pink.  They were too hard to see.
  //#DOTNET_EXCLUDE_BEGIN
  private Color colorTable[] = {new Color(200, 0, 150), Color.blue, new Color(230, 230, 0), Color.red, Color.black, Color.magenta, Color.cyan, 
  Color.pink, new Color(0, 200, 150), Color.green};
  private Color noConversationColor = Color.gray;
  //#DOTNET_EXCLUDE_END
  /*#DOTNET_INCLUDE_BEGIN
  	private Color colorTable[] = 
		{
			Color.get_Blue(), Color.get_Black(), Color.get_Cyan(), 
			Color.get_Magenta(), Color.get_Red(), Color.get_White(), Color.get_Yellow()
		};
  private Color noConversationColor = Color.gray;
  #DOTNET_INCLUDE_END*/
  private int colorCounter = 0;
  
  public MMCanvas(boolean type,MainWindow mWnd, PanelCanvas panCan, MainPanel mPan, MMCanvas other ) {
   super();
   otherCanv=other;
   typeCanv=type;
   al=new AgentList();
   ml=new MessageList();
   this.panCan = panCan;
   //#DOTNET_EXCLUDE_BEGIN
   setDoubleBuffered(false);
   addMouseListener(this);
   addMouseMotionListener(this);
   //#DOTNET_EXCLUDE_END
   this.mWnd = mWnd;
   this.mPan=mPan;

   //#DOTNET_EXCLUDE_BEGIN
   if (typeCanv)
     setPreferredSize( new Dimension(horDim,50));
   else
     setPreferredSize( new Dimension(horDim,vertDim));
   //#DOTNET_EXCLUDE_END
   }

  // drawing is all here

  /*#DOTNET_INCLUDE_BEGIN
  public void setPanel(Panel panel)
  {
	myPanel = panel;
	myPanel.add_Paint( new PaintEventHandler( this.paint ) );
  }
  
  public Panel getPanel()
  {
	return myPanel;
  }
  #DOTNET_INCLUDE_END*/

  //#DOTNET_EXCLUDE_BEGIN
  public void paintComponent(Graphics g)
  //#DOTNET_EXCLUDE_END
  /*#DOTNET_INCLUDE_BEGIN
  public void paint(Object o, PaintEventArgs e)
  #DOTNET_INCLUDE_END*/
  {
   //#DOTNET_EXCLUDE_BEGIN
   super.paintComponent(g);
   //#DOTNET_EXCLUDE_END

   int yDim			= 0;
   int xSource		= 0;
   int xDest		= 0;

   int xCanvDim		= 0;
   /*#DOTNET_INCLUDE_BEGIN
   double yOffset		= 0;
   Color color = Color.get_White();
   Graphics g = e.get_Graphics();	
   #DOTNET_INCLUDE_END*/
   try 
   {
     if(typeCanv == true) {
 
       Iterator it = al.getAgents();
       while(it.hasNext()) {

         Agent agent = (Agent)it.next();

         int x = Agent.yRet+(xCanvDim++)*80;

	     //#DOTNET_EXCLUDE_BEGIN
         if(agent.onCanv == false) g.setColor(Color.gray);
         else g.setColor(Color.red);

         if(checkNoSniffedVector(agent)) g.setColor(Color.yellow);
	     
   	     g.draw3DRect(x,Agent.yRet,Agent.bRet,Agent.hRet,true);
		 g.fill3DRect(x,Agent.yRet,Agent.bRet,Agent.hRet,true);
	     g.setColor(Color.black);
		 FontMetrics fm = g.getFontMetrics();
	     //#DOTNET_EXCLUDE_END

		 /*#DOTNET_INCLUDE_BEGIN
		 if (agent.onCanv == false)
			color = Color.get_Gray();
		 else
			color = Color.get_Red();
		 
		 if(checkNoSniffedVector(agent)) 
			color = Color.get_Yellow();
		 
		 Pen pen = new Pen(color);
		 g.DrawRectangle(pen, x, Agent.yRet, Agent.bRet, Agent.hRet);
		 g.FillRectangle(new SolidBrush(color), x, Agent.yRet, Agent.bRet, Agent.hRet);
		 color = Color.get_Black();
		 #DOTNET_INCLUDE_END*/

         String aName=agent.agentName;
         aName = nameClip(aName);
		 //#DOTNET_EXCLUDE_BEGIN
         int nameWidth = fm.stringWidth(aName);
	     //#DOTNET_EXCLUDE_END
	     /*#DOTNET_INCLUDE_BEGIN
		 Font f = new Font("Arial", 10);
		 SizeF sizef = g.MeasureString(aName, f );
		 int nameWidth = (int) sizef.get_Width();
		 #DOTNET_INCLUDE_END*/
         if (nameWidth < Agent.bRet) 
		 {
		    //#DOTNET_EXCLUDE_BEGIN
            g.drawString(aName,x+(Agent.bRet-nameWidth)/2,Agent.yRet+(Agent.hRet/2) + (fm.getAscent()/2));
		    //#DOTNET_EXCLUDE_END
		    /*#DOTNET_INCLUDE_BEGIN
			g.DrawString(aName, f, new SolidBrush(color), x+(Agent.bRet-nameWidth)/2, Agent.yRet+(Agent.hRet/2) - (sizef.get_Height()/2));
			yOffset = Agent.yRet+(Agent.hRet/2) - (sizef.get_Height()/2);
			#DOTNET_INCLUDE_END*/
         } 
		 else 
		 {
           // Need to chop the string up into at most 2 or 3 pieces, truncating the rest.
           int len = aName.length();
           String part1;
           String part2;
           String part3;
           if (nameWidth < Agent.bRet * 2) {
               // Ok, it is not quite twice as big, so cut in half
               part1 = aName.substring(0, len/2);
               part2 = aName.substring(len/2);
			   //#DOTNET_EXCLUDE_BEGIN
               g.drawString(part1, x+(Agent.bRet-fm.stringWidth(part1))/2,Agent.yRet+(Agent.hRet/2) - (int)(fm.getAscent() * 0.2));
               g.drawString(part2, x+(Agent.bRet-fm.stringWidth(part2))/2,Agent.yRet+(Agent.hRet/2) + (int)(fm.getAscent() * 0.9));
			   //#DOTNET_EXCLUDE_END
			   /*#DOTNET_INCLUDE_BEGIN
			   SolidBrush sb = new SolidBrush(color);
			   SizeF sizef1 = g.MeasureString(part1, f );
			   g.DrawString(part1, f, sb, x+( Agent.bRet-sizef1.get_Width() )/2, Agent.yRet+( Agent.hRet/2) - (int) (sizef1.get_Height() * 0.9) );
			   SizeF sizef2 = g.MeasureString(part2, f );
			   g.DrawString(part2, f, sb, x+( Agent.bRet-sizef2.get_Width() )/2, Agent.yRet+( Agent.hRet/2) - (int) (sizef2.get_Height() * 0.2) );
			   #DOTNET_INCLUDE_END*/

           } else if (nameWidth < Agent.bRet * 3) {
               // Ok, it is not quite thrice as big, so cut in three
               part1 = aName.substring(0, len/3);
               part2 = aName.substring(len/3, 2*len/3);
               part3 = aName.substring(2*len/3);
			   //#DOTNET_EXCLUDE_BEGIN
               g.drawString(part1, x+(Agent.bRet-fm.stringWidth(part1))/2,Agent.yRet+(Agent.hRet/2) - (int)(fm.getAscent() * 0.65));
               g.drawString(part2, x+(Agent.bRet-fm.stringWidth(part2))/2,Agent.yRet+(Agent.hRet/2) + (int)(fm.getAscent() * 0.3));
               g.drawString(part3, x+(Agent.bRet-fm.stringWidth(part3))/2,Agent.yRet+(Agent.hRet/2) + (int)(fm.getAscent() * 0.95));
			   //#DOTNET_EXCLUDE_END
			   /*#DOTNET_INCLUDE_BEGIN
  			   SolidBrush sb = new SolidBrush(color);
			   SizeF sizef1 = g.MeasureString(part1, f );
			   g.DrawString(part1, f, sb, x+( Agent.bRet-sizef1.get_Width() )/2, Agent.yRet+( Agent.hRet/2) - (int) (sizef1.get_Height() * 1.00) );
			   SizeF sizef2 = g.MeasureString(part2, f );
			   g.DrawString(part2, f, sb, x+( Agent.bRet-sizef2.get_Width() )/2, Agent.yRet+( Agent.hRet/2) - (int) (sizef2.get_Height() * 0.50) );
			   SizeF sizef3 = g.MeasureString(part3, f );
			   g.DrawString(part3, f, sb, x+( Agent.bRet-sizef3.get_Width() )/2, Agent.yRet+( Agent.hRet/2) - (int) (sizef3.get_Height() * 0.01) );
			   #DOTNET_INCLUDE_END*/
           } 
		   else 
		   {
               // This is rounded down the size of each char.
               int approxCharWidth = nameWidth / agent.agentName.length();
               int charCount = Agent.bRet / approxCharWidth;
               part1 = aName.substring(0, charCount);
               if (aName.length() < (charCount * 2) ) {
                   part2 = aName.substring(charCount);
                   part3 = "";
               } else {
                   part2 = aName.substring(charCount, (charCount * 2));
                   if (charCount * 3 > aName.length()) {
                       part3 = aName.substring(charCount * 2);
                   } else {
                    part3 = aName.substring(charCount*2, (charCount * 3));
                   }
               }
			   //#DOTNET_EXCLUDE_BEGIN
               g.drawString(part1, x+(Agent.bRet-fm.stringWidth(part1))/2,Agent.yRet+(Agent.hRet/2) - (int)(fm.getAscent() * 0.65));
               g.drawString(part2, x+(Agent.bRet-fm.stringWidth(part2))/2,Agent.yRet+(Agent.hRet/2) + (int)(fm.getAscent() * 0.3));
               g.drawString(part3, x+(Agent.bRet-fm.stringWidth(part3))/2,Agent.yRet+(Agent.hRet/2) + (int)(fm.getAscent() * 0.95));
			   //#DOTNET_EXCLUDE_END
			   /*#DOTNET_INCLUDE_BEGIN
			   SolidBrush sb = new SolidBrush(color);
			   SizeF sizef1 = g.MeasureString(part1, f);
			   g.DrawString(part1, f, sb, x+(agent.bRet-sizef1.get_Width())/2, Agent.yRet+(Agent.hRet/2) - (int) (sizef1.get_Height() * 1.00));
			   SizeF sizef2 = g.MeasureString(part2, f);
			   g.DrawString(part2, f, sb, x+(agent.bRet-sizef2.get_Width())/2, Agent.yRet+(Agent.hRet/2) - (int) (sizef2.get_Height() * 0.50));
			   SizeF sizef3 = g.MeasureString(part3, f);
			   g.DrawString(part3, f, sb, x+(agent.bRet-sizef3.get_Width())/2, Agent.yRet+(Agent.hRet/2) - (int) (sizef3.get_Height() * 0.01));
			   #DOTNET_INCLUDE_END*/
             }
         }
       }

       horDim = 100+(xCanvDim*80);

     }
     
     /*#DOTNET_INCLUDE_BEGIN
	 int myOffset = Agent.yRet + Agent.hRet;
     #DOTNET_INCLUDE_END*/

     if((typeCanv == false)) 
	 {

       /* This is the Message Canvas: so let's paint all the messages */

       int x1,x2,y;
       int xCoords[] = new int[3];
       int yCoords[] = new int[3];
       xCanvDim = otherCanv.al.size();

       Iterator it = ml.getMessages();
       int AllReceiver = 0;  
       while(it.hasNext()) {
         Message mess = (Message)it.next();
         String senderName = mess.getSender().getName();    
         xSource = otherCanv.al.getPos(senderName);
         //int receiverForAMessage = 0;
         //for(Iterator i = mess.getAllReceiver(); i.hasNext();) {
       	   //receiverForAMessage++;
       	   //String receiverName = ((AID)i.next()).getName();
         String receiverName = mess.getUnicastReceiver().getName();
           xDest = otherCanv.al.getPos(receiverName);
       
           x1 = mess.getInitSeg(xSource);
           x2 = mess.getEndSeg(xDest);
           y = mess.getOrdSeg(yDim++);

           /* Were we fill the coordinate array for the arrow tip */

           xCoords[0] = x2-6;
           xCoords[1] = x2-6;
           xCoords[2] = x2+2;
		   //#DOTNET_EXCLUDE_BEGIN
           yCoords[0] = y-5;
           yCoords[1] = y+5;
           yCoords[2] = y;
		   //#DOTNET_EXCLUDE_END
		   /*#DOTNET_INCLUDE_BEGIN
		   yCoords[0] = y-5+myOffset;
		   yCoords[1] = y+5+myOffset;
		   yCoords[2] = y+myOffset;
		   #DOTNET_INCLUDE_END*/

           if(x1 > x2) {
	         xCoords[0] = x2+10;
	         xCoords[1] = x2+10;
	         xCoords[2] = x2+2;
           }

           // First we lookup convID, replywith and replyto to see if any of them
           // have a colorindex.  If any of them do, then that becomes the one that
           // we will use.
           Integer colorIndex = new Integer(-1);
           //System.out.println("Starting color:" + mess.getPerformative() +
           //    " CID:" + mess.getConversationId() +
           //    " RW:" + mess.getReplyWith() +
           //    " RT:" + mess.getInReplyTo());
           
           Object conversationKey = null;
           if (mess.getConversationId() != null) {
        	   conversationKey = mess.getConversationId();
           }
           else if (mess.getReplyWith() != null) {
        	   conversationKey = mess.getReplyWith();
           }
           else if (mess.getInReplyTo() != null) {
        	   conversationKey = mess.getInReplyTo();
           }
           
           Color messageColor = null;
           if (conversationKey != null) {
	           if (mapToColor.containsKey(conversationKey)) {
	               colorIndex = (Integer)mapToColor.get(conversationKey);        	   
	           }
	           else {
	        	   colorIndex = getNewColorIndex();
	        	   mapToColor.put(conversationKey, colorIndex);
	           }
	           messageColor = colorTable[colorIndex.intValue() % colorTable.length];
           }
           else {
        	   messageColor = noConversationColor;
           }
           /*if (mess.getConversationId() != null) {
             if (mapToColor.containsKey(mess.getConversationId())) {
                colorIndex = (Integer)mapToColor.get(mess.getConversationId());
                //System.out.println("Found CID:" + colorIndex);
             }
           }
           if (mess.getReplyWith() != null && colorIndex.intValue() == -1) {
             if (mapToColor.containsKey(mess.getReplyWith())) {
                colorIndex = (Integer)mapToColor.get(mess.getReplyWith());
                //System.out.println("Found RW:" + colorIndex);
             }
           } 
           if (mess.getInReplyTo() != null && colorIndex.intValue() == -1) {
             if (mapToColor.containsKey(mess.getInReplyTo())) {
                colorIndex = (Integer)mapToColor.get(mess.getInReplyTo());
                //System.out.println("Found RT:" + colorIndex);
             }
           }

           // If not, then we get the next color value.
           if (colorIndex.intValue() == -1) {
             colorCounter = new Integer(colorCounter.intValue() + 1);
             colorIndex = colorCounter;
             //System.out.println("Making new:" + colorIndex);
           }

           // Now, we store this value on all non-null ids.
           if (mess.getConversationId() != null) {
             //System.out.println("CID:" + mess.getConversationId()+ " was: " + mapToColor.get(mess.getConversationId()));
             mapToColor.put(mess.getConversationId(), colorIndex);
           }
           if (mess.getReplyWith() != null) {
             //System.out.println("RW:" + mess.getReplyWith()+ " was: " + mapToColor.get(mess.getReplyWith()));
             mapToColor.put(mess.getReplyWith(), colorIndex);
           }
           if (mess.getInReplyTo() != null) {
             //System.out.println("RT:" + mess.getInReplyTo() + " was: " + mapToColor.get(mess.getInReplyTo()));
             mapToColor.put(mess.getInReplyTo(), colorIndex);
           }
           //System.out.println("Done");*/
           
           
		   //#DOTNET_EXCLUDE_BEGIN
           g.setColor(messageColor);
           g.drawRect(x1-3,y-4,4,8);
           g.fillRect(x1-3,y-4,4,8);

		   // This code displays the name of the performative centered above the
		   // arrow.  At some point, might want to make this optional.
     	   g.setFont(font3);
		   FontMetrics fmPerf = g.getFontMetrics();
		   
		   String perf = mess.getPerformative(mess.getPerformative());
		   //#DOTNET_EXCLUDE_END
		   /*#DOTNET_INCLUDE_BEGIN
		   color = colorTable[colorIndex.intValue() % colorTable.length];
		   Pen pen = new Pen(color);
		   SolidBrush sb = new SolidBrush(color);
		   g.DrawRectangle(pen, x1-3, y-4+myOffset, 4, 8);
		   g.FillRectangle(sb, x1-3, y-4+myOffset, 4, 8);
	   
	       // This code displays the name of the performative centered above the
           // arrow.  At some point, might want to make this optional.
		   String perf = mess.getPerformative(mess.getPerformative());
		   SizeF sizefPerf = g.MeasureString(perf, font3);
		   #DOTNET_INCLUDE_END*/

           // Add ConversationId and ReplyWith
           int numberToShow=3;
           perf=perf + ":" + colorIndex
                     + " (" + tail(numberToShow,mess.getConversationId()) 
                     + "  " + tail(numberToShow,mess.getReplyWith()) 
                     + "  " + tail(numberToShow,mess.getInReplyTo()) + " )";

		   //#DOTNET_EXCLUDE_BEGIN
           int perfWidth = fmPerf.stringWidth(perf);
		   //#DOTNET_EXCLUDE_END
		   /*#DOTNET_INCLUDE_BEGIN
		   int perfWidth = (int) sizefPerf.get_Width();
		   int perfHeight = (int) sizefPerf.get_Height();
		   #DOTNET_INCLUDE_END*/
           if (x2 > x1) 
		   {
		     //#DOTNET_EXCLUDE_BEGIN
             g.drawString(perf, x1+((x2-x1)/2)-perfWidth/2, y-4);
		     //#DOTNET_EXCLUDE_END
		     /*#DOTNET_INCLUDE_BEGIN
			 g.DrawString(perf, font3, sb, x1+((x2-x1)/2)-perfWidth/2, y+myOffset);
			 #DOTNET_INCLUDE_END*/
           } 
		   else 
		   {
		     //#DOTNET_EXCLUDE_BEGIN
             g.drawString(perf, x2+((x1-x2)/2)-perfWidth/2, y-4);
		     //#DOTNET_EXCLUDE_END
			 /*#DOTNET_INCLUDE_BEGIN
			 g.DrawString(perf, font3, sb, x2+((x1-x2)/2)-perfWidth/2, y+myOffset);
			 #DOTNET_INCLUDE_END*/
           }
        
		   /*#DOTNET_INCLUDE_BEGIN
		   int deep = y+myOffset+perfHeight;
		   if ( deep > myPanel.get_Height() )
		   {
			   myPanel.set_Height( deep+perfHeight );
		   }
		   #DOTNET_INCLUDE_END*/

           // disegno segmento messaggio
           for(int k=-1; k<=1; k++) {
             if (x2 > x1) {
			   //#DOTNET_EXCLUDE_BEGIN
	           g.drawLine(x1,y+k,x2,y+k);
			   //#DOTNET_EXCLUDE_END
			   /*#DOTNET_INCLUDE_BEGIN
			   g.DrawLine(pen, x1, y+k+myOffset, x2, y+k+myOffset);
			   #DOTNET_INCLUDE_END*/
             } 
			 else 
			 {
			   //#DOTNET_EXCLUDE_BEGIN
	           g.drawLine(x1,y+k,x2+4,y+k);
			   //#DOTNET_EXCLUDE_END
			   /*#DOTNET_INCLUDE_BEGIN
			   g.DrawLine(pen, x1, y+k+myOffset, x2+4, y+k+myOffset);
			   #DOTNET_INCLUDE_END*/
             }
           }

           // disegno freccetta del receiver
		   //#DOTNET_EXCLUDE_BEGIN
		   g.drawPolygon(xCoords,yCoords,3);
           g.fillPolygon(xCoords,yCoords,3);
		   //#DOTNET_EXCLUDE_END
		   /*#DOTNET_INCLUDE_BEGIN
  		   Point[] xyPoints = new Point[xCoords.length];
		
		   for (int i=0; i<xCoords.length; i++)
		   {
			xyPoints[i] = new Point(xCoords[i], yCoords[i]);
		   }//End FOR block

		   g.DrawPolygon(pen, xyPoints);
		   g.FillPolygon(sb, xyPoints);
		   #DOTNET_INCLUDE_END*/

         //}
         AllReceiver++;
         //AllReceiver = AllReceiver+receiverForAMessage;
       } // while

       int msgNum = ml.size();
       for(int num = 0; num < xCanvDim; num++) {
         // Here we update the green lines of the timeline
	     //#DOTNET_EXCLUDE_BEGIN
         int x =  jade.tools.sniffer.Agent.yRet/2+num*80;
         g.setColor(new Color(0,100,50));
	     //#DOTNET_EXCLUDE_END
	     /*#DOTNET_INCLUDE_BEGIN
		 int x = (int) jade.tools.sniffer.Agent.yRet/2+num*80;
		 color = Color.FromArgb(0, 100, 50);
		 #DOTNET_INCLUDE_END*/
         //g.drawLine(x+xOffset,1,x+xOffset,timeUnitWidth*(msgNum+1));
         int counter = 0;
         for(Iterator i = ml.getMessages(); i.hasNext(); ) {
         	Message msg = (Message)i.next();
         	//int singleMsgCounter =0;
         	//for(Iterator j = msg.getAllReceiver(); j.hasNext(); )
         	//{  j.next();
         	//   singleMsgCounter++;
          //         msg.setMessageNumber(counter + singleMsgCounter);
         	//}
          //counter = counter + singleMsgCounter;        
         	msg.setMessageNumber(counter++);
         }
	     //#DOTNET_EXCLUDE_BEGIN
         g.drawLine(x+xOffset,1,x+xOffset,timeUnitWidth*(counter+1));
		 //#DOTNET_EXCLUDE_END
	     /*#DOTNET_INCLUDE_BEGIN
		 Pen pen = new Pen(color);
		 g.DrawLine(pen, x+xOffset, 1+myOffset, x+xOffset, timeUnitWidth*(counter+1)+myOffset);
		 #DOTNET_INCLUDE_END*/
       }

	   //#DOTNET_EXCLUDE_BEGIN
       g.setColor(new Color(150,50,50));
	   //#DOTNET_EXCLUDE_END
	   /*#DOTNET_INCLUDE_BEGIN
	   color = Color.FromArgb(150, 50, 50);
	   #DOTNET_INCLUDE_END*/
       Integer msgNumWrapped;
       for (int t=0; t <=AllReceiver; t++) {
          // Here we update the red numbers of the timeline
          msgNumWrapped = new Integer(t);
	      //#DOTNET_EXCLUDE_BEGIN
          g.drawString(msgNumWrapped.toString(),10,timeUnitWidth*(t)+15);
	      //#DOTNET_EXCLUDE_END
	      /*#DOTNET_INCLUDE_BEGIN
		  Font f = new Font("Arial", 10);
		  g.DrawString(msgNumWrapped.ToString(), f, new SolidBrush(color), 10, timeUnitWidth*(t)+15+myOffset);
		  #DOTNET_INCLUDE_END*/
       }
       horDim = 100+(xCanvDim*80);
       vertDim = 100+(yDim*20);
	   /*#DOTNET_INCLUDE_BEGIN
	   int deep = timeUnitWidth*(AllReceiver)+15+myOffset;
	   if ( deep > myPanel.get_Height() )
	   {
		myPanel.set_Height( deep+Agent.hRet*2 );
	   }
	   #DOTNET_INCLUDE_END*/
    }// if
  } catch (ConcurrentModificationException cme) {
     // Ignore - next repaint will correct things
  }
 } // Method

  private Integer getNewColorIndex() {
	  for (int i = 0; i < colorTable.length; ++i) {
		  Integer index = new Integer(i);
		  if (!mapToColor.containsValue(index)) {
			  return index;
		  }
	  }
	  Integer index = new Integer(colorCounter);
	  colorCounter++;
	  if (colorCounter >= colorTable.length) {
		  colorCounter = 0;
	  }
	  return index;
  }
  /**
   * Method invoked everytime the use clicks on a blue arrow: it updates the TextMessage
   * component displaying the type of the message.
   *
   * @param evt mouse event
   */
  //#DOTNET_EXCLUDE_BEGIN
  public void mousePressed(MouseEvent evt) {
  //#DOTNET_EXCLUDE_END
  /*#DOTNET_INCLUDE_BEGIN
  public void OnMousePressed(Object o, MouseEventArgs evt) {
   String messageToDisplay	= "";
  #DOTNET_INCLUDE_END*/
   String info				= "";
   Message mess;
   int numberToShow=5;

    if( ((mess = selMessage(evt)) != null) && (typeCanv == false)) {
       info = "  Message:" + mess.getMessageNumber() + " ";
       //#DOTNET_EXCLUDE_BEGIN
	   mPan.textArea.setText(" ");
       //mPan.textArea.setFont(font1);
       mPan.textArea.setText(info);
       mPan.textArea.setFont(font2);
       mPan.textArea.append(ACLMessage.getPerformative(
	   //#DOTNET_EXCLUDE_END
	   /*#DOTNET_INCLUDE_BEGIN
  	   messageToDisplay = jade.lang.acl.ACLMessage.getPerformative(
	   #DOTNET_INCLUDE_END*/
        mess.getPerformative())
         + " ( cid=" + tail(numberToShow,mess.getConversationId()) 
         + " rw="   + tail(numberToShow,mess.getReplyWith()) 
         + " irt="   + tail(numberToShow,mess.getInReplyTo()) 
         + " proto=" + mess.getProtocol()
         + " onto=" + mess.getOntology()
	   //#DOTNET_EXCLUDE_BEGIN
         + " )" );
	   //#DOTNET_EXCLUDE_END
	   /*#DOTNET_INCLUDE_BEGIN
	   + " )";
	   #DOTNET_INCLUDE_END*/
    } else {
        Agent selectedAgent = selAgent(evt);
        if ((selectedAgent != null) && (typeCanv == true)) {
			//#DOTNET_EXCLUDE_BEGIN
            mPan.textArea.setText("Agent: ");
            mPan.textArea.setFont(font2);
            mPan.textArea.append(selectedAgent.agentName);
			//#DOTNET_EXCLUDE_END
			/*#DOTNET_INCLUDE_BEGIN
			info = "Agent: ";
			messageToDisplay = selectedAgent.agentName;
			#DOTNET_INCLUDE_END*/
        }
    }
    /*#DOTNET_INCLUDE_BEGIN
	mWnd.textBox1.set_Font( font4 );
	mWnd.textBox1.set_Text(info + messageToDisplay);
	#DOTNET_INCLUDE_END*/

   }

  //#DOTNET_EXCLUDE_BEGIN
  /**
   * This method is invoked every time a user double-click on a blue arrow in the message canvas: the double-click occurs
   * on a blue arrow in the message canavs, a dialog box is displayed with the entire
   * message.
   *
   * @param evt mouse event
   */
   public void mouseClicked(MouseEvent evt) {
    Agent ag;
    Message mess;
    String info;
      if(evt.getClickCount() == 2) {

       if( ((mess = selMessage(evt)) != null) && (typeCanv == false)) {
			   AclGui.showMsgInDialog(mess,mWnd);
       }
    }
   }

 public void mouseEntered(MouseEvent evt) {}
 public void mouseExited(MouseEvent evt) {}
 public void mouseReleased(MouseEvent evt) {}

 public void mouseDragged(MouseEvent evt) {}
 
  public void mouseMoved(MouseEvent evt) {
    Agent selectedAgent = selAgent(evt);
    if ((selectedAgent != null) && (typeCanv == true)) {
      if (!nameShown) {
        nameShown = true;
        mPan.textArea.setText("Agent: ");
        mPan.textArea.setFont(font2);
        mPan.textArea.append(selectedAgent.agentName);
      }
    } else {
      if (nameShown) {
        nameShown = false;
        mPan.textArea.setText(null);
      }
    }
  }
 //#DOTNET_EXCLUDE_END

 private boolean checkNoSniffedVector(Agent agent) {
  boolean isPresent=false;
  Agent agentToCompare;

  if(noSniffAgents.size()==0) return false;
   else {
    for (int i=0; i<noSniffAgents.size();i++) {
     agentToCompare=(Agent)noSniffAgents.get(i);
     if(agentToCompare.agentName.equals(agent.agentName)) {
      isPresent=true;
      positionAgent=i;
      break;
     }
    }
    if (isPresent) return true;
    else return false;
   }
 }

 //#DOTNET_EXCLUDE_BEGIN
 public Message selMessage(MouseEvent evt) {
 //#DOTNET_EXCLUDE_END
 /*#DOTNET_INCLUDE_BEGIN
 public Message selMessage(MouseEventArgs evt) {
 #DOTNET_INCLUDE_END*/
   int j = 0;

   /*#DOTNET_INCLUDE_BEGIN
   Point pClient	= myPanel.PointToClient( new Point(evt.get_X(), evt.get_Y() ) );
   Point pScreen	= myPanel.PointToScreen( new Point(evt.get_X(), evt.get_Y() ) );
   int xClient		= pClient.get_X();
   int yClient		= pClient.get_Y();
   int xScreen		= pScreen.get_X();
   int yScreen		= pScreen.get_Y();
   int panelHeight	= otherCanv.getPanel().get_Height(); 
   #DOTNET_INCLUDE_END*/

   Iterator it = ml.getMessages();
   while(it.hasNext()) {
     Message mess = (Message)it.next();
     String senderName = mess.getSender().getName();
     
     //for(Iterator i = mess.getAllReceiver();i.hasNext(); )
     //{
     	//String receiverName = ((AID)i.next()).getName();
     	String receiverName = mess.getUnicastReceiver().getName();
      x1 = mess.getInitSeg(otherCanv.al.getPos(senderName));
      x2 = mess.getEndSeg(otherCanv.al.getPos(receiverName));
      y = mess.getOrdSeg(j++);
      if(x1 < x2) {
	   //#DOTNET_EXCLUDE_BEGIN
       if((evt.getX() >= x1+H_TOL) && (evt.getX() <= x2+H_TOL) &&
	    (evt.getY() >= y - V_TOL) && (evt.getY() <= y + V_TOL)) {
	   //#DOTNET_EXCLUDE_END
	   /*#DOTNET_INCLUDE_BEGIN
	   if((evt.get_X() >= x1+H_TOL) && (evt.get_X() <= x2+H_TOL) &&
	    (evt.get_Y()-panelHeight >= y - V_TOL) && (evt.get_Y()-panelHeight <= y + V_TOL)) {
	   #DOTNET_INCLUDE_END*/
	      return mess;
       }
      }
      else {
	   //#DOTNET_EXCLUDE_BEGIN
       if((evt.getX() >= x2 - H_TOL) && (evt.getX() <= x1 + H_TOL) &&
	      (evt.getY() >= y - V_TOL) && (evt.getY() <= y + V_TOL)) {
	   //#DOTNET_EXCLUDE_END
	   /*#DOTNET_INCLUDE_BEGIN
	   if((evt.get_X() >= x2 - H_TOL) && (evt.get_X() <= x1 + H_TOL) &&
	      (evt.get_Y()-panelHeight >= y - V_TOL) && (evt.get_Y()-panelHeight <= y + V_TOL)) {
	   #DOTNET_INCLUDE_END*/
	       return mess;
       }
      }
     //}//for
   }//while

   return null;

 }

  /**
   * Returns an Agent if an Agent has been selected form the user, otherwise
   * returns null.
   *
   * @param evt mouse event
   * @return Agent selected or null if no Agent was selected
   */
 //#DOTNET_EXCLUDE_BEGIN
 public Agent selAgent(MouseEvent evt) {
 //#DOTNET_EXCLUDE_END
 /*#DOTNET_INCLUDE_BEGIN
 public Agent selAgent(MouseEventArgs evt) {
 #DOTNET_INCLUDE_END*/
   int j = 0;
   int y1 = Agent.yRet;
   int y2 = y1 + Agent.yRet;

   try {
     Iterator it = al.getAgents();
     while(it.hasNext()) {
       Agent ag = (Agent)it.next();
       x1 = Agent.yRet + j*80;
       x2 = x1 + Agent.bRet;

	   //#DOTNET_EXCLUDE_BEGIN
       if((evt.getX() >= x1) && (evt.getX() <= x2) &&
	      (evt.getY() >= y1) && (evt.getY() <= y2)) {
	   //#DOTNET_EXCLUDE_END
	   /*#DOTNET_INCLUDE_BEGIN
	   if((evt.get_X() >= x1) && (evt.get_X() <= x2) &&
	      (evt.get_Y() >= y1) && (evt.get_Y() <= y2)) {
	   #DOTNET_INCLUDE_END*/
	     if (ag.agentName.equals("Other")) 
		 {
	        return null;
	     } else {
	        return ag;
	     }
       }
       j++;
     }
   } catch (ConcurrentModificationException cme) {
      //  Ignore - next repaint will correct things
   }
   return null;
 }


  /**
   * This method repaint both canvas checking the size of the scrollbars. The
   * right procedure to follow is to call method setPreferredSize() the revalidate()
   * method.
   */
  private void repaintBothCanvas() {
    MMCanvas c1 = panCan.canvAgent;
    MMCanvas c2 = panCan.canvMess;

    //#DOTNET_EXCLUDE_BEGIN
    panCan.setPreferredSize(new Dimension(horDim,c2.getVertDim()+50));
    c1.setPreferredSize(new Dimension(horDim,50));
    c2.setPreferredSize(new Dimension(horDim,c2.getVertDim()));
    /*panCan.setPreferredSize(new Dimension(horDim,vertDim+50));
    c1.setPreferredSize(new Dimension(horDim,50));
    c2.setPreferredSize(new Dimension(horDim,vertDim));*/
    panCan.revalidate();
    //#DOTNET_EXCLUDE_END
    /*#DOTNET_INCLUDE_BEGIN
	panCan.getPanel().Invalidate();
	#DOTNET_INCLUDE_END*/
    c1.repaint();
    c2.repaint();
  }

  public int getVertDim(){
	  return vertDim;
  }

  /**
   * Adds an agent to canvas agent then repaints it
   *
   * @param agent agent to be add
   */

 public void rAgfromNoSniffVector(Agent agent) {
  if (checkNoSniffedVector(agent)) {
   noSniffAgents.remove(positionAgent);
   repaintBothCanvas();
  }
 }

 public void addAgent (Agent agent) {
   al.addAgent(agent);
   repaintBothCanvas();
 }

  /**
   * Removes an agent from the canvas agent then repaints it
   *
   * @param agentName agent to be removed
   */
  public void removeAgent (String agentName) {
   try{
    al.removeAgent(agentName);
    repaintBothCanvas();
   }
   catch(Exception e) {}
  }

  /**
   * Removes all the agents and messages from their lists then repaints the canvas
   */
  public void removeAllAgents () {
    //#DOTNET_EXCLUDE_BEGIN
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
	al.removeAllAgents();
	ml.removeAllMessages();
	repaintBothCanvas();
      }
    });
    //#DOTNET_EXCLUDE_END
    /*#DOTNET_INCLUDE_BEGIN
	al.removeAllAgents();
	ml.removeAllMessages();
	#DOTNET_INCLUDE_END*/
  }

  // method to repaint the  NoSniffed agent

  public void repaintNoSniffedAgent(Agent agent) {
    if(!checkNoSniffedVector(agent)) noSniffAgents.add(agent);
    repaintBothCanvas();
  }

  /**
   * Adds a message to canvas message then repaints the canvas
   *
   * @param mess message to be added
   */

  public void addMessage (final Message mess) {
    //#DOTNET_EXCLUDE_BEGIN
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        ml.addMessage(mess);
        repaintBothCanvas();
      }
    });
    //#DOTNET_EXCLUDE_END
    /*#DOTNET_INCLUDE_BEGIN
	ml.addMessage(mess);
    repaintBothCanvas();
	#DOTNET_INCLUDE_END*/
  }

  /**
   * Removes all the messages in the message list then repaints the canvas
   */

 public void removeAllMessages() {
  try{
   ml.removeAllMessages();
   repaintBothCanvas();
  }
   catch (Exception e) {}
 }

  /**
   * Looks if an agent is present on Agent Canvas
   *
   * @param agName agent name to look for
   * @return true if agent is present, false otherwise
   */
  public boolean isPresent (String agName) {
    return al.isPresent(agName);
  }

  /**
   * Returns an handler to the agent list. The agent list contains all the agents
   * contained in the Agent Canvas displayed by grey or red boxes
   *
   * @return handler to agent list
   */
  public AgentList getAgentList() {
   return al;
  }

  /**
   * Returns an handler to the message list. The message list contains all
   * sniffed messages displayed on the Message Canavs as blue arrows
   *
   * @return handler to the message list
   */
 public MessageList getMessageList() {
  return ml;
 }

  /**
   * Set the agent list handler as the parameter passed then repaints the canvas
   *
   * @param savedList new list of agents
   */

 public void setAgentList(AgentList savedList) {
   al = savedList;
   repaintBothCanvas();
 }

  /**
   * Set the message list handler as the parameter passed then repaints the canvas
   *
   * @param savedList new list of messages
   */
 public void setMessageList(MessageList savedList) {
  ml = savedList;
  repaintBothCanvas();
 }	

  /** 
   * Returns new messages and put them into canvas agent 
   *
   * @param newMess new message
   */ 

  public void recMessage(Message newMess) {
   addMessage(newMess);
  }

  private String tail(int n, String s) {
      try {
	  return s.substring(s.length()-n,s.length());
      } catch (Exception any) {
	  return " ";
      }
  }


  /**
   * Trim off known prefixes.
   */
  private String nameClip(String aName) {
    String clipNames = mWnd.getProperties().getProperty("clip", null);
    if (clipNames == null) {
      return aName;
    }
    StringTokenizer parser = new StringTokenizer(clipNames, ";");
    while (parser.hasMoreElements()) {
      String clip = parser.nextToken();
      if (aName.startsWith(clip)) {
        return aName.substring(clip.length());
      }
    }
    return aName;
  }

  /*#DOTNET_INCLUDE_BEGIN
  	public void repaint()
	{
		myPanel.Refresh();
	}
  #DOTNET_INCLUDE_END*/

} 
