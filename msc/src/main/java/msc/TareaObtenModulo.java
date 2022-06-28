package msc;

import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;

public class TareaObtenModulo implements Callable<Set<OWLAxiom>>{
	private ModuleType metodo;
	private IRI iriOntologia;
	private Set<OWLEntity> signature;
	
	
	
	public TareaObtenModulo(ModuleType metodo, IRI iriOntologia, Set<OWLEntity> signature) {
		super();
		this.metodo = metodo;
		this.iriOntologia = iriOntologia;
		this.signature = signature;
	}



	@Override
	public Set<OWLAxiom> call() throws Exception {
		System.out.println("Obteniendo modulo de " + this.iriOntologia.toQuotedString());
		OWLOntologyManager owlOntologyManager = OWLManager.createConcurrentOWLOntologyManager();
		System.out.println("cargando " + this.iriOntologia.toQuotedString());
		OWLOntology ontologia = owlOntologyManager.loadOntology(iriOntologia);
		System.out.println("Cargada");
		SyntacticLocalityModuleExtractor extractor = new SyntacticLocalityModuleExtractor(owlOntologyManager, ontologia,
				metodo);
		
		System.out.println("Extrayendo modulo " + metodo.toString());
		OWLOntology modulo = extractor.extractAsOntology(signature, IRI.create(ontologia.getOntologyID().getOntologyIRI().get().toString() + "_module"));
		System.out.println("Extraido");
		
		return modulo.axioms().collect(Collectors.toSet());
	}


}
