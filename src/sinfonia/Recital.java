package sinfonia;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/*
 Clase orquestadora principal.
 Contiene la lógica para gestionar el recital,
 los artistas y las contrataciones.
 */
public class Recital {

    private List<Cancion> setlist;
    private List<ArtistaBase> artistasBase;
    private List<ArtistaExterno> artistasCandidatos;
    
    // Esta lista guardará todos los "contratos" (Asignacion) que hagamos.
    private List<Contrato> contrataciones;

    /**
      Constructor para inicializar el Recital.
      @param setlist La lista de canciones a tocar.
      @param artistasBase Los artistas de la discográfica.
      @param artistasCandidatos Los artistas externos disponibles para contratar.
     */
    public Recital(List<Cancion> setlist, List<ArtistaBase> artistasBase, List<ArtistaExterno> artistasCandidatos) {
        this.setlist = setlist;
        this.artistasBase = artistasBase;
        this.artistasCandidatos = artistasCandidatos;
        
        // Inicializamos la lista de contrataciones como vacía.
        this.contrataciones = new ArrayList<>();
    }

    
    // --- GETTERS ---
    
    /**
     Devuelve la lista de candidatos externos.
     @return La lista de artistas candidatos.
     */
    public List<ArtistaExterno> getArtistasCandidatos() {
        return this.artistasCandidatos;
    }

    /**
     Devuelve la lista de canciones (setlist) del recital.
     @return La lista de objetos Cancion.
     */
    public List<Cancion> getSetlist() {
        return this.setlist;
    }
    
    /**
     Devuelve la lista de contratos realizados.
     @return La lista de objetos Contrato.
     */
    public List<Contrato> getContrataciones() {
        return this.contrataciones;
    }
    
    /**
     Calcula y devuelve el costo total de todos los artistas externos contratados.
     @return El costo total.
     */
    public double getCostoTotalContratos() {
        double costoTotal = 0.0;
        for (Contrato contrato : this.contrataciones) {
            costoTotal += contrato.getCostoPagado();
        }
        return costoTotal;
    }
    
    // --- METODOS DE CONSULTA ---

    /**
     Calcula los roles (con cantidad) que faltan para cubrir una canción específica.
     @param cancion La canción a verificar.
     @return Un Mapa donde la clave es el rol (String) y el valor la cantidad (Integer) de
     músicos que faltan para ese rol.
     */
    public Map<String, Integer> getRolesFaltantesCancion(Cancion cancion) {
        
        //Obtenemos los roles requeridos
        HashMap<String, Integer> rolesFaltantes = new HashMap<>(cancion.getConteoRolesRequeridos());

        //Restamos los roles que pueden cubrir los artistasBase.
        for (ArtistaBase artistaBase : this.artistasBase) {
            //Buscamos el primer rol que este artista pueda cubrir y que se necesite
            for (String rolQueSabeTocar : artistaBase.getRolesHistoricos()) {
                
                // Verificamos si este rol todavía se necesita
                int cantidadFaltante = rolesFaltantes.getOrDefault(rolQueSabeTocar, 0);
                
                if (cantidadFaltante > 0) {
                	// Restamos 1 a cantidadFaltante
                    rolesFaltantes.put(rolQueSabeTocar, cantidadFaltante - 1);
               
                    // pasamos al siguiente artista 
                    break; 
                }
            }
        }

        // Restamos los roles ya cubiertos por artistasExternos
        for (Contrato contrato : this.contrataciones) {
            
            // Verificamos si el contrato es para esta canción
            if (contrato.getCancion().equals(cancion)) {
                String rolAsignado = contrato.getRolAsignado();
                int cantidadFaltante = rolesFaltantes.getOrDefault(rolAsignado, 0);
                
                if (cantidadFaltante > 0) {
                    // Restamos 1 a cantidadFaltante si el contrato lo cubre
                    rolesFaltantes.put(rolAsignado, cantidadFaltante - 1);
                }
            }
        }

        // Devolvemos el mapa resultante, limpiando roles que quedaron en 0.
        Iterator<Map.Entry<String, Integer>> iterador = rolesFaltantes.entrySet().iterator();
        while (iterador.hasNext()) {
            Map.Entry<String, Integer> entrada = iterador.next();
            
            if (entrada.getValue() <= 0) {
                iterador.remove();
            }
        }
        
        return rolesFaltantes;
    }



	/**
     Calcula todos los roles (con cantidad) que faltan para cubrir el recital completo.
     @return Un Mapa consolidado de todos los roles faltantes.
     */
    public Map<String, Integer> getRolesFaltantesRecital() {

        Map<String, Integer> faltantesGlobal = new HashMap<>();

        // Iteramos por cada cancion
        for (Cancion cancion : this.setlist) {
            
            // Usamos getRolesFaltantesCancion(cancion).
            Map<String, Integer> faltantesCancion = this.getRolesFaltantesCancion(cancion);
    
            // Iteramos por cada entrada (par clave-valor) del mapa de faltantes de la canción
            for (Map.Entry<String, Integer> entrada : faltantesCancion.entrySet()) {
                String rol = entrada.getKey();
                int cantidad = entrada.getValue();

                // Obtenemos la cantidad que ya teníamos acumulada para este rol (o 0 si era la primera vez)
                int cantidadActual = faltantesGlobal.getOrDefault(rol, 0);
                
                // Guardamos la nueva suma total para ese rol
                faltantesGlobal.put(rol, cantidadActual + cantidad);
            }
        }
        
        return faltantesGlobal;
    }

    // --- METODOS DE ACCION ---

    /**
     Contrata artistas para una canción específica, optimizando por costo (Greedy).
     @param cancion La canción para la cual contratar.
     */
    public void contratarParaCancion(Cancion cancion) {

        //Usamos getRolesFaltantesCancion(cancion) que ya excluye Artistas Base y contratos previos.
        Map<String, Integer> rolesFaltantes = this.getRolesFaltantesCancion(cancion);
        
        System.out.println("\nIniciando contratación para '" + cancion.getTitulo() + "'...");
        
        // Iteramos por cada tipo de rol faltante
        for (Map.Entry<String, Integer> entrada : rolesFaltantes.entrySet()) {
            String rol = entrada.getKey();
            int cantidadARequerir = entrada.getValue();

            // Iteramos por cada rol de ese tipo requerido
            for (int i = 0; i < cantidadARequerir; i++) {
                
                System.out.println("\n\t-Buscando artista para " + rol + " (" + (i+1) + "/" + cantidadARequerir + ")...");
                
                // 3. Buscar en 'artistasCandidatos' al artista MÁS BARATO
                ArtistaExterno artistaMasBarato = null;
                double costoMinimo = Double.MAX_VALUE;

                for (ArtistaExterno candidato : this.artistasCandidatos) {
                    
                    // Si no puede tocar ese rol, continua
                    if (!candidato.puedeTocar(rol)) {
                        continue; 
                    }

                    // Si ya esta contratado para otro rol en esta cancion continua
                    if (this.estaContratadoParaCancion(candidato, cancion)) {
                    	continue;
                    }

                    // Si alcanzo su limite de 'maxCanciones' continua
                    int cancionesAsignadas = this.getCancionesAsignadas(candidato);
                    if (cancionesAsignadas >= candidato.getMaxCanciones()) {
                    	continue;
                    }
                    
                    // Calculamos su costo con descuento
                    double costoActual = candidato.getCostoContratacion();
                    
                    boolean tieneDescuento = false;
                    for (ArtistaBase artistaBase : this.artistasBase) {
                        if (candidato.compartioBanda(artistaBase)) {
                            tieneDescuento = true;
                            break;
                        }
                    }

                    if (tieneDescuento) {
                        costoActual = costoActual * 0.5;
                    }

                    // Comparamos si es el mas barato encontrado
                    if (costoActual < costoMinimo) {
                        costoMinimo = costoActual;
                        artistaMasBarato = candidato;
                    }
                } 

                // Creamos el Contrato
                if (artistaMasBarato != null) {

                    System.out.println("\t\t*Contratado: " + artistaMasBarato.getNombre() + 
                                       " para " + rol + " por $" + costoMinimo);
                                       
                    Contrato nuevoContrato = new Contrato(artistaMasBarato, cancion, rol, costoMinimo);
                    this.contrataciones.add(nuevoContrato);
                    
                } else {
                    // Si no se encuentra artista, generamos un error.
                    System.err.println("¡ERROR! No se encontraron artistas disponibles para el rol '" + 
                                       rol + "' en la canción '" + cancion.getTitulo() + "'.");
                    
                    // Lanzamos una excepción para detener la operación.
                    throw new RuntimeException("Faltan artistas para " + rol + " en " + cancion.getTitulo());
                }
                
            } 
            
        } 
        System.out.println("\nContratación finalizada para '" + cancion.getTitulo() + "'");  
    }
    
    /**
     Helper para contar en cuantas canciones esta asignado un artista.
     @param artista El artista a verificar.
     @return El número de canciones únicas.
     */
    private int getCancionesAsignadas(ArtistaExterno artista) {
        Set<Cancion> cancionesUnicas = new HashSet<>();
        
        for (Contrato contrato : this.contrataciones) {
            if (contrato.getArtista().equals(artista)) {
                cancionesUnicas.add(contrato.getCancion());
            }
        } 
        return cancionesUnicas.size();
    }
    
    /**
     Helper para verificar si un artista ya tiene un contrato
     para un rol en una canción específica.
     @param artista El artista a verificar.
     @param cancion La canción a verificar.
     @return true si ya tiene un contrato en esa canción, false si no.
     */
    private boolean estaContratadoParaCancion(ArtistaExterno artista, Cancion cancion) {
        for (Contrato contrato : this.contrataciones) {
            if (contrato.getArtista().equals(artista) && 
                contrato.getCancion().equals(cancion)) {
                return true;
            }
        }
        return false;
    }

    
    
    /**
     Contrata artistas para todas las canciones del recital.
     */
    public void contratarParaRecital() {
        int cancionesExitosas = 0;
        int cancionesFallidas = 0;
        double costoAntes = this.getCostoTotalContratos();
        
        // Iteramos por cada cancion en 'setlist'
        for (Cancion cancion : this.setlist) {
            
            //Llamamos a contratarParaCancion(cancion) para cada una
            try {
            	
                // Si no faltan roles para esta cancion continuamos
                if (this.getRolesFaltantesCancion(cancion).isEmpty()) {
                    continue;
                }
                
                // Si faltan roles, intentamos contratar
                this.contratarParaCancion(cancion);
                cancionesExitosas++;
                
            } catch (RuntimeException e) {
                // Si falla la contratación de una canción (ej. no hay artistas),
                // informamos el error y continuamos con la siguiente.
                System.err.println("Error al contratar para '" + cancion.getTitulo() + 
                                   "': " + e.getMessage());
                System.err.println("Continuando con la siguiente canción...");
                cancionesFallidas++;
            }
        }
        
        double costoDespues = this.getCostoTotalContratos();
        
        System.out.println("\n===== Contratación del recital finalizada =====");
        System.out.println("Resumen:");
        System.out.println("-" + cancionesExitosas + " canciones procesadas.");
        if (cancionesFallidas > 0) {
            System.out.println("-" + cancionesFallidas + " canciones no pudieron completarse por falta de artistas.");
        }
        System.out.println("\n-Total Gastado: $" + (costoDespues-costoAntes));
        System.out.println("===============================================");
    }


    /**
     Entrena a un artista candidato.
     @param nombreArtista El nombre del artista a entrenar.
     @param nuevoRol El rol para el que se va a entrenar.
     */
    public void entrenarArtista(String nombreArtista, String nuevoRol) {
        
        // Buscar al artistaExterno en artistasCandidatos por su nombre.
        ArtistaExterno artistaEncontrado = null;
        for (ArtistaExterno candidato : this.artistasCandidatos) {
            // Usamos .equalsIgnoreCase() que ignora mayusculas/minusculas
            if (candidato.getNombre().equalsIgnoreCase(nombreArtista)) {
                artistaEncontrado = candidato;
                break; 
            }
        }

        // Si se encuentra, llamamos a artista.entrenar(nuevoRol).
        if (artistaEncontrado != null) {

            boolean exito = artistaEncontrado.entrenar(nuevoRol);
            
            if (exito) {
                System.out.println("Entrenamiento de " + artistaEncontrado.getNombre() + " completado.");
            } else {
                System.out.println("El entrenamiento de " + artistaEncontrado.getNombre() + " no pudo completarse.");
            }
            
        } else {
            // Manejar el caso de que el artista no exista
            System.err.println("Error: No se encontró ningún artista candidato con el nombre '" + 
                               nombreArtista + "'.");
        }
    }

    
    // --- MÉTODOS DE REPORTE ---

    /**
     Imprime por consola la lista de todos los artistas externos contratados,
     el rol, la canción y el costo pagado. También muestra un total.
     */
    public void listarArtistasContratados() {
        System.out.println("\n===== Listado de Artistas Contratados =====\n");
        
        // Iteramos por 'this.contrataciones'.
        if (this.contrataciones.isEmpty()) {
            System.out.println("Aún no se ha contratado a ningún artista externo.");
            System.out.println("============================================");
            return;
        }

        double costoTotal = 0.0;
        
        // Se imprime la informacion de cada Contrato.
        for (Contrato contrato : this.contrataciones) {
            System.out.println(
                "- Artista: " + contrato.getArtista().getNombre() + 
                "\n    Canción: " + contrato.getCancion().getTitulo() + 
                "\n    Rol: " + contrato.getRolAsignado() + 
                "\n    Costo: $" + String.format("%.2f", contrato.getCostoPagado()) 
            );
            costoTotal += contrato.getCostoPagado();
        }

        System.out.println("--------------------------------------------");
        System.out.println("COSTO TOTAL (Artistas Externos): $" + String.format("%.2f", costoTotal));
        System.out.println("============================================");
    }

    /**
     Imprime por consola el estado de cada canción del setlist
     (completa o incompleta) y, si está incompleta, detalla
     los roles que aún faltan por cubrir.
     */
    public void listarEstadoCanciones() {
        System.out.println("\n===== Estado de Canciones del Recital =====\n");
        
        // Iteramos por 'this.setlist'.
        for (Cancion cancion : this.setlist) {
            
            // Por cada canción, se llama a getRolesFaltantesCancion(cancion).
            Map<String, Integer> rolesFaltantes = this.getRolesFaltantesCancion(cancion);
            
            // Si el mapa de roles faltantes está vacío, se imprime "Completa".
            if (rolesFaltantes.isEmpty()) {
                System.out.println("\n- " + cancion.getTitulo() + ": [COMPLETA]");
            } else {
                // Si no, imprimir "Incompleta" y los roles que faltan.
                System.out.println("\n- " + cancion.getTitulo() + ": [INCOMPLETA]");
                
                // Iteramos por el mapa de faltantes para detallar
                for (Map.Entry<String, Integer> entrada : rolesFaltantes.entrySet()) {
                    System.out.println("    * Falta(n): " + entrada.getValue() + " de '" + entrada.getKey() + "'");
                }
            }
        }
        System.out.println("===========================================");
    }

}