package com.ed1.jogo;

public class Fila<T> {
    private static class No<T> { // cada nó da fila guarda um valor e aponta pra próxima
        final T valor;
        No<T> proximo;

        No(T valor) {
            this.valor = valor;
        }
    }

    private No<T> inicio; // quem sai primeiro
    private No<T> fim; // quem entrou por último
    private int tamanho;

    public void enfileirar(T valor) { // colocando valor no fim da fila
        No<T> novo = new No<>(valor);
        if (estaVazia()) {
            inicio = novo; // fila vazia: o novo é o primeiro e o último
        } else {
            fim.proximo = novo; // o antigo último passa a apontar pro novo
        }
        fim = novo;
        tamanho++;
    }

    public T desenfileirar() { // tirando e devolvendo o valor do início da fila
        if (estaVazia()) { // devolve null se estiver vazia
            return null;
        }
        T valor = inicio.valor;
        inicio = inicio.proximo;
        if (inicio == null) {
            fim = null; // esvaziou: não sobrou ninguém no fim também
        }
        tamanho--;
        return valor;
    }

    public T primeiro() {
        return estaVazia() ? null : inicio.valor; // se fila esta vazia, return null
    }

    public boolean estaVazia() {
        return inicio == null; // fila esta vazia se inicio for null
    }

    public int tamanho() { // tamanho da fila
        return tamanho;
    }
}