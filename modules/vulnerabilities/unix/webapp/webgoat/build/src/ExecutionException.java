
/**
 *  Copyright (c) 2002 Free Software Foundation developed under the custody of
 *  the Open Web Application Security Project (http://www.owasp.org) This
 *  software package is published by OWASP under the GPL. You should read and
 *  accept the LICENSE before you use, modify and/or redistribute this software.
 *
 *@author     jwilliams@aspectsecurity.com
 *@created    November 6, 2002
 */
public class ExecutionException extends Exception
{

	/**
	 *  Constructor for the ExecutionException object
	 */
	public ExecutionException()
	{
		super();
	}


	/**
	 *  Constructor for the ExecutionException object
	 *
	 *@param  msg  Description of the Parameter
	 */
	public ExecutionException( String msg )
	{
		super( msg );
	}
}

