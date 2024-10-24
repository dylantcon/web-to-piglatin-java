// WebToPigLatin.java

// Author: Dylan Connolly
// ASG#: 4
// Sect: 0
// Date: 10/21/24 @ 3:47 PM

import java.io.FileReader;  // I
import java.io.FileWriter;  // O
import java.io.IOException; // for if something is very wrong
import java.util.Scanner;   // for reading from input file

public class WebToPigLatin
{

  private String inputFileName;     // name of file we're reading from
  private String outputFileName;    // name of file we're creating and writing to
  private Scanner scanner;          // for reading lines in the file
  private FileWriter outF;          // output file stream
  private FileReader inF;           // input file stream

  public WebToPigLatin( String[] arguments ) throws IOException, IllegalArgumentException
  {
    // check that program was run with adequate num args (2 args)
    if ( arguments.length != 2 )
    {
      // if not, inform the user and abort
      System.err.println( "Usage: java WebToPigLatin inputFile outputFile" );
      throw new IllegalArgumentException( "Must have two command-line parameters\n" );
    }

    // if program hasn't thrown exception, was used correctly
    inputFileName = arguments[0];
    outputFileName = arguments[1];    // java WebToPigLatin (args[0]) inputFile (args[1]) outputFile

    inF = new FileReader( inputFileName );  // init the filereader for file 'inputFileName'
    scanner = new Scanner( inF );           // init the scanner to read from the input file stream we made

    outF = new FileWriter( outputFileName );  // init the filewriter for file 'outputFileName'
  }

  // gets a single line as a String from input file
  private String inputFileLine()
  {
    // only read if there actually is data
    if ( scanner.hasNextLine() )
    {
      String line = scanner.nextLine();
      return line + "\n"; // return string but with a newline, for regex
    }
    return null; // if there isn't a line to read, return null (should never happen)
  }

  // writes a single line, 'l', to the output file
  private void outputFileLine( String l ) throws IOException
  {
    outF.write( l );
  }

  private void close() throws IOException
  {
    // if they're not already closed, close them
    if ( scanner != null )
      scanner.close();
    if ( outF != null )
      outF.close();
    if ( inF != null )
      inF.close();
  }

  // file must be readable (has lines remaining, stream functional)
  public void parseALine() throws IOException
  {
    String line = inputFileLine();                    // get a line from the html file
    String dupeLine = new String( line );             // create a duplicate, which we will tokenize

    // after lineTokenizer call, dupeLine dealloc'd
    String[] tokens = lineTokenizer( dupeLine );      // strip html & get individual unprocessed tokens (using regex)
    String[] pigTokens = tokenProcessor( tokens );    // translate any valid tokens to pig latin, store separately
    
    // all substrings in 'line' corresponding with 'tokens[i]'
    // .replace()'d with 'pigTokens[i]' iff they're not equal
    line = tokenAssembler( pigTokens, tokens, line );
    outputFileLine( line );                           // store 'line' in our output file
  }

  // just reflects whether or not there are lines left to read
  public boolean fileReadable()
  {
    return scanner.hasNextLine();
  }

  // regex was developed using insights in the Deitel text, and readings from Oracle.
  //  link: 'https://docs.oracle.com/javase/6/docs/api/java/util/regex/Pattern.html'
  //   for testing, I used 'regex101.com'. it was a very helpful learning tool!
  private static String[] lineTokenizer( String dLine )
  {
/* * * * * * * * * * * * * * * * * MY REGEX EXPLANATION * * * * * * * * * * * * * * * * * * * 
 *
 * ### (replaceAll) = "<[^>]+>|&[^;]+;" ###
 *
 * - "<[^<]+>": first, looks for an instance of an opening HTML tag character '<' in the
 *              source string. then, if found, [^>]+ will capture one or more character(s)
 *              that don't equal the closing HTML tag character, '>'. once [^>] no longer
 *              holds, we know the next character is a '>'. ultimately, this results in
 *              the selection of everything between '<' and '>', our endgoal.
 *
 * - "&[^;]+;": this portion of the regex uses the same logic as what is described above,
 *              applied on HTML character entities. these are used when the creator of the
 *              HTML file must display a type of character that either is reserved as an
 *              HTML keyword, or can't be explicitly typed on a normal keyboard. examples
 *              are ampersands, single quotes, greater/less than, etc.
 *
 * - "|": this is essentially OR, the set union, which when combined with the two 
 *              expressions above, says: 'within the string, select the substring(s) which
 *              qualify as HTML tags OR that qualify as HTML character entities--result is
 *              removal of any HTML in source string.
 *
 * ### (split) = "(?:[\\W&&[^']]|_|\\d|(?<=\\s)')+" ###
 *
 * - "[\\W&&[^']]": this is the set intersection of the set of all non-word characters 
 *                 (every char except a-z, A-Z, 0-9, and _), and the set of all characters
 *                 except the apostrophe. this will result in selection of all punctuation,
 *                 whitespace, parentheses, brackets, and braces for delimiting, but not
 *                 apostrophes, because we need to handle those in a specific way.
 *
 * - "_": since the expression above doesn't select underscores, we will select them to
 *        delimit manually.
 *
 * - "\\d": the aforementioned compound expression also doesn't handle numerical digits,
 *          delimit manually.
 *
 * - "(?<=\\s)'": although these aren't expressly mentioned in the Deitel textbook, Oracle
 *                categorizes this as a 'Non-Capturing Special Construct'. (?<=...) is 
 *                defined as a 'zero-width positive lookbehind'. 'positive' in this context 
 *                signifies conditional selection of the character(s) preceding OR following
 *                the Special Construct, provided that the character whose presence is being
 *                checked IS indeed at the location the construct checks. For example:
 *                (?<=A)B would capture all instances of B preceded by an instance of A.
 *                Thus, (?<=\\s)' will capture any apostrophes that follow a whitespace.
 *                this removes any apostrophes that would ordinarily be the 0'th element of
 *                a string token, ensuring apostrophes are never at the beginning of a word.
 *
 * - "(?:...)+": this wraps the entire compound regex, and is also a Non-Capturing special
 *               construct. when used in conjunction with the '+' quantifier, states "match
 *               everything enclosed one or more times". this allows the regex as a whole
 *               to avoid excessive splitting of source strings, and ensures that
 *               tokenization is done only in accordance to the rules.
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    String[] tok = new String[] { " " };                     // initialize a blank array of strings (in the case of 0 valid tokens)
    dLine = dLine.replaceAll( "(?:<[^>]+>|&[^;]+;)+", " " );  // replace all HTML syntax in duped string with a space
    
    // now, it is wise to check if there's an apostrophe at the zero'th index.
    //  this is because our split regex will only delimit a leading apostrophe
    //  if it is following a whitespace. if so, prepend a space to handle this
    if ( dLine.charAt( 0 ) == 39 )
    {
      StringBuilder temp = new StringBuilder( dLine );
      temp.insert( 0, ' ' );
      dLine = temp.toString();
    }
    // now, each word token will split using delimiters corresponding to the rules.
    // only attempt a split if there's actually content!
    if ( !dLine.isBlank() )
      tok = dLine.split( "(?:[\\W&&[^']]|_|\\d|(?<=[\\s])')+" );
    
    // now, string has been tokenized. return it
    return tok;
  }

  private static String[] tokenProcessor( String[] incTok )
  {
    // must create a copy of the input array, so that we aren't modifying
    // the reference to tokens[] in parseALine()!
    String[] procTok = new String[ incTok.length ];
    System.arraycopy( incTok, 0, procTok, 0, incTok.length );
  
    // now, iterate through processed tokens and piggify them if need be  
    for ( int thisTok = 0; thisTok < procTok.length; thisTok++ )
    {
      if ( isPiggable( procTok[thisTok] ) )
        procTok[thisTok] = piggify( procTok[thisTok] );
    }
    return procTok;   // this contains the array of processed tokens. any that needed modification are now modified
  }

  private static String tokenAssembler( String[] pigTok, String[] tok, String sourceLine )
  {
    // pigTok is created using tok, so if tok is empty we can return ( no valid tokens in line ).
    // also, because pigTok is derived from tok, they are the same length implicitly.
    
    // we should also check to see if pigTok[] and tok[] differ at all,
    // because if not, we can just return our source line. use a loop
    if ( tok.length == 0 )
      return sourceLine;
      
    boolean madeChanges = false; 
    for ( int i = 0; i < tok.length; i++ )
    {
      if ( tok[i].compareTo( pigTok[i] ) != 0 )
      {
        madeChanges = true;
        break;                  // break early if any changes found
      }
    }

    if ( !madeChanges )         // if no differences found in pair of tokens, return original string
      return sourceLine;

    StringBuilder pigLatinizer = new StringBuilder( sourceLine ); // if no return, there are tokens to modify
    int startPos = 0;                                             // fromIndex in indexOf(String str, int fromIndex)
    int countHTML = 0;                                            // for handling nested html content
    int tempSP = 0;
    char curChar = '\0';

    for ( int i = 0; i < tok.length; i++ )
    {
      if ( tok[i].isBlank() == true )                             // if we have a blank token skip it (im new to regex)
        continue;                                                 //  this prevents logic errors
    
      while ( startPos < pigLatinizer.length() )                  // adjust starting search position (skipping extraneous characters)
      {
        curChar = pigLatinizer.charAt( startPos );                // capture character at startPos
        if ( curChar == '<' || curChar == '&' )                   // check if it's the start of html syntax
        {
          tempSP = startPos;
          countHTML++;                                                // countHTML is incremented for any opening html syntax
          while ( countHTML != 0 && tempSP < pigLatinizer.length() )  // now, we'll loop to:
          {
            if ( ++tempSP + 1 > pigLatinizer.length() )             // case of singular html occurrance in line, skip it and break loop
            {
              countHTML = 0;                                        // there's no closing chars, so set to zero
              tempSP = startPos + 1;                                // set tempSP to be 1 index past problematic char
              break;                                                // break
            }
            curChar = pigLatinizer.charAt( tempSP );              // capture next character inside html syntax
            
            if ( curChar == '<' || curChar == '&' )               // if we detect another opening syntax char, increment (nested tags)
              countHTML++;
            else if ( curChar == '>' || curChar == ';' )          // if we detect a closing syntax char, decrement
              countHTML--;
          }                                                       // stops when we have equal opening and closing chars, or end of string
          startPos = tempSP;                                      // set startPos to hold correct position
        }
        if ( Character.isLetter( curChar ) )                      // check if curChar is a letter (start of a valid token)
          break;                                                  // if so, break so that startPos is at the index of a token
        startPos++;                                               // otherwise, increment startPos and begin another iteration
      }
      // search position is now at a word, must see if there are 
      // any changes that need to be made for this pair of tokens, 
      // by checking to see if pigTok[i] differs from tok[i].
      if ( tok[i].compareTo( pigTok[i] ) == 0 )
      {
        startPos += tok[i].length();    // they're equal, so have startPos point to the character
        continue;                       // immediately after the unprocessed token, then continue
      }
      // at this point, startPos must point to the index of a token that needs
      // to be altered by the contents of pigTok[]. So, .replace() it.
      pigLatinizer.replace( startPos, startPos + tok[i].length(), pigTok[i] );
      startPos += pigTok[i].length();
    }
    // now, pigLatinizer has assembled all tokens into the string!
    return pigLatinizer.toString();
  }

  // 
  private static boolean isPiggable( String tok )
  {
    // word is a word if it has a vowel. check all chars with isVowel
    boolean hasVowel = false;
    for ( int i = 0; i < tok.length(); i++ )
    {
      hasVowel = isVowel( tok.charAt( i ), i );
      if ( hasVowel == true )
        break;                // if vowel detected, we can exit loop early
    }
    return hasVowel;  // return our boolean result from isVowel
  }

  private static String piggify( String tok )
  {
    StringBuilder pigBuilder = new StringBuilder( tok );  // make a stringBuilder, we'll be changing 'tok'
  
    // append '-way' vowel case
    if ( isVowel( tok.charAt( 0 ), 0 ) )
      pigBuilder.append( "way" );

    else
    {
      // implicitly, one consonant
      int consonantNum = 1;
      boolean capSwap = Character.isUpperCase( tok.charAt( consonantNum - 1 ) );

      // iterate through pigBuilder until we have reached a vowel letter
      while ( isConsonant( tok.charAt( consonantNum ), consonantNum ) )
        consonantNum++;

      // then, the substring to be appended is [0, consonantNum), because of non-inclusive range
      String cSS = tok.substring( 0, consonantNum );

      // check if we need to handle relocation of cap case
      if ( capSwap )
      { 
        // concatenation of leading lowercase equivalent and the remainder of the consonant substring.
        // pigBuilder.charAt( consonantNum, [uppercase] ) is future beginning of word, so capitalize it
        cSS = Character.toLowerCase( cSS.charAt( 0 ) ) + cSS.substring( 1 );
        pigBuilder.setCharAt( consonantNum, Character.toUpperCase( tok.charAt( consonantNum ) ) );
      } 

      // now, append our substring + ay, and remove old leading instance
      pigBuilder.append( cSS ).append( "ay" );
      pigBuilder.delete( 0, consonantNum );
      // we're done. exit else, and return
      }
    return pigBuilder.toString(); 
  }

  private static boolean isVowel( char ch, int ind )
  {
    ch = Character.toLowerCase( ch );   // primitives PBV, make it lowercase
    switch ( ch )
    {
      case 'a': // |
      case 'e': // |
      case 'i': // |
      case 'o': // V
      case 'u': // all unconditional vowels cascade to true
        return true;
      case 'y':                          // if y isn't at the start of a word (str[i], i > 0)
        if ( ind > 0 )                   //  it's a vowel in our case, so we return true
          return true;                   //   otherwise, IS at the start, so consonant
      default:
        return false;                    // all consonants (and y for i = 0) will be default case
    }
  }

  // if isVowel is false, isConsonant must be true
  private static boolean isConsonant( char ch, int ind )
  {
    return !( isVowel( ch, ind ) );
  }

  public static void main( String[] args )
  {
    // since an IOException could be thrown, try
    try
    {
      // init a converter with supplied filenames.
      // while lines remain, parse them
      WebToPigLatin converter = new WebToPigLatin( args );
      while ( converter.fileReadable() )
        converter.parseALine();
      // once finished, close all streams
      converter.close();
    }
    // if there is an IOException, display stack trace
    catch ( IOException e )
    {
      e.printStackTrace();
    }
  }
} // end class WebToPigLatin
