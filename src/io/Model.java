package io;

import java.io.*;
import java.util.*;

public class Model<S extends ModelState> {
	private String modelFile;
	private Map<S, Object> stateValueMap;
	private List<ModelHandler<S>> modelHandlers;
	private Set<String> addedModelHandlers;
	private Class<S> modelStateClass;
	private KVFileHandler<S> serializer;
	private Set<S> savedValues;
	
	public boolean modelFileExists() {
		return new File(modelFile).exists();
	}
	
	public Model(String modelFile, Class<S> modelStateEnumClass ) {
		this.modelStateClass = modelStateEnumClass;
		this.modelFile = modelFile;
		serializer = new KVFileHandler<S>();
		modelHandlers = new LinkedList<ModelHandler<S>>();
		addedModelHandlers = new TreeSet<String>();

		// Populate state value map with default values - then load from state file
		stateValueMap = new HashMap<S, Object>();	
		for(S state : modelStateClass.getEnumConstants()) {
			stateValueMap.put(state, state.getDefaultValue());
		}

		FileInputStream fis = null;
		BufferedReader br = null;
		try {
			fis = new FileInputStream(modelFile);
	        br = new BufferedReader(new InputStreamReader(fis));
	        
	        loadFrom(br);
		}
		catch (IOException e) {
			Log.log(e);
		} 
		finally {
			try {
				if(br != null)
					br.close();
				if(fis != null)
					fis.close();							
			}
			catch(IOException e2) {
				Log.log(e2);				
			}
		}
	}
	
	public void loadFrom(BufferedReader br) throws IOException {
		serializer.readFile(br, stateValueMap);
		for(ModelHandler<S> modelHandler : modelHandlers) {
			modelHandler.handleModelChange(this);
		}
	}
	
	public Object get(S state) {
		Object out = stateValueMap.get(state);
		if(out == null)
			return state.getDefaultValue();
		return out;
	}
	
	public void set(S state, Object value) {
		if(value == null) {
			if(savedValues != null)
				savedValues.add(state);
			return;
		}
		if(!aExtendsB(value.getClass(), state.getType()))
			if(value.getClass() != state.getType())
				throw new IllegalArgumentException("Wrong type: " + state.getType() + "!=" + value.getClass());
		if(savedValues != null && savedValues.contains(state)) {
			Log.log(new IllegalStateException("State value " + state + " saved more than once!"));
		}
		stateValueMap.put(state, value);
		if(savedValues != null)
			savedValues.add(state);
	}

	/**
	 * Needed because some methods tend to return subtypes.
	 */
	private static boolean aExtendsB(Class<?> a, Class<?> b) {
		if(a == b)
			return true;
		if(a == Object.class)
			return false;
		return aExtendsB(a.getSuperclass(), b);
	}
	
	public void addModelHandler(ModelHandler<S> modelHandler) {
		if(addedModelHandlers.contains(modelHandler.getClass().getName()))
			throw new IllegalStateException("Model handler of type " + addedModelHandlers  + " already added!");
		modelHandlers.add(modelHandler);
		addedModelHandlers.add(modelHandler.getClass().getName());
	}
	
	/**
	 * Saves using all registered modelSavers. 
	 * Logs errors if any value is not saved exactly once
	 */
	private void runModelsavers() {
		savedValues = new TreeSet<S>();
		for(ModelHandler<S> modelHandler : modelHandlers) {
			modelHandler.save(this);
		}
		// Check that everything got saved:
		for(S s : stateValueMap.keySet()) {
			if(!savedValues.contains(s)) {
				Log.log(new IllegalStateException("State value " + s + " was not saved!"));				
			}
		}
		savedValues = null;
	}
	
	public void saveToFile() throws IOException {
		saveToFile(new File(modelFile));
	}

	public void saveToFile(File outputFile) throws IOException {
		runModelsavers();
		if(!outputFile.exists())
			outputFile.createNewFile();
		FileOutputStream fos = new FileOutputStream(outputFile, false);
		PrintWriter pw = new PrintWriter(fos);

		serializer.writeFile(pw, stateValueMap);

		pw.flush();
		pw.close();
		fos.close();		
	}
}
