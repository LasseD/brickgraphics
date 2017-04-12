package transforms;

import java.util.*;

/**
 * This kind of transform has a state. If the state is changed, the buffers will be emptied.
 * @author ld
 * @param <StateType> Type of the state.
 */
public abstract class StateTransform<StateType> extends BufferedTransform {
	private StateType state;
	private Comparator<StateType> cmp;
	
	public StateTransform(StateType initialState) {
		this(initialState, null);
	}

	/**
	 * 
	 * @param initialState
	 * @param cmp comparator for comparing the state to a new one set.
	 */
	public StateTransform(StateType initialState, Comparator<StateType> cmp) {
		super(1);
		this.state = initialState;
		this.cmp = cmp;
	}
	
	public void set(StateType state) {
		if(cmp == null && state.equals(this.state) || 
		   cmp != null && cmp.compare(state, this.state) == 0)
			return;
		this.state = state;
		clearBuffer();
	}
	
	public StateType get() {
		return state;
	}
}
