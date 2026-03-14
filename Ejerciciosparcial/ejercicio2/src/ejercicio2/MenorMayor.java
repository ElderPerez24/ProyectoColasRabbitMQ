package ejercicio2;

/*
 * Encuentra el segundo número menor y el segundo número mayor en un arreglo.
 *
 * Complejidad temporal: O(n)
 * Justificación: el arreglo se recorre una sola vez.
 *
 * Complejidad espacial: O(1)
 * Justificación: solo se usan variables simples, no estructuras adicionales.
 */

public class MenorMayor {

    public static int[] secondMinMax(int[] numbers) {

        // Variables para guardar menor, segundo menor, mayor y segundo mayor
        int min1 = Integer.MAX_VALUE;
        int min2 = Integer.MAX_VALUE;
        int max1 = Integer.MIN_VALUE;
        int max2 = Integer.MIN_VALUE;

        // Recorre el arreglo
        for (int n : numbers) {

            // Busca el menor y segundo menor
            if (n < min1) {
                min2 = min1;
                min1 = n;
            } else if (n != min1 && n < min2) {
                min2 = n;
            }

            // Busca el mayor y segundo mayor
            if (n > max1) {
                max2 = max1;
                max1 = n;
            } else if (n != max1 && n > max2) {
                max2 = n;
            }
        }

        // Retorna el segundo menor y segundo mayor
        return new int[]{min2, max2};
    }

    public static void main(String[] args) {

        // Arreglo de prueba
        int[] numbers = {7, 2, 9, 4, 18};

        int[] resultado = secondMinMax(numbers);

        // Imprime resultados
        System.out.println("Segundo menor: " + resultado[0]);
        System.out.println("Segundo mayor: " + resultado[1]);
    }
}
