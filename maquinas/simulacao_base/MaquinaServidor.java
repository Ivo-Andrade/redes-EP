package maquinas.simulacao_base;

import java.net.InetAddress;
import java.util.SortedMap;
import java.util.TreeMap;

import implementacoes_udp.servidor.UDPdoServidor;
import modelos.EnderecoDeMaquina;

public class MaquinaServidor 
{

    public MaquinaServidor()
    {
        
    }
    
    public void main ( String[] args )
        throws Exception
    {

        UDPdoServidor udpDoServidor = 
            new UDPdoServidor(
                "simulacao_base-Servidor",
                9999
            );
        
        udpDoServidor.setRoteador(
            new EnderecoDeMaquina(
                "simulacao_base-Roteador",
                InetAddress.getLocalHost(),
                9555
            )
        );

        SortedMap<Integer,String> listaDeClientes = new TreeMap<>();

        listaDeClientes.put( 1, "simulacao_base-Cliente" );

        udpDoServidor.setClientes( listaDeClientes );

        udpDoServidor.start();
        
    }
    
}
