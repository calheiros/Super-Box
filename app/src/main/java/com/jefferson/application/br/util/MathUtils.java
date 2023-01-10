/*
 * Copyright (C) 2023 Jefferson Calheiros


 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package com.jefferson.application.br.util;

public class MathUtils {
    
    //The following code was taken from a stackoverflow response, answered by Boann. Thank you to him
    //Source: https://stackoverflow.com/questions/3422673/how-to-evaluate-a-math-expression-given-in-string-form
    
    public static double eval(final String str) { 
        Object result = new Object() {
            int pos = -1, ch;
            void nextChar() { 
                ch = (++pos < str.length()) ? str.charAt(pos) : -1; }
            boolean eat(int charToEat) { while (ch == ' ') nextChar(); if (ch == charToEat) {
                    nextChar();
                    return true; 
                } return false; 
            } double parse() {
                nextChar(); 
                double x = parseExpression(); 
                if (pos < str.length()) 
                    throw new RuntimeException("Unexpected: " + 
                                               (char)ch); return x;
            }
            // Grammar: 
            // expression = term | expression `+` term | expression `-` term 
            // term = factor | term `*` factor | term `/` factor 
            // factor = `+` factor | `-` factor | `(` expression `)` | number 
            //     | functionName `(` expression `)` | functionName factor 
            //     | factor `^` factor 

            double parseExpression() { 
                double x = parseTerm(); 
                for (;;) { 
                    if (eat('+')) x += parseTerm(); // addition 
                    else if (eat('-')) x -= parseTerm(); // subtraction
                    else return x; 
                } 
            }
            
            double parseTerm() { 
                double x = parseFactor(); 
                for (;;) {
                    if (eat('*')) x *= parseFactor(); // multiplication 
                    else if (eat('/')) x /= parseFactor(); // division 
                    else return x; } 
            }
            
            double parseFactor() { 
                if (eat('+')) 
                    return +parseFactor(); // unary plus 
                if (eat('-')) return -parseFactor(); // unary minus 
                double x; int startPos = this.pos; 
                if (eat('(')) { // parentheses 
                    x = parseExpression(); 
                    if (!eat(')')) 
                        throw new RuntimeException("Missing ')'"); 
                } else if ((ch >= '0' && ch <= '9') || ch == '.') { // numbers 
                    while ((ch >= '0' && ch <= '9') || ch == '.')
                        nextChar(); x = Double.parseDouble(str.substring(startPos, this.pos)); 
                } else if (ch >= 'a' && ch <= 'z') { // functions
                    while (ch >= 'a' && ch <= 'z') 
                        nextChar(); 
                    String func = str.substring(startPos, this.pos); 
                    if (eat('(')) { 
                        x = parseExpression(); if (!eat(')')) throw new RuntimeException("Missing ')' after argument to " + func); 
                    } else { 
                        x = parseFactor(); 
                    } if (func.equals("sqrt"))
                        x = Math.sqrt(x); 
                    else if (func.equals("sin"))
                        x = Math.sin(Math.toRadians(x)); 
                    else if (func.equals("cos")) 
                        x = Math.cos(Math.toRadians(x)); 
                    else if (func.equals("tan")) 
                        x = Math.tan(Math.toRadians(x)); 
                    else throw new RuntimeException("Unknown function: " + func);
                } else {
                    throw new RuntimeException("Unexpected: " + ch); 
                } if (eat('^'))
                    x = Math.pow(x, parseFactor()); // exponentiation 
                return x;
                }
        }.parse();
        return(double) result;
    } 
}

