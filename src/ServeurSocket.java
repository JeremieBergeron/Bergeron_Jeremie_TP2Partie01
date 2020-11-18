

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;



/**
 * @author dorsaf serveur qui attend la connexion d'un client sur un port X. Le
 *         serveur re�oit une requ�te d'un client et lui retourne un message en
 *         HTML Ex�cution: lancer le serveur et ensuite, ouvrez le navigateur et
 *         tapez l'adresse http://localhost:port. Observez le message sur le
 *         navigateur
 */

public class ServeurSocket {

    public static void main(String[] args) {
        new ServeurSocket().startServer();
    }

    public void startServer() {
        final ExecutorService clientProcessingPool = Executors.newFixedThreadPool(10);

        Runnable serverTask = new Runnable() {
            @Override
            public void run() {
                try {
                    ServerSocket socketServeur = new ServerSocket(8085);
                    while (true) {
                        Socket socketCommunication = socketServeur.accept();
                        clientProcessingPool.submit(new TacheClient(socketCommunication));
                    }
                } catch (IOException e) {
                    System.err.println("Erreur Client" + e.getMessage());
                }
            }
        };
        Thread serverThread = new Thread(serverTask);
        serverThread.start();

    }

    private class TacheClient implements Runnable {
        private final Socket socketCommunication;

        private TacheClient(Socket socketCommunication) {
            this.socketCommunication = socketCommunication;
        }

        @Override
        public void run() {
            System.out.println("un client a fait une connexion");

            ConnexionClient connexionClient = new ConnexionClient(socketCommunication);
            
			connexionClient.getEntete();
			
			// le serveur renvoie une réponse au client
			connexionClient.envoiReponse();

			connexionClient.fermetureFlux();

            try {
            	socketCommunication.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}