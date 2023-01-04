package implementacoes_udp.cliente;

import java.util.TimerTask;

public class TimeoutTask
    extends TimerTask
{

    private final UDPdoCliente udp;
    private final int numPacote;
    private final byte[] pacote;
    private final int iteracao;

    public TimeoutTask (
        UDPdoCliente udp,
        int numPacote,
        byte[] pacote,
        int iteracao
    )
    {
        this.udp = udp;
        this.numPacote = numPacote;
        this.pacote = pacote;
        this.iteracao = iteracao;
    }

    @Override
    public void run() 
    {
        
        try 
        {
            udp.getSemaforoDeReenvios().acquire();
            udp.adicionarPacoteEmTimeout( numPacote, pacote, iteracao );
            udp.getSemaforoDeReenvios().release();
        } 
        catch ( Exception e ) 
        {
			e.printStackTrace();
			System.exit(-1);
        }
        
    }
    
}
