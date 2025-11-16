package sinfonia;

import java.util.List;
import java.util.HashMap;

/*
 Representa una canción del recital y los roles que requiere.
 */
public class Cancion {

    private String titulo;
    private List<String> rolesRequeridos; // Puede tener roles repetidos (ej. "voz", "voz")

    public Cancion(String titulo, List<String> rolesRequeridos) {
        this.titulo = titulo;
        this.rolesRequeridos = rolesRequeridos;
    }

    /**
     Devuelve la lista de roles tal como fue cargada.
     @return Lista de strings de roles.
     */
    public List<String> getRolesRequeridos() {
        return this.rolesRequeridos;
    }
    
    /**
    Método de utilidad para contar cuántas veces se necesita cada rol.
    Ej: ["voz", "voz", "bajo"] se convierte en {"voz": 2, "bajo": 1}
    @return Un Mapa donde la clave es el rol (String) y el valor es la cantidad (Integer).
    */
   public HashMap<String, Integer> getConteoRolesRequeridos() {
	   
       HashMap<String, Integer> conteo = new HashMap<>();

       for (String rol : this.rolesRequeridos) {
           
           // Usamos getOrDefault para obtener el valor actual (o 0 si no existe)
           // y le sumamos 1.
           int cantidadActual = conteo.getOrDefault(rol, 0);
           conteo.put(rol, cantidadActual + 1);
           
       }

       return conteo;
   }
   
    public String getTitulo() {
        return titulo;
    }
}