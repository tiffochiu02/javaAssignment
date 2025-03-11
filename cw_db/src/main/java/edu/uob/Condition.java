package edu.uob;

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
   public static Condition parseCondition(ArrayList<String> tokens){
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
               String name = tokens.get(tokens.indexOf(token) - 1).toLowerCase();
               String value = tokens.get(tokens.indexOf(token) + 1);
               if(NodeCheck.isAttributeName(name) && NodeCheck.isValue(value)) {
                   if(value.startsWith("\"") && value.endsWith("\"")) {
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
       return conditionStack.pop();
   }

   public boolean checkCondition(Row row){
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
           String rowValue = row.getValue(attributeName);
           if(value.startsWith("'") && value.endsWith("'")) {
               value = value.substring(1, value.length() - 1);
           }
           System.out.println("retrieved row Value: " + rowValue + "; retrieved attribute: " + attributeName +
                   "; retrieved comparison: " + comparator + "; compared value: " + value);
           switch(comparator){
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
               default: return false;
           }
       }
       return false;
   }







//   public static Condition parseCondition(ArrayList<String> tokens){
//       //if the condition is like: "(" condition ")"
//       Stack<Condition> conditionStack = new Stack<>();
//       Stack<String> operatorStack = new Stack<>();
//       ArrayList<String> newTokens = new ArrayList<String>(tokens);
//       if(tokens.get(0).equals("(") && tokens.get(tokens.size()-1).equals(")")){
//           Stack<String> parStack = new Stack<>();
//           parStack.push("(");
//           List<String> subString = tokens.subList(0,tokens.size()-1);
//           for(String subToken : subString){
//               if(subToken.equals("(")){
//                   parStack.push("(");
//               }
//               else if(subToken.equals(")")){
//                   parStack.pop();
//               }
//           }
//           if(parStack.empty()) {
//               newTokens = (ArrayList<String>) tokens.subList(0,tokens.size()-1);
//           }
//       }
//       //simple condition: [AttributeName] <Comparator> [Value]
//       boolean isSimple = true;
//       for(String token : newTokens){
//           if(NodeCheck.isComparator(token)){
//               isSimple = false;
//               break;
//           }
//       }
//       if(isSimple){
//           Condition sub = new Condition(newTokens.get(0), newTokens.get(1), newTokens.get(2));
//           conditionStack.push(sub);
//           //return new Condition(newTokens.get(0), newTokens.get(1), newTokens.get(2));
//       }
//
//       //process compound conditions:
//       // 1. if there are inner brackets, find the innermost condition
//       // and turn it into a substring of subcondition and recursively parse the subcondition
//       // For example:
//       // age > 20 AND (PASS == TRUE AND (EMAIL LIKE "XXX" OR rank > 15) OR class == 2)
//       // becomes
//       // age > 20 AND (PASS == TRUE AND subCond) OR class == 2
//       // becomes
//       // age > 20 AND cond OR class == 2
//       int start = 0;
//       while(start < newTokens.size()){
//           if(newTokens.get(start).equals("(")){
//               Stack<String> parStack = new Stack<>();
//               parStack.push("(");
//               int temp = start + 1;
//               while(temp < newTokens.size()){
//                   if(newTokens.get(temp).equals("(")){
//                       parStack.push("(");
//                   } else if(newTokens.get(temp).equals(")")){
//                       if(parStack.size() == 1){ //find the biggest bracket and recursively parse it again
//                           temp = temp + 1;
//                           break;
//                       } else {
//                           parStack.pop();
//                       }
//                       Condition sub = new Condition (parseCondition((ArrayList<String>) newTokens.subList(start + 1, temp - 1)).toString());
//                       conditionStack.push(sub);
//                       //return new Condition (parseCondition((ArrayList<String>) newTokens.subList(start + 1, temp - 1)).toString());
//                   }
//               }
//           } else if(NodeCheck.isBoolOperator(newTokens.get(start))){
//               operatorStack.push(newTokens.get(start));
//               start++;
//           } else{
//               conditionStack.push(new Condition(newTokens.get(start), newTokens.get(start + 1), newTokens.get(start + 2)));
//               start += 3;
//           }
//       }
//       while(!operatorStack.isEmpty()){
//           Condition right = conditionStack.pop();
//           Condition left = conditionStack.pop();
//           Condition parent = new Condition(operatorStack.pop());
//           parent.addSubCondition(right);
//           parent.addSubCondition(left);
//           conditionStack.push(parent);
//       }
//       return conditionStack.pop();
//   }
//

}
