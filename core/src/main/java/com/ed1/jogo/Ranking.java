package com.ed1.jogo;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

/**
 * Tudo do ranking num lugar só:
 *  - VETOR:     Registro[]
 *  - ARQUIVO:   ranking.txt (uma linha por jogador, "nome;pontos")
 *  - ORDENAÇÃO: insertion sort feito à mão (maior pontuação primeiro)
 *  - BUSCA:     busca sequencial pelo nome
 */
public class Ranking {
    private static final String ARQUIVO = "ranking.txt";



    // ---------------- ARQUIVOS ----------------

    /** Lê o ranking.txt e devolve um vetor já ordenado. Se o arquivo não existe, vetor vazio. */
    public static Registro[] carregar() {
        FileHandle f = Gdx.files.local(ARQUIVO);
        if (!f.exists()) return new Registro[0];

        String[] linhas = f.readString("UTF-8").split("\\r?\\n");
        Registro[] temp = new Registro[linhas.length];
        int n = 0;
        for (String linha : linhas) {
            String[] partes = linha.split(";");
            if (partes.length != 2) continue; // ignora linha vazia/quebrada
            try {
                temp[n++] = new Registro(partes[0].trim(), Integer.parseInt(partes[1].trim()));
            } catch (NumberFormatException e) {
                // linha com pontos inválidos: ignora
            }
        }

        Registro[] v = new Registro[n];
        for (int i = 0; i < n; i++) v[i] = temp[i];
        ordenar(v);
        return v;
    }

    /** Grava o vetor inteiro no ranking.txt (sobrescreve). */
    public static void salvar(Registro[] v) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < v.length; i++) {
            sb.append(v[i].nome).append(';').append(v[i].pontos).append('\n');
        }
        Gdx.files.local(ARQUIVO).writeString(sb.toString(), false, "UTF-8");
    }

    // ---------------- VETOR ----------------

    /** Vetor não cresce, então criamos um novo com uma posição a mais. */
    public static Registro[] adicionar(Registro[] v, Registro novo) {
        Registro[] r = new Registro[v.length + 1];
        for (int i = 0; i < v.length; i++) r[i] = v[i];
        r[v.length] = novo;
        return r;
    }

    /** Carrega, adiciona o jogador, ordena e salva. Chame quando o jogo terminar. */
    public static void registrar(String nome, int pontos) {
        String limpo = nome.replace(';', ' ').trim();
        if (limpo.isEmpty()) limpo = "Jogador";
        Registro[] v = carregar();
        v = adicionar(v, new Registro(limpo, pontos));
        ordenar(v);
        salvar(v);
    }

    // ---------------- ORDENAÇÃO ----------------

    /** Insertion sort, decrescente por pontos. */
    public static void ordenar(Registro[] v) {
        for (int i = 1; i < v.length; i++) {
            Registro atual = v[i];
            int j = i - 1;
            while (j >= 0 && v[j].pontos < atual.pontos) {
                v[j + 1] = v[j];
                j--;
            }
            v[j + 1] = atual;
        }
    }

    // ---------------- BUSCA ----------------

    /**
     * Busca sequencial pelo nome (ignorando maiúsculas/minúsculas).
     * Como o vetor está ordenado por pontos, o primeiro achado é a melhor
     * pontuação daquele jogador. Devolve o índice, ou -1 se não achou.
     * A posição no ranking é índice + 1.
     */
    public static int buscar(Registro[] v, String nome) {
        String alvo = nome.trim();
        for (int i = 0; i < v.length; i++) {
            if (v[i].nome.equalsIgnoreCase(alvo)) return i;
        }
        return -1;
    }
}