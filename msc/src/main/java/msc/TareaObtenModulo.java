package msc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.FunctionalSyntaxDocumentFormat;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import java.util.concurrent.atomic.LongAdder;

import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;

public class TareaObtenModulo implements Callable<Set<OWLAxiom>>{
	private ModuleType metodo;
	private IRI iriOntologia;
	private Set<OWLEntity> signature;
	private PrintWriter log;
	private File guardmol;
	private LongAdder tiempo;
	
	
	
	public TareaObtenModulo(ModuleType metodo, IRI iriOntologia, Set<OWLEntity> signature, PrintWriter log, File guardmol, LongAdder tiempo) {
		super();
		this.metodo = metodo;
		this.iriOntologia = iriOntologia;
		this.signature = signature;
		this.log = log;
		this.guardmol = guardmol;
		this.tiempo = tiempo;
		
	}



	@Override
	public Set<OWLAxiom> call() throws Exception {
		//System.out.println("Obteniendo modulo de " + this.iriOntologia.toQuotedString());
		System.out.println("Leyendo: " + this.iriOntologia.toQuotedString());
		this.log.println("Leyendo: " + this.iriOntologia.toQuotedString());
		OWLOntologyManager owlOntologyManager = OWLManager.createConcurrentOWLOntologyManager();
		//System.out.println("cargando " + this.iriOntologia.toQuotedString());
		OWLOntology ontologia = owlOntologyManager.loadOntology(iriOntologia);
		//System.out.println("Cargada");
		System.out.println("Leido: " + this.iriOntologia.toQuotedString());
		this.log.println("Leido: " + this.iriOntologia.toQuotedString());
		SyntacticLocalityModuleExtractor extractor = new SyntacticLocalityModuleExtractor(owlOntologyManager, ontologia,
				metodo);
		
		//System.out.println("Extrayendo modulo " + metodo.toString());
		long start = System.currentTimeMillis();
		OWLOntology modulo = extractor.extractAsOntology(signature, IRI.create(ontologia.getOntologyID().getOntologyIRI().get().toString() + "_module"));
		//System.out.println("Extraido");
		
		long end = System.currentTimeMillis();
		
		System.out.println("La estrategia " + metodo.toString() + " ha tardado en extraer los axiomas de " + this.iriOntologia.toQuotedString() 
				+ " " + ((end-start)/1000) + " segundos.");
		this.log.println("La estrategia " + metodo.toString() + " ha tardado en extraer los axiomas de " + this.iriOntologia.toQuotedString() 
				+ " " + ((end-start)/1000) + " segundos.");
		System.out.println("Se han extraido " + modulo.getAxiomCount() + " axiomas y " + modulo.getClassesInSignature().size() + " entidades"
				+ " con " + metodo.toString() + " de " + this.iriOntologia.toQuotedString());
		this.log.println("Se han extraido " + modulo.getAxiomCount() + " axiomas y " + modulo.getClassesInSignature().size() + " entidades"
				+ " con " + metodo.toString() + " de " + this.iriOntologia.toQuotedString());
		
		this.tiempo.add(end - start);
		
		owlOntologyManager.saveOntology(modulo, new FunctionalSyntaxDocumentFormat(),new FileOutputStream(this.guardmol));
		
		this.log.flush();
		
		owlOntologyManager.removeOntology(ontologia);
		
		return modulo.axioms().collect(Collectors.toSet());
	}



	public ModuleType getMetodo() {
		return metodo;
	}



	public IRI getIriOntologia() {
		return iriOntologia;
	}



	public PrintWriter getLog() {
		return log;
	}


}
