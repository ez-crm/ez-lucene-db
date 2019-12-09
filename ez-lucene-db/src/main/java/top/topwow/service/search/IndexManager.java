package top.topwow.service.search;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;

import top.topwow.config.Config;

public class IndexManager {

	private ConcurrentHashMap<String, IndexAction> map = new ConcurrentHashMap<>();

	public static IndexManager me = new IndexManager();
	private Config config = Config.me;

	private IndexAction getindexAction(String path) {
		IndexAction indexAction = map.get(path);
		if (indexAction == null) {
			synchronized (map) {
				indexAction = map.get(path);
				if (indexAction != null) {
					return indexAction;
				}
				indexAction = new IndexAction(path, config);
				map.put(path, indexAction);
			}

		}
		return indexAction;
	}

	public List<Long> insert(String path, List<Document> docs) throws IOException {
		IndexAction write = getindexAction(path);
		return write.writeDocuments(docs);
	}

	public long insert(String path, Document doc) throws IOException {
		IndexAction write = getindexAction(path);
		return write.writeDocument(doc);
	}

	public long delete(String path, Query query) throws IOException {
		IndexAction write = getindexAction(path);
		return write.delete(query);
	}

	public long delete(String path, Term term) throws IOException {
		IndexAction write = getindexAction(path);
		return write.delete(term);
	}

	public long queryTotal(String path, Query query) throws IOException {
		IndexAction indexAction = getindexAction(path);
		TopDocs topDocs = indexAction.search(query, Integer.MAX_VALUE);
		return topDocs.totalHits.value;
	}
	public boolean queryExist(String path, Query query) throws IOException {
		IndexAction indexAction = getindexAction(path);
		TopDocs topDocs = indexAction.search(query, 1);
		return topDocs.totalHits.value > 0L;
	}
	public List<Document> query(String path, Query query, Page page) throws IOException {
		IndexAction indexAction = getindexAction(path);

		int offset = page.getPage() * page.getPageSize();
		TopDocs topDocs = indexAction.search(query, offset);
		ScoreDoc[] hits = topDocs.scoreDocs;

		page.setTotal(Math.toIntExact(topDocs.totalHits.value));// 总记录数

		if (page.getTotal() == 0) {
			return Collections.emptyList();
		}

		LinkedList<Document> result = new LinkedList<>();

		int min = (page.getPage() - 1) * page.getPageSize();
		int max = Math.min(page.getTotal(), min + page.getPageSize());
		for (int i = min; i < max; i++) {
			result.add(indexAction.document(hits[i].doc));
		}

		return result;
	}

	public List<Document> query(String path, Query query, int numRows) throws IOException {
		IndexAction indexAction = getindexAction(path);

		TopDocs topDocs = indexAction.search(query, numRows);
		if (topDocs.totalHits.value == 0) {
			return Collections.emptyList();
		}

		LinkedList<Document> result = new LinkedList<>();
		for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
			result.add(indexAction.document(scoreDoc.doc));
		}

		return result;
	}
}
