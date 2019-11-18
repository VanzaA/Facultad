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

package jade.gui;

//#APIDOC_EXCLUDE_FILE
//#J2ME_EXCLUDE_FILE

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.TreeSelectionModel;
import java.net.URL;
import java.io.IOException;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JDialog;
import javax.swing.JPanel;
import java.awt.*;
import java.awt.event.*;

/*
This class provide the help for the GUI of the DF
@author Tiziana Trucco - CSELT S.p.A.
@version $Date: 2003-11-20 11:55:37 +0100 (gio, 20 nov 2003) $ $ Revision: 1.3 $

 */

public class TreeHelp extends JDialog {
    
	  /**
    @serial
	  */
	  private JEditorPane htmlPane;
    
	  /**
    @serial
	  */
    private URL helpURL;

    //Optionally play with line styles.  Possible values are
    //"Angled", "Horizontal", and "None" (the default).
    /**
    @serial
	  */
    private boolean playWithLineStyle = false;
    /**
    @serial
	  */
    private String lineStyle = "Angled"; 

    public TreeHelp(Frame owner, String title, String url) {
  
        super(owner,title);
        //setTitle(title);
        // added for reply to window closing
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                disposeAsync();
            }
        });
        setHTMLText(url);
        	

    }
    
    public TreeHelp(Dialog owner, String title, String url) {
  
        super(owner,title);
        //setTitle(title);
        // added for reply to window closing
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                disposeAsync();
            }
        });
        
       
        setHTMLText(url);
        	
    }
    

    private void setHTMLText(String url)
    {
        JPanel main = new JPanel();
        
        main.setLayout(new BorderLayout());
        
        //Create the HTML viewing pane.
        htmlPane = new JEditorPane();
        htmlPane.setEditable(false);
        htmlPane.setPreferredSize(new Dimension(500,300));
        JScrollPane htmlView = new JScrollPane(htmlPane);
	      

    		try {
            htmlPane.setPage(getClass().getResource(url));
        } catch (IOException e) {
            System.err.println("Attempted to read a bad URL");
        }
    
        main.add(htmlView, BorderLayout.CENTER);
        setSize(500,500);
        try{
        	int x = getOwner().getX() + (getOwner().getWidth() - getWidth()) / 2;
        	int y = getOwner().getY() + (getOwner().getHeight() - getHeight()) / 2;
          setLocation( x>0 ? x:0, y>0 ? y:0);
        }catch(Exception e){}
        getContentPane().add(main, BorderLayout.CENTER);
        
    }

    

    
    public void disposeAsync() {

    class disposeIt implements Runnable {
      private Window toDispose;

      public disposeIt(Window w) {
			toDispose = w;
      }

      public void run() {
			toDispose.dispose();
      }

    }
    
    EventQueue.invokeLater(new disposeIt(this));

  }


    
}
