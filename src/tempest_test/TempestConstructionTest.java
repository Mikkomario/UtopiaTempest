package tempest_test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;

import tempest_io.JsonIOAccessor;
import tempest_io.JsonObjectWriter;
import flow_test.TestConstructable;
import flow_test.TestConstructor;

/**
 * This class tests writing and reading objects from json
 * @author Mikko Hilpinen
 * @since 5.5.2015
 */
public class TempestConstructionTest
{
	// CONSTRUCTOR	-------------------------------
	
	private TempestConstructionTest()
	{
		// Static interface
	}

	
	// MAIN METHOD	-------------------------------
	
	/**
	 * Starts the test
	 * @param args the amount of test objects to be created (default = 5)
	 */
	public static void main(String[] args)
	{
		int amount = 5;
		
		if (args.length > 0)
			amount = Integer.parseInt(args[0]);
		
		List<TestConstructable> objects = createObjects(amount);
		printObjects(objects);
		
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		try
		{
			writeObjects(output, objects);
		}
		catch (IOException e)
		{
			System.err.println("Failure when writing objects");
			e.printStackTrace();
		}
		
		byte[] json = output.toByteArray();
		System.out.println(new String(json));
		
		try
		{
			List<TestConstructable> readObjects = new ArrayList<>(readObjects(json));
			readObjects.sort(null);
			printObjects(readObjects);
		}
		catch (IOException e)
		{
			System.err.println("Failure when reading objects");
			e.printStackTrace();
		}
	}
	
	
	// OTHER METHODS	---------------------------
	
	private static List<TestConstructable> createObjects(int amount)
	{
		List<TestConstructable> objects = new ArrayList<>();
		TestConstructable lastConstructable = null;
		
		for (int i = 0; i < amount; i++)
		{
			lastConstructable = new TestConstructable("GROUP" + i/3, "Message" + i, "Name" + i, 
					lastConstructable);
			objects.add(lastConstructable);
		}
		
		return objects;
	}
	
	private static void writeObjects(OutputStream targetStream, 
			List<TestConstructable> objects) throws IOException
	{
		JsonGenerator writer = null;
		
		try
		{
			JsonObjectWriter objectWriter = new JsonObjectWriter();
			writer = JsonIOAccessor.createWriter(targetStream);
			
			String lastBornUnder = null;
			
			writer.writeStartObject();
			for (TestConstructable object : objects)
			{
				if (!object.getBornUnder().equals(lastBornUnder))
					objectWriter.writeInstruction(writer, object.getBornUnder());
				objectWriter.writeObject(object, writer, true);
			}
			writer.writeEndObject();
		}
		finally
		{
			JsonIOAccessor.closeWriter(writer);
		}
	}
	
	private static Collection<TestConstructable> readObjects(byte[] json) throws 
			JsonParseException, IOException
	{
		JsonParser reader = null;
		
		try
		{
			reader = JsonIOAccessor.createReader(new ByteArrayInputStream(json));
			TestConstructor constructor = new TestConstructor();
			JsonIOAccessor.instructConstructor(reader, constructor);
			
			return constructor.getConstructs().values();
		}
		finally
		{
			JsonIOAccessor.closeReader(reader);
		}
	}
	
	private static void printObjects(List<TestConstructable> objects)
	{
		for (TestConstructable object : objects)
		{
			System.out.println(object);
		}
	}
}
