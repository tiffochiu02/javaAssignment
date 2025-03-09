package edu.uob;

import java.util.ArrayList;
import java.util.Stack;


// Cond BoolOperator Cond BoolOperator Cond BoolOperator Cond
//<FirstCondition> <BoolOperator> <SecondCondition>
// Cond BoolOperator (Cond BoolOperator Cond) BoolOperator Cond
// Cond BoolOperator Cond BoolOperator Cond

//<Condition>       ::=  "(" <Condition> ")" | <FirstCondition> <BoolOperator> <SecondCondition> | [AttributeName] <Comparator> [Value]

//<FirstCondition>  ::=  <Condition> " " | "(" <Condition> ")"

//<SecondCondition> ::=  " " <Condition> | "(" <Condition> ")"

//two constructors for condition - one for simple, the other for parent
//two stacks, one for comparators and booloperators, the other for condition
public class ConditionRec {
//    base
   String attributeName;
   String value;
   String comparator;
//   recursive 1
    ConditionRec cond1;
    ConditionRec cond2;
   String boolOperator;

   //simple condition
   public ConditionRec(String attributeName, String value, String comparator){
       this.attributeName = attributeName;
       this.value = value;
       this.comparator = comparator;
   }

   //compound condition
   public ConditionRec(String boolOperator, ConditionRec cond1, ConditionRec cond2) {
       this.cond1 = cond1;
       this.cond2 = cond2;
       this.boolOperator = boolOperator;
   }

   // SELECT * FROM XXX WHERE <.....>
//    condition.check
   public static ConditionRec parseCondition(ArrayList<String> tokens) {
       ArrayList<String> tokens_ = new ArrayList<>(tokens);
       if (tokens.get(0).equals("(") && tokens.get(tokens.size() - 1).equals(")")) {
           Stack<String> parStack = new Stack<>();
           parStack.push("(");
           boolean isMatched = true;
           for (String token : tokens.subList(1, tokens.size() - 1)) {
                if (token.equals("(")) {
                    parStack.push("(");
                } else if (token.equals(")")) {
                    parStack.pop();
                    if (parStack.isEmpty()) {
                        isMatched = false;
                        break;
                    }
               }
           }
           if (isMatched) {
                tokens_ = (ArrayList<String>) tokens_.subList(1, tokens_.size() - 1);
           }
       }
       Boolean isSimple = true;
       for (String token: tokens_) {
           if (token.equals("AND") || token.equals("OR")) {
               isSimple = false;
               break;
           }
       }
       if (isSimple) {
           return new ConditionRec(tokens_.get(0), tokens_.get(1), tokens_.get(2));
       }
       // helper
       ArrayList<String> subOperators = new ArrayList<>();
       ArrayList<ConditionRec> subConditions = new ArrayList<>();
       // Cond_1 BoolOperator Cond_2 BoolOperator Cond_3
       int i = 0;
       while (i < tokens_.size()) {
           if (tokens_.get(i).equals("(")) {
               Stack<String> parStack = new Stack<>();
               parStack.push("(");
               int j = i + 1;
               while (j < tokens_.size()) {
                   if (tokens_.get(j).equals("(")) {
                       parStack.push("(");
                   } else if (tokens_.get(j).equals(")")) {
                       if (parStack.size() == 1) {
                           j = j + 1;
                           break;
                       } else {
                           parStack.pop();
                       }
                   }
               }
               subConditions.add(parseCondition((ArrayList<String>) tokens_.subList(i+1, j-1)));
               i = j;
           } else if (tokens_.get(i).equals("AND") || tokens_.get(i).equals("OR")) {
                subOperators.add(tokens_.get(i));
                i++;
           } else {
               subConditions.add(new ConditionRec(tokens_.get(i), tokens_.get(i+1), tokens_.get(i+2)));
               i += 3;
           }
       }
       // (      Cond1      ) AND ( Cond2 )
   }

   public boolean checkRow(Row row) {
       if (this.attributeName != null) {
           // comparator
           return row.getValue(this.attributeName) == this.value;
       } else {
           // boolOperator
           return this.cond1.checkRow(row) && this.cond2.checkRow(row);
       }
   }

//   public void addSubCondition(ConditionRec subCondition){
//       subConditions.add(subCondition);
//   }
//
//   public static ConditionRec parseCondition(ArrayList<String> tokens){
//       Stack<String> operatorStack = new Stack<>();
//       Stack<ConditionRec> conditionStack = new Stack<>();
//       for(String token : tokens) {
//           if(token.equals("(")) {
//               operatorStack.push(token);
//           } else if(token.equals(")")){
//               while(!operatorStack.isEmpty() && !operatorStack.peek().equals("(")){
//                   ConditionRec right = conditionStack.pop();
//                   ConditionRec left = conditionStack.pop();
//                   ConditionRec parent = new ConditionRec(operatorStack.pop());
//               }
//               operatorStack.pop(); //if the token is ")" and the top of stack is "(" then pop the "(" to complete the condition
//           } else if(NodeCheck.isBoolOperator(token)) {
//               operatorStack.push(token);
//           } else if (NodeCheck.isComparator(token)) {
//               String name = tokens.get(tokens.indexOf(token)-1);
//               String value = tokens.get(tokens.indexOf(token)+1);
//               if(NodeCheck.isAttributeName(name) && NodeCheck.isValue(value)) {
//                   ConditionRec condition = new ConditionRec(name, value, token);
//                   conditionStack.push(condition);
//               }
//           }
//       }
//
//       //the case: age < 10 AND PASS == TRUE; if there are no brackets
//       //if the operator not empty, then it means there is only boolean operator left
//       // so we need to finally combine the two conditions into a parent condition
//       while(!operatorStack.isEmpty()){
//           ConditionRec right = conditionStack.pop();
//           ConditionRec left = conditionStack.pop();
//           ConditionRec parent = new ConditionRec(operatorStack.pop());
//       }
//       return conditionStack.pop();
//
//
//   }
//    public boolean checkCondition(Map<String, String> row){
//       if(!subConditions.isEmpty()){
//
//       }
//    }


}