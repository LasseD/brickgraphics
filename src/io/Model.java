package io;

import java.io.*;
import java.util.*;
import java.util.List;

public class Model<S extends ModelState> {
	private String modelFile;
	private Map<S, Object> map;
	private List<ModelSaver<S>> modelSavers;
	private Class<S> modelStateClass;
	
	public boolean modelFileExists() {
		return new File(modelFile).exists();
	}
	
	public Model(String modelFile, Class<S> modelStateEnumClass ) {
		this.modelStateClass = modelStateEnumClass;
		this.modelFile = modelFile;
		modelSavers = new LinkedList<ModelSaver<S>>();
		try {
			FileInputStream fis = new FileInputStream(modelFile);
	        ObjectInputStream ois = new ObjectInputStream(fis);
			loadFrom(ois);
	        ois.close();
	        fis.close();	        
	        return;
		}
		catch(ClassCastException e) {
			e.printStackTrace();
		}
		catch(FileNotFoundException e) {
			// expected.
		} 
		catch(InvalidObjectException e) {
			// expected.
		}
		catch (IOException e) {
			e.printStackTrace();
		} 
		catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		map = new HashMap<S, Object>();	
		for(S state : modelStateClass.getEnumConstants()) {
			set(state, state.getDefaultValue());
		}
	}
	
	@SuppressWarnings("unchecked")
	public void loadFrom(ObjectInputStream ois) throws ClassCastException, FileNotFoundException, InvalidObjectException, IOException, ClassNotFoundException {
        map = (Map<S, Object>)ois.readObject();
        Set<S> readKeys = map.keySet();
        for(S s : modelStateClass.getEnumConstants()) {
        	if(!readKeys.contains(s))
        		throw new InvalidObjectException("State " + s + " not read!");
        }
	}
	
	public Object get(S state) {
		Object out = map.get(state);
		if(out == null) // never return null
			return state.getDefaultValue();
		return out;
	}
	
	public void set(S state, Object value) {
		if(!aExtendsB(value.getClass(), state.getType()))
			if(value.getClass() != state.getType())
				throw new IllegalArgumentException("Wrong type: " + state.getType() + "!=" + value.getClass());
		map.put(state, value);
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
	
	public void addModelSaver(ModelSaver<S> modelSaver) {
		modelSavers.add(modelSaver);
	}
	
	private void save() {
		for(ModelSaver<S> modelSaver : modelSavers) {
			modelSaver.save(this);
		}
	}
	
	public void saveToFile() throws IOException {
		save();
		File outputFile = new File(modelFile);
		if(!outputFile.exists())
			outputFile.createNewFile();
		FileOutputStream fos = new FileOutputStream(modelFile, false);
		ObjectOutputStream oos = new ObjectOutputStream(fos);

		saveTo(oos);

		oos.close();
		fos.close();		
	}
	
	public void saveTo(ObjectOutputStream oos) throws IOException {
		oos.writeObject(map);
	}
}
