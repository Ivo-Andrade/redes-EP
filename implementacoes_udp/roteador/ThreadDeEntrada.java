package implementacoes_udp.roteador;

import java.net.DatagramPacket;

import pacotes.GerenciadorDePacote;

public class ThreadDeEntrada 
    extends Thread
{

    private final UDPdeRoteador udp;

    public ThreadDeEntrada ( UDPdeRoteador udp )
    {
        this.udp = udp;
    }

    public void run () 
    {

        try {

            while ( true ) {

                byte[] dadosDeEntrada = new byte[ udp.getTamanhoDoPacote() ];
                DatagramPacket pacoteDeEntrada = 
                    new DatagramPacket( dadosDeEntrada, dadosDeEntrada.length );

                udp.getSocket().receive( pacoteDeEntrada );
                
                if ( GerenciadorDePacote.verificarPacote( dadosDeEntrada, pacoteDeEntrada.getLength() ) )
                {
                    udp.adicionarPacoteAoBuffer( pacoteDeEntrada );
                }

            }

        } catch ( Exception e ) {
            e.printStackTrace();
            System.exit(-1);
        }

    }
    
}
