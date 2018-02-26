
import java.io.IOException;
import java.nio.file.Paths;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.spans.SpanMultiTermQueryWrapper;
import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import GUI.Window;
import LuceneIndexing.TolerantID;

/** Lucene Models and Tolerant retrieval test on sMarT benchmark
 * 
 * Apache Lucene is high-performance java API that supports fulltext-searching,
 * which include indexing, text pre-processing, scoring, querying and several 
 * utils and unique feature (such as Query and Scoring personalization).
 * 
 * This project focus on showing briefly what does it provides for Tolerant 
 * Retrieval and Modelling, so knowledge about classic information retrieval 
 * model is assumed. It's based on Lucene 7.2.1 which doesn't differ too much
 * from previous versions.
 * 
 * Test and evaluation (Precision & Recall) will be based on SMART benchmark.
 * 
 * Data: Gennaio 2017
 * Autore: Hu Jia Cheng
 */
public class LuceneMT {

	public static void main(String[] args) {
		System.out.println("Starting LuceneMT...");
		Window wn = new Window();
		/*
		try {
		Directory indexDir = FSDirectory.open(Paths.get("./indexes/SMART/ADI"));
		IndexReader reader = DirectoryReader.open(indexDir);
		IndexSearcher searcher = new IndexSearcher(reader);
		// configuring query parser and analyzer
		Analyzer analyzer = new StandardAnalyzer();
		QueryParser parser = new QueryParser("content",analyzer);
		// handling fuzzy queries
		Query query = new FuzzyQuery(new Term("content","computer"));
		//String[] tokens = term.split(" ");
		//SpanQuery[] clauses = new SpanQuery[tokens.length];
		//for (int i = 0 ; i < tokens.length ; i++) {
		//	clauses[i] = new SpanMultiTermQueryWrapper<FuzzyQuery>(
		//		new FuzzyQuery(new Term("content",
		//		tokens[i])));
		//}
		//query = new SpanNearQuery(clauses, 0, true);
		System.out.println("sarching for:"+query.toString());
		TopDocs results = searcher.search(query, 5*10);
		int totalHits = Math.toIntExact(results.totalHits);
		System.out.println("TotalHits:"+totalHits);
		ScoreDoc[] hits = results.scoreDocs;
		Document[] resultDoc = new Document[hits.length];
		for (int i = 0 ; i < hits.length ; i++) {
			resultDoc[i] = searcher.doc(hits[i].doc);
			System.out.println("ID:"+resultDoc[i].get("id"));
		}
		
		System.out.println("erroree");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} */
		
	}
}
