package com.magadiflo.app.models;

import jakarta.persistence.*;

@Entity
@Table(name = "banks")
public class Bank {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @Column(name = "total_transfers")
    private int totalTransfers;

    public Bank() {
    }

    public Bank(Long id, String name, int totalTransfers) {
        this.id = id;
        this.name = name;
        this.totalTransfers = totalTransfers;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getTotalTransfers() {
        return totalTransfers;
    }

    public void setTotalTransfers(int totalTransfers) {
        this.totalTransfers = totalTransfers;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Bank{");
        sb.append("id=").append(id);
        sb.append(", name='").append(name).append('\'');
        sb.append(", totalTransfers=").append(totalTransfers);
        sb.append('}');
        return sb.toString();
    }
}
