import java.util.Arrays;
import java.util.Scanner;

public class Main {
	public static void main(String[] args) {
		// Usar interfaz gráfica o consola dependiendo de si el programa se ejecutó con argumentos.
		if (args.length == 0) {
			Calculator calculator = new Calculator();
			calculator.setVisible(true);
			calculator.setLocationRelativeTo(null);
		} else {
			Scanner scanner = new Scanner(System.in);
			// Reducir los argumentos a una operación aritmética.
			String operation = Arrays.asList(args).stream().reduce((a, b) -> a + b).get();
			Expression expression = Expression.getExpression(operation);
			double value = expression.getValue();
			
			value = Calculator.FixDouble(value);
			
			System.out.println("result: " + value);
			scanner.close();
		}
	}
}
