package simulacoes;

import java.net.InetAddress;
import java.util.SortedMap;
import java.util.TreeMap;

import maquinas.MaquinaCliente;
import maquinas.MaquinaRoteador;
import maquinas.MaquinaServidor;
import modelos.EnderecoDeMaquina;

public class SimulacaoPerdaDePacote_ClienteSimples_8_1 
{

    public static void main ( String[] args ) 
        throws Exception
    {
            
        // Definições gerais de váriaveis

        int tamanhoDoPacote = 1000;
        int tempoDeTimeoutDoCliente = 500;

        // Nota: Valor máximo recomendado de 1024 pacotes, 
        //      dado que se mantem uma lista de status de ACK com este valor
        //      definindo seu tamamnho
        int tamanhoDaJanelaDeRepeticaoSeletiva = ( 8 );

        int tamanhoDaFilaDePacotesNoRoteador = ( Integer.MAX_VALUE );

        int atrasoDeRecepcaoNoServidor = 0;

        // Definição de endereços

        EnderecoDeMaquina servidor = 
            new EnderecoDeMaquina(
                "SimulacaoPerdaDePacote_ClienteSimples_8_1-Servidor",
                InetAddress.getLocalHost(),
                9999
            );

        EnderecoDeMaquina roteador = 
            new EnderecoDeMaquina(
                "SimulacaoPerdaDePacote_ClienteSimples_8_1-Roteador",
                InetAddress.getLocalHost(),
                9555
            );

        EnderecoDeMaquina cliente =
            new EnderecoDeMaquina(
                "SimulacaoPerdaDePacote_ClienteSimples_8_1-Cliente",
                InetAddress.getLocalHost(),
                9111
            );

        SortedMap<Integer,EnderecoDeMaquina> clientes = new TreeMap<>();
        clientes.put( 1, cliente );

        // Definição de variáveis

        SortedMap<Integer,Integer> atrasosDePropagacao = new TreeMap<>();
        atrasosDePropagacao.put( 0, 0 );
        atrasosDePropagacao.put( 1, 0 );

        SortedMap<Integer,Integer> atrasosDeTransmissao = new TreeMap<>();
        atrasosDeTransmissao.put( 0, 0 );
        atrasosDeTransmissao.put( 1, 0 );

        SortedMap<Integer,Double> probabilidadesDePerda = new TreeMap<>();
        probabilidadesDePerda.put( 0, 0.1 );
        probabilidadesDePerda.put( 1, 0.1 );

        // Definição de máquinas
        
        MaquinaServidor maquinaServidor = 
            new MaquinaServidor(
                servidor,
                roteador,
                clientes
            );

        maquinaServidor.setTamanhoDoPacote( tamanhoDoPacote );
        maquinaServidor.setTamanhoDaJanelaDeRepeticaoSeletiva( tamanhoDaJanelaDeRepeticaoSeletiva );
        maquinaServidor.setAtrasoDeRecepcao( atrasoDeRecepcaoNoServidor );

        maquinaServidor.setAtrasoDePropagacao( atrasosDePropagacao.get( 0 ));
        maquinaServidor.setAtrasoDeTransmissao( atrasosDeTransmissao.get( 0 ));
        maquinaServidor.setProbabilidadeDePerda( probabilidadesDePerda.get( 0 ) );

        maquinaServidor.run();

        Thread.sleep( 100 );

        MaquinaRoteador maquinaRoteador = 
            new MaquinaRoteador(
                servidor,
                roteador,
                clientes,
                atrasosDePropagacao,
                atrasosDeTransmissao,
                probabilidadesDePerda
            );

        maquinaRoteador.setTamanhoDoPacote( tamanhoDoPacote );
        maquinaRoteador.setTamanhoDaFilaDePacotes( tamanhoDaFilaDePacotesNoRoteador );

        maquinaRoteador.run();

        Thread.sleep( 100 );

        MaquinaCliente maquinaCliente = 
            new MaquinaCliente(
                1,
                cliente,
                roteador,
                1000000
            );

        maquinaCliente.setTamanhoDeJanelaDeRepeticaoSeletiva( tamanhoDaJanelaDeRepeticaoSeletiva );
        maquinaCliente.setTempoDeTimeout( tempoDeTimeoutDoCliente );

        maquinaCliente.setAtrasoDePropagacao( atrasosDePropagacao.get( 1 ));
        maquinaCliente.setAtrasoDeTransmissao( atrasosDeTransmissao.get( 1 ) );
        maquinaCliente.setProbabilidadeDePerda( probabilidadesDePerda.get( 1 ) );

        maquinaCliente.run();
   
    }
    
}
