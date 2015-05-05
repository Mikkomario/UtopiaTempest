package tempest_io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import flow_recording.AbstractConstructor;
import flow_recording.IDGenerator;

/**
 * This is a static collection of methods that may help in reading and writing Json
 * @author Mikko Hilpinen
 * @since 5.5.2015
 */
public class JsonIOAccessor
{
	/*
	 * http://wiki.fasterxml.com/JacksonHome
	 * http://fasterxml.github.io/jackson-core/javadoc/2.5/
	 * http://wiki.fasterxml.com/JacksonInFiveMinutes
	 * http://en.wikipedia.org/wiki/JavaBeans
	 * http://en.wikipedia.org/wiki/Serialization
	 */
	
	// CONSTRUCTOR	----------------------------
	
	private JsonIOAccessor()
	{
		// The interface is static
	}

	
	// OTHER METHODS	------------------------
	
	/**
	 * Creates a new json writer
	 * @param targetStream The stream where the data is written
	 * @return A new jsonGenerator
	 * @throws IOException if the generator couldn't be created
	 */
	public static JsonGenerator createWriter(OutputStream targetStream) throws IOException
	{
		JsonFactory f = new JsonFactory();
		return f.createGenerator(targetStream, JsonEncoding.UTF8);
	}
	
	/**
	 * Closes a json writer
	 * @param writer The writer that will be closed
	 */
	public static void closeWriter(JsonGenerator writer)
	{
		if (writer != null)
		{
			try
			{
				writer.flush();
				writer.close();
			}
			catch (IOException e)
			{
				System.err.println("Failed to close a jsonGenerator");
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Creates a new json reader
	 * @param stream The stream where the data is read from
	 * @return A new json parser
	 * @throws JsonParseException If the reader couldn't be created
	 * @throws IOException If the reader couldn't be created
	 */
	public static JsonParser createReader(InputStream stream) throws JsonParseException, IOException
	{
		JsonFactory f = new JsonFactory();
		return f.createParser(stream);
	}
	
	/**
	 * Closes a json parser
	 * @param reader The json parser that will be closed
	 */
	public static void closeReader(JsonParser reader)
	{
		if (reader != null)
		{
			try
			{
				reader.close();
			}
			catch (IOException e)
			{
				System.err.println("Failed to close a jsonParser");
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Instructs an objectConstructor in creating a set of objects from json data
	 * @param reader The json parser that goes through the json data
	 * @param constructor The constructor that will construct the objects
	 * @param instructionIndicator The string that defines if a field should be read as an 
	 * instruction (the field name should start with the indicator)
	 * @throws JsonParseException If the parsing failed
	 * @throws IOException If the operation failed
	 */
	public static void instructConstructor(JsonParser reader, 
			AbstractConstructor<?> constructor, String instructionIndicator) throws 
			JsonParseException, IOException
	{
		// Depth 1 = ready to receive a new object, 2 = inside an object, 0 = outside document
		int depth = 1;
		boolean newObjectIntroduced = false;
		String lastObjectName = null;
		
		while (depth > 0)
		{
			JsonToken token = reader.nextToken();
			if (token == null)
				break;
			
			// Start object means that a new object is introduced
			if (token == JsonToken.START_OBJECT)
			{
				if (newObjectIntroduced)
				{
					depth ++;
					constructor.create(lastObjectName);
					newObjectIntroduced = false;
				}
			}
			// End object means that an object was finished
			else if (token == JsonToken.END_OBJECT)
				depth --;
			// A new field means either an attribute, a link, an instruction or a new object
			else if (token == JsonToken.FIELD_NAME)
			{
				String name = reader.getCurrentName();
				
				if (name.startsWith(IDGenerator.ID_INDICATOR))
				{
					newObjectIntroduced = true;
					lastObjectName = name;
				}
			}
			// If the last field wasn't an object, tries to parse the value
			else if (!newObjectIntroduced)
			{	
				String value = reader.getValueAsString();
				String name = reader.getCurrentName();
				
				if (value == null)
					reader.skipChildren();
				else if (name.startsWith(instructionIndicator))
					constructor.setInstruction(value);
				else if (value.startsWith(IDGenerator.ID_INDICATOR))
					constructor.addLink(name, value);
				else
					constructor.addAttribute(name, value);
			}
		}
	}
	
	/**
	 * Instructs an objectConstructor in creating a set of objects from json data. The default 
	 * instruction indicator "%CHECK:" is used
	 * @param reader The json parser that goes through the json data
	 * @param constructor The constructor that will construct the objects
	 * @throws JsonParseException If the parsing failed
	 * @throws IOException If the operation failed
	 */
	public static void instructConstructor(JsonParser reader, 
			AbstractConstructor<?> constructor) throws 
			JsonParseException, IOException
	{
		instructConstructor(reader, constructor, "%CHECK:");
	}
}
