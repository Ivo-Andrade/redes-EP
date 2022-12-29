package implementacoes_udp.cliente;

import java.net.DatagramPacket;

import pacotes.GerenciadorDePacote;

public class ThreadDeEntrada 
    extends Thread 
{

    private final UDPdoCliente udp;

    public ThreadDeEntrada (
        UDPdoCliente udp
    ) 
    {
        this.udp = udp;
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
                
                int numDeACK = 
                    GerenciadorDePacote.decodificarNumDoPacote( dadosDeEntrada );
                    
                System.out.println( 
                    udp.getDenominacao() 
                        + ": Recebido ACK " 
                        + numDeACK 
                );
                
                udp.getSemaforoDasVarsDeTimeout().acquire();
                udp.removeTimeoutTask( numDeACK );
                udp.getSemaforoDasVarsDeTimeout().release();

                if ( numDeACK == -2 )
                {
                    udp.sinalizarTerminoDaTransferencia();
                }
                else if ( numDeACK != -1 )
                {

                    udp.getSemaforoDasVarsDeJanela().acquire();
                    udp.atualizarJanela( numDeACK );
                    udp.getSemaforoDasVarsDeJanela().release();
                    
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
            udp.esvaziarListaDeTimeouts();
            udp.getSocket().close();
        }

    }
    
}
