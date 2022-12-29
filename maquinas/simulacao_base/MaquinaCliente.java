package maquinas.simulacao_base;

import java.net.InetAddress;

import implementacoes_udp.cliente.UDPdoCliente;
import lorem_ipsum.GeradorDeLoremIpsum;
import modelos.EnderecoDeMaquina;

public class MaquinaCliente 
{
    
    public static void main ( String[] args )
        throws Exception
    {

        String mensagem = GeradorDeLoremIpsum.gerarLoremIpsum( 10000000 );

        UDPdoCliente udpDoCliente = 
            new UDPdoCliente(
                1,
                "simulacao_base-Cliente",
                9111,
                mensagem
            );
        
        udpDoCliente.setDestinatario(
            new EnderecoDeMaquina(
                "simulacao_base-Roteador",
                InetAddress.getLocalHost(),
                9555
            )
        );

        udpDoCliente.start();

    }

}