/**
 * class: MapCreater
 * brief: The class for creating d2rq/ontop mapping file.
 *
 * @author Satoshi Tanaka
 *
 * Copyright (c) 2016 DBCLS, S.Tanaka
 * Released under the MIT license.
 * https://github.com/DBCLS-human/multiomics15/blob/master/LICENSE
 */

package jp.dbcls.rdf.mapping;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;
import java.util.StringTokenizer;

import jp.dbcls.CommandLineTool;

/**
 * map creater
 * @author Satoshi
 *
 */
public class MapCreater {
	/**
	 * constructor
	 */
	public MapCreater() {
	}

	/**
	 * creates
	 * @param template template file path
	 * @param out output file path
	 * @param props properties file path
	 * @throws IOException io exception
	 */
	public void create( String template, String out, String props ) throws IOException {
		// properties
		Properties properties = loadProperties( props );

		// template
		InputStream in = openTemplate( template );
		BufferedReader reader = new BufferedReader( new InputStreamReader( in ) );

		// output file
		PrintWriter writer = new PrintWriter(
				new BufferedWriter( new FileWriter( out ) )
		);

		// write mapping file
		writeMappingFile( reader, writer, properties );

		// close
		writer.flush();
		writer.close();
		reader.close();
		in.close();
	}

	/**
	 * loads properties
	 * @param props properties file
	 * @return prpperties
	 * @throws IOException io exception
	 */
	Properties loadProperties( String props ) throws IOException {
		// input
		InputStream in = null;
		if( props == null ) {
			in = getClass().getClassLoader().getResourceAsStream( "jp/dbcls/rdf/mapping/tables.properties" );
		}
		else {
			in = new FileInputStream( props );
		}

		// load
		Properties properties = null;
		if( in != null ) {
			properties = new Properties();
			properties.load( in );
		}

		return properties;
	}

	/**
	 * opens template
	 * @param template template file path
	 * @return template file iput stream
	 * @throws IOException io exception
	 */
	InputStream openTemplate( String template ) throws IOException {
		InputStream in = null;

		File file = null;
		if( template != null ) {
			file = new File( template );
		}
		if( file != null && file.exists() ) {
			in = new FileInputStream( file );
		}
		else {
			String path = "jp/dbcls/rdf/mapping/" + template;
			in = getClass().getClassLoader().getResourceAsStream( path );
		}

		return in;
	}

	/**
	 * writes mapping file
	 * @param template template file stream
	 * @param out out put file stream
	 * @param props properties
	 * @throws IOException io exception
	 */
	void writeMappingFile( BufferedReader template, PrintWriter out, Properties props ) throws IOException {
		// map
		HashMap< String, String > propsMap = new HashMap< String, String >();
		Enumeration<Object> keys = props.keys();
		while( keys.hasMoreElements() ) {
			String key = (String)keys.nextElement();
			String value = props.getProperty( key );
			propsMap.put( key,  value );
		}

		// read
		String line = null;
		String table = null;
		ArrayList< String > tableLines = new ArrayList< String >();

		while( ( line = template.readLine() ) != null ) {
			if( table == null ) {
				if( line.startsWith( "[" ) ) {
					line = line.substring( 1 );
					int index = line.indexOf( "]" );
					if( index >= 0 ) {
						table = line.substring( 0, index );
					}
				}
				else {
					String tmpLine = getLine( line, propsMap );
					if( tmpLine != null ) {
						out.println( tmpLine );
					}
				}
			}
			else {
				String endTag = "[/" + table + "]";
				if( line.indexOf( endTag ) >= 0 ) {
					String value = props.getProperty( table );
					StringTokenizer st = new StringTokenizer( value, ", \t" );
					while( st.hasMoreTokens() ) {
						propsMap.put( table,  st.nextToken() );

						for( int i = 0; i < tableLines.size(); i++ ) {
							String tableLine = getLine( tableLines.get( i ), propsMap );
							if( tableLine != null ) {
								out.println( tableLine );
							}
						}
						out.println( "" );
					}

					propsMap.put( table,  props.getProperty( table ) );
					table = null;
					tableLines.clear();
				}
				else {
					tableLines.add( line );
				}
			}
		}
	}

	/**
	 * writes line
	 * @param line line
	 * @param propsMap properties map
	 * @return table line
	 */
	String getLine( String line, HashMap< String, String > propsMap ) {
		// replace
		line = line.replaceAll( "\\\\\\[", "[" );
		line = line.replaceAll( "\\\\\\]", "]" );

		// line
		String ret = "";
		int index = line.indexOf( "{{" );

		while( index >= 0 ) {
			ret += line.substring( 0, index );
			line = line.substring( index + 2 );

			index = line.indexOf( "}}" );
			String key = line.substring( 0, index );
			String value = propsMap.get( key );

			if( value.isEmpty() ) {
				return null;
			}

			ret += value;

			line = line.substring( index + 2 );
			index = line.indexOf( "{{" );
		}

		ret += line;

		return ret;
	}

	/**
	 * main method
	 * @param args arguments
	 * @exception Exception exception
	 */
	public static void main( String[] args ) throws Exception {
		// options
		String template = CommandLineTool.getOption( "template", args );
		String out = CommandLineTool.getOption( "out", args );
		String props = CommandLineTool.getOption( "props", args );
		if( template == null || out == null ) {
			showUsage();
			return;
		}

		// create map
		MapCreater creater = new MapCreater();
		creater.create( template, out, props );
	}

	/**
	 * shows the usage
	 */
	static void showUsage() {
		String usage = "java jp.dbcls.rdf.mapping.MapCreater ";
		usage += "-out [output file] ";
		usage += "-template [template file] ";
		usage += "-props [properties file]";

		System.out.println( usage );
	}

}
