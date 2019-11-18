package jade.content;

import jade.util.WrapperException;

/**
 * Base class for OntologyException and CodecException
 */
public class ContentException extends WrapperException {
    public ContentException(String message) {
        super(message);
    }
    
    public ContentException(String message, Throwable t) {
        super(message, t);
    }
}
