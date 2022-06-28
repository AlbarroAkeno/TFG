package msc;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;


@Command(
		name = "main",
		description = "Es el main"		
	)

public class Main implements Runnable{

	
	@Parameters(index = "0", description = "Esto es la URI de la ontolog�a a completar. Si deseas que sea una ruta utiliza -f o --file")
	private String entrada;
	
	@Parameters(index = "1", description = "Esto es la ruta del directorio donde guardar los resultados")
	private String guardado;
	
	@Parameters(index = "2", description = "Indica que estrategia utilizar, las opciones son Star, BOT, TOP o Todas")
	private String metodo;
	
	@Option(names = {"-f", "-F", "--file", "--File"}, description = "Introduce esta opci�n si vas a introducir la ontolog�a con la ruta de un fichero")
	private boolean fichero = true;
	
	
	public static void main(String[] args) {
		
		new CommandLine(new Main()).execute(args);
		
	}

	public void run() {
	
	// TODO Auto-generated method stub
			Principal prin = null;
			PrintWriter log;
			try {
				log = new PrintWriter(guardado+"log.txt", "UTF-8");
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				//e1.printStackTrace();
				System.err.println("No se pudo crear el fichero log ruta de guardado no encontrada");
				return;
			} catch (UnsupportedEncodingException e1) {
				// TODO Auto-generated catch block
				//e1.printStackTrace();
				System.err.println("No se pudo crear el fichero log encoding erroneo");
				return;
			}
			//File ontologia = new File("C:\\Users\\�lvaro\\Documents\\TFG\\alvaro\\doidn.owl");
			//File ontologia = new File("C:\\Users\\�lvaro\\Documents\\TFG\\alvaro\\OMIM.ttl");
			
			log.println("Iniciando ejecuci�n");
			
			if (fichero) {
				try {
					prin = new Principal(entrada+".owl", log, guardado);
				} catch (OWLOntologyCreationException e) {
					// TODO Auto-generated catch block
					System.err.println("Error al leer la ontolog�a pasada como par�metro, por favor, revise el fichero o la URI utilizada");
					log.println("Error al leer la ontolog�a pasada como par�metro, por favor, revise el fichero o la URI utilizada");
					return;
				}
			} else {
				File ontologia = new File(entrada);								
			try {
				prin = new Principal(ontologia, log, guardado);
			} catch (OWLOntologyCreationException e) {
				// TODO Auto-generated catch block
					System.err.println("Error al leer la ontolog�a pasada como par�metro, por favor, revise el fichero o la URI utilizada");
					log.println("Error al leer la ontolog�a pasada como par�metro, por favor, revise el fichero o la URI utilizada");
					return;
				}
			}
			prin.generaMapeo();
			
			PrintWriter esc = null;
			
			try {
				//esc = new PrintWriter("C:\\Users\\�lvaro\\Documents\\TFG\\alvaro\\MapeoMain.txt", "UTF-8");
				esc = new PrintWriter(guardado+"MapeoOntologia.txt", "UTF-8");
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				System.err.println("Error al generar el fichero de texto para mostrar el mapping");
				log.println("Error al generar el fichero de texto para mostrar el mapping");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				System.err.println("Error al generar el fichero de texto para mostrar el mapping");
				log.println("Error al generar el fichero de texto para mostrar el mapping");
			}
			
			
			if (esc != null) prin.imprimeMapeo(esc);
			
			String metodominus = metodo.toLowerCase();
			
			if (metodominus.equals("todas")) prin.completaOntologia();
			else {
				int sal = prin.completaOntolog�aMetodo(metodominus);
				if (sal != 0) {
					System.err.println("Ha ocurrido un error, revise el log");
					return;
				}
			}
			
			//File guardado = new File("C:\\Users\\�lvaro\\Documents\\TFG\\alvaro\\MainGuardado.owl");
			//File gur = new File(guardado+"MainGuardado.owl");
			try {
				prin.GuardaOntologiaResultado(metodominus);
			} catch (OWLOntologyStorageException e) {
				// TODO Auto-generated catch block
				System.err.println("Error al guardar la ontolog�a");
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				System.err.println("Error generando los ficheros de guardado");
			}
			
			prin.generarEstad�sticas(metodominus);
			prin.razonar(metodominus);
	}
}
