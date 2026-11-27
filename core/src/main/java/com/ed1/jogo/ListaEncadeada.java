package com.ed1.jogo;

import java.util.Iterator;

/**
 * Lista encadeada simples, feita à mão com nós.
 *
 *   inicio -> [A] -> [B] -> [C] -> null
 *
 * Implementa Iterable só pra poder usar  for (X x : lista)  — o percurso em si
 * é o mesmo "vai de nó em nó pelo proximo" de sempre.
 */
public class ListaEncadeada<T> implements Iterable<T> {

    private static class No<T> {
        final T valor;
        No<T> proximo;

        No(T valor) {
            this.valor = valor;
        }
    }

    private No<T> inicio;
    private No<T> fim;
    private int tamanho;

    /** Coloca um valor no fim da lista. */
    public void adicionar(T valor) {
        No<T> novo = new No<>(valor);
        if (inicio == null) {
            inicio = novo;
        } else {
            fim.proximo = novo;
        }
        fim = novo;
        tamanho++;
    }

    /** Devolve o valor na posição i (0 = primeiro). */
    public T obter(int i) {
        if (i < 0 || i >= tamanho) {
            throw new IndexOutOfBoundsException("posicao " + i + " (tamanho " + tamanho + ")");
        }
        No<T> atual = inicio;
        for (int k = 0; k < i; k++) {
            atual = atual.proximo;
        }
        return atual.valor;
    }

    public int tamanho() {
        return tamanho;
    }

    public boolean estaVazia() {
        return tamanho == 0;
    }

    /** Percorre a lista nó por nó (usado pelo for-each). */
    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private No<T> atual = inicio;

            @Override
            public boolean hasNext() {
                return atual != null;
            }

            @Override
            public T next() {
                T valor = atual.valor;
                atual = atual.proximo;
                return valor;
            }
        };
    }
}