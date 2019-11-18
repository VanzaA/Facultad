// Oliver Hoffmann 10 May 2001 oliver@hoffmann.org
// in collaboration with Fabio Bellifemine and Ernest Friedmann-Hill
package examples.JadeJessProtege;
import jess.*;
import jade.core.*;

public class JadeJessAgent extends jade.core.Agent // shows how Protege, JESS and JADE can be combined
  {
  private Rete jess;
  public Rete getRete ()
    {
    return jess;
    }
  public void setArguments(String arguments[]) // get the first argument passed to this Agent as the key to get the Rete engine
    {
	  jess = JessHashtable.getRete(arguments[0]); // JESS should have stored a java pointer to itself there
    } // end setArguments method
  protected void setup() // is called when agent is started
    {
	  System.err.println("JadeJessAgent: jess: "+jess);
  	if (jess == null)
  	  {
  	  System.err.println("JadeJessAgent: no JESS engine provided, will make a new one");
      jess = new Rete();
  		}
    try
      {
      jess.store("AGENT",this);
    	jess.executeCommand("(assert (Agent (fetch AGENT)))");
      }
    catch (Exception exception)
      {
      exception.printStackTrace();
      }
    try
      {
      addBehaviour(new JadeJessBehaviour(this));
      }
    catch (Exception exception)
      {
      exception.printStackTrace();
      }
    } // end setup method
  } // end JadeJessAgent class
