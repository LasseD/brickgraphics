package io;

import java.io.*;
import java.util.*;
import java.util.List;

public class Model<S extends ModelState> {
	private String modelFile;
	private Map<S, Object> map;
	private Map<S, List<ModelChangeListener>> changeListeners;
	private List<ModelSaver<S>> modelSavers;
	private Class<S> modelStateClass;
	private boolean isSaving;
	
	public boolean modelFileExists() {
		return new File(modelFile).exists();
	}
	
	public Model(String modelFile, Class<S> modelStateEnumClass ) {
		this.modelStateClass = modelStateEnumClass;
		this.modelFile = modelFile;
		modelSavers = new LinkedList<ModelSaver<S>>();
		changeListeners = new HashMap<S, List<ModelChangeListener>>();
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
        for(S s : map.keySet()) {
        	List<ModelChangeListener> listeners = changeListeners.get(s);
        	if(listeners == null)
        		continue;
        	for(ModelChangeListener l : listeners) {
        		l.modelChanged(get(s));
        	}
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
		if(isSaving)
			return;
		List<ModelChangeListener> listeners = changeListeners.get(state);
    	if(listeners == null)
    		return;
    	for(ModelChangeListener l : listeners) {
			Object o = get(state);
			l.modelChanged(o);
		}
	}
	
	public void addModelChangeListener(ModelChangeListener c, S state) {
		List<ModelChangeListener> l = changeListeners.get(state);
		if(l == null) {
			l = new LinkedList<ModelChangeListener>();
			changeListeners.put(state, l);
		}
		l.add(c);
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
	
	public void save() {
		isSaving = true;
		for(ModelSaver<S> modelSaver : modelSavers) {
			modelSaver.save(this);
		}
		isSaving = false;
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
