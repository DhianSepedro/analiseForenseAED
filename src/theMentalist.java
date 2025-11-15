import br.edu.icev.aed.forense.AnaliseForenseAvancada;
import br.edu.icev.aed.forense.Alerta;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;
import java.util.ArrayList;
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
            String linha = br.readLine(); 
            
            while ((linha = br.readLine()) != null) {
                String[] campos = linha.split(",");
                
                String userId = campos[1];
                String sessionId = campos[2];
                String actionType = campos[3];
                
                pilhasDosUsuarios.putIfAbsent(userId, new Stack<>());
                Stack<String> pilha = pilhasDosUsuarios.get(userId);
                
                if (actionType.equals("LOGIN")) {
                    if (!pilha.isEmpty()) {
                        sessoesInvalidas.add(pilha.peek());
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
                
                if (sessionId.equals(campos[2])) {
                    fila.add(campos[3]);
                    linhaDoTempo.add(fila.poll());
                }
                


                
                
                
               
            }
            
        }
        linhaDoTempo.addAll(fila);
        return linhaDoTempo;
    }
     @Override
    public List<Alerta> priorizarAlertas(String arquivo, int n) throws IOException {
        // Implementar usando PriorityQueue<Alerta>
    }
    
    @Override
    public Map<Long, Long> encontrarPicosTransferencia(String arquivo) throws IOException {
        // Implementar usando Stack (Next Greater Element)
    }
    
    @Override
    public Optional<List<String>> rastrearContaminacao(String arquivo, String origem, String destino) throws IOException {
        // Implementar usando BFS em grafo
    }
}
