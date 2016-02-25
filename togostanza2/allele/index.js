/**
 * file:  index.js
 * brief: Allele Stanza file
 *
 * @author Satoshi Tanaka
 *
 * Copyright (c) 2016 DBCLS, S.Tanaka
 * Released under the MIT license.
 * https://github.com/DBCLS-human/multiomics15/blob/master/LICENSE
 */

Stanza(
    function(stanza, params) {
	var url = 'http://humanrdf.dbcls.jp/sparql';
	
	var q = stanza.query(
	    {
		endpoint:   url,
		template:   'stanza.rq',
		parameters: params
	    }
	);
	console.log( q );

	var search = 'Sample=' + params.sample + ', '
            + 'Chromosome=' + params.chromosome + ', '
            + 'Position=' + params.position;

	q.then(
	    function( data ) {
		var rows = data.results.bindings;

		var html = JSON.stringify( data );
		console.log( html );
		
		stanza.render(
		    {
			template: "stanza.html",
			parameters: {
			    alleles: rows,
			    search:  search
			}
		    }
		);
	    }
	);
    }
);
	
