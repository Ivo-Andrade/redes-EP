package simulacoes;

import java.net.InetAddress;
import java.util.SortedMap;
import java.util.TreeMap;

import maquinas.MaquinaCliente;
import maquinas.MaquinaRoteador;
import maquinas.MaquinaServidor;
import modelos.EnderecoDeMaquina;

public class SimulacaoBase_DualClient 
{

    public static void main ( String[] args ) 
        throws Exception
    {
            
        // Definições gerais de váriaveis

        int tamanhoDoPacote = 1000;
        int tempoDeTimeoutDoCliente = 300;

        // Nota: Valor máximo recomendado de 1024 pacotes, 
        //      dado que se mantem uma lista de status de ACK com este valor
        //      definindo seu tamamnho
        int tamanhoDaFilaDePacotesNoCliente = ( 64 );

        int tamanhoDaFilaDePacotesNoRoteador = ( Integer.MAX_VALUE );
        int tamanhoDoBufferDeRecepcaoNoServidor = ( Integer.MAX_VALUE );

        int atrasoDeRecepcaoNoServidor = 0;

        // Definição de endereços

        EnderecoDeMaquina servidor = 
            new EnderecoDeMaquina(
                "simulacaoBase_dualClient-Servidor",
                InetAddress.getLocalHost(),
                9999
            );

        EnderecoDeMaquina roteador = 
            new EnderecoDeMaquina(
                "simulacaoBase_dualClient-Roteador",
                InetAddress.getLocalHost(),
                9555
            );
        
        EnderecoDeMaquina cliente1 =
            new EnderecoDeMaquina(
                "simulacaoBase_dualClient-Cliente_1",
                InetAddress.getLocalHost(),
                9111
            );

        EnderecoDeMaquina cliente2 =
            new EnderecoDeMaquina(
                "simulacaoBase_dualClient-Cliente_2",
                InetAddress.getLocalHost(),
                9222
            );

        SortedMap<Integer,EnderecoDeMaquina> clientes = new TreeMap<>();
        clientes.put( 1, cliente1 );
        clientes.put( 2, cliente2 );

        // Definição de variáveis

        SortedMap<Integer,Integer> atrasosDePropagacao = new TreeMap<>();
        atrasosDePropagacao.put( 0, 0 );
        atrasosDePropagacao.put( 1, 0 );
        atrasosDePropagacao.put( 2, 0 );

        SortedMap<Integer,Integer> atrasosDeTransmissao = new TreeMap<>();
        atrasosDeTransmissao.put( 0, 1 );
        atrasosDeTransmissao.put( 1, 1 );
        atrasosDeTransmissao.put( 2, 1 );

        SortedMap<Integer,Double> probabilidadesDePerda = new TreeMap<>();
        probabilidadesDePerda.put( 0, 0.0 );
        probabilidadesDePerda.put( 1, 0.0 );
        probabilidadesDePerda.put( 2, 0.0 );

        // Definição de máquinas
        
        MaquinaServidor maquinaServidor = 
            new MaquinaServidor(
                servidor,
                roteador,
                clientes
            );

        maquinaServidor.setTamanhoDoPacote( tamanhoDoPacote );
        maquinaServidor.setTamanhoDaJanelaDeRepeticaoSeletiva( tamanhoDoBufferDeRecepcaoNoServidor );
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

        MaquinaCliente maquinaCliente1 = 
            new MaquinaCliente(
                1,
                cliente1,
                roteador,
                100000
            );

        maquinaCliente1.setTamanhoDeJanelaDeRepeticaoSeletiva( tamanhoDaFilaDePacotesNoCliente );
        maquinaCliente1.setTempoDeTimeout( tempoDeTimeoutDoCliente );

        maquinaCliente1.setAtrasoDePropagacao( atrasosDePropagacao.get( 1 ));
        maquinaCliente1.setAtrasoDeTransmissao( atrasosDeTransmissao.get( 1 ) );
        maquinaCliente1.setProbabilidadeDePerda( probabilidadesDePerda.get( 1 ) );

        MaquinaCliente maquinaCliente2 = 
            new MaquinaCliente(
                2,
                cliente2,
                roteador,
                100000
            );

        maquinaCliente2.setTamanhoDeJanelaDeRepeticaoSeletiva( tamanhoDaFilaDePacotesNoCliente );
        maquinaCliente2.setTempoDeTimeout( tempoDeTimeoutDoCliente );

        maquinaCliente2.setAtrasoDePropagacao( atrasosDePropagacao.get( 1 ));
        maquinaCliente2.setAtrasoDeTransmissao( atrasosDeTransmissao.get( 1 ) );
        maquinaCliente2.setProbabilidadeDePerda( probabilidadesDePerda.get( 1 ) );

        maquinaCliente1.run();
        maquinaCliente2.run();
   
    }
    
}
