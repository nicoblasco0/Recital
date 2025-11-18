package sinfonia;

import org.jpl7.Atom;
import org.jpl7.Compound;
import org.jpl7.Query;
import org.jpl7.Term;
import org.jpl7.Variable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.net.URL;
import java.nio.file.Paths;

public class IntegracionProlog {

    public IntegracionProlog() {
        try {
        	
            // Busqueda de archivo .pl
            URL resource = IntegracionProlog.class
                    .getClassLoader()
                    .getResource("entrenamientos.pl");


            if (resource == null) {
                throw new IllegalStateException("No se encontró se encontró el archivo .pl");
            }
            
            //System.out.println(System.getProperty("java.class.path"));

            // Normalizacion del PATH
            String path = Paths.get(resource.toURI())
                    .toString()
                    .replace("\\", "/");

            // Consulta
            Query q = new Query(
                    "consult",
                    new Term[]{ new Atom(path) }
            );

            boolean ok = q.hasSolution();

            if (!ok) {
                throw new IllegalStateException("No se pudo consultar el archivo .pl en: " + path);
            }

        } catch (Exception e) {
            throw new RuntimeException("Error abriendo prolog", e);
        }
    }

    private String normalizarRol(String rol) {		//Es necesario normalizar para evitar errores en la ejecución del script de Prolog debido a caracteres inválidos.
        if (rol == null) return "";
        return rol.toLowerCase()
                .replace(" ", "_")
                .replace("á", "a")
                .replace("é", "e")
                .replace("í", "i")
                .replace("ó", "o")
                .replace("ú", "u");
    }

    private void limpiarHechosPrevios() {
        // Limpiamos los hechos previos
        new Query("retractall(requiere(_, _))").hasSolution();
        new Query("retractall(tiene_base(_, _))").hasSolution();
    }

    public int entrenamientosMinimos(List<Cancion> canciones, List<ArtistaBase> artistasBase) {

        limpiarHechosPrevios();

        
        Map<String, Integer> maxXrol = new HashMap<>();

        for (Cancion c : canciones) {
            for (String r : c.getRolesRequeridos()) {
                String rolNorm = normalizarRol(r);
                maxXrol.merge(rolNorm, 1, Math::max);
            }
        }

        
        Map<String, Integer> artDiscograficaXRol = new HashMap<>();

        for (ArtistaBase a : artistasBase) {
            for (String rol : a.getRolesHistoricos()) {
                String rolNorm = normalizarRol(rol);
                artDiscograficaXRol.merge(rolNorm, 1, Integer::sum);
            }
        }

        for (Map.Entry<String, Integer> e : maxXrol.entrySet()) {
            String rol = e.getKey();
            int cantMax = e.getValue();

            Term rolAtom = new Atom(rol);
            Term cantTerm = new org.jpl7.Integer(cantMax);
            Term fact = new Compound("requiere", new Term[]{rolAtom, cantTerm});

            Query assertQ = new Query("assertz", new Term[]{fact});
            assertQ.hasSolution();
        }

        for (Map.Entry<String, Integer> e : artDiscograficaXRol.entrySet()) {
            String rol = e.getKey();
            int cantBase = e.getValue();

            Term rolAtom = new Atom(rol);
            Term cantTerm = new org.jpl7.Integer(cantBase);
            Term fact = new Compound("tiene_base", new Term[]{rolAtom, cantTerm});

            Query assertQ = new Query("assertz", new Term[]{fact});
            assertQ.hasSolution();
        }

        Variable N = new Variable("N");
        Query q = new Query(
                "min_entrenamientos",
                new Term[]{N}
        );

        Map<String, Term> solution = q.oneSolution();
        if (solution == null) {
            throw new RuntimeException("Sin solución para min_entrenamientos(N).");
        }

        Term nTerm = solution.get("N");
        if (!(nTerm instanceof org.jpl7.Integer)) {
            throw new RuntimeException("N no es un entero Prolog: " + nTerm);
        }
        return ((org.jpl7.Integer) nTerm).intValue();
    }

    public double costoTotalEntrenamientos(List<Cancion> canciones, List<ArtistaBase> artistasBase, double costoUnitario) {
        int n = entrenamientosMinimos(canciones, artistasBase);
        return n * costoUnitario;
    }
}
