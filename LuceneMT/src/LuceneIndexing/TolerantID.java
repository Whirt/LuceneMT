package LuceneIndexing;

public enum TolerantID {
	PREINFIX, 	// Supporta sia pre/postfix che * interni
			  	// WILDCARD si differenzia dal Postfix perchè Lucene
			  	// implementa la Reverse nel caso di Postfix, con * interno
			  	// addotta altri meccanismi
	POSTFIX, 	// disattiva le postfix
	ALTERN,   	// vengono suggerite alternative in caso di query giuste
	FUZZY	// nel caso non si ricordi esattamente una parola
}
