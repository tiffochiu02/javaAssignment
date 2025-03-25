package edu.uob;

import java.util.LinkedList;

public class Tokenizer {
    //static String query = "  please   chop  the tree  using   the axe";
    //String[] specialCharacters = {"(",")",",",";"};
    //LinkedList<String> tokens = new LinkedList<>();


    public static LinkedList<String> setup(String query)
    {
        // Split the query on single quotes (to separate out query text from string literals)
       LinkedList<String> tokens = new LinkedList<>();
       query = query.trim();
       tokens = tokenise(query);
        // Finally, loop through the result array list, printing out each token a line at a time
        for(int i=0; i<tokens.size(); i++) System.out.println(tokens.get(i));
        return tokens;
    }

    public static LinkedList<String> tokenise(String input)
    {
        // Add in some extra padding spaces either side of the "special characters"...
        // so we can be SURE that they are separated by AT LEAST one space (possibly more)

        // Remove any double spaces (the previous padding activity might have introduced some of these)
        while (input.contains("  ")) input = input.replace("  ", " "); // Replace two spaces by one
        // Remove any whitespace from the beginning and the end that might have been introduced
        input = input.trim();
        int start = 0;
        LinkedList<String> tokens = new LinkedList<>();
        for(int i=start; i<input.length(); i++) {
            if (input.charAt(i) == ' ') {
                String token = input.substring(start, i);
                tokens.add(token);
                start = i + 1;
            }
        }
        String lastToken = input.substring(start, input.length());
        tokens.add(lastToken);
        // Finally split on the space char (since there will now ALWAYS be a SINGLE space between tokens)
        return tokens;
    }
}
