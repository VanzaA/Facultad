package jade.domain.DFGUIManagement;

import jade.domain.FIPAAgentManagement.FIPAManagementVocabulary;

/**
   
   @author Elisabetta Cortese - TiLab S.p.A.
   @version $Date: 2003-08-26 11:15:34 +0200 (mar, 26 ago 2003) $ $Revision: 4243 $
*/

public interface DFAppletVocabulary extends FIPAManagementVocabulary {
	
  /**
    A symbolic constant, containing the name of this ontology.
   */
  public static final String NAME = "DFApplet-Management";


  // Action
  public static final String GETDESCRIPTION= "getdescription";

  //public static final String FEDERATEWITH = "federatewith";
  public static final String FEDERATE = "federate";
  //public static final String FEDERATEWITH_PARENTDF = "parentdf";
  public static final String FEDERATE_DF = "df";
  //public static final String FEDERATEWITH_CHILDRENDF = "childrendf";
  public static final String FEDERATE_DESCRIPTION = "description";

  public static final String REGISTERWITH = "registerwith";
  public static final String REGISTERWITH_DF = "df";
  public static final String REGISTERWITH_DESCRIPTION = "description";

  public static final String DEREGISTERFROM = "deregisterfrom";
  //public static final String DEREGISTERFROM_PARENTDF = "parentdf";
  public static final String DEREGISTERFROM_DF = "df";
  //public static final String DEREGISTERFROM_CHILDRENDF = "childrendf";
  public static final String DEREGISTERFROM_DESCRIPTION = "description";

  public static final String MODIFYON = "modifyon";
  public static final String MODIFYON_DF = "df";
  public static final String MODIFYON_DESCRIPTION = "description";

  public static final String SEARCHON = "searchon";
  public static final String SEARCHON_DF = "df";
  public static final String SEARCHON_DESCRIPTION = "description";
  public static final String SEARCHON_CONSTRAINTS = "constraints";

  public static final String GETPARENTS = "getparents";

  public static final String GETDESCRIPTIONUSED = "getdescriptionused";
  public static final String GETDESCRIPTIONUSED_PARENTDF = "parentdf";

}
