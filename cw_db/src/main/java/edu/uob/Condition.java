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
   public static Condition parseCondition(ArrayList<String> tokens) throws IOException{
       Stack<String> operatorStack = new Stack<>();
       Stack<Condition> conditionStack = new Stack<>();
       for(String token : tokens) {
           if(token.equals("(")) {
               operatorStack.push(token);
           } else if(token.equals(")")){
               while(!operatorStack.isEmpty() && !operatorStack.peek().equals("(")){
                   Condition right = conditionStack.pop();
                   Condition left = conditionStack.pop();
                   Condition parent = new Condition(operatorStack.pop());
                   parent.addSubCondition(left);
                   parent.addSubCondition(right);
                   conditionStack.push(parent);
               }
               operatorStack.pop(); //if the token is ")" and the top of stack is "(" then pop the "(" to complete the condition
           } else if(NodeCheck.isBoolOperator(token)) {
               operatorStack.push(token);
           } else if (NodeCheck.isComparator(token)) {
               int index = tokens.indexOf(token);
               if (index == 0 || index == tokens.size() - 1) {
                   throw new IOException();
               }
               String name = tokens.get(index - 1).toLowerCase();
               String value = tokens.get(index + 1);
               if(NodeCheck.isAttributeName(name) && NodeCheck.isValue(value)) {
                   if(value.startsWith("'") && value.endsWith("'")) {
                       value = value.substring(1, value.length() - 1);
                   }
                   Condition simpleCondition = new Condition(name, value, token);
                   conditionStack.push(simpleCondition);
               }
           }
       }

        //the case: age < 10 AND PASS == TRUE; if there are no brackets
        //if the operator not empty, then it means there is only boolean operator left
        // so we need to finally combine the two conditions into a parent condition
       while(!operatorStack.isEmpty()){
           Condition right = conditionStack.pop();
           Condition left = conditionStack.pop();
           Condition parent = new Condition(operatorStack.pop());
           parent.addSubCondition(left);
           parent.addSubCondition(right);
           conditionStack.push(parent);
       }
       if (conditionStack.size() != 1) {
           throw new IOException();
       }
       return conditionStack.pop();
   }

   public boolean checkCondition(Row row) throws NoSuchElementException{
       System.out.println("Checking condition in Condition class");
       System.out.println(attributeName + comparator + value);
       if(!subConditions.isEmpty()){
           if(boolOperator.equals("AND")){
               boolean isMatch = true;
               for(Condition subCondition : subConditions){
                   if (!subCondition.checkCondition(row)) {
                       isMatch = false;
                   }
               }
               return isMatch;
           } else if(boolOperator.equals("OR")){
               for(Condition subCondition : subConditions){
                   if(subCondition.checkCondition(row)){
                       return true;
                   }
               }
           }

       } else {
           if(attributeName.equals(Table.ID_COL)){
               String idNum = String.valueOf(row.getPrimaryKey());
               switch(comparator){
                   case "==":
                       return idNum.equals(value);
                   case "!=":
                       return !idNum.equals(value);
                   case ">":
                       return idNum.compareTo(value) > 0;
                   case "<":
                       return idNum.compareTo(value) < 0;
                   case ">=":
                       return idNum.compareTo(value) >= 0;
                   case "<=":
                       return idNum.compareTo(value) <= 0;
                   case "LIKE":
                       return idNum.contains(value);
                   default: return false;
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
               switch (comparator) {
                   case "==":
                       return rowValue.equals(value);
                   case "!=":
                       return !rowValue.equals(value);
                   case ">":
                       return rowValue.compareTo(value) > 0;
                   case "<":
                       return rowValue.compareTo(value) < 0;
                   case ">=":
                       return rowValue.compareTo(value) >= 0;
                   case "<=":
                       return rowValue.compareTo(value) <= 0;
                   case "LIKE":
                       return rowValue.contains(value);
                   default:
                       return false;
               }
           }
       }
       return false;
   }
}
