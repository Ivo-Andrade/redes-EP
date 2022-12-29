package simulacoes;

import maquinas.simulacao_base.MaquinaCliente;
import maquinas.simulacao_base.MaquinaRoteador;
import maquinas.simulacao_base.MaquinaServidor;

public class SimulacaoBase 
{

    public static void main ( String[] args )
        throws Exception
    {

        MaquinaServidor.main( null );

        Thread.sleep( 1000 );

        MaquinaRoteador.main( null );

        Thread.sleep( 1000 );

        MaquinaCliente.main( null );

    }
    
}
