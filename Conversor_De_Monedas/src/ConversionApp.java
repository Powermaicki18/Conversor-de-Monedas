import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Scanner;
import java.util.Set;

public class ConversionApp {

    public static class ConversionActual {
        // definir la API y el APIKey para que no se puedan modificar
        private static final String APIKey = "b636d344f43ab530481210a5";
        private static final String direccion = "https://v6.exchangerate-api.com/v6/";

        // definir HTTPCliente
        private final HttpClient httpClient;
        Scanner sc = new Scanner(System.in);

        public ConversionActual() {
            // aplicar HTTPClient
            this.httpClient = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(30))//tiempo de espera para conectar con un server, en caso de que no, se interrumpe y genera error
                    .build();
        }

        public void ejecutarConversor() {
            System.out.println("=== CONVERSOR DE MONEDAS ===");
            System.out.println("¡Te doy la bienvenida al Conversor de Monedas Manguito!");

            while (true) {
                try {
                    mostrarMenu();
                    char opcion = ' '; //tipo char para consumir menos memoria al ser solo un caracter
                    opcion = sc.next().charAt(0);
                    sc.nextLine(); // Limpiar buffer

                    switch (opcion) {
                        case '1':
                            mostrarMonedasDisponibles();
                            break;

                        case '2':
                            realizarConversion();
                            break;

                        case '0':
                            System.out.println("""
                                    \n¡Gracias por usar el conversor!
                                    Terminando programa.
                                    """);
                            return;

                        default:
                            System.out.println("Opción no válida. Intenta de nuevo.");
                            break;
                    }

                } catch (Exception e) {
                    System.err.println("Error: " + e.getMessage());
                    sc.nextLine(); // Limpiar buffer en caso de error
                }
            }
        }

        private void mostrarMenu() {

            System.out.println("""
                    \n--- MENÚ PRINCIPAL ---
                    1. Ver monedas disponibles
                    2. Convertir monedas
                    0. Salir
                    """);
            System.out.print("Seleccione una opcion: ");
        }

        //Implementar la API para conversiones
        private void realizarConversion() throws IOException, InterruptedException {
            System.out.print("Ingresa la moneda de origen (ej: USD): ");
            var monedaOrigen = sc.nextLine().toUpperCase(); //ingresar datos y pasarlos a mayus

            System.out.print("Ingresa la moneda de destino (ej: EUR): ");
            var monedaDestino = sc.nextLine().toUpperCase();

            System.out.print("Ingresa la cantidad a convertir: ");
            var cantidad = sc.nextDouble();
            sc.nextLine(); // Limpiar buffer

            // Obtener tasas de cambio
            JsonObject tasasCambio = obtenerTasasCambio(monedaOrigen);

            if (tasasCambio == null) { //si la moneda escrita no se encuentra, manda error
                System.out.println("Error al obtener las tasas de cambio.");
                return;
            }

            // Convertir monedas usando funciones
            double resultado = convertirMoneda(tasasCambio, monedaDestino, cantidad);

            if (resultado != -1) {
                System.out.printf("\nLa conversión de: %.2f %s es equivalente a: %.2f %s%n",
                        cantidad, monedaOrigen, resultado, monedaDestino);
            } else {
                System.out.println("Moneda de destino no encontrada: " + monedaDestino);
            }
        }

        // definir HttpRequest
        private JsonObject obtenerTasasCambio(String monedaBase) throws IOException, InterruptedException {
            String url = direccion + APIKey + "/latest/" + monedaBase;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(30))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            // definir HttpResponse
            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                // Analizar respuesta JSON con Gson
                return analizarRespuestaJSON(response.body());
            } else {
                System.err.println("Error HTTP: " + response.statusCode());
                System.err.println("Respuesta: " + response.body());
                return null;
            }
        }

        // Analizar la respuesta en formato JSON
        private JsonObject analizarRespuestaJSON(String jsonString) {
            try {
                JsonObject jsonObject = JsonParser.parseString(jsonString).getAsJsonObject();

                // Verificar si la respuesta fue exitosa
                String result = jsonObject.get("result").getAsString();
                if (!"success".equals(result)) {
                    System.err.println("Error en la API: " +
                            jsonObject.get("error-type").getAsString());
                    return null;
                }

                return jsonObject.getAsJsonObject("conversion_rates");

            } catch (Exception e) {
                System.err.println("Error al parsear JSON: " + e.getMessage());
                return null;
            }
        }

        // función para convertir monedas
        private double convertirMoneda(JsonObject tasasCambio, String monedaDestino, double cantidad) {
            if (tasasCambio.has(monedaDestino)) {
                double tasaConversion = tasasCambio.get(monedaDestino).getAsDouble();
                return cantidad * tasaConversion;
            }
            return -1; // Moneda no encontrada
        }

        // filtrar monedas usando Gson
        private void mostrarMonedasDisponibles() throws IOException, InterruptedException {
            System.out.println("\nObteniendo lista de monedas disponibles...");

            JsonObject tasasCambio = obtenerTasasCambio("USD");
            if (tasasCambio == null) {
                System.out.println("No se pudieron obtener las monedas disponibles.");
                return;
            }

            // buscar y mostrar codigos de las monedas
            Set<String> codigosMoneda = tasasCambio.keySet(); // set para que no se repitan datos e8oñb

            System.out.println("\n=== MONEDAS DISPONIBLES ===");
            System.out.println("Total de monedas: " + codigosMoneda.size());
            System.out.println("\nCódigos de moneda:");

            int contador = 0;
            for (String codigo : codigosMoneda) {
                System.out.printf("%-6s", codigo);
                contador++;

                // Nueva línea cada 10 monedas para mejor legibilidad
                if (contador % 10 == 0) {
                    System.out.println();
                }
            }

            if (contador % 10 != 0) {
                System.out.println();
            }

            // Mostrar algunas monedas populares con sus tasas
            mostrarMonedasPopulares(tasasCambio);
        }

        private void mostrarMonedasPopulares(JsonObject tasasCambio) {
            String[] monedasPopulares = {"EUR", "GBP", "JPY", "CAD", "AUD", "CHF", "CNY", "MXN", "BRL", "COP"};

            System.out.println("\n=== TASAS POPULARES (desde USD) ===");
            for (String moneda : monedasPopulares) {
                if (tasasCambio.has(moneda)) {
                    double tasa = tasasCambio.get(moneda).getAsDouble();
                    System.out.printf("1 USD = %.4f %s%n", tasa, moneda);
                }
            }
        }
    }

}
