package arreglor;

/*
 * Calcula un puntaje según los valores de un arreglo.
 * 
 * Complejidad temporal: O(n)
 * Justificación: se recorre el arreglo una sola vez.
 * 
 * Complejidad espacial: O(1)
 * Justificación: solo usa variables simples, no estructuras adicionales.
 */

public class PuntajeArreglo {

    public static int score(int[] numbers) {
        int total = 0; // variable acumuladora

        for (int number : numbers) { // recorre el arreglo
            if (number == 5) {
                total += 5; // si es 5 suma 5
            } else if (number % 2 == 0) {
                total += 1; // si es par suma 1
            } else {
                total -= 3; // si es impar resta 3
            }
        }

        return total; // retorna el puntaje
    }

    public static void main(String[] args) {
        int[] arreglo1 = {1, 2, 3, 4, 5};
        int[] arreglo2 = {17, 19, 21};
        int[] arreglo3 = {5, 5, 5};

        System.out.println("Resultado 1: " + score(arreglo1));
        System.out.println("Resultado 2: " + score(arreglo2));
        System.out.println("Resultado 3: " + score(arreglo3));
    }
}
