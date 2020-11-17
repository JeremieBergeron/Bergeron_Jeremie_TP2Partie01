

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class ConnexionClient {
	Socket socketCommunication;
	PrintWriter out = null; // le flux de sortie de socket
	BufferedReader in = null;

	ConnexionClient(Socket socketCommunication) {
		this.socketCommunication = socketCommunication;
		try {
			out = new PrintWriter(socketCommunication.getOutputStream());
			in = new BufferedReader(new InputStreamReader(socketCommunication.getInputStream()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	String getEntete() {

		String s = null, enTete = null;

		try {
			// Permet de sauvegarder l'emplacement de la premi�re ligne
			in.mark(1000);

			// lecture de l'ent�te http
			// http est un protocole structur� en lignes
			while ((s = in.readLine()).compareTo("") != 0) {
				System.out.println("re�u: " + s);
				enTete += s + "\r\n";

			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		return enTete;
	}

	// Permet de r�cup�rer le nom du fichier demand�
	private String fichierDemande() {
		String fichierHtmlDemande = "";
		String[] strArray;
		try {
			//Permet de retourner au mark pr�c�dement r�alis�
			in.reset();
			
			//Permet d'obtenir le nom du fichier demand� parmit la requ�te
			strArray = in.readLine().split("\n");
			strArray = strArray[0].split(" ");
			fichierHtmlDemande = strArray[1];

		} catch (IOException e) {
			e.printStackTrace();

		}
		return fichierHtmlDemande;
	}
	
	//Permet de lire un fichier et de le retourner
	private String lireFichier(String chemin) throws FileNotFoundException {
		String fichierHtml = "";

		File myObj = new File(chemin);
		Scanner myReader = new Scanner(myObj);
		while (myReader.hasNextLine()) {
			fichierHtml += myReader.nextLine();
		}

		return fichierHtml;
	}

	//V�rifie si un fichier est prive ou non
	private boolean fichierPrive(String nomFichier) {
		String[] listeFichierPrive = { "/secret.html" };
		boolean fichierPrive = false;

		for (int i = 0; i < listeFichierPrive.length && !fichierPrive; i++) {
			if (listeFichierPrive[i].equals(nomFichier)) {
				fichierPrive = true;
			}
		}
		

		return fichierPrive;

	}
	/* le serveur pr�pare une r�ponse en format HTTP et L'envoie au client */

	void envoiReponse() {
		String nomFichierHtmlDemande, fichierHtml;

		nomFichierHtmlDemande = fichierDemande();

		if (!fichierPrive(nomFichierHtmlDemande)) {

			try {
				fichierHtml = lireFichier("src/exemple2/siteHTTP" + nomFichierHtmlDemande);

				int len = fichierHtml.length();
				out.print("HTTP-1.0 200 OK\r\n");
				out.print("Content-Length: " + len + "\r\n");
				out.print("Content-Type: text/html\r\n\r\n");
				out.print(fichierHtml);
				
				out.flush();
			} catch (FileNotFoundException e1) {
				String erreurHtml = "";
				erreurHtml += "<html>";
				erreurHtml += "<body>";
				erreurHtml += "<p>";
				erreurHtml += "Erreur, ce fichier n'existe pas";
				erreurHtml += "</p>";
				erreurHtml += "</body>";
				erreurHtml += "</html>";
				// longueur du corps de la r�ponse
				int len = erreurHtml.length();

				// envoie de la line de d�but, ent�tes, la ligne vide et le corps
				out.print("HTTP-1.0 404 Not Found\r\n");
				out.print("Content-Length: " + len + "\r\n");
				out.print("Content-Type: text/html\r\n\r\n");
				out.print(erreurHtml);

				out.flush();
			}
		} else {
			String priveHtml = "";
			priveHtml += "<html>";
			priveHtml += "<body>";
			priveHtml += "<p>";
			priveHtml += "Erreur, ce fichier est interdit d'acc�s";
			priveHtml += "</p>";
			priveHtml += "</body>";
			priveHtml += "</html>";
			// longueur du corps de la r�ponse
			int len = priveHtml.length();

			// envoie de la line de d�but, ent�tes, la ligne vide et le corps
			out.print("HTTP-1.0 401 Unauthorized\r\n");
			out.print("Content-Length: " + len + "\r\n");
			out.print("Content-Type: text/html\r\n\r\n");
			out.print(priveHtml);
			
			out.flush();
		}
	}

	/**
	 * cette m�thode ferme le flux
	 */
	void fermetureFlux() {
		try {
			in.close();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
