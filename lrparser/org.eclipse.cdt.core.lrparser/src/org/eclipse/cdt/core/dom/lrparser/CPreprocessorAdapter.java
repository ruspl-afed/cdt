/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.lrparser;

import lpg.lpgjavaruntime.IToken;
import lpg.lpgjavaruntime.Token;

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.parser.EndOfFileException;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.OffsetLimitReachedException;

/**
 * Adapts the CPreprocessor from the CDT core for use with LPG based parsers.
 * 
 * @author Mike Kucera
 *
 */
class CPreprocessorAdapter {
	
	/**
	 * During content assist the preprocessor may return a completion token
	 * which represents the identifier on which the user invoked content assist.
	 * Then the preprocessor normally returns arbitrarily many end-of-completion 
	 * (EOC) tokens.
	 * 
	 * A bottom-up parser cannot know ahead of time how many EOC tokens are
	 * needed in order for the parse to complete successfully. So we pick
	 * a number that seems arbitrarily large enough.
	 */
	private static final int NUM_EOC_TOKENS = 50;
	
	private static final int DUMMY_TOKEN_KIND = 0; 
	private static final int tCOMPLETION   = org.eclipse.cdt.core.parser.IToken.tCOMPLETION;
	
	
	/** 
	 * Collect the tokens generated by the preprocessor.
	 * TODO: should preprocessor.nextTokenRaw() be called instead?
	 */
	@SuppressWarnings("restriction")
	public static void runCPreprocessor(IScanner preprocessor, ITokenCollector tokenCollector, IDOMTokenMap tokenMap, IASTTranslationUnit tu) {
		// LPG requires that the token stream start with a dummy token
		tokenCollector.addToken(createDummyToken());
		
		preprocessor.getLocationResolver().setRootNode(tu);
		
		try {
			while(true) {
				// the preprocessor throws EndOfFileException when it reaches the end of input
				org.eclipse.cdt.core.parser.IToken domToken = preprocessor.nextToken();
				processDOMToken(domToken, tokenCollector, tokenMap);
				
				if(domToken.getType() == tCOMPLETION)
					break;
			}
		} catch(OffsetLimitReachedException e) { 
			// preprocessor throws this when content assist is invoked inside a preprocessor directive
			org.eclipse.cdt.core.parser.IToken domToken = e.getFinalToken();
			assert domToken.getType() == tCOMPLETION;
			processDOMToken(domToken, tokenCollector, tokenMap);
		} catch (EndOfFileException e) { 
			// use thrown exception to break out of loop
		} 
		
		// LPG requires that the token stream end with an EOF token
		tokenCollector.addToken(createEOFToken(tokenMap));
	}
	
	
	private static void processDOMToken(org.eclipse.cdt.core.parser.IToken domToken, ITokenCollector tokenCollector, IDOMTokenMap tokenMap) {
		int newKind = tokenMap.mapKind(domToken);			
		tokenCollector.addToken(new LPGTokenAdapter(domToken, newKind));
		
		if(domToken.getType() == tCOMPLETION) {
			for(int i = 0; i < NUM_EOC_TOKENS; i++)
				tokenCollector.addToken(createEOCToken(tokenMap));
		}
	}
	
	
	private static IToken createEOCToken(IDOMTokenMap tokenMap) {
		return new Token(null, 0, 0, tokenMap.getEOCTokenKind());
	}
	
	private static IToken createDummyToken() {
		return new Token(null, 0, 0, DUMMY_TOKEN_KIND);
	}
	
	private static IToken createEOFToken(IDOMTokenMap tokenMap) {
		return new Token(null, 0, 0, tokenMap.getEOFTokenKind());
	}
	
}
