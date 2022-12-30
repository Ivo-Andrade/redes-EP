package maquinas.simulacao_base;

import java.util.SortedMap;

import implementacoes_udp.servidor.UDPdoServidor;
import modelos.EnderecoDeMaquina;

public class MaquinaServidor 
{

    private final EnderecoDeMaquina servidor;
    private final EnderecoDeMaquina roteador;
    private final SortedMap<Integer,EnderecoDeMaquina> clientes;
    
    private int atrasoDePropagacao;
    private int atrasoDeTransmissao;
    private int probabilidadeDePerda;

    public MaquinaServidor (
        EnderecoDeMaquina servidor,
        EnderecoDeMaquina roteador,
        SortedMap<Integer,EnderecoDeMaquina> clientes
    )
    {
        this.servidor = servidor;
        this.roteador = roteador;
        this.clientes = clientes;

        this.atrasoDePropagacao = 0;
        this.atrasoDeTransmissao = 0;
        this.probabilidadeDePerda = 0;
    }

    public void setAtrasoDePropagacao( int atrasoDePropagacao ) 
    {
        this.atrasoDePropagacao = atrasoDePropagacao;
    }

    public void setAtrasoDeTransmissao( int atrasoDeTransmissao ) 
    {
        this.atrasoDeTransmissao = atrasoDeTransmissao;
    }

    public void setProbabilidadeDePerda( int probabilidadeDePerda ) 
    {
        this.probabilidadeDePerda = probabilidadeDePerda;
    }
    
    public void run ()
        throws Exception
    {

        UDPdoServidor udpDoServidor = 
            new UDPdoServidor(
                servidor.getNome(),
                servidor.getPorta(),
                roteador,
                clientes
            );

        udpDoServidor.setAtrasoDePropagacao( this.atrasoDePropagacao );
        udpDoServidor.setAtrasoDeTransmissao( this.atrasoDeTransmissao );
        udpDoServidor.setProbabilidadeDePerda( this.probabilidadeDePerda );

        udpDoServidor.start();
        
    }
    
}
