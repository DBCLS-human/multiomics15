/**
 * class: CommandLineTools
 * brief: The tools class for command line.
 *
 * @author Satoshi Tanaka
 *
 * Copyright (c) 2016 DBCLS, S.Tanaka
 * Released under the MIT license.
 * https://github.com/DBCLS-human/multiomics15/blob/master/LICENSE
 */

package jp.dbcls;

/**
 * string tool class
 * @author Satoshi
 *
 */
public class CommandLineTool {
	/**
	 * gets the command line option value
	 * @param optionName option name
	 * @param args arguments
	 * @return option value
	 */
	public static String getOption( String optionName, String[] args ) {
		int step = 0;
		String value = null;
		for( int i = 0; i < args.length && step <= 1; i++ ) {
			if( step == 0 ) {
				String arg = args[ i ].replace( "-", "" );
				if( arg.equals( optionName ) ) {
					step = 1;
					value = "";
					step = 1;
				}
			}
			else if( step == 1 ) {
				value = args[ i ];
				step = 2;
			}
		}

		return value;
	}
}
