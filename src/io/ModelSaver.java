package io;

public interface ModelSaver<S extends ModelState> {
	void save(Model<S> model);
}
