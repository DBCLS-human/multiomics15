/**
 * class: Vcf2Rdf
 * brief: The class for converting vcf data to rdf format.
 *
 * @author Satoshi Tanaka
 *
 * Copyright (c) 2016 DBCLS, S.Tanaka
 * Released under the MIT license.
 * https://github.com/DBCLS-human/multiomics15/blob/master/LICENSE
 */

package jp.dbcls.rdf.vcf;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;

import jp.dbcls.CommandLineTool;


/**
 * vcf2rdf class
 * @author Satoshi
 *
 */
public class Vcf2Rdf {
	/** template file path */
	private static final String m_templatePath = "jp/dbcls/rdf/vcf/template.ttl";

	/**
	 * vcf2rdf
	 * @param args
	 * @throws IOException
	 */
	public static void main( String[] args ) throws IOException {
		// time
		long t0 = System.currentTimeMillis();

		// arguments
		String sample = CommandLineTool.getOption( "sample", args );
		String inFile = CommandLineTool.getOption( "in", args );
		String outFile = CommandLineTool.getOption( "out", args );
		String template = CommandLineTool.getOption( "template", args );

		if( sample == null || inFile == null ) {
			showUsage();
			return;
		}

		// files
		BufferedReader in = new BufferedReader( new FileReader( new File( inFile ) ) );
		PrintStream out = System.out;
		if( outFile != null ) {
			out = new PrintStream( new BufferedOutputStream( new FileOutputStream( new File( outFile ) ) ) );
		}

		Vcf2Rdf vcf2rdf = new Vcf2Rdf();
		vcf2rdf.convert( in, out, sample, template );

		out.flush();
		out.close();

		long t1 = System.currentTimeMillis();

		String msg = String.format( "Finished!! (%.2f)", (double)( t1 - t0 ) / 1000.0 );

		System.out.println( msg );
	}

	/**
	 * shows usage
	 */
	static void showUsage() {
		String line = "java jp.trans_it.rdf.Vcf2Rdf -sample [sample ID] -in [vcf file] -out [output file] -template [template file]";
		System.out.println( line );
	}

	/**
	 * convert vcf file to rdf
	 * @param in input file
	 * @param out output file
	 * @param sample sample name
	 * @param template template file
	 * @throws IOException
	 */
	void convert( BufferedReader in, PrintStream out, String sample, String template ) throws IOException {
		// template
		InputStream templateStream = null;
		if( template == null ) {
			templateStream = getClass().getClassLoader().getResourceAsStream( m_templatePath );
		}
		else {
			templateStream = new FileInputStream( template );
		}

		BufferedReader reader = new BufferedReader( new InputStreamReader( templateStream ) );

		boolean inVcf = false;
		ArrayList< String > vcfLines = new ArrayList< String >();

		String line = null;
		while( ( line = reader.readLine() ) != null ) {
			if( inVcf ) {
				vcfLines.add( line );
			}
			else if( line.trim().equals( "[vcf]" ) ) {
				inVcf = true;
			}
			else {
				out.println( line );
			}
		}

		reader.close();
		templateStream.close();

		// write sample
		line = null;
		while( ( line = in.readLine() ) != null ) {
			line = line.trim();

			if( !line.startsWith( "#" ) ) {
				Vcf vcf = Vcf.parseLine( sample, line );
				out.println( "" );
				writeVcf( vcf, vcfLines, out );
			}
		}
		out.close();
	}

	/**
	 * writes vcf lines
	 * @param vcf vcf object
	 * @param lines lines
	 * @param out output stream
	 */
	static void writeVcf( Vcf vcf, ArrayList< String > lines, PrintStream out ) {
		for( int i = 0; i < lines.size(); i++ ) {
			String line = lines.get( i );

			boolean written = writeVcf( vcf, line, out );
			if( written ) {
				out.println( "" );
			}
		}
	}

	/**
	 * write vcf line
	 * @param vcf vcf object
	 * @param line line
	 * @param out output stream
	 * @return written flag
	 */
	static boolean writeVcf( Vcf vcf, String line, PrintStream out ) {
		String ttlLine = getTtlLine( vcf, line );
		if( ttlLine != null ) {
			out.print( ttlLine );
		}

		return ( ttlLine != null );
	}

	/**
	 * gets the TTL line
	 * @param vcf vcf object
	 * @param line line
	 * @return TTL line
	 */
	static String getTtlLine( Vcf vcf, String line ) {
		// return value
		String ttlLine = "";

		// check the line
		int index = line.indexOf( "{{" );
		if( index < 0 ) {
				return line;
		}

		ttlLine = line.substring( 0, index );

		// key
		line = line.substring( index + 2 );

		index = line.indexOf( "}}" );
		if( index < 0 ) {
			return null;
		}

		String key = line.substring( 0, index ).trim();
		String value = vcf.getValue( key );
		if( value == null || value.length() == 0 ) {
			return null;
		}

		ttlLine += value.trim();

		String after = getTtlLine( vcf, line.substring( index + 2 ) );
		if( after == null ) {
			return null;
		}

		ttlLine += after;

		return ttlLine;
	}
}
