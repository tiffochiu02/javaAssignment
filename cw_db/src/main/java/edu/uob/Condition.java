package edu.uob;

import java.io.IOException;
import java.util.*;
import java.util.ArrayList;


//<Condition>       ::=  "(" <Condition> ")" | <FirstCondition> <BoolOperator> <SecondCondition> | [AttributeName] <Comparator> [Value]

//<FirstCondition>  ::=  <Condition> " " | "(" <Condition> ")"

//<SecondCondition> ::=  " " <Condition> | "(" <Condition> ")"

//two constructors for condition - one for simple, the other for parent
//two stacks, one for comparators and bool operators, the other for condition
public class Condition{
    String attributeName;
    String value;
    String comparator;
    String boolOperator;
    ArrayList<Condition> subConditions;


    //simple condition
    public Condition(String attributeName, String value, String comparator){
        this.attributeName = attributeName;
        this.value = value;
        this.comparator = comparator;
        this.subConditions = new ArrayList<>();
    }
    //compound condition
    public Condition(String boolOperator) {
        this.boolOperator = boolOperator;
        this.subConditions = new ArrayList<>();
    }
    public void addSubCondition(Condition subCondition){
        subConditions.add(subCondition);
    }

    // age > 20 AND ((PASS == TRUE AND EMAIL LIKE "XXX") OR rank > 15) OR class == 2
    public static Condition parseCondition(ArrayList<String> tokens) throws IOException {
        Stack<String> operatorStack = new Stack<>();
        Stack<Condition> conditionStack = new Stack<>();
        if(!isValidCondition(tokens)) throw new IOException("Invalid condition");
//        int numOfLeftParenthesis = 0;
//        int numOfRightParenthesis = 0;
//        for(String token:tokens){
//            if(token.equals("(")){
//                numOfLeftParenthesis++;
//            }
//            else if(token.equals(")")){
//                numOfRightParenthesis++;
//            }
//        }
//        if(numOfLeftParenthesis != numOfRightParenthesis){
//            throw new IOException(numOfLeftParenthesis+" "+numOfRightParenthesis);
//        }

        for (int i = 0; i < tokens.size(); i++) {
            String token = tokens.get(i);

            if (token.equals("(")) {
                operatorStack.push(token);
            } else if (token.equals(")")) {
                while (!operatorStack.isEmpty() && !operatorStack.peek().equals("(")) {
                    System.out.println("Popping operator inside parentheses: " + operatorStack.peek());
                    combineConditions(operatorStack, conditionStack);
                }
                operatorStack.pop();  // Pop the "("
            } else if (NodeCheck.isBoolOperator(token)) {
                while (!operatorStack.isEmpty() && hasHigherPrecedence(operatorStack.peek(), token)) {
                    System.out.println("Popping operator due to precedence: " + operatorStack.peek());
                    combineConditions(operatorStack, conditionStack);
                }
                operatorStack.push(token);  // Push the current Boolean operator
            } else if (NodeCheck.isComparator(token)) {
                if (i == 0 || i == tokens.size() - 1) {
                    throw new IOException();
                }

                String name = tokens.get(i - 1).toLowerCase();
                String value = tokens.get(i + 1);

                if (NodeCheck.isValue(value)) {
                    //if (NodeCheck.isAttributeName(name) && NodeCheck.isValue(value)) {
                    if (value.startsWith("'") && value.endsWith("'")) {
                        value = value.substring(1, value.length() - 1);
                    }
                    Condition simpleCondition = new Condition(name, value, token);
                    System.out.println("Created condition: " + name + " " + token + " " + value); // 检查条件
                    conditionStack.push(simpleCondition);
                }
            }
        }

        while (!operatorStack.isEmpty()) {
            combineConditions(operatorStack, conditionStack);
        }

        if (conditionStack.size() != 1) {
            throw new IOException("Error parsing condition: unmatched conditions");
        }

        return conditionStack.pop();
    }


    private boolean isNumeric(String str) {
        if (str == null) return false;
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    //SELECT * FROM numbers WHERE ((num < 15) or (flag == TRUE)) and (id == 4);
    public boolean checkCondition(Row row) throws NoSuchElementException {
        if (!subConditions.isEmpty()) {
            if (boolOperator.equalsIgnoreCase("AND")) {
                for (Condition subCondition : subConditions) {
                    if (!subCondition.checkCondition(row)) {
                        return false;
                    }
                }
                return true;
            } else if (boolOperator.equalsIgnoreCase("OR")) {
                for (Condition subCondition : subConditions) {
                    if (subCondition.checkCondition(row)) {
                        return true;
                    }
                }
            }
        } else {
            if (attributeName.equalsIgnoreCase(Table.ID_COL)) {
                String idNum = String.valueOf(row.getPrimaryKey());
                //if (idNum == null || value == null) {
                if (value == null) {
                    return false;
                }
                try {
                    double idNumValue = Double.parseDouble(idNum);
                    double valueNum = Double.parseDouble(value);
                    switch (comparator) {
                        case "==":
                            return idNumValue == valueNum;
                        case "!=":
                            return idNumValue != valueNum;
                        case ">":
                            return idNumValue > valueNum;
                        case "<":
                            return idNumValue < valueNum;
                        case ">=":
                            return idNumValue >= valueNum;
                        case "<=":
                            return idNumValue <= valueNum;
                        case "LIKE":
                            return idNum.contains(value);
                        default:
                            return false;
                    }
                } catch (NumberFormatException e) {
                    throw new NoSuchElementException("Invalid number format: " + idNum + " or " + value);
                }
            } else {
                String rowValue = row.getValue(attributeName);
                if (rowValue == null) {
                    throw new NoSuchElementException();
                }
                if (value.startsWith("'") && value.endsWith("'")) {
                    value = value.substring(1, value.length() - 1);
                } else {
                    value = value.toLowerCase();
                    rowValue = rowValue.toLowerCase();
                }

                System.out.println("retrieved row Value: " + rowValue + "; retrieved attribute: " + attributeName +
                        "; retrieved comparison: " + comparator + "; compared value: " + value);

                try {
                    if (isNumeric(rowValue) && isNumeric(value)) {
                        double rowValueNum = Double.parseDouble(rowValue);
                        double valueNum = Double.parseDouble(value);
                        switch (comparator) {
                            case "==":
                                return rowValueNum == valueNum;
                            case "!=":
                                return rowValueNum != valueNum;
                            case ">":
                                return rowValueNum > valueNum;
                            case "<":
                                return rowValueNum < valueNum;
                            case ">=":
                                return rowValueNum >= valueNum;
                            case "<=":
                                return rowValueNum <= valueNum;
                            default:
                                return false;
                        }
                    } else {
                        switch (comparator) {
                            case "==":
                                return rowValue.equals(value);
                            case "!=":
                                return !rowValue.equals(value);
                            case "LIKE":
                                return rowValue.contains(value);
                            default:
                                return false;
                        }
                    }
                } catch (NumberFormatException e) {
                    throw new NoSuchElementException("Invalid number format in comparison: " + rowValue + " and " + value);
                }
            }
        }
        return false;
    }



    private static void combineConditions(Stack<String> operatorStack, Stack<Condition> conditionStack) {
        if (conditionStack.size() < 2) {
            if (operatorStack.peek().equals("(")) {
                System.out.println(" [DEBUG] Ignoring '(' in operator stack.");
                return;
            }
            System.out.println("Insufficient conditions to combine. Stack size:" + conditionStack.size());
            return;
        }
        Condition right = conditionStack.pop();
        Condition left = conditionStack.pop();
        Condition parent = new Condition(operatorStack.pop());
        parent.addSubCondition(left);
        parent.addSubCondition(right);
        conditionStack.push(parent);

        System.out.println("Combine conditions with operator: " + parent.boolOperator);
    }

    private static boolean hasHigherPrecedence(String op1, String op2) {
        Map<String, Integer> precedence = Map.of(
                "AND", 2,
                "OR", 1
        );
        return precedence.getOrDefault(op1, 0) >= precedence.getOrDefault(op2, 0);
    }


//    private static boolean isValidCondition(ArrayList<String> tokens) {
//        int numOfLeftParenthesis = 0;
//        int numOfRightParenthesis = 0;
//        for(String token:tokens){
//            if(token.equals("(")){
//                numOfLeftParenthesis++;
//            }
//            else if(token.equals(")")){
//                numOfRightParenthesis++;
//            }
//        }
//        if(numOfLeftParenthesis != numOfRightParenthesis){
//            return false;
//        }
////        for(String token:tokens) {
////            if (!NodeCheck.isPlainText(token) && !token.equals("(") && !token.equals(")")
////                    && !NodeCheck.isValue(token) && !NodeCheck.isComparator(token)) {
////                return false;
////            }
////        }
//        return true;
//    }

    public static boolean isValidCondition(ArrayList<String> tokens) {
        int numOfLeftParenthesis = 0;
        int numOfRightParenthesis = 0;
        for(String token:tokens){
            if(token.equals("(")){
                numOfLeftParenthesis++;
            }
            else if(token.equals(")")){
                numOfRightParenthesis++;
            }
        }
        if(numOfLeftParenthesis != numOfRightParenthesis){
            return false;
        }
        for (int i = 0; i < tokens.size(); i++) {
            String token = tokens.get(i);
            if (NodeCheck.isComparator(token)) {
                if (i + 1 >= tokens.size()) return false;

                String literalString = tokens.get(i+1);

                if (NodeCheck.isIntegerLiteral(literalString) || NodeCheck.isFloatLiteral(literalString) ||
                        NodeCheck.isBooleanLiteral(literalString) || literalString.equalsIgnoreCase("NULL")) {
                    continue;
                }
                if (literalString.length() < 2 ||
                        literalString.charAt(0) != '\'' ||
                        literalString.charAt(literalString.length() - 1) != '\'') {
                    return false;
                }
            }
        }
        return true;
    }
}



