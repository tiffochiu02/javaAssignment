package edu.uob;

import java.util.*;
import java.util.ArrayList;


//<Condition>       ::=  "(" <Condition> ")" | <FirstCondition> <BoolOperator> <SecondCondition> | [AttributeName] <Comparator> [Value]

//<FirstCondition>  ::=  <Condition> " " | "(" <Condition> ")"

//<SecondCondition> ::=  " " <Condition> | "(" <Condition> ")"

//two constructors for condition - one for simple, the other for parent
//two stacks, one for comparators and booloperators, the other for condition
public class Condition{
   String attributeName;
   String value;
   String comparator;
   String boolOperator;
   ArrayList<Condition> subConditions;
   String column;

   //simple condition
   public Condition(String attributeName, String value, String comparator){
       this.attributeName = attributeName;
       this.value = value;
       this.comparator = comparator;
       subConditions = new ArrayList<>();
   }
   //compound condition
   public Condition(String boolOperator) {
       this.boolOperator = boolOperator;
       subConditions = new ArrayList<>();
   }
   public void addSubCondition(Condition subCondition){
       subConditions.add(subCondition);
   }

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
               }
               operatorStack.pop(); //if the token is ")" and the top of stack is "(" then pop the "(" to complete the condition
           } else if(NodeCheck.isBoolOperator(token)) {
               operatorStack.push(token);
           } else if (NodeCheck.isComparator(token)) {
               String name = tokens.get(tokens.indexOf(token)-1);
               String value = tokens.get(tokens.indexOf(token)+1);
               if(NodeCheck.isAttributeName(name) && NodeCheck.isValue(value)) {
                   Condition condition = new Condition(name, value, token);
                   conditionStack.push(condition);
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
       }
       return conditionStack.pop();


   }
    public boolean checkCondition(Map<String, String> row){
       if(!subConditions.isEmpty()){
           
       }
    }


}