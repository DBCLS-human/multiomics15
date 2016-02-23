/**
 * File: faldo.js
 * brief: faldo operation functions
 *
 * @author Satoshi Tanaka
 *
 * Copyright (c) 2016 DBCLS, S.Tanaka
 * Released under the MIT license.
 * https://github.com/DBCLS-human/multiomics15/blob/master/LICENSE
 */


// namespace
var dbclsFaldo = {};

// inclusion relation (enumeration)
dbclsFaldo.InclusionRelation = {};
dbclsFaldo.InclusionRelation.NOT_EXIST            = 0;
dbclsFaldo.InclusionRelation.DIFFERENT_REFFERENCE = 1;
dbclsFaldo.InclusionRelation.SAME_REFERENCE       = 2;
dbclsFaldo.InclusionRelation.OVERLAPPING          = 3;
dbclsFaldo.InclusionRelation.INCLUDING            = 4;


// get range
dbclsFaldo.getRegion = function( region ) {
	// sparql
	var sparql = 'select distinct replace( str( ?reg ), ".*/", "" ) as ?name ?label replace( str( ?ref ), ".*#", "" ) as ?chromosome if( ?begin_type = faldo:ReverseStrandPosition, "reverse", "forward" ) as ?type ?begin ?end where {'
	           + '    ?reg ?p ?location;'
	           + '        rdfs:label ?label.'
	           + '    ?location a faldo:Region;'
		       + '        faldo:begin ?begin_pos;'
	           + '        faldo:end ?end_pos.'
	           + '    ?begin_pos a ?begin_type;'
	           + '        faldo:reference ?ref;'
	           + '        faldo:position ?begin.'
	           + '    ?end_pos a ?end_type;'
	           + '         faldo:position ?end.'
	           + '    filter( ?begin_type = ?end_type ).'
	           + '    filter( ?begin_type in( faldo:ForwardStrandPosition, faldo:ReverseStrandPosition) ).'
	           + '    filter( !isBlank( ?reg ) ).'
	           + '    filter ( replace( str( ?reg ), ".*/", "" ) = "' + region + '" ).'
	           + '} limit 1';
	sparql = dbclsFaldo.getPrefix() + sparql;

	// execute
	var objects = dbclsSparql.executeQuery( sparql );
	if( objects.length === 0 ) {
		return null;
	}

	return objects[ 0 ];
}

// get inclusion relation
dbclsFaldo.getInclusionRelation = function( region1, region2 ) {
	var status = dbclsFaldo.InclusionRelation.NOT_EXIST;

	var range1 = dbclsFaldo.getRegion( region1 );
	var range2 = dbclsFaldo.getRegion( region2 );

	if( range1 != null && range2 != null ) {
		if( range1[ 'ref'] === range2[ 'ref' ] ) {
			var start1 = parseInt( range1[ 'begin' ], 10 );
			var start2 = parseInt( range2[ 'begin' ], 10 );
			var end1 = parseInt( range1[ 'end' ], 10 );
			var end2 = parseInt( range2[ 'end' ], 10 );
			var min1 = Math.min( start1, end1 );
			var max1 = Math.max( start1, end1 );
			var min2 = Math.min( start2, end2 );
			var max2 = Math.max( start2, end2 );

			if( ( min1 <= min2 && max1 >= max2 ) || ( min2 <= min1 && max2 >= max1 ) ) {
				status = dbclsFaldo.InclusionRelation.INCLUDING;
			}
			else if( ( min2 >= min1 && min2 <= max1 ) || ( max2 >= min1 && max2 <= max1 ) ) {
				status = dbclsFaldo.InclusionRelation.OVERLAPPING;
			}
		    else {
		    	status = dbclsFaldo.InclusionRelation.SAME_REFERENCE;
		    }
		}
		else {
			status = dbclsFaldo.InclusionRelation.DIFFERENT_REFERENCE;
		}
	}
	return status;
}

// is overlapping
dbclsFaldo.isOverlapping = function( region1, region2 ) {
	var status = dbclsFaldo.getInclusionRelation( region1, region2 );
	var result = ( status >= dbclsFaldo.InclusionRelation.OVERLAPPING );
	return result;
}

// is including
dbclsFaldo.isIncluding = function( region1, region2 ) {
	var status = dbclsFaldo.getInclusionRelation( region1, region2 );
	var result = ( status >= dbclsFaldo.InclusionRelation.INCLUDING );
	return result;
}

// get regions in range
dbclsFaldo.getRegionsInRange = function( chrom, start, end, include ) {
	// sparql
	var range_filter = null;
	if( include ) {
		range_filter = '?begin >= ' + start + ' && ?end <= ' + end;
	}
	else {
		range_filter = '?end >= ' + start + ' && ?begin <= ' + end;
	}

	var sparql = 'select distinct replace( str( ?reg ), ".*/", "" ) as ?name ?label replace( str( ?ref ), ".*#", "" ) as ?chromosome if( ?begin_type = faldo:ReverseStrandPosition, "reverse", "forward" ) as ?type ?begin ?end where {'
	           + '    ?reg ?p ?location;'
	           + '        rdfs:label ?label.'
	           + '    ?location a faldo:Region;'
	           + '        faldo:begin ?begin_pos;'
	           + '        faldo:end ?end_pos.'
	           + '    ?begin_pos a ?begin_type;'
	           + '        faldo:reference ?ref;'
	           + '        faldo:position ?begin.'
	           + '    ?end_pos a ?end_type;'
	           + '         faldo:position ?end.'
	           + '    filter( ?begin_type = ?end_type ).'
	           + '    filter( ?begin_type in( faldo:ForwardStrandPosition, faldo:ReverseStrandPosition) ).'
	           + '    filter( !isBlank( ?reg ) ).'
	           + '    filter( replace( str( ?ref ), ".*#", "" ) = "' + chrom + '" ).'
	           + '    filter( ' + range_filter + ' ).'
	           + '} order by ?name';
	sparql = dbclsFaldo.getPrefix() + sparql;

	// execute
	var objects = dbclsSparql.executeQuery( sparql );

	return objects;
}

// get upstream
dbclsFaldo.getUpstreamRegions = function( region, length, include ) {
	// region
	var obj = dbclsFaldo.getRegion( region );
	if( obj === null ) {
		return null;
	}

	// range
	var chrom = obj[ 'chromosome' ];
	var type = obj[ 'type' ];
	var start = parseInt( obj[ 'begin' ], 10 );
	var end = parseInt( obj[ 'end' ], 10 );
	if( type === 'reverse' ) {
		start = end + 1;
		end = end + length;
	}
	else {
		end = start - 1;
		start = start - length;
	}

	var objects = dbclsFaldo.getRegionsInRange( chrom, start, end, include );

	return objects;
}

// get prefix
dbclsFaldo.getPrefix = function() {
    var prefix = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
	       + "PREFIX rdfs: <http://www.w3.org/TR/rdf-schema/#> "
	       + "PREFIX dct: <http://purl.org/dc/terms/> "
	       + "PREFIX faldo: <http://biohackathon.org/resource/faldo#> "
	       + "PREFIX obo: <http://purl.obolibrary.org/obo/> "
	       + "PREFIX kero: <http://dbtss.hgc.jp/ontology/kero.owl#> "
	       + "PREFIX chr: <http://dbtss.hgc.jp/rdf/chromosome/9606/GRCh38#> "
	       + "PREFIX file: <http://dbtss.hgc.jp/rdf/BSseqFile/> "
	       + "PREFIX bs: <http://dbtss.hgc.jp/rdf/BSseq/> ";

    return prefix;
}