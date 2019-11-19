import jade.core.*;
import jade.util.leap.Serializable;
import jade.wrapper.ContainerController;
import java.util.ArrayList;
import java.util.Iterator;

public class AgenteInfo extends Agent
{

	private int index;
	private long startTime;
	private Location origen;
	private ArrayList<String> contenedores  = new ArrayList<String>();
	private ArrayList<ContainerInfo> info = new ArrayList<ContainerInfo>();
	
	// Ejecutado por unica vez en la creacion
	public void setup()
	{
		this.origen = here();

		System.out.println("\n\nHola, agente con nombre local " + getLocalName());
		System.out.println(", nombre completo... " + getName());
		System.out.println("Y en location " + this.origen.getID() + "\n\n");
		
		//Creacion de los contenedores
		for (int i=0; i<10;i++){
			
			String containerName="Contenedor-"+i;
			
			if (!containerName.equals(origen.getName())) {
				createContainer(containerName);
				contenedores.add(containerName);
			}
		}
		//agrego el origen para que sea el final de la cadena
		contenedores.add(this.origen.getName());
		
		try {
		
			index=0;
			// preparacion para migrar el agente
			ContainerID destino = new ContainerID(contenedores.get(index++), null);
			
			System.out.println("\nMigrando el agente a " + destino.getID());
			
			startTime = System.currentTimeMillis();    
			doMove(destino);
		} catch (Exception e) {
		
			System.out.println("\n\n\nNo fue posible migrar el agente\n\n\n");}
	}

	// Ejecutado al llegar a un contenedor como resultado de una migracion
	protected void afterMove()
	{
		Location actual = here();
		
		if (actual.getName().equals(origen.getName())){
			this.originAction();
		}
		else{
			this.remoteAction(actual);
		}
	}

	private void remoteAction(Location actual) {
		long startContainerTime =System.currentTimeMillis();    
		
		ContainerInfo currentContainerInfo= new ContainerInfo();
		
		System.out.println("\n\nHola, agente migrado con nombre local " + getLocalName());
		System.out.println("Y nombre completo... " + getName());
		
		currentContainerInfo.setFreeMemory(java.lang.Runtime.getRuntime().freeMemory());
		currentContainerInfo.setName(actual.getName());
		
		try {
			ContainerID destino = new ContainerID(contenedores.get(index++), null);
			
			System.out.println("\nMigrando el agente a " + destino.getID());
			
			info.add(currentContainerInfo);
			long finishContainerTime=System.currentTimeMillis() - startContainerTime;
			currentContainerInfo.setProcessingTime(finishContainerTime);
			
			doMove(destino);
		} catch (Exception e) {
			System.out.println("\n\nError al migrar el agente\n\n");
		}		
	}

	private void originAction() {
		long finishTime=System.currentTimeMillis() - startTime;
		System.out.println("\n\n-----------------------------------------------------------");
		System.out.println("Termine la vuelta, tiempo total: "+ finishTime +" milisegundos");
		
		for (ContainerInfo containerInfo : info) {
			System.out.println("\n\nInformacion container " + containerInfo.getName() + " :");
			System.out.println("  Memoria libre: "+ (containerInfo.getFreeMemory()/1024)/1024 + "Mb");
			float processingPercentage = (containerInfo.getProcessingTime()*100)/finishTime;
			System.out.println("  Carga de procesamiento: " + processingPercentage + "%");
			System.out.println("  Tiempo de procesamiento: " + containerInfo.getProcessingTime() + " milisegundos");
		}
	}

	protected ContainerController createContainer(String name){
		//Get the JADE runtime interface (singleton)
		jade.core.Runtime runtime = jade.core.Runtime.instance();
		//Create a Profile, where the launch arguments are stored
		Profile profile = new ProfileImpl();
		profile.setParameter(Profile.CONTAINER_NAME, name);
		profile.setParameter(Profile.MAIN_HOST, "localhost");
		//create a non-main agent 
		return runtime.createAgentContainer(profile);
	}
	
	public class ContainerInfo implements Serializable {
		
		private String name;
		private long processingTime;
		private long freeMemory;

		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public long getProcessingTime() {
			return processingTime;
		}
		public void setProcessingTime(long processingTime) {
			this.processingTime = processingTime;
		}
		public long getFreeMemory() {
			return freeMemory;
		}
		public void setFreeMemory(long freeMemory) {
			this.freeMemory = freeMemory;
		}
	}
}