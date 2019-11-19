package top.topwow.service.search;

import java.util.logging.Logger;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;

import top.topwow.util.MyLog;

public class IndexReadWriteChange implements Runnable {

	private static final Logger logger = Logger.getLogger(IndexReadWriteChange.class.toString());

	private IndexAction indexReadWrite;

	public IndexReadWriteChange(IndexAction indexReadWrite) {
		this.indexReadWrite = indexReadWrite;
	}

	@Override
	public void run() {

		while (true) {
			try {
				IndexReader changeReader = DirectoryReader
						.openIfChanged((DirectoryReader) indexReadWrite.getIndexReader());
				if (changeReader != null) {
					indexReadWrite.changeNewReader(changeReader);
				}
				Thread.sleep(5000);
			} catch (Exception e) {
				logger.severe(MyLog.stackTrace(e, String.format("openIfChanged indexReader %s fail")).toString());
			}
		}
	}

}
