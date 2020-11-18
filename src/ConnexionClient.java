
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

	/**
	 * 
	 * @return L'en tête de la requête Socket
	 */
	String getEntete() {

		String s = null, enTete = null;

		try {
			// Permet de sauvegarder l'emplacement de la première ligne de la requête
			in.mark(1000);

			// lecture de l'entête http
			while ((s = in.readLine()).compareTo("") != 0) {
				System.out.println("reçu: " + s);
				enTete += s + "\r\n";

			}

			// Permet de retourner au mark précédement réalisé
			in.reset();

		} catch (IOException e) {
			e.printStackTrace();
		}

		return enTete;
	}

	/**
	 * Le serveur prépare une réponse en format HTTP et l'envoie au client
	 */
	void envoiReponse() {
		String nomFichierHtmlDemande, contenuFichierHtml = null;
		int codeEtatHttp = -1, longueurFichier;

		// Permet d'obtenir le nom du fichier demandé
		nomFichierHtmlDemande = cheminFichierDemande();

		// Permet de regarder si le fichier demandé est privé ou non
		if (!fichierPrive(nomFichierHtmlDemande)) {

			try {
				// Lit le fichier demander
				contenuFichierHtml = lireFichier("src/siteHTTP" + nomFichierHtmlDemande);

				codeEtatHttp = 200;
				
			} catch (FileNotFoundException e1) {
				codeEtatHttp = 404;
			}
		} else {
			codeEtatHttp = 401;
		}

		// Selon le code d'état définie, il envoit la réponse au client
		switch (codeEtatHttp) {
		case 200:
			longueurFichier = contenuFichierHtml.length();

			out.print("HTTP-1.0 200 OK\r\n");
			out.print("Content-Length: " + longueurFichier + "\r\n");
			out.print("Content-Type: text/html\r\n\r\n");
			out.print(contenuFichierHtml);

			out.flush();
			break;

		case 401:
			String priveHtml = "";
			priveHtml += "<html>";
			priveHtml += "<body>";
			priveHtml += "<p>";
			priveHtml += "Erreur, ce fichier est interdit d'accès";
			priveHtml += "</p>";
			priveHtml += "</body>";
			priveHtml += "</html>";

			longueurFichier = priveHtml.length();

			out.print("HTTP-1.0 401 Unauthorized\r\n");
			out.print("Content-Length: " + longueurFichier + "\r\n");
			out.print("Content-Type: text/html\r\n\r\n");
			out.print(priveHtml);

			out.flush();
			break;

		case 404:
			String fichierNonTrouveHtml = "";
			fichierNonTrouveHtml += "<html>";
			fichierNonTrouveHtml += "<body>";
			fichierNonTrouveHtml += "<p>";
			fichierNonTrouveHtml += "Erreur, ce fichier n'existe pas";
			fichierNonTrouveHtml += "</p>";
			fichierNonTrouveHtml += "</body>";
			fichierNonTrouveHtml += "</html>";

			longueurFichier = fichierNonTrouveHtml.length();

			out.print("HTTP-1.0 404 Not Found\r\n");
			out.print("Content-Length: " + longueurFichier + "\r\n");
			out.print("Content-Type: text/html\r\n\r\n");
			out.print(fichierNonTrouveHtml);

			out.flush();
			break;

		default:
			String erreurHtml = "";
			erreurHtml += "<html>";
			erreurHtml += "<body>";
			erreurHtml += "<p>";
			erreurHtml += "Erreur, la syntaxe de la requête est erronée";
			erreurHtml += "</p>";
			erreurHtml += "</body>";
			erreurHtml += "</html>";

			longueurFichier = erreurHtml.length();

			out.print("HTTP-1.0 400 Bad Request\r\n");
			out.print("Content-Length: " + longueurFichier + "\r\n");
			out.print("Content-Type: text/html\r\n\r\n");
			out.print(erreurHtml);

			out.flush();
			break;
		}
	}
	
	/**
	 * @return Retourne le chemin du fichier dans l'en tête
	 */
	private String cheminFichierDemande() {
		String fichierHtmlDemande = "", enTete = getEntete();
		String[] strArray;

		// Sépare l'enTete pour isoler le nom du fichier
		strArray = enTete.split("\r\n");
		strArray = strArray[0].split(" ");

		// Le nom du fichier est récupéré
		fichierHtmlDemande = strArray[1];

		return fichierHtmlDemande;
	}

	/**
	 * Permet de lire un fichier et de le retourner. Si le fichier n'est pas trouvé,
	 * l'erreur sera gérer par la méthode qui l'appelera.
	 * 
	 * @param Le chemin du fichier
	 * @return Un string qui contient le contenu du fichier
	 */
	private String lireFichier(String chemin) throws FileNotFoundException {
		String fichierHtml = "";

		File fichier = new File(chemin);
		Scanner scanner = new Scanner(fichier);
		while (scanner.hasNextLine()) {
			fichierHtml += scanner.nextLine();
		}
		scanner.close();

		return fichierHtml;
	}

	/**
	 * Vérifie si un fichier est privée ou non
	 * 
	 * @param nomFichier
	 * @return
	 */
	private boolean fichierPrive(String nomFichier) {
		// Liste de tout les fichiers privés
		String[] listeFichierPrive = { "/secret.html" };

		boolean fichierPrive = false;

		// Vérifie si le nom du fichier se trouve dans la liste des fichiers privés
		for (int i = 0; i < listeFichierPrive.length && !fichierPrive; i++) {
			if (listeFichierPrive[i].equals(nomFichier)) {
				fichierPrive = true;
			}
		}

		return fichierPrive;
	}

	/**
	 * cette méthode ferme le flux
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
