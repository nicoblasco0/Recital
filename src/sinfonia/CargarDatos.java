package sinfonia;

//Importaciones para Gson
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken; // Para leer listas genéricas

//Importaciones de Java
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
Clase de utilidad para cargar toda la información inicial
desde los archivos JSON y construir el objeto Recital.
 */
public class CargarDatos {

 
    /**
     * DTO que mapea la estructura de artistas.json
     */
    private static class ArtistaDTO {
        String nombre;
        List<String> roles;
        List<String> bandas;
        double costo; 
        int maxCanciones; 
    }

    /**
     * DTO que mapea la estructura de recital.json
     */
    private static class CancionDTO {
        String titulo;
        List<String> rolesRequeridos;
    }


    /**
     * Método principal de la clase. Lee todos los archivos
     * y devuelve un objeto Recital listo para usar.
     * @param rutaArtistas Ruta al archivo "artistas.json"
     * @param rutaRecital Ruta al archivo "recital.json"
     * @param rutaArtistasBase Ruta al archivo "artistas-discografica.json"
     * @return Un objeto Recital inicializado.
     * @throws IOException Si ocurre un error al leer los archivos.
     */
    public static Recital cargarRecitalDesdeArchivos(String rutaArtistas, String rutaRecital, String rutaArtistasBase) 
        throws IOException {
        
        Gson gson = new Gson();

        // Carga la lista de nombres de artistas base (artistas-discografica.json)
        Set<String> nombresBase;
        try (Reader reader = new FileReader(rutaArtistasBase)) {
            Type listType = new TypeToken<List<String>>() {}.getType();
            List<String> listaNombresBase = gson.fromJson(reader, listType);
            nombresBase = new HashSet<>(listaNombresBase);
        }
        System.out.println("Cargados " + nombresBase.size() + " artistas base.");

        // Carga todos los artistas (artistas.json) 
        List<ArtistaBase> artistasBase = new ArrayList<>();
        List<ArtistaExterno> artistasCandidatos = new ArrayList<>();
        
        try (Reader reader = new FileReader(rutaArtistas)) {
            // Leemos el JSON como un array de ArtistaDTO
            ArtistaDTO[] dtos = gson.fromJson(reader, ArtistaDTO[].class);

            // Iteramos y "convertimos" los DTOs en nuestro Modelo
            for (ArtistaDTO dto : dtos) {
                // Verificamos si el nombre está en el Set que cargamos antes
                if (nombresBase.contains(dto.nombre)) {
                    // Es un Artista Base
                    artistasBase.add(new ArtistaBase(
                        dto.nombre, 
                        dto.roles, 
                        dto.bandas
                    ));
                } else {
                    // Es un Artista Externo (candidato)
                    artistasCandidatos.add(new ArtistaExterno(
                        dto.nombre, 
                        dto.roles, 
                        dto.bandas, 
                        dto.costo, 
                        dto.maxCanciones
                    ));
                }
            }
        }
        System.out.println("Artistas base encontrados: " + artistasBase.size());
        System.out.println("Artistas candidatos encontrados: " + artistasCandidatos.size());

        // Carga las canciones (recital.json)
        List<Cancion> setlist = new ArrayList<>();
        try (Reader reader = new FileReader(rutaRecital)) {
            // Leemos el JSON como un array de CancionDTO
            CancionDTO[] dtos = gson.fromJson(reader, CancionDTO[].class);

            // Iteramos y convertimos
            for (CancionDTO dto : dtos) {
                setlist.add(new Cancion(
                    dto.titulo, 
                    dto.rolesRequeridos
                ));
            }
        }
        System.out.println("Cargadas " + setlist.size() + " canciones para el setlist.");

        // Devolver el objeto Recital
        return new Recital(setlist, artistasBase, artistasCandidatos);
    }
}