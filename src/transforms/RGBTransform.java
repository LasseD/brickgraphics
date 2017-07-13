package transforms;

import java.util.Comparator;

public abstract class RGBTransform extends StateTransform<float[]> {
	public RGBTransform(float[] initialState) {
		super(initialState, new Comparator<float[]>(){
			@Override
			public int compare(float[] o1, float[] o2) {
				for(int i = 0; i < o1.length; i++)
					if(o1[i] != o2[i])
						return -1;
				return 0;
			}
		});
	}

	public float get(int index) {
		return get()[index];
	}
	
	public void set(int index, int val) {
		get()[index] = val;
	}
	
	public boolean allAreOne() {
		float[] scales = get();
		return scales[0] == 1f && scales[1] == 1f && scales[2] == 1f;
	}
}
