package LuceneIndexing;

import java.io.IOException;
import java.nio.file.Paths;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanQuery;
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
import org.apache.lucene.search.spell.LuceneDictionary;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/* Classe addetta alla ricerca/query dei file indicizzati
 * si definisce Index"Explorer" solo per differenziarsi da IndexSearcher
 */

/** Implements IndexSearcher and 
 */
public class IndexExplorer {
	
	public static final int SHOWPAGES = 5;
	public static final int HITSPERPAGE = 5;
	
	private TopDocs results;
	private ScoreDoc[] hits;
	private int totalHits;
	private Document[] resultDoc;
	// Supporto per prova fino
	private String[] alternatives ;
	
	// Spelling check
	private SpellChecker spell;
	
	private IndexReader reader;
	private IndexSearcher searcher;
	private Analyzer analyzer;
	private QueryParser parser;
	
	/* Risponde alla query cercando nell'indice alla directory indicata
	 * e col modelo specificato  */
	@SuppressWarnings("unused")
	private IndexExplorer() {}
	public IndexExplorer(String queryString, String searchIndexPath, ModelsID model, 
			TolerantID tolerantMode,DocFields field) throws Exception {
		if (queryString == null || searchIndexPath == null ||
			queryString.equals(searchIndexPath)) {
			System.err.println("In IndexExplorer each field must be specified");
			throw new IllegalArgumentException();
		}

		Directory indexDir = FSDirectory.open(Paths.get(searchIndexPath));
		reader = DirectoryReader.open(indexDir);
		searcher = new IndexSearcher(reader);
		
		// configuring model
		switch(model) {
		case BM25_PROBABILITY: 
			searcher.setSimilarity(new BM25Similarity()); break;
		default: searcher.setSimilarity(new ClassicSimilarity());
		}
		
		// configuring query parser and analyzer
		analyzer = new StandardAnalyzer();
		parser = new QueryParser(field.toString(),analyzer);
		if (tolerantMode == TolerantID.POSTFIX)
			parser.setAllowLeadingWildcard(true);
		
		// handling fuzzy queries
		Query query;
		String term = queryString;
		// forbid postfix if Tolerant Mode PostFix is not set
		//if ((queryString.charAt(0) == '*') && (tolerantMode != TolerantID.POSTFIX))
			
			
		String[] tokens = term.split(" ");
		// Nel caso di piu' termini fuzzy va usato un'altra query di support
		if (tokens.length > 1 && tolerantMode == TolerantID.PLUSFUZZY) {
			SpanQuery[] clauses = new SpanQuery[tokens.length];
			for (int i = 0 ; i < tokens.length ; i++) {
				clauses[i] = new SpanMultiTermQueryWrapper<FuzzyQuery>(
						new FuzzyQuery(new Term(field.toString(),
						tokens[i])));
			}
			SpanNearQuery span = new SpanNearQuery(clauses, 0, true);
		// Nel caso di un termine fuzzy è più semplice
		} if (tokens.length == 1 && tolerantMode == TolerantID.ONEFUZZY) {
			query = new FuzzyQuery(new Term(field.toString(),queryString));
		} else { // prefix query
			query = parser.parse(queryString);
		}
		
		System.out.println("Searching for: "+query.toString(field.toString()));
		results = searcher.search(query, SHOWPAGES*HITSPERPAGE);
		hits = results.scoreDocs;
		totalHits = Math.toIntExact(results.totalHits);
		System.out.println("TotalHits:"+totalHits);
		System.out.println("Hits len:"+hits.length);
		
		// handling alternatives
		if (totalHits != 0 && tolerantMode == TolerantID.ALTERN) {
			SpellChecker spell = new SpellChecker(indexDir);
			IndexWriterConfig iwc = new IndexWriterConfig();
			spell.indexDictionary(
					new LuceneDictionary(reader,DocFields.content.toString()),
					iwc, false);
			alternatives = new String[tokens.length];
			for (int i = 0 ; i < tokens.length ; i++) {
				// le guide informano di sceglierne almeno 5
				// per aver dei buoni suggerimenti, tuttavia è ad uso
				// dimostrativo, quindi ne lascio 1
				String[] similar = spell.suggestSimilar(tokens[i],1);
				if (similar == null || similar.length == 0) {
					alternatives[i] = "No Suggestions";
				} else
					alternatives[i] = similar[0];
				System.out.println(alternatives[i]);
			}
			spell.close();
		}

		// storing in result doc
		System.out.println("Printing results...");
		resultDoc = new Document[hits.length];
		for (int i = 0 ; i < hits.length ; i++) {
			resultDoc[i] = searcher.doc(hits[i].doc);
		}
		
		/* Se è attivo la spellCheck si crea una lista 
		 * di alternative per ogni index term nella query, per ogni token
		 * si offre un suggerimento */
		//if (tolerantMode == TolerantID.SPELLCHECK) {
		//	sugg = spell.suggestSimilar("computre",1);
		//	for (int i = 0 ; i < sug.length ; i++) {
		//		System.out.println("Suggerimenti:" + sug[i]);
		//	}
		//	System.out.println("Done");
		//}
	}
	
	// getters
	/** Get number of result pages */
	public int getNumPage() { 
		if (hits.length == 0) return 1;
		if (hits.length%HITSPERPAGE == 0) return hits.length/HITSPERPAGE;
		else return hits.length/HITSPERPAGE+1;
	}
	/** Get total number of hits */
	public int getTotalHit() { return totalHits; }
	/** Get number of document in the answer */
	public int getResultDocNum() { return hits.length; }

	public Document getDocument(int position) throws IOException {
		if (position < 0 || position >= totalHits)
			return null;
			
		return resultDoc[position];
	}

	/** Get list of "Did you mean?" terms */
	public String[] getAlternatives() {
		return alternatives;
	}
}
