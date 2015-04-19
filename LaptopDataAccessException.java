/**
 * Created by admin on 4/14/15.
 */

/** Used to wrap SQL exceptions and other problems from the Model class - in out program it's a Derby database,
 * but any other data source could throw these exceptions. */


public class LaptopDataAccessException extends RuntimeException {


    //Call superclass constructor
    public LaptopDataAccessException(String message) {
        super(message);
    }

    //Call superclass constructor - this version allows our exception to contain a reference to another exception - most likely a SQLException
    public LaptopDataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
