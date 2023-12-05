import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;
import static java.lang.System.out;

// Una expresión puede ser una suma, una resta, multiplicación, división, un número o una combinación de todas.
// Sirve para calcular el valor de una operación.
public abstract class Expression {
	// Muestra información adicional en la consola con propositos de depuración. No afecta a la funcionaliad de la calculadora.
	private static final boolean LOGS = false;
	
	private static final HashMap<String, Integer> OPERATORS_PRIORITY = new HashMap<String, Integer>();
	
	static {
		OPERATORS_PRIORITY.put("+", 0);
		OPERATORS_PRIORITY.put("-", 0);
		OPERATORS_PRIORITY.put("*", 1);
		OPERATORS_PRIORITY.put("/", 1);
	}
	
	public static boolean isOperator(char character) {
		return isOperator(Character.toString(character));
	}
	
	public static boolean isOperator(String string) {
		return OPERATORS_PRIORITY.containsKey(string);
	}
	
	// Devuelve una Expression basada en una cadena de texto.
	// Ej:
	//     "4 * (2 + 1)" -> Multiplication(4, Addition(2, 1))
	public static Expression getExpression(String operation) {
		operation = operation.replaceAll("\\s+", "");
		ArrayList<String> tokensList = new ArrayList<String>();
		
		for (int i = 0; i < operation.length(); i++) {
			char c = operation.charAt(i);
			String lastToken = tokensList.size() > 0 ? tokensList.get(tokensList.size() - 1) : "";
			boolean isNegativeNumber = false;
			
			if (Character.isWhitespace(c)) {
				continue;
			}
			
			if (c == '-' && (tokensList.size() == 0 || lastToken.equals("("))) {
				isNegativeNumber = true;
			}
			
			if (isNegativeNumber || Character.isDigit(c) || c == '.') {
				String number = "" + c;
				while (Character.isDigit(charAtOrNUL(operation, i + 1)) || charAtOrNUL(operation, i + 1) == '.') {
					c = operation.charAt(++i);
					number += c;
				}
				if (lastToken.equals(")")) {
					tokensList.add("*");
				}
				tokensList.add(number);
				continue;
			}
			
			if (c == '+' || c == '-' || c == '*' || c == '/' || c == '(' || c == ')') {
				
				if (c == '(' && (isNumber(lastToken) || lastToken.equals(")"))) {
					tokensList.add("*");
				}
				tokensList.add("" + c);
				continue;
			}
		}
		
		String[] tokens = {};
		tokens = tokensList.toArray(tokens);
		return getExpression(tokens);
	}
	
	// Devuelve una Expression basada en un arreglo de strings.
	// Ej:
	//     {"5", "+", "2"} -> Addition(5, 2)
	public static Expression getExpression(String[] tokens) {
		return getExpressionFromTokens(tokens, new AtomicReference<Integer>(0));
	}
	
	//#region Lógica de conversión a Expression.

	private static Expression getParenthesesExpression(String[] tokens, AtomicReference<Integer> index) {
		int from = index.get() + 1,
			to = -1;
		int parenthesesCount = 1;
		
		for (int i = from; i < tokens.length; i++) {
			if (tokens[i].equals("(")) {
				parenthesesCount++;
			}
			else if (tokens[i].equals(")")) {
				parenthesesCount--;
			}
			
			if (parenthesesCount == 0) {
				to = i;
				break;
			}
		}
		
		if (to < 0) {
			out.println("Invalid expression. Unclosed parentheses (index: " + index.get() + ")");
			return null;
		}
		
		index.set(to);
		String[] tokensCopy = Arrays.copyOfRange(tokens, from, to);
		return getExpressionFromTokens(tokensCopy, new AtomicReference<Integer>(0));
	}
	
	private static Expression getExpressionFromTokens(String[] tokens, AtomicReference<Integer> index) {
		return getExpressionFromTokens(tokens, index, null);
	}
	
	private static Expression getExpressionFromTokens(String[] tokens, AtomicReference<Integer> index, Expression first) {
		if (tokens.length == 1) {
			return new Number(Double.parseDouble(tokens[0]));
		}
		
		Expression expression = null;
		Expression firstExpression = first;
		Expression secondExpression = null;
		String operator = "";
		
		if (first != null) {
			index.set(index.get() + 1);
		}
		
		for (; index.get() < tokens.length; index.set(index.get() + 1)) {
			if (operator.equals("")) {
				if (isOperator(tokens[index.get()])) {
					operator = tokens[index.get()];
				} else if (tokens[index.get()].equals("(")) {
					firstExpression = getParenthesesExpression(tokens, index);
				} else {
					try {
						double number = Double.parseDouble(tokens[index.get()]);
						firstExpression = new Number(number);
					} catch (Exception e) {
						out.println("Invalid expression (err: 0) (index: " + index.get() + ")");
						return null;
					}
				}
			} else {
				try {
					if (expression != null) {
						firstExpression = expression;
					}
					
					if (tokens[index.get()].equals("(")) {
						secondExpression = getParenthesesExpression(tokens, index);
					}
					
					boolean endOfOperation = tokens.length <= index.get() + 1;
					
					if (!endOfOperation &&
						isOperator(tokens[index.get() + 1]) &&
						OPERATORS_PRIORITY.get(operator) < OPERATORS_PRIORITY.get(tokens[index.get() + 1])) {
						if (secondExpression != null) {
							secondExpression = getExpressionFromTokens(tokens, index, secondExpression);
						} else {
							secondExpression = getExpressionFromTokens(tokens, index);
						}
					} else if (secondExpression == null) {
						secondExpression = new Number(Double.parseDouble(tokens[index.get()]));
					}
					
					if (secondExpression == null) {
						out.println("Invalid expression (err: 2) (index: " + index.get() + ")");
						return null;
					}
					
					switch (operator) {
						case "+":
							expression = new Addition(firstExpression, secondExpression);
							break;
						case "-":
							expression = new Subtraction(firstExpression, secondExpression);
							break;
						case "*":
							expression = new Multiplication(firstExpression, secondExpression);
							break;
						case "/":
							expression = new Division(firstExpression, secondExpression);
							break;
					}
					
					operator = "";
					firstExpression = null;
					secondExpression = null;
				} catch (Exception e) {
					out.println("Invalid expression (err: 1) (index: " + index.get() + ")");
					return null;
				}
			}
		}
		
		return expression;
	}
	
	private static char charAtOrNUL(String string, int index) {
		if (index >= string.length())
			return '\0';
		return string.charAt(index);
	}
	
	private static boolean isNumber(String string) {
		try {
			Double.parseDouble(string);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	//#endregion

	// Calcula el valor de la expresión.
	public abstract double getValue();
	
	// Muestra en la consola el árbol jerárquico de expresiones.
	// Ej:
	//     5 * 2 + 16 / 8
	//
	//     [Addition]
	//         [Multiplication]
	//             [Number: 5]
	//             [Number: 2]
	//         [Division]
	//             [Number: 16]
	//             [Number: 8]
	public abstract void printTree(String indentation, int indentCount);
	
	static class Number extends Expression {
		double value;
		
		public Number(double value) {
			this.value = value;
		}
		
		@Override public double getValue() {
			return this.value;
		}
		
		@Override public String toString() {
			return "" + this.value;
		}

		@Override public void printTree(String indentation, int indentCount) {
			String finalIndentation = "";
			for (int i = 0; i < indentCount; i++)
				finalIndentation += indentation;
			out.println(finalIndentation + "[Number: " + this.value + "]");
		}
	}
	
	static abstract class Operator extends Expression {
		Expression value1;
		Expression value2;
		
		public Operator(Expression value1, Expression value2) {
			this.value1 = value1;
			this.value2 = value2;
		}
		
		@Override public void printTree(String indentation, int indentCount) {
			String finalIndentation = "";
			for (int i = 0; i < indentCount; i++)
				finalIndentation += indentation;
			out.println(finalIndentation + "[" + this.getClass().getName() + "]");
			this.value1.printTree(indentation, indentCount + 1);
			this.value2.printTree(indentation, indentCount + 1);
		}
	}
	
	static class Addition extends Operator {
		public Addition(Expression value1, Expression value2) {
			super(value1, value2);
		}
		
		@Override public double getValue() {
			double value1 = this.value1.getValue();
			double value2 = this.value2.getValue();
			log(value1 + " + " + value2 + " = " + (value1 + value2));
			return value1 + value2;
		}
		
		@Override public String toString() {
			return "(" + this.value1.toString() + " + " + this.value2.toString() + ")";
		}
	}
	
	static class Subtraction extends Operator {
		public Subtraction(Expression value1, Expression value2) {
			super(value1, value2);
		}
		
		@Override public double getValue() {
			double value1 = this.value1.getValue();
			double value2 = this.value2.getValue();
			log(value1 + " - " + value2 + " = " + (value1 - value2));
			return value1 - value2;
		}
		
		@Override public String toString() {
			return "(" + this.value1.toString() + " - " + this.value2.toString() + ")";
		}
	}
	
	static class Multiplication extends Operator {
		public Multiplication(Expression value1, Expression value2) {
			super(value1, value2);
		}

		@Override public double getValue() {
			double value1 = this.value1.getValue();
			double value2 = this.value2.getValue();
			log(value1 + " * " + value2 + " = " + (value1 * value2));
			return value1 * value2;
		}
		
		@Override public String toString() {
			return "(" + this.value1.toString() + " * " + this.value2.toString() + ")";
		}
	}
	
	static class Division extends Operator {
		public Division(Expression value1, Expression value2) {
			super(value1, value2);
		}

		@Override public double getValue() {
			double value1 = this.value1.getValue();
			double value2 = this.value2.getValue();
			log(value1 + " / " + value2 + " = " + (value1 / value2));
			return value1 / value2;
		}
		
		@Override public String toString() {
			return "(" + this.value1.toString() + " / " + this.value2.toString() + ")";
		}
	}
	
	private static void log(String x) {
		if (LOGS)
			System.out.println(x);
	}
}

/*

    ___________
	|____2+2=4|
}   |7|8|9|÷|C|
    |4|5|6|x|(|
	|1|2|3|-|)|
	|0|,|%|+|=|

*/
