package sinfonia;

import java.util.List;

/*
 Representa a un artista que pertenece a la discográfica (costo 0).
 Hereda de Artista.
 */
public class ArtistaBase extends Artista {

    public ArtistaBase(String nombre, List<String> rolesHistoricos, List<String> bandasHistoricas) {
        super(nombre, rolesHistoricos, bandasHistoricas);
    }

    /**
     El costo de un artista base es siempre 0.
     @return 0.0
     */
    @Override
    public double getCostoContratacion() {
        return 0.0;
    }

    /**
     Un artista base no tiene límite de canciones.
     Se devuelve un valor maximo para representar "infinito".
     @return Integer.MAX_VALUE
     */
    @Override
    public int getMaxCanciones() {
        // Representa que no tienen límite
        return Integer.MAX_VALUE; 
    }
}