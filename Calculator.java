import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.*;
import java.io.File;
import javax.swing.*;
import javax.swing.border.LineBorder;
import static java.lang.System.err;
import static java.lang.System.out;

// Interfaz gráfica de la calculadora.
public class Calculator extends JFrame implements ActionListener, KeyListener, FocusListener {
	// Arregla errores de precisión en los la parte decimal de los números.
	// Ejemplo:
	//     0.1 + 0.2 = 0.30000000000000004
	//     FixDouble(0.1 + 0.2) = 0.3
	public static double FixDouble(double value) {
		if (Double.isFinite(value)) {
			long longValue = Math.round(value * 1000000000000000d);
			if (longValue > Long.MIN_VALUE && longValue < Long.MAX_VALUE)
				value = (double)longValue / 1000000000000000d;
		}
		return value;
	}
	
	private String operation = "";
	private boolean shiftPressed = false;
	
	private Container container;
	private JButton clearButton;
	private JButton eraseButton;
	private JButton resultButton;
	private CalculatorButton[] digitButtons = new CalculatorButton[10];
	private CalculatorButton additionButton;
	private CalculatorButton subtractionButton;
	private CalculatorButton multiplicationButton;
	private CalculatorButton divisionButton;
	private CalculatorButton openParenthesesButton;
	private CalculatorButton closeParenthesesButton;
	private CalculatorButton decimalPointButton;
	private JPanel operationPanel;
	private JLabel operationLabel;
	
	// Posicion de los botones numéricos.
	private Point[] digitsPositions = {
		new Point(10, 250),
		new Point(10, 190),
		new Point(70, 190),
		new Point(130, 190),
		new Point(10, 130),
		new Point(70, 130),
		new Point(130, 130),
		new Point(10, 70),
		new Point(70, 70),
		new Point(130, 70)
	};
	
	public Calculator() {
		setTitle("Calculadora");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setLayout(null);
		setFocusable(true);
		addKeyListener(this);
		addFocusListener(this);
		
		container = new Container();
		container.setPreferredSize(new Dimension(310, 310));
		setContentPane(container);
		
		try {
			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, new File("digital-7.mono.ttf")));
		} catch (Exception e) {
			out.println(e);
		}
		
		operationLabel = new JLabel(operation);
		operationLabel.setHorizontalAlignment(JLabel.RIGHT);
		operationLabel.setForeground(new Color(0x617491));
		operationLabel.setFont(new Font("Digital-7 Mono", Font.PLAIN, 32));
		
		operationPanel = new JPanel();
		operationPanel.setBounds(10, 10, 290, 50);
		operationPanel.setLayout(new BorderLayout());
		operationPanel.setBorder(new LineBorder(new Color(0x617491), 2, true));
		operationPanel.add(operationLabel);
		add(operationPanel);
		
		clearButton = new JButton("C");
		clearButton.setBounds(250, 70, 50, 50);
		clearButton.addActionListener(this);
		add(clearButton);
		
		eraseButton = new JButton("<-"); // "\u232b"
		eraseButton.setBounds(130, 250, 50, 50);
		eraseButton.addActionListener(this);
		add(eraseButton);
		
		resultButton = new JButton("=");
		resultButton.setBounds(250, 250, 50, 50);
		resultButton.addActionListener(this);
		add(resultButton);
		
		for (int i = 0; i < digitButtons.length; i++) {
			digitButtons[i] = new CalculatorButton(Integer.toString(i));
			digitButtons[i].setBounds(new Rectangle(digitsPositions[i], new Dimension(50, 50)));
			digitButtons[i].addActionListener(this);
			add(digitButtons[i]);
		}
		
		additionButton = new CalculatorButton("+");
		additionButton.setBounds(190, 250, 50, 50);
		additionButton.addActionListener(this);
		add(additionButton);
		
		subtractionButton = new CalculatorButton("-");
		subtractionButton.setBounds(190, 190, 50, 50);
		subtractionButton.addActionListener(this);
		add(subtractionButton);
		
		multiplicationButton = new CalculatorButton("*");
		multiplicationButton.setBounds(190, 130, 50, 50);
		multiplicationButton.addActionListener(this);
		add(multiplicationButton);
		
		divisionButton = new CalculatorButton("/");
		divisionButton.setBounds(190, 70, 50, 50);
		divisionButton.addActionListener(this);
		add(divisionButton);
		
		openParenthesesButton = new CalculatorButton("(");
		openParenthesesButton.setBounds(250, 130, 50, 50);
		openParenthesesButton.addActionListener(this);
		add(openParenthesesButton);
		
		closeParenthesesButton = new CalculatorButton(")");
		closeParenthesesButton.setBounds(250, 190, 50, 50);
		closeParenthesesButton.addActionListener(this);
		add(closeParenthesesButton);
		
		decimalPointButton = new CalculatorButton(".");
		decimalPointButton.setBounds(70, 250, 50, 50);
		decimalPointButton.addActionListener(this);
		add(decimalPointButton);
		
		pack();
	}

	// Acción de los botones.
	@Override
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if (source instanceof CalculatorButton) {
			CalculatorButton button = (CalculatorButton)source;
			tryAddToken(button.getValue().charAt(0));
		} else if (source == resultButton) {
			evaluate();
		} else if (source == eraseButton) {
			erase();
		} else if (source == clearButton) {
			clear();
		}
	}

	// Ingresar la operación por el teclado.
	@Override
	public void keyTyped(KeyEvent e) {
		tryAddToken(e.getKeyChar());
	}

	// Mostrar el resultado, borrar el último caracter o borrar todo con el teclado.
	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ENTER) {
			evaluate();
		} else if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
			if (shiftPressed) {
				clear();
			} else {
				erase();
			}
		} else if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
			shiftPressed = true;
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
			shiftPressed = false;
		}
	}
	
	@Override
	public void focusGained(FocusEvent e) {
	}

	// Enfocar al programa cuando pierda el foco.
	// Cuando el programa está desenfocado, no se puede usar el teclado para escribir.
	@Override
	public void focusLost(FocusEvent e) {
		requestFocus();
	}
	
	// Agregar un caracter a la operación cuando sea válido.
	// Solo válida unos pocos casos pero no la mayoría, por lo que aún así se puede escribir una operación inválida.
	private void tryAddToken(char character) {
		char lastCharacter = operation.length() > 0 ? operation.charAt(operation.length() - 1) : '\0';
		
		if (Expression.isOperator(character)) {
			if ((lastCharacter == '\0' && character != '-') || Expression.isOperator(lastCharacter)) {
				return;
			}
		} else if (character == ')') {
			if (lastCharacter == '\0') {
				return;
			}
		} else if (!Character.isDigit(character) && character != '(') {
			if (character != '.' || lastCharacter == '.') {
				return;
			}
		}
		
		updateOperation(operation + character);
	}
	
	// Mostrar el resultado de la operación si es válida.
	private void evaluate() {
		Expression expression = Expression.getExpression(operation);
		if (expression != null) {
			try {
				out.print(operation + " = ");
				
				double result = FixDouble(expression.getValue());
				if (Double.isFinite(result)) {
					updateOperation(Double.toString(result), true);
				}
				
				out.println(result);
			} catch (Exception e) {
				updateOperation("Error");
				err.println(e);
			}
		} else {
			err.println("Error");
		}
	}
	
	// Borrar el último caracter de la operación.
	private void erase() {
		if (operation.length() > 0) {
			updateOperation(operation.substring(0, operation.length() - 1));
		}
	}
	
	// Borrar la operación.
	private void clear() {
		updateOperation("");
		out.println("cleared");
	}
	
	// Mostrar la operación o resultado en la panel superior.
	private void updateOperation(String operation, boolean removeUnnecessaryDecimal) {
		if (removeUnnecessaryDecimal && operation.endsWith(".0")) {
			updateOperation(operation.substring(0, operation.length() - 2));
		} else {
			updateOperation(operation);
		}
	}
	
	// Mostrar la operación o resultado en la panel superior.
	private void updateOperation(String operation) {
		this.operation = operation;
		operationLabel.setText(this.operation);
	}
	
	// Boton que agrega un valor a la operación.
	class CalculatorButton extends JButton {
		private String value;
		
		public CalculatorButton(String value) {
			super(value, null);
			this.value = value;
		}
		
		public String getValue() {
			return value;
		}
	}
}
