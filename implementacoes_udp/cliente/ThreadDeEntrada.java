package implementacoes_udp.cliente;

import java.net.DatagramPacket;

import pacotes.GerenciadorDePacote;

public class ThreadDeEntrada 
    extends Thread 
{

    private final UDPdoCliente udp;
    private final int atrasoDePropagacao;

    public ThreadDeEntrada (
        UDPdoCliente udp,
        int atrasoDePropagacao
    ) 
    {
        this.udp = udp;
        this.atrasoDePropagacao = atrasoDePropagacao;
    }

    public void run () 
    {

        byte[] dadosDeEntrada = new byte[ GerenciadorDePacote.getTamanhoCabecalho() ];
        DatagramPacket pacoteDeEntrada = 
            new DatagramPacket( dadosDeEntrada, dadosDeEntrada.length );

        try
        {

            while ( ! udp.aTransferenciaTerminou() )
            {

                udp.getSocket().receive( pacoteDeEntrada );

                if ( this.atrasoDePropagacao > 0 ) 
                {
                    sleep( this.atrasoDePropagacao );
                }
                
                int numDeACK = 
                    GerenciadorDePacote.decodificarNumDoPacote( dadosDeEntrada );
                    
                System.out.println( 
                    udp.getDenominacao() 
                        + ": Recebido ACK " 
                        + numDeACK 
                );
                
                udp.removeTimeoutTask( numDeACK );

                if ( numDeACK == -2 )
                {
                    udp.sinalizarTerminoDaTransferencia();
                }
                else if ( numDeACK != -1 )
                {
                    udp.atualizarJanela( numDeACK );
                }

            }

        }
        catch ( Exception e )
        {
            e.printStackTrace();
            System.exit( -1 );
        }
        finally
        {

            System.out.println( udp.getDenominacao() + ": Sequencia de pacotes enviada!" );
            
            try {
                udp.esvaziarListaDeTimeouts();
            } 
            catch ( Exception e )
            {
                e.printStackTrace();
            }
            
            udp.getSocket().close();

        }

    }
    
}
