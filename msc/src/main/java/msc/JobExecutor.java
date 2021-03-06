package msc;

import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.semanticweb.owlapi.model.OWLAxiom;

public class JobExecutor implements Callable<Set<OWLAxiom>>{
	private TareaObtenModulo tarea;
	private int tiempo;
	
	public JobExecutor(TareaObtenModulo tarea, int tiempo) {
		super();
		this.tarea = tarea;
		this.tiempo = tiempo;
	}

	@Override
	public Set<OWLAxiom> call()  {
		Set<OWLAxiom> axioms = null;
		ExecutorService exec = Executors.newSingleThreadExecutor();
		try {
			axioms = exec.submit(this.tarea).get(this.tiempo, TimeUnit.HOURS);
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			//e.printStackTrace();
			System.out.println("Tiempo excedido extrayendo " + this.tarea.getIriOntologia() + " con la estrategia " + this.tarea.getMetodo() + e.getMessage());
			this.tarea.getLog().println("Tiempo excedido extrayendo " + this.tarea.getIriOntologia() + " con la estrategia " + this.tarea.getMetodo() + e.getMessage());
		}
		
		exec.shutdown();
		return axioms;
	}

}
