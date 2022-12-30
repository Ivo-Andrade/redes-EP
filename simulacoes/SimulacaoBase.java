package simulacoes;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import maquinas.simulacao_base.MaquinaCliente;
import maquinas.simulacao_base.MaquinaRoteador;
import maquinas.simulacao_base.MaquinaServidor;
import modelos.EnderecoDeMaquina;

public class SimulacaoBase 
{

    public static void main ( String[] args )
        throws Exception
    {

        // Definifição de endereços

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

        // Definifição de máquinas
        
        MaquinaServidor maquinaServidor = 
            new MaquinaServidor(
                servidor,
                roteador,
                clientes
            );
        maquinaServidor.run();

        Thread.sleep( 1000 );

        MaquinaRoteador maquinaRoteador = 
            new MaquinaRoteador(
                servidor,
                roteador,
                clientes
            );
        maquinaRoteador.run();

        Thread.sleep( 1000 );

        MaquinaCliente maquinaCliente = 
            new MaquinaCliente(
                1,
                cliente,
                roteador,
                1000000
            );
        maquinaCliente.run();

    }
    
}
