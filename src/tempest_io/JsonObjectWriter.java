package tempest_io;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerator;

import flow_recording.ObjectWriter;
import flow_recording.Writable;

/**
 * This objectWriter is able to write objects as json data
 * @author Mikko Hilpinen
 * @since 5.5.2015
 */
public class JsonObjectWriter extends ObjectWriter
{
	// ATTRIBUTES	--------------------------------
	
	private int instructionsWritten;
	
	
	// CONSTRUCTOR	--------------------------------
	
	/**
	 * Creates a new writer
	 */
	public JsonObjectWriter()
	{
		this.instructionsWritten = 0;
	}
	
	
	// OTHER METHODS	----------------------------
	
	/**
	 * Writers a writable object as json data into the stream
	 * @param object The object that will be written
	 * @param writer The writer that will generate the json
	 * @param isWrittenIntoAnObject false if the object is supposed to be the root object, 
	 * true otherwise. If the objects will be read with {@link 
	 * JsonIOAccessor#instructConstructor(com.fasterxml.jackson.core.JsonParser, 
	 * flow_recording.AbstractConstructor, String)}}, this should be true and the object 
	 * start and end should be written separately
	 * @throws IOException If the object writing failed
	 */
	public void writeObject(Writable object, JsonGenerator writer, 
			boolean isWrittenIntoAnObject) throws IOException
	{
		// Writes the introduction
		if (!isWrittenIntoAnObject)
			writer.writeStartObject();
		else
			writer.writeObjectFieldStart(getIDForWritable(object));
		
		
		// Writes the attributes
		Map<String, String> attributes = object.getAttributes();
		for (String key : attributes.keySet())
		{
			writer.writeStringField(key, attributes.get(key));
		}
		// Writes the links
		Map<String, Writable> links = object.getLinks();
		for (String key : links.keySet())
		{
			writer.writeStringField(key, getIDForWritable(links.get(key)));
		}
		
		// Closes the object
		writer.writeEndObject();
	}
	
	/**
	 * Writes a new instruction amidst the json data
	 * @param writer The writer that will write the data
	 * @param instruction The instruction that will be written
	 * @param instructionIndicator The instruction indicator that will be used
	 * @throws IOException If the operation failed
	 */
	public void writeInstruction(JsonGenerator writer, String instruction, 
			String instructionIndicator) throws IOException
	{
		writer.writeStringField(instructionIndicator + this.instructionsWritten, instruction);
		this.instructionsWritten ++;
	}
	
	/**
	 * Writes a new instruction amidst the json data. The method uses the default instruction 
	 * indicator "%CHECK:"
	 * @param writer The writer that will write the data
	 * @param instruction The instruction that will be written
	 * @throws IOException If the operation failed
	 */
	public void writeInstruction(JsonGenerator writer, String instruction) throws IOException
	{
		writeInstruction(writer, instruction, "%CHECK:");
	}
}
