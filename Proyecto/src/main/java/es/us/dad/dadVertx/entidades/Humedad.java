package es.us.dad.dadVertx.entidades;

import java.util.Objects;

public class Humedad {

	private Integer id;
	private Double valor;
	private Integer idPlaca;
	private Long fecha;
	
	public Humedad() {
		super();
	}
	
	public Humedad(Integer id,Double valor, Integer idPlaca, Long fecha) {
		super();
		this.id = id;
		this.valor = valor;
		this.idPlaca = idPlaca;
		this.fecha = fecha;
	}
	public Humedad(Double valor, Integer idPlaca, Long fecha) {
		super();
		this.valor = valor;
		this.idPlaca = idPlaca;
		this.fecha = fecha;
	}
	public Double getValor() {
		return valor;
	}
	public void setValor(Double valor) {
		this.valor = valor;
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
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}
	@Override
	public int hashCode() {
		return Objects.hash(fecha,valor, id, idPlaca);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Humedad other = (Humedad) obj;
		return Objects.equals(fecha, other.fecha) && Objects.equals(id, other.id)
				&& Objects.equals(idPlaca, other.idPlaca) && Objects.equals(valor, other.valor);
	}

	@Override
	public String toString() {
		return "Humedad [id=" + id + ", valor=" + valor + ", idPlaca=" + idPlaca + ", fecha=" + fecha + "]";
	}
	

}
