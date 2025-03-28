package edu.uob;

//<ValueList>       ::=  [Value] | [Value] "," <ValueList>
//
//[DigitSequence]   ::=  [Digit] | [Digit] [DigitSequence]
//
//        [IntegerLiteral]  ::=  [DigitSequence] | "-" [DigitSequence] | "+" [DigitSequence]
//
//        [FloatLiteral]    ::=  [DigitSequence] "." [DigitSequence] | "-" [DigitSequence] "." [DigitSequence] | "+" [DigitSequence] "." [DigitSequence]
//
//        [BooleanLiteral]  ::=  "TRUE" | "FALSE"
//
//        [CharLiteral]     ::=  [Space] | [Letter] | [Symbol] | [Digit]
//
//        [StringLiteral]   ::=  "" | [CharLiteral] | [StringLiteral] [CharLiteral]



import java.util.ArrayList;

public class NodeCheck {
    String token;

    public static boolean isdigitSequence(String token) {
        boolean flag = false;
        for(char c : token.toCharArray()) {
            if(Character.isDigit(c)) {
                flag = true;
            }
        }
        return flag;
    }


    public static boolean isComparator(String token) {
        String[] comparators = {"==", ">", "<", ">=", "<=", "!=", "LIKE"};
        for (String comparator : comparators) {
            if (comparator.equals("LIKE")) {
                if (token.equalsIgnoreCase("LIKE")) {
                    return true;
                }
            } else if (token.equals(comparator)) {
                return true;
            }
        }
        return false;
    }

    static String[] operators = {"AND", "OR"};
    public static boolean isBoolOperator(String token) {
        for (String operator : operators) {
            if(token.equalsIgnoreCase(operator)){
                return true;
            }
        }
        return false;
    }

    static char[] symbols = {'!', '#', '$', '%', '&', '(', ')', '*', '+', ',', '-',
            '.', '/', ':', ';', '>', '=', '<', '?', '@', '[', '\\', ']', '^', '_', '`', '{', '}', '~'};

    public static boolean isSymbol(char character){
        for(char symbol: symbols) {
            if(character == symbol){
                return true;
            }
        }
        return false;
    }

    public static boolean isPlainText(String token) {
        for(char c : token.toCharArray()) {
            if(!Character.isLetterOrDigit(c)) {return false;}
        }
        return true;
    }



    public static boolean isIntegerLiteral(String token){
        if (token == null || token.isEmpty()) {
            return false;
        }
        if(isdigitSequence(token)){ return true; }
        if(token.charAt(0) == '-' || token.charAt(0) == '+'){
            String sub = token.substring(1);
            if(!sub.isEmpty() && isdigitSequence(sub)){ return true; }
        }
        return false;
    }

    public static boolean isFloatLiteral(String token){
        if(token.contains(".")){
            String firstHalf = token.substring(0, token.indexOf('.'));
            String secondHalf = token.substring(token.indexOf('.') + 1);
            if(isIntegerLiteral(firstHalf) && isdigitSequence(secondHalf)){ return true;}
        }
        return false;
    }

    public static boolean isBooleanLiteral(String token){
        if(token.equalsIgnoreCase("TRUE") || token.equalsIgnoreCase("FALSE")){ return true; }
        return false;
    }

    public static boolean isCharLiteral(char c){
        if(Character.isLetter(c) || isSymbol(c) || Character.isDigit(c) || Character.isSpaceChar(c)){ return true; }
        return false;
    }

    public static boolean isStringLiteral(String token){
        for(char c : token.toCharArray()){
            if(!isCharLiteral(c)){ return false; }
        }
        return true;
    }
    // [Value]::=  "'" [StringLiteral] "'" | [BooleanLiteral] | [FloatLiteral] | [IntegerLiteral] | "NULL"
    public static boolean isValue(String token){
        if(token.startsWith("'") && token.endsWith("'")){
            String sub = token.substring(1, token.length()-1);
            if(isStringLiteral(sub)){ return true; }
        }
        else if(isBooleanLiteral(token)){ return true; }
        else if(isFloatLiteral(token)){ return true; }
        else if(isIntegerLiteral(token)){ return true; }
        else if (token.equalsIgnoreCase("NULL")) {
            return true;
        }

        return false;
    }
    public static boolean isKeyword(String token){
        String[] commands = {"SELECT","INSERT","INTO","DELETE","UPDATE","CREATE",
                "UPDATE","DROP","ALTER","WHERE","JOIN","USE","FROM","TABLE","DATABASE","SET","ON"};
        for(String command:commands){
            if(token.equalsIgnoreCase(command)) return true;
        }
        return false;
    }

    public static boolean isAttributeName(String token){
        if(isPlainText(token) && !isBoolOperator(token) && !token.equalsIgnoreCase("LIKE")
                && !isBooleanLiteral(token) && !token.equalsIgnoreCase(Table.ID_COL) && !isKeyword(token)){
            return true;
        }
        return false;
    }

    public static boolean isNameValuePair(ArrayList<String> tokens){
        if(!(tokens.size() == 3)){ return false; }
        String first = tokens.get(0);
        String second = tokens.get(1);
        String third = tokens.get(2);
        if(isAttributeName(first) && second.equals("=") && isValue(third)){ return true; }
        return false;
    }


}
