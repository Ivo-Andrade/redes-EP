package maquinas;

import implementacoes_udp.cliente.UDPdoCliente;
import lorem_ipsum.GeradorDeLoremIpsum;
import modelos.EnderecoDeMaquina;

public class MaquinaCliente 
{

    private int idDeCliente;

    private final EnderecoDeMaquina cliente;
    private final EnderecoDeMaquina roteador;

    private final int bytesDaMensagem;

    private int tamanhoDaJanelaDeRepeticaoSeletiva;
    private int tempoDeTimeout;

    private int atrasoDePropagacao;
    private int atrasoDeTransmissao;
    private double probabilidadeDePerda;

    public MaquinaCliente (
        int idDeCliente,
        EnderecoDeMaquina cliente,
        EnderecoDeMaquina roteador,
        int bytes 
    )
    {
        this.idDeCliente = idDeCliente;
        this.cliente = cliente;
        this.roteador = roteador;
        this.bytesDaMensagem = bytes;

        this.tamanhoDaJanelaDeRepeticaoSeletiva = -1;
        this.tempoDeTimeout = -1;

        this.atrasoDePropagacao = 0;
        this.atrasoDeTransmissao = 0;
        this.probabilidadeDePerda = 0;
    }

    public void setTamanhoDeJanelaDeRepeticaoSeletiva ( int tamanhoDaJanelaDeRepeticaoSeletiva ) 
    {
        this.tamanhoDaJanelaDeRepeticaoSeletiva = tamanhoDaJanelaDeRepeticaoSeletiva;
    }

    public void setTempoDeTimeout ( int tempoDeTimeout ) 
    {
        this.tempoDeTimeout = tempoDeTimeout;
    }

    public void setAtrasoDePropagacao ( int atrasoDePropagacao ) 
    {
        this.atrasoDePropagacao = atrasoDePropagacao;
    }

    public void setAtrasoDeTransmissao( int atrasoDeTransmissao ) 
    {
        this.atrasoDeTransmissao = atrasoDeTransmissao;
    }

    public void setProbabilidadeDePerda( double probabilidadeDePerda ) 
    {
        this.probabilidadeDePerda = probabilidadeDePerda;
    }
    
    public void run ()
        throws Exception
    {

        String mensagem = GeradorDeLoremIpsum.gerarLoremIpsum( this.bytesDaMensagem );

        UDPdoCliente udpDoCliente = 
            new UDPdoCliente(
                idDeCliente,
                cliente.getNome(),
                cliente.getPorta(),
                mensagem,
                roteador
            );

        if ( this.tamanhoDaJanelaDeRepeticaoSeletiva > 0 )
        {
            udpDoCliente.setTamanhoDaJanelaDeRepeticaoSeletiva( this.tamanhoDaJanelaDeRepeticaoSeletiva );
        }

        if ( this.tamanhoDaJanelaDeRepeticaoSeletiva > 0 )
        {
            udpDoCliente.setTempoDeTimeout( this.tempoDeTimeout );
        }

        udpDoCliente.setAtrasoDePropagacao( this.atrasoDePropagacao );
        udpDoCliente.setAtrasoDeTransmissao( this.atrasoDeTransmissao );
        udpDoCliente.setProbabilidadeDePerda( this.probabilidadeDePerda );

        udpDoCliente.start();

    }

}