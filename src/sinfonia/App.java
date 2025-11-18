package sinfonia;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Scanner;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class App {
    private static Recital recital;
    private static Scanner scanner = new Scanner(System.in);


    public static void main(String[] args) {
        System.out.println("=== Bienvenido al Sistema de Gestion de Recitales 'Sinfonia' ===");
        
        // --- Carga de Datos ---
        try {
            System.out.println("Cargando datos desde los archivos...");
            recital = CargarDatos.cargarRecitalDesdeArchivos(
                "artistas.json", 
                "recital.json", 
                "artistas-discografica.json"
            );
            System.out.println("¡Datos cargados exitosamente!");
            System.out.println("===============================================================");
            
            // --- Mostrar Menu ---
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
            System.out.println("\nGracias por usar 'Sinfonia'. ¡Hasta luego!");
        }
    }

    /**
     * Muestra el menu principal y maneja la seleccion del usuario.
     */
    private static void mostrarMenuPrincipal() {
        boolean salir = false;
        while (!salir) {
            System.out.println("\n\n--- MENU PRINCIPAL ---");
            System.out.println("1. Listar estado de todas las canciones");
            System.out.println("2. Ver roles faltantes para el recital completo");
            System.out.println("3. Ver roles faltantes para una cancion especifica");
            System.out.println("4. Contratar artistas para una cancion especifica");
            System.out.println("5. Contratar artistas para TODO el recital");
            System.out.println("6. Entrenar artista");
            System.out.println("7. Eliminar contratacion de Artista");
            System.out.println("8. Listar artistas contratados y costo total");
            System.out.println("9. [PROLOG] Calcular entrenamientos minimos");
            System.out.println("10. Guardar estado del recital en 'recital-out.json'");
            System.out.println("0. Salir");
            System.out.print("\nSeleccione una opcion: ");

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
                        uiQuitarArtista();   
                        break;
                    case "8":
                        recital.listarArtistasContratados();
                        break;
                    case "9":
                    	uiPreguntaProlog();
                        break;
                    case "10":
                        uiExportarEstadoRecital();
                        break;           
                    case "0":
                        salir = true;
                        break;
                    default:
                        System.err.println("Opcion no valida. Por favor, intente de nuevo.");
                }
            } catch (RuntimeException e) {
                // Capturamos cualquier error
                System.err.println("\n¡Ha ocurrido un error durante la operacion!");
                System.err.println("Mensaje: " + e.getMessage());
            }
            
            if (!salir) {
                System.out.println("\n(Presione Enter para continuar...)");
                scanner.nextLine(); // Pausa
            }
        }
    }

    // --- Metodos de UI ---

    /**
     * UI para la opcion 2: Ver roles faltantes del recital.
     */
    private static void uiVerRolesFaltantesRecital() {
        Map<String, Integer> faltantes = recital.getRolesFaltantesRecital();
        
        System.out.println("\n===== Roles Faltantes (Global) =====");
        if (faltantes.isEmpty()) {
            System.out.println("¡Felicidades! Todos los roles del recital estan cubiertos.");
        } else {
            for (Map.Entry<String, Integer> entrada : faltantes.entrySet()) {
                System.out.println("- " + entrada.getKey() + ": Faltan " + entrada.getValue());
            }
        }
        System.out.println("====================================");
    }
    
    /**
    * UI para la opcion 3: Ver roles faltantes de una cancion.
    */
   private static void uiVerRolesFaltantesCancion() {
       Cancion cancion = uiBuscarCancion();
       if (cancion == null) return; // El usuario cancelo o hubo error
       
       Map<String, Integer> faltantes = recital.getRolesFaltantesCancion(cancion);
       
       System.out.println("\n===== Roles Faltantes para '" + cancion.getTitulo() + "' =====");
       if (faltantes.isEmpty()) {
           System.out.println("¡Esta cancion tiene todos los roles cubiertos!");
       } else {
           for (Map.Entry<String, Integer> entrada : faltantes.entrySet()) {
               System.out.println("- Falta(n): " + entrada.getValue() + " de '" + entrada.getKey() + "'");
           }
       }
       System.out.println("===================================" + ("=").repeat(cancion.getTitulo().length()));
   }
   
   /**
    * Ayudante de UI para buscar una cancion por titulo o numero.
    * @return El objeto Cancion seleccionado, o null si cancela.
    */
   private static Cancion uiBuscarCancion() {
       System.out.println("\n--- Seleccionar Cancion ---");
       List<Cancion> setlist = recital.getSetlist();
       for (int i = 0; i < setlist.size(); i++) {
           System.out.println((i + 1) + ". " + setlist.get(i).getTitulo());
       }
       System.out.println("0. Cancelar");
       System.out.print("\nIngrese el numero de la cancion: ");
       
       try {
           int num = Integer.parseInt(scanner.nextLine());
           if (num > 0 && num <= setlist.size()) {
               return setlist.get(num - 1);
           } else if (num == 0) {
               System.out.println("Operacion cancelada.");
               return null;
           } else {
               System.err.println("Numero fuera de rango.");
               return null;
           }
       } catch (NumberFormatException e) {
           System.err.println("Entrada no valida. Debe ingresar un numero.");
           return null;
       }
   }
   
   /**
    * UI para la opcion 4: Contratar para una cancion.
    */
   private static void uiContratarParaCancion() {
       Cancion cancion = uiBuscarCancion();
       
       // El usuario cancelo o hubo error
       if (cancion == null) return;
       recital.contratarParaCancion(cancion);
   }
   
   
    /**
     * UI para la opcion 6: Entrenar un artista.
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
     * Ayudante de UI para buscar un artista candidato por numero.
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
        System.out.print("\nIngrese el numero del artista: ");
        
        try {
            int num = Integer.parseInt(scanner.nextLine());
            if (num > 0 && num <= candidatos.size()) {
                return candidatos.get(num - 1);
            } else if (num == 0) {
                System.out.println("Operacion cancelada.");
                return null;
            } else {
                System.err.println("Numero fuera de rango.");
                return null;
            }
        } catch (NumberFormatException e) {
            System.err.println("Entrada no valida. Debe ingresar un numero.");
            return null;
        }
    }
    
    /**
    *UI para la opcion 8: Pregunta de Prolog
    */
    private static void uiPreguntaProlog() {
    	System.out.println("--- Integración con Prolog ---");
        IntegracionProlog integracionProlog = new IntegracionProlog();
		integracionProlog.entrenamientosMinimos(recital.getSetlist(), recital.getArtistasBase());
    }
    
    /**
     * UI para la opcion 9: Exportar el estado actual a un JSON.
     */
    private static void uiExportarEstadoRecital() {
        System.out.println("\n--- Opcion 9: Exportar Estado del Recital ---");
        String nombreArchivo = "recital-out.json";

        try (Writer writer = new FileWriter(nombreArchivo)) {
        	
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            
            // Creamos el objeto DTO de salida
            RecitalSalidaDTO salida = new RecitalSalidaDTO();
            salida.costoTotal = recital.getCostoTotalContratos();
            
            // Llenamos la lista de canciones
            salida.canciones = new ArrayList<>();
            for (Cancion cancion : recital.getSetlist()) {
                CancionSalidaDTO cancionDTO = new CancionSalidaDTO();
                cancionDTO.titulo = cancion.getTitulo();
                
                Map<String, Integer> faltantes = recital.getRolesFaltantesCancion(cancion);
                cancionDTO.estaCompleta = faltantes.isEmpty();
                cancionDTO.rolesFaltantes = faltantes.isEmpty() ? null : faltantes;
                
                // Llenar los artistas contratados para cada cancion
                cancionDTO.artistasAsignados = new ArrayList<>();
                for (Contrato contrato : recital.getContrataciones()) {
                    if (contrato.getCancion().equals(cancion)) {
                        ContratoSalidaDTO contratoDTO = new ContratoSalidaDTO(
                            contrato.getArtista().getNombre(),
                            contrato.getRolAsignado(),
                            contrato.getCostoPagado()
                        );
                        cancionDTO.artistasAsignados.add(contratoDTO);
                    }
                }
                salida.canciones.add(cancionDTO);
            }
            
            // Conviete el objeto DTO a JSON y lo guarda
            gson.toJson(salida, writer);

            System.out.println("El estado del recital se ha guardado en '" + nombreArchivo + "'");

        } catch (IOException e) {
            System.err.println("Error: No se pudo escribir el archivo JSON de salida.");
            System.err.println("Detalle: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error inesperado al exportar JSON: " + e.getMessage());
            e.printStackTrace();
        }
    }
  
    
    
   
    /* Clases DTO para definir la ESTRUCTURA del JSON de salida)
    Usamos @SuppressWarnings("unused") para evitar warings,
     ya que Gson accede a sus campos.
    */
    
    @SuppressWarnings("unused")
    private static class RecitalSalidaDTO {
        double costoTotal;
        List<CancionSalidaDTO> canciones;
    }
    
    @SuppressWarnings("unused")
    private static class CancionSalidaDTO {
        String titulo;
        boolean estaCompleta;
        Map<String, Integer> rolesFaltantes; // Sera null si esta completa
        List<ContratoSalidaDTO> artistasAsignados;
    }
    
    @SuppressWarnings("unused")
    private static class ContratoSalidaDTO {
        String artista;
        String rol;
        double costoPagado;
        
        ContratoSalidaDTO(String a, String r, double c) {
            this.artista = a;
            this.rol = r;
            this.costoPagado = c;
        }
    }
    
    
    /**
     * UI para la opcion 7: Eliminar contratacion.
     */
    private static void uiQuitarArtista() {


        
        // Obtener la lista de artistas contratados
        Set<ArtistaExterno> artistasContratadosSet = new HashSet<>();
        for (Contrato contrato : recital.getContrataciones()) {
            artistasContratadosSet.add(contrato.getArtista());
        }

        if (artistasContratadosSet.isEmpty()) {
            System.out.println("\nNo hay artistas contratados para quitar.");
            return;
        }

        // Convertir el Set a una Lista para poder mostrarlos con numero
        List<ArtistaExterno> artistasList = new ArrayList<>(artistasContratadosSet);

        System.out.println("\n--- Seleccionar artista ---\n");
        for (int i = 0; i < artistasList.size(); i++) {
            ArtistaExterno art = artistasList.get(i);
            int cancionesAsignadas = recital.getCancionesAsignadas(art);
            System.out.println((i + 1) + ". " + art.getNombre() + 
                               " (" + cancionesAsignadas + " contrato(s) activo(s))");
        }
        System.out.println("0. Cancelar");
        System.out.print("\nIngrese el numero del artista: ");

        ArtistaExterno artistaSeleccionado = null;
        try {
            int num = Integer.parseInt(scanner.nextLine());
            if (num > 0 && num <= artistasList.size()) {
                artistaSeleccionado = artistasList.get(num - 1);
            } else if (num == 0) {
                System.out.println("Operacion cancelada.");
                return;
            } else {
                System.err.println("Numero fuera de rango.");
                return;
            }
        } catch (NumberFormatException e) {
            System.err.println("Entrada no valida. Debe ingresar un numero.");
            return;
        }
        
        
        System.out.println("\n--- Seleccionar Contrato de " + artistaSeleccionado.getNombre() + " a eliminar ---\n");
        
        // Filtramos los contratos del artista
        List<Contrato> contratosDelArtista = new ArrayList<>();
        for (Contrato contrato : recital.getContrataciones()) {
            if (contrato.getArtista().equals(artistaSeleccionado)) {
                contratosDelArtista.add(contrato);
            }
        }
        
        // menu de contratos

        for (int i = 0; i < contratosDelArtista.size(); i++) {
            Contrato c = contratosDelArtista.get(i);
            System.out.println((i + 1) + ". Cancion: '" + c.getCancion().getTitulo() + 
                               "' (Rol: " + c.getRolAsignado() + 
                               " - Costo: $" + String.format("%.2f", c.getCostoPagado()) + ")");
        }
        
        
        System.out.println("\n-1. Eliminar todos los contratos (" + contratosDelArtista.size() + 
                           ")");
        System.out.println("0. Cancelar");
        System.out.print("\nIngrese el numero de la opcion: ");
        
        try {
            int num = Integer.parseInt(scanner.nextLine());
            if (num > 0 && num <= contratosDelArtista.size()) {
                // Eliminar uno
                Contrato contratoAQuitar = contratosDelArtista.get(num - 1);
                
                recital.quitarContrato(contratoAQuitar);
                
            } else if (num == -1) {
                // Eliminar TODOS
                recital.quitarTodosLosContratosDeArtista(artistaSeleccionado);

            } else if (num == 0) {
                System.out.println("Operacion cancelada.");
            } else {
                System.err.println("Numero fuera de rango.");
            }
        } catch (NumberFormatException e) {
            System.err.println("Entrada no valida. Debe ingresar un numero.");
        }
    }

    

}
