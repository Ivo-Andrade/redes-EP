package maquinas.simulacao_base;

import java.net.InetAddress;
import java.util.SortedMap;
import java.util.TreeMap;

import implementacoes_udp.roteador.UDPdeRoteador;
import modelos.EnderecoDeMaquina;

public class MaquinaRoteador 
{
    
    public static void main ( String[] args )
        throws Exception
    {

        UDPdeRoteador udpDoRoteador = 
            new UDPdeRoteador(
                "simulacao_base-Roteador",
                9555
            );
        
        udpDoRoteador.setServidor(
            new EnderecoDeMaquina(
                "simulacao_base-Servidor",
                InetAddress.getLocalHost(),
                9999
            )
        );

        SortedMap<Integer,EnderecoDeMaquina> listaDeClientes = 
            new TreeMap<>();

        EnderecoDeMaquina cliente =
            new EnderecoDeMaquina(
                "simulacao_base-Cliente",
                InetAddress.getLocalHost(),
                9111
            );

        listaDeClientes.put( 1, cliente );

        udpDoRoteador.setClientes( listaDeClientes );

        SortedMap<Integer,Integer> atrasosDePropagacao = 
            new TreeMap<>();

        atrasosDePropagacao.put( 0, 0 );
        atrasosDePropagacao.put( 1, 0 );

        udpDoRoteador.setAtrasoDePropagacao( atrasosDePropagacao );

        SortedMap<Integer,Integer> probabilidadeDePerdas = 
            new TreeMap<>();

        probabilidadeDePerdas.put( 0, 0 );
        probabilidadeDePerdas.put( 1, 0 );

        udpDoRoteador.setProbabilidadeDePerda( probabilidadeDePerdas );    

        udpDoRoteador.start();

    }
    
}
