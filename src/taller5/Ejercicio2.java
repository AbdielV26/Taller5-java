package taller5;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

/**
 * Ejercicio 2 - Promedio de tres notas (BufferedReader)
 *
 * El autograde invoca por reflexión:
 *   calcularPromedio(double n1, double n2, double n3)
 *   obtenerEstado(double promedio)
 *
 * El main debe usar BufferedReader para leer las notas.
 */
public class Ejercicio2 {

    public static double calcularPromedio(double n1, double n2, double n3) {
        // TODO: retornar (n1 + n2 + n3) / 3.0
        return 0.0;
    }

    public static String obtenerEstado(double promedio) {
        // TODO: retornar "APROBADO" si promedio >= 70, si no "REPROBADO"
        return "";
    }

    public static void main(String[] args) throws IOException {
        // TODO:
        // 1. Crear BufferedReader sobre System.in
        // 2. Leer las tres notas con br.readLine()
        // 3. Convertir con Double.parseDouble
        // 4. Mostrar con System.out.printf EXACTO:
        //      Promedio: X.XX
        //      Estado: APROBADO   (o REPROBADO)
    }
}
