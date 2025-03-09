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
//


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

    static String[] comparators = {"==", ">", "<", ">=", "<=", "!=", "LIKE"};
    public static boolean isComparator(String token) {
        boolean flag = false;
        for(String comparator : comparators) {
            flag = token.equals(comparator);
        }
        return flag;
    }

    static String[] operators = {"AND", "OR"};
    public static boolean isBoolOperator(String token) {
        boolean flag = false;
        for (String operator : operators) {
            flag = token.equals(operator);
        }
        return flag;
    }

    static char[] symbols = {'!', '#', '$', '%', '&', '(', ')', '*', '+', ',', '-',
            '.', '/', ':', ';', '>', '=', '<', '?', '@', '[', '\\', ']', '^', '_', '`', '{', '}', '~'};

    public static boolean isSymbol(char character){
        boolean flag = false;
        for(char symbol: symbols) {
            if(character == symbol){
                flag = true;
            }
        }
        return flag;
    }

    public static boolean isPlainText(String token) {
        boolean flag = false;
        for(char c : token.toCharArray()) {
            if(Character.isLetter(c) || Character.isDigit(c)) {flag = true;}
            if(isPlainText(token) || Character.isLetterOrDigit(c)) {flag = true;}
        }
        return flag;
    }



    public static boolean isIntegerLiteral(String token){
        boolean flag = false;
        if(isdigitSequence(token)){ flag = true; }

        if(token.charAt(0) == '-' || token.charAt(1) == '+'){
            String sub = token.substring(1);
            if(isdigitSequence(sub)){ flag = true; }
        }
        return flag;
    }

    public static boolean isFloatLiteral(String token){
        boolean flag = false;
        if(token.contains(".")){
            String firstHalf = token.substring(0, token.indexOf('.'));
            String secondHalf = token.substring(token.indexOf('.') + 1);
            if(isIntegerLiteral(firstHalf) && isdigitSequence(secondHalf)){ flag = true;}
        }
        return flag;
    }

    public static boolean isBooleanLiteral(String token){
        boolean flag = false;
        if(token.equals("TRUE") || token.equals("FALSE")){ flag = true; }
        return flag;
    }

    public static boolean isCharLiteral(char c){
        boolean flag = false;

        if(Character.isLetter(c) || Character.isSpaceChar(c) || isSymbol(c) || Character.isDigit(c)){ flag = true; }

        return flag;
    }

    public static boolean isStringLiteral(String token){
        boolean flag = false;
        for(char c : token.toCharArray()){
            if(isCharLiteral(c) || Character.isSpaceChar(c)){ flag = true; }
            if(isCharLiteral(c) || isStringLiteral(token)){ flag = true; }
        }
        return flag;
    }
    // [Value]::=  "'" [StringLiteral] "'" | [BooleanLiteral] | [FloatLiteral] | [IntegerLiteral] | "NULL"
    public static boolean isValue(String token){
        boolean flag = false;
        if(token.startsWith("'") && token.endsWith("'")){
            String sub = token.substring(0, token.length()-1);
            if(isStringLiteral(sub)){ flag = true; }
        }
        if(isBooleanLiteral(token)){ flag = true; }
        if(isFloatLiteral(token)){ flag = true; }
        if(isIntegerLiteral(token)){ flag = true; }
        return flag;
    }

    public static boolean isAttributeName(String token){
        boolean flag = false;
        if(isPlainText(token)){ return true; }
        return flag;
    }


//<Condition>       ::=  "(" <Condition> ")" | <FirstCondition> <BoolOperator> <SecondCondition> | [AttributeName] <Comparator> [Value]

//    public static boolean checkCondition(ArrayList<String> tokens){
//        boolean flag = false;
//        if(tokens.size() == 3){
//            String sub1 = tokens.get(0);
//            String sub2 = tokens.get(1);
//            String sub3 = tokens.get(2);
//            if(isAttributeName(sub1) && Comparator.isComparator(sub2) && isValue(sub3)){ flag = true; }
//        }
//        for(String token: tokens){
//            if(BoolOperator.isBoolOperator(token)){
//                int indexOfOperator = tokens.indexOf(token);
//                ArrayList<String> part1 = new ArrayList<>(tokens.subList(0, indexOfOperator));
//                ArrayList<String> part2 = new ArrayList<>(tokens.subList(indexOfOperator + 1, tokens.size()));
//                if(isFirstCondition(part1) && isSecondCondition(part2)){flag = true; }
//            }
//        }
//        return flag;
//    }
//
//    //<FirstCondition>  ::=  <Condition> " " | "(" <Condition> ")"
//    public static boolean isFirstCondition(ArrayList<String> tokens){
//        boolean flag = false;
//        if(tokens.get(0).equals("(") && tokens.get(tokens.size()).equals(")")){
//           ArrayList<String> sub = new ArrayList<>(tokens.subList(1, tokens.size()-1));
//           if(checkCondition(sub)){flag = true; }
//        }
//        if(tokens.get(tokens.size()).equals(" ")){
//            ArrayList<String> sub = new ArrayList<>(tokens.subList(0, tokens.size()-1));
//            if(checkCondition(sub)){flag = true; }
//        }
//        return flag;
//    }
//
//    //<SecondCondition> ::=  " " <Condition> | "(" <Condition> ")"
//    public static boolean isSecondCondition(ArrayList<String> tokens){
//        boolean flag = false;
//        if(tokens.get(0).equals(" ")){
//            ArrayList<String> sub = new ArrayList<>(tokens.subList(1, tokens.size()));
//            if(checkCondition(sub)){flag = true; }
//        }
//        if(tokens.get(0).equals("(") && tokens.get(tokens.size()).equals(")")){
//            ArrayList<String> sub = new ArrayList<>(tokens.subList(1, tokens.size()-1));
//            if(checkCondition(sub)){flag = true; }
//        }
//        return flag;
//    }

//    //if the current node is a "("
//    public static boolean newCondition(String token){
//        boolean flag = false;
//        if(tokens.get(0).equals("(")){
//
//        }
//    }


}
