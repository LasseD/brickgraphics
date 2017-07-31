package io;

public interface ModelHandler<S extends ModelState> {
	void save(Model<S> model);
	void handleModelChange(Model<S> model);
}
