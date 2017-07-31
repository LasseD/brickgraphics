package mosaic.rendering;

public interface ProgressCallback {
	public static final ProgressCallback NOP = new ProgressCallback() {		
		@Override
		public void reportProgress(int progressInPromilles) {
		}
	};
	
	void reportProgress(int progressInPromilles);
}
