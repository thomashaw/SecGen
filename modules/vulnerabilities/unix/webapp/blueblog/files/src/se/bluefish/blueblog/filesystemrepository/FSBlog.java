package se.bluefish.blueblog.filesystemrepository;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import se.bluefish.blueblog.BBException;
import se.bluefish.blueblog.blog.AbstractBlog;
import se.bluefish.blueblog.blog.BlogData;
import se.bluefish.blueblog.blog.Category;
import se.bluefish.blueblog.formatters.Formatter;

/**
 * A weblog entry, represented by a file in the file system. The first line
 * in the file is the blog title, and the rest is the content. If the file 
 * contains a line which is equals to the property {@link #PROPERTIES_SEPARATOR}, 
 * the rest of the file after that line is regarded as a properties file.
 * <p>
 * Possible properties are:
 * <ul>
 * <li><b>created</b> - if specified, this date/time is used as the "created"
 * property for this instance. Otherwise, created is same as the lastUpdated 
 * property.</li>
 * <li><b>publish</b> - if specified, this date/time is used as the "publish"
 * property for this instance. Otherwise, publish is same as the lastUpdated 
 * property.</li>
 * </ul>
 * The date/time format for the properties part of the file is "yyyy-MM-dd HH:mm:ss"
 * as specified by the java.text.SimpleDateFormat class.
 * 
 * @see java.text.SimpleDateFormat
 * 
 * @author Robert Burén
 *
 */
class FSBlog extends AbstractBlog {
	/**
	 * String defining the date format to be used in the blog text
	 * files for the date properties (created, publish, updated) 
	 * of this Blog.<br/><br/>
	 * Value is <b>"yyyy-MM-dd HH:mm:ss"</b>
	 * 
	 * @see java.text.DateFormat
	 */
	public static final String DATEFORMAT="yyyy-MM-dd HH:mm:ss";
	
	/**
	 * String used on a line in the blog file to separate the
	 * blog content with the properties.<br/><br/>
	 * Value is <b>"--"</b>
	 */
	public static final String PROPERTIES_SEPARATOR="--";

	/**
	 * String label of the "created" date/time property, when
	 * stored in the blog file.<br/><br/>
	 * Value is <b>"created"</b>
	 * 
	 * @see #getCreatedDate()
	 */
	public static final String PROPERTY_CREATED_DATE="created";

	/**
	 * String label of the "publish" date/time property, when
	 * stored in the blog file.<br/><br/>
	 * Value is <b>"publish"</b>
	 * 
	 * @see #getPublishDate()
	 */
	public static final String PROPERTY_PUBLISH_DATE="publish";

	/**
	 * Creates a new FSBlog instance, including an actual file
	 * in the file system. Main user of this method is the 
	 * FSCategory.addNewBlog() method.
	 *  
	 * @param category
	 * @param blogData
	 * @param formatter
	 * @return the newly created Blog instance
	 * @throws BBException if the Blog couldn't be created
	 * 
	 * @see FSCategory#addNewBlog(BlogData)
	 */
	static FSBlog createNewBlog(FSCategory category, BlogData blogData, Formatter formatter)
		throws BBException 
	{
		String fileEnding = null;
		if( blogData.getBlogType().equals("text/plain") ) {
			fileEnding = ".txt";
		} else if( blogData.getBlogType().equals("text/html") ) {
			fileEnding = ".html";
		} else {
			throw new BBException("Can't create new blogs with blogType: "+blogData.getBlogType());
		}

		String filename = blogData.getSuggestedId();
		File categoryDirectory = category.getCategoryDirectory();
		File file = new File(categoryDirectory, filename+fileEnding);
		if( file.exists() ) {
			// we need to try another filename
			String filenameBase = filename.length() < 27 ? filename : filename.substring(0,27);
			
			int i = 1;
			filename = filenameBase + i + fileEnding;
			file = new File(categoryDirectory, filename);
			while( file.exists() ) {
				++i;
				filename = filenameBase + i + fileEnding;
				file = new File(categoryDirectory, filename);
			}
		}
		if( file.exists() ) {
			throw new BBException("Couldn't find a unique filename based on \""+blogData.getSuggestedId()+"\"");
		}
		
		// ok, now we have a File. Time to save data in it.
		// first we make sure that the title is only one line
		if( blogData.getTitle().indexOf('\n') >= 0 ) {
			throw new BBException("BlogData has an invalid value of title property: contains a newline");
		}
		
		DateFormat df = new SimpleDateFormat(FSBlog.DATEFORMAT);
		try {
			PrintWriter out = new PrintWriter(new FileWriter(file));
			out.println(blogData.getTitle());
			out.println(blogData.getContent());
			out.println(FSBlog.PROPERTIES_SEPARATOR);
			if( blogData.getCreated() != null ) {
				out.print(FSBlog.PROPERTY_CREATED_DATE+"=");
				out.println(df.format(blogData.getCreated()));
			}
			if( blogData.getPublish() != null ) {
				out.print(FSBlog.PROPERTY_PUBLISH_DATE+"=");
				out.println(df.format(blogData.getPublish()));
			}
			out.close();
			return new FSBlog(category, file, formatter);
		} catch (IOException e) {
			throw new BBException(e);
		}
	}

	private static StringBuffer fixLocalLinks(String text, String regex, String prefix) {
		Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
		Matcher matcher = p.matcher(text);
		StringBuffer sb = new StringBuffer();
		while( matcher.find() ) {
			String link = matcher.group(2);
			if( link.indexOf('/') < 0 && !link.startsWith("mailto:") ) {
				// the link is local to the same dir -- we need to modify it
				matcher.appendReplacement(sb, matcher.group(1)+prefix+link+matcher.group(3));
			} else {
				// the link is either not relative, or at least not local to the
				// same dir. we let it pass unchanged.
				// TODO actually, it would be good to fix non-local, relative URLs as well!
				matcher.appendReplacement(sb, matcher.group());
			}
		}
		matcher.appendTail(sb);
		return sb;
	}
	
	private FSCategory belongsTo;
	private File blogFile;
	private String blogId;
	private String content;
	private Date created;
	private String formattedContent;
	private String formattedTitle;
	private Date publish;
	private String title;
	private Date updated;

	/**
	 * Constructor.
	 * 
	 * @param cat Category this blog belongs to
	 * @param file file on the filesystem that the blog contents is in
	 * @param formatter Formatter to be used when getting the formatted* properties
	 * @throws IOException if there is a problem reading the file from the filesystem
	 */
	public FSBlog(FSCategory cat, File file, Formatter formatter) throws IOException {
		belongsTo = cat;
		blogFile = file;
		BufferedReader in=null;
		BufferedInputStream instr=null;
		FileInputStream fis=null;
		try {
			updated = new Date(file.lastModified());
			created = updated;
			publish = updated;
			fis = new FileInputStream(file);
			in = new BufferedReader(new InputStreamReader(fis));
			
			
			// First line is the title
			title = in.readLine();
			if( title == null ) {
				throw new EOFException("File "+file.getPath()+" is empty.");
			}
			title = title.trim();
			StringBuffer contentBuffer = new StringBuffer();
			String line = in.readLine();
			while( line != null ) {
				contentBuffer.append(line);
				contentBuffer.append('\n');
				line = in.readLine();
				if( line != null && line.equals(PROPERTIES_SEPARATOR) ) {
					StringBuffer propbuff=new StringBuffer();
					line=in.readLine();
					while( line != null ) {
						propbuff.append(line);
						propbuff.append('\n');
						line=in.readLine();
					}
					readBlogProperties(propbuff);
				}
			}
			content = contentBuffer.toString().trim();
			if( formatter != null ) {
				formattedContent = formatter.formatBlogContent(content); //formattedContentBuffer.toString();
				formattedTitle = formatter.formatBlogTitle(title);
			} else {
				formattedContent = content;
				formattedTitle = title;
			}
			fixLocalLinksInFormattedText();
			String fileName = blogFile.getName();
			blogId = fileName.substring(0,fileName.lastIndexOf('.'));
		} finally {
			if( in != null ) {
				in.close();
			}
		}
	}

	/**
	 * Search for local links, ie &lt;a href="filename">, where
	 * the filename doesn't include a '/' character, and fix the
	 * href value to point to a file in the same dir as this Blog.
	 * 
	 * @return the fixed text, or the unchanged text if no fixes were made
	 */
	private String fixLocalLinksIn(String text) {
		String prefix = getCategory().getCompleteUrl();
		
		// first pass: fix <a href="localresource"> links
		String hrefRegex = "(<a.*?href=\")(.+?)(\".*?>)";
		StringBuffer sb = fixLocalLinks(text, hrefRegex, prefix);
		
		// second pass: fix <img src="localresource"> links
		String imgRegex = "(<img.*?src=\")(.+?)(\".*?>)";
		sb = fixLocalLinks(sb.toString(), imgRegex, prefix);
		
		return sb.toString();
	}

	private void fixLocalLinksInFormattedText() {
		formattedContent = fixLocalLinksIn( formattedContent );
		formattedTitle = fixLocalLinksIn( formattedTitle );
	}

	/**
	 * FSBlog instances uses the file name of the underlying file
	 * (excluding file extension) as blogId.
	 * 
	 * @see se.bluefish.blueblog.blog.Blog#getBlogId()
	 */
	public String getBlogId() {
		return blogId;
	}

	/**
	 * Returns the Category that this Blog belongs to.
	 */
	public Category getCategory() {
		return belongsTo;
	}

	/**
	 * @see se.bluefish.blueblog.blog.Blog#getCreatedDate()
	 */
	public Date getCreatedDate() {
		return created;
	}

	/**
	 * @see se.bluefish.blueblog.blog.Blog#getFormattedContent()
	 */
	public String getFormattedContent() {
		return formattedContent;
	}

	/**
	 * @see se.bluefish.blueblog.blog.AbstractBlog#getFormattedTitle()
	 */
	public String getFormattedTitle() {
		return formattedTitle;
	}

	/**
	 * @see se.bluefish.blueblog.blog.Blog#getLastUpdated()
	 */
	public Date getLastUpdated() {
		return updated;
	}

	/**
	 * @see se.bluefish.blueblog.blog.Blog#getPublishDate()
	 */
	public Date getPublishDate() {
		return publish;
	}

	/**
	 * @see se.bluefish.blueblog.blog.Blog#getRawContent()
	 */
	public String getRawContent() {
		return content;
	}


	/**
	 * @see se.bluefish.blueblog.blog.Blog#getRawTitle()
	 */
	public String getRawTitle() {
		return title;
	}

	private void readBlogProperties(StringBuffer instr) throws IOException  {
		DateFormat df=new SimpleDateFormat(DATEFORMAT);
		Properties props=new Properties();
		try {
			ByteArrayInputStream instream=new ByteArrayInputStream(instr.toString().getBytes("ISO-8859-1"));
			props.load(instream);
			instream.close();
		} catch (UnsupportedEncodingException e2) {
			ByteArrayInputStream instream=new ByteArrayInputStream(instr.toString().getBytes());
			props.load(instream);
			instream.close();
		}
		try {
			created = df.parse(props.getProperty(PROPERTY_CREATED_DATE, df.format(updated)));
		} catch (ParseException e) {
			// do nothing -- created already initialized
		}
		try {
			publish = df.parse(props.getProperty(PROPERTY_PUBLISH_DATE, df.format(updated)));
		} catch (ParseException e1) {
			// do nothing -- publish already initialized
		}
	}

}
