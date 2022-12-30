package maquinas.simulacao_base;

import java.util.SortedMap;

import implementacoes_udp.servidor.UDPdoServidor;
import modelos.EnderecoDeMaquina;

public class MaquinaServidor 
{

    private final EnderecoDeMaquina servidor;
    private final EnderecoDeMaquina roteador;
    private final SortedMap<Integer,EnderecoDeMaquina> clientes;

    public MaquinaServidor (
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

        UDPdoServidor udpDoServidor = 
            new UDPdoServidor(
                servidor.getNome(),
                servidor.getPorta()
            );
        
        udpDoServidor.setRoteador(
            new EnderecoDeMaquina(
                roteador.getNome(),
                roteador.getEnderecoIP(),
                roteador.getPorta()
            )
        );

        udpDoServidor.setClientes( this.clientes );

        udpDoServidor.start();
        
    }
    
}
