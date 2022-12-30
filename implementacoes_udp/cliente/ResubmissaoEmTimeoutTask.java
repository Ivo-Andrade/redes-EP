package implementacoes_udp.cliente;

import java.util.TimerTask;

public class ResubmissaoEmTimeoutTask
    extends TimerTask
{

    private final UDPdoCliente udp;
    private final int numPacote;
    private final byte[] pacote;

    public ResubmissaoEmTimeoutTask (
        UDPdoCliente udp,
        int numPacote,
        byte[] pacote 
    )
    {
        this.udp = udp;
        this.numPacote = numPacote;
        this.pacote = pacote;
    }

    @Override
    public void run() 
    {
        
        try 
        {
            udp.reenviePacote( numPacote, pacote );
        } 
        catch ( Exception e ) 
        {
			e.printStackTrace();
			System.exit(-1);
        }
        
    }
    
}
