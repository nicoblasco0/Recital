package sinfonia;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.HashSet;

/*
 Clase abstracta que representa a un artista.
 Define las propiedades y comportamientos comunes a todos los artistas.
 */
public abstract class Artista {

    protected String nombre;
    protected Set<String> rolesHistoricos; 
    protected Set<String> bandasHistoricas;
    
    // El constructor recibe una lista de rolesHistoricos y una de bandasHistoricas
    // y las aplica a un HashSet
    public Artista(String nombre, List<String> rolesHistoricos, List<String> bandasHistoricas) {
        this.nombre = nombre;
        this.rolesHistoricos = new HashSet<>(rolesHistoricos);
        this.bandasHistoricas = new HashSet<>(bandasHistoricas);
    }

    /**
     Verifica si el artista puede desempeñar un rol específico.
     @param rol El rol a verificar (ej. "guitarra eléctrica").
     @return true si el artista tiene el rol en su historial, false en caso contrario.
     */
    public boolean puedeTocar(String rol) {
        return this.rolesHistoricos.contains(rol);
    }

    /**
     Verifica si este artista compartió banda con un artista base.
     @param artistaBase El artista base con quien comparar el historial.
     @return true si comparten al menos una banda, false en caso contrario.
     */
    public boolean compartioBanda(ArtistaBase artistaBase) {
        for (String banda : this.bandasHistoricas) {
            if (artistaBase.getBandasHistoricas().contains(banda)) {
                return true;
            }
        }
        return false;
    }
    
    // --- Métodos Abstractos (a ser implementados por las subclases) ---

    /**
     Obtiene el costo de contratación del artista para una canción.
     @return El costo (0 para ArtistaBase, variable para ArtistaExterno).
     */
    public abstract double getCostoContratacion();

    /**
     Obtiene la cantidad máxima de canciones que el artista puede tocar.
     @return Límite de canciones (muy alto para Base, específico para Externo).
     */
    public abstract int getMaxCanciones();

    
    // --- Getters ---
    
    public String getNombre() {
        return nombre;
    }

    public Set<String> getRolesHistoricos() {
        return rolesHistoricos;
    }

    public Set<String> getBandasHistoricas() {
        return bandasHistoricas;
    }
    
    // --- equals y hashCode ---
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass().getSuperclass() != o.getClass().getSuperclass()) return false;
        Artista artista = (Artista) o;
        return Objects.equals(nombre, artista.nombre);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nombre);
    }
}