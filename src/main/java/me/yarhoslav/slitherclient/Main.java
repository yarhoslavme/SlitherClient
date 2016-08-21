package me.yarhoslav.slitherclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import me.yarhoslav.actors.DecrypterActor;
import me.yarhoslav.ymactors.core.ActorsContainer;
import me.yarhoslav.ymactors.core.interfaces.IActorRef;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author yarhoslavme
 */
public class Main {

    private static final Logger LOG = LogManager.getLogger(Main.class);

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        LOG.traceEntry();

        ActorsContainer ac = new ActorsContainer("TEST");
        //TODO: For test junit
        ac.start();
        IActorRef ca = ac.crearActor("DECRYPTER", new DecrypterActor("mvzkltsfelsarzubipyhka"));
        LOG.debug(ca);
        ca.tell(DecrypterActor.MSGS.DECRYPT, ca);
        //ac.stop();

        BufferedReader buf = new BufferedReader(new InputStreamReader(System.in));
        try {
            buf.readLine();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }

        LOG.traceExit();

    }

}
