package top.topwow.service.search;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.AlreadyClosedException;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.NoLockFactory;
import org.wltea.analyzer.lucene.IKAnalyzer;

import top.topwow.config.Config;
import top.topwow.util.MyLog;

public final class IndexAction implements Runnable {
	private static final Logger logger = Logger.getLogger(IndexAction.class.toString());
	private Config config;
	private IndexWriter indexWriter;
	private IndexReader indexReader;
	private IndexSearcher indexSearcher;
	private String path;
	private boolean startReaderCheck = false;

	public IndexAction(String path, Config config) {
		this.path = path;
		this.config = config;
	}

	private synchronized long writeAction(UpdateType taskType, Document documentIfWrite, Term TermIfDelete, Query queryIfDelete, boolean commit) {
		try {
			long result = 0;
			switch (taskType) {
			case WRITE:
				result = getIndexWriter().addDocument(documentIfWrite);
				break;
			case DELETE:
				if (TermIfDelete != null)
					result = getIndexWriter().deleteDocuments(TermIfDelete);
				if (queryIfDelete != null)
					result = getIndexWriter().deleteDocuments(queryIfDelete);
				break;
			case MERGE:
				getIndexWriter().maybeMerge();
				break;
			}

			if (commit && result > 0) {
				commitAndRefreshReader();
			}
			return result;
		} catch (IOException e) {
			logger.severe(
					MyLog.stackTrace(e, String.format("UpdateTask  %s fail:", taskType.name())).toString());
			return 0;
		}
	}

	private void commitAndRefreshReader() {
		try {
			getIndexWriter().commit();
		} catch (Exception e) {
			logger.severe(MyLog.stackTrace(e, "提交异常").toString());
		}

		refreshReader();
	}

	private void refreshReader() {
		try {
			IndexReader changeReader = DirectoryReader.open(getIndexWriter());
			changeNewReader(changeReader);
		} catch (Exception e) {
			logger.severe(MyLog.stackTrace(e, "刷新Reader异常").toString());
		}
	}

	// 此方法为启动新线程去刷新Reader
	@SuppressWarnings("unused")
	private void commitAndRefreshReaderOld() {
		try {
			getIndexWriter().commit();
			if (!startReaderCheck) {
				synchronized (this) {
					if (!startReaderCheck) {
						new Thread(this).start();
						startReaderCheck = true;
					}
				}
			}
		} catch (Exception e) {
			logger.severe(MyLog.stackTrace(e, String.format("openIfChanged indexReader %s fail")).toString());
		}
	}

	@Override
	public void run() {
		while (true) {
			try {
				IndexReader changeReader = DirectoryReader.openIfChanged((DirectoryReader) indexReader);
				if (changeReader != null) {
					changeNewReader(changeReader);
				}
			} catch (Exception e) {
				logger.severe(MyLog.stackTrace(e, String.format("openIfChanged indexReader %s fail")).toString());
			}

			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				logger.warning("更新reader线程结束");
				break;
			}
		}
	}

	public List<Long> writeDocuments(List<Document> docs) {
		if (indexWriter == null) {
			init();
		}
		boolean added = false;
		LinkedList<Long> result = new LinkedList<>();
		for (Document doc : docs) {
			long code = writeAction(UpdateType.WRITE, doc, null, null, false);
			result.add(code);
			if (code > 0) {
				added = true;
			}
		}

		if (added) {
			commitAndRefreshReader();
		}
		return result;
	}

	public long writeDocument(Document doc) {
		if (indexWriter == null) {
			init();
		}
		return writeAction(UpdateType.WRITE, doc, null, null, true);
	}

	public long delete(Term term) {
		if (indexWriter == null) {
			init();
		}
		return writeAction(UpdateType.DELETE, null, term, null, true);
	}

	public long delete(Query query) {
		if (indexWriter == null) {
			init();
		}
		return writeAction(UpdateType.DELETE, null, null, query, true);
	}

	public TopDocs search(Query query, int offset) throws IOException {
		try {
			return getIndexSearcher().search(query, offset);
		} catch (AlreadyClosedException e) {
			refreshReader();
			return search(query, offset);
		}
	}

	public Document document(int docID) throws IOException {
		try {
			return getIndexReader().document(docID);
		} catch (AlreadyClosedException e) {
			refreshReader();
			return document(docID);
		}
	}

	protected IndexWriter getIndexWriter() {
		if (indexWriter == null) {
			init();
		}
		return indexWriter;
	}

	protected IndexReader getIndexReader() {
		if (indexReader == null) {
			init();
		}
		return indexReader;
	}

	public IndexSearcher getIndexSearcher() {
		if (indexSearcher == null) {
			init();
		}
		return indexSearcher;
	}

	private synchronized void init() {

		if (indexWriter != null) {
			return;
		}

		try {
			Analyzer analyzer = new IKAnalyzer();
			IndexWriterConfig conf = new IndexWriterConfig(analyzer);
			conf.setCommitOnClose(true);
			conf.setOpenMode(OpenMode.CREATE_OR_APPEND);
			conf.setMaxBufferedDocs(config.getMaxBufferedDocs());

			indexWriter = new IndexWriter(FSDirectory.open(new File(path).toPath(), NoLockFactory.INSTANCE),
					conf);

			indexReader = DirectoryReader.open(indexWriter);
			indexSearcher = new IndexSearcher(indexReader);

		} catch (IOException e) {
			logger.severe(MyLog.stackTrace(e, String.format("Open indexWriter %s fail:", path)).toString());
		}
	}

	public void changeNewReader(IndexReader changeReader) {
		indexSearcher = new IndexSearcher(changeReader);
		try {
			indexReader.close();
		} catch (IOException e) {
			logger.severe(
					MyLog.stackTrace(e, String.format("changeNewReader indexReader.close fail", path)).toString());
		}
		indexReader = changeReader;
	}

	public String getPath() {
		return path;
	}

}
