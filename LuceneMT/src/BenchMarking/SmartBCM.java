package BenchMarking;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.regex.Pattern;

import LuceneIndexing.DocFields;
import LuceneIndexing.IndexExplorer;
import LuceneIndexing.ModelsID;
import LuceneIndexing.TolerantID;
import utils.Finder;

/* Classe che automatizza il processo di valutazione delle Precision
 * e Recall del benchmark SMART
 */

// Cosa fa in breve, dato il path di dove cercare il benchmark
// e dove cercare l'indice, calcola aumaticamente le precision a diversi
// livelli di recall

/** This class process automatically precision and recall evaluation 
 * from a given SMART BENCHMARK data and provide method to get results that can be
 * used to generate table and plots.
 */
public class SmartBCM {
	
	private final static int STANDARD_RECALL = 11;
	
	// query at position 0 is unused
	private String[] queries;
	// relevant docs for each given query, again array 0 is unused
	private int[][] relevantDocs;
	
	private String searchIndexPath;
	
	/** Evaluates precision and recall on each query of query set
	 * @param benchMarkPath, benchmark query set and relevant set path
	 * @param searchIndexPath, benchmark index path 
	 * @param model, searching phase similarity configuration
	 * @param tolerantMode, tolerant mode that will be activated
	 * @param field, document field
	 */
	public SmartBCM(String benchMarkPath, String searchIndexPath) throws IOException {
		if (benchMarkPath == null || searchIndexPath == null || 
				benchMarkPath.equals(searchIndexPath) ||	
				!Files.isReadable(Paths.get(benchMarkPath)) ||
				!Files.isWritable(Paths.get(searchIndexPath))) {
				System.err.println("Error the following path\n"+benchMarkPath+"\n"
						+searchIndexPath+"\n"+"may not exist or respectively "+
						"not readable or writable");
				throw new IllegalArgumentException();
			}
		
		this.searchIndexPath = searchIndexPath;
		
		queries = null;
		relevantDocs = null;
		
		// Unfortunately Smart benchmark present several query file names
		Finder finder = new Finder("*.QRY");
		Path startingDir = Paths.get(benchMarkPath);
		Files.walkFileTree(startingDir,finder);
		String[] findResults = finder.getResults();
		String queryFile = findResults[0];
		initQueryArray(queryFile);
		relevantDocs = new int[queries.length][];
		
		finder = new Finder("*.REL");
		startingDir = Paths.get(benchMarkPath);
		Files.walkFileTree(startingDir,finder);
		findResults = finder.getResults();
		String relevantsFile = findResults[0];
		initRelevantDocs(relevantsFile);
	}
	
	public String getQuery(int index) throws IllegalArgumentException {
		if (index < 1 || index >= queries.length) {
				throw new IllegalArgumentException();
		} else
			return queries[index];
	}
	
	public int getNumQueries() {
		if (queries == null) {
			return 0;
		} else {
			return (queries.length)-1;
		}
	}
	
	/* Siccome i documenti di SMART non sono sempre perfetti. Si effettua
	 * il retrieving delle query ha un aspetto complicato ma è per ovviare
	 * a ciò.
	 */
	private void initQueryArray(final String queryFile) 
			throws FileNotFoundException {
		
		/* L'indexing seguente funziona nel caso i file contenenti
		 * i dati del benchmark siano ordinati correttamente senza 
		 * ripetizione di delimitatori, in caso contrario si avranno
		 * errori in lettura */
		Scanner scan;
		scan = new Scanner(new File(queryFile));
		Pattern delimiter = Pattern.compile("\\p{IsPunctuation}(I|W|N|A)");
		scan.useDelimiter(delimiter);
		
		// count number of queries
		int numQuery = 0;
		while(scan.hasNextLine()) {
			String delimiterStr = scan.findInLine(delimiter);
			while (!delimiterStr.equals(".W")) {
					scan.next();
				delimiterStr = scan.findInLine(delimiter);
			}
			numQuery++;
			scan.next(); 
		}
		scan.close();
		
		scan = new Scanner(new File(queryFile));
		scan.useDelimiter(delimiter);
		// Playing with +1-1 on array index is confusing and error prone, 
		// adding "+1" on dimension we guarantee that array index corresponds 
		// to the query ID since SMART query enumeration start from 1
		queries = new String[numQuery+1];
		queries[0] = "NO_QUERY_WILL_BE_PLACED_HERE";
		int idCounter = 1;
		while (scan.hasNextLine()) {
			String delimiterStr = scan.findInLine(delimiter);
			while (!delimiterStr.equals(".W")) {
				scan.next();
				delimiterStr = scan.findInLine(delimiter);
			}
			queries[idCounter] = scan.next();
			
			//System.out.println("id:"+idCounter);
			//System.out.println("query:"+queries[idCounter]);
			idCounter++;
		}
		scan.close();
	}
	
	private void initRelevantDocs(String relevantsFile) throws FileNotFoundException {
		// counting occurrencess
		int[] occurrences = new int[queries.length] ;
		for (int i = 0 ; i < occurrences.length ; i++) {
			occurrences[i] = 0;
		}
		
		Scanner scan;
		scan = new Scanner(new File(relevantsFile));
		//Pattern delimiter = Pattern.compile("");
		//scan.useDelimiter(delimiter);
		while(scan.hasNext() && scan.hasNextLine()) {
			String stringIDNum = scan.next();
			int queryID = Integer.parseInt(stringIDNum);
			occurrences[queryID]++;
			scan.next();
			scan.next();
			scan.next();
		}
		scan.close();
		
		System.out.println("Hey printing");
		
		for (int i = 1 ; i < occurrences.length ; i++) {
			relevantDocs[i] = new int[occurrences[i]];
		}
		
		// Generate relevant docs id
		int queryId = 1;
		int docCounter = 0;
		scan = new Scanner(new File(relevantsFile));
		while(scan.hasNext() && scan.hasNextLine()) {
			scan.next();
			
			String stringIDNum = scan.next();
			int docID = Integer.parseInt(stringIDNum);
			relevantDocs[queryId][docCounter] = docID;
			docCounter++;
			if (docCounter == occurrences[queryId]) {
				queryId++;
				docCounter = 0;
			}
			
			scan.next();
			scan.next();
			
		}
		scan.close();
	}
	
	/**
	 * Get natural precision recall evaluation as a String
	 * @param queryString, query string
	 * @param model, similarity model adopted
	 * @param tolerantMode, tolerant mode that will be activated
	 * @param field, document field
	 * @return
	 * @throws Exception
	 */
	public String getTextPrecisionRecallEvaluation
		(int queryID,ModelsID model,TolerantID tolerant,DocFields field)
		throws Exception {
		
		double[] precNatRecLevel = getNaturalPrecRecEval
				(queryID, model,tolerant, field);
					
		String result = "Precision on natural recall level: "+System.getProperty("line.separator");
		for (int i = 0 ; i < precNatRecLevel.length ; i++) {
			result += "Natural Recall level precision "+(i+1)+"/"+precNatRecLevel.length+" "
					+precNatRecLevel[i]*100+"% " + System.getProperty("line.separator");
		}
		
		double[] precStandardRecLevel = getStandardPrecRecEval
				(queryID, model, tolerant, field);
		result += "Precision on standard recall level: "+System.getProperty("line.separator");
		for (int i = 0 ; i < precStandardRecLevel.length ; i++) {
			result += //"Standard Recall level precision "+(i+1)+"/"+precStandardRecLevel.length+" "
					precStandardRecLevel[i]+" " + System.getProperty("line.separator");
		}
		
		/*result += System.getProperty("line.separator")+"Result docs: ";
		for (int i = 0 ; i < resultDocs.length ; i++) {
			result += resultDocs[i]+" ";
		}
		result += System.getProperty("line.separator")+"Relevant docs: ";
		for (int i = 0 ; i < relevantDocNum ; i++) {
			result += relevantDoc[i]+" ";
		}*/
		result += System.getProperty("line.separator");
		
		return result;
		
	}
	
	public String getWholePrecRecEval(ModelsID model,TolerantID tolerant,
			DocFields field) throws Exception {
		
		String result = "Benchmark Average precision recall:"+System.getProperty("line.separator");
		double[] wholeRecLevel = getAverageStandardPrecRecEval(model, tolerant, field);

		for (int i = 0 ; i < wholeRecLevel.length ; i++) {
			result += wholeRecLevel[i]+" " + System.getProperty("line.separator");
		}
		
		return result;
	}
	
	// Ottieni l'array di precision ai livelli standard, eventuamente
	// interpolata da quella naturale, funzione piu' complessa del progetto
	private double[] getStandardPrecRecEval
		(int queryID,ModelsID model,TolerantID tolerant,DocFields field)
		throws Exception {
		
		double[] natPrec = getNaturalPrecRecEval(queryID, model,tolerant,field);
		int numNat = natPrec.length;

		/* Il calcolo avviene tramite la seguente formula
		 * Formula j-th standard P(rj) = max(rj<=r<=rj+1){P(r) naturale)} */
		double[] stdPrec = new double[STANDARD_RECALL];
		final int TEN = STANDARD_RECALL-1;

		stdPrec[TEN] = natPrec[numNat-1];
		boolean onlyOne = numNat == 1;
		boolean sameQty = TEN == numNat;
		if (onlyOne || sameQty) { // in entrambi i casi devo copiare
			for (int i = 1 ; i < TEN ; i++) {
				if (onlyOne) stdPrec[i] = natPrec[0];
				else stdPrec[i] = natPrec[i-1];
			}
			if (sameQty) stdPrec[0] = stdPrec[1];
			else stdPrec[0] = natPrec[0];
		}
		boolean noOverFlow;
		if (numNat < TEN) {
			for (int i = 1 ; i < TEN ; i++) {
				int k = 0;
				boolean natRecSmaller;
				do { k++;
				     natRecSmaller = (double)k/numNat <= (double)i/TEN;
				     noOverFlow = k <= numNat;
				} while (natRecSmaller && noOverFlow);
				double confrontVal = natPrec[k-1];

				if (k == numNat) {
					stdPrec[i] = confrontVal;
					continue;
				}

				noOverFlow = (i+1) <= TEN;
				boolean nextStdRecIncludedSameRec = (double)(i+1)/TEN <= (double)k/numNat;
				if (noOverFlow && !nextStdRecIncludedSameRec)
					stdPrec[i] = Math.max(confrontVal,natPrec[k]);
				else
					stdPrec[i] = confrontVal;

			}
			stdPrec[0] = stdPrec[1];
		}
		if (numNat > TEN) {
			for (int i = 0 ; i < TEN ; i++) {
				// mando due carabinieri ad affiancarsi al primo j a sx
				// al j+1 a dx (il teorema in inglese si dice squeeze theorem)
				int k = 0;
				boolean dxStart;
				do { k++;
				     noOverFlow = k <= numNat;
				     dxStart = (double)k/numNat > (double)i/TEN;
				} while (noOverFlow && !dxStart);
				int k_start = k;


                		boolean sxEnd;
				do { k++;
				    noOverFlow = k <= numNat;
				    sxEnd = (double)k/numNat > (double)(i+1)/TEN;
				} while (noOverFlow && !sxEnd);
				int k_stop = k;

				double max = natPrec[k_start-1];
				for (int j = k_start+1 ; j < k_stop ; j++) {
					if (max < natPrec[j-1])
						max = natPrec[j-1];
				}
				stdPrec[i] = max;

			}
		}
				
		return stdPrec;
	}
	
	private double[] getAverageStandardPrecRecEval
		(ModelsID model,TolerantID tolerant,DocFields field) throws Exception {
		double[] standardPrecRec = new double[STANDARD_RECALL];
		for (int i = 0 ; i < STANDARD_RECALL ; i++)
			standardPrecRec[i] = 0;
		for (int i = 1 ; i < queries.length ; i++) {
			double[] stnd = getStandardPrecRecEval(i,model,tolerant,field);
			for (int j = 0 ; j < STANDARD_RECALL ; j++)
				standardPrecRec[j] += stnd[j]; 
		}
		for (int i = 0 ; i < STANDARD_RECALL ; i++)
			standardPrecRec[i] /= queries.length;
		
		return standardPrecRec;
	}
	
	// Ottieni l'array di precision a livelli naturali di recall
	private double[] getNaturalPrecRecEval
	(int queryID,ModelsID model,TolerantID tolerant,DocFields field)
		throws Exception {
	IndexExplorer ie = new IndexExplorer(getQuery(queryID),searchIndexPath,
			model,tolerant,field);
	
	int[] resultDocs = ie.getResultIDArray();
	int[] relevantDoc = relevantDocs[queryID];
	
	double[] precNatRecLevel = new double[relevantDoc.length];
	int naturalLevel = 1;
	for (int i = 0 ; i < resultDocs.length ; i++) {
		if (isRelevant(relevantDoc,resultDocs[i])) {
			precNatRecLevel[naturalLevel-1] = naturalLevel/(double)(i+1);
			naturalLevel++;
		}
	}
	
	for (int i = naturalLevel ; i < precNatRecLevel.length ; i++) {
		precNatRecLevel[i] = 0F; // if not all relevant document are found
	}
	
	return precNatRecLevel;
}
	
	private boolean isRelevant(int[] relevantDocs, int id) {
		int numRelevantDocs = relevantDocs.length;
		for (int i = 0 ; i < numRelevantDocs ; i++) {
			if (relevantDocs[i] == id)
				return true;
		}
		return false;
	}
}
