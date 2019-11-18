package jade.domain.FIPAAgentManagement;

/**
   This interface contains all the string constants for frame and slot
   names of exceptions defined in the
   <code>fipa-agent-management</code> ontology.
*/
public interface ExceptionVocabulary {
	
  /**
    A symbolic constant, containing the name of this ontology.
  */
  public static final String NAME = "Exception";

  // Not-understood Exception Predicates
  public static final String UNSUPPORTEDACT = "unsupported-act";
  public static final String UNSUPPORTEDACT_ACT = "act";	
  
  public static final String UNEXPECTEDACT = "unexpected-act";
  public static final String UNEXPECTEDACT_ACT = "act";
  
  public static final String UNSUPPORTEDVALUE = "unsupported-value";
  public static final String UNSUPPORTEDVALUE_VALUE = "value";
    
  public static final String UNRECOGNISEDVALUE = "unrecognised-value";
  public static final String UNRECOGNISEDVALUE_VALUE = "value";	
  
  // Refusal Exception Predicates
  public static final String UNAUTHORISED = "unauthorised";
  
  public static final String UNSUPPORTEDFUNCTION = "unsupported-function";
  public static final String UNSUPPORTEDFUNCTION_FUNCTION = "function";
  
  public static final String MISSINGARGUMENT = "missing-argument";
  public static final String MISSINGARGUMENT_ARGUMENT = "argument-name";
  
  public static final String UNEXPECTEDARGUMENT = "unexpected-argument";
  public static final String UNEXPECTEDARGUMENT_ARGUMENT = "argument-name";
  
  public static final String UNEXPECTEDARGUMENTCOUNT = "unexpected-argument-count";

  public static final String MISSINGPARAMETER = "missing-parameter";
  public static final String MISSINGPARAMETER_OBJECT_NAME    = "object-name";	
  public static final String MISSINGPARAMETER_PARAMETER_NAME = "parameter-name";

  public static final String UNEXPECTEDPARAMETER = "unexpected-parameter";
  public static final String UNEXPECTEDPARAMETER_OBJECT_NAME = "object-name";
  public static final String UNEXPECTEDPARAMETER_PARAMETER_NAME = "parameter-name";
  
  public static final String UNRECOGNISEDPARAMETERVALUE = "unrecognised-parameter-value";
  public static final String UNRECOGNISEDPARAMETERVALUE_PARAMETER_NAME = "parameter-name";
  public static final String UNRECOGNISEDPARAMETERVALUE_PARAMETER_VALUE = "parameter-value";

  // Failure Exception Predicates
  public static final String INTERNALERROR = "internal-error";  
  public static final String INTERNALERROR_MESSAGE = "error-message";	

}
