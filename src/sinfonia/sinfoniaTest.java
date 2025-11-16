package sinfonia;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
* Clase de pruebas para Recital y la lógica de negocio asociada.
*/
public class sinfoniaTest {

 // --- Objetos de Prueba ---
 private Recital recital;
 private ArtistaBase brianMay;
 private ArtistaBase johnDeacon;
 
 private ArtistaExterno eltonJohn;
 private ArtistaExterno georgeMichael;
 private ArtistaExterno davidBowie;
 private ArtistaExterno annieLennox;

 private Cancion somebodyToLove;
 private Cancion underPressure;

 /**
  * Configura un escenario de prueba complejo antes de CADA test.
  */
 @BeforeEach
 public void setUp() {
     // --- Artistas Base ---
     brianMay = new ArtistaBase("Brian May", 
         Arrays.asList("guitarra eléctrica", "voz secundaria"), 
         Arrays.asList("Queen"));
     
     johnDeacon = new ArtistaBase("John Deacon", 
         Arrays.asList("bajo"), 
         Arrays.asList("Queen"));

     List<ArtistaBase> artistasBase = Arrays.asList(brianMay, johnDeacon);

     // --- Artistas Externos (Candidatos) ---
     eltonJohn = new ArtistaExterno("Elton John", 
         Arrays.asList("voz principal", "piano"), 
         Arrays.asList("Elton John Band"), 
         1000.0, 2);
         
     georgeMichael = new ArtistaExterno("George Michael", 
         Arrays.asList("voz principal"), 
         Arrays.asList("Wham!"), 
         800.0, 2); // Más barato que Elton para "voz"

     davidBowie = new ArtistaExterno("David Bowie", 
         Arrays.asList("voz principal"), 
         Arrays.asList("Tin Machine", "Queen"), // Comparte "Queen" con los base
         1500.0, 2); // Caro, pero tendrá 50% descuento (costo final 750)

     annieLennox = new ArtistaExterno("Annie Lennox", 
         Arrays.asList("voz principal"), 
         Arrays.asList("Eurythmics"), 
         100.0, 1); // Muy barata, pero max 1 canción

     List<ArtistaExterno> artistasCandidatos = new ArrayList<>(
         Arrays.asList(eltonJohn, georgeMichael, davidBowie, annieLennox)
     );

     // --- Canciones ---
     // Requiere: 1 voz, 1 guitarra, 1 bajo, 1 piano
     somebodyToLove = new Cancion("Somebody to Love", 
         Arrays.asList("voz principal", "guitarra eléctrica", "bajo", "piano"));

     // Requiere: 2 voces (para probar cantidad), 1 bajo
     underPressure = new Cancion("Under Pressure", 
         Arrays.asList("voz principal", "voz principal", "bajo"));

     List<Cancion> setlist = Arrays.asList(somebodyToLove, underPressure);

     // --- Recital ---
     recital = new Recital(setlist, artistasBase, artistasCandidatos);
 }

 // --- Tests de ArtistaExterno ---

 @Test
 public void testEntrenamientoArtistaExterno() {
     // Elton cuesta 1000
     assertEquals(1000.0, eltonJohn.getCostoContratacion());
     assertFalse(eltonJohn.puedeTocar("batería"));

     // Primer entrenamiento (costo * 1.5 = 1500)
     boolean exito1 = eltonJohn.entrenar("batería");
     assertTrue(exito1);
     assertEquals(1500.0, eltonJohn.getCostoContratacion());
     assertTrue(eltonJohn.puedeTocar("batería"));

     // Segundo entrenamiento (costo * 1.5 = 2250)
     boolean exito2 = eltonJohn.entrenar("flauta");
     assertTrue(exito2);
     assertEquals(2250.0, eltonJohn.getCostoContratacion());
     
     // No se puede entrenar si ya está contratado
     eltonJohn.setYaContratado(); // Simula una contratación
     boolean exito3 = eltonJohn.entrenar("trompeta");
     assertFalse(exito3);
     assertEquals(2250.0, eltonJohn.getCostoContratacion()); // El costo no cambia
 }

 // --- Tests de Recital (Lógica de Consulta) ---

 @Test
 public void testGetRolesFaltantesCancionConBase() {
     // somebodyToLove requiere: {"voz principal": 1, "guitarra eléctrica": 1, "bajo": 1, "piano": 1}
     // Artistas Base cubren:
     // - Brian May -> "guitarra eléctrica"
     // - John Deacon -> "bajo"
     // Faltan: {"voz principal": 1, "piano": 1}

     Map<String, Integer> faltantes = recital.getRolesFaltantesCancion(somebodyToLove);

     assertNotNull(faltantes);
     assertEquals(2, faltantes.size()); // Solo deben quedar 2 roles
     assertEquals(1, faltantes.get("voz principal"));
     assertEquals(1, faltantes.get("piano"));
     assertFalse(faltantes.containsKey("guitarra eléctrica")); // Cubierto por base
     assertFalse(faltantes.containsKey("bajo")); // Cubierto por base
 }

 @Test
 public void testGetRolesFaltantesRecital() {
     // Canción 1 (Somebody to Love) faltan: {"voz principal": 1, "piano": 1} (visto en test anterior)
     // Canción 2 (Under Pressure) requiere: {"voz principal": 2, "bajo": 1}
     //  - Base cubren: John Deacon -> "bajo"
     //  - Faltan: {"voz principal": 2}
     
     // Total Recital Faltante:
     // Voz: 1 (Song1) + 2 (Song2) = 3
     // Piano: 1 (Song1) + 0 (Song2) = 1
     // Total: {"voz principal": 3, "piano": 1}

     Map<String, Integer> faltantes = recital.getRolesFaltantesRecital();

     assertNotNull(faltantes);
     assertEquals(2, faltantes.size());
     assertEquals(3, faltantes.get("voz principal"));
     assertEquals(1, faltantes.get("piano"));
 }

 // --- Tests de Recital (Lógica de Contratación) ---

 @Test
 public void testContratarParaCancionEligeMasBaratoConDescuento() {
     // somebodyToLove faltan: {"voz principal": 1, "piano": 1}
     
     // Para "voz principal":
     // - Elton: 1000
     // - George: 800
     // - Bowie: 1500 * 0.5 (descuento Queen) = 750 <-- DEBE ELEGIR A BOWIE
     // - Annie: 100
     // - CORRECCIÓN: Annie es la más barata (100).

     // Para "piano":
     // - Elton: 1000 <-- DEBE ELEGIR A ELTON

     // Contratamos para la canción 1
     recital.contratarParaCancion(somebodyToLove);
     
     // Verificamos que la canción esté completa
     Map<String, Integer> faltantes = recital.getRolesFaltantesCancion(somebodyToLove);
     assertTrue(faltantes.isEmpty(), "La canción debería estar completa después de contratar");
     
     // Verificamos que Annie (la más barata) fue contratada para la voz
     // (No podemos verlo directo, pero sí podemos ver si está contratada)
     assertTrue(annieLennox.YaContratado());
 }

 @Test
 public void testContratarFallaSiNoHayArtistasDisponibles() {
     // Creamos una canción imposible de tocar
     Cancion cancionImposible = new Cancion("Solo de Flauta", Arrays.asList("flauta"));
     
     // Añadimos la canción al setlist (solo para este test)
     // (Mejor crear un recital nuevo y simple)
     Recital recitalSimple = new Recital(Arrays.asList(cancionImposible), new ArrayList<>(), new ArrayList<>());
     
     // Debe lanzar una RuntimeException porque no hay artistas para "flauta"
     assertThrows(RuntimeException.class, () -> {
         recitalSimple.contratarParaCancion(cancionImposible);
     });
 }

 @Test
 public void testContratarRespetaMaxCanciones() {
     // Annie Lennox tiene maxCanciones = 1
     // somebodyToLove falta {"voz": 1, "piano": 1}
     // underPressure falta {"voz": 2}
     
     // Annie es la más barata para "voz".
     // 1. Contratamos para "Somebody to Love"
     recital.contratarParaCancion(somebodyToLove);
     
     // Annie debería haber sido contratada para "voz" en somebodyToLove
     // y ahora ya no puede ser contratada para "Under Pressure"
     assertTrue(annieLennox.YaContratado());

     // 2. Ahora contratamos para "Under Pressure", que necesita 2 voces.
     // Annie ya no puede ser elegida, aunque sea la más barata.
     // Los siguientes más baratos son Bowie (750) y George (800).
     
     recital.contratarParaCancion(underPressure);
     
     // Verificamos que "Under Pressure" esté completa (con Bowie y George)
     Map<String, Integer> faltantes = recital.getRolesFaltantesCancion(underPressure);
     assertTrue(faltantes.isEmpty(), "Under Pressure debería estar completa");
 }

 @Test
 public void testEntrenarArtistaEnRecital() {
     // Verificamos el costo original de Elton (1000)
     // (Necesitamos encontrarlo en la lista de candidatos)
     ArtistaExterno elton = null;
     for (ArtistaExterno a : recital.getArtistasCandidatos()) {
         if (a.getNombre().equals("Elton John")) {
             elton = a;
             break;
         }
     }
     assertNotNull(elton);
     assertEquals(1000.0, elton.getCostoContratacion());

     // Entrenamos a Elton vía el Recital
     recital.entrenarArtista("Elton John", "batería");

     // Verificamos que el costo se actualizó DENTRO del recital
     assertEquals(1500.0, elton.getCostoContratacion());
     assertTrue(elton.puedeTocar("batería"));
 }
}