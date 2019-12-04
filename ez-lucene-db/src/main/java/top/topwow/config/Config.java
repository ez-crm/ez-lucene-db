package top.topwow.config;

public class Config extends PropertiesConfigBase {

	private int mergeFactor;
	private int maxBufferedDocs;

	public static Config me=new Config(); 
	@Override
	public String getFileName() {
		return "conf/config.ini";
	}

	@Override
	public void onLoad(String key, String value) {
		if (key.equals("lucene.mergeFactor")) {
			mergeFactor = Integer.parseInt(value);
		} else if (key.equals("lucene.maxBufferedDocs")) {
			maxBufferedDocs = Integer.parseInt(value);
		}
	}

	public int getMergeFactor() {
		super.getPropMap();
		return mergeFactor;
	}

	public void setMergeFactor(int luceneMergeFactor) {
		this.mergeFactor = luceneMergeFactor;
	}

	public int getMaxBufferedDocs() {
		super.getPropMap();
		return maxBufferedDocs;
	}

	public void setMaxBufferedDocs(int lucenemaxBufferedDocs) {
		this.maxBufferedDocs = lucenemaxBufferedDocs;
	}

}
