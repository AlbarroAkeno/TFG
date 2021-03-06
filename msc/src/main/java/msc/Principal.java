package msc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

import org.semanticweb.HermiT.Configuration;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.FunctionalSyntaxDocumentFormat;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.parameters.OntologyCopy;

import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;



public class Principal {

	private OWLOntologyManager man;
	private OWLOntology Original;
	private OWLOntology resultadoCompleto;
	private OWLOntologyManager manstar;
	private OWLOntologyManager mantop;
	private OWLOntologyManager manbot;
	private OWLOntology top;
	private OWLOntology bot;
	private Set<? extends OWLEntity> conjuntoOriginal;
	private Set<OWLAxiom> introducidosStar;
	private Set<OWLAxiom> introducidosBot;
	private Set<OWLAxiom> introducidosTop;

	private TreeMap<String, HashSet<OWLEntity>> mapeo;
	private TreeMap<String, Integer> axiomas;
	private int todos;
	private LongAdder tiempostar;
	private LongAdder tiempobot;
	private LongAdder tiempotop;
	private String rutaGuardado;

	PrintWriter log = null;
	private int tiempo;
	
	
	public Principal (String URI, PrintWriter p, String r, int tiempo) throws OWLOntologyCreationException {
		
		this.man = OWLManager.createOWLOntologyManager();
		this.manstar = OWLManager.createOWLOntologyManager();
		IRI ont = IRI.create(URI);
		
		this.Original = man.loadOntology(ont);
		this.log = p;
		this.rutaGuardado = r;
		this.tiempo = tiempo;

		inicializa();
			
	}
	
	public Principal (File ont, PrintWriter p,String r, int tiempo) throws OWLOntologyCreationException {
		
		this.man = OWLManager.createOWLOntologyManager();
		this.manstar = OWLManager.createOWLOntologyManager();
		
		this.Original = man.loadOntologyFromOntologyDocument(ont);
		this.log = p;
		this.rutaGuardado = r;
		this.tiempo = tiempo;
		
		inicializa();

	}
	
	
	private void inicializa() throws OWLOntologyCreationException {
		
		this.manstar = OWLManager.createOWLOntologyManager();
		this.manbot = OWLManager.createOWLOntologyManager();
		this.mantop = OWLManager.createOWLOntologyManager();
		this.conjuntoOriginal = this.Original.getClassesInSignature();
		this.resultadoCompleto = manstar.copyOntology(Original, OntologyCopy.DEEP);
		this.bot = manbot.copyOntology(Original, OntologyCopy.DEEP);
		this.top = mantop.copyOntology(Original, OntologyCopy.DEEP);
		this.introducidosStar = new HashSet<OWLAxiom>();
		this.introducidosBot =  new HashSet<OWLAxiom>();
		this.introducidosTop =  new HashSet<OWLAxiom>();
		this.axiomas = new TreeMap<String, Integer>();
		this.todos = 0;
		this.tiempostar = new LongAdder();
		this.tiempobot = new LongAdder();
		this.tiempotop = new LongAdder();
			
	}
	
	private String trataURI (OWLEntity URImal) {
		
		String URIbien = null;
		
		String sin = URImal.toString().replaceAll("<|>", "");
		
		String [] temp = sin.split("/");
		
		int tamano = temp.length;
		
		String ultima = temp[tamano-1];
		
		if (ultima.contains("_")) {
			String[] caso1 = ultima.split("_");
			URIbien = "";
			for (int i = 0; i < tamano-1; i++) {
				URIbien = URIbien + temp[i];
				URIbien = URIbien + "/";
			}
			for (int k = 0; k < caso1.length-1; k++) {
				if (k > 0) URIbien = URIbien + "_";
				URIbien = URIbien + caso1[k];
			}
		} else if (ultima.contains("#")) {
			String [] caso2 = sin.split("#");
			URIbien = caso2[0];
		} else {
			URIbien = "";
			for (int i = 0; i < tamano-1; i++) {
				URIbien = URIbien + temp[i];
				if (i < tamano-2) URIbien = URIbien + "/";
			}
		}
		//System.out.println(URIbien);
		return URIbien;
	}

		
	public void generaMapeo() {
		
		TreeMap<String, HashSet<OWLEntity>> mapeado = new TreeMap<String, HashSet<OWLEntity>>();
		
		Set<? extends OWLEntity> signature = this.Original.getClassesInSignature();
		
		System.out.println(this.Original.getOntologyID());
		
		for (OWLEntity e : signature) {

			String URIbien = trataURI(e);

			if (URIbien != null) {
			
				HashSet<OWLEntity> entrada = mapeado.get(URIbien);

				if (entrada != null) {
					entrada.add(e);
					mapeado.replace(URIbien, entrada);
				} else {
					HashSet<OWLEntity> nuevo = new HashSet<OWLEntity>();
					nuevo.add(e);
					mapeado.put(URIbien, nuevo);
					this.axiomas.put(URIbien, 0);		//Genera el mapa por Ontologias que luego usaremos para saber cuantos axiomas tenia
				}
			}
		}
		
		this.mapeo = mapeado;
		this.log.println("MapeoGenerado");
		return;
	}
	

	public TreeMap<String, HashSet<OWLEntity>> getMapeo() {
		return mapeo;
	}
	

	public OWLOntology getResultadoCompleto() {
		return resultadoCompleto;
	}

	public void imprimeMapeo (PrintWriter p) {
		for (Entry<String, HashSet<OWLEntity>> e : this.mapeo.entrySet()) {
			p.println(e.getKey());
			for (OWLEntity s : e.getValue()) {
				p.println(s);
				p.flush();
			}			
		}
	}
	
	public int completaOntologiaMetodo(String metodo) {
		ModuleType m = null;
		LongAdder tiempo = null;
		Set<OWLAxiom> conjunto = null;
		
		//this.metod = metodo;
		if (metodo.equals("star")) {
			m = ModuleType.STAR;
			tiempo = this.tiempostar;
			conjunto = introducidosStar;
		}
		else if (metodo.equals("bot")) {
			m = ModuleType.BOT;
			tiempo = this.tiempobot;
			conjunto = introducidosBot;
		}
		else if (metodo.equals("top")) {
			m = ModuleType.TOP;
			tiempo = this.tiempotop;
			conjunto = introducidosTop;
		}
		else {
			System.err.println("Metodo introducido no valido. Por favor introduzca Star, Bot, Top");
			log.println("Metodo introducido no valido: " + metodo);
			return 1;
		}
		
		ExecutorService executor = Executors.newFixedThreadPool(1);
		Map<IRI, Set<OWLAxiom>> axiomasPorOntologia;
		Map<IRI, Future<Set<OWLAxiom>>> futureAxiomasPorOntologia = new HashMap<>();
		for (Entry<String, HashSet<OWLEntity>> e : this.mapeo.entrySet()) {
			if (!this.Original.getOntologyID().toString().contains(e.getKey().toLowerCase()) && e.getKey() != null && !e.getKey().equals("")) {
				String URIReserva = e.getKey() + ".owl";
				String URIbien = URIReserva.toLowerCase();
				IRI ontologia = IRI.create(URIbien);
				File guardmol = new File(this.rutaGuardado+metodo+ e.getKey().replaceAll("<|>|:|\\.|\\/", "")+".owl");
				TareaObtenModulo tarea = new TareaObtenModulo(m, ontologia, e.getValue(),log, guardmol, tiempo);
				Future<Set<OWLAxiom>> result = executor.submit(new JobExecutor(tarea, this.tiempo));
				futureAxiomasPorOntologia.put(ontologia, result);
			}
		}
		
		try {
			executor.shutdown();
			executor.awaitTermination(24, TimeUnit.HOURS);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
			return 1;
		}
		
		axiomasPorOntologia = this.getAxiomasPorOntologia(futureAxiomasPorOntologia);
		for (IRI ontologiaUsada : axiomasPorOntologia.keySet()) {
			Set<OWLAxiom> axiomasModulo = axiomasPorOntologia.get(ontologiaUsada);
			if (axiomasModulo != null) {
				if (metodo.equals("star")) this.resultadoCompleto.add(axiomasModulo);
				else if (metodo.equals("bot")) this.bot.add(axiomasModulo);
				else if (metodo.equals("top")) this.top.add(axiomasModulo);
				conjunto.addAll(axiomasModulo);
				
			} else {
				System.out.println("No pudo extraerse el modulo para " + ontologiaUsada.toQuotedString());
				this.log.println("No pudo extraerse el modulo para " + ontologiaUsada.toQuotedString());
			}
		}
		
		
		return 0;
	}
	private Map<IRI, Set<OWLAxiom>> getAxiomasPorOntologia (Map<IRI, Future<Set<OWLAxiom>>> futureAxiomasPorOntologia) {
		Map<IRI, Set<OWLAxiom>> axiomasPorOntologia = new HashMap<>();
		if (futureAxiomasPorOntologia != null) {
			for (IRI ont : futureAxiomasPorOntologia.keySet()) {
				try {
					axiomasPorOntologia.put(ont, futureAxiomasPorOntologia.get(ont).get());
				} catch (Exception e) {
					//e.printStackTrace();
					System.out.println("Error al leer la ontolog?a " + ont);
					this.log.println("Error al leer la ontolog?a " + ont);
					axiomasPorOntologia.put(ont, null);
				}
			}
			
		}
		return axiomasPorOntologia;
		
	}
	
	
	public void completaOntologia () {
				
		completaOntologiaMetodo("star");
		completaOntologiaMetodo("bot");
		completaOntologiaMetodo("top");
				
	}
	
	public void GuardaOntologiaResultado (String metod) throws OWLOntologyStorageException, FileNotFoundException {
		
		//File guardado
		
		File guard =  new File(this.rutaGuardado+"OntologiaResultado"+".owl");

		File guardstar = new File(this.rutaGuardado+"ResultadoStar"+".owl");

		File guardBOT = new File(this.rutaGuardado+"ResultadoBot"+".owl");
		
		File guardTOP = new File(this.rutaGuardado+"ResultadoTop"+".owl");
		
		if (!metod.equals("todas")) man.saveOntology(this.resultadoCompleto,new FunctionalSyntaxDocumentFormat(),new FileOutputStream(guard));
		else {
			man.saveOntology(this.resultadoCompleto,new FunctionalSyntaxDocumentFormat(),new FileOutputStream(guardstar));
			mantop.saveOntology(this.top,new FunctionalSyntaxDocumentFormat(),new FileOutputStream(guardTOP));
			manbot.saveOntology(this.bot,new FunctionalSyntaxDocumentFormat(),new FileOutputStream(guardBOT));
		}
	}
	
	public void generarEstadisticas (String metodo) {
		
		long tiempoTotal = 0;
		int introducidosTotal = 0;
		
		if (!metodo.equals("todas")) {
			
			if (metodo.equals("star")) {
				tiempoTotal = this.tiempostar.longValue()/1000;
				introducidosTotal = this.introducidosStar.size();
			} else if (metodo.equals("bot")) {
				tiempoTotal = this.tiempobot.longValue()/1000;
				introducidosTotal = this.introducidosBot.size();
			} else if (metodo.equals("top")) {
				tiempoTotal = this.tiempobot.longValue()/1000;
				introducidosTotal = this.introducidosBot.size();
			}
			
			System.out.println("Para completar la ontologia la estrategia " + metodo + " ha introducido " + introducidosTotal + " axiomas.");
			this.log.println("Para completar la ontologia la estrategia " + metodo + " ha introducido " + introducidosTotal + " axiomas.");
			System.out.println("La estrategia " + metodo + " ha tenido un tiempo acumulado de: " + tiempoTotal + " segundos.");
			this.log.println("La estrategia " + metodo + " ha tenido un tiempo acumulado de: " + tiempoTotal + " segundos.");
			this.log.flush();
			
		} else {

			System.out.println("Para completa la ontologia la estrategia star ha introducido " + this.introducidosStar.size() + " axiomas.");
			this.log.println("Para completa la ontologia la estrategia star  ha introducido " + this.introducidosStar.size() + " axiomas.");
			System.out.println("La estrategia star ha tenido un tiempo acumulado de: " + this.tiempostar.longValue()/1000 + " segundos.");
			this.log.println("La estrategia star ha tenido un tiempo acumulado de: " + this.tiempostar.longValue()/1000 + " segundos.");
			
			System.out.println("Para completa la ontologia la estrategia BOT ha introducido " + this.introducidosBot.size() + " axiomas.");
			this.log.println("Para completa la ontologia la estrategia BOT ha introducido " + this.introducidosBot.size() + " axiomas.");
			System.out.println("La estrategia bot ha tenido un tiempo acumulado de:" + this.tiempobot.longValue()/1000 + " segundos.");
			this.log.println("La estrategia bot ha tenido un tiempo acumulado de:" + this.tiempobot.longValue()/1000 + " segundos.");
			
			System.out.println("Para completa la ontologia la estrategia TOP ha introducido " + this.introducidosTop.size() + " axiomas.");
			this.log.println("Para completa la ontologia la estrategia TOP ha introducido " + this.introducidosTop.size() + " axiomas.");
			System.out.println("La estrategia top ha tenido un tiempo acumulado de:" + this.tiempotop.longValue()/1000 + " segundos.");
			this.log.println("La estrategia top ha tenido un tiempo acumulado de:" + this.tiempotop.longValue()/1000 + " segundos.");
			
			this.log.flush();
		}
	}
	
	public void razonar(String metodo) {
		
		this.log.flush();
		
		if (!metodo.equals("todas")) {
			System.out.println("Generando Razonador " + metodo);
			this.log.println("Generando Razonador " + metodo);
			
			File guard =  new File(this.rutaGuardado+"OntologiaResultado"+".owl");
			
			OWLOntologyManager razonar = OWLManager.createOWLOntologyManager();
			OWLOntology o = null;
			
			try {
				o = razonar.loadOntologyFromOntologyDocument(guard);
			} catch (OWLOntologyCreationException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
				System.out.println("Error al leer la ontolog?a para razonar.");
				this.log.println("Error al leer la ontolog?a para razonar.");
			}
			
			Configuration conf = new Configuration();
			//Reasoner razonador = new Reasoner(conf, this.resultadoCompleto);
			Reasoner razonador = new Reasoner(conf, o);
		
			System.out.println("Cargando razonador");
			this.log.println("Cargando razonador");
			razonador.precomputeInferences();
			if (razonador.isConsistent()) {
				System.out.println("La ontolog?a generada con "+metodo+" no tiene ninguna inconguencia y por tanto se ha completado correctamente");
				this.log.println("La ontolog?a generada con "+metodo+" no tiene ninguna inconguencia y por tanto se ha completado correctamente");
			} else {
				System.out.println("La ontolog?a generada con "+metodo+" tiene incongruencias y por tanto no se ha completado correctamente");
				this.log.println("La ontolog?a generada con "+metodo+" tiene incongruencias y por tanto no se ha completado correctamente");
			}
		} else {
			
			File guardstar = new File(this.rutaGuardado+"ResultadoStar"+".owl");

			File guardBOT = new File(this.rutaGuardado+"ResultadoBot"+".owl");
			
			File guardTOP = new File(this.rutaGuardado+"ResultadoTop"+".owl");
			
			System.out.println("Generando Razonador Star");
			this.log.println("Generando Razonador Star");
			
			OWLOntologyManager razonarstar = OWLManager.createOWLOntologyManager();
			OWLOntology ostar = null;
			
			try {
				ostar = razonarstar.loadOntologyFromOntologyDocument(guardstar);
			} catch (OWLOntologyCreationException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
				System.out.println("Error al leer la ontolog?a de Star para razonar.");
				this.log.println("Error al leer la ontolog?a de Star para razonar.");
			}
			
			Configuration confstar = new Configuration();
			Reasoner razonadorstar = new Reasoner(confstar, ostar);
			
			System.out.println("Cargando razonador Star");
			this.log.println("Cargando razonador Star");
			razonadorstar.precomputeInferences();
			if (razonadorstar.isConsistent()) {
				System.out.println("La ontolog?a generada con star no tiene ninguna inconguencia y por tanto se ha completado correctamente");
				this.log.println("La ontolog?a generada con star no tiene ninguna inconguencia y por tanto se ha completado correctamente");
			} else {
				System.out.println("La ontolog?a generada con star tiene incongruencias y por tanto no se ha completado correctamente");
				this.log.println("La ontolog?a generada con star tiene incongruencias y por tanto no se ha completado correctamente");
			}
			
			System.out.println("Generando Razonador Bot");
			this.log.println("Generando Razonador Bot");
			
			OWLOntologyManager razonarbot = OWLManager.createOWLOntologyManager();
			OWLOntology obot = null;
			
			try {
				obot = razonarbot.loadOntologyFromOntologyDocument(guardBOT);
			} catch (OWLOntologyCreationException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
				System.out.println("Error al leer la ontolog?a de BOT para razonar.");
				this.log.println("Error al leer la ontolog?a de BOT para razonar.");
			}
			
			Configuration confbot = new Configuration();
			Reasoner razonadorbot = new Reasoner(confbot, obot);
			
			System.out.println("Cargando razonador Bot");
			this.log.println("Cargando razonador Bot");
			razonadorbot.precomputeInferences();
			
			if (razonadorbot.isConsistent()) {
				System.out.println("La ontolog?a generada con bot no tiene ninguna inconguencia y por tanto se ha completado correctamente");
				this.log.println("La ontolog?a generada con bot no tiene ninguna inconguencia y por tanto se ha completado correctamente");
			} else {
				System.out.println("La ontolog?a generada con bot tiene incongruencias y por tanto no se ha completado correctamente");
				this.log.println("La ontolog?a generada con bot tiene incongruencias y por tanto no se ha completado correctamente");
			}
			
			System.out.println("Generando Razonador Top");
			this.log.println("Generando Razonador Top");
			
			OWLOntologyManager razonartop = OWLManager.createOWLOntologyManager();
			OWLOntology otop = null;
			
			try {
				otop = razonartop.loadOntologyFromOntologyDocument(guardTOP);
			} catch (OWLOntologyCreationException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
				System.out.println("Error al leer la ontolog?a de TOP para razonar.");
				this.log.println("Error al leer la ontolog?a de TOP para razonar.");
			}
			
			Configuration conftop = new Configuration();
			Reasoner razonadortop = new Reasoner(conftop, otop);
			
			System.out.println("Cargando razonador Top");
			this.log.println("Cargando razonador Top");
			razonadortop.precomputeInferences();
			
			if (razonadortop.isConsistent()) {
				System.out.println("La ontolog?a generada con Top no tiene ninguna inconguencia y por tanto se ha completado correctamente");
				this.log.println("La ontolog?a generada con Top no tiene ninguna inconguencia y por tanto se ha completado correctamente");
			} else {
				System.out.println("La ontolog?a generada con Top tiene incongruencias y por tanto no se ha completado correctamente");
				this.log.println("La ontolog?a generada con Top tiene incongruencias y por tanto no se ha completado correctamente");
			}
			
		}
	}
}


