**This is a programming exercise from my Advanced Java Programming class.**

It will take in any simple pure HTML file, and translate any words in
the text body into Pig Latin. All HTML syntax is ignored (hopefully).
I use string tokenization with regular expressions to break each of the 
respective lines from the input file into words, and then analyze any of
the tokens extracted via str.Split( "[regex]" ) to see if they are valid
processable tokens. I challenged myself to learn how to use regular expr.
because I found them conceptually challenging; this program could have
also been implemented iteratively with Scanner or similar ilk. I am proud
of how it turned out, though, as it works very well for its intended input.

If tokens are valid, said tokens are translated to Pig Latin. Any string in the array
of string tokens that changes after processing is then marked for insertion into
a duplicate of the respective input file line, in the form of a StringBuilder obj.

If a string has not been changed during the token processing routine, its index is
skipped and the length of said token is added to increment an indexing variable that
spans the length of the duplicated input line StringBuilder. This allows incremental
jumps over the indices containing strings that are invalid for processing, which guides
the control of the sB.replace( "Occessedpray" start_ind, end_ind ) calls during assembly
of the processed tokens. Then, after the assembly of all processed tokens into the string 
duplicate, the line is returned from a method, and stored in the specified output file.

Program must be run via:

$ java -jar hw4.jar _inputfile_ _outputfile_
OR
$ java WebToPigLatin _inputfile_ _outputfile_
 
**Words to be translated must consist of**:
1) A string of 1 or more consecutive letters and apostrophes that contain at least 1 vowel
2) A word mustn't start with an apostrophe, but may contain or terminate with one or more
3) Y is a vowel if it is trailing the starting letter. However, if it IS the starting letter,
   it is considered to be a consonant.
