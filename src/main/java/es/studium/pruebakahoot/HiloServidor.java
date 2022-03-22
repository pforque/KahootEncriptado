package es.studium.pruebakahoot;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class HiloServidor extends Thread {
	DataInputStream fentrada;
	ObjectInputStream entradaObjeto;
	ObjectOutputStream fentradaPregunta;
	Socket socket;
	String nombre;
	String nombreEncriptado = "";
	static boolean fin = false;
	int resultado=-1;
	int puntuacion = 0;

	public HiloServidor(Socket socket) {
		this.socket = socket;
		try {
			fentrada = new DataInputStream(socket.getInputStream());
		} catch (IOException e) {
			System.out.println("Error de E/S");
			e.printStackTrace();
		}
	}

	// En el método run() lo primero que hacemos
	// es enviar todos los mensajes actuales al cliente que se
	// acaba de incorporar
	public void run() {
		ServidorChat.mensaje.setText("Número de conexiones actuales: " + ServidorChat.ACTUALES);
		String texto = ServidorChat.textarea.getText();
		//EnviarMensajes(texto);
		// Seguidamente, se crea un bucle en el que se recibe lo que el cliente escribe en el chat.
		// Cuando un cliente finaliza con el botón Salir, se envía un * al servidor del Chat,
		// entonces se sale del bucle while, ya que termina el proceso del cliente,
		// de esta manera se controlan las conexiones actuales
		while (!fin) {
			String cadena = "";
			try {
				nombreEncriptado =  fentrada.readUTF();
				//entradaObjeto = new ObjectInputStream(socket.getInputStream());	
				//NombreCliente nombreEncriptado = (NombreCliente) entradaObjeto.readObject();
				
				if(!nombreEncriptado.equals("")) 
				{
					nombre = desencriptarNombre(nombreEncriptado);
					System.out.println("NOMBRE DESENCRIPTADO = " + nombre);
				}
				ServidorChat.textarea.append("Servidor> " + nombre);
				if(!nombre.equals(""))
				{
					this.setName(nombre);
				}
				System.out.println(this.getName() + "Nombre Jugador");
				if (cadena.trim().equals("*")) {
					ServidorChat.ACTUALES--;
					ServidorChat.mensaje.setText("Número de conexiones actuales: " + ServidorChat.ACTUALES);
					fin = true;
				}
				// El texto que el cliente escribe en el chat,
				// se añade al textarea del servidor y se reenvía a todos los clientes
				else {
					ServidorChat.textarea.append(cadena + "\n");

					Preguntas p = ObtenerPreguntas.consulta();
					System.out.println("PREGUNTA " + p.getEnunciado());
					ServidorChat.textarea.append(p.getEnunciado());
					
					p.setEnunciado(encriptarPregunta(p));
					p.setCorrecta("");
					p.setIncorrecta1("");
					p.setIncorrecta2("");
					p.setIncorrecta3("");
					
					System.out.println("Pregunta ENCRIPTADA = " + p.getEnunciado());
		
					
					EnviarMensajes(p);
					resultado = recibirResultado(); 
					System.out.println(resultado + "SiEsCorrecto");
					establecerPuntuacion();
					System.out.println(puntuacion + "PuntJugador");
					if (puntuacion == 5)
					{
						System.out.println("Ha Ganado: " + this.getName());
						String nombreSocket = this.getName();
						fin = true;
						for (int i = 0; i < ServidorChat.CONEXIONES; i++) {
							Socket socket = ServidorChat.tabla[i];
							try {
								Preguntas pre = new Preguntas();
								pre.setEnunciado("*");
								pre.setCorrecta(encriptarNombre(nombreSocket));
								fentradaPregunta = new ObjectOutputStream(socket.getOutputStream());
								fentradaPregunta.writeObject(pre);
							} catch (IOException e) {
								e.printStackTrace();
							}
						}

					}



				}
				} catch (Exception ex) {
					ex.printStackTrace();
					fin = true;
				}
			}
		}

		// El método EnviarMensajes() envía el texto del textarea a
		// todos los sockets que están en la tabla de sockets,
		// de esta forma todos ven la conversación.
		// El programa abre un stream de salida para escribir el texto en el socket
		private void EnviarMensajes(Preguntas pregunta) {
			//for (int i = 0; i < ServidorChat.CONEXIONES; i++) {
			//Socket socket = ServidorChat.tabla[i];
			try {
				fentradaPregunta = new ObjectOutputStream(socket.getOutputStream());
				fentradaPregunta.writeObject(pregunta);
			} catch (IOException e) {
				e.printStackTrace();
			}
			//}
		}

		private int recibirResultado() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeySpecException {
			//for (int i = 0; i < ServidorChat.CONEXIONES; i++) {
			//Socket socket = ServidorChat.tabla[i];
			int correcto = -1;
			try {
				fentrada = new DataInputStream(socket.getInputStream());
				String resultadoEncriptado = fentrada.readUTF();

				correcto =  desencriptarResultado(resultadoEncriptado);

			} catch (IOException e) {
				e.printStackTrace();
			}
			return correcto;
			//}
		}
		private void establecerPuntuacion() 
		{
			if(resultado == 1) 
			{
				puntuacion++;
			}
		}
		
		public String desencriptarNombre(String nombre) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeySpecException, IOException 
		{
			String nombreDesencriptado="";
			
			RSA rsa = new RSA();
			rsa.genKeyPair(2048);
			
			rsa.openFromDiskPrivateKey("rsa.pri");
			rsa.openFromDiskPublicKey("rsa.pub");			
			
			nombreDesencriptado = rsa.Decrypt(nombre);
			
			return nombreDesencriptado;
		}
		
		public int desencriptarResultado(String resEncriptado) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeySpecException, IOException 
		{
			String resDesencriptado="";
			
			RSA rsa = new RSA();
			rsa.genKeyPair(2048);
			
			rsa.openFromDiskPrivateKey("rsa.pri");
			rsa.openFromDiskPublicKey("rsa.pub");
			
			resDesencriptado = rsa.Decrypt(resEncriptado);
			
			return Integer.parseInt(resDesencriptado);
		}
		
		public String encriptarPregunta(Preguntas pregunta) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, IOException, InvalidKeySpecException, NoSuchProviderException 
		{
			String preguntaEncriptada;
			
			System.out.println("BD Enunciado= " + pregunta.getEnunciado());
			
			String preguntaToString = pregunta.getEnunciado() + "/" + pregunta.getCorrecta() + "/" 
			+ pregunta.getIncorrecta1() + "/" + pregunta.getIncorrecta2() + "/" + pregunta.getIncorrecta3();
			
			RSA rsa = new RSA();
			rsa.genKeyPair(2048);
			
			rsa.saveToDiskPrivateKey("rsa.pri");
			rsa.saveToDiskPublicKey("rsa.pub");
			
			preguntaEncriptada = rsa.Encrypt(preguntaToString);
			
			return preguntaEncriptada;
		}
		
		public String encriptarNombre(String nombre) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeySpecException, NoSuchProviderException, IOException 
		{
			String nombreEncriptado = "";
			
			RSA rsa = new RSA();
			rsa.genKeyPair(2048);
			
			rsa.saveToDiskPrivateKey("rsa.pri");
			rsa.saveToDiskPublicKey("rsa.pub");
			
			nombreEncriptado = rsa.Encrypt(nombre);
			
			System.out.println(nombreEncriptado);
			
			return nombreEncriptado;
		}


	}
