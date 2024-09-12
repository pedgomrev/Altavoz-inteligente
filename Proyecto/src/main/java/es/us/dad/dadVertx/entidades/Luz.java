package es.us.dad.dadVertx.entidades;

import java.util.Objects;

public class Luz {

	private Integer id;
	private Integer encendido;
	private Integer idPlaca;
	private Long fecha;
	
	public Luz() {
		super();
	}
	public Luz(Integer id,Integer encendido,Integer idPlaca,Long fecha) {
		super();
		this.id = id;
		this.encendido = encendido;
		this.fecha = fecha;
		this.idPlaca = idPlaca;
	}
	public Luz(Integer encendido,Integer idPlaca,Long fecha) {
		super();
		this.encendido = encendido;
		this.fecha = fecha;
		this.idPlaca = idPlaca;
	}
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Integer getEncendida() {
		return encendido;
	}
	public void setEncendida(Integer encendida) {
		this.encendido = encendida;
	}
	public Integer getIdPlaca() {
		return idPlaca;
	}
	public void setIdPlaca(Integer idPlaca) {
		this.idPlaca = idPlaca;
	}
	public Long getFecha() {
		return fecha;
	}
	public void setFecha(Long fecha) {
		this.fecha = fecha;
	}
	@Override
	public int hashCode() {
		return Objects.hash(encendido, fecha, id, idPlaca);
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Luz other = (Luz) obj;
		return Objects.equals(encendido, other.encendido) && Objects.equals(fecha, other.fecha)
				&& Objects.equals(id, other.id) && Objects.equals(idPlaca, other.idPlaca);
	}
	@Override
	public String toString() {
		return "Luz [id=" + id + ", encendido=" + encendido + ", idPlaca=" + idPlaca + ", fecha=" + fecha + "]";
	}
	
}
