package exceptions;

public class DriverAlreadyExistsException extends Exception {
	 public DriverAlreadyExistsException()
	  {
	    super();
	  }
	  /**This exception is triggered if the question already exists 
	  *@param s String of the exception
	  */
	  public DriverAlreadyExistsException(String s)
	  {
	    super(s);
	  }
}
