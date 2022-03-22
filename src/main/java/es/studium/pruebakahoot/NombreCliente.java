package es.studium.pruebakahoot;

import java.io.Serializable;

public class NombreCliente implements Serializable {
	
	private static final long serialVersionUID = 1L;

	String nombre;
	
	public NombreCliente(String nombre) 
	{
		this.nombre=nombre;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	
	
}
