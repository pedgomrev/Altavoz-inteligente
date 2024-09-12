package es.us.dad.dadVertx.entidades;

import java.util.Objects;

public class Placa {
	private Integer idPlaca;
	private String nombre;
	
	public Placa() {
		super();
	}

	public Placa(Integer idPlaca, String nombre) {
		super();
		this.idPlaca = idPlaca;
		this.nombre = nombre;
	}

	public Placa(String nombre) {
		super();
		this.nombre = nombre;
	}
	public Integer getIdPlaca() {
		return idPlaca;
	}

	public void setIdPlaca(Integer idPlaca) {
		this.idPlaca = idPlaca;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	@Override
	public int hashCode() {
		return Objects.hash(idPlaca, nombre);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Placa other = (Placa) obj;
		return Objects.equals(idPlaca, other.idPlaca) && Objects.equals(nombre, other.nombre);
	}

	@Override
	public String toString() {
		return "Placa [idPlaca=" + idPlaca + ", nombre=" + nombre + "]";
	}
	
}
