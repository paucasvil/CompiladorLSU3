package compilador;

/**
 *
 * @author layla
 */

import java.util.Stack;

public class Polaca {

    // Método para determinar la precedencia y el tipo del símbolo
    private int tipoYPrecedencia(char c) {
        switch (c) {
            case '+': case '-': return 1;   // Suma y resta
            case '*': case '/': return 2;   // Multiplicación y división
            case '^': return 3;             // Potencia
            case '(': case ')': return 0;   // Paréntesis
            default: return -1;             // Operandos
        }
    }

    // Método para convertir de infija a prefija
    public String notacionPolaca ( String expInfija ) {
        expInfija = expInfija.replaceAll("\\s", "");
        
        // Invertir la expresión infija
        StringBuilder sb = new StringBuilder(expInfija).reverse();
        for (int i = 0; i < sb.length(); i++) {
            if (sb.charAt(i) == '(') sb.setCharAt(i, ')');
            else if (sb.charAt(i) == ')') sb.setCharAt(i, '(');
        }

        // Convertir a postfija
        Stack<Character> stack = new Stack<>();
        StringBuilder expPostfija = new StringBuilder();

        for (int i = 0; i < sb.length(); i++) {
            char carac = sb.charAt(i);
            int actual = tipoYPrecedencia(carac);

            if (actual == -1) { // Operando
                expPostfija.append(carac);
            } else if (carac == '(') {
                stack.push(carac);
            } else if (carac == ')') {
                while (!stack.isEmpty() && stack.peek() != '(') {
                    expPostfija.append(stack.pop());
                }
                stack.pop(); // Quitar el paréntesis izquierdo
            } else { // Operador
                while (!stack.isEmpty() && tipoYPrecedencia(stack.peek()) >= actual) {
                    expPostfija.append(stack.pop());
                }
                stack.push(carac);
            }
        }

        // Vaciar la pila restante
        while (!stack.isEmpty()) {
            expPostfija.append(stack.pop());
        }

        // Invertir el resultado postfijo para obtener prefijo
        return expPostfija.reverse().toString();
    }
}
