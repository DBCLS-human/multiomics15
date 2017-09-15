/**
 * File: sparql.js
 * brief: SPARQL operation functions
 *
 * @author Satoshi Tanaka
 *
 * Copyright (c) 2016 DBCLS, S.Tanaka
 * Released under the MIT license.
 * https://github.com/DBCLS-human/multiomics15/blob/master/LICENSE
 */


// namespace
var dbclsSparql = {};

// default endpoint
dbclsSparql.endpoint = 'http://humanrdf.dbcls.jp/sparql';

// temporary query result
dbclsSparql.queryResult = null;

// ID count
dbclsSparql.idCounter = 0;

// sets the endpont
dbclsSparql.setEndpoint = function( endpoint ) {
	dbclsSparql.endpoint = endpoint;
}

// set result
dbclsSparql.setResult = function( result ) {
	dbclsSparql.queryResult = result;
}

// execute SPARQL
dbclsSparql.executeWithEndpoint = function( sparql, endpoint ) {
	// initialize
	dbclsSparql.setResult( null );

	// endpoint
	dbclsSparql.setEndpoint( endpoint );

	// call
	$.ajax(
		{
			url: endpoint,
			headers: {
				"Accept": "application/sparql-results+json"
			},
			data: {query: sparql},
			success: dbclsSparql.setResult,
			error: dbclsSparql.setResult,
			type: 'GET',
			cache: false,
			dataType: 'json',
			async: false
		}
	);

	var result = dbclsSparql.queryResult;

	var html = JSON.stringify( result );
	console.log( html );

	return result;
}

// execute sparql
dbclsSparql.exec = function( sparql ) {
	return  dbclsSparql.executeWithEndpoint( sparql, dbclsSparql.endpoint );
}

// gets headers from the result
dbclsSparql.getHeaders = function( result ) {
	var head = result[ 'head' ];
	var headers = head[ 'vars' ];

	return headers;
}

// gets objects from the result
dbclsSparql.getObjects = function( result ) {
	// bindings
	var results = result[ 'results' ];
	var bindings = results[ 'bindings' ];

	// return value
	var values = new Array();

	for( var i = 0; i < bindings.length; i++ ) {
		var binding = bindings[ i ];

		var obj = new Object();

		for( var key in binding ) {
			var element = binding[ key ];
			var value = element[ 'value' ];
			obj[ key ] = value;
		}

		values[ i ] = obj;
	}

	return values;
}

// execute query
dbclsSparql.executeQuery = function( sparql ) {
	var result = dbclsSparql.exec( sparql );
	var objects = dbclsSparql.getObjects( result );

	return objects;
}

// add table
dbclsSparql.addTableFromResult = function( box, classPrefix, result ) {
	// result
	var headers = dbclsSparql.getHeaders( result );
	var objects = dbclsSparql.getObjects( result );

	dbclsSparql.addTableFromHeadersAndObjects( box, classPrefix, headers, objects );
}

// add table
dbclsSparql.addTableFromValue = function( box, classPrefix, value, header ) {
	var array = new Array();
	array[ 0 ] = value;

	dbclsSparql.addTableFromArray( box, classPrefix, array, header );
}

// add table
dbclsSparql.addTableFromArray = function( box, classPrefix, values, header ) {
	// class name
	var tableClass = dbclsSparql.getClassName( 'table', classPrefix );
	var rowClass = dbclsSparql.getClassName( 'row', classPrefix );
	var cellClass = dbclsSparql.getClassName( 'cell', classPrefix );
	var headerRowClass = dbclsSparql.getClassName( 'header_row', classPrefix );
	var headerCellClass = dbclsSparql.getClassName( 'header_cell', classPrefix );
	var contentRowClass = dbclsSparql.getClassName( 'content_row', classPrefix );
	var contentCellClass = dbclsSparql.getClassName( 'content_cell', classPrefix );
	var contentOddRowClass = dbclsSparql.getClassName( 'content_odd_row', classPrefix );
	var contentEvenRowClass = dbclsSparql.getClassName( 'content_even_row', classPrefix );

	// add table
	var div = box.html( '<div></div>' );
	var table = dbclsSparql.addTag( div, 'table', null );
	table.addClass( tableClass );

	// header
	if( header !== null && header.length > 0 ) {
		var headerRow = dbclsSparql.addTag( table, 'tr', null );
		headerRow.addClass( rowClass );
		headerRow.addClass( headerRowClass );

		var cell = dbclsSparql.addTag( headerRow, 'th', header );
		cell.addClass( cellClass );
		cell.addClass( headerCellClass );
	}

	for( var i = 0; i < values.length; i++ ) {
		var value = values[ i ];

		var row = dbclsSparql.addTag( table, 'tr', null );
		row.addClass( rowClass );
		row.addClass( contentRowClass );

		if( i % 2 === 0 ) {
			row.addClass( contentEvenRowClass );
		}
		else {
			row.addClass( contentOddRowClass );
		}

		var cell = dbclsSparql.addTag( row, 'td', values[ i ] );
		cell.addClass( cellClass );
		cell.addClass( contentCellClass );
	}
}

// get value from result
dbclsSparql.getValueFromResult = function( result ) {
	// result
	var headers = dbclsSparql.getHeaders( result );
	var objects = dbclsSparql.getObjects( result );
	if( headers.length === 0 || objects.length === 0 ) {
		return null;
	}

	var obj = objects[ 0 ];
	var key = headers[ 0 ];

	return obj[ key ];
}

// get array from result
 dbclsSparql.getArrayFromResult = function( result ) {
	// result
	var headers = dbclsSparql.getHeaders( result );
	var objects = dbclsSparql.getObjects( result );
	if( headers.length === 0 || objects.length === 0 ) {
		return null;
	}

	var key = headers[ 0 ];

	var array = new Array();
	for( var i = 0; i < values.length; i++ ) {
		var obj = objects[ i ];
		array[ i ] = obj[ key ];
	}

	return array;
}

//add table from headers and objects
dbclsSparql.addTableFromHeadersAndObjects = function( box, classPrefix, headers, objects ) {
	// class name
	var tableClass = dbclsSparql.getClassName( 'table', classPrefix );
	var rowClass = dbclsSparql.getClassName( 'row', classPrefix );
	var cellClass = dbclsSparql.getClassName( 'cell', classPrefix );
	var headerRowClass = dbclsSparql.getClassName( 'header_row', classPrefix );
	var headerCellClass = dbclsSparql.getClassName( 'header_cell', classPrefix );
	var contentRowClass = dbclsSparql.getClassName( 'content_row', classPrefix );
	var contentCellClass = dbclsSparql.getClassName( 'content_cell', classPrefix );
	var contentOddRowClass = dbclsSparql.getClassName( 'content_odd_row', classPrefix );
	var contentEvenRowClass = dbclsSparql.getClassName( 'content_even_row', classPrefix );

	// add table
	var div = box.html( '<div></div>' );
	var table = dbclsSparql.addTag( div, 'table', null );
	table.addClass( tableClass );

	var header = dbclsSparql.addTag( table, 'tr', null );
	header.addClass( rowClass );
	header.addClass( headerRowClass );

	for( var j = 0; j < headers.length; j++ ) {
		var cell = dbclsSparql.addTag( header, 'th', headers[ j ] );
		cell.addClass( cellClass );
		cell.addClass( headerCellClass );
	}

	for( var i = 0; i < objects.length; i++ ) {
		var obj = objects[ i ];

		var row = dbclsSparql.addTag( table, 'tr', null );
		row.addClass( rowClass );
		row.addClass( contentRowClass );

		if( i % 2 === 0 ) {
			row.addClass( contentEvenRowClass );
		}
		else {
			row.addClass( contentOddRowClass );
		}

		for( var j = 0; j < headers.length; j++ ) {
			var key = headers[ j ];
			var cell = dbclsSparql.addTag( row, 'td', obj[ key ] );
			cell.addClass( cellClass );
			cell.addClass( contentCellClass );
		}
	}
}

// add table from objects
dbclsSparql.addTableFromObjects = function( box, classPrefix, objects ) {
	// headers
	var keys = new Object();
	var headers = new Array();
	var array_size = 0;
	for( var i = 0; i < objects.length; i++ ) {
		var obj = objects[ i ];
		for( key in obj ) {
			if( keys[ key ] !== 1 ) {
				headers[ array_size ] = key;
				array_size++;
				keys[ key ] = 1;
			}
		}
	}

	// add table
	dbclsSparql.addTableFromHeadersAndObjects( box, classPrefix, headers, objects );
}

// adds select
dbclsSparql.addOptionsFromArray = function( select, options ) {
	for( var i = 0; i < options.length; i++ ) {
		var option = dbclsSparql.addTag( select, 'option', options[ i ] );
	}
}

// gets the class name
dbclsSparql.getClassName = function( base, prefix ) {
	var className = base;
	if( prefix !== null && prefix.length > 0 ) {
		className = prefix + '_' + base;
	}
	return className;
}

// add tab
dbclsSparql.addTag = function( parent, tag, content ) {
	// check the parameter
	if( content === null ) {
		content = '';
	}

	// id
	var id = 'dbcls_sparql_tag_id_' + dbclsSparql.idCounter;
	dbclsSparql.idCounter++;

	var html = '<' + tag + ' id="' + id + '">' + content + '</' + tag + '>';
	parent.append( html );

	return $( '#' + id );
}
