import java.nio.charset.Charset;
import java.nio.file.Files;

import jade.core.*;
import java.io.IOException;
import java.util.List;
import java.nio.file.Paths;

public class AgenteSuma extends Agent
{
    private final String maquina = "Main-Container";
    private Location origen = null; 
    private int suma;
    private String filename = "";

    public void setup()
    {
        Object[] args = getArguments();
        if(args.length > 0) {
            this.filename = (String) args[0];
        }else {
            this.filename = "file";
        }

        this.origen = here();
        System.out.println("\n\nHola, agente con nombre local " + getLocalName());
        System.out.println(", nombre completo... " + getName());
        System.out.println("y en location " + origen.getID() + "\n\n");
        try {
            ContainerID destino = new ContainerID(this.maquina, null);
            System.out.println("Migrando el agente a " + destino.getID());
            doMove(destino);
        } catch (Exception e) {
            System.out.println("\nNo fue posible migrar el agente\n");
        }
    }

    protected void afterMove()
    {
        Location actual = here();

        if (!actual.getName().equals(this.origen.getName())) {
            try {
                List<String> numbers = Files.readAllLines(Paths.get(this.filename), Charset.forName("utf8"));
                int result = 0;
                for (String number: numbers) {
                    result += Integer.parseInt(number);
                }
                suma = result;
            } catch(NumberFormatException e) {
                System.out.println("Solo se admiten numeros");
            } catch(IOException e) {
                System.out.println("El archivo no existe");
            } catch(Exception e) {
                System.out.printf("Algo salio mal");
            }
            
            ContainerID destino = new ContainerID(this.origen.getName(), null);
            doMove(destino);
        } else {
            System.out.printf("La suma es: %d\n", this.suma);
        }
    }
}