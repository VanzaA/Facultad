// Oliver Hoffmann 10 May 2001 oliver@hoffmann.org
// in collaboration with Fabio Bellifemine and Ernest Friedmann-Hill
package examples.JadeJessProtege;
import jess.*;
import java.util.Hashtable;
public class JessHashtable // for storing the running JESS engine
  {
  private static Hashtable jessHashtable = new Hashtable();
  public static void setRete (String key, Rete rete) // intended to be used by JESS to store a pointer to itself
    {
    jessHashtable.put(key,rete);
    }
  public static Rete getRete (String key) // intended to be used by JADE behaviours to retrieve the pointer to JESS
    {
    return (Rete) jessHashtable.get(key);
    }
  }
