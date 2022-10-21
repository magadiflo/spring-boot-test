package org.magadiflo.test.springboot.app.models;

import javax.persistence.*;

@Entity
@Table(name = "bancos")
public class Banco {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;

    @Column(name = "total_transferencia")
    private int totalTransferencia;

    public Banco() {
    }

    public Banco(Long id, String nombre, int totalTransferencia) {
        this.id = id;
        this.nombre = nombre;
        this.totalTransferencia = totalTransferencia;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getTotalTransferencia() {
        return totalTransferencia;
    }

    public void setTotalTransferencia(int totalTransferencia) {
        this.totalTransferencia = totalTransferencia;
    }
}
