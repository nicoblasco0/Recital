package sinfonia;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class App {
    private static Recital recital;
    private static Scanner scanner = new Scanner(System.in);

    /**
     * Punto de entrada principal del programa.
     */
    public static void main(String[] args) {
        System.out.println("=== Bienvenido al Sistema de Gestión de Recitales 'Sinfonía' ===");
        
        // --- 1. Carga de Datos ---
        try {
            System.out.println("Cargando datos desde los archivos...");
            recital = CargarDatos.cargarRecitalDesdeArchivos(
                "artistas.json", 
                "recital.json", 
                "artistas-discografica.json"
            );
            System.out.println("¡Datos cargados exitosamente!");
            System.out.println("===============================================================");
            
            // --- 2. Mostrar Menú ---
            mostrarMenuPrincipal();

        } catch (IOException e) {
            System.err.println("¡ERROR FATAL AL CARGAR ARCHIVOS!");
            System.err.println("No se puede iniciar el programa.");
            System.err.println("Detalle: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("¡ERROR INESPERADO!");
            e.printStackTrace();
        } finally {
            scanner.close(); 
            System.out.println("\nGracias por usar 'Sinfonía'. ¡Hasta luego!");
        }
    }

    /**
     * Muestra el menú principal y maneja la selección del usuario.
     */
    private static void mostrarMenuPrincipal() {
        boolean salir = false;
        while (!salir) {
            System.out.println("\n\n--- MENÚ PRINCIPAL ---");
            System.out.println("1. Listar estado de todas las canciones");
            System.out.println("2. Ver roles faltantes para el recital completo");
            System.out.println("3. Ver roles faltantes para una canción específica");
            System.out.println("4. Contratar artistas para una canción específica");
            System.out.println("5. Contratar artistas para TODO el recital");
            System.out.println("6. Entrenar artista");
            System.out.println("7. Listar artistas contratados y costo total");
            System.out.println("--------------------------------------------");
            System.out.println("8. [PROLOG] Pregunta de entrenamientos mínimos");
            System.out.println("0. Salir");
            System.out.print("Seleccione una opción: ");

            String opcion = scanner.nextLine();

            try {
                switch (opcion) {
                    case "1":
                        recital.listarEstadoCanciones();
                        break;
                    case "2":
                        uiVerRolesFaltantesRecital();
                        break;
                    case "3":
                        uiVerRolesFaltantesCancion();
                        break;
                    case "4":
                        uiContratarParaCancion();
                        break;
                    case "5":
                        recital.contratarParaRecital();
                        break;
                    case "6":
                        uiEntrenarArtista();
                        break;
                    case "7":
                        recital.listarArtistasContratados();
                        break;
                    case "8":
                        uiPreguntaProlog();
                        break;
                    case "0":
                        salir = true;
                        break;
                    default:
                        System.err.println("Opción no válida. Por favor, intente de nuevo.");
                }
            } catch (RuntimeException e) {
                // Capturamos cualquier error de lógica (ej. "No hay artistas")
                System.err.println("\n¡Ha ocurrido un error durante la operación!");
                System.err.println("Mensaje: " + e.getMessage());
            }
            
            if (!salir) {
                System.out.println("\n(Presione Enter para continuar...)");
                scanner.nextLine(); // Pausa
            }
        }
    }

    // --- Métodos de UI (ayudantes del menú) ---

    /**
     * UI para la opcion 2: Ver roles faltantes del recital.
     */
    private static void uiVerRolesFaltantesRecital() {
        Map<String, Integer> faltantes = recital.getRolesFaltantesRecital();
        
        System.out.println("\n===== Roles Faltantes (Global) =====");
        if (faltantes.isEmpty()) {
            System.out.println("¡Felicidades! Todos los roles del recital están cubiertos.");
        } else {
            for (Map.Entry<String, Integer> entrada : faltantes.entrySet()) {
                System.out.println("- " + entrada.getKey() + ": Faltan " + entrada.getValue());
            }
        }
        System.out.println("====================================");
    }
    
    /**
    * UI para la opcion 3: Ver roles faltantes de una canción.
    */
   private static void uiVerRolesFaltantesCancion() {
       Cancion cancion = uiBuscarCancion();
       if (cancion == null) return; // El usuario canceló o hubo error
       
       Map<String, Integer> faltantes = recital.getRolesFaltantesCancion(cancion);
       
       System.out.println("\n===== Roles Faltantes para '" + cancion.getTitulo() + "' =====");
       if (faltantes.isEmpty()) {
           System.out.println("¡Esta canción tiene todos los roles cubiertos!");
       } else {
           for (Map.Entry<String, Integer> entrada : faltantes.entrySet()) {
               System.out.println("- Falta(n): " + entrada.getValue() + " de '" + entrada.getKey() + "'");
           }
       }
       System.out.println("===================================" + ("=").repeat(cancion.getTitulo().length()));
   }
   
   /**
    * Ayudante de UI para buscar una canción por título o número.
    * @return El objeto Cancion seleccionado, o null si cancela.
    */
   private static Cancion uiBuscarCancion() {
       System.out.println("\n--- Seleccionar Canción ---");
       List<Cancion> setlist = recital.getSetlist();
       for (int i = 0; i < setlist.size(); i++) {
           System.out.println((i + 1) + ". " + setlist.get(i).getTitulo());
       }
       System.out.println("0. Cancelar");
       System.out.print("\nIngrese el NÚMERO de la canción: ");
       
       try {
           int num = Integer.parseInt(scanner.nextLine());
           if (num > 0 && num <= setlist.size()) {
               return setlist.get(num - 1); // Devuelve la canción (índice es num - 1)
           } else if (num == 0) {
               System.out.println("Operación cancelada.");
               return null;
           } else {
               System.err.println("Número fuera de rango.");
               return null;
           }
       } catch (NumberFormatException e) {
           System.err.println("Entrada no válida. Debe ingresar un número.");
           return null;
       }
   }
   
   /**
    * UI para la opción 4: Contratar para una canción.
    */
   private static void uiContratarParaCancion() {
       Cancion cancion = uiBuscarCancion();
       
       // El usuario cancelo o hubo error
       if (cancion == null) return;
       recital.contratarParaCancion(cancion);
   }
   
   
    /**
     * UI para la opción 6: Entrenar un artista.
     */
   private static void uiEntrenarArtista() {
  
       ArtistaExterno artista = uiBuscarArtistaCandidato();
       
       // Verificamos si el usuario cancelo
       if (artista == null) {
           return; 
       }

       // Verificamos si el artista esta contratado
       if (artista.YaContratado()) {
           System.err.println("Error: No se puede entrenar a " + artista.getNombre() + " porque ya fue contratado.");
           return;
       }
       
       // Pedimos el rol
       System.out.print("Ingrese el nuevo rol a aprender para " + artista.getNombre() + ": ");
       String rol = scanner.nextLine();
       
       // Llamamos al metodo del recital 
       recital.entrenarArtista(artista.getNombre(), rol);
   }
    
    /**
     * Ayudante de UI para buscar un artista candidato por número.
     * @return El objeto ArtistaExterno seleccionado, o null si cancela.
     */
    private static ArtistaExterno uiBuscarArtistaCandidato() {
        System.out.println("\n--- Seleccionar Artista Candidato ---");
        List<ArtistaExterno> candidatos = recital.getArtistasCandidatos();
        
        if (candidatos.isEmpty()) {
            System.err.println("No hay artistas candidatos cargados.");
            return null;
        }

        // Mostramos la lista numerada
        for (int i = 0; i < candidatos.size(); i++) {
            ArtistaExterno art = candidatos.get(i);
            // Mostramos estado (Contratado/Disponible) para que el usuario sepa
            String estado = art.YaContratado() ? " (Ya Contratado)" : " (Disponible)";
            System.out.println((i + 1) + ". " + art.getNombre() 
                + " - Costo actual: $" + String.format("%.2f", art.getCostoContratacion()) 
                + estado);
        }
        System.out.println("0. Cancelar");
        System.out.print("\nIngrese el NÚMERO del artista: ");
        
        try {
            int num = Integer.parseInt(scanner.nextLine());
            if (num > 0 && num <= candidatos.size()) {
                return candidatos.get(num - 1);
            } else if (num == 0) {
                System.out.println("Operación cancelada.");
                return null;
            } else {
                System.err.println("Número fuera de rango.");
                return null;
            }
        } catch (NumberFormatException e) {
            System.err.println("Entrada no válida. Debe ingresar un número.");
            return null;
        }
    }

    /**
     * UI para la opción 8: Pregunta de Prolog (placeholder)
     */
    private static void uiPreguntaProlog() {
        System.out.println("--- Integración con Prolog ---");
        System.out.println("Esta funcionalidad requiere la integración con JPL (Java Prolog Library).");
        System.out.println("La lógica de Java debe recolectar los datos y pasarlos a un predicado de Prolog.");
    }

}
