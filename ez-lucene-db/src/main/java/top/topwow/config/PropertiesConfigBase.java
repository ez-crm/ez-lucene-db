package top.topwow.config;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.logging.Logger;

public abstract class PropertiesConfigBase {

	private static final Logger logger = Logger.getLogger(PropertiesConfigBase.class.toString());

	private boolean loaded = false;
	private HashMap<String, String> propMap = new HashMap<String, String>();

	public abstract String getFileName();

	public String get(String key) {
		if (!loaded)
			load();
		return propMap.get(key);
	}

	public abstract void onLoad(String key, String value);

	protected synchronized void load() {

		if (loaded)
			return;

		String fullFile = getFileName();

		Properties prop = new Properties();
		FileInputStream inputStream = null;
		try {
			inputStream = new FileInputStream(fullFile);
			prop.load(inputStream);
		} catch (FileNotFoundException e) {
			logger.severe("Properties file not found: " + fullFile);
		} catch (IOException e) {
			logger.severe("Properties file can not be loading: " + fullFile);
		} finally {
			try {
				if (inputStream != null)
					inputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		Iterator<Entry<Object, Object>> iterator = prop.entrySet().iterator();

		Entry<Object, Object> entry;
		while (iterator.hasNext()) {
			entry = iterator.next();
			String key = entry.getKey().toString().trim();
			String value = entry.getValue().toString().trim();
			propMap.put(key, value);
			onLoad(key, value);
		}

		loaded = true;

	}

	public HashMap<String, String> getPropMap() {
		if (!loaded)
			load();
		return propMap;
	}

}
