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

                sleep( this.atrasoDePropagacao );
                
                int numDeACK = 
                    GerenciadorDePacote.decodificarNumDoPacote( dadosDeEntrada );
                    
                System.out.println( 
                    udp.getDenominacao() 
                        + ": Recebido ACK " 
                        + numDeACK 
                );

                if ( numDeACK == -2 )
                {
                    udp.sinalizarTerminoDaTransferencia();
                }
                else if ( numDeACK != -1 )
                {

                    udp.getSemaforoDeTimeouts().acquire();
                    udp.removerTimeoutTask( numDeACK );
                    udp.getSemaforoDeTimeouts().release();

                    udp.getSemaforoDeFluxo().acquire();
                    udp.atualizarJanelaDeRepeticaoSeletiva( numDeACK );
                    udp.getSemaforoDeFluxo().release();
                    
                }

            }

            sleep( 5 );

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
