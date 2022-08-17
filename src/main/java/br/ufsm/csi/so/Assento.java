package br.ufsm.csi.so;

public class Assento {

    private int id;
    private String nome;
    private String data;
    private String hora;
    private boolean ocupado;

    public Assento(int id) {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }

    public String getNome() {
        return this.nome;
    }

    public String getData() {
        return this.data;
    }

    public String getHora() {
        return this.hora;
    }

    public boolean isOcupado() {
        return this.ocupado;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setData(String data) {
        this.data = data;
    }

    public void setHora(String hora) {
        this.hora = hora;
    }

    public void setOcupado(boolean ocupado) {
        this.ocupado = ocupado;
    }
}
