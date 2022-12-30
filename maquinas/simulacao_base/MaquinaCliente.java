package maquinas.simulacao_base;

import java.net.InetAddress;

import implementacoes_udp.cliente.UDPdoCliente;
import lorem_ipsum.GeradorDeLoremIpsum;
import modelos.EnderecoDeMaquina;

public class MaquinaCliente 
{

    private int idDeCliente;

    private final EnderecoDeMaquina cliente;
    private final EnderecoDeMaquina roteador;

    private final int bytesDaMensagem;

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
                mensagem
            );
        
        udpDoCliente.setRoteador( roteador );

        udpDoCliente.start();

    }

}