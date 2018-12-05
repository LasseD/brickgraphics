package io;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.io.*;
import java.util.*;
import java.util.List;

public class KVFileHandler<S extends ModelState> {
	@SuppressWarnings("rawtypes")
	private Map<String, KVFileValueHandler> typeSerializers;
	
	@SuppressWarnings("rawtypes")
	public KVFileHandler() {
		typeSerializers = new TreeMap<String, KVFileValueHandler >();
		typeSerializers.put(String.class.getName(), new KV_String());
		typeSerializers.put(Rectangle.class.getName(), new KV_Rectangle());
		typeSerializers.put(Dimension.class.getName(), new KV_Dimension());
		typeSerializers.put(Integer.class.getName(), new KV_Integer());
		typeSerializers.put(Float.class.getName(), new KV_Float());
		typeSerializers.put(Double.class.getName(), new KV_Double());
		typeSerializers.put(float[].class.getName(), new KV_FloatArray());
		typeSerializers.put(int[].class.getName(), new KV_IntArray());
		typeSerializers.put(boolean[].class.getName(), new KV_BoolArray());
		typeSerializers.put(TreeMap.class.getName(), new KV_DoubleMap());
		typeSerializers.put(Boolean.class.getName(), new KV_Boolean());
		typeSerializers.put(Rectangle2D.Double.class.getName(), new KV_Rectangle2DDouble());
		typeSerializers.put(DataFile.class.getName(), new KV_DataFile());
	}
	
	public void readFile(BufferedReader br, Map<S, Object> mapWithDefaults) throws IOException {
		// create mapping from key to S:
		Map<String, S> keyToSMap = new TreeMap<String, S>();
		for(S s : mapWithDefaults.keySet()) {
			keyToSMap.put(s.getName(), s);
		}
		
		// Read and save all key/value pars:
		String line;
		while((line = br.readLine()) != null) {
			int indexOfColon = line.indexOf(':');
			if(indexOfColon == -1)
				continue; // not a valid line.
			String k = line.substring(0, indexOfColon);
			String vRaw = line.substring(indexOfColon+1);
			
			// Build longer vRaw if necessary:
			if(vRaw.length() > 0 && vRaw.charAt(0) == '"') {
				StringBuilder v = new StringBuilder();
				v.append(vRaw);
				while(vRaw.charAt(vRaw.length()-1) != '"') {
					vRaw = br.readLine();
					if(vRaw == null)
						throw new IOException("KVFile corrupt: Mismatched text blob. No matching \"-character ending text blob.");
					v.append(vRaw);
				}
				vRaw = v.toString();
				vRaw = vRaw.substring(1, vRaw.length()-1);
			}
			
			if(!keyToSMap.containsKey(k)) {
				Log.log("Unknown key value pair. Key: " + k + ", value: " + vRaw);
				continue;
			}
			S type = keyToSMap.get(k);
			keyToSMap.remove(k);
			try {
				mapWithDefaults.put(type, typeSerializers.get(type.getType().getName()).kl2value(vRaw));
			}
			catch(Exception e) {
				Log.log(e);
			}
		}
		for(String key : keyToSMap.keySet()) {
			Log.log("KVFile read, but the value was not found for key: " + key);
		}
	}
	
	@SuppressWarnings("unchecked")
	public void writeFile(PrintWriter pw, Map<S, Object> map) {
		for(S s : map.keySet()) {
			String typeName = s.getType().getName();
			Object value = map.get(s);
			if(value == null)
				throw new NoSuchElementException("Value " + s + " not found in map!");
			KVFileValueHandler vh = typeSerializers.get(typeName);
			if(vh == null)
				throw new NoSuchElementException("Type name " + typeName + " not found in serializers!");
			pw.println(s + ":" + vh.value2kl(value));
		}
	}
	
	private static interface KVFileValueHandler<T> {
		public T kl2value(String s);
		public String value2kl(T t);
	}
	
	private static class KV_String implements KVFileValueHandler<String> {
		@Override
		public String kl2value(String s) {
			return s;
		}
		@Override
		public String value2kl(String t) {
			return '"' + t + '"';
		}
	}
	private static class KV_Integer implements KVFileValueHandler<Integer> {
		@Override
		public Integer kl2value(String s) {
			return Integer.parseInt(s);
		}
		@Override
		public String value2kl(Integer t) {
			return Integer.toString(t);
		}
	}
	private static class KV_Double implements KVFileValueHandler<Double> {
		@Override
		public Double kl2value(String s) {
			return Double.parseDouble(s);
		}
		@Override
		public String value2kl(Double t) {
			return Double.toString(t);
		}
	}
	private static class KV_Float implements KVFileValueHandler<Float> {
		@Override
		public Float kl2value(String s) {
			return Float.parseFloat(s);
		}
		@Override
		public String value2kl(Float t) {
			return Float.toString(t);
		}
	}
	
	private static class KV_DoubleMap implements KVFileValueHandler<TreeMap<Integer,Double> > {
		@Override
		public TreeMap<Integer,Double> kl2value(String s) {
			TreeMap<Integer,Double> m = new TreeMap<Integer,Double>();			
			Scanner scanner = new Scanner(s);
			scanner.useLocale(Locale.US);
			while(scanner.hasNextInt()) {
				m.put(scanner.nextInt(), scanner.nextDouble());
			}
			return m;
		}
		@Override
		public String value2kl(TreeMap<Integer,Double> t) {
			StringBuffer sb = new StringBuffer();
			boolean first = true;
			for(int i : t.keySet()) {
				if(!first)
					sb.append(' ');
				sb.append(i);
				sb.append(' ');
				sb.append(t.get(i));
				first = false;				
			}
			return sb.toString();
		}
	}
	
	private static class KV_FloatArray implements KVFileValueHandler<float[]> {
		@Override
		public float[] kl2value(String s) {
			List<Float> list = new LinkedList<Float>();			
			Scanner scanner = new Scanner(s);
			scanner.useLocale(Locale.US);
			while(scanner.hasNextFloat()) {
				list.add(scanner.nextFloat());
			}
			float[] out = new float[list.size()];
			int i = 0;
			for(float f : list) {
				out[i++] = f;
			}
			return out;
		}
		@Override
		public String value2kl(float[] t) {
			StringBuffer sb = new StringBuffer();
			boolean first = true;
			for(float f : t) {
				if(!first)
					sb.append(' ');
				sb.append(f);
				first = false;				
			}
			return sb.toString();
		}
	}
	
	private static class KV_IntArray implements KVFileValueHandler<int[]> {
		@Override
		public int[] kl2value(String s) {
			List<Integer> list = new LinkedList<Integer>();			
			Scanner scanner = new Scanner(s);
			while(scanner.hasNextInt()) {
				list.add(scanner.nextInt());
			}
			int[] out = new int[list.size()];
			int i = 0;
			for(int f : list) {
				out[i++] = f;
			}
			return out;
		}
		@Override
		public String value2kl(int[] t) {
			StringBuffer sb = new StringBuffer();
			boolean first = true;
			for(int f : t) {
				if(!first)
					sb.append(' ');
				sb.append(f);
				first = false;				
			}
			return sb.toString();
		}
	}
	private static class KV_BoolArray implements KVFileValueHandler<boolean[]> {
		@Override
		public boolean[] kl2value(String s) {
			List<Boolean> list = new LinkedList<Boolean>();			
			Scanner scanner = new Scanner(s);
			while(scanner.hasNextInt()) {
				list.add(scanner.nextInt() == 1);
			}
			boolean[] out = new boolean[list.size()];
			int i = 0;
			for(boolean f : list) {
				out[i++] = f;
			}
			return out;
		}
		@Override
		public String value2kl(boolean[] t) {
			StringBuffer sb = new StringBuffer();
			boolean first = true;
			for(boolean f : t) {
				if(!first)
					sb.append(' ');
				sb.append(f ? 1 : 0);
				first = false;	
			}
			return sb.toString();
		}
	}
	private static class KV_Boolean implements KVFileValueHandler<Boolean> {
		@Override
		public Boolean kl2value(String s) {
			return Boolean.parseBoolean(s);
		}
		@Override
		public String value2kl(Boolean t) {
			return Boolean.toString(t);
		}
	}
	private static class KV_Rectangle implements KVFileValueHandler<Rectangle> {
		@Override
		public Rectangle kl2value(String s) {
			Scanner scanner = new Scanner(s);
			Rectangle out = new Rectangle();
			out.x = scanner.nextInt();
			out.y = scanner.nextInt();
			out.width = scanner.nextInt();
			out.height = scanner.nextInt();
			return out;
		}
		@Override
		public String value2kl(Rectangle t) {
			return t.x + " " + t.y + " " + t.width + " " + t.height;
		}
	}
	private static class KV_Rectangle2DDouble implements KVFileValueHandler<Rectangle2D.Double> {
		@Override
		public Rectangle2D.Double kl2value(String s) {
			Scanner scanner = new Scanner(s);
			scanner.useLocale(Locale.US);
			Rectangle2D.Double out = new Rectangle2D.Double();
			out.x = scanner.nextDouble();
			out.y = scanner.nextDouble();
			out.width = scanner.nextDouble();
			out.height = scanner.nextDouble();
			return out;
		}
		@Override
		public String value2kl(Rectangle2D.Double t) {
			return t.x + " " + t.y + " " + t.width + " " + t.height;
		}
	}
	private static class KV_Dimension implements KVFileValueHandler<Dimension> {
		@Override
		public Dimension kl2value(String s) {
			Scanner scanner = new Scanner(s);
			Dimension out = new Dimension();
			out.width = scanner.nextInt();
			out.height = scanner.nextInt();
			return out;
		}
		@Override
		public String value2kl(Dimension t) {
			return t.width + " " + t.height;
		}
	}
	private static class KV_DataFile implements KVFileValueHandler<DataFile> {
		@Override
		public DataFile kl2value(String s) {
			try {
				return new DataFile(s);
			} catch (IOException e) {
				Log.log(e);
				return new DataFile();
			}
		}
		@Override
		public String value2kl(DataFile t) {
			return t.toBase64();
		}
	}

}
