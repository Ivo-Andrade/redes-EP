package maquinas.simulacao_base;

import java.util.SortedMap;
import java.util.TreeMap;

import implementacoes_udp.roteador.UDPdeRoteador;
import modelos.EnderecoDeMaquina;

public class MaquinaRoteador 
{

    private final EnderecoDeMaquina servidor;
    private final EnderecoDeMaquina roteador;
    private final SortedMap<Integer,EnderecoDeMaquina> clientes;

    public MaquinaRoteador (
        EnderecoDeMaquina servidor,
        EnderecoDeMaquina roteador,
        SortedMap<Integer,EnderecoDeMaquina> clientes
    )
    {
        this.servidor = servidor;
        this.roteador = roteador;
        this.clientes = clientes;
    }
    
    public void run ()
        throws Exception
    {

        UDPdeRoteador udpDoRoteador = 
            new UDPdeRoteador(
                roteador.getNome(),
                roteador.getPorta()
            );
        
        udpDoRoteador.setServidor(
            new EnderecoDeMaquina(
                servidor.getNome(),
                servidor.getEnderecoIP(),
                servidor.getPorta()
            )
        );

        udpDoRoteador.setClientes( clientes );

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
