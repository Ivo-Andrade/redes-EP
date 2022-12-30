package simulacoes;

import maquinas.simulacao_base.MaquinaCliente;
import maquinas.simulacao_base.MaquinaRoteador;
import maquinas.simulacao_base.MaquinaServidor;

public class SimulacaoBase 
{

    public static void main ( String[] args )
        throws Exception
    {

        MaquinaServidor maquinaServidor = new MaquinaServidor();
        maquinaServidor.main( null );

        Thread.sleep( 1000 );

        MaquinaRoteador maquinaRoteador = new MaquinaRoteador();
        maquinaRoteador.main( null );

        Thread.sleep( 1000 );

        MaquinaCliente maquinaCliente = new MaquinaCliente( 1000000 );
        maquinaCliente.main( null );

    }
    
}
