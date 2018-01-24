import java.io.*;
import java.text.*;
import java.util.*;
import javax.servlet.*;

/**
 *  Copyright (c) 2002 Free Software Foundation developed under the custody of
 *  the Open Web Application Security Project (http://www.owasp.org) This
 *  software package is published by OWASP under the GPL. You should read and
 *  accept the LICENSE before you use, modify and/or redistribute this software.
 *
 *@author     jwilliams@aspectsecurity.com
 *@created    November 6, 2002
 */
public class ParameterParser
{

	/**
	 *  Constructs a new ParameterParser to handle the parameters of the given
	 *  request.
	 *
	 *@param  request  the servlet request
	 */
	public ParameterParser( ServletRequest request )
	{
		this.request = request;
	}


	private final static String ALLOWED_CHARACTERS = "#$()-?.@!,:;&=";


	/**
	 *  Description of the Method
	 *
	 *@param  s  Description of the Parameter
	 *@return    Description of the Return Value
	 */
	private String clean( String s )
	{
		StringBuffer clean = new StringBuffer();
		for ( int loop = 0; loop < s.length(); loop++ )
		{
			char c = s.charAt( loop );
			if
				 (
				Character.isLetterOrDigit( c ) ||
				Character.isWhitespace( c ) ||
				( ALLOWED_CHARACTERS.indexOf( c ) != -1 )
				 )
			{
				clean.append( c );
			}
			else
			{
				clean.append( '.' );
			}
		}
		return ( clean.toString() );
	}


	/**
	 *  Gets the named parameter value as a boolean
	 *
	 *@param  name                            the parameter name
	 *@return                                 the parameter value as a boolean
	 *@exception  ParameterNotFoundException  if the parameter was not found
	 */
	public boolean getBooleanParameter( String name )
		 throws ParameterNotFoundException
	{
		return new Boolean( getStringParameter( name ) ).booleanValue();
	}


	/**
	 *  Gets the named parameter value as a boolean, with a default. Returns the
	 *  default value if the parameter is not found.
	 *
	 *@param  name  the parameter name
	 *@param  def   the default parameter value
	 *@return       the parameter value as a boolean, or the default
	 */
	public boolean getBooleanParameter( String name, boolean def )
	{
		try
		{
			return getBooleanParameter( name );
		}
		catch ( Exception e )
		{
			return def;
		}
	}


	/**
	 *  Gets the booleanSubParameter attribute of the ParameterParser object
	 *
	 *@param  first  Description of the Parameter
	 *@param  next   Description of the Parameter
	 *@param  def    Description of the Parameter
	 *@return        The booleanSubParameter value
	 */
	public boolean getBooleanSubParameter( String first, String next, boolean def )
	{
		try
		{
			return new Boolean( getSubParameter( first, next ) ).booleanValue();
		}
		catch ( Exception e )
		{
			return def;
		}
	}


	/**
	 *  Gets the named parameter value as a byte
	 *
	 *@param  name                            the parameter name
	 *@return                                 the parameter value as a byte
	 *@exception  ParameterNotFoundException  if the parameter was not found
	 *@exception  NumberFormatException       if the parameter value could not be
	 *      converted to a byte
	 */
	public byte getByteParameter( String name )
		 throws ParameterNotFoundException, NumberFormatException
	{
		return Byte.parseByte( getStringParameter( name ) );
	}


	/**
	 *  Gets the named parameter value as a byte, with a default. Returns the
	 *  default value if the parameter is not found or cannot be converted to a
	 *  byte.
	 *
	 *@param  name  the parameter name
	 *@param  def   the default parameter value
	 *@return       the parameter value as a byte, or the default
	 */
	public byte getByteParameter( String name, byte def )
	{
		try
		{
			return getByteParameter( name );
		}
		catch ( Exception e )
		{
			return def;
		}
	}


	/**
	 *  Gets the named parameter value as a char
	 *
	 *@param  name                            the parameter name
	 *@return                                 the parameter value as a char
	 *@exception  ParameterNotFoundException  if the parameter was not found or was
	 *      the empty string
	 */
	public char getCharParameter( String name )
		 throws ParameterNotFoundException
	{
		String param = getStringParameter( name );
		if ( param.length() == 0 )
		{
			throw new ParameterNotFoundException( name + " is empty string" );
		}
		else
		{
			return ( param.charAt( 0 ) );
		}
	}


	/**
	 *  Gets the named parameter value as a char, with a default. Returns the
	 *  default value if the parameter is not found.
	 *
	 *@param  name  the parameter name
	 *@param  def   the default parameter value
	 *@return       the parameter value as a char, or the default
	 */
	public char getCharParameter( String name, char def )
	{
		try
		{
			return getCharParameter( name );
		}
		catch ( Exception e )
		{
			return def;
		}
	}


	/**
	 *  Gets the classNameParameter attribute of the ParameterParser object
	 *
	 *@param  name  Description of the Parameter
	 *@return       The classNameParameter value
	 */
	public String getClassNameParameter( String name )
	{
		return getClassNameParameter( name, null );
	}


	// FIXME: check for [a-zA-Z].([a-zA-Z])*
	/**
	 *  Gets the classNameParameter attribute of the ParameterParser object
	 *
	 *@param  name  Description of the Parameter
	 *@param  def   Description of the Parameter
	 *@return       The classNameParameter value
	 */
	public String getClassNameParameter( String name, String def )
	{
		try
		{
			String p = getStringParameter( name );
			StringTokenizer st = new StringTokenizer( p );
			return ( st.nextToken().trim() );
		}
		catch ( Exception e )
		{
			return def;
		}
	}


	/**
	 *  Gets the named parameter value as a double
	 *
	 *@param  name                            the parameter name
	 *@return                                 the parameter value as a double
	 *@exception  ParameterNotFoundException  if the parameter was not found
	 *@exception  NumberFormatException       if the parameter could not be
	 *      converted to a double
	 */
	public double getDoubleParameter( String name )
		 throws ParameterNotFoundException, NumberFormatException
	{
		return new Double( getStringParameter( name ) ).doubleValue();
	}


	/**
	 *  Gets the named parameter value as a double, with a default. Returns the
	 *  default value if the parameter is not found.
	 *
	 *@param  name  the parameter name
	 *@param  def   the default parameter value
	 *@return       the parameter value as a double, or the default
	 */
	public double getDoubleParameter( String name, double def )
	{
		try
		{
			return getDoubleParameter( name );
		}
		catch ( Exception e )
		{
			return def;
		}
	}


	/**
	 *  Gets the named parameter value as a float
	 *
	 *@param  name                            the parameter name
	 *@return                                 the parameter value as a float
	 *@exception  ParameterNotFoundException  if the parameter was not found
	 *@exception  NumberFormatException       if the parameter could not be
	 *      converted to a float
	 */
	public float getFloatParameter( String name )
		 throws ParameterNotFoundException, NumberFormatException
	{
		return new Float( getStringParameter( name ) ).floatValue();
	}


	/**
	 *  Gets the named parameter value as a float, with a default. Returns the
	 *  default value if the parameter is not found.
	 *
	 *@param  name  the parameter name
	 *@param  def   the default parameter value
	 *@return       the parameter value as a float, or the default
	 */
	public float getFloatParameter( String name, float def )
	{
		try
		{
			return getFloatParameter( name );
		}
		catch ( Exception e )
		{
			return def;
		}
	}


	/**
	 *  Gets the named parameter value as an IP String, with a default. Returns the
	 *  default value if the parameter is not found or is the empty string.
	 *
	 *@param  name  the parameter name
	 *@param  def   the default parameter value
	 *@return       the parameter value as a String, or the default
	 */
	public String getIPParameter( String name, String def )
	{
		try
		{
			return getIPParameter( name );
		}
		catch ( Exception e )
		{
			return def;
		}
	}


	/**
	 *  Gets the named parameter value as an IP String
	 *
	 *@param  name                            the parameter name
	 *@return                                 the parameter value as a valid IP
	 *      String or an Empty string if invalid
	 *@exception  ParameterNotFoundException  if the parameter was not found or was
	 *      the empty string
	 */
	public String getIPParameter( String name )
		 throws ParameterNotFoundException
	{
		boolean valid = true;

		String[] values = request.getParameterValues( name );
		String value;
		if ( values == null )
		{
			throw new ParameterNotFoundException( name + " not found" );
		}
		else if ( values[0].length() == 0 )
		{
			throw new ParameterNotFoundException( name + " was empty" );
		}
		else
		{
			// trim illegal characters
			value = clean( values[0].trim() );
			if ( value.indexOf( "&" ) > 0 )
			{
				// truncate addtional parameters that follow &
				value = value.substring( 0, value.indexOf( "&" ) );
			}

			// validate the IP  ex: 124.143.12.254
			int startIndex = 0;
			int endIndex = 0;
			int octetCount = 0;
			int octetValue;
			String octet;

			// if no .'s then it's not an IP
			if ( value.indexOf( "." ) >= 0 )
			{
				while ( valid == true && octetCount < 4 )
				{
					endIndex = value.indexOf( ".", startIndex );
					if ( endIndex == -1 )
					{
						endIndex = value.length();
					}
					octet = value.substring( startIndex, endIndex );
					startIndex = endIndex + 1;
					try
					{
						octetValue = Integer.parseInt( octet );
						if ( octetValue <= 0 || octetValue >= 256 )
						{
							valid = false;
						}
					}
					catch ( Exception e )
					{
						valid = false;
					}
					octetCount++;
				}
			}
			else
			{
				// Not a valid IP
				valid = false;
			}

			// Check for any extra garbage. If the last octet was a large value
			// it would be trapped by the above range check.
			if ( value.length() != endIndex )
			{
				valid = false;
			}
			return valid ? value : null;
		}
	}


	/**
	 *  Gets the named parameter value as a int
	 *
	 *@param  name                            the parameter name
	 *@return                                 the parameter value as a int
	 *@exception  ParameterNotFoundException  if the parameter was not found
	 *@exception  NumberFormatException       if the parameter could not be
	 *      converted to a int
	 */
	public int getIntParameter( String name )
		 throws ParameterNotFoundException, NumberFormatException
	{
		return Integer.parseInt( getStringParameter( name ) );
	}


	/**
	 *  Gets the named parameter value as a int, with a default. Returns the
	 *  default value if the parameter is not found.
	 *
	 *@param  name  the parameter name
	 *@param  def   the default parameter value
	 *@return       the parameter value as a int, or the default
	 */
	public int getIntParameter( String name, int def )
	{
		try
		{
			return getIntParameter( name );
		}
		catch ( Exception e )
		{
			return def;
		}
	}


	/**
	 *  Gets the named parameter value as a long
	 *
	 *@param  name                            the parameter name
	 *@return                                 the parameter value as a long
	 *@exception  ParameterNotFoundException  if the parameter was not found
	 *@exception  NumberFormatException       if the parameter could not be
	 *      converted to a long
	 */
	public long getLongParameter( String name )
		 throws ParameterNotFoundException, NumberFormatException
	{
		return Long.parseLong( getStringParameter( name ) );
	}


	/**
	 *  Gets the named parameter value as a long, with a default. Returns the
	 *  default value if the parameter is not found.
	 *
	 *@param  name  the parameter name
	 *@param  def   the default parameter value
	 *@return       the parameter value as a long, or the default
	 */
	public long getLongParameter( String name, long def )
	{
		try
		{
			return getLongParameter( name );
		}
		catch ( Exception e )
		{
			return def;
		}
	}


	/**
	 *  Determines which of the required parameters were missing from the request.
	 *  Returns null if all the parameters are present.
	 *
	 *@param  requestuired  Description of the Parameter
	 *@return               an array of missing parameters, or null if none are
	 *      missing
	 */
	public String[] getMissingParameters( String[] requestuired )
	{
		Vector missing = new Vector();
		for ( int i = 0; i < requestuired.length; i++ )
		{
			String val = getStringParameter( requestuired[i], null );
			if ( val == null )
			{
				missing.addElement( requestuired[i] );
			}
		}
		if ( missing.size() == 0 )
		{
			return null;
		}
		else
		{
			String[] ret = new String[missing.size()];
			missing.copyInto( ret );
			return ret;
		}
	}


	/**
	 *  Gets the parameterNames attribute of the ParameterParser object
	 *
	 *@return    The parameterNames value
	 */
	public Enumeration getParameterNames()
	{
		if ( request == null )
		{
			return ( null );
		}
		return request.getParameterNames();
	}


	/**
	 *  Gets the parameterValues attribute of the ParameterParser object
	 *
	 *@param  name  Description of the Parameter
	 *@return       The parameterValues value
	 */
	public String[] getParameterValues( String name )
	{
		if ( request == null )
		{
			return ( null );
		}
		return request.getParameterValues( name );
	}


	/**
	 *  Gets the rawParameter attribute of the ParameterParser object
	 *
	 *@param  name  Description of the Parameter
	 *@param  def   Description of the Parameter
	 *@return       The rawParameter value
	 */
	public String getRawParameter( String name, String def )
	{
		try
		{
			return getRawParameter( name );
		}
		catch ( Exception e )
		{
			return def;
		}
	}


	/**
	 *  Gets the rawParameter attribute of the ParameterParser object
	 *
	 *@param  name                            Description of the Parameter
	 *@return                                 The rawParameter value
	 *@exception  ParameterNotFoundException  Description of the Exception
	 */
	public String getRawParameter( String name )
		 throws ParameterNotFoundException
	{
		String[] values = request.getParameterValues( name );
		if ( values == null )
		{
			throw new ParameterNotFoundException( name + " not found" );
		}
		else if ( values[0].length() == 0 )
		{
			throw new ParameterNotFoundException( name + " was empty" );
		}
		return ( values[0] );
	}


	/**
	 *  Gets the named parameter value as a short
	 *
	 *@param  name                            the parameter name
	 *@return                                 the parameter value as a short
	 *@exception  ParameterNotFoundException  if the parameter was not found
	 *@exception  NumberFormatException       if the parameter could not be
	 *      converted to a short
	 */
	public short getShortParameter( String name )
		 throws ParameterNotFoundException, NumberFormatException
	{
		return Short.parseShort( getStringParameter( name ) );
	}


	/**
	 *  Gets the named parameter value as a short, with a default. Returns the
	 *  default value if the parameter is not found.
	 *
	 *@param  name  the parameter name
	 *@param  def   the default parameter value
	 *@return       the parameter value as a short, or the default
	 */
	public short getShortParameter( String name, short def )
	{
		try
		{
			return getShortParameter( name );
		}
		catch ( Exception e )
		{
			return def;
		}
	}


	/**
	 *  Gets the named parameter value as a String
	 *
	 *@param  name                            the parameter name
	 *@return                                 the parameter value as a String
	 *@exception  ParameterNotFoundException  if the parameter was not found or was
	 *      the empty string
	 */
	public String getStringParameter( String name )
		 throws ParameterNotFoundException
	{
		String[] values = request.getParameterValues( name );
		String value;
		if ( values == null )
		{
			throw new ParameterNotFoundException( name + " not found" );
		}
		else if ( values[0].length() == 0 )
		{
			throw new ParameterNotFoundException( name + " was empty" );
		}
		else
		{
			// trim illegal characters
			value = clean( values[0].trim() );
			if ( value.indexOf( "&" ) > 0 )
			{
				// truncate addtional parameters that follow &
				value = value.substring( 0, value.indexOf( "&" ) );
			}
			return value;
		}
	}


	/**
	 *  Gets the named parameter value as a String, with a default. Returns the
	 *  default value if the parameter is not found or is the empty string.
	 *
	 *@param  name  the parameter name
	 *@param  def   the default parameter value
	 *@return       the parameter value as a String, or the default
	 */
	public String getStringParameter( String name, String def )
	{
		try
		{
			return getStringParameter( name );
		}
		catch ( Exception e )
		{
			return def;
		}
	}


	/**
	 *  Gets the subParameter attribute of the ParameterParser object
	 *
	 *@param  first  Description of the Parameter
	 *@param  next   Description of the Parameter
	 *@param  def    Description of the Parameter
	 *@return        The subParameter value
	 */
	public String getSubParameter( String first, String next, String def )
	{
		try
		{
			return getSubParameter( first, next );
		}
		catch ( Exception e )
		{
			return def;
		}
	}


	/**
	 *  Gets the parameter named 'next' following the parameter 'first'. Presumes
	 *  the structure: first=firstvalue&next=nextValue
	 *
	 *@param  first                           Description of the Parameter
	 *@param  next                            Description of the Parameter
	 *@return                                 The subParameter value
	 *@exception  ParameterNotFoundException  Description of the Exception
	 */
	public String getSubParameter( String first, String next )
		 throws ParameterNotFoundException
	{
		String[] values = request.getParameterValues( first );
		String value;

		if ( values == null )
		{
			throw new ParameterNotFoundException( first + " not found" );
		}
		else if ( values[0].length() == 0 )
		{
			throw new ParameterNotFoundException( first + " was empty" );
		}
		else
		{
			value = clean( values[0].trim() );

			int idx = value.indexOf( "&" ) + 1;
			// index of first char of first sub-param name
			if ( idx == 0 )
			{
				throw new ParameterNotFoundException( "No subparameter key" );
			}

			value = value.substring( idx );

//System.out.println("= = = = = =Parameter parser looking for " + next + " in " + value );
			int nextValueIndex = value.indexOf( next + "=" );
//System.out.println("= = = = = =Parameter parser nextValueIndex = " + nextValueIndex );
			if ( nextValueIndex < 0 )
			{
				throw new ParameterNotFoundException( "No subparameter value" );
			}

			nextValueIndex += next.length() + 1;
			if ( nextValueIndex >= 0 )
			{
				value = value.substring( nextValueIndex );
			}
			else
			{
				throw new ParameterNotFoundException( next + " not found" );
			}
		}
		if ( value.indexOf( "&" ) > 0 )
		{
			// truncate addtional parameters that follow &
			value = value.substring( 0, value.indexOf( "&" ) );
		}
//System.out.println("=-=-=-=-=ParameterParser returning value " + value );
		return value;
	}


	/**
	 *  Gets the wordParameter attribute of the ParameterParser object
	 *
	 *@param  name  Description of the Parameter
	 *@return       The wordParameter value
	 */
	public String getWordParameter( String name )
	{
		return getWordParameter( name, null );
	}


	// FIXME: check for [a-zA-Z]
	/**
	 *  Gets the wordParameter attribute of the ParameterParser object
	 *
	 *@param  name  Description of the Parameter
	 *@param  def   Description of the Parameter
	 *@return       The wordParameter value
	 */
	public String getWordParameter( String name, String def )
	{
		try
		{
			String p = getStringParameter( name );
			StringTokenizer st = new StringTokenizer( p );
			return ( st.nextToken().trim() );
		}
		catch ( Exception e )
		{
			return def;
		}
	}


	private ServletRequest request;


	/**
	 *  Description of the Method
	 *
	 *@return    Description of the Return Value
	 */
	public String toString()
	{
		StringBuffer s = new StringBuffer( "[" );

		Enumeration e = getParameterNames();
		while ( e.hasMoreElements() )
		{
			String key = (String) e.nextElement();
			s.append( key + "=" + getParameterValues( key )[0] );
			// FIXME: Other values?
			if ( e.hasMoreElements() )
			{
				s.append( "," );
			}
		}
		s.append( "]" );
		return ( s.toString() );
	}


	/**
	 *  Description of the Method
	 *
	 *@param  request  Description of the Parameter
	 */
	public void update( ServletRequest request )
	{
		this.request = request;
	}
}

