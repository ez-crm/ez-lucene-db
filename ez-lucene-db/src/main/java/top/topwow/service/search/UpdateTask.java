package top.topwow.service.search;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;

public final class UpdateTask {
	private UpdateType updateType;
	private Document writeDocument;
	private Term deleteTerm;
	private Query deleteQuery;

	public UpdateTask(UpdateType updateType) {
		this.updateType = updateType;
	}

	public UpdateTask(Document doc) {
		this.updateType = UpdateType.WRITE;
		this.writeDocument = doc;
	}

	public UpdateTask(Term deleteTerm) {
		this.updateType = UpdateType.DELETE;
		this.deleteTerm = deleteTerm;
	}

	public UpdateTask(Query deleteQuery) {
		this.updateType = UpdateType.DELETE;
		this.deleteQuery = deleteQuery;
	}

	public UpdateType getUpdateType() {
		return updateType;
	}

	public Document getWriteDocument() {
		return writeDocument;
	}

	public Term getDeleteTerm() {
		return deleteTerm;
	}

	public Query getDeleteQuery() {
		return deleteQuery;
	}
}
