package maquinas.simulacao_base;

import java.util.SortedMap;

import implementacoes_udp.roteador.UDPdeRoteador;
import modelos.EnderecoDeMaquina;

public class MaquinaRoteador 
{

    private final EnderecoDeMaquina servidor;
    private final EnderecoDeMaquina roteador;
    private final SortedMap<Integer,EnderecoDeMaquina> clientes;
    private final SortedMap<Integer,Integer> atrasosDePropagacao;
    private final SortedMap<Integer,Integer> atrasosDeTransmissao;
    private final SortedMap<Integer,Integer> probabilidadeDePerdas;

    public MaquinaRoteador (
        EnderecoDeMaquina servidor,
        EnderecoDeMaquina roteador,
        SortedMap<Integer,EnderecoDeMaquina> clientes,
        SortedMap<Integer,Integer> atrasosDePropagacao,
        SortedMap<Integer,Integer> atrasosDeTransmissao, 
        SortedMap<Integer, Integer> probabilidadesDePerda
    )
    {
        this.servidor = servidor;
        this.roteador = roteador;
        this.clientes = clientes;
        this.atrasosDePropagacao = atrasosDePropagacao;
        this.atrasosDeTransmissao = atrasosDeTransmissao;
        this.probabilidadeDePerdas = atrasosDeTransmissao;
    }
    
    public void run ()
        throws Exception
    {

        UDPdeRoteador udpDoRoteador = 
            new UDPdeRoteador(
                roteador.getNome(),
                roteador.getPorta(),
                servidor,
                clientes,
                atrasosDePropagacao,
                atrasosDeTransmissao,
                probabilidadeDePerdas
            ); 

        udpDoRoteador.start();

    }
    
}
