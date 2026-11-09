package taller5;

import javax.swing.JOptionPane;

/**
 * Ejercicio 1 - Calculadora de propina (JOptionPane)
 *
 * El autograde invoca por reflexión:
 *   calcularPropina(double monto, double porcentaje)
 *   calcularTotal(double monto, double porcentaje)
 *
 * El main debe usar JOptionPane para interactuar con el usuario.
 */
public class Ejercicio1 {

    public static double calcularPropina(double monto, double porcentaje) {
        // TODO: retornar la propina = monto * porcentaje / 100
        return 0.0;
    }

    public static double calcularTotal(double monto, double porcentaje) {
        // TODO: retornar monto + propina
        return 0.0;
    }

    public static void main(String[] args) {
        // TODO:
        // 1. Pedir monto con JOptionPane.showInputDialog
        // 2. Pedir porcentaje con JOptionPane.showInputDialog
        // 3. Convertir con Double.parseDouble
        // 4. Calcular con los métodos de arriba
        // 5. Mostrar con JOptionPane.showMessageDialog el formato EXACTO:
        //      Propina: $X.XX
        //      Total: $Y.YY
    }
}
