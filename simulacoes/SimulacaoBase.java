package simulacoes;

import java.net.InetAddress;
import java.util.SortedMap;
import java.util.TreeMap;

import maquinas.MaquinaCliente;
import maquinas.MaquinaRoteador;
import maquinas.MaquinaServidor;
import modelos.EnderecoDeMaquina;

public class SimulacaoBase 
{

    public static void main ( String[] args )
        throws Exception
    {

        // Definições gerais de váriaveis

        int tamanhoDoPacote = 1000;
        int tamanhoDaFilaDePacotesNoRoteador = Integer.MAX_VALUE ;
        int tamanhoDoBufferDeRecepcaoNoServidor = Integer.MAX_VALUE;
        int atrasoDeRecepcaoNoServidor = 0;

        // Definição de endereços

        EnderecoDeMaquina servidor = 
            new EnderecoDeMaquina(
                "simulacao_base-Servidor",
                InetAddress.getLocalHost(),
                9999
            );

        EnderecoDeMaquina roteador = 
            new EnderecoDeMaquina(
                "simulacao_base-Roteador",
                InetAddress.getLocalHost(),
                9555
            );

        
        EnderecoDeMaquina cliente =
            new EnderecoDeMaquina(
                "simulacao_base-Cliente",
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

        SortedMap<Integer,Integer> probabilidadesDePerda = new TreeMap<>();
        probabilidadesDePerda.put( 0, 0 );
        probabilidadesDePerda.put( 1, 0 );

        // Definição de máquinas
        
        MaquinaServidor maquinaServidor = 
            new MaquinaServidor(
                servidor,
                roteador,
                clientes
            );

        maquinaServidor.setTamanhoDoPacote( tamanhoDoPacote );
        maquinaServidor.setTamanhoDoBufferDeRecepcao( tamanhoDoBufferDeRecepcaoNoServidor );
        maquinaServidor.setAtrasoDeRecepcao( atrasoDeRecepcaoNoServidor );

        maquinaServidor.setAtrasoDePropagacao( atrasosDePropagacao.get( 0 ));
        maquinaServidor.setAtrasoDeTransmissao( atrasosDeTransmissao.get( 0 ));
        maquinaServidor.setProbabilidadeDePerda( probabilidadesDePerda.get( 0 ) );

        maquinaServidor.run();

        Thread.sleep( 1000 );

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

        Thread.sleep( 1000 );

        MaquinaCliente maquinaCliente = 
            new MaquinaCliente(
                1,
                cliente,
                roteador,
                1000000
            );

        maquinaCliente.setAtrasoDePropagacao( atrasosDePropagacao.get( 1 ));
        maquinaCliente.setAtrasoDeTransmissao( atrasosDeTransmissao.get( 1 ) );
        maquinaCliente.setProbabilidadeDePerda( probabilidadesDePerda.get( 1 ) );

        maquinaCliente.run();

    }
    
}
