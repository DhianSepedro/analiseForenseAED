package br.edu.icev.aed.forense;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class theMentalist implements AnaliseForenseAvancada {  
    public theMentalist() {  
 }
    @Override
    public Set<String> encontrarSessoesInvalidas(String caminhoArquivo) throws IOException {
        Set<String> sessoesInvalidas = new HashSet<>();
        
        Map<String, Stack<String>> pilhasDosUsuarios = new HashMap<>();
        
        try (BufferedReader br = new BufferedReader(new FileReader(caminhoArquivo))) {
            br.readLine(); 
            String linha;
            
            while ((linha = br.readLine()) != null) {
                String[] campos = linha.split(",");
                 if (campos.length < 4) { 
                    continue;
                }
                String userId = campos[1];
                String sessionId = campos[2];
                String actionType = campos[3];
                
                pilhasDosUsuarios.putIfAbsent(userId, new Stack<>());
                Stack<String> pilha = pilhasDosUsuarios.get(userId);
                
                if (actionType.equals("LOGIN")) {
                    if (!pilha.isEmpty()) { //verifica se tem sessao aberta
                        sessoesInvalidas.add(pilha.peek()); //sessao anterior invalida
                    }
                    pilha.push(sessionId);
                }
                else if (actionType.equals("LOGOUT")) {
                    if (pilha.isEmpty() || !pilha.peek().equals(sessionId)) {
                        sessoesInvalidas.add(sessionId);
                    } else {
                        pilha.pop();
                    }
                }
            }
        }
        //adiciona sessoes que ficaram abertas
        for (Stack<String> pilha : pilhasDosUsuarios.values()) {
            sessoesInvalidas.addAll(pilha);
        }
        
        return sessoesInvalidas;
    }
    @Override
    public List<String> reconstruirLinhaTempo (String caminhoArquivo, String sessionId) throws IOException {
        List<String> linhaDoTempo = new ArrayList<>();
        Queue<String> fila = new LinkedList<>();
        
         try (BufferedReader br = new BufferedReader(new FileReader(caminhoArquivo))) {
            String linha = br.readLine(); 
            
            while ((linha = br.readLine()) != null) {
                String[] campos = linha.split(",");
                 if (campos.length < 4) { 
                    continue;
                }
                if (sessionId.equals(campos[2].trim())) {
                    fila.add(campos[3]);
                }
                     
               
            }
            
        }
        linhaDoTempo.addAll(fila);
        return linhaDoTempo;
    }
     @Override
    public List<Alerta> priorizarAlertas(String caminhoArquivo, int n) throws IOException {
        List<Alerta> alertasPrioritarios = new ArrayList<>(n); //entra n como parametro por enquanto, +otimizado
        PriorityQueue<Alerta> filaPrioridade = new PriorityQueue<>(
        (a1, a2) -> Integer.compare(a2.getSeverityLevel(), a1.getSeverityLevel()));

         try (BufferedReader br = new BufferedReader(new FileReader(caminhoArquivo))) {
            String linha = br.readLine(); 
            
            while ((linha = br.readLine()) != null) {
                String[] campos = linha.split(",", -1);
                
                if (campos.length < 7) {
                    continue; //pra pular linhas incompletas
                }
                long bytes = 0L; //inicializar bytes pra evitar erro de campo vazio
                String bytesStr = campos[6].trim();
                if (!bytesStr.isEmpty()) {
                    try {
                        bytes = Long.parseLong(bytesStr);
                    } catch (NumberFormatException e) {
                        bytes = 0L; //valor padrao
                    }
                }
                Alerta alerta = new Alerta(
                    Long.parseLong(campos[0].trim()),
                    campos[1],
                    campos[2],
                    campos[3],
                    campos[4],
                    Integer.parseInt(campos[5].trim()),
                    bytes
                );

                filaPrioridade.add(alerta);                  
            }
            
        }
         for (int i = 0; i < n && !filaPrioridade.isEmpty(); i++) {
        alertasPrioritarios.add(filaPrioridade.poll());
        }
        return alertasPrioritarios;
    }
    
    @Override
    public Map<Long, Long> encontrarPicosTransferencia(String caminhoArquivo) throws IOException {
        Map<Long, Long> resultado = new HashMap<>();
        List<long[]> eventos = new ArrayList<>();
    
        try (BufferedReader br = new BufferedReader(new FileReader(caminhoArquivo))) {
            String linha = br.readLine(); 
            
            while ((linha = br.readLine()) != null) {
            if (linha.isEmpty()) {
                continue;
            }
        
            String[] campos = linha.split(",");
            
            if (campos.length < 7) {
                continue;
            }
            
            //tratamento do campo vazio dos bytes
            String bytesStr = campos[6].trim();
            
            if (!bytesStr.isEmpty()) {
                try {
                    long bytes = Long.parseLong(bytesStr);
                    
                    if (bytes > 0) {
                        long timestamp = Long.parseLong(campos[0].trim());
                        eventos.add(new long[]{timestamp, bytes});
                    }
                } catch (NumberFormatException e) {
                    //campo bytes invalido, ignora o evento
                }
            }
        }
    }
    
    //Algoritmo Next Greater Element
    Stack<long[]> pilha = new Stack<>();
    //processa da direita pra esquerda para encontrar o proximo maior
    for (int i = eventos.size() - 1; i >= 0; i--) {
        long[] eventoAtual = eventos.get(i);
        long timestampAtual = eventoAtual[0];
        long bytesAtual = eventoAtual[1];
        
        //remove da pilha eventos menores que nunca serao o proximo maior
        while (!pilha.isEmpty() && pilha.peek()[1] <= bytesAtual) {
            pilha.pop();
        }
        
        //topo e o proximo maior
        if (!pilha.isEmpty()) {
            resultado.put(timestampAtual, pilha.peek()[0]);
        }
        
        //empilha o atual
        pilha.push(eventoAtual);
    }
    
    return resultado;
}
    
    @Override
    public Optional<List<String>> rastrearContaminacao(String caminhoArquivo, String recursoInicial, String recursoAlvo) throws IOException {
         Map<String, List<String>> sessoes = new HashMap<>(); //grafo
    
        try (BufferedReader br = new BufferedReader(new FileReader(caminhoArquivo))) {
             String linha = br.readLine(); 
             while ((linha = br.readLine()) != null) {
                  if (linha.isEmpty()) {
                    continue;
                 }
            
                String[] campos = linha.split(",");
            
                if (campos.length < 5) {
                  continue;
                }
            
                String sessionId = campos[2].trim();
                String targetResource = campos[4].trim();
            
                 //agrupa os recursos pelo id da sessao
                sessoes.putIfAbsent(sessionId, new ArrayList<>());
                sessoes.get(sessionId).add(targetResource);
        }
    }
    
        
        Map<String, List<String>> grafo = new HashMap<>();
        //construcao do grafo dentro das sessoes
        for (List<String> recursos : sessoes.values()) {
            for (int i = 0; i < recursos.size() - 1; i++) {
                String origem = recursos.get(i);
                String destino = recursos.get(i + 1);
                
                //arestas do grafo: origem -> destino
                grafo.putIfAbsent(origem, new ArrayList<>());
                grafo.get(origem).add(destino);
            }
        }
    
        //se recurso inicial = recurso alvo
        if (recursoInicial.equals(recursoAlvo)) {
            // Verifica se o recurso existe no grafo
            if (grafo.containsKey(recursoInicial) || sessoes.values().stream()
                .anyMatch(lista -> lista.contains(recursoInicial))) {
                List<String> caminho = new ArrayList<>();
                caminho.add(recursoInicial);
                return Optional.of(caminho);
            }
            return Optional.empty();
        }
    
        
        Queue<String> fila = new LinkedList<>();
        Set<String> visitados = new HashSet<>();
        Map<String, String> predecessor = new HashMap<>();
    
        fila.add(recursoInicial);
        visitados.add(recursoInicial);
        //BFS
        while (!fila.isEmpty()) {
            String atual = fila.poll();
        
            //se chegou no alvo, reconstroi o caminho
            if (atual.equals(recursoAlvo)) {
                List<String> caminho = new ArrayList<>();
                String no = recursoAlvo;
            
                while (no != null) {
                    caminho.add(no);
                    no = predecessor.get(no); //volta pelo predecessor
                }
            
                Collections.reverse(caminho); //inverte para ordem, do inicial ao alvo
                return Optional.of(caminho);
            }
        
            //explora vizinhos
            if (grafo.containsKey(atual)) {
                for (String vizinho : grafo.get(atual)) {
                    if (!visitados.contains(vizinho)) {
                        visitados.add(vizinho);
                        predecessor.put(vizinho, atual);
                        fila.add(vizinho);
                    }
                }
            }
        }
    
        //nao encontrou um caminho
        return Optional.empty();
    }
}
