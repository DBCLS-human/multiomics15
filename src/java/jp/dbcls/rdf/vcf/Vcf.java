/**
 * class: Vcf
 * brief: VCF data management class.
 *
 * @author Satoshi Tanaka
 *
 * Copyright (c) 2016 DBCLS, S.Tanaka
 * Released under the MIT license.
 * https://github.com/DBCLS-human/multiomics15/blob/master/LICENSE
 */

package jp.dbcls.rdf.vcf;

import java.util.ArrayList;
import java.util.StringTokenizer;


/**
 * VCF information
 * @author Satoshi
 *
 */
public class Vcf {
	/** experiment */
	String m_experiment;

	/** chrom */
	String m_chrom;

	/** pos */
	String m_pos;

	/** id0 */
	String m_id0;

	/** id1 */
	String m_id1;

	/** id2 */
	String m_id2;

	/** id3 */
	String m_id3;

	/** id4 */
	String m_id4;

	/** id5 */
	String m_id5;

	/** id6 */
	String m_id6;

	/** id7 */
	String m_id7;

	/** id8 */
	String m_id8;

	/** id9 */
	String m_id9;

	/** ref */
	String m_ref;

	/** alt */
	String m_alt;

	/** qual */
	String m_qual;

	/** filter */
	String m_filter;

	/** info */
	String m_info;

	/** format */
	String m_format;

	/** detail */
	String m_detail;

	/**
	 * constructor
	 * @param experiment experiment
	 * @param chrom chrom
	 * @param pos pos
	 * @param id id
	 * @param ref ref
	 * @param alt alt
	 * @param qual qual
	 * @param filter filter
	 * @param info info
	 * @param format format
	 * @param detail detail
	 */
	public Vcf(
			String experiment,
			String chrom,
			String pos,
			String id,
			String ref,
			String alt,
			String qual,
			String filter,
			String info,
			String format,
			String detail
	) {
		m_experiment = experiment;
		m_chrom = chrom.replace( ".fa",  "" ).replace( "chr",  "" ).replace( "c", "" );
		m_pos = pos;
		m_ref = ref;
		m_alt = alt;
		m_qual = qual;
		m_filter = filter;
		m_info = info;
		m_format = format;
		m_detail = detail;

		StringTokenizer st = null;
		if( id.length() > 1 ) {
			st = new StringTokenizer( id, "; " );
		}

		m_id0 = getNextString( st );
		m_id1 = getNextString( st );
		m_id2 = getNextString( st );
		m_id3 = getNextString( st );
		m_id4 = getNextString( st );
		m_id5 = getNextString( st );
		m_id6 = getNextString( st );
		m_id7 = getNextString( st );
		m_id8 = getNextString( st );
		m_id9 = getNextString( st );
	}

	/**
	 * gets the next string
	 * @param st string tokenizer
	 * @return next string
	 */
	protected String getNextString( StringTokenizer st ) {
		String token = null;
		if( st != null && st.hasMoreTokens() ) {
			token = st.nextToken();
		}

		return token;
	}

	/**
	 * gets the string value
	 * @param key key
	 * @return value
	 */
	public String getValue( String key ) {
		// return value
		String value = "";

		//
		if( key.contains( ":" ) ) {
			value = getDetailValue( key );
		}
		else if( key.contains( "|" ) ) {
			value = getInformationValue( key );
		}
		else {
			value = getKeyValue( key );
		}

		return value;
	}

	/**
	 * gets the key value
	 * @param key key
	 * @return key value
	 */
	String getKeyValue( String key ) {
		// array
		String[][] values = {
				{ "Experiment", m_experiment },
				{ "Chrom",      m_chrom },
				{ "Pos",        m_pos },
				{ "Id0",        m_id0 },
				{ "Id1",        m_id1 },
				{ "Id2",        m_id2 },
				{ "Id3",        m_id3 },
				{ "Id4",        m_id4 },
				{ "Id5",        m_id5 },
				{ "Id6",        m_id6 },
				{ "Id7",        m_id7 },
				{ "Id8",        m_id8 },
				{ "Id9",        m_id9 },
				{ "Ref",        m_ref },
				{ "Alt",        m_alt },
				{ "Qual",       m_qual },
				{ "Filter",     m_filter },
				{ "Info",       m_info },
				{ "Format",     m_format },
				{ "Detail",     m_detail },
		};

		// value
		String value = null;
		for( int i = 0; i < values.length && value == null; i++ ) {
			if( key.equals( values[ i ][ 0 ] ) ) {
				value = values[ i ][ 1 ];
			}
		}

		return value;
	}

	/**
	 * gets the detail value
	 * @param key key
	 * @return detail value
	 */
	String getDetailValue( String key ) {
		// search
		int index = key.indexOf( ":" );

		String[] formats = getDetailValues( m_format );
		String[] values = getDetailValues( getKeyValue( key.substring( 0, index ) ) );

		key = key.substring( index + 1 );
		index = -1;
		for( int i = 0; i < formats.length && index < 0; i++ ) {
			if( key.equals( formats[ i ] ) ) {
				index = i;
			}
		}

		return values[ index ];
	}

	/**
	 * gets the details values
	 * @param values
	 * @return
	 */
	String[] getDetailValues( String values ) {
		ArrayList< String > arrayList = new ArrayList< String >();
		StringTokenizer st = new StringTokenizer( values, ": \t" );
		while( st.hasMoreTokens() ) {
			arrayList.add( st.nextToken() );
		}

		String array[] = arrayList.toArray( new String[ arrayList.size() ] );
		return array;
	}

	/**
	 * gets the information value
	 * @param key key
	 * @return information value
	 */
	String getInformationValue( String key ) {
		int index = key.indexOf( "|" );

		String value = null;
		StringTokenizer st = new StringTokenizer( getKeyValue( key.substring( 0, index ) ), ";" );
		key = key.substring( index + 1 );

		while( st.hasMoreTokens() && value == null ) {
			String token = st.nextToken();
			index = token.indexOf( "=" );
			String name = token.substring( 0, index ).trim();
			if( name.equals( key ) ) {
				value = token.substring( index + 1 ).trim();
			}
		}

		return value;
	}

	/**
	 * parses line
	 * @param experiment experiment name
	 * @param line vcf file line
	 * @return vcf object.
	 */
	public static Vcf parseLine( String experiment, String line ) {
		StringTokenizer st = new StringTokenizer( line );
		Vcf vcf = new Vcf(
				experiment,
				st.nextToken(),
				st.nextToken(),
				st.nextToken(),
				st.nextToken(),
				st.nextToken(),
				st.nextToken(),
				st.nextToken(),
				st.nextToken(),
				st.nextToken(),
				st.nextToken()
		);

		return vcf;
	}
}
